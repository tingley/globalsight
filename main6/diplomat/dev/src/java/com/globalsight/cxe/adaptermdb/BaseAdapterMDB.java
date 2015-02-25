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
package com.globalsight.cxe.adaptermdb;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.hornetq.api.jms.HornetQJMSConstants;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.adapter.cap.CapAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.AmbassadorServer;
import com.globalsight.everest.util.system.SystemControlTemplate;
import com.globalsight.log.ActivityLog;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * BaseAdapterMDB is a base class for all adapter message driven beans to
 * extend.
 */
public abstract class BaseAdapterMDB implements MessageDrivenBean,
        MessageListener
{
    private MessageDrivenContext m_context;
    private Logger m_logger;
    private BaseAdapter m_adapter;
    private QueueConnectionFactory m_queueConnectionFactory;
    private InitialContext m_initialContext;
    private HashMap m_cachedQueue = new HashMap();

    private static final Object LOCK = new Object();

    protected BaseAdapterMDB()
    {
    }

    //
    // Public Methods
    //

    /**
     * This method is required by the EJB Specification,
     */
    public void ejbActivate()
    {
    }

    /**
     * This method is required by the EJB Specification,
     * 
     */
    public void ejbRemove()
    {
    }

    /**
     * This method is required by the EJB Specification,
     * 
     */
    public void ejbPassivate()
    {
    }

    /**
     * Sets the session context.
     * 
     * @param ctx
     *            MessageDrivenContext Context for session
     */
    public void setMessageDrivenContext(MessageDrivenContext ctx)
    {
        m_context = ctx;
    }

    /**
     * This sets the adapter to use and the adapter name.
     */
    public void ejbCreate()
    {
        try
        {
            this.setLogger(this.getAdapterName());

            if (AmbassadorServer.isSystem4Accessible()
                    && SystemControlTemplate.areAllServerClassesLoaded())
            {
                m_logger.info("Creating MDB for " + this.getAdapterName());
            }
            else
            {
                m_logger.info("Delaying creation of MDB for "
                        + this.getAdapterName());

                while (AmbassadorServer.isSystem4Accessible() == false
                        && SystemControlTemplate.areAllServerClassesLoaded() == false)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }

                m_logger.info("Resuming creation of MDB for "
                        + this.getAdapterName());
            }

            this.setAdapter(this.loadAdapter());
            m_initialContext = new InitialContext();
            m_queueConnectionFactory = (QueueConnectionFactory) m_initialContext
                    .lookup(JmsHelper.JMS_QUEUE_FACTORY_NAME);
        }
        catch (Exception ex)
        {
            String msg = "Could not create MDB for " + this.getAdapterName();
            this.getLogger().error(msg, ex);
            throw new EJBException(msg);
        }
    }

    /**
     * Handles the AdapterResults. It is used if no JMS.
     * 
     * @param cxeMessage
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<CxeMessage> handlerAdapterResults(CxeMessage cxeMessage) throws Exception
    {
        setAdapter(loadAdapter());
        List<CxeMessage> cms = new ArrayList<CxeMessage>();
        String companyId =  CompanyThreadLocal.getInstance().getValue();
        m_adapter.loadConfiguration();
        m_adapter.loadProcessors();
        
        CxeMessage preProcessedMsg = m_adapter
                .runPreProcessor(cxeMessage);
        preProcessedMsg.getParameters().put(
                CompanyWrapper.CURRENT_COMPANY_ID, companyId);

        AdapterResult results[] = m_adapter
                .handleMessage(preProcessedMsg);

        for (int i = 0; results != null && i < results.length; i++)
        {
            CxeMessage newCxeMessage = results[i].cxeMessage;
            if (newCxeMessage != null)
            {
                newCxeMessage.getParameters().put(
                        CompanyWrapper.CURRENT_COMPANY_ID,
                        companyId);

                CxeMessage postProcessedMsg = m_adapter
                        .runPostProcessor(newCxeMessage);
                postProcessedMsg.getParameters().put(
                        CompanyWrapper.CURRENT_COMPANY_ID,
                        companyId);

                cms.add(postProcessedMsg);
            }
            
            List<CxeMessage> msgs = results[i].getMsgs();
            for (CxeMessage msg : msgs)
            {
                msg.getParameters().put(
                        CompanyWrapper.CURRENT_COMPANY_ID,
                        companyId);

                CxeMessage postProcessedMsg = m_adapter
                        .runPostProcessor(msg);
                postProcessedMsg.getParameters().put(
                        CompanyWrapper.CURRENT_COMPANY_ID,
                        companyId);

                cms.add(postProcessedMsg);
            }
        }
        
        return cms;
    }
    
    //
    // Implementation of Message Listener Methods
    //

    /**
     * Performs the actual function of the adapter.
     * 
     * @param msg
     *            JMS Message
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message msg)
    {
        QueueConnection queueConnection = null;
        CxeMessage cxeMessage = null;
        ActivityLog.Start activityStart = null;
        try
        {
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug("onMessage()==" + msg.getJMSMessageID());                
            }

            queueConnection = m_queueConnectionFactory.createQueueConnection();

            if (AmbassadorServer.isSystem4Accessible())
            {
                ObjectMessage jmsMessage = (ObjectMessage) msg;
                Serializable ob = jmsMessage.getObject();

                AdapterResult ars = null;
                if (ob instanceof CxeMessage)
                {
                    cxeMessage = (CxeMessage) ob;
                }
                else
                {
                    ars = (AdapterResult) ob;
                    cxeMessage = ars.getMsgs().get(0);
                }

                if (m_logger.isDebugEnabled())
                {
                    m_logger.debug("Handling msg "
                            + cxeMessage.getMessageType().getName());                    
                }

                String companyId = (String) cxeMessage.getParameters().get(
                        CompanyWrapper.CURRENT_COMPANY_ID);
                m_logger.debug("Company id get from the previous message is: "
                        + companyId);
                CompanyThreadLocal.getInstance().setIdValue(companyId);

                Map<Object, Object> activityArgs = new HashMap<Object, Object>();
                activityArgs.put("adapter", m_adapter.getClass().getName());
                activityArgs.put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);
                activityArgs.put("messageType", cxeMessage.getMessageType());
                activityStart = ActivityLog.start(BaseAdapterMDB.class,
                        "onMessage", activityArgs);

                // loading properties, For issue
                // "Properties files separation by Company"
                m_adapter.loadConfiguration();
                m_adapter.loadProcessors();

                if (ars != null)
                {
                    if (m_adapter instanceof CapAdapter
                            && CxeMessageType.GXML_CREATED_EVENT == cxeMessage
                                    .getMessageType().getValue())
                    {
                        CapAdapter adapter = (CapAdapter) m_adapter;
                        adapter.handleMessage(ars);
                    }
                    else
                    {
                        List<CxeMessage> msgs = new ArrayList<CxeMessage>();
                        for (CxeMessage m : ars.getMsgs())
                        {
                            CxeMessage preProcessedMsg = m_adapter
                                    .runPreProcessor(m);
                            preProcessedMsg.getParameters().put(
                                    CompanyWrapper.CURRENT_COMPANY_ID,
                                    companyId);

                            AdapterResult results[] = m_adapter
                                    .handleMessage(preProcessedMsg);
                            for (int i = 0; results != null
                                    && i < results.length; i++)
                            {
                                CxeMessage cm = results[i].cxeMessage;
                                if (cm != null)
                                {
                                    msgs.add(cm);
                                }
                                else
                                {
                                    msgs.addAll(results[i].getMsgs());
                                }
                            }
                        }

                        if (msgs.size() > 0)
                        {
                            Map<String, AdapterResult> jmsArs = new HashMap<String, AdapterResult>();

                            for (CxeMessage m : msgs)
                            {
                                m.getParameters().put(
                                        CompanyWrapper.CURRENT_COMPANY_ID,
                                        companyId);

                                CxeMessage pMsg = m_adapter.runPostProcessor(m);
                                pMsg.getParameters().put(
                                        CompanyWrapper.CURRENT_COMPANY_ID,
                                        companyId);

                                CxeMessageType eventName = pMsg
                                        .getMessageType();
                                String jmsTopicName = EventTopicMap
                                        .getJmsQueueName(eventName);

                                AdapterResult ar = jmsArs.get(jmsTopicName);
                                if (ar == null)
                                {
                                    ar = new AdapterResult();
                                    jmsArs.put(jmsTopicName, ar);
                                }

                                ar.addMsg(pMsg);
                            }

                            for (AdapterResult ar : jmsArs.values())
                            {
                                publishToJMS(ar, queueConnection);
                            }
                        }
                    }
                }
                else
                {
                    CxeMessage preProcessedMsg = m_adapter
                            .runPreProcessor(cxeMessage);
                    preProcessedMsg.getParameters().put(
                            CompanyWrapper.CURRENT_COMPANY_ID, companyId);

                    AdapterResult results[] = m_adapter
                            .handleMessage(preProcessedMsg);

                    for (int i = 0; results != null && i < results.length; i++)
                    {
                        CxeMessage newCxeMessage = results[i].cxeMessage;
                        if (newCxeMessage != null)
                        {
                            newCxeMessage.getParameters().put(
                                    CompanyWrapper.CURRENT_COMPANY_ID,
                                    companyId);

                            CxeMessage postProcessedMsg = m_adapter
                                    .runPostProcessor(newCxeMessage);
                            postProcessedMsg.getParameters().put(
                                    CompanyWrapper.CURRENT_COMPANY_ID,
                                    companyId);

                            publishToJMS(postProcessedMsg, queueConnection);
                        }
                        else
                        {
                            List<CxeMessage> msgs = results[i].getMsgs();
                            AdapterResult newResult = new AdapterResult();

                            for (CxeMessage m : msgs)
                            {
                                m.getParameters().put(
                                        CompanyWrapper.CURRENT_COMPANY_ID,
                                        companyId);

                                CxeMessage pMsg = m_adapter.runPostProcessor(m);
                                pMsg.getParameters().put(
                                        CompanyWrapper.CURRENT_COMPANY_ID,
                                        companyId);

                                newResult.addMsg(pMsg);
                            }

                            publishToJMS(newResult, queueConnection);
                        }
                    }
                }
            }
            else
            {
                // The system is being shut down, but JMS is still
                // delivering messages. Do not acknowlege this
                // message and hopefully it will be redelivered.
                throw new Exception(
                        "System is not ready. Cannot handle message "
                                + msg.getJMSMessageID());
            }
        }
        catch (Exception e)
        {
            getLogger().error("Problem handling JMS message", e);
        }
        finally
        {
            if (cxeMessage != null)
            {
                try
                {
                    cxeMessage.free();
                    m_logger.debug("freed message");
                }
                catch (IOException e)
                {
                    m_logger.error("free message error", e);
                }
            }

            if (queueConnection != null)
            {
                try
                {
                    queueConnection.close();
                }
                catch (Exception e)
                {
                    m_logger.error("close queueConnection error", e);
                }
            }

            HibernateUtil.closeSession();
            if (activityStart != null)
            {
                activityStart.end();
            }
        }
    }

    /**
     * Publishes the given CxeMessage to JMS
     * 
     * @param p_jmsTopicName
     *            the topic name to use
     * @param p_cxeMessage
     *            a CxeMessage object to publish
     * @exception Exception
     */
    private void publishToJMS(CxeMessage p_cxeMessage,
            QueueConnection queueConnection) throws Exception
    {
        QueueSession session = null;

        try
        {
            CxeMessageType eventName = p_cxeMessage.getMessageType();
            String jmsName = EventTopicMap.getJmsQueueName(eventName);

            m_logger.debug("Publishing event " + eventName + " to QUEUE "
                    + jmsName);

            session = queueConnection.createQueueSession(false,
                    HornetQJMSConstants.PRE_ACKNOWLEDGE);
            Queue queue = (Queue) m_cachedQueue.get(jmsName);
            if (queue == null)
            {
                queue = session.createQueue(jmsName);
                m_cachedQueue.put(jmsName, queue);
            }

            QueueSender sender = session.createSender(queue);
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            ObjectMessage om = session.createObjectMessage(p_cxeMessage);

            if (eventName.getValue() == CxeMessageType.DYNAMIC_PREVIEW_EVENT)
            {
                // Add the sessionId as a property to the object
                // message so it can be filtered by the export
                // servlet's message selector
                String sessionId = (String) p_cxeMessage.getParameters().get(
                        "SessionId");

                m_logger.info("Sending a dynamic preview event for sessionId "
                        + sessionId);

                if (sessionId != null && sessionId.length() > 1)
                {
                    om.setStringProperty("SESSIONID", sessionId);
                }
            }

            sender.send(om);
        }
        finally
        {
            if (session != null)
            {
                try
                {
                    session.close();
                }
                catch (Exception ex)
                {
                    m_logger.error("Error closing session", ex);
                }
            }
        }
    }

    /**
     * Publishes the given CxeMessage to JMS
     * 
     * @param p_jmsTopicName
     *            the topic name to use
     * @param p_cxeMessage
     *            a CxeMessage object to publish
     * @exception Exception
     */
    private void publishToJMS(AdapterResult ars, QueueConnection queueConnection)
            throws Exception
    {
        QueueSession session = null;

        try
        {
            List<CxeMessage> msgs = ars.getMsgs();
            CxeMessage msg = msgs.get(0);
            CxeMessageType eventName = msg.getMessageType();
            String jmsName = EventTopicMap.getJmsQueueName(eventName);

            m_logger.debug("Publishing event " + eventName + " to QUEUE "
                    + jmsName);

            session = queueConnection.createQueueSession(false,
                    HornetQJMSConstants.PRE_ACKNOWLEDGE);
            Queue queue = (Queue) m_cachedQueue.get(jmsName);
            if (queue == null)
            {
                queue = session.createQueue(jmsName);
                m_cachedQueue.put(jmsName, queue);
            }

            QueueSender sender = session.createSender(queue);
            sender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            ObjectMessage om = session.createObjectMessage(ars);

            if (eventName.getValue() == CxeMessageType.DYNAMIC_PREVIEW_EVENT)
            {
                // Add the sessionId as a property to the object
                // message so it can be filtered by the export
                // servlet's message selector
                String sessionId = (String) msg.getParameters()
                        .get("SessionId");

                m_logger.info("Sending a dynamic preview event for sessionId "
                        + sessionId);

                if (sessionId != null && sessionId.length() > 1)
                {
                    om.setStringProperty("SESSIONID", sessionId);
                }
            }

            sender.send(om);
        }
        finally
        {
            if (session != null)
            {
                try
                {
                    session.close();
                }
                catch (Exception ex)
                {
                }
            }
        }
    }

    //
    // Protected Methods
    //

    /**
     * Gets the session context.
     * 
     * @param ctx
     *            MessageDrivenContext Context for session
     */
    protected MessageDrivenContext getMessageDrivenContext()
    {
        return m_context;
    }

    /**
     * Gets the logger this AdapterMDB should use
     * 
     * @return Logger
     */
    protected Logger getLogger()
    {
        return m_logger;
    }

    /**
     * Sets the logger this AdapterMDB should use
     * 
     * @param p_categoryName
     *            a category name
     */
    protected void setLogger(String p_categoryName)
    {
        m_logger = Logger.getLogger(p_categoryName);
    }

    /**
     * Sets the adapter to use to actually handle messages
     * 
     * @param p_adapter
     *            desired adapter
     */
    protected void setAdapter(BaseAdapter p_adapter)
    {
        m_adapter = p_adapter;
    }

    /**
     * Gets the adapter this proxy uses
     * 
     * @return adapter
     */
    protected BaseAdapter getAdapter()
    {
        return m_adapter;
    }

    /**
     * Returns a String containing the adapter name. Like
     * "FileSystemSourceAdapter";
     * 
     * @return String
     */
    abstract protected String getAdapterName();

    /**
     * Creates and loads the appropriate BaseAdapter class
     * 
     * @return BaseAdapter
     */
    abstract protected BaseAdapter loadAdapter() throws Exception;
}
