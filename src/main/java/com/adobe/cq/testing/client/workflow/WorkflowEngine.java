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

import java.util.Map;

/**
 * Simple helper class to get all available infos return from the workflow engine. The class is initialised by a call
 * to {@link com.adobe.cq.testing.client.WorkflowClient#getWorkflowEngineInfo(int...)}.
 *
 */
public class WorkflowEngine {

    /**
     * Property that contains the state of the workflow engine. The possible states equal the OSGi Service states:
     * "ACTIVE", "ACTIVATING","DEACTIVATING","DESTROYED","DISABLED","ENABLED","REGISTERED","UNSATISFIED"
     */
    public static final String STATE = "state";

    /**
     * Name of the Vendor for this workflow service
     */
    public static final String SERVICE_VENDOR = "service.vendor";

    /**
     * The process id of the workflow engine service
     */
    public static final String SERVICE_PID = "service.pid";

    /**
     * Description of the workflow service
     */
    public static final String SERVICE_DESCR  = "service.description";

    /**
     * The name of hte OSGi Component that implements this service
     */
    public static final String COMPONENT_NAME = "component.name";

    /**
     * The id given to the workflow component
     */
    public static final String COMPONENT_ID = "component.id";

    /**
     * List of all properties returned from requesting the Workflow Engine Status
     */
    private Map<String,String> properties = null;

    /**
     * Teh only constructor
     * @param prop the properties list returned by requesting the server.
     */
    public WorkflowEngine(Map<String,String> prop){
        this.properties = prop;
    }

    /**
     * returns the value of an engine property or null if not defined.
     *
     * @param propName name of the property
     * @return the value of the requested property
     */
    public String getProperty(String propName){
        return properties.get(propName);
    }

    /**
     * returns list of all properties.
     * @return  a map of all properties, where the key is the name of the property.
     */
    public Map<String,String> getAllProperties(){
        return properties;
    }
}