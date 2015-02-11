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
package com.globalsight.everest.persistence;

import com.globalsight.util.GeneralException;

/**
 * PersistenceException represents an exception that occurs during
 * execution of any functionality on the PersistenceService.
 */
public class PersistenceException
    extends GeneralException
{
    //
    // PUBLIC CONSTANTS FOR MESSAGE NAMES
    //
    public static final String MSG_FAILED_TO_CREATE_CLASS =
        "CantCreateClass";
    public static final String MSG_INVALID_LOCKING_METHOD =
        "InvalidLockMode";
    public static final String MSG_GENERAL_EXCEPTION =
        "GeneralPersistenceException";
    public static final String MSG_MISSING_CONFIG_PARAM =
        "MissingConfigParam";
    public static final String MSG_BAD_CONFIG_PARAM =
        "InvalidConfigParam";
    public static final String MSG_ALREADY_LOGGED_IN =
        "AlreadyLoggedIn";
    public static final String MSG_FAILED_TO_LOGIN_TO_DB =
        "CantLoginToDb";
    public static final String MSG_FAILED_TO_CHANGE_LOGIN_INFORMATION =
        "CantChangeLoginInfo";
    public static final String MSG_FAILED_TO_ACQUIRE_CLIENT_SESSION =
        "CantGetClientSession";
    public static final String MSG_FAILED_TO_ACQUIRE_SERVER_SESSION =
        "CantGetServerSession";
    public static final String MSG_FAILED_TO_ACQUIRE_UNIT_OF_WORK =
        "CantGetUnitOfWork";
    public static final String MSG_FAILED_TO_DELETE_OBJECT =
        "CantDeleteObject";
    public static final String MSG_FAILED_TO_DELETE_ALL_OBJECTS =
        "CantDeleteObjectCollection";
    public static final String MSG_FAILED_TO_INSERT_OBJECT =
        "CantInsertObject";
    public static final String MSG_OBJECT_ALREADY_EXISTS =
        "CantInsertNonuniqueObject";
    public static final String MSG_FAILED_TO_INSERT_ALL_OBJECTS =
        "CantInsertObjectCollection";
    public static final String MSG_IMMUTABLE_OBJECT =
        "ImmutableObject";
    public static final String MSG_OBJECT_DELETED_FROM_DB =
        "CantUpdateDeletedObject";
    public static final String MSG_FAILED_TO_UPDATE_OBJECT =
        "CantUpdateObject";
    public static final String MSG_FAILED_TO_UPDATE_ALL_OBJECTS =
        "CantUpdateObjectCollection";
    public static final String MSG_FAILED_TO_EXECUTE_NAMED_QUERY =
        "CantExecuteNamedQuery";
    public static final String MSG_FAILED_TO_BUILD_QUERY =
        "CantBuildQuery";
    public static final String MSG_FAILED_TO_FIND_QUERY =
        "CantFindQuery";
    public static final String MSG_FAILED_TO_RETRIEVE_QUERY =
        "CantRetrieveQuery";
    public static final String MSG_FAILED_TO_BUILD_RESULT_SET =
        "CantBuildResultSet";
    public static final String MSG_INVALID_QUERY =
        "InvalidReadQuery";
    public static final String MSG_FAILED_TO_INSERT_USING_STORED_PROCEDURE=
        "failedInsertUsingStoredProcedure";
    public static final String MSG_FAILED_TO_RUN_QUERY_USING_STORED_PROCEDURE=
        "failedQueryUsingStoredProcedure";
    public static final String MSG_FAILED_TO_READ_SYSTEM_CONFIGURATION_FILE =
        "failedToReadSystemConfig";

    /**
     * Creates a PersistenceException by specifying a message key.
     * <p>
     * @param p_messageKey the key that specifies the message to be displayed.
     */
    public PersistenceException(String p_messageKey)
    {
        this(p_messageKey, null, null);
    }

    /**
     * Creates a PersistenceException by specifying a message key and an
     * originating exception.
     * <p>
     * @param p_messageKey the key that specifies the message to be displayed.
     * @param p_originalException the origininal exception.
     */
    public PersistenceException(String p_messageKey,
        Exception p_originalException)
    {
        this(p_messageKey, null, p_originalException);
    }

    /**
     * Creates a PersistenceException by specifying a message key, and an array
     * of 1 or more arguments.
     * <p>
     * @param p_messageKey the key that specifies the message to be displayed.
     * @param p_messageArguments the arguments that will be embedded in the
     * message.
     */
    public PersistenceException(String p_messageKey,
        String[] p_messageArguments)
    {
        this(p_messageKey, p_messageArguments, null);
    }

    /**
     * Creates a PersistenceException by specifying a message key, an array
     * of 1 or more arguments, and an originating exception.
     * <p>
     * @param p_messageKey the key that specifies the message to be displayed.
     * @param p_messageArguments the arguments that will be embedded in the
     * message.
     * @param p_originalException the origininating exception.
     */
    public PersistenceException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }


    /**
     * Constructs an instance using the given Exception object. The
     * primary use of this constructor is to wrap the other exception
     * in GeneralException.
     *
     * @param p_originalException Original exception that caused the error
     */
    public PersistenceException(Exception p_originalException)
    {
        super(p_originalException);
    }
}

















