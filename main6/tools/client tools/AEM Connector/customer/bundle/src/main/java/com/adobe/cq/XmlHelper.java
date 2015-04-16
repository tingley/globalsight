package com.adobe.cq;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlHelper {
	private final Logger log = LoggerFactory.getLogger(super.getClass());
	private File sourceFile;
	private File targetFile;
	private OutputFormat format = OutputFormat.createPrettyPrint();
	
	public XmlHelper(File sourceFile) {
		this.sourceFile = sourceFile;
		format.setEncoding("UTF-8");
	}

	public XmlHelper(File sourceFile, File targetFile) {
		this.sourceFile = sourceFile;
		this.targetFile = targetFile;
		format.setEncoding("UTF-8");
	}
	
	public void modifySourceFileTargetLanguage(String target) {
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(this.sourceFile);
			List list = document
					.selectNodes("/TranslationFile/@GSTargetLanguage");
			Iterator iter = list.iterator();

			while (iter.hasNext()) {
				Attribute attribute = (Attribute) iter.next();
				attribute.setValue(target);
			}

			XMLWriter writer = new XMLWriter(new FileOutputStream(this.sourceFile), format);
			writer.write(document);
			writer.close();
		} catch (Exception ex) {
			this.log.error("Error: ", ex);
		}
	}

	public void modifySourceFileSourceLanguage(String source) {
		try {
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(this.sourceFile);
			List list = document
					.selectNodes("/TranslationFile/@GSSourceLanguage");
			Iterator iter = list.iterator();

			while (iter.hasNext()) {
				Attribute attribute = (Attribute) iter.next();
				attribute.setValue(source);
			}

			XMLWriter writer = new XMLWriter(new FileOutputStream(this.sourceFile), format);
			writer.write(document);
			writer.close();
		} catch (Exception ex) {
			this.log.error("Error: ", ex);
		}
	}

//	private static byte[] InputStreamToByte(InputStream is) throws IOException {
//		ByteArrayOutputStream byteArrOut = new ByteArrayOutputStream();
//		byte[] temp = new byte[1024];
//		int len = 0;
//		while ((len = is.read(temp, 0, 1024)) != -1) {
//			byteArrOut.write(temp, 0, len);
//		}
//		byteArrOut.flush();
//		byte[] bytes = byteArrOut.toByteArray();
//		return bytes;
//	}
}