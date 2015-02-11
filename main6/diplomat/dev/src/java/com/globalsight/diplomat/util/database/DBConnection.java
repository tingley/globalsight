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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
 
public class DBConnection {
	
	static private DBConnection m_instance = null;
	
	private final String DRIVER = "com.mysql.jdbc.Driver";
	private final String PROPERTIES = "db_connection";
	private final String CONNECT_THIN_CLIENT = "jdbc:mysql://";
	private final String SERVER_NAME= "server_name";
	private final String PORT_NUMBER = "port_number";
	private final String DATABASE_NAME = "database_name";
	private final String USER_NAME = "user_name";
	private final String PASSWORD = "password";
	
	private Connection m_connection = null;
	private String m_serverName = "";
	private int m_portNumber = 0;
	private String m_databaseName = "";
	private String m_userName = "";
	private String m_password = "";
	
	/////////////////////////////////////////////////
	private DBConnection() 
	{
	    // retrieve our connection information from the property file
	    
	    try
        {
            ResourceBundle bundle = ResourceBundle.getBundle(PROPERTIES);
		    m_serverName = bundle.getString(SERVER_NAME);
		    m_portNumber = Integer.parseInt( bundle.getString(PORT_NUMBER) );
		    m_databaseName = bundle.getString(DATABASE_NAME);
		    m_userName = bundle.getString(USER_NAME);
		    m_password = bundle.getString(PASSWORD);
		}
		catch (MissingResourceException e) {
		    e.printStackTrace();
		}
	    
		try
		{
			// We need to load the thin client jdbc driver
			Class.forName(DRIVER); // Load the JDBC driver
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();	
		}
		
		makeConnection();
	}
	
	/////////////////////////////////////////////////
	protected void finalize()
	{
	    closeConnection();    
	}
	
	/////////////////////////////////////////////////
	static public DBConnection getInstance()
	{
		if (m_instance == null)
			m_instance = new DBConnection();
		return m_instance;	
	}
	
	/////////////////////////////////////////////////
	public String getServerName () { return m_serverName; }
	public int getPortNumber() { return m_portNumber; }
	public String getDatabaseName() { return m_databaseName; }
	public String getUserName() { return m_userName; }
	public String getPassword() { return m_password; }
	
	public void setServerName(String p_serverName) { m_serverName = p_serverName; }
	public void setPortNumber(int p_portNumber) { m_portNumber = p_portNumber; }
	public void setDatabaseName(String p_databaseName) { m_databaseName = p_databaseName; }
	public void setUserName(String p_userName) { m_userName = p_userName; }
	public void setPassword(String p_password) { m_password = p_password; }
		
	/////////////////////////////////////////////////
	public Connection getConnection() 
	{
	    if (m_connection == null)
	    {
	        makeConnection();
	    }
	    return m_connection; 
	}
	
	/////////////////////////////////////////////////
	public void closeConnection()
	{
	    if (m_connection != null)
	    {
	        try {
	            m_connection.close();
	        } catch (java.sql.SQLException e) {
    			e.printStackTrace();
	    	}
	    }
	    
	    m_connection = null;
	}
		
	/////////////////////////////////////////////////
	public void executeUpdate(String p_sql)
	{
	    Statement statement = null;
	    try 
		{
		    statement = m_connection.createStatement();
		    statement.executeUpdate(p_sql);		
		    m_connection.commit();
            statement.close();
		}
		catch (SQLException e) {
			System.out.println ( e );
		}
	}
	
	/////////////////////////////////////////////////
	private void makeConnection()
	{
	    try
	    {
	        String url = CONNECT_THIN_CLIENT + m_serverName + ":" + m_portNumber + "/" +
	            m_databaseName;
	        m_connection = DriverManager.getConnection (url, m_userName, m_password);    
	    }
	    catch (java.sql.SQLException e) 
		{
			e.printStackTrace();
		}
	}
}
