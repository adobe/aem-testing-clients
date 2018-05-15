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

package com.adobe.cq.testing.client.tartan;

import com.adobe.cq.testing.client.CQClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;
import org.codehaus.jackson.JsonNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URI;

public class TartanClient extends CQClient {

    protected String homePath = "/content/mac/geometrixx";

    public TartanClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public TartanClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    public Document getPageContent(String pagePath) {
        try {
            SlingHttpResponse resp = doGet(pagePath);
            return Jsoup.parseBodyFragment(resp.getContent());
        } catch (ClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return null;
    }

    public String getHomePath() {
        return homePath;
    }

    public void validateUserAgreement() {
        try {
            URLParameterBuilder params = URLParameterBuilder.create();
            params.add("query", "{\"condition\":[{\"named\":\"" + getUser() + "\"}]}");

            SlingHttpResponse exec = doGet("/libs/granite/security/search/authorizables.json", params.getList());
            JsonNode authorizablesJson = JsonUtils.getJsonNodeFromString(exec.getContent());
            JsonNode userJson = authorizablesJson.get("authorizables").get(0);

            String agreementPath = userJson.get("home").getTextValue() + "/profile";

            setPropertyString(agreementPath, "userAgreementAccepted", "true");
        } catch (ClientException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
