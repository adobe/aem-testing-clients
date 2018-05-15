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
package com.adobe.cq.testing.polling;

import org.apache.http.HttpStatus;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Poller to check if the instance is in a ready state (no services
 * registering/unregistering and no indexing running).
 */
public class InstanceReadyPoller extends Polling {
    private static final Logger LOG = LoggerFactory.getLogger(InstanceReadyPoller.class);

    private static final String TEST_JSP_LOCATION = "/libs/granite/core/components/login/test.json.jsp";

    private static final String TEST_URL = "/libs/granite/core/content/login.test.json";

    private SlingClient client;

    public InstanceReadyPoller(SlingClient client) {
        super();
        this.client = client;
    }

    @Override
    public Boolean call() throws ClientException {
        LOG.debug("checking " + TEST_URL);
        SlingHttpResponse exec = client.doGet(TEST_URL, HttpStatus.SC_OK);
        // check that the property "ready" is true
        // extract the json output as expected from AEM
        JsonNode resultNode = JsonUtils.getJsonNodeFromString(exec.getContent());

        // then check that the page is in the result set
        JsonNode readyProperty = resultNode.get("ready");
        boolean isReady = readyProperty.getBooleanValue();
        JsonNode indexingProperty = resultNode.get("indexing");
        boolean isIndexing = indexingProperty.getBooleanValue();
        return isReady && !isIndexing;
    }

    /**
     * Method to install the little script on the server that will allow this
     * poller to work.
     * 
     * Uninstall it if you don't need it anymore or at the end of your tests.
     * 
     * @param client client
     * @throws IOException if the test cannot be installed
     * @throws ClientException if the request fails
     */
    public static void installTest(SlingClient client) throws IOException, ClientException {
        File tmpFile = File.createTempFile("_install_", ".jsp");
        FileOutputStream fos = new FileOutputStream(tmpFile);
        InputStream fis = InstanceReadyPoller.class.getResourceAsStream("test.json.jsp");

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
        fis.close();
        fos.close();

        client.upload(tmpFile, "text/plain", TEST_JSP_LOCATION, false, HttpStatus.SC_OK);
        //noinspection ResultOfMethodCallIgnored
        tmpFile.delete();
    }

    /**
     * To be called at the end of the test in order to remove that script that
     * was used to do the polling.
     * 
     * @param client client
     * @throws ClientException if the request fails
     */
    public static void uninstallTest(SlingClient client) throws ClientException {
        client.deletePath(TEST_JSP_LOCATION);
    }
}
