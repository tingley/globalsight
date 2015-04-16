package com.adobe.cq.sling;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class XmlHelperTest {

	private File inputXml;

	public XmlHelperTest(File inputXml) {
		this.inputXml = inputXml;
	}

	public Document getDocument() {
		SAXReader saxReader = new SAXReader();
		Document document = null;
		try {
			document = saxReader.read(inputXml);
		} catch (DocumentException e) {
			// log.error("Error happened getDocument():", e);
		}
		return document;
	}

	public Element getRootElement() {
		return getDocument().getRootElement();
	}

	public TranslationFile traversalDocument() {
		if (inputXml == null) {
			return null;
		}

		Element root = this.getRootElement();
		TranslationFile translationFile = new TranslationFile();

		// Root Attributes --- TranslationFile
		for (Iterator tfIt = root.attributeIterator(); tfIt.hasNext();) {
			Attribute tfAttribute = (Attribute) tfIt.next();
			System.out.println("TranslationFile Attribute: "
					+ tfAttribute.getName() + "=" + tfAttribute.getValue());

			if (tfAttribute.getName().equals(XmlConstants.GSDescription)) {
				translationFile.setGSDescription(tfAttribute.getValue());
			} else if (tfAttribute.getName().equals(
					XmlConstants.GSSourcelanguage)) {
				translationFile.setGSSourceLanguage(tfAttribute.getValue());
			} else if (tfAttribute.getName().equals(
					XmlConstants.GSTargetLanguage)) {
				translationFile.setGSTargetLanguage(tfAttribute.getValue());
			}
		}

		// child of Root ---- GSPageTranslations
		for (Iterator ptsIt = root.elementIterator(); ptsIt.hasNext();) {
			Element ptsElement = (Element) ptsIt.next();
			// GSPageTranslation pageTranslation = new GSPageTranslation();
			// translationFile.getGSPageTranslationList().add(pageTranslation);

			// child of GSPageTranslations ---- GSPageTranslation
			for (Iterator ptIt = ptsElement.elementIterator(); ptIt.hasNext();) {
				Element ptElement = (Element) ptIt.next();// GSPageTranslation

				GSPageTranslation pageTranslation = new GSPageTranslation();
				translationFile.getGSPageTranslationList().add(pageTranslation);

				for (Iterator ptIA = ptElement.attributeIterator(); ptIA
						.hasNext();) {
					Attribute ptAttribute = (Attribute) ptIA.next();
					System.out.println("GSPageTranslation Attribute: "
							+ ptAttribute.getName() + "="
							+ ptAttribute.getValue());

					if (ptAttribute.getName().equals(XmlConstants.PagePath)) {
						pageTranslation.setPagePath(ptAttribute.getValue());
					}
				}

				// start
				// child of GSPageTranslation --- PropertyContents
				for (Iterator pcsIt = ptElement.elementIterator(); pcsIt
						.hasNext();) {
					System.out.println("--------------------------------");
					Element pcsElement = (Element) pcsIt.next();

					// Child of PropertyContents --- PropertyContent
					for (Iterator pcIt = pcsElement.elementIterator(); pcIt.hasNext();) {
						Element pcElement = (Element) pcIt.next(); // PropertyContent
						PropertyContent propertyContent = new PropertyContent();
						pageTranslation.getPropertyContentList().add(propertyContent);

						// Attribute PropertyContent
						for (Iterator pcIA = pcElement.attributeIterator(); pcIA.hasNext();) {
							Attribute pcAttribute = (Attribute) pcIA.next();
							System.out.println("PropertyContent Attribute: "
									+ pcAttribute.getName() + "="
									+ pcAttribute.getValue());

							if (pcAttribute.getName().equals(
									XmlConstants.PropertyName)) {
								propertyContent.setPropertyName(pcAttribute
										.getValue());
							} else if (pcAttribute.getName().equals(
									XmlConstants.NodePath)) {
								propertyContent.setNodePath(pcAttribute
										.getValue());
							}

						}
						
						System.out.println("PropertyContent Text: "
								+ pcElement.getText());
						propertyContent.setText(pcElement.getText());

					}
				}

			}

		}

		return translationFile;
	}

	public static void main(String[] args) {
		TranslationFile tf = new XmlHelperTest(new File(
				"E:\\FilesOutput\\source_20150205191246_zh_CN.xml"))
				.traversalDocument();
		System.out.println(tf.getGSPageTranslationList().size());
	}
}
