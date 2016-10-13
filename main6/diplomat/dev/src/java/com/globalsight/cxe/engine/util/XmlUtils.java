/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.cxe.engine.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.globalsight.cxe.engine.eventflow.ParserException;

// TODO This class doesn't consider any special character, such as "<" and ">"
public class XmlUtils
{
    /**
     * if the elementName is an attr of rootElement,return the attr value else
     * if elementName is a direct childElement of rootElement, return
     * childElement value
     * 
     * recommended to use this method
     * 
     * @author shaucle
     */
    public static String getSimpleValue(Element p_rootElement,
            String p_elementName)
    {
        String value = getAttributeValue(p_rootElement, p_elementName);
        if (value == "" || value == null)
        {
            value = getDirectChildElementValue(p_rootElement, p_elementName);
        }
        return value;
    }

    public static String getDirectChildElementValue(Element p_rootElement,
            String p_elementName)
    {
        Element element = getDirectChildElement(p_rootElement, p_elementName);
        if (element == null)
            return null;
        return getElementValue(element);
    }

    public static List getDirectChildElementValues(Element p_rootElement,
            String p_elementName)
    {
        List result = new ArrayList();
        List elems = getChildElements(p_rootElement, p_elementName);
        for (Iterator itor = elems.iterator(); itor.hasNext();)
        {
            Element elem = (Element) itor.next();
            result.add(getElementValue(elem));
        }
        return result;
    }

    public static String getChildElementValue(Element p_rootElement,
            String p_elementName)
    {
        Element element = getChildElement(p_rootElement, p_elementName);
        if (element == null)
            return null;
        return getElementValue(element);
    }

    public static List getChildElementValues(Element p_rootElement,
            String p_elementName)
    {
        List result = new ArrayList();
        List elems = getChildElements(p_rootElement, p_elementName);
        for (Iterator itor = elems.iterator(); itor.hasNext();)
        {
            Element elem = (Element) itor.next();
            result.add(getElementValue(elem));
        }
        return result;
    }

    // ignore case, return the first direct child
    public static Element getDirectChildElement(Element p_rootElement,
            String p_elementName)
    {
        for (Node node = p_rootElement.getFirstChild(); node != null; node = node
                .getNextSibling())
        {
            if (node.getNodeName().equalsIgnoreCase(p_elementName))
                return (Element) node;
        }
        return null;
    }

    // return the first child
    public static Element getChildElement(Element p_rootElement,
            String p_elementName)
    {
        NodeList nodeList = p_rootElement.getElementsByTagName(p_elementName);
        // if (nodeList.getLength() >= 0)
        return (Element) nodeList.item(0);
        // return null;
    }

    // ignore case
    public static List getDirectChildElements(Element p_rootElement,
            String p_elementName)
    {
        List result = new ArrayList();
        for (Node node = p_rootElement.getFirstChild(); node != null; node = node
                .getNextSibling())
        {
            if (node.getNodeName().equalsIgnoreCase(p_elementName))
                result.add((Element) node);
        }
        return result;
    }

    public static List getChildElements(Element p_rootElement,
            String p_elementName)
    {
        List result = new ArrayList();
        NodeList nodeList = p_rootElement.getElementsByTagName(p_elementName);
        for (int i = 0; i < nodeList.getLength(); i++)
        {
            Element elem = (Element) nodeList.item(i);
            result.add(elem);
        }
        return result;
    }

    // =====================other helper method
    public static Element findRootElement(String p_xml)
    {
        try
        {
            StringReader reader = new StringReader(p_xml);
            InputSource source = new InputSource(reader);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false);
            parser.parse(source);

            Document document = parser.getDocument();
            return document.getDocumentElement();
        }
        catch (Exception e)
        {
            throw new ParserException(e);
        }
    }

    public static String getElementValue(Element p_element)
    {
        Node n = p_element.getFirstChild();
        if (n != null)
        {
            return n.getNodeValue();
        }
        return null;
    }

    public static String getAttributeValue(Element p_elem, String p_name)
    {
        return p_elem.getAttribute(p_name);
    }

    // =====================TODO do the same thing of dom4j?
    public static StringBuffer appendElementStart(StringBuffer p_buffer,
            String p_name)
    {
        appendElementStart(p_buffer, p_name, null);
        // appendLine(p_buffer);
        return p_buffer;
    }

    public static StringBuffer appendElementStart(StringBuffer p_buffer,
            String p_name, String[][] attributes)
    {
        appendElementStartHead(p_buffer, p_name, attributes);
        p_buffer.append(">");
        // appendLine(p_buffer);
        return p_buffer;
    }

    public static StringBuffer appendEmptyElement(StringBuffer p_buffer,
            String p_name, String[][] attributes)
    {
        appendElementStartHead(p_buffer, p_name, attributes);
        p_buffer.append("/>");
        return p_buffer;
    }

    private static StringBuffer appendElementStartHead(StringBuffer p_buffer,
            String p_name, String[][] attributes)
    {
        p_buffer.append("<");
        p_buffer.append(p_name);
        if (attributes != null)
        {
            for (int i = 0; i < attributes.length; i++)
            {
                appendAttribute(p_buffer, attributes[i][0], attributes[i][1]);
            }
        }
        return p_buffer;
    }

    public static StringBuffer appendElementEnd(StringBuffer p_buffer,
            String p_name)
    {
        p_buffer.append("</");
        p_buffer.append(p_name);
        p_buffer.append(">");
        // appendLine(p_buffer);
        return p_buffer;
    }

    public static StringBuffer appendElement(StringBuffer p_buffer,
            String p_name, String[][] attributes, String text)
    {
        if (text != null)
        {
            appendElementStart(p_buffer, p_name, attributes);
            p_buffer.append(text);
            appendElementEnd(p_buffer, p_name);
            appendLine(p_buffer);
        }
        return p_buffer;
    }

    public static StringBuffer appendElement(StringBuffer p_buffer,
            String p_name, String p_text)
    {
        appendElement(p_buffer, p_name, null, p_text);
        // appendLine(p_buffer);
        return p_buffer;
    }

    public static StringBuffer appendLine(StringBuffer sb)
    {
        sb.append("\n");
        return sb;
    }

    public static StringBuffer appendAttribute(StringBuffer p_buffer,
            String p_name, String p_value)
    {
        if (p_value != null)
        {
            p_buffer.append(" ");
            p_buffer.append(p_name);
            p_buffer.append("=\"");
            p_buffer.append(p_value);
            p_buffer.append("\"");
        }
        return p_buffer;
    }

    // =============head/foot(dtd) utils
    public static StringBuffer appendHead(StringBuffer sb)
    {
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        return sb;
    }
}
