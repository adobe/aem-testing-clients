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

import org.junit.runners.model.Statement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class LogFileRuleStatement extends Statement {

    private LogFileRule logFileRule;

    private Statement baseStatement;

    public LogFileRuleStatement(LogFileRule logFileRule, Statement baseStatement) {
        this.logFileRule = logFileRule;
        this.baseStatement = baseStatement;
    }

    @Override
    public void evaluate() throws Throwable {
        List<String> errors = new ArrayList<>();
        try {
            //Before
            logFileRule.setErrors(new ArrayList<String>());
            List<File> logFiles = getLogFiles();
            for (File logFile : logFiles) {
                testLogFile(errors, logFile);
            }
        } finally {
            logFileRule.setErrors(errors);
            baseStatement.evaluate();
            //After
            logFileRule.setErrors(new ArrayList<String>());
        }
    }

    private List<File> getLogFiles() {
        List<File> logFiles = new ArrayList<>();
        final String folders = System.getProperty(logFileRule.getRuleConfig().quickstartFolderProperty(), "target/author/crx-quickstart");
        final String[] dirs = folders.split(File.pathSeparator);
        for (String dir : dirs) {
            File quickstart;
            if (logFileRule.getRuleConfig().readLogFileFromTestResources()) {
                if (!dir.startsWith("/")) {
                    dir = "/" + dir;
                }
                URL url = this.getClass().getResource(dir);
                quickstart = new File(url.getFile());

            } else {
                quickstart = new File(dir);
            }
            final File logs = new File(quickstart, "logs");
            assertTrue("logs is not a directory", logs.isDirectory());
            File[] files = logs.listFiles();

            for (File file : files != null ? files : new File[0]) {
                if (file != null && isFileUnderTest(file.getName())) {
                    logFiles.add(file);
                }
            }
        }
        return logFiles;
    }


    private void testLogFile(final List<String> errors, final File logFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            final String endMarker = logFileRule.getEndMarkerForLogFile(logFile.getName());
            String logLine = reader.readLine();
            while (logLine != null) {
                if (endMarker != null && logLine.contains(endMarker)) {
                    break;
                }
                if (logLineHasErrors(logLine)) {
                    errors.add(logFile.getName() + " contains the unexpected line: " + logLine);
                    if (logFileRule.getRuleConfig().stopAtError()) {
                        return;
                    }
                }
                logLine = reader.readLine();
            }
        }
    }


    private boolean logLineHasErrors(final String line) {
        return isErrorLine(line) && !isKnownIssue(line);
    }

    private boolean isErrorLine(final String line) {
        for (String errorMarker : logFileRule.getRuleConfig().errorMarkers()) {
            if (line.contains(errorMarker)) {
                return true;
            }
        }
        return false;
    }

    private boolean isKnownIssue(final String line) {
        for (String pattern : logFileRule.getRuleConfig().knownIssues()) {
            if (line.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean isFileUnderTest(String fileName) {
        for (String currentFileName : logFileRule.getRuleConfig().fileNames()) {
            if (fileName.equals(currentFileName)) {
                return true;
            }
        }
        return false;
    }

}
