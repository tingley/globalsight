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

import org.apache.log4j.Logger;

import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.crypto.Crypto;

/**
 * CmsUserManagerLocal provides the main implementation of the CmsUserManager
 * interface.
 */
public class CmsUserManagerLocal implements CmsUserManager
{
    private static final Logger s_logger = Logger
            .getLogger(CmsUserManagerLocal.class);

    //
    // CmsUserManager interface methods
    //

    /**
     * @see CmsUserManager.createCmsUserInfo(CmsUserInfo);
     */
    public CmsUserInfo createCmsUserInfo(CmsUserInfo p_cmsUserInfo)
            throws RemoteException, CmsUserManagerException
    {
        try
        {
            // encrypt the password before persistance....
            String pwd = Crypto.encrypt(p_cmsUserInfo.getCmsPassword());
            p_cmsUserInfo.setCmsPassword(pwd);
            HibernateUtil.save(p_cmsUserInfo);
        }
        catch (Exception e)
        {
            String[] args = { p_cmsUserInfo.getAmbassadorUserId() };
            throw new CmsUserManagerException(
                    CmsUserManagerException.MSG_CREATE_CMS_USER_INFO_FAILED,
                    args, e);
        }

        return findCmsUserInfo(p_cmsUserInfo.getAmbassadorUserId());
    }

    /**
     * @see CmsUserManager.findCmsUserInfo(String);
     */
    public CmsUserInfo findCmsUserInfo(String p_ambassadorUserId)
            throws RemoteException, CmsUserManagerException
    {
        return cmsUserInfo(p_ambassadorUserId, true);
    }

    /**
     * @see CmsUserManager.modifyCmsUserInfo(CmsUserInfo);
     */
    public CmsUserInfo modifyCmsUserInfo(CmsUserInfo p_cmsUserInfo)
            throws RemoteException, CmsUserManagerException
    {
        String ambassadorUserId = p_cmsUserInfo.getAmbassadorUserId();
        try
        {
            CmsUserInfo clone = cmsUserInfo(ambassadorUserId, false);
            clone.setCmsUserId(p_cmsUserInfo.getCmsUserId());
            // encrypt the password.....
            String pwd = Crypto.encrypt(p_cmsUserInfo.getCmsPassword());
            clone.setCmsPassword(pwd);
            HibernateUtil.saveOrUpdate(clone);
        }
        catch (Exception e)
        {
            String[] args = { p_cmsUserInfo.getAmbassadorUserId() };
            throw new CmsUserManagerException(
                    CmsUserManagerException.MSG_MODIFY_CMS_USER_INFO_FAILED,
                    args, e);
        }
        return findCmsUserInfo(ambassadorUserId);
    }

    /**
     * @see CmsUserManager.removeCmsUserInfo(CmsUserInfo);
     */
    public boolean removeCmsUserInfo(CmsUserInfo p_cmsUserInfo)
            throws RemoteException, CmsUserManagerException
    {
        String ambassadorUserId = p_cmsUserInfo.getAmbassadorUserId();
        try
        {
            CmsUserInfo cmsUserInfo = cmsUserInfo(ambassadorUserId, false);
            HibernateUtil.delete(cmsUserInfo);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to remove CMS user info for: "
                    + ambassadorUserId);
        }
        return false;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////
    /*
     * Get the CmsUserInfo object based on the given Ambassador username.
     */
    private CmsUserInfo cmsUserInfo(String p_ambassadorUserId,
            boolean p_editable) throws CmsUserManagerException
    {
        try
        {
            String hql = "from CmsUserInfo c where c.ambassadorUserId = ?";
            CmsUserInfo cmsUserInfo = (CmsUserInfo) HibernateUtil.getFirst(hql,
                    p_ambassadorUserId);

            // decrypt the password first....
            String pwd = Crypto.decrypt(cmsUserInfo.getCmsPassword());
            cmsUserInfo.setCmsPassword(pwd);

            return cmsUserInfo;
        }
        catch (Exception e)
        {
            String[] args = { p_ambassadorUserId };
            throw new CmsUserManagerException(
                    CmsUserManagerException.MSG_FIND_CMS_USER_INFO_FAILED,
                    args, e);
        }
    }
}
