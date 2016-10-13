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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;


public class AddWorkflowHandler extends PageHandler 
{

    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);

        dataForTable(request, session);
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Get list of WorkflowTemplateInfos for displaying them in a table
     */
    private void dataForTable(HttpServletRequest request,
                              HttpSession session)
        throws EnvoyServletException 
    {
        Locale locale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        try
        {        
            SessionManager sessionMgr =
                    (SessionManager)session.getAttribute(SESSION_MANAGER);
            long jobId = 0;
            if (request.getParameter(JobManagementHandler.JOB_ID) == null) 
            {
                // get it from the session
                jobId = ((Long)sessionMgr.getAttribute(JobManagementHandler.JOB_ID)).
                            longValue();
            }
            else
            {
                jobId = Long.parseLong(request.getParameter(JobManagementHandler.JOB_ID));

                // store it in the session for job details
                //sessionMgr.setAttribute(JobManagementHandler.JOB_ID, new Long(jobId));
                request.setAttribute(JobManagementHandler.JOB_ID, jobId+"");
            }
            Job job = WorkflowHandlerHelper.getJobById(jobId);
            // first validate the state of the existing pages of the job
            WorkflowHandlerHelper.validateStateOfPagesInJob(job);

            List wfInfos = (List)WorkflowHandlerHelper.getWorkflowTemplateInfos(job);
            // here remove DTP workflow Templated, since currently Adding DTP worklfow
            // in a in progress job is not supported.
            List newWfInfos = new ArrayList();
            for(Iterator it = wfInfos.iterator();it.hasNext();)
            {
                wfInfos = new ArrayList(); 
                WorkflowTemplateInfo wfTemplate = (WorkflowTemplateInfo)it.next();
                if (!WorkflowTypeConstants.TYPE_DTP.equals(wfTemplate.getWorkflowType()))
                {
                    newWfInfos.add(wfTemplate);
                }
            }
            wfInfos = newWfInfos;
            
            setTableNavigation(request, session, wfInfos,
                           new WorkflowTemplateInfoComparator(locale),
                           20,   // change this to be configurable!
                           "numPerPage",
                           "numPages", "wfInfoList",
                           "sorting",
                           "reverseSort",
                           "pageNum",
                           "lastPageNum",
                           "listSize");
        }
        catch(Exception e)// Config exception (already has message key...)
        {
            throw new EnvoyServletException(e);
        }
    }
}
