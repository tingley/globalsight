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
package galign.helpers.util;

import galign.helpers.util.ObjectPool;

import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;

import java.io.StringReader;
import java.io.ByteArrayInputStream;

/**
 * <p>An XML Parser wrapper that simplifies parsing from an XML string
 * and returns Document objects. Also manages instances of itself in a
 * pool of parser objects.</p>
 */
public class XmlParser
    extends SAXReader
{
    //
    // Pool Management
    //
    static private ObjectPool s_pool = new ObjectPool(XmlParser.class);

    static public XmlParser hire()
    {
        return (XmlParser)s_pool.getInstance();
    }

    static public void fire(XmlParser p_object)
    {
        try
        {
            s_pool.freeInstance(p_object);
        }
        catch (Exception e)
        {
            // reset() can throw exception meaning object is
            // unusable, just don't free it then and let pool
            // allocate a new instance.
        }
    }

    //
    // Constructor
    //
    public XmlParser()
    {
        super();

        // set parsing features here
        try
        {
            // setXMLReaderClassName("org.dom4j.io.aelfred.SAXDriver");
            // setStringInternEnabled(true);
            setValidation(false);
            // in jdom1.1+ use this to remove ignorable whitespace
            //setMergeAdjacentText(true);
            //setStripWhitespaceText(true);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Document parseString(String p_xml)
    {
        try
        {
            return this.read(new StringReader(p_xml));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    static public Document parseXml(String p_xml)
    {
        XmlParser parser = null;

        try
        {
            parser = XmlParser.hire();
            return parser.parseString(p_xml);
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }

    static public Element parseXmlFragment(String p_xml)
    {
        XmlParser parser = null;

        try
        {
            StringBuffer xml = new StringBuffer(p_xml.length() + 53);
            xml.append("<AllYourBaseAreBelongToUs>");
            xml.append(p_xml);
            xml.append("</AllYourBaseAreBelongToUs>");

            parser = XmlParser.hire();
            return parser.parseString(xml.toString()).getRootElement();
        }
        finally
        {
            XmlParser.fire(parser);
        }
    }
}
