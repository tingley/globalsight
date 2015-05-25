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
package com.globalsight.ling.docproc.extractor.sgml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.EFInputDataConstants;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.TmxTagGenerator;
import com.globalsight.ling.sgml.sgmlrules.SgmlRule;
import com.globalsight.ling.sgml.sgmlrules.SgmlRulesManager;

/**
 * <P>
 * Listens to the events from the SGML parser and populates the Output object
 * with Diplomat XML extracted from the tags and text pieces received. Switches
 * to other Extractors as necessary.
 * </P>
 *
 * <P>
 * If this extractor is called on strings inside a JavaScript context, strings
 * will be output as type "string" and not type "text" (the default).
 * </P>
 */
class ExtractionHandler implements ISgmlHandler, ISgmlConstants,
        ExtractorExceptionConstants
{
    //
    // Constants Section
    //

    // attributes of interest
    private static final String ACTION = "action";
    private static final String SRC = "src";
    private static final String HREF = "href";
    private static final String CONTENT = "content";
    private static final String NAME = "name";
    private static final String HTTP_EQUIV = "http-equiv";
    private static final String VALUE = "value";
    private static final String STYLE = "style";

    //
    // Member Variables Section
    //

    private Output m_output = null;
    private EFInputData m_input = null;
    private Extractor m_extractor = null;
    private Sgml2TmxMap m_sgml2TmxMap = null;
    private ExtractionRules m_rules = null;

    private Pattern m_entityPattern = Pattern.compile("&([^;]*?);");

    /**
     * <p>
     * List of all the candidates for extraction (i.e non-breaking tags, text,
     * comments...).
     * </P
     */
    private ArrayList m_extractionCandidates;

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

    /** The next extractor to be called. */
    private String m_strSwitchTo;

    /**
     * <p>
     * True if we are between white-space preserving tags like &lt;PRE&gt;.
     * Enforces special white-space handling.
     * </p>
     */
    private boolean m_bPreserveWhite;

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

    private XmlEntities m_sgmlDecoder = new XmlEntities();

    private int m_iEmbeddedLine;
    private int m_iEmbeddedCol;

    /** Holds the current XSP language (Javascript, VBScript, Java) */
    private int m_xspLanguage;

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
        m_sgml2TmxMap = new Sgml2TmxMap();
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
    }

    /**
     * @return <code>true</code> if this instance is an embedded extractor,
     *         <code>false</code> if it is a top-level, standalone extractor.
     */
    private boolean isEmbeddedExtractor()
    {
        return m_extractor.isEmbedded();
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
            return "An error has occured during SGML extraction.";
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
        m_strSwitchTo = "";
        m_extractionCandidates = new ArrayList();
        m_sgml2TmxMap.reset();
    }

    /**
     * Called once at the end of a document.
     */
    public void handleFinish()
    {
        try
        {
            flushText();
        }
        catch (ExtractorException e)
        {
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
        finally
        {
            // also in handleStart() - free memory here.
            m_sgml2TmxMap.reset();
            m_strSkippedText = null;
            m_strSwitchTo = null;
            m_extractionCandidates = null;
        }
    }

    /**
     * Handle an SGML comment <code>&lt;!-- --&gt;</code>.
     */
    public void handleComment(SgmlObjects.Comment c)
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
            String strTempComment = c.getComment().trim();
            // do not decode comments here...

            // Fri Sep 26 15:39:01 2003 Oracle Addition: preserve
            // comments that contain a paragraph ID (PID). We convert
            // the comment to a different object so it won't be moved
            // out of the paragraph in flushText().
            if (strTempComment.startsWith("BOLOC")
                    || strTempComment.startsWith("EOLOC"))
            {
                addToText(new SgmlObjects.PidComment(c.getComment()));
            }
            else
            {
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
                    m_bHasError = true;
                    m_strErrorMsg = e.toString();
                }

                addToText(c);
            }
        }
    }

    /**
     * Handle an SGML declaration <code>&lt;!DOCTYPE &gt;</code>, and also MS
     * Office's conditional instructions like
     * <code>&lt;![if !vml]&gt;...&lt;![endif]&gt;</code>.
     */
    public void handleDeclaration(SgmlObjects.Declaration d)
    {
        if (m_bHasError)
        {
            return;
        }

        String decl = d.toString();

        int index = decl.indexOf("PUBLIC");
        if (index > 0)
        {
            int start = decl.indexOf("\"", index);
            int end = decl.indexOf("\"", start + 1);

            String publicid = decl.substring(start + 1, end);

            // System.err.println("Found PUBLIC DTD decl " + publicid);

            SgmlRule rule = SgmlRulesManager.loadSgmlRule(publicid);

            if (rule != null)
            {
                // System.err.println("Found extraction rules for " + publicid);

                try
                {
                    m_rules.loadRules(rule);
                }
                catch (Exception ignore)
                {
                    // System.err.println("Error loading extraction rules: " +
                    // ignore);
                }
            }
        }

        if (!m_bExtracting)
        {
            skipText(decl);
        }
        else
        {
            addToText(d);
        }
    }

    /**
     * Handle an SGML processing instruction <code>&lt;?  ?&gt;</code>.
     */
    public void handlePI(SgmlObjects.PI t)
    {
        if (m_bHasError)
        {
            return;
        }

        if (!m_bExtracting)
        {
            skipText(t.toString());
        }
        else
        {
            addToText(t);
        }

        /*
         * try { flushText(); m_output.addSkeleton(t.toString()); } catch
         * (ExtractorException e) { m_bHasError = true; m_strErrorMsg =
         * e.toString(); }
         */
    }

    /**
     * Handle an SGML start tag including its attributes.
     */
    public void handleStartTag(SgmlObjects.Tag t)
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

                addTagToSkeleton(t, true);
            }
        }
        catch (ExtractorException e)
        {
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle an SGML end tag.
     */
    public void handleEndTag(SgmlObjects.EndTag t)
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

                String skippedText = m_strSkippedText.toString();
                m_strSkippedText.setLength(0);

                boolean b_script = false;

                b_script = skippedText.length() > 0
                        && !Text.isBlank(skippedText); // non-null xml

                // m_strSwitchTo set by handleStartTag(). If allowed
                // by dynamic rules, send content to XML extractor.
                if (b_script && m_strSwitchTo.equalsIgnoreCase("xml")
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
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    public void handleCFEndTag(SgmlObjects.EndTag t)
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
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle end of line characters.
     */
    public void handleNewline(SgmlObjects.Newline t)
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
                    addToText(t);
                }
            }
        }
        catch (ExtractorException e)
        {
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;script&gt;</code> tag; the script text is included
     * in the argument <code>s</code>.
     */
    public void handleScript(SgmlObjects.Script s)
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

                // if we're extracting, and if we're extracting the
                // script tag, and if there's any script to deal with,
                // and the dynamic rules allow extraction, then switch
                // to javascript
                if (!m_extractor.exclude() && m_rules.isSwitchTag(s.tag)
                        && b_script)
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
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;java&gt;</code> tag; the java text is included in
     * the argument <code>s</code>.
     */
    public void handleJava(SgmlObjects.Java s)
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
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle the <code>&lt;style&gt;</code> tag; the style text is included in
     * the argument <code>t</code>.
     */
    public void handleStyle(SgmlObjects.Style s)
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

                // if we're extracting, and if we're extracting the
                // style tag, and if this stylesheet is a CSS
                // stylesheet, and the dynamic rules allow extraction,
                // and if there's any style to deal with, then switch
                // to CSS
                if (!m_extractor.exclude() && m_rules.isSwitchTag(s.tag)
                        && isCSSStyleSheet(s) && b_style)
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
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }

    /**
     * Handle text (#PCDATA).
     */
    public void handleText(SgmlObjects.Text t)
    {
        if (!m_bExtracting)
        {
            skipText(t.toString()); // pass as is to other extractors
        }
        else
        {
            addToText(t);
        }
    }

    //
    // Private and Protected Method Section
    //

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
     *            : the tag the attribute belongs to (from SgmlObjects)
     * @param attrib
     *            : the attribute to ouput to the skeleton
     * @param decode
     *            : indicates whether entities in the attribute need to be
     *            decoded when outputting as trans/loc.
     */
    protected void addAttributeToSkeleton(SgmlObjects.Tag t,
            SgmlObjects.Attribute attrib, boolean decode)
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

        // Proceed with normal handling of attributes: decode if
        // requested, then examine the type and value.

        if (decode)
        {
            decodeEntities(attrib);
            strValue = Text.removeQuotes(attrib.value);
        }

        // A normal, inconspicuous attribute, handle the HTML way.

        String quote = Text.getQuoteCharacter(attrib.value);

        /*
         * This is SGML, not HTML... // Special cases first: <tag
         * href="javascript:..."> if ((attrib.name.equalsIgnoreCase(HREF) ||
         * attrib.name.equalsIgnoreCase(ACTION)) &&
         * strValue.toLowerCase().startsWith("javascript:")) {
         * m_output.addSkeleton(" " + attrib.name + "=" + quote);
         * m_output.addSkeleton("javascript:");
         * 
         * // this is a URL, needs to be decoded String temp =
         * decodeUrl(strValue.substring(11)); switchToJavaScript(temp, false);
         * 
         * m_output.addSkeleton(quote); }
         * 
         * // Style Attribute: <tag style="..."> else if
         * (attrib.name.equalsIgnoreCase(STYLE)) { m_output.addSkeleton(" " +
         * attrib.name + "=" + quote); switchToStyles(strValue, false);
         * m_output.addSkeleton(quote); }
         */

        // Localizable Attributes
        /* else */if (m_rules.isLocalizableAttribute(t.tag, attrib.name))
        {
            String strAttributeType = m_rules.getLocalizableAttribType(t.tag,
                    attrib.name);

            m_output.addSkeleton(" " + attrib.name + "=" + quote);
            m_output.addLocalizable(strValue);

            try
            {
                m_output.setLocalizableAttrs(ExtractorRegistry.FORMAT_SGML,
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
            // disregards attribute decoding done above
            m_output.addSkeleton(" " + attrib.toString());
        }
    }

    protected void addAttributeToSkeleton(SgmlObjects.Tag t,
            SgmlObjects.SimpleTag embeddedTag, boolean decode)
            throws ExtractorException
    {
        // embedded tags are in attribute position, so output a space
        m_output.addSkeleton(" ");
        m_output.addSkeleton(embeddedTag.toString());
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
    protected void addTagToSkeleton(SgmlObjects.Tag t, boolean decode)
            throws ExtractorException
    {
        // all other tags
        m_output.addSkeleton("<" + t.tag);

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            Object o = it.next();

            if (o instanceof SgmlObjects.Attribute)
            {
                addAttributeToSkeleton(t, (SgmlObjects.Attribute) o, decode);
            }
            else if (o instanceof SgmlObjects.SimpleTag)
            {
                addAttributeToSkeleton(t, (SgmlObjects.SimpleTag) o, false /* decode */);
            }
            else if (o instanceof SgmlObjects.EndTag)
            {
                SgmlObjects.EndTag e = (SgmlObjects.EndTag) o;

                m_output.addSkeleton(e.toString());
            }
        }

        if (t.isClosed)
        {
            m_output.addSkeleton("/>");
        }
        else
        {
            m_output.addSkeleton(">");
        }
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
    protected void addCommentToSkeleton(SgmlObjects.Comment t)
            throws ExtractorException
    {
        // normal comment, handle normally (don't decode)
        m_output.addSkeleton(t.toString());
    }

    /**
     * <p>
     * Outputs a comment as placeholder to a translatable section.
     * </p>
     */
    protected void addCommentToTranslatable(SgmlObjects.Comment t)
            throws ExtractorException
    {
        TmxTagGenerator tg = m_sgml2TmxMap
                .getPlaceholderTmxTag("comment", true);

        // normal comment, handle normally
        m_output.addTranslatableTmx(tg.getStart());
        m_output.addTranslatable(t.toString());
        m_output.addTranslatableTmx(tg.getEnd());
    }

    /**
     * <p>
     * Adds the current tag or text to the list of extraction candidates.
     * </p>
     */
    protected void addToText(Object p_elt)
    {
        // decode embedded entities inside HTML text nodes
        // (SCRIPT and STYLE content does not arrive as Text node)
        if (p_elt instanceof SgmlObjects.Text)
        {
            SgmlObjects.Text tag = (SgmlObjects.Text) p_elt;

            // CvdL: Thu Jul 29 18:25:17 2004 cannot decode entities:
            // "example entity: &amp;amp;"
            // tag.text = m_sgmlDecoder.decodeString(tag.text, null);

            // Whitespace handling is getting a nightmare: when in
            // documents converted from Word (and maybe other Office
            // formats), treat nbsp as whitespace. Otherwise, let the
            // system-wide configuration in Diplomat.properties guide
            // the working of Text.isBlank().
            if (!m_bContainsText)
            {
                if (true /* isWordExtractor() */)
                {
                    m_bContainsText = !Text.isBlankOrNbsp(tag.text);
                }
                else
                {
                    m_bContainsText = !Text.isBlank(tag.text);
                }
            }
        }
        // decode embedded entities inside SGML attributes
        else if (p_elt instanceof SgmlObjects.Tag)
        {
            SgmlObjects.Tag tag = (SgmlObjects.Tag) p_elt;

            for (Iterator it = tag.attributes.iterator(); it.hasNext();)
            {
                Object o = it.next();

                if (o instanceof SgmlObjects.Attribute)
                {
                    SgmlObjects.Attribute attr = (SgmlObjects.Attribute) o;
                    decodeEntities(attr);
                }
            }
        }

        m_extractionCandidates.add(p_elt);
    }

    /**
     * <p>
     * Walk through a segment and mark each pairable tag without buddy as
     * isolated. The tags' boolean members m_bPaired and m_bIsolated are false
     * by default.
     */
    protected void assignPairingStatus(ArrayList p_segment)
    {
        ArrayList tags = new ArrayList(p_segment);
        Object o1, o2;
        int i_start, i_end, i_max;
        int i_level, i_partner = 1;
        SgmlObjects.Tag t_start, t_tag;
        SgmlObjects.EndTag t_end;

        i_start = 0;
        i_max = tags.size();
        outer: while (i_start < i_max)
        {
            o1 = tags.get(i_start);

            if (o1 instanceof SgmlObjects.Tag)
            {
                t_start = (SgmlObjects.Tag) o1;

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

                    if (o2 instanceof SgmlObjects.Tag)
                    {
                        t_tag = (SgmlObjects.Tag) o2;

                        if (t_start.tag.equalsIgnoreCase(t_tag.tag))
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof SgmlObjects.EndTag)
                    {
                        t_end = (SgmlObjects.EndTag) o2;

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
                if (m_rules.isPairedTag(t_start.tag))
                {
                    t_start.isIsolated = true;
                }

                // done with this tag, don't consider again
                tags.remove(i_start);
                --i_max;
                continue outer;
            }
            else if (!(o1 instanceof SgmlObjects.EndTag))
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
            SgmlObjects.SgmlElement t = (SgmlObjects.SgmlElement) tags
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
    protected void flushLeadingIsolatedEndTags(ArrayList p_segments)
            throws ExtractorException
    {
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(0);

            if (o instanceof SgmlObjects.EndTag)
            {
                SgmlObjects.EndTag t = (SgmlObjects.EndTag) o;

                if (t.isIsolated && m_sgml2TmxMap.peekExternalId(t.tag) == -1)
                {
                    m_output.addSkeleton(t.toString());
                    p_segments.remove(0);
                    continue;
                }
            }
            // Fri Aug 22 00:39:14 2003 CvdL flush all isolated tags
            // for MsOffice.
            else if (o instanceof SgmlObjects.Tag)
            {
                SgmlObjects.Tag t = (SgmlObjects.Tag) o;

                if (t.isIsolated)
                {
                    addTagToSkeleton(t, false);
                    p_segments.remove(0);
                    continue;
                }
            }
            else if (o instanceof SgmlObjects.Text
                    || o instanceof SgmlObjects.Newline)
            {
                String text = o.toString();

                if (Text.isBlank(text))
                {
                    m_output.addSkeleton(text);
                    p_segments.remove(0);
                    continue;
                }
            }
            else if (o instanceof SgmlObjects.Comment)
            {
                String text = o.toString();

                m_output.addSkeleton(text);
                p_segments.remove(0);
                continue;
            }
            else if (o instanceof SgmlObjects.Declaration)
            {
                String text = o.toString();

                m_output.addSkeleton(text);
                p_segments.remove(0);
                continue;
            }
            else if (o instanceof SgmlObjects.PI)
            {
                String text = o.toString();

                m_output.addSkeleton(text);
                p_segments.remove(0);
                continue;
            }

            break;
        }
    }

    protected void flushLeadingIsolatedFontTags(ArrayList p_segments)
            throws ExtractorException
    {
        // While the first element is an isolated starting font tag,
        // flush it to the skeleton.
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(0);

            if (o instanceof SgmlObjects.Tag)
            {
                SgmlObjects.Tag t = (SgmlObjects.Tag) o;
                if (t.isIsolated && t.tag.equalsIgnoreCase("FONT"))
                {
                    addTagToSkeleton(t, false);
                    p_segments.remove(0);
                    continue;
                }
            }
            else if (o instanceof SgmlObjects.Text
                    || o instanceof SgmlObjects.Newline)
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
    protected void removeTrailingIsolatedEndTags(ArrayList p_segments,
            ArrayList p_after)
    {
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(p_segments.size() - 1);

            if (o instanceof SgmlObjects.Text
                    || o instanceof SgmlObjects.Newline)
            {
                if (Text.isBlank(o.toString()))
                {
                    p_after.add(0, p_segments.remove(p_segments.size() - 1));
                    continue;
                }
            }
            else if (o instanceof SgmlObjects.EndTag)
            {
                SgmlObjects.EndTag t = (SgmlObjects.EndTag) o;

                if (t.isIsolated && m_sgml2TmxMap.peekExternalId(t.tag) == -1)
                {
                    p_after.add(0, p_segments.remove(p_segments.size() - 1));
                    continue;
                }
            }
            // Fri Aug 22 00:39:14 2003 CvdL flush all isolated tags
            // for MsOffice (see also the isMsOfficeExtractor() clause
            // above.
            else if (o instanceof SgmlObjects.Tag)
            {
                SgmlObjects.Tag t = (SgmlObjects.Tag) o;

                if (t.isIsolated)
                {
                    p_after.add(0, p_segments.remove(p_segments.size() - 1));
                    continue;
                }
            }

            break;
        }
    }

    /**
     * Flush all tags passed in to the skeleton. The argument list is
     * destructively modified (i.e., cleared).
     */
    protected void flushLeftOverTags(ArrayList p_segments)
    {
        while (p_segments.size() > 0)
        {
            Object o = p_segments.remove(0);

            m_output.addSkeleton(o.toString());
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
        if (m_bContainsText && !m_extractor.exclude())
        {
            ArrayList tagsBefore = new ArrayList();
            ArrayList tagsAfter = new ArrayList();

            // For each segment in the list: assign tag status
            // (paired, isolated).
            assignPairingStatus(m_extractionCandidates);

            // Get rid of leading isolated end tags that may have
            // their counterpart in other parts of the skeleton.
            flushLeadingIsolatedEndTags(m_extractionCandidates);

            // FONT HACK: flush leading isolated font tags to the
            // skeleton, since they most likely have their counterpart
            // in the skeleton.
            flushLeadingIsolatedFontTags(m_extractionCandidates);

            // Fetch the trailing isolated stuff, too, and save it for
            // later.
            removeTrailingIsolatedEndTags(m_extractionCandidates, tagsAfter);

            // flush the segment
            flushTextToTranslatable(m_extractionCandidates);

            // flush any stuff left at the end of the segment
            flushLeftOverTags(tagsAfter);
        }
        else
        {
            flushTextToSkeleton(m_extractionCandidates);
        }

        m_bContainsText = false;
        m_extractionCandidates.clear();
    }

    /**
     * <p>
     * Helper method for flushText(): flushes text and tags to a TMX skeleton
     * section.
     * </p>
     */
    protected void flushTextToSkeleton(ArrayList p_elements)
            throws ExtractorException
    {
        for (Iterator it = p_elements.iterator(); it.hasNext();)
        {
            Object o = it.next();

            if (o instanceof SgmlObjects.Tag)
            {
                addTagToSkeleton((SgmlObjects.Tag) o, false);
            }
            else if (o instanceof SgmlObjects.PidComment)
            {
                SgmlObjects.PidComment x = (SgmlObjects.PidComment) o;
                o = new SgmlObjects.Comment(x.getComment());

                addCommentToSkeleton((SgmlObjects.Comment) o);
            }
            else if (o instanceof SgmlObjects.Comment)
            {
                addCommentToSkeleton((SgmlObjects.Comment) o);
            }
            else
            {
                m_output.addSkeleton(o.toString());
            }
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
    protected void flushTextToTranslatable(ArrayList p_segments)
            throws ExtractorException
    {
        for (ListIterator it = p_segments.listIterator(); it.hasNext();)
        {
            Object o = it.next();

            if (o instanceof SgmlObjects.Text
                    || o instanceof SgmlObjects.Newline)
            {
                // Combine consecutive text and newline nodes to get
                // correct whitespace normalization.
                StringBuffer buf = new StringBuffer();
                buf.append(o.toString());
                while (it.hasNext())
                {
                    o = it.next();

                    if (o instanceof SgmlObjects.Text
                            || o instanceof SgmlObjects.Newline)
                    {
                        buf.append(o.toString());
                    }
                    else
                    {
                        it.previous();
                        break;
                    }
                }

                // Output text to the output structure.

                // TODO: the parser does not parse embedded DTD declarations
                // and outputs the trailing ]> token in <!DOCTYPE ... [ ]>
                // as a text string.

                String s = normalizeString(buf.toString());

                if (s.equals("]&gt;"))
                {
                    m_output.addSkeleton("]>");
                }
                else
                {
                    m_output.addTranslatableTmx(s);

                    // Thu Nov 30 20:45:45 2000 CvdL: XXXXX text inside
                    // javascript contexts are of type "string", but the
                    // dataformat should be "sgml"?
                    try
                    {
                        if (m_isInsideJavaScript)
                        {
                            m_output.setTranslatableAttrs(
                                    ExtractorRegistry.FORMAT_SGML, "string");
                        }
                        else
                        {
                            m_output.setTranslatableAttrs(
                                    ExtractorRegistry.FORMAT_SGML, "text");
                        }
                    }
                    catch (DocumentElementException e) // SNH
                    {
                        throw new ExtractorException(SGML_UNEXPECTED_ERROR, e);
                    }
                }
            }
            else if (o instanceof SgmlObjects.Tag)
            {
                SgmlObjects.Tag t = (SgmlObjects.Tag) o;
                TmxTagGenerator tg;

                // Handle special cases first that can change the
                // sequence of tags.

                if (t.isClosed || (!t.isPaired && !t.isIsolated))
                {
                    tg = m_sgml2TmxMap.getPlaceholderTmxTag(t.tag, true);
                }
                else
                {
                    tg = m_sgml2TmxMap.getPairedTmxTag(t, true, t.isIsolated);
                }

                flushTagToTranslatable(tg, t);
            }
            else if (o instanceof SgmlObjects.EndTag)
            {
                SgmlObjects.EndTag t = (SgmlObjects.EndTag) o;

                TmxTagGenerator tg = m_sgml2TmxMap.getPairedTmxTag(t, false,
                        t.isIsolated);

                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable(o.toString());
                m_output.addTranslatableTmx(tg.getEnd());
            }
            else if (o instanceof SgmlObjects.PidComment)
            {
                SgmlObjects.PidComment x = (SgmlObjects.PidComment) o;
                SgmlObjects.Comment c = new SgmlObjects.Comment(x.getComment());

                // PidComments are just comments that need to be
                // preserved, write out as normal comment.
                addCommentToTranslatable(c);
            }
            else if (o instanceof SgmlObjects.Comment)
            {
                SgmlObjects.Comment c = (SgmlObjects.Comment) o;

                // Handle potential SSI comments separately
                addCommentToTranslatable(c);
            }
            else if (o instanceof SgmlObjects.Declaration)
            {
                TmxTagGenerator tg = m_sgml2TmxMap.getPlaceholderTmxTag(
                        "declaration", true);

                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable(o.toString());
                m_output.addTranslatableTmx(tg.getEnd());
            }
            else if (o instanceof SgmlObjects.PI)
            {
                TmxTagGenerator tg = m_sgml2TmxMap.getPlaceholderTmxTag("pi",
                        true);

                m_output.addTranslatableTmx(tg.getStart());
                m_output.addTranslatable(o.toString());
                m_output.addTranslatableTmx(tg.getEnd());
            }
        }

        m_sgml2TmxMap.resetCounter();
    }

    /**
     * <p>
     * Flushes a single tag as part of flushing out the list of potential
     * extraction candidates.
     *
     * <p>
     * Called from flushTextToTranslatable().
     */
    private void flushTagToTranslatable(TmxTagGenerator tg, SgmlObjects.Tag t)
            throws ExtractorException
    {
        m_output.addTranslatableTmx(tg.getStart());
        m_output.addTranslatable("<" + t.tag);

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            // Note: Attributes may have been decoded.

            Object o = it.next();

            if (o instanceof SgmlObjects.Attribute)
            {
                SgmlObjects.Attribute attrib = (SgmlObjects.Attribute) o;
                String strValue = Text.removeQuotes(attrib.value);

                // Value-less Attribute - quick exit
                if (strValue == null || strValue.length() == 0
                        || Text.isBlank(strValue))
                {
                    m_output.addTranslatable(" " + attrib.toString());
                    continue;
                }

                String quote = Text.getQuoteCharacter(attrib.value);

                /*
                 * This is SGML, not HTML... // Special case first: <A
                 * href="javascript:..."> if
                 * ((attrib.name.equalsIgnoreCase(HREF) ||
                 * attrib.name.equalsIgnoreCase(ACTION)) &&
                 * strValue.toLowerCase().startsWith("javascript:")) {
                 * m_output.addTranslatable(" " + attrib.name + "=" + quote);
                 * m_output.addTranslatable("javascript:");
                 * 
                 * // this is a URL, needs to be decoded String temp =
                 * decodeUrl(strValue.substring(11));
                 * 
                 * m_output.addTranslatableTmx(switchToJavaScript(temp, true));
                 * m_output.addTranslatable(quote); }
                 * 
                 * // Style Attribute: <tag style="..."> else if
                 * (attrib.name.equalsIgnoreCase(STYLE)) {
                 * m_output.addTranslatable(" " + attrib.name + "=" + quote);
                 * m_output.addTranslatableTmx( switchToStyles(strValue, true));
                 * m_output.addTranslatable(quote); }
                 */

                // Localizable Attribute
                /* else */if (m_rules
                        .isLocalizableAttribute(t.tag, attrib.name))
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
                else if (m_rules.isTranslatableAttribute(t.tag, attrib.name)
                        && !m_extractor.exclude())
                {
                    // all urls are localizable, nothing to decode here

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
            // Not an attribute but something else (a CFTag in HTML?)
            else
            {
                m_output.addTranslatable(" " + o.toString());
            }
        }

        if (t.isClosed)
        {
            m_output.addTranslatable("/>");
        }
        else
        {
            m_output.addTranslatable(">");
        }

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

    protected int getScriptLanguage(SgmlObjects.Script t)
    {
        int result = EC_JAVASCRIPT;

        for (Iterator it = t.attributes.iterator(); it.hasNext();)
        {
            SgmlObjects.Attribute attr = (SgmlObjects.Attribute) it.next();

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
        if (!m_bPreserveWhite)
        {
            // Fri Apr 12 16:55:59 2002 CvdL: this was written as a
            // no-op. Unless somebody tells me this was indeed a
            // feature, we do remove whitespace.
            p_text = Text.normalizeWhiteSpaces(p_text);
        }

        // For SGML we need to mark up user-defined entities and
        // preserve well-known SGML (XML) entities.

        StringBuffer result = new StringBuffer();
        int index = 0;

        Matcher matcher = m_entityPattern.matcher(p_text);
        while (matcher.find(index))
        {
            String entity = matcher.group();
            String entityName = matcher.group(1);
            String entityChar = null;

            result.append(m_sgmlDecoder.encodeStringBasic(p_text.substring(
                    index, matcher.start())));

            if (entityName.equals("amp") || entityName.equals("apos")
                    || entityName.equals("quot") || entityName.equals("lt")
                    || entityName.equals("gt"))
            {
                result.append(entity);
            }
            else if ((entityChar = mapEntityToChar(entityName)) != null)
            {
                result.append(entityChar);
            }
            else
            {
                result.append("<ph type=\"entity-" + entityName + "\">"
                        + m_sgmlDecoder.encodeStringBasic(entity) + "</ph>");
            }

            index = matcher.end();
        }

        result.append(m_sgmlDecoder.encodeStringBasic(p_text.substring(index)));

        return result.toString();
    }

    private String mapEntityToChar(String p_entity)
    {
        String result = null;

        if (m_rules.isEntity(p_entity))
        {
            String value = m_rules.getEntityValue(p_entity);

            if (value.length() == 1)
            {
                // Entity mapped to single character, use it.
                result = value;
            }
            else if (m_rules.isSystemEntity(value))
            {
                result = m_rules.getSystemEntityValue(value);
            }
        }

        return result;
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
    private void decodeEntities(SgmlObjects.Attribute attr)
    {
        String text = attr.value;

        if (text != null)
        {
            // Could add &apos; to the recognized entities.
            text = m_sgmlDecoder.decodeString(text, null);
            attr.value = text;
        }
    }

    /**
     * Tests the style tag if it has a src attribute with value "text/css".
     * 
     * @returns <code>true</code> if src="text/css" or not present. Returns
     *          <code>false</code> if src is present but has any other value.
     */
    protected boolean isCSSStyleSheet(SgmlObjects.Style s)
    {
        boolean b_res = true;

        for (Iterator it = s.attributes.iterator(); it.hasNext();)
        {
            SgmlObjects.Attribute attrib = (SgmlObjects.Attribute) it.next();

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

    protected String switchToJavaScript(String p_strJSCode, boolean p_Embedded)
            throws ExtractorException
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

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_SGML)) == -1)
            {
                throw new ExtractorException(FORMAT_NOT_REGISTERED);
            }

            EFInputData input = new EFInputData();
            input.setUnicodeInput(p_strJSCode);
            input.setLocale(m_input.getLocale());
            input.setType(javascript);

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
                throw new ExtractorException(SGML_EMBEDDED_JS_ERROR,
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

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_SGML)) == -1)
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
                throw new ExtractorException(SGML_EMBEDDED_VB_ERROR,
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

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_SGML)) == -1)
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
                throw new ExtractorException(SGML_EMBEDDED_JAVA_ERROR,
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

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_SGML)) == -1)
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
                throw new ExtractorException(SGML_EMBEDDED_CSS_ERROR,
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

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_SGML)) == -1)
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
                throw new ExtractorException(SGML_EMBEDDED_CSS_ERROR,
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

            if ((html = er.getFormatId(ExtractorRegistry.FORMAT_SGML)) == -1)
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
                throw new ExtractorException(SGML_EMBEDDED_XML_ERROR,
                        "Embedded XML parse exception in line "
                                + m_extractor.getParser().getCurrentLine()
                                + " column "
                                + m_extractor.getParser().getCurrentColumn()
                                + ":\n" + e.toString());
            }
        }

        return "";
    }

    private void debugTags(ArrayList p_tags)
    {
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < p_tags.size(); i++)
        {
            System.err.println(p_tags.get(i));
        }
    }
}
