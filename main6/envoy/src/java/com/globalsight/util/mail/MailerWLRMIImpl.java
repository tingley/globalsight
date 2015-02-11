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
import com.globalsight.everest.util.system.RemoteServer;




/**
 * This class represents the remote implementation of a mailer interface.
 */

public class MailerWLRMIImpl
    extends RemoteServer
    implements MailerWLRemote
{
    
    // PRIVATE MEMBER VARIABLES
    MailerLocal m_localInstance = null;

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Construct a remote mailer object.
     */
    public MailerWLRMIImpl()
        throws RemoteException, MailerException
    {
        super(Mailer.SERVICE_NAME); 
        m_localInstance = new MailerLocal();        
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////


    //////////////////////////////////////////////////////////////////////
    //  Begin: Implementation of Mailer
    //////////////////////////////////////////////////////////////////////    
    /**
     * @see Mailer.getEmailAddresses(String[])
     */
    public List getEmailAddresses(String[] p_userIds)
    	throws MailerException, RemoteException
    {
        return m_localInstance.getEmailAddresses(p_userIds);
    }

    
    /**
     * @see Mailer.sendMail(EmailInformation, EmailInformation, String, String, String[], String)
     */
    public void sendMail(EmailInformation p_sendFromEmailInfo, 
                         EmailInformation p_recipientEmailInfo,
                         String p_subjectKey, String p_messageKey, 
                         String[] p_messageArguments, String p_companyIdStr)
        throws MailerException, RemoteException
    {
        m_localInstance.sendMail(p_sendFromEmailInfo, p_recipientEmailInfo, 
                                 p_subjectKey, p_messageKey, 
                                 p_messageArguments, p_companyIdStr);
    }
    
    /**
     * Modify on Mailer.sendMail(String, EmailInformation, String, String, String[], String)
     */
    public void sendMail(String p_sendFromUserId, 
                         EmailInformation p_recipient,
                         String p_subjectKey, String p_messageKey, 
                         String[] p_messageArguments, String p_companyIdStr)
        throws MailerException, RemoteException
    {
        m_localInstance.sendMail(p_sendFromUserId, p_recipient, 
                                 p_subjectKey, p_messageKey, 
                                 p_messageArguments, p_companyIdStr);
    }
    
    public void sendMail(EmailInformation p_sendFromEmailInfo,
            EmailInformation p_recipientEmailInfo, String p_subjectKey,
            String p_messageKey, String[] p_messageArguments,
            String[] p_attachments, long p_companyId)
            throws MailerException, RemoteException
    {
        m_localInstance.sendMail(p_sendFromEmailInfo, p_recipientEmailInfo,
                p_subjectKey, p_messageKey, p_messageArguments, p_attachments,
                p_companyId);
    }

    /**
     * @see Mailer.sendMailFromAdmin(String, String[], String, String)
     */
    public void sendMailFromAdmin(String p_recipient, String[] p_messageArguments,
                           String p_subjectKey, String p_messageKey,
                           String p_companyId)
        throws MailerException, RemoteException
    {
        m_localInstance.sendMailFromAdmin(p_recipient, p_messageArguments, 
                                          p_subjectKey, p_messageKey, p_companyId);
    }
    
    /**
     * Send an email to a recipient from the system admin.  This method is
     * used for system notifications (i.e. during an import/export).
     *
     */
    public void sendMailFromAdmin(User p_recipient, 
                                  String[] p_messageArguments,
                                  String p_subjectKey,
                                  String p_messageKey,
                                  String p_companyIdStr)
        throws MailerException, RemoteException
    {
        m_localInstance.sendMailFromAdmin(p_recipient, p_messageArguments, 
                                          p_subjectKey, p_messageKey, 
                                          p_companyIdStr);
    }

    /**
     * Send an email to the system administrator.  This method is
     * used for system notifications (i.e. during an import/export)
     * where the project manager could not be identified.
     * Attachments can be added to the email message.  They are
     * strings that are converted into file attachments.
     */
    public void sendMailToAdmin(String[] p_messageArguments,
                                       String p_subjectKey,
                                       String p_messageKey,
                                       String[] p_attachments, 
                                       String p_companyIdStr)
        throws MailerException, RemoteException
    {
        m_localInstance.sendMailToAdmin(p_messageArguments, p_subjectKey,
                                        p_messageKey, p_attachments, 
                                        p_companyIdStr);
    }
    
    /**
     * Determines whether the system-wide notification is enabled
     */
    public boolean isSystemNotificationEnabled() throws MailerException, RemoteException
    {
    	return m_localInstance.isSystemNotificationEnabled();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Implementation of Mailer
    //////////////////////////////////////////////////////////////////////       
}
