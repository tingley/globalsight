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

/**
*/
public class Token 
{

    private float m_weight = (float)0.0;
    private String m_token = null;
    
    /** Creates a new instance of Token */
    public Token(String p_token, float p_weight) 
    {        
        m_token = p_token;
        m_weight = p_weight;
    }
    
    /** Creates a new instance of Token */
    public Token() 
    {             
    }
    
    public void setToken(String p_token)
    {
        m_token = p_token;
    }
    
    public void setWeight(float p_weight)
    {
         m_weight = p_weight;
    }
    
    public String getToken()
    {
        return m_token;
    }
    
    public float getWeight()
    {
        return m_weight;
    }

    public boolean equals(Object p_token) 
    {
        if (!(p_token instanceof Token) || p_token == null)
        {
            return false;
        }
        
        return m_token.equals(p_token.toString());
    }
    
    public int hashCode() 
    {
        return m_token.hashCode();
    }
    
    public String toString() 
    {
        return m_token;
    }    
}
