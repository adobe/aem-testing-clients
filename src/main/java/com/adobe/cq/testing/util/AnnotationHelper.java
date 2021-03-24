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

package com.adobe.cq.testing.util;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Optional;

public final class AnnotationHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationHelper.class);

    private AnnotationHelper() {
    }

    /**
     * Try to find if the annotation in the class or any parent it is {@link java.lang.annotation.Inherited}.
     * @param context {@link ExtensionContext} starting context to find annotation from the test class.
     * @param annotationType the annotation class to lookup for.
     * @param <A> the returned type should be the same as the annotation class.
     * @return an optional object filled with the annotation if found.
     */
    public static <A extends Annotation> Optional<A> findOptionalAnnotation(final ExtensionContext context, final Class<A> annotationType) {
        Optional<ExtensionContext> current = Optional.of(context);
        try {
            while (current.isPresent()) {
                Optional<A> annotation = AnnotationUtils.findAnnotation(current.get().getRequiredTestClass(), annotationType);
                if (annotation.isPresent()) {
                    return annotation;
                }
                current = current.get().getParent();
            }
        } catch (Exception ex) {
            LOGGER.trace(ex.getMessage());
        }
        return Optional.empty();
    }

}
