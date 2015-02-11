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
package com.globalsight.everest.util.jms;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;
import com.globalsight.webservices.WebServiceException;

/**
 * Utility class for dealing with JMS
 */
public class JmsHelper
{
    private static final Logger s_logger =
        Logger.getLogger(
            JmsHelper.class);

    ///////////////////////////
    //  Public JMS Constants //
    ///////////////////////////
    public static final String JMS_QUEUE_FACTORY_NAME =
        "com.globalsight.jms.GlobalSightQueueConnectionFactory";
    public static final String JMS_TOPIC_FACTORY_NAME =
        "com.globalsight.jms.GlobalSightTopicConnectionFactory";

    public static final String JMS_ALIGNER_QUEUE =
        "com.globalsight.cxe.jms.Aligner";
    public static final String JMS_IMPORTING_QUEUE =
        "com.globalsight.cxe.jms.CapImporting";
    public static final String JMS_EXPORTING_QUEUE =
        "com.globalsight.everest.jms.CapExporting";
    public static final String JMS_UPLOAD_QUEUE =
        "com.globalsight.everest.jms.FileUpload";
    public static final String JMS_SCHEDULING_QUEUE =
        "com.globalsight.jms.FluxEventScheduling";
    public static final String JMS_CALCULATE_COST_QUEUE =
        "com.globalsight.everest.jms.CostCalculations";
    public static final String JMS_WORKFLOW_ADDITION_QUEUE =
        "com.globalsight.everest.jms.WorkflowAdditions";
    public static final String JMS_PROJECT_UPDATE_QUEUE =
        "com.globalsight.everest.jms.ProjectUpdate";
    public static final String JMS_TERMBASE_DELETION_QUEUE =
        "com.globalsight.everest.jms.TermbaseDeletion";
    public static final String JMS_TRASH_COMPACTION_QUEUE =
        "com.globalsight.everest.jms.TrashCompaction";
    public static final String JMS_MAIL_QUEUE =
        "com.globalsight.everest.jms.MailSender";
    public static final String JMS_NEW_COMPANY_QUEUE =
        "com.globalsight.everest.jms.NewCompany";
    public static final String JMS_CANCEL_JOB_QUEUE =
        "com.globalsight.everest.jms.JobCancel";
    public static final String JMS_CANCEL_WORKFLOW_QUEUE =
        "com.globalsight.everest.jms.WorkflowCancel";
    public static final String JMS_UPDATE_lEVERAGE_MATCH_QUEUE =
        "com.globalsight.everest.jms.UpdateLeverageMatchMDB";
    public static final String JMS_ADD_SOURCE_FILE_QUEUE =
        "com.globalsight.everest.jms.AddSourceFile";

    private static AppServerWrapper s_appServer =
        AppServerWrapperFactory.getAppServerWrapper();

    private static TopicSession s_topicSession = getTopicSession();
    private static QueueSession s_queueSession = getQueueSession();


    /**
     * Sends an ObjectMessage to the given JMS topic
     *
     * @param p_msg serializable Object to insert into an ObjectMessage
     * @param p_jmsTopic topic name
     * @exception JMSException
     * @exception NamingException
     */
    public static void sendMessageToTopic(Serializable p_msg,
        String p_jmsTopic)
        throws JMSException, NamingException
    {
        // This is JBOSS specified, when jboss bind the JMS destination(Queue or Topic) in to naming context,
        // it will automatically add the prefix "topic/queue" ahead of the JNDI name, such as: 
        // "topic/com.globalsight.cxe.jms.ForExtractor".  
        // So if the GlobalSight works on JBOSS, we need add the prefix manually when lookup the JMS destination.
        
        if (s_appServer.getJ2EEServerName().equals(AppServerWrapperFactory.JBOSS)) {
            p_jmsTopic = EventTopicMap.TOPIC_PREFIX_JBOSS + p_jmsTopic;
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Sending message to topic " + p_jmsTopic);
        }

        Context context = s_appServer.getNamingContext();
        Topic topic = (Topic) context.lookup(p_jmsTopic);
        TopicPublisher sender = s_topicSession.createPublisher(topic);
        ObjectMessage om = s_topicSession.createObjectMessage(p_msg);

        sender.publish(om);
    }

    /**
     * Sends an ObjectMessage to the given JMS queue
     *
     * @param p_msg serializable Object to insert into an ObjectMessage
     * @param p_jmsTopic topic name
     * @exception JMSException
     * @exception NamingException
     */
    public static void sendMessageToQueue(Serializable p_msg,
        String p_jmsQueue)
        throws JMSException, NamingException
    {
    	p_jmsQueue = getFreeQueue(p_jmsQueue);
    	
        // This is JBOSS specified, when jboss bind the JMS destination(Queue or Topic) in to naming context,
        // it will automatically add the prefix "topic/queue" ahead of the JNDI name, such as: 
        // "topic/com.globalsight.cxe.jms.ForExtractor".  
        // So if the GlobalSight works on JBOSS, we need add the prefix manually when lookup the JMS destination.
        if (s_appServer.getJ2EEServerName().equals(AppServerWrapperFactory.JBOSS)) {
            p_jmsQueue = EventTopicMap.QUEUE_PREFIX_JBOSS + p_jmsQueue;

        	s_logger.info("Sending message to '" + p_jmsQueue + "'");
        }
    	
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Sending message to queue " + p_jmsQueue);
        }

        Context context = s_appServer.getNamingContext();
        Queue queue = (Queue) context.lookup(p_jmsQueue);
        QueueSender sender = s_queueSession.createSender(queue);
        ObjectMessage om = s_queueSession.createObjectMessage(p_msg);

        sender.send(om);
        sender.close();
    }


    /**
     * Gets a JMS topic session
     *
     * @return JMS TopicSession
     * @exception JMSException
     * @exception NamingException
     */
    private static TopicSession getTopicSession()
    {
        try
        {
            Context context = s_appServer.getNamingContext();
            TopicConnectionFactory cf = (TopicConnectionFactory)
                context.lookup(JMS_TOPIC_FACTORY_NAME);
            TopicConnection topicConnection = cf.createTopicConnection();
            topicConnection.start();
            TopicSession session = topicConnection.createTopicSession(
                false, Session.CLIENT_ACKNOWLEDGE);

            return session;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot initialize JmsHelper " +
                e.getMessage());
        }
    }

    /**
     * Gets a JMS queue session
     *
     * @return JMS TopicSession
     * @exception JMSException
     * @exception NamingException
     */
    private static QueueSession getQueueSession()
    {
        try
        {
            Context context = s_appServer.getNamingContext();
            QueueConnectionFactory cf = (QueueConnectionFactory)
                context.lookup(JMS_QUEUE_FACTORY_NAME);
            QueueConnection queueConnection = cf.createQueueConnection();
            queueConnection.start();
            QueueSession session = queueConnection.createQueueSession(
                false, Session.CLIENT_ACKNOWLEDGE);

            return session;
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot initialize JmsHelper " +
                e.getMessage());
        }
    }
    
    /**
     * Get the freest queue name for performance balance purpose
     * @return the freest destination name (queue)
     */
    private static String getFreeQueue(String p_jmsQueue) 
    {
//    	if ( p_jmsQueue.equals(JMS_IMPORTING_QUEUE) || p_jmsQueue.equals(JMS_EXPORTING_QUEUE) ) {
    	if ( p_jmsQueue.equals(JMS_IMPORTING_QUEUE)) {
            int queue1 = 0;
            int queue2 = 0;
            int queue3 = 0;
            int queue4 = 0;
            int queue5 = 0;
            
            Connection connection = null;
            PreparedStatement query = null;
            ResultSet results = null;
            String sql = "select messageid, destination, txid, txop from jms_messages where destination like 'QUEUE." + p_jmsQueue + "%'";;

            try {
            	connection = ConnectionPool.getConnection();
            	query = connection.prepareStatement(sql);
            	results = query.executeQuery();
            	while (results != null && results.next()) {
            		String dest = results.getString("DESTINATION");
            		if ( dest.equals("QUEUE." + p_jmsQueue)) {
            			queue1++;
            		} else if (dest.equals("QUEUE." + p_jmsQueue + "2")) {
            			queue2++;
            		} else if (dest.equals("QUEUE." + p_jmsQueue + "3")) {
            			queue3++;
            		} else if (dest.equals("QUEUE." + p_jmsQueue + "4")) {
            			queue4++;
            		} else if (dest.equals("QUEUE." + p_jmsQueue + "5")) {
            			queue5++;
            		}
            	}

                HashMap queueMap = new HashMap();
                queueMap.put(queue1, p_jmsQueue);
                queueMap.put(queue2, p_jmsQueue+"2");
                queueMap.put(queue3, p_jmsQueue+"3");
                queueMap.put(queue4, p_jmsQueue+"4");
                queueMap.put(queue5, p_jmsQueue+"5");
                
            	int[] intArr = {queue1, queue2, queue3, queue4, queue5};
            	Arrays.sort(intArr);
            	p_jmsQueue = (String) queueMap.get(intArr[0]);
            } catch (Exception e) { 
            	//do nothing
            } finally {
                releaseDBResource(results, query, connection);
            }
    	}

    	return p_jmsQueue;
    }

    private static void releaseDBResource(ResultSet results, PreparedStatement query,
            Connection connection)
    {
        // close ResultSet
        if (results != null) {
            try {
                results.close();
            } catch (Exception e) {
                s_logger.error("Closing ResultSet", e);
            }
        }
        // close PreparedStatement
        if (query != null) {
            try {
                query.close();
            } catch (Exception e) {
                s_logger.error("Closing query", e);
            }
        }
        // close Connection
        returnConnection(connection);
    }
    
    private static void returnConnection(Connection p_connection)
    {
        try {
            ConnectionPool.returnConnection(p_connection);
        } catch (Exception cpe){ }
    }
}
