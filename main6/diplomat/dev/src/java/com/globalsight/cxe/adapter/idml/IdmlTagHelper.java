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

package com.globalsight.cxe.adapter.idml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.util.SortUtil;

public class IdmlTagHelper
{
    private static final String REGEX_CHAR_START = "<CharacterStyleRange[^>]*[^/]>";
    private static final String CHAR_END = "</CharacterStyleRange>";
    private static final int END_LENGTH = CHAR_END.length();
    private static final String REGEX_CONTENT = "<Content>([^<]*)</Content>";
    private static final String REGEX_FONT = "<AppliedFont[^>]*>[^<]*</AppliedFont>";

    public String mergeTags(String content)
    {
        List<Integer> startIndexs = getStartIndexs(content);
        List<Integer> endIndexs = getEndIndexs(content);

        if (!isRight(startIndexs, endIndexs))
            return content;

        List<Integer> indexs = removeAloneIndexs(startIndexs, endIndexs);

        List<String> chars = new ArrayList<String>();
        for (int i = 0; i < startIndexs.size(); i++)
        {
            chars.add(content.substring(startIndexs.get(i), endIndexs.get(i)));
        }

        for (Integer i : indexs)
        {
            String s1 = chars.get(i);
            String s2 = chars.get(i + 1);

            if (s1.indexOf("<Br/>") > 0
                    || (s2.indexOf("<Br/>") > 0 && s2.indexOf("<Br/>") < s2
                            .indexOf("<Content>")))
                continue;

            String temp1 = s1.replaceAll("<Content>[^<]*</Content>", "");
            String temp2 = s2.replaceAll("<Content>[^<]*</Content>", "");
            temp2 = temp2.replace("<Br/>", "");

            boolean fontRemoved = false;
            if (temp1.indexOf("AppliedFont") > 0
                    && temp2.indexOf("AppliedFont") < 0)
            {
                temp1 = temp1.replaceAll(REGEX_FONT, "");
                fontRemoved = true;
            }

            if (temp1.equals(temp2))
            {
                String c1 = getContent(s1);
                String c2 = getContent(s2);

                if (c1 == null || c2 == null)
                    continue;

                String all = s2.replace(">" + c2 + "<", ">" + c1 + c2 + "<");
                if (fontRemoved)
                {
                    all = addFont(s1, all);
                }

                content = content.replace(s1 + s2, all);
                chars.set(i + 1, all);
            }
            else
            {
                if (temp1.indexOf("AppliedFont") > 0
                        && temp2.indexOf("AppliedFont") < 0)
                {
                    temp1 = temp1.replaceAll(REGEX_FONT, "");
                }
            }
        }

        return content;
    }

    private String addFont(String s1, String s2)
    {
        Pattern p = Pattern.compile(REGEX_FONT);
        Matcher m = p.matcher(s1);

        if (m.find())
        {
            String font = m.group();
            String[] ss = s2.split("</Properties>");
            if (ss.length != 2)
                return s2;

            return ss[0] + font + "</Properties>" + ss[1];
        }

        return s2;
    }

    private String getContent(String s)
    {
        Pattern p = Pattern.compile(REGEX_CONTENT);
        Matcher m = p.matcher(s);
        String content = null;
        while (m.find())
        {
            if (content != null)
                return null;

            content = m.group(1);
        }

        return content;
    }

    private List<Integer> getStartIndexs(String content)
    {
        Set<String> starts = new HashSet<String>();
        Pattern p = Pattern.compile(REGEX_CHAR_START);
        Matcher m = p.matcher(content);
        while (m.find())
        {
            starts.add(m.group());
        }

        Set<Integer> indexs = new HashSet<Integer>();
        for (String s : starts)
        {
            int n = -1;
            while (true)
            {
                n = content.indexOf(s, n + 1);
                if (n < 0)
                    break;

                indexs.add(n);
            }
        }

        List<Integer> sIndexs = new ArrayList<Integer>(indexs);
        SortUtil.sort(sIndexs, new java.util.Comparator<Integer>()
        {
            @Override
            public int compare(Integer o1, Integer o2)
            {
                return o1 - o2;
            }
        });

        return sIndexs;
    }

    private List<Integer> getEndIndexs(String content)
    {
        List<Integer> indexs = new ArrayList<Integer>();
        int n = -1;
        while (true)
        {
            n = content.indexOf(CHAR_END, n + 1);
            if (n < 0)
                break;

            indexs.add(n + +END_LENGTH);
        }

        return indexs;
    }

    private boolean isRight(List<Integer> starts, List<Integer> ends)
    {
        if (starts.size() != ends.size())
            return false;

        for (int i = 0; i < starts.size(); i++)
        {
            if (i > 0 && starts.get(i) < ends.get(i - 1))
                return false;

            if (starts.get(i) >= ends.get(i))
                return false;
        }

        return true;
    }

    private List<Integer> removeAloneIndexs(List<Integer> starts,
            List<Integer> ends)
    {
        List<Integer> indexs = new ArrayList<Integer>();
        for (int i = 0; i < starts.size() - 1; i++)
        {
            boolean isAlone = true;

            if (ends.get(i).equals(starts.get(i + 1)))
                isAlone = false;

            if (isAlone)
            {
                if (!indexs.contains(i - 1))
                {
                    starts.remove(i);
                    ends.remove(i);
                    i--;
                }
            }
            else
            {
                indexs.add(i);
            }
        }

        return indexs;
    }
}
