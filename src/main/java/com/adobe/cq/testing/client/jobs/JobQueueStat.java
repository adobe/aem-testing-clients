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
package com.adobe.cq.testing.client.jobs;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JobQueueStat {
    String queueName;
    long startTime;
    long lastActivated;
    long lastFinished;
    int queuedJobs;
    int activeJobs;
    int finishedJobs;

    int failedJobs;

    int cancelledJobs;
    int avgProcessingTime;
    int avgWaitingTime;
    String type;

    List<String> topics;
    int maxParallel;
    int maxRetries;
    int retryDelay;
    String priority;
    String statusInfo;

    public String getQueueName() {
        return queueName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getLastActivated() {
        return lastActivated;
    }

    public long getLastFinished() {
        return lastFinished;
    }

    public int getQueuedJobs() {
        return queuedJobs;
    }

    public int getActiveJobs() {
        return activeJobs;
    }

    public int getFinishedJobs() {
        return finishedJobs;
    }

    public int getFailedJobs() {
        return failedJobs;
    }

    public int getCancelledJobs() {
        return cancelledJobs;
    }

    public int getAvgProcessingTime() {
        return avgProcessingTime;
    }

    public int getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public String getType() {
        return type;
    }

    public List<String> getTopics() {
        return topics;
    }

    public int getMaxParallel() {
        return maxParallel;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public int getRetryDelay() {
        return retryDelay;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public JobQueueStat(String queueName, long startTime, long lastActivated, long lastFinished, int queuedJobs, int activeJobs,
                        int finishedJobs, int failedJobs, int cancelledJobs, int avgProcessingTime, int avgWaitingTime, String type,
                        List<String> topics, int maxParallel, int maxRetries, int retryDelay, String priority, String statusInfo) {
        this.queueName = queueName;
        this.startTime = startTime;
        this.lastActivated = lastActivated;
        this.lastFinished = lastFinished;
        this.queuedJobs = queuedJobs;
        this.activeJobs = activeJobs;
        this.finishedJobs = finishedJobs;
        this.failedJobs = failedJobs;
        this.cancelledJobs = cancelledJobs;
        this.avgProcessingTime = avgProcessingTime;
        this.avgWaitingTime = avgWaitingTime;
        this.type = type;
        this.topics = topics;
        this.maxParallel = maxParallel;
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
        this.priority = priority;
        this.statusInfo = statusInfo;
    }


    /**
     * Constructor from a {@link JsonNode} object
     * Format :
     *
     * <pre>
     *     {@code
     *       {
                statistics: {
                    start_time: 1375793518694,
                    last_activated: 1375793524795,
                    last_finished: -1,
                    queued_jobs: 1,
                    active_jobs: 0,
                    finished_jobs: 0,
                    failed_jobs: 4,
                    cancelled_jobs: 0,
                    avg_processing_time: 0,
                    avg_waiting_time: 1522
                },
                configuration: {
                    type: "TOPIC_ROUND_ROBIN",
                            topics: [ "/my/topic/one/*", ""/my/topic/two/*" ],
                    max_parallel: 8,
                            max_retries: 10,
                            retry_delay: 2000,
                            priority: "MIN"
                },
                status_info: "isWaiting=false, suspendedSince=-1, isWaitingForNext=true, asyncJobs=0, jobCount=0, eventCount=0"
            }
     *     }
     * </pre>
     *
     * @param queueName the name of the job queue
     * @param jobJson the json node to parse
     */
    public JobQueueStat(String queueName, JsonNode jobJson) {
        this.queueName = queueName;
        JsonNode statistics = jobJson.get("statistics");
        JsonNode configuration = jobJson.get("configuration");

        this.startTime = statistics.get("start_time").longValue();
        this.lastActivated = statistics.get("last_activated").longValue();
        this.lastFinished = statistics.get("last_finished").longValue();
        this.queuedJobs = statistics.get("queued_jobs").intValue();
        this.activeJobs = statistics.get("active_jobs").intValue();
        this.finishedJobs = statistics.get("finished_jobs").intValue();
        this.failedJobs = statistics.get("failed_jobs").intValue();
        this.cancelledJobs = statistics.get("cancelled_jobs").intValue();
        this.avgProcessingTime = statistics.get("avg_processing_time").intValue();
        this.avgWaitingTime = statistics.get("avg_waiting_time").intValue();

        this.type = configuration.get("type").textValue();

        this.topics = new ArrayList<>();
        Iterator<JsonNode> topicsIterator = configuration.get("topics").elements();
        while (topicsIterator.hasNext()) {
            this.topics.add(topicsIterator.next().textValue());
        }

        this.maxParallel = configuration.get("max_parallel").intValue();
        this.maxRetries = configuration.get("max_retries").intValue();
        this.retryDelay = configuration.get("retry_delay").intValue();
        this.priority = configuration.get("priority").textValue();
        this.statusInfo = jobJson.get("status_info").textValue();
    }
}
