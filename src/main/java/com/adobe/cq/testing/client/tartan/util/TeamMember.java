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

package com.adobe.cq.testing.client.tartan.util;

public class TeamMember {

    private String userId;
    private String roleId;
    private String message;

    public static String PARAM_USER_ID = "teamMemberUserId";
    public static String PARAM_ROLE_ID = "teamMemberRoleId";
    public static String PARAM_MESSAGE = "teamMemberMessage";

    public static String PARAM_ROLE_VIEWER = "viewer";
    public static String PARAM_ROLE_EDITOR = "editor";
    public static String PARAM_ROLE_OWNER = "owner";

    public TeamMember(String userId, String roleId, String message) {
        this.userId = userId;
        this.roleId = roleId;
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
