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

package com.globalsight.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import com.globalsight.diplomat.util.database.ConnectionPool;
/**
 * The PreparedStatementBatch holds a batch of PreparedStatements
 * where each PreparedStatement is used with the PreparedStatement.addBatch()
 * call. There appears to be a limit to the number of times
 * addBatch() can be called on a given PreparedStatement, and thus the
 * need for this class.
 */
public class PreparedStatementBatch
{
    /** Arbitrary batch size. Seems like a batch size above 15000 gives problems.*/
    public static final int DEFAULT_BATCH_SIZE = 5000;
    private int m_batchSize;
    private ArrayList m_preparedStatements;
    private int m_count = 0; //number of statements processed so far
    private Connection m_connection = null;
    private String m_sql; //SQL statement to use
    private boolean m_callCreationState = false;

    /**
     * Creates a PreparedStatementBatch
     * 
     * @param p_batchSize
     *               batch size to use (5000 is default)
     * @param p_connection
     *               Connection
     * @param p_sql  SQL statement to use for prepared statements
     * @param p_callCreationState --true if .creationState() should be called
     *        on newly created PreparedStatements (default is false)
     */
    public PreparedStatementBatch(int p_batchSize,
                                  Connection p_connection,
                                  String p_sql,
                                  boolean p_callCreationState)
    {
        m_batchSize = p_batchSize;
        m_preparedStatements = new ArrayList();
        m_connection = p_connection;
        m_sql = p_sql;
        m_callCreationState = p_callCreationState;
    }

    /** Gets the current batch count */
    public int getBatchCount()
    {
        return m_count;
    }

    /** Gets the current number of PreparedStatements count */
    public int getNumPreparedStatements()
    {
        return m_preparedStatements.size();
    }

    /** Gets a prepared statement appropriate for the current batch count.
    * This could be an existing prepared statement or a new one depending
    * on how many times this method has been called.
    * @throws SQLException
    */
    public PreparedStatement getNextPreparedStatement() throws SQLException
    {
        PreparedStatement ps;
        int batchCount = m_count / m_batchSize;
        if(batchCount + 1 > m_preparedStatements.size())
        {
            //create a new prepared statement
            ps = m_connection.prepareStatement(m_sql);

            m_preparedStatements.add(ps);
        }
        else
        {
            //get a pre-existing prepared statement
            ps = (PreparedStatement)m_preparedStatements.get(batchCount);
        }
        m_count++;
        return ps;
    }

    /** Executes all the Prepared Statements */
    public void executeBatches() throws SQLException
    {
        PreparedStatement ps = null;
        for (int i=0; i < m_preparedStatements.size(); i++)
        {
            ps = (PreparedStatement)m_preparedStatements.get(i);
            ps.executeBatch();
        }
    }

    /**
     * Closes all PreparedStatements and clears state
     */
    public void closeAll()
    {
        PreparedStatement ps = null;
        for (int i=0; i < m_preparedStatements.size(); i++)
        {
            ps = (PreparedStatement) m_preparedStatements.get(i);
            ConnectionPool.silentClose(ps);
        }
        m_preparedStatements.clear();
        m_connection = null;
        m_count = 0;
        m_sql = null;
    }
}

