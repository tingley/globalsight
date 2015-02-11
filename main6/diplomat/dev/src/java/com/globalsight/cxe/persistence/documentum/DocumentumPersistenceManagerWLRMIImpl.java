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

import com.globalsight.everest.util.system.RemoteServer;

public class DocumentumPersistenceManagerWLRMIImpl 
extends RemoteServer
implements DocumentumPersistenceManagerWLRemote
{

    private DocumentumPersistenceManagerLocal m_localReference = null;
    
    public DocumentumPersistenceManagerWLRMIImpl() throws RemoteException {
        super(DocumentumPersistenceManager.SERVICE_NAME);
        m_localReference = new DocumentumPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    //////////////////////////////////////////////////////////////////////////////
    //  BEGIN: Documentum UserInfo  //////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////
    
    /**
     * Creates a new DocumentumUserInfo object in the data store
     * @return the newly created object
     */
    public DocumentumUserInfo createDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo) throws RemoteException, DocumentumPersistenceManagerException {
        return m_localReference.createDocumentumUserInfo(p_documentumUserInfo);
    }

    /**
     * Reads the DocumentumUserInfo object from the datastore
     * @return DocumentumUserInfo object
     */
    public DocumentumUserInfo findDocumentumUserInfo(String p_id) throws RemoteException, DocumentumPersistenceManagerException {
        return m_localReference.findDocumentumUserInfo(p_id);
    }

    /**
     * Update the DocumentumUserInfo object in the datastore
     * @return the updated DocumentumUserInfo object
     */
    public DocumentumUserInfo modifyDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo) throws RemoteException, DocumentumPersistenceManagerException {
        return m_localReference.modifyDocumentumUserInfo(p_documentumUserInfo);
    }

    /**
     * Deletes an DocumentumUserInfo from the datastore
     */
    public boolean removeDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo) throws RemoteException, DocumentumPersistenceManagerException {
        return m_localReference.removeDocumentumUserInfo(p_documentumUserInfo);
    }

    /**
     * Get a list of all existing DocumentumUserInfo objects in the datastore; make
     * them not editable.
     *
     * @return a vector of the DocumentumUserInfo objects
     */
    public Collection getAllDocumentumUserInfos() throws RemoteException, DocumentumPersistenceManagerException {
        return m_localReference.getAllDocumentumUserInfos();
    }


}
