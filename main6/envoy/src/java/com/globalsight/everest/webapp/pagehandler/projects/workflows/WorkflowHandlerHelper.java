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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.io.File;
import java.io.FileInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.cxe.util.EventFlowXmlParser;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.qachecks.QAChecker;
import com.globalsight.everest.qachecks.QACheckerHelper;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.company.CompanyRemoval;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.persistence.workflow.JbpmVariable;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.client2.Ambassador2;
import com.globalsight.webservices.client2.WebService2ClientHelper;

/**
 * A helper class for all workflow related page handlers.
 */
public class WorkflowHandlerHelper
{
    private static final Logger logger = Logger
            .getLogger(WorkflowHandlerHelper.class.getName());

    /**
     * Archives the specified job.
     */
    static void archiveJob(Job p_job) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getJobHandler().archiveJob(p_job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * This method allows the client to archive a single workflow.
     * 
     * @param String
     *            Session Id, Workflow object
     * @throws EnvoyServletException
     */
    static void archiveWorkflow(Workflow p_workflow)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getWorkflowManager().archiveWorkflow(p_workflow);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Cancel a pending job.
     * 
     * @param p_userId
     *            The userId requesting the job cancellation.
     * @param p_job
     *            The specified job.
     */
    static void cancelJob(String p_userId, Job p_job)
            throws EnvoyServletException
    {
        cancelJob(p_userId, p_job, Job.PENDING);
    }

    /**
     * Cancel all workflows of the given state; if all are cancelled, cancel the
     * job too. If the state is NULL then it cancels all workflows within the
     * job, and the Job.
     * 
     * @param p_userId
     *            The userId requesting the job cancellation.
     * @param p_job
     *            The specified job.
     * @param p_state
     *            The state of workflows to be cancelled or NULL to cancel all
     *            workflows in the job, along with the job.
     */
    public static void cancelJob(String p_userId, Job p_job, String p_state)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getJobHandler().cancelJob(p_userId, p_job, p_state);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Cancel the import errors that are part of the job.
     * 
     * @param p_userId
     *            The user requesting the job cancellation.
     * @param p_job
     *            The specified job.
     */
    static void cancelImportErrorPages(String p_userId, Job p_job)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getJobHandler().cancelImportErrorPages(p_userId, p_job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Dispatch the job.
     * 
     * @param p_job
     *            The specified job.
     */
    static void dispatchJob(Job p_job) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getJobHandler().dispatchJob(p_job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * This method cancels a single Workflow
     * 
     * @param Workflow
     *            workflow object
     * @exception throws EnvoyServletException
     */
    static void cancelWF(String p_userId, Workflow p_workflow)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getWorkflowManager().cancel(p_userId, p_workflow);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * This method dispatches a single workflow (only manual)
     * 
     * @param Workflow
     *            workflow object
     * @throws EnvoyServletException
     */
    static void dispatchWF(Workflow p_workflow) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getWorkflowManager().dispatch(p_workflow);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Perform the manual/automatic export for the specified list of pages.
     * 
     * @param p_exportParameters
     *            - The workflow level parameters required for export.
     * @param p_pageIds
     *            - A collection of pages to be exported.
     */
    public static void exportPage(ExportParameters p_exportParameters, List p_pageIds,
            boolean p_isTargetPage, long p_exportBatchId)
            throws EnvoyServletException
    {
        try
        {
            ServerProxy.getPageManager().exportPage(p_exportParameters,
                    p_pageIds, p_isTargetPage, p_exportBatchId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of currently active task(s) for the specified workflow
     * instance id.
     * 
     * @return A list of active task(s) for the specified workflow instance id.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance.
     * @exception EnvoyServletException
     *                - Wraps server side exceptions.
     */
    @Deprecated
    static Map getActiveTasksForWorkflow(long p_workflowInstanceId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowServer().getActiveTasksForWorkflow(
                    p_workflowInstanceId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets activities for a workflow history.
     */
    static Map<TaskInstance, Boolean> getWorkflowHistory(
            long p_workflowInstanceId)
    {
        JbpmContext ctx = WorkflowConfiguration.getInstance().getJbpmContext();
        Map<TaskInstance, Boolean> taskMap = new HashMap<TaskInstance, Boolean>();
        try
        {
            Session dbSession = ctx.getSession();
            String hql = "from TaskInstance t where t.token.id = :wiId order by t.id asc";
            Query query = dbSession.createQuery(hql);
            query.setParameter("wiId", p_workflowInstanceId);
            List<TaskInstance> tasks = query.list();
            if (tasks != null)
            {
                for (TaskInstance ti : tasks)
                {
                    hql = "from JbpmVariable j where j.name='skip' and j.taskInstance.id = :tiId";
                    query = dbSession.createQuery(hql);
                    query.setParameter("tiId", ti.getId());
                    List<JbpmVariable> skipped = query.list();
                    if (skipped != null && skipped.size() > 0)
                    {
                        // skipped activity
                        taskMap.put(ti, true);
                    }
                    else
                    {
                        taskMap.put(ti, false);
                    }
                }
            }
            return taskMap;
        }
        finally
        {
            ctx.close();
        }
    }

    /**
     * Retrieves a Task by Id for the given user.
     * 
     * @param p_userId
     *            the Id of the user.
     * @param p_taskId
     *            The id of the target task.
     * 
     * @return a Task object
     * @throws EnvoyServletException
     */
    static Task getTask(String p_userId, long p_taskId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getTaskManager().getTask(p_userId, p_taskId,
                    WorkflowConstants.TASK_ALL_STATES);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Retrieves all codesets in the datastore.
     * 
     * @exception EnvoyServletException
     *                - Wraps server side exceptions
     */
    static Vector getAllCodeSets() throws EnvoyServletException
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

    /**
     * Get the job object specified by its job id.
     * 
     * @param p_jobId
     *            The specified job id.
     * @return The job object.
     */
    public static Job getJobById(long p_jobId) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobHandler().getJobById(p_jobId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of job objects based on a particular state (such as: 'ready',
     * 'in progresss' and 'completed').
     * 
     * @param p_state
     *            The state of the job.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    static public Collection getJobsByStateList(Vector p_stateList)
            throws EnvoyServletException
    {
        try
        {
            // get the most recent jobs (within the number of days
            // specified in envoy.properties)
            return ServerProxy.getJobHandler().getJobsByStateList(p_stateList,
                    true);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets a list of job objects based on a id of a particular user and
     * particular state (such as: 'ready', 'in progresss' and 'completed').
     * 
     * @param p_userId
     *            The user id of the project manager.
     * @param p_state
     *            The state of the job.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    static Collection getJobsByManagerIdAndState(String p_userId, String p_state)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobHandler().getJobsByManagerIdAndState(
                    p_userId, p_state);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets a list of job objects based on a id of a particular user and
     * particular state (such as: 'ready', 'in progresss' and 'completed').
     * 
     * @param p_userId
     *            The user id of the project manager.
     * @param p_stateList
     *            The state list of multiple states.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    static public Collection getJobsByManagerIdAndStateList(String p_userId,
            Vector p_stateList) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobHandler().getJobsByManagerIdAndStateList(
                    p_userId, p_stateList);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets a list of job objects based on a id of a particular workflow manager
     * and particular state (such as: 'ready', 'in progresss' and 'completed').
     * 
     * @param p_userId
     *            The user id of the workflow manager.
     * @param p_stateList
     *            The state list of multiple states.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    static public Collection getJobsByWfManagerIdAndStateList(String p_userId,
            Vector p_stateList) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobHandler()
                    .getJobsByWfManagerIdAndStateList(p_userId, p_stateList);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Gets a list of job objects based on a rate and Job state is either
     * PENDING or DISPATCHED
     * 
     * @param p_rate
     *            The rate used by any of the tasks.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    static public Collection getJobsByRate(String p_rateId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobHandler().getJobsByRate(p_rateId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get project by the project id.
     * 
     * @param p_id
     *            The project id.
     * @return Return the specified project.
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Project getProjectById(long p_id)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler().getProjectById(p_id);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Returns list of WorkflowTemplateInfos
     * 
     * @param p_job
     *            the job of the job that is having workflows added to
     */
    public static Collection getWorkflowTemplateInfos(Job p_job)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getProjectHandler()
                    .getAllWorkflowTemplateInfosByL10nProfileId(p_job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of workflow objects based on a particular workflow id.
     * 
     * @return the target Page.
     * @param p_targetPageId
     *            target page identifier
     */
    static TargetPage getTargetPage(long p_targetPageId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getPageManager().getTargetPage(p_targetPageId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a list of workflow objects based on a particular workflow id.
     * 
     * @param p_workflowId
     *            - The id of the workflow.
     * @return A vector of only ONE workflow (always a vector is returned since
     *         TOPLink is unaware of querying for one or more objects.)
     * @exception EnvoyServletException
     *                Component related exception.
     */
    public static Workflow getWorkflowById(long p_workflowId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowManager().getWorkflowByIdRefresh(
                    p_workflowId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Updates the planned completion dates for the workflows in a job.
     */
    public static void updatePlannedCompletionDates(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        String wfIds = (String) p_request.getParameter("editableWFS");

        StringTokenizer stok = new StringTokenizer(wfIds, ",");
        while (stok.hasMoreTokens())
        {
            String wfid = stok.nextToken();
            String year = (String) p_request.getParameter("yearField_" + wfid);
            String month = (String) p_request
                    .getParameter("monthField_" + wfid);
            String day = (String) p_request.getParameter("dayField_" + wfid);
            updatePlannedCompletionDate(Long.parseLong(wfid), year, month, day);
        }
    }

    /**
     * Updates the estimated completion dates for the workflows in a job.
     */
    public static void updateEstimatedCompletionDates(
            HttpServletRequest p_request, TimeZone timezone)
            throws EnvoyServletException
    {
        String wfIds = (String) p_request.getParameter("editableWFS");

        StringTokenizer stok = new StringTokenizer(wfIds, ",");
        while (stok.hasMoreTokens())
        {
            String wfid = stok.nextToken();

            // Update the date only when checkbox is checked.
            if (p_request.getParameter("checkField_" + wfid) != null)
            {
                String year = (String) p_request.getParameter("yearField_"
                        + wfid);
                String month = (String) p_request.getParameter("monthField_"
                        + wfid);
                String day = (String) p_request
                        .getParameter("dayField_" + wfid);
                String hour = (String) p_request.getParameter("hourField_"
                        + wfid);
                String minute = (String) p_request.getParameter("minuteField_"
                        + wfid);
                updateEstimatedCompletionDate(Long.parseLong(wfid), year,
                        month, day, hour, minute, timezone);
            }
        }
    }

    /**
     * Updates the estimated translate completion dates for the workflows in a
     * job.
     */
    public static void updateEstimatedTranslateCompletionDates(
            HttpServletRequest p_request, TimeZone timezone)
            throws EnvoyServletException
    {
        String wfIds = (String) p_request.getParameter("editableWFS");

        StringTokenizer stok = new StringTokenizer(wfIds, ",");
        while (stok.hasMoreTokens())
        {
            String wfid = stok.nextToken();

            // Update the date only when checkbox is checked.
            if (p_request.getParameter("checkField_" + wfid) != null)
            {
                String year = (String) p_request.getParameter("yearField_"
                        + wfid);
                String month = (String) p_request.getParameter("monthField_"
                        + wfid);
                String day = (String) p_request
                        .getParameter("dayField_" + wfid);
                String hour = (String) p_request.getParameter("hourField_"
                        + wfid);
                String minute = (String) p_request.getParameter("minuteField_"
                        + wfid);
                updateEstimatedTranslateCompletionDate(Long.parseLong(wfid),
                        year, month, day, hour, minute, timezone);
            }
        }
    }

    /**
     * Get a list of workflow objects based on a particular job id.
     * 
     * @param p_jobId
     *            - The id of the job (also is the foreign key in the workflow).
     * @return A vector of only ONE workflow (always a vector is returned since
     *         TOPLink is unaware of querying for one or more objects.)
     * @exception EnvoyServletException
     *                - Component related exception.
     */
    static Collection getWorkflowsByJobId(long p_jobId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getJobReportingManager().getWorkflowsByJobId(
                    p_jobId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Get a particular workflow instance.
     * 
     * @param p_workflowInstanceId
     *            - The id of the workflow instance to be retreived.
     * @return A WorkflowInstance object (if it exists).
     * @exception EnvoyServletException
     *                - Component related exception.
     */
    public static WorkflowInstance getWorkflowInstance(long p_workflowInstanceId)
            throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowServer().getWorkflowInstanceById(
                    p_workflowInstanceId);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Return list of TaskInfos for a given workflow.
     */
    static List getTaskInfos(Workflow wf) throws EnvoyServletException
    {
        try
        {
            return ServerProxy.getWorkflowManager().getTaskInfosInDefaultPath(
                    wf);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * This method is used to make a job ready-in other words move it from the
     * 'PENDING' state to the 'READY' state.
     * 
     * @param Job
     *            p_job
     */
    static void makeReadyJob(Job p_job) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getJobDispatchEngine().makeReadyJob(p_job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * This method reassigns a user to a different user during any task of the
     * workflow
     * 
     * @param String
     *            Session Id, WorkflowInstance iFlow instance
     * @throws EnvoyServletException
     */
    static void modifyWorkflow(String p_sessionId,
            WorkflowInstance p_wfInstance, String p_projectManagerID,
            Hashtable p_modifiedTasks) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getWorkflowManager().modifyWorkflow(p_sessionId,
                    p_wfInstance, p_projectManagerID, p_modifiedTasks);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Determine if the workflow can be modified. This is used for enabling a
     * link or button in order to go to the modify workflow UI.
     */
    static boolean isWorkflowModifiable(String p_workflowState)
    {
        return (p_workflowState.equals(Workflow.DISPATCHED)
                || p_workflowState.equals(Workflow.READY_TO_BE_DISPATCHED) || p_workflowState
                    .equals(Workflow.EXPORT_FAILED));
    }

    static long getDueDateInDays(WorkflowTaskInstance p_wfTask)
    {
        return p_wfTask.getCompletedTime();
    }

    /**
     * @see WorkflowManager.updatePlannedCompletionDate(long, Date)
     */
    static void updatePlannedCompletionDate(long p_workflowId, String p_year,
            String p_month, String p_day) throws EnvoyServletException
    {
        try
        {
            // Note that the Calendar object wrapped in Timestamp uses
            // a "zero-based" month numbering system (so August is month
            // 7 and NOT month 8).
            Timestamp ts = new Timestamp(Integer.parseInt(p_year),
                    Integer.parseInt(p_month) - 1, Integer.parseInt(p_day),
                    Timestamp.DATE);
            ServerProxy.getWorkflowManager().updatePlannedCompletionDate(
                    p_workflowId, ts.getDate());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * @see WorkflowManager.updateEstimatedCompletionDate(long, Date)
     */
    static void updateEstimatedCompletionDate(long p_workflowId, String p_year,
            String p_month, String p_day, String p_hour, String p_minute,
            TimeZone timezone) throws EnvoyServletException
    {
        try
        {
            // Note that the Calendar object wrapped in Timestamp uses
            // a "zero-based" month numbering system (so August is month
            // 7 and NOT month 8).
            Timestamp ts = new Timestamp(Integer.parseInt(p_year),
                    Integer.parseInt(p_month) - 1, Integer.parseInt(p_day),
                    Timestamp.DATE, timezone);
            ts.setHour(Integer.parseInt(p_hour));
            ts.setMinute(Integer.parseInt(p_minute));
            ServerProxy.getWorkflowManager().updateEstimatedCompletionDate(
                    p_workflowId, ts.getDate());

        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * @see WorkflowManager.updateEstimatedTranslateCompletionDate(long, Date)
     */
    static void updateEstimatedTranslateCompletionDate(long p_workflowId,
            String p_year, String p_month, String p_day, String p_hour,
            String p_minute, TimeZone timezone) throws EnvoyServletException
    {
        try
        {
            // Note that the Calendar object wrapped in Timestamp uses
            // a "zero-based" month numbering system (so August is month
            // 7 and NOT month 8).
            Timestamp ts = new Timestamp(Integer.parseInt(p_year),
                    Integer.parseInt(p_month) - 1, Integer.parseInt(p_day),
                    Timestamp.DATE, timezone);
            ts.setHour(Integer.parseInt(p_hour));
            ts.setMinute(Integer.parseInt(p_minute));
            ServerProxy.getWorkflowManager()
                    .updateEstimatedTranslateCompletionDate(p_workflowId,
                            ts.getDate());
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    /**
     * Make sure no page is in UPDATING state for the given job.
     */
    public static void validateStateOfPagesByJobId(long p_jobId)
            throws EnvoyServletException
    {
        Job job = getJobById(p_jobId);
        validateStateOfPagesInJob(job);
    }

    /**
     * Make sure no page is in UPDATING state for the given job.
     */
    public static void validateStateOfPagesInJob(Job p_job)
            throws EnvoyServletException
    {
        try
        {
            PageStateValidator.validateStateOfPagesInJob(p_job);
        }
        catch (Exception e)
        {
            throw new EnvoyServletException(e);
        }
    }

    private static Vector vectorizedCollection(Collection p_collection)
    {
        return new Vector(p_collection);
    }

    /**
     * Converts the vector of workflow instance tasks to an array.
     * 
     * @param p_wfInstanceTasks
     *            - the workflow instance tasks.
     * 
     * @return an array of the workflow instance tasks.
     */
    public static WorkflowTaskInstance[] convertToArray(Vector p_wfInstanceTasks)
    {
        WorkflowTaskInstance[] taskInstances = new WorkflowTaskInstance[p_wfInstanceTasks
                .size()];

        Enumeration e = p_wfInstanceTasks.elements();
        int i = 0;
        while (e.hasMoreElements())
        {
            taskInstances[i++] = (WorkflowTaskInstance) e.nextElement();
        }

        return taskInstances;
    }

    /**
     * Recreate job with same job id, job name, source files, target locales
     * etc.
     * 
     * @param jobId
     */
    public static void recreateJob(Long jobId) throws EnvoyServletException
    {
        HashMap<String, Object> args = new HashMap<String, Object>();
        Vector<String> filePaths = new Vector<String>();
        Vector<String> fileProfileIds = new Vector<String>();
        Vector<String> targetLocales = new Vector<String>();

        Vector<File> sourceFiles = new Vector<File>();
        Vector<File> backupFiles = new Vector<File>();
        try
        {
            Job job = JobCreationMonitor.loadJobFromDB(jobId);
            String jobName = job.getJobName();
            String priority = String.valueOf(job.getPriority());
            String createUserId = job.getCreateUserId();
            String state = job.getState();
            String docDir = AmbFileStoragePathUtils.getCxeDocDirPath(job.getCompanyId());

            Set<String> realFiles = new HashSet<String>();
            Collection<Request> requests = job.getRequestList();
            Iterator it = job.getWorkflows().iterator();
            String trgLocales = "";
            if (Job.READY_TO_BE_DISPATCHED.equals(state))
            {
                while (it.hasNext())
                {
                    Workflow workflow = (Workflow) it.next();
                    if (!Workflow.CANCELLED.equals(workflow.getState()))
                    {
                        trgLocales += workflow.getTargetLocale().toString() + ",";
                    }
                }
                trgLocales = trgLocales.substring(0, trgLocales.length() - 1);
            }
            FileProfilePersistenceManager fpManager = ServerProxy
                    .getFileProfilePersistenceManager();
            for (Request request : requests)
            {
                long fpId = request.getFileProfileId();
                fpId = fixFileProfileId(fpId);

                EventFlowXmlParser parser = new EventFlowXmlParser();
                parser.parse(request.getEventFlowXml());
                if (Job.IMPORTFAILED.equals(state))
                {
                    trgLocales = parser.getTargetLocale();
                }
                String srcFilePathName = parser.getDataValue("source", "Filename");
                
                File srcFile = new File(docDir, srcFilePathName);
                if (XliffFileUtil.isXliffFile(srcFile.getName()))
                {
                    srcFile = XliffFileUtil.getOriginalRealSoureFile(srcFile);
                    if (srcFile.exists() && srcFile.isFile())
                    {
                        String path = srcFile.getAbsolutePath().replace("\\", "/");
                        String docDirTmp = docDir.replace("\\", "/");
                        srcFilePathName = path.substring(docDirTmp.length() + 1);
                    }
                }

                // Get the real source file if it has script on import.
				FileProfile fp = fpManager.getFileProfileById(fpId, true);
				if (StringUtils.isNotEmpty(fp.getScriptOnImport()))
				{
                    String scriptedFolderNamePrefix = FileSystemUtil
                            .getScriptedFolderNamePrefixByJob(jobId);
					String srcFileName = srcFile.getName();
					int index = srcFileName.lastIndexOf(".");
					String name = srcFileName.substring(0, index);
					String extension = srcFileName.substring(index + 1);
                    String folderName = scriptedFolderNamePrefix + "_" + name + "_" + extension;
                    if (srcFile.getParentFile().getName().equalsIgnoreCase(folderName))
					{
                        File parentFile = srcFile.getParentFile().getParentFile();
                        String path = parentFile.getAbsolutePath() + File.separator + srcFileName;
						File file = new File(path);
						if (file.exists())
						{
                            srcFilePathName = file.getAbsolutePath().substring(docDir.length() + 1);
						}
						srcFile = new File(docDir, srcFilePathName);
					}
				}

                // For office 2010 formats, multiple requests may be from the
                // same real file
                if (realFiles.contains(srcFilePathName))
                {
                    continue;
                }
                realFiles.add(srcFilePathName);

                // back up source files
                File backupFile = new File(docDir + File.separator + "recreateJob_tmp",
                        srcFilePathName);
                if (srcFile.exists() && srcFile.isFile())
                {
                    FileUtil.copyFile(srcFile, backupFile);
                    sourceFiles.add(srcFile);
                    backupFiles.add(backupFile);
                }
                else
                {
                    // ignore this request as its source file does not exist.
                    logger.warn("Can not find the source file for recreate: "
                            + srcFile.getAbsolutePath());
                    continue;
                }
                
                // For eloqua file
                if (srcFilePathName.toLowerCase().endsWith(".email.html")
                        || srcFilePathName.toLowerCase().endsWith(".landingpage.html"))
                {
                    String name = srcFilePathName.substring(0, srcFilePathName.lastIndexOf("."));
                    name = name.substring(0, name.lastIndexOf("."))+".obj";
                    File objSrcFile = new File(docDir, name);
                    File objBackupFile = new File(docDir + File.separator + "recreateJob_tmp"
                            + File.separator + name);
                    if (objSrcFile.exists() && objSrcFile.isFile())
                    {
                        FileUtil.copyFile(objSrcFile, objBackupFile);
                        sourceFiles.add(objSrcFile);
                        backupFiles.add(objBackupFile);
                    }
                }

                // For MindTouch job, backup .obj help files too.
                if (job.isMindTouchJob())
                {
                    String objFileName = srcFilePathName + ".obj";
                    File objSrcFile = new File(docDir, objFileName);
                    File objBackupFile = new File(docDir + File.separator + "recreateJob_tmp"
                            + File.separator + objFileName);
                    if (objSrcFile.exists() && objSrcFile.isFile())
                    {
                        FileUtil.copyFile(objSrcFile, objBackupFile);
                        sourceFiles.add(objSrcFile);
                        backupFiles.add(objBackupFile);
                    }
                }

                filePaths.add(getSourceFileLocalPathName(srcFilePathName));
                fileProfileIds.add(String.valueOf(fpId));
                targetLocales.add(trgLocales);
            }

            // try to re-create job only when it has at least one valid source file.
            if (sourceFiles.size() > 0)
            {
                // Remove job, but do not remove data in "job" table.
                CompanyRemoval removal = new CompanyRemoval(job.getCompanyId());
                boolean isRecreateJob = true;
                removal.removeJob(job, isRecreateJob);

                // Move backup source files back to working path after original
                // job is discarded.
                for (int i = 0; i < sourceFiles.size(); i++)
                {
                    File srcFile = sourceFiles.get(i);
                    File bakFile = backupFiles.get(i);
                    FileUtil.copyFile(bakFile, srcFile);
                }
                // Clean back up files
                cleanBackupFile(backupFiles.get(0));

                // update job state
                job.setState(Job.IN_QUEUE);
                HibernateUtil.saveOrUpdate(job);

                // Reuse the implementation in web service.
                args.put("jobId", String.valueOf(job.getJobId()));
                args.put("jobName", jobName);
                args.put("priority", priority);
                args.put("filePaths", filePaths);
                args.put("fileProfileIds", fileProfileIds);
                args.put("targetLocales", targetLocales);
                // args.put("attributes", attributeXml);

                //   use job's original creator
                User user = UserUtil.getUserById(createUserId);
                SystemConfiguration config = SystemConfiguration.getInstance();
                String hostName = config.getStringParameter("server.host");
                String port = config.getStringParameter("nonSSLPort");
                String isHttps = config.getStringParameter("server.ssl.enable");
                boolean httsEnabled = "true".equalsIgnoreCase(isHttps) ? true : false;
                String sslPort = config.getStringParameter("server.ssl.port");
                String userName = user.getUserName();
                String password = user.getPassword();
                Ambassador2 amb2 = WebService2ClientHelper.getClientAmbassador2(hostName,
                        (httsEnabled ? sslPort : port), userName, password, (httsEnabled ? true
                                : false));
                String accessToken = amb2.dummyLogin(userName, password);
                args.put("accessToken", accessToken);
                args.put("recreate", "true");
                boolean isViaWS = isJobCreatedOriginallyViaWS(realFiles.iterator().next(),
                        job.getId()); 
                args.put("isJobCreatedOriginallyViaWS", isViaWS ? "true" : "false");
                amb2.createJobOnInitial(args);
            }
            else
            {
                logger.warn("Do not find any source files on hard disk, so can not re-create this job");
            }
        }
        catch (Exception e)
        {
            logger.error("Error when try to re-create job " + jobId, e);
            throw new EnvoyServletException(e);
        }
        finally
        {
            filePaths = null;
            fileProfileIds = null;
            targetLocales = null;
            args = null;

            sourceFiles = null;
            backupFiles = null;
        }
    }

	public static String getExportFilePath(Workflow workflow)
	{
		String filePath = null;
		try
		{
			Map activeTasks = ServerProxy.getWorkflowServer()
					.getActiveTasksForWorkflow(workflow.getId());

			WorkflowTaskInstance activeTask = null;
			Object[] tasks = (activeTasks == null) ? null : activeTasks
					.values().toArray();
			if (tasks != null && tasks.length > 0)
			{
				activeTask = (WorkflowTaskInstance) tasks[0];
			}

			if (activeTask != null)
			{
				Task task = ServerProxy.getTaskManager().getTask(
						activeTask.getTaskId());
				if (QACheckerHelper.isShowQAChecksTab(task))
				{
					if (QACheckerHelper.isQAActivity(task))
					{
						QAChecker qaChecker = new QAChecker();
						filePath = qaChecker.runQAChecksAndGenerateReport(task
								.getId());
					}
					else
					{
						filePath = getPreviousQAReportFilePath(workflow);
					}
				}
			}
			else
			{
				if (workflow.getJob().getProject().getAllowManualQAChecks())
				{
					if (workflow.getState().equalsIgnoreCase("EXPORTED")
							|| workflow.getState()
									.equalsIgnoreCase("LOCALIZED"))
					{
						filePath = getPreviousQAReportFilePath(workflow);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e);
		}
		return filePath;
	}
    
	private static String getPreviousQAReportFilePath(Workflow workflow)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(AmbFileStoragePathUtils.getReportsDir(workflow.getCompanyId()));
		sb.append(File.separator);
		sb.append(ReportConstants.REPORT_QA_CHECKS_REPORT);
		sb.append(File.separator);
		sb.append(workflow.getJob().getId());
		sb.append(File.separator);
		sb.append(workflow.getTargetLocale().toString());
		File file = new File(sb.toString());
		long maxTime = 0;
		File emptyFile = null;
		String filePath = null;
		if (file.exists())
		{
			File[] files = file.listFiles();
			for (File fe : files)
			{
				long time = fe.lastModified();
				if (time > maxTime)
				{
					maxTime = time;
					emptyFile = fe;
				}
			}

			long maxDate = 0;
			File emptyDownFile = null;
			if (emptyFile != null)
			{
				File[] downFiles = emptyFile.listFiles();
				for (File f : downFiles)
				{
					long time = f.lastModified();
					if (time > maxDate)
					{
						maxDate = time;
						emptyDownFile = f;
					}
				}
				if (emptyDownFile != null)
				{
					filePath = emptyDownFile.getPath();
				}
			}
		}
		return filePath;
	}
	
	public static void zippedFolder(HttpServletRequest p_request,
			HttpServletResponse p_response, long companyId, Set<Long> jobIdSet,
			Set<File> exportListFiles, Set<String> locales)
	{
		String zipFileName = AmbFileStoragePathUtils.getReportsDir(companyId)
				+ File.separator + ReportConstants.REPORT_QA_CHECKS_REPORT
				+ ".zip";
		File zipFile = new File(zipFileName);
		Map<File, String> entryFileToFileNameMap = getEntryFileToFileNameMap(
				exportListFiles, jobIdSet, locales, AmbFileStoragePathUtils
						.getReportsDir(companyId).getPath()
						+ File.separator
						+ ReportConstants.REPORT_QA_CHECKS_REPORT);
		try
		{
			ZipIt.addEntriesToZipFile(zipFile, entryFileToFileNameMap, "");
			String downloadFileName = zipFile.getName();
			if (entryFileToFileNameMap.entrySet().size() > 0)
			{
				if (jobIdSet != null && jobIdSet.size() == 1)
				{
					Long jobId = jobIdSet.iterator().next();
					downloadFileName = ReportConstants.REPORT_QA_CHECKS_REPORT
							+ "_(" + jobId + ").zip";
				}
				else if (jobIdSet != null && jobIdSet.size() > 1)
				{
					String tempS = jobIdSet.toString();
					String jobNamesstr = tempS.substring(1, tempS.length() - 1);
					downloadFileName = ReportConstants.REPORT_QA_CHECKS_REPORT
							+ "_(" + jobNamesstr + ").zip";
				}
			}
			else
			{
				downloadFileName = "No Report Download.zip";
			}

			// write zip file to client
			p_response.setContentType("application/zip");
			p_response.setHeader("Content-Disposition",
					"attachment; filename=\"" + downloadFileName + "\";");
			if (p_request.isSecure())
			{
				PageHandler.setHeaderForHTTPSDownload(p_response);
			}
			p_response.setContentLength((int) zipFile.length());

			// Send the data to the client
			byte[] inBuff = new byte[4096];
			FileInputStream fis = new FileInputStream(zipFile);
			int bytesRead = 0;
			while ((bytesRead = fis.read(inBuff)) != -1)
			{
				p_response.getOutputStream().write(inBuff, 0, bytesRead);
			}

			if (bytesRead > 0)
			{
				p_response.getOutputStream().write(inBuff, 0, bytesRead);
			}

			fis.close();
			FileUtil.deleteFile(zipFile);
		}
		catch (Exception e)
		{
			logger.error(e);
		}
		finally
		{
			if (zipFile.exists())
				zipFile.deleteOnExit();
		}
	}
    
	public static Map<File, String> getEntryFileToFileNameMap(Set<File> entryFiles,
			Set<Long> jobIdSet, Set<String> locales, String cxeDocsDirPath)
	{
		Map<File, String> entryFileToFileNameMap = new HashMap<File, String>();
		File tempFile;

		for (Long jobId : jobIdSet)
		{
			ArrayList<String> entryNames = new ArrayList<String>();

			for (String locale : locales)
			{
				entryNames.clear();
				String prefixStr1 = cxeDocsDirPath + File.separator + jobId
						+ File.separator + locale;
				for (File entryFile : entryFiles)
				{
					String entryFilePath = entryFile.getPath();
					if (entryFilePath.startsWith(prefixStr1))
					{
						entryNames.add(entryFilePath.replaceAll("\\\\", "/"));
					}
				}
				if (entryNames.size() > 0)
				{
					Map<String, String> tempMap = ZipIt
							.getEntryNamesMap(entryNames);
					for (String key : tempMap.keySet())
					{
						tempFile = new File(key);
						entryFileToFileNameMap.put(tempFile, locale
								+ File.separator + jobId + File.separator
								+ tempMap.get(key));
					}
				}
			}
		}
		return entryFileToFileNameMap;
	}
    
    
    /**
     * Get the local file pathname of source file.
     * @param displayName
     * @return
     */
    private static String getSourceFileLocalPathName(String displayName)
    {
        String localPath = displayName.replace("\\", "/");

        // remove source locale section
        localPath = localPath.substring(localPath.indexOf("/") + 1);
        // remove "webservice"
        if (localPath.startsWith("webservice"))
        {
            localPath = localPath.substring(localPath.indexOf("/") + 1);
        }
        // remove "jobid" or "jobname"(old jobs)
        localPath = localPath.substring(localPath.indexOf("/") + 1);

        localPath = localPath.replace("/", File.separator);

        return localPath;
    }

    /**
     * Clean backup files in "recreateJob_tmp" folder for current job.
     */
    private static void cleanBackupFile(File bakFile)
    {
        StringBuffer fileBuf = new StringBuffer();

        String path = bakFile.getAbsolutePath().replace("\\", "/");
        // recreateJob_tmp
        int index = path.indexOf("/recreateJob_tmp/");
        fileBuf.append(path.substring(0, index + "/recreateJob_tmp/".length()));
        path = path.substring(index + "/recreateJob_tmp/".length());
        // source locale
        index = path.indexOf("/");
        fileBuf.append(path.substring(0, index));
        path = path.substring(index + 1);
        // webservice
        if (path.startsWith("webservice"))
        {
            fileBuf.append("/webservice");
            path = path.substring("webservice".length() + 1);
        }
        // job id
        index = path.indexOf("/");
        fileBuf.append("/").append(path.substring(0, index));

        String filePath = fileBuf.toString().replace("/", File.separator);
        // this is the job id folder
        File file = new File(filePath);
        FileUtil.deleteFile(file);
    }

    /**
     * For some file formats, the "displayName" in DB starts with some special
     * characters like "footer1", "header1", "diagram data1", "comments" etc.
     * Display name in DB:
     * "(Hyperlinks) en_US\4\GS\test\Files\testfile\FromBugs\Docx\test3_1415.docx"
     * Expected: 
     * "en_US\4\GS\test\Files\testfile\FromBugs\Docx\test3_1415.docx"
     */
//    private static String fixDisplayNameForOffice(String displayName,
//            GlobalSightLocale sourceLocale)
//    {
//        String fixedDisplayName = displayName.replace("\\", "/");
//        int index = fixedDisplayName.indexOf("/");
//        fixedDisplayName = fixedDisplayName.substring(index + 1);
//        fixedDisplayName = sourceLocale.toString() + "/" + fixedDisplayName;
//        fixedDisplayName = fixedDisplayName.replace("/", File.separator);
//        return fixedDisplayName;
//    }

    private static long fixFileProfileId(long fpId) throws GeneralException,
            RemoteException, NamingException
    {
        long result = fpId;

        FileProfilePersistenceManager fpManager = ServerProxy
                .getFileProfilePersistenceManager();
        FileProfile fp = fpManager.getFileProfileById(fpId, false);
        String fpName = fp.getName();
        boolean isXlzRef = fpManager.isXlzReferenceXlfFileProfile(fpName);
        if (isXlzRef)
        {
            String tmpFPName = fpName.substring(0, fpName.length() - 4);
            FileProfile xlzFP = fpManager.getFileProfileByName(tmpFPName);
            result = xlzFP.getId();
        }

        return result;
    }

    private static boolean isJobCreatedOriginallyViaWS(
            String modifiedDisplayName, long jobId)
    {
        String fakeDisplayName = modifiedDisplayName.replace("\\", "/");
        if (fakeDisplayName.indexOf("/webservice/") > -1)
        {
            int index = fakeDisplayName.indexOf("/");
            String rest = fakeDisplayName.substring(index);
            if (rest.startsWith("/webservice/" + jobId + "/"))
            {
                return true;
            }
        }
        return false;
    }
}
