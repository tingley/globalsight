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
package com.globalsight.everest.foundation;

// Core Java classes
import java.io.Serializable;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.GlobalSightLocale;

/**
 * This is source/target locales pair
 * 
 */
public class LocalePair extends PersistentObject implements Serializable
{
    private static final long serialVersionUID = 6845165035619564192L;

    static public String SOURCE = "m_source";
    static public String TARGET = "m_target";
    /*
     * Member variables
     */
    private GlobalSightLocale m_source;
    private GlobalSightLocale m_target;
    private long m_companyId;

    public boolean useActive = true;

    /**
     * Default constructor to be used by TopLink only. This is here solely
     * because the persistence mechanism that persists instances of this class
     * is using TopLink, and TopLink requires a public default constructor for
     * all the classes that it handles persistence for.
     */
    public LocalePair()
    {
    }

    /**
     * Constructor
     * 
     * @param p_source
     *            The source locale
     * @param p_target
     *            The target locale
     * @param p_companyId
     *            The company id.
     */
    public LocalePair(GlobalSightLocale p_source, GlobalSightLocale p_target,
            long p_companyId)
    {
        m_source = p_source;
        m_target = p_target;
        m_companyId = p_companyId;
    }

    /*
     * Methods
     */
    /**
     * Get the source locale
     * 
     * @return The source locale
     */
    public GlobalSightLocale getSource()
    {
        return m_source;
    }

    /**
     * Get the id of the company this locale pair belongs to.
     * 
     * @return The company id.
     */
    public long getCompanyId()
    {
        return m_companyId;
    }

    /**
     * Set the id of the company this locale pair belongs to.
     */
    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    /**
     * Get the target locale
     * 
     * @return The target locale
     */
    public GlobalSightLocale getTarget()
    {
        return m_target;
    }

    /**
     * Convert to displayable string
     * 
     * return The display string
     */
    public String toString()
    {
        return m_source.toString() + " -> " + m_target.toString();
    }

    public String getUIName()
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getSource().getLanguageCode());
        sb.append(" ");
        sb.append(getSource().getCountryCode());
        sb.append(" ");
        sb.append(getTarget().getLanguageCode());
        sb.append(" ");
        sb.append(getTarget().getCountryCode());
        return sb.toString();
    }

    /**
     * Returns 'true' if the ids of the LocalePair objects are equal, 'false' if
     * they aren't.
     * 
     * @param p_locale
     *            The LocalePair object to compare with
     * @return 'true' or 'false'
     */
    public boolean equals(LocalePair p_localePair)
    {
        return (getSource().equals(p_localePair.getSource()) && getTarget()
                .equals(p_localePair.getTarget()));
    }

    public void setSource(GlobalSightLocale m_source)
    {
        this.m_source = m_source;
    }

    public void setTarget(GlobalSightLocale m_target)
    {
        this.m_target = m_target;
    }

}
