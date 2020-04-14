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
package com.adobe.cq.testing.client.components;

import com.adobe.cq.testing.client.ComponentClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.TimeoutException;

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public abstract class AbstractComponent {
    /**
     * Stores the currently set of edited property values
     */
    protected HashMap<String, String[]> changeProperties = new HashMap<>();

    /**
     * The default location inside a CQ page where the component gets added
     * @deprecated - use the component's {@link #getLocation()} method as this value is not applicable to all websites
     */
    @Deprecated
    public static final String DEFAULT_LOCATION = "/jcr:content/par/";

    /**
     * Path to the parent node of the component
     */
    String parentPath = null;

    /**
     * Name of the component node.
     */
    String name = null;

    /**
     * Reference to the {@link com.adobe.cq.testing.client.FoundationClient } that created
     * this instance
     */
    protected ComponentClient client = null;

    /**
     * The complete path to this component in the repository.
     */
    protected String componentPath = null;

    /**
     * Path to the CQ page that contains this component
     */
    protected String pagePath = null;

    /**
     * relative location inside the page
     */
    protected String location = DEFAULT_LOCATION;

    /**
     * contains the Json Structure as it is currently saved on the server
     */
    protected JsonNode jsonNode = null;

    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The ComponentClient that will be used for sending the requests.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
*                 occurs. The {@link #getName()} method will return the correct name after {@link #create
*                 (order,int...)} has been called.
     */
    public AbstractComponent(ComponentClient client, String pagePath,
                                       String location, String nameHint) {
        if (location != null) {
            this.location = location;
        }

        this.parentPath = pagePath + this.location;

        if (nameHint == null) {
            this.name = this.getClass().getSimpleName().toLowerCase();
        } else {
            this.name = nameHint;
        }

        this.componentPath = this.parentPath + this.name;
        this.pagePath = pagePath;
        this.client = client;
    }

    /**
     * Creates the component on the server by sending the http request.
     * @param order          Defines where the component should be added in relation to its siblings. Possible values
     *                       are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 201 is assumed.
     * @return Sling response
     * @throws ClientException if something fails during the request/response cycle
     * @throws InterruptedException to mark this method as waiting
     */
    public SlingHttpResponse create(String order, int... expectedStatus) throws ClientException, InterruptedException {
        // get the default setting 
        SlingHttpResponse exec = client.doPost(parentPath, getCreateFormEntity(order).build());
        String errorMessage = "Creating a '" + this.getClass().getSimpleName() + "' component below " +
                getParentPath() + " failed!";
        HttpUtils.verifyHttpStatus(exec, errorMessage, HttpUtils.getExpectedStatus(SC_CREATED, expectedStatus));
        // store the path that was really used
        componentPath = exec.getSlingPath();
        // store the name that was really used
        name = componentPath.substring(componentPath.lastIndexOf("/") + 1);
        // parse all known props and store them in the properties list
        getComponentNode();
        return exec;
    }

    /**
     * Returns the FormEntityBuilder used for the create request in {@link #create(String,int...)}. This method can be
     * overridden in a subclass to extend the parameters that need to sent with the creation request.
     * @return              An FormEntityBuilder with {@code _charset_},{@code ./sling:resourceType} and
     *                      {@code :nameHint} set.
     * @param order         Defines where the component should be added in relation to its siblings. Possible values
     *                      are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     */
    public FormEntityBuilder getCreateFormEntity(String order) {
        // build the form data for the request
        FormEntityBuilder fBuilder = FormEntityBuilder.create()
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("./sling:resourceType", getResourceType())
                .addParameter(":nameHint", getName())
                .addParameter("./jcr:created", "")
                .addParameter("./jcr:createdBy", "")
                .addParameter("./jcr:lastModified", "")
                .addParameter("./jcr:lastModifiedBy", "");
        // if order is set
        if (order != null && !order.equals("")){
            fBuilder.addParameter(":order",order);
        }

        return fBuilder;
    }

    /**
     * Submits all editable values to the server. Same as pressing the {@code OK} button on the edit dialog
     * of a component.
     *
     * @param expectedStatus list of allowed HTTP Status to be returned. if not set, status 200 is assumed
     * @return Sling response
     * @throws ClientException If something fails during request/response cycle
     * @throws InterruptedException to mark this method as waiting
     */
    public SlingHttpResponse save(int... expectedStatus) throws ClientException, InterruptedException {
        // Start the form
        FormEntityBuilder form = FormEntityBuilder.create()
                .addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .addParameter("./sling:resourceType", getResourceType());
        // add all properties
        for (String key : changeProperties.keySet()) {
            String[] values = changeProperties.get(key);
            for (String value : values) {
                if(!key.startsWith(":")){
                    form.addParameter("./" + key, value);
                }
            }
        }
        // execute the request
        SlingHttpResponse exec = client.doPost(componentPath, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // parse all known props and jsonNode and store them in the properties list
        getComponentNode();
        // the sling response wrapper
        return exec;
    }

    /**
     * Allows reordering of a component in relation to its siblings.
     * @param order          Defines where the component should be added in relation to its siblings. Possible values
     *                       are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     * @param expectedStatus list of allowed HTTP Status to be returned. if not set, status 200 is assumed
     * @return Sling response
     * @throws ClientException if posting the reorder fails
     * @throws InterruptedException to mark this method as waiting
     */
    public SlingHttpResponse reorder(String order, int... expectedStatus) throws ClientException, InterruptedException {
        // Start the form
        FormEntityBuilder form = FormEntityBuilder.create().addParameter(":order", order);
        // execute the request
        SlingHttpResponse exec = client.doPost(componentPath, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
        // parse all known props and jsonNode and store them in the properties list
        getComponentNode();
        // the sling response wrapper
        return exec;
    }

    /**
     * sets a string or multiple string property to be saved.
     *
     * @param name  name of the property to set e.g. ./text.
     * @param value the value(s) to be set.
     */
    public void setProperty(String name, String... value) {
        changeProperties.put(name, value);
    }

    /**
     * sets a integer to be saved.
     *
     * @param name  name of the property to set e.g. {@code ./width}.
     * @param value the value(s) to be set.
     */
    public void setProperty(String name, int value) {
        changeProperties.put(name, new String[]{Integer.toString(value)});
    }

    /**
     * Returns the property value as string, as it is saved at the moment on the server.
     *
     * @param propName  name of the property
     * @return prop value on server or {@code null} if no such property has been saved yet.
     */
    public String getPropertyAsString(String propName) {
        JsonNode propNode = getJsonNode(propName);
        // if it is not set return null
        if (propNode == null) return null;
        // if its a multi value property
        if (propNode.isArray()) {
            ArrayNode array = ((ArrayNode) propNode);
            // check if array is empty
            if (array.size() == 0) return null;
            // return only the first value
            return array.get(0).getValueAsText();
        }
        return getJsonNode(propName).getValueAsText();
    }

    /**
     * Returns the property value as string array, as it is saved at the moment on the server.
     *
     * @param propName  name of the property
     * @return value on server or {@code null} if no such property has been saved yet.
     */
    public String[] getPropertyAsStringArray(String propName) {
        return getStringArrayFromJsonNode(getJsonNode(propName));
    }

    /**
     * returns a components property as JsonNode.
     *
     * @param propName the property to get
     * @return jsonNode of the property stored
     */
    public JsonNode getProperty(String propName) {
        return getJsonNode(propName);
    }

    /**
     * Returns the root node of the component node as a JsonNode
     *
     * @return a JsonNode of the component node
     * @throws ClientException if something fails during request
     * @throws InterruptedException to mark this method as waiting
     */
    public JsonNode getComponentNode() throws ClientException, InterruptedException {
        try {
            client.waitExists(getComponentPath(), 60000, 100);
        } catch (TimeoutException e) {
            throw new ClientException("Component does not exist " + getComponentPath(), e);
        }
        jsonNode = client.doGetJson(getComponentPath(), -1);
        return jsonNode;
    }


    private String[] getStringArrayFromJsonNode(JsonNode propNode) {
        // if it is not set return null
        if (propNode == null) return null;
        // if its a not an array
        if (!propNode.isArray()) {
            // as string array with one entry
            return new String[]{propNode.getValueAsText()};
        }
        ArrayNode array = ((ArrayNode) propNode);
        // check if array is empty
        if (array.size() == 0) return null;
        // return values
        String[] values = new String[array.size()];
        for (int i = 0; i < array.size(); i++) {
            values[i] = array.get(i).getValueAsText();
        }
        return values;
    }


    /**
     * Returns the name of the component node. Gets preset with the nameHint passed in the constructor and
     * updated if required after the create() request.
     *
     * @return Component node name
     */
    public String getName() {
        return name;
    }

    /**
     * Path to the parent node of the component
     *
     * @return Node path
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * The complete path to the component.
     *
     * @return node path
     */
    public String getComponentPath() {
        return componentPath;
    }

    /**
     * returns the path to the page that contains the component
     *
     * @return  page path
     */
    public String getPagePath() {
        return pagePath;
    }

    /**
     * returns the relative path inside the page
     * @return relative node path
     */
    public String getLocation() {
        return location;
    }

    /**
     * The resource type for the component, e.g. foundation/components/text. this will be set in the
     * {@code sling:resourceType } property of the node.
     *
     * @return the resource type
     */
    public abstract String getResourceType();

    /**
     * Returns the client that created this component.
     *
     * @return  the Component Client
     */
    public ComponentClient getClient() {
        return client;
    }

    /**
     * Get the JsonNode of a property. The property can have multiple levels.
     *
     * @param propName  the name of the property
     * @return  the json node for the property
     */
    private JsonNode getJsonNode(String propName) {
        StringTokenizer st = new StringTokenizer(propName, "/");
        JsonNode prop = jsonNode;
        while (st.hasMoreElements()) {
            prop = prop.get((String) st.nextElement());
        }
        return prop;
    }

    /**
     * Returns the component wrapper instance for the following sibling or null if this component
     * is already the last one. It looks for the sling:resourceType property an tries to find a matching
     * component wrapper that has been registered with the ComponentClient (see  {@link
     * com.adobe.cq.testing.client.ComponentClient#registerComponent(String, Class)})
     *
     * @param <T> component type
     * @return A component wrapper instance or null if the node has no or an unknown resource type.
     * @throws ClientException  if something goes wrong during request/response of json.
     * @throws InterruptedException to mark this method as waiting
     */
    public <T extends AbstractComponent>T getNext() throws ClientException, InterruptedException {
        String nextPath =  getNextNodePath();
        if (nextPath == null) return null;
        return client.getComponent(nextPath);
    }

    /**
     * Returns the component path for the following sibling or null if this component is already 
     * the last one. 
     * @return The component path to the following sibling.
     * @throws ClientException  if something goes wrong during request/response of json.
     * @throws InterruptedException to mark this method as waiting
     */
    public String getNextNodePath() throws ClientException, InterruptedException {
        // get the parent node
        try {
            client.waitExists(getParentPath(), 60000, 100);
        } catch (TimeoutException e) {
            throw new ClientException("Parent path does not exist " + getComponentPath(), e);
        }
        JsonNode parNode = client.doGetJson(getParentPath(), 1);
        // go through the list of field names
        for(Iterator<String> it = parNode.getFieldNames();it.hasNext();){
            // get next field name
            String testName = it.next();
            // if the field references an object 
            if (parNode.get(testName).isObject()){
                // if the field name the name of this node
                if (testName.equals(getName())){
                    // if there is a next entry
                    if (it.hasNext()){
                        return getParentPath() + it.next();
                    }else{
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * returns the first child node that is a component or null if not found.
     *
     * @param <T> a subclass of AbstractComponent
     * @return an Component wrapper or null if it has no child components or the
     * component type is unknown
     * @throws ClientException if requesting json fails.
     * @throws InterruptedException to mark this method as waiting
     */
    public <T extends AbstractComponent>T getFirstChild() throws ClientException, InterruptedException {
        // shortcut
        JsonNode parentNode = getComponentNode();
        // go trough the fields of the component node
        for(Iterator<String> it = parentNode.getFieldNames();it.hasNext();){
            // get the next field name
            String childName = it.next();
            // get the next field  node
            JsonNode child = parentNode.get(childName);
            // if the next field is an object and has a sling resource type property
            if (child.isObject() && !child.get("sling:resourceType").isMissingNode()){
                return client.getComponent(getComponentPath() + "/" +  childName );
            }
        }

        // nothing found return null
        return null;
    }

    /**
     * Returns the component wrapper instance for the previous sibling or null if this component
     * is already the first one. It looks for the sling:resourceType property an tries to find a matching
     * component wrapper that has been registered with the ComponentClient (see  {@link
     * com.adobe.cq.testing.client.ComponentClient#registerComponent(String, Class)})
     *
     * @param <T> component type
     * @return A component wrapper instance or null if the node has no or an unknown resource type.
     * @throws ClientException  if something goes wrong during request/response of json
     * @throws InterruptedException to mark this method as waiting
     */
    public <T extends AbstractComponent>T getPrevious() throws ClientException, InterruptedException {
        String prevPath =  getPreviousNodePath();
        if (prevPath == null) return null;
        return client.getComponent(prevPath);
    }

    /**
     * Returns the component path for the previous sibling or null if this component is already
     * the last one.
     * @return The component path to the following sibling.
     * @throws ClientException  if something goes wrong during request/response of json.
     * @throws InterruptedException to mark this method as waiting
     */
    public String getPreviousNodePath() throws ClientException, InterruptedException {
        // get the parent node
        try {
            client.waitExists(getParentPath(), 60000, 100);
        } catch (TimeoutException e) {
            throw new ClientException("Component does not exist " + getComponentPath(), e);
        }
        JsonNode parNode = client.doGetJson(getParentPath(), 1);
        String prevNodeName = null;
        // go through the list of field names
        for(Iterator<String> it = parNode.getFieldNames();it.hasNext();){
            // get next field name
            String testName = it.next();
            // if the field references an object 
            if (parNode.get(testName).isObject()){
                // if the field name is the current components name
                if (testName.equals(getName())){
                    // if the node is not at first location
                    if (prevNodeName != null){
                        return getParentPath() + prevNodeName;
                    } else {
                        return null;
                    }
                }    else {
                    // store it as the possible previous value
                    prevNodeName = testName;
                }
            }
        }
        return null;
    }
}
