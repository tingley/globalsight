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
package com.globalsight.everest.usermgr;

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
import com.globalsight.util.GeneralExceptionConstants;

/**
 * An exception handling object for user manager component.
 */
public class UserManagerException extends GeneralException
    implements GeneralExceptionConstants
{
    ///////////////////// Error Message Keys //////////////////////////////

    //Messages for User based functions
    public final static String MSG_ADD_USER_ERROR = "addUserError";
    public final static String MSG_MODIFY_USER_ERROR = "modifyUserError";
    public final static String MSG_DELETE_USER_ERROR = "deleteUserError";
    public final static String MSG_DEACTIVATE_USER_ERROR = "deactivateUserError";
    public final static String MSG_REACTIVATE_USER_ERROR = "reactivateUserError";
    public final static String MSG_GET_USERS_ERROR = "getUsersError";
    public final static String MSG_GET_USER_ERROR = "getUserError";
    public final static String MSG_GET_VENDORLESS_USERS_ERROR = "getVendorlessUsersError";
    public final static String MSG_GET_EMAILS_FORUSERS_ERROR = "getEmailsForUsersError";

    //Messages for Role based functions
    public final static String MSG_ADD_ROLE_ERROR = "addRoleError";
    public final static String MSG_DELETE_ROLE_ERROR = "deleteRoleError";
    public final static String MSG_ADD_USERS_TO_ROLE_ERROR
        = "addUsersToRoleError";
    public final static String MSG_ACTIVATE_USER_ROLES_ERROR = 
        "activateUserRolesError";
    public final static String MSG_DELETE_USERS_FROM_ROLE_ERROR
        = "deleteUsersFromRoleError";
    public final static String MSG_DEACTIVATE_USER_ROLES_ERROR 
        = "deactivateUsersFromRolesError";
    public final static String MSG_GET_ROLES_ERROR  = "getRolesError";
    public final static String MSG_GET_ACTIVITY_ERROR = "getActivityError";
    public final static String MSG_GET_JOB_HANDLER_ERROR = 
        "getJobHandlerError";
    public final static String MSG_GET_COSTING_ENGINE_ERROR = 
        "getCostingEngineError";
    // Args: 1 - rate id,  2 - role name
    public final static String MSG_ADD_RATE_TO_ROLE_ERROR = 
        "addRateToRoleError";
    // Args: 1 - activity name, 2 - source locale, 3 - target locale
    public final static String MSG_ROLE_DOES_NOT_EXIST = 
        "roleDoesNotExist";
    // Args: 1 - rate id, 2 - role name
    public final static String MSG_REMOVE_RATE_FROM_ROLE_ERROR = 
        "removeRateFromRoleError";
    // Args: 1 - name of roles  or ALL for all of them
    //       2 - user id
    public final static String MSG_FAILED_TO_ADD_ROLES_TO_USER =
        "addRolesToUserError";

    //Other function error messages
    public final static String MSG_DUPLICATE_RECORD_ERROR = "duplicateRecordError";
    public final static String MSG_INIT_SERVER_ERROR 	= "initServerError";
    public final static String MSG_AUTHENTICATE_BINDING_USER_ERROR =
        "authenticateBindingUserError";
    public final static String MSG_SET_CONNECTION_OPTIONS_ERROR =
        "setConnectionOptionsError";

    public final static String MSG_GET_COMPANY_NAMES_ERROR = "getCompanyNamesError";
    // Args 1: user id
    // Args 2: project is list
    public final static String MSG_FAILED_TO_ASSOCIATE_USER_WITH_PROJECTS = 
        "failedToAssociateUserWIthProjects";

    ////////////////// Error Message arguments //////////////////////////
    // Note: These arguments are used for system logging as well as exception messages.
    //       But the exception property file (UserManagerException.properties)
    //       can define the way to display the argument or not and how.
    /////////////////////////////////////////////////////////////////////

    public final static String ARG_FAILED_TO_DELETE_USER_FROM_ROLES = "Failed to delete the user from the roles the user attached to";
    public final static String ARG_FAILED_TO_DEACTIVATE_USER_ROLES = "Failed to deactivate the user roles attached to";
    public final static String ARG_FAILED_TO_GET_UIDS_FROM_ROLE = "Failed to get user ids from role subtree";
    public final static String ARG_FAILED_TO_GET_UIDS_FROM_USER = "Failed to get user ids from user subtree";
    public final static String ARG_FAILED_TO_GET_USERS_ENTRIES = "Failed to get user entries";
    public final static String ARG_FAILED_TO_REACTIVATE_ROLE = "Failed to reactivate role";
    public final static String ARG_FAILED_TO_VALIDATE_USERS = "Failed to validate the users";
    public final static String ARG_INVALID_ATTRIBUTE = "Invalid attribute";
    public final static String ARG_INVALID_ROLE = "Invalid Role parameter";
    public final static String ARG_INVALID_ROLE_NAME = "Invalid role name parameter";
    public final static String ARG_INVALID_USER = "Invalid User parameter";
    public final static String ARG_LDAP_UNKNOWN_ERROR = "Some other LDAP error";
    public final static String ARG_PROTECTED_USER = "User is protected";
    public final static String ARG_RATES_ALREADY_IN_ROLE = "Some rates are already in the Role";
    public final static String ARG_RATES_NOT_IN_ROLE = "Some rates are not part of the Role";
    public final static String ARG_ROLE_ALREADY_EXIST = "Role already exists";
    public final static String ARG_ROLE_NOT_EXIST = "Role does not exist";
    public final static String ARG_USERS_ALRAEDY_IN_ROLE = "Some users are already in the Role";
    public final static String ARG_USERS_NOT_EXIST = "Some of the users don't not exist";
    public final static String ARG_USERS_NOT_IN_ROLE = "Some users are not in the Role";
    public final static String ARG_USER_ALREADY_EXIST = "User already exists";
    public final static String ARG_USER_NOT_EXIST = "User does not exist";
    public final static String ARG_USER_YOURSELF = "You cannot remove yourself";
    public final static String ARG_USER_ADMIN = "You cannot remove an Administrator user";

    public final static String ARG_FAILED_TO_GET_COMPANY_ENTRIES = "Failed to get all the company names the users are associated with.";



    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public UserManagerException(Exception p_originalException)
    {
        super(p_originalException);
    }

    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error.
     * It can be null.
     */
    public UserManagerException(String p_messageKey, String[] p_messageArguments,
            Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
