/*
 * Copyright 2021 Adobe Systems Incorporated
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

package com.adobe.cq.testing.util;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.CQConfigManagerClient;
import com.adobe.cq.testing.client.CQSecurityClient;
import com.adobe.cq.testing.client.TagClient;
import com.adobe.cq.testing.client.TemplateEditorManagerClient;
import com.adobe.cq.testing.client.security.Authorizable;
import com.adobe.cq.testing.client.security.CQAuthorizableManager;
import com.adobe.cq.testing.client.security.Group;
import com.adobe.cq.testing.client.security.User;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.clients.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.adobe.cq.testing.Constants.DEFAULT_RETRY_DELAY;
import static com.adobe.cq.testing.Constants.DEFAULT_TIMEOUT;
import static com.adobe.cq.testing.Constants.HOME_GROUPS;
import static com.adobe.cq.testing.Constants.HOME_USERS;
import static com.adobe.cq.testing.Constants.PROP_CQ_CONF;
import static com.adobe.cq.testing.Constants.CONTENT_DAM;
import static com.adobe.cq.testing.Constants.CONTENT_ROOT;
import static com.adobe.cq.testing.Constants.PROP_CQ_ALLOWED_TEMPLATES;
import static com.adobe.cq.testing.Constants.DEFAULT_SMALL_SIZE;

import static com.adobe.cq.testing.client.CQConfigManagerClient.CQConfigCapability.CONTENT_FRAGMENT_MODEL;
import static com.adobe.cq.testing.client.CQConfigManagerClient.CQConfigCapability.EDITABLE_TEMPLATES;
import static org.awaitility.Awaitility.await;


/**
 * Helper to build TestContent structure mainly.
 *
 * - /conf/{someprefix}_[randomSuffix]/....(*)
 * - /content/{someprefix}_[randomSuffix]/ (as folder)
 * - /content/dam/{someprefix}_[randomSuffix]/ (as folder)
 * - /content/cq:tag/{someprefix}_[randomSuffix]
 * - /home/groups/{someprefix}_[randomSuffix]/... (as intermediate path)
 * - /home/users/{someprefix}_[randomSuffix]/... (as intermediate path)
 *
 * (*) **Note**: /conf can include a default page template, content fragment structure model,
 * and content fragment template. Call the related **enable** methods.
 *
 * A [TestContentBuilder.dispose()](#dispose--) method is used to cleanup all those paths, as well as the associated /var/audit/... .
 */
public final class TestContentBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestContentBuilder.class);

    private static final String VAR_AUDIT_BASEPATH = "/var/audit/";
    private static final String VAR_AUDIT_WCM = VAR_AUDIT_BASEPATH + "com.day.cq.wcm.core.page";
    private static final String VAR_AUDIT_DAM = VAR_AUDIT_BASEPATH + "com.day.cq.dam";
    private static final String VAR_AUDIT_REPLICATION = VAR_AUDIT_BASEPATH + "com.day.cq.replication";
    private static final String CLEANING_MSG = "Cleaning {}";
    private static final String CLEANING_TAG_MSG = "Cleaning Tag {}";

    private static final String DEFAULT_PAGE_TEMPLATE_TITLE = "Simple-Template";
    private static final String DEFAULT_PAGE_TEMPLATE_DESCRIPTION = "A Simple Template for IT tests";

    private final CQClient client;
    private final String label;

    private String parentTag;

    private String damRootPath;
    private String pageTemplatePath;
    private String topLevelResponsiveGridPath;
    private String contentRootPath;

    private String pageTemplateTitle = DEFAULT_PAGE_TEMPLATE_TITLE;
    private String pageTemplateDescription = DEFAULT_PAGE_TEMPLATE_DESCRIPTION;

    private CQConfigManagerClient.CQConfig cqConfig;

    private String cqConfigPath;

    private String defaultUserName;
    private List<String> defaultGroups;
    private List<Group> groupsInternal;
    private String defaultPassword;
    private User defaultUser;
    private CQClient userClient;

    private boolean withEmptyTemplateEnabled;
    private boolean withDefaultPoliciesEnabled;
    private CQConfigManagerClient.CQConfigCapability[] cqConfigCapabilities = {CONTENT_FRAGMENT_MODEL, EDITABLE_TEMPLATES};

    /**
     * @param cqClient used to perform all operations, make sure it has all permissions to perform necessary operations (i.e administrator).
     * @param prefix used to prefix a {@link TestContentBuilder#randomSmallText()} for generated path.
     */
    public TestContentBuilder(final CQClient cqClient, final String prefix) {
        this.client = cqClient;
        this.label = prefix + "_" + randomSmallText();
    }

    /**
     * @return the generated initial {prefix}_[randomSuffix] value.
     */
    public String getLabel() {
        return label;
    }

    public String getParentTag() {
        return parentTag;
    }

    public String getConfigPath() {
        return cqConfigPath;
    }

    public String getDefaultPageTemplatePath() {
        return pageTemplatePath;
    }

    public String getTopLevelComponentPath() {
        return topLevelResponsiveGridPath;

    }


    public String getDamRootPath() {
        return damRootPath;
    }

    /**
     * @return  /content/{someprefix}_[randomSuffix]/
     */
    public String getContentRootPath() {
        return contentRootPath;
    }



    public TestContentBuilder withEmptyTemplate() {
        this.withEmptyTemplateEnabled = true;
        return this;
    }

    public TestContentBuilder withDefaultPolicies() {
        this.withDefaultPoliciesEnabled = true;
        return this;
    }

    public TestContentBuilder withConfigCapabilities (final CQConfigManagerClient.CQConfigCapability... capabilities) {
        this.cqConfigCapabilities = capabilities;
        return this;
    }

    public TestContentBuilder withPageTemplateTitle(final String title) {
        this.pageTemplateTitle = title;
        return this;
    }

    public TestContentBuilder withPageTemplateDescription(final String description) {
        this.pageTemplateTitle = description;
        return this;
    }

    public TestContentBuilder withUser(final String password, final List<String> groups) {
        this.defaultGroups = groups != null ? groups : Collections.emptyList();
        this.defaultUserName = label;
        this.defaultPassword = password;
        this.groupsInternal = new ArrayList<>();
        return this;
    }

    /**
     * - To be called when test content is no longer required. Usually in an after test handler.
     * See also [TestContentExtension](../junit5/extension/TestContentExtension.html).
     * - It will clean from all the root paths created, and it will also include the related /var/audits/... .
     * To make sure it doesn't leave garbages there as well.
     *
     * @throws ClientException
     * @throws InterruptedException
     */
    public void dispose() throws ClientException, InterruptedException {
        if (cqConfigPath != null) {
            LOGGER.info(CLEANING_MSG, cqConfigPath);
            client.deletePageWithRetry(cqConfigPath, true, false, DEFAULT_TIMEOUT, DEFAULT_RETRY_DELAY, HttpStatus.SC_OK);
            cleanupAudit(cqConfigPath);
        }
        if (damRootPath != null) {
            LOGGER.info(CLEANING_MSG, damRootPath);
            client.deletePageWithRetry(damRootPath, true, false, DEFAULT_TIMEOUT, DEFAULT_RETRY_DELAY, HttpStatus.SC_OK);
            cleanupAudit(damRootPath);
        }
        if (contentRootPath != null) {
            LOGGER.info(CLEANING_MSG, contentRootPath);
            client.deletePageWithRetry(contentRootPath, true, false, DEFAULT_TIMEOUT, DEFAULT_RETRY_DELAY, HttpStatus.SC_OK);
            cleanupAudit(contentRootPath);
        }

        if (parentTag != null) {
            LOGGER.info(CLEANING_TAG_MSG, parentTag);
            TagClient tagClient = client.adaptTo(TagClient.class);
            tagClient.deleteTag(parentTag);
        }
        if (defaultUser != null) {
            deleteGroups();
            deleteUser(defaultUser, defaultGroups.toArray(new String[0]));
            deletePathRetries(HOME_USERS + getLabel());
            deletePathRetries(HOME_GROUPS + getLabel());
        }
    }

    private void deletePathRetries(final String path) {
        LOGGER.info("Delete {} with retries", path);
        await().ignoreExceptions().until(() -> {
            client.deletePath(path);
            return true;
        });
    }

    public void cleanupAudit(final String path) {
        String[] audits = new String[] {VAR_AUDIT_DAM, VAR_AUDIT_WCM, VAR_AUDIT_REPLICATION};
        Arrays.stream(audits).forEach(s -> {
            String auditPath = s + path;
            try {
                if (client.exists(auditPath)) {
                    await().untilAsserted(() -> client.deletePath(auditPath, HttpStatus.SC_OK));
                }
            } catch (ClientException e) {
                LOGGER.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Perform the write operations to the AEM instance via provided CQClient to build up the **TestContent**.
     * See constructor [TestContentBuilder](#TestContentBuilder-com.adobe.cq.testing.client.CQClient-java.lang.String-).
     * @throws ClientException
     * @throws InterruptedException
     */
    public void build() throws ClientException, InterruptedException {
        createConfig();
        createDefaultPageTemplate();
        createDamRoot();
        createContentRoot();
        createDefaultTags();
        createDefaultUser();
    }

    private void createDamRoot() throws ClientException {
        damRootPath = client.createFolder(
                label.toLowerCase(),
                label,
                CONTENT_DAM,
                HttpStatus.SC_CREATED
        ).getSlingPath();
        client.setPageProperty(damRootPath, PROP_CQ_CONF, getConfigPath());
    }

    private void createContentRoot() throws ClientException {
        contentRootPath = client.createFolder(
                label.toLowerCase(),
                label,
                CONTENT_ROOT,
                HttpStatus.SC_CREATED
        ).getSlingPath();
        client.setPageProperty(contentRootPath, PROP_CQ_CONF, getConfigPath());
        client.setPageProperty(contentRootPath, PROP_CQ_ALLOWED_TEMPLATES, pageTemplatePath);
    }


    private void createDefaultUser()
            throws ClientException, InterruptedException {
        if (defaultUserName != null) {
            defaultUser = createUniqueUser(defaultUserName, defaultPassword, defaultGroups.toArray(new String[0]));
        }
    }

    public User createUniqueUser(final String username, final String password, final String... groups)
            throws ClientException, InterruptedException {
        User newUser = getCqSecurityClient().createUser(username + "_" + randomSmallText(), password, getLabel(), null, null, HttpStatus.SC_CREATED);
        LOGGER.info("Created user {} at path {}", newUser.getId(), newUser.getHomePath());
        final CQAuthorizableManager manager = getCqSecurityClient().getManager();
        for (String groupName : groups) {
            final Group group = manager.getGroup(groupName);
            await().ignoreExceptions().untilAsserted(() -> group.addMember(newUser));
            LOGGER.info("added {} to group {}", newUser.getId(), group.getId());
        }
        return newUser;
    }



    private void createDefaultPageTemplate() throws ClientException, InterruptedException {
        TemplateEditorManagerClient tmplClient = client.adaptTo(TemplateEditorManagerClient.class);
        pageTemplatePath = tmplClient.createHTML5(getConfigPath(), pageTemplateTitle, pageTemplateDescription);
        tmplClient.enable(pageTemplatePath);
        if (!withEmptyTemplateEnabled) {
            topLevelResponsiveGridPath = tmplClient.createTopLevelResponsiveGrid(pageTemplatePath,null);
            tmplClient.unlockStructureComponent(topLevelResponsiveGridPath);
            if (withDefaultPoliciesEnabled) {
                tmplClient.setComponentPolicy(topLevelResponsiveGridPath, "wcm/foundation/components/responsivegrid/default");
            } else {
                String policiesData = ResourceHelper.readResourceAsString("default-it-policies.json");
                tmplClient.importPolicy(cqConfigPath, policiesData);
                tmplClient.setComponentPolicy(topLevelResponsiveGridPath, "wcm/foundation/components/responsivegrid/it-default");
            }
        }
        LOGGER.info("Created Default Page Template {}", pageTemplatePath);
    }

    private void createConfig() throws ClientException {
        CQConfigManagerClient configManagerClient = client.adaptTo(CQConfigManagerClient.class);
        cqConfig = configManagerClient.create(label, cqConfigCapabilities);
        cqConfigPath = cqConfig.getPath();
        cqConfig.setWcmTemplatesPermissions();
        LOGGER.info("Created Config {}", cqConfigPath);
    }

    private void createDefaultTags() throws ClientException {
        TagClient tagClient = client.adaptTo(TagClient.class);
        parentTag = tagClient.createTag(label, label.toLowerCase(), null, null).getSlingPath();
        LOGGER.info("Created Tag root namespace under {}", parentTag);
    }



    public static String randomSmallText() {
        return RandomStringUtils.randomAlphabetic(DEFAULT_SMALL_SIZE);
    }

    public static String randomSmallTextSearch() {
        return "__" + randomSmallText() + "__";
    }

    public static String randomSmallTestLabel() {
        return "test" + randomSmallText();
    }

    public static String randomSmallTestTitle() {
        return "Test" + randomSmallText();
    }

    /**
     * @return generated user authorizable.
     */
    public User getUser() {
        assert defaultUser != null;
        return defaultUser;
    }

    /**
     * @return authorizable ID of the generated user.
     */
    public String getUserName() {
        assert defaultUser != null;
        return defaultUser.getId();
    }

    /**
     * @return authorizable path of the generated user in the repository.
     */
    public String getUserHomePath() {
        assert defaultUser != null;
        return defaultUser.getHomePath();
    }

    public Group createGroup(String groupPrefix) throws ClientException, InterruptedException {
        Group group = getCqSecurityClient().createGroup(groupPrefix + "_" + randomSmallText(), getLabel(), null, null, null);
        LOGGER.info("Created group {} at path {}", group.getId(), group.getHomePath());
        groupsInternal.add(group);
        return group;
    }

    public void deleteUser(final User user, final String... groups) throws ClientException {
        if (user != null) {
            LOGGER.info("Removing user: {} from groups:  {}", user.getHomePath(), groups);
            final CQAuthorizableManager manager = getCqSecurityClient().getManager();
            for (String groupName: groups) {
                Group group = manager.getGroup(groupName);
                await().ignoreExceptions().untilAsserted(() -> group.removeMembers(new Authorizable[] { user }, HttpStatus.SC_OK));
            }

            LOGGER.info("Deleting user:  {}", user.getHomePath());
            await().ignoreExceptions().untilAsserted(() -> user.delete());
        }
    }

    private void deleteGroups() {
        groupsInternal.forEach(internalGroup -> await().ignoreExceptions().untilAsserted(() -> internalGroup.delete(HttpStatus.SC_OK)));
        groupsInternal.clear();
    }

    /**
     * See constructor [TestContentBuilder](#TestContentBuilder-com.adobe.cq.testing.client.CQClient-java.lang.String-).
     * @return original constructor client.
     */
    public CQClient getClient() {
        return client;
    }

    /**
     * @return client based on the generated authorizable. See also [withUser](#withUser-java.lang.String-java.util.List-).
     * @throws ClientException
     */
    public CQClient getDefaultUserClient() throws ClientException {
        if (userClient == null) {
            userClient = getUserClient(defaultUser, defaultPassword);
        }
        return userClient;
    }

    /**
     * @return client based on the generated authorizable. See also [withUser](#withUser-java.lang.String-java.util.List-).
     * @throws ClientException
     */
    public CQClient getUserClient(final User user, final String password) throws ClientException {
        return CQClient.Builder.create(client.getUrl(), user.getId(), password).build();
    }

    /**
     * @return client based on the constructor CQClient.
     * @throws ClientException
     */
    public CQSecurityClient getCqSecurityClient() throws ClientException {
        return client.adaptTo(CQSecurityClient.class);
    }


}
