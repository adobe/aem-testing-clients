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

import com.adobe.cq.testing.util.TestUtil;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.SlingParameter;
import org.apache.sling.testing.clients.util.URLParameterBuilder;
import org.codehaus.jackson.JsonNode;

import java.net.URI;
import java.util.Date;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * The base client for all json related tests. It provides a core set of commonly used json
 * functions e.g. pages / versions / references... <br>
 * <br>
 * It extends from {@link CQClient} which in turn provides a core set of
 * commonly used website and page functionality.
 */
public class JsonClient extends CQClient {

    public JsonClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public JsonClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Get children for a given page.
     *
     * @param parentPath parent page to get children from
     * @return the root {@link org.codehaus.jackson.JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getPages(String parentPath) throws ClientException {
        String path = parentPath + ".pages.json?tidy=true&predicate=page";
        SlingHttpResponse exec = doGet(path, SC_OK);
        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Get all references used and its status in a given page
     * <br>
     * In case path is not valid, an empty pages array will be returned
     *
     * @param pagePaths the page path to get the references for
     * @return the root {@link org.codehaus.jackson.JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getPageReferences(String[] pagePaths) throws ClientException {
        String getPath = "/bin/wcm/references";

        URLParameterBuilder params = URLParameterBuilder.create()
                .add("tidy", Boolean.TRUE.toString())
                .add(new SlingParameter("path").values(pagePaths).multiple().toNameValuePairs());

        SlingHttpResponse exec = doGet(getPath, params.getList(), SC_OK);

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Queries the list of page references for a given page, as it is used to
     * display in the move dialog.
     *
     * @param pagePath       the page path to get the references for
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return the root {@link org.codehaus.jackson.JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getMoveReferences(String pagePath, int... expectedStatus) throws ClientException {
        if (pagePath == null) {
            throw new IllegalArgumentException("pagePath parameter must not be null");
        }

        String postPath = "/bin/wcm/heavymove";
        SlingHttpResponse exec = doGet(postPath, URLParameterBuilder.create().add("path", pagePath).getList(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Get list of available languages for a page.
     * <br>
     * In case path is not valid, an empty rows array will be returned
     *
     * @param sitePath the page path to get the languages for
     * @param deep     true if languages' subpages should be part of json
     * @return the root {@link org.codehaus.jackson.JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getLanguages(String sitePath, boolean deep) throws ClientException {
        if (sitePath == null) {
            throw new IllegalArgumentException("sitePath parameter must not be null");
        }
        String getPath = sitePath + ".languages.json";
        SlingHttpResponse exec = doGet(getPath, URLParameterBuilder.create().add("deep", Boolean.toString(deep)).getList(), SC_OK);
        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Get versions for a given page
     *
     * @param pagePath       the page path to get the versions for
     * @param showChildren   true if versions of the children should be included
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return the root {@link org.codehaus.jackson.JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getVersions(String pagePath, boolean showChildren, int... expectedStatus) throws ClientException {
        String getPath = "/bin/wcm/versions.json";

        SlingHttpResponse exec = doGet(getPath, URLParameterBuilder.create()
                .add("path", pagePath)
                .add("showChildren", Boolean.toString(showChildren))
                .getList(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Get version tree for a given page.
     *
     * @param pagePath the page path to get the version tree for
     * @param node     the relative path to node
     * @param date     the date which versions should be shown e.g. 2010-01-01T12:00:00+01:00
     * @param expectedStatus list of expected http status codes
     * @return the root {@link org.codehaus.jackson.JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getVersionTree(String pagePath, String node, Date date,
                                   int... expectedStatus) throws ClientException {
        String getPath = "/bin/wcm/versiontree";

        SlingHttpResponse exec = doGet(getPath, URLParameterBuilder.create()
                .add("path", pagePath)
                .add("node", node)
                .add("date", date == null ? null : TestUtil.ISO_DATETIME_TIME_ZONE_FORMAT.format(date))
                .getList(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Get user generated pages in the {@code user generated content} (ucg) space,
     * currently under {@code /content/usergenerated}.
     * <br>
     * In case path is not valid, an empty pages array will be returned
     *
     * @param ugcBasePath The base path to start listing
     * @param limit       limits the number of nodes below ugcBasePath to be returned
     * @param start       defines the start node below ugcBasePath
     * @param predicate   Predicate used to filter hierarchy nodes in the siteadmin e.g. siteadmin (nt:hierarchy- node,
     *                    not hidden, no file
     * @param view        filter: dependent from module
     * @return a {@link org.codehaus.jackson.JsonNode} mapping to the requested content node.
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getUserGeneratedPages(String ugcBasePath, int limit, int start, String predicate, String view)
            throws ClientException {
        String getPath = "/content/usergenerated" + ugcBasePath + ".ugc.json";
        URLParameterBuilder params = URLParameterBuilder.create();
        params.add("limit", Integer.valueOf(limit).toString());
        params.add("start", Integer.valueOf(start).toString());
        params.add("predicate", predicate);
        if (view != null) {
            params.add("view", view);
        }
        SlingHttpResponse exec = doGet(getPath, params.getList(), SC_OK);

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }
}
