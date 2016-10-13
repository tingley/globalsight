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
package com.globalsight.everest.webapp.pagehandler.tm.corpus;

import com.globalsight.ling.tm2.leverage.LeverageOptions;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import java.util.Collection;
import java.util.ArrayList;

/**
 * OverrideableLeverageOptions is a LeverageOptions, but allows
 * values to be over-ridden, so they are not read from the underlying
 * TM Profile.
 */
public class OverridableLeverageOptions extends LeverageOptions
{

    /**
     * the fuzzy threshold value to use
     */
    private int m_overriddenFuzzyThreshold = 50;
    private ArrayList<Long> m_overriddenTms = null;

    /**
     * Creates a LeverageOptions with a fuzzy match threshold
     * that can be overridden
     * 
     * @param p_tmProfile
     * @param p_leveragingLocales
     */
    public OverridableLeverageOptions(TranslationMemoryProfile p_tmProfile,
            LeveragingLocales p_leveragingLocales)
    {
        super(p_tmProfile,p_leveragingLocales);
    }



    /**
     * Gets the fuzzy threshold
     * 
     * @return 
     */
    public int getMatchThreshold()
    {
        return m_overriddenFuzzyThreshold;
    }

    /**
     * Sets the fuzzy match threshold
     * 
     * @param p_value
     */
    public void setMatchThreshold(int p_value)
    {
        m_overriddenFuzzyThreshold = p_value;
    }

    /**
     * Returns the collection of overridden TM ids
     * 
     * @return 
     */
    public Collection<Long> getTmIdsToLeverageFrom()
    {
        return m_overriddenTms;
    }

    /**
     * Sets the collection of overridden TM ids
     * 
     */
    public void setTmsToLeverageFrom(Collection<Long> p_tmIds)
    {
        m_overriddenTms = new ArrayList<Long>(p_tmIds);
    }
}
