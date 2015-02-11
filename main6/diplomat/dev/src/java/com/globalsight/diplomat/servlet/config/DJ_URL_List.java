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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

public class DJ_URL_List
{
    private long m_id = 0;
    private String m_name = "";
    private String m_xml = "";

    private final String URL_LIST = "URL_LIST";
    private final String URL_ID = "URL_ID";
    private final String URL_XML = "URL_XML";
    private final String NAME = "NAME";
    private final String INCREMENT_URL_LIST = "INCREMENT_URL_LIST";
    private final String RECORD_PROFILE = "RECORD_PROFILE";

    // ///////////////////////////////////////////////
    public DJ_URL_List(long p_id, String p_name, String p_xml)
    {
        m_id = p_id;
        m_name = p_name;
        m_xml = p_xml;
    }

    // ///////////////////////////////////////////////
    public DJ_URL_List(long p_id)
    {
        Connection connection = null;
        m_id = p_id;

        // retrieve information from the database
        try
        {
            connection = ConnectionPool.getConnection();
            // retrieve all the columns
            Statement query = connection.createStatement();

            String sql = "SELECT * FROM " + URL_LIST + " WHERE " + URL_ID
                    + " = " + m_id;

            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                m_name = results.getString(NAME);

                m_xml = results.getString(URL_XML);
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", e);
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

    public String getXML()
    {
        return m_xml;
    }

    // ///////////////////////////////////////////////
    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setXML(String p_xml)
    {
        m_xml = p_xml;
    }

    // ///////////////////////////////////////////////
    public void deleteEntry()
    {
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            _deleteEntry(connection);
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", e);
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

    private void _deleteEntry(Connection connection) throws SQLException
    {
        if (m_id > 0)
        {
            connection.setAutoCommit(false);
            String sql = "DELETE FROM " + URL_LIST + " WHERE " + URL_ID + " = "
                    + m_id;
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
    }

    // ///////////////////////////////////////////////
    public void save()
    {
        theLogger.println(Logger.DEBUG_D, "DJ_URL_List: saving");
        Connection connection = null;
        boolean isUpdate = false;
        try
        {
            connection = ConnectionPool.getConnection();
            boolean oldFlag = connection.getAutoCommit();
            connection.setAutoCommit(false);

            PreparedStatement preparedStatement = null;

            if (m_id == 0)
            {
                // retrieve the unique index value
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT "
                        + INCREMENT_URL_LIST + ".NEXTVAL FROM DUAL");
                while (results.next())
                    m_id = results.getLong(1);
                statement.close();
            }
            else
            {
                isUpdate = true; // row exists, just update it
                theLogger.println(Logger.DEBUG_D,
                        "DJ_URL_List: old row. doing update.");
            }

            if (!isUpdate)
            {
                theLogger.println(Logger.DEBUG_D,
                        "DJ_URL_List: new row. doing insert.");
                String sql = "INSERT INTO " + URL_LIST + " VALUES(" + m_id
                        + ", " + Utility.quote(m_name) + ", ?)";
                preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, m_xml);
                preparedStatement.execute();
            }

            connection.commit();
            connection.setAutoCommit(oldFlag);

            if (preparedStatement != null)
            {
                preparedStatement.close();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", e);
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
            if (profile.getPreviewUrlID() == m_id)
            {
                dependency = new Dependency(profile.getID(), profile.getName(),
                        RECORD_PROFILE);
                break;
            }
        }

        return dependency;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveURLProfiles()
    {
        Connection connection = null;
        Vector urls = new Vector();
        Logger theLogger = Logger.getLogger();
        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            String sql = "SELECT URL_ID FROM URL_LIST ORDER BY URL_ID";
            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                long id = results.getLong(1);
                urls.add(new DJ_URL_List(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_URL_List", e);
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

        return urls;
    }

    private Logger theLogger = Logger.getLogger();
}
