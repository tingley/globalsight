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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.CodeSetImpl;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.LocaleComparator;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobManagementHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * This class handles the Basic Info screen for Workflows
 */

public class BasicWorkflowTemplateHandler extends PageHandler implements
        WorkflowTemplateConstants
{

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    public BasicWorkflowTemplateHandler()
    {
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

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
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        String action = (String) p_request
                .getParameter(WorkflowTemplateConstants.ACTION);
        p_request.setAttribute(WorkflowTemplateConstants.ACTION, action);

        if (action != null
                && action.equals(WorkflowTemplateConstants.LEVERAGE_ACTION))
        {
            sessionMgr.setAttribute(CHOSEN_NAME,
                    p_request.getParameter(NAME_FIELD));
            sessionMgr.setAttribute(CHOSEN_DESCRIPTION,
                    p_request.getParameter(DESCRIPTION_FIELD));
            sessionMgr.setAttribute(CHOSEN_PROJECT,
                    p_request.getParameter(PROJECT_FIELD));
            sessionMgr.setAttribute(CHOSEN_NOTIFICATION,
                    p_request.getParameter(NOTIFICATION_FIELD));
            sessionMgr.setAttribute(CHOSEN_LOCALE_PAIR,
                    p_request.getParameter(LOCALE_PAIR_FIELD));
            sessionMgr.setAttribute(CHOSEN_TARGET_ENCODING,
                    p_request.getParameter(ENCODING_FIELD));
        }
        else
        {
            setInfoInSession(p_request, action);
        }

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_pageDescriptor, p_request, p_response,
                p_context);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Override Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Set necessary information in the session. This is when they are creating
     * a NEW workflow.
     * 
     * @param p_request
     * @exception EnvoyServletException
     */
    private void setInfoInSession(HttpServletRequest p_request, String action)
            throws EnvoyServletException
    {
        // need to provide a list of PMs, Locale pairs, and target encoding
        HttpSession session = p_request.getSession(false);
        PermissionSet perms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        Locale uiLocale = (Locale) session
                .getAttribute(WebAppConstants.UILOCALE);

        // If new workflow and not admin, get only the projects for that
        // user (PM).
        List projectInfos;
        if (!EDIT_ACTION.equals(action)
                && !perms.getPermissionFor(Permission.GET_ALL_PROJECTS))
        {
            String userName = (String) session
                    .getAttribute(WebAppConstants.USER_NAME);
            User user = UserHandlerHelper.getUser(userName);
            projectInfos = WorkflowTemplateHandlerHelper
                    .getAllProjectInfosForUser(user, uiLocale);
        }
        else
        {
            projectInfos = WorkflowTemplateHandlerHelper
                    .getAllProjectInfos(uiLocale);
        }
        HashMap wfManagers = WorkflowTemplateHandlerHelper
                .getAllWorkflowManagersInProject();
        List localePairs = WorkflowTemplateHandlerHelper
                .getAllLocalePairs(uiLocale);
        List targetEncodingList = new ArrayList();
        CodeSetImpl codeSet = new CodeSetImpl();
        codeSet.setCodeSet(JobManagementHandler.SAME_AS_SOURCE);
        targetEncodingList.add(codeSet);
        List targetEncodings = WorkflowTemplateHandlerHelper.getAllCodeSets();
        targetEncodingList.addAll(targetEncodings);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // now set the values in the request
        sessionMgr.setAttribute(PROJECTS, projectInfos);
        sessionMgr.setAttribute(WORKFLOW_MANAGERS, wfManagers);
        sessionMgr.setAttribute(LOCALE_PAIRS, localePairs);
        sessionMgr.setAttribute(ENCODINGS, targetEncodingList);

        // get the template id first (for edit, or duplicate actions)
        String id = p_request.getParameter(WF_TEMPLATE_INFO_ID);
        if (id != null)
        {
            addWorkflowToRequest(p_request, id);
        }
        else
        {
            setLeverageLocales(p_request, localePairs);
            try
            {
                // set the email notification flag during the creation of
                // workflow. The value is in the envoy.properties
                SystemConfiguration sc = SystemConfiguration.getInstance();
                sessionMgr.setAttribute(CHOSEN_NOTIFICATION,
                        sc.getStringParameter(sc.PM_EMAIL_NOTIFICATION));
            }
            catch (Exception e)
            {
            }
        }
    }

    /**
     * This method is used during modify workflow template. In this case, all
     * required info should be stored in session manager. If values are
     * modified, they'll also be updated in session manager.
     * 
     * @param p_request
     * @param p_wfId
     * @exception EnvoyServletException
     */
    private void addWorkflowToRequest(HttpServletRequest p_request,
            String p_wfId) throws EnvoyServletException
    {
        long wfId = -1;
        // This should be part of a util class (after adding paging to a util
        // class, i'll add
        // this one too.
        try
        {
            wfId = Long.parseLong(p_wfId);
        }
        catch (NumberFormatException nfe)
        {
        }
        HttpSession session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // first try the session manager (in case the user clicked on previous)
        WorkflowTemplateInfo storedWfti = (WorkflowTemplateInfo) sessionMgr
                .getAttribute(WF_TEMPLATE_INFO);

        // get wf template info based on the id
        WorkflowTemplateInfo wfti = storedWfti == null ? WorkflowTemplateHandlerHelper
                .getWorkflowTemplateInfoById(wfId) : storedWfti;

        // pass the id to the request (to be used as part of the next url
        p_request.setAttribute(TEMPLATE_ID,
                new Long(wfti.getWorkflowTemplateId()));

        LocalePair lp = WorkflowTemplateHandlerHelper
                .getLocalePairBySourceTargetIds(wfti.getSourceLocale().getId(),
                        wfti.getTargetLocale().getId());
        ArrayList localePairs = new ArrayList();
        localePairs.add(lp);
        setLeverageLocales(p_request, localePairs);

        // WF_TEMPLATE_INFO
        sessionMgr.setAttribute(LOCALE_PAIR, Long.toString(lp.getId()));
        sessionMgr.setAttribute(WF_TEMPLATE_INFO, wfti);
        // Action type (edit or duplicate)
        String actionType = (String) p_request.getParameter(ACTION);
        sessionMgr.setAttribute(ACTION, actionType);
    }

    /**
     * Set the all possible locales available for leveraging based on the
     * available locale pairs. We'll then use these locales in javascript to
     * populate the "Leverage from:" multi-select list.
     * 
     * @param p_request
     * @param localePairs
     * @exception EnvoyServletException
     */
	@SuppressWarnings("unchecked")
	public void setLeverageLocales(HttpServletRequest p_request,
            List localePairs) throws EnvoyServletException
    {
        HttpSession p_session = p_request.getSession(false);
        SessionManager sessionMgr = (SessionManager) p_session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        // set new target locales if target language is selected
        String newTargetLangCode = null;
        Locale uiLocale = (Locale) p_session
                .getAttribute(WebAppConstants.UILOCALE);
        Hashtable supportedLocale = getSupportedLocales(uiLocale);

        Vector<String> targetLeverageLocalesDisplays = new Vector<String>();
        Vector<GlobalSightLocale> targetLeverageLocalesObjects = new Vector<GlobalSightLocale>();
        Vector<GlobalSightLocale> supportedLocales = (Vector<GlobalSightLocale>) supportedLocale.get("object");
        Vector<String> displayNames = (Vector<String>) supportedLocale.get("display");
        GlobalSightLocale gsl = null;
        String displayName = null;
        for (int i = 0; i < localePairs.size(); i++)
        {
            LocalePair lp = (LocalePair) localePairs.get(i);
            GlobalSightLocale trgLocale = (GlobalSightLocale) lp.getTarget();
            newTargetLangCode = trgLocale.getLanguageCode();

            // add matching cross-locales based on lang code
            for (int j = 0; j < supportedLocales.size(); j++)
            {
            	gsl = supportedLocales.elementAt(j);
            	displayName = displayNames.elementAt(j);
                String lang = gsl.getLanguageCode();

				if (newTargetLangCode.equals("no") || newTargetLangCode.equals("nb") 
						|| newTargetLangCode.equals("nn"))
				{
					if (!validLeverageLocale(trgLocale, gsl)
							|| (targetLeverageLocalesObjects.contains(gsl)))
                    {
                        continue;
                    }

					if (lang.equals("no") || lang.equals("nb") || lang.equals("nn"))
					{
						targetLeverageLocalesObjects.addElement(gsl);
						targetLeverageLocalesDisplays.addElement(displayName);
					}
				}
                else
                {
	                if (newTargetLangCode.equals(lang))
	                {
	                    // check special exclusionary cases
						if (!validLeverageLocale(trgLocale, gsl)
								|| (targetLeverageLocalesObjects.contains(gsl)))
	                    {
	                        continue;
	                    }
	
	                    targetLeverageLocalesObjects.addElement(gsl);
						targetLeverageLocalesDisplays.addElement(displayName);
	                }
               }
            }
        }
		sessionMgr.setAttribute(LEVERAGE_OBJ,
				(Vector<GlobalSightLocale>) targetLeverageLocalesObjects);
		sessionMgr.setAttribute(LEVERAGE_DISP,
				(Vector<String>) targetLeverageLocalesDisplays);
    }

    /**
     * Create hashtable of supported locale names and ids
     * 
     * @param p_uiLocale
     * @return
     * @exception EnvoyServletException
     */
    private Hashtable getSupportedLocales(Locale p_uiLocale)
            throws EnvoyServletException
    {
        // get all supported locales for user to choose
        Vector supportedLocales = LocProfileHandlerHelper.getSupportedLocales();
        // - use this code for sorting based on getDisplayName(uiLocale) by
        // passing 2
        SortUtil.sort(supportedLocales, new LocaleComparator(2, p_uiLocale));
        Hashtable result = new Hashtable();

        // for each locale, get language to display
        long id = 0;
        GlobalSightLocale locale;
        GlobalSightLocale localeToBeRemoved = null;
        Vector displayNames = new Vector();
        for (int i = 0; i < supportedLocales.size(); i++)
        {
            locale = (GlobalSightLocale) supportedLocales.elementAt(i);
            displayNames.add(locale.getDisplayName(p_uiLocale));
        }

        // need to send both the object and display name based on the locale
        // (since display
        // name within applet will result in incorrect names for some locales
        // due to
        // non-java 2 jvm)
        result.put("object", supportedLocales);
        result.put("display", displayNames);
        return result;
    }

    /**
     * Helps to handle certain leverage exclusions
     * 
     * @param p_defaultTargetLocale
     * @param p_leverageLocale
     * @return
     */
    private boolean validLeverageLocale(
            GlobalSightLocale p_defaultTargetLocale,
            GlobalSightLocale p_leverageLocale)
    {
        boolean result = true;

        // exclude Chinese (tiwan) if default target is Chinese (china)
        if (p_defaultTargetLocale.toString().equals("zh_CN")
                && p_leverageLocale.toString().equals("zh_TW"))
        {
            result = false;
        }

        // exclude Chinese (china) if default target is Chinese (Tiwan)
        if (p_defaultTargetLocale.toString().equals("zh_TW")
                && p_leverageLocale.toString().equals("zh_CN"))
        {
            result = false;
        }

        return result;
    }
}
