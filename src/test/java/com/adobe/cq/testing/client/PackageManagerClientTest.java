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
package com.adobe.cq.testing.client;

import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class PackageManagerClientTest {

    static {
        // date formats below are expected to be formatted in english locale
        Locale.setDefault(Locale.ENGLISH);
    }

    @Test
    public void testBuildPackageFromJson() throws Exception {
        final String PACKAGE_JSON =
                "{\n"+
                        "  \"jcr:primaryType\": \"vlt:PackageDefinition\",\n"+
                        "  \"lastUnwrappedBy\": \"adminUnwrappedBy\",\n"+
                        "  \"jcr:createdBy\": \"admin\",\n"+
                        "  \"lastUnpacked\": \"Mon Sep 11 2017 12:00:20 GMT+0000\",\n"+
                        "  \"lastWrappedBy\": \"adminWrappedBy\",\n"+
                        "  \"lastUnpackedBy\": \"adminUnpackedBy\",\n"+
                        "  \"requiresRestart\": \"false\",\n"+
                        "  \"requiresRoot\": \"true\",\n"+
                        "  \"jcr:lastModifiedBy\": \"admin\",\n"+
                        "  \"lastWrapped\": \"Tue Sep 12 2017 15:24:51 GMT+0000\",\n"+
                        "  \"buildCount\": \"3\",\n"+
                        "  \"acHandling\": \"merge_preserve\",\n"+
                        "  \"jcr:created\": \"Tue Sep 12 2017 15:24:51 GMT+0000\",\n"+
                        "  \"name\": \"we.retail.community.apps\",\n"+
                        "  \"group\": \"adobe/aem6/sample\",\n"+
                        "  \"version\": \"1.11.66\",\n"+
                        "  \"jcr:description\": \"Profiles used for exporting We.Retail Community Site Apps.\",\n"+
                        "  \"dependencies\": [\n"+
                        "    \"adobe/aem6/sample:we.retail.ui.content:1.9.0\",\n"+
                        "    \"day/cq610/social/enablement:cq-social-enablement-pkg:1.1.0\",\n"+
                        "    \"adobe/aem6/sample:we.retail.community.content:1.11.0\"\n"+
                        "  ],\n"+
                        "  \"jcr:lastModified\": \"Tue Sep 12 2017 15:24:51 GMT+0000\",\n"+
                        "  \"lastUnwrapped\": \"Tue Sep 12 2017 15:24:51 GMT+0000\"\n"+
                        "  }";

        PackageManagerClient.Package p = PackageManagerClient.Package.build(PACKAGE_JSON);

        DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals("adminUnwrappedBy", p.getLastUnwrappedBy());
        assertEquals("11 Sep 2017 12:00:20 GMT", df.format(p.getLastUnpacked()));
        assertEquals("adminWrappedBy", p.getLastWrappedBy());
        assertEquals("adminUnpackedBy", p.getLastUnpackedBy());
        assertEquals(false, p.getRequiresRestart());
        assertEquals(true, p.getRequiresRoot());
        assertEquals("12 Sep 2017 15:24:51 GMT", df.format(p.getLastWrapped()));
        assertEquals(3, p.getBuildCount().intValue());
        assertEquals("we.retail.community.apps", p.getName());
        assertEquals("adobe/aem6/sample", p.getGroup());
        assertEquals("1.11.66", p.getVersion());
        assertEquals("Profiles used for exporting We.Retail Community Site Apps.", p.getDescription());
        assertEquals("12 Sep 2017 15:24:51 GMT", df.format(p.getLastUnwrapped()));
    }


    @Test
    public void testBuildNullPackageFromJson() throws Exception {
        final String SMALL_PACKAGE_JSON =
                "{\n" +
                        "  \"jcr:primaryType\": \"vlt:PackageDefinition\",\n" +
                        "  \"jcr:createdBy\": \"AEM Synchronization Tool\",\n" +
                        "  \"lastUnpacked\": \"Tue Sep 12 2017 11:16:08 GMT+0000\",\n" +
                        "  \"lastUnpackedBy\": \"adminUnpackedBy\",\n" +
                        "  \"requiresRestart\": \"false\",\n" +
                        "  \"name\": \"we.retail.community.apps\",\n"+
                        "  \"group\": \"aemsync\",\n" +
                        "  \"version\": \"1.11.66\",\n"+
                        "  \"lastUnwrapped\": \"Tue Sep 12 2017 11:15:54 GMT+0000\"\n" +
                        "  }";

        PackageManagerClient.Package p = PackageManagerClient.Package.build(SMALL_PACKAGE_JSON);

        DateFormat df = new SimpleDateFormat("dd MMM yyyy kk:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        assertEquals(null, p.getLastUnwrappedBy());
        assertEquals("12 Sep 2017 11:16:08 GMT", df.format(p.getLastUnpacked()));
        assertEquals(null, p.getLastWrappedBy());
        assertEquals("adminUnpackedBy", p.getLastUnpackedBy());
        assertEquals(false, p.getRequiresRestart());
        assertEquals(null, p.getRequiresRoot());
        assertEquals(null, p.getLastWrapped());
        assertEquals(null, p.getBuildCount());
        assertEquals("we.retail.community.apps", p.getName());
        assertEquals("aemsync", p.getGroup());
        assertEquals("1.11.66", p.getVersion());
        assertEquals(null, p.getDescription());
        assertEquals("12 Sep 2017 11:15:54 GMT", df.format(p.getLastUnwrapped()));
    }
}
