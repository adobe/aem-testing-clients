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
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.codehaus.jackson.JsonNode;

import java.util.Map;

/**
 * Interface which defines an authorizable: user / group
 */
public interface Authorizable {

    /**
     * JSON Schema from authorizable (minimum)
     */
    public static final String SCHEMA_AUTHORIZABLE = "/schemas/json/authorizable-schema.json";

    /**
     * Selector for AuthorizableServlet
     */
    public static final String SELECTOR = ".rw";

    /**
     * Selector for UserProperties
     */
    public static final String SELECTOR_USERPROPERTIES = ".userproperties";

    /**
     * Parameter indicating that the corresponding authorizable should be removed.
     */
    public static final String PARAM_DELETE = "deleteAuthorizable";

    /**
     * Parameter marking the request being used for creating a new user.
     */
    public static final String PARAM_CREATE_USER = "createUser";

    /**
     * Parameter marking the request being used for creating a new group.
     */
    public static final String PARAM_CREATE_GROUP = "createGroup";

    public static final int ACTION_CREATE_USER = 1;
    public static final int ACTION_CREATE_GROUP = 2;

    public static final String PARAM_AUTHORIZABLE_ID = "authorizableId";
    public static final String PARAM_PASSWORD = "rep:password";
    public static final String PARAM_INTERMEDIATE_PATH = "intermediatePath";
    public static final String PARAM_ADD_IMPERSONATORS = "addImpersonators";
    public static final String PARAM_REMOVE_IMPERSONATORS = "removeImpersonators";
    public static final String PARAM_ADD_MEMBERS = "addMembers";
    public static final String PARAM_REMOVE_MEMBERS = "removeMembers";

    /**
     * JSON properties
     */
    public static final String AUTHORIZABLE_ID = "authorizableId";
    public static final String HOME = "home";
    public static final String IS_IMPERSONATED = "isImpersonated";
    public static final String MEMBERS = "members";
    public static final String MEMBER_OF = "memberOf";
    public static final String PROFILE = "profile";
    public static final String IMPERSONATORS = "impersonators";
    public static final String TYPE = "type";
    public static final String TYPE_GROUP = "group";
    public static final String TYPE_USER = "user";
    public static final String WILDCARD = "*";

    /**
     * Get any client implementing the {@link SecurityClient}
     *
     * @return {@link SecurityClient}
     */
    public SecurityClient getClient();

    /**
     * Get id of authorizable
     *
     * @return authorizableId
     */
    public String getId();

    /**
     * Get home path to authorizable
     *
     * @return path to authorizable
     */
    public String getHomePath();

    /**
     * Get home URL to authorizable
     *
     * @return URL to authorizable
     */
    public String getHomeUrl();

    /**
     * Get root path of the authorizable (user/group) in repository
     *
     * @return root path
     */
    public String getRootPath();

    /**
     * Check if the authorizable exists
     *
     * @return true if the authorizable exists
     * @throws ClientException if the request failed
     * @throws InterruptedException if interrupted
     */
    public boolean exists() throws ClientException, InterruptedException;

    /**
     * String representation of authorizable
     * http://localhost:4502/home/groups/default/administrators.rw.json?props=replication,modification,memberOf,
     * membersTotal,members,profile/*&amp;ml=2000
     *
     * @param propsFilter    properties filter
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return the string representing a json
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public String getJsonAsString(String propsFilter, int... expectedStatus) throws ClientException;

    /**
     * Get user properties JSON representation as string
     * @param expectedStatus status to be
     * @return user properties JSON representation as String
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public String getUserPropertiesJsonAsString(int... expectedStatus) throws ClientException;

        /**
        * Deletes an authorizable.
        *
        * @param expectedStatus list of allowed HTTP Status to be returned.
        * @return Sling response
        * @throws ClientException
        *          If something fails during request/response cycle
        */
    public SlingHttpResponse delete(int... expectedStatus) throws ClientException;

    /**
     * Creates a new {@code Authorizable}.
     *
     * @param client            any class implementing the {@link SecurityClient}.
     * @param authorizableClass Authorizable class to be created.
     * @param authorizableId    the ID for the new authorizable.
     * @param formParameters    form parameters.
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 201 (CREATED) is assumed.
     * @param <T>               client type
     * @return created {@code Authorizable}
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public <T extends SecurityClient> Authorizable create(T client,
                                                          Class<? extends AbstractAuthorizable> authorizableClass,
                                                          String authorizableId,
                                                          FormEntityBuilder formParameters,
                                                          int... expectedStatus) throws ClientException;

    /**
     * Get members authorizable is assigned to.
     *
     * @return list of {@code Authorizable}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     *
     */
    public Map<String, Authorizable> getMemberOf() throws ClientException, InterruptedException;

    /**
     * Get members assigned to the authorizable.
     *
     * @return list of {@code Authorizable}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     *
     */
    public Map<String, Authorizable> getMembers() throws ClientException, InterruptedException;

    /**
     * Check if authorizable is impersonated.
     *
     * @return true if authorizable is impersonated.
     * @throws ClientException
     *          If something fails during request/response cycle
     *
     */
    public boolean isImpersonated() throws ClientException;

    /**
     * Get impersonators of the authorizable.
     *
     * @return list of {@code Authorizable}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     *
     */
    public Map<String, Authorizable> getImpersonators() throws ClientException, InterruptedException;

    /**
     * Get profile attributes for authorizable.
     *
     * @return JsonNode containing profile properties
     * @throws ClientException
     *          If something fails during request/response cycle
     *
     */
    public JsonNode getProfile() throws ClientException;
}
