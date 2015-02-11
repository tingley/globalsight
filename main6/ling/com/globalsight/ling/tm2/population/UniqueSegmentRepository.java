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
import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.ling.tm2.BaseTmTu;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.SegmentTmTu;
import com.globalsight.ling.tm3.core.TM3Attribute;
import com.globalsight.ling.tm3.core.TM3Tu;

import java.util.ArrayList;
import java.util.List;
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
    private Map<BaseTmTuv, ArrayList<BaseTmTu>> m_attTus;
    
    /**
     * Constructor.
     * @param p_sourceLocale source locale
     */
    public UniqueSegmentRepository(GlobalSightLocale p_sourceLocale)
    {
        m_tus = new HashMap<BaseTmTuv, BaseTmTu>();
        m_attTus = new HashMap<BaseTmTuv, ArrayList<BaseTmTu>>();
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
        List<BaseTmTu> result = new ArrayList<BaseTmTu>();
        result.addAll(m_tus.values());
        result.addAll(getAllAttTus());
        
        return result;
    }
    
    public Collection<BaseTmTu> getAllAttTus()
    {
        List<BaseTmTu> result = new ArrayList<BaseTmTu>();
        Collection<ArrayList<BaseTmTu>> values = m_attTus.values();
        if (values != null && values.size() > 0)
        {
            for (ArrayList<BaseTmTu> list : values)
            {
                result.addAll(list);
            }
        }
        
        return result;
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
     * Get a Tu that has an identical source Tuv with a given
     * Tuv with attributes. 
     * @param p_sourceTuv Tuv object
     * @return matched Tu or null if there isn't a match
     */
    public List<BaseTmTu> getMatchedAttTu(BaseTmTuv p_sourceTuv)
    {
        return m_attTus.get(p_sourceTuv);
    }
    
    public List<BaseTmTu> getAllMatchedTu(BaseTmTuv p_sourceTuv)
    {
        ArrayList<BaseTmTu> result = new ArrayList<BaseTmTu>();
        BaseTmTu f = getMatchedTu(p_sourceTuv);
        if (f != null)
        {
            result.add(f);
        }
        
        ArrayList<BaseTmTu> atttus = m_attTus.get(p_sourceTuv);
        if (atttus != null)
        {
            result.addAll(atttus);
        }
        
        return result;
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
            if (isMergeAble(tu, p_tu))
            {
                mergeTu(p_tu, tu);
            }
            else
            {
                ArrayList<BaseTmTu> attTus = m_attTus.get(srcTuv);
                if (attTus == null)
                {
                    attTus = new ArrayList<BaseTmTu>();
                    attTus.add(p_tu);
                    m_attTus.put(srcTuv, attTus);
                }
                else
                {
                    boolean isMerged = false;
                    for (BaseTmTu curTu : attTus)
                    {
                        if (isMergeAble(curTu, p_tu))
                        {
                            curTu = mergeTu(p_tu, curTu);
                            isMerged = true;
                            break;
                        }
                    }
                    
                    if (!isMerged)
                    {
                        attTus.add(p_tu);
                    }
                }
            }
        }
    }


    private boolean isMergeAble(BaseTmTu tu, BaseTmTu p_tu)
    {
        SegmentTmTu oriTu = null;
        SegmentTmTu newTu = null;

        if (tu instanceof SegmentTmTu)
        {
            oriTu = (SegmentTmTu) tu;
        }

        if (p_tu instanceof SegmentTmTu)
        {
            newTu = (SegmentTmTu) p_tu;
        }

        if (newTu == null || oriTu == null)
        {
            return false;
        }

        Collection<ProjectTmTuTProp> oriProps = oriTu.getProps();
        // merge tu if one of them has non tu attribute
        if (oriProps == null || oriProps.size() == 0)
        {
            return true;
        }

        Collection<ProjectTmTuTProp> newProps = newTu.getProps();
        // merge tu if one of them has non tu attribute
        if (newProps == null || newProps.size() == 0)
        {
            return true;
        }

        for (ProjectTmTuTProp tuProp : oriProps)
        {
            String name = tuProp.getAttributeName();
            String value = tuProp.getPropValue();
            for (ProjectTmTuTProp newTuProp : newProps)
            {
                String newName = newTuProp.getAttributeName();
                String newvalue = newTuProp.getPropValue();

                // return false if tu attributes are not same
                if (name.equals(newName) && !value.equals(newvalue))
                {
                    return false;
                }
            }
        }

        return true;
    }


    private BaseTmTu mergeTu(BaseTmTu p_tu, BaseTmTu tu)
    {
        // merge tu attribute
        SegmentTmTu oriTu = null;
        SegmentTmTu newTu = null;
        if (tu instanceof SegmentTmTu && p_tu instanceof SegmentTmTu)
        {
            oriTu = (SegmentTmTu) tu;
            newTu = (SegmentTmTu) p_tu;
            Collection<ProjectTmTuTProp> oriProps = oriTu.getProps();
            Collection<ProjectTmTuTProp> newProps = newTu.getProps();
            if (newProps != null && newProps.size() > 0)
            {
                if (oriProps == null)
                {
                    oriProps = new ArrayList<ProjectTmTuTProp>();
                }
                oriProps.addAll(newProps);
                oriTu.setProps(oriProps);
            }
        }
        
        // the same source segment is found. Each target Tuvs must
        // be merged into the found Tu.
        for (GlobalSightLocale trgLocale : p_tu.getAllTuvLocales())
        {
            if (!trgLocale.equals(m_sourceLocale))
            {
                Collection<BaseTmTuv> tuvs = p_tu.getTuvList(trgLocale);
                if (tuvs != null)
                {
                    for (BaseTmTuv trgTuv : tuvs)
                    {
                        tu.addTuv(trgTuv);
                    }
                }
            }
        }
        
        return tu;
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
