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
package com.adobe.cq.testing.junit.assertion;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.components.AbstractComponent;
import com.adobe.cq.testing.util.TestUtil;
import org.apache.sling.testing.clients.AbstractSlingClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import static org.apache.http.HttpStatus.SC_OK;

public class ComponentAssert {
    /**
     * Checks if the component's node has the minimum to be a valid component node.
     *
     * @param comp the component class referencing the component on the server
     * @throws ClientException if requesting json node of fails for some reason
     * @throws InterruptedException to mark this method as waiting
     */
    public static void assertValidComponentNode(AbstractComponent comp) throws ClientException, InterruptedException {
        // get the json node
        JsonNode componentNode = comp.getComponentNode();
        // Check if sling:resourceType property was properly set
        Assert.assertEquals("'sling:resourceType' for '" + comp.getComponentPath() + "' not properly set!",
                comp.getResourceType(),
                componentNode.get("sling:resourceType").getValueAsText());
    }

    /**
     * Checks if the property of a node is set to the correct value.
     *
     * @param comp     the compent that contains the property
     * @param value    the expected value
     * @param propName the name of the property to check
     */
    public static void assertStringProperty(AbstractComponent comp, String value, String propName) {
        // check if title is set in nodes json
        Assert.assertNotNull(propName + " text has not been set!", comp.getPropertyAsString(propName));
        Assert.assertEquals(propName + " is wrong.", value, comp.getPropertyAsString(propName));
    }

    public static void assertUploadedImage(CQClient client, String imagePath,
                                           String resourcePath) throws ClientException, IOException {
        assertBinaryCompare(ResourceUtil.getResourceAsStream(resourcePath),
                client.doStreamGet(imagePath, null, null).getEntity().getContent());
    }

    public static void assertBinaryCompare(InputStream inStream1, InputStream inStream2) throws ClientException {
        Assert.assertTrue("The Binaries are not the same", TestUtil.binaryCompare(inStream1, inStream2));
    }

    /**
     * Checks if the component type has a edit dialog defined.
     *
     * @param client client to be used
     * @param resourceType the component to check
     * @throws ClientException if requesting the dialog definition as json fails
     */
    public static void assertHasEditDialog(AbstractSlingClient client, String resourceType) throws ClientException {
        // request the dialogs json
        SlingHttpResponse exec = client.doGet("/libs/" + resourceType + "/dialog.infinity.json", SC_OK);
        // get the json string
        String dialogJson = exec.getContent();
        // search
        Pattern p = Pattern.compile("\"xtype\":\".*panel\"", Pattern.CASE_INSENSITIVE);
        GraniteAssert.assertRegExFind("Can't call the edit dialog for component type " + resourceType, dialogJson, p);
    }

}
