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
package com.globalsight.everest.projecthandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.globalsight.cxe.entity.customAttribute.TMAttribute;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.tm.Tm;
import com.globalsight.ling.tm2.SegmentTmInfo;
import com.globalsight.ling.tm2.segmenttm.Tm2SegmentTmInfo;
import com.globalsight.ling.tm3.integration.segmenttm.Tm3SegmentTmInfo;

public class ProjectTM extends PersistentObject implements Tm
{
    private static final long serialVersionUID = 6667987349894911360L;

    private String m_name = "";
    // id of the company which this activity belong to
    private long m_companyId;
    private String m_organization = "";
    private String m_description = "";
    private Date m_creationDate;
    private String m_domain = "";
    private String m_creationUser = "";
    private boolean m_isRemoteTm = false;
    private long m_gsEditionId = -1;
    private long m_remoteTmProfileId = -1;
    private String m_remoteTmProfileName = null;
    private Long m_tm3Id;
    private int m_convertRate = 0;
    private long m_lastTUId = -1;
    private long m_convertedTM3Id = -1;
    private String m_status = "";
    private boolean indexTarget = false;

    private Set<TMAttribute> attributes;

    public String getStatus()
    {
        return m_status;
    }

    public void setStatus(String m_status)
    {
        this.m_status = m_status;
    }

    public ProjectTM()
    {
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    public void setOrganization(String p_organization)
    {
        m_organization = p_organization;
    }

    public void setDomain(String p_domain)
    {
        m_domain = p_domain;
    }

    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    public void setCreationDate(Date p_creationDate)
    {
        m_creationDate = p_creationDate;
    }

    public void setCreationUser(String p_creationUser)
    {
        m_creationUser = p_creationUser;
    }

    public String getName()
    {
        return m_name == null ? "" : m_name;
    }

    public String getDescription()
    {
        return m_description == null ? "" : m_description;
    }

    public String getCreationUser()
    {
        return m_creationUser == null ? "" : m_creationUser;
    }

    public String getOrganization()
    {
        return m_organization == null ? "" : m_organization;
    }

    public Date getCreationDate()
    {
        return m_creationDate;
    }

    public String getDomain()
    {
        return m_domain == null ? "" : m_domain;
    }

    public void setIsRemoteTm(boolean p_isRemoteTm)
    {
        this.m_isRemoteTm = p_isRemoteTm;
    }

    @Override
    public boolean getIsRemoteTm()
    {
        return this.m_isRemoteTm;
    }

    public void setGsEditionId(long p_gsEditionId)
    {
        this.m_gsEditionId = p_gsEditionId;
    }

    public long getGsEditionId()
    {
        return this.m_gsEditionId;
    }

    public void setRemoteTmProfileId(long p_remoteTmProfileId)
    {
        this.m_remoteTmProfileId = p_remoteTmProfileId;
    }

    public long getRemoteTmProfileId()
    {
        return this.m_remoteTmProfileId;
    }

    public void setRemoteTmProfileName(String p_remoteTmProfileName)
    {
        this.m_remoteTmProfileName = p_remoteTmProfileName;
    }

    public String getRemoteTmProfileName()
    {
        return this.m_remoteTmProfileName;
    }

    @Override
    public SegmentTmInfo getSegmentTmInfo()
    {
        if (getTm3Id() != null)
        {
            return new Tm3SegmentTmInfo();
        }
        return new Tm2SegmentTmInfo();
    }

    @Override
    public Long getTm3Id()
    {
        return m_tm3Id;
    }

    public void setTm3Id(Long tm3Id)
    {
        this.m_tm3Id = tm3Id;
    }

    public List<TMAttribute> getAllTMAttributes()
    {
        List<TMAttribute> atts = new ArrayList<TMAttribute>();
        Set<TMAttribute> tmAtts = getAttributes();
        if (tmAtts != null)
        {
            atts.addAll(tmAtts);
        }

        return atts;
    }

    public List<String> getAllTMAttributenames()
    {
        List<String> atts = new ArrayList<String>();
        Set<TMAttribute> tmAtts = getAttributes();
        if (tmAtts != null)
        {
            Iterator<TMAttribute> it = tmAtts.iterator();
            while (it.hasNext())
            {
                TMAttribute tma = it.next();
                atts.add(tma.getAttributename());
            }
        }

        return atts;
    }

    public Set<TMAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Set<TMAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public int getConvertRate()
    {
        return m_convertRate;
    }

    public void setConvertRate(int m_convertRate)
    {
        this.m_convertRate = m_convertRate;
    }

    public long getLastTUId()
    {
        return m_lastTUId;
    }

    public void setLastTUId(long m_lastTUId)
    {
        this.m_lastTUId = m_lastTUId;
    }

    public long getConvertedTM3Id()
    {
        return m_convertedTM3Id;
    }

    public void setConvertedTM3Id(long m_convertedTM3Id)
    {
        this.m_convertedTM3Id = m_convertedTM3Id;
    }

    public boolean isIndexTarget()
    {
        return indexTarget;
    }

    public void setIndexTarget(boolean p_indexTarget)
    {
        this.indexTarget = p_indexTarget;
    }
}
