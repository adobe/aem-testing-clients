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

import java.util.*;


/**
 * Wraps a Workflow Instance info returned from server as JSON.
 */

public class WorkflowInstance {

    private JsonNode rootNode = null;

    /**
     * The only constructor
     *
     * @param rootNode the JSON node containing all infos
     */
    public WorkflowInstance(JsonNode rootNode){
        this.rootNode = rootNode;
    }

    /**
     * returns the id of this workflow instance
     * @return the id
     */
    public String getId(){
        if (rootNode.get("id") == null)return null;
        return rootNode.get("id").asText();
    }

    /**
     * returns the current state of this workflow instance, see {@link WorkflowClient.Status }
     * @return the current status.
     */
    public WorkflowClient.Status getStatus(){
        if (rootNode.get("state") == null)return null;
        return WorkflowClient.Status.valueOf(rootNode.get("state").asText());
    }

    /**
     * The model used for this workflow instance
     * @return  the model id
     */
    public String getModelId(){
        if (rootNode.get("model") == null)return null;
        return rootNode.get("model").asText();
    }

    /**
     * The type of payload reference ,either URL or JCR_PATH
     * @return the payload type
     */
    public String getPayloadType(){
        if (rootNode.get("payloadType") == null)return null;
        return rootNode.get("payloadType").asText();
    }

    /**
     * the payload reference
     * @return the url or jcr path
     */
    public String getPayload(){
        if (rootNode.get("payload") == null)return null;
        return rootNode.get("payload").asText();
    }

    /**
     * returns who started this workflow instance
     * @return the initiator id
     */
    public String getInitiator(){
        if (rootNode.get("initiator") == null)return null;
        return rootNode.get("initiator").asText();
    }

    /**
     * time when the workflow instance was started
     * @return the start time
     */
    public Date getStartTime() {
        if (rootNode.get("startTime") == null)return null;
        return WorkflowClient.parseJSONDate(rootNode.get("startTime").asText());
    }

    /**
     * time when this instance was completed/aborted
     * @return the end time
     */
    public Date getEndTime()  {
        if (rootNode.get("endTime") == null)return null;
        return WorkflowClient.parseJSONDate(rootNode.get("endTime").asText());
    }

    public List<String> getWorkItemIds(){
        if (rootNode.get("workItems") == null)return null;
        List<String> list = new ArrayList<>();

        JsonNode itemsNode = rootNode.get("workItems");
        for (int i = 0; i < itemsNode.size(); i++){
            list.add(itemsNode.get(i).get("id").asText());
        }
        return list;
    }


    /**
     * A map of all currently active work items belonging to this instance
     * @return  a map of all work items, where the key ist the work item id, and the value is
     * the node id from the corresponding model.
     */
    public Map<String,String> getWorkItems() {
        if (rootNode.get("workItems") == null)return null;
        Map<String,String> map = new HashMap<>();

        JsonNode itemsNode = rootNode.get("workItems");
        for (int i = 0; i < itemsNode.size(); i++){
            JsonNode workItem = itemsNode.get(i);
            map.put(workItem.get("id").asText(), workItem.get("node").asText());
        }
        return map;
    }
}
