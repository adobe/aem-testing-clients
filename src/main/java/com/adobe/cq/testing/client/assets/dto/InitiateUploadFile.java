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
 * Part of a successful initiateUpload response, mapped by ObjectMapper from the response JSON.
 */
public class InitiateUploadFile {

    private String fileName = "";
    private long minPartSize;
    private long maxPartSize;
    private List<String> uploadURIs = Collections.emptyList();
    private String mimeType = "";
    private String uploadToken = "";

    @Nonnull
    public String getFileName() {
        return fileName;
    }

    public void setFileName(@Nonnull String fileName) {
        this.fileName = fileName;
    }

    public long getMinPartSize() {
        return minPartSize;
    }

    public void setMinPartSize(long minPartSize) {
        this.minPartSize = minPartSize;
    }

    public long getMaxPartSize() {
        return maxPartSize;
    }

    public void setMaxPartSize(long maxPartSize) {
        this.maxPartSize = maxPartSize;
    }

    @Nonnull
    public List<String> getUploadURIs() {
        return uploadURIs;
    }

    public void setUploadURIs(@Nonnull List<String> uploadURIs) {
        this.uploadURIs = uploadURIs;
    }

    @Nonnull
    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(@Nonnull String mimeType) {
        this.mimeType = mimeType;
    }

    @Nonnull
    public String getUploadToken() {
        return uploadToken;
    }

    public void setUploadToken(@Nonnull String uploadToken) {
        this.uploadToken = uploadToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        InitiateUploadFile that = (InitiateUploadFile) o;

        return new EqualsBuilder()
                .append(minPartSize, that.minPartSize)
                .append(maxPartSize, that.maxPartSize)
                .append(fileName, that.fileName)
                .append(uploadURIs, that.uploadURIs)
                .append(mimeType, that.mimeType)
                .append(uploadToken, that.uploadToken)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(fileName)
                .append(minPartSize)
                .append(maxPartSize)
                .append(uploadURIs)
                .append(mimeType)
                .append(uploadToken)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "InitiateUploadFile{" +
                "fileName='" + fileName + '\'' +
                ", minPartSize=" + minPartSize +
                ", maxPartSize=" + maxPartSize +
                ", uploadURIs=" + uploadURIs +
                ", mimeType='" + mimeType + '\'' +
                ", uploadToken='" + uploadToken + '\'' +
                '}';
    }

}
