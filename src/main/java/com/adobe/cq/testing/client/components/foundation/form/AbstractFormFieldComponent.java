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
import org.apache.sling.testing.clients.util.FormEntityBuilder;

/**
 * This abstract class defines common Methods and Constants for all form fields. Please note that on individual
 * level components may have some of this defaults removed (e.g captcha has no element name), or modified (e.g. more
 * Styling values).
 * Then either extend the methods of this class by overriding or add component specific methods.
 */
public abstract class AbstractFormFieldComponent extends AbstractComponent {

    String stringValue = null;
    
    // commonly available fields
    public static final String PROP_DESCRIPTION = "jcr:description";
    public static final String PROP_TITLE = "jcr:title";
    public static final String PROP_HIDE_TITLE = "hideTitle";
    // all form fields must store an element name
    public static final String PROP_ELEMENT_NAME = "name";
    // all form fields have an 'Initial Values' tab
    public static final String PROP_DEFAULT_VALUE = "defaultValue";
    // all form fields have a 'Constraints' tab
    public static final String PROP_REQUIRED = "required";
    public static final String PROP_REQUIRED_MESSAGE = "requiredMessage";
    public static final String PROP_CONSTRAINT_TYPE = "constraintType";
    public static final String PROP_CONSTRAINT_MESSAGE = "constraintMessage";
    // some commonly used options for constraint types
    public static final String OPT_CONSTRAINT_TYPE_DATE = "foundation/components/form/constraints/date";
    public static final String OPT_CONSTRAINT_TYPE_EMAIL = "foundation/components/form/constraints/email";
    public static final String OPT_CONSTRAINT_TYPE_NAME = "foundation/components/form/constraints/name";
    public static final String OPT_CONSTRAINT_TYPE_NUMERIC = "foundation/components/form/constraints/numeric";
    // all form fields have a 'Styling' tab
    public static final String PROP_STYLING_CSS = "css";

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
    public AbstractFormFieldComponent(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    @Override
    public FormEntityBuilder getCreateFormEntity(String order) {
        return super.getCreateFormEntity(order)
                // all form fields have a common super resource type
                .addParameter("./sling:resourceSuperType", "foundation/components/form/defaults/field");
    }

    /**
     * Set the name of the form field element.
     *
     * @param elementName element name to be set.
     */
    public void setElementName(String elementName){
        setProperty(PROP_ELEMENT_NAME,elementName);
    }

    /**
     * Get the elements name.
     *
     * @return the element name
     */
    public String getElementName(){
        return getPropertyAsString(PROP_ELEMENT_NAME);
    }
    
    /**
     * Set the default value for a form field. The default implementation only sets '{@value #PROP_DEFAULT_VALUE}'.
     * If extended component has more default fields (or none) either override this method or create new one.
     *
     * @param defaultValue  the default string value to be set for this form field
     */
    public void setDefaultValue(String defaultValue){
        setProperty(PROP_DEFAULT_VALUE,defaultValue);
    }

    /**
     * Defines if constraints should be applied to this field or not
     *
     * @param useConstraints  true to apply defined constraint, otherwise false
     */
    public void setUseConstraint(boolean useConstraints){
        if(useConstraints){
            setProperty(PROP_REQUIRED,"true");
        } else {
            setProperty(PROP_REQUIRED,"");
        }
    }

    /**
     * Configures the constraint that should apply to this form field. to enable/disable the constraint use
     * {@link #setUseConstraint(boolean)}. This is the default implementation. If the extended form field has
     * more or less fields either override this method or create a new one.
     *
     * @param requiredMessage The message that describes whats expected for this field
     * @param constraintMessage  The error message shown when the contstraint check fails
     * @param constraintType  what type of constraint it is. Constraints are implemented as components. e.g.
     *                        {@link #OPT_CONSTRAINT_TYPE_EMAIL } ({@value #OPT_CONSTRAINT_TYPE_EMAIL}).
     */
    public void setConstraint(String requiredMessage, String constraintMessage, String constraintType){
        setProperty(PROP_REQUIRED_MESSAGE,requiredMessage);
        setProperty(PROP_CONSTRAINT_MESSAGE,constraintMessage);
        setProperty(PROP_CONSTRAINT_TYPE,constraintType);
    }

    /**
     * Sets the CSS value on the Styling tab for this form field.
     *
     * @param css css value
     */
    public void setStylingCss(String css){
        setProperty(PROP_STYLING_CSS,css);
    }

    /**
     * Set the description for the text input field
     *
     * @param description  The description text
     */
    public void setDescription(String description) {
        setProperty(PROP_DESCRIPTION,description);
    }

    /**
     * Sets the title to be shown for this form field.
     *
     * @param title  title text.
     */
    public void setTitle(String title){
        setProperty(PROP_TITLE,title);
    }

    /**
     * Defines if the title for this form field should be shown or not.
     *
     * @param hideTitle true to hide the title, otherwise false
     */
    public void setHideTitle(boolean hideTitle){
        if (hideTitle){
            setProperty(PROP_HIDE_TITLE,"true");
        } else {
            setProperty(PROP_HIDE_TITLE,"");
        }
    }
}
