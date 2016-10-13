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
package com.globalsight.cxe.entity.segmentationrulefile;

import com.globalsight.everest.persistence.PersistentObject;

/** Represents a CXE Segmentation Rule File implements entity object. */
public class SegmentationRuleFileImpl extends PersistentObject implements
        SegmentationRuleFile
{
    private static final long serialVersionUID = 8406618565097318001L;

    private String m_name;
    private String m_description;
    private String m_ruleText;
    private long m_companyId;
    private int m_type;
    private boolean isDefault;

    public boolean useActive = true;

    /**
     * default constructor
     */
    public SegmentationRuleFileImpl()
    {
        m_name = null;
        m_description = null;
        m_ruleText = null;
        m_type = 0;
        m_companyId = -1;
    }

    /**
     * Construct from name ...
     * 
     * @param p_name
     * @param p_description
     * @param p_ruleText
     * @param p_type
     *            type
     */
    public SegmentationRuleFileImpl(String p_name, String p_description,
            String p_ruleText, int p_type)
    {
        m_name = p_name;
        m_description = p_description;
        m_ruleText = p_ruleText;
        m_type = p_type;
        m_companyId = -1;
    }

    /**
     * Construct from another SegmentationRuleFileImpl
     * 
     * @param p_object
     */
    public SegmentationRuleFileImpl(SegmentationRuleFileImpl p_object)
    {
        this(p_object.getName(), p_object.getDescription(), p_object
                .getRuleText(), p_object.getType());
    }

    /**
     ** Return the name of the segmentation Rule File
     ** 
     * @return segmentation Rule File name
     **/
    public String getName()
    {
        return m_name;
    }

    /**
     * Get name of the company this Segmentation rule file belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return m_companyId;
    }

    /**
     ** Return the description of the segmentation Rule File
     ** 
     * @return segmentation Rule File description
     **/
    public String getDescription()
    {
        return m_description;
    }

    /**
     ** Return the body of the segmentation Rule File
     ** 
     * @return segmentation Rule File
     **/
    public String getRuleText()
    {
        return m_ruleText;
    }

    /**
     ** Gets the body of the segmentation Rule File
     **/
    public int getType()
    {
        return m_type;
    }

    /**
     * Gets id
     */
    public long getId()
    {
        return super.getId();
    }

    /**
     ** Sets the name of the segmentation Rule File
     **/
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set name of the company this Segmentation rule file belong to.
     * 
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     ** Sets the description of the segmentation Rule File
     **/
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     ** Sets the body of the segmentation Rule File
     **/
    public void setRuleText(String p_ruleText)
    {
        m_ruleText = p_ruleText;
    }

    /**
     ** Sets the type of the segmentation Rule File
     **/
    public void setType(int p_type)
    {
        m_type = p_type;
    }

    /**
     * Set this segmentation Rule file not to display
     */
    public void inActive()
    {
        isActive(false);
    }

    public String toString()
    {
        return m_name;
    }

    public boolean getIsDefault()
    {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }
}
