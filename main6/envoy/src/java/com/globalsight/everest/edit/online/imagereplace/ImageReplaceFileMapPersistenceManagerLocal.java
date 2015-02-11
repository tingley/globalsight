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

package com.globalsight.everest.edit.online.imagereplace;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;

import org.hibernate.Session;

import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Implementation of Image file map perrsistence Manager,
 */
public class ImageReplaceFileMapPersistenceManagerLocal implements
        ImageReplaceFileMapPersistenceManager
{
    /**
     * Delete specified image file map from data store.
     * 
     * @param p_imageFileMap
     *            Image file map object to delete
     * @throws RemoteException
     *             Application Server Error
     * @throws ImageReplaceFileMapEntityException
     *             Error deleting iamge file map from data store.
     */
    public void deleteImageReplaceFileMap(ImageReplaceFileMap p_imageFileMap)
            throws RemoteException, ImageReplaceFileMapEntityException
    {
        try
        {
            HibernateUtil.delete(p_imageFileMap);
        }
        catch (Exception e)
        {
            throw new ImageReplaceFileMapEntityException(e);
        }
    }

    /**
     * Persist a new image file map object in data store.
     * 
     * @param p_imageFileMap
     *            Image file map object to persist in data store
     * @throws RemoteException
     *             Application Server Error
     * @throws ImageReplaceFileMapEntityException
     *             Error creating image file map in data store.
     */
    public void createImageReplaceFileMap(ImageReplaceFileMap p_imageFileMap)
            throws RemoteException, ImageReplaceFileMapEntityException
    {
        try
        {
            HibernateUtil.save(p_imageFileMap);
        }
        catch (Exception e)
        {
            throw new ImageReplaceFileMapEntityException(e);
        }
    }

    /**
     * Update specified image file map in data store.
     * 
     * @param p_imageFileMap
     *            image file map object to modify
     * @throws RemoteException
     *             Application Server Error
     * @throws ImageReplaceFileMapEntityException
     *             Error updating image file map in data store.
     */
    public void updateImageReplaceFileMap(ImageReplaceFileMap p_imageFileMap)
            throws RemoteException, ImageReplaceFileMapEntityException
    {
        try
        {
            HibernateUtil.saveOrUpdate(p_imageFileMap);
        }
        catch (Exception e)
        {
            throw new ImageReplaceFileMapEntityException(e);
        }
    }

    /**
     * Retrieve a specified image file map object with passed id
     * 
     * @param p_id
     *            image file map object id
     * @throws RemoteException
     *             Application Server Error
     * @throws ImageReplaceFileMapEntityException
     *             Error retrieving a specific image file map.
     * @return image file map object with matching id
     */
    public ImageReplaceFileMap getImageReplaceFileMap(long p_id)
            throws RemoteException, ImageReplaceFileMapEntityException
    {

        try
        {
            return (ImageReplaceFileMap) HibernateUtil.get(
                    ImageReplaceFileMap.class, p_id);
        }
        catch (Exception e)
        {
            throw new ImageReplaceFileMapEntityException(e);
        }
    }

    /**
     * Retrieve a specified image file map object with passed target page id,
     * TUV id and sub id.
     * 
     * @param p_targetPageId
     *            target page id of matching image file map to retreive
     * @param p_tuvId
     *            TUV id of matching image file map to retreive
     * @param p_subId
     *            sub id of matching image file map to retreive
     * @throws RemoteException
     *             Application Server Error
     * @throws ImageReplaceFileMapEntityException
     *             Error retrieving a specific image file map.
     * @return image file map object with matching passed ids.
     */
    public ImageReplaceFileMap getImageReplaceFileMap(Long p_targetPageId,
            long p_tuvId, long p_subId) throws RemoteException,
            ImageReplaceFileMapEntityException
    {
        String hql = "from ImageReplaceFileMap this "
                + " where this.targetPageId = :targetPageId "
                + " and this.tuvId = :tuvId " + "and this.subId = :subId ";

        Session session = HibernateUtil.getSession();

        try
        {
            List result = session.createQuery(hql).setLong("targetPageId",
                    p_targetPageId.longValue()).setLong("tuvId", p_tuvId)
                    .setLong("subId", p_subId).list();

            if (result == null || result.size() == 0)
            {
                return null;
            }

            return (ImageReplaceFileMap) result.get(0);
        }
        catch (Exception e)
        {
            throw new ImageReplaceFileMapEntityException(e);
        }
        finally
        {
            //session.close();
        }
    }

    /**
     * Return all image replace file maps for a specific target page. All maps
     * are editable clones because they will be cached by OnlineEditorManager.
     * 
     * @param p_targetPageId
     *            id of target page which might have images replaced
     * @throws RemoteException
     *             Application Server Error
     * @throws ImageReplaceFileMapEntityException
     *             Error getting collection of image replace file maps.
     * @return Collection of image replace file maps for a specified target page
     */
    public Collection getImageReplaceFileMapsForTargetPage(Long p_targetPageId)
            throws RemoteException, ImageReplaceFileMapEntityException
    {
        String hql = "from ImageReplaceFileMap this "
                + "where this.targetPageId = :targetPageId";

        Session session = HibernateUtil.getSession();

        try
        {
            return session.createQuery(hql).setLong("targetPageId",
                    p_targetPageId.longValue()).list();
        }
        catch (Exception e)
        {
            throw new ImageReplaceFileMapEntityException(e);
        }
        finally
        {
            //session.close();
        }
    }
}
