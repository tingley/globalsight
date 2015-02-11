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

public class TableBranchLanguage
{
	// SQL string to query for a user by user name
	static private final String QUERY_STATEMENT_STRING = 
		"SELECT SOURCE_BRANCH, TARGET_BRANCH FROM TEAMSITE_BRANCH_LANGUAGE " + 
		"WHERE SOURCE_BRANCH = ? AND TARGET_LOCALE_ID = ? " +
                "AND TEAMSITE_SERVER_ID = ? AND TEAMSITE_STORE_ID = ?";

	// name of the data base columns that make up a TableBranchLanguage row
	static private final String SOURCE_BRANCH = "SOURCE_BRANCH";
	static private final String TARGET_BRANCH = "TARGET_BRANCH";
	static private final String TARGET_LOCALE_ID = "TARGET_LOCALE_ID";
	static private final String TEAMSITE_SERVER_ID = "TEAMSITE_SERVER_ID";
	static private final String TEAMSITE_STORE_ID = "TEAMSITE_STORE_ID";

	// TableBranchLanguage variables

	// The branch where the source file was stored.
	private String sourceBranch = "";
	// The branch where the target file was stored.
	private String targetBranch = "";
	// The ID of the language of the target file.
	private int targetLanguage = -1;      
	// The ID of TeamSite Server
	private int serverId = -1;      
	// The ID of TeamSite Store
	private int storeId= -1;      

	/**
	 * Default constructor
	 */
	public TableBranchLanguage()
	{
	}

	/**
	 * Accessor for the target language branch.
	 */
	public String getTargetBranch()
	{
		return targetBranch;
	}

	/**
	 * Accessor for the source language branch.
	 */
	public String getSourceBranch()
	{
		return sourceBranch;
	}

	/**
	 * Accessor for the TeamSite Server Id
	 */
	public int getTeamSiteServerId()
	{
		return serverId;
	}


	/**
	 * Accessor for the TeamSite Store Id
	 */
	public int getTeamSiteStoreId()
	{
		return storeId;
	}

	/**
	 * Attempts to populate this User object from the next record of a
	 * ResultSet.
	 *
	 * @param resultSet a result set that contains a series of rows
	 * from the BRANCH_LANGUAGE table
	 *
	 * @return a TableBranchLanguage object
	 */
	static public TableBranchLanguage createFromDB(ResultSet resultSet)
	{
		TableBranchLanguage result = new TableBranchLanguage();

		try
		{
			result.sourceBranch = resultSet.getString(SOURCE_BRANCH);
			result.targetBranch = resultSet.getString(TARGET_BRANCH);

			if (result.sourceBranch.equals(result.targetBranch)) 
			{
				return null;
			}
		}
		catch (SQLException exc)
		{
			// return null since the read from the data base failed
			return null;
		}

		return result;
	}

	/**
	 * Find a BranchLanguage record given its target language ID.
	 *
	 * @param targetLanguage ID
	 *
	 * @return returns a TableBranchLanguage object with the information
	 */
	static public TableBranchLanguage findBranchLanguage(
		String sourceBranchStr, int targetLanguageID,
                int serverID, int storeID)
	{
		// obtain a data base connection
		Connection conn = null;

		try
		{
			conn = ConnectionPool.getConnection();
		}
		catch (Exception ignore)
		{
		}

		if (conn == null)
		{
			return null;
		}

		TableBranchLanguage result = null;
		PreparedStatement stmt = null;
                ResultSet rs = null;

		try
		{
                    stmt = conn.prepareStatement(QUERY_STATEMENT_STRING);
                    boolean branchFound = false;
                    while( sourceBranchStr != null)
                    {
			stmt.setString(1, sourceBranchStr);
			stmt.setInt(2, targetLanguageID);
			stmt.setInt(3, serverID);
			stmt.setInt(4, storeID);

			rs = stmt.executeQuery();

			if (!rs.next())
			{
                            sourceBranchStr = nextBranchPath(sourceBranchStr);
			}
                        else
                        {
                            branchFound = true;
                            break;
                        }
                    }
                    if(!branchFound)
                    {
                        //there is no branch mapping record
                        return null;
                    }
                    result = TableBranchLanguage.createFromDB(rs);
		}
		catch (SQLException ignore)
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

        static public String nextBranchPath(String st)
        {
            if(st.lastIndexOf("/") > -1)
            {
                return st.substring(0, st.lastIndexOf("/"));
            }
            return null;
        }
}
