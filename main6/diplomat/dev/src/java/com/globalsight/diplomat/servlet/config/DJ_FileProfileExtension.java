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
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

public class DJ_FileProfileExtension
{
    private static final String FILE_PROFILE_ID = "FILE_PROFILE_ID";
    private static final String FILE_PROFILE_EXTENSION = "FILE_PROFILE_EXTENSION";
    private static final String EXTENSION_ID = "EXTENSION_ID";

    private Vector m_fileProfileExtensionList = new Vector();

    // ///////////////////////////////////////////////
    public DJ_FileProfileExtension(Vector p_fileProfileExtensionList)
    {
        m_fileProfileExtensionList = p_fileProfileExtensionList;
    }

    // ///////////////////////////////////////////////
    public DJ_FileProfileExtension(
            WrapFileProfileExtensionTable p_fileProfileExtension)
    {
        m_fileProfileExtensionList.add(p_fileProfileExtension);
    }

    // ///////////////////////////////////////////////
    public DJ_FileProfileExtension(long p_id, boolean p_extensionBased)
    {
        Connection connection = null;

        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();

            String sql = "";
            if (p_extensionBased)
            {
                sql = "SELECT FILE_PROFILE_ID, EXTENSION_ID FROM "
                        + FILE_PROFILE_EXTENSION + " WHERE " + EXTENSION_ID
                        + " = " + p_id;
            }
            else
            {
                sql = "SELECT FILE_PROFILE_ID, EXTENSION_ID FROM "
                        + FILE_PROFILE_EXTENSION + " WHERE " + FILE_PROFILE_ID
                        + " = " + p_id;
            }

            ResultSet results = query.executeQuery(sql);
            while (results.next())
            {
                long fileProfileID = results.getLong(FILE_PROFILE_ID);
                long extensionID = results.getLong(EXTENSION_ID);
                m_fileProfileExtensionList
                        .add(new WrapFileProfileExtensionTable(fileProfileID,
                                extensionID));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfileExtension",
                    cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_FileProfileExtension",
                    e);
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
    public Vector getList()
    {
        return m_fileProfileExtensionList;
    }

    public void setList(Vector p_fileProfileExtensionList)
    {
        m_fileProfileExtensionList = p_fileProfileExtensionList;
    }

    // ///////////////////////////////////////////////
    public void save()
    {
        Connection connection = null;
        String sql = "INSERT INTO " + FILE_PROFILE_EXTENSION
                + " VALUES ( ?, ?)";

        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement pStatement = connection.prepareStatement(sql);
            for (int i = 0; i < m_fileProfileExtensionList.size(); ++i)
            {
                WrapFileProfileExtensionTable entity = (WrapFileProfileExtensionTable) m_fileProfileExtensionList
                        .elementAt(i);
                pStatement.setLong(1, entity.getFileProfileID());
                pStatement.setLong(2, entity.getExtensionID());
                pStatement.executeUpdate();
                connection.commit();
            }

            pStatement.close();
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
    /**
     * delete records from database based on the file_profile_id
     */
    public void delete()
    {
        Vector idList = new Vector();

        // find unique file_profile_id
        for (int i = 0; i < m_fileProfileExtensionList.size(); ++i)
        {
            long id = ((WrapFileProfileExtensionTable) m_fileProfileExtensionList
                    .elementAt(i)).getFileProfileID();
            boolean found = false;
            for (int j = 0; j < idList.size(); ++j)
            {
                if (id == ((Long) idList.elementAt(j)).longValue())
                {
                    found = true;
                    break;
                }
            }
            if (!found)
                idList.add(new Long(id));
        }

        Connection connection = null;
        try
        {
            connection = ConnectionPool.getConnection();
            connection.setAutoCommit(false);

            if (idList.size() > 0)
            {
                // delete all records associated with file_profile_id

                String whereClause = FILE_PROFILE_ID + " = "
                        + ((Long) idList.elementAt(0)).longValue();
                for (int i = 1; i < idList.size(); ++i)
                    whereClause += " OR " + FILE_PROFILE_ID + " = "
                            + ((Long) idList.elementAt(i)).longValue();
                String sql = "DELETE FROM " + FILE_PROFILE_EXTENSION
                        + " WHERE " + whereClause;
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

    private Logger theLogger = Logger.getLogger();
}
