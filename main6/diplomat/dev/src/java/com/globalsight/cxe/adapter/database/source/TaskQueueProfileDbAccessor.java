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

import com.globalsight.diplomat.util.database.DbAccessException;
import com.globalsight.diplomat.util.database.DbAccessor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.Vector;

/**
 * Concrete extension of the DbAccessir class.
 * <p>
 * Adds functionality to permit the loading of task queue profiles from the
 * database.  Task queue profiles are assumed to reside in the diplomat table
 * space, so all accesses are made via the default connection profile.
 */
public class TaskQueueProfileDbAccessor
    extends DbAccessor
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String TABLE_NAME = "DB_DISPATCH_PROFILE";
    private static final String TQP_ID = "ID";
    private static final String TQP_NAME = "NAME";
    private static final String TQP_TABLE_NAME = "TABLE_NAME";
    private static final String TQP_CONNECT_ID = "CONNECTION_ID";

    private static final String TQP_RPP = "RECORDS_PER_PAGE";
    private static final String TQP_PPB = "PAGES_PER_BATCH";
    private static final String TQP_ELAPSED = "MAX_ELAPSED_MILLIS";
    private static final String BASE_SQL = "SELECT * FROM " + TABLE_NAME;
    private static final String WHERE_CLAUSE = " WHERE " + TQP_ID + " =?";

    //
    // PUBLIC STATIC METHODS
    //
    /**
     * Read the record profile with the given id from the database.
     * Load all of its related column profiles at the same time.
     *
     * @param p_recordProfileId the id of the profile to load.
     *
     * @return the record profile.
     *
     * @throws DbAccessException any database access error occurs.
     */
    public static TaskQueueProfile readTaskQueueProfile(long p_profileId)
    throws DbAccessException
    {
        TaskQueueProfile profile = null;
        try
        {
            Connection conn = getConnection();
            String sql = BASE_SQL + WHERE_CLAUSE;
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setLong(1,p_profileId);
            ResultSet rs = pst.executeQuery();
            if (rs.next())
            {
                profile = profileFromResultSet(rs);
            }
            pst.close();
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to load task queue profile, id=" +
                                        p_profileId, e);
        }
        return profile;
    }

    /**
     * Read all task queue profiles from the database, and return them in a
     * (possibly empty) vector.
     *
     * @return a vector containing all profiles.
     *
    * @throws DbAccessException any database access error occurs.
     */
    public static Vector readAllTaskQueueProfiles()
    throws DbAccessException
    {
        Vector v = new Vector();
        try
        {
            Connection conn = getConnection();
            PreparedStatement pst = conn.prepareStatement(BASE_SQL);
            ResultSet rs = pst.executeQuery();
            while (rs.next())
            {
                v.addElement(profileFromResultSet(rs));
            }
            pst.close();
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to load task queue profiles", e);
        }
        return v;
    }

    //
    // PRIVATE STATIC SUPPORT METHODS
    //
    /* Return a task queue profile based on the contents of the result set. */
    private static TaskQueueProfile profileFromResultSet(ResultSet p_rs)
    throws SQLException
    {
        TaskQueueProfile p = new TaskQueueProfile();                   
        p.setId(p_rs.getLong(TQP_ID));
        p.setName(p_rs.getString(TQP_NAME));
        p.setTableName(p_rs.getString(TQP_TABLE_NAME));
        p.setConnectionId(p_rs.getLong(TQP_CONNECT_ID));
        p.setRecordsPerPage(p_rs.getLong(TQP_RPP));
        p.setPagesPerBatch(p_rs.getLong(TQP_PPB));
        p.setMaxElapsedMillis(p_rs.getLong(TQP_ELAPSED));
        return p;
    }
}

