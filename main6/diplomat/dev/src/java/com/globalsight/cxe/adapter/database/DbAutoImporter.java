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
package com.globalsight.cxe.adapter.database;

import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.database.source.TaskQueueTableMonitor;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.cxe.message.FileMessageData;
import com.globalsight.cxe.message.MessageDataFactory;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.Logger;
import com.globalsight.diplomat.util.Utility;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * Periodically polls the customer database for changes. Any changes are brought
 * into System4 as PRS XML files.
 */
public class DbAutoImporter extends TimerTask
{
    private static org.apache.log4j.Logger s_logger = org.apache.log4j.Logger
            .getLogger(DbAutoImporter.class);
    private TaskQueueTableMonitor m_monitor;
    private long m_delay = 1L * 60L * 1000L; // one minute delay
    private long m_period = 5L * 60L * 1000L; // two minute period

    /**
     * Creates a database AutoImporter
     */
    public DbAutoImporter()
    {
        m_monitor = new TaskQueueTableMonitor();
        setValues();
    }

    public long getDelay()
    {
        return m_delay;
    }

    public long getPeriod()
    {
        return m_period;
    }

    /**
     * Polls the DB and publishes PRSXML_IMPORTED events.
     */
    public void run()
    {
        Connection connection = null;
        try
        {
            s_logger.debug("Polling customer databases at " + new Date());
            Vector taskXmlVector = m_monitor.readDatabase();
            if (taskXmlVector.size() == 0)
            {
                s_logger.debug("Nothing returned from TaskQueueMonitor.");
                return;
            }
            s_logger.info("There are " + taskXmlVector.size()
                    + " tasks in the database.");
            connection = ConnectionPool.getConnection();
            AdapterResult[] results = createOutputMessages(taskXmlVector,
                    connection);
            CxeProxy.publishEvents(results, EventTopicMap.FOR_EXTRACTOR);
        }
        catch (Exception e)
        {
            // just log the error, do not publish an import error event
            s_logger.error("Could not import from the customer database.", e);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    ConnectionPool.returnConnection(connection);
                }
                catch (Exception cpe)
                {
                }
            }
        }
    }

    private AdapterResult[] createOutputMessages(Vector p_taskXmlVector,
            Connection p_connection) throws Exception
    {
        CxeMessageType msgType = CxeMessageType
                .getCxeMessageType(CxeMessageType.PRSXML_IMPORTED_EVENT);
        AdapterResult results[] = new AdapterResult[p_taskXmlVector.size()];
        for (int t = 0; t < p_taskXmlVector.size(); t++)
        {
            TaskXml taskxml = (TaskXml) p_taskXmlVector.elementAt(t);
            String prsXml = taskxml.getPaginatedResultSetXml();
            FileMessageData fmd = MessageDataFactory.createFileMessageData();
            BufferedOutputStream bos = new BufferedOutputStream(
                    fmd.getOutputStream());
            OutputStreamWriter osw = new OutputStreamWriter(bos, "UTF8");
            osw.write(prsXml, 0, prsXml.length());
            osw.close();

            CxeMessage cxeMessage = new CxeMessage(msgType);
            cxeMessage.setEventFlowXml(taskxml.getEventFlowXml());
            cxeMessage.setMessageData(fmd);
            Logger.writeDebugFile(Integer.toString(t) + "srcDB_ef.xml",
                    cxeMessage.getEventFlowXml());
            Logger.writeDebugFile(Integer.toString(t) + "srcDB_prs.xml",
                    cxeMessage.getMessageData());
            results[t] = new AdapterResult(cxeMessage);
            storePrsXml(prsXml, p_connection);
        }
        return results;
    }

    private void storePrsXml(String p_prsXml, Connection p_connection)
            throws Exception
    {
        PreparedStatement st = null;
        try
        {
            StringReader sr = new StringReader(p_prsXml);
            InputSource inputsource = new InputSource(sr);
            DOMParser parser = new DOMParser();
            parser.setFeature("http://xml.org/sax/features/validation", false); // don't
                                                                                // validate
            parser.parse(inputsource);
            Element elem = parser.getDocument().getDocumentElement();
            String id = elem.getAttribute("id");
            String quotedId = Utility.quote(id);
            p_connection.setAutoCommit(false);
            String sql = "INSERT INTO PRSXML_STORAGE VALUES(?, ?)";
            st = p_connection.prepareStatement(sql);
            st.setString(1, quotedId);
            // Insert Clob into MySql as String.
            st.setString(2, p_prsXml);
            st.executeUpdate();
            st.close();
            p_connection.commit();
            p_connection.setAutoCommit(true);
        }
        catch (Exception e)
        {
            try
            {
                if (p_connection != null)
                    p_connection.rollback();
            }
            catch (Exception e2)
            {
                s_logger.error("Could not rollback: ", e2);
            }

            throw e;
        }
        finally
        {
            if (st != null)
            {
                try
                {
                    st.close();
                }
                catch (Exception e1)
                {
                }
            }
        }
    }

    /**
     * Sets the delay and period and other values after reading
     * DatabaseAdapter.properties
     */
    private void setValues()
    {
        try
        {
            SystemConfiguration config = SystemConfiguration
                    .getInstance("/properties/DatabaseAdapter.properties");
            String delay = config.getStringParameter("delay");
            String period = config.getStringParameter("period");
            m_delay = Long.valueOf(delay).longValue() * 60L * 1000L;
            m_period = Long.valueOf(period).longValue() * 60L * 1000L;
            s_logger.info("Using a delay of " + delay
                    + " minutes and a period of " + period + " minutes.");
        }
        catch (Exception e)
        {
            s_logger.error(
                    "Could not get delay and period from DatabaseAdapter.properties. Using a 2-minute period.",
                    e);
            m_delay = 1L * 60L * 1000L; // one minute delay
            m_period = 5L * 60L * 1000L; // two minute period
        }
    }
}
