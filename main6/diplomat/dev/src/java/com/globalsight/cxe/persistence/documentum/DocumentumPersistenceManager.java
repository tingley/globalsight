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

import java.rmi.RemoteException;
import java.util.Collection;

/** A service interface for performing CRUD operations for DocumentumUserInfo **/
public interface DocumentumPersistenceManager {
    
    public final static String SERVICE_NAME = "DocumentumPersistenceManager";
    
    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: Documentum UserInfos   ////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     ** Creates a new DocumentumUserInfo object in the data store
     ** @return the newly created object
     **/
    public DocumentumUserInfo createDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo)
    throws RemoteException, DocumentumPersistenceManagerException;
    
    /**
     ** Reads the DocumentumUserInfo object from the datastore
     ** @return FileProfile with the given id
     **/
    public DocumentumUserInfo findDocumentumUserInfo(String p_id)
    throws RemoteException, DocumentumPersistenceManagerException;
    
    /**
     ** Update the DocumentumUserInfo object in the datastore
     ** @return the newly updated object
     **/
    public DocumentumUserInfo modifyDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo)
    throws RemoteException, DocumentumPersistenceManagerException;
    
    /**
     ** Deletes a DocumentumUserInfo object from the datastore
     **/
    public boolean removeDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo)
    throws RemoteException, DocumentumPersistenceManagerException;
    
    /**
     ** Get a list of all existing DoucmentumUserInfo objects in the datastore
     ** @return a vector of the DocumentumUserInfo objects
     **/
    public Collection getAllDocumentumUserInfos()
    throws RemoteException, DocumentumPersistenceManagerException;
}
