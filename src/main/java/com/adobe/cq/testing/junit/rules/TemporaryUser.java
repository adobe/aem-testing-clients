/*
 * Copyright 2021 Adobe Systems Incorporated
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
package com.adobe.cq.testing.junit.rules;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.CQSecurityClient;
import com.adobe.cq.testing.client.SecurityClient;
import com.adobe.cq.testing.client.security.Authorizable;
import com.adobe.cq.testing.client.security.Group;
import com.adobe.cq.testing.client.security.User;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.junit.rules.ExternalResource;
import org.ops4j.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * TemporaryUser creates a user and deletes it at the end of the test.
 * Whether the delete is successful or not is not checked.
 * The create operation is retried until a timeout is reached.
 * The total wait time in the {@code before} method can be up to 30s.
 */
public class TemporaryUser extends ExternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(TemporaryUser.class);

    private final Supplier<SlingClient> creatorSupplier;
    private final String[] groups;

    private final ThreadLocal<CQClient> userClient;
    private final ThreadLocal<List<String>> usersToDelete;

    /**
     * Instantiate a new TemporaryUser rule, to be used with the {@code @Rule} annotation.
     * @param creatorSupplier supplier for the client used to create the temporary user
     * @param groups groups to which the temporary user is assigned
     */
    public TemporaryUser(Supplier<SlingClient> creatorSupplier, String... groups) {
        if (creatorSupplier == null) {
            throw new NullArgumentException("creatorSupplier is null");
        }

        this.creatorSupplier = creatorSupplier;
        this.groups = groups;

        this.userClient = new ThreadLocal<>();
        this.usersToDelete = ThreadLocal.withInitial(ArrayList::new);
    }

    /**
     * @return the CQClient matching the temporary user
     */
    public CQClient getClient() {
        return this.userClient.get();
    }

    /**
     * @return a <code>SlingClient</code> Supplier matching the temporary user
     */
    public Supplier<SlingClient> getClientSupplier() {
        return this::getClient;
    }

    @Override
    protected void before() throws Throwable {
        CQSecurityClient securityClient = creatorSupplier.get().adaptTo(CQSecurityClient.class);
        Group[] assignedGroups = Arrays.stream(groups).map(getGroupFunction(securityClient)).toArray(Group[]::new);

        class CreateUserPolling extends Polling {
            String username;
            String password;
            User user;

            @Override
            public Boolean call() throws Exception {
                username = generateName();
                password = generatePassword();
                usersToDelete.get().add(username);
                user = securityClient.createUser(username, password, assignedGroups);
                return true;
            }
        }

        CreateUserPolling p = new CreateUserPolling();
        try {
            p.poll(SECONDS.toMillis(20), SECONDS.toMillis(1));
        } catch (TimeoutException e) {
            LOG.error("Timeout of 20s reached while trying to create user." +
                    " List of exceptions: " + p.getExceptions(), e);
            deleteUsers();
            throw e;
        }

        LOG.info("Created user {} at {}", p.user.getId(), p.user.getHomePath());
        userClient.set(new CQClient(securityClient.getUrl(), p.username, p.password));
    }

    @Override
    protected void after() {
        deleteUsers();
    }

    /**
     * Generate a unique name for the user.
     * Can be overridden if anther pattern is needed.
     *
     * @return a unique username
     */
    protected String generateName() {
        return "testuser-" + UUID.randomUUID();
    }

    /**
     * Generate a unique password for the user.
     * Can be overridden if another pattern is needed.
     *
     * @return a unique, random password
     */
    protected String generatePassword() {
        return new Random().ints(97, 123)
                .limit(30)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    /**
     * Delete all the created users.
     * The delete operation is not retried and exceptions are ignored.
     */
    protected void deleteUsers() {
        CQSecurityClient securityClient;

        try {
            securityClient = creatorSupplier.get().adaptTo(CQSecurityClient.class);
        } catch (ClientException e) {
            LOG.warn("Unable to delete user", e);
            return;
        }

        for (String username : usersToDelete.get()) {
            try {
                if (User.exists(securityClient, username)) {
                    User userToDelete = new User(securityClient, username);

                    new Polling(() -> {
                        securityClient.deleteAuthorizables(new Authorizable[]{userToDelete});
                        return true;
                    }).poll(SECONDS.toMillis(10), SECONDS.toMillis(1));

                    LOG.info("Deleted user {}", username);
                }
            } catch (Exception e) {
                LOG.warn("Failed to delete user {}, but error is ignored", username);
            }
        }
    }

    /**
     * Helper for returning a mapping function that instantiates a Group from its name
     * @param client the SecurityClient to be used for instantiating the Group
     *
     * @return the mapping function
     */
    private Function<String, Group> getGroupFunction(SecurityClient client) {
        return (String groupName) -> {
            try {
                return new Group(client, groupName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load group " + groupName, e);
            }
        };
    }
}
