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
package com.globalsight.config;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * System Parameter Class Implementation
 */
public class SystemParameterImpl
    extends PersistentObject
    implements SystemParameter
{
    private static final long serialVersionUID = -2856030441603929227L;

    public static final String M_NAME = "m_name";
    public static final String M_VALUE = "m_value";
    public static final String M_COMPANYID = "m_companyId";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private String  m_name;
    private String  m_value;
    private long m_companyId;

    /**
     *  Default constructor
     */
    public SystemParameterImpl()
    {
        this(null, null, -1);
    }


    /**
     * Constructor that supplies all attributes for system parameter object.
     *
     * @param p_name system parameter name
     * @param p_value system parameter value
     */
    public SystemParameterImpl(String p_name, String p_value)
    {
        super();

        m_name = p_name;
        m_value = p_value;
    }

    /**
     * Constructor that supplies all attributes for system parameter object.
     *
     * @param p_name system parameter name
     * @param p_value system parameter value
     */
    public SystemParameterImpl(String p_name, String p_value, long p_companyId)
    {
        super();

        m_name = p_name;
        m_value = p_value;
        m_companyId = p_companyId;
    }
    
    /**
     * Return the system parameter key name
     *
     * @return system parameter key name
     */
    public String getName()
    {
        return m_name;
    }


    /**
     * Return the system parameter value
     *
     * @return system parameter value
     */
    public String getValue()
    {
        return m_value;
    }


    /**
     * Set the system parameter value
     *
     * @param p_value system parameter value
     */
    public void setValue(String p_value)
    {
        m_value = p_value;
    }

    /**
     * Convert int to String and set system parameter value
     *
     * @param p_intValue int system parameter value
     */
    public void setValue(int p_intValue)
    {
        m_value = Integer.toString(p_intValue);
        
    }


    /**
     * Convert boolean to String and set system parameter value -
     * true is set as "1" and false is set as "0"
     *
     * @param p_booleanValue int system parameter value
     */
    public void setValue(boolean p_booleanValue)
    {
        if (p_booleanValue)
        {
            m_value = "1";
        }
        else
        {
            m_value = "0";
        }
    }


    /**
     * Return string representation of object
     *
     * @return string representation of object
     */
    public String toString()
    {
        return super.toString() +
            " m_name="  + (m_name == null ? "null" : m_name) +
            " m_value=" + (m_value == null ? "null" : m_value)+
            " m_companyId=" +  m_companyId;
    }
    public boolean equals(Object o){
        if(o instanceof SystemParameter){
            SystemParameter sp=(SystemParameter)o;
            return (this.getId()==sp.getId());
        }
        return false;
    }


    
    /**
     * @return Returns the companyId.
     */
    public long getCompanyId() {
        return m_companyId;
    }
    /**
     * @param companyId The companyId to set.
     */
    public void setCompanyId(long companyId) {
        this.m_companyId = companyId;
    }
    
    public void setCurrentCompanyId(){
        String companyId = CompanyThreadLocal.getInstance().getValue();
        this.setCompanyId(Long.parseLong(companyId));
    }

    public void setName(String m_name)
    {
        this.m_name = m_name;
    }
}
