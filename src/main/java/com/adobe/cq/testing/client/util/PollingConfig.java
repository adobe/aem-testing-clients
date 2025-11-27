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
package com.adobe.cq.testing.client.util;

public class PollingConfig {
  private final long timeout;
  private final long interval;

  /**
   *make new polling config
   *
   * @param timeout timeout in millisec
   * @param interval interval in millisec
   */
  public PollingConfig(long timeout, long interval) {
    this.timeout = timeout;
    this.interval = interval;
  }

  public long getTimeout() {
    return timeout;
  }

  public long getInterval() {
    return interval;
  }

  /**quick polling config - 10s timeout, 100ms interval*/
  public static PollingConfig fast() {
    long fastTimeout = 10000;
    long fastInterval = 100;
    return new PollingConfig(fastTimeout, fastInterval);
  }

  /**standard polling config - 5s timeout, 500ms interval*/
  public static PollingConfig standard() {
    long stdTimeout = 5000;
    long stdInterval = 500;
    return new PollingConfig(stdTimeout, stdInterval);
  }
}
