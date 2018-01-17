/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.axis2.transport.http.util;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.ApplicationXMLFormatter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Test case to validate whether the XML declaration is included when required for ApplicationXMLFormatter output
 */
public class XMLDeclarationTest extends TestCase {

    public void testWithoutDeclaration() throws Exception {
        ApplicationXMLFormatter xmlFormatter = new ApplicationXMLFormatter();
        MessageContext messageContext = new MessageContext();

        createPayload(messageContext);

        OMOutputFormat format = new OMOutputFormat();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        xmlFormatter.writeTo(messageContext, format, baos, true);
        String output = new String(baos.toByteArray());
        assertEquals("Compare payload without XML declaration", "<hello>world</hello>", output);
    }

    public void testWithDeclaration() throws Exception {
        ApplicationXMLFormatter xmlFormatter = new ApplicationXMLFormatter();
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty("WRITE_XML_DECLARATION", "true");

        createPayload(messageContext);

        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding(StandardCharsets.UTF_8.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        xmlFormatter.writeTo(messageContext, format, baos, true);
        String output = new String(baos.toByteArray());
        assertEquals("Compare payload with default XML declaration",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><hello>world</hello>", output);
    }

    public void testWithCustomEncodingDeclaration() throws Exception {
        ApplicationXMLFormatter xmlFormatter = new ApplicationXMLFormatter();
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty("WRITE_XML_DECLARATION", "true");
        messageContext.setProperty("XML_DECLARATION_ENCODING", StandardCharsets.ISO_8859_1.toString());

        createPayload(messageContext);

        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding(StandardCharsets.UTF_8.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        xmlFormatter.writeTo(messageContext, format, baos, true);
        String output = new String(baos.toByteArray());
        assertEquals("Compare payload with custom encoding XML declaration",
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?><hello>world</hello>", output);
    }

    public void testWithStandalone() throws Exception {
        ApplicationXMLFormatter xmlFormatter = new ApplicationXMLFormatter();
        MessageContext messageContext = new MessageContext();
        messageContext.setProperty("WRITE_XML_DECLARATION", "true");
        messageContext.setProperty("XML_DECLARATION_ENCODING", StandardCharsets.UTF_8.toString());
        messageContext.setProperty("XML_DECLARATION_STANDALONE", "yes");

        createPayload(messageContext);

        OMOutputFormat format = new OMOutputFormat();
        format.setCharSetEncoding(StandardCharsets.UTF_8.toString());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        xmlFormatter.writeTo(messageContext, format, baos, true);
        String output = new String(baos.toByteArray());
        assertEquals("Compare payload with XML declaration containing standalone attribute",
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><hello>world</hello>", output);
    }

    private void createPayload(MessageContext messageContext) throws AxisFault {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        OMElementImpl omHello = new OMElementImpl("hello", new OMNamespaceImpl("", ""), factory);
        omHello.addChild(new OMTextImpl("world", factory));
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        defaultEnvelope.getBody().addChild(omHello);
        messageContext.setEnvelope(defaultEnvelope);
    }
}
