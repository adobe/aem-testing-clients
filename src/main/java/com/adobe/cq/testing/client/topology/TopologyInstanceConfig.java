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
package com.adobe.cq.testing.client.topology;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.TopologyClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.config.InstanceConfig;
import org.apache.sling.testing.clients.util.config.InstanceConfigException;

import java.util.Set;

//import org.apache.sling.testing.clients.util.config.InstanceConfig;

public class TopologyInstanceConfig implements InstanceConfig {
    TopologyClient client;
    Set<String> topology;

    public <T extends SlingClient> TopologyInstanceConfig(T client) throws InstanceConfigException {
        try {
            this.client = client.adaptTo(TopologyClient.class);
        } catch (ClientException e) {
            throw new InstanceConfigException("Couldn't set client", e);
        }
        save();
    }

    public <T extends CQClient> TopologyClient setClient(T client) throws InstanceConfigException {
        try {
            this.client = client.adaptTo(TopologyClient.class);
        } catch (ClientException e) {
            throw new InstanceConfigException("Couldn't set client", e);
        }
        return this.client;
    }

    public TopologyClient getClient() {
        return this.client;
    }

    @Override
    public TopologyInstanceConfig save() throws InstanceConfigException {
        try {
            try {
                this.topology = client.getConnectorUrlsWithWait(300);
            } catch (InterruptedException e) {
                throw new InstanceConfigException("Fetching the topology connector URLs interrupted", e);
            }
        } catch (ClientException e) {
            throw new InstanceConfigException("Fetching the topology connector URLs failed", e);
        }
        return this;
    }

    @Override
    public TopologyInstanceConfig restore() throws InstanceConfigException {
        try {
            client.setConnectorUrlsWithWait(this.topology, 300);
        } catch (ClientException e) {
            throw new InstanceConfigException("Setting the topology connector URLs failed", e);
        } catch (InterruptedException e) {
            throw new InstanceConfigException("Setting the topology connector URLs was interrupted", e);
        }

        return this;
    }
}
