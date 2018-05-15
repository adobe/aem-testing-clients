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
package com.adobe.cq.testing.client.offloading;

import java.util.HashSet;
import java.util.Set;

public class OffloadingInstanceConfiguration {

    public String slingId;
    public String ip;
    public String port;
    public String cluster;
    public Set<String> topics;


    public OffloadingInstanceConfiguration() {
        topics = new HashSet<>();
    }

    public OffloadingInstanceConfiguration(String slingId, String ip, String port, String cluster, Set<String> topics) {
        this.slingId = slingId;
        this.ip = ip;
        this.port = port;
        this.cluster = cluster;
        this.topics = topics;
    }

    public OffloadingInstanceConfiguration(String ip, String port) {
        this.ip = ip;
        this.port = port;
        this.slingId = null;
        this.cluster = null;
        this.topics = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OffloadingInstanceConfiguration)) return false;

        OffloadingInstanceConfiguration that = (OffloadingInstanceConfiguration) o;

        if (slingId != null && that.slingId != null && !this.slingId.equals(that.slingId)) return false;
        if (ip != null && that.ip != null && !this.ip.equals(that.ip)) return false;
        if (port != null && that.port != null && !this.port.equals(that.port)) return false;
        if (cluster != null && that.cluster != null && !this.cluster.equals(that.cluster)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = slingId != null ? slingId.hashCode() : 0;
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        result = 31 * result + (port != null ? port.hashCode() : 0);
        result = 31 * result + (cluster != null ? cluster.hashCode() : 0);
        return result;
    }
}
