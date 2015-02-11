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

import com.globalsight.everest.util.system.RemoteServer;

public class PreviewUrlPersistenceManagerWLRMIImpl 
    extends RemoteServer
    implements PreviewUrlPersistenceManagerWLRemote
{
    PreviewUrlPersistenceManager m_localReference;

    public PreviewUrlPersistenceManagerWLRMIImpl() 
        throws java.rmi.RemoteException, PreviewUrlEntityException
    {
        super(PreviewUrlPersistenceManager.SERVICE_NAME);
        m_localReference = new PreviewUrlPersistenceManagerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public  com.globalsight.cxe.entity.previewurl.PreviewUrl createPreviewUrl(com.globalsight.cxe.entity.previewurl.PreviewUrl param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.previewurl.PreviewUrlEntityException
    {
        return m_localReference.createPreviewUrl(param1);
    }

    public  void deletePreviewUrl(com.globalsight.cxe.entity.previewurl.PreviewUrl param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.previewurl.PreviewUrlEntityException
    {
        m_localReference.deletePreviewUrl(param1);
    }

    public  com.globalsight.cxe.entity.previewurl.PreviewUrl updatePreviewUrl(com.globalsight.cxe.entity.previewurl.PreviewUrl param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.previewurl.PreviewUrlEntityException
    {
        return m_localReference.updatePreviewUrl(param1);
    }

    public  com.globalsight.cxe.entity.previewurl.PreviewUrl getPreviewUrl(long param1) throws java.rmi.RemoteException,com.globalsight.cxe.persistence.previewurl.PreviewUrlEntityException
    {
        return m_localReference.getPreviewUrl(param1);
    }

    public  java.util.Collection getAllPreviewUrls() throws java.rmi.RemoteException,com.globalsight.cxe.persistence.previewurl.PreviewUrlEntityException
    {
        return m_localReference.getAllPreviewUrls();
    }
}
