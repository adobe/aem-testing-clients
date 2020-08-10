package com.adobe.cq.testing.client.security;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.CQSecurityClient;
import com.adobe.cq.testing.client.SecurityClient;
import org.apache.sling.testing.clients.ClientException;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.config.InstanceConfig;
import org.apache.sling.testing.clients.util.config.InstanceConfigCache;
import org.apache.sling.testing.clients.util.config.impl.InstanceConfigCacheImpl;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Create and cleanup at the end a user belonging to the Authors group
 */
public class CreateUserRule extends ExternalResource implements UserRule {
    private static final Logger LOG = LoggerFactory.getLogger(CreateUserRule.class);
    private Instance instanceRule;
    private CQSecurityClient adminAuthor;
    private final String[] groups;
    private ThreadLocal<CQClient> userClient = new ThreadLocal<>();
    private ThreadLocal<InstanceConfigCache> usersToDelete = new ThreadLocal<>();

    public CreateUserRule(Instance instanceRule, String... groups) {
        this.instanceRule = instanceRule;
        this.groups = groups;
    }

    private class UserCreateCallable implements Callable<Boolean> {
        private final Group[] assignedGroups;
        private final InstanceConfigCache userConfigs = new InstanceConfigCacheImpl();
        private final SecurityClient client;
        private NewRandomUserInstanceConfig successfulUserConfig;

        public UserCreateCallable(SecurityClient client, Group[] assignedGroups) {
            this.client = client;
            this.assignedGroups = assignedGroups;
        }

        public String getUsername() {
            return (null != successfulUserConfig) ? successfulUserConfig.getUsername() : null;
        }

        public String getPassword() {
            return (null != successfulUserConfig) ? successfulUserConfig.getPassword() : null;
        }

        public InstanceConfigCache getUserConfigs() {
            return userConfigs;
        }

        public NewRandomUserInstanceConfig getSuccessfulUserConfig() {
            return successfulUserConfig;
        }

        @Override
        public Boolean call() throws Exception {
            final NewRandomUserInstanceConfig config = new NewRandomUserInstanceConfig(client, assignedGroups);
            userConfigs.add(config);
            config.save();
            successfulUserConfig = config;
            return true;
        }
    }

    @Override
    protected void before() throws Throwable {
        adminAuthor = instanceRule.getAdminClient(CQSecurityClient.class);
        Group[] assignedGroups = Arrays.stream(groups).map(this::getGroup).toArray(Group[]::new);
        UserCreateCallable c = new UserCreateCallable(adminAuthor, assignedGroups);

        Polling p = new Polling(c);
        try {
            p.poll(10000, 1000);
        } catch (TimeoutException e) {
            LOG.error("Could not create user. List of exceptions: " + p.getExceptions(), e);
            usersToDelete.set(c.getUserConfigs());
            // After is not called by JUnit if before() throws
            after();
            throw e;
        }
        usersToDelete.set(c.getUserConfigs());
        Thread.sleep(500);

        // Wait until user exists
        new Polling(() -> c.getSuccessfulUserConfig().getUser().exists()).poll(5000, 500);
        userClient.set(new CQClient(adminAuthor.getUrl(), c.getUsername(), c.getPassword()));
    }

    @Override
    protected void after() {
        // Go through all the attempted users
        LOG.info("Cleaning up all attempted user creations");
        for (InstanceConfig userConfig : usersToDelete.get()) {
            final NewRandomUserInstanceConfig cfg;
            if (!(userConfig instanceof NewRandomUserInstanceConfig)) {
                continue;
            }
            // TODO: Sling testing clients needs parameter type for InstanceConfig and InstanceConfigCache
            cfg = (NewRandomUserInstanceConfig) userConfig;

            try {
                // poll their deletion until it doesn't exist
                new Polling(() -> {
                    cfg.restore();
                    return !User.exists(adminAuthor, cfg.getUsername());
                }).poll(5000, 500);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public CQClient getClient() {
        return this.userClient.get();
    }

    /**
     * @return a <code>SlingClient</code> Supplier
     */
    public Supplier<SlingClient> getClientSupplier() {
        class ClientSupplier implements Supplier<SlingClient>{
            private final CreateUserRule userRule;
            public ClientSupplier(CreateUserRule userRule) {
                this.userRule = userRule;
            }

            @Override
            public SlingClient get() {
                return this.userRule.getClient();
            }
        }
        return new ClientSupplier(this);
    }

    private Group getGroup(String groupName) {
        try {
            return new Group(adminAuthor, groupName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
