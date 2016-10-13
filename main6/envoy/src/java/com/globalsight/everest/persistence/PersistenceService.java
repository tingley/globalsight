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
package com.globalsight.everest.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;

/**
 * PersistenceService
 * <p>
 * Provides database access using TOPLink.
 */
public class PersistenceService
{
    private JDBCPersistenceHelper m_jdbcPersistenceHelper;

    // singleton
    private static PersistenceService c_instance = null;

    /**
     * Returns the single instance of the persistence service. The first time it
     * is called the service is set up and the connections are created.
     * 
     * @return The single instance of the PersistenceService.
     * 
     * @throws A
     *             PersistenceException if an error occurred while setting up
     *             the service.
     */
    public static PersistenceService getInstance() throws PersistenceException
    {
        // this should all be done on start-up and
        // not the first time a user wants to access
        // something in the database
        if (c_instance == null)
        {
            // two or more threads may be here
            synchronized (PersistenceService.class)
            {
                // must check again as one of the
                // blocked threads can still enter
                if (c_instance == null)
                {
                    c_instance = new PersistenceService();
                }
            }
        }
        return c_instance;
    }

    /**
     * PersistenceService constructor private constructor ensures it is created
     * only once. It prepares the service to be used.
     * 
     * @throws A
     *             PersistenceException if there are any configuration or
     *             database access errors.
     */
    private PersistenceService() throws PersistenceException
    {
        super();
        m_jdbcPersistenceHelper = new JDBCPersistenceHelper();
    }

    public Connection getConnection() throws ConnectionPoolException
    {
        return m_jdbcPersistenceHelper.getConnection();
    }

    public Connection getConnectionForImport() throws ConnectionPoolException
    {
        return m_jdbcPersistenceHelper.getConnectionForImport();
    }

    public void returnConnection(Connection p_connection)
            throws ConnectionPoolException
    {
        m_jdbcPersistenceHelper.returnConnection(p_connection);
    }

    /** Silently closes the result set */
    public static void silentClose(ResultSet p_resultSet)
    {
        ConnectionPool.silentClose(p_resultSet);
    }

    /** Silently closes the statement */
    public static void silentClose(PreparedStatement p_statement)
    {
        ConnectionPool.silentClose(p_statement);
    }

    public long getSequenceNumber(long p_numberOfObjects, String p_sequenceName)
            throws PersistenceException
    {
        return m_jdbcPersistenceHelper.getSequenceNumber(p_numberOfObjects,
                p_sequenceName);
    }
}
