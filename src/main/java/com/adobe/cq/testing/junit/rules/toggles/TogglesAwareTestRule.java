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
package com.adobe.cq.testing.junit.rules.toggles;

import com.adobe.cq.testing.client.TogglesClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.function.Supplier;

/**
 * Junit rule for enable filtering of tests based on the toggles enabled on the remote instance.
 *
 * The rule should be used in combination with the {@link RunIfToggleEnabled} annotation.
 *
 * @see RunIfToggleEnabled
 */
public class TogglesAwareTestRule implements TestRule {

    private final Supplier<SlingClient> clientSupplier;

    public TogglesAwareTestRule(Supplier<SlingClient> clientSupplier) {
        this.clientSupplier = clientSupplier;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (shouldRunTest(description)) {
            return base;
        } else {
            return emptyStatement(description.getDisplayName());
        }
    }

    protected boolean shouldRunTest(Description description) {
        String requiredToggle = getAnnotationToggle(description);
        if (requiredToggle == null) {
            return true;
        }

        try {
            return clientSupplier.get().adaptTo(TogglesClient.class).isToggleEnabled(requiredToggle);
        } catch (ClientException e) {
            return true; // if something goes wrong, we assume test should run
        }
    }

    private String getAnnotationToggle(Description description) {
        RunIfToggleEnabled annotation = description.getAnnotation(RunIfToggleEnabled.class);
        if (annotation == null) {
            return null;
        }

        return annotation.value();
    }

    private Statement emptyStatement(final String testName) {
        return new Statement() {
            @Override
            public void evaluate() {
                throw new AssumptionViolatedException("Test " + testName + " was ignored by TogglesAwareTestRule" +
                        " because required toggle is not enabled on the target instance");
            }
        };
    }
}
