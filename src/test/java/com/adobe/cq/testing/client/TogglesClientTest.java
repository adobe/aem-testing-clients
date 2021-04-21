package com.adobe.cq.testing.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

import java.net.URI;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static spark.Spark.awaitInitialization;
import static spark.Spark.get;
import static spark.Spark.port;

public class TogglesClientTest {

    @BeforeClass
    public static void startServer() {
        port(0);
        get("etc.clientlibs/toggles.json", (req, res) -> "{\"enabled\":[\"a\", \"b\", \"c\"]}");
        awaitInitialization();
    }

    @AfterClass
    public static void stopServer() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void testGetEnabledToggles() throws Exception {
        TogglesClient client = new TogglesClient(URI.create(String.format("http://localhost:%d", port())), "", "");
        List<String> toggles = client.getEnabledToggles();
        assertArrayEquals(toggles.toArray(), new String[]{"a", "b", "c"});
    }

    @Test
    public void testIsToggleEnabled() throws Exception {
        TogglesClient client = new TogglesClient(URI.create(String.format("http://localhost:%d", port())), "", "");
        assertTrue(client.isToggleEnabled("a"));
        assertFalse(client.isToggleEnabled("e"));
    }

}