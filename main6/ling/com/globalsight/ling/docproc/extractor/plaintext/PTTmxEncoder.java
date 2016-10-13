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
package com.globalsight.ling.docproc.extractor.plaintext;

import com.globalsight.ling.docproc.TmxTagGenerator;

/**
 * <p>Controls the creation of diplomat tags and atributes.</p>
 */
public class PTTmxEncoder
    implements PTConstants
{
    protected int m_nErasable;
    private boolean m_bUseTmxOnLeadingSpaces;

    /**
     * Constructor.
     */
    public PTTmxEncoder()
    {
        super();

        m_nErasable = ALL_TMX_NON_ERASABLE;
        m_bUseTmxOnLeadingSpaces = true;
    }

    /**
     * <p>Enables or disables diplomat &lt;ph&gt; tags on leading
     * spaces.</p>
     *
     * @param p_bState boolean, true = enabled, false = disabled.
     */
    public void enableTagsOnLeadingSpaces(boolean p_bState)
    {
        m_bUseTmxOnLeadingSpaces = p_bState;
    }

    /**
     * <p>Returns the input string with formatting tagged.</p>
     *
     * @param p_str java.lang.String
     * @return java.lang.String - the encoded string.
     */
    public String encode(String p_str)
    {
        int x = 0, len = 0;
        char aChar;

        len = p_str.length();
        StringBuffer buff = new StringBuffer (len*3);

        aChar = p_str.charAt(x++);

        // leading whitespace - for software it should be preserved
        while (Character.isWhitespace(aChar))
        {
            buff.append(makeTmx(aChar, true)); // true = leading whitespace
            aChar = p_str.charAt(x++);
        }
        --x; // backup one

        while (x < len)
        {
            aChar = p_str.charAt(x++);
            buff.append(makeTmx(aChar, false));
        }

        return (buff.toString());
    }

    /**
     * <p>Returns the &lt;ph&gt; tag for the given charater. If there
     * is no tag equivalent, the input char is returned.</p>
     *
     * @param p_char - the item to be wrapped.
     * @param p_bLeads - true indicates leading whitespace.
     * @return java.lang.String - A tag sequence or if there is no
     * mapping, the input char is returned.
     */
    public String makeTmx(char p_char, boolean p_bLeads)
    {
        String tmp = new String("");
        TmxTagGenerator TmxTagGen = new TmxTagGenerator();

        TmxTagGen.setTagType(TmxTagGenerator.PH);

        if ((m_nErasable == ALL_TMX_NON_ERASABLE) ||
          (p_bLeads && (m_nErasable == PTConstants.LEADING_TMX_NON_ERASABLE)))
        {
            TmxTagGen.setErasable(false);
        }
        else
        {
            TmxTagGen.setErasable(true);
        }

        if (p_char == '\r')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.LINEBREAK);
            tmp = "\\r";
        }
        else if (p_char == '\n')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.LINEBREAK);
            tmp = "\\n";
        }
        else if (p_char == '\f')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.FORMFEED);
            tmp = "\\f";
        }
        else if (p_char == '\t')
        {
            TmxTagGen.setInlineType(TmxTagGenerator.TAB);
            tmp = "\\t";
        }
        else if ((p_char == ' ') && m_bUseTmxOnLeadingSpaces && p_bLeads )
        {
            TmxTagGen.setInlineType( TmxTagGenerator.SPACE );
            tmp = " ";
        }

        if (tmp.length() > 0)                     // (!tmp.equals(""))
        {
            TmxTagGen.makeTags();
            return ( TmxTagGen.getStart() + tmp + TmxTagGen.getEnd() );
        }
        else
        {
            return ("" + p_char );
        }
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
     * @see PTConstants
     */
    public void setErasable( int p_state )
    {

        if((p_state < 0 ) || (p_state > 3)) return;

        m_nErasable = p_state;

    }
}
