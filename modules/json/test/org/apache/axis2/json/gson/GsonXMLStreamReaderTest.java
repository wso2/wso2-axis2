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

package org.apache.axis2.json.gson;

import com.google.gson.stream.JsonReader;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GsonXMLStreamReaderTest {
    

    @Test
    public void testGsonXMLStreamReader() throws Exception {
        String jsonString = "{\"response\":{\"return\":{\"name\":\"kate\",\"age\":\"35\",\"gender\":\"female\"}}}";
        String xmlString = "<response xmlns=\"http://www.w3schools.com\"><return><name>kate</name><age>35</age><gender>female</gender></return></response>";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream , "UTF-8"));
        String fileName = "test-resources/custom_schema/testSchema_1.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);
        QName elementQName = new QName("http://www.w3schools.com", "response");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());
        GsonXMLStreamReader gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);
        gsonXMLStreamReader.initXmlStreamReader(elementQName , schemaList , configCtxt);
        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(gsonXMLStreamReader);
        OMElement omElement = stAXOMBuilder.getDocumentElement();
        String actual = omElement.toString();
        inputStream.close();
        is.close();
        Assert.assertEquals(xmlString , actual);

    }

    /**
     * This method tests GsonXMLStreamReader for a schema with different data types including binary.
     */
    @Test
    public void testGsonXMLStreamReaderWithDataTypes() throws Exception {
        String jsonString = "{\"response\":{\"return\":{\"name\":\"kate\",\"homes\":1,\"age\":23,\"height\":5.5,"
                + "\"image\":\"iVBORw0KGg\"}}}";
        String xmlString = "<response xmlns=\"http://www.w3schools.com\"><return><name>kate</name><homes>1</homes>"
                + "<age>23</age><height>5.5</height><image>iVBORw0KGg</image></return></response>";
        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
        String fileName = "test-resources/custom_schema/testSchema_3.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);
        QName elementQName = new QName("http://www.w3schools.com", "response");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());
        GsonXMLStreamReader gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);
        gsonXMLStreamReader.initXmlStreamReader(elementQName, schemaList, configCtxt);
        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(gsonXMLStreamReader);
        OMElement omElement = stAXOMBuilder.getDocumentElement();
        String actual = omElement.toString();
        inputStream.close();
        is.close();
        Assert.assertEquals(xmlString, actual);
    }

    /**
     * This method tests GsonXMLStreamReader without the request name ie: request method + request path  which is
     * required in the schema ( _postemployee ).
     */
    @Test
    public void testGsonXMLStreamReaderSingleRequest() throws Exception {

        String jsonString = "{ \"employee\":" +
                "{ \"employeenumber\":\"0001\"," +
                "\"firstname\":\"Steve\"," +
                "\"lastname\":\"Wilson\"," +
                "\"email\":\"wilson@Wso2.com\"," +
                "\"salary\":\"15000.00\"" +
                "}" +
                "}";

        String xmlString = "<_postemployee xmlns=\"http://www.w3schools.com\">" +
                "<employeenumber>0001</employeenumber>" +
                "<firstname>Steve</firstname>" +
                "<lastname>Wilson</lastname>" +
                "<email>wilson@Wso2.com</email>" +
                "<salary>15000.00</salary>" +
                "</_postemployee>";

        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String fileName = "test-resources/custom_schema/testSchema_4.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);
        QName elementQName = new QName("http://www.w3schools.com", "_postemployee");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());
        GsonXMLStreamReader gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);
        gsonXMLStreamReader.initXmlStreamReader(elementQName, schemaList, configCtxt);
        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(gsonXMLStreamReader);
        OMElement omElement = stAXOMBuilder.getDocumentElement();
        String actual = omElement.toString();
        inputStream.close();
        is.close();
        Assert.assertEquals(xmlString, actual);
    }

    /**
     * This method tests GsonXMLStreamReader without the request name ie: request method + request path which is
     *required in the schema ( _postemployee_batch_req , _postemployee ).
     */
    @Test
    public void testGsonXMLStreamReaderBatchRequest() throws Exception {

        String jsonString = "{\"employees\": " +
                "{\"employees\": [" +
                "{ \"employeenumber\": \"00021\"," +
                "\"firstname\": \"Will\"," +
                "\"lastname\": \"Smith\"," +
                "\"email\": \"will@smith.com\"," +
                "\"salary\": \"1500.00\"" +
                "}," +
                "{ \"employeenumber\": \"00021\"," +
                "\"firstname\": \"Will\"," +
                "\"lastname\": \"Smith\"," +
                "\"email\": \"will@smith.com\"," +
                "\"salary\": \"1500.00\"" +
                "}" +
                "]" +
                "}" +
                "}";

        String xmlString = "<_postemployee_batch_req xmlns=\"http://www.w3schools.com\">" +
                "<_postemployee>" +
                "<employeenumber>00021</employeenumber>" +
                "<firstname>Will</firstname>" +
                "<lastname>Smith</lastname>" +
                "<email>will@smith.com</email>" +
                "<salary>1500.00</salary>" +
                "</_postemployee>" +
                "<_postemployee>" +
                "<employeenumber>00021</employeenumber>" +
                "<firstname>Will</firstname>" +
                "<lastname>Smith</lastname>" +
                "<email>will@smith.com</email>" +
                "<salary>1500.00</salary>" +
                "</_postemployee>" +
                "</_postemployee_batch_req>";

        InputStream inputStream = new ByteArrayInputStream(jsonString.getBytes());
        JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String fileName = "test-resources/custom_schema/testSchema_5.xsd";
        InputStream is = new FileInputStream(fileName);
        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(new StreamSource(is), null);
        List<XmlSchema> schemaList = new ArrayList<XmlSchema>();
        schemaList.add(schema);
        QName elementQName = new QName("http://www.w3schools.com", "_postemployee_batch_req");
        ConfigurationContext configCtxt = new ConfigurationContext(new AxisConfiguration());
        GsonXMLStreamReader gsonXMLStreamReader = new GsonXMLStreamReader(jsonReader);
        gsonXMLStreamReader.initXmlStreamReader(elementQName, schemaList, configCtxt);
        StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(gsonXMLStreamReader);
        OMElement omElement = stAXOMBuilder.getDocumentElement();
        String actual = omElement.toString();
        inputStream.close();
        is.close();
        Assert.assertEquals(xmlString, actual);
    }
}
