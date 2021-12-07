/*
 * Copyright 2017 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adobe.cq.testing.client;

import com.adobe.cq.testing.client.workflow.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;

import org.joda.time.DateTime;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.*;

/**
 * Implements the Workflow REST API calls <br>
 * ModelID = handle to the model Node<br>
 */
public class WorkflowClient extends CQClient {

    /**
     * Request Path to manage workflow instances
     **/
    public static final String MANAGE_WF_INSTANCES_PATH = "/etc/workflow/instances";

    /**
     * Request Path to manage workflow models
     */
    public static final String MANAGE_WF_MODELS_PATH = "/etc/workflow/models";

    /**
     * Request Path to manage users workflow inbox
     */
    public static final String MANAGE_WF_INBOX_PATH = "/bin/workflow/inbox";

    /**
     * Request Path to manage workflow engine
     */
    public static final String MANAGE_WF_ENGINE_PATH = "/etc/workflow";

    /**
     * Type of model list to return, model id = model node handle
     */
    public static final String MODEL_LIST_TYPE_ID = "id";

    /**
     * Type of models list to return, uri = root context + model id
     */
    public static final String MODEL_LIST_TYPE_URI = "uri";

    /**
     * Defines what type of model definition gets passed, currently only JSON
     * format is implemented
     */
    public static final String MODEL_DESCR_TYPE_JSON = "JSON";

    /**
     * Type of payload referencing used when starting a workflow, in this case a
     * JCR Path
     */
    public static final String PAYLOAD_TYPE_JCR = "JCR_PATH";

    /**
     * Type of payload referencing used when starting a workflow, in this case a
     * URL Path
     */
    public static final String PAYLOAD_TYPE_URL = "URL";

    /**
     * Enumeration of the different Workflow Statuses
     */
    public enum Status {
        RUNNING("RUNNING"), SUSPENDED("SUSPENDED"), ABORTED("ABORTED"), COMPLETED("COMPLETED");

        private String status;

        /**
         * Initiates a enum with a string
         * 
         * @param status
         *            the string associated with this enum value
         */
        Status(String status) {
            this.status = status;
        }

        /**
         * returns the string associated with this status enum
         * 
         * @return the string value for this enum
         */
        public String getString() {
            return status;
        }
    }

    public WorkflowClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public WorkflowClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Creates a new workflow model in the repository by sending a model
     * definition.<br>
     * <br>
     * Handled by:<br>
     * <br>
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite\workflow
     * \console\servlet\ModelsServlet.java<br>
     *
     * @param modelSourcePath
     *            path to model
     * @param modelType
     *            if null or MODEL_DESCR_TYPE_JSON, the model description should
     *            be in JSON format.
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. if not set, http
     *            status 201 (CREATED) is assumed.
     *
     * @throws ClientException
     *             If something fails during request/response cycle
     * @return the path to the newly created model, equals to the models ID
     * @throws IOException
     *             if access to to the JSON resource fails
     */
    public String deployModel(String modelSourcePath, String modelType, int... expectedStatus) throws ClientException, IOException {
        // check for default value
        if (modelType == null)
            modelType = MODEL_DESCR_TYPE_JSON;
        // get the JSON definition
        String model = ResourceUtil.readResourceAsString(modelSourcePath);
        // build the request
        SlingHttpResponse response = doPost(MANAGE_WF_MODELS_PATH,
                FormEntityBuilder.create().addParameter("model", model).addParameter("type", modelType).build());
        // check the returned status
        HttpUtils.verifyHttpStatus(response, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
        // if it was a valid request
        if (response.getStatusLine().getStatusCode() == SC_CREATED) {
            // the location header will contain the URL to the newly deployed
            // model
            String url = response.getHeaders("Location")[0].getValue();

            // return the model id
            return getPath(url).toString();
        }
        return null;
    }

    /**
     * same as {@link #deployModel(String, String, int...)}, but with a list of strings to replace in the model
     * @param modelSourcePath resource path of the model
     * @param modelType model type
     * @param replaces list of strings pairs to replace
     * @param expectedStatus expected status of the http response
     * @return model id
     * @throws ClientException of the request failed
     * @throws IOException if the model cannot be read
     */
    public String deployModel(String modelSourcePath, String modelType, List<NameValuePair> replaces, int... expectedStatus)
            throws ClientException, IOException {
        // check for default value
        if (modelType == null)
            modelType = MODEL_DESCR_TYPE_JSON;
        // get the JSON definition
        String model = ResourceUtil.readResourceAsString(modelSourcePath);

        for (NameValuePair replacePair : (replaces != null) ? replaces : new ArrayList<NameValuePair>(0)) {
            model = model.replaceAll(replacePair.getName(), replacePair.getValue());
        }

        // build the request
        SlingHttpResponse response = doPost(MANAGE_WF_MODELS_PATH,
                FormEntityBuilder.create().addParameter("model", model).addParameter("type", modelType).build());
        // check the returned status
        HttpUtils.verifyHttpStatus(response, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
        // if it was a valid request
        if (response.getStatusLine().getStatusCode() == SC_CREATED) {
            // the location header will contain the URL to the newly deployed
            // model
            String url = response.getHeaders("Location")[0].getValue();

            // return the model id
            return getPath(url).toString();
        }
        return null;
    }

    /**
     * Advances a workflow item trough the next step.
     *
     * Handled by:
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\InboxServlet.java
     *
     * @param workItemId
     *            the work item to advance
     * @param routeId
     *            the route it should take
     * @param expectedStatus
     *            The HTTP status that should be returned, otherwise 200 is
     *            assumed.
     * @return The request executor if access to response is needed.
     * @throws ClientException
     *             if HTTP request fails for any reason.
     */
    public SlingHttpResponse advanceWorkItem(String workItemId, String routeId, int... expectedStatus) throws ClientException {
        SlingHttpResponse exec = doPost(MANAGE_WF_INBOX_PATH,
                FormEntityBuilder.create().addParameter("item", workItemId).addParameter("route", routeId).build());
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return executor
        return exec;
    }

	/**
	 * Choose the route and advance with it
	 *
	 * @param workflowInstancePath
	 *            the source of the workflow instance
	 * @param chooseRoute
	 *            based on the model at the expected step, the route that should
	 *            be used (0, 1, 2, etc..)
	 * @return The request executor as list if access to response is needed for
	 *         each of the workitems (in case more than one was found).
	 * @throws ClientException
	 *             if HTTP request fails for any reason.
	 */
	public List<SlingHttpResponse> chooseRouteAndAdvance(String workflowInstancePath, int chooseRoute)
			throws ClientException {
		List<SlingHttpResponse> responses = new ArrayList<>();
		WorkflowInstance wfInstance = getWorkflowInstance(workflowInstancePath);
		List<String> wfWkItemIds = wfInstance.getWorkItemIds();
		for (String wfWkItemId : wfWkItemIds) {
			WorkItem wkItem = getWorkItemByURI(wfWkItemId);
			ArrayList<WorkItem.Route> arrayRoutes = wkItem.getRoutes();
			responses.add(advanceWorkItem(wkItem.getId(), arrayRoutes.get(chooseRoute).getId()));
		}
		return responses;
	}

    /**
     * Advances a workflow item back to a previous step.
     *
     * Handled by:
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\InboxServlet.java
     *
     * @param workItemId
     *            the work item to advance
     * @param backrouteId
     *            the route it should take
     * @param expectedStatus
     *            The HTTP status that should be returned, otherwise 200 is
     *            assumed.
     * @return The request executor if access to response is needed.
     * @throws ClientException
     *             if HTTP request fails for any reason.
     */
    public SlingHttpResponse stepBackWorkItem(String workItemId, String backrouteId, int... expectedStatus) throws ClientException {
        SlingHttpResponse exec =  doPost(MANAGE_WF_INBOX_PATH,
                FormEntityBuilder.create().addParameter("item", workItemId).addParameter("backroute", backrouteId).build());
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return executor
        return exec;
    }

    /**
     * Delegates a workflow item to an other user
     *
     * Handled by:
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\InboxServlet.java
     *
     * @param workItemId
     *            the work item to delegate
     * @param delegatee
     *            the user/group this work item gets delegated to
     * @param expectedStatus
     *            The HTTP status that should be returned, otherwise 200 is
     *            assumed.
     * @return The request executor if access to response is needed.
     * @throws ClientException
     *             if HTTP request fails for any reason.
     */
    public SlingHttpResponse delegateWorkItem(String workItemId, String delegatee, int... expectedStatus) throws ClientException {
        SlingHttpResponse exec = doPost(MANAGE_WF_INBOX_PATH,
                FormEntityBuilder.create().addParameter("item", workItemId).addParameter("delegatee", delegatee).build());
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return executor
        return exec;
    }

    /**
     * Approves a workflow item and advanced to another node.
     *
     * Handled by:
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\InboxServlet.java
     *
     * @param workItem
     *            the work item to delegate
     * @param routeId
     *            the user/group this work item gets delegated to
     * @param expectedStatus
     *            The HTTP status that should be returned, otherwise 200 is
     *            assumed.
     * @return The request executor if access to response is needed.
     * @throws ClientException
     *             if HTTP request fails for any reason.
     */
    public SlingHttpResponse approveAndAdvanceWorkItem(WorkItem workItem, String routeId, int... expectedStatus) throws ClientException {

        doPost(workItem.getPayLoad(),
                FormEntityBuilder.create()
                        .addParameter("item", workItem.getId())
                        .addParameter("route", routeId)
                        .addParameter("./approved", "true")
                        .addParameter("./approved@Delete", "true")
                        .addParameter("./approved@TypeHint", "Boolean")
                        .build());

        SlingHttpResponse exec = doPost(MANAGE_WF_INBOX_PATH,
                FormEntityBuilder.create()
                        .addParameter("item", workItem.getId())
                        .addParameter("route", routeId)
                        .addParameter("./approved", "true")
                        .addParameter("./approved@Delete", "true")
                        .addParameter("./approved@TypeHint", "Boolean")
                        .build());

        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return executor
        return exec;
    }

    public SlingHttpResponse approveComment(String workItemId, String routeId, int... expectedStatus) throws ClientException {
        SlingHttpResponse exec = doPost(MANAGE_WF_INBOX_PATH,
                FormEntityBuilder.create().addParameter("item", workItemId).addParameter("route", routeId).addParameter("./approved", "true")
                        .addParameter("./approved@Delete", "true").addParameter("./approved@TypeHint", "Boolean").build());

        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return executor
        return exec;
    }


    /**
     * Returns the list of work item that are currently in the inbox of this
     * client's user. The returned map is using the work items uri as key.
     *
     * handled by:
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\InboxServlet.java
     *
     * @param expectedStatus
     *            The HTTP status that should returned, othewise 200 is assumed.
     * @return A map where key ist the uri of the stored work item
     * @throws ClientException
     *             if requesting the JSON fails
     */
    public Map<String, InboxItem> getInboxItems(int... expectedStatus) throws ClientException {
        // make the request
        SlingHttpResponse exec = doGet(MANAGE_WF_INBOX_PATH + ".json");
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // get the map
        JsonNode workItems = JsonUtils.getJsonNodeFromString(exec.getContent());
        Map<String, InboxItem> map = new HashMap<>();
        for (int i = 0; i < workItems.size(); i++) {
            JsonNode workItem = workItems.get(i);
            map.put(workItem.get("uri").asText(), new InboxItem(workItem));
        }
        return map;
    }

    /**
     * Returns all available information about a work item.
     *
     * @param uri the URI pointing to the work item node
     * @param expectedStatus the HTTP status that should be returned, otherwise 200 is assumed
     * @return the Work item wrapper
     * @throws ClientException if requesting the JSON fails for any reason
     */
    public WorkItem getWorkItemByURI(String uri, int... expectedStatus) throws ClientException {
        // make the request
        SlingHttpResponse exec = doGet(uri + ".json");
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return the wrapper
        return new WorkItem(JsonUtils.getJsonNodeFromString(exec.getContent()));
    }

    public List<String> getWorkItemBackRouteIds(String uri, int... expectedStatus) throws ClientException {
        // make the request
        SlingHttpResponse exec = doGet(uri + ".backroutes.json");
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return the wrapper
        JsonNode rootNode = JsonUtils.getJsonNodeFromString(exec.getContent()).get("backroutes");
        ArrayList<String> backrouteIds = new ArrayList<>();
        for (int i = 0; i < rootNode.size(); i++) {
            backrouteIds.add(rootNode.get(i).get("rid").asText());
        }

        return backrouteIds;
    }

    /**
     * Deletes a workflow model definition. Note that the model node does not
     * get removed. It only gets a property set in its Metadata sub node named
     * 'deleted' with a value of true.<br>
     * <br>
     * In order to solve firewall/proxy issues a {@code POST} that contains a
     * {@code X-HTTP-Method-Override} header entry with value {@code DELETE} is
     * sent instead of the proper {@code HTTP DELETE}.<br>
     * <br>
     * Handled by:<br>
     * <br>
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite\workflow
     * \console\servlet\ModelServlet.java
     *
     * @param modelId
     *            the id of the model to delete
     * @param expectedStatus
     *            HTTP status to be returned, otherwise 20 (NO CONTENT) is
     *            assumed
     * @throws ClientException
     *             if POST request fails for any reason
     * @return the request executor containing the response object and content.
     */
    public SlingHttpResponse deleteModel(String modelId, int... expectedStatus) throws ClientException {
        String url = modelId + ".json";
        SlingHttpResponse exec = doDelete(url, null, null);
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_NO_CONTENT, expectedStatus));
        return exec;
    }

    /**
     * Aborts/Terminates a running workflow instance.
     *
     * @param instanceId  instanceId (nodepath) of the running workflow instance
     * @param expectedStatus if not set it defaults to 200
     * @return the request executor containing the repsone object and content
     * @throws ClientException if POST fails for any reasons
     */
    public SlingHttpResponse abortWorkflow(String instanceId, int... expectedStatus) throws ClientException {
        // build the post request
        SlingHttpResponse exec = doPost(instanceId,  FormEntityBuilder.create().addParameter("state", "ABORTED")
                .addParameter("terminateComment", "").addParameter("_charset_","utf-8").build());
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return exec;
    }
    /**
     * updates an existing workflow model in the repository by sending a updated
     * model definition.<br>
     * <br>
     * Handled by:<br>
     * <br>
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite\workflow
     * \console\servlet\ModelServlet.java<br>
     *
     * @param modelSourcePath
     *            path to model
     * @param modelType
     *            if null or MODEL_DESCR_TYPE_JSON, the model description should
     *            bE in JSON format.
     * @param modelId
     *            the model to be updated
     * @param expectedStatus
     *            list of allowed HTTP Status to be returned. if not set, http
     *            status 200 (ok) is assumed.
     *
     * @throws ClientException
     *             If something fails during request/response cycle
     * @return The request executor used to send the request
     * @throws IOException
     *             if access to to the JSON resource fails
     */
    public SlingHttpResponse updateModel(String modelSourcePath, String modelType, String modelId, int... expectedStatus)
            throws ClientException, IOException {
        // check for default value
        if (modelType == null)
            modelType = MODEL_DESCR_TYPE_JSON;
        // get the JSON definition
        String model = getJsonDefinition(modelSourcePath, modelId);
        // build the request
        SlingHttpResponse exec = doPost(modelId, FormEntityBuilder.create().addParameter("model", model).addParameter("type", modelType)
                .build());
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return exec;
    }

    private String getJsonDefinition(String modelSourcePath, String modelId) throws IOException {

        // read data
        String model = ResourceUtil.readResourceAsString(modelSourcePath);

        // read as JSON
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.readValue(model, ObjectNode.class);

        // add the modelId
        node.put("id", modelId);

        // return the updated form
        return node.toString();
    }

    /**
     * Gets the workflow engine info from {@value #MANAGE_WF_ENGINE_PATH}.<br>
     * <br>
     * Handled by:<br> <br>
     * granite\bundles\workflow\content\src\main\content\jcr_root
     * \libs\cq\workflow\components\engine\json.jsp
     *
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 200 is assumed
     * @return the Workflow Engine wrapper
     * @throws ClientException
     *             if the request for the JSON data fails.
     */
    public WorkflowEngine getWorkflowEngineInfo(int... expectedStatus) throws ClientException {
        SlingHttpResponse exec = doGet(MANAGE_WF_ENGINE_PATH + ".json", expectedStatus);
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // parse the returned content into a JSON node
        JsonNode rootNode = JsonUtils.getJsonNodeFromString(exec.getContent());
        // turn properties into a map
        HashMap<String, String> properties = new HashMap<>();
        // iterate over properties
        rootNode.fieldNames();
        for (Iterator<String> it = rootNode.fieldNames(); it.hasNext();) {
            String propName = it.next();
            properties.put(propName, rootNode.get(propName).asText());
        }
        // return the result
        return new WorkflowEngine(properties);
    }

    /**
     * Stops the Workflow Engine, equals to stopping the Workflow service.
     *
     * Handled by:
     *
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite\workflow
     * \console\servlet\EngineServlet.java
     *
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 200 is assumed
     * @return returns the request executor, if more info from the response is
     *         required.
     * @throws ClientException
     *             if the POST request fails.
     */
    public SlingHttpResponse stopWorkflowEngine(int... expectedStatus) throws ClientException {
        // sent the new engine state to 'DISABLED'
        SlingHttpResponse exec = doPost(MANAGE_WF_ENGINE_PATH, FormEntityBuilder.create().addParameter("state", "DISABLED").build());
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return exec;
    }

    /**
     * Starts the Workflow Engine, equals to starting the Workflow service.
     *
     * Handled by:
     *
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite\workflow
     * \console\servlet\EngineServlet.java
     *
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 200 is assumed
     * @return returns the request executor, if more info from the response is
     *         required.
     * @throws ClientException
     *             if the POST request fails.
     */
    public SlingHttpResponse startWorkflowEngine(int... expectedStatus) throws ClientException {
        // sent the new engine state to 'ACTIVE'
        SlingHttpResponse exec = doPost(MANAGE_WF_ENGINE_PATH, FormEntityBuilder.create().addParameter("state", "ACTIVE").build());
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return exec;
    }

    /**
     * Checks if the workflow engine is in an active state (actually its the
     * workflow OSGi service that gets tested).
     *
     * @return true if the Workflow Engine is active otherwise false.
     * @throws ClientException
     *             if requesting Workflow Info fails
     */
    public boolean isWorkflowEngineActive() throws ClientException {
        return "active".equals(getWorkflowEngineInfo().getProperty(WorkflowEngine.STATE).toLowerCase());
    }

    /**
     * Convenience function for the most common case , using JCR path to
     * reference the payload.
     *
     * @param modelId
     *            the workflow model id to be used.
     * @param payload
     *            the payload JCR path to be set.
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 201 is assumed
     * @return the id of the new workflowInstance
     * @throws ClientException
     *             if the POST request fails
     */
    public String startWorkflow(String modelId, String payload, int... expectedStatus) throws ClientException {
        return startWorkflow(modelId, PAYLOAD_TYPE_JCR, payload, null, null, null, expectedStatus);
    }

    /**
     * Starts a new workflow using the given model id, with the payload
     * specified.
     *
     * Handled by:
     *
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite\workflow
     * \console\servlet\InstancesServlet.java
     *
     * @param modelId
     *            The workflow model Id to be used.
     * @param payloadType
     *            The type of payload referencing, either
     *            {@link #PAYLOAD_TYPE_JCR} or {@link #PAYLOAD_TYPE_URL} . if
     *            set to null , {#PAYLOAD_TYPE_JCR} is used as default
     * @param metaData
     *            any meta data that needs to be passed when starting the
     *            workflow
     * @param title
     *            title to be set for this workflow instance
     * @param comment
     *            comment to be set when starting the workflow
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 201 is assumed
     * @param payload
     *            the JCR path(s) or URL(s) pointing to the server resource that
     *            act as payload. each payload entry will start a new workflow
     *            instance
     * @return the id of the new workflowInstance
     * @throws ClientException
     *             if the POST request fails
     */
    public String startWorkflow(String modelId, String payloadType, String payload, Map<String, String> metaData, String title,
            String comment, int... expectedStatus) throws ClientException {
        // if modelId is not set, fail
        if (modelId == null || "".equals(modelId))
            throw new ClientException("Invalid ModelId!");
        // if no payload is given , fail
        if (payload == null || "".equals(payload))
            throw new ClientException(("No payload set!"));
        // set the default for the type if needed
        if (payloadType == null)
            payloadType = PAYLOAD_TYPE_JCR;
        // build the form entity
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("model", modelId);
        form.addParameter("payloadType", payloadType);
        form.addParameter("payload", payload);
        if (title != null && !"".equals(title)) {
            form.addParameter("workflowTitle", title);
        }
        if (comment != null && !"".equals(comment)) {
            form.addParameter("startComment", comment);
        }
        if (metaData != null) {
            for (String key : metaData.keySet()) {
                form.addParameter(key, metaData.get(key));
            }
        }
        // do the post
        SlingHttpResponse response = doPost(MANAGE_WF_INSTANCES_PATH, form.build());
        // check returned status
        HttpUtils.verifyHttpStatus(response, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
        // if successfully created
        if (response.getStatusLine().getStatusCode() == SC_CREATED) {
            // return the instance path
            return getPath(response.getSlingLocation()).toString();
        } else {
            return null;
        }
    }

    /**
     * Convenience functions to update/set the comment in the workflow instance
     * metadata.
     *
     * @param instanceURI
     *            the workflow instance to be updated
     * @param comment
     *            the comment of the workflow instance to be set
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 200 is assumed
     * @return the request executor containing the response object and content.
     * @throws ClientException
     *             if the POST request fails.
     */
    public SlingHttpResponse updateWfInstanceComment(String instanceURI, String comment, int... expectedStatus) throws ClientException {
        HashMap<String, String> map = new HashMap<>();
        map.put("startComment", comment);
        return updateWfInstanceMetaData(instanceURI, map, expectedStatus);
    }

    /**
     * Convenience function to set edit/set the title of a workflow instance in
     * its meta data.
     *
     * @param instanceURI
     *            the workflow instance to be updated
     * @param title
     *            the title of the workflow instance to be set
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 200 is assumed
     * @return the request executor containing the response object and content.
     * @throws ClientException
     *             if the POST request fails.
     */
    public SlingHttpResponse updateWfInstanceTitle(String instanceURI, String title, int... expectedStatus) throws ClientException {
        HashMap<String, String> map = new HashMap<>();
        map.put("workflowTitle", title);
        return updateWfInstanceMetaData(instanceURI, map, expectedStatus);
    }

    /**
     * Sends an update request to a workflow instance to update/add metadata
     *
     * @param instanceURI
     *            the workflow instance to be updated
     * @param metaData
     *            the metadata to be added /updated
     * @param expectedStatus
     *            The HTTP Status to be returned otherwise 200 is assumed
     * @return the request executor containing the response object and content.
     * @throws ClientException
     *             if the POST request fails.
     */
    public SlingHttpResponse updateWfInstanceMetaData(String instanceURI, Map<String, String> metaData, int... expectedStatus)
            throws ClientException {
        // build the form entity
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("action", "UPDATE");
        // add metadata
        for (String key : metaData.keySet()) {
            form.addParameter(key, metaData.get(key));
        }
        SlingHttpResponse exec = doPost(instanceURI, form.build());
        // check returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return exec;
    }

    /**
     * Returns a list of workflow instance URI's that are visible for the user.
     * Can be filtered by the instance status.<br>
     * <br>
     * Handled by :<br>
     * <br>
     * granite\bundles\workflow\content\src\main\content\jcr_root\libs\cq\
     * workflow\components\instances\json.jsp
     *
     * @param wfStatus
     *            Only returns workflows that are in one of the possible States
     *            ( {@link Status#RUNNING}, {@link Status#ABORTED},
     *            {@link Status#COMPLETED} or {@link Status#SUSPENDED}), or, if
     *            set to null, returns all workflow instances.
     * @param expectedStatus HTTP Status to check for or, if not set, 200 is assumed
     * @return A String List containing all workflow instance handles
     *         visible to this user according to the filter set in {@code wfStatus}.
     * @throws ClientException if the call to the backend requesting the JSON fails
     */
    public List<URI> getWorkflowInstanceURLs(Status wfStatus, int... expectedStatus) throws ClientException {
        // default Status
        String status = "";
        // if a specific Status filter is set
        if (wfStatus != null) {
            status = "." + wfStatus.getString();
        }
        // execute the request
        SlingHttpResponse exec = doGet(MANAGE_WF_INSTANCES_PATH + status + ".json");
        // check the returned status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // parse the returned content into a JSON node
        JsonNode rootNode = JsonUtils.getJsonNodeFromString(exec.getContent());
        // turn it into a string array
        ArrayList<URI> wfInstances = new ArrayList<>();
        // iterate over the URI's
        for (JsonNode aRootNode : rootNode) {
            try {
                wfInstances.add(new URI(aRootNode.get("uri").asText()));
            } catch (URISyntaxException e) {
                throw new ClientException("Error parsing url: " + aRootNode.get("uri").asText(), e);
            }
        }
        // return the result
        return wfInstances;
    }

    /**
     * Returns a list of all available workflow models for this user. Handled
     * by: granite\bundles\workflow\content\src\main\content\jcr_root\libs\cq\
     * workflow\components\models\json.jsp
     *
     * @param listType
     *            if set to MODEL_LIST_TYPE_ID , a list of model ID's gets
     *            returned (model ID = model Handle) if set to null or
     *            MODEL_LIST_TYPE_URI, a list of model URI's gets returned (URI
     *            = context path + model ID)
     * @param expectedStatus
     *            HTTP Status to check for or, if not set, 200 is assumed
     * @return A String array containing a list of all workflow model visible to
     *         this user
     * @throws ClientException
     *             if requesting the JSON fails for any reason
     */
    public ArrayList<String> getWorkflowModels(String listType, int... expectedStatus) throws ClientException {

        // set default value for listType if not set
        if (listType == null)
            listType = MODEL_LIST_TYPE_URI;

        // execute the request
        SlingHttpResponse exec = doGet(MANAGE_WF_MODELS_PATH + ".json",
                Collections.<NameValuePair>singletonList(new BasicNameValuePair("format", listType)),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        // parse the returned content into a JSON node
        JsonNode rootNode = JsonUtils.getJsonNodeFromString(exec.getContent());
        // turn it into a string array
        ArrayList<String> wfModels = new ArrayList<>();

        // depending on list type we need to read a different node property
        String nodeProperty = "uri";
        if (listType.equals(MODEL_LIST_TYPE_ID))
            nodeProperty = "value";

        // iterate over the returned entries
        for (JsonNode aRootNode : rootNode) {
            wfModels.add(aRootNode.get(nodeProperty).asText());
        }
        // return the result
        return wfModels;
    }

    /**
     * Returns the model definition (metadata, nodes, transitions,title
     * ,description etc)
     *
     * handled by:
     * \granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\ModelServlet.java
     *
     * @param modelId
     *            the model id to request
     * @param version
     *            the version to retrieve. Either 'HEAD' or null to get the
     *            latest version or in the format major.minor e.g. '1.2'.
     * @param expectedStatus
     *            the expected HTTP Status, if not set 200 is assumed
     * @return the requested version of the model definition in JSON format
     *
     * @throws ClientException
     *             If requesting the JSON fails for any reason
     */
    public String getWorkflowModelAsJSON(String modelId, String version, int... expectedStatus) throws ClientException {
        // if no specific version was requested
        if (version == null || "HEAD".equals(version)) {
            version = "";
        } else {
            // check the passed version pattern
            Pattern p = Pattern.compile("^\\d+\\.\\d+$");
            Matcher m = p.matcher(version);
            // if pattern does not match
            if (!m.find()) {
                // and invalid version number was passed then
                throw new IllegalArgumentException("Version identifier " + version + " is invalid!");
            }
            version = "." + version;
        }
        // request the json definition
        String uri = modelId + version + ".json";
        SlingHttpResponse exec = doGet(uri);
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return the response content
        return exec.getContent();
    }

    /**
     * Returns the model definition (metadata, nodes, transitions,title
     * ,description etc) in a wrapper class.
     *
     * handled by:
     * \granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\ModelServlet.java
     *
     * @param modelId
     *            the model id to request
     * @param version
     *            the version to retrieve. Either 'HEAD' or null to get the
     *            latest version or in the format major.minor e.g. '1.2'.
     * @param expectedStatus
     *            the expected HTTP Status, if not set 200 is assumed
     * @return the requested version of the model definition in JSON format
     *
     * @throws ClientException
     *             If requesting the JSON fails for any reason
     */
    public WorkflowModel getWorkflowModel(String modelId, String version, int... expectedStatus) throws ClientException {
        // get the json definition from the server and wrap it
        return new WorkflowModel(JsonUtils.getJsonNodeFromString(getWorkflowModelAsJSON(modelId, version, expectedStatus)));
    }

    /**
     * Returns all the information about a workflow instance in JSON format.
     *
     * Handled by:
     *
     * granite\bundles\workflow\content\src\main\content\jcr_root\libs\cq\
     * workflow\components\instance\json.jsp
     *
     * @param instanceURI
     *            the path to the workflow instance
     * @param expectedStatus
     *            the expected HTTP status, otherwise 200 is assumed
     * @return A WorkflowInstance that wraps the JSON response.
     * @throws ClientException
     *             if requesting the JSON fails
     */
    public WorkflowInstance getWorkflowInstance(String instanceURI, int... expectedStatus) throws ClientException {
        // get the data
        SlingHttpResponse exec = doGet(instanceURI + ".json");
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return the json in a light wrapper for easy data extraction
        return new WorkflowInstance(JsonUtils.getJsonNodeFromString(exec.getContent()));
    }

    public List<WorkflowInstance> getWorkflowInstances(Status wfStatus, String model, String payload, DateTime startTimeLimit,
                                                       DateTime endTimeLimit) throws ClientException {
        List<URI> urls = getWorkflowInstanceURLs(wfStatus);
        List<WorkflowInstance> workflows = new ArrayList<>();

        // get all the workflows and add them to a list
        for (URI url : urls) {
            WorkflowInstance workflowInstance;
            try {
                workflowInstance = getWorkflowInstance(getPath(url).toString());
            } catch (ClientException e) {
                continue;
            }

            Date start = workflowInstance.getStartTime();
            Date end = workflowInstance.getEndTime();
            DateTime wfStart = (null==start) ? null : new DateTime(start);
            DateTime wfEnd = (null==end) ? null : new DateTime(end);

            // skip the items if they don't match the criteria
            if (model != null && !workflowInstance.getModelId().equals(model))
                continue;
            if (payload != null && !workflowInstance.getPayload().startsWith(payload))
                continue;
            if (null != startTimeLimit  && null != wfStart && wfStart.isBefore(startTimeLimit)) {
                continue;
            }
            if (null != endTimeLimit  && null != wfEnd && wfEnd.isAfter(endTimeLimit)) {
                continue;
            }

            workflows.add(workflowInstance);
        }

        return workflows;
    }

    public List<WorkflowInstance> getWorkflowInstances(Status wfStatus) throws ClientException {
        return getWorkflowInstances(wfStatus, null, null, null, null);
    }

    public List<WorkflowInstance> getWorkflowInstances(Status wfStatus, DateTime startTimeLimit, DateTime endTimeLimit) throws ClientException {
        return getWorkflowInstances(wfStatus, null, null, startTimeLimit, endTimeLimit);
    }

    public List<WorkflowInstance> getWorkflowInstances() throws ClientException {
        return getWorkflowInstances(null);
    }

	/**
	 * Retrieve the workflow history of a specific workflow instance
	 *
	 * @param instanceURI
	 *            the workflow instance uri
	 * @param expectedStatus
	 *            provide the status code that is expected, default is 200 OK.
	 * @return the history items as a list
	 * @throws ClientException if the request failed
	 */
	public List<HistoryItem> getWorkflowInstanceHistory(String instanceURI, int... expectedStatus)
			throws ClientException {
		// get the data
        List<NameValuePair> params = Collections.<NameValuePair>singletonList(new BasicNameValuePair("workflow", instanceURI));
		SlingHttpResponse exec = doGet("/libs/cq/workflow/content/console/history.json", params,
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

		// return the json in a light wrapper for easy data extraction
		List<HistoryItem> listHistoryItems = new ArrayList<>();
		JsonNode workflowHistoryNode = JsonUtils.getJsonNodeFromString(exec.getContent());
		JsonNode historyNode = workflowHistoryNode.get("historyItems");
		Iterator<JsonNode> historyEntryIterator = historyNode.elements();
		while (historyEntryIterator.hasNext()) {
			// read the history items and store it in the list via wrapper
			// object
			listHistoryItems.add(new HistoryItem(historyEntryIterator.next()));
		}

		return listHistoryItems;
	}

    /**
     * Set the status of an running workflow instance.
     *
     * handled by:
     * granite\bundles\workflow\console\src\main\java\com\adobe\granite
     * \workflow\console\servlet\InstanceServlet.java
     *
     * @param instanceURI
     *            path to the instance url
     * @param status
     *            status to be set, either {@link Status#ABORTED},
     *            {@link Status#RUNNING} or {@link Status#SUSPENDED}
     * @param expectedStatus
     *            Expected HTTP status, otherwise 200 is assumed
     * @return the request executor if more info from response is required.
     * @throws ClientException
     *             if POST request has problems
     */
    public SlingHttpResponse setWorkflowInstanceStatus(String instanceURI, Status status, int... expectedStatus) throws ClientException {
        SlingHttpResponse exec = doPost(instanceURI, FormEntityBuilder.create().addParameter("state", status.getString()).build());
        // check return status
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // return the request executor
        return exec;
    }

    /**
     * Turns a string date returned by JSON into a Date object.
     * 
     * @param date
     *            the string to parse
     * @return the parsed Date object or null if parsing cannot be done
     */
    public static Date parseJSONDate(String date) {
        Locale store = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        try {
            return new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(date);
        } catch (ParseException e) {
            return null;
        } finally {
            Locale.setDefault(store);
        }
    }

}
