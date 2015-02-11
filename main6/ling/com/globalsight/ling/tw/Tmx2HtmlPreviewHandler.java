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
package com.globalsight.ling.tw;

import com.globalsight.ling.common.DiplomatBasicHandler;
import com.globalsight.ling.common.DiplomatBasicParserException;
import java.util.Stack;
import java.util.Properties;

/**
 * Handles diplomat parser events to convert a TMX to Html Preview.<p>
 * This handler assumes that the original format of the TMX is HTML.<p>
 */
public class Tmx2HtmlPreviewHandler
    implements DiplomatBasicHandler
{
    private Stack elementStack = new Stack();
    private XmlEntities xmlDecoder = new XmlEntities();
    private StringBuffer htmlText;
    private StringBuffer originalTag;

    static final Integer BPT   = new Integer(1);
    static final Integer EPT   = new Integer(2);
    static final Integer IT    = new Integer(3); // including it, ut and ph
    static final Integer SUB   = new Integer(4);
    static final Integer UNSET = new Integer(-1);

    /**
     * Returns the HTML string ready to display in a browser
     */
    public String getResult()
    {
        return htmlText.toString();
    }

    /**
     * Handles diplomat basic parser Start event.
     */
    public void handleStart()
        throws DiplomatBasicParserException
    {
        htmlText = new StringBuffer(32);
        originalTag = new StringBuffer(32);
        elementStack.push(UNSET);
    }

    /**
     * Handles diplomat basic parser Stop event.
     */
    public void handleStop()
        throws DiplomatBasicParserException
    {
    }

    /**
    * End-tag event handler.
    * @param p_strName - the literal tag name
    * @param p_strOriginalTag - the complete raw token from the parser
    */
    public void handleEndTag(String p_strName, String p_strOriginalTag)
    {
        Integer tagId = (Integer)elementStack.pop();

        // if the parent is the normal text, process the tag
        if ((Integer)elementStack.peek() == UNSET)
        {
            String htmlTag = originalTag.toString();
            if (tagId == BPT || tagId == IT)
            {
                // 1. remove onXXX attribute
                String noOnXXX = removeOnXXXAttribute(htmlTag);

                // 2. replace href to href="#"
                htmlTag = replaceHref(noOnXXX);
            }

            htmlText.append(htmlTag);
            originalTag.setLength(0);
        }
    }

    /**
    * Start-tag event handler.
    * @param p_strTmxTagName - The literal tag name.
    * @param p_hAtributes - Tag attributes in the form of a hashtable.
    * @param p_strOriginalString - The complete raw token from the parser.
    */
    public void handleStartTag(String p_strTmxTagName,
        Properties p_hAttributes, String p_strOriginalString)
        throws DiplomatBasicParserException
    {
        Integer tagId;

        if (p_strTmxTagName.equals("bpt"))
        {
            tagId = BPT;
        }
        else if (p_strTmxTagName.equals("ept"))
        {
            tagId = EPT;
        }
        else if (p_strTmxTagName.equals("it") ||
            p_strTmxTagName.equals("ph") ||
            p_strTmxTagName.equals("ut"))
        {
            tagId = IT;
        }
        else if (p_strTmxTagName.equals("sub"))
        {
            tagId = SUB;
        }
        else
        {
            // Shouldn't happen... in case for the future spec change
            throw new DiplomatBasicParserException();
        }

        elementStack.push(tagId);
    }

    /**
    * Text event handler.
    * @param p_strText - the next text chunk from between the tags
    */
    public void handleText(String p_strText)
        throws DiplomatBasicParserException
    {
        Integer tagId = (Integer)elementStack.peek();

        if (tagId == BPT || tagId == EPT || tagId == IT)
        {
            originalTag.append(xmlDecoder.decodeString(p_strText));
        }
        else if (tagId == SUB)
        {
            originalTag.append(p_strText);
        }
        else if (tagId == UNSET)
        {
            htmlText.append(replaceLF(replaceApos(p_strText)));
        }
        else
        {
            // Shouldn't happen... in case for the future spec change
            throw new DiplomatBasicParserException();
        }

    }

    /*
     * replace &apos; with '
     */
    private String replaceApos(String s)
    {
        StringBuffer buf = new StringBuffer(s.length());
        int prevLast = 0;
        int index = prevLast;
        String apos = "&apos;";
        int lenApos = apos.length();

        while ((index = s.indexOf("&apos;", index)) != -1)
        {
            buf.append(s.substring(prevLast, index));
            buf.append('\'');
            index += lenApos;
            prevLast = index;
        }

        buf.append(s.substring(prevLast));

        return buf.toString();
    }


    /*
     * replace LF with <BR> (LF is only passed in for
     * plaintext and javascript strings)
     */
    private String replaceLF(String s)
    {
        StringBuffer buf = new StringBuffer();
        int prevLast = 0;
        int index = prevLast;
        String br = "\n";
        int lenBr = br.length();

        while((index = s.indexOf("\n", index)) != -1)
        {
            buf.append(s.substring(prevLast, index));
            buf.append("<BR>");
            index += lenBr;
            prevLast = index;
        }

        buf.append(s.substring(prevLast));

        return buf.toString();
    }


    /*
     * remove onXXX attribute from HTML tags
     */
    private String removeOnXXXAttribute(String tagText)
    {
        StringBuffer buf = new StringBuffer();
        int prevLast = 0;
        int[] indices;

        while ((indices = findAttribute(tagText, "on", prevLast)) != null)
        {
            buf.append(tagText.substring(prevLast, indices[0]));
            prevLast = indices[1] + 1;
        }

        // append remaining text
        buf.append(tagText.substring(prevLast, tagText.length()));

        return buf.toString();
    }


    /*
     * remove href attribute from HTML tags
     */
    private String replaceHref(String tagText)
    {
        StringBuffer buf = new StringBuffer();
        int begin = 0;
        int[] indices;

        if ((indices = findAttribute(tagText, "href", 0)) != null)
        {
            buf.append(tagText.substring(0, indices[0]));
            // without onclick='return false', the editor still try to
            // jump somewhere
            buf.append("href=\"\" onclick='return false'");
            begin = indices[1] + 1;
        }

        buf.append(tagText.substring(begin));

        return buf.toString();
    }


    /*
     * Finds an attribute starting with the specified string.
     * @param htmlTag String from which the attribute is looked for.
     * @param attName Attribute name. Any attribute starting with
     * attName will be found.
     * @param index Starting index of the search
     * @return An array of two int, the starting index and the last
     * index of the attribute. If the attribute couldn't be found,
     * null is returned.
     */
    private int[] findAttribute(String htmlTag, String attName, int index)
    {
        String lower = attName.toLowerCase();
        String upper = attName.toUpperCase();
        int first = index;
        int last = -1;

        while((first = htmlTag.indexOf(" " + lower, first)) != -1
              || (first = htmlTag.indexOf(" " + upper, first)) != -1)
        {
            first++; // to preserve attribute's delimiter

            int space = htmlTag.indexOf(' ', first);
            last = htmlTag.indexOf('=', first);
            if (last == -1 || (space != -1 && last > space))
            {
                // assumes not an attribute we are interested in
                continue;
            }

            // determine the quote character
            char quote = htmlTag.charAt(++last);
            if (quote != '\"' && quote != '\'')
            {
                quote = ' ';
            }

            // find the closing quote

            // NOTE: maybe quote is escaped by CER
//              while((last = htmlTag.indexOf(quote, ++last)) != -1)
//              {
//                  // assuming the escape character of the quote is `\'
//                  if (quote == ' ' || htmlTag.charAt(last - 1) != '\\')
//                  {
//                      break;
//                  }
//              }
            if ((last = htmlTag.indexOf(quote, ++last)) == -1)
            {
                if (quote == ' ')
                {
                    // The attribute is in the end of the tag
                    // conjunctioning with `>' without a space
                    last = htmlTag.length() - 2;
                }
                else
                {
                    // assumes not an attribute we are interested in
                    continue;
                }
            }
            else
            {
                if (quote == ' ')
                {
                    // to preserve the delimiting space
                    last--;
                }
            }

            // Found the attribute!
            break;
        }

        int[] result = new int[2];
        if (first == -1)
        {
            result = null;
        }
        else
        {
            result[0] = first;
            result[1] = last;
        }

        return result;
    }

}
