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
package com.globalsight.everest.permission;

import com.globalsight.everest.persistence.PersistentObject;

/**
 * TopLink mapped object that implements a PermissionGroup
 */
public class PermissionGroupImpl extends PersistentObject implements
        PermissionGroup
{
    private static final long serialVersionUID = -2806261345378016420L;

    // PRIVATE MEMBERS
    private String m_description;
    private PermissionSet m_permissionSet;
    private long m_companyId;

    /** Default constructor */
    public PermissionGroupImpl()
    {
        super();
        m_description = null;
        m_permissionSet = null;
        m_companyId = -1;
    }

    /**
     * Constructs a PermisisonGroupImpl
     * 
     * @param p_name
     * @param p_description
     * @param p_permissionSetString
     */
    public PermissionGroupImpl(String p_name, String p_description,
            String p_permissionSetString, String p_companyId)
    {
        super();
        setName(p_name);
        m_description = p_description;
        setPermissionSetAsString(p_permissionSetString);
        m_companyId = Long.parseLong(p_companyId);
    }

    /**
     ** Return the description of the PermissionGroup
     ** 
     * @return String
     **/
    public String getDescription()
    {
        return m_description;
    }

    /**
     ** Return the PermissionSet as a String
     ** 
     * @return
     **/
    public String getPermissionSetAsString()
    {
        return m_permissionSet.toString();
    }

    public long getCompanyId()
    {
        return m_companyId;
    }

    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     ** Sets the Permission set from a String representation
     ** 
     * @param p_permissionSet
     *            -- string represenation
     ** @see PermissionSet
     **/
    public void setPermissionSetAsString(String p_permissionSetString)
    {
        m_permissionSet = new PermissionSet(p_permissionSetString);
    }

    /** Gets the PermissionSet for this PermissionGroup */
    public PermissionSet getPermissionSet()
    {
        return m_permissionSet;
    }

    /**
     * Sets the PermissionSet for this PermissionGroup The string should be of
     * the format "|1|2|3|" where 1,2,and 3 are permissions that the use has.
     */
    public void setPermissionSet(String p_permissionSetString)
    {
        setPermissionSetAsString(p_permissionSetString);
    }

    /**
     * Returns a string representation for debugging. It contains the id,
     * name,and permission set as a string
     * 
     * @return String
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[").append(getIdAsLong()).append(",");
        sb.append(getName()).append(",");
        sb.append(getPermissionSetAsString());
        sb.append("]");
        return sb.toString();
    }
}
