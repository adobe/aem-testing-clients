package com.adobe.cq.testing.client;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spark.Spark;

import static spark.Spark.port;

public class TogglesClientTest {

    @Before
    public void startServer() {
        port(0);
    }

    @After
    public void stopServer() {
        Spark.stop();
        Spark.awaitStop();
    }

    @Test
    public void testGetEnabledToggles() {

    }

}