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

import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.Text;
import com.globalsight.util.GlobalSightLocale;

/**
 * PtagStringFormatter is responsible for rendering ptag strings with the
 * proper directionality and adding color formatting when requested.
 */
public class PtagStringFormatter implements PseudoBaseHandler
{
    private static final Logger CATEGORY =
     Logger.getLogger(
      PtagStringFormatter.class.getName());
   
    private static final String PTAG_COLOR_START = "<font color=\"red\">";
    private static final String PTAG_COLOR_END = "</font>";
    private static final String HIGHLIGHT_START = "<FONT COLOR=\"blue\"><B>";
    private static final String HIGHLIGHT_END = "</B></FONT>";
    
    private PseudoParser m_ptagParser = null;
    private StringBuffer m_ptagSegment = null;
    private boolean m_enableLtrPtags = false;
    private boolean m_enableBidiText = false;
    private boolean m_rtlSourceLocale = false;
    private boolean m_rtlTargetLocale = false;
    private String m_findText = null;
    private boolean m_caseSensitiveFind = false;
    private GlobalSightLocale m_locale = null;
  
    /**
     * constructor 
     */
    public PtagStringFormatter()
    {
        super();
        m_ptagParser = new PseudoParser(this);
        m_ptagSegment = new StringBuffer();
    }
    
    /**
     * This method simply colors the ptags. No directionality markup is added.
     *
     * @param p_segment the segment text
     */
    public String htmlPlain(String p_segment) //processSegmentText
        throws PseudoParserException
    {    
        return processSegmentText(p_segment, false, false, null, true, null);
    }

    /**
     * This method colors the ptags and adds LTR 
     * directionality on ptags (only). The segment text is left alone.
     *
     * @param p_segment the segment text
     */
    public String htmlLtrPtags(String p_segment) //processSegmentTextLtrPtags
        throws PseudoParserException
    {    
        return processSegmentText(p_segment, true, false, null, true, null);
    }
    
    /**
     * This method colors the ptags, adds LTR directionality on
     * ptags (only), and highlights the p_findText text substring.
     *
     * @param p_segment the segment text
     * @param p_findText the substring to highlight
     * @param p_caseSensitiveFind true for case sensitive highlighting
     * @param p_locale the locale of the segment
     */
    public String htmlLtrPtags(String p_segment, String p_findText, //processSegmentTextLtrPtags
        boolean p_caseSensitiveFind, GlobalSightLocale p_locale)
            throws PseudoParserException
    {    
        return processSegmentText(p_segment, true, false, p_findText,
            p_caseSensitiveFind, p_locale);
    }

    /**
     * This method colors the ptags, adds LTR directionality on
     * ptags and dynamically determines the directionality of segment text.
     *
     * @param p_segment the segment text
     */
    public String htmlFullBidi(String p_segment) //processSegmentTextFullBidi
            throws PseudoParserException
    {    
        // no highlighting
        return processSegmentText(p_segment, true, true, null, true, null);
    }   
   
    /**
     * Core method that does all the work.
     * @param p_segment the segment text
     * @param  p_ltrPtags enables insertion of markup for LTR ptags
     * @param  p_bidiText enables insertion of markup for RTL/LTR text
     * @param p_findText the substring to highlight
     * @param p_caseSensitiveFind true for case sensitive highlighting
     * @param p_locale the locale of the segment
     */
    private String processSegmentText(String p_segment, boolean p_ltrPtags,
        boolean p_bidiText, String p_findText, boolean p_caseSensitiveFind, 
        GlobalSightLocale p_locale)
            throws PseudoParserException
    {    
        m_enableLtrPtags = p_ltrPtags;
        m_enableBidiText = p_bidiText;
        m_findText = p_findText;
        m_caseSensitiveFind = p_caseSensitiveFind;
        m_locale = p_locale;
       
        m_ptagSegment.setLength(0);
        m_ptagParser.tokenize(p_segment);
       
        return m_ptagSegment.toString();
    }
    
    /**
     * Internal callback method - Parser event handler for ptags
     * @param tagName java.lang.String
     * @param originalString java.lang.String
     */
    public void processTag(String tagName, String originalString)
        throws PseudoParserException
    {
        m_ptagSegment.append(PTAG_COLOR_START);
        m_ptagSegment.append(m_enableLtrPtags ? "<SPAN DIR=\"LTR\" >" : "");
        m_ptagSegment.append(originalString);
        m_ptagSegment.append(m_enableLtrPtags ? "</SPAN>" : "");
        m_ptagSegment.append(PTAG_COLOR_END);
    }
    
    /**
     * Internal callback method - Parser event handler for text
     * @param strText java.lang.String
     */
    public void processText(String strText)
    {        
        boolean isRtl = false;
        
        // bug 7609 : To prevent ptags from being highlighted, we work only on 
        //            the text chunks sent to this text callback method.
        //            Remember, ptag verbose mode could contain real words
        //            (especially with imported xml).
        if(m_findText != null)
        {
            strText = highlight(strText, m_findText, 
                            m_caseSensitiveFind, m_locale);
        }        

        if(m_enableBidiText)
        {
            isRtl = Text.containsBidiChar(strText) ? true : false;
            m_ptagSegment.append(isRtl ? "<SPAN DIR=\"RTL\">" : "<SPAN DIR=\"LTR\">");
        }
        m_ptagSegment.append(strText);
        if(m_enableBidiText)
        {
            m_ptagSegment.append("</SPAN>");
        }
    }
    
    private String highlight(String p_text, String p_findText,
        boolean p_caseSensitiveFind, GlobalSightLocale p_locale)
    {
        if(p_text != null && p_findText != null)
        {            
            if(p_caseSensitiveFind)
            {
                return htmlHighlight(p_text, p_findText);
            }
            else if(p_locale != null)
            {
                return htmlHighlightIgnoreCase(
                    p_text, p_findText, p_locale.getLocale());               
            }
            else
            {
                return p_text;
            }
        }
        else
        {
            return p_text;
        }
    }
    
    /**
     * Case sensitive Highlight all occurences of p_find.
     *
     * @param p_string original String
     * @param p_find the string to be highlighted
     * @return a processed String with html added for highlighting
     */
    private String htmlHighlight(String p_string,
        String p_find)
    {
        StringBuffer replaceWith = new StringBuffer();
        replaceWith.append(HIGHLIGHT_START);
        replaceWith.append(p_find);
        replaceWith.append(HIGHLIGHT_END);

        // here we use the same method that is used by the actual replacement process
        return Text.replaceString(p_string, p_find, replaceWith.toString());
    }
    
    /**
     * Case insensitive Highlight of all occurences of p_find.
     *
     * @param p_string original String
     * @param p_find the string to be replaced
     * @param p_locale locale of which rule is used when ignoring case
     * @return a processed String with html added for highlighting
     */
    private String htmlHighlightIgnoreCase(String p_string,
        String p_find, Locale p_locale)
    {
        // Here we cannot use the same method that is used by the actual 
        // replacement process since that method produces undesired results for
        // hightlighting. Depending on what the users typed, Text.replaceString()
        // can change the case of the original text. In the case of highlighting, 
        // we just want to hightlight it and keep its original mixed case intact.
        // So for now we have local methods to handle case insensistve highlighting.
        return wrapWord(p_string, p_string.toLowerCase(p_locale),
            p_find.toLowerCase(p_locale), HIGHLIGHT_START, HIGHLIGHT_END);
    }

    // Finds occurences of p_caseNormalizedFind in p_caseNormalizedString and
    // highlights them by inserting the p_leadInsert and p_tailInsert formatting .
    //
    // p_caseNormalizedString is a case normalized p_original. 
    // p_caseNormalizedFind string is also case normalized.     
    private String wrapWord(String p_originalString,
        String p_caseNormalizedString, String p_caseNormalizedFind,
        String p_leadInsert,  String p_tailInsert)
    {
        if (p_caseNormalizedFind == null || p_caseNormalizedFind.length()==0)
            return p_originalString;

        StringBuffer buff = new StringBuffer();
        int findStrLength = p_caseNormalizedFind.length();
        int index = 0;
        int prevIndex = 0;
        while((index = p_caseNormalizedString.indexOf(p_caseNormalizedFind, prevIndex)) != -1)
        {
            buff.append(p_originalString.substring(prevIndex, index));
            buff.append(p_leadInsert);            
            buff.append(p_originalString.substring(index, index + findStrLength));
            buff.append(p_tailInsert);            
            prevIndex = index + findStrLength;
        }
        buff.append(p_originalString.substring(prevIndex, p_originalString.length()));
        return buff.toString();
    }
}
