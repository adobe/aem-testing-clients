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
package com.adobe.cq.testing.client.components.foundation.profile;

import com.adobe.cq.testing.client.ComponentClient;
import com.adobe.cq.testing.client.components.AbstractComponent;

/**
 * Wraps the Profile Display Name foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/profile/form/textproperty} in the repository for implementation details.
 */
public class ProfileDisplayName extends AbstractComponent{

    public static final String RESOURCE_TYPE = "foundation/components/profile/form/textproperty";

    public static final String PROP_NAME = "name";
    public static final String PROP_TITLE = "jcr:title";
    public static final String PROP_COLS = "cols";
    public static final String PROP_ROWS = "rows";
    public static final String PROP_WIDTH = "width";
    public static final String PROP_CONSTRAINT_MESSAGE = "constraintMessage";
    public static final String PROP_CONSTRAINT_TYPE = "constraintType";
    public static final String PROP_REQUIRED = "required";
    public static final String PROP_REQUIRED_MESSAGE = "requiredMessage";
    
    
    
    public static final String PROP_NAME_VALUE = "./displayName";
    public static final String PROP_TITLE_VALUE = "Profile Display Name";
    public static final String PROP_COLS_VALUE = "5";
    public static final String PROP_ROWS_VALUE = "5";
    public static final String PROP_WIDTH_VALUE = "100";
    public static final String PROP_CONSTRAINT_MESSAGE_VALUE = "Constraint Message";
    public static final String PROP_CONSTRAINT_TYPE_VALUE = "foundation/components/form/constraints/name";
    public static final String PROP_REQUIRED_VALUE = "true";
    public static final String PROP_REQUIRED_MESSAGE_VALUE = "Required Message";
    
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
    public ProfileDisplayName(ComponentClient client, String pagePath, String location, String nameHint){
        super(client, pagePath, location, nameHint);
        
    }
 
    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

}
