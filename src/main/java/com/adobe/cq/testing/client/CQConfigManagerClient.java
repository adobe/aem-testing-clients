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

package com.adobe.cq.testing.client;


import com.adobe.cq.testing.client.security.CQPermissions;
import com.adobe.cq.testing.util.CSRFUtils;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.util.FormEntityBuilder;
import org.apache.sling.testing.clients.util.JsonUtils;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.sling.testing.Constants.CHARSET_UTF8;
import static org.apache.sling.testing.Constants.PARAMETER_CHARSET;

/**
 * Client to configs in create /conf structure. Basic Conf Structure will look similar to this
 * /conf
 * ----/confName
 *          jcr:title
 *     ----/settings
 *         ----/configCapability1
 *         ----/configCapability2
 * A config can be created using {@link #create(String title, CQConfigCapability...)}
 * To manipulate existing config use {@link  CQConfig}
 */
public final class CQConfigManagerClient extends CQClient {

    private static final String PARAM_TITLE = "configTitle";
    private static final String PARAM_NAME = "configName";
    private static final String PARAM_PARENT = "configParent";
    private static final String PARAM_CONFIG_CAPABILITIES = "configCapabilities";
    private static final String CREATE_ENDPOINT = "/conf.createconf.json";
    private static final String DEFAULT_ROOT_PATH = "/conf";
    private static final String CFM_RELATIVE_PATH = "/settings/dam/cfm";
    private static final String CFM_TEMPLATES_RELATIVE_PATH = CFM_RELATIVE_PATH + "/templates";
    private static final String PARAM_APPLY_TO = ":applyTo";
    private static final String PROP_JCR_TITLE = "./jcr:title";
    private static final long DEFAULT_RETRY_DELAY = Duration.ofSeconds(1).toMillis();
    private static final long DEFAULT_TIMEOUT = Duration.ofSeconds(30).toMillis();
    private static final String GROUPID_TEMPLATE_AUTHORS = "template-authors";
    private static final String GROUPID_CONTENT_AUTHORS = "content-authors";
    private static final String GROUPID_EVERYONE = "everyone";

    public CQConfigManagerClient(final CloseableHttpClient http, final SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public CQConfigManagerClient(final URI serverUrl, final String user, final String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /** Creates a config on default config path /conf
     *
     * @param configTitle title of the config to be created
     * @param capabilities capabilities of the configs eg Editable Templates
     * @return created config
     * @throws ClientException
     */
    public CQConfig create(final String configTitle, final CQConfigCapability... capabilities) throws ClientException {
        return create(DEFAULT_ROOT_PATH, configTitle, configTitle, capabilities);
    }

    /**
     * Created a  Config on the given path
     * @param parentPath path below which config should be created
     * @param configTitle title of the config to be created
     * @param capabilities capabilities of the configs eg Editable Templates
     * @return created config
     * @throws ClientException
     */
    public CQConfig create(final String parentPath, final String configTitle, final CQConfigCapability... capabilities) throws ClientException {
        return create(parentPath, configTitle, configTitle, capabilities);
    }

    /**
     * Created a  Config on the given path and with given name
     * @param parentPath path below which config should be created
     * @param configTitle title of the config to be created
     * @param configName node name for the config
     * @param capabilities capabilities of the configs eg Editable Templates
     * @return created config
     * @throws ClientException
     */
    public CQConfig create(final String parentPath, final String configTitle, final String configName, final CQConfigCapability... capabilities) throws ClientException {
        FormEntityBuilder formEntry = FormEntityBuilder.create()
                .addParameter(PARAMETER_CHARSET, CHARSET_UTF8)
                .addParameter(PARAM_TITLE, configTitle)
                .addParameter(PARAM_NAME, configName)
                .addParameter(PARAM_PARENT, parentPath);
        Arrays.stream(capabilities).forEach(c -> {
            formEntry.addParameter(PARAM_CONFIG_CAPABILITIES, c.getTitle());
        });
        doPost(CREATE_ENDPOINT, formEntry.build(), SC_OK);
        return new CQConfig(this, parentPath + "/" + configTitle);
    }

    /**
     * Helper class to update or delete an existing config.
     */
    public final class CQConfig {

        private static final String DELETE_ENDPOINT_SUFFIX = ".deleteconf.json";
        private static final String UPDATE_ENDPOINT_SUFFIX = ".updateconf.json";

        private final CQClient client;

        private String configPath;

        protected CQConfig(final CQClient client, final String path) {
            this.client = client;
            this.configPath = path;
        }

        /**
         * @return path of the config
         */
        public String getPath() {
            return configPath;
        }

        /**
         * Delete the path of current config.
         * @throws ClientException
         */
        public void delete() throws ClientException {
            FormEntityBuilder formEntry = FormEntityBuilder.create()
                    .addParameter(PARAMETER_CHARSET, CHARSET_UTF8)
                    .addParameter(PARAM_APPLY_TO, configPath);
            client.doPost(configPath + DELETE_ENDPOINT_SUFFIX, formEntry.build(), SC_OK);
        }

        /**
         * Updates the title of config.
         * @param newTitle  title to update
         * @throws ClientException
         */
        public void updateTitle(final String newTitle) throws ClientException {
            FormEntityBuilder formEntry = FormEntityBuilder.create()
                    .addParameter(PARAMETER_CHARSET, CHARSET_UTF8)
                    .addParameter(PROP_JCR_TITLE, newTitle)
                    .addParameter(CSRFUtils.PARAM_CSRF_TOKEN, CSRFUtils.createCSRFToken(client))
                    .addParameter(PARAM_APPLY_TO, configPath);
            client.doPost(configPath + UPDATE_ENDPOINT_SUFFIX, formEntry.build(), SC_OK);
        }

        /**
         * Imports the Template for ContentFragment into the current config
         * @param jsonString Structure of the the template to be imported into json format
         * @return created template path
         * @throws ClientException
         * @throws InterruptedException
         */
        public String importContentFragmentTemplate(final String jsonString) throws ClientException, InterruptedException {
            if (!client.exists(configPath + CFM_TEMPLATES_RELATIVE_PATH)) {
                // Create the templates page
                client.createPageWithRetry("templates", "Templates",
                        configPath + CFM_RELATIVE_PATH, null,
                        DEFAULT_TIMEOUT, DEFAULT_RETRY_DELAY);
            }
            return client.importJson(configPath + CFM_TEMPLATES_RELATIVE_PATH,
                    JsonUtils.getJsonNodeFromString(jsonString),
                    HttpStatus.SC_CREATED
            ).getSlingPath();
        }

        /** sets the require permissons for template-authors, content-authors and everyone group
         *  so that content-authors and everyone group can read templates and policies of current config and
         *  template-authors can update the templates and policies of the current config
         * @throws ClientException
         */
        public void setWcmTemplatesPermissions() throws ClientException {
            // add required permissions for template author
            CQSecurityClient sClient = client.adaptTo(CQSecurityClient.class);
            CQPermissions cqPermissions = new CQPermissions(sClient);
            final String templatePath = configPath + "/settings/wcm/templates";
            cqPermissions.changePermissions(GROUPID_EVERYONE, templatePath, true, false, false, false, false, false, false, SC_OK);
            cqPermissions.changePermissions(GROUPID_CONTENT_AUTHORS, templatePath, true, false, false, false, false, false, true, SC_OK);
            cqPermissions.changePermissions(GROUPID_TEMPLATE_AUTHORS, templatePath, true, true, true, true, false, false, true, SC_OK);
            cqPermissions.changePermissions("version-manager-service", templatePath, true, true, true, true, false, false, false, SC_OK);
            final String policiesPath = configPath + "/settings/wcm/policies";
            cqPermissions.changePermissions(GROUPID_EVERYONE, policiesPath, true, false, false, false, false, false, false, SC_OK);
            cqPermissions.changePermissions(GROUPID_CONTENT_AUTHORS, policiesPath, true, false, false, false, false, false, true, SC_OK);
            cqPermissions.changePermissions(GROUPID_TEMPLATE_AUTHORS, policiesPath, true, true, true, true, false, false, true, SC_OK);
            final String templateTypesPath = configPath + "/settings/wcm/template-types";
            cqPermissions.changePermissions(GROUPID_TEMPLATE_AUTHORS, templateTypesPath, true, false, false, false, false, false, false, SC_OK);
        }
    }

    /**
     * List of available Config Capabilities in AEM
     */
    public enum CQConfigCapability {
        CLOUD("Cloud Configurations"),
        CONTEXT_HUB("ContextHub segments"),
        CONTENT_FRAGMENT_MODEL("Content Fragment Models"),
        EDITABLE_TEMPLATES("Editable Templates"),
        GRAPHQL("GraphQL Persistent Queries");

        CQConfigCapability(final String s) {
            this.capabilityTitle = s;
        }

        private String capabilityTitle;

        /**
         * @return title of the config capability
         */
        public String getTitle() {
            return capabilityTitle;
        }
    }
}