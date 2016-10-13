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
package com.globalsight.diplomat.servlet.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

public class DJ_TaskQueue
{
    static private final String ORDER_BY_ID = "SELECT TASK_QUEUE_PROFILE_ID FROM TASK_QUEUE_PROFILE ORDER BY TASK_QUEUE_PROFILE_ID";
    static private final String ORDER_BY_NAME = "SELECT TASK_QUEUE_PROFILE_ID FROM TASK_QUEUE_PROFILE ORDER BY TQP_NAME";

    private final String TASK_CLASSIFIER = "TASK_CLASSIFIER";
    private final String TASK_QUEUE_PROFILE = "TASK_QUEUE_PROFILE";
    private final String TASK_QUEUE_PROFILE_ID = "TASK_QUEUE_PROFILE_ID";
    private final String INCREMENT_TQP_ID = "INCREMENT_TQP_ID";
    private final String TQP_NAME = "TASK_QUEUE_PROFILE_NAME";
    private final String TQ_TABLE_NAME = "TASK_QUEUE_TABLE_NAME";
    private final String TQ_CONNECTION_ID = "TASK_QUEUE_CONNECTION_ID";
    private final String RECORDS_PER_PAGE = "RECORDS_PER_PAGE";
    private final String PAGES_PER_BATCH = "PAGES_PER_BATCH";
    private final String MAX_ELAPSED_MILLIS = "MAX_ELAPSED_MILLIS";

    private long m_id = 0;
    private String m_name = "";
    private String m_tableName = "";
    private long m_connectionID = 0;
    private int m_recordsPerPage = 0;
    private int m_pagesPerBatch = 0;
    private long m_maxElapsedMillis = 0;

    // ///////////////////////////////////////////////
    public DJ_TaskQueue(long p_id, String p_name, String p_tableName,
            long p_connectionID, int p_recordsPerPage, int p_pagesPerBatch,
            long p_maxElapsedMillis)
    {
        m_id = p_id;
        m_name = p_name;
        m_tableName = p_tableName;
        m_connectionID = p_connectionID;
        m_recordsPerPage = p_recordsPerPage;
        m_pagesPerBatch = p_pagesPerBatch;
        m_maxElapsedMillis = p_maxElapsedMillis;
    }

    // ///////////////////////////////////////////////
    public DJ_TaskQueue(long p_id)
    {
        Connection connection = null;
        m_id = p_id;

        // retrieve records from database
        try
        {
            // retrieve all the columns
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();

            String sql = "SELECT * FROM " + TASK_QUEUE_PROFILE + " WHERE "
                    + TASK_QUEUE_PROFILE_ID + " = " + m_id;

            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                m_name = results.getString(TQP_NAME);
                m_tableName = results.getString(TQ_TABLE_NAME);
                m_connectionID = results.getLong(TQ_CONNECTION_ID);
                m_recordsPerPage = results.getInt(RECORDS_PER_PAGE);
                m_pagesPerBatch = results.getInt(PAGES_PER_BATCH);
                m_maxElapsedMillis = results.getLong(MAX_ELAPSED_MILLIS);
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    // ///////////////////////////////////////////////
    public long getID()
    {
        return m_id;
    }

    public String getName()
    {
        return m_name;
    }

    public String getTableName()
    {
        return m_tableName;
    }

    public long getConnectionID()
    {
        return m_connectionID;
    }

    public int getRecordsPerPage()
    {
        return m_recordsPerPage;
    }

    public int getPagesPerBatch()
    {
        return m_pagesPerBatch;
    }

    public long getMaxElapsedMillis()
    {
        return m_maxElapsedMillis;
    }

    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setTableName(String p_tableName)
    {
        m_tableName = p_tableName;
    }

    public void setConnectionID(long p_connectionID)
    {
        m_connectionID = p_connectionID;
    }

    public void setRecordsPerPage(int p_recordsPerPage)
    {
        m_recordsPerPage = p_recordsPerPage;
    }

    public void setPagesPerBatch(int p_pagesPerBatch)
    {
        m_pagesPerBatch = p_pagesPerBatch;
    }

    public void setMaxElapsedMillis(long p_maxElapsedMillis)
    {
        m_maxElapsedMillis = p_maxElapsedMillis;
    }

    // ///////////////////////////////////////////////
    private String deleteSql(String p_tableName)
    {
        return "DELETE FROM " + p_tableName + " WHERE " + TASK_QUEUE_PROFILE_ID
                + " = " + m_id;
    }

    public void deleteEntry()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            Statement st = connection.createStatement();
            if (m_id > 0)
            {
                st.executeUpdate(deleteSql(TASK_CLASSIFIER));
                st.executeUpdate(deleteSql(TASK_QUEUE_PROFILE));
                connection.commit();
            }
            st.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", e);
            // attempt a roll-back
            try
            {
                if (connection != null)
                    connection.rollback();
            }
            catch (SQLException sqle2)
            {
                theLogger.printStackTrace(Logger.ERROR, "Could not rollback: ",
                        sqle2);
            }
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    // ///////////////////////////////////////////////
    public void update()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            String sql = "UPDATE " + TASK_QUEUE_PROFILE + " SET " + TQP_NAME
                    + "=" + Utility.quote(m_name) + ", " + TQ_TABLE_NAME + "="
                    + Utility.quote(m_tableName) + ", " + TQ_CONNECTION_ID
                    + "=" + m_connectionID + ", " + RECORDS_PER_PAGE + "="
                    + m_recordsPerPage + ", " + PAGES_PER_BATCH + "="
                    + m_pagesPerBatch + ", " + MAX_ELAPSED_MILLIS + "="
                    + m_maxElapsedMillis + " WHERE " + TASK_QUEUE_PROFILE_ID
                    + "=" + m_id;
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", e);
            // attempt a roll-back
            try
            {
                if (connection != null)
                    connection.rollback();
            }
            catch (SQLException sqle2)
            {
                theLogger.printStackTrace(Logger.ERROR, "Could not rollback: ",
                        sqle2);
            }
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }
    }

    // ///////////////////////////////////////////////
    public void insert()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            String sql = "INSERT INTO " + TASK_QUEUE_PROFILE + " VALUES("
                    + INCREMENT_TQP_ID + ".NEXTVAL," + Utility.quote(m_name)
                    + "," + Utility.quote(m_tableName) + "," + m_connectionID
                    + "," + m_recordsPerPage + "," + m_pagesPerBatch + ","
                    + m_maxElapsedMillis + ")";
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", e);
            // attempt a roll-back
            try
            {
                if (connection != null)
                    connection.rollback();
            }
            catch (SQLException sqle2)
            {
                theLogger.printStackTrace(Logger.ERROR, "Could not rollback: ",
                        sqle2);
            }
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }

    }

    // ///////////////////////////////////////////////
    static private Vector retrieveAllProfiles(String p_sql)
    {
        Connection connection = null;
        Vector files = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(p_sql);
            while (results.next())
            {
                long id = results.getLong(1);
                files.add(new DJ_TaskQueue(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_TaskQueue", e);
        }
        finally
        {
            try
            {
                ConnectionPool.returnConnection(connection);
            }
            catch (ConnectionPoolException cpe)
            {
            }
        }

        return files;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveTaskProfiles()
    {
        return retrieveAllProfiles(ORDER_BY_ID);
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveTaskProfilesByName()
    {
        return retrieveAllProfiles(ORDER_BY_NAME);
    }

    private Logger theLogger = Logger.getLogger();
}
