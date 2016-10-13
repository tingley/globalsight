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

/**
 * Represents an atom or language feature (i.e. a word, a n-gram,
 * etc). Objects of this class are immutable.
 */
public class Atom
    implements Serializable
{  
    private String m_atom = null;
    private long m_atomCrc = 0;
    private long m_tokenCrc = 0;
    private long m_tuvId;
    private long m_locale;
    private long m_tmId;
    private int m_tokenCount = 0;

   
    /**
     * Constructor
    */
    public Atom(String p_atom)
    {
        m_atom = p_atom;
        m_atomCrc = GlobalSightCrc.calculate(m_atom);
    }

    /**    
     * Getter of atom
     * @return atom
    */
    public String getAtom()
    {
        return m_atom;
    }

    /**    
     * getter of atom crc
     * @return atom crc
    */
    public long getAtomCrc()
    {
        return m_atomCrc;
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
