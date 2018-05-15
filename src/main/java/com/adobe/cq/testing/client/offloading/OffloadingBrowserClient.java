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
package com.adobe.cq.testing.client.offloading;

import com.adobe.cq.testing.client.CQClient;
import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class OffloadingBrowserClient extends CQClient {
    public static final Logger LOG = LoggerFactory.getLogger(OffloadingBrowserClient.class);

    public static final String OFFLOADING_BROWSER_JSON_LIST_URL = "/libs/granite/offloading/content/view/views/wrapper/items/servletComponentPlaceholder.list.json";
    public static final String OFFLOADING_CONFIG_URL = "/libs/granite/offloading/content/view/views/wrapper/items/servletComponentPlaceholder.config.html";
    public static final String SLING_ID_KEY = "slingID";
    public static final String IP_KEY = "ip";
    public static final String PORT_KEY = "port";
    public static final String CLUSTER_KEY = "cluster";
    public static final String OFFLOADING_TOPIC = "com/adobe/granite/workflow/offloading";

    public OffloadingBrowserClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public OffloadingBrowserClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Returns a set of the instance descriptors of all the existing instances
     * @return all the existing instances
     * @throws ClientException if the call to the backend fails
     */
    public Set<OffloadingInstanceConfiguration> getAllInstances() throws ClientException {
        Set<OffloadingInstanceConfiguration> instances = new HashSet<>();

        SlingHttpResponse re = this.doGet(OFFLOADING_BROWSER_JSON_LIST_URL, 200);

        JsonNode json = JsonUtils.getJsonNodeFromString(re.getContent());

        JsonNode topics = json.get("topics");
        for (JsonNode topicNode: topics) {
            JsonNode instancesNode = topicNode.get("instances");
            Set<OffloadingInstanceConfiguration> topicInstances = instancesFromJSONArray(instancesNode,
                    topicNode.get("topic").getTextValue().trim());

            // Matching is done by using the slingId, cluster, ip and port
            instances.addAll(topicInstances);

            // Need to add the topics to the instances; TODO: apply a map/reduce algorithm
            for (OffloadingInstanceConfiguration instanceConfiguration : instances) {
                for (OffloadingInstanceConfiguration topicInstanceConfiguration : topicInstances) {
                    if (topicInstanceConfiguration.equals(instanceConfiguration)) {
                        instanceConfiguration.topics.addAll(topicInstanceConfiguration.topics);
                    }
                }
            }

        }

        return instances;
    }

    /**
     * Returns the instance descriptor for the given Sling ID, or null if it doesn't exist
     * @param slingId id of the instance
     * @return the instance descriptor
     * @throws ClientException if the call to the backend fails
     */
    public OffloadingInstanceConfiguration getInstance(String slingId) throws ClientException {
        Set<OffloadingInstanceConfiguration> instances = getAllInstances();
        for (OffloadingInstanceConfiguration instance : instances) {
            if (instance.slingId.equals(slingId)) {
                return instance;
            }
        }
        // if not found, return null
        return null;
    }


    /**
     * Enables/ disables a topic for the given slingId.
     * This method is asynchronous, it does not wait for the configuration change to be applied.
     *
     * @param slingId id of the instance
     * @param topic topic to enable/disable
     * @param enable enable or disable
     * @throws ClientException if the HTTP call to the backend fails
     */
    public void enableDisableTopic(String slingId, String topic, boolean enable) throws ClientException {
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("slingID", slingId);
        form.addParameter("config:" + topic, enable ? "enable" : "disable");

        LOG.debug("URL:{}, slingId:{}, topic:{}, enable: {}", OFFLOADING_CONFIG_URL, slingId, topic, enable);
        this.doPost(OFFLOADING_CONFIG_URL, form.build(), HttpStatus.SC_OK);
    }

    /**
     * Enables/ disables a topic for the given slingId.
     * This method checks every second for until the topic was enabled/ disabled in the OffloadingBrowser,
     * for a maximum of waitCount seconds
     * If the change was not visible after waitCount, throws ClientException
     * @param slingId id of the instance
     * @param topic topic to enable/disable
     * @param enable enable or disable
     * @param waitCount number of seconds to wait and check the topic configuration
     * @throws ClientException if the action did not register with the OffloadingBrowser
     * @throws InterruptedException if the method was interrupted
     */
    public void enableDisableTopicWithWait(String slingId, String topic, boolean enable, int waitCount) throws ClientException,
            InterruptedException {

        // save the topic configuration
        enableDisableTopic(slingId, topic, enable);

        OffloadingInstanceConfiguration instance;

        // get the configuration and check if it was applied
        for (int i = 0; i < waitCount; i++) {
            Thread.sleep(1000);
            instance = getInstance(slingId);
            // if the enabled topic shows as enabled, return
            if (enable && instance.topics.contains(topic))
                return;
            // if the disabled topic shows as disabled, return
            if (!enable && !instance.topics.contains(topic))
                return;
        }

        // if the action was not registered, throw a client exception
        throw new ClientException(enable ? "Enabling" : "Disabling" + " the topic did not register in the Offloading Browser!");
    }

    private Set<OffloadingInstanceConfiguration> instancesFromJSONArray(JsonNode instancesNode, String topicName) {
        Set<OffloadingInstanceConfiguration> instances = new HashSet<>();
        for (JsonNode instanceNode: instancesNode) {
            OffloadingInstanceConfiguration instance = new OffloadingInstanceConfiguration();
            instance.slingId = instanceNode.get(SLING_ID_KEY).getTextValue();
            instance.ip = instanceNode.get(IP_KEY).getTextValue();
            instance.port = instanceNode.get(PORT_KEY).getTextValue();
            instance.cluster = instanceNode.get(CLUSTER_KEY).getTextValue();

            if (instanceNode.get("topicEnabled") != null && instanceNode.get("topicEnabled").getBooleanValue()) {
                instance.topics.add(topicName);
            }
            instances.add(instance);
        }
        return instances;
    }
}
