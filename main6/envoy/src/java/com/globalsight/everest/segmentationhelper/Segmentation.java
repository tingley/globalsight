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
package com.globalsight.everest.segmentationhelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.util.SortUtil;

public class Segmentation
{

    static private final Logger CATEGORY = Logger.getLogger(Segmentation.class);

    /**
     * The Segmentation Rule to segment text
     */
    private SegmentationRule segmentationRule = new SegmentationRule();

    /**
     * First we use breakIndex to hold the indexes originally produced, and then
     * remove all nonbreak indexes holded by nonBreakIndex.
     */
    private ArrayList<Integer> breakIndex = new ArrayList<Integer>();
    /**
     * A temp ArrayList to hold the break index before handle format.
     */
    private ArrayList<Integer> interBreak = new ArrayList<Integer>();

    /**
     * nonBreakIndex is used to hold the indexs produced according to break
     * exceptions in a rule
     */
    private ArrayList<Integer> nonBreakIndex = new ArrayList<Integer>();

    /**
     * translable represents the text to be segmented.
     */
    private String translable = null;

    /**
     * The locale used by translable(text to be segmented).
     */
    private String locale = null;

    /**
     * White space is defined as [\t\n\f\r\p{Z}].
     */
    private static final Pattern whitespace = Pattern.compile("[\\s\\xA0]+");

    // "</  >"
    private static final Pattern closemark = Pattern
            .compile("</[^><]*[^/]>\\s*");
    // "<  />"
    private static final Pattern isolatemark = Pattern
            .compile("<[^/][^><]*/>\\s*");
    // "<   >"
    private static final Pattern openmark = Pattern
            .compile("<[^/][^><]*[^/]>\\s*");

    private Matcher whitespaceMatcher = null;

    private Matcher closemarkMatcher = null;

    private Matcher isolatemarkMatcher = null;

    private Matcher openmarkMatcher = null;

    public Segmentation() throws Exception
    {

        locale = null;
        translable = null;
        segmentationRule = null;

    }

    public Segmentation(String p_locale, String p_translable,
            SegmentationRule p_srx) throws Exception
    {

        locale = p_locale;
        translable = p_translable;
        segmentationRule = p_srx;
        whitespaceMatcher = whitespace.matcher(translable);
        closemarkMatcher = closemark.matcher(translable);
        openmarkMatcher = openmark.matcher(translable);
        isolatemarkMatcher = isolatemark.matcher(translable);

    }

    /**
     * Set segmentation rule used to segment a text.
     * 
     * @param segmentationRule
     */
    public void setSegmentationRule(SegmentationRule p_segmentationRule)
    {
        this.segmentationRule = p_segmentationRule;
    }

    /**
     * Set the text to be segmented.
     * 
     * @param translable
     */
    public void setTranslable(String p_translable)
    {
        translable = p_translable;
        whitespaceMatcher = whitespace.matcher(translable);
        closemarkMatcher = closemark.matcher(translable);
        openmarkMatcher = openmark.matcher(translable);
        isolatemarkMatcher = isolatemark.matcher(translable);
    }

    /**
     * Set locale used by text to be segmented.
     * 
     * @param locale
     */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * This method is used to segment text, it produces breakIndex and
     * nonBreakIndex for latter use, also return one String[] contains result
     * segments.
     * 
     * @return
     */
    public String[] doSegmentation() throws Exception
    {
        ArrayList<Rule> rules = segmentationRule.getRulesByLocale(locale);
        CATEGORY.debug("locale is :" + locale);
        if (rules.size() == 0)
        {
            CATEGORY.info("There is no rule associated : " + locale);
        }

        // These two strings are used to hold
        // regular exprissions in a rule element.
        String before = null;
        String after = null;
        int endIndex;
        int startIndex;
        Rule rule;
        int length = translable.length();

        try
        {
            for (int i = 0; i < rules.size(); i++)
            {
                rule = rules.get(i);
                before = rule.getBeforeBreak();
                after = rule.getAfterBreak();

                if (before != null)
                {
                    // before regular expression is not null
                    if (after != null)
                    {
                        // after regular expression is not null
                        Matcher be = Pattern.compile(before)
                                .matcher(translable);
                        Matcher af = Pattern.compile(after).matcher(translable);
                        // both before and after are not null, we will find
                        // out break indexes and nonbreak indexes. there
                        // nonbreak
                        // index is the index produced according to a rule whose
                        // break is set to "no" in rule element.
                        while (be.find())
                        {
                            endIndex = be.end();

                            if (af.find(endIndex) && (af.start() == endIndex))
                            {
                                endIndex = handleWhiteSpace(endIndex);
                                saveIndex(rule, endIndex);
                            }
                        }
                    }// end if (after != null)
                    else
                    {
                        // after is null, but before is not, we find out both
                        // break indexes and
                        // nobreak indexes only accoring to the before regular
                        // expression.
                        Matcher be = Pattern.compile(before)
                                .matcher(translable);

                        while (be.find())
                        {
                            endIndex = be.end();
                            endIndex = handleWhiteSpace(endIndex);
                            saveIndex(rule, endIndex);
                        }
                    }// end else
                }// end if (before != null)
                else
                {
                    // Now before is null.
                    if (after != null)
                    {
                        // after is not null, we will find out both break
                        // indexes and
                        // nobreak indexes only according to the after regular
                        // expression.
                        Matcher af = Pattern.compile(after).matcher(translable);

                        while (af.find())
                        {
                            startIndex = af.start();
                            startIndex = handleWhiteSpace(startIndex);
                            saveIndex(rule, startIndex);
                        }
                    }// end if (after != null)
                    else
                    {
                        // (before == null) && (after == null)
                        // we do nothing
                    }
                }// end else

            }// end for (int i = 0; i < rules.size(); i++)
        }
        catch (Exception e)
        {
            CATEGORY.error("There is a Exception while doing segmentation", e);
            throw new Exception(e.getMessage());
        }

        // Remove all the nonbreak index from the break index.
        interBreak.removeAll(nonBreakIndex);

        // Now handle format while checking break index.
        Integer integer = null;
        int newIndex = 0;
        for (int i = 0; i < interBreak.size(); i++)
        {
            integer = (Integer) interBreak.get(i);
            newIndex = handleFormat(integer.intValue());
            integer = new Integer(newIndex);
            if (!breakIndex.contains(integer))
            {
                breakIndex.add(integer);
            }

        }
        // Now clear the temp ArrayList
        interBreak.clear();

        // handle rangeRule
        String rangeRule = segmentationRule.getHeader().getRangeRule();
        if (rangeRule != null && rangeRule.length() > 0)
        {
            Matcher m = Pattern.compile(rangeRule).matcher(translable);
            while (m.find())
            {
                int start = m.start();
                int end = m.end();
                // Remove existing breaker inside the range
                for (int n = start; n < end; n++)
                {
                    Integer ni = new Integer(n);
                    if (breakIndex.contains(ni))
                    {
                        breakIndex.remove(ni);
                    }
                }

                // Set start and end of the range as breaker
                Integer startI = new Integer(start);
                Integer endI = new Integer(end);
                if (start > 0 && !breakIndex.contains(startI))
                {
                    breakIndex.add(startI);
                }
                if (!breakIndex.contains(endI))
                {
                    breakIndex.add(endI);
                }
            }
        }

        // handle last
        Integer last = new Integer(length);
        if (!breakIndex.contains(last))
        {
            breakIndex.add(last);
        }

        // Now we sort the breakIndex to get the correct segments
        SortUtil.sort(breakIndex);

        String[] segments = splitTranslatable();
        return segments;
    }

    private String[] splitTranslatable()
    {
        // segment the translable text
        ArrayList<String> segments = new ArrayList<String>();
        // After remove nonbreak index, if the size of breakIndex is 0,
        // the text should not be segmented.
        if (breakIndex.size() == 0)
        {
            CATEGORY.info("The size of breakIndex is 0 after sorting");
            segments.add(translable);
        }
        // If the size of breakIndex is not 0, we should
        // break the text into segments for testing interface
        else
        {
            int start = 0;
            int end;
            String segment = null;

            for (int k = 0; k < breakIndex.size(); k++)
            {
                end = (breakIndex.get(k)).intValue();
                segment = translable.substring(start, end);

                if (segment != null)
                {
                    segments.add(segment);
                }

                start = end;
            }
        }
        // Convert ArrayList to String[].
        int resultSize = segments.size();

        while (resultSize > 0 && segments.get(resultSize - 1).equals(""))
            resultSize--;
        return listToArray(segments, resultSize);
    }

    private static String[] listToArray(ArrayList strs, int resultSize)
    {
        if (strs == null)
        {
            return null;
        }

        if (resultSize > strs.size())
        {
            resultSize = strs.size();
        }

        String[] result = new String[resultSize];

        return (String[]) strs.subList(0, resultSize).toArray(result);
    }

    /**
     * Handle formating marks ("< >", "</ >" and "< />") according to rule.
     * 
     * @param p_rule
     * @param p_index
     * @return new break index.
     */
    private int handleFormat(int p_index)
    {
        int resultIndex = p_index;
        SrxHeader header = segmentationRule.getHeader();
        HashMap<String, String> format = header.getFormatHandle();
        String include = null;
        int endIndex;

        while (isolatemarkMatcher.find())
        {
            // < />
            endIndex = isolatemarkMatcher.end();
            if (endIndex == p_index)
            {
                // Breaking condition happens right after the matched isolated
                // formatting mark.
                include = format.get("isolated");

                if ((include != null) && include.equalsIgnoreCase("no"))
                {
                    resultIndex = isolatemarkMatcher.start();
                    isolatemarkMatcher.reset();
                    return resultIndex;
                }
            }
            else if (endIndex > p_index)
            {
                // Breaking condition happens before the matched isolated
                // formatting mark.
                isolatemarkMatcher.reset();
                break;
            }
        }

        while (openmarkMatcher.find())
        {
            // < >
            endIndex = openmarkMatcher.end();
            if (endIndex == p_index)
            {
                // Breaking condition happens right after the matched opening
                // formatting mark.
                include = format.get("start");

                if ((include != null) && include.equalsIgnoreCase("no"))
                {
                    resultIndex = openmarkMatcher.start();
                    openmarkMatcher.reset();
                    return resultIndex;
                }
            }
            else if (endIndex > p_index)
            {
                // Breaking condition happens before the matched opening
                // formtting mark.
                openmarkMatcher.reset();
                break;
            }

        }

        while (closemarkMatcher.find())
        {
            // </ >
            endIndex = closemarkMatcher.end();
            if (endIndex == p_index)
            {
                // Breaking condition happens right after the matched closing
                // formatting mark.
                include = format.get("end");

                if ((include != null) && include.equalsIgnoreCase("no"))
                {
                    resultIndex = closemarkMatcher.start();
                    closemarkMatcher.reset();
                    return resultIndex;
                }
            }
            else if (endIndex > p_index)
            {
                // Breaking condition happens before the matched closing
                // formatting mark.
                closemarkMatcher.reset();
                break;
            }

        }

        return resultIndex;
    }

    /**
     * If the following characters of p_index are whitespaces, the break index
     * should at the first non whitespace character after whitespaces.
     * 
     * @param p_index
     * @return new break index
     */
    private int handleWhiteSpace(int p_index)
    {
        int index = p_index;

        if (whitespaceMatcher == null)
        {
            return index;
        }

        else if (whitespaceMatcher.find(p_index)
                && (whitespaceMatcher.start() == p_index))
        {
            index = whitespaceMatcher.end();
        }

        return index;
    }

    /**
     * Save index into breakIndex or nonBreakIndex accroding to rule.
     * 
     * @param p_rule
     * @param p_index
     */
    private void saveIndex(Rule p_rule, int p_index)
    {
        Integer it = new Integer(p_index);
        // Now we determine the index is break index or not.
        if (p_rule.isBreak())
        {
            // Test if we meet a break rule more than
            // one time.
            if ((p_index != 0) && (!interBreak.contains(it)))
            {
                interBreak.add(it);
            }
        }
        else
        {
            // Test if we meet a break rule exception more than one
            // time.
            if (!nonBreakIndex.contains(it))
            {
                nonBreakIndex.add(it);
            }
        }
    }

    /**
     * Get the final break index ArrayList which has been removed the nonbreak
     * index and sorted, this method should be used after doSegmentation().
     * 
     * @return
     */
    public ArrayList<Integer> getBreakIndex()
    {
        return breakIndex;
    }

    /**
     * Only for test segmentation rule and unit testing, use
     * {@link #handleSrxExtension(SegmentationRule, List)}
     * 
     * @return
     */
    @Deprecated
    public static String[] handleSrxExtension(SegmentationRule p_rule,
            String[] p_segments)
    {
        if (p_segments == null)
        {
            return p_segments;
        }

        List<String> segments = new ArrayList<String>();
        for (int i = 0; i < p_segments.length; i++)
        {
            segments.add(p_segments[i]);
        }

        List<SegmentNode> resultList = handleSrxExtension(p_rule, segments);

        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < resultList.size(); i++)
        {
            SegmentNode snode = resultList.get(i);
            if (!snode.outputToSkeleton())
            {
                result.add(snode.getSegment());
            }
        }

        return listToArray(result, result.size());
    }

    /**
     * Handle these segments with SRX extensions
     */
    public static List<SegmentNode> handleSrxExtension(SegmentationRule p_rule,
            List<String> p_segments)
    {
        if (p_segments == null || p_segments.size() == 0)
        {
            return new ArrayList<SegmentNode>();
        }

        if (p_rule == null || p_rule.getHeader() == null)
        {
            ArrayList<SegmentNode> result = new ArrayList<SegmentNode>();
            for (String string : p_segments)
            {
                result.add(new SegmentNode(string));
            }

            return result;
        }

        SrxHeader header = p_rule.getHeader();
        ArrayList<SegmentNode> result = new ArrayList<SegmentNode>();
        for (String oriSeg : p_segments)
        {
            StringBuffer sb = new StringBuffer(oriSeg);

            // handle one segment include all
            if (p_segments.size() == 1 && header.isOneSegmentIncludesAll())
            {
                result.add(new SegmentNode(sb.toString()));
                break;
            }

            StringBuffer leadingWS = new StringBuffer();
            StringBuffer trailingWS = new StringBuffer();
            // trim leading whitespace
            if (header.isTrimLeadingWhitespaces())
            {
                int i = 0;
                for (; i < sb.length(); i++)
                {
                    char cc = sb.charAt(i);
                    if (!isWhitespace(cc))
                    {
                        break;
                    }
                    else
                    {
                        leadingWS.append(cc);
                    }
                }

                if (i != 0)
                {
                    sb.replace(0, i, "");
                }
            }

            // trim trailing whitespace
            if (header.isTrimTrailingWhitespaces())
            {
                int i = sb.length() - 1;
                for (; i > -1; i--)
                {
                    char cc = sb.charAt(i);
                    if (!isWhitespace(cc))
                    {
                        break;
                    }
                    else
                    {
                        trailingWS.insert(0, cc);
                    }
                }

                if (i != sb.length() - 1)
                {
                    sb.replace(i + 1, sb.length(), "");
                }
            }

            if (leadingWS.length() > 0)
            {
                SegmentNode node = new SegmentNode(leadingWS.toString());
                node.setIsLeadingWS(true);
                result.add(node);
            }
            if (sb.length() > 0)
            {
                result.add(new SegmentNode(sb.toString()));
            }
            if (trailingWS.length() > 0)
            {
                SegmentNode node = new SegmentNode(trailingWS.toString());
                node.setIsTrailingWS(true);
                result.add(node);
            }
        }

        // add one empty segment if there is non segment left
        if (result.size() == 0)
        {
            result.add(new SegmentNode(""));
        }

        return result;
    }

    public static boolean isWhitespaceString(String str)
    {
        if (str == null || str.length() == 0)
        {
            return false;
        }

        int i = str.length() - 1;
        for (; i > -1; i--)
        {
            char cc = str.charAt(i);
            if (!isWhitespace(cc))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean isWhitespace(char cc)
    {
        return Character.isWhitespace(cc);
    }

    public static Vector adjustEmptySegments(Vector documentElements)
    {
        Vector result = new Vector();
        for (Iterator it = documentElements.iterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    if (elem.hasSegments())
                    {
                        TranslatableElement newElem = newTranslatableElement(elem);

                        ArrayList segments = elem.getSegments();
                        for (Iterator iterator = segments.iterator(); iterator
                                .hasNext();)
                        {
                            SegmentNode segNode = (SegmentNode) iterator.next();
                            if (segNode.outputToSkeleton())
                            {
                                SkeletonElement ske = new SkeletonElement();
                                ske.setSkeleton(segNode.getSegment());

                                if (newElem.hasSegments()
                                        && newElem.getSegments().size() > 0)
                                {
                                    result.add(newElem);
                                    newElem = newTranslatableElement(elem);
                                }

                                result.add(ske);
                            }
                            else
                            {
                                newElem.addSegment(segNode);
                            }
                        }

                        if (newElem.hasSegments()
                                && newElem.getSegments().size() > 0)
                        {
                            result.add(newElem);
                        }
                    }
                    else
                    {
                        result.add(de);
                    }

                    break;
                }

                default:
                {
                    result.add(de);
                    break;
                }
            }
        }

        return result;
    }

    private static TranslatableElement newTranslatableElement(
            TranslatableElement oriElem)
    {
        TranslatableElement newElem = new TranslatableElement();
        newElem.setDataType(oriElem.getDataType());
        newElem.setIsLocalized(oriElem.getIsLocalized());
        newElem.setPreserveWhiteSpace(oriElem.isPreserveWhiteSpace());
        newElem.setSid(oriElem.getSid());
        newElem.setType(oriElem.getType());
        newElem.setWordcount(oriElem.getWordcount());
        newElem.setXliffPart(oriElem.getXliffPart());

        return newElem;
    }

}
