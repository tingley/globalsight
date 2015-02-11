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
package com.globalsight.cxe.entity.databaseprofile;
/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

import com.globalsight.everest.persistence.PersistentObject;

/**
 * Database Profile Class Implementation
 */
public class DatabaseProfileImpl extends PersistentObject
    implements DatabaseProfile
{
    private static final long serialVersionUID = 6310672294313983248L;

    /* used for TOPLink queries against the profile id attribute */
	static final public String L10NPROFILE_ID = "m_l10nProfileId";
	 
    private String  m_name;
    private String  m_description;
    private Long    m_previewUrlId;
    private Long    m_checkOutConnectProfileId;
    private String  m_checkOutSql;
    private Long    m_checkInConnectProfileId;
    private String  m_checkInInsertSql;
    private String  m_checkInUpdateSql;
    private Long    m_previewConnectProfileId;
    private String  m_previewInsertSql;
    private String  m_previewUpdateSql;
    private Long    m_l10nProfileId;
    private String  m_codeSet;

    /**
     *  Default constructor used by TopLink only
     */
    public DatabaseProfileImpl ()
    {
        super();

        m_name = null;
        m_description = null;
        m_previewUrlId = null;
        m_checkOutConnectProfileId = null;
        m_checkOutSql = null;
        m_checkInConnectProfileId = null;
        m_checkInInsertSql = null;
        m_checkInUpdateSql = null;
        m_previewConnectProfileId = null;
        m_previewInsertSql = null;
        m_previewUpdateSql = null;
        m_l10nProfileId = null;
        m_codeSet = null;
    }

    /**
     * Constructor that supplies all attributes for database profile object.
     *
     * @param p_name Name of database profile
     * @param p_description Description of DB profile
     * @param p_checkOutConnectProfileId Check-out connection profile Id
     * @param p_checkOutSql Check-out SQL statement
     * @param p_checkInConnectProfileId Check-in connection profile Id
     * @param p_checkInInsertSql Check-in insert SQL statement
     * @param p_checkInUpdateSql Check-in update SQL statement
     * @param p_previewConnectProfileId Preview connection profile Id
     * @param p_previewInsertSql Preview insert SQL statement
     * @param p_previewUpdateSql Check-in update SQL statement
     * @param p_previewUrlId Preview URL id
     * @param p_l10nProfileId Localization profile id
     * @param p_codeSet Language code-set
     */
    public DatabaseProfileImpl(String   p_name,
                               String   p_description,
                               long     p_checkOutConnectProfileId,
                               String   p_checkOutSql,
                               long     p_checkInConnectProfileId,
                               String   p_checkInInsertSql,
                               String   p_checkInUpdateSql,
                               long     p_previewConnectProfileId,
                               String   p_previewInsertSql,
                               String   p_previewUpdateSql,
                               long     p_previewUrlId,
                               long     p_l10nProfileId,
                               String   p_codeSet)
    {
        super();

        m_name = p_name;
        m_description = p_description;
        setPreviewUrlId(p_previewUrlId);
        setCheckOutConnectionProfileId(p_checkOutConnectProfileId);
        m_checkOutSql = p_checkOutSql;
        setCheckInConnectionProfileId(p_checkInConnectProfileId);
        m_checkInInsertSql = p_checkInInsertSql;
        m_checkInUpdateSql = p_checkInUpdateSql;
        setPreviewConnectionProfileId(p_previewConnectProfileId);
        m_previewInsertSql = p_previewInsertSql;
        m_previewUpdateSql = p_previewUpdateSql;
        setL10nProfileId(p_l10nProfileId);
        m_codeSet = p_codeSet;
    }


    /**
     * Constructs an DatabaseProfileImpl object from a
     * DatabaseProfile object
     *
     * @param p_dbProfile Another DatabaseProfile object
     **/
    public DatabaseProfileImpl(DatabaseProfile p_dbProfile)
    {
        super();

        m_name = p_dbProfile.getName();
        m_description = p_dbProfile.getDescription();
        setPreviewUrlId(p_dbProfile.getPreviewUrlId());
        setCheckOutConnectionProfileId(p_dbProfile.getCheckOutConnectionProfileId());
        m_checkOutSql = p_dbProfile.getCheckOutSql();
        setCheckInConnectionProfileId(p_dbProfile.getCheckInConnectionProfileId());
        m_checkInInsertSql = p_dbProfile.getCheckInInsertSql();
        m_checkInUpdateSql = p_dbProfile.getCheckInUpdateSql();
        setPreviewConnectionProfileId(p_dbProfile.getPreviewConnectionProfileId());
        m_previewInsertSql = p_dbProfile.getPreviewInsertSql();
        m_previewUpdateSql = p_dbProfile.getPreviewUpdateSql();
        setL10nProfileId(p_dbProfile.getL10nProfileId());
        m_codeSet = p_dbProfile.getCodeSet();
    }

    /**
     * Return Preview URL id
     *
     * @return  Preview URL id
     */
    public long getPreviewUrlId ()
    {
        if (m_previewUrlId == null)
            return 0;
        else
            return m_previewUrlId.longValue();
    }

    /**
     * Set check-in insert SQL statement
     *
     * @param p_sql Check-in insert SQL statement
     */
    public void setCheckInInsertSql (String p_sql)
    {
        m_checkInInsertSql = p_sql;
    }

    /**
     * Return the description of the database profile
     *
     * @return database profile description
     */
    public String getDescription ()
    {
        return m_description;
    }

    /**
     * Set preview insert SQL statement
     *
     * @param p_sql Preview insert SQL statement
     */
    public void setPreviewInsertSql (String p_sql)
    {
        m_previewInsertSql = p_sql;
    }

    /**
     * Sets the name of this database profile
     *
     * @param p_name Preview URL name
     */
    public void setName (String p_name)
    {
        m_name = p_name;
    }

    /**
     * Return the check-out connection profile id
     *
     * @return connection profile id
     */
    public long getCheckOutConnectionProfileId ()
    {
        if (m_checkOutConnectProfileId == null)
            return 0;
        else
            return m_checkOutConnectProfileId.longValue();
    }

    /**
     * Return the check-out SQL statement
     *
     * @return check-out SQL statement
     */
    public String getCheckOutSql ()
    {
        return m_checkOutSql;
    }

    /**
     * Set check-in connection profile id
     *
     * @param p_id  check-in connection profile id
     */
    public void setCheckInConnectionProfileId (long p_id)
    {
        if (p_id == 0)
            m_checkInConnectProfileId = null;
        else
            m_checkInConnectProfileId = new Long(p_id);
    }

    /**
     * Return the check-in update SQL statement
     *
     * @return  check-in update SQL statement
     */
    public String getCheckInUpdateSql ()
    {
        return m_checkInUpdateSql;
    }

    /**
     * Set preview URL id
     *
     * @param p_id  Preview URL id
     */
    public void setPreviewUrlId (long p_id)
    {
        if (p_id == 0)
            m_previewUrlId = null;
        else
            m_previewUrlId = new Long(p_id);
    }

    /**
     * Set preview connection profile id
     *
     * @param p_id  connection profile id
     */
    public void setPreviewConnectionProfileId (long p_id)
    {
        if (p_id == 0)
            m_previewConnectProfileId = null;
        else
            m_previewConnectProfileId = new Long(p_id);
    }

    /**
     * Return check-in connection profile id
     *
     * @return  check-in connection profile id
     */
    public long getCheckInConnectionProfileId ()
    {
        if (m_checkInConnectProfileId == null)
            return 0;
        else
            return m_checkInConnectProfileId.longValue();
    }

    /**
     * Return the check-in insert SQL statement
     *
     * @return  check-in insert SQL statement
     */
    public String getCheckInInsertSql ()
    {
        return m_checkInInsertSql;
    }

    /**
     * Return the name of the database profile
     *
     * @return database profile name
     */
    public String getName ()
    {
        return m_name;
    }

    /**
     * Return the preview update SQL statement
     *
     * @return  preview update SQL statement
     */
    public String getPreviewUpdateSql ()
    {
        return m_previewUpdateSql;
    }

    /**
     * Sets the description of this database profile
     *
     * @param p_description Database profile description
     */
    public void setDescription (String p_description)
    {
        m_description = p_description;
    }

    /**
     * Return the preview insert SQL statement
     *
     * @return  preview insert SQL statement
     */
    public String getPreviewInsertSql ()
    {
        return m_previewInsertSql;
    }

    /**
     * Set check-out connection profile id
     *
     * @param p_id connection profile id
     */
    public void setCheckOutConnectionProfileId (long p_id)
    {
        if (p_id == 0)
            m_checkOutConnectProfileId = null;
        else
            m_checkOutConnectProfileId = new Long(p_id);
    }

    /**
     * Set check-out SQL statement
     *
     * @param p_sql Check-out SQL statement
     */
    public void setCheckOutSql (String p_sql)
    {
        m_checkOutSql = p_sql;
    }

    /**
     * Return preview connection profile id
     *
     * @return  preview connection profile id
     */
    public long getPreviewConnectionProfileId ()
    {
        if (m_previewConnectProfileId == null)
            return 0;
        else
            return m_previewConnectProfileId.longValue();
    }

    /**
     * Set check-in update SQL statement
     *
     * @param p_sql Check-in update SQL statement
     */
    public void setCheckInUpdateSql (String p_sql)
    {
        m_checkInUpdateSql = p_sql;
    }

    /**
     * Set preview update SQL statement
     *
     * @param p_sql Preview update SQL statement
     */
    public void setPreviewUpdateSql (String p_sql)
    {
        m_previewUpdateSql = p_sql;
    }

    /**
     * Return Localization profile Id attached to this data base profile
     *
     * @return  Localization profile Id
     */
    public long getL10nProfileId()
    {
        if (m_l10nProfileId == null)
            return 0;
        else
            return m_l10nProfileId.longValue();
    }

    /**
     * Set Localization profile Id attached to this data base profile
     *
     * @param p_id  Localization profile Id
     */
    public void setL10nProfileId(long p_id)
    {
        if (p_id == 0)
            m_l10nProfileId = null;
        else
            m_l10nProfileId = new Long(p_id);
    }

    /**
     * Return language code-set for this data base profile
     *
     * @return  Language code-set
     */
    public String getCodeSet()
    {
        return m_codeSet;
    }

    /**
     * Set language code-set for this data base profile
     *
     * @param p_codeSet  Language code-set
     */
    public void setCodeSet(String p_codeSet)
    {
        m_codeSet = p_codeSet;
    }

    /**
     * Return string representation of object
     *
     * @return string representation of object
     */
    public String toString()
    {
        return m_name;
    }

    /**
     * Get the string representation of the object for debugging purposes.
     * @return The string representation of object.
     */
    public String toDebugString()
    {
        return super.toString() +
            "   name is: "+m_name;
    }

    public Long getCheckInConnectProfileId()
    {
        return m_checkInConnectProfileId;
    }

    public void setCheckInConnectProfileId(Long inConnectProfileId)
    {
        m_checkInConnectProfileId = inConnectProfileId;
    }

    public Long getCheckOutConnectProfileId()
    {
        return m_checkOutConnectProfileId;
    }

    public void setCheckOutConnectProfileId(Long outConnectProfileId)
    {
        m_checkOutConnectProfileId = outConnectProfileId;
    }

    public Long getPreviewConnectProfileId()
    {
        return m_previewConnectProfileId;
    }

    public void setPreviewConnectProfileId(Long connectProfileId)
    {
        m_previewConnectProfileId = connectProfileId;
    }
}
