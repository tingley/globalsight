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

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

public class TargetLocaleLgIdsMapper
{
    private static final Logger CATEGORY =
        Logger.getLogger(
            TargetLocaleLgIdsMapper.class.getName());

    // Key: source page id
    // Value: LocaleLgIdsPair object
    private Map m_spIdMap = new HashMap();


    public void addTargetLocale(Long p_sourcePageId,
        GlobalSightLocale p_targetLocale)
    {
        if(!m_spIdMap.containsKey(p_sourcePageId))
        {
            m_spIdMap.put(p_sourcePageId, new LocaleLgIdsPair());
        }

        LocaleLgIdsPair pair = (LocaleLgIdsPair)m_spIdMap.get(p_sourcePageId);
        pair.addLocale(p_targetLocale);
    }
    

    public void addLgIds(Long p_sourcePageId, Collection p_lgIds)
    {
        if(!m_spIdMap.containsKey(p_sourcePageId))
        {
            m_spIdMap.put(p_sourcePageId, new LocaleLgIdsPair());
        }

        LocaleLgIdsPair pair = (LocaleLgIdsPair)m_spIdMap.get(p_sourcePageId);
        pair.addLgIds(p_lgIds);
    }
    

    public Set getAllSourcePageIds()
    {
        return Collections.unmodifiableSet(m_spIdMap.keySet());
    }
    

    Collection getAllLocaleLgIdsPairs()
    {
        return Collections.unmodifiableCollection(m_spIdMap.values());
    }
    

    public boolean hasMap()
    {
        return m_spIdMap.size() > 0 ? true : false;
    }
    
            
    /**
     * Returns all locales in this object.
     *
     * @return Set of GlobalSightLocale
     */
    public Set getAllLocales()
    {
        Set allLocales = new HashSet();
        
        Iterator pairIt = m_spIdMap.values().iterator();
        while(pairIt.hasNext())
        {
            LocaleLgIdsPair pair = (LocaleLgIdsPair)pairIt.next();
            allLocales.addAll(pair.getLocales());
        }
        
        return allLocales;
    }


    public String toString()
    {
        return m_spIdMap.toString();
    }
    

    class LocaleLgIdsPair
    {
        private Set m_locales = null;
        private Collection m_lgIds = null;
        
        private void addLocale(GlobalSightLocale p_locale)
        {
            if(m_locales == null)
            {
                m_locales = new HashSet();
            }
            m_locales.add(p_locale);
        }
        
        private void addLgIds(Collection p_lgIds)
        {
            m_lgIds = p_lgIds;
        }
        
        Collection getLocales()
        {
            return m_locales;
        }
        
        Collection getLgIds()
        {
            return m_lgIds;
        }


        public String toString()
        {
            StringBuffer sb = new StringBuffer();
            sb.append("[ locales = ")
                .append(m_locales.toString()).append("\r\n");
            sb.append("  LG ids  = ").append(m_lgIds.toString()).append(" ]");
            
            return sb.toString();
        }
        
    }

            
}
