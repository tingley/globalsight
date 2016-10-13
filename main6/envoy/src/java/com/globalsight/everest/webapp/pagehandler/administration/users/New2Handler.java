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
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.modules.Modules;

public class New2Handler extends PageHandler implements UserStateConstants
{
    /**
     * Invokes this PageHandler.
     */
    public void invokePageHandler(WebPageDescriptor p_thePageDescriptor,
                                  HttpServletRequest p_theRequest,
				  HttpServletResponse p_theResponse,
	                          ServletContext p_context)
    throws ServletException, IOException, EnvoyServletException
    {
        // Get the session manager.
        HttpSession session = p_theRequest.getSession();
        SessionManager sessionMgr = (SessionManager) session.getAttribute(SESSION_MANAGER);
        CreateUserWrapper wrapper;

        //If from 'Add Another' button
        String action = p_theRequest.getParameter(USER_ACTION);
        String sourceLocale = null;
        String targetLocale = null;
        boolean addAnotherFlag = false;
        // Get the CreateUserWrapper off the session manager
        wrapper = (CreateUserWrapper) sessionMgr.getAttribute(
              UserConstants.CREATE_USER_WRAPPER);
        if(WebAppConstants.USER_ACTION_SET_SOURCE.equals(action))
        {
            p_theRequest.setAttribute("selectSource", "true");    
        }
        if ("next".equals(action))
        {
            if (Modules.isCalendaringInstalled())
            {
                //From Calendar, save calendar data
                UserUtil.extractCalendarData(p_theRequest, wrapper.getUserId());
            }
            else 
            {
                // Save the data from the base user page
                UserUtil.extractContactInfoData(p_theRequest, wrapper);
                UserUtil.prepareRolesPage(session, p_theRequest, wrapper,
                     null, null, false);

            }
        }        
        else if ("previous".equals(action))
        {
            // save projects data
            UserUtil.extractProjectData(p_theRequest, wrapper);
        }
        else
        {            
            //Extract source/target locale, plus the activity-cost map.
            sourceLocale = UserUtil.extractSourceLocale(p_theRequest);
            targetLocale = UserUtil.extractTargetLocale(p_theRequest);
            Hashtable activityCostMap =
                UserUtil.generateActivityCostMap(p_theRequest);

            if (action.intern() == USER_ACTION_ADD_ANOTHER_LOCALES)
            {
                addAnotherFlag = true;
                wrapper.addUserRoles(sourceLocale, targetLocale, activityCostMap);
                sessionMgr.setAttribute("roleAdded", "true");
            }
        }

        UserUtil.prepareRolesPage(session, p_theRequest, wrapper,
                                  sourceLocale, targetLocale,
                                  addAnotherFlag);

        //Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(p_thePageDescriptor, p_theRequest,
            p_theResponse,p_context);

    }

    /**
     * Overide getControlFlowHelper so we can do processing
     * and redirect the user correctly.  This is needed because project
     * management is an add-on feature, so we need to turn it on and off.
     *
     * @return the name of the link to follow
     */
    public ControlFlowHelper getControlFlowHelper(
        HttpServletRequest p_request, HttpServletResponse p_response)
    {

        return new New2ControlFlowHelper(p_request, p_response);
    }

}
