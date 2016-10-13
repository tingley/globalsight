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
package com.globalsight.ling.docproc.extractor.xliff20;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Xliff helper class.
 * 
 * @since GBS-3936
 */
public class XliffHelper
{
    // self-defined marks
    public static final String MARK_XLIFF_PART = "xliffPart";
    public static final String MARK_TU_ID = "tuId";
    public static final String MARK_TUV_ID = "tuvId";
    public static final String MARK_XLIFF_TARGET_LANG = "xliffTargetLang";
    public static final String XLIFF_VERSION = "xliffVersion";
    public static final String XLIFF_VERSION_20 = "2.0";
    
    // for xliff alt
    public static final String MRK = "mrk";
    public static final String MATCH = "mtc:match";
    public static final String MRK_ID = "mrkId";
    public static final String ALT_SOURCE = "altSource";
    public static final String ALT_TARGET = "altTarget";
    
    // xliff 2.0 elements
    public static final String XLIFF = "xliff";
    public static final String FILE = "file";
    public static final String UNIT = "unit";
    public static final String SEGMENT = "segment";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String ATTR_VERSION = "version";
    public static final String ATTR_ID = "id";
    public static final String ATTR_TRANSLATE = "translate";
    public static final String ATTR_TOOL = "tool";
    public static final String ATTR_VALUE_WS = "WorldServer";

    private static Pattern P_XLIFF_VERSION = Pattern
            .compile("<xliff[^>]*?version=\"([\\d\\D]*?)\"[^>]*?>");
    private static Pattern P_XLIFF20_TRGLANG = Pattern
            .compile("<xliff[^>]*?trglang=\"([\\d\\D]*?)\"[^>]*?>");
    private static Pattern P_XLIFF12_TRGLANG = Pattern
            .compile("<file[^>]*?target-language=\"([\\d\\D]*?)\"[^>]*?>");

    /**
     * Gets the xliff version attribute based on given xliff skeleton string
     * content.
     */
    public static String getXliffVersion(String xliffSkeletonString)
    {
        String version = null;
        Matcher m = P_XLIFF_VERSION.matcher(xliffSkeletonString);
        if (m.find())
        {
            version = m.group(1);
        }
        return version;
    }

    /**
     * Gets the xliff target language attribute based on given xliff skeleton
     * string content. This is for xliff 1.2 version.
     */
    public static String getXliff12TargetLanguage(String xliffSkeletonString)
    {
        String trgLang = null;
        Matcher m = P_XLIFF12_TRGLANG.matcher(xliffSkeletonString);
        if (m.find())
        {
            trgLang = m.group(1);
        }
        return trgLang;
    }

    /**
     * Gets the xliff target language attribute based on given xliff skeleton
     * string content. This is for xliff 2.0 version.
     */
    public static String getXliff20TargetLanguage(String xliffSkeletonString)
    {
        String trgLang = null;
        Matcher m = P_XLIFF20_TRGLANG.matcher(xliffSkeletonString);
        if (m.find())
        {
            trgLang = m.group(1);
        }
        return trgLang;
    }
}
