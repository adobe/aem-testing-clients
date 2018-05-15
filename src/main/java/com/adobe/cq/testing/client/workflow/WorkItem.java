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
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple wrapper around the Work Item info returned by the server
 */
public class WorkItem {

    private JsonNode rootNode = null;

    /**
     * the only constructor
     *
     * @param rootNode the JSON node containing all the infos
     */
    public WorkItem(JsonNode rootNode){
        this.rootNode = rootNode;
    }

    /**
     * the id of this work item , equals its  URI
     * @return the id
     */
    public String getId() {
        if (rootNode.get("id") == null)return null;
        return rootNode.get("id").getValueAsText();
    }

    /**
     * the URI of the workflow instance this work item belongs to
     * @return the instance id
     */
    public String getInstanceURI() {
        if (rootNode.get("instance") == null)return null;
        return rootNode.get("instance").getValueAsText();
    }

    /**
     * the model that was used to create the workflow instance this work item belongs to.
     *
     * @return the model id
     */
    public String getModelId() {
        if (rootNode.get("model") == null)return null;
        return rootNode.get("model").getValueAsText();
    }

    /**
     * the node in the model that is associated with this work item
     * @return node id
     */
    public String getNodeId() {
        if (rootNode.get("node") == null)return null;
        return rootNode.get("node").getValueAsText();
    }

    /**
     * The start date of this work item
     * @return start date
     */
    public Date getStartDate(){
        if (rootNode.get("startTime") == null)return null;
        return WorkflowClient.parseJSONDate(rootNode.get("startTime").getValueAsText());
    }

    /**
     * The type of payload managed by this work item, either URL or JCR_PATH
     * @return type of payload
     */
    public String getPayloadType(){
        if (rootNode.get("payloadType") == null)return null;
        return rootNode.get("payloadType").getValueAsText();
    }

    /**
     * The payload reference
     * @return the payload reference
     */
    public String getPayLoad(){
        if (rootNode.get("payload") == null)return null;
        return rootNode.get("payload").getValueAsText();
    }

    /**
     * A map of all properties stored in this work item
     *
     * @return map of all properties , where the key is the name of the property.
     */
    public Map getProperties(){
        if (rootNode.get("properties") == null)return null;
        JsonNode properties = rootNode.get("properties");
        HashMap<String,String> map = new HashMap<>();
        for (int i = 0 ; i < properties.size();i++){
            JsonNode property = properties.get(i);
            map.put(property.get("key").getValueAsText(),property.get("value").getValueAsText());
        }
        return map;
    }

    /**
     * list of all possible routes from this work item
     *
     * @return a list of {@link Route} objects
     */
    public ArrayList<Route> getRoutes(){
        if (rootNode.get("routes") == null)return null;
        JsonNode routes = rootNode.get("routes");
        ArrayList<Route> list = new ArrayList<>();
        for (int i = 0; i < routes.size();i++){
            list.add(new Route(routes.get(i)));
        }
        return list;
    }

    /**
     * Lost of all user/groups this work item can be delegated to.
     *
     * @return list of {@link Delegatee} objects.
     */
    public ArrayList<Delegatee> getDelegatees(){
        if (rootNode.get("delegatees") == null)return null;
        JsonNode delegatees = rootNode.get("delegatees");
        ArrayList<Delegatee> list = new ArrayList<>();
        for (int i = 0; i < delegatees.size();i++){
            list.add(new Delegatee(delegatees.get(i)));
        }
        return list;
    }

    /**
     * Simple inner class wrapping a Delegatee
     */
    public class Delegatee{

        private JsonNode delNode = null;

        /**
         * The only constructor
         * @param node the JSON node containing all the infos
         */
        protected Delegatee(JsonNode node){
            delNode = node;
        }

        /**
         * the user / group id
         * @return the id
         */
        public String getId(){
            if (delNode.get("id") == null)return null;
            return delNode.get("id").getValueAsText();
        }

        /**
         * The user/groups name
         * @return the name
         */
        public String getName(){
            if (delNode.get("name") == null)return null;
            return delNode.get("name").getValueAsText();
        }

        /**
         * the type of the participant either USER or GROUP
         * @return type of participant
         */
        public String getType(){
            if (delNode.get("type") == null)return null;
            return delNode.get("type").getValueAsText();
        }
    }

    /**
     * Simple inner class wrapping one route
     */
    public class Route{

        private JsonNode routeNode = null;

        /**
         * The only constructor
         *
         * @param node the JSON node containing all the information
         */
        protected Route(JsonNode node){
            routeNode = node;
        }

        /**
         * the id of the route
         * @return the id
         */
        public String getId(){
            if (routeNode.get("id") == null)return null;
            return routeNode.get("id").getValueAsText();
        }

        /**
         * The name of this route
         * @return the name
         */
        public String getName(){
            if (routeNode.get("name") == null)return null;
            return routeNode.get("name").getValueAsText();
        }

        /**
         * Map of all possible destinations
         * @return map where the key is the id of the destination, the value is the title of the destination
         */
        public Map<String, String> getDestinations(){
            if (routeNode.get("destinations") == null)return null;
            JsonNode destinations = routeNode.get("destinations");
            HashMap<String,String> map = new HashMap<>();
            for (int i = 0; i < destinations.size();i++){
                JsonNode destination = destinations.get(i);
                map.put(destination.get("id").getValueAsText(),destination.get("title").getTextValue());
            }
            return map;
        }
    }

}