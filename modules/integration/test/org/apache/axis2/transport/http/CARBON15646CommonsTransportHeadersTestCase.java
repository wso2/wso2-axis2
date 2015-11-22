package org.apache.axis2.transport.http;

import junit.framework.TestCase;
import org.apache.commons.httpclient.Header;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class CARBON15646CommonsTransportHeadersTestCase extends TestCase {
    Header[] headers;
    CommonsTransportHeaders commonsTransportHeaders;
    String value;

    public void initForRandomNoOfHeaders() {
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

    @Test public void testForCARBON15646() {

        initForRandomNoOfHeaders();

        // This is to get init() method called.
        commonsTransportHeaders.isEmpty();
        HashMap headerMap = commonsTransportHeaders.headerMap;
        Assert.assertTrue(headerMap.containsKey("name"));
        Assert.assertEquals(headerMap.get("name"), value);
    }
}
