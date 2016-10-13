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

package com.globalsight.everest.comment;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

/**
 * Exception class for comment reference files and their persistence manager.
 */
public class CommentException
    extends GeneralException
{
    public final static String PROPERTY_FILE_NAME = "CommentException";

    public static final String MSG_FAILED_TO_UPLOAD_FILE = "failedToUpload";
    // Args: 1 - file name
    public static final String MSG_FAILED_TO_DELETE_FILE = "failedToDelete";

    // Args: 1 - object type comment associated with (Job, Workflow, Task)
    //       2 - object id
    public static final String MSG_FAILED_TO_SAVE_COMMENT = "failedToSaveComment";

    // Args: 1 - job id
    public static final String MSG_FAILED_TO_GET_TASK_COMMENTS = "failedToGetTaskComments";
    // Args: 1 - object type comment associated with (Job, Workflow, Task)
    //       2 - object id
    //       3 - id of user adding the issue
    public static final String MSG_FAILED_TO_ADD_ISSUE = "failedToAddIssue";
    // Args: 1 - issue id
    //       2 - id of user editing the issue
    public static final String MSG_FAILED_TO_EDIT_ISSUE = "failedToEditIssue";
    // Args: 1 - issue id
    public static final String MSG_FAILED_TO_GET_ISSUE = "failedToGetIssue";
    // Args: 1 - object type
    //       2 - logical key
    public static final String MSG_FAILED_TO_GET_ISSUES = "failedToGetIssues";
    // Args: 1 - object type
    //       2 - logical key
    //       3 - string of status
    public static final String MSG_FAILED_TO_GET_ISSUE_COUNT = "failedToGetIssueCount";
    // Args: 1 - object type
    //       2 - comma delimited string of ids
    public static final String MSG_FAILED_TO_DELETE_ISSUES = "failedToDeleteIssues";



    /**
     * Constructor that creates a exception with specified message
     * string.
     *
     * @param p_msg Exception message
     */
    public CommentException(String p_msg)
    {
        super(COMP_PERSISTENCE, EX_COMMENT_REFERENCE, p_msg);
    }

    /**
     * Constructor that wraps an existing exception with
     * CommentException.
     *
     * @param p_exception Original exception object.
     */
    public CommentException(Exception p_exception)
    {
        super(COMP_PERSISTENCE, EX_COMMENT_REFERENCE, p_exception);
    }

    /**
     * Constructs an instance of CommentException using the new
     * message scheme <p>
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. Can be
     * null.
     * @param p_originalException Original exception that caused the
     * error. Can be null.
     */
    public CommentException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException);
    }
}
