/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
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

package com.globalsight.tools.reindexer;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Iterator;


/**
 * Record a reindex progress in reindex_progress table.
 */
public class ProgressRecorder
{
    private static final String CREATE_REINDEX_PROGRESS
        = "create table REINDEX_PROGRESS "
        + "(tu_id integer constraint reindex_progress_pk primary key, "
        + " migrated integer)";

    private static final String CREATE_INDEX
        = "create index reindex_progress_migrated on "
        + "REINDEX_PROGRESS(migrated)";

    private static final String TABLE_EXISTS
        = "select count(*) from user_tables "
        + "where table_name = 'REINDEX_PROGRESS'";

    private static final String TRUNCATE_REINDEX_PROGRESS
        = "truncate table REINDEX_PROGRESS";
    
    private static final String INIT_REINDEX_PROGRESS
        = "insert into REINDEX_PROGRESS select id, 0 from PROJECT_TM_TU_T";
    
    private static final String SET_MIGRATED
        = "update REINDEX_PROGRESS set migrated = 1 where tu_id = ?";
    
    private static final String ALL_TU_COUNT
        = "select count(*) from REINDEX_PROGRESS";
    
    private static final String NON_INDEXED_TU_COUNT
        = "select count(*) from REINDEX_PROGRESS where migrated = 0";
    
    private static final String ANALYZE_REINDEX_PROGRESS
        = "analyze table REINDEX_PROGRESS estimate statistics sample 10 percent";

    private static final String DROP_INDEX_TABLE
        = "drop table segment_tm_index_t";

    private static final String TRUNCATE_TOKEN_TABLE
        = "truncate table segment_tm_token_t";

    private static final String INDEX_TABLE_EXISTS
        = "select count(*) from user_tables "
        + "where table_name = 'SEGMENT_TM_INDEX_T'";

    
    private Connection m_connection;

    public ProgressRecorder(Connection p_connection)
    {
        m_connection = p_connection;
    }


    public void setupRecordTable(boolean p_initRecordTable)
        throws Exception
    {
        if(!recordTableExists())
        {
            createRecordTable();
            initRecordTable();
            truncateTokenTable();
        }
        else if(p_initRecordTable)
        {
            truncateRecordTable();
            initRecordTable();
            truncateTokenTable();
        }
    }
    
    private boolean recordTableExists()
        throws Exception
    {
        boolean result = false;
        
        Statement st = null;
        ResultSet rs = null;
        try
        {
            st = m_connection.createStatement();
            rs = st.executeQuery(TABLE_EXISTS);
            if(rs.next())
            {
                long exists = rs.getLong(1);
                if(exists > 0)
                {
                    result = true;
                }
            }
        }
        finally
        {
            if(st != null)
                st.close();
        }

        return result;
    }
    
        
    private void createRecordTable()
        throws Exception
    {
        Statement st = null;
        
        try
        {
            st = m_connection.createStatement();
            st.executeUpdate(CREATE_REINDEX_PROGRESS);
            st.executeUpdate(CREATE_INDEX);
        }
        finally
        {
            if(st != null)
                st.close();
        }
    }
    

    private void truncateRecordTable()
        throws Exception
    {
        Statement st = null;
        
        try
        {
            st = m_connection.createStatement();
            st.executeUpdate(TRUNCATE_REINDEX_PROGRESS);
        }
        finally
        {
            if(st != null)
                st.close();
        }
    }
    

    private void truncateTokenTable()
        throws Exception
    {
        // drop index table if exists.
        dropIndexTable();
        
        Statement st = null;
        
        try
        {
            st = m_connection.createStatement();
            st.executeUpdate(TRUNCATE_TOKEN_TABLE);
        }
        finally
        {
            if(st != null)
                st.close();
        }

        // analyze token table
        AnalyzeIndexTable analyzeIndexTable
            = new AnalyzeIndexTable(m_connection);
        analyzeIndexTable.analyze();
    }
    

    private void dropIndexTable()
        throws Exception
    {
        Statement st = null;
        ResultSet rs = null;
        try
        {
            st = m_connection.createStatement();
            rs = st.executeQuery(INDEX_TABLE_EXISTS);
            if(rs.next())
            {
                long exists = rs.getLong(1);
                if(exists > 0)
                {
                    st.executeUpdate(DROP_INDEX_TABLE);
                }
            }
        }
        finally
        {
            if(st != null)
                st.close();
        }
    }
    

    private void initRecordTable()
        throws Exception
    {
        Statement st = null;
        
        try
        {
            st = m_connection.createStatement();
            st.executeUpdate(INIT_REINDEX_PROGRESS);
            m_connection.commit();

            st.executeUpdate(ANALYZE_REINDEX_PROGRESS);
            m_connection.commit();
        }
        finally
        {
            if(st != null)
                st.close();
        }
    }

    
    static private final int BATCH_LIMIT = 1000;
    
    public void setTusMigrated(List p_tuIds)
        throws Exception
    {
        PreparedStatement st = null;
        
        try
        {
            st = m_connection.prepareStatement(SET_MIGRATED);

            int batchNum = 0;
            Iterator it = p_tuIds.iterator();
            while(it.hasNext())
            {
                batchNum++;
                
                Long tuId = (Long)it.next();
                
                st.setLong(1, tuId.longValue());
                st.addBatch();

                if(batchNum > 1000)
                {
                    st.executeBatch();
                    batchNum = 0;
                }
            }

            if(batchNum > 0)
            {
                st.executeBatch();
            }
            
            m_connection.commit();
        }
        finally
        {
            if(st != null)
                st.close();
        }
    }


    public long getAllTuCount()
        throws Exception
    {
        long count = 0;
        
        Statement st = null;
        ResultSet rs = null;
        try
        {
            st = m_connection.createStatement();
            rs = st.executeQuery(ALL_TU_COUNT);
            if(rs.next())
            {
                count = rs.getLong(1);
            }
        }
        finally
        {
            if(st != null)
                st.close();
        }

        return count;
    }
    

    public long getNonIndexedTuCount()
        throws Exception
    {
        long count = 0;
        
        Statement st = null;
        ResultSet rs = null;
        try
        {
            st = m_connection.createStatement();
            rs = st.executeQuery(NON_INDEXED_TU_COUNT);
            if(rs.next())
            {
                count = rs.getLong(1);
            }
        }
        finally
        {
            if(st != null)
                st.close();
        }

        return count;
    }
    

    
}

