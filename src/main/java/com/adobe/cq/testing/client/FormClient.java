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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;
import org.apache.sling.testing.clients.SlingHttpResponse;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;

/**
 * The base client for all form-related actions. It provides a core set of commonly used foundation/form
 * functions such as post. <br>
 * <br>
 * It extends from {@link CQClient} which in turn provides a core set of commonly used website and page
 * functionality.
 */
public class FormClient extends CQClient {
    public FormClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public FormClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }

    /**
     * Mimics a submit of a foundation form.
     *
     * @param path              path to the {@code cq:Page} containing the form
     * @param sessionCookie     an optional session cookie to add to the request
     * @param formId            the id of the form on the page
     * @param formStart         the path to the form-start component on the page
     * @param redirect          the redirect to follow after a successful submission
     * @param parameters        the form parameters to post
     * @param expectedValidationErrors  an optional list of expected validation messages
     * @return                  a {@code SlingHttpResponse} with a 302 redirect assuming a successful submission
     * @throws ClientException  if the request fails
     */
    public SlingHttpResponse doFormPost(String path, String sessionCookie,
                                        String formId, String formStart, String redirect,
                                        Map<String, String> parameters,
                                        String... expectedValidationErrors) throws ClientException {
        redirect = getUrl(redirect).getPath();

        HttpEntity entity = FormEntityBuilder.create()
                .addParameter(":formid", formId)
                .addParameter(":formstart", formStart)
                .addParameter(":redirect", redirect)
                .addParameter("_charset_", "UTF-8")
                .addAllParameters(parameters)
                .build();

        List<Header> headers = Arrays.<Header>asList(
                new BasicHeader("Cookie", sessionCookie),
                new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
        );

        SlingHttpResponse exec = doPost(path, entity, headers, SC_OK, SC_MOVED_TEMPORARILY);
        String response = exec.getContent();

        // Fetch any validation error messages out of the response
        String parts[] = response.split("<[^>]*class=\"[^\"]*form_error[^\"]*\"[^>]*>");
        List<String> validationErrors = new ArrayList<>();
        for (int i = 1 /* start at 1 to skip prelude before first form_error */; i < parts.length; i++) {
            validationErrors.add(parts[i].substring(0, parts[i].indexOf("</")));
        }

        // If we're expecting any specific errors, then we're also expecting the global summary message
        final String globalMessage = "Please correct the errors and send your information again.";
        List<String> expectedErrors = new ArrayList<>(Arrays.asList(expectedValidationErrors));
        if (expectedErrors.size() > 0 && !expectedErrors.contains(globalMessage)) {
            expectedErrors.add(globalMessage);
        }

        // See if there are any differences in the expected and the actual messages
        Collection unexpectedErrors = subtract(validationErrors, expectedErrors);
        Collection missingErrors = subtract(expectedErrors, validationErrors);
        if (!unexpectedErrors.isEmpty()) {
            throw new ClientException("Unexpected form validation errors!\r" + StringUtils.join(unexpectedErrors, "\r"));
        }
        if (!missingErrors.isEmpty()) {
            throw new ClientException("Expected form validation error missing!\r" + StringUtils.join(missingErrors, "\r"));
        }

        return exec;
    }

    private static Collection<String> subtract(final Collection<String> a, final Collection<String> b) {
        ArrayList<String> list = new ArrayList<>(a);
        for (String aB : b) {
            list.remove(aB);
        }
        return list;
    }
}
