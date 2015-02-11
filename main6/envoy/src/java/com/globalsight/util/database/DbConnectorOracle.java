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
//import java.sql.DriverManager;
//import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles Oracle specific operations as required by the abstract
 * super class that handles generic operations for making connections
 * to databases.
 * 
 * @version     1.0
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         12/06/1999   Ininitial version.
 */

public class DbConnectorOracle extends DbConnectorAbstract
{
    //TomyD --  replaced the oracle driver with the weblogic's.
	static final String DEFAULT_DRIVER = "weblogic.jdbc.oci.Driver";//"oracle.jdbc.driver.OracleDriver";
/**
 * Constructor that loads the default JDBC driver.
 *
 * @param p_url The URL that for the database to connect to.
 * @param p_username The username of an account that can access the database.
 * @param p_password The password for the above account.
 * @exception FailedToLoadDriverException Failed to load JDBC driver.
 */
public DbConnectorOracle(String p_url, String p_username, String p_password) throws FailedToLoadDriverException
{
	super(p_url, p_username, p_password);
	initConnector(DEFAULT_DRIVER);
}
/**
 * Constructor that loads a specific driver.
 *
 * @param p_driver The fully qualified driver class name (e.g. "oracle.jdbc.driver.OracleDriver").
 * @param p_url The URL that for the database to connect to.
 * @param p_username The username of an account that can access the database.
 * @param p_password The password for the above account.
 * @exception FailedToLoadDriverException Failed to load JDBC driver.
 */
public DbConnectorOracle(String p_driver, String p_url, String p_username, String p_password) throws FailedToLoadDriverException
{
	super(p_url, p_username, p_password);
	initConnector(p_driver);
}
/**
 * Initialize a new instance.
 *
 * @param p_driver The JDBC driver for this connector.
 * @exception ClassNotFoundException The JDBC driver class is not available.
 */
void initConnector(String p_driver) throws FailedToLoadDriverException
{
	try
	{
		Class.forName(p_driver); // Load the JDBC driver
	}
	catch (ClassNotFoundException cnfe)
	{
		throw new FailedToLoadDriverException(cnfe.getLocalizedMessage());
	}
}
}
