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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Test the {@link LogFileRule}.
 */
public class LogFileRuleTest {

    @Rule
    public LogFileRule logFileRule = new LogFileRule();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Test defaults.
     */
    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true
    )
    public void testErrors() {
        List<String> errors = logFileRule.getErrors();
        assertEquals("Number of expected errors does not match.", 1, errors.size());
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            stopAtError = false
    )
    public void testStopAtError() {
        List<String> errors = logFileRule.getErrors();
        assertEquals("Number of expected errors does not match", 4, errors.size());
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            stopAtError = false,
            errorMarkers = "ERROR"
    )
    public void testErrorMarkersErrors() {
        List<String> errors = logFileRule.getErrors();
        assertEquals("Number of expected errors does not match", 2, errors.size());
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            stopAtError = false,
            errorMarkers = "WARN"
    )
    public void testErrorMarkersWarnings() {
        List<String> errors = logFileRule.getErrors();
        assertEquals("Number of expected warnings does not match", 2, errors.size());
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            stopAtError = false,
            knownIssues = {
                    "LuceneSearchCollection Error executing doUpdate",
                    "Unable to resolve the template for the project"
            }
    )
    public void testKnownIssues() {
        List<String> errors = logFileRule.getErrors();
        assertEquals("Number of expected errors does not match", 0, errors.size());
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true
    )
    public void testRuleConfigDefaults() {
        LogFileRuleConfig ruleConfig = logFileRule.getRuleConfig();
        assertNotNull("ruleConfig is null", ruleConfig);
        assertArrayEquals("default end markers is wrong", new String[]{"error.log;WorkflowLauncherListener StartupListener.startupFinished called"}, ruleConfig.endMarkers());
        assertArrayEquals("default error markers are wrong", new String[]{"ERROR","WARN"}, ruleConfig.errorMarkers());
        assertTrue("default stopAtError is wrong", ruleConfig.stopAtError());
        assertEquals("default quickstart property is wrong", "granite.it.default.quickstart.folders", ruleConfig.quickstartFolderProperty());
        assertArrayEquals("default known issues is wrong", new String[]{}, ruleConfig.knownIssues());
        assertArrayEquals("default file names is wrong", new String[]{"error.log"}, ruleConfig.fileNames());


    }

    /**
     * Test with no @LogFileRuleConfig annotation.
     */
    @Test
    public void testRuleConfig() {
        LogFileRuleConfig ruleConfig = logFileRule.getRuleConfig();
        assertNull("ruleConfig must be null", ruleConfig);
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            endMarkers = {"error.log;some end marker"}
    )
    public void testEndMarker() {
        String endMarker = logFileRule.getEndMarkerForLogFile("error.log");
        assertEquals("end marker rule does not match", "some end marker", endMarker);
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            endMarkers = {"error.log,some wrong separator for end marker"}
    )
    public void testEndMarkerConfigFormatSeparator() {
        exception.expect(IllegalArgumentException.class);
        logFileRule.getEndMarkerForLogFile("error.log");
    }

    @Test
    @LogFileRuleConfig(
            readLogFileFromTestResources = true,
            endMarkers = {"error.log;"}
    )
    public void testEndMarkerConfigFormat() {
        exception.expect(IllegalArgumentException.class);
        logFileRule.getEndMarkerForLogFile("error.log");
    }


}
