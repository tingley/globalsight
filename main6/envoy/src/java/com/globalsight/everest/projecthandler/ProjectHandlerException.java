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
package com.globalsight.everest.projecthandler;

import com.globalsight.util.GeneralException;

/**
 * An exception thrown during the process of using the projecthandler api
 * (projects and l10nprofiles)
 * 
 */
public class ProjectHandlerException extends GeneralException
{
    private static final long serialVersionUID = 1L;

    private static final String PROPERTY_FILE_NAME = "ProjectHandlerException";

    /*
     * Error messages
     */
    // Args: 1 = id of the user
    public final static String MSG_FAILED_TO_ACCESS_USERMANAGER = "FailedToAccessUserManagerComponent";
    // Args: 1 = project id
    public final static String MSG_FAILED_TO_CREATE_TM_FOR_PROJECT = "FailedToCreateTmForProject";
    public final static String MSG_FAILED_TO_GET_ALL_PROFILES = "FailedToGetAllProfiles";
    public final static String MSG_FAILED_TO_GET_ALL_PROFILES_FOR_GUI = MSG_FAILED_TO_GET_ALL_PROFILES;
    public final static String MSG_FAILED_TO_GET_ALL_PROFILE_NAMES = "FailedToGetProfileNames";
    public final static String MSG_FAILED_TO_GET_ALL_PROJECTS = "FailedToGetAllProjects";
    public final static String MSG_FAILED_TO_GET_ALL_PROJECTS_FOR_GUI = MSG_FAILED_TO_GET_ALL_PROJECTS;
    // Args: 1 = profile name
    public final static String MSG_FAILED_TO_ADD_PROFILE = "FailedToAddL10nProfile";
    // Args: 1 = profile name
    public final static String MSG_FAILED_TO_ADD_PROFILE_ALREADY_EXISTS = "FailedToAddL10nProfileAlreadyExists";
    // Args: 1 = project name
    public final static String MSG_FAILED_TO_ADD_PROJECT = "FailedToAddProject";
    // Args: 1 = project name
    public final static String MSG_FAILED_TO_ADD_PROJECT_ALREADY_EXISTS = "FailedToAddProjectAlreadyExists";
    // Args: 1 = project name
    public final static String MSG_FAILED_TO_ADD_VENDORS_TO_PROJECT = "FailedToAddVendorsToProject";
    // Args: 1 = profile id
    public final static String MSG_FAILED_TO_GET_LOCALIZATION_PROFILE = "FailedToGetL10nProfile";
    // Args: 1 = project id
    public final static String MSG_FAILED_TO_GET_PROJECT_BY_ID = "FailedToGetProject";
    // Args: 1 = project name
    public final static String MSG_FAILED_TO_GET_PROJECT_BY_NAME = "FailedToGetProject";
    // Args: 1 = pm user id
    public final static String MSG_FAILED_TO_GET_PROJECTS_BY_PM = "FailedToGetProjectsByPm";
    // Args: 1 = user id
    public final static String MSG_FAILED_TO_GET_PROJECT_INFOS_BY_USER = "FailedToGetProjectInfosByUser";
    // Args: 1 = user id
    public final static String MSG_FAILED_TO_GET_PROJECTS_BY_USER_ID = "FailedToGetProjectsByUser";
    // Args: 1 = list of user ids
    public final static String MSG_FAILED_TO_GET_PROJECTS_BY_USER_IDS = "FailedToGetProjectsByUserIds";
    // Args: 1 = vendor id
    public final static String MSG_FAILED_TO_GET_PROJECTS_BY_VENDOR_ID = "FailedToGetProjectsByVendorId";
    // Args: 1 - user id for permissions
    public final static String MSG_FAILED_TO_GET_PROJECTS_BY_PERMISSION = "FailedToGetProjectsByPermission";
    // Args: 1 = user id
    // Args: 2 = list of project ids
    public final static String MSG_FAILED_TO_ADD_USER_TO_PROJECTS = "FailedToAddUserToProjects";
    // Args: 1 = user id
    // Args: 2 = list of project ids
    public final static String MSG_FAILED_TO_REMOVE_USER_FROM_PROJECTS = "FailedToRemoveUserFromProjects";
    // Args: 1 = user id
    // Args: 2 = list of project ids
    public final static String MSG_FAILED_TO_ASSOCIATE_USER_WITH_PROJECTS = "FailedToAssociatedUserWithProjects";
    // Args: 1 = project manager user id
    public final static String MSG_FAILED_TO_GET_ALL_USERS_PM_MANAGES = "FailedToGetUsersPmManaages";

    // Args: 1 = workflow instance id
    public final static String MSG_FAILED_TO_GET_PROJECTS_BY_WFI_ID = "FailedToGetProjectsByWorkflowInstance";
    // Args: 1 = project id
    public final static String MSG_FAILED_TO_GET_WORKFLOW_INSTANCES_BY_PROJECT = "FailedToGetWorkflowInstancesOfProject";
    // Args: 1= project id
    public final static String MSG_FAILED_TO_MODIFY_PROJECT = "FailedToModifyProject";
    // Args: 1 = project id, 2 = new name for project
    public final static String MSG_FAILED_TO_MODIFY_PROJECT_NAME_EXISTS = "FailedToModifyProjectNameExists";
    // Args: 1 = project id
    public final static String MSG_FAILED_TO_REMOVE_PROJECT = "FailedToRemoveProject";
    // Args: 1 = profile id
    public final static String MSG_FAILED_TO_MODIFY_PROFILE = "FailedToModifyL10nProfile";
    // Args: 1 = profile if, 2 = new name for profile
    public final static String MSG_FAILED_TO_MODIFY_PROFILE_NAME_EXISTS = "FailedToModifyL10nProfileNameExists";
    // Args: 1 = profile id
    public final static String MSG_FAILED_TO_REMOVE_PROFILE = "FailedToRemoveL10nProfile";
    // Args: 1 = profile id
    public final static String MSG_FAILED_TO_DUPLICATE_PROFILE = "FailedToDuplicateL10nProfile";

    // Args: 1 = name
    public final static String MSG_FAILED_TO_CREATE_WFI = "FailedToCreateWfi";
    // Args: 1 = name
    public final static String MSG_FAILED_TO_DUPLICATE_WFI = "FailedToDuplicateWfi";
    public final static String MSG_FAILED_TO_DUPLICATE_WFIS = "FailedToDuplicateWfis";
    public final static String MSG_FAILED_TO_DUPLICATE_IFLOW_PLAN = "FailedToDuplicateIFlowPlan";
    // Args: 1 = id
    public final static String MSG_FAILED_TO_GET_WFI = "FailedToGetWfi";
    // Args: 0
    public final static String MSG_FAILED_TO_GET_WFIS = "FailedToGetWfis";
    // Args: 1 = name
    public final static String MSG_FAILED_TO_MODIFY_WFI = "FailedToModifyWfi";
    // Args: 1 = id
    public final static String MSG_FAILED_TO_REMOVE_WFI = "FailedToRemoveWfi";
    // Args: 1 = workflow template info id
    // 2 = l10n profile id
    public final static String MSG_FAILED_TO_REPLACE_WFI_IN_PROFILE = "FailedToReplaceWfiInProfile";

    //
    public final static String MSG_FAILED_TO_CREATE_TMP = "FailedToCreateTMP";
    // Args: 1 = id
    public final static String MSG_FAILED_TO_MODIFY_TMP = "FailedToModifyTMP";
    //
    public final static String MSG_FAILED_TO_GET_TM_PROFILES = "FailedToGetTMProfiles";
    //
    public final static String MSG_FAILED_TO_GET_MT_PROFILES = "FailedToGetMTProfiles";

    public final static String MSG_FAILED_TO_GET_TM_PROFILE_BY_ID = "FailedToGetTMProfileById";

    //
    public final static String MSG_FAILED_TO_CREATE_PROJECT_TM = "FailedToCreateProjectTM";
    // Args: 1 = id
    public final static String MSG_FAILED_TO_MODIFY_PROJECT_TM = "FailedToModifyProjectTM";
    //
    public final static String MSG_FAILED_TO_GET_PROJECT_TMS = "FailedToGetProjectTMs";
    // Args: 1 = id
    public final static String MSG_FAILED_TO_GET_PROJECT_TM_BY_ID = "FailedToGetProjectTmById";
    // Args: 1 = id
    public final static String MSG_FAILED_TO_REMOVE_PROJECT_TM = "FailedToRemoveProjectTm";

    public final static String MSG_FAILED_TO_REMOVE_TM_PROFILE = "FailedToRemoveTmProfile";

    public final static String MSG__CREATE_JMS_POOL_FAILED = "FailedToCreateJmsPool";
    // Args: 0
    public final static String MSG_FAILED_TO_GET_FPIS = "FailedToGetFpis";
    
    public final static String MSG_FAILED_TO_REMOVE_WF_STATE_POST_PROFILE = "FailedToRemoveWfStatePostProfile";

    /**
     * Create a ProjectHandlerException with the specified message.
     * 
     * @param p_messageKey
     *            The key to the message located in the property file.
     * @param p_messageArguments
     *            An array of arguments needed for the message. This can be
     *            null.
     * @param p_originalException
     *            The original exception that this one is wrapping. This can be
     *            null.
     */
    public ProjectHandlerException(String p_messageKey,
            String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
                PROPERTY_FILE_NAME);
    }

    /**
     * Create a ProjectHandlerException with the specified message.
     * 
     * @p_message The message.
     */
    public ProjectHandlerException(String p_message)
    {
        super(p_message);
    }

    /**
     * @see GeneralException#GeneralException(int, int, int, String)
     * @param p_message
     *            error message.
     * @param p_originalException
     *            original exception.
     * 
     * @deprecated It doesn't take a raw message any more
     */
    public ProjectHandlerException(String p_message,
            Exception p_originalException)
    {
        super(p_message, p_originalException);
    }
}
