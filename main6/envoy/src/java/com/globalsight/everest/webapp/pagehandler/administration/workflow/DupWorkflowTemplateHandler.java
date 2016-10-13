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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * DupWorkflowTemplateHandler is the page handler responsible for displaying
 * source & target locales.
 */

public class DupWorkflowTemplateHandler extends PageHandler implements
        WorkflowTemplateConstants
{

    public DupWorkflowTemplateHandler()
    {
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Override Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page desciptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);
        String userName = (String) session
                .getAttribute(WebAppConstants.USER_NAME);

        if (CANCEL_ACTION.equals(p_request.getParameter(ACTION)))
        {
            clearSessionExceptTableInfo(session, KEY);
        }

        List projectInfos = new ArrayList();
        if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
        {
            projectInfos = WorkflowTemplateHandlerHelper
                    .getAllProjectInfos(uiLocale);
        }
        else if (perms.getPermissionFor(Permission.GET_PROJECTS_I_MANAGE))
        {
            projectInfos = WorkflowTemplateHandlerHelper.getProjectInfosByUser(
                    userName, uiLocale);
        }
        else
        {
            User user = UserHandlerHelper.getUser(userName);
            projectInfos = WorkflowTemplateHandlerHelper
                    .getAllProjectInfosForUser(user, uiLocale);
        }
        p_request.setAttribute(PROJECTS, projectInfos);

        getLocales(p_request, session);

        String wfId = p_request.getParameter(WF_TEMPLATE_INFO_ID);
        WorkflowTemplateInfo wti = WorkflowTemplateHandlerHelper
                .getWorkflowTemplateInfoById(Long.parseLong(wfId));
        p_request.setAttribute(CHOSEN_PROJECT,
                Long.valueOf(wti.getProject().getId()));

        session.setAttribute(WF_TEMPLATE_INFO_ID, wfId);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Put the source, target, and locale pairs in the request for the dup
     * workflow page
     */
    private void getLocales(HttpServletRequest p_request, HttpSession p_session)
            throws ServletException, IOException, EnvoyServletException

    {
        List vPairs = null;
        try
        {
            HttpSession session = p_request.getSession(false);
            Locale uiLocale = (Locale) session
                    .getAttribute(WebAppConstants.UILOCALE);
            vPairs = WorkflowTemplateHandlerHelper.getAllLocalePairs(uiLocale);
            p_request.setAttribute(ALL_LOCALES, vPairs);
            vPairs = WorkflowTemplateHandlerHelper
                    .getAllSourceLocales(uiLocale);
            p_request.setAttribute(SOURCE_LOCALES, vPairs);
            vPairs = WorkflowTemplateHandlerHelper
                    .getAllTargetLocales(uiLocale);
            p_request.setAttribute(TARGET_LOCALES, vPairs);
        }
        catch (Exception e)
        {
            System.out.println("ERROR in getting locales");
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Local Methods
    // ////////////////////////////////////////////////////////////////////

}
