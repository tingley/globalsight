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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.segmentationhelper.Segmentation;
import com.globalsight.everest.segmentationhelper.SegmentationRule;
import com.globalsight.everest.segmentationhelper.XmlLoader;
import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.common.srccomment.SourceComment;
import com.globalsight.ling.docproc.extractor.html.OfficeContentPostFilterHelper;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Takes unsegmented GXML input and produces a segmented version based on the
 * locale. We also do a bit of TMX cleanup so we are conformant to the TMX spec.
 * 
 * <p>
 * Mon Dec 16 21:11:14 2002 Now handles TMX tags representing whitespace
 * correctly during segmentation.
 * </p>
 * 
 * <p>
 * NOTE: EACH EXTRACTOR NEEDS TO OUTPUT THE TYPE ATTRIBUTE OF A TMX TAG IN A
 * TRANSLATABLE STRING BEFORE ANY OTHER ATTRIBUTES FOR THE REGULAR EXPRESSIONS
 * BELOW TO WORK.
 * </p>
 */
public class DiplomatSegmenter
{
    //
    // Member Variables
    //

    private static final Logger logger = Logger
            .getLogger(DiplomatSegmenter.class);

    private boolean m_preserveWhitespace = false;

    private Output m_output = null;
    private XmlEntities m_codec = null;
    // Segmentation implementation with default SRX.
    private Segmenter m_defaultSegmenter = null;

    private RE m_tmxTags = null;
    private RE m_startTagsAtEnd = null;
    private RE m_matchAllBptEpt = null;

    // Segmenting information:
    private String m_segment = null;
    private String m_segmentWithoutTags = null;
    // List of tag positions in the original translatable string.
    private ArrayList m_tagPositions = null;

    // This is a hack. I don't understand why it is needed. Without it,
    // the following segment fails to segment (BR == internal, .doc-html):
    // <p><b><span>Press Release No. [<BR>]<span
    // style='mso-tab-count:1'> </span></span></b><span>IFC Corporate
    // Relations</span></p>
    private int m_globalAdjust;

    private boolean isXliff = false;

    /**
     * Stores the position of a TMX tag in a segment string.
     */
    final private class TagPosition
    {
        // TMX and native tags
        public String m_tag = null;
        // original offset in string
        public int m_offset = 0;
        // length of tag sequence
        public int m_length = 0;
        // flag to treat tag as single space or empty
        public int m_adjust = 0;
    }

    /**
     * Stores the break position in a segment string.
     */
    final private class BreakPosition
    {
        // Offset of segment boundary
        public int m_split = 0;

        public BreakPosition(int p_pos)
        {
            m_split = p_pos;
        }
    }

    //
    // Constructor
    //

    public DiplomatSegmenter()
    {
        m_codec = new XmlEntities();
        m_tagPositions = new ArrayList();

        try
        {
            // Fri Nov 17 03:49:34 2000 (CvdL): This here is a
            // peculiar problem with the RE: it won't match correctly
            // when intervening stuff (the .*? part) contains
            // newlines... have to use (.|[:space:])*?

            // new RE("(<(bpt|ept|it|ph|ut)[^>]+>)(.*?)(</(\\2)\\s*>)",

            // Mon Dec 16 21:06:31 2002 (CvdL): we now match the type
            // attribute too and if the tag represents some kind of
            // whitespace, replace it with a single blank.
            m_tmxTags = new RE(
                    "<(bpt|ept|it|ph|ut) +(type=\"([^\"]+)\")?[^>]*>(.|[:space:])*?</(\\1)>",
                    RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e) // SNH (Should Not Happen)
        {
            System.err.println(e.toString());
        }

        try
        {
            // Matches <bpt>..</bpt> optionally followed by <ph> and
            // <it> at the end of a string.
            m_startTagsAtEnd = new RE(
                    "<bpt[^>]+>[^<]*?(<[^>]*?>[^<]*?</[^>]*?>[^<]*)*</bpt>([:space:]*?<(ph|it)[^>]+>[^<]*?(<[^>]*?>[^<]*?</[^>]*?>[^<]*)*</\\3>)*[:space:]*?$",
                    RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e) // SNH (Should Not Happen)
        {
            System.err.println(e.toString());
        }

        try
        {
            // paren 1 is the tagname, paren 2 is the value of the i attribute.
            m_matchAllBptEpt = new RE("<(bpt|ept)[^>]+i=\"([^\"]+)\"",
                    RE.MATCH_NORMAL);
        }
        catch (RESyntaxException e) // SNH (Should Not Happen)
        {
            System.err.println(e.toString());
        }
    }

    //
    // Public Methods
    //

    /**
     * Treat tags representing whitespace as white during segmentation.
     */
    public void setPreserveWhitespace(boolean p_flag)
    {
        m_preserveWhitespace = p_flag;
    }

    /**
     * <p>
     * Takes GXML input from an <code>Output</code> object, segments each
     * "TranslatableElement" node and keeps the result in an internal
     * <code>Output</code>object.
     * 
     * <p>
     * The result can be retrieved as the Output object itself (
     * <code>getOutput()</code>), or as string (<code>getDiplomatXml()</code>).
     * 
     * @throws Exception
     */
    public void segment(Output p_diplomat) throws Exception
    {
        m_output = p_diplomat;

        doSegmentation();
    }

    public void segmentXliff(Output p_diplomat) throws Exception
    {
        isXliff = true;
        m_output = p_diplomat;

        doSegmentation();
    }

    /**
     * <p>
     * Takes GXML input from an <code>Output</code> object, segments each
     * "TranslatableElement" node according to segmentation rule and keeps the
     * result in an internal <code>Output</code>object.
     * 
     * <p>
     * The result can be retrieved as the Output object itself (
     * <code>getOutput()</code>), or as string (<code>getDiplomatXml()</code>).
     */
    public void segment(Output p_diplomat, String p_segmentationRuleText)
            throws DiplomatSegmenterException, Exception
    {
        m_output = p_diplomat;

        doSegmentation(p_segmentationRuleText);
    }

    /**
     * Takes GXML input from a string. Converts it to our internal data
     * structure and then performs segmentation on each "TranslatableElement"
     * node.
     */
    public String segment(String p_gxml) throws Exception
    {
        try
        {
            convertToOutput(p_gxml);
        }
        catch (DiplomatReaderException e)
        {
            throw new DiplomatSegmenterException(
                    DiplomatSegmenterExceptionConstants.SEGMENTER_ERROR, e);
        }

        doSegmentation();

        return getDiplomatXml();
    }

    /**
     * Takes GXML input from a string. Converts it to our internal data
     * structure and then performs segmentation according to segmentation rule
     * on each "TranslatableElement" node.
     */
    public String segment(String p_gxml, String p_ruleText)
            throws DiplomatSegmenterException, Exception
    {
        try
        {
            convertToOutput(p_gxml);
        }
        catch (DiplomatReaderException e)
        {
            throw new DiplomatSegmenterException(
                    DiplomatSegmenterExceptionConstants.SEGMENTER_ERROR, e);
        }

        doSegmentation(p_ruleText);

        return getDiplomatXml();
    }

    /**
     * Returns the segmented Diplomat XML as string.
     */
    public String getDiplomatXml()
    {
        String result = DiplomatWriter.WriteXML(m_output);

        return result;
    }

    /**
     * Returns the segmented GXML as <code>Output</code> object.
     */
    public Output getOutput()
    {
        return m_output;
    }

    //
    // Private Methods
    //

    /**
     * Parses a GXML string into an internal Output object.
     */
    private void convertToOutput(String p_gxml) throws DiplomatReaderException
    {
        DiplomatReader reader = new DiplomatReader(p_gxml);
        m_output = reader.getOutput();
    }

    /**
     * Walks an internal Output object and segments translatable nodes.
     * 
     * @throws Exception
     */
    private void doSegmentation() throws Exception
    {
        Locale locale = LocaleCreater.makeLocale(m_output.getLocale());
        m_defaultSegmenter = new Segmenter(locale);

        HashMap<String, String> trgTrunks = null;
        if (isXliff)
        {
            trgTrunks = getTargetChuncks();
        }
        HashMap<String, List<String>> srcSegmentedResult = new HashMap<String, List<String>>();

        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    List<String> segments = new ArrayList<String>();

                    if (isXliff)
                    {
                        segments = segmentXliff(elem, trgTrunks,
                                srcSegmentedResult, null);
                    }
                    else
                    {
                        segments = segment(elem);
                    }

                    addSegmentsToNode(elem, segments, null);

                    elem.setChunk(null);

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    LocalizableElement elem = (LocalizableElement) de;
                    updateSegmentInNode(elem, elem.getChunk());

                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }
    }

    /**
     * Walks an internal Output object and segments translatable nodes according
     * to segmentation rule.
     */
    private void doSegmentation(String p_segmentationRuleText)
            throws DiplomatSegmenterException, Exception
    {
        SegmentationRule srx = XmlLoader
                .parseSegmentationRule(p_segmentationRuleText);

        HashMap<String, String> trgTrunks = null;
        if (isXliff)
        {
            trgTrunks = getTargetChuncks();
        }
        HashMap<String, List<String>> srcSegmentedResult = new HashMap<String, List<String>>();

        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    List<String> segments = new ArrayList<String>();

                    if (isXliff)
                    {
                        segments = segmentXliff(elem, trgTrunks,
                                srcSegmentedResult, srx);
                    }
                    else
                    {
                        segments = segment(elem, srx);
                    }

                    addSegmentsToNode(elem, segments, srx);

                    elem.setChunk(null);

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    LocalizableElement elem = (LocalizableElement) de;
                    updateSegmentInNode(elem, elem.getChunk());

                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }
    }

    /**
     * Segments a single Translatable node according to segmentation rule by
     * making a list of offsets of segment breaks. Also remembers tag lengths in
     * the TagNode list <code>m_tagPositions</code>.
     */
    private List<String> segment(TranslatableElement p_node,
            SegmentationRule p_rule) throws Exception
    {
        m_segment = p_node.getChunk();
        String type = p_node.getDataType();
        if (type != null && "javascript".equalsIgnoreCase(type))
        {
            m_segment = EditUtil.encodeNTREntities(new StringBuffer(m_segment));
        }
        m_tagPositions.clear();

        // Remove all TMX+native tags and store them for later.
        m_segmentWithoutTags = removeTags(m_segment);

        ArrayList<Integer> breakPositions = new ArrayList<Integer>();

        Segmentation segmentation = new Segmentation();
        // Set locale used by text to be segmented.
        segmentation.setLocale(m_output.getLocale());
        // Set text to be segmented.
        segmentation.setTranslable(m_segmentWithoutTags);
        // Set segmentation rule generated according to locale
        // from rule xml text.
        segmentation.setSegmentationRule(p_rule);
        // Now do segmentation, and produce the break indexes.
        segmentation.doSegmentation();
        // Get break indexes.
        breakPositions = segmentation.getBreakIndex();

        // Now go back and split the string with tags into segments.
        List<String> segments = splitOriSegmentWithRule(breakPositions);

        // Finally move a few tags around and make broken bpt/epb isolated.
        segments = fixTmxTags(segments);

        // Move a few tags from office content in the end of one segment to
        // next.
        segments = fixOfficeContentTags(segments);

        return segments;
    }

    /**
     * Segments a single Translatable node by making a list of offsets of
     * segment breaks. Also remembers tag lengths in the TagNode list
     * <code>m_tagPositions</code>.
     */
    private List<String> segment(TranslatableElement p_node)
            throws DiplomatSegmenterException
    {
        m_segment = p_node.getChunk();
        m_tagPositions.clear();

        // Remove all TMX+native tags and store them for later.
        m_segmentWithoutTags = removeTags(m_segment);

        // Do Locale sensitive sentence breaking.
        m_defaultSegmenter.setText(m_segmentWithoutTags);

        ArrayList breakPositions = new ArrayList();

        // System.err.println("::: `" + m_segmentWithoutTags + "'");

        int iStart = m_defaultSegmenter.first();
        for (int iEnd = m_defaultSegmenter.next(); iEnd != Segmenter.DONE; iStart = iEnd, iEnd = m_defaultSegmenter
                .next())
        {
            // System.err.println("--> `" +
            // m_segmentWithoutTags.substring(iStart, iEnd) + "'");

            BreakPosition pos = new BreakPosition(iEnd);
            breakPositions.add(pos);
        }

        // remove these index for GBS-3794
        /*
         * <rule break="no"> <beforebreak>\w\n</beforebreak>
         * <afterbreak>[\s]+</afterbreak> </rule>
         */

        // Now go back and split the string with tags into segments.
        List<String> segments = splitOriSegment(breakPositions);

        // Finally move a few tags around and make broken bpt/epb isolated.
        segments = fixTmxTags(segments);

        // Move a few tags from office content in the end of one segment to
        // next.
        segments = fixOfficeContentTags(segments);

        return segments;
    }

    /**
     * Splits the original segment into segments at the specified break
     * positions. Takes into account intervening TMX tags.
     */
    private List<String> splitOriSegmentWithRule(ArrayList<Integer> p_breaks)
            throws Exception
    {
        List<String> result = new ArrayList<String>();

        // offsets in m_segmentWithoutTags
        int iStartWithoutTags = 0;
        int iEndWithoutTags;
        int iTotalIncrease = 0;

        // offsets in m_segment
        int iStart = 0;
        int iEnd;
        int iTotalTagLen = 0;

        // If the section was empty, restore the original input.
        if (p_breaks.size() == 0)
        {
            result.add(m_segment);
            return result;
        }

        // Else compute the segment breaks in the original input and
        // add the segments to the segment list.
        for (Iterator<Integer> it = p_breaks.iterator(); it.hasNext();)
        {
            Integer pos = it.next();

            iEndWithoutTags = pos.intValue();

            String text = m_segmentWithoutTags.substring(iStartWithoutTags,
                    iEndWithoutTags);

            int iIncrease = m_codec.encodeStringBasic(text).length()
                    - (iEndWithoutTags - iStartWithoutTags);
            int iTagLen = previousTagLengths(pos.intValue() + iIncrease
                    + iTotalIncrease);

            iEnd = pos.intValue() + iIncrease + iTotalIncrease + iTagLen
                    + iTotalTagLen;
            iEnd += m_globalAdjust;

            try
            {
                String seg = m_segment.substring(iStart, iEnd);
                result.add(seg);
            }
            catch (Exception ex)
            {
                logger.error("substring error, segment: " + m_segment);
                logger.error("iStart " + iStart + " iEnd " + iEnd);
                logger.error("substring error!", ex);
                throw ex;
            }

            iTotalIncrease += iIncrease;
            iTotalTagLen += iTagLen;

            iStart = iEnd;
            iStartWithoutTags = iEndWithoutTags;
        }

        return result;
    }

    /**
     * Splits the original segment into segments at the specified break
     * positions. Takes into account intervening TMX tags.
     */
    private List<String> splitOriSegment(ArrayList p_breaks)
    {
        List<String> result = new ArrayList<String>();

        // offsets in m_segmentWithoutTags
        int iStartWithoutTags = 0;
        int iEndWithoutTags;
        int iTotalIncrease = 0;

        // offsets in m_segment
        int iStart = 0;
        int iEnd;
        int iTotalTagLen = 0;

        // If the section was empty, restore the original input.
        if (p_breaks.size() == 0)
        {
            result.add(m_segment);
            return result;
        }

        // Else compute the segment breaks in the original input and
        // add the segments to the segment list.
        for (Iterator it = p_breaks.iterator(); it.hasNext();)
        {
            BreakPosition pos = (BreakPosition) it.next();

            iEndWithoutTags = pos.m_split;

            // System.err.println("Old: " + iStartWithoutTags + ":" +
            // iEndWithoutTags + " `" +
            // m_segmentWithoutTags.substring(iStartWithoutTags,
            // iEndWithoutTags) + "'");

            String text = m_segmentWithoutTags.substring(iStartWithoutTags,
                    iEndWithoutTags);

            int iIncrease = m_codec.encodeStringBasic(text).length()
                    - (iEndWithoutTags - iStartWithoutTags);

            int iTagLen = previousTagLengths(pos.m_split + iIncrease
                    + iTotalIncrease);

            iEnd = pos.m_split + iIncrease + iTotalIncrease + iTagLen
                    + iTotalTagLen;

            iEnd += m_globalAdjust;

            // System.err.println("start=" + iStart + " increase=" + iIncrease +
            // " taglen=" + iTagLen + " end=" + iEnd);

            // System.err.println("New: " + iStart + ":" + iEnd + " `" +
            // m_segment.substring(iStart, iEnd) + "'");
            if (iEnd > m_segment.length())
            {
                result.add(m_segment.substring(iStart));
            }
            else
            {
                result.add(m_segment.substring(iStart, iEnd));
            }

            iTotalIncrease += iIncrease;
            iTotalTagLen += iTagLen;

            iStart = iEnd;
            iStartWithoutTags = iEndWithoutTags;
        }

        return result;
    }

    /**
     * Moves <ph> tags at the end of segments to the beginning of the next.
     */
    private List<String> fixOfficeContentTags(List<String> segments)
    {
        for (int i = 0; i < segments.size() - 1; i++)
        {
            String segment = segments.get(i);
            while (segment.trim().endsWith("</ph>"))
            {
                int lastPhIndex = segment.lastIndexOf("<ph ");
                if (lastPhIndex < 0)
                {
                    break;
                }
                String s = segment.substring(lastPhIndex);
                if (s.contains(OfficeContentPostFilterHelper.IS_FROM_OFFICE_CONTENT))
                {
                    String newSegment = segment.substring(0, lastPhIndex);
                    segments.set(i + 1, s + segments.get(i + 1));
                    segments.set(i, newSegment);
                    segment = newSegment;
                }
                else
                {
                    break;
                }
            }
        }
        return segments;
    }

    /**
     * Moves <bpt> tags at the end of segments to the beginning of the next.
     * 
     * Then converts all <bpt>, <ept> tags to <it> tags if they were split by
     * segment boundaries.
     */
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    private List<String> fixTmxTags(List<String> p_segments)
    {
        String segment;
        int offset;

        if (p_segments.size() == 0)
        {
            return p_segments;
        }

        // Move all <bpt> at the end of a segment to the beginning of
        // the next segment.

        // Iterate all segments except for the last one.
        for (int i = 0; i < p_segments.size() - 1; ++i)
        {
            String input = p_segments.get(i);
            // System.err.println("STARTTAG_AT_END? " + input);

            while (m_startTagsAtEnd.match(input))
            {
                // Move the tags to the beginning of the next segment
                segment = p_segments.get(i + 1);
                p_segments.set(i + 1, m_startTagsAtEnd.getParen(0) + segment);

                // remove the matched tags from the segment
                segment = p_segments.get(i);
                offset = m_startTagsAtEnd.getParenStart(0);
                p_segments.set(i, segment.substring(0, offset));

                // reset loop variable
                input = p_segments.get(i);
            }
        }

        // Convert all <bpt>, <ept> tags to <it> tags if they were
        // split by segment boundaries.
        Hashtable tag_index = new Hashtable();
        String paren2, paren3, paren4, xKey, substitute;
        for (int i = 0; i < p_segments.size(); ++i)
        {
            int start = 0;
            String input = p_segments.get(i);
            tag_index.clear();

            while (m_matchAllBptEpt.match(input, start))
            {
                String key = m_matchAllBptEpt.getParen(2);

                if (tag_index.get(key) == null)
                {
                    tag_index
                            .put(key, m_matchAllBptEpt.getParen(1)
                                    .equals("bpt") ? "begin" : "end");
                }
                else
                {
                    // If the same index is found, there is a peer in
                    // the paragraph.
                    tag_index.remove(key);
                }

                start = m_matchAllBptEpt.getParenEnd(0);
            }

            // replace <bpt|ept> tags with <it>
            String replaced = p_segments.get(i);
            Enumeration keys = tag_index.keys();

            while (keys.hasMoreElements())
            {
                String key = (String) keys.nextElement();
                String value = (String) tag_index.get(key);
                RE matchAllBptEpt = null;

                try
                {
                    matchAllBptEpt = new RE("<(bpt|ept)[:space:]+([^>]*)i=\""
                            + key + "\"([^>]*)>((.|[:space:])+?)</\\1>",
                            RE.MATCH_NORMAL);
                }
                catch (RESyntaxException e) // SNH (Should Not Happen)
                {
                    System.err.println(e.toString());
                }

                start = 0;
                xKey = "x=\"" + key + "\"";
                while (matchAllBptEpt.match(replaced, start))
                {
                    paren2 = matchAllBptEpt.getParen(2);
                    paren3 = matchAllBptEpt.getParen(3);
                    paren4 = matchAllBptEpt.getParen(4);
                    // if x="key" has already existed (XLF case), not add
                    // duplicate one.
                    if (paren2.indexOf(xKey) > -1 || paren3.indexOf(xKey) > -1
                            || paren4.indexOf(xKey) > -1)
                    {
                        substitute = "<it " + matchAllBptEpt.getParen(2)
                                + "pos=\"" + value + "\" " + "i=\"" + key
                                + "\"" + matchAllBptEpt.getParen(3) + ">"
                                + matchAllBptEpt.getParen(4) + "</it>";
                    }
                    else
                    {
                        substitute = "<it " + matchAllBptEpt.getParen(2)
                                + "pos=\"" + value + "\" " + "x=\"" + key
                                + "\" i=\"" + key + "\""
                                + matchAllBptEpt.getParen(3) + ">"
                                + matchAllBptEpt.getParen(4) + "</it>";
                    }

                    replaced = matchAllBptEpt.subst(replaced, substitute,
                            RE.REPLACE_FIRSTONLY);

                    start = matchAllBptEpt.getParenEnd(0);
                }
            }

            p_segments.set(i, replaced);
        }

        return p_segments;
    }

    /**
     * Calculates the lengths of all intervening TMX tags between beginning of
     * segments and current position.
     */
    private int previousTagLengths(int p_iSplitPoint)
    {
        m_globalAdjust = 0;

        if (m_tagPositions.isEmpty())
        {
            return 0;
        }

        int result = 0;

        while (!m_tagPositions.isEmpty())
        {
            TagPosition pos = (TagPosition) m_tagPositions.remove(0);

            // System.err.println("Split at " + p_iSplitPoint +
            // " pos.m_offset = " + pos.m_offset +
            // " (adjust=" + pos.m_adjust + ")" +
            // " pos.m_length = " + pos.m_length +
            // "\n\ttag = `" + pos.m_tag + "'");

            if (pos.m_offset <= p_iSplitPoint)
            {
                result += pos.m_length;
                result -= pos.m_adjust;

                // Mon Jun 23 21:18:34 2003 CvdL
                // Adjust the offset if the split is right where a
                // whitespace tag was replaced by a space char " ".
                // If this is not done, the last ">" of the tag will
                // be printed as first char of the next segment.
                if (pos.m_offset == p_iSplitPoint)
                {
                    m_globalAdjust = pos.m_adjust;
                }
            }
            else
            {
                m_tagPositions.add(0, pos);
                break;
            }
        }

        return result;
    }

    /**
     * Removes all TMX tags and TMX content from the given string, returning the
     * pure text.
     * 
     * As a side effect, populates m_tagPositions with positions where tags have
     * been in the original string.
     */
    public String removeTags(String p_string)
    {
        int start = 0;
        int iTagLength = 0;

        // Find TMX tag positions
        while (m_tmxTags.match(p_string, start))
        {
            TagPosition pos = new TagPosition();

            // TMX and native tags
            pos.m_tag = m_tmxTags.getParen(0);
            // original offset in string
            pos.m_offset = m_tmxTags.getParenStart(0) - iTagLength;
            // length of tag sequence
            pos.m_length = m_tmxTags.getParenLength(0);

            String type = m_tmxTags.getParen(3);
            // System.err.println("TMX tag type = " + type);

            // Replace tags representing whitespace with actual white
            // space during segmentation. Always do this for MS Office
            // whitespace, but for backwards-compatibility, do it for
            // other tags only if demanded
            // (segmentation_preserve_whitespace=true in
            // Diplomat.properties).
            if (type != null)
            {
                if (Text.isTmxMsoWhitespaceNode(type)
                        || (m_preserveWhitespace && Text
                                .isTmxWhitespaceNode(type)))
                {
                    pos.m_adjust = 1;
                }
            }

            m_tagPositions.add(pos);

            iTagLength += (pos.m_length - pos.m_adjust);

            start = m_tmxTags.getParenEnd(0);
        }

        // Replace all TMX tags with either "" or " ".
        m_segmentWithoutTags = p_string;
        for (int i = 0; i < m_tagPositions.size(); ++i)
        {
            TagPosition pos = (TagPosition) m_tagPositions.get(i);

            String replace = "";

            if (pos.m_adjust == 1)
            {
                replace = " ";
            }

            m_segmentWithoutTags = m_tmxTags.subst(m_segmentWithoutTags,
                    replace, RE.REPLACE_FIRSTONLY);
        }

        // entities are escaped, must decode (must not decode twice)
        m_segmentWithoutTags = m_codec.decodeStringBasic(m_segmentWithoutTags);

        // System.err.println("Segment = " + m_segmentWithoutTags);

        return m_segmentWithoutTags;
    }

    /**
     * Adds the new segments to the original translatable node.
     */
    private void addSegmentsToNode(TranslatableElement p_element,
            List<String> p_segments, SegmentationRule p_srxRule)
            throws DiplomatSegmenterException
    {
        List<String> segments = new ArrayList<String>();
        for (Iterator<String> it = p_segments.iterator(); it.hasNext();)
        {
            String segment = it.next();
            if (segment != null)
            {
                segments.add(segment);
            }
        }

        List<SegmentNode> nodes = Segmentation.handleSrxExtension(p_srxRule,
                segments);
        List<SegmentNode> nodes2 = SourceComment.handleSrcComment(nodes);
        for (SegmentNode segmentNode : nodes2)
        {
            p_element.addSegment(segmentNode);
        }
    }

    /**
     * Updates the original localizable node.
     */
    private void updateSegmentInNode(LocalizableElement p_element, String p_loc)
            throws DiplomatSegmenterException
    {
        if (p_loc != null)
        {
            p_element.setChunk(p_loc);
        }
    }

    // Put target chunks into map for later using, only for xliff/PO file.
    private HashMap<String, String> getTargetChuncks()
    {
        HashMap<String, String> chunks = new HashMap<String, String>();
        for (Iterator it = m_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            if (de.type() == DocumentElement.TRANSLATABLE)
            {
                TranslatableElement elem = (TranslatableElement) de;
                String xlfPartName = elem.getXliffPartByName();
                if ("target".equalsIgnoreCase(xlfPartName))
                {
                    String key = getKeyFromElement(elem);
                    if (key != null)
                    {
                        chunks.put(key, elem.getChunk());
                    }
                }
            }
        }

        return chunks;
    }

    // key is "tuID_mid" style or only "tuID" if no "mid".
    private String getKeyFromElement(TranslatableElement elem)
    {
        String key = null;
        Map xlfAtts = elem.getXliffPart();
        if (xlfAtts != null && xlfAtts.size() > 0)
        {
            String tuID = null;
            String mid = null;
            if (xlfAtts.get("tuID") != null)
            {
                tuID = (String) xlfAtts.get("tuID");
            }
            if (xlfAtts.get("xliffSegSourceMrkId") != null)
            {
                mid = (String) xlfAtts.get("xliffSegSourceMrkId");
            }

            if (tuID != null)
            {
                key = tuID;
                if (mid != null)
                {
                    key += "_" + mid;
                }
            }
        }
        return key;
    }

    /**
     * For bilingual files such as XLF or PO, they do not need segmentation
     * generally, only do segmentation when target segment is empty or equals to
     * source.
     * 
     * @param elem
     * @param trgTrunks
     *            -- cache all target trunks to decide if segment source.
     * @param srcSegmentedResult
     *            -- cache segmented source, if one source becomes to pieces
     *            after segmentation, need add same size targets nodes.
     * @return
     * @throws Exception
     */
    private List<String> segmentXliff(TranslatableElement elem,
            HashMap<String, String> trgTrunks,
            HashMap<String, List<String>> srcSegmentedResult,
            SegmentationRule srx) throws Exception
    {
        List<String> segments = new ArrayList<String>();

        boolean needDoSegmentation = false;
        String xliffChunk = elem.getChunk();
        String key = getKeyFromElement(elem);
        // If segment has tag, do not do segmentation anyway.
        boolean hasTag = (xliffChunk.indexOf("<") > -1 || xliffChunk
                .indexOf(">") > -1);
        // if target is empty or same with source or composed of pure tags,
        // source need segmentation.
        if (!hasTag && "source".equals(elem.getXliffPartByName())
                && key != null)
        {
            String trgTrunk = trgTrunks.get(key);
            if (StringUtil.isEmpty(trgTrunk)
                    || (trgTrunk.equals(xliffChunk) && elem
                            .getTmScoreFromXlfPart() != 100f))
            {
                needDoSegmentation = true;
            }

            if (!needDoSegmentation)
            {
                try
                {
                    String textValue = MTHelper.getGxmlElement(
                            "<segment>" + trgTrunk + "</segment>")
                            .getTextValue();
                    if (StringUtil.isEmpty(textValue))
                        needDoSegmentation = true;
                }
                catch (Exception ignore)
                {
                }
            }
        }

        // only source is possible to go through segmentation.
        if (needDoSegmentation)
        {
            if (srx == null)
            {
                segments = segment(elem);
            }
            else
            {
                segments = segment(elem, srx);
            }

            srcSegmentedResult.put(key, segments);
        }
        else
        {
            List<String> segmentedSrc = srcSegmentedResult.get(key);
            // source has been segmented, but still one segment.
            if (segmentedSrc != null && segmentedSrc.size() == 1)
            {
                segments.add(xliffChunk);
            }
            // source has been segmented, become to multiple segments, set
            // target as empty. This will impact exported target if not
            // translated, but to be simple, this is acceptable.
            else if (segmentedSrc != null && segmentedSrc.size() > 1)
            {
                for (int i = 0; i < segmentedSrc.size(); i++)
                {
                    segments.add(" ");
                }
            }
            // source is not segmented.
            else
            {
                segments.add(xliffChunk);
            }
        }

        return segments;
    }
}
