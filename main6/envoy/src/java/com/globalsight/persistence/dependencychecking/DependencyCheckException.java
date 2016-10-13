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

package com.globalsight.persistence.dependencychecking;

import com.globalsight.util.GeneralException;

/**
 * An exception thrown during the process of checking for dependencies
 * on objects (done during the process of removing an object).
 */
public class DependencyCheckException
    extends GeneralException
{
    // property file names
    public final static String PROPERTY_FILE_NAME = "DependencyCheckException";

    // message keys in the property file
    public final static String MSG_FAILED_TO_FIND_WORKFLOW_SERVER =
        "FailedToFindWorkflowServer";
    public final static String MSG_FAILED_TO_FIND_USER_MANAGER =
        "FailedToFindUserManager";
    public final static String MSG_FAILED_TO_FIND_PROJECT_HANDLER = 
        "FailedToFindProjectHandler";
    // Args: 1 = fully qualified class name of the dependency checker
    //       2 = fully qualified class name of the object to check dependencies for
    public final static String MSG_INVALID_OBJECT =
        "InvalidObjectToCheckForDependencies";


    // User Dependency Exceptions
    // Args: 1 - user id
    public final static String FAILED_JOB_DEPENDENCIES_FOR_USER =
        "FailedToGetJobDependenciesForUser";
    // Args: 1 = user id
    public final static String FAILED_L10N_PROFILE_DEPENDENCIES_FOR_USER =
        "FailedToGetL10nProfileDependenciesForUser";
    // Args: 1 = user id
    public final static String FAILED_PROJECT_DEPENDENCIES_FOR_USER =
        "FailedToGetProjectDependenciesForUser";
    // Args: 1 = user id
    public final static String FAILED_WORKFLOW_DEPENDENCIES_FOR_USER =
        "FailedToGetWorkflowDependenciesForUser";
    // Args: 1 = user id
    public final static String FAILED_TO_RETRIEVE_ROLES_OF_USER=
        "FailedToGetRolesOfUser";

    // L10nProfile Dependency Exceptions
    // Args: 1 - L10nProfile id
    //       2 - L10nProfile name
    public final static String FAILED_FILE_PROFILE_DEPENDENCIES_FOR_L10N_PROFILE =
        "FailedToGetFileProfileDependenciesForL10nProfile";
    // Args: 1 - L10nProfile id
    //       2 - L10nProfile name
    public final static String FAILED_DB_PROFILE_DEPENDENCIES_FOR_L10N_PROFILE =
        "FailedToGetDbProfileDependenciesForL10nProfile";

    // Locale Pair Dependency Exceptions
    // Args: 1 - Source Locale (toString)
    //       2 - Target Locale (toString)
    public final static String FAILED_L10N_PROFILE_DEPENDENCIES_FOR_LOCALE_PAIR =
        "FailedToGetL10nProfileDependenciesForLocalePair";
    public final static String FAILED_WORKFLOW_TEMPLATE_DEPENDENCIES_FOR_LOCALE_PAIR = 
        "FailedToGetWorkflowTemplateDependenciesForLocalePair";
    public final static String FAILED_ACTIVE_JOB_DEPENDENCIES_FOR_LOCALE_PAIR =
        "FailedToGetActiveJobsForLocalePair";


    // Acitivity Dependency Exceptions
    // Args: 1 - Activity name
    public final static String FAILED_TO_GET_WORKFLOW_TEMPLATES_FOR_ACTIVITY =
        "FailedToGetWorkflowTemplatesForActivity";
    // Args: 1-  A string of all workflow template ids
    public final static String FAILED_TO_GET_L10N_PROFILES_BY_WORKLFLOW_TEMPLATE_IDS =
        "FailedToGetL10nProfilesByWorkflowTemplateId";

    /**
     * Create a DependencyCheckException with the specified message.
     *
     * @param p_messageKey The key to the message located in the property file.
     * @param p_messageArguments An array of arguments needed for the
     * message.  This can be null.
     * @param p_originalException The original exception that this one
     * is wrapping.  This can be null.
     */
    public DependencyCheckException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }
}
