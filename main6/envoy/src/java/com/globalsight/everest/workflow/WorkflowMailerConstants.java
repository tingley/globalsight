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
package com.globalsight.everest.workflow;
/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
/**
 * Constants used with the Mailer package and
 * sending emails for workflow.
 */
public interface WorkflowMailerConstants
{
    public static final int ACCEPT_TASK 	= 0;
    public static final int ADVANCE_TASK 	= 1;
    public static final int CANCEL_TASK		= 2;
    public static final int REASSIGN_TASK 	= 3;
    public static final int REJECT_TASK		= 4;
    public static final int REROUTE_TASK 	= 5;
    public static final int ACTIVATE_TASK       = 6;
    public static final int COMPLETED_TASK      = 7;
    public static final int PAGE_REIMPORTED     = 8;
    public static final int PAGE_EXPORTED_FOR_UPDATE = 9;
    public static final int NO_AVAILABLE_RESOURCE = 10;
    public static final int ACTIVATE_REVIEW_TASK  = 11;
    public static final int COMPLETED_WFL = 13;
    public static final int COMPLETED_JOB = 14;

    // message text.
    public static final String ACCEPT_TASK_MESSAGE = "message_accept_task";
    public static final String ADVANCE_TASK_MESSAGE = "message_advance_task";
    public static final String CANCEL_TASK_MESSAGE = "message_cancel_task";
    public static final String REASSIGN_TASK_MESSAGE = "message_reassign_task";
    public static final String REJECT_TASK_MESSAGE = "message_reject_task";
    public static final String REROUTE_TASK_MESSAGE = "message_reroute_task";
    public static final String ACTIVATE_TASK_MESSAGE = "message_activate_task";
    public static final String ACTIVATE_REVIEW_TASK_MESSAGE = "message_activate_review_task";
    public static final String COMPLETED_TASK_MESSAGE = "message_completed_task";
    public static final String COMPLETED_WFL_MESSAGE = "message_completed_workflow";
    public static final String COMPLETED_JOB_MESSAGE = "message_completed_job";
    public static final String PAGE_REIMPORTED_MESSAGE = "message_page_reimported";
    public static final String PAGE_EXPORTED_FOR_UPDATE_MESSAGE = "message_page_efu";
    public static final String NO_AVAILABLE_RESOURCE_MESSAGE = "message_no_available_resource";
    
    // mesage subject.
    public static final String ACCEPT_TASK_SUBJECT = "subject_accept_task";
    public static final String ADVANCE_TASK_SUBJECT = "subject_advance_task";
    public static final String CANCEL_TASK_SUBJECT = "subject_cancel_task";
    public static final String REASSIGN_TASK_SUBJECT = "subject_reassign_task";
    public static final String REJECT_TASK_SUBJECT = "subject_reject_task";
    public static final String REROUTE_TASK_SUBJECT = "subject_reroute_task";
    public static final String ACTIVATE_TASK_SUBJECT = "subject_activate_task";
    public static final String ACTIVATE_REVIEW_TASK_SUBJECT = "subject_activate_review_task";
    public static final String COMPLETED_TASK_SUBJECT = "subject_completed_task";
    public static final String COMPLETED_WFL_SUBJECT = "subject_completed_workflow";
    public static final String COMPLETED_JOB_SUBJECT = "subject_completed_job";
    public static final String PAGE_REIMPORTED_SUBJECT = "subject_page_reimported";
    public static final String PAGE_EXPORTED_FOR_UPDATE_SUBJECT = "subject_page_efu";
    public static final String NO_AVAILABLE_RESOURCE_SUBJECT = "subject_no_available_resource";
}
