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

import java.util.Hashtable;

public class TmxBasicParser
{
    TmxBasicHandler m_handler;

    public TmxBasicParser(TmxBasicHandler p_Handler)
    {
        m_handler = p_Handler;
    }

    /**
     * Make the index progress to the next non white space.
     * @return int first index of non-white space following the offset
     * (string length is returned if it doesn't exist)
     */
    private int eatWhitespaces(String p_strString, int p_iIndex)
    {
        int i = p_iIndex;

        while (i < p_strString.length() &&
            Character.isWhitespace(p_strString.charAt(i)))
        {
            i++;
        }

        return i;
    }

    /**
     * Read a tag. Assumes that all atributes are correctly quoted
     * with double-quotes.
     */
    private void processTag(String p_content)
    {
        StringBuffer tagName = new StringBuffer();
        Hashtable attributes = new Hashtable();
        boolean bEndTag;

        int i = eatWhitespaces(p_content, 0);

        bEndTag = i < p_content.length() && p_content.charAt(i) == '/';

        //read the tag name...
        while (i < p_content.length() &&
            !Character.isWhitespace(p_content.charAt(i)))
        {
            tagName.append(p_content.charAt(i));

            i++;
        }

        i = eatWhitespaces(p_content, i);

        //read the attributes...
        StringBuffer attributeName  = new StringBuffer();
        StringBuffer attributeValue = new StringBuffer();

        while (i < p_content.length())
        {
            try
            {
                i = eatWhitespaces(p_content, i);

                //read the name...
                while (!Character.isWhitespace(p_content.charAt(i)) &&
                    p_content.charAt(i) != '=')
                {
                    attributeName.append(p_content.charAt(i));
                    i++;
                }

                i = eatWhitespaces(p_content, i);
                if (p_content.charAt(i) == '=')
                {
                    while (p_content.charAt(i) != '"')
                    {
                        i++;
                    }
                    i++;
                    while (p_content.charAt(i) != '"')
                    {
                        attributeValue.append(p_content.charAt(i));
                        i++;
                    }
                    i++;
                }

                attributes.put(attributeName.toString(),
                    attributeValue.toString());

                attributeName.setLength(0);
                attributeValue.setLength(0);
            }
            catch (IndexOutOfBoundsException e)
            {
                // tough luck the TMX wasn't as well written as we thought...
                // CvdL: TODO: throw an exception!!
            }
        }

        if (bEndTag)
        {
            m_handler.processEndTag(tagName.substring(1),
                "<" + p_content + ">");
        }
        else
        {
            m_handler.processTag(tagName.toString(), attributes,
                "<" + p_content + ">");
        }
    }

    private void processText(String p_text)
    {
        m_handler.processText(p_text);
    }

    public void tokenize(String p_tmx)
    {
        boolean bInTag = false;
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < p_tmx.length(); i++)
        {
            char c = p_tmx.charAt(i);

            if (bInTag)
            {
                if (c == '>')
                {
                    processTag(buf.toString());
                    buf.setLength(0);
                    bInTag = false;
                }
                else
                {
                    buf.append(c);
                }
            }
            else
            {
                if (c == '<')
                {
                    processText(buf.toString());
                    buf.setLength(0);
                    bInTag = true;
                }
                else
                {
                    buf.append(c);
                }
            }
        }

        if (buf.length() > 0)
        {
            if (bInTag)
            {
                // should be an error no closing > !!
                processTag(buf.toString());
            }
            else
            {
                processText(buf.toString());
            }
        }
    }
}
