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

//JDK
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ssl.SSLException;

import org.apache.log4j.Logger;

import com.globalsight.everest.util.system.SystemConfiguration;

/**
 * This class actually wrap the javax.mail stuff to send emails.
 */

public class MailSender
{
    private static Logger s_logger = Logger.getLogger(MailSender.class
            .getName());

    private static MailSender s_instance = null;
    private static String lock = new String("lock");

    // mail session
    private Session m_session;
    private static String HOST = "mail.smtp.host";
    private static String SMTP_AUTH = "mail.smtp.auth";
    private static String MAIL_SERVER = "mailserver";
    private static String CHARSET_UTF8 = "UTF-8";
    private static String MAIL_SMTP = "smtp";

    // setttings
    private String m_transportProtocol = "";
    private String m_mailServer = "";
    private String m_mailUser = "";
    private String m_mailPwd = "";

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////////////////
    /**
     * Construct a MailSender for sending email.
     * 
     * @exception MailerException
     *                - wraps a ConfigException, and GeneralException.
     */
    private MailSender() throws MailerException
    {
        setupSession();
    }

    // ////////////////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////////////////

    static public MailSender getInstance() throws MailerException
    {
        synchronized (lock)
        {
            if (s_instance == null)
                s_instance = new MailSender();
        }
        return s_instance;
    }

    // basic setup - getting mail server used for the JavaMail Session.
    private void setupSession() throws MailerException
    {
        Context initCtx;
        try
        {
            initCtx = new InitialContext();
            try
            {
                m_session = (Session) initCtx.lookup("java:jboss/mail/Default");
            }
            catch (NamingException e)
            {
                throw new MailerException(
                        MailerException.MSG_FAILED_TO_RETRIEVE_CONFIG_PARAM,
                        null, e);
            }

            if (m_session != null)
            {
                m_transportProtocol = m_session
                        .getProperty("mail.transport.protocol");
                m_mailServer = m_session.getProperty("mail.smtp.host");
                m_mailUser = m_session.getProperty("mail.smtp.user");
                SystemConfiguration sc = SystemConfiguration.getInstance();
                m_mailPwd = sc
                        .getStringParameter("mailserver.account.password");
            }
        }
        catch (NamingException e)
        {
            throw new MailerException(
                    MailerException.MSG_FAILED_TO_RETRIEVE_CONFIG_PARAM, null,
                    e);
        }
        catch (Exception ce)
        {
            s_logger.error("Error getting email authentication parameters", ce);
            throw new MailerException(
                    MailerException.MSG_FAILED_TO_RETRIEVE_CONFIG_PARAM, null,
                    ce);
        }
    }

    // perform authentication if it's required.
    private Authenticator getAuthenticator(boolean authEnabled)
    {
        return authEnabled ? new EmailAuthenticator() : null;
    }

    /*
     * Add the attachments to the message. Convert the string attachments to
     * files for attachment to the email message.
     */
    private void addAttachments(Multipart p_msgPart, String[] p_attachments)
            throws Exception
    {
        for (int i = 0; i < p_attachments.length; i++)
        {
            // Add each attachment
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            // create the file and write out the string
            File file = new File(p_attachments[i]);
            String fileName = "Attachment" + i;
            DataSource source = null;
            if (!file.exists())
            {
                // create the file and write out the string
                File temp = File.createTempFile("GS" + fileName, "xml");
                FileOutputStream streamOut = new FileOutputStream(temp);
                streamOut.write(p_attachments[i].getBytes());
                source = new FileDataSource(temp);
            }
            else
            {
                // add the file to the email
                fileName = file.getName();
                source = new FileDataSource(file.getAbsoluteFile());
            }

            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            p_msgPart.addBodyPart(messageBodyPart);
        }
    }

    // get the SMTP mail server from the property file.
    private String getSMTPServer() throws MailerException
    {
        String smtpServer = null;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            smtpServer = sc.getStringParameter(MAIL_SERVER);
        }
        catch (Exception ce) // ConfigException and GeneralException
        {
            throw new MailerException(
                    MailerException.MSG_FAILED_TO_RETRIEVE_CONFIG_PARAM, null,
                    ce);
        }

        return smtpServer;
    }

    // Parse the given comma separated sequence of addresses into
    // InternetAddress objects.
    // Addresses must follow RFC822
    private InternetAddress[] parse(String p_addresslist)
    {
        try
        {
            return InternetAddress.parse(p_addresslist);
        }
        catch (AddressException ae)
        {
            return new InternetAddress[0];
        }
    }

    /*
     * Send the specified message to the person(s) specified.
     * 
     * @param p_sendFrom - The email address of the sender.
     * 
     * @param p_sendTo - The email address(es) where the message will be sent
     * to.
     * 
     * @param p_sendCc - The email address(es) of the "carbon copy"
     * recipient(s).
     * 
     * @param p_sendBcc - The email address(es) of "blind carbon copy"
     * recipient(s).
     * 
     * @param p_subject - The subject of this email.
     * 
     * @param p_message - The text that is the message's content.
     * 
     * @param p_attachments - Attachments to add to the email. They are strings
     * that are turned into files for attaching. This can be null if no
     * attachments.
     * 
     * @exception MailerException - wraps JavaMail's MessagingException.
     */
    public void sendMail(String p_sendFrom, String p_sendTo, String p_sendCc,
            String p_sendBcc, String p_subject, String p_message,
            String[] p_attachments) throws MailerException
    {
        try
        {
            // create a MIME style email message.
            MimeMessage msg = new MimeMessage(m_session);
            try
            {
                // sender's address (from...)
                msg.setFrom(new InternetAddress(p_sendFrom));
            }
            catch (Exception e)
            {
                s_logger.warn("Unable to send to mail from " + p_sendFrom
                        + "; FROM email was invalid");
                return;
            }
            // set the "To" recepients
            if (!setRecipients(p_sendTo, msg, Message.RecipientType.TO))
            {
                s_logger.warn("Unable to send to mail from " + p_sendFrom
                        + " to " + p_sendTo + "; TO email was invalid");
                return;
            }
            // set the "CC" recepients
            if (p_sendCc != null && !"".equals(p_sendCc)
                    && !"null".equalsIgnoreCase(p_sendCc))
                setRecipients(p_sendCc, msg, Message.RecipientType.CC);
            // set the "BCC" recepients
            if (p_sendBcc != null && !"".equals(p_sendBcc)
                    && !"null".equalsIgnoreCase(p_sendBcc))
                setRecipients(p_sendBcc, msg, Message.RecipientType.BCC);

            // set the subject and date
            msg.setSubject(p_subject, CHARSET_UTF8);
            msg.setSentDate(new Date());

            // Send mail with both plain text and HTML
            MimeMultipart multipart = new MimeMultipart("alternative");
            MimeBodyPart textPart = new MimeBodyPart();
            MimeBodyPart htmlPart = new MimeBodyPart();
            textPart.setText(MailerHelper.getTextContext(p_message),
                    CHARSET_UTF8);
            htmlPart.setDataHandler(new DataHandler(MailerHelper
                    .getHTMLContext(p_message), "text/html;charset=\"UTF-8\""));
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(htmlPart);

            // if attachments were specified
            if (p_attachments != null && p_attachments.length > 0)
            {
                addAttachments(multipart, p_attachments);
            }

            // Put parts in message
            msg.setContent(multipart);

            // now send the message...
            // Transport.send(msg);
            Transport tr = null;
            try
            {
                tr = m_session.getTransport(m_transportProtocol);
                tr.connect(m_mailServer, m_mailUser, m_mailPwd);
            }
            catch (Exception e)
            {
                String mark = "plaintext connection";
                Throwable cause = e.getCause();
                String emsg = e.getMessage() == null ? "" : e.getMessage();
                String causeMsg = (cause == null || cause.getMessage() == null) ? ""
                        : cause.getMessage();
                if ((e instanceof SSLException || cause instanceof SSLException)
                        && (emsg.contains(mark) || causeMsg.contains(mark)))
                {
                    s_logger.info("use smtp to send mail.");
                    tr = m_session.getTransport(MAIL_SMTP);
                    tr.connect(m_mailServer, m_mailUser, m_mailPwd);
                }
                else
                {
                    throw e;
                }
            }
            msg.saveChanges();
            tr.sendMessage(msg, msg.getAllRecipients());
            tr.close();
        }
        catch (Exception e)
        {
            String[] args = new String[3];
            args[0] = p_sendFrom;
            String sendTo = p_sendTo;
            if ((p_sendCc != null) && (!p_sendCc.equals(""))
                    && !"null".equalsIgnoreCase(p_sendCc))
            {
                sendTo = sendTo + " and Cc to: " + p_sendCc;
            }
            if ((p_sendBcc != null) && (!p_sendBcc.equals(""))
                    && !"null".equalsIgnoreCase(p_sendBcc))
            {
                sendTo = sendTo + " and Bcc to: " + p_sendBcc;
            }
            args[1] = sendTo;
            args[2] = p_subject;
            throw new MailerException(MailerException.MSG_FAILED_TO_SEND_EMAIL,
                    args, e);

        }
    }

    // set the recipients of the email (To, CC, or BCC)
    private boolean setRecipients(String p_recipients, MimeMessage p_mimeMsg,
            Message.RecipientType p_type) throws MessagingException
    {
        InternetAddress[] recipients = parse(p_recipients);
        if (recipients.length > 0)
        {
            p_mimeMsg.setRecipients(p_type, recipients);
        }
        else
        {
            // Parsing failures a fine unless we don't have anybody to
            // send the message to
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("Unable to parse recipients of type " + p_type
                        + ": " + p_recipients);                
            }

            if (p_type == Message.RecipientType.TO)
            {
                return false;
            }
        }
        return true;
    }

    /*
     * // for testing only... public static void main(String args[]) throws
     * Exception { String from = "dragade@transware.com"; String to =
     * "dhananjay.ragade@transware.com"; String cc = ""; String subject =
     * "Test"; String text = "Hello World"; MailSender ms =
     * MailSender.getInstance(); ms.sendMail(from,to,cc, null, subject, text,
     * null); }
     */
}
