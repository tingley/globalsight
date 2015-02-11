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
package com.globalsight.ling.tm2.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.DbAccessor;
import com.globalsight.everest.persistence.PersistenceService;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GlobalSightLocale;

/**
 * A collection of utility methods regarding to DB access.
 */

public class DbUtil
{
    private static final Logger c_logger = Logger.getLogger(DbUtil.class);

    // maximum element number ARRAY can carry is 4095, not 4096.
    static public final int MAX_ELEM = 4095;

    // max byte size in RAW type PL/SQL variable
    static public final int MAX_RAW_SIZE = 32767;

    private static final String LOCK_TABLE = "lock table ";
    private static final String LOCK_TABLE_TYPE = " write ";

    private static final String UNLOCK = "unlock tables";

    static private PersistenceService s_persistence = null;

    public static int BATCH_INSERT_UNIT = getBatchInsertUnit();

    private static final String SQL_QUERY_TABLE = "show tables like ?";

    private static int getBatchInsertUnit()
    {
        int unit;
        try
        {
            SystemConfiguration prop = SystemConfiguration.getInstance();
            unit = prop.getIntParameter("leverager.batchInsertUnit");
        }
        catch (Exception e)
        {
            // It ususally shouldn't happen. Return default value.
            unit = 200;
        }
        return unit;
    }

    // get database connection from a connection pool
    static public Connection getConnection() throws Exception
    {
        if (s_persistence == null)
        {
            s_persistence = PersistenceService.getInstance();
        }
        return s_persistence.getConnection();
    }

    // return database connection to a connection pool
    static public void returnConnection(Connection p_connection)
            throws Exception
    {
        if (s_persistence == null)
        {
            s_persistence = PersistenceService.getInstance();
        }
        s_persistence.returnConnection(p_connection);
    }

    /** Silently returns the connection */
    public static void silentReturnConnection(Connection p_connection)
    {
        try
        {
            if (p_connection != null)
            {
                returnConnection(p_connection);
            }
        }
        catch (Exception e)
        {
        }
    }

    /** Silently closes the result set */
    public static void silentClose(ResultSet p_resultSet)
    {
        ConnectionPool.silentClose(p_resultSet);
    }

    /**
     * Closes ResultSet and Statement.
     * 
     * @param p_resultSet
     */
    public static void closeAll(ResultSet p_resultSet)
    {
        ConnectionPool.closeAll(p_resultSet);
    }

    /** Silently closes the statement */
    public static void silentClose(PreparedStatement p_statement)
    {
        ConnectionPool.silentClose(p_statement);
    }

    public static void silentClose(Statement p_statement)
    {
        ConnectionPool.silentClose(p_statement);
    }

    // read Clob column from a result set
    static public String readClob(ResultSet p_resultSet, int p_columnIndex)
            throws Exception
    {
        return DbAccessor.readClob(p_resultSet, p_columnIndex);
    }

    // read Clob column from a result set
    static public String readClob(ResultSet p_resultSet, String p_columnName)
            throws Exception
    {
        return DbAccessor.readClob(p_resultSet, p_columnName);
    }

    // read Blob column from a result set
    static public byte[] readBlob(ResultSet p_resultSet, int p_columnIndex)
            throws Exception
    {
        return DbAccessor.readBlob(p_resultSet, p_columnIndex);
    }

    // read Blob column from a result set
    static public byte[] readBlob(ResultSet p_resultSet, String p_columnName)
            throws Exception
    {
        return DbAccessor.readBlob(p_resultSet, p_columnName);
    }

    /**
     * Unlocks all tables locked in the connection.
     * 
     * @param p_connection
     * @throws Exception
     */
    static public void unlockTables(Connection p_connection) throws Exception
    {
        c_logger.debug("Unlock tables.");
        execute(p_connection, UNLOCK);
    }

    /**
     * Locks one table at a time using LOCK TABLE sql command,
     * unlockTable(Connection p_connection) method must be called when the lock
     * is unnecessary.
     * 
     * @param p_connection
     * @param name
     * @throws Exception
     */
    static public void lockTable(Connection p_connection, String name)
            throws Exception
    {
        List<String> tables = new ArrayList<String>();
        tables.add(name);
        lockTables(p_connection, tables);
    }

    /**
     * Locks more than one tables at a time using LOCK TABLE sql command,
     * unlockTable(Connection p_connection) method must be called when the lock
     * is unnecessary.
     * 
     * @param p_connection
     *            Connection
     * @param p_tables
     *            List of table names of which the lock is obtained
     */
    synchronized static public void lockTables(Connection p_connection,
            List<String> p_tables) throws Exception
    {
        if (c_logger.isDebugEnabled())
        {
            c_logger.debug("Locks tables :" + p_tables.toString());            
        }

        if (p_tables == null || p_tables.isEmpty())
        {
            return;
        }

        // Just load the values at the first time.
        Sequence.init();

        StringBuilder sql = new StringBuilder(LOCK_TABLE);
        sql.append(p_tables.get(0)).append(LOCK_TABLE_TYPE);

        for (int i = 1; i < p_tables.size(); i++)
        {
            sql.append(", ").append(p_tables.get(i)).append(LOCK_TABLE_TYPE);
        }

        execute(p_connection, sql.toString());
    }

    private static void putArgsToStatement(PreparedStatement stmt,
            List<Object> args) throws SQLException
    {
        if (args != null)
        {
            for (int i = 0; i < args.size(); i++)
            {
                stmt.setObject(i + 1, args.get(i));
            }
        }
    }

    /**
     * Executes a sql without '?'.
     * <p>
     * For example:
     * <p>
     * String sql = "update table test set name = 'a'";<br>
     * execute(conn, sql);<br>
     * 
     * @param p_connection
     * @param sql
     * @throws SQLException
     */
    public static void execute(Connection p_connection, String sql)
            throws SQLException
    {
        execute(p_connection, sql, null);
    }

    /**
     * Executes a sql with '?'.
     * <p>
     * For example:
     * <p>
     * String sql = "update table test set name = 'a' where id = ?";<br>
     * List args = new ArrayList();<br>
     * execute(conn, sql, args);<br>
     * 
     * @param p_connection
     * @param sql
     * @param args
     * @throws SQLException
     */
    public static void execute(Connection p_connection, String sql, List args)
            throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            stmt = p_connection.prepareStatement(sql);
            putArgsToStatement(stmt, args);
            stmt.execute();
        }
        catch (SQLException e)
        {
            // try to lock again.
            c_logger.warn("Try to lock again for 'Lock wait timeout', sql is "
                    + sql);
            if (e.getMessage().indexOf("Lock wait timeout") > -1)
            {
                execute(p_connection, sql, args);
            }
            else
            {
                c_logger.error(e.getMessage(), e);
                throw e;
            }
        }
        finally
        {
            silentClose(stmt);
        }
    }

    public static List search(String sql, List args, HandleSqlSearch handle)
            throws Exception
    {
        PreparedStatement stmt = null;
        Connection conn = getConnection();
        List result = null;
        try
        {
            stmt = conn.prepareStatement(sql);
            putArgsToStatement(stmt, args);
            ResultSet set = stmt.executeQuery();
            result = handle.handleSearch(set);
        }
        catch (SQLException e)
        {
            c_logger.error(e.getMessage(), e);
            throw e;
        }
        finally
        {
            silentClose(stmt);
            returnConnection(conn);
        }

        return result;
    }

    /**
     * create in clause of locale ids. It produces somethig like this: (9238,
     * 2034, 2039)
     * 
     * @param p_locales
     *            Collection of locales (GlobalSightLocale)
     * @return in clause string shown above.
     */
    public static String createLocaleInClause(
            Collection<GlobalSightLocale> p_locales)
    {
        StringBuffer sf = new StringBuffer();
        sf.append("(");

        Iterator<GlobalSightLocale> it = p_locales.iterator();
        while (it.hasNext())
        {
            GlobalSightLocale locale = (GlobalSightLocale) it.next();
            sf.append(locale.getId());
            if (it.hasNext())
            {
                sf.append(", ");
            }
        }
        sf.append(")");
        return sf.toString();
    }

    /**
     * create in clause of integers. It produces somethig like this: (9238,
     * 2034, 2039)
     * 
     * @param p_integers
     *            Collection of Number objects
     * @return in clause string shown above.
     */
    public static String createIntegerInClause(Collection p_integers)
    {
        StringBuffer sf = new StringBuffer();
        sf.append("(");

        for (Iterator it = p_integers.iterator(); it.hasNext();)
        {
            Number id = (Number) it.next();
            sf.append(id.longValue());
            if (it.hasNext())
            {
                sf.append(", ");
            }
        }
        sf.append(")");
        return sf.toString();
    }

    /**
     * Create a SQL IN clause of strings. It produces somethig like this:
     * ('abc', 'def', 'ghi')
     * 
     * @param p_strings
     *            Set of String objects
     * @return in clause string shown above.
     */
    public static String createStringInClause(Set p_strings)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("(");

        for (Iterator it = p_strings.iterator(); it.hasNext();)
        {
            String text = (String) it.next();

            sb.append("'").append(text.replaceAll("'", "''")).append("'");

            if (it.hasNext())
            {
                sb.append(", ");
            }
        }

        sb.append(")");

        return sb.toString();
    }

    /**
     * Check if the specified table is existed.
     * 
     * @param p_tableName
     */
    public static boolean isTableExisted(String p_tableName)
    {
        Connection conn = null;
        try
        {
            conn = DbUtil.getConnection();
            return isTableExisted(conn, p_tableName);
        }
        catch (Exception e)
        {
            c_logger.error("Error when check table exists : " + p_tableName, e);
        }
        finally
        {
            DbUtil.silentReturnConnection(conn);
        }

        return false;
    }

    /**
     * Check if the specified table is existed.
     * 
     * @param p_connection
     * @param p_tableName
     */
    public static boolean isTableExisted(Connection p_connection,
            String p_tableName)
    {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ps = p_connection.prepareStatement(SQL_QUERY_TABLE);
            ps.setString(1, p_tableName);
            rs = ps.executeQuery();
            if (rs.next())
            {
                return true;
            }
        }
        catch (Exception e)
        {
            c_logger.warn("Error when check table exists : " + p_tableName, e);
        }
        finally
        {
            DbUtil.silentClose(rs);
            DbUtil.silentClose(ps);
        }

        return false;
    }
}
