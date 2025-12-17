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

package org.apache.axis2.transport.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Async callback/latch that mediation completes with the final AckDecision. */
public final class AckDecisionCallback {
    private static final Log log = LogFactory.getLog(AckDecisionCallback.class);
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<AckDecision> ackDecisionAtomicReference = new AtomicReference<>();


    /**
     * Attempts to set the final AckDecision exactly once (null defaults to SET_REQUEUE_ON_ROLLBACK),
     * releases waiting threads via latch on success, and returns true only for the first caller.
     **/
    public boolean complete(AckDecision decision) {
        AckDecision toSet = decision;

        if (decision == null) {
            toSet = AckDecision.SET_REQUEUE_ON_ROLLBACK;
            log.warn("Null AckDecision provided; defaulting to " + toSet + " (requeue on rollback).");
        }

        boolean set = ackDecisionAtomicReference.compareAndSet(null, toSet);
        if (set) {
            log.info("AckDecisionCallback completed successfully with decision: " + toSet);
            latch.countDown();
        } else {
            log.warn("Attempt to complete AckDecisionCallback multiple times; ignoring : " + toSet);
        }
        return set;
    }

    /** Blocks up to timeout; returns the AckDecision or null if it times out. */
    public AckDecision await(long timeoutMillis) throws InterruptedException {
        if (latch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            return ackDecisionAtomicReference.get();
        }
        return null;
    }
}
