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
package com.adobe.cq.testing.client;

import com.adobe.cq.testing.client.jobs.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import static org.apache.http.HttpStatus.SC_OK;


public class JobsClient extends CQClient {
    public static final String QE_QUEUES_SERVLET_PATH = "/libs/granite/qe/jobqueues";
    public static final String QE_JOBS_SERVLET_PATH = "/libs/granite/qe/jobs";
    public static final String QE_JOBS_ALL_SELECTOR = "all";
    public static final String QE_JOBS_JSON_EXTENSION = "json";
    public static final String QE_QUEUES_ALL_PATH = QE_QUEUES_SERVLET_PATH + "." + QE_JOBS_ALL_SELECTOR + "." + QE_JOBS_JSON_EXTENSION;
    public static final String QE_JOBS_ALL_PATH = QE_JOBS_SERVLET_PATH + "." + QE_JOBS_ALL_SELECTOR + "." + QE_JOBS_JSON_EXTENSION;

    public JobsClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public JobsClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Provides a {@link JobsStatistics} object to describe all the current jobs statistics
     * @return a jobs statistics descriptor
     * @throws ClientException if the HTTP call fails
     */
    public JobsStatistics getJobsStatistics() throws ClientException {
        SlingHttpResponse exec = doGet(QE_QUEUES_ALL_PATH);
        HttpUtils.verifyHttpStatus(exec, HttpUtils.getExpectedStatus(SC_OK));

        // get the node for all the jobs statistics
        JsonNode statsJson = JsonUtils.getJsonNodeFromString(exec.getContent());
        JsonNode data = statsJson.get("data");

        JobsStatistics jobsStatistics = new JobsStatistics();

        // Populate the active queues statistics
        Iterator<String> activeQueuesIter = data.get("active_queues").fieldNames();
        while (activeQueuesIter.hasNext()) {
            String queueName = activeQueuesIter.next();
            JsonNode queueJson = data.get("active_queues").get(queueName);
            jobsStatistics.addJobQueueStat(new JobQueueStat(queueName, queueJson));
        }

        // Populate the topics statistics
        Iterator<JsonNode> topicsIter = data.get("topic_statistics").elements();
        while (topicsIter.hasNext()) {
            jobsStatistics.addTopicStat(new TopicStat(topicsIter.next()));
        }

        return jobsStatistics;
    }

    /**
     * Get a list of active and queued jobs for a specific topic
     * @param topic the topic to filter jobs
     * @return a list of jobs
     * @throws ClientException if the call to the quickstart fails
     */
    public JobsList getJobs(String topic) throws ClientException {
        SlingHttpResponse exec = doGet(QE_JOBS_SERVLET_PATH + "." + QE_JOBS_ALL_SELECTOR + "." + QE_JOBS_JSON_EXTENSION,
                Collections.<NameValuePair>singletonList(new BasicNameValuePair("topic", topic)),
                SC_OK);

        // get the node for all the jobs statistics
        JsonNode statsJson = JsonUtils.getJsonNodeFromString(exec.getContent());
        JsonNode data = statsJson.get("data");

        ArrayList<JobDescriptor> active = extractJobDescriptors(data.get("active"));
        ArrayList<JobDescriptor> queued = extractJobDescriptors(data.get("queued"));

        return new JobsList(active, queued);
        }

    private ArrayList<JobDescriptor> extractJobDescriptors(JsonNode root) {
        ArrayList<JobDescriptor> jobDescriptors = new ArrayList<>();
        Iterator<String> elems = root.fieldNames();
        while (elems.hasNext()) {
            String jobId = elems.next();
            JsonNode jobNode = root.get(jobId);
            JobDescriptor jd = new JobDescriptor();
            jd.setId(jobId);
            jd.setName(jobNode.get("name").textValue());
            jd.setTopic(jobNode.get("topic").textValue());
            jd.setQueueName(jobNode.get("queue_name").textValue());
            jd.setCreateTime(jobNode.get("created").longValue());
            jd.setStartTime(jobNode.get("processing_started").longValue());
            jd.setMaxRetries(jobNode.get("max_retries").intValue());
            jd.setRetryCount(jobNode.get("retry_count").intValue());
            jd.setCreatedBy(jobNode.get("created_by").textValue());
            jd.setTargetInstanceId(jobNode.get("target_instance").textValue());
            jobDescriptors.add(jd);
        }

        return jobDescriptors;
    }


}
