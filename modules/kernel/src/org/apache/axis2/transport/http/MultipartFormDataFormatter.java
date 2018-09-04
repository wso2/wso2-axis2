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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.DiskFileDataSource;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.util.ComplexPart;
import org.apache.axis2.transport.http.util.URLTemplatingUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

/**
 * Formates the request message as multipart/form-data. An example of this serialization is shown
 * below which was extracted from the Web Services Description Language (WSDL) Version 2.0 Part 2: Adjuncts
 * <p/>
 * The following instance data of an input message:
 * <p/>
 * <data>
 * <town>
 * <name>Fréjus</name>
 * <country>France</country>
 * </town>
 * <date>@@@@-@@-@@</date>
 * </data>
 * <p/>
 * with the following operation element
 * <p/>
 * <operation ref='t:data'
 * whttp:location='temperature'
 * whttp:method='POST'
 * whttp:inputSerialization='multipart/form-data'/>
 * <p/>
 * will serialize the message as follow:
 * <p/>
 * Content-Type: multipart/form-data; boundary=AaB03x
 * Content-Length: xxx
 * <p/>
 * --AaB03x
 * Content-Disposition: form-data; name="town"
 * Content-Type: application/xml
 * <p/>
 * <town>
 * <name>Fréjus</name>
 * <country>France</country>
 * </town>
 * --AaB03x
 * Content-Disposition: form-data; name="date"
 * Content-Type: text/plain; charset=utf-8
 *
 * @@@@-@@-@@ --AaB03x--
 */
public class MultipartFormDataFormatter implements MessageFormatter {

    /**
     * QName of the reserved XML node for file fields
     */
    private static final QName FILE_FIELD_QNAME = new QName("http://org.apache.axis2/xsd/form-data", "file");

    /**
     * QName of the reserved XML attribute for filename
     */
    private static final QName FILENAME_ATTRIBUTE_QNAME = new QName("filename");

    /**
     * QName of the reserved XML attribute for filename
     */
    private static final QName FILE_FIELD_NAME_ATTRIBUTE_QNAME = new QName("name");

    /**
     * QName of the reserved XML attribute for content-type
     */
    private static final QName CONTENT_TYPE_ATTRIBUTE_QNAME = new QName("content-type");

    /**
     * QName of the reserved XML attribute for charset
     */
    private static final QName CHARSET_ATTRIBUTE_QNAME = new QName("charset");

    /**
     * Default filename used in file fields
     */
    private static final String DEFAULT_FILE_NAME = "esb-generated-file";

    /**
     * Default field name used in file fields
     */
    private static final String DEFAULT_FILE_FIELD_NAME = "file";

    /**
     * Default content-type used in the file fields
     */
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    /**
     * Default charset used in file fields
     */
    private static final String DEFAULT_CHARSET = "ISO-8859-1";

    /**
     * Flag to decide if multipart data should be decoded/not decoded before sending out
     */
    private static final String DECODE_MULTIPART_DATA_PARAM = "DECODE_MULTIPART_DATA";

    /**
     * @return a byte array of the message formatted according to the given
     *         message format.
     */
    public byte[] getBytes(MessageContext messageContext, OMOutputFormat format) throws AxisFault {

        OMElement omElement = messageContext.getEnvelope().getBody().getFirstElement();

        Part[] parts = createMultipatFormDataRequest(messageContext, omElement);
        if (parts.length > 0) {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            try {

                // This is accessing a class of Commons-FlieUpload
                Part.sendParts(bytesOut, parts, format.getMimeBoundary().getBytes());
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
            return bytesOut.toByteArray();
        }

        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * To support deffered writing transports as in http chunking.. Axis2 was
     * doing this for some time..
     * <p/>
     * Preserve flag can be used to preserve the envelope for later use. This is
     * usefull when implementing authentication machnisms like NTLM.
     *
     * @param outputStream
     * @param preserve     :
     *                     do not consume the OM when this is set..
     */
    public void writeTo(MessageContext messageContext, OMOutputFormat format,
                        OutputStream outputStream, boolean preserve) throws AxisFault {

        try {
            byte[] b = getBytes(messageContext, format);

            if (b != null && b.length > 0) {
                outputStream.write(b);
            } else {
                outputStream.flush();
            }
        } catch (IOException e) {
            throw new AxisFault("An error occured while writing the request");
        }
    }

    /**
     * Different message formats can set their own content types
     * Eg: JSONFormatter can set the content type as application/json
     *
     * @param messageContext
     * @param format
     * @param soapAction
     */
    public String getContentType(MessageContext messageContext, OMOutputFormat format,
                                 String soapAction) {

        String contentType = HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA;
        String encoding = format.getCharSetEncoding();
        if (encoding != null) {
            contentType += "; charset=" + encoding;
        }
        contentType = contentType + "; " + "boundary=" + format.getMimeBoundary();
        return contentType;
    }

    /**
     * Some message formats may want to alter the target url.
     *
     * @return the target URL
     */
    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat format, URL targetURL)
            throws AxisFault {
        // Check whether there is a template in the URL, if so we have to replace then with data
        // values and create a new target URL.
        targetURL = URLTemplatingUtil.getTemplatedURL(targetURL, messageContext, false);

        return targetURL;
    }

    /**
     * @return this only if you want set a transport header for SOAP Action
     */
    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat format,
                                   String soapAction) {
        return soapAction;
    }

    /**
     * @param dataOut
     * @return
     */
    private Part[] createMultipatFormDataRequest(MessageContext messageContext, OMElement dataOut) {
        List<Part> parts = new ArrayList<Part>();
        if (dataOut != null) {
            Iterator<?> iter1 = dataOut.getChildElements();
            OMFactory omFactory = OMAbstractFactory.getOMFactory();
            while (iter1.hasNext()) {
                Part part = null;
                OMElement ele = (OMElement) iter1.next();
                Iterator<?> iter2 = ele.getChildElements();
                // Checks whether the element is a complex type
                if (iter2.hasNext()) {
                    OMElement omElement = omFactory.createOMElement(ele.getQName().getLocalPart(), null);
                    omElement.addChild(processComplexType(omElement, ele.getChildElements(), omFactory, false));
                    part = new ComplexPart(ele.getQName().getLocalPart(), omElement.toString());
                } else if (FILE_FIELD_QNAME.equals(ele.getQName())) {

                    String fieldName = getAttributeValue(ele.getAttribute(FILE_FIELD_NAME_ATTRIBUTE_QNAME),
                            DEFAULT_FILE_FIELD_NAME);
                    String filename = getAttributeValue(ele.getAttribute(FILENAME_ATTRIBUTE_QNAME), DEFAULT_FILE_NAME);
                    String contentType = getAttributeValue(ele.getAttribute(CONTENT_TYPE_ATTRIBUTE_QNAME),
                            DEFAULT_CONTENT_TYPE);
                    String charset = getAttributeValue(ele.getAttribute(CHARSET_ATTRIBUTE_QNAME), DEFAULT_CHARSET);

                    part = new FilePart(fieldName,
                            new ByteArrayPartSource(filename, Base64.decodeBase64(ele.getText().getBytes())),
                            contentType, charset);
                } else if ((ele.getAttribute(FILENAME_ATTRIBUTE_QNAME) != null)) {
                    String fieldName
                            = getAttributeValue(ele.getAttribute(FILE_FIELD_NAME_ATTRIBUTE_QNAME), DEFAULT_FILE_FIELD_NAME);
                    String filename
                            = getAttributeValue(ele.getAttribute(FILENAME_ATTRIBUTE_QNAME), DEFAULT_FILE_NAME);
                    String contentType
                            = getAttributeValue(ele.getAttribute(CONTENT_TYPE_ATTRIBUTE_QNAME),
                            DEFAULT_CONTENT_TYPE);
                    String charset
                            = getAttributeValue(ele.getAttribute(CHARSET_ATTRIBUTE_QNAME), DEFAULT_CHARSET);
                    Boolean decodeMultipartData = (Boolean) messageContext.getProperty(DECODE_MULTIPART_DATA_PARAM);
                    if (decodeMultipartData != null && decodeMultipartData) {
                        part = new FilePart(fieldName,
                                new ByteArrayPartSource(filename, Base64.decodeBase64(ele.getText().getBytes())),
                                contentType, charset);
                    } else {
                        part = new StringPart(fieldName, ele.getText(), charset);
                    }
                } else {
                    // Gets the first child object
                    OMTextImpl firstChild = (OMTextImpl) ele.getFirstOMChild();
                    // Checks whether the first object is a binary
                    if (firstChild.isBinary()) {
                        if (firstChild.getDataHandler() != null) {
                            if (((DataHandler) firstChild.getDataHandler())
                                    .getDataSource() instanceof DiskFileDataSource) {
                                String fileName = null;
                                String contentType = null;
                                // Gets the disk file data source
                                DiskFileDataSource fileDataSource = (DiskFileDataSource) ((DataHandler) firstChild
                                        .getDataHandler()).getDataSource();
                                // Gets the filename and content type
                                fileName = fileDataSource.getName();
                                contentType = fileDataSource.getContentType();
                                // Creates the FilePart
                                part = new FilePart(ele.getQName().getLocalPart(),
                                        new ByteArrayPartSource(fileName, ele.getText().getBytes()), contentType, null);
                            }
                        }
                    }
                    if (part == null) {
                        part = new StringPart(ele.getQName().getLocalPart(), ele.getText());
                    }
                }
                parts.add(part);
            }
        }
        Part[] partsArray = new Part[parts.size()];
        return parts.toArray(partsArray);
    }

    private String getAttributeValue(OMAttribute filenameAttribute, String defaultValue) {
        String filename = defaultValue;

        if (filenameAttribute != null) {
            filename = filenameAttribute.getAttributeValue();
        }

        return filename;
    }

    /**
     * @param parent
     * @param iter
     * @param omFactory
     * @return
     */
    private OMElement processComplexType(OMElement parent, Iterator iter, OMFactory omFactory, boolean returnParent) {
        OMElement omElement = null;
        while (iter.hasNext()) {
            OMElement ele = (OMElement) iter.next();
            omElement = omFactory.createOMElement(ele.getQName().getLocalPart(), null);
            Iterator iter2 = ele.getChildElements();
            if (iter2.hasNext()) {
                parent.addChild(processComplexType(omElement, ele.getChildElements(), omFactory, true));
            } else {
                omElement = omFactory.createOMElement(ele.getQName().getLocalPart(), null);
                omElement.setText(ele.getText());
                parent.addChild(omElement);
            }
        }
        if (returnParent) {
            return parent;
        } else {
            return omElement;
        }
    }
}
