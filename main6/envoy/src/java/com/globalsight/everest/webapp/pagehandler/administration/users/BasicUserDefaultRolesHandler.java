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

import org.apache.log4j.Logger;

import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.calendar.UserFluxCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.*;

public class BasicUserDefaultRolesHandler
    extends PageHandler
{
	private static Logger c_logger = 
		Logger.getLogger(BasicUserDefaultRolesHandler.class.getName());
    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
        HttpServletRequest request, HttpServletResponse response,
        ServletContext context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);

        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        
        Locale uiLocale = (Locale)session.getAttribute(WebAppConstants.UILOCALE);
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);
        
        String action = (String)request.getParameter("action");
        if ("edit".equals(action)) {
        	String id = request.getParameter("id");
        	UserDefaultRole selRole = null;
        	try {
        		selRole = wrapper.getDefaultRoleFromTmp(id);
			} catch (Exception e) {
				selRole = new UserDefaultRole();
			}

			sessionMgr.setAttribute("isEdit", "1");
        	sessionMgr.setAttribute("defaultRole", selRole);
        	sessionMgr.setAttribute("curActivities", getActivityNames(selRole.getActivities()));
        } else if ("setTarget".equals(action)) {
        	//Get target locale according with selected source locale
        	String sourceLocaleId = request.getParameter("sourceLocale");
        	LocaleManager localeManager = ServerProxy.getLocaleManager();
        	Vector targetLocales = localeManager.getTargetLocales(localeManager.getLocaleById(Long.parseLong(sourceLocaleId)));
        	Vector nonTargetLocales = new Vector();
        	ArrayList<String> currentTLs = wrapper.getTargetLocalesOfDefaultRole();
        	GlobalSightLocale locale = null;
        	for (int i=0;i<targetLocales.size();i++) {
        		locale = (GlobalSightLocale) targetLocales.get(i);
        		if (!currentTLs.contains(sourceLocaleId + "_" + locale.getId()))
        			nonTargetLocales.add(locale);
        	}
        	
        	sessionMgr.setAttribute("sourceLocale", sourceLocaleId);
        	sessionMgr.setAttribute("targetLocales", nonTargetLocales);
        } else if (action == null) {
        	Vector sourceLocales = ServerProxy.getLocaleManager().getAllSourceLocales();
        	sessionMgr.setAttribute("sourceLocales", sourceLocales);
        	sessionMgr.setAttribute("sourceLocale", null);
        	sessionMgr.setAttribute("targetLocales", null);

        	sessionMgr.setAttribute("activities", null);
        }
        
		ArrayList<String> uniqueActivityNames = (ArrayList<String>)sessionMgr.getAttribute("activities");
    	ArrayList allActivities = null;
    	if (uniqueActivityNames == null) {
    		allActivities = new ArrayList(UserHandlerHelper.getAllActivities(uiLocale));
    		uniqueActivityNames = getUniqueActivityNames(allActivities);
    	}
		sessionMgr.setAttribute("activities", uniqueActivityNames);
    	
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
            response, context);
    }

    private ArrayList<String> getUniqueActivityNames(ArrayList p_acs) {
    	ArrayList<String> result = new ArrayList<String>();
    	if (p_acs == null || p_acs.size() == 0)
    		return result;
    	String activityName = "";
    	for (int i = 0; i < p_acs.size(); i++) {
    		activityName = ((Activity)p_acs.get(i)).getDisplayName().trim();
    		if (!result.contains(activityName))
    			result.add(activityName);
    	}
    	return result;
    }
    
    private ArrayList<String> getActivityNames(Set p_acs) {
    	ArrayList<String> result = new ArrayList<String>();
    	if (p_acs == null)
    		return result;
    	Iterator it = p_acs.iterator();
    	while (it.hasNext()) {
    		result.add(((UserDefaultActivity)it.next()).getActivityName());
		}
    	return result;
    }
}

