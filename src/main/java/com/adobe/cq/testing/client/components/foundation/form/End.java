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
import org.apache.sling.testing.clients.ClientException;

public class End extends AbstractComponent {

    public static final String RESOURCE_TYPE = "foundation/components/form/end";

    public static final String TYPE_SUBMIT = "submit";
    public static final String TYPE_RESET = "reset";

    public static final String PROP_SUBMIT = "submit";
    public static final String PROP_RESET = "reset";

    public static final String PROP_SUBMIT_DELETE = "submit@Delete";
    public static final String PROP_RESET_DELETE = "reset@Delete";

    public static final String PROP_SUBMIT_TITLE = "jcr:title";
    public static final String PROP_RESET_TITLE = "resetTitle";

    public static final String PROP_DESCRIPTION = "jcr:description";


    private Start startComponent = null;
    
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
    public End(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * Internal method to set the start component that belongs to this end component
     * @param start  reference to the corresponding start component
     */
    protected void setStartComponent(Start start){
        startComponent = start;
    }

    /**
     * Returns the start component wrapper that corresponds to this end component
     * @return returns corresponding start component
     */
    public Start getStartComponent(){
        return startComponent;
    }

    /**
     * Convenience method to create submit and reset buttons for the form.
     *
     * @param submitTitle submit button title
     * @param resetTitle reset button title
     */
    public void setSubmitButtons(String submitTitle, String resetTitle){
        setProperty(PROP_SUBMIT, "true");
        setProperty(PROP_SUBMIT_DELETE, "true");
        setProperty(PROP_RESET, "true");
        setProperty(PROP_RESET_DELETE, "true");
        if (submitTitle != null) {
            setProperty(PROP_SUBMIT_TITLE, submitTitle);
        }
        if (resetTitle != null) {
            setProperty(PROP_RESET_TITLE, resetTitle);
        }
        try {
            save();
        } catch (ClientException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
