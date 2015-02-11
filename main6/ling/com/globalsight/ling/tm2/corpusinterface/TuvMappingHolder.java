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
package com.globalsight.ling.tm2.corpusinterface;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

/**
 * The TuvMappingHolder holds mappings of segments in project_tm_tuv_t
 * and translation_unit_variant tables. Each TuvMappingHolder object
 * holds mappings for a page. Objects are obtained as a return value
 * of populatePageForAllLocales and populatePageByLocale methods in
 * TmCoreManager class. These objects are used to build the Corpus
 * based TM.
 */

public class TuvMappingHolder
    implements Serializable
{
    private static Logger c_logger =
        Logger.getLogger(
            TuvMappingHolder.class.getName());

    // key:   GlobalSightLocale
    // Value: a HashMap of TuvMapping objects
    private Map<GlobalSightLocale, Map<Long, TuvMapping>> m_tuvMappings = 
        new HashMap<GlobalSightLocale, Map<Long, TuvMapping>>();


    /**
     * Add a mapping of translation_unit_variant id and
     * project_tm_tuv_t id.
     *
     * @param p_locale GlobalSightLocale of the segment
     * @param p_tuvId translation_unit_variant id
     * @param p_tmId TM id of the mapped tuv
     * @param p_tmTuId tuid of the mapped tuv
     * @param p_tmTuvId tuvid of the mapped tuv
     *
     */
    public void addMapping(GlobalSightLocale p_locale,
                           long p_tuvId, long p_tuId,
                           long p_tmId, 
                           long p_tmTuId, long p_tmTuvId)
    {
        Map<Long, TuvMapping> mapping = m_tuvMappings.get(p_locale);
        if(mapping == null)
        {
            mapping = new HashMap<Long, TuvMapping>();
            m_tuvMappings.put(p_locale, mapping);
        }
        
        TuvMapping tuvMapping = new TuvMapping(p_tuvId, p_tuId,
                                   p_tmId, p_tmTuId, p_tmTuvId);
        mapping.put(tuvMapping.getTuvId(),tuvMapping);
    }


    /**
     * Get mappings for a locale.
     *
     * @return a HashMap of mappings for a specified locale.
     */
    public Map<Long, TuvMapping> getMappingsByLocale(GlobalSightLocale p_locale)
    {
        return m_tuvMappings.get(p_locale);
    }
    

    /**
     * Get all locales (including source) of the mappings. 
     *
     * @return a Set of GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getAllLocales()
    {
        return m_tuvMappings.keySet();
    }


    public String toDebugString()
    {
        StringBuffer sb = new StringBuffer();
        
        Iterator itLocale = getAllLocales().iterator();
        while(itLocale.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale)itLocale.next();
            sb.append("Locale: ").append(locale.toString()).append("\n");

            Iterator itTuvMapping = getMappingsByLocale(locale).values().iterator();
            while(itTuvMapping.hasNext())
            {
                TuvMapping tuvMapping = (TuvMapping)itTuvMapping.next();
                sb.append(tuvMapping.toDebugString());
            }
        }
        
        return sb.toString();
    }
    
}
