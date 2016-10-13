package com.globalsight.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class StringXmlHelper
{

    public static List getAttribute(String xml, String attribute)
            throws Exception
    {
        List list = new ArrayList();

        Document document = new SAXReader().read(new ByteArrayInputStream(xml
                .getBytes("UTF-8")));
        document.getRootElement().normalize();
        Element rootElement = document.getRootElement();
        List nodeList = rootElement.elements();
        for (int i = 0; i < nodeList.size(); i++)
        {
            Element nodeItem = (Element) nodeList.get(i);
            if (nodeItem.getName().equals(attribute))
            {
                String value = nodeItem.getText();
                list.add(value);
            }

        }
        return list;
    }

}
