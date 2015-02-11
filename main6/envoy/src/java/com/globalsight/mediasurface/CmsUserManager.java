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

import java.rmi.RemoteException;

/**
 * The interface for CMS user activities.
 */
public interface CmsUserManager
{
    /**
     * CMS User Manager Service Name
     */
    public static final String SERVICE_NAME =
        "CmsUserManager";

    /**
     * Create a CMS username/password for an existing GlobalSight user.
     * @param p_cmsUser The object wrapping a CMS user info that should be created.
     *
     * @return The newly created CMS user info.
     *
     * @throws RemoteException Network related exception.
     * @throws CmsUserManagerException Component related exception.
     */
    public CmsUserInfo createCmsUserInfo(CmsUserInfo p_cmsUserInfo)
        throws RemoteException, CmsUserManagerException;

    /**
     * Find the CMS user info based on the given GlobalSight username.
     * @param p_ambassadorUserId - The username of the GlobalSight user.
     *
     * @return The CMS user info (if any).
     * 
     * @throws RemoteException Network related exception.
     * @throws CmsUserManagerException Component related exception.
     */    
    public CmsUserInfo findCmsUserInfo(String p_ambassadorUserId)
        throws RemoteException, CmsUserManagerException;

    /**
     * Modify the given CMS user info.
     * @param p_cmsUser The CMS user info that should be modified.
     *
     * @return The modified CMS user info.
     * 
     * @throws RemoteException Network related exception.
     * @throws CmsUserManagerException Component related exception.
     */    
    public CmsUserInfo modifyCmsUserInfo(CmsUserInfo p_cmsUserInfo)
        throws RemoteException, CmsUserManagerException;

    /**
     * Remove the given CMS user info (de-activate).
     * @param p_cmsUser The CMS user info that should be removed.
     *
     * @return True if the process is completed successfully.
     * 
     * @throws RemoteException Network related exception.
     * @throws CmsUserManagerException Component related exception.
     */    
    public boolean removeCmsUserInfo(CmsUserInfo p_cmsUserInfo)
        throws RemoteException, CmsUserManagerException;
}
