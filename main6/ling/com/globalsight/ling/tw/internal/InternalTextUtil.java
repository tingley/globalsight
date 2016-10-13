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

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.XmlEntities;
import com.globalsight.util.StringUtil;

public class InternalTextUtil
{
    private final static Pattern INTERNAL_START_REGEX_1 = Pattern.compile("<bpt[^>]*?i=\"(\\d*?)\"[^>]*?internal=\"yes\"[^>]*?>[^<]*?</bpt>");
    private final static Pattern INTERNAL_START_REGEX_2 = Pattern.compile("<bpt[^>]*?internal=\"yes\"[^>]*?i=\"(\\d*?)\"[^>]*?>[^<]*?</bpt>");
    private final static Pattern INTERNAL_START_REGEX_3 = Pattern.compile("<bpt[^>]*?internal=\"yes\"[^>]*?i=\"(\\d*?)\"[^>]*?/>");
    private final static Pattern INTERNAL_START_REGEX_5 = Pattern.compile("<bpt[^>]*?i=\"(\\d*?)\"[^>]*?internal=\"yes\"[^>]*?[^>]*?/>");
    
    private final static String INTERNAL_ALL_REGEX_1 = "<bpt[^>]*?i=\"%n%\"[^>]*?internal=\"yes\"[^>]*?>[^<]*?</bpt>(.*?)<ept i=\"%n%\"[^>]*?>[^<]*?</ept>";
    private final static String INTERNAL_ALL_REGEX_2 = "<bpt[^>]*?internal=\"yes\"[^>]*?i=\"%n%\"[^>]*?>[^<]*?</bpt>(.*?)<ept i=\"%n%\"[^>]*?>[^<]*?</ept>";
    private final static String INTERNAL_ALL_REGEX_3 = "<bpt[^>]*?internal=\"yes\"[^>]*?i=\"%n%\"[^>]*?/>(.*?)<ept[^>]*?i=\"%n%\"[^>]*?/>";
    private final static String INTERNAL_ALL_REGEX_5 = "<bpt[^>]*?i=\"%n%\"[^>]*?internal=\"yes\"[^>]*?/>(.*?)<ept[^>]*?i=\"%n%\"[^>]*?/>";
    
    private final static String MRK_TAG = "<mrk ";
    private final static Pattern INTERNAL_START_REGEX_4 = Pattern.compile("<mrk[^>]*?comment=\"internal text, i=(\\d*?)\"[^>]*?/>");
    private final static String INTERNAL_ALL_REGEX_4 = "<mrk[^>]*?comment=\"internal text, i=%n%\"[^>]*?/>";
    
    private final static Pattern REGEX_INTERNAL_1 = Pattern.compile("<bpt[^>]*?i=\"(\\d*?)\"[^>]*?internal=\"yes\"[^>]*?>[^<]*?</bpt>([\\s\\S]*?)<ept[^>]*?i=\"(\\d*?)\"[^>]*?>[^<]*?</ept>");
    private final static Pattern REGEX_INTERNAL_2 = Pattern.compile("<bpt[^>]*?internal=\"yes\"[^>]*?i=\"(\\d*?)\"[^>]*?>[^<]*?</bpt>([\\s\\S]*?)<ept[^>]*?i=\"(\\d*?)\"[^>]*?>[^<]*?</ept>");
    private final static Pattern REGEX_INTERNAL_3 = Pattern.compile("<bpt[^>]*?internal=\"yes\"[^>]*?i=\"(\\d*?)\"[^>]*?/>([\\s\\S]*?)<ept[^>]*?i=\"(\\d*?)\"[^>]*?/>");

    private static final String TAG_REGEX = "<.pt.*?>[^<]*?</.pt>";
    private static final String TAG_REGEX_PH = "<ph[\\s].*?>[^<]*?</ph>";
    private static final String TAG_REGEX_ALONE = "<[^>]*?>";

    private InternalTexts texts = new InternalTexts();
    private InternalTag internalTag;

    public static final String INSIDE_INTERNAL_BRACKET_LEFT = "GS_INSIDE_INTERNAL_BRACKET_LEFT";
    public static final String INSIDE_INTERNAL_BRACKET_RIGHT = "GS_INSIDE_INTERNAL_BRACKET_RIGHT";

    public InternalTextUtil(InternalTag internalTag)
    {
        this.internalTag = internalTag;
    }

    private String removeTags(String segment)
    {
        String s1, s2;
        s2 = segment;
        s1 = StringUtil.replaceWithRE(s2, TAG_REGEX, "");
        while (!s1.equals(s2))
        {
            s2 = s1;
            s1 = StringUtil.replaceWithRE(s2, TAG_REGEX, "");
        }
        
        // replace ph tags - GBS-3287
        s1 = StringUtil.replaceWithRE(s1, TAG_REGEX_PH, "");

        s1 = StringUtil.replaceWithRE(s1, TAG_REGEX_ALONE, "");
        return s1;
    }

    private String removeWhiteSpace(String segment)
    {
        String s1, s2;
        s2 = segment;
        s1 = StringUtil.replace(s2, "  ", " ");
        while (!s1.equals(s2))
        {
            s2 = s1;
            s1 = StringUtil.replace(s2, "  ", " ");
        }

        return s1;
    }

    /**
     * Uses special entities to mark the square brackets inside internal tag.
     * <p>
     * Need to replace back before displaying to user.
     */
    private String convertBrackets(String internalTag)
    {
        if (!internalTag.startsWith("[") && !internalTag.endsWith("]"))
        {
            return internalTag;
        }
        String strInside = internalTag.substring(1, internalTag.length() - 1);
        strInside = StringUtil.replace(strInside, "[", INSIDE_INTERNAL_BRACKET_LEFT);
        strInside = StringUtil.replace(strInside, "]", INSIDE_INTERNAL_BRACKET_RIGHT);

        return "[" + strInside + "]";
    }

    private String preProcessInternalText(String segment, Pattern bptRegex,
            String allRegex) throws DiplomatBasicParserException
    {
        Matcher m = bptRegex.matcher(segment);
        StringBuilder output = new StringBuilder();
        XmlEntities xmlDecoder = new XmlEntities();
        int start = 0;
        // String lastReplaceTag = null;
        // String lastMatchedSegment = null;
        // String lastWrappedSegment = null;

        while (m.find(start))
        {
            String i = m.group(1);
            String regex = StringUtil.replace(allRegex, "%n%", i);
            Pattern p2 = Pattern.compile(regex, Pattern.DOTALL);
            Matcher m2 = p2.matcher(segment);

            if (m2.find())
            {
                String matchedSegment = m2.group();
                String internalSegment = "";
                if (!matchedSegment.startsWith(MRK_TAG))
                {
                    internalSegment = m2.group(1);
                }
                
                internalSegment = removeTags(internalSegment);
                internalSegment = removeWhiteSpace(internalSegment);
                String replaceTag = internalTag.getInternalTag(internalSegment,
                        matchedSegment, texts);
                // for GBS-2580
                String wrappedTag = convertBrackets(replaceTag);
                // for merge
                /*
                if (start != 0 && start == m2.start() && lastReplaceTag != null)
                {
                    String ltag = lastReplaceTag.substring(0,
                            lastReplaceTag.length() - 1);
                    String rtag = replaceTag.substring(1);

                    String newReplaceTag = ltag + rtag;
                    String newMatchedSegment = lastMatchedSegment
                            + matchedSegment;
                    String newWrappedTag = lastWrappedSegment.substring(0,
                            lastWrappedSegment.length() - 1)
                            + wrappedTag.substring(1);

                    replaceTag = newReplaceTag;
                    matchedSegment = newMatchedSegment;
                    wrappedTag = newWrappedTag;
                    
                    int len = output.length();
                    output.delete(len - lastReplaceTag.length(), len);
                    
                    if (texts.getInternalTexts().containsKey(lastReplaceTag))
                    {
                        texts.getInternalTexts().remove(lastReplaceTag);
                    }
                    
                    String wkey = xmlDecoder.decodeString(lastReplaceTag);
                    if (texts.getWrappedInternalTexts().containsKey(wkey))
                    {
                        texts.getWrappedInternalTexts().remove(wkey);
                    }
                }
                lastReplaceTag = replaceTag;
                lastMatchedSegment = matchedSegment;
                lastWrappedSegment = wrappedTag;
                 */ 
                
                texts.addInternalTags(replaceTag, matchedSegment);
                
                if (!wrappedTag.equals(replaceTag))
                {
                    texts.addWrappedInternalTags(
                            xmlDecoder.decodeString(replaceTag),
                            xmlDecoder.decodeString(wrappedTag));
                }
                
                
                output.append(segment.substring(start, m2.start())); 
                output.append(replaceTag);
                start = m2.end();
            }
            else
            {
                throw new DiplomatBasicParserException("Can not find <ept i=\""
                        + i + "\"> from segment:" + segment);
            }
        }
        
        output.append(segment.substring(start)); 
        return output.toString();
    }

    private static Set<String> getInternalIndex(String segment, Pattern p)
    {
        Matcher m = p.matcher(segment);
        Set<String> indexs = new HashSet<String>();
        while (m.find())
        {
            indexs.add(m.group(1));
        }

        return indexs;
    }

    /**
     * Checks if the segment gxml contains an internal text.
     */
    private static boolean isInternalText(String segment, Pattern internalRegex)
    {
        Matcher m = internalRegex.matcher(segment);
        if (m.find())
        {
            String matched = m.group();
            if (segment.trim().length() == matched.trim().length())
            {
                return true;
            }
        }
        return false;
    }

    public static Set<String> getInternalIndex(String segment)
    {
        Set<String> indexs = new HashSet<String>();
        indexs.addAll(getInternalIndex(segment, INTERNAL_START_REGEX_1));
        indexs.addAll(getInternalIndex(segment, INTERNAL_START_REGEX_2));
        indexs.addAll(getInternalIndex(segment, INTERNAL_START_REGEX_3));
        indexs.addAll(getInternalIndex(segment, INTERNAL_START_REGEX_5));

        return indexs;
    }

    public InternalTexts preProcessInternalText(String segment)
            throws DiplomatBasicParserException
    {
        segment = preProcessInternalText(segment, INTERNAL_START_REGEX_1,
                INTERNAL_ALL_REGEX_1);
        segment = preProcessInternalText(segment, INTERNAL_START_REGEX_2,
                INTERNAL_ALL_REGEX_2);
        segment = preProcessInternalText(segment, INTERNAL_START_REGEX_3,
                INTERNAL_ALL_REGEX_3);
        segment = preProcessInternalText(segment, INTERNAL_START_REGEX_5,
                INTERNAL_ALL_REGEX_5);
        if (segment.contains(MRK_TAG))
        {
            segment = preProcessInternalText(segment, INTERNAL_START_REGEX_4,
                    INTERNAL_ALL_REGEX_4);
        }

        texts.setSegment(segment);
        return texts;
    }

    public static boolean isInternalText(String segment)
    {
        return isInternalText(segment, REGEX_INTERNAL_1)
                || isInternalText(segment, REGEX_INTERNAL_2)
                || isInternalText(segment, REGEX_INTERNAL_3);
    }
}
