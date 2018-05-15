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
 * Wraps the Search foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/search} in the repository for implementation details.
 */
public class Search extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/search";
    public static final String PROP_RESULT_PAGES = "resultPagesText";
    public static final String PROP_PREVIOUS_TEXT = "previousText";
    public static final String PROP_NEXT_TEXT = "nextText";
    public static final String PROP_SEARCH_TRENDS_TEXT = "searchTrendsText";
    public static final String PROP_RELATED_SEARCHES_TEXT = "relatedSearchesText";
    public static final String PROP_SIMILAR_PAGES_TEXT = "similarPagesText";
    public static final String PROP_SPELLCHECK_TEXT = "spellcheckText";
    public static final String PROP_NO_RESULTS_TEXT = "noResultsText";
    public static final String PROP_STATISTICS_TEXT = "statisticsText";
    public static final String PROP_SEARCH_BUTTON_TEXT = "searchButtonText";
    public static final String PROP_SEARCH_IN = "searchIn";

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
    public Search(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    /**
     * Adds an additional parameter {@code parentResourceType} to get properly inherited styles.
     *
     * @return FormEntityBuilder to be used for the create POST request.
     * @param order order
     */
    public FormEntityBuilder getCreateFormEntity(String order) {
        return super.getCreateFormEntity(order)
                .addParameter("parentResourceType", "foundation/components/parsys");
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }
}
