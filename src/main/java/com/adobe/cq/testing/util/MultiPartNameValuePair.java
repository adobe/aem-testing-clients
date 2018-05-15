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

/**
 *
 */
package com.adobe.cq.testing.util;

import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.Charset;

public class MultiPartNameValuePair extends BasicNameValuePair {
    /**
     * Serial Version Uid
     */
    private static final long    serialVersionUID    = 1L;
    private String    mimetype, charset;

    /**
     * Generates a replica of BasicNameValuePair
     *
     * @param name name
     * @param value value
     */
    public MultiPartNameValuePair(String name, String value){
        super(name, value);
    }

    /**
     * Generates a customized NameValuePair for multipart entities.
     *
     * @param name name
     * @param value value
     * @param mimetype mime type
     * @param charset charset
     */
    public MultiPartNameValuePair(String name, String value, String mimetype, String charset) {
        super(name, value);
        this.mimetype = mimetype;
        this.charset = charset;
    }


    /**
     * @return the mimetype
     */
    public String getMimetype() {
        return mimetype;
    }

    /**
     * @return the charset
     */
    public Charset getCharset() {
        return Charset.forName(charset);
    }

}
