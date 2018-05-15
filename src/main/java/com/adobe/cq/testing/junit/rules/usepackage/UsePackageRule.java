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
package com.adobe.cq.testing.junit.rules.usepackage;

import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Install a package on the provided instance every time this rule is applied.
 */
public class UsePackageRule implements TestRule {

    private String srcPath;

    private Instance instance;

    protected TestRule ruleChain;

    public UsePackageRule(String srcPath, Instance instance) {
        this.srcPath = srcPath;
        this.instance = instance;
        this.ruleChain = RuleChain.outerRule(new InternalRule(this));
    } 
    
    
    @Override
    public Statement apply(Statement base, Description description) {
        return ruleChain.apply(base, description);
    }
    
    public String getSrcPath() {
        return srcPath;
    }

    public Instance getInstance() {
        return instance;
    }
    
    private class InternalRule implements TestRule {
        
        private UsePackageRule parentRule;
        
        protected InternalRule(UsePackageRule rule) {
            this.parentRule = rule;
        }

        @Override
        public Statement apply(Statement base, Description description) {
            return new UsingPackageStatement(parentRule, base);
        }
        
    }
}
