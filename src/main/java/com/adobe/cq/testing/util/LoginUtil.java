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

import com.adobe.cq.testing.client.CQClient;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.sling.testing.clients.AbstractSlingClient;
import org.apache.sling.testing.clients.util.FormEntityBuilder;

import java.io.IOException;

public class LoginUtil {

    // The form authentication request URL
    public final static String FORM_AUTH_URL = "/libs/crx/core/content/login.html/j_security_check";

    /**
     * Retrieves a page using the given login token
     *
     * @param loginToken login token
     * @param cqClient client to use
     * @param testPage path to the page
     * @return the http response
     * @throws IOException if the request fails
     */
    public static HttpResponse doGetWithLoginToken(String loginToken, CQClient cqClient, String testPage) throws IOException {
        // We uses plain Http Client to avoid integration testing framework side effects.
        HttpClient client = HttpClientBuilder.create().build();

        HttpGet get = new HttpGet(cqClient.getUrl(testPage));

        // set the cookie token
        get.addHeader("Cookie", loginToken);

        // set referer
        get.addHeader("Referer", "about:blank");

        // execute the request
        return client.execute(get);
    }

    /**
     * Retrieves a login token
     *
     * @param graniteClient client to use
     * @param targetPage path to the page
     * @param <T> client type
     * @return the login token
     * @throws IOException if the request fails
     */
    public static <T extends AbstractSlingClient> String getLoginToken(T graniteClient, String targetPage)
            throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = buildFormAuthPost(
                graniteClient,
                graniteClient.getUser(), graniteClient.getPassword(),
                targetPage);
        HttpResponse response = client.execute(post);

        // consume response so that the connection can be reused
        EntityUtils.consume(response.getEntity());

        // get Set-Cookie header
        Header[] setCookie = response.getHeaders("Set-Cookie");

        // get the login token from the Set-Cookie header
        String loginToken = setCookie[0].getValue();
        return loginToken.split(";")[0];
    }

    /**
     * Creates the post request with the form for authentication
     *
     * @param graniteClient client to use
     * @param user username
     * @param pass password
     * @param page path to the page
     * @param <T> client type
     * @return the http post
     */
    public static <T extends AbstractSlingClient> HttpPost buildFormAuthPost(T graniteClient, String user,
                                             String pass, String page) {
        // create post request
        HttpPost post = new HttpPost(graniteClient.getUrl(FORM_AUTH_URL));

        // construct the form body of the request
        FormEntityBuilder formPartEntity = FormEntityBuilder.create();
        formPartEntity.addParameter("j_username", user);
        formPartEntity.addParameter("j_password", pass);
        formPartEntity.addParameter("resource", graniteClient.getUrl(page).getPath());
        formPartEntity.addParameter("_charset_", "UTF-8");
        formPartEntity.addParameter("contextPath", "");

        // add the form data to the post
        post.setEntity(formPartEntity.build());

        // set header to state that is a form submit
        post.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        // set Accept header to accept HTML
        post.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        // set Accept header to accept HTML
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:8.0) Gecko/20100101 Firefox/8.0");
        // set Referer
        post.setHeader("Referer", "about:blank");
        // return the post object
        return post;
    }
}
