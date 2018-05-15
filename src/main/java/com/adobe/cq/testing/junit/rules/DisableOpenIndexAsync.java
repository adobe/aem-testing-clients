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
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * <p>Rule used to make sure that the OSGi config setting for 'Open index asynchronously'
 * is set to false in org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexProviderService.</p>
 *
 * <p>This is needed to make sure that the TestIndexUtils.waitForIndexingDone works properly, which is needed
 * in all tests where repo content is manipulated which is indexed by the async lucene property index and
 * accessed right afterwards.</p>
 */
public class DisableOpenIndexAsync implements TestRule {

    // the config ID we want to adapt
    private final static String SERVICE_PID = "org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexProviderService";

    // the property we want to change
    private final static String OPEN_INDEX_ASYNC_PROPERTY = "enableOpenIndexAsync";

    // marker if config has already been adapted
    private boolean adapted = false;

    private final Instance quickstartRule;

    // Log output
    private static final Logger LOG = LoggerFactory.getLogger(DisableOpenIndexAsync.class);

    public DisableOpenIndexAsync(Instance quickstart) {
        super();
        this.quickstartRule = quickstart;
    }

    /**
     * The method been called, returns a new Statement which wraps the original Junit execution,
     * @param statement the original execution statement
     * @param description test method description
     * @return the wrapped statement
     */
    @Override
    public Statement apply(final Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                // if not done yet
                if (!adapted) {
                    // change the config
                    adaptOSGiConfig();
                    // make sure its only done once during a run of multiple test classes
                    adapted = true;

                }
                // run the base Junit
                statement.evaluate();
            }
        };
    }

    /**
     * disable the setting
     */
    private void adaptOSGiConfig() {
        LOG.info("adapting OSGi config for " + SERVICE_PID + " -> " + OPEN_INDEX_ASYNC_PROPERTY);

        HashMap<String, Object> props = new HashMap<>();
        props.put(OPEN_INDEX_ASYNC_PROPERTY, "false");

        try {
            OsgiConsoleClient osgiClient = quickstartRule.getAdminClient(OsgiConsoleClient.class);
            osgiClient.editConfiguration(SERVICE_PID, null, props);
        } catch (ClientException e) {
            LOG.warn("No author instance found to adapt OSGi Config for " + SERVICE_PID + " -> " + OPEN_INDEX_ASYNC_PROPERTY);
        }
    }
}
