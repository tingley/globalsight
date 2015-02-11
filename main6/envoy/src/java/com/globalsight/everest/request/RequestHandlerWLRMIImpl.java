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
package com.globalsight.everest.request;

//globalsight
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.util.GeneralException;

/*
 * Remote implementation of RequestHandler.
 */
public class RequestHandlerWLRMIImpl extends RemoteServer implements
        RequestHandlerWLRemote
{
    private RequestHandler m_localReference;

    /*
     * default constructor
     */
    public RequestHandlerWLRMIImpl() throws RemoteException
    {
        super(RequestHandler.SERVICE_NAME);
        m_localReference = new RequestHandlerLocal();
    }

    /**
     * Initialize the server
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        // bind the server
        super.init();

        // clean up any imports that were stopped because the system was
        // shutdown
        try
        {
            cleanupIncompleteRequests();
            // start importing all the requests that were delayed by a timer
            // since the server is restarted the timers don't exist anymore
            // so just restart
            startDelayedImports();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            /*
             * throw new SystemStartupException(
             * SystemStartupException.MSG_FAILED_TO_START_REQUESTHANDLER, null,
             * e);
             */
        }
    }

    /*
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.findRequest(long p_requestId)
     */
    public Request findRequest(long param1) throws RequestHandlerException,
            RemoteException
    {
        return m_localReference.findRequest(param1);
    }

    /*
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.findRequest(long p_requestId)
     */
    public WorkflowRequest findWorkflowRequest(long param1)
            throws RequestHandlerException, RemoteException
    {
        return m_localReference.findWorkflowRequest(param1);
    }

    /**
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.setExceptionInRequest(Request p_request,
     *      GeneralException p_exception)
     */
    public void setExceptionInRequest(Request p_request,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException
    {
        m_localReference.setExceptionInRequest(p_request, p_exception);
    }

    /**
     * @see RequestHandler.setExceptionInRequest(long p_requestId,
     *      GeneralException p_exception)
     */
    public void setExceptionInRequest(long p_requestId,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException
    {
        m_localReference.setExceptionInRequest(p_requestId, p_exception);
    }

    public void setExceptionInWorkflowRequest(WorkflowRequest p_request,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException
    {
        m_localReference.setExceptionInWorkflowRequest(p_request, p_exception);
    }

    public long createWorkflowRequest(WorkflowRequest p_request, Job p_job,
            Collection p_workflowTemplates) throws GeneralException,
            RemoteException
    {
        return m_localReference.createWorkflowRequest(p_request, p_job,
                p_workflowTemplates);
    }

    /*
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.importPage(...)
     */
    public void importPage(Request p_request) throws RequestHandlerException,
            RemoteException
    {
        m_localReference.importPage(p_request);
    }

    /**
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.getDataSourceNameOfRequest(Request)
     */
    public String getDataSourceNameOfRequest(Request p_request)
            throws RequestHandlerException, RemoteException
    {
        return m_localReference.getDataSourceNameOfRequest(p_request);
    }

    /**
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.startDelayedImports()
     */
    public void startDelayedImports() throws RemoteException,
            RequestHandlerException
    {
        m_localReference.startDelayedImports();
    }

    /**
     * Implementation of RequestHandler method
     * 
     * @see RequestHandler.cleanupIncompleteRequests()
     */
    public void cleanupIncompleteRequests() throws RemoteException,
            RequestHandlerException
    {
        m_localReference.cleanupIncompleteRequests();
    }

    /**
     * Used by the service activator MDB. Prepares and submits a request for
     * localization. (This used to be the onMessage() functionality)
     */
    public void prepareAndSubmitRequest(HashMap p_hashmap,
            String p_contentFileName, int p_requestType, String p_eventFlowXml,
            GeneralException p_exception, String p_l10nRequestXml)
            throws RemoteException, RequestHandlerException
    {
        m_localReference.prepareAndSubmitRequest(p_hashmap, p_contentFileName,
                p_requestType, p_eventFlowXml, p_exception, p_l10nRequestXml);
    }

    public FileProfile getFileProfile(Request p_request)
    {
        return m_localReference.getFileProfile(p_request);
    }
}
