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
package com.globalsight.cxe.adapter.database.source;

import com.globalsight.diplomat.util.Logger;

import com.globalsight.diplomat.util.database.DbAccessor;
import com.globalsight.diplomat.util.database.DbAccessException;
import com.globalsight.diplomat.util.database.RecordProfile;
import com.globalsight.diplomat.util.database.RecordProfileDbAccessor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Vector;

/**
 * Concrete extension of the DbAccessor class.
 * <p>
 * Adds functionality to load all tasks currently in a task queue table, and
 * to delete a specific subset of records from the table when required.
 * In order to load tasks, the task accessor needs to know the name of the task
 * queue table as well as the connection profile it should use to connect.
 * Both of these pieces of information are stored on a TaskProfile which is
 * passed in as an argument.
 */
public class TaskDbAccessor
    extends DbAccessor
{
    //
    // PRIVATE CONSTANTS
    //

    //the SQL statement is generic for all possible "taskqueue" type tables
    private static final String TASKQUEUE_SQL =
	"SELECT task_queue_id, record_profile_id, substitution_parameters FROM ";
    //indices into the result set
    private static final int TQ_ID = 1;
    private static final int TQ_REC_PROF_ID = 2;
    private static final int TQ_PARMS = 3;

    //This SQL statement gets the L10nProfile, and locale
    private static final String LOCALIZATION_SQL =
	"SELECT LP.ID, L.ISO_LANG_CODE, L.ISO_COUNTRY_CODE, CDAP.CODE_SET " +
	"FROM LOCALE L, L10N_PROFILE LP, CUSTOMER_DB_ACCESS_PROFILE CDAP " +
	"WHERE CDAP.ID=? AND LP.SOURCE_LOCALE_ID=L.ID and CDAP.L10N_PROFILE_ID=LP.ID";

    //Indices into the result set from the LOCALIZATION_SQL query
    private static final int L10N_PROFILE_ID = 1;
    private static final int LANG_CODE = 2;
    private static final int COUNTRY_CODE = 3;
    private static final int CODE_SET = 4;



    //
    //  PRIVATE STATIC MEMBER VARIABLES
    //
    private static HashMap s_recProfs;
    private static Logger s_logger = Logger.getLogger();

    //
    // PUBLIC STATIC METHODS
    //
    /**
     * Return a vector containing all the tasks currently in the task queue
     * table specified by the given task queue profile.
     *
     * @param p_profile the task queue profile to use.
     *
     * @return the existing records
     *
     * @throws DbAccessException if any data access fails.
     */
    public static Vector readTasks(TaskQueueProfile p_profile)
    throws DbAccessException
    {
        s_recProfs = new HashMap();
        Vector v = new Vector();

        try
        {
            Connection conn = getConnection(p_profile.getConnectionId());
            String tableName = p_profile.getTableName();
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            markRecords(conn, tableName);
            v = getRecords(conn, tableName);
            conn.setAutoCommit(autoCommit);
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to load tasks for profile id=" + p_profile, e);
        }
        return v;
    }

    /**
     * Remove all "LOADED" tasks from the table identified by the given task
     * queue profile.
     *
     * @param p_profile the task queue profile containing table name and
     * database connection id.
     *
     * @throws DbAccessException if any failure occurs.
     */
    public static void removeTasks(TaskQueueProfile p_profile)
    throws DbAccessException
    {
        try
        {
            Connection conn = getConnection(p_profile.getConnectionId());
            PreparedStatement st = conn.prepareStatement(deleteSql(p_profile.getTableName()));
            st.executeUpdate();
            st.close();
            conn.commit();
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to delete tasks, " +
                                        "profileId=" + p_profile.getId() + 
                                        ", table=" + p_profile.getTableName(), e);
        }
    }

    //
    // PRIVATE SUPPORT METHODS
    //
    /* Construct appropriate select sql for the given table name */
    private static String selectSql(String p_tableName)
    {
        String sql = TASKQUEUE_SQL + p_tableName + 
            " WHERE LOADED = 1 ORDER BY " + TQ_ID;
        log("selectSql", sql);
        return sql;
    }

    /* Construct appropriate update sql for the given table name */
    private static String markSql(String p_tableName)
    {
        String sql = "UPDATE " + p_tableName + " SET LOADED = 1";
        log("markSql", sql);
        return sql;
    }

    /* Construct appropriate delete sql for the given table name */
    private static String deleteSql(String p_tableName)
    {
        String sql = "DELETE FROM " + p_tableName + " WHERE LOADED = 1";
        log("deleteSql", sql);
        return sql;
    }

    /* Mark all the current records in the task queue as having been loaded. */
    private static void markRecords(Connection p_conn, String p_tableName)
    throws SQLException
    {
        log("markRecords", "marking table " + p_tableName);
        PreparedStatement st = p_conn.prepareStatement(markSql(p_tableName));
        st.executeUpdate();
        p_conn.commit();
        st.close();
    }

    /* Retrieve all the records in the task queue that are marked as loaded. */
    private static Vector getRecords(Connection p_conn, String p_tableName)
    throws DbAccessException, SQLException
    {
        log("getRecords", "selecting from table " + p_tableName);
        Vector v = new Vector();
        PreparedStatement st = p_conn.prepareStatement(selectSql(p_tableName));
        ResultSet rs = st.executeQuery();
        while (rs.next())
        {
            Task t = new Task();
            t.setTaskId(rs.getLong(TQ_ID));
            t.setRecordProfileId(rs.getLong(TQ_REC_PROF_ID));
	    readLanguageCharset(t);
            readManualMode(t);
            // uncomment the following line if the SUBSTITUTION_PARAMETERS
            // field is reverted back to a clob field.
            // t.setParameterString(readClob(rs, TQ_PARMS));
            t.setParameterString(rs.getString(TQ_PARMS));
            v.addElement(t);
        }
        rs.close();
        st.close();
        return v;
    }

    /* Read the language and character set for the localization profile on the */
    /* given task */
    private static void readLanguageCharset(Task p_task)
            throws DbAccessException
    {
        try
        {
            Connection conn = getConnection();
            PreparedStatement ps = conn.prepareStatement(LOCALIZATION_SQL);
            ps.setLong(1, p_task.getRecordProfileId());
            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                //the database now stores country and lang separately
                //but System3 stored them together like "en_US"
                String sourceLanguage = rs.getString(LANG_CODE) + "_"
                        + rs.getString(COUNTRY_CODE);
                p_task.setSourceLanguage(sourceLanguage);
                p_task.setCharset(rs.getString(CODE_SET));
                p_task.setLocalizationProfileId(rs.getLong(L10N_PROFILE_ID));
            }
            else
            {
                throw new DbAccessException(
                        "No language/charset data found for "
                                + "customer db access profile id "
                                + p_task.getRecordProfileId(), null);
            }
            rs.close();
            ps.close();
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Error executing LOCALIZATION_SQL", e);
        }
    }

    /* Read the record profile and pull the manual mode flag from it. */
    private static void readManualMode(Task p_task)
    throws DbAccessException
    {
        Long id = new Long(p_task.getRecordProfileId());
        RecordProfile rp = (RecordProfile)s_recProfs.get(id);
        if (rp == null)
        {
            try
            {
                rp = RecordProfileDbAccessor.readRecordProfile(id.longValue());
                s_recProfs.put(id, rp);
            }
            catch (Exception e)
            {
                throw new DbAccessException("Error reading record profile table", e);
            }
        }
        p_task.setManualMode(rp.isManualMode());
    }

    /* Log the given debug message. */
    private static void log(String p_method, String p_msg)
    {
        s_logger.println(Logger.DEBUG_D, "TaskDbAccessor." + p_method + "(): " + p_msg);
    }
}
