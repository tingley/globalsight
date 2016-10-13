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

public class DJ_KnownFormat
{
    static private final String ORDER_BY_ID = "SELECT ID FROM KNOWN_FORMAT_TYPE ORDER BY ID";
    static private final String ORDER_BY_NAME = "SELECT ID FROM KNOWN_FORMAT_TYPE ORDER BY NAME";

    private final String KNOWN_FORMAT_TYPE = "KNOWN_FORMAT_TYPE";
    private final String ID = "ID";
    private final String NAME = "NAME";
    private final String FORMAT_TYPE = "FORMAT_TYPE";
    private final String PRE_EXTRACT_EVENT = "PRE_EXTRACT_EVENT";
    private final String PRE_MERGE_EVENT = "PRE_MERGE_EVENT";
    private final String INCREMENT_KNOWN_FORMAT_ID = "INCREMENT_KNOWN_FORMAT_ID";

    private long m_id = 0;
    private String m_name = "";
    private String m_formatType = "";
    private String m_preExtractEvent = "";
    private String m_preMergeEvent = "";

    // ///////////////////////////////////////////////
    public DJ_KnownFormat(long p_id, String p_name, String p_formatType,
            String p_preExtractEvent, String p_preMergeEvent)
    {
        m_id = p_id;
        m_name = p_name;
        m_formatType = p_formatType;
        m_preExtractEvent = p_preExtractEvent;
        m_preMergeEvent = p_preMergeEvent;
    }

    // ///////////////////////////////////////////////
    public DJ_KnownFormat(long p_id)
    {
        Connection connection = null;
        m_id = p_id;

        // retrieve records from database
        try
        {
            // retrieve all the columns
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();

            String sql = "SELECT NAME, FORMAT_TYPE, PRE_EXTRACT_EVENT, PRE_MERGE_EVENT FROM "
                    + KNOWN_FORMAT_TYPE + " WHERE " + ID + " = " + m_id;

            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                m_name = results.getString(NAME);
                m_formatType = results.getString(FORMAT_TYPE);
                m_preExtractEvent = results.getString(PRE_EXTRACT_EVENT);
                m_preMergeEvent = results.getString(PRE_MERGE_EVENT);
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", e);
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

    public String getFormatType()
    {
        return m_formatType;
    }

    public String getPreExtractEvent()
    {
        return m_preExtractEvent;
    }

    public String getPreMergeEvent()
    {
        return m_preMergeEvent;
    }

    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setFormatType(String p_formatType)
    {
        m_formatType = p_formatType;
    }

    public void setPreExtractEvent(String p_preExtractEvent)
    {
        m_preExtractEvent = p_preExtractEvent;
    }

    public void setPreMergeEvent(String p_preMergeEvent)
    {
        m_preMergeEvent = p_preMergeEvent;
    }

    // ///////////////////////////////////////////////
    public void deleteEntry()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            if (m_id > 0)
            {
                String sql = "DELETE FROM " + KNOWN_FORMAT_TYPE + " WHERE "
                        + ID + " = " + m_id;
                connection.createStatement().executeUpdate(sql);
                connection.commit();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", e);
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
            String sql = "UPDATE " + KNOWN_FORMAT_TYPE + " SET " + NAME + "="
                    + Utility.quote(m_name) + ", " + FORMAT_TYPE + "="
                    + Utility.quote(m_formatType) + ", " + PRE_EXTRACT_EVENT
                    + "=" + Utility.quote(m_preExtractEvent) + ", "
                    + PRE_MERGE_EVENT + "=" + Utility.quote(m_preMergeEvent)
                    + " WHERE " + ID + "=" + m_id;
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", e);
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
            String sql = "INSERT INTO " + KNOWN_FORMAT_TYPE + " VALUES("
                    + INCREMENT_KNOWN_FORMAT_ID + ".NEXTVAL,"
                    + Utility.quote(m_name) + "," + Utility.quote(m_formatType)
                    + "," + Utility.quote(m_preExtractEvent) + ","
                    + Utility.quote(m_preMergeEvent) + ")";
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", e);
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
        Vector types = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(p_sql);
            while (results.next())
            {
                long id = results.getLong(1);
                types.add(new DJ_KnownFormat(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_KnownFormat", e);
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

        return types;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveFileProfiles()
    {
        return retrieveAllProfiles(ORDER_BY_ID);
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveFileProfilesByName()
    {
        return retrieveAllProfiles(ORDER_BY_NAME);
    }

    private Logger theLogger = Logger.getLogger();
}
