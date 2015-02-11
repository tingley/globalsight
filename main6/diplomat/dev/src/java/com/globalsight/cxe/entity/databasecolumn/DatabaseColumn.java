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
package com.globalsight.cxe.entity.databasecolumn;
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
 * Database column Class Definition
 */
public interface DatabaseColumn
{
    /**
     * Return the id of database column.
     *
     * <p>Note: The id is set by TopLink persistence.
     *
     * @return id as a long
     */
    public long getId();

    /**
     * Return the column number
     *
     * @return column number
     */
    public long getColumnNumber();

    /**
     * Set column number
     *
     * @param p_colNo column number
     */
    public void setColumnNumber(long p_colNo);

    /**
     * Return the column name
     *
     * @return column name
     */
    public String getColumnName();

    /**
     * Set column name
     *
     * @param p_colName column name
     */
    public void setColumnName(String p_colName);

    /**
     * Return the column label used for display
     *
     * @return column label
     */
    public String getLabel();

    /**
     * Set the column's display label
     *
     * @param p_label column label
     */
    public void setLabel(String p_label);

    /**
     * Return the table name of column
     *
     * @return column's table name
     */
    public String getTableName();

    /**
     * Set table name of column
     *
     * @param p_tableName table name
     */
    public void setTableName(String p_tableName);

    /**
     * Return the database profile id
     *
     * @return database profile id
     */
    public long getDatabaseProfileId();

    /**
     * Set database profile id
     *
     * @param p_id database profile id
     */
    public void setDatabaseProfileId(long p_id);

    /**
     * Return XML rule id
     *
     * @return  XML rule id
     */
    public long getXmlRuleId();

    /**
     * Set XML rule id
     *
     * @param p_id  XML rule id
     */
    public void setXmlRuleId(long p_id);

    /**
     * Return known format type
     *
     * @return  known format type
     */
    public long getFormatType();

    /**
     * Set format type
     *
     * @param p_formatType  known format type
     */
    public void setFormatType(long p_formatType);

    /**
     * Return column content mode
     *
     * @return  column content mode
     */
    public int getContentMode();

    /**
     * Set column content mode
     *
     * @param p_mode  column content mode
     */
    public void setContentMode(int p_mode);
}
