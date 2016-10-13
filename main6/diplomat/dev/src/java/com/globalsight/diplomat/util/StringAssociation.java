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
package com.globalsight.diplomat.util;

/**
 * This is a decorator for the basic Association class, adding functionality
 * to ensure that all keys and values are Strings only.
 */
public class StringAssociation
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private Association m_assoc;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Create a new string association, initialized with the given key and
     * value.
     *
     * @param p_key the key of the new association.
     * @param p_value the value of the new association.
     */
    public StringAssociation(String p_key, String p_value)
    {
        m_assoc = new Association(p_key, p_value);
    }

    /**
     * Return the key for this string association.
     *
     * @return the desired key.
     */
    public String getKey()
    {
        return (String)m_assoc.getKey();
    }

    /**
     * Return the value for this string association.
     *
     * @return the value.
     */
    public String getValue()
    {
        return (String)m_assoc.getValue();
    }
}

