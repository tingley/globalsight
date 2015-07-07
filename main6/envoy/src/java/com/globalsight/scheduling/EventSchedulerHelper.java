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
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.everest.foundation.EmailInformation;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManagerLocal;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.scheduling.FluxEventMapDescriptorModifier;
import com.globalsight.util.GeneralException;
import com.globalsight.util.resourcebundle.LocaleWrapper;

/**
 * EventSchedulerHelper is a helper class with static methods which can be
 * accessed and used from any scheduling related class.
 */

public class EventSchedulerHelper
{
    private static final Logger s_category = Logger
            .getLogger(EventSchedulerHelper.class);

    private static Timestamp s_timestamp = new Timestamp();

    // determines whether the system-wide notification is enabled
    private static boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Static Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Find and return a FluxEventMap object based on the given parameters.
     * 
     * @param p_eventType
     *            - The event type (i.e. accept, complete, or etc.).
     * @param p_domainObjType
     *            - The domain object type.
     * @param p_domainObjId
     *            - The id of the domain object.
     */
    public static FluxEventMap findFluxEventMap(Integer p_eventType,
            Integer p_domainObjType, Long p_domainObjId) throws Exception
    {
        Vector args = new Vector();
        args.add(p_eventType);
        args.add(p_domainObjId);
        args.add(p_domainObjType);

        String sql = FluxEventMapDescriptorModifier.FLUX_EVENT_MAP_SQL;
        Map params = new HashMap();
        params.put("DOID", p_domainObjId);
        params.put("DOT", p_domainObjType);
        params.put("ET", p_eventType);
        List events = HibernateUtil.searchWithSql(sql, params,
                FluxEventMap.class);
        FluxEventMap map = null;
        if (events != null && events.size() > 0)
        {
            map = (FluxEventMap) events.get(0);
        }

        return map;
    }

    /**
     * Find and return a FluxEventMap object based on the eventType,
     * domainObjType, and domainObjId taken from the eventInfo
     * 
     * @param p_eventInfo
     *            HashMap of event information
     * @return FluxEventMap
     */
    public static FluxEventMap findFluxEventMap(HashMap p_eventInfo)
            throws Exception
    {
        Long objectId = (Long) p_eventInfo
                .get(SchedulerConstants.SCHEDULE_DOMAIN_OBJ_ID);
        Integer objectType = (Integer) p_eventInfo
                .get(SchedulerConstants.DOMAIN_OBJ_TYPE);
        Integer eventTypeKey = (Integer) p_eventInfo
                .get(SchedulerConstants.SCHEDULE_EVENT_TYPE);
        FluxEventMap fem = EventSchedulerHelper.findFluxEventMap(eventTypeKey,
                objectType, objectId);
        return fem;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Static Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Static Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Convert the string representation of a locale to Locale object
     * 
     * @param p_localeAsString
     *            - locale as a string (i.e. en_US).
     * @return The Locale object based on the given string.
     */
    static Locale convertToLocale(String p_localeAsString)
    {
        return LocaleWrapper.getLocale(p_localeAsString);
    }

    /**
     * Create a new FluxEventMap object which contains information about a newly
     * created event. This info is used for unscheduling the event.
     * 
     * @param p_fluxEventMap
     *            - The object to be persisted.
     */
    static void createFluxEventMap(FluxEventMap p_fluxEventMap)
            throws Exception
    {
        HibernateUtil.save(p_fluxEventMap);
    }

    /**
     * Determince a final date for a base date and a given duration.
     * 
     * @param p_timeInMilliSec
     *            The base date in milliseconds.
     * @param p_seconds
     *            The duration to be added to that date.
     * @return The final date.
     */
    static Date determineDate(long p_timeInMilliSec, long p_seconds)
    {
        Timestamp ts = new Timestamp();
        ts.setDate(new Date(p_timeInMilliSec));

        // make sure the value is set for the right time unit
        if (p_seconds <= Integer.MAX_VALUE)
        {
            ts.add(Timestamp.SECOND, (int) p_seconds);
        }
        else if ((p_seconds = p_seconds / 60l) <= Integer.MAX_VALUE)
        {
            ts.add(Timestamp.MINUTE, (int) p_seconds);
        }
        else
        {
            ts.add(Timestamp.HOUR, (int) p_seconds / 60);
        }

        return ts.getDate();
    }

    /**
     * Find and return all FluxEventMap objects based on the given parameters.
     * 
     * @param p_domainObjType
     *            - The domain object type.
     * @param p_domainObjId
     *            - The id of the domain object.
     */
    static Iterator findFluxEventMaps(Integer p_domainObjType,
            Long p_domainObjId) throws Exception
    {
        String sql = FluxEventMapDescriptorModifier.FLUX_EVENT_MAPS_SQL;
        Map params = new HashMap();
        params.put("DOID", p_domainObjId);
        params.put("DOT", p_domainObjType);
        List events = HibernateUtil.searchWithSql(sql, params,
                FluxEventMap.class);

        return events.iterator();
    }

    /**
     * Get a source/target locale pair displayed based on the display locale.
     * 
     * @param p_sourceLocale
     *            - The source locale.
     * @param p_targetLocale
     *            - The target locale.
     * @param p_displayLocale
     *            - The locale used for displaying the locale pair.
     * @return The string representation of the locale pair based on the display
     *         locale (i.e. English (United States) / French (France))
     */
    static String localePair(Locale p_sourceLocale, Locale p_targetLocale,
            Locale p_displayLocale)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(p_sourceLocale.getDisplayName(p_displayLocale));
        sb.append(" / ");
        sb.append(p_targetLocale.getDisplayName(p_displayLocale));

        return sb.toString();
    }

    /**
     * Remove an existing FluxEventMap object. This happens when an event is
     * unscheduled.
     * 
     * @param p_fluxEventMap
     *            - The object to be removed.
     */
    static void removeFluxEventMap(FluxEventMap p_fluxEventMap)
            throws Exception
    {
        p_fluxEventMap = HibernateUtil.get(FluxEventMap.class,
                p_fluxEventMap.getEventId());
        try
        {
            HibernateUtil.delete(p_fluxEventMap);
        }
        catch (Exception e)
        {
            String s = e.getMessage();
            if (s.indexOf("unexpected row") < 0)
            {
                throw e;
            }
        }
    }

    /* Notify the Project Manager and other workflow owner(s) (if any) */
    static void notifyProjectManager(Map p_emailInfo, String p_subjectKey,
            String p_messageKey) throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        Long wfId = (Long) p_emailInfo.get(SchedulerConstants.WF_ID);
		Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(
				wfId.longValue());
        WorkflowTemplateInfo wfti = wf.getJob().getL10nProfile()
                .getWorkflowTemplateInfo(wf.getTargetLocale());
        List wfManagerIds = wf
                .getWorkflowOwnerIdsByType(Permission.GROUP_WORKFLOW_MANAGER);
        int size = wfManagerIds.size();
        boolean notifyPm = wfti.notifyProjectManager();

        // Do not send an email if there's no WF manager and the
        // PM has disabled the email option
        if (size == 0 && !notifyPm)
        {
            return;
        }

        Long projectId = (Long) p_emailInfo.get(SchedulerConstants.PROJECT_ID);
        String jobName = (String) p_emailInfo.get(SchedulerConstants.JOB_NAME);
        String activityName = (String) p_emailInfo
                .get(SchedulerConstants.ACTIVITY_NAME);
        String sourceLocale = (String) p_emailInfo
                .get(SchedulerConstants.SOURCE_LOCALE);
        String targetLocale = (String) p_emailInfo
                .get(SchedulerConstants.TARGET_LOCALE);
        Date deadline = (Date) p_emailInfo
                .get(SchedulerConstants.DEADLINE_DATE);

        Project project = ServerProxy.getProjectHandler().getProjectById(
                projectId.longValue());
        String companyId = String.valueOf(project.getCompanyId());
        // convert string representation of a locale to Locale object
        Locale srcLocale = convertToLocale(sourceLocale);
        Locale trgtLocale = convertToLocale(targetLocale);

        // prepare email arguments
        String[] messageArguments = new String[5];
        messageArguments[0] = project.getName();
        messageArguments[1] = jobName;
        messageArguments[2] = activityName;

        if (notifyPm)
        {
            User user = project.getProjectManager();
            sendEmail(user, srcLocale, trgtLocale, deadline, messageArguments,
                    p_subjectKey, p_messageKey, companyId);
        }

        // notify the workflow managers (if any)
        for (int i = 0; i < size; i++)
        {
            User wfMgr = ServerProxy.getUserManager().getUser(
                    (String) wfManagerIds.get(i));
            sendEmail(wfMgr, srcLocale, trgtLocale, deadline, messageArguments,
                    p_subjectKey, p_messageKey, companyId);
        }

    }

    /* Notify the PM that the task overdue for days */
    static void notifyProjectManagerOverdue(Map p_emailInfo,
            String p_subjectKey, String p_messageKey, String p_companyId)
            throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        if (shouldNotifyPm(p_emailInfo))
        {
            sendEmailForOverdue(new User[]
            { getProjectManager(p_emailInfo) }, p_emailInfo, p_subjectKey,
                    p_messageKey, p_companyId);
        }
    }

    /* Notify the LPs the activity overdue (if any) */
    static void notifyProjectUser(Map p_emailInfo, String p_subjectKey,
            String p_messageKey, String p_companyId) throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        String assigneesName = (String) p_emailInfo
                .get(SchedulerConstants.ASSIGNEES_NAME);
        if (assigneesName != null && assigneesName.length() > 0)
        {
            sendEmailForOverdue(toUsers(assigneesName), p_emailInfo,
                    p_subjectKey, p_messageKey, p_companyId);
        }
    }

    private static boolean shouldNotifyPm(Map p_emailInfo)
            throws PersistenceException
    {
        Long wfId = (Long) p_emailInfo.get(SchedulerConstants.WF_ID);
        Workflow wf = null;
        try
        {
			wf = ServerProxy.getWorkflowManager().getWorkflowById(
					wfId.longValue());
	        if (wf == null)
	        {
				throw new PersistenceException("Can't get workflow by id:"
						+ wfId);
	        }
        }
        catch (Exception e)
        {
        	s_category.error(e);
            throw new PersistenceException(e);
        }

        WorkflowTemplateInfo wfti = wf.getJob().getL10nProfile()
                .getWorkflowTemplateInfo(wf.getTargetLocale());
        return wfti.notifyProjectManager();
    }

    /* Gets the PM. */
    private static User getProjectManager(Map p_emailInfo)
            throws RemoteException, ProjectHandlerException, GeneralException,
            NamingException
    {
        Long projectId = (Long) p_emailInfo.get(SchedulerConstants.PROJECT_ID);

        Project project = ServerProxy.getProjectHandler().getProjectById(
                projectId.longValue());
        return project.getProjectManager();
    }

    /* Sends email to users for overdue. */
    private static void sendEmailForOverdue(User[] p_users, Map p_emailInfo,
            String p_subjectKey, String p_messageKey, String p_sendFrom)
            throws Exception
    {
        String sourceLocale = (String) p_emailInfo
                .get(SchedulerConstants.SOURCE_LOCALE);
        String targetLocale = (String) p_emailInfo
                .get(SchedulerConstants.TARGET_LOCALE);
        // convert string representation of a locale to Locale object
        Locale srcLocale = convertToLocale(sourceLocale);
        Locale trgtLocale = convertToLocale(targetLocale);

        Date deadline = (Date) p_emailInfo
                .get(SchedulerConstants.DEADLINE_DATE_COMPLETION);
        for (int i = 0; i < p_users.length; i++)
        {
            sendEmail(p_users[i], srcLocale, trgtLocale, deadline,
                    getArguments(p_emailInfo), p_subjectKey, p_messageKey,
                    p_sendFrom);
        }

    }

    /* Prepares email arguments. */
    private static String[] getArguments(Map p_emailInfo)
            throws PersistenceException, RemoteException,
            ProjectHandlerException, GeneralException, NamingException
    {
        Long wfId = (Long) p_emailInfo.get(SchedulerConstants.WF_ID);
		Workflow wf = ServerProxy.getWorkflowManager().getWorkflowById(
				wfId.longValue());
        if (wf == null)
        {
            throw new PersistenceException("Can't get workflow by Id: " + wfId);
        }

        long jobId = wf.getJob().getJobId();
        String jobName = (String) p_emailInfo.get(SchedulerConstants.JOB_NAME);
        String activityName = (String) p_emailInfo
                .get(SchedulerConstants.ACTIVITY_NAME);

        // Get the overdue days.
        String overduePm = (String) p_emailInfo
                .get(SchedulerConstants.OVERDUE_PM);
        String overdueUser = (String) p_emailInfo
                .get(SchedulerConstants.OVERDUE_USER);
        String assigneesName = (String) p_emailInfo
                .get(SchedulerConstants.ASSIGNEES_NAME);

        Long projectId = (Long) p_emailInfo.get(SchedulerConstants.PROJECT_ID);
        Project project = ServerProxy.getProjectHandler().getProjectById(
                projectId.longValue());

        // prepare email arguments
        String[] messageArguments = new String[9];
        messageArguments[0] = project.getName();
        messageArguments[1] = jobName;
        messageArguments[2] = activityName;
        messageArguments[5] = overduePm;
        messageArguments[6] = overdueUser;
        messageArguments[7] = UserUtil.getUserNamesByIds(assigneesName);
        messageArguments[8] = String.valueOf(jobId);
        return messageArguments;
    }

    /* Gets the users through the assigneesName. */
    private static User[] toUsers(String assigneesName) throws RemoteException,
            UserManagerException, GeneralException
    {
        String[] allAssignee = assigneesName.split(",");
        int assigneeSize = allAssignee.length;
        User[] users = new User[assigneeSize];
        for (int i = 0; i < assigneeSize; i++)
        {
            users[i] = ServerProxy.getUserManager().getUser(allAssignee[i]);
        }
        return users;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Package-scope Static Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Private Methods
    // ////////////////////////////////////////////////////////////////////
    /*
     * Get the time zone from the user's calendar.
     */
    private static TimeZone getUserTimeZone(String p_userId)
    {
        TimeZone timeZone = null;
        try
        {
            timeZone = ServerProxy.getCalendarManager().findUserTimeZone(
                    p_userId);
        }
        catch (Exception e)
        {
            s_category.error("failed to get user time zone. ", e);
            timeZone = TimeZone.getDefault();
        }

        return timeZone;
    }

    /**
     * Format the date based on the given locale.
     * 
     * @param p_date
     *            - The date to be formatted.
     * @param p_displayLocale
     *            - The locale used for formatting the date.
     * @return A formatted date based on the given locale.
     */
    private static String getConvertedTime(Date p_date, Locale p_displayLocale,
            TimeZone p_timeZone)
    {
        s_timestamp.setDate(p_date);
        s_timestamp.setLocale(p_displayLocale);
        s_timestamp.setTimeZone(p_timeZone);
        return s_timestamp.toString();
    }

    /*
     * Notify the responsible PM or WFM.
     */
    private static void sendEmail(User p_user, Locale p_srcLocale,
            Locale p_trgtLocale, Date p_deadline, String[] p_messageArguments,
            String p_subjectKey, String p_messageKey, String p_companyId)
            throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        Locale userLocale = convertToLocale(p_user.getDefaultUILocale());
        p_messageArguments[3] = localePair(p_srcLocale, p_trgtLocale,
                userLocale);
        p_messageArguments[4] = getConvertedTime(p_deadline, userLocale,
                getUserTimeZone(p_user.getUserId()));

        EmailInformation receipt = ServerProxy.getUserManager()
                .getEmailInformationForUser(p_user.getUserId());
        ServerProxy.getMailer().sendMail((EmailInformation) null, receipt,
                p_subjectKey, p_messageKey, p_messageArguments, p_companyId);
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Private Methods
    // ////////////////////////////////////////////////////////////////////
}
