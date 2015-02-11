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

/*
 * Copyright (c) 2000 GlobalSight Corporation. All rights reserved.
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

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.util.GeneralException;


/**
 * This interface defines the methods that handle request to create,
 * modify and inquire about localization requests.
 */
public interface RequestHandler
{
    /* 
     * Constants
     */
    /**
     *  Service name - registered 
     */
    public static final String SERVICE_NAME = "RequestHandler";


    /**
     * Find the request by its unique id
     *
     * @param p_pk long The unique id of the request
     * @return The Request identified by the id or null if a request couldn't be found.
     *
     * @exception RequestHandlerException An error occurred when trying to find the request.
     */
    Request findRequest(long p_id) 
        throws RequestHandlerException, RemoteException;

    public WorkflowRequest findWorkflowRequest(long p_id)
        throws RequestHandlerException, RemoteException;
    /**
     * Set the exception within the request and persist.
     *
     * @param p_request The request to set the exception in.
     * @param p_exception The GeneralException or subclass that occurred while
     *					   processing the requst.
     * @exception An error occurred when trying to set the exception.
     * 		      Possibly a database error.
     */
    void setExceptionInRequest(Request p_request, GeneralException p_exception) 
        throws RequestHandlerException, RemoteException;

    /**
     * Set the exception within the request specified by the id.
     * Set the request type to an IMPORT_ERROR.  This persistence
     * is done using JDBC and this method should only be used during
     * importing (before the request is in the TOPLink cache).
     *
     * @param p_requestId  The id of the request that should be marked as failed.
     * @param p_exception  The general exception that explains why the request failed.
     *
     */
    public void setExceptionInRequest(long p_requestId,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException;

    
    public long createWorkflowRequest(WorkflowRequest p_request,
                                      Job p_job,
                                      Collection p_workflowTemplates)
        throws RemoteException, GeneralException;

    public void setExceptionInWorkflowRequest(WorkflowRequest p_request,
            GeneralException p_exception) throws RequestHandlerException,
            RemoteException;

    /**
     * Import the page that is part of the specified request.
     * This is called by the ActivePageReimporter when it is time to
     * start importing.
     *
     * @param p_request The request that contains all the information for
     *                  importing.
     *
     * @exception RequestHandlerException The component internal exception.
     */
    void importPage(Request p_request)
        throws RequestHandlerException, RemoteException;

    /**
     * Return the name of the data source that this request is associated with.
     * The data source name can't be stored in the request (only the id) because
     * the data source can be modified.
     *
     * @param p_request The request to get the type and id from to determine the data source.
     *
     * @exception RequestHandlerException The component internal exception.
     */
    String getDataSourceNameOfRequest(Request p_request)
        throws RequestHandlerException, RemoteException;
    
    /**
     * Start importing all the requests that have been delayed.
     * This is used on start-up.
     * 
     * @exception RequestHandlerException An exception within the component
     *                                    that kept it from starting delayed imports
     */
    void startDelayedImports()
        throws RemoteException, RequestHandlerException;

    /**
     * Cleans up dangling, half imported requests and source pages that
     * were importing when the system was shutdown.
     * This is used on start-up.
     *
     * @exception RequestHandlerException An exception within the component
     *                                    that kept it from cleaning up incomplete imports
     */
    void cleanupIncompleteRequests()
        throws RemoteException, RequestHandlerException;

    /**
     * Used by the service activator MDB. Prepares and submits a request
     * for localization. (This used to be the onMessage() functionality)
     */
    public void prepareAndSubmitRequest(HashMap p_hashmap,
                                        String p_contentFileName,
                                        int p_requestType,
                                        String p_eventFlowXml,
                                        GeneralException p_exception,
                                        String p_l10nRequestXml)
    throws RemoteException, RequestHandlerException;
    
    public FileProfile getFileProfile(Request p_request);
}

