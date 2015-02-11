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
package com.globalsight.everest.aligner;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;


/**
 * Handles Email notification
 */
public class EmailNotification
{
    private static final Logger c_logger =
        Logger.getLogger(
            EmailNotification.class.getName());

    public static final String BATCH_COMPLETE_SUBJECT
        = "subject_batchAlignmentCompleted";
    public static final String BATCH_COMPLETE_MESSAGE
        = "message_batchAlignmentCompleted";

    public static final String BATCH_FAILED_SUBJECT
        = "subject_batchAlignmentFailed";
    public static final String BATCH_FAILED_MESSAGE
        = "message_batchAlignmentFailed";

    public static final String UPLOAD_COMPLETE_SUBJECT
        = "subject_uploadAlignmentCompleted";
    public static final String UPLOAD_COMPLETE_MESSAGE
        = "message_uploadAlignmentCompleted";

    public static final String UPLOAD_FAILED_SUBJECT
        = "subject_uploadAlignmentFailed";
    public static final String UPLOAD_FAILED_MESSAGE
        = "message_uploadAlignmentFailed";

    static void sendNotification(
        User p_user, String p_subjectKey, String p_messageKey, String[] p_args)
    {
        try
        {   
            String companyIdStr =  CompanyWrapper.getCompanyIdByName(p_user.getCompanyName());        
            ServerProxy.getMailer().sendMailFromAdmin(p_user, p_args,
                p_subjectKey, p_messageKey, companyIdStr);
        }
        catch (Exception ex)
        {  
            // do not throw an exception if email notification fails...
            c_logger.error("failed to send alignment notification e-mail: "
                + "p_user=" + p_user + ", p_subjectKey=" + p_subjectKey
                + ", p_messageKey=" + p_messageKey + ", p_args=" + p_args,
                ex);
        } 
    }
}
