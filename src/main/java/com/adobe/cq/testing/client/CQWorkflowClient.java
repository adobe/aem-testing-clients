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

import com.adobe.cq.testing.client.workflow.EventType;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;


import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Extends the Granite Workflow client with CQ Specific Workflow methods
 */
public class CQWorkflowClient extends WorkflowClient {

    public static final String LAUNCHER_CONFIG = "/etc/workflow/launcher/config/";
    public static final String ROOT_PATH_MODEL = "/etc/workflow/models/";
    public static final String CQ_MODEL_PAGE_TEMPLATE = "/libs/cq/workflow/templates/model";
    public static final String SAVE_SUFFIX = "/jcr:content.generate.json";

    CQClient cqClient;

    public CQWorkflowClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
        this.cqClient = adaptTo(CQClient.class);
    }

    public CQWorkflowClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
        this.cqClient = adaptTo(CQClient.class);
    }

    /**
     * Creates a new Model page in the default directory {@value #ROOT_PATH_MODEL}.
     *
     * @param modelName name of the model
     * @param modelTitle title to be given to this model
     * @param expectedStatusRange expected HTTP status
     * @return the response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createNewModelPage(String modelName, String modelTitle, int... expectedStatusRange)
            throws ClientException {
        // create the model page
        return cqClient.createPage(modelName, modelTitle, ROOT_PATH_MODEL, CQ_MODEL_PAGE_TEMPLATE, expectedStatusRange);
    }

    /**
     * Saves a model.
     *
     * @param modelPath Name of the model to save
     * @param expectedStatus list of expected http status codes
     * @return the response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse saveModel(String modelPath, int... expectedStatus) throws ClientException {
        return doPost(modelPath + SAVE_SUFFIX, null, expectedStatus);
    }

    /**
     * Adds a new workflow launcher. This method only allows some parameters.
     *
     * @param workflowId the workflow URI
     * @param path the path it binds to
     * @param nodeType the node type it affects
     * @param eventType the event type to the node
     * @throws ClientException if the call to the backend fails
     */
    public void createWorkflowLauncher(String workflowId, String path, String nodeType, EventType eventType) throws ClientException {
        HttpEntity form = FormEntityBuilder.create()
                .addParameter("_charset_", "utf-8")
                .addParameter("add", "true")
                .addParameter("eventType", String.valueOf(eventType))
                .addParameter("nodetype", nodeType)
                .addParameter("glob", path)
                .addParameter("condition", "")
                .addParameter("workflow", workflowId)
                .addParameter("enabled", "true")
                .addParameter("runModes", "author")
                .addParameter(":status", "browser")
                .addParameter("description", "")
                .addParameter("excludeList", "")
                .build();

        doPost("/libs/cq/workflow/launcher", form, HttpStatus.SC_OK);
    }

    /**
     * <p>Edit one property of an existing workflow launcher identified by the launcher configuration name</p>
     * <p>The rest of the properties on the workflow launcher stay the same</p>
     *
     * @param launcherId the launcher configuration string
     * @param propName The name of the property about to be changed
     * @param value The value of the property about to be changed
     * @throws ClientException if the request fails
     */
    public void editWorkflowLauncher(String launcherId, String propName, String value)
            throws ClientException {
        Map<String, String> wv = new HashMap<>();
        wv.put(propName, value);
        editWorkflowLauncher(launcherId, wv);
    }

    /**
     * <p>Edit an existing workflow launcher identified by the launcher configuration name with a set of properties to change</p>
     * <p>The rest of the properties on the workflow launcher stay the same</p>
     *
     * @param launcherId the launcher configuration string
     * @param propMap A map with properties names and values to be changed on the workflow
     * @throws ClientException if the request fails
     */
    public void editWorkflowLauncher(String launcherId, Map<String, String> propMap)
            throws ClientException {
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("_charset_", "utf-8");
        form.addParameter(":status", "browser");
        form.addParameter("edit", LAUNCHER_CONFIG + launcherId);

        Map<String, String> launcher = getWorkflowLauncher(launcherId);
        for (String launcherProp : launcher.keySet()) {
            // skip if the value will be edited, populate with the existing one otherwise
            if (!propMap.containsKey(launcherProp)) {
                form.addParameter(launcherProp, launcher.get(launcherProp));
            }
        }

        // add the provided properties
        for (String propName : propMap.keySet()) {
            form.addParameter(propName, propMap.get(propName));
        }

        // Add exclude list property if not provided
        if (!propMap.keySet().contains("excludeList"))
            form.addParameter("excludeList", "");

        // save the launcher
        doPost("/libs/cq/workflow/launcher", form.build(), HttpStatus.SC_OK);
    }

    /**
     * Returns a {@link Map} with properties of the workflow launcher
     *
     * @param launcherId the URI of the launcher
     * @return a {@link Map} with properties
     * @throws ClientException if the request fails
     */
    public Map<String, String> getWorkflowLauncher(String launcherId) throws ClientException {
        JsonNode launchers = this.doGetJson("/etc/workflow/launcher/config", 1);
        JsonNode launcherNode = launchers.get(launcherId);

        if (null == launcherNode)
            return null;

        Map<String, String> launcher = new HashMap<>();
        Iterator<String> it = launcherNode.fieldNames();
        while (it.hasNext()) {
            String propName = it.next();
            launcher.put(propName, launcherNode.get(propName).asText());
        }

        return launcher;
    }

    /**
     * Enables or disables the workflow launcher
     *
     * @param launcherId the id of the launcher
     * @param enabled "true" to enable, "false" to disable
     * @throws ClientException if the request fails
     */
    public void enableDisableWorkflowLauncher(String launcherId, boolean enabled) throws ClientException {
        editWorkflowLauncher(launcherId, "enabled", enabled ? "true" : "false");
    }
    /**
     * Finds out if the Transient feature flag is enabled for a workflow.
     *
     * @param workflowModel
     *            the path of the workflow model. These are stored at
     *            <code>/etc/workflow/models/</code>. The model path to be
     *            provided can be relative (or absolute) to this path
     * @return <code>True</code> if the workflow is transient in nature,
     *         <code>False</code> otherwise.
     * @throws ClientException
     *             if it fails to find the specified workflow model.
     */
    public boolean isWorkflowTransient(String workflowModel)
            throws ClientException {
        workflowModel = workflowModel.startsWith("/etc/workflow/models/") ? workflowModel
                : "/etc/workflow/models/" + workflowModel;
        workflowModel = workflowModel.endsWith("/jcr:content") ? workflowModel
                : workflowModel+"/jcr:content" ;
        JsonNode json = this.adaptTo(JsonClient.class).doGetJson(workflowModel, 0);
        return (!json.path("transient").isMissingNode()) && (json.get("transient").asText().equalsIgnoreCase("true"));
    }
}
