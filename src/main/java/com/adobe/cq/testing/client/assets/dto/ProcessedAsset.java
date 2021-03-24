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

import java.util.List;

/**
 * Simple DTO for a processed asset.
 */
public class ProcessedAsset {

    private String assetPath;
    private List<String> processedRenditions;
    private List<FailedRendition> failedRenditions;

    /**
     * Constructor.
     */
    public ProcessedAsset() {
        // Do nothing.
    }

    /**
     * Get the processed asset's path.
     *
     * @return String
     */
    public String getAssetPath() {
        return assetPath;
    }

    /**
     * Set the processed asset's path.
     *
     * @param assetPath String
     */
    public void setAssetPath(final String assetPath) {
        this.assetPath = assetPath;
    }

    /**
     * Get the processed asset's processed renditions.
     *
     * @return String
     */
    public List<String> getProcessedRenditions() {
        return processedRenditions;
    }

    /**
     * Set the processed asset's processed renditions.
     *
     * @param processedRenditions List of String
     */
    public void setProcessedRenditions(final List<String> processedRenditions) {
        this.processedRenditions = processedRenditions;
    }

    /**
     * Get the processed asset's failed renditions.
     *
     * @return String
     */
    public List<FailedRendition> getFailedRenditions() {
        return failedRenditions;
    }

    /**
     * Set the processed asset's failed renditions.
     *
     * @param failedRenditions List of String
     */
    public void setFailedRenditions(final List<FailedRendition> failedRenditions) {
        this.failedRenditions = failedRenditions;
    }
}
