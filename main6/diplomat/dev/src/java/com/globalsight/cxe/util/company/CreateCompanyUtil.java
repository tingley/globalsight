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
package com.globalsight.cxe.util.company;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.securitymgr.UserFieldSecurity;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.webapp.pagehandler.administration.calendars.CalendarHelper;
import com.globalsight.everest.webapp.pagehandler.administration.projects.ProjectHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.CreateUserWrapper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.SystemAction;
import com.globalsight.everest.workflow.WorkflowConditionSpec;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowOwners;
import com.globalsight.everest.workflow.WorkflowTask;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;

/**
 * Class {@code CreateCompanyUtil} is used for creating default items for a new
 * company without using JMS.
 * 
 * @since GBS-4400
 */
public class CreateCompanyUtil
{
    static private final Logger logger = Logger.getLogger(CreateCompanyUtil.class);

    private final static long LOCALE_ID_EN_US = 32;

    private final static long LOCALE_ID_DE_DE = 25;

    private final static long LOCALE_ID_ES_ES = 41;

    private final static long LOCALE_ID_FR_FR = 57;

    private final static long LOCALE_ID_IT_IT = 64;

    private final static long[] LOCALES_TARGET =
    { LOCALE_ID_DE_DE, LOCALE_ID_ES_ES, LOCALE_ID_FR_FR, LOCALE_ID_IT_IT };

    private static final String DEFAULT_DESCRIPTION_ACTIVITY = "";
    private static final String DEFAULT_DESCRIPTION_PROJECT = "";
    private static final String DEFAULT_DESCRIPTION_WORKFLOW = "";

    private static final String[] ACTIVITIES_TRANSLATION =
    { "Translation1", "Translation2", "Translation3", "Translation4", "Dtp1", "Dtp2", "Dtp3",
            "Dtp4", "Gs_dtpSO1", "Gs_dtpSO2", "GSPM1", "GSPM2", "GSPM3", "GSPM4", "GSPM5", "LSO",
            "Null", "Final_approval", "DTP_postLSO", "DTP_Preview" };

    private static final String[] ACTIVITIES_REVIEW =
    { "review_linguistc1", "review_linguistc2", "review_linguistc3", "review_linguistc4",
            "review_dtp1", "review_dtp2", "review_dtp3", "review_dtp4" };

    private final static String[] ACTIVITIES_PM =
    { "GSPM1", "GSPM2", "GSPM3", "GSPM4" };

    private final static String DEFAULT_USER_PASSWORD = "password";

    private final static String DEFAULT_EMAIL = "@";

    private final static String DEFAULT_UI_LOCALE = "en_US";

    private final static String DEFAULT_PROJECT_NAME = "Template";

    private final static String EN_US_ENCODING = "UTF-8";

    private final static String DEFAULT_ROLE = "All qualified users";

    private final static int RATIO_X = 11;

    private final static int RATIO_Y = 5;

    private final static String START_NODE = "WFObject Name";

    private final static String CONDITION_NODE = "Condition Node";

    private final static String END_NODE = "Exit";

    private final static String START_TYPE = "0";

    private final static String CONDITION_TYPE = "5";

    private final static String NODE_TYPE = "2";

    private final static String END_TYPE = "1";

    private final static String[][] TASKS_TO =
    {
            { START_NODE, START_TYPE, null, "10", "20" },
            { "", NODE_TYPE, "Translation1", "24", "35" },
            { "", NODE_TYPE, "GSPM1", "41", "35" },
            { END_NODE, END_TYPE, null, "55", "20" } };

    private final static String[][] ARROWS_TO =
    {
            { "Action75", "1", "0", "1" },
            { "Action76", "1", "1", "2" },
            { "Action77", "1", "2", "3" } };

    private final static String[][] TASKS_TR =
    {
            { "", NODE_TYPE, "Translation1", "10", "20" },
            { "", NODE_TYPE, "review_linguistc1", "26", "20" },
            { "", NODE_TYPE, "Translation2", "42", "20" },
            { "", NODE_TYPE, "review_linguistc2", "58", "20" },
            { "", NODE_TYPE, "Translation3", "74", "20" },
            { "", NODE_TYPE, "review_linguistc3", "90", "20" },

            { START_NODE, START_TYPE, null, "10", "40" },
            { CONDITION_NODE, CONDITION_TYPE, null, "30", "40" },
            { CONDITION_NODE, CONDITION_TYPE, null, "50", "40" },
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "40" },
            { "", NODE_TYPE, "GSPM1", "90", "40" },

            { END_NODE, END_TYPE, null, "10", "60" },
            { "", NODE_TYPE, "GSPM3", "30", "60" },
            { "", NODE_TYPE, "Final_approval", "50", "60" },
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "60" },
            { "", NODE_TYPE, "Translation4", "90", "60" },

            { "", NODE_TYPE, "GSPM2", "70", "80" }, };

    private final static String[][] ARROWS_TR =
    {
            { "Action1", "1", "6", "0" },
            { "Action2", "1", "0", "1" },
            { "Action3", "1", "1", "7" },
            { "Edits Required", "1", "7", "2" },
            { "Action5", "1", "2", "3" },
            { "Action6", "1", "3", "8" },
            { "Edits Required", "1", "8", "4" },
            { "Action8", "1", "4", "5" },
            { "Action97", "1", "5", "9" },
            { "Edits Required:to PM", "1", "9", "10" },
            { "To SignOff", "1", "7", "13" },
            { "To SignOff", "1", "8", "13" },
            { "To SignOff", "1", "9", "13" },
            { "Action99", "1", "10", "14" },
            { "Action123", "1", "12", "11" },
            { "Action122", "1", "13", "12" },
            { "To SignOff", "1", "14", "13" },
            { "Translation", "1", "14", "15" },
            { "Action102", "1", "15", "16" },
            { "Action106", "1", "16", "13" } };

    private final static String[][] TASKS_TRD =
    {
            { "", NODE_TYPE, "Translation1", "10", "20" },
            { "", NODE_TYPE, "review_linguistc1", "26", "20" },
            { "", NODE_TYPE, "Translation2", "42", "20" },
            { "", NODE_TYPE, "review_linguistc2", "58", "20" },
            { "", NODE_TYPE, "Translation3", "74", "20" },
            { "", NODE_TYPE, "review_linguistc3", "90", "20" },

            { START_NODE, START_TYPE, null, "10", "40" },
            { CONDITION_NODE, CONDITION_TYPE, null, "30", "40" },
            { CONDITION_NODE, CONDITION_TYPE, null, "50", "40" },
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "40" },
            { "", NODE_TYPE, "GSPM1", "90", "40" },

            { "", NODE_TYPE, "DTP_postLSO", "10", "60" },
            { "", NODE_TYPE, "LSO", "30", "60" },
            { "", NODE_TYPE, "Dtp1", "50", "60" },
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "60" },
            { "", NODE_TYPE, "Translation4", "90", "60" },

            { "", NODE_TYPE, "GSPM2", "70", "75" },

            { "", NODE_TYPE, "Gs_dtpSO1", "10", "90" },
            { "", NODE_TYPE, "review_dtp1", "30", "90" },
            { "", NODE_TYPE, "Dtp2", "50", "90" },
            { "", NODE_TYPE, "review_dtp2", "70", "90" },
            { "", NODE_TYPE, "Dtp3", "90", "90" },

            { END_NODE, END_TYPE, null, "10", "110" },
            { CONDITION_NODE, CONDITION_TYPE, null, "40", "110" },
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "110" },

            { "", NODE_TYPE, "GSPM5", "10", "130" },
            { "", NODE_TYPE, "Final_approval", "30", "130" },
            { "", NODE_TYPE, "Gs_dtpSO2", "50", "130" },
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "130" },
            { "", NODE_TYPE, "review_dtp3", "90", "130" },

            { CONDITION_NODE, CONDITION_TYPE, null, "70", "150" },
            { "", NODE_TYPE, "GSPM3", "90", "150" },

            { "", NODE_TYPE, "GSPM4", "50", "170" },
            { "", NODE_TYPE, "Dtp4", "70", "170" } };

    private final static String[][] ARROWS_TRD =
    {
            { "Action1", "1", "6", "0" },
            { "Action2", "1", "0", "1" },
            { "Action3", "1", "1", "7" },
            { "Edits Required", "1", "7", "2" },
            { "Action5", "1", "2", "3" },
            { "Action6", "1", "3", "8" },
            { "Edits Required", "1", "8", "4" },
            { "Action8", "1", "4", "5" },
            { "Action97", "1", "5", "9" },
            { "Edits Required:to PM", "1", "9", "10" },
            { "Approved for DTP", "1", "7", "13" },
            { "Approved for DTP", "1", "8", "13" },
            { "Approved for DTP", "1", "9", "13" },
            { "Action99", "1", "10", "14" },
            { "Action105", "1", "12", "11" },
            { "Action104", "1", "13", "12" },
            { "Approved for DTP", "1", "14", "13" },
            { "Translation", "1", "14", "15" },
            { "Action102", "1", "15", "16" },
            { "Action103", "1", "16", "13" },
            { "Action106", "1", "11", "17" },
            { "Action107", "1", "17", "18" },
            { "Action108", "1", "18", "23" },
            { "dtp edits", "1", "23", "19" },
            { "Action110", "1", "19", "20" },
            { "Action111", "1", "20", "24" },
            { "dtp edits", "1", "24", "21" },
            { "Action113", "1", "21", "29" },
            { "Action123", "1", "25", "22" },
            { "Approved for SO", "1", "23", "27" },
            { "Approved for SO", "1", "24", "27" },
            { "Action122", "1", "26", "25" },
            { "Action121", "1", "27", "26" },
            { "Approved for SO", "1", "28", "27" },
            { "Action114", "1", "29", "28" },
            { "Approved for SO", "1", "30", "27" },
            { "dtp edits:to PM", "1", "28", "31" },
            { "Action117", "1", "31", "30" },
            { "Action118", "1", "30", "33" },
            { "Action119", "1", "33", "32" },
            { "Action120", "1", "32", "27" } };

    /**
     * Creates a company asynchronously with thread instead of JMS.
     */
    static public void createCompanyWithThread(Map<String, String> data)
    {
        CreateCompanyRunnable runnable = new CreateCompanyRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Creates a company synchronously.
     */
    static public void createCompany(Map<String, String> p_data)
    {
        String creatorId = p_data.get("creatorId");
        String companyId = p_data.get("companyId");
        if (companyId == null || creatorId == null)
        {
            logger.error("Could not continue creating company with null companyId or creatorId.");
            return;
        }
        try
        {
            Company company = CompanyWrapper.getCompanyById(companyId);
            createDefaultItems(company, creatorId);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private static void createDefaultItems(Company p_company, String p_creatorId)
    {
        logger.info("Creating default items for new company " + p_company.getCompanyName());

        List<LocalePair> localePairs = createLocalePairs(p_company);
        List<Activity> activities = createActivities(p_company, localePairs);
        createUsersAndProjectAndWorkflows(p_company, localePairs, activities, p_creatorId);

        logger.info("Done creating default items for company " + p_company.getCompanyName());
    }

    /**
     * Creates default locale pairs for the new company.
     */
    private static List<LocalePair> createLocalePairs(Company p_company)
    {
        logger.info("Creating locale pairs for company " + p_company.getCompanyName());

        long srcId = LOCALE_ID_EN_US;
        List<LocalePair> localePairs = new ArrayList<LocalePair>();
        try
        {
            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();

            GlobalSightLocale srcLocale = localeMgr.getLocaleById(srcId);
            for (long targetLocaleId : LOCALES_TARGET)
            {
                GlobalSightLocale targLocale = localeMgr.getLocaleById(targetLocaleId);
                try
                {
                    LocalePair lp = new LocalePair(srcLocale, targLocale, p_company.getId());
                    HibernateUtil.saveOrUpdate(lp);
                    localePairs.add(lp);
                }
                catch (Exception e)
                {
                    logger.error("Failed to create a locale pair " + srcLocale.toString() + " : "
                            + targLocale.toString(), e);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Error creating locale pairs for company " + p_company.getCompanyName(),
                    e);
        }

        logger.info("Done creating locale pairs for company " + p_company.getCompanyName());

        return localePairs;
    }

    /**
     * Creates default activities for the new company.
     */
    private static List<Activity> createActivities(Company p_company,
            List<LocalePair> p_localePairs)
    {

        logger.info("Creating activities for company " + p_company.getCompanyName());

        List<Activity> activities = new ArrayList<Activity>();
        for (String activityName : ACTIVITIES_TRANSLATION)
        {
            Activity a = new Activity();
            a.setDisplayName(activityName);
            a.setName(activityName + "_" + p_company.getId());
            a.setDescription(DEFAULT_DESCRIPTION_ACTIVITY);
            a.setType(Activity.TYPE_TRANSLATE);
            a.setIsEditable(true);
            a.setCompanyId(p_company.getId());
            a.setUseType(Activity.USE_TYPE_TRANS);

            createActivity(a, p_localePairs);
            activities.add(a);
        }
        for (String activityName : ACTIVITIES_REVIEW)
        {
            Activity a = new Activity();
            a.setDisplayName(activityName);
            a.setName(activityName + "_" + p_company.getId());
            a.setDescription(DEFAULT_DESCRIPTION_ACTIVITY);
            a.setType(Activity.TYPE_REVIEW);
            a.setIsEditable(false);
            a.setCompanyId(p_company.getId());
            a.setUseType(Activity.USE_TYPE_TRANS);

            createActivity(a, p_localePairs);
            activities.add(a);
        }

        logger.info("Done creating activities for company " + p_company.getCompanyName());

        return activities;
    }

    /**
     * Creates an activity and associated roles.
     */
    private static void createActivity(Activity p_activity, List<LocalePair> p_localePairs)
    {
        try
        {
            HibernateUtil.save(p_activity);
        }
        catch (Exception e)
        {
            logger.error("Failed to save activity " + p_activity.getActivityName(), e);
        }
        for (LocalePair lp : p_localePairs)
        {
            ContainerRole role = ServerProxy.getUserManager().createContainerRole();
            role.setActivity(p_activity);
            role.setSourceLocale(lp.getSource().toString());
            role.setTargetLocale(lp.getTarget().toString());
            try
            {
                ServerProxy.getUserManager().addRole(role);
            }
            catch (Exception e)
            {
                logger.error("Failed to save role " + role.getName(), e);
            }
        }
    }

    /**
     * Creates three default users, project and workflows for the new company.
     * 
     * <p>
     * The three users include a project manager, an administrator and a
     * localization participant.
     */
    private static void createUsersAndProjectAndWorkflows(Company p_company,
            List<LocalePair> p_localePairs, List<Activity> p_activities, String p_creatorId)
    {
        logger.info("Creating users for company " + p_company.getCompanyName());

        String pmName = getPmName(p_company.getCompanyName());
        List<Activity> pmActivities = getPmActivities(p_activities, p_company.getId());

        String adminName = getAdminName(p_company.getCompanyName());
        String lpName = getLpName(p_company.getCompanyName());
        try
        {
            createUser(pmName, pmActivities, p_company, p_creatorId, "ProjectManager");
        }
        catch (Exception e)
        {
            logger.error("Failed to create project manager " + pmName, e);
        }

        try
        {
            createUser(adminName, null, p_company, p_creatorId, "Administrator");
        }
        catch (Exception e)
        {
            logger.error("Failed to create administrator " + adminName, e);
        }

        try
        {
            createUser(lpName, p_activities, p_company, p_creatorId, "LocalizationParticipant");
        }
        catch (Exception e)
        {
            logger.error("Failed to create localization participant " + lpName, e);
        }

        logger.info("Done creating users for company " + p_company.getCompanyName());

        Project project = createProject(p_company, pmName, adminName, lpName);
        createWorkflows(p_company, p_localePairs, p_activities, project, pmName, adminName);
    }

    /**
     * Creates a default user.
     */
    private static void createUser(String p_userName, List<Activity> p_activities,
            Company p_company, String p_creatorId, String p_permGroupName) throws Exception
    {
        UserManager userMgr = ServerProxy.getUserManager();
        User creatorUser = userMgr.getUser(p_creatorId);

        CreateUserWrapper wrapper = new CreateUserWrapper(userMgr, creatorUser);
        wrapper.setUserId(UserUtil.newUserId(p_userName));
        wrapper.setUserName(p_userName);
        wrapper.setFirstName(p_userName);
        wrapper.setLastName(p_userName);
        wrapper.setCompanyName(p_company.getCompanyName());
        wrapper.setPassword(DEFAULT_USER_PASSWORD);
        wrapper.setEmail(DEFAULT_EMAIL);
        wrapper.setDefaultUILocale(DEFAULT_UI_LOCALE);
        wrapper.setCurCompanyId(String.valueOf(p_company.getId()));

        // Adds roles
        Hashtable<Activity, Vector<String>> activityCostMap = new Hashtable<Activity, Vector<String>>();
        Vector<String> params = new Vector<String>();
        params.addElement("true");
        params.addElement("-1");
        if (p_activities != null && p_activities.size() > 0)
        {
            for (Activity activity : p_activities)
            {
                activityCostMap.put(activity, params);
            }

            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
            long srcId = LOCALE_ID_EN_US;

            GlobalSightLocale srcLocale = localeMgr.getLocaleById(srcId);
            String sSrcLocale = srcLocale.getLocale().toString();
            for (long targetLocaleId : LOCALES_TARGET)
            {
                GlobalSightLocale targLocale = localeMgr.getLocaleById(targetLocaleId);
                String sTargLocale = targLocale.getLocale().toString();
                wrapper.addUserRoles(sSrcLocale, sTargLocale, activityCostMap);
            }
        }
        wrapper.setIsInAllProjects(true);
        // Adds field security
        wrapper.setFieldSecurity(new UserFieldSecurity());
        // Adds calendar
        FluxCalendar baseCal = CalendarHelper.getDefaultCalendar(String.valueOf(p_company.getId()));
        UserFluxCalendar cal = new UserFluxCalendar(baseCal.getId(), wrapper.getUserId(),
                baseCal.getTimeZoneId());
        CalendarHelper.updateUserCalFieldsFromBase(baseCal, cal);
        wrapper.setCalendar(cal);

        wrapper.commitWrapper();
        // Adds permission group
        PermissionManager manager = Permission.getPermissionManager();
        ArrayList<String> users = new ArrayList<String>(1);
        users.add(wrapper.getUserId());

        Collection<PermissionGroup> allPermGroups = Permission.getPermissionManager()
                .getAllPermissionGroupsByCompanyId(String.valueOf(p_company.getId()));
        Iterator<PermissionGroup> iter = allPermGroups.iterator();
        while (iter.hasNext())
        {
            PermissionGroup permGroup = (PermissionGroup) iter.next();
            if (p_permGroupName.equals(permGroup.getName()))
            {
                manager.mapUsersToPermissionGroup(users, permGroup);
                break;
            }
        }
    }

    /**
     * Creates a default project for the new company.
     */
    private static Project createProject(Company p_company, String p_pmName, String p_adminName,
            String p_lpName)
    {
        logger.info("Creating project for company " + p_company.getCompanyName());

        Project project = ProjectHandlerHelper.createProject();
        project.setCompanyId(p_company.getId());
        project.setDescription(DEFAULT_DESCRIPTION_PROJECT);
        project.setTermbaseName("");
        project.setName(DEFAULT_PROJECT_NAME);
        project.setQuotePerson(null);

        try
        {
            User pm = ServerProxy.getUserManager().getUserByName(p_pmName);
            project.setProjectManager(pm);
        }
        catch (Exception e)
        {
            logger.error("Failed to get project manager " + p_pmName, e);
        }
        // Adds default users to the project.
        TreeSet<String> users = new TreeSet<String>();
        if (p_pmName != null)
        {
            users.add(UserUtil.getUserIdByName(p_pmName));
        }
        if (p_adminName != null)
        {
            users.add(UserUtil.getUserIdByName(p_adminName));
        }
        if (p_lpName != null)
        {
            users.add(UserUtil.getUserIdByName(p_lpName));
        }
        project.setUserIds(users);

        ProjectHandlerHelper.addProject(project);

        logger.info("Done creating project for company " + p_company.getCompanyName());

        return project;
    }

    /**
     * Creates the Translation Only workflow with name suffix _T.
     */
    private static void createToWorkflow(LocalePair localePair, List<Activity> p_activities,
            Project p_project, String p_pmName, String p_adminName)
    {
        createWorkflow("_T", localePair, p_activities, p_project, p_pmName, p_adminName, TASKS_TO,
                ARROWS_TO);
    }

    /**
     * Creates the Translation Review workflow with name suffix _TR.
     */
    private static void createTRWorkflow(LocalePair localePair, List<Activity> p_activities,
            Project p_project, String p_pmName, String p_adminName)
    {
        createWorkflow("_TR", localePair, p_activities, p_project, p_pmName, p_adminName, TASKS_TR,
                ARROWS_TR);
    }

    /**
     * Creates the Translation Review DTP workflow with name suffix _TRD.
     */
    private static void createTRDWorkflow(LocalePair localePair, List<Activity> p_activities,
            Project p_project, String p_pmName, String p_adminName)
    {
        createWorkflow("_TRD", localePair, p_activities, p_project, p_pmName, p_adminName,
                TASKS_TRD, ARROWS_TRD);
    }

    /**
     * Creates the default workflow.
     */
    private static void createWorkflow(String p_nameSuffix, LocalePair p_localePair,
            List<Activity> p_activities, Project p_project, String p_pmName, String p_adminName,
            String[][] p_tasksInfo, String[][] p_arrowsInfo)
    {
        String workflowName = p_localePair.getSource().toString() + "_"
                + p_localePair.getTarget().toString() + p_nameSuffix;

        WorkflowTemplateInfo wfti = createWfti(p_localePair, workflowName, p_project, p_pmName,
                p_adminName);
        String[] wfManagerIds = new String[wfti.getWorkflowManagerIds().size()];
        wfManagerIds = (String[]) wfti.getWorkflowManagerIds().toArray(wfManagerIds);
        WorkflowTemplate wfTemplate = createWfTemplate(workflowName, p_localePair, p_activities,
                p_tasksInfo, p_arrowsInfo);
        WorkflowOwners orwners = new WorkflowOwners(wfti.getProjectManagerId(), wfManagerIds);

        try
        {
            WorkflowTemplate wft = ServerProxy.getWorkflowServer()
                    .createWorkflowTemplate(wfTemplate, orwners);
            wfti.setWorkflowTemplate(wft);
            ServerProxy.getProjectHandler().createWorkflowTemplateInfo(wfti);
        }
        catch (Exception e)
        {
            logger.error("Failed to create workflow " + workflowName, e);
        }
    }

    /**
     * Creates a workflow template.
     */
    private static WorkflowTemplate createWfTemplate(String p_workflowName, LocalePair p_localePair,
            List<Activity> p_activities, String[][] p_tasksInfo, String[][] p_arrowsInfo)
    {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName(p_workflowName);
        template.setDescription(DEFAULT_DESCRIPTION_WORKFLOW);
        ArrayList<WorkflowTask> tasks = new ArrayList<WorkflowTask>();

        // Adds all task according to taskInfo.
        for (int i = 0; i < p_tasksInfo.length; i++)
        {
            String[] taskInfo = p_tasksInfo[i];
            WorkflowTask task = createWorkflowTask(p_localePair, p_activities, taskInfo);
            task.setSequence(i);
            template.addWorkflowTask(task);
            tasks.add(task);
        }

        // Adds all arrows according to arrowsInfo.
        for (int i = 0; i < p_arrowsInfo.length; i++)
        {
            String[] arrowInfo = p_arrowsInfo[i];
            WorkflowTask sourceNode = (WorkflowTask) tasks.get(Integer.parseInt(arrowInfo[2]));
            WorkflowTask targetNode = (WorkflowTask) tasks.get(Integer.parseInt(arrowInfo[3]));
            template.addArrow(arrowInfo[0], Long.parseLong(arrowInfo[1]), sourceNode, targetNode);
        }

        return template;
    }

    /**
     * Creates a workflow task.
     */
    private static WorkflowTask createWorkflowTask(LocalePair p_localePair,
            List<Activity> p_activities, String[] p_taskInfo)
    {
        String name = p_taskInfo[0];
        int type = Integer.parseInt(p_taskInfo[1]);
        Activity activity = getActivityByName(p_taskInfo[2], p_activities,
                p_localePair.getCompanyId());
        int x = Integer.parseInt(p_taskInfo[3]) * RATIO_X;
        int y = (Integer.parseInt(p_taskInfo[4]) - 10) * RATIO_Y;

        // Condition node is not in center.
        if (CONDITION_NODE.equals(name))
        {
            x -= 15;
            y -= 15;
        }

        WorkflowTask task = new WorkflowTask(name, type);

        task.setActivity(activity);
        task.setPosition(new Point(x, y));
        task.setAcceptedTime(WorkflowConstants.accept_time);
        task.setCompletedTime(WorkflowConstants.complete_time);
        task.setOverdueToPM(WorkflowConstants.overDuePM_time);
        task.setOverdueToUser(WorkflowConstants.overDueUser_time);

        task.setDisplayRoleName(DEFAULT_ROLE);
        task.setActionType(SystemAction.NO_ACTION);
        task.setConditionSpec(new WorkflowConditionSpec());

        // Adds roles if the task has activities.
        if (activity != null)
        {
            StringBuffer role = new StringBuffer();
            role.append(activity.getId()).append(" ").append(activity.getName()).append(" ")
                    .append(p_localePair.getSource()).append(" ").append(p_localePair.getTarget());
            String[] roles =
            { role.toString() };
            task.setRoles(roles);
        }
        else if (CONDITION_NODE.equals(name))
        {
            task.setConditionSpec(new WorkflowConditionSpec());
        }

        return task;
    }

    /**
     * Creates a workflow template info.
     */
    private static WorkflowTemplateInfo createWfti(LocalePair p_localePair, String p_workflowName,
            Project p_project, String p_pmName, String p_adminName)
    {
        // Sets the manager.
        ArrayList<String> managerIds = new ArrayList<String>();
        if (p_pmName != null)
        {
            managerIds.add(UserUtil.getUserIdByName(p_pmName));
        }

        if (p_adminName != null)
        {
            managerIds.add(UserUtil.getUserIdByName(p_adminName));
        }

        // Sets leverage locale.
        Set<LeverageLocales> leveragingLocales = new HashSet<LeverageLocales>();
        LeverageLocales leverageLocale = new LeverageLocales(p_localePair.getTarget());
        leveragingLocales.add(leverageLocale);

        // Sets workflowtemplateInfo.
        WorkflowTemplateInfo wfti = new WorkflowTemplateInfo(p_workflowName,
                DEFAULT_DESCRIPTION_WORKFLOW, p_project, true, managerIds, p_localePair.getSource(),
                p_localePair.getTarget(), EN_US_ENCODING, leveragingLocales);

        wfti.setWorkflowType(WorkflowTypeConstants.TYPE_TRANSLATION);

        // Sets company.
        wfti.setCompanyId(p_localePair.getCompanyId());

        leverageLocale.setBackPointer(wfti);

        return wfti;
    }

    /**
     * Creates default workflows for the new company.
     */
    private static void createWorkflows(Company p_company, List<LocalePair> p_localePairs,
            List<Activity> p_activities, Project p_project, String p_pmName, String p_adminName)
    {
        logger.info("Creating workflows for company " + p_company.getCompanyName());

        for (LocalePair localePair : p_localePairs)
        {
            createToWorkflow(localePair, p_activities, p_project, p_pmName, p_adminName);
            createTRWorkflow(localePair, p_activities, p_project, p_pmName, p_adminName);
            createTRDWorkflow(localePair, p_activities, p_project, p_pmName, p_adminName);
        }

        logger.info("Done creating workflows for company " + p_company.getCompanyName());
    }

    /**
     * Gets activity from the created activities.
     */
    private static Activity getActivityByName(String p_activityName, List<Activity> p_activities,
            long p_companyId)
    {
        if (p_activityName == null)
        {
            return null;
        }
        String activityName = p_activityName + "_" + p_companyId;
        for (Activity activity : p_activities)
        {
            if (activityName.equals(activity.getActivityName()))
            {
                return activity;
            }
        }
        logger.error("The activity " + activityName + " could not be found.");
        return null;
    }

    /**
     * Gets PM activities from the newly created activities.
     */
    private static List<Activity> getPmActivities(List<Activity> p_activities, long p_companyId)
    {
        List<Activity> activities = new ArrayList<Activity>();
        for (String activityName : ACTIVITIES_PM)
        {
            Activity activity = getActivityByName(activityName, p_activities, p_companyId);
            if (activity != null)
            {
                activities.add(activity);
            }
            else
            {
                logger.error("Could not get the activity: " + activityName);
            }
        }

        return activities;
    }

    /**
     * Gets a user name that does not exist in database.
     */
    private static String getName(String name)
    {
        String name1 = name.toLowerCase().replace(" ", "");
        String name2 = name1;
        int i = 1;

        while (nameExist(name2))
        {
            name2 = name1 + i;
            i++;
        }

        return name2;
    }

    /**
     * Gets admin's name.
     */
    private static String getAdminName(String p_companyName)
    {
        String name = p_companyName + "admin";

        return getName(name);
    }

    /**
     * Gets localization participant's name.
     */
    private static String getLpName(String p_companyName)
    {
        String name = p_companyName + "anyone";

        return getName(name);
    }

    /**
     * Gets project manager's name.
     */
    private static String getPmName(String p_companyName)
    {
        String name = "gs" + p_companyName + "pm";

        return getName(name);
    }

    private static boolean nameExist(String p_name)
    {
        String[] userNames = UserHandlerHelper.getAllUserNames();
        for (String userName : userNames)
        {
            // The name exists
            if (p_name.equalsIgnoreCase(userName))
            {
                logger.error("Could not create user " + p_name + " because " + p_name
                        + " already exists in the system.");

                return true;
            }
        }

        return false;
    }
}
