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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.sling.testing.clients.ClientException;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Utility to get a CSRF token needed in some POST methods.
 */
public final class CSRFUtils {

    private CSRFUtils() {
    }

    protected static final String TOKEN_SERVLET_ENDPOINT = "/libs/granite/csrf/token.json";

    public static String createCSRFToken(final CQClient client) throws ClientException {
        String content = client.doGet(TOKEN_SERVLET_ENDPOINT, SC_OK).getContent();
        return new Gson().fromJson(content, JsonObject.class).get("token").getAsString();
    }
}
