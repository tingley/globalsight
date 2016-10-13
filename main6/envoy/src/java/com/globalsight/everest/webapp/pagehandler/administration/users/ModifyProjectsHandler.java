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
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.securitymgr.UserSecureFields;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

/**
 * Page handler for adding/removing projects to a user
 */
public class ModifyProjectsHandler extends PageHandler
{
    public ModifyProjectsHandler()
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

        String action = (String) request.getParameter(USER_ACTION);
        setUpEdit(request, session);
        request.setAttribute("fromUserEdit", "1");

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Set up edit of projects
     */
    private void setUpEdit(HttpServletRequest request, HttpSession session)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                .getAttribute(MODIFY_USER_WRAPPER);

        // Save data from previous page unless coming from table navigation
        if (request.getParameter("firstName") != null)
            UserUtil.extractUserData(request, wrapper, false);

        // Set edit flag
        sessionMgr.setAttribute("editUser", "true");

        FieldSecurity securitiesHash = (FieldSecurity) sessionMgr
                .getAttribute("securitiesHash");
        String access = (String) securitiesHash.get(UserSecureFields.PROJECTS);
        if ("hidden".equals(access))
        {
            return;
        }

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
        }
        else
        {
            addedProjects = (ArrayList) UserHandlerHelper
                    .getProjectsByUser(wrapper.getUserId());
        }

        String userName = (String) session
                .getAttribute(WebAppConstants.USER_NAME);
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

        List<Project> projects = null;
        if (!isSuperPM)
        {
            projects = UserHandlerHelper.getProjectsManagedByUser(user);
            String companyId = null;
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
            projects = (List<Project>) ProjectHandlerHelper.getAllProjects();
        }

        ArrayList availableProjects = new ArrayList(projects);

        List defaultProjects = UserHandlerHelper.setProjectsForEdit(
                availableProjects, addedProjects, wrapper.isInAllProjects(),
                request, session);

        if ("locked".equals(access))
        {
            defaultProjects.addAll(addedProjects);
        }

        setTableNavigation(request, session, defaultProjects, null, 10, // change
                                                                        // this
                                                                        // to be
                                                                        // configurable!
                "projects", "project");

        request.setAttribute("future", new Boolean(wrapper.isInAllProjects()));
    }
}
