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

package com.globalsight.util;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.ContainerRole;
import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionGroup;
import com.globalsight.everest.permission.PermissionManager;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.securitymgr.UserFieldSecurity;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserManager;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
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

/**
 * <p>
 * This class is used to create default items for a new company.
 * 
 * <p>
 * These items will be created by default.
 * <ul>
 * <li>Locale pairs</li>
 * <li>Activities</li>
 * <li>Users</li>
 * <li>A Project</li>
 * <li>Workflows</li>
 * </ul>
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_NEW_COMPANY_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class SetupCompanyMDB extends GenericQueueMDB
{
    private static Logger CATEGORY = Logger.getLogger(SetupCompanyMDB.class
            .getName());

    private static final long serialVersionUID = 1L;

    private long companyId;

    private String companyName;

    private ArrayList<Activity> activities;

    private User createUser;

    private ArrayList<LocalePair> localePairs;

    private Project project;

    private String pmName;

    private String adminName;

    private String lpName;

    // For adding locale pairs
    private final long EN_US_LOCALE_ID = 32;

    private final long DE_DE_LOCALE_ID = 25;

    private final long ES_ES_LOCALE_ID = 41;

    private final long FR_FR_LOCALE_ID = 57;

    private final long IT_IT_LOCALE_ID = 64;

    private final long[] LOCALE_TO =
    { DE_DE_LOCALE_ID, ES_ES_LOCALE_ID, FR_FR_LOCALE_ID, IT_IT_LOCALE_ID };

    // For adding activities
    private static final String DEFAULT_DECCRIPTION = "";

    private static final String[] TRANS =
    { "Translation1", "Translation2", "Translation3", "Translation4", "Dtp1",
            "Dtp2", "Dtp3", "Dtp4", "Gs_dtpSO1", "Gs_dtpSO2", "GSPM1", "GSPM2",
            "GSPM3", "GSPM4", "GSPM5", "LSO", "Null", "Final_approval",
            "DTP_postLSO", "DTP_Preview" };

    private static final String[] REVIEWERS =
    { "review_linguistc1", "review_linguistc2", "review_linguistc3",
            "review_linguistc4", "review_dtp1", "review_dtp2", "review_dtp3",
            "review_dtp4" };

    // For creating users
    private final String DEFAULT_PASSWORD = "password";

    private final String DEFAULT_EMAIL = "@";

    private final String DEFAULT_UI_LOCALE = "en_US";

    private final String[] PM_ACTIVITIES =
    { "GSPM1", "GSPM2", "GSPM3", "GSPM4" };

    // For create project
    private final String DEFAULT_PROJECT_NAME = "Template";

    private final String EN_US_ENCODING = "UTF-8";

    // For create workflow
    private final long ACCEPT_TIME = 86400000;// 24*60*60*1000 = 86400

    private final long COMPLETE_TIME = 86400000;

    private final String DEFAULT_ROLE = "All qualified users";

    private final int X_RATIO = 11; // It is used to count the real point.x of

    // each node.
    private final int Y_RATIO = 5; // It is used to count the real point.y of

    // each node.
    private final String START_NODE = "WFObject Name";

    private final String CONDITION_NODE = "Condition Node";

    private final String END_NODE = "Exit";

    private final String START_TYPE = "0";

    private final String CONDITION_TYPE = "5";

    private final String NODE_TYPE = "2";

    private final String END_TYPE = "1";

    private final String[][] TO_TASKS =
    { // real x,y need * X_RATIO or
      // Y_RATIO.
      // name(0) type(1) activity (2) x(3) y(4)
            { START_NODE, START_TYPE, null, "10", "20" }, // 0
            { "", NODE_TYPE, "Translation1", "24", "35" }, // 1
            { "", NODE_TYPE, "GSPM1", "41", "35" }, // 2
            { END_NODE, END_TYPE, null, "55", "20" } // 3
    };

    private final String[][] TO_ARROWS =
    {
            // name(0) type(1) start note(2) end note(3)
            { "Action75", "1", "0", "1" },
            { "Action76", "1", "1", "2" },
            { "Action77", "1", "2", "3" } };

    private final String[][] TR_TASKS =
    { // real x,y need * X_RATIO or
      // Y_RATIO.
      // name(0) type(1) activity (2) x(3) y(4)
            { "", NODE_TYPE, "Translation1", "10", "20" }, // 0
            { "", NODE_TYPE, "review_linguistc1", "26", "20" }, // 1
            { "", NODE_TYPE, "Translation2", "42", "20" }, // 2
            { "", NODE_TYPE, "review_linguistc2", "58", "20" }, // 3
            { "", NODE_TYPE, "Translation3", "74", "20" }, // 4
            { "", NODE_TYPE, "review_linguistc3", "90", "20" }, // 5

            { START_NODE, START_TYPE, null, "10", "40" }, // 6
            { CONDITION_NODE, CONDITION_TYPE, null, "30", "40" }, // 7
            { CONDITION_NODE, CONDITION_TYPE, null, "50", "40" }, // 8
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "40" }, // 9
            { "", NODE_TYPE, "GSPM1", "90", "40" }, // 10

            { END_NODE, END_TYPE, null, "10", "60" }, // 11
            { "", NODE_TYPE, "GSPM3", "30", "60" }, // 12
            { "", NODE_TYPE, "Final_approval", "50", "60" }, // 13
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "60" }, // 14
            { "", NODE_TYPE, "Translation4", "90", "60" }, // 15

            { "", NODE_TYPE, "GSPM2", "70", "80" }, // 16
    };

    private final String[][] TR_ARROWS =
    {
            // name(0) type(1) start note(2) end note(3)
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

    private final String[][] TRD_TASKS =
    { // real x,y need * X_RATIO or
      // Y_RATIO.
      // name(0) type(1) activity (2) x(3) y(4)
            { "", NODE_TYPE, "Translation1", "10", "20" }, // 0
            { "", NODE_TYPE, "review_linguistc1", "26", "20" }, // 1
            { "", NODE_TYPE, "Translation2", "42", "20" }, // 2
            { "", NODE_TYPE, "review_linguistc2", "58", "20" }, // 3
            { "", NODE_TYPE, "Translation3", "74", "20" }, // 4
            { "", NODE_TYPE, "review_linguistc3", "90", "20" }, // 5

            { START_NODE, START_TYPE, null, "10", "40" }, // 6
            { CONDITION_NODE, CONDITION_TYPE, null, "30", "40" }, // 7
            { CONDITION_NODE, CONDITION_TYPE, null, "50", "40" }, // 8
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "40" }, // 9
            { "", NODE_TYPE, "GSPM1", "90", "40" }, // 10

            { "", NODE_TYPE, "DTP_postLSO", "10", "60" }, // 11
            { "", NODE_TYPE, "LSO", "30", "60" }, // 12
            { "", NODE_TYPE, "Dtp1", "50", "60" }, // 13
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "60" }, // 14
            { "", NODE_TYPE, "Translation4", "90", "60" }, // 15

            { "", NODE_TYPE, "GSPM2", "70", "75" }, // 16

            { "", NODE_TYPE, "Gs_dtpSO1", "10", "90" }, // 17
            { "", NODE_TYPE, "review_dtp1", "30", "90" }, // 18
            { "", NODE_TYPE, "Dtp2", "50", "90" }, // 19
            { "", NODE_TYPE, "review_dtp2", "70", "90" }, // 20
            { "", NODE_TYPE, "Dtp3", "90", "90" }, // 21

            { END_NODE, END_TYPE, null, "10", "110" }, // 22
            { CONDITION_NODE, CONDITION_TYPE, null, "40", "110" }, // 23
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "110" }, // 24

            { "", NODE_TYPE, "GSPM5", "10", "130" }, // 25
            { "", NODE_TYPE, "Final_approval", "30", "130" }, // 26
            { "", NODE_TYPE, "Gs_dtpSO2", "50", "130" }, // 27
            { CONDITION_NODE, CONDITION_TYPE, null, "70", "130" }, // 28
            { "", NODE_TYPE, "review_dtp3", "90", "130" }, // 29

            { CONDITION_NODE, CONDITION_TYPE, null, "70", "150" }, // 30
            { "", NODE_TYPE, "GSPM3", "90", "150" }, // 31

            { "", NODE_TYPE, "GSPM4", "50", "170" }, // 32
            { "", NODE_TYPE, "Dtp4", "70", "170" } // 33
    };

    private final String[][] TRD_ARROWS =
    {
            // name(0) type(1) start note(2) end note(3)
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

    public SetupCompanyMDB()
    {
        super(CATEGORY);
    }

    /**
     * <p>
     * Adds all default items for the new company.
     * 
     * <p>
     * These items will be created as follows.
     * <ul>
     * <li>Locale pairs</li>
     * <li>Activities</li>
     * <li>A Project</li>
     * <li>Users</li>
     * <li>Work flows</li>
     * </ul>
     * 
     * @throws JobException
     * @throws RemoteException
     * @throws GeneralException
     * @throws NamingException
     */
    private void addDefaultItems() throws Exception
    {
        CATEGORY.info("Creating default items for new company "
                + this.companyName);

        createLanguagePairs();
        createActivities();
        createUsers();
        createProject();
        createWorkflows();

        CATEGORY.info("Done creating default items for company "
                + this.companyName);
    }

    /**
     * <p>
     * Creates all default language pairs for the new company.
     * 
     * <p>
     * Source locale only <code>en_Us</code>, target locale is read from
     * <code>this.LOCALE_TO</code>, and <code>this.LOCALE_TO</code> records
     * target locales id.
     */
    private void createLanguagePairs()
    {
        CATEGORY.info("Creating locale pairs for company " + this.companyName);

        long srcId = EN_US_LOCALE_ID;

        try
        {
            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();

            // Gets source locale.
            GlobalSightLocale srcLocale = localeMgr.getLocaleById(srcId);

            // Records all created locale pairs, then can use it when add other
            // default items.
            localePairs = new ArrayList<LocalePair>();

            for (int i = 0; i < LOCALE_TO.length; i++)
            {
                // Gets target locale.
                long targId = LOCALE_TO[i];
                GlobalSightLocale targLocale = localeMgr.getLocaleById(targId);

                // Creates a locale pair with the source locale and target
                // locale.
                try
                {
                    LocalePair lp = new LocalePair(srcLocale, targLocale,
                            companyId);
                    HibernateUtil.saveOrUpdate(lp);
                    localePairs.add(lp);
                }
                catch (Exception e)
                {
                    CATEGORY.error(
                            "Failed to add a locale pair"
                                    + srcLocale.toString() + " : "
                                    + targLocale.toString(), e);
                    String args[] =
                    { srcLocale.toString(), targLocale.toString() };
                    throw new LocaleManagerException(
                            LocaleManagerException.MSG_FAILED_TO_ADD_LOCALE_PAIR,
                            args, e);
                }
            }
        }
        catch (Exception lme)
        {
            throw new EnvoyServletException(EnvoyServletException.EX_GENERAL,
                    lme);
        }

        CATEGORY.info("Done creating locale pairs for company "
                + this.companyName);
    }

    /**
     * <p>
     * Creates all activities for the new company.
     * 
     * <p>
     * The informations about that activities is read from
     * <code>this.TRANS</code> and <code>this.REVIEWERS</code>
     * 
     * <p>
     * <code>this.TRANS</code> include the information about translate
     * activities and <code>this.REVIEWERS</code> is about review-only
     * activities.
     * 
     * @throws JobException
     * @throws RemoteException
     * @throws GeneralException
     * @throws NamingException
     */
    private void createActivities() throws Exception
    {

        CATEGORY.info("Creating activities for company " + this.companyName);

        activities = new ArrayList<Activity>();

        for (int i = 0; i < TRANS.length; i++)
        {
            Activity act = new Activity();
            act.setDisplayName(TRANS[i]);
            act.setName(TRANS[i] + "_" + companyId);
            act.setDescription(DEFAULT_DECCRIPTION);
            act.setType(Activity.TYPE_TRANSLATE);
            act.setIsEditable(true);
            act.setCompanyId(companyId);
            act.setUseType(Activity.USE_TYPE_TRANS);
            createActivity(act);
            activities.add(act);
        }
        for (int i = 0; i < REVIEWERS.length; i++)
        {
            Activity act = new Activity();
            act.setDisplayName(REVIEWERS[i]);
            act.setName(REVIEWERS[i] + "_" + companyId);
            act.setDescription(DEFAULT_DECCRIPTION);
            act.setType(Activity.TYPE_REVIEW);
            act.setIsEditable(false);
            act.setCompanyId(companyId);
            act.setUseType(Activity.USE_TYPE_TRANS);
            createActivity(act);
            activities.add(act);
        }

        CATEGORY.info("Done creating activities for company "
                + this.companyName);
    }

    /**
     * Creates a activity.
     * 
     * @param activity
     *            the activity to create
     */
    private void createActivity(Activity activity)
    {
        try
        {
            HibernateUtil.save(activity);
        }
        catch (Exception e1)
        {
            throw new EnvoyServletException(e1);
        }

        for (int i = 0; i < this.localePairs.size(); i++)
        {
            ContainerRole role = ServerProxy.getUserManager()
                    .createContainerRole();
            LocalePair lp = (LocalePair) localePairs.get(i);
            role.setActivity(activity);
            role.setSourceLocale(lp.getSource().toString());
            role.setTargetLocale(lp.getTarget().toString());

            try
            {
                ServerProxy.getUserManager().addRole(role);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
    }

    /**
     * <p>
     * Creates three users for the new company.
     * 
     * <p>
     * These users include a project manager, a admin and a localizition
     * participant.
     */
    private void createUsers()
    {
        CATEGORY.info("Creating users for company " + this.companyName);
        this.pmName = getPmName();
        this.adminName = getAdminName();
        this.lpName = getLpName();

        try
        {
            createUser(this.pmName, getActivitiesByName(PM_ACTIVITIES),
                    "ProjectManager");
        }
        catch (Exception e)
        {
            CATEGORY.error("Project manager " + this.pmName
                    + " failed to be created.", e);
            this.pmName = null;
        }

        try
        {
            createUser(this.adminName, null, "Administrator");
        }
        catch (Exception e)
        {
            CATEGORY.error("Admin " + this.adminName + " failed.", e);
            this.adminName = null;
        }

        try
        {
            createUser(this.lpName, activities, "LocalizationParticipant");
        }
        catch (Exception e)
        {
            CATEGORY.error("LP " + this.lpName + " failed.", e);
            this.lpName = null;
        }

        CATEGORY.info("Done creating users for company " + this.companyName);
    }

    /**
     * <p>
     * Reads activities from recorded activities(<code>this.activities</code>)
     * according to names.
     * 
     * <p>
     * Please make sure that all activity names are exist, others will write a
     * error massage in log, and will not show any exceptions, sometimes this
     * error is not easy to find.
     * 
     * @param names
     *            Activity names, can't be null.
     * 
     * @return Required activities
     */
    private ArrayList<Activity> getActivitiesByName(String[] names)
    {

        ArrayList<Activity> activities = new ArrayList<Activity>();
        for (int i = 0; i < names.length; i++)
        {
            Activity activity = getActivityByName(PM_ACTIVITIES[i]);
            if (activity != null)
            {
                activities.add(activity);
            }
            else
            {
                CATEGORY.error("Could not get the activity:" + PM_ACTIVITIES[i]);
            }
        }

        return activities;
    }

    /**
     * <p>
     * Creates a workflow template.
     * 
     * <p>
     * <code>tasksInfo</code> and <code>arrowsInfo</code> include informations
     * about the template.
     * 
     * @param name
     *            The name of the template.
     * @param localePair
     * @param tasksInfo
     * @param arrowsInfo
     * 
     * @return The created workflow template.
     */
    private WorkflowTemplate createWfTemplate(String name,
            LocalePair localePair, String[][] tasksInfo, String[][] arrowsInfo)
    {
        WorkflowTemplate template = new WorkflowTemplate();
        template.setName(name);
        template.setDescription("");
        ArrayList<WorkflowTask> tasks = new ArrayList<WorkflowTask>();

        // Adds all task according to taskInfo.
        for (int i = 0; i < tasksInfo.length; i++)
        {
            String[] taskInfo = tasksInfo[i];
            WorkflowTask task = createWorkflowTask(localePair, taskInfo);
            task.setSequence(i);
            template.addWorkflowTask(task);
            tasks.add(task);
        }

        // Adds all arrows according to arrowsInfo.
        for (int i = 0; i < arrowsInfo.length; i++)
        {
            String[] arrowInfo = arrowsInfo[i];
            WorkflowTask sourceNode = (WorkflowTask) tasks.get(Integer
                    .parseInt(arrowInfo[2]));
            WorkflowTask targetNode = (WorkflowTask) tasks.get(Integer
                    .parseInt(arrowInfo[3]));
            template.addArrow(arrowInfo[0], Long.parseLong(arrowInfo[1]),
                    sourceNode, targetNode);
        }

        return template;
    }

    /**
     * <p>
     * Creates a workflow task. It is called by
     * <code>this.createWfTemplate</code>, more informations can see the
     * <code>this.createWfTemplate</code> method.
     * 
     * @param localePair
     * @param taskInfo
     * 
     * @return The created work low task.
     */
    private WorkflowTask createWorkflowTask(LocalePair localePair,
            String[] taskInfo)
    {
        String name = taskInfo[0];
        int type = Integer.parseInt(taskInfo[1]);
        Activity activity = getActivityByName(taskInfo[2]);
        int x = Integer.parseInt(taskInfo[3]) * this.X_RATIO;
        int y = (Integer.parseInt(taskInfo[4]) - 10) * this.Y_RATIO;

        // Condition node is not in centre.
        if (this.CONDITION_NODE.equals(name))
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

        task.setDisplayRoleName(this.DEFAULT_ROLE);
        task.setActionType(SystemAction.NO_ACTION);
        task.setConditionSpec(new WorkflowConditionSpec());

        // Adds roles if the task has activities.
        if (activity != null)
        {
            StringBuffer role = new StringBuffer();
            role.append(activity.getId()).append(" ")
                    .append(activity.getName()).append(" ")
                    .append(localePair.getSource()).append(" ")
                    .append(localePair.getTarget());
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
     * <p>
     * Creates a <code>WorkflowTemplateInfo</code>.
     * 
     * <p>
     * The manager is the created project manager created in
     * <code>this.addUsers()</code>.
     * 
     * <p>
     * The leverage locale name is same as the target locale name.
     * 
     * @param localePair
     * @param name
     *            The name of the <code>WorkflowTemplateInfo</code>.
     * 
     * @return The created <code>WorkflowTemplateInfo</code>.
     */
    private WorkflowTemplateInfo createWfti(LocalePair localePair, String name)
    {
        // Sets the manager.
        ArrayList<String> managerIds = new ArrayList<String>();
        if (this.pmName != null)
        {
            managerIds.add(UserUtil.getUserIdByName(this.pmName));
        }

        if (this.adminName != null)
        {
            managerIds.add(UserUtil.getUserIdByName(this.adminName));
        }

        // Sets leverage locale.
        Set<LeverageLocales> leveragingLocales = new HashSet<LeverageLocales>();
        LeverageLocales leverageLocale = new LeverageLocales(
                localePair.getTarget());
        leveragingLocales.add(leverageLocale);

        // Sets workflowtemplateInfo.
        WorkflowTemplateInfo wfti = new WorkflowTemplateInfo(name, "", project,
                true, managerIds, localePair.getSource(),
                localePair.getTarget(), EN_US_ENCODING, leveragingLocales);

        wfti.setWorkflowType(WorkflowTypeConstants.TYPE_TRANSLATION);

        // Sets company.
        wfti.setCompanyId(companyId);

        leverageLocale.setBackPointer(wfti);

        return wfti;
    }

    /**
     * <p>
     * Creates a workflow.
     * 
     * <p>
     * The real name is
     * <code>source locale name + "_" +  target locale name + nameSuffix</code>.
     * May be it is like <code>"en_US_fr_FR_T"</code>
     * 
     * @param nameSuffix
     *            It is used to get the name of workflow template info and
     *            template.
     *            <ul>
     *            <li><code>T</code> means Translation_Only</li> <li><code>TR
     *            </code> Translation_Review</li> <li><code>TRD</code>
     *            Translation_Review_DTP</li>
     *            </ul>
     * @param lPair
     * @param tasksInfo
     * @param arrowsInfo
     */
    private void createWorkflow(String nameSuffix, LocalePair lPair,
            String[][] tasksInfo, String[][] arrowsInfo)
    {
        // Gets real name.
        String name = lPair.getSource().toString() + "_"
                + lPair.getTarget().toString() + nameSuffix;

        WorkflowTemplateInfo wfti = createWfti(lPair, name);
        String[] wfManagerIds = new String[wfti.getWorkflowManagerIds().size()];
        wfManagerIds = (String[]) wfti.getWorkflowManagerIds().toArray(
                wfManagerIds);
        WorkflowTemplate wfTemplate = createWfTemplate(name, lPair, tasksInfo,
                arrowsInfo);
        WorkflowOwners orwners = new WorkflowOwners(wfti.getProjectManagerId(),
                wfManagerIds);

        try
        {
            WorkflowTemplate wft = ServerProxy.getWorkflowServer()
                    .createWorkflowTemplate(wfTemplate, orwners);
            wfti.setWorkflowTemplate(wft);
            ServerProxy.getProjectHandler().createWorkflowTemplateInfo(wfti);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Creates a workflow, the nameSuffix is <code>_T</code>, the tasksInfo is
     * <code>this.TO_TASKS</code>, and the arrowsInfo is
     * <code>this.TO_ARROWS</code>
     * 
     * @param localePair
     */
    private void createToWorkflow(LocalePair localePair)
    {
        this.createWorkflow("_T", localePair, this.TO_TASKS, this.TO_ARROWS);
    }

    /**
     * Creates a workflow, the nameSuffix is <code>_TR</code>, the tasksInfo is
     * <code>this.TR_TASKS</code>, and the arrowsInfo is
     * <code>this.TR_ARROWS</code>
     * 
     * @param localePair
     */
    private void createTRWorkflow(LocalePair localePair)
    {
        createWorkflow("_TR", localePair, this.TR_TASKS, this.TR_ARROWS);
    }

    /**
     * Creates a workflow, the nameSuffix is <code>_TRD</code>, the tasksInfo is
     * <code>this.TRD_TASKS</code>, and the arrowsInfo is
     * <code>this.TRD_ARROWS</code>
     * 
     * @param localePair
     */
    private void createTRDWorkflow(LocalePair localePair)
    {
        createWorkflow("_TRD", localePair, this.TRD_TASKS, this.TRD_ARROWS);
    }

    /**
     * Creates all workflows for the new company.
     */
    private void createWorkflows()
    {
        CATEGORY.info("Creating workflows for company " + this.companyName);

        if (this.project == null)
        {
            CATEGORY.info("Could not create workflows because project is null");
            return;
        }

        for (int i = 0; i < this.localePairs.size(); i++)
        {
            LocalePair localePair = (LocalePair) this.localePairs.get(i);
            createToWorkflow(localePair);
            createTRWorkflow(localePair);
            createTRDWorkflow(localePair);
        }

        CATEGORY.info("Done creating Workflows for company " + this.companyName);
    }

    /**
     * Creates a default user.
     * 
     * @param name
     *            User name.
     * @param activities
     *            A ArrayList of Activity. If it is null or the size is 0, will
     *            not create roles for the user.
     * @param permGroupName
     *            The name of the user's PermissionGroup.
     * 
     * @throws EnvoyServletException
     * @throws RemoteException
     * @throws LocaleManagerException
     */
    private void createUser(String name, ArrayList<Activity> activities,
            String permGroupName) throws EnvoyServletException,
            LocaleManagerException, RemoteException
    {
        if (name == null)
        {
            return;
        }

        UserManager userMgr = ServerProxy.getUserManager();
        CreateUserWrapper wrapper = new CreateUserWrapper(userMgr,
                this.createUser);

        // Sets default informations
        wrapper.setUserId(UserUtil.newUserId(name));
        wrapper.setUserName(name);
        wrapper.setFirstName(name);
        wrapper.setLastName(name);
        wrapper.setCompanyName(this.companyName);
        wrapper.setPassword(this.DEFAULT_PASSWORD);
        wrapper.setEmail(this.DEFAULT_EMAIL);
        wrapper.setDefaultUILocale(this.DEFAULT_UI_LOCALE);
        wrapper.setCurCompanyId(Long.toString(this.companyId));

        // Adds roles
        Hashtable<Activity, Vector<String>> activityCostMap = new Hashtable<Activity, Vector<String>>();
        Vector<String> params = new Vector<String>();
        params.addElement("true");
        params.addElement("-1");
        if (activities != null && activities.size() > 0)
        {
            for (int i = 0; i < activities.size(); i++)
            {
                Activity activity = (Activity) activities.get(i);
                activityCostMap.put(activity, params);
            }

            LocaleManagerWLRemote localeMgr = ServerProxy.getLocaleManager();
            long srcId = this.EN_US_LOCALE_ID;

            GlobalSightLocale srcLocale = localeMgr.getLocaleById(srcId);
            String sSrcLocale = srcLocale.getLocale().toString();

            for (int j = 0; j < LOCALE_TO.length; j++)
            {
                long targId = LOCALE_TO[j];
                GlobalSightLocale targLocale = localeMgr.getLocaleById(targId);
                String sTargLocale = targLocale.getLocale().toString();
                wrapper.addUserRoles(sSrcLocale, sTargLocale, activityCostMap);
            }

        }

        wrapper.setIsInAllProjects(true);

        // Adds field security
        wrapper.setFieldSecurity(new UserFieldSecurity());

        // Adds calendar
        FluxCalendar baseCal = CalendarHelper.getDefaultCalendar(Long
                .toString(companyId));
        UserFluxCalendar cal = new UserFluxCalendar(baseCal.getId(),
                wrapper.getUserId(), baseCal.getTimeZoneId());
        CalendarHelper.updateUserCalFieldsFromBase(baseCal, cal);
        wrapper.setCalendar(cal);

        wrapper.commitWrapper();

        // Adds permission group
        PermissionManager manager = Permission.getPermissionManager();
        ArrayList<String> users = new ArrayList<String>(1);
        users.add(wrapper.getUserId());

        Collection<PermissionGroup> allPermGroups = Permission
                .getPermissionManager().getAllPermissionGroupsByCompanyId(
                        Long.toString(companyId));
        Iterator<PermissionGroup> iter = allPermGroups.iterator();
        while (iter.hasNext())
        {
            PermissionGroup permGroup = (PermissionGroup) iter.next();
            if (permGroupName.equals(permGroup.getName()))
            {
                manager.mapUsersToPermissionGroup(users, permGroup);
                break;
            }
        }
    }

    /**
     * Gets a activity according to the name. If the activity is not exist, will
     * return null, please take care about it.
     * 
     * @param name
     *            The name of the activity.
     * 
     * @return The found activity.
     */
    private Activity getActivityByName(String name)
    {
        if (name == null)
        {
            return null;
        }

        // Finds the activity in this.activities.
        if (this.activities != null && this.activities.size() > 0)
        {
            String realName = name + "_" + this.companyId;
            for (int i = 0; i < this.activities.size(); i++)
            {

                Activity activity = (Activity) this.activities.get(i);
                if (realName.equals(activity.getActivityName()))
                {
                    return activity;
                }
            }
        }

        // If not found, return null.
        CATEGORY.error("The activity(" + name + ") could not be found");

        return null;
    }

    /**
     * <p>
     * Creates a default project for the new company.
     * 
     * <p>
     * The project manager is the created project manager created in
     * <code>this.addUsers()</code>.
     * 
     * <p>
     * The name of the default project is <code>this.DEFAULT_PROJECT_NAME</code>
     */
    private void createProject() throws Exception
    {
        CATEGORY.info("Creating project for company " + this.companyName);

        if (this.pmName == null)
        {
            CATEGORY.error("Could not create project because the project manager hasn't been found");
            return;
        }

        Project project = ProjectHandlerHelper.createProject();
        project.setCompanyId(this.companyId);
        project.setDescription("");
        project.setTermbaseName("");
        project.setName(DEFAULT_PROJECT_NAME);
        project.setQuotePerson(null);

        User pm = ServerProxy.getUserManager().getUserByName(this.pmName);
        project.setProjectManager(pm);

        // Adds default users for the project.
        TreeSet<String> users = new TreeSet<String>();
        if (this.pmName != null)
        {
            users.add(UserUtil.getUserIdByName(this.pmName));
        }
        if (this.adminName != null)
        {
            users.add(UserUtil.getUserIdByName(this.adminName));
        }
        if (this.lpName != null)
        {
            users.add(UserUtil.getUserIdByName(this.lpName));
        }

        project.setUserIds(users);

        ProjectHandlerHelper.addProject(project);
        this.project = project;

        CATEGORY.info("Done creating project for company " + this.companyName);
    }

    /**
     * Sets default value of params.
     * 
     * @param message
     */
    private void init(Message message)
    {
        try
        {
            ArrayList<?> msg = (ArrayList<?>) ((ObjectMessage) message)
                    .getObject();
            this.companyName = (String) msg.get(0);
            this.companyId = Long.parseLong((String) msg.get(1));
            this.createUser = (User) msg.get(2);
            this.project = null;
            this.localePairs = null;
            this.activities = null;
            this.pmName = null;
            this.adminName = null;
            this.lpName = null;

            CompanyThreadLocal.getInstance().setValue(this.companyName);
        }
        catch (JMSException e)
        {
            CATEGORY.error("Could not get params form jms message.", e);
        }
    }

    /**
     * <p>
     * Listens message and add default items.
     * 
     * <p>
     * These params info is needed in the message.
     * <ul>
     * <li>String companyName : The name of the new company.</li>
     * <li>String companyId : The id of the new company.</li>
     * <li>User createUser : The user create the company.</li>
     * </ul>
     * 
     * @param message
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message message)
    {
        try
        {
            if (message.getJMSRedelivered())
            {
                CATEGORY.warn("Ignoring duplicate JMS message.");
                return;
            }

            synchronized (SetupCompanyMDB.class)
            {
                init(message);
                addDefaultItems();
            }
        }
        catch (Exception oe)
        {
            CATEGORY.error("Failed to add default items for new company "
                    + this.companyName, oe);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    /**
     * Gets name of project manager.
     * 
     * @return name if the default name is existed, will return null.
     */
    private String getPmName()
    {
        String name = "gs" + this.companyName + "pm";

        return getRealName(name);
    }

    /**
     * Gets name of admin.
     * 
     * @return name if the default name is existed, will return null.
     */
    private String getAdminName()
    {
        String name = this.companyName + "admin";

        return getRealName(name);
    }

    /**
     * Gets name of localizition participant.
     * 
     * @return name if the default name is existed, will return null.
     */
    private String getLpName()
    {
        String name = this.companyName + "anyone";

        return getRealName(name);
    }

    /**
     * Judges the name exist or not.
     * 
     * @param name
     * 
     * @return the name exist or not
     */
    private boolean isNameExist(String name)
    {
        String[] userNames = UserHandlerHelper.getAllUserNames();

        for (int i = 0; i < userNames.length; i++)
        {
            // The name has exist.
            if (name.equalsIgnoreCase(userNames[i]))
            {
                CATEGORY.error("Can not create " + name + " because " + name
                        + " already exists in the system");

                return true;
            }
        }

        return false;
    }

    /**
     * Gets a user name that not existed is database.
     * 
     * @param name
     * @return name
     */
    private String getRealName(String name)
    {
        String name1 = name.toLowerCase().replaceAll(" ", "");
        String name2 = name1;
        int i = 1;

        while (isNameExist(name2))
        {
            name2 = name1 + i;
            i++;
        }

        return name2;
    }
}
