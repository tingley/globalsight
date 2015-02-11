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
            String format, boolean noTag, boolean doDecode)
    {
        if (oriStr == null || oriStr.length() == 0)
            return oriStr;

        if (es == null || es.size() == 0)
            return oriStr;

        StringBuffer sb = new StringBuffer();
        
        List<TagIndex> tags = null;
        
        if (noTag)
        {
            TagIndex ti = TagIndex.createTagIndex(oriStr, false, 0, oriStr.length());
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
                sb.append(ti.content);
            }
            else
            {
                StringBuffer sub = new StringBuffer();
                String ccc = ti.content;
                ccc = doDecode ? m_xmlEncoder.decodeStringBasic(ccc) : ccc;
                int length = ccc.length();
                for (int j = 0; j < length; j++)
                {
                    char char1 = ccc.charAt(j);

                    String processed = handleChar4Export(es, char1);
                    sub.append(processed);
                }

                String subStr = doDecode ? m_xmlEncoder.encodeStringBasic(sub
                        .toString()) : sub.toString();
                sb.append(subStr);
            }
        }

        return sb.toString();
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

                    if (segments != null && !segments.isEmpty())
                    {
                        for (Object object : segments)
                        {
                            SegmentNode snode = (SegmentNode) object;
                            String segment = snode.getSegment();
                            // protect internal text to avoid escape
                            List<String> internalTexts = new ArrayList<String>();
                            segment = InternalTextHelper.protectInternalTexts(segment, internalTexts);

                            String result = handleString4Import(segment, es,
                                    format, false);
                            result = InternalTextHelper.restoreInternalTexts(result, internalTexts);
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

    public static String handleString4Import(String oriStr, List<Escaping> es,
            String format, boolean isPureText)
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
                    String subStr = handleString4Import(ccc, es, doDecode);
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
                    String subStr = handleString4Import(ccc, es, doDecode);
                    sb.append(subStr);
                }
            }
        }

        return sb.toString();
    }

    private static String handleString4Import(String ccc, List<Escaping> es,
            boolean doDecode)
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

                boolean processed = handleChar4Import(es, char1, char2);

                if (!processed)
                {
                    sub.append(char1);
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

    private static String handleChar4Export(List<Escaping> es, char char1)
    {
        for (Escaping escaping : es)
        {
            if (!escaping.isReEscapeOnExport())
            {
                continue;
            }

            if ((char1 + "").equals(escaping.getCharacter()))
            {
                return "\\" + char1;
            }
        }

        return "" + char1;
    }
}
