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
import com.adobe.cq.testing.client.security.Authorizable;
import com.adobe.cq.testing.client.security.CQPermissions;
import com.adobe.cq.testing.client.security.Group;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.junit.rules.ExternalResource;
import org.ops4j.lang.NullArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * TemporaryContentAuthorGroup creates a content author group with write permission under /content
 * and deletes it at the end of the test.
 * Whether the delete is successful or not is not checked.
 * The create operation is retried until a timeout is reached.
 * The total wait time in the {@code before} method can be up to 30s.
 */
public class TemporaryContentAuthorGroup extends ExternalResource {
    private static final Logger LOG = LoggerFactory.getLogger(TemporaryContentAuthorGroup.class);
    private static final long DEFAULT_TIMEOUT = Duration.ofSeconds(30).toMillis();
    private static final long DEFAULT_RETRY_DELAY = Duration.ofSeconds(1).toMillis();
    private static final String CONTENT_NODE = "/content";
    private final Supplier<SlingClient> creatorSupplier;
    private final String groupName;

    private final ThreadLocal<CQClient> groupClient;

    /**
     * Instantiate a new TemporaryContentAuthorGroup rule, to be used with the {@code @Rule} annotation.
     * @param creatorSupplier supplier for the client used to create the temporary content author group
     */
    public TemporaryContentAuthorGroup(Supplier<SlingClient> creatorSupplier) {
        if (creatorSupplier == null) {
            throw new NullArgumentException("creatorSupplier is null");
        }

        this.creatorSupplier = creatorSupplier;
        this.groupName = createUniqueAuthorizableId("testGroup");

        this.groupClient = new ThreadLocal<>();
    }

    /**
     * @return the CQClient matching the temporary content author group
     */
    public CQClient getClient() {
        return this.groupClient.get();
    }

    /**
     * @return the group name
     */
    public String getGroupName() {
        return this.groupName;
    }

    /**
     * @return a <code>SlingClient</code> Supplier matching the temporary content author group
     */
    public Supplier<SlingClient> getClientSupplier() {
        return this::getClient;
    }

    @Override
    protected void before() throws Throwable {
        CQSecurityClient securityClient = creatorSupplier.get().adaptTo(CQSecurityClient.class);
        CQPermissions permissionsObj = new CQPermissions(securityClient);


        class CreateGroupPolling extends Polling {
            Group group;

            @Override
            public Boolean call() throws Exception {
                group = securityClient.createGroup((groupName), 201);
                // set permissions
                permissionsObj.changePermissions(group.getId(), CONTENT_NODE, true, true, true, false,
                        false, false, false, 200);
                return true;
            }
        }

        CreateGroupPolling p = new CreateGroupPolling();
        try {
            p.poll(DEFAULT_TIMEOUT, SECONDS.toMillis(DEFAULT_RETRY_DELAY));
        } catch (TimeoutException e) {
            LOG.error("Timeout of " + DEFAULT_TIMEOUT +" seconds reached while trying to create group." +
                    " List of exceptions: " + p.getExceptions(), e);
            deleteGroup();
            throw e;
        }

        LOG.info("Created group {}", groupName);
    }

    @Override
    protected void after() {
        deleteGroup();
    }

    /**
     * Delete the created group.
     * The delete operation is not retried and exceptions are ignored.
     */
    protected void deleteGroup() {
        CQSecurityClient securityClient;

        try {
            securityClient = creatorSupplier.get().adaptTo(CQSecurityClient.class);
        } catch (ClientException e) {
            LOG.warn("Unable to delete group", e);
            return;
        }
        try {
            if (Group.exists(securityClient, groupName)) {
                Group grouptoDelete = new Group(securityClient, groupName);
                new Polling(() -> {
                    securityClient.deleteAuthorizables(new Authorizable[]{grouptoDelete});
                    return true;
                }).poll(DEFAULT_TIMEOUT, SECONDS.toMillis(DEFAULT_RETRY_DELAY));

                LOG.info("Deleted group {}", groupName);
            }
        } catch (Exception e) {
            LOG.warn("Failed to delete group {}, but error is ignored", groupName);
        }
    }

    /**
     * Create unique authorizable Id to make sure no side effects from versioning / restore occurs
     *
     * @param authorizableId authorizable id
     * @return unique authorizableId
     */
    protected String createUniqueAuthorizableId(String authorizableId) {
        return authorizableId + UUID.randomUUID();
    }

}
