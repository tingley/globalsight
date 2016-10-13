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
import java.sql.SQLException;

/**
 * This interface defines the methods that vendor specific database
 * connection utilities must implement.
 * 
 * @version     1.0, (12/3/99 1:35:10 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         11/30/1999   Initial version.
 */

public interface DbConnector
{
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
public void closeConnection(Connection p_connection) throws SQLException;
/**
 * Get a Connection object for communicating with the database.
 *
 * @return java.sql.Connection Connection object for the database.
 * @exception java.sql.SQLException JDBC exception.
 */
public Connection getConnection() throws SQLException;
/**
 * Get a new connection next time.  This method only affect future invokation
 * of methods that depend on the database connection.  Existing connections
 * that other clients have obtained in previous calls to getConnection()
 * will still exist and remain unchanged.
 */
public void renewConnection();
}
