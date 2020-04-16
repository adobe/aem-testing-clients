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
