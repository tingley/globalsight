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
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.ClassNotFoundException;

/**
 * This is a factory class that creates vendor specific database
 * utilities such as database connector (that creates database
 * connections).
 * 
 * @version     1.0, (12/3/99 2:00:38 AM)
 * @author      Marvin Lau, mlau@globalsight.com
 */

/*
 * MODIFIED     MM/DD/YYYY
 * mlau         11/30/1999   Initial version.
 */

public abstract class DbUtilFactory
{
	static final String CLASS_PREFIX = "com.globalsight.util.database.DbUtilFactory";
/**
 * DbUtilFactory constructor comment.
 */
DbUtilFactory() {
	super();
}
/**
 * Create an instance of a concrete sub class, given the type of
 * the database (which is used as part of the name of the factory).
 * 
 * @return An instance of a concrete sub class.
 * @param p_dbType Database type.
 * @exception com.globalsight.util.database.FailedToCreateFactoryException Failed to create factory.
 */
public static DbUtilFactory getInstance(String p_dbType) throws FailedToCreateFactoryException
{
	try
	{
		Class factoryClass = Class.forName(CLASS_PREFIX + p_dbType);
		return (DbUtilFactory) factoryClass.newInstance();
	}
	catch (ClassNotFoundException cnfe)
	{
		throw new FailedToCreateFactoryException(cnfe.getLocalizedMessage());
	}
	catch (IllegalAccessException iae)
	{
		throw new FailedToCreateFactoryException(iae.getLocalizedMessage());
	}
	catch (InstantiationException ie)
	{
		throw new FailedToCreateFactoryException(ie.getLocalizedMessage());
	}
}
/**
 * Create an instance of a concrete sub class, given the URL of
 * the database.  The database type is automatically detected by
 * looking at the protocol part of the URL.
 * 
 * @return An instance of a concrete sub class.
 * @param p_URL Database URL.
 * @exception com.globalsight.util.database.FailedToCreateFactoryException Failed to create factory.
 */
public static DbUtilFactory getInstanceForURL(String p_URL) throws FailedToCreateFactoryException
{
	StringTokenizer tokenizer = new StringTokenizer(p_URL, ":");
	if (tokenizer.hasMoreTokens())
	{
		tokenizer.nextToken(); // Ignore first one, which is "jdbc"
		if (tokenizer.hasMoreTokens())
		{
			String protocol = tokenizer.nextToken();
			char[] chars = protocol.toCharArray();
			if (chars.length > 0)
			{
				chars[0] = Character.toUpperCase(chars[0]); // Capitalize the string
				return getInstance(new String(chars));
			}
		}
	}
	throw new FailedToCreateFactoryException();
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
public abstract DbConnector makeConnector(String p_url, String p_username, String p_password) throws FailedToLoadDriverException;
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
public abstract DbConnector makeConnector(String p_driver, String p_url, String p_username, String p_password) throws FailedToLoadDriverException;
/**
 * Make a new sequence generator using the provided connector.
 * 
 * @return A new sequence generator.
 * @param p_connector Database connector.
 */
public abstract SequenceGenerator makeSequenceGenerator(DbConnector p_connector);
}
