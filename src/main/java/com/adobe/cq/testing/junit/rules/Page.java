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

package com.adobe.cq.testing.junit.rules;

import com.adobe.cq.testing.client.CQClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_CREATED;

/**
 * Create a new page. This rule can be sub-classed to specify the parent page and the template of the newly created
 * page. Subclasses can also specify which client to use or which server to target when a new page is created.
 */

public class Page extends ExternalResource {

    private Logger logger = LoggerFactory.getLogger(Page.class);

    private static final String SITE_ROOT_PATH = "/content/test-site";
    private static final String TEMPLATE_ROOT_PATH = "/conf/test-site";
    private static final String TEMPLATE_PATH = "/conf/test-site/settings/wcm/templates/content-page";

    private final Instance quickstartRule;

    private ThreadLocal<String> parentPath = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return initialParentPath();
        }
    };
    private ThreadLocal<String> name = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return initialName();
        }
    };
    private ThreadLocal<String> templatePath = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return initialTemplatePath();
        }
    };

    public Page(Instance quickstartRule) {
        super();
        this.quickstartRule = quickstartRule;
    }

    @Override
    protected void before() throws ClientException {
        prepare();
        SlingHttpResponse response = getClient().createPage(getName(), getTitle(), getParentPath(), getTemplatePath(), HttpStatus.SC_OK);
        logger.info("Created page at {}", response.getSlingLocation());

    }

    @Override
    protected void after() {
        try {
            getClient().deletePage(new String[]{getPath()}, true, false);
            logger.info("Deleted page at {}", getPath());
        } catch (Exception e) {
            logger.error("Unable to delete the page", e);
        }
    }

    /**
     * The initial parent path to be used when creating the page
     * This implementation returns {@value SITE_ROOT_PATH}
     * You can override this with dynamic names for each threads
     * @return the parent path used to create the page
     */
    protected String initialParentPath() {
        return SITE_ROOT_PATH;
    }

    /**
     * The initial page name to be used when creating the page
     * This implementation returns "testpage_[randomUUID]"
     * You can override this with dynamic names for each thread
     *
     * @return the page name used to create the page
     */
    protected String initialName() {
        return "testpage_" + UUID.randomUUID();
    }

    /**
     * The initial template path to be used when creating the page
     * This implementation returns {@value TEMPLATE_PATH}
     * You can override this with dynamic names for each threads
     *
     * @return the template path used to create the page
     */
    protected String initialTemplatePath() {
        return TEMPLATE_PATH;
    }

    /**
     * The client to use to create and delete this page. The default implementation creates a {@link CQClient}.
     * The default implementation also uses the default admin user.
     *
     * @return The client to use to create and delete this page.
     * @throws ClientException if the client cannot be retrieved
     */
    protected CQClient getClient() throws ClientException {
        return quickstartRule.getAdminClient(CQClient.class);
    }

    /**
     * Method to be called before creating the page.
     * You can override this to perform custom operations before creating the page.
     * This implementation creates, if needed, the test template and site.
     *
     * @throws ClientException if the content cannot be created
     */
    protected void prepare() throws ClientException {
        if (!getClient().exists(TEMPLATE_ROOT_PATH)) {
            try {
                InputStream templateStream =
                        ResourceUtil.getResourceAsStream("/com/adobe/cq/testing/junit/rules/template.json");
                String template = IOUtils.toString(templateStream, StandardCharsets.UTF_8);
                getClient().importContent(TEMPLATE_ROOT_PATH, "json", template, SC_CREATED);
                logger.info("Created test template in {}", TEMPLATE_ROOT_PATH);
            } catch (IOException e) {
                throw new ClientException("Failed to create test template.", e);
            }
        }

        if (!getClient().exists(SITE_ROOT_PATH)) {
            try {
                InputStream siteStream =
                        ResourceUtil.getResourceAsStream("/com/adobe/cq/testing/junit/rules/site.json");
                String site = IOUtils.toString(siteStream, StandardCharsets.UTF_8);
                getClient().importContent(SITE_ROOT_PATH, "json", site, SC_CREATED);
                logger.info("Created test site {}", SITE_ROOT_PATH);
            } catch (IOException e) {
                throw new ClientException("Failed to create test site.", e);
            }
        }
    }

    /**
     * The title of this page. The default implementation returns the value {@code "Test Page"}.
     *
     * @return The title of this page.
     */
    public String getTitle() {
        return "Test Page";
    }

    /**
     * The node name of this page. The default implementation returns the value of {@link #initialName()}.
     *
     * @return The node name of this page.
     */
    public String getName() {
        return name.get();
    }

    /**
     * The path of the parent of this page. The default implementation returns the value {@link #initialParentPath()}
     *
     * @return The parent path of this page. The path must be absolute and must not end with a slash.
     */
    public String getParentPath() {
        return parentPath.get();
    }

    /**
     * The path of the template of this page. The default implementation returns the value {@link #initialTemplatePath()}
     *
     * @return The parent path of this page. The path must be a valid template path.
     */
    public String getTemplatePath() {
        return templatePath.get();
    }

    /**
     * The absolute path of this page.
     *
     * @return The absolute path of the page.
     */
    public final String getPath() {
        return getParentPath() + "/" + getName();
    }

}
