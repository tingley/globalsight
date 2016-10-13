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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.SkeletonElement;
import com.globalsight.ling.docproc.TranslatableElement;

/**
 * Provides methods to parse strings with emojis.
 * 
 * @since GBS-3997&GBS-4066
 */
public class EmojiUtil
{
    public static final String TYPE_EMOJI = "gs-protect-emoji-";
    public static final Pattern P_EMOJI_TAG = Pattern
            .compile("(<ph[^>]*?erasable=\"yes\"[^>]*?type=\"(emoji-[^<]*?)\"[^>]*?>)[^<]*?(</ph>)");
    public static final Pattern P_SUB_TAG = Pattern
            .compile("<sub[^>]*?>([\\d\\D]*?)</sub>");
    public static final String EMOJI_TAG = "<ph erasable=\"yes\" type=\"emoji-emojiAlias\">emojiDescription</ph>";

    /**
     * Replaces the emoji's alias occurrences by erasable tags.
     */
    private static String parseEmojiAliasToTag(String input)
    {
        String result = input;
        result = parseEmojiAliasToUnicode(result);
        if (!result.equals(input))
        {
            result = parseEmojiUnicodeToTag(result);
        }
        return result;
    }

    /**
     * Replaces the emoji's unicode occurrences by erasable tags.
     */
    private static String parseEmojiUnicodeToTag(String input)
    {
        String result = input;
        for (Emoji emoji : EmojiManager.getAll())
        {
            String replaced = EMOJI_TAG.replace("emojiAlias", emoji
                    .getAliases().get(0));
            replaced = replaced.replace("emojiDescription",
                    emoji.getDescription());

            result = StringUtil.replace(result, emoji.getUnicode(), replaced);
        }
        return result;
    }

    /**
     * Replaces the emoji's unicode occurrences by their alias string with
     * TYPE_EMOJI mark.
     */
    private static String parseEmojiUnicodeToAlias(String input)
    {
        String result = input;
        for (Emoji emoji : EmojiManager.getAll())
        {
            String replaced = TYPE_EMOJI + ":" + emoji.getAliases().get(0)
                    + ":";
            result = StringUtil.replace(result, emoji.getUnicode(), replaced);
        }
        return result;
    }

    /**
     * Replaces the emoji's alias string by their unicodes.
     */
    public static String parseEmojiAliasToUnicode(String input)
    {
        String result = input;
        if (input.contains(TYPE_EMOJI))
        {
            String aliases = StringUtil.replace(result, TYPE_EMOJI, "");
            String unicodes = EmojiParser.parseToUnicode(aliases);
            if (!unicodes.equals(aliases))
            {
                result = unicodes;
            }
        }
        return result;
    }

    /**
     * Replaces the emoji's tag occurrences by their alias string with
     * TYPE_EMOJI mark, for export.
     */
    public static String parseEmojiTagToAlias(String input)
    {
        String result = input;
        Matcher m = P_EMOJI_TAG.matcher(result);
        while (m.find())
        {
            String type = m.group(2);
            if (type.startsWith("emoji-"))
            {
                String alias = type.substring(6);
                String replaced = TYPE_EMOJI + ":" + alias + ":";
                result = StringUtil.replace(result, m.group(), replaced);
            }
        }
        return result;
    }

    /**
     * Protects the emoji's unicode occurrences from corrupting segmentation and
     * word counting.
     */
    public static void protectEmojiUnicodes(Output output)
    {
        for (Iterator it = output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();
            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    String segment = elem.getChunk();

                    if (!StringUtil.isEmpty(segment))
                    {
                        String changed = parseEmojiUnicodeToAlias(segment);
                        if (!changed.equals(segment))
                        {
                            elem.setChunk(changed);
                        }
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    /**
     * Replaces the emoji's unicode and alias occurrences by erasable tags.
     */
    public static void tagEmojiUnicodes(Output output)
    {
        for (Iterator it = output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();
            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;
                    String sid = elem.getSid();
                    if (!StringUtil.isEmpty(sid))
                    {
                        String changed = parseEmojiUnicodeToAlias(sid);
                        if (!changed.equals(sid))
                        {
                            elem.setSid(changed);
                        }
                    }
                    ArrayList segments = elem.getSegments();

                    if (segments != null && !segments.isEmpty())
                    {
                        for (Object object : segments)
                        {
                            SegmentNode snode = (SegmentNode) object;
                            String segment = snode.getSegment();
                            String changed = parseEmojiAliasToTag(segment);
                            if (!changed.equals(segment))
                            {
                                snode.setSegment(changed);
                            }
                        }
                    }
                    break;
                }
                case DocumentElement.SKELETON:
                {
                    SkeletonElement elem = (SkeletonElement) de;
                    String skeleton = elem.getSkeleton();
                    String changed = parseEmojiUnicodeToAlias(skeleton);
                    if (!changed.equals(skeleton))
                    {
                        elem.setSkeleton(changed);
                    }
                }
                default:
                    break;
            }
        }
    }
}
