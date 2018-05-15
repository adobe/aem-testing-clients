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
import org.apache.sling.testing.clients.Constants;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.indexing.IndexingClient;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;

import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Object which handles group actions
 */
public class Group extends AbstractAuthorizable {

    public static final String ROOT_PATH = "/home/groups";
    protected GroupProfile profile;

    // FIXME move reading to a method loadGroup() and make constructor private
    public <T extends SecurityClient> Group(T client, String authorizableId) throws ClientException, InterruptedException {
        super(client, authorizableId);
    }

    public String getRootPath() {
        return ROOT_PATH;
    }

    /**
     * Get user profile
     *
     * @return {@link GroupProfile}
     * @throws ClientException if the request failed
     */
    public GroupProfile getGroupProfile() throws ClientException {
        if (profile == null) {
            initProfile();
        }
        return profile;
    }

    protected void initProfile() throws ClientException {
        // initialize profile
        this.profile = new GroupProfile(this);
    }

    /**
     * Adds the group as member to other authorizables.
     *
     * @param authorizables  array of any {@link Authorizable}
     * @param expectedStatus array of allowed HTTP Status to be returned.
     * @return Sling response
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public SlingHttpResponse addMembers(Authorizable[] authorizables, int... expectedStatus) throws ClientException {
        if (authorizables == null) {
            throw new IllegalArgumentException("authorizables may not be null!");
        }
        // add user to group - base = group
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        for (Authorizable authorizable : authorizables) {
            formEntry.addParameter(Authorizable.PARAM_ADD_MEMBERS, encodeURI(authorizable.getId()));
        }

        // send the request
        return doPost(formEntry, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Adds the group as member to another authorizable.
     *
     * @param authorizable  any {@link Authorizable}
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return Sling response
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public SlingHttpResponse addMember(Authorizable authorizable, int... expectedStatus) throws ClientException {
        Authorizable[] authorizables = {authorizable};
        return addMembers(authorizables, expectedStatus);
    }



    /**
     * Remove the group as member from other authorizables
     *
     * @param authorizables  list of any {@link Authorizable} object
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @return Sling response
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public SlingHttpResponse removeMembers(Authorizable[] authorizables, int... expectedStatus)
            throws ClientException {
        if (authorizables == null) {
            throw new IllegalArgumentException("List of authorizables may not be null!");
        }
        // add user to group - base = group
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        for (Authorizable authorizable : authorizables) {
            formEntry.addParameter(Authorizable.PARAM_REMOVE_MEMBERS, encodeURI(authorizable.getId()));
        }

        // send the request
        return doPost(formEntry, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Check if authorizable is member of a given group
     *
     * @param authorizable any {@link Authorizable} object
     * @return true if authorizable is member of a given group
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public boolean hasGroupMember(Authorizable authorizable) throws ClientException, InterruptedException {
        if (authorizable == null) {
            throw new IllegalArgumentException("Authorizable may not be null!");
        }

        return getMembers().containsKey(authorizable.getId());
    }

    /**
     * Creates a new group.
     *
     * @param client           any class implementing the {@link SecurityClient}.
     * @param groupId          the group ID for the new group.
     * @param intermediatePath the root path user will be created.
     * @param givenName        the name of the group.
     * @param aboutMe          description of the group.
     * @param expectedStatus   list of allowed HTTP Status to be returned. If not set,
     *                         http status 201 (CREATED) is assumed.
     * @param <T>              client type
     * @return {@code Group}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public static <T extends SecurityClient> Group createGroup(T client, String groupId, String intermediatePath,
                                                               String givenName, String aboutMe,
                                                               int... expectedStatus) throws ClientException, InterruptedException {
        if (client == null || groupId == null) {
            throw new IllegalArgumentException("Client and groupId may not be null!");
        }

        FormEntityBuilder feb = FormEntityBuilder.create()
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter(Authorizable.PARAM_CREATE_GROUP, "" + Authorizable.ACTION_CREATE_GROUP)
                .addParameter(Authorizable.AUTHORIZABLE_ID, groupId);

        if (intermediatePath != null) {
            feb.addParameter(Authorizable.PARAM_INTERMEDIATE_PATH, intermediatePath);
        }

        if (givenName != null) {
            feb.addParameter("./profile/givenName", givenName);
        }

        if (aboutMe != null) {
            feb.addParameter("./profile/aboutMe", aboutMe);
        }

        client.getManager().doPost(feb, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));


        try {
            client.adaptTo(IndexingClient.class).waitForAsyncIndexing();
        } catch (TimeoutException e) {
            throw new ClientException("Indexing did not finish in time", e);
        }

        return new Group(client, groupId);
    }
}
