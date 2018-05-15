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

import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A simple wrapper around the JSON representing a workflow model requested from the server.
 */
public class WorkflowModel {

    private JsonNode rootNode = null;

    /**
     * The only constructor
     *
     * @param rootNode the root JSON node containing all the info
     */
    public WorkflowModel(JsonNode rootNode){
        this.rootNode = rootNode;
    }

    /**
     * The id of this model
     * @return the id
     */
    public String getId(){
        if (rootNode.get("id") == null)return null;
        return rootNode.get("id").getValueAsText();
    }

    /**
     * The title of this model
     * @return the title
     */
    public String getTitle(){
        if (rootNode.get("title") == null)return null;
        return rootNode.get("title").getValueAsText();
    }

    /**
     * the description of this model
     * @return the description
     */
    public String getDescription(){
        if (rootNode.get("description") == null)return null;
        return rootNode.get("description").getValueAsText();
    }

    /**
     * the version of this model
     *
     * @return the version
     */
    public String getVersion(){
        if (rootNode.get("version") == null)return null;
        return rootNode.get("version").getValueAsText();
    }


    /**
     * the major version number, usually 1
     * @return the major version number
     */
    public int getVersionMajor(){
        if (getVersion() == null )return -1;
        return Integer.parseInt(getVersion().split("\\.")[0]);
    }

    /**
     * the minor version number.
     *
     * @return the minor version number
     */
    public int getVersionMinor(){
        if (getVersion() == null )return -1;
        return Integer.parseInt(getVersion().split("\\.")[1]);
    }

    /**
     * Any meta data stored in this model
     *
     * @return Map of metadata, where key is the properties name.
     */
    public Map getMetaData(){
        return getFieldsMap(rootNode.get("metaData"));
    }


    /**
     * Returns a map of all nodes defined in this model
     * @return a map of all nodes where the key is the node id, and the value is a {@link Node} object.
     */
    public Map<String, Node> getNodes(){
        if (rootNode.get("nodes") == null)return null;
        Map<String, Node> map = new HashMap<>();
        JsonNode nodes = rootNode.get("nodes");
        for (int i = 0;i < nodes.size();i++){
            JsonNode n = nodes.get(i);
            map.put(n.get("id").getValueAsText(),new Node(n));
        }
        return map;
    }

    public Node getNodeById(String nodeId){
        if (rootNode.get("nodes") == null)return null;
        JsonNode nodes = rootNode.get("nodes");
        for (int i = 0;i < nodes.size();i++){
            if (nodes.get(i).get("id").getValueAsText().equals(nodeId)){
                return new Node(nodes.get(i));
            }
        }
        return null;
    }

    /**
     * Returns a list of all transitions defined between the nodes.
     *
     * @return List of {@link Transition} objects
     */
    public ArrayList<Transition> getTransitions(){
        if (rootNode.get("transitions") == null)return null;
        ArrayList<Transition> list = new ArrayList<>();
        JsonNode transitions = rootNode.get("transitions");
        for (int i = 0; i < transitions.size();i++){
            list.add(new Transition(transitions.get(i)));
        }
        return list;
    }

    /**
     * Simple inner class wrapping a JSON node defining a model node
     */
    public class Node{

        private JsonNode nodeNode = null;

        /**
         * The only constructor
         * @param node the JSON node containing all the info
         */
        protected Node(JsonNode node){
            nodeNode = node;
        }

        /**
         * the id of this node
         * @return the id
         */
        public String getId(){
            if (nodeNode.get("id") == null)return null;
            return nodeNode.get("id").getValueAsText();
        }

        /**
         * The type of this node
         * @return node type
         */
        public String getType(){
            if (nodeNode.get("type") == null)return null;
            return nodeNode.get("type").getValueAsText();
        }

        /**
         * The title set for this node
         * @return node title
         */
        public String getTitle(){
            if (nodeNode.get("title") == null)return null;
            return nodeNode.get("title").getValueAsText();
        }

        /**
         * the description set for this node
         * @return the description
         */
        public String getDescription(){
            if (nodeNode.get("description") == null)return null;
            return nodeNode.get("description").getValueAsText();
        }

        /**
         * A map of all meta data properties stored in this node
         * @return  a map of all meta data properties, where the key is the name of the property.
         */
        public Map<String,String> getMetaData() {
            return getFieldsMap(nodeNode.get("metaData"));
        }
    }

    /**
     * Simple inner class wrapping a JSON node defining a transition between nodes
     */
    public class Transition {

        private JsonNode transitionNode;

        /**
         * The only constructor
         * @param node the JSON node containing the transition info
         */
        protected Transition(JsonNode node){
            this.transitionNode = node;
        }

        /**
         * From with node this transition starts
         * @return the node id
         */
        public String getFrom(){
            if (transitionNode.get("from") == null)return null;
            return transitionNode.get("from").getValueAsText();
        }

        /**
         * to which node this transition goes
         * @return the node id
         */
        public String getTo(){
            if (transitionNode.get("to") == null)return null;
            return transitionNode.get("to").getValueAsText();
        }

        /**
         * Any rule defined for traversal
         * @return the rule
         */
        public String getRule(){
            if (transitionNode.get("rule") == null)return null;
            return transitionNode.get("rule").getValueAsText();
        }

        /**
         * A map of all meta data properties stored for this transition
         *
         * @return a map of all meta data properties, where the key is the name of the property
         */
        public Map getMetaData(){
            if (transitionNode.get("metaData") == null)return null;
            HashMap<String,String> map = new HashMap<>();
            JsonNode metaData = transitionNode.get("metaData");
            for(Iterator<String> it = metaData.getFieldNames();it.hasNext();){
                String key = it.next();
                map.put(key,metaData.get(key).getValueAsText());
            }
            return map;
        }
    }

    protected Map<String, String> getFieldsMap(JsonNode node) {
        if (node == null) return null;

        HashMap<String,String> map = new HashMap<>();

        for(Iterator<String> it = node.getFieldNames(); it.hasNext();) {
            String key = it.next();
            map.put(key, node.get(key).getValueAsText());
        }

        return map;
    }
}
