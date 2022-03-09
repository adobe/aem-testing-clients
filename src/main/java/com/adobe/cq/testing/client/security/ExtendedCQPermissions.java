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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public JsonNode changePermissionsWithRetry(PermissionConfig config, int... expectedStatus) throws ClientException {
        int retries = 0;
        int maxRetries = 10;
        boolean passed = false;
        JsonNode permissions = JsonNodeFactory.instance.objectNode();
        while (!passed && (retries < maxRetries)) {
            try {
                permissions = super.changePermissions(config.getAuthorizableId(), config.getPath(),
                        config.isRead(), config.isModify(), config.isCreate(), config.isDelete(),
                        config.isAclRead(), config.isAclEdit(), config.isReplicate(), expectedStatus);
                LOG.info("new set permission response string for {} at {}: {}",
                        config.getAuthorizableId(), config.getPath(), permissions);
                passed = true;
            } catch (Exception e) {
                if (retries + 1 == maxRetries) {
                    LOG.error("Maximal retries reached!");
                    throw e;
                }
                LOG.warn("Error at retry number {} on change permission -> retry:  {}",retries, e.getLocalizedMessage(), e);
            } finally {
                retries++;
            }
        }
        return permissions;
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
    @Override
    public JsonNode getPermissions(String authorizableId, String path, int depth, int... expectedStatus) throws
            ClientException {
        int retries = 0;
        int maxRetries = 10;
        boolean passed = false;
        JsonNode permissions = JsonNodeFactory.instance.objectNode();
        while (!passed && (retries < maxRetries)) {
            try {
                permissions = super.getPermissions(authorizableId, path, depth, expectedStatus);
                passed = true;
            } catch (ClientException e) {
                if (retries + 1 == maxRetries) {
                    LOG.error("Maximal retries reached!");
                    throw e;
                }
                LOG.warn("Error at retry number {} on get permission -> retry:  {}",retries, e.getLocalizedMessage(), e);
            } finally {
                retries++;
            }
        }
        return permissions;
    }

}
