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

package org.apache.axis2.builder;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.axiom.om.util.DetachableInputStream;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class SOAPBuilder implements Builder {

    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        SOAPFactory soapFactory = null;
        SOAPEnvelope envelope = null;
        try {
            String charSetEncoding = (String) messageContext
                    .getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            
            // Apply a detachable inputstream.  This can be used later
            // to (a) get the length of the incoming message or (b)
            // free transport resources.
            DetachableInputStream is = new DetachableInputStream(inputStream);
            messageContext.setProperty(Constants.DETACHABLE_INPUT_STREAM, is);
            
            // Get the actual encoding by looking at the BOM of the InputStream
            PushbackInputStream pis = BuilderUtil.getPushbackInputStream(is);
            int bytesRead = pis.read();
            if (bytesRead != -1) {
                pis.unread(bytesRead);
                String actualCharSetEncoding = BuilderUtil.getCharSetEncoding(pis, charSetEncoding);

                // createSOAPModelBuilder takes care of configuring the underlying parser to
                // avoid the security issue described in CVE-2010-1632
                OMXMLParserWrapper builder = OMXMLBuilderFactory.createSOAPModelBuilder(pis,
                        actualCharSetEncoding);
                envelope = (SOAPEnvelope) builder.getDocumentElement();
                BuilderUtil
                        .validateSOAPVersion(BuilderUtil.getEnvelopeNamespace(contentType), envelope);
                BuilderUtil.validateCharSetEncoding(charSetEncoding, builder.getDocument()
                        .getCharsetEncoding(), envelope.getNamespace().getNamespaceURI());

            } else {
                if (contentType != null) {
                    if (contentType.indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                        soapFactory = OMAbstractFactory.getSOAP12Factory();
                    } else if (contentType
                            .indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE) > -1 || isRESTRequest(contentType)) {
                        soapFactory = OMAbstractFactory.getSOAP11Factory();
                    }
                }
                if (soapFactory != null) {
                    envelope = soapFactory.getDefaultEnvelope();
                }
            }
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }

        return envelope;
    }

    public boolean isRESTRequest(String contentType) {
        return contentType != null &&
                (contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_XML) > -1 ||
                        contentType.indexOf(HTTPConstants.MEDIA_TYPE_X_WWW_FORM) > -1 ||
                        contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1 ||
                        contentType.indexOf(HTTPConstants.MEDIA_TYPE_APPLICATION_JSON) > -1);
    }
}
