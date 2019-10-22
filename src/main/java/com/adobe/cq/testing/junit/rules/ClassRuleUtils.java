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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassRuleUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ClassRuleUtils.class);

    /**
     * Create a new {@code Instance} object depending on the default auth mechanism
     * @param forceBasicAuth set to true to always use basic auth
     * @return the instance object
     */
    public static Instance newInstanceRule(boolean forceBasicAuth) {
        return new ConfigurableInstance(forceBasicAuth);
    }

    /**
     * Create a new {@code Instance} object depending on the default auth mechanism
     *
     * @return the instance object
     */
    public static Instance newInstanceRule() {
        return newInstanceRule(false);
    }
}
