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
package com.util.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.config.properties.InstallValues;
import com.util.PropertyUtil;
import com.util.ServerUtil;

/**
 * A util class, provide some methods to operate database. <br>
 * It is a abstract class, and some method need to be implement according to the
 * system entironment in Subclass.<br>
 * Only support mysql.
 * @param <T>
 */
public abstract class DbUtil
{
    private static Logger log = Logger.getLogger(DbUtil.class);
    private static final String TEST_FILE = "script/mysql/testConnection.sql";
    private static final String HIBERNATE_PROPERTIES = "/jboss/server/standalone/deployments/globalsight.ear/lib/classes/hibernate.properties";
    private static final String OLE_HIBERNATE_PROPERTIES = "/jboss/jboss_server/server/default/deploy/globalsight.ear/lib/classes/hibernate.properties";

    private static String host;
    private static String port;
    private static String user;
    private static String password;
    private static String database;

    private static String URL = null;
    
    Connection connection = null;

    /**
     * Reads database host from installValues.properties.
     * 
     * @return Database host.
     */
    protected String getHost()
    {
        if (host == null)
        {
            host = InstallValues.getIfNull("database_server", host);
        }

        return host;
    }

    /**
     * Reads database port from installValues.properties.
     * 
     * @return Database port.
     */
    protected String getPort()
    {
        if (port == null)
        {
            port = InstallValues.getIfNull("database_port", port);
        }
        return port;
    }

    /**
     * Reads database user from installValues.properties.
     * 
     * @return Database user.
     */
    protected String getUser()
    {
        if (user == null)
        {
            user = InstallValues.getIfNull("database_username", user);
        }
        return user;
    }

    /**
     * Reads database password from installValues.properties.
     * 
     * @return database password.
     */
    protected String getPassword()
    {
        if (password == null)
        {
            password = InstallValues.getIfNull("database_password", password);
        }
        return password;
    }

    /**
     * Reads database name from installValues.properties.
     * 
     * @return The name of database.
     */
    protected String getDatabase()
    {
        if (database == null)
        {
            database = InstallValues.getIfNull("database_instance_name",
                    database);
        }
        return database;
    }

    /**
     * Test database service is start or not. <br>
     * A Exception will be throw out if can't connection to database.
     * 
     * @throws Exception
     *             Throw out if can't connection to database.
     */
    public void testConnection() throws Exception
    {
        log.debug("Test connection");
        execSqlFile(new File(TEST_FILE));
    }

    /**
     * To execute a sql file.
     * 
     * @param file
     *            The sql file to execute, can't be null and must exist.
     * @throws Exception
     *             Throw out if execute sql file failed.
     */
    public abstract void execSqlFile(File file) throws Exception;

    private String getUrl()
    {
        if (URL == null)
        {
            URL = getDbConfig("hibernate.connection.url");
        }

        return URL;
    }

    public Connection getExistConnection() throws SQLException
    {
    	if (connection == null)
    	{
    		connection = DriverManager.getConnection(getUrl(), getUser(),
                    getPassword());
    	}
        
        return connection;
    }
    
    public Connection getConnection() throws SQLException
    {
		Connection conn = DriverManager.getConnection(getUrl(), getUser(),
                getPassword());
        
        return conn;
    }

    public void updateBatch(String[] sql) throws SQLException
    {
        Connection conn = getConnection();
        Statement st = conn.createStatement();
        for (int i = 0; i < sql.length; i++)
        {
            st.addBatch(sql[i]);
        }
        st.executeBatch();
        st.close();
        conn.close();
    }

    public void update(String sql) throws SQLException
    {
        Connection conn = getConnection();
        Statement st = conn.createStatement();
        st.execute(sql);
        st.close();
        conn.close();
    }

    public List queryForSingleColumn(String sql) throws SQLException
    {
        Connection conn = getExistConnection();
        List list = new ArrayList();
        ResultSet rs = null;
        Statement st = null;
        ResultSetMetaData rsmd = null;
        try
        {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            rsmd = rs.getMetaData();
            int column = rsmd.getColumnCount();
            while (rs.next())
            {
                for (int i = 0; i < column; i++)
                {
                    list.add(rs.getObject(i + 1));
                }
            }
        }
        finally
        {
            closeResultSet(rs);
            closeStatement(st);
//            closeConn(conn);
        }
        return list;
    }

    public void insert(String sql) throws SQLException
    {
        Connection conn = getConnection();
        Statement st = null;
        try
        {
            st = conn.createStatement();
            st.execute(sql);
        }
        finally
        {
            closeStatement(st);
            closeConn(conn);
        }
    }
    
    public void executeBatch (String sql, List<List> values) throws SQLException
    {
    	 Connection conn = getExistConnection();
         PreparedStatement st = null;
         try
         {
             st = conn.prepareStatement(sql);

             int j = 0;
             for (List value : values)
             {
            	 j++;
            	 int i = 1;
            	 for (Object o : value)
            	 {
            		 st.setObject(i++, o);
            	 }
            	 st.addBatch();
            	 
            	 if (j % 5000 == 0)
            	 {
            		 st.executeBatch();
//            		 st = conn.prepareStatement(sql);
            	 }
             }
             
             if (j % 1000 > 0)
             {
            	 st.executeBatch();
             }
         }
         finally
         {
             closeStatement(st);
//             closeConn(conn);
         }
    }
    
    public void execute (String sql, Execute e) throws SQLException
    {
        Connection conn = getConnection();
        PreparedStatement st = null;
        try
        {
            st = conn.prepareStatement(sql);
            e.setValue(st);
            st.execute();
        }
        finally
        {
            closeStatement(st);
            closeConn(conn);
        }
    }
    
    public List<Long> executeWithIds (String sql, Execute e) throws SQLException
    {
    	List<Long> rIds = new ArrayList<Long>();
    	
        Connection conn = getExistConnection();
        PreparedStatement st = null;
        ResultSet ids = null;
        try
        {
            st = conn.prepareStatement(sql,  Statement.RETURN_GENERATED_KEYS);
            e.setValue(st);
            st.executeUpdate();
            ids = st.getGeneratedKeys();
            while (ids.next())
            {
            	rIds.add((long) ids.getInt(1));
            }
        }
        finally
        {
        	closeResultSet(ids);
            closeStatement(st);
//            closeConn(conn);
        }
        
        return rIds;
    }

    public List<ArrayList> query(String sql) throws SQLException
    {
        Connection conn = getConnection();
        List<ArrayList> list = new ArrayList<ArrayList>();
        ResultSet rs = null;
        Statement st = null;
        ResultSetMetaData rsmd = null;
        try
        {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            rsmd = rs.getMetaData();
            int column = rsmd.getColumnCount();
            while (rs.next())
            {
                ArrayList l = new ArrayList();
                for (int i = 0; i < column; i++)
                {
                    l.add(rs.getObject(i + 1));
                }
                list.add(l);
            }
        }
        finally
        {
            closeResultSet(rs);
            closeStatement(st);
            closeConn(conn);
        }
        return list;
    }
    
    public <T> List<T> query(String sql, ResultHander<T> hander) throws SQLException
    {
        List<T> list = new ArrayList<T>();

        Connection conn = getConnection();
        ResultSet rs = null;
        Statement st = null;
        try
        {
            st = conn.createStatement();
            rs = st.executeQuery(sql);
            while (rs.next())
            {
                list.add(hander.handerResultSet(rs));
            }
            return list;
        }
        finally
        {
            closeResultSet(rs);
            closeStatement(st);
            closeConn(conn);
        }
    }

    public void closeConn(Connection conn)
    {
        if (conn != null)
        {
            try
            {
                conn.close();
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    public void closeExistConn()
    {
    	if (connection != null)
    	{
    		try
            {
    			connection.close();
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
    		
    		connection = null;
    	}
    }

    public void closeStatement(Statement st)
    {
        if (st != null)
        {
            try
            {
                st.close();
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void closeResultSet(ResultSet rs)
    {
        if (rs != null)
        {
            try
            {
                rs.close();
            }
            catch (SQLException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }

    private String getPropertiesPath()
    {
    	String path = ServerUtil.getPath() + OLE_HIBERNATE_PROPERTIES;
    	if (new File(path).exists())
    		return path;
    	
        return ServerUtil.getPath() + HIBERNATE_PROPERTIES;
    }

    private String getDbConfig(String key)
    {
        return PropertyUtil.get(new File(getPropertiesPath()), key);
    }
}
