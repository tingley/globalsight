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


public class ExtractorRegistry
    implements IFormatNames
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
        "com.globalsight.ling.docproc.extractor.xml.Extractor",             // for xml
        "com.globalsight.ling.docproc.extractor.xml.Extractor",             // for xsl
        "com.globalsight.ling.docproc.extractor.javaprop.Extractor",        // JavaProperties
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
        "com.globalsight.ling.docproc.extractor.troff.Extractor",           // TROFF [21]

        "com.globalsight.ling.docproc.extractor.msoffice.ExcelExtractor",
        "com.globalsight.ling.docproc.extractor.msoffice.WordExtractor",
        "com.globalsight.ling.docproc.extractor.msoffice.PowerPointExtractor",

        "com.globalsight.ling.docproc.extractor.ebay.SgmlExtractor",
        "com.globalsight.ling.docproc.extractor.ebay.PrjExtractor",
        "com.globalsight.ling.docproc.extractor.xliff.Extractor",
        "com.globalsight.ling.docproc.extractor.openoffice.OpenOfficeExtractor",
        "com.globalsight.ling.docproc.extractor.po.Extractor",              // PO [29]
        "com.globalsight.ling.docproc.extractor.msoffice.OfficeXmlExtractor",
        "com.globalsight.ling.docproc.extractor.rc.Extractor",
        "com.globalsight.ling.docproc.extractor.xml.Extractor",             // for resx
        "com.globalsight.ling.docproc.extractor.idml.IdmlExtractor"             // IDML
        };

    // Codec class names.
    // *** Keep this table in sync with m_ExtractorClassPath ***
    private String[] m_DecoderClassPath =
        {
        "com.globalsight.ling.common.PTEscapeSequence",
        "com.globalsight.ling.common.HtmlEscapeSequence",
        "com.globalsight.ling.common.JSEscapeSequence",
        "com.globalsight.ling.common.JSEscapeSequence",  // for Java
        "com.globalsight.ling.common.CssEscapeSequence",
        "com.globalsight.ling.common.CssEscapeSequence", // list twice
        "com.globalsight.ling.common.XmlEnDecoder",
        "com.globalsight.ling.common.XmlEnDecoder",      // same for XSL
        "com.globalsight.ling.common.JPEscapeSequence",  // JavaProperties
        "com.globalsight.ling.common.JPEscapeSequence",  // list twice
        "com.globalsight.ling.common.JPMFEscapeSequence",
        "com.globalsight.ling.common.VBEscapeSequence",
        "com.globalsight.ling.common.CFEscapeSequence",
        "com.globalsight.ling.common.HtmlEscapeSequence", // ColdFusion
        "com.globalsight.ling.common.HtmlEscapeSequence", // JHTML
        "com.globalsight.ling.common.HtmlEscapeSequence", // ASP
        "com.globalsight.ling.common.HtmlEscapeSequence", // JSP
        "com.globalsight.ling.common.CppEscapeSequence",  // CPP
        "com.globalsight.ling.common.RtfEnDecoder",       // RTF
        "com.globalsight.ling.common.XmlEnDecoder",       // SGML
        "com.globalsight.ling.common.XPTagEnDecoder",     // XPTAG
        "com.globalsight.ling.common.TroffEnDecoder",     // TROFF [21]

        "com.globalsight.ling.common.HtmlEscapeSequence", // Excel (HTML)
        "com.globalsight.ling.common.HtmlEscapeSequence", // Word (HTML)
        "com.globalsight.ling.common.HtmlEscapeSequence", // PPT (HTML)

        "com.globalsight.ling.common.HtmlEscapeSequence", // EBay SGML
        "com.globalsight.ling.common.XmlEnDecoder",       // EBay PRJ
        "com.globalsight.ling.common.XmlEnDecoder",       // EBay PRJ
        "com.globalsight.ling.common.XmlEnDecoder",       // openoffice xml
        "com.globalsight.ling.common.PTEscapeSequence",   // PO [29]
        "com.globalsight.ling.common.XmlEnDecoder",       // Office (XML)
        "com.globalsight.ling.common.PTEscapeSequence",   // RC
        "com.globalsight.ling.common.XmlEnDecoder"        // IDML
        };

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
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",      // JavaProperties
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
        "com.globalsight.ling.docproc.merger.html.HtmlPostMergeProcessor", // ColdFusion
        "com.globalsight.ling.docproc.merger.html.HtmlPostMergeProcessor", // JHTML
        "com.globalsight.ling.docproc.merger.asp.AspPostMergeProcessor", // ASP
        "com.globalsight.ling.docproc.merger.jsp.JspPostMergeProcessor", // JSP
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // CPP
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // RTF
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // SGML
        "com.globalsight.ling.docproc.merger.xptag.XptagPostMergeProcessor", // XPTAG
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // TROFF

        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // Excel
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // Word
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // PPT

        "com.globalsight.ling.docproc.merger.ebay.SgmlPostMergeProcessor", // EBay SGML
        "com.globalsight.ling.docproc.merger.ebay.PrjPostMergeProcessor", // EBay PRJ
        "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor",
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor", // openoffice xml
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",
        "com.globalsight.ling.docproc.merger.NoOpPostMergeProcessor",    // Office (XML)
        "com.globalsight.ling.docproc.merger.plaintext.PlaintextPostMergeProcessor", //RC
        "com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor"  // IDML
        };

    // format names used in diplomat
    private String[] m_FormatName =
    {
        FORMAT_PLAINTEXT,
        FORMAT_HTML,
        FORMAT_JAVASCRIPT,
        FORMAT_JAVA,
        FORMAT_CSS,
        FORMAT_CSS_STYLE,
        FORMAT_XML,
        FORMAT_XSL,
        FORMAT_JAVAPROP,                // JavaProperties
        FORMAT_JAVAPROP_HTML,
        FORMAT_JAVAPROP_MSG,
        FORMAT_VBSCRIPT,
        FORMAT_CFSCRIPT,
        FORMAT_CF,
        FORMAT_JHTML,
        FORMAT_ASP,
        FORMAT_JSP,
        FORMAT_CPP,
        FORMAT_RTF,
        FORMAT_SGML,
        FORMAT_XPTAG,
        FORMAT_TROFF_MAN,

        FORMAT_EXCEL_HTML,
        FORMAT_WORD_HTML,
        FORMAT_POWERPOINT_HTML,

        FORMAT_EBAY_SGML,
        FORMAT_EBAY_PRJ,
        FORMAT_XLIFF,
        FORMAT_OPENOFFICE_XML,
        FORMAT_PO,
        FORMAT_OFFICE_XML,
        FORMAT_RC,
        FORMAT_RESX,
        FORMAT_IDML
    };


    /** Singleton instance, call getObject(). */
    private ExtractorRegistry()
    {
    }


    /**
     * Get singleton instances of our class.
     * @author: Jim Hargrave
     * Creation date: (8/4/2000 2:31:18 PM)
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
     * @author: Jim Hargrave
     * Creation date: (8/4/2000 4:03:29 PM)
     * @return java.lang.String
     */
    public String getDecoderClasspath(int p_iDecoderId)
    {
        return m_DecoderClassPath[p_iDecoderId];
    }


    /**
     * Get class path to format specific extractor.
     * @author: Jim Hargrave
     * Creation date: (8/4/2000 4:02:57 PM)
     * @return java.lang.String
     */
    public String getExtractorClasspath(int p_iExtractorId)
    {
        return m_ExtractorClassPath[p_iExtractorId];
    }


    /**
     * Get class path to post merge processor.
     * @param p_formatId format id of the processor. It can be
     * obtained by getFormatId()
     * @return fully qualified class path name
     */
    public String getPostMergeClasspath(int p_formatId)
    {
        return m_PostMergeClassPath[p_formatId];
    }


    /**
     * Get unique id (index) for the format.  id points to an
     * extractor that will handle the format.
     * @author: Jim Hargrave
     * Creation date: (8/4/2000 4:01:25 PM)
     * @return int
     */
    public int getFormatId(String p_strFormatName)
    {
        String format = (String)m_formatAliasTable.get(p_strFormatName);

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
     * @author: Jim Hargrave
     * Creation date: (8/4/2000 4:02:07 PM)
     * @return boolean
     * @param p_strFormat java.lang.String
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
