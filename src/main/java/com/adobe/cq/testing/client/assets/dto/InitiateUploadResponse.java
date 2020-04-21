/*
 * Copyright 2020 Adobe Systems Incorporated
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

/**
 * Successful initiateUpload response, mapped by ObjectMapper from the response JSON.
 */
public class InitiateUploadResponse {

    private String folderPath = "";
    private List<InitiateUploadFile> files = Collections.emptyList();
    private String completeURI = "";

    @Nonnull
    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(@Nonnull String folderPath) {
        this.folderPath = folderPath;
    }

    @Nonnull
    public List<InitiateUploadFile> getFiles() {
        return files;
    }

    public void setFiles(@Nonnull List<InitiateUploadFile> files) {
        this.files = files;
    }

    @Nonnull
    public String getCompleteURI() {
        return completeURI;
    }

    public void setCompleteURI(@Nonnull String completeURI) {
        this.completeURI = completeURI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InitiateUploadResponse that = (InitiateUploadResponse) o;

        return new EqualsBuilder()
                .append(folderPath, that.folderPath)
                .append(files, that.files)
                .append(completeURI, that.completeURI)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(folderPath)
                .append(files)
                .append(completeURI)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "InitiateUploadResponse{" +
                "folderPath='" + folderPath + '\'' +
                ", files=" + files +
                ", completeURI='" + completeURI + '\'' +
                '}';
    }

}
