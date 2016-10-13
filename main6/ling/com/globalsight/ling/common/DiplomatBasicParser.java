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

import java.util.Properties;
import java.util.Stack;

import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.everest.page.pageexport.style.mif.TagUtil;
import com.globalsight.ling.tw.Tmx2PseudoHandler;

/**
 * Parses standalone Diplomat strings using registered handler.
 */
public class DiplomatBasicParser
{
    DiplomatBasicHandler m_handler;

    int m_maxLen;
    int m_start;
    int m_end;

    StringBuffer m_tagName;
    boolean m_inSub = false;
    StringBuffer m_attrName;
    StringBuffer m_attrValue;
    Properties m_attributes;
    private CxeMessage cxeMessage = null;

    public CxeMessage getCxeMessage()
    {
        return cxeMessage;
    }

    public void setCxeMessage(CxeMessage cxeMessage)
    {
        this.cxeMessage = cxeMessage;
    }

    /** Constructor. */
    public DiplomatBasicParser(DiplomatBasicHandler p_handler)
    {
        m_handler = p_handler;
    }

    /** Allocates needed resources. */
    private void init(String p_tmx)
    {
        if (m_tagName == null)
        {
            m_tagName = new StringBuffer(32);
        }

        if (m_attrName == null)
        {
            m_attrName = new StringBuffer(32);
        }

        if (m_attrValue == null)
        {
            m_attrValue = new StringBuffer(32);
        }

        if (m_attributes == null)
        {
            m_attributes = new Properties();
        }

        m_start = m_end = 0;

        m_maxLen = p_tmx.length();
        
        m_inSub = false;
    }

    /**
     * Frees used resources. Objects will be garbage-collected if this objectd
     * is free'd.
     */
    private void exit()
    {
        m_tagName.setLength(0);
        m_attrName.setLength(0);
        m_attrValue.setLength(0);
        m_attributes = new Properties();
    }

    /**
     * Parses a TMX string and generates events for a DiplomatBasicHandler can
     * catch. Decoding the string (if it is in UTF-8, for instance) is not
     * implemented by this class but must be implemented by the handler.
     * 
     * @see DiplomatBasicHandler
     */
    public void parse(String p_tmx) throws DiplomatBasicParserException
    {
    	if (cxeMessage != null
				&& cxeMessage.getMessageType().getName()
						.equals("MIF_LOCALIZED_EVENT")) 
		{
			TagUtil u = new TagUtil();
			p_tmx = u.handleString(p_tmx);
		}
    	
    	init(p_tmx);

        m_handler.handleStart();
        
        int i;

        try
        {
            boolean bInTag = false;
            boolean isExtracted = false;
            Stack<Boolean> isInPhStack = new Stack<Boolean>();
            Stack<Boolean> extractedStack = new Stack<Boolean>();
            Stack<String> tagStack = new Stack<String>();
            for (i = 0; i < m_maxLen; i++)
            {
                char ch = p_tmx.charAt(i);

                if (bInTag)
                {
                    if (ch == '>')
                    {
                        m_end = i + 1;
                        String subStr = p_tmx.substring(m_start, m_end);
                        isExtracted = subStr.indexOf("isTranslate=") != -1;

                        if (subStr.startsWith("<ph "))
                        {
                            int subi = 0;
                            char subCh = subStr.charAt(subi);
                            // read the name...
                            while (subCh != '>' && subCh != '/')
                            {
                                subCh = subStr.charAt(++subi);
                            }

                            if (subCh == '>')
                            {
                                isInPhStack.push(true);
                            }
                        }

                        if (subStr.indexOf("</ph>") != -1
                                && isInPhStack.size() > 0 && isInPhStack.peek())
                        {
                            isInPhStack.pop();
                        }

                        if (subStr.indexOf("<it") != -1)
                        {
                            tagStack.push("start");
                        }

                        if (tagStack.size() > 0
                                && subStr.indexOf("</it>") != -1)
                        {
                            tagStack.pop();
                        }

                        if (isExtracted)
                        {
                            if (subStr.indexOf("<bpt") != -1)
                            {
                                extractedStack.push(isExtracted);
                                tagStack.push("start");
                            }
                            else if (subStr.indexOf("</bpt>") != -1)
                            {
                                tagStack.push("end");
                            }
                        }
                        else
                        {
                            if (extractedStack.size() > 0
                                    && extractedStack.peek())
                            {
                                if (subStr.indexOf("<bpt") != -1)
                                {
                                    tagStack.push("start");
                                }
                                else if (subStr.indexOf("</bpt>") != -1)
                                {
                                    tagStack.push("end");
                                }
                                
                                if (subStr.indexOf("</ept") != -1)
                                {
                                    extractedStack.pop();
                                }
                            }
                            if (tagStack.size() > 0
                                    && subStr.indexOf("<ept") != -1)
                            {
                                tagStack.pop();
                            }

                            if (tagStack.size() > 0
                                    && subStr.indexOf("</ept") != -1)
                            {
                                tagStack.pop();
                            }
                        }

                        processTag(p_tmx, m_start, m_end);

                        bInTag = false;
                        m_start = m_end = i + 1;
                    }
                }
                else if (ch == '<')
                {
                    m_end = i;

                    if (m_start < m_end)
                    {
                        boolean isInPh = (isInPhStack.size() > 0) ? isInPhStack
                                .peek() : false;
                        String subString = p_tmx.substring(m_start, m_end);
                        if (isInPh)
                        {
                            if (m_inSub && cxeMessage != null
                                    && cxeMessage.getMessageType().getName()
                                            .equals("MIF_LOCALIZED_EVENT")
                                    && !isMifPlaceholder(subString))
                            {
                                subString = subString.replace("&apos;", "\\q");
                                subString = subString.replace("'", "\\q");
                                subString = subString.replace("`", "\\Q");
                                if (subString.contains("&gt;")
                                        && !subString.contains("\\&gt;"))
                                {
                                    subString = subString
                                            .replace("&gt;", "\\>");
                                }
                            }
                            m_handler.handleText(subString);
                        }
                        else
                        {
                            if (cxeMessage != null
                                    && cxeMessage.getMessageType().getName()
                                            .equals("MIF_LOCALIZED_EVENT")
                                    && !isMifPlaceholder(subString))
                            {
                                subString = subString.replace("\\", "\\\\");
                                subString = subString.replace("&apos;", "\\q");
                                subString = subString.replace("'", "\\q");
                                subString = subString.replace("`", "\\Q");
                                subString = subString
                                        .replace("&amp;gt;", "\\>");
                                subString = subString.replace("&gt;", "\\>");
                            }
                            isExtracted = (extractedStack.size() > 0) ? extractedStack
                                    .peek() : false;
                            String tag = (tagStack.size() > 0) ? tagStack
                                    .peek() : "start";

                            if (tagStack.size() == 0)
                            {
                                tag = "end";
                            }

                            if (isExtracted && "end".equals(tag)
                                    && m_handler instanceof Tmx2PseudoHandler)
                            {
                                ((Tmx2PseudoHandler) m_handler)
                                        .handleIsExtractedText(subString);
                            }
                            else 
                            {
                                m_handler.handleText(subString);
                            }
                        }
                    }

                    bInTag = true;
                    m_start = m_end = i;
                }
            }

            if (i > m_start)
            {
                if (bInTag)
                {
                    throw new DiplomatBasicParserException(
                            "Invalid GXML string `" + p_tmx + "'");
                }
                else
                {
                    m_end = m_maxLen;
                    p_tmx = p_tmx.substring(m_start, m_end);
                    if (cxeMessage != null
                            && cxeMessage.getMessageType().getName()
                                    .equals("MIF_LOCALIZED_EVENT"))
                    {
                        p_tmx = p_tmx.replace("\\", "\\\\");
                        p_tmx = p_tmx.replace("&apos;", "\\q");
                        p_tmx = p_tmx.replace("'", "\\q");
                        p_tmx = p_tmx.replace("`", "\\Q");
                        p_tmx = p_tmx.replace("&amp;gt;", "\\>");
                        p_tmx = p_tmx.replace("&gt;", "\\>");
                    }
                    m_handler.handleText(p_tmx);
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            throw new DiplomatBasicParserException("Error parsing GXML: "
                    + e.toString());
        }

        m_handler.handleStop();

        exit();
    }

    private boolean isMifPlaceholder(String subString)
    {
        if (subString.startsWith("&lt;") && subString.endsWith("\\&gt;"))
        {
            return true;
        }

        return false;
    }

    /**
     * Skips over whitespace in the input string.
     * 
     * @return int first index of non-white space following the offset
     */
    private int eatWhitespaces(String p_tmx, int p_index)
    {
        int i = p_index;

        while (i < m_maxLen && Character.isWhitespace(p_tmx.charAt(i)))
        {
            i++;
        }

        return i;
    }

    /**
     * Reads a tag. Assumes that all atributes are correct. Input is the tag
     * including &lt; and &gt;.
     */
    private void processTag(String p_tmx, int p_min, int p_max)
            throws IndexOutOfBoundsException, DiplomatBasicParserException
    {
        int i = eatWhitespaces(p_tmx, p_min + 1);
        char ch = p_tmx.charAt(i);

        boolean bEndTag = ch == '/';
        if (bEndTag)
        {
            ch = p_tmx.charAt(++i);
        }

        // read the name...
        while (!Character.isWhitespace(ch) && ch != '>' && ch != '/')
        {
            m_tagName.append(ch);
            ch = p_tmx.charAt(++i);
        }

        boolean bEmptyTag = ch == '/';
        if (bEmptyTag)
        {
            ch = p_tmx.charAt(++i);
        }

        if (!(bEndTag || bEmptyTag))
        {
            m_attributes = new Properties();

            // read the attributes...
            while (i < p_max)
            {
                i = eatWhitespaces(p_tmx, i);
                ch = p_tmx.charAt(i);

                // we've consumed all the attributes - only whitespace left
                if (ch == '>' || i >= p_max)
                {
                    break;
                }

                // also break if the element is empty
                if (ch == '/')
                {
                    bEmptyTag = true;
                    ch = p_tmx.charAt(++i);
                    break;
                }

                // read the attribute name...
                while (!Character.isWhitespace(ch) && ch != '=')
                {
                    m_attrName.append(ch);
                    ch = p_tmx.charAt(++i);
                }

                i = eatWhitespaces(p_tmx, i);
                ch = p_tmx.charAt(i);

                // ... and attribute value
                if (ch == '=')
                {
                    while (ch != '"')
                    {
                        ch = p_tmx.charAt(++i);
                    }

                    ch = p_tmx.charAt(++i);

                    while (ch != '"')
                    {
                        m_attrValue.append(ch);
                        ch = p_tmx.charAt(++i);
                    }

                    ++i;
                }
                else
                {
                    throw new DiplomatBasicParserException(
                            "Invalid GXML string `" + p_tmx + "'");
                }

                m_attributes.put(m_attrName.toString(), m_attrValue.toString());

                m_attrName.setLength(0);
                m_attrValue.setLength(0);
            }
        }

        String tag = m_tagName.toString();
        m_tagName.setLength(0);

        if (bEndTag)
        {
            m_handler.handleEndTag(tag, p_tmx.substring(p_min, p_max));
            
            if (m_inSub && "sub".equals(tag))
            {
                m_inSub = false;
            }
        }
        else if (bEmptyTag)
        {
            // An empty is rare, so we can construct temp strings.
            m_handler.handleStartTag(tag, m_attributes,
                    p_tmx.substring(p_min, i - 1) + ">");

            m_handler.handleEndTag(tag, "</" + tag + ">");
        }
        else
        {
            m_handler.handleStartTag(tag, m_attributes,
                    p_tmx.substring(p_min, p_max));
            
            if ("sub".equals(tag))
            {
                m_inSub = true;
            }
        }
    }
}
