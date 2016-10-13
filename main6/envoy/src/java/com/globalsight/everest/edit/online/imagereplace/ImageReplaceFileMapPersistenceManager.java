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
/*
 * Copyright (c) 2001 GlobalSight Corporation. All rights reserved.
 *
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */

// java
import java.util.Collection;

import java.rmi.RemoteException;

/**
 * Persistence manager for data access of Image replace file mapping objec
 *
 */
public interface ImageReplaceFileMapPersistenceManager
{
    /**
     * Image replace file map persistence service name
     *
     */
    public static final String SERVICE_NAME = "ImageReplaceFileMapPersistenceManager";

    /**
     * Persist a new image file map object in data store.
     *
     * @param p_imageFileMap Image file map object to persist in data store
     * @throws RemoteException Application Server Error
     * @throws ImageReplaceFileMapEntityException Error creating image file map in
     *         data store.
     */
    public void createImageReplaceFileMap(ImageReplaceFileMap p_imageFileMap)
        throws RemoteException, ImageReplaceFileMapEntityException;

    /**
     * Delete specified image file map from data store.
     *
     * @param p_imageFileMap Image file map object to delete
     * @throws RemoteException Application Server Error
     * @throws ImageReplaceFileMapEntityException Error deleting iamge file map from
     *         data store.
     */
    public void deleteImageReplaceFileMap(ImageReplaceFileMap p_imageFileMap)
        throws RemoteException, ImageReplaceFileMapEntityException;

    /**
     * Update specified image file map in data store.
     *
     * @param p_imageFileMap image file map object to modify
     * @throws RemoteException Application Server Error
     * @throws ImageReplaceFileMapEntityException Error updating image file map in
     *         data store.
     */
    public void updateImageReplaceFileMap(ImageReplaceFileMap p_imageFileMap)
        throws RemoteException, ImageReplaceFileMapEntityException;

    /**
     * Retrieve a specified image file map object with passed id
     *
     * @param p_id image file map object id
     * @throws RemoteException Application Server Error
     * @throws ImageReplaceFileMapEntityException Error retrieving a specific
     *         image file map.
     * @return  image file map object with matching id
     */
    public ImageReplaceFileMap getImageReplaceFileMap(long p_id)
        throws RemoteException, ImageReplaceFileMapEntityException;

    /**
     * Retrieve a specified image file map object with passed target page id,
     * TUV id and sub id.
     *
     * @param p_targetPageId target page id of matching image file map to retreive
     * @param p_tuvId TUV id of matching image file map to retreive
     * @param p_subId sub id of matching image file map to retreive
     * @throws RemoteException Application Server Error
     * @throws ImageReplaceFileMapEntityException Error retrieving a specific
     *         image file map.
     * @return  image file map object with matching passed ids.
     */
    public ImageReplaceFileMap getImageReplaceFileMap(Long p_targetPageId,
                                                      long p_tuvId,
                                                      long p_subId)
        throws RemoteException, ImageReplaceFileMapEntityException;

    /**
     * Return all image replace file maps for a specific target page.
     *
     * @param p_targetPageId id of target page which might have images replaced
     * @throws RemoteException Application Server Error
     * @throws ImageReplaceFileMapEntityException Error getting collection of
     *         image replace file maps.
     * @return Collection of image replace file maps for a specified target page
     */
    public Collection getImageReplaceFileMapsForTargetPage(Long p_targetPageId)
        throws RemoteException, ImageReplaceFileMapEntityException;
}
