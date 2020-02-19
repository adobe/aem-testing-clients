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

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.indexing.IndexingClient;
import org.apache.sling.testing.clients.interceptors.FormBasedAuthInterceptor;
import org.apache.sling.testing.junit.rules.instance.BuilderCustomizer;
import org.apache.sling.testing.junit.rules.instance.ExistingInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurableInstance extends ExistingInstance {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurableInstance.class);

    // TODO way to make these props homogenous?
    public static final String LOGIN_TOKEN_AUTH = "it.logintokenauth";

    /**
     * If index lanes should be dynamically detected
     */
    private static final String DETECT_INDEXING_LANES = "it.detectIndexingLanes";
    private final boolean useLoginToken;

    /**
     * Indexing lanes to use if not detected dynamically
     */
    private static final String[] DEFAULT_INDEXING_LANES = new String[]{"async", "fulltext-async"};
    private final boolean detectIndexLanes;

    public ConfigurableInstance(boolean forceBasicAuth) {
        this(forceBasicAuth, false);
    }

    public ConfigurableInstance(boolean forceBasicAuth, boolean forceIndexLaneDetection) {
        this.useLoginToken = !forceBasicAuth && loginTokenAuth();
        this.detectIndexLanes = forceIndexLaneDetection || shouldDetectIndexingLanes();

        LOG.info("Using {} Auth as default. Index lane detection: {}",
                (useLoginToken ? "LoginToken" : "Basic"), detectIndexLanes);
    }

    @Override
    public <T extends SlingClient> T newClient(Class<T> clientClass, String user, String pass, BuilderCustomizer... customizers) {
        return injectLaneNames(super.newClient(clientClass, user, pass, customizers));
    }

    @Override
    public <T extends SlingClient.InternalBuilder> T customize(T builder) {
        if (useLoginToken) {
            Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().build();
            builder.httpClientBuilder().setDefaultAuthSchemeRegistry(authSchemeRegistry);
            builder.setPreemptiveAuth(false).addInterceptorFirst(new FormBasedAuthInterceptor("login-token"));
            return builder;
        } else {
            return super.customize(builder);
        }
    }

    /**
     * Should index lanes should be dynamically detected
     *
     * @return true if index lanes should be detected automatically
     */
    private static boolean shouldDetectIndexingLanes() {
        return Boolean.getBoolean(DETECT_INDEXING_LANES);
    }

    private <T extends SlingClient> T injectLaneNames(T in) {
        if (in != null && !detectIndexLanes) {
            try {
                IndexingClient client = in.adaptTo(IndexingClient.class);
                client.setLaneNames(DEFAULT_INDEXING_LANES);
            } catch (ClientException ce) {
                LOG.error("Error occurred while configuring indexing lanes. Returning null", ce);
                in = null;
            }
        }
        return in;
    }

    /**
     * Should default login be with login-token?
     *
     * @return true if login token auth is configured
     */
    private static boolean loginTokenAuth() {
        if (System.getProperties().contains(LOGIN_TOKEN_AUTH)) {
            return Boolean.getBoolean(LOGIN_TOKEN_AUTH);
        } else {
            return true;
        }
    }
}