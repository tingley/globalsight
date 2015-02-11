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

import com.globalsight.ling.docproc.Segmenter;

import com.globalsight.everest.segmentationhelper.*;

import com.globalsight.ling.common.LocaleCreater;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.util.edit.EditUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

//jakarta regexp package
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;


/**
 * Takes unsegmented GXML input and produces a segmented version based
 * on the locale.  We also do a bit of TMX cleanup so we are
 * conformant to the TMX spec.
 *
 * <p>Mon Dec 16 21:11:14 2002 Now handles TMX tags representing
 * whitespace correctly during segmentation.</p>
 *
 * <p>NOTE: EACH EXTRACTOR NEEDS TO OUTPUT THE TYPE ATTRIBUTE OF A TMX
 * TAG IN A TRANSLATABLE STRING BEFORE ANY OTHER ATTRIBUTES FOR THE
 * REGULAR EXPRESSIONS BELOW TO WORK.</p>
 */
public class DiplomatSegmenter
{
    //
    // Member Variables
    //

    private boolean m_preserveWhitespace = false;

    private Output m_output = null;
    private XmlEntities m_codec = null;
    private Segmenter m_segmenter = null;

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
    // <p><b><span>Press Release No. [<BR>]<span style='mso-tab-count:1'> </span></span></b><span>IFC Corporate Relations</span></p>
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
                "<bpt[^>]+>[^<]*(<[^>]*>[^<]*</[^>]*>[^<]*)*</bpt>([:space:]*<(ph|it)[^>]+>[^<]*(<[^>]*>[^<]*</[^>]*>[^<]*)*</\\3>)*[:space:]*$",
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
     * <p>Takes GXML input from an <code>Output</code> object,
     * segments each "TranslatableElement" node and keeps the result
     * in an internal <code>Output</code>object.
     *
     * <p>The result can be retrieved as the Output object itself
     * (<code>getOutput()</code>), or as string
     * (<code>getDiplomatXml()</code>).
     */
    public void segment(Output p_diplomat)
        throws DiplomatSegmenterException
    {
        m_output = p_diplomat;

        doSegmentation();
    }
    
    public void segmentXliff(Output p_diplomat)
        throws DiplomatSegmenterException
    {
        isXliff = true;
        m_output = p_diplomat;

        doSegmentation();
    }
    
    /**
     * <p>Takes GXML input from an <code>Output</code> object,
     * segments each "TranslatableElement" node according to
     * setmenatation rule and keeps the result
     * in an internal <code>Output</code>object.
     *
     * <p>The result can be retrieved as the Output object itself
     * (<code>getOutput()</code>), or as string
     * (<code>getDiplomatXml()</code>).
     */
    public void segment(Output p_diplomat, String p_segmentationRuleText)
        throws DiplomatSegmenterException,Exception
    {
        m_output = p_diplomat;

        doSegmentation(p_segmentationRuleText);
    }

    /**
     * Takes GXML input from a string.  Converts it to our internal
     * data structure and then performs segmentation on each
     * "TranslatableElement" node.
     */
    public String segment(String p_gxml)
        throws DiplomatSegmenterException
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
     * Takes GXML input from a string.  Converts it to our internal
     * data structure and then performs segmentation according to
     * segmentation rule on each
     * "TranslatableElement" node.
     */
    public String segment(String p_gxml, String p_ruleText)
        throws DiplomatSegmenterException,Exception
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
    private void convertToOutput(String p_gxml)
        throws DiplomatReaderException
    {
        DiplomatReader reader = new DiplomatReader(p_gxml);
        m_output = reader.getOutput();
    }

    /**
     * Walks an internal Output object and segments translatable nodes.
     */
    private void doSegmentation()
        throws DiplomatSegmenterException
    {
        Locale locale = LocaleCreater.makeLocale(m_output.getLocale());
        m_segmenter = new Segmenter(locale);

        for (Iterator it = m_output.documentElementIterator(); it.hasNext(); )
        {
            DocumentElement de = (DocumentElement)it.next();

            switch (de.type())
            {
            case DocumentElement.TRANSLATABLE:
            {
                TranslatableElement elem = (TranslatableElement)de;
                ArrayList segments = new ArrayList();
                
                if(isXliff) {
                    String xliffChunk = elem.getChunk();
                    segments.add(xliffChunk);
                }
                else {
                    segments = segment(elem);
                }
                
                addSegmentsToNode(elem, segments);

                elem.setChunk(null);

                break;
            }
            case DocumentElement.LOCALIZABLE:
            {
                LocalizableElement elem = (LocalizableElement)de;
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
     * Walks an internal Output object and segments translatable nodes
     * according to segmentation rule.
     */
    private void doSegmentation(String p_segmentationRuleText)
        throws DiplomatSegmenterException, Exception
    {
        // Locale locale = LocaleCreater.makeLocale(m_output.getLocale());
        // m_segmenter = new Segmenter(locale);
        // String loc = locale.getLanguage() + "_" + locale.getCountry();
    	String loc = m_output.getLocale();
        SegmentationRule srx = XmlLoader.loaderSegmentationRule(p_segmentationRuleText);           

        for (Iterator it = m_output.documentElementIterator(); it.hasNext(); )
        {
            DocumentElement de = (DocumentElement)it.next();

            switch (de.type())
            {
            	case DocumentElement.TRANSLATABLE:
            	{
            		TranslatableElement elem = (TranslatableElement)de;

            		ArrayList segments = segment(elem, srx, loc);
            		addSegmentsToNode(elem, segments);

            		elem.setChunk(null);

            		break;
            	}
            	case DocumentElement.LOCALIZABLE:
            	{
            		LocalizableElement elem = (LocalizableElement)de;
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
     * Segments a single Translatable node according to
     * segmentation rule by making a list of offsets
     * of segment breaks.  Also remembers tag lengths in the TagNode
     * list <code>m_tagPositions</code>.
     */
    private ArrayList segment(TranslatableElement p_node, 
    		SegmentationRule p_rule, String p_locale)
        throws DiplomatSegmenterException, Exception
    {
        m_segment = p_node.getChunk();
        String type = p_node.getDataType();
        if ( type != null && "javascript".equalsIgnoreCase(type)) {
            m_segment = EditUtil.encodeNTREntities(new StringBuffer(m_segment));        	
        }
        m_tagPositions.clear();

        // Remove all TMX+native tags and store them for later.
        m_segmentWithoutTags = removeTags(m_segment);


        ArrayList breakPositions = new ArrayList();
        
        Segmentation segmentation = new Segmentation();
        // Set locale used by text to be segmented. 
	    segmentation.setLocale(p_locale);
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
        ArrayList segments = splitSegmentWithRule(m_segment, breakPositions);

        // Finally move a few tags around and make broken bpt/epb isolated.
        segments = fixTmxTags(segments);

        return segments;
    }

    /**
     * Segments a single Translatable node by making a list of offsets
     * of segment breaks.  Also remembers tag lengths in the TagNode
     * list <code>m_tagPositions</code>.
     */
    private ArrayList segment(TranslatableElement p_node)
        throws DiplomatSegmenterException
    {
        m_segment = p_node.getChunk();
        m_tagPositions.clear();

        // Remove all TMX+native tags and store them for later.
        m_segmentWithoutTags = removeTags(m_segment);

        // Do Locale sensitive sentence breaking.
        m_segmenter.setText(m_segmentWithoutTags);

        ArrayList breakPositions = new ArrayList();

        // System.err.println("::: `" + m_segmentWithoutTags + "'");

        int iStart = m_segmenter.first();
        for (int iEnd = m_segmenter.next();
             iEnd != Segmenter.DONE;
             iStart = iEnd, iEnd = m_segmenter.next())
        {
            // System.err.println("--> `" +
            // m_segmentWithoutTags.substring(iStart, iEnd) + "'");

            BreakPosition pos = new BreakPosition(iEnd);
            breakPositions.add(pos);
        }

        // Now go back and split the string with tags into segments.
        ArrayList segments = splitSegment(m_segment, breakPositions);

        // Finally move a few tags around and make broken bpt/epb isolated.
        segments = fixTmxTags(segments);

        return segments;
    }
    
    /**
     * Splits the original segment into segments at the specified
     * break positions.  Takes into account intervening TMX tags.
     */
    private ArrayList splitSegmentWithRule(String p_segment, ArrayList p_breaks)
    {
        ArrayList result = new ArrayList();

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
        for (Iterator it = p_breaks.iterator(); it.hasNext(); )
        {
            Integer pos = (Integer)it.next();

            iEndWithoutTags = pos.intValue();

            //System.err.println("Old: " + iStartWithoutTags + ":" +
            //iEndWithoutTags + " `" +
            //m_segmentWithoutTags.substring(iStartWithoutTags,
            //iEndWithoutTags) + "'");

            String text = m_segmentWithoutTags.substring(
                iStartWithoutTags, iEndWithoutTags);

            int iIncrease = m_codec.encodeStringBasic(text).length() -
                (iEndWithoutTags - iStartWithoutTags);

            int iTagLen = previousTagLengths(
                pos.intValue() + iIncrease + iTotalIncrease);

            iEnd = pos.intValue() + iIncrease + iTotalIncrease +
                iTagLen + iTotalTagLen;

            iEnd += m_globalAdjust;

            //System.err.println("start=" + iStart + " increase=" + iIncrease +
            //" taglen=" + iTagLen + " end=" + iEnd);

            //System.err.println("New: " + iStart + ":" + iEnd + " `" +
            //m_segment.substring(iStart, iEnd) + "'");

            result.add(m_segment.substring(iStart, iEnd));

            iTotalIncrease += iIncrease;
            iTotalTagLen += iTagLen;

            iStart = iEnd;
            iStartWithoutTags = iEndWithoutTags;
        }

        return result;
    }

    /**
     * Splits the original segment into segments at the specified
     * break positions.  Takes into account intervening TMX tags.
     */
    private ArrayList splitSegment(String p_segment, ArrayList p_breaks)
    {
        ArrayList result = new ArrayList();

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
        for (Iterator it = p_breaks.iterator(); it.hasNext(); )
        {
            BreakPosition pos = (BreakPosition)it.next();

            iEndWithoutTags = pos.m_split;

            //System.err.println("Old: " + iStartWithoutTags + ":" +
            //iEndWithoutTags + " `" +
            //m_segmentWithoutTags.substring(iStartWithoutTags,
            //iEndWithoutTags) + "'");

            String text = m_segmentWithoutTags.substring(
                iStartWithoutTags, iEndWithoutTags);

            int iIncrease = m_codec.encodeStringBasic(text).length() -
                (iEndWithoutTags - iStartWithoutTags);

            int iTagLen = previousTagLengths(
                pos.m_split + iIncrease + iTotalIncrease);

            iEnd = pos.m_split + iIncrease + iTotalIncrease +
                iTagLen + iTotalTagLen;

            iEnd += m_globalAdjust;

            //System.err.println("start=" + iStart + " increase=" + iIncrease +
            //" taglen=" + iTagLen + " end=" + iEnd);

            //System.err.println("New: " + iStart + ":" + iEnd + " `" +
            //m_segment.substring(iStart, iEnd) + "'");
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
     * Moves <bpt> tags at the end of segments to the beginning of the
     * next.
     *
     * Then converts all <bpt>, <ept> tags to <it> tags if they were
     * split by segment boundaries.
     */
    private ArrayList fixTmxTags(ArrayList p_segments)
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
            String input = (String)p_segments.get(i);
            // System.err.println("STARTTAG_AT_END? " + input);

            while (m_startTagsAtEnd.match(input))
            {
                // Move the tags to the beginning of the next segment
                segment = (String) p_segments.get(i+1);
                p_segments.set(i + 1, m_startTagsAtEnd.getParen(0) + segment);

                // remove the matched tags from the segment
                segment = (String) p_segments.get(i);
                offset = m_startTagsAtEnd.getParenStart(0);
                p_segments.set(i, segment.substring(0, offset));

                // reset loop variable
                input = (String)p_segments.get(i);

                // System.err.println("seg i  : " + (String)p_segments.get(i));
                // System.err.println("seg i+1: " + (String)p_segments.get(i+1) + "\n");
            }
        }

        // Convert all <bpt>, <ept> tags to <it> tags if they were
        // split by segment boundaries.
        Hashtable tag_index = new Hashtable();

        for (int i = 0; i < p_segments.size(); ++i)
        {
            int start = 0;
            String input = (String) p_segments.get(i);
            tag_index.clear();

            while (m_matchAllBptEpt.match(input, start))
            {
                String key = m_matchAllBptEpt.getParen(2);

                if (tag_index.get(key) == null)
                {
                    tag_index.put(key, m_matchAllBptEpt.getParen(1).equals("bpt") ?
                        "begin" : "end");
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
            String replaced = (String)p_segments.get(i);
            Enumeration keys = tag_index.keys();

            while (keys.hasMoreElements())
            {
                String key = (String)keys.nextElement();
                String value = (String)tag_index.get(key);
                RE matchAllBptEpt = null;

                try
                {
                    matchAllBptEpt =
                        new RE("<(bpt|ept)[:space:]+([^>]*)i=\"" +
                            key + "\"([^>]*)>((.|[:space:])+?)</\\1>",
                            RE.MATCH_NORMAL);
                }
                catch (RESyntaxException e) // SNH (Should Not Happen)
                {
                    System.err.println(e.toString());
                }

                start = 0;
                while (matchAllBptEpt.match(replaced, start))
                {
                    String substitute = "<it " + matchAllBptEpt.getParen(2) +
                        "pos=\"" + value + "\" " +
                        "x=\"" + key + "\" i=\"" + key + "\"" +
                        matchAllBptEpt.getParen(3) + ">" +
                        matchAllBptEpt.getParen(4) + "</it>";

                    // System.err.println("M--> " + matchAllBptEpt.getParen(0));
                    // System.err.println("S--> " + substitute);

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
     * Calculates the lengths of all intervening TMX tags between
     * beginning of segments and current position.
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
            TagPosition pos = (TagPosition)m_tagPositions.remove(0);

            //System.err.println("Split at " + p_iSplitPoint +
            //" pos.m_offset = " + pos.m_offset +
            //" (adjust=" + pos.m_adjust + ")" +
            //" pos.m_length = " + pos.m_length +
            //"\n\ttag = `" + pos.m_tag + "'");

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
     * Removes all TMX tags and TMX content from the given string,
     * returning the pure text.
     *
     * As a side effect, populates m_tagPositions with positions where
     * tags have been in the original string.
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
            //System.err.println("TMX tag type = " + type);

            // Replace tags representing whitespace with actual white
            // space during segmentation. Always do this for MS Office
            // whitespace, but for backwards-compatibility, do it for
            // other tags only if demanded
            // (segmentation_preserve_whitespace=true in
            // Diplomat.properties).
            if (type != null)
            {
                if (Text.isTmxMsoWhitespaceNode(type) ||
                    (m_preserveWhitespace && Text.isTmxWhitespaceNode(type)))
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
            TagPosition pos = (TagPosition)m_tagPositions.get(i);

            String replace = "";

            if (pos.m_adjust == 1)
            {
                replace = " ";
            }

            m_segmentWithoutTags = m_tmxTags.subst(
                m_segmentWithoutTags, replace, RE.REPLACE_FIRSTONLY);
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
        ArrayList p_segments)
        throws DiplomatSegmenterException
    {
        for (Iterator it = p_segments.iterator(); it.hasNext(); )
        {
            String segment = (String)it.next();
            if (segment != null)
            {
                p_element.addSegment(new SegmentNode(segment));
            }
        }
    }

    /**
     * Updates the original localizable node.
     */
    private void updateSegmentInNode(LocalizableElement p_element,
        String p_loc)
        throws DiplomatSegmenterException
    {
        if (p_loc != null)
        {
            p_element.setChunk(p_loc);
        }
    }
}
