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
package com.globalsight.everest.page.pageexport.style;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfficeTagUtil
{
    private static Pattern PATTERN_BPT = Pattern.compile("<bpt i=\"([^\"]*)\" type=\"([^\"]*)\" erasable=\"yes\">&lt;[^&<]*?&gt;</bpt>");
    private static String REGEX_BPT_ALL = "<bpt i=\"({0})\" type=\"([^\"]*)\" erasable=\"yes\">&lt;([^&<]*?)&gt;</bpt>([\\s\\S]*?)<ept i=\"{0}\">&lt;([^&<]*?)&gt;</ept>";
    private static String FORMAT_ALL = "<bpt i=\"{0}\" type=\"{1}\" erasable=\"yes\">&lt;{2}&gt;</bpt>{3}<ept i=\"{0}\">&lt;/{2}&gt;</ept>";

    private static List<String> OFFICE_STYLE_TAG = new ArrayList<String>();
    static
    {
        OFFICE_STYLE_TAG.add("bold");
        OFFICE_STYLE_TAG.add("italic");
        OFFICE_STYLE_TAG.add("ulined");
        OFFICE_STYLE_TAG.add("office-sub");
        OFFICE_STYLE_TAG.add("office-sup");
    }

    /**
     * For tags maybe nested, style tag should be split to several tags.
     * <p>
     * For example
     * 
     * <pre>
     * [b]a[g1]b[/g1]c[/b]d should be changed to [b]a[/b][g1][b]b[/b][/g1][b]c[/b]d
     * </pre>
     * 
     * @param s
     *            The string need to change.
     * @return The string that have been changed.
     */
    public String handleString(String s)
    {
    	StringBuilder sb = new StringBuilder();
    	int index = 0;
    	
        Matcher m = PATTERN_BPT.matcher(s);
        while (m.find())
        {
            String i = m.group(1);
            String type = m.group(2);

            if (OFFICE_STYLE_TAG.contains(type))
            {
                String regex = MessageFormat.format(REGEX_BPT_ALL, i);

                Pattern p2 = Pattern.compile(regex);
                Matcher m2 = p2.matcher(s);
                if (m2.find())
                {
                    String all = m2.group();

                    List<String> notStyleType = getAllNotStyleType(all);
                    if (notStyleType.size() > 0 || all.indexOf("<it") > 0 || all.indexOf("<ph") > 0)
                    {
                        int maxIndex = getMaxI(s);
                        String newAll = addStyle(all, maxIndex, type,
                                m2.group(3));
                        
                        sb.append(s.substring(index, m.start()));
            			index = m2.end();
            			sb.append(newAll);
                    }
                }
            }
        }
        
        sb.append(s.substring(index));

        return sb.toString();
    }

    private int getMaxI(String s)
    {
        int i = 0;

        Pattern p = Pattern.compile("<[^>]* i=\"([^\"]*)\"[^>]*>");
        Matcher m = p.matcher(s);
        while (m.find())
        {
            int n = Integer.parseInt(m.group(1));
            if (n > i)
            {
                i = n;
            }
        }

        return i + 1;
    }

    private String addStyle(String s, int index, String type, String tagContent)
    {
        int i = index;

        Pattern p = Pattern.compile("(</[^>]*>)([^<]+)(<[^>]*>)");
        Matcher m = p.matcher(s);
        while (m.find())
        {
            String prefix = m.group(1);
            String content = m.group(2);
            String suffix = m.group(3);

            String oldString = m.group();
            String newString = prefix
                    + MessageFormat.format(FORMAT_ALL, i++, type, tagContent,
                            content) + suffix;

            s = s.replace(oldString, newString);
        }

        s = s.replaceFirst("^<bpt[^>]*>[^<]*</bpt>", "");
        s = s.replaceFirst("<ept[^>]*>[^<]*</ept>$", "");

        return s;
    }

    private List<String> getAllNotStyleType(String s)
    {
        List<String> types = new ArrayList<String>();
        Pattern p = Pattern.compile("<[^>]*i=\"[^>]*>");
        Matcher m = p.matcher(s);
        while (m.find())
        {
            Pattern p2 = Pattern.compile("type=\"([^\"]*)");
            Matcher m2 = p2.matcher(m.group());
            String type = null;
            if (m2.find())
            {
                 type = m2.group(1);   
            }
                    
            if (type == null || !OFFICE_STYLE_TAG.contains(type))
            {
                types.add(type);
            }
        }

        return types;
    }
}
