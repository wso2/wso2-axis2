/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.util;

import java.util.Timer;
import java.util.TimerTask;

public class GracefulShutdownTimer extends TimerTask {

    private long shutdownTimeoutMillis;
    private long startTime;
    private volatile boolean expired = false;
    private volatile boolean started = false;
    private final static String GRACEFUL_SHUTDOWN_TIMEOUT_THREAD_NAME = "MIGracefulShutdownTimer";

    private static volatile GracefulShutdownTimer instance;

    private GracefulShutdownTimer() {}

    public static GracefulShutdownTimer getInstance() {
        if (instance == null) {
            synchronized (GracefulShutdownTimer.class) {
                if (instance == null) {
                    instance = new GracefulShutdownTimer();
                }
            }
        }
        return instance;
    }

    @Override
    public void run() {
        expired = true;
    }

    public void start(long shutdownTimeoutMillis) {
        this.startTime = System.currentTimeMillis();
        Timer timer = new Timer(GRACEFUL_SHUTDOWN_TIMEOUT_THREAD_NAME, true);
        timer.schedule(this, shutdownTimeoutMillis);
        started = true;
    }

    public boolean isStarted() {

        return started;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setShutdownTimeoutMillis(long shutdownTimeoutMillis) {

        this.shutdownTimeoutMillis = shutdownTimeoutMillis;
    }

    public long getShutdownTimeoutMillis() {

        return shutdownTimeoutMillis;
    }

    public void reset() {
        started = false;
        expired = false;
        instance = null;
        startTime = 0;
    }

    /** Returns remaining time (ms) until shutdown, or 0 if already elapsed */
    public long getRemainingTimeMillis() {
        if (expired) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = shutdownTimeoutMillis - elapsed;
        return Math.max(remaining, 0);
    }
}
