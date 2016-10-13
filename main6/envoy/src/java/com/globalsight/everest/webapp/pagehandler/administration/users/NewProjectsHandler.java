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
package com.globalsight.everest.webapp.pagehandler.administration.users;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.vendors.ProjectComparator;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.SortUtil;

/**
 * Page handler for adding/removing projects to a user
 */

public class NewProjectsHandler extends PageHandler
{

    public NewProjectsHandler()
    {
    }

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
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        CreateUserWrapper wrapper = (CreateUserWrapper) sessionMgr
                .getAttribute(CREATE_USER_WRAPPER);

        String action = (String) request.getParameter("action");
        if ("prev".equals(action))
        {
            // save security info
            FieldSecurity fs = (FieldSecurity) sessionMgr
                    .getAttribute("securitiesHash");
            UserUtil.extractSecurity(fs, request);
        }
        else if ("next".equals(action))
        {
            // save role info
            saveRoles(request, sessionMgr, wrapper);
        }

        String userName = (String) session
                .getAttribute(WebAppConstants.USER_NAME);
        setUpNew(request, sessionMgr, userName, wrapper);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Save data from roles page
     */
    private void saveRoles(HttpServletRequest request,
            SessionManager sessionMgr, CreateUserWrapper wrapper)
            throws EnvoyServletException
    {
        // Extract source and target locale Strings from the request.
        String sourceLocale = UserUtil.extractSourceLocale(request);
        String targetLocale = UserUtil.extractTargetLocale(request);

        // Generate the map of activity-cost pairs from the attributes
        // on the request.
        Hashtable activityCostMap = UserUtil.generateActivityCostMap(request);

        // Use the strings and the generated hashtable to add these last
        // roles to the user.
        wrapper.addUserRoles(sourceLocale, targetLocale, activityCostMap);
        sessionMgr.setAttribute("roleAdded", "true");
    }

    /**
     * Set up edit of projects
     */
    private void setUpNew(HttpServletRequest request,
            SessionManager sessionMgr, String userName,
            CreateUserWrapper wrapper) throws EnvoyServletException
    {
        PermissionSet perms = (PermissionSet) request.getSession()
                .getAttribute(WebAppConstants.PERMISSIONS);

        // get data for the page
        User user = UserHandlerHelper.getUser(userName);

        boolean isSuperPM = false;
        try
        {
            isSuperPM = UserUtil.isSuperPM(user.getUserId());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        List projects = null;
        String companyId = null;
        if (!isSuperPM)
        {
            projects = UserHandlerHelper.getProjectsManagedByUser(user);
            try
            {
                companyId = CompanyWrapper.getCompanyIdByName(wrapper
                        .getCompanyName());
            }
            catch (PersistenceException e)
            {
                throw new EnvoyServletException(e);
            }
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(companyId))
            {
                Project project = null;
                for (Iterator iter = projects.iterator(); iter.hasNext();)
                {
                    project = (Project) iter.next();
                    if (!String.valueOf(project.getCompanyId()).equals(
                            companyId))
                    {
                        iter.remove();
                    }
                }
            }
        }
        else
        {
            projects = new ArrayList(ProjectHandlerHelper.getAllProjects());
        }

        List avilableProjects = new ArrayList(projects);
        SortUtil.sort(avilableProjects,
                new ProjectComparator(Locale.getDefault()));
        request.setAttribute("availableProjects", avilableProjects);
        ArrayList addedProjects = new ArrayList();
        if (sessionMgr.getAttribute("ProjectPageVisited") != null)
        {
            ArrayList addedProjectIds = (ArrayList) wrapper.getProjects();
            // convert list of projectIds to Projects
            for (int i = 0; i < addedProjectIds.size(); i++)
            {
                Long id = (Long) addedProjectIds.get(i);
                addedProjects.add(ProjectHandlerHelper.getProjectById(id
                        .longValue()));
            }
            SortUtil.sort(addedProjects,
                    new ProjectComparator(Locale.getDefault()));
            request.setAttribute("addedProjects", addedProjects);
            request.setAttribute("future",
                    new Boolean(wrapper.isInAllProjects()));
        }
        else
        {
            SortUtil.sort(projects, new ProjectComparator(Locale.getDefault()));
            request.setAttribute("addedProjects", projects);
            if (perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
            {
                request.setAttribute("future", new Boolean(true));
            }
            else
            {
                request.setAttribute("future", new Boolean(false));
            }
        }
    }
}
