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

import com.globalsight.everest.persistence.PersistentObject;

/** Implements an XmlRuleFile */
public class XmlRuleFileImpl extends PersistentObject implements XmlRuleFile
{
    private static final long serialVersionUID = -873521866679410418L;
    private long m_companyId;

    /** Default constructor for TOPLink */
    public XmlRuleFileImpl()
    {
        m_name = null;
        m_description = null;
        m_ruleText = null;
    }

    /** Constructs an XmlRuleFileImpl with id, name, description, and ruleText **/
    public XmlRuleFileImpl(String p_name, String p_description,
            String p_ruleText)
    {
        m_name = p_name;
        m_description = p_description;
        m_ruleText = p_ruleText;
    }

    /** Constructs an XmlRuleFileImpl from an XmlRuleFile **/
    public XmlRuleFileImpl(XmlRuleFile o)
    {
        this(o.getName(), o.getDescription(), o.getRuleText());
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

    /**
     ** Return the name of the XML Rule File
     ** 
     * @return XML Rule File name
     **/
    public String getName()
    {
        return m_name;
    }

    /**
     ** Return the description of the XML Rule File
     ** 
     * @return XML Rule File description
     **/
    public String getDescription()
    {
        return m_description;
    }

    /**
     ** Return the body of the XML Rule File
     ** 
     * @return XML Rule File
     **/
    public String getRuleText()
    {
        return m_ruleText;
    }

    /**
     ** Sets the name of the XML Rule File
     **/
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     ** Sets the description of the XML Rule File
     **/
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     ** Sets the body of the XML Rule File
     **/
    public void setRuleText(String p_ruleText)
    {
        m_ruleText = p_ruleText;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    // PRIVATE MEMBERS
    private String m_name;
    private String m_description;
    private String m_ruleText;
}
