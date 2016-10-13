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

import com.globalsight.everest.util.system.RemoteServer;

public class ImageReplaceFileMapPersistenceManagerWLRMIImpl 
    extends RemoteServer
    implements ImageReplaceFileMapPersistenceManagerWLRemote
{
    ImageReplaceFileMapPersistenceManager m_localReference;

    public ImageReplaceFileMapPersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, ImageReplaceFileMapEntityException
    {
        super(ImageReplaceFileMapPersistenceManager.SERVICE_NAME);
        m_localReference = new ImageReplaceFileMapPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  void createImageReplaceFileMap(com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap param1) throws java.rmi.RemoteException,com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapEntityException
    {
        m_localReference.createImageReplaceFileMap(param1);
    }

    public  void deleteImageReplaceFileMap(com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap param1) throws java.rmi.RemoteException,com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapEntityException
    {
        m_localReference.deleteImageReplaceFileMap(param1);
    }

    public  void updateImageReplaceFileMap(com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap param1) throws java.rmi.RemoteException,com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapEntityException
    {
        m_localReference.updateImageReplaceFileMap(param1);
    }

    public  com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap getImageReplaceFileMap(Long param1,
            long param2,
            long param3) throws java.rmi.RemoteException,com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapEntityException
    {
        return m_localReference.getImageReplaceFileMap(param1,
            param2,
            param3);
    }

    public  com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMap getImageReplaceFileMap(long param1) throws java.rmi.RemoteException,com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapEntityException
    {
        return m_localReference.getImageReplaceFileMap(param1);
    }

    public  java.util.Collection getImageReplaceFileMapsForTargetPage(Long param1) throws java.rmi.RemoteException,com.globalsight.everest.edit.online.imagereplace.ImageReplaceFileMapEntityException
    {
        return m_localReference.getImageReplaceFileMapsForTargetPage(param1);
    }
}
