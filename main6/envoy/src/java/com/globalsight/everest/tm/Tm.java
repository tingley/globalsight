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
 *  test
 */

package com.globalsight.everest.tm;

import java.util.Date;

import com.globalsight.ling.tm2.SegmentTmInfo;

/**
 * Translation Memory (TM) is a collection of translation units (TU). Tm holds
 * translations of text segments for re-use in new translations.
 */
public interface Tm
{
    /**
     * Get Tm unique identifier.
     * 
     * @return Tm unique identifier.
     */
    public long getId();

    /**
     * Return the persistent object's id as a Long object.
     * 
     * This is a convenience method that simply wraps the id as an object, so
     * that, for example, the idAsLong can be used as a Hashtable key.
     * 
     * @return the unique identifier as a Long object.
     */
    public Long getIdAsLong();

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId();

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId);

    /**
     * Get the name of the Tm.
     * 
     * @return the name of the Tm.
     */
    public String getName();

    /**
     * Set the name of the Tm.
     * 
     * @param p_name
     *            the name of the Tm.
     */
    public void setName(String p_name);

    /**
     * Get the domain attribute of the Tm.
     * 
     * @return the domain attribute of the Tm.
     */
    public String getDomain();

    /**
     * Set the domain attribute of the Tm.
     * 
     * @param p_domain
     *            the domain attribute of the Tm.
     */
    public void setDomain(String p_domain);

    /**
     * Get the organization attribute of the Tm.
     * 
     * @return the organization attribute of the Tm.
     */
    public String getOrganization();

    /**
     * Set the organization attribute of the Tm.
     * 
     * @param p_organization
     *            the organization attribute of the Tm.
     */
    public void setOrganization(String p_organization);

    /**
     * Get the description of the Tm.
     * 
     * @return the description of the Tm.
     */
    public String getDescription();

    /**
     * Set the description of the Tm.
     * 
     * @param p_description
     *            the description of the Tm.
     */
    public void setDescription(String p_description);

    /**
     * Get the creation user of the Tm.
     * 
     * @return the creation user of the Tm.
     */
    public String getCreationUser();

    /**
     * Get the creation date of the Tm.
     * 
     * @return the creation date of the Tm.
     */
    public Date getCreationDate();

    /**
     * Get the segment TM implementation for this TM.
     * 
     * @return segment TM implementation
     */
    public SegmentTmInfo getSegmentTmInfo();

    /**
     * Get the ID of the tm3 TM backing this logical TM. If this is a legacy
     * (tm2) TM, this value will be null.
     */
    public Long getTm3Id();

    /**
     * Set the ID of the tm3 TM backing this logical TM.
     * 
     * @param id
     */
    public void setTm3Id(Long id);

    /**
     * Check whether this is are remote TM.
     * 
     * @return true if this is a remote TM
     */
    public boolean getIsRemoteTm();

    public int getConvertRate();

    public void setConvertRate(int convertRate);

    public long getLastTUId();

    public void setLastTUId(long lastTUId);

    public long getConvertedTM3Id();

    public void setConvertedTM3Id(long convertedTm3Id);

    public boolean isIndexTarget();

    public void setIndexTarget(boolean p_indexTarget);

}
