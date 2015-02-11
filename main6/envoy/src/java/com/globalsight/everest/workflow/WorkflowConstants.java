/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.workflow;

import com.globalsight.util.date.DateHelper;

/**
 * This class contains the workflow related constants.
 * 
 */
public class WorkflowConstants
{
    /**
     * Constant used for process-definition element in jbpm template xml.
     */
    public static final String PROCESS_DEFINITION = "process-definition";

    /**
     * Constant used for start-state element in jbpm template xml.
     */
    public static final String START_STATE = "start-state";

    /**
     * Constant used for transition element in jbpm template xml.
     */
    public static final String TRANSITION = "transition";

    /**
     * Constant used for action element in jbpm template xml.
     */
    public static final String ACTION = "action";

    /**
     * Constant used for task-node element in jbpm template xml.
     */
    public static final String TASK_NODE = "task-node";

    /**
     * Constant used for task element in jbpm template xml.
     */
    public static final String TASK = "task";

    /**
     * Constant used for assignment element in jbpm template xml.
     */
    public static final String ASSIGNMENT = "assignment";

    /**
     * Constant used for controller element in jbpm template xml.
     */
    public static final String CONTROLLER = "controller";

    /**
     * Constant used for variable element in jbpm template xml.
     */
    public static final String VARIABLE = "variable";

    /**
     * Constant used for decision element in jbpm template xml.
     */
    public static final String DECISION = "decision";

    /**
     * Constant used for handler element in jbpm template xml.
     */
    public static final String HANDLER = "handler";

    /**
     * Constant used for end-state element in jbpm template xml.
     */
    public static final String END_STATE = "end-state";

    /**
     * Constant used for name attribute of the nodes in jbpm template xml.
     */
    public static final String ATTR_NAME = "name";

    /**
     * Constant used for class attribute of the nodes in jbpm template xml.
     */
    public static final String ATTR_CLASS = "class";

    /**
     * Constant used for to attribute of the nodes in jbpm template xml.
     */
    public static final String ATTR_TO = "to";

    /**
     * Constant used for access attribute of the variable node in jbpm template
     * xml.
     */
    public static final String ATTR_ACCESS = "access";

    /**
     * Constant used for access read and write level of the variable in jbpm
     * template xml.
     */
    public static final String VARIABLE_ACCESS_RW = "read,write";

    /**
     * Constant used for the variable isRejected in jbpm template xml.
     */
    public static final String VARIABLE_IS_REJECTED = "isRejected";

    /**
     * Constant used for the variable userId in jbpm template xml.
     */
    public static final String VARIABLE_USER_ID = "userId";

    /**
     * Constant used for the variable goTo in jbpm template xml.
     */
    public static final String VARIABLE_GOTO = "goTo";

    /**
     * Constant used for the value of variable goTo when doing skip operation.
     */
    public static final String VARIABLE_GOTO_SKIP = "to_skip";

    /**
     * Constant used for the separator of the point field in jbpm template xml.
     */
    public static final String POINT_SEPARATOR = ":";

    /**
     * Constant used for the separator of the name field in jbpm template xml.
     */
    public static final String NAME_SEPARATOR = "_";

    /**
     * Constant used for start_node field for a end-state node in jbpm template
     * xml.
     */
    public static final String FIELD_START_STATE = "start_node";

    /**
     * Constant used for workflow_manager field for activity nodes in jbpm
     * template xml.
     */
    public static final String FIELD_MANAGER = "workflow_manager";

    /**
     * Constant used for workflow_pm field for activity nodes in jbpm template
     * xml.
     */
    public static final String FIELD_PM = "workflow_pm";

    /**
     * Constant used for workflow_description field for a start-state node in
     * jbpm template xml.
     */
    public static final String FIELD_DESCRIPTION = "workflow_description";

    /**
     * Constant used for activity field for a task node in jbpm template xml.
     */
    public static final String FIELD_ACTIVITY = "activity";
    
    /**
     * Constant used for report_upload_check field for a task node in jbpm template xml.
     */
    public static final String FIELD_REPORT_UPLOAD_CHECK = "report_upload_check";

    /**
     * Constant used for roles field for a task node in jbpm template xml.
     */
    public static final String FIELD_ROLES = "roles";

    /**
     * Constant used for accepted_time field for a task node in jbpm template
     * xml.
     */
    public static final String FIELD_ACCEPTED_TIME = "accepted_time";

    /**
     * Constant used for completed_time field for a task node in jbpm template
     * xml.
     */
    public static final String FIELD_COMPLETED_TIME = "completed_time";

    /**
     * Constant used for overduetoPM_time field for a task node in jbpm template
     * xml.
     */
    public static final String FIELD_OVERDUE_PM_TIME = "overdueToPM_time";

    /**
     * Constant used for overduetoUser_time field for a task node in jbpm
     * template xml.
     */
    public static final String FIELD_OVERDUE_USER_TIME = "overdueToUser_time";

    /**
     * Constant used for rate_selection_criteria field for a task node in jbpm
     * template xml.
     */
    public static final String FIELD_RATE_SELECTION_CRITERIA = "rate_selection_criteria";

    /**
     * Constant used for expense_rate_id field for a task node in jbpm template
     * xml.
     */
    public static final String FIELD_EXPENSE_RATE_ID = "expense_rate_id";

    /**
     * Constant used for revenue_rate_id field for a task node in jbpm template
     * xml.
     */
    public static final String FIELD_REVENUE_RATE_ID = "revenue_rate_id";

    /**
     * Constant used for role_name field for a task node in jbpm template xml.
     */
    public static final String FIELD_ROLE_NAME = "role_name";

    /**
     * Constant used for role_id field for a task node in jbpm template xml.
     */
    public static final String FIELD_ROLE_ID = "role_id";

    /**
     * Constant used for action_type field for a task node in jbpm template xml.
     */
    public static final String FIELD_ACTION_TYPE = "action_type";

    /**
     * Constant used for role_preference field for a task node in jbpm template
     * xml.
     */
    public static final String FIELD_ROLE_PREFERENCE = "role_preference";

    /**
     * Constant used for role_type field for a task node in jbpm template xml.
     */
    public static final String FIELD_ROLE_TYPE = "role_type";

    /**
     * Constant used for workflow_data_item field for a task node in jbpm
     * template xml.
     */
    public static final String FIELD_WORKFLOW_DATA_ITEM = "workflow_data_item";

    /**
     * Constant used for name field for a workflow data item in jbpm template
     * xml.
     */
    public static final String FIELD_WORKFLOW_DATA_ITEM_NAME = "name";

    /**
     * Constant used for type field for a workflow data item in jbpm template
     * xml.
     */
    public static final String FIELD_WORKFLOW_DATA_ITEM_TYPE = "type";

    /**
     * Constant used for value field for a workflow data item in jbpm template
     * xml.
     */
    public static final String FIELD_WORKFLOW_DATA_ITEM_VALUE = "value";

    /**
     * Constant used for workflow_condition_spec field for a decision node in
     * jbpm template xml.
     */
    public static final String FIELD_WORKFLOW_CONDITION_SPEC = "workflow_condition_spec";

    /**
     * Constant used for workflow_branch_spec field for a workflow condition
     * spec in a decision node.
     */
    public static final String FIELD_WORKFLOW_BRANCH_SPEC = "workflow_branch_spec";

    /**
     * Constant used for comparison_operator field for a workflow branch spec in
     * a decision node.
     */
    public static final String FIELD_COMPARISON_OPERATOR = "comparison_operator";

    /**
     * Constant used for branch_value field for a workflow branch spec in a
     * decision node.
     */
    public static final String FIELD_BRANCH_VALUE = "branch_value";

    /**
     * Constant used for arrow_label field for a workflow branch spec in a
     * decision node.
     */
    public static final String FIELD_ARROW_LABEL = "arrow_label";

    /**
     * Constant used for is_default field for a workflow branch spec in a
     * decision node.
     */
    public static final String FIELD_IS_DEFAULT = "is_default";

    /**
     * Constant used for structural_state field.
     */
    public static final String FIELD_STRUCTUAL_STATE = "structural_state";

    /**
     * Constant used for max-node-id field.
     */
    public static final String FIELD_MAX_NODE_ID = "max_node_id";

    /**
     * Constant used for condition_attribute field for a workflow condition spec
     * in a decision node.
     */
    public static final String FIELD_CONDITION_ATTRIBUTE = "condition_attribute";

    /**
     * Constant used for sequence field for a node in jbpm template xml.
     */
    public static final String FIELD_SEQUENCE = "sequence";

    /**
     * Constant used for point field for a node in jbpm template xml.
     */
    public static final String FIELD_POINT = "point";

    /**
     * Constant used for leaving field for a node in jbpm template xml.
     */
    public static final String FIELD_LEAVING = "leaving";

    /**
     * Constant used for arriving field for a node in jbpm template xml.
     */
    public static final String FIELD_ARRIVING = "arriving";

    /**
     * Constant used for intermediate_points field for a node in jbpm template
     * xml.
     */
    public static final String FIELD_INTERMEDIATE_POINTS = "intermediate_points";

    /* The activity need be skipped to ,stored in the exit node */
    public static final String FIELD_SKIP = "skip";

    /**
     * Constant used for a node name in jbpm template xml.
     */
    public static final String NAME_NODE = "node";

    /**
     * Constant used for a task name in jbpm template xml.
     */
    public static final String NAME_TASK = "task";

    /**
     * Constant used for the xml suffix of jbpm template xml.
     */
    public static final String SUFFIX_XML = ".xml";

    /**
     * Constant identifying the email sender(s) address.
     */
    public static final int MAIL_FROM = 1;

    /**
     * Constant indenitfying the email address of the recipient(s).
     */
    public static final int MAIL_TO = 2;

    /**
     * Constant identifying the cc recipient(s).
     */
    public static final int MAIL_CC = 3;

    /**
     * Constant identifying the bcc recipient(s).
     */
    public static final int MAIL_BCC = 4;

    /**
     * Constant identifying the email's subject.
     */
    public static final int MAIL_SUBJECT = 5;

    /**
     * Constant identifying the email's body.
     */
    public static final int MAIL_BODY = 6;

    /**
     * Constant identifying a name for the start node of a workflow.
     */
    public static final String START_NODE = "Start";

    /**
     * Constant identifying a name for the end node of a workflow.
     */
    public static final String END_NODE = "Exit";

    /**
     * Constant identifying a name for the stop node of a workflow.
     */
    public static final String STOP_NODE = "Stop";

    /**
     * Constant identifying the arrow connecting nodes of a workflow. This node
     * is only used in the back-end process for iFlow purposes and is not
     * exposed to the client.
     */
    public static final String ARROW_NAME = "arrow";

    /**
     * Constant used for getting a list of activities in workflow instance.
     */
    public static final String TASK_INFOS = "taskInfos";

    /**
     * Constant used for getting a taskInfo in a workflow instance.
     */
    public static final String TASK_INFO = "taskInfo";

    // ////////////////////////// Filter types ////////////////////////////

    // ///////////////////////// Process Instance States
    // /////////////////////////

    // ////////////////////////// Node Types ////////////////////////////

    /**
     * Constant identifying the start node of a workflow. This node is only used
     * in the back-end process for jBPM purposes and is not exposed to the
     * client.
     */
    public static final int START = 0;

    /**
     * Constant identifying the end node of a workflow. This node is only used
     * in the back-end process for jBPM purposes and is not exposed to the
     * client.
     */
    public static final int STOP = 1;

    /**
     * Constant identifying the Activity node type of a workflow.
     */
    public static final int ACTIVITY = 2;

    /**
     * Constant identifying the AND node type of a workflow.
     */
    public static final int AND = 3;

    /**
     * Constant identifying the OR node type of a workflow.
     */
    public static final int OR = 4;

    /**
     * Constant identifying the CONDITION node type of a workflow.
     */
    public static final int CONDITION = 5;

    /**
     * Constant identifying the Sub-Process node.
     */
    public static final int SUB_PROCESS = 6;

    /**
     * Constant identifying the maximum length of the node name.
     */
    public static final int MAX_NODE_NAME_LENGTH = 30;

    // ////////////////////////// Node Instance States
    // ////////////////////////////
    /**
     * Constant identifying the active state for a workflow instance's node.
     */
    public static final int STATE_RUNNING = 3;

    /**
     * Constant identifying the initial state for a workflow instance's node.
     */
    public static final int STATE_INITIAL = 0;

    /**
     * Constant identifying the completed state for a workflow instance's node.
     */
    public static final int STATE_COMPLETED = 5;

    /**
     * Constant identifying the state in which the node is waiting on a
     * sub-process to be completed.
     */
    public static final int STATE_WAITING_ON_SUB_PROCESS = 4;

    /**
     * Constant identifying the criteria for rate selection depending on who
     * accepted the task.
     */
    public static final int USE_ONLY_SELECTED_RATE = 1;
    
    public static final int REPORT_UPLOAD_CHECK = 0;

    public static final int USE_SELECTED_RATE_UNTIL_ACCEPTANCE = 2;

    // ////////////////////////// Arrow Types & States
    // ///////////////////////////
    /**
     * Constant identifying the regular type of arrow.
     */
    public static final int REGULAR_ARROW = 0;

    /**
     * Constant used to ignore a particular work item state. This constant is
     * used when getting all work items for a particular NodeInstanceId where
     * state is not important for filteration.
     */
    public static final int TASK_ALL_STATES = -10;

    public static final int TASK_FINISHING = 10;

    public static final int TASK_SKIP = 11;

    /**
     * Constant identifying an active state for a task.
     */
    public static final int TASK_ACTIVE = 3;

    /**
     * Constant identifying a deactive state for a task.
     */
    public static final int TASK_DEACTIVE = 4;

    /**
     * Constant identifying an accepted state for a task.
     */
    public static final int TASK_ACCEPTED = 8;

    // The three constants are for GlobalSight Edition tasks.
    public static final int TASK_DISPATCHED_TO_TRANSLATION = 81;
    public static final int TASK_IN_TRANSLATION = 82;
    public static final int TASK_TRANSLATION_COMPLETED = 83;
    public static final int TASK_READEAY_DISPATCH_GSEDTION = 84;
    public static final int TASK_GSEDITION_IN_PROGESS = 85;

    /**
     * Constant identifying a declined state for a task.
     */
    public static final int TASK_DECLINED = 6;

    /**
     * Constant identifying a completed state for a task.
     */
    public static final int TASK_COMPLETED = -1;

    // ////////////////////////// DataItemRef types ////////////////////////////
    /**
     * Constant identifying a Boolean type for a DataItemRef.
     */
    public static final String BOOLEAN = "BOOLEAN";

    /**
     * Constant identifying a Float type for a DataItemRef.
     */
    public static final String FLOAT = "FLOAT";

    /**
     * Constant identifying an Integer type for a DataItemRef.
     */
    public static final String INTEGER = "INTEGER";

    /**
     * Constant identifying a Long type for a DataItemRef.
     */
    public static final String LONG = "LONG";

    /**
     * Constant identifying a String type for a DataItemRef.
     */
    public static final String STRING = "STRING	";

    // //////////////////////////TimerAction names ////////////////////////////
    /**
     * Constant identifying the MAIL_FROM for an iFlow TimerAction object.
     */
    public static final String MAIL_FROM_SUFFIX = "_mfrom";

    /**
     * Constant identifying the MAIL_SUBJECT for an iFlow TimerAction object.
     */
    public static final String MAIL_SUBJECT_SUFFIX = "_msub";

    /**
     * Constant identifying the MAIL_BODY for an iFlow TimerAction object.
     */
    public static final String MAIL_BODY_SUFFIX = "_mbody";

    /**
     * Constant identifying the MAIL_TO for an iFlow TimerAction object.
     */
    public static final String MAIL_TO_SUFFIX = "_mto";

    /**
     * Constant identifying the MAIL_CC for an iFlow TimerAction object.
     */
    public static final String MAIL_CC_SUFFIX = "_mcc";

    /**
     * Constant identifying the MAIL_BCC for an iFlow TimerAction object.
     */
    public static final String MAIL_BCC_SUFFIX = "_mbcc";

    // timer names and timer related attribute names. We currently support two
    // types of durations:
    // 1. a duration for accepting a task.
    // 2. a duration for task completion.
    /**
     * Constant identifying a duration for accepting a task.
     */
    public static final String ACCEPT = "Accept";

    /**
     * Constant identifying a duration for task completion.
     */
    public static final String COMPLETE = "Complete";

    /**
     * Constant identifying a duration for overdue to PM.
     */
    public static final String OVERDUETOPM = "overduetopm";

    /**
     * Constant identifying a duration for overdue to user.
     */
    public static final String OVERDUETOUSER = "overduetouser";

    // //////////////////////////Role types ///////////////////////
    /**
     * Constant identifying a role attribute used as a key for role types.
     */
    public static final String ROLE = "role";

    /**
     * Constant identifying a "User Role" role type.
     */
    public static final String USER_ROLE = "UserRole";

    /**
     * Constant identifying a "Container Role" role type.
     */
    public static final String CONTAINER_ROLE = "ContainerRole";

    /**
     * Constant identifying a node without a role type.
     */
    public static final String UNDEFINED_ROLE_TYPE = "UndefinedRoleType";

    /**
     * Constant identifying the preference of a role (i.e. fastest, available,
     * etc.) This is the key to the UDA created for a node.
     */
    public static final String ROLE_PREFERENCE = "ROLE_PREFERENCE_";

    // ///////////////// Role Preference types ////////////////////////
    /**
     * Constant identifying the fastest resources preference.
     */
    public static final String FASTEST_ROLE_PREFERENCE = "fastestResources";

    /**
     * Constant identifying the available resources preference.
     */
    public static final String AVAILABLE_ROLE_PREFERENCE = "availableResources";

    // ////////////////////////// TimerAciton Types ////////////////////////////
    /**
     * Constant identifying a "send mail" action.
     */
    public static final int SENDMAIL = 1;

    // ////////////////////////// TimerDef Types ////////////////////////////
    /**
     * Constant identifying a relative time.
     */
    public static final int RELATIVE = 0;

    /**
     * Constant identifying an absolute time.
     */
    public static final int ABSOLUTE = 1;

    /**
     * Constant identifying a period of time.
     */
    public static final int PERIODIC = 2;

    // ////////////////////////// Structural States ////////////////////////////
    /**
     * Constant identifying a new object.
     */
    public static final int NEW = 0;

    /**
     * Constant identifying an object in modified state (should be updated).
     */
    public static final int MODIFIED = 1;

    /**
     * Constant identifying an object in removed state (should be removed).
     */
    public static final int REMOVED = 2;

    /**
     * Constant identifying an object in an unchanged state.
     */
    public static final int UNCHANGED = 3;

    // ////////////////////////// Comparison Operators
    // ////////////////////////////

    // ////////// property keys and values for creating a WFSession ////////////
    /**
     * Constant identifying the prefix of naming context. The naming context is
     * basically "prefix" + server host + suffix.
     */
    public static final String NAMING_CONTEXT_PREFIX = "TwfNameFactory_";

    /**
     * Constant identifying the suffix of the naming context. The naming context
     * is basically "prefix" + server host + suffix.
     */
    public static final String NAMING_CONTEXT_SUFFIX = "/1";

    /**
     * Constant identifying a suffix for the iFlow server name (server host +
     * suffix).
     */
    public static final String FLOW = "Flow";

    /**
     * Constant identifying the transport type.
     */
    public static final String TRANSPORT_TYPE = "TWFTransportType";

    /**
     * Constant identifying the RMI protocol.
     */
    public static final String RMI = "RMI";

    /**
     * Constant identifying the naming provider.
     */
    public static final String NAMING_PROVIDER = "java.naming.provider.url";

    /**
     * Constant identifying the protocol used by iFlow's model.
     */
    public static final String PROTOCOL = "rmi://";

    /**
     * Constant identifying the port used by RMI.
     */
    public static final String PORT = ":1099";

    /**
     * user name paramter in the envoy.properties file used for iFlow login.
     */
    public static final String USER_NAME = "iflow.username";

    /**
     * Password paramter in the envoy.properties file used for iFlow login.
     */
    public static final String PASSWORD = "iflow.password";

    /**
     * Host paramter in the envoy.properties file used for iFlow login.
     */
    public static final String HOST = "iflow.host";

    // ////////////////////////////// for reroute/reassign email action
    // ///////////////////
    public static final String IS_NEW = "isNew";

    public static final String IS_DELETED = "isDeleted";

    public static final String IS_REASSIGNED = "isReassigned";

    public static final String IS_MODIFIED = "isModified";

    public static final String IS_BACKUP_FILE_MAP = "isBackupFileMap";

    public static final String LIST_SIZE = "listSize";

    public static final String TASK_ID = "taskId";

    public static final String TASK_SEQ = "taskSeq";

    public static final String ROLE_FOR_ACTIVATE = "roleForActivate";

    public static final String ROLE_FOR_DEACTIVATE = "roleForDeactivate";

    public static final String NAME_FOR_DEACTIVATE = "activityNameForDeactive";

    public static final String UPDATE_EVENT = "shouldEventBeUpdated";

    // ////////////////////////// for iFlow's scripts
    // ////////////////////////////////
    public static final String ROLE_SCRIPT_PREFIX = "setActivityAssignees(getProcessAttribute(\"";

    public static final String ROLE_SCRIPT_SUFFIX = "\"))";

    public static final String DOUBLE_QUOTE = "\"";

    public static final String PROCESS_OWNER = "setProcessOwners(\"";

    public static final String PROCESS_OWNER_SUFFIX = "\")";

    public static final String PROCESS_NAME = "setProcessName(\"";

    // //// prefix for data item ref for rate id ////
    // To save lot of efforts of migration and testing, keeping this
    // as RATE. Note that this PREFIX_RATE_NAME is rate for
    // expenses.
    static final String PREFIX_RATE_NAME = "RATE_";

    static final String PREFIX_REVENUE_RATE_NAME = "REVENUE_RATE_";

    // Constant used to decide the rate selection criteria
    static final String PREFIX_RATE_SELECTION_CRITERIA_NAME = "RATE_SELECTION_CRITERIA_";

    // constants for names of UDAs
    public static final String PREFIX_ROLE_NAME = "ROLE_NAME_";

    public static final String PREFIX_ROLE = "ROLE_";

    public static final String PREFIX_ASSIGNEES = "ASSIGNEES_";

    /**
     * Constant used as name in locale property files. This name is used for
     * getting a display string as a condition name. Note that the value is used
     * in the property files as a key for locale based values.
     */
    public static final String CONDITION_UDA = "lb_condition_uda";

    /**
     * Constant used as a key for the "Create Secondary Target Files" system
     * action type associated with a particular task. This is a UDA key and its
     * value is used as a localization property file's key.
     */
    public static final String CSTF_ACTION = "CSTF_ACTION_";

    /**
     * Constant used as the seperator to seperate the name and the description.
     * 
     */
    public static final String SEPERATOR = "|";

    /**
     * Accept/Complete/Overdue Time for workflow.
     */
    public static final String daysToAccept = "1";
    public static final String hoursToAccept = "0";
    public static final String minutesToAccept = "0";
    public static final String daysToComplete = "1";
    public static final String hoursToComplete = "0";
    public static final String minutesToComplete = "0";
    public static final String daysOverDueToPM = "1";
    public static final String hoursOverDueToPM = "0";
    public static final String minutesOverDueToPM = "0";
    public static final String daysOverDueToUser = "0";
    public static final String hoursOverDueToUser = "1";
    public static final String minutesOverDueToUser = "0";

    public static final long accept_time = DateHelper.milliseconds(
            daysToAccept, hoursToAccept, minutesToAccept);
    public static final long complete_time = DateHelper.milliseconds(
            daysToComplete, hoursToComplete, minutesToComplete);
    public static final long overDuePM_time = DateHelper.milliseconds(
            daysOverDueToPM, hoursOverDueToPM, minutesOverDueToPM);
    public static final long overDueUser_time = DateHelper.milliseconds(
            daysOverDueToUser, hoursOverDueToUser, minutesOverDueToUser);

    /**
     * TASK_TYPE means new/accept/complete the task/activity.
     */
    public static final String TASK_TYPE_NEW = "new";
    public static final String TASK_TYPE_ACC = "accept";
    public static final String TASK_TYPE_COM = "complete";

}
