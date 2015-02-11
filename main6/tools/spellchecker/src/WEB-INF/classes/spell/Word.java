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

public class Word
{
    public String m_word;
    public int m_start;
    public int m_end;
    public boolean m_correct = false;
    public ArrayList m_suggestions = new ArrayList();

    public Word(String p_word, int p_start, int p_end)
    {
        m_word = p_word;
        m_start = p_start;
        m_end = p_end;
    }

    public String getText()
    {
        return m_word;
    }

    public void setIsCorrect()
    {
        m_correct = true;
    }

    public boolean isCorrect()
    {
        return m_correct;
    }

    public void addSuggestion(String p_suggestion)
    {
        m_suggestions.add(p_suggestion);
    }

    /**
     * @return list of String objects
     */
    public ArrayList getSuggestions()
    {
        return m_suggestions;
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer();

        result.append("suggestions: ");

        for (int i = 0, max = m_suggestions.size(); i < max; i++)
        {
            String sugg = (String)m_suggestions.get(i);

            result.append("`" + sugg + "' ");
        }

        return result.toString();
    }
}
