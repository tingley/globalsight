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

package com.globalsight.cxe.persistence.fileprofile;

import com.globalsight.util.GeneralException;
import com.globalsight.util.GeneralExceptionConstants;

public class FileProfileEntityException
    extends GeneralException
{
    // public statics for error messages

    public static final String PROP_FILEPROFILE_MESSAGES =
        "FileProfileEntityException";

    public static final String PROP_FILEEXTENSION_MESSAGES =
        "FileExtensionEntityException";

    // Arg 1: file profile name
    public static final String MSG_FILE_PROFILE_ALREADY_EXISTS =
        "fileProfileAlreadyExists";

    // Arg 1: file extension name
    public static final String MSG_FILE_EXTENSION_ALREADY_EXISTS =
        "fileExtensionAlreadyExists";

    // Arg 1: file extension name
    public static final String MSG_FILE_EXTENSION_TOO_LONG =
        "fileExtensionTooLong";

    // Arg 1: file extension name
    public static final String MSG_FILE_EXTENSION_INVALID =
        "fileExtensionInvalid";

    public FileProfileEntityException(String p_msg)
    {
        super(COMP_PERSISTENCE, EX_SQL, p_msg);
    }

    public FileProfileEntityException(Exception p_ex)
    {
        super(COMP_PERSISTENCE, EX_SQL, p_ex);
    }

    /**
     * Normal constructor for FileProfileEntityException used by the
     * FileProfilePersistenceManager.
     */
    public FileProfileEntityException(String p_key, String[] p_args,
        Exception p_exception)
    {
        super(p_key, p_args, p_exception, PROP_FILEPROFILE_MESSAGES);
    }

    /**
     * Creates an FileExtensionEntityException in custom cases.  The
     * property file must exist in com.globalsight.resources.messages
     */
    public FileProfileEntityException(String p_key, String[] p_args,
        Exception p_exception, String p_propertyFile)
    {
        super(p_key, p_args, p_exception, p_propertyFile);
    }
}

