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

import org.apache.sling.testing.clients.ClientException;

/**
 * Define and load group's profile properties
 */
public class GroupProfile extends AbstractProfile {

    public static final String PROPERTY_GIVEN_NAME = "givenName";
    public static final String PROPERTY_ABOUT_ME = "aboutMe";

    /**
     * Default constructor for an existing group
     *
     * @param authorizable any {@link Authorizable} extending the {@link AbstractAuthorizable}
     * @param <T> authorizable type
     * @throws ClientException if the group details failed to load
     *
     */
    public <T extends AbstractAuthorizable> GroupProfile(T authorizable) throws ClientException {
        super(authorizable);
    }

    public void setGivenName(String givenName) {
        profileProps.put(PROPERTY_GIVEN_NAME, givenName);
    }

    public void setAboutMe(String aboutMe) {
        profileProps.put(PROPERTY_ABOUT_ME, aboutMe);
    }
}
