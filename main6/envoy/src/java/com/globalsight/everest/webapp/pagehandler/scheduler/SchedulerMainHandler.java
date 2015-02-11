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
package com.globalsight.everest.webapp.pagehandler.scheduler;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;

import com.globalsight.terminology.scheduler.CronExpression;
import com.globalsight.terminology.scheduler.ITermbaseScheduler;
import com.globalsight.terminology.scheduler.TermbaseReindexExpression;


import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * <p>SchedulerMainHandler is responsible for showing scheduled cron
 * jobs (using Quartz) and creating new jobs.</p>
 *
 * <p>This handler can schedule cron jobs for more than one object type.
 * Currently it is used for the "Reindex Termbase" type.</p>
 */
public class SchedulerMainHandler
    extends PageHandler
{
    private static final Logger s_logger =
        Logger.getLogger(
            SchedulerMainHandler.class);

    //
    // Interface Methods: PageHandler
    //

    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
        HttpServletRequest p_request, HttpServletResponse p_response,
        ServletContext p_context)
        throws ServletException, IOException, EnvoyServletException
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager)session.getAttribute(
            WebAppConstants.SESSION_MANAGER);

        try
        {
            String event = (String)sessionMgr.getAttribute(CRON_EVENT);
            String objectName = (String)sessionMgr.getAttribute(CRON_OBJECT_NAME);
            Long objectId = (Long)sessionMgr.getAttribute(CRON_OBJECT_ID);
            CronExpression schedule =
                (CronExpression)sessionMgr.getAttribute(CRON_SCHEDULE);

            String action = p_request.getParameter(CRON_ACTION);
            if (action == null)
            {
                action = "";
            }

            if (action.equals(CRON_ACTION_LIST))
            {
                String backptr = p_request.getParameter(CRON_BACKPOINTER);
                event = p_request.getParameter(CRON_EVENT);
                objectName = p_request.getParameter(CRON_OBJECT_NAME);
                objectName = EditUtil.utf8ToUnicode(objectName);
                String tmp = p_request.getParameter(CRON_OBJECT_ID);
                objectId = Long.valueOf(tmp);

                schedule = null;

                if (event.equals(CRON_EVENT_REINDEX_TERMBASE))
                {
                    // retrieve TermbaseReindexExpression from database
                    schedule = ServerProxy.getTermbaseScheduler().
                        getEvent(objectId);
                }
                else if (event.equals(CRON_EVENT_REINDEX_TM))
                {
                    // TODO
                }

                // put everything in session manager
                sessionMgr.setAttribute(CRON_EVENT, event);
                sessionMgr.setAttribute(CRON_OBJECT_NAME, objectName);
                sessionMgr.setAttribute(CRON_OBJECT_ID, objectId);
                sessionMgr.setAttribute(CRON_SCHEDULE, schedule);
                sessionMgr.setAttribute(CRON_BACKPOINTER, backptr);
            }
            else if (action.equals(CRON_ACTION_SCHEDULE))
            {
                try
                {
                    if (event.equals(CRON_EVENT_REINDEX_TERMBASE))
                    {
                        schedule = new TermbaseReindexExpression(objectId);
                        readSchedule(p_request, schedule);

                        // cancel previous schedule (removes from db)
                        ServerProxy.getTermbaseScheduler().
                            unscheduleEvent(objectId);

                        // persist and dispatch new schedule
                        ServerProxy.getTermbaseScheduler().
                            scheduleEvent((TermbaseReindexExpression)schedule);
                    }
                    else if (event.equals(CRON_EVENT_REINDEX_TM))
                    {
                        // TODO
                    }
                }
                catch (Throwable ex)
                {
                    // JSP needs to clear this.
                    sessionMgr.setAttribute(CRON_ERROR, ex);
                }

                sessionMgr.setAttribute(CRON_SCHEDULE, schedule);
            }
            else if (action.equals(CRON_ACTION_UNSCHEDULE))
            {
                try
                {
                    if (event.equals(CRON_EVENT_REINDEX_TERMBASE))
                    {
                        // cancel previous schedule (removes from db)
                        ServerProxy.getTermbaseScheduler().
                            unscheduleEvent(objectId);
                    }
                    else if (event.equals(CRON_EVENT_REINDEX_TM))
                    {
                        // TODO
                    }
                }
                catch (Throwable ex)
                {
                    // JSP needs to clear this.
                    sessionMgr.setAttribute(CRON_ERROR, ex);
                }

                sessionMgr.setAttribute(CRON_SCHEDULE, null);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }

        super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
    }


    private void readSchedule(HttpServletRequest p_request, CronExpression p_expr)
    {
        p_expr.setMinutes(p_request.getParameter(CRON_MINUTES));
        p_expr.setHours(p_request.getParameter(CRON_HOURS));
        p_expr.setDaysOfMonth(p_request.getParameter(CRON_DAYSOFMONTH));
        p_expr.setMonths(p_request.getParameter(CRON_MONTHS));
        p_expr.setDaysOfWeek(p_request.getParameter(CRON_DAYSOFWEEK));
        p_expr.setDayOfYear(p_request.getParameter(CRON_DAYOFYEAR));
        p_expr.setWeekOfMonth(p_request.getParameter(CRON_WEEKOFMONTH));
        p_expr.setWeekOfYear(p_request.getParameter(CRON_WEEKOFYEAR));
        p_expr.setYear(p_request.getParameter(CRON_YEAR));
    }
}

