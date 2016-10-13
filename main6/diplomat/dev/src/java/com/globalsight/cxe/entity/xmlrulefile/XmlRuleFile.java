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
package com.globalsight.cxe.entity.xmlrulefile;

/** Represents a CXE XML Rule File entity object. */
public interface XmlRuleFile
{
    /**
     ** Return the id of the XML Rule File (cannot be set)
     ** 
     * @return id as a long
     **/
    public long getId();

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
     ** Return the name of the XML Rule File
     ** 
     * @return XML Rule File name
     **/
    public String getName();

    /**
     ** Return the description of the XML Rule File
     ** 
     * @return XML Rule File description
     **/
    public String getDescription();

    /**
     ** Return the body of the XML Rule File
     ** 
     * @return XML Rule File
     **/
    public String getRuleText();

    /**
     ** Sets the name of the XML Rule File
     **/
    public void setName(String p_name);

    /**
     ** Sets the description of the XML Rule File
     **/
    public void setDescription(String p_ruleText);

    /**
     ** Sets the body of the XML Rule File
     **/
    public void setRuleText(String p_ruleText);
}
