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
package com.globalsight.ling.docproc;

import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;

/**
 * Helper class to generate TMX tags with needed attributes.
 */
public class TmxTagGenerator
{
    private String m_strStart;
    private String m_strEnd;
    private int m_iTmxTagType = 5;
    private int m_iInlineType = -1; // used for mainstrean types (bold,
                                    // italic...).
    private String m_strInlineType = ""; // used for format specific type
                                         // (x_yada,x_yadi...).
    private int m_iInternalMatching = 0;
    private int m_iExternalMatching = 0;
    private String m_strPosition = "";
    private boolean m_bErasable = false;
    private boolean m_internalTag = false;
    private boolean m_isFromOfficeContent = false;

    // static TMX inline elements
    public static final int BPT = 0;
    public static final int EPT = 1;
    public static final int SUB = 2;
    public static final int IT = 3;
    public static final int PH = 4;
    public static final int UT = 5;

    private static final String[] m_TmxTagTypes =
    { DiplomatNames.Element.BPT, DiplomatNames.Element.EPT,
            DiplomatNames.Element.SUB, DiplomatNames.Element.IT,
            DiplomatNames.Element.PH, DiplomatNames.Element.UT, };

    // static TMX position types
    public static final int POS_BEGIN = 0;
    public static final int POS_END = 1;

    // static inline types
    public static final int UNKNOWN = 0;
    public static final int BOLD = 1;
    public static final int ITALIC = 2;
    public static final int STRIKE = 3;
    public static final int SUBSCRIPT = 4;
    public static final int SUPERSCRIPT = 5;
    public static final int UNDERLINE = 6;
    public static final int LINK = 7;
    public static final int FONTCHANGE = 8;
    public static final int COLORCHANGE = 9;
    public static final int DOUBLEUNDERLINE = 10;
    public static final int LINEBREAK = 11;
    public static final int COMMENT = 12;
    public static final int JAVASCRIPT = 13;
    public static final int TAB = 14;
    public static final int SPACE = 15;
    public static final int FORMFEED = 16;
    public static final int NBSPACE = 17;
    public static final int VARIABLE = 18;
    // Inline formatting having attributes must be special
    public static final int X_BOLD = 19;
    public static final int X_ITALIC = 20;
    public static final int X_UNDERLINE = 21;

    // To ensure backwards-compatibility, we must add BR as "x-br",
    // since that is the type currently assigned by the HTML
    // Extractor. We should have thought of that in the beginning!
    // X_BR should really be LINEBREAK, but we would have to migrate
    // existing TMs.
    public static final int X_BR = 22;

    public static final int STRONG = 23;
    public static final int X_STRONG = 24;
    public static final int EM = 25;
    public static final int X_EM = 26;

    // capital
    public static final int C_BOLD = 27;
    public static final int C_X_BOLD = 28;
    public static final int C_STRONG = 29;
    public static final int C_X_STRONG = 30;
    public static final int C_ITALIC = 31;
    public static final int C_X_ITALIC = 32;
    public static final int C_UNDERLINE = 33;
    public static final int C_X_UNDERLINE = 34;
    public static final int C_LINK = 35;
    public static final int C_STRIKE = 36;
    public static final int C_SUBSCRIPT = 37;
    public static final int C_SUPERSCRIPT = 38;
    public static final int C_FONTCHANGE = 39;
    public static final int C_EM = 40;
    public static final int C_X_EM = 41;
    public static final int C_LINEBREAK = 42;
    public static final int C_TAB = 43;
    public static final int C_FORMFEED = 44;
    public static final int C_BR = 45;
    public static final int OFFICE_SUPERSCRIPT = 46;
    public static final int OFFICE_HYPERLINK = 47;
    public static final int OFFICE_BOLD = 48;
    public static final int OFFICE_COLOR = 49;
    public static final int OFFICE_ITALIC = 50;
    public static final int OFFICE_UNDERLINE = 51;
    public static final int OFFICE_SUB = 52;
    public static final int OFFICE_SUP = 53;
    public static final int OFFICE_HIGHLIGHT = 54;
    
    // specific for html
    private static final String[] m_InlineTypes =
    { "x-unknown", "bold", "italic", "x-strike", "x-sub", "x-super", "ulined",
            "link", "font", "color", "dulined",
            "lb",
            "x-comment",
            "x-javascript",
            "x-tab",
            "x-space", // javprop leading spaces
            "x-formfeed",
            "x-nbspace",
            "x-variable",
            // special inline formatting types
            "x-bold", "x-italic", "x-ulined", "x-br", "strong", "x-strong",
            "em", "x-em", "c-bold", "c-x-bold", "c-strong", "c-x-strong",
            "c-italic", "c-x-italic", "c-ulined", "c-x-ulined", "c-link",
            "c-strike", "c-sub", "c-super", "c-font", "c-em", "c-x-em", "c-lb",
            "c-tab", "c-formfeed", "c-br", "superscript","hyperlink", "office-bold",
            "color", "office-italic", "office-underline", "office-sub", "office-sup", "highlight"};

    public TmxTagGenerator()
    {
        super();
    }

    public String getEnd()
    {
        return m_strEnd;
    }

    public String getStart()
    {
        return m_strStart;
    }

    public void setEnd(String end)
    {
        m_strEnd = end;
    }

    public void setStart(String start)
    {
        m_strStart = start;
    }

    public void makeTags()
    {
        StringBuffer sb = new StringBuffer();

        String strInlineType = "";
        String strInternal = "";
        String strTmxTagType;
        String strInternalMatchingAtt = "";
        String strExternalMatchingAtt = "";
        String strPositionAtt = "";
        String strErasable = "";
        String strIsFromOfficeContent = "";

        // build type (type) attribute
        switch (m_iTmxTagType)
        {
            case BPT:
            case IT:
            case PH:
                if (m_iInlineType != -1)
                {
                    strInlineType = " type=\"" + m_InlineTypes[m_iInlineType]
                            + "\"";
                }
                else if (m_strInlineType != null
                        && m_strInlineType.length() > 0)
                {
                    strInlineType = " type=\"" + m_strInlineType + "\"";
                }
                else
                {
                    strInlineType = "";
                }

                break;
            case EPT:
            case SUB:
            case UT:
                break;
            default:
                break;
        }

        // build erasable attribute
        switch (m_iTmxTagType)
        {
            case BPT:
            case IT:
            case PH:
            case UT:
                if (m_bErasable)
                {
                    strErasable = " erasable=\"yes\"";
                }
                break;
            default:
                break;
        }

        // build internal matching (i) attribute
        switch (m_iTmxTagType)
        {
            case BPT:
            case EPT:
                if (m_iInternalMatching != 0)
                {
                    strInternalMatchingAtt = " i=\"" + m_iInternalMatching
                            + "\"";
                }
                break;
            default:
                break;
        }

        // build external matching (x) attribute
        switch (m_iTmxTagType)
        {
            // isolated tags only for now
            case IT:
                if (m_iExternalMatching != 0)
                {
                    strExternalMatchingAtt = " x=\"" + m_iExternalMatching
                            + "\"";
                }
                break;
            default:
                break;
        }

        // build position (pos) attribute
        switch (m_iTmxTagType)
        {
            case IT:
                strPositionAtt = " pos=\"" + m_strPosition + "\"";
                break;
            default:
                break;
        }
        strTmxTagType = m_TmxTagTypes[m_iTmxTagType];

        if (m_internalTag)
        {
            strInternal = " internal=\"yes\"";
        }
        if (m_isFromOfficeContent)
        {
            strIsFromOfficeContent = OfficeContentPostFilterHelper.IS_FROM_OFFICE_CONTENT;
        }

        // Build start tag
        sb.append("<");
        sb.append(strTmxTagType);
        sb.append(strInlineType);
        sb.append(strInternal);
        sb.append(strInternalMatchingAtt);
        sb.append(strExternalMatchingAtt);
        sb.append(strPositionAtt);
        sb.append(strErasable);
        sb.append(strIsFromOfficeContent);
        sb.append(">");

        m_strStart = sb.toString();

        sb.setLength(0);

        // Build end tag
        sb.append("</");
        sb.append(strTmxTagType);
        sb.append(">");

        m_strEnd = sb.toString();
    }

    public void resetAll()
    {
        m_strStart = "";
        m_strEnd = "";
        m_iTmxTagType = 5;
        m_iInlineType = -1;
        m_strInlineType = "";
        m_iInternalMatching = 0;
        m_strPosition = "";
        m_bErasable = false;
        m_isFromOfficeContent = false;
    }

    public void setFromOfficeContent(boolean isFromOfficeContent)
    {
        m_isFromOfficeContent = isFromOfficeContent;
    }

    public void setErasable(boolean p_bErasable)
    {
        m_bErasable = p_bErasable;
    }

    public void setExternalMatching(int p_iEM)
    {
        m_iExternalMatching = p_iEM;
    }

    public void setInlineType(int p_iInlineType)
    {
        m_iInlineType = p_iInlineType;
    }

    public void setInlineType(String p_strInlineType)
    {
        m_strInlineType = p_strInlineType;
    }

    public void setInternalMatching(int p_iIM)
    {
        m_iInternalMatching = p_iIM;
    }

    public void setPosition(int p_nPosition)
    {
        // the only legal values for the pos attribute are "", "begin"
        // and "end"
        if (p_nPosition == POS_BEGIN)
        {
            m_strPosition = "begin";
        }
        else if (p_nPosition == POS_END)
        {
            m_strPosition = "end";
        }
        else
        {
            m_strPosition = "";
        }
    }

    public void setTagType(int p_iTmxType)
    {
        m_iTmxTagType = p_iTmxType;
    }

    /**
     * Get a legal TMX name via it's ID. Return null if ID out of range.
     * 
     * @return java.lang.String
     * @param p_typeId
     *            int
     */
    public String getTmxName(int p_typeId)
    {
        if (p_typeId < BPT || p_typeId > UT)
        {
            return null;
        }

        return m_TmxTagTypes[p_typeId];
    }

    /**
     * Get a TMX type name via it's ID. Return null if ID out of range.
     * 
     * @return java.lang.String
     * @param p_typeId
     *            int
     */
    public static String getInlineTypeName(int p_ID)
    {
        if (p_ID >= m_InlineTypes.length)
        {
            return null;
        }

        return m_InlineTypes[p_ID];
    }

    public boolean isInternalTag()
    {
        return m_internalTag;
    }

    public void setInternalTag(boolean tag)
    {
        m_internalTag = tag;
    }
}
