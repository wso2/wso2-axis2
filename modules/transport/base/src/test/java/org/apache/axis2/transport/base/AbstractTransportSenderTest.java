/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.axis2.transport.base;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.transport.OutTransportInfo;

import java.util.ArrayList;

public class AbstractTransportSenderTest extends TestCase {
    public void testWaitForSynchronousResponse() {
        AbstractTransportSender abstractTransportSender = new AbstractTransportSender() {
            @Override
            public void sendMessage(MessageContext msgCtx, String targetEPR, OutTransportInfo outTransportInfo)
                    throws AxisFault {

            }
        };
        MessageContext messageContext = new MessageContext();
        AxisOperation axisOperation = new AxisOperation() {
            @Override
            public void addMessage(AxisMessage message, String label) {

            }

            @Override
            public void addMessageContext(MessageContext msgContext, OperationContext opContext) throws AxisFault {

            }

            @Override
            public void addFaultMessageContext(MessageContext msgContext, OperationContext opContext) throws AxisFault {

            }

            @Override
            public AxisMessage getMessage(String label) {
                return null;
            }

            @Override
            public ArrayList getPhasesInFaultFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesOutFaultFlow() {
                return null;
            }

            @Override
            public ArrayList getPhasesOutFlow() {
                return null;
            }

            @Override
            public ArrayList getRemainingPhasesInFlow() {
                return null;
            }

            @Override
            public void setPhasesInFaultFlow(ArrayList list) {

            }

            @Override
            public void setPhasesOutFaultFlow(ArrayList list) {

            }

            @Override
            public void setPhasesOutFlow(ArrayList list) {

            }

            @Override
            public void setRemainingPhasesInFlow(ArrayList list) {

            }

            @Override
            public OperationClient createClient(ServiceContext sc, Options options) {
                return null;
            }
        };
        ServiceContext serviceContext = new ServiceContext();
        axisOperation.setMessageExchangePattern("http://www.w3.org/ns/wsdl/in-out");
        OperationContext operationContext = new OperationContext(axisOperation, serviceContext);
        messageContext.setOperationContext(operationContext);
        assertTrue(abstractTransportSender.waitForSynchronousResponse(messageContext));
    }
}
