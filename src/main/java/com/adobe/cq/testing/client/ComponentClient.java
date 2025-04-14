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
package com.adobe.cq.testing.client;

import com.adobe.cq.testing.client.components.AbstractComponent;
import com.adobe.cq.testing.client.registry.ComponentRegistry;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.lang.reflect.Constructor;
import java.net.URI;


public class ComponentClient extends CQClient {

    /**
     * The key name for the default 'relativeLocation' component property
     *
     * <p>Used as a default for the {@link AbstractComponent#getLocation()} method.</p>
     */
    private static final String CONFIG_KEY_RELATIVE_LOCATION = "componentDefaultRelativeLocation";

    public static final String ORDER_FIRST = "first";
    public static final String ORDER_LAST = "last";
    public static final String ORDER_BEFORE_PREFIX = "before ";
    public static final String ORDER_AFTER_PREFIX = "after ";

    public ComponentClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public ComponentClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Adds a new, empty component to a CQ page. The component is added at the default
     * location in the page inside the page.
     *
     * @param componentClass the {@link com.adobe.cq.testing.client.components.AbstractComponent}
     *                       subclass to be created
     * @param pagePath       path to the page where the component gets added
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @param <T>            A subclass of {@link com.adobe.cq.testing.client.components.AbstractComponent}
     * @return A instance of the class passed in {@code componentClass}
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public <T extends AbstractComponent> T addComponent(Class<T> componentClass, String pagePath, int... expectedStatus)
            throws ClientException, InterruptedException {
        // call the generic version
        return addComponent(componentClass, pagePath, null, null,null, expectedStatus);
    }

    /**
     * Adds a new, empty component to a CQ page. The component is added at the default
     * location in the page inside the page.
     *
     * @param componentClass the {@link com.adobe.cq.testing.client.components.AbstractComponent}
     *                       subclass to be created
     * @param pagePath       path to the page where the component gets added
     * @param order          Defines where the component should be added in relation to its siblings. Possible values
     *                       are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @param <T>            A subclass of {@link com.adobe.cq.testing.client.components.AbstractComponent}
     * @return A instance of the class passed in {@code componentClass}
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public <T extends AbstractComponent> T addComponent(Class<T> componentClass, String pagePath,String order, int... expectedStatus)
            throws ClientException, InterruptedException {
        // call the generic version
        return addComponent(componentClass, pagePath, null, null,order, expectedStatus);
    }

    /**
     * Adds a new, empty component to a CQ page.
     *
     * @param componentClass the {@link com.adobe.cq.testing.client.components.AbstractComponent} subclass to be
     *                       created
     * @param pagePath       path to the page where the component gets added
     * @param location       relative path inside the page where the component gets added
     * @param nameHint       proposed node for the new component node. Might get altered if the name if there is
     *                       already
     *                       a node with the same name on the same level. {@link AbstractComponent#getName()}
     *                       returns the name as is it is really set.
     * @param order          Defines where the component should be added in relation to its siblings. Possible values
     *                       are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @param <T>            A subclass of {@link com.adobe.cq.testing.client.components.AbstractComponent}
     * @return An instance of the class passed in {@code componentClass}
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public <T extends AbstractComponent> T addComponent(Class<T> componentClass, String pagePath,
                                                        String location, String nameHint, String order,
                                                        int... expectedStatus) throws ClientException, InterruptedException {
        if ( location == null ) {
            location = getValue(CONFIG_KEY_RELATIVE_LOCATION);
        }

        T component;
        try {
            // get the constructor for the component subclass
            Constructor<T> cons = componentClass.getConstructor(ComponentClient.class,
                    String.class, String.class, String.class);
            // execute the constructor
            component = cons.newInstance(this, pagePath, location, nameHint);
        } catch (Exception e) {
            throw new ClientException("Instantiation failed", e);
        }

        // call the create method on the component class
        component.create(order, expectedStatus);

        // return the newly created component
        return component;
    }

    /**
     * Initializes an {@link com.adobe.cq.testing.client.components.AbstractComponent} subclass
     * against an existing foundation component node.
     *
     * @param componentPath complete path to the the repository node that forms the root node of a
     *                      component. Must contain a property named {@code sling:resourceType} referencing
     *                      a valid component.
     * @param compClass     The component wrapper class to be initialized
     * @param <T>           A subclass of {@link com.adobe.cq.testing.client.components.AbstractComponent}
     * @return A instance of the class passed in {@code componentClass}
     * @throws ClientException if the request fails
     */
    public <T extends AbstractComponent> T getComponent(String componentPath, Class<T> compClass)
            throws ClientException {
        // check if path even exists
        if(!this.exists(componentPath)) return null;
        // Get the json for this node
        JsonNode node = adaptTo(JsonClient.class).doGetJson(componentPath, 1);
        // check if the node has a sling:resourceType property
        if (node.get("sling:resourceType").isMissingNode()) {
            throw new ClientException(componentPath + " is not pointing to a foundation component node, no " +
                    "sling:resourceType property was found!");
        }
        return initializeComponent(componentPath,compClass);
    }

    /**
     * Initializes an {@link com.adobe.cq.testing.client.components.AbstractComponent} subclass
     * against an existing foundation component node. t looks for the sling:resourceType property an tries to find a 
     * matching component wrapper that has been registered with the ComponentClient (see  {@link
     * com.adobe.cq.testing.client.ComponentClient#registerComponent(String, Class)})
     *
     * @param componentPath complete path to the the repository node that forms the root node of a
     *                      component. Must contain a property named {@code sling:resourceType} referencing
     *                      a valid component.
     * @param <T>           A subclass of {@link com.adobe.cq.testing.client.components.AbstractComponent}
     * @return A instance of the class passed in {@code componentClass}
     * @throws ClientException if the request fails
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractComponent>T getComponent(String componentPath) throws ClientException {
        // check if path even exists
        if(!this.exists(componentPath)) return null;
        // Get the json for this node
        JsonNode node = adaptTo(JsonClient.class).doGetJson(componentPath, 1);
        // check if the node has a sling:resourceType property
        if (node.get("sling:resourceType").isMissingNode()) {
            throw new ClientException(componentPath + " is not pointing to a foundation component node, no " +
                    "sling:resourceType property was found!");
        }
        // get the class
        String resourceType = node.get("sling:resourceType").textValue();
        Class<? extends AbstractComponent> compClass = ComponentRegistry.getComponentClass(resourceType);
        
        // no class found ?
        if (compClass == null) return null;
        
        // instantiate
        return (T) initializeComponent(componentPath,compClass);
    }

    /**
     * Helper method to execute the constructor of passed class using reflection.
     * 
     * @param componentPath  The path is used to generate the constructor parameters
     * @param compClass   The class to instantiate
     * @param <T>  a subclass of an AbstractComponent
     * @return an instance of the passed class
     * @throws ClientException  if instantiation through reflection fails for any reason.
     */
    private <T extends AbstractComponent>T initializeComponent(String componentPath, Class<T> compClass) throws ClientException {
        try {
            // get the constructor for the component subclass
            Constructor<T> cons = compClass.getConstructor(ComponentClient.class,
                    String.class, String.class, String.class);
            // prepare the constructor parameters
            String[] parts = componentPath.split("/jcr:content");
            String location = "/jcr:content" + parts[1].substring(0, (parts[1].lastIndexOf("/") + 1));
            String name = componentPath.substring(componentPath.lastIndexOf("/") + 1);
            // execute the constructor
            T comp = cons.newInstance(this, parts[0], location, name);
            comp.getComponentNode();
            return comp;
        } catch (Exception e) {
            throw new ClientException("Instantiation failed", e);
        }
    }

    /**
     * Use this method to register additional component wrappers with the client. All wrappers 
     * from the framework are registered by default. The registered components are used by {@link #getComponent(String)}
     * to return the correct wrapper for a given component path, and by the
     * {@link com.adobe.cq.testing.client.components.AbstractComponent#getNext() AbstractComponent.getNext()} and
     * {@link com.adobe.cq.testing.client.components.AbstractComponent#getPrevious() AbstractComponent.getPrevious()}
     * to return the next/previous component
     * @param resourceType  the resource type to register
     * @param c the corresponding component wrapper class
     */
    public void registerComponent(String resourceType, Class<? extends AbstractComponent> c){
        ComponentRegistry.registerComponent(resourceType,c);
    }

    /**
     * Deletes a component.
     * @param delComp The component to delete
     *
     * @throws ClientException  If the delete request fails
     */
    public void deleteComponent(AbstractComponent delComp) throws ClientException {
        deleteComponent(delComp.getComponentPath());
    }

    /**
     * Deletes a component.
     * @param componentPath the path to the components node
     * @throws ClientException  If the delete request fails
     */
    public void deleteComponent(String componentPath) throws ClientException {
        HttpEntity entity = FormEntityBuilder.create().addParameter(":operation","delete").build();
        doPost(componentPath, entity);
    }

    /**
     * @return the value of the component's default relative location, possibly <code>null</code>
     */
    public String getDefaultComponentRelativeLocation() {
        return getValue(CONFIG_KEY_RELATIVE_LOCATION);
    }

    /**
     * Sets a new value for the component's default relative location
     *
     * @param defaultComponentRelativeLocation the new value for the default component relative location
     */
    public void setDefaultComponentRelativeLocation(String defaultComponentRelativeLocation) {
        getValues().put(CONFIG_KEY_RELATIVE_LOCATION, defaultComponentRelativeLocation);
    }
}
