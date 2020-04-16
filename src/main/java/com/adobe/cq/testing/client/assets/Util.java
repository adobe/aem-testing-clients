package com.adobe.cq.testing.client.assets;

import org.apache.http.Header;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Util {

    /**
     * Retrieve the size of a resource.
     *
     * Could be optimized by checking the URL and if it's a file on disk to get the
     * file size directly. The code tries to be general though, and the assets uploaded
     * should be small.
     *
     * @param resourcePath Path to the resource available to the class loader
     * @return Size of the resource
     * @throws ClientException Thrown if the size of the resource can't be calculated
     */
    // TODO: Contribute to org.apache.sling.testing.clients.util.ResourceUtil
    // TODO: Optimize if resource can be directly asset as a file
    public static long getResourceSize(String resourcePath) throws ClientException {
        try (InputStream in = ResourceUtil.getResourceAsStream(resourcePath)) {
            byte[] buffer = new byte[100000];
            long size = 0;
            for (;;) {
                int bytesRead = in.read(buffer);
                if (bytesRead < 0) {
                    return size;
                }
                size += bytesRead;
            }
        } catch (IOException e) {
            throw new ClientException("Unable to get size of resource: " + resourcePath);
        }
    }

    /**
     * Check if the content length of the response is equal to the given value
     *
     * @param response HTTP response
     * @param expected Expected content length
     * @throws ClientException Thrown if the content-length of the HTTP response doesn't match expected
     */
    // TODO: Contribute to org.apache.sling.testing.clients.SlingHttpResponse
    public static void checkContentLength(SlingHttpResponse response, long expected) throws ClientException {
        long contentLength = response.getEntity().getContentLength();
        if (contentLength != expected) {
            throw new ClientException(response + " has wrong content length (" + contentLength + "). Expected " + expected);
        }
    }

    /**
     * Check if the content disposition header of the response is equal to the given value
     *
     * @param response HTTP response
     * @param expected Expected content disposition
     * @throws ClientException Thrown if the content-disposition of the HTTP response doesn't match expected
     */
    // TODO: Contribute to org.apache.sling.testing.clients.SlingHttpResponse
    public static void checkContentDisposition(SlingHttpResponse response, String expected) throws ClientException {
        Header[] headers = response.getHeaders("Content-Disposition");
        if (headers == null || headers.length == 0) {
            throw new ClientException(response + " has no content disposition. Expected " + expected);
        } else if (headers.length > 1) {
            throw new ClientException(response + " has more than 1 content disposition (" + Arrays.toString(headers) + "). Expected " + expected);
        } else {
            String contentDisposition = headers[0].getValue();
            if (!expected.equals(contentDisposition)) {
                throw new ClientException(response + " has wrong content disposition (" + contentDisposition + "). Expected " + expected);
            }
        }
    }

    /**
     * Retrieve a child node
     *
     * @param parent Parent node
     * @param children Each child goes down one level, e.g. (root, a, b) will go to root/a/b
     * @return Child node or null if the node doesn't exist
     */
    // TODO: Contribute to org.apache.sling.testing.clients.util.JsonUtils
    public static JsonNode getJsonChildNode(JsonNode parent, String... children) {
        JsonNode node = parent;
        int i = 0;
        while ((node != null) && (i < children.length)) {
            node = node.get(children[i++]);
        }
        return node;
    }

    /**
     * Retrieve the text value of a field
     *
     * @param parent Parent node
     * @param fieldName Field to retrieve
     * @return Text value in field or null if the field doesn't exist
     */
    // TODO: Contribute to org.apache.sling.testing.clients.util.JsonUtils
    public static String getJsonTextValue(JsonNode parent, String fieldName) {
        JsonNode field = parent.get(fieldName);
        return field != null ? field.getTextValue() : null;
    }

    /**
     * Retrieve the long value of a field
     *
     * @param parent Parent node
     * @param fieldName Field to retrieve
     * @return Long value in field or 0 if the field doesn't exist
     */
    // TODO: Contribute to org.apache.sling.testing.clients.util.JsonUtils
    public static long getJsonLongValue(JsonNode parent, String fieldName) {
        JsonNode field = parent.get(fieldName);
        return field != null ? field.getLongValue() : 0;
    }

    private Util() {
    }

}
