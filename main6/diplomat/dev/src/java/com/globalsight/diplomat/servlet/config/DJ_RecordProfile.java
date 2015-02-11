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

public class DJ_RecordProfile
{
    static private final String ORDER_BY_ID = "SELECT RECORD_PROFILE_ID FROM RECORD_PROFILE ORDER BY RECORD_PROFILE_ID";
    static private final String ORDER_BY_NAME = "SELECT RECORD_PROFILE_ID FROM RECORD_PROFILE ORDER BY NAME";

    private final String RECORD_PROFILE = "RECORD_PROFILE";
    private final String RECORD_PROFILE_ID = "RECORD_PROFILE_ID";
    private final String INCREMENT_RECORD_PROFILE_ID = "INCREMENT_RECORD_PROFILE_ID";
    private final String COLUMN_PROFILE = "COLUMN_PROFILE";
    private final String NAME = "NAME";
    private final String ACQUISITION_SQL = "ACQUISITION_SQL";
    private final String ACQUISITION_CONTEXT_SQL = "ACQUISITION_CONTEXT_SQL";
    private final String ACQUISITION_CONNECT_ID = "ACQUISITION_CONNECT_ID";
    private final String PREVIEW_INSERT_SQL = "PREVIEW_INSERT_SQL";
    private final String PREVIEW_UPDATE_SQL = "PREVIEW_UPDATE_SQL";
    private final String PREVIEW_CONNECT_ID = "PREVIEW_CONNECT_ID";
    private final String FINAL_INSERT_SQL = "FINAL_INSERT_SQL";
    private final String FINAL_UPDATE_SQL = "FINAL_UPDATE_SQL";
    private final String FINAL_CONNECT_ID = "FINAL_CONNECT_ID";
    private final String PREVIEW_URL_ID = "PREVIEW_URL_ID";
    private final String MANUAL_MODE = "MANUAL_MODE";
    private final String TABLE_NAME = "TABLE_NAME";
    private final String COLUMN_NUMBER = "COLUMN_NUMBER";
    private final String DATA_TYPE = "DATA_TYPE";
    private final String RULE_ID = "RULE_ID";
    private final String CONTENT_MODE = "CONTENT_MODE";
    private final String COLUMN_LABEL = "COLUMN_LABEL";

    private long m_id = 0;
    private String m_name = "";
    private String m_acquisitionSql = "";
    private String m_acquisitionContextSql = "";
    private long m_acquisitionConnectID = 0;
    private String m_previewInsertSql = "";
    private String m_previewUpdateSql = "";
    private long m_previewConnectID = 0;
    private String m_finalInsertSql = "";
    private String m_finalUpdateSql = "";
    private long m_finalConnectID = 0;
    private long m_previewUrlID = 0;
    private boolean m_manualMode = false;
    private Vector m_columnList = null;

    // ///////////////////////////////////////////////
    public DJ_RecordProfile(long p_id, String p_name, String p_acquisitionSql,
            String p_acquisitionContextSql, long p_acquisitionConnectID,
            String p_previewInsertSql, String p_previewUpdateSql,
            long p_previewConnectID, String p_finalInsertSql,
            String p_finalUpdateSql, long p_finalConnectID,
            long p_previewUrlID, boolean p_manualMode, Vector p_columnList)
    {
        m_id = p_id;
        m_name = p_name;
        m_acquisitionSql = p_acquisitionSql;
        m_acquisitionContextSql = p_acquisitionContextSql;
        m_acquisitionConnectID = p_acquisitionConnectID;
        m_previewInsertSql = p_previewInsertSql;
        m_previewUpdateSql = p_previewUpdateSql;
        m_previewConnectID = p_previewConnectID;
        m_finalInsertSql = p_finalInsertSql;
        m_finalUpdateSql = p_finalUpdateSql;
        m_finalConnectID = p_finalConnectID;
        m_previewUrlID = p_previewUrlID;
        m_manualMode = p_manualMode;
        m_columnList = p_columnList;
    }

    // ///////////////////////////////////////////////
    public DJ_RecordProfile(long p_id)
    {
        Connection connection = null;
        m_id = p_id;
        m_columnList = new Vector();

        // retrieve records from database

        try
        {
            connection = ConnectionPool.getConnection();
            // sql statements are clobs
            // we convert clobs to strings below

            // retrieve all the columns
            Statement query = connection.createStatement();

            String sql = "SELECT * FROM " + RECORD_PROFILE + " r, "
                    + COLUMN_PROFILE + " c WHERE r." + RECORD_PROFILE_ID
                    + "=c." + RECORD_PROFILE_ID + " AND r." + RECORD_PROFILE_ID
                    + "=" + m_id;

            ResultSet results = query.executeQuery(sql);
            // retrieve the record profile once
            // retrieve the column profile multiple times
            boolean firstTime = true;

            while (results.next())
            {

                if (firstTime)
                {
                    m_name = results.getString(NAME);

                    m_acquisitionSql = results.getString(ACQUISITION_SQL);

                    m_acquisitionContextSql = results
                            .getString(ACQUISITION_CONTEXT_SQL);

                    m_acquisitionConnectID = results
                            .getLong(ACQUISITION_CONNECT_ID);

                    m_previewInsertSql = results.getString(PREVIEW_INSERT_SQL);

                    m_previewUpdateSql = results.getString(PREVIEW_UPDATE_SQL);

                    m_previewConnectID = results.getLong(PREVIEW_CONNECT_ID);

                    m_finalInsertSql = results.getString(FINAL_INSERT_SQL);

                    m_finalUpdateSql = results.getString(FINAL_UPDATE_SQL);

                    m_finalConnectID = results.getLong(FINAL_CONNECT_ID);

                    m_previewUrlID = results.getLong(PREVIEW_URL_ID);
                    m_manualMode = (results.getInt(MANUAL_MODE) == 1) ? true
                            : false;
                }

                firstTime = false;

                int column = results.getInt(COLUMN_NUMBER);
                String tableName = results.getString(TABLE_NAME);
                long type = results.getLong(DATA_TYPE);
                long rule = results.getLong(RULE_ID);
                int mode = results.getInt(CONTENT_MODE);
                String label = results.getString(COLUMN_LABEL);

                m_columnList.add(new DJ_ColumnProfile(column, tableName, type,
                        rule, mode, label));
            }
            query.close();
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_RecordProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_RecordProfile", e);
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
    public DJ_RecordProfile(String p_name, String p_acquisitionSql,
            String p_acquisitionContextSql, long p_acquisitionConnectID,
            String p_previewInsertSql, String p_previewUpdateSql,
            long p_previewConnectID, String p_finalInsertSql,
            String p_finalUpdateSql, long p_finalConnectID,
            long p_previewUrlID, boolean p_manualMode, Vector p_columnList)
    {
        m_name = p_name;
        m_acquisitionSql = p_acquisitionSql;
        m_acquisitionContextSql = p_acquisitionContextSql;
        m_acquisitionConnectID = p_acquisitionConnectID;
        m_previewInsertSql = p_previewInsertSql;
        m_previewUpdateSql = p_previewUpdateSql;
        m_previewConnectID = p_previewConnectID;
        m_finalInsertSql = p_finalInsertSql;
        m_finalUpdateSql = p_finalUpdateSql;
        m_finalConnectID = p_finalConnectID;
        m_previewUrlID = p_previewUrlID;
        m_manualMode = p_manualMode;
        m_columnList = p_columnList;
    }

    // ///////////////////////////////////////////////
    public DJ_RecordProfile()
    {
        m_columnList = new Vector();
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

    public String getAcquisitionSql()
    {
        return m_acquisitionSql;
    }

    public String getAcquisitionContextSql()
    {
        return m_acquisitionContextSql;
    }

    public long getAcquisitionConnectID()
    {
        return m_acquisitionConnectID;
    }

    public String getPreviewInsertSql()
    {
        return m_previewInsertSql;
    }

    public String getPreviewUpdateSql()
    {
        return m_previewUpdateSql;
    }

    public long getPreviewConnectID()
    {
        return m_previewConnectID;
    }

    public String getFinalInsertSql()
    {
        return m_finalInsertSql;
    }

    public String getFinalUpdateSql()
    {
        return m_finalUpdateSql;
    }

    public long getFinalConnectID()
    {
        return m_finalConnectID;
    }

    public long getPreviewUrlID()
    {
        return m_previewUrlID;
    }

    public boolean getManualMode()
    {
        return m_manualMode;
    }

    public Vector getColumnList()
    {
        return m_columnList;
    }

    public void setID(long p_id)
    {
        m_id = p_id;
    }

    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setAcquisitionSql(String p_acquisitionSql)
    {
        m_acquisitionSql = p_acquisitionSql;
    }

    public void setAcquisitionContextSql(String p_acquisitionContextSql)
    {
        m_acquisitionContextSql = p_acquisitionContextSql;
    }

    public void setAcquisitionConnectID(long p_acquisitionConnectID)
    {
        m_acquisitionConnectID = p_acquisitionConnectID;
    }

    public void setPreviewInsertSql(String p_previewInsertSql)
    {
        m_previewInsertSql = p_previewInsertSql;
    }

    public void setPreviewUpdateSql(String p_previewUpdateSql)
    {
        m_previewUpdateSql = p_previewUpdateSql;
    }

    public void setPreviewConnectID(long p_previewConnectID)
    {
        m_previewConnectID = p_previewConnectID;
    }

    public void setFinalInsertSql(String p_finalInsertSql)
    {
        m_finalInsertSql = p_finalInsertSql;
    }

    public void setFinalUpdateSql(String p_finalUpdateSql)
    {
        m_finalUpdateSql = p_finalUpdateSql;
    }

    public void setFinalConnectID(long p_finalConnectID)
    {
        m_finalConnectID = p_finalConnectID;
    }

    public void setPreviewUrlID(long p_previewUrlID)
    {
        m_previewUrlID = p_previewUrlID;
    }

    public void setManualMode(boolean p_manualMode)
    {
        m_manualMode = p_manualMode;
    }

    public void setColumnList(Vector p_columnList)
    {
        m_columnList = p_columnList;
    }

    // ///////////////////////////////////////////////
    public void save()
    {
        Connection connection = null;
        String sql = "";

        try
        {
            connection = ConnectionPool.getConnection();

            // this whole thing is one big transaction
            boolean oldFlag = connection.getAutoCommit();
            connection.setAutoCommit(false);
            _deleteEntry(connection);

            if (0 == m_id)
            {
                // retrieve a new id
                sql = "SELECT " + INCREMENT_RECORD_PROFILE_ID
                        + ".NEXTVAL FROM DUAL";
                Statement query = connection.createStatement();
                ResultSet result = query.executeQuery(sql);
                result.next();
                m_id = result.getLong(1);
                query.close();
            }

            String acquisitionConnectID = "null";
            String previewConnectID = "null";
            String finalConnectID = "null";
            String previewUrlID = "null";
            String manualMode = "0";

            if (m_acquisitionConnectID > 0)
                acquisitionConnectID = Long.toString(m_acquisitionConnectID);
            if (m_previewConnectID > 0)
                previewConnectID = Long.toString(m_previewConnectID);
            if (m_finalConnectID > 0)
                finalConnectID = Long.toString(m_finalConnectID);
            if (m_previewUrlID > 0)
                previewUrlID = Long.toString(m_previewUrlID);

            if (m_manualMode)
                manualMode = "1";

            sql = "INSERT INTO " + RECORD_PROFILE + " ( " + RECORD_PROFILE_ID
                    + "," + NAME + "," + ACQUISITION_SQL + ","
                    + ACQUISITION_CONTEXT_SQL + "," + ACQUISITION_CONNECT_ID
                    + "," + PREVIEW_INSERT_SQL + "," + PREVIEW_UPDATE_SQL + ","
                    + PREVIEW_CONNECT_ID + "," + FINAL_INSERT_SQL + ","
                    + FINAL_UPDATE_SQL + "," + FINAL_CONNECT_ID + ","
                    + PREVIEW_URL_ID + "," + MANUAL_MODE + ") " + "VALUES ("
                    + m_id + "," + Utility.quote(m_name) + ", ?, ?, "
                    + acquisitionConnectID + ", ?, ?, " + previewConnectID
                    + ", ?, ?, " + finalConnectID + "," + previewUrlID + ","
                    + manualMode + ")";

            theLogger.println(Logger.DEBUG_D,
                    "DJ_RecordProfile: inserting new row with sql\n" + sql);

            PreparedStatement preparedStatement = connection
                    .prepareStatement(sql);
            preparedStatement.setString(1, m_acquisitionSql);
            preparedStatement.setString(2, m_acquisitionContextSql);
            preparedStatement.setString(3, m_previewInsertSql);
            preparedStatement.setString(4, m_previewUpdateSql);
            preparedStatement.setString(5, m_finalInsertSql);
            preparedStatement.setString(6, m_finalUpdateSql);

            preparedStatement.execute();

            for (int i = 0; i < m_columnList.size(); ++i)
            {
                DJ_ColumnProfile profile = (DJ_ColumnProfile) m_columnList
                        .elementAt(i);
                String ruleID = "null";
                if (profile.getRule() > 0)
                    ruleID = Long.toString(profile.getRule());

                sql = "INSERT INTO " + COLUMN_PROFILE + " VALUES(" + m_id + ","
                        + profile.getColumnNumber() + ","
                        + Utility.quote(profile.getTableName()) + ","
                        + profile.getDataType() + "," + ruleID + ","
                        + profile.getContentMode() + ","
                        + Utility.quote(profile.getLabel()) + ")";
                theLogger.println(Logger.DEBUG_D,
                        "DJ_RecordProfile: inserting columns with sql\n" + sql);
                connection.createStatement().executeUpdate(sql);
            }

            // now commit
            connection.commit();
            connection.setAutoCommit(oldFlag);

            if (preparedStatement != null)
            {
                preparedStatement.close();
            }
        }
        catch (ConnectionPoolException cpe)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_RecordProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_RecordProfile", e);
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
            theLogger.printStackTrace(Logger.ERROR, "DJ_RecordProfile", cpe);
        }
        catch (SQLException e)
        {
            theLogger.printStackTrace(Logger.ERROR, "DJ_RecordProfile", e);
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
            theLogger.println(Logger.DEBUG_D, "DJ_RecordProfile: deleting id "
                    + m_id);
            String sql = "DELETE FROM " + RECORD_PROFILE + " WHERE "
                    + RECORD_PROFILE_ID + " = " + m_id;
            connection.createStatement().executeUpdate(sql);
            connection.commit();
        }
    }

    // ///////////////////////////////////////////////
    static private Vector retrieveAllProfiles(String p_sql)
    {
        Connection connection = null;
        Vector records = new Vector();
        Logger theLogger = Logger.getLogger();

        try
        {
            connection = ConnectionPool.getConnection();
            Statement query = connection.createStatement();
            ResultSet results = query.executeQuery(p_sql);
            while (results.next())
            {
                long id = results.getLong(1);
                records.add(new DJ_RecordProfile(id));
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

        return records;
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveRecordProfiles()
    {
        return retrieveAllProfiles(ORDER_BY_ID);
    }

    // ///////////////////////////////////////////////
    static public Vector retrieveRecordProfilesByName()
    {
        return retrieveAllProfiles(ORDER_BY_NAME);
    }

    private Logger theLogger = Logger.getLogger();
}
