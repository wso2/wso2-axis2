/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.transport.http;

import junit.framework.TestCase;
import org.apache.commons.httpclient.Header;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class CommonsTransportHeadersTest extends TestCase {
    Header[] headers;
    CommonsTransportHeaders commonsTransportHeaders;
    String value;

    public void initHeaders() {
        int numberOfHeaders = ((Double) (Math.random() * 10000 % 100)).intValue() + 10;

        headers = new Header[numberOfHeaders];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = new Header("name", "value" + i);
            if (i == 0) {
                value = "value" + i;
            } else {
                value += "; value" + i;
            }
        }
        commonsTransportHeaders = new CommonsTransportHeaders(headers);
    }

    @Test
    public void testCompleteHeaderValueInclusion() {
        initHeaders();
        // This is to get init() method called.
        commonsTransportHeaders.isEmpty();
        Map headerMap = commonsTransportHeaders.headerMap;
        Assert.assertTrue(headerMap.containsKey("name"));
        Assert.assertEquals(headerMap.get("name"), value);
    }
}
