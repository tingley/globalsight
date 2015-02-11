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
package com.globalsight.cxe.entity.dbconnection;
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

/** Represents a CXE DB Connection entity object.
*/
public interface DBConnection
{
    /**
    ** Return the id of the DBConnection (cannot be set)
    ** @return id as a long
    **/
    public long getId();

    /**
     * Return the name of the db connection
     * @return db connection name
     */
    public String getName();

    /**
     * Return the description of the db connection
     * @return db connection description
     */
    public String getDescription();

    /**
     * Return the driver of the db connection
     * @return db connection driver
     */
    public String getDriver();

    /**
     * Return the connection string of the db connection
     * @return db connection string
     */
    public String getConnection();

    /**
     * Return the user name of the db connection
     * @return db connection user name
     */
    public String getUserName();

    /**
     * Return the password of the db connection
     * @return db connection password
     */
    public String getPassword();

    /**
     * Set the name of the db connection
     */
    public void setName(String p_name);

    /**
     * Set the description of the db connection
     * @param p_description The description of the db connection
     */
    public void setDescription(String p_description);

    /**
     * Set the driver of the db connection
     * @param p_driver The driver of the db connection
     */
    public void setDriver(String p_driver);

    /**
     * Set the connection string of the db connection
     * @param p_connection The connection string of the db connection
     */
    public void setConnection(String p_connection);

    /**
     * Set the user name of the db connection
     * @param p_username The user name of the db connection
     */
    public void setUserName(String p_username);

    /**
     * Set the password of the db connection
     * @param p_password The password of the db connection
     */
    public void setPassword(String p_password);

}

