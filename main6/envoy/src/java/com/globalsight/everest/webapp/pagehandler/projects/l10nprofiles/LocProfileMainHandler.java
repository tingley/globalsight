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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfo;
import com.globalsight.everest.foundation.L10nProfileWFTemplateInfoKey;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocProfileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.FormUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;


/**
 * LocProfileMainHandler, A page handler to produce the entry
 * page(index.jsp) for L10nProfile management.
 */
public class LocProfileMainHandler
    extends PageHandler
    implements LocProfileStateConstants
{

    // Category for log4j logging.
    private static final Logger CATEGORY =
        Logger.getLogger(
            LocProfileMainHandler.class.getName());

    /**
     * Invokes this PageHandler
     * <p>
     * @param p_pageDescriptor the page descriptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        String action = p_request.getParameter("action");
        if (action == null || "cancel".equals(action))
        {
            clearSessionExceptTableInfo(session, LocProfileStateConstants.LOCPROFILE_KEY);
        }
        else if ("save".equals(action))
        {
        	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
    		{
    			p_response
    					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
    			return;
    		}
            boolean isNotDuplicateSubmission = false;
            if (FormUtil.isNotDuplicateSubmisson(p_request, FormUtil.Forms.NEW_LOCALIZATION_PROFILE))
            {
                isNotDuplicateSubmission = true;
            }
            else if (FormUtil.isNotDuplicateSubmisson(p_request, FormUtil.Forms.EDIT_LOCALIZATION_PROFILE))
            {
                isNotDuplicateSubmission = true;
            }

            if (isNotDuplicateSubmission)
            {
                createModifyLocProfile(p_request, session);
                
            }
        }
        else if ("remove".equals(action))
        {
        	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
    		{
    			p_response
    					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
    			return;
    		}
            doRemove(p_request, session);
        }
        else if ("saveDup".equals(action))
        {
        	if (p_request.getMethod().equalsIgnoreCase(REQUEST_METHOD_GET)) 
    		{
    			p_response
    					.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
    			return;
    		}
            duplicateProfile(p_request);
        }
        try
        {
            checkPreReqData(p_request, session);
            dataForTable(p_request, session);
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

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request,
            p_response, p_context);
    }

    private void doRemove(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException, IOException
    {
        try 
        {
            String id = (String)p_request.getParameter(RADIO_BUTTON);
            BasicL10nProfile locprofile = (BasicL10nProfile)
                    LocProfileHandlerHelper.getL10nProfile(Long.parseLong(id));
            String deps = LocProfileHandlerHelper.checkForDependencies(
                                locprofile, PageHandler.getBundle(p_session));
            if (deps == null)
            {
                LocProfileHandlerHelper.removeL10nProfile(locprofile);
            }
            else
            {
                SessionManager sessionMgr = (SessionManager)
                    p_session.getAttribute(WebAppConstants.SESSION_MANAGER);
                sessionMgr.setAttribute(DEPENDENCIES, deps);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private void createModifyLocProfile(HttpServletRequest p_request,
                                                    HttpSession p_session)
        throws EnvoyServletException, IOException
    {
        String value;
        SessionManager sessionMgr = (SessionManager)
             p_session.getAttribute(SESSION_MANAGER);
        

        // check for fields requiring a value
        String name = (String)sessionMgr.getAttribute(LOC_PROFILE_NAME);
        Vector<WorkflowInfos> workflowInfos = (Vector<WorkflowInfos>)sessionMgr.getAttribute(LocProfileStateConstants.WORKFLOW_INFOS);
        value = (String)sessionMgr.getAttribute(LOC_PROFILE_PROJECT_ID);
        long projectId = Long.parseLong(value);

        value = (String)sessionMgr.getAttribute(LOC_TM_PROFILE_ID);
        long tmProfileId = Long.parseLong(value);
      
        value = (String)sessionMgr.getAttribute(JOB_PRIORITY);
        int priority = Integer.parseInt(value);

        value = (String)sessionMgr.getAttribute(SOURCE_LOCALE_ID);
        long sourceLocaleId = Long.parseLong(value);

        int TMChoice = -1;
        boolean exactMatch = false;
        value = (String)sessionMgr.getAttribute(LOC_PROFILE_TM_USAGE_ID);
        int TMUsageId = Integer.parseInt(value);
        if (TMUsageId == NO_TM_USAGE)
        {
            TMChoice = L10nProfile.NO_TM;
            exactMatch = false;
        }
        if (TMUsageId == DENY_EDIT_TM_USAGE)
        {
            TMChoice = L10nProfile.REGULAR_TM_WITH_PAGE_TM;
            exactMatch = false;
        }
        if (TMUsageId == ALLOW_EDIT_TM_USAGE)
        {
            TMChoice = L10nProfile.REGULAR_TM_WITH_PAGE_TM;
            exactMatch = true;
        }

        boolean automaticDispatch = false;
        value = (String)sessionMgr.getAttribute(AUTOMATIC_DISPATCH);
        if (value.equals("true"))
        {
            automaticDispatch = true;
        }
        else if (value.equals("false"))
        {
            automaticDispatch = false;
        }

        // load the fields that do not need a value
        String description = (String)sessionMgr.getAttribute(LOC_PROFILE_DESCRIPTION);

        boolean runSQLScript;
        String SQLScript = null;
        if (sessionMgr.getAttribute(LOC_PROFILE_SQL_SCRIPT) == null)
        {
            runSQLScript = false;
        }
        else
        {
            runSQLScript = true;
            SQLScript = (String)sessionMgr.getAttribute(LOC_PROFILE_SQL_SCRIPT);
            if (SQLScript.length() == 0)
            {
                runSQLScript = false;
            }
        }

        // determine whether to create or modify BasicL10nProfile
        BasicL10nProfile locprofile;
        if (sessionMgr.getAttribute("edit") != null)
        {
            locprofile = (BasicL10nProfile)sessionMgr.getAttribute("locprofile");
            locprofile.setName(name);
        }
        else
        {
            locprofile = new BasicL10nProfile(name);
        }

        // fill in user inputs
        locprofile.setSourceLocale(LocProfileHandlerHelper.getLocaleById(sourceLocaleId));
        locprofile.setDescription(description);
        locprofile.setCompanyId(CompanyThreadLocal.getInstance().getValue());


        Project project = null;
        try
        {
            // set the project - in case it was changed.
            // the id will be set too.
            project = ServerProxy.getProjectHandler().getProjectById(projectId);
        } catch (Exception e)
        {
            CATEGORY.error("Failed to find the project associated with " + projectId);
            return;
        }
        locprofile.setProject(project);
        locprofile.setAutomaticDispatch(automaticDispatch);
        locprofile.setRunScriptAtJobCreation(runSQLScript);
        locprofile.setJobCreationScriptName(SQLScript);
        locprofile.setTMChoice(TMChoice);
        locprofile.setExactMatchEditing(exactMatch);
        locprofile.setPriority(priority);

        Boolean isSamePM = (Boolean)sessionMgr.getAttribute(
            LocProfileStateConstants.IS_SAME_PROJECT_MANAGER);
        // the project was changed and the new project's
        // PM was not the same as the previous
        // Project's PM, remove the existing workflow templates
        // and add the new ones.
        Boolean isSameProject = (Boolean)sessionMgr.getAttribute(
            LocProfileStateConstants.IS_SAME_PROJECT);
        if (isSameProject != null &&
            !isSameProject.booleanValue() &&
            isSamePM != null &&
            !isSamePM.booleanValue())
        {
            locprofile.setWorkflowTemplateInfos(new Vector());
        }

        Hashtable wftLocaleHash = (Hashtable)sessionMgr.getAttribute(
            WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH);
        
        //If the user go to path "Add/Edit Workflows " -> "Attach Workflows to Target Locales" -> "Save" button, 
        //then the TARGET_LOCALE_IN_BOX exists. But if the user go path
        //"Add/Edit Workflows " -> "Save" button, then the TARGET_LOCALE_IN_BOX 
        //doesn't exist.
        Vector targetLocales = (Vector)sessionMgr.getAttribute(WorkflowTemplateConstants.TARGET_LOCALE_IN_BOX);
        if (targetLocales == null) {
            targetLocales = (Vector) sessionMgr.getAttribute(LocProfileStateConstants.TARGET_OBJECTS);
        }
		for (int k = 0; k < targetLocales.size(); k++) {
			GlobalSightLocale targetLocale = (GlobalSightLocale)targetLocales.elementAt(k);
//			WorkflowInfos workflowInfo = workflowInfos.elementAt(k);
//			if( ! workflowInfo.isActive())
//			{
//				continue;
//			}
			List ids = (List) wftLocaleHash.get(targetLocale);
			if(ids != null && !ids.isEmpty()){
				// Clear workflowTemplateInfoList related to the targetLocale first
				locprofile.clearWorkflowTemplateInfo(targetLocale);				
				for (int i = 0; i < ids.size(); i++) {
					long wfId = -1;
					try {
						wfId = Long.parseLong((String) ids.get(i));
					} catch (NumberFormatException nfe) {
					}
					// Add workflowTemplateInfo to workflowTemplateInfoList
					try {
						locprofile.addWorkflowTemplateInfo(ServerProxy.getProjectHandler()
								.getWorkflowTemplateInfoById(wfId));
					} catch (Exception e) {
						throw new EnvoyServletException(e);
					}
				}
			}
		}

        // Integrate with TM Profiles
        try
        {
            if (tmProfileId > 0)
            {
                TranslationMemoryProfile tmProfile =
                     ServerProxy.getProjectHandler().getTMProfileById(tmProfileId, false);
                locprofile.addTMProfile(tmProfile);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
        // check for any WorkflowTasks to persist
        Vector workflowtransmit;
        if (sessionMgr.getAttribute("edit") != null)
        {
            // persist the modifications in database
            LocProfileHandlerHelper.modifyL10nProfile(locprofile, workflowInfos);
        }
        else
        {
            if (wftLocaleHash != null) 
            {
                // persist BasicL10Profile in database
                LocProfileHandlerHelper.addL10nProfile(locprofile);
            }
        }
        //These codes are not needed any more
        //if (sessionMgr.getAttribute("edit") == null)
        //{
        //    modifyLnProfileWFTemplateInfo(locprofile, workflowInfos);
        //}
        
        // reset the state after successful store to database
        clearSessionExceptTableInfo(p_session, LocProfileStateConstants.LOCPROFILE_KEY);
    }
/*
    private void modifyLnProfileWFTemplateInfo(BasicL10nProfile locprofile,
			Vector<WorkflowInfos> workflowInfos) {
		Vector<WorkflowTemplateInfo> workflowTemplateInfos = locprofile.getWorkflowTemplateInfoList();
		for(int i = 0; i < workflowTemplateInfos.size(); i++)
		{
			WorkflowTemplateInfo wfInfo = workflowTemplateInfos.get(i);
			L10nProfileWFTemplateInfo lnWfInfo = new L10nProfileWFTemplateInfo();
			L10nProfileWFTemplateInfoKey key = new L10nProfileWFTemplateInfoKey();
			key.setL10nProfileId(locprofile.getId());
			key.setWfTemplateId(wfInfo.getId());
			lnWfInfo.setKey(key);
			lnWfInfo.setIsActive(true);
			try {
				ServerProxy.getProjectHandler().saveL10nProfileWfTemplateInfo(lnWfInfo);
			} catch (Exception e) {
				CATEGORY.error("The exception is " + e);
			}
		}
	}
*/
	private void duplicateProfile(HttpServletRequest p_request)
        throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
        String lpId = (String)sessionMgr.getAttribute(DUP_LOC_PROFILE);
        String list = (String)p_request.getParameter("localePairs");
        String name = (String)p_request.getParameter("nameTF");
        try {
            ArrayList alist = new ArrayList();
            StringTokenizer st = new StringTokenizer(list, ",");
            while (st.hasMoreTokens()) {
                String id = st.nextToken();
                alist.add(ServerProxy.getLocaleManager().getLocalePairById(Long.parseLong(id)));
            }

            LocProfileHandlerHelper.duplicateL10nProfile(
                                                Long.parseLong(lpId),
                                                alist,
                                                name,
                                                getBundle(session));
        } catch (Exception e) {
            CATEGORY.error("The exception is " + e);
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Before being able to create a Rate, certain objects must exist.
     * Check that here.
     */
    private void checkPreReqData(HttpServletRequest p_request, HttpSession p_session)
        throws EnvoyServletException
    {
        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        SessionManager sessMgr = (SessionManager) p_request.getSession().getAttribute(SESSION_MANAGER);
        User user = (User)sessMgr.getAttribute(WebAppConstants.USER);
        List allProjects = LocProfileHandlerHelper.getAllProjectNamesForManagedUser(user);
        List allSrcLocales = LocProfileHandlerHelper.getAllSourceLocales(uiLocale);

        if (allProjects == null || allProjects.size() < 1
            || allSrcLocales == null || allSrcLocales.size() < 1)
        {
            ResourceBundle bundle = getBundle(p_session);
            StringBuffer message = new StringBuffer();
            boolean addcomma = false;
            message.append(bundle.getString("msg_prereq_warning_1"));
            message.append(":  ");
            if(allProjects == null || allProjects.size() < 1)
            {
                message.append(bundle.getString("lb_projects"));
                addcomma = true;
            }
            if(allSrcLocales == null || allSrcLocales.size() < 1)
            {
                if (addcomma) message.append(", ");
                message.append(bundle.getString("lb_currency"));
            }
            message.append(".  ");
            message.append(bundle.getString("msg_prereq_warning_2"));

            p_request.setAttribute("preReqData", message.toString());
        }
    }

    /**
     * Get list of all profiles, sorted appropriately
     */
    private void dataForTable(HttpServletRequest p_request, HttpSession p_session)
        throws RemoteException, NamingException, GeneralException
    {
        Vector locprofiles = LocProfileHandlerHelper.getAllL10nProfilesForGUI();

        Locale uiLocale = (Locale)p_session.getAttribute(
                                    WebAppConstants.UILOCALE);

        setTableNavigation(p_request, p_session, locprofiles,
                       new LocProfileComparator(uiLocale),
                       10,
                       LocProfileStateConstants.LOCPROFILE_LIST,
                       LocProfileStateConstants.LOCPROFILE_KEY);
    }
}

