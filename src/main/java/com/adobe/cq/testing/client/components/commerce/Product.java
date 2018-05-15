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
package com.adobe.cq.testing.client.components.commerce;

import com.adobe.cq.testing.client.ComponentClient;
import com.adobe.cq.testing.client.components.AbstractComponent;
import org.apache.sling.testing.clients.ClientException;

/**
 * Wraps the product commerce component, providing methods for editing it.
 */
public class Product extends AbstractComponent {

    public static final String RESOURCE_TYPE = "commerce/components/product";
    public static final String PROP_PRODUCT_DATA = "productData";

    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The {@link com.adobe.cq.testing.client.FoundationClient FoundationClient} that's creating
     *                 GraniteClientthis instance.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
     *                 occurs. The {@link #getName()} method will return the correct name after {@link #create(String, int...)}
     *                 has been called.
     * @throws ClientException if the request fails
     */
    public Product(ComponentClient client, String pagePath, String location, String nameHint) throws ClientException {
        super(client, pagePath, location, nameHint);
    }

    public String getResourceType() {
        return RESOURCE_TYPE;
    }
}
