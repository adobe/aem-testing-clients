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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.codehaus.jackson.JsonNode;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Client for all replication related actions: activate, deactivate, agents management.
 */
public class ReplicationClient extends CQClient {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ReplicationClient.class);

    public static final String AUTHOR_GROUP_PATH = "/etc/replication/agents.author";

    public static final String PUBLISH_REPLICATION_DEFAULT_AGENT = AUTHOR_GROUP_PATH + "/publish";
    public static final String PUBLISH_REVERSE_REPLICATION_DEFAULT_AGENT = AUTHOR_GROUP_PATH + "/publish_reverse";

    private static final String SYSTEM_USE_DISTRIBUTION = "granite.it.useDistribution";
    private static final boolean isDistribution = "true".equals(System.getProperty(SYSTEM_USE_DISTRIBUTION, null));

    public static final String DIST_AGENTS_PATH = "/libs/sling/distribution/services/agents";

    public ReplicationClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public ReplicationClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Activates(publish) a node on the given agent.
     * 
     * @param agent agent to send the replication request
     * @param nodePath path of the node to activate
     * @param expectedStatus list of expected HTTP status to be returned, if not set, 200 is assumed.
     * 
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse activate(String agent, String nodePath, int... expectedStatus) throws ClientException {
        FormEntityBuilder formEntityBuilder = FormEntityBuilder.create()
                .addParameter("cmd", "Activate")
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("path", nodePath);
                
        if (StringUtils.isNotBlank(agent)) {
            formEntityBuilder.addParameter("agentId", agent);
        }
        
        return doPost("/bin/replicate.json",
            formEntityBuilder.build(),
            HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
    
    /**
     * Activates (publish) a node.
     *
     * @param nodePath path of the node to activate
     * @param expectedStatus list of expected HTTP status to be returned, if not set, 200 is assumed.
     *
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse activate(String nodePath, int... expectedStatus) throws ClientException {
        return activate("", nodePath, expectedStatus);
    }

    /**
     * Deactivates (un-publish) a node on the specified agent.
     * 
     * @param agent agent to send the replication request
     * @param pagePath path of the node to deactivate
     * @param expectedStatus list of expected HTTP status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse deactivate(String agent, String pagePath, int... expectedStatus) throws ClientException {
        FormEntityBuilder formEntityBuilder = FormEntityBuilder.create()
            .addParameter("cmd", "Deactivate")
            .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
            .addParameter("path", pagePath);

        if (StringUtils.isNotBlank(agent)) {
            formEntityBuilder.addParameter("agentId", agent);
        }

        return doPost("/bin/replicate.json",
            formEntityBuilder.build(),
            HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
    
    /**
     * Deactivates (un-publish) a node.
     *
     * @param pagePath path of the node to deactivate
     * @param expectedStatus list of expected HTTP status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse deactivate(String pagePath, int... expectedStatus) throws ClientException {
        return deactivate("", pagePath, expectedStatus);
    }

    /**
     * Activates a page after a period of time.
     *
     * @param nodePath The path to the node which is activated
     * @param timeMilliseconds The time interval, in milliseconds, from current timestamp, after which the node will be activated
     * @param expectedStatus list of expected statuses
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse activateLater(String nodePath, long timeMilliseconds, int... expectedStatus)
            throws ClientException {

        Calendar now = Calendar.getInstance();
        long currentTime = now.getTimeInMillis();
        long scheduledTime = currentTime + timeMilliseconds;

        HttpEntity entity = FormEntityBuilder.create()
                .addParameter("model", "/etc/workflow/models/scheduled_activation/jcr:content/model")
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("absoluteTime", Long.toString(scheduledTime))
                .addParameter("payload", nodePath)
                .addParameter("payloadType", "JCR_PATH")
                .build();

        return doPost("/etc/workflow/instances", entity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Deactivates a page after a period of time.
     *
     * @param nodePath The path to the node which is deactivated
     * @param timeMilliseconds The time interval, in milliseconds, from current timestamp, after which the node will be deactivated
     * @param expectedStatus list of expected statuses
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse deactivateLater(String nodePath, long timeMilliseconds, int... expectedStatus)
            throws ClientException {

        Calendar now = Calendar.getInstance();
        long currentTime = now.getTimeInMillis();
        long scheduledTime = currentTime + timeMilliseconds;
        HttpEntity entity = FormEntityBuilder.create()
                .addParameter("model", "/etc/workflow/models/scheduled_deactivation/jcr:content/model")
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("absoluteTime", Long.toString(scheduledTime))
                .addParameter("payload", nodePath)
                .addParameter("payloadType", "JCR_PATH").build();

        return doPost("/etc/workflow/instances", entity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Creates a replication agent, if not already present
     *
     * @param name name of the agent
     * @param title title of the agent (page)
     * @param parentPath where to create the agent. Typical paths for CQ are
     *                   {@code /etc/replication/agents.author/} and {@code /etc/replication/agents.publish/}
     * @param template template of the agent. Typical value is {@code /libs/cq/replication/templates/agent}
     * @param properties list of properties to be set on the agent
     *
     * @return the json representation of agent's (jcr:content) node
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode createReplicationAgent(String name, String title, String parentPath, String template,
                                           BasicNameValuePair... properties)
            throws ClientException, InterruptedException {
        SlingHttpResponse page = createPage(name, title, parentPath, template, SC_CREATED, SC_OK);
        String agentPath = page.getSlingPath();

        return adaptReplicationAgent(agentPath, properties);
    }

    /**
     * Adapts an existing agent's properties
     *
     * @param agentPath the path of the agent
     * @param properties list of properties to be set on the agent
     *
     * @return the json representation of the agent's (jcr:content) node
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode adaptReplicationAgent(final String agentPath, BasicNameValuePair... properties)
            throws ClientException, InterruptedException {
        if ("".equals(agentPath) || agentPath == null) {
            throw new IllegalArgumentException("agentPath may not be null.");
        }

        final HttpEntity entity = FormEntityBuilder.create()
                .addAllParameters(Arrays.<NameValuePair>asList(properties))
                .build();

        try {
            new Polling() {
                @Override
                public Boolean call() throws Exception {
                    doPost(agentPath + "/jcr:content", entity, SC_OK);
                    return true;
                }
            }.poll(10000, 100);
        } catch (TimeoutException e) {
            throw new ClientException("Failed to adapt replication agent" + agentPath, e);
        }

        return doGetJson(agentPath, -1).get("jcr:content");
    }

    /**
     * Returns the agent replication queue.
     *
     * @param agentPath The location in jcr where the agent is stored.
     * @return the queue as json node
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode getAgentReplicationQueue(String agentPath) throws ClientException, InterruptedException {
        try {
            waitExists(agentPath + "/_jcr_content.queue", 5000, 500);
        } catch (TimeoutException e) {
            throw new ClientException("Failed to retrieve queue for replication agent " + agentPath, e);
        }
        return doGetJson(agentPath + "/_jcr_content.queue", -1, SC_OK).get("queue");
    }

    /**
     * Checks whether the given agent exists.
     *
     * @param agent to check
     * @return true if the given agent exists
     * @throws ClientException if an error occurred
     */
    public Boolean checkContentDistributionAgentExists(String agent) throws ClientException {
        JsonNode agents = doGetJson(DIST_AGENTS_PATH, 3);
        log.info("Replication agents list: {}", agents);
        if (agents.path(agent).isMissingNode()) {
            log.warn("Default distribution agent {} is missing from the distribution list", agent);
            return false;
        }
        return true;
    }

    /**
     * Checks if the item is empty or if not whether the replicated path is in the list of packages.
     * Sample HTTP response
     * <pre>
     * {
     *   "name": "publish",
     *   "queues": {
     *     "items": [
     *       "7ece8e2c-d641-4d50-a0ff-6fcd3c52f7d8-publishSubscriber",
     *       "0c77809c-cb61-437b-981b-0424c042fd92-publishSubscriber"
     *     ],
     *     "7ece8e2c-d641-4d50-a0ff-6fcd3c52f7d8-publishSubscriber": {
     *       "state": "BLOCKED",
     *       "items": [
     *         "package-0@52734825"
     *       ],
     *       "itemsCount": 1,
     *       "empty": false,
     *       "package-0@52734825": {
     *         "size": 6073,
     *         "paths": [
     *           "/content/test-site/testpage_8e57dec8-40e8-4e42-a267-ff5142f5c472"
     *         ],
     *         "errorMessage": "Failed attempt (12/infinite) to import the distribution package"
     *     },
     *     "0c77809c-cb61-437b-981b-0424c042fd92-publishSubscriber": {
     *       "sling:resourceType": "sling/distribution/service/agent/queue",
     *       "state": "IDLE",
     *       "items": [],
     *       "itemsCount": 0,
     *       "empty": true
     *     }
     *   },
     *   "log": {
     *     "sling:resourceType": "sling/distribution/service/log"
     *   },
     *   "status": {
     *     "state": "BLOCKED"
     *   }
     * }
     * </pre> 
     *
     * @param agentPath the agent path for which queues to be asserted
     * @param replicatedPath the path replicated
     * @return true if path empty
     * @throws Exception the exception
     */
    public boolean waitQueueEmptyOfPath(String agentPath, String replicatedPath) throws Exception {
        JsonNode queuesJson = doGetJson(agentPath, 2, 200, 300).get("queues");
        log.debug("queuesJson for agentPath {} is {}", agentPath, queuesJson);
        Set<String> queueIds = elementsAsText(queuesJson.get("items"));

        for (String queueId : queueIds) {
            JsonNode queueJson = queuesJson.get(queueId);

            boolean isEmpty = queueJson.get("empty").getBooleanValue();
            log.debug("Queue {} is empty {}", queueId, isEmpty);

            if (!isEmpty) {
                Set<String> pkgs = elementsAsText(queueJson.get("items"));
                for (String pkg : pkgs) {
                    JsonNode pkgJson = queueJson.get(pkg);

                    if (pkgJson != null) {
                        Set<String> paths = elementsAsText(pkgJson.get("paths"));

                        if (paths.contains(replicatedPath)) {
                            log.warn("The replication queue {} is blocked by the item {} with paths {} due to {}.",
                                queueId, pkg, paths, pkgJson.get("errorMessage"));
                        }
                    }
                    log.warn("The replication queue {} is blocked by the item {}.", queueId, pkg);
                    return false;
                }
            }
        }
        return true;
    }

    private static Set<String> elementsAsText(JsonNode queue) {
        return elements(queue).map(JsonNode::getTextValue).collect(Collectors.toSet());
    }

    private static Stream<JsonNode> elements(JsonNode node) {
        if (node == null ) {
            return Stream.empty();
        }
        Iterator<JsonNode> elementsIt = node.getElements();
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(elementsIt, Spliterator.ORDERED), false);
    }
    
    /**
     * Returns the default replication agent queue.
     *
     * @return the queue as json node
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */

    public JsonNode getPublishReplicationQueue() throws ClientException, InterruptedException {
        return getAgentReplicationQueue(PUBLISH_REPLICATION_DEFAULT_AGENT);
    }

    /**
     * Returns the reverse replication agent queue.
     *
     * @return the queue as json node
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode getReverseReplicationQueue() throws ClientException, InterruptedException {
        return getAgentReplicationQueue(PUBLISH_REVERSE_REPLICATION_DEFAULT_AGENT);
    }

    /**
     * Waits until the queue of the requested agent is empty
     *
     * @param agentPath path to the agent
     * @throws ClientException if the queue is not empty after the default timeout (2 min) expires
     * @throws InterruptedException to mark this method as waiting
     */
    public void waitAgentReplicationQueueIsEmpty(final String agentPath) throws InterruptedException, ClientException {
        // Check if the agent exists and the client has access to the queue
        try {
            getAgentReplicationQueue(agentPath);
        } catch (ClientException e) {
            throw new ClientException("Could not access the Queue for Replication Agent " + agentPath, e);
        }

        EmptyReplicationQueuePoller polling = new EmptyReplicationQueuePoller(this, agentPath);
        try {
            polling.poll(TimeUnit.MINUTES.toMillis(1), 100);
        } catch (TimeoutException e) {
            throw new ClientException("The queue for replication agent " + agentPath +
                    "was not empty after " + polling.getWaited() + "." +
                    " It still contains " + polling.queue.size() + "elements: \n" + polling.queue.toString());
        }
    }

    public void waitPublishReplicationQueueIsEmpty() throws InterruptedException, ClientException {
        waitAgentReplicationQueueIsEmpty(PUBLISH_REPLICATION_DEFAULT_AGENT);
    }

    public void waitReversePublishReplicationQueueIsEmpty() throws InterruptedException, ClientException {
        waitAgentReplicationQueueIsEmpty(PUBLISH_REVERSE_REPLICATION_DEFAULT_AGENT);
    }

    public boolean isConnectionSuccessful(String agentLocation) throws ClientException {
        SlingHttpResponse exec = doGet(agentLocation + ".test.html", SC_OK);
        return exec.getContent().contains("succeeded");
    }

    /**
     * Returns true if the specific at least one pattern was found in the log or
     * false otherwise.
     *
     * @param agentLocation agent path
     * @param patterns list of patterns
     * @param startTime start time
     * @return true if the pattern was found
     * @throws ClientException if the request fails
     */
    public boolean findInLog(String agentLocation, String[] patterns, Date startTime) throws ClientException {
        SlingHttpResponse exec = doGet(agentLocation + ".log.html", SC_OK);

        String content = exec.getContent();
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (line.length() >= 20) {
                String time = line.substring(0, 19);
                try {
                    SimpleDateFormat logDate = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    Date convertedLogDate = logDate.parse(time);
                    if (convertedLogDate.equals(startTime) || convertedLogDate.after(startTime)) {
                        // checks the error level
                        for (String pattern : patterns) {
                            // System.out.println(line);
                            if (line.contains(pattern))
                                return true;
                        }
                    }
                } catch (Exception e) {
                    // if the text cannot be converted, ignore it
                }
            }
        }
        return false;
    }

    public static class EmptyReplicationQueuePoller extends Polling {
        private final ReplicationClient client;
        private final String agentPath;
        private JsonNode queue;

        public EmptyReplicationQueuePoller(ReplicationClient client, String agentPath) {
            super();
            this.client = client;
            this.agentPath = agentPath;
        }

        @Override
        public Boolean call() throws  Exception {
            queue = client.getAgentReplicationQueue(agentPath);
            return !queue.getElements().hasNext();
        }
    }
}
