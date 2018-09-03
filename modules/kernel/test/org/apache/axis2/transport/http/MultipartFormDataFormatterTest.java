/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.axis2.transport.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.DiskFileDataSource;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.TransportUtils;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import org.junit.Assert;
import java.nio.charset.StandardCharsets;

public class MultipartFormDataFormatterTest extends AbstractTestCase {

    private MessageContext messageContext;
    private OMElement rootElement;

    public MultipartFormDataFormatterTest(String testName) {
        super(testName);
    }

    public void setUp() throws AxisFault {
        // Dummy OMElement
        OMFactory factory = OMAbstractFactory.getOMFactory();
        rootElement = factory.createOMElement("mediate", null);
        OMElement child = factory.createOMElement("file", null);
        OMAttribute childAttribute = factory.createOMAttribute("filename", null, "brs-worker.json");
        factory.createOMText(child, "ewogICAgInR5cGUiOiAid3NvMmJycy0yMTAtd29ya2VyIiwKfQo=");
        child.addAttribute(childAttribute);
        rootElement.addChild(child);
        AxisConfiguration er = new AxisConfiguration();
        AxisService servicesDesc = new AxisService();
        servicesDesc.setName("testService");
        er.addService(servicesDesc);
        ConfigurationContext engineContext = new ConfigurationContext(er);
        messageContext = engineContext.createMessageContext();
        SOAPEnvelope soapEnvelope = TransportUtils.createSOAPEnvelope(rootElement);
        messageContext.setEnvelope(soapEnvelope);
    }

    /**
     * test multipart payload being decoded when DECODE_MULTIPART_DATA is true
     * @throws AxisFault
     */
    public void testcreateMultipatFormDataRequestWhenDecodeMultipartDataIsTrue() throws AxisFault {
        // add message context property DECODE_MULTIPART_DATA
        messageContext.setProperty("DECODE_MULTIPART_DATA", new Boolean(true));
        MultipartFormDataFormatter multipartFormDataFormatter = new MultipartFormDataFormatter();
        OMOutputFormat omOutputFormat = new OMOutputFormat();
        omOutputFormat.setCharSetEncoding("UTF-8");
        omOutputFormat.setMimeBoundary("MIMEBoundary_b00a5f9f83314755ecf2373baedb9a48fcaaea4e7e01b4bf");
        String payload = new String(multipartFormDataFormatter.getBytes(messageContext, omOutputFormat),
                StandardCharsets.UTF_8);
        Assert.assertTrue("The decoded response should contain 'wso2brs-210-worker' text", payload.contains
                ("wso2brs-210-worker"));
    }

    /**
     * test multipart payload being un-decoded when DECODE_MULTIPART_DATA is false
     * @throws AxisFault
     */
    public void testcreateMultipatFormDataRequestWhenDecodeMultipartDataIsFalse() throws AxisFault {
        // add message context property DECODE_MULTIPART_DATA
        messageContext.setProperty("DECODE_MULTIPART_DATA", new Boolean(false));
        MultipartFormDataFormatter multipartFormDataFormatter = new MultipartFormDataFormatter();
        OMOutputFormat omOutputFormat = new OMOutputFormat();
        omOutputFormat.setCharSetEncoding("UTF-8");
        omOutputFormat.setMimeBoundary("MIMEBoundary_b00a5f9f83314755ecf2373baedb9a48fcaaea4e7e01b4bf");
        String payload = new String(multipartFormDataFormatter.getBytes(messageContext, omOutputFormat),
                StandardCharsets.UTF_8);
        Assert.assertFalse("The encoded response should not contain 'wso2brs-210-worker' text",
                payload.contains("wso2brs-210-worker"));
    }

    public void testTransportHeadersOfFileAttachments() throws Exception {
        String FILE_NAME = "binaryFile.xml";
        String FIELD_NAME = "fileData";
        String CONTENT_TYPE_VALUE = "text/xml";
        String MEDIATE_ELEMENT = "mediate";
        String FILE_KEY = "fileAttachment";
        String CONTENT_DISPOSITION = "Content-Disposition";
        String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
        String CONTENT_TYPE = "Content-Type";
        String FORM_DATA_ELEMENT = "form-data";
        String NAME_ELEMENT = "name";
        String FILE_NAME_ELEMENT = "filename";
        String CONTENT_TRANSFER_ENCODING_VALUE = "binary";

        MultipartFormDataFormatter formatter = new MultipartFormDataFormatter();
        File binaryAttachment = getTestResourceFile(FILE_NAME);

        DiskFileItem diskFileItem = null;
        InputStream input = null;
        try {
            if (binaryAttachment.exists() && !binaryAttachment.isDirectory()) {
                diskFileItem = (DiskFileItem) new DiskFileItemFactory().createItem(FIELD_NAME, CONTENT_TYPE_VALUE, true,
                        binaryAttachment.getName());
                input = new FileInputStream(binaryAttachment);
                OutputStream os = diskFileItem.getOutputStream();
                int ret = input.read();
                while (ret != -1) {
                    os.write(ret);
                    ret = input.read();
                }
                os.flush();
            }

        } finally {
            input.close();
        }

        DataSource dataSource = new DiskFileDataSource(diskFileItem);
        DataHandler dataHandler = new DataHandler(dataSource);

        SOAPFactory soapFactory = OMAbstractFactory.getSOAP12Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        SOAPBody body = soapEnvelope.getBody();
        OMElement bodyFirstChild = soapFactory.createOMElement(new QName(MEDIATE_ELEMENT), body);
        OMText binaryNode = soapFactory.createOMText(dataHandler, true);
        soapFactory.createOMElement(FILE_KEY, null, bodyFirstChild).addChild(binaryNode);
        messageContext.setEnvelope(soapEnvelope);

        OMOutputFormat format = new OMOutputFormat();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        formatter.writeTo(messageContext, format, baos, true);

        MimeMultipart mp = new MimeMultipart(new ByteArrayDataSource(baos.toByteArray(), format.getContentType()));
        BodyPart bp = mp.getBodyPart(0);
        String contentDispositionValue = FORM_DATA_ELEMENT + "; " + NAME_ELEMENT + "=\"" + FILE_KEY + "\"; "
                + FILE_NAME_ELEMENT + "=\"" + binaryAttachment.getName() + "\"";
        String contentTypeValue = bp.getHeader(CONTENT_TYPE)[0].split(";")[0];
        assertEquals(contentDispositionValue, bp.getHeader(CONTENT_DISPOSITION)[0]);
        assertEquals(CONTENT_TYPE_VALUE, contentTypeValue);
        assertEquals(CONTENT_TRANSFER_ENCODING_VALUE, bp.getHeader(CONTENT_TRANSFER_ENCODING)[0]);
    }

    public void tearDown () {
        messageContext = null;
    }
}
