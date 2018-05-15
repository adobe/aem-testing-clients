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

import org.apache.http.NameValuePair;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.SlingParameter;
import org.codehaus.jackson.JsonNode;

import java.net.URI;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * The base client for all community related tests. It provides a core set of commonly used community functions e.g. comments <br>
 * <br>
 * It extends from {@link CQClient} which in turn provides a core set of commonly used website and page functionality.
 */
public class CommunityClient extends CQClient {

    public CommunityClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public CommunityClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Adds a Comment component to a page.
     *
     * @param pagePath       path to the page where the comment component will be created.
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 201 (CREATED) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse addCommentsComponent(String pagePath, int... expectedStatus) throws ClientException {
        final String postPath = pagePath + "/jcr:content/par/";

        FormEntityBuilder feb = FormEntityBuilder.create();
        feb.addParameter("./sling:resourceType", "social/commons/components/comments");
        feb.addParameter(":nameHint", "comments");
        feb.addParameter(":order", "last");

        return doPost(postPath, feb.build(), HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
    }

    /**
     * Configures the Comment component.
     *
     * @param commentPath    path to the comment component
     * @param defaultMessage the topic for the comments
     * @param isModerated    true if comments are moderated
     * @param allowReplies   true if replies are allowed
     * @param displayAsTree  true if the comments are displayed as tree
     * @param closed         true if topic is closed (no posting of comments possible anymore)
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse configureCommentComponent(String commentPath, String defaultMessage,
                                                       boolean isModerated,
                                                       boolean allowReplies, boolean displayAsTree, boolean closed,
                                                       int... expectedStatus)
            throws ClientException {
        FormEntityBuilder feb = FormEntityBuilder.create();
        for (NameValuePair val : new SlingParameter("./allowRepliesToComments")
                .value(Boolean.valueOf(allowReplies).toString()).delete().typeHint("Boolean").toNameValuePairs())
            feb.addParameter(val.getName(), val.getValue());
        for (NameValuePair val : new SlingParameter("./moderateComments").value(Boolean.valueOf(isModerated).toString())
                .typeHint("Boolean").delete().toNameValuePairs())
            feb.addParameter(val.getName(), val.getValue());
        for (NameValuePair val : new SlingParameter("./displayCommentsAsTree")
                .value(Boolean.valueOf(displayAsTree).toString()).typeHint("Boolean").delete().toNameValuePairs())
            feb.addParameter(val.getName(), val.getValue());
        for (NameValuePair val : new SlingParameter("./closed").value(Boolean.valueOf(closed).toString())
                .typeHint("Boolean").delete().toNameValuePairs())
            feb.addParameter(val.getName(), val.getValue());
        for (NameValuePair val : new SlingParameter("./defaultMessage").value(defaultMessage).toNameValuePairs())
            feb.addParameter(val.getName(), val.getValue());

        return doPost(commentPath, feb.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Moderate comment: approve \ deny
     *
     * @param commentPath    the path to the comment
     * @param approve        true if comment should be approved, false if comment should be denied
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse moderateComment(String commentPath, boolean approve, int... expectedStatus) throws ClientException {
        return wcmCommands.moderateComment(commentPath, approve, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Mark comment as spam
     *
     * @param commentPath    the path to the comment
     * @param isSpam         true if comment should be marked as spam, false if not
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse markCommentAsSpam(String commentPath, boolean isSpam, int... expectedStatus) throws ClientException {
        return wcmCommands.markCommentAsSpam(commentPath, isSpam, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Edit the comment: approve | deny | markCommentAsSpam | deleteComment
     *
     * @param command        available commands: approve | deny | markCommentAsSpam | deleteComment
     * @param commentPath    the path to the comment
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse editComment(String command, String commentPath, int... expectedStatus) throws ClientException {
        switch (command) {
            case "approve":
                return wcmCommands.moderateComment(commentPath, true, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
            case "deny":
                return wcmCommands.moderateComment(commentPath, false, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
            case "markCommentAsSpam":
                return wcmCommands.markCommentAsSpam(commentPath, true, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
            case "deleteComment":
                return wcmCommands.deleteComment(commentPath, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        }
        return null;
    }

    /**
     * Adds a single comment to a Comments component.
     *
     * @param commentPath    path to the comment component
     * @param comment        the comment text
     * @param user           the user writing the comment
     * @param url            the users url
     * @param email          the users email
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return a {@link org.codehaus.jackson.JsonNode} mapping to the requested content node.
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode createComment(String commentPath, String comment, String user, String url,
                                  String email,
                                  int... expectedStatus)
            throws ClientException {
        String postPath = commentPath + ".social.createcomment.json";

        SlingHttpResponse exec = doPost(postPath, FormEntityBuilder.create()
                .addParameter("email", email)
                .addParameter("id", "nobot")
                .addParameter("jcr:description", comment)
                .addParameter("url", url)
                .addParameter("userIdentifier", user)
                .addParameter(":templatename","comment")
                .build(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }

    /**
     * List all comments in the {@code user generated content} space,
     * currently under {@code /content/usergenerated}.
     *
     * @param ugcBasePath The base path to start listing
     * @param limit       limits the number of nodes below ugcBasePath to be returned
     * @param start       defines the start node below ugcBasePath
     * @param predicate   Predicate used to filter hierarchy nodes in the siteadmin e.g. siteadmin
     * @param view        filter: null | approved | denied | spam | notspam
     * @return a {@link org.codehaus.jackson.JsonNode} mapping to the requested content node.
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getComments(String ugcBasePath, int limit, int start, String predicate,
                                String view) throws ClientException {
        return adaptTo(JsonClient.class).getUserGeneratedPages(ugcBasePath, limit, start, predicate, view);
    }

    /**
     * Delete comment
     * @param commentPath The page where the comments are
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse deleteComment(String commentPath, int...expectedStatus) throws ClientException {
        String postPath = "/bin/wcmcommand";
        return doPost(postPath, FormEntityBuilder.create()
                .addParameter("cmd", "deleteComment")
                .addParameter("path", commentPath)
                .build(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
}
