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
package com.globalsight.everest.snippet;

import com.globalsight.util.GeneralException;

/**
 * An exception handling object for the Snippet component.
 */
public class SnippetException
    extends GeneralException
{
    // message file name
    private static final String PROPERTY_FILE_NAME = "SnippetException";


    // message keys to look up message from property file

    //--------------modification exceptions----------------------------

    // Args: 1 - Snippet information
    public static final String INVALID_SNIPPET = "FailedToValidateSnippet";
    // Args: 1 - Snippet information
    public static final String INVALID_EXTRACTION_ERROR = "InvalidExtractionError";
    // Args: 1 - Snippet information
    public static final String INVALID_TOO_MANY_ELEMENTS = "InvalidTooManyElements";
    // Args: 1 - Snippet information
    public static final String INVALID_TOO_FEW_ELEMENTS = "InvalidTooFewElements";
    // Args: 1 - Snippet information
    public static final String INVALID_GS_TAG_ADDED = "InvalidGsTagAdded";
    // Args: 1 - Snippet information
    public static final String INVALID_GS_TAG_REMOVED = "InvalidGsTagRemoved";
    // Args: 1 - Snippet information
    //       2 - New element text
    //       3 - New element type (int)
    //       4 - Old element text
    //       5 - Old element type (int)
    public static final String INVALID_DIFFERENT_ELEMENTS = "InvalidDifferentElements";
    // Args: 1 - Snippet information
    //       2 - New element text
    //       3 - Old element text
    public static final String INVALID_DIFFERENT_CONTENT = "InvalidDifferentContent";

    //Args: 1 - Snippet information (toString)
    public static final String ADD_SNIPPET = "FailedToAddSnippet";
    //Args: 1 - Snippet name
    public static final String DUPLICATE_SNIPPET_NAME =
        "FailedToAddDuplicateGenericSnippet";
    // Args: 1 - Snippet information (toString)
    //       2 - Locale display name (the locale cloning to)
    public static final String CLONE_SNIPPET = "FailedToCloneSnippet";
    // Args: 1 - Snippet information (toString)
    public static final String MODIFY_SNIPPET = "FailedToModifySnippet";
    // Args: 1 - Snippet information (toString)
    public static final String REMOVE_SNIPPET = "FailedToRemoveSnippet";
    // Args: 1 = Snippet name
    public static final String REMOVE_SNIPPETS_WITH_NAME =
        "FailedToRemoveSnippetsWithName";

    //-----------retrieval exceptions--------------------------

    public static final String GENERIC_SNIPPET_NAMES =
        "FailedToRetrieveGenericSnippetNames";
    public static final String SNIPPETS_BY_LOCALE =
        "FailedToRetrieveSnippetsByLocale";
    public static final String ALL_SNIPPETS =
        "FailedToRetrieveAllSnippets";
    // Args: 1 - Snippet name
    public static final String SNIPPETS_BY_NAME =
        "FailedToRetrieveSnippetsByName";
    // Args: 1 - Snippet name
    //       2 - Locale display name
    public static final String SNIPPETS_BY_NAME_AND_LOCALE =
        "FailedToRetrieveSnippetsByNameAndLocale";
    // Args: 1 - All the snippet names in a long string
    //       2 - Locale display name
    public static final String SNIPPETS_BY_NAMES_AND_LOCALE =
        "FailedToRetrieveSnippetsByNamesAndLocale";
    // Args: 1 - Snippet name
    //       2 - Snippet locale display name
    //       3 - Id of the  snippet
    public static final String SNIPPET_BY_KEY = "FailedToRetrieveSnippet";
    public static final String INVALID_LOCALE = "InvalidLocale";

    /**
     * Main constructor for creating an exception object.
     *
     * @param p_messageKey: The message key that matches the correct
     * message in the property file.
     * @param p_messageArguments: An array of strings that are the
     * parameters needed for the message.
     * @param p_originalException: The original exception that this
     * one is wrapping or NULL if there wasn't one.
     */
    public SnippetException(String p_messageKey,
        String[] p_messageArguments, Exception p_originalException)
    {
        super(p_messageKey, p_messageArguments, p_originalException,
            PROPERTY_FILE_NAME);
    }

    /**
     * @see GeneralException#GeneralException(Exception)
     * This constructor is used when a subclass of GeneralException is wrapped.
     * In this case the wrapped exception already has the message related
     * information (unless a new message or arguments are needed).
     *
     * @param p_originalException Original exception that caused the error
     */
    public SnippetException(Exception p_originalException)
    {
        super(p_originalException);
    }
}
