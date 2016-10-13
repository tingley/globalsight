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
package com.globalsight.ling.tm2;

import org.apache.log4j.Logger;

import com.globalsight.util.GlobalSightLocale;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Divides a list of Tuvs to groups by a conbination of tm id, locale
 * and translatable/localizable.
 */

public class TuvSorter
{
    private static Logger c_logger =
        Logger.getLogger(
            TuvSorter.class);

    // key: TuvGroup
    // value: TuvGroup (the same object as key)
    private HashMap m_groups = new HashMap();
    

    /**
     * constructor.
     *
     * @param p_tuvs Collection of BaseTmTuv objects. 
     */
    public TuvSorter(Collection p_tuvs)
    {
        for(Iterator it = p_tuvs.iterator(); it.hasNext();)
        {
            BaseTmTuv tuv = (BaseTmTuv)it.next();
            TuvGroup newGroup = new TuvGroup(tuv.getTu().getTmId(),
                tuv.getLocale(), tuv.isTranslatable());

            TuvGroup existingGroup = (TuvGroup)m_groups.get(newGroup);
            if(existingGroup == null)
            {
                existingGroup = newGroup;
                m_groups.put(existingGroup, existingGroup);
            }
            existingGroup.addTuv(tuv);
        }
    }
    

    /**
     * Returns Iterator of TuvGroup. TuvGroup contains a list of Tuvs
     * and the info of their tm id, locale and
     * translatable/localizable.
     *
     * @return Iterator object
     */
    public Iterator iterator()
    {
        return m_groups.keySet().iterator();
    }


    /**
     * This class holds a group of TUVs that have the same tm id,
     * locale and localizability (translatable/localizable)
     */
    public class TuvGroup
    {
        private long m_tmId;
        private GlobalSightLocale m_locale;
        private boolean m_isTranslatable;
        private ArrayList m_tuvs = new ArrayList();

        private TuvGroup(long p_tmId,
            GlobalSightLocale p_locale, boolean p_isTranslatable)
        {
            m_tmId = p_tmId;
            m_locale = p_locale;
            m_isTranslatable = p_isTranslatable;
        }
        
        public long getTmId()
        {
            return m_tmId;
        }
        
        public GlobalSightLocale getLocale()
        {
            return m_locale;
        }
        
        public boolean isTranslatable()
        {
            return m_isTranslatable;
        }
        
        public ArrayList getTuvs()
        {
            return m_tuvs;
        }

        // derived from Object
        public boolean equals(Object p_group)
        {
            if (p_group instanceof TuvGroup)
            {
                TuvGroup that = (TuvGroup)p_group;
                
                return (m_tmId == that.m_tmId
                    && m_locale.equals(that.m_locale)
                    && m_isTranslatable == that.m_isTranslatable);
            }

            return false;
        }

        // derived from Object
        public int hashCode()
        {
            return (int)(m_tmId + m_locale.hashCode()
                + (m_isTranslatable ? 1 : 0));
        }

        private void addTuv(BaseTmTuv p_tuv)
        {
            m_tuvs.add(p_tuv);
        }
    }
}
