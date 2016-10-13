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
 
package com.globalsight.everest.vendormanagement;



/**
 * This class represents a Vendor - an individual who performs work.  
 * They are not a company but rather an individual associated with one company.  
 * They perform localization tasks (translate, review, edit) for a fee.
 */
public class VendorInfo 
{

    // private data members
    private long m_id = -1;
    private String m_companyName = null;
    private String m_customVendorId = null; 
    private String m_firstName = null;
    private String m_lastName = null;
    private String m_pseudonym = null;
    private String m_status = null;
    private String m_userId = null;
    
    /**
     * Constructor - Set some of the Vendor attributes
     */
    public VendorInfo(long p_id, String p_vendorId, 
                      String p_pseudonym, String p_firstName, 
                      String p_lastName, String p_companyName, 
                      String p_userId, String p_status)
    {
        m_id = p_id;
        m_customVendorId = p_vendorId;
        m_pseudonym = p_pseudonym;
        m_firstName = p_firstName;
        m_lastName = p_lastName;
        m_companyName = p_companyName;
        m_userId = p_userId;
        m_status = p_status;
    }

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Helper Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Get the company name of the vendor.
     */
    public String getCompanyName()
    {
        return m_companyName;
    }

    /**
     * Get the unique identifier for the vendor that the customer can
     * defined.  If they don't define one then it is generated automatically.
     */
    public String getCustomVendorId()
    {
        return m_customVendorId;
    }

    /**
     * Get the first name of the vendor.
     */
    public String getFirstName()
    {
        return m_firstName;
    }

    /**
     * Get the full name of the vendor (First name+ Last name)
     */
    public String getFullName()
    {
        StringBuffer sb = new StringBuffer();

        if (m_firstName != null)
        {
            sb.append(m_firstName);
            sb.append(" ");
        }

        if (m_lastName != null)
        {
            sb.append(m_lastName);
        }

        return sb.toString();
    }

    /**
     * Get the vendor id.
     */
    public long getId()
    {
        return m_id;
    }

    /**
     * Get the vendor's last name.
     */
    public String getLastName()
    {
        return m_lastName;
    }

    /**
     * Get the pseudonym/alias of the vendor.
     */
    public String getPseudonym()
    {
        return m_pseudonym;
    }
    
    /**
     * Gets the status of the vendor (APPROVED, PENDING, ON-HOLD, REJECTED
     */
    public String getStatus()
    {
        return m_status;
    }
    
    /**
     * Get the vendor's user id (if GlobalSight user).
     */
    public String getUserId()
    {
        return m_userId;
    }

    /**
     * This method is used only for debugging.
     */
    public String toDebugString()
    {

        StringBuffer dString = new StringBuffer();
        dString.append("m_id=");
        dString.append(m_id);
        dString.append("m_customVendorId=");
        dString.append(m_customVendorId);
        dString.append(", m_companyName=");
        dString.append(m_companyName);
        dString.append(", m_firstName=");
        dString.append(m_firstName);
        dString.append(", m_lastName=");
        dString.append(m_lastName);
        dString.append(", m_pseudonym=");
        dString.append(m_pseudonym);
        dString.append(", m_userId=");
        dString.append(m_userId);
        dString.append("\n");
        return dString.toString();
    }
 }
