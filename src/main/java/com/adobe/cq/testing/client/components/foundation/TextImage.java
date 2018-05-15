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
import org.apache.sling.testing.clients.SlingHttpResponse;

/**
 * Wraps the TextImage foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/textimage} in the repository for implementation details.
 */
public class TextImage extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/textimage";
    public static final String PROP_TEXT = "text";
    public static final String PROP_TEXT_IS_RICH = "textIsRich";
    public static final String PROP_TITLE = "jcr:title";
    public static final String PROP_CSS_CLASS = "cq:cssClass";
    public static final String PROP_IMAGE_NODE_LOCATION = "/";
    public static final String PROP_IMAGE_NODE_NAME = "/image";

    /**
     * Default Image object used in the component
     */
    protected Image testImage = null;

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
     *                 (String,int...)} has been called.
     */
    public TextImage(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    /*
    * uploads the image to be used by the Textimage component
    *
    * @param mimeType       MIME type of image
    * @param imageName      image name
    * @param resourcePath   path to the image resource
    * @param expectedStatus list of allowed HTTP Status to be returned. if not set, status 200 is assumed
    * @return a {@link SlingResponseHandler} wrapping the HTML response returned by Sling
    * @throws ClientException If something fails during request/response cycle
    */
    public SlingHttpResponse uploadImage(String mimeType, String imageName, String resourcePath) throws ClientException {

        testImage = new Image(client, componentPath, PROP_IMAGE_NODE_LOCATION, null);

        client.doPost(componentPath + PROP_IMAGE_NODE_NAME, testImage.getCreateFormEntity(null).build());

        return testImage.uploadImage(mimeType, imageName, resourcePath);
    }

    /**
     * Sets properties to remove image upon save.
     *
     * @throws ClientException if the request fails
     * @throws InterruptedException to mark this method as waiting
     */
    public void setDeleteImage() throws ClientException, InterruptedException {
        testImage.setDeleteImage();
        testImage.save();
    }

    /**
     * @return The default Image component
     */
    public Image getImage() {
        return testImage;
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }

    @Override
    public SlingHttpResponse save(int... expectedStatus) throws ClientException, InterruptedException {
        if (testImage != null) {
            testImage.save();
        }

        return super.save(expectedStatus);
    }
}
