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

public class DJ_XML_Rule
{
    static private final String ORDER_BY_ID = "SELECT XML_RULE_ID, NAME FROM XML_RULE ORDER BY XML_RULE_ID";
    static private final String ORDER_BY_NAME = "SELECT XML_RULE_ID, NAME FROM XML_RULE ORDER BY NAME";

    private final String XML_RULE = "XML_RULE";
    private final String XML_RULE_ID = "XML_RULE_ID";
    private final String RULE_TEXT = "RULE_TEXT";
    private final String NAME = "NAME";
    private final String INCREMENT_XML_RULE = "INCREMENT_XML_RULE";
    private final int BUFFER_SIZE = 1024;
    private final String RECORD_PROFILE_ID = "RECORD_PROFILE_ID";
    private final String COLUMN_PROFILE = "COLUMN_PROFILE";
    private final String RULE_ID = "RULE_ID";
    private final String RECORD_PROFILE = "RECORD_PROFILE";
    private final String FILE_PROFILE = "FILE_PROFILE";

    private long m_id = 0;
    private String m_name = null;
    private String m_xml = null;

    // flags to hold whether we've gotten the name and rule_file from the DB
    private boolean m_have_name = false;
    private boolean m_have_xml = false;

    // ///////////////////////////////////////////////
    public DJ_XML_Rule(long p_id, String p_name, String p_xml)
    {
        m_id = p_id;
        m_name = p_name;
        m_have_name = true;

        m_xml = p_xml;
        m_have_xml = true;
    }

    // constructors to allow lazy instantiation of the xml itself
    public DJ_XML_Rule(long p_id)
    {
        m_id = p_id;
        m_name = null;
        m_have_name = false;

        m_xml = null;
        m_have_xml = false;
    }

    public DJ_XML_Rule(long p_id, String p_name)
    {
        m_id = p_id;
        m_name = p_name;
        m_have_name = true;

        m_xml = null;
        m_have_xml = false;
    }

    // ///////////////////////////////////////////////
    public long getID()
    {
        return m_id;
    }

    public String getName()
    {
        if (!m_have_name)
            _getName();
        return m_name;
    }

    public String getXML()
    {
        if (!m_have_xml)
            _getXml();
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
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", e);
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

    private void _deleteEntry(Connection p_connection) throws SQLException
    {
        if (m_id > 0)
        {
            p_connection.setAutoCommit(false);
            String sql = "DELETE FROM " + XML_RULE + " WHERE " + XML_RULE_ID
                    + " = " + m_id;
            Statement statement = p_connection.createStatement();
            statement.executeUpdate(sql);
            p_connection.commit();
        }
    }

    /*
     * Fills in the m_xml field with the rule file from the database. This
     * lazily instantiates the m_xml since getXml is only called when needed.
     */
    private void _getXml()
    {
        Connection connection = null;
        try
        {
            theLogger.println(Logger.DEBUG_D,
                    "DJ_Xml_Rule: reading rule file from DB for id " + m_id);
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            String sql = "SELECT RULE_TEXT FROM " + XML_RULE + " WHERE "
                    + XML_RULE_ID + "=" + m_id;
            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                theLogger.println(Logger.DEBUG_D, "DJ_Xml_Rule: getting clob.");
                m_xml = results.getString(1);
            }
            query.close();
            m_have_xml = true;
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", cpe);
            m_xml = null;
            m_have_xml = false;
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", e);
            m_xml = null;
            m_have_xml = false;
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

    /*
     * Fills in the m_name field with the rule file from the database. This
     * lazily instantiates the m_xml since getXml is only called when needed.
     */
    private void _getName()
    {
        Connection connection = null;
        try
        {
            theLogger.println(Logger.DEBUG_D,
                    "DJ_Xml_Rule: reading name from DB for id " + m_id);
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            String sql = "SELECT NAME FROM " + XML_RULE + " WHERE "
                    + XML_RULE_ID + "=" + m_id;
            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                m_name = results.getString(1);
            }
            query.close();
            m_have_name = true;
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", cpe);
            m_name = null;
            m_have_name = false;
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", e);
            m_name = null;
            m_have_name = false;
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
    public void save()
    {
        Connection connection = null;
        boolean isUpdate = false;

        try
        {
            connection = ConnectionPool.getConnection();
            PreparedStatement preparedStatement = null;
            boolean oldFlag = connection.getAutoCommit();
            connection.setAutoCommit(false);
            if (m_id == 0)
            {
                // retrieve the unique index value
                Statement statement = connection.createStatement();
                ResultSet results = statement.executeQuery("SELECT "
                        + INCREMENT_XML_RULE + ".NEXTVAL FROM DUAL");
                while (results.next())
                    m_id = results.getLong(1);
                statement.close();
            }
            else
                isUpdate = true; // row exists, just update it

            if (!isUpdate)
            {
                String sql = "INSERT INTO " + XML_RULE + " VALUES(" + m_id
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
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", e);

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
        Connection connection = null;
        long recordProfileID = 0;
        try
        {
            // retrieve the record_profile from the column profile
            String sql = "SELECT " + RECORD_PROFILE_ID + " FROM "
                    + COLUMN_PROFILE + " WHERE " + RULE_ID + " = " + m_id;
            connection = ConnectionPool.getConnection();
            Statement statement = connection.createStatement();
            ResultSet results = statement.executeQuery(sql);
            if (results.next())
                recordProfileID = results.getLong(RECORD_PROFILE_ID);
            statement.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", e);
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

        // intantiate the dependency object if we have a record profile id
        if (recordProfileID > 0)
        {
            DJ_RecordProfile profile = new DJ_RecordProfile(recordProfileID);
            dependency = new Dependency(profile.getID(), profile.getName(),
                    RECORD_PROFILE);
        }
        else
        {
            // check the File Profile table
            Vector files = DJ_FileProfile.retrieveFileProfiles();
            for (int i = 0; i < files.size(); ++i)
            {
                DJ_FileProfile profile = (DJ_FileProfile) files.elementAt(i);
                if (profile.getRule() == m_id)
                {
                    dependency = new Dependency(profile.getID(),
                            profile.getName(), FILE_PROFILE);
                    break;
                }
            }
        }

        return dependency;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveAllProfiles(String p_sql)
    {
        Connection connection = null;
        Vector xmls = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            theLogger
                    .println(Logger.DEBUG_D, "DJ_XML_Rule: Getting connection");
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            theLogger.println(Logger.DEBUG_D, "DJ_XML_Rule: executing query");
            ResultSet results = query.executeQuery(p_sql);
            String name = "";
            long id = 0;
            while (results.next())
            {
                id = results.getLong(1);
                name = results.getString(2);
                theLogger.println(Logger.DEBUG_D,
                        "DJ_XML_Rule: creating DJ_XML_Rule. Id:" + id
                                + " Name:" + name);
                xmls.add(new DJ_XML_Rule(id, name));
            }
            theLogger.println(Logger.DEBUG_D, "DJ_XML_Rule: closing query");
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_XML_Rule", e);
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

        theLogger.println(Logger.DEBUG_D,
                "DJ_XML_Rule: returning vector of size " + xmls.size());
        return (xmls);
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveXMLProfiles()
    {
        return (retrieveAllProfiles(ORDER_BY_ID));
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveXMLProfilesByName()
    {
        return (retrieveAllProfiles(ORDER_BY_NAME));
    }

    private Logger theLogger = Logger.getLogger();
}
