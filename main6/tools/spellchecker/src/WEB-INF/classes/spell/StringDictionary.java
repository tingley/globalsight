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

/**
 * Dictionary represented by a string.
 *
 * Format allowed: words separated by newlines.
 * word1\nword2\r\nword3
 */
public class StringDictionary
    implements Dictionary
{
    private ArrayList m_words = new ArrayList();

    public StringDictionary (String p_words)
    {
        StringTokenizer tok = new StringTokenizer(p_words, "\r\n");
        while (tok.hasMoreTokens())
        {
            String word = tok.nextToken();

            word = word.trim();

            if (word.length() > 0)
            {
                m_words.add(word);
            }
        }
    }

    public Iterator getWordsIterator()
    {
        return m_words.iterator();
    }
}
