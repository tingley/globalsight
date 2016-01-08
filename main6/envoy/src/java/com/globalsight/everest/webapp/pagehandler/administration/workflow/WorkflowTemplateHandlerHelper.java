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

//GlobalSight
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.dom4j.Document;

import com.globalsight.calendar.CalendarManagerLocal;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.usermgr.UserManagerException;
import com.globalsight.everest.util.comparator.ActivityComparator;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.comparator.LocalePairComparator;
import com.globalsight.everest.util.comparator.ProjectComparator;
import com.globalsight.everest.util.comparator.UserComparator;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.workflow.SystemAction;
import com.globalsight.everest.workflow.WorkflowOwners;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.log.OperationLog;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class WorkflowTemplateHandlerHelper
{
    private static Boolean m_areAndOrNodesVisible = null;

    /**
     * Get a list of all existing activities in the system.
     * <p>
     * 
     * @exception EnvoyServletException
     *                Component related exception.
     * @exception GeneralException
     */
    static Vector getAllActivities(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            Vector activities = vectorizedCollection(ServerProxy
                    .getJobHandler().getAllActivities());
            SortUtil.sort(activities, new ActivityComparator(
                    ActivityComparator.NAME, p_locale));

            return activities;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    static Vector getAllDtpActivities(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            Vector activities = vectorizedCollection(ServerProxy
                    .getJobHandler().getAllDtpActivities());
            SortUtil.sort(activities, new ActivityComparator(
                    ActivityComparator.NAME, p_locale));

            return activities;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    static Vector getAllTransActivities(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            Vector activities = vectorizedCollection(ServerProxy
                    .getJobHandler().getAllTransActivities());
            SortUtil.sort(activities, new ActivityComparator(
                    ActivityComparator.NAME, p_locale));

            return activities;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get all the code sets from locale manager.
     * <p>
     * 
     * @exception EnvoyServletException
     */
    public static List getAllCodeSets() throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getAllCodeSets();
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /*
     * Get all the source locales from locale manager. <p>
     * 
     * @exception EnvoyServletException Component related exception.
     * 
     * @exception GeneralException
     */
    public static List getAllSourceLocales(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllSourceLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, p_locale);
            SortUtil.sort(al, comp);
            return al;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /*
     * Get all the target locales from locale manager. <p>
     * 
     * @exception EnvoyServletException Component related exception.
     * 
     * @exception GeneralException
     */
    public static List getAllTargetLocales(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getAllTargetLocales());
            GlobalSightLocaleComparator comp = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, p_locale);
            SortUtil.sort(al, comp);
            return al;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(GeneralException.EX_GENERAL, e);
        }
    }

    /**
     * Get a list of all locale pairs.
     */
    public static List getAllLocalePairs(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            ArrayList al = new ArrayList(ServerProxy.getLocaleManager()
                    .getSourceTargetLocalePairs());
            LocalePairComparator comp = new LocalePairComparator(p_locale);
            SortUtil.sort(al, comp);
            return al;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of all projects
     */
    public static List getAllProjectInfos(Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            List projects = ServerProxy.getProjectHandler()
                    .getAllProjectInfosForGUI();
            ProjectComparator uc = new ProjectComparator(p_locale);
            SortUtil.sort(projects, uc);
            return projects;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of projects that the specified user (i.e. PM) manages them
     */
    public static List getAllProjectInfosForUser(User user, Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            List projects = ServerProxy.getProjectHandler()
                    .getProjectInfosManagedByUser(user,
                            Permission.GROUP_MODULE_GLOBALSIGHT);

            if (projects != null && projects.size() > 1)
            {
                ProjectComparator uc = new ProjectComparator(p_locale);
                SortUtil.sort(projects, uc);
            }

            return projects;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the project info of the projects this user is part of.
     */
    public static List getProjectInfosByUser(String p_userId, Locale p_locale)
            throws EnvoyServletException
    {
        try
        {
            List projects = ServerProxy.getProjectHandler()
                    .getProjectInfosByUser(p_userId);

            // only sort if the list is not null (and has more then 1 entry)
            if (projects != null && projects.size() > 1)
            {
                ProjectComparator uc = new ProjectComparator(p_locale);
                SortUtil.sort(projects, uc);
            }

            return projects;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the project identified by the id.
     */
    static Project getProjectById(long p_projectId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectById(p_projectId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a map of all workflow managers in a given list of projects
     */
    static HashMap getAllWorkflowManagersInProject()
            throws EnvoyServletException
    {
        try
        {
            // query out all the people that can manage projects
            Collection usernames = Permission.getPermissionManager()
                    .getAllUsersWithPermission(
                            Permission.PROJECTS_MANAGE_WORKFLOWS);
            Iterator iter = usernames.iterator();
            ArrayList users = new ArrayList();
            while (iter.hasNext())
            {
                String userid = (String) iter.next();
                User u = null;
                try
                {
                    u = ServerProxy.getUserManager().getUser(userid);
                }
                catch (UserManagerException ume)
                {
                    // do nothing
                }
                if (u != null)
                {
                    users.add(u);
                }
            }

            return ServerProxy.getProjectHandler().getProjectsWithUsers(users);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a collection of existing active worklfow template infos.
     */
    public static List getAllWorkflowTemplateInfos() throws EnvoyServletException
    {
        try
        {
            return new ArrayList(ServerProxy.getProjectHandler()
                    .getAllWorkflowTemplateInfos());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns the container role based on the given activity name, source
     * locale, and target locale.
     * <p>
     * 
     * @param p_activity
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the source Locale.toString().
     * @param p_targetLocale
     *            The results of the target Locale.toString().
     * @return The container role.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static ContainerRole getContainerRole(String p_activity,
            String p_sourceLocale, String p_targetLocale, long p_projectId)
            throws EnvoyServletException
    {
        try
        {
            ContainerRole role = ServerProxy.getUserManager().getContainerRole(
                    p_activity, p_sourceLocale, p_targetLocale, p_projectId);

            return (role != null && role.isContainerRoleValid()) ? role : null;
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a LocalePair object based on a given id.
     */
    static LocalePair getLocalePairById(long p_lpId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager().getLocalePairById(p_lpId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a locale pair object based on a source and target locale ids.
     */
    static LocalePair getLocalePairBySourceTargetIds(long p_sourceLocaleId,
            long p_targetLocaleId) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getLocaleManager()
                    .getLocalePairBySourceTargetIds(p_sourceLocaleId,
                            p_targetLocaleId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of all rates for a particular source/target locale.
     * 
     * @see CostingEngine.getRates(GlobalSightLocale, GlobalSightLocale) to see
     *      whatthe hashtable contains.
     *      <p>
     * @exception EnvoyServletException
     *                Component related exception.
     */
    static Hashtable getRatesForLocale(GlobalSightLocale p_source,
            GlobalSightLocale p_target) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getCostingEngine().getRates(p_source, p_target);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get the SessionManager object for this session.
     */
    static SessionManager getSessionManager(HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        return (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);
    }

    /**
     * Get user matched the given uid
     * 
     * @param p_uid
     *            - The user id
     * @return a User object
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static User getUser(String p_uid) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUser(p_uid);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns the Collection of all UserRole objects with the given activity
     * name, source locale, and target locale.
     * <p>
     * 
     * @param p_activty
     *            The results of the Activity.getActivityName() method.
     * @param p_sourceLocale
     *            The results of the source Locale.toString().
     * @param p_targetLocale
     *            The results of the target Locale.toString().
     * @return Collection of all UserRole objects.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Collection getUserRoles(String p_activity,
            String p_sourceLocale, String p_targetLocale)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getUserManager().getUserRoles(p_activity,
                    p_sourceLocale, p_targetLocale);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a workflow template by the given id.
     */
    public static WorkflowTemplate getWorkflowTemplateById(long p_wfTemplateId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowServer().getWorkflowTemplateById(
                    p_wfTemplateId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Given a user, start date, and duration, determine an estimated completion
     * date.
     */
    public static Date getEstimatedCompletionDate(String userId,
            Date startDate, long duration) throws EnvoyServletException
    {
        try
        {
            UserFluxCalendar cal = CalendarHelper
                    .getUserCalendarByOwner(userId);
            return ServerProxy.getEventScheduler().determineDate(startDate,
                    cal, duration);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a "Task" object by id.
     */
    public static Task getTaskById(long taskId) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getTaskManager().getTask(taskId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a "WorkflowTemplateInfo" object by id. Note that this object does not
     * contain "WorkflowTemplate" (for performance purposes).
     */
    static WorkflowTemplateInfo getWorkflowTemplateInfoById(long p_wfId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getWorkflowTemplateInfoById(
                    p_wfId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /*
     * Determines whether the system-wide AND/OR nodes shoule be visible
     */
    static Boolean areAndOrNodesEnabled()
    {
        if (m_areAndOrNodesVisible == null)
        {
            try
            {
                SystemConfiguration sc = SystemConfiguration.getInstance();
                m_areAndOrNodesVisible = new Boolean(
                        sc.getStringParameter(sc.AND_OR_NODES));
            }
            catch (Exception ge)
            {
                // assumes AND/OR should be invisible
                m_areAndOrNodesVisible = Boolean.FALSE;
            }
        }

        return m_areAndOrNodesVisible;
    }

    /**
     * Determines whether the system-wide costing feature is enables
     */
    static boolean isCostingEnabled()
    {
        boolean costingEnabled = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            costingEnabled = sc.getBooleanParameter(sc.COSTING_ENABLED);
        }
        catch (Exception ge)
        {
            // assumes costing is disabled.
        }
        return costingEnabled;
    }

    /**
     * Determines whether the system-wide revenue feature is enables
     */
    static boolean isRevenueEnabled()
    {
        boolean revenueEnabled = false;
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            revenueEnabled = sc.getBooleanParameter(sc.REVENUE_ENABLED);
        }
        catch (Exception ge)
        {
            // assumes revenue is disabled.
        }
        return revenueEnabled;
    }

    /* Convert the given string into a long value; if null, or an error */
    /* occurs, return the default value instead (always -1) */
    static long parseLong(String p_string)
    {
        long longValue = -1;
        if (p_string != null)
        {
            try
            {
                longValue = Long.parseLong(p_string);
            }
            catch (NumberFormatException e)
            {
            }
        }
        return longValue;
    }

    static void duplicateWorkflowTemplateInfo(long p_wfId,
            ArrayList p_localePairs, String newName, Project project,
            ResourceBundle p_resourceBundle) throws EnvoyServletException
    {
        try
        {
            WorkflowTemplateInfo wftInfo = getWorkflowTemplateInfoById(p_wfId);
            WorkflowTemplate workflowTemplate = getWorkflowTemplateById(wftInfo
                    .getWorkflowTemplateId());

            String displayRoleName = p_resourceBundle
                    .getString("lb_all_qualified_users");
            ServerProxy.getProjectHandler().setResourceBundle(p_resourceBundle);
            ServerProxy.getProjectHandler().duplicateWorkflowTemplates(p_wfId,
                    newName, project, workflowTemplate, p_localePairs,
                    displayRoleName);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    static void importWorkflowTemplateInfo(Document doc,
            ArrayList<LocalePair> p_localePairs, String newName,
            String projectId, ResourceBundle p_resourceBundle)
            throws EnvoyServletException
    {
        try
        {
            String displayRoleName = p_resourceBundle
                    .getString("lb_all_qualified_users");
            ServerProxy.getProjectHandler().setResourceBundle(p_resourceBundle);
            ServerProxy.getProjectHandler().importWorkflowTemplates(doc,
                    newName, p_localePairs, displayRoleName, projectId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    // first save the iflow template and then the workflow template info...
    static void saveWorkflowTemplateInfo(WorkflowTemplateInfo p_wfti,
            WorkflowTemplate p_wfTemplate, String m_userId) throws EnvoyServletException
    {
        try
        {
            String[] wfManagerIds = new String[p_wfti.getWorkflowManagerIds()
                    .size()];
            wfManagerIds = (String[]) p_wfti.getWorkflowManagerIds().toArray(
                    wfManagerIds);
            if (p_wfti.getId() == -1)
            {
                WorkflowTemplate wft = ServerProxy.getWorkflowServer()
                        .createWorkflowTemplate(
                                p_wfTemplate,
                                new WorkflowOwners(
                                        p_wfti.getProjectManagerId(),
                                        wfManagerIds));

                // set the object with the created id (will set the id within
                // wfti).
                p_wfti.setWorkflowTemplate(wft);

                ServerProxy.getProjectHandler().createWorkflowTemplateInfo(
                        p_wfti);
                OperationLog.log(m_userId, OperationLog.EVENT_ADD,
                        OperationLog.COMPONET_WORKFLOW, p_wfti.getName());
            }
            else
            {
                // long oldTemplateId = p_wfTemplate.getId();
                // modify is basically creating a new template in i-Flow db
                // due to restriction of template modification (can not really
                // modify a template that has an associated process instance).
                WorkflowTemplate wft = ServerProxy.getWorkflowServer()
                        .modifyWorkflowTemplate(
                                p_wfTemplate,
                                new WorkflowOwners(
                                        p_wfti.getProjectManagerId(),
                                        wfManagerIds));

                // set the object with the created id (will set the id within
                // wfti).
                p_wfti.setWorkflowTemplate(wft);

                ServerProxy.getProjectHandler().modifyWorkflowTemplate(p_wfti);
                OperationLog.log(m_userId, OperationLog.EVENT_EDIT,
                        OperationLog.COMPONET_WORKFLOW, p_wfti.getName());
                // now try removing the old template (to cleanup template table)
                // removeWorkflowTemplate(oldTemplateId);
            }
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns a sorted list of users who have the given permission
     * 
     * @param p_permission
     * @see Permission
     * @param p_locale
     *            locale
     * @return ArrayList of User
     * @exception Exception
     */
    private static ArrayList getUsersByPermission(String p_permission,
            Locale p_locale) throws Exception
    {
        // query out all the people that have this permission
        Collection usernames = Permission.getPermissionManager()
                .getAllUsersWithPermission(p_permission);
        Iterator iter = usernames.iterator();
        ArrayList users = new ArrayList();
        while (iter.hasNext())
        {
            String userid = (String) iter.next();
            User u = ServerProxy.getUserManager().getUser(userid);
            users.add(u);
        }
        UserComparator uc = new UserComparator(UserComparator.DISPLAYNAME,
                p_locale);
        SortUtil.sort(users, uc);
        return users;
    }

    // convert a collection to a vector.
    private static Vector vectorizedCollection(Collection p_collection)
    {
        return new Vector(p_collection);
    }

    static Hashtable getDataForDialog(ResourceBundle bundle, Locale p_locale,
            WorkflowTemplateInfo p_wfti) throws EnvoyServletException
    {
        // return getDataForDialog(bundle, p_locale, p_wfti.getSourceLocale(),
        // p_wfti.getTargetLocale());
        boolean hasCosting = isCostingEnabled();
        boolean hasRevenue = isRevenueEnabled();
        boolean isCalendarInstalled = CalendarManagerLocal.isInstalled();
        // Start Dialog data
        Hashtable hashtable = new Hashtable();
        hashtable.put(WorkflowTemplateConstants.LABELS,
                dialogLabels(bundle, isCalendarInstalled));
        hashtable.put(WorkflowTemplateConstants.BTN_LABELS,
                dialogButtons(bundle));
        hashtable.put(WorkflowTemplateConstants.MESSAGES, messages(bundle));
        hashtable.put(WorkflowTemplateConstants.JOB_COSTING_ENABLED,
                new Boolean(hasCosting));
        hashtable.put(WorkflowTemplateConstants.JOB_REVENUE_ENABLED,
                new Boolean(hasRevenue));
        hashtable.put("isCalendarInstalled", new Boolean(isCalendarInstalled));

        // hashtable.put(WorkflowTemplateConstants.ACTIVITIES,
        // getAllActivities(p_locale));
        if (WorkflowTemplateInfo.TYPE_DTP.equals(p_wfti.getWorkflowType()))
        {
            hashtable.put(WorkflowTemplateConstants.ACTIVITIES,
                    getAllDtpActivities(p_locale));
        }
        else
        {
            hashtable.put(WorkflowTemplateConstants.ACTIVITIES,
                    getAllTransActivities(p_locale));
        }
        hashtable.put(WorkflowTemplateConstants.SYSTEM_ACTION,
                systemActions(bundle));

        if (hasCosting)
        {
            hashtable.put(
                    WorkflowTemplateConstants.RATES,
                    getRatesForLocale(p_wfti.getSourceLocale(),
                            p_wfti.getTargetLocale()));
        }

        // for applet i18n issue fixing
        hashtable.put(WorkflowTemplateConstants.I18N_CONTENT,
                getI18NContents(bundle));

        return hashtable;
    }

    // get the data required for the activity dialog
    public static Hashtable getWorkflowDetailData(ResourceBundle bundle, Locale p_locale,
            GlobalSightLocale p_srcLocale, GlobalSightLocale p_targetLocale)
            throws EnvoyServletException
    {
        boolean hasCosting = isCostingEnabled();
        boolean hasRevenue = isRevenueEnabled();
        boolean isCalendarInstalled = CalendarManagerLocal.isInstalled();
        // Start Dialog data
        Hashtable hashtable = new Hashtable();
        hashtable.put(WorkflowTemplateConstants.JOB_COSTING_ENABLED,
                new Boolean(hasCosting));
        hashtable.put(WorkflowTemplateConstants.JOB_REVENUE_ENABLED,
                new Boolean(hasRevenue));
        hashtable.put("isCalendarInstalled", new Boolean(isCalendarInstalled));
        hashtable.put(WorkflowTemplateConstants.ACTIVITIES,
                getAllActivities(p_locale));
        hashtable.put(WorkflowTemplateConstants.SYSTEM_ACTION,
                systemActions(bundle));

        if (hasCosting)
        {
            hashtable.put(WorkflowTemplateConstants.RATES,
                    getRatesForLocale(p_srcLocale, p_targetLocale));
        }

        hashtable.put("templateSource", p_srcLocale);
        hashtable.put("templateTarget", p_targetLocale);

        return hashtable;
    }
    
    // get the data required for the activity dialog
    public static Hashtable getDataForDialog(ResourceBundle bundle, Locale p_locale,
            GlobalSightLocale p_srcLocale, GlobalSightLocale p_targetLocale)
            throws EnvoyServletException
    {
        boolean hasCosting = isCostingEnabled();
        boolean hasRevenue = isRevenueEnabled();
        boolean isCalendarInstalled = CalendarManagerLocal.isInstalled();
        // Start Dialog data
        Hashtable hashtable = new Hashtable();
        hashtable.put(WorkflowTemplateConstants.LABELS,
                dialogLabels(bundle, isCalendarInstalled));
        hashtable.put(WorkflowTemplateConstants.BTN_LABELS,
                dialogButtons(bundle));
        hashtable.put(WorkflowTemplateConstants.MESSAGES, messages(bundle));
        hashtable.put(WorkflowTemplateConstants.JOB_COSTING_ENABLED,
                new Boolean(hasCosting));
        hashtable.put(WorkflowTemplateConstants.JOB_REVENUE_ENABLED,
                new Boolean(hasRevenue));
        hashtable.put("isCalendarInstalled", new Boolean(isCalendarInstalled));
        hashtable.put(WorkflowTemplateConstants.ACTIVITIES,
                getAllActivities(p_locale));
        hashtable.put(WorkflowTemplateConstants.SYSTEM_ACTION,
                systemActions(bundle));

        if (hasCosting)
        {
            hashtable.put(WorkflowTemplateConstants.RATES,
                    getRatesForLocale(p_srcLocale, p_targetLocale));
        }

        // for applet i18n issue fixing
        hashtable.put(WorkflowTemplateConstants.I18N_CONTENT,
                getI18NContents(bundle));
        hashtable.put("templateSource", p_srcLocale);
        hashtable.put("templateTarget", p_targetLocale);

        return hashtable;
    }

    /**
     * Init i18n contents for applet
     * 
     * @param p_bundle
     *            {@code ResourceBundle} base on ui locale
     * @return
     */
    private static Hashtable getI18NContents(ResourceBundle p_bundle)
    {
        Hashtable<String, String> m_i18nContents = new Hashtable<String, String>();

        setI18nContent(m_i18nContents, p_bundle, "lb_action");
        setI18nContent(m_i18nContents, p_bundle, "lb_activity_node");
        setI18nContent(m_i18nContents, p_bundle, "lb_and_node");
        setI18nContent(m_i18nContents, p_bundle, "lb_and_node_dialog");
        setI18nContent(m_i18nContents, p_bundle, "lb_and_node_properties");
        setI18nContent(m_i18nContents, p_bundle, "lb_arrow");
        setI18nContent(m_i18nContents, p_bundle, "lb_browse_d");
        setI18nContent(m_i18nContents, p_bundle, "lb_close");
        setI18nContent(m_i18nContents, p_bundle, "lb_cancel");
        setI18nContent(m_i18nContents, p_bundle, "lb_condition_node");
        setI18nContent(m_i18nContents, p_bundle,
                "lb_conditional_node_properties");
        setI18nContent(m_i18nContents, p_bundle, "lb_description_c");
        setI18nContent(m_i18nContents, p_bundle, "lb_decisions");
        setI18nContent(m_i18nContents, p_bundle, "lb_doc_title");
        setI18nContent(m_i18nContents, p_bundle, "lb_down");
        setI18nContent(m_i18nContents, p_bundle, "lb_email_address_c");
        setI18nContent(m_i18nContents, p_bundle, "lb_enter_script_name");
        setI18nContent(m_i18nContents, p_bundle, "lb_epilogue_c");
        setI18nContent(m_i18nContents, p_bundle, "lb_exit");
        setI18nContent(m_i18nContents, p_bundle, "lb_exit_node");
        setI18nContent(m_i18nContents, p_bundle, "lb_file");
        setI18nContent(m_i18nContents, p_bundle, "lb_general");
        setI18nContent(m_i18nContents, p_bundle, "lb_if");
        setI18nContent(m_i18nContents, p_bundle, "lb_is");
        setI18nContent(m_i18nContents, p_bundle, "lb_name_c");
        setI18nContent(m_i18nContents, p_bundle, "lb_notification");
        setI18nContent(m_i18nContents, p_bundle, "lb_ok");
        setI18nContent(m_i18nContents, p_bundle, "lb_or_node");
        setI18nContent(m_i18nContents, p_bundle, "lb_or_node_dialog");
        setI18nContent(m_i18nContents, p_bundle, "lb_or_node_properties");
        setI18nContent(m_i18nContents, p_bundle, "lb_path");
        setI18nContent(m_i18nContents, p_bundle, "lb_pointer");
        setI18nContent(m_i18nContents, p_bundle, "lb_print");
        setI18nContent(m_i18nContents, p_bundle, "lb_prologue_c");
        setI18nContent(m_i18nContents, p_bundle, "lb_properties");
        setI18nContent(m_i18nContents, p_bundle, "lb_save");
        setI18nContent(m_i18nContents, p_bundle, "lb_save_node");
        setI18nContent(m_i18nContents, p_bundle, "lb_script_c");
        setI18nContent(m_i18nContents, p_bundle, "lb_scripting");
        setI18nContent(m_i18nContents, p_bundle, "lb_select_dms_dir");
        setI18nContent(m_i18nContents, p_bundle, "lb_select_script_d");
        setI18nContent(m_i18nContents, p_bundle, "lb_start");
        setI18nContent(m_i18nContents, p_bundle, "lb_start_node_properties");
        setI18nContent(m_i18nContents, p_bundle, "lb_then");
        setI18nContent(m_i18nContents, p_bundle, "lb_title");
        setI18nContent(m_i18nContents, p_bundle,
                "lb_toggles_email_notification");
        setI18nContent(m_i18nContents, p_bundle, "lb_toggles_notification");
        setI18nContent(m_i18nContents, p_bundle, "lb_up");
        setI18nContent(m_i18nContents, p_bundle, "msg_arrow_empty_name");
        setI18nContent(m_i18nContents, p_bundle, "msg_arrow_max_length");
        setI18nContent(m_i18nContents, p_bundle,
                "msg_arrow_with_pending_events");
        setI18nContent(m_i18nContents, p_bundle, "msg_condition_to_condition");
        setI18nContent(m_i18nContents, p_bundle, "msg_data_conversion_error");
        setI18nContent(m_i18nContents, p_bundle, "msg_event_pending_warning");
        setI18nContent(m_i18nContents, p_bundle, "msg_exit_node");
        setI18nContent(m_i18nContents, p_bundle, "msg_file_extension_error");
        setI18nContent(m_i18nContents, p_bundle, "msg_incorrect_data_type");
        setI18nContent(m_i18nContents, p_bundle, "msg_invalid_workflow");
        setI18nContent(m_i18nContents, p_bundle, "msg_no_dir_selection");
        setI18nContent(m_i18nContents, p_bundle, "msg_no_dup_arrow_error");
        setI18nContent(m_i18nContents, p_bundle, "msg_path_field_blank");
        setI18nContent(m_i18nContents, p_bundle, "msg_path_field_error");
        setI18nContent(m_i18nContents, p_bundle, "msg_selection_error");
        setI18nContent(m_i18nContents, p_bundle, "msg_two_arrows");
        setI18nContent(m_i18nContents, p_bundle, "msg_two_outgoing_arrows");
        setI18nContent(m_i18nContents, p_bundle, "msg_note_ok_gray");
        setI18nContent(m_i18nContents, p_bundle, "msg_note_auto_action");
        setI18nContent(m_i18nContents, p_bundle,
                "msg_gsediton_workflow_warining");
        setI18nContent(m_i18nContents, p_bundle, "msg_note_gs_action");
        setI18nContent(m_i18nContents, p_bundle, "msg_activity_edit_warning");

        return m_i18nContents;
    }

    private static void setI18nContent(Hashtable<String, String> i18nContents,
            ResourceBundle pBundle, String key)
    {
        i18nContents.put(key, pBundle.getString("applet.resources." + key));
    }

    // get all labels required for the dialog.
    private static String[] dialogLabels(ResourceBundle p_bundle,
            boolean p_isCalendarInstalled)
    {
        String colon = p_bundle.getString("lb_colon");
        String[] array;
        // if(p_isCalendarInstalled)
        // {
        array = new String[37];
        // }
        // else
        // {
        // array= new String[32];
        // }
        array[0] = p_bundle.getString("lb_activity_type") + "* " + colon;
        array[1] = p_bundle.getString("lb_time_complete") + "* " + colon;
        array[2] = p_bundle.getString("lb_participant") + "* " + colon;
        array[3] = p_bundle.getString("lb_user_select") + "...";
        array[4] = p_bundle.getString("lb_all_qualified_users");
        array[5] = p_bundle.getString("lb_first_name");
        array[6] = p_bundle.getString("lb_last_name");
        array[7] = p_bundle.getString("lb_user_name");
        array[8] = p_bundle.getString("lb_choose");
        array[9] = p_bundle.getString("lb_days");
        array[10] = p_bundle.getString("lb_activity");
        array[11] = p_bundle.getString("msg_remove_activity_from_wf");
        array[12] = p_bundle.getString("lb_globalsight") + colon + " "
                + p_bundle.getString("lb_new_activity_type");
        array[13] = p_bundle.getString("msg_new_task");
        array[14] = p_bundle.getString("lb_globalsight") + colon + " "
                + p_bundle.getString("lb_edit_activity_type");
        array[15] = p_bundle.getString("msg_edit_activity");
        array[16] = p_bundle.getString("lb_time_accept") + "* " + colon;
        array[17] = p_bundle.getString("lb_abbreviation_day");
        array[18] = p_bundle.getString("lb_abbreviation_hour");
        array[19] = p_bundle.getString("lb_abbreviation_minute");
        array[20] = p_bundle.getString("lb_expense_rate") + colon;
        array[21] = p_bundle.getString("lb_rate_none");
        array[22] = p_bundle.getString("lb_revenue_rate") + colon;
        array[23] = p_bundle.getString("lb_estimated_amount") + colon;
        array[24] = p_bundle.getString("lb_system_action") + "* " + colon;
        array[25] = p_bundle.getString("lb_none");
        array[26] = p_bundle.getString("lb_cstfs");
        array[27] = p_bundle.getString("lb_before_activity");
        array[28] = p_bundle.getString("lb_use_only_selected_rate");
        array[29] = p_bundle.getString("lb_use_selected_rate_until_acceptance");
        array[30] = p_bundle.getString("lb_internal_costing_rate_selection")
                + colon;
        array[31] = p_bundle.getString("lb_estimated_completion_date");
        array[32] = "";
        array[33] = "";

        if (p_isCalendarInstalled)
        {
            array[32] = p_bundle.getString("lb_users_completed");
            array[33] = p_bundle.getString("lb_users_earliest");
        }

        array[34] = p_bundle.getString("lb_Overdue_PM") + "* " + colon;
        array[35] = p_bundle.getString("lb_Overdue_user") + "* " + colon;

        array[36] = p_bundle.getString("lb_report_upload_check") + colon;
              
        return array;
    }

    private static String[] dialogButtons(ResourceBundle p_bundle)
    {
        String[] array =
        { p_bundle.getString("applet_save"),
                p_bundle.getString("applet_saveb"),
                p_bundle.getString("applet_savex"),
                p_bundle.getString("applet_cancel"),
                p_bundle.getString("applet_cancelb"),
                p_bundle.getString("applet_ok"),
                p_bundle.getString("applet_okb"),
                p_bundle.getString("applet_okx") };
        return array;
    }

    // get the message
    private static String[] messages(ResourceBundle p_bundle)
    {
        String[] array =
        {
                p_bundle.getString("msg_loc_profiles_time_restrictions"),
                p_bundle.getString("msg_loc_profiles_auto_restrictions"),
                p_bundle.getString("msg_loc_profiles_overdue_time_restrictions"),
                p_bundle.getString("jsmsg_system_parameters_notification_days"),
                p_bundle.getString("msg_loc_profiles_GS_restrictions") };
        return array;
    }

    // return a list of system actions that will be displayed in the
    // activity dialog.
    private static Vector systemActions(ResourceBundle p_bundle)
    {
        Vector values = new Vector();

        values.addElement(new SystemAction(SystemAction.NO_ACTION, p_bundle
                .getString(SystemAction.NO_ACTION)));

        values.addElement(new SystemAction(SystemAction.CSTF, p_bundle
                .getString(SystemAction.CSTF)));

        values.addElement(new SystemAction(SystemAction.RSTF, p_bundle
                .getString(SystemAction.RSTF)));

        values.addElement(new SystemAction(SystemAction.ETF, p_bundle
                .getString(SystemAction.ETF)));

        return values;
    }
}
