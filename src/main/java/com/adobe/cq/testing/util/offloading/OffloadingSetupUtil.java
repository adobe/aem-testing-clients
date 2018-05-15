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
package com.adobe.cq.testing.util.offloading;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.CQWorkflowClient;
import com.adobe.cq.testing.client.TopologyClient;
import com.adobe.cq.testing.client.offloading.OffloadingBrowserClient;
import org.apache.sling.testing.clients.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to help in the setup of offloading in CQ
 */
public class OffloadingSetupUtil {
    public static final Logger LOG = LoggerFactory.getLogger(OffloadingSetupUtil.class);

    /**
     * Replaces the "DAM Update Asset" Workflow with the "Dam Update Asset Offloading"
     * This is currently required as part of the offloading setup for the instance that delegates the offloading
     *
     * @param client client to use
     * @param <T> client type
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public static <T extends CQClient> void setDAMUpdateAssetOffloading(T client) throws ClientException, InterruptedException {
        CQWorkflowClient wClient = client.adaptTo(CQWorkflowClient.class);

        wClient.editWorkflowLauncher("update_asset_create", "workflow",
                "/etc/workflow/models/dam/update_asset_offloading/jcr:content/model");

        wClient.editWorkflowLauncher("update_asset_mod", "workflow",
                "/etc/workflow/models/dam/update_asset_offloading/jcr:content/model");
    }

    /**
     * Replaces the "DAM Update Asset Offloading" Workflow with the original "Dam Update Asset"
     * This is currently required as part of the offloading setup for the instance that delegates the offloading
     *
     * @param client client to use
     * @param <T> client type
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public static <T extends CQClient> void setDAMUpdateAsset(T client) throws ClientException, InterruptedException {
        CQWorkflowClient wClient = client.adaptTo(CQWorkflowClient.class);

        wClient.editWorkflowLauncher("update_asset_create", "workflow",
                "/etc/workflow/models/dam/update_asset/jcr:content/model");

        wClient.editWorkflowLauncher("update_asset_mod", "workflow",
                "/etc/workflow/models/dam/update_asset/jcr:content/model");
    }

    /**
     * Enables/ disables the DAM update asset Workflow
     *
     * @param client client to use
     * @param enable true means enable, false means disable
     * @param <T> client type
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public static <T extends CQClient> void enableDisableDAMUpdateAsset(T client, boolean enable) throws ClientException,
            InterruptedException {
        CQWorkflowClient wClient = client.adaptTo(CQWorkflowClient.class);
        wClient.enableDisableWorkflowLauncher("update_asset_create", enable);
        wClient.enableDisableWorkflowLauncher("update_asset_mod", enable);

    }

    /**
     * Enables/disables the offloading topic for the instance the client points to, through the Offloading Browser
     *
     * @param client client to use
     * @param enable true means enable, false means disable
     * @param <T> client type
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public static <T extends CQClient> void enableDisableOffloadingTopic(T client, boolean enable)
            throws ClientException, InterruptedException {
        TopologyClient tClient = client.adaptTo(TopologyClient.class);
        OffloadingBrowserClient oClient = client.adaptTo(OffloadingBrowserClient.class);

        // get the sling ID of this instance
        String slingId = tClient.getSlingId();
        oClient.enableDisableTopicWithWait(slingId, OffloadingBrowserClient.OFFLOADING_TOPIC, enable, 30);
    }

}
