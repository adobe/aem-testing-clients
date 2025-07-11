/*
 * Copyright 2025 Adobe Systems Incorporated
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URI;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class TogglesClientTest {

  @Rule public WireMockRule togglesService = new WireMockRule();

  @Before
  public void startServer() {
    togglesService.stubFor(
        get(urlEqualTo("/etc.clientlibs/toggles.json"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"enabled\":[\"a\", \"b\", \"c\"]}")));
  }

  @Test
  public void testGetEnabledToggles() throws Exception {
    TogglesClient client =
        new TogglesClient(
            URI.create(String.format("http://localhost:%d", togglesService.port())), "", "");
    List<String> toggles = client.getEnabledToggles();
    assertArrayEquals(toggles.toArray(), new String[] {"a", "b", "c"});
  }

  @Test
  public void testIsToggleEnabled() throws Exception {
    TogglesClient client =
        new TogglesClient(
            URI.create(String.format("http://localhost:%d", togglesService.port())), "", "");
    assertTrue(client.isToggleEnabled("a"));
    assertFalse(client.isToggleEnabled("e"));
  }
}
