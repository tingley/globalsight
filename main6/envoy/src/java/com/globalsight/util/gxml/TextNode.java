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
package com.globalsight.util.gxml;

import org.apache.log4j.Logger;

import com.globalsight.util.edit.EditUtil;

/**
 * <p>In GXML or Paginated ResultSet, each XML tag pairs and the
 * content in between are taken as a single Gxml element.  The text
 * content of a GxmlElement is represented by the GxmlElement having a
 * child of class TextNode.  TextNode contains the text content.</p>
 */
public class TextNode extends GxmlElement
{
	private static final long serialVersionUID = 8700131155153140233L;

	static private final Logger logger = Logger.getLogger(TextNode.class);

    /** the text content coming between the tag pair */
    private StringBuffer m_textBuffer = null;

    /**
     * Construct a TextNode with the given type and name.
     */
    TextNode()
    {
        super(TEXT_NODE, GxmlNames.TEXT);
        m_textBuffer = new StringBuffer();
    }

    /**
     * Shallow copy constructor.
     * @param p_textNode construct a TextNode from p_textNode
     */
    public TextNode(TextNode p_textNode)
    {
        super(p_textNode);
        m_textBuffer = new StringBuffer(p_textNode.m_textBuffer.toString());
    }

    /**
     * Get the text value of the element. The text value is defined as
     * the the content of the TextNode, if it is a TextNode, and if it
     * is not a TextNode, it returns the summation of the content of
     * the immediate children TextNodes
     *
     * @return the text content of the element as a String.  Returns
     * an empty String if the GxmlElement is not a TextNode or it has
     * no immediate child TextNodes.
     */
    public String getTextValue()
    {
        return getTextNodeValue();
    }

    /**
     * Get the text value of a TextNode element. The TextNode value is
     * defined as the content of a TextNode.  If the GxmlElement is
     * not a TextNode, null is returned.
     *
     * @return the content of a TextNode GxmlElement as a String.
     * Returns null if it is not a TextNode.
     */
    public String getTextNodeValue()
    {
        return m_textBuffer.toString();
    }

    /**
     * Get the total text value of the element. The total text value
     * is defined as the the content of the TextNode, if it is a
     * TextNode, and if it is not a TextNode, it returns the summation
     * of the content of all the descendant TextNodes.  Returns null
     * if there are no TextNodes.
     *
     * @return the content of the element as a String.
     */
    public String getTotalTextValue()
    {
        return getTextNodeValue();
    }

    /**
     * To get the text value of the element. The text value is defined
     * as the text content between the element tag pair.
     * @return the content of the element as a StringBuffer.
     */
    public void setTextBuffer(StringBuffer p_stringBuffer)
    {
        m_textBuffer = new StringBuffer(p_stringBuffer.toString());
    }

    /**
     * To append text value to the existing text value.
     */
    void appendTextValue(String p_textValue)
    {
        m_textBuffer.append(p_textValue);
    }

    void appendTextValue(char buf[], int offset, int len)
    {
        m_textBuffer.append(buf, offset, len);
    }

    /**
     * To get the end tag of this element.
     * @return  element ending tag as a String.
     */
    public String getEndTag()
    {
        return "";
    }

    /**
     * To get the start tag of this element, the start tag includes
     * all the attributes defined.
     *
     * @return  element starting tag as a String.
     */
    public String getStartTag()
    {
        return "";
    }

    /**
     * A String representation of the object.
     * @return  a String representation of the object.
     */
    public String toString()
    {
        return super.toString() + "[" + m_textBuffer.toString() + "]";
    }

    /**
     * Reverse this element to XML content, including converting all
     * its child elements. Text is returned in xml-encoded form so it
     * can be parsed by an XML reader.
     * @param p_startTagDelimiter placed before start-tag
     * @return  the element as a Gxml String
     */
    protected String toGxml(String p_startTagDelimiter,
        boolean p_excludeTopTags)
    {
        return EditUtil.encodeXmlEntities(m_textBuffer);
    }
    
    /**
     * Reverse this element to XML content, including converting all
     * its child elements. Text is returned in xml-encoded form so it
     * can be parsed by an XML reader.
     * @param p_startTagDelimiter placed before start-tag
     * @param p_handleNRT if handle \n \r \t (for javascript files)
     * @return  the element as a Gxml String
     */
    protected String toGxml(String p_startTagDelimiter,
            boolean p_excludeTopTags, boolean p_handleNRT)
    {
    	return toGxml(p_startTagDelimiter, p_excludeTopTags, p_handleNRT, false);
    }
    
    protected String toGxml(String p_startTagDelimiter,
            boolean p_excludeTopTags, boolean p_handleNRT, boolean isXliff)
    {
        String strResult = EditUtil.encodeXmlEntities(m_textBuffer);
        if ( p_handleNRT ) {
            strResult = EditUtil.encodeNTREntities(new StringBuffer(strResult));
        }
        
        return strResult;
    }

    public boolean equals(Object p_object)
    {
        if (this == p_object)
        {
            return true;
        }

        if (p_object instanceof TextNode)
        {
            return this.equals((TextNode)p_object);
        }

        return false;
    }

    public boolean equals(TextNode p_textNode)
    {
        if (this == p_textNode)
        {
            return true;
        }

        if (m_textBuffer.toString().equals(
            p_textNode.m_textBuffer.toString()))
        {
            return true;
        }

        return false;
    }
}
