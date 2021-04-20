package com.adobe.cq.testing.junit.rules.toggles;

import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.fail;
import static spark.Spark.awaitInitialization;
import static spark.Spark.awaitStop;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.stop;

public class TogglesAwareTestRuleTest {

    @BeforeClass
    public static void startServer() {
        port(0);
        get("etc.clientlibs/toggles.json", (req, res) -> "{\"enabled\":[\"a\", \"b\", \"c\"]}");
        awaitInitialization();
    }

    @AfterClass
    public static void stopServer() {
        stop();
        awaitStop();
    }

    @Rule
    public TogglesAwareTestRule togglesAwareTestRule = new TogglesAwareTestRule(() -> {
        try {
            return new SlingClient(URI.create(String.format("http://localhost:%d", port())), "", "");
        } catch (ClientException e) {
            return null;
        }
    });

    @Test
    @RunIfToggleEnabled("dummy")
    public void testSkipIfToggleNotEnabled() {
        fail();
    }

    @Test
    @RunIfToggleEnabled("a")
    public void testRunIfToggleEnabled() {
    }

    @Test
    public void testRunNoToggleAnnotation() {
    }
}
