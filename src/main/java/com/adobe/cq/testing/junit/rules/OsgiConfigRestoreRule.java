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
import org.apache.sling.testing.clients.osgi.OsgiInstanceConfig;
import org.apache.sling.testing.clients.util.config.InstanceConfigException;
import org.apache.sling.testing.clients.util.config.impl.InstanceConfigCacheImpl;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Make a snapshot of the OSGi configuration of a given bundle and restore it to its original state after test execution.
 */
public class OsgiConfigRestoreRule extends ExternalResource {

    private final Logger logger = LoggerFactory.getLogger(OsgiConfigRestoreRule.class);
    private final String pid;
    private final Instance quickstartRule;
    private SlingClient client;
    private InstanceConfigCacheImpl configs;

    public OsgiConfigRestoreRule(Instance quickstartRule, String pid) {
        super();
        this.pid = pid;
        this.quickstartRule = quickstartRule;
    }

    @Override
    protected void before() throws ClientException, InstanceConfigException, InterruptedException {
        this.client = this.quickstartRule.getAdminClient(SlingClient.class);
        this.configs = new InstanceConfigCacheImpl();
        this.configs.add(new OsgiInstanceConfig(this.client, this.pid));
    }

    @Override
    protected void after() {
        try {
            this.configs.restore();
        } catch (InstanceConfigException | InterruptedException e) {
            logger.error("Could not restore OSGi config.", e);
        }
    }

}
