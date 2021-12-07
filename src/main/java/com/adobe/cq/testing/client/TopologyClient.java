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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_OK;

public class TopologyClient extends CQClient {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TopologyClient.class);

    public static final String DEFAULT_CONNECTOR_PATH = "/libs/sling/topology/connector";
    public static final String QE_TOPOLOGY_SERVLET_PATH = "/libs/granite/qe/topology";
    public static final String CONNECTOR_URLS_PATH = "connectorurls";
    public static final String CLUSTER_VIEW_PATH = "clusterview";

    public static final String UTF_8 = "UTF-8";
    public static final String QE_SLINGSETTINGS_SERVLET_PATH = "/libs/granite/qe/slingsettings";

    public TopologyClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public TopologyClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Join a topology by adding the connector url to the discovery configuration
     * @param baseUrl The base url of the instance about to be joined; uses the {@link TopologyClient#DEFAULT_CONNECTOR_PATH}
     *               to build the full connector URL
     * @return true if the topology was joined, false otherwise
     * @throws ClientException if the request failed
     */
    public boolean joinTopology(String baseUrl) throws ClientException {
        return joinTopology(baseUrl, DEFAULT_CONNECTOR_PATH);
    }

    /**
     * Join a topology by adding the connector url to the discovery configuration
     * @param baseUrl The full connector URL to be added to the discovery configuration
     * @param connectorPath path to the connector
     * @return true if the topology was joined, false otherwise
     * @throws ClientException if the request failed
     */
    public boolean joinTopology(String baseUrl, String connectorPath) throws ClientException {
        Set<String> newConnectorUrlsList = AddConnUrl(baseUrl, connectorPath);

        // Write the configuration with the new values
        setConnectorUrls(newConnectorUrlsList);

        return true;
    }

    private Set<String> AddConnUrl(String baseUrl, String connectorPath) throws ClientException {
        URL bUrl, connectorUrl;

        try {
            bUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("baseUrl is not a valid URL", e);
        }

        try {
            connectorUrl = new URL(bUrl, connectorPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not create connector URL from server URL and connector path", e);
        }

        String connectorUrlString = connectorUrl.toString();
        Set<String> newConnectorUrlsList = getConnectorUrls();

        // Add the new connector url if it doesn't exist
        newConnectorUrlsList.add(connectorUrlString);
        return newConnectorUrlsList;
    }

    /**
     * Join a topology by adding the connector url to the discovery configuration.
     * This method waits and retries until the topology is current
     * @param baseUrl base url
     * @param connectorPath connector path
     * @param timeout time (in milliseconds) to wait before throwing an exception
     * @return true
     * @throws ClientException if the topology could not be joined
     * @throws InterruptedException if interrupted while waiting for the server
     */
    public boolean joinTopologyWithWait(String baseUrl, String connectorPath, long timeout) throws ClientException, InterruptedException {
        Set<String> newConnectorUrlsList = AddConnUrl(baseUrl, connectorPath);
        // Write the configuration with the new values
        setConnectorUrlsWithWait(newConnectorUrlsList, timeout);

        return true;
    }

    /**
     * Join a topology by adding the connector url to the discovery configuration.
     * This method waits and retries until the topology is current
     * @param baseUrl base url
     * @param timeout time (in milliseconds) to wait before throwing an exception
     * @return true
     * @throws ClientException if the topology could not be joined
     * @throws InterruptedException if interrupted while waiting for the server
     */
    public boolean joinTopologyWithWait(String baseUrl, long timeout) throws ClientException, InterruptedException {
        return joinTopologyWithWait(baseUrl, DEFAULT_CONNECTOR_PATH, timeout);
    }

    /**
     * Leave a topology by removing a connector URL from the discovery configuration
     * @param baseUrl the base URL of the instance about to be left; uses the {@link TopologyClient#DEFAULT_CONNECTOR_PATH}
     *               to build the full connector URL
     * @return true if topology was left, false otherwise
     * @throws ClientException in case of error
     */
    public boolean leaveTopology(String baseUrl) throws ClientException {
        return leaveTopology(baseUrl, DEFAULT_CONNECTOR_PATH);
    }

    /**
     * Leave a topology by removing a connector URL from the discovery configuration
     * @param baseUrl the full connector URL to be removed
     * @param connectorPath path to the connector
     * @return true if topology was left, false otherwise
     * @throws ClientException if the request failed
     */
    public boolean leaveTopology(String baseUrl, String connectorPath) throws ClientException {
        Set<String> newConnectorUrlsList = removeConnUrl(baseUrl, connectorPath);

        // Write the configuration with the new values
        setConnectorUrls(newConnectorUrlsList);

        return true;
    }

    private Set<String> removeConnUrl(String baseUrl, String connectorPath) throws ClientException {
        URL bUrl, connectorUrl;

        try {
            bUrl = new URL(baseUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("baseUrl is not a valid URL", e);
        }

        try {
            connectorUrl = new URL(bUrl, connectorPath);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not create connector URL from server URL and connector path", e);
        }

        Set<String> newConnectorUrlsList = getConnectorUrls();
        // remove the connector url if it exists;
        newConnectorUrlsList.remove(connectorUrl.toString());
        return newConnectorUrlsList;
    }

    /**
     * Leave a topology by removing a connector URL from the discovery configuration
     * This method waits and retries until the topology is current
     *
     * @param baseUrl base url
     * @param connectorPath connector path
     * @param timeout time (in milliseconds) to wait before throwing an exception
     * @return true
     * @throws ClientException if it could not leave the topology
     * @throws InterruptedException if interrupted while waiting for the server
     */
    public boolean leaveTopologyWithWait(String baseUrl, String connectorPath, long timeout) throws ClientException, InterruptedException {
        Set<String> newConnectorUrlsList = removeConnUrl(baseUrl, connectorPath);

        // Write the configuration with the new values
        setConnectorUrlsWithWait(newConnectorUrlsList, timeout);
        return true;
    }

    /**
     * Leave a topology by removing a connector URL from the discovery configuration
     * This method waits and retries until the topology is current
     *
     * @param baseUrl base url
     * @param timeout time (in milliseconds) to wait before throwing an exception
     * @return true
     * @throws ClientException if it could not leave the topology
     * @throws InterruptedException if interrupted while waiting for the server
     */
    public boolean leaveTopologyWithWait(String baseUrl, long timeout) throws ClientException, InterruptedException {
        return leaveTopologyWithWait(baseUrl, DEFAULT_CONNECTOR_PATH, timeout);
    }


    /**
     * Get the set of connector URLs configured for the discovery service
     *
     * @return the set of URLs
     * @throws ClientException if the request failed
     */
    public Set<String> getConnectorUrls() throws ClientException {
        JsonNode json = getConnectorUrlsJson();
        return parseConnectorUrls(json);
    }

    /**
     * Get the set of connector URLs configured for the discovery service.
     * The method retries to get the list until the configuration is saved and the topology is current
     *
     * @param timeout time to wait (in milliseconds) before giving up
     * @return set of connector URLs
     * @throws ClientException if the topology could not be joined
     * @throws InterruptedException if interrupted while waiting for the server
     */
    public Set<String> getConnectorUrlsWithWait(long timeout) throws ClientException, InterruptedException {
        class TopologyPoller extends Polling {
            public JsonNode json = null;
            public Set<String> connectorUrls = null;

            @Override
            public Boolean call() throws Exception {
                json = getConnectorUrlsJson();
                if (null != json && "ok".equals(json.get("status").textValue())
                        && json.get("is_current").booleanValue()) {
                    connectorUrls = parseConnectorUrls(json);
                    return true;
                }
                return false;
            }
        }

        TopologyPoller poller = new TopologyPoller();
        try {
            poller.poll(timeout, 1000);
        } catch (TimeoutException e) {
            throw new ClientException("Failed to retrieve connector urls in " + poller.getWaited() + " ms", e);
        }

        return poller.connectorUrls;
    }

    private JsonNode getConnectorUrlsJson(int... statusCode) throws ClientException {
        // Get the current connectors urls list
        SlingHttpResponse response = doGet(QE_TOPOLOGY_SERVLET_PATH + "." + CONNECTOR_URLS_PATH + ".json",
                HttpUtils.getExpectedStatus(SC_OK, statusCode));
        return JsonUtils.getJsonNodeFromString(response.getContent());
    }

    private Set<String> parseConnectorUrls(JsonNode json) throws ClientException {
        Set<String> connectorUrls = new HashSet<>(5);
        Iterator<JsonNode> connectorListIterator = json.get("data").elements();
        while (connectorListIterator.hasNext()){
            String connUrl;
            try {
                connUrl = URLDecoder.decode(connectorListIterator.next().textValue(), UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new ClientException("Could not decode URL", e);
            }
            // skip empty values
            if (null == connUrl || connUrl.isEmpty())
                continue;
            connectorUrls.add(connUrl);
        }

        return connectorUrls;
    }


    /**
     * Write the discovery service configuration with a set of connector URLs
     * @param connectorUrlsList the set of connector URLs to be written
     * @throws ClientException if the request failed
     */
    public void setConnectorUrls(Set<String> connectorUrlsList) throws ClientException {
        String connectorUrls = StringUtils.join(connectorUrlsList.toArray(), ",");
        FormEntityBuilder feb = FormEntityBuilder.create().addParameter("connectorUrls", connectorUrls);
        doPost(QE_TOPOLOGY_SERVLET_PATH + "." + CONNECTOR_URLS_PATH + ".json", feb.build(), SC_OK);
    }

    /**
     * Write the discovery service configuration with a set of connector URLs
     * The method retries until the configuration was saved and the topology is current
     *
     * @param connectorUrlsList list of connector urls
     * @param timeout time (in milliseconds) to wait before throwing an exception
     * @throws ClientException if the request failed
     * @throws InterruptedException to mark this method as "waiting"
     */
    public void setConnectorUrlsWithWait(Set<String> connectorUrlsList, long timeout)
            throws ClientException, InterruptedException {
        setConnectorUrls(connectorUrlsList);
        getConnectorUrlsWithWait(timeout);
    }

    /**
     *
     * @return The Sling ID of this instance
     * @throws ClientException if call to instance fails
     */
    public String getSlingId() throws ClientException {
        // Get the current connectors urls list
        SlingHttpResponse response = doGet(QE_SLINGSETTINGS_SERVLET_PATH + ".sling_id.json", SC_OK);
        JsonNode json = JsonUtils.getJsonNodeFromString(response.getContent());
        // this is a String and not a UUID because the sling method in SlingSettingsService returns String

        return json.get("data").get("sling_id").textValue();
    }

}
