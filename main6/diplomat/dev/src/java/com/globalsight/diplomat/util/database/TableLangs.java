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
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;


public class TableLangs
{
	// SQL string to query for a user by user name
	static private final String QUERY_STATEMENT_STRING = 
		"SELECT ID FROM LOCALE " + 
		"WHERE ISO_LANG_CODE = ? AND ISO_COUNTRY_CODE = ?";

	// name of the data base columns that make up a LOCALE row
	static private final String ISO_LANGUAGE_CODE = "ISO_LANGUAGE_CODE";
	static private final String ID = "ID";
	static private final String ISO_COUNTRY_CODE = "TARGET_LOCALE_ID";

	// LOCALE variables:
	// The primary key for a record.
	private int localeID = -1;          
	// The language code.
	private String isoLanguageCode = "";
	// The country code.
	private String isoCountryCode = "";       

	/**
	 * Default constructor
	 */
	public TableLangs()
	{
	}

	/**
	 * Accessor for the primary key
	 */
	public int getID()
	{
		return localeID;
	}

	/**
	 * Accessor for the country code
	 */
	public String getIsoCountryCode()
	{
		return isoCountryCode;
	}

	/**
	 * Accessor for the language code
	 */
	public String getIsoLanguageCode()
	{
		return isoLanguageCode;
	}

	/**
	 * Attempts to populate this User object from the next record of a
	 * ResultSet.
	 *
	 * @param resultSet a result set that contains a series of rows
	 * from the LOCALE table
	 *
	 * @return a TableLangs object
	 */
	static public TableLangs createFromDB(ResultSet resultSet)
	{
		TableLangs result = new TableLangs();

		try
		{
			String strID = resultSet.getString(ID);
			Integer tmp = new Integer(strID);
			result.localeID = tmp.intValue();
		}
		catch (SQLException ex)
		{
			// return null since the read form the data base failed
			return null;
		}

		return result;
	}

	/**
	 * Find a LOCALE record given its isoLanguageCode and isoCountryCode
	 *
	 * @param isoLanguageCodeStr
	 * @param isoCountryCodeStr
	 * @return returns a TableLangs object with the information
	 */
	static public TableLangs findLanguageByLocale(String isoLanguageCodeStr,
		String isoCountryCodeStr)
	{
		// obtain a data base connection
		Connection conn = null;

		try
		{
			conn = ConnectionPool.getConnection();
		}
		catch (Exception exc)
		{
		}

		if (conn == null)
		{
			return null;
		}

		TableLangs result = null;
		PreparedStatement stmt = null;
                ResultSet rs = null;

		try
		{
			stmt = conn.prepareStatement(QUERY_STATEMENT_STRING);
			stmt.setString(1, isoLanguageCodeStr);
			stmt.setString(2, isoCountryCodeStr);

			rs = stmt.executeQuery();

			if (!rs.next())
			{
				//there is no user record
				return null;
			}

			result = TableLangs.createFromDB(rs);
		}
		catch (SQLException ex)
		{
		}
		finally
		{
                    ConnectionPool.silentClose(rs);
                    ConnectionPool.silentClose(stmt);
                    ConnectionPool.silentReturnConnection(conn);
		}

		return result;
	}
}

