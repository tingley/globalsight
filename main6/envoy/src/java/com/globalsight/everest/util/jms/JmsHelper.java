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

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.hornetq.api.jms.HornetQJMSConstants;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * Utility class for dealing with JMS
 */
public class JmsHelper
{
    private static final Logger s_logger = Logger.getLogger(JmsHelper.class);

    // /////////////////////////
    // Public JMS Constants //
    // /////////////////////////
    public static final String JMS_QUEUE_FACTORY_NAME = "java:/JmsXA";
    public static final String JMS_TOPIC_FACTORY_NAME = JMS_QUEUE_FACTORY_NAME;

    public static final String JMS_TYPE_QUEUE = "javax.jms.Queue";
    public static final String JMS_TYPE_TOPIC = "javax.jms.Topic";

    public static final String MAX_SESSION_SIZE = "30";

    public static final String JMS_ALIGNER_QUEUE = "com.globalsight.cxe.jms.Aligner";
    public static final String JMS_IMPORTING_QUEUE = "com.globalsight.cxe.jms.CapImporting";
    public static final String JMS_EXPORTING_QUEUE = "com.globalsight.everest.jms.CapExporting";
    public static final String JMS_UPLOAD_QUEUE = "com.globalsight.everest.jms.FileUpload";
    public static final String JMS_SCHEDULING_QUEUE = "com.globalsight.jms.FluxEventScheduling";
    public static final String JMS_CALCULATE_COST_QUEUE = "com.globalsight.everest.jms.CostCalculations";
    public static final String JMS_WORKFLOW_ADDITION_QUEUE = "com.globalsight.everest.jms.WorkflowAdditions";
    public static final String JMS_PROJECT_UPDATE_QUEUE = "com.globalsight.everest.jms.ProjectUpdate";
    public static final String JMS_TERMBASE_DELETION_QUEUE = "com.globalsight.everest.jms.TermbaseDeletion";
    public static final String JMS_TRASH_COMPACTION_QUEUE = "com.globalsight.everest.jms.TrashCompaction";
    public static final String JMS_MAIL_QUEUE = "com.globalsight.everest.jms.MailSender";
    public static final String JMS_NEW_COMPANY_QUEUE = "com.globalsight.everest.jms.NewCompany";
    public static final String JMS_CANCEL_JOB_QUEUE = "com.globalsight.everest.jms.JobCancel";
    public static final String JMS_CANCEL_WORKFLOW_QUEUE = "com.globalsight.everest.jms.WorkflowCancel";
    public static final String JMS_ADD_SOURCE_FILE_QUEUE = "com.globalsight.everest.jms.AddSourceFile";

    private static AppServerWrapper s_appServer = AppServerWrapperFactory
            .getAppServerWrapper();

    private static Context s_context = s_appServer.getNamingContext();
    private static TopicConnectionFactory s_tcf = null;
    private static QueueConnectionFactory s_qcf = null;

    static
    {
        try
        {
            s_tcf = (TopicConnectionFactory) s_context
                    .lookup(JMS_TOPIC_FACTORY_NAME);
            s_qcf = (QueueConnectionFactory) s_context
                    .lookup(JMS_QUEUE_FACTORY_NAME);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Cannot initialize JmsHelper "
                    + e.getMessage());
        }
    }

    /**
     * Sends an ObjectMessage to the given JMS topic
     * 
     * @param p_msg
     *            serializable Object to insert into an ObjectMessage
     * @param p_jmsTopic
     *            topic name
     * @exception JMSException
     * @exception NamingException
     */
    public static void sendMessageToTopic(Serializable p_msg, String p_jmsTopic)
            throws JMSException, NamingException
    {
        // This is JBOSS specified, when jboss bind the JMS destination(Queue or
        // Topic) in to naming context,
        // it will automatically add the prefix "topic/queue" ahead of the JNDI
        // name, such as:
        // "topic/com.globalsight.cxe.jms.ForExtractor".
        // So if the GlobalSight works on JBOSS, we need add the prefix manually
        // when lookup the JMS destination.

        if (s_appServer.getJ2EEServerName().equals(
                AppServerWrapperFactory.JBOSS))
        {
            p_jmsTopic = EventTopicMap.TOPIC_PREFIX_JBOSS + p_jmsTopic;
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Sending message to topic " + p_jmsTopic);
        }

        TopicConnection topicConnection = null;
        try
        {
            topicConnection = s_tcf.createTopicConnection();
            TopicSession session = topicConnection.createTopicSession(false,
                    HornetQJMSConstants.PRE_ACKNOWLEDGE);
            Topic topic = session.createTopic(p_jmsTopic);
            TopicPublisher sender = session.createPublisher(topic);
            ObjectMessage om = session.createObjectMessage(p_msg);
            sender.publish(om);
        }
        finally
        {
            closeConnection(topicConnection);
        }
    }

    /**
     * Sends an ObjectMessage to the given JMS queue
     * 
     * @param p_msg
     *            serializable Object to insert into an ObjectMessage
     * @param p_jmsTopic
     *            topic name
     * @exception JMSException
     * @exception NamingException
     */
    public static void sendMessageToQueue(Serializable p_msg, String p_jmsQueue)
            throws JMSException, NamingException
    {
        // This is JBOSS specified, when jboss bind the JMS destination(Queue or
        // Topic) in to naming context,
        // it will automatically add the prefix "topic/queue" ahead of the JNDI
        // name, such as:
        // "topic/com.globalsight.cxe.jms.ForExtractor".
        // So if the GlobalSight works on JBOSS, we need add the prefix manually
        // when lookup the JMS destination.
        if (s_appServer.getJ2EEServerName().equals(
                AppServerWrapperFactory.JBOSS))
        {
            p_jmsQueue = EventTopicMap.QUEUE_PREFIX_JBOSS + p_jmsQueue;
        }

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("Sending message to queue " + p_jmsQueue);
        }

        QueueConnection queueConnection = null;
        try
        {
            queueConnection = s_qcf.createQueueConnection();
            QueueSession session = queueConnection.createQueueSession(false,
                    HornetQJMSConstants.PRE_ACKNOWLEDGE);
            Queue queue = session.createQueue(p_jmsQueue);
            QueueSender sender = session.createSender(queue);
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            ObjectMessage om = session.createObjectMessage(p_msg);
            sender.send(om);
        }
        finally
        {
            closeConnection(queueConnection);
        }
    }

    private static void closeConnection(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (JMSException e)
            {
                s_logger.error("Error closing connection", e);
            }
        }
    }
}
