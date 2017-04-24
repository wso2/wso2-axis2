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
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

public class MultipartFormDataFormatterTest extends TestCase {
    public void testFileDataFields() throws Exception {
        MultipartFormDataFormatter formatter = new MultipartFormDataFormatter();
        MessageContext mc = new MessageContext();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope defaultEnvelope = factory.getDefaultEnvelope();
        /*
              <root xmlns="">
                  <customFieldOne>$1</customFieldOne>
                  <customFieldTwo>$2</customFieldTwo>
                  <file xmlns="http://ws.apache.org/ns/synapse/form-data"
                        name="fieldname"
                        filename="sample-file.txt"
                        content-type="text/plain"
                        charset="US-ASCII">$3</file>
               </root>
         */

        OMElement rootElement = factory.createOMElement("root", null);
        OMElement customFieldOne = factory.createOMElement("customFieldOne", null);
        customFieldOne.setText("ValueOne");
        rootElement.addChild(customFieldOne);
        OMElement customFieldTwo = factory.createOMElement("customFieldTwo", null);
        customFieldTwo.setText("ValueTwo");
        rootElement.addChild(customFieldTwo);
        OMElement fileElement = factory.createOMElement("file", factory.createOMNamespace("http://org.apache.axis2/xsd/form-data",
                                                                                   null));
        fileElement.addAttribute("filename", "sample-file.txt", null);
        fileElement.addAttribute("content-type", "application/xml", null);
        fileElement.addAttribute("charset", "UTF-8", null);
        String fileContent = "<Test content/>";
        fileElement.setText(new String(Base64.encodeBase64(fileContent.getBytes())));

        rootElement.addChild(fileElement);
        defaultEnvelope.getBody().addChild(rootElement);

        mc.setEnvelope(defaultEnvelope);

        OMOutputFormat format = new OMOutputFormat();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.writeTo(mc, format, baos, true);

        MimeMultipart mp = new MimeMultipart(new ByteArrayDataSource(baos.toByteArray(), format.getContentType()));

        assertEquals(3, mp.getCount());
        BodyPart bodyPartOne = mp.getBodyPart(0);
        assertEquals(null, bodyPartOne.getFileName());

        BodyPart bodyPartThree = mp.getBodyPart(2);
        assertEquals("application/xml; charset=UTF-8", bodyPartThree.getContentType());
        assertEquals("sample-file.txt", bodyPartThree.getFileName());

        ByteArrayInputStream contentStream = (ByteArrayInputStream) bodyPartThree.getContent();
        int n = contentStream.available();
        byte[] bytes = new byte[n];
        contentStream.read(bytes, 0, n);
        String content = new String((bytes));

        assertEquals(fileContent, content);
    }
}
