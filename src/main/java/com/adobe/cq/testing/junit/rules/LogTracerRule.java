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

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.osgi.OsgiConsoleClient;
import org.apache.sling.testing.clients.osgi.OsgiInstanceConfig;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.config.InstanceConfigException;
import org.apache.sling.testing.clients.util.config.impl.InstanceConfigCacheImpl;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;

public class LogTracerRule extends ExternalResource {

    private final Logger logger = LoggerFactory.getLogger(OsgiConfigRestoreRule.class);
    private final String pid = "org.apache.sling.tracer.internal.LogTracer";
    private final Instance quickstartRule;
    private OsgiConsoleClient client;
    private InstanceConfigCacheImpl configs;

    private int recordingCacheSizeInMB = 50;
    private int recordingCacheDurationInSecs = 900;

    // Request headers required to configure log tracer for each request
    private List<Header> tracerHeaders = Arrays.<Header>asList(
            new BasicHeader("Sling-Tracer-Record", "true"),
            new BasicHeader("Sling-Tracer-Config", "com.day;level=error,com.adobe;level=error,org.apache;level=error")
    );

    public LogTracerRule(Instance quickstartRule) {
        super();
        this.quickstartRule = quickstartRule;
    }

    public LogTracerRule(Instance quickstartRule, int recordingCacheSizeInMB, int recordingCacheDurationInSecs) {
        super();
        this.quickstartRule = quickstartRule;
        this.recordingCacheSizeInMB = recordingCacheSizeInMB;
        this.recordingCacheDurationInSecs = recordingCacheDurationInSecs;
    }

    public List<Header> getTracerHeaders() {
        return this.tracerHeaders;
    }

    public void setTracerHeaders(List<Header> headers) {
        this.tracerHeaders = headers;
    }

    /**
     * Store the state of the log tracer configuration and enable it.
     */
    @Override
    protected void before() throws ClientException, InstanceConfigException, InterruptedException {
        // Store existing config
        this.client = this.quickstartRule.getAdminClient(OsgiConsoleClient.class);
        this.configs = new InstanceConfigCacheImpl();
        this.configs.add(new OsgiInstanceConfig(this.client, this.pid));

        // Update config
        Map<String, Object> config = new HashMap<>();
        config.put("enabled", "true");
        config.put("servletEnabled", "true");
        config.put("recordingCacheSizeInMB", String.valueOf(this.recordingCacheSizeInMB));
        config.put("recordingCacheDurationInSecs", String.valueOf(this.recordingCacheDurationInSecs));
        try {
            this.client.waitEditConfiguration(30000, this.pid, null, config, SC_MOVED_TEMPORARILY);
            config = this.client.waitGetConfiguration(30000, this.pid, SC_OK);
        } catch (TimeoutException e) {
            throw new ClientException("Failed editing configuration for " + this.pid, e);
        }

        Assert.assertEquals(config.get("enabled"), "true");
        Assert.assertEquals(config.get("servletEnabled"), "true");
    }

    /**
     * Restore the original configuration state of the log tracer.
     */
    @Override
    protected void after() {
        try {
            this.configs.restore();
        } catch (InstanceConfigException | InterruptedException e) {
            logger.error("Could not restore OSGi config.", e);
        }
    }

    /**
     * Parse the tracer request id from the given response object and return the corresponding tracer report from the tracer servlet as
     * JSON object.
     *
     * @param response SlingHttpResponse object
     * @return Request report as JSONObject
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode getTracerReport(SlingHttpResponse response) throws ClientException, InterruptedException {

        // Parse tracer request id
        Header requestIdHeader = response.getFirstHeader("Sling-Tracer-Request-Id");

        // TODO: Don't fail for flaky behaviour
        // Assert.assertNotNull(requestIdHeader);
        if(requestIdHeader == null) {
            logger.warn("Request did not include Sling-Tracer-Request-Id header.");
            return new ObjectMapper().createObjectNode();
        }
        String requestId = requestIdHeader.getValue();

        // Parse log data from tracer servlet using the request id
        LogReportPoller poller = new LogReportPoller(requestId, this.client);
        try {
            poller.poll(2000, 100);
        } catch (TimeoutException e) {
            throw new ClientException("Failed to retrieve logs", e);
        }

        return poller.logJson;

    }

    /**
     * Parse the tracer request id from the given response object and return the corresponding logs from the tracer servlet as
     * JSON array.
     *
     * @param response SlingHttpResponse object
     * @return Request logs as JSONArray
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode getLogs(SlingHttpResponse response) throws ClientException, InterruptedException {
        JsonNode report = this.getTracerReport(response);

        if (report.get("logs") == null) {
            return new ObjectMapper().createObjectNode();
        }

        return report.get("logs");
    }

    /**
     * Parse the tracer request id from the given response object and check if there are any entries in the corresponding log.
     * Marks the test case from which this method was called as failed if there are any log entries.
     *
     * @param response SlingHttpResponse object
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public void verifyLog(SlingHttpResponse response) throws ClientException, InterruptedException {
        JsonNode report = this.getTracerReport(response);

        // TODO: Don't fail for flaky behaviour
        if (report.get("logs") == null) {
            return;
        }

        JsonNode logs = report.get("logs");

        if (logs.isArray() && logs.size() > 0) {
            Assert.fail("Found error in log: " + logs.get(0));
        }
    }

    private class LogReportPoller extends Polling {

        public String requestId;
        public SlingClient client;
        public JsonNode logJson;

        public LogReportPoller(String requestId, SlingClient client) {
            super();
            this.requestId = requestId;
            this.client = client;
        }

        @Override public Boolean call() throws ClientException {
            SlingHttpResponse logResponse = this.client.doGet("/system/console/tracer/" + requestId + ".json", SC_OK);
            this.logJson = JsonUtils.getJsonNodeFromString(logResponse.getContent());
            return this.logJson.get("logs") != null;
        }
    }
}
