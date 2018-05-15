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

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClientConfig;

import java.net.URI;

/**
 * This client is used to add new Foundation Components (see /libs/foundation/components in repository) to a
 * page. The component functions are wrapped in subclasses of
 * {@link com.adobe.cq.testing.client.components.foundation.AbstractFoundationComponent}.
 */
public class FoundationClient extends ComponentClient {
    public FoundationClient(CloseableHttpClient http, SlingClientConfig config) throws ClientException {
        super(http, config);
    }

    public FoundationClient(URI serverUrl, String user, String password) throws ClientException {
        super(serverUrl, user, password);
    }
}
