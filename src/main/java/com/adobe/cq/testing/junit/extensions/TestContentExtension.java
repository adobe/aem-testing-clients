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
import com.adobe.cq.testing.util.TestContentBuilder;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public final class TestContentExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private static final String KEY_PREFIX = "_tcb_";

    private static final ExtensionContext.Namespace THIS_STORE = ExtensionContext.Namespace.GLOBAL;

    private final String runmode;

    public TestContentExtension(final String runMode) {
        this.runmode = runMode;
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        TestContentBuilder testContentBuilder = context.getStore(THIS_STORE).remove(getKey(), TestContentBuilder.class);
        testContentBuilder.dispose();
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        TestContentBuilder testContentBuilder = new TestContentBuilder(
                SlingClientExtension.Store.getInstance().getOrCompute(context, runmode, false).adaptTo(CQClient.class),
                context.getTestMethod().orElseThrow(Exception::new).getName()
        );
        context.getStore(THIS_STORE).put(getKey(), testContentBuilder);
    }

    private String getKey() {
        return KEY_PREFIX + Thread.currentThread().getId();
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext context) {
        return TestContentBuilder.class.equals(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.GLOBAL).get(getKey(), TestContentBuilder.class);
    }

}
