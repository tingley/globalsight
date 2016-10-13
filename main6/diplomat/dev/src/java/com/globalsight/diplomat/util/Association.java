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
 * Represents a simple mapping association between two arbitrary Objects,
 * one a key and the other a value.
 */
public class Association
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private Object m_key;
    private Object m_value;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Create a new association, initialized with the given key and value.
     *
     * @param p_key the key of the new association.
     * @param p_value the value of the new association.
     */
    public Association(Object p_key, Object p_value)
    {
        m_key = p_key;
        m_value = p_value;
    }

    /**
     * Return the key for this association.
     *
     * @return the desired key.
     */
    public Object getKey()
    {
        return m_key;
    }

    /**
     * Return the value for this association.
     *
     * @return the value.
     */
    public Object getValue()
    {
        return m_value;
    }
}

