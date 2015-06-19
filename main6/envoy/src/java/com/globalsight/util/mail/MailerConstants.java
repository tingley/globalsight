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
import java.util.ArrayList;
import com.globalsight.config.UserParamNames;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.workflow.WorkflowMailerConstants;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.everest.aligner.EmailNotification;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;

/**
 * This class contains notification related constants.  It also
 * provides access to email flags per category for a given key
 * (email subject).
 */
public class MailerConstants
{
    // Email subject keys
    public static final String CANCEL_FAILURE_SUBJECT =
        "cancelFailureSubject";
    public static final String DISPATCH_SUBJECT =
        "subjectJobDispatch";
    public static final String DISPATCH_MESSAGE =
        "manualJobDispatch";
    public static final String DISPATCH_FAILURE_SUBJECT =
        "dispatchFailureSubject";
    public static final String INITIAL_IMPORT_FAILED_SUBJECT =
        "importFailedSubject";
    public static final String JOB_IMPORT_FAILED_SUBJECT =
        "jobImportFailedSubject";
    public static final String JOB_IMPORT_CORRECTION_SUBJECT =
        "jobImportCorrectionSubject";
    public static String PAGE_REIMPORT_TO_PM_SUBJECT =
        "pageReimportedToPmSubject";
    public static String PAGE_FAILED_TO_REIMPORT_SUBJECT =
        "pageFailedReimportToPmSubject";
    public static String WF_PM_CHANGE_FAILED_SUBJECT =
        "subject_wfUpdateFailed";
    public static String WF_PM_CHANGE_COMPLETED_SUBJECT =
        "subject_wfUpdateCompleted";
    public static final String EXPORT_FAILED_SUBJECT =
        "exportFailedSubject";
    public static final String DTD_VALIDATE_FAILED_SUBJECT =
        "dtdFailedSubject";
    public static final String PAGE_EFU_TO_PM_SUBJECT =
        "pageExportForUpdateToPmSubject";
    public static final String PAGE_FAILED_TO_EFU_SUBJECT =
        "pageFailedExportForUpdateToPmSubject";
    public static final String REPORT_EXPORT_COMPLETED_SUBJECT =
        "subject_export_completed";
    public static final String REPORT_EXPORT_FAILED_SUBJECT =
        "subject_export_failed";
    public static final String ESTIMATED_EXCEEDS_PLANNED_DATE =
        "subjectEstimatedExceedsPlanned";
    public static final String WF_IMPORT_FAILURE_SUBJECT =
        "importFailureSubject";
    public static final String STF_CREATION_FAILED_SUBJECT =
        "subject_stf_creation_failed";
    public static final String CUSTOMER_UPLOAD_COMPLETED_SUBJECT =
        "subject_customerUploadCompleted";
    public static final String DESKTOPICON_UPLOAD_COMPLETED_SUBJECT =
        "subject_desktopiconUploadCompleted";
    public static final String GXML_EDIT_SUCCESS =
        "subjectGxmlEditSuccess";
    public static final String GXML_EDIT_FAILURE =
        "subjectGxmlEditFailure";
    public static final String LOGIN_RESET_PASSWORD_SUBJECT 		= "subject_login_resetPassword";
    public static final String LOGIN_RETRIEVE_UESRNAME_SUBJECT 		= "subject_login_retrieveUsernames";
    public static final String JOB_IMPORT_SUCC_SUBJECT              = "subject_job_import_succ";

    // Email message keys
    public static final String CUSTOMER_UPLOAD_COMPLETED_MESSAGE 	= "message_customerUploadCompleted";
    public static final String DESKTOPICON_UPLOAD_COMPLETED_MESSAGE = "message_desktopiconUploadCompleted";
    public static final String LOGIN_RESET_PASSWORD_MESSAGE 		= "message_login_resetPassword";
    public static final String LOGIN_RETRIEVE_UESRNAME_MESSAGE 		= "message_login_retrieveUsernames";
    public static final String JOB_IMPORT_SUCC_MESSAGE              = "message_job_import_succ"; 
    
    public static final String WORKFLOW_STATE_POST_FAILURE_SUBJECT = "subject_wfStatePostFailure";
    public static final String WORKFLOW_STATE_POST_FAILURE_MESSAGE = "message_wfStatePostFailure";
    
    // Auto-accept email
    public static final String AUTO_ACCEPT_SUBJECT = "subject_autoaccept_task";
    public static final String AUTO_ACCEPT_MESSAGE = "message_autoaccept_task";

    private static HashMap s_notificationMap = new HashMap();

    //keep lists of the categories of the notifications since these
    //are tied to permissions for system,workflow/project mgmt, and general
    private static ArrayList s_systemNotifications = new ArrayList();
    private static ArrayList s_wfNotifications = new ArrayList();
    private static ArrayList s_generalNotifications = new ArrayList();
    static
    {
        //////////////////////////////////////////////////////////////////////
        //  ADMIN
        //////////////////////////////////////////////////////////////////////
        s_notificationMap.put(INITIAL_IMPORT_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_INITIAL_IMPORT_FAILURE);
        s_systemNotifications.add(INITIAL_IMPORT_FAILED_SUBJECT);

        //////////////////////////////////////////////////////////////////////
        //  PM / WFM
        //////////////////////////////////////////////////////////////////////
        s_notificationMap.put(CANCEL_FAILURE_SUBJECT,
                              UserParamNames.NOTIFY_JOB_DISCARD_FAILURE);
        s_wfNotifications.add(CANCEL_FAILURE_SUBJECT);

        s_notificationMap.put(DISPATCH_SUBJECT,
                              UserParamNames.NOTIFY_READY_TO_DISPATCH);
        s_wfNotifications.add(DISPATCH_SUBJECT);

        s_notificationMap.put(DISPATCH_FAILURE_SUBJECT,
                              UserParamNames.NOTIFY_DISPATCH_FAILURE);
        s_wfNotifications.add(DISPATCH_FAILURE_SUBJECT);

        s_notificationMap.put(JOB_IMPORT_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_IMPORT_FAILURE);
        s_wfNotifications.add(JOB_IMPORT_FAILED_SUBJECT);

        s_notificationMap.put(JOB_IMPORT_CORRECTION_SUBJECT,
                              UserParamNames.NOTIFY_IMPORT_CORRECTION);
        s_wfNotifications.add(JOB_IMPORT_CORRECTION_SUBJECT);

        s_notificationMap.put(PAGE_FAILED_TO_REIMPORT_SUBJECT,
                              UserParamNames.NOTIFY_DELAYED_REIMPORT_FAILURE);
        s_wfNotifications.add(PAGE_FAILED_TO_REIMPORT_SUBJECT);

        s_notificationMap.put(WorkflowMailerConstants.ACCEPT_TASK_SUBJECT,
                              UserParamNames.NOTIFY_TASK_ACCEPTANCE);
        s_wfNotifications.add(WorkflowMailerConstants.ACCEPT_TASK_SUBJECT);

        s_notificationMap.put(WorkflowMailerConstants.REJECT_TASK_SUBJECT,
                              UserParamNames.NOTIFY_TASK_REJECTION);
        s_wfNotifications.add(WorkflowMailerConstants.REJECT_TASK_SUBJECT);

        s_notificationMap.put(WorkflowMailerConstants.COMPLETED_TASK_SUBJECT,
                              UserParamNames.NOTIFY_TASK_COMPLETION);
        s_wfNotifications.add(WorkflowMailerConstants.COMPLETED_TASK_SUBJECT);

        s_notificationMap.put(WorkflowMailerConstants.COMPLETED_WFL_SUBJECT,
                UserParamNames.NOTIFY_WFL_COMPLETION);

        s_wfNotifications.add(WorkflowMailerConstants.COMPLETED_JOB_SUBJECT);
        s_notificationMap.put(WorkflowMailerConstants.COMPLETED_JOB_SUBJECT,
                UserParamNames.NOTIFY_JOB_COMPLETION);
        s_wfNotifications.add(WorkflowMailerConstants.COMPLETED_TASK_SUBJECT);

        s_notificationMap.put(WF_PM_CHANGE_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_PM_CHANGE_IN_PROJECT);
        s_wfNotifications.add(WF_PM_CHANGE_FAILED_SUBJECT);

        s_notificationMap.put(WF_PM_CHANGE_COMPLETED_SUBJECT,
                              UserParamNames.NOTIFY_PM_CHANGE_IN_PROJECT);
        s_wfNotifications.add(WF_PM_CHANGE_COMPLETED_SUBJECT);

        s_notificationMap.put(PAGE_FAILED_TO_EFU_SUBJECT,
                              UserParamNames.NOTIFY_EXPORT_SOURCE_FAILURE);
        s_wfNotifications.add(PAGE_FAILED_TO_EFU_SUBJECT);

        s_notificationMap.put(ESTIMATED_EXCEEDS_PLANNED_DATE,
                              UserParamNames.NOTIFY_ESTIMATED_EXCEEDS_PLANNED_DATE);
        s_wfNotifications.add(ESTIMATED_EXCEEDS_PLANNED_DATE);

        s_notificationMap.put(WF_IMPORT_FAILURE_SUBJECT,
                              UserParamNames.NOTIFY_ADD_WORKFLOW_TO_JOB_FAILURE);
        s_wfNotifications.add(WF_IMPORT_FAILURE_SUBJECT);

        s_notificationMap.put(STF_CREATION_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_STF_CREATION_FAILURE);
        s_wfNotifications.add(STF_CREATION_FAILED_SUBJECT);

        s_notificationMap.put(SchedulerConstants.SCHEDULING_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_SCHEDULING_FAILURE);
        s_wfNotifications.add(SchedulerConstants.SCHEDULING_FAILED_SUBJECT);

        // Deadline warning emails as one category
        s_notificationMap.put(SchedulerConstants.ACCEPTANCE +
                              SchedulerConstants.DEADLINE_APPROACH_SUBJECT,
                              UserParamNames.NOTIFY_ACTIVITY_DEADLINE);
        s_wfNotifications.add(SchedulerConstants.ACCEPTANCE +
                              SchedulerConstants.DEADLINE_APPROACH_SUBJECT);
        s_notificationMap.put(SchedulerConstants.ACCEPTANCE +
                              SchedulerConstants.DEADLINE_PASSED_SUBJECT,
                              UserParamNames.NOTIFY_ACTIVITY_DEADLINE);
        s_wfNotifications.add(SchedulerConstants.ACCEPTANCE +
                              SchedulerConstants.DEADLINE_PASSED_SUBJECT);

        s_notificationMap.put(SchedulerConstants.COMPLETION +
                              SchedulerConstants.DEADLINE_APPROACH_SUBJECT,
                              UserParamNames.NOTIFY_ACTIVITY_DEADLINE);
        s_wfNotifications.add(SchedulerConstants.COMPLETION +
                              SchedulerConstants.DEADLINE_APPROACH_SUBJECT);

        s_notificationMap.put(SchedulerConstants.COMPLETION +
                              SchedulerConstants.DEADLINE_PASSED_SUBJECT,
                              UserParamNames.NOTIFY_ACTIVITY_DEADLINE);
        s_wfNotifications.add(SchedulerConstants.COMPLETION +
                              SchedulerConstants.DEADLINE_PASSED_SUBJECT);
        
       
        // Adds the subjects and bodies of notify pm overdue email.
        s_notificationMap.put(SchedulerConstants.ACCEPTANCE
				+ SchedulerConstants.NOTIFY_PM_OVERDUE_SUBJECT,
				UserParamNames.NOTIFY_OVERDUE_PM);
		s_wfNotifications.add(SchedulerConstants.ACCEPTANCE
				+ SchedulerConstants.NOTIFY_PM_OVERDUE_SUBJECT);

		s_notificationMap.put(SchedulerConstants.COMPLETION
				+ SchedulerConstants.NOTIFY_PM_OVERDUE_SUBJECT,
				UserParamNames.NOTIFY_OVERDUE_PM);
		s_wfNotifications.add(SchedulerConstants.COMPLETION
				+ SchedulerConstants.NOTIFY_PM_OVERDUE_SUBJECT);

		s_notificationMap.put(WorkflowMailerConstants.NO_AVAILABLE_RESOURCE_SUBJECT,
				              UserParamNames.NOTIFY_NO_AVAILABLE_RESOURCE);
		s_wfNotifications.add(WorkflowMailerConstants.NO_AVAILABLE_RESOURCE_SUBJECT);


        // ////////////////////////////////////////////////////////////////////
        // Any Participant
        //////////////////////////////////////////////////////////////////////
        // Delayed reimport as one category
        s_notificationMap.put(PAGE_REIMPORT_TO_PM_SUBJECT,
                              UserParamNames.NOTIFY_DELAYED_REIMPORT);
        s_generalNotifications.add(PAGE_REIMPORT_TO_PM_SUBJECT);
        s_notificationMap.put(WorkflowMailerConstants.PAGE_REIMPORTED_SUBJECT,
                              UserParamNames.NOTIFY_DELAYED_REIMPORT);
        s_generalNotifications.add(WorkflowMailerConstants.PAGE_REIMPORTED_SUBJECT);

        s_notificationMap.put(WorkflowMailerConstants.ACTIVATE_TASK_SUBJECT,
                              UserParamNames.NOTIFY_NEWLY_ASSIGNED_TASK);
        s_generalNotifications.add(WorkflowMailerConstants.ACTIVATE_TASK_SUBJECT);

        s_notificationMap.put(WorkflowMailerConstants.CANCEL_TASK_SUBJECT,
                              UserParamNames.NOTIFY_WORKFLOW_DISCARD);
        s_generalNotifications.add(WorkflowMailerConstants.CANCEL_TASK_SUBJECT);

        // Export for update as one category
        s_notificationMap.put(WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE_SUBJECT,
                              UserParamNames.NOTIFY_EXPORT_FOR_UPDATE);
        s_generalNotifications.add(WorkflowMailerConstants.PAGE_EXPORTED_FOR_UPDATE_SUBJECT);
        s_notificationMap.put(PAGE_EFU_TO_PM_SUBJECT,
                              UserParamNames.NOTIFY_EXPORT_FOR_UPDATE);
        s_generalNotifications.add(PAGE_EFU_TO_PM_SUBJECT);

        // Export completion (successful/failed) as one category
        s_notificationMap.put(EXPORT_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_EXPORT_COMPLETION);
        s_generalNotifications.add(EXPORT_FAILED_SUBJECT);
        s_notificationMap.put(REPORT_EXPORT_COMPLETED_SUBJECT,
                              UserParamNames.NOTIFY_EXPORT_COMPLETION);
        s_generalNotifications.add(REPORT_EXPORT_COMPLETED_SUBJECT);
        s_notificationMap.put(REPORT_EXPORT_FAILED_SUBJECT,
                              UserParamNames.NOTIFY_EXPORT_COMPLETION);
        s_generalNotifications.add(REPORT_EXPORT_FAILED_SUBJECT);

        s_notificationMap.put(OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
                              UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
        s_generalNotifications.add(OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT);

        s_notificationMap.put(OfflineEditHelper.UPLOAD_FAIL_SUBJECT,
                              UserParamNames.NOTIFY_SAVING_SEGMENTS_FAILURE);
        s_generalNotifications.add(OfflineEditHelper.UPLOAD_FAIL_SUBJECT);

        // Adds the subjects and bodies of notify user overdue email.
        s_notificationMap.put(SchedulerConstants.ACCEPTANCE
				+ SchedulerConstants.NOTIFY_USER_OVERDUE_SUBJECT,
				UserParamNames.NOTIFY_OVERDUE_USER);
        s_generalNotifications.add(SchedulerConstants.ACCEPTANCE
				+ SchedulerConstants.NOTIFY_USER_OVERDUE_SUBJECT);

		s_notificationMap.put(SchedulerConstants.COMPLETION
				+ SchedulerConstants.NOTIFY_USER_OVERDUE_SUBJECT,
				UserParamNames.NOTIFY_OVERDUE_USER);
		s_generalNotifications.add(SchedulerConstants.COMPLETION
				+ SchedulerConstants.NOTIFY_USER_OVERDUE_SUBJECT);
		
		// Adds the subjects and bodies of notify quoted person email.
		s_notificationMap.put(SchedulerConstants.NOTIFY_QUOTE_PERSON_SUBJECT,
					UserParamNames.NOTIFY_QUOTE_PERSON);
        s_generalNotifications.add(SchedulerConstants.NOTIFY_QUOTE_PERSON_SUBJECT);

        // aligner email subjects
        s_notificationMap.put(EmailNotification.BATCH_COMPLETE_SUBJECT,
            UserParamNames.NOTIFY_BATCH_ALIGNMENT_SUCCESS);
        s_generalNotifications.add(EmailNotification.BATCH_COMPLETE_SUBJECT);
        s_notificationMap.put(EmailNotification.BATCH_FAILED_SUBJECT,
            UserParamNames.NOTIFY_BATCH_ALIGNMENT_FAILURE);
        s_generalNotifications.add(EmailNotification.BATCH_FAILED_SUBJECT);
        s_notificationMap.put(EmailNotification.UPLOAD_COMPLETE_SUBJECT,
            UserParamNames.NOTIFY_ALIGNMENT_UPLOAD_SUCCESS);
        s_generalNotifications.add(EmailNotification.UPLOAD_COMPLETE_SUBJECT);
        s_notificationMap.put(EmailNotification.UPLOAD_FAILED_SUBJECT,
            UserParamNames.NOTIFY_ALIGNMENT_UPLOAD_FAILURE);
        s_generalNotifications.add(EmailNotification.UPLOAD_FAILED_SUBJECT);
    }

    /**
     * Get the notification parameter name based on the email subject.
     * The email subject is the key used in the resource bundle and is
     * a unique string.
     */
    public static String getNotificationParamName(String p_emailSubject)
    {
        return p_emailSubject == null ? null :
            (String)s_notificationMap.get(p_emailSubject);
    }


    /**
     * Checks the permissionset for the appropriate notification
     * permission based on the category of the email subject.
     * 
     * @param p_permSet
     * @param p_emailSubject
     * @return 
     */
    public static boolean hasPermissionForThisCategoryOfNotification(
        PermissionSet p_permSet,
        String p_emailSubject)
    {
        boolean hasPermission = false;
        if (s_systemNotifications.contains(p_emailSubject))
            hasPermission = p_permSet.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_SYSTEM);
        else if (s_wfNotifications.contains(p_emailSubject))
            hasPermission = p_permSet.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_WFMGMT);
        else
            hasPermission = p_permSet.getPermissionFor(Permission.ACCOUNT_NOTIFICATION_GENERAL);

        return hasPermission;
    }
}
