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

package com.globalsight.ling.tw.internal;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XliffInternalTag implements InternalTag
{
    private static final String I_REGEX = "i=\"(\\d*?)\"";
    private static final String PATTERN = "<mrk mtype=\"protected\" comment=\"internal text, i={0}\">{1}</mrk>";
    
    // for GBS-4328 Offline XLF upload fails on attribute order in mrk element
    private static final String PATTERN_REGEX = "<mrk [^>]*comment=\"internal text, i=(\\d*?)\"[^>]*>([^<]*?)</mrk>";
    private static final String REVERT_FORMAT = "<bpt internal=\"yes\" i=\"{0}\"/>{1}<ept i=\"{0}\"/>";

    @Override
    public String getInternalTag(String internalText, String allText,
            InternalTexts texts)
    {
        Pattern pattern = Pattern.compile(I_REGEX);
        Matcher matcher = pattern.matcher(allText);
        String i = "1";
        if (matcher.find())
        {
            i = matcher.group(1);
        }

        return MessageFormat.format(PATTERN, i, internalText);
    }

    private static String getRevertSegment(String i, String internalText)
    {
        return MessageFormat.format(REVERT_FORMAT, i, internalText);
    }

    public static String revertXliffInternalText(String segment)
    {
        Pattern pattern = Pattern.compile(PATTERN_REGEX);
        Matcher matcher = pattern.matcher(segment);
        while (matcher.find())
        {
            segment = segment.replace(matcher.group(), getRevertSegment(matcher
                    .group(1), matcher.group(2)));
            matcher = pattern.matcher(segment);
        }

        return segment;
    }
}
