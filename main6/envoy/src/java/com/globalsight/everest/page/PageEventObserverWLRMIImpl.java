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

import com.globalsight.everest.util.system.RemoteServer;

import java.util.Collection;

import java.rmi.RemoteException;

/**
 * This class represents the remote implementation of an observer of
 * events that affect Page.
 */
public class PageEventObserverWLRMIImpl
    extends RemoteServer
    implements PageEventObserverWLRemote
{
    // an instance of the PageEventObserverLocal
    private PageEventObserver m_localInstance = null;

    //
    // Constructor
    //

    /**
     * Construct a remote Page event observer.
     *
     * @param p_localInstance The local instance of the page event observer
     * @exception java.rmi.RemoteException Network related exception.
     */
    public PageEventObserverWLRMIImpl() throws RemoteException
    {
        super(PageEventObserver.SERVICE_NAME);
        m_localInstance = new PageEventObserverLocal();
    }

    //
    // PageEventObserver Implementation
    //

    /**
     * @see PageEventObserver#notifyWorkflowDispatchEvent(Collection)
     * <p>
     * Change state to ACTIVE_JOB.
     * The source pages change state with the
     * first workflow dispatched.
     * @param p_sourcePages source pages.
     * @param p_targetPages target pages.
     * @throws PageException when an error occurs.
     */
    public void notifyWorkflowDispatchEvent(Collection p_sourcePages,
        Collection p_targetPages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyWorkflowDispatchEvent(p_sourcePages,
            p_targetPages);
    }

    /**
     * @see PageEventObserver#notifyLastTaskCompleteEvent(Collection)
     * <p>
     * Set state LOCALIZED.
     * @param p_targetPages target pages.
     * @throws PageException when an error occurs.
     */
    public void notifyLastTaskCompleteEvent(Collection p_targetPages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyLastTaskCompleteEvent(p_targetPages);
    }


    /**
     * @see PageEventObserver#notifyLastTaskCompleteAllWorkflowsEvent(Collection)
     * <p>
     * Set state LOCALIZED.
     * @param p_sourcePages source pages.
     * @throws PageException when an error occurs.
     */
    public void notifyLastTaskCompleteAllWorkflowsEvent(Collection p_sourcePages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyLastTaskCompleteAllWorkflowsEvent(p_sourcePages);
    }


    /**
     * @see PageEventObserver#notifyTaskCompleteEvent(Collection, long)
     * <p>
     * Call TuvEventObserver.notifyTaskCompleteEvent().
     * Call indexer ton index pages.
     * @param p_targetPages target pages.
     * @param p_taskId task identifier that completed.
     * @throws PageException when an error occurs.
     */
    public void notifyTaskCompleteEvent(Collection p_targetPages, long p_taskId)
        throws PageException, RemoteException
    {
        m_localInstance.notifyTaskCompleteEvent(p_targetPages, p_taskId);
    }


    /**
     * @see PageEventObserver#notifyExportInProgressEvent(TargetPage)
     * @param p_targetPage target page that is being exported.
     * @throws PageException when an error occurs.
     */
    public void notifyExportInProgressEvent(TargetPage p_targetPage)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportInProgressEvent(p_targetPage);
    }

    /**
     * @see PageEventObserver#notifyExportSuccessEvent(Page)
     * @param p_targetPage target page that exported successfully.
     * <p>
     * Set state EXPORTED.
     * Call TuvEventObserver.notifyPageExportedEvent().
     * @throws PageException when an error occurs.
     */
    public void notifyExportSuccessEvent(TargetPage p_targetPage)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportSuccessEvent(p_targetPage);
    }


    /**
     * Notification that the page export got cancelled.
     * @param p_targetPage - The target page of the export cancelled action.
     * @throws PageException when an error occurs.
     */
    public void notifyExportFailCancelEvent(TargetPage p_targetPage)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportFailCancelEvent(p_targetPage);
    }


    /**
     * @see PageEventObserver#notifyAllSourcePagesExportedEvent(Collection)
     * <p>
     * Set all the source pages state to EXPORTED and their TUVs.
     * @param p_sourcePages source pages that
     * all target pages for all workflows exported successfully.
     * @throws PageException when an error occurs.
     */
    public void notifyAllSourcePagesExportedEvent(Collection p_sourcePages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyAllSourcePagesExportedEvent(p_sourcePages);
    }

    /**
     * @see PageEventObserver#notifyExportFailEvent(Page)
     * Set state EXPORT_FAILED.
     * @param p_targetPage the target page that export failed.
     * @param p_exceptionXml GeneralException XML for why the export failed
     * @throws PageException when an error occurs.
     */
    public void notifyExportFailEvent(TargetPage p_targetPage, String p_exceptionXml)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportFailEvent(p_targetPage,p_exceptionXml);
    }
    

    /**
     * @see PageEventObserver#notifyExportFailEvent(Page)
     * Set state EXPORT_FAILED.
     * @param p_targetPage the target page that export failed.
     * @param p_exceptionXml GeneralException XML for why the export failed
     * @throws PageException when an error occurs.
     */
    public void notifyExportFailEvent(TargetPage p_targetPage, String p_exceptionXml, boolean sendEmail)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportFailEvent(p_targetPage,p_exceptionXml, sendEmail);
    }

    /**
     * @see PageEventObserver#notifyImportSuccessEvent(Page,
     * Collection, Page, Collection)
     * <p>
     * Set state IMPORT_SUCCESS for new pages.
     * Set state OUT_OF_DATE for old pages.
     * @param p_sourcePage source page that imported successfully.
     * @param p_targetPages target pages that imported successfully.
     * @param p_oldSourcePage old source page.  Can be null.
     * @param p_oldTargetPages old target pages.  Can be empty.
     * @throws PageException when an error occurs.
     */
    public void notifyImportSuccessEvent(SourcePage p_sourcePage,
        Collection p_targetPages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyImportSuccessEvent(p_sourcePage,
            p_targetPages);
    }

    /**
     * @see PageEventObserver#notifyImportFailEvent(Page, Collection)
     * <p>
     * Set state IMPORT_FAIL.
     * @param p_sourcePage source page that import failed.
     * @param p_targetPages target pages that import failed.
     * Can be empty.
     * @throws PageException when an error occurs.
     */
    public void notifyImportFailEvent(SourcePage p_sourcePage,
        Collection p_targetPages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyImportFailEvent(p_sourcePage, p_targetPages);
    }

    public void notifyImportSuccessNewTargetPagesEvent(Collection p_targetPages)
       throws PageException, RemoteException
    {
        m_localInstance.notifyImportSuccessNewTargetPagesEvent(p_targetPages);
    }

   public void notifyImportFailureNewTargetPagesEvent(Collection p_targetPages)
       throws PageException, RemoteException
   {
       m_localInstance.notifyImportFailureNewTargetPagesEvent(p_targetPages);
   }

    /**
     * @see PageEventObserver.notifyImportFailEvent(Collection)
     */
    public void notifyImportFailEvent(Collection p_targetPages)
        throws PageException, RemoteException
    {
        m_localInstance.notifyImportFailEvent(p_targetPages);
    }

    /**
     * Completes the export for update when notified about the source
     * page's successful export.
     *
     * @param p_sourcePage the source page
     * @exception PageException
     * @exception RemoteException
     */
    public void notifyExportForUpdateSuccessEvent(SourcePage p_sourcePage)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportForUpdateSuccessEvent(p_sourcePage);
    }

    /**
     * Handles notifying the PM that an export for update has failed.
     * @param p_sourcePage the source page
     * @exception PageException
     * @exception RemoteException
     */
    public void notifyExportForUpdateFailEvent(SourcePage p_sourcePage)
        throws PageException, RemoteException
    {
        m_localInstance.notifyExportForUpdateFailEvent(p_sourcePage);
    }
}
