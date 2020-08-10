package com.adobe.cq.testing.client.security;

import com.adobe.cq.testing.client.SecurityClient;
import org.apache.commons.logging.Log;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.util.config.InstanceConfig;
import org.apache.sling.testing.clients.util.config.InstanceConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.UUID;

public class NewRandomUserInstanceConfig implements InstanceConfig {
    private static final Logger LOG = LoggerFactory.getLogger(NewRandomUserInstanceConfig.class);
    private final Group[] assignedGroups;
    private final SecurityClient client;
    String username;
    String password;
    private User user;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NewRandomUserInstanceConfig(SecurityClient client, Group[] assignedGroups) {
        this.client = client;
        this.assignedGroups = assignedGroups;
    }

    @Override
    public InstanceConfig save() throws InstanceConfigException, InterruptedException {
        // Whenever creating a user, try a new random username and store it
        this.username = "testuser-" + UUID.randomUUID();
        this.password = randomPass(30);
        try {
            this.user = client.createUser(username, password, assignedGroups);
        } catch (ClientException e) {
            throw new InstanceConfigException(e);
        }
        return this;
    }

    @Override
    public InstanceConfig restore() throws InstanceConfigException, InterruptedException {
        try {
            if (null == user && User.exists(client, username)) {
                LOG.info("Deleting user {} (operation failed, but the user is present on the instance)", username);
                user = new User(client, username);
            }
        } catch (ClientException e) {
            throw new InstanceConfigException(e);
        }
        try {
            client.deleteAuthorizables(new Authorizable[]{user});
            LOG.info("Deleted user {}", username);
        } catch (ClientException e) {
            throw new InstanceConfigException(e);
        }
        return this;
    }

    private String randomPass(int length) {
        return new Random().ints(97, 123)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
