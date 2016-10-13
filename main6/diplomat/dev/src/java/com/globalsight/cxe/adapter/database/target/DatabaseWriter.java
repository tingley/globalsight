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
package com.globalsight.cxe.adapter.database.target;

import com.globalsight.cxe.adapter.database.TaskXml;
import com.globalsight.cxe.adapter.database.InvalidTaskXmlException;

import com.globalsight.diplomat.util.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

import com.globalsight.diplomat.util.database.RecordProfile;
import com.globalsight.diplomat.util.database.RecordProfileDbAccessor;
import com.globalsight.diplomat.util.database.SqlParameterSubstituter;
import com.globalsight.diplomat.util.database.SqlParameterSubstitutionException;
import com.globalsight.diplomat.util.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.HashMap;
import java.util.Vector;

/**
 * DatabaseWriter merges the contents of a TaskXml object into one or more
 * SQL (insert or update) templates, to produce executable SQL statements.
 * Each statement is executed to put the appropriate data into the appropriate
 * database table.  If the operation fails, the DatabaseWriter throws an
 * exception to report the problem.
 */
public class DatabaseWriter
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String PREVIEW = "preview";

    //
    // PRIVATE MEMBER VARIABLES
    //
    private transient Vector m_proxies;
    private transient DbRecordProxyBuilder m_builder;
    private transient HashMap m_profiles;
    private transient Logger m_logger;

    //
    // PUBLIC CONSTRUCTORS
    //
    public DatabaseWriter()
    {
        super();
        m_proxies = null;
        m_builder = new DbRecordProxyBuilder();
        m_profiles = new HashMap();
        m_logger = Logger.getLogger();
    }

    //
    // PUBLIC METHODS
    //
    /**
     * Write the given task XML into the database, using the update/insert
     * sequel provided on the record profile identified by the task XML.
     *
     * @throws DatabaseWriteException, which wraps previously thrown exceptions,
     * if anything goes wrong.
     */
    public void write(TaskXml p_taskXml)
    throws DatabaseWriteException
    {
        try
        {
            buildProxiesFrom(p_taskXml);
        }
        catch (InvalidTaskXmlException e)
        {
            throw new DatabaseWriteException(e);
        }
        writeRecords();
        // TEMPORARY FIX: disable caching so that updated record-profiles
        // will be reloaded.
        m_profiles = new HashMap();
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Use the given task XML to update the proxy's variable */
    private void buildProxiesFrom(TaskXml p_taskXml)
    throws InvalidTaskXmlException
    {
        m_builder.setTaskXml(p_taskXml);
        m_proxies = m_builder.buildProxies();
    }

    /* Read the record profile with the given id from the database.*/
    private RecordProfile recordProfile(long p_id)
    throws DatabaseWriteException
    {
        Long id = new Long(p_id);
        RecordProfile rp = (RecordProfile)m_profiles.get(id);
        if (rp == null)
        {
            try
            {
                rp = RecordProfileDbAccessor.readRecordProfile(p_id);
                if (rp != null)
                {
                    m_profiles.put(id, rp);
                }
            }
            catch (Exception e)
            {
                throw new DatabaseWriteException("Unable to load record profile, id=" + p_id, e);
            }
        }
        return rp;
    }

    /* Write all the records corresponding to the current collection of */
    /* proxies */
    private void writeRecords()
    throws DatabaseWriteException
    {
        for (int i = 0 ; i < m_proxies.size() ; i++)
        {
            DbRecordProxy proxy = (DbRecordProxy)m_proxies.elementAt(i);
            m_logger.println(Logger.DEBUG_D, "DatabaseWriter:writeRecords(), proxy=" + proxy.detailString());
            RecordProfile profile = recordProfile(Long.parseLong(proxy.getRecordProfileId()));
            executeSql(sqlHolder(proxy, profile));
        }
    }

    /* Obtain a connection to the database, and execute the given sql. */
    /* First, we try the update SQL statements; if any failure occurs, we */
    /* assume that it's because there are no records to update, so we try */
    /* the insert statements next */
    private void executeSql(SqlHolder p_sqlHolder)
    throws DatabaseWriteException
    {
        Connection conn = null;
        try
        {
            conn = ConnectionPool.getConnection(p_sqlHolder.getConnectionId());
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            if (!executeUpdateSql(conn, p_sqlHolder))
            {
                conn.rollback();
                executeInsertSql(conn, p_sqlHolder);
            }
            conn.commit();
            conn.setAutoCommit(autoCommit);
            ConnectionPool.returnConnection(conn);
        }
        catch (DatabaseWriteException dwe)
        {
           throw dwe; //no need to re-wrap
        }
        catch (Exception e)
        {
           //account for anything else bad happening
           throw new DatabaseWriteException("Unable to execute put-back SQL", e);
        }
    }

    /* try to execute 1 or more update statements */
    private boolean executeUpdateSql(Connection p_conn, SqlHolder p_sqlHolder)
    throws DatabaseWriteException
    {
        return execute(p_conn, p_sqlHolder.parsedUpdateSql(), "Update");
    }

    /* try to execute 1 or more insert statements */
    private boolean executeInsertSql(Connection p_conn, SqlHolder p_sqlHolder)
    throws DatabaseWriteException
    {
        return execute(p_conn, p_sqlHolder.parsedInsertSql(), "Insert");
    }

    /* Execute the given sql statements on the given connection. */
    /* Execution can fail either by affecting no records or by throwing an */
    /* exception. In the first case, we allow the caller to decide whether */
    /* to rollback or commit.  In the second case, we force a rollback and */
    /* percolate the exception back to the caller. */
    private boolean execute(Connection p_conn, String[] p_sql, String p_type)
    throws DatabaseWriteException
    {
        boolean executeSucceeded = true;
        String currentSqlStatement = null;
        try
        {
            Statement st = p_conn.createStatement();
            for (int i = 0 ; i < p_sql.length ; i++)
            {
               currentSqlStatement = p_sql[i];
               m_logger.println(Logger.DEBUG_B, p_type + " SQL=" + currentSqlStatement);
               int count = st.executeUpdate(currentSqlStatement);
               executeSucceeded = (executeSucceeded && (count > 0));
            }
            st.close();
        }
        catch (SQLException e)
        {
           try {p_conn.rollback();}
           catch (SQLException sqle) {
              m_logger.printStackTrace(Logger.ERROR,"Unable to rollback",sqle);
           }
           
           throw new DatabaseWriteException("Unable to execute put-back "
                                            + p_type + " SQL", e, currentSqlStatement);
        }
        
        return executeSucceeded;
    }

    /* Obtain a holder containing the pertinent sql for this proxy. */
    private SqlHolder sqlHolder(DbRecordProxy p_proxy, RecordProfile p_profile)
    throws DatabaseWriteException
    {
       String trimmedUpdateSql;
       String trimmedInsertSql;
        String updateSql;
        String insertSql;
        long connectionId;
        HashMap params = p_proxy.parameterMap();

        if (p_proxy.getSqlType().equals(PREVIEW))
        {
           trimmedUpdateSql = p_profile.getPreviewUpdateSql().trim();
           trimmedInsertSql = p_profile.getPreviewInsertSql().trim();

           if (trimmedUpdateSql.equals("") || trimmedInsertSql.equals(""))
           {
              throw new DatabaseWriteException("SQL cannot contain only whitespace.");
           }

            updateSql = constructSql(trimmedUpdateSql, params);
            insertSql = constructSql(trimmedInsertSql, params);
            connectionId = p_profile.getPreviewConnectionId();
        }
        else
        {
           trimmedUpdateSql = p_profile.getFinalUpdateSql().trim();
           trimmedInsertSql = p_profile.getFinalInsertSql().trim();

           if (trimmedUpdateSql.equals("") || trimmedInsertSql.equals(""))
           {
              throw new DatabaseWriteException("SQL cannot contain only whitespace.");
           }
           
            updateSql = constructSql(trimmedUpdateSql, params);
            insertSql = constructSql(trimmedInsertSql, params);
            connectionId = p_profile.getFinalConnectionId();
        }
        return new SqlHolder(updateSql, insertSql, connectionId);
    }

    /* Construct executable SQL by merging the parameters into the template */
    private String constructSql(String p_template, HashMap p_params)
    throws DatabaseWriteException
    {
        String sql = null;
        try
        {
            sql = SqlParameterSubstituter.substitute(p_template, p_params);
        }
        catch (SqlParameterSubstitutionException e)
        {
            throw new DatabaseWriteException("Parameter substitution failure", e);
        }
        return sql;
    }
}

