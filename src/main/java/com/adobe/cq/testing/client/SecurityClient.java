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

import com.adobe.cq.testing.client.security.Authorizable;
import com.adobe.cq.testing.client.security.AuthorizableManager;
import com.adobe.cq.testing.client.security.Group;
import com.adobe.cq.testing.client.security.User;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;

/**
 * Client for managing authorizables, such as users or groups
 */
public class SecurityClient extends CQClient {

    protected static final String LOGIN_URL = "/libs/granite/core/content/login.html";
    protected static final String ADMIN_URL = "/libs/granite/security/content/admin.html";

    private AuthorizableManager authorizableMgr;

    public SecurityClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
        authorizableMgr = new AuthorizableManager(this);
    }

    public SecurityClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
        authorizableMgr = new AuthorizableManager(this);
    }

    /**
     * Get the login page
     *
     * @return the login url
     */
    public String getLoginUrl() {
        return LOGIN_URL;
    }

    /**
     * URL for Security Admin
     * @return the admin url
     */
    public String getAdminUrl() {
        return ADMIN_URL;
    }

    /**
     * Get {@link AuthorizableManager} to get and create {@link
     * Authorizable}s: User / Groups
     *
     * @return {@link AuthorizableManager}
     */
    public AuthorizableManager getManager() {
        return this.authorizableMgr;
    }

    /**
     * Delete a list of authorizables.
     *
     * @param authorizables {@link Authorizable}
     * @param expectedStatus list of allowed HTTP Status to be returned
     * @throws ClientException if the request failed
     *
     */
    public void deleteAuthorizables(Authorizable[] authorizables, int... expectedStatus) throws ClientException {
        if (authorizables == null) return;
        for (Authorizable authorizable : authorizables) {
            authorizable.delete(expectedStatus);
        }
    }

    /**
     * Creates a new user with password the same as userId.
     *
     * @param userId the user ID for the new user.
     * @param expectedStatus list of allowed HTTP Status to be returned
     * @return {@link User}
     * @throws ClientException if the request failed
     * @throws InterruptedException to mark this method as "waiting"
     */
    public User createUser(String userId, int... expectedStatus) throws ClientException, InterruptedException {
        return createUser(userId, userId, null, null, null, expectedStatus);
    }

    /**
     * Creates a new user with password the same as userId in a defined root path.
     *
     * @param userId           the user ID for the new user.
     * @param intermediatePath the root path user will be created.
     * @param expectedStatus   list of allowed HTTP Status to be returned.
     * @return                 {@link User}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public User createUser(String userId, String intermediatePath, int... expectedStatus) throws ClientException, InterruptedException {
        return createUser(userId, userId, intermediatePath, null, null, expectedStatus);
    }

    /**
     * Creates a new user and assigns him to a list of given {@link Authorizable}s.
     *
     * @param userId         the user ID for the new user.
     * @param password       the password to be assigned.
     * @param assignedGroups groups the user will be assigned to as {@link Group}-Array.
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @param <T>            group type
     * @return {@link User}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public <T extends Group> User createUser(String userId, String password, T[] assignedGroups, int... expectedStatus)
            throws ClientException, InterruptedException {
        return createUser(userId, password, null, null, assignedGroups, expectedStatus);
    }


    /**
     * Creates a new user with a {@link com.adobe.cq.testing.client.security.Profile} and assigns him to a list
     * of given {@link Authorizable}s.
     *
     * @param userId           the user ID for the new user.
     * @param password         the password to be assigned.
     * @param intermediatePath the root path user will be created.
     * @param profileMap       profile properties to be set for the new user.
     * @param assignedGroups   groups the user will be assigned to as {@link Authorizable}-Array.
     * @param expectedStatus   list of allowed HTTP Status to be returned.
     * @param <T>            group type
     * @return {@link User}
     * @throws ClientException
     *          If something fails during request/ response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public <T extends Group> User createUser(String userId, String password, String intermediatePath,
                                             Map<String, String> profileMap,
                                             T[] assignedGroups, int... expectedStatus) throws
            ClientException, InterruptedException {
        User user = User.createUser(this, userId, password, intermediatePath, profileMap, expectedStatus);
        if (assignedGroups != null) {
            for (T assignedGroup : assignedGroups) {
                assignedGroup.addMembers(new Authorizable[]{user}, 200);
            }
        }
        return user;
    }

    /**
     * Creates a new user with a {@link com.adobe.cq.testing.client.security.Profile} and assigns him to a list
     * of given {@link Authorizable}s.
     *
     * @param userId           the user ID for the new user.
     * @param password         the password to be assigned.
     * @param intermediatePath the root path user will be created.
     * @param profileMap       profile properties to be set for the new user.
     * @param waitForIndexing  whether or not to wait for indexing to be completed after creating the user
     * @param assignedGroups   groups the user will be assigned to as {@link Authorizable}-Array.
     * @param expectedStatus   list of allowed HTTP Status to be returned.
     * @param <T>            group type
     * @return {@link User}
     * @throws ClientException
     *          If something fails during request/ response cycle
     * @throws InterruptedException to mark this method as "waiting"
     *
     * @deprecated waitForIndexing is not used anymore and it's ignored. Use {@link #createGroup(String, String, String, String, Group[], int...)}.
     */
    @Deprecated
    public <T extends Group> User createUser(String userId, String password, String intermediatePath,
                                             Map<String, String> profileMap,
                                             boolean waitForIndexing,
                                             T[] assignedGroups, int... expectedStatus) throws
            ClientException, InterruptedException {
        return createUser(userId, password, intermediatePath, profileMap, assignedGroups, expectedStatus);
    }

    /**
     * Creates a new group.
     *
     * @param groupId        the group ID for the new group.
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return {@link Group}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public Group createGroup(String groupId, int... expectedStatus) throws ClientException, InterruptedException {
        return createGroup(groupId, null, null, null, null, expectedStatus);
    }

    /**
     * Creates a new group.
     *
     * @param groupId          the group ID for the new group.
     * @param intermediatePath the root path user will be created.
     * @param expectedStatus   list of allowed HTTP Status to be returned.
     * @return {@link Group}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public Group createGroup(String groupId, String intermediatePath, int... expectedStatus) throws ClientException, InterruptedException {
        return createGroup(groupId, intermediatePath, groupId, null, null, expectedStatus);
    }

    /**
     * Creates a new group and assigns him to a list of given {@link Authorizable}s.
     *
     * @param groupId        the group ID for the new group.
     * @param assignedGroups groups the user will be assigned to as {@link Authorizable}-Array.
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return {@link Group}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public Group createGroup(String groupId, Group[] assignedGroups, int... expectedStatus) throws ClientException, InterruptedException {
        return createGroup(groupId, null, null, null, assignedGroups, expectedStatus);
    }


    /**
     * Creates a new group and assigns him to a list of given {@link Authorizable}s.
     *
     * @param groupId          the group ID for the new group.
     * @param intermediatePath the root path user will be created.
     * @param givenName        the name of the group.
     * @param aboutMe          description of the group.
     * @param assignedGroups   groups the user will be assigned to
     * @param expectedStatus   list of allowed HTTP Status to be returned.
     * @return {@link Group}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public Group createGroup(String groupId, String intermediatePath, String givenName, String aboutMe, Group[] assignedGroups,
                             int... expectedStatus) throws ClientException, InterruptedException {
        Group group = Group.createGroup(this, groupId, intermediatePath, givenName, aboutMe, expectedStatus);
        if (null != assignedGroups) {
            group.addMembers(assignedGroups, 200);
        }
        return group;
    }

    /**
     * Get the login token cookie to authenticate future requests.
     *
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 302 (REDIRECT (to the requested resource)) is assumed.
     * @return the login-token cookie to be used for further requests.
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public String getLoginTokenCookie(int... expectedStatus) throws ClientException {
        String postPath = getLoginUrl() + "/j_security_check";

        HttpEntity feb = FormEntityBuilder.create()
                .addParameter("j_username", getUser())
                .addParameter("j_password", getPassword())
                .addParameter("resource", "/")
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("contextPath", "")
                .build();

        // additional headers
        List<Header> headers = Arrays.<Header>asList(
                // set header to state that is a form submit
                new BasicHeader("Content-Type", "application/x-www-form-urlencoded"),
                // set Accept header to accept HTML
                new BasicHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"));

        SlingHttpResponse response = doPost(postPath, feb, headers, HttpUtils.getExpectedStatus(SC_MOVED_TEMPORARILY, expectedStatus));

        // get the cookie string
        String cookie = response.getHeaders("Set-Cookie")[0].getValue();
        cookie = cookie.substring(0, cookie.indexOf(";"));
        return cookie;
    }
}
