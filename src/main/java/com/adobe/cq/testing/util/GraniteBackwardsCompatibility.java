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
package com.adobe.cq.testing.util;
import org.apache.sling.testing.clients.SystemPropertiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraniteBackwardsCompatibility {
    private static final Logger LOG = LoggerFactory.getLogger(GraniteBackwardsCompatibility.class);

    public static void translateGranitePropertiesToSling() {
        for (String property : System.getProperties().stringPropertyNames()) {
            if (property.startsWith("granite.it.")) {
                String slingProp = property.replaceFirst("granite\\.it\\.", SystemPropertiesConfig.CONFIG_PROP_PREFIX);
                if (System.getProperty(slingProp) == null) {
                    System.setProperty(slingProp, System.getProperty(property));
                    LOG.info("Set {}={} from {}", slingProp, System.getProperty(slingProp), property);
                }
            }
        }

        // it.logintokenauth was initially named granite.it.logintokenauth
        String loginTokenAuth = System.getProperty("granite.it.logintokenauth");
        if (loginTokenAuth != null && System.getProperty("it.logintokenauth") == null) {
            System.setProperty("it.logintokenauth", loginTokenAuth);
        }

        // Special handling of granite.it.author.url and granite.it.publish.url
        String authorUrl = System.getProperty("granite.it.author.url");
        if (authorUrl != null && !isInstanceAlreadyConfigured(authorUrl, "author")) {
            final int instances = Integer.parseInt(System.getProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instances", "0"));
            System.setProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instance.url." + (instances + 1), authorUrl);
            System.setProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instance.runmode." + (instances + 1), "author");
            System.setProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instances", String.valueOf(instances + 1));
        }

        String publishUrl = System.getProperty("granite.it.publish.url");
        if (publishUrl != null && !isInstanceAlreadyConfigured(publishUrl, "publish")) {
            final int instances = Integer.parseInt(System.getProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instances", "0"));
            System.setProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instance.url." + (instances + 1), publishUrl);
            System.setProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instance.runmode." + (instances + 1), "publish");
            System.setProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instances", String.valueOf(instances + 1));
        }
    }

    private static boolean isInstanceAlreadyConfigured(String url, String runmode) {
        final int instances = Integer.parseInt(System.getProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instances", "0"));
        for (int i = 1; i <= instances; i++) {
            String instanceUrl = System.getProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instance.url." + i);
            String instanceRunmode = System.getProperty(SystemPropertiesConfig.CONFIG_PROP_PREFIX + "instance.runmode." + i);

            if (runmode.equals(instanceRunmode) && url.equals(instanceUrl)) {
                return true;
            }
        }

        return false;
    }
}
