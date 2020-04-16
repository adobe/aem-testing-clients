/*
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 * Copyright 2019 Adobe
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
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
