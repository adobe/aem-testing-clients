/*************************************************************************
 * ADOBE CONFIDENTIAL
 * ___________________
 *
 *  Copyright 2019 Adobe
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Adobe
 * and its suppliers and are protected by all applicable intellectual
 * property laws, including trade secret and copyright laws.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe.
 **************************************************************************/

package com.adobe.cq.testing.polling;

import org.apache.sling.testing.clients.SlingClient;
import org.junit.Test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

public class InstanceReadyPollerTest {

    @Test
    public void verifyCallingSetupDoesNotResultInException() {
        // GIVEN
        SlingClient slingClient = mock(SlingClient.class);

        // WHEN
        // THEN
        try {
            InstanceReadyPoller.installTest(slingClient);
        } catch (Exception e) {
            fail("Expected no exception while installing test but got " + e.getMessage());
        }
    }
}
