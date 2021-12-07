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
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Manager for getting and creating {@link Authorizable}s
 */
public class AuthorizableManager {

    /**
     * POST path for Authorizable
     */
    public static final String AUTHORIZABLE_POST_PATH = "/libs/granite/security/post/authorizables";

    /**
     * Path to JSON representing the authorizables
     */
    public static final String AUTHORIZABLES_PATH = "/libs/granite/security/search/authorizables";
    public static final String AUTHORIZABLES = "authorizables";

    /**
     * The security granite HTTP client
     */
    protected final SecurityClient client;

    public <T extends SecurityClient> AuthorizableManager(T client) {
        this.client = client;
    }

    /**
     * Get built-in group "administrators"
     *
     * @return administrators' {@link Group}
     * @throws ClientException if the request failed
     */
    public Group getGroupAdministrators() throws ClientException {
        return getGroup("administrators");
    }

    /**
     * Get built-in group "contributor"
     *
     * @return contributor' {@link Group}
     * @throws ClientException if the request failed
     */
    public Group getGroupContributor() throws ClientException {
        return getGroup("contributor");
    }

    /**
     * Get built-in group "everyone"
     *
     * @return everyone' {@link Group}
     * @throws ClientException if the request failed
     */
    public Group getGroupEveryone() throws ClientException {
        return getGroup("everyone");
    }

    /**
     * Get built-in group "user-administrators"
     *
     * @return user-administrators' {@link Group}
     * @throws ClientException if the request failed
     */
    public Group getGroupUserAdministrators() throws ClientException {
        return getGroup("user-administrators");
    }

    /**
     * Get an existing user object from the id.
     *
     * @param userId the user ID.
     * @return {@link User}
     * @throws ClientException If something fails during request/response cycle
     */
    public User getUser(String userId) throws ClientException {
        return getAuthorizable(User.class, userId);
    }

    /**
     * Get an existing group object from the id.
     *
     * @param groupId the group ID.
     * @return {@link Group}
     * @throws ClientException If something fails during request/response cycle
     */
    public Group getGroup(String groupId) throws ClientException {
        return getAuthorizable(Group.class, groupId);
    }

    /**
     * Creates a new {@link Authorizable}.
     *
     * @param client            any class implementing the {@link SecurityClient}.
     * @param authorizableClass Authorizable class to be created.
     * @param authorizableId    the ID for the new authorizable.
     * @param formParameters    form parameters.
     * @param expectedStatus    list of allowed HTTP Status to be returned. If not set,
     *                          http status 201 (CREATED) is assumed.
     * @param <T>               client type
     * @return created {@link Authorizable}
     * @throws ClientException If something fails during request/response cycle
     */
    public <T extends SecurityClient> Authorizable create(T client, Class<? extends AbstractAuthorizable> authorizableClass,
                                                          String authorizableId, FormEntityBuilder formParameters,
                                                          int... expectedStatus)
            throws ClientException {
        if (formParameters == null) {
            throw new IllegalArgumentException("Parameters for creating an authorizable may not be null!");
        }
        client.doPost(AUTHORIZABLES_PATH, formParameters.build(),
                HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));

        return getAuthorizable(authorizableClass, authorizableId);
    }

    /**
     * Get an instance of any class extending {@link AbstractAuthorizable}
     * with given ID. Home path for authorizable is assumed in default structure.
     * <p>
     *
     * @param authorizableClass any class extending the {@link AbstractAuthorizable}
     * @param authorizableId    the authorizable ID.
     * @param <T>               any class extending {@link AbstractAuthorizable}
     * @return the client
     * @throws ClientException if client can't be instantiated
     */
    protected <T extends AbstractAuthorizable> T getAuthorizable(Class<T> authorizableClass, String authorizableId)
            throws ClientException {
        T authorizable;
        try {
            Constructor<T> cons = authorizableClass.getConstructor(SecurityClient.class, String.class);
            authorizable = cons.newInstance(client, authorizableId);
        } catch (Exception e) {
            throw new ClientException("Could not initialize Authorizable: '"
                    + authorizableClass.getCanonicalName() + "'.", e);
        }
        return authorizable;
    }

    /**
     * Get all authorizables from Json as list of {@link Authorizable}s.
     *
     * @param query search query for authorizables
     * @return authorizables as {@link Authorizable}
     * @throws ClientException if the request failed
     */
    public List<Authorizable> getAuthorizables(String query) throws ClientException {
        JsonNode json = JsonUtils.getJsonNodeFromString(getAuthorizablesJson(query));

        JsonNode authorizablesNode = json.get(AUTHORIZABLES);
        ArrayList<Authorizable> authorizables = new ArrayList<>();
        if (authorizablesNode.isArray()) {
            for (JsonNode authorizableNode : authorizablesNode) {
                // assert json is valid
                // FIXME find solution to validate schema
                //GraniteAssert.assertSchemaValid(authorizableNode.toString(), Authorizable.SCHEMA_AUTHORIZABLE);
                // add authorizable
                String authorizableId = authorizableNode.get(Authorizable.AUTHORIZABLE_ID).asText();
                String type = authorizableNode.get(Authorizable.TYPE).asText();
                Authorizable authorizable = getAuthorizable(
                        AbstractAuthorizable.getAuthorizableClass(type), authorizableId);
                authorizables.add(authorizable);
            }
        }
        return authorizables;
    }

    /**
     * Get all authorizables as json String.
     *
     * @param query search query for authorizables
     * @return authorizables as JSON String
     * @throws ClientException if the request failed
     */
    public String getAuthorizablesJson(String query) throws ClientException {
        // do search
        URLParameterBuilder params = URLParameterBuilder.create()
                .add("offset", "0")
                .add("query", "{" + ((query == null) ? "" : query) + "}");

        return client.doGet(AUTHORIZABLES_PATH + ".json", params.getList(), SC_OK).getContent();
    }

    /**
     * POST request to AuthorizableServlet.
     *
     * @param formParameters form parameters.
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return executed request
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse doPost(FormEntityBuilder formParameters, int... expectedStatus) throws ClientException {
        return client.doPost(AUTHORIZABLE_POST_PATH + ".html", formParameters.build(), expectedStatus);
    }
}
