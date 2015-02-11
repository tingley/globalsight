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
package com.globalsight.everest.page.pageexport;

import org.apache.log4j.Logger;


// globalsight imports
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.pageexport.ExportEventObserverWLRemote;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;

// java imports
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import java.rmi.RemoteException;
                                                                                 
/**
 * Note that all of the methods of this class throw the following exceptions:
 * 1. PageExportException - For component related errors.
 * 2. RemoteException - For network related exception.
 */
public class ExportEventObserverWLRMIImpl extends RemoteServer 
    implements ExportEventObserverWLRemote
{    
    // for logging purposes
    private static final Logger c_logger =
        Logger.getLogger(
            ExportEventObserverWLRMIImpl.class.getName());

    // passes all calls off to the local instance (serves as a proxy)
    private ExportEventObserverLocal m_localInstance = null;
    
    /**
     * Construct a remote Export Event Observer.
     * @exception java.rmi.RemoteException Network related exception.
     */
    public ExportEventObserverWLRMIImpl() throws RemoteException, ExportEventObserverException
    {
        super(ExportEventObserver.SERVICE_NAME); 
        m_localInstance = new ExportEventObserverLocal();        
    }
   
    /**
     * See interface
     */
     public long notifyBeginExportSourceBatch(Job p_job, User p_user,
        List p_pageIds, Long p_taskId)
        throws RemoteException, ExportEventObserverException
     {
        return m_localInstance.notifyBeginExportSourceBatch(
            p_job, p_user, p_pageIds, p_taskId);         
     }
    
    /**
     * See interface
     */ 
    public long notifyBeginExportSourcePage(Job p_job, User p_user,
        long p_pageId, Long p_taskId)
        throws RemoteException, ExportEventObserverException
    {
        return m_localInstance.notifyBeginExportSourcePage(
            p_job, p_user, p_pageId, p_taskId);
    }
    
    /**
     * See interface
     */
    public long notifyBeginExportTargetBatch(Job p_job, User p_user,
        List p_pageIds, List p_wfIds, Long p_taskId, String p_exportType)
        throws RemoteException, ExportEventObserverException
    {
        return m_localInstance.notifyBeginExportTargetBatch(
            p_job, p_user, p_pageIds, p_wfIds, p_taskId, p_exportType);
    }
       
    /**
     * See interface
     */
    public long notifyBeginExportTargetPage(Job p_job, User p_user,
        long p_pageId, Long p_wfId, Long p_taskId, String p_exportType)
        throws RemoteException, ExportEventObserverException
    {
        return m_localInstance.notifyBeginExportTargetPage(
            p_job, p_user, p_pageId, p_wfId, p_taskId, p_exportType);
    }

    /**
     * See interface
     */
    public void notifyPageExportComplete(long p_exportId, String p_pageId, 
        HttpServletRequest p_request)
        throws RemoteException, ExportEventObserverException 
    {
        m_localInstance.notifyPageExportComplete(p_exportId, p_pageId, p_request);
    }
     
    /** 
     * See interface
     */
    public void cancelExportBatchEvent(long p_exportId)
        throws RemoteException, ExportEventObserverException 
     {
          m_localInstance.cancelExportBatchEvent(p_exportId);
     }

    /**
     * @see ExportEventObserver.getExportBatchEventById(long, boolean)
     */
    public ExportBatchEvent getExportBatchEventById(long p_ebeId, 
                                                    boolean p_editable)
        throws RemoteException, ExportEventObserverException
    {
        return m_localInstance.getExportBatchEventById(p_ebeId, p_editable);
    }

    /**
     * @see ExportEventObserver.removeExportBatchEvents(long)
     */
    public void removeExportBatchEvents(long p_jobId)
        throws RemoteException, ExportEventObserverException
    {
        m_localInstance.removeExportBatchEvents(p_jobId);
    }
}
