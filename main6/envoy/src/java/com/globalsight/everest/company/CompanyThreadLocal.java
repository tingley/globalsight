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
package com.globalsight.everest.company;

// GlobalSight
import com.globalsight.everest.persistence.PersistenceException;

/**
 * This class only represent a wrapper object for the company names defined in
 * Envoy database and is used for defining a workflow template (template node
 * names).
 * 
 */
public class CompanyThreadLocal
{
    // company description
    private static InheritableThreadLocal<String> m_companyThreadLocal = null;
    private static CompanyThreadLocal m_instance = new CompanyThreadLocal();

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Default Company constructor used ONLY for TopLink.
     */
    protected CompanyThreadLocal()
    {
        m_companyThreadLocal = new InheritableThreadLocal<String>();
    }

    public static CompanyThreadLocal getInstance()
    {
        return m_instance;
    }

    public void setIdValue(String companyId)
    {
        m_companyThreadLocal.set(companyId);
    }

    public void setIdValue(long companyId)
    {
        setIdValue(String.valueOf(companyId));
    }

    public void setValue(String companyName)
    {
        try
        {
            String companyId = CompanyWrapper.getCompanyIdByName(companyName);
            m_companyThreadLocal.set(companyId);
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }
    }

    public String getValue()
    {
        return (String) m_companyThreadLocal.get();
    }

    public boolean fromSuperCompany()
    {
        return CompanyWrapper.SUPER_COMPANY_ID.equals(m_instance.getValue());
    }
}
