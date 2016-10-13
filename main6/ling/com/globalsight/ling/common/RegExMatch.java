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
package com.globalsight.ling.common;

import com.sun.org.apache.regexp.internal.RE;

public class RegExMatch implements RegExMatchInterface
{
    private RE m_match = null;

    /**
    */
    public String toString()
    {
        return m_match.getParen(0);
    }

    /**
     * @deprecated No equivalent in Jakarta Regexp 1.1
     */
    public int begin(int p_group)
    {
        return -1;
    }

    /**
     * 
     * 
     * @return int
     * @param p_group
     *            int
     */
    public int beginOffset(int p_group)
    {
        return m_match.getParenStart(p_group);
    }

    /**
     * @deprecated No equivalent in Jakarta Regexp 1.1
     */
    public int end(int p_group)
    {
        return -1;
    }

    /**
     * 
     * 
     * @return int
     * @param p_group
     *            int
     */
    public int endOffset(int p_group)
    {
        return m_match.getParenEnd(p_group);
    }

    /**
     * 
     * 
     * @return java.lang.String
     * @param p_group
     *            int
     */
    public String group(int p_group)
    {
        return m_match.getParen(p_group);
    }

    /**
     * 
     * 
     * @return int
     */
    public int groups()
    {
        return m_match.getParenCount();
    }

    /**
     * 
     * 
     * @return int
     */
    public int length()
    {
        return m_match.getParenLength(0);
    }

    /**
     * RegExMatch constructor comment.
     */
    public RegExMatch(RE p_match)
    {
        super();
        m_match = p_match;
    }
}