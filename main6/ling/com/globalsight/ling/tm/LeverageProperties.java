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

import java.io.Serializable;

/**
 * The LeverageProperties class defines the matching constraints for
 * the Leverager.
 */
public class LeverageProperties
    implements Serializable
{
    private float m_fuzzyThreshold = LeverageType.DEFAULT_FUZZY_THRESHOLD;
    private int m_maximumReturnedHits = 3;
    private int m_matchType = LeverageType.FUZZY;

    public float getFuzzyThreshold()
    {
        return m_fuzzyThreshold;
    }

    public int getMatchType()
    {
        return m_matchType;
    }

    public int getMaximumReturnedHits()
    {
        return m_maximumReturnedHits;
    }

    public void setFuzzyThreshold(float p_fuzzyThreshold)
    {
        m_fuzzyThreshold = p_fuzzyThreshold;
    }

    public void setMatchType(int p_matchType)
    {
        m_matchType = p_matchType;
    }

    public void setMaximumReturnedHits(int p_maximumReturnedHits)
    {
        m_maximumReturnedHits = p_maximumReturnedHits;
    }
}
