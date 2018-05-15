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
 * Define and load user's profile properties
 */
public class UserProfile extends AbstractProfile {

    public static final String PROPERTY_EMAIL = "email";
    public static final String PROPERTY_JOB_TITLE = "jobTitle";
    public static final String PROPERTY_GIVEN_NAME = "givenName";
    public static final String PROPERTY_MIDDLE_NAME = "middleName";
    public static final String PROPERTY_FAMILY_NAME = "familyName";
    public static final String PROPERTY_STREET = "street";
    public static final String PROPERTY_CITY = "city";
    public static final String PROPERTY_POSTAL_CODE = "postalCode";
    public static final String PROPERTY_STATE = "state";
    public static final String PROPERTY_COUNTRY = "country";
    public static final String PROPERTY_PHONE_NUMBER = "phoneNumber";
    public static final String PROPERTY_MOBILE = "mobile";
    public static final String PROPERTY_GENDER = "gender";
    public static final String PROPERTY_ABOUT_ME = "aboutMe";

    /**
     * Default constructor for an existing user
     *
     * @param authorizable any {@link Authorizable} extending the {@link AbstractAuthorizable}
     * @param <T> authorizable type
     * @throws ClientException if the user profile cannot be loaded
     *
     */
    public <T extends AbstractAuthorizable> UserProfile(T authorizable) throws ClientException {
        super(authorizable);
    }

    public void setEmail(String email) {
        profileProps.put(PROPERTY_EMAIL, email);
    }

    public void setJobTitle(String jobTitle) {
        profileProps.put(PROPERTY_JOB_TITLE, jobTitle);
    }

    public void setGivenName(String givenName) {
        profileProps.put(PROPERTY_GIVEN_NAME, givenName);
    }

    public void setMiddleName(String middleName) {
        profileProps.put(PROPERTY_MIDDLE_NAME, middleName);
    }

    public void setFamilyName(String familyName) {
        profileProps.put(PROPERTY_FAMILY_NAME, familyName);
    }

    public void setStreet(String street) {
        profileProps.put(PROPERTY_STREET, street);
    }

    public void setCity(String city) {
        profileProps.put(PROPERTY_CITY, city);
    }

    public void setPostalCode(String postalCode) {
        profileProps.put(PROPERTY_POSTAL_CODE, postalCode);
    }

    public void setState(String state) {
        profileProps.put(PROPERTY_STATE, state);
    }

    public void setCountry(String country) {
        profileProps.put(PROPERTY_COUNTRY, country);
    }

    public void setPhoneNumber(String phoneNumber) {
        profileProps.put(PROPERTY_PHONE_NUMBER, phoneNumber);
    }

    public void setMobile(String mobile) {
        profileProps.put(PROPERTY_MOBILE, mobile);
    }

    public void setGender(String gender) {
        profileProps.put(PROPERTY_GENDER, gender);
    }

    public void setAboutMe(String aboutMe) {
        profileProps.put(PROPERTY_ABOUT_ME, aboutMe);
    }
}
