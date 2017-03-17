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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.om.OMText;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AbstractTestCase;
import org.apache.axis2.builder.DiskFileDataSource;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

public class MultipartFormDataFormatterTest extends AbstractTestCase {

	public MultipartFormDataFormatterTest(String testName) {
		super(testName);
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

		MessageContext mc = new MessageContext();

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
		mc.setEnvelope(soapEnvelope);

		OMOutputFormat format = new OMOutputFormat();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		formatter.writeTo(mc, format, baos, true);

		MimeMultipart mp = new MimeMultipart(new ByteArrayDataSource(baos.toByteArray(), format.getContentType()));
		BodyPart bp = mp.getBodyPart(0);
		String contentDispositionValue = FORM_DATA_ELEMENT + "; " + NAME_ELEMENT + "=\"" + FILE_KEY + "\"; "
				+ FILE_NAME_ELEMENT + "=\"" + binaryAttachment.getName() + "\"";
		String contentTypeValue = bp.getHeader(CONTENT_TYPE)[0].split(";")[0];
		assertEquals(contentDispositionValue, bp.getHeader(CONTENT_DISPOSITION)[0]);
		assertEquals(CONTENT_TYPE_VALUE, contentTypeValue);
		assertEquals(CONTENT_TRANSFER_ENCODING_VALUE, bp.getHeader(CONTENT_TRANSFER_ENCODING)[0]);
	}
}
