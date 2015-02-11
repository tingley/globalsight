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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.globalsight.util.GlobalSightLocale;


/**
 * This class represents a list of target locales of a page. Each
 * locale has a list of compatible locales the user selected for the
 * purpose of the cross locale leveraging.
 */
public class LeveragingLocales
{
    // Key:    target locale (GlobalSightLocale)
    // Value:  leveraging locales (Set of GlobalSightLocale)
    private HashMap<GlobalSightLocale, Set<GlobalSightLocale>> m_locales = null;

    // Constructor
    public LeveragingLocales()
    {
        m_locales = new HashMap<GlobalSightLocale, Set<GlobalSightLocale>>();
    }

    
    /**
     * Set a target locale and its leveraging locales.
     *
     * @param p_targetLocale target locale
     * @param p_leveragingLocales a list of leveraging locales. The
     * elements are GlobalSightLocale objects.
     */
    public void setLeveragingLocale(GlobalSightLocale p_targetLocale,
        Set<GlobalSightLocale> p_leveragingLocales)
    {
        if(p_leveragingLocales == null)
        {
            p_leveragingLocales = new HashSet<GlobalSightLocale>();
        }
        
        p_leveragingLocales.add(p_targetLocale);
        m_locales.put(p_targetLocale, p_leveragingLocales);
    }

    /**
     * Removes the group of leveraging locales for a specific target locale.
     */
    public void removeLeveragingLocales(GlobalSightLocale p_targetLocale)
    {
        m_locales.remove(p_targetLocale);
    }

    public int size()
    {
        return m_locales.size();
    }
    

    /**
     * Get a list of all target locales.
     *
     * @return a list of all target locales. The type of the elements
     * is GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getAllTargetLocales()
    {
        return Collections.unmodifiableSet(m_locales.keySet());
    }


    /**
     * Get a list of all target locales except for the specified locales.
     *
     * @param p_excludeLocales list of locales excluded from the return value
     * @return a list of all target locales. The type of the elements
     *         is GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getAllTargetLocales(Collection<GlobalSightLocale> p_excludeLocales)
    {
        Set<GlobalSightLocale> newSet = new HashSet<GlobalSightLocale>(m_locales.keySet());
        for (GlobalSightLocale locale : p_excludeLocales) 
        {
            if(newSet.contains(locale))
            {
                newSet.remove(locale);
            }
        }
        return Collections.unmodifiableSet(newSet);
    }


    /**
     * Get a list of leveraging locales of a target locale.
     *
     * @param p_targetLocale target locale
     * @return a list of leveraging locales of p_targetLocale. The
     * list always include p_targetLocale. The type of the elements of
     * the list is GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getLeveragingLocales(GlobalSightLocale p_targetLocale)
    {
        return Collections.unmodifiableSet(m_locales.get(p_targetLocale));
    }
    

    /**
     * Get a collective list of leveraging locales of all target locales.
     *
     * @return a list of leveraging locales of all target locales.
     * The type of the elements of the list is GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getAllLeveragingLocales()
    {
        Set<GlobalSightLocale> allLocales = new HashSet<GlobalSightLocale>();
        for (Set<GlobalSightLocale> s : m_locales.values()) 
        {
            allLocales.addAll(s);
        }
        return allLocales;
    }
    

    /**
     * Get a collective list of leveraging locales of all target
     * locales except for the specified target locales.
     *
     * @param p_excludeTargetLocales list of target locales. They and
     * leveraging locales belong to them are excluded from the return
     * value.
     * @return a list of leveraging locales of all target locales.
     * The type of the elements of the list is GlobalSightLocale.
     */
    public Set<GlobalSightLocale> getAllLeveragingLocales(Collection<GlobalSightLocale> p_excludeTargetLocales)
    {
        Set<GlobalSightLocale> allLocales = new HashSet<GlobalSightLocale>();
        for (GlobalSightLocale targetLocale : m_locales.keySet())
        {
            if(!p_excludeTargetLocales.contains(targetLocale))
            {
                allLocales.addAll(m_locales.get(targetLocale));
            }
        }
        return Collections.unmodifiableSet(allLocales);
    }
    
}
