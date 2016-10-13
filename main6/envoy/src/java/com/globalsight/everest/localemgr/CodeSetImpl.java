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
package com.globalsight.everest.localemgr;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Represents a CAP Code set entity object.
 */
public class CodeSetImpl extends PersistentObject implements CodeSet
{

    private static final long serialVersionUID = 170191272480400154L;
    public boolean useActive = false;

    // CONSTRUCTORS
    public CodeSetImpl()
    {
        m_code_set = null;
    }

    /***************************************************************************
     * Constructs an CodeSetImpl from a CodeSet (no deep copy)
     * 
     * @param o
     *            Another CodeSet object *
     **************************************************************************/
    public CodeSetImpl(CodeSet o)
    {
        m_code_set = o.getCodeSet();
    }

    // PUBLIC METHODS

    /**
     * Return the code_set of the code set
     * 
     * @return code set code_set
     */
    public String getCodeSet()
    {
        return m_code_set;
    }

    /**
     * Set the code-set of the code set
     */
    public void setCodeSet(String p_code_set)
    {
        m_code_set = p_code_set;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_code_set;
    }

    // PRIVATE MEMBERS
    private String m_code_set;

    public String getCode_set()
    {
        return m_code_set;
    }

    public void setCode_set(String m_code_set)
    {
        this.m_code_set = m_code_set;
    }
}
