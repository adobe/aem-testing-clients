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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;
import org.jsoup.nodes.Document;

import java.net.URI;

import static org.apache.http.HttpStatus.SC_OK;

public class FeedClient extends TartanClient {

    private static final String REFRESH_SUFFIX_PATH = "/_jcr_content.refresh.json";
    private static final String CONTENT_SUFFIX_PATH = ".content.html";

    private String defaultFeedPath = null;

    public FeedClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);

        homePath += "/home/users/" + getUser();
        defaultFeedPath = homePath + "/feed";
    }

    public FeedClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);

        homePath += "/home/users/" + user;
        defaultFeedPath = homePath + "/feed";
    }

    public Boolean feedHasChanged() throws ClientException {
        return feedHasChanged("-1");
    }

    public Boolean feedHasChanged(String since) throws ClientException {
        return feedHasChanged("-1", defaultFeedPath);
    }

    public Boolean feedHasChanged(String since, String feedPath) throws ClientException {
        Boolean hasChanges = false;

        String checkRefreshPath = feedPath + REFRESH_SUFFIX_PATH;

        URLParameterBuilder params = URLParameterBuilder.create().add("since", since);
        SlingHttpResponse exec = doGet(checkRefreshPath, params.getList(), SC_OK);

        String count = JsonUtils.getJsonNodeFromString(exec.getContent()).get("count").getTextValue();
        return "1".equals(count);
    }

    public Document getFeedContent() {
        return getPageContent(defaultFeedPath + CONTENT_SUFFIX_PATH);
    }

    public String getDefaultFeedPath() {
        return defaultFeedPath;
    }
}
