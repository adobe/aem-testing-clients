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

import java.util.ArrayList;
import java.util.List;

/**
 * POJO that describes active jobs statistics and topic statistics in sling
 */
public class JobsStatistics {
    public List<JobQueueStat> jobQueues;
    public List<TopicStat> topics;

    public List<JobQueueStat> getJobQueues() {
        return jobQueues;
    }

    public void setJobQueues(List<JobQueueStat> jobQueues) {
        this.jobQueues = jobQueues;
    }

    public List<TopicStat> getTopics() {
        return topics;
    }

    public void setTopics(List<TopicStat> topics) {
        this.topics = topics;
    }

    public JobsStatistics() {
        this.jobQueues = new ArrayList<>();
        this.topics = new ArrayList<>();
    }

    /**
     * Add a job queue statistics entry tot his statistics container
     * @param stats statistics
     */
    public void addJobQueueStat(JobQueueStat stats) {
        this.jobQueues.add(stats);
    }

    /**
     * Add a topic statistics element to this statistics container
     * @param stats statistics
     */
    public void addTopicStat(TopicStat stats) {
        this.topics.add(stats);
    }
}
