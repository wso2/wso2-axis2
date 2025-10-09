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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Singleton timer class for managing graceful shutdown operations.
 * Allows starting a shutdown countdown, checking its status, and querying remaining time.
 */
public class GracefulShutdownTimer extends TimerTask {

    private static final Log log = LogFactory.getLog(GracefulShutdownTimer.class);
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

    /**
     * Marks the timer as expired when the scheduled time elapses.
     * This method is called by the Timer framework.
     */
    @Override
    public void run() {
        expired = true;
    }

    /**
     * Starts the graceful shutdown timer with the specified timeout.
     *
     * @param shutdownTimeoutMillis the timeout in milliseconds before shutdown is considered expired
     */
    public void start(long shutdownTimeoutMillis) {
        if (started) {
            log.warn("Graceful shutdown timer is already started.");
            return;
        }
        this.startTime = System.currentTimeMillis();
        this.shutdownTimeoutMillis = shutdownTimeoutMillis;
        Timer timer = new Timer(GRACEFUL_SHUTDOWN_TIMEOUT_THREAD_NAME, true);
        timer.schedule(this, shutdownTimeoutMillis);
        started = true;
    }

    /**
     * Checks if the shutdown timer has been started.
     *
     * @return true if started, false otherwise
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Checks if the shutdown timer has expired.
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expired;
    }

    public long getShutdownTimeoutMillis() {
        return shutdownTimeoutMillis;
    }

    /**
     * Returns the remaining time in milliseconds until shutdown.
     * Returns 0 if the timer has already expired.
     *
     * @return remaining time in milliseconds, or 0 if expired
     */
    public long getRemainingTimeMillis() {
        if (expired || !started) {
            return 0;
        }
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = shutdownTimeoutMillis - elapsed;
        return Math.max(remaining, 0);
    }
}
