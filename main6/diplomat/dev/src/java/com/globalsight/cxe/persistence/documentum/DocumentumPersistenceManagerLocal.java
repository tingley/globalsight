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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.crypto.Crypto;

/**
 * Implements the service interface for performing CRUD operations for
 * DocumentumUserInfos.
 */
public class DocumentumPersistenceManagerLocal implements
        DocumentumPersistenceManager
{
    private static final Logger s_logger = Logger
            .getLogger(DocumentumPersistenceManagerLocal.class);

    /**
     * Creates a new DocumentumUserInfo object in the data store
     * 
     * @return the newly created object
     */
    public DocumentumUserInfo createDocumentumUserInfo(
            DocumentumUserInfo p_documentumUserInfo) throws RemoteException,
            DocumentumPersistenceManagerException
    {
        try
        {
            String pwd = Crypto.encrypt(p_documentumUserInfo
                    .getDocumentumPassword());
            p_documentumUserInfo.setDocumentumPassword(pwd);
            HibernateUtil.save(p_documentumUserInfo);
        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            String[] args =
            { p_documentumUserInfo.getDocumentumUserId() };
            throw new DocumentumPersistenceManagerException(
                    DocumentumPersistenceManagerException.MSG_CREATE_DOCUMENTUM_USER_INFO_FAILED,
                    args, e);

        }

        return p_documentumUserInfo;
    }

    /**
     * Reads the DocumentumUserInfo object from the datastore
     * 
     * @return DocumentumUserInfo object
     */
    public DocumentumUserInfo findDocumentumUserInfo(String p_id)
            throws RemoteException, DocumentumPersistenceManagerException
    {
        return documentumUserInfo(Long.parseLong(p_id));
    }

    /**
     * Update the DocumentumUserInfo object in the datastore
     * 
     * @return the updated DocumentumUserInfo object
     */
    public DocumentumUserInfo modifyDocumentumUserInfo(
            DocumentumUserInfo p_documentumUserInfo) throws RemoteException,
            DocumentumPersistenceManagerException
    {
        long userId = p_documentumUserInfo.getId();

        try
        {

            DocumentumUserInfo clone = documentumUserInfo(userId);
            clone.setDocumentumUserId(p_documentumUserInfo
                    .getDocumentumUserId());
            clone.setDocumentumDocbase(p_documentumUserInfo
                    .getDocumentumDocbase());

            // encrypt the password.....
            String pwd = Crypto.encrypt(p_documentumUserInfo
                    .getDocumentumPassword());
            clone.setDocumentumPassword(pwd);
            HibernateUtil.update(clone);

        }
        catch (Exception e)
        {
            s_logger.error(e.getMessage(), e);
            String[] args =
            { Long.toString(userId) };
            throw new DocumentumPersistenceManagerException(
                    DocumentumPersistenceManagerException.MSG_MODIFY_DOCUMENTUM_USER_INFO_FAILED,
                    args, e);

        }

        return findDocumentumUserInfo(Long.toString(userId));
    }

    /**
     * Deletes an DocumentumUserInfo from the datastore
     */
    public boolean removeDocumentumUserInfo(
            DocumentumUserInfo p_documentumUserInfo) throws RemoteException,
            DocumentumPersistenceManagerException
    {
        try
        {
            HibernateUtil.delete(p_documentumUserInfo);
            return true;
        }
        catch (Exception e)
        {
            s_logger.error("remove documentum user infor failed;", e);
        }

        return false;
    }

    /**
     * Get a list of all existing DocumentumUserInfo objects in the datastore;
     * make them not editable.
     * 
     * @return a vector of the DocumentumUserInfo objects
     */
    public Collection getAllDocumentumUserInfos() throws RemoteException,
            DocumentumPersistenceManagerException
    {
        try
        {
            ArrayList result = new ArrayList();
            Collection c = HibernateUtil
                    .search("from DocumentumUserInfo", null);
            Iterator it = c.iterator();
            while (it.hasNext())
            {
                DocumentumUserInfo userInfo = (DocumentumUserInfo) it.next();
                // decrypt the password first....
                String pwd = Crypto.decrypt(userInfo.getDocumentumPassword());
                userInfo.setDocumentumPassword(pwd);
                result.add(userInfo);
            }
            return result;
        }
        catch (Exception e)
        {
            String[] args =
            { " " };
            throw new DocumentumPersistenceManagerException(
                    DocumentumPersistenceManagerException.MSG_FIND_ALL_DOCUMENTUM_USER_INFO_FAILED,
                    args, e);
        }

    }

    /**
     * Gets DocumentumUserInfo object with decripted password by id
     * 
     * @param p_documentumUserId
     * @param p_editable
     * @return DocumentumUserInfo object
     * @throws DocumentumPersistenceManagerException
     */
    private DocumentumUserInfo documentumUserInfo(long p_documentumUserId)
            throws DocumentumPersistenceManagerException
    {
        try
        {
            DocumentumUserInfo userInfo = (DocumentumUserInfo) HibernateUtil
                    .get(DocumentumUserInfo.class, p_documentumUserId);

            if (userInfo != null)
            {
                // decrypt the password first....
                String pwd = Crypto.decrypt(userInfo.getDocumentumPassword());
                userInfo.setDocumentumPassword(pwd);
            }

            return userInfo;
        }
        catch (Exception e)
        {
            String[] args =
            { Long.toString(p_documentumUserId) };
            throw new DocumentumPersistenceManagerException(
                    DocumentumPersistenceManagerException.MSG_FIND_DOCUMENTUM_USER_INFO_FAILED,
                    args, e);
        }
    }

}
