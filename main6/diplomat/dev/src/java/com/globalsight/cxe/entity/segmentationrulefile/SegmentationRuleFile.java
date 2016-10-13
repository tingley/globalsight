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

/** Represents a CXE Segmentation Rule File entity object. */
public interface SegmentationRuleFile
{
    /**
     ** Return the id of the Segmentation Rule File (cannot be set)
     ** 
     * @return id as a long
     **/
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
     ** Return the name of the segmentation Rule File
     ** 
     * @return segmentation Rule File name
     **/
    public String getName();

    /**
     * Get name of the company this Segmentation rule file belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId();

    /**
     ** Return the description of the segmentation Rule File
     ** 
     * @return segmentation Rule File description
     **/
    public String getDescription();

    /**
     ** Return the body of the segmentation Rule File
     ** 
     * @return segmentation Rule File
     **/
    public String getRuleText();

    /**
     ** Gets the type of the segmentation Rule File
     **/
    public int getType();

    /**
     ** Sets the name of the segmentation Rule File
     **/
    public void setName(String p_name);

    /**
     * Set name of the company this Segmentation rule file belong to.
     * 
     */
    public void setCompanyId(long p_companyId);

    /**
     ** Sets the description of the segmentation Rule File
     **/
    public void setDescription(String p_ruleText);

    /**
     ** Sets the body of the segmentation Rule File
     **/
    public void setRuleText(String p_ruleText);

    /**
     ** Sets the type of the segmentation Rule File
     **/
    public void setType(int p_ruleText);

    /**
     * Set this segmentation Rule file not to display
     */
    public void inActive();

    
    public void setIsDefault(boolean isDefault);
    
    public boolean getIsDefault();
}
