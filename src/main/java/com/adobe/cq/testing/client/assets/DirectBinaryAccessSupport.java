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

package com.adobe.cq.testing.client.assets;

/**
 * Tri-state of DirectBinaryAccess support
 */
public class DirectBinaryAccessSupport {

    enum State {
        UNKNOWN,
        SUPPORTED,
        UNSUPPORTED
    }

    private State state;

    public DirectBinaryAccessSupport() {
        this.state = State.UNKNOWN;
    }

    public boolean isUnknown() {
        return this.state == State.UNKNOWN;
    }

    public boolean isSupported() {
        return this.state == State.SUPPORTED;
    }

    public void setSupported(boolean supported) {
        this.state = supported ? State.SUPPORTED : State.UNSUPPORTED;
    }

}
