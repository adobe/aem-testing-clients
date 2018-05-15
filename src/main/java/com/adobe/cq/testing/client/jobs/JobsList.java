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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class JobsList extends LinkedList<JobDescriptor> {
    List<JobDescriptor> active;
    List<JobDescriptor> queued;

    public JobsList() {
        super();
        this.active = new LinkedList<>();
        this.queued = new LinkedList<>();
    }

    public JobsList(List<JobDescriptor> active, List<JobDescriptor> queued) {
        super(active);
        this.addAll(queued);
        this.active = active;
        this.queued = queued;
    }

    public JobsList(Collection<? extends JobDescriptor> c) {
        super(c);
        for (JobDescriptor j : c) {
            if (j.isStarted()) {
                active.add(j);
            } else {
                queued.add(j);
            }
        }
    }

    @Override
    public boolean add(JobDescriptor jobDescriptor) {
        boolean added = super.add(jobDescriptor);
        if (jobDescriptor.isStarted()) {
            active.add(jobDescriptor);
        } else {
            queued.add(jobDescriptor);
        }

        return added;
    }

    public List<JobDescriptor> getActive() {
        return active;
    }

    public void setActive(List<JobDescriptor> active) {
        this.active = active;
    }

    public List<JobDescriptor> getQueued() {
        return queued;
    }

    public void setQueued(List<JobDescriptor> queued) {
        this.queued = queued;
    }
}
