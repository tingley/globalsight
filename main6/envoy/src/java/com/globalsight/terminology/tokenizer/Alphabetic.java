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
import java.lang.StringBuffer;

/**
 */
class Alphabetic 
implements ITokenizer 
{
    private TokenizerParameters m_params = null;
    
    /** Creates a new instance of Alphabetic */
    public Alphabetic() 
    {
    }

    /**
     * Return locale specific tokens.
     * @param p_string: Input string to tokenize.
     */
    public Collection tokenize(String p_string) 
    {   
        int lfSize = m_params.getLangFeatureSize();
        HashMap tokenMap = new HashMap(p_string.length()+2);
        
        // add term start and end character
        String string = (" " + p_string.trim() + " ").toLowerCase(m_params.getLocale());
     
        // get language features
        int tokenLength = string.length() - (lfSize - 1);
        int i = 0;
        while (i < tokenLength)
        {
            String lf  = string.substring(i, lfSize+i);
            tokenMap.put(lf, TokenPool.getInstance(lf));
            i++;    
        }        
        
        return TokenWeighter.weightTokens(tokenMap.values(), m_params.getLocale());
    }        
    
    public void setParameters(TokenizerParameters p_params) 
    {
        m_params = p_params;
    }
}
