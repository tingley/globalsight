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

package com.globalsight.everest.tm.util.trados;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.html.HtmlObjects;
import com.globalsight.ling.docproc.extractor.html.IHtmlHandler;
import com.globalsight.ling.docproc.extractor.html.ParseException;
import com.globalsight.ling.docproc.extractor.html.Parser;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * <p>
 * Trados-TMX Converter for RTF.
 * </p>
 *
 * <p>
 * Use TradosTmxToRtf to read a Trados TMX file and write it out as RTF that
 * Word can read. When the file is saved from within Word as HTML, use
 * StylesheetRemover to remove the possibly very large stylesheet, and then use
 * this class to parse the HTML, extract segments, and write the result as GXML
 * TMX.
 * </p>
 */
public class WordHtmlToTmx implements IHtmlHandler
{
    private Logger m_logger = null;

    private PrintWriter m_writer;

    private String m_version = "";
    private Tmx m_header = null;
    private String m_filename;

    private HtmlEntities m_htmlDecoder = new HtmlEntities();
    private DiplomatAPI m_diplomat = new DiplomatAPI();
    private Parser m_parser;
    private int m_entryCount = 0;
    private int m_ignoreCount = 0;
    private int m_errorCount = 0;
    private boolean m_skipping = true;
    private ArrayList m_tags = new ArrayList();
    private String m_html = null;
    private String m_tmx = null;

    // A hack: I can't get rid of two TAB characters showing up at the
    // beginning of the RTF doc loaded into Word. Word then writes out
    // two <span mso-tab-counts> which we have to get rid of.
    private boolean m_firstParagraph = true;

    static private ArrayList s_EMPTYSEGMENTS = new ArrayList();

    private static final REProgram SEG_SEARCH_PATTERN = createSearchPattern("<seg>(.*?)</seg>");

    private static final REProgram B_BIDI_BOLD_SEARCH_PATTERN = createSearchPattern("<b([:space:])+?style=\'mso-bidi-font-weight:[^>]*?>");
    private static final REProgram B_BIDI_ITALIC_SEARCH_PATTERN = createSearchPattern("<i([:space:])+?style=\'mso-bidi-font-style:[^>]*?>");

    private static REProgram createSearchPattern(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException e)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(e.getMessage());
        }

        return pattern;
    }

    //
    // Constructors
    //

    public WordHtmlToTmx()
    {
    }

    public WordHtmlToTmx(Logger p_logger)
    {
        m_logger = p_logger;
    }

    //
    // Standard Helper Methods
    //

    public void debug(String p_message)
    {
        if (m_logger != null)
        {
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug(p_message);
            }
        }
        else
        {
            System.err.println(p_message);
        }
    }

    public void info(String p_message)
    {
        if (m_logger != null)
        {
            m_logger.info(p_message);
        }
        else
        {
            System.err.println(p_message);
        }
    }

    //
    // IHtmlHandler Interface Implementation
    //

    /**
     * Called once at the beginning of a new document.
     */
    public void handleStart()
    {
    }

    /**
     * Called once at the end of a document.
     */
    public void handleFinish()
    {
    }

    /**
     * Handle an HTML start tag including its attributes.
     */
    public void handleStartTag(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("P"))
        {
            m_skipping = false;
        }

        if (m_skipping)
        {
            return;
        }

        m_tags.add(t);
    }

    /**
     * Handle an HTML end tag.
     */
    public void handleEndTag(HtmlObjects.EndTag t)
    {
        if (m_skipping)
        {
            return;
        }

        m_tags.add(t);

        if (t.tag.equalsIgnoreCase("P"))
        {
            // The first paragraph is used to send TMX header
            // information our way.
            if (m_firstParagraph)
            {
                handleFirstParagraph();
            }
            else
            {
                m_entryCount++;
                handleTU();
            }

            if (m_entryCount > 0 && m_entryCount % 1000 == 0)
            {
                debug("Entry " + m_entryCount);
            }

            m_tags.clear();
            m_skipping = true;
        }
    }

    /**
     * Handle end of line characters.
     */
    public void handleNewline(HtmlObjects.Newline t)
    {
        if (m_skipping)
        {
            return;
        }

        m_tags.add(t);
    }

    /**
     * Handle text (#PCDATA).
     */
    public void handleText(HtmlObjects.Text t)
    {
        if (m_skipping)
        {
            return;
        }

        t.text = m_htmlDecoder.decodeString(t.text, null);

        m_tags.add(t);
    }

    /**
     * Handle an HTML comment <code>&lt;!-- --&gt;</code>.
     */
    public void handleComment(HtmlObjects.Comment t)
    {
    }

    /**
     * Handle an HTML declaration <code>&lt;!DOCTYPE  &gt;</code>.
     */
    public void handleDeclaration(HtmlObjects.Declaration t)
    {
    }

    /**
     * Handle an HTML processing instruction <code>&lt;?  ?&gt;</code>.
     */
    public void handlePI(HtmlObjects.PI t)
    {
    }

    /**
     * Handle a ColdFusion comment <code>&lt;!--- ---&gt;</code>.
     */
    public void handleCfComment(HtmlObjects.CfComment c)
    {
    }

    /**
     * Handle a ColdFusion start tag including its attributes.
     */
    public void handleCFStartTag(HtmlObjects.CFTag t)
    {
    }

    /**
     * Handle a ColdFusion end tag.
     */
    public void handleCFEndTag(HtmlObjects.EndTag t)
    {
    }

    /**
     * Handle a ASP/JSP tag; the script text is included in the argument
     * <code>t</code>.
     */
    public void handleXsp(HtmlObjects.Xsp t)
    {
    }

    /**
     * Handle the <code>&lt;script&gt;</code> tag; the script text is included
     * in the argument <code>t</code>.
     */
    public void handleScript(HtmlObjects.Script t)
    {
    }

    /**
     * Handle the <code>&lt;java&gt;</code> tag; the java text is included in
     * the argument <code>t</code>.
     */
    public void handleJava(HtmlObjects.Java t)
    {
    }

    /**
     * Handle the <code>&lt;style&gt;</code> tag; the style text is included in
     * the argument <code>t</code>.
     */
    public void handleStyle(HtmlObjects.Style t)
    {
    }

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag; the ColdFusion script text
     * is included in the argument <code>t</code>.
     */
    public void handleCFScript(HtmlObjects.CFScript t)
    {
    }

    /**
     * Handle the <code>&lt;CFSCRIPT&gt;</code> tag with SQL statements inside;
     * the SQL text is included in the argument <code>t</code>.
     */
    public void handleCFQuery(HtmlObjects.CFQuery t)
    {
    }

    //
    // Other Methods
    //

    public void handleFirstParagraph()
    {
        m_firstParagraph = false;

        StringBuffer para = new StringBuffer();

        for (int i = 0, max = m_tags.size(); i < max; i++)
        {
            Object o = m_tags.get(i);
            para.append(o.toString());
        }

        // debug("First paragraph = " + para + "\n");
        String temp = para.toString();

        String sourceLang = getSourceLang(temp);
        m_header.setSourceLang(sourceLang);

        m_writer.println(m_header.getHeaderXml());
        m_writer.println("<body>");

    }

    public void handleTU()
    {
        StringBuffer tu = new StringBuffer();
        ArrayList preambles = new ArrayList();
        ArrayList segments = new ArrayList();
        String postamble = null;

        try
        {
            // Remove tags inserted by Word: either <p>...</p>, or
            // <p><span>...<o:p></o:p></span></p>.
            cleanupRawTags(m_tags);

            for (int i = 0, max = m_tags.size(); i < max; i++)
            {
                Object o = m_tags.get(i);
                tu.append(o.toString());
            }

            // debug("TU = " + tu + "\n");

            String temp = tu.toString();

            // Gather data for TUVs that consist of multiple segments
            // (according to System4 tag definition and paragraph
            // segmentation). Keep data around and print it at the end.

            String pre = "";
            String post = "";
            String segment = "";

            RE re = new RE(SEG_SEARCH_PATTERN, RE.MATCH_CASEINDEPENDENT
                    | RE.MATCH_SINGLELINE);

            while (re.match(temp))
            {
                pre = temp.substring(0, re.getParenStart(0));
                segment = re.getParen(1);
                post = temp.substring(re.getParenEnd(0));
                temp = post;

                /*
                 * // Fix up peculiarities of the extraction here. Example: //
                 * <b> comes back as <b style='mso-bidi-font-weight:normal'>, //
                 * <i> comes back as <i style='mso-bidi-font-style:normal'>. //
                 * Note: The Word-HTML Extractor does this too.
                 * 
                 * //debug(segment); RE re2 = new RE(B_BIDI_BOLD_SEARCH_PATTERN,
                 * RE.MATCH_CASEINDEPENDENT | RE.MATCH_SINGLELINE); segment =
                 * re2.subst(segment, "<b>");
                 * 
                 * re2 = new RE(B_BIDI_ITALIC_SEARCH_PATTERN,
                 * RE.MATCH_CASEINDEPENDENT | RE.MATCH_SINGLELINE); segment =
                 * re2.subst(segment, "<i>"); //debug(segment);
                 */

                preambles.add(pre);

                Output output = extractSegment(segment);
                ArrayList gxmlSegments = s_EMPTYSEGMENTS;

                if (output != null)
                {
                    gxmlSegments = getSegments(output);
                }

                segments.add(gxmlSegments);

                if (false)
                {
                    debug("\tPRE=" + pre);
                    debug("\tSEG=" + segment);
                    debug("\tPOS=" + post);
                }
            }

            postamble = post;

            // Sanity check: make sure a paragraph contained a TU.
            // End notes embedded in segments appear as <div><p> at
            // the end of the document...
            if (segments.size() == 0)
            {
                m_ignoreCount++;
                return;
            }

            int maxSegs = ((ArrayList) segments.get(0)).size();

            // Sanity check 2: Loop over the System4 segments and find
            // the minimum number of segments in each <seg> that can
            // be created in this TU.
            for (int i = 0, max = preambles.size(); i < max; i++)
            {
                int numSegs = ((ArrayList) segments.get(i)).size();

                maxSegs = Math.min(maxSegs, numSegs);
            }

            // If one 1 TU contained no segments at all, skip.
            if (maxSegs == 0)
            {
                m_ignoreCount++;
                return;
            }

            // Now output multiple TUs, one for each System4 segment in
            // the original Trados TMX paragraph.
            StringBuffer toWrite = new StringBuffer();

            for (int i = 0; i < maxSegs; i++)
            {
                // Loop over TUVs in TU
                for (int j = 0, max = preambles.size(); j < max; j++)
                {
                    String preamble = (String) preambles.get(j);
                    String gxml = (String) (((ArrayList) segments.get(j))
                            .get(i));

                    toWrite.append(preamble);
                    toWrite.append("<seg>");
                    toWrite.append(gxml.trim());
                    toWrite.append("</seg>");
                }

                toWrite.append(post);
                toWrite.append("\n");
            }

            // only output if no errors
            if (toWrite.length() > 0)
            {
                writeEntry(toWrite.toString());
            }
        }
        catch (Throwable ex)
        {
            ++m_errorCount;

            // ex.printStackTrace();
            debug("Error in TU " + m_entryCount + ":\n" + tu + "\n");

            try
            {
                if (segments.size() == 0)
                {
                    debug("No segments extracted");
                }

                for (int i = 0; i < segments.size(); i++)
                {
                    ArrayList segs = (ArrayList) segments.get(i);

                    for (int j = 0; j < segs.size(); j++)
                    {
                        String gxml = (String) segs.get(j);
                        debug("Segment " + i + ":" + j + "= " + gxml);
                    }
                }
            }
            catch (Throwable ex1)
            {
                // Ignore subsequent errors and return.
            }
        }
    }

    public Output extractSegment(String p_segment)
    {
        try
        {
            m_diplomat.reset();
            m_diplomat.setSourceString(p_segment);
            m_diplomat.setInputFormat(IFormatNames.FORMAT_WORD_HTML);
            m_diplomat.setEncoding("unicode");
            m_diplomat.setLocale(Locale.US);
            m_diplomat.setSentenceSegmentation(false);

            m_diplomat.extract();

            return m_diplomat.getOutput();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            return null;
        }
    }

    /**
     * After System4 paragraph segmentation, return all translatable segments
     * (the real segments) in a list.
     */
    public ArrayList getSegments(Output p_output)
    {
        ArrayList result = new ArrayList();

        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;

                    if (elem.hasSegments())
                    {
                        ArrayList segments = elem.getSegments();

                        for (int i = 0, max = segments.size(); i < max; i++)
                        {
                            SegmentNode node = (SegmentNode) segments.get(i);
                            result.add(node.getSegment());
                        }
                    }

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    // Thu Dec 04 01:53:59 2003 CvdL: ignore localizables.
                    /*
                     * LocalizableElement elem = (LocalizableElement)de;
                     * result.add(elem.getChunk());
                     */

                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }

        return result;
    }

    /**
     * Removes tags inserted by Word: either
     * <p>
     * ...
     * </p>
     * , or
     * <p>
     * <span>...<o:p></o:p></span>
     * </p>
     * .
     *
     * The Word-HTML extractor performs the same simplifications but WE DO HAVE
     * TO MAKE THEM HERE because of Word conversion particuliarities that
     * generate "improperly" balanced tags (for our purposes, at least).
     */
    private void cleanupRawTags(ArrayList p_tags)
    {
        // Find the paired tags that belong together.
        // !!! DO NOT RELY ON PAIRING STATUS. Some prop changes start
        // after <seg> but close after </seg> and even after </tu>!!!
        assignPairingStatus(p_tags);

        // Basic cleanup: remove surrounding <p> and inner <o:p>
        removeParagraphTags(p_tags);

        // Remove revision markers when they were ON accidentally
        // before translating documents or during an alignment task
        // (WinAlign).
        removePropRevisionMarkers(p_tags);

        // Cleanup INS/DEL revisions similarly.
        removeDelRevisions(p_tags);
        applyInsRevisions(p_tags);

        // WinAligned files can contain endnotes (and footnotes, but I
        // think in Word-HTML they're both endnotes).
        removeEndNotes(p_tags);

        // Remove empty spans that are created from superfluous
        // original formatting in Word (<span color=blue></span>)
        removeEmptyFormatting(p_tags);

        // Leave the rest to the Word-HTML Extractor.
    }

    /**
     * Removes paragraph tags:
     * <p>
     * and <o:p>.
     */
    private void removeParagraphTags(ArrayList p_tags)
    {
        // outer <p>
        p_tags.remove(0);
        p_tags.remove(p_tags.size() - 1);

        // <o:p>
        boolean b_found = false;
        for (int i = 0; i < p_tags.size() - 1; i++) // loop ok
        {
            Object o = p_tags.get(i);

            if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag tag = (HtmlObjects.Tag) o;

                if (tag.tag.equalsIgnoreCase("o:p"))
                {
                    p_tags.subList(i, i + 2).clear();
                    b_found = true;
                    break;
                }
            }
        }

        // if <o:p> was output, there also was a <span> around the
        // entire paragraph content.
        if (b_found)
        {
            p_tags.remove(0);
            p_tags.remove(p_tags.size() - 1);
        }
    }

    /**
     * Removes property revisions: <span class=msoProChange> and the closing
     * tag.
     */
    private void removePropRevisionMarkers(ArrayList p_tags)
    {
        boolean b_changed = true;

        propchange: while (b_changed)
        {
            for (int i = 0, max = p_tags.size(); i < max; i++)
            {
                Object o = p_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("span")
                            && original.indexOf("class=msoChangeProp") >= 0)
                    {
                        p_tags.remove(i);

                        // Note that the closing tag for changeprop
                        // may come *after* </seg> and </tu>.
                        removeClosingTag(p_tags, tag);

                        continue propchange;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes DEL revisions: <span class=msoDel> and text up to and including
     * the closing tag.
     */
    private void removeDelRevisions(ArrayList p_tags)
    {
        boolean b_changed = true;

        deltags: while (b_changed)
        {
            for (int i = 0, max = p_tags.size(); i < max; i++)
            {
                Object o = p_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("span")
                            && original.indexOf("class=msoDel") >= 0)
                    {
                        removeTagAndContent(p_tags, tag);

                        continue deltags;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes INS revisions: <span class=msoIns> followed by <INS>, </INS> and
     * the closing </span>.
     */
    private void applyInsRevisions(ArrayList p_tags)
    {
        boolean b_changed = true;

        instags: while (b_changed)
        {
            for (int i = 0, max = p_tags.size(); i < max; i++)
            {
                Object o = p_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("span")
                            && original.indexOf("class=msoIns") >= 0)
                    {
                        removeInsTag(p_tags, tag);

                        continue instags;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes endnotes.
     */
    private void removeEndNotes(ArrayList p_tags)
    {
        boolean b_changed = true;

        endnotes: while (b_changed)
        {
            for (int i = 0, max = p_tags.size() - 1; i < max; i++)
            {
                Object o = p_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("A")
                            && original.indexOf("style='mso-endnote-id:") > 0)
                    {
                        removeTagAndContent(p_tags, tag);
                        continue endnotes;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes INS revisions: <span class=msoIns> followed by <INS>, </INS> and
     * the closing </span>.
     */
    private void removeEmptyFormatting(ArrayList p_tags)
    {
        boolean b_changed = true;

        emptytag: while (b_changed)
        {
            for (int i = 0, max = p_tags.size() - 1; i < max; i++)
            {
                Object o1 = p_tags.get(i);
                Object o2 = p_tags.get(i + 1);

                if (o1 instanceof HtmlObjects.Tag
                        && o2 instanceof HtmlObjects.EndTag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o1;
                    HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o2;

                    if (tag.tag.equalsIgnoreCase(etag.tag)
                            && tag.partnerId == etag.partnerId)
                    {
                        p_tags.remove(i + 1);
                        p_tags.remove(i);

                        continue emptytag;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes the closing tag for the given HtmlObjects.Tag as determined by
     * the tag's partner id.
     */
    private void removeClosingTag(ArrayList p_tags, HtmlObjects.Tag p_tag)
    {
        for (int i = 0, max = p_tags.size(); i < max; i++)
        {
            Object o = p_tags.get(i);

            if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o;

                if (p_tag.tag.equalsIgnoreCase(etag.tag)
                        && p_tag.partnerId == etag.partnerId)
                {
                    p_tags.remove(i);
                    return;
                }
            }
        }
    }

    /**
     * Removes a Word insert revision by removing a starting <span
     * class=msoIns><ins> tag + counterparts, and leaving the contents in place.
     */
    private void removeInsTag(ArrayList p_tags, HtmlObjects.Tag p_tag)
    {
        int i_start = 0;
        int i_end = 0;

        loop: for (int i = 0, max = p_tags.size(); i < max; i++)
        {
            Object o = p_tags.get(i);

            if (o == p_tag)
            {
                i_start = i;

                for (int j = i + 1; j < max; j++)
                {
                    Object o1 = p_tags.get(j);

                    if (o1 instanceof HtmlObjects.EndTag)
                    {
                        HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o1;

                        if (p_tag.tag.equalsIgnoreCase(etag.tag)
                                && p_tag.partnerId == etag.partnerId)
                        {
                            i_end = j;
                            break loop;
                        }
                    }
                }
            }
        }

        if (i_start >= 0 && i_end > i_start)
        {
            p_tags.subList(i_end - 1, i_end + 1).clear();
            p_tags.subList(i_start, i_start + 2).clear();
        }
    }

    /**
     * Removes a tag and its content between the endtag (which must exist and be
     * paired with the start).
     */
    private void removeTagAndContent(ArrayList p_tags, HtmlObjects.Tag p_tag)
    {
        int i_start = 0;
        int i_end = 0;

        loop: for (int i = 0, max = p_tags.size(); i < max; i++)
        {
            Object o = p_tags.get(i);

            if (o == p_tag)
            {
                i_start = i;

                for (int j = i + 1; j < max; j++)
                {
                    Object o1 = p_tags.get(j);

                    if (o1 instanceof HtmlObjects.EndTag)
                    {
                        HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o1;

                        if (p_tag.tag.equalsIgnoreCase(etag.tag)
                                && p_tag.partnerId == etag.partnerId)
                        {
                            i_end = j;
                            break loop;
                        }
                    }
                }
            }
        }

        if (i_start >= 0 && i_end > i_start)
        {
            p_tags.subList(i_start, i_end + 1).clear();
        }
    }

    /**
     * <p>
     * Walk through a segment and mark each pairable tag without buddy as
     * isolated. The tags' boolean members m_bPaired and m_bIsolated are false
     * by default.
     *
     * <p>
     * Copied from ling/docproc/extractor/html/ExtractionHandler.
     */
    private void assignPairingStatus(ArrayList p_segment)
    {
        ArrayList tags = new ArrayList(p_segment);
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
    }

    /**
     * The first paragraph contains TMX header info as in "
     * <p>
     * SOURCELANG='EN-US'
     * </p>
     * ".
     */
    public String getSourceLang(String p_text)
    {
        String result;

        int i_start = p_text.indexOf("SOURCELANG='");
        i_start += "SOURCELANG='".length();

        int i_end = p_text.indexOf("'", i_start);

        result = p_text.substring(i_start, i_end);

        return result;
    }

    public String getBaseName(String p_name)
    {
        return p_name.substring(0, p_name.lastIndexOf("."));
    }

    public String getExtension(String p_name)
    {
        return p_name.substring(p_name.lastIndexOf(".") + 1);
    }

    public void startOutputFile(String p_base) throws Exception
    {
        m_filename = p_base + "-gxml.tmx";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(m_filename)),
                "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        m_writer.println(m_header.getTmxDeclaration());
        m_writer.println(m_header.getTmxXml());
    }

    public void closeOutputFile() throws Exception
    {
        m_writer.println("</body>");
        m_writer.println("</tmx>");
        m_writer.close();
    }

    public void writeEntry(String p_tmx)
    {
        m_writer.print(p_tmx);
    }

    public void createHeader()
    {
        m_header = new Tmx();
        m_header.setTmxVersion(Tmx.TMX_GS);
        m_header.setCreationTool("GlobalSight TradosTmxToRtf+WordHtmlToTmx");
        m_header.setCreationToolVersion("1.0");
        m_header.setSegmentationType("sentence");
        m_header.setOriginalFormat("rtf");
        m_header.setAdminLang("en_US");
        // Done in handleFirstParagraph()
        // m_header.setSourceLang("en_US");

        // TODO: Datatype should be a constant in Tmx.java. The data
        // type should probably be a native identifier ("g-tmx"?)
        // because now we have native data and don't care about where
        // it came from.
        m_header.setDatatype("rtf");
    }

    public Reader createInputReader(String p_url) throws Exception
    {
        return new BufferedReader(new InputStreamReader(new FileInputStream(
                p_url), "UTF8"));
    }

    public void parseFile(String p_url) throws Exception
    {
        Reader inputReader = createInputReader(p_url);

        m_parser = new Parser(inputReader);
        m_parser.setHandler(this);

        try
        {
            m_parser.parse();
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String convertHtmlToTmx(String p_url) throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        info("Converting HTML file to TMX: `" + p_url + "'");

        createHeader();
        startOutputFile(baseName);

        parseFile(p_url);

        closeOutputFile();

        info("Processed " + m_entryCount + " TUs into file `" + m_filename
                + "', " + m_errorCount + " errors, " + m_ignoreCount
                + " ignored TUs.");

        return m_filename;
    }

    static public void main(String[] argv) throws Exception
    {
        WordHtmlToTmx a = new WordHtmlToTmx();

        if (argv.length != 1)
        {
            System.err.println("Usage: WordHtmlToTmx FILE\n");
            System.exit(1);
        }

        a.convertHtmlToTmx(argv[0]);
    }

    @Override
    public void handleSpecialChar(
            com.globalsight.ling.docproc.extractor.html.HtmlObjects.Text t)
    {
        // TODO Auto-generated method stub

    }
}
