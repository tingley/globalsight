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
package com.globalsight.ling.docproc;

import java.util.Locale;

/**
 * Util class for Diplomat Word Counter
 */
public class DiplomatWordCountUtil
{
    public static boolean isCJKChar(char p_char)
    {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(p_char);

        if (ub != null)
        {
            if (ub.equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS)
                    || ub.equals(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS)
                    || ub.equals(Character.UnicodeBlock.CJK_COMPATIBILITY)
                    || ub.equals(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS))
            {
                return true;
            }

            if (ub.equals(Character.UnicodeBlock.HANGUL_SYLLABLES))
            {
                return true;
            }

            if (ub.equals(Character.UnicodeBlock.HIRAGANA)
                    || ub.equals(Character.UnicodeBlock.KATAKANA))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isCJKLocale(Locale locale)
    {
        String language = locale.getLanguage();

        if (language.equals("ja"))
        {
            return true;
        }
        else if (language.equals("ko"))
        {
            return true;
        }
        else if (language.equals("zh"))
        {
            return true;
        }

        return false;
    }
}
