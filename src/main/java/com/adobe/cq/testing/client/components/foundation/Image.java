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
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.Constants;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;

import static org.apache.http.HttpStatus.SC_OK;

/**
 * Wraps the Image foundation component, providing methods for editing it. See
 * {@code /libs/foundation/components/image} in the repository for implementation details.
 */
public class Image extends AbstractFoundationComponent {

    public static final String RESOURCE_TYPE = "foundation/components/image";
    public static final String PROP_FILE_REFERENCE = "fileReference";
    public static final String PROP_UNDO_BLOBS = ":cq:undoblobs";
    public static final String PROP_FILE = "file";
    public static final String PROP_FILE_TMP = "file.sftmp";
    public static final String PROP_FILE_NAME = "fileName";
    public static final String PROP_WIDTH = "width";
    public static final String PROP_HEIGHT = "height";
    public static final String PROP_IMAGE_CROP = "imageCrop";
    public static final String PROP_IMAGE_ROTATE = "imageRotate";
    public static final String PROP_IMAGE_MAP = "imageMap";
    public static final String PROP_TITLE = "jcr:title";
    public static final String PROP_ALT_TEXT = "alt";
    public static final String PROP_DESCRIPTION = "jcr:description";
    public static final String PROP_LINK_URL = "linkURL";
    public static final String PROP_FILE_MOVE_FROM = "file@MoveFrom";

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
    public Image(ComponentClient client, String pagePath, String location, String nameHint) {
        super(client, pagePath, location, nameHint);
    }

    /**
     * uploads the image to be used by the image component
     *
     * @param mimeType       MIME type of image
     * @param imageName      image name
     * @param resourcePath   path to the image resource
     * @param expectedStatus list of allowed HTTP Status to be returned. if not set, status 200 is assumed
     * @return a {@link SlingHttpResponse} wrapping the HTML response returned by Sling
     * @throws ClientException If something fails during request/response cycle
     */
    public SlingHttpResponse uploadImage(String mimeType, String imageName, String resourcePath, int... expectedStatus)
            throws ClientException {
        // 2 steps:
        //   1. Upload the file as a temporary file node (file.sftmp)
        //   2. On "save", the temporary node is moved to final node (file)


        //   1. Upload the file as a temporary file node (file.sftmp)
        HttpEntity uploadEntity = MultipartEntityBuilder.create()
                .addBinaryBody("./" + PROP_FILE_TMP, ResourceUtil.getResourceAsStream(resourcePath),
                        ContentType.create(mimeType), imageName)
                .addTextBody(Constants.PARAMETER_CHARSET, Constants.CHARSET_UTF8)
                .build();

        // send the request with the multipart entity as content
        client.doPost(componentPath, uploadEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));

        //   2. On "save", the temporary node is moved to final node (file)
        HttpEntity saveEntity = FormEntityBuilder.create()
                .addParameter("./" + PROP_FILE_MOVE_FROM, componentPath + "/" + PROP_FILE_TMP)
                .addParameter(PROP_UNDO_BLOBS, componentPath + "/" + PROP_FILE)
                .build();

        // send the request with the multipart entity as content
        return client.doPost(componentPath, saveEntity, HttpUtils.getExpectedStatus(SC_OK, expectedStatus));
    }

    /**
     * Set properties to reference an image in the repository.
     *
     * @param imageReference reference path to image in repository.
     */
    public void setImageReference(String imageReference) {
        setProperty(PROP_FILE_REFERENCE, imageReference);
        setProperty(PROP_UNDO_BLOBS, "update:" + componentPath + "/file");
    }

    /**
     * Sets properties to remove image upon save.
     */
    public void setDeleteImage() {
        setProperty(PROP_FILE + "@Delete", "true");
        setProperty(PROP_UNDO_BLOBS, "update:" + componentPath + "/file");
    }

    @Override
    public String getResourceType() {
        return RESOURCE_TYPE;
    }
}
