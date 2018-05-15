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

package com.adobe.cq.testing.client.components.foundation;

import com.adobe.cq.testing.client.ComponentClient;
import com.adobe.cq.testing.client.components.AbstractComponent;

/**
 * Base class for all Foundation Component wrapping objects. Foundation components can be found
 * at /libs/foundation/components in the CQ repository.<br>
 * <br>
 * The subclasses provide methods to manipulate the according component but does not store or cache any
 * component data to avoid inconsistencies. The only information stored with the client is path info.
 */
public abstract class AbstractFoundationComponent extends AbstractComponent {

    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The ComponentClient that will be used for sending the requests.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
     *                 occurs. The {@link #getName()} method will return the correct name after {@link #create
     *                 (String,int...)} has been called.
     */
    public AbstractFoundationComponent(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }
}

