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

import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class CQAuthorPublishClassRule implements TestRule {

    /** Granite rules to be executed at class level */
    public final CQClassRule cqClassRule;

    /** ExistingInstance to reserve an Author */
    public final Instance authorRule;

    /** ExistingInstance to reserve a Publish */
    public final Instance publishRule;

    protected TestRule ruleChain;

    public CQAuthorPublishClassRule() {
        this(false);
    }

    /**
     * @param forceBasicAuth Force basic authentication for author and publish instances.
     */
    public CQAuthorPublishClassRule(boolean forceBasicAuth) {
        this(forceBasicAuth, forceBasicAuth);
    }

    /**
     * @param forceBasicAuthAuthor Force basic authentication for author instance.
     * @param forceBasicAuthPublish Force basic authentication for publish instance.
     */
    public CQAuthorPublishClassRule(boolean forceBasicAuthAuthor, boolean forceBasicAuthPublish) {
        super();
        cqClassRule = new CQClassRule();
        authorRule = ClassRuleUtils.newInstanceRule(forceBasicAuthAuthor).withRunMode("author");
        publishRule = ClassRuleUtils.newInstanceRule(forceBasicAuthPublish).withRunMode("publish");

        ruleChain = RuleChain.outerRule(cqClassRule)
                .around(authorRule)
                .around(publishRule);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return ruleChain.apply(base, description);
    }
}
