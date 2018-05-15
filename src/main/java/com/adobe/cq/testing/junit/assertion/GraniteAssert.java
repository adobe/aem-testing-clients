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
import org.apache.http.HttpResponse;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.HttpUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.impl.DefaultPrettyPrinter;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ContainerNode;
import org.codehaus.jackson.node.ObjectNode;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Assertion utility methods for Granite integration testing.
 */
public class GraniteAssert {
    private static final Logger LOG = LoggerFactory.getLogger(GraniteAssert.class);

    public static void assertRegExMatch(String toValidate, String pattern) {
        assertRegExMatch(null, toValidate, pattern);
    }

    public static void assertRegExMatch(String message, String toValidate, String pattern) {
        assertRegExMatch(message, toValidate, Pattern.compile(pattern, Pattern.DOTALL));
    }

    public static void assertRegExMatch(String toValidate, Pattern pattern) {
        assertRegExMatch(null, toValidate, pattern);
    }

    public static void assertRegExMatch(String message, String toValidate, Pattern pattern) {
        // TODO: Should throw the NPE or IllegalArgumentException.
        // But it's late now, as changing the signature would break backwards compat
        Assert.assertNotNull("the string to validate must not be null", toValidate);
        Assert.assertNotNull("the pattern to test the string against must not be null", pattern);
        Assert.assertTrue(message, pattern.matcher(toValidate).matches());
    }

    public static void assertRegExNoMatch(String message, String toValidate,
                                          Pattern pattern) {
        Assert.assertNotNull("the string to validate must not be null", toValidate);
        Assert.assertNotNull("the pattern to test the string against must not be null", pattern);
        Assert.assertFalse(message, pattern.matcher(toValidate).matches());
    }

    public static void assertRegExFind(String toValidate, String pattern) {
        assertRegExFind(null, toValidate, pattern);
    }

    public static void assertRegExFind(String message, String toValidate, String pattern) {
        assertRegExFind(message, toValidate, Pattern.compile(pattern, Pattern.DOTALL));
    }

    public static void assertRegExFind(String toValidate, Pattern pattern) {
        assertRegExFind(null, toValidate, pattern);
    }

    public static void assertRegExFind(String message, String toValidate, Pattern pattern) {
        Assert.assertNotNull("the string to validate must not be null", toValidate);
        Assert.assertNotNull("the pattern to test the string against must not be null", pattern);

        Assert.assertTrue(message, pattern.matcher(toValidate).find());
    }

    public static void assertRegExNoFind(String message, String toValidate, Pattern pattern) {
        Assert.assertNotNull("the string to validate must not be null", toValidate);
        Assert.assertNotNull("the pattern to test the string against must not be null", pattern);

        Assert.assertFalse(message, pattern.matcher(toValidate).find());
    }

    public static void assertStatusCode(HttpResponse httpResponse, int statusCode) {
        assertStatusCode(null, httpResponse, new int[]{statusCode});
    }

    public static void assertStatusCode(String message, HttpResponse httpResponse, int statusCode) {
        assertStatusCode(null, httpResponse, new int[]{statusCode});
    }

    public static void assertStatusCode(HttpResponse httpResponse, int[] statusCodes) {
        assertStatusCode(null, httpResponse, statusCodes);
    }

    public static void assertStatusCode(String message, HttpResponse httpResponse, int[] statusCodes) {
        Assert.assertNotNull("the http response to test for return codes must not be null", httpResponse);
        Assert.assertNotNull("the codes to test the response against must not be null", statusCodes);

        try {
            HttpUtils.verifyHttpStatus(httpResponse, message, statusCodes);
        } catch (ClientException e) {
            throw new AssertionError("Unexpected response code", e);
        }
    }

    /**
     * Compares two json strings for equality in terms of structure and data.
     * <p>
     * There are some properties, like the lastModified property, that make it
     * hard to compare against a reference. In this case you can feed in a list
     * of of such properties, and they are ignored during comparison.
     *
     * @param jsonString1 expected
     * @param jsonString2 actual
     * @param ignoreProperties properties to be ignored
     */
    public static void assertJsonEquals(String jsonString1, String jsonString2, List<String> ignoreProperties) {
        assertJsonEquals(jsonString1, jsonString2, ignoreProperties, null);
    }

    /**
     * Compares two json strings for equality in terms of structure and data.
     * By default order of properties will be not taken in account.
     * <p>
     * There are some properties, like the lastModified property, that make it
     * hard to compare against a reference. In this case you can feed in a list
     * of of such properties, and they are ignored during comparison.
     *
     * @param jsonStringExpected expected
     * @param jsonStringActual actual
     * @param ignoreProperties properties to be ignored
     * @param ignoreNodes nodes to be ignored
     */
    public static void assertJsonEquals(String jsonStringExpected, String jsonStringActual, List<String> ignoreProperties,
                                        List<String> ignoreNodes) {
        assertJsonEquals(jsonStringExpected, jsonStringActual, ignoreProperties, ignoreNodes, true);
    }

    /**
     * Compares two json strings for equality in terms of structure and data.
     * <p>
     * There are some properties, like the lastModified property, that make it
     * hard to compare against a reference. In this case you can feed in a list
     * of of such properties, and they are ignored during comparison.
     *
     * @param jsonStringExpected expected
     * @param jsonStringActual actual
     * @param ignoreProperties properties to be ignored
     * @param ignoreNodes nodes to be ignored
     * @param ignoreOrderOfProperties ignore the order of properties
     */
    public static void assertJsonEquals(String jsonStringExpected, String jsonStringActual, List<String> ignoreProperties,
                                        List<String> ignoreNodes, boolean ignoreOrderOfProperties) {
        Assert.assertNotNull("the expected json to compare for equality must not be null", jsonStringExpected);
        Assert.assertNotNull("the actual json to compare for equality must not be null", jsonStringActual);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonTree1;
        JsonNode jsonTree2;
        try {
            jsonTree1 = mapper.readTree(jsonStringExpected);
            jsonTree2 = mapper.readTree(jsonStringActual);
        } catch (IOException e) {
            throw new AssertionError("Failed reading JSON string", e);
        }

        // remove ignore properties before comparing
        removeProperties(jsonTree1, ignoreProperties);
        removeProperties(jsonTree2, ignoreProperties);

        // remove ignore nodes before comparing
        removeNodes(jsonTree1, ignoreNodes);
        removeNodes(jsonTree2, ignoreNodes);

        if (ignoreOrderOfProperties) {
            // ignore order of properties
            List<String> failures = compareJson(jsonTree1, jsonTree2, new ArrayList<String>());

            if (failures.size() != 0) {
                for (String failure : failures) {
                    LOG.error(failure);
                }
                try {
                    Assert.assertEquals("This json strings are not equal:", prettyPrint(mapper, jsonTree1), prettyPrint(mapper, jsonTree2));
                } catch (IOException e) {
                    throw new AssertionError("Failed reading JSON string", e);
                }
            }
        } else {
            // don't ignore the order of properties
            Assert.assertEquals("The jsons are not equal", jsonTree1, jsonTree2);
        }
    }

    /**
     * The order of the properties can vary.
     *
     * @param expected expected JsonNode
     * @param actual actual JsonNode
     * @param failures list of failures
     * @return the failures list parameter extended with new failures
     */
    private static List<String> compareJson(JsonNode expected, JsonNode actual, List<String> failures) {
        if (expected.isValueNode() && actual.isValueNode()) {
            if (!expected.equals(actual)) {
                failures.add("Value nodes are not the same: Expected: " + expected + " // Actual: " + actual);
            }
        }

        if (expected.isArray() && actual.isArray()) {
            if (expected.size() == actual.size()) {
                for (int i = 0; i < expected.size(); i++) {
                    compareJson(expected.get(i), actual.get(i), failures);
                }
            } else {
                failures.add("Arrays of different size: Expected: " + expected + " // Actual: " + actual);
            }
        }

        if (expected.isObject() && actual.isObject()) {
            Set<String> expectedFields = getFieldNames(expected);
            Set<String> actualFields = getFieldNames(actual);

            if (expectedFields.equals(actualFields)) {
                for (String name : expectedFields) {
                    compareJson(expected.get(name), actual.get(name), failures);
                }
            } else {
                failures.add("Object with different properties: Expected: " + expected + " // Actual: " + actual);
            }
        }

        return failures;
    }

    private static Set<String> getFieldNames(JsonNode object) {
        Set<String> names = new HashSet<>();

        Iterator<String> i = object.getFieldNames();
        while (i.hasNext()) {
            names.add(i.next());
        }

        return names;
    }


    /**
     * Pretty-prints a json object
     */
    private static String prettyPrint(ObjectMapper mapper, JsonNode node) throws IOException {
        final StringWriter w = new StringWriter();
        final JsonGenerator g = mapper.getJsonFactory().createJsonGenerator(w);
        g.setPrettyPrinter(new DefaultPrettyPrinter());
        mapper.writeTree(g, node);
        return w.toString();
    }

    /**
     * Compares two json strings for equality in terms of structure and data.
     *
     * @param jsonString1 first string
     * @param jsonString2 second string
     */
    public static void assertJsonEquals(String jsonString1, String jsonString2) {
        assertJsonEquals(jsonString1, jsonString2, null, null);
    }

    /**
     * Recursively traverse the json tree and tests each object against the list
     * of ignore properties. If the object has such a ignore property, remove
     * it.
     *
     * @param baseNode the root
     * @param ignoreProperties list of properties to keep
     */
    private static void removeProperties(JsonNode baseNode,
                                         List<String> ignoreProperties) {
        if (ignoreProperties == null) return;

        for (String ignoreProperty : ignoreProperties) {
            JsonNode ignoreNode = baseNode.get(ignoreProperty);
            if (ignoreNode != null) {
                ((ObjectNode) baseNode).remove(ignoreProperty);
            }
        }

        for (JsonNode node : baseNode) {
            if (node instanceof ContainerNode) {
                removeProperties(node, ignoreProperties);
            }
        }
    }

    /**
     * Remove nodes from the json tree
     *
     * @param baseNode the root
     * @param ignoreNodes list of nodes to keep from deleting
     */
    private static void removeNodes(JsonNode baseNode, List<String> ignoreNodes) {
        if (ignoreNodes == null) return;
        for (String ignoreNodeName : ignoreNodes) {
            JsonNode ignoreNode = baseNode.get(ignoreNodeName);
            if (ignoreNode != null) {
                ((ObjectNode) baseNode).remove(ignoreNodeName);
            }
        }

        for (JsonNode node : baseNode) {
            if (node instanceof ContainerNode) {
                removeNodes(node, ignoreNodes);
            }
        }
    }

    /**
     * Checks if an replication agent is enabled. Asserts if its not the case.
     *
     * @param client    The client used for verification
     * @param agentPath The path to the agent
     */
    public static void assertIsAgentEnabled(CQClient client, String agentPath) {
        JsonNode node;
        try {
            node = client.doGetJson(agentPath, 1);
        } catch (ClientException e) {
            throw new AssertionError("Requesting " + agentPath + ".1.json failed!: ", e);
        }

        // get the content node
        node = node.get("jcr:content");

        // check if node was found
        Assert.assertFalse("Missing " + agentPath + "/jcr:content !", node.isMissingNode());

        // check if the property enabled is set
        node = node.get("enabled");

        // check if node was found
        Assert.assertFalse("Missing " + agentPath + "/jcr:content/enabled !", node.isMissingNode());

        // check if node is properly set
        Assert.assertEquals("Agent " + agentPath + " is not enabled!", "true", node.getValueAsText());
    }

}
