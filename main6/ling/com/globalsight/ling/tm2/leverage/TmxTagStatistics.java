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

import java.util.Map;
import java.util.HashMap;

/**
 * TmxTagStatistics records the number of occurence of the same TMX
 * tags in a Tuv. The same tags means that they are the same element
 * (bpt, ept, ph or it) and their type attribute value are the
 * same. Eraseable tags are not counted in the statistics. The
 * statistics can be used to determine if two segment's tag structures
 * are similar enough to call them the same.
 */
public class TmxTagStatistics
{
    // map of tag type and its number of occurrence
    // key:    String of element name + its type, e.g. "bpt-link"
    // value:  number of occurrence of the key (Integer)
    private Map m_statistics = new HashMap();


    // add a tag
    public void add(String p_elementName, String p_type)
    {
        String key = p_elementName + "-" + p_type;
        Integer occurrence = (Integer)m_statistics.get(key);
        int newValue = 0;
        if(occurrence != null)
        {
            newValue = occurrence.intValue();
        }
        newValue++;
        
        m_statistics.put(key, new Integer(newValue));
    }
    

    // test if the tow tag statistics are the same
    public boolean areSame(TmxTagStatistics p_other)
    {
        return m_statistics.equals(p_other.m_statistics);
    }

    
    public String toDebugString()
    {
        return "\nTmxTagStatistics:\n" + m_statistics.toString();
    }
    
}
