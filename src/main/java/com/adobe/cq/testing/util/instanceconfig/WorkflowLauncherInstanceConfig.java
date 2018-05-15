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
import com.adobe.cq.testing.client.CQWorkflowClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.config.InstanceConfig;
import org.apache.sling.testing.clients.util.config.InstanceConfigException;

import java.util.HashMap;
import java.util.Map;

/**
 * A configuration for a single CQ workflow launcher
 */
public class WorkflowLauncherInstanceConfig implements InstanceConfig {

    private final String launcherId;
    private Map<String, String> launcher;
    private CQWorkflowClient wClient;

    public <T extends CQClient> WorkflowLauncherInstanceConfig(T client, String launcherId) throws ClientException {
       this.launcherId = launcherId;
       this.launcher = new HashMap<>();
       this.wClient = client.adaptTo(CQWorkflowClient.class);
    }

    @Override
    public InstanceConfig save() throws InstanceConfigException {
        try {
            launcher = wClient.getWorkflowLauncher(launcherId);
        } catch (ClientException e) {
            throw new InstanceConfigException("Could not retrieve workflow launcher info", e);
        }

        return this;
    }

    @Override
    public InstanceConfig restore() throws InstanceConfigException {
        try {
            wClient.editWorkflowLauncher(launcherId, launcher);
        } catch (ClientException e) {
            throw new InstanceConfigException("Could not write workflow launcher info", e);
        }
        return this;
    }
}
