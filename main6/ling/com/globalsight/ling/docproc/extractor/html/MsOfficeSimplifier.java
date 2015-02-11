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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.globalsight.ling.common.Text;

/**
 * Simplifies Word-HTML paragraphs extracted by the HTML Extractor.
 * 
 * <P>
 * If more simplifiers are needed, an abstract base class should be implemented
 * with simplify() calling beforeSimplify() and afterSimplify() (both assigning
 * pairing status), and an abstract method doSimplify().
 * </P>
 * 
 * This class segments a Word-HTML paragraph on TAB boundaries, the result are
 * lists of HtmlObjects: beforePara, seg1, betweenPara, seg2, betweenPara, ...,
 * segN, afterPara.
 */
public class MsOfficeSimplifier
{
    private static int s_SKELETON = 0;
    private static int s_SEGMENT = 1;

    private ExtractionRules m_rules;
    private List<HtmlObjects.HtmlElement> m_before;
    private List<HtmlObjects.HtmlElement> m_tags;
    private List<HtmlObjects.HtmlElement> m_after;

    // The final result: a list of lists of HtmlObjects.
    private List<List<HtmlObjects.HtmlElement>> m_tagLists = new ArrayList<List<HtmlObjects.HtmlElement>>();

    // For PPT issue
    private Set<String> m_bulletStyleSet = new HashSet<String>();

    //
    // Constructors
    //

    public MsOfficeSimplifier(ExtractionRules p_rules,
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        m_rules = p_rules;
        m_before = p_before;
        m_tags = p_tags;
        m_after = p_after;
    }

    //
    // Public Methods
    //

    /**
     * For PPT issue
     */
    public void setBulletStyleSet(Set<String> bulletStyleSet)
    {
        m_bulletStyleSet = bulletStyleSet;
    }

    /**
     * Returns the final result of simplification, a list of lists of
     * HtmlObjects. The result lists should be written to skeleton, segments,
     * skeleton, ... skeleton.
     */
    public List<List<HtmlObjects.HtmlElement>> getTagLists()
    {
        return m_tagLists;
    }

    /**
     * Simplifies a Word-HTML paragraph by moving tags/text to leading/trailing
     * skeleton and paragraph-segmenting the rest, introducing intervening
     * skeleton sections.
     */
    public void simplify()
    {
        // !!! Caller should have removed styles on B/I.

        // Simplify the raw paragraph.
        simplifyPass1();

        if (containsTabs())
        {
            segmentByTabs();

            // Simplify the segmented paragraph some more.
            simplifyPass2();
        }
        else
        {
            segmentByNothing();
        }
    }

    //
    // Private Methods
    //

    /**
     * Simplifies an entire paragraph (before tabs).
     */
    private void simplifyPass1()
    {
        reAssignPairingStatus(m_tags);

        // Remove outer formatting (isolated and paired around
        // segment), move hidden text into skeleton as well.
        removeOuterFormatting(m_before, m_tags, m_after);

        // Remove Word footnote and endnote markers from beginning of segment.
        removeReferenceMarkers(m_before, m_tags);

        // Remove PowerPoint bullets which are the first character and
        // use Wingdings fonts.
        removePowerpointBullets(m_before, m_tags);

        // Remove PowerPoint lastCR spans.
        removeLastCR(m_tags, m_after);

        convertSpaceRuns();

        // For PPT issues
        convertBlankBullet(m_after);

        // Remove spans with unnecessary styles.
        removeUselessSpans();

        // Remove revision markers when they were ON accidentally.
        removePropRevisionMarkers();
        // Cleanup INS/DEL revisions similarly.
        removeDelRevisions();
        applyInsRevisions();

        // Remove empty spans that are created from
        // superfluous original formatting in Word (<span
        // color=blue></span>).
        removeEmptyFormatting();

        // Remove outer formatting (isolated and paired around
        // segment), move hidden text into skeleton as well.
        removeOuterFormatting(m_before, m_tags, m_after);

        reAssignPairingStatus(m_tags);
    }

    /**
     * Simplifies m_tagsList, which is the raw paragraph segmented by tabs:
     * skel,seg,skel,seg,...,skel.
     */
    private void simplifyPass2()
    {
        boolean b_changed = true;

        pass2: while (b_changed)
        {
            int state = s_SKELETON;

            for (int i = 0, max = m_tagLists.size(); i < max; i++)
            {
                List<HtmlObjects.HtmlElement> tags = m_tagLists.get(i);

                if (state == s_SKELETON)
                {
                    state = s_SEGMENT;
                    continue;
                }

                List<HtmlObjects.HtmlElement> before = m_tagLists.get(i - 1);
                List<HtmlObjects.HtmlElement> after = m_tagLists.get(i + 1);

                reAssignPairingStatus(tags);

                // Moves all outer tags and hidden text (isolated or not)
                // to skeleton.
                removeOuterFormatting(before, tags, after);

                reAssignPairingStatus(tags);

                // No segment text left, collapse skeletons.
                if (tags.size() == 0)
                {
                    before.addAll(after);
                    m_tagLists.remove(i + 1);
                    m_tagLists.remove(i);

                    continue pass2;
                }

                state = s_SKELETON;
                continue;
            }

            b_changed = false;
        }
    }

    /**
     * Converts a raw paragraph to the internal result structure (skel, tags,
     * skel).
     */
    private void segmentByNothing()
    {
        m_tagLists.add(m_before);
        m_tagLists.add(m_tags);
        m_tagLists.add(m_after);

        // Debugging aid. m_tags is invalid from now on.
        m_tags = m_before = m_after = null;
    }

    /**
     * Converts a raw paragraph to the internal result structure (skel, tags,
     * skel) based on TAB boundaries.
     */
    private void segmentByTabs()
    {
        int state = s_SKELETON;
        int start = 0;
        int end = m_tags.size();
        List<HtmlObjects.HtmlElement> list;

        list = m_before;
        m_tagLists.add(m_before);
        state = s_SKELETON;
        Stack<HtmlObjects.HtmlElement> spanStack = new Stack<HtmlObjects.HtmlElement>();
        boolean doNotTranslate = false;

        while (start < end)
        {
            Object o = m_tags.get(start);

            if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                if (isUntranslatableSpan(tag))
                {
                    spanStack.push(tag);
                    doNotTranslate = true;
                }

                // Paragraph break condition.
                // do not break if using content post-filter
                if (!doNotTranslate && isMsoTabCount(tag))
                {
                    if (state == s_SEGMENT)
                    {
                        list = new ArrayList<HtmlObjects.HtmlElement>();
                        m_tagLists.add(list);
                        state = s_SKELETON;
                    }

                    list.add(tag);

                    endtag: while (++start < end)
                    {
                        Object o1 = m_tags.get(start);

                        list.add((HtmlObjects.HtmlElement) o1);

                        if (o1 instanceof HtmlObjects.EndTag)
                        {
                            break endtag;
                        }
                    }
                }
                else
                {
                    if (state == s_SKELETON)
                    {
                        list = new ArrayList<HtmlObjects.HtmlElement>();
                        m_tagLists.add(list);
                        state = s_SEGMENT;
                    }

                    list.add((HtmlObjects.HtmlElement) o);
                }
            }
            else
            {
                if (o instanceof HtmlObjects.EndTag)
                {
                    HtmlObjects.EndTag tag = (HtmlObjects.EndTag) o;
                    if (!spanStack.isEmpty()
                            && ((HtmlObjects.Tag) spanStack.peek()).partnerId == tag.partnerId)
                    {
                        spanStack.pop();
                        doNotTranslate = (!spanStack.isEmpty());
                    }
                }

                if (state == s_SKELETON)
                {
                    list = new ArrayList<HtmlObjects.HtmlElement>();
                    m_tagLists.add(list);
                    state = s_SEGMENT;
                }

                list.add((HtmlObjects.HtmlElement) o);
            }

            start++;
        }

        if (state == s_SKELETON)
        {
            list.addAll(m_after);
        }
        else
        {
            m_tagLists.add(m_after);
        }

        // Debugging aid. m_tags is invalid from now on.
        m_tags = m_before = m_after = null;
    }

    private void reAssignPairingStatus(List<HtmlObjects.HtmlElement> p_tags)
    {
        clearPairingStatus(p_tags);
        assignPairingStatus(p_tags);
    }

    private void clearPairingStatus(List<HtmlObjects.HtmlElement> p_tags)
    {
        for (int i = 0, max = p_tags.size(); i < max; i++)
        {
            HtmlObjects.HtmlElement t = p_tags.get(i);

            t.isIsolated = false;
            t.isPaired = false;
            t.partnerId = -1;
        }
    }

    /**
     * <p>
     * Walk through a segment and mark each pairable tag without buddy as
     * isolated. The tags' boolean members m_bPaired and m_bIsolated are false
     * by default.
     */
    private void assignPairingStatus(List<HtmlObjects.HtmlElement> p_tags)
    {
        List<HtmlObjects.HtmlElement> tags = new ArrayList<HtmlObjects.HtmlElement>(
                p_tags);
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
                if (m_rules.isPairedTag(t_start.tag))
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
                if (m_rules.isPairedTag(t_CFstart.tag))
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
     * Moves initial Word footnote and endnote markers into the m_before list.
     */
    private void removeReferenceMarkers(List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags)
    {
        // usually there are 6 elements making up the marker:
        // <a><span><span>[optional marker char removed]</span></span></a>
        if (p_tags.size() >= 6)
        {
            Object o1 = p_tags.get(0);

            if (o1 instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t1 = (HtmlObjects.Tag) o1;

                // <a style='mso-endnote-id:edn1'>...</a>
                if (t1.isPaired && !t1.isIsolated
                        && isMsoReferenceMarker((HtmlObjects.Tag) o1))
                {
                    while (true)
                    {
                        o1 = p_tags.remove(0);
                        p_before.add((HtmlObjects.HtmlElement) o1);

                        if (o1 instanceof HtmlObjects.EndTag
                                && isMsoEndReference((HtmlObjects.EndTag) o1))
                        {
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Moves initial PowerPoint bullets (those with wingding/webding fonts) into
     * the m_before list.
     * 
     * There are 3 cases in total to consider: bullets with 1 span around them,
     * with 2, and with 3 (these are hard).
     */
    private void removePowerpointBullets(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags)
    {
        // For PPT issue
        removeAllPowerpointBullets(p_before, p_tags);
    }

    private void removeAllPowerpointBullets(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags)
    {
        if (p_tags.size() < 1)
            return;

        List<HtmlObjects.HtmlElement> tagList = new ArrayList<HtmlObjects.HtmlElement>();
        if (p_before.size() > 0)
        {
            tagList.add(p_before.get(p_before.size() - 1));
        }
        tagList.addAll(p_tags);

        List<HtmlObjects.HtmlElement> removedBullets = new ArrayList<HtmlObjects.HtmlElement>();
        for (int i = 1; i < tagList.size(); i++)
        {
            Object o1 = tagList.get(i - 1);
            Object o2 = tagList.get(i);
            Object o3 = null;

            try
            {
                o3 = tagList.get(i + 1);
            }
            catch (IndexOutOfBoundsException e)
            {
                o3 = null;
            }

            if (o2 instanceof HtmlObjects.EndTag)
            {
                i++;
            }
            else if ((o1 instanceof HtmlObjects.Tag)
                    && (o2 instanceof HtmlObjects.Text))
            {
                removedBullets.addAll(judgeRemovedBullets(o1, o2, o3));
                i++;
            }
        }
        p_before.addAll(removedBullets);
        p_tags.removeAll(removedBullets);
    }

    private List<HtmlObjects.HtmlElement> judgeRemovedBullets(Object o1,
            Object o2, Object o3)
    {
        List<HtmlObjects.HtmlElement> removedBullets = new ArrayList<HtmlObjects.HtmlElement>();
        if ((o1 == null) || (o2 == null))
            return removedBullets;

        if (o1 instanceof HtmlObjects.Tag && o2 instanceof HtmlObjects.Text
                && ((HtmlObjects.Tag) o1).isPaired
                && ((HtmlObjects.Text) o2).toString().length() > 0
                && isBulletSpan((HtmlObjects.Tag) o1))
        {
            if (o3 != null
                    && o3 instanceof HtmlObjects.EndTag
                    && ((HtmlObjects.EndTag) o3).isPaired
                    && (((HtmlObjects.Tag) o1).partnerId == ((HtmlObjects.EndTag) o3).partnerId))
            {
                removedBullets.add((HtmlObjects.Tag) o1);
                removedBullets.add((HtmlObjects.Text) o2);
                removedBullets.add((HtmlObjects.EndTag) o3);
            }
            else
            {
                removedBullets.add((HtmlObjects.Text) o2);
            }
        }

        return removedBullets;
    }

    /**
     * Moves trailing PowerPoint lastCR spans into the m_after list.
     * 
     * <span style='text-shadow:none;...;
     * mso-special-format:lastCR;display:none'>&#13;</span>
     */
    private void removeLastCR(List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        if (p_tags.size() >= 3)
        {
            Object o1 = p_tags.get(p_tags.size() - 3);
            Object o2 = p_tags.get(p_tags.size() - 2);
            Object o3 = p_tags.get(p_tags.size() - 1);

            if (o1 instanceof HtmlObjects.Tag && o2 instanceof HtmlObjects.Text
                    && o3 instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.Tag t1 = (HtmlObjects.Tag) o1;
                HtmlObjects.Text t2 = (HtmlObjects.Text) o2;
                HtmlObjects.EndTag t3 = (HtmlObjects.EndTag) o3;

                // First and third tag must be paired, text must be &#13;.
                if (t1.isPaired && t3.isPaired && t1.partnerId == t3.partnerId
                        && t2.toString().length() == 1 && isLastCR(t1))
                {
                    p_after.add(0, (HtmlObjects.EndTag) o3);
                    p_after.add(0, (HtmlObjects.Text) o2);
                    p_after.add(0, (HtmlObjects.Tag) o1);

                    p_tags.remove(p_tags.size() - 1);
                    p_tags.remove(p_tags.size() - 1);
                    p_tags.remove(p_tags.size() - 1);
                }
            }
        }
    }

    private void convertBlankBullet(List<HtmlObjects.HtmlElement> p_after)
    {
        if (p_after.size() < 3)
            return;

        Object o1 = null;
        Object o2 = null;
        Object o3 = null;
        for (int i = 1; i < p_after.size() - 1; i++)
        {
            o2 = p_after.get(i);
            if (!(o2 instanceof HtmlObjects.Text))
            {
                continue;
            }

            // o1 - Tag
            // o2 - Text
            // o3 - EndTag
            o1 = p_after.get(i - 1);
            o3 = p_after.get(i + 1);

            if (o1 instanceof HtmlObjects.Tag && o2 instanceof HtmlObjects.Text
                    && o3 instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.Tag t1 = (HtmlObjects.Tag) o1;
                HtmlObjects.Text t2 = (HtmlObjects.Text) o2;
                HtmlObjects.EndTag t3 = (HtmlObjects.EndTag) o3;

                // First and third tag must be paired, text must be &#13;.
                if (t1.isPaired && t3.isPaired && t1.partnerId == t3.partnerId
                        && t2.text.equalsIgnoreCase("\r"))
                {
                    t2.text = "&#13;";
                }
            } // end if
        } // end for
    }

    /**
     * Convert runs of whitespace to single whitespace if they are smaller than
     * a magic number (2 or 3).
     * 
     * Two spaces often occur in en_US documents after a period (old grammar
     * rule), or by mistake. Three spaces are most often a mistake, especially
     * after a period or semicolon, but 4, 5, and more are most likely
     * intentional - so these get preserved.
     * 
     * Note that spaces can optionally be converted to nbsp, although it's then
     * up to the translator to see the nbsps in the editor and not make any
     * mistakes when translating them.
     */
    private void convertSpaceRuns()
    {
        StringBuffer buf = new StringBuffer();

        boolean b_changed = true;
        spacerun: while (b_changed)
        {
            int i_start = 0, i_end = 0;

            // Must consider at least 3 elements: <span>content</span>.
            while (i_start < m_tags.size() - 3)
            {
                Object o = m_tags.get(i_start);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;

                    if (isMsoSpaceRun(tag))
                    {
                        buf.setLength(0);

                        i_end = i_start;

                        while (i_end < m_tags.size())
                        {
                            Object o1 = m_tags.get(i_end);

                            if (o1 instanceof HtmlObjects.Text
                                    || o1 instanceof HtmlObjects.Newline)
                            {
                                buf.append(o1.toString());
                            }
                            else if (o1 instanceof HtmlObjects.EndTag)
                            {
                                break;
                            }

                            i_end++;
                        }

                        String content = buf.toString();
                        if (countNbsp(content) <= 3
                                && (content.indexOf("&nbsp;") < 0)) // 3 is
                                                                    // magic
                        {
                            m_tags.subList(i_start, i_end + 1).clear();
                            m_tags.add(i_start, new HtmlObjects.Text(" "));

                            continue spacerun;
                        }
                    }
                }

                i_start++;
            }

            b_changed = false;
        }
    }

    /**
     * Removes property revisions: &lt;span class=msoProChange&gt; and the
     * closing tag.
     */
    private void removePropRevisionMarkers()
    {
        boolean b_changed = true;

        propchange: while (b_changed)
        {
            for (int i = 0; i < m_tags.size(); i++)
            {
                Object o = m_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("span")
                            && original.indexOf("class=msoChangeProp") >= 0
                            && tag.isPaired)
                    {
                        m_tags.remove(i);
                        removeClosingTag(tag);

                        continue propchange;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes DEL revisions: &lt;span class=msoDel&gt; and text up to and
     * including the closing tag.
     */
    private void removeDelRevisions()
    {
        boolean b_changed = true;

        deltags: while (b_changed)
        {
            for (int i = 0; i < m_tags.size(); i++)
            {
                Object o = m_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("span")
                            && original.indexOf("class=msoDel") >= 0
                            && tag.isPaired)
                    {
                        removeTagAndContent(tag);

                        continue deltags;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes INS revisions: &lt;span class=msoIns&gt; followed by &lt;INS&gt;,
     * </INS> and the closing &lt;/span&gt;.
     */
    private void applyInsRevisions()
    {
        boolean b_changed = true;

        instags: while (b_changed)
        {
            for (int i = 0; i < m_tags.size(); i++)
            {
                Object o = m_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;
                    String original = tag.original;

                    if (tag.tag.equalsIgnoreCase("span")
                            && original.indexOf("class=msoIns") >= 0
                            && tag.isPaired)
                    {
                        removeInsTag(tag);

                        continue instags;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes specific spans that are deemed useless, e.g. &lt;span
     * style='layout-grid-mode:line'&gt;.
     */
    private void removeUselessSpans()
    {
        boolean b_changed = true;

        useless: while (b_changed)
        {
            for (int i = 0; i < m_tags.size(); i++)
            {
                Object o = m_tags.get(i);

                if (o instanceof HtmlObjects.Tag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o;

                    if (isUselessSpan(tag) && tag.isPaired)
                    {
                        m_tags.remove(i);
                        removeClosingTag(tag);

                        continue useless;
                    }
                }
            }

            b_changed = false;
        }
    }

    /**
     * Removes empty formatting, i.e. formatting that surrounds no text at all.
     * 
     * TODO: ensure that bookmarks and other markers are not removed.
     */
    private void removeEmptyFormatting()
    {
        boolean b_changed = true;

        emptyformatting: while (b_changed)
        {
            for (int i = 0; i < m_tags.size() - 1; i++)
            {
                Object o1 = m_tags.get(i);
                Object o2 = m_tags.get(i + 1);

                if (o1 instanceof HtmlObjects.Tag
                        && o2 instanceof HtmlObjects.EndTag)
                {
                    HtmlObjects.Tag tag = (HtmlObjects.Tag) o1;
                    HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o2;

                    if (tag.isPaired && etag.isPaired
                            && tag.partnerId == etag.partnerId
                            && !isMsoSpecialCharacter(tag))
                    {
                        m_tags.remove(i + 1);
                        m_tags.remove(i);

                        continue emptyformatting;
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
    private void removeClosingTag(HtmlObjects.Tag p_tag)
    {
        for (int i = 0, max = m_tags.size(); i < max; i++)
        {
            Object o = m_tags.get(i);

            if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o;

                if (p_tag.tag.equalsIgnoreCase(etag.tag) && p_tag.isPaired
                        && etag.isPaired && p_tag.partnerId == etag.partnerId)
                {
                    m_tags.remove(i);
                    return;
                }
            }
        }
    }

    /**
     * Removes a Word insert revision by removing a starting &lt;span
     * class=msoIns&gt;&lt;ins&gt; tag + counterparts, and leaving the contents
     * in place.
     */
    private void removeInsTag(HtmlObjects.Tag p_tag)
    {
        int i_start = 0;
        int i_end = 0;

        loop: for (int i = 0, max = m_tags.size(); i < max; i++)
        {
            Object o = m_tags.get(i);

            if (o == p_tag)
            {
                i_start = i;

                for (int j = i + 1; j < max; j++)
                {
                    Object o1 = m_tags.get(j);

                    if (o1 instanceof HtmlObjects.EndTag)
                    {
                        HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o1;

                        if (p_tag.tag.equalsIgnoreCase(etag.tag)
                                && p_tag.isPaired && etag.isPaired
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
            m_tags.subList(i_end - 1, i_end + 1).clear();
            m_tags.subList(i_start, i_start + 2).clear();
        }
    }

    /**
     * Removes a tag and its content between the endtag (which must exist and be
     * paired with the start).
     */
    private void removeTagAndContent(HtmlObjects.Tag p_tag)
    {
        int i_start = 0;
        int i_end = 0;

        loop: for (int i = 0, max = m_tags.size(); i < max; i++)
        {
            Object o = m_tags.get(i);

            if (o == p_tag)
            {
                i_start = i;

                for (int j = i + 1; j < max; j++)
                {
                    Object o1 = m_tags.get(j);

                    if (o1 instanceof HtmlObjects.EndTag)
                    {
                        HtmlObjects.EndTag etag = (HtmlObjects.EndTag) o1;

                        if (p_tag.tag.equalsIgnoreCase(etag.tag)
                                && p_tag.isPaired && etag.isPaired
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
            m_tags.subList(i_start, i_end + 1).clear();
        }
    }

    /**
     * Detects and removes SPAN formatting tags inserted by MS Office that
     * surrounds the current segment (leaving B/I/U in place unless isolated).
     * Also removes hidden text.
     */
    protected void removeOuterFormatting(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        boolean b_changed = true;

        while (b_changed && p_tags.size() > 0)
        {
            b_changed = false;

            b_changed |= removeLeadingFormatting(p_before, p_tags, p_after);
            b_changed |= removeTrailingFormatting(p_before, p_tags, p_after);
            b_changed |= removeOuterPairedFormatting(p_before, p_tags, p_after);

            if (!isExcelExtractor())
            {
                b_changed |= removeLeadingHiddenText(p_before, p_tags, p_after);
                b_changed |= removeTrailingHiddenText(p_before, p_tags, p_after);
            }

        }
    }

    protected boolean removeLeadingFormatting(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        boolean result = false;
        boolean b_changed = true;

        loop_before: while (b_changed && p_tags.size() > 0)
        {
            Object o = p_tags.get(0);

            if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag tag = (HtmlObjects.Tag) o;

                if (/* tag.tag.equalsIgnoreCase("span") || */tag.isIsolated)
                {
                    p_before.add(tag);
                    p_tags.remove(0);

                    result = true;
                    continue loop_before;
                }
            }
            else if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag tag = (HtmlObjects.EndTag) o;

                if (/* tag.tag.equalsIgnoreCase("span") || */tag.isIsolated)
                {
                    p_before.add(tag);
                    p_tags.remove(0);

                    result = true;
                    continue loop_before;
                }
            }
            else if (o instanceof HtmlObjects.Comment)
            {
                p_before.add((HtmlObjects.Comment) o);
                p_tags.remove(0);

                result = true;
                continue loop_before;
            }
            else if (o instanceof HtmlObjects.Text
                    || o instanceof HtmlObjects.Newline)
            {
                String text = o.toString();

                if (Text.isBlankOrNbsp(text))
                {
                    p_before.add((HtmlObjects.HtmlElement) o);
                    p_tags.remove(0);

                    result = true;
                    continue loop_before;
                }
            }

            b_changed = false;
        }

        return result;
    }

    protected boolean removeTrailingFormatting(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        boolean result = false;
        boolean b_changed = true;

        loop_after: while (b_changed && p_tags.size() > 0)
        {
            Object o = p_tags.get(p_tags.size() - 1);

            if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag tag = (HtmlObjects.Tag) o;

                if (/* tag.tag.equalsIgnoreCase("span") || */tag.isIsolated)
                {
                    p_after.add(0, tag);
                    p_tags.remove(p_tags.size() - 1);

                    result = true;
                    continue loop_after;
                }
            }
            else if (o instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.EndTag tag = (HtmlObjects.EndTag) o;

                if (/* tag.tag.equalsIgnoreCase("span") || */tag.isIsolated)
                {
                    p_after.add(0, tag);
                    p_tags.remove(p_tags.size() - 1);

                    result = true;
                    continue loop_after;
                }
            }
            else if (o instanceof HtmlObjects.Comment)
            {
                p_after.add(0, (HtmlObjects.Comment) o);
                p_tags.remove(p_tags.size() - 1);

                result = true;
                continue loop_after;
            }
            else if (o instanceof HtmlObjects.Text
                    || o instanceof HtmlObjects.Newline)
            {
                String text = o.toString();

                if (Text.isBlankOrNbsp(text))
                {
                    p_after.add((HtmlObjects.HtmlElement) o);
                    p_tags.remove(p_tags.size() - 1);

                    result = true;
                    continue loop_after;
                }
            }

            b_changed = false;
        }

        return result;
    }

    protected boolean removeOuterPairedFormatting(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        boolean result = false;
        boolean b_changed = true;

        loop_paired: while (b_changed && p_tags.size() >= 2)
        {
            Object o1 = p_tags.get(0);
            Object o2 = p_tags.get(p_tags.size() - 1);

            if (o1 instanceof HtmlObjects.Tag
                    && o2 instanceof HtmlObjects.EndTag
                    && !isHiddenText((HtmlObjects.Tag) o1))
            {
                HtmlObjects.Tag t1 = (HtmlObjects.Tag) o1;
                HtmlObjects.EndTag t2 = (HtmlObjects.EndTag) o2;

                // First and last tag are paired, can be removed.
                if (t1.isPaired && t2.isPaired && t1.partnerId == t2.partnerId
                        && !isUntranslatableSpan(t1)
                        && !isInternalStyleSpan(t1))
                {
                    p_before.add(t1);
                    p_after.add(0, t2);
                    p_tags.remove(p_tags.size() - 1);
                    p_tags.remove(0);

                    result = true;

                    continue loop_paired;
                }
            }

            b_changed = false;
        }

        return result;
    }

    /**
     * Moves segment-initial hidden text to the skeleton, i.e. the start tag,
     * content in between, and end tag.
     */
    protected boolean removeLeadingHiddenText(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        boolean result = false;
        boolean b_changed = true;

        loop_hidden: while (b_changed && p_tags.size() >= 2)
        {
            Object o1 = p_tags.get(0);

            if (o1 instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag t1 = (HtmlObjects.Tag) o1;
                HtmlObjects.EndTag t2 = null;

                if (isHiddenText(t1))
                {
                    // Find the end tag within this paragraph.
                    loop_endtag: for (int j = 1, max = p_tags.size(); j < max; j++)
                    {
                        Object o2 = p_tags.get(j);

                        if (o2 instanceof HtmlObjects.EndTag)
                        {
                            t2 = (HtmlObjects.EndTag) o2;

                            if (t1.tag.equalsIgnoreCase(t2.tag) && t1.isPaired
                                    && t2.isPaired
                                    && t1.partnerId == t2.partnerId)
                            {
                                break loop_endtag;
                            }

                            t2 = null;
                        }
                    }

                    // If paired endtag found, move all into skeleton.
                    if (t2 != null)
                    {
                        while (true)
                        {
                            Object o = p_tags.remove(0);
                            p_before.add((HtmlObjects.HtmlElement) o);

                            if (o == t2)
                            {
                                break;
                            }
                        }

                        result = true;

                        continue loop_hidden;
                    }
                }

                b_changed = false;
            }

            b_changed = false;
        }

        return result;
    }

    /**
     * Moves segment-trailing hidden text to the skeleton, i.e. the start tag,
     * content in between, and end tag.
     */
    protected boolean removeTrailingHiddenText(
            List<HtmlObjects.HtmlElement> p_before,
            List<HtmlObjects.HtmlElement> p_tags,
            List<HtmlObjects.HtmlElement> p_after)
    {
        boolean result = false;
        boolean b_changed = true;

        loop_hidden: while (b_changed && p_tags.size() >= 2)
        {
            Object o2 = p_tags.get(p_tags.size() - 1);

            if (o2 instanceof HtmlObjects.EndTag)
            {
                HtmlObjects.Tag t1 = null;
                HtmlObjects.EndTag t2 = (HtmlObjects.EndTag) o2;

                if (t2.isPaired)
                {
                    // Find the start tag within this paragraph.
                    loop_starttag: for (int j = p_tags.size() - 2, min = 0; j >= min; j--)
                    {
                        Object o1 = p_tags.get(j);

                        if (o1 instanceof HtmlObjects.Tag)
                        {
                            t1 = (HtmlObjects.Tag) o1;

                            if (t1.tag.equalsIgnoreCase(t2.tag) && t1.isPaired
                                    && t2.isPaired
                                    && t1.partnerId == t2.partnerId)
                            {
                                break loop_starttag;
                            }

                            t1 = null;
                        }
                    }

                    // If paired starttag found, move all into skeleton.
                    if (t1 != null && isHiddenText(t1))
                    {
                        while (true)
                        {
                            Object o = p_tags.remove(p_tags.size() - 1);
                            p_after.add(0, (HtmlObjects.HtmlElement) o);

                            if (o == t1)
                            {
                                break;
                            }
                        }

                        result = true;

                        continue loop_hidden;
                    }
                }

                b_changed = false;
            }

            b_changed = false;
        }

        return result;
    }

    //
    // Helper Methods
    //

    /**
     * Checks if the initial tag list (m_tags) contains at least one TAB.
     */
    private boolean containsTabs()
    {
        for (int i = 0, max = m_tags.size(); i < max; i++)
        {
            Object o = m_tags.get(i);

            if (o instanceof HtmlObjects.Tag)
            {
                HtmlObjects.Tag tag = (HtmlObjects.Tag) o;

                if (isMsoTabCount(tag))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if this span is useless, i.e. contributes nothing to the HTML
     * presentation or Word's round-tripping capabilities.
     */
    private boolean isUselessSpan(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("SPAN") && t.attributes.isDefined("style"))
        {
            String style = t.attributes.getValue("style");

            if (style.startsWith("'layout-grid-mode:line'")
                    || style.startsWith("'layout-grid-mode:both'")
                    || style.startsWith("'mso-bidi-font-weight:bold'")
                    || style.startsWith("'mso-bidi-font-size:"))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if this span is for a (powerpoint) bullet character, i.e. one that
     * uses Wingdings as font and has style='mso-special-format:[no]bullet'.
     */
    private boolean isBulletSpan(HtmlObjects.Tag t)
    {
        // For PPT issue
        if (t.tag.equalsIgnoreCase("SPAN"))
        {
            if (t.attributes.isDefined("style"))
            {
                String style = t.attributes.getValue("style");
                if (style.indexOf("mso-special-format") > -1
                        && style.indexOf("bullet") > -1)
                {
                    return true;
                }
            }

            if (t.attributes.isDefined("class"))
            {
                String style = t.attributes.getValue("class");
                if ((m_bulletStyleSet != null) && (m_bulletStyleSet.size() > 0)
                        && m_bulletStyleSet.contains(style))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if this span is for a (powerpoint) CR end-of-line character, i.e.
     * one whose content is &amp;#13; and that uses
     * style='mso-special-format:lastCR'.
     */
    private boolean isLastCR(HtmlObjects.Tag t)
    {
        if (t.tag.equalsIgnoreCase("SPAN") && t.attributes.isDefined("style"))
        {
            String style = t.attributes.getValue("style");

            if (style.indexOf("mso-special-format") > -1
                    && style.indexOf("lastCR") > -1)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Detects series of nbsp that Word converted from tabs during conversion to
     * HTML.
     * 
     * Looks like &lt;span style='mso-tab-count:1'&gt; &lt;/span&gt;.
     */
    private boolean isMsoTabCount(HtmlObjects.Tag t)
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
     * Detects series of nbsp that Word converted from multiple whitespace
     * during conversion to HTML.
     * 
     * Looks like &lt;span style="mso-spacerun:yes"&gt; &lt;/span&gt;.
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
     * Detects a special MSO span that represents a character. The content of
     * the span includes the char for non-MSO applications in a conditional
     * declaration which the extractor may have removed.
     * 
     * Example:
     * 
     * <span style='mso-special-character:footnote'> <![if
     * !supportFootnotes]>[1]<![endif]></span>
     * 
     * is extracted as:
     * 
     * <span style='mso-special-character:footnote'></span>
     * 
     * But in this case, the SPAN cannot be removed without altering document
     * semantics.
     */
    private boolean isMsoSpecialCharacter(HtmlObjects.Tag t)
    {
        if (t.attributes.isDefined("style")
                && t.attributes.getValue("style").startsWith(
                        "mso-special-character:", 1))
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
    private boolean isHiddenText(HtmlObjects.Tag t)
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

    private boolean isUntranslatableSpan(HtmlObjects.Tag t)
    {
        if (t.isPaired && !t.isIsolated && t.tag.equalsIgnoreCase("span")
                && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");

            // if (!m_rules.canExtractWordCharStyle(clazz))
            // {
            // System.err.println("Style " + clazz + " is untranslatable");
            // }

            return !m_rules.canExtractWordCharStyle(clazz);
        }

        return false;
    }

    private boolean isInternalStyleSpan(HtmlObjects.Tag t)
    {
        if (t.isPaired && !t.isIsolated && t.tag.equalsIgnoreCase("span")
                && t.attributes.isDefined("class"))
        {
            String clazz = t.attributes.getValue("class");

            return m_rules.isInternalTextCharStyle(clazz);
        }

        return false;
    }

    private boolean isExcelExtractor()
    {
        return m_rules.isExcelTractionRules();
    }

    /**
     * Counts the number of nbsp characters in a string.
     */
    private int countNbsp(String p_text)
    {
        int result = 0;

        for (int i = 0, max = p_text.length(); i < max; i++)
        {
            char ch = p_text.charAt(i);

            if (ch == '\u00a0')
            {
                result++;
            }
            else
            {
                break;
            }
        }

        return result;
    }
}
