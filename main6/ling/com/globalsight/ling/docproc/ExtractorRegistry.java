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

import java.util.HashMap;

public class ExtractorRegistry implements IFormatNames
{
    private static final HashMap m_formatAliasTable = mapFormatAliases();

    private static ExtractorRegistry m_self = null;

    // Extractor class names. Update when we add a new extractor type.
    // *** Keep this table in sync with m_DecoderClassPath ***
    private String[] m_ExtractorClassPath =
    {
            "com.globalsight.ling.docproc.extractor.plaintext.Extractor",
            "com.globalsight.ling.docproc.extractor.html.Extractor",
            "com.globalsight.ling.docproc.extractor.javascript.Extractor",
            "com.globalsight.ling.docproc.extractor.java.Extractor",
            "com.globalsight.ling.docproc.extractor.css.Extractor",
            "com.globalsight.ling.docproc.extractor.css.StyleExtractor",
            "com.globalsight.ling.docproc.extractor.xml.XmlExtractor",
            "com.globalsight.ling.docproc.extractor.xml.Extractor",
            "com.globalsight.ling.docproc.extractor.javaprop.Extractor",
            "com.globalsight.ling.docproc.extractor.javaprop.HtmlExtractor",
            "com.globalsight.ling.docproc.extractor.javaprop.MsgExtractor",
            "com.globalsight.ling.docproc.extractor.vbscript.Extractor",
            "com.globalsight.ling.docproc.extractor.cfscript.Extractor",
            "com.globalsight.ling.docproc.extractor.html.CFExtractor",
            "com.globalsight.ling.docproc.extractor.html.JHTMLExtractor",
            "com.globalsight.ling.docproc.extractor.html.ASPExtractor",
            "com.globalsight.ling.docproc.extractor.html.JSPExtractor",
            "com.globalsight.ling.docproc.extractor.cpp.Extractor",
            "com.globalsight.ling.docproc.extractor.rtf.Extractor",
            "com.globalsight.ling.docproc.extractor.sgml.Extractor",
            "com.globalsight.ling.docproc.extractor.xptag.Extractor",
            "com.globalsight.ling.docproc.extractor.troff.Extractor",
            "com.globalsight.ling.docproc.extractor.msoffice.ExcelExtractor",
            "com.globalsight.ling.docproc.extractor.msoffice.WordExtractor",
            "com.globalsight.ling.docproc.extractor.msoffice.PowerPointExtractor",
            "com.globalsight.ling.docproc.extractor.ebay.SgmlExtractor",
            "com.globalsight.ling.docproc.extractor.ebay.PrjExtractor",
            "com.globalsight.ling.docproc.extractor.xliff.Extractor",
            "com.globalsight.ling.docproc.extractor.openoffice.OpenOfficeExtractor",
            "com.globalsight.ling.docproc.extractor.po.Extractor",
            "com.globalsight.ling.docproc.extractor.msoffice.OfficeXmlExtractor",
            "com.globalsight.ling.docproc.extractor.rc.Extractor",
            "com.globalsight.ling.docproc.extractor.xml.XmlExtractor",
            "com.globalsight.ling.docproc.extractor.idml.IdmlExtractor",
            "com.globalsight.ling.docproc.extractor.fm.Extractor",
            "com.globalsight.ling.docproc.extractor.passolo.PassoloExtractor",
            "com.globalsight.ling.docproc.extractor.windowspe.WindowsPEExtractor",
            "com.globalsight.ling.docproc.extractor.xml.XmlExtractor",
            "com.globalsight.ling.docproc.extractor.xliff20.Extractor" };

    // Codec class names.
    // *** Keep this table in sync with m_ExtractorClassPath ***
    private String[] m_DecoderClassPath =
    { "com.globalsight.ling.common.PTEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.JSEscapeSequence",
            "com.globalsight.ling.common.JSEscapeSequence",
            "com.globalsight.ling.common.CssEscapeSequence",
            "com.globalsight.ling.common.CssEscapeSequence",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.JPEscapeSequence",
            "com.globalsight.ling.common.JPEscapeSequence",
            "com.globalsight.ling.common.JPMFEscapeSequence",
            "com.globalsight.ling.common.VBEscapeSequence",
            "com.globalsight.ling.common.CFEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.CppEscapeSequence",
            "com.globalsight.ling.common.RtfEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XPTagEnDecoder",
            "com.globalsight.ling.common.TroffEnDecoder",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.HtmlEscapeSequence",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.PTEscapeSequence",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.PTEscapeSequence",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.MifEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder",
            "com.globalsight.ling.common.XmlEnDecoder" };

    // Post merge process class names
    // *** Keep this table in sync with m_FormatName ***
    private String[] m_PostMergeClassPath =
    {
            "com.globalsight.ling.docproc.merger.plaintext.PlaintextPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.html.HtmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.html.HtmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.html.HtmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.asp.AspPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.jsp.JspPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xptag.XptagPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.ebay.SgmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.ebay.PrjPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.plaintext.PlaintextPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.fm.FmPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.NoOpPostMergeProcessor",
            "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor" };

    // format names used in diplomat
    private String[] m_FormatName =
    { FORMAT_PLAINTEXT, FORMAT_HTML, FORMAT_JAVASCRIPT, FORMAT_JAVA,
            FORMAT_CSS, FORMAT_CSS_STYLE, FORMAT_XML, FORMAT_XSL,
            FORMAT_JAVAPROP, FORMAT_JAVAPROP_HTML, FORMAT_JAVAPROP_MSG,
            FORMAT_VBSCRIPT, FORMAT_CFSCRIPT, FORMAT_CF, FORMAT_JHTML,
            FORMAT_ASP, FORMAT_JSP, FORMAT_CPP, FORMAT_RTF, FORMAT_SGML,
            FORMAT_XPTAG, FORMAT_TROFF_MAN, FORMAT_EXCEL_HTML,
            FORMAT_WORD_HTML, FORMAT_POWERPOINT_HTML, FORMAT_EBAY_SGML,
            FORMAT_EBAY_PRJ, FORMAT_XLIFF, FORMAT_OPENOFFICE_XML, FORMAT_PO,
            FORMAT_OFFICE_XML, FORMAT_RC, FORMAT_RESX, FORMAT_IDML, FORMAT_MIF,
            FORMAT_PASSOLO, FORMAT_WINDOWSPE, FORMAT_AUTHORIT_XML,
            FORMAT_XLIFF20 };

    /** Singleton instance, call getObject(). */
    private ExtractorRegistry()
    {
    }

    /**
     * Get singleton instances of our class.
     * 
     * @author: Jim Hargrave Creation date: (8/4/2000 2:31:18 PM)
     * @return com.globalsight.ling.docproc.ExtractorRegistry
     */
    public static synchronized ExtractorRegistry getObject()
    {
        if (m_self == null)
        {
            m_self = new ExtractorRegistry();
        }

        return m_self;
    }

    /**
     * Get class path to format specific decoder
     * 
     * @author: Jim Hargrave Creation date: (8/4/2000 4:03:29 PM)
     * @return java.lang.String
     */
    public String getDecoderClasspath(int p_iDecoderId)
    {
        return m_DecoderClassPath[p_iDecoderId];
    }

    /**
     * Get class path to format specific extractor.
     * 
     * @author: Jim Hargrave Creation date: (8/4/2000 4:02:57 PM)
     * @return java.lang.String
     */
    public String getExtractorClasspath(int p_iExtractorId)
    {
        return m_ExtractorClassPath[p_iExtractorId];
    }

    /**
     * Get class path to post merge processor.
     * 
     * @param p_formatId
     *            format id of the processor. It can be obtained by
     *            getFormatId()
     * @return fully qualified class path name
     */
    public String getPostMergeClasspath(int p_formatId)
    {
        return m_PostMergeClassPath[p_formatId];
    }

    /**
     * Get unique id (index) for the format. id points to an extractor that will
     * handle the format.
     * 
     * @author: Jim Hargrave Creation date: (8/4/2000 4:01:25 PM)
     * @return int
     */
    public int getFormatId(String p_strFormatName)
    {
        String format = (String) m_formatAliasTable.get(p_strFormatName);

        if (format != null)
        {
            p_strFormatName = format;
        }

        int i = 0;
        while (i < m_FormatName.length)
        {
            if (p_strFormatName.compareToIgnoreCase(m_FormatName[i]) == 0)
            {
                return i;
            }

            i++;
        }

        return -1; // string not found
    }

    public String getFormatName(int p_iFormatId)
    {
        return m_FormatName[p_iFormatId];
    }

    /**
     * Return true if a valid, registered format.
     * 
     * @author: Jim Hargrave Creation date: (8/4/2000 4:02:07 PM)
     * @return boolean
     * @param p_strFormat
     *            java.lang.String
     */
    public boolean isValidFormat(String p_strFormat)
    {
        return (getFormatId(p_strFormat) == -1) ? false : true;
    }

    private static HashMap mapFormatAliases()
    {
        HashMap h = new HashMap();
        h.put("text", FORMAT_PLAINTEXT);
        return h;
    }
}
