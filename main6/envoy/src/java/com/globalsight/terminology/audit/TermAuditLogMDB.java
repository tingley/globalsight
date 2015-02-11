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
package com.globalsight.terminology.audit;

import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.jms.GenericTopicMDB;

import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.UTC;
import com.globalsight.ling.common.Text;

import com.globalsight.diplomat.util.database.ConnectionPool;

import org.apache.log4j.spi.LoggingEvent;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.StringTokenizer;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;

/**
 * The TermAuditLogMDB receives JMS messages based on the use of
 * the term audit log4j logging category (TermAuditLog.AUDIT_LOG_NAME)
 *
 * These JMS messages are parsed and then a record is inserted into
 * the tb_audit_log table for each message.
 */
public class TermAuditLogMDB extends GenericTopicMDB
{
    private static final String SQL_INSERT =
        "insert into tb_audit_log(event_date,username,termbase,target,languages,action,details) values(?,?,?,?,?,?,?)";

    // for logging purposes
    private static GlobalSightCategory s_logger =
        (GlobalSightCategory) GlobalSightCategory.getLogger(TermAuditLogMDB.class);

    private static final int MAX_DETAILS_LEN=3999;

    //////////////////////////////////////
    // Constructor                      //
    //////////////////////////////////////
    public TermAuditLogMDB()
    {
        super(s_logger);
    }

    //////////////////////////////////////
    // Public Methods                   //
    //////////////////////////////////////

    /**
     * This method gets invoked each time a terminology audit message
     * is sent through JMS. This MDB updates the database table
     * for terminology audit purposes.
     *
     * The String message is assumed to be of the format:
     * event_date|username|termbase|target|languages|action|details
     *
     * Dates are in UTC.
     *
     * For example:
     * 2000-02-16T15:56:00|joe|myTB|entry1|English,French|create|created tb myTB
     *
     * @param p_msg The JMS message containing the audit message
     *
     * @see com.globalsight.util.UTC
     */
    public void onMessage(Message p_msg)
    {
        Connection c = null;
        PreparedStatement pst = null;

        try
        {
            ObjectMessage msg = (ObjectMessage)p_msg;
            LoggingEvent le = (LoggingEvent)msg.getObject();
            String s = (String) le.getMessage();

            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Received jms msg: " + s);
            }

            //we expect to parse the string into 7 fields
            String toks[] = s.split(TermAuditLog.REGEXP_SEP,7);
            String eventDate = toks[0];
            String username = toks[1];
            String termbase = toks[2];
            String item = toks[3];
            String langs = toks[4];
            String action= toks[5];
            String details = toks[6];

            //truncate details if it is too long for the column
            if (Text.getUTF8Len(details) > MAX_DETAILS_LEN)
                details = details.substring(0,MAX_DETAILS_LEN -1);
            
            c = ConnectionPool.getConnection();
            pst = c.prepareStatement(SQL_INSERT);
            Date date = UTC.parse(eventDate);
            pst.setTimestamp(1, new Timestamp(date.getTime()));
            pst.setString(2, username);
            pst.setString(3, termbase);
            pst.setString(4, item);
            pst.setString(5, langs);
            pst.setString(6, action);
            pst.setString(7, details);
            pst.executeUpdate();
            c.commit();
        }
        catch (Exception e)
        {
            if (c != null)
            {
                try { c.rollback(); } catch (Throwable ignore) {}
            }

            s_logger.error("Problem handling term audit:", e);
        }
        finally
        {
            ConnectionPool.silentClose(pst);
            ConnectionPool.silentReturnConnection(c);
        }
    }
}

