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

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;
/**
 * A generic mailer exception to report errors in email process.
 * <p>
 * For mail package the EXCEPTION ID ranges from 2300-2399.
 * For mail package the ERROR message ID ranges from 4300-4399.
 * For mail package the INFO message ID ranges from 6300-6399.
 * For mail package the DEBUG message ID ranges from 8300-8399.
 * <p>
 * Creation date: (8/20/00)
 * @author: Marving Lau
 */


public class MailerException extends GeneralException
{

    /**
     * Email related messages are stored in the following property file
     */
    final static String PROPERTY_FILE_NAME = "MailerException";
    ///////////////////////////////////////////////////////////////////
    ////////////            Exception Id's           //////////////
    ///////////////////////////////////////////////////////////////////
    /*public final static String EX_SYSTEM_CONFIG     = 2300;
    public final static String EX_SENDING_MESSAGE   = 2301;
    public final static String EX_PARSING           = 2302;*/
    ///////////////////////////////////////////////////////////////////
    ////////////            Error Message Id's           //////////////
    ///////////////////////////////////////////////////////////////////
    
    public final static String MSG_FAILED_TO_RETRIEVE_CONFIG_PARAM  = "configParam";
    // Args: 1 - email address sending from
    //       2 - email address sending to
    //       3 - subject of email
    public final static String MSG_FAILED_TO_SEND_EMAIL             = "sendEmail";
    // Args: 1 - address list that it failed to parse
    public final static String MSG_FAILED_TO_PARSE                  = "errorParsing";
    // Args: 1 - list of user ids that failed to get the email address for
    public final static String MSG_FAILED_TO_GET_EMAIL_ADDRESSES    = "getEmailAddresses";
    // Args: 1 - user id that failed to get the email address for 
    public final static String MSG_FAILED_TO_GET_EMAIL_ADDRESS      = "getEmailAddress";


    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public MailerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error. 
     *        It can be null.
     */
    public MailerException(String p_messageKey,
                           String[] p_messageArguments,
                           Exception p_originalException)
    {
        super (p_messageKey, p_messageArguments, p_originalException, 
              PROPERTY_FILE_NAME);
    }
}
