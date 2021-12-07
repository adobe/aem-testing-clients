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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.*;


import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;


public class BulkEditorClient extends CQClient {
    public BulkEditorClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public BulkEditorClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Search with bulk editor.
     * 
     * @param rootPath
     *            The search root path.
     * @param queryParams
     *            GQL query parameters.
     * @param cols
     *            List of columns to be displayed.
     * @return Search results.
     * @throws ClientException if the request fails
     */
    public JsonNode search(String rootPath, String queryParams, String[] cols)
            throws ClientException {
        // build the request
        ArrayList<String> augumentedCols = new ArrayList<>(cols.length);
        for (String col : cols) {
            augumentedCols.add("jcr:content/" + col);
        }
        String columns = StringUtils.join(augumentedCols, ",");

        List<NameValuePair> params = new ArrayList<>(3);
        params.add(new BasicNameValuePair("query", "path:" + rootPath + " " + queryParams));
        params.add(new BasicNameValuePair("tidy", "true"));
        params.add(new BasicNameValuePair("cols", columns));

        SlingHttpResponse exec = doGet("/etc/importers/bulkeditor/query.json", params, SC_OK);
        return JsonUtils.getJsonNodeFromString(exec.getContent()).path("hits");
    }

    /**
     *
     * Import content from a TSV file.
     *
     * @param rootPath where to import
     * @param fileName the file to import
     * @param resourceFile resource file
     * @param expectedStatus list of expected http status codes
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse importContent(String rootPath, String fileName,
                                           String resourceFile, int... expectedStatus)
            throws ClientException {
        return importContent(rootPath, fileName, ResourceUtil.getResourceAsStream(resourceFile), expectedStatus);
    }

    /**
     * Import content from a TSV file.
     *
     * @param rootPath where to import
     * @param fileName the file name
     * @param inputStream the document input stream
     * @param expectedStatus list of expected http status codes
     * @return the http response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse importContent(String rootPath, String fileName,
                                           InputStream inputStream, int... expectedStatus)
            throws ClientException {

        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("./rootPath", rootPath)
                .addBinaryBody("document", inputStream,
                        ContentType.create("text/tab-separated-values"), fileName)
                .addTextBody("./contentMode", "on")
                .addTextBody("document@TypeHint", "nt:file")
                .build();

        return doPost("/etc/importers/bulkeditor/import", entity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Export content to TSV file.
     * 
     * @param rootPath Content root path.
     * @param queryParams GQL query parameters.
     * @param cols List of columns to be exported.
     * @return File content.
     * @throws ClientException if the request fails
     */
    public String exportContent(String rootPath, String queryParams,
            String[] cols) throws ClientException {
        // build the request
        ArrayList<String> augumentedCols = new ArrayList<>(cols.length);
        for (String col : cols) {
            augumentedCols.add("jcr:content/" + col);
        }
        String columns = StringUtils.join(augumentedCols, ",");

        URLParameterBuilder builder = URLParameterBuilder.create();
        builder.add("query", "path:" + rootPath + " " + queryParams);
        builder.add("tidy", "true");
        builder.add("cols", columns);
        builder.add("separator", "null");

        SlingHttpResponse exec = doGet("/etc/importers/bulkeditor/export.tsv", builder.getList(), SC_OK);
        return exec.getContent();
    }

    /**
     * Update property value.
     * 
     * @param nodePath The path to the property.
     * @param newValue The value for the property.
     * @param expectedStatus list of expected http status codes
     * @return the response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse changePropertyValue(String nodePath,
                                                 String newValue, int... expectedStatus) throws ClientException {
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("_charset_", "utf-8");
        form.addParameter(nodePath, newValue);

        return doPost("/", form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
}
