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
package com.adobe.cq.testing.util.instanceconfig;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.TopologyClient;
import com.adobe.cq.testing.client.offloading.OffloadingBrowserClient;
import com.adobe.cq.testing.client.offloading.OffloadingInstanceConfiguration;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.config.InstanceConfig;
import org.apache.sling.testing.clients.util.config.InstanceConfigException;

public class TopicInstanceConfig implements InstanceConfig {

    private final OffloadingBrowserClient obClient;
    private final String slingId;
    private final String topic;
    private boolean enabled;

    public <T extends CQClient> TopicInstanceConfig(T client, String topic) throws ClientException, InstanceConfigException {
        this.topic = topic;
        this.obClient = client.adaptTo(OffloadingBrowserClient.class);

        // Save the SlingId
        TopologyClient tClient = obClient.adaptTo(TopologyClient.class);
        this.slingId = tClient.getSlingId();

        // Save the configuration
        save();
    }

    @Override
    public InstanceConfig save() throws InstanceConfigException {
        String slingId;
        try {
            TopologyClient tClient = obClient.adaptTo(TopologyClient.class);
            slingId = tClient.getSlingId();
        } catch (ClientException e) {
            throw new InstanceConfigException("Could not get sling id", e);
        }

        try {
            OffloadingInstanceConfiguration instance = obClient.getInstance(slingId);
            this.enabled = instance.topics.contains(topic);
        } catch (ClientException e) {
            throw new InstanceConfigException("Could not save configuration", e);
        }

        return this;
    }

    @Override
    public InstanceConfig restore() throws InstanceConfigException {
        try {
            obClient.enableDisableTopicWithWait(slingId, topic, enabled, 30);
        } catch (ClientException e) {
            throw new InstanceConfigException("Could not restore configuration", e);
        } catch (InterruptedException e) {
            throw new InstanceConfigException("Restoring configuration was interrupted", e);
        }
        return this;
    }
}
