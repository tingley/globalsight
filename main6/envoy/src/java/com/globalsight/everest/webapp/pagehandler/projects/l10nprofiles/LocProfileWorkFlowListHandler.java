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
package com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfoKey;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * LocProfileWorkFlowListHandler, A page handler to produce the entry
 * page(index.jsp) for DataSources management.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class LocProfileWorkFlowListHandler
    extends PageHandler
    implements LocProfileStateConstants
{
    // Category for log4j logging.
    private static final Logger CATEGORY =
        Logger.getLogger(
            LocProfileWorkFlowListHandler.class.getName());

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
    	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
		{
			p_response
					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
			return;
		}
        dispatchJSP(p_pageDescriptor, p_request, p_response, p_context);
    }

    /**
     * Invoke the correct JSP for this page
     */
    protected void dispatchJSP(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context) 
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        // if user has clicked on cancle, remove left-overs from session manager.
        if (p_request.getParameter(CANCEL) != null)
        {
            removeParameterFromSession(sessionMgr, WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH); 
        }
        // put user input into session
        setData(session, p_request);

        FormUtil.addSubmitToken(p_request, FormUtil.Forms.EDIT_LOCALIZATION_PROFILE);

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request,
                                p_response,p_context);
    }

    private void setData(HttpSession p_session, HttpServletRequest p_request)
        throws EnvoyServletException
    {
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);

        saveUTFParameterToSession(p_request, sessionMgr, LOC_PROFILE_NAME);
        saveUTFParameterToSession(p_request, sessionMgr, LOC_PROFILE_DESCRIPTION);
        saveParameterToSession(p_request, sessionMgr, LOC_PROFILE_SQL_SCRIPT);
        saveParameterToSession(p_request, sessionMgr, LOC_TM_PROFILE_ID);
        saveParameterToSession(p_request, sessionMgr, LOC_PROFILE_PROJECT_ID);
        saveParameterToSession(p_request, sessionMgr, JOB_PRIORITY);
        saveParameterToSession(p_request, sessionMgr, SOURCE_LOCALE_ID);
        saveParameterToSession(p_request, sessionMgr, LOC_PROFILE_TM_USAGE_ID);
        saveParameterToSession(p_request, sessionMgr, AUTOMATIC_DISPATCH);

        // obtain the localization profile to be modified
        BasicL10nProfile p_locprofile = (BasicL10nProfile)sessionMgr.getAttribute("locprofile");
        p_locprofile = HibernateUtil.get(BasicL10nProfile.class, p_locprofile.getId());
        String action = p_request.getParameter("action");

        String pid = (String)sessionMgr.getAttribute(LOC_PROFILE_PROJECT_ID);
        long selectedProjectId = -1;
        try
        {
            selectedProjectId = Long.parseLong(pid);
        }
        catch(NumberFormatException nfe)
        {
        }

        boolean isSameProject = selectedProjectId == p_locprofile.getProjectId();
        sessionMgr.setAttribute(IS_SAME_PROJECT, isSameProject ? 
                                Boolean.TRUE : Boolean.FALSE);

        boolean isSameManager= false;
        Project oldProject = p_locprofile.getProject();

        Project newProject = null;
        try
        {
            newProject =
                ServerProxy.getProjectHandler().getProjectById(selectedProjectId);
        } 
        catch (Exception e)
        {
            CATEGORY.error("Failed to find the project associated with " + selectedProjectId);
        }

        if ((newProject != null && oldProject != null) 
            && (oldProject.getProjectManagerId().equals(newProject.getProjectManagerId())))
        {
            isSameManager = true;
        }
        sessionMgr.setAttribute(IS_SAME_PROJECT_MANAGER, isSameManager ? 
                                Boolean.TRUE : Boolean.FALSE);

        
        Vector displayTargetNames = new Vector();
        Vector targetObjects = new Vector();
        Locale uiLocale = (Locale)p_session.getAttribute(UILOCALE);
        Hashtable wftLocaleHash = (Hashtable)sessionMgr.getAttribute(WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH); 
        // long wfId = -1;
		Vector targetLocales = new Vector();
		Vector workflowNames = new Vector();
		Vector<WorkflowInfos> workflowInfos = new Vector<WorkflowInfos>();
        if(wftLocaleHash != null && ! "remove".equals(action))
        {
            Enumeration wfts = (Enumeration)wftLocaleHash.keys();
            
            if (isSameProject || isSameManager)
            {
                L: while(wfts.hasMoreElements())
                {
                    GlobalSightLocale gl = (GlobalSightLocale)wfts.nextElement();
                    // String id = (String)wftLocaleHash.get(gl);
					List ids = (List) wftLocaleHash.get(gl);
					long wfId = -1;
					WorkflowTemplateInfo wfti = null;
						try {
							wfId = Long.parseLong((String) ids.get(0));
						} catch (Exception nfe) {
						
						}
						L10nProfileWFTemplateInfo l10nProfileWFTemplateInfo = null;
						try {
							l10nProfileWFTemplateInfo = ServerProxy.getProjectHandler().getL10nProfileWfTemplateInfo(p_locprofile.getId(), wfId);
//							if( ! l10nProfileWFTemplateInfo.getIsActive())
//							{
//								continue L;
//							}
						} catch (Exception e1) {
							CATEGORY.error(e1.getMessage());
						}
						// get wf template info based on the id
						try {
							wfti = ServerProxy.getProjectHandler()
									.getWorkflowTemplateInfoById(wfId);
						} catch (Exception e) {
							throw new EnvoyServletException(e);
						}
						targetLocales.addElement(gl);
						workflowNames.addElement(wfti.getName());
						if(l10nProfileWFTemplateInfo != null){
							WorkflowInfos workflowInfo = new WorkflowInfos(wfId, wfti.getName(), l10nProfileWFTemplateInfo.getIsActive());
							workflowInfo.setTargetLocale(gl);
							workflowInfos.add(workflowInfo);
						}
						else
						{
							WorkflowInfos workflowInfo = new WorkflowInfos(wfId, wfti.getName(), true);
							workflowInfo.setTargetLocale(gl);
							workflowInfos.add(workflowInfo);
						}
			            sessionMgr.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);
				}
                
                for(int j=0; j<targetLocales.size(); j++)
                {
                    GlobalSightLocale tl = (GlobalSightLocale)targetLocales.elementAt(j);
                    displayTargetNames.add(tl.getDisplayName(uiLocale));                
                    targetObjects.add(tl);                
                }
            }

            String displaySourceName = p_locprofile.getSourceLocale().getDisplayName(uiLocale);

            sessionMgr.setAttribute(LocProfileStateConstants.TARGET_LOCALES, displayTargetNames);
            sessionMgr.setAttribute(LocProfileStateConstants.WORKFLOW_NAMES, workflowNames);
            sessionMgr.setAttribute(LocProfileStateConstants.SOURCE_LOCALE, displaySourceName);
            sessionMgr.setAttribute(LocProfileStateConstants.TARGET_OBJECTS, targetObjects);
        }
        if("remove".equals(action))
        {
        	/*
        	long locProfileId = p_locprofile.getId();
        	L10nProfileWFTemplateInfo l10nProfileWFTemplateInfo = new L10nProfileWFTemplateInfo();
        	L10nProfileWFTemplateInfoKey key = new L10nProfileWFTemplateInfoKey();
        	key.setL10nProfileId(locProfileId);
        	key.setWfTemplateId(wfTemplateId);
        	l10nProfileWFTemplateInfo.setIsActive(false);
        	l10nProfileWFTemplateInfo.setKey(key);
        	WorkflowInfos workflowInfo = new WorkflowInfos(locProfileId, wfTemplateId, false);
        	*/
            long wfTemplateId = Long.parseLong(p_request.getParameter("wfTemplateId"));
        	workflowInfos = (Vector<WorkflowInfos>)sessionMgr.getAttribute(LocProfileStateConstants.WORKFLOW_INFOS);
        	for(int i = 0; i < workflowInfos.size(); i++)
        	{
        	    /*
        		if(workflowInfo.equals(workflowInfos.get(i)))
        		{
        			workflowInfos.set(i, workflowInfo);
        		}
        		*/
        	    if(workflowInfos.get(i).getWfId() == wfTemplateId)
                {
                    workflowInfos.get(i).setActive(false);
                }
        	}
        	sessionMgr.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);
        	p_request.setAttribute(LocProfileStateConstants.WORKFLOW_INFOS, workflowInfos);

        }

    }
    
}
