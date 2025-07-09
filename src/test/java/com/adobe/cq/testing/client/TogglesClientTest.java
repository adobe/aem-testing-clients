package com.adobe.cq.testing.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TogglesClientTest {

    @Rule
    public WireMockRule togglesService = new WireMockRule();

    @Before
    public void startServer() {
        togglesService.stubFor(get(urlEqualTo("/etc.clientlibs/toggles.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"enabled\":[\"a\", \"b\", \"c\"]}")));
    }

    @Test
    public void testGetEnabledToggles() throws Exception {
        TogglesClient client = new TogglesClient(URI.create(String.format("http://localhost:%d", togglesService.port())), "", "");
        List<String> toggles = client.getEnabledToggles();
        assertArrayEquals(toggles.toArray(), new String[]{"a", "b", "c"});
    }

    @Test
    public void testIsToggleEnabled() throws Exception {
        TogglesClient client = new TogglesClient(URI.create(String.format("http://localhost:%d", togglesService.port())), "", "");
        assertTrue(client.isToggleEnabled("a"));
        assertFalse(client.isToggleEnabled("e"));
    }

}