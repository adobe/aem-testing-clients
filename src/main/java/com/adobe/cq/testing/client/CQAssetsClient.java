/*
 * Copyright 2020 Adobe Systems Incorporated
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

package com.adobe.cq.testing.client;

import com.adobe.cq.testing.client.assets.*;
import com.adobe.cq.testing.client.assets.dto.InitiateUploadFile;
import com.adobe.cq.testing.client.assets.dto.InitiateUploadResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.JsonUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class CQAssetsClient extends CQClient {

    private static Logger LOG = LoggerFactory.getLogger(CQAssetsClient.class);
    private static final String ACP_REL_CREATE = "http://ns.adobe.com/adobecloud/rel/create";
    private static final String ACP_LINK_HREF = "href";
    private static final String ACP_LINK_TYPE = "type";
    private static final String ACP_LINK_TYPE_DIRECT = "direct";
    private static final String ACP_LINKS = "_links";
    private static final String ACP_LIST_CONTENT_DAM_FOLDER = "/platform/content/dam";
    private static final String DAM_ASSET_STATE = "dam:assetState";
    private static final String DAM_ASSET_STATE_PROCESSED = "processed";
    private static final String DBA_CONTENT_DAM_INITIATE_UPLOAD = "/content/dam.initiateUpload.json";
    private final DirectBinaryAccessSupport binaryAccessSupport = new DirectBinaryAccessSupport();

    /**
     * The default timeout for asset processing, {@value #ASSET_PROCESSED_TIMEOUT} milliseconds.
     */
    protected static final long ASSET_PROCESSED_TIMEOUT = 30000;

    /**
     * The default delay between polling for asset processing status, {@value #ASSET_PROCESSED_DELAY} milliseconds.
     */
    protected static final long ASSET_PROCESSED_DELAY = 500;

    /**
     * Check if Direct Binary Access is enabled
     *
     * @return True if enabled, false otherwise
     * @throws ClientException if something fails during the request/response cycle
     */
    public boolean isDirectBinaryAccessSupported() throws ClientException {
        synchronized (binaryAccessSupport) {
            if (binaryAccessSupport.isUnknown()) {
                binaryAccessSupport.setSupported(checkDirectBinaryAccessSupport());
            }
            return binaryAccessSupport.isSupported();
        }
    }

    /**
     * Uploads an <b>Asset</b> to the repository. Same as using {@code New File...} in the DAM admin or when
     * uploading a file below {@code Digital Assets} in the Site Admin.<br>
     * <br>
     * This will upload the file and store it in a node typed {@code dam:Asset}. This will trigger all
     * DAM related workflows for generating rendition nodes, extract metadata etc.<br>
     * <br>
     * To upload a file that is not to be handled as an asset use {@link #uploadFileCQStyle} instead.<br>
     * To upload a file directly using sling use {@link #upload(java.io.File, String, String, boolean, int...)}.<br>
     * <br>
     * This implementation will pick Direct Binary Access upload when available or fallback to the original AEM implementation
     *
     * @param fileName file name
     * @param resourcePath defines the path to the resource
     * @param mimeType MIME type of the image getting uploaded
     * @param parentPath parent page (folder) that will contain the file
     * @param expectedStatus list of expected HTTP Status to be returned, if not set, 200 is assumed.
     * @return the response
     * @throws ClientException if something fails during the request/response cycle
     */
    public SlingHttpResponse uploadAsset(String fileName, String resourcePath, String mimeType, String parentPath, int... expectedStatus) 
            throws ClientException {
        if (isDirectBinaryAccessSupported()) {
            LOG.info("Using Direct Binary Access for upload");
            return uploadAssetDBA(fileName, resourcePath, mimeType, parentPath);
        } else {
            LOG.info("Using the Create Asset Servlet for upload");
            return super.uploadAsset(fileName, resourcePath, mimeType, parentPath, expectedStatus);
        }
    }

    /**
     * Wait for an asset to complete processing.  The total timeout is {@value #ASSET_PROCESSED_TIMEOUT} milliseconds.
     * Polling the asset status occurs every {@value #ASSET_PROCESSED_DELAY} milliseconds.
     *
     * @param assetPath Path to an asset
     * @throws ClientException if something fails during the request/response cycle, or if the asset failed to process
     * @throws TimeoutException if the wait times out
     * @throws InterruptedException if the wait is interrupted
     */
    public void waitAssetProcessed(String assetPath) throws ClientException, TimeoutException, InterruptedException {
        waitAssetProcessed(assetPath, ASSET_PROCESSED_TIMEOUT, ASSET_PROCESSED_DELAY);
    }

    /**
     * Wait for an asset to complete processing. The asset state will be checked at least once. If the timeout is 0 or 
     * less, then the asset state will be executed exactly once.
     *
     * @param assetPath Path to an asset
     * @param timeout total time to wait, in milliseconds
     * @param delay time to wait between polls of asset status, in milliseconds
     * @throws ClientException if something fails during the request/response cycle, or if the asset failed to process
     * @throws TimeoutException if the wait times out
     * @throws InterruptedException if the wait is interrupted
     */
    public void waitAssetProcessed(String assetPath, long timeout, long delay) 
            throws ClientException, TimeoutException, InterruptedException {
        Polling p = new Polling() {
            private String assetStatus;

            @Override
            public Boolean call() throws Exception {
                String currentStatus = getAssetStatus(assetPath);
                if (!StringUtils.equals(assetStatus, currentStatus)) {
                    LOG.info("Waiting on " + assetPath + ", status: " + currentStatus);
                }
                assetStatus = currentStatus;
                return DAM_ASSET_STATE_PROCESSED.equals(assetStatus);
            }

            @Override
            protected String message() {
                return "Asset " + assetPath + " has not been processed after %1$d ms";
            }
        };
        p.poll(timeout, delay);

        // check if there are any failures
        String failures = getAssetProcessingFailures(assetPath);
        if (!StringUtils.isBlank(failures)) {
            throw new ClientException(assetPath + " failed to process: " + failures);
        }
    }

    /**
     * Retrieve the asset processing status.
     *
     * @param assetPath Asset path
     * @return dam:assetState value (i.e. unProcessed, processing, processed)
     * @throws ClientException if something fails during the request/response cycle
     */
    public String getAssetStatus(String assetPath) throws ClientException {
        String requestPath = assetPath + "/jcr:content";
        JsonNode node = doGetJson(requestPath, 1, HttpStatus.SC_OK);
        JsonNode assetStateNode = node.get(DAM_ASSET_STATE);
        if (assetStateNode == null) {
            throw new ClientException("Property not found: " + requestPath + "/" + DAM_ASSET_STATE);
        } else {
            return assetStateNode.getTextValue();
        }
    }

    /**
     * Retrieve any processing failures with an asset.
     * Only able to find failures when Asset Compute is enabled, will return "" otherwise.
     *
     * @param assetPath Asset path
     * @return Processing failures or "" if there were no failures
     * @throws ClientException if something fails during the request/response cycle
     */
    public String getAssetProcessingFailures(String assetPath) throws ClientException {
        StringBuilder result = new StringBuilder();
        String requestPath = assetPath + "/jcr:content/dam:failedRenditions.2.json";
        SlingHttpResponse response = this.doGet(requestPath, HttpStatus.SC_OK, HttpStatus.SC_NOT_FOUND);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            JsonNode failedRenditions = JsonUtils.getJsonNodeFromString(response.getContent());
            Iterator<String> fieldNames = failedRenditions.getFieldNames();
            while (fieldNames.hasNext()) {
                String name = fieldNames.next();
                JsonNode n = failedRenditions.get(name);
                String reason = n.get("reason") != null ? n.get("reason").getTextValue() : null;
                String message = n.get("message") != null ? n.get("message").getTextValue() : null;
                if (reason != null && message != null) {
                    result.append("{ name: '");
                    result.append(name);
                    result.append("', reason: '");
                    result.append(reason);
                    result.append("', message: '");
                    result.append(message);
                    result.append("'}, ");
                }
            }
        }
        return result.toString();
    }

    //*********************************************
    // Creation
    //*********************************************

    public CQAssetsClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    /**
     * Check if Direct Binary Access is supported on the AEM instance
     *
     * @return True if supported, False is not supported
     * @throws ClientException Thrown if AEM is unreachable
     */
    private boolean checkDirectBinaryAccessSupport() throws ClientException {
        SlingHttpResponse response = doGet(
                ACP_LIST_CONTENT_DAM_FOLDER,
                HttpStatus.SC_OK,
                HttpStatus.SC_NOT_FOUND
        );

        // Older AEM instances don't have the Platform ACP API end point
        // These don't support direct binary access
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return false;
        }

        // The create link is currently only available on instances with Direct Binary Access,
        // but do a thorough test any way in case that changes
        JsonNode root = JsonUtils.getJsonNodeFromString(response.getContent());
        JsonNode createLink = Util.getJsonChildNode(root, ACP_LINKS, ACP_REL_CREATE);
        if (createLink != null) {
            String href = createLink.path(ACP_LINK_HREF).getTextValue();
            String type = createLink.path(ACP_LINK_TYPE).getTextValue();
            return DBA_CONTENT_DAM_INITIATE_UPLOAD.equals(href) && ACP_LINK_TYPE_DIRECT.equals(type);
        } else {
            return false;
        }
    }

    /**
     * Upload an asset using Direct Binary Access
     *
     * @param fileName Name of the file to upload
     * @param resourcePath Path to the resource available to the class loader
     * @param mimeType Mimetype of the asset
     * @param parentPath Parent path where to create the file
     * @throws ClientException Thrown on communication error with AEM
     */
    private SlingHttpResponse uploadAssetDBA(
            String fileName, String resourcePath, String mimeType, String parentPath
    ) throws ClientException {
        long fileSize = Util.getResourceSize(resourcePath);
        long startTime = System.currentTimeMillis();

        // Initiate upload
        InitiateUploadResponse r = initiateUpload(fileName, parentPath, fileSize);
        if (r.getCompleteURI().isEmpty()) {
            throw new ClientException("InitiateUpload response is missing the complete URI: " + r);
        }
        if (r.getFiles().size() != 1) {
            throw new ClientException("InitiateUpload response doesn't contain exactly 1 file: " + r);
        }
        InitiateUploadFile uploadFile = r.getFiles().get(0);
        List<String> uploadURIs = uploadFile.getUploadURIs();
        if (r.getFiles().size() < 1) {
            throw new ClientException("InitiateUpload response must contain at least 1 url: " + r);
        }
        long partSize = fileSize / uploadURIs.size();
        if (partSize > uploadFile.getMaxPartSize()) {
            // part sizes larger than maxPartSize may not work
            throw new ClientException("InitiateUpload response requires a partSize that's larger than maxPartSize: " + r + ", fileSize: " + fileSize);
        }

        // Upload each part
        long index = 0;
        for (String uri : uploadURIs) {
            long size = Math.min(partSize, fileSize - index);
            uploadAssetPart(
                    resourcePath,
                    mimeType,
                    URI.create(uri),
                    index,
                    size
            );
            index += size;
        }

        // Complete upload
        long uploadDuration = System.currentTimeMillis() - startTime;
        return completeUpload(
                r.getCompleteURI(),
                fileName,
                uploadFile.getUploadToken(),
                mimeType,
                uploadDuration,
                fileSize
        );
    }

    /**
     * Initiate the direct binary access upload to AEM
     *
     * @param fileName Name of the file to upload
     * @param parentPath Parent path where to create the file
     * @param fileSize Size of the file
     * @return initiate upload response with urls where to upload the asset, and a url to complete the asset
     * @throws ClientException Thrown when initiateUpload fails
     */
    private InitiateUploadResponse initiateUpload(String fileName, String parentPath, long fileSize) throws ClientException {
        String requestPath = parentPath + ".initiateUpload.json";
        try {
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addTextBody("fileName", fileName)
                    .addTextBody("fileSize", Long.toString(fileSize))
                    .setCharset(StandardCharsets.UTF_8)
                    .build();

            SlingHttpResponse response = doPost(requestPath, entity, HttpStatus.SC_OK);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getContent(), InitiateUploadResponse.class);
        } catch (IOException e) {
            throw new ClientException("Unable to parse JSON response for initiateUpload - " +
                    " requestPath: " + requestPath + ", fileName: " + fileName + ", fileSize: " + fileSize, e);
        }
    }

    /**
     * Upload a part of the asset.
     *
     * @param resourcePath Path to the resource to upload
     * @param mimeType Mimetype of the resource
     * @param targetUri Cloud URL where to upload the asset part
     * @param start Offset where to start reading the resource
     * @param size Number of bytes to upload
     * @throws ClientException Thrown when upload fails.
     */
    private void uploadAssetPart(String resourcePath, String mimeType, URI targetUri, long start, long size) throws ClientException {
        try (InputStream assetStream = ResourceUtil.getResourceAsStream(resourcePath)) {
            long skipped = assetStream.skip(start);
            if (skipped != start) {
                throw new AssertionError("Unable to skip InputStream - resource: " + resourcePath + ", expected: " + start + ", actual: " + skipped);
            }
            HttpPut request = new HttpPut(targetUri);
            request.setEntity(new InputStreamEntity(assetStream, size));
            List<Header> headers = new ArrayList<>();
            headers.add(new BasicHeader(HttpHeaders.CONTENT_TYPE, mimeType));
            doRequest(request, headers, HttpStatus.SC_CREATED);
        } catch (IOException e) {
            throw new ClientException("Unable to read resource: " + resourcePath, e);
        }
    }

    /**
     * Complete the upload, create the asset in the JCR
     *
     * @param completeUri URI to call in AEM to complete the upload
     * @param fileName Filename of the asset
     * @param uploadToken Upload token as a reference for Oak
     * @param mimeType Mimetype of the asset
     * @param uploadDuration Duration of the upload (metrics)
     * @param fileSize Size of the uploaded file (metrics)
     * @throws ClientException When completion fails
     */
    private SlingHttpResponse completeUpload(String completeUri, String fileName, String uploadToken, String mimeType,
                                             long uploadDuration, long fileSize) throws ClientException {
        HttpEntity entity = MultipartEntityBuilder.create()
                .addTextBody("fileName", fileName)
                .addTextBody("uploadToken", uploadToken)
                .addTextBody("mimeType", mimeType)
                .addTextBody("uploadDuration", Long.toString(uploadDuration))
                .addTextBody("fileSize", Long.toString(fileSize))
                .setCharset(StandardCharsets.UTF_8)
                .build();
        return doPost(completeUri, entity, HttpStatus.SC_OK);
    }

}
