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
package com.globalsight.ling.tm2.leverage;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * LeverageMatchResults holds a batch of leverage match results.
 * 
 * TODO: this class should be replaced with List<LeverageMatches>.
 */
public class LeverageMatchResults implements Iterable<LeverageMatches>
{
    // list of LeverageMatches
    Collection<LeverageMatches> m_leverageMatches = new ArrayList<LeverageMatches>();
    
    public LeverageMatchResults() { }
    
    public LeverageMatchResults(Collection<LeverageMatches> m) {
        m_leverageMatches.addAll(m);
    }
    
    public void add(LeverageMatches p_leverageMatches)
    {
        m_leverageMatches.add(p_leverageMatches);
    }
    

    public Iterator<LeverageMatches> iterator()
    {
        return m_leverageMatches.iterator();
    }

    public void merge(LeverageMatchResults p_other)
    {
        m_leverageMatches.addAll(p_other.m_leverageMatches);
    }
    
            
}
