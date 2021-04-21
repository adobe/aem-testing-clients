/*
 * Copyright 2021 Adobe
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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.codehaus.jackson.JsonNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * CQClient for performing operations related to feature toggles.
 */
public class TogglesClient extends CQClient {
    private static final String TOGGLES_PATH = "etc.clientlibs/toggles.json";

    @SuppressWarnings("unused")
    public TogglesClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public TogglesClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Retrieves a list of all the enabled toggles on the instance.
     *
     * @return the list of enabled toggles
     * @throws ClientException if the toggles state cannot be retrieved
     */
    public List<String> getEnabledToggles() throws ClientException {
        List<String> enabledToggles = new ArrayList<>();
        SlingHttpResponse response = doGet(TOGGLES_PATH, SC_OK);
        JsonUtils.getJsonNodeFromString(response.getContent())
                .path("enabled")
                .forEach((JsonNode node) -> enabledToggles.add(node.getTextValue()));

        return enabledToggles;
    }

    /**
     * Checks whether a toggle is enabled or not.
     *
     * @param toggle the toggle to check
     * @return true if the toggle is enabled
     * @throws ClientException if the toggles state cannot be retrieved
     */
    public boolean isToggleEnabled(String toggle) throws ClientException {
        return getEnabledToggles().contains(toggle);
    }
}
