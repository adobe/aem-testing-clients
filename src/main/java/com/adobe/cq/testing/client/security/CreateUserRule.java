package com.adobe.cq.testing.client.security;

import com.adobe.cq.testing.client.CQClient;
import com.adobe.cq.testing.client.CQSecurityClient;
import org.apache.sling.testing.clients.SlingClient;
import org.apache.sling.testing.clients.util.poller.Polling;
import org.apache.sling.testing.junit.rules.instance.Instance;
import org.junit.rules.ExternalResource;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Create and cleanup at the end a user belonging to the Authors group
 */
public class CreateUserRule extends ExternalResource implements UserRule {
    private Instance instanceRule;
    private CQSecurityClient adminAuthor;
    private final String[] groups;
    private ThreadLocal<CQClient> userClient = new ThreadLocal<>();
    private ThreadLocal<User> user = new ThreadLocal<>();

    public CreateUserRule(Instance instanceRule, String... groups) {
        this.instanceRule = instanceRule;
        this.groups = groups;
    }

    @Override
    protected void before() throws Throwable {
        adminAuthor = instanceRule.getAdminClient(CQSecurityClient.class);
        String username = "testuser-" + UUID.randomUUID() ;
        final String password = randomPass(30);
        user.set(adminAuthor.createUser(
                username,
                password,
                Arrays.stream(groups).map(this::getGroup).toArray(Group[]::new)
        ));
        Thread.sleep(500);
        new Polling(() -> user.get().exists()).poll(5000, 500);
        userClient.set(new CQClient(adminAuthor.getUrl(), username, password));
    }

    @Override
    protected void after() {
        try {
            new Polling(() -> {
                adminAuthor.deleteAuthorizables(new Authorizable[]{user.get()});
                    return !user.get().exists();
            }).poll(5000, 500);
        } catch (Exception e) {
            throw new RuntimeException(e);
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

    private String randomPass(int length) {
        return new Random().ints(97, 123)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    private Group getGroup(String groupName) {
        try {
            return new Group(adminAuthor, groupName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
