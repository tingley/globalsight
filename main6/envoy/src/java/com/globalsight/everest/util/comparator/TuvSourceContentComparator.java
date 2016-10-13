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
package com.globalsight.everest.util.comparator;

import java.util.Comparator;

import com.globalsight.everest.tuv.Tuv;

@SuppressWarnings("rawtypes")
public class TuvSourceContentComparator implements Comparator
{
    private long m_jobId;
    
    public TuvSourceContentComparator(long jobId)
    {
        m_jobId = jobId;
    }

    /**
     * the class is used to compare the tuv list 
     * according to the sourceContent
     */
    public int compare(Object o1, Object o2)
    {
        Tuv tuv1 = (Tuv) o1;
        Tuv tuv2 = (Tuv) o2;
        
        String sc1 = tuv1.getTu(m_jobId).getSourceContent();
        String sc2 = tuv2.getTu(m_jobId).getSourceContent();
        
        if (sc1 == null && sc2 == null)
        {
            return 0;
        }
        else if (sc1 == null)
        {
            return 1;
        }
        else if (sc2 == null) 
        {
            return -1;
        }
        else 
        {
            return sc1.compareTo(sc2);
        }
    }
}
