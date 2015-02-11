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
package com.globalsight.ling.docproc.extractor.javaprop;

import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.TmxTagGenerator;

/**
 * <p>Controls the creation of TMX tags and attributes for java
 * properties.</p>
 */
public class JPTmxEncoder
    implements JPConstants
{
    protected int m_nErasable;
    private boolean m_bUseTmxOnLeadingSpaces;

    public JPTmxEncoder()
    {
        super();

        m_nErasable = ALL_TMX_NON_ERASABLE;
        m_bUseTmxOnLeadingSpaces = true;
    }

    /**
     * <p>Enables or disables TMX encoding on leading spaces.</p>
     *
     * @param p_bState boolean, true = enabled, false = disabled.
     */
    public void enableTmxOnLeadingSpaces(boolean p_bState)
    {
        m_bUseTmxOnLeadingSpaces = p_bState;
    }

    /**
     * <p>Returns a new string object containing the input string with
     * formatting marked by TMX.</p>
     *
     * @param p_str String
     * @return String - the TMX encoded string (always a new
     * object), or <code>null</code> if the input string was null.
     */
    public String encode(String p_str)
    {
        int x = 0, len = 0;
        char aChar;
        StringBuffer buff;

        if (p_str == null)
        {
            return null;
        }

        len = p_str.length();
        if (len == 0)
        {
            return p_str;
        }

        buff = new StringBuffer (len*2);
        aChar = p_str.charAt(x++);

        // leading whitespace - for software it should be preserved
        while ((x < len) && Character.isWhitespace(aChar))
        {
            buff.append(makeTmx(aChar, true)); // true = leading whitespace
            aChar = p_str.charAt(x++);
        }
        --x; // backup one

        while (x < len)
        {
            aChar = p_str.charAt(x++);
            String tmx = makeTmx(aChar, false);
            if (tmx.equals("" + aChar))
            {
                tmx = encodeXml(aChar);
            }

            buff.append(tmx);
        }

        return (buff.toString());
    }

    /**
     * <p>Returns a placeholder TMX tag for the given character, if
     * the character is treated special by TMX.  If there is no TMX
     * equivalent, the input char is returned.</p>
     *
     * <p>Special Characters recognized by TMX are \r (carriage
     * return), \n (line break), \f (form feed) \t (tab).  Leading
     * space characters are also considered special when the feature
     * has been enabled with {@link
     * #enableTmxOnLeadingSpaces(boolean)}.</p>
     *
     * @param p_char - the item to be wrapped.
     * @param p_bLeads - true indicates leading whitespace.
     * @return String - A TMX tag or, if there is no TMX
     * mapping, the input char itself.
     */
    public String makeTmx(char p_char, boolean p_bLeads)
    {
        String tmp = new String("");
        TmxTagGenerator TmxTagGen = new TmxTagGenerator();

        TmxTagGen.setTagType(TmxTagGenerator.PH);

        if ((m_nErasable == ALL_TMX_NON_ERASABLE) ||
          (p_bLeads && (m_nErasable == JPConstants.LEADING_TMX_NON_ERASABLE)))
        {
            TmxTagGen.setErasable(false);
        }
        else
        {
            TmxTagGen.setErasable(true);
        }

        if (p_char == '\f')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.FORMFEED);
            tmp = "\\f";
        }
        // CvdL: following placeholders break the segmenter
        /* else if (p_char == '\r')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.LINEBREAK);
            tmp = "\\r";
        }
        else if (p_char == '\n')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.LINEBREAK);
            tmp = "\\n";
        }
        else if (p_char == '\t')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.TAB);
            tmp = "\\t";
        }
        */
        else if ((p_char == ' ') && m_bUseTmxOnLeadingSpaces && p_bLeads)
        {
            TmxTagGen.setInlineType(TmxTagGenerator.SPACE);
            tmp = " ";
        }

        if (tmp.length() > 0)
        {
            TmxTagGen.makeTags();
            return (TmxTagGen.getStart() + tmp + TmxTagGen.getEnd());
        }
        else
        {
            return ("" + p_char);
        }
    }

    /**
     * <p>Returns a TMX string for the given message placholder
     * @param p_str - the message placholder.
     * @return String - A TMX string or null if the input is invalid.
     */
    static private final String CHOICE = "choice,";

    public String makeTmxForMsgPlaceholder(String p_str)
    {
        if (p_str == null || (p_str.length() == 0) ||
           !p_str.startsWith("{") || !p_str.endsWith("}") )
        {
            return null;
        }

        StringBuffer result = new StringBuffer();
        TmxTagGenerator TmxTagGen = new TmxTagGenerator();

        TmxTagGen.setTagType(TmxTagGenerator.PH);
        TmxTagGen.setErasable(false);             // never erasable
        TmxTagGen.setInlineType(TmxTagGenerator.VARIABLE);
        TmxTagGen.makeTags();

        result.append(TmxTagGen.getStart());

        int len = p_str.length();
        int i = 0;
        char aChar = p_str.charAt(i++);

        while (i < len && aChar != ',')
        {
            result.append(encodeXml(aChar));
            aChar = p_str.charAt(i++);
        }
        result.append(encodeXml(aChar));

        if (aChar == ',')
        {
            if (p_str.substring(i).startsWith(CHOICE))
            {
                result.append(CHOICE);
                i += CHOICE.length();

                while (true)
                {
                    while (i < len && aChar != '#' && aChar != '<')
                    {
                        aChar = p_str.charAt(i++);
                        result.append(encodeXml(aChar));
                    }

                    StringBuffer sub = new StringBuffer ();
                    while (i < len)
                    {
                        aChar = p_str.charAt(i++);

                        if (aChar == '|' || aChar == '}') // end of choice
                        {
                            break;
                        }

                        sub.append(encodeXml(aChar));

                        if (aChar == '{')         // must be recursive
                        {
                            int level = 1;
                            while (i < len && level > 0)
                            {
                                aChar = p_str.charAt(i++);
                                sub.append(encodeXml(aChar));
                                if (aChar == '{') ++level;
                                else if (aChar == '}') --level;
                            }
                        }
                    }

                    // Subs are normal text, even if they contain
                    // recursive MessageFormat placeholders
                    result.append(
                      "<sub locType=\"translatable\" datatype=\"" +
                      IFormatNames.FORMAT_JAVAPROP + "\" type=\"text\">" +
                      sub.toString() + "</sub>");

                    result.append(encodeXml(aChar));

                    if (aChar == '}')
                    {
                        break;
                    }
                }
            }
            else
            {
                // contains no special chars
                result.append(p_str.substring(i));
            }
        }

        result.append(TmxTagGen.getEnd());

        return result.toString();
    }

    /**
     * <p>Sets the erasable option for TMX tags.  The value may be set
     * to one of the following:</p>
     *
     * <ul>
     * <li><code>ALL_TMX_NON_ERASABLE</code>
     *      encoded formatting cannot be erased</li>
     *  <li><code>ALL_TMX_ERASABLE</code>
     *       encoded formatting can be erased</li>
     *  <li><code>LEADING_TMX_NON_ERASABLE</code>
     *       only leading formatting can be erased</li>
     * </ul>
     *
     * @see JPConstants
     */
    public void setErasable(int p_state)
    {
        if ((p_state < 0 ) || (p_state > 3))
        {
            return;
        }

        m_nErasable = p_state;
    }

    private String encodeXml(char p_char)
    {
        switch (p_char)
        {
        case '<':  return "&lt;";
        case '>':  return "&gt;";
        case '&':  return "&amp;";
        case '\'': return "&apos;";
        case '"':  return "&quot;";
        default:   return "" + p_char;
        }
    }

}
