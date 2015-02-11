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

package com.globalsight.cxe.entity.filterconfiguration;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.openoffice.StringIndex;
import com.globalsight.everest.util.comparator.PriorityComparator;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.javaprop.JPTmxEncoder;
import com.globalsight.ling.tw.internal.InternalTextUtil;
import com.globalsight.util.SortUtil;

public class InternalTextHelper
{
    private static final Logger CATEGORY = Logger
            .getLogger(InternalTextHelper.class);
    private static String BPT_START = "<bpt internal=\"yes\" i=\"iii\"></bpt>";
    private static String EPT_END = "<ept i=\"iii\"></ept>";
    private static HtmlEntities m_htmlDecoder = new HtmlEntities();
    private static XmlEntities m_xmlEncoder = new XmlEntities();
    private static JPTmxEncoder m_tmx = new JPTmxEncoder();
    private static String INTERNAL_TEXT_KEY = "gsstartinternaltext";
    private static String INTERNAL_TEXT_KEY_END = "gsend";
    private static String REGEX_ALL = "<bpt[^>]*i=\"{0}\"[^>]*>.*?</bpt>(.*?)<ept[^>]*i=\"{0}\"[^>]*>.*?</ept>";
    private static String REGEX_ALL_2 = "<bpt[^>]*i=\"{0}\"[^>]*/>(.*?)<ept[^>]*i=\"{0}\"[^>]*/>";
    private static String REGEX_GSINTERNALTEXT = "<GS-INTERNAL-TEXT[^>]*>[^<]*</GS-INTERNAL-TEXT>";
    private static String internal_key_0 = "internal=\"yes\"";
    private static String internal_key_1 = "</GS-INTERNAL-TEXT>";

    public static String GS_INTERNALT_TAG_START = "<GS-INTERNAL-TEXT>";
    public static String GS_INTERNALT_TAG_END = "</GS-INTERNAL-TEXT>";
    public static String REG_INTERNAL_TEXT = "<(GS-INTERNAL-TEXT)>(.*)</GS-INTERNAL-TEXT>";
    public static final String m_tag_amp = "AmpersandOfGS";
    /**
     * Handle the internal texts for one string
     * 
     * @param oriStr
     * @param internalTexts
     * @param format
     * @param useBpt
     *            true to use bpt/ept tag; false to use GS-INTERNAL-TEXT tag
     * @return
     */
    public static String handleString(String oriStr,
            List<InternalText> internalTexts, String format, boolean useBpt,
            boolean doSegFirst)
    {
        if (oriStr == null || oriStr.length() == 0)
            return oriStr;

        if (internalTexts == null || internalTexts.size() == 0)
            return oriStr;

        List<String> sList = handleStringWithListReturn(oriStr, internalTexts,
                format, doSegFirst);

        if (useBpt)
        {
            assignIndexToBpt(1, sList);
        }

        return listToString(sList);
    }

    /**
     * Handle the internal texts for one string
     * 
     * @param oriStr
     * @param internalTexts
     * @param format
     * @param useBpt
     *            true to use bpt/ept tag; false to use GS-INTERNAL-TEXT tag
     * @return
     */
    public static String handleString(String oriStr,
            List<InternalText> internalTexts, String format, boolean useBpt)
    {
        return handleString(oriStr, internalTexts, format, useBpt, false);
    }

    /**
     * Handle the internal texts for one string, use GS-INTERNAL-TEXT tag
     * 
     * @param oriStr
     * @param internalTexts
     * @param format
     * @return
     */
    public static List<String> handleStringWithListReturn(String oriStr,
            List<InternalText> internalTexts, String format, boolean doSegFirst)
    {
        List<String> sList = new ArrayList<String>();
        sList.add(oriStr);

        if (oriStr == null || oriStr.length() == 0)
            return sList;

        if (internalTexts == null || internalTexts.size() == 0)
            return sList;

        SortUtil.sort(internalTexts, new PriorityComparator());

        if (!doSegFirst
                || (doSegFirst && !oriStr.contains("<") && !oriStr
                        .contains(">")))
        {
            for (InternalText it : internalTexts)
            {
                List<String> result = new ArrayList<String>();
                for (String s : sList)
                {
                    result.addAll(handleStringByOneRule(s, it, format,
                            doSegFirst));
                }

                sList = result;
            }

            if (sList == null || sList.size() == 0)
            {
                sList = new ArrayList<String>();
                sList.add(oriStr);
            }

            return sList;
        }
        else
        {
            List<InternalTextElement> elms = null;
            try
            {
                elms = InternalTextElement.parse(oriStr);
            }
            catch (Exception e1)
            {
                String msg = "Exception in handleStringWithListReturn, InternalTextElement.parse:"
                        + oriStr;
                CATEGORY.error(msg, e1);
                elms = new ArrayList<InternalTextElement>();
                elms.add(InternalTextElement.newTag(oriStr));
            }

            for (InternalText it : internalTexts)
            {
                List<InternalTextElement> result = new ArrayList<InternalTextElement>();
                for (InternalTextElement e : elms)
                {
                    result.addAll(handleElementByOneRule(e, it, format));
                }

                elms = result;
            }
            sList = new ArrayList<String>();

            if (elms != null && elms.size() > 0)
            {
                // find max i
                int i = 0;
                for (InternalTextElement ite : elms)
                {
                    if (ite.isTag())
                    {
                        StringIndex si = StringIndex.getValueBetween(
                                new StringBuffer(ite.toString()), 0, " i=\"",
                                "\"");
                        if (si != null && si.value != null)
                        {
                            try
                            {
                                int temp = Integer.parseInt(si.value);
                                if (temp > i)
                                    i = temp;
                            }
                            catch (Exception exx)
                            {
                                // ignore
                            }
                        }
                    }
                }
                // then add bpt tag
                for (InternalTextElement ite : elms)
                {
                    if (ite.isInternalText())
                    {
                        String c = ite.getContent();
                        String before = BPT_START.replace("iii", "" + (i + 1));
                        String end = EPT_END.replace("iii", "" + (i + 1));
                        ite.setContent(before + c + end);
                        ++i;
                    }

                    sList.add(ite.toString());
                }
            }
            else
            {
                sList.add(oriStr);
            }

            return sList;
        }
    }

    private static List<InternalTextElement> handleElementByOneRule(
            InternalTextElement e, InternalText it, String format)
    {
        List<InternalTextElement> result = new ArrayList<InternalTextElement>();
        if (!e.doInternalText())
        {
            result.add(e);
            return result;
        }

        String s = e.toString();
        if (!it.isRE())
        {
            String internalText = getInternalTextValue(it.getName(), format);
            String itext = resolveInternalTextValue(internalText, format, true);
            String end = new String(s.toCharArray());
            int i = end.indexOf(internalText);

            while (i > -1)
            {
                String first = end.substring(0, i);
                if (first.length() != 0)
                {
                    result.add(InternalTextElement.newText(first, false));
                }
                result.add(InternalTextElement.newText(itext, true));
                end = end.substring(i + internalText.length());

                i = end.indexOf(internalText);
            }

            if (end.length() != 0)
            {
                result.add(InternalTextElement.newText(end, false));
            }
        }
        else
        {
            try
            {
                Pattern p = Pattern.compile(it.getName());
                Matcher m = p.matcher(s);
                int fromIndex = 0;
                String end = new String(s.toCharArray());
                while (m.find())
                {
                    String internalText = m.group();

                    // if the length of internal text is 0, continue
                    if (internalText.length() == 0)
                    {
                        continue;
                    }

                    int i = s.indexOf(internalText, fromIndex);
                    String first = s.substring(fromIndex, i);
                    end = s.substring(i + internalText.length());

                    if (first.length() != 0)
                    {
                        result.add(InternalTextElement.newText(first, false));
                    }

                    String itext = resolveInternalTextValue(internalText,
                            format, true);
                    result.add(InternalTextElement.newText(itext, true));

                    fromIndex = i + internalText.length();
                }

                if (end != null && end.length() != 0)
                {
                    result.add(InternalTextElement.newText(end, false));
                }
            }
            catch (Exception exx)
            {
                String msg = "Exception in handleStringByOneRule, re rule:"
                        + it.getName() + " text:" + s;
                CATEGORY.error(msg, exx);
                result.clear();
                result.add(e);
            }
        }

        return result;
    }

    /**
     * Handle the internal texts for one string, use GS-INTERNAL-TEXT tag
     * 
     * @param oriStr
     * @param internalTexts
     * @param format
     * @return
     */
    public static List<String> handleStringWithListReturn(String oriStr,
            List<InternalText> internalTexts, String format)
    {
        return handleStringWithListReturn(oriStr, internalTexts, format, false);
    }

    /**
     * Assign id
     * 
     * @param startIndex
     * @param sList
     * @return
     */
    public static int assignIndexToBpt(int startIndex, List<String> sList)
    {
        int i = startIndex;
        for (int j = 0; j < sList.size(); j++)
        {
            String s = sList.get(j);

            if (s.startsWith(GS_INTERNALT_TAG_START))
            {
                s = s.replace(GS_INTERNALT_TAG_START, BPT_START);
                s = s.replace(GS_INTERNALT_TAG_END, EPT_END);
            }

            if (s.startsWith(BPT_START))
            {
                String news = s.replace("i=\"iii\"", "i=\"" + i + "\"");
                i++;
                sList.set(j, news);
            }
        }

        return i;
    }

    /**
     * Append all strings in list to string
     * 
     * @param sList
     * @return
     */
    public static String listToString(List<String> sList)
    {
        StringBuffer sb = new StringBuffer();

        for (String s : sList)
        {
            sb.append(s);
        }

        return sb.toString();
    }

    /**
     * Handle one internal text for one string
     * 
     * @param it
     * @param s
     * @param format
     * @return
     */
    private static List<String> handleStringByOneRule(String s,
            InternalText it, String format, boolean doSegFirst)
    {
        String tagStart = GS_INTERNALT_TAG_START;
        String tagEnd = GS_INTERNALT_TAG_END;
        List<String> result = new ArrayList<String>();
        if (s.startsWith(tagStart))
        {
            result.add(s);
            return result;
        }
		if (s.contains(m_tag_amp))
		{
			s = s.replace(m_tag_amp, "&amp;");
		}
        if (!it.isRE())
        {
            String internalText = getInternalTextValue(it.getName(), format);
            String itext = resolveInternalTextValue(internalText, format,
                    doSegFirst);
            String end = new String(s.toCharArray());
            int i = end.indexOf(internalText);

            while (i > -1)
            {
                String first = end.substring(0, i);
                if (first.length() != 0)
                {
                    result.add(first);
                }
                result.add(tagStart + itext + tagEnd);
                end = end.substring(i + internalText.length());

                i = end.indexOf(internalText);
            }

            if (end.length() != 0)
            {
                result.add(end);
            }
        }
        else
        {
            try
            {
                Pattern p = Pattern.compile(it.getName());
                Matcher m = p.matcher(s);
                int fromIndex = 0;
                String end = new String(s.toCharArray());
                while (m.find())
                {
                    String internalText = m.group();

                    // if the length of internal text is 0, continue
                    if (internalText.length() == 0)
                    {
                        continue;
                    }

                    int i = s.indexOf(internalText, fromIndex);
                    String first = s.substring(fromIndex, i);
                    end = s.substring(i + internalText.length());

                    if (first.length() != 0)
                    {
                        result.add(first);
                    }

                    String itext = resolveInternalTextValue(internalText,
                            format, doSegFirst);
                    result.add(tagStart + itext + tagEnd);

                    fromIndex = i + internalText.length();
                }

                if (end != null && end.length() != 0)
                {
                    result.add(end);
                }
            }
            catch (Exception e)
            {
                String msg = "Exception in handleStringByOneRule, re rule:"
                        + it.getName() + " text:" + s;
                CATEGORY.error(msg, e);
                result.clear();
                result.add(s);
            }
        }

        return result;
    }

    private static String getInternalTextValue(String name, String format)
    {
        if (format == null)
        {
            return name;
        }

        if ((IFormatNames.FORMAT_XML.equals(format) || IFormatNames.FORMAT_OFFICE_XML
                .equals(format)) && (name != null && name.length() == 1))
        {
            return m_xmlEncoder.encodeStringBasic(name);
        }

        if (IFormatNames.FORMAT_XML.equals(format))
        {
            return name;
        }

        if (IFormatNames.FORMAT_WORD_HTML.equals(format)
                || IFormatNames.FORMAT_EXCEL_HTML.equals(format)
                || IFormatNames.FORMAT_POWERPOINT_HTML.equals(format))
        {
            return m_xmlEncoder.encodeStringBasic(name);
        }

        if (IFormatNames.FORMAT_HTML.equals(format) && name != null
                && name.length() == 1)
        {
            return m_xmlEncoder.encodeStringBasic(name);
        }

        if (IFormatNames.FORMAT_HTML.equals(format))
        {
            return name;
        }

        if (IFormatNames.FORMAT_JAVAPROP_HTML.equals(format)
                || IFormatNames.FORMAT_JAVAPROP_MSG.equals(format)
                || IFormatNames.FORMAT_JAVAPROP.equals(format))
        {
            return m_tmx.encode(name);
        }

        if (IFormatNames.FORMAT_PLAINTEXT.equals(format))
        {
            return m_tmx.encode(name);
        }

        return name;
    }

    private static String resolveInternalTextValue(String p_value,
            String format, boolean doSegFirst)
    {
        if (format == null)
        {
            return p_value;
        }

        if (IFormatNames.FORMAT_XML.equals(format)
                || IFormatNames.FORMAT_OFFICE_XML.equals(format))
        {
            if (p_value != null && p_value.length() == 1)
                return m_xmlEncoder.encodeStringBasic(p_value);
            if (doSegFirst)
                return p_value;
            else
                return m_xmlEncoder.encodeStringBasic(p_value);
        }

        if (IFormatNames.FORMAT_HTML.equals(format)
                || IFormatNames.FORMAT_WORD_HTML.equals(format)
                || IFormatNames.FORMAT_EXCEL_HTML.equals(format)
                || IFormatNames.FORMAT_POWERPOINT_HTML.equals(format))
        {
            return p_value;
        }

        if (IFormatNames.FORMAT_PLAINTEXT.equals(format))
        {
            return p_value;
        }

        return p_value;
    }

    /**
     * Protect internal text for segmentation
     * 
     * @param p_output
     * @return
     */
    public static List<String> protectInternalTexts(Output p_output)
    {
        List<String> internalTexts = new ArrayList<String>();
        int i = 0;
        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    String temp = elem.getChunk();
                    List<String> its = getInternalTexts(temp);

                    if (its != null && !its.isEmpty())
                    {
                        for (String itext : its)
                        {
                            String key = INTERNAL_TEXT_KEY + i
                                    + INTERNAL_TEXT_KEY_END;
                            i++;
                            temp = replaceFirstText(temp, itext, key);
                            internalTexts.add(itext);
                        }

                        elem.setChunk(temp);
                    }

                    break;
                }

                default:
                    // skip all others
                    break;
            }
        }

        return internalTexts;
    }

    /**
     * Restore internal text from protect
     * 
     * @param p_output
     * @param internalTexts
     */
    public static void restoreInternalTexts(Output p_output,
            List<String> internalTexts)
    {
        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    ArrayList segments = elem.getSegments();

                    if (segments != null && !segments.isEmpty())
                    {
                        for (Object object : segments)
                        {
                            SegmentNode snode = (SegmentNode) object;
                            String segment = snode.getSegment();

                            if (segment.contains(INTERNAL_TEXT_KEY))
                            {
                                for (int i = 0; i < internalTexts.size(); i++)
                                {
                                    String key = INTERNAL_TEXT_KEY + i
                                            + INTERNAL_TEXT_KEY_END;
                                    String ori = internalTexts.get(i);
                                    segment = replaceFirstText(segment, key,
                                            ori);
                                }
                                snode.setSegment(segment);
                            }
                        }
                    }

                    break;
                }

                default:
                    // skip all others
                    break;
            }
        }
    }

    /**
     * Protect internal text for segmentation
     * 
     * @param p_output
     * @return
     */
    public static String protectInternalTexts(String p_text,
            List<String> internalTexts)
    {
        int i = 0;

        List<String> its = getInternalTexts(p_text);
        String temp = p_text;

        if (its != null && !its.isEmpty())
        {
            for (String itext : its)
            {
                String key = INTERNAL_TEXT_KEY + i + INTERNAL_TEXT_KEY_END;
                i++;
                temp = replaceFirstText(temp, itext, key);
                internalTexts.add(itext);
            }
        }

        return temp;
    }

    /**
     * Restore internal text from protect
     * 
     * @param p_output
     * @param internalTexts
     */
    public static String restoreInternalTexts(String p_text,
            List<String> internalTexts)
    {
        String temp = p_text;
        if (temp.contains(INTERNAL_TEXT_KEY))
        {
            for (int i = 0; i < internalTexts.size(); i++)
            {
                String key = INTERNAL_TEXT_KEY + i + INTERNAL_TEXT_KEY_END;
                String ori = internalTexts.get(i);
                temp = replaceFirstText(temp, key, ori);
            }
        }

        return temp;
    }

    private static String replaceFirstText(String src, String subtext,
            String newtext)
    {
        int index = src.indexOf(subtext);
        if (index != -1)
        {
            String before = src.substring(0, index);
            String end = src.substring(index + subtext.length());
            return before + newtext + end;
        }
        else
        {
            return src;
        }
    }

    private static List<String> getInternalTexts(String src)
    {
        if ((src == null)
                || !(src.contains(internal_key_0) || src
                        .contains(internal_key_1)))
        {
            return null;
        }

        List<String> internalTexts = new ArrayList<String>();
        List<Integer> ids = getInternalIndex(src);
        for (int id : ids)
        {
            Object[] ob =
            { Integer.toString(id) };
            String regex = MessageFormat.format(REGEX_ALL, ob);
            Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
            Matcher matcher = pattern.matcher(src);

            while (matcher.find())
            {
                String s = matcher.group();
                internalTexts.add(s);
            }

            String regex2 = MessageFormat.format(REGEX_ALL_2, ob);
            Pattern pattern2 = Pattern.compile(regex2, Pattern.DOTALL);
            Matcher matcher2 = pattern2.matcher(src);

            while (matcher2.find())
            {
                String s = matcher2.group();
                internalTexts.add(s);
            }
        }

        Matcher m = Pattern.compile(REGEX_GSINTERNALTEXT).matcher(src);
        while (m.find())
        {
            String s = m.group();
            internalTexts.add(s);
        }

        return internalTexts;
    }

    private static List<Integer> getInternalIndex(String src)
    {
        List<Integer> ids = new ArrayList<Integer>();
        Set<String> set = InternalTextUtil.getInternalIndex(src);
        for (String id : set)
        {
            ids.add(Integer.parseInt(id));
        }

        SortUtil.sort(ids);
        return ids;
    }

    public static void handleOutput(Output p_output, Filter mFilter,
            boolean useBptTag)
    {
        if (mFilter == null || p_output == null)
        {
            return;
        }

        // get base filter (internal text filer) for main filter
        BaseFilter bf = null;
        if (mFilter instanceof BaseFilter)
        {
            bf = (BaseFilter) mFilter;
        }
        else
        {
            long filterId = mFilter.getId();
            String filterTableName = mFilter.getFilterTableName();
            bf = BaseFilterManager.getBaseFilterByMapping(filterId,
                    filterTableName);
        }

        if (bf == null)
        {
            return;
        }

        // get internal texts
        List<InternalText> internalTexts = null;
        try
        {
            internalTexts = BaseFilterManager.getInternalTexts(bf);
            SortUtil.sort(internalTexts, new PriorityComparator());
        }
        catch (Exception e)
        {
            CATEGORY.error("Get internal text failed. ", e);
        }
        if (internalTexts == null || internalTexts.size() == 0)
        {
            return;
        }

        String format = p_output.getDataFormat();
        // handle internal text by segment
        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    ArrayList segments = elem.getSegments();

                    if (segments != null && !segments.isEmpty())
                    {
                        for (Object object : segments)
                        {
                            SegmentNode snode = (SegmentNode) object;
                            String segment = snode.getSegment();

                            String result = handleString(segment,
                                    internalTexts, format, useBptTag, true);
                            snode.setSegment(result);
                        }
                    }

                    break;
                }

                default:
                    // skip all others
                    break;
            }
        }
    }
}
