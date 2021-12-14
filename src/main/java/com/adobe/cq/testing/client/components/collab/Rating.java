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

package com.adobe.cq.testing.client.components.collab;

import com.fasterxml.jackson.databind.JsonNode;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Rating {

    public static final String RESOURCE_TYPE = "collab/commons/components/ratings/rating";
    public static final String PRIMARY_TYPE = "cq:Rating";

    private Date added;
    private int rating;
    private String description;
    private String url;
    private String userAgent;
    private String email;
    private boolean approved;
    private String userIdentifier;
    private String createdBy;


    public Rating(Date added, int rating, String description, String url, String userAgent, String email, boolean approved,
                    String userIdentifier, String createdBy) {
        this.added = added;
        this.rating = rating;
        this.description = description;
        this.url = url;
        this.userAgent = userAgent;
        this.email = email;
        this.approved = approved;
        this.userIdentifier = userIdentifier;
        this.createdBy = createdBy;
    }


    public Rating(JsonNode node) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
            this.added = format.parse(node.get("added").textValue());
        } catch (ParseException e) {
            this.added = null;
        }
        this.rating = node.get("rating").intValue();
        this.description = node.get("jcr:description").textValue();
        this.url = node.get("url").textValue();
        this.userAgent = node.get("userAgent").textValue();
        this.email = node.get("email").textValue();
        this.approved = node.get("approved") != null && node.get("approved").booleanValue();
        this.userIdentifier = node.get("userIdentifier").textValue();
        this.createdBy = node.get("jcr:createdBy").textValue();
    }


    /**
     * @return the added
     */
    public Date getAdded() {
        return added;
    }


    /**
     * @param added
     *            the added to set
     */
    public void setAdded(Date added) {
        this.added = added;
    }


    /**
     * @return the rating
     */
    public int getRating() {
        return rating;
    }


    /**
     * @param rating
     *            the rating to set
     */
    public void setRating(int rating) {
        this.rating = rating;
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }


    /**
     * @param url
     *            the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }


    /**
     * @return the userAgent
     */
    public String getUserAgent() {
        return userAgent;
    }


    /**
     * @param userAgent
     *            the userAgent to set
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }


    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }


    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }


    /**
     * @return the approved
     */
    public boolean isApproved() {
        return approved;
    }


    /**
     * @param approved
     *            the approved to set
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }


    /**
     * @return the userIdentifier
     */
    public String getUserIdentifier() {
        return userIdentifier;
    }


    /**
     * @param userIdentifier
     *            the userIdentifier to set
     */
    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }


    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }


    /**
     * @param createdBy
     *            the createdBy to set
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }


    /**
     * @return the resourceType
     */
    public static String getResourceType() {
        return RESOURCE_TYPE;
    }


    /**
     * @return the primaryType
     */
    public static String getPrimaryType() {
        return PRIMARY_TYPE;
    }

}
