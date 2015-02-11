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

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
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
import com.globalsight.util.GeneralException;

/**
 * WorkflowException is a subclass of GeneralException. All workflow related
 * exceptions will throw this exception.
 */

public class WorkflowException extends GeneralException
{

    private static final long serialVersionUID = 924087940728333530L;
    /**
     * Workflow related messages are stored in the following property file
     */
    public final static String PROPERTY_FILE_NAME = "WorkflowException";
    // //////////////////////////////////////////////////////////////////////////
    // /////////////// Component Specific Exception ID's //////////////////////
    // //////////////////////////////////////////////////////////////////////////
    public final static int EX_DATA_ITEM_ERROR = 2100;
    public final static int EX_INSTANCE_NOT_CREATED = 2101;
    public final static int EX_INSTANCE_NOT_DELETED = 2102;
    public final static int EX_LOGIN_ERROR = 2103;
    public final static int EX_LOGOUT_ERROR = 2104;
    public final static int EX_PARAM_ERROR = 2105;
    public final static int EX_START_ERROR = 2106;
    public final static int EX_TASK_ERROR = 2107;
    public final static int EX_TEMPLATE_NOT_CREATED = 2108;
    public final static int EX_TEMPLATE_NOT_DELETED = 2109;
    public final static int EX_TEMPLATE_RETRIEVE_ERROR = 2110;
    public final static int EX_UNSUPPORTED_TEMPLATE_STRUCTURE = 2111;
    public final static int EX_UPDATE_ERROR = 2112;
    public final static int EX_WORKFLOW_INSTANCE_ADD_ARROW_INSTANCE = 2113;
    public final static int EX_WORKFLOW_INSTANCE_ADD_NODE_INSTANCE = 2114;
    public final static int EX_WORKFLOW_INSTANCE_ADD_TIMER = 2115;
    public final static int EX_WORKFLOW_INSTANCE_MOVE_NODE_INSTANCE = 2116;
    public final static int EX_WORKFLOW_INSTANCE_REMOVE_NODE_INSTANCE = 2117;
    public final static int EX_WORKFLOW_INSTANCE_REMOVE_TIMERS = 2118;
    public final static int EX_WORKFLOW_INSTANCE_SET_ROLE = 2119;
    public final static int EX_WORKFLOW_INSTANCE_STRUCTURAL_EDIT = 2120;
    public final static int EX_WORKFLOW_INSTANCE_TIMER_ACTION_ATTRIBUTE_OPERATION = 2121;
    public final static int EX_WORKFLOW_INSTANCE_TIMER_ATTRIBUTE_OPERATION = 2122;
    public final static int EX_WORKFLOW_OBJECT_FACTORY = 2123;
    public final static int EX_WORKFLOW_TEMPLATE_SET_DESCRIPTION = 2124;
    public final static int EX_WFSESSION_ERROR = 2125;
    public final static int EX_WORKFLOW_INSTANCE_SET_ROLESCRIPT = 2126;
    // /////////////////////////////////////////////////////////////////
    // ////////// Component Specific Error Message Id's //////////////
    // /////////////////////////////////////////////////////////////////
    // message is: "User should login to workflow server first!"
    public final static int MSG_FAILED_TO_ACCEPT_TASK = 4138;
    public final static int MSG_FAILED_TO_ADD_TIMER_DATA_ITEMS = 4101;
    public final static int MSG_FAILED_TO_ADD_TIMERS_TO_NODE_INSTANCE = 4102;
    public final static int MSG_FAILED_TO_ADVANCE_TASK = 4103;
    public final static int MSG_FAILED_TO_CLOSE_WORKFLOW_SESSION = 4104;
    public final static int MSG_FAILED_TO_CREATE_NODE = 4106;
    public final static int MSG_FAILED_TO_CREATE_TEMPLATE = 4107;
    public final static int MSG_FAILED_TO_CREATE_WORKFLOW_INSTANCE = 4108;
    public final static int MSG_FAILED_TO_DATA_ITEM_REFS = 4109;
    public final static int MSG_FAILED_TO_DECREMENT_NODE_INSTANCES_SEQ = 4110;
    public final static int MSG_FAILED_TO_GET_ACTIVE_TASK_FOR_WORKFLOW = 4111;
    public final static int MSG_FAILED_TO_GET_ACTIVE_TASK_LIST_FOR_USER = 4112;
    public final static int MSG_FAILED_TO_GET_PLAN = 4113;
    public final static int MSG_FAILED_TO_GET_TIMER_DEF = 4115;
    public final static int MSG_FAILED_TO_GET_WFOBJECT_LIST = 4116;
    public final static int MSG_FAILED_TO_GET_WORKFLOW_TASK = 4118;
    public final static int MSG_FAILED_TO_INCREMENT_NODE_INSTANCES_SEQ = 4119;
    public final static int MSG_FAILED_TO_INSERT_NODE_INSTANCE = 4120;
    public final static int MSG_FAILED_TO_LOGIN = 4121;
    public final static int MSG_FAILED_TO_LOGOUT = 4122;
    public final static int MSG_FAILED_TO_MOVE_NODE_INSTANCE = 4123;
    public final static int MSG_FAILED_TO_OPEN_WORKFLOW_SESSION = 4124;
    public final static int MSG_FAILED_TO_PROCESS_INSTANCE_STRUCTURAL_EDIT = 4125;
    public final static int MSG_FAILED_TO_REJECT_TASK = 4126;
    public final static int MSG_FAILED_TO_REMOVE_NODE_INSTANCE = 4127;
    public final static int MSG_FAILED_TO_REMOVE_TIMERS = 4128;
    public final static int MSG_FAILED_TO_REMOVE_WORKFLOW_TEMPLATE = 4129;
    public final static int MSG_FAILED_TO_REMOVE_WORKFLOW_INSTANCE = 4130;
    public final static int MSG_FAILED_TO_RESUME_WORKFLOW = 4131;
    public final static int MSG_FAILED_TO_SET_ACTION_ATTRIBUTE = 4132;
    public final static int MSG_FAILED_TO_SET_ROLE = 4133;
    public final static int MSG_FAILED_TO_SET_SEQUENCE = 4134;
    public final static int MSG_FAILED_TO_START_WORKFLOW = 4135;
    public final static int MSG_FAILED_TO_SUSPEND_WORKFLOW = 4136;
    public final static int MSG_FAILED_TO_UPDATE_TEMPLATE = 4137;
    public final static int MSG_FAILED_TO_SET_ROLESCRIPT = 4138;

    public final static String MSG_FAILED_TO_ACCEPT = "failedToAccept";
    public final static String MSG_FAILED_TO_ACCEPT_CANCELED = "failedToAcceptCanceled";
    public final static String MSG_FAILED_TO_ADVANCE = "failedToAdvance";
    public final static String MSG_FAILED_TO_ASSIGN_OWNERS = "failedToAssignOwners";
    public final static String MSG_FAILED_TO_COMMIT_TASK_ACTIVATION = "failedToCommitTaskActivation";
    public final static String MSG_FAILED_TO_CREATE_WF_INSTANCE = "failedToCreateWfInstance";
    public final static String MSG_FAILED_TO_CREATE_WF_TEMPLATE = "failedToCreateWfTemplate";
    public final static String MSG_FAILED_TO_GET_ACTIVE_TASKS = "failedToGetActiveTasks";
    public final static String MSG_FAILED_TO_GET_TASKS_ACCEPT_USER = "failedToGetTaskAcceptUser";
    public final static String MSG_FAILED_TO_GET_ACTIVITIES = "failedToGetActivities";
    public final static String MSG_FAILED_TO_GET_PLANS = "failedToGetPlans";
    public final static String MSG_FAILED_TO_GET_PROCESSES = "failedToGetProcesses";
    public final static String MSG_FAILED_TO_GET_TASKS_FOR_WF = "failedToGetTasksForWf";
    public final static String MSG_FAILED_TO_GET_WF_INSTANCE = "failedToGetWfInstance";
    public final static String MSG_FAILED_TO_GET_WF_TEMPLATE = "failedToGetWfTemplate";
    public final static String MSG_FAILED_TO_GET_WORK_ITEM = "failedToGetWorkItem";
    public final static String MSG_FAILED_TO_REJECT = "failedToReject";
    public final static String MSG_FAILED_TO_UPDATE_WF_INSTANCE = "failedToUpdateWfInstance";
    public final static String MSG_FAILED_TO_UPDATE_WF_TEMPLATE = "failedToUpdateWfTemplate";
    public final static String MSG_NOT_LOGGED_IN = "notLoggedInWf";
    public final static String MSG_FAILED_TO_ADD_RATE_SELECTION_CRITERIA = "failedToAddRateSelectionCriteria";
    public final static String MSG_FAILED_TO_UPDATE_WORKFLOW_STATE = "failedToUpdateWorkflowState";

    // Args: 1 - rate id
    public final static String MSG_FAILED_TO_ADD_RATE_ID = "failedToAddRateId";

    // Args: 1 - workflow id which is the iflow instance id
    public final static String MSG_FAILED_TO_FIND_DEFAULT_PATH = "failedToFindDefaultPath";

    // Args: 1 - role name (i.e. 1001 translate en_US fr_FR, 1001 translate
    // en_US fr_FR userid)
    public final static String MSG_FAILED_TO_GET_ASSIGNEES_FOR_ROLE = "failedToGetAssigneesForRole";

    // Args: 1 - sequence number of task
    // 2 - process instance id
    public final static String MSG_FAILED_TO_UPDATE_ASSIGNEES_FOR_TASK = "failedToUpdateAssigneesForTask";

    /**
     * Constructs an instance of WorkflowException using the given exception id,
     * id for the explanation of the error, and the original exception.
     * <p>
     * 
     * @param p_exceptionId
     *            Reason for the exception.
     * @param p_messageId
     *            The id for the explanation of the error.
     * @param p_originalException
     *            The original exception that this exception identifies.
     */
    public WorkflowException(int p_exceptionId, int p_messageId,
            Exception p_originalException)
    {
        super();
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructors to be used (the ones outside this block should be
    // removed.
    // ////////////////////////////////////////////////////////////////////
    /**
     * @see GeneralException#GeneralException(Exception) This constructor is
     *      used when a subclass of GeneralException is wrapped. In this case
     *      the wrapped exception already has the message related information
     *      (unless a new message or arguments are needed).
     * 
     * @param p_originalException
     *            Original exception that caused the error
     */
    public WorkflowException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception,
     *      String)
     * 
     * @param p_messageKey
     *            key in properties file
     * @param p_messageArguments
     *            Arguments to the message. It can be null.
     * @param p_originalException
     *            Original exception that caused the error. It can be null.
     */
    public WorkflowException(String p_messageKey, String[] p_messageArguments,
            Exception p_originalException)
    {
        this(p_messageKey, p_messageArguments, p_originalException,
                PROPERTY_FILE_NAME);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception,
     *      String)
     * 
     * @param p_messageKey
     *            key in properties file
     * @param p_messageArguments
     *            Arguments to the message. It can be null.
     * @param p_originalException
     *            Original exception that caused the error. It can be null.
     * @param p_propertyFileName
     *            Property file base name. If the property file is
     *            LingMessage.properties, the parameter should be "LingMessage".
     */
    protected WorkflowException(String p_messageKey,
            String[] p_messageArguments, Exception p_originalException,
            String p_propertyFileName)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
                p_propertyFileName);
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Constructors to be used (the ones outside this block should be
    // removed.
    // ////////////////////////////////////////////////////////////////////
}
