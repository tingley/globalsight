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
package com.globalsight.cxe.util.terminology;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Class {@code TermbaseDeleterUtil} is used for deleting TB data without using
 * JMS.
 * 
 * @since GBS-4400
 */
public class TermbaseDeleterUtil
{
    static private final Logger logger = Logger.getLogger(TermbaseDeleterUtil.class);

    // Delete 100 rows at a time.
    static private final int BATCHSIZE = 100;

    /**
     * Performs the necessary deletes on term base asynchronously with thread
     * instead of JMS.
     */
    static public void deleteTermbaseWithThread(Map<String, Long> data)
    {
        TermbaseDeleterRunnable runnable = new TermbaseDeleterRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Performs the necessary deletes on term base synchronously.
     */
    static public void deleteTermbase(Map<String, Long> p_data)
    {
        long tbid = (Long) p_data.get("tbid");
        Connection conn = null;
        try
        {
            logger.info("Asynchronously deleting termbase " + tbid + ".");

            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            // Methods will commit their statements.
            deleteTerms(conn, tbid);
            deleteLanguages(conn, tbid);
            deleteConcepts(conn, tbid);
            deleteUserData(conn, tbid);

            // No commit necessary.
            logger.info("Asynchronous deletion of termbase " + tbid + " done.");
        }
        catch (Exception e)
        {
            logger.error("Termbase deletion error for TB " + tbid + ", ignoring.", e);
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.rollback();
                }
            }
            catch (Exception e)
            {
                logger.error("Failed to rollback connection.", e);
            }
            DbUtil.silentReturnConnection(conn);
            HibernateUtil.closeSession();
        }
    }

    private static void deleteTerms(Connection p_conn, long p_tbid) throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn
                    .prepareStatement("select min(cid), max(cid) from TB_TERM " + "where tbid = ?");
            stmt.setLong(1, p_tbid);

            rset = stmt.executeQuery();

            if (rset.next())
            {
                min = rset.getLong(1);
                max = rset.getLong(2);
            }

            rset.close();
            stmt.close();

            if (max > 0)
            {
                stmt = p_conn.prepareStatement(
                        "delete from TB_TERM " + "where tbid = ? and cid >= ? and cid <= ?");

                for (long i = min; i <= max; i += BATCHSIZE)
                {
                    stmt.setLong(1, p_tbid);
                    stmt.setLong(2, i);
                    stmt.setLong(3, i + BATCHSIZE);

                    stmt.executeUpdate();

                    p_conn.commit();
                }
            }
        }
        finally
        {
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
        }
    }

    private static void deleteLanguages(Connection p_conn, long p_tbid) throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn.prepareStatement(
                    "select min(cid), max(cid) from TB_LANGUAGE " + "where tbid = ?");
            stmt.setLong(1, p_tbid);

            rset = stmt.executeQuery();

            if (rset.next())
            {
                min = rset.getLong(1);
                max = rset.getLong(2);
            }

            rset.close();
            stmt.close();

            if (max > 0)
            {
                stmt = p_conn.prepareStatement(
                        "delete from TB_LANGUAGE " + "where tbid = ? and cid >= ? and cid <= ?");

                for (long i = min; i <= max; i += BATCHSIZE)
                {
                    stmt.setLong(1, p_tbid);
                    stmt.setLong(2, i);
                    stmt.setLong(3, i + BATCHSIZE);

                    stmt.executeUpdate();

                    p_conn.commit();
                }

                stmt.close();
            }
        }
        finally
        {
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
        }
    }

    private static void deleteConcepts(Connection p_conn, long p_tbid) throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn.prepareStatement(
                    "select min(cid), max(cid) from TB_CONCEPT " + "where tbid = ?");
            stmt.setLong(1, p_tbid);

            rset = stmt.executeQuery();

            if (rset.next())
            {
                min = rset.getLong(1);
                max = rset.getLong(2);
            }

            rset.close();
            stmt.close();

            if (max > 0)
            {
                stmt = p_conn.prepareStatement(
                        "delete from TB_CONCEPT " + "where tbid = ? and cid >= ? and cid <= ?");

                for (long i = min; i <= max; i += BATCHSIZE)
                {
                    stmt.setLong(1, p_tbid);
                    stmt.setLong(2, i);
                    stmt.setLong(3, i + BATCHSIZE);

                    stmt.executeUpdate();

                    p_conn.commit();
                }

                stmt.close();
            }
        }
        finally
        {
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
        }
    }

    private static void deleteUserData(Connection p_conn, long p_tbid) throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn.prepareStatement(
                    "select min(type), max(type) from TB_USER_DATA " + "where tbid = ?");
            stmt.setLong(1, p_tbid);

            rset = stmt.executeQuery();

            if (rset.next())
            {
                min = rset.getLong(1);
                max = rset.getLong(2);
            }

            rset.close();
            stmt.close();

            if (max > 0)
            {
                stmt = p_conn.prepareStatement(
                        "delete from TB_USER_DATA " + "where tbid = ? and type = ?");

                for (long i = min; i <= max; i++)
                {
                    stmt.setLong(1, p_tbid);
                    stmt.setLong(2, i);

                    stmt.executeUpdate();

                    p_conn.commit();
                }

                stmt.close();
            }
        }
        finally
        {
            DbUtil.silentClose(rset);
            DbUtil.silentClose(stmt);
        }
    }
}
