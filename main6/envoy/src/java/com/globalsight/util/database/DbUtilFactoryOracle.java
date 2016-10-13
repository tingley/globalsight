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

/**
 * This factory class creates Oracle RDBMS specific database
 * utilities such as database connector (that creates database
 * connections).
 * 
 * @version     1.0, (12/6/99 1:58:57 PM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         12/06/1999   Initial version.
 */

public class DbUtilFactoryOracle extends DbUtilFactory
{
/**
 * DbUtilFactoryOracle constructor comment.
 */
DbUtilFactoryOracle() {
	super();
}
/**
 * Create a vendor specific database connector using the specified parameters
 * and the default JDBC driver.
 * 
 * @return Vendor specific database connector
 * @param p_url URL for the database to connect to.
 * @param p_username User name of account to connect to.
 * @param p_password Password of account to connect to.
 * @exception com.gloalsight.util.database.FailedToLoadDriverException Failed to load the JDBC driver required for by the connector.
 */
public DbConnector makeConnector(String p_url, String p_username, String p_password) throws FailedToLoadDriverException
{
	return new DbConnectorOracle(p_url, p_username, p_password);
}
/**
 * Create a vendor specific database connector using the specified parameters.
 * 
 * @return Vendor specific database connector
 * @param p_driver JDBC driver class name.
 * @param p_url URL for the database to connect to.
 * @param p_username User name of account to connect to.
 * @param p_password Password of account to connect to.
 * @exception com.gloalsight.util.database.FailedToLoadDriverException Failed to load the JDBC driver required for by the connector.
 */
public DbConnector makeConnector(String p_driver, String p_url, String p_username, String p_password) throws FailedToLoadDriverException
{
	return new DbConnectorOracle(p_driver, p_url, p_username, p_password);
}
/**
 * Make a new sequence generator using the provided connector.
 * 
 * @return A new sequence generator.
 * @param p_connector Database connector.
 */
public SequenceGenerator makeSequenceGenerator(DbConnector p_connector)
{
	return new SequenceGeneratorOracle((DbConnectorOracle)p_connector);
}
}
