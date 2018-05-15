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

import com.adobe.cq.testing.junit.assertion.GraniteAssert;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.XSSUtils;
import org.apache.taglibs.standard.functions.Functions;
import org.junit.Assert;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Basic class for XSS Tests
 */
public class CQXSSUtils extends XSSUtils {

    public static final String XSS_ATTACK_SIMPLE = "';!--\"<XSS>=&{()}";
    public static final String XSS_ATTACK_JS_SIMPLE = "'\"></title></script><script>alert('XSS');</script>";
    public static final String XSS_ATTACK_JS_SIMPLE2 = "\"});});alert(23);</script>";
    public static final String XSS_ATTACK_JS_CASE_INSENSITIVE = "<IMG SRC=\"JaVaScRiPt:alert('XSS');\">";
    public static final String XSS_ATTACK_HERF_SIMPLE = "XSSHERF%22%20onclick=alert(23)%3E";

    /**
     * Use to encapsulate escaping of XML with standard JSTL.
     * This is the old method of escaping in CQ5 and is beeing replaced
     * by ESAPI. See {@link XSSUtils#escapeXml(String)}
     *
     * @param xmlString string to escape
     * @return the escaped string
     */
    public static String escapeXmlJSTL(String xmlString) {
        return Functions.escapeXml(xmlString);
    }

    /**
     * Replaces special chars to avoid breaking the regexp
     *
     * @param regexp regular expression
     * @return the sanitized regular expression
     */
    public static String replaceSpecialCharsForRegexp(String regexp) {
        return TestUtil.replaceSpecialCharsForRegexp(regexp);
    }

    /**
     * Assert title output is sanitized in head
     *
     * @param response Sling response containing the page
     * @param expectedTitle expected title
     * @throws IOException never
     */
    public static void assertTitleTagIsNotVulnerable(SlingHttpResponse response, String expectedTitle) throws
            IOException {
        // check if title tag exists
        Assert.assertTrue(response.getContent().contains("<title>"));

        String content = response.getContent();

        // check if title is properly sanitized
        GraniteAssert.assertRegExFind("Title tag is not properly sanitized: " + content,
                content,
                Pattern.compile("<title>[^<]*" + CQXSSUtils.replaceSpecialCharsForRegexp(expectedTitle) +
                        "[^<]*</title>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE));
    }
}
