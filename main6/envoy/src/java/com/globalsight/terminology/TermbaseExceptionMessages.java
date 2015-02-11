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

package com.globalsight.terminology;

/**
 * See ../resources/messages/TermbaseException.properties
 */
public interface TermbaseExceptionMessages
{
    public final static String MSG_INTERNAL_ERROR   = "internal_error";
    public final static String MSG_NOT_IMPLEMENTED  = "not_implemented";
    public final static String MSG_SQL_ERROR        = "sql_error";
    public final static String MSG_XML_ERROR        = "xml_error";

    // 1 arg: arg that was invalid
    public final static String MSG_INVALID_ARG      = "invalid_arg";

    // 1 arg: termbase name
    public final static String MSG_TB_IS_LOCKED = "tb_locked";
    // 1 arg: termbase name
    public final static String MSG_TB_IS_NOT_LOCKED = "tb_not_locked";
    // 1 arg: termbase name
    public final static String MSG_TB_IS_USED   = "tb_used";

    // 1 arg: termbase name
    public final static String MSG_TB_ALREADY_EXISTS = "tbAlreadyExists";
    // 1 arg: termbase name
    public final static String MSG_TB_DOES_NOT_EXIST = "tbDoesNotExist";

    // 1 arg: termbase name
    public final static String MSG_FAILED_TO_CREATE_TB = "createTb";
    // 1 arg: termbase name
    public final static String MSG_FAILED_TO_UPDATE_TB = "updateTb";
    // 1 arg: termbase name
    public final static String MSG_FAILED_TO_DELETE_TB = "deleteTb";
    // 2 args: termbase name, new termbase name
    public final static String MSG_FAILED_TO_RENAME_TB = "renameTb";

    // 1 arg: informative message
    public final static String MSG_INVALID_DEFINITION = "invalid_definition";
    // 1 arg: informative message
    public final static String MSG_INVALID_ENTRY = "invalid_entry";
    // 1 arg: informative message
    public final static String MSG_INVALID_LOCK = "invalid_lock";

    public final static String MSG_YOU_DONT_OWN_LOCK = "you_dont_own_lock";
    public final static String MSG_ENTRY_NOT_LOCKED = "entry_not_locked";
    public final static String MSG_ENTRY_DOES_NOT_EXIST =
        "entry_does_not_exist";

    // 1 arg: informative message
    public final static String MSG_INVALID_EXPORT_OPTIONS =
        "invalid_export_options";

    // 1 arg: informative message
    public final static String MSG_INVALID_IMPORT_OPTIONS =
        "invalid_import_options";

    // 1 arg: informative message
    public final static String MSG_INVALID_IMPORT_FILE =
        "invalid_import_file";

    // 1 arg: termbase name
    public final static String MSG_INDEXING_IN_PROGRESS =
        "indexing_in_progress";
}
