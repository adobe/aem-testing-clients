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
import com.adobe.cq.testing.client.JsonClient;
import com.adobe.cq.testing.util.TestUtil;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.codehaus.jackson.JsonNode;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;


/**
 * Contains CQ specific Asserts for testing.
 */
public class CQAssert {

    /**
     * Tests if the page at  {code}path{code} exists on the server connected by  {code}client{code} and is
     * a valid CQ Page. It does this by requesting the JSON tree of the page node, verifying if it
     * has all needed properties and subnodes of CQ Page.<br>
     * <br>
     * To do this it tries to reach the page for  {code}timeout{code} milliseconds with  {code}waitInterval{code}
     * milliseconds pause between each request.
     *
     * @param client The client used for requesting the page.
     * @param path The path to the page
     * @param timeout How long we should try to reach the page in milliseconds.
     * @param delay Time between request retries in milliseconds.
     * @throws InterruptedException to mark this method as waiting
     */
    public static void assertCQPageExistsWithTimeout(final CQClient client, final String path,
                                                     final long timeout, final long delay) throws InterruptedException {
        try {
            new Polling() {
                @Override
                public Boolean call() {
                    try {
                        // Get page and verify that it is a valid CQPage
                        assertIsCQPage(client, path);
                        return true;
                    } catch(Throwable e) {
                        return false;
                    }
                }
            }.poll(timeout, delay);
        } catch (TimeoutException e) {
            Assert.fail("Timeout reached while waiting for CQ page " + path);
        }
    }

    public static void assertPathDoesNotExistWithTimeout(final CQClient client, final String path,
                                                         final long timeout, final long delay) throws InterruptedException {
        try {
            new Polling() {
                @Override
                public Boolean call() {
                    try {
                        return !client.exists(path);
                    } catch(Throwable e) {
                        return false;
                    }
                }
            }.poll(timeout, delay);
        } catch (TimeoutException e) {
            Assert.fail("Timeout reached while waiting for path to be deleted: " + path);
        }
    }

    /**
     * Tests if a folder exists at the location  {code}path{code} with named  {code}folderTitle{code}.
     * It verifies by requesting the json output of the node and verifying that all nodes and properties
     * are properly created.
     *
     * @param client      The client used to request the json for the folder node.
     * @param path        Path to the folder in question.
     * @param folderTitle Title of the folder to verify.
     */
    public static void assertFolderExists(final CQClient client, String path, String folderTitle)  {

        // Get the root node as JsonNode object
        JsonNode rootNode = null;
        try {
            rootNode = client.adaptTo(JsonClient.class).doGetJson(path, 1);
        } catch (ClientException e) {
            Assert.fail("Request for " + path + " failed!");
        }

        // check if jcr:primaryType is set to sling:OrderedFolder
        String primaryType = rootNode.get("jcr:primaryType").getValueAsText();

        Assert.assertTrue("jcr:primaryType of folder node " + path + " is neither sling:OrderedFolder nor Sling:Folder",
                ("sling:OrderedFolder".equals(primaryType) ||"sling:Folder".equals(primaryType) ));

        // check if jcr:content node exists
        Assert.assertTrue("No jcr:content node found below " + path + "!",
                !rootNode.path("jcr:content").isMissingNode());

        // doGet the content node
        JsonNode contentNode = rootNode.path("jcr:content");

        // check if jcr:primaryType is set to cq:PageContent
        Assert.assertEquals("jcr:primaryType of jcr:content node below " + path +
                " is not set to nt:unstructured!",
                "nt:unstructured", contentNode.get("jcr:primaryType").getTextValue());

        // check if jcr:title is set
        Assert.assertNotNull("jcr:title property is not set in jcr:content node below " +
                path + "!", contentNode.get("jcr:title"));

        // check if jcr:title is set to the folder title
        Assert.assertEquals("jcr:title of jcr:content node below " + path +
                " is not set folder title " + folderTitle + "!",
                folderTitle, contentNode.get("jcr:title").getTextValue());
    }

    /**
     * Tests if a File exists at the location  {code}path{code}. It verifies by requesting the json output
     * of the node and verifying that all nodes and properties are properly created.<br>
     * <br>
     * It also makes a binary compare between the original file data and the requested file.
     * <br>
     *
     * @param client   The client used to request the json for the folder node.
     * @param path     Path to the file in question.
     * @param fileData The file's contents that were uploaded.
     * @param mimeType file mime type
     */
    public static void assertFileExists(final CQClient client, String path, InputStream fileData, String mimeType) {

        // Get the root node as JsonNode object
        JsonNode node = null;
        try {
            node = client.adaptTo(JsonClient.class).doGetJson(path, -1);
        } catch (ClientException e) {
            Assert.fail("Request for " + path + " failed");
        }

        // check if jcr:primaryType is set to sling:OrderedFolder
        Assert.assertEquals("jcr:primaryType of folder node " + path + " is not set to sling:OrderedFolder!",
                "sling:OrderedFolder", node.get("jcr:primaryType").getValueAsText());

        // check if file node exists
        Assert.assertTrue("No file node found below " + path + "!",
                !node.path("file").isMissingNode());


        // Get the file node
        node = node.path("file");
        path += "/file";

        // check if jcr:primaryType is set to nt:file
        Assert.assertEquals("jcr:primaryType of file node below " + path +
                " is not set to nt:file!",
                "nt:file", node.get("jcr:primaryType").getTextValue());

        // check if jcr:content node exists
        Assert.assertTrue("No jcr:content node found below " + path + "!",
                !node.path("jcr:content").isMissingNode());

        // Get the jcr:content node
        node = node.path("jcr:content");
        path += "/jcr:content";

        // check if jcr:mimeType is set correctly
        Assert.assertEquals("jcr:mimeType is not set to " + mimeType,
                mimeType, node.get("jcr:mimeType").getTextValue());

        // check if jcr:primaryType is set to nt:resource
        Assert.assertEquals("jcr:primaryType of jcr:content node below " + path +
                " is not set to nt:resource!",
                "nt:resource", node.get("jcr:primaryType").getTextValue());

        try {
            InputStream in = client.doStreamGet(path, null, null).getEntity().getContent();
            Assert.assertTrue("The original file and the requested file are not the same", TestUtil.binaryCompare(fileData, in));
        } catch (IOException | ClientException e) {
            throw new AssertionError(e);
        }

    }

    /**
     * Tests if a Asset exists at the location  {code}path{code}. It verifies by requesting the json output
     * of the node and verifying that all nodes and properties are properly created.<br>
     * <br>
     * It also makes a binary compare between the original file data and the requested rendition named
     *  {code}original{code}.<br>
     * <br>
     * NOTE: This assert makes no assumptions about the type of the uploaded file (image, pdf, etc)
     * so it only verifies if the  {code}Metadata{code} node and  {code}renditions{code} folder was created.
     * It does not verify extracted Metadata or check what renditions have been created. The
     * only rendition verified is the one named  {code}original{code} through binary compare with the original
     * file.
     *
     * @param client   The client used to request the json for the folder node.
     * @param path     Path to the asset in question.
     * @param fileData The file's contents that were uploaded.
     * @param mimeType file mime type
     */
    public static void assertAssetExists(final CQClient client, String path, InputStream fileData, String mimeType) {

        // Get the root node as JsonNode object
        JsonNode node = null;
        try {
            node = client.adaptTo(JsonClient.class).doGetJson(path, -1);
        } catch (ClientException e) {
            Assert.fail("Request for " + path + " failed!");
        }

        // check if jcr:primaryType is set to dam:Asset
        Assert.assertEquals("jcr:primaryType of folder node " + path + " is not set to dam:Asset!",
                "dam:Asset", node.get("jcr:primaryType").getValueAsText());

        // check if jcr:content node exists
        Assert.assertTrue("No jcr:content node found below " + path + "!",
                !node.path("jcr:content").isMissingNode());


        // Get the jcr:content node
        node = node.path("jcr:content");
        path += "/jcr:content";

        // check if jcr:primaryType is set to dam:AssetContent
        Assert.assertEquals("jcr:primaryType of jcr:content node below " + path +
                " is not set to dam:AssetContent!",
                "dam:AssetContent", node.get("jcr:primaryType").getTextValue());

        // check if metadata node exists
        Assert.assertTrue("No metadata node found below " + path + "!",
                !node.path("metadata").isMissingNode());

        // check if renditions folder exists
        Assert.assertTrue("No renditions folder found below " + path + "!",
                !node.path("renditions").isMissingNode());

        // Get the renditions node
        node = node.path("renditions");
        path += "/renditions";

        // check if original folder exists
        Assert.assertTrue("No original folder found below " + path + "!",
                !node.path("original").isMissingNode());

        // Get the original node
        node = node.path("original");
        path += "/original";

        // check if jcr:primaryType is set to nt:file
        Assert.assertEquals("jcr:primaryType of node  " + path +
                " is not set to nt:file!",
                "nt:file", node.get("jcr:primaryType").getTextValue());

        // check if jcr:content node exists
        Assert.assertTrue("No jcr:content node found below " + path + "!",
                !node.path("jcr:content").isMissingNode());

        // Get the jcr:content node
        node = node.path("jcr:content");
        path += "/jcr:content";

        // check if jcr:mimeType is set correctly
        Assert.assertEquals("jcr:mimeType is not set to " + mimeType,
                mimeType, node.path("jcr:mimeType").getTextValue());

        try {
            Assert.assertTrue("The original file and the requested file are not the same",
                    TestUtil.binaryCompare(fileData, client.doStreamGet(path, null, null).getEntity().getContent()));
        } catch (Exception e) {
            Assert.fail("Binary compare of files failed!");
        }
    }

    /**
     * Verifies if a page is really a CQ page by checking for specific nodes
     * and properties:<br>
     * <ul>
     * <li> {code}jcr:primaryType{code} is set to  {code}cq:Page{code}</li>
     * <li> {code}jcr:content{code} node exists</li>
     * </ul>
     *
     * @param client The client used to doGet the JSON of the page
     * @param path   Location of the page
     */
    public static void assertIsCQPage(final CQClient client, final String path) {

        // doGet the root node as JsonNode object
        JsonNode rootNode = null;
        try {
            rootNode = client.adaptTo(JsonClient.class).doGetJson(path, 2);
        } catch (ClientException e) {
            Assert.fail("Request for " + path + " failed!");
        }

        // check if jcr:primaryType is set to cq:Page
        Assert.assertEquals("jcr:primaryType of page node " + path + " is not set to cq:Page!",
                "cq:Page", rootNode.get("jcr:primaryType").getValueAsText());

        // check if jcr:content node exists
        Assert.assertTrue("No jcr:content node found below " + path + "!", !rootNode.path("jcr:content").isMissingNode());
    }


    public static void assertIsLiveSite(final CQClient client, final String newPath, final String masterPath) {
        JsonNode rootNode = null;
        try {
            rootNode = client.adaptTo(JsonClient.class).doGetJson(newPath, 2);
        } catch (ClientException e) {
            Assert.fail("Request for " + newPath + " failed!");
        }

        JsonNode contentNode = rootNode.path("jcr:content");
        Assert.assertTrue("No jcr:content node found!", !contentNode.isMissingNode());
        Assert.assertNotNull("cq:lastRolledout is not set!", contentNode.get("cq:lastRolledout"));
        Assert.assertNotNull("cq:lastRolledoutBy is not set!", contentNode.get("cq:lastRolledoutBy"));
        JsonNode liveSyncConfig = contentNode.path("cq:LiveSyncConfig");
        Assert.assertTrue("No cq:LiveSyncConfig node found!", !liveSyncConfig.isMissingNode());
        Assert.assertEquals("cq:master is not set correctly!", masterPath, liveSyncConfig.get("cq:master")
                .getValueAsText());
        Assert.assertNotNull("cq:isDeep is not set!", liveSyncConfig.get("cq:isDeep"));
    }

    public static void assertIsBluePrint(final CQClient client, final String path) {
        JsonNode rootNode = null;
        try {
            rootNode = client.adaptTo(JsonClient.class).doGetJson(path, 7);
        } catch (ClientException e) {
            Assert.fail("Request for " + path + " failed!");
        }
        JsonNode currentNode = rootNode.path("jcr:content");
        Assert.assertTrue("No jcr:content node found!", !currentNode.isMissingNode());
        String[][] nodePath = {
                {"dialog", "cq:Dialog"},
                {"items", "cq:WidgetCollection"},
                {"tabs", "cq:TabPanel"},
                {"items", "cq:WidgetCollection"}
        };
        for (String[] child : nodePath) {
            currentNode = currentNode.path(child[0]);
            Assert.assertTrue("No " + child[0] + " node found!", !currentNode.isMissingNode());
            Assert.assertEquals(child[0] + " is not of type " + child[1], currentNode.get("jcr:primaryType")
                    .getValueAsText(), child[1]);
        }
        String[][] tabs = {
                {"tab_lan", "/libs/wcm/msm/templates/blueprint/defaults/language_tab.infinity.json"},
                {"tab_chap", "/libs/wcm/msm/templates/blueprint/defaults/chapters_tab.infinity.json"},
                {"tab_lc", "/libs/wcm/msm/templates/blueprint/defaults/livecopy_tab.infinity.json"}
        };
        for (String[] child : tabs) {
            JsonNode tab = currentNode.path(child[0]);
            Assert.assertTrue("No " + child[0] + " node found!", !tab.isMissingNode());
            Assert.assertEquals(child[0] + " has wrong type.", tab.get("jcr:primaryType").getValueAsText(),
                    "cq:Widget");
            Assert.assertEquals(child[0] + " has wrong path property.", tab.get("path").getValueAsText(), child[1]);
        }
    }

    public static void assertIsVersionList(CQClient client, String jsonString) {
        JsonNode rootNode = null;
        try {
            rootNode = JsonUtils.getJsonNodeFromString(jsonString);
        } catch (ClientException e) {
            Assert.fail("Parsing of JSON String failed!");
        }
        JsonNode versions = rootNode.path("versions");
        Assert.assertTrue("versions node not found!", !versions.isMissingNode());
        Assert.assertTrue("versions node is not an array!", versions.isArray());

        final String[] properties = {"id", "label", "name", "title", "comment", "created", "deleted"};
        for (int i = 0; i < versions.size(); ++i) {
            JsonNode version = versions.get(i);
            for (String property : properties) {
                Assert.assertTrue(property + " property node not found!", !version.get(property).isMissingNode());
            }
        }
    }

    public static void assertIsVersionTree(CQClient client, String jsonString) {
        JsonNode rootNode = null;
        try {
            rootNode = JsonUtils.getJsonNodeFromString(jsonString);
        } catch (ClientException e) {
            Assert.fail("Parsing of JSON String failed!");
        }
        Assert.assertTrue("Version tree is not an array!", rootNode.isArray());
        final String[] properties = {"text", "date", "name", "id", "leaf", "label", "title", "deleted"};
        for (int i = 0; i < rootNode.size(); ++i) {
            JsonNode element = rootNode.get(i);
            for (String property : properties) {
                Assert.assertTrue(property + " property node not found!", !element.get(property).isMissingNode());
            }
        }
    }
}
