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
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.codehaus.jackson.JsonNode;

/**
 * http://qabase.day.com/cf#/content/qabase/testcases/cq/setting_user_andgrouppreferencesdocumentation.html
 */
public class CQPreferences {

    public static final String PREFERENCES_NODE = "preferences";
    public static final String SELECTOR = ".preferences";
    public static final String LANGUAGE_PROPERTY = "language";
    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_DE = "de";
    public static final String LANGUAGE_FR = "fr";


    protected SecurityClient client;

    /**
     * Group / User
     */
    protected Authorizable authorizable;

    /**
     * path to {@link Authorizable}s' references
     */
    protected String path;

    public CQPreferences(Authorizable authorizable) throws ClientException {
        if (authorizable == null) {
            throw new IllegalArgumentException("Authorizable may not be null!");
        }
        this.authorizable = authorizable;
        this.client = authorizable.getClient();
        this.path = authorizable.getHomePath() + "." + SELECTOR;
    }

    public SecurityClient getClient() {
        return client;
    }

    public Authorizable getAuthorizable() {
        return authorizable;
    }

    /**
     * Get path to {@link Authorizable}s' references
     *
     * @return path
     * @throws ClientException if the request fails
     */
    public String getPath() throws ClientException {
        return path;
    }

    /**
     * Get json representation of the preferences
     *
     * @param expectedStatus list of expected http status codes
     * @return preferences as {@link JsonNode}
     * @throws ClientException if the request fails
     */
    public JsonNode getJson(int... expectedStatus) throws ClientException {
        return JsonUtils.getJsonNodeFromString(
                client.doGet(authorizable.getHomePath() + SELECTOR + ".json",
                        HttpUtils.getExpectedStatus(200, expectedStatus)).getContent());
    }

    /**
     * Get language settings of {@link Authorizable}
     *
     * @return language
     * @throws ClientException if the request fails
     */
    public String getLanguage() throws ClientException {
        JsonNode lang = getJson().get(LANGUAGE_PROPERTY);
        return (lang == null) ? LANGUAGE_EN : lang.getValueAsText();
    }

    /**
     * Set language - if language is not given, default is set to "en"
     *
     * @param language language to set
     * @return Sling response
     * @throws ClientException if the request fails
     */
    public SlingHttpResponse setLanguage(String language) throws ClientException {
        if (language == null) {
            language = LANGUAGE_EN;
        }
        FormEntityBuilder feb = FormEntityBuilder.create();
        feb.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        feb.addParameter("./" + LANGUAGE_PROPERTY, language);
        return client.doPost(authorizable.getHomePath() + "/" + PREFERENCES_NODE, feb.build(), 201);
    }
}
