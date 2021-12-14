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

import com.adobe.cq.testing.client.WorkflowClient;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Date;

/**
 * Simple wrapper around the History Item info returned by the server
 */
public class HistoryItem {
	
	public static final String STATUS_ACTIVE = "Active";

	public static final String STATUS_COMPLETED = "Completed";
	
    private JsonNode rootNode = null;

    /**
     * the only constructor
     *
     * @param rootNode the JSON node containing all the infos
     */
    public HistoryItem(JsonNode rootNode){
        this.rootNode = rootNode;
    }

    /**
     * The status of this history item (i.e Active, Completed etc..)
     * @return status
     */
    public String getStatus() {
        if (rootNode.get("status") == null)return null;
        return rootNode.get("status").asText();
    }

    /**
     * The title of the step that is related to this history item
     * @return process title
     */
    public String getProcess() {
        if (rootNode.get("process") == null)return null;
        return rootNode.get("process").asText();
    }

    /**
     * The user id that performed this step in the history item
     * @return userid
     */
    public String getUser() {
        if (rootNode.get("user") == null)return null;
        return rootNode.get("user").asText();
    }

    /**
     * The action of this history item (i.e NodeTransition etc..)
     * @return comment
     */
    public String getAction() {
        if (rootNode.get("action") == null)return null;
        return rootNode.get("action").asText();
    }

    /**
     * The comment information available in the history item
     * @return comment
     */
    public String getComment() {
        if (rootNode.get("comment") == null)return null;
        return rootNode.get("comment").asText();
    }

    /**
     * The start date of this history item
     * @return start date
     */
    public Date getStartDate(){
        if (rootNode.get("startTime") == null)return null;
        return WorkflowClient.parseJSONDate(rootNode.get("startTime").asText());
    }

    /**
     * The end date of this history item
     * @return end date
     */
    public Date getEndDate(){
        if (rootNode.get("endTime") == null)return null;
        return WorkflowClient.parseJSONDate(rootNode.get("endTime").asText());
    }

}
