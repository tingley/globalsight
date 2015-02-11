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
package com.globalsight.ling.tm;

import java.util.List;
import java.util.Iterator;
import java.io.StringReader;
import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.apache.log4j.Logger;

import org.jdom.output.Format;
import org.jdom.output.Format.TextMode;
import org.jdom.output.XMLOutputter;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.Attribute;


/**
 * Represents a GXML segment internal tag (TMX superset) and provides
 * methods for comparison between two tags.
 */
public class TmxTag
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            SegmentTagsAligner.class.getName());

    private static final String DEFAULT_SAX_DRIVER_CLASS =
        "org.apache.xerces.parsers.SAXParser";

    private boolean m_Matched = false;
    private boolean m_duplicate = false;
    private int m_InsertionPoint = -1;
    private String m_TmxTag = null;
    private Document m_TmxTagDocument = null;
    private String m_xValue = null;
    private String m_Name = null;
    private String m_Type = null;
    private static SAXBuilder c_builder =
        new SAXBuilder(DEFAULT_SAX_DRIVER_CLASS, false);

    public TmxTag(String p_TmxTag, int p_insertionPoint)
        throws LingManagerException
    {
        m_TmxTag = p_TmxTag;
        m_InsertionPoint = p_insertionPoint;

        try
        {         
            // Build the JDOM Document
			m_TmxTagDocument = c_builder.build(new StringReader(p_TmxTag));
        }
        catch (JDOMException e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new LingManagerException(e);
        } catch (IOException e) {
            CATEGORY.error(e.getMessage(), e);
            throw new LingManagerException(e);
        }
    }

    public void setMatched()
    {
        m_Matched = true;
    }
    
    public void setUnMatched()
    {
        m_Matched = false;
    }

    public int getInsertionPoint()
    {
        return m_InsertionPoint;
    }

    public String getXValue()
    {
        m_xValue = m_TmxTagDocument.getRootElement().getAttributeValue("x");

        if (m_xValue == null)
        {
            m_xValue = "";
        }

        return m_xValue;
    }

    public void setXValue(String p_xValue)
    {
        m_xValue = p_xValue;
        m_TmxTagDocument.getRootElement().removeAttribute("x");

        Element root = m_TmxTagDocument.getRootElement().setAttribute("x", p_xValue);

        m_TmxTagDocument.setRootElement(root);
    }

    public int getLength()
    {
        return m_TmxTag.length();
    }

    public String getName()
    {
        m_Name = m_TmxTagDocument.getRootElement().getName();

        if (m_Name == null)
        {
            m_Name = "";
        }

        return m_Name;
    }

    public String getType()
    {
        m_Type = m_TmxTagDocument.getRootElement().getAttributeValue("type");

        if (m_Type == null)
        {
            m_Type = "";
        }

        return m_Type;
    }

    private boolean compareAttributes(List p_sourceAttrs, List p_targetAttrs)
    {
        /*       // check sizes - should be the same
                 if (p_sourceAttrs.size() != p_targetAttrs.size())
                 {
                 return false;
                 }
        */
        Iterator sourceIterator = p_sourceAttrs.iterator();
        Iterator targetIterator = p_targetAttrs.iterator();

        while (targetIterator.hasNext())
        {
            Attribute attr = (Attribute)targetIterator.next();

            // skip "i" and "x" for comparison
            String attrName = attr.getName();
            if ((!attrName.equals("i")) && (!attrName.equals("x")) &&
                (!attrName.equals("id")))
            {
                if (!findAttributeMatch(p_sourceAttrs, attr))
                {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean compareElement(Element p_sourceElement,
        Element p_targetElement)
    {
        List sourceChildren = p_sourceElement.getContent();
        List targetChildren = p_targetElement.getContent();

        // check sizes - should be the same
        if (sourceChildren.size() != targetChildren.size())
        {
            return false;
        }

        Iterator sourceIterator = sourceChildren.iterator();
        Iterator targetIterator = targetChildren.iterator();

        // compare element names
        if (!p_sourceElement.getName().equals(p_targetElement.getName()))
        {
            return false;
        }

        // compare attribues ignoring "i" and "x"
        if (!compareAttributes(p_sourceElement.getAttributes(),
            p_targetElement.getAttributes()))
        {
            return false;
        }

        // skip sub and all it's children
        if (p_sourceElement.getName().equals("sub"))
        {
            if (sourceIterator.hasNext())
            {
                sourceIterator.next();
                targetIterator.next();
            }
        }

        while (sourceIterator.hasNext())
        {
            Object so = sourceIterator.next();
            Object to = targetIterator.next();

            if (so instanceof Element && to instanceof Element)
            {
                if (!compareElement((Element) so, (Element) to))
                {
                    return false;
                }
            }
            else if (so instanceof String && to instanceof String)
            {
                if (((String) so).compareTo((String) to) != 0)
                {
                    return false;
                }
            }
            else // error
            {
                System.err.println("compare error");
                return false;
            }
        }

        return true;
    }

    public boolean equals(TmxTag p_targetTmxTag)
    {
        // check tag name
        if (!getName().equals(p_targetTmxTag.getName()))
        {
            return false;
        }

        // check TMX type
        if (!getType().equals(p_targetTmxTag.getType()))
        {
            return false;
        }

        List sourceChildren = m_TmxTagDocument.getContent();
        List targetChildren = p_targetTmxTag.getDocument().getContent();

        // check sizes - should be the same
        if (sourceChildren.size() != targetChildren.size())
        {
            return false;
        }

        Iterator sourceIterator = sourceChildren.iterator();
        Iterator targetIterator = targetChildren.iterator();
        while (sourceIterator.hasNext() && targetIterator.hasNext())
        {
            Object so = sourceIterator.next();
            Object to = targetIterator.next();

            if ((so instanceof Element) && (to instanceof Element))
            {
                if (!compareElement((Element)so, (Element)to))
                {
                    return false;
                }
            }
            else // error
            {
                return false;
            }
        }

        return true;
    }

    /**
       @return org.jdom.Element
    */
    public Document getDocument()
    {
        return m_TmxTagDocument;
    }

    public String getString()
        throws LingManagerException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Format format = Format.getRawFormat();
        format.setTextMode(TextMode.TRIM);
        /**
         * Updated by Vincent Yan, 2010/04/21
         * format details got from jdom 1.1.1:
         * indent = null;
         * lineSeparator = "\r\n";
         * encoding = "UTF-8";
         * omitDeclaration = false;
         * omitEncoding = false;
         * expandEmptyElements = false;
         * ignoreTrAXEscapingPIs = false;
         * mode = TextMode.PRESERVE;
         * escapeStrategy = new DefaultEscapeStrategy(encoding);
        */

        XMLOutputter outputter = new XMLOutputter(format);
//        outputter.setTrimText(true);
//        outputter.setIndent(false);
//        outputter.setNewlines(false);
//        outputter.setSuppressDeclaration(true);

        try
        {
            outputter.output(m_TmxTagDocument, out);
        }
        catch (IOException e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new LingManagerException(e);
        }

        return out.toString();
    }

    /**
     *
     * @return boolean
     */
    public boolean isAddable()
    {
        String value =
            m_TmxTagDocument.getRootElement().getAttributeValue("erasable");

        if (value == null)
        {
            return false;
        }

        if (value.equals("yes"))
        {
            return true;
        }

        return false;
    }

    private boolean findAttributeMatch(List p_sourceList, Attribute p_attr)
    {
        Iterator sourceIterator = p_sourceList.iterator();

        while(sourceIterator.hasNext())
        {
            Attribute attr = (Attribute)sourceIterator.next();

            // skip "i" and "x" for comparison
            String attrName = attr.getName();
            String attrValue = attr.getValue();
            if (attrName.equals("i") || attrName.equals("x") ||
                attrName.equals("id"))
            {
                continue;
            }

            if (attrName.equals(p_attr.getName()) &&
                attrValue.equals(p_attr.getValue()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isMatched()
    {
        return m_Matched;
    }

    public String getIValue()
    {
        String value =
            m_TmxTagDocument.getRootElement().getAttributeValue("i");

        if (value == null)
        {
            value = "";
        }

        return value;
    }

    public boolean isEpt()
    {
        if (getName().equals("ept"))
        {
            return true;
        }

        return false;
    }

    public boolean isBpt()
    {
        if (getName().equals("bpt"))
        {
            return true;
        }

        return false;
    }
    
    public boolean isDuplicate()
    {
        return m_duplicate;
    }
    
    public void setDuplicate()
    {
        m_duplicate = true;
    }
    
}
