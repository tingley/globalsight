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
package com.globalsight.ling.tm;

import java.util.Locale;
import java.util.ArrayList;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

/**
 * TM hits (leveraged matches) for a target locale (m_locale).
 */
public class LeverageMatches
    implements Serializable
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            LeverageMatches.class.getName());

    GlobalSightLocale m_locale = null;
    ArrayList<CandidateMatch> m_leverageMatches = null;

    /**
     * LeverageMatches constructor comment.
     */
    public LeverageMatches(GlobalSightLocale p_locale,
        ArrayList<CandidateMatch> p_leverageMatches)
    {
        m_locale = p_locale;
        m_leverageMatches = p_leverageMatches;
    }

    public void add(CandidateMatch p_match)
    {
        m_leverageMatches.add(p_match);
    }

    public boolean equals(Object p_leverageMatches)
    {
        if (p_leverageMatches instanceof LeverageMatches)
        {
            return m_locale.equals(
                ((LeverageMatches)p_leverageMatches).getGlobalSightLocale());
        }
        else
        {
            CATEGORY.error("\n******ERROR: Wrong type passed to LeverageMatches.equals");
            return false;
        }
    }

    public int hashCode()
    {
        return m_locale.getLocale().hashCode();
    }

    public ArrayList<CandidateMatch> getLeverageMatches()
    {
        return m_leverageMatches;
    }

    public Locale getLocale()
    {
        return m_locale.getLocale();
    }

    public GlobalSightLocale getGlobalSightLocale()
    {
        return m_locale;
    }

    /**
     * Combines the LeverageMatch List of self and p_leverageMatches.
     */
    public void combine(LeverageMatches p_leverageMatches)
    {
        m_leverageMatches.addAll(p_leverageMatches.getLeverageMatches());
    }
}
