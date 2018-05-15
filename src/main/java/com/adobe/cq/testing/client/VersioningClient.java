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
import org.apache.http.Header;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;
import org.codehaus.jackson.JsonNode;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import static org.apache.http.HttpStatus.SC_OK;

public class VersioningClient extends CQClient {

    private static final String PAGE_VERSIONS_URL = "/bin/wcm/versions.json";
    private static final String TREE_VERSIONS_URL = "/bin/wcm/versiontree";

    public VersioningClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public VersioningClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }


    /**
     * Returns the json node that contains all asset versions.
     * 
     * @param assetLocation Location in repository where the asset is stored.
     * @return An array of all versions specific for this asset.
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode getAssetVersions(String assetLocation) throws ClientException, InterruptedException {

        return doGetJson(assetLocation + ".version.json", -1).get("versions");

    }


    /**
     * Creates a new snapshot for the specific asset.
     * 
     * @param assetLocation
     *            Location in repository where the asset is stored.
     * @param versionLabel
     *            The snapshot label.
     * @param versionComment
     *            The snapshot specific comment.
     * @param expectedStatus list of expected status list of expected status
     * @return the response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse createAssetVersion(String assetLocation, String versionLabel, String versionComment, int... expectedStatus)
                    throws ClientException {
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter("cmd", "createVersion");
        formEntry.addParameter("_charset_", "utf-8");
        formEntry.addParameter(":status", "browser");
        formEntry.addParameter("label", versionLabel);
        formEntry.addParameter("comment", versionComment);
        return doPost(assetLocation + ".version.html", formEntry.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Restores the specific version.
     * 
     * @param assetLocation
     *            Location in repository where the asset is stored.
     * @param versionId
     *            The version's unique id.
     * @param expectedStatus list of expected status
     * @return the response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse restoreAssetVersion(String assetLocation, String versionId, int... expectedStatus) throws ClientException {

        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter("cmd", "restoreVersion");
        formEntry.addParameter("id", versionId);
        return doPost(assetLocation + ".version.html", formEntry.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Returns an json node with all existent page versions.
     * 
     * @param pageLocation
     *            Location in repository where the page is stored.
     * @param expectedStatus list of expected status
     * @return the response
     * @throws ClientException if the request fails
     */
    public JsonNode getPageVersions(String pageLocation, int... expectedStatus) throws ClientException {

        URLParameterBuilder params = URLParameterBuilder.create().add(new BasicNameValuePair("path", pageLocation));
        SlingHttpResponse exec = doGet(PAGE_VERSIONS_URL, params.getList(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return JsonUtils.getJsonNodeFromString(exec.getContent()).path("versions");
    }


    /**
     * Returns an json node with all existent versions of a given tree.
     * 
     * @param path
     *            Path of the tree root
     * @param date
     *            calendar date to restore to
     * @param preserverNVP
     *            if true non versionable nodes are preserved
     * @param node
     *            path to look for inside the tree root
     * @param expectedStatus list of expected status
     * @return the response
     * @throws ClientException if the request fails
     */
    public JsonNode getTreeVersions(String path, Date date, boolean preserverNVP, String node, int... expectedStatus)
                    throws ClientException {
        URLParameterBuilder params = URLParameterBuilder.create();
        params.add(new BasicNameValuePair("path", path));
        params.add(new BasicNameValuePair("preserveNVP", Boolean.toString(preserverNVP)));
        params.add(new BasicNameValuePair("node", (node == null ? "." : node)));

        if (date != null) {
            params.add(new BasicNameValuePair("date", TestUtil.ISO_DATETIME_TIME_ZONE_FORMAT.format(date)));
        } else {
            params.add(new BasicNameValuePair("date", null));
        }

        SlingHttpResponse exec = doGet(TREE_VERSIONS_URL, params.getList(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }


    /**
     * Returns a json node with all existent versions for the children pages.
     *
     * @param parentPageLocation parent page
     * @param expectedStatus list of expected http status codes
     * @return the versions as json node
     * @throws ClientException if the request fails
     */
    public JsonNode getChildrenPageVersions(String parentPageLocation, int... expectedStatus) throws ClientException {

        URLParameterBuilder params = URLParameterBuilder.create();
        params.add(new BasicNameValuePair("path", parentPageLocation));
        params.add(new BasicNameValuePair("showChildren", "true"));
        SlingHttpResponse exec = doGet(PAGE_VERSIONS_URL, params.getList(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        return JsonUtils.getJsonNodeFromString(exec.getContent()).path("versions");
    }


    /**
     * Returns the version creation date.
     * 
     * @param version
     *            The Json node for the specific version.
     * @return Version creation date.
     * @throws java.text.ParseException if the date is not valid
     */
    public long getVersionCreationDate(JsonNode version) throws ParseException {
        String creationDate = version.path("created").getValueAsText();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        Date convertDate = dateFormat.parse(creationDate);
        return convertDate.getTime();
    }


    /**
     * Returns a page from a specific date.
     * 
     * @param pageLocation
     *            The path to the requested page.
     * @param date
     *            The date (in miliseconds) for the requested page.
     * @param expectedStatus list of expected http status codes
     * @return The page from the specific date.
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse getPageWithTimeWrap(String pageLocation, long date, int... expectedStatus) throws ClientException {
        return doGet(pageLocation + ".html", null,
                Collections.<Header>singletonList(new BasicHeader("Cookie", "timewarp=" + Long.toString(date))),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
}
