package com.adobe.cq.testing.junit.rules;

import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.model.Statement;
import spark.Spark;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class TemporaryUserTest {

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
    public void testUserIsCreated() throws Throwable {
        get("/libs/granite/security/search/authorizables.json",
                (req, res) -> "{ \"authorizables\": [{ \"home\": \"/home/users/dummy\"}] }");
        post("/libs/granite/security/post/authorizables.html", (req, res) -> {
            res.status(201);
            return "{}";
        });
        post("/home/users/dummy.rw.html", (req, res) -> "{}");
        Spark.awaitInitialization();

        TemporaryUser temporaryUserRule = new TemporaryUser(() -> {
            try {
                return new SlingClient(URI.create(String.format("http://localhost:%d", port())),"","");
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
        AtomicInteger getGroupCalls = new AtomicInteger();
        AtomicInteger checkUserCalls = new AtomicInteger();
        AtomicInteger createUserCalls = new AtomicInteger();

        get("/libs/granite/security/search/authorizables.json", (req, res) -> {

            if (req.queryParams("query").equals("{\"condition\":[{\"named\":\"my-group\"}]}")) {
                // Request to instantiate the Group
                if (getGroupCalls.incrementAndGet() == 1) {
                    res.status(500);
                    return "";
                }
                return "{ \"authorizables\": [{ \"home\": \"/home/groups/my-group\"}] }";
            } else if (req.queryParams("query").contains("testuser")) {
                // Request to check that the user exists
                if (checkUserCalls.incrementAndGet() == 1) {
                    res.status(404);
                    return "";
                }

                return "{ \"authorizables\": [{ \"home\": \"/home/user/a/abcdef\"}] }";
            }

            res.status(400);
            return "";
        });
        post("/libs/granite/security/post/authorizables.html", (req, res) -> {
            // Request to create the user
            if (createUserCalls.incrementAndGet() == 1) {
                res.status(407);
                return "";
            }

            res.status(201);
            return "{}";
        });
        post("/home/groups/my-group.rw.html", (req, res) -> "{}"); // Upgrade Group
        post("/home/user/a/abcdef.rw.html", (req, res) -> "{}"); // Delete User
        Spark.awaitInitialization();

        TemporaryUser temporaryUserRule = new TemporaryUser(() -> {
            try {
                return new SlingClient(URI.create(String.format("http://localhost:%d", port())),"","");
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
}
