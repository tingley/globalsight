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
package com.globalsight.ling.tm2.leverage;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm2.SegmentTmTuv;
import com.globalsight.ling.tm2.indexer.Token;
import com.globalsight.ling.tm2.indexer.Tokenizer;
import com.globalsight.ling.tm2.lucene.LuceneUtil;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;


/**
 * SegmentForFuzzyMatching stores the information necessary for fuzzy
 * matching.
 */

class SegmentForFuzzyMatching
{
    private static final Logger c_logger =
        Logger.getLogger(
            SegmentForFuzzyMatching.class.getName());

    private long m_tuvId;
    private String m_subId;

    private Map<String, Token> m_tokenMap;
    private int m_totalTokenCount;
    private String m_text;

    
    // constructor
    public SegmentForFuzzyMatching(SegmentTmTuv p_tuv)
        throws Exception
    {
        m_tuvId = p_tuv.getId();
        m_subId = ((SegmentTmTu)p_tuv.getTu()).getSubId();
        
        String text = p_tuv.getFuzzyIndexFormat();

        List<Token> tokens = LuceneUtil.createGsTokens(
                text, p_tuv.getLocale());

        m_text = text;
        m_tokenMap = new HashMap<String, Token>(tokens.size());
        
        if(tokens.size() > 0)
        {
            m_totalTokenCount = tokens.get(0).getTotalTokenCount();

            for (Token token : tokens)
            {
                m_tokenMap.put(token.getTokenString(), token);
            }
        }
        else
        {
            m_totalTokenCount = 0;
        }
    }
    

    public long getTuvId()
    {
        return m_tuvId;
    }
    
    public String getSubId()
    {
        return m_subId;
    }
    
    public String getAllText()
    {
        return m_text;
    }
    
    public Set<String> getAllTokenStrings()
    {
        return m_tokenMap.keySet();
    }
    
    public Token getTokenByTokenString(String p_tokenString)
    {
        return m_tokenMap.get(p_tokenString);
    }

    public int getTotalTokenCount()
    {
        return m_totalTokenCount;
    }
}
