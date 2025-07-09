package com.adobe.cq.testing.junit.rules;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.Statement;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;

public class TemporaryUserTest {

    @Rule
    public WireMockRule aemService = new WireMockRule();

    @Test
    public void testUserIsCreated() throws Throwable {
        aemService.stubFor(get(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"authorizables\": [{ \"home\": \"/home/users/dummy\"}] }")));
        aemService.stubFor(post(urlEqualTo("/libs/granite/security/post/authorizables.html"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));
        aemService.stubFor(post(urlEqualTo("/home/users/dummy.rw.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        TemporaryUser temporaryUserRule = new TemporaryUser(() -> {
            try {
                return new SlingClient(URI.create(String.format("http://localhost:%d", aemService.port())), "", "");
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            }
        }, "my-group");
        Statement statement = temporaryUserRule.apply(new Statement() {
            @Override
            public void evaluate() {

            }
        }, null);
        statement.evaluate();
    }

    @Test
    public void testAuthorizableInstability() throws Throwable {
        aemService.stubFor(get(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                .withQueryParam("query", equalTo("{\"condition\":[{\"named\":\"my-group\"}]}"))
                .inScenario("get-groups")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody(""))
                .willSetStateTo("retry"));
        aemService.stubFor(get(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                .withQueryParam("query", equalTo("{\"condition\":[{\"named\":\"my-group\"}]}"))
                .inScenario("get-groups")
                .whenScenarioStateIs("retry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"authorizables\":[{\"home\":\"/home/groups/my-group\"}]}")));

        aemService.stubFor(get(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                .withQueryParam("query", containing("testuser"))
                .inScenario("get-user")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(""))
                .willSetStateTo("retry"));
        aemService.stubFor(get(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                .withQueryParam("query", containing("testuser"))
                .inScenario("get-user")
                .whenScenarioStateIs("retry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"authorizables\": [{ \"home\": \"/home/user/a/abcdef\"}] }")));

        aemService.stubFor(post(urlEqualTo("/libs/granite/security/post/authorizables.html"))
                .inScenario("create-user")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse()
                        .withStatus(407)
                        .withBody(""))
                .willSetStateTo("retry"));
        aemService.stubFor(post(urlEqualTo("/libs/granite/security/post/authorizables.html"))
                .inScenario("create-user")
                .whenScenarioStateIs("retry")
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        aemService.stubFor(post(urlEqualTo("/home/groups/my-group.rw.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));
        aemService.stubFor(post(urlEqualTo("/home/user/a/abcdef.rw.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        TemporaryUser temporaryUserRule = new TemporaryUser(() -> {
            try {
                return new SlingClient(URI.create(String.format("http://localhost:%d", aemService.port())), "", "");
            } catch (ClientException e) {
                e.printStackTrace();
                return null;
            }
        }, "my-group");
        Statement statement = temporaryUserRule.apply(new Statement() {
            @Override
            public void evaluate() {

            }
        }, null);
        statement.evaluate();

        verify(exactly(2),
                getRequestedFor(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                        .withQueryParam("query", equalTo("{\"condition\":[{\"named\":\"my-group\"}]}")));
        verify(exactly(6),
                getRequestedFor(urlPathEqualTo("/libs/granite/security/search/authorizables.json"))
                        .withQueryParam("query", containing("testuser")));
        verify(exactly(2), postRequestedFor(urlEqualTo("/libs/granite/security/post/authorizables.html")));
        verify(exactly(1), postRequestedFor(urlEqualTo("/home/groups/my-group.rw.html")));
        verify(exactly(2), postRequestedFor(urlEqualTo("/home/user/a/abcdef.rw.html")));
    }
}
