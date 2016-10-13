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
package spell;

import java.util.*;

public class SpellCheckResult
{
    public String m_text;
    public Locale m_locale;
    public ArrayList m_words = new ArrayList();

    public SpellCheckResult(String p_text, Locale p_locale)
    {
        m_text = p_text;
        m_locale = p_locale;
    }

    public void add(Word p_word)
    {
        m_words.add(p_word);
    }

    /**
     * Returns list of Word objects.
     */
    public ArrayList getWords()
    {
        return m_words;
    }
}
