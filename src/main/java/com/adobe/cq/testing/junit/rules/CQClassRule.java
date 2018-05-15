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

import com.adobe.cq.testing.util.GraniteBackwardsCompatibility;
import org.apache.sling.testing.clients.instance.InstanceConfiguration;
import org.apache.sling.testing.junit.rules.SlingClassRule;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.net.URI;

/**
 * Junit rule to be used in every test class (to be applied at class level).
 * It chains the {@link SlingClassRule}.
 */
public class CQClassRule implements TestRule {
    public static final InstanceConfiguration DEFAULT_AUTHOR_CONFIG =
            new InstanceConfiguration(URI.create("http://localhost:4502"), "author");
    public static final InstanceConfiguration DEFAULT_PUBLISH_CONFIG =
            new InstanceConfiguration(URI.create("http://localhost:4503"), "publish");

    public final SlingClassRule slingBaseClassRule = new SlingClassRule();

    protected TestRule ruleChain = RuleChain.outerRule(slingBaseClassRule);

    static {
        GraniteBackwardsCompatibility.translateGranitePropertiesToSling();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return ruleChain.apply(base, description);
    }
}
