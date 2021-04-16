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
package com.adobe.cq.testing.util;

import com.adobe.cq.testing.client.CQClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.JsonUtils;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Utility to get a CSRF token needed in some POST methods.
 */
public final class CSRFUtils {

    private CSRFUtils() {
    }

    private static final String TOKEN_SERVLET_ENDPOINT = "/libs/granite/csrf/token.json";

    /**
     * csrf token parameter to be use in POST request
     */
    public static final String PARAM_CSRF_TOKEN = ":cq_csrf_token";

    /**
     *
     * @param client Client to use
     * @return CSRF token
     * @throws ClientException if the request fails
     */
    public static String createCSRFToken(final CQClient client) throws ClientException {
        String content = client.doGet(TOKEN_SERVLET_ENDPOINT, SC_OK).getContent();
        return JsonUtils.getJsonNodeFromString(content).path("token").getTextValue();
    }
}