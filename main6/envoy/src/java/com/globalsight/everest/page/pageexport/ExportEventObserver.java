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

//globalsight
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.pageexport.PageExportException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.util.GlobalSightLocale;

// java 
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import java.rmi.RemoteException;


/**
 * This class represents an observer of events that occur for Export.
 * The callers notify the observer of users who need notification
 * of completed exports.
 */
public interface ExportEventObserver
{

    public static final String SERVICE_NAME = "ExportEventObserverServer";    
    
    /**
     * Notifies the observer that a source page export has begun and returns a 
     * unique export ID to used to track the export request for notifications.
     * <p>
     * @param p_job - the job
     * @param p_user - the user who initiated export
     * @param p_pageIds - pages to be exported
     * @param p_taskId - the taskid
     * @param p_exportType - the type of export (see ExportBatchEvent constants)
     * can be one of INTERIM_EXPORT, FINAL_EXPORT, EXPORT_SOURCE or CREATE_STF
     * @exception ExportEventObserverException - An error occurred in the component
     *           RemoteException - a network exception occurred
     */
     public long notifyBeginExportSourceBatch(Job p_job, User p_user,
        List p_pageIds, Long p_taskId)
        throws RemoteException, ExportEventObserverException;
    
    /**
     * Notifies the observer that a source page export has begun and returns a unique
     * export ID to used to track the export request for notifications.
     * <p>
     * @param p_job - the job
     * @param p_user - the user who initiated export
     * @param p_pageId - page to be exported
     * @param p_taskId - the taskid
     * @exception ExportEventObserverException - An error occurred in the component
     *           RemoteException - a network exception occurred
     */
    public long notifyBeginExportSourcePage(Job p_job, User p_user,
        long p_pageId, Long p_taskId)
        throws RemoteException, ExportEventObserverException;
    
    /**
     * Notifies the observer that a target page export has begun and returns a 
     * unique export ID to used to track the export request for notifications.
     * <p>
     * @param p_job - the job
     * @param p_user - the user who initiated export
     * @param p_pageIds - pages to be exported
     * @param p_wfIds - worklflows for which pages are being exported
     * @param p_taskId - the taskid
     * @param p_exportType - the type of export (see ExportBatchEvent constants)
     * can be one of INTERIM_EXPORT, FINAL_EXPORT or CREATE_STF
     * @exception ExportEventObserverException - An error occurred in the component
     *           RemoteException - a network exception occurred
     */
    public long notifyBeginExportTargetBatch(Job p_job, User p_user,
        List p_pageIds, List p_wfIds, Long p_taskId, String p_exportType)
        throws RemoteException, ExportEventObserverException;
    
    /**
     * Notifies the observer that a target page export has begun and returns a unique
     * export ID to used to track the export request for notifications.
     * <p>
     * @param p_jobName - the job
     * @param p_user - the user who initiated export
     * @param p_pageId - page to be exported
     * @param p_wfId - the worklflow for which the page is being exported
     * @param p_taskId - the taskid
     * @param p_exportType - the type of export (see ExportBatchEvent constants)
     * can be one of INTERIM_PRIMARY, FINAL_PRIMARY, INTRIM_SECONDARY,
     * FINAL_SECONDARY or CREATE_STF.
     * @exception ExportEventObserverException - An error occurred in the component
     *           RemoteException - a network exception occurred
     */
    public long notifyBeginExportTargetPage(Job p_job, User p_user,
        long p_pageId, Long p_wfId, Long p_taskId, String p_exportType)
        throws RemoteException, ExportEventObserverException;
    
    /**
     * Notifies the observer that a given page has compeleted export.
     * <p>
     * @param p_exportId - the export ID returned from notifyBeginExport()
     * @param p_pageId - the page id
     * @param p_request - HttpServletRequest from CXE
     * @exception ExportEventObserverException - An error occurred in the component
     *           RemoteException - a network exception occurred
     */
    public void notifyPageExportComplete(long p_exportId, String p_pageId, 
        HttpServletRequest p_request)
        throws RemoteException, ExportEventObserverException; 
    
    /**
     * Cancels tracking of this export request.
     * <p>
     * @param p_exportId - the export ID returned from notifyBeginExport()
     * @exception ExportEventObserverException - An error occurred in the component
     *           RemoteException - a network exception occurred
     */
    public void cancelExportBatchEvent(long p_exportId)
        throws RemoteException, ExportEventObserverException; 

    /**
     * Get an export batch event by the given id.
     * 
     * @param p_ebeId - The export batch event id.
     * @param p_editable - Determines whether the requested object shoud
     * be editable.
     *
     * @exception ExportEventObserverException - An error occurred in the component
     * @exception RemoteException - a network exception occurred
     */
    ExportBatchEvent getExportBatchEventById(long p_ebeId, 
                                             boolean p_editable)
    throws RemoteException, ExportEventObserverException;

    /**
     * Removes any existing export batch events for the given job id.
     * <p>
     * @param p_jobId - the id of the job used as a key for deleting the export
     * batch events.
     * @exception ExportEventObserverException - An error occurred in the component
     * @exception RemoteException - a network exception occurred
     */
    void removeExportBatchEvents(long p_jobId)
        throws RemoteException, ExportEventObserverException;
    
}