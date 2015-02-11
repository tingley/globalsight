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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.edit.offline.download.WriterInterface;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.ling.common.LCID;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.PseudoOverrideMapItem;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * <p>
 * Generates an RTF file from an OfflinePageData object.
 * </p>
 * 
 * <p>
 * The output is written as ASCII characters to the provided OutputStream.
 * Unicode or double-byte characters are converted to the proper RTF codes as
 * necessary. Note that RTF files are always ASCII files.
 * </p>
 * 
 */
public abstract class RTFWriterUnicode extends RTFUnicode implements
        WriterInterface
{
    static private final Logger c_logger = Logger
            .getLogger(RTFWriterUnicode.class);

    //
    // Public Constants
    //

    /**
     * A string identifying this product. Used in the info section of the
     * generated RTF file.
     */
    static public final String PRODUCTNAME = "GlobalSight";

    static public final String DOCUMENTTITLE = "Ambassador RTF Export";
    static public final String DEFAULT_FONT = "RTFDefaultFont";
    static public final String DEFAULT_FONT_DOT = "RTFDefaultFont.";
    static public final String MSWORD_TRANS_TEMPLATE = "AOR-Template.dot";

    //
    // RTF Document Variable Names
    //

    /** RTF DocVar name: version number of the writer. */
    static public final String DOCVAR_NAME_DOCVERSION = "ambassadorDocVer";

    // RTF Document variable values
    /** RTF DocVar value: a string which indicates boolean true. */
    static public final String DOCVAR_VAL_TRUE = "True";
    /** RTF DocVar value: a string which indicates boolean false. */
    static public final String DOCVAR_VAL_FALSE = "False";
    /** RTF DocVar value: a string which indicates the ptag display state. */
    static public final String DOCVAR_VAL_PTAG_DIM = "dim";
    /** RTF DocVar value: a string which indicates the ptag display state. */
    static public final String DOCVAR_VAL_PTAG_BRIGHT = "bright";

    //
    // Private & Protected Constants
    //

    /** The name of the person downloading the file. */
    protected String m_downloadUserName = "";
    static protected final String RTF_VER = "rtf1";
    static protected final String RTF_HEADER_START = "\\rtf1\\ansi\\ansicpg1252\\uc0\\deflang1033\\deflangfe1033\\deff0";

    static protected final String RTF_FONT_TABLE_START = "{\\fonttbl";

    // f0 = source lang font, f1 target lang font, f2 = Comment and/or UI lang
    // font
    static protected final String RTF_FONT_TABLE_END = "{\\f3\\fmodern\\fcharset0\\fprq1{\\*\\panose 02070309020205020404}Courier New;}"
            + "{\\f4\\fswiss\\fcharset0\\fprq2 Arial;}"
            + "{\\f5\\froman\\fcharset0\\fprq2{\\*\\panose 02020603050405020304} Times New Roman;}"
            + "{\\f6\\fswiss\\fcharset128\\fprq2{\\*\\panose 020b0604020202020204} Arial Unicode MS;}"
            + "}";

    static protected final String RTF_STYLESHEET_START = "{\\stylesheet"
            + "{\\ql \\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0 ";

    // NOTE: Stylesheet entries (\csN and \sN) cannot be used as references
    // in the document body. The stylesheet is SIMPLY a standalone library of
    // pre-defined styles that is mainly used by the host application.
    // You can **relate** a run of text to one of these styles but you
    // MUST ALWAYS reinsert the literal RTF that defines the all of the
    // formating
    // for a given run of text each time you make a run of the given style!!!
    // And then optionally relate the run to a style by using the \csN or \sN
    // below.
    static protected final String RTF_STYLESHEET_END = " \\cgrid\\loch\\f1\\hich\\af0\\dbch\\af16\\fs24 \\snext0 Normal;}"
            + "{\\*\\cs10 \\additive Default Paragraph Font;}"
            + "{\\*\\cs1 \\additive \\v\\f4\\fs24\\sub\\cf11 tw4winMark;}"
            + "{\\*\\cs2 \\additive \\cf3\\fs40\\f4 tw4winError;}"
            +
            // RTF spec: lang1024 is defined as
            // "Process or User Default Language"
            "{\\*\\cs3 \\additive \\f4\\cf10\\lang1024 tw4winPopup;}"
            + "{\\*\\cs4 \\additive \\f4\\cf9\\lang1024 tw4winJump;}"
            + "{\\*\\cs5 \\additive \\f4\\fs24\\cf15\\lang1024 tw4winExternal;}"
            + "{\\*\\cs6 \\additive \\f4\\fs24\\cf6\\lang1024 tw4winInternal;}"
            + "{\\*\\cs7 \\additive \\cf1 tw4winTerm;}"
            + "{\\*\\cs8 \\additive \\f4\\cf12\\lang1024 DO_NOT_TRANSLATE;}"
            + "{\\*\\cs9 \\additive \\fs16 \\sbasedon10 annotation ref;}"
            +
            // RTF spec: lang1024 is defined as
            // "Process or User Default Language"
            "{\\*\\cs11 \\additive \\f4\\fs20\\cf15\\lang1024 gsComment;}"
            + "{\\*\\cs12 \\additive \\f4\\fs16\\cf15\\lang1024 gsPlaceholder;}"
            + "{\\*\\cs13 \\additive \\f5\\fs16\\cf2\\lang1024 gsInfo;}"
            + "{\\*\\cs14 \\additive \\f5\\fs16\\cf2 gsExact;}"
            + "{\\*\\cs15 \\additive \\f5\\fs16\\cf2 gsLockedExact;}"
            + "{\\*\\cs16 \\additive \\f5\\fs16\\cf2 gsFuzzy;}"
            + "{\\s10 \\ql\\fs20\\li0\\ri0\\widctlpar\\aspalpha\\aspnum\\faauto\\adjustright\\rin0\\lin0\\itap0  annotation txt}"
            + "}";

    static protected final String COLOR_TABLE = "{\\colortbl;\r\n"
            + "\\red0\\green0\\blue0;\r\n" + "\\red0\\green0\\blue255;\r\n"
            + "\\red0\\green255\\blue255;\r\n" + "\\red0\\green255\\blue0;\r\n"
            + "\\red255\\green0\\blue255;\r\n" + "\\red255\\green0\\blue0;\r\n"
            + "\\red255\\green255\\blue0;\r\n"
            + "\\red255\\green255\\blue255;\r\n"
            + "\\red0\\green0\\blue128;\r\n" + "\\red0\\green128\\blue128;\r\n"
            + "\\red0\\green128\\blue0;\r\n" + "\\red128\\green0\\blue128;\r\n"
            + "\\red128\\green0\\blue0;\r\n" + "\\red128\\green128\\blue0;\r\n"
            + "\\red128\\green128\\blue128;\r\n"
            + "\\red192\\green192\\blue192;\r\n"
            + "\\red204\\green255\\blue255;\r\n"
            + "\\red230\\green230\\blue230;\r\n"
            + "\\red255\\green102\\blue0;\r\n"
            + "\\red102\\green255\\blue0;\r\n" + "}";

    // short cuts to character styles
    static protected final String CSTYLE_TW4WIN_MARK = "\\cs1";
    static protected final String CSTYLE_TW4WIN_EXTERNAL = "\\cs5";
    static protected final String CSTYLE_TW4WIN_INTERNAL = "\\cs6";
    static protected final String CSTYLE_TW4WIN_TERM = "\\cs7";
    static protected final String CSTYLE_TW4WIN_DO_NOT_TRANSLATE = "\\cs8";
    static protected final String CSTYLE_ANNOTATION_REF = "\\cs9";
    static protected final String CSTYLE_GS_COMMENT = "\\cs11";
    static protected final String CSTYLE_GS_PTAG = "\\cs12";
    static protected final String CSTYLE_GS_INFO = "\\cs13";

    // not added to stlyesheet for now0
    static protected final String CSSTYLE_HEADER_BORDER = "\\brdrt\\brdrs\\brdrw10\\brsp20 \\brdrl\\brdrs\\brdrw10\\brsp80 "
            + "\\brdrb\\brdrs\\brdrw10\\brsp20 \\brdrr\\brdrs\\brdrw10\\brsp80 "
            + "\\faauto\\rin0\\lin0\\itap0 \\cbpat17";

    // short cuts to paragraph styles
    static protected final String PSTYLE_ANNOTATION_TXT = "\\s10";
    static protected final String PSTYLE_PLAIN = "\\s15";

    // short cut to fonts
    static protected final String FONT_SOURCE = "\\f0";
    static protected final String FONT_TARGET = "\\f1";
    static protected final String FONT_COMMENT = "\\f2";
    static protected final String FONT_COURIER = "\\f3";
    static protected final String FONT_ARIAL = "\\f4";
    static protected final String FONT_TIMES = "\\f5";
    static protected final String FONT_ARIAL_UNICODE = "\\f6";

    // short cuts to colors
    static protected final String COLOR_BLACK = "\\cf1";
    static protected final String COLOR_BLUE = "\\cf2";
    static protected final String COLOR_GREEN = "\\cf4";
    static protected final String COLOR_PURPLE = "\\cf5";
    static protected final String COLOR_RED = "\\cf6";
    static protected final String COLOR_DARK_GREEN = "\\cf11";
    static protected final String COLOR_GREY = "\\cf15";
    static protected final String COLOR_GREY_10PCT = "\\cf18";
    static protected final String COLOR_ORANGE = "\\cf19";
    static protected final String COLOR_LIGHT_GREEN = "\\cf20";

    // short cuts to background colors (based on COLOR_GREY_10PCT)
    static protected final String BG_PATTERN_PROGRESS = "\\chshdng0\\chcfpat0\\chcbpat18";

    // short cuts to point sizes
    static protected final String SIZE_3PT = "\\fs6";
    static protected final String SIZE_6PT = "\\fs12";
    static protected final String SIZE_8PT = "\\fs16";
    static protected final String SIZE_9PT = "\\fs18";
    static protected final String SIZE_10PT = "\\fs20";
    static protected final String SIZE_12PT = "\\fs24";

    // some rtf tokens
    static protected final String NO_PROOF = "\\noproof";
    static protected final String HIDDEN = "\\v";

    // Bookmark flags
    static protected final boolean BM_OPEN = true;
    static protected final boolean BM_CLOSE = false;

    // Bookmark name prefixes

    /**
     * Generic bookmark prefix. Note: the value must be synchronized with the
     * VBA code.
     */
    static protected final String BM_PREFIX = "gs";

    /**
     * Bookmark prefix for an exact match. Note: the value must be synchronized
     * with the VBA code.
     */
    static protected final String BM_PREFIX_EXACT = BM_PREFIX + "e";

    /**
     * Bookmark prefix for a fuzzy match. Note: the value must be synchronized
     * with the VBA code.
     */
    static protected final String BM_PREFIX_FUZZY = BM_PREFIX + "f";

    /**
     * Bookmark prefix for a nomatch match. Note: the value must be synchronized
     * with the VBA code.
     */
    static protected final String BM_PREFIX_NOMATCH = BM_PREFIX + "n";

    /**
     * Bookmark prefix for a locked segment. Note: the value must be
     * synchronized with the VBA code.
     */
    static protected final String BM_PREFIX_LOCKED = BM_PREFIX + "l";

    /**
     * Bookmark prefix for the resource source paragraph. Note: the value must
     * be synchronized with the VBA code.
     */
    static protected final String BM_PREFIX_SRCPARA = BM_PREFIX + "p";

    static protected final String EDIT_FLD_OPEN = "\\{";
    static protected final String EDIT_FLD_CLOSE = "\\}";

    // ////////////////////////////////////
    // Private Member Variables
    // ////////////////////////////////////

    /**
     * A resource bundle that contains our RTF strings.
     */
    static protected ResourceBundle m_resource;
    static private Set m_resourceKeys;
    static
    {
        m_resource = ResourceBundle
                .getBundle("com.globalsight.everest.edit.offline.rtf.UnicodeRTF");
        m_resourceKeys = new HashSet();
        Enumeration enumeration = m_resource.getKeys();
        while (enumeration.hasMoreElements())
        {
            m_resourceKeys.add(enumeration.nextElement());
        }
    }

    /**
     * A resource bundle that contains the UI related strings that appear in RTF
     * files.
     */
    static protected ResourceBundle m_localeRes;

    /**
     * The info section of the RTF header. Specifies document title, author,
     * creation and revision dates etc.
     */
    protected String m_strRTFInfoGroup;
    protected String m_strEOL = "\r\n";

    protected int m_neutralLcid = 0000;
    protected int m_sourceLcid = 1033;
    protected int m_targetLcid = 1033;
    protected int m_labelAndHeaderLcid = 1033;
    protected int m_issueLcid = m_neutralLcid;

    protected boolean m_sourceIsRtlLang = false;
    protected boolean m_targetIsRtlLang = false;
    protected boolean m_labelAndHeaderIsRtlLang = false;
    protected boolean m_issueIsRtlLang = false;

    // language of "# id" markers etc, always en_US
    protected final String m_strDefaultLcid = "\\lang1033\\langfe1033\\cgrid\\langnp1033\\langfenp1033";
    // language of source text
    protected String m_strSourceLcid = m_strDefaultLcid;
    // language of target text
    protected String m_strTargetLcid = m_strDefaultLcid;
    // language of comment text
    protected String m_strLabelAndHeaderLcid = m_strDefaultLcid;
    // language of comment text
    protected String m_strIssueLcid = m_strDefaultLcid;

    /** Shorthand for the default document style */
    protected String m_strDefaultStyle;
    /** Shorthand for the style of normal text (source segments). */
    protected String m_strSourceTextStyle;
    /**
     * Shorthand for the style of source with an available fuzzy match (not
     * inserted).
     */
    protected String m_strSourceTextFuzzyStyle;
    /** Shorthand for the style of normal text (target segments). */
    protected String m_strTargetTextStyle;
    /** Shorthand for the style of exact match segments. */
    protected String m_strTargetTextExactStyle;
    /** Shorthand for the style of Unverified exact match segments. */
    protected String m_strTargetTextUnverifiedExactStyle;
    /** Shorthand for the style of a locked exact match segments. */
    protected String m_strTargetTextLockedExactStyle;
    /** Shorthand for the style of target text inserted as a fuzzy match. */
    protected String m_strTargetTextFuzzyStyle;
    /** Shorthand for the style of label and header text. */
    protected String m_strLabelAndHeaderTextStyle;

    // /** Shorthand for the style of editable issue fields. */
    // protected String m_strEditableIssueFieldLabelStyle;
    // /** Shorthand for the style of non-editable issues fields. */
    // protected String m_strNonEditableIssueFieldLabelStyle;
    // /** Shorthand for the style of editable issue text. */
    // protected String m_strEditableIssueTextStyle;
    // /** Shorthand for the style of non-editable issue text. */
    // protected String m_strNonEditableIssueTextStyle;

    /** Shorthand for the style of editor info text. */
    protected String m_strEditorInfoTextStyle;
    /** Shorthand for the style of ptags tags. */
    protected String m_strDefaultPtagStyle;
    /** Shorthand for the style of internal tags (tw4winInternal). */
    protected String m_strInternalStyle;
    /** Shorthand for the style of external tags (tw4winExternal). */
    protected String m_strExternalStyle;
    /** Shorthand for the style of external tags with the target font. */
    protected String m_strExternalStyleWithTargetFont;
    /** Shorthand for Workbench segment markers (tw4winMark). */
    protected String m_strMarkerStyle;

    /**
     * The input page object from which the RTF output is derived.
     */
    protected OfflinePageData m_page;

    /**
     * The ui locale. Used to write comments and other header text when
     * possible.
     */
    protected Locale m_uiLocale = GlobalSightLocale
            .makeLocaleFromString("en_US");

    /**
     * The output stream to which we write the RTF results.
     */
    protected OutputStreamWriter m_outputStream;

    //
    // Constructors
    //

    /**
     * Constructs an RTFWriterUnicode without setting the OfflinePageData. You
     * must call setOfflinePageData() before writing can begin.
     */
    public RTFWriterUnicode()
    {
    }

    //
    // Protected Abstract Methods
    //

    /**
     * Extend this method to generates an RTF {\info} block with creation date,
     * document title etc. The info section becomes part of the encoded RTF
     * header.
     */
    protected abstract String makeRTF_infoSection()
            throws AmbassadorDwUpException;

    /**
     * Extend this method to create docment variables. The doc variables section
     * is part of the encoded RTF header.
     */
    protected abstract String makeRTF_documentVariables()
            throws AmbassadorDwUpException;

    /**
     * Extend this method to create the template attachment section. The
     * template section is part of the encoded RTF header.
     */
    protected abstract String makeRTF_templateAttachment()
            throws AmbassadorDwUpException;

    /**
     * Extend this method to create the document default view section. The dco
     * defaults section is part of the encoded RTF header.
     */
    protected abstract String makeRTF_documentDefaults()
            throws AmbassadorDwUpException;

    /**
     * Extend this method to write a visible ambassador file header, a section
     * that the user sees before any segments.
     */
    protected abstract void writeRTF_docHeader() throws IOException,
            AmbassadorDwUpException;

    /**
     * Extend this method to write the main body of the document. Classes that
     * extend RTFWriterUnicode should use this method to create the desired
     * segment presentation format.
     */
    protected abstract void writeRTF_docBody() throws IOException,
            AmbassadorDwUpException;

    //
    // Protected Methods
    //

    /**
     * Sets the OfflinePageData object that will be written as RTF. Call
     * {@link #writeRTF(OutputStream)} to run the conversion.
     * 
     * @param page
     *            an OfflinePageData object
     * @param p_uiLocale
     *            the locale used to write the Header and other non-translatable
     *            instructions
     */
    protected void setOfflinePageData(OfflinePageData p_page, Locale p_uiLocale)
    {
        m_page = p_page;
    }

    /**
     * This is a common base method that should be used to write **all**
     * segments to the output stream.
     * 
     * @param p_bmName
     *            if not null, this bookmark will enclose the segment.
     * @param p_segment
     *            the segment stext
     * @param p_textStyle
     *            contains the RTF style string for text
     * @param p_tagStyle
     *            contains the RTF style string for non-rendered tags
     * @param p_isRtlLang
     *            set true for bidi langs
     * @param p_cvtTagToFld
     *            when true, non-rendered tags will be converted to ms-Word
     *            MACROBUTTON fields.
     * @param p_renderTags
     *            when true, tags will be converted to native RTF (when
     *            possible!)
     * @param p_issue
     *            the associated segment level Issue object
     */
    protected void writeRTF_docSegment(String p_bmName, String p_segment,
            String p_textStyle, String p_tagStyle, boolean p_isRtlLang,
            boolean p_cvtTagToFld, String p_nativeFormat, boolean p_renderTags,
            Issue p_issue, User p_user) throws IOException
    {
        m_outputStream.write(makeRTF_docSegment(p_bmName, p_segment,
                p_textStyle, p_tagStyle, p_isRtlLang, p_cvtTagToFld,
                p_nativeFormat, p_renderTags, p_issue, p_user));
    }

    /**
     * This is a common base method that should be used to write **all**
     * segments to the output stream.
     * 
     * @param p_bmName
     *            if not null, this bookmark will enclose the segment.
     * @param p_segment
     *            the segment stext
     * @param p_textStyle
     *            contains the RTF style string for text
     * @param p_tagStyle
     *            contains the RTF style string for non-rendered tags
     * @param p_isRtlLang
     *            set true for bidi langs
     * @param p_cvtTagToFld
     *            when true, non-rendered tags will be converted to ms-Word
     *            MACROBUTTON fields.
     * @param p_renderTags
     *            when true, tags will be converted to native RTF (when
     *            possible!)
     * @param p_issue
     *            the associated segment level Issue object
     * @param p_user
     *            the download user.
     */
    public String makeRTF_docSegment(String p_bmName, String p_segment,
            String p_textStyle, String p_tagStyle, boolean p_isRtlLang,
            boolean p_cvtTagToFld, String p_nativeFormat, boolean p_renderTags,
            Issue p_issue, User p_user)
    {
        StringBuffer sb = new StringBuffer();

        boolean bookmark = (p_bmName != null && p_bmName.length() > 0);
        String segTextRTF = formatSegmentText(p_segment, p_tagStyle,
                p_isRtlLang, p_cvtTagToFld, p_nativeFormat, p_renderTags);

        sb.append("{");
        sb.append(p_textStyle);

        if (bookmark)
        {
            sb.append(makeRTF_bookmark(true, p_bmName));
        }

        sb.append(makeSegmentIssueAnnotation(p_issue, p_user));
        sb.append(segTextRTF);

        if (bookmark)
        {
            sb.append(makeRTF_bookmark(false, p_bmName));
        }

        sb.append("}");
        sb.append(m_strEOL);

        return sb.toString();
    }

    protected String makeStyleSheet()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(RTF_STYLESHEET_START);
        // default document style ("Normal") is in target language,
        // so you can hit ^space (normal style) and start overwriting
        // text in the target language
        sb.append("\\lang");
        sb.append(m_targetLcid);
        sb.append("\\langfe");
        sb.append(m_targetLcid);
        sb.append("\\langnp");
        sb.append(m_targetLcid);
        sb.append("\\langfenp");
        sb.append(m_targetLcid);
        sb.append(RTF_STYLESHEET_END);
        return sb.toString();
    }

    /**
     * Makes a bookmark name using the segment id. The name includes old segment
     * attributes.
     * 
     * @param p_osd
     *            an offline segment object.
     * @param p_suffix
     *            string to appended to the root id (can be null).
     * @return string
     */
    protected String makeBookmarkName(OfflineSegmentData p_osd, String p_suffix)
    {
        return makeBookmarkName(true, p_osd, p_suffix);
    }

    /**
     * Makes a bookmark name using the segment id.
     * 
     * @param p_osd
     *            an offline segment object.
     * @return string
     */
    protected String newMakeBookmarkName(OfflineSegmentData p_osd)
    {
        return makeBookmarkName(false, p_osd, null);
    }

    /**
     * Makes a bookmark name using the segment id. The name does NOT include old
     * segment attributes (now in index file).
     * 
     * @param p_oldStyle
     *            build original name with prefix attributes.
     * @param p_osd
     *            an offline segment object.
     * @param p_suffix
     *            string to appended to the root id (can be null).
     * @return string
     */
    private String makeBookmarkName(boolean p_oldStyle,
            OfflineSegmentData p_osd, String p_suffix)
    {
        StringBuffer rslt = new StringBuffer();

        if (!p_oldStyle)
        {
            rslt.append(BM_PREFIX);
        }
        else if (p_osd.isWriteAsProtectedSegment())
        {
            rslt.append(BM_PREFIX_LOCKED);
        }
        else
        {
            switch (p_osd.getMatchTypeId())
            {
                case AmbassadorDwUpConstants.MATCH_TYPE_EXACT:
                    rslt.append(BM_PREFIX_EXACT);
                    break;
                case AmbassadorDwUpConstants.MATCH_TYPE_FUZZY:
                    rslt.append(BM_PREFIX_FUZZY);
                    break;
                case AmbassadorDwUpConstants.MATCH_TYPE_NOMATCH:
                    rslt.append(BM_PREFIX_NOMATCH);
                    break;
                default:
                    rslt.append(BM_PREFIX_LOCKED);
                    break;
            }
        }

        rslt.append(p_osd.getDisplaySegmentID());

        // addable ptag category
        if (p_oldStyle
                && p_osd.getDisplaySegmentFormat().toLowerCase().equals("html")
                && p_osd.getSegmentType().toLowerCase().equals("text"))
        {
            rslt.append(WC_SUFFIX_SEGID_ADDABLE_HTML);
        }

        if (p_suffix != null)
        {
            rslt.append(p_suffix);
        }

        return rslt.toString();
    }

    protected void setDefaults()
    {
        m_localeRes = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);

        String str_sourceLocale = m_page.getSourceLocaleName().trim();
        String str_targetLocale = m_page.getTargetLocaleName().trim();
        String str_labelAndHeaderLocale = m_uiLocale.getLanguage() + "_"
                + m_uiLocale.getCountry();
        String str_issueLocale = m_uiLocale.getLanguage() + "_"
                + m_uiLocale.getCountry();

        // set up the parameters we need: source locale id, lang
        try
        {
            m_sourceLcid = LCID.getLocaleId(str_sourceLocale);
        }
        catch (MissingResourceException ex)
        {
            m_sourceLcid = 0;
        }
        m_strSourceLcid = "\\lang" + m_sourceLcid;

        // set up the parameters we need: target locale id, lang
        try
        {
            m_targetLcid = LCID.getLocaleId(str_targetLocale);
        }
        catch (MissingResourceException ex)
        {
            m_targetLcid = 0;
        }
        m_strTargetLcid = "\\lang" + m_targetLcid;

        // set up the parameters we need: Header and label locale id, lang
        try
        {
            m_labelAndHeaderLcid = LCID.getLocaleId(str_labelAndHeaderLocale);
        }
        catch (MissingResourceException ex)
        {
            m_labelAndHeaderLcid = 0;
        }
        m_strLabelAndHeaderLcid = "\\lang" + m_labelAndHeaderLcid;

        // set up the parameters we need: issue locale id, lang
        try
        {
            m_issueLcid = LCID.getLocaleId(str_issueLocale);
        }
        catch (MissingResourceException ex)
        {
            m_issueLcid = 0;
        }
        m_strIssueLcid = "\\lang" + m_issueLcid;

        // NOTE: Word 97 ignores rtl/ltr tokens - there is no
        // directionality in word 97
        if (EditUtil.isRTLLocale(str_sourceLocale))
        {
            m_sourceIsRtlLang = true;
        }

        if (EditUtil.isRTLLocale(str_targetLocale))
        {
            m_targetIsRtlLang = true;
        }

        if (EditUtil.isRTLLocale(str_labelAndHeaderLocale))
        {
            m_labelAndHeaderIsRtlLang = true;
        }

        if (EditUtil.isRTLLocale(str_issueLocale))
        {
            m_issueIsRtlLang = true;
        }

        m_strSourceLcid = "\\lang" + m_sourceLcid + "\\langfe" + m_sourceLcid
                + "\\cgrid\\langnp" + m_sourceLcid + "\\langfenp"
                + m_sourceLcid;
        m_strTargetLcid = "\\lang" + m_targetLcid + "\\langfe" + m_targetLcid
                + "\\cgrid\\langnp" + m_targetLcid + "\\langfenp"
                + m_targetLcid;
        m_strLabelAndHeaderLcid = "\\lang" + m_labelAndHeaderLcid + "\\langfe"
                + m_labelAndHeaderLcid + "\\cgrid\\langnp"
                + m_labelAndHeaderLcid + "\\langfenp" + m_labelAndHeaderLcid;
        m_strIssueLcid = "\\lang" + m_issueLcid + "\\langfe" + m_issueLcid
                + "\\cgrid\\langnp" + m_issueLcid + "\\langfenp" + m_issueLcid;

        // set up shortcuts for the RTF controls we need
        m_strDefaultStyle = PSTYLE_PLAIN + FONT_ARIAL + SIZE_12PT
                + m_strDefaultLcid + " ";

        // style for source segments (text still in source language)
        m_strSourceTextStyle = FONT_SOURCE + SIZE_12PT + m_strSourceLcid + " ";

        if (m_sourceIsRtlLang)
        {
            m_strSourceTextStyle = "\\rtlch" + m_strSourceTextStyle;
        }

        // style for source text with a fuzzy match (text is **NOT**
        // in target language)
        m_strSourceTextFuzzyStyle = FONT_SOURCE + SIZE_12PT + COLOR_PURPLE
                + m_strSourceLcid + " "; // use source lcid

        if (m_sourceIsRtlLang)
        {
            m_strSourceTextFuzzyStyle = "\\rtlch" + m_strSourceTextFuzzyStyle;
        }

        // style for target segments (text already in target language)
        m_strTargetTextStyle = FONT_TARGET + SIZE_12PT + m_strTargetLcid + " ";

        if (m_targetIsRtlLang)
        {
            m_strTargetTextStyle = "\\rtlch" + m_strTargetTextStyle;
        }

        // style for exact target segments (text already in target language)
        m_strTargetTextExactStyle = FONT_TARGET + SIZE_12PT + COLOR_DARK_GREEN
                + m_strTargetLcid + " ";

        if (m_targetIsRtlLang)
        {
            m_strTargetTextExactStyle = "\\rtlch" + m_strTargetTextExactStyle;
        }

        // style for Unverified exact target segments (text already in
        // target language)
        m_strTargetTextUnverifiedExactStyle = FONT_TARGET + SIZE_12PT
                + COLOR_LIGHT_GREEN + m_strTargetLcid + " ";

        if (m_targetIsRtlLang)
        {
            m_strTargetTextUnverifiedExactStyle = "\\rtlch"
                    + m_strTargetTextUnverifiedExactStyle;
        }

        // style for exact target segments (text already in target language)
        m_strTargetTextLockedExactStyle = FONT_TARGET + SIZE_12PT + COLOR_GREY
                + m_strTargetLcid + " ";

        if (m_targetIsRtlLang)
        {
            m_strTargetTextLockedExactStyle = "\\rtlch"
                    + m_strTargetTextLockedExactStyle;
        }

        // style for target text which is an inserted fuzzy match
        // (text **is* in target language)
        m_strTargetTextFuzzyStyle = FONT_TARGET + SIZE_12PT + COLOR_PURPLE
                + m_strTargetLcid + " "; // use source lcid

        if (m_targetIsRtlLang)
        {
            m_strTargetTextFuzzyStyle = "\\rtlch" + m_strTargetTextFuzzyStyle;
        }

        // style for comment and header text
        // NOTE: lets try using ARIAL instead of the comment font (FONT_COMMENT)
        // looks better.
        m_strLabelAndHeaderTextStyle = CSTYLE_GS_COMMENT + FONT_ARIAL
                + SIZE_10PT + COLOR_GREY + m_strLabelAndHeaderLcid + " ";

        if (m_labelAndHeaderIsRtlLang)
        {
            m_strLabelAndHeaderTextStyle = "\\rtlch"
                    + m_strLabelAndHeaderTextStyle;
        }

        // style for editable issue field
        //
        // m_strEditableIssueFieldLabelStyle = CSTYLE_ANNOTATION_REF +
        // FONT_ARIAL +
        // SIZE_10PT + m_strIssueLcid + " ";
        //
        // if (m_issueIsRtlLang)
        // {
        // m_strEditableIssueFieldLabelStyle = "\\rtlch" +
        // m_strEditableIssueFieldLabelStyle;
        // }
        //
        // // style for non-editable issue field
        // m_strNonEditableIssueFieldLabelStyle = CSTYLE_ANNOTATION_REF +
        // FONT_ARIAL +
        // SIZE_10PT + COLOR_GREY + m_strIssueLcid + " ";
        //
        // if (m_issueIsRtlLang)
        // {
        // m_strNonEditableIssueFieldLabelStyle = "\\rtlch" +
        // m_strNonEditableIssueFieldLabelStyle;
        // }
        //
        // // style for editable issue text
        // m_strEditableIssueTextStyle = CSTYLE_ANNOTATION_REF + FONT_ARIAL +
        // SIZE_10PT + COLOR_GREY + m_strIssueLcid + " ";
        //
        // if (m_issueIsRtlLang)
        // {
        // m_strEditableIssueTextStyle = "\\rtlch" +
        // m_strEditableIssueTextStyle;
        // }
        //
        // // style for non-editable issue text
        // m_strNonEditableIssueTextStyle = CSTYLE_ANNOTATION_REF + FONT_ARIAL +
        // SIZE_10PT + m_strIssueLcid + " ";
        //
        // if (m_issueIsRtlLang)
        // {
        // m_strNonEditableIssueTextStyle = "\\rtlch" +
        // m_strNonEditableIssueTextStyle;
        // }

        // style for the editor info text (use label Lcid for now)
        m_strEditorInfoTextStyle = CSTYLE_GS_INFO + FONT_TIMES + SIZE_10PT
                + COLOR_BLUE + m_strLabelAndHeaderLcid + " ";

        if (m_labelAndHeaderIsRtlLang)
        {
            m_strEditorInfoTextStyle = "\\rtlch" + m_strEditorInfoTextStyle;
        }

        // default ptag style
        m_strDefaultPtagStyle = CSTYLE_GS_PTAG + FONT_ARIAL + SIZE_8PT
                + COLOR_GREY /* + m_strDefaultLcid */+ " ";

        // Trados styles
        m_strInternalStyle = CSTYLE_TW4WIN_INTERNAL + COLOR_RED + FONT_ARIAL
                + SIZE_12PT /* + m_strDefaultLcid */+ " ";
        m_strExternalStyle = CSTYLE_TW4WIN_EXTERNAL + COLOR_GREY + FONT_COURIER
                + SIZE_10PT /* + m_strDefaultLcid */+ " ";
        m_strExternalStyleWithTargetFont = CSTYLE_TW4WIN_EXTERNAL + COLOR_GREY
                + FONT_TARGET + SIZE_12PT /* + m_strDefaultLcid */+ " ";
        m_strMarkerStyle = CSTYLE_TW4WIN_MARK + COLOR_DARK_GREEN + FONT_ARIAL
                + "\\sub\\v ";

    }

    //
    // Public Methods
    //

    /**
     * Returns true if we exceeded MS-words limitations for bookmarks, returns
     * false otherwise.
     * 
     * @see http://support.microsoft.com/default.aspx?scid=kb;en-us;211489
     */
    public boolean isMSWord2000BmLimitExceeded()
    {
        // we reserve space for 50 dynamic bookmarks
        return (getBookmarkCnt() >= (AmbassadorDwUpConstants.MSWORD2000_BOOKMARK_LIMIT - 50));
    }

    /**
     * Returns true if we exceeded MS-words limitations for fields, returns
     * false otherwise.
     * 
     * @see http://support.microsoft.com/default.aspx?scid=kb;en-us;211489
     */
    public boolean isMSWord2000FieldLimitExceeded()
    {
        return (getFieldCnt() >= AmbassadorDwUpConstants.MSWORD2000_FIELD_LIMIT);
    }

    /**
     * Sets a new OfflinePageData and writes the resulting RTF to the output
     * stream.
     * 
     * @param p_page
     *            the OfflinePageData object that will be written as RTF.
     * @param p_outputStream
     *            the stream to write to
     * @param p_uiLocale
     *            the locale used to write the Header and other non-translatable
     *            instructions
     */
    public void write(OfflinePageData p_page, OutputStream p_outputStream,
            Locale p_uiLocale) throws IOException, AmbassadorDwUpException
    {
        m_outputStream = new OutputStreamWriter(p_outputStream, "ASCII");
        m_page = p_page;
        m_uiLocale = p_uiLocale;

        writeRTF();

        m_outputStream.flush();
    }

    /**
     * Runs the conversion on the current OfflinePAgeData and writes the
     * resulting RTF to the output stream. The page must have been initialized
     * by {@link #setOfflinePageData(OfflinePageData) setOfflinePageData()}.
     * 
     * @param p_outputStream
     *            the stream to write to
     * @param p_uiLocale
     *            the locale used to write the Header and other non-translatable
     *            instructions
     */
    public void writeRTF(OutputStream p_outputStream, Locale p_uiLocale)
            throws IOException, AmbassadorDwUpException
    {
        m_outputStream = new OutputStreamWriter(p_outputStream, "ASCII");

        writeRTF();

        m_outputStream.flush();
    }

    //
    // Private Methods
    //

    /**
     * <p>
     * Runs the conversion on the current OfflinePageData object and writes the
     * resulting RTF to the output stream.
     * </p>
     */
    private void writeRTF() throws IOException, AmbassadorDwUpException
    {
        setDefaults();

        m_outputStream.write("{");

        writeRTF_rtfHeader();

        m_outputStream.write(m_strDefaultStyle);

        writeRTF_docHeader();

        writeRTF_docBody();

        writeRTF_rtfEnd();
    }

    /**
     * <p>
     * Writes an RTF header to the output stream.
     * </p>
     * 
     * @throws AmbassadorDwUpException
     *             with a code of
     *             AmbassadorDwUpExceptionConstants.RTF_MISSING_RESOURCE when
     *             other properties like <code>RTFHeaderStart</code> cannot be
     *             found.
     */
    private void writeRTF_rtfHeader() throws IOException,
            AmbassadorDwUpException
    {
        Exception error = null;
        String str_sourceFont = getFont(m_page.getSourceLocaleName());
        String str_targetFont = getFont(m_page.getTargetLocaleName());
        String str_commentFont = getFont(m_uiLocale);

        m_outputStream.write(RTF_HEADER_START);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(RTF_FONT_TABLE_START);
        m_outputStream.write("{\\f0" + str_sourceFont + "}");
        m_outputStream.write("{\\f1" + str_targetFont + "}");
        m_outputStream.write("{\\f2" + str_commentFont + "}");
        m_outputStream.write(RTF_FONT_TABLE_END);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(COLOR_TABLE);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(makeStyleSheet());
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(makeRTF_infoSection());
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(makeRTF_documentVariables());
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        m_outputStream.write(makeRTF_templateAttachment());
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);
        // set document defaults after the header
        m_outputStream.write(makeRTF_documentDefaults());
        m_outputStream.write(m_strEOL);
        m_outputStream.write(m_strEOL);

        // Switch the writing direction of the document when the
        // target language is Hebrew or Arabic.
        if (m_targetIsRtlLang)
        {
            m_outputStream.write("\\rtlpar");
        }

        m_outputStream.write(m_strEOL);
    }

    /**
     * Writes the end signature to the document.
     */
    private void writeRTF_rtfEnd() throws IOException
    {
        m_outputStream.write("\\par}");
        m_outputStream.write(m_strEOL);
    }

    private String getFont(Locale p_locale) throws AmbassadorDwUpException
    {
        StringBuffer sb = new StringBuffer();

        sb.append(p_locale.getLanguage());
        sb.append("-");
        sb.append(p_locale.getCountry());

        return getFont(sb.toString());
    }

    private String getFont(String p_locale) throws AmbassadorDwUpException
    {
        String locale = p_locale.toLowerCase();
        String result;

        if (locale.length() > 2 && locale.charAt(2) != '-')
        {
            locale = locale.substring(0, 2) + "-" + locale.substring(3);
        }

        // carefully find a default font in which to output text
        String srcFontKey = DEFAULT_FONT_DOT + locale;
        if (m_resourceKeys.contains(srcFontKey))
        {
            result = m_resource.getString(srcFontKey);
        }
        else
        {
            locale = locale.substring(0, 2);
            srcFontKey = DEFAULT_FONT_DOT + locale;

            if (m_resourceKeys.contains(srcFontKey))
            {
                result = m_resource.getString(srcFontKey);
            }
            else
            {
                c_logger.info("No font for locale " + locale + ", using Arial.");

                try
                {
                    result = m_resource.getString(DEFAULT_FONT);
                }
                catch (MissingResourceException ex)
                {
                    throw new AmbassadorDwUpException(
                            AmbassadorDwUpExceptionConstants.RTF_MISSING_RESOURCE,
                            ex);
                }
            }
        }

        return result;
    }

    protected void debug_writeRTF_dummyBookmarks(int p_howMany)
            throws IOException
    {
        int cnt = 0;

        while (cnt < p_howMany)
        {
            cnt++;
            String infoBkmName = "" + cnt;

            m_outputStream.write("{ ");
            m_outputStream.write(makeRTF_bookmark(BM_OPEN,
                    infoBkmName.toString()));
            m_outputStream.write(makeRTF_bookmark(BM_CLOSE,
                    infoBkmName.toString()));
            m_outputStream.write("}\\par ");
        }
    }

    protected void debug_writeRTF_dummyFields(int p_howMany) throws IOException
    {
        int cnt = 0;

        while (cnt < p_howMany)
        {
            cnt++;
            String fldInst = "MACROBUTTON ptag";
            m_outputStream.write("{ ");
            m_outputStream.write(makeRTF_field(true, true, true, true, fldInst,
                    "result"));
            m_outputStream.write("}");
        }
    }

    /**
     * Creates a segment level issue and history as an annotation field.
     * 
     * @param p_segData
     *            - reference to OfflineSegmentData
     */
    protected String makeSegmentIssueAnnotation(Issue p_issue, User p_user)
    {
        StringBuffer sb = new StringBuffer();

        if (p_issue != null && p_user != null)
        {
            // initialization groups
            sb.append(makeRTF_annotationIdandAuthor("Cmt",
                    UserUtil.getUserNameById(p_issue.getCreatorId())));

            // open the annotation group
            sb.append("{");
            sb.append("\\*\\annotation \\pard\\plain ");
            sb.append(PSTYLE_ANNOTATION_TXT);
            sb.append(FONT_ARIAL);
            sb.append(SIZE_10PT);
            sb.append(" {");
            sb.append(CSTYLE_ANNOTATION_REF);
            sb.append("\\chatn }\\par"); // atnid - annotation frame

            sb.append(formatSegmentIssueAtnEntry(p_issue, p_user));
            sb.append("\\par");

            // close the annotation group
            sb.append("}");
        }

        return sb.toString();
    }

    // -----------------------------------
    // Formating methods
    // -----------------------------------

    /**
     * Helper function, returns source text formatted for a source-text
     * annotation.
     */
    private String formatSegmentIssueAtnEntry(Issue p_issue, User p_user)
    {
        List history = p_issue.getHistory();
        boolean editable = false;
        StringBuffer sb = new StringBuffer();

        // start Issue general formatting
        // sb.append("{");

        // title line ------------------
        editable = (p_user != null)
                && p_issue.getCreatorId().equalsIgnoreCase(p_user.getUserId());

        sb.append(m_strEOL);
        sb.append(formatIssueTitle(p_issue, editable));
        sb.append(m_strEOL);

        if (history != null)
        {
            ListIterator it = history.listIterator();
            IssueHistory issHis = null;
            boolean first = true;
            boolean firstIsCreatorCmt = false;
            editable = false;

            while (it.hasNext())
            {
                issHis = (IssueHistory) it.next();
                firstIsCreatorCmt = issHis.reportedBy().equalsIgnoreCase(
                        p_user.getUserId());

                if (first && !firstIsCreatorCmt)
                {
                    sb.append(formatEmptyReply());
                    sb.append(m_strEOL);
                }

                editable = first ? firstIsCreatorCmt : false;

                sb.append(formatIssueHistoryInfo(issHis, editable));
                sb.append(m_strEOL);
                sb.append(formatIssueHistoryComment(issHis, editable));
                sb.append(m_strEOL);

                first = false;
            }
        }

        // end Issue formatting
        // sb.append("}");

        return sb.toString();
    }

    // title formatter
    private String formatIssueTitle(Issue p_issue, boolean p_editable)
    {
        StringBuffer sb = new StringBuffer();

        // Title (always editable)
        sb.append("{");
        sb.append(formatFieldStart(true, "MyTitle"));
        sb.append("=");
        sb.append(encodeText(p_issue.getTitle()));
        sb.append(formatFieldEnd(true));
        sb.append("}");

        // Status (always editable)
        sb.append("{, ");
        sb.append(formatFieldStart(true, "Status"));
        sb.append("=");
        // sb.append("{\\b"); //open bold
        sb.append(encodeText(m_localeRes.getString("issue.status."
                + p_issue.getStatus())));
        // sb.append("}"); // close bold
        sb.append(formatFieldEnd(true));
        sb.append("}");

        // Priority (always editable)
        sb.append("{, ");
        sb.append(formatFieldStart(true, "Priority"));
        sb.append("=");
        // sb.append("{\\b ="); // open bold
        sb.append(encodeText(m_localeRes.getString("issue.priority."
                + p_issue.getPriority())));
        // sb.append("}"); // close bold
        sb.append(formatFieldEnd(true));
        sb.append("}");

        // Category (always editable)
        sb.append("{, ");
        sb.append(formatFieldStart(true, "Category"));
        sb.append("=");
        // sb.append("{\\b ="); // open bold
        sb.append(encodeText(p_issue.getCategory()));
        // sb.append("}"); // close bold
        sb.append(formatFieldEnd(true));
        sb.append("}");

        // end
        sb.append("\\par");

        return sb.toString();
    }

    private String formatFieldStart(boolean p_editable, String p_fieldName)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{");
        sb.append(p_editable ? COLOR_BLACK : COLOR_GREY);
        sb.append("\\b");
        sb.append(p_editable ? EDIT_FLD_OPEN : "");
        sb.append(encodeText(p_fieldName));
        sb.append("}");

        return sb.toString();
    }

    private String formatFieldEnd(boolean p_editable)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{");
        sb.append(p_editable ? COLOR_BLACK : COLOR_GREY);
        sb.append("\\b");
        sb.append(p_editable ? EDIT_FLD_CLOSE : "");
        sb.append("}");

        return sb.toString();
    }

    // history level comment formatter
    private String formatEmptyReply()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{");
        sb.append("\\tab");
        sb.append(formatFieldStart(true, "MyReply"));
        sb.append("= ");
        sb.append(formatFieldEnd(true));
        sb.append("\\par}");

        return sb.toString();
    }

    // history level comment formatter
    private String formatIssueHistoryComment(IssueHistory p_issueHistory,
            boolean p_editable)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{");
        sb.append("\\tab ");
        sb.append(formatFieldStart(p_editable, p_editable ? "MyReply" : ""));
        sb.append(p_editable ? "=" : "");
        sb.append(formatIssueComment(p_issueHistory.getComment(), p_editable));
        sb.append(formatFieldEnd(p_editable));
        sb.append("\\par}");

        return sb.toString();
    }

    // base comment formatter
    private String formatIssueComment(String p_cmt, boolean p_editable)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{"); // start cmt
        sb.append(p_editable ? COLOR_BLACK : COLOR_GREY);
        sb.append(" ");
        sb.append(encodeText(p_cmt));
        sb.append("}"); // end cmt

        return sb.toString();
    }

    // history level info
    private String formatIssueHistoryInfo(IssueHistory p_issueHistory,
            boolean p_editable)
    {
        return formatIssueInfo(UserUtil.getUserNameById(p_issueHistory
                .reportedBy()), p_issueHistory.dateReported(),
                p_editable ? COLOR_GREY : COLOR_GREY);
    }

    // base info formatter
    private String formatIssueInfo(String p_creatorId, String p_createdDate,
            String p_color)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{"); // start info
        sb.append(p_color);
        sb.append("{\\b ");
        sb.append(encodeText(p_creatorId));
        sb.append("} ");
        sb.append("{\\i ");
        sb.append(encodeText(p_createdDate));
        sb.append("}");
        sb.append("\\par}"); // end info

        return sb.toString();
    }

    /**
     * A helper function which wraps all ptags in a segment with the given
     * style.
     * 
     * @param p_segment
     *            the segment to be formatted
     * @param p_tagStyle
     *            the style to be applead to tags
     * @param p_isRtlLang
     *            set true if the language is right to left
     * @param p_wrapTagInFld
     *            set ture to wrap tags in RTF fields. If p_renderTags is set,
     *            only the remaining tags that cannot be rendered are wraped.
     * @param p_segNativeFormat
     *            the name of the native format in which extraction took place,
     *            example:"html"
     * @param p_renderTags
     *            when true, tags that can be rendered in RTF will be converted
     *            to native RTF. These same renderings will be converted back to
     *            ptags on upload.
     * @return an encoded RTF string.
     */
    protected String formatSegmentText(String p_segment, String p_tagStyle,
            boolean p_isRtlLang, boolean p_wrapTagInFld,
            String p_segNativeFormat, boolean p_renderTags)
    {
        final int DIR_LTR = 2;
        final int DIR_RTL = 3;
        int direction = p_isRtlLang ? DIR_RTL : DIR_LTR;
        StringBuffer rtfRun = new StringBuffer();
        StringBuffer tagRun = null;
        boolean isEpt = false;

        int i_at = 0;
        int i_max = p_segment.length();

        while (i_at < i_max)
        {
            char ch = p_segment.charAt(i_at);

            if (ch == '[')
            {
                // we can check i+1 because if there's a '[' character
                // at the end of the segment it must be escaped.
                if (p_segment.charAt(i_at + 1) == ch)
                {
                    rtfRun.append(ch);
                    rtfRun.append(ch);
                    ++i_at;
                    ++i_at;
                    continue;
                }
                else
                {
                    tagRun = new StringBuffer();

                    // get tag run
                    tagRun.append(ch);
                    ++i_at;

                    // check ept
                    ch = p_segment.charAt(i_at);
                    isEpt = (ch == PseudoConstants.PSEUDO_END_TAG_MARKER);

                    do
                    {
                        ch = p_segment.charAt(i_at);
                        tagRun.append(ch);
                        ++i_at;
                    } while (ch != ']');

                    rtfRun.append(convertTag(isEpt, tagRun.toString(),
                            p_tagStyle, p_segNativeFormat, p_wrapTagInFld,
                            p_renderTags));

                    continue;
                }
            }
            else if (ch == NORMALIZED_LINEBREAK)
            {
                rtfRun.append(convertTag(false, "[LF]", p_tagStyle,
                        p_segNativeFormat, p_wrapTagInFld, p_renderTags));

                // Note: By this time, the string has been converted to ptag.
                // Unprotected newlines were normalized for Rtf when we
                // got the target text in writeTranslationUnit().
                ++i_at;
                continue;
            }
            else if (p_isRtlLang && direction != DIR_LTR && ch != ' '
                    && !Text.isBidiChar(ch))
            {
                // open left-to-right run
                rtfRun.append("{\\ltrch ");
                direction = DIR_LTR;
            }
            else if (p_isRtlLang && direction == DIR_LTR && Text.isBidiChar(ch))
            {
                // close left-to-right run
                rtfRun.append('}');
                direction = DIR_RTL;
            }

            rtfRun.append(encodeChar(ch));
            ++i_at;
        }

        // end of segment
        if (p_isRtlLang && direction == DIR_LTR)
        {
            // close left-to-right run
            rtfRun.append('}');
            direction = DIR_RTL;
        }

        return rtfRun.toString();
    }

    private String convertTag(boolean p_isEpt, String p_tagRun,
            String p_tagStyle, String p_segNativeFormat,
            boolean p_wrapTagInFld, boolean p_render)
    {
        PseudoData PD = new PseudoData();
        PseudoOverrideMapItem POMI = null;
        String mapKey = null;
        String rtfRun = null;

        // Convert tag if it's renderable and addable.
        if (p_render)
        {
            // 1st, check if the tag is addable in the segments native format
            mapKey = PD.isAddableInFormat(p_tagRun, p_segNativeFormat);

            if (mapKey != null)
            {
                // 2nd, check if tag is addable in rtf
                mapKey = PD.isAddableInFormat(p_tagRun, "rtf");

                if (mapKey != null)
                {
                    // If addable in both formats, it can be recovered on
                    // upload.
                    // So, we can now go ahead and render it.
                    POMI = PD.getOverrideMapItem(mapKey);

                    return (String) (p_isEpt ? POMI.m_hAttributes
                            .get(PseudoConstants.ADDABLE_ENDPAIR_RTF_CONTENT)
                            : POMI.m_hAttributes
                                    .get(PseudoConstants.ADDABLE_RTF_CONTENT));
                }
            }
        }

        return formatPtag(p_tagRun, p_tagStyle, p_wrapTagInFld);
    }

    private String formatPtag(String p_tagRun, String p_tagStyle,
            boolean p_wrapTagInFld)
    {
        // Format the tag:
        // Regardless of the languages in this file, ptags
        // are always English, ASCII and left-to-right.
        StringBuffer rtfRun = new StringBuffer();

        rtfRun.append("{\\ltrch");
        rtfRun.append(p_tagStyle);
        rtfRun.append(encodeText(p_tagRun));
        rtfRun.append('}');

        if (p_wrapTagInFld)
        {
            rtfRun.insert(0, " MACROBUTTON ptag ");
            rtfRun = new StringBuffer(makeRTF_field(false, false, false, true,
                    rtfRun.toString(), ""));
        }

        return rtfRun.toString();
    }

    /**
     * Creates a source text annotation field.
     * 
     * @param p_atnId
     *            - the name of the comment (which appears in document)
     * @param p_atnAuthor
     *            - name of the person who inserted the comment
     * @param p_formattedRtfContent
     *            - the content
     * @param p_textStyle
     *            the style for the annotation text
     * @param p_refStyle
     *            the style of the reference
     */
    // NOT USED
    protected String makeRTF_annotation(String p_atnId, String p_atnAuthor,
            String p_formattedRtfContent, String p_textStyle, String p_refStyle)
    {
        StringBuffer sb = new StringBuffer();

        if (p_atnId != null && p_atnAuthor != null
                && p_formattedRtfContent != null)
        {
            // make initialization groups
            sb.append(makeRTF_annotationIdandAuthor(p_atnId, p_atnAuthor));

            // now open the annotation group
            sb.append("{");
            sb.append("\\*\\annotation \\pard\\plain ");
            sb.append(p_textStyle);
            sb.append(" {");
            sb.append(p_refStyle);
            sb.append("\\chatn }\\par"); // atnid - annotation frame

            sb.append(p_formattedRtfContent);
            sb.append("\\par");

            // close the annotation group
            sb.append("}");
        }

        return sb.toString();
    }

    /**
     * Helper function to make the intial annotation groups for Id and Author.
     * This method is not to be called directly. It is called internally by the
     * various annotation creation methods such as makeTermAnnotation.
     * 
     * @param p_atnId
     *            - the name of the comment (which appears in document)
     * @param p_atnAuthor
     *            - name of the person who inserted the comment
     */
    protected String makeRTF_annotationIdandAuthor(String p_atnId,
            String p_atnAuthor)
    {
        StringBuffer sb = new StringBuffer();

        if (p_atnId != null && p_atnAuthor != null)
        {
            // initialization groups
            sb.append("{\\*\\atnid " + p_atnId + "}");
            sb.append("{\\*\\atnauthor " + encodeText(p_atnAuthor) + "}");
            sb.append("\\chatn"); // atnid - document body
        }

        return sb.toString();
    }

    /**
     * Create a ListView header "note" string
     */
    protected String makeRTF_listViewHeaderNote(String p_msg)
    {
        StringBuffer sb = new StringBuffer();

        sb.append("{" + m_strExternalStyle
                + AmbassadorDwUpConstants.HEADER_NOTES_KEY + " ");
        sb.append(p_msg);
        sb.append("}\\par");
        sb.append(m_strEOL); // required by grammar
        return sb.toString();
    }
}
