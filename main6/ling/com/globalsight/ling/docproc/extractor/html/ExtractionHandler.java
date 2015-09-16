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
package com.globalsight.ling.docproc.extractor.html;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.filterconfiguration.BaseFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.HtmlInternalTag;
import com.globalsight.cxe.entity.filterconfiguration.InternalText;
import com.globalsight.cxe.entity.filterconfiguration.InternalTextHelper;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficeDocFilter;
import com.globalsight.cxe.entity.filterconfiguration.MSOfficePPTFilter;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.common.DiplomatNames;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.RegEx;
import com.globalsight.ling.common.RegExException;
import com.globalsight.ling.common.RegExMatchInterface;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.EFInputDataConstants;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.TmxTagGenerator;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects.ExtendedAttributeList;
import com.globalsight.ling.docproc.extractor.msoffice.ExcelExtractor;
import com.globalsight.ling.docproc.extractor.msoffice.PowerPointExtractor;
import com.globalsight.ling.docproc.extractor.msoffice.WordExtractor;
import com.globalsight.util.edit.EditUtil;

/**
 * <P>
 * Listens to the events from the HTML parser and populates the Output object
 * with Diplomat XML extracted from the tags and text pieces received. Switches
 * to other Extractors as necessary.
 * </P>
 * 
 * <P>
 * If this extractor is called on strings inside a JavaScript context, strings
 * will be output as type "string" and not type "text" (the default).
 * </P>
 */
class ExtractionHandler implements IHtmlHandler, IHTMLConstants,
        ExtractorExceptionConstants
{
    static private final Logger logger = Logger
            .getLogger(ExtractionHandler.class);
    //
    // Constants Section
    //

    // attributes of interest
    private static final String ACTION = "action";
    private static final String SRC = "src";
    private static final String CONTENT = "content";
    private static final String NAME = "name";
    private static final String HTTP_EQUIV = "http-equiv";
    private static final String VALUE = "value";
    private static final String STYLE = "style";
    private final String INVISIBLE = "invisible";
    private final String VISIBLE = "visible";

    //
    // Member Variables Section
    //

    private Output m_output = null;
    private EFInputData m_input = null;
    private Extractor m_extractor = null;
    private Html2TmxMap m_html2TmxMap = null;
    private ExtractionRules m_rules = null;

    private boolean ignoreInvalidHtmlTags = false;

    private String jsFunctionText;

    private List<HtmlInternalTag> htmlInternalTags;

    /**
     * A stack to parse MS Office's nested conditional declarations. We'll push
     * DeclarationContext objects.
     */
    private Stack<DeclarationContext> m_declarationStack = null;
    private Stack<String> m_internalStack = null;

    static private class DeclarationContext
    {
        public String m_context;
        public boolean m_bExtracting;
        public StringBuffer m_strSkippedText;

        public DeclarationContext(String p_context, boolean p_bExtracting,
                StringBuffer p_strSkippedText)
        {
            m_context = p_context;
            m_bExtracting = p_bExtracting;
            m_strSkippedText = p_strSkippedText;
        }
    }

    /**
     * <p>
     * List of all the candidates for extraction (i.e non-breaking tags, text,
     * comments...).
     * </P
     */
    private List<HtmlObjects.HtmlElement> m_extractionCandidates;

    /**
     * <p>
     * True if the list of extraction candidates contains any text.
     * </p>
     */
    private boolean m_bContainsText;

    /**
     * <p>
     * True if we are extracting text, false if we are buffering text for
     * another extractor.
     * </p>
     */
    private boolean m_bExtracting;

    /** The text extracted for another extractor. */
    private StringBuffer m_strSkippedText;

    private StringBuffer m_gsInternalTagBuffer;
    private boolean m_isGSInternalTag = false;

    /** The next extractor to be called. */
    private String m_strSwitchTo;

    /**
     * For untranslatable Word paragraphs and similar cases where an entire
     * well-known element (like
     * <p>
     * ...
     * </p>
     * ) can be added to the skeleton. This variable holds the name of the end
     * tag - so currently only non-embeddable tags like P can be recognized, not
     * spans because spans can be embedded.
     */
    // For added Excel do not translate parameter, there may some embedded
    // tags that should not be translated, we use Stack to hold skippableContext
    // instead of the Ori String.
    // private String m_skippableContext = null;
    private Stack<String> m_skippableContext = new Stack<String>();

    private Stack<String> m_untranslatableContext = new Stack<String>();
    private Stack<String> m_internalStyleContext = new Stack<String>();

    /**
     * <p>
     * True if we are between white-space preserving tags like &lt;PRE&gt;.
     * Enforces special white-space handling.
     * </p>
     */
    private boolean m_bPreserveWhite;

    /**
     * keep all whitespace from GBS-3663
     */
    private boolean m_preserveAllWhite = false;

    /**
     * <p>
     * Flag if an exception has occured inside the visitor methods (they can't
     * throw exceptions).
     * </p>
     */
    private boolean m_bHasError = false;

    /**
     * <p>
     * The message of the exception that occured during visiting the input.
     * </p>
     */
    private String m_strErrorMsg = "";

    /**
     * <p>
     * Flag telling us if we're called from the JavaScript extractor.
     * <p>
     */
    private boolean m_isInsideJavaScript = false;

    /**
     * <p>
     * A counter (po'boy's stack) to match GSA tags. We can <EM>not</EM> output
     * closing GSA tags when they have not been opened. GSA tags go into the
     * output structure and the XmlWriter will pop an end element off its stack
     * and fail with an EmptyStackException when it should close the top-level
     * DIPLOMAT element.
     * </p>
     */
    private int m_gsaCounter = 0;

    /**
     * <p>
     * Flag if an Excel Cell (TD) has started in the skeleton and we must flush
     * its content to the skeleton (/TD).
     * </p>
     */
    private boolean m_bSkipExcelCell;

    private HtmlEntities m_htmlDecoder = new HtmlEntities();

    private int m_iEmbeddedLine;
    private int m_iEmbeddedCol;

    /** Holds the current XSP language (Javascript, VBScript, Java) */
    private int m_xspLanguage;

    private Map<String, String> m_excelStyle2NameMap = new HashMap<String, String>();

    // Handle hidden columns
    private int m_iExcelCol = 0;
    private Map<String, String> m_excelColStyleMap = new HashMap<String, String>();
    private boolean m_excelTrStart = false;
    private int m_iExcelTDNumber = 0;

    // Hidden sheets
    private boolean m_excelHiddenSheet = false;

    // Hidden rows
    private boolean m_excelHiddenRow = false;

    private boolean m_isUntranslatableEndTag = false;

    private boolean m_pptNotes = false;
    private boolean m_needSpecialTagRemoveForPPT = false;

    //
    // Constructor Section
    //

    public ExtractionHandler(EFInputData p_input, Output p_output,
            Extractor p_Extractor, ExtractionRules p_rules, int p_xspLanguage)
    {
        super();

        m_output = p_output;
        m_input = p_input;
        m_extractor = p_Extractor;
        m_html2TmxMap = new Html2TmxMap();
        m_rules = p_rules;
        m_xspLanguage = p_xspLanguage;

        if (p_Extractor.getParentType() != EFInputDataConstants.UNKNOWN)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if (p_Extractor.getParentType() == er
                    .getFormatId(ExtractorRegistry.FORMAT_JAVASCRIPT))
            {
                m_isInsideJavaScript = true;
            }
        }

        if (isExcelExtractor())
        {
            if (m_input.getExcelStyle() == null)
            {
                // This case corresponds to one-to-one relationship
                // when converting xls to html(htm) files, the style
                // definition section is embeded in the converted html file.
                Reader inputReader = m_extractor.readInput();
                initExcelStyleMap(inputReader);
            }
            else
            {
                // This case corresponds to one-to-many relationship
                // when converting xls to html(htm) files, one of which
                // is post-named .css, we have extracted it in
                // MicrosoftWordHelper.java
                // and transformed it through JMS and finaly put it in
                // EFinputData.
                m_excelStyle2NameMap = m_input.getExcelStyle();
            }

        }
    }

    private void initExcelStyleMap(Reader p_inputReader)
    {
        BufferedReader br = new BufferedReader(p_inputReader);
        Map<String, String> tempMap = new HashMap<String, String>();
        try
        {
            String style = "";
            String styleName = "";
            String line = br.readLine();
            while (line != null)
            {
                if (line.equalsIgnoreCase("<style>"))
                {
                    // Now we are in "style" definition section
                    line = br.readLine();
                    while (line != null && !line.equalsIgnoreCase("</stryle>"))
                    {
                        if (line.startsWith(".style"))
                        {
                            // There we encounter one style definition
                            style = line;
                            line = br.readLine();
                            while (!(line.startsWith(".style"))
                                    && (line.indexOf("mso-style-name") == -1))
                            {
                                // Keep looping until we encounter next style
                                // definition
                                // or find the style name.
                                line = br.readLine();
                                continue;
                            }
                            if (line.startsWith(".style"))
                            {
                                // Encounter the next style definition
                                continue;
                            }
                            else if ((line.indexOf("mso-style-name") != -1))
                            {
                                // Find style name
                                int beginIndex = line.indexOf("mso-style-name")
                                        + "mso-style-name:".length();
                                int endIndex = line.indexOf(";", beginIndex);
                                styleName = line
                                        .substring(beginIndex, endIndex);
                                // Map style mark (without ".") to name in the
                                // tem Map
                                tempMap.put(style.substring(1), styleName);
                            }
                        }

                        if (line.startsWith(".x"))
                        {
                            // Now we in class (html class element) definition
                            // which has one style parent.
                            style = line;
                            line = br.readLine();

                            while (!(line.startsWith(".x"))
                                    && (line.indexOf("mso-style-parent") == -1))
                            {
                                // Keep looping until we encounter next class
                                // (html class element)
                                // definition or find style parent.
                                line = br.readLine();
                                continue;
                            }

                            if (line.startsWith(".x"))
                            {
                                // Encounter the next class (html class element)
                                // definition.
                                continue;
                            }
                            else if ((line.indexOf("mso-style-parent") != -1))
                            {
                                // Find style parent
                                int beginIndex = line
                                        .indexOf("mso-style-parent")
                                        + "mso-style-parent:".length();
                                int endIndex = line.indexOf(";", beginIndex);
                                styleName = tempMap.get(line.substring(
                                        beginIndex, endIndex));
                                // Map class (html class element) name to style
                                // parent name.
                                m_excelStyle2NameMap.put(style.substring(1),
                                        styleName);
                            }
                        }
                        line = br.readLine();
                    }
                }
                else if (line.equalsIgnoreCase("</stryle>"))
                {
                    // Style definition terminated.
                    break;
                }
                line = br.readLine();
            }
            br.close();
        }
        catch (Exception e)
        {
            // Do nothing.
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (Exception e)
                {
                    // Do nothing.
                }
            }
        }

    }

    private boolean isCFExtractor()
    {
        return m_extractor instanceof CFExtractor;
    }

    private boolean isXSPExtractor()
    {
        return m_extractor instanceof ASPExtractor
                || m_extractor instanceof JSPExtractor;
    }

    private boolean isMsOfficeExtractor()
    {
        return isWordExtractor() || isPowerPointExtractor()
                || isExcelExtractor();
    }

    private boolean isWordExtractor()
    {
        return m_extractor instanceof WordExtractor;
    }

    private boolean isPowerPointExtractor()
    {
        return m_extractor instanceof PowerPointExtractor;
    }

    private boolean isExcelExtractor()
    {
        return m_extractor instanceof ExcelExtractor;
    }

    private boolean simplifySegments()
    {
        return m_extractor.getSimplifySegments();
    }

    /**
     * <p>
     * Sends back any error message that occured during the extraction. An Empty
     * result means that there was no Error.
     * </p>
     */
    public String checkError()
    {
        if (!m_bHasError)
        {
            return null;
        }
        else if (m_strErrorMsg.length() == 0)
        {
            return "An error has occured during HTML extraction.";
        }

        return m_strErrorMsg;
    }

    //
    // Implementation of Interface - IHtmlHandler
    //

    /**
     * Called once at the beginning of a new document.
     */
    public void handleStart()
    {
        m_bExtracting = true;
        m_bContainsText = false;
        m_bPreserveWhite = false;
        m_strSkippedText = new StringBuffer();
        m_gsInternalTagBuffer = new StringBuffer();
        m_isGSInternalTag = false;
        m_strSwitchTo = "";
        m_extractionCandidates = new ArrayList<HtmlObjects.HtmlElement>();
        m_html2TmxMap.reset();
        m_gsaCounter = 0;
        m_bSkipExcelCell = false;
        m_declarationStack = new Stack<DeclarationContext>();
        m_internalStack = new Stack<String>();
    }

    /**
     * Called once at the end of a document.
     */
    public void handleFinish()
    {
        try
        {
            flushText();

            if (m_gsaCounter > 0)
            {
                throw new ExtractorException(HTML_GS_TAG_ERROR,
                        "GS start tag not closed");
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
        finally
        {
            // also in handleStart() - free memory here.
            m_html2TmxMap.reset();
            m_strSkippedText = null;
            m_strSwitchTo = null;
            m_extractionCandidates = null;
            m_declarationStack = null;
        }
    }

    /**
     * Handle an HTML comment <code>&lt;!-- --&gt;</code>.
     */
    public void handleComment(HtmlObjects.Comment c)
    {
        if (m_bHasError)
        {
            return;
        }

        // Wed Apr 06 19:05:12 2005: don't output material inside
        // discardable decls like footnotes and endnotes.
        if (isInsideDiscardableDecl())
        {
            return;
        }

        // Tue Aug 30 21:29:53 2005: don't output material inside
        // skippable contexts like Word paragraphs whose style is
        // unextractable.
        if (isInsideSkippableContext())
        {
            m_output.addSkeleton(c.toString());
            return;
        }

        if (!m_bExtracting)
        {
            skipText(c.toString());
        }
        else
        {
            String strTempComment = c.getComment().trim();

            // Fri Sep 26 15:39:01 2003 Oracle Addition: preserve
            // comments that contain a paragraph ID (PID). We convert
            // the comment to a different object so it won't be moved
            // out of the paragraph in flushText().
            if (strTempComment.startsWith("BOLOC")
                    || strTempComment.startsWith("EOLOC"))
            {
                addToText(new HtmlObjects.PidComment(c.getComment()));
            }
            // Thu Dec 18 21:54:29 2003 Parse Word-HTML's VML
            // recursively with the HTML Extractor.
            else if (isMsOfficeExtractor() && isVmlComment(strTempComment))
            {
                try
                {
                    flushText();

                    m_output.addSkeleton("<!--");
                    switchToVML(strTempComment);
                    m_output.addSkeleton("-->");
                }
                catch (ExtractorException e)
                {
                    logger.error(e.getMessage(), e);
                    m_bHasError = true;
                    m_strErrorMsg = e.toString();
                }
            }
            else
            {
                if (this.isExcelExtractor()
                        && strTempComment.indexOf("SheetHidden") >= 0)
                {
                    this.m_excelHiddenSheet = true;
                }
                try
                {
                    m_extractor.readMetaMarkup(strTempComment);
                }
                catch (ExtractorException e)
                {
                    // A RegExException, really.
                    // Indicates an error in a regular expression and
                    // could be ignored if AbstractExtractor spit out a
                    // debug time message...
                    logger.error(e.getMessage(), e);
                    m_bHasError = true;
                    m_strErrorMsg = e.toString();
                }

                addToText(c);
            }
        }
    }

    public void handleCfComment(HtmlObjects.CfComment c)
    {
        if (m_bHasError)
        {
            return;
        }

        if (!m_bExtracting)
        {
            skipText(c.toString());
        }
        else
        {
            addToText(c);
        }
    }

    /**
     * Handle an HTML declaration <code>&lt;!DOCTYPE &gt;</code>, and also MS
     * Office's conditional instructions like
     * <code>&lt;![if !vml]&gt;...&lt;![endif]&gt;</code>.
     */
    public void handleDeclaration(HtmlObjects.Declaration d)
    {
        if (m_bHasError)
        {
            return;
        }

        // Tue Aug 30 21:29:53 2005: don't output material inside
        // skippable contexts like Word paragraphs whose style is
        // unextractable.
        if (isInsideSkippableContext())
        {
            m_output.addSkeleton(d.toString());
            return;
        }

        String decl = d.toString();

        // Thu Dec 18 22:29:32 2003: we extract VML instructions now
        // and skip material meant for non-VML-compliant applications.
        // We do this by pretending we're collecting text for a
        // different extractor. This code is also aware of the fact
        // that decls can be nested (example: rotated text boxes).
        if (decl.startsWith("<![if "))
        {
            // Wed Apr 06 19:05:12 2005: footnote and endnote markers
            // should be skipped but there should be no paragraph break.
            if (isDiscardableDecl(decl))
            {
                // don't output the decl and everything inside it
            }
            else
            {
                // flush previous data
                if (!m_bExtracting)
                {
                    m_output.addSkeleton(m_strSkippedText.toString());
                    m_strSkippedText.setLength(0);
                }
                else
                {
                    try
                    {
                        flushText();
                    }
                    catch (ExtractorException e)
                    {
                        logger.error(e.getMessage(), e);
                        m_bHasError = true;
                        m_strErrorMsg = e.toString();
                    }
                }

                // generate paragraph break before the decl
                m_output.addSkeleton(decl);
            }

            pushDeclarationContext(decl);

            if (decl.equals("<![if RotText]>"))
            {
                m_bExtracting = true;
            }
            else
            // !mso, !vml, !supportFootnotes, !supportEndnotes etc
            {
                m_bExtracting = false;
            }
        }
        else if (decl.equals("<![endif]>"))
        {
            // Wed Apr 06 19:05:12 2005: footnote and endnote markers
            // should be skipped but there should be no paragraph break.
            if (isInsideDiscardableDecl())
            {
                // don't output the decl and everything inside it
            }
            else
            {
                if (!m_bExtracting)
                {
                    m_output.addSkeleton(m_strSkippedText.toString());
                    m_strSkippedText.setLength(0);
                }
                else
                {
                    try
                    {
                        flushText();
                    }
                    catch (ExtractorException e)
                    {
                        logger.error(e.getMessage(), e);
                        m_bHasError = true;
                        m_strErrorMsg = e.toString();
                    }
                }

                m_output.addSkeleton(decl);
            }

            popDeclarationContext();
        }
        else
        {
            if (!m_bExtracting)
            {
                skipText(decl);
            }
            else
            {
                addToText(d);
            }
        }
    }

    /**
     * Handle an HTML processing instruction <code>&lt;?  ?&gt;</code>.
     */
    public void handlePI(HtmlObjects.PI t)
    {
        if (m_bHasError)
        {
            return;
        }

        // Wed Apr 06 19:05:12 2005: don't output material inside
        // discardable decls like footnotes and endnotes.
        if (isInsideDiscardableDecl())
        {
            return;
        }

        // Tue Aug 30 21:29:53 2005: don't output material inside
        // skippable contexts like Word paragraphs whose style is
        // unextractable.
        if (isInsideSkippableContext())
        {
            m_output.addSkeleton(t.toString());
            return;
        }

        try
        {
            flushText();
            m_output.addSkeleton(t.toString());
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle an HTML start tag including its attributes.
     */
    public void handleStartTag(HtmlObjects.Tag t)
    {
        if (isPowerPointExtractor() && containsDiv(t))
        {
            m_needSpecialTagRemoveForPPT = true;
        }

        if (this.isExcelExtractor() && this.isExcelTDTag(t))
        {
            ++this.m_iExcelTDNumber;
        }

        if (m_bHasError)
        {
            return;
        }

        // Wed Apr 06 19:05:12 2005: don't output material inside
        // discardable decls like footnotes and endnotes.
        if (isInsideDiscardableDecl())
        {
            return;
        }

        try
        {
            // Tue Aug 30 21:29:53 2005: don't output material inside
            // skippable contexts like Word paragraphs whose style is
            // unextractable.
            if (isInsideSkippableContext())
            {
                addTagToSkeleton(t, false);
                return;
            }

            // for GBS-1793, exclude ppt notes from extraction
            if (isPowerPointExtractor())
            {
                boolean isNotesTag = isNotesTagInMaster(t);
                m_pptNotes = m_pptNotes || isNotesTag;
                MSOfficePPTFilter filter = (MSOfficePPTFilter) m_extractor
                        .getMainFilter();
                boolean shouldExtractNotes = (filter != null && filter
                        .isNotesTranslate());
                if (isNotesTag && !shouldExtractNotes)
                {
                    setSkippableContext(t);
                    m_output.addSkeleton(t.toString());
                    return;
                }
                if (m_pptNotes && isUnextractableContentFromNotes(t))
                {
                    setSkippableContext(t);
                    m_output.addSkeleton(t.toString());
                    return;
                }
                String isMasterTranslate = SystemConfiguration.getInstance()
                        .getStringParameter(
                                SystemConfigParamNames.PPT_MASTER_TRANSLATE);
                if (!m_pptNotes && !Boolean.parseBoolean(isMasterTranslate)
                        && isContentFromMaster(t))
                {
                    setSkippableContext(t);
                    m_output.addSkeleton(t.toString());
                    return;
                }
            }

            // If TOC needn't translate, just put them into skeleton and no
            // extracting
            if (isWordExtractor())
            {
                MSOfficeDocFilter filter = (MSOfficeDocFilter) m_extractor
                        .getMainFilter();

                if (filter != null && !filter.isTocTranslate())
                {
                    if (t.tag.equalsIgnoreCase("p"))
                    {
                        if (t.toString().indexOf("class=MsoToc") > -1)
                        {
                            setSkippableContext(t);
                            m_output.addSkeleton(t.toString());
                            return;
                        }
                    }
                }
            }

            // Tue Aug 30 21:29:53 2005: check for Word paragraph
            // or Excel Cell styles that are marked as not translatable.
            if ((isWordExtractor() || isExcelExtractor())
                    && isSkippableContextStart(t))
            {
                setSkippableContext(t);

                m_output.addSkeleton(t.toString());
                return;
            }

            if (isWordExtractor()
                    && (isUntranslatableStart(t) || isInsideUntranslatableContext())
                    && !t.isClosed && !isUnpairedTag(t))
            {
                setUntranslatableContext(t);
            }

            if (isWordExtractor()
                    && (isInternalStyleStart(t) || isInsideInternalStyleContext())
                    && !t.isClosed && !isUnpairedTag(t))
            {
                setInternalStyleContext(t);
            }

            if (isInsideInternalStyleContext())
            {
                t.isInternalStyleContent = true;
            }

            if (isMsOfficeExtractor())
            {
                // Bold and italics come with unnecessary CSS styles.
                if (t.tag.equalsIgnoreCase("b") || t.tag.equalsIgnoreCase("i"))
                {
                    t.attributes.clear();
                }
            }

            // start tag in <GS-INTERNAL-TEXT>#message here#</GS-INTERNAL-TEXT>
            if (isGSInternalTag(t))
            {
                m_gsInternalTagBuffer.append("<" + t.tag + ">");
                m_isGSInternalTag = true;
                return;
            }

            boolean isInternalTag = isInternalTag(t);
            if (isInternalTag)
            {
                m_internalStack.push(t.tag);
            }

            // Code to skip text between tags that start in skeleton,
            // like Excel's <TD x:xxx>...</TD>.
            if (m_bSkipExcelCell)
            {
                addTagToSkeleton(t, false);
            }
            else if (!m_bExtracting)
            {
                skipText(t.toString());
            }
            else if (m_rules.isInlineTag(t.tag))
            {
                addToText(t);
            }
            else if (isInternalTag)
            {
                addToText(t);
            }
            else if (isInsideUntranslatableContext() && isUnpairedTag(t))
            {
                addToText(t);
            }
            else
            {
                // Excel cells that don't need to be extracted arrive
                // as <TD x:xxx> and must be handled here.
                if (isExcelExtractor())
                {
                    // Thu Nov 14 14:56:40 2002 Office Additions: if a
                    // text cell starts with an apostrophe, Excel
                    // writes a copy of the real cell text to the
                    // x:str attribute. The apostrophe is for Lotus
                    // 1-2-3 compatibility only (' = left, ^ = center,
                    // " = right alignment) and was replaced by cell
                    // formatting. Note that ^ and " will always be
                    // written out as apostrophe. Upon reconversion
                    // from HTML the value in x:str takes precedence
                    // over the cell content.
                    //
                    // To us it means, if we simply remove x:str the
                    // cell content will take precedence (without the
                    // apostrophe), which will produce the desired
                    // result.
                    if (isExcelStringCell(t))
                    {
                        t.attributes.removeAttribute("x:str");
                    }
                    // Excel number cell: don't translate. Set a flag
                    // to flush the cell content to skeleton until the
                    // endtag arrives. If numbers should be changable,
                    // the x:num attribute value must be deleted
                    // (e.g. <TD x:num>123</TD>.
                    if (isExcelNumberCell(t))
                    {
                        // Extract numbers on demand (untested) but
                        // never extract formulas.
                        if (m_rules.doExtractNumbers()
                                && !isExcelFormulaCell(t))
                        {
                            t.attributes.getAttribute("x:num").deleteValue();
                        }
                        else
                        {
                            addTagToSkeleton(t, false);

                            m_bSkipExcelCell = true;

                            return;
                        }
                    }
                    // The contents in the hidden excel rows
                    // don't need to be translated.
                    if (this.isExcelHiddenRow(t))
                    {
                        addTagToSkeleton(t, false);

                        this.m_excelHiddenRow = true;

                        return;
                    }
                    // Gets the excel columns' style.
                    else if (this.isExcelColTag(t))
                    {
                        this.setExcelColStyle(t, ++this.m_iExcelCol);
                    }
                    // Mark each rows open.
                    // because the hidden column is in excel row.
                    else if (this.isExcelTRTag(t))
                    {
                        this.m_excelTrStart = true;
                    }
                    // The contents in the hidden column
                    // don't need to be translated.
                    else if (this.isExcelTDTag(t))
                    {
                        if (this.isExcelHiddenColumn(t, this.m_iExcelTDNumber))
                        {
                            addTagToSkeleton(t, false);

                            m_bSkipExcelCell = true;

                            return;
                        }
                    }
                }

                // set flag for content of <pre>, <listing> etc...
                m_bPreserveWhite = m_rules.isWhitePreservingTag(t.tag);

                flushText();

                // Note: <script> and <style> switching never occurs here.
                // These tags are parsed separately and passed to
                // handleScript() and handleStyle(), respectively.
                // This code is functional for <xml> only.
                if (!t.isClosed && m_rules.isSwitchTag(t.tag))
                {
                    // Reset flag: start skipping text for another
                    // extractor.
                    m_bExtracting = false;
                    m_iEmbeddedLine = m_extractor.getParser().getCurrentLine();
                    m_iEmbeddedCol = m_extractor.getParser().getCurrentColumn();
                    m_strSwitchTo = t.tag;
                }

                addTagToSkeleton(t, false);

                // Mon Sep 27 17:24:31 2004 CvdL: TMX compliance hack,
                // need to suppress TITLE in RTF documents. Remove me.
                if (isMsOfficeExtractor() && t.tag.equalsIgnoreCase("title"))
                {
                    m_extractor.readMetaMarkup("GS_EXCLUDE_NEXT");
                }
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    public void handleCFStartTag(HtmlObjects.CFTag t)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            if (!m_bExtracting)
            {
                skipText(t.toString());
            }
            else if (m_rules.isInlineTag(t.tag))
            {
                addToText(t);
            }
            else
            {
                // set flag for content of <pre>, <listing> etc...
                m_bPreserveWhite = m_rules.isWhitePreservingTag(t.tag);

                flushText();

                addTagToSkeleton(t, true);
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle an HTML end tag.
     */
    public void handleEndTag(HtmlObjects.EndTag t)
    {
        m_isUntranslatableEndTag = false;
        if (m_bHasError)
        {
            return;
        }

        // Wed Apr 06 19:05:12 2005: don't output material inside
        // discardable decls like footnotes and endnotes.
        if (isInsideDiscardableDecl())
        {
            return;
        }

        // Tue Aug 30 21:29:53 2005: don't output material inside
        // skippable contexts like Word paragraphs whose style is
        // unextractable.
        if (isInsideSkippableContext())
        {
            m_output.addSkeleton(t.toString());
            // We just pop this tag up from Stack
            if (isSkippableContextEnd(t))
            {
                // setSkippableContext(null);
                m_skippableContext.pop();
                if (m_pptNotes)
                {
                    m_pptNotes = false;
                }
            }

            return;
        }

        if (isUntranslatableEnd(t))
        {
            m_untranslatableContext.pop();
            m_isUntranslatableEndTag = true;
        }

        if (isInsideInternalStyleContext())
        {
            t.isInternalStyleContent = true;
        }

        if (isInternalStyleEnd(t))
        {
            m_internalStyleContext.pop();
        }

        // end tag in <GS-INTERNAL-TEXT>#message here#</GS-INTERNAL-TEXT>
        if (m_isGSInternalTag)
        {
            m_gsInternalTagBuffer.append(t.toString());
            HtmlObjects.InternalText it = new HtmlObjects.InternalText(
                    m_gsInternalTagBuffer.toString());
            addToText(it);
            m_isGSInternalTag = false;
            m_gsInternalTagBuffer.delete(0, m_gsInternalTagBuffer.length());
            return;
        }

        try
        {
            // Code to skip text between tags that start in skeleton,
            // like Excel's <TD x:xxx>...</TD>.
            if (m_bSkipExcelCell && t.tag.equalsIgnoreCase("TD"))
            {
                m_bSkipExcelCell = false;
            }

            // Resets the column number in each excel row when </tr>.
            if (this.m_excelTrStart && "TR".equalsIgnoreCase(t.tag))
            {
                this.m_iExcelTDNumber = 0;
                this.m_excelTrStart = false;
            }

            // Resets the hiddenRow as false when the hidden row is end.
            if (this.m_excelHiddenRow && t.tag.equalsIgnoreCase("TR"))
            {
                this.m_excelHiddenRow = false;
            }

            // Resets the columns's style of each excel sheet when the sheet is
            // end.
            if (this.isExcelExtractor() && "table".equalsIgnoreCase(t.tag)
                    && !this.m_excelColStyleMap.isEmpty())
            {
                this.m_excelStyle2NameMap.clear();
                this.m_iExcelCol = 0;
                this.m_excelHiddenSheet = false;
            }

            if (m_rules.isSwitchTag(t.tag))
            {
                m_bExtracting = true;

                String skippedText = m_strSkippedText.toString();
                m_strSkippedText.setLength(0);

                boolean b_script = false;

                b_script = skippedText.length() > 0
                        && !Text.isBlank(skippedText); // non-null xml

                // m_strSwitchTo set by handleStartTag(). If allowed
                // by dynamic rules, send content to XML extractor.
                if (b_script && m_strSwitchTo.equalsIgnoreCase("xml")
                        && m_rules.doExtractXml()
                        && m_rules.isSwitchTag(m_strSwitchTo))
                {
                    try
                    {
                        switchToXml(skippedText, false);
                    }
                    catch (ExtractorException e)
                    {
                        // no rules file found or other error - ignore
                        m_output.addTranslatable(skippedText);
                    }
                }
                else
                {
                    m_output.addSkeleton(skippedText);
                }
            }

            m_bPreserveWhite = m_bPreserveWhite
                    && !m_rules.isWhitePreservingTag(t.tag);

            boolean isInternalEndTag = isInternalEndTag(t);
            if (isInternalEndTag)
            {
                m_internalStack.pop();
            }

            if (!m_bExtracting)
            {
                skipText(t.toString());
            }
            else if (m_rules.isInlineTag(t.tag))
            {
                addToText(t);
            }
            else if (isInsideUntranslatableContext())
            {
                addToText(t);
            }
            else if (isInsideInternalStyleContext())
            {
                addToText(t);
            }
            else if (isInternalEndTag)
            {
                addToText(t);
            }
            else if (t.tag.equalsIgnoreCase(DiplomatNames.Element.GSA))
            {
                flushText();

                if (m_gsaCounter > 0)
                {
                    m_output.addGsaEnd();
                    --m_gsaCounter;
                }
                else
                {
                    // End tag without start tag.
                    throw new ExtractorException(HTML_GS_TAG_ERROR,
                            "GS end tag without GS start tag");
                }
            }
            else
            {
                flushText();
                m_output.addSkeleton(t.toString());
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    public void handleCFEndTag(HtmlObjects.EndTag t)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            if (m_rules.isSwitchTag(t.tag))
            {
                m_bExtracting = true;

                m_output.addSkeleton(m_strSkippedText.toString());

                m_strSkippedText.setLength(0);
            }

            m_bPreserveWhite = m_bPreserveWhite
                    && !m_rules.isWhitePreservingTag(t.tag);

            if (!m_bExtracting)
            {
                skipText(t.toString());
            }
            else if (m_rules.isInlineTag(t.tag))
            {
                addToText(t);
            }
            else
            {
                flushText();
                m_output.addSkeleton(t.toString());
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle end of line characters.
     */
    public void handleNewline(HtmlObjects.Newline t)
    {
        if (m_bHasError)
        {
            return;
        }

        // Wed Apr 06 19:05:12 2005: don't output material inside
        // discardable decls like footnotes and endnotes.
        if (isInsideDiscardableDecl())
        {
            return;
        }

        // Tue Aug 30 21:29:53 2005: don't output material inside
        // skippable contexts like Word paragraphs whose style is
        // unextractable.
        if (isInsideSkippableContext())
        {
            m_output.addSkeleton(t.toString());
            return;
        }

        try
        {
            // Code to skip text between tags that start in skeleton,
            // like Excel's <TD x:xxx>...</TD>.
            if (m_bSkipExcelCell || this.m_excelHiddenRow
                    || this.m_excelHiddenSheet)
            {
                m_output.addSkeleton(t.toString());
            }
            else if (!m_bExtracting)
            {
                skipText(t.toString());
            }
            else
            {
                // if in <pre> or we haven't extracted any text or tags yet
                if (m_bPreserveWhite || !isAccumulatingText())
                {
                    flushText();
                    m_output.addSkeleton(t.toString());
                }
                else
                {
                    if (isInsideInternalStyleContext())
                    {
                        t.isInternalStyleContent = true;
                    }
                    addToText(t);
                }
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle a ASP/JSP tag; the script text is included in the argument
     * <code>t</code>.
     */
    public void handleXsp(HtmlObjects.Xsp s)
    {
        RegExMatchInterface mr = null;

        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }

        try
        {
            m_output.addSkeleton("<%");

            // non-null script?
            boolean b_script = s.text.length() > 0 && !Text.isBlank(s.text);

            // NOTE: This does not extract and set the script language
            // if within an excluded section.

            // if there's any script to deal with
            if (!m_extractor.exclude() && b_script)
            {
                m_iEmbeddedLine = s.iLine;
                m_iEmbeddedCol = s.iCol;

                // find directives element '@'
                try
                {
                    mr = null;
                    mr = RegEx.matchSubstring(s.text, "^(\\s*@)(.*|\\n*)",
                            false);
                }
                catch (RegExException e)
                {
                    logger.error(e.getMessage(), e);
                    m_bHasError = true;
                    m_strErrorMsg = e.getMessage();
                }

                if (mr != null)
                {
                    m_output.addSkeleton(mr.group(1));

                    s.text = mr.group(2);
                    m_output.addSkeleton(s.text);

                    // find language directive
                    try
                    {
                        mr = null;
                        mr = RegEx.matchSubstring(s.text,
                                "^(.*|\\n*)language\\s*=\\s*\"([^\"]*)\"",
                                false);
                    }
                    catch (RegExException e)
                    {
                        logger.error(e.getMessage(), e);
                        m_bHasError = true;
                        m_strErrorMsg = e.getMessage();
                    }

                    if (mr != null)
                    {
                        m_strSwitchTo = mr.group(2);
                        m_xspLanguage = getScriptLanguage(m_strSwitchTo);
                    }
                }
                else
                {
                    switch (m_xspLanguage)
                    {
                        case EC_JAVA:
                            try
                            {
                                mr = null;
                                if (s.text.trim().startsWith("="))
                                {
                                    mr = RegEx.matchSubstring(s.text,
                                            "^(\\s*=)(.*|\\n*)", false);
                                }
                            }
                            catch (RegExException e)
                            {
                                logger.error(e.getMessage(), e);
                                m_bHasError = true;
                                m_strErrorMsg = e.getMessage();
                            }

                            if (mr != null)
                            {
                                m_output.addSkeleton(mr.group(1));
                                s.text = mr.group(2);

                                switchToJava(s.text, false, "print");
                            }
                            else
                            {
                                try
                                {
                                    mr = null;
                                    mr = RegEx.matchSubstring(s.text,
                                            "^(\\s*!)((.|\\r|\\n)*)", false);
                                }
                                catch (RegExException e)
                                {
                                    logger.error(e.getMessage(), e);
                                    m_bHasError = true;
                                    m_strErrorMsg = e.getMessage();
                                }

                                if (mr != null)
                                {
                                    m_output.addSkeleton(mr.group(1));
                                    s.text = mr.group(2);

                                    switchToJava(s.text, false, "class");
                                }
                                else
                                {
                                    try
                                    {
                                        mr = null;
                                        mr = RegEx.matchSubstring(s.text,
                                                "^--", false);
                                    }
                                    catch (RegExException e)
                                    {
                                        logger.error(e.getMessage(), e);
                                        m_bHasError = true;
                                        m_strErrorMsg = e.getMessage();
                                    }

                                    if (mr != null)
                                    {
                                        m_output.addSkeleton(s.text);
                                    }
                                    else
                                    {
                                        switchToJava(s.text, false, "code");
                                    }
                                }
                            }

                            break;
                        case EC_VBSCRIPT:
                            switchToVB(s.text, false);
                            break;
                        case EC_JAVASCRIPT:
                            switchToJavaScript(s.text, false);
                            break;
                        case EC_UNKNOWNSCRIPT:
                        default:
                            m_output.addSkeleton(s.text);
                            break;
                    }
                }
            }
            else
            {
                m_output.addSkeleton(s.text);
            }

            m_output.addSkeleton("%>");
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;script&gt;</code> tag; the script text is included
     * in the argument <code>s</code>.
     */
    public void handleScript(HtmlObjects.Script s)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }

        try
        {
            addTagToSkeleton(s, true);

            if (!s.isClosed)
            {
                // non-null script?
                boolean b_script = s.text.length() > 0 && !Text.isBlank(s.text);
                boolean isInswitchMap = m_rules.isSwitchTag(s.tag);

                // if we're extracting, and if we're extracting the
                // script tag, and if there's any script to deal with,
                // and the dynamic rules allow extraction, then switch
                // to javascript
                if (!m_extractor.exclude() && m_rules.doExtractScripts()
                        && m_rules.isSwitchTag(s.tag) && b_script
                        && isInswitchMap)
                {
                    m_iEmbeddedLine = s.iLine;
                    m_iEmbeddedCol = s.iCol;

                    int e_language = getScriptLanguage(s);

                    switch (e_language)
                    {
                        case EC_JAVASCRIPT:
                            switchToJavaScript(s.text, false);
                            break;
                        case EC_VBSCRIPT:
                        case EC_PERLSCRIPT:
                        case EC_PYTHONSCRIPT:
                            // fall through
                        case EC_UNKNOWNSCRIPT:
                        default:
                            m_output.addSkeleton(s.text);
                            break;
                    }
                }
                else
                {
                    m_output.addSkeleton(s.text);
                }

                m_output.addSkeleton("</" + s.tag + ">");
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;java&gt;</code> tag; the java text is included in
     * the argument <code>s</code>.
     */
    public void handleJava(HtmlObjects.Java s)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }

        try
        {
            addTagToSkeleton(s, true);

            if (!s.isClosed)
            {
                boolean b_script = false;
                String type = null;

                // ATG Dynamo's JHTML tags:
                // ========================

                // <java></java> Java tag delimiters which define Java
                // code for the service method.

                // <java type=code></java> Java tag delimiters; same
                // as <java></java>.

                // <java type=import></java> Encloses an argument
                // containing the name of the class to import.

                // <java type=extends></java> Encloses an argument
                // containing the name of a class to extend the
                // servlet from.

                // <java type=implements></java> Encloses a list of
                // interfaces the servlet implements.

                // <java type=print></java> Encloses a Java expression
                // to be sent to the output stream.

                // <java type=class></java> Encloses an argument for
                // adding member variables and defining methods of the
                // page class directly.

                // WebLogic's JHTML tags:
                // ======================
                // see (http://www.weblogic.com/docs51/classdocs/
                // API_jhtml.html#tags)

                // <java type=package></java> the class' package

                // <java type=method></java> Overrides the service()
                // method name; the superclass method can then be used
                // for pre- and post-processing of the user-specified
                // method.

                // <!--java>...</java--> Special syntax for WebLogic
                // (not supported, use <java>).

                if (!s.attributes.isDefined("type"))
                {
                    b_script = true;
                    type = "code";
                }
                else
                {
                    type = s.attributes.getValue("type");
                    if (type.equalsIgnoreCase("code")
                            || type.equalsIgnoreCase("print")
                            || type.equalsIgnoreCase("class"))
                    {
                        b_script = true;
                    }
                }

                // if there's any script to deal with
                if (!m_extractor.exclude() && m_rules.isSwitchTag(s.tag)
                        && b_script)
                {
                    m_iEmbeddedLine = s.iLine;
                    m_iEmbeddedCol = s.iCol;

                    switchToJava(s.text, false, type);
                }
                else
                {
                    m_output.addSkeleton(s.text);
                }

                m_output.addSkeleton("</" + s.tag + ">");
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag; the ColdFusion script text
     * is included in the argument <code>t</code>.
     */
    public void handleCFScript(HtmlObjects.CFScript s)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }

        try
        {
            addTagToSkeleton(s, true);

            if (!s.isClosed)
            {
                // non-null script?
                boolean b_script = s.text.length() > 0 && !Text.isBlank(s.text);

                // if there's any script to deal with
                if (!m_extractor.exclude() && m_rules.isSwitchTag(s.tag)
                        && b_script)
                {
                    m_iEmbeddedLine = s.iLine;
                    m_iEmbeddedCol = s.iCol;

                    switchToCFScript(s.text, false);
                }
                else
                {
                    m_output.addSkeleton(s.text);
                }

                m_output.addSkeleton("</" + s.tag + ">");
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag with SQL statements inside;
     * the SQL text is included in the argument <code>t</code>.
     */
    public void handleCFQuery(HtmlObjects.CFQuery s)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }

        try
        {
            addTagToSkeleton(s, true);

            if (!s.isClosed)
            {
                // non-null SQL?
                boolean b_sql = s.text.length() > 0 && !Text.isBlank(s.text);

                // if there's any SQL to deal with
                if (!m_extractor.exclude() && m_rules.isSwitchTag(s.tag)
                        && b_sql)
                {
                    // Handle SQL later
                    // switchToSQL(s.m_strText, false);
                    m_output.addSkeleton(s.text);
                }
                else
                {
                    m_output.addSkeleton(s.text);
                }

                m_output.addSkeleton("</" + s.tag + ">");
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;style&gt;</code> tag; the style text is included in
     * the argument <code>t</code>.
     */
    public void handleStyle(HtmlObjects.Style s)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }

        try
        {
            addTagToSkeleton(s, true);

            if (!s.isClosed)
            {
                // non-null style?
                boolean b_style = s.text.length() > 0 && !Text.isBlank(s.text);
                boolean isInswitchMap = m_rules.isSwitchTag(s.tag);

                // if we're extracting, and if we're extracting the
                // style tag, and if this stylesheet is a CSS
                // stylesheet, and the dynamic rules allow extraction,
                // and if there's any style to deal with, then switch
                // to CSS
                if (!m_extractor.exclude() && m_rules.doExtractStylesheets()
                        && m_rules.isSwitchTag(s.tag) && isCSSStyleSheet(s)
                        && b_style && isInswitchMap)
                {
                    switchToStylesheet(s.text, false);
                }
                else
                {
                    m_output.addSkeleton(s.text);
                }

                m_output.addSkeleton("</" + s.tag + ">");
            }
        }
        catch (ExtractorException e)
        {
            logger.error(e.getMessage(), e);
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    public void handleSpecialChar(HtmlObjects.Text t)
    {
        // String text = t.text;
        // if (">".equals(text))
        // t.text = "&gt;";

        handleText(t);
    }

    /**
     * Handle text (#PCDATA).
     */
    public void handleText(HtmlObjects.Text t)
    {
        // Wed Apr 06 19:05:12 2005: don't output material inside
        // discardable decls like footnotes and endnotes.
        if (isInsideDiscardableDecl())
        {
            return;
        }

        // text in <GS-INTERNAL-TEXT>#message here#</GS-INTERNAL-TEXT>
        if (m_isGSInternalTag)
        {
            String text = t.toString();
            text = m_htmlDecoder.decodeStringBasic(text);
            m_gsInternalTagBuffer.append(text);
            return;
        }

        if (isInsideUntranslatableContext() || isInsideSkippableContext()
                || m_bSkipExcelCell || this.m_excelHiddenRow
                || this.m_excelHiddenSheet || isInsideInternalStyleContext())
        {
            // Keep markup content in text not mixed with style tags.
            // Here, markup content means anything between "<" (inclusive) and
            // ">" (inclusive).
            // If we are in DONOTTRANSLATE, either "para" or "char" context,
            // don't decode "&lt;",
            // "&gt;" and "&nbsp;", they will mix style tags.
            // Note: if any other encoded character in this context (marked
            // DONOTTRANSLATE _para or _char)
            // cause file format problems in future, just put it in the String[]
            // parameter;
            t.text = m_htmlDecoder.decodeString(t.text, new String[]
            { "&lt;", "&gt;", "&nbsp;", "&amp;" });
            // Thanks to word converter's bug, we should convert blankspace, if
            // t.text contains any,
            // to &nbsp;, or we will get format error when export job if the
            // original word file contains
            // consecutive blankspace.
            t.text = replaceSpaceWithNbsp(t.text);
        }
        else
        {
            t.text = m_htmlDecoder.decodeString(t.text, null);
        }

        // Tue Aug 30 21:29:53 2005: don't output material inside
        // skippable contexts like Word paragraphs whose style is
        // unextractable.
        if (isInsideSkippableContext())
        {
            m_output.addSkeleton(t.toString());
            return;
        }

        if (!m_bExtracting)
        {
            skipText(t.toString()); // pass as is to other extractors
        }
        else
        {
            String mainFormat = m_extractor.getMainFormat();
            // Code to skip text between tags that start in skeleton,
            // like Excel's <TD x:xxx>...</TD>.
            if (m_bSkipExcelCell || this.m_excelHiddenRow
                    || this.m_excelHiddenSheet)
            {
                m_output.addSkeleton(t.toString());
            }
            // internal tag
            else if (m_internalStack.size() > 0)
            {
                addToText(t);
            }
            // internal style content
            else if (isInsideInternalStyleContext())
            {
                t.isInternalStyleContent = true;
                addToText(t);
            }
            // office html, handle internal text it self in flushText
            else if (OfficeContentPostFilterHelper.isOfficeFormat(mainFormat))
            {
                addToText(t);
            }
            else
            {
                List<String> handled = InternalTextHelper
                        .handleStringWithListReturn(t.toString(),
                                getInternalTexts(), IFormatNames.FORMAT_HTML);

                if (handled == null || handled.size() == 0)
                {
                    addToText(t);
                }
                else
                {
                    for (String subtext : handled)
                    {
                        if (subtext
                                .startsWith(InternalTextHelper.GS_INTERNALT_TAG_START))
                        {
                            addToText(new HtmlObjects.InternalText(subtext));
                        }
                        else
                        {
                            addToText(new HtmlObjects.Text(subtext));
                        }
                    }
                }
            }
        }
    }

    private List<InternalText> getInternalTexts()
    {
        // GBS-2894 : do segmentation before internal text
        if (m_extractor.isDoSegBeforeInlText())
        {
            return null;
        }

        if (m_extractor.getMainBaseFilter() != null)
        {
            try
            {
                return BaseFilterManager.getInternalTexts(m_extractor
                        .getMainBaseFilter());
            }
            catch (Exception e)
            {
                logger.error(
                        "Error when load InternalTexts from base filter : "
                                + m_extractor.getMainBaseFilter()
                                        .getFilterName(), e);
                return new ArrayList<InternalText>();
            }
        }
        else
        {
            return m_rules.getInternalTextList();
        }
    }

    private String replaceSpaceWithNbsp(String p_text)
    {
        StringBuffer sb = null;
        if (p_text.indexOf('\u00a0') >= 0)
        {
            sb = new StringBuffer(p_text);
            for (int i = 0; i < sb.length(); i++)
            {
                if (sb.charAt(i) == '\u00a0')
                {
                    sb.replace(i, i + 1, "&nbsp;");
                }
            }
        }
        return sb == null ? p_text : sb.toString();
    }

    /**
     * <p>
     * Adds an attribute to the skeleton. Translatable and localizable tags are
     * output appropriately, and eventhandlers (onXXX) are handed off to the
     * Javascript parser.
     * 
     * <p>
     * Called from addTagToSkeleton().
     * 
     * @param t
     *            : the tag the attribute belongs to (from HtmlObjects)
     * @param attrib
     *            : the attribute to ouput to the skeleton
     * @param decode
     *            : indicates whether entities in the attribute need to be
     *            decoded when outputting as trans/loc.
     */
    protected void addAttributeToSkeleton(HtmlObjects.Tag t,
            HtmlObjects.Attribute attrib, boolean decode)
            throws ExtractorException
    {
        String strValue = Text.removeQuotes(attrib.value);

        // Value-less Attribute - quick exit
        if (strValue == null || strValue.length() == 0
                || Text.isBlank(strValue))
        {
            m_output.addSkeleton(" " + attrib.toString());
            return;
        }

        // When extracting ASP/JSP, check for att="<%=...%>".
        if (isXSPExtractor() && strValue.indexOf("<%") >= 0
                && strValue.indexOf("%>") >= 0)
        {
            m_output.addSkeleton(" " + attrib.toString());
            return;
        }

        // When extracting CFM, check for att="<cf ... >".
        if (isCFExtractor()
                && (strValue.indexOf("<cf") >= 0 || strValue.indexOf("<CF") >= 0))
        {
            m_output.addSkeleton(" " + attrib.toString());
            return;
        }

        // Proceed with normal handling of attributes: decode if
        // requested, then examine the type and value.

        if (decode)
        {
            decodeEntities(attrib);
            strValue = Text.removeQuotes(attrib.value);
        }

        // A normal, inconspicuous attribute, handle the HTML way.

        String quote = Text.getQuoteCharacter(attrib.value);

        // comment this for do not extract href content, for gbs-1522
        // // Special cases first: <tag href="javascript:...">
        // if ((attrib.name.equalsIgnoreCase(HREF) || attrib.name
        // .equalsIgnoreCase(ACTION))
        // && strValue.toLowerCase().startsWith("javascript:"))
        // {
        // m_output.addSkeleton(" " + attrib.name + "=" + quote);
        // m_output.addSkeleton("javascript:");
        //
        // // this is a URL, needs to be decoded
        // String temp = decodeUrl(strValue.substring(11));
        // switchToJavaScript(temp, false);
        //
        // m_output.addSkeleton(quote);
        // }

        // Style Attribute: <tag style="...">
        if (!isIgnoreInvalidHtmlTags() && attrib.name.equalsIgnoreCase(STYLE))
        {
            // to fix problem:
            // "style='font-family:&quot;Arial&quot;,&quot;sans-serif&quot;'"
            decodeEntities(attrib);
            strValue = Text.removeQuotes(attrib.value);
            m_output.addSkeleton(" " + attrib.name + "=" + quote);
            switchToStyles(strValue, false);
            m_output.addSkeleton(quote);
        }

        // Event Handler Attribute
        else if (m_rules.isEventHandlerAttribute(attrib.name)
                && m_rules.isTranslatableAttribute(t.tag, attrib.name))
        {
            m_output.addSkeleton(" " + attrib.name + "=" + quote);
            switchToJavaScript(strValue, false);
            m_output.addSkeleton(quote);
            // m_output.addTranslatable(" " + attrib.name + "=" + quote);
            // m_output.addTranslatableTmx(switchToJavaScript(strValue,
            // true));
            // m_output.addTranslatable(quote);

        }

        // Localizable Attributes
        // else if (m_rules.isLocalizableAttribute(t.tag, attrib.name))
        // {
        // String strAttributeType = m_rules.getLocalizableAttribType(t.tag,
        // attrib.name);
        //
        // // CvdL: can't URLdecode - information loss - can't merge
        // // if (strAttributeType.startsWith("url"))
        // // {
        // // strValue = decodeUrl(strValue);
        // // }
        //
        // m_output.addSkeleton(" " + attrib.name + "=" + quote);
        // m_output.addLocalizable(strValue);
        //
        // try
        // {
        // m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_HTML,
        // strAttributeType);
        // }
        // catch (DocumentElementException e)
        // {
        // throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
        // }
        //
        // m_output.addSkeleton(quote);
        // }

        // Translatable Attribute. Do not extract url in the attribute value.
        else if ((t.isFromOfficeContent ? m_rules
                .isContentTranslatableAttribute(attrib.name) : m_rules
                .isTranslatableAttribute(t.tag, attrib.name))
                && !m_extractor.exclude())
        {
            m_output.addSkeleton(" " + attrib.name + "=" + quote);
            m_output.addTranslatable(strValue);

            try
            {
                m_output.setTranslatableAttrs(ExtractorRegistry.FORMAT_HTML,
                        attrib.name.toLowerCase());
            }
            catch (DocumentElementException e)
            {
                throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
            }

            m_output.addSkeleton(quote);
        }

        // Simple non-translatable, non-localizable boring attribute
        else
        {
            // disregards attribute decoding done above
            m_output.addSkeleton(" " + attrib.toString());
        }
    }

    protected void addAttributeToSkeleton(HtmlObjects.Tag t,
            HtmlObjects.SimpleTag embeddedTag, boolean decode)
            throws ExtractorException
    {
        // embedded tags are in attribute position, so output a space
        m_output.addSkeleton(" ");
        m_output.addSkeleton(embeddedTag.toString());
    }

    /**
     * <p>
     * Adds a CFM attribute to the skeleton. Translatable and localizable tags
     * are output appropriately.
     * 
     * <p>
     * Called from addTagToSkeleton(CFTag).
     * 
     * @param t
     *            : the CF tag the attribute belongs to (from HtmlObjects)
     * @param attrib
     *            : the attribute to ouput to the skeleton
     * @param decode
     *            : indicates whether entities in the attribute need to be
     *            decoded when outputting as trans/loc.
     */
    protected void addAttributeToSkeleton(HtmlObjects.CFTag t,
            HtmlObjects.Attribute attrib, boolean decode)
            throws ExtractorException
    {
        if (decode)
        {
            decodeEntities(attrib);
        }

        String strValue = Text.removeQuotes(attrib.value);

        // Value-less Attribute - quick exit
        if (strValue == null || strValue.length() == 0
                || Text.isBlank(strValue))
        {
            m_output.addSkeleton(" " + attrib.toString());
            return;
        }

        // No need to check for <%..%>, this is ColdFusion

        String quote = Text.getQuoteCharacter(attrib.value);

        // Localizable Attributes
        if (m_rules.isLocalizableAttribute(t.tag, attrib.name))
        {
            String strAttributeType = m_rules.getLocalizableAttribType(t.tag,
                    attrib.name);

            m_output.addSkeleton(" " + attrib.name + "=" + quote);
            m_output.addLocalizable(strValue);

            try
            {
                m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_HTML,
                        strAttributeType);
            }
            catch (DocumentElementException e)
            {
                throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
            }

            m_output.addSkeleton(quote);
        }

        // Translatable Attribute
        else if (m_rules.isTranslatableAttribute(t.tag, attrib.name)
                && !m_extractor.exclude())
        {
            m_output.addSkeleton(" " + attrib.name + "=" + quote);
            m_output.addTranslatable(strValue);

            try
            {
                m_output.setTranslatableAttrs(ExtractorRegistry.FORMAT_HTML,
                        attrib.name.toLowerCase());
            }
            catch (DocumentElementException e)
            {
                throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
            }

            m_output.addSkeleton(quote);
        }

        // Simple non-translatable, non-localizable boring attribute
        else
        {
            m_output.addSkeleton(" " + attrib.toString());
        }
    }

    /**
     * <p>
     * Adds a tag to the skeleton. If necessary extracts translatable attributes
     * and switches to the JavaScript parser for event handlers and JS entities.
     * </p>
     * 
     * <p>
     * This function is called from both handleStartTag() and
     * flushTextToSkeleton(). The former has not decoded entities in attribute
     * values whereas the latter has (the tag went through addToText() into the
     * list of extraction candidates).
     * </p>
     * 
     * <p>
     * The <code>decode</code> parameter tells us if attributes have to be
     * decoded when output as translatable/localizable or not.
     * </p>
     */
    protected void addTagToSkeleton(HtmlObjects.Tag t, boolean decode)
            throws ExtractorException
    {
        if (!t.isFromOfficeContent)
        {
            // gsa tags are a little special and deserve a special method...
            if (t.tag.equalsIgnoreCase(DiplomatNames.Element.GSA))
            {
                processGsa(t); // nothing to decode
                return;
            }

            // meta tag are a little special and deserve a special method...
            if (t.tag.equalsIgnoreCase("meta"))
            {
                // processMeta(t); // nothing to decode
                // For GBS-740, meta tags exposed as non translatable
                m_output.addSkeleton(t.original);
                return;
            }

            // possible generator tag (<param value="..."> inside <object>)
            if (t.tag.equalsIgnoreCase("jsp:param"))
            {
                processJspParam(t);
                return;
            }

            // possible generator tag (<param value="..."> inside <object>)
            // if ((t.tag.equalsIgnoreCase("param")) &&
            // t.isDefinedAttribute(VALUE))
            // {
            // processGenerator(t); // this is so esoteric, just forget
            // // about decoding and wait for a REAL bug.
            // return;
            // }

            // possible generator tag (<embed src="..."> tag)
            if ((t.tag.equalsIgnoreCase("embed")) && t.isDefinedAttribute(SRC))
            {
                processGenerator(t);
                return;
            }

            // Wed Mar 12 20:55:47 2003 CvdL: spacer gifs, reduce number
            // of dreadful HP localizables here.
            String value;
            if ((t.tag.equalsIgnoreCase("img"))
                    && t.isDefinedAttribute(SRC)
                    && (value = t.attributes.getAttribute(SRC).getValue()) != null
                    && m_rules.isSpacerGif(value))
            {
                processSpacerGif(t);
                return;
            }

            // all other tags
            if (t.isIgnore())
            {
                m_output.addSkeleton(t.original);
                return;
            }
        }

        if (t.isFromOfficeContent)
        {
            // add for export merge
            m_output.addSkeleton(OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_START);
        }
        if (t.isMerged)
        {
            m_output.addSkeleton(t.original);
            // add for export merge
            m_output.addSkeleton(OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_END);
            return;
        }
        m_output.addSkeleton("<" + t.tag);
        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            Object o = it.next();

            if (o instanceof HtmlObjects.Attribute)
            {
                addAttributeToSkeleton(t, (HtmlObjects.Attribute) o, decode);
            }
            else if (o instanceof HtmlObjects.SimpleTag)
            {
                addAttributeToSkeleton(t, (HtmlObjects.SimpleTag) o, false /* decode */);
            }
            else if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag e = (HtmlObjects.EndTag) o;

                m_output.addSkeleton(e.toString());
            }
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addSkeleton(" ");
            index--;
        }
        m_output.addSkeleton(end);

        if (t.isFromOfficeContent)
        {
            // add for export merge
            m_output.addSkeleton(OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_END);
        }
    }

    /**
     * <p>
     * Adds a ColdFusion tag to the skeleton. If necessary, extracts
     * translatable attributes..
     * </p>
     * 
     * <p>
     * This function is called from both handleStartTag() and
     * flushTextToSkeleton(). The former has not decoded entities in attribute
     * values whereas the latter has. The <code>decode</code> parameter tells us
     * if we should decode attributes or not.
     * </p>
     */
    protected void addTagToSkeleton(HtmlObjects.CFTag t, boolean decode)
            throws ExtractorException
    {
        // all other tags
        m_output.addSkeleton("<" + t.tag);

        if (m_rules.isCFExpressionTag(t.tag))
        {
            String expr = t.attributes.toString();
            switchToCFScript(expr, false);
        }
        else
        {
            for (Iterator it = t.attributes.iterator(); it.hasNext();)
            {
                HtmlObjects.Attribute attr = (HtmlObjects.Attribute) it.next();
                addAttributeToSkeleton(t, attr, decode);
            }
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addSkeleton(" ");
            index--;
        }
        m_output.addSkeleton(end);
    }

    /**
     * <p>
     * Adds a comment tag to the skeleton. When the comment is an SSI
     * instruction, localizable attributes are extracted.
     * </p>
     * 
     * <p>
     * Note this function duplicates code in addCommentToTranslatable().
     * </p>
     */
    protected void addCommentToSkeleton(HtmlObjects.Comment t)
            throws ExtractorException
    {
        if (isSSIComment(t) && !m_extractor.exclude()
                && m_rules.doExtractSSIInclude())
        {
            String str_comment = t.getComment();

            RegEx regex = new RegEx();
            RegExMatchInterface mr = null;

            try
            {
                mr = regex.matchSubstring(str_comment,
                        "#\\s*include\\s+(file|virtual)=\"([^\"]*)\"", false);
            }
            catch (RegExException e) // Should Not Happen
            {
                // System.err.println(e.toString());
                throw new ExtractorException(REGEX_ERROR, e);
            }

            if (mr != null) // found an include filename
            {
                int i_fileStart = mr.beginOffset(2);
                int i_fileEnd = mr.endOffset(2);

                m_output.addSkeleton("<!--");

                String first = str_comment.substring(0, i_fileStart);
                m_output.addSkeleton(first);

                String second = str_comment.substring(i_fileStart, i_fileEnd);
                second = Text.removeCRNL(second);
                m_output.addLocalizable(second);

                try
                {
                    m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_HTML,
                            "ssi-include");
                }
                catch (DocumentElementException e) // SNH
                {
                    throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
                }

                String third = str_comment.substring(i_fileEnd);
                m_output.addSkeleton(third);

                m_output.addSkeleton("-->");
            }
            else
            {
                // not an include instruction, send to skeleton
                m_output.addSkeleton(t.toString());
            }
        }
        else
        {
            // normal comment, handle normally
            m_output.addSkeleton(t.toString());
        }
    }

    /**
     * <p>
     * Outputs a comment as placeholder to a translatable section. If the
     * comment contains SSI instructions, appropriate sub elements are
     * generated, e.g. for #include file="...".
     * </p>
     * 
     * <p>
     * Note this function duplicates code in addCommentToSkeleton().
     * </p>
     */
    protected void addCommentToTranslatable(HtmlObjects.Comment t)
            throws ExtractorException
    {
        TmxTagGenerator tg = m_html2TmxMap
                .getPlaceholderTmxTag("comment", true);

        if (isSSIComment(t) && !m_extractor.exclude()
                && m_rules.doExtractSSIInclude())
        {
            String str_comment = t.getComment();

            RegEx regex = new RegEx();
            RegExMatchInterface mr = null;

            try
            {
                mr = regex.matchSubstring(str_comment,
                        "#\\s*include\\s+(file|virtual)=\"([^\"]*)\"", false);
            }
            catch (RegExException e) // Should Not Happen
            {
                // System.err.println(e.toString());
                throw new ExtractorException(REGEX_ERROR, e);
            }

            if (mr != null) // found an include filename
            {
                int i_fileStart = mr.beginOffset(2);
                int i_fileEnd = mr.endOffset(2);

                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable("<!--");

                String first = str_comment.substring(0, i_fileStart);
                m_output.addTranslatable(first);

                String second = str_comment.substring(i_fileStart, i_fileEnd);
                m_output.addTranslatableTmx("<sub type=\"ssi-include\" "
                        + "locType=\"localizable\">");
                second = Text.removeCRNL(second);
                m_output.addTranslatable(second);
                m_output.addTranslatableTmx("</sub>");

                String third = str_comment.substring(i_fileEnd);
                m_output.addTranslatable(third);

                m_output.addTranslatable("-->");

                m_output.addTranslatableTmx(tg.getEnd());
            }
            else
            {
                // not an include instruction, send to translatable
                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable(t.toString());
                m_output.addTranslatableTmx(tg.getEnd());
            }
        }
        else
        {
            // normal comment, handle normally
            m_output.addTranslatableTmx(tg.getStart());
            m_output.addTranslatable(t.toString());
            m_output.addTranslatableTmx(tg.getEnd());
        }
    }

    /**
     * <p>
     * Adds the current tag or text to the list of extraction candidates.
     * </p>
     */
    protected void addToText(Object p_elt)
    {
        if (p_elt instanceof HtmlObjects.Text)
        {
            HtmlObjects.Text tag = (HtmlObjects.Text) p_elt;
            // Whitespace handling is getting a nightmare: when in
            // documents converted from Word (and maybe other Office
            // formats), treat nbsp as whitespace. Otherwise, let the
            // system-wide configuration in Diplomat.properties guide
            // the working of Text.isBlank().
            if (!m_bContainsText)
            {
                if (isWordExtractor())
                {
                    m_bContainsText = !Text.isBlankOrNbsp(tag.text);
                }
                else
                {
                    m_bContainsText = !Text.isBlank(tag.text);
                }
            }
        }
        else if (p_elt instanceof HtmlObjects.InternalText)
        {
            if (!m_bContainsText)
            {
                m_bContainsText = true;
            }
        }
        // decode embedded entities inside HTML attributes
        else if (false && p_elt instanceof HtmlObjects.Tag)
        {
            HtmlObjects.Tag tag = (HtmlObjects.Tag) p_elt;

            for (Iterator it = tag.attributes.iterator(); it.hasNext();)
            {
                Object o = it.next();

                if (o instanceof HtmlObjects.Attribute)
                {
                    HtmlObjects.Attribute attr = (HtmlObjects.Attribute) o;
                    decodeEntities(attr);
                }
            }
        }
        // and also inside CF attributes?? I doubt this is right.
        else if (false && p_elt instanceof HtmlObjects.CFTag)
        {
            HtmlObjects.CFTag tag = (HtmlObjects.CFTag) p_elt;

            // CFExpression tags have the expression in the name field
            // of the first attribute. Don't decode that.
            if (!m_rules.isCFExpressionTag(tag.tag))
            {
                for (Iterator it = tag.attributes.iterator(); it.hasNext();)
                {
                    HtmlObjects.Attribute attr = (HtmlObjects.Attribute) it
                            .next();
                    decodeEntities(attr);
                }
            }
        }

        m_extractionCandidates.add((HtmlObjects.HtmlElement) p_elt);
    }

    /**
     * <p>
     * Walk through a segment and mark each pairable tag without buddy as
     * isolated. The tags' boolean members m_bPaired and m_bIsolated are false
     * by default.
     */
    protected void assignPairingStatus(List<HtmlObjects.HtmlElement> p_segment)
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>(
                p_segment);
        Object o1, o2;
        int i_start, i_end, i_max;
        int i_level, i_partner = 1;
        HtmlObjects.Tag t_start, t_tag;
        HtmlObjects.EndTag t_end;
        HtmlObjects.CFTag t_CFstart, t_CFtag;

        i_start = 0;
        i_max = tags.size();
        outer: while (i_start < i_max)
        {
            o1 = tags.get(i_start);

            if (o1 instanceof HtmlObjects.Tag)
            {
                t_start = (HtmlObjects.Tag) o1;

                // don't consider tags that are already closed (<BR/>)
                if (t_start.isClosed)
                {
                    tags.remove(i_start);
                    --i_max;
                    continue outer;
                }

                // handle recursive tags
                i_level = 0;

                // see if the current opening tag has a closing tag
                for (i_end = i_start + 1; i_end < i_max; ++i_end)
                {
                    o2 = tags.get(i_end);

                    if (o2 instanceof HtmlObjects.Tag)
                    {
                        t_tag = (HtmlObjects.Tag) o2;

                        if (t_start.tag.equalsIgnoreCase(t_tag.tag))
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof HtmlObjects.EndTag)
                    {
                        t_end = (HtmlObjects.EndTag) o2;

                        if (t_start.tag.equalsIgnoreCase(t_end.tag))
                        {
                            if (i_level > 0)
                            {
                                --i_level;
                                continue;
                            }

                            // found a matching buddy in this segment
                            t_start.isPaired = t_end.isPaired = true;
                            t_start.partnerId = t_end.partnerId = i_partner;
                            i_partner++;
                            tags.remove(i_end);
                            tags.remove(i_start);
                            i_max -= 2;
                            continue outer;
                        }
                    }
                }

                // tag with no buddy - if it requires one, mark as isolated
                if (t_start.isFromOfficeContent ? m_rules
                        .isContentPairedTag(t_start.tag) : m_rules
                        .isPairedTag(t_start.tag))
                {
                    t_start.isIsolated = true;
                }

                // done with this tag, don't consider again
                tags.remove(i_start);
                --i_max;
                continue outer;
            }
            else if (o1 instanceof HtmlObjects.CFTag)
            {
                t_CFstart = (HtmlObjects.CFTag) o1;

                // don't consider tags that are already closed (<BR/>)
                if (t_CFstart.isClosed)
                {
                    tags.remove(i_start);
                    --i_max;
                    continue outer;
                }

                // handle recursive tags
                i_level = 0;

                // see if the current opening tag has a closing tag
                for (i_end = i_start + 1; i_end < i_max; ++i_end)
                {
                    o2 = tags.get(i_end);

                    if (o2 instanceof HtmlObjects.CFTag)
                    {
                        t_CFtag = (HtmlObjects.CFTag) o2;

                        if (t_CFstart.tag.equalsIgnoreCase(t_CFtag.tag))
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof HtmlObjects.EndTag)
                    {
                        t_end = (HtmlObjects.EndTag) o2;

                        if (t_CFstart.tag.equalsIgnoreCase(t_end.tag))
                        {
                            if (i_level > 0)
                            {
                                --i_level;
                                continue;
                            }

                            // found a matching buddy in this segment
                            t_CFstart.isPaired = t_end.isPaired = true;
                            t_CFstart.partnerId = t_end.partnerId = i_partner;
                            i_partner++;
                            tags.remove(i_end);
                            tags.remove(i_start);
                            i_max -= 2;
                            continue outer;
                        }
                    }
                }

                // tag with no buddy - if it requires one, mark as isolated
                if (t_CFstart.isFromOfficeContent ? m_rules
                        .isContentPairedTag(t_CFstart.tag) : m_rules
                        .isPairedTag(t_CFstart.tag))
                {
                    t_CFstart.isIsolated = true;
                }

                // done with this tag, don't consider again
                tags.remove(i_start);
                --i_max;
                continue outer;
            }
            else if (!(o1 instanceof HtmlObjects.EndTag))
            {
                // don't consider non-tag tags in the list
                tags.remove(i_start);
                --i_max;
                continue outer;
            }

            ++i_start;
        }

        // only isolated begin/end tags are left in the list
        for (i_start = 0; i_start < i_max; ++i_start)
        {
            HtmlObjects.HtmlElement t = (HtmlObjects.HtmlElement) tags
                    .get(i_start);

            t.isIsolated = true;
        }
    }

    /**
     * While the first element is an isolated end tag with no opening tag in the
     * file, flush it to the skeleton. Also flush leading comments and
     * whitespace.
     * 
     * For MS Word: flush all isolated tags.
     * 
     * This method is used with different semantics which causes problems: once
     * to remove the leading isolated tags, and then to remove all tags that are
     * in the list passed in.
     */
    protected void flushLeadingIsolatedEndTags(
            List<HtmlObjects.HtmlElement> p_segments) throws ExtractorException
    {
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(0);

            if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag t = (HtmlObjects.EndTag) o;

                if (t.isIsolated
                        && (isMsOfficeExtractor() || m_html2TmxMap
                                .peekExternalId(t.tag) == -1))
                {
                    m_output.addSkeleton(t.toString());
                    p_segments.remove(0);
                    continue;
                }
            }
            // Fri Aug 22 00:39:14 2003 CvdL flush all isolated tags
            // for MsOffice.
            else if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t = (HtmlObjects.Tag) o;

                if (isMsOfficeExtractor() && t.isIsolated)
                {
                    addTagToSkeleton(t, false);
                    p_segments.remove(0);
                    continue;
                }
            }
            else if (o instanceof HtmlObjects.Text
                    || o instanceof HtmlObjects.Newline)
            {
                String text = o.toString();

                if (Text.isBlank(text))
                {
                    m_output.addSkeleton(text);
                    p_segments.remove(0);
                    continue;
                }
            }
            else if (o instanceof HtmlObjects.Comment)
            {
                String text = o.toString();

                m_output.addSkeleton(text);
                p_segments.remove(0);
                continue;
            }
            else if (o instanceof HtmlObjects.Declaration)
            {
                String text = o.toString();

                m_output.addSkeleton(text);
                p_segments.remove(0);

                // Fri Jun 20 16:03:28 2003 CvdL Recognize MS Office
                // indented lists and move the whitespaces out of the
                // segment into the skeleton. (Hopefully these <![]>
                // instructions are not recursive.)
                if (text.equals("<![if !supportLists]>"))
                {
                    do
                    {
                        o = p_segments.get(0);

                        text = o.toString();

                        m_output.addSkeleton(text);
                        p_segments.remove(0);
                    } while (!text.equals("<![endif]>"));
                }

                continue;
            }

            break;
        }
    }

    protected void flushLeadingIsolatedFontTags(
            List<HtmlObjects.HtmlElement> p_segments) throws ExtractorException
    {
        // While the first element is an isolated starting font tag,
        // flush it to the skeleton.
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(0);

            if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t = (HtmlObjects.Tag) o;
                if (t.isIsolated && t.tag.equalsIgnoreCase("FONT"))
                {
                    addTagToSkeleton(t, false);
                    p_segments.remove(0);
                    continue;
                }
            }
            else if (o instanceof HtmlObjects.Text
                    || o instanceof HtmlObjects.Newline)
            {
                if (Text.isBlank(o.toString()))
                {
                    m_output.addSkeleton(o.toString());
                    p_segments.remove(0);
                    continue;
                }
            }

            break;
        }
    }

    /**
     * While the last element is an isolated end tag with no opening tag in the
     * file, collect it into a list that must be flushed to the skeleton _after_
     * the segment has been processed.
     * 
     * For MS Word: flush all isolated tags.
     */
    protected void removeTrailingIsolatedEndTags(
            List<HtmlObjects.HtmlElement> p_segments,
            List<HtmlObjects.HtmlElement> p_after)
    {
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(p_segments.size() - 1);

            if (o instanceof HtmlObjects.Text
                    || o instanceof HtmlObjects.Newline)
            {
                if (Text.isBlank(o.toString()))
                {
                    p_after.add(0, p_segments.remove(p_segments.size() - 1));
                    continue;
                }
            }
            else if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag t = (HtmlObjects.EndTag) o;

                if (t.isIsolated
                        && (isMsOfficeExtractor() || m_html2TmxMap
                                .peekExternalId(t.tag) == -1))
                {
                    p_after.add(0, p_segments.remove(p_segments.size() - 1));
                    continue;
                }
            }
            // Fri Aug 22 00:39:14 2003 CvdL flush all isolated tags
            // for MsOffice (see also the isMsOfficeExtractor() clause
            // above.
            else if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t = (HtmlObjects.Tag) o;

                if (isMsOfficeExtractor() && t.isIsolated)
                {
                    p_after.add(0, p_segments.remove(p_segments.size() - 1));
                    continue;
                }
            }

            break;
        }
    }

    /**
     * Flushes the tags which are not from embedded office contents into
     * skeleton.
     */
    protected void flushLeftOverTags(List<HtmlObjects.HtmlElement> p_segments)
    {
        for (Iterator<HtmlObjects.HtmlElement> it = p_segments.iterator(); it
                .hasNext();)
        {
            HtmlObjects.HtmlElement tag = it.next();
            if (tag.isFromOfficeContent)
            {
                continue;
            }
            m_output.addSkeleton(tag.toString());
            it.remove();
        }
    }

    /**
     * <P>
     * This method is called each time we reach a segment breaking element.
     * Flushes out the list of potential extraction candidates, updating as
     * necessary the skeleton and segment list.
     * </p>
     */
    protected void flushText() throws ExtractorException
    {
        if (m_isUntranslatableEndTag && m_extractionCandidates.size() > 0)
        {
            Object o = m_extractionCandidates.get(0);

            if (o instanceof HtmlObjects.Text)
            {
                m_isUntranslatableEndTag = false;
            }
        }

        if (m_bContainsText && !m_extractor.exclude()
                && !m_isUntranslatableEndTag)
        {
            List<HtmlObjects.HtmlElement> tagsBefore = new ArrayList<HtmlObjects.HtmlElement>();
            List<HtmlObjects.HtmlElement> tagsAfter = new ArrayList<HtmlObjects.HtmlElement>();

            // For each segment in the list: assign tag status
            // (paired, isolated).
            assignPairingStatus(m_extractionCandidates);

            // Fri Aug 05 23:18:27 2005 CvdL: gsdef 13568: when
            // editing source pages, i.e. when segments that were
            // already extracted get re-extracted, simplifying the
            // segments again causes tags to be lost. So, allow the
            // page update code to keep all tags.
            if (simplifySegments())
            {
                // Get rid of leading isolated end tags that may have
                // their counterpart in other parts of the skeleton.
                flushLeadingIsolatedEndTags(m_extractionCandidates);

                // FONT HACK: flush leading isolated font tags to the
                // skeleton, since they most likely have their counterpart
                // in the skeleton.
                flushLeadingIsolatedFontTags(m_extractionCandidates);

                // Fetch the trailing isolated stuff, too, and save it
                // for later.
                removeTrailingIsolatedEndTags(m_extractionCandidates, tagsAfter);
            }

            if (isMsOfficeExtractor() && simplifySegments())
            {
                MsOfficeSimplifier s = new MsOfficeSimplifier(m_rules,
                        tagsBefore, m_extractionCandidates, tagsAfter);

                // For PPT issue
                if (isPowerPointExtractor())
                {
                    String cssBullets = this.m_input.getBulletsMsOffice();
                    if ((cssBullets != null) && (cssBullets.length() > 0))
                    {
                        String[] bulletStyles = cssBullets.split("/");
                        Set<String> bulletStyleSet = new HashSet<String>();

                        for (int i = 0; i < bulletStyles.length; i++)
                        {
                            bulletStyleSet.add(bulletStyles[i]);
                        }

                        s.setBulletStyleSet(bulletStyleSet);
                    }
                }

                s.simplify();

                // Simplification creates a list of lists of tags. The
                // individual lists must be flushed to skeleton and
                // segments, alternately. The lists can be empty.
                Iterator<List<HtmlObjects.HtmlElement>> it = s.getTagLists()
                        .iterator();
                List<HtmlObjects.HtmlElement> tags = it.next();
                // this is flushing the before tag list
                flushLeftOverTags(tags);
                while (it.hasNext())
                {
                    tags = it.next();
                    OfficeContentPostFilterHelper helper = new OfficeContentPostFilterHelper(
                            m_rules);
                    if (m_rules.useContentPostFilter())
                    {
                        // For GBS-2073, handle tags in the embedded contents
                        tags = helper.handleTagsInContent(tags);
                    }
                    if (m_rules.useInternalTextFilter()
                            && !m_extractor.isDoSegBeforeInlText())
                    {
                        // For GBS-2073, handle internal text in embedded
                        // contents
                        tags = helper.handleInternalText(tags);
                    }
                    if (m_rules.useContentPostFilter()
                            && OfficeContentPostFilterHelper.isAllTags(tags))
                    {
                        flushTextToSkeleton(tags);
                    }
                    else
                    {
                        List<HtmlObjects.HtmlElement> leadingTags = helper
                                .getLeadingTags(tags);
                        List<HtmlObjects.HtmlElement> trailingTags = helper
                                .getTrailingTags(tags);
                        flushTextToSkeleton(leadingTags);
                        // flushing the main tag list
                        flushTextToTranslatable(tags);
                        flushTextToSkeleton(trailingTags);
                    }
                    tags = it.next();
                    // this is flushing the after tag list
                    flushLeftOverTags(tags);
                }
            }
            else
            {
                // flush the segment
                flushTextToTranslatable(m_extractionCandidates);

                // flush any stuff left at the end of the segment
                flushLeftOverTags(tagsAfter);
            }
            m_needSpecialTagRemoveForPPT = false;
        }
        else
        {
            flushTextToSkeleton(m_extractionCandidates);
        }

        m_bContainsText = false;
        m_isUntranslatableEndTag = false;
        m_extractionCandidates.clear();
    }

    /**
     * <p>
     * Helper method for flushText(): flushes text and tags to a TMX skeleton
     * section.
     * </p>
     */
    protected void flushTextToSkeleton(HtmlObjects.HtmlElement o,
            HtmlObjects.HtmlElement last) throws ExtractorException
    {
        if (o instanceof HtmlObjects.Tag)
        {
            addTagToSkeleton((HtmlObjects.Tag) o, false);
        }
        else if (o instanceof HtmlObjects.CFTag)
        {
            addTagToSkeleton((HtmlObjects.CFTag) o, false);
        }
        else if (o instanceof HtmlObjects.PidComment)
        {
            HtmlObjects.PidComment x = (HtmlObjects.PidComment) o;
            o = new HtmlObjects.Comment(x.getComment());

            addCommentToSkeleton((HtmlObjects.Comment) o);
        }
        else if (o instanceof HtmlObjects.Comment)
        {
            addCommentToSkeleton((HtmlObjects.Comment) o);
        }
        else if (o instanceof HtmlObjects.InternalText)
        {
            String tt = ((HtmlObjects.InternalText) o).internalText;
            m_output.addSkeleton(tt);
        }
        else
        {
            String tt = o.toString();
            if (o.isFromOfficeContent)
            {
                // add for export merge
                m_output.addSkeleton(OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_START);
            }

            // restore lastCR &#13; for ppt files
            if (isPowerPointExtractor() && o instanceof HtmlObjects.Text
                    && last != null && last instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t = (HtmlObjects.Tag) last;
                ExtendedAttributeList atts = t.attributes;
                String styleValue = (atts == null) ? "" : atts
                        .getValue("style");
                if (styleValue.contains("lastCR") && tt != null
                        && tt.length() == 1 && tt.charAt(0) == 13)
                {
                    tt = "&#13;";
                }
            }

            m_output.addSkeleton(tt);
            if (o.isFromOfficeContent)
            {
                // add for export merge
                m_output.addSkeleton(OfficeContentPostFilterHelper.SKELETON_OFFICE_CONTENT_END);
            }
        }
    }

    /**
     * <p>
     * Helper method for flushText(): flushes text and tags to a TMX skeleton
     * section.
     * </p>
     */
    protected void flushTextToSkeleton(List<HtmlObjects.HtmlElement> p_elements)
            throws ExtractorException
    {
        HtmlObjects.HtmlElement lastO = null;
        for (HtmlObjects.HtmlElement o : p_elements)
        {
            flushTextToSkeleton(o, lastO);
            lastO = o;
        }
    }

    /**
     * <p>
     * Helper method for flushText(): flushes text and tags to a TMX
     * translatable section.
     * </p>
     * 
     * TODO: this is the place where the tag list can be modified for MS Office
     * fields and so on.
     */
    protected void flushTextToTranslatable(
            List<HtmlObjects.HtmlElement> p_segments) throws ExtractorException
    {
        boolean endTagToSkeleton = false;
        int forCount = 0;
        for (ListIterator<HtmlObjects.HtmlElement> it = p_segments
                .listIterator(); it.hasNext();)
        {
            HtmlObjects.HtmlElement o = it.next();
            forCount++;

            if (o instanceof HtmlObjects.Text
                    || o instanceof HtmlObjects.Newline)
            {
                HtmlObjects.HtmlElement he = (HtmlObjects.HtmlElement) o;
                // Combine consecutive text and newline nodes to get
                // correct whitespace normalization.
                StringBuffer buf = new StringBuffer();
                buf.append(o.toString());
                while (it.hasNext())
                {
                    o = it.next();

                    if (o instanceof HtmlObjects.Text
                            || o instanceof HtmlObjects.Newline)
                    {
                        buf.append(o.toString());
                    }
                    else
                    {
                        it.previous();
                        break;
                    }
                }

                String normalizedStr = normalizeString(buf.toString());
                if (!he.isInternalStyleContent)
                {
                    // Output text to the output structure.
                    m_output.addTranslatable(normalizedStr);
                }
                else
                {
                    outputInternalText(normalizedStr);
                }

                // Thu Nov 30 20:45:45 2000 CvdL: XXXXX text inside
                // javascript contexts are of type "string", but the
                // dataformat should be "html"?
                try
                {
                    if (m_isInsideJavaScript)
                    {
                        m_output.setTranslatableAttrs(
                                ExtractorRegistry.FORMAT_HTML, "string");
                    }
                    else
                    {
                        m_output.setTranslatableAttrs(
                                ExtractorRegistry.FORMAT_HTML, "text");
                    }
                }
                catch (DocumentElementException e) // SNH
                {
                    throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
                }
            }
            else if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t = (HtmlObjects.Tag) o;
                TmxTagGenerator tg;

                // Handle special cases first that can change the
                // sequence of tags.

                // Tue Aug 30 23:31:26 2005: Handle Word character
                // styles that the user declared to be untranslatable.
                if (isWordExtractor() && !t.isFromOfficeContent)
                {
                    // ouput UntranslatableSpan to skeleton, add new logic for
                    // GBS-2108 Get the content of the span tag
                    boolean isBlankInTag = true;
                    boolean hasText = false;
                    int flag = 0;
                    List<HtmlObjects.HtmlElement> skeletonList = new ArrayList<HtmlObjects.HtmlElement>();
                    skeletonList.add(o);
                    while (it.hasNext())
                    {
                        flag++;
                        o = it.next();
                        skeletonList.add(o);
                        if (o.isFromOfficeContent)
                        {
                            if (o instanceof HtmlObjects.Text)
                            {
                                hasText = true;
                                if (!Text.isBlank(o.toString().replace("\r\n",
                                        "")))
                                {
                                    isBlankInTag = false;
                                }
                            }
                            else if (o instanceof HtmlObjects.InternalText)
                            {
                                isBlankInTag = false;
                            }
                        }
                        else
                        {
                            if (o instanceof HtmlObjects.EndTag)
                            {
                                HtmlObjects.EndTag e = (HtmlObjects.EndTag) o;

                                if (t.tag.equalsIgnoreCase(e.tag) && t.isPaired
                                        && e.isPaired
                                        && t.partnerId == e.partnerId)
                                {
                                    break;
                                }
                            }
                            else if (o instanceof HtmlObjects.Text
                                    || o instanceof HtmlObjects.Newline)
                            {
                                hasText = true;
                                if (!Text.isBlank(o.toString().replace("\r\n",
                                        "")))
                                {
                                    isBlankInTag = false;
                                }
                            }
                            else if (o instanceof HtmlObjects.InternalText)
                            {
                                isBlankInTag = false;
                            }
                        }
                    }
                    // if no need to untranslated or there is only blank in
                    // this tag, buf will be added to skeleton
                    if (isUntranslatableSpan(t) || (isBlankInTag && hasText))
                    {
                        flushTextToSkeleton(skeletonList);
                        flushNextTagsToSkeleton(p_segments, it);
                        continue;
                    }
                    else
                    {
                        for (int i = 0; i < flag; i++)
                        {
                            it.previous();
                        }
                    }

                    // output the parent tags of UntranslatableSpan to skeleton
                    if (it.hasNext())
                    {
                        boolean outputToSkeleton = false;
                        Object nextO = it.next();

                        if (nextO instanceof HtmlObjects.Tag)
                        {
                            HtmlObjects.Tag nextT = (HtmlObjects.Tag) nextO;

                            outputToSkeleton = isUntranslatableSpan(nextT)
                                    && t.isPaired;
                        }

                        it.previous();

                        if (outputToSkeleton)
                        {
                            endTagToSkeleton = true;
                            m_output.addSkeleton(t.toString());
                            flushNextTagsToSkeleton(p_segments, it);
                            continue;
                        }
                    }

                    // output internal style tag
                    if (t.isInternalStyleContent)
                    {
                        List<Object> items = new ArrayList<Object>();
                        HtmlObjects.EndTag e = null;
                        StringBuffer startTag = new StringBuffer();
                        StringBuffer endTag = new StringBuffer();
                        StringBuffer textBuf = new StringBuffer();
                        startTag.append(t.toString());
                        boolean outputTogether = true;
                        items.add(t);
                        flag = 0;
                        while (it.hasNext())
                        {
                            flag++;
                            o = it.next();
                            items.add(o);

                            if (o instanceof HtmlObjects.EndTag)
                            {
                                endTag.append(o.toString());
                                e = (HtmlObjects.EndTag) o;

                                if (t.tag.equalsIgnoreCase(e.tag) && t.isPaired
                                        && e.isPaired
                                        && t.partnerId == e.partnerId)
                                {
                                    break;
                                }
                            }
                            else if (o instanceof HtmlObjects.Tag)
                            {
                                startTag.append(o.toString());

                                if (endTag.length() > 0 || textBuf.length() > 0)
                                {
                                    outputTogether = false;
                                }
                            }
                            else if (o instanceof HtmlObjects.Text
                                    || o instanceof HtmlObjects.Newline
                                    || o instanceof HtmlObjects.InternalText)
                            {
                                textBuf.append(getElementText(o));

                                if (endTag.length() > 0)
                                {
                                    outputTogether = false;
                                }
                            }
                        }

                        if (e == null)
                        {
                            for (int i = 0; i < flag; i++)
                            {
                                it.previous();
                            }
                        }
                        else
                        {
                            if (outputTogether)
                            {
                                tg = m_html2TmxMap.getPairedInternalTmxTag(t,
                                        true);
                                m_output.addTranslatableTmx(tg.getStart());
                                m_output.addTranslatable(startTag.toString());
                                m_output.addTranslatableTmx(tg.getEnd());

                                String tempText = normalizeString(textBuf
                                        .toString());
                                if (tempText.contains("&lt;")
                                        || tempText.contains("&gt;")
                                        || tempText.contains("&nbsp;")
                                        || tempText.contains("&amp;"))
                                {
                                    m_output.addTranslatableTmx(tempText);
                                }
                                else
                                {
                                    m_output.addTranslatable(tempText);
                                }

                                tg = m_html2TmxMap.getPairedTmxTag(e, false,
                                        e.isIsolated);
                                m_output.addTranslatableTmx(tg.getStart());
                                m_output.addTranslatable(endTag.toString());
                                m_output.addTranslatableTmx(tg.getEnd());
                            }
                            else
                            {
                                ListIterator<Object> itemsIt = items
                                        .listIterator();
                                while (itemsIt.hasNext())
                                {
                                    Object oo = itemsIt.next();

                                    if (oo instanceof HtmlObjects.Tag)
                                    {
                                        HtmlObjects.Tag too = (HtmlObjects.Tag) oo;
                                        tg = m_html2TmxMap.getPairedTmxTag(too,
                                                true, t.isIsolated);
                                        flushTagToTranslatable(tg, too);
                                    }
                                    else if (oo instanceof HtmlObjects.EndTag)
                                    {
                                        HtmlObjects.EndTag eoo = (HtmlObjects.EndTag) oo;
                                        tg = m_html2TmxMap.getPairedTmxTag(eoo,
                                                false, e.isIsolated);
                                        m_output.addTranslatableTmx(tg
                                                .getStart());
                                        m_output.addTranslatable(oo.toString());
                                        m_output.addTranslatableTmx(tg.getEnd());
                                    }
                                    else if (oo instanceof HtmlObjects.Text
                                            || oo instanceof HtmlObjects.Newline
                                            || oo instanceof HtmlObjects.InternalText)
                                    {
                                        String text = getElementText(oo);

                                        while (itemsIt.hasNext())
                                        {
                                            oo = itemsIt.next();

                                            if (oo instanceof HtmlObjects.Text
                                                    || oo instanceof HtmlObjects.Newline
                                                    || oo instanceof HtmlObjects.InternalText)
                                            {
                                                text = text
                                                        + getElementText(oo);
                                            }
                                            else
                                            {
                                                itemsIt.previous();
                                                break;
                                            }
                                        }

                                        String normalizedStr = normalizeString(text);
                                        outputInternalText(normalizedStr);
                                    }
                                }
                            }

                            continue;
                        }
                    }
                }

                // Fri Jun 20 01:10:07 2003 CvdL: changed from Word
                // only to all Office formats. See GSDEF 9412.
                if (isMsOfficeExtractor() && !t.isFromOfficeContent)
                {
                    // Thu Dec 18 02:21:01 2003 Treat mso-tab-counts
                    // as paragraph breaks.
                    if (isMsoTabCountSpan(t) || isMsoSpaceRun(t))
                    {
                        StringBuffer buf = new StringBuffer();

                        buf.append(t.toString());

                        while (it.hasNext())
                        {
                            o = it.next();

                            if (o instanceof HtmlObjects.Text
                                    || o instanceof HtmlObjects.Newline)
                            {
                                buf.append(o.toString());
                            }
                            else
                            {
                                it.previous();
                                break;
                            }
                        }

                        HtmlObjects.EndTag e = (HtmlObjects.EndTag) it.next();
                        buf.append(e.toString());

                        // Create paragraph break.
                        m_output.addSkeleton(buf.toString());
                        flushNextTagsToSkeleton(p_segments, it);
                        continue;
                    }
                    // Thu Jan 20 12:00:00 2011 Office Additions:
                    // detect series of nbsp that Word converted from
                    // tabs during conversion to HTML and output the first
                    // enclosing as skelenton
                    // Looks like <span
                    // style='mso-special-character:comment'>&nbsp;</span>
                    else if (forCount == 1 && isMsoSpecailChar(t, "comment"))
                    {
                        StringBuffer buf = new StringBuffer();

                        buf.append(t.toString());

                        while (it.hasNext())
                        {
                            o = it.next();

                            if (o instanceof HtmlObjects.Text
                                    || o instanceof HtmlObjects.Newline)
                            {
                                String txt = o.toString();
                                buf.append(replaceSpaceWithNbsp(txt));
                            }
                            else
                            {
                                it.previous();
                                break;
                            }
                        }

                        HtmlObjects.EndTag e = (HtmlObjects.EndTag) it.next();
                        buf.append(e.toString());

                        while (it.hasNext())
                        {
                            o = it.next();

                            if (o instanceof HtmlObjects.EndTag)
                            {
                                buf.append(o.toString());
                            }
                            else
                            {
                                it.previous();
                                break;
                            }
                        }

                        // Create paragraph break.
                        m_output.addSkeleton(buf.toString());
                        flushNextTagsToSkeleton(p_segments, it);
                        continue;
                    }
                    // OLD CODE FOR REFERENCE
                    //
                    // Tue Jun 17 23:13:18 20032 Office Additions:
                    // detect series of symbol font characters that Word
                    // didn't convert to Unicode chars during conversion
                    // to HTML and output the enclosing span as PH.
                    // See GSDEF00009445.
                    //
                    // Looks like <span style='font-family: Symbol;
                    // mso-ascii-font-family:Arial;mso-hansi-font-family:Arial;
                    // mso-char-type: symbol;mso-symbol-font-family:Symbol'>
                    // <span style='mso-char-type:symbol;
                    // mso-symbol-font-family:Symbol'>鑴�/span></span>
                    else if (isMsoSymbolRun(t))
                    {
                        StringBuffer buf = new StringBuffer();

                        // NOTE: this is not reliable code. There
                        // should be much more error checking.

                        // begin of main span
                        buf.append(t.toString());

                        // begin of embedded span
                        o = it.next();
                        buf.append(o.toString());

                        while (it.hasNext())
                        {
                            o = it.next();

                            if (o instanceof HtmlObjects.Text
                                    || o instanceof HtmlObjects.Newline)
                            {
                                buf.append(o.toString());
                            }
                            else
                            {
                                it.previous();
                                break;
                            }
                        }

                        // end of embedded span
                        o = it.next();
                        buf.append(o.toString());

                        // end of main span
                        o = it.next();
                        buf.append(o.toString());

                        m_output.addTranslatableTmx("<ph type=\"x-mso-symbol\">");
                        m_output.addTranslatable(buf.toString());
                        m_output.addTranslatableTmx("</ph>");

                        continue;
                    }
                    // Wed Apr 06 20:14:49 2005 Office Additions:
                    // write out footnote markers as a single PH.
                    else if (t.isPaired && !t.isIsolated
                            && isMsoReferenceMarker(t))
                    {
                        StringBuffer buf = new StringBuffer();

                        // begin of outer link or anchor (A)
                        buf.append(t.toString());

                        while (it.hasNext())
                        {
                            o = it.next();

                            buf.append(o.toString());

                            if (o instanceof HtmlObjects.EndTag)
                            {
                                HtmlObjects.EndTag endtag = (HtmlObjects.EndTag) o;

                                if (isMsoEndReference(endtag))
                                {
                                    break;
                                }
                            }
                        }

                        m_output.addTranslatableTmx("<ph type=\"x-mso-reference\">");
                        m_output.addTranslatable(buf.toString());
                        m_output.addTranslatableTmx("</ph>");

                        continue;
                    }
                    // An MSO paragraph marker, only needed for empty
                    // paragraphs. Since this paragraph is not empty
                    // (or this method would not have been called),
                    // skip these elements.

                    // December 21, 2007
                    // Sometimes </o:p> does not happen right after <o:p>,
                    // so, if here we just ignore the object happens right after
                    // <o:p>
                    // without any check whether she is </o:p>, maybe we ignored
                    // <o:p>
                    // but </o:p>. and we could not get a paired tag when
                    // converting
                    // html tag to TMX tag which will result in violating TMX
                    // specification.
                    // This is why there are these issues: some word document
                    // crash online editor,
                    // some word jobs could not download to work offline and
                    // some segments
                    // could not edit when working online.
                    /*
                     * else if (t.tag.toLowerCase().equals("o:p")) { o =
                     * it.next(); // "o" must be "</o:p>"
                     * 
                     * continue; }
                     */
                    // Hidden text in Word, Excel.
                    // If Word output inside placeholder,
                    // if Excel extract
                    else if (isMsoHiddenText(t))
                    {
                        StringBuffer buf = new StringBuffer();

                        // Find end tag and collect content in between
                        // start/end.
                        while (it.hasNext())
                        {
                            o = it.next();

                            if (o instanceof HtmlObjects.EndTag)
                            {
                                HtmlObjects.EndTag e = (HtmlObjects.EndTag) o;

                                if (t.tag.equalsIgnoreCase(e.tag) && t.isPaired
                                        && e.isPaired
                                        && t.partnerId == e.partnerId)
                                {
                                    it.previous();
                                    break;
                                }

                                if (!isExcelExtractor())
                                {
                                    buf.append(o.toString());
                                }

                            }
                            else
                            {
                                if ((isExcelExtractor())
                                        && ((o instanceof HtmlObjects.Newline) || (o instanceof HtmlObjects.Text)))
                                {
                                    buf.append(o.toString());
                                }
                                else if (!isExcelExtractor())
                                {
                                    buf.append(o.toString());
                                }

                            }
                        }

                        HtmlObjects.EndTag endtag = (HtmlObjects.EndTag) it
                                .next();
                        if (!isExcelExtractor())
                        {
                            m_output.addTranslatableTmx("<ph type=\"x-mso-hidden\">");
                            m_output.addTranslatable(t.toString());
                        }

                        m_output.addTranslatable(buf.toString());

                        if (!isExcelExtractor())
                        {
                            m_output.addTranslatable(endtag.toString());
                            m_output.addTranslatableTmx("</ph>");
                        }
                        continue;
                    }
                }

                if (t.isClosed || (!t.isPaired && !t.isIsolated))
                {
                    if (t.isFromOfficeContent)
                    {
                        tg = m_html2TmxMap.getPlaceholderTmxTag(t, true);
                    }
                    else
                    {
                        tg = m_html2TmxMap.getPlaceholderTmxTag(t.tag, true);
                    }
                }
                else
                {
                    if (isInternalTag(t))
                    {
                        tg = m_html2TmxMap.getPairedInternalTmxTag(t, true);
                    }
                    else
                    {
                        tg = m_html2TmxMap.getPairedTmxTag(t, true,
                                t.isIsolated);
                    }
                }

                flushTagToTranslatable(tg, t);
            }
            else if (o instanceof HtmlObjects.InternalText)
            {
                HtmlObjects.InternalText internalText = (HtmlObjects.InternalText) o;
                TmxTagGenerator tg = m_html2TmxMap
                        .getPairedTagForInternalText(internalText);

                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable(internalText.internalText);
                m_output.addTranslatableTmx(tg.getEnd());
            }
            else if (o instanceof HtmlObjects.CFTag)
            {
                HtmlObjects.CFTag t = (HtmlObjects.CFTag) o;
                TmxTagGenerator tg;

                if (t.isClosed || (!t.isPaired && !t.isIsolated))
                {
                    tg = m_html2TmxMap.getPlaceholderTmxTag(t.tag, true);
                }
                else
                {
                    tg = m_html2TmxMap.getPairedTmxTag(t, true, t.isIsolated);
                }

                flushTagToTranslatable(tg, t);
            }
            else if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag t = (HtmlObjects.EndTag) o;

                if (endTagToSkeleton)
                {
                    m_output.addSkeleton(t.toString());
                    flushNextTagsToSkeleton(p_segments, it);
                    endTagToSkeleton = false;
                }
                else
                {
                    TmxTagGenerator tg = m_html2TmxMap.getPairedTmxTag(t,
                            false, t.isIsolated);

                    m_output.addTranslatableTmx(tg.getStart());
                    m_output.addTranslatable(o.toString());
                    m_output.addTranslatableTmx(tg.getEnd());
                }
            }
            else if (o instanceof HtmlObjects.PidComment)
            {
                HtmlObjects.PidComment x = (HtmlObjects.PidComment) o;
                HtmlObjects.Comment c = new HtmlObjects.Comment(x.getComment());

                // PidComments are just comments that need to be
                // preserved, write out as normal comment.
                addCommentToTranslatable(c);
            }
            else if (o instanceof HtmlObjects.Comment)
            {
                HtmlObjects.Comment c = (HtmlObjects.Comment) o;

                // Handle potential SSI comments separately
                addCommentToTranslatable(c);
            }
            else if (o instanceof HtmlObjects.Declaration)
            {
                TmxTagGenerator tg = m_html2TmxMap.getPlaceholderTmxTag(
                        "declaration", true);

                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable(o.toString());
                m_output.addTranslatableTmx(tg.getEnd());
            }
        }

        m_html2TmxMap.resetCounter();
    }

    private void outputInternalText(String text)
    {
        String itext = InternalTextHelper.GS_INTERNALT_TAG_START + text
                + InternalTextHelper.GS_INTERNALT_TAG_END;
        HtmlObjects.InternalText internalText = new HtmlObjects.InternalText(
                itext);
        TmxTagGenerator tg = m_html2TmxMap
                .getPairedTagForInternalText(internalText);

        m_output.addTranslatableTmx(tg.getStart());
        if (text.contains("&lt;") || text.contains("&gt;")
                || text.contains("&nbsp;") || text.contains("&amp;"))
        {
            m_output.addTranslatableTmx(text);
        }
        else
        {
            m_output.addTranslatable(text);
        }
        m_output.addTranslatableTmx(tg.getEnd());
    }

    private String getElementText(Object oo)
    {
        String text = "";
        if (oo instanceof HtmlObjects.InternalText)
        {
            text = ((HtmlObjects.InternalText) oo).internalText;
        }
        else
        {
            text = oo.toString();
        }

        return text;
    }

    /**
     * Flushes the tags after a paragraph break to skeleton as well.
     */
    private void flushNextTagsToSkeleton(
            List<HtmlObjects.HtmlElement> segments,
            ListIterator<HtmlObjects.HtmlElement> it)
    {
        loop: while (it.hasNext())
        {
            HtmlObjects.HtmlElement o = it.next();
            if (o instanceof HtmlObjects.Tag)
            {
                if (o.isFromOfficeContent)
                {
                    if (it.hasNext())
                    {
                        // need to check the next tag
                        HtmlObjects.HtmlElement o2 = it.next();
                        if (o2 instanceof HtmlObjects.Text)
                        {
                            if (!Text
                                    .isBlank(o2.toString().replace("\r\n", "")))
                            {
                                it.previous();
                                it.previous();
                                break loop;
                            }
                        }
                    }
                    it.previous();
                    setPairedEndTagIsolated(it, (HtmlObjects.Tag) o);
                    flushTextToSkeleton(o, null);
                }
                else
                {
                    // !o.isFromOfficeContent
                    if (!o.isPaired || o.isIsolated)
                    {
                        flushTextToSkeleton(o, null);
                    }
                    else
                    {
                        it.previous();
                        break loop;
                    }
                }
            }
            else if (o instanceof HtmlObjects.CFTag
                    && (!o.isPaired || o.isIsolated))
            {
                flushTextToSkeleton(o, null);
            }
            else if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag et = (HtmlObjects.EndTag) o;
                for (HtmlObjects.HtmlElement e : segments)
                {
                    if (e instanceof HtmlObjects.Tag)
                    {
                        HtmlObjects.Tag t = (HtmlObjects.Tag) e;
                        if (t.tag.equalsIgnoreCase(et.tag) && t.isPaired
                                && et.isPaired && t.partnerId == et.partnerId)
                        {
                            if (t.isInTranslatable)
                            {
                                it.previous();
                                break loop;
                            }
                        }
                    }
                }
                flushTextToSkeleton(o, null);
            }
            else if (o instanceof HtmlObjects.Text)
            {
                if (Text.isBlank(o.toString().replace("\r\n", "")))
                {
                    m_output.addSkeleton(o.toString());
                }
                else
                {
                    it.previous();
                    break loop;
                }
            }
            else
            {
                it.previous();
                break loop;
            }
        }
    }

    /**
     * Sets the end tag which is paired with the tag isolated.
     */
    private void setPairedEndTagIsolated(
            ListIterator<HtmlObjects.HtmlElement> it, HtmlObjects.Tag t)
    {
        int flag = 0;
        while (it.hasNext())
        {
            flag++;
            HtmlObjects.HtmlElement e = it.next();
            if (e instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag et = (HtmlObjects.EndTag) e;

                if (t.tag.equalsIgnoreCase(et.tag) && t.isPaired && et.isPaired
                        && t.partnerId == et.partnerId)
                {
                    et.isIsolated = true;
                    break;
                }
            }
        }
        for (int i = 0; i < flag; i++)
        {
            it.previous();
        }
    }

    /**
     * Check if it is start tag in &lt;GS-INTERNAL-TEXT&gt;#message
     * here#&lt;/GS-INTERNAL-TEXT&gt;
     * 
     * @param t
     * @return
     */
    private boolean isGSInternalTag(HtmlObjects.Tag t)
    {
        return InternalTextHelper.GS_INTERNALT_TAG_START.equals("<" + t.tag
                + ">");
    }

    private boolean isInternalTag(HtmlObjects.Tag tag)
    {
        if (htmlInternalTags != null)
        {
            for (HtmlInternalTag internalTag : htmlInternalTags)
            {
                if (internalTag.accept(tag))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInternalEndTag(HtmlObjects.EndTag tag)
    {
        if (m_internalStack.size() > 0)
        {
            String lastTag = m_internalStack.peek();
            if (lastTag.equals(tag.tag))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Flushes a single tag as part of flushing out the list of potential
     * extraction candidates.
     * 
     * <p>
     * Called from flushTextToTranslatable().
     */
    private void flushTagToTranslatable(TmxTagGenerator tg, HtmlObjects.Tag t)
            throws ExtractorException
    {
        // tell the paired end tag not to be put into skeleton
        t.isInTranslatable = true;

        if (t.isIgnore() || tg.isInternalTag())
        {
            m_output.addTranslatableTmx(tg.getStart());
            m_output.addTranslatable(t.original);
            m_output.addTranslatableTmx(tg.getEnd());
            return;
        }

        m_output.addTranslatableTmx(tg.getStart());

        if (t.isMerged)
        {
            m_output.addTranslatable(t.original);
            m_output.addTranslatableTmx(tg.getEnd());
            return;
        }
        else
        {
            m_output.addTranslatable("<" + t.tag);
        }

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            // Note: Attributes may have been decoded and stray <% or
            // <CF sequences may have been generated. Oh well.

            Object o = it.next();

            if (o instanceof HtmlObjects.Attribute)
            {
                HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) o;
                String strValue = Text.removeQuotes(attrib.value);

                // Value-less Attribute - quick exit
                if (strValue == null || strValue.length() == 0
                        || Text.isBlank(strValue))
                {
                    m_output.addTranslatable(" " + attrib.toString());
                    continue;
                }

                // When extracting ASP/JSP, check for att="<%=...%>"
                if (isXSPExtractor()
                        && (strValue.indexOf("<%") >= 0 || strValue
                                .indexOf("%>") >= 0))
                {
                    m_output.addTranslatable(" " + attrib.toString());
                    continue;
                }

                // When extracting CFM, check for att="<cf ... >".
                if (isCFExtractor()
                        && (strValue.indexOf("<cf") >= 0 || strValue
                                .indexOf("<CF") >= 0))
                {
                    m_output.addTranslatable(" " + attrib.toString());
                    continue;
                }

                String quote = Text.getQuoteCharacter(attrib.value);

                // Special case first: <A href="javascript:...">
                if (attrib.name.equalsIgnoreCase(ACTION)
                        && strValue.toLowerCase().startsWith("javascript:"))
                {
                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatable("javascript:");

                    // this is a URL, needs to be decoded
                    String temp = decodeUrl(strValue.substring(11));

                    m_output.addTranslatableTmx(switchToJavaScript(temp, true));
                    m_output.addTranslatable(quote);
                }

                // Style Attribute: <tag style="...">
                else if (!isIgnoreInvalidHtmlTags()
                        && attrib.name.equalsIgnoreCase(STYLE))
                {
                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx(switchToStyles(
                            EditUtil.decodeXmlEntities(strValue), true));
                    m_output.addTranslatable(quote);
                }

                // Event Handler Attribute: <div onClick="...">
                else if (m_rules.isEventHandlerAttribute(attrib.name)
                        && (t.isFromOfficeContent ? m_rules
                                .isContentTranslatableAttribute(attrib.name)
                                : m_rules.isTranslatableAttribute(t.tag,
                                        attrib.name)))
                {
                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx(switchToJavaScript(strValue,
                            true, true));
                    m_output.addTranslatable(quote);
                }

                // Localizable Attribute
                else if (!t.isFromOfficeContent
                        && m_rules.isLocalizableAttribute(t.tag, attrib.name))
                {
                    String strAttributeType = m_rules.getLocalizableAttribType(
                            t.tag, attrib.name);

                    // CvdL: can't decode - information loss - can't merge
                    // if (strAttributeType.startsWith("url"))
                    // {
                    // strValue = decodeUrl(strValue);
                    // }

                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx("<sub type=\"");
                    m_output.addTranslatable(strAttributeType);
                    m_output.addTranslatableTmx("\" locType=\"localizable\">");
                    m_output.addTranslatable(strValue);
                    m_output.addTranslatableTmx("</sub>");
                    m_output.addTranslatable(quote);
                }

                // Translatable Attribute
                else if ((t.isFromOfficeContent ? m_rules
                        .isContentTranslatableAttribute(attrib.name) : m_rules
                        .isTranslatableAttribute(t.tag, attrib.name))
                        && !m_extractor.exclude())
                {
                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx("<sub type=\"");
                    m_output.addTranslatable(attrib.name.toLowerCase());
                    m_output.addTranslatableTmx("\" locType=\"translatable\">");
                    m_output.addTranslatable(strValue);
                    m_output.addTranslatableTmx("</sub>");
                    m_output.addTranslatable(quote);
                }

                // Simple non-translatable, non-localizable boring attribute
                else
                {
                    m_output.addTranslatable(" " + attrib.toString());
                }
            }
            // Not an attribute but a CFTag
            else
            {
                m_output.addTranslatable(" " + o.toString());
            }
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addTranslatable(" ");
            index--;
        }
        m_output.addTranslatable(end);

        m_output.addTranslatableTmx(tg.getEnd());
    }

    private void flushTagToTranslatable(TmxTagGenerator tg, HtmlObjects.CFTag t)
            throws ExtractorException
    {
        // tell the paired end tag not to be put into skeleton
        t.isInTranslatable = true;

        m_output.addTranslatableTmx(tg.getStart());
        m_output.addTranslatable("<");
        m_output.addTranslatable(t.tag);

        if (m_rules.isCFExpressionTag(t.tag))
        {
            String expr = t.attributes.toString();
            m_output.addTranslatableTmx(switchToCFScript(expr, true));
        }
        else
        {
            for (Iterator it = t.attributes.iterator(); it.hasNext();)
            {
                HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it
                        .next();
                String strValue = Text.removeQuotes(attrib.value);

                // Value-less Attribute - quick exit
                if (strValue == null || strValue.length() == 0
                        || Text.isBlank(strValue))
                {
                    m_output.addTranslatable(" " + attrib.toString());
                    continue;
                }

                String quote = Text.getQuoteCharacter(attrib.value);

                // Event Handler Attribute: <CFFORM onError="...">
                if (m_rules.isEventHandlerAttribute(attrib.name))
                {
                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx(switchToJavaScript(strValue,
                            true));
                    m_output.addTranslatable(quote);
                }

                // Localizable Attribute
                else if (m_rules.isLocalizableAttribute(t.tag, attrib.name))
                {
                    String strAttributeType = m_rules.getLocalizableAttribType(
                            t.tag, attrib.name);

                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx("<sub type=\"");
                    m_output.addTranslatable(strAttributeType);
                    m_output.addTranslatableTmx("\" locType=\"localizable\">");
                    m_output.addTranslatable(strValue);
                    m_output.addTranslatableTmx("</sub>");
                    m_output.addTranslatable(quote);
                }

                // Translatable Attribute
                else if (m_rules.isTranslatableAttribute(t.tag, attrib.name)
                        && !m_extractor.exclude())
                {
                    m_output.addTranslatable(" " + attrib.name + "=" + quote);
                    m_output.addTranslatableTmx("<sub type=\"");
                    m_output.addTranslatable(attrib.name.toLowerCase());
                    m_output.addTranslatableTmx("\" locType=\"translatable\">");
                    m_output.addTranslatable(strValue);
                    m_output.addTranslatableTmx("</sub>");
                    m_output.addTranslatable(quote);
                }

                // Simple non-translatable, non-localizable boring attribute
                else
                {
                    m_output.addTranslatable(" " + attrib.toString());
                }
            }
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addTranslatable(" ");
            index--;
        }
        m_output.addTranslatable(end);

        m_output.addTranslatableTmx(tg.getEnd());
    }

    protected boolean isAccumulatingText()
    {
        return !m_extractionCandidates.isEmpty();
    }

    static final String strJava = "*java*";
    static final String strJavaScript = "*jscript"
            + "*javascript*javascript1.1*javascript1.2*javascript1.3*javascript1.4"
            + "*ecmascript*";
    static final String strVbScript = "*vbs*vbscript*";
    static final String strPerlScript = "*perlscript*";
    static final String strPythonScript = "*python*pythonscript*";

    protected int getScriptLanguage(String p_script)
    {
        p_script = p_script.toLowerCase();

        if (strJavaScript.indexOf("*" + p_script + "*") != -1)
        {
            return EC_JAVASCRIPT;
        }

        if (strVbScript.indexOf("*" + p_script + "*") != -1)
        {
            return EC_VBSCRIPT;
        }

        if (strPerlScript.indexOf("*" + p_script + "*") != -1)
        {
            return EC_PERLSCRIPT;
        }

        if (strPythonScript.indexOf("*" + p_script + "*") != -1)
        {
            return EC_PYTHONSCRIPT;
        }

        if (strJava.indexOf("*" + p_script + "*") != -1)
        {
            return EC_JAVA;
        }

        return EC_UNKNOWNSCRIPT;
    }

    protected int getScriptLanguage(HtmlObjects.Script t)
    {
        int result = EC_JAVASCRIPT;

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attr = (HtmlObjects.Attribute) it.next();

            if (attr.name.equalsIgnoreCase("language"))
            {
                result = getScriptLanguage(Text.removeQuotes(attr.value));
            }
        }

        return result;
    }

    /**
     * Called from flushTextToTranslatable().
     */
    protected String normalizeString(String p_text)
    {
        if (!m_bPreserveWhite && !m_preserveAllWhite)
        {
            // Fri Apr 12 16:55:59 2002 CvdL: this was written as a
            // no-op. Unless somebody tells me this was indeed a
            // feature, we do remove whitespace.
            p_text = Text.normalizeWhiteSpaces(p_text);
        }

        return p_text;
    }

    /**
     * <p>
     * Matches embedded text in a URL to a Macromedia file. The URL looks like
     * this: <code>&lt;PARAM NAME=movie
     * VALUE="http://209.101.232.126/Macromedia/text_over_logo.swt?type=swf&amp;mytext=Global%20Strategic%20Text%20Management"&gt;</code>
     * </p>
     * 
     * <p>
     * The match tries to extract the argument to the <code>mytext=</code> or
     * <code>message=</code> parameter.
     * </p>
     */
    private RegExMatchInterface matchGeneratorText(String p_value)
            throws ExtractorException
    {
        RegEx regex = new RegEx();
        String generator = null;
        RegExMatchInterface mr = null;

        try
        {
            mr = regex.matchSubstring(p_value,
                    "\\.swt\\?.*(mytext|message)=([^\\&\\'\"]+)", false);
        }
        catch (RegExException e) // if the regex was improper
        {
            throw new ExtractorException(REGEX_ERROR, e);
        }

        return mr;
    }

    /**
     * <p>
     * Matches embedded text in a JHTML <param value=""> attribute.
     * </p>
     */
    private RegExMatchInterface matchValueText(String p_value)
            throws ExtractorException
    {
        RegEx regex = new RegEx();
        String generator = null;
        RegExMatchInterface mr = null;

        try
        {
            mr = regex.matchSubstring(p_value, "([a-z].+)=(.+)", false);
        }
        catch (RegExException e) // if the regex was improper
        {
            throw new ExtractorException(REGEX_ERROR, e);
        }

        return mr;
    }

    /**
     * <p>
     * Handle &lt;jsp:param name=... value=...&gt;.
     * </p>
     */
    private void processJspParam(HtmlObjects.Tag t) throws ExtractorException
    {
        boolean b_extractValue = false;

        if (t.isDefinedAttribute(NAME))
        {
            String strName = Text.removeQuotes(t.attributes.getValue(NAME));

            if (m_rules.isTranslatableJspParam(strName))
            {
                b_extractValue = true;
            }
        }

        m_output.addSkeleton("<");
        m_output.addSkeleton(t.tag);

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it.next();
            String strValue = Text.removeQuotes(attrib.value);

            // When extracting ASP/JSP, check for att="<%=...%>"
            if (strValue.length() == 0
                    || (m_xspLanguage != EC_UNKNOWNSCRIPT
                            && strValue.startsWith("<%") && strValue
                                .endsWith("%>")))
            {
                m_output.addSkeleton(" " + attrib.toString());
                continue;
            }

            String quote = Text.getQuoteCharacter(attrib.value);

            m_output.addSkeleton(" " + attrib.name + "=" + quote);

            if (attrib.name.equals(VALUE) && b_extractValue)
            {
                m_output.addTranslatable(strValue);
            }
            else
            {
                m_output.addSkeleton(strValue);
            }

            m_output.addSkeleton(quote);
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addSkeleton(" ");
            index--;
        }
        m_output.addSkeleton(end);
    }

    /**
     * <p>
     * "Generator" tags are <code>&lt;embed&gt;</code> and
     * <code>&lt;param&gt;</code> (the latter appearing inside an
     * <code>&lt;object&gt;</code> tag).
     * </p>
     * 
     * <p>
     * They seem to have localizable (translatable?) content inside some of
     * their url-style attribute values.
     * </p>
     */
    private void processGenerator(HtmlObjects.Tag t) throws ExtractorException
    {
        String generator = "";
        RegExMatchInterface mr;

        m_output.addSkeleton("<" + t.tag);
        String lastQuote = "";
        String lastValue = "";

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it.next();
            String strValue = Text.removeQuotes(attrib.value);

            // When extracting ASP/JSP, check for att="<%=...%>"
            if (m_xspLanguage != EC_UNKNOWNSCRIPT && strValue.startsWith("<%")
                    && strValue.endsWith("%>"))
            {
                m_output.addSkeleton(" " + attrib.toString());
                continue;
            }

            // for GBS-2071 <embed src="%(link6)" width="330 height="190">
            if (strValue == null && lastValue.contains("="))
            {
                m_output.addSkeleton(attrib.toString() + lastQuote);
                lastQuote = "";
                lastValue = "";
                continue;
            }

            // for <embed src="%(link6)" contenteditable height="190">
            if (strValue == null)
            {
                m_output.addSkeleton(" " + attrib.toString());
                lastQuote = "";
                lastValue = "";
                continue;
            }

            String quote = Text.getQuoteCharacter(attrib.value);

            m_output.addSkeleton(" " + attrib.name + "=" + quote);

            // Match "\\.swt\\?.*(mytext|message)=([^\\&\\'\"]+)"
            if ((mr = matchGeneratorText(strValue)) != null)
            {
                int generatorStart = mr.beginOffset(2);
                int generatorEnd = mr.endOffset(2);

                String first = strValue.substring(0, generatorStart);

                // XXXX This is a URL and should be localizable.
                m_output.addSkeleton(first);

                String second = strValue
                        .substring(generatorStart, generatorEnd);
                // XXXX This should not be localizable, but TRANSLATABLE.
                m_output.addLocalizable(second);

                try
                {
                    m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_HTML,
                            "generator");
                }
                catch (DocumentElementException e) // SNH (Should Not Happen)
                {
                    throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
                }

                String third = strValue.substring(generatorEnd);
                m_output.addSkeleton(third);
            }
            // JHTML <param value="...">. We should extract
            // value="message=Enter an address" but not
            // value="bean=some.java.class".
            // In JHTML mode we should extract all non-qualified values.
            else if (attrib.name.equals(VALUE)
                    && (mr = matchValueText(strValue)) != null)
            {
                int textStart = mr.beginOffset(2);
                int textEnd = mr.endOffset(2);

                String first = strValue.substring(0, textStart);

                m_output.addSkeleton(first);

                String second = strValue.substring(textStart, textEnd);
                m_output.addTranslatable(second);
            }
            else if (m_rules.isLocalizableAttribute(t.tag, attrib.name))
            {
                m_output.addLocalizable(strValue);

                try
                {
                    String strAttributeType = m_rules.getLocalizableAttribType(
                            t.tag, attrib.name);
                    m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_HTML,
                            strAttributeType);
                }
                catch (DocumentElementException e)
                {
                    throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
                }
            }
            else
            {
                m_output.addSkeleton(strValue);
            }

            m_output.addSkeleton(quote);
            lastQuote = quote;
            lastValue = strValue == null ? "" : strValue;
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addSkeleton(" ");
            index--;
        }
        m_output.addSkeleton(end);
    }

    /**
     * <P>
     * &lt;GS&gt; tags are output using a special method in the Output class.
     * Tag attributes must be passed as method arguments.
     * </P>
     * 
     * <P>
     * Called from <code>addTagToSkeleton()</code>.
     * </P>
     * 
     * @see Output
     */
    protected void processGsa(HtmlObjects.Tag t) throws ExtractorException
    {
        String extract = null;
        String desc = null;
        String locale = null;
        String add = null;
        boolean delete = false;
        String added = null;
        String deleted = null;
        String snippetName = null;
        String snippetId = null;

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it.next();

            decodeEntities(attrib);

            if (attrib.name.equalsIgnoreCase(DiplomatNames.Attribute.EXTRACT))
            {
                extract = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name
                    .equalsIgnoreCase(DiplomatNames.Attribute.DESCRIPTION))
            {
                desc = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name
                    .equalsIgnoreCase(DiplomatNames.Attribute.LOCALE))
            {
                locale = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name.equalsIgnoreCase(DiplomatNames.Attribute.ADD))
            {
                add = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name
                    .equalsIgnoreCase(DiplomatNames.Attribute.DELETE))
            {
                delete = true;
            }
            else if (attrib.name
                    .equalsIgnoreCase(DiplomatNames.Attribute.ADDED))
            {
                added = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name.equalsIgnoreCase(DiplomatNames.Attribute.NAME))
            {
                snippetName = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name.equalsIgnoreCase(DiplomatNames.Attribute.ID))
            {
                snippetId = Text.removeQuotes(attrib.value);
            }
            else if (attrib.name
                    .equalsIgnoreCase(DiplomatNames.Attribute.DELETED))
            {
                deleted = Text.removeQuotes(attrib.value);
            }
            else
            {
                // unknown attribute...
            }
        }

        try
        {
            if (t.isClosed)
            {
                m_output.addEmptyGsa(extract, desc, locale, add, delete, added,
                        deleted, snippetName, snippetId);
            }
            else
            {
                m_output.addGsaStart(extract, desc, locale, add, delete, added,
                        deleted, snippetName, snippetId);
                ++m_gsaCounter;
            }
        }
        catch (DocumentElementException ex)
        {
            throw new ExtractorException(HTML_GS_TAG_ERROR, ex.getMessage());
        }
    }

    /**
     * <P>
     * Special function has been written for the meta tag since it is the only
     * one for which the extraction of an attribute ("content") depends of the
     * value of another attribute ("name").
     * 
     * <P>
     * Basically, what this routing should decide is
     * </P>
     * <UL>
     * <LI>when a CONTENT is translatable or localizable, depending on the NAME
     * attribute</LI>
     * <LI>when a sub-part of a content is localizable, depending on the
     * HTTP-EQUIV attribute</LI>
     * </UL>
     * <P>
     * The HTTP-EQUIV attribute is never loczalizable since its values are cast
     * in stone in the HTTP standards.
     * <P>
     * The NAME attribute is never translatable nor localizable since it is
     * either a predefined value in the HTML standard, or it is a tag assigned
     * by a content management system, interfering with which is not the purpose
     * of this application.
     * </P>
     * 
     * <P>
     * Some examples:
     * <UL>
     * <LI>META HTTP-EQUIV="Content-Type" content="text/html; charset=x" - only
     * the charset is localizable</LI>
     * <LI>META HTTP-EQUIV="Keywords" content="key,key" - content is
     * translatable</LI>
     * <LI>META NAME="Keywords" content="key,key" - content is translatable</LI>
     * <LI>META NAME="ROBOTS" content="INDEX,FOLLOW,ALL" - content is not
     * translatable</LI>
     * <LI>META NAME="EXPIRES" content="Fri Sep 01 00:25:01 2000" - content is
     * not translatable</LI>
     * </UL>
     * 
     * @param t
     *            HtmlObjects.Tag: a META tag with all its attributes.
     */
    protected void processMeta(HtmlObjects.Tag t) throws ExtractorException
    {
        boolean bExtractCharset = false;
        boolean bContentIsTranslatable = true;

        // A meta tag usually has 2 attributes in any order.
        // First, find out if a NAME/HTTP-EQUIV exists that
        // prevents translatability of the CONTENT attribute.
        // Also canonicalize the order of the attributes.

        List<HtmlObjects.Attribute> canonicalList = new ArrayList<HtmlObjects.Attribute>();

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it.next();

            if (attrib.name.equalsIgnoreCase(HTTP_EQUIV))
            {
                String strValue = Text.removeQuotes(attrib.value);

                if (strValue.equalsIgnoreCase("content-type"))
                {
                    bExtractCharset = true;
                }
                else if (m_rules.isNonTranslatableMetaAttribute(strValue))
                {
                    bContentIsTranslatable = false;
                }

                canonicalList.add(attrib);
            }
            else if (attrib.name.equalsIgnoreCase(NAME))
            {
                String strValue = Text.removeQuotes(attrib.value);

                if (m_rules.isNonTranslatableMetaAttribute(strValue))
                {
                    bContentIsTranslatable = false;
                }

                canonicalList.add(0, attrib);
            }
            else
            {
                canonicalList.add(attrib);
            }
        }

        // Now output the tag and all its attributes in canonical order
        m_output.addSkeleton("<" + t.tag);

        for (Iterator it = canonicalList.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it.next();
            String strValue = Text.removeQuotes(attrib.value);

            // Value-less Attribute - quick exit
            if (strValue == null || strValue.length() == 0)
            {
                m_output.addSkeleton(" " + attrib.toString());
                continue;
            }

            // When extracting ASP/JSP, check for att="<%=...%>"
            if (m_xspLanguage != EC_UNKNOWNSCRIPT && strValue.startsWith("<%")
                    && strValue.endsWith("%>"))
            {
                m_output.addSkeleton(" " + attrib.toString());
                continue;
            }

            String quote = Text.getQuoteCharacter(attrib.value);

            if (attrib.name.equalsIgnoreCase(CONTENT))
            {
                m_output.addSkeleton(" " + attrib.name + "=" + quote);

                if (bExtractCharset)
                {
                    // try pulling out codeset, other parts of attr
                    // value go to skeleton
                    RegEx regex = new RegEx();
                    RegExMatchInterface mr = null;

                    try
                    {
                        mr = regex.matchSubstring(strValue,
                                "charset=([^ \t\r\n\'\"]+)", false);
                    }
                    catch (RegExException e) // Should Not Happen
                    {
                        // System.err.println(e.toString());
                        throw new ExtractorException(REGEX_ERROR, e);
                    }

                    // found a charset and we're allowed to extract it
                    if (mr != null && m_rules.doExtractCharset())
                    {
                        int charsetStart = mr.beginOffset(1);
                        int charsetEnd = mr.endOffset(1);

                        String first = strValue.substring(0, charsetStart);
                        m_output.addSkeleton(first);

                        String second = strValue.substring(charsetStart,
                                charsetEnd);
                        m_output.addLocalizable(second);

                        try
                        {
                            m_output.setLocalizableAttrs(
                                    ExtractorRegistry.FORMAT_HTML, "charset");
                        }
                        catch (DocumentElementException e) // SNH
                        {
                            throw new ExtractorException(HTML_UNEXPECTED_ERROR,
                                    e);
                        }

                        String third = strValue.substring(charsetEnd,
                                strValue.length());

                        m_output.addSkeleton(third);
                    }
                    else
                    // no charset send to skeleton
                    {
                        m_output.addSkeleton(strValue);
                    }
                }
                else if (bContentIsTranslatable)
                {
                    m_output.addTranslatable(strValue);

                    try
                    {
                        m_output.setTranslatableAttrs(
                                ExtractorRegistry.FORMAT_HTML, "meta-content");
                    }
                    catch (DocumentElementException e) // SNH
                    {
                        throw new ExtractorException(HTML_UNEXPECTED_ERROR, e);
                    }
                }
                else
                {
                    m_output.addSkeleton(strValue);
                }

                m_output.addSkeleton(quote);
            }
            // attributes other than CONTENT go to the skeleton as is
            else
            {
                m_output.addSkeleton(" " + attrib.toString());
            }
        }

        String end = ">";
        if (t.isClosed)
        {
            end = "/>";
        }
        int index = t.toString().indexOf(end);
        while (' ' == t.toString().charAt(index - 1))
        {
            m_output.addSkeleton(" ");
            index--;
        }
        m_output.addSkeleton(end);
    }

    /**
     * Dumps a spacer gif to skeleton. Can only be called from
     * addTagToSkeleton().
     */
    protected void processSpacerGif(HtmlObjects.Tag t)
            throws ExtractorException
    {
        m_output.addSkeleton(t.toString());
    }

    protected void skipText(String p_strText)
    {
        m_strSkippedText.append(p_strText);
    }

    protected String decodeUrl(String url)
    {
        try
        {
            return URLDecoder.decode(url, "UTF-8");
        }
        catch (Throwable ex)
        {
            // do nothing...
            return url;
        }
    }

    /**
     * <p>
     * Decodes entities in an attribute value. Returns the original, undecoded
     * attribute value.
     * </p>
     */
    private void decodeEntities(HtmlObjects.Attribute attr)
    {
        String text = attr.value;

        if (text != null)
        {
            // Could add &apos; to the recognized entities.
            text = m_htmlDecoder.decodeString(text, null);
            attr.value = text;
        }
    }

    /**
     * Tests the style tag if it has a src attribute with value "text/css".
     * 
     * @returns <code>true</code> if src="text/css" or not present. Returns
     *          <code>false</code> if src is present but has any other value.
     */
    protected boolean isCSSStyleSheet(HtmlObjects.Style s)
    {
        boolean b_res = true;

        for (Iterator it = s.attributes.iterator(); it.hasNext();)
        {
            HtmlObjects.Attribute attrib = (HtmlObjects.Attribute) it.next();

            if (attrib.name.equalsIgnoreCase("type"))
            {
                String value = Text.removeQuotes(attrib.value);
                if (!value.equalsIgnoreCase("text/css"))
                {
                    b_res = false;
                }
                break;
            }
        }

        return b_res;
    }

    /**
     * <p>
     * Determines if a comment contains SSI instructions ("server-side
     * includes") that possibly have to be extracted as loc/trans.
     * </p>
     * 
     * <p>
     * SSI comments have the form &lt;!--#TAG {var="value"} --&gt;. TAG can be
     * "include" etc. var="value" are standard HTML attribute-value pairs.
     * </p>
     */
    protected boolean isSSIComment(HtmlObjects.Comment c)
    {
        boolean b_res = false;

        String str_comment = c.getComment().trim();

        if (str_comment.startsWith("#include"))
        {
            b_res = true;
        }

        return b_res;
    }

    protected String switchToJavaScript(String p_strJSCode, boolean p_Embedded,
            boolean... noUseJsFunction) throws ExtractorException
    {
        int javascript = -1;
        int html = -1;
        String result = "";

        if (p_strJSCode != null && p_strJSCode.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((javascript = er
                    .getFormatId(ExtractorRegistry.FORMAT_JAVASCRIPT)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strJSCode);
            input.setLocale(m_input.getLocale());
            input.setType(javascript);

            try
            {
                if (noUseJsFunction != null && noUseJsFunction.length != 0
                        && noUseJsFunction[0])
                {
                    String[] jsFunctionTexts = new String[]
                    { jsFunctionText, "" + noUseJsFunction[0] };
                    if (p_Embedded)
                    {
                        result = m_extractor.switchExtractorEmbedded(html,
                                input, jsFunctionTexts);
                    }
                    else
                    {
                        m_extractor.switchExtractor(html, input,
                                jsFunctionTexts);
                    }
                }
                else
                {
                    if (p_Embedded)
                    {
                        result = m_extractor.switchExtractorEmbedded(html,
                                input, jsFunctionText);
                    }
                    else
                    {
                        m_extractor
                                .switchExtractor(html, input, jsFunctionText);
                    }
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_JS_ERROR,
                        "Embedded JavaScript parse exception between " + "("
                                + m_iEmbeddedLine + ":" + m_iEmbeddedCol + ") "
                                + "and ("
                                + m_extractor.getParser().getCurrentLine()
                                + ":"
                                + m_extractor.getParser().getCurrentColumn()
                                + "):\n" + e.toString());
            }
        }

        return result;
    }

    protected String switchToVB(String p_strVBCode, boolean p_Embedded)
            throws ExtractorException
    {
        int vb = -1;
        int html = -1;
        String result = "";

        if (p_strVBCode != null && p_strVBCode.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((vb = er.getFormatId(ExtractorRegistry.FORMAT_VBSCRIPT)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strVBCode);
            input.setLocale(m_input.getLocale());
            input.setType(vb);

            try
            {
                if (p_Embedded)
                {
                    result = m_extractor.switchExtractorEmbedded(html, input);
                }
                else
                {
                    m_extractor.switchExtractor(html, input);
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_VB_ERROR,
                        "Embedded VB parse exception between " + "("
                                + m_iEmbeddedLine + ":" + m_iEmbeddedCol + ") "
                                + "and ("
                                + m_extractor.getParser().getCurrentLine()
                                + ":"
                                + m_extractor.getParser().getCurrentColumn()
                                + "):\n" + e.toString());
            }
        }

        return result;
    }

    protected String switchToJava(String p_strJSCode, boolean p_embedded,
            String p_parseMode) throws ExtractorException
    {
        int java = -1;
        int html = -1;
        String result = "";

        if (p_strJSCode != null && p_strJSCode.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((java = er.getFormatId(ExtractorRegistry.FORMAT_JAVA)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strJSCode);
            input.setLocale(m_input.getLocale());
            input.setType(java);
            // communicate parse mode through rules mechanism
            input.setRules(p_parseMode);

            try
            {
                if (p_embedded)
                {
                    result = m_extractor.switchExtractorEmbedded(html, input);
                }
                else
                {
                    m_extractor.switchExtractor(html, input);
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_JAVA_ERROR,
                        "Embedded Java parse exception between " + "("
                                + m_iEmbeddedLine + ":" + m_iEmbeddedCol + ") "
                                + "and ("
                                + m_extractor.getParser().getCurrentLine()
                                + ":"
                                + m_extractor.getParser().getCurrentColumn()
                                + "):\n" + e.toString());
            }
        }

        return result;
    }

    protected String switchToStylesheet(String p_strStylesheet,
            boolean p_embedded) throws ExtractorException
    {
        int css = -1;
        int html = -1;

        if (p_strStylesheet != null && p_strStylesheet.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((css = er.getFormatId(ExtractorRegistry.FORMAT_CSS)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strStylesheet);
            input.setLocale(m_input.getLocale());
            input.setType(css);

            try
            {
                if (p_embedded)
                {
                    return m_extractor.switchExtractorEmbedded(html, input);
                }
                else
                {
                    m_extractor.switchExtractor(html, input);
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_CSS_ERROR,
                        "Embedded CSS parse exception in line "
                                + m_extractor.getParser().getCurrentLine()
                                + " column "
                                + m_extractor.getParser().getCurrentColumn()
                                + ":\n" + e.toString());
            }
        }

        return "";
    }

    protected String switchToStyles(String p_strStyles, boolean p_embedded)
            throws ExtractorException
    {
        int css_styles = -1;
        int html = -1;

        if (p_strStyles != null && p_strStyles.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((css_styles = er
                    .getFormatId(ExtractorRegistry.FORMAT_CSS_STYLE)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strStyles);
            input.setLocale(m_input.getLocale());
            input.setType(css_styles);

            try
            {
                if (p_embedded)
                {
                    return m_extractor.switchExtractorEmbedded(html, input);
                }
                else
                {
                    m_extractor.switchExtractor(html, input);
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_CSS_ERROR,
                        "Embedded CSS STYLE parse exception in line "
                                + m_extractor.getParser().getCurrentLine()
                                + " column "
                                + m_extractor.getParser().getCurrentColumn()
                                + ":\n" + e.toString());
            }
        }

        return "";
    }

    protected String switchToXml(String p_strXml, boolean p_embedded)
            throws ExtractorException
    {
        int xml = -1;
        int html = -1;

        if (p_strXml != null && p_strXml.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((xml = er.getFormatId(ExtractorRegistry.FORMAT_XML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strXml);
            input.setLocale(m_input.getLocale());
            input.setType(xml);

            try
            {
                if (p_embedded)
                {
                    return m_extractor.switchExtractorEmbedded(html, input);
                }
                else
                {
                    m_extractor.switchExtractor(html, input);
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_XML_ERROR,
                        "Embedded XML parse exception in line "
                                + m_extractor.getParser().getCurrentLine()
                                + " column "
                                + m_extractor.getParser().getCurrentColumn()
                                + ":\n" + e.toString());
            }
        }

        return "";
    }

    protected String switchToCFScript(String p_strCode, boolean p_embedded)
            throws ExtractorException
    {
        int cfscript = -1;
        int html = -1;
        String result = "";

        if (p_strCode != null && p_strCode.length() > 0)
        {
            ExtractorRegistry er = ExtractorRegistry.getObject();

            if ((cfscript = er.getFormatId(ExtractorRegistry.FORMAT_CFSCRIPT)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_HTML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strCode);
            input.setLocale(m_input.getLocale());
            input.setType(cfscript);

            try
            {
                if (p_embedded)
                {
                    result = m_extractor.switchExtractorEmbedded(html, input);
                }
                else
                {
                    m_extractor.switchExtractor(html, input);
                }
            }
            catch (ExtractorException e)
            {
                throw new ExtractorException(HTML_EMBEDDED_CF_ERROR,
                        "Embedded CFScript parse exception between " + "("
                                + m_iEmbeddedLine + ":" + m_iEmbeddedCol + ") "
                                + "and ("
                                + m_extractor.getParser().getCurrentLine()
                                + ":"
                                + m_extractor.getParser().getCurrentColumn()
                                + "):\n" + e.toString());
            }
        }

        return result;
    }

    protected void switchToVML(String p_vml) throws ExtractorException
    {
        // Remove the conditionals "[if gte vml 1]>" and "<![endif]".
        String before = p_vml.substring(0, 15);
        String after = p_vml.substring(p_vml.length() - 9);
        String vml = p_vml.substring(15, p_vml.length() - 9);

        Output output = m_extractor.switchExtractor(vml,
                m_extractor.getMainFormat());

        // No exception, output the results.
        m_output.addSkeleton(before);

        for (Iterator it = output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement element = (DocumentElement) it.next();
            m_output.addDocumentElement(element);
        }

        m_output.addSkeleton(after);
    }

    /**
     * Fri Dec 19 00:33:46 2003 Office Additions: determines if an HTML comment
     * contains VML instructions (for text boxes and other graphics).
     */
    private boolean isVmlComment(String p_text)
    {
        if (p_text.startsWith("[if gte vml 1]>")
                && p_text.endsWith("<![endif]"))
        {
            return true;
        }

        return false;
    }

    /**
     * Tue Oct 29 22:57:11 2002 Office Additions: detect series of nbsp that
     * Word converted from tabs during conversion to HTML and output the
     * enclosing span as PH. See GSDEF00008101.
     * 
     * Looks like <span style='mso-tab-count:1'> </span>.
     */
    private boolean isMsoTabCountSpan(HtmlObjects.Tag t)
    {
        if (t.isPaired
                && !t.isIsolated
                && t.tag.equalsIgnoreCase("SPAN")
                && t.attributes.isDefined("style")
                && t.attributes.getValue("style").startsWith("mso-tab-count:",
                        1))
        {
            // also needs to check that content consists of only &nbsp;
            return true;
        }

        return false;
    }

    /**
     * 
     * Looks like <span style='mso-special-character:comment'>&nbsp;</span>
     */
    private boolean isMsoSpecailChar(HtmlObjects.Tag t, String ttype)
    {
        if (t.isPaired
                && !t.isIsolated
                && t.tag.equalsIgnoreCase("SPAN")
                && t.attributes.isDefined("style")
                && t.attributes.getValue("style").startsWith(
                        "mso-special-character:", 1)
                && t.attributes.getValue("style").contains(":" + ttype))
        {
            return true;
        }

        return false;
    }

    /**
     * Thu Nov 14 00:44:48 2002 Office Additions: detect series of nbsp that
     * Word converted from tabs during conversion to HTML and output the
     * enclosing span as PH. See GSDEF00008101.
     * 
     * Looks like <span style="mso-spacerun:yes"> </span>.
     */
    private boolean isMsoSpaceRun(HtmlObjects.Tag t)
    {
        if (t.isPaired
                && !t.isIsolated
                && t.tag.equalsIgnoreCase("SPAN")
                && t.attributes.isDefined("style")
                && t.attributes.getValue("style")
                        .startsWith("mso-spacerun:", 1))
        {
            return true;
        }

        return false;
    }

    /**
     * Tue Jun 17 23:02:11 2003 Office Additions: detect spans in Symbol font
     * that Word didn't convert to Unicode characters and output the enclosing
     * span as PH. See GSDEF00009445.
     * 
     */
    private boolean isMsoSymbolRun(HtmlObjects.Tag t)
    {
        if (t.isPaired
                && !t.isIsolated
                && t.tag.equalsIgnoreCase("SPAN")
                && t.attributes.isDefined("style")
                && t.attributes.getValue("style").startsWith(
                        "font-family:Symbol", 1)
                && t.attributes.getValue("style").endsWith(
                        "mso-symbol-font-family:Symbol'"))
        {
            return true;
        }

        return false;
    }

    /**
     * Wed Apr 06 20:14:49 2005 Office Additions: footnote and endnote markers
     * get output as a single PH.
     * 
     * looks like <a style='mso-footnote-id:ftn1' href="#_ftn1" name="_ftnref1"
     * title=""><span class=MsoFootnoteReference><span
     * style='mso-special-character:footnote'></span></span></a>
     */
    private boolean isMsoReferenceMarker(HtmlObjects.Tag t)
    {
        if (t.isPaired
                && !t.isIsolated
                && t.tag.equalsIgnoreCase("A")
                && t.attributes.isDefined("style")
                && (t.attributes.getValue("style").startsWith(
                        "mso-footnote-id", 1) || t.attributes.getValue("style")
                        .startsWith("mso-endnote-id", 1)))
        {
            return true;
        }

        return false;
    }

    /** Simply a </A>. */
    private boolean isMsoEndReference(HtmlObjects.EndTag t)
    {
        return t.tag.equalsIgnoreCase("a");
    }

    /**
     * Hidden text appears as a span with display set to none.
     * 
     * Example: <span style='display:none;mso-hide:all'>HIDDEN Text</span>
     */
    private boolean isMsoHiddenText(HtmlObjects.Tag t)
    {
        if (t.isPaired
                && !t.isIsolated
                && t.tag.equalsIgnoreCase("SPAN")
                && t.attributes.isDefined("style")
                && (t.attributes.getValue("style").indexOf("display:none") >= 0))
        {
            return true;
        }

        return false;
    }

    /**
     * Thu Nov 14 00:44:48 2002 Office Additions: detect the
     * 
     * Looks like <TD x:str="'text with apos">text without apos</TD>.
     */
    private boolean isExcelStringCell(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("TD") && t.attributes.isDefined("x:str"))
        {
            return true;
        }

        return false;
    }

    /**
     * Thu Nov 14 00:44:48 2002 Office Additions: detect an Excel number cell.
     * Number cells are used for dates, currencies, scientific numbers,
     * accounting numbers, percentages etc.
     * 
     * Looks like: <TD x:num>number</TD>. <TD style="s1" x:num="123.45">$ 123.45
     * </TD>. <TD style="s2" x:num="32760">01-Jan-02</TD>. <TD style="s3" x:num="123.45">
     * 1.23E2</TD>. <TD style="s3" x:num x:fmla="=A1+A2">123</TD>.
     */
    private boolean isExcelNumberCell(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("TD") && t.attributes.isDefined("x:num"))
        {
            return true;
        }

        return false;
    }

    private boolean isExcelFormulaCell(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("TD") && t.attributes.isDefined("x:num")
                && t.attributes.isDefined("x:fmla")
                && t.attributes.hasValue("x:fmla"))
        {
            return true;
        }

        return false;
    }

    private void pushDeclarationContext(String p_decl)
    {
        m_declarationStack.push(new DeclarationContext(p_decl, m_bExtracting,
                m_strSkippedText));
    }

    private void popDeclarationContext()
    {
        if (m_declarationStack.size() > 0)
        {
            DeclarationContext ctxt = (DeclarationContext) m_declarationStack
                    .pop();

            m_bExtracting = ctxt.m_bExtracting;
            m_strSkippedText = ctxt.m_strSkippedText;
        }
    }

    /**
     * Discardable conditional declarations like <![if !supportEndnotes]> are
     * those whose content can be entirely discarded without changing the
     * semantics of the document to Word.
     */
    private boolean isInsideDiscardableDecl()
    {
        // Stack isa Vector

        for (int i = m_declarationStack.size() - 1; i >= 0; --i)
        {
            DeclarationContext ctxt = (DeclarationContext) m_declarationStack
                    .get(i);

            if (isDiscardableDecl(ctxt.m_context))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Discardable conditional declarations like <![if !supportEndnotes]> are
     * those whose content can be entirely discarded without changing the
     * semantics of the document to Word.
     */
    private boolean isDiscardableDecl(String p_decl)
    {
        if (p_decl.startsWith("<![if !supportFootnotes")
                || p_decl.startsWith("<![if !supportEndnotes"))
        {
            return true;
        }

        return false;
    }

    /**
     * Checks the style of a Word span against a user-configured list of
     * non-translatable character style names.
     */
    private boolean isUntranslatableSpan(HtmlObjects.Tag t)
    {
        if (t.isPaired && !t.isIsolated && t.tag.equalsIgnoreCase("span")
                && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");

            return !m_rules.canExtractWordCharStyle(clazz);
        }

        return false;
    }

    private boolean isUntranslatableStart(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("span") && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");

            return !m_rules.canExtractWordCharStyle(clazz);
        }

        return false;
    }

    private void setUntranslatableContext(HtmlObjects.Tag t)
    {
        if (t != null)
        {
            m_untranslatableContext.push(t.tag);
        }
    }

    private boolean isUntranslatableEnd(HtmlObjects.EndTag t)
    {
        if (isInsideUntranslatableContext())
        {
            String tag = (String) m_untranslatableContext.peek();
            return tag.equalsIgnoreCase(t.tag);
        }
        else
        {
            return false;
        }
    }

    private boolean isInsideUntranslatableContext()
    {
        return m_untranslatableContext.size() > 0;
    }

    private boolean isInternalStyleStart(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("span") && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");

            return m_rules.isInternalTextCharStyle(clazz);
        }

        return false;
    }

    private void setInternalStyleContext(HtmlObjects.Tag t)
    {
        if (t != null)
        {
            m_internalStyleContext.push(t.tag);
        }
    }

    private boolean isInternalStyleEnd(HtmlObjects.EndTag t)
    {
        if (isInsideInternalStyleContext())
        {
            String tag = (String) m_internalStyleContext.peek();
            return tag.equalsIgnoreCase(t.tag);
        }
        else
        {
            return false;
        }
    }

    private boolean isInsideInternalStyleContext()
    {
        return m_internalStyleContext.size() > 0;
    }

    private boolean isUnpairedTag(HtmlObjects.Tag t)
    {
        return m_rules.isUnpairedTag(t.tag);
    }

    /**
     * Checks the style of a Word paragraph or an Excel Cell against a
     * user-configured list of non-translatable paragraph style names.
     */
    private boolean isSkippableContextStart(HtmlObjects.Tag t)
    {
        if (isWordExtractor())
        {
            if (t.tag.equalsIgnoreCase("P") && t.attributes.isDefined("class"))
            {
                String clazz = t.attributes.getValue("class");

                return !m_rules.canExtractWordParaStyle(clazz);
            }
        }

        else if (isExcelExtractor())
        {
            if ((t.tag.equalsIgnoreCase("td") || t.tag.equalsIgnoreCase("tr"))
                    && t.attributes.isDefined("class"))
            {
                String clazz = t.attributes.getValue("class");
                String clazz2 = (String) m_excelStyle2NameMap.get(clazz);

                return !m_rules.canExtractExcelCellStyle(clazz2);
            }
        }

        return false;
    }

    private void setSkippableContext(HtmlObjects.Tag t)
    {
        if (t != null)
        {
            m_skippableContext.push(t.tag);
        }
    }

    private boolean isSkippableContextEnd(HtmlObjects.EndTag t)
    {
        String tag = (String) m_skippableContext.peek();
        return t.tag.equalsIgnoreCase(tag);
    }

    private boolean isInsideSkippableContext()
    {
        return m_skippableContext.size() > 0;
    }

    /*
     * Checks whether the rows of excel is hidden or not. eg: <tr
     * style="display:none">
     */
    private boolean isExcelHiddenRow(HtmlObjects.Tag t)
    {
        if ((t.tag.equalsIgnoreCase("td") || t.tag.equalsIgnoreCase("tr"))
                && t.attributes.isDefined("style")
                && (t.attributes.getValue("style").indexOf("display:none") >= 0))
        {
            return true;
        }

        return false;
    }

    // Checks whether the tag of excel html is col or not.
    private boolean isExcelColTag(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("col"))
        {
            return true;
        }

        return false;
    }

    /*
     * Sets the columns style in a hashmap for judgment whether the column is
     * hidden or not. In the html, <col style="display:none;....> <col span=2
     * style="display:none;....> <col style="...> <tr> <td>don't translate
     * because it's invisible</td> <td>don't translate because it's
     * invisible</td> <td>don't translate because it's invisible</td> <td>need
     * translate because it's visible</td> </tr>
     */
    private void setExcelColStyle(HtmlObjects.Tag t, int colNumber)
    {
        if (t.attributes.getValue("style").indexOf("display:none") >= 0)
        {
            this.setExcelColSpanStyle(colNumber, this.INVISIBLE, t);
        }
        else
        {
            this.setExcelColSpanStyle(colNumber, this.VISIBLE, t);
        }
    }

    /*
     * If the col attribute is <span>, it should re-count the column number.
     */
    private void setExcelColSpanStyle(int colNumber, String value,
            HtmlObjects.Tag t)
    {
        String key = "col_" + colNumber;
        this.m_excelColStyleMap.put(key, value);

        if (!"".equals(t.attributes.getValue("span"))
                && t.attributes.getValue("span") != null)
        {
            for (int i = 1; i < Integer.parseInt(t.attributes.getValue("span")); i++)
            {
                this.m_iExcelCol = ++colNumber;
                key = "col_" + colNumber;
                this.m_excelColStyleMap.put(key, value);
            }
        }
    }

    /*
     * Checks whether the excel tag is <tr> or not
     */
    private boolean isExcelTRTag(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("tr"))
        {
            return true;
        }

        return false;
    }

    /*
     * Checks whether the excel tag is <td>.
     */
    private boolean isExcelTDTag(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("td") && this.m_excelTrStart)
        {
            return true;
        }

        return false;
    }

    /*
     * Checks whether the column of excel is hidden or not.
     */
    private boolean isExcelHiddenColumn(HtmlObjects.Tag t, int columnNumber)
    {
        String key = "col_" + columnNumber;
        String value = (String) this.m_excelColStyleMap.get(key);

        if (!"".equals(t.attributes.getValue("colspan"))
                && t.attributes.getValue("colspan") != null)
        {
            int colspan = Integer.parseInt(t.attributes.getValue("colspan"));
            this.m_iExcelTDNumber = columnNumber + colspan - 1;
        }
        if (this.INVISIBLE.equalsIgnoreCase(value))
        {
            return true;
        }

        return false;
    }

    public boolean isIgnoreInvalidHtmlTags()
    {
        return ignoreInvalidHtmlTags;
    }

    public void setIgnoreInvalidHtmlTags(boolean ignoreInvalidHtmlTags)
    {
        this.ignoreInvalidHtmlTags = ignoreInvalidHtmlTags;
    }

    public String getJsFunctionText()
    {
        return jsFunctionText;
    }

    public void setJsFunctionText(String jsFunctionText)
    {
        this.jsFunctionText = jsFunctionText;
    }

    public void setHtmlInternalTags(List<HtmlInternalTag> htmlInternalTags)
    {
        this.htmlInternalTags = htmlInternalTags;
    }

    public boolean preserveAllWhite()
    {
        return m_preserveAllWhite;
    }

    public void setPreserveAllWhite(boolean p_preserveAllWhite)
    {
        this.m_preserveAllWhite = p_preserveAllWhite;
    }

    /**
     * Checkes if the TAG contains a note content from a master.
     */
    private boolean isNotesTagInMaster(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("p:notes")
                && t.attributes.isDefined("layout"))
        {
            return "\"notes\"".equals(t.attributes.getValue("layout"));
        }

        return false;
    }

    /**
     * Checkes if the DIV from notes does not need to be extracted.
     */
    private boolean isUnextractableContentFromNotes(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("div") && t.attributes.isDefined("class"))
        {
            return "O".equals(t.attributes.getValue("class"));
        }

        return false;
    }

    /**
     * Checkes if the DIV is content from master.
     */
    private boolean isContentFromMaster(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("div") && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");
            return clazz.startsWith("N") || clazz.startsWith("O");
        }

        return false;
    }

    private boolean containsDiv(HtmlObjects.Tag t)
    {
        return t.toString().startsWith("<div v:shape=")
                || t.toString().startsWith("<div class=");
    }
}
