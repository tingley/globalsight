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
package com.globalsight.everest.webapp.pagehandler.administration.calendars;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.comparator.HolidayComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ControlFlowHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.Holiday;
import com.globalsight.util.GeneralException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * This class is the page handler for adding holidays to a
 * calendar.
 */
public class CalHolidayHandler extends PageHandler
    implements HolidayConstants
{
    
    public CalHolidayHandler()
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

        String action = request.getParameter(ACTION);
        
        if (CalendarConstants.CAL_HOLIDAYS_ACTION.equals(action)) 
        {
            setUp(request, session);
        }
        else if (CalendarConstants.SAVE_ACTION.equals(action)) 
        {
            saveHoliday(request, session);
        }

        request.setAttribute(HolidayConstants.HOLIDAY_LIST,
                CalendarHelper.getAllHolidays());

        // Call parent invokePageHandler() to set link beans and invoke JSP
        super.invokePageHandler(pageDescriptor, request,
                                response, context);
    }


    /**
     * Get list of all holidays and the calendar's holidays.
     * Save the page's values to the session, so after the user
     * adds holidays, any other values they may have set are
     * retained.
     */
    private void setUp(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {

        SessionManager sessionMgr =
            (SessionManager)session.getAttribute(SESSION_MANAGER);
        FluxCalendar cal = (FluxCalendar)
             sessionMgr.getAttribute(CalendarConstants.CALENDAR);
        if (cal == null)
        {
            cal = new FluxCalendar();
        }
        CalendarHelper.updateCalFields(request, cal);
        // get the list of holidays.  If they have been going back and
        // forth between creating holidays and adding/removing them, then
        // the list contains state.  Remove the elements that have "removed"
        // state from the list.
        ArrayList list = (ArrayList)cal.getHolidaysList();
        ArrayList newlist = new ArrayList();
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++) 
            {
                Holiday holiday = (Holiday)list.get(i);
                if (holiday.getCalendarAssociationState() != 
                    com.globalsight.calendar.CalendarConstants.DELETED)
                {
                    newlist.add(holiday);
                }
            }
        }

        sessionMgr.setAttribute(HolidayConstants.CAL_HOLIDAY_LIST, newlist);
        sessionMgr.setAttribute(CalendarConstants.CALENDAR, cal);
    }


    /**
     * Save the new holiday
     */
    private void saveHoliday(HttpServletRequest request, HttpSession session)
    throws EnvoyServletException
    {
        CalendarHelper.createHoliday(request, session);
    }

}
