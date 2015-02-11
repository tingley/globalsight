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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.PageStateValidator;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.workflow.JbpmVariable;

/**
 * A helper class for all workflow related page handlers.
 */
public class WorkflowHandlerHelper
{
    static private final long DAY_IN_MILLISEC = 86400000;

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
    static void exportPage(ExportParameters p_exportParameters, List p_pageIds,
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
                    .getAllWorkflowTemplateInfosBySourceLocaleAndPmId(p_job);
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
}
