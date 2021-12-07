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
package com.adobe.cq.testing.client.workflow;


import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

/**
 * Simple wrapper for the return JSON representing a work item in a users inbox.
 */
public class InboxItem {

    private JsonNode rootNode = null;

    /**
     * The only Constructor.
     *
     * @param rootNode the JSON node containing the data.
     */
    public InboxItem(JsonNode rootNode){
        this.rootNode = rootNode;
    }

    /**
     * returns the URI of this work item
     *
     * @return the URI of the work item
     */
    public String getUri() {
        if (rootNode.get("uri") == null)return null;
        return rootNode.get("uri").asText();
    }

    /**
     * returns the current assignee for this work item.
     * @return  current assignee
     */
    public String  getCurrentAssignee(){
        if (rootNode.get("currentAssignee") == null)return null;
        return rootNode.get("currentAssignee").asText();
    }

    /**
     * The start date when this work item was started
     *
     * @return   the start date
     */
    public Date getStartTime(){
        if (rootNode.get("startTime") == null)return null;
        Date startTime = new Date();
        startTime.setTime(Long.parseLong(rootNode.get("startTime").asText()));
        return startTime;
    }

    /**
     * the end date when this work item was completed
     *
     * @return the end date
     */
    public Date getEndTime(){
        if (rootNode.get("endTime") == null)return null;
        Date endTime = new Date();
        endTime.setTime(Long.parseLong(rootNode.get("endTime").asText()));
        return endTime;
    }

    /**
     * the payload type, either URL or JCR_PATH.
     *
     * @return the type of payload reference
     */
    public String getPayloadType(){
        if (rootNode.get("payloadType") == null)return null;
        return rootNode.get("payloadType").asText();
    }

    /**
     * the reference to the payload
     * @return payload reference
     */
    public String getPayload(){
        if (rootNode.get("payload") == null)return null;
        return rootNode.get("payload").asText();
    }

    /**
     * the comment set for this work item
     * @return the comment
     */
    public String getComment(){
        if (rootNode.get("comment") == null)return null;
        return rootNode.get("comment").asText();
    }

}
