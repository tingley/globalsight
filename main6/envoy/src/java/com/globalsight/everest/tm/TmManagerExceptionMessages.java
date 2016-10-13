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
package com.globalsight.everest.tm;

/**
 * An exception thrown during the process of using the TmManager api.
 */
public interface TmManagerExceptionMessages
{
    //
    // Error messages
    //
    public final static String MSG_INTERNAL_ERROR = "InternalError";
    public final static String MSG_NOT_IMPLEMENTED = "NotImplemented";

    // 1 arg: Tm Name
    public final static String MSG_TM_IS_LOCKED = "TmIsLocked";

    // 1 arg: Tm Name
    public final static String MSG_TM_IS_USED = "TmIsUsed";

    // 1 arg: Tm Name
    public final static String MSG_TM_DOES_NOT_EXIST = "TmDoesNotExist";

    // 1 arg: Tm Name
    public final static String MSG_TM_DOES_EXIST = "TmDoesExist";

    // This means the TM name is null or the empty string
    public final static String MSG_INVALID_NAME = "InvalidName";

    // 1 arg: the argument name that is invalid
    public final static String MSG_INVALID_ARG = "InvalidArg";

    // 1 arg: TM name
    public final static String MSG_FAILED_TO_CREATE_TM = "FailedToCreateTm";

    // 1 arg: TM name
    public final static String MSG_FAILED_TO_UPDATE_TM = "FailedToUpdateTm";

    // 1 arg: TM name
    public final static String MSG_FAILED_TO_DELETE_TM = "FailedToDeleteTm";

    // Search & Replace
    public final static String MSG_FAILED_TO_SEARCH = "FailedToSearch";

    public final static String MSG_FAILED_TO_UPDATE_JOB_TUV_DATA = "FailedToUpdateJobTuvData";
}
