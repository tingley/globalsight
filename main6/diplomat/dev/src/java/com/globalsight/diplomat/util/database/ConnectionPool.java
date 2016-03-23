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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * <p>
 * Connection Pool provides static methods for obtaining and returning
 * connections. The class maintains one physical instance for a default
 * ConnectionProfile, as well as one physical instance for each connection
 * profile that is specifically designated.
 * </p>
 * 
 * <p>
 * When a connection is allocated, it is moved from the "unallocated" array into
 * the "allocated" array and wrapped inside a WeakReference. This way if an
 * allocated connection is never explicitly returned by the user, it will
 * eventually be reclaimed by the garbage collector, and ultimately removed from
 * the "allocated" array.
 * </p>
 */
public class ConnectionPool
{
    private static final Logger CATEGORY = Logger
            .getLogger(ConnectionPool.class.getName());

    //
    // PRIVATE CONSTANTS
    //
    private static int MAX_CONNECTIONS = 200;
    private static int MIN_CONNECTIONS = 0;
    private static long MAX_CONNECTION_WAIT_TIME = 60 * 60 * 1000;
    private static final long PRIVATE_ID = -1000000L;
    private static final long JDBCPOOL_ID = -1L;
    private static final long IMPORTPOOL_ID = -100L;
    private static final String PROPERTIES = "/properties/db_connection.properties";
    private static final String DRIVER = "driver";
    private static final String CONNECT_STRING = "connect_string";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    private static final String PROFILE_NAME = "GSA";
    private static final String PROP_MAX_CONNECTIONS = "maxConnections";
//    private static final String PROP_MIN_CONNECTIONS = "minConnections";
    private static final String PROP_MAX_CONNECTION_WAIT_TIME = "maxConnectionWaitTime";
    private static final String PROP_USE_CONNECTION_POOL = "useConnectionPool";
    private static boolean s_doPooling = true;

    private static final String PROP_USE_C3P0_POOL = "use_c3p0_connection_pool";
    private static boolean useC3P0ConnectionPool = true;

    //
    // PRIVATE STATIC MEMBER VARIABLES
    //
	private static ConcurrentMap<Long, ConnectionPool> m_pools = new ConcurrentHashMap<Long, ConnectionPool>();
    private static SystemConfiguration m_systemConfiguration;
    private static Properties s_props = new Properties();

	private static final ConcurrentMap<Connection, ConnectionPool> m_connToPools = new ConcurrentHashMap<Connection, ConnectionPool>();

    //
    // PRIVATE INSTANCE MEMBER VARIABLES
    //
    private ConnectionProfile m_connectionProfile;
    private Vector<Connection> m_unallocatedConns;
    private Vector<Connection> m_allocatedConns;

    private static Timer timer = new Timer(true);
    //
    // STATIC INITIALIZER
    //
    static
    {
        try
        {
            InputStream is = ConnectionPool.class.getResourceAsStream(PROPERTIES);
            s_props.load(is);
            MAX_CONNECTIONS = Integer.parseInt(s_props.getProperty(PROP_MAX_CONNECTIONS));
            MAX_CONNECTION_WAIT_TIME = Long.parseLong(s_props
                    .getProperty(PROP_MAX_CONNECTION_WAIT_TIME));
            s_doPooling = Boolean.valueOf(s_props.getProperty(PROP_USE_CONNECTION_POOL))
                    .booleanValue();
            CATEGORY.info("Use Connection Pooling: " + s_doPooling);

            useC3P0ConnectionPool = Boolean.valueOf(s_props.getProperty(PROP_USE_C3P0_POOL));
            if (useC3P0ConnectionPool)
            {
                CATEGORY.info("Use C3P0 connection pool to replace GS own pool!!!");
            }
        }
        catch (Throwable e)
        {
            MAX_CONNECTIONS = 20;
            CATEGORY.error("Could not set property for connection pool.", e);
        }
    }

    /**
     * Silently closes a Statement without throwing any exceptions.
     */
    public static void silentClose(Statement p_statement)
    {
        if (p_statement != null)
        {
            try
            {
                p_statement.close();
                p_statement = null;
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage());
            }
        }
    }

    /**
     * Silently closes a ResultSet without throwing any exceptions.
     */
    public static void silentClose(ResultSet p_resultSet)
    {
        if (p_resultSet != null)
        {
            try
            {
                p_resultSet.close();
                p_resultSet = null;
            }
            catch (Exception e)
            {
                CATEGORY.error(e.getMessage());
            }
        }
    }

    /**
     * Closes ResultSet and Statement.
     * 
     * @param p_resultSet
     */
    public static void closeAll(ResultSet p_resultSet)
    {
        if (p_resultSet != null)
        {
            try
            {
                Statement statement = p_resultSet.getStatement();
                silentClose(p_resultSet);
                silentClose(statement);
            }
            catch (SQLException e)
            {
                CATEGORY.error(e.getMessage());
            }
        }
    }

    /**
     * Silently returns a connection without throwing any exceptions
     */
    public static void silentReturnConnection(Connection p_connection)
    {
        try
        {
            returnConnection(p_connection);
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage());
        }
    }

    /**
     * Returns the max number of DB connections in the pool
     * 
     * @return max connections
     */
    public static int getMaxConnections()
    {
        return MAX_CONNECTIONS;
    }

    /**
     * Return a connection based on the default connection profile.
     * 
     * @return a valid connection.
     */
    public static Connection getConnection() throws ConnectionPoolException
    {
        return getConnection(PRIVATE_ID);
    }

    /**
     * Return a connection based on the connection profile with the given id.
     * 
     * @return a valid connection
     * @throws ConnectionPoolException
     *             if the profile cannot be loaded
     */
    public static Connection getConnection(long p_profileId)
            throws ConnectionPoolException
    {
    	if (useC3P0ConnectionPool)
    	{
        	return ConnectionPoolBridgeToC3P0.getInstance().getConnection();
    	}

    	ConnectionPool pool = _getPool(p_profileId);
        Connection c = null;
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Getting connection for pool " + p_profileId
                    + ". Free pool is " + pool.m_unallocatedConns.size()
                    + " Used pool is " + pool.m_allocatedConns.size());
        }

        c = pool._getConnection();
        try
        {
            c.setAutoCommit(true);
        }
        catch (SQLException e)
        {
            CATEGORY.error(e);
            throw new ConnectionPoolException(
                    "Unable to set autoCommit to true");
        }
        if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Got connection " + c.hashCode());
        }

		if (c != null) {
			m_connToPools.put(c, pool);
		}

        return c;
    }

    /**
     * Put the given connection back into the unallocated collection, assuming
     * that it actually was allocated by the connection pool in the first place.
     *
     * @param p_connection
     *            the connection to recirculate.
     *
     * @throws ConnectionPoolException
     *             if the connection was not allocated by the ConnectionPool.
     */
    public static void returnConnection(Connection p_connection)
            throws ConnectionPoolException
    {
    	if (useC3P0ConnectionPool)
    	{
			ConnectionPoolBridgeToC3P0.getInstance().returnConnection(
					p_connection);
			return;
    	}

    	if (CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("Returning connection " + p_connection.hashCode());
        }

        if (s_doPooling == false)
        {
            // close this connection
            try
            {
                p_connection.close();
            }
            catch (Exception se)
            {
                CATEGORY.error("Unable to close DB connection.", se);
            }
        }
        else if (p_connection != null)
        {
            ConnectionPool pool = _poolThatOwns(p_connection);

            if (pool == null)
            {
                m_connToPools.remove(p_connection);
                throw new ConnectionPoolException(
                        "Returned connection does not belong to the pool");
            }

            pool._returnConnection(p_connection);
            m_connToPools.remove(p_connection);
            CloseConnectionTask task = new CloseConnectionTask(pool,
                    p_connection);
            timer.schedule(task, MAX_CONNECTION_WAIT_TIME);
        }
    }

    /**
     * Terminate all pools that are maintained in the connection pool. Ensure
     * that all connections are closed in each pool.
     */
    public static synchronized void terminate()
    {
		if (useC3P0ConnectionPool) {
			try {
				ConnectionPoolBridgeToC3P0.getInstance().finalize();
			} catch (Throwable e) {
				CATEGORY.error(e);
			}
			return;
		}

    	Iterator<ConnectionPool> it = m_pools.values().iterator();
        while (it.hasNext())
        {
            ConnectionPool pool = (ConnectionPool) it.next();

            pool._closeAllConnections();

            it.remove();
        }
    }

    public static void terminate(long p_connectionProfileId)
    {
		if (useC3P0ConnectionPool) {
			try {
				ConnectionPoolBridgeToC3P0.getInstance().finalize();
			} catch (Throwable e) {
				CATEGORY.error(e);
			}
			return;
		}

    	ConnectionPool pool = __removePool(p_connectionProfileId);

        if (pool != null)
        {
            pool._closeAllConnections();
        }
    }

    //
    // PRIVATE STATIC SUPPORT METHODS
    //

    /**
     * Return the pool instance identified by the given profile id. If the
     * desired pool is not in the hash map, then it is created first.
     */
    private static ConnectionPool _getPool(long p_profileId)
            throws ConnectionPoolException
    {
        ConnectionPool pool = (ConnectionPool) m_pools.get(p_profileId);
        if (pool == null)
        {
            pool = _createPool(p_profileId);
        }

        return pool;
    }

    /**
     * Return the pool instance identified by the given profile id and remove it
     * from the pools list. If the desired pool is not in the hash map, null is
     * returned.
     */
    private static ConnectionPool __removePool(long p_profileId)
    {
        return (ConnectionPool) m_pools.remove(p_profileId);
    }

    /**
     * Create a new connection pool, add it to the map, and return it. Depending
     * on the given id, the pool may be loaded from the database.
     *
     * This method is called by the synchronized _getPool().
     */
    private static ConnectionPool _createPool(long p_profileId)
            throws ConnectionPoolException
    {
        ConnectionProfile cp = null;

        if (p_profileId == PRIVATE_ID)
        {
            cp = _defaultProfile();
        }
        else if (p_profileId == JDBCPOOL_ID)
        {
            cp = _jdbcPoolProfile();
        }
        else if (p_profileId == IMPORTPOOL_ID)
        {
            cp = _importPoolProfile();
        }
        else
        {
            cp = _loadProfile(p_profileId);
        }

        ConnectionPool pool = new ConnectionPool(cp);

        m_pools.put(new Long(p_profileId), pool);

        return pool;
    }

    /**
     * Return a new default connection profile based on the properties file.
     */
    private static ConnectionProfile _defaultProfile()
            throws ConnectionPoolException
    {
        ConnectionProfile cp = null;

        try
        {
            cp = new ConnectionProfile();
            cp.setId(PRIVATE_ID);
            cp.setProfileName(PROFILE_NAME);
            cp.setDriver(s_props.getProperty(DRIVER));
            cp.setConnectionString(s_props.getProperty(CONNECT_STRING));
            cp.setUserName(s_props.getProperty(USER_NAME));
            cp.setPassword(s_props.getProperty(PASSWORD));
        }
        catch (MissingResourceException e)
        {
            throw new ConnectionPoolException(
                    "Incomplete or missing property file " + PROPERTIES, e);
        }

        return cp;
    }

    /**
     * Return a new default jdbc connection profile based on the settings in the
     * system configuration.
     */
    private static ConnectionProfile _jdbcPoolProfile()
            throws ConnectionPoolException
    {
        ConnectionProfile cp = null;

        _initConfig();

        try
        {
            String username = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_USERNAME);
            String password = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_PASSWORD);
            String url = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_CONNECTION);
            String driverName = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_DRIVER);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("The JDBC drivername is " + driverName);
                CATEGORY.debug("The JDBC username is " + username);
            }

            cp = new ConnectionProfile();
            cp.setUserName(username);
            cp.setPassword(password);
            cp.setConnectionString(url);
            cp.setDriver(driverName);
            cp.setId(JDBCPOOL_ID);
        }
        catch (Exception e)
        {
            throw new ConnectionPoolException(
                    "Unable to create JDBCPoolProfile", e);
        }

        return cp;
    }

    /**
     * Return a new default jdbc connection profile based on the settings in the
     * system configuration.
     */
    private static ConnectionProfile _importPoolProfile()
            throws ConnectionPoolException
    {
        ConnectionProfile cp = null;

        _initConfig();

        try
        {
            String username = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_USERNAME);
            String password = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_PASSWORD);
            String url = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_CONNECTION);
            String driverName = m_systemConfiguration
                    .getStringParameter(SystemConfigParamNames.DB_DRIVER);

            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("The JDBC drivername is " + driverName);
                CATEGORY.debug("The JDBC username is " + username);
            }

            cp = new ConnectionProfile();
            cp.setUserName(username);
            cp.setPassword(password);
            cp.setConnectionString(url);
            cp.setDriver(driverName);
            cp.setId(IMPORTPOOL_ID);
        }
        catch (Exception e)
        {
            throw new ConnectionPoolException(
                    "Unable to create JDBCPoolProfile", e);
        }

        return cp;
    }

    /* Load the system configuration file */
    private static synchronized SystemConfiguration _initConfig()
            throws ConnectionPoolException
    {
        try
        {
            if (m_systemConfiguration == null)
            {
                m_systemConfiguration = SystemConfiguration.getInstance();
            }
        }
        catch (Exception e)
        {
            throw new ConnectionPoolException(
                    "Unable to initialize system configuration", e);
        }

        return m_systemConfiguration;
    }

    /**
     * Load the connection profile with the given id from the database. NOTE:
     * this method is a little tricky because it depends on the
     * ConnectionProfileDbAccessor, which depends on the ConnectionPool.
     */
    private static ConnectionProfile _loadProfile(long p_profileId)
            throws ConnectionPoolException
    {
        ConnectionProfile cp = null;

        try
        {
            cp = ConnectionProfileDbAccessor.readConnectionProfile(p_profileId);
        }
        catch (Exception e)
        {
            throw new ConnectionPoolException("Unable to load profile with id="
                    + p_profileId, e);
        }

        return cp;
    }

    /**
     * Find the connection pool instance that owns the given connection, or null
     * if none exists.
     */
    private static ConnectionPool _poolThatOwns(Connection p_conn)
    {
        return m_connToPools.get(p_conn);
    }

    //
    // PRIVATE CONSTRUCTORS
    //

    /**
     * Return a default instance of the Connection Pool.
     */
    private ConnectionPool()
    {
        super();

        m_connectionProfile = null;
        m_unallocatedConns = new Vector<Connection>();
        m_allocatedConns = new Vector<Connection>();
    }

    /**
     * Return an instance of the Connection Pool with the given profile.
     */
    private ConnectionPool(ConnectionProfile p_profile)
            throws ConnectionPoolException
    {
        this();

        m_connectionProfile = p_profile;

        try
        {
            Class.forName(p_profile.getDriver()).newInstance();
        }
        catch (Exception e)
        {
            throw new ConnectionPoolException("Unable to load driver "
                    + p_profile.getDriver(), e);
        }
    }

    //
    // PRIVATE INSTANCE METHODS that are considered PUBLIC to the
    // static methods (all methods are prefixed with underscore).
    //

    /**
     * Return the first available connection from this connection pool. If no
     * connection is available and none can be allocated, a
     * ConnectionPoolException is thrown.
     *
     * This is a "public" method on the pool instance.
     */
    private Connection _getConnection() throws ConnectionPoolException
    {
        Connection conn = null;

        while (conn == null && !Thread.interrupted())
        {
            try
            {
                conn = __findConnection();
            }
            catch (ConnectionPoolException e)
            {
                try
                {
                    wait();
                }
                catch (InterruptedException e1)
                {
                    throw new ConnectionPoolException(
                            "Thead call wait() failed");
                }
            }
        }

        return conn;
    }

    /**
     * Remove the given connection from the allocated list; if it is still
     * alive, add it to the unallocated list; otherwise, since _findConnection
     * may be waiting for a connection, create a new one and add it to the
     * unallocated list.
     *
     * This is a "public" method on the pool instance.
     */
    private void _returnConnection(Connection p_conn)
    {
        __removeAllocatedConnection(p_conn);
        __unallocateConnection(p_conn);
    }

    /**
     * Close all connections for this instance.
     *
     * This is a "public" method on the pool instance.
     */
    private synchronized void _closeAllConnections()
    {
        __closeUnallocatedConnections();
        __closeAllocatedConnections();
    }

    //
    // PRIVATE INSTANCE METHODS that are considered PRIVATE to the
    // static methods (all methods are prefixed with 2 underscores).
    //

    /**
     * Find an unallocated connection, or create one if there is space available
     * to do so. If this is not possible, return null.
     *
     * This method is called by the synchronized _getConnection().
     */
    private Connection __findConnection() throws ConnectionPoolException
    {
        if (s_doPooling == false)
        {
            // don't pool, always return a new Connection
            return __createConnection();
        }

        Connection conn = null;

        if (m_unallocatedConns.size() > 0)
        {
        	// "remove" is thread safe
            conn = (Connection) m_unallocatedConns.remove(0);
            try
            {
                if (conn.isClosed())
                {
                    conn = null;
                }
                else
                {
                	try
                    {
                		__allocateConnection(conn);
                    }
                    catch (Exception e)
                    {
                        conn = null;
                    }
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        else if (__totalConnections() <= MAX_CONNECTIONS)
        {
            // This call fails if the server's hard limit is exceeded.
            conn = __createConnection();
        }
        else
        {
            CATEGORY.info("Soft connection limit (" + MAX_CONNECTIONS
                    + ") has been exceeded."
                    + "  Update the number of database connections in "
                    + PROPERTIES + ".properties.");
            // No more connections can be allocated. This is the soft
            // limit, the database server will have its own hard limit.
            throw new ConnectionPoolException("Soft connection limit ("
                    + MAX_CONNECTIONS + ") has been exceded."
                    + "  Update the number of database connections in "
                    + PROPERTIES + ".properties.");
        }

        return conn;
    }

    /**
     * Add the given connection to the allocated connections array.
     *
     * Called by the synchronized _findConnection().
     */
    private void __allocateConnection(Connection p_conn)
    {
    	// "addElement" is thread safe
        m_allocatedConns.addElement(p_conn);
    }

    /**
     * Add the given connection to the unallocated connections array.
     *
     * Called by the synchronized _returnConnection().
     */
    private void __unallocateConnection(Connection p_conn)
    {
    	// "addElement" is thread safe
        m_unallocatedConns.addElement(p_conn);
    }

    /**
     * Create a new connection based in the connection profile information.
     * Creation of a new connection implies that it is automatically added to
     * the "allocated" connection.
     *
     * Called by the synchronized _findConnection().
     */
	private Connection __createConnection() throws ConnectionPoolException
    {
        Connection conn = null;
        Properties props = new Properties();

        props.put("user", m_connectionProfile.getUserName());
        props.put("password", m_connectionProfile.getPassword());

        PreparedStatement ps = null;
        try
        {
            Driver d = (Driver) Class.forName(m_connectionProfile.getDriver())
                    .newInstance();
            conn = d.connect(m_connectionProfile.getConnectionString(), props);

            // only add to the allocated pool if pooling is on
            if (s_doPooling == true)
            {
                __allocateConnection(conn);
            }
        }
        catch (Exception e)
        {
            throw new ConnectionPoolException(
                    "Unable to create new connection", e);
        }
        finally
        {
            ConnectionPool.silentClose(ps);
        }

        return conn;
    }

    /**
     * Return the total number of connections maintained by this pool.
     *
     * This method is called from _findConnection().
     */
    private int __totalConnections()
    {
        return m_unallocatedConns.size() + m_allocatedConns.size();
    }

    /**
     * Remove the given connection from the allocated list.
     *
     * This method is called from the synchronized_returnConnection().
     */
    private synchronized void __removeAllocatedConnection(Connection p_conn)
    {
    	// "remove" is not thread safe
    	m_allocatedConns.remove(p_conn);

    	m_connToPools.remove(p_conn);

    	notifyAll();
    }

    /**
     * Close all the connections in the allocated vector.
     *
     * This method is called from the synchronized closeAllConnections().
     */
    private void __closeAllocatedConnections()
    {
        while (m_allocatedConns.size() > 0)
        {
            __closeConnection((Connection) m_allocatedConns.remove(0));
        }
    }

    /**
     * Close all the connections in the unallocated vector.
     *
     * This method is called from the synchronized closeAllConnections().
     */
    private void __closeUnallocatedConnections()
    {
        while (m_unallocatedConns.size() > 0)
        {
            __closeConnection((Connection) m_unallocatedConns.remove(0));
        }
    }

    /**
     * Close the given connection. Ignore any exception.
     */
    private void __closeConnection(Connection p_conn)
    {
        try
        {
            p_conn.close();
            m_connToPools.remove(p_conn);
        }
        catch (Throwable e)
        {
            CATEGORY.error("Cannot close connection", e);
        }
    }

    /**
     * Try to close a connection in m_unallocatedConns. If the connection is not
     * in m_unallocatedConns or the size of all connection is < min number, the
     * connection will not be closed.
     *
     * <p>
     * Notice: The method will be run automatice, so please <b>NOT</b> call this
     * method.
     *
     * @param conn
     *            The connection try to close.
     */
    public synchronized void closeUnallocatedConnection(Connection conn)
    {
        if (m_unallocatedConns.contains(conn)
                && __totalConnections() > MIN_CONNECTIONS)
        {
            m_unallocatedConns.remove(conn);
            __closeConnection(conn);
        }
    }
}
