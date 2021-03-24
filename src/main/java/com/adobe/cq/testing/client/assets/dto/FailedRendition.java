/*
 * Copyright 2021 Adobe Systems Incorporated
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

package com.adobe.cq.testing.client.assets.dto;

/**
 * Simple DTO for a failed rendition.
 */
public class FailedRendition {

    private final String name;
    private final String message;
    private final String reason;

    /**
     * Constructor.
     *
     * @param name String
     * @param message String
     * @param reason String
     */
    public FailedRendition(final String name, final String message, final String reason) {
        this.name = name;
        this.message = message;
        this.reason = reason;
    }

    /**
     * Get the failed rendition's name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Get the failed rendition's message.
     *
     * @return String
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the failed rendition's reason.
     *
     * @return String
     */
    public String getReason() {
        return reason;
    }
}
