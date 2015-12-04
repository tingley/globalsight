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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.adobe.InddTuMappingHelper;
import com.globalsight.cxe.adapter.idml.IdmlHelper;
import com.globalsight.cxe.adapter.ling.ExtractRule;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.filterconfiguration.EscapingHelper;
import com.globalsight.cxe.entity.filterconfiguration.Filter;
import com.globalsight.cxe.entity.filterconfiguration.FilterConstants;
import com.globalsight.cxe.entity.filterconfiguration.FilterHelper;
import com.globalsight.cxe.entity.filterconfiguration.HtmlFilter;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.cxe.entity.filterconfiguration.JSPFilter;
import com.globalsight.cxe.entity.filterconfiguration.JavaPropertiesFilter;
import com.globalsight.cxe.entity.filterconfiguration.POFilter;
import com.globalsight.cxe.entity.filterconfiguration.XMLRuleFilter;
import com.globalsight.cxe.entity.filterconfiguration.XmlFilterConfigParser;
import com.globalsight.cxe.entity.knownformattype.KnownFormatType;
import com.globalsight.cxe.entity.knownformattype.KnownFormatTypeImpl;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.segmentationhelper.Segmentation;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.common.CodesetMapper;
import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.ling.common.TranscoderException;
import com.globalsight.ling.docproc.extractor.html.Extractor;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterChecker;
import com.globalsight.ling.docproc.extractor.xml.XmlFilterHelper;
import com.globalsight.ling.docproc.merger.PostMergeProcessor;
import com.globalsight.ling.docproc.merger.fm.FMPreviewerHelper;
import com.globalsight.ling.docproc.merger.fm.FmPostMergeProcessor;
import com.globalsight.ling.docproc.merger.fm.FontMappingHelper;
import com.globalsight.ling.docproc.merger.html.HtmlPreviewerHelper;
import com.globalsight.ling.docproc.merger.jsp.JspPostMergeProcessor;
import com.globalsight.ling.docproc.merger.xml.XmlPostMergeProcessor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.EmojiUtil;

/**
 * The public API for the GXML Extractor Framework.
 * 
 * <p>
 * The framework processes different input file formats such as HTML, XML,
 * JavaScript, CSS, Java Property files, plain text, and C++. From these input
 * files it extracts the skeleton and all translatable and localizable pieces of
 * text and marks them up with GXML tags (TMX tags).
 * </p>
 * 
 * <p>
 * The result of this extraction process is an XML document that conforms to the
 * GXML DTD.
 * </p>
 * 
 * <p>
 * The format of an input file can be specified with
 * {@link #setInputFormat(String) setInputFormat()}, or automatically derived
 * from the file extension. Known extensions are:
 * </p>
 * 
 * <dl>
 * <dt><b>htm, html, dhtml</b>
 * <dd>HTML files
 * <dt><b>css</b>
 * <dd>Cascaded Style Sheet
 * <dt><b>js</b>
 * <dd>JavaScript
 * <dt><b>properties</b>
 * <dd>Java property file
 * <dt><b>xml</b>
 * <dd>XML file
 * <dt><b>xsl, xslt</b>
 * <dd>XSL(T) style sheet
 * <dt><b>cfm</b>
 * <dd>Cold Fusion file
 * <dt><b>txt, text</b>
 * <dd>Text file
 * </dl>
 * 
 * <p>
 * The XML Extractor uses a rules file to determine which parts of the input are
 * translatable and localizable (no other Extractor makes use of a rules file at
 * this point). The XML rules must conform to the schemarules.rng.
 * 
 * <p>
 * To extract from a String, use this code:
 * </p>
 * 
 * <PRE>
 * DiplomatAPI diplomat = new DiplomatAPI();
 * 
 * diplomat.setSourceString(str_inputString);
 * diplomat.setInputFormat(str_format);
 * diplomat.setEncoding(str_encoding);
 * diplomat.setLocale(str_locale);
 * 
 * String strDiplomatXml = diplomat.extract();
 * </PRE>
 * 
 * <P>
 * To extract from a file, use code like this:
 * </p>
 * 
 * <PRE>
 * DiplomatAPI diplomat = new DiplomatAPI();
 * 
 * diplomat.setSourceFile(&quot;../tests/test.xml&quot;);
 * // The input format is determined from the file extension.
 * // If the file has an unusual extension, set the format manually:
 * // diplomat.setInputFormat(DiplomatAPI.FORMAT_XML);
 * diplomat.setRuleFile(&quot;../tests/test.rules&quot;);
 * diplomat.setEncoding(&quot;iso-8859-1&quot;);
 * diplomat.setLocale(&quot;en_US&quot;);
 * 
 * String strDiplomatXml = diplomat.extract();
 * 
 * Writer writer = new BufferedWriter(new OutputStreamWriter(System.out, &quot;UTF-8&quot;));
 * 
 * writer.write(strDiplomatXml);
 * writer.close();
 * </PRE>
 * 
 * @see com.globalsight.ling.docproc.extractor.css.Extractor CSS Extractor
 * @see com.globalsight.ling.docproc.extractor.html.Extractor HTML Extractor
 * @see com.globalsight.ling.docproc.extractor.javaprop.Extractor JavaProp
 *      Extractor
 * @see com.globalsight.ling.docproc.extractor.javascript.Extractor JavaScript
 *      Extractor
 * @see com.globalsight.ling.docproc.extractor.plaintext.Extractor PlainText
 *      Extractor
 * @see com.globalsight.ling.docproc.extractor.xml.Extractor XML Extractor
 * @see com.globalsight.ling.docproc.extractor.cpp.Extractor C++ Extractor
 * @see <A HREF="../../../../diplomat.dtd.txt">GXML DTD</A>
 * @see <A HREF="www.lisa.org/tmx">TMX</A>
 */
public class DiplomatAPI implements IFormatNames
{
    private static final Logger logger = Logger.getLogger(DiplomatAPI.class
            .getName());

    private List<ExtractRule> rules = new ArrayList<ExtractRule>();

    private AbstractExtractor extractor = null;
    public static final String m_tag_internal_start = "&lt;GS-INTERNAL-TEXT&gt;";
    public static final String m_tag_internal_end = "&lt;/GS-INTERNAL-TEXT&gt;";
    public static final String tag_internal_start = "<GS-INTERNAL-TEXT>";
    public static final String tag_internal_end = "</GS-INTERNAL-TEXT>";

    /**
     * Diplomat Extractor Options.
     */
    static public class Options
    {
        /**
         * If true, validate the generated GXML string against the GXML DTD and
         * raise an exception on error. Default is false.
         */
        public boolean m_validateGxml = false;

        /**
         * If true, perform sentence segmentation on paragraphs. Default is
         * true.
         */
        public boolean m_sentenceSegmentation = true;

        /**
         * If true, the HTML extractor will simplify segments by moving
         * surrounding tags into the skeleton. The page-update code can set this
         * to false in order not to modify segments that initial extraction or
         * the user have created. Default is true.
         */
        public boolean m_extractorSimplify = true;

        /**
         * If true, the JS extractor will submit strings that look like HTML to
         * the HTML extractor. If false, the JS extractor will extract all
         * strings as dataformat=javascript, type=string. Default is true.
         */
        public boolean m_canCallOtherExtractor = true;

        /**
         * If true, the segmenter replaces TMX tags representing whitespace with
         * actual whitespace during segmentation (correct behavior). If false,
         * such tags are treated as non-existent (traditional behavior up to
         * 4.4.x). Default is false to prevent migration issues
         * (backwards-compatibility).
         */
        public boolean m_segmenterPreserveWhitespace = false;

        /**
         * An integer specifying as how many words a localizable should be
         * counted. If 1, each localizable counts as 1 word; if 0, localizables
         * are not counted.
         */
        public int m_localizableWordCount = 1;

        public Options()
        {
            m_validateGxml = s_validateGxml;
            m_sentenceSegmentation = s_sentenceSegmentation;
            m_extractorSimplify = s_extractorSimplify;
            m_canCallOtherExtractor = s_canCallOtherExtractor;
            m_segmenterPreserveWhitespace = s_segmenterPreserveWhitespace;
            m_localizableWordCount = s_localizableWordCount;
        }
    }

    // Global Default Options (read once)
    static public boolean s_validateGxml = false;
    static public boolean s_sentenceSegmentation = true;
    static public boolean s_extractorSimplify = true;
    static public boolean s_canCallOtherExtractor = true;
    static public boolean s_segmenterPreserveWhitespace = false;
    static public int s_localizableWordCount = 1;

    static
    {
        try
        {
            ResourceBundle res = ResourceBundle.getBundle(
                    "properties/Diplomat", Locale.US);

            try
            {
                if (res.getString("validate_diplomat_xml").equalsIgnoreCase(
                        "true"))
                    s_validateGxml = true;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                if (res.getString("validate_gxml").equalsIgnoreCase("true"))
                    s_validateGxml = true;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                if (res.getString("segmentation").equalsIgnoreCase("paragraph"))
                    s_sentenceSegmentation = false;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                if (res.getString("segmentation_preserve_whitespace")
                        .equalsIgnoreCase("true"))
                    s_segmenterPreserveWhitespace = true;
            }
            catch (MissingResourceException e)
            {
            }

            try
            {
                if (res.getString("wordcount_localizables").equalsIgnoreCase(
                        "true"))
                    s_localizableWordCount = 1;
                else
                    s_localizableWordCount = 0;
            }
            catch (MissingResourceException e)
            {
            }
        }
        catch (MissingResourceException e)
        {
            // Do nothing if configuration file was not found.
        }
    }

    private static final Locale sm_defaultLocale = Locale.US;
    private static final String sm_defaultEncoding = "8859_1";
    private static Logger c_category = Logger.getLogger(DiplomatAPI.class);

    /**
     * <p>
     * The system-internal location of the GXML DTD. It names a file stored
     * inside the diplomat jar or the file system (default:
     * <CODE>"diplomat.dtd"</CODE>) and can be retrieved with
     * <CODE>CLASS.class.getResourceAsStream(DIPLOMAT_DTD);</CODE>
     * </p>
     * 
     * @see <A href="../../../../diplomat.dtd.txt">diplomat.dtd</A>
     */
    public static final String DIPLOMAT_INTERNAL_DTD = "diplomat.dtd";

    /**
     * <p>
     * The official URL to a public web site from which the GXML DTD can be
     * retrieved. Its value is
     * <CODE>"http://globalsight.com/dtds/diplomat.dtd"</CODE>
     * </p>
     */
    public static final String DIPLOMAT_DTD = "http://globalsight.com/dtds/diplomat.dtd";

    /**
     * <p>
     * The SGML document type declaration of a Diplomat XML file.
     * </p>
     */
    public static final String DIPLOMAT_DOCTYPE_DECL = "<!DOCTYPE diplomat SYSTEM \""
            + DIPLOMAT_DTD + "\">";

    /**
     * <p>
     * A map from a file extension to file format string
     * </p>
     */
    private static HashMap<String, String> sm_formatMap = null;
    static
    {
        sm_formatMap = new HashMap<String, String>();

        sm_formatMap.put("htm", FORMAT_HTML);
        sm_formatMap.put("html", FORMAT_HTML);
        sm_formatMap.put("dhtml", FORMAT_HTML);
        sm_formatMap.put("jhtml", FORMAT_JHTML);
        sm_formatMap.put("cfm", FORMAT_CF);
        sm_formatMap.put("css", FORMAT_CSS);
        sm_formatMap.put("js", FORMAT_JAVASCRIPT);
        sm_formatMap.put("vb", FORMAT_VBSCRIPT);
        sm_formatMap.put("vbs", FORMAT_VBSCRIPT);
        sm_formatMap.put("xml", FORMAT_XML);
        sm_formatMap.put("xxml", FORMAT_XML);
        sm_formatMap.put("resx", FORMAT_RESX);
        sm_formatMap.put("xsl", FORMAT_XSL);
        sm_formatMap.put("xslt", FORMAT_XSL);
        sm_formatMap.put("properties", FORMAT_JAVAPROP);
        sm_formatMap.put("txt", FORMAT_PLAINTEXT);
        sm_formatMap.put("text", FORMAT_PLAINTEXT);
        sm_formatMap.put("jsp", FORMAT_JSP);
        sm_formatMap.put("asp", FORMAT_ASP);
        sm_formatMap.put("c++", FORMAT_CPP);
        sm_formatMap.put("cpp", FORMAT_CPP);
        sm_formatMap.put("h", FORMAT_CPP);
        sm_formatMap.put("hpp", FORMAT_CPP);
        sm_formatMap.put("java", FORMAT_JAVA);
        sm_formatMap.put("rtf", FORMAT_RTF);
        sm_formatMap.put("sgm", FORMAT_SGML);
        sm_formatMap.put("sgml", FORMAT_SGML);
        sm_formatMap.put("xtg", FORMAT_XPTAG);

        // MSOffice extractors
        sm_formatMap.put("xls-htm", FORMAT_EXCEL_HTML);
        sm_formatMap.put("xls-html", FORMAT_EXCEL_HTML);
        sm_formatMap.put("doc-htm", FORMAT_WORD_HTML);
        sm_formatMap.put("doc-html", FORMAT_WORD_HTML);
        sm_formatMap.put("ppt-htm", FORMAT_POWERPOINT_HTML);
        sm_formatMap.put("ppt-html", FORMAT_POWERPOINT_HTML);

        sm_formatMap.put("1", FORMAT_TROFF_MAN);
        sm_formatMap.put("1m", FORMAT_TROFF_MAN);
        sm_formatMap.put("2", FORMAT_TROFF_MAN);
        sm_formatMap.put("3", FORMAT_TROFF_MAN);
        sm_formatMap.put("4", FORMAT_TROFF_MAN);
        sm_formatMap.put("5", FORMAT_TROFF_MAN);
        sm_formatMap.put("6", FORMAT_TROFF_MAN);
        sm_formatMap.put("7", FORMAT_TROFF_MAN);
        sm_formatMap.put("8", FORMAT_TROFF_MAN);
        sm_formatMap.put("9", FORMAT_TROFF_MAN);

        // OpenOffice extractors
        sm_formatMap.put("od-xml", FORMAT_OPENOFFICE_XML);

        sm_formatMap.put("po", FORMAT_PO);

        sm_formatMap.put("office-xml", FORMAT_OFFICE_XML);

        // eBay Extractors - disabled Thu Apr 22 19:56:33 2004
        // sm_formatMap.put("prj", FORMAT_EBAY_PRJ);
        // sm_formatMap.put("sgml", FORMAT_EBAY_SGML);
        // sm_formatMap.put("ecb", FORMAT_HTML);
        sm_formatMap.put("rc", FORMAT_RC);
        sm_formatMap.put("idml", FORMAT_IDML);
    }

    private URL m_sourceUrl = null;
    private URL m_rulesUrl = null;
    private byte[] m_rulesBytes = null;

    /** The java encoding of the input */
    private String m_encoding = sm_defaultEncoding;
    /** The java locale of the input */
    private Locale m_locale = sm_defaultLocale;
    private Locale m_targetLocale = null;

    /** Extractor options */
    private Options m_options = null;

    private String m_segmentationRuleText = null;

    /**
     * <p>
     * The source file format id. <CODE>-1</CODE> is the invalid format.
     * </p>
     * 
     * @see ExtractorRegistry
     */
    private int m_inputFormat = -1;

    private EFInputData m_input = new EFInputData();
    private Output m_output = null;

    /** Debug flag for Diplomat command line utility. */
    private boolean m_debug = false;

    // For PPT issue
    private String m_bullets_MsOffice = null;

    private String jsFilterRegex = null;
    private CxeMessage cxeMessage = null;
    private FileProfileImpl fileProfile = null;
    private String fileProfileId;
    private long filterId = 0;
    private boolean isPreview = false;

    private String filterTableName;
    private boolean isSecondFilter = false;

    public boolean getisSecondFilter()
    {
        return isSecondFilter;
    }

    public void setIsSecondFilter(boolean isSecondFilter)
    {
        this.isSecondFilter = isSecondFilter;
    }

    public FileProfileImpl getFileProfile()
    {
        return fileProfile;
    }

    public void setFileProfile(FileProfileImpl fileProfile)
    {
        this.fileProfile = fileProfile;
    }

    public String getFilterTableName()
    {
        return filterTableName;
    }

    public void setFilterTableName(String filterTableName)
    {
        this.filterTableName = filterTableName;
    }

    public String getFileProfileId()
    {
        return fileProfileId;
    }

    public void setFileProfileId(String fileProfileId)
    {
        this.fileProfileId = fileProfileId;
    }

    public long getFilterId()
    {
        return filterId;
    }

    public void setFilterId(long filterId)
    {
        this.filterId = filterId;
    }

    public CxeMessage getCxeMessage()
    {
        return cxeMessage;
    }

    public void setCxeMessage(CxeMessage cxeMessage)
    {
        this.cxeMessage = cxeMessage;
    }

    /**
     * <p>
     * Constructs an uninitialized instance. Use the <CODE>setXXX</CODE> methods
     * to specify the necessary arguments.
     * </p>
     * <p>
     * DiplomatAPI objects can be recycled. Call {@link #reset() reset()} after
     * each extraction and specify new input parameters with the
     * <CODE>setXXX</CODE> methods for a new extraction.
     * </p>
     * 
     * @see #setSourceFile
     * @see #setSourceString
     * @see #setRuleFile
     * @see #setEncoding
     * @see #setLocale
     * @see #setInputFormat
     */
    public DiplomatAPI()
    {
        reset();
    }

    /**
     * <p>
     * Constructs an instance that is immediately ready to extract. Defaults for
     * encoding and locale are hardcoded to ISO_8859_1 and en_US, and the source
     * file format is automatically determined.
     * </p>
     */
    public DiplomatAPI(String sourceFileName, String rulesFileName)
            throws FileNotFoundException, IOException
    {
        init(sourceFileName, rulesFileName);
    }

    /**
     * <p>
     * Constructs an instance that is immediately ready to extract. The source
     * file format is automatically determined.
     * </p>
     */
    public DiplomatAPI(String sourceFileName, String rulesFileName,
            String encoding, String locale) throws ExtractorException,
            FileNotFoundException, IOException
    {
        init(sourceFileName, rulesFileName, encoding, locale);
    }

    /**
     * <p>
     * Constructs an instance that is immediately ready to extract.
     * </p>
     */
    public DiplomatAPI(String sourceFileName, String rulesFileName,
            String encoding, String locale, String formatName)
            throws ExtractorException, FileNotFoundException, IOException
    {
        init(sourceFileName, rulesFileName, encoding, locale, formatName);
    }

    /**
     * Determines if a format string represents a whitespace preserving format
     * or not.
     * 
     * Used by tm2.TmUtil.
     */
    static public boolean isWsNonPreservingFormat(String p_format)
    {
        boolean result = false;

        if (p_format.equals(FORMAT_HTML) || p_format.equals(FORMAT_JHTML)
                || p_format.equals(FORMAT_EXCEL_HTML)
                || p_format.equals(FORMAT_WORD_HTML)
                || p_format.equals(FORMAT_POWERPOINT_HTML))
        {
            result = true;
        }

        return result;
    }

    static public boolean isMsOfficeFormat(int p_format)
    {
        ExtractorRegistry reg = ExtractorRegistry.getObject();
        String formatName = reg.getFormatName(p_format);

        if (formatName.equals(FORMAT_EXCEL_HTML)
                || formatName.equals(FORMAT_WORD_HTML)
                || formatName.equals(FORMAT_POWERPOINT_HTML))
        {
            return true;
        }

        return false;
    }

    static public boolean isHtmlFormat(int p_format)
    {
        ExtractorRegistry reg = ExtractorRegistry.getObject();
        String formatName = reg.getFormatName(p_format);

        return formatName.equals(FORMAT_HTML);
    }

    // For PPT issue
    static public boolean isMsOfficePowerPointFormat(String p_formatName)
    {
        return p_formatName.equals(FORMAT_POWERPOINT_HTML);
    }

    static public boolean isMsOfficePowerPointFormat(int p_format)
    {
        ExtractorRegistry reg = ExtractorRegistry.getObject();
        String formatName = reg.getFormatName(p_format);
        return isMsOfficePowerPointFormat(formatName);
    }

    static public boolean isOpenOfficeFormat(String p_formatName)
    {
        return p_formatName.equals(FORMAT_OPENOFFICE_XML);
    }

    static public boolean isOfficeXmlFormat(String p_formatName)
    {
        return p_formatName.equals(FORMAT_OFFICE_XML);
    }

    /**
     * <p>
     * Resets the state of this instance to as it is after calling the default
     * constructor.
     * </p>
     */
    public void reset()
    {
        m_sourceUrl = null;
        m_rulesUrl = null;
        m_encoding = sm_defaultEncoding;
        m_locale = sm_defaultLocale;
        m_targetLocale = null;
        m_inputFormat = -1;
        m_input = new EFInputData();
        m_options = new Options();

        m_segmentationRuleText = null;
    }

    /**
     * <p>
     * Reset for the second filter configuration.
     * </p>
     */
    public void resetForChainFilter()
    {
        m_sourceUrl = null;
        m_inputFormat = -1;
        m_input = new EFInputData();

        // m_output = null;
        filterId = 0;
        filterTableName = null;
    }

    /**
     * <p>
     * Initializes the state of this instance after a call to {@link #reset()
     * reset()}. The instance is ready to extract.
     * </p>
     */
    public void init(String sourceFileName, String rulesFileName)
            throws FileNotFoundException, IOException
    {
        setSourceFile(sourceFileName);
        setRuleFile(rulesFileName);
    }

    /**
     * <p>
     * Initializes the state of this instance after a call to {@link #reset()
     * reset()}. The instance is ready to extract.
     * </p>
     */
    public void init(String sourceFileName, String rulesFileName,
            String encoding, String locale) throws ExtractorException,
            FileNotFoundException, IOException
    {
        setSourceFile(sourceFileName);
        setRuleFile(rulesFileName);
        setEncoding(encoding);
        setLocale(locale);
    }

    /**
     * <p>
     * Initializes the state of this instance after a call to {@link #reset()
     * reset()}. The instance is ready to extract.
     * </p>
     */
    public void init(String sourceFileName, String rulesFileName,
            String encoding, String locale, String formatName)
            throws ExtractorException, FileNotFoundException, IOException
    {
        setSourceFile(sourceFileName);
        setRuleFile(rulesFileName);
        setEncoding(encoding);
        setLocale(locale);
        setInputFormat(formatName);
    }

    /**
     * <p>
     * Sets the segmentation type: sentence (true) or paragraph (false).
     * </p>
     */
    public void setValidateGxml(boolean p_flag)
    {
        m_options.m_validateGxml = p_flag;
    }

    public void setSegmentationRuleText(String ruleText)
    {
        m_segmentationRuleText = ruleText;
    }

    /**
     * <p>
     * Sets the segmentation type: sentence (true) or paragraph (false).
     * </p>
     */
    public void setSentenceSegmentation(boolean p_flag)
    {
        m_options.m_sentenceSegmentation = p_flag;
    }

    /**
     * <p>
     * Sets the simplification behavior of the HTML extractor. If true, tags are
     * moved out of segments, if false, tags are kept.
     * </p>
     */
    public void setExtractorSimplify(boolean p_flag)
    {
        m_options.m_extractorSimplify = p_flag;
    }

    /**
     * <p>
     * Sets the extraction behavior of the JS extractor. If true, segments that
     * look like HTML will be sent to the HTML extractor. If false, all strings
     * are extracted as javascript strings as they are.
     * </p>
     */
    public void setCanCallOtherExtractor(boolean p_flag)
    {
        m_options.m_canCallOtherExtractor = p_flag;
    }

    /**
     * <p>
     * Sets the whitespace preservation behavior of the segmenter: if true, tags
     * representing whitespace will be treated as white during segmentation, if
     * false, such tags will be treated as empty.
     */
    public void setSegmenterPreserveWhitespace(boolean p_flag)
    {
        m_options.m_segmenterPreserveWhitespace = p_flag;
    }

    /**
     * <p>
     * Enables debug output of intermediate GXML results. This is for command
     * line use.
     * </p>
     */
    public void setDebug(boolean p_flag)
    {
        m_debug = p_flag;
    }

    /**
     * <p>
     * For PPT issue
     * </p>
     */
    public void setBulletsMsOffice(String p_bullets_MsOffice)
    {
        m_bullets_MsOffice = p_bullets_MsOffice;
    }

    /**
     * <p>
     * Excel Do Not Translate configuration
     * </p>
     */
    public void setExcelStyle(HashMap p_excelStyle)
    {
        m_input.setExcelStyle(p_excelStyle);
    }

    /**
     * <p>
     * Retrieves the current options for this Extractor.
     * </p>
     */
    public Options getOptions()
    {
        return m_options;
    }

    /**
     * <p>
     * Sets the options for this Extractor.
     * </p>
     */
    public void setOptions(Options p_options)
    {
        m_options = p_options;
    }

    /**
     * After extraction, this method allows to access the internal Output
     * structure used during extraction and segmentation. This lets callers
     * prevent parsing the Gxml string all over again.
     */
    public Output getOutput()
    {
        return m_output;
    }

    /**
     * <p>
     * Sets the name of the source file to be extracted.
     * </p>
     */
    public void setSourceFile(String p_sourceFileName)
    {
        m_sourceUrl = fileToUrl(p_sourceFileName);
        m_input.setURL(m_sourceUrl.toString());
    }

    /**
     * <p>
     * Sets the URL of the source file to be extracted.
     * </p>
     */
    public void setSourceFile(URL p_sourceFile)
    {
        m_sourceUrl = p_sourceFile;
        m_input.setURL(m_sourceUrl.toString());
    }

    /**
     * <p>
     * Sets the source file to be extracted from a <CODE>File</CODE> object.
     * </p>
     */
    public void setSourceFile(File p_sourceFile)
    {
        m_sourceUrl = fileToUrl(p_sourceFile.getPath());
        m_input.setURL(m_sourceUrl.toString());
    }

    /**
     * <p>
     * Sets the source file to be extracted from a <CODE>String</CODE> object.
     * </p>
     */
    public void setSourceString(String p_unicodeInput)
    {
        m_input.setUnicodeInput(p_unicodeInput);
    }

    /**
     * <p>
     * Sets the source file to be extracted from a byte array.
     * </p>
     */
    public void setSourceBytes(byte[] p_bytes)
    {
        m_input.setInput(p_bytes);
    }

    /**
     * <p>
     * Sets the name of the rule file containing additional extraction options.
     * Currently used for XML extraction only.
     * </p>
     * 
     * @see com.globalsight.ling.docproc.extractor.xml.Extractor
     */
    public void setRuleFile(String p_rulesFileName)
            throws FileNotFoundException, IOException
    {
        File f_rulesFile = new File(p_rulesFileName);
        byte[] a_rules = new byte[(int) f_rulesFile.length()];
        FileInputStream rulesReader = new FileInputStream(f_rulesFile);
        rulesReader.read(a_rules, 0, a_rules.length);
        m_input.setRules(new String(a_rules));
    }

    /**
     * Sets the extraction rules from a string.
     */
    public void setRules(String p_rules)
    {
        m_input.setRules(p_rules);
    }

    /**
     * <p>
     * Sets the IANA-style encoding of the source file, e.g.&nbsp;
     * <code>"ISO-8859-1"</code>. The encoding will be automatically converted
     * to the internal Java encoding.
     * </p>
     */
    public void setEncoding(String p_encoding) throws ExtractorException
    {
        String javaEncoding = CodesetMapper.getJavaEncoding(p_encoding);

        if (javaEncoding == null)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_ENCODING);
        }

        m_encoding = javaEncoding;
    }

    /**
     * <p>
     * Sets the locale, or source language, of the source file. The locale must
     * be in Internet form, e.g. <code>en</code> or <code>en_US</code>.
     * </p>
     * 
     * @exception ExtractorException
     *                with code
     *                {@link com.globalsight.ling.docproc.ExtractorExceptionConstants#INVALID_LOCALE
     *                INVALID_LOCALE} when the string cannot be converted to a
     *                valid locale.
     */
    public void setLocale(String p_locale) throws ExtractorException
    {
        m_locale = LocaleCreater.makeLocale(p_locale);
    }

    /**
     * <p>
     * Sets the locale, or source language, of the source file.
     * </p>
     */
    public void setLocale(Locale p_locale)
    {
        m_locale = p_locale;
    }

    public void setTargetLocale(String p_locale) throws ExtractorException
    {
        m_targetLocale = LocaleCreater.makeLocale(p_locale);
    }

    public void setTargetLocale(Locale p_locale)
    {
        m_targetLocale = p_locale;
    }

    /**
     * <p>
     * Sets the file format, e.g., HTML, XML, JS, ... For the list of valid
     * formats, see the description of <CODE>{@link
     * ExtractorRegistry ExtractorRegistry}</CODE>.
     * </p>
     */
    public void setInputFormat(String p_formatName)
    {
        m_inputFormat = ExtractorRegistry.getObject().getFormatId(p_formatName);
    }

    /**
     * <p>
     * Sets the file format id, e.g., HTML, XML, JS. For the list of valid
     * formats, see the description of
     * <CODE>{@link ExtractorRegistry ExtractorRegistry}</CODE>.
     * </p>
     */
    public void setInputFormat(int p_formatId)
    {
        m_inputFormat = p_formatId;
    }

    /**
     * <p>
     * Runs the Diplomat Extractor on the source file and returns a string
     * containing segmented and word-counted GXML.
     * </p>
     */
    public String extract() throws ExtractorException,
            ExtractorRegistryException, DiplomatWordCounterException,
            DiplomatSegmenterException, Exception
    {
        if (m_encoding == null)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_ENCODING);
        }

        if (m_sourceUrl == null && m_input.getInput() == null
                && m_input.getUnicodeInput() == null)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_SOURCE);
        }

        if (m_locale == null)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_LOCALE);
        }

        if (m_inputFormat == -1)
        {
            m_inputFormat = guessInputFormat();
        }

        if (m_inputFormat == -1)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INVALID_FILE_FORMAT,
                    "File extension not recognized.");
        }

        m_input.setCodeset(m_encoding);
        m_input.setLocale(m_locale);
        m_input.setType(m_inputFormat);

        // For PPT issue
        if (isMsOfficePowerPointFormat(m_inputFormat))
        {
            m_input.setBulletsMsOffice(m_bullets_MsOffice);
        }

        if (extractor == null)
        {
            extractor = createExtractor(m_inputFormat);
        }
        // AbstractExtractor extractor = createExtractor(m_inputFormat);

        m_output = new Output();
        Filter mainFilter = getMainFilter();
        extractor.init(m_input, m_output);
        extractor.setMainFilter(mainFilter);
        extractor.loadRules();
        extractor.setSimplifySegments(m_options.m_extractorSimplify);
        extractor.setCanCallOtherExtractor(m_options.m_canCallOtherExtractor);
        // # GBS-2894 : do segmentation before internal text
        extractor.setDoSegBeforeInlText(true);

        // Is javascript
        if (m_inputFormat == 2)
        {
            Method method = extractor.getClass().getMethod("setJsFilterRegex",
                    String.class);
            method.invoke(extractor, getJsFilterRegex());
        }

        // is html
        if (m_inputFormat == 1)
        {

        }

        boolean isIdmlXml = false;
        boolean isInddXml = false;
        if (extractor instanceof com.globalsight.ling.docproc.extractor.xml.Extractor)
        {
            com.globalsight.ling.docproc.extractor.xml.Extractor xmlExtractor = (com.globalsight.ling.docproc.extractor.xml.Extractor) extractor;
            if (fileProfileId != null)
            {
                isIdmlXml = IdmlHelper.isIdmlFileProfile(Long
                        .parseLong(fileProfileId));
                xmlExtractor.setIsIdmlXml(isIdmlXml);

                if (!isIdmlXml)
                {
                    FileProfileImpl f = HibernateUtil.get(
                            FileProfileImpl.class,
                            Long.parseLong(fileProfileId), false);
                    if (f != null)
                    {
                        long id = f.getKnownFormatTypeId();
                        KnownFormatTypeImpl type = HibernateUtil.get(
                                KnownFormatTypeImpl.class, id);
                        if (type != null)
                        {
                            isInddXml = type.getFormatType()
                                    .startsWith("indd_");
                        }
                    }
                }
            }

            for (ExtractRule r : rules)
            {
                xmlExtractor.addExtractRule(r);
            }
        }

        // XLF extractor need to know if it is a Blaise job file
        setBlaiseJobFlagForXlfExtractor();

        extractor.extract();

        if (m_debug)
        {
            System.err.println("------- GXML after extraction -------------");
            System.err.println(DiplomatWriter.WriteXML(m_output));
            System.err.println("-------------------------------------------");
        }

        // Convert C0 control codes to PUA characters to avoid XML
        // parser error. Doing it on the output object is much cheaper
        // than on the final string and also ensures correct input in
        // extraction steps that use XML parsers (like word counting).
        DiplomatCtrlCharConverter dc = new DiplomatCtrlCharConverter();
        dc.convertChars(m_output);
        m_output = dc.getOutput();

        // We have quasi paragraph-segmented GXML. Produce sentence
        // segmentation if requested by config file or user-defined
        // options, else run paragraph segmentation to produce a valid
        // file for the next step.
        ExtractorRegistry registry = ExtractorRegistry.getObject();
        String formatName = registry.getFormatName(m_inputFormat);
        boolean isXLIFF = IFormatNames.FORMAT_XLIFF.equals(formatName)
                || IFormatNames.FORMAT_XLIFF20.equals(formatName)
                || IFormatNames.FORMAT_PASSOLO.equals(formatName);
        boolean isPO = IFormatNames.FORMAT_PO.equals(formatName);

        // GBS-3997&GBS-4066, protect emoji's unicodes
        EmojiUtil.protectEmojiUnicodes(m_output);

        // protect internal text / internal tag for segmentation
        List<String> internalTexts = InternalTextHelper
                .protectInternalTexts(m_output);

        if (m_options.m_sentenceSegmentation)
        {
            DiplomatSegmenter ds = new DiplomatSegmenter();

            if (isMsOfficeFormat(m_inputFormat))
            {
                ds.setPreserveWhitespace(true);
            }
            else
            {
                ds.setPreserveWhitespace(m_options.m_segmenterPreserveWhitespace);
            }

            if (m_segmentationRuleText.equalsIgnoreCase("default"))
            {
                if (isXLIFF || isPO)
                {
                    ds.segmentXliff(m_output);
                }
                else
                {
                    // Default rule, currently it represents the existing
                    // segmentation function.
                    ds.segment(m_output);
                }
            }
            else
            {
                if (isXLIFF || isPO)
                {
                    ds.segmentXliff(m_output);
                }
                else
                {
                    // Not default rule, now we use it to do segmentation.
                    ds.segment(m_output, m_segmentationRuleText);
                }
            }

            // output all empty segments to skeleton except PO and XLIFF
            if (!isXLIFF && !isPO)
            {
                Vector vv = ds.getOutput().getDocumentElements();
                ds.getOutput().setDocumentElements(
                        Segmentation.adjustEmptySegments(vv));
            }

            m_output = ds.getOutput();
        }
        else
        {
            DiplomatParagraphSegmenter ds = new DiplomatParagraphSegmenter();

            if (isMsOfficeFormat(m_inputFormat))
            {
                ds.setPreserveWhitespace(true);
            }
            else
            {
                ds.setPreserveWhitespace(m_options.m_segmenterPreserveWhitespace);
            }

            ds.segment(m_output);

            m_output = ds.getOutput();
        }

        // restore protected internal text
        if (internalTexts != null && internalTexts.size() > 0)
        {
            InternalTextHelper.restoreInternalTexts(m_output, internalTexts);
        }

        // # GBS-2894 : do segmentation before internal text
        if (extractor.isDoSegBeforeInlText())
        {
            boolean checkPOAndProp = checkPOAndPropertiesFilter(mainFilter);
            if (checkPOAndProp)
            {
                if (fileProfile != null)
                {
                    Filter filter = FilterHelper.getFilter(
                            fileProfile.getFilterTableName(),
                            fileProfile.getFilterId());
                    InternalTextHelper.handleOutput(m_output, filter, true);
                }
                else
                {
                    InternalTextHelper.handleOutput(m_output, mainFilter, true);
                }
            }
        }

        if (!isSecondFilter)
        {
            EscapingHelper.handleOutput4Import(m_output, mainFilter);
        }

        if (m_debug)
        {
            System.err.println("------- GXML after segmentation -----------");
            System.err.println(DiplomatWriter.WriteXML(m_output));
            System.err.println("-------------------------------------------");
        }

        DiplomatWordCounter wc = new DiplomatWordCounter();
        wc.setLocalizableWordcount(m_options.m_localizableWordCount);
        wc.countDiplomatDocument(m_output);
        m_output = wc.getOutput();

        if (isIdmlXml || isInddXml)
        {
            InddTuMappingHelper.processOutput(m_output, isIdmlXml, isInddXml);
        }

        // GBS-3997&GBS-4066, tag emoji's unicodes
        EmojiUtil.tagEmojiUnicodes(m_output);

        // call GC here to free some memory used in extracting
        // System.gc();
        String gxml = "";

        // by walter, the xliff target content needn't be diplomate process
        if (isXLIFF || isPO)
        {
            gxml = DiplomatWriter.WriteXML(m_output);
        }
        else
        {
            // Wed Jun 04 21:02:29 2003 CvdL new step to wrap nbsp and fix
            // the "x" attributes.
            DiplomatPostProcessor pp = new DiplomatPostProcessor();
            pp.setFormatName(formatName);
            pp.postProcess(m_output);
            m_output = pp.getOutput();
            gxml = pp.getDiplomatXml();
        }

        if (m_debug)
        {
            System.err.println("------- GXML after word counting ----------");
            System.err.println(gxml);
            System.err.println("-------------------------------------------");
        }

        // Now we have a final result.
        String finalGxml = gxml;

        // Free some resources (large strings and data-structures), but
        // not m_output since that is a different from of the result
        // that can be retrieved later.
        gxml = null;

        if (m_options.m_validateGxml || m_debug)
        {
            DiplomatValidator val = new DiplomatValidator();
            String error = val.validate(finalGxml);

            if (error != null)
            {
                String message = "GXML validation error: " + error;

                throw new ExtractorException(
                        ExtractorExceptionConstants.DIPLOMAT_XML_PARSE_ERROR,
                        message);
            }
        }
        return finalGxml;

    }

    /**
     * 
     * 
     * */
    private boolean checkPOAndPropertiesFilter(Filter mainFilter)
    {
        if (mainFilter != null)
        {
            boolean propFilter = mainFilter instanceof JavaPropertiesFilter ? true
                    : false;
            boolean poFilter = mainFilter instanceof POFilter ? true : false;
            if ((!propFilter && fileProfile == null)
                    && (!poFilter && fileProfile == null))
            {
                return true;
            }
            else if (propFilter || poFilter && fileProfile == null)
            {
                boolean useBptTag = true;
                if (propFilter)
                {
                    JavaPropertiesFilter jf = (JavaPropertiesFilter) mainFilter;
                    long scid = jf.getSecondFilterId();
                    String scTableName = jf.getSecondFilterTableName();
                    useBptTag = !FilterHelper.isFilterExist(scTableName, scid);
                    return useBptTag;
                }
                else if (poFilter)
                {
                    POFilter po = (POFilter) mainFilter;
                    long scid = po.getSecondFilterId();
                    String scTableName = po.getSecondFilterTableName();
                    useBptTag = !FilterHelper.isFilterExist(scTableName, scid);
                    return useBptTag;
                }
            }
            else if (!propFilter || !poFilter && fileProfile != null)
            {
                return true;
            }
        }
        return false;
    }

    public Filter getMainFilter() throws Exception
    {
        if (filterId >= 0 && filterTableName != null)
        {
            return FilterHelper.getFilter(filterTableName, filterId);
        }
        else
        {
            return null;
        }
    }

    private void propareOutPut()
    {

    }

    /**
     * <p>
     * Runs the Diplomat Extractor on the source file and returns a string
     * containing segmented and word-counted Diplomat XML. The result does/not/
     * contain a reference to the Diplomat DTD.
     * </p>
     * 
     * @param input
     *            : the input as a byte array. See
     *            {@link #setSourceBytes(byte[]) setSourceBytes()}.
     * @param encoding
     *            : the Java-style encoding of the input bytes. See
     *            {@link #setEncoding(String) setEncoding()}.
     * @param locale
     *            : the locale of the input. See {@link #setLocale(String)
     *            setLocale()}.
     * @param format
     *            : the input file format. See {@link #setInputFormat(String)
     *            setInputFormat()}.
     * @param rules
     *            : the extraction rules to use, as a Unicode string. See
     *            {@link #setRules(String) setRules()}.
     * 
     * @see com.globalsight.ling.docproc.extractor.css.Extractor CSS Extractor
     * @see com.globalsight.ling.docproc.extractor.html.Extractor HTML Extractor
     * @see com.globalsight.ling.docproc.extractor.javaprop.Extractor JavaProp
     *      Extractor
     * @see com.globalsight.ling.docproc.extractor.javascript.Extractor
     *      JavaScript Extractor
     * @see com.globalsight.ling.docproc.extractor.plaintext.Extractor Plaintext
     *      Extractor
     * @see com.globalsight.ling.docproc.extractor.xml.Extractor XML Extractor
     */
    public String extract(byte[] input, String encoding, String locale,
            String format, String rules) throws ExtractorException,
            ExtractorRegistryException, DiplomatWordCounterException,
            DiplomatSegmenterException, Exception

    {
        reset();

        setSourceBytes(input);
        setEncoding(encoding);
        setLocale(locale);
        setInputFormat(format);
        setRules(rules);

        return extract();
    }

    /**
     * Converts a Diplomat XML string back to its original format by stripping
     * Diplomat XML tags and unescaping XML entities. The additional parameter
     * <code>keepGsa</code> specifies whether <code>&lt;gs&gt;</code> tags from
     * the original input file are to be output or not (default is
     * <code>false</code>).
     */
    public String merge(String p_gxml, boolean p_keepGsa)
            throws DiplomatMergerException
    {
        L10nContent l10ncontent = new L10nContent();
        DiplomatMerger merger = new DiplomatMerger();

        merger.init(p_gxml, l10ncontent);
        merger.setKeepGsa(p_keepGsa);

        // if job is using "html_filter" as secondary filter,when export,
        // whether to convert html entity is decided by html filter setting.
        Boolean[] isConvert = isConvertHtmlEntity();
        merger.setIsUseSecondaryFilter(isConvert[0]);
        merger.setConvertHtmlEntryFromSecondFilter(isConvert[1]);
        merger.setFilterTableName(filterTableName);
        merger.setFilterId(filterId);

        merger.merge();

        // format specific post merge processing
        String processed = postMergeProcess(l10ncontent.getL10nContent(),
                merger.getDocumentFormat(), "UTF-8");

        // processed == null means the content doesn't need to be changed.
        if (processed != null)
        {
            l10ncontent.setL10nContent(processed);
        }

        checkWellFormedIfNeed(l10ncontent.getL10nContent(),
                merger.getDocumentFormat());

        return l10ncontent.getL10nContent();
    }

    /**
     * Converts a Diplomat XML string back to its original format by stripping
     * Diplomat XML tags and unescaping XML entities. Returns the result in the
     * specified target encoding. The additional parameter <code>keepGsa</code>
     * specifies whether <code>&lt;gs&gt;</code> tags from the original input
     * file are to be output or not (default is <code>false</code>).
     */
    public byte[] merge(String p_gxml, String p_targetEncoding,
            boolean p_keepGsa) throws DiplomatMergerException,
            TranscoderException
    {
        L10nContent l10ncontent = new L10nContent();
        DiplomatMerger merger = new DiplomatMerger();
        merger.setFilterTableName(filterTableName);
        merger.setFilterId(filterId);
        try
        {
            setEncoding(p_targetEncoding);
        }
        catch (ExtractorException e)
        {
            throw new DiplomatMergerException(
                    ExtractorExceptionConstants.INVALID_ENCODING, e.toString());
        }

        merger.init(p_gxml, l10ncontent);
        merger.setKeepGsa(p_keepGsa);
        merger.setTargetEncoding(m_encoding);

        // if job is using "html_filter" as secondary filter,when export,
        // whether to convert html entity is decided by html filter setting.
        Boolean[] isConvert = isConvertHtmlEntity();
        merger.setIsUseSecondaryFilter(isConvert[0]);
        merger.setConvertHtmlEntryFromSecondFilter(isConvert[1]);

        merger.setCxeMessage(cxeMessage);
        merger.merge();

        // Convert PUA characters back to original C0 control codes
        String gxml = l10ncontent.getL10nContent();
        l10ncontent.setL10nContent(CtrlCharConverter.convertToCtrl(gxml));

        // format specific post merge processing
        String processed = postMergeProcess(l10ncontent.getL10nContent(),
                merger.getDocumentFormat(), p_targetEncoding);

        // processed == null means the content doesn't need to be changed.
        if (processed != null)
        {
            l10ncontent.setL10nContent(processed);
        }

        checkWellFormedIfNeed(l10ncontent.getL10nContent(),
                merger.getDocumentFormat());

        return l10ncontent.getTranscodedL10nContent(m_encoding);
    }

    //
    // Private Support Methods
    //

    /**
     * <p>
     * Guesses the file format of the input by trying to find the file
     * extension.
     * </p>
     * <p>
     * This function does not work when the input comes from a string. If
     * called, it will return an invalid format id.
     * </p>
     * 
     * @return a format id or <CODE>-1</CODE>
     * @throws URISyntaxException
     * @see ExtractorRegistry
     */
    private int guessInputFormat() throws ExtractorRegistryException,
            URISyntaxException
    {
        // System.err.println("guessing format for " + m_sourceUrl);

        if (m_sourceUrl == null)
        {
            // can't check string input
            return -1;
        }

        // assume default format
        String str_format = null;

        String str_fileName = m_sourceUrl.toURI().getPath();
        int i_dot = str_fileName.lastIndexOf('.');

        if (i_dot == -1)
        {
            // no extension found, can't continue
            return -1;
        }

        String str_extension = str_fileName.substring(i_dot + 1);
        str_extension = str_extension.toLowerCase();

        // System.err.println("Extension " + str_extension);

        str_format = (String) sm_formatMap.get(str_extension);

        if (str_format == null)
        {
            return -1;
        }
        else
        {
            return ExtractorRegistry.getObject().getFormatId(str_format);
        }
    }

    /**
     * <p>
     * Converts a filename to an absolute file:// URL.
     * </p>
     */
    static private URL fileToUrl(String p_fileName)
    {
        File file = new File(p_fileName);
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");

        // System.err.println("absolute path: " + path);

        if (fSep != null && fSep.length() == 1)
        {
            path = path.replace(fSep.charAt(0), '/');
        }

        if (path.length() > 0 && path.charAt(0) != '/')
        {
            path = '/' + path;
        }

        // System.err.println("fixed path: " + path);

        try
        {
            return new URL("file", null, path);
        }
        catch (java.net.MalformedURLException e)
        {
            // According to the spec this could only happen if the file
            // protocol were not recognized.

            // throw new Exception ("unexpected MalformedURLException: " +
            // e.toString());

            System.err.println("UNEXPECTED ERROR: file protocol unknown");
            return null;
        }
    }

    private String postMergeProcess(String p_content, String p_format,
            String p_ianaEncoding) throws DiplomatMergerException
    {
        ExtractorRegistry registry = ExtractorRegistry.getObject();

        int formatId = registry.getFormatId(p_format);
        if (c_category.isDebugEnabled())
        {
            c_category.debug("p_format = " + p_format);
            c_category.debug("formatId = " + formatId);
        }
        // p_format is not a known format. do nothing.
        if (formatId == -1)
        {
            return null;
        }

        // construct an post merge processor
        String strClass = registry.getPostMergeClasspath(formatId);

        PostMergeProcessor processor = null;
        boolean donotProcess = false;
        try
        {
            processor = (PostMergeProcessor) Class.forName(strClass)
                    .newInstance();
            if (processor instanceof JspPostMergeProcessor)
            {
                if (filterId > 0 && filterTableName != null)
                {
                    Filter filter = FilterHelper.getFilter(filterTableName,
                            filterId);
                    if (filter instanceof JSPFilter)
                    {
                        boolean addAdditionalHead = ((JSPFilter) filter)
                                .getAddAdditionalHead();
                        ((JspPostMergeProcessor) processor)
                                .setAddAdditionalHead(addAdditionalHead);
                    }
                }
            }
            else if (processor instanceof XmlPostMergeProcessor)
            {
                boolean checkIsInddXml = true;
                if (filterId > 0 && filterTableName != null)
                {
                    Filter filter = FilterHelper.getFilter(filterTableName,
                            filterId);
                    if (filter instanceof XMLRuleFilter)
                    {
                        checkIsInddXml = false;
                        try
                        {
                            XMLRuleFilter xmlFilter = (XMLRuleFilter) filter;
                            XmlFilterConfigParser config = new XmlFilterConfigParser(
                                    xmlFilter);
                            config.parserXml();
                            XmlFilterHelper helper = new XmlFilterHelper(
                                    xmlFilter);
                            helper.init();

                            XmlPostMergeProcessor xmlProcessor = (XmlPostMergeProcessor) processor;
                            xmlProcessor.setLocale(m_targetLocale);
                            xmlProcessor.setGenerateEncoding(false);
                            xmlProcessor.setGenerateLang(config
                                    .isGerateLangInfo());
                            xmlProcessor.setXmlFilterHelper(helper);
                        }
                        catch (Exception e)
                        {
                            System.err
                                    .println("UNEXPECTED ERROR: apply XmlFilter config failed");
                            System.err.println(e.toString());
                        }
                    }
                }

                if (checkIsInddXml
                        && FontMappingHelper.isInddXml(p_format, p_content)
                        && m_targetLocale != null)
                {
                    // do not change font for exporting, just for previewing PDF
                    // p_content =
                    // FontMappingHelper.processInddXml(m_targetLocale.toString(),
                    // p_content);
                }
            }
            else if (processor instanceof FmPostMergeProcessor)
            {
                if (isPreview)
                {
                    FMPreviewerHelper previewerHelper = new FMPreviewerHelper(
                            p_content);
                    p_content = previewerHelper.process();
                }

                FmPostMergeProcessor fmProcessor = (FmPostMergeProcessor) processor;
                fmProcessor.setSourceLocale(m_locale.toString());
                fmProcessor.setTargetLocale(m_targetLocale.toString());

                donotProcess = !isPreview;
            }

            if (isPreview && FORMAT_OFFICE_XML.equals(p_format))
            {
                if (!(p_content.endsWith("</w:document>")
                        || p_content.endsWith("</w:ftr>") || p_content
                            .endsWith("</w:hdr>")))
                {
                    p_content = HtmlPreviewerHelper.removeGSColorTag(p_content);
                    return p_content;
                }
            }
        }
        catch (Exception e)
        {
            throw new DiplomatMergerException(
                    "PostMergeProcessorCreationFailure", null, e);
        }

        if (donotProcess)
        {
            return null;
        }
        else
        {
            return processor.process(p_content, p_ianaEncoding);
        }
    }

    private void checkWellFormedIfNeed(String p_content, String p_format)
    {
        if (FORMAT_XML.equals(p_format))
        {
            if (filterId > 0 && filterTableName != null)
            {
                XMLRuleFilter xmlFilter = null;
                XmlFilterConfigParser config = null;
                try
                {
                    Filter filter = FilterHelper.getFilter(filterTableName,
                            filterId);
                    if (filter instanceof XMLRuleFilter)
                    {
                        xmlFilter = (XMLRuleFilter) filter;
                        config = new XmlFilterConfigParser(xmlFilter);
                        config.parserXml();
                    }
                }
                catch (Exception e)
                {
                    System.err
                            .println("UNEXPECTED ERROR: get XmlFilter config failed");
                    System.err.println(e.toString());
                }

                try
                {
                    if (config != null && config.isCheckWellFormed())
                    {
                        XmlFilterChecker.checkWellFormed(p_content);
                    }
                }
                catch (Exception e)
                {
                    throw new DiplomatMergerException(e);
                }
            }
        }
    }

    /**
     * Creates an extractor for the requested format. We could use
     * AbstractExtractor.makeExtractor() or some such but I'm not sure if this
     * is the exact same thing. For backwards-compatibility, we do it the
     * old-fashioned way.
     */
    private AbstractExtractor createExtractor(int p_format)
            throws ExtractorException
    {
        try
        {
            ExtractorRegistry registry = ExtractorRegistry.getObject();

            // construct an extractor
            String strClass = registry.getExtractorClasspath(p_format);

            if (m_debug)
            {
                System.err.println("Creating extractor " + strClass);
            }

            AbstractExtractor result = (AbstractExtractor) Class.forName(
                    strClass).newInstance();

            // For "html_filter" and office filters
            if ((FilterConstants.HTML_TABLENAME.equals(filterTableName) && p_format == 1)
                    || isMsOfficeFormat(p_format))
            {
                ((Extractor) result).setRules(fileProfileId, filterId);
                ((Extractor) result).setFilterId(filterId);
                ((Extractor) result).setFilterTableName(filterTableName);
            }
            else if (filterTableName == null && filterId == -1
                    && isHtmlFormat(p_format))
            {
                ((Extractor) result).useDefaultRules();
            }
            // For office formats
            // if(isMsOfficeFormat(p_format))
            // {
            // //load the configure file.
            // ((Extractor) result).setRules(fileProfileId, -1);
            // ((Extractor) result).setFilterId(filterId);
            // ((Extractor) result).setFilterTableName(filterTableName);
            // }

            if (p_format == 23)
            {
                ((Extractor) result).setMSOfficeDocFilterId(filterId);
            }

            return result;
        }
        catch (Exception e)
        {
            throw new ExtractorException(
                    ExtractorExceptionConstants.INTERNAL_ERROR, e);
        }
    }

    // Actually "isConvertHtmlEntity" can be true only for JavaProperties.
    private Boolean[] isConvertHtmlEntity()
    {
        boolean isUseSecondaryHtmlFilter = false;
        boolean isConvertHtmlEntity = false;
        try
        {
            FileProfilePersistenceManager fpManager = ServerProxy
                    .getFileProfilePersistenceManager();
            FileProfileImpl fp = (FileProfileImpl) fpManager
                    .getFileProfileById(Long.parseLong(this.fileProfileId),
                            false);
            KnownFormatType kft = fpManager.getKnownFormatTypeById(
                    fp.getKnownFormatTypeId(), false);

            long secondFilterId = fp.getSecondFilterId();
            String secondFilterName = fp.getSecondFilterTableName();
            if (secondFilterId > 0 && "html_filter".equals(secondFilterName))
            {
                isUseSecondaryHtmlFilter = true;
                HtmlFilter htmlFilter = FilterHelper
                        .getHtmlFilter(secondFilterId);
                isConvertHtmlEntity = htmlFilter.isConvertHtmlEntry();
            }

            // PO need not "Convert HTML Entity For Export".
            String format = kft.getFormatType();
            if (FORMAT_PO.equals(format))
            {
                isConvertHtmlEntity = false;
            }
        }
        catch (Exception e)
        {
        }

        return new Boolean[]
        { isUseSecondaryHtmlFilter, isConvertHtmlEntity };
    }

    public String getJsFilterRegex()
    {
        return jsFilterRegex;
    }

    public void setJsFilterRegex(String jsFilterRegex)
    {
        this.jsFilterRegex = jsFilterRegex;
    }

    public boolean isPreview()
    {
        return isPreview;
    }

    public void setPreview(boolean isPreview)
    {
        this.isPreview = isPreview;
    }

    /**
     * @see com.globalsight.cxe.adapter.ling.HasExtractRule#addExtractRule()
     */
    public void addExtractRule(ExtractRule rule)
    {
        rules.add(rule);
    }

    public AbstractExtractor getExtractor()
    {
        return extractor;
    }

    public void setExtractor(AbstractExtractor extractor)
    {
        this.extractor = extractor;
    }

    private void setBlaiseJobFlagForXlfExtractor()
    {
        if (extractor instanceof com.globalsight.ling.docproc.extractor.xliff.Extractor)
        {
        	if (cxeMessage != null)
        	{
        		boolean isBlaiseJob = false;
            	try
            	{
            		String jobId = (String) cxeMessage.getParameters().get("JobId");
            		isBlaiseJob = ServerProxy.getJobHandler()
            				.getJobById(Long.parseLong(jobId)).isBlaiseJob();
            	}
            	catch (Exception e)
            	{
            	}
				((com.globalsight.ling.docproc.extractor.xliff.Extractor) extractor)
						.setIsBlaiseJob(isBlaiseJob);
        	}
        }
    }
}
