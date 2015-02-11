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

package com.globalsight.terminology;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.terminology.util.SqlUtil;

/**
 * Helper class for TermbaseManager.deleteTermbase() that deletes a termbase
 * asynchronously. It does so by carefully deleteing data in chunks so that
 * database's rollback segments do not overflow.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_TERMBASE_DELETION_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class TermbaseDeleterMDB extends GenericQueueMDB
{
    private static final Logger CATEGORY = Logger
            .getLogger(TermbaseDeleterMDB.class);

    // Delete 100 rows at a time.
    static private final int BATCHSIZE = 100;

    // import com.globalsight.everest.util.jms.JmsHelper;
    // JmsHelper.sendMessageToQueue((Serializable)hashmap,
    // JmsHelper.JMS_TERMBASE_DELETION_QUEUE);

    public TermbaseDeleterMDB()
    {
        super(CATEGORY);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_message)
    {
        Long tbid = new Long(-1);

        try
        {
            HashMap map = (HashMap) ((ObjectMessage) p_message).getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) map.get(CompanyWrapper.CURRENT_COMPANY_ID));

            String action = (String) map.get("action");

            if ("delete_termbase".equals(action))
            {
                tbid = (Long) map.get("tbid");

                deletePhysicalTermbase(tbid);
            }
        }
        catch (Throwable ex)
        {
            // Unexpected error: roll back the JMS transaction to make
            // the message come back.
            CATEGORY.error("Termbase deletion error for TB " + tbid
                    + ", ignoring", ex);

            m_messageDrivenContext.setRollbackOnly();
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Helper method for TermbaseManager.deleteTermbase() that performs the
     * necessary deletes in the SQL database.
     */
    private void deletePhysicalTermbase(Long p_tbid) throws SQLException
    {
        Connection conn = null;

        try
        {
            CATEGORY.info("Asynchronously deleting termbase " + p_tbid + ".");

            conn = SqlUtil.hireConnection();
            conn.setAutoCommit(false);

            // Methods will commit their statements.
            deleteTerms(conn, p_tbid);
            deleteLanguages(conn, p_tbid);
            deleteConcepts(conn, p_tbid);
            deleteUserData(conn, p_tbid);

            // No commit necessary.

            CATEGORY.info("Asynchronous deletion of termbase " + p_tbid
                    + " done.");
        }
        finally
        {
            try
            {
                if (conn != null)
                    conn.rollback();
            }
            catch (Throwable t)
            { /* ignore */
            }

            SqlUtil.fireConnection(conn);
        }
    }

    private void deleteTerms(Connection p_conn, Long p_tbid)
            throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn
                    .prepareStatement("select min(cid), max(cid) from TB_TERM "
                            + "where tbid = ?");
            stmt.setLong(1, p_tbid.longValue());

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
                stmt = p_conn.prepareStatement("delete from TB_TERM "
                        + "where tbid = ? and cid >= ? and cid <= ?");

                for (long i = min; i <= max; i += BATCHSIZE)
                {
                    stmt.setLong(1, p_tbid.longValue());
                    stmt.setLong(2, i);
                    stmt.setLong(3, i + BATCHSIZE);

                    stmt.executeUpdate();

                    p_conn.commit();
                }
            }
        }
        finally
        {
            ConnectionPool.silentClose(rset);
            ConnectionPool.silentClose(stmt);
        }
    }

    private void deleteLanguages(Connection p_conn, Long p_tbid)
            throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn
                    .prepareStatement("select min(cid), max(cid) from TB_LANGUAGE "
                            + "where tbid = ?");
            stmt.setLong(1, p_tbid.longValue());

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
                stmt = p_conn.prepareStatement("delete from TB_LANGUAGE "
                        + "where tbid = ? and cid >= ? and cid <= ?");

                for (long i = min; i <= max; i += BATCHSIZE)
                {
                    stmt.setLong(1, p_tbid.longValue());
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
            ConnectionPool.silentClose(rset);
            ConnectionPool.silentClose(stmt);
        }
    }

    private void deleteConcepts(Connection p_conn, Long p_tbid)
            throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn
                    .prepareStatement("select min(cid), max(cid) from TB_CONCEPT "
                            + "where tbid = ?");
            stmt.setLong(1, p_tbid.longValue());

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
                stmt = p_conn.prepareStatement("delete from TB_CONCEPT "
                        + "where tbid = ? and cid >= ? and cid <= ?");

                for (long i = min; i <= max; i += BATCHSIZE)
                {
                    stmt.setLong(1, p_tbid.longValue());
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
            ConnectionPool.silentClose(rset);
            ConnectionPool.silentClose(stmt);
        }
    }

    private void deleteUserData(Connection p_conn, Long p_tbid)
            throws SQLException
    {
        long min = 0, max = 0;
        PreparedStatement stmt = null;
        ResultSet rset = null;
        try
        {
            stmt = p_conn
                    .prepareStatement("select min(type), max(type) from TB_USER_DATA "
                            + "where tbid = ?");
            stmt.setLong(1, p_tbid.longValue());

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
                stmt = p_conn.prepareStatement("delete from TB_USER_DATA "
                        + "where tbid = ? and type = ?");

                for (long i = min; i <= max; i++)
                {
                    stmt.setLong(1, p_tbid.longValue());
                    stmt.setLong(2, i);

                    stmt.executeUpdate();

                    p_conn.commit();
                }

                stmt.close();
            }
        }
        finally
        {
            ConnectionPool.silentClose(rset);
            ConnectionPool.silentClose(stmt);
        }
    }
}
