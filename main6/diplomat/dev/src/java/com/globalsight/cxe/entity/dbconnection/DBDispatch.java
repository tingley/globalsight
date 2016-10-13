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

/** Represents a CXE DB Dispatch entity object.
*/
public interface DBDispatch
{
    /**
    ** Return the id of the DBDispatch (cannot be set)
    ** @return id as a long
    **/
    public long getId();

    /**
     * Return the name of the db dispatch
     * @return db dispatch name
     */
    public String getName();

    /**
     * Return the description of the db dispatch
     * @return db dispatch description
     */
    public String getDescription();

    /**
     * Return the table_name of the db dispatch
     * @return db dispatch table_name
     */
    public String getTableName();

    /**
     * Return the connection id of the db dispatch
     * @return db dispatch connection id
     */
    public long getConnectionId();

    /**
     * Return the records per page of the db dispatch
     * @return db dispatch records per page
     */
    public long getRecordsPerPage();

    /**
     * Return the pages per batch of the db dispatch
     * @return db dispatch pages per batch
     */
    public long getPagesPerBatch();

    /**
     * Return the max elapsed milliseconds of the db dispatch
     * @return db dispatch max elapsed milliseconds
     */
    public long getMaxElapsedMillis();

    /**
     * Set the name of the db dispatch
     */
    public void setName(String p_name);

    /**
     * Set the description of the db dispatch
     * @param p_description The description of the db dispatch
     */
    public void setDescription(String p_description);

    /**
     * Set the table_name of the db dispatch
     * @param p_table_name The table_name of the db dispatch
     */
    public void setTableName(String p_table_name);

    /**
     * Set the dispatch connection id of the db dispatch
     * @param p_connection_id The dispatch connection id of the db dispatch
     */
    public void setConnectionId(long p_connection_id);

    /**
     * Set the records per page of the db dispatch
     * @param p_records_per_page The records per page of the db dispatch
     */
    public void setRecordsPerPage(long p_records_per_page);

    /**
     * Set the pages per batch of the db dispatch
     * @param p_pages_per_batch The pages per batch of the db dispatch
     */
    public void setPagesPerBatch(long p_pages_per_batch);

    /**
     * Set the max elapsed milliseconds of the db dispatch
     * @param p_max_elapsed_milli The max elapsed milliseconds of the db dispatch
     */
    public void setMaxElapsedMillis(long p_max_elapsed_milli);

}

