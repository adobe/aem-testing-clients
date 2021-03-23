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

import java.time.Duration;

public class Constants {

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

    public static final long DEFAULT_RETRY_DELAY = Duration.ofSeconds(1).toMillis();
    public static final long DEFAULT_TIMEOUT = Duration.ofSeconds(30).toMillis();

}
