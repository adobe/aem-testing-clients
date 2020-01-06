/*
 * Copyright 2019 Adobe Systems Incorporated
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
package com.adobe.cq.testing.junit5.extensions;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Some constants used in the extensions
 */
public final class Constants {
    private Constants() {};
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.GLOBAL;
    public static final String SLING_CLIENTS_MAP_KEY = "slingClients";
    public static final String RUNMODE_AUTHOR = "author";
    public static final String RUNMODE_PUBLISH = "publish";
    public static final String DEFAULT_USER = "admin";
    public static final String DEFAULT_PWD = "admin";
    public static final String DEFAULT_AUTHOR_URL = "http://localhost:4502";
    public static final String DEFAULT_PUBLISH_URL = "http://localhost:4503";
}
