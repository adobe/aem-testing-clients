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

import com.adobe.cq.testing.util.WCMCommands;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * <p>Base client for all CQ related clients. It provides a core set of commonly used website and page functions
 * e.g. creating/deleting/moving pages, versioning, activation/deactivation, restore tree etc. </p>
 *
 * <p>It extends from {@link SlingClient} which in turn provides functions for
 * manipulating repository nodes directly.</p>
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class CQClient extends SlingClient {
    public static Logger LOG = LoggerFactory.getLogger(CQClient.class);

    /**
     * Path where statistics are stored
     */
    protected static final String STATISTICS_ROOT = "/var/statistics/pages";

    /**
     * WCMCommands object that encapsulates all available WCM commands
     */
    protected final WCMCommands wcmCommands = new WCMCommands(this);

    /**
     * Constructor used by Builders and adaptTo(). <b>Should never be called directly from the code.</b>
     * See AbstractSlingClient#AbstractSlingClient(CloseableHttpClient, SlingClientConfig)
     *
     * @param http   the underlying HttpClient to be used
     * @param config sling specific configs
     * @throws ClientException if the client could not be created
     */
    public CQClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    /**
     * <p>Handy constructor easy to use in simple tests. Creates a client that uses basic authentication.</p>
     *
     * <p>For constructing clients with complex configurations, use a {@link InternalBuilder}</p>
     *
     * <p>For constructing clients with the same configuration, but a different class, use {@link #adaptTo(Class)}</p>
     *
     * @param url      url of the server (including context path)
     * @param user     username for basic authentication
     * @param password password for basic authentication
     * @throws ClientException never, kept for uniformity with the other constructors
     */
    public CQClient(URI url, String user, String password) throws ClientException {
        super(url, user, password);
    }

    /**
     * Creates a CQ page in the repository.
     *
     * @param pageName       name of the page
     * @param pageTitle      title of the page
     * @param parentPath     path to the parent page
     * @param templatePath   path to the template
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return a {@link SlingHttpResponse}
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse createPage(String pageName, String pageTitle, String parentPath, String templatePath,
                                        int... expectedStatus) throws ClientException {
        return wcmCommands.createPage(pageName, pageTitle, parentPath, templatePath, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Tries to create a CQ page until the request succeeds or timeout is reached
     *
     * @param pageName       name of the page
     * @param pageTitle      title of the page
     * @param parentPath     path to the parent page
     * @param templatePath   path to the template definition
     * @param timeout        max execution time, in milliseconds
     * @param delay          time to wait between retries, in milliseconds
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return a {@link SlingHttpResponse}
     * @throws ClientException      if something fails during the request/response cycle
     * @throws InterruptedException to mark this method as waiting
     */
    public SlingHttpResponse createPageWithRetry(final String pageName, final String pageTitle,
                                                 final String parentPath, final String templatePath,
                                                 long timeout, long delay, final int... expectedStatus)
            throws ClientException, InterruptedException {

        class CreatePagePolling extends Polling {
            SlingHttpResponse response;

            @Override
            public Boolean call() throws Exception {
                try {
                    response = wcmCommands.createPage(pageName, pageTitle, parentPath, templatePath, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
                } catch (ClientException e) {
                    ClientException ex = (ClientException) e.getCause();
                    response = ex.getResponse();
                    LOG.info("Exception while creating page " + e.toString() + "\n Cause is: " + e.getCause());
                    throw e;
                }
                return true;
            }
        }

        CreatePagePolling createPolling = new CreatePagePolling();
        try {
            createPolling.poll(timeout, delay);
        } catch (TimeoutException e) {
            throw new ClientException("Failed to create page " + pageName + " in " + createPolling.getWaited(), e);
        }
        return createPolling.response;
    }

    /**
     * <p>Deletes an array of pages.</p>
     * <p>The caller must ensure that the paths can be deleted</p>
     *
     * @param pagePaths      array of paths to be deleted
     * @param force          force param passed to wcmCommands
     * @param shallow        shallow param passed to wcmCommands
     * @param expectedStatus list of expected HTTP status to be returned
     * @return the response
     * @throws ClientException if one of the pages fails to delete
     */
    public SlingHttpResponse deletePage(String[] pagePaths, boolean force, boolean shallow, int... expectedStatus)
            throws ClientException {
        return wcmCommands.deletePage(pagePaths, force, shallow, expectedStatus);
    }

    /**
     * Tries to deletes a CQ page multiple times if the request fails
     *
     * @param pagePath       the path to delete
     * @param force          passed to wcmCommands
     * @param shallow        passed to wcmCommands
     * @param expectedStatus list of expected HTTP status to be returned
     * @param timeout        max execution time, in milliseconds
     * @param delay          time to wait between retries, in milliseconds
     * @return the response
     * @throws ClientException      if the page(s) wre not deleted
     * @throws InterruptedException if the method was interrupted
     */
    public SlingHttpResponse deletePageWithRetry(final String pagePath, final boolean force, final boolean shallow,
                                                 long timeout, long delay, final int... expectedStatus)
            throws ClientException, InterruptedException {

        // The deletePage call might fail with a server-side StaleItemStateException, as we're deleting a page
        // that we just created and CQ listeners might still be making modifications to it.
        // Retry a few times if that happens - this is an "unusual" use case that we don't need to solve server-side.
        class DeletePagePolling extends Polling {
            SlingHttpResponse response;

            @Override
            public Boolean call() throws ClientException {
                response = deletePage(new String[]{pagePath}, force, shallow, expectedStatus);
                return !pageExists(pagePath);
            }
        }

        DeletePagePolling deletePagePolling = new DeletePagePolling();
        try {
            deletePagePolling.poll(timeout, delay);
        } catch (TimeoutException e) {
            throw new ClientException("Could not delete page " + pagePath + " as user " + getUser(), e);
        }

        return deletePagePolling.response;
    }

    /**
     * Returns whether a CQ page exists or not
     *
     * @param pagePath The path of the page
     * @return whether the CQ page exists
     * @throws ClientException If the request failed
     */
    public boolean pageExists(String pagePath) throws ClientException {
        SlingHttpResponse response = getAuthorSitesPage(pagePath);
        final int status = response.getStatusLine().getStatusCode();
        return status == SC_OK;
    }

    /**
     * Polls on whether a CQ page exists or not
     *
     * @param pagePath The path of the page
     * @param timeout  Timeout in milliseconds for the poller
     * @return whether the CQ page exists
     * @throws InterruptedException if interrupted
     */
    public boolean pageExistsWithRetry(String pagePath, int timeout) throws InterruptedException {
        try {
            new Polling(() -> pageExists(pagePath)).poll(timeout, 500);
        } catch (TimeoutException e) {
            return false;
        }
        return true;
    }

    /**
     * Polls on whether a CQ page exists or not after 1 second
     *
     * @param pagePath The path of the page
     * @return whether the CQ page exists
     * @throws InterruptedException if interrupted
     */
    public boolean pageExistsWithRetry(String pagePath) throws InterruptedException {
        return pageExistsWithRetry(pagePath, 1000);
    }

    /**
     * Get a CQ Page (.html extension)
     *
     * @param pagePath       The path of the page
     * @param expectedStatus An array of expected HTTP status codes for the response
     * @return the http response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse getAuthorSitesPage(String pagePath, int... expectedStatus) throws ClientException {
        final String uriPath = getPageAbsoluteUri(pagePath).toString();
        return this.doGet(uriPath, expectedStatus);
    }

    private URI getPageAbsoluteUri(String pagePath) {
        final String path = pagePath.endsWith(".html")
                ? pagePath
                : pagePath.replaceFirst("/*$", "").concat(".html");
        return this.getUrl(path);
    }

    /**
     * Copies one or more CQ pages to a specified location in the repository.
     *
     * @param srcPaths       list of pages to copy
     * @param destName       name given to the copied page at new location. Only works for single page copy, otherwise
     *                       the operation fails.
     * @param destPath       destination of copy operation. Can be used instead of destParentPath + destName.
     *                       Works only for single page copy, otherwise the operation fails.
     * @param destParentPath target location of the copy operation
     * @param before         if true, the copied page will be ordered before the page with this label (Name)
     * @param shallow        if true, the only the page itself gets copied
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the http response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse copyPage(String[] srcPaths, String destName, String destPath, String destParentPath,
                                      String before, boolean shallow, int... expectedStatus)
            throws ClientException {
        return wcmCommands.copyPage(srcPaths, destName, destParentPath, before, shallow,
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Moves one or more CQ pages to a specified location.<br>
     * <br>
     * Setting {@code integrity} to false and list of referrers in {@code adjusts} to null equals
     * a drag'n'drop move in the site admin. The server then performs an auto adjustment on referring pages.<br>
     * <br>
     * Passing a list of referrers and setting integrity flag to {@code true} is the same as the
     * {@code Move...} command in the site admin.
     *
     * @param srcPaths       list of pages to copy
     * @param destName       name given to the moved page at new location. Only works for single page copy, otherwise
     *                       the operation fails.
     * @param destPath       destination of copy operation. Can be used instead of destParentPath + destName. Only
     *                       works for single page copy, otherwise the operation fails.
     * @param destParentPath target location of the move operation
     * @param before         if true, the copied page will be ordered before the page with this label (Name)
     * @param shallow        if true, the only the page itself gets copied
     * @param integrity      if true, no auto adjustment of referred pages will be done
     * @param adjusts        List of referrer page paths that need adjusting
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse movePage(String[] srcPaths, String destName, String destPath, String destParentPath,
                                      String before, boolean shallow, boolean integrity, String[] adjusts,
                                      int... expectedStatus)
            throws ClientException {
        return wcmCommands.movePage(srcPaths, destName, destParentPath, before, shallow,
                integrity, adjusts, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Moves one or more CQ pages to a specified location.<br>
     * <br>
     * Setting {@code integrity} to false and list of referrers in {@code adjusts} to null equals
     * a drag'n'drop move in the site admin. The server then performs an auto adjustment on referring pages.<br>
     * <br>
     * Passing a list of referrers and setting integrity flag to {@code true} is the same as the
     * {@code Move...} command in the site admin.
     *
     * @param srcPaths       list of pages to copy
     * @param destName       name given to the moved page at new location. Only works for single page copy, otherwise
     *                       the operation fails.
     * @param destPath       destination of copy operation. Can be used instead of destParentPath + destName. Only
     *                       works for single page copy, otherwise the operation fails.
     * @param destParentPath target location of the move operation
     * @param before         if true, the copied page will be ordered before the page with this label (Name)
     * @param shallow        if true, the only the page itself gets copied
     * @param integrity      if true, no auto adjustment of referred pages will be done
     * @param adjusts        list of referrer page paths that need adjusting
     * @param publishes      list of page paths that need to be published
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse movePage(String[] srcPaths, String destName, String destPath, String destParentPath,
                                      String before, boolean shallow, boolean integrity, String[] adjusts,
                                      String[] publishes, int... expectedStatus)
            throws ClientException {
        return wcmCommands.movePage(srcPaths, destName, destParentPath, before, shallow,
                integrity, adjusts, publishes, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Locks a CQ page so it can only be edited by the person who locked it.
     *
     * @param path           path of the page to lock
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse lockPage(String path, int... expectedStatus) throws ClientException {
        return wcmCommands.lockPage(path, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Unlocks a previously locked CQ page.
     *
     * @param path           path of the page to unlock
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse unlockPage(String path, int... expectedStatus) throws ClientException {
        return wcmCommands.unlockPage(path, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Sets a single page property on a CQ page.
     *
     * @param pagePath       path of the page to be edited
     * @param propName       name of the property to be edited
     * @param propValue      value to be set
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse setPageProperty(String pagePath, String propName, String propValue, int... expectedStatus)
            throws ClientException {
        ArrayList<NameValuePair> list = new ArrayList<>();
        list.add(new BasicNameValuePair(propName, propValue));
        return setPageProperties(pagePath, list, expectedStatus);
    }

    /**
     * Sets multiple page properties on a CQ page with one request.
     *
     * @param pagePath       path of the page to be edited
     * @param props          list of name/value string pairs to be set
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse setPageProperties(String pagePath, List<NameValuePair> props, int... expectedStatus)
            throws ClientException {
        UrlEncodedFormEntity formEntry = FormEntityBuilder.create()
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addAllParameters(props)
                .build();

        return doPost(pagePath + "/jcr:content", formEntry, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Sets the teaser image of a content page.
     *
     * @param pagePath       path to the page to be edited
     * @param mimeType       MIME type of the image getting uploaded
     * @param fileName       file name of the image
     * @param resourcePath   defines the path to the resource
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse setPagePropertyImage(String pagePath, String mimeType, String fileName, String resourcePath,
                                                  int... expectedStatus) throws ClientException {

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("./image/file", ResourceUtil.getResourceAsStream(resourcePath),
                        ContentType.create(mimeType), fileName)
                .setCharset(StandardCharsets.UTF_8)
                .build();

        // send the request with the multipart entity as content
        return doPost(pagePath + "/jcr:content", entity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Uploads an <b>File</b> to the repository. Same as using {@code New File...} in the Site Admin outside of the
     * {@code Digital Assets} folder.<br>
     * <br>
     * This will create a folder with the file name and upload the file below it in a node typed {@code nt:file}.<br>
     * To upload a file that is to be handled as an Asset use {@link CQAssetsClient#uploadAsset}  instead.<br>
     * To upload a file directly using sling use {@link #upload(java.io.File, String, String, boolean, int...) upload}.
     *
     * @param fileName       file name. The file name will become part of the URL to the file.
     * @param resourcePath   defines the path to the resource
     * @param mimeType       MIME type of the image getting uploaded
     * @param parentPath     parent page (folder) that will contain the file
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 201 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse uploadFileCQStyle(String fileName, String resourcePath, String mimeType,
                                               String parentPath, int... expectedStatus) throws ClientException {
        String filePath = parentPath + "/" + fileName;

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("file", ResourceUtil.getResourceAsStream(resourcePath),
                        ContentType.create(mimeType), fileName)
                .addTextBody("fileName", fileName)
                .setCharset(StandardCharsets.UTF_8)
                .build();

        return doPost(filePath, entity, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
    }

    /**
     * Creates a Version for a CQ page. See {@code Version} tab in the sidekick of a page when opened on
     * an author instance.
     *
     * @param pagePath       path of the page we want to create a version of
     * @param comment        comment to be set for this version
     * @param label          Version label to be set
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse createVersion(String pagePath, String comment, String label, int... expectedStatus)
            throws ClientException {
        // execute the request
        return wcmCommands.createVersion(pagePath, comment, label, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Restores a specified version of a CQ page. See {@code Version} tab in the sidekick of a page when opened
     * on an author instance.
     *
     * @param versionIds     version Id of the current page and/or version ids of sub pages of {@code pagePath}
     * @param pagePath       path to the page we want to restore the version
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse restoreVersion(String[] versionIds, String pagePath, int... expectedStatus)
            throws ClientException {
        // execute the request
        return wcmCommands.restoreVersion(versionIds, pagePath, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Restores a sub page in path to the version that existed at the given date.
     *
     * @param path           path of the root page of the tree
     * @param date           the date to restore
     * @param preserveNVP    whether to preserve NVP or not
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse restoreTree(String path, Date date, boolean preserveNVP,
                                         int... expectedStatus) throws ClientException {
        return wcmCommands.restoreTree(path, date, preserveNVP, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Creates language copies af a master site.
     *
     * @param sitePath       path to the site with the master content
     * @param relPaths       list of string pairs, first being the relative path to be created, second the language
     *                       shortcut to be copied from e.g. {fr/products/circle,en}
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse copyLanguages(String sitePath, List<BasicNameValuePair> relPaths, int... expectedStatus)
            throws ClientException {
        return wcmCommands.copyLanguages(sitePath, relPaths, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Rolls out changes to the livecopy
     *
     * @param srcPath          the blue print path
     * @param targetPaths      the live copy paths
     * @param deep             if set to false, page is the fallback
     * @param reset            reset
     * @param useBackgroundJob if set to true background jobs are used for rollout
     * @param expectedStatus   list of expected HTTP Status to be returned, if not set, 200 is assumed
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse rolloutPage(String srcPath, String[] targetPaths, boolean deep, boolean reset,
                                         boolean useBackgroundJob, int... expectedStatus)
            throws ClientException {
        return wcmCommands.rollout(new String[]{srcPath}, targetPaths, null, (deep) ? "deep" : "page",
                reset, useBackgroundJob, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Requests a text search on the content of a path and returns the pages matching the criteria.
     *
     * @param startPath     Path to look at
     * @param searchQuery   The text to search for
     * @param caseSensitive Do a "case-sensitive" search or not
     * @param wholeWordOnly Do a "whole word" search or not
     * @return The list of pages matching the search criteria
     * @throws Exception If something fails during the request/response cycle
     */
    public JsonNode searchInPages(String startPath, String searchQuery, boolean caseSensitive, boolean wholeWordOnly)
            throws Exception {
        String searchPath = startPath + ".find.json";

        ArrayList<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("f", searchQuery));
        params.add(new BasicNameValuePair(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8));
        params.add(new BasicNameValuePair("cs", Boolean.toString(caseSensitive)));
        params.add(new BasicNameValuePair("wwo", Boolean.toString(wholeWordOnly)));

        SlingHttpResponse exec = doGet(searchPath, params, SC_OK);

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * Requests a search and replaces the content of a path.
     *
     * @param startPath     Path to look at
     * @param searchQuery   The text to search for
     * @param replace       The text to replace with
     * @param caseSensitive Do a "case-sensitive" search or not
     * @param wholeWordOnly Do a "whole word" search or not
     * @throws Exception If something fails during the request/response cycle
     */
    public void searchAndReplaceInPages(String startPath, String searchQuery, String replace, boolean caseSensitive,
                                        boolean wholeWordOnly)
            throws Exception {

        JsonNode pages = searchInPages(startPath, searchQuery, caseSensitive, wholeWordOnly).get("matches");

        if (pages.isArray() && pages.size() > 0) {
            String replacePath = startPath + ".replace.json";

            FormEntityBuilder entityBuilder = FormEntityBuilder.create();
            entityBuilder.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
            entityBuilder.addParameter("f", searchQuery);
            entityBuilder.addParameter("r", replace);

            if (caseSensitive) {
                entityBuilder.addParameter("cs", "on");
            }

            if (wholeWordOnly) {
                entityBuilder.addParameter("wwo", "on");
            }

            for (Iterator<JsonNode> it = pages.elements(); it.hasNext(); ) {
                JsonNode page = it.next();
                entityBuilder.addParameter("p", page.get("path").textValue());
            }

            doPost(replacePath, entityBuilder.build(), SC_OK);
        }
    }

    /**
     * Deletes existing statistics (page impressions) of a page.
     *
     * @param pagePath path of the page to delete the statistics for.
     * @throws ClientException if something fails during the request/response cycle
     */
    public void resetPageStatistics(String pagePath) throws ClientException {
        deletePath(STATISTICS_ROOT + pagePath);
    }


    // Builders

    public static abstract class InternalBuilder<T extends CQClient> extends SlingClient.InternalBuilder<T> {

        protected InternalBuilder(URI url, String user, String password) {
            super(url, user, password);
        }
    }

    public final static class Builder extends InternalBuilder<CQClient> {

        private Builder(URI url, String user, String password) {
            super(url, user, password);
        }

        @Override
        public CQClient build() throws ClientException {
            return new CQClient(buildHttpClient(), buildSlingClientConfig());
        }

        public static Builder create(URI url, String user, String password) {
            return new Builder(url, user, password);
        }
    }
}
