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
package com.globalsight.util.mail;

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
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * The MailSenderMDB is a message driven bean that handles sending email
 * asynchronously.
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_MAIL_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class MailSenderMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = -6749239337126583933L;
    // for logging purposes
    private static Logger s_logger = Logger.getLogger(MailSenderMDB.class);

    // ////////////////////////////////////
    // Constructor //
    // ////////////////////////////////////
    public MailSenderMDB()
    {
        super(s_logger);

    }

    // ////////////////////////////////////
    // Public Methods //
    // ////////////////////////////////////

    /**
     * Receives a message to send an email and uses the MailSender class to
     * actually send it. This allows email to be sent asynchronously
     * 
     * @param p_cxeRequest
     *            The JMS message containing the hashtable with email args
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_msg)
    {
        try
        {
            ObjectMessage msg = (ObjectMessage) p_msg;
            HashMap args = (HashMap) msg.getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) args.get(CompanyWrapper.CURRENT_COMPANY_ID));

            String to = (String) args.get("to");
            String from = (String) args.get("from");
            String cc = (String) args.get("cc");
            String bcc = (String) args.get("bcc");
            String subject = (String) args.get("subject");
            String text = (String) args.get("text");
            String[] attachments = (String[]) args.get("attachments");
            s_logger.info("Sending email to " + to + " for '" + subject + "'");
            MailSender.getInstance().sendMail(from, to, cc, bcc, subject, text,
                    attachments);
        }
        catch (Exception e)
        {
            s_logger.error(e.getLocalizedMessage(), e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
