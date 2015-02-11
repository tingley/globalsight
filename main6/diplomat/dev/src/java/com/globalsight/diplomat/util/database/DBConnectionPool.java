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
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import com.globalsight.diplomat.servlet.config.DJ_ConnectionProfile;
 
public class DBConnectionPool {
	
	// hash table containing all connection instances
	static private HashMap m_pool = new HashMap();
	// maximum number of concurrent DB Conncetions
	static public final int MAX_DB_CONNECTIONS = 10;
	// the key number for GSA connection pool 
	static private final long GSA_ID = 1000000;
	
	// keys for properties file
	static private final String PROPERTIES = "db_connection";
	static private final String DRIVER = "driver";
	static private final String CONNECT_STRING = "connect_string";
	static private final String USER_NAME = "user_name";
	static private final String PASSWORD = "password";
	static private final String PROFILE_NAME = "GSA";
	
	private DJ_ConnectionProfile m_connectionProfile = null;
	// stores connections available for use
    private Vector m_availableConnections = new Vector();   
    // how many working connections have been created
    private int m_totalActiveConnectionsAllocated = 0;      
	
	/////////////////////////////////////////////////
	static public DBConnectionPool getInstance() throws ClassNotFoundException
	{
	    Long id = new Long(GSA_ID);
	    DBConnectionPool pool = null;
	    
	    synchronized (m_pool)
	    {
	        if (m_pool.containsKey(id))
	        {
	            pool = (DBConnectionPool) m_pool.get(id);    
	        }
	        else
	        {
	            String driver = "";
	            String connectString = "";
	            String userName = "";
	            String password = "";
	        
	            // retrieve our connection information from the property file
	            try
                {
                    ResourceBundle bundle = ResourceBundle.getBundle(PROPERTIES);
		            driver = bundle.getString(DRIVER);
		            connectString = bundle.getString(CONNECT_STRING);
		            userName = bundle.getString(USER_NAME);
		            password = bundle.getString(PASSWORD);
		        }
		        catch (MissingResourceException e) {
		            e.printStackTrace();
		        }
		        DJ_ConnectionProfile profile = new DJ_ConnectionProfile(GSA_ID, PROFILE_NAME, driver, 
		            connectString, userName, password);        
	            
	            pool = new DBConnectionPool(profile);
	            m_pool.put(id, pool);
	        }	        
	    }
	    return pool;    
	}
	
	/////////////////////////////////////////////////
	static public DBConnectionPool getInstance(long p_id) throws ClassNotFoundException
	{
	    boolean newConnection = true;
	    Long id = new Long(p_id);
	    DBConnectionPool pool = null;
	    DJ_ConnectionProfile profile = new DJ_ConnectionProfile(p_id);
	    
	    synchronized (m_pool)
	    {
	        if (m_pool.containsKey(id))
	        {
	            pool = (DBConnectionPool) m_pool.get(id);
	            // use the same connection if they are equal
	            if (profile.equals(pool.getConnectionProfile()))
	                newConnection = false;
	        }
	        if (newConnection)
	        {
	            pool = new DBConnectionPool(profile);
	            m_pool.put(id, pool);    
	        }
	    }
	    return pool;
	}
	
	/////////////////////////////////////////////////
	static public DBConnectionPool getInstance(DJ_ConnectionProfile p_profile) throws ClassNotFoundException
	{
	    boolean newConnection = true;
	    Long id = new Long(p_profile.getID() );
	    DBConnectionPool pool = null;
	    
	    synchronized (m_pool)
	    {
	        if (m_pool.containsKey(id))
	        {
	            pool = (DBConnectionPool) m_pool.get(id);
	            // use the same connection if they are equal
	            if (p_profile.equals(pool.getConnectionProfile()))
	                newConnection = false;
	        }
	        if (newConnection)
	        {
	            pool = new DBConnectionPool(p_profile);
	            m_pool.put(id, pool);    
	        }
	    }
	    return pool;
	}
	
	/////////////////////////////////////////////////
	// Releases all connections that it owns
    // This method may be called multiple times by different servlets
    // and it may release connections at different times
    static public void terminate()
    {
        synchronized (m_pool)
	    {
            Set set = m_pool.entrySet();
            Iterator iterator = set.iterator();
            
            // loop through all instances
            while(iterator.hasNext())
            {   
                // remove the connections
                Map.Entry entry = (Map.Entry)iterator.next();
                DBConnectionPool instance = (DBConnectionPool) entry.getValue();
                instance.closeAllConnections();
                // remove the instance
                iterator.remove();
            }
        }
    }
	
	/////////////////////////////////////////////////
	private DBConnectionPool(DJ_ConnectionProfile p_profile) throws ClassNotFoundException
	{
	    m_connectionProfile = p_profile;
	    
	    // We need to load the thin client jdbc driver
	    try {
            Class.forName(m_connectionProfile.getDriver()).newInstance(); // Load the JDBC driver        
        }
        catch (IllegalAccessException ie) {
            ie.printStackTrace();
        }
        catch (InstantiationException e) {
            e.printStackTrace();
        }            
	}
	
	/////////////////////////////////////////////////    	
	// gets the first available connection from the pool
    synchronized public Connection getConnection() throws SQLException
    {
        Connection connection = findConnection();
        if (connection == null)
        {
            // if there are no available connections make the thread wait until one is returned
            boolean found = false;
            while(m_availableConnections.size() == 0 && !Thread.interrupted() && !found)
            {
                try
                {
                    wait();
                    connection = findConnection();
                    if (connection != null)
                        found = true;     
                }
                catch(InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return connection;
    }
    
    /////////////////////////////////////////////////
    // returns an available connection to the pool
    public void returnConnection(Connection p_theConnection)
    {
        if( p_theConnection != null )
        {   
            // if the connection is good, return it to the pool
            if( isConnectionAlive(p_theConnection) )
            {
                addConnection(p_theConnection);
            }
            else
            {
                // the connection is no longer open. 
                --m_totalActiveConnectionsAllocated;
                notifyPool();
            }
            
        }
    }
    
    /////////////////////////////////////////////////
    // notify connection to the pool
    synchronized private void notifyPool()
    {       
        notifyAll();
    }
    
    /////////////////////////////////////////////////
    // add a connection to the pool
    synchronized private void addConnection(Connection p_connection)
    {
        m_availableConnections.addElement(p_connection);        
        notifyAll();
    }
    
    /////////////////////////////////////////////////
	public void executeUpdate(String p_sql) throws SQLException
	{
		Connection connection = getConnection();
		Statement statement = connection.createStatement();
		statement.executeUpdate(p_sql);		
		connection.commit();
        statement.close();
        returnConnection(connection);
	}
    
    /////////////////////////////////////////////////
    // returns the number of available connections
    public int getNumAvailableConnections()
    {
        return m_availableConnections.size();
    }
    
    /////////////////////////////////////////////////
    // returns the number of active connections both within and without the pool
    public int getNumActiveConnections()
    {
        return m_totalActiveConnectionsAllocated;
    }
    
    /////////////////////////////////////////////////
    public DJ_ConnectionProfile getConnectionProfile()
    {
        return m_connectionProfile;    
    }
	
	/////////////////////////////////////////////////
	// shutdown all connections for this instance
    synchronized public void closeAllConnections()
    {
        // closes all available connections and deallocates them
        synchronized (m_availableConnections)
        {
            while(m_availableConnections.size() > 0)
            {
                Connection theConnection = (Connection) m_availableConnections.remove(0);
                --m_totalActiveConnectionsAllocated;
                try
                {
                    theConnection.close();
                }
                catch(SQLException ex) {}
            }
        }
    }
  
	/////////////////////////////////////////////////    	
	private Connection findConnection() throws SQLException
	{
	    // if there are connections available, return the first one
        boolean found = false;
        Connection connection = null;
        
        synchronized(m_availableConnections)
        {
            while ( ( m_availableConnections.size() > 0 ) && !found)
            {
                connection = (Connection) m_availableConnections.remove(0);
                // check for valid connection ( could possibly timed out )
                if ( isConnectionAlive(connection) )
                    found = true;
                else
                {
                    // this is a bad connection.
                    --m_totalActiveConnectionsAllocated;
                }
            }
        
            // if there are no connections available but the number allocated does not exceed the pool size
            // make a new connection
            if (!found)
            {
                if(m_availableConnections.size() == 0 && m_totalActiveConnectionsAllocated < MAX_DB_CONNECTIONS)
                    connection = makeNewConnection();
            }
        }
        
        return connection;    
	}	
       
    /////////////////////////////////////////////////
    // creates a new connection to the data base
    synchronized private Connection makeNewConnection() throws SQLException
    {
        Connection newConnection =  (Connection) DriverManager.getConnection(
            m_connectionProfile.getConnectionString(), m_connectionProfile.getUserName(),
            m_connectionProfile.getPassword());
        if( newConnection != null )
            ++m_totalActiveConnectionsAllocated;
           
        return newConnection;
    }    

    /////////////////////////////////////////////////
    // indicates whether the connection is alive
    private boolean isConnectionAlive(Connection p_theConnection)
    {
        boolean ok = false;
        try
        {
            if(!p_theConnection.isClosed())
            {
                // check for a valid connection
                Statement statement = p_theConnection.createStatement();
                statement.executeQuery("SELECT * FROM DUAL");
                statement.close();
                ok = true;
            }
        }
        catch(SQLException e)
        {
            // connection isn't valid
            e.printStackTrace();
            try {
                p_theConnection.close();
            }
            catch(SQLException ex){}
        }       
        return ok;
    }    
}
