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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.foundation.SSOUserMapping;
import com.globalsight.everest.foundation.SSOUserUtil;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.securitymgr.FieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

public class Modify1Handler extends PageHandler
{
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

        String action = (String) request.getParameter(CalendarConstants.ACTION);
        if ("edit".equals(action))
        {
            User loggedInUser = (User) sessionMgr
                    .getAttribute(WebAppConstants.USER);
            String userId = request.getParameter("radioBtn");
            if (userId == null || request.getMethod().equalsIgnoreCase("get"))
            {
                response.sendRedirect("/globalsight/ControlServlet?activityName=users");
                return;
            }

            User user = UserHandlerHelper.getUser(userId);
            ModifyUserWrapper muw = UserHandlerHelper.createModifyUserWrapper(
                    loggedInUser, user);
            SSOUserMapping ssoMapping = SSOUserUtil.getUserMapping(user);
            if (ssoMapping != null)
            {
                muw.setSsoUserId(ssoMapping.getSsoUserId());
            }
            sessionMgr.setAttribute(MODIFY_USER_WRAPPER, muw);
            // Also put field level securities hash table in the session
            sessionMgr.setAttribute("securitiesHash",
                    UserHandlerHelper.getSecurity(user, loggedInUser, true));
        }
        else if (CalendarConstants.SAVE_ACTION.equals(action))
        {
            // save from calendar page
            saveCalData(session, request);
        }
        else if (WebAppConstants.USER_ACTION_MODIFY_USER_CONTACT.equals(action))
        {
            // Get the user wrapper off the session manager.
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            UserUtil.extractContactInfoData(request, wrapper);
        }
        else if (WebAppConstants.USER_ACTION_MODIFY_USER.equals(action))
        {
            // Got here from hitting done on roles page.
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            wrapper.saveRoles();
        }
        else if (WebAppConstants.USER_ACTION_CANCEL_LOCALES.equals(action))
        {
            sessionMgr.removeElement("rolesList");
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            wrapper.cancelRoles();
        }
        else if (WebAppConstants.USER_ACTION_MODIFY_USER_PROJECTS
                .equals(action))
        {
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            UserUtil.extractProjectData(request, wrapper);
        }
        else if ("doneSecurity".equals(action))
        {
            FieldSecurity fs = (FieldSecurity) sessionMgr
                    .getAttribute("fieldSecurity");
            UserUtil.extractSecurity(fs, request);
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            wrapper.setFieldSecurity(fs);
        }
        else if ("donePermission".equals(action))
        {
            UserUtil.extractPermissionData(request);
        }
        else if ("doneDefaultRole".equals(action))
        {
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            wrapper.saveDefaultRoles();
        }
        else if ("cancelDefaultRole".equals(action))
        {
            ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                    .getAttribute(MODIFY_USER_WRAPPER);
            wrapper.cancelDefaultRoles();
        }

        String[] companies = UserHandlerHelper.getCompanyNames();
        sessionMgr.setAttribute("companyNames", companies);

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Perform modify user and user calendar.
     */
    private void modifyUserCal(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // Get the user wrapper off the session manager.
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                .getAttribute(MODIFY_USER_WRAPPER);

        // Modify the user's calendar
        UserFluxCalendar cal = (UserFluxCalendar) sessionMgr
                .getAttribute(CalendarConstants.CALENDAR);
        CalendarHelper.modifyUserCalendar(p_request, session, cal);
        sessionMgr.removeElement(CalendarConstants.CALENDAR);
    }

    /**
     * Save the calendar data to the session
     */
    private void saveCalData(HttpSession session, HttpServletRequest request)
            throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        ModifyUserWrapper wrapper = (ModifyUserWrapper) sessionMgr
                .getAttribute(MODIFY_USER_WRAPPER);

        UserFluxCalendar cal = UserUtil.extractCalendarData(request,
                wrapper.getUserId());
        wrapper.setCalendar(cal);
    }
}
