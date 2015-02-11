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
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerWLRemote;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.WorkflowMailerConstants;
import com.globalsight.util.RegexUtil;
import com.globalsight.util.resourcebundle.LocaleWrapper;
import com.globalsight.util.resourcebundle.SystemResourceBundle;


/**
* This class is the concrete implementation of Mailer interface and is responsible for 
* sending emails (which it delegates to MailSender)
*/

public class MailerLocal implements Mailer
{
    // the resource name for retrieving email message subject and text
    public static final String DEFAULT_RESOURCE_NAME = 
        "com/globalsight/resources/messages/EmailMessageResource";

    private UserManagerWLRemote m_userManager = null;

    private static Logger s_logger =
        Logger.getLogger(MailerLocal.class.getName());

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = 
        EventNotificationHelper.systemNotificationEnabled();

    private static List<String> CONTAINS_DURATION_IN_EMAIL = new ArrayList<String>();
    
    static
    {
    	CONTAINS_DURATION_IN_EMAIL.add("jobCancelFailure");
    	CONTAINS_DURATION_IN_EMAIL.add("manualJobDispatch");
    	CONTAINS_DURATION_IN_EMAIL.add("jobDispatchFailure");
    	CONTAINS_DURATION_IN_EMAIL.add("importFailure");
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Constructor
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Construct a MailerLocal for sending email.
    * @exception MailerException - wraps a ConfigException, and GeneralException.
    */
    public MailerLocal() throws MailerException
    {
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Constructor
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Mailer Implementation
    //////////////////////////////////////////////////////////////////////////////////
    /**
     * @see Mailer.getEmailAddresses(String[])
     */
    public List getEmailAddresses(String[] p_userIds)
    	throws MailerException, RemoteException
    {
        List returnVal = null;
        try
        {
            returnVal = getUserManager().
                getEmailInformationForUsers(p_userIds);
        }
        catch (Exception e)
        {
            String[] arg = {p_userIds.toString()};
            throw new MailerException(
                MailerException.MSG_FAILED_TO_GET_EMAIL_ADDRESSES,
                arg, e);
        }
        return returnVal;
    }

    /**
     * @see Mailer.sendMail(EmailInformation, EmailInformation, String, String, String[], String) 
     */
    public void sendMail(EmailInformation p_sendFromEmailInfo, 
                         EmailInformation p_recipientEmailInfo,
                         String p_subjectKey, String p_messageKey, 
                         String[] p_messageArguments,
                         String p_companyIdStr)
           throws MailerException, RemoteException
    {
        if (!m_systemNotificationEnabled ||
            !isNotificationEnabled(
                p_subjectKey, p_recipientEmailInfo.getUserId()))
        {
            return;
        }
        
        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(DEFAULT_RESOURCE_NAME, p_recipientEmailInfo.getEmailLocale());
        p_messageArguments = handleMessageArgments(p_messageArguments);
        // get the subject and message
        String subject = MessageFormat.format(
            bundle.getString(p_subjectKey), p_messageArguments);
        
        PermissionSet perSet = Permission.getPermissionManager().
        getPermissionSetForUser(p_recipientEmailInfo.getUserId());
        boolean jobCommentsPermission = 
            perSet.getPermissionFor(Permission.ACTIVITIES_COMMENTS_JOB);
        
        if(!jobCommentsPermission && p_messageArguments.length > 15)
        {
            p_messageArguments[15] = "N/A";
        }

        String message =
                MessageFormat.format(bundle.getString(p_messageKey),
                                     p_messageArguments);
        // send the email
        String email = p_recipientEmailInfo.getEmailAddress();
        String ccEmail = p_recipientEmailInfo.getCCEmailAddress();
        String bccEmail = p_recipientEmailInfo.getBCCEmailAddress();
        
        if(p_recipientEmailInfo.isAutoAction()) {
            email = p_recipientEmailInfo.getAutoActionEmailAddress();
            ccEmail = null;
            bccEmail = null;
        }
        
        String from = MailerHelper.getSendFrom(p_companyIdStr, p_sendFromEmailInfo);
        int len = p_messageArguments.length;
        String attachment = p_messageArguments[len - 1]; 
        //16 stands for the numbers of arguments in the configuration file of 
        //EmailMessageResource_en_US.properties, attachment is not the argument 
        //in this configuration file but be added in the the last element of 
        //argument, so when the length larger than 16 means the attachment  
        //need to be added to the mail
        if(jobCommentsPermission && len > 16 
                && (!WorkflowMailerConstants.ACTIVATE_TASK_SUBJECT.equals(p_subjectKey))
                && attachment != null && attachment.length() > 0)
        {
            sendMail(from, email, ccEmail, bccEmail, subject, message, 
                     attachment.split(","));
 
        }
        else
        {
            //when not any attachment is added to mail the argument of  
            //attachment is setted to null
            sendMail(from, email, ccEmail, bccEmail, subject, message, null);
        }
    }
    
    public void sendMail(EmailInformation p_sendFromEmailInfo,
            EmailInformation p_recipientEmailInfo, String p_subjectKey,
            String p_messageKey, String[] p_messageArguments,
            String[] p_attachments, long p_companyId)
            throws MailerException, RemoteException
    {
        if (!m_systemNotificationEnabled
                || !isNotificationEnabled(p_subjectKey,
                        p_recipientEmailInfo.getUserId()))
        {
            return;
        }

        // Operates message arguments, e.g. URL.
        p_messageArguments = handleMessageArgments(p_messageArguments);
        
        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(DEFAULT_RESOURCE_NAME, p_recipientEmailInfo.getEmailLocale());        
        // get the subject and message
        String subject = MessageFormat.format(bundle.getString(p_subjectKey),
                p_messageArguments);
        String message = MessageFormat.format(bundle.getString(p_messageKey),
                p_messageArguments);
        
        // Email address
        String fromEmail = MailerHelper.getSendFrom(String.valueOf(p_companyId), p_sendFromEmailInfo);
        String toEmail = p_recipientEmailInfo.getEmailAddress();
        String ccEmail = p_recipientEmailInfo.getCCEmailAddress();
        String bccEmail = p_recipientEmailInfo.getBCCEmailAddress();
        
        sendMail(fromEmail, toEmail, ccEmail, bccEmail, subject, message, p_attachments);
    }

    /**
     * Modify on Mailer.sendMail(EmailInformation, EmailInformation, String, String, String[], String)
     */
    public void sendMail(String p_sendFromUserId, 
                         EmailInformation recipientEmailInfo,
                         String p_subjectKey, String p_messageKey,
                         String[] p_messageArguments, String p_companyIdStr) 
         throws MailerException, RemoteException
    {
        EmailInformation fromEmailInfo =
            ServerProxy.getUserManager().getEmailInformationForUser(p_sendFromUserId);
        sendMail(fromEmailInfo, recipientEmailInfo, p_subjectKey, p_messageKey, 
                 p_messageArguments, p_companyIdStr);
    }

    /**
     * @see Mailer.sendMailFromAdmin(String, String[], String, String)
     */
    public void sendMailFromAdmin(String p_recipient, 
                                  String[] p_messageArguments,
                                  String p_subjectKey, 
                                  String p_messageKey,
                                  String p_companyIdStr)
        throws MailerException, RemoteException
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        User recipient = new UserImpl();
        recipient.setEmail(p_recipient);
        
        sendMailFromAdmin(Locale.getDefault(), 
                          recipient,
                          p_subjectKey,
                          p_messageKey,
                          p_messageArguments,
                          null,
                          p_companyIdStr);
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
        if (!m_systemNotificationEnabled || 
            !isNotificationEnabled(p_subjectKey, p_recipient.getUserId()))
        {
            return;
        }
        String userLocale = p_recipient.getDefaultUILocale();
        
        sendMailFromAdmin(	
        		LocaleWrapper.getLocale(userLocale), 
        		p_recipient,
                p_subjectKey,
                p_messageKey,
                p_messageArguments,
                null,
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
    	if(CONTAINS_DURATION_IN_EMAIL.contains(p_messageKey))
    	{
    		p_messageArguments = buildMessageArguments(p_messageArguments);
    	}
        // get from, to addresses - they will be the same
        String from = null;
        try
        {
            from = MailerHelper.getSendFrom(p_companyIdStr);
        } 
        catch (Exception ge) //GeneralException
       	{
            s_logger.error( "Couldn't get the FROM email from Company.", ge);
            String args[] = new String[1];
            args[0] = p_companyIdStr;
            throw new MailerException(
                MailerException.MSG_FAILED_TO_GET_EMAIL_ADDRESS, 
                args, ge);
       	}
        
        String to = from;

        if (!m_systemNotificationEnabled ||
            !isNotificationEnabled(p_subjectKey, to))
        {
            return;
        }

        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(DEFAULT_RESOURCE_NAME, Locale.getDefault());
        String[] messageArguments = handleMessageArgments(p_messageArguments);
        // get the subject and message
        String subject = MessageFormat.format(
            bundle.getString(p_subjectKey), messageArguments);
        String message = MessageFormat.format(
            bundle.getString(p_messageKey),messageArguments);

        sendMail(from, to, null, null, subject, message, p_attachments);
    }
    
    /**
     * Determines whether the system-wide notification is enabled
     */
    public boolean isSystemNotificationEnabled() throws MailerException, RemoteException
    {
    	return m_systemNotificationEnabled;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Mailer Implementation
    //////////////////////////////////////////////////////////////////////////////////


    private String[] buildMessageArguments(String[] arguments) {
		String[] newArguments = new String[arguments.length + 2];
		int totalMinutes = Integer.parseInt(arguments[1]);
		int days = totalMinutes/60/24;
		int hours = (totalMinutes - (days * 24 * 60))/60;
		int minutes = totalMinutes - (days * 24 * 60) - hours * 60;
		newArguments[0] = arguments[0];
		newArguments[1] = "" + days;
		newArguments[2] = "" + hours;
		newArguments[3] = "" + minutes;
		
		for( int i = 2; i < arguments.length; i++ )
		{
			newArguments[i + 2] = arguments[i];
		}
		return newArguments;
	}

	//////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////
    // get a reference to the user manager remote interface
    private UserManagerWLRemote getUserManager()
        throws Exception
    {
        if (m_userManager == null)
        {
            m_userManager = ServerProxy.getUserManager();
        }
        return m_userManager;
    }

    /*
     * Determines whether the notification preference for this user is enabled, and
     * whether the user even has permission to get the notification.
     */
    private static boolean isNotificationEnabled(String p_subjectKey,
                                                 String p_userId)
    {
        boolean isNotificationEnabled = true;
        try 
        {
            //first check the user's permissions
            PermissionSet perms = Permission.getPermissionManager().getPermissionSetForUser(p_userId);
            isNotificationEnabled = MailerConstants.hasPermissionForThisCategoryOfNotification(
                perms, p_subjectKey);
            if (isNotificationEnabled == false)
            {
                s_logger.warn("User " + p_userId + " does not have permission to receive notifications for " + p_subjectKey);
                return isNotificationEnabled; //we know at this point there is no permission for this notification
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get permissions for user " + p_userId + " to determine if he/she could receive notifications for " +
                           p_subjectKey,e);
            return false;
        }

        try
        {
            UserParameter categoryParam = null;
            UserParameter up = ServerProxy.getUserParameterManager().
                getUserParameter(p_userId, UserParamNames.NOTIFICATION_ENABLED);

            String category = MailerConstants.getNotificationParamName(p_subjectKey);
            if (category != null)
            {
                categoryParam = ServerProxy.getUserParameterManager().
                    getUserParameter(p_userId, category);
            }            

            isNotificationEnabled = 
                (up == null || up.getBooleanValue()) && 
                (categoryParam == null || categoryParam.getBooleanValue());
        }
        catch (Exception e)
        {
            s_logger.error("Failed to get notification preference for user: "+
                           p_userId, e);
        }

        return isNotificationEnabled;
    }

    /**
     * Send an email from the GlobalSight Admin to the specified recipient.
     */
    private void sendMailFromAdmin(Locale p_userLocale, 
    							   User p_user,
                                   String p_subjectKey,
                                   String p_messageKey,
                                   String[] p_messageArguments,
                                   String[] p_attachments,
                                   String p_companyIdStr)
        throws MailerException
    {
    	if(CONTAINS_DURATION_IN_EMAIL.contains(p_messageKey))
    	{
    		p_messageArguments = buildMessageArguments(p_messageArguments);
    	}
        ResourceBundle bundle = SystemResourceBundle.getInstance()
                .getResourceBundle(DEFAULT_RESOURCE_NAME, p_userLocale);
        String from = null;
        try
        {
            from = MailerHelper.getSendFrom(p_companyIdStr);
        } 
        catch (Exception ge) //GeneralException
        {
            s_logger.error( "Couldn't get the FROM email from Company.", ge);
            String args[] = new String[1];
            args[0] = p_companyIdStr;
            throw new MailerException(
                MailerException.MSG_FAILED_TO_GET_EMAIL_ADDRESS, 
                args, ge);
        }
        
        String[] messageArguments = handleMessageArgments(p_messageArguments);
        // get the subject and message
        String subject = MessageFormat.format(
            bundle.getString(p_subjectKey), messageArguments);
        String message = MessageFormat.format(
            bundle.getString(p_messageKey), messageArguments);

        String sendTo  = p_user.getEmail();
        String sendCc  = p_user.getCCEmail();
        String sendBcc = p_user.getBCCEmail();
        if(!RegexUtil.validEmail(sendCc))
        {
        	sendCc = null;
        }
        if(!RegexUtil.validEmail(sendBcc))
        {
        	sendBcc = null;
        }
        
        sendMail(from, sendTo, sendCc, sendBcc, subject, message, p_attachments);
    }

    /*
    * Send the specified message to the person(s) specified.
    * @param  p_sendFrom - The email address of the sender.
    * @param  p_sendTo - The email address(es) where the message will be sent to.
    * @param  p_sendCc - The email address(es) of the "carbon copy" recipient(s).
    * @param  p_sendBcc - The email address(es) of "blind carbon copy" recipient(s).
    * @param  p_subject - The subject of this email.
    * @param  p_message - The text that is the message's content.
    * @param  p_attachments - Attachments to add to the email.  They are
    *                         strings that are turned into files for attaching.
    *                         This can be null if no attachments.
    * @exception MailerException - wraps JavaMail's MessagingException.
    */
    private void sendMail(String p_sendFrom, String p_sendTo, 
                          String p_sendCc, String p_sendBcc, 
                          String p_subject, String p_message,
                          String[] p_attachments) 
    throws MailerException
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        try
        {
            String companyId = CompanyThreadLocal.getInstance().getValue();
            s_logger.debug("Company id get from ThreadLocal is: " + companyId);
            
            HashMap args = new HashMap();
            CompanyWrapper.saveCurrentCompanyIdInMap(args, s_logger);
            args.put("from", p_sendFrom);
            args.put("to", p_sendTo);
            args.put("cc", p_sendCc);
            args.put("bcc", p_sendBcc);
            args.put("subject", p_subject);
            args.put("text", p_message);
            args.put("attachments", p_attachments);
            JmsHelper.sendMessageToQueue(args, JmsHelper.JMS_MAIL_QUEUE);
        }
        catch (Exception e)
        {
        	String[] args = new String[3];
        	args[0] = p_sendFrom;
        	String sendTo = p_sendTo;
        	if ( (p_sendCc != null) && (!p_sendCc.equals("")) )
        	{
        		sendTo = sendTo + " and Cc to: " +p_sendCc;
        	}
        	if ( (p_sendBcc != null) && (!p_sendBcc.equals("")) )
        	{
        		sendTo = sendTo + " and Bcc to: " + p_sendBcc;
        	}
        	args[1] = sendTo; 
        	args[2] = p_subject;
            throw new MailerException(MailerException
                                      .MSG_FAILED_TO_SEND_EMAIL,
                                      args, e);
        }
    }
    
    private String[] handleMessageArgments(String[] p_args)
    {
        if (p_args == null || p_args.length == 0)
        {
            return p_args;
        }
        
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean useNewUrl = false;
            boolean usePublicUrl = sc.getBooleanParameter("cap.public.url.enable");
            boolean useSSLUrl = sc.getBooleanParameter("server.ssl.enable");
            String newUrl = "";
            
            if (usePublicUrl)
            {
                String publicUrl = sc.getStringParameter("cap.public.url");
                newUrl = publicUrl;
                useNewUrl = true;
            }
            else if (useSSLUrl)
            {
                String sslUrl = sc.getStringParameter("cap.login.url.ssl");
                newUrl = sslUrl;
                useNewUrl = true;
            }
            
            if (useNewUrl)
            {
                String capUrl = sc.getStringParameter("cap.login.url");
                String[] newArgs = new String[p_args.length];
                
                for (int i = 0; i < p_args.length; i++)
                {
                    String oldArg = p_args[i];
                    if (oldArg != null && oldArg.equalsIgnoreCase(capUrl))
                    {
                        newArgs[i] = newUrl;
                    }
                    else if (oldArg != null && oldArg.contains(capUrl))
                    {
                        newArgs[i] = oldArg.replace(capUrl, newUrl);
                    }
                    else
                    {
                        newArgs[i] = oldArg;
                    }
                }
                
                return newArgs;
            }
        }
        catch (Exception e)
        {
            s_logger.error("Failed to handleMessageArgments: " +
                    p_args, e);
        }
        
        return p_args;
    }

    

    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Local Methods
    //////////////////////////////////////////////////////////////////////////////////
}
