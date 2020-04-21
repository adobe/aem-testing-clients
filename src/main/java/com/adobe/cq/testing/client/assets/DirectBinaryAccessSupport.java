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
