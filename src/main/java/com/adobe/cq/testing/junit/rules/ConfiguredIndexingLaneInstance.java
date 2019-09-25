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
package com.adobe.cq.testing.junit.rules;

import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.indexing.IndexingClient;
import org.apache.sling.testing.junit.rules.instance.BuilderCustomizer;
import org.apache.sling.testing.junit.rules.instance.ExistingInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfiguredIndexingLaneInstance extends ExistingInstance {
    private static final Logger LOG = LoggerFactory.getLogger(ConfiguredIndexingLaneInstance.class);

    /**
     * If index lanes should be dynamically detected
     */
    private static final String DETECT_INDEXING_LANES = "it.detectIndexingLanes";

    /**
     * Indexing lanes to use if not detected dynamically
     */
    private static final String[] DEFAULT_INDEXING_LANES = new String[]{"async", "fulltext-async"};

    @Override
    public <T extends SlingClient> T newClient(Class<T> clientClass, String user, String pass, BuilderCustomizer... customizers) {
        return injectLaneNames(super.newClient(clientClass, user, pass, customizers));
    }

    /**
     * Should index lanes should be dynamically detected
     *
     * @return true if index lanes should be detected automatically
     */
    private static boolean shouldDetectIndexingLanes() {
        return Boolean.getBoolean(DETECT_INDEXING_LANES);
    }

    private static <T extends SlingClient> T injectLaneNames(T in) {
        if (in != null && !shouldDetectIndexingLanes()) {
            try {
                IndexingClient client = in.adaptTo(IndexingClient.class);
                client.setLaneNames(DEFAULT_INDEXING_LANES);
            } catch (ClientException ce) {
                LOG.error("Error occurred while configuring indexing lanes. Returning null", ce);
                in = null;
            }
        }
        return in;
    }
}