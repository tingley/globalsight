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
package com.globalsight.util.database;

/* Copyright (c) 1999, Global Sight Corporation.  All rights reserved. */

// Core Java classes
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sequence number generator that uses Oracle RDBMS specific
 * facility.
 * 
 * @version     1.0, (12/6/99 11:42:09 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         12/06/1999   Initial version.
 */

public class SequenceGeneratorOracle implements SequenceGenerator
{
	DbConnector m_connector; // Connector to the database
/**
 * Construct an instance that uses the provided database connector.
 *
 * @param p_connector Connector to the database that provides the
 *                    required sequence number generation facility.
 */
public SequenceGeneratorOracle(DbConnectorOracle p_connector)
{
	super();
	m_connector = p_connector;
}
/**
 * Create a new sequence.
 * 
 * @param p_sequenceName The name of the new sequence.
 * @exception java.sql.SQLException Failed to create the new sequence in the database.
 */
public void createSequence(String p_sequenceName) throws SQLException
{
	Statement stmt = null;
	try
	{
		Connection dbConn = m_connector.getConnection();
		stmt = dbConn.createStatement();
		stmt.execute("CREATE SEQUENCE " + p_sequenceName);
	}
	catch (SQLException se)
	{
		m_connector.renewConnection();
		throw se;
	}
	finally
	{
		// Clean up database objects
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
		catch (SQLException se1)
		{
			se1.printStackTrace();
		}
	}
}
/**
 * Delete an existing sequence.  Once deleted, the sequence will not be
 * available.  A new sequence created using the same name will start from
 * 1 again.
 * 
 * @param p_sequenceName The name of the sequence to delete.
 * @exception java.sql.SQLException Failed to delete sequence in the database.
 */
public void deleteSequence(String p_sequenceName) throws SQLException
{
	Statement stmt = null;
	try
	{
		Connection dbConn = m_connector.getConnection();
		stmt = dbConn.createStatement();
		stmt.execute("DROP SEQUENCE " + p_sequenceName);
	}
	catch (SQLException se)
	{
		m_connector.renewConnection();
		throw se;
	}
	finally
	{
		// Clean up database objects
		try
		{
			if (stmt != null)
			{
				stmt.close();
			}
		}
		catch (SQLException se1)
		{
			se1.printStackTrace();
		}
	}
}
/**
 * Get the next value in the specified sequence from the database
 * server.
 * 
 * @return The next value in the specified sequence.
 * @param p_sequenceName Name of the sequence to get a value for.
 * @exception java.sql.SQLException Database related exception.
 */
public int getNextValue(String p_sequenceName) throws java.sql.SQLException
{
	Statement stmt = null;
	ResultSet result = null;
	try
	{
		Connection dbConn = m_connector.getConnection();
		stmt = dbConn.createStatement();
		result = stmt.executeQuery("SELECT " + p_sequenceName + ".NEXTVAL FROM DUAL");
		result.next();
		return result.getInt(1);
	}
	catch (SQLException se)
	{
		m_connector.renewConnection();
		throw se;
	}
	finally
	{
		// Clean up database objects
		try
		{
			if (result != null)
			{
				result.close();
			}
			if (stmt != null)
			{
				stmt.close();
			}
		}
		catch (SQLException se1)
		{
			se1.printStackTrace();
		}
	}
}
}
