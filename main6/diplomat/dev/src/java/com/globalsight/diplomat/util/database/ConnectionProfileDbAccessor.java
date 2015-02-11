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
package com.globalsight.diplomat.util.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.globalsight.ling.tm2.persistence.DbUtil;

/**
 * Responsible for accessing connection profiles from the database.  Provides
 * static access only.
 */
public class ConnectionProfileDbAccessor
    extends DbAccessor
{
    //
    // PRIVATE CONSTANTS
    //
    private static final String CONNECTION_PROFILE_TABLE = "CONNECTION_PROFILE";
    private static final String CON_ID = "ID";
    private static final String NAME = "NAME";
    private static final String DRIVER = "DRIVER";
    private static final String CONNECTION_STRING = "CONNECTION";
    private static final String USER_NAME = "USER_NAME";
    private static final String PASSWORD = "PASSWORD";
    private static final String SELECT_SQL =
        "SELECT * FROM " + CONNECTION_PROFILE_TABLE +
        " WHERE " + CON_ID + " =?";

    //
    // PUBLIC STATIC METHODS
    //
    /**
     * Read the connection profile with the given id from the database.
     * Load all of its related column profiles at the same time.
     *
     * @return the connection profile.
     *
     * @throws DbAccessException if any error occurs
     */
    public static ConnectionProfile readConnectionProfile(long p_id)
    throws DbAccessException
    {
        ConnectionProfile profile = new ConnectionProfile();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try
        {
            conn = getConnection();
            st = conn.prepareStatement(SELECT_SQL);
            st.setLong(1,p_id);
            rs = st.executeQuery();
            if (rs.next())
            {
                profile.setId(rs.getLong(CON_ID));
                profile.setProfileName(rs.getString(NAME));
                profile.setDriver(rs.getString(DRIVER));
                profile.setConnectionString(rs.getString(CONNECTION_STRING));
                profile.setUserName(rs.getString(USER_NAME));
                profile.setPassword(rs.getString(PASSWORD));
            }
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to read connection profile with id=" + p_id, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(st);
            DbUtil.silentReturnConnection(conn);
        }

        return profile;
    }
}