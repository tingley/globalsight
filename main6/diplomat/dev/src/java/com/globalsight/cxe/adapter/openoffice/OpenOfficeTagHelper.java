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

package com.globalsight.cxe.adapter.openoffice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.util.FileUtil;

public class OpenOfficeTagHelper
{
    private static final String REGEX_SPAN = "(((<text:span [^>]*>)|(<text:span>))[^<]*?</text:span>)";
    private static final String REGEX_CONTENT_SPAN = "(<text:span[^>]*>)([^<]*?)</text:span>";
    private int m_type = OpenOfficeHelper.OPENOFFICE_ODT;

    public OpenOfficeTagHelper(int type)
    {
        m_type = type;
    }

    /**
     * Merges the near tags with same style to one tag for the xml files.
     * 
     * @param xmlFiles
     *            the paths of all XML files.
     * @param type
     *            the file type
     * @throws Exception
     */
    public void mergeTags(String[] xmlFiles) throws Exception
    {
        if (xmlFiles != null && xmlFiles.length > 0)
        {
            for (String xmlFile : xmlFiles)
            {
                if (!xmlFile.endsWith("content.xml"))
                {
                    continue;
                }
                File f = new File(xmlFile);
                String content = FileUtil.readFile(f, "utf-8");
                String newContent = mergeTag(content);
                FileUtil.writeFile(f, newContent, "utf-8");
            }
        }
    }
    
    public static String mergeTag(String content) throws Exception
    {
        List<String> spans = new ArrayList<String>();
        List<Integer> indexes = new ArrayList<Integer>();
        Pattern p = Pattern.compile(REGEX_SPAN);
        
        int n = 0;
        Matcher m = p.matcher(content);
        while (m.find())
        {
            spans.add(m.group());
            n = content.indexOf(m.group(), n);
            indexes.add(n);
        }

        String newContent = mergeTag(content, spans, indexes);
        
        return newContent;
    }

    private static String mergeTag(String content, List<String> spans, List<Integer> indexes)
            throws IOException
    {
        List<Integer> mergeindexes = getMergeindexes(spans, indexes);

        for (int i = spans.size() - 1; i > 0; i--)
        {
            String span = spans.get(i);
            String last = spans.get(i - 1);

            if (mergeindexes.contains(indexes.get(i)))
            {
                String mergedTag = getMergedTags(last, span);
                if (mergedTag != last)
                {
                    spans.set(i - 1, mergedTag);
                    content = content.replace(last + span, mergedTag);
                }
            }
        }
        
        return content;
    }

    private static List<Integer> getMergeindexes(List<String> spans, List<Integer> indexes)
    {
        List<Integer> mindexes = new ArrayList<Integer>();
        for (int i = 0; i < spans.size() - 1; i++)
        {
            String s = spans.get(i);
            if (s.length() + indexes.get(i) == indexes.get(i + 1))
            {
                mindexes.add(indexes.get(i + 1));
            }
        }

        return mindexes;
    }

    public static String getMergedTags(String f1, String f2)
    {
        String styleNameStart = " text:style-name=\"";
        String attEnd = "\"";
        boolean merged = false;
        String f1temp = new String(f1);
        String f2temp = new String(f2);
        String first = new String(f1);
        String second = new String(f2);

        StringIndex style1 = StringIndex.getValueBetween(new StringBuffer(f1temp), 0,
                styleNameStart, attEnd);
        StringIndex style2 = StringIndex.getValueBetween(new StringBuffer(f2temp), 0,
                styleNameStart, attEnd);
        
        if (style1 != null && style2 != null && style1.value.equals(style2.value))
        {
            merged = true;
        }
        
        if (merged)
        {

            String content1 = getContent(first);
            String content2 = getContent(second);

            String spanTag = getTextTag(first);
            if (spanTag.length() == 0)
            {
                return first;
            }

            String newContent = content1 + content2;
            first = first.replace(spanTag + content1, spanTag + newContent);
            
            return first;
        }
        else
        {
            return f1;
        }
    }

    private static String getTextTag(String all)
    {
        Pattern p = Pattern.compile(REGEX_CONTENT_SPAN);
        Matcher m = p.matcher(all);
        if (m.find())
        {
            return m.group(1);
        }

        return "";
    }

    private static String getContent(String all)
    {
        Pattern p = Pattern.compile(REGEX_CONTENT_SPAN);
        Matcher m = p.matcher(all);
        if (m.find())
        {
            return m.group(2);
        }

        return "";
    }
}