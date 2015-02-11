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



package com.globalsight.everest.edit.offline.rtf;
import org.apache.log4j.Logger;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;

/**
 * A base class responsible for generating RTF (in Unicode format).
 */
public class RTFUnicode
    implements AmbassadorDwUpConstants
{
    private static final Logger s_logger =
        Logger.getLogger(
            RTFUnicode.class);

    /** Tracks the number of bookmarks created. */
    private long m_bookmarkCnt = 0;
    /** Tracks the number of fields created. */
    private long m_fieldCnt = 0;

    public String m_strEOL = "\r\n";
    public static final char NORMALIZED_LINEBREAK = '\n';

    // field modification constants
    protected static final String FLDMOD_DIRTY = "\\flddirty";
    protected static final String FLDMOD_EDIT = "\\fldedit";
    protected static final String FLDMOD_LOCK = "\\fldlock";
    protected static final String FLDMOD_PRIV = "\\fldpriv";

    //
    // Constructor
    //

    public RTFUnicode()
    {
    }

    //
    // Public Methods
    //

    /**
     * Gets the number of calls to makeRTFBookmark.
     */
    public long getBookmarkCnt()
    {
        return m_bookmarkCnt;
    }

    /**
     * Clears the number of calls to makeRTFBookmark.
     */
    public void clearBookmarkCnt()
    {
        m_bookmarkCnt = 0;
    }


    /**
     * Gets the number of calls made to makeRTFField.
     */
    public long getFieldCnt()
    {
        return m_fieldCnt;
    }

    /**
     * Clears the number of calls made to makeRTFField.
     */
    public void clearFieldCnt()
    {
        m_fieldCnt = 0;
    }

    /**
     * Makes the RTF for a bookmark Start or bookmark End.
     *
     * @param p_start if true, a start string is created, otherwise
     * the end string is created.
     * @param p_bmkName the name of the bookmark.
     * @return full rtf token string
     */
    public String makeRTF_bookmark(boolean p_start, String p_bmkName)
    {
        if (p_start)
        {
            m_bookmarkCnt ++;
        }

        StringBuffer sb = new StringBuffer();

        sb.append(p_start ? "{\\*\\bkmkstart " : "{\\*\\bkmkend ");
        sb.append(p_bmkName);
        sb.append("}");

        return sb.toString();
    }

    /**
     * Makes an RTF field code.
     *
     * @param p_dirty A formatting change has been made to the field
     * result since the field was last updated
     * @param p_edit Text has been added to, or removed from, the
     * field result since the field was last updated.
     * @param p_private Result is not in a form suitable for display
     * (for example, binary data used by fields whose result is a
     * picture).
     * @param p_locked Field is locked and cannot be updated.
     * @return full rtf field string
     */
    public String makeRTF_field(boolean p_dirty, boolean p_edit,
        boolean p_private, boolean p_locked, String p_fieldinst,
        String p_fieldrslt)
    {
        StringBuffer sb = new StringBuffer();

        m_fieldCnt ++;

        sb.append("{\\field");
        sb.append(p_dirty ? FLDMOD_DIRTY : "");
        sb.append(p_edit ? FLDMOD_EDIT : "");
        sb.append(p_private ? FLDMOD_PRIV : "");
        sb.append(p_locked ? FLDMOD_LOCK : "");
        sb.append("{\\*\\fldinst");
        sb.append(p_fieldinst);
        sb.append("}");
        sb.append("{\\fieldrslt");
        sb.append(p_fieldrslt);
        sb.append("}");
        sb.append("}");

        return sb.toString();
    }

    protected String makeRTF_docVar(String p_varName, String p_value)
    {
        // {\*\docvar {p_varName}{p_value}}
        StringBuffer sb = new StringBuffer();
        sb.append("{\\*\\docvar {");
        sb.append(p_varName);
        sb.append("}{");
        sb.append(p_value);
        sb.append("}}");
        return sb.toString();
    }

    /**
     * Creates a hyperlink compatible to MS-Word.
     *
     * @param p_url - the url for the link
     * @param p_anchorName - the target link within the url
     * @param p_screenTip - the screen tip text
     * @param p_linkText - visible link text
     * @param p_style - the underlying format style of the link
     * @param p_color - the color of the link
     */
    protected String makeRTF_msWordHyperLink(String p_url,
        String p_anchorName, String p_screenTip, String p_linkText,
        String p_style, String p_color)
    {
        StringBuffer sb = new StringBuffer();

        if (p_url != null && p_anchorName != null &&
            p_linkText != null && p_style != null && p_color != null)
        {
            sb.append("{");
            sb.append("\\field{\\*\\fldinst HYPERLINK ");
            sb.append("\"" + p_url + "\" ");
            sb.append("\\\\l \"" + p_anchorName + "\" ");
            sb.append("\\\\o \"" +
                (p_screenTip == null ? p_linkText : p_screenTip) + "\" ");
            sb.append("}");
            sb.append("{\\fldrslt " + p_style + " \\ul ");
            sb.append("{" + p_color + p_linkText + "}");
            sb.append("}}");
        }

        return sb.toString();
    }

    /**
     * Encodes a Unicode char to a series of RTF escaped chars in
     * unicode (\uDDDDD) where DDDDD is the decimal code of the char.
     */
    public String encodeChar(char p_ch)
    {
        short code = (short)p_ch;

        if (code == 0x5c || code == 0x7b || code == 0x7d) // {,\,}
        {
            return "\\" + p_ch;
        }
        else if (0x20 <= code && code < 0x80)
        {
            return String.valueOf(p_ch);
        }
        else // if (code > 0xff || code < 0)
        {
            return new String("\\u" + code + " ");
        }
    }

    /**
     * Encode a run of text.
     */
    public String encodeText(String p_text)
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 0, max = p_text.length(); i < max; i++)
        {
            sb.append(encodeChar(p_text.charAt(i)));
        }

        return sb.toString();
    }

    /**
     * Escapes the file name path for rtf.
     */
    public String escapeFileName(String p_filename)
    {
        StringBuffer sb = new StringBuffer();

        for (int i = 0, max = p_filename.length(); i < max; i++)
        {
            if (p_filename.charAt(i) =='\\')
            {
                sb.append("\\\\");
            }
            else
            {
                sb.append(p_filename.charAt(i));
            }
        }

        return sb.toString();
    }
}
