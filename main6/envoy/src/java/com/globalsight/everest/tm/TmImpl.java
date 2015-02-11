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

package com.globalsight.everest.tm;

import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.SegmentTmInfo;

import com.globalsight.util.edit.EditUtil;

import java.io.Serializable;

import java.util.Date;

/**
 * @deprecated This code is no longer supported.
 */
public final class TmImpl
    implements Tm, Serializable
{
    //
    // Private Members
    //
    private long m_id;
    private Long m_idAsLong;

    private String m_name = "";
    private String m_domain = "";
    private String m_organization = "";
    private String m_description = "";
    private String m_creationUser = "";
    private Date m_creationDate = null;
    // id of the company which this activity belong to
    private String m_companyId;    

    //
    // Constructors
    //
    public TmImpl()
    {
        m_creationDate = new Date();
    }

    public TmImpl(String p_name)
    {
        m_name = p_name;
        m_creationDate = new Date();
    }

    //
    // Tm interface methods
    //
    public long getId()
    {
        return m_id;
    }

    public void setId(long p_id)
    {
        m_id = p_id;
        m_idAsLong = null;
    }
    
    /**
     * Get name of the company this activity belong to.
     * @return The company name. 
     */
    public String getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * @return The company name. 
     */
    public void setCompanyId(String p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    public Long getIdAsLong()
    {
        if (m_idAsLong == null)
        {
            m_idAsLong = new Long(m_id);
        }

        return m_idAsLong;
    }

    /**
     * Gets the name of the Tm.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Sets the name of the Tm.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Gets the domain attribute of the Tm.
     */
    public String getDomain()
    {
        return m_domain;
    }

    /**
     * Sets the domain attribute of the Tm.
     */
    public void setDomain(String p_domain)
    {
        m_domain = p_domain;
    }

    /**
     * Gets the organization attribute of the Tm.
     */
    public String getOrganization()
    {
        return m_organization;
    }

    /**
     * Sets the organization attribute of the Tm.
     */
    public void setOrganization(String p_organization)
    {
        m_organization = p_organization;
    }

    /**
     * Gets the description of the Tm.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Sets the description of the Tm.
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Gets the creation user of the Tm.
     */
    public String getCreationUser()
    {
        return m_creationUser;
    }

    /**
     * Gets the creation date of the Tm.
     */
    public Date getCreationDate()
    {
        return m_creationDate;
    }

    //
    // Other public methods
    //

    /**
     * Returns a string representation of the object.
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Returns a string representation of the object for debugging
     * purposes.
     */
    public String toDebugString()
    {
        StringBuffer result = new StringBuffer();

        result.append("[TM ");
        result.append("id=");
        result.append(getId());
        result.append("name=");
        result.append(m_name);
        result.append(" domain=");
        result.append(m_domain);
        result.append(" organization=");
        result.append(m_organization);
        result.append(" description=");
        result.append(m_description);
        result.append(" creationDate=");
        result.append(m_creationDate);
        result.append("]");

        return result.toString();
    }

    @Override
    public SegmentTmInfo getSegmentTmInfo() {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }
    
    @Override
    public Long getTm3Id() {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }
    
    @Override
    public void setTm3Id(Long id) {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public boolean getIsRemoteTm() {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public int getConvertRate()
    {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public void setConvertRate(int convertRate)
    {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public long getLastTUId()
    {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public void setLastTUId(long lastTUId)
    {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public long getConvertedTM3Id()
    {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }

    @Override
    public void setConvertedTM3Id(long convertedTm3Id)
    {
        throw new UnsupportedOperationException("This implementation is no longer supported");
    }
}
