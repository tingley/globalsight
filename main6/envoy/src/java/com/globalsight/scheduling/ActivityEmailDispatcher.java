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

package com.globalsight.scheduling;

//GlobalSight
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * ActivityEmailDispatcher is responsible for sending warning/deadline emails to
 * a Project Manager. The eventFired method is invoked through Quartz when the
 * time for notification is reached.
 */

public class ActivityEmailDispatcher extends EventHandler

{
    // for logging purposes
    private static final Logger s_category = Logger
            .getLogger(ActivityEmailDispatcher.class.getName());

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    public ActivityEmailDispatcher()
    {
        super();
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Implementation of the Abstract Method
    // ////////////////////////////////////////////////////////////////////
    /**
     * This method is called when a scheduled event is fired. Subclasses must
     * implement this method in order to obtain the desired behavior at fire
     * time.
     * 
     * @param p_fireDate
     *            the date/time when the event fired.
     * @param p_event
     *            the event itself.
     * 
     * @throws EventHandlerException
     *             if any error occurs.
     */
    public void eventFired(KeyFlowContext p_flowContext)
            throws EventHandlerException
    {
        try
        {
            int eventKey = ((Integer) p_flowContext
                    .get(SchedulerConstants.EVENT_KEY)).intValue();

            EventInfo myKey = (EventInfo) p_flowContext.getKey();
            // modify the integer value (toggle during warning/deadline events)
            if (eventKey == INITIAL)
            {
                // reset the toggle key
                p_flowContext.put(SchedulerConstants.EVENT_KEY, new Integer(0));

                if (myKey.getMap().get(SchedulerConstants.OVERDUE) == null)
                {
                    notifyProjectManager(
                            SchedulerConstants.DEADLINE_APPROACH_SUBJECT,
                            SchedulerConstants.DEADLINE_APPROACH_BODY,
                            myKey.getMap());
                }
                else
                {
                    // Sends "notify user overdue" email.
                    notifyProjectUser(
                            SchedulerConstants.NOTIFY_USER_OVERDUE_SUBJECT,
                            SchedulerConstants.NOTIFY_USER_OVERDUE_BODY,
                            myKey.getMap());
                }
            }
            else
            {

                if (myKey.getMap().get(SchedulerConstants.OVERDUE) == null)
                {
                    notifyProjectManager(
                            SchedulerConstants.DEADLINE_PASSED_SUBJECT,
                            SchedulerConstants.DEADLINE_PASSED_BODY,
                            myKey.getMap());
                }
                else
                {
                    // Sends the "notify pm overdue" email.
                    notifyProjectManagerOverdue(
                            SchedulerConstants.NOTIFY_PM_OVERDUE_SUBJECT,
                            SchedulerConstants.NOTIFY_PM_OVERDUE_BODY,
                            myKey.getMap());
                }
            }
        }
        catch (Exception e)
        {
            s_category
                    .error("Failed to notify Project Manager about a warning/deadline notification.",
                            e);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Implementation of the Abstract Method
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Local Private Methods
    // ////////////////////////////////////////////////////////////////////
    // This method notifies the PM that the acceptance/completion time of a task
    // is either approaching or passed.
    private void notifyProjectManager(String p_subjectSuffix,
            String p_messageSuffix, HashMap p_emailInfo) throws Exception
    {
        String prefix = (String) p_emailInfo.get(SchedulerConstants.EVENT_TYPE);
        EventSchedulerHelper.notifyProjectManager(p_emailInfo, prefix
                + p_subjectSuffix, prefix + p_messageSuffix);
    }

    // This method notifies the PM that when the acceptance/completion time of
    // a task over the deadline for days.
    private void notifyProjectManagerOverdue(String p_subjectSuffix,
            String p_messageSuffix, HashMap p_emailInfo) throws Exception
    {
        String prefix = (String) p_emailInfo.get(SchedulerConstants.EVENT_TYPE);
        long projId = (Long) p_emailInfo.get(SchedulerConstants.PROJECT_ID);
        Project proj = ServerProxy.getProjectHandler().getProjectById(projId);
        if (proj == null)
        {
            s_category
                    .info("Not found the project in the system. Ignoring this request.");
            return;
        }
        String companyId = String.valueOf(proj.getCompanyId());
        Company c = CompanyWrapper.getCompanyById(companyId);
        if (c == null)
        {
            // the company does not exist in database any more, ignore this
            // request. Should be because the company has been deleted.
            s_category
                    .info("Not found the company in the system. Ignoring this request.");
            return;
        }
        EventSchedulerHelper.notifyProjectManagerOverdue(p_emailInfo, prefix
                + p_subjectSuffix, prefix + p_messageSuffix, companyId);
    }

    // This method notifies the task user that when the acceptance/completion
    // time of
    // a task over the deadline for days.
    private void notifyProjectUser(String p_subjectSuffix,
            String p_messageSuffix, HashMap p_emailInfo) throws Exception
    {
        String prefix = (String) p_emailInfo.get(SchedulerConstants.EVENT_TYPE);
        long projId = (Long) p_emailInfo.get(SchedulerConstants.PROJECT_ID);
        Project proj = ServerProxy.getProjectHandler().getProjectById(projId);
        if (proj == null)
        {
            s_category
                    .info("Not found the project in the system. Ignoring this request.");
            return;
        }
        String companyId = String.valueOf(proj.getCompanyId());
        Company c = CompanyWrapper.getCompanyById(companyId);
        if (c == null)
        {
            // the company does not exist in database any more, ignore this
            // request. Should be because the company has been deleted.
            s_category
                    .info("Not found the company in the system. Ignoring this request.");
            return;
        }
        EventSchedulerHelper.notifyProjectUser(p_emailInfo, prefix
                + p_subjectSuffix, prefix + p_messageSuffix, companyId);
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Local Private Methods
    // ////////////////////////////////////////////////////////////////////
}
