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

import java.util.Locale;
import java.util.Collection;
import java.util.HashMap;

/**
 */
class Japanese 
extends CJK 
{

    private TokenizerParameters m_params = null;
     
    /** Creates a new instance of Japanese */
    public Japanese() 
    {
    }    
    
    /**
    * Return locale specific tokens.
    * @param p_string: Input string to tokenize.
    */
    public Collection tokenize(String p_string) 
    {
        int lfSize = m_params.getLangFeatureSize();
        HashMap tokenMap = new HashMap(p_string.length()*2);
     
        // get language features
        int tokenLength = p_string.length();
        int i = 0;
        while (i < tokenLength)
        {                              
            // single character for han
            if (isCjkUnified(p_string.charAt(i)))
            {                         
                String lf2 = p_string.substring(i, i+1);
                tokenMap.put(lf2, TokenPool.getInstance(lf2));
            }
            
            // ngram
            if (lfSize+i <= tokenLength)
            {
                String lf  = p_string.substring(i, lfSize+i);     
                tokenMap.put(lf, TokenPool.getInstance(lf));            
            }            
            i++;  
        }
        
        return TokenWeighter.weightTokens(tokenMap.values(), m_params.getLocale());
    }
    
    public void setParameters(TokenizerParameters p_params) 
    {
         m_params = p_params;
    }
    
}
