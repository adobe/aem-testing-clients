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

package com.adobe.cq.testing.client.components.collab;

import com.adobe.cq.testing.client.ComponentClient;
import com.adobe.cq.testing.client.components.AbstractComponent;
import org.apache.http.HttpEntity;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;

public class Ratings extends AbstractComponent {

    public static final String RESOURCE_TYPE = "collab/commons/components/ratings";
    public static final String RATING_ELEMENT_RESOURCE_TYPE = "collab/commons/components/ratings";

    public static String GENERATED_RATINGS_PREFIX_PATH = "/content/usergenerated";

    public static final String PROP_COMMENTS_ALLOWED = "commentsAllowed";
    public static final String PROP_DATE_FORMAT = "dateFormat";
    public static final String PROP_DEFAULT_MESSAGE = "defaultMessage";
    public static final String PROP_MODERATE_COMMENTS = "moderateComments";
    public static final String PROP_SIGNED_IN_TEXT = "signedInText";

    private static final String CREATE_RATING_SUFFIX_PATH = ".createrating.html";


    /**
     * The constructor stores all the component path information like
     * parentPage, name etc.
     * 
     * @param client
     *            The {@link com.adobe.cq.testing.client.FoundationClient
     *            FoundationClient} that's creating this instance.
     * @param pagePath
     *            path to the page that will contain the component.
     * @param location
     *            relative location to the parent node inside the page that will
     *            contain the component node.
     * @param nameHint
     *            name to be used for the component node. Might get altered by
     *            the server if a naming conflict occurs. The {@link #getName()}
     *            method will return the correct name after
     *            {@link #create (String,int...)} has been called.
     */
    public Ratings(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }


    public SlingHttpResponse postRating(String description, String userIdentifier, String email, String url, int rating)
                    throws ClientException {
        String createRatingPath = getComponentPath() + CREATE_RATING_SUFFIX_PATH;

        HttpEntity feb = FormEntityBuilder.create()
                .addParameter("jcr:description", description)
                .addParameter("userIdentifier", userIdentifier)
                .addParameter("email", email)
                .addParameter("url", url)
                .addParameter("rating", Integer.toString(rating))
                .addParameter("id", "nobot")
                .build();

        // send the request with the multipart entity as content
        return client.doPost(createRatingPath, feb, SC_OK, SC_MOVED_TEMPORARILY);
    }


    public String getGeneratedRatingsPath() {
        return GENERATED_RATINGS_PREFIX_PATH + getPagePath() + "/jcr:content/" + getName();
    }


    public ArrayList<Rating> getGeneratedRatings() throws ClientException {
        ArrayList<Rating> res = new ArrayList<>();

        JsonNode compJson = client.doGetJson(getGeneratedRatingsPath(), -1);

        Iterator<JsonNode> it = compJson.getElements();
        while (it.hasNext()) {
            JsonNode n = it.next();
            if (n.isObject() && (n.get("sling:resourceType") != null) &&
                    n.get("sling:resourceType").getTextValue().equals(Rating.RESOURCE_TYPE)) {
                Rating r = new Rating(n);
                res.add(r);
            }
        }

        return res;
    }


    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }
}
