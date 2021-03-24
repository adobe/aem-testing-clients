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

import com.adobe.cq.testing.Constants;
import com.adobe.cq.testing.junit.annotations.WithClient;

import java.lang.annotation.Annotation;

public final class DefaultWithClient implements WithClient {
    @Override
    public boolean forceAnonymous() {
        return false;
    }

    @Override
    public String runMode() {
        return Constants.RUNMODE_AUTHOR;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return WithClient.class;
    }
}
