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
package com.adobe.cq.testing.util;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.sling.testing.clients.ClientException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Some helper utilities for testing.
 */
public class TestUtil {

    /**
     * The formatter for ISO8601 date time including milliseconds and time zone.
     * That's how date stings must look like to be send and stored in nodes using post request
     */
    public static final FastDateFormat ISO_DATETIME_TIME_ZONE_FORMAT = FastDateFormat
            .getInstance("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");


    /**
     * Parses a string representing a date, as it is returned when requesting a node using .json
     *
     * @param dateString string representing a date
     * @return the date object
     * @throws ClientException if the string cannot be parsed
     */
    public static Date parseJsonDateString(String dateString) throws ClientException {
        try {
            return new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH).parse(dateString);
        } catch (ParseException e) {
            throw new ClientException("Couldn't parse date from string!",e);
        }
    }

    /**
     * Calculates the MD5 checksum for a input stream. The input stream gets closed
     * after checksum has been calculated.
     *
     * @param stream the input stream
     * @return the MD5 Checksum string
     */
    public static String getMD5Checksum(InputStream stream) {
        //read filestream and create a byte array
        byte[] buffer = new byte[1024];
        MessageDigest complete;

        if (null == stream) {
            throw new IllegalArgumentException("Input stream can't be null!");
        }

        try {
            complete = MessageDigest.getInstance("MD5");
            int numRead;
            do {
                numRead = stream.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
            stream.close();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        byte[] b = complete.digest();

        //write the byte array to a HEX string
        String result = "";
        for (byte aB : b) {
            result +=
                    Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    /**
     * Makes a MD5 compare of two input streams. the input streams are closed after md5 has been calculated.
     *
     * @param i1 First Input Stream
     * @param i2 Second Input Stream
     * @return true if binaries are the same otherwise false
     */
    public static boolean binaryCompare(InputStream i1, InputStream i2) {
        try {
            if (i1 == null) {
                if (i2 != null) {
                    i2.close();
                    throw new AssertionError("Input stream i1 must not be null for binary compare!");
                }
            }
            if (i2 == null) {
                if (i1 != null) {
                    i1.close();
                }
                throw new AssertionError("Input stream i2 must not be null for binary compare!");
            }
        } catch (IOException e) {
            throw new AssertionError("Can't close Input stream!");
        }

        String md51 = getMD5Checksum(i1);
        String md52 = getMD5Checksum(i2);

        return md51.equals(md52);
    }

    /**
     * Escapes special characters in the string to avoid breaking a regexp
     *
     * @param regexp regexp to escape
     * @return the escaped regexp
     */
    public static String replaceSpecialCharsForRegexp(String regexp) {
        String spRegexp = regexp;
        // replace special chars not to be used as regexp
        spRegexp = spRegexp.replace("(", "\\(").replace(")", "\\)");
        spRegexp = spRegexp.replace("{", "\\{").replace("}", "\\}");
        spRegexp = spRegexp.replace(":", "\\:");
        return spRegexp;
    }
}
