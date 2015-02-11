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

package com.globalsight.terminology.tokenizer;

/**
 */
abstract class CJK
    implements ITokenizer
{
    /** Creates a new instance of Asian */
    public CJK()
    {
    }

    protected boolean isCjkUnified(char p_char)
    {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(p_char);

        if (ub != null)
        {
            if (ub.equals(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) ||
                ub.equals(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS) ||
                ub.equals(Character.UnicodeBlock.CJK_COMPATIBILITY) ||
                ub.equals(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS))
            {
                return true;
            }
        }

        return false;
    }

    protected boolean isHangulSyllable(char p_char)
    {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(p_char);

        if (ub != null)
        {
            if (ub.equals(Character.UnicodeBlock.HANGUL_SYLLABLES))
            {
                return true;
            }
        }

        return false;
    }
}
