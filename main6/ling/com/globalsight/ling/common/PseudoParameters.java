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

public final class PseudoParameters
{
    public double m_growthFactor;
    public int m_rangeUp;
    public int m_rangeDown;
    public boolean m_bKeepSpace;

    public PseudoParameters(double p_growthFactor, int p_rangeUp,
        int p_rangeDown, boolean p_bKeepSpace)
    {
        m_growthFactor = p_growthFactor;
        m_rangeUp = p_rangeUp;
        m_rangeDown = p_rangeDown;
        m_bKeepSpace = p_bKeepSpace;
    }
}


