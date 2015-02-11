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

import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import java.io.IOException;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class SetUserDefaultRolesHandler extends PageHandler
{
	private static Logger c_logger =
        Logger.getLogger(SetUserDefaultRolesHandler.class);
	
    public SetUserDefaultRolesHandler()
    {
    }

    /**
     * Invokes this PageHandler
     *
     * @param p_pageDescriptor the page desciptor
     * @param p_request the original request sent from the browser
     * @param p_response the original response object
     * @param p_context context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor pageDescriptor,
                                  HttpServletRequest request,
                                  HttpServletResponse response,
                                  ServletContext context)
    throws ServletException, IOException,
        EnvoyServletException
    {
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager)session.getAttribute(WebAppConstants.SESSION_MANAGER);
        ResourceBundle bundle = PageHandler.getBundle(session);
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr.getAttribute(UserConstants.MODIFY_USER_WRAPPER);

        String action = (String) request.getParameter("action");
        if (action != null && "save".equals(action)) {
        	saveData(request, sessionMgr, wrapper);
        } else if (action != null && "remove".equals(action)) {
        	String id = request.getParameter("id");
        	wrapper.removeDefaultRole(id);
        }
        
        ArrayList<UserDefaultRole> defaultRoles = wrapper.getTmpDefaultRoles();
        sessionMgr.setAttribute("defaultRoles", defaultRoles);
        sessionMgr.setAttribute("isEdit", "0");
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }
    
    private void saveData(HttpServletRequest request, SessionManager sessionMgr, ModifyUserWrapper wrapper) {
    	String editMark = (String)sessionMgr.getAttribute("isEdit");
    	boolean isEdit = false;
    	if (editMark != null && editMark.equals("1"))
    		isEdit = true;
    	UserDefaultRole role = null;
    	UserDefaultActivity ac = null;
    	String sourceLocale = "";
    	String[] targetLocales = null;
    	try {
			if (isEdit) {
				role = (UserDefaultRole)sessionMgr.getAttribute("defaultRole");
				sourceLocale = String.valueOf(role.getSourceLocaleId());
				targetLocales = new String[]{String.valueOf(role.getTargetLocaleId())};
			} else {
				role = new UserDefaultRole();
				sourceLocale = (String)request.getParameter("sourceLocale");
				targetLocales = (String[])request.getParameterValues("targetLocale");
			}
			//Get the total count of activities
			String activityCountStr = (String)request.getParameter("activityCount");
			int activityCount = 0;
			long sourceLocaleId, targetLocaleId;
			try {
				activityCount = Integer.parseInt(activityCountStr);
				sourceLocaleId = Long.parseLong(sourceLocale);
			} catch (NumberFormatException nfe) {
				activityCount = 0;
				sourceLocaleId = 0;
			}
			//Get selected activities
			ArrayList<String> acs = new ArrayList<String>();
			HashSet<UserDefaultActivity> activities = null;
			String tmp = "";
			for (int i=0;i<activityCount;i++) {
				tmp = (String)request.getParameter("activity_" + i);
				if (tmp != null) {
					acs.add(tmp);
				}
			}
			
			String userId = wrapper.getUserId();
			LocaleManager localeManager = ServerProxy.getLocaleManager();
			for (int i=0;i<targetLocales.length;i++) {
				targetLocaleId = Long.parseLong(targetLocales[i]);
				
				if (!isEdit) {
					//To add a new default role
					role = new UserDefaultRole();
					role.setUserId(userId);
					role.setSourceLocaleId(sourceLocaleId);
					role.setTargetLocaleId(targetLocaleId);
					role.setSourceLocaleObject(localeManager.getLocaleById(sourceLocaleId));
					role.setTargetLocaleObject(localeManager.getLocaleById(targetLocaleId));
					role.setStatus(UserDefaultRole.ADD);
				} else {
					//To save a modified default role
					if (!role.getStatus().equals(UserDefaultRole.ADD))
						role.setStatus(UserDefaultRole.EDIT);
					role.setActivities(null);
				}

				activities = new HashSet<UserDefaultActivity>();
				//Procced selected activities
				for (int j=0;j<acs.size();j++) {
					ac = new UserDefaultActivity();
					ac.setActivityName(acs.get(j));
					ac.setDefaultRole(role);
					
					activities.add(ac);
				}
				role.setActivities(activities);
				
				if (isEdit)
					wrapper.modifyDefaultRole(role);
				else
					wrapper.addDefaultRole(role);
			}
		} catch (Exception e) {
			c_logger.error(e.getMessage(), e);
		}
    }
}
