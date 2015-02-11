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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A abstract class that used to deal with the things about the style tag during
 * exporting.
 */
public abstract class StyleUtil
{
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
