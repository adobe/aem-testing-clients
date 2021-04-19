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
package com.adobe.cq.testing.client.security;

import com.adobe.cq.testing.client.SecurityClient;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.Security;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Existing {@link Authorizable}
 */
public abstract class AbstractAuthorizable implements Authorizable {

    /**
     * Servlet providing JSON description of authorizables, and key-names within the JSON.
     */
    private static final String AUTHORIZABLES = "authorizables";
    private static final String HOME = "home";
    private static final int TIMEOUT = 60000; // in milliseconds
    private static final int DELAY = 500;  // in milliseconds

    protected SecurityClient client;
    protected String authorizableId;
    protected String authorizablePath;
    private String authorizableUrl;

    /** Logger for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractAuthorizable.class);

    /**
     * Constructor for an {@link Authorizable},
     * default path to authorizable is expected
     *
     * @param client         any class implementing the {@link SecurityClient}.
     * @param authorizableId the authorizable ID.
     * @param <T>            any class implementing teh {@link SecurityClient}.
     *
     * @throws ClientException if the details of the authorizables cannot be retrieved
     * @throws InterruptedException to mark this method as "waiting"
     */
    // TODO: Refactor all of this!
    public <T extends SecurityClient> AbstractAuthorizable(T client, String authorizableId) throws ClientException, InterruptedException {
        if (client == null) {
            throw new IllegalArgumentException("Client must not be null!");
        } else if (authorizableId == null) {
            throw new IllegalArgumentException("AuthorizableId must not be null!");
        }
        this.client = client;
        this.authorizableId = authorizableId;
        this.authorizablePath = getAuthorizablePath(authorizableId);
        if (this.authorizablePath == null) {
            throw new ClientException("Failed to retrieve authorizable path for " + authorizableId);
        }
        this.authorizableUrl = encodePathToURL(this.authorizablePath);
    }

    public SecurityClient getClient() {
        return client;
    }

    public String getId() {
        return authorizableId;
    }

    public String getHomePath() {
        return authorizablePath;
    }

    public String getHomeUrl() {
        return authorizableUrl;
    }

    public String getJsonAsString(String propsFilter, int... expectedStatus) throws ClientException {
        List<NameValuePair> params = Collections.<NameValuePair>singletonList(
                new BasicNameValuePair("props", (propsFilter != null) ? propsFilter : Authorizable.WILDCARD));
        return doGet(params, expectedStatus).getContent();
    }

    public String getUserPropertiesJsonAsString(int... expectedStatus) throws ClientException {
        return client.doGet(getHomePath() + SELECTOR_USERPROPERTIES + ".json", expectedStatus).getContent();
    }

    public static boolean exists(SecurityClient client, String authorizableId) throws ClientException {
        JsonNode authorizables = getAuthorizables(client, getQuery(authorizableId));
        return authorizables != null && authorizables.size() != 0;
    }

    public boolean exists() throws ClientException {
        return exists(this.client, this.authorizableId);
    }

    public boolean exists(String query) throws ClientException {
        JsonNode authorizables = getAuthorizables(query);
        return authorizables != null && authorizables.size() != 0;
    }

    public SlingHttpResponse delete(int... expectedStatus) throws ClientException {
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        formEntry.addParameter(PARAM_DELETE, "1");
        return doPost(formEntry, expectedStatus);
    }

    public <T extends SecurityClient> Authorizable create(T client,
                                                          Class<? extends AbstractAuthorizable> authorizableClass,
                                                          String authorizableId,
                                                          FormEntityBuilder formParameters,
                                                          int... expectedStatus) throws ClientException {
        return client.getManager().create(client, authorizableClass, authorizableId, formParameters, expectedStatus);
    }

    public Map<String, Authorizable> getMemberOf() throws ClientException, InterruptedException {
        JsonNode authorizableNode = JsonUtils.getJsonNodeFromString(getJsonAsString(Authorizable
                .MEMBER_OF, SC_OK));
        JsonNode propsNode = null;
        if (authorizableNode != null) {
            propsNode = authorizableNode.get(Authorizable.MEMBER_OF);
        }
        return buildAuthorizableList(propsNode);
    }

    public Map<String, Authorizable> getMembers() throws ClientException, InterruptedException {
        JsonNode authorizableNode = JsonUtils.getJsonNodeFromString(getJsonAsString(Authorizable.MEMBERS, SC_OK));
        JsonNode propsNode = null;
        if (authorizableNode != null) {
            propsNode = authorizableNode.get(Authorizable.MEMBERS);
        }
        return buildAuthorizableList(propsNode);
    }

    public Map<String, Authorizable> getImpersonators() throws ClientException, InterruptedException {
        JsonNode authorizableNode = JsonUtils.getJsonNodeFromString(getJsonAsString(Authorizable.IMPERSONATORS, SC_OK));
        JsonNode propsNode = null;
        if (authorizableNode != null) {
            propsNode = authorizableNode.get(Authorizable.IMPERSONATORS);
        }
        return buildAuthorizableList(propsNode);
    }

    public boolean isImpersonated() throws ClientException {
        JsonNode authorizableNode = JsonUtils.getJsonNodeFromString(getJsonAsString(null, SC_OK));
        JsonNode isImpersonated = authorizableNode.get(Authorizable.IS_IMPERSONATED);
        return (isImpersonated != null) && "true".equals(isImpersonated.getValueAsText());
    }

    public JsonNode getProfile() throws ClientException {
        JsonNode authorizableNode = JsonUtils.getJsonNodeFromString(
                getJsonAsString(Authorizable.PROFILE + "/" + Authorizable.WILDCARD));
        JsonNode propsNode = null;
        if (authorizableNode != null) {
            propsNode = authorizableNode.get(Authorizable.PROFILE);
        }
        return propsNode;
    }

    /**
     * POST request to AuthorizableServlet.
     *
     * @param formParameters form parameters.
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return executed request
     * @throws ClientException if the request failed
     *
     */
    public SlingHttpResponse doPost(FormEntityBuilder formParameters, int... expectedStatus) throws ClientException {
        String homeURL = getHomeUrl();
        return client.doPost(homeURL + SELECTOR + ".html", formParameters.build(), expectedStatus);
    }

    /**
     * GET request to AuthorizableServlet.
     *
     * @param parameters url parameters.
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return executed request
     * @throws ClientException if the request failed
     *
     */
    public SlingHttpResponse doGet(List<NameValuePair> parameters, int... expectedStatus) throws ClientException {
        return client.doGet(getHomeUrl() + SELECTOR + ".json", parameters, expectedStatus);
    }

    /**
     * Get authorizable Class.
     *
     * @param authorizableType Allowed values: "user" or "group" (fallback).
     * @return a Class extending {@code AbstractAuthorizable}
     */
    public static Class<? extends AbstractAuthorizable> getAuthorizableClass(String authorizableType) {
        return Authorizable.TYPE_USER.equals(authorizableType) ? User.class : Group.class;
    }

    /**
     * Fetch the path of an authorizable.
     * @param authorizableId    the ID of the authorizable
     * @return                  the authorizable's repository path
     * @throws ClientException if the request failed
     */
    private String getAuthorizablePath(String authorizableId) throws ClientException, InterruptedException {
        return getAuthorizableNodeWithRetry(authorizableId).path(HOME).getTextValue();
    }

    /**
     * Get authorizable type based on id.
     *
     * @param authorizableId the ID of the authorizable
     * @return type as String: "user" or "group"
     */
    private String getAuthorizableType(String authorizableId) throws ClientException, InterruptedException {
        return getAuthorizableNodeWithRetry(authorizableId).path(Authorizable.TYPE).getTextValue();
    }

    private JsonNode getAutorizablesWithRetry(final String query) throws ClientException, InterruptedException {
        class AuthorizablesPolling extends Polling {
            public JsonNode authorizables;

            @Override
            public Boolean call() throws Exception {
                authorizables = getAuthorizables(query);
                if (authorizables == null) {
                    throw new ClientException("getAuthorizables returned null");
                }

                if (authorizables.size() != 1) {
                    throw new ClientException("getAuthorizables returned " + authorizables.size() + " elements, expected 1");
                }

                return true;
            }
        }

        try {
            AuthorizablesPolling polling  = new AuthorizablesPolling();
            polling.poll(TIMEOUT, DELAY);
            return polling.authorizables;
        } catch (TimeoutException e) {
            throw new ClientException("Failed to retrieve authorizables in " + TIMEOUT + " ms", e);
        }
    }

    private static JsonNode getAuthorizables(SecurityClient client, final String query) throws ClientException {
        final String authorizablesJson = client.getManager().getAuthorizablesJson(query);
        return JsonUtils.getJsonNodeFromString(authorizablesJson).get(AUTHORIZABLES);
    }

    private JsonNode getAuthorizables(final String query) throws ClientException {
        return getAuthorizables(this.client, query);
    }

    /**
     * Get the authorizable json node from the search servlet
     *
     * @param authorizableId it of authorizable node
     * @return authorizable node
     * @throws ClientException if the request failed
     */
    private JsonNode getAuthorizableNodeWithRetry(String authorizableId) throws ClientException, InterruptedException {
        String query = getQuery(authorizableId);

        final JsonNode authorizables = getAutorizablesWithRetry(query);

        if (authorizables == null || authorizables.size() != 1) {
            throw new ClientException("Authorizable " + authorizableId + " not found!");
        }
        return authorizables.get(0);
    }

    public static String getQuery(String authorizableId) {
        return "\"condition\":[{\"named\":\"" + StringEscapeUtils.escapeJson(authorizableId) + "\"}]";
    }

    /**
     * Get home path of the authorizable (user/group) in repository. Either user is created in a user defined structure
     * (intermediatePath is set while creation) or within the default structure
     *
     * @param authorizableId   the ID of the authorizable
     * @param rootPath         the rootPath of the authorizable
     * @param intermediatePath the parent path the user will be created.
     *
     * @return the authorizable path
     * @throws ClientException if the request failed
     *
     */
    public static String buildAuthorizablePath(String authorizableId, String rootPath,
                                               String intermediatePath) throws ClientException {
        boolean buildDefaultPath = (intermediatePath == null || "".equals(intermediatePath));

        String authorizablePath;
        if (buildDefaultPath) {
            // base path
            authorizablePath = rootPath;
            authorizablePath += "/";
            // The first sub folder level is the first user id char as node id,
            // make sure it properly escaped to be a valid JCR Node name
            authorizablePath += escapeIllegalJcrChars(authorizableId.substring(0, 1));
        } else { // intermediatePath is set
            // base path
            authorizablePath = intermediatePath;
        }
        authorizablePath += "/" + escapeIllegalJcrChars(authorizableId);

        return authorizablePath;
    }

    /**
     * Get url of the authorizable (user/group) in repository. Either user is created in a user defined structure
     * (intermediatePath is set while creation) or within the default structure
     * <p>
     * JCR escaping works similar to URL encoding, meaning that special chars doGet %-encoded.
     * so to avoid % being interpreted by the URL as an URL Encoding we must URL encode the % to doGet a valid URL
     *
     * @param authorizableHomePath the home path of the authorizable
     * @return authorizable url
     * @throws ClientException if the request failed
     *
     */
    protected static String encodePathToURL(String authorizableHomePath) throws ClientException {
        String authorizableUrl = "";
        try {
            StringTokenizer st = new StringTokenizer(authorizableHomePath, "/");
            while (st.hasMoreElements()) {
                String nn = (String) st.nextElement();
                // Workaround: The space must be properly %-encoded to work.
                authorizableUrl += "/" + URLEncoder.encode(nn, "UTF-8").replaceAll("\\+", "%20");
            }
        } catch (UnsupportedEncodingException e) {
            throw new ClientException("Error occurred while URL encoding the path to authorizable!", e);
        }

        return authorizableUrl;
    }

    /**
     * Escape path to URI
     * @param unescapedPath unescaped path
     * @return escaped path
     * @throws ClientException if the request failed
     */
    protected static String encodeURI(String unescapedPath) throws ClientException {
        try {
            return new URI(null, null, unescapedPath, null).toASCIIString();
        } catch (URISyntaxException e) {
            throw new ClientException("Could not encode URI: " + unescapedPath);
        }
    }

    /**
     * Build list of {@link Authorizable}s as Map: key = id of
     * authorizable.
     *
     * @param authorizableJsonNode JSON node of an authorizable; TODO; format
     * @return list of {@link Authorizable}
     * @throws ClientException if the request failed
     * @throws InterruptedException to mark this method as "waiting"
     *
     */
    protected Map<String, Authorizable> buildAuthorizableList(JsonNode authorizableJsonNode) throws ClientException, InterruptedException {
        if (authorizableJsonNode == null) throw new IllegalArgumentException("JSON of authorizable may not be null!");

        Map<String, Authorizable> authorizables = new LinkedHashMap<>();

        // multiple results
        if (authorizableJsonNode.isArray()) {
            for (int i = 0; i < authorizableJsonNode.size(); i++) {
                Authorizable authorizable = buildAuthorizable(authorizableJsonNode.get(i));
                authorizables.put(authorizable.getId(), authorizable);
            }
        } else { // one result
            Authorizable authorizable = buildAuthorizable(authorizableJsonNode);
            authorizables.put(authorizable.getId(), authorizable);
        }

        return authorizables;
    }

    /**
     * Build {@link Authorizable} object from JSON
     *
     * @param authorizableNode authorizable as {@link JsonNode}
     * @return {@link Authorizable} object
     * @throws ClientException if the request failed
     * @throws InterruptedException to mark this method as "waiting"
     *
     */
    private Authorizable buildAuthorizable(JsonNode authorizableNode) throws ClientException, InterruptedException {
        // assert valid json
        // FIXME find solution to validate schema
        //GraniteAssert.assertSchemaValid(authorizableNode.toString(), SCHEMA_AUTHORIZABLE);
        // add authorizable
        String authorizableId = authorizableNode.get(Authorizable.AUTHORIZABLE_ID).getValueAsText();
        // check if authorizable is user or group
        String type = getAuthorizableType(authorizableId);
        return client.getManager().getAuthorizable(getAuthorizableClass(type), authorizableId);
    }


    private static String escapeIllegalJcrChars(String name) {
        final String illegal = "%/:[]*|\t\r\n";

        StringBuilder buffer = new StringBuilder(name.length() * 2);
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (illegal.indexOf(ch) != -1
                    || (ch == '.' && name.length() < 3)
                    || (ch == ' ' && (i == 0 || i == name.length() - 1))) {
                buffer.append('%');
                buffer.append(Character.toUpperCase(Character.forDigit(ch / 16, 16)));
                buffer.append(Character.toUpperCase(Character.forDigit(ch % 16, 16)));
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }
}
