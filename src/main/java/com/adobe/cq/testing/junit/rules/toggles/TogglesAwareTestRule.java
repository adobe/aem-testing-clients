/*
 * Copyright 2021 Adobe
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
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Junit rule for filtering tests based on the toggles enabled on the remote instance.<br>
 * <br>
 * The rule is used in combination with the annotations {@link RunIfToggleEnabled} and
 * {@link SkipIfToggleEnabled}.
 * If both annotations are applied to a test, both conditions must be met (AND operation).
 *
 * @see RunIfToggleEnabled
 * @see SkipIfToggleEnabled
 */
public class TogglesAwareTestRule implements TestRule {
    private static final Logger LOG = LoggerFactory.getLogger(TogglesAwareTestRule.class);

    /**
     * Supplier of the client needed for retrieving the enabled toggles.
     */
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
        String runIfToggle = getRunIfToggle(description);
        String skipIfToggle = getSkipIfToggle(description);

        if (runIfToggle == null && skipIfToggle == null) {
            return true; // no annotation was applied to the test
        }

        AtomicReference<List<String>> enabledToggles = new AtomicReference<>();
        try {
            new Polling(() -> {
                enabledToggles.set(clientSupplier.get().adaptTo(TogglesClient.class).getEnabledToggles());
                return true;
            }).poll(SECONDS.toMillis(30), SECONDS.toMillis(1));
        } catch (TimeoutException e) {
            LOG.warn("Failed to retrieve toggles", e);
            return true; // if something goes wrong we assume test should run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true; // thread was interrupted, we assume test should run
        }

        if (runIfToggle != null && !enabledToggles.get().contains(runIfToggle)) {
            return false; // RunIfToggleEnabled condition was not met, skipping
        }

        return skipIfToggle == null || !enabledToggles.get().contains(skipIfToggle);
    }

    private String getRunIfToggle(Description description) {
        RunIfToggleEnabled annotation = description.getAnnotation(RunIfToggleEnabled.class);
        if (annotation == null) {
            return null;
        }

        return annotation.value();
    }

    private String getSkipIfToggle(Description description) {
        SkipIfToggleEnabled annotation = description.getAnnotation(SkipIfToggleEnabled.class);
        if (annotation == null) {
            return null;
        }

        return annotation.value();
    }

    private Statement emptyStatement(final String testName) {
        return new Statement() {
            @Override
            public void evaluate() {
                throw new AssumptionViolatedException("Test " + testName + " was ignored by TogglesAwareTestRule");
            }
        };
    }
}
