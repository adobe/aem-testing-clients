/*
 * Copyright 2022 Adobe Systems Incorporated
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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

public class ExtendedCQPermissions extends CQPermissions {

    private static final Logger LOG = LoggerFactory.getLogger(ExtendedCQPermissions.class);

    public <T extends SecurityClient> ExtendedCQPermissions(T client) {
        super(client);
    }

    /**
     * Changes permissions for an authorizable with retry in case an exception is thrown.
     * The exception is thrown anyway if 10 times retry did not succeed.
     *
     * @param config The PermissionConfig to be used
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Json node containing resulting permissions
     * @throws ClientException
     *          If something fails during request/response cycle after 10 times unsuccessful retry
     */
    public JsonNode changePermissionsWithRetry(PermissionConfig config, long timeout, long delay,
                                               int... expectedStatus) throws ClientException, InterruptedException {

        class ChangePermissionsPolling extends Polling {
            JsonNode permissions = JsonNodeFactory.instance.objectNode();

            @Override
            public Boolean call() throws Exception {
                try {
                    permissions = changePermissions(config.getAuthorizableId(), config.getPath(),
                            config.isRead(), config.isModify(), config.isCreate(), config.isDelete(),
                            config.isAclRead(), config.isAclEdit(), config.isReplicate(), expectedStatus);
                    LOG.info("new set permission response string for {} at {}: {}",
                            config.getAuthorizableId(), config.getPath(), permissions);
                    return true;
                } catch (Exception e) {
                        LOG.warn("Error on change permission -> retry:  {}", e.getLocalizedMessage(), e);
                        throw e;
                }
            }
        }

        ChangePermissionsPolling createPolling = new ChangePermissionsPolling();
        try {
            createPolling.poll(timeout, delay);
        } catch (TimeoutException e) {
            String errorMsg = String.format("Failed to change permission for {} at {} in {}",
                    config.getAuthorizableId(), config.getPath(), createPolling.getWaited());
            throw new ClientException(errorMsg, e);
        }
        return createPolling.permissions;
    }

    /**
     * Get permissions for an authorizable with retry in case an exception is thrown.
     * The exception is thrown anyway if 10 times retry did not succeed.
     *
     * @param authorizableId the Id of the authorizable
     * @param path           path
     * @param depth          depth
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return the root {@link JsonNode}
     * @throws ClientException If something fails during request/response cycle after 10 times unsuccessful retry
     */

    public JsonNode getPermissionsWithRetry(String authorizableId, String path, int depth,
                                   long timeout, long delay, int... expectedStatus) throws
            ClientException, InterruptedException {
        class GetPermissionsPolling extends Polling {
            JsonNode permissions = JsonNodeFactory.instance.objectNode();

            @Override
            public Boolean call() throws Exception {
                try {
                    permissions = getPermissions(authorizableId, path, depth, expectedStatus);
                    LOG.info("get permission response string for {} at {} with depth {}: {}",
                            authorizableId, path, depth, permissions);
                    return true;
                } catch (Exception e) {
                    LOG.warn("Error on get permission -> retry:  {}", e.getLocalizedMessage(), e);
                    throw e;
                }
            }
        }

        GetPermissionsPolling createPolling = new GetPermissionsPolling();
        try {
            createPolling.poll(timeout, delay);
        } catch (TimeoutException e) {
            String errorMsg = String.format("Failed to get permission for {} at {} with depth {} in {}",
                    authorizableId, path, depth, createPolling.getWaited());
            throw new ClientException(errorMsg, e);
        }
        return createPolling.permissions;

    }

}
