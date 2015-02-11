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
package com.globalsight.cxe.persistence.documentum;

import com.globalsight.util.GeneralException;

public class DocumentumPersistenceManagerException extends GeneralException {
    
    //  public statics for error messages
    
    public final static String PROPERTY_FILE_NAME = 
        "DocumentumPersistenceManagerException";
    
    public final static String MSG_CREATE_DOCUMENTUM_USER_INFO_FAILED = 
        "failedToCreateDocumentumUserInfo";
    
    public final static String MSG_FIND_DOCUMENTUM_USER_INFO_FAILED =
        "failedToFindDocumentumUserInfo";
    
    public final static String MSG_MODIFY_DOCUMENTUM_USER_INFO_FAILED =
        "failedToModifyDocumentumUserInfo";
    
    public final static String MSG_DELETE_DOCUMENTUM_USER_INFO_FAILED =
        "failedToDeleteDocumentumUserInfo";

    public static final String MSG_FIND_ALL_DOCUMENTUM_USER_ID_FAILED = 
        "failedToFindAllDocumentumUserId";
    
    public static final String MSG_FIND_ALL_DOCUMENTUM_USER_INFO_FAILED = 
        "failedToFindAllDocumentumUserInfo";

    public DocumentumPersistenceManagerException(Exception p_originalException) {
        super(p_originalException);
    }
    
    /**
     * Normal constructor for FileProfileEntityException used by the
     * DocumentumPersistenceManager.
     */
    public DocumentumPersistenceManagerException(
            String p_messageKey,
            String[] p_messageArguments,
            Exception p_orinalException) {
        super(
                p_messageKey,
                p_messageArguments,
                p_orinalException,
                PROPERTY_FILE_NAME);
    }

}
