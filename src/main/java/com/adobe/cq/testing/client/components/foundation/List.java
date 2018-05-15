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

/**
 * Wraps the List foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/list} in the repository for implementation details.
 */
public class List extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/list";
    public static final String OPT_LIST_FROM_CHILDREN = "children";
    public static final String OPT_LIST_FIXED_LIST = "static";
    public static final String OPT_LIST_SEARCH = "search";
    public static final String OPT_LIST_QUERY_BUILDER = "querybuilder";

    public static final String PROP_LIST_FROM = "listFrom";
    public static final String PROP_PARENT_PAGE = "parentPage";
    public static final String PROP_ANCESTOR_PAGE = "ancestorPage";
    public static final String PROP_DISPLAY_AS = "displayAs";
    public static final String PROP_ORDER_BY = "orderBy";
    public static final String PROP_LIMIT = "limit";
    public static final String PROP_FEED_ENABLED = "feedEnabled";
    public static final String PROP_PAGE_MAX = "pageMax";
    public static final String PROP_SEARCH_IN = "searchIn";
    public static final String PROP_PAGES = "pages";
    public static final String PROP_PAGES_DEL = "pages@Delete";
    public static final String PROP_QUERY = "query";
    public static final String PROP_SAVED_QUERY = "savedquery";
    public static final String PROP_TAG_SEARCHROOT = "tagsSearchRoot";


    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The {@link com.adobe.cq.testing.client.FoundationClient FoundationClient} that's
     *                 creating this
     *                 instance.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
     *                 occurs. The {@link #getName()} method will return the correct name after {@link #create
     *                 (String,int...)}
     *                 has been called.
     */
    public List(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }
}
