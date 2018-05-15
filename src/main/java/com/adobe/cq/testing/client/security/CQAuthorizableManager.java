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

public class CQAuthorizableManager extends AuthorizableManager {

    public <T extends SecurityClient> CQAuthorizableManager(T client) {
        super(client);
    }

    /**
     * Get built-in group "content-authors"
     * @return content-authors' {@link Group}
     * @throws ClientException if the request fails
     */
    public Group getGroupAuthors() throws ClientException {
        return getGroup("content-authors");
    }

    /**
     * Get built-in group "workflow-editors"
     * @return workflow-editors' {@link Group}
     * @throws ClientException if the request fails
     */
    public Group getGroupWorkflowEditors() throws ClientException {
        return getGroup("workflow-editors");
    }

    /**
     * Get built-in group "workflow-users"
     * @return workflow-users' {@link Group}
     * @throws ClientException if the request fails
     */
    public Group getGroupWorkflowUsers() throws ClientException {
        return getGroup("workflow-users");
    }

    /**
     * Get built-in group "tag-administrators"
     * @return tag-administrators' {@link Group}
     * @throws ClientException if the request fails
     */
    public Group getGroupTagAdministrators() throws ClientException {
        return getGroup("tag-administrators");
    }
}
