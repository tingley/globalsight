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
package com.globalsight.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used for Regular expression
 */
public class TagIndex implements Comparable<TagIndex>
{
    private static String re_tag = "<([a-z]+)[^>]+>.*?</(\\1)>";
    private static String re_tagEmpty = "<[a-z]+[^>]+/[\\s]*>";
    private static Pattern p1 = Pattern.compile(re_tag, Pattern.DOTALL);
    private static Pattern p2 = Pattern.compile(re_tagEmpty);

    private static String re_pureTag = "<([a-z]+)[^>]*?>";
    private static Pattern p_pureTag = Pattern.compile(re_pureTag);

    private TagIndex()
    {
    }

    public int start = 0;
    public int end = 0;
    public String content;
    public boolean isTag = true;

    public static List<TagIndex> getContentIndexes(String gxml,
            boolean isPureText)
    {
        List<TagIndex> tags = getTagIndexes(gxml, isPureText);

        List<TagIndex> result = new ArrayList<TagIndex>();
        if (tags.size() > 0)
        {
            int i = 0;

            for (TagIndex tagIndex : tags)
            {
                if (i == tagIndex.start)
                {
                    result.add(tagIndex);
                    i = tagIndex.end;
                }
                else
                {
                    String ccc = gxml.substring(i, tagIndex.start);
                    result.add(TagIndex.createTagIndex(ccc, false, i,
                            tagIndex.start));

                    result.add(tagIndex);
                    i = tagIndex.end;
                }
            }

            if (i < gxml.length())
            {
                String ccc = gxml.substring(i);
                result.add(TagIndex.createTagIndex(ccc, false, i, gxml.length()));
            }
        }
        else
        {
            result.add(TagIndex.createTagIndex(gxml, false, 0, gxml.length()));
        }

        return result;
    }

    public static List<TagIndex> getTagIndexes(String gxml, boolean isPureText)
    {
        List<TagIndex> tags = new ArrayList<TagIndex>();

        if (isPureText)
        {
            Matcher m1 = p_pureTag.matcher(gxml);
            while (m1.find())
            {
                TagIndex ti = createTagIndex(m1.group(), true, m1.start(),
                        m1.end());
                tags.add(ti);
            }
        }
        else
        {
            Matcher m1 = p1.matcher(gxml);
            while (m1.find())
            {
                TagIndex ti = createTagIndex(m1.group(), true, m1.start(),
                        m1.end());
                tags.add(ti);
            }

            Matcher m2 = p2.matcher(gxml);
            while (m2.find())
            {
                TagIndex ti = createTagIndex(m2.group(), true, m2.start(),
                        m2.end());
                tags.add(ti);
            }
        }

        Collections.sort(tags);
        return tags;
    }

    public static TagIndex createTagIndex(String c, boolean isTag, int start,
            int end)
    {
        TagIndex ti = new TagIndex();
        ti.content = c;
        ti.start = start;
        ti.end = end;
        ti.isTag = isTag;

        return ti;
    }

    @Override
    public int compareTo(TagIndex o)
    {
        return this.start - o.start;
    }
}
