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
package com.globalsight.cxe.util.mail;

import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.mail.MailSender;

/**
 * Class {@code MailSenderUtil} is used for sending emails without using JMS.
 * 
 * @since GBS-4400
 */
public class MailSenderUtil
{
    static private final Logger logger = Logger.getLogger(MailSenderUtil.class);

    /**
     * Sends an email and uses the MailSender class to actually send it,
     * asynchronously with thread instead of JMS.
     */
    static public void sendMailWithThread(Map<String, Object> data)
    {
        MailSenderRunnable runnable = new MailSenderRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Sends an email and uses the MailSender class to actually send it,
     * synchronously.
     */
    static public void sendMail(Map<String, Object> p_data)
    {
        try
        {
            String to = (String) p_data.get("to");
            String from = (String) p_data.get("from");
            String cc = (String) p_data.get("cc");
            String bcc = (String) p_data.get("bcc");
            String subject = (String) p_data.get("subject");
            String text = (String) p_data.get("text");
            String[] attachments = (String[]) p_data.get("attachments");

            logger.info("Sending email to " + to + " for '" + subject + "'");
            MailSender.getInstance().sendMail(from, to, cc, bcc, subject, text, attachments);
        }
        catch (Exception e)
        {
            logger.error("Failed to send email.", e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }
}
