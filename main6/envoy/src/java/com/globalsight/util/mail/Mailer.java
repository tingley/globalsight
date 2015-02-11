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


// JDK
import java.util.List;
import java.util.Locale;

import java.rmi.RemoteException;
// GlobalSight
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.User;



/**
 * Provides an interface for emailing.
 */
public interface Mailer 
{
    /**
     * The public name bound to the remote object.
     */
    public static final String SERVICE_NAME = "Mailer";


    /**
     * Retrieve a list of EmailInformation objects for the specified user ids.
     * @param p_userIds A list of user ids to retrieve email addresses.
     * @return A list of EmailInformation objects.
     */
    List getEmailAddresses(String[] p_userIds)
    	throws MailerException, RemoteException;
    
    /**
     * Send the specified message to the person(s) specified.
     * @param  p_sendFromEmailInfo - The email info of the sender.
     * @param  p_recipientEmailInfo - The email info of the recipient.    
     * @param  p_subjectKey - The subject key of this email.
     * @param  p_messageKey - The text key for the message's content.
     * @param  p_messageArguments - An array of strings that provide the arguments
     *                              for the email message text. 
     * @param  p_companyIdStr - Company ID String                                 
     * @exception MailerException - wraps JavaMail's MessagingException.
     */
    void sendMail(EmailInformation p_sendFromEmailInfo, 
                  EmailInformation p_recipientEmailInfo,
                  String p_subjectKey,
                  String p_messageKey, String[] p_messageArguments,
                  String p_companyIdStr)
        throws MailerException, RemoteException;
    
    
    /**
     * Modify on Mailer.sendMail(EmailInformation, EmailInformation, String, String, String[], String)
     */
    void sendMail(String p_sendFromUserId, EmailInformation p_recipient, String p_subjectKey,
                  String p_messageKey, String[] p_messageArguments, 
                  String p_companyIdStr)
        throws MailerException, RemoteException;

    /**
     * Sends Email with attachment.
     * 
     * @param p_sendFromEmailInfo
     *            Email Sender
     * @param p_recipientEmailInfo
     *            Email Recipient
     * @param p_subjectKey
     *            Email Subject key
     * @param p_messageKey
     *            Email Body key
     * @param p_messageArguments
     *            Email Subject and Body arguments
     * @param p_attachments
     *            Email Attachment
     * @param p_companyIdStr
     *            Company ID string
     * @throws MailerException
     * @throws RemoteException
     */
    public void sendMail(EmailInformation p_sendFromEmailInfo,
            EmailInformation p_recipientEmailInfo, String p_subjectKey,
            String p_messageKey, String[] p_messageArguments,
            String[] p_attachments, long p_companyId)
            throws MailerException, RemoteException;
    /**
     * Send an email to a recipient from the system admin.  Since only
     * the recipient's email address is known, the server locale would
     * be used for formatting and display purposes.
     * @param p_recipient - The recipient's email address.
     * @param p_messageArguments - The arguments that will be added to the email message.
     * @param p_subjectKey - The subject key which is the key in the resource bundle.
     * @param p_messageKey - The message key which is the key in resource bundle.
     *
     * @exception MailerException - wraps JavaMail's MessagingException.
     */
    void sendMailFromAdmin(String p_recipient, String[] p_messageArguments,
                           String p_subjectKey, String p_messageKey, 
                           String p_companyId)
        throws MailerException, RemoteException;

    
    /**
     * Send an email to a recipient from the system admin.  This method is
     * used for system notifications (i.e. during an import/export).
     *
     */
    void sendMailFromAdmin(User p_recipient, String[] p_messageArguments,
                           String p_subjectKey, String p_messageKey, 
                           String p_companyIdStr)
        throws MailerException, RemoteException;
    

    /**
     * Send an email to the system administrator.  This method is
     * used for system notifications (i.e. during an import/export)
     * where the project manager could not be identified.
     * Attachments can be added to the email message.  They are
     * strings that are converted into file attachments.
     */
    void sendMailToAdmin(String[] p_messageArguments, String p_subjectKey,
                         String p_messageKey, String[] p_attachments, 
                         String p_companyIdStr)
        throws MailerException, RemoteException;
    
    /**
     * Determines whether the system-wide notification is enabled
     */
    boolean isSystemNotificationEnabled() throws MailerException, RemoteException;
}
