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

/* Copyright (c) 2000, GlobalSight Corporation.  All rights reserved.
 * THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
 * GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
 * IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
 * OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
 * AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.
 *
 * THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
 * SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
 * UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
 * BY LAW.
 */
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.persistence.hibernate.HibernateUtil;

public class WorkflowPersistenceAccessor
{

    public WorkflowPersistenceAccessor()
    {

    }

    /**
     * @param Workflow
     *            p_workflow, String p_state
     * @throws WorkflowManagerException
     */
    public static void updateWorkflowState(Workflow p_workflow)
            throws WorkflowManagerException
    {
        try
        {
            JobImpl jobImpl = (JobImpl) p_workflow.getJob();
            // getPersistenceService().updateObject(jobImpl);
            HibernateUtil.update(jobImpl);

        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = new Long(p_workflow.getId()).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_UPDATE_WORKFLOW,
                    args, e);
        }

    }

    /**
     * @param long
     *            p_workflowId
     * @return Workflow
     */
    public static Workflow getWorkflowById(long p_workflowId)
            throws WorkflowManagerException
    {
        Workflow wf = null;
        try
        {
            // Vector queryArgs = new Vector();
            // queryArgs.add(new Long(p_workflowId));
            // Iterator it = getPersistenceService().
            // executeNamedQuery(WorkflowQueryNames.WORKFLOW_BY_ID,queryArgs,true).
            // iterator();
            // wf = (Workflow)it.next();
            wf = (Workflow) HibernateUtil.get(WorkflowImpl.class, new Long(
                    p_workflowId));
        }
        catch (Exception e)
        {
            String[] args = new String[1];
            args[0] = new Long(p_workflowId).toString();
            throw new WorkflowManagerException(
                    WorkflowManagerException.MSG_FAILED_TO_GET_WORKFLOW_BY_ID,
                    args, e);

        }
        return wf;
    }

    /**
     * @param p_criteriaExpression
     * @return Vector
     */
    // public Collection getWorkflows(Expression p_criteriaExpression)
    // throws WorkflowManagerException
    // {
    // // @@@@@@@@@ NOT FOUND reference, can delete the method. @@@@@@@@@@@@@@
    // Collection workflows = null;
    // ReadAllQuery m_query = new ReadAllQuery();
    // m_query.setReferenceClass(WorkflowImpl.class);
    // m_query.setSelectionCriteria(p_criteriaExpression);
    //
    // try
    // {
    // // execute query
    // workflows = getPersistenceService().executeQuery(m_query, true);
    // }
    // catch (PersistenceException pe)
    // {
    // throw new
    // WorkflowManagerException(WorkflowManagerException.MSG_FAILED_TO_GET_LIST_OF_WORKFLOWS,
    // null, pe);
    // }
    // return workflows;
    // }
}
