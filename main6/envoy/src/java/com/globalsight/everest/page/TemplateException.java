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
package com.globalsight.everest.page;

// globalsight
import com.globalsight.util.GeneralException;


/**
 * An exception thrown during the process of updating and
 * processing PageTemplates.  Thrown by TemplateManager.
 */
public class TemplateException extends GeneralException
{
    /**
     * Template related messages are stored in the following property file
     */
    final static String PROPERTY_FILE_NAME = "TemplateException";


    // Args: 1 - action being attempted as a string (i.e. addSnippet, deleteSnippet)
    final static String INVALID_PARAM = "InvalidParamForAction";
    // Args: 1 - Position
    //       2 - Locale
    final static String INVALID_POSITION = "InvalidPosition";
    // Args: 1 - Snippet name
    //       2 - Snippet id
    //       3 - Locale
    //       4 - Position
    final static String FAILED_TO_ADD_SNIPPET = "FailedToAddSnippet";
    // Args: 1 - Locale
    //       2 - Position
    final static String FAILED_TO_DELETE_SNIPPET = "FailedToDeleteSnippet";
    // Args: 1 - Locale
    //       2 - Position
    final static String FAILED_TO_DELETE_CONTENT = "FailedToDeleteContent";
    // Args: 1 - Locale
    //       2 - Position
    final static String FAILED_TO_UNDELETE_CONTENT = "FailedToUnDeleteContent";

    
    final static String FAILED_TO_PERSIST = "PersistenceError";


    // query exceptions

    // Args: 1 - the position
    //       2 - the locale
    final static String FAILED_TO_FIND_POSITION = "FailedToFindPosition";
    // Args: 1 - page id
    final static String FAILED_TO_GET_PAGE = "FailedToGetSourcePage";
    // Args: 1 - snippet name
    //       2 - locale
    //       3 = id
    final static String FAILED_TO_GET_SNIPPET = "FailedToGetSnippet";
    // Args: 1 - locale
    final static String FAILED_TO_GET_LOCALE = "FailedToGetLocale";
    // Args: 1 - source page id
    //       2 - template type
    final static String FAILED_TO_GET_TEMPLATE_PARTS = "FailedToGetTemplateParts";



    /**
     * @see GeneralException#GeneralException(String, String[], Exception, String)
     *
     * @param p_messageKey key in properties file
     * @param p_messageArguments Arguments to the message. It can be null.
     * @param p_originalException Original exception that caused the error. 
     *        It can be null.
     */
    public TemplateException(String p_messageKey,
                            String[] p_messageArguments,
                            Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException, 
              PROPERTY_FILE_NAME);
    }
}
