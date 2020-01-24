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
import com.adobe.cq.testing.client.security.UserRule;
import org.apache.commons.io.IOUtils;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static org.apache.http.HttpStatus.SC_CREATED;

/**
 * Create a new page. This rule can be sub-classed to specify the parent page of the newly created
 * page. Subclasses can also specify which client to use or which server to target when a new page is created.
 */
public class Page extends ExternalResource {

    private static final String SITE_ROOT_PATH = "/content";
    private Logger logger = LoggerFactory.getLogger(Page.class);

    private final Supplier<SlingClient> clientSupplier;

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

    /**
     * @deprecated use {{public Page(Supplier<SlingClient> clientSupplier)}} instead.
     * @param quickstartRule An {code}Instance{code} object pointing to the remote test instance
     */
    public Page(Instance quickstartRule) {
        this(() -> quickstartRule.getAdminClient());
    }

    /**
     *
     * @param clientSupplier {code}Supplier{code} that returns an http client pointing to a remote test instance
     */
    public Page(Supplier<SlingClient> clientSupplier) {
        super();
        this.clientSupplier = clientSupplier;
    }

    @Override
    protected void before() throws ClientException, InterruptedException {
        getClient().createPageWithRetry(getName(), getTitle(), getParentPath(), "", 2000, 500);
    }

    @Override
    protected void after() {
        try {
            getClient().deletePageWithRetry(getPath(), true, false, 2000, 500);
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
     * The client to use to create and delete this page. The default implementation creates a {@link CQClient}.
     * The default implementation also uses the default admin user.
     *
     * @return The client to use to create and delete this page.
     * @throws ClientException if the client cannot be retrieved
     */
    protected CQClient getClient() throws ClientException {
        try {
            return this.clientSupplier.get().adaptTo(CQClient.class);
        } catch (Exception e) {
            throw new ClientException("Cannot create client", e);
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
     * The absolute path of this page.
     *
     * @return The absolute path of the page.
     */
    public final String getPath() {
        return getParentPath() + "/" + getName();
    }

}
