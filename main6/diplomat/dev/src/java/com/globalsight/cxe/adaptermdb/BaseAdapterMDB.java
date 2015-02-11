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

import java.util.HashMap;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;

import com.globalsight.cxe.adapter.AdapterResult;
import com.globalsight.cxe.adapter.BaseAdapter;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.message.CxeMessageType;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.system.AmbassadorServer;
import com.globalsight.everest.util.system.SystemControlTemplate;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * BaseAdapterMDB is a base class for all adapter message driven beans
 * to extend.
 */
public abstract class BaseAdapterMDB
    implements MessageDrivenBean, MessageListener
{
    //
    // Private Members
    //

    private MessageDrivenContext m_context;
    private GlobalSightCategory m_logger;
    private BaseAdapter m_adapter;
    private TopicConnectionFactory m_topicConnectionFactory;
    private TopicConnection m_topicConnection;
    private InitialContext m_initialContext;
    private HashMap m_cachedTopics = new HashMap();

    //
    // Protected Members
    //

    static public final String FACTORY_NAME =
        "com.globalsight.jms.GlobalSightTopicConnectionFactory";

    //
    // Constructor
    //

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
        try
        {
            m_topicConnection = m_topicConnectionFactory.createTopicConnection();
            m_topicConnection.start();
        }
        catch (Exception ex)
        {
            m_logger.error("Could not establish JMS topic connection in ejbActivate().",
                ex);
        }
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
        try
        {
            m_topicConnection.close();
        }
        catch (Exception ex)
        {
            m_logger.error("Problem closing JMS topic connection.", ex);
        }
    }

    /**
     * Sets the session context.
     *
     * @param ctx MessageDrivenContext Context for session
     */
    public void setMessageDrivenContext(MessageDrivenContext ctx)
    {
        m_context = ctx;
    }


    /**
     * This sets the adapter to use and the adapter name.
     */
    public void ejbCreate ()
    {
        try
        {
            this.setLogger(this.getAdapterName());

            if (AmbassadorServer.isSystem4Accessible() &&
                SystemControlTemplate.areAllServerClassesLoaded())
            {
                m_logger.info("Creating MDB for " + this.getAdapterName());
            }
            else
            {
                m_logger.info("Delaying creation of MDB for " + this.getAdapterName());

                while (AmbassadorServer.isSystem4Accessible() == false &&
                    SystemControlTemplate.areAllServerClassesLoaded() == false)
                {
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ex)
                    {
                    }
                }

                m_logger.info("Resuming creation of MDB for " + this.getAdapterName());
            }

            this.setAdapter(this.loadAdapter());
            m_initialContext = new InitialContext();
            m_topicConnectionFactory =
                (TopicConnectionFactory) m_initialContext.lookup(FACTORY_NAME);
            m_topicConnection = m_topicConnectionFactory.createTopicConnection();
            m_topicConnection.start();
        }
        catch (Exception ex)
        {
            String msg = "Could not create MDB for " + this.getAdapterName();
            this.getLogger().error(msg, ex);
            throw new EJBException(msg);
        }
    }


    //
    // Implementation of Message Listener Methods
    //

    /**
     * Performs the actual function of the adapter.
     *
     * @param msg JMS Message
     */
    public void onMessage(Message msg)
    {
        boolean outgoingMessagesPublished = false;

        try
        {
            m_logger.debug("onMessage()==" + msg.getJMSMessageID());

            if (AmbassadorServer.isSystem4Accessible())
            {
                ObjectMessage jmsMessage = (ObjectMessage) msg;
                CxeMessage cxeMessage = (CxeMessage) jmsMessage.getObject();

                m_logger.debug("Handling msg " +
                    cxeMessage.getMessageType().getName());

                String companyId = (String) cxeMessage.getParameters().get(CompanyWrapper.CURRENT_COMPANY_ID);
                m_logger.debug("Company id get from the previous message is: " + companyId);
                CompanyThreadLocal.getInstance().setIdValue(companyId);
                
                // loading properties, For issue "Properties files separation by Company"
                m_adapter.loadConfiguration();
                m_adapter.loadProcessors();
                
                CxeMessage preProcessedMsg = m_adapter.runPreProcessor(cxeMessage);
                preProcessedMsg.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);
                
                AdapterResult results[] = m_adapter.handleMessage(preProcessedMsg);
                for (int i = 0; results != null && i < results.length; i++)
                {
                    CxeMessage newCxeMessage = results[i].cxeMessage;
                    newCxeMessage.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);
                    
                    CxeMessage postProcessedMsg =
                        m_adapter.runPostProcessor(newCxeMessage);
                    postProcessedMsg.getParameters().put(CompanyWrapper.CURRENT_COMPANY_ID, companyId);

                    publishToJMS(postProcessedMsg);
                    outgoingMessagesPublished = true;
                }

                cxeMessage.free();

                m_logger.debug("freed message");
            }
            else
            {
                // The system is being shut down, but JMS is still
                // delivering messages.  Do not acknowlege this
                // message and hopefully it will be redelivered.
                throw new Exception("System is not ready. Cannot handle message " +
                    msg.getJMSMessageID());
            }
        }
        catch (Exception ex)
        {
            if (outgoingMessagesPublished)
            {
                getLogger().error("Problem handling JMS message, but NOT rolling back.", ex);
            }
            else
            {
                getLogger().error("Problem handling JMS message, rolling back.", ex);

                try
                {
                    m_context.setRollbackOnly();
                }
                catch (Exception e2)
                {
                    getLogger().error("Problem rolling back:", e2);
                }
            }
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Publishes the given CxeMessage to JMS
     *
     * @param p_jmsTopicName the topic name to use
     * @param p_cxeMessage a CxeMessage object to publish
     * @exception Exception
     */
    private void publishToJMS(CxeMessage p_cxeMessage)
        throws Exception
    {
        TopicSession session = null;

        try
        {
            CxeMessageType eventName = p_cxeMessage.getMessageType();
            String jmsTopicName = EventTopicMap.getJmsTopicName(eventName);

            m_logger.debug("Publishing event " + eventName +
                " to topic " + jmsTopicName);

            Topic topic = (Topic) m_cachedTopics.get(jmsTopicName);
            if (topic == null)
            {
                topic = (Topic) m_initialContext.lookup(jmsTopicName);
                m_cachedTopics.put(jmsTopicName,topic);
            }
            
            //the topic connection pools the sessions already
            session = m_topicConnection.createTopicSession(false,
                Session.CLIENT_ACKNOWLEDGE);
            TopicPublisher sender = session.createPublisher(topic);
            ObjectMessage om = session.createObjectMessage(p_cxeMessage);

            if (eventName.getValue() == CxeMessageType.DYNAMIC_PREVIEW_EVENT)
            {
                // Add the sessionId as a property to the object
                // message so it can be filtered by the export
                // servlet's message selector
                String sessionId = (String) p_cxeMessage.getParameters().get(
                    "SessionId");

                m_logger.info("Sending a dynamic preview event for sessionId " +
                    sessionId);

                if (sessionId != null && sessionId.length() > 1)
                {
                    om.setStringProperty("SESSIONID",sessionId);
                }
            }

            sender.publish(om);
        }
        finally
        {
            if (session != null)
            {
                try
                {
                    session.close();
                }
                catch (Exception ex){}
            }
        }
    }

    //
    // Protected Methods
    //

    /**
     * Gets the session context.
     *
     * @param ctx MessageDrivenContext Context for session
     */
    protected MessageDrivenContext getMessageDrivenContext()
    {
        return m_context;
    }


    /**
     * Gets the logger this AdapterMDB should use
     *
     * @return GlobalSightCategory
     */
    protected GlobalSightCategory getLogger()
    {
        return m_logger;
    }

    /**
     * Sets the logger this AdapterMDB should use
     *
     * @param p_categoryName a category name
     */
    protected void setLogger(String p_categoryName)
    {
        m_logger = (GlobalSightCategory)GlobalSightCategory.
            getLogger(p_categoryName);
    }

    /**
     * Sets the adapter to use to actually handle messages
     *
     * @param p_adapter desired adapter
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
     * Returns a String containing the adapter name.
     * Like "FileSystemSourceAdapter";
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

