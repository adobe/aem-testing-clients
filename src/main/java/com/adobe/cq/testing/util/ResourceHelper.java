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

import org.apache.commons.io.IOUtils;
import org.apache.sling.testing.clients.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * Fix a bug in the {@link ResourceUtil#readResourceAsString(String)}
 * which append extra line break at the end of the file.
 */
public class ResourceHelper extends ResourceUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceHelper.class);

    public static String readResourceAsString(final String resource) {
        String output = null;
        try {
            output = IOUtils.toString(ResourceUtil.getResourceAsStream(resource), UTF_8.name());
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return output;
    }

}
