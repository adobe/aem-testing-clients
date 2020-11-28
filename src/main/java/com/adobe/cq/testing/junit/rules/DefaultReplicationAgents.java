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

import com.adobe.cq.testing.client.ReplicationClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Adapts the default replication agents on the author to target the provided publish.</p>
 *
 * <p>WARNING: make sure the input Instance rules (authorRule and publishRule) are applied
 * before this one, otherwise it will fail to retrieve the urls of the two instances.
 * The easiest way is to use a {@link org.junit.rules.RuleChain}, where this rule is the last one</p>
 *
 * <p>Usually this is applied at class level to avoid stressing the target instance for each test</p>
 */
public class DefaultReplicationAgents implements TestRule {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultReplicationAgents.class);

    /** Provider of the author client on which the replication agent will be adapted */
    private final Instance authorRule;

    /** Provider of the publish client and url which the replication agent will target */
    private final Instance publishRule;

    public DefaultReplicationAgents(Instance authorRule, Instance publishRule) {
        super();
        this.authorRule = authorRule;
        this.publishRule = publishRule;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (configure()) {
                    configureReplicationAgents();
                }
                try {
                    base.evaluate();
                } finally {
                    cleanup();
                }
            }
        };
    }

    /**
     * <p>Whether to configure the agents or not.</p>
     *
     * <p>Used to ensure that the agents are configured only if a certain condition is met.
     * This default implementation always returns true, but subclasses can override it.</p>
     *
     * @return true
     */
    protected  boolean configure() {
        return true;
    }

    /**
     * Configure the replication agents to {@link #getReplicationAgent()} and {@link #getReverseReplicationAgent()}
     *
     * @throws ClientException if an error happened during configuration
     * @throws InterruptedException to mark this operation as waiting; the exception should be thrown up the stack
     */
    protected void configureReplicationAgents() throws ClientException, InterruptedException {
        adaptDefaultReplicationAgents(authorRule.getAdminClient(ReplicationClient.class),
                publishRule.getConfiguration().getUrl().toString());
    }

    /**
     * Performs the cleanup after evaluating the statement.
     * This default implementation doesn't do anything.
     */
    protected void cleanup() {
    }

    private void adaptDefaultReplicationAgents(ReplicationClient rClient, String serverUrl)
            throws ClientException, InterruptedException {
        // normalize the URL to remove trailing slash, if present.
        if (serverUrl.lastIndexOf("/") == serverUrl.length() - 1) {
            serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
        }
        rClient.adaptReplicationAgent(getReplicationAgent(),
                new BasicNameValuePair("enabled", "true"),
                new BasicNameValuePair("transportUri", serverUrl + "/bin/receive?sling:authRequestLogin=1"),
                new BasicNameValuePair("userId", rClient.getUser()),
                new BasicNameValuePair("transportUser", rClient.getUser()),
                new BasicNameValuePair("transportPassword", rClient.getPassword()));

        rClient.adaptReplicationAgent(getReverseReplicationAgent(),
                new BasicNameValuePair("enabled", "true"),
                new BasicNameValuePair("transportUri", serverUrl + "/bin/receive?sling:authRequestLogin=1"),
                new BasicNameValuePair("userId", rClient.getUser()),
                new BasicNameValuePair("transportUser", rClient.getUser()),
                new BasicNameValuePair("transportPassword", rClient.getPassword()));

        LOG.info("Replication agents on author ({}) adapted to publish ({})", rClient.getUrl(), serverUrl);
    }

    /**
     * Returns the path to the replication agent on the publishRule instance.
     * This default implementation returns the value of {@code ReplicationClient.PUBLISH_REPLICATION_DEFAULT_AGENT}
     * @return the path to the replication agent
     */
    protected String getReplicationAgent() {
        return ReplicationClient.PUBLISH_REPLICATION_DEFAULT_AGENT;
    }

    /**
     * Returns the path to the reverse replication agent on the publishRule instance.
     * This default implementation returns the value of {@code ReplicationClient.PUBLISH_REVERSE_REPLICATION_DEFAULT_AGENT}
     * @return the path to the reverse replication agent
     */
    protected String getReverseReplicationAgent() {
        return ReplicationClient.PUBLISH_REVERSE_REPLICATION_DEFAULT_AGENT;
    }
}
