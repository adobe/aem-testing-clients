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
package com.adobe.cq.testing.junit.rules.toggles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.fail;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.net.URI;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.junit.*;

public class TogglesAwareTestRuleTest {

  @ClassRule public static WireMockRule togglesService = new WireMockRule();

  @Rule
  public TogglesAwareTestRule togglesAwareTestRule =
      new TogglesAwareTestRule(
          () -> {
            togglesService.stubFor(
                get(urlEqualTo("/etc.clientlibs/toggles.json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{\"enabled\":[\"a\", \"b\", \"c\"]}")));

            try {
              return new SlingClient(
                  URI.create(String.format("http://localhost:%d", togglesService.port())), "", "");
            } catch (ClientException e) {
              return null;
            }
          });

  @Test
  @RunIfToggleEnabled("a")
  public void testRunIfToggleEnabled() {}

  @Test
  @RunIfToggleEnabled("dummy")
  public void testNotRunIfToggleNotEnabled() {
    fail();
  }

  @Test
  @SkipIfToggleEnabled("dummy")
  public void testNotSkipIfToggleEnabled() {}

  @Test
  @SkipIfToggleEnabled("a")
  public void testSkipIfToggleNotEnabled() {
    fail();
  }

  @Test
  public void testRunNoAnnotation() {}

  @Test
  @RunIfToggleEnabled("a")
  @SkipIfToggleEnabled("b")
  public void testSkipIfRunSkip() {
    fail(); // Skip condition is met
  }

  @Test
  @RunIfToggleEnabled("a")
  @SkipIfToggleEnabled("dummy")
  public void testRunIfRunNotSkip() {}

  @Test
  @RunIfToggleEnabled("dummy")
  @SkipIfToggleEnabled("a")
  public void testSkipIfNotRunSkip() {
    fail(); // Skip condition is met
  }

  @Test
  @RunIfToggleEnabled("dummy")
  @SkipIfToggleEnabled("yummy")
  public void testSkipIfNotRunNotSkip() {
    fail(); // Run condition is not met
  }
}
