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
package com.globalsight.ling.tm3.core.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Simple routines to simplify common SQL tasks.
 */
public class SQLUtil
{
    private static Logger SQL_LOGGER = Logger.getLogger(SQLUtil.class);

    private static final String myClassName = SQLUtil.class.getName();

    public static Logger getLogger()
    {
        return SQL_LOGGER;
    }

    /**
     * Return the ID of the last auto_inc field incremented in this connection.
     */
    public static long getLastInsertId(Connection conn) throws SQLException
    {
        PreparedStatement ps = conn.prepareStatement("SELECT LAST_INSERT_ID()");
        logStatement(findLabel(), "SELECT LAST_INSERT_ID()");
        ResultSet rs = SQLUtil.execQuery(ps);
        rs.next();
        long l = rs.getLong(1);
        ps.close();
        log("LAST_INSERT_ID is " + l);
        return l;
    }

    /**
     * Exec a statement. If logging is enabled, examine the stack to find the
     * calling method.
     * 
     * @param conn
     * @param sql
     * @throws SQLException
     */
    public static void exec(Connection conn, String sql) throws SQLException
    {
        String label = null;
        exec(conn, sql, findLabel());
    }

    /**
     * Exec a statement, using the specified label for logging purposes.
     * 
     * @param conn
     * @param sql
     * @param label
     * @throws SQLException
     */
    public static void exec(Connection conn, String sql, String label)
            throws SQLException
    {
        logStatement(label, sql);
        Statement s = conn.createStatement();
        Timer t = new Timer();
        s.executeUpdate(sql);
        s.close();
        logTimer(t);
    }

    public static ResultSet execQuery(Statement statement, String sql)
            throws SQLException
    {
        String label = null;
        return execQuery(statement, sql, findLabel());
    }

    public static ResultSet execQuery(Statement statement, String sql,
            String label) throws SQLException
    {
        logStatement(label, sql);
        Timer t = new Timer();
        ResultSet rs = statement.executeQuery(sql);
        logTimer(t);
        return rs;
    }

    public static ResultSet execQuery(PreparedStatement ps) throws SQLException
    {
        return execQuery(ps, findLabel());
    }

    public static ResultSet execQuery(PreparedStatement ps, String label)
            throws SQLException
    {
        logStatement(label, ps);
        Timer t = new Timer();
        ResultSet rs = ps.executeQuery();
        logTimer(t);
        return rs;
    }

    /**
     * Executes a query that whose ResultSet returns a single column containing
     * a long. Return the row results as a List.
     */
    public static List<Long> execIdsQuery(Connection conn, StatementBuilder sb)
            throws SQLException
    {
        return execIdsQuery(conn, sb, findLabel());
    }

    public static List<Long> execIdsQuery(Connection conn, StatementBuilder sb,
            String label) throws SQLException
    {
        logStatement(label, sb);
        PreparedStatement ps = sb.toPreparedStatement(conn);
        Timer t = new Timer();
        ResultSet rs = ps.executeQuery();
        List<Long> ids = new ArrayList<Long>();
        while (rs.next())
        {
            ids.add(rs.getLong(1));
        }
        ps.close();
        logTimer(t);
        return ids;
    }

    /**
     * Executes a query that whose ResultSet returns a single column. Return the
     * row results as a List.
     */
    public static List<Object> execObjectsQuery(Connection conn,
            StatementBuilder sb) throws SQLException
    {
        return execObjectsQuery(conn, sb, findLabel());
    }

    public static List<Object> execObjectsQuery(Connection conn,
            StatementBuilder sb, String label) throws SQLException
    {
        logStatement(label, sb);
        PreparedStatement ps = sb.toPreparedStatement(conn);
        Timer t = new Timer();
        ResultSet rs = ps.executeQuery();
        List<Object> ids = new ArrayList<Object>();
        while (rs.next())
        {
            ids.add(rs.getObject(1));
        }
        ps.close();
        logTimer(t);
        return ids;
    }

    /**
     * Executes a query that whose ResultSet returns a single row consisting of
     * a single column containing a long. Return this value as a long.
     * 
     * @return single row value, or 0 if no results were returned
     */
    public static long execCountQuery(Connection conn, StatementBuilder sb)
            throws SQLException
    {
        return execCountQuery(conn, sb, findLabel());
    }

    public static long execCountQuery(Connection conn, StatementBuilder sb,
            String label) throws SQLException
    {
        logStatement(label, sb);
        PreparedStatement ps = sb.toPreparedStatement(conn);
        Timer t = new Timer();
        ResultSet rs = ps.executeQuery();
        long count = 0;
        if (rs.next())
        {
            count = rs.getLong(1);
        }
        ps.close();
        logTimer(t);
        return count;
    }

    /**
     * Executes a batch PreparedStatement with executeUpdate() and then closes
     * the statement.
     * 
     * @param conn
     * @param sb
     * @throws SQLException
     */
    public static void execBatch(Connection conn, BatchStatementBuilder sb)
            throws SQLException
    {
        execBatch(conn, sb, findLabel());
    }

    public static void execBatch(Connection conn, BatchStatementBuilder sb,
            String label) throws SQLException
    {
        sb.setRequestKeys(false);
        logStatement(label, sb);
        PreparedStatement ps = sb.toPreparedStatement(conn);
        Timer t = new Timer();
        ps.executeBatch();
        logTimer(t);
        ps.close();
    }

    /**
     * Exec the statement contained in a StatementBuilder. If logging is
     * enabled, examine the stack to find the calling method.
     * 
     * @param conn
     * @param sb
     * @throws SQLException
     */
    public static void exec(Connection conn, AbstractStatementBuilder sb)
            throws SQLException
    {
        exec(conn, sb, findLabel());
    }

    /**
     * Exec the statement contained in a Statement builder, using the specified
     * label for logging purposes.
     * 
     * @param conn
     * @param sb
     * @param label
     * @throws SQLException
     */
    public static void exec(Connection conn, AbstractStatementBuilder sb,
            String label) throws SQLException
    {
        logStatement(label, sb);
        PreparedStatement ps = sb.toPreparedStatement(conn);
        Timer t = new Timer();
        ps.execute();
        logTimer(t);
        ps.close();
    }

    // Does not check for empty list
    public static StringBuilder longGroup(List<Long> ids)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(" (").append(ids.get(0));
        for (int i = 1; i < ids.size(); i++)
        {
            sb.append(", ").append(ids.get(i));
        }
        sb.append(") ");
        return sb;
    }

    private static void logStatement(String label, Statement s)
    {
        log(label + ">> " + s);
    }

    private static void logStatement(String label, AbstractStatementBuilder sb)
    {
        log(label + ">> " + sb);
    }

    private static void logStatement(String label, String s)
    {
        log(label + ">> " + s);
    }

    private static void logTimer(Timer timer)
    {
        log("Operation took " + timer.getDuration() + "ms");
    }

    private static void log(String s)
    {
        SQL_LOGGER.debug(s);
    }

    private static String findLabel()
    {
        if (!SQL_LOGGER.isDebugEnabled())
        {
            return null;
        }
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // Walk upwards to find the first non-SQLUtil caller (skip 0, which is
        // Thread)
        for (int i = 1; i < stack.length; i++)
        {
            if (!stack[i].getClassName().equals(myClassName))
            {
                return stack[i].getMethodName();
            }
        }
        return "Unknown";
    }

    private static class Timer
    {
        private long start;

        Timer()
        {
            start = System.currentTimeMillis();
        }

        long getDuration()
        {
            return (System.currentTimeMillis() - start);
        }
    }

	public static List<List<Long>> toBatchList(List<Long> ids,
			int batchSize)
    {
        List<List<Long>> batchList = new ArrayList<List<Long>>();
        if (ids == null)
        {
            return batchList;
        }

        List<Long> subList = new ArrayList<Long>();
        int count = 0;
        for (Long obj : ids)
        {
        	subList.add(obj);
        	count++;
        	if (count == batchSize)
        	{
        		batchList.add(subList);
        		subList = new ArrayList<Long>();
        		count = 0;
        	}
        }

        if (subList.size() > 0)
        {
        	batchList.add(subList);
        }

        return batchList;
    }

	public static void execBatchDelete(Connection conn, String sql,
			List<List<Long>> batchList) throws SQLException
    {
        int batchCount = batchList.size();
        if (batchCount > 1)
        {
        	SQL_LOGGER.info(batchCount + " batches of records found to be deleted.");
        }
        int deletedBatchCount = 0;
        for (List<Long> list : batchList)
        {
            exec(conn, sql + toInClause(list));
            if (batchCount > 1)
            {
                deletedBatchCount++;
                int leftBatchCount = batchCount - deletedBatchCount;
                String message = "";
                if (deletedBatchCount == 1)
                {
                    if (leftBatchCount == 1)
                    {
                        message = "1 batch deleted, left 1";
                    }
                    else
                    {
                        message = "1 batch deleted, left " + leftBatchCount;
                    }
                }
                else
                {
                    if (leftBatchCount == 1)
                    {
                        message = deletedBatchCount
                                + " batches deleted, left 1";
                    }
                    else if (leftBatchCount > 1)
                    {
                        message = deletedBatchCount + " batches deleted, left "
                                + leftBatchCount;
                    }
                }
                if (leftBatchCount > 0)
                {
                	SQL_LOGGER.info(message);
                }
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static String toInClause(List<?> list)
    {
        StringBuilder in = new StringBuilder();
        if (list.size() == 0)
            return "(0)";
        
        in.append("(");
        for (Object o : list)
        {
            if (o instanceof List)
            {
                if (((List) o).size() == 0)
                    continue;
                
                for (Object id : (List<?>) o)
                {
                    if (id instanceof String)
                    {
                        in.append("'");
                        in.append(((String) id).replace("\'", "\\\'"));
                        in.append("'");
                    }
                    else
                    {
                        in.append(id);
                    }
                    in.append(",");
                }
            }
            else if (o instanceof String)
            {
                in.append("'");
                in.append(((String) o).replace("\'", "\\\'"));
                in.append("'");
                in.append(",");
            }
            else
            {
                in.append(o);
                in.append(",");
            }
        }
        in.deleteCharAt(in.length() - 1);
        in.append(")");

        return in.toString();
    }
}
