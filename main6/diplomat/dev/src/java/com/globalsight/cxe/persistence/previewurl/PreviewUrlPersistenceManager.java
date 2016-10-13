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

import com.globalsight.cxe.entity.previewurl.PreviewUrl;

import java.rmi.RemoteException;

/**
 * Persistence manager for data access of Preview URL objects.
 *
 */
public interface PreviewUrlPersistenceManager
{
    /**
     * Preview URL Persistence Service Name
     *
     */
    public static final String SERVICE_NAME = "PreviewUrlPersistenceManager";

    /**
     * Persist a new Preview URL object in data store.
     *
     * @param p_previewUrl Preview URL object to persist in data store
     * @throws RemoteException Application Server Error
     * @throws PreviewUrlEntityException Error creating Preview Url in data
     *         store.
     * @return Newly created Preview URL object
     */
    public PreviewUrl createPreviewUrl(PreviewUrl p_previewUrl)
        throws RemoteException, PreviewUrlEntityException;

    /**
     * Delete specified Preview URL from data store.
     *
     * @param p_previewUrl Preview URL object to delete
     * @throws RemoteException Application Server Error
     * @throws PreviewUrlEntityException Error deleting Preview Url from data
     *         store.
     */
    public void deletePreviewUrl(PreviewUrl p_previewUrl)
        throws RemoteException, PreviewUrlEntityException;

    /**
     * Update specified Preview URL in data store.
     *
     * @param p_previewUrl Preview URL object to modify
     * @throws RemoteException Application Server Error
     * @throws PreviewUrlEntityException Error updating Preview Url in data
     *         store.
     * @return Modified Preview URL object
     */
    public PreviewUrl updatePreviewUrl(PreviewUrl p_previewUrl)
        throws RemoteException, PreviewUrlEntityException;

    /**
     * Retrieve a specified Preview URL object with passed id
     *
     * @param p_id Id of preview URL to retreive
     * @throws RemoteException Application Server Error
     * @throws PreviewUrlEntityException Error retrieving a specific Preview
     *         Url.
     * @return  Preview URL object with matching id
     */
    public PreviewUrl getPreviewUrl(long p_id)
        throws RemoteException, PreviewUrlEntityException;

    /**
     * Return all preview URL objects from data store.
     *
     * @throws RemoteException Application Server Error
     * @throws PreviewUrlEntityException Error getting collection of Preview
     *        Urls from data store.
     * @return Collection of all preview URL objects in system
     */
    public Collection getAllPreviewUrls()
        throws RemoteException, PreviewUrlEntityException;
}
