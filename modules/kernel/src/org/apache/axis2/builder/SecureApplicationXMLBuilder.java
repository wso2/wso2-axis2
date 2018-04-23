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
import org.apache.axiom.om.impl.OMNodeEx;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

// Based on ApplicationXMLBuilder r559336
public class SecureApplicationXMLBuilder implements Builder {
    private final XMLInputFactory inputFactory;
    
    public SecureApplicationXMLBuilder() {
        // Older versions of Axiom don't allow to apply custom settings to XMLInputFactory;
        // we need to create our own one.
        inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    }
    
    /**
     * @return Returns the document element.
     */
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext)
            throws AxisFault {
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        if (inputStream != null) {
            try {
                PushbackInputStream pushbackInputStream = new PushbackInputStream(inputStream);
                int b;
                if ((b = pushbackInputStream.read()) > 0) {
                    pushbackInputStream.unread(b);
                    XMLStreamReader xmlreader = new DisallowDoctypeDeclStreamReaderWrapper(
                            inputFactory.createXMLStreamReader(pushbackInputStream,
                                    (String) messageContext.getProperty(
                                            Constants.Configuration.CHARACTER_SET_ENCODING)));
                    StAXBuilder builder = new StAXOMBuilder(xmlreader);
                    OMNodeEx documentElement = (OMNodeEx) builder.getDocumentElement();
                    documentElement.setParent(null);
                    SOAPBody body = soapEnvelope.getBody();
                    body.addChild(documentElement);
                }

            } catch (XMLStreamException e) {
                throw AxisFault.makeFault(e);
            } catch (IOException e) {
                throw AxisFault.makeFault(e);
            }
        }
        return soapEnvelope;
    }
}
