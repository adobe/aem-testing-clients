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
package com.adobe.cq.testing.client.components.foundation.form;

import com.adobe.cq.testing.client.ComponentClient;
import com.adobe.cq.testing.client.components.AbstractComponent;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Start extends AbstractComponent {

    public static final String RESOURCE_TYPE = "foundation/components/form/start";
    public static final String PROP_REDIRECT = "redirect";
    public static final String PROP_WORKFLOW = "workflowModel";
    public static final String PROP_ACTION_TYPE = "actionType";
    public static final String PROP_FORM_ID = "formid";
    public static final String PROP_CLIENT_VALIDATION = "clientValidation";
    public static final String PROP_VALIDATION_RT = "validationRT";

    public static final String OPT_ACTION_TYPE_STORE = "foundation/components/form/actions/store";
    public static final String PROP_ACTION_STORE_CONTENT_PATH = "action";

    public static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    private End endComponent = null;
    private HashMap<String,Object> formValues = new HashMap<>();

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
    public Start(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }


    @Override
    public FormEntityBuilder getCreateFormEntity(String order) {
        return super.getCreateFormEntity(order)
                // The following param is required to get the proper styles inherited
                .addParameter("parentResourceType", "foundation/components/parsys");

    }

    /**
     * We override the default create call, so we can capture the reference to the end component.
     * The end component is created right after the start component.
     *
     * @param order          Defines where the component should be added in relation to its siblings. Possible values
     *                       are {@code first}, {@code last}, {@code before [nodeName]}, {@code after [nodeName]}.
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 201 is assumed.
     * @return Sling response
     * @throws ClientException if the request fails
     */
    @Override
    public SlingHttpResponse create(String order, int... expectedStatus) throws ClientException, InterruptedException {
        SlingHttpResponse res = super.create(order, expectedStatus);
        endComponent = getNext();
        endComponent.setStartComponent(this);
        return res;
    }

    /**
     * Returns the end form component that belongs to this start component.
     * @return The end component for this start component
     */
    public End getFormEndComponent() {
        return endComponent;
    }

    /**
     * Sets the handle for the redirect after form submit. e.g. /content/site/thank_you
     *
     * @param pageHandle  handle to redirect page
     */
    public void setRedirect(String pageHandle){
        setProperty(PROP_REDIRECT, pageHandle);
    }

    /**
     * Set workflow model that should be started on form submission.
     *
     * @param modelHandle  handle of of the workflow model
     */
    public void setWorkflowModel(String modelHandle){
        setProperty(PROP_WORKFLOW, modelHandle + "/jcr:content/model");
    }

    /**
     * Defines what type of action should be executed upon form submission. actions are implemented as components
     * so this value must be a handle to a node in the repository that has sling:resourceType set to
     * foundation/components/form/action.
     *
     * @param actionTypeHandle handle to the action component
     */
    public void setActionType(String actionTypeHandle){
        setProperty(PROP_ACTION_TYPE, actionTypeHandle);
    }

    /**
     * Sets the form id
     *
     * @param formId name for the form id
     */
    public void setFormId(String formId){
        setProperty(PROP_FORM_ID, formId);
    }

    /**
     * Convenience method that returns the form id
     * @return the form id set, otherwise null
     */
    public String getFormId(){
        return getPropertyAsString(PROP_FORM_ID);
    }

    /**
     * Defines if the form should get validated client side before submission
     *
     * @param validate set true if client validation of form should occur.
     */
    public void setClientValidation(boolean validate){
        if(validate){
            setProperty(PROP_CLIENT_VALIDATION, "true");
        } else {
            setProperty(PROP_CLIENT_VALIDATION + "@Delete", "true");
            //setProperty(PROP_CLIENT_VALIDATION, "");
        }
    }

    /**
     * Defines a form validation resource type if you want to validate the entire form (instead of individual fields).
     *
     * @param resourceType  the validation resource type
     */
    public void setClientValidationResourceType(String resourceType){
        setProperty(PROP_VALIDATION_RT,resourceType);
    }

    /**
     * Sets action type to 'Store Content', and sets the 'Action' path where the
     * data will get stored.
     *
     * @param contentPath path where the data will be stored
     */
    public void setFormActionStore(String contentPath){
        setProperty(PROP_ACTION_TYPE, OPT_ACTION_TYPE_STORE);
        setProperty(PROP_ACTION_STORE_CONTENT_PATH,contentPath);
    }

    /**
     * Get the action property value (which is the action store content path) from this start component.
     *
     * @return the action property value
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public String getFormActionStore() throws ClientException, InterruptedException {
        try {
            client.waitExists(getComponentPath(), 60000, 100);
        } catch (TimeoutException e) {
            throw new ClientException("Component does not exist " + getComponentPath(), e);
        }
        JsonNode node  = client.doGetJson(getComponentPath(), 1);
        JsonNode action = node.get(PROP_ACTION_STORE_CONTENT_PATH);
        return action.asText();
    }

    /**
     * this method will 'emulate' the submit of this form by rebuilding the
     * multipart/form-data request, collecting all the values from the form fields
     * found between the start and end component
     *
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public void submit() throws ClientException, InterruptedException {
        client.doPost(getPagePath() + ".html", createMultipartEntity());
    }

    /**
     * this method will 'emulate' the submit of this form by rebuilding the
     * multipart/form-data request, collecting all the values from the form fields
     * found between the start and end component

     * @return the sling http response
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public SlingHttpResponse submit2() throws ClientException, InterruptedException {
        return client.doPost(getPagePath() + ".html", createMultipartEntity());
    }



    /**
     * Create a MultiPartEntity out of the all the values of the Form components
     * which are between this Start component and its End component in the page paragraph list.
     * This MultiPartEntity is used to submit the form data to the server.
     * Accepted as Form values are:
     * - String for single value parameter if the parameter name is the same as the component name
     * - String Array for multi value parameters if the parameter name is the same as the component name
     * - File for File Uploads
     * - Map of Strings for Form components which are sending several parameters (e.g Captcha)
     *
     * @return the MultipartEntity with the Form values.
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public HttpEntity createMultipartEntity() throws ClientException, InterruptedException {
        // Use UTF-8 instead of default ISO-8859-1 as we change default Sling encoding to UTF-8
        ContentType textContentType = ContentType.create("text/plain", Consts.UTF_8);

        // start a new multipart entity
        MultipartEntityBuilder meb = MultipartEntityBuilder.create();

        // get the start component
        AbstractComponent next = this;
        // the form id must be sent with if set
        String formId = next.getPropertyAsString(Start.PROP_FORM_ID);
        if (formId != null){
            meb.addTextBody(":" + Start.PROP_FORM_ID, formId, textContentType);
        }
        // the redirect must be set as a control value, not a form value as well
        String redirect = next.getPropertyAsString(Start.PROP_REDIRECT);
        if (redirect != null){
            meb.addTextBody(":" + Start.PROP_REDIRECT, redirect, textContentType);
        }
        // reference to the form start component must be sent with
        meb.addTextBody(":formstart", getComponentPath(), textContentType);
        // the encoding
        meb.addTextBody(":_charset_", "UTF-8", textContentType);
        meb.addTextBody(":Submit", "Submit", textContentType);

        // go through all the following components
        while(next.getNext() != null) {
            // get the next component
            next = next.getNext();
            // if the next component is this end component
            if (next.getName().equals(getFormEndComponent().getName())) {
                // we have reach the end of the form, exit while loop
                break;
            }
            // if the next element is not a subclass of AbstractFormFieldComponent , check next
            if (!(next instanceof AbstractFormFieldComponent)) continue;
            // get the submit value stored for this element if any
            Object value = formValues.get(next.getName());
            // if no value was set get next component
            if (value == null) continue;
            // get the field name
            String fieldName = ((AbstractFormFieldComponent) next).getElementName();
            // if the value is a string
            if (value instanceof String){
                // add the value to the multipart submit
                meb.addTextBody(fieldName, (String) value, textContentType);
            }
            // if we have multiple values
            else if (value instanceof String[]) {
                String[] values = (String[]) value;
                for (String v : values) {
                    // add the value to the multipart submit
                    meb.addTextBody(fieldName, v, textContentType);
                }
            }
            // if we have a file upload
            else if (value instanceof File) {
                String name = ((File) value).getName();
                FileBody fileBody = new FileBody((File) value);
                meb.addPart(name, fileBody);
            }
            // if the value is a Map of Strings
            else if (value instanceof Map) {
                for (Object o : ((Map) value).keySet()) {
                    String key = (String) o;
                    Object v = ((Map) value).get(key);

                    if (v instanceof String) {
                        // add the value to the multipart submit
                        meb.addTextBody(key, (String) v, textContentType);
                    }
                    // if we have multiple values
                    else if (v instanceof String[]) {
                        String[] values = (String[]) v;
                        for (String val : values) {
                            // add the value to the multipart submit
                            meb.addTextBody(key, val, textContentType);
                        }
                    }
                }
            }
        }
        // if we reached the end of the list instead of the end component
        // the start and end component are in mismatch and we don't submit.
        if (!next.getName().equals(getFormEndComponent().getName())){
            throw new ClientException("Start and End Form Components do not match, not submitting!");
        }

        return meb.build();
    }

    /**
     * The value we want to submit for a form field. The value is only submitted
     * if the corresponding form field component still exists at time when
     * {@link #submit()} gets called and is located between the form start and
     * end component.
     *
     * If the the value is not set or set to null at the time of submitting
     * then the form field is not added at all.
     *
     * If set to an empty String, it will submitted as such.
     *
     * @param componentName the name of the form field component, associated with this value
     * @param value the value send upon submit of the form
     */
    public void setFormValueToSubmit(String componentName,Object value) {
        // if the value is null, remove it completely
        if (value == null){
            formValues.remove(componentName);
        }
        formValues.put(componentName,value);
    }

    /**
     * returns the string value set for this form element
     *
     * @param componentName component name
     * @return the string value set
     * @throws ClientException if the request fails
     */
    public String getFormValue(String componentName) throws ClientException {
        Object value = formValues.get(componentName);
        if (value instanceof String){
            return (String) value;
        } else {
            throw new ClientException("Requested Form Value is not a String!");
        }
    }
}
