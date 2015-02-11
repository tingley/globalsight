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
package com.globalsight.everest.webapp.pagehandler.administration.workflow;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.WorkflowTemplateInfoComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateControlFlowHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.List;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ImpWorkflowTemplateHandler is the page handler responsible for
 * displaying source & target locales.
 */

public class ImpWorkflowTemplateHandler extends PageHandler
    implements WorkflowTemplateConstants
{
    
    public ImpWorkflowTemplateHandler()
    {
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Override Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
                                  HttpServletRequest p_request,
                                  HttpServletResponse p_response,
                                  ServletContext p_context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);

        if (CANCEL_ACTION.equals(p_request.getParameter(ACTION))) 
        {
            clearSessionExceptTableInfo(session, KEY);
        }

        getLocales(p_request, session);
        getProjects(p_request);
        session.setAttribute(
            WF_TEMPLATE_INFO_ID, p_request.getParameter(WF_TEMPLATE_INFO_ID));

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    private void getProjects(HttpServletRequest p_request)
	{
        List allProjects = (List)ProjectHandlerHelper.getAllProjectsForGUI();
        p_request.setAttribute("projects", allProjects);
	}

	//////////////////////////////////////////////////////////////////////
    //  End: Override Methods
    //////////////////////////////////////////////////////////////////////

    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Local Methods
    //////////////////////////////////////////////////////////////////////


    /**
     * Put the source, target, and locale pairs in the request
     * for the dup workflow page
     */
    private void getLocales(HttpServletRequest p_request,
                                           HttpSession p_session)
        throws ServletException, IOException, EnvoyServletException

    {
        List vPairs = null;
        try {
            HttpSession session = p_request.getSession(false);
            Locale uiLocale =
                (Locale)session.getAttribute(WebAppConstants.UILOCALE);
            vPairs = WorkflowTemplateHandlerHelper.getAllLocalePairs(uiLocale);
            p_request.setAttribute(ALL_LOCALES, vPairs);
            vPairs = WorkflowTemplateHandlerHelper.getAllSourceLocales(uiLocale);
            p_request.setAttribute(SOURCE_LOCALES, vPairs);
            vPairs = WorkflowTemplateHandlerHelper.getAllTargetLocales(uiLocale);
            p_request.setAttribute(TARGET_LOCALES, vPairs);
        } catch (Exception e)
        {
            System.out.println("ERROR in getting locales");
        }
    }


    //////////////////////////////////////////////////////////////////////
    //  End: Local Methods
    //////////////////////////////////////////////////////////////////////

}
