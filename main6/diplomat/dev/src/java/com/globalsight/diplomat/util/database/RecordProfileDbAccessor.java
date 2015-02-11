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

import com.globalsight.diplomat.util.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.HashMap;
import java.util.Vector;

/**
 * Concrete extension of the DbAccessor class.
 * <p>
 * Adds functionality to permit the access of record profiles from the
 * database.  Record profiles (and their associated column profiles are
 * assumed to reside in the diplomat table space, so all accesses are made
 * via the default connection profile.
 */
public class RecordProfileDbAccessor
    extends DbAccessor
{
    //
    // PRIVATE CONSTANTS
    //
    /* constants related to the customer db access table (formerly record profile) */
    private static String RECORD_PROFILE_SQL =
	"SELECT name, description, checkout_sql, checkout_connect_id, " +
	"preview_insert_sql, preview_update_sql, preview_connect_id, checkin_insert_sql, " +
	"checkin_update_sql, checkin_connect_id, preview_url_id, code_set, l10n_profile_id " +
	"FROM customer_db_access_profile WHERE id=?";
    private static final int RP_NAME = 1;
    private static final int RP_DESC = 2;
    private static final int RP_ACQUISITION_SQL = 3;
    private static final int RP_ACQUISITION_CONNECT_ID = 4;
    private static final int RP_PREVIEW_INSERT_SQL = 5;
    private static final int RP_PREVIEW_UPDATE_SQL = 6;
    private static final int RP_PREVIEW_CONNECT_ID = 7;
    private static final int RP_FINAL_INSERT_SQL = 8;
    private static final int RP_FINAL_UPDATE_SQL = 9;
    private static final int RP_FINAL_CONNECT_ID = 10;
    private static final int RP_PREVIEW_URL_ID = 11;
    private static final int RP_CODE_SET= 12;
    private static final int RP_L10NPROFILE_ID = 13;

    /* constants related to the customer_column_detail table (formerly column profile)  */
    private static String COLUMN_PROFILE_SQL = 
	"SELECT column_number, table_name, known_format_type, xml_rule_id, " + 
	"content_mode, column_label " +
	"FROM customer_column_detail " +
	"WHERE db_profile_id=";
    private static final int CP_COLUMN_NUMBER = 1;
    private static final String CP_COLUMN_NUMBER_NAME = "column_number";
    private static final int CP_TABLE_NAME = 2;
    private static final int CP_DATA_TYPE_ID = 3;
    private static final int CP_RULE_ID = 4;
    private static final int CP_CONTENT_MODE = 5;
    private static final int CP_COLUMN_LABEL = 6;

    /* constants related to known format type table */
    private static final String FORMAT_TYPE_TABLE = "KNOWN_FORMAT_TYPE";
    private static final String FT_ID = "ID";
    private static final String FT_FORMAT_TYPE = "FORMAT_TYPE";

    //
    // PRIVATE STATIC MEMBER VARIABLES
    //
    private static HashMap c_dataTypes = new HashMap();
    private static Logger c_logger = Logger.getLogger();

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
     * @throws SQLException if any failure occurs
     */
    public static RecordProfile readRecordProfile(long p_recordProfileId)
    throws DbAccessException
    {
        RecordProfile profile = null;
        try
        {
            Connection conn = getConnection();
            PreparedStatement st = conn.prepareStatement(RECORD_PROFILE_SQL);
            st.setLong(1,p_recordProfileId);
            ResultSet rs = st.executeQuery();
            if (rs.next())
            {
                profile = new RecordProfile();                   
                profile.setId(p_recordProfileId);
                profile.setName(rs.getString(RP_NAME));
                profile.setAcquisitionSql(readClob(rs, RP_ACQUISITION_SQL));
                profile.setAcquisitionContextSql("");
                profile.setAcquisitionConnectionId(rs.getLong(RP_ACQUISITION_CONNECT_ID));
                profile.setPreviewInsertSql(readClob(rs, RP_PREVIEW_INSERT_SQL));
                profile.setPreviewUpdateSql(readClob(rs, RP_PREVIEW_UPDATE_SQL));
                profile.setPreviewConnectionId(rs.getLong(RP_PREVIEW_CONNECT_ID));
                profile.setFinalInsertSql(readClob(rs, RP_FINAL_INSERT_SQL));
                profile.setFinalUpdateSql(readClob(rs, RP_FINAL_UPDATE_SQL));
                profile.setFinalConnectionId(rs.getLong(RP_FINAL_CONNECT_ID));
                profile.setPreviewUrlId(rs.getLong(RP_PREVIEW_URL_ID));
                profile.setManualMode(false); //all DB job creation is automatic in System4
                profile.setColumnProfiles(readColumnProfiles(conn, p_recordProfileId));
                c_logger.println(Logger.DEBUG_D, "RecordProfileDbAccessor, got profile: " + profile.detailString());
            }
            st.close();
            returnConnection(conn);
        }
        catch (Exception e)
        {
            throw new DbAccessException("Unable to load record profile with id=" +
                                        p_recordProfileId, e);
        }
        return profile;
    }

    //
    // PRIVATE STATIC SUPPORT METHODS
    //
    /* Return a vector containing all the column profiles associated with the */
    /* record profile with the given id. */
    private static Vector readColumnProfiles(Connection p_conn, long p_recordProfileId)
    throws SQLException
    {
        Vector v = new Vector();
        PreparedStatement st = p_conn.prepareStatement(columnProfileSelectSql());
        st.setLong(1,p_recordProfileId);
        ResultSet rs = st.executeQuery();
        while (rs.next())
        {
            ColumnProfile profile = new ColumnProfile();
            profile.setRecordProfileId(p_recordProfileId);
            profile.setColumnNumber(rs.getLong(CP_COLUMN_NUMBER));
            profile.setTableName(rs.getString(CP_TABLE_NAME));
            profile.setDataType(readDataType(p_conn, rs.getLong(CP_DATA_TYPE_ID)));
            profile.setRuleId(rs.getLong(CP_RULE_ID));
            profile.setContentMode(rs.getInt(CP_CONTENT_MODE));
            profile.setLabel(rs.getString(CP_COLUMN_LABEL));
            v.addElement(profile);
        }
        st.close();
        return v;
    }

    /* Return a string representing the data type associated with the current */
    /* column in the record profile.  If the type is in the hashmap, return */
    /* it; otherwise, read the type from the database. */
    private static String readDataType(Connection p_conn, long p_dataTypeId)
    throws SQLException
    {
        Long id = new Long(p_dataTypeId);
        String dataType = (String)c_dataTypes.get(id);
        if (dataType == null)
        {
            PreparedStatement st = p_conn.prepareStatement(formatTypeSelectSql());
            st.setLong(1,p_dataTypeId);
            ResultSet rs = st.executeQuery();
            if (rs.next())
            {
                dataType = rs.getString(FT_FORMAT_TYPE);
                c_dataTypes.put(id, dataType);
            }
            st.close();
        }
        return dataType;
    }

    /* Generic select sql generator */
    private static String selectSql(String p_idName, String p_idValue, String p_table)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ");
        sb.append(p_table);
        sb.append(" WHERE ");
        sb.append(p_idName);
        sb.append(" = ");
        sb.append(p_idValue);
        return sb.toString();
    }

    /* Construct the sql to read from the known format type table. */
    private static String formatTypeSelectSql()
    {
        return selectSql(FT_ID, "?", FORMAT_TYPE_TABLE);
    }

    /* Construct the sql to read from the column profile table.*/
    private static String columnProfileSelectSql()
    {
	String sql = COLUMN_PROFILE_SQL + "?" + " ORDER BY "
	    + CP_COLUMN_NUMBER_NAME;
	return sql;
    }
}

