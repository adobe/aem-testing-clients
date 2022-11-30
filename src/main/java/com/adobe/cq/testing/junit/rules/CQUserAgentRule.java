package com.adobe.cq.testing.junit.rules;

import org.apache.sling.testing.clients.interceptors.UserAgentHolder;
import org.apache.sling.testing.clients.util.UserAgentUtil;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class CQUserAgentRule implements TestRule {

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                starting(description);
                try {
                    base.evaluate();
                } finally {
                    finished();
                }
            }
        };
    }

    protected void starting(final Description description) {
        UserAgentHolder.set(
                UserAgentUtil.constructAgent("aem-testing-client", getClass().getPackage()) +
                " ("+description.getTestClass().getSimpleName()+")"
        );
    }

    protected void finished() {
        UserAgentHolder.reset();
    }
}
