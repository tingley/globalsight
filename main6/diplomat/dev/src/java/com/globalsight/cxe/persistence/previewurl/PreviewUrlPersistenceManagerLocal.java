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
package com.globalsight.cxe.persistence.previewurl;

import java.rmi.RemoteException;
import java.util.Collection;

import com.globalsight.cxe.entity.previewurl.PreviewUrl;
import com.globalsight.cxe.entity.previewurl.PreviewUrlImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implementation of Preview Url persistence Manager,
 */
public class PreviewUrlPersistenceManagerLocal implements
        PreviewUrlPersistenceManager
{
    /**
     * Creates new PreviewUrlPersistenceManagerLocal
     */
    public PreviewUrlPersistenceManagerLocal()
    {
        super();
    }

    /**
     * Delete specified Preview URL from data store.
     * 
     * @param p_previewUrl
     *            Preview URL object to delete
     * @throws RemoteException
     *             Application Server Error
     * @throws PreviewUrlEntityException
     *             Error deleting Preview Url from data store.
     */
    public void deletePreviewUrl(PreviewUrl p_previewUrl)
            throws RemoteException, PreviewUrlEntityException
    {
        try
        {
            HibernateUtil.delete((PreviewUrlImpl) p_previewUrl);
        }
        catch (Exception e)
        {
            throw new PreviewUrlEntityException(e);
        }
    }

    /**
     * Persist a new Preview URL object in data store.
     * 
     * @param p_previewUrl
     *            Preview URL object to persist in data store
     * @throws RemoteException
     *             Application Server Error
     * @throws PreviewUrlEntityException
     *             Error creating Preview Url in data store.
     * @return Newly created Preview URL object
     */
    public PreviewUrl createPreviewUrl(PreviewUrl p_previewUrl)
            throws RemoteException, PreviewUrlEntityException
    {
        try
        {
            HibernateUtil.save((PreviewUrlImpl) p_previewUrl);
            return getPreviewUrl(p_previewUrl.getId());
        }
        catch (Exception e)
        {
            throw new PreviewUrlEntityException(e);
        }
    }

    /**
     * Retrieve a specified Preview URL object with passed id in editable state
     * 
     * @param p_id
     *            Id of preview URL to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws PreviewUrlEntityException
     *             Error retrieving a specific Preview Url.
     * @return Preview URL object with matching id
     */
    public PreviewUrl getPreviewUrl(long p_id) throws RemoteException,
            PreviewUrlEntityException
    {
        try
        {
            return (PreviewUrlImpl) HibernateUtil.get(PreviewUrlImpl.class,
                    p_id);
        }
        catch (Exception e)
        {
            throw new PreviewUrlEntityException(e);
        }
    }

    /**
     * Return all preview URL objects from data store as EDITABLE
     * 
     * @throws RemoteException
     *             Application Server Error
     * @throws PreviewUrlEntityException
     *             Error getting collection of Preview Urls from data store.
     * @return Collection of all preview URL objects in system
     */
    public Collection getAllPreviewUrls() throws RemoteException,
            PreviewUrlEntityException
    {
        try
        {
            String hql = "from PreviewUrlImpl p order by p.name";
            return HibernateUtil.search(hql);
        }
        catch (Exception e)
        {
            throw new PreviewUrlEntityException(e);
        }
    }

    /**
     * Update specified Preview URL in data store.
     * 
     * @param p_previewUrl
     *            Preview URL object to modify
     * @throws RemoteException
     *             Application Server Error
     * @throws PreviewUrlEntityException
     *             Error updating Preview Url in data store.
     * @return Modified Preview URL object
     */
    public PreviewUrl updatePreviewUrl(PreviewUrl p_previewUrl)
            throws RemoteException, PreviewUrlEntityException
    {
        try
        {
            HibernateUtil.update((PreviewUrlImpl) p_previewUrl);
        }
        catch (Exception e)
        {
            throw new PreviewUrlEntityException(e);
        }
        return p_previewUrl;
    }
}
