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

package com.adobe.cq.testing;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;

import static org.apache.sling.testing.clients.SystemPropertiesConfig.CONFIG_PROP_PREFIX;

public class Constants {

    private static final String DEFAULT_URL = "%s://%s:%d%s";
    private static final String DEFAULTS_PROP_PREFIX = CONFIG_PROP_PREFIX + "defaults.";
    private static final String ADMIN = "admin";

    private Constants() {
    }

    public static final String GROUPID_TEMPLATE_AUTHORS = "template-authors";
    public static final String GROUPID_CONTENT_AUTHORS = "content-authors";
    public static final String GROUPID_EVERYONE = "everyone";

    public static final String PARAM_APPLY_TO = ":applyTo";
    public static final String PARAM_CSRF_TOKEN = ":cq_csrf_token";
    public static final String PARAM_DESCRIPTION = "description";
    public static final String PARAM_PARENT_PATH = "_parentPath_";

    public static final String PROP_JCR_TITLE = "./jcr:title";

    public static final String HOME_GROUPS = "/home/groups/";
    public static final String HOME_USERS = "/home/users/";
    public static final String CONTENT_ROOT = "/content";
    public static final String CONTENT_DAM = "/content/dam";
    public static final String PROP_CQ_CONF = "cq:conf";
    public static final String PROP_CQ_ALLOWED_TEMPLATES = "cq:allowedTemplates";

    public static final long DEFAULT_RETRY_DELAY = Duration.ofSeconds(1).toMillis();
    public static final long DEFAULT_TIMEOUT = Duration.ofSeconds(30).toMillis();
    public static final int DEFAULT_SMALL_SIZE = 8;

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.GLOBAL;
    public static final String SLING_CLIENTS_MAP_KEY = "slingClients";
    public static final String RUNMODE_AUTHOR = "author";
    public static final String RUNMODE_PUBLISH = "publish";
    public static final String DEFAULT_SCHEME = "http";
    public static final String DEFAULT_HOST = "localhost";
    public static final String DEFAULT_CONTEXT_PATH = "/";
    public static final int DEFAULT_AUTHOR_PORT = Integer.parseInt("4502");
    public static final int DEFAULT_PUBLISH_PORT = Integer.parseInt("4503");
    public static final String DEFAULT_USER = ADMIN;
    public static final String DEFAULT_PASSWORD = ADMIN;
    public static final String DEFAULT_AUTHOR_URL = String.format(DEFAULT_URL,
            DEFAULT_SCHEME, DEFAULT_HOST, DEFAULT_AUTHOR_PORT, DEFAULT_CONTEXT_PATH);
    public static final String DEFAULT_PUBLISH_URL = String.format(DEFAULT_URL,
            DEFAULT_SCHEME, DEFAULT_HOST, DEFAULT_PUBLISH_PORT, DEFAULT_CONTEXT_PATH);

}
