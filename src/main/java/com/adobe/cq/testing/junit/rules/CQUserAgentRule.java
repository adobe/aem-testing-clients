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

import org.apache.sling.testing.clients.interceptors.UserAgentHolder;
import org.apache.sling.testing.clients.util.UserAgentUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class CQUserAgentRule implements TestRule {

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                starting(description);
                try {
                    base.evaluate();
                } finally {
                    finished();
                }
            }
        };
    }

    protected void starting(final Description description) {
        UserAgentHolder.set(
                UserAgentUtil.constructAgent("aem-testing-client", getClass().getPackage()) +
                " ("+description.getTestClass().getSimpleName()+")"
        );
    }

    protected void finished() {
        UserAgentHolder.reset();
    }
}
