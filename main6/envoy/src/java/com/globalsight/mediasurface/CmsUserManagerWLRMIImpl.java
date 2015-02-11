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
package com.globalsight.mediasurface;

import com.globalsight.everest.util.system.RemoteServer;

import java.rmi.RemoteException;


public class CmsUserManagerWLRMIImpl
    extends RemoteServer
    implements CmsUserManagerWLRemote
{
    private CmsUserManager m_localReference = null;

    public CmsUserManagerWLRMIImpl()
        throws RemoteException
    {
        super(CmsUserManager.SERVICE_NAME);

        m_localReference = new CmsUserManagerLocal();
    }

    
    /**
     * @see CmsUserManager.createCmsUserInfo(CmsUserInfo);
     */
    public CmsUserInfo createCmsUserInfo(CmsUserInfo p_cmsUserInfo)
        throws RemoteException, CmsUserManagerException
    {
        return m_localReference.createCmsUserInfo(p_cmsUserInfo);
    }

    /**
     * @see CmsUserManager.findCmsUserInfo(String);
     */    
    public CmsUserInfo findCmsUserInfo(String p_ambassadorUserId)
        throws RemoteException, CmsUserManagerException
    {
        return m_localReference.findCmsUserInfo(p_ambassadorUserId);
    }

    /**
     * @see CmsUserManager.modifyCmsUserInfo(CmsUserInfo);
     */    
    public CmsUserInfo modifyCmsUserInfo(CmsUserInfo p_cmsUserInfo)
        throws RemoteException, CmsUserManagerException
    {
        return m_localReference.modifyCmsUserInfo(p_cmsUserInfo);
    }

    /**
     * @see CmsUserManager.removeCmsUserInfo(CmsUserInfo);
     */    
    public boolean removeCmsUserInfo(CmsUserInfo p_cmsUserInfo)
        throws RemoteException, CmsUserManagerException
    {
        return m_localReference.removeCmsUserInfo(p_cmsUserInfo);
    }
}
