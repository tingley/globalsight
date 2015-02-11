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

import java.io.IOException;
import java.text.*;
import java.util.*;

/**
 * Spell checking logic: analyzes a string, queries the index for
 * suggestions, returns results.
 */
public class SpellChecker
{
    // Maximum number of suggestions to return.
    static private int NUMSUG = 10;

    // The indexes in which to search (main dict, custom dict).
    private ArrayList m_indexes;
    private Locale m_locale;

    //
    // Constructor
    //

    public SpellChecker(ArrayList p_indexNames, Locale p_locale)
        throws IOException
    {
        m_indexes = getIndexes(p_indexNames);
        m_locale = p_locale;
    }

    //
    // Public Methods
    //

    public SpellCheckResult doSpell(String p_text)
        throws IOException
    {
        SpellCheckResult result = new SpellCheckResult(p_text, m_locale);

        BreakIterator bit = BreakIterator.getWordInstance(m_locale);

        bit.setText(p_text);

        int start = bit.first();
        for (int end = bit.next(); end != BreakIterator.DONE;
             start = end, end = bit.next())
        {
            String word = p_text.substring(start, end);

            // Words start with a letter, not punctuation or digits
            if (!Character.isLetter(word.charAt(0)))
            {
                //System.out.println("WORD `" + word + "' IGNORED (no word)");
                continue;
            }

            // May root out short words here
            if (word.length() < 3)
            {
                //System.out.println("WORD `" + word + "' IGNORED (too short)");
                continue;
            }

            /*
            // Ignore all upper case (acronyms etc)
            if (isAllUpperCase(word))
            {
                //System.out.println("WORD `" + word + "' IGNORED (uppercase)");
                continue;
            }
            */

            Word wordresult = doSpellWord(word, start, end);

            // System.out.println("WORD `" + word + "' " + wordresult);

            result.add(wordresult);
        }

        return result;
    }

    //
    // Private Methods
    //

    private Word doSpellWord(String p_word, int p_start, int p_end)
        throws IOException
    {
        Word result = new Word(p_word, p_start, p_end);

        SuggestWordQueue queue = new SuggestWordQueue(NUMSUG);

        for (int i = 0, max = m_indexes.size(); i < max; i++)
        {
            SpellIndex index = (SpellIndex)m_indexes.get(i);

            if (index.exist(p_word))
            {
                result.setIsCorrect();
                break;
            }

            ArrayList suggestions = index.suggestSimilar(p_word, NUMSUG);

            for (int j = 0, maxj = suggestions.size(); j < maxj; j++)
            {
                SuggestWord sugg = (SuggestWord)suggestions.get(j);

                queue.insert(sugg);
            }
        }

        // Read out the NUMSUG best results
        for (int i = queue.size() - 1; i >= 0; i--)
        {
            result.addSuggestion(((SuggestWord)queue.pop()).string);
        }

        return result;
    }

    private boolean isAllUpperCase(String p_word)
    {
        for (int i = 0, max = p_word.length(); i < max; i++)
        {
            char ch = p_word.charAt(i);

            if (Character.isLetter(ch))
            {
                if (!Character.isUpperCase(ch))
                {
                    return false;
                }
            }
        }

        return true;
    }

    final static public boolean isBlank(String p_string)
    {
        for (int i = 0, max = p_string.length(); i < max; i++)
        {
            char ch = p_string.charAt(i);

            if (!Character.isWhitespace(ch))
            {
                return false;
            }
        }

        return true;
    }

    private ArrayList getIndexes(ArrayList p_indexNames)
        throws IOException
    {
        ArrayList result = new ArrayList();

        for (int i = 0, max = p_indexNames.size(); i < max; i++)
        {
            String name = (String)p_indexNames.get(i);

            SpellIndex index = SpellIndexList.getSpellIndex(name);

            result.add(index);
        }

        return result;
    }
}
