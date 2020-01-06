/*
 * Copyright 2019 Adobe Systems Incorporated
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
package com.adobe.cq.testing.junit5.extensions;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.junit5.annotations.SlingClientContext;
import com.adobe.cq.testing.junit5.annotations.WithClient;
import com.adobe.cq.testing.util.AnnotationHelper;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.instance.InstanceConfiguration;
import org.apache.sling.testing.clients.instance.InstanceSetup;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SlingClientExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback, ParameterResolver {

    private Store clientStore = Store.getInstance();

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        after(extensionContext);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        after(extensionContext);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        before(extensionContext);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        before(extensionContext);
    }

    private void before(ExtensionContext extensionContext) throws ClientException {

    }

    private void after(ExtensionContext extensionContext) throws IOException {
        clientStore.clear(extensionContext);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return isOfType(parameterContext, SlingClient.class, CQClient.class);
    }

    private boolean isOfType(@Nonnull ParameterContext context, @Nonnull Class ...clazz) {
        boolean match = Arrays.stream(clazz).anyMatch(p -> {
            return p.isAssignableFrom(context.getParameter().getType());
        });
        return match;
    }

    private String getDefaultRunMode(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, SlingClientContext.class).map(cqClientContext -> cqClientContext.defaultRunMode()).orElseThrow(() -> new ExtensionConfigurationException("@CQClientContext not found"));
    }

    private boolean getDefaultForceAnonymous(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, SlingClientContext.class).map(cqClientContext -> cqClientContext.defaultAnonymous()).orElseThrow(() -> new ExtensionConfigurationException("@CQClientContext not found"));
    }

    private String getRunMode(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, WithClient.class).map(cqClientContext -> cqClientContext.runMode()).orElseThrow(() -> new ExtensionConfigurationException("@WithClient not found"));
    }

    private boolean getForceAnonymous(ExtensionContext extensionContext) {
        return AnnotationHelper.findOptionalAnnotation(extensionContext, WithClient.class).map(cqClientContext -> cqClientContext.forceAnonymous()).orElseThrow(() -> new ExtensionConfigurationException("@WithClient not found"));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {

        String runMode = getDefaultRunMode(extensionContext);
        boolean forceAnonymous = getDefaultForceAnonymous(extensionContext);

        if (parameterContext.isAnnotated(WithClient.class)) {
            runMode = getRunMode(extensionContext);
            forceAnonymous = getForceAnonymous(extensionContext);
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

        private final Logger LOGGER = LoggerFactory.getLogger(Store.class);

        private static Store instance = new Store();

        private static final Map<String, InstanceConfiguration> DEFAULT_CONFIGURATION_MAP = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>(Constants.RUNMODE_AUTHOR, new InstanceConfiguration(URI.create(Constants.DEFAULT_AUTHOR_URL), Constants.RUNMODE_AUTHOR, Constants.DEFAULT_USER, Constants.DEFAULT_PWD)),
                new AbstractMap.SimpleImmutableEntry<>(Constants.RUNMODE_PUBLISH, new InstanceConfiguration(URI.create(Constants.DEFAULT_PUBLISH_URL), Constants.RUNMODE_PUBLISH, Constants.DEFAULT_USER, Constants.DEFAULT_PWD)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        private InstanceSetup instanceSetup = InstanceSetup.get();

        private Store() {}

        private HashMap<String, SlingClient> getClientMapFromStore(ExtensionContext extensionContext) {
            HashMap<String, SlingClient> stringSlingClientHashMap = (HashMap<String, SlingClient>) extensionContext.getStore(Constants.NAMESPACE).get(Constants.SLING_CLIENTS_MAP_KEY);
            if (stringSlingClientHashMap == null) {
                stringSlingClientHashMap = new HashMap<>();
                extensionContext.getStore(Constants.NAMESPACE).put(Constants.SLING_CLIENTS_MAP_KEY, stringSlingClientHashMap);
            }
            return stringSlingClientHashMap;
        }

        private SlingClient buildClient(String runMode, boolean forceAnonymous) throws ClientException {
            List<InstanceConfiguration> confList = instanceSetup.getConfigurations(runMode);
            InstanceConfiguration conf = DEFAULT_CONFIGURATION_MAP.get(runMode);
            if (confList.size() > 0) {
                conf = confList.get(0);
            }
            return SlingClient.Builder.create(conf.getUrl(), forceAnonymous ? null : conf.getAdminUser(), conf.getAdminPassword()).build();
        }

        private String getKeyPrefix() {
            return "_" + Thread.currentThread().getId() + "_";
        }

        public static Store getInstance() {
            return instance;
        }

        public SlingClient getOrCompute( ExtensionContext extensionContext, String runMode, boolean forceAnonymous) {
            HashMap<String, SlingClient> clientMapFromStore = getClientMapFromStore(extensionContext);
            String key = getKeyPrefix() + runMode + forceAnonymous;
            SlingClient slingClient = clientMapFromStore.get(key);
            if (slingClient == null) {
                try {
                    slingClient = buildClient(runMode, forceAnonymous);
                    clientMapFromStore.put(key, slingClient);
                } catch (ClientException e) {
                    LOGGER.error(e::getMessage);
                }
            }
            return slingClient;

        }

        protected void saveLatest(ExtensionContext extensionContext, SlingClient latestClient) {
            String key = getKeyPrefix() + "latest";
            getClientMapFromStore(extensionContext).put(key, latestClient);
        }

        public SlingClient recallLatest(ExtensionContext extensionContext) {
            String key = getKeyPrefix() + "latest";
            return getClientMapFromStore(extensionContext).get(key);
        }

        protected void clear(ExtensionContext extensionContext) {
            final String keyPrefix = getKeyPrefix();
            getClientMapFromStore(extensionContext).forEach((s, slingClient) -> {
                try {
                    if (s.startsWith(keyPrefix)) {
                        slingClient.close();
                    }
                } catch (IOException e) {
                    LOGGER.warn(e::getMessage);
                }
            });
            getClientMapFromStore(extensionContext).clear();
        }
    }
}
