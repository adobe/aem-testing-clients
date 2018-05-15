/*
 * Copyright 2017 Adobe Systems Incorporated
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adobe.cq.testing.junit.rules.logfile;

import org.apache.commons.lang3.StringUtils;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * Junit rule for parsing log files.
 *
 * This rule is intended to be used during quickstart builds and allows to scan multiple log files for errors. The rule
 * is enabled by annotating a test method with the {@link LogFileRuleConfig} annotation. If enabled, the log files are
 * scanned before the test method is executed. Errors are reported in the rule and can be accessed in the test using
 * {@link LogFileRule#getErrors()}.
 *
 * See the {@link LogFileRuleConfig} for the various configuration options.
 *
 * @see LogFileRuleConfig
 */
public class LogFileRule implements MethodRule {

    private List<String> errors = new ArrayList<>();

    private LogFileRuleConfig ruleConfig;


    @Override
    public Statement apply(Statement baseStatement, FrameworkMethod frameworkMethod, Object o) {
        LogFileRuleConfig ruleConfig = frameworkMethod.getAnnotation(LogFileRuleConfig.class);
        //only intercept methods with a config
        if (ruleConfig != null) {
            this.ruleConfig = ruleConfig;
            return new LogFileRuleStatement(this, baseStatement);
        } else {
            this.ruleConfig = null;
            return baseStatement;
        }
    }


    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }

    public LogFileRuleConfig getRuleConfig() {
        return ruleConfig;
    }


    public String getEndMarkerForLogFile(String logFileName) {
        String endMarker = null;
        for (String currentEndMarker : ruleConfig.endMarkers()) {
            String[] parts = currentEndMarker.split(";");
            if (parts.length != 2) {
                throw new IllegalArgumentException("end marker not valid, must be of form <logfileName>;<endMarkerString>");
            }
            String currentLogFileName = parts[0];
            if (StringUtils.isBlank(currentLogFileName)) {
                throw new IllegalArgumentException("logfileName must not be blank");
            }
            if (currentLogFileName.equals(logFileName)) {
                endMarker = parts[1];
                break;
            }
        }

        if (StringUtils.isBlank(endMarker)) {
            throw new IllegalArgumentException("end marker must not be blank");
        }
        return endMarker;
    }

}
