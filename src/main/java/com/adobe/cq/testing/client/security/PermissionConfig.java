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

import java.io.Serializable;

public class PermissionConfig {

    private String authorizableId;
    private String path;
    private boolean read = false;
    private boolean modify = false;
    private boolean create = false;
    private boolean delete = false;
    private boolean aclRead = false;
    private boolean aclEdit = false;
    private boolean replicate = false;

    public static Builder builder() {
        return new Builder();
    }

    public String getAuthorizableId() {
        return authorizableId;
    }

    public String getPath() {
        return path;
    }

    public boolean isRead() {
        return read;
    }

    public boolean isModify() {
        return modify;
    }

    public boolean isCreate() {
        return create;
    }

    public boolean isDelete() {
        return delete;
    }

    public boolean isAclRead() {
        return aclRead;
    }

    public boolean isAclEdit() {
        return aclEdit;
    }

    public boolean isReplicate() {
        return replicate;
    }

    public static class Builder implements Serializable {

        PermissionConfig config;

        Builder() {
            this.config = new PermissionConfig();
        }

        public Builder withAuthorizableId(String authorizableId) {
            config.authorizableId = authorizableId;
            return this;
        }

        public Builder withPath(String path) {
            config.path = path;
            return this;
        }

        public Builder withRead() {
            config.read = true;
            return this;
        }

        public Builder withModify() {
            config.modify = true;
            return this;
        }

        public Builder withCreate() {
            config.create = true;
            return this;
        }

        public Builder withDelete() {
            config.delete = true;
            return this;
        }

        public Builder withAclRead() {
            config.aclRead = true;
            return this;
        }

        public Builder withAclEdit() {
            config.aclEdit = true;
            return this;
        }

        public Builder withReplicate() {
            config.replicate = true;
            return this;
        }

        public Builder withoutRead() {
            config.read = false;
            return this;
        }

        public Builder withoutModify() {
            config.modify = false;
            return this;
        }

        public Builder withoutCreate() {
            config.create = false;
            return this;
        }

        public Builder withoutDelete() {
            config.delete = false;
            return this;
        }

        public Builder withoutAclRead() {
            config.aclRead = false;
            return this;
        }

        public Builder withoutAclEdit() {
            config.aclEdit = false;
            return this;
        }

        public Builder withoutReplicate() {
            config.replicate = false;
            return this;
        }

        public PermissionConfig build() {
            return config;
        }

    }

}
