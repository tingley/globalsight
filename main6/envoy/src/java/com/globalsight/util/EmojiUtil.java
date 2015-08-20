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

import java.util.regex.Pattern;

/**
 * Provides methods to parse strings with emojis.
 * 
 * @since GBS-3997&GBS-4066
 */
public class EmojiUtil
{
    public static final String TYPE_EMOJI = "emoji-";
    public static final Pattern P_EMOJI_TAG = Pattern
            .compile("(<ph[^>]*?erasable=\"yes\"[^>]*?type=\"(emoji-[^<]*?)\"[^>]*?>)[^<]*?(</ph>)");

    /**
     * Replaces the emoji's unicode occurrences by erasable tags.
     */
    public static String parseEmojiToAliasTag(String text)
    {
        String result = text;
        String tag = "<ph erasable=\"yes\" type=\"emoji-emojiAlias\">emojiDescription</ph>";
        for (Emoji emoji : EmojiManager.getAll())
        {
            String newTag = tag
                    .replace("emojiAlias", emoji.getAliases().get(0));
            newTag = newTag.replace("emojiDescription", emoji.getDescription());
            result = result.replace(emoji.getUnicode(), newTag);
        }
        return result;
    }

    /**
     * Replaces the emoji's unicode occurrences by their alias string with
     * TYPE_EMOJI mark.
     */
    public static String parseEmojiToAliasString(String input)
    {
        String result = input;
        for (Emoji emoji : EmojiManager.getAll())
        {
            result = result.replace(emoji.getUnicode(), TYPE_EMOJI + ":"
                    + emoji.getAliases().get(0) + ":");
        }
        return result;
    }
}
