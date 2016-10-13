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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.globalsight.util.FileUtil;

/**
 * A abstract class that used to deal with the things about the style tag during
 * exporting.
 */
public abstract class StyleUtil
{
    static private final Logger s_logger = Logger.getLogger(StyleUtil.class);

    protected static List<Pattern> OFFICE_PATTERNS = new ArrayList<Pattern>();
    static
    {
        // b
        OFFICE_PATTERNS
                .add(Pattern
                        .compile("<bpt i=\"[^\"]*\" type=\"bold\" erasable=\"yes\"\\s*>&lt;b&gt;</bpt>"));
        OFFICE_PATTERNS.add(Pattern
                .compile("<ept i=\"[^\"]*\"\\s*>&lt;/b&gt;</ept>"));

        // i
        OFFICE_PATTERNS
                .add(Pattern
                        .compile("<bpt i=\"[^\"]*\" type=\"italic\" erasable=\"yes\"\\s*>&lt;i&gt;</bpt>"));
        OFFICE_PATTERNS.add(Pattern
                .compile("<ept i=\"[^\"]*\"\\s*>&lt;/i&gt;</ept>"));

        // u
        OFFICE_PATTERNS
                .add(Pattern
                        .compile("<bpt i=\"[^\"]*\" type=\"ulined\" erasable=\"yes\"\\s*>&lt;u&gt;</bpt>"));
        OFFICE_PATTERNS.add(Pattern
                .compile("<ept i=\"[^\"]*\"\\s*>&lt;/u&gt;</ept>"));

        // sub
        OFFICE_PATTERNS
                .add(Pattern
                        .compile("<bpt i=\"[^\"]*\" type=\"office-sub\" erasable=\"yes\"\\s*>&lt;sub&gt;</bpt>"));
        OFFICE_PATTERNS.add(Pattern
                .compile("<ept i=\"[^\"]*\"\\s*>&lt;/sub&gt;</ept>"));

        // sup
        OFFICE_PATTERNS
                .add(Pattern
                        .compile("<bpt i=\"[^\"]*\" type=\"office-sup\" erasable=\"yes\"\\s*>&lt;sup&gt;</bpt>"));
        OFFICE_PATTERNS.add(Pattern
                .compile("<ept i=\"[^\"]*\"\\s*>&lt;/sup&gt;</ept>"));
    }

    protected static Pattern ATTRIBUTE_PATTERN = Pattern
            .compile("(<[^<>]*=\")([^\"]*<[^\"<>]*>[^\"]*)(\"[^<>]*>)");

    /**
     * Doing something before update segment value.
     * 
     * @param content
     *            the segment content.
     * @return updated segment value.
     */
    public abstract String preHandle(String content);

    /**
     * Doing something after update segment value.
     * 
     * @param content
     *            the segment content.
     * @return the updated segment value,
     */
    public abstract String sufHandle(String content);

    /**
     * Used to generate a random string.
     */
    private static int index = 1;

    /**
     * Gets all sub strings which match the provided regular expression.
     * 
     * @param regex
     *            the provided regular expression.
     * @param s
     *            the string to match.
     * @return all matched sub string.
     */
    public List<String> getAllString(String regex, String s)
    {
        List<String> rs = new ArrayList<String>();

        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(s);
        while (m.find())
        {
            rs.add(m.group());
        }

        return rs;
    }

    /**
     * Remove tag in attribute.
     * 
     * @param filePath
     */
    protected void repairAttributeValue(String filePath)
    {
        try
        {
            String s = FileUtil.readFile(new File(filePath), "utf-8");
            Matcher m = ATTRIBUTE_PATTERN.matcher(s);
            StringBuilder sb = new StringBuilder();
            int index = 0;
            while (m.find())
            {
                sb.append(s.substring(index, m.start()));
                index = m.end();
                sb.append(m.group(1));
                sb.append(m.group(2).replaceAll("<[^>]*>", ""));
                sb.append(m.group(3));
            }
            sb.append(s.substring(index));

            FileUtil.writeFile(new File(filePath), sb.toString(), "utf-8");
        }
        catch (Exception e)
        {
            s_logger.error(e);
        }
    }

    /**
     * Gets all sub strings which match the provided regular expression.
     * 
     * @param p
     *            the pattern with regular expression.
     * @param s
     *            the string to match.
     * @return all matched sub string.
     */
    public List<String> getAllString(Pattern p, String s)
    {
        List<String> rs = new ArrayList<String>();

        Matcher m = p.matcher(s);
        while (m.find())
        {
            rs.add(m.group());
        }

        return rs;
    }

    /**
     * Doing something before exporting. The style tag should be updated here.
     * 
     * @param filePath
     *            the path of the file that maybe have style tags.
     */
    public abstract void updateBeforeExport(String filePath);

    /**
     * Gets a random string.
     * 
     * @return a random string.
     */
    public String getRandom()
    {
        Random rd1 = new Random();
        return "#" + rd1.nextFloat() + index++ + "#";
    }
}
