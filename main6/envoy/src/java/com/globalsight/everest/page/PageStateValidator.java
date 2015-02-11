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

package com.globalsight.everest.page;

import com.globalsight.everest.edit.EditHelper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflowmanager.Workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * A helper class used to check for state validation of
 * a page (source/target).  This is important during the 
 * editing process of a page in GlobalSight.
 */

public class PageStateValidator
{
    /**
     * Check for validity of the state of the pages (source/target)
     * in the specified workflow and if at least one page is in 
     * PageState.UPDATING state, throw an exception to inform the 
     * user about this process.
     *
     * @param p_workflow The workflow used to get it's source/target pages
     *
     * @throws PageException - If at least one page is in UPDATING state.
     */
    public static void validateStateOfPagesInWorkflow(Workflow p_workflow)
        throws PageException
    {
        
        if (EditHelper.isGxmlEditorInstalled())
        {                
            Job job = p_workflow.getJob();
            Collection pages = job.getSourcePages(
                PrimaryFile.EXTRACTED_FILE);
            // no need to perform validation if there are no extracted pages
            if (pages == null || pages.size() == 0)
            {
                return;
            }
            
            pages.addAll(p_workflow.getTargetPages(
                PrimaryFile.EXTRACTED_FILE));

            validateStateOfPages(new ArrayList(pages), job.getId());
        }
    }

    /**
     * Check for validity of the state of the pages (source/target)
     * in the specified job and if at least one page is in 
     * PageState.UPDATING state, throw an exception to inform the 
     * user about this process.
     *
     * @param p_job The job used to get all of it's source/target pages
     *
     * @throws PageException - If at least one page is in UPDATING state.
     */
    public static void validateStateOfPagesInJob(Job p_job)
        throws PageException
    {
        if (EditHelper.isGxmlEditorInstalled())
        {                
            validateStateOfPages(p_job, p_job.getWorkflows().toArray());
        }
    }

    /**
     * Check for validity of the state of the pages (source/target)
     * in the specified workflows and if at least one page is in 
     * PageState.UPDATING state, throw an exception to inform the 
     * user about this process.
     *
     * @param p_workflows The workflows used to get it's source/target pages
     *
     * @throws PageException - If at least one page is in UPDATING state.
     */
    public static void validateStateOfPagesInWorkflows(Object[] p_workflows)
        throws PageException
    {
        if ((p_workflows != null && p_workflows.length > 0) && 
            EditHelper.isGxmlEditorInstalled())
        {
            validateStateOfPages(((Workflow)p_workflows[0]).getJob(), 
                                 p_workflows);
        }        
    }

    /**
     * Check for validity of the state of the pages (source/target)
     * associated with the specified task and if at least one page is in
     * PageState.UPDATING state, throw an exception to inform the 
     * user about this process.
     *
     * @param p_task The task used to get it's associated source/target pages
     *
     * @throws PageException - If at least one page is in UPDATING state.
     */
    public static void validateStateOfPagesInTask(Task p_task)
        throws PageException
    {
        if (EditHelper.isGxmlEditorInstalled())
        {                
            List pages = p_task.getSourcePages(
                PrimaryFile.EXTRACTED_FILE);

            // no need to do validation if they are not extracted
            if (pages == null || pages.size() == 0)
            {
                return;
            }

            pages.addAll(p_task.getTargetPages(
                PrimaryFile.EXTRACTED_FILE));

            validateStateOfPages(pages, p_task.getJobId());
        }
    }

    //////////////////////////////////////////////////////////////////////
    //  Private Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Check for validity of the state of the given pages (source/target)
     * and if at least one page is in PageState.UPDATING state, throw an
     * exception to inform the user about this process.
     *
     * @param p_pages A list of source/target pages
     *
     * @throws PageException - If at least one page is in UPDATING state.
     */
    private static void validateStateOfPages(List p_pages, long p_jobId)
        throws PageException
    {
        int size = p_pages == null ? -1 : p_pages.size();
        boolean isPageInUpdatingState = false;
        for (int i = 0; i < size && !isPageInUpdatingState; i++)
        {
            Page p = (Page)p_pages.get(i);
            isPageInUpdatingState = p.isInUpdatingState();
        }

        if (isPageInUpdatingState)
        {
            String[] args = {String.valueOf(p_jobId)};
            throw new PageException(
                PageException.MSG_PAGE_IN_UPDATING_STATE_ERROR, 
                args, new RuntimeException());
        }
    }

    /*
     * Make sure no page is in UPDATING state.
     */
    private static void validateStateOfPages(Job p_job, 
                                             Object[] p_workflows)
        throws PageException
    {
        Collection pages = p_job.getSourcePages(
            PrimaryFile.EXTRACTED_FILE);
        // no need to perform validation if there are no extracted pages
        if (pages == null || pages.size() == 0)
        {
            return;
        }

        for (int i = 0; i < p_workflows.length; i++)
        {
            pages.addAll(((Workflow)p_workflows[i]).getTargetPages(
                PrimaryFile.EXTRACTED_FILE));
        }

        validateStateOfPages(new ArrayList(pages), p_job.getId());
    }

}
