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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.globalsight.everest.projecthandler.ProjectTmTuTProp;
import com.globalsight.util.GlobalSightLocale;

/**
 * AbstractTmTu is an abstract class that represents a super class of various
 * Translation Unit representations. Translation Unit usually has one source
 * Translation Unit Variant and one or more target Translation Unit Variants.
 * Translation Unit Variant has text in a given locale. The term Translation
 * Unit and Translation Unit Variant are taken from TMX
 * (http://www.lisa.org/tmx) specification.
 */

public abstract class AbstractTmTu implements BaseTmTu, Cloneable
{
    private long m_id; // id
    private long m_tmId; // tm id
    private String m_format; // in lowercase, html, xml, plaintext, etc
    private String m_type; // type of the segment. (text, url-a, etc)
    private boolean m_translatable; // translatable or localizable
    private String m_sourceContent;
    private String m_sourceTmName;

    // Tuvs that belong to this Tu
    // key: GlobalSightLocale object
    // Value: a Set of Tuvs (multiple translations have more
    // than one Tuvs for a locale and Set guarantees that all
    // Tuvs in the Set are unique
    private Map m_tuvs;
    private Collection<ProjectTmTuTProp> m_props;

    /**
     * Default constructor. It can be accessed only from its subclasses.
     */
    protected AbstractTmTu()
    {
        m_id = 0;
        m_tmId = 0;
        m_format = null;
        m_type = null;
        m_translatable = true;
        m_tuvs = new HashMap();
        m_props = new ArrayList<ProjectTmTuTProp>();
    }

    /**
     * Constructor. It can be accessed only from its subclasses.
     * 
     * @param p_id
     *            id
     * @param p_tmId
     *            tm id
     * @param p_format
     *            format name
     * @param p_type
     *            type name
     * @param p_translatable
     *            set this Tu translatable if this param is true
     */
    protected AbstractTmTu(long p_id, long p_tmId, String p_format,
            String p_type, boolean p_translatable)
    {
        m_id = p_id;
        m_tmId = p_tmId;
        m_format = p_format;
        m_type = p_type;
        m_translatable = p_translatable;
        m_tuvs = new HashMap();
        m_props = new ArrayList<ProjectTmTuTProp>();
    }

    public long getId()
    {
        return m_id;
    }

    public void setId(long p_id)
    {
        m_id = p_id;
    }

    public long getTmId()
    {
        return m_tmId;
    }

    public void setTmId(long p_tmId)
    {
        m_tmId = p_tmId;
    }

    /**
     * returns its format
     * 
     * @return name of the format
     */
    public String getFormat()
    {
        return m_format;
    }

    /**
     * returns its type
     * 
     * @return name of the type
     */
    public String getType()
    {
        return m_type;
    }

    /**
     * test if it's translatable
     * 
     * @return true if translatable, false if localizable
     */
    public boolean isTranslatable()
    {
        return m_translatable;
    }

    /**
     * sets its format
     * 
     * @param p_format
     *            name of the format
     */
    public void setFormat(String p_format)
    {
        m_format = p_format.toLowerCase();
    }

    /**
     * sets its type
     * 
     * @param p_type
     *            name of the type
     */
    public void setType(String p_type)
    {
        m_type = p_type;
    }

    /**
     * sets the TU as translatable
     */
    public void setTranslatable()
    {
        m_translatable = true;
    }

    /**
     * sets the TU as localizable
     */
    public void setLocalizable()
    {
        m_translatable = false;
    }

    public void setSourceTmName(String p_sourceTmName)
    {
        m_sourceTmName = p_sourceTmName;
    }

    public String getSourceTmName()
    {
        return m_sourceTmName;
    }

    /**
     * adds a Tuv to this Tu
     * 
     * @param p_tuv
     *            Tuv to add
     */
    public void addTuv(BaseTmTuv p_tuv)
    {
        p_tuv.setTu(this);

        Set tuvs = (Set) m_tuvs.get(p_tuv.getLocale());
        if (tuvs == null)
        {
            tuvs = new HashSet();
            m_tuvs.put(p_tuv.getLocale(), tuvs);
        }
        tuvs.add(p_tuv);
    }

    /**
     * removes a Tuv from this Tu
     * 
     * @param p_tuv
     *            Tuv to be removed
     */
    public void removeTuv(BaseTmTuv p_tuv)
    {
        Set tuvs = (Set) m_tuvs.get(p_tuv.getLocale());
        if (tuvs != null)
        {
            tuvs.remove(p_tuv);
            if (tuvs.size() == 0)
            {
                m_tuvs.remove(p_tuv.getLocale());
            }
        }
    }
    
    public void removeTuvsForLocale(GlobalSightLocale locale)
    {
        if (m_tuvs != null && locale != null && m_tuvs.containsKey(locale))
        {
            m_tuvs.remove(locale);
        }
    }

    public void addProp(ProjectTmTuTProp p_p)
    {
        boolean added = false;
        for (ProjectTmTuTProp p : m_props)
        {
            if (p_p.getPropType().equals(p.getPropType()))
            {
                p.setPropValue(p_p.getPropValue());
                added = true;
            }
        }

        if (!added)
            m_props.add(p_p);
    }

    public void removeProp(String p_pType)
    {
        ProjectTmTuTProp delObj = null;
        for (ProjectTmTuTProp p : m_props)
        {
            if (p_pType.equals(p.getPropType()))
            {
                delObj = p;
            }
        }
        
        if (delObj != null)
            m_props.remove(delObj);
    }
    
    public Collection<ProjectTmTuTProp> getProps()
    {
        return m_props;
    }
    
    public void setProps(Collection<ProjectTmTuTProp> props)
    {
        m_props = props;
    }

    /**
     * Get a first Tuv in a Tuv array in a given locale.
     * 
     * @param p_locale
     *            GlobalSightLocale
     * @return BaseTmTuv. If a Tuv in a given locale cannot be found, null is
     *         returned.
     */
    public BaseTmTuv getFirstTuv(GlobalSightLocale p_locale)
    {
        BaseTmTuv tuv = null;

        Set tuvList = (Set) m_tuvs.get(p_locale);
        if (tuvList != null)
        {
        	TreeSet tmp = new TreeSet(new Comparator() {

				@Override
				public int compare(Object o1, Object o2) {
					BaseTmTuv tuv1 = (BaseTmTuv)o1;
					BaseTmTuv tuv2 = (BaseTmTuv)o2;
					
					return tuv1.getModifyDate().compareTo(tuv2.getModifyDate());
				}
			});
        	tmp.addAll(tuvList);
        	tuv = (BaseTmTuv)tmp.last();
        	/**
            Iterator it = tuvList.iterator();
            if (it.hasNext())
            {
                tuv = (BaseTmTuv) it.next();
            }
            */
        }
        return tuv;
    }

    /**
     * Get a Collection of Tuv in a given locale.
     * 
     * @param p_locale
     *            GlobalSightLocale
     * @return Collection object that contains one or more BaseTmTuv objects. If
     *         a Collection in a given locale cannot be found, null is returned.
     */
    public Collection getTuvList(GlobalSightLocale p_locale)
    {

        return (Collection) m_tuvs.get(p_locale);
    }

    /**
     * Get all locales of Tuvs that are added to this Tu.
     * 
     * @return a Set of GlobalSightLocale.
     */
    public Set getAllTuvLocales()
    {
        return m_tuvs.keySet();
    }

    /**
     * Get a number of Tuvs this Tu has.
     * 
     * @return Number of Tuvs
     */
    public int getTuvSize()
    {
        int size = 0;

        Iterator it = m_tuvs.values().iterator();
        while (it.hasNext())
        {
            size += ((Collection) it.next()).size();
        }

        return size;
    }

    public Object clone()
    {
        AbstractTmTu tu = null;
        try
        {
            tu = (AbstractTmTu) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new RuntimeException(e.getMessage());
        }

        tu.m_tuvs = new HashMap();
        return tu;
    }

    public String toDebugString(boolean p_includeTuvs)
    {
        StringBuffer result = new StringBuffer();

        result.append("TU ");
        result.append(isTranslatable() ? 'T' : 'L');
        result.append(" id=");
        result.append(getId());
        result.append(" format=");
        result.append(getFormat());
        result.append(" type=");
        result.append(getType());
        result.append(" #loc=");
        result.append(getAllTuvLocales().size());
        result.append(" #tuv=");
        result.append(getTuvSize());
        result.append(getExtraDebugInfo());
        result.append("\n");

        if (p_includeTuvs)
        {
            Collection locales = getAllTuvLocales();

            for (Iterator it = locales.iterator(); it.hasNext();)
            {
                GlobalSightLocale locale = (GlobalSightLocale) it.next();

                Collection tuvs = getTuvList(locale);

                for (Iterator it2 = tuvs.iterator(); it2.hasNext();)
                {
                    BaseTmTuv tuv = (BaseTmTuv) it2.next();

                    result.append(tuv.toDebugString());
                }
            }
        }

        return result.toString();
    }

    // For subclasses
    protected String getExtraDebugInfo()
    {
        return ""; 
    }
    
    public List<BaseTmTuv> getTuvs()
    {
        ArrayList<BaseTmTuv> allTuvs = new ArrayList<BaseTmTuv>();
        Set<GlobalSightLocale> keys = getAllTuvLocales();
        for (GlobalSightLocale key : keys)
        {
            allTuvs.addAll(getTuvList(key));
        }

        return allTuvs;
    }

    public String getSourceContent()
    {
        return m_sourceContent;
    }

    public void setSourceContent(String sourceContent)
    {
        this.m_sourceContent = sourceContent;
    }
    
    
}
