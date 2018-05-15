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
import org.apache.sling.testing.clients.util.FormEntityBuilder;

/**
 * Wraps the Chart foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/chart} in the repository for implementation details.
 */
public class Chart extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/chart";
    public static final String PROP_CHART_DATA = "chartData";
    public static final String PROP_CHART_TYPE = "chartType";
    public static final String PROP_CHART_ALT = "chartAlt";
    public static final String PROP_CHART_WIDTH = "chartWidth";
    public static final String PROP_CHART_HEIGHT = "chartHeight";

    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The {@link com.adobe.cq.testing.client.FoundationClient FoundationClient} that's
     *                 creating this
     *                 instance.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
     *                 occurs. The {@link #getName()} method will return the correct name after
     *                 {@link #create(String,int...)} has been called.
     */
    public Chart(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    /**
     * Adds an additional parameter {@code parentResourceType} to get properly inherited styles.
     *
     * @param order order
     * @return FormEntityBuilder to be used for the create POST request.
     */
    public FormEntityBuilder getCreateFormEntity(String order) {
        return super.getCreateFormEntity(order)
                // The following param is required to get the proper styles inherited
                .addParameter("parentResourceType", "foundation/components/parsys");
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }
}
