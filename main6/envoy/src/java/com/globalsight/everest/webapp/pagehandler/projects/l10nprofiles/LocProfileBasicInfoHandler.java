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

// Envoy packages
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.webapp.pagehandler.administration.tmprofile.TMProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.workflow.WorkflowTemplateConstants;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectInfo;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.util.comparator.TMProfileComparator;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;

//Sun
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * PageHandler for entering loc profile data.
 * <p>
 * @see com.globalsight.everest.webapp.pagehandler.PageHandler
 */
public class LocProfileBasicInfoHandler
    extends PageHandler
    implements LocProfileStateConstants
{

    // Category for log4j logging.
    private static final GlobalSightCategory CATEGORY =
        (GlobalSightCategory) GlobalSightCategory.getLogger(
            LocProfileBasicInfoHandler.class.getName());


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

        HttpSession session = p_request.getSession();
        SessionManager sessionMgr =
                (SessionManager) session.getAttribute(SESSION_MANAGER);
        String action = (String)p_request.getParameter("action");

        getDataForBasicPage(sessionMgr, session);

        if ("edit".equals(action))
        {
            sessionMgr.setAttribute("edit", "true");
            String id = (String)p_request.getParameter(RADIO_BUTTON);
            if (id == null
					|| p_request.getMethod().equalsIgnoreCase(
							REQUEST_METHOD_GET)) 
			{
				p_response
						.sendRedirect("/globalsight/ControlServlet?activityName=locprofiles");
				return;
			}
            BasicL10nProfile locprofile = (BasicL10nProfile)
                LocProfileHandlerHelper.getL10nProfile(Long.parseLong(id));
            sessionMgr.setAttribute("locprofile", locprofile);
            setupState(p_request, locprofile);
            getEditData(sessionMgr, session, locprofile);
        }
        
        super.invokePageHandler(p_pageDescriptor, p_request,
                                p_response,p_context);
    }

    /**
     * Get the data for combo boxes.
     */
    private void getDataForBasicPage(SessionManager sessionMgr, HttpSession p_session)
        throws ServletException, IOException, EnvoyServletException
    {
        // L10n profiles (for dup name checking
        sessionMgr.setAttribute("names", LocProfileHandlerHelper.getL10nProfileNames());

        // Source Locales
        Locale uiLocale = (Locale)p_session.getAttribute(UILOCALE);
        List sourceLocales = LocProfileHandlerHelper.getAllSourceLocales(uiLocale);
        sessionMgr.setAttribute("srcLocales", sourceLocales);

        // TmProfiles
        List tmProfiles = TMProfileHandlerHelper.getAllTMProfiles();
        TMProfileComparator comp =
            new TMProfileComparator(TMProfileComparator.NAME, uiLocale);
        Collections.sort(tmProfiles, comp);
        sessionMgr.setAttribute("tmProfiles", tmProfiles);

        // Projects
        User user = (User)sessionMgr.getAttribute(WebAppConstants.USER);
        List projectInfos = LocProfileHandlerHelper.getAllProjectNamesForManagedUser(user);
        Collections.sort(projectInfos, new ProjectComparator(uiLocale));
        sessionMgr.setAttribute("projects", projectInfos);

        // Priorities
       int maxPriority;
       int defaultPriority;
       try
       {
           SystemConfiguration sc = SystemConfiguration.getInstance();
           maxPriority = sc.getIntParameter(SystemConfigParamNames.MAX_PRIORITY);
           defaultPriority = sc.getIntParameter(SystemConfigParamNames.DEFAULT_PRIORITY);
       }
       catch (Throwable e)
       {
           //  Default to some know default values
           maxPriority = 5;
           defaultPriority = 3;
       }
       sessionMgr.setAttribute("maxPriority", new Integer(maxPriority));
       sessionMgr.setAttribute("defaultPriority", new Integer(defaultPriority));

        // Source Locales
        sessionMgr.setAttribute("srcLocales",
             LocProfileHandlerHelper.getAllSourceLocales(uiLocale));
    }


    /**
     * Get data only needed if editing the profile.
     */
    private void getEditData(SessionManager sessionMgr, HttpSession p_session,
                             BasicL10nProfile p_locprofile)
        throws ServletException, IOException, EnvoyServletException
    {
        Locale uiLocale = (Locale)p_session.getAttribute(UILOCALE);

        // Target Locales
        GlobalSightLocale[] targets = p_locprofile.getTargetLocales();
        Vector displayTargetNames = new Vector();
        Vector targetObjects = new Vector();
        Hashtable wftLocaleHash = new Hashtable();
        for (int j = 0; j < targets.length; j++)
        {
        	List wfIds = new ArrayList(2);
			WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) p_locprofile
					.getWorkflowTemplateInfo((GlobalSightLocale) targets[j]);
			if(wfti != null){
				wfIds.add(String.valueOf(wfti.getId()));
			}		
			WorkflowTemplateInfo dtpWfti = p_locprofile
					.getDtpWorkflowTemplateInfo(targets[j]);
			if (dtpWfti != null) {
				wfIds.add(String.valueOf(dtpWfti.getId()));
			}
            displayTargetNames.add(targets[j].getDisplayName(uiLocale));
            targetObjects.add(targets[j]);
            wftLocaleHash.put(targets[j], wfIds);
//            WorkflowInfos workflowInfo = new WorkflowInfos(wfti.getId(), wfti.getName(), );
        }
        sessionMgr.setAttribute(WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH,
            wftLocaleHash);
        sessionMgr.setAttribute(LocProfileStateConstants.TARGET_LOCALES, 
            displayTargetNames);
        sessionMgr.setAttribute(LocProfileStateConstants.SOURCE_LOCALE,
             p_locprofile.getSourceLocale().getDisplayName(uiLocale));
        sessionMgr.setAttribute(LocProfileStateConstants.TARGET_OBJECTS,
            targetObjects);
    }

    /**
     * This was moved from LocProfileMainHandler. (2/05)
     * I moved it because this is only needed if doing an edit and it
     * was doing it no matter what.
     */
    private void setupState(HttpServletRequest p_request,
                                  BasicL10nProfile p_locprofile)
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);

        Vector displayTargetNames = new Vector();
        Vector targetObjects = new Vector();
        Locale uiLocale = (Locale)session.getAttribute(UILOCALE);
        String displaySourceName = p_locprofile.getSourceLocale().getDisplayName(uiLocale);
        GlobalSightLocale[] targets = p_locprofile.getTargetLocales();
        Hashtable wftLocaleHash = new Hashtable();

        for(int j=0; j<targets.length; j++)
        {
        	List wfIds = new ArrayList(2);
			WorkflowTemplateInfo wfti = p_locprofile
					.getWorkflowTemplateInfo((GlobalSightLocale) targets[j]);
			if (wfti != null) {
				wfIds.add(String.valueOf(wfti.getId()));
			}
			WorkflowTemplateInfo dtpWfti = p_locprofile
					.getDtpWorkflowTemplateInfo(targets[j]);
			if (dtpWfti != null) {
				wfIds.add(String.valueOf(dtpWfti.getId()));
			}
			displayTargetNames.add(targets[j].getDisplayName(uiLocale));
			targetObjects.add(targets[j]);
			wftLocaleHash.put(targets[j], wfIds);
        }
        sessionMgr.setAttribute(WorkflowTemplateConstants.LOCALE_WORKFLOW_HASH, wftLocaleHash);
        sessionMgr.setAttribute(LocProfileStateConstants.SOURCE_LOCALE, displaySourceName);
        sessionMgr.setAttribute(LocProfileStateConstants.TARGET_LOCALES, displayTargetNames);
        sessionMgr.setAttribute(LocProfileStateConstants.TARGET_OBJECTS, targetObjects);

        
        // name
        sessionMgr.setAttribute(LOC_PROFILE_NAME, (String)p_locprofile.getName());
        // description
        sessionMgr.setAttribute(LOC_PROFILE_DESCRIPTION,
            (String)p_locprofile.getDescription());

        // project id
        Long Ltmp = new Long((long)p_locprofile.getProjectId());
        sessionMgr.setAttribute(LOC_PROFILE_PROJECT_ID, Ltmp.toString());

        // priority
        sessionMgr.setAttribute(JOB_PRIORITY, Integer.toString(p_locprofile.getPriority()));

        //tmProfile id
        Iterator it = p_locprofile.getTranslationMemoryProfiles().iterator();
        TranslationMemoryProfile tmProfile = (TranslationMemoryProfile)it.next();
        Long LtmProfile = new Long(tmProfile.getId());
        sessionMgr.setAttribute(LOC_TM_PROFILE_ID,LtmProfile.toString());
        // source locale id
        GlobalSightLocale locale =
            (GlobalSightLocale)p_locprofile.getSourceLocale();
        Integer Itmp = new Integer((int)locale.getId());
        sessionMgr.setAttribute(SOURCE_LOCALE_ID, Itmp.toString());

        // tm usage id
        int tmchoice = (int)p_locprofile.getTMChoice();
        boolean test = (boolean)p_locprofile.isExactMatchEditing();
        if (test)
        {
            if (tmchoice == L10nProfile.REGULAR_TM_WITH_PAGE_TM)
            {
                Itmp = new Integer((int)ALLOW_EDIT_TM_USAGE);
                sessionMgr.setAttribute(LOC_PROFILE_TM_USAGE_ID, Itmp.toString());
            }
        }
        else
        {
            if (tmchoice == L10nProfile.REGULAR_TM_WITH_PAGE_TM)
            {
                Itmp = new Integer((int)DENY_EDIT_TM_USAGE);
                sessionMgr.setAttribute(LOC_PROFILE_TM_USAGE_ID, Itmp.toString());
            }
            if (tmchoice == L10nProfile.NO_TM)
            {
                Itmp = new Integer((int)NO_TM_USAGE);
                sessionMgr.setAttribute(LOC_PROFILE_TM_USAGE_ID, Itmp.toString());
            }
        }

        // automatic dispatch
        String chosenAutomaticDispatch;
        test = (boolean)p_locprofile.dispatchIsAutomatic();
        if (test)
        {
            chosenAutomaticDispatch = "true";
        }
        else
        {
            chosenAutomaticDispatch = "false";
        }
        sessionMgr.setAttribute(AUTOMATIC_DISPATCH, chosenAutomaticDispatch);

        // sql script
        String chosenSQLScript;
        test = (boolean)p_locprofile.runScriptAtJobCreation();
        if (test)
        {
            chosenSQLScript = (String)p_locprofile.getNameOfJobCreationScript();
        }
        else
        {
            chosenSQLScript = "";
        }
        sessionMgr.setAttribute(LOC_PROFILE_SQL_SCRIPT, chosenSQLScript);

    }
}
