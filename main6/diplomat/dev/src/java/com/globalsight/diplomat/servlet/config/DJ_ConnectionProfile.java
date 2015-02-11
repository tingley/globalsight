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

public class DJ_ConnectionProfile
{
    private long m_id = 0;
    private String m_profileName = null;
    private String m_driver = null;
    private String m_connectionString = null;
    private String m_userName = null;
    private String m_password = null;

    private final String INCREMENT_CONNECTION_ID = "INCREMENT_CONNECTION_ID";
    private final String CONNECTION_PROFILE = "CONNECTION_PROFILE";
    private final String CONNECTION_PROFILE_ID = "CONNECTION_PROFILE_ID";
    private final String CONNECTION_ID = "CONNECTION_ID";
    private final String NAME = "NAME";
    private final String DRIVER = "DRIVER";
    private final String CONNECTION = "CONNECTION";
    private final String USER_NAME = "USER_NAME";
    private final String PASSWORD = "PASSWORD";
    private final String RECORD_PROFILE = "RECORD_PROFILE";
    private final String TASK_QUEUE_PROFILE = "TASK_QUEUE_PROFILE";

    // ///////////////////////////////////////////////
    public DJ_ConnectionProfile(long p_id)
    {
        m_id = p_id;

        // retrieve information from the database
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            // retrieve all the columns
            Statement query = connection.createStatement();

            String sql = "SELECT NAME, DRIVER, CONNECTION, USER_NAME, PASSWORD FROM "
                    + CONNECTION_PROFILE
                    + " WHERE "
                    + CONNECTION_PROFILE_ID
                    + " = " + m_id;

            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                m_profileName = results.getString(NAME);
                m_driver = results.getString(DRIVER);
                m_connectionString = results.getString(CONNECTION);
                m_userName = results.getString(USER_NAME);
                m_password = results.getString(PASSWORD);
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger
                    .printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", e);
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
    public DJ_ConnectionProfile(String p_profileName, String p_driver,
            String p_connectionString, String p_userName, String p_password)
    {
        this(0, p_profileName, p_driver, p_connectionString, p_userName,
                p_password);
    }

    // ///////////////////////////////////////////////
    public DJ_ConnectionProfile(long p_id, String p_profileName,
            String p_driver, String p_connectionString, String p_userName,
            String p_password)
    {
        m_id = p_id;
        m_profileName = p_profileName;
        m_driver = p_driver;
        m_connectionString = p_connectionString;
        m_userName = p_userName;
        m_password = p_password;
    }

    // ///////////////////////////////////////////////
    public boolean equals(long p_id)
    {
        DJ_ConnectionProfile profile = new DJ_ConnectionProfile(p_id);
        return (equals(profile));
    }

    // ///////////////////////////////////////////////
    public boolean equals(DJ_ConnectionProfile p_profile)
    {
        if ((m_id == p_profile.getID())
                && (0 == (m_profileName.compareTo(p_profile.getProfileName())))
                && (0 == (m_driver.compareTo(p_profile.getDriver())))
                && (0 == (m_connectionString.compareTo(p_profile
                        .getConnectionString())))
                && (0 == (m_userName.compareTo(p_profile.getUserName())))
                && (0 == (m_password.compareTo(p_profile.getPassword()))))
            return true;
        else
            return false;
    }

    // ///////////////////////////////////////////////
    public long getID()
    {
        return m_id;
    }

    public String getProfileName()
    {
        return m_profileName;
    }

    public String getDriver()
    {
        return m_driver;
    }

    public String getConnectionString()
    {
        return m_connectionString;
    }

    // ///////////////////////////////////////////////
    public String getUserName()
    {
        if (m_userName == null)
            return (new String(""));
        else
            return m_userName;
    }

    // ///////////////////////////////////////////////
    public String getPassword()
    {
        if (m_password == null)
            return (new String(""));
        else
            return m_password;
    }

    // ///////////////////////////////////////////////
    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setProfileName(String p_profileName)
    {
        m_profileName = p_profileName;
    }

    public void setDriver(String p_driver)
    {
        m_driver = p_driver;
    }

    public void setConnectionString(String p_connectionString)
    {
        m_connectionString = p_connectionString;
    }

    public void setUserName(String p_userName)
    {
        m_userName = p_userName;
    }

    public void setPassword(String p_password)
    {
        m_password = p_password;
    }

    public void deleteEntry()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            if (m_id > 0)
            {
                String sql = "DELETE FROM " + CONNECTION_PROFILE + " WHERE "
                        + CONNECTION_PROFILE_ID + " = " + m_id;
                Statement s = connection.createStatement();
                s.executeUpdate(sql);
                connection.commit();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger
                    .printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", e);
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
            String sql = "UPDATE " + CONNECTION_PROFILE + " SET " + NAME + "="
                    + Utility.quote(m_profileName) + ", " + DRIVER + "="
                    + Utility.quote(m_driver) + ", " + CONNECTION + "="
                    + Utility.quote(m_connectionString) + ", " + USER_NAME
                    + "=" + Utility.quote(m_userName) + ", " + PASSWORD + "="
                    + Utility.quote(m_password) + " WHERE "
                    + CONNECTION_PROFILE_ID + " = " + m_id;
            Statement s = connection.createStatement();
            s.executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger
                    .printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", e);
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
            String sql = "INSERT INTO " + CONNECTION_PROFILE + " VALUES("
                    + INCREMENT_CONNECTION_ID + ".NEXTVAL ,"
                    + Utility.quote(m_profileName) + ","
                    + Utility.quote(m_driver) + ","
                    + Utility.quote(m_connectionString) + ","
                    + Utility.quote(m_userName) + ","
                    + Utility.quote(m_password) + ")";
            Statement s = connection.createStatement();
            s.executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger
                    .printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", e);
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
    public Dependency checkDependencies()
    {
        Dependency dependency = null;
        // retrieve records from record profile table
        Vector records = DJ_RecordProfile.retrieveRecordProfiles();
        // look for connection id in records
        for (int i = 0; i < records.size(); ++i)
        {
            DJ_RecordProfile profile = (DJ_RecordProfile) records.elementAt(i);
            if ((profile.getAcquisitionConnectID() == m_id)
                    || (profile.getFinalConnectID() == m_id)
                    || (profile.getPreviewConnectID() == m_id))
            {
                dependency = new Dependency(profile.getID(), profile.getName(),
                        RECORD_PROFILE);
                break;
            }
        }

        if (dependency == null)
        {
            // retrieve records from task queue profile table
            Vector tasks = DJ_TaskQueue.retrieveTaskProfiles();
            for (int i = 0; i < tasks.size(); ++i)
            {
                DJ_TaskQueue profile = (DJ_TaskQueue) tasks.elementAt(i);
                if (profile.getConnectionID() == m_id)
                {
                    dependency = new Dependency(profile.getID(),
                            profile.getName(), TASK_QUEUE_PROFILE);
                    break;
                }
            }
        }

        return dependency;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveConnectionProfiles()
    {
        Connection connection = null;
        Vector connections = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            String sql = "SELECT CONNECTION_PROFILE_ID FROM CONNECTION_PROFILE ORDER BY CONNECTION_PROFILE_ID";
            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                long id = results.getLong(1);
                connections.add(new DJ_ConnectionProfile(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger
                    .printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_ConnectionProfile", e);
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

        return connections;
    }

    private Logger theLogger = Logger.getLogger();
}
