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
package com.globalsight.ling.docproc.merger.fm;

import java.util.ArrayList;
import java.util.List;

/**
 * The Parser to parse the mapping configuration
 */
public class FontMappingParser
{
    /**
     * key zh_cn, value Arial|MingLiU, Times New Roman|MingLiU
     * 
     * @param key
     * @param value
     * @return
     */
    public static List<FontMapping> parse(String key, String value)
    {
        List<FontMapping> fms = new ArrayList<FontMapping>();
        String targetLocale = key;
        String sourceFont = null;
        String targetFont = null;

        String[] fontArray = value.split(",");
        for (String fontPair : fontArray)
        {
            String[] fonts = fontPair.trim().split("\\|");
            sourceFont = fonts[0].trim();
            targetFont = fonts[1].trim();
            FontMapping fm = new FontMapping();
            fm.setSourceFont(sourceFont);
            fm.setTargetLocale(targetLocale);
            fm.setTargetFont(targetFont);
            fms.add(fm);
        }

        return fms;
    }

    /**
     * zh_cn_default=MingLiU
     * 
     * @param key
     * @param value
     * @return
     */
    public static FontMapping parseOne(String key, String value)
    {
        int index = key.lastIndexOf("_");

        FontMapping fm = null;
        if (index != -1)
        {
            String locale = key.substring(0, index);
            fm = new FontMapping();
            fm.setTargetLocale(locale);
            fm.setTargetFont(value);
            fm.setDefault(true);
        }

        return fm;
    }
}
