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

package com.globalsight.util.edit;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.DynamicPropertiesSystemConfiguration;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.ling.docproc.extractor.html.DynamicRules;

public class SegmentUtil2
{
    private static String TAGS;
    private static SegmentUtil UTIL;

    private static final String STYLE_KEY = "untranslatableWordCharacterStyles";
    private static final String PROPERTY_PATH = "/properties/WordExtractor.properties";

    private static final Logger LOG = Logger
            .getLogger(SegmentUtil2.class.getName());

    public static List getNotTranslateWords(String src)
    {
        List words = getUTIL().getNotTranslateWords(src);
        words.addAll(getUTIL().getInternalWords(src));
        return words;
    }

    public static SegmentUtil getUTIL()
    {
        if (UTIL == null)
        {
            UTIL = new SegmentUtil(getTAGS());
        }
        return UTIL;
    }

    public static String[] getTags()
    {
        return getTAGS().split(",");
    }
    
    public static String getTAGS()
    {
        if (TAGS == null)
        {
            Properties props = ((DynamicPropertiesSystemConfiguration) SystemConfiguration
                    .getInstance(PROPERTY_PATH)).getProperties();
            TAGS = props.getProperty(STYLE_KEY);
            if (TAGS == null)
            {
                TAGS = "";
            }
            
            String[] tagList = TAGS.split(",");
            TAGS = "";
            for (int i = 0; i < tagList.length; i++)
            {
                String tag = tagList[i].trim();
                tag = DynamicRules.normalizeWordStyle(tag);

                if (i > 0)
                {
                    TAGS += ",";
                }
                TAGS += tag;
            }
         }
        
        return TAGS;
    }
}
