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

public class DJ_FileProfile
{
    static private final String ORDER_BY_ID = "SELECT ID FROM FILE_PROFILE ORDER BY ID";
    static private final String ORDER_BY_NAME = "SELECT ID FROM FILE_PROFILE ORDER BY NAME";

    private final String FILE_PROFILE = "FILE_PROFILE";
    private final String FILE_PROFILE_ID = "ID";
    private final String NAME = "NAME";
    private final String DATA_TYPE = "KNOWN_FORMAT_TYPE_ID";
    private final String RULE_ID = "RULE_ID";
    private final String INCREMENT_FILE_PROFILE = "INCREMENT_FILE_PROFILE";

    private long m_id = 0;
    private String m_name = "";
    private long m_type = 0;
    private long m_rule = 0;

    // ///////////////////////////////////////////////
    public DJ_FileProfile(long p_id, String p_name, long p_type, long p_rule)
    {
        m_id = p_id;
        m_name = p_name;
        m_type = p_type;
        m_rule = p_rule;
    }

    // ///////////////////////////////////////////////
    public DJ_FileProfile(long p_id)
    {
        Connection connection = null;
        m_id = p_id;

        // retrieve records from database

        try
        {
            // retrieve all the columns
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();

            String sql = "SELECT NAME, KNOWN_FORMAT_TYPE_ID, RULE_ID FROM "
                    + FILE_PROFILE + " WHERE " + FILE_PROFILE_ID + " = " + m_id;

            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                m_name = results.getString(NAME);
                m_type = results.getLong(DATA_TYPE);
                m_rule = results.getLong(RULE_ID);
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", e);
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

    public long getType()
    {
        return m_type;
    }

    public long getRule()
    {
        return m_rule;
    }

    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setType(long p_type)
    {
        m_type = p_type;
    }

    public void setRule(long p_rule)
    {
        m_rule = p_rule;
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
                String sql = "DELETE FROM " + FILE_PROFILE + " WHERE "
                        + FILE_PROFILE_ID + " = " + m_id;
                connection.createStatement().executeUpdate(sql);
                connection.commit();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", e);
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
            String sql;
            if (m_rule > 0)
            {
                sql = "UPDATE " + FILE_PROFILE + " SET " + NAME + "="
                        + Utility.quote(m_name) + ", " + DATA_TYPE + "="
                        + m_type + ", " + RULE_ID + "=" + m_rule + " WHERE "
                        + FILE_PROFILE_ID + "=" + m_id;
            }
            else
            {
                sql = "UPDATE " + FILE_PROFILE + " SET " + NAME + "="
                        + Utility.quote(m_name) + ", " + DATA_TYPE + "="
                        + m_type + " WHERE " + FILE_PROFILE_ID + "=" + m_id;
            }
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", e);
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
        String sql;
        Connection connection = null;

        try
        {
            connection = ConnectionPool.getConnection();
            // retrieve a new id
            sql = "SELECT " + INCREMENT_FILE_PROFILE + ".NEXTVAL FROM DUAL";
            Statement query = connection.createStatement();
            ResultSet result = query.executeQuery(sql);
            if (result.next())
                m_id = result.getLong(1);
            query.close();

            // write the record
            if (m_rule > 0)
            {
                sql = "INSERT INTO " + FILE_PROFILE + " VALUES(" + m_id + ", "
                        + Utility.quote(m_name) + "," + m_type + "," + m_rule
                        + ")";
            }
            else
            {
                sql = "INSERT INTO " + FILE_PROFILE + "( " + FILE_PROFILE_ID
                        + "," + NAME + "," + DATA_TYPE + ") VALUES(" + m_id
                        + ", " + Utility.quote(m_name) + ", " + m_type + ")";
            }
            connection.createStatement().executeUpdate(sql);
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", e);
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
                files.add(new DJ_FileProfile(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfile", e);
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
