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
package com.globalsight.ling.tm2.population;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;


/**
 * A repository of segments. This class is responsible for maintaining
 * uniqueness of source and target segment in this repository.
 */

public class UniqueSegmentRepository
{
    // a repository of Tus. This map maintains unique source Tuvs.
    // key: source BaseTmTuv
    // value: BaseTmTu
    private Map<BaseTmTuv, BaseTmTu> m_tus; 
    private GlobalSightLocale m_sourceLocale;
    
    /**
     * Constructor.
     * @param p_sourceLocale source locale
     */
    public UniqueSegmentRepository(GlobalSightLocale p_sourceLocale)
    {
        m_tus = new HashMap<BaseTmTuv, BaseTmTu>();
        m_sourceLocale = p_sourceLocale;
    }


    /**
     * Get iterator of source Tuvs.
     *
     * @return Iterator of source Tuvs
     */
    public Iterator<BaseTmTuv> sourceTuvIterator()
    {
        return m_tus.keySet().iterator();
    }


    public Collection<BaseTmTu> getAllTus()
    {
        return m_tus.values();
    }
    

    /**
     * Get a Tu that has an identical source Tuv with a given
     * Tuv. Identical Tuvs have the same text and type. Since this
     * class maintains a uniqueness of source Tuvs, there is at most
     * only one matched Tu.
     *
     * @param p_sourceTuv Tuv object
     * @return matched Tu or null if there isn't a match
     */
    public BaseTmTu getMatchedTu(BaseTmTuv p_sourceTuv)
    {
        return (BaseTmTu)m_tus.get(p_sourceTuv);
    }
    

    /**
     * Add Tu (and Tuvs that are owned by the Tu). Only unique
     * segments are added.
     *
     * @param p_tu Tu to be added
     */
    public void addTu(BaseTmTu p_tu)
    {
        BaseTmTuv srcTuv = p_tu.getFirstTuv(m_sourceLocale);
        BaseTmTu tu = (BaseTmTu)m_tus.get(srcTuv);
        if(tu == null)
        {
            // There isn't the same source segment yet. Add the whole p_tu.
            m_tus.put(srcTuv, p_tu);
        }
        else
        {
            // the same source segment is found. Each target Tuvs must
            // be merged into the found Tu.
            for (GlobalSightLocale trgLocale : p_tu.getAllTuvLocales())
            {
                if(!trgLocale.equals(m_sourceLocale))
                {
                    Collection<BaseTmTuv> tuvs = p_tu.getTuvList(trgLocale);
                    if (tuvs != null )
                    {
                        for (BaseTmTuv trgTuv : tuvs)
                        {
                            tu.addTuv(trgTuv);
                        }
                    }
                }
            }
        }
    }


    /**
     * Add a list Tus. This method calls addTu() on each Tu.
     *
     * @param p_tus Collection of BaseTmTu
     */
    public void addTus(Collection p_tus)
    {
        Iterator it = p_tus.iterator();
        while(it.hasNext())
        {
            addTu((BaseTmTu)it.next());
        }
    }
    

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }


    public String toDebugString()
    {
        StringBuffer result = new StringBuffer();
        result.append("\nUniqueSegmentRepository:\n{\n");
        result.append("Source locale = ");
        result.append(m_sourceLocale.toString());
        result.append("\n");

        Iterator itSourceTuv = m_tus.keySet().iterator();
        while(itSourceTuv.hasNext())
        {
            BaseTmTuv sourceTuv = (BaseTmTuv)itSourceTuv.next();
            result.append("Source Tuv = {");
            result.append(sourceTuv.toDebugString());
            result.append("}\n");

            BaseTmTu tu = (BaseTmTu)m_tus.get(sourceTuv);
            result.append("Tu = {");
            result.append(tu.toDebugString(true));
            result.append("}\n");
        }
        result.append("}\n");

        return result.toString();
    }
    
}
