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

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.util.GeneralException;
import java.util.Iterator;

public class DocumentumUserInfoHelper
{

    private static final Logger s_logger = Logger
        .getLogger(DocumentumTest.class);

    DocumentumPersistenceManager mgr = null;
    
    public DocumentumUserInfoHelper()
    {
        try
        {
            mgr = ServerProxy.getDocumentumPersistenceManager();
        } catch (RemoteException e)
        {
            s_logger.error(e.getMessage(), e);
        } catch (GeneralException e)
        {
            s_logger.error(e.getMessage(), e);
        } catch (NamingException e)
        {
            s_logger.error(e.getMessage(), e);
        } 
    }
    
    public DocumentumUserInfo createDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo)
    {
        try
        {
            Collection allUserInfo = getAllDocumentumUserInfos();
            Iterator userIter = allUserInfo.iterator();
            while(userIter.hasNext()) 
            {
                DocumentumUserInfo userInfo = (DocumentumUserInfo)userIter.next();
                if ((userInfo.getDocumentumUserId().equals(p_documentumUserInfo.getDocumentumUserId()))
                        && (userInfo.getDocumentumDocbase().equals(p_documentumUserInfo.getDocumentumDocbase())))
                {
                    if (userInfo.getDocumentumPassword().equals(p_documentumUserInfo.getDocumentumPassword()))
                    {
                    return userInfo;
                    } else
                    {
                        userInfo.setDocumentumPassword(p_documentumUserInfo.getDocumentumPassword());
                        userInfo = modifyDocumentumUserInfo(userInfo);
                        return userInfo;
                    }
                      
                    
                }
            }
            
            return mgr.createDocumentumUserInfo(p_documentumUserInfo);
        } catch (RemoteException e)
        {
            s_logger.error(e.getMessage(), e);
        } catch (DocumentumPersistenceManagerException e)
        {
            s_logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public DocumentumUserInfo modifyDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo)
    {
        try
        {
            return mgr.modifyDocumentumUserInfo(p_documentumUserInfo);
        } catch (RemoteException e)
        {
            s_logger.error(e.getMessage(), e);
        } catch (DocumentumPersistenceManagerException e)
        {
            s_logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public boolean removeDocumentumUserInfo(DocumentumUserInfo p_documentumUserInfo)
    {
        try
        {
            return mgr.removeDocumentumUserInfo(p_documentumUserInfo);
        } catch (RemoteException e)
        {
            s_logger.error(e.getMessage(), e);
        } catch (DocumentumPersistenceManagerException e)
        {
            s_logger.error(e.getMessage(), e);
        }
        return false;
    }
    
    public DocumentumUserInfo findDocumentumUserInfo(String p_id) 
    {
        try {
            return mgr.findDocumentumUserInfo(p_id);
        } catch (RemoteException e) {
            s_logger.error(e.getMessage(), e);
        } catch (DocumentumPersistenceManagerException e) {
            s_logger.error(e.getMessage(), e);
        }
        return null;
    }
    
    public Collection getAllDocumentumUserInfos()
    {
        try
        {
            return mgr.getAllDocumentumUserInfos();
        } catch (RemoteException e)
        {
            s_logger.error(e.getMessage(), e);
        } catch (DocumentumPersistenceManagerException e)
        {
            s_logger.error(e.getMessage(), e);
        }
        return null;
    }
   
    
    
}
