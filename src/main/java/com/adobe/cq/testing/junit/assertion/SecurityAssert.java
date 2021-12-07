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

import com.adobe.cq.testing.client.SecurityClient;
import com.adobe.cq.testing.client.security.Authorizable;
import com.adobe.cq.testing.client.security.Group;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.sling.testing.clients.ClientException;
import org.junit.Assert;

import java.util.HashMap;
import java.util.Map;

public class SecurityAssert {

    /**
     * The ace schema file = {@code {@value}} *
     */
    private static final String SCHEMA_ACE = "/schemas/json/ace-schema.json";
    public static final String REP_POLICY = "rep:policy";

    /**
     * Assert authorizableMember is member of authorizableMemberOf
     *
     * @param authorizableMemberOf any {@link Group} that has the memberEntry
     * @param authorizableMember   authorizable that is member of the other authorizable
     * @param <T> a class that extends Authorizable
     *
     * @throws ClientException if the membership cannot be retrieved
     * @throws InterruptedException to mark this operation as "waiting"
     */
    public static <T extends Authorizable> void assertIsMemberOf(Group authorizableMemberOf, T authorizableMember)
            throws ClientException, InterruptedException {
        Assert.assertTrue("Authorizable '" + authorizableMember.getId() + "' should be member of group " +
                authorizableMemberOf.getId() + ".", authorizableMemberOf.hasGroupMember(authorizableMember));
    }

    /**
     * Assert authorizableMemberOf is NOT member of authorizableMember
     *
     * @param authorizableMemberOf any {@link Group} that has the memberEntry
     * @param authorizableMember   authorizable that is member of the other authorizable
     * @param <T> a class that extends Authorizable
     *
     * @throws ClientException if the membership cannot be retrieved
     * @throws InterruptedException to mark this operation as "waiting"
     */
    public static <T extends Authorizable> void assertIsNotMember(Group authorizableMemberOf, T authorizableMember)
            throws ClientException, InterruptedException {
        Map<String, Authorizable> members = authorizableMemberOf.getMembers();
        Assert.assertFalse("Authorizable '" + authorizableMember.getId() + "' may not be member of group " +
        authorizableMemberOf.getId() + ".", members.containsKey(authorizableMember.getId()));
    }


    /**
     * Assert authorizableMember is NOT member of authorizableMemberOf
     *
     * @param authorizableMemberOf any {@link Group} that has the memberEntry
     * @param authorizableMember   authorizable that is member of the other authorizable
     * @param <T> Authorizable class
     *
     * @throws ClientException if the membership cannot be retrieved
     * @throws InterruptedException to mark this operation as "waiting"
     */
    public static <T extends Authorizable> void assertIsNotMemberOf(Group authorizableMemberOf, T authorizableMember)
            throws ClientException, InterruptedException {
        Map<String, Authorizable> memberOf = authorizableMember.getMemberOf();
        Assert.assertFalse("Authorizable '" + authorizableMember.getId() + "' may not be member of group " +
                authorizableMemberOf.getId() + ".", memberOf.containsKey(authorizableMemberOf.getId()));
    }

    /**
     * Assert user is a valid user
     *
     * @param client   {@link SecurityClient}
     * @param userPath the path to the user, starting from root, encoded
     * @param <T> client type
     *CQXSSUtils
     * @throws ClientException if something fails during request/response cycle
     */
    public static <T extends SecurityClient> void assertValidUser(T client, String userPath)
            throws ClientException {
        if (client == null) {
            throw new IllegalArgumentException("Client may not be null!");
        }
        if (userPath == null) {
            throw new IllegalArgumentException("Path to user may not be null!");
        }

        JsonNode userNode = client.doGetJson(userPath, -1);

        // check resource type
        Assert.assertEquals("'jcr:primaryType' for '" + userPath + "' is not 'rep:User'!", "rep:User",
                userNode.get("jcr:primaryType").asText());

        // check permissions / rep:policy node exist
        assertACE(client, userPath);
    }

    /**
     * Assert permissions / rep:policy node exist and has at minimum one allow or deny node
     *
     * @param client   {@link SecurityClient}
     * @param userPath the path to the user, starting from root
     * @param <T> client type
     *
     * @throws ClientException if something fails during request/response cycle
     */
    public static <T extends SecurityClient> void assertACE(T client, String userPath) throws ClientException {
        if (client == null) {
            throw new IllegalArgumentException("Client may not be null!");
        }
        if (userPath == null) {
            throw new IllegalArgumentException("Path to user may not be null!");
        }

        JsonNode userNode = client.doGetJson(userPath, -1);

        JsonNode policyNode = userNode.get(REP_POLICY);
        // check node exists
        Assert.assertFalse("'" + REP_POLICY + "' node for '" + userPath + "' is missing!", policyNode.isMissingNode());

        // check node has correct primary type
        Assert.assertEquals("'jcr:primaryType' for '" + userPath + "/" + REP_POLICY + "' is wrong!", "rep:ACL",
                policyNode.get("jcr:primaryType").asText());

        // check node has at minimum one allow or deny node
        JsonNode allowNode = policyNode.get("allow");
        JsonNode denyNode = policyNode.get("deny");
        Assert.assertTrue("'rep:policy' node for '" + userPath + "' has no entries (allow / deny)!",
                !allowNode.isMissingNode() || !denyNode.isMissingNode());

        assertACENode(userPath, allowNode);
        if (denyNode != null) assertACENode(userPath, denyNode);
    }

    /**
     * Assert ACE node has correct resource type and structure
     *
     * @param userPath user path
     * @param aceNode ace node
     */
    public static void assertACENode(String userPath, JsonNode aceNode) {
        // FIXME find solution to validate schema
        //assertSchemaValid(aceNode.toString(), SCHEMA_ACE);
        Assert.assertEquals("'jcr:primaryType' for '" + userPath + "' is not 'rep:GrantACE'!", "rep:GrantACE",
                aceNode.get("jcr:primaryType").asText());
    }

    /**
     * Assert group is a valid group
     *
     * @param client    {@link SecurityClient}
     * @param groupPath the path to group starting from root
     * @param <T> client type
     *
     * @throws ClientException if something fails during request/response cycle
     *
     */
    public static <T extends SecurityClient> void assertValidGroup(T client, String groupPath)
            throws ClientException {
        if (client == null) {
            throw new IllegalArgumentException("Client may not be null!");
        }
        if (groupPath == null) {
            throw new IllegalArgumentException("Path to group may not be null!");
        }
        JsonNode userNode = client.doGetJson(groupPath, 0);

        // check resource type
        Assert.assertEquals("'jcr:primaryType' for '" + groupPath + "' is not 'rep:Group'!", "rep:Group",
                userNode.get("jcr:primaryType").asText());
    }

    /**
     * Assert profile information is correctly set
     *
     * @param client           {@link SecurityClient}
     * @param authorizablePath the path to the user, starting from root
     * @param expectedProps    properties as map: key = propertyName, value = expected value
     * @param <T>              client type
     *
     * @throws ClientException if something fails during request/response cycle
     *
     */
    public static <T extends SecurityClient> void assertProfile(T client, String authorizablePath,
                                                                HashMap<String, String> expectedProps)
            throws ClientException {
        JsonNode userNode = client.doGetJson(authorizablePath, -1);

        // check profile node exists
        JsonNode profileNode = userNode.path("profile");
        Assert.assertFalse("UserProfile node for '" + authorizablePath + "' is missing!", profileNode.isMissingNode());

        // check profile node has correct jcr:primaryType
        String expectedResourceType = "nt:unstructured";
        Assert.assertEquals("'jcr:primaryType' for '" + authorizablePath + "/profile' is wrong'!",
                expectedResourceType,
                profileNode.get("jcr:primaryType").asText());

        // check profile properties
        if (expectedProps == null) return; // nothing to check

        for (String propName : expectedProps.keySet()) {
            Assert.assertNotNull("Users property '" + propName + "' does not exist in profile.", profileNode.get(propName));
            String expectedValue = expectedProps.get(propName);
            Assert.assertEquals("Users property '" + propName + "' does not match expected value in profile node.",
                    expectedValue,
                    profileNode.get(propName).asText());
        }
    }

}
