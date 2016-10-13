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

import java.lang.Math;
import java.util.Collection;
import java.util.Locale;
import java.util.Iterator;

/**
*/
class TokenWeighter 
{
    /** Creates a new instance of TokenWeighter */
    public TokenWeighter() 
    {
    }
    
    /**
     * Assuming all language feature weights are one - then normalize them.
     * We currently ignore the Locale parameter. 
     * Future releases will do locale dependent weighting.
    */
    public static Collection weightTokens(Collection p_tokenList, Locale p_locale)
    {
        // all ngrams weights equal 1 (one). Normalize them here
        // so that the dot product calculation in the term leverage
        // query gives us the right answers.      
        double weight = 1.0/Math.sqrt((double)p_tokenList.size());
        
        Iterator it = p_tokenList.iterator();                
        while(it.hasNext())
        {
            Token t = (Token)it.next();
            t.setWeight((float)weight);
        }
        
        return p_tokenList;
    }
}
