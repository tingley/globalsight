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

/**
 * Database Profile Class Definition
 */
public interface DatabaseProfile
{
    /**
     * Return the id of database profile.
     *
     * <p>Note: The id is set by TopLink persistence.
     *
     * @return id as a long
     */
    public long getId();

    /**
     * Return the name of the database profile
     *
     * @return database profile name
     */
    public String getName();

    /**
     * Sets the name of this database profile
     *
     * @param p_name Preview URL name
     */
    public void setName(String p_name);

    /**
     * Return the description of the database profile
     *
     * @return database profile description
     */
    public String getDescription();

    /**
     * Sets the description of this database profile
     *
     * @param p_description Database profile description
     */
    public void setDescription(String p_description);

    /**
     * Return the check-out SQL statement
     *
     * @return check-out SQL statement
     */
    public String getCheckOutSql();

    /**
     * Set check-out SQL statement
     *
     * @param p_sql Check-out SQL statement
     */
    public void setCheckOutSql(String p_sql);

    /**
     * Return the check-out connection profile id
     *
     * @return connection profile id
     */
    public long getCheckOutConnectionProfileId();

    /**
     * Set check-out connection profile id
     *
     * @param p_id connection profile id
     */
    public void setCheckOutConnectionProfileId(long p_id);

    /**
     * Return the preview insert SQL statement
     *
     * @return  preview insert SQL statement
     */
    public String getPreviewInsertSql();

    /**
     * Set preview insert SQL statement
     *
     * @param p_sql Preview insert SQL statement
     */
    public void setPreviewInsertSql(String p_sql);

    /**
     * Return the preview update SQL statement
     *
     * @return  preview update SQL statement
     */
    public String getPreviewUpdateSql();

    /**
     * Set preview update SQL statement
     *
     * @param p_sql Preview update SQL statement
     */
    public void setPreviewUpdateSql(String p_sql);

    /**
     * Return preview connection profile id
     *
     * @return  preview connection profile id
     */
    public long getPreviewConnectionProfileId();

    /**
     * Set preview connection profile id
     *
     * @param p_id  connection profile id
     */
    public void setPreviewConnectionProfileId(long p_id);

    /**
     * Return the check-in insert SQL statement
     *
     * @return  check-in insert SQL statement
     */
    public String getCheckInInsertSql();

    /**
     * Set check-in insert SQL statement
     *
     * @param p_sql Check-in insert SQL statement
     */
    public void setCheckInInsertSql(String p_sql);

    /**
     * Return the check-in update SQL statement
     *
     * @return  check-in update SQL statement
     */
    public String getCheckInUpdateSql();

    /**
     * Set check-in update SQL statement
     *
     * @param p_sql Check-in update SQL statement
     */
    public void setCheckInUpdateSql(String p_sql);

    /**
     * Return check-in connection profile id
     *
     * @return  check-in connection profile id
     */
    public long getCheckInConnectionProfileId();

    /**
     * Set check-in connection profile id
     *
     * @param p_id  check-in connection profile id
     */
    public void setCheckInConnectionProfileId(long p_id);

    /**
     * Return Preview URL id
     *
     * @return  Preview URL id
     */
    public long getPreviewUrlId();

    /**
     * Set preview URL id
     *
     * @param p_id  Preview URL id
     */
    public void setPreviewUrlId(long p_id);

    /**
     * Return Localization profile Id attached to this data base profile
     *
     * @return  Localization profile Id
     */
    public long getL10nProfileId();

    /**
     * Set Localization profile Id attached to this data base profile
     *
     * @param p_id  Localization profile Id
     */
    public void setL10nProfileId(long p_id);

    /**
     * Return language code-set for this data base profile
     *
     * @return  Language code-set
     */
    public String getCodeSet();

    /**
     * Set language code-set for this data base profile
     *
     * @param p_codeSet  Language code-set
     */
    public void setCodeSet(String p_codeSet);
}
