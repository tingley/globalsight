package com.globalsight.cvsoperation.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

public class XMLParser {
	public static ArrayList parseLocales(String xml) throws DocumentException {
		if (xml == null || xml.trim().length() == 0)
			return null;
		ArrayList r = new ArrayList();
		ArrayList keys = new ArrayList();
		ArrayList values = new ArrayList();
		Document dc = DocumentHelper.parseText(xml);
		Element rootElement = dc.getRootElement();
		Element locale = null;
		for (Iterator i = rootElement.elementIterator("locale"); i.hasNext();) {
			locale = (Element) i.next();
			keys.add(locale.elementText("id"));
			values.add(locale.elementText("name"));
		}
		r.add(keys);
		r.add(values);
		return r;
	}
}
