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
package com.globalsight.everest.webapp.pagehandler.administration.activity;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;

/**
 * Pagehandler for the new & edit Activity pages.
 */
public class ActivityBasicHandler extends PageHandler
        implements ActivityConstants
{
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");

        try
        {
            if (action.equals(ActivityConstants.CREATE))
            {
                setActivityNames(p_request);
                FormUtil.addSubmitToken(p_request, FormUtil.Forms.NEW_ACTIVITY_TYPE);
            }
            else if (action.equals(ActivityConstants.EDIT))
            {
                SessionManager sessionMgr = (SessionManager) session
                        .getAttribute(SESSION_MANAGER);
                String name = (String) p_request.getParameter("name");
                Activity act = (Activity) ServerProxy.getJobHandler()
                        .getActivity(name);
                sessionMgr.setAttribute(ActivityConstants.ACTIVITY, act);
                p_request.setAttribute("edit", "true");
            }
        }
        catch (NamingException ne)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ne);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, re);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL, ge);
        }
        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }

    /**
     * Get list of all activity names.  Needed in jsp to determine duplicate names.
     */
    private void setActivityNames(HttpServletRequest p_request)
        throws RemoteException, NamingException, GeneralException
    {
        ArrayList list = (ArrayList)ServerProxy.getJobHandler().getAllActivities();
        ArrayList names = new ArrayList();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                Activity act = (Activity)list.get(i);
                names.add(act.getDisplayName());
            }
        }
        p_request.setAttribute(ActivityConstants.NAMES, names);
    }
}


