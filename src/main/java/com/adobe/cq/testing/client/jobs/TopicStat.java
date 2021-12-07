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


public class TopicStat {

    String topicName;
    long lastActivated;
    long lastFinished;
    int finishedJobs;
    int failedJobs;
    int cancelledJobs;
    long avgProcessingTime;
    long avgWaitingTime;

    public TopicStat(String topicName, long lastActivated, long lastFinished, int finishedJobs, int failedJobs, int cancelledJobs,
                     long avgProcessingTime, long avgWaitingTime) {
        this.topicName = topicName;
        this.lastActivated = lastActivated;
        this.lastFinished = lastFinished;
        this.finishedJobs = finishedJobs;
        this.failedJobs = failedJobs;
        this.cancelledJobs = cancelledJobs;
        this.avgProcessingTime = avgProcessingTime;
        this.avgWaitingTime = avgWaitingTime;
    }




    /**
     * Constructor from a {@link JsonNode} object
     * Format :
     *
     * <pre>
     *     {@code
              {
                topic: "/my/topic/name",
                last_activated: 1375793524795,
                last_finished: -1,
                finished_jobs: 0,
                failed_jobs: 4,
                cancelled_jobs: 0,
                avg_processing_time: 0,
                avg_waiting_time: 1522
              }
     *     }
     * </pre>
     *
     * @param topicJson  the topic JSON node
     */
    public TopicStat(JsonNode topicJson) {
        this.topicName = topicJson.get("topic").textValue();
        this.lastActivated = topicJson.get("last_activated").longValue();
        this.lastFinished = topicJson.get("last_finished").longValue();
        this.finishedJobs = topicJson.get("finished_jobs").intValue();
        this.failedJobs = topicJson.get("failed_jobs").intValue();
        this.cancelledJobs = topicJson.get("cancelled_jobs").intValue();
        this.avgProcessingTime = topicJson.get("avg_processing_time").longValue();
        this.avgWaitingTime = topicJson.get("avg_waiting_time").longValue();
    }

    public String getTopicName() {
        return topicName;
    }

    public long getLastActivated() {
        return lastActivated;
    }

    public long getLastFinished() {
        return lastFinished;
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

    public long getAvgProcessingTime() {
        return avgProcessingTime;
    }

    public long getAvgWaitingTime() {
        return avgWaitingTime;
    }

}
