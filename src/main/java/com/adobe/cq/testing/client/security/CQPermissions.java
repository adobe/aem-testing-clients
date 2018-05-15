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
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.URLParameterBuilder;
import org.codehaus.jackson.JsonNode;

import static org.apache.http.HttpStatus.SC_OK;

public class CQPermissions extends Permissions {

    public <T extends SecurityClient> CQPermissions(T client) {
        super(client);
    }

    /**
     * Changes permissions for an authorizable.
     *
     * @param authorizableId the authorizable id
     * @param path           path
     * @param read           read permission
     * @param modify         modify permission
     * @param create         createGroup permission
     * @param delete         delete permission
     * @param acl_read       read acl
     * @param acl_edit       edit acl
     * @param replicate      replication
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return Json node containing resulting permissions
     * @throws ClientException
     *          If something fails during request/response cycle
     */
    public JsonNode changePermissions(String authorizableId, String path, boolean read, boolean modify, boolean create,
                                      boolean delete, boolean acl_read, boolean acl_edit, boolean replicate,
                                      int... expectedStatus) throws ClientException {
        final String postPath = "/.cqactions.html";

        client.doPost(postPath, FormEntityBuilder.create()
                .addParameter("authorizableId", authorizableId)
                .addParameter("changelog", "path:" + path + "," +
                              "read:" + Boolean.toString(read) + "," +
                              "modify:" + Boolean.toString(modify) + "," +
                              "create:" + Boolean.toString(create) + "," +
                              "delete:" + Boolean.toString(delete) + "," +
                              "acl_read:" + Boolean.toString(acl_read) + "," +
                              "acl_edit:" + Boolean.toString(acl_edit) + "," +
                              "replicate:" + Boolean.toString(replicate) + "")
                .build(),
                HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return getPermissions(authorizableId, path, 0, 200);
    }

    /**
     * Get permissions for an authorizable.
     *
     * @param authorizableId the Id of the authorizable
     * @param path           path
     * @param depth          depth
     * @param expectedStatus list of allowed HTTP Status to be returned. If not set,
     *                       http status 200 (OK) is assumed.
     * @return the root {@link JsonNode}
     * @throws ClientException If something fails during request/response cycle
     */
    public JsonNode getPermissions(String authorizableId, String path, int depth, int... expectedStatus) throws
            ClientException {
        final String getPath = "/.cqactions.json";
        URLParameterBuilder params = URLParameterBuilder.create();
        params.add("authorizableId", authorizableId);
        params.add("path", path);
        params.add(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        params.add("depth", "" + depth);
        params.add("predicate", "useradmin");

        SlingHttpResponse exec = client.doGet(getPath, params.getList(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        return JsonUtils.getJsonNodeFromString(exec.getContent());
    }
}
