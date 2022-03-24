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

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.sling.testing.clients.ClientException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Define and load authorizable's profile properties
 */
public class AbstractProfile implements Profile {

    public static final String NODE_PROFILE = "profile";

    protected AbstractAuthorizable authorizable;

    /**
     * Collect the properties for the profile
     */
    protected LinkedHashMap<String, String> profileProps = new LinkedHashMap<>();

    /**
     * Default constructor for an existing authorizable
     *
     * @param authorizable any {@link Authorizable} extending the {@link AbstractAuthorizable}
     * @param <T> authorizable type
     * @throws ClientException if the request to load the authorizable failed
     *
     */
    public <T extends AbstractAuthorizable> AbstractProfile(T authorizable)
            throws ClientException {
        if (authorizable == null) {
            throw new IllegalArgumentException("Authorizable has to exist and therefore may not be null!");
        }
        this.authorizable = authorizable;

        //load profile properties
        loadProperties();
    }

    /**
     * Get profile properties from {@link Authorizable}
     *
     * @return profile properties as {@link JsonNode}
     * @throws ClientException if the request failed
     *
     */
    public JsonNode getProfileNode() throws ClientException {
        return authorizable.getProfile();
    }

    public HashMap<String, String> getProperties() {
        return profileProps;
    }

    public void setProperties(HashMap<String, String> propertiesMap) {
        if (propertiesMap == null) {
            throw new IllegalArgumentException("Properties map for profile may not be empty!");
        }
        profileProps.putAll(propertiesMap);
    }

    /**
     * Load properties from Authorizable
     *
     * @throws ClientException if the request failed
     *
     */
    private void loadProperties() throws ClientException {
        JsonNode profileNode = getProfileNode();
        if (profileNode != null) {
            for (Iterator<String> fieldNames = profileNode.fieldNames(); fieldNames.hasNext(); ) {
                String fieldName = fieldNames.next();
                JsonNode fieldNode = profileNode.get(fieldName);
                if (fieldNode.isArray()) {
                    profileProps.put(fieldName, arrayNodeToString(fieldNode));
                } else {
                    profileProps.put(fieldName, fieldNode.textValue());
                }
            }
        }
    }

    private @NotNull String arrayNodeToString(@NotNull JsonNode node) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < node.size(); i++) {
            sb.append(node.get(i).asText());
            if (i + 1 < node.size()) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
