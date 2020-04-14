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
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.Constants;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Wraps the Carousel foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/carousel} in the repository for implementation details.
 */
public class Carousel extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/carousel";
    public static final String PROP_PLAY_SPEED = "playSpeed";
    public static final String PROP_TRANS_TIME = "transTime";
    public static final String PROP_CONTROLS_TYPE = "controlsType";
    public static final String PROP_LIST_FROM = "listFrom";
    public static final String PROP_ORDER_BY = "orderBy";
    public static final String PROP_LIMIT = "limit";
    public static final String PROP_PARENT_PAGE = "parentPage";
    public static final String PROP_PAGES = "pages";
    public static final String PROP_SEARCH_IN = "searchIn";
    public static final String PROP_QUERY = "query";
    public static final String PROP_SAVED_QUERY = "savedquery";

    /**
     * The constructor stores all the component path information like parentPage, name etc.
     *
     * @param client   The ComponentClient that will be used for sending the requests.
     * @param pagePath path to the page that will contain the component.
     * @param location relative location to the parent node inside the page that will contain the component node.
     * @param nameHint name to be used for the component node. Might get altered by the server if a naming conflict
     *                 occurs. The {@link #getName()} method will return the correct name after
     *                 {@link #create(String,int...)} has been called.
     */
    public Carousel(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    /**
     * Adds an additional parameter {@code parentResourceType} to get properly inherited styles.
     *
     * @return FormEntityBuilder to be used for the create POST request.
     * @param order order
     */
    @Override
    public FormEntityBuilder getCreateFormEntity(String order) {
        return super.getCreateFormEntity(order)
                // The following param is required to get the proper styles inherited
                .addParameter("parentResourceType", "foundation/components/parsys");
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    /**
     * Sends an editing request to a carousel component.
     *
     * @param playSpeed      Time in milliseconds until the next slide is shown.  Set 0 to not change.
     * @param transTime      Time in milliseconds for a transition between 2 slides. Set 0 to not change.
     * @param controlsType   Allowed values:<br>
     *                       <ul>
     *                       <li>pn (prev next)</li>
     *                       <li>tl (top left)</li>
     *                       <li>tc (top center)</li>
     *                       <li>tr (top right)</li>
     *                       <li>bl (bottom left)</li>
     *                       <li>bc (bottom center)</li>
     *                       <li>br (bottom right)</li>
     *                       </ul>
     *                       Set null to not change.
     * @param listFrom       Allowed values:<br>
     *                       <ul>
     *                       <li>children (Child Pages)</li>
     *                       <li>static (Fixed List)</li>
     *                       <li>search (Search)</li>
     *                       <li>querybuilder (Advanced Search)</li>
     *                       </ul>
     *                       Set null to not change.
     * @param orderBy        Allowed  values:<br>
     *                       <ul>
     *                       <li>jcr:title</li>
     *                       <li>jcr:created</li>
     *                       <li>cq:lastModified</li>
     *                       <li>cq:template</li>
     *                       </ul>
     *                       Set null to not change.
     * @param limit          Maximum number of items displayed in list, set 0 to ignore.
     * @param parentPage     If listFrom = children, defines the parent page.Set null to use current page.
     * @param fixedList      If listFrom = static,  set String array of pages that should doGet shown in carousel.
     * @param searchIn       If listFrom = search, where to start search, Set null to use current site
     *                       (eg. /content/mysite)
     * @param query          If listFrom  = search, the search query to execute.
     * @param savedQuery     If listFrom = querybuilder, the querybuilder predicate notation.
     * @param expectedStatus list of allowed HTTP Status to be returned. if not set, status 200 is assumed
     * @return a {@link SlingHttpResponse} wrapping the HTML response returned by Sling
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse edit(int playSpeed, int transTime,
                                  String controlsType,
                                  String listFrom, String orderBy, int limit, String parentPage,
                                  String[] fixedList,
                                  String searchIn, String query, String savedQuery,
                                  int... expectedStatus)
            throws ClientException {

        // build the form to submit
        FormEntityBuilder form = FormEntityBuilder.create();
        //form.addParameter("./sling:resourceType", RESOURCE_TYPE);
        form.addParameter(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8);
        if (playSpeed != 0) {
            form.addParameter("./"+ PROP_PLAY_SPEED, Integer.toString(playSpeed));
        }
        if (transTime != 0) {
            form.addParameter("./" + PROP_TRANS_TIME, Integer.toString(transTime));
        }
        if (controlsType != null) {
            form.addParameter("./" + PROP_CONTROLS_TYPE, controlsType);
        }
        if (listFrom != null) {
            form.addParameter("./" + PROP_LIST_FROM, listFrom);
        }
        if (orderBy != null) {
            form.addParameter("./" + PROP_ORDER_BY, orderBy);
        }
        if (limit != 0) {
            form.addParameter("./" + PROP_LIMIT, Integer.toString(limit));
        }
        if (parentPage != null) {
            form.addParameter("./" + PROP_PARENT_PAGE, parentPage);
        }

        if (fixedList != null && fixedList.length > 0) {
            for (String aFixedList : fixedList) {
                form.addParameter("./" + PROP_PAGES, aFixedList);
            }
        }
        if (searchIn != null) {
            form.addParameter("./" + PROP_SEARCH_IN, searchIn);
        }
        if (query != null) {
            form.addParameter("./" + PROP_QUERY, query);
        }
        if (savedQuery != null) {
            form.addParameter("./" + PROP_SAVED_QUERY, savedQuery);
        }

        return client.doPost(componentPath, form.build(), HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }
}
