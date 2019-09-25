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

import com.adobe.cq.testing.client.CQClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.indexing.IndexingClient;
import org.apache.sling.testing.clients.instance.InstanceConfiguration;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ConfiguredIndexLanesTest {
    private static final String[] EXPECTED_INDEX_LANE_NAMES = new String[]{"async", "fulltext-async"};

    public ConfiguredIndexLanesTest(boolean useBasicAuth, String runMode, InstanceConfiguration defaultConfig) {
        instance = ClassRuleUtils.newInstanceRule(useBasicAuth)
                .withRunMode(runMode).orDefault(defaultConfig);
    }

    @Parameterized.Parameters (name = "{index}: {1} with basicAuth {0}")
    public static Collection input() {
        return Arrays.asList(new Object[][]{
                {true, "author", CQClassRule.DEFAULT_AUTHOR_CONFIG},
                {false, "author", CQClassRule.DEFAULT_AUTHOR_CONFIG},
                {true, "publish", CQClassRule.DEFAULT_PUBLISH_CONFIG},
                {false, "publish", CQClassRule.DEFAULT_PUBLISH_CONFIG},
        });
    }

    @Rule
    public Instance instance;

    @Test
    public void basics() throws ClientException {
        SlingClient client = instance.getClient(SlingClient.class, "admin", "admin");

        IndexingClient indexingClient = client.adaptTo(IndexingClient.class);

        List<String> laneNames = indexingClient.getLaneNames();
        Assert.assertEquals("Incorrect number of configured index lanes",
                EXPECTED_INDEX_LANE_NAMES.length, laneNames.size());
        Assert.assertThat(laneNames, CoreMatchers.hasItems(EXPECTED_INDEX_LANE_NAMES));
    }

    @Test
    public void cachedClient() throws ClientException {
        // just create a client to create a cache entry.
        instance.getClient(CQClient.class, "admin", "admin");

        // .... rest of the test is same as basics test
        basics();
    }

    @Test
    public void basicsAdminClient() throws ClientException {
        SlingClient client = instance.getAdminClient(SlingClient.class);

        IndexingClient indexingClient = client.adaptTo(IndexingClient.class);

        List<String> laneNames = indexingClient.getLaneNames();
        Assert.assertEquals("Incorrect number of configured index lanes",
                EXPECTED_INDEX_LANE_NAMES.length, laneNames.size());
        Assert.assertThat(laneNames, CoreMatchers.hasItems(EXPECTED_INDEX_LANE_NAMES));
    }
}
