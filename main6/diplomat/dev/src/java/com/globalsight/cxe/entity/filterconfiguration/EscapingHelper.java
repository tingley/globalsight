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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.comparator.PriorityComparator;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.util.SortUtil;
import com.globalsight.util.TagIndex;

public class EscapingHelper
{
    private static final Logger CATEGORY = Logger
            .getLogger(EscapingHelper.class);
    private static XmlEntities m_xmlEncoder = new XmlEntities();

	public static String handleString4Export(String oriStr, List<Escaping> es,
			String format, boolean noTag, boolean doDecode,
			String escapingChars)
	{
		return handleString4Export(oriStr, es, format, noTag, doDecode,
				escapingChars, false);
	}

    public static String handleString4Export(String oriStr, List<Escaping> es,
            String format, boolean noTag, boolean doDecode,
            String escapingChars, boolean isInCDATA)
    {
        if (oriStr == null || oriStr.length() == 0)
            return oriStr;

        if (es == null || es.size() == 0)
            return oriStr;

        // To be safe, do not escape anything for office 2010.
		if (IFormatNames.FORMAT_OFFICE_XML.equalsIgnoreCase(format))
        {
            return oriStr;
        }

        StringBuffer sb = new StringBuffer();

        List<TagIndex> tags = null;

        if (noTag)
        {
            TagIndex ti = TagIndex.createTagIndex(oriStr, false, 0,
                    oriStr.length());
            tags = new ArrayList<TagIndex>();
            tags.add(ti);
        }
        else
        {
            tags = TagIndex.getContentIndexes(oriStr, false);
        }

        int count = tags.size();
        for (int i = 0; i < count; i++)
        {
            TagIndex ti = tags.get(i);
            if (ti.isTag)
            {
            	if (IFormatNames.FORMAT_XML.equals(format) && isInCDATA)
            	{
                    // Escape tag content is dangerous...
                    sb.append(handleTagContent4Export(ti.content, es, doDecode,
                            format, escapingChars));
            	}
            	else
            	{
                    sb.append(ti.content);
            	}
            }
            else
            {
                sb.append(handleString4Export(ti.content, es, doDecode, format,
                        escapingChars));
            }
        }

        return sb.toString();
    }

    /**
     * The text node value in tag is also need escape handling. i.e.
     * <bpt i="2" type="font" x="2">&lt;font
     * color=\&apos;#0063AD\&apos;&gt;</bpt>
     * 
     * Also supports sub segments.
     */
    private static String handleTagContent4Export(String content,
            List<Escaping> es, boolean doDecode, String format,
            String escapingChars)
    {
        StringBuffer sub = new StringBuffer();
        List<String> splits = new ArrayList<String>();
        splitContent(content, splits);
        while (splits.size() == 3)
        {
            sub.append(splits.get(0));
            sub.append(handleString4Export(splits.get(1), es, doDecode, format,
                    escapingChars));
            splitContent(splits.get(2), splits);
        }
        // splits.size == 1 or 2
        for (String str : splits)
        {
            sub.append(str);
        }

        return sub.toString();
    }

    private static void splitContent(String content, List<String> splits)
    {
        splits.clear();
        int index = content.indexOf(">");
        if (index > -1)
        {
            splits.add(content.substring(0, index + 1));
            String rest = content.substring(index + 1);
            index = rest.indexOf("<");
            if (index > -1)
            {
                String textNodeStr = rest.substring(0, index);
                splits.add(textNodeStr);
                splits.add(rest.substring(index));
            }
            else
            {
                splits.add(rest);
            }
        }
        else
        {
            splits.add(content);
        }
    }

    private static String handleString4Export(String ccc, List<Escaping> es,
            boolean doDecode, String format, String escapingChars)
    {
        StringBuffer sub = new StringBuffer();
        String preProcessed = null;
        String processed = null;
        ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;
        int length = ccc.length();
        for (int j = 0; j < length; j++)
        {
            char char1 = ccc.charAt(j);
            char char2 = (j + 1 < length) ? ccc.charAt(j + 1) : 'X';
            char char3 = (j + 2 < length) ? ccc.charAt(j + 2) : ' ';

            processed = handleChar4Export(es, sub.toString(), char1, char2,
                    char3, format, escapingChars);
            // avoid double escape like "\\'".
            if ("\\".equals(preProcessed) && !"\\".equals(processed)
                    && processed.startsWith("\\"))
            {
                sub.append(char1);
            }
            else
            {
                sub.append(processed);
            }
            preProcessed = processed;
        }

        String subStr = doDecode
                ? m_xmlEncoder.encodeStringBasic(sub.toString())
                : sub.toString();
        return subStr;
    }

    public static void handleOutput4Import(Output p_output, Filter mFilter)
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
        List<Escaping> es = null;
        try
        {
            es = BaseFilterManager.getEscapings(bf);
            SortUtil.sort(es, new PriorityComparator());
        }
        catch (Exception e)
        {
            CATEGORY.error("Get escaping setting failed. ", e);
        }
        if (es == null || es.size() == 0)
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
                    List<Character> processedChars = new ArrayList<Character>();

                    if (segments != null && !segments.isEmpty())
                    {
                        for (Object object : segments)
                        {
                            SegmentNode snode = (SegmentNode) object;
                            String segment = snode.getSegment();
                            // protect internal text to avoid escape
                            List<String> internalTexts = new ArrayList<String>();
                            segment = InternalTextHelper.protectInternalTexts(
                                    segment, internalTexts);

                            String result = handleString4Import(segment, es,
                                    format, false, processedChars);
                            result = InternalTextHelper.restoreInternalTexts(
                                    result, internalTexts);
                            snode.setSegment(result);
                        }
                    }

                    if (processedChars != null && processedChars.size() > 0)
                    {
                        StringBuffer sb = new StringBuffer();
                        for (Character ccc : processedChars)
                        {
                            sb.append(ccc);
                        }

                        elem.setEscapingChars(sb.toString());
                    }

                    break;
                }

                default:
                    // skip all others
                    break;
            }
        }
    }

    public static String handleString4Import(String oriStr, List<Escaping> es,
            String format, boolean isPureText, List<Character> processedChars)
    {
        if (oriStr == null || oriStr.length() <= 1)
            return oriStr;

        if (es == null || es.size() == 0)
            return oriStr;

        boolean doDecode = !isPureText;
        StringBuffer sb = new StringBuffer();
        List<TagIndex> tags = TagIndex.getContentIndexes(oriStr, isPureText);

        int count = tags.size();
        for (int i = 0; i < count; i++)
        {
            TagIndex ti = tags.get(i);

            if (ti.isTag)
            {
                if (isPureText)
                {
                    String ccc = ti.content;
                    String subStr = handleString4Import(ccc, es, doDecode,
                            format, processedChars);
                    sb.append(subStr);
                }
                else
                {
                    sb.append(ti.content);
                }
            }
            else
            {
                if (isPureText)
                {
                    sb.append(ti.content);
                }
                else
                {
                    String ccc = ti.content;
                    String subStr = handleString4Import(ccc, es, doDecode,
                            format, processedChars);
                    sb.append(subStr);
                }
            }
        }

        return sb.toString();
    }

    private static String handleString4Import(String ccc, List<Escaping> es,
            boolean doDecode, String format, List<Character> processedChars)
    {
        StringBuffer sub = new StringBuffer();
        ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;

        int length = ccc.length() - 1;
        // only 1 char, do not need to do un-escape
        if (length == 0)
        {
            sub.append(ccc);
        }
        else
        {
            int j = 0;
            for (; j < length; j++)
            {
                char char1 = ccc.charAt(j);
                char char2 = ccc.charAt(j + 1);
                char char3 = (j + 2 <= length) ? ccc.charAt(j + 2) : ' ';

                boolean processed = handleChar4Import(es, char1, char2);

                if (!processed)
                {
                    sub.append(char1);
                }
                else
                {
                    if (isSpecialFormat(format) && char1 == '\\'
                            && char2 == '\\')
                    {
                        sub.append(char1);
                        j = j + 1;

                        if (char3 != ' ' && processedChars != null
                                && !sub.toString().endsWith("\\\\")
                                && !processedChars.contains(char3))
                        {
                            processedChars.add(char3);
                        }
                    }
                }

                if (j == length - 1)
                {
                    sub.append(char2);
                }
            }
        }

        String subStr = sub.toString();
        String result = doDecode ? m_xmlEncoder.encodeStringBasic(subStr)
                : subStr;
        return result;
    }

    private static boolean handleChar4Import(List<Escaping> es, char char1,
            char char2)
    {
        if (char1 == '\\')
        {
            for (Escaping escaping : es)
            {
                if (!escaping.isUnEscapeOnImport())
                {
                    continue;
                }

                if ((char2 + "").equals(escaping.getCharacter()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private static String handleChar4Export(List<Escaping> es, String before,
            char char1, char char2, char char3, String format,
            String escapingChars)
    {
        for (Escaping escaping : es)
        {
            if (!escaping.isReEscapeOnExport())
            {
                continue;
            }

            if ((char1 + "").equals(escaping.getCharacter()))
            {
                // process special chars in special format
                if (isSpecialFormat(format) && char1 == '\\')
                {
                    if (escapingChars != null && escapingChars.contains("" + char2))
                    {
                        return "\\" + char1;
                    }
                    
                    if ("/:?*<>|\"".contains("" + char2))
                    {
                        return "" + char1;
                    }

                    if ("nrt".contains("" + char2))
                    {
                        // \\\\netapp\\HR
                        if (before != null && before.endsWith("\\\\"))
                        {
                            return "\\" + char1;
                        }

                        // \\netapp\\HR
                        if (Character.isLetter(char3))
                        {
                            return "\\" + char1;
                        }

                        return "" + char1;
                    }
                    else
                    {
                        return "\\" + char1;
                    }
                }

                return "\\" + char1;
            }
        }

        return "" + char1;
    }

    private static boolean isSpecialFormat(String format)
    {
        return IFormatNames.FORMAT_JAVAPROP.equals(format)
                || IFormatNames.FORMAT_HTML.equals(format)
                || IFormatNames.FORMAT_PLAINTEXT.equals(format);
    }
}
