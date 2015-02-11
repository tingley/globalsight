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
package com.globalsight.everest.webapp.pagehandler.tasks;

// globalsight
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.config.UserParamNames;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.util.edit.EditUtil;
 
//java
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Provides the functionality of saving user's account information
 * and routing to the correct control flow.
 */
public class AccountInfoHandler extends PageHandler
{
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
        String action = request.getParameter(CalendarConstants.ACTION);
        String taskaction = request.getParameter(TASK_ACTION);

        if (CalendarConstants.SAVE_ACTION.equals(action))
        {
            SessionManager sessionMgr =
                (SessionManager)session.getAttribute(SESSION_MANAGER);
            UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
            CalendarHelper.updateUserCalFields(request, cal);
        }
        else if (TASK_ACTION_MODIFY_ACCOUNT.equals(taskaction))
        {
            SessionManager sessionMgr = (SessionManager)
                    session.getAttribute(SESSION_MANAGER);

            modifyAccount(request, session);
        }
        else if (USER_ACTION_MODIFY_USER_CONTACT.equals(action))
        {
            saveContactInformation(request, session);
        }
        else if ("notificationOptions".equals(action))
        {
            saveOptions(session, request);
        }
        else 
        {
            SessionManager sessionMgr = (SessionManager)session.
                getAttribute(SESSION_MANAGER);
            String[] companies = UserHandlerHelper.getCompanyNames();
            sessionMgr.setAttribute("companyNames", companies);

            User user = (User)sessionMgr.getAttribute("myAccountUser");
            if (user == null)
            {
                User curUser = (User)sessionMgr.getAttribute(USER);
                user = UserHandlerHelper.getUser(curUser.getUserId());
                sessionMgr.setAttribute("myAccountUser", user);
            }
        }
        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }

    /**
     * Update the details of the given user from inputs supplied on
     * the request.
     */
    private void modifyAccount(HttpServletRequest p_request,
        HttpSession p_session)
        throws EnvoyServletException
    {
        TaskHelper.saveBasicInformation(p_session, p_request);

        TaskHelper.modifyUserAccount(p_session);
        String uiLocaleString = p_request.getParameter(USER_UI_LOCALE);

        Locale uiLocale = PageHandler.getUILocale(uiLocaleString);
        // only use ui locale from the cookie
        // Error: p_request.getParameter(USER_UI_LOCALE) is always en_US
        // It is hardcode in accountInfo.jsp
        // p_session.setAttribute(UILOCALE, uiLocale);

        // If the calendar was modified too, save it.
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);
        UserFluxCalendar cal = (UserFluxCalendar)
                sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        if (cal != null)
        {
            CalendarHelper.modifyUserCalendar(p_session, cal);
        }

        // If account options were modified, save those too
        HashMap optionsHash = (HashMap) sessionMgr.getAttribute("optionsHash");
        if (optionsHash != null)
        {
            AccountOptionsHelper.modifyOptions(p_session, p_request, optionsHash);
        }
    }

    /*
     * Save the contact information
     */
    private void saveContactInformation(HttpServletRequest p_request,
        HttpSession p_session)
        throws EnvoyServletException
    {
        SessionManager sessionMgr = (SessionManager)p_session.
            getAttribute(WebAppConstants.SESSION_MANAGER);

        User user = (User)sessionMgr.getAttribute("myAccountUser");
        String address = EditUtil.utf8ToUnicode(
            p_request.getParameter("address"));
        String homePhone = EditUtil.utf8ToUnicode(
            p_request.getParameter("homePhone"));
        String workPhone = EditUtil.utf8ToUnicode(
            p_request.getParameter("workPhone"));
        String cellPhone = EditUtil.utf8ToUnicode(
            p_request.getParameter("cellPhone"));
        String fax = EditUtil.utf8ToUnicode(
            p_request.getParameter("fax"));
        String email = EditUtil.utf8ToUnicode(
            p_request.getParameter("email"));
        String ccEmail = EditUtil.utf8ToUnicode(
                p_request.getParameter("ccEmail"));
        String bccEmail = EditUtil.utf8ToUnicode(
                p_request.getParameter("bccEmail"));
        String regionalPref = EditUtil.utf8ToUnicode(
            p_request.getParameter("uiLocale"));

        user.setAddress(address);
        user.setHomePhoneNumber(homePhone);
        user.setOfficePhoneNumber(workPhone);
        user.setCellPhoneNumber(cellPhone);
        user.setFaxPhoneNumber(fax);
        user.setEmail(email);
        user.setCCEmail(ccEmail);
        user.setBCCEmail(bccEmail);
        user.setDefaultUILocale(regionalPref);

        sessionMgr.setAttribute("myAccountUser", user);
    }


    /**
     * Clear the session manager
     *
     * @param p_session - The client's HttpSession where the
     * session manager is stored.
     */
    private void clearSessionManager(HttpSession p_session)
    {
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);

        sessionMgr.clear();
    }

    /**
     * Save the notification options
     */
    private void saveOptions(HttpSession p_session,
                             HttpServletRequest p_request)
        throws EnvoyServletException
    {
        SessionManager sessionMgr =
            (SessionManager)p_session.getAttribute(SESSION_MANAGER);

        String toField = (String)p_request.getParameter("toField");
        List addedOptions = new ArrayList();
        if (toField != null && !toField.equals(""))
        {
            String[] values = toField.split(",");
            for (int i=0; i < values.length; i++)
            {
                addedOptions.add(values[i]);
            }
        }
        // update the added options in the session manager.
        sessionMgr.setAttribute(
            WebAppConstants.ADDED_NOTIFICATION_OPTIONS, addedOptions);

        // now check for the options hash map
        HashMap optionsHash = (HashMap)sessionMgr.getAttribute("optionsHash");
        if (optionsHash == null)
        {
            optionsHash = new HashMap();
            sessionMgr.setAttribute("optionsHash", optionsHash);
        }

        String enabled = p_request.getParameter(
            UserParamNames.NOTIFICATION_ENABLED) == null ? "0" : "1";
        // Store the main notification flag
        optionsHash.put(UserParamNames.NOTIFICATION_ENABLED, enabled);
        // now let's set the other notification options
        List availableOptions = (List)sessionMgr.getAttribute(
            WebAppConstants.AVAILABLE_NOTIFICATION_OPTIONS);
        int size = availableOptions.size();
        for (int i = 0; i < size; i++)
        {
            String option = (String)availableOptions.get(i);
            String value = addedOptions.contains(option) ? "1" : "0";
            optionsHash.put(option, value);
        }
    }
}
