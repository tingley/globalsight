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

public class DJ_Extension extends WrapExtensionTable
{
    private static final String EXTENSION = "EXTENSION";
    private static final String EXTENSION_NAME = "EXTENSION_NAME";
    private static final String EXTENSION_ID = "EXTENSION_ID";
    private static final String INCREMENT_EXTENSION_ID = "INCREMENT_EXTENSION_ID";

    static private final String ORDER_BY_ID = "SELECT EXTENSION_ID, EXTENSION_NAME FROM "
            + EXTENSION + " ORDER BY " + EXTENSION_ID;
    static private final String ORDER_BY_EXTENSION = "SELECT EXTENSION_ID, EXTENSION_NAME FROM "
            + EXTENSION + " ORDER BY " + EXTENSION_NAME;

    // ///////////////////////////////////////////////
    public DJ_Extension(long p_id, String p_extension)
    {
        super(p_id, p_extension);
    }

    // ///////////////////////////////////////////////
    public DJ_Extension(String p_extension)
    {
        super(0, p_extension);
    }

    // ///////////////////////////////////////////////
    public DJ_Extension(long p_id)
    {
        super(p_id, "");
        m_id = p_id;
        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            String sql = "SELECT " + EXTENSION_NAME + " FROM " + EXTENSION
                    + " WHERE " + EXTENSION_ID + " = " + p_id;
            ResultSet result = query.executeQuery(sql);
            if (result.next())
                m_extension = result.getString(EXTENSION_NAME);
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", e);
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
    public void deleteEntry()
    {
        Connection connection = null;
        try
        {

            if (m_id > 0)
            {
                connection = ConnectionPool.getConnection();
                connection.setAutoCommit(false);
                String sql = "DELETE FROM " + EXTENSION + " WHERE "
                        + EXTENSION_ID + " = " + m_id;
                connection.createStatement().executeUpdate(sql);
                connection.commit();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", e);
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
            String sql = "INSERT INTO " + EXTENSION + " VALUES("
                    + INCREMENT_EXTENSION_ID + ".NEXTVAL,"
                    + Utility.quote(m_extension) + ")";
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", e);
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
        Vector extensions = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(p_sql);
            while (results.next())
            {
                long id = results.getLong(1);
                extensions.add(new DJ_Extension(id));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_Extension", e);
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
        return extensions;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveExtensionProfiles()
    {
        return (retrieveAllProfiles(ORDER_BY_ID));
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveExtensionProfilesByName()
    {
        return (retrieveAllProfiles(ORDER_BY_EXTENSION));
    }

    private Logger theLogger = Logger.getLogger();
}
