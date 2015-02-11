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
package com.globalsight.diplomat.util.database;

/**
 * Represents a mapping between a key, a value and a width.  This type of
 * association is a helper class used only by the ResultSetProxy.
 */
public class ColumnAssociation
{
    //
    // PRIVATE MEMBER VARIABLES
    //
    private String m_key;
    private String m_value;
    private int m_width;

    //
    // PUBLIC CONSTRUCTORS
    //
    /**
     * Create a new column association, initialized with the given parameters.
     *
     * @param p_key the key of the new association.
     * @param p_value the value of the new association.
     * @param p_width the width for the underlying column.
     */
    public ColumnAssociation(String p_key, String p_value, int p_width)
    {
        m_key = p_key;
        m_value = p_value;
        m_width = p_width;
    }

    /**
     * Return the key for this association.
     *
     * @return the desired key.
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Return the value for this association.
     *
     * @return the value.
     */
    public String getValue()
    {
        return m_value;
    }

    /**
     * Return the column width.
     *
     * @return the width.
     */
    public int getWidth()
    {
        return m_width;
    }
}

