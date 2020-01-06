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
package com.adobe.cq.testing.junit.assertion;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.junit.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpAssert {

    public static void assertContentTypeEquals(HttpResponse response) {
        Header contentType = response.getFirstHeader("Content-Type");

        // since wie accept html as reponse the <code>Content-Type</code> header should be set to <code>text/html</code>.
        Assert.assertEquals("Response Header 'Content-Type' is not properly set!", "text/html;charset=utf-8",
                contentType.getValue().replace(" ", "").toLowerCase());
    }

    public static URL parseURL(String pathOrURL) {
        try {
            return new URL(pathOrURL);
        } catch (MalformedURLException e) {
            try {
                return new URL(new URL("http://localhost"), pathOrURL);
            } catch (MalformedURLException e1) {
                Assert.fail("URL is malformed: " + pathOrURL);
                return null;
            }
        }
    }

    public static String extractPath(String pathOrURL) {
        URL parsedUrl = parseURL(pathOrURL);
        if (parsedUrl != null) {
            return parsedUrl.getPath();
        }

        return null;
    }

    public static void assertURLPathEquals(String expected, String loc) throws MalformedURLException {
        Assert.assertEquals("Redirect target of Form Authentication response is wrong!",
                extractPath(expected), extractPath(loc));
    }

    public static Map<String, String> parseParameters(String query) {
        Map<String, String> parameters = new HashMap<>();
        if (query == null) {
            return parameters;
        }

        for (String paramAndValue : query.split("&")) {
            String[] pair = paramAndValue.split("=");

            if (pair.length == 2) {
                parameters.put(pair[0], pair[1]);
            }
        }

        return parameters;
    }

    public static void assertParameter(String pathOrURL, String parameter, String value) {
        URL url = parseURL(pathOrURL);

        Map<String, String> params = parseParameters(url != null ? url.getQuery() : null);

        Assert.assertTrue(params.get(parameter).matches(value));
    }
}
