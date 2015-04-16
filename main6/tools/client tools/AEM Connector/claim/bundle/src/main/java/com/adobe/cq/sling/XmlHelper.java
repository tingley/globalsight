package com.adobe.cq.sling;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlHelper {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private File inputXml;

	public XmlHelper(File inputXml) {
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
			log.info("TranslationFile Attribute: " + tfAttribute.getName()
					+ "=" + tfAttribute.getValue());

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
					log.info("GSPageTranslation Attribute: "
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
					log.info("--------------------------------");
					Element pcsElement = (Element) pcsIt.next();

					// Child of PropertyContents --- PropertyContent
					for (Iterator pcIt = pcsElement.elementIterator(); pcIt
							.hasNext();) {
						Element pcElement = (Element) pcIt.next(); // PropertyContent
						PropertyContent propertyContent = new PropertyContent();
						pageTranslation.getPropertyContentList().add(
								propertyContent);

						// Attribute PropertyContent
						for (Iterator pcIA = pcElement.attributeIterator(); pcIA
								.hasNext();) {
							Attribute pcAttribute = (Attribute) pcIA.next();
							log.info("PropertyContent Attribute: "
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
							} else if (pcAttribute.getName().equals(XmlConstants.RefPath)){
								propertyContent.setRefPath(pcAttribute.getValue());
							}

						}

						log.info("PropertyContent Text: " + pcElement.getText());
						propertyContent.setText(pcElement.getText());

					}
				}

			}

		}

		return translationFile;
	}

}
