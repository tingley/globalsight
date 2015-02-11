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
package com.globalsight.ling.docproc.extractor.troff;

import com.globalsight.ling.docproc.extractor.troff.IHandler;
import com.globalsight.ling.docproc.extractor.troff.Tag2TmxMap;
import com.globalsight.ling.docproc.extractor.troff.TroffProperties;
import com.globalsight.ling.docproc.extractor.troff.Token;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.DocumentElementException;
import com.globalsight.ling.docproc.EFInputData;
import com.globalsight.ling.docproc.EFInputDataConstants;
import com.globalsight.ling.docproc.ExtractorException;
import com.globalsight.ling.docproc.ExtractorExceptionConstants;
import com.globalsight.ling.docproc.ExtractorRegistry;
import com.globalsight.ling.docproc.ExtractorRegistryException;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.Segmentable;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TmxTagGenerator;

import com.globalsight.ling.common.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * <P>Listens to the events from the HTML parser and populates the
 * Output object with Diplomat XML extracted from the tags and text
 * pieces received.  Switches to other Extractors as necessary.</P>
 *
 * <P>If this extractor is called on strings inside a JavaScript
 * context, strings will be output as type "string" and not type
 * "text" (the default).</P>
 */
class ExtractionHandler
    implements IHandler, ExtractorExceptionConstants
{
    //
    // Member Variables Section
    //

    private Output m_output = null;
    private EFInputData m_input = null;
    private Extractor m_extractor = null;
    private Tag2TmxMap m_tmxMap = null;
    private TroffProperties m_rules = null;

    /**
     * <p>List of all the candidates for extraction (i.e styles,
     * paragraphs, character tags, text...).</P
     */
    private ArrayList m_extractionCandidates = new ArrayList();
    private ArrayList m_tagsBefore = new ArrayList();
    private ArrayList m_tagsAfter = new ArrayList();

    /**
     * <p>True if the list of extraction candidates contains any
     * text.</p>
     */
    private boolean m_bContainsText;

    /**
     * <p>Flag if an exception has occured inside the visitor methods
     * (they can't throw exceptions).</p>
     */
    private boolean m_bHasError = false;

    /**
     * <p>The message of the exception that occured during visiting
     * the input.</p>
     */
    private String m_strErrorMsg = "";

    //private HtmlEntities m_htmlDecoder = new HtmlEntities();

    //
    // Constructor Section
    //

    public ExtractionHandler(EFInputData p_input, Output p_output,
        Extractor p_Extractor)
    {
        super();

        m_output = p_output;
        m_input = p_input;
        m_extractor = p_Extractor;
        m_tmxMap = new Tag2TmxMap();
        m_rules = new TroffProperties();
    }

    /**
     * @return <code>true</code> if this instance is an embedded
     * extractor, <code>false</code> if it is a top-level, standalone
     * extractor.
     */
    private boolean isEmbeddedExtractor()
    {
        return m_extractor.isEmbedded();
    }

    /**
     * <p>Sends back any error message that occured during the
     * extraction.  An Empty result means that there was no Error.</p>
     */
    public String checkError()
    {
        if (!m_bHasError)
        {
            return null;
        }
        else if (m_strErrorMsg.length() == 0)
        {
            return "An error has occured during TROFF extraction.";
        }

        return m_strErrorMsg;
    }

    //
    // Implementation of Interface - IHandler
    //

    /**
     * Called once at the beginning of a new document.
     */
    public void handleStart()
    {
        m_bContainsText = false;
        m_tmxMap.reset();
        m_extractionCandidates.clear();
        m_tagsBefore.clear();
        m_tagsAfter.clear();
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
            m_tmxMap.reset();
            m_extractionCandidates.clear();
            m_tagsBefore.clear();
            m_tagsAfter.clear();
        }
    }

    public void handleToken(Token t)
    {
    }

    /****
    public void handleSpecialTag(XPTagObjects.SpecialTag t)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            String mapping;
            String tag = t.tag.substring(1, t.tag.length() - 1);

            // Special tags are new boxes, new columns (breaks)
            if (m_rules.isParagraphBreak(tag))
            {
                flushText();
                addTagToSkeleton(t);
            }
            // or codes for special characters
            else if ((mapping = m_rules.mapSpecialCode(
                tag, m_input.getCodeset())) != null)
            {
                if (mapping.length() > 0)
                {
                    ////addToText(new XPTagObjects.Text(mapping));
                }
            }
            // or else codes we represent as ptags (like <BR>)
            else
            {
                addToText(t);
            }
        }
        catch (ExtractorException e)
        {
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }
    ****/

    /**
     * Handle end of line characters (paragraph breaks).
     */
    /*
    public void handleNewline(XPTagObjects.Newline t)
    {
        if (m_bHasError)
        {
            return;
        }

        try
        {
            flushText();
            addTagToSkeleton(t);
        }
        catch (ExtractorException e)
        {
            m_bHasError = true;
            m_strErrorMsg = e.toString();
        }
    }
    */

    /**
     * Handle text (#PCDATA).
     */
    /*
    public void handleText(XPTagObjects.Text t)
    {
        if (m_bHasError)
        {
            return;
        }

        addToText(t);
    }
    */

    //
    // Private and Protected Method Section
    //

    /**
     * <p>Adds a tag to the skeleton.</p>
     *
     * <p>This function is called from both handleStartTag() and
     * flushTextToSkeleton().</p>
     */
    protected void addTagToSkeleton(Object t)
        throws ExtractorException
    {
        m_output.addSkeleton(t.toString());
    }

    /**
     * <p>Adds the current tag or text to the list of extraction
     * candidates.</p>
     */
    protected void addToText(Object p_elt)
    {
        /***
        if (p_elt instanceof XPTagObjects.Text)
        {
            XPTagObjects.Text tag = (XPTagObjects.Text)p_elt;

            if (!m_bContainsText)
            {
                m_bContainsText = !Text.isBlankOrNbsp(tag.text);
            }
        }
        ***/
        /*
        else if (p_elt instanceof XPTagObjects.CharTag)
        {
            XPTagObjects.CharTag tag = (XPTagObjects.CharTag)p_elt;
            ...
        }
        */

        m_extractionCandidates.add(p_elt);
    }

    /**
     * <p>Walks through a segment and marks each pairable tag without
     * buddy as isolated.  The tags' boolean members m_paired and
     * m_isolated are false by default.
     */
    protected void assignPairingStatus(ArrayList p_segment)
    {
        /*
        ArrayList tags = new ArrayList(p_segment);
        Object o1, o2;
        int i_start, i_end, i_max;
        int i_level, i_partner = 1;
        XPTagObjects.Tag t_start, t_tag;
        XPTagObjects.EndTag t_end;
        XPTagObjects.CFTag t_CFstart, t_CFtag;

        i_start = 0;
        i_max = tags.size();
  outer:
        while (i_start < i_max)
        {
            o1 = tags.get(i_start);

            if (o1 instanceof XPTagObjects.Tag)
            {
                t_start = (XPTagObjects.Tag)o1;

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

                    if (o2 instanceof XPTagObjects.Tag)
                    {
                        t_tag = (XPTagObjects.Tag)o2;

                        if (t_start.tag.equalsIgnoreCase(t_tag.tag))
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof XPTagObjects.EndTag)
                    {
                        t_end = (XPTagObjects.EndTag)o2;

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
            else if (o1 instanceof XPTagObjects.CFTag)
            {
                t_CFstart = (XPTagObjects.CFTag)o1;

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

                    if (o2 instanceof XPTagObjects.CFTag)
                    {
                        t_CFtag = (XPTagObjects.CFTag)o2;

                        if (t_CFstart.tag.equalsIgnoreCase(t_CFtag.tag))
                        {
                            ++i_level;
                            continue;
                        }
                    }
                    else if (o2 instanceof XPTagObjects.EndTag)
                    {
                        t_end = (XPTagObjects.EndTag)o2;

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
                if (m_rules.isPairedTag(t_CFstart.tag))
                {
                    t_CFstart.isIsolated = true;
                }

                // done with this tag, don't consider again
                tags.remove(i_start);
                --i_max;
                continue outer;
            }
            else if (! (o1 instanceof XPTagObjects.EndTag))
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
            XPTagObjects.HtmlElement t =
                (XPTagObjects.HtmlElement)tags.get(i_start);

            t.isIsolated = true;
        }
        */
    }

    /**
     * Removes tags from the beginning and end of the segment, and
     * collapses runs of multiple tags into single (uber) tags.
     */
    protected void minimizeParagraph(ArrayList p_segments,
        ArrayList p_before, ArrayList p_after)
        throws ExtractorException
    {
        // Remove tags at the beginning of the paragraph.
        while (p_segments.size() > 0)
        {
            Object o = p_segments.get(0);

            /***
            if (o instanceof XPTagObjects.Tag)
            {
                p_before.add(o);
                p_segments.remove(0);
                continue;
            }
            else if (o instanceof XPTagObjects.Text ||
                o instanceof XPTagObjects.Newline)
            {
                if (Text.isBlankOrNbsp(o.toString()))
                {
                    p_before.add(o);
                    p_segments.remove(0);
                    continue;
                }
            }
            ***/
            break;
        }

        // Remove tags at the end of the paragraph.
        int len;
        while ((len = p_segments.size()) > 0)
        {
            Object o = p_segments.get(len - 1);

            /***
            if (o instanceof XPTagObjects.Tag)
            {
                p_after.add(o);
                p_segments.remove(len - 1);
                continue;
            }
            else if (o instanceof XPTagObjects.Text ||
                o instanceof XPTagObjects.Newline)
            {
                if (Text.isBlankOrNbsp(o.toString()))
                {
                    p_after.add(o);
                    p_segments.remove(len - 1);
                    continue;
                }
            }
            ***/
            break;
        }

        // collapse tag runs into single tags
        for (int i = 0, max = p_segments.size() - 1; i < max; i++)
        {
            Object o1 = p_segments.get(i);
            Object o2 = p_segments.get(i + 1);

            /***
            if (o1 instanceof XPTagObjects.Tag &&
                o2 instanceof XPTagObjects.Tag)
            {
                XPTagObjects.Tag oo1 = (XPTagObjects.Tag)o1;
                XPTagObjects.Tag oo2 = (XPTagObjects.Tag)o2;

                oo1.tag = oo1.tag + oo2.tag;

                p_segments.remove(i + 1);
                i--;
                max--;
            }
            ***/
        }
    }

    /**
     * Flushes all tags passed in to the skeleton. The argument list
     * is destructively modified (i.e., cleared).
     */
    protected void flushLeftOverTags(ArrayList p_segments)
    {
        for (int i = 0, max = p_segments.size(); i < max; i++)
        {
            Object o = p_segments.get(i);

            m_output.addSkeleton(o.toString());
        }
    }

    /**
     * <P>This method is called each time we reach a segment breaking
     * element.  Flushes out the list of potential extraction
     * candidates, updating as necessary the skeleton and segment
     * list.</p>
     */
    protected void flushText()
        throws ExtractorException
    {
        if (m_bContainsText)
        {
            m_tagsBefore.clear();
            m_tagsAfter.clear();

            // For each segment in the list: assign tag status
            // (paired, isolated). (Noop for Quark.)
            assignPairingStatus(m_extractionCandidates);

            minimizeParagraph(m_extractionCandidates,
                m_tagsBefore, m_tagsAfter);

            // Move tags at the beginning segment out of the way.
            flushLeftOverTags(m_tagsBefore);

            // flush the segment
            flushTextToTranslatable(m_extractionCandidates);

            // Flush any stuff left at the end of the segment
            flushLeftOverTags(m_tagsAfter);
        }
        else
        {
            flushTextToSkeleton(m_extractionCandidates);
        }

        m_bContainsText = false;
        m_extractionCandidates.clear();
    }

    /**
     * <p>Helper method for flushText(): flushes text and tags to a
     * TMX skeleton section.</p>
     */
    protected void flushTextToSkeleton(ArrayList p_elements)
        throws ExtractorException
    {
        for (int i = 0, max = p_elements.size(); i < max; i++)
        {
            Object o = p_elements.get(i);

            /***
            if (o instanceof XPTagObjects.CharTag)
            {
                addTagToSkeleton((XPTagObjects.CharTag)o);
            }
            else
            {
                m_output.addSkeleton(o.toString());
            }
            ***/
        }
    }

    /**
     * <p>Helper method for flushText(): flushes text and tags to a
     * TMX translatable section.</p>
     */
    protected void flushTextToTranslatable(ArrayList p_segments)
        throws ExtractorException
    {
        StringBuffer buf = new StringBuffer();

        for (ListIterator it = p_segments.listIterator(); it.hasNext(); )
        {
            Object o = it.next();

            /***
            if (o instanceof XPTagObjects.Text ||
                o instanceof XPTagObjects.Newline)
            {
                // Combine consecutive text and newline nodes to get
                // correct whitespace normalization.
                buf.setLength(0);
                buf.append(o.toString());

                while (it.hasNext())
                {
                    o = it.next();

                    if (o instanceof XPTagObjects.Text ||
                        o instanceof XPTagObjects.Newline)
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
                m_output.addTranslatable(normalizeString(buf.toString()));
            }
            else if (o instanceof XPTagObjects.CharTag)
            {
                XPTagObjects.CharTag t = (XPTagObjects.CharTag)o;
                TmxTagGenerator tg;

                if (!t.isPaired && !t.isIsolated)
                {
                    tg = m_tmxMap.getPlaceholderTmxTag(t.tag, true);
                }
                else
                {
                    tg = m_tmxMap.getPairedTmxTag(t, true, t.isIsolated);
                }

                flushTagToTranslatable(tg, t.toString());
            }
            else if (o instanceof XPTagObjects.SpecialTag)
            {
                XPTagObjects.SpecialTag t = (XPTagObjects.SpecialTag)o;
                TmxTagGenerator tg;

                tg = m_tmxMap.getPlaceholderTmxTag(t.tag, true);

                flushTagToTranslatable(tg, t.toString());
            }
            ***/
        }

        m_tmxMap.resetCounter();
    }

    /**
     * <p>Flushes a single tag as part of flushing out the list of
     * potential extraction candidates.
     *
     * <p>Called from flushTextToTranslatable().
     */
    private void flushTagToTranslatable(TmxTagGenerator tg, String tag)
        throws ExtractorException
    {
        m_output.addTranslatableTmx(tg.getStart());
        m_output.addTranslatable(tag);
        m_output.addTranslatableTmx(tg.getEnd());
    }

    /**
     * Called from flushTextToTranslatable().
     */
    protected String normalizeString(String p_text)
    {
        return Text.normalizeWhiteSpaces(p_text);
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
