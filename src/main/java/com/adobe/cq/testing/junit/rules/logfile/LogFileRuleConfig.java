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


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to configure the log file rule.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface LogFileRuleConfig {

    /**
     * The log file names to parse for errors.
     * @return the list of filenames
     */
    String[] fileNames() default {"error.log"};

    /**
     * List of known and accepted errors.
     * @return the list of issues
     */
    String[] knownIssues() default {};

    /**
     * End markers per log file. The log files are only scanned up to the line containing these end markers.
     * @return the list of markers
     */
    String[] endMarkers() default {"error.log;WorkflowLauncherListener StartupListener.startupFinished called"};

    /**
     * Lines containing at least one of these markers are considered an error.
     * @return the list of markers
     */
    String[] errorMarkers() default {"ERROR", "WARN"};

    /**
     * Whether to stop scanning the log files once an error is detected.
     * @return true if the scanning is stopped once an error is detected
     */
    boolean stopAtError() default true;

    /**
     * System property containing the full path of the quickstart folders. This property is usually set with the
     * quickstart-runner.properties file during quickstart build.
     * @return the property name
     */
    String quickstartFolderProperty() default "granite.it.default.quickstart.folders";

    /**
     * Whether to read the log files from the test resources or not. Usable for testing the rule itself.
     * @return true if the log files are read from the test resources
     */
    boolean readLogFileFromTestResources() default false;

}
