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

package com.globalsight.everest.page;

import java.util.Collection;

import java.rmi.RemoteException;

/**
 * This class represents an observer of events that affect Page.
 * The callers notify the observer of an event that could
 * have an affect on the state of the page.
 */
public interface PageEventObserver
{
    /**
     * The name bound to the remote object.
     */
    public static final String SERVICE_NAME = "PageEventObserverServer";

    /**
     * Notification that the workflow of source and target
     * pages has been dispatched.
     * @param p_sourcePages source pages.
     * @param p_targetPages target pages.
     * @throws PageException when an error occurs.
     */
    public void notifyWorkflowDispatchEvent(Collection p_sourcePages,
        Collection p_targetPages)
        throws PageException, RemoteException;

    /**
     * Notification that the workflow's last task of target pages has
     * completed.
     * @param p_targetPages target pages.
     * @throws PageException when an error occurs.
     */
    public void notifyLastTaskCompleteEvent(Collection p_targetPages)
        throws PageException, RemoteException;

    /**
     * Notification that the all workflows last tasks of source pages
     * has completed.
     * @param p_sourcePages source pages.
     * @throws PageException when an error occurs.
     */
    public void notifyLastTaskCompleteAllWorkflowsEvent(Collection p_sourcePages)
        throws PageException, RemoteException;

    /**
     * Notification that the workflow's task of target pages has
     * completed.
     * @param p_targetPages target pages.
     * @param p_taskId task identifier that completed.
     * @throws PageException when an error occurs.
     */
    public void notifyTaskCompleteEvent(Collection p_targetPages, long taskId)
        throws PageException, RemoteException;

    /**
     * Notification that the page exported successfully.
     * @param p_targetPage target page that exported successfully.
     * @throws PageException when an error occurs.
     */
    public void notifyExportSuccessEvent(TargetPage p_targetPage)
        throws PageException, RemoteException;

    /**
     * Notification that the page in the export process.
     * @param p_targetPage target page that is being exported.
     * @throws PageException when an error occurs.
     */
    public void notifyExportInProgressEvent(TargetPage p_targetPage)
        throws PageException, RemoteException;

    /**
     * Notification that the page export failed.
     * @param p_targetPage the target page that export failed.
     * @param p_exceptionXml GeneralException XML for why the export failed
     * @throws PageException when an error occurs.
     */
    public void notifyExportFailEvent(TargetPage p_targetPage,String p_exceptionXml)
        throws PageException, RemoteException;
    
    /**
     * Notification that the page export failed.
     * @param p_targetPage the target page that export failed.
     * @param p_exceptionXml GeneralException XML for why the export failed
     * @throws PageException when an error occurs.
     */
    public void notifyExportFailEvent(TargetPage p_targetPage,String p_exceptionXml, boolean sendEmail)
        throws PageException, RemoteException;

    /**
     * Notification that the page export got cancelled.
     * @param p_targetPage - The target page of the export cancelled action.
     * @throws PageException when an error occurs.
     */
    public void notifyExportFailCancelEvent(TargetPage p_targetPage)
        throws PageException, RemoteException;

    
    /**
     * Notification that the job (all target pages and workflows have
     * been exported successfully) so now update the state of the
     * source pages to be EXPORTED as well.
     * @param p_sourcePages source pages that
     * all target pages exported successfully.
     * @throws PageException when an error occurs.
     */
    public void notifyAllSourcePagesExportedEvent(Collection p_sourcePages)
        throws PageException, RemoteException;

    
    public void notifyImportSuccessNewTargetPagesEvent(Collection p_targetPages)
        throws PageException, RemoteException;

    public void notifyImportFailureNewTargetPagesEvent(Collection p_targetPages)
        throws PageException, RemoteException;
    /**
     * Notification that the page imported successfully.
     * @param p_sourcePage source page that imported successfully.
     * @param p_targetPages target pages that imported successfully.
     * @throws PageException when an error occurs.
     */
    public void notifyImportSuccessEvent(SourcePage p_sourcePage,
        Collection p_targetPages)
        throws PageException, RemoteException;

    /**
     * Notification that the page import failed by changing the page
     * state, deleting LeverageGroups, Tus, and Tuvs of the page.
     * Finally delete the target pages (if there are any).
     * @param p_sourcePage source page that import failed.
     * @param p_targetPages target pages that import failed.
     * Can be empty.
     * @throws PageException when an error occurs.
     */
    public void notifyImportFailEvent(SourcePage p_sourcePage,
        Collection p_targetPages)
        throws PageException, RemoteException;

    /**
     * Notification that the page import for the various target pages
     * has failed.  Change the state to IMPORT_FAIL
     *
     * @param p_targetPages A collection of TargetPage that failed to
     *                      import properly.                      
     */
    public void notifyImportFailEvent(Collection p_targetPages)
        throws PageException, RemoteException;

    /**
     * Completes the export for update when notified about the source
     * page's successful export.
     *
     * @param p_sourcePage the source page
     * @exception PageException
     * @exception RemoteException
     */
    public void notifyExportForUpdateSuccessEvent(SourcePage p_sourcePage)
        throws PageException, RemoteException;

    /**
     * Handles notifying the PM that an export for update has failed.
     * @param p_sourcePage the source page
     * @exception PageException
     * @exception RemoteException
     */
    public void notifyExportForUpdateFailEvent(SourcePage p_sourcePage)
        throws PageException, RemoteException;
}
