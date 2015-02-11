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
package com.globalsight.mediasurface;

import com.globalsight.util.GeneralException;


/**
 * Represents an exception thrown within CalendarManager remote interface.
 */
public class CmsUserManagerException
    extends GeneralException
{
    
    // PUBLIC CONSTANTS
    public final static String PROPERTY_FILE_NAME = 
        "CmsUserManagerException";
    
    // Exception Message Keys
    public final static String MSG_CREATE_CMS_USER_INFO_FAILED = 
        "failedToCreateCmsUserInfo";
    public final static String MSG_FIND_CMS_USER_INFO_FAILED = 
        "failedToFindCmsUserInfo";
    public final static String MSG_MODIFY_CMS_USER_INFO_FAILED = 
        "failedToModifyCmsUserInfo";
    


    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public CmsUserManagerException(Exception p_originalException)
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
    public CmsUserManagerException(String p_messageKey,
                                   String[] p_messageArguments,
                                   Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, 
             PROPERTY_FILE_NAME);
    }
}
