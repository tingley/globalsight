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

package com.globalsight.everest.tuv;

import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.tuv.TuvEventObserver;
import com.globalsight.everest.tuv.TuvEventObserverWLRemote;
import com.globalsight.everest.tuv.TuvEventObserverLocal;
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.util.system.SystemStartupException;
import java.util.Collection;

import java.rmi.RemoteException;


public final class TuvEventObserverWLImpl extends RemoteServer 
        implements TuvEventObserverWLRemote
{
    private TuvEventObserverLocal m_tuvEventObserverLocal;

    public TuvEventObserverWLImpl() throws RemoteException
    {
        super(TuvEventObserver.SERVICE_NAME);
        m_tuvEventObserverLocal = new TuvEventObserverLocal();
    }

    /**
     * Bind the remote server to the ServerRegistry.
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        super.init();
    }

    //
    // TuvEventObserver interface methods
    // 

    /**
     * Notification that the target locale page of Tuvs has been exported.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @param p_jobId
     *               jobId these Tuvs belong to.
     * @throws TuvException
     *             when an error occurs.
     */
    public void notifyPageExportedEvent(Collection p_targetTuvs, long p_jobId)
            throws TuvException, RemoteException
    {
        m_tuvEventObserverLocal.notifyPageExportedEvent(p_targetTuvs, p_jobId);
    }
        
    /**
     * Updates the state of target page TUVs that are LOCALIZED to COMPLETED
     * otherwise leaves the state alone. Should be called after an export
     * for update of a source page.
     * 
     * @param p_targetTuvs
     *               collection of target TUVs
     * @exception TuvException
     * @exception RemoteException
     */
    public void notifyPageExportedForUpdateEvent(Collection p_targetTuvs,
            long p_jobId) throws TuvException, RemoteException
    {
        m_tuvEventObserverLocal.notifyPageExportedForUpdateEvent(p_targetTuvs,
                p_jobId);
    }

    /**
     * Notification that the job of source locale page Tuvs has been exported.
     * 
     * @param p_sourceTuvs
     *            source locale page Tuvs.
     * @param p_companyId
     *               companyId these Tuvs belong to.
     * @throws TuvException
     *             when an error occurs.
     */
    public void notifyJobExportedEvent(Collection p_sourceTuvs, long p_jobId)
            throws TuvException, RemoteException
    {
        m_tuvEventObserverLocal.notifyJobExportedEvent(p_sourceTuvs, p_jobId);
    }

    /**
     * Notification that the workflow task completed for this target locale page
     * of Tuvs.
     * 
     * @param p_targetTuvs
     *            target locale page Tuvs.
     * @param p_taskId
     *            task identifier that completed.
     * @throws TuvException
     *             when an error occurs.
     */
    public void notifyTaskCompleteEvent(Collection p_targetTuvs,
            long p_taskId)
            throws TuvException, RemoteException
    {
        m_tuvEventObserverLocal.notifyTaskCompleteEvent(p_targetTuvs,
                p_taskId);
    }
}
