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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.common.XmlEntities;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;
import com.globalsight.ling.docproc.extractor.javaprop.JPTmxEncoder;
import com.globalsight.ling.tw.internal.InternalTextUtil;

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
    
    public static String GS_INTERNALT_TAG_START = "<GS-INTERNAL-TEXT>";
    public static String GS_INTERNALT_TAG_END = "</GS-INTERNAL-TEXT>";
    public static String REG_INTERNAL_TEXT = "<(GS-INTERNAL-TEXT)>(.*)</GS-INTERNAL-TEXT>";

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
    public static String handleString(String oriStr, List<InternalText> internalTexts,
            String format, boolean useBpt)
    {
        if (oriStr == null || oriStr.length() == 0)
            return oriStr;

        if (internalTexts == null || internalTexts.size() == 0)
            return oriStr;

        List<String> sList = handleStringWithListReturn(oriStr, internalTexts, format);

        if (useBpt)
        {
            assignIndexToBpt(1, sList);
        }
        
        return listToString(sList);
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
        List<String> sList = new ArrayList<String>();
        sList.add(oriStr);

        if (oriStr == null || oriStr.length() == 0)
            return sList;

        if (internalTexts == null || internalTexts.size() == 0)
            return sList;

        for (InternalText it : internalTexts)
        {
            List<String> result = new ArrayList<String>();
            for (String s : sList)
            {
                result.addAll(handleStringByOneRule(s, it, format));
            }

            sList = result;
        }

        if (sList != null && sList.size() > 0)
        {
            return sList;
        }
        else
        {
            sList = new ArrayList<String>();
            sList.add(oriStr);
            return sList;
        }
    }

    /**
     * Assign id 
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
    private static List<String> handleStringByOneRule(String s, InternalText it, String format)
    {
        String tagStart = GS_INTERNALT_TAG_START;
        String tagEnd = GS_INTERNALT_TAG_END;
        List<String> result = new ArrayList<String>();
        if (s.startsWith(tagStart))
        {
            result.add(s);
            return result;
        }

        if (!it.isRE())
        {
            String internalText = getInternalTextValue(it.getName(), format);
            String itext = resolveInternalTextValue(internalText, format);
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
                    
                    String itext = resolveInternalTextValue(internalText, format);
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
                String msg = "Exception in handleStringByOneRule, re rule:" + it.getName()
                        + " text:" + s;
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
        
        if (IFormatNames.FORMAT_XML.equals(format))
        {
            return m_xmlEncoder.decodeStringBasic(name);
        }
        
        if (IFormatNames.FORMAT_HTML.equals(format))
        {
            return m_htmlDecoder.decodeStringBasic(name);
        }
        
        if (IFormatNames.FORMAT_JAVAPROP_HTML.equals(format)
                || IFormatNames.FORMAT_JAVAPROP_MSG.equals(format)
                || IFormatNames.FORMAT_JAVAPROP.equals(format))
        {
            return m_tmx.encode(name);
        }
        
        return name;
    }
    
    private static String resolveInternalTextValue(String p_value, String format)
    {
        if (format == null)
        {
            return p_value;
        }

        if (IFormatNames.FORMAT_XML.equals(format))
        {
            return m_xmlEncoder.encodeStringBasic(p_value);
        }

        if (IFormatNames.FORMAT_HTML.equals(format) 
                || IFormatNames.FORMAT_WORD_HTML.equals(format)
                || IFormatNames.FORMAT_EXCEL_HTML.equals(format)
                || IFormatNames.FORMAT_POWERPOINT_HTML.equals(format))
        {
            return p_value;
        }

        return p_value;
    }

    /**
     * Protect internal text for segmentation
     * @param p_output
     * @return
     */
    public static List<String> protectInternalTexts(Output p_output)
    {
        List<String> internalTexts = new ArrayList<String>();
        int i = 0;
        for (Iterator it = p_output.documentElementIterator(); it.hasNext(); )
        {
            DocumentElement de = (DocumentElement)it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement)de;
                    String temp = elem.getChunk();
                    List<String> its = getInternalTexts(temp);
                    
                    if (!its.isEmpty())
                    {                        
                        for (String itext : its)
                        {
                            String key = INTERNAL_TEXT_KEY + i + INTERNAL_TEXT_KEY_END;
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
     * @param p_output
     * @param internalTexts
     */
    public static void restoreInternalTexts(Output p_output, List<String> internalTexts)
    {
        for (Iterator it = p_output.documentElementIterator(); it.hasNext(); )
        {
            DocumentElement de = (DocumentElement)it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement)de;
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
                                    String key = INTERNAL_TEXT_KEY + i + INTERNAL_TEXT_KEY_END;
                                    String ori = internalTexts.get(i);
                                    segment = replaceFirstText(segment, key, ori);
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
     * @param p_output
     * @return
     */
    public static String protectInternalTexts(String p_text, List<String> internalTexts)
    {
        int i = 0;

        List<String> its = getInternalTexts(p_text);
        String temp = p_text;

        if (!its.isEmpty())
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
     * @param p_output
     * @param internalTexts
     */
    public static String restoreInternalTexts(String p_text, List<String> internalTexts)
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
    
    private static String replaceFirstText(String src, String subtext, String newtext)
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
        List<String> internalTexts = new ArrayList<String>();
        List<String> ids = new ArrayList<String>();
        ids.addAll(InternalTextUtil.getInternalIndex(src));

        for (int i = 0; i < ids.size(); i++)
        {
            String id = (String) ids.get(i);
            Object[] ob = { id };
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
}
