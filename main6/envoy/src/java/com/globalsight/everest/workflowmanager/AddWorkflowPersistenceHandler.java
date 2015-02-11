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

package com.globalsight.everest.workflowmanager;

//
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationException;
import com.globalsight.everest.page.PagePersistenceAccessor;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileHandlerHelper;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WfTaskInfo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class AddWorkflowPersistenceHandler
{
    private static final Logger c_logger = Logger
            .getLogger(AddWorkflowPersistenceHandler.class.getName());

    public AddWorkflowPersistenceHandler()
    {
    }

    @SuppressWarnings("unchecked")
    public List createWorkflows(HashMap p_pages, Job p_job,
            Collection p_workflowTemplates) throws WorkflowManagerException
    {
        SourcePage sourcePage = null;
        List listOfWorkflows = null;
        List<Workflow> workflows = new ArrayList<Workflow>();

        Session session = null;
        Transaction transaction = null;

        try
        {
            session = HibernateUtil.getSession();
            transaction = session.beginTransaction();

            HashMap targetPages = new HashMap(p_pages);

            // get the source page from the map
            sourcePage = (SourcePage) targetPages.remove(p_job.getL10nProfile()
                    .getSourceLocale().getIdAsLong());
            Vector toplinkTargetPages = PagePersistenceAccessor
                    .getTargetPages(sourcePage.getId());
            Map mapTargetPages = convertVectorIntoMap(toplinkTargetPages);

            listOfWorkflows = createWorkflowInstances(sourcePage.getRequest(),
                    p_job, p_workflowTemplates);

            JobImpl job = (JobImpl) session.get(JobImpl.class,
                    new Long(p_job.getId()));
            ;
            Iterator it = listOfWorkflows.iterator();
            while (it.hasNext())
            {
                Workflow workflow = (Workflow) it.next();

                workflow.setJob(job);
                job.addWorkflowInstance(workflow);
                TargetPage targetPage = (TargetPage) (targetPages.get(workflow
                        .getTargetLocale().getIdAsLong()));
                TargetPage toplinkTargetPage = (TargetPage) (mapTargetPages
                        .get(targetPage.getIdAsLong()));
                workflow.addTargetPage(toplinkTargetPage);
                workflows.add(workflow);
            }
            session.update(job);
            transaction.commit();
        }
        catch (Exception jce)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }

            c_logger.error("Unable to create new workflows for given job "
                    + jce);
            String args[] = new String[1];
            args[0] = Long.toString(sourcePage.getId());
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_ADD_WORKFLOW, args,
                    jce);
        }
        finally
        {
            if (session != null)
            {
                // session.close();
            }
        }
        return workflows;
    }

    private Map convertVectorIntoMap(Vector p_toplinkTargetPages)
    {
        Iterator it = p_toplinkTargetPages.iterator();
        Map<Long, TargetPage> map = new HashMap<Long, TargetPage>();
        while (it.hasNext())
        {
            TargetPage targetPage = (TargetPage) it.next();
            map.put(new Long(targetPage.getId()), targetPage);
        }
        return map;
    }

    /*
     * Create the workflow instances that are part of the new job.
     */
    private List createWorkflowInstances(Request p_request, Job p_job,
            Collection p_workflowTemplates) throws JobCreationException
    {
        List<Workflow> listOfWorkflows = new ArrayList<Workflow>();
        L10nProfile l10nProfile = p_request.getL10nProfile();
        long lpId=l10nProfile.getId();
        try
        {
            Iterator it = p_workflowTemplates.iterator();
            while (it.hasNext())
            {
                WorkflowTemplateInfo wfInfo = (WorkflowTemplateInfo) it.next();
                long wfTemplateId = wfInfo.getWorkflowTemplateId();
                WorkflowInstance wfInstance = ServerProxy.getWorkflowServer()
                        .createWorkflowInstance(wfTemplateId);

                Workflow wf = new WorkflowImpl();
                wf.setId(wfInstance.getId());
                wf.setIflowInstance(wfInstance);
                wf.setState(Workflow.PENDING);
                wf.setTargetLocale(wfInfo.getTargetLocale());
                wf.setScorecardShowType(wfInfo.getScorecardShowType());
                wf.setDuration(calculateDuration(wfInstance));
                wf.setCompanyId(p_job.getCompanyId());
                wf.setPriority(p_job.getPriority());
                MachineTranslationProfile mtProfile = MTProfileHandlerHelper
                        .getMTProfileByRelation(lpId, wfInfo.getId());
                boolean useMT = false;
                long mtConfidenceScore = 0;
                String mtProfileName = null;
                if (mtProfile != null && mtProfile.isActive())
                {
                    useMT = true;
                    mtConfidenceScore = mtProfile.getMtConfidenceScore();
                    mtProfileName = mtProfile.getMtProfileName();
                }
                wf.setUseMT(useMT);
                wf.setMtConfidenceScore((int) mtConfidenceScore);
                wf.setMtProfileName(mtProfileName);

                // set workflow owners (PM and WFM)
                wf.addWorkflowOwner(new WorkflowOwner(wfInfo
                        .getProjectManagerId(),
                        Permission.GROUP_PROJECT_MANAGER));

                List wfmIds = wfInfo.getWorkflowManagerIds();
                if (wfmIds != null)
                {
                    for (Iterator wfmii = wfmIds.iterator(); wfmii.hasNext();)
                    {
                        wf.addWorkflowOwner(new WorkflowOwner((String) wfmii
                                .next(), Permission.GROUP_WORKFLOW_MANAGER));
                    }
                }

                // create the tasks and add them to the workflow
                createTasks(wf);
                listOfWorkflows.add(wf);
            }
        }
        catch (Exception e)
        {
            c_logger.error("Failed to create workflow instances for the new "
                    + "of request " + p_request.getId(), e);
            String args[] = new String[1];
            args[0] = Long.toString(p_request.getId());
            throw new JobCreationException(
                    JobCreationException.MSG_FAILED_TO_CREATE_WORKFLOW_INSTANCES,
                    args, e);

        }
        return listOfWorkflows;
    }

    /**
     * Create all the tasks of the workflow from the WorkflowTaskInstances. This
     * creates a task for each task within iflow - and provides a place to store
     * information in the System4 DB and not in iflow. Like rates, accept time,
     * complete time, hours to work on task, etc..
     */
    private void createTasks(Workflow p_wf)
    {
        Vector tasks = p_wf.getIflowInstance().getWorkflowInstanceTasks();
        for (int i = 0; i < tasks.size(); i++)
        {
            WorkflowTaskInstance wti = (WorkflowTaskInstance) tasks.get(i);
            // no need to create task for start, exit, and condition node.
            if (wti.getType() == WorkflowConstants.ACTIVITY)
            {
                TaskImpl task = new TaskImpl(p_wf);
                task.setId(wti.getTaskId());
                task.setName(wti.getActivityName());
                task.setState(TaskImpl.STATE_DEACTIVE);
                task.setType(getActivityType(task.getName()));
                task.setCompanyId(p_wf.getCompanyId());
                task.setRateSelectionCriteria(wti.getRateSelectionCriteria());
                // if an expense rate is specified
                if (wti.getExpenseRateId() > 0)
                {
                    try
                    {
                        Rate r = ServerProxy.getCostingEngine().getRate(
                                wti.getExpenseRateId());
                        task.setExpenseRate(r);
                    }
                    catch (Exception e)
                    {
                        // couldn't find the rate so left to be null
                        c_logger.error("Couldn't find the expense rate for task "
                                + wti.getTaskId()
                                + " of workflow "
                                + p_wf.getId());
                    }
                }
                // if a revenuve rate is specified
                if (wti.getRevenueRateId() > 0)
                {
                    try
                    {
                        Rate r = ServerProxy.getCostingEngine().getRate(
                                wti.getRevenueRateId());
                        task.setRevenueRate(r);
                    }
                    catch (Exception e)
                    {
                        // couldn't find the rate so left to be null
                        c_logger.error("Couldn't find the rate for task "
                                + wti.getTaskId() + " of workflow "
                                + p_wf.getId());
                    }
                }
                p_wf.addTask(task);
            }

        }

    }

    /**
     * Return the activity's type.
     */
    private int getActivityType(String p_activityName)
    {
        // default
        int type = TaskImpl.TYPE_TRANSLATE;
        try
        {
            Activity act = ServerProxy.getJobHandler().getActivity(
                    p_activityName);
            type = act.getType();

            // for sla report issue
            if ((type == Activity.TYPE_REVIEW) && act.getIsEditable())
            {
                type = TaskImpl.TYPE_REVIEW_EDITABLE;
            }
        }
        catch (Exception e)
        {
            // do nothing just return the default
        }
        return type;
    }

    /*
     * Calculate the duration of the workflow as number of 'minutes'.
     */
    private int calculateDuration(WorkflowInstance wfi)
    {
        long durationInMilli = 0;
        int minutes = 0;

        try
        {
            // -1 indicates that the default path would begin from the
            // START node.
            List wfTaskInfos = ServerProxy.getWorkflowServer()
                    .timeDurationsInDefaultPath(null, wfi.getId(), -1);

            int size = wfTaskInfos.size();
            for (int i = 0; i < size; i++)
            {
                // get all the complete durations in the default path (not the
                // accept time)
                WfTaskInfo taskInfo = (WfTaskInfo) wfTaskInfos.get(i);
                {
                    durationInMilli += taskInfo.getCompletionDuration();
                }
            }
            // convert the millisec to minutes since it's the smallest unit
            // of time used for each activity of a workflow.
            minutes = (int) durationInMilli / 60000;

        }
        catch (Exception e)
        {
            // if this fails just flag an error - and leave the cost at 0
            c_logger.error(
                    "Failed to calculate the cost for workflow " + wfi.getId(),
                    e);
        }
        return minutes;
    }
}
