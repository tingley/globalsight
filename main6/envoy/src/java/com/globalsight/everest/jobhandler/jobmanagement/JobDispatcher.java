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
package com.globalsight.everest.jobhandler.jobmanagement;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.calendar.FluxCalendar;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.costing.BigDecimalHelper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.costing.FlatSurcharge;
import com.globalsight.everest.costing.PercentageSurcharge;
import com.globalsight.everest.foundation.DispatchCriteria;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.UserImpl;
import com.globalsight.everest.foundation.VolumeOfData;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobEventObserver;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.statistics.StatisticsService;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowManager;
import com.globalsight.log.GlobalSightCategory;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.scheduling.EventSchedulerHelper;
import com.globalsight.scheduling.FluxEventMap;
import com.globalsight.scheduling.SchedulerConstants;
import com.globalsight.scheduling.SchedulingInformation;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.date.DateHelper;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.mail.MailerHelper;
import com.globalsight.util.resourcebundle.LocaleWrapper;

/**
 * This class is used to dispatch a single job. It obtains the various states of
 * a job and performs actions based on the next state. It also creates a job
 * timer if necessary.
 */
public class JobDispatcher
{
    private static final Logger c_category = Logger
            .getLogger(JobDispatcher.class);

    private static final String DISPATCH_FAILURE = "jobDispatchFailure";

    private static final String CANCEL_FAILURE = "jobCancelFailure";

    private static final String ANALYZE_PROCEDURE = "ANALYZE_SYS4";

    // determines whether the system-wide notification is enabled
    private boolean m_systemNotificationEnabled = EventNotificationHelper
            .systemNotificationEnabled();

    /**
     * Constructor creates the TimerServices
     */
    public JobDispatcher(Job p_job)
    {
        Session session = HibernateUtil.getSession();
        JobImpl job = (JobImpl) session.get(JobImpl.class,
                new Long(p_job.getId()));
        if (job.getL10nProfile() == null)
        {
            return;
        }
        DispatchCriteria dispatchCriteria = job.getL10nProfile()
                .getDispatchCriteria();

        int condition = dispatchCriteria.getCondition();

        if (c_category.isDebugEnabled())
        {
            c_category.debug("Constructor: " + " p_job.getId()=" + job.getId()
                    + " dispatchCriteria=" + dispatchCriteria.toString());
        }

        // The OR condition - The word count or the timer option has
        // been chosen
        if (condition == DispatchCriteria.WORD_COUNT_OR_TIMER_CONDITION)
        {
            createTimerServices(job);
        }
        // session.close();
    }

    private void createTimerServices(Job p_job)
    {
        try
        {
            TimedDispatch td = new TimedDispatch(p_job, p_job.getL10nProfile()
                    .getDispatchCriteria().getTimeCondition());

            long objId = p_job.getId();
            Integer objType = SchedulerConstants.getKeyForClass(Job.class);
            Integer eventTypeKey = SchedulerConstants
                    .getKeyForType(SchedulerConstants.TIMED_JOB_DISPATCH_TYPE);
            HashMap eventInfo = new HashMap();
            eventInfo.put("timedDispatch", td);
            Date startDate = td.getStartDate();
            c_category.debug("Time is now  :" + new java.util.Date());
            c_category.debug("timer expires:" + startDate);
            SchedulingInformation schedulingInformation = new SchedulingInformation();
            schedulingInformation.setListener(TimedDispatchEventHandler.class);
            schedulingInformation.setStartDate(startDate);
            String recurrance = "+3600s";
            int repeatCount = 2;
            c_category.debug("Using recurrance '" + recurrance
                    + "' and repeat: " + repeatCount);
            schedulingInformation.setRecurranceExpression(recurrance);
            schedulingInformation.setRepeatCount(repeatCount);
            schedulingInformation.setEventInfo(eventInfo);
            schedulingInformation
                    .setEventTypeName(SchedulerConstants.TIMED_JOB_DISPATCH_TYPE);
            schedulingInformation.setEventType(eventTypeKey.intValue());
            schedulingInformation.setObjectId(objId);
            schedulingInformation.setObjectType(objType.intValue());

            c_category.debug("calling scheduler...");
            ServerProxy.getEventScheduler()
                    .scheduleEvent(schedulingInformation);
            c_category.debug("scheduled...");
        }
        catch (Exception gstte)
        {
            c_category.error("Cannot create dispatch timer", gstte);
        }
    }

    /**
     * This method returns the next state of the job as set in the transitions
     * table All possible states of the job are known in advance and stored in a
     * table Based on the next state a series of actions are performed
     */
    private String getJobState(Job p_job)
    {
        return p_job.getState() + "_" + p_job.getDispatchType();
    }

    /**
     * This method is used to determine a word count completion event
     * 
     * @param Job
     *            p_job
     * @throws JobException
     */
    public void wordCountIncreased(Job p_job) throws JobException
    {
        VolumeOfData vod = getCriteria(p_job).getVolumeOfDataCondition();
        long volume = vod.getVolume();

        try
        {
            if (c_category.isDebugEnabled())
            {
                c_category.debug("wordCountIncreased :" + " p_job.getId()="
                        + p_job.getId() + " vod="
                        + (vod != null ? vod.toString() : "null")
                        + " p_job.getWordCount()="
                        + Integer.toString(p_job.getWordCount()));
            }

            if (p_job.getWordCount() >= volume)
            {
                p_job.setWordCountReached(true);
                HibernateUtil.update((JobImpl) p_job);
                scheduleJob(p_job);
            }
            else
            {
                HibernateUtil.update((JobImpl) p_job);
            }
        }
        catch (Exception e)
        {
            throw new JobException(
                    JobException.MSG_FAILED_TO_UPDATE_WORDCOUNT_REACHED, null,
                    e, JobException.PROPERTY_FILE_NAME);
        }
    }

    /**
     * This method is used for call back purposes. When a timer fires an event
     * it calls JobDispatchEngine and JobDispatchEngine calls timeTriggerEvent.
     * It no longer unschedules the job as that is handled by the
     * TimedDispatchEventHandler
     * 
     * @param Job
     *            p_job
     * @throws JobException
     */
    public void timerTriggerEvent(Job p_job) throws JobException
    {
        scheduleJob(p_job);
    }

    public void dispatchBatchJob(Job p_job) throws JobException,
            RemoteException
    {
        try
        {
            if (p_job.getState().equals(Job.BATCHRESERVED))
            {
                getJobEventObserver().notifyJobPendingEvent(p_job);
            }

            scheduleJob(p_job);
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            JobImpl job = (JobImpl) p_job;
            args[0] = job.getIdAsLong().toString();
            c_category.error("Failed to dispatch batch job : " + p_job.getId(),
                    e);
            throw new JobException(JobException.MSG_FAILED_TO_GET_JOB_BY_ID,
                    args, e, JobException.PROPERTY_FILE_NAME);
        }
    }

    public void dispatchJob(Job p_job) throws JobException
    {
        dispatchWorkflows(p_job);
    }

    /**
     * Cancel the workflows in the job that are of the specified state. Or if
     * the state is null cancel all the workflows and the job.
     */
    public void cancelJob(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws JobException
    {
        validateStateOfPagesInJob(p_job);
        cancelWorkflows(p_idOfUserRequestingCancel, p_job, p_state);
        destroyTimer(p_job);
    }

    /**
     * Cancel the workflows in the job that are of the specified state. Or if
     * the state is null cancel all the workflows and the job. this takes care
     * of cancellation for reimport page
     */
    public void cancelJob(String p_idOfUserRequestingCancel, Job p_job,
            String p_state, boolean p_reimport) throws JobException
    {
        validateStateOfPagesInJob(p_job);

        cancelWorkflows(p_idOfUserRequestingCancel, p_job, p_state, p_reimport);

        destroyTimer(p_job);
    }

    /**
     * Cancel the workflows associated with the given state OR pass NULL for all
     * states and NULL.
     */
    private void cancelWorkflows(String p_idOfUserRequestingCancel, Job p_job,
            String p_state, boolean p_reimport) throws JobException
    {
        try
        {
            getWorkflowManager().cancel(p_idOfUserRequestingCancel, p_job,
                    p_state, p_reimport);
        }
        catch (Exception e)
        {
            // c_category.error("Failure to cancel a workflow", e);

            c_category.error("Failure to cancel a workflow " + e.toString()
                    + GlobalSightCategory.getLineContinuation() + "p_job="
                    + (p_job != null ? p_job.toString() : "null"), e);

            sendEmail(p_job, MailerConstants.CANCEL_FAILURE_SUBJECT,
                    CANCEL_FAILURE);

            throw new JobException(JobException.MSG_FAILED_TO_CANCEL_WORKFLOW,
                    null, e, JobException.PROPERTY_FILE_NAME);
        }
    }

    /**
     * This method obtains the next state of a job based on the current state
     * and then performs a series of actions based on the next state.
     */
    private void scheduleJob(Job p_job) throws JobException
    {
        String jobState = getJobState(p_job);

        // if the job is in a manual dispatch state, do not update any states.
        // this will prevent a multi-thread state update
        if (jobState.equals(Job.READY_TO_BE_DISPATCHED + "_" + Job.MANUAL))
        {
            return;
        }

        HashMap<String, String> stateTransitions = JobDispatchEngineLocal
                .getJobStateTransitions();
        String nextState = stateTransitions.get(jobState);

        if (c_category.isDebugEnabled())
        {
            c_category.debug("The next state of job is " + nextState
                    + " current state is " + jobState + " p_job.getId()="
                    + p_job.getId());
        }

        Job job = JobCreationMonitor.loadJobFromDB(p_job.getId());

        calculateWordCounts(job);
        if (Job.DISPATCHED.equals(nextState))
        {
         // do not automatic dispatch job for AEM jobs
            if (job.hasSetCostCenter()
                    && !"aem_gs_translator".equals(job.getJobType()))
            {
                toDispatch(job);
            }
            else
            {
                toReady(job);
            }
        }
        else if ((Job.READY_TO_BE_DISPATCHED + "_" + Job.MANUAL)
                .equals(nextState))
        {
            toReady(job);
        }
    }

    /**
     * Calculates target page and workflow related word counts for the job.
     */
    private void calculateWordCounts(Job job)
    {
        JobCreationMonitor.updateJobState(job, Job.CALCULATING_WORD_COUNTS);
        c_category.info("Calculating word counts for job " + job.getJobId());

        Vector<String> jobExcludeTypes = job.getL10nProfile()
                .getTranslationMemoryProfile().getJobExcludeTuTypes();
        for (Workflow workflow : job.getWorkflows())
        {
            StatisticsService.calculateTargetPagesWordCount(workflow,
                    jobExcludeTypes);
        }

        StatisticsService.calculateWorkflowStatistics(
                new ArrayList(job.getWorkflows()), jobExcludeTypes);

        c_category.info("Done calculating word counts for job "
                + job.getJobId());
    }

    private void toDispatch(Job job)
    {
        calculateCost(job);
        try
        {
            sendEmailForJobImportSucc(job);
        }
        catch (Exception e)
        {
            c_category
                    .error("Failed to send notification for creating job successfully",
                            e);
        }
        dispatchWorkflows(job);
        destroyTimer(job);
        // Auto accept the activity/task of Job.
        TaskHelper.autoAcceptTaskInJob(job);
    }

    private void toReady(Job job)
    {
        try
        {
            getJobEventObserver().notifyJobReadyToBeDispatchedEvent(job);
        }
        catch (Exception e)
        {
            c_category.error(e.getMessage(), e);
        }

        calculateCost(job);
        calculateEstimatedCompletionDate(job);
        try
        {
            sendEmailForJobImportSucc(job);
        }
        catch (Exception e)
        {
            c_category
                    .error("Failed to send notification for creating job successfully",
                            e);
        }
        sendEmail(job, MailerConstants.DISPATCH_SUBJECT,
                MailerConstants.DISPATCH_MESSAGE);
        destroyTimer(job);
    }

    /**
     * This method dispatches all the workflows of a job.
     * 
     * @param Job
     *            p_job
     */
    private void dispatchWorkflows(Job p_job) throws JobException
    {
        try
        {
            getWorkflowManager().dispatch(p_job);
        }
        catch (Exception e)
        {
            c_category.error("Cannot dispatch a Job " + e.toString()
                    + GlobalSightCategory.getLineContinuation() + "p_job="
                    + p_job, e);

            sendEmail(p_job, MailerConstants.DISPATCH_FAILURE_SUBJECT,
                    DISPATCH_FAILURE);

            throw new JobException(JobException.MSG_WORKFLOWMANAGER_FAILURE,
                    null, e, JobException.PROPERTY_FILE_NAME);
        }
    }

    // perform the cost calculation after a job is created.
    private void calculateCost(Job p_job)
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            /**
             * This is JMS implementation for calling calculate cost This is
             * commented out currently as it's causing deadlock and a unique
             * constraint violation This is replaced by a non-jms call below.
             * 
             * if
             * (sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED)
             * == true) { // Calculate the Cost of the Job. Currency c =
             * ServerProxy.getCostingEngine().getPivotCurrency(); String curr =
             * c.getIsoCode(); Currency oCurrency =
             * ServerProxy.getCostingEngine().getCurrency(curr); // Calculate
             * Expenses CostCalculator calculator = new
             * CostCalculator(job.getId(), oCurrency, true, Cost.EXPENSE);
             * calculator.sendToCalculateCost(); if
             * (sc.getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED)
             * == true) { // Calculate Revenue calculator = new
             * CostCalculator(job.getId(), oCurrency, true, Cost.REVENUE);
             * calculator.sendToCalculateCost(); } }
             */
            if (sc.getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED))
            {
                // Calculate the Cost of the Job.
                Currency c = ServerProxy.getCostingEngine().getPivotCurrency();
                String curr = c.getIsoCode();
                Currency oCurrency = ServerProxy.getCostingEngine()
                        .getCurrency(curr);

                // Calculate Expenses
                Cost cost = ServerProxy.getCostingEngine().calculateCost(p_job,
                        oCurrency, true, Cost.EXPENSE);

                if (sc.getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED))
                {
                    // Calculate Revenues
                    cost = ServerProxy.getCostingEngine().calculateCost(p_job,
                            oCurrency, true, Cost.REVENUE);

                    float PMCost = p_job.getL10nProfile().getProject()
                            .getPMCost();
                    PercentageSurcharge percentageSurcharge = new PercentageSurcharge(
                            PMCost);
                    percentageSurcharge.setName("PM Cost");
                    cost = ServerProxy.getCostingEngine().addSurcharge(
                            cost.getId(), percentageSurcharge, Cost.REVENUE);

                    // For "Additional functionality quotation" issue
                    calculateAdditionalSurCharges(cost, p_job, oCurrency);
                }
            }
        }
        catch (Exception e)
        {
            // log there was a costing error - but shouldn't stop the job from
            // moving along.
            c_category.error(
                    "Error occurred while calculating cost for job with id"
                            + p_job.getId(), e);
        }
    }

    // For sla issue
    // Calculate the ETCD & ECD while the job is in ready status.
    private void calculateEstimatedCompletionDate(Job p_job)
    {
        JobImpl jobClone = null;
        Session session = null;
        Transaction transaction = null;
        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            jobClone = (JobImpl) session.get(JobImpl.class,
                    new Long(p_job.getId()));
            Iterator it = jobClone.getWorkflows().iterator();

            while (it.hasNext())
            {
                Workflow wfClone = (Workflow) it.next();
                if (!Workflow.READY_TO_BE_DISPATCHED.equals(wfClone.getState()))
                {
                    continue;
                }

                List wfTaskInfos = ServerProxy.getWorkflowServer()
                        .timeDurationsInDefaultPath(null, wfClone.getId(), -1);
                FluxCalendar defaultCalendar = ServerProxy.getCalendarManager()
                        .findDefaultCalendar(
                                String.valueOf(wfClone.getCompanyId()));

                Hashtable tasks = wfClone.getTasks();
                long translateDuration = 0l;
                long workflowDuration = 0l;

                for (int i = 0; i < wfTaskInfos.size(); i++)
                {
                    WfTaskInfo wfTaskInfo = (WfTaskInfo) wfTaskInfos.get(i);
                    TaskImpl task = (TaskImpl) tasks.get(new Long(wfTaskInfo
                            .getId()));
                    if (task == null)
                        continue;

                    workflowDuration += wfTaskInfo.getTotalDuration();
                    Activity act = ServerProxy.getJobHandler().getActivity(
                            task.getTaskName());

                    if (Activity.isTranslateActivity(act.getType()))
                    {
                        translateDuration = workflowDuration;
                    }
                }

                wfClone.setEstimatedTranslateCompletionDate(ServerProxy
                        .getEventScheduler().determineDate(
                                jobClone.getCreateDate(), defaultCalendar,
                                translateDuration));
                wfClone.setEstimatedCompletionDate(ServerProxy
                        .getEventScheduler().determineDate(
                                jobClone.getCreateDate(), defaultCalendar,
                                workflowDuration));
                session.update(wfClone);
            }
            transaction.commit();
        }
        catch (Exception e2)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            c_category
                    .error("Failed to calculate ETCD & ECD: "
                            + p_job.getJobName(), e2);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
    }

    /**
     * Cancel the workflows associated with the given state OR pass NULL for all
     * states.
     */
    private void cancelWorkflows(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws JobException
    {
        try
        {
            getWorkflowManager().cancel(p_idOfUserRequestingCancel, p_job,
                    p_state);
        }
        catch (Exception e)
        {
            // c_category.error("Failure to cancel a workflow", e);

            c_category.error("Failure to cancel a workflow: " + e.toString()
                    + GlobalSightCategory.getLineContinuation() + "p_job="
                    + p_job, e);

            sendEmail(p_job, MailerConstants.CANCEL_FAILURE_SUBJECT,
                    CANCEL_FAILURE);

            throw new JobException(JobException.MSG_FAILED_TO_CANCEL_WORKFLOW,
                    null, e, JobException.PROPERTY_FILE_NAME);
        }
    }

    public void destroyTimer(Job p_job)
    {
        if (getCriteria(p_job).getCondition() == DispatchCriteria.WORD_COUNT_OR_TIMER_CONDITION)
        {
            try
            {
                // first find the timer from the flux_event_map
                FluxEventMap fem = EventSchedulerHelper
                        .findFluxEventMap(
                                SchedulerConstants
                                        .getKeyForType(SchedulerConstants.TIMED_JOB_DISPATCH_TYPE),
                                SchedulerConstants.getKeyForClass(Job.class),
                                new Long(p_job.getId()));

                // cancel the timer
                ServerProxy.getEventScheduler().unschedule(fem);
            }
            catch (Exception gstte)
            {
                c_category.error("Cannot cancel timer", gstte);
            }
        }
    }

    /**
     * Get the No Matches and Repetition Word Count of Job.
     * 
     * @param p_user
     *            uploader
     * @param p_job
     *            job data.
     */
    private String getNoMatchesAndRepetitionsWordCount(User p_user, Job p_job)
    {
        StringBuffer result = new StringBuffer();
        try
        {
            PermissionSet ps = Permission.getPermissionManager()
                    .getPermissionSetForUser(p_user.getUserId());
            boolean isNoMatches = ps
                    .getPermissionFor(Permission.ACCOUNT_NOTIFICATION_NOMATCHES);
            boolean isRepetitions = ps
                    .getPermissionFor(Permission.ACCOUNT_NOTIFICATION_REPETITIONS);
            if (!isNoMatches && !isRepetitions)
            {
                return result.toString();
            }

            Iterator<Workflow> it = p_job.getWorkflows().iterator();
            if (it.hasNext())
            {
                Workflow wf = it.next();
                if (isNoMatches)
                {
                    result.append("No Matches: ")
                            .append(wf.getThresholdNoMatchWordCount())
                            .append("\r\n");
                }

                if (isRepetitions)
                {
                    int repetitionCount = wf.getRepetitionWordCount();
                    result.append("Repetitions: ").append(repetitionCount)
                            .append("\r\n");
                }
            }
        }
        catch (Exception e)
        {
        }

        return result.toString();
    }

    /**
     * Sends the notification for creating job successfully
     */
    private void sendEmailForJobImportSucc(Job p_job) throws Exception
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        String companyIdStr = String.valueOf(p_job.getCompanyId());
        Project project = ServerProxy.getProjectHandler().getProjectById(
                p_job.getProjectId());
        GlobalSightLocale sourceLocale = p_job.getSourceLocale();
        Date createDate = p_job.getCreateDate();
        User jobUploader = p_job.getCreateUser();
        String fpNames = "";
        List<FileProfile> fps = p_job.getAllFileProfiles();
        Set<String> fpSet = new HashSet<String>();
        for (FileProfile fp : fps)
        {
            String tempFPName = fp.getName();
            boolean isXLZ = ServerProxy.getFileProfilePersistenceManager()
                    .isXlzReferenceXlfFileProfile(tempFPName);
            if (isXLZ)
            {
                tempFPName = tempFPName.substring(0, tempFPName.length() - 4);
            }
            fpSet.add(tempFPName);
        }
        fpNames = fpSet.toString();
        fpNames = fpNames.substring(1, fpNames.length() - 1);
        //get Job Comments
        String comments = MailerHelper.getJobCommentsByJob(p_job);
        
        String messageArgs[] = new String[10];
        messageArgs[0] = Long.toString(p_job.getId());
        messageArgs[1] = p_job.getJobName();
        messageArgs[2] = project.getName();
        messageArgs[3] = sourceLocale.getDisplayName();
        messageArgs[4] = String.valueOf(p_job.getWordCount());
        messageArgs[6] = jobUploader.getSpecialNameForEmail();
        messageArgs[7] = fpNames;
        messageArgs[8] = getNoMatchesAndRepetitionsWordCount(jobUploader, p_job);
        messageArgs[9] = comments;
        
        String subject = MailerConstants.JOB_IMPORT_SUCC_SUBJECT;
        String message = MailerConstants.JOB_IMPORT_SUCC_MESSAGE;
        List<String> receiverList = new ArrayList<String>();
        receiverList.add(jobUploader.getUserId());
        String managerID = project.getProjectManagerId();
        if (null != managerID && !receiverList.contains(managerID))
        {
            receiverList.add(managerID);
        }

        for (int i = 0; i < receiverList.size(); i++)
        {
            User receiver = (User) ServerProxy.getUserManager().getUser(
                    receiverList.get(i));
            messageArgs[5] = DateHelper.getFormattedDateAndTimeFromUser(
                    createDate, receiver);
            ServerProxy.getMailer().sendMailFromAdmin(receiver, messageArgs,
                    subject, message, companyIdStr);
        }
    }

    private void sendEmail(Job p_job, String p_subject, String p_message)
            throws JobException
    {
        if (!m_systemNotificationEnabled)
        {
            return;
        }

        L10nProfile l10nProfile = p_job.getL10nProfile();
        long projectId = l10nProfile.getProjectId();
        String capLoginUrl = null;
        User user = null;

        try
        {
            ProjectHandler projectHandler = getProjectHandler();
            Project project = projectHandler.getProjectById(projectId);
            SystemConfiguration config = SystemConfiguration.getInstance();
            capLoginUrl = config
                    .getStringParameter(SystemConfigParamNames.CAP_LOGIN_URL);

            String[] msgArgs = new String[7];
            msgArgs[0] = p_job.getJobName();
            msgArgs[1] = Long.toString(p_job.getDuration());
            msgArgs[3] = Integer.toString(p_job.getWordCount());
            msgArgs[4] = capLoginUrl;
            msgArgs[5] = Integer.toString(p_job.getPriority());
            msgArgs[6] = Long.toString(p_job.getId());

            // first go thru all wftInfos and send an email to WFM (if any)
            GlobalSightLocale[] targetLocales = l10nProfile.getTargetLocales();
            boolean shouldNotifyPm = false;

            List<String> mailList = new ArrayList<String>();
            for (int i = 0; i < targetLocales.length; i++)
            {
                WorkflowTemplateInfo wfti = l10nProfile
                        .getWorkflowTemplateInfo(targetLocales[i]);
                if (!shouldNotifyPm && wfti.notifyProjectManager())
                {
                    shouldNotifyPm = true;
                }
                List userIds = wfti.getWorkflowManagerIds();
                mailList.addAll(userIds);
                if (userIds != null)
                {
                    for (Iterator uii = userIds.iterator(); uii.hasNext();)
                    {
                        String userId = (String) uii.next();
                        sendMailFromAdmin(userId, p_job, msgArgs, p_subject,
                                p_message);
                    }
                }

            }

            // if at least one of the wfInfos had the pm notify flag on, notify
            // PM.
            String pmUserId = project.getProjectManagerId();
            if (shouldNotifyPm && !mailList.contains(pmUserId))
            {
                sendMailFromAdmin(pmUserId, p_job, msgArgs, p_subject,
                        p_message);
            }
        }
        catch (Exception e)
        {
            c_category.error(
                    "Unable to send email after a dispatch for Job id: "
                            + p_job.getId()
                            + " capLoginUrl="
                            + capLoginUrl
                            + " user="
                            + (user != null ? ((UserImpl) user).toDebugString()
                                    : "null") + " p_subject=" + p_subject
                            + " p_message=" + p_message, e);
        }
    }

    // send email from Admin
    private void sendMailFromAdmin(String p_userId, Job p_job,
            String[] p_msgArgs, String p_subject, String p_message)
            throws Exception
    {
        User user = ServerProxy.getUserManager().getUser(p_userId);
        String userLocale = user.getDefaultUILocale();
        Locale locale = LocaleWrapper.getLocale(userLocale);
        DateFormat dateformat = DateFormat.getDateInstance(DateFormat.MEDIUM,
                locale);
        p_msgArgs[2] = dateformat.format(p_job.getCreateDate());

        ServerProxy.getMailer().sendMailFromAdmin(user, p_msgArgs, p_subject,
                p_message, String.valueOf(p_job.getCompanyId()));
    }

    private WorkflowManager getWorkflowManager() throws Exception
    {
        return ServerProxy.getWorkflowManager();
    }

    private JobEventObserver getJobEventObserver() throws Exception
    {
        return ServerProxy.getJobEventObserver();
    }

    private ProjectHandler getProjectHandler() throws Exception
    {
        return ServerProxy.getProjectHandler();
    }

    private L10nProfile getProfile(Job p_job)
    {
        return p_job.getL10nProfile();
    }

    private DispatchCriteria getCriteria(Job p_job)
    {
        return getProfile(p_job).getDispatchCriteria();
    }

    /**
     * Make sure no page is in UPDATING state for the given job.
     */
    private void validateStateOfPagesInJob(Job p_job) throws JobException
    {
        try
        {
            PageStateValidator.validateStateOfPagesInJob(p_job);
        }
        catch (Exception e)
        {
            throw new JobException(e);
        }
    }

    // For "Additional functionality quotation" issue
    private void calculateAdditionalSurCharges(Cost cost, Job p_job,
            Currency oCurrency)
    {
        HashMap surchanges = getAdditionalSurcharges();
        int fileCounts = p_job.getSourcePages().size();
        Iterator it = surchanges.keySet().iterator();
        while (it.hasNext())
        {
            String surchargeName = it.next().toString();
            Object surchargePerValueObj = surchanges.get(surchargeName);

            float surchargePerValue = Float.parseFloat(surchargePerValueObj
                    .toString());
            float surchargeValue = 0.0f;
            if (isFileSurCharge(surchargeName))
            {
                surchargeValue = BigDecimalHelper.multiply(surchargePerValue,
                        fileCounts);
            }
            else if (isJobSurCharge(surchargeName))
            {
                surchargeValue = surchargePerValue;
            }
            else
            {
                // Never come here...
            }
            FlatSurcharge flatSurcharge = new FlatSurcharge(oCurrency,
                    surchargeValue);
            flatSurcharge.setName(surchargeName);
            try
            {
                ServerProxy.getCostingEngine().addSurcharge(cost.getId(),
                        flatSurcharge, Cost.REVENUE);
            }
            catch (Exception e)
            {
                // log there was a costing error - but shouldn't stop the job
                // from
                // moving along.
                c_category
                        .error("Error occurred while calculating additional surchange",
                                e);
            }

        }
    }

    // ArrayList
    // SystemConfigParamNames of perSurcharge
    private ArrayList getPerSurchargeNameList()
    {
        ArrayList perSurchargeNameList = new ArrayList();
        perSurchargeNameList.add(SystemConfigParamNames.PER_FILE_CHARGE01_KEY);
        perSurchargeNameList.add(SystemConfigParamNames.PER_FILE_CHARGE02_KEY);
        perSurchargeNameList.add(SystemConfigParamNames.PER_JOB_CHARGE_KEY);
        return perSurchargeNameList;
    }

    // HashMap
    // Key - Surchange name
    // Value - Surchange float value in String
    private HashMap getAdditionalSurcharges()
    {
        HashMap surcharges = new HashMap();
        ArrayList perSurchargeNameList = getPerSurchargeNameList();

        String perSurchargeName = null;
        String perSurchargeValue = null;
        for (int i = 0; i < perSurchargeNameList.size(); i++)
        {
            perSurchargeName = perSurchargeNameList.get(i).toString();
            try
            {

                perSurchargeValue = ServerProxy
                        .getSystemParameterPersistenceManager()
                        .getSystemParameter(perSurchargeName).getValue();
            }
            catch (Exception e)
            {
                // log there was a costing error - but shouldn't stop the job
                // from
                // moving along.
                c_category
                        .error("Error occurred while calculating additional surchange",
                                e);
            }
            if (!perSurchargeValue.equals("#") && perSurchargeValue != null)
            {
                surcharges.put(perSurchargeName, perSurchargeValue);
            }
        }
        return surcharges;
    }

    // For "Additional functionality quotation" issue
    private boolean isFileSurCharge(String key)
    {
        return (SystemConfigParamNames.PER_FILE_CHARGE01_KEY
                .equalsIgnoreCase(key) || SystemConfigParamNames.PER_FILE_CHARGE02_KEY
                .equalsIgnoreCase(key));
    }

    // For "Additional functionality quotation" issue
    private boolean isJobSurCharge(String key)
    {
        return SystemConfigParamNames.PER_JOB_CHARGE_KEY.equalsIgnoreCase(key);
    }
}
