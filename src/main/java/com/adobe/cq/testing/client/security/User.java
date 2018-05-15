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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * Object which handles user actions
 */
public class User extends AbstractAuthorizable {
    private static final Logger LOG = LoggerFactory.getLogger(User.class);
    public static final String ROOT_PATH = "/home/users";

    /**
     * <p>
     * How many millisecond to wait before giving up on waiting for an async index to be updated.
     * </p>
     * <p>
     * By default it will wait {@code 6000}ms but it can be fine tuned with, for example
     * {@code -Dgranite.it.maxasync=12000}
     * </p>
     */
    public static final int MAX_ASYNC_WAIT_MILLIS = Integer.getInteger("granite.it.maxasync", 6000);

    protected UserProfile profile;

    public <T extends SecurityClient> User(T client, String authorizableId) throws ClientException, InterruptedException {
        super(client, authorizableId);
    }

    public String getRootPath() {
        return ROOT_PATH;
    }

    /**
     * Adds impersonators to the user.
     *
     * @param authorizables  list of any {@code User} objects
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @param <T> user type
     * @return Sling response
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public <T extends User> SlingHttpResponse addImpersonators(T[] authorizables, int... expectedStatus) throws
            ClientException {
        if (authorizables == null) {
            throw new IllegalArgumentException("List of authorizables may not be null!");
        }
        // add user to group - base = group
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        for (T authorizable : authorizables) {
            formEntry.addParameter(Authorizable.PARAM_ADD_IMPERSONATORS, encodeURI(authorizable.getId()));
        }

        // send the request
        return doPostAndWaitForAsync(formEntry, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * <p>
     * Perform a {@link #doPost(FormEntityBuilder, int...)} and keeps performing it for at most
     * {@link #MAX_ASYNC_WAIT_MILLIS} for as long as it didn't receive the
     * {@code expectedStatus} response from the server.
     * </p>
     *
     * <p>
     * Useful when dealing with post requests that perform queries against asynchronous indexes and
     * therefore should give it the time to update itself before failing.
     * </p>
     *
     * @param entity form to be submitted. Cannot be null.
     * @param expectedStatus the expected HTTP status from the call
     * @return an instance of the response
     * @throws ClientException if the request failed
     */
    public SlingHttpResponse doPostAndWaitForAsync(final FormEntityBuilder entity, final int... expectedStatus)
            throws ClientException {
        checkNotNull(entity);
        LOG.info("entering doPostAndWaitForAsync()");

        SlingHttpResponse exec;
        int totalSleptSoFar = 0;
        int sleepMillis = 100;
        int count = 1;

        do {
            if (count > 1) {
                try {
                    LOG.info(
                        "doPostAndWaitForAsync() - Sleeping for {}. Attempt: {}. Slept so far: {}",
                        sleepMillis, count, totalSleptSoFar);
                    Thread.sleep(sleepMillis);
                } catch (InterruptedException e) {
                    LOG.error("Error while sleeping", e);
                }

                // we wait at most 5000ms with a capped time wait of 500ms for each iteration
                totalSleptSoFar += sleepMillis;
                sleepMillis = Math.min(sleepMillis * 2, 500);
            }

            count++;
            exec = doPost(entity, expectedStatus);
            if (exec == null) {
                LOG.error("doPostAndWaitForAsync() - Null HTTPResponse. Not waiting.");
                return null;
            }
        } while(totalSleptSoFar < MAX_ASYNC_WAIT_MILLIS);

        return exec;
    }

    /**
     * Remove impersonators from the user
     *
     * @param authorizables  list of any {@code User} objects
     * @param expectedStatus list of allowed HTTP Status to be returned.
     * @param <T> user type
     * @return Sling response
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public <T extends User> SlingHttpResponse removeImpersonators(T[] authorizables, int... expectedStatus)
            throws ClientException {
        if (authorizables == null) {
            throw new IllegalArgumentException("List of authorizables may not be null!");
        }
        // add user to group - base = group
        // prepare the form
        FormEntityBuilder formEntry = FormEntityBuilder.create();
        formEntry.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        for (T authorizable : authorizables) {
            formEntry.addParameter(Authorizable.PARAM_REMOVE_IMPERSONATORS, encodeURI(authorizable.getId()));
        }

        // send the request
        return doPost(formEntry, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Get user profile
     *
     * @return {@link UserProfile}
     * @throws ClientException if the request failed
     */
    public UserProfile getUserProfile() throws ClientException {
        if (profile == null) {
            initProfile();
        }
        return profile;
    }

    protected void initProfile() throws ClientException {
        // initialize profile
        this.profile = new UserProfile(this);
    }


    /**
     * Creates a new user.
     *
     * @param client           any class implementing the {@link SecurityClient}.
     * @param userId           the user ID for the new user.
     * @param password         the password to be assigned.
     * @param intermediatePath the root path user will be created.
     * @param profileMap       profile properties to be set for the new user.
     * @param expectedStatus   list of allowed HTTP Status to be returned. If not set,
     *                         http status 201 (CREATED) is assumed.
     * @param <T>              client type
     * @return {@code User}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    // FIXME move reading to a method loadUser() and make constructor private
    public static <T extends SecurityClient> User createUser(T client, String userId, String password,
                                                             String intermediatePath, Map<String, String> profileMap,
                                                             int... expectedStatus) throws ClientException, InterruptedException {
        return createUser(client, userId, password, intermediatePath, profileMap, true, expectedStatus);
    }

    /**
     * Creates a new user.
     *
     * @param client           any class implementing the {@link SecurityClient}.
     * @param userId           the user ID for the new user.
     * @param password         the password to be assigned.
     * @param intermediatePath the root path user will be created.
     * @param profileMap       profile properties to be set for the new user.
     * @param waitForIndex     Whether or not to wait for indexing to be complete before returning
     * @param expectedStatus   list of allowed HTTP Status to be returned. If not set,
     *                         http status 201 (CREATED) is assumed.
     * @param <T>              client type
     * @return {@code User}
     * @throws ClientException
     *          If something fails during request/response cycle
     * @throws InterruptedException to mark this method as "waiting"
     */
    public static <T extends SecurityClient> User createUser(T client, String userId, String password,
                                                             String intermediatePath, Map<String, String> profileMap,
                                                             boolean waitForIndex,
                                                             int... expectedStatus) throws ClientException, InterruptedException {
        if (client == null || userId == null) {
            throw new IllegalArgumentException("Client and userId may not be null!");
        }
        // password: fallback = userId
        if (password == null) {
            password = userId;
        }
        FormEntityBuilder feb = FormEntityBuilder.create();
        feb.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        feb.addParameter(Authorizable.PARAM_CREATE_USER, "" + Authorizable.ACTION_CREATE_USER);
        feb.addParameter(Authorizable.PARAM_AUTHORIZABLE_ID, userId);
        feb.addParameter(Authorizable.PARAM_PASSWORD, password);

        if (intermediatePath != null) {
            feb.addParameter(Authorizable.PARAM_INTERMEDIATE_PATH, intermediatePath);
        }

        // profile
        if (profileMap != null) {
            Set<String> profileProps = profileMap.keySet();
            for (String propName : profileProps) {
                String propValue = profileMap.get(propName);
                if (propValue != null) {
                    feb.addParameter("./" + UserProfile.NODE_PROFILE + "/" + propName, propValue);
                }
            }
        }

        client.getManager().doPost(feb, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));

        if (waitForIndex) {
            try {
                client.adaptTo(IndexingClient.class).waitForAsyncIndexing();
            } catch (TimeoutException e) {
                throw new ClientException("Waiting for async index update failed (" + userId +")");
            }
        }

        // create Authorizable
        return new User(client, userId);
    }
}
