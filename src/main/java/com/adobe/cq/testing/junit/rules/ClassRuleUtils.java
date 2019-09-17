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
import org.apache.sling.testing.junit.rules.instance.ExistingInstance;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassRuleUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ClassRuleUtils.class);

    /**
     * If index lanes should be dynamically detected
     */
    protected static final boolean DETECT_INDEXING_LANES = Boolean.getBoolean("cq.testing.detectIndexingLanes");

    /**
     * Indexing lanes to use if not detected dynamically
     */
    protected static final String[] DEFAULT_INDEXING_LANES = new String[]{"async", "fulltext-async"};

    // TODO way to make these props homogenous?
    public static final String LOGIN_TOKEN_AUTH = "it.logintokenauth";

    public static class LoginTokenInstance extends ConfigureIndexingLaneInstance {
        @Override
        public <T extends SlingClient.InternalBuilder> T customize(T builder) {
            Lookup<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().build();
            builder.httpClientBuilder().setDefaultAuthSchemeRegistry(authSchemeRegistry);
            builder.setPreemptiveAuth(false).addInterceptorFirst(new FormBasedAuthInterceptor("login-token"));
            return builder;
        }
    }

    public static class ConfigureIndexingLaneInstance extends ExistingInstance {
        @Override
        public <T extends SlingClient> T getClient(Class<T> clientClass, String user, String pass) {
            return ClassRuleUtils.injectLaneNames(super.getClient(clientClass, user, pass));
        }

        @Override
        public <T extends SlingClient> T getAdminClient(Class<T> clientClass) {
            return ClassRuleUtils.injectLaneNames(super.getAdminClient(clientClass));
        }
    }

    /**
     * Create a new {@code Instance} object depending on the default auth mechanism
     * @param forceBasicAuth set to true to always use basic auth
     * @return the instance object
     */
    public static Instance newInstanceRule(boolean forceBasicAuth) {
        if (!forceBasicAuth && loginTokenAuth()) {
            LOG.info("Using LoginToken Auth as default");
            return new LoginTokenInstance();
        } else {
            LOG.info("Using Basic Auth as default");
            return new ConfigureIndexingLaneInstance();
        }
    }

    /**
     * Create a new {@code Instance} object depending on the default auth mechanism
     *
     * @return the instance object
     */
    public static Instance newInstanceRule() {
        return newInstanceRule(false);
    }

    /**
     * Should default login be with login-token?
     *
     * @return true if login token auth is configured
     */
    public static boolean loginTokenAuth() {
        return Boolean.getBoolean(LOGIN_TOKEN_AUTH);
    }

    private static <T extends SlingClient> T injectLaneNames(T in) {
        if (in != null && !DETECT_INDEXING_LANES) {
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
}
