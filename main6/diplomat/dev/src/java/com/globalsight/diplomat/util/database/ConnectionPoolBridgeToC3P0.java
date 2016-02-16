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

import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * Turn all JDBC connection request to C3P0.
 * 
 * @author YorkJin
 * @since 8.6.5 (2015-11-05)
 */
public class ConnectionPoolBridgeToC3P0
{
	private static final Logger logger = Logger
			.getLogger(ConnectionPoolBridgeToC3P0.class.getName());

	private static ConnectionPoolBridgeToC3P0 instance;

	private ComboPooledDataSource ds;

    private static int MAX_CONNECTIONS = 200;
    private static final String CONNECTION_POOL_NAME = "ConnectionPoolBridgeToC3P0";

    private static final String PROP_MAX_CONNECTIONS = "maxConnections";
    private static final String DRIVER = "driver";
    private static final String CONNECT_STRING = "connect_string";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    private static final String PROPERTIES = "/properties/db_connection.properties";
    private static Properties s_props = new Properties();
    static
    {
        try
        {
            InputStream is = ConnectionPool.class
                    .getResourceAsStream(PROPERTIES);
            s_props.load(is);

			MAX_CONNECTIONS = Integer.parseInt(s_props
					.getProperty(PROP_MAX_CONNECTIONS));
        }
        catch (Throwable e)
        {
        	logger.error("Could not set property for connection pool.", e);
        }
    }

	public static final ConnectionPoolBridgeToC3P0 getInstance()
			throws ConnectionPoolException
	{
		if (instance == null)
		{
			try
			{
				instance = new ConnectionPoolBridgeToC3P0();
			}
			catch (Exception e)
			{
				logger.error(e);
				throw new ConnectionPoolException(
						"Faile to initialize ConnectionPoolBridgeToC3P0 pool");
			}
		}

		return instance;
	}

	private ConnectionPoolBridgeToC3P0() throws PropertyVetoException
	{
		String driver = s_props.getProperty(DRIVER);
		String connect_string = s_props.getProperty(CONNECT_STRING)
				+ "?useUnicode=true&characterEncoding=UTF-8";
		String userName = s_props.getProperty(USER_NAME);
        String password = s_props.getProperty(PASSWORD);

        ds = new ComboPooledDataSource();

        ds.setUser(userName);
		ds.setPassword(password);
		ds.setJdbcUrl(connect_string);
		ds.setDriverClass(driver);

		ds.setInitialPoolSize(3);
		ds.setMinPoolSize(1);
		ds.setMaxPoolSize(MAX_CONNECTIONS);
		ds.setAcquireIncrement(2);

		// 0 is the best choice?
		ds.setMaxStatements(0);
		ds.setMaxStatementsPerConnection(0);

		// Every 5 minutes check all idle connections asynchronously.
		ds.setIdleConnectionTestPeriod(300);

		// 30 minutes
		ds.setMaxIdleTime(1800);
		// Test connection when check out. for performance, this must be "false".
        ds.setTestConnectionOnCheckout(false);
		// Test connection when check in. for performance, had better be "false".
        ds.setTestConnectionOnCheckin(false);

		ds.setPreferredTestQuery("SELECT id FROM company WHERE id = 1");

		ds.setAcquireRetryAttempts(30);

		ds.setAutoCommitOnClose(true);
	}

	public synchronized Connection getConnection()
			throws ConnectionPoolException
	{
		try
		{
			return ds.getConnection();
		}
		catch (SQLException e)
		{
			logger.error(e);
			throw new ConnectionPoolException(
					"Faile to get connection from c3p0 pool: " + CONNECTION_POOL_NAME);
		}
	}

	public void returnConnection(Connection conn)
			throws ConnectionPoolException
	{
		try
		{
			// NewProxyConnection.close() is synchronized
			conn.close();
		}
		catch (SQLException e)
		{
			logger.error(e);
			throw new ConnectionPoolException(
					"Faile to return connection back to c3p0 pool: "
							+ CONNECTION_POOL_NAME);
		}
	}

	public void finalize() throws Throwable
	{
		DataSources.destroy(ds);
		super.finalize();
	}
}
