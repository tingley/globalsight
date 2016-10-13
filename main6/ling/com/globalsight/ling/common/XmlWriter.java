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
package com.globalsight.ling.common;

import java.util.Stack;
import java.util.Properties;
import java.util.Enumeration;

public class XmlWriter
{
    private StringBuffer xml = null;
    private XmlEntities xmlEntities = new XmlEntities();
    private Stack elementStack = new Stack();

    private int indentLevel = 0;
    private int indentWidth = 2;
    private boolean pretty = true;

    public XmlWriter()
    {
        xml = new StringBuffer();
    }

    public XmlWriter(boolean pretty)
    {
        xml = new StringBuffer();
        this.pretty = pretty;
    }

    public XmlWriter(int bufferSize, int indentWidth)
    {
        xml = new StringBuffer(bufferSize);
        this.indentWidth = indentWidth;
    }


    public void setIndentLevel(int indentLevel)
    {
        this.indentLevel = indentLevel;
    }


    public void element(String elem, Properties attribs,
      String content, boolean escapeContent)
    {
        if (pretty)
        {
            xml.append("\n");
        }
        xml.append(getIndent());
        writeStartElement(elem, attribs, false);
        xml.append(escapeContent ?
          xmlEntities.encodeStringBasic(content) : content);
        writeEndElement(elem);
    }


    public void startElement(String elem, Properties attribs)
    {
        // Store the element name
        elementStack.push(elem);

        if (pretty)
        {
            xml.append("\n");
        }
        xml.append(getIndent());
        writeStartElement(elem, attribs, false);
        indentLevel++;
    }


    public void endElement()
    {
        indentLevel--;
        if (pretty)
        {
            xml.append("\n");
        }
        xml.append(getIndent());
        writeEndElement((String)elementStack.pop());
    }


    public void emptyElement(String elem)
    {
        if (pretty)
        {
            xml.append("\n");
        }
        xml.append(getIndent());
        xml.append("<");
        xml.append(elem);
        xml.append("/>");
    }


    public void emptyElement(String elem, Properties attribs)
    {
        if (pretty)
        {
            xml.append("\n");
        }
        xml.append(getIndent());
        writeStartElement(elem, attribs, true);
    }


    public void xmlDeclaration(String encoding)
    {
        // XML declaration
        StringBuffer declaration = new StringBuffer("<?xml version=\"1.0\"");

        if (encoding != null)
        {
            declaration.append(" encoding=\"");
            declaration.append(encoding);
            declaration.append("\"");
        }

        declaration.append("?>\n");

        // insert it at the beginning
        xml.insert(0, declaration.toString());
    }


    public String getXml()
    {
        return xml.toString();
    }


    //
    // Private Methods
    //

    private String getIndent()
    {
        StringBuffer indent = new StringBuffer(8);

        for (int i = 0; i < indentWidth * indentLevel; i++)
        {
            indent.append(' ');
        }

        return indent.toString();
    }


    private void writeStartElement(String elem, Properties attribs,
        boolean empty)
    {
        xml.append("<");
        xml.append(elem);

        // write all attributes
        if (attribs != null && attribs.size() > 0)
        {
            for (Enumeration e = attribs.propertyNames();
                 e.hasMoreElements();)
            {
                String attribName = (String)e.nextElement();

                xml.append(" ");
                xml.append(attribName);
                xml.append("=\"");
                xml.append(attribs.getProperty(attribName));
                xml.append("\"");
            }
        }

        if (empty)
        {
            xml.append("/");
        }

        xml.append(">");
    }


    private void writeEndElement(String name)
    {
        xml.append("</");
        xml.append(name);
        xml.append(">");
    }
}
