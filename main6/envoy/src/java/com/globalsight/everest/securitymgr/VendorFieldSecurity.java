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

package com.globalsight.everest.securitymgr;


// globalsight
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.VendorSecureFields;

// java
import java.util.Hashtable;


/**
 * Class that specifies the field securities that belong to a particular
 * vendor.  The fields in the FieldSecurity pertain to the vendor.
 */
public class VendorFieldSecurity
    extends FieldSecurity
    implements VendorSecureFields
{
    // the if of the vendor these field securities apply to.
    private long m_vendorId;

    /**
     * Default constructor.
     * Initializes all the fields too in case of an upgrade and the
     * security hasn't been set yet.
     */ 
    public VendorFieldSecurity()
        throws SecurityException
    {
        super();
        initFields();
    }


    //============================ package methods=============================


    void setVendorId(long p_vendorId)
    {
        m_vendorId = p_vendorId;
    }


    //=============================private methods=================================


    /**
     * Set all the fields to shared (so it continues to work like past releases.
     * However hide the security page.  This should only be open to particular
     * users within the same project.
     */
    private void initFields()
    {
        m_fs.put(ADDRESS, SHARED);
        m_fs.put(AMBASSADOR_ACCESS, SHARED);
        m_fs.put(CELL_PHONE, SHARED);
        m_fs.put(COMPANY, SHARED);
        m_fs.put(CITIZENSHIP, SHARED);
        m_fs.put(COUNTRY, SHARED);
        m_fs.put(CUSTOM_FIELDS, SHARED);
        m_fs.put(CUSTOM_ID, SHARED);
        m_fs.put(DOB, SHARED);
        m_fs.put(EMAIL, SHARED);
        m_fs.put(EMAIL_LANGUAGE, SHARED);
        m_fs.put(FAX, SHARED);
        m_fs.put(FIRST_NAME, SHARED);
        m_fs.put(HOME_PHONE, SHARED);
        m_fs.put(IS_INTERNAL, SHARED);
        m_fs.put(LAST_NAME, SHARED);
        m_fs.put(NOTES, SHARED);
        m_fs.put(PASSWORD, SHARED);
        m_fs.put(PROJECTS, SHARED);
        m_fs.put(RESUME, SHARED);
        m_fs.put(ROLES, SHARED);
        m_fs.put(SECURITY, HIDDEN);
        m_fs.put(STATUS, SHARED);
        m_fs.put(TITLE, SHARED);
        m_fs.put(USERNAME, SHARED);
        m_fs.put(WORK_PHONE, SHARED);
        setFieldSecurity(m_fs);
    }

    // For hibernate
	public long getVendorId()
	{
		return m_vendorId;
	}
}


