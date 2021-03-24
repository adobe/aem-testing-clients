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

package com.adobe.cq.testing.junit.extensions;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.Constants;
import com.adobe.cq.testing.junit.annotations.SlingClientConfig;
import com.adobe.cq.testing.junit.annotations.SlingClientContext;
import com.adobe.cq.testing.junit.annotations.WithClient;
import com.adobe.cq.testing.util.AnnotationHelper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.instance.InstanceConfiguration;
import org.apache.sling.testing.clients.instance.InstanceSetup;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.adobe.cq.testing.util.GraniteBackwardsCompatibility.translateGranitePropertiesToSling;

public final class SlingClientExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private Store clientStore = Store.getInstance();

    @Override
    public void afterAll(final ExtensionContext extensionContext) throws Exception {
        after(extensionContext);
    }

    @Override
    public void beforeAll(final ExtensionContext extensionContext) throws Exception {
        // Allow backward compatibility parameter
        translateGranitePropertiesToSling();
        setupWithSlingClientConfig(extensionContext);
    }

    private void setupWithSlingClientConfig(final ExtensionContext extensionContext) throws URISyntaxException {
        Class<?> requiredTestClass = extensionContext.getRequiredTestClass();
        SlingClientConfig[] annotationsByType = requiredTestClass.getAnnotationsByType(SlingClientConfig.class);
        for (SlingClientConfig config : annotationsByType) {
            URI uri = new URIBuilder()
                    .setPath(config.contextPath())
                    .setHost(config.host())
                    .setPort(config.port())
                    .setScheme(config.scheme())
                    .build();
            InstanceConfiguration instanceConfiguration = new InstanceConfiguration(uri, config.runMode(), config.username(), config.password());
            InstanceSetup.get().getConfigurations().add(instanceConfiguration);
        }
    }

    private void after(final ExtensionContext extensionContext) {
        clientStore.clear(extensionContext);
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {
        return isOfType(parameterContext, SlingClient.class, CQClient.class);
    }

    private boolean isOfType(final ParameterContext context, final Class...clazz) {
        return Arrays.stream(clazz).anyMatch(p -> p.isAssignableFrom(context.getParameter().getType()));
    }

    private String getDefaultRunMode(final ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, SlingClientContext.class)
                .map(SlingClientContext::defaultRunMode)
                .orElseThrow(() -> new ExtensionConfigurationException("@CQClientContext not found"));
    }

    private boolean getDefaultForceAnonymous(final ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, SlingClientContext.class)
                .map(SlingClientContext::defaultAnonymous)
                .orElseThrow(() -> new ExtensionConfigurationException("@CQClientContext not found"));
    }

    private String getRunMode(final ParameterContext parameterContext) {
        Optional<WithClient> annotation = parameterContext.findAnnotation(WithClient.class);
        return annotation.orElse(new DefaultWithClient()).runMode();
    }

    private boolean getForceAnonymous(final ParameterContext parameterContext) {
        return parameterContext.findAnnotation(WithClient.class).orElse(new DefaultWithClient()).forceAnonymous();
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext extensionContext) {

        String runMode = getDefaultRunMode(extensionContext);
        boolean forceAnonymous = getDefaultForceAnonymous(extensionContext);

        if (parameterContext.isAnnotated(WithClient.class)) {
            runMode = getRunMode(parameterContext);
            forceAnonymous = getForceAnonymous(parameterContext);
        }

        SlingClient client = clientStore.getOrCompute(extensionContext, runMode, forceAnonymous);
        Store.getInstance().saveLatest(extensionContext, client);

        assert client != null;

        Object returnedObject = client;
        if (isOfType(parameterContext, CQClient.class)) {
            try {
                returnedObject = client.adaptTo(CQClient.class);
            } catch (ClientException e) {
                throw new ParameterResolutionException(e.getMessage());
            }
        }

        return returnedObject;
    }

    public static final class Store {

        private static final Logger LOGGER = LoggerFactory.getLogger(Store.class);

        private static Store instance = new Store();

        private static final Map<String, InstanceConfiguration> DEFAULT_CONFIGURATION_MAP = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>(Constants.RUNMODE_AUTHOR,
                        new InstanceConfiguration(
                                URI.create(Constants.DEFAULT_AUTHOR_URL),
                                Constants.RUNMODE_AUTHOR,
                                Constants.DEFAULT_USER,
                                Constants.DEFAULT_PASSWORD
                        )
                ),
                new AbstractMap.SimpleImmutableEntry<>(Constants.RUNMODE_PUBLISH,
                        new InstanceConfiguration(
                                URI.create(Constants.DEFAULT_PUBLISH_URL),
                                Constants.RUNMODE_PUBLISH,
                                Constants.DEFAULT_USER,
                                Constants.DEFAULT_PASSWORD
                        )
                )
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        private Store() {
        }

        private HashMap<String, SlingClient> getClientMapFromStore(final ExtensionContext extensionContext) {
            HashMap<String, SlingClient> stringSlingClientHashMap = (HashMap<String, SlingClient>) extensionContext
                    .getStore(Constants.NAMESPACE).get(Constants.SLING_CLIENTS_MAP_KEY);
            if (stringSlingClientHashMap == null) {
                stringSlingClientHashMap = new HashMap<>();
                extensionContext
                        .getStore(Constants.NAMESPACE).put(Constants.SLING_CLIENTS_MAP_KEY, stringSlingClientHashMap);
            }
            return stringSlingClientHashMap;
        }

        private SlingClient buildClient(final URI url, final String username, final String password, final boolean forceAnonymous) throws ClientException {
            LOGGER.info("Building client for url={} user={}", url.toString(), username);
            return SlingClient.Builder.create(url, forceAnonymous ? null : username, password).build();
        }

        private InstanceConfiguration getInstanceConfiguration(String runMode) {
            InstanceSetup instanceSetup = InstanceSetup.get();
            List<InstanceConfiguration> confList = instanceSetup.getConfigurations(runMode);
            InstanceConfiguration conf = DEFAULT_CONFIGURATION_MAP.get(runMode);
            if (!confList.isEmpty()) {
                conf = confList.get(0);
            }
            return conf;
        }

        private String getKeyPrefix() {
            return "_" + Thread.currentThread().getId() + "_";
        }

        public static Store getInstance() {
            return instance;
        }

        public SlingClient getOrCompute(final ExtensionContext extensionContext, final String runMode, final boolean forceAnonymous) {
            return getOrCompute(extensionContext, runMode, null, null, forceAnonymous);
        }

        public SlingClient getOrCompute(final ExtensionContext extensionContext, final String runMode, final String username, final String password, final boolean forceAnonymous) {
            InstanceConfiguration conf = getInstanceConfiguration(runMode);
            HashMap<String, SlingClient> clientMapFromStore = getClientMapFromStore(extensionContext);

            String user = username == null ? conf.getAdminUser():username;
            String pass = password == null ? conf.getAdminPassword():password;

            String key = getKeyPrefix() + runMode + user + forceAnonymous;
            SlingClient slingClient = clientMapFromStore.get(key);
            if (slingClient == null) {
                try {
                    slingClient = buildClient(conf.getUrl(), user, pass, forceAnonymous);
                    clientMapFromStore.put(key, slingClient);
                } catch (ClientException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return slingClient;

        }

        protected void saveLatest(final ExtensionContext extensionContext, final SlingClient latestClient) {
            String key = getKeyPrefix() + "latest";
            getClientMapFromStore(extensionContext).put(key, latestClient);
        }

        public SlingClient recallLatest(final ExtensionContext extensionContext) {
            String key = getKeyPrefix() + "latest";
            return getClientMapFromStore(extensionContext).get(key);
        }

        protected void clear(final ExtensionContext extensionContext) {
            final String keyPrefix = getKeyPrefix();
            getClientMapFromStore(extensionContext).forEach((s, slingClient) -> {
                try {
                    if (s.startsWith(keyPrefix)) {
                        slingClient.close();
                    }
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage());
                }
            });
            getClientMapFromStore(extensionContext).clear();
        }
    }
}
