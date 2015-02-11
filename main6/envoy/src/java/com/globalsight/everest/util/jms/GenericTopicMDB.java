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

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.naming.Context;

import org.apache.log4j.Logger;

import com.globalsight.util.j2ee.AppServerWrapper;
import com.globalsight.util.j2ee.AppServerWrapperFactory;

/**
 * Abstract base class for topic based Message Driven Beans
 */
public abstract class GenericTopicMDB implements MessageDrivenBean, MessageListener
{
    protected static AppServerWrapper s_appServerWrapper =
        AppServerWrapperFactory.getAppServerWrapper();

    protected Logger m_logger;
    protected MessageDrivenContext m_messageDrivenContext;
    protected TopicConnectionFactory m_topicConnectionFactory;
    protected TopicConnection m_topicConnection;
    protected Context m_initialContext;

    /**
     * Protected constructor allows base classes to create an MDB using their
     * own logging object.
     *
     * @param p_logger -- the logger to use
     */
    protected GenericTopicMDB(Logger p_logger)
    {
        m_logger = p_logger;
    }

    /**
     * Activates the EJB
     */
    public void ejbActivate() {
        try {
            m_topicConnection = m_topicConnectionFactory.createTopicConnection();
            m_topicConnection.start();
        }
        catch (Exception e)
        {
            m_logger.error("Could not establish JMS topic connection.",e);
        }
    }

    /**
     * Called when the EJB is removed.
     *
     */
    public void ejbRemove() {}

    /**
     * Called when the EJB is made passive.
     *
     */
    public void ejbPassivate() {
        try {
            m_topicConnection.close();
        }
        catch (Exception e)
        {
            m_logger.error("Problem closing JMS topic connection.",e);
        }
    }

    /**
     * Sets the session context.
     *
     * @param ctx               MessageDrivenContext Context for session
     */
    public void setMessageDrivenContext(MessageDrivenContext ctx) {
      m_messageDrivenContext = ctx;
    }


    /**
     * Creates the EJB
     */
    public void ejbCreate () {
        try{
            m_initialContext = s_appServerWrapper.getNamingContext();
            m_topicConnectionFactory =
                (TopicConnectionFactory) m_initialContext.lookup(
                    JmsHelper.JMS_TOPIC_FACTORY_NAME);
        }
        catch (Exception e)
        {
            String msg = "Could not create MDB: " + e.getMessage();
            m_logger.error(msg,e);
            throw new EJBException(msg);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: MessageListener implementation
    //////////////////////////////////////////////////////////////////////
    /**
     * Does whatever the MDB should do when receiving a message from JMS.
     *
     * @param p_message - JMS Message
     */
    public abstract void onMessage(Message p_message);
}

