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
    @RunIfToggleEnabled("a")
    public void testRunIfToggleEnabled() {
    }

    @Test
    @RunIfToggleEnabled("dummy")
    public void testNotRunIfToggleNotEnabled() {
        fail();
    }

    @Test
    @SkipIfToggleEnabled("dummy")
    public void testNotSkipIfToggleEnabled() {
    }

    @Test
    @SkipIfToggleEnabled("a")
    public void testSkipIfToggleNotEnabled() {
        fail();
    }

    @Test
    public void testRunNoAnnotation() {
    }

    @Test
    @RunIfToggleEnabled("a")
    @SkipIfToggleEnabled("b")
    public void testSkipIfRunSkip() {
        fail(); // Skip condition is met
    }

    @Test
    @RunIfToggleEnabled("a")
    @SkipIfToggleEnabled("dummy")
    public void testRunIfRunNotSkip() {
    }

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
