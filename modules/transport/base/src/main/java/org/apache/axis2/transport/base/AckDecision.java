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

public enum AckDecision {
    ACKNOWLEDGE,
    SET_ROLLBACK_ONLY,
    SET_REQUEUE_ON_ROLLBACK;

    /** Convert from string, null safe, returns null if no match. */
    public static AckDecision fromString(String decision) {
        if (decision == null) return null;
        switch (decision.trim()) {
            case "ACKNOWLEDGE": return ACKNOWLEDGE;
            case "SET_ROLLBACK_ONLY": return SET_ROLLBACK_ONLY;
            case "SET_REQUEUE_ON_ROLLBACK": return SET_REQUEUE_ON_ROLLBACK;
            default: return null;
        }
    }

    /** Convert to string, null safe, returns null if no match. */
    public String toStringValue() {
        switch (this) {
            case ACKNOWLEDGE: return "ACKNOWLEDGE";
            case SET_ROLLBACK_ONLY: return "SET_ROLLBACK_ONLY";
            case SET_REQUEUE_ON_ROLLBACK: return "SET_REQUEUE_ON_ROLLBACK";
            default: return null;
        }
    }
}
