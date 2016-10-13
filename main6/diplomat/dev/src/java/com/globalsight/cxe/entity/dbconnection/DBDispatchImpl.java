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
import com.globalsight.everest.persistence.PersistentObject;

/** Represents a CXE DB Dispatch entity object.
*/
public class DBDispatchImpl
    extends PersistentObject implements DBDispatch
{
    private static final long serialVersionUID = -6024480322834724528L;
    public boolean useActive = false;
    
    //CONSTRUCTORS
    /** Default constructor for TOPLink*/
    public DBDispatchImpl()
    {
        m_name = null;
        m_description = null;
        m_table_name = null;
        m_connection_id = 0;
        m_records_per_page = 0;
        m_pages_per_batch = 0;
        m_max_elapsed_milli = 0;
    }

    /** Constructs an DBDispatchImpl from a DBDispatch (no deep copy)
     **	@param o Another DBDispatch object **/
    public DBDispatchImpl(DBDispatch o)
    {
        m_name = o.getName();
        m_description = o.getDescription();
        m_table_name = o.getTableName();
        m_connection_id = o.getConnectionId();
        m_records_per_page = o.getRecordsPerPage();
        m_pages_per_batch = o.getPagesPerBatch();
        m_max_elapsed_milli = o.getMaxElapsedMillis();
    }

    //PUBLIC METHODS

    /**
     * Return the name of the db dispatch
     * @return db dispatch name
     */
    public String getName()
    {return m_name;}

    /**
     * Return the description of the db dispatch
     * @return db dispatch description
     */
    public String getDescription()
    {return m_description;}

    /**
     * Return the table_name of the db dispatch
     * @return db dispatch table_name
     */
    public String getTableName()
    {return m_table_name;}

    /**
     * Return the connection id of the db dispatch
     * @return db dispatch connection id
     */
    public long getConnectionId()
    {return m_connection_id;}

    /**
     * Return the records per page of the db dispatch
     * @return db dispatch records per page
     */
    public long getRecordsPerPage()
    {return m_records_per_page;}

    /**
     * Return the pages per batch of the db dispatch
     * @return db dispatch pages per batch
     */
    public long getPagesPerBatch()
    {return m_pages_per_batch;}

    /**
     * Return the max elapsed milliseconds of the db dispatch
     * @return db dispatch max elapsed milliseconds
     */
    public long getMaxElapsedMillis()
    {return m_max_elapsed_milli;}

    /**
     * Set the name of the db dispatch
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the description of the db dispatch
     * @param p_description The description of the db dispatch
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the table_name of the db dispatch
     * @param p_table_name The table_name of the db dispatch
     */
    public void setTableName(String p_table_name)
    {
        m_table_name = p_table_name;
    }

    /**
     * Set the dispatch connection id of the db dispatch
     * @param p_connection_id The dispatch connection id of the db dispatch
     */
    public void setConnectionId(long p_connection_id)
    {
        m_connection_id = p_connection_id;
    }

    /**
     * Set the records per page of the db dispatch
     * @param p_records_per_page The records per page of the db dispatch
     */
    public void setRecordsPerPage(long p_records_per_page)
    {
        m_records_per_page = p_records_per_page;
    }

    /**
     * Set the pages per batch of the db dispatch
     * @param p_pages_per_batch The pages per batch of the db dispatch
     */
    public void setPagesPerBatch(long p_pages_per_batch)
    {
        m_pages_per_batch = p_pages_per_batch;
    }

    /**
     * Set the max elapsed milliseconds of the db dispatch
     * @param p_max_elapsed_milli The max elapsed milliseconds of the db dispatch
     */
    public void setMaxElapsedMillis(long p_max_elapsed_milli)
    {
        m_max_elapsed_milli = p_max_elapsed_milli;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    //PRIVATE MEMBERS
    private String m_name;
    private String m_description;
    private String m_table_name;
    private long m_connection_id;
    private long m_records_per_page;
    private long m_pages_per_batch;
    private long m_max_elapsed_milli;

    public String getTable_name()
    {
        return m_table_name;
    }

    public void setTable_name(String m_table_name)
    {
        this.m_table_name = m_table_name;
    }

    public long getRecords_per_page()
    {
        return m_records_per_page;
    }

    public void setRecords_per_page(long m_records_per_page)
    {
        this.m_records_per_page = m_records_per_page;
    }

    public long getPages_per_batch()
    {
        return m_pages_per_batch;
    }

    public void setPages_per_batch(long m_pages_per_batch)
    {
        this.m_pages_per_batch = m_pages_per_batch;
    }

    public long getMax_elapsed_milli()
    {
        return m_max_elapsed_milli;
    }

    public void setMax_elapsed_milli(long m_max_elapsed_milli)
    {
        this.m_max_elapsed_milli = m_max_elapsed_milli;
    }

    public long getConnection_id()
    {
        return m_connection_id;
    }

    public void setConnection_id(long m_connection_id)
    {
        this.m_connection_id = m_connection_id;
    }
}

