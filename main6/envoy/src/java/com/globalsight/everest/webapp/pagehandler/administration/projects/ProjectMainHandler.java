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
package com.globalsight.everest.webapp.pagehandler.administration.projects;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.TaskInterimPersistenceAccessor;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.importer.IImportManager;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.progress.ProcessStatus;

public class ProjectMainHandler extends PageHandler
{
    public static final String PROJECT_KEY = "project";
    public static final String PROJECT_LIST = "projects";
    private static int NUM_PER_PAGE = 10;

    private static final Logger s_logger = 
    	Logger.getLogger("ProjectMainHandler");
    
    /**
     * Invokes this PageHandler
     *
     * @param pageDescriptor the page desciptor
     * @param request the original request sent from the browser
     * @param response the original response object
     * @param context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
        HttpServletRequest request, HttpServletResponse response,
        ServletContext context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");

        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);

        ProcessStatus status =
            (ProcessStatus)sessionMgr.getAttribute(TM_TM_STATUS);

        if (status != null)
        {
            IImportManager importer =
                (IImportManager)sessionMgr.getAttribute(TM_IMPORTER);

            importer.detachListener(status);

            sessionMgr.removeElement(TM_IMPORTER);
            sessionMgr.removeElement(TM_IMPORT_OPTIONS);
            sessionMgr.removeElement(TM_TM_STATUS);
        }

        if ("save".equals(action))
        {
            String modifierId = (String)session.getAttribute(USER_NAME);
            // from edit basic info page
            saveProject(request, sessionMgr, modifierId);
            sessionMgr.clear();
        }
        else if ("create".equals(action))
        {
            if (FormUtil.isNotDuplicateSubmisson(request,
                    FormUtil.Forms.NEW_PROJECT))
            {
                createProject(request, session);
                sessionMgr.clear();
            }
        }
        else if ("cancel".equals(action))
        {
            sessionMgr.clear();
        }
        else if ("remove".equals(action))
        {
        	try {
        		String ids = (String) request.getParameter(RADIO_BUTTON);
        		if (ids == null
						|| request.getMethod().equalsIgnoreCase(
								REQUEST_METHOD_GET)) 
				{
					response
							.sendRedirect("/globalsight/ControlServlet?activityName=projects");
					return;
				}
            	long longProjectId = -1;
                String[] idarr=ids.trim().split(" ");
            	for(String projectId:idarr){
	                if("on".equals(projectId))continue;
	            	try {
	            		longProjectId = (new Integer(projectId)).longValue();
	            	} catch (Exception e) {
	            		s_logger.error("Wrong project id : " + projectId);
	            	}
            	
	        		String error_msg = checkProjectDependency(longProjectId);
	        		if ( error_msg != null && !"".equals(error_msg.trim()) ) {
	        			throw new Exception(error_msg);
	        		}
	        		//delete user from "project_user" table and set project is_active = 'N'
	        		else {
	        			doDeleteProject(longProjectId);
	        		}
                 }
        		sessionMgr.clear();
        	} catch (Exception e) {
        		sessionMgr.setAttribute(WebAppConstants.PROJECT_ERROR, e.getMessage());		
        	}
        }
        handleFilters(request, sessionMgr, action);
        dataForTable(request, session);

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Save a project
     */
    private void saveProject(HttpServletRequest p_request,
        SessionManager p_sessionMgr, String p_modifierId)
        throws EnvoyServletException
    {
        Project project = (Project)p_sessionMgr.getAttribute("project");
        if(project == null) return;
        String prePM = project.getProjectManagerId();
        PermissionSet perms = (PermissionSet)p_request.getSession(false).getAttribute(WebAppConstants.PERMISSIONS);
        boolean updatePm = perms.getPermissionFor(Permission.GET_ALL_PROJECTS);
        ProjectHandlerHelper.setData(project, p_request, updatePm);
        ProjectHandlerHelper.modifyProject(project, p_modifierId);
        String newPM = project.getProjectManagerId();
        // for gbs-1302, activity dashboard
        if (!prePM.equals(newPM))
        {
            // Refresh the activities in TASK_INTERIM table for the new and old
            // PMs.
            final String[] userIds =
            { prePM, newPM };
            Runnable runnable = new Runnable()
            {
                public void run()
                {
                    TaskInterimPersistenceAccessor.refreshActivities(userIds);
                }
            };
            Thread t = new MultiCompanySupportedThread(runnable);
            t.start();
        }
    }


    /**
     * Create a project
     */
    private void createProject(HttpServletRequest p_request,
        HttpSession p_session)
        throws EnvoyServletException
    {
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        Project project = (Project)sessionMgr.getAttribute("project");

        ProjectHandlerHelper.extractUsers(project, p_request, sessionMgr);

        ProjectHandlerHelper.addProject(project);
    }

    /**
     * Get list of projects and info for displaying them in a table
     */
    private void dataForTable(HttpServletRequest p_request,
        HttpSession p_session)
        throws EnvoyServletException
    {
        Locale locale = (Locale)p_session.getAttribute(UILOCALE);
        SessionManager sessionMgr = (SessionManager) p_session.getAttribute(SESSION_MANAGER);
        try
        {
            String pNameFilter = (String) sessionMgr.getAttribute("pNameFilter");
            String pCompanyFilter = (String) sessionMgr.getAttribute("cNameFilter");
            String condition="";
            if(StringUtils.isNotBlank(pNameFilter)){
                condition+=" and " +"p.name LIKE '%" + pNameFilter.trim() + "%'";
            }
            if(StringUtils.isNotBlank(pCompanyFilter)){
                condition+=" and " +"c.name LIKE '%" + pCompanyFilter.trim() + "%'";
            }
            List allProjects = (List)ServerProxy.getProjectHandler().getAllProjectInfosForGUIbyCondition(condition);
            String numOfPerPage = p_request.getParameter("numOfPageSize");
            if (StringUtils.isNotEmpty(numOfPerPage))
            {
                try
                {
                    NUM_PER_PAGE = Integer.parseInt(numOfPerPage);
                }
                catch (Exception e)
                {
                    NUM_PER_PAGE = Integer.MAX_VALUE;
                }
            }
            setTableNavigation(p_request, p_session, allProjects,
                new ProjectComparator(locale),
                NUM_PER_PAGE,   // change this to be configurable!
                PROJECT_LIST, PROJECT_KEY);
        }
        catch (Exception e)
        {
            // Config exception (already has message key...)
            throw new EnvoyServletException(e);
        }
    }

    private void setData2(Project p_project, HttpServletRequest p_request)
        throws EnvoyServletException
    {
        // Set users
    }
    
    private String checkProjectDependency(long projectId)
    {
    	//check if this project is referenced by wf.
    	List associatedWF = new ArrayList();
    	Collection allWfTemplates = null;    		
    	try {
    		allWfTemplates = ServerProxy.getProjectHandler().getAllWorkflowTemplateInfos();
    	} catch (Exception e) {
    		
    	}
    	Iterator wfIterator = null;
    	if (allWfTemplates != null)
    	{
    		wfIterator = allWfTemplates.iterator();
    	}
    	while (wfIterator.hasNext())
    	{
    		WorkflowTemplateInfo wf = (WorkflowTemplateInfo) wfIterator.next();
    		long project_id = wf.getProject().getId();
    		if (projectId == project_id )  {
    			associatedWF.add(wf);
        	}
    	}
    		
    	//check if this project is referenced by l10n profile
    	List assocaitedL18nProfiles = new ArrayList();

    	Collection p_projects = new ArrayList();
    	
    	try {
    		Project projectDel = ServerProxy.getProjectHandler().getProjectById(projectId);
    		p_projects.add(projectDel);
    		Collection l10nProfiles = ServerProxy.getProjectHandler().getL10nProfiles(p_projects);
    		if (l10nProfiles != null ) {
    			Iterator it = l10nProfiles.iterator();
    			while (it.hasNext()) {
    				L10nProfile locProfile = (L10nProfile) it.next();
    				if ( locProfile.getProjectId() == projectId ) {
    					assocaitedL18nProfiles.add(locProfile);
    				}
    			}
    		}
    	} catch (Exception e) {}
    		
    	//set error message
    	StringBuffer error_msg_bf = new StringBuffer();
    	if ( (associatedWF != null && associatedWF.size() > 0) || 
    				(assocaitedL18nProfiles != null && assocaitedL18nProfiles.size() > 0))
    	{
    		error_msg_bf.append("<span class=\"errorMsg\">");
    		error_msg_bf.append("Project dependencies found.The project you are trying to remove" +
    				" is part of the GlobalSight objects listed below. You must resolve these dependencies" +
    				" before removing the project.");
    		if ( associatedWF != null && associatedWF.size() > 0) {
    			error_msg_bf.append("<p>***WorkFlows***<br>");
    			for (int i=0; i<associatedWF.size(); i++){
    				WorkflowTemplateInfo wf2 = (WorkflowTemplateInfo) associatedWF.get(i);
    				error_msg_bf.append(wf2.getName()+"<br>");
    			}
    			error_msg_bf.append("<br>");
     		}
    		
    		if (assocaitedL18nProfiles != null && assocaitedL18nProfiles.size() > 0) {
    			error_msg_bf.append("***Localization Profiles***<br>");
    			for (int j=0; j<assocaitedL18nProfiles.size(); j++) {
    				L10nProfile l10nProfile = (L10nProfile) assocaitedL18nProfiles.get(j);
    				error_msg_bf.append(l10nProfile.getName() + "<br>");
    			}
    		}
    		
    		error_msg_bf.append("</span>");
    	}

    	return error_msg_bf.toString();
    }
    
	private void doDeleteProject(long projectId)
	{
		try {
			Project project = ServerProxy.getProjectHandler().getProjectById(projectId);
			if (project != null) {
				project.deactivate();
				//change the in-active project name to an unique value
				long time = (new Date()).getTime();
				String changedProjectName = project.getName() + "_" + time;
				if ( changedProjectName.length() >= 40 ) {
					changedProjectName = changedProjectName.substring(0,39);
				}
				project.setName(changedProjectName);
				HibernateUtil.saveOrUpdate(project);

				//not delete users info from 'project_user' table
			}
		}
		catch (Exception e)
		{
			s_logger.error("Fail to delete project.", e );
		}
	}
	
	 private void handleFilters(HttpServletRequest p_request, SessionManager sessionMgr, String action)
	    {
	     
	        String pNameFilter = (String) p_request.getParameter("pNameFilter");
	        String cNameFilter = (String) p_request.getParameter("cNameFilter");
	        if (p_request.getMethod().equalsIgnoreCase(WebAppConstants.REQUEST_METHOD_GET))
	        {
	             pNameFilter = (String) sessionMgr.getAttribute("pNameFilter");
	             cNameFilter = (String) sessionMgr.getAttribute("cNameFilter");
	        }
	        sessionMgr.setAttribute("pNameFilter", pNameFilter);
	        sessionMgr.setAttribute("cNameFilter", cNameFilter);
	    }

    
}
