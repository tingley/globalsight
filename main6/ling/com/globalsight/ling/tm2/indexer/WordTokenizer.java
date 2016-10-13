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
package com.globalsight.ling.tm2.indexer;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.common.Text;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.text.BreakIterator;

/**
 * WordTokenizer tokenize strings using words.
 */

public class WordTokenizer
    implements Tokenizer
{
    static public final int TOKEN_COLUM_LEN = 300;

    /**
     * tokenize a given string and return a collection of Token objects.
     *
     * @param p_segment string to be tokenized
     * @param p_tuvId Tuv id of the segment
     * @param p_tuId Tu id of the segment
     * @param p_tmId Tm id the Tuv belongs to
     * @param p_locale locale of the segment
     * @param p_sourceLocale indicates whether the Tuv is source
     * @return List of Token objects
     */
    public List tokenize(String p_segment, long p_tuvId, long p_tuId,
        long p_tmId, GlobalSightLocale p_locale, boolean p_sourceLocale)
        throws Exception
    {
        StopWord stopWord = StopWord.getStopWord(p_locale);
        
        // map of token and its repetition count
        // key:   token string
        // value: repetition count in Integer
        Map tokenMap = new HashMap();
        
        int totalTokenCount = 0;
        
        BreakIterator wordIterator
            = BreakIterator.getWordInstance(p_locale.getLocale());
        wordIterator.setText(p_segment);
        
        int start = wordIterator.first();
        for(int end = wordIterator.next(); end != BreakIterator.DONE;
            start = end, end = wordIterator.next())
        {
            String tokenString
                = p_segment.substring(start, end);

            // limit the length of the token. Database column for the
            // token has only 300 byte length.
            tokenString
                = Text.substrUtf8LimitLen(tokenString, TOKEN_COLUM_LEN);

            if(!stopWord.isStopWord(tokenString))
            {
                totalTokenCount++;

                int repetition = 0;
                Integer repInteger = (Integer)tokenMap.get(tokenString);
                if(repInteger != null)
                {
                    repetition = repInteger.intValue();
                }
                
                tokenMap.put(tokenString, new Integer(repetition + 1));
            }
        }
        
        // Make Token objects
        List tokenList = new ArrayList(tokenMap.size());
        Iterator it = tokenMap.keySet().iterator();
        while(it.hasNext())
        {
            String tokenString = (String)it.next();
            Token token = new Token(tokenString, p_tuvId, p_tuId, p_tmId,
                ((Integer)tokenMap.get(tokenString)).intValue(),
                totalTokenCount, p_sourceLocale);
            tokenList.add(token);
        }
        
        return tokenList;
    }
    
    
}
