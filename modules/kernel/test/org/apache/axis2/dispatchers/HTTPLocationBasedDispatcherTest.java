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

package org.apache.axis2.dispatchers;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.*;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.wsdl.WSDLUtil;

import javax.xml.namespace.QName;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

public class HTTPLocationBasedDispatcherTest extends TestCase {

    public void testValidateActionFlag() throws Exception {
        MessageContext mc = new MessageContext();

        Map<Pattern, AxisOperation> httpLocationTableForResource = new TreeMap<Pattern, AxisOperation>(
                new Comparator<Pattern>() {
                    public int compare(Pattern o1, Pattern o2) {
                        return (-1 * o1.pattern().compareTo(o2.pattern()));
                    }
                });
        AxisOperation operation1 = new InOnlyAxisOperation(new QName("operation1"));
        String method1 = "GET";
        String httpLocation1 = "student";
        AxisOperation operation2 = new InOnlyAxisOperation(new QName("operation2"));
        String method2 = "GET";
        String httpLocation2 = "student/{stuId}";

        Pattern httpLocationPattern1 = WSDLUtil.getConstantFromHTTPLocationForResource(httpLocation1, method1);
        Pattern httpLocationPattern2 = WSDLUtil.getConstantFromHTTPLocationForResource(httpLocation2, method2);
        httpLocationTableForResource.put(httpLocationPattern1, operation1);
        httpLocationTableForResource.put(httpLocationPattern2, operation2);

        // set request endpoint
        mc.setTo(new EndpointReference("/services/StudentsService/student?stuId=101"));
        //set HTTP method
        mc.setProperty(HTTPConstants.HTTP_METHOD, method1);

        AxisService axisService = new AxisService();
        axisService.setName("StudentsService");
        AxisEndpoint endpoint = new AxisEndpoint();
        AxisBinding binding = new AxisBinding();
        // set the httpLocationTable
        binding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE_FOR_RESOURCE, httpLocationTableForResource);
        endpoint.setBinding(binding);
        mc.setProperty(WSDL2Constants.ENDPOINT_LOCAL_NAME, endpoint);
        mc.setAxisService(axisService);

        HTTPLocationBasedDispatcher dispatcher = new HTTPLocationBasedDispatcher();
        // find correct operation for endpoint and it should equal to operation1
        AxisOperation resultOp = dispatcher.findOperation(axisService, mc);
        Assert.assertEquals(operation1, resultOp);
    }
}
