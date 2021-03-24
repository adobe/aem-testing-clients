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

package com.adobe.cq.testing.junit.annotations;

import javax.annotation.meta.TypeQualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@TypeQualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface WithClient {
    /**
     * @return the defined value if client should be using anonymous user, false as default.
     */
    boolean forceAnonymous() default false;

    /**
     * Define which run mode to use for this client (i.e author, publish etc..).
     * @return the run mode to be used
     */
    String runMode();
}
