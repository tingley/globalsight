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

// hacked code
public class DbUtilFactoryDb2 extends DbUtilFactory
{
/**
 * DbUtilFactoryDb2 constructor comment.
 */
DbUtilFactoryDb2() {
	super();
}
/**
 * makeConnector method comment.
 */
public DbConnector makeConnector(String p_url, String p_username, String p_password) throws FailedToLoadDriverException {
	return new DbConnectorDb2(p_url, p_username, p_password);
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
	return new DbConnectorDb2(p_driver, p_url, p_username, p_password);
}
/**
 * makeSequenceGenerator method comment.
 */
public SequenceGenerator makeSequenceGenerator(DbConnector p_connector)
{
	return new SequenceGeneratorDb2((DbConnectorDb2)p_connector);
}
}
