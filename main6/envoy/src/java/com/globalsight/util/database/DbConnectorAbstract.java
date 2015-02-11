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

// Import Core Java classes
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.globalsight.util.GeneralException;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import com.globalsight.util.j2ee.AppServerWrapper;


/**
 * Wrapper class for a JDBC database connection.  Handles operations
 * such as connecting to the JDBC driver and the database, and
 * recovering from connection errors.
 * 
 * @version     2.0
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         05/12/1999   Ininitial version.
 * mlau         06/25/1999   Add check for connection status
 *                           before giving the connection to client
 * mlau         12/03/1999   Delegate getConnection() to vendor
 *                           specific class.
 * tdoomany     07/26/2000   Added support for weblogic's connection pooling (in makeNewConnection() method)
 */

public abstract class DbConnectorAbstract implements DbConnector
{
	Connection  m_dbConnection;
	String      m_url;      // Database location URL
	String      m_username; // Username of account to connect to database
	String      m_password; // Password of account to connect to database
        
/**
 * Store provided parameter as state.
 * 
 * @param p_url Pointer to the database.
 * @param p_username Username of account to access the database.
 * @param p_password Password of account to access the database.
 */
DbConnectorAbstract(String p_url, String p_username, String p_password)
{
	m_url = p_url;
	m_username = p_username;
	m_password = p_password;
}
/**
 * Close the specified database connection.  If the specified connection is
 * the same as the one currently in this instance, the current connection is
 * automatically released throught a call to renewConnection().
 * <p>
 * Note that this method does not check whether the specified database
 * connection is in use by another thread.
 *
 * @return java.sql.Connection Connection object for the database.
 * @exception java.sql.SQLException JDBC exception.
 */
public void closeConnection(Connection p_connection) throws SQLException
{
	if (m_dbConnection == p_connection)
	{
		m_dbConnection = null;
	}
	p_connection.close();
}
/**
 * Get a Connection object for communicating with the database.
 *
 * @return java.sql.Connection Connection object for the database.
 */
public Connection getConnection() throws SQLException
{
	boolean needToReconnect = false;
	if (m_dbConnection == null)
	{
		needToReconnect = true;
	}
	else
	{
		try
		{
			if (m_dbConnection.isClosed())
			{
				needToReconnect = true;
			}
		}
		catch(SQLException sqle)
		{
			needToReconnect = true;
		}
	}
	if (needToReconnect)
	{
		m_dbConnection = makeNewConnection();
	}
	return m_dbConnection;
}
/**
 * Make a new connection to the database.
 * 
 * @return New connection object.
 * @exception java.sql.SQLException Any database exception while attempting to connect.
 */
Connection makeNewConnection() throws java.sql.SQLException
{        
    Connection conn = null;
    Context ctx = null;    
    
    try 
    {                
        ctx = AppServerWrapperFactory.getAppServerWrapper().getNamingContext();
        javax.sql.DataSource ds = (javax.sql.DataSource) ctx.lookup ("myNonJtsDataSource");
        conn = ds.getConnection();
        
    }
    catch (NamingException e) 
    {
        //TomyD - temp solution - wrap the exception in the sql exception to avoid adding a catch
        // to all the code that's calling this part
        throw new SQLException(e.getMessage());
    }
    
    return conn;
}
/**
 * Get a new connection next time.  This method only affect future invokation
 * of methods that depend on the database connection.  Existing connections
 * that other clients have obtained in previous calls to getConnection()
 * will still exist and remain unchanged.
 */
public void renewConnection()
{
	// Set current object (handle) to null so that if any method is still
	// using the connection, it is still available to that method.
	// Next time someone ask for a connection, a new one will be created.
	m_dbConnection = null;
}
}
