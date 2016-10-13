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

import java.io.Serializable;

import com.globalsight.ling.util.GlobalSightCrc;
import com.globalsight.everest.persistence.PersistentObject;

/**
Represents a token or language feature used in fuzzy matching.
*/
public class Token
    extends PersistentObject
{  
    /**
    Constant used for TopLink's query.
    */
    public static final String TUV_ID = "m_tuvId";
    
    private String m_token = null;
    private long m_tokenCrc = 0;
    private long m_tuvId;
    private long m_locale;
    private long m_tmId;
    private int m_tokenCount = 0;
   
    /**
    Token constructor comment.
    */
    public Token()
    {
        super();
    }

    /**
    Constructor
    @param p_token java.lang.String
    */
    public Token(String p_token)
    {
        m_token = p_token;
        m_tokenCrc = GlobalSightCrc.calculate(m_token);
    }

    /**
    Compares two objects for equality. Returns a boolean that indicates
    whether this object is equivalent to the specified object. This method
    is used when an object is stored in a hashtable.

    @param obj the Object to compare with
    @return true if these Objects are equal; false otherwise.
    @see java.util.Hashtable
    */    
    public boolean equals(Object p_token)
    {
        if (!(p_token instanceof Token) || p_token == null)
        {
            return false;
        }
        
        return m_token.equals(p_token.toString());
    }

    /**    
    */
    public long getTokenCrc()
    {
        return m_tokenCrc;
    }

    /**
    Generates a hash code for the receiver.
    This method is supported primarily for
    hash tables, such as those provided in java.util.
    @return an integer hash code for the receiver
    @see java.util.Hashtable
    */
    public int hashCode()
    {
        return m_token.hashCode();
    }

    /**
    @param p_token java.lang.String
    */
    public void setToken(String p_token)
    {
        m_token = p_token;
        m_tokenCrc = GlobalSightCrc.calculate(m_token);
    }

    /**
    Returns a String that represents the value of this object.
    @return a string representation of the receiver
    */
    public String toString()
    {
        return m_token.toString();
    }
    
    /** Getter for property p_locale.
    @return Value of property p_locale.
    */
    public long getLocale() 
    {
        return m_locale;
    }
    
    /** Setter for property p_locale.
    @param p_locale New value of property p_locale.
    */
    public void setLocale(long p_locale) 
    {
        m_locale = p_locale;
    }
    
    /** Getter for property p_tmId.
    @return Value of property p_tmId.
    */
    public long getTmId() 
    {
        return m_tmId;
    }
    
    /** Setter for property p_tmId.
    @param p_tmId New value of property p_tmId.
    */
    public void setTmId(long p_tmId) 
    {
        m_tmId = p_tmId;
    }
    
    /** Getter for property p_tuvId.
    @return Value of property p_tuvId.
    */
    public long getTuvId() 
    {
        return m_tuvId;
    }
    
    /** Setter for property p_tuvId.
    @param p_tuvId New value of property p_tuvId.
    */
    public void setTuvId(long p_tuvId) 
    {
        m_tuvId = p_tuvId;
    }
 
    public long getTokenCount() 
    {
        return m_tokenCount;
    }
    
    public void setTokenCount(int p_tokenCount) 
    {
        m_tokenCount = p_tokenCount;
    }
}