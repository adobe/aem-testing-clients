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

import com.adobe.cq.testing.client.tagging.Tag;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * The base client for all tag related tests. It provides a core set of commonly used tag functions e.g. create / edit /
 * delete <br> <br> It extends from {@link CQClient} which in turn provides a core set of commonly used website and
 * page functionality.
 */
public class TagClient extends CQClient {

    public static final String TAG_COMMAND_PATH = "/bin/tagcommand";

    private static final String TAG_COMMAND_CAN_CREATE = "canCreateTag";
    private static final String TAG_COMMAND_CAN_CREATE_BY_TITLE = "canCreateTagByTitle";
    private static final String TAG_COMMAND_CREATE = "createTag";
    private static final String TAG_COMMAND_CREATE_BY_TITLE = "createTagByTitle";
    private static final String TAG_COMMAND_DELETE = "deleteTag";
    private static final String TAG_COMMAND_LIST = "list";
    private static final String TAG_COMMAND_MOVE = "moveTag";
    private static final String TAG_COMMAND_MERGE = "mergeTag";
    private static final String TAG_COMMAND_ACTIVATE = "activateTag";
    private static final String TAG_COMMAND_DEACTIVATE = "deactivateTag";
    private static final String TAG_COMMAND_RUN_GC = "gc";

    public static final String TAG_RESOURCE_PATH = ".tag";

    private static String TAG_GARBAGE_COLLECTOR_CONFIG_PATH = "/system/console/configMgr/com.day.cq.tagging.impl.TagGarbageCollector";

    public static String TAG_GARBAGE_COLLECTOR_DEFAULT_CRON_EXPRESSION = "0 0 12 * * ?";
    public static String TAG_GARBAGE_COLLECTOR_CRON_EXPRESSION_PROP = "scheduler.expression";

    public TagClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public TagClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Adds a TagCloud Component to a page.
     *
     * @param pageHandle handle of the page that will get a tag cloud component added
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 201 (CREATED) is
     * assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse addTagCloudComponent(String pageHandle, int... expectedStatus) throws ClientException {
        // the doPost goes to the paragraph system on the page
        final String postPath = pageHandle + "/jcr:content/par/";

        // build the form to submit
        UrlEncodedFormEntity feb = FormEntityBuilder.create()
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("./sling:resourceType", "cq/tagging/components/tagcloud")
                .addParameter(":nameHint", "tagcloud")
                .addParameter("parentResourceType", "foundation/components/parsys")
                .build();

        // execute the request
        return doPost(postPath, feb, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
    }


    /**
     * Edits the TagCloud component.
     *
     * @param componentPath the components path
     * @param showAsLink true if tags should have link tags (execute a search for the tag)
     * @param path optional, path of the page, the display values 'page' and 'pagetree' relate to.
     * @param display what should be rendered:
     *                <ul>
     *                    <li>page: show only tags of this page or the page referenced in path</li>
     *                    <li>pagetree: show tags of this page and sub pages or the refrenced page in path and its according subpages</li>
     *                    <li>all: show all tags set in the site</li>
     *                </ul>
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 201 (CREATED) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse editTagCloudComponent(String componentPath, boolean showAsLink, String path, String display,
                                                   int... expectedStatus) throws ClientException {

        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("./sling:resourceType", "cq/tagging/components/tagcloud");
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        form.addParameter("./display", display);

        if (!showAsLink) {
            form.addParameter("./noLinks", "true");
            form.addParameter("./noLinks@Delete", "true");
        } else {
            form.addParameter("./noLinks@Delete", "true");
        }

        // if null any previously set path gets cleared
        form.addParameter("./path", path);

        return doPost(componentPath, form.build(), HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
    }


    /**
     * Checks if user can create a specific Tag
     *
     * @param tagId tag ID. mandatory.
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  canCreateTag(String tagId, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_CAN_CREATE);
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        form.addParameter("tag", tagId);

        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Checks if user can create a specific Tag based on title
     *
     * @param tagId tag ID. mandatory.
     * @param locale i18n locale code.
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  canCreateTagByTitle(String tagId, String locale, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_CAN_CREATE_BY_TITLE);
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        form.addParameter("tag", tagId);
        form.addParameter("locale", locale);

        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Adds a tag
     *
     * @param tagTitle title of the Tag. mandatory.
     * @param tagId tag ID. mandatory.
     * @param tagDescr description of the tag. Set null if not used.
     * @param parentTagId parent tag ID. Set null if not used. E.g. stockphotography:animals/birds/
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  createTag(String tagTitle, String tagId, String tagDescr, String parentTagId, int... expectedStatus)
            throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_CREATE);
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        form.addParameter("tag", tagId);
        form.addParameter("jcr:title", tagTitle);
        if (tagDescr != null) {
            form.addParameter("jcr:description", tagDescr);
        }
        if (parentTagId != null) {
            form.addParameter("parentTagID", parentTagId);
        }

        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Adds a tag
     *
     * @param tagId tag ID. mandatory.
     * @param tagTitle title of the Tag. mandatory.
     * @param locale tag locale
     * @param tagDescr description of the tag. Set null if not used.
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse createTagByTitle(String tagId, String tagTitle, String locale, String tagDescr, int... expectedStatus)
            throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_CREATE_BY_TITLE);
        form.addParameter("tag", tagId);
        if (locale != null) {
            form.addParameter("locale", locale);
        }

        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Removes a tag.
     *
     * @param tagPath path to the tag to delete. E.g. /content/cq:tags/stockphotography/animals/birds/
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  deleteTag(String tagPath, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = createCmdFormEntityBuilder(TAG_COMMAND_DELETE, tagPath);

        // build the request
        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Activates a tag.
     *
     * @param path path of the tag to activate
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  activateTag(String path, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = createCmdFormEntityBuilder(TAG_COMMAND_ACTIVATE, path);
        // build the request
        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Deactivates a tag.
     *
     * @param path path of the tag to deactivate
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  deactivateTag(String path, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_DEACTIVATE);
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        form.addParameter("path", path);

        // build the request
        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Sets a single Tag property.
     *
     * @param tag tag to modify
     * @param propName name of the property to be edited
     * @param propValue value to be set
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return a {@link SlingHttpResponse } wrapping the HTML response returned by Sling
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse  setTagProperty(Tag tag, String propName, String propValue, int... expectedStatus) throws ClientException {
        // start a new list
        ArrayList<String[]> list = new ArrayList<>();
        // add the single prop to change
        list.add(new String[]{propName, propValue});
        // call the generic command
        return setTagProperties(tag, list, expectedStatus);
    }


    /**
     * Sets multiple tag properties.
     *
     * @param tag Tag to be edited
     * @param props Array of name/value string pairs to be set
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return a {@link SlingHttpResponse } wrapping the HTML response returned by Sling
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse  setTagProperties(Tag tag, List<String[]> props, int... expectedStatus) throws ClientException {
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        // set properties
        for (String[] prop : props) {
            formEntry.addParameter(prop[0], prop[1]);
        }

        String tagPath = tag.getPath().substring(0, tag.getPath().length() - 1);

        // send the request
        return doPost(tagPath, formEntry.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Gets list of pages referring to a tag.
     *
     * @param tag Tag to be edited
     * @return a {@link SlingHttpResponse } wrapping the HTML response returned by Sling
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse  getRefList2(Tag tag) throws ClientException {
        return getRefList(tag);
    }


    /**
     * Gets list of pages referring to a tag.
     *
     * @param tag Tag to be edited
     * @param expectedStatus list of expected http status codes
     * @return a {@link SlingHttpResponse} wrapping the HTML response returned by Sling
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse  getRefList(Tag tag, int... expectedStatus) throws ClientException {
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        formEntry.addParameter("cmd", TAG_COMMAND_LIST);

        String tagPath = tag.getPath().substring(0, tag.getPath().length() - 1);

        formEntry.addParameter("path", tagPath);

        // send the request
        return doPost(TAG_COMMAND_PATH, formEntry.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Gets list of pages referring to a tag path.
     *
     * @param tagPath Tag to be edited
     * @param expectedStatus list of expected http status codes
     * @return a {@link SlingHttpResponse } wrapping the HTML response returned by Sling
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse  listTag(String tagPath, int... expectedStatus) throws ClientException {
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        formEntry.addParameter("cmd", TAG_COMMAND_LIST);

        formEntry.addParameter("path", tagPath);

        // send the request
        return doPost(TAG_COMMAND_PATH, formEntry.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Gets tag details.
     *
     * @param tagPath Path of the tag
     * @param expectedStatus list of expected http status codes
     * @return the http response containing the json string
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse getTag(String tagPath, int... expectedStatus) throws ClientException {
        return doGet(tagPath + TAG_RESOURCE_PATH + ".json",
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Gets list of set tags for the given node.
     *
     * @param nodePath Path of the node
     * @param expectedStatus list of expected http status codes
     * @return the list of tags as a JsonNode
     * @throws ClientException if something fails during the request/response cycle
     */
    public JsonNode getTags(String nodePath, int... expectedStatus) throws ClientException {
        JsonNode assetNode = doGetJson(nodePath, -1);
        return assetNode.get("cq:tags");
    }


    /**
     * Gets list of tags for the given node.
     *
     * @param resourcePath node path
     * @param titleSuggestion title suggestion
     * @param nameSuggestion name suggestion
     * @param ignoreCase true to ignore case
     * @param matchWordStart whether to match word start
     * @param locale tags locale
     * @param count count
     * @param expectedStatus list of expected http status codes
     * @return the list of tags as a JsonNode
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse listTags(String resourcePath, String titleSuggestion, String nameSuggestion, boolean ignoreCase,
                                       boolean matchWordStart, String locale, Boolean count, int... expectedStatus)
            throws ClientException {
        URLParameterBuilder params = URLParameterBuilder.create();
        params.add(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);

        if (count != null) {
            params.add("count", count.toString());
        }

        if (locale != null) {
            params.add("locale", locale);
        }

        if (nameSuggestion != null) {
            params.add("suggestByName", nameSuggestion);
        }

        if (titleSuggestion != null) {
            params.add("suggestByTitle", titleSuggestion);
        }

        if (!ignoreCase) {
            params.add("ignoreCase", "false");
        }

        if (matchWordStart) {
            params.add("matchWordStart", "true");
        }

        // build the request
        return doGet(resourcePath + ".tags.json", params.getList(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Sets a tag on a page
     *
     * @param page the page that will doGet the tag
     * @param tagHandle The tag handle e.g. itnamespace:testid/testlevel2. NOTE: to add a tag you have to prefix it with
     * +.
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 201 (CREATED) is
     * assumed.
     * @throws ClientException If something fails during request/response cycle
     */
    public void setTag(String page, String tagHandle, int... expectedStatus) throws ClientException {
        ArrayList<String> list = new ArrayList<>();
        list.add(tagHandle);
        setTags(page, list, expectedStatus);
    }


    /**
     * Sets multiple tag on a page.
     *
     * @param page the page that will doGet the tags
     * @param tags list of tags e.g. itnamespace:testid/testlevel2. NOTE: to doGet a tag you have to prefix it with +,
     * otherwise with -.
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 201 (CREATED) is
     * assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse setTags(String page, List<String> tags, int... expectedStatus) throws ClientException {

        List<NameValuePair> props = new ArrayList<>();

        // add all the tags to the list
        for (String tag : tags) {
            props.add(new BasicNameValuePair("./cq:tags", tag));
        }

        // add additional tags required
        props.add(new BasicNameValuePair("./cq:tags@TypeHint", "String[]"));
        props.add(new BasicNameValuePair("./cq:tags@Patch", "true"));

        // set the tags on a page
        return setPageProperties(page, props, expectedStatus);
    }


    /**
     * Moves a tag.
     *
     * @param from tag id or path to move
     * @param to target tag id or path
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  moveTag(String from, String to, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = createCmdFormEntityBuilder(TAG_COMMAND_MOVE, from);
        form.addParameter("dest", to);

        // build the request
        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    /**
     * Merge a tag.
     *
     * @param path tag id or path to merge
     * @param destPath target tag id or path
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set, http status 20O (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse  mergeTag(String path, String destPath, int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_MERGE);
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        form.addParameter("path", path);
        form.addParameter("dest", destPath);

        // build the request
        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    public SlingHttpResponse  searchTags(Boolean count, String locale, String query, int... expectedStatus) throws ClientException {
        URLParameterBuilder params = URLParameterBuilder.create();
        params.add(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        params.add("count", count.toString());
        params.add("locale", locale);
        params.add("suggestByTitle", query);

        // build the request
        return doGet("/content/cq:tags.tags.json", params.getList(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }


    public SlingHttpResponse  setTagGarbageCollectorCronExpression(String cronExpression, int... expectedStatus) throws ClientException {
        if (cronExpression == null) {

            this.deletePath(TAG_GARBAGE_COLLECTOR_CONFIG_PATH);

            return null;
        } else {

            HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("apply", "true")
                .addTextBody("action", "ajaxConfigManager")
                .addTextBody("propertylist", TAG_GARBAGE_COLLECTOR_CRON_EXPRESSION_PROP)
                .addTextBody(TAG_GARBAGE_COLLECTOR_CRON_EXPRESSION_PROP, cronExpression)
                .build();

            // build the request
            return doPost(TAG_GARBAGE_COLLECTOR_CONFIG_PATH, entity, expectedStatus);
        }
    }


    public JsonNode getTagGarbageCollectorConfig(int... expectedStatus) throws ClientException {
        List<Header> headers = Collections.<Header>singletonList(new BasicHeader("Accept", "application/json"));

        // build the request
        SlingHttpResponse exec = doPost(TAG_GARBAGE_COLLECTOR_CONFIG_PATH, null, headers, expectedStatus);

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }


    public SlingHttpResponse  runGc(int... expectedStatus) throws ClientException {
        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        form.addParameter("cmd", TAG_COMMAND_RUN_GC);

        return doPost(TAG_COMMAND_PATH, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    private FormEntityBuilder createCmdFormEntityBuilder(final String command, final String path) {
        return FormEntityBuilder.create()
                .addParameter("cmd", command)
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("path", path);
    }
}
