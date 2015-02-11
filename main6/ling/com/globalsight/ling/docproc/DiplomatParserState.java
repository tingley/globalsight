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
package com.globalsight.ling.docproc;

/**
 * Used to maintain parser state. We Save Diplomat element type and
 * native format.
 */
public class DiplomatParserState
{
    private int m_elementType = -1;
    private String m_format = null;
    private String m_type = null;

    /**
     * Set our element and format types.
     */
    public DiplomatParserState(int p_elementType, String p_format, String p_type)
    {
        m_elementType = p_elementType;
        m_format = p_format;
        m_type = p_type;
    }

    /**
     * Only equal if ElementType is the same. Ignore format.
     *
     * @return boolean
     * @param p_state com.globalsight.ling.docproc.DiplomatParserState
     */
    public boolean equals(Object p_state)
    {
        boolean equal =
          (getElementType() == ((DiplomatParserState)p_state).getElementType());
        return equal;
    }

    /**
     * Return the element type
     */
    public int getElementType()
    {
        return m_elementType;
    }

    /**
     * Return the format type.
     */
    public String getFormat()
    {
        return m_format;
    }

    /**
     * Return the type.
     */
    public String getType()
    {
        return m_type;
    }
}
