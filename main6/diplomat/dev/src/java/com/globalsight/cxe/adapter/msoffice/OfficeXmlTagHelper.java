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
package com.globalsight.cxe.adapter.msoffice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.util.FileUtil;

public class OfficeXmlTagHelper
{
    private static final String REGEX_WT = "(((<w:r [^>]*>)|(<w:r>)).*?</w:r>)";
    private static final String REGEX_CONTENT = "(<w:t[^>]*>)([^<]*?)</w:t>";

    /**
     * Merges the same tags to one for the xml files.
     * 
     * @param xmlFiles
     *            the paths of all xml files.
     * @throws Exception
     */
    public void mergeTags(String[] xmlFiles) throws Exception
    {
        List<String> wrs = new ArrayList<String>();
        List<Integer> indexes = new ArrayList<Integer>();

        Pattern p = Pattern.compile(REGEX_WT);
        if (xmlFiles != null && xmlFiles.length > 0)
        {
            for (String xmlFile : xmlFiles)
            {
                File f = new File(xmlFile);
                String content = FileUtil.readFile(f, "utf-8");
                content = removeUnusdTags(content);

                int n = 0;
                Matcher m = p.matcher(content);
                while (m.find())
                {
                    wrs.add(m.group());
                    n = content.indexOf(m.group(), n);
                    indexes.add(n);
                }

                mergeTag(f, content, wrs, indexes);
            }
        }
    }

    /**
     * Gets the indexes of all tags that can be merged.
     * 
     * @param wrs
     *            all tags
     * @param indexes
     *            the indexes of all tags.
     * @return the indexes of all tags that can be merged.
     */
    private List<Integer> getMergeindexes(List<String> wrs, List<Integer> indexes)
    {
        List<Integer> mindexes = new ArrayList<Integer>();
        for (int i = 0; i < wrs.size() - 1; i++)
        {
            String s = wrs.get(i);
            if (s.length() + indexes.get(i) == indexes.get(i + 1))
            {
                mindexes.add(indexes.get(i + 1));
            }
        }

        return mindexes;
    }

    private void mergeTag(File f, String content, List<String> wrs,
            List<Integer> indexes) throws IOException
    {
        List<Integer> mergeindexes = getMergeindexes(wrs, indexes);

        for (int i = wrs.size() - 1; i > 0; i--)
        {
            String wr = wrs.get(i);
            String last = wrs.get(i - 1);

            if (mergeindexes.contains(indexes.get(i)))
            {
                String mergedTag = getMergedTags(last, wr);
                if (mergedTag != last)
                {
                    wrs.set(i - 1, mergedTag);
                    content = content.replace(last + wr, mergedTag);
                }
            }
        }

        FileUtil.writeFile(f, content, "utf-8");
    }

    private String getMergedTags(String f1, String f2)
    {
        if (f1.indexOf("<w:br/>") > 0 || f2.indexOf("<w:br/>") > 0)
        {
            return f1;
        }
        
        if (f1.indexOf("<w:b/>") > 0 && f2.indexOf("<w:b/>") < 0)
            return f1;
        
        if (f2.indexOf("<w:b/>") > 0 && f1.indexOf("<w:b/>") < 0)
            return f1;
        
        String firstTemp = f1;
        String secondTemp = f2;
        String first = f1;
        String second = f2;

        firstTemp = firstTemp.replaceAll("<w:r>", "");
        firstTemp = firstTemp.replaceAll("<w:r [^>]*>", "");
        firstTemp = firstTemp.replaceAll("<w:t>[^<]*?</w:t>", "");
        firstTemp = firstTemp.replaceAll("<w:t [^>]*>[^<]*?</w:t>", "");

        secondTemp = secondTemp.replaceAll("<w:r>", "");
        secondTemp = secondTemp.replaceAll("<w:r [^>]*>", "");
        secondTemp = secondTemp.replaceAll("<w:t>[^<]*?</w:t>", "");
        secondTemp = secondTemp.replaceAll("<w:t [^>]*>[^<]*?</w:t>", "");

        boolean isOrderChanged = false;
        if ("</w:r>".equals(firstTemp))
        {
            isOrderChanged = true;
        }
        
        if (!isOrderChanged)
        {
            String temp = secondTemp.replaceAll("<w:rFonts w:hint=\"[^\"]*\"/>", "");
            temp = temp.replaceAll(" w:hint=\"[^\"]*\"", "");
            if (temp.equals(firstTemp))
            {
                isOrderChanged = true;
            }
        }
        
        if (isOrderChanged)
        {
            String temp = firstTemp;
            firstTemp = secondTemp;
            secondTemp = temp;
            
            first = f2;
            second = f1;
        }
        
        boolean matched = secondTemp.equals(firstTemp) || isOrderChanged;
        
        if (!matched && "</w:r>".equals(secondTemp))
        {
            matched = true;
        }
        
        if (!matched)
        {
            String temp = firstTemp.replaceAll("<w:rFonts w:hint=\"[^\"]*\"/>", "");
            if (temp.equals(secondTemp))
            {
                matched = true;
            }
        }
        
        if (matched)
        {
            String content1 = getContent(first);
            String content2 = getContent(second);

            String wt = getWt(first);
            if (wt.length() == 0)
            {
                return first;
            }

            String newContent = isOrderChanged ? content2 + content1 : content1
                    + content2;
            first = first.replace(wt + content1, wt + newContent);

            if (first.indexOf("xml:space=\"preserve\"") < 0
                    && second.indexOf("xml:space=\"preserve\"") > 0)
            {
                first = first.replace("<w:t", "<w:t xml:space=\"preserve\"");
            }

            if (first.indexOf("<w:r>") > 0 && second.indexOf("<w:r>") < 0)
            {
                Pattern p = Pattern.compile("<w:r[^>]*>");
                Matcher m = p.matcher(second);
                if (m.find())
                {
                    first = first.replace("<w:r>", m.group());
                }
            }
        }

        return first;
    }

    private String getWt(String all)
    {
        Pattern p = Pattern.compile(REGEX_CONTENT);
        Matcher m = p.matcher(all);
        if (m.find())
        {
            return m.group(1);
        }

        return "";
    }

    private String getContent(String all)
    {
        Pattern p = Pattern.compile(REGEX_CONTENT);
        Matcher m = p.matcher(all);
        if (m.find())
        {
            return m.group(2);
        }

        return "";
    }

    private String removeUnusdTags(String content)
    {
        String temp = content
                .replace("<w:proofErr w:type=\"spellStart\"/>", "");
        temp = temp.replace("<w:proofErr w:type=\"spellEnd\"/>", "");
        temp = temp.replace("<w:proofErr w:type=\"gramStart\"/>", "");
        temp = temp.replace("<w:proofErr w:type=\"gramEnd\"/>", "");
        temp = temp.replace("<w:lastRenderedPageBreak/>", "");

        return temp;
    }
}
