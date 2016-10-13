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
package com.globalsight.ling.tm.fuzzy;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import com.globalsight.ling.docproc.GlobalsightBreakIterator;
import com.globalsight.ling.docproc.GlobalsightRuleBasedBreakIterator;

/**
Generates locale specific tokens or language features
for the indexer and fuzzy matcher.
*/
public class Tokenizer
{  
    /**
    Tokenizer constructor comment.
    */
    public Tokenizer()
    {
    }

    /**
    Take the input string and break it up into locale
    specific tokens (language features).

    @param p_tuv java.lang.String
    @param p_locale the string's locale
    @return HashMap - All unique tokens found in the input string.
    */
    public HashMap tokenize(String p_tuv, Locale p_locale, 
        GlobalsightBreakIterator p_breakIterator)
    {
        HashMap tokenMap = new HashMap();
        String token = null;
        Token tokenInstance = null;
        int end;
        int start;
        
        p_breakIterator.setText(p_tuv);

        // Get each token in the tuv and add it to the hash table
        start = p_breakIterator.first();
        for (end = p_breakIterator.next();
             end != GlobalsightBreakIterator.DONE;
             start = end, end = p_breakIterator.next())
        {
            token = p_tuv.substring(start, end);

            // only need unique tokens
            if (!tokenMap.containsKey(token))
            {
                tokenInstance = TokenPool.getInstance(token);
                tokenMap.put(token, tokenInstance);
            }
        }

        return tokenMap;
    }


    /**
    Take the input string and break it up into locale
    specific atoms (language features).

    @param p_tuv java.lang.String
    @param p_locale the string's locale
    @return Map - All unique atoms found in the input string.
    */
    public Map atomize(String p_tuv, Locale p_locale, 
        GlobalsightBreakIterator p_breakIterator)
    {
        Map atomMap = new HashMap();
        String atomString = null;
        int end;
        int start;
        
        p_breakIterator.setText(p_tuv);

        // Get each token in the tuv and add it to the hash table
        start = p_breakIterator.first();
        for (end = p_breakIterator.next();
             end != GlobalsightBreakIterator.DONE;
             start = end, end = p_breakIterator.next())
        {
            atomString = p_tuv.substring(start, end);

            // only need unique tokens
            if (!atomMap.containsKey(atomString))
            {
                atomMap.put(atomString, AtomPool.getInstance(atomString));
            }
        }

        return atomMap;
    }
}
