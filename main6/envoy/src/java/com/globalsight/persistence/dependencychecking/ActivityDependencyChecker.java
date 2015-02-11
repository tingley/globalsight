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
package com.globalsight.persistence.dependencychecking;

// globalsight classes
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.jbpm.JbpmContext;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.taskmgmt.def.Task;

import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConfiguration;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowServer;
import com.globalsight.util.GeneralException;

/**
 * Checks for objects that may have dependencies on a particular Activity. The
 * class dependencies are hard-coded into this class.
 */
public class ActivityDependencyChecker extends DependencyChecker
{
    private static final Logger c_logger = Logger
            .getLogger(ActivityDependencyChecker.class);

    /**
     * Returns the dependencies in a Vector of Objects. The vector contains all
     * objects that are dependent on the Activity object being passed in.
     * 
     * @param p_object
     *            The object to test if there are dependencies on.
     * 
     * @return A vector of all objects that are dependent on p_object.
     * @exception A
     *                general exception that wraps the specific exception of why
     *                retrieving the dependencies failed.
     */
    protected Vector findDependencies(PersistentObject p_object)
            throws DependencyCheckException
    {
        if (p_object.getClass() != Activity.class)
        {
            String args[] = { this.getClass().getName(),
                    p_object.getClass().getName() };

            throw new DependencyCheckException(
                    DependencyCheckException.MSG_INVALID_OBJECT, args, null);
        }

        Activity act = (Activity) p_object;

        return WorkflowTemplateDependencies(act);
    }

    /**
     * Return the L10nProfiles that are dependent on this Activity.
     */
    private Vector WorkflowTemplateDependencies(Activity p_act)
            throws DependencyCheckException
    {
        Collection activeWfTemplateInfos = null;
        Vector dependentWfTemplatesInfos = new Vector();
        JbpmContext ctx = null;

        try
        {
            ctx = WorkflowConfiguration.getInstance().getJbpmContext();
            activeWfTemplateInfos = ServerProxy.getProjectHandler()
                    .getAllWorkflowTemplateInfos();
            for (Iterator itActive = activeWfTemplateInfos.iterator(); itActive
                    .hasNext();)
            {
                WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) itActive
                        .next();
                ProcessDefinition pd = ctx.getGraphSession()
                        .getProcessDefinition(wfti.getWorkflowTemplateId());
                if (pd.getTaskMgmtDefinition().getTask(
                        WorkflowConstants.NAME_TASK
                                + WorkflowConstants.NAME_SEPARATOR
                                + p_act.getName()) != null)
                {
                    dependentWfTemplatesInfos.add(wfti);
                }
            }
        }
        catch (PersistenceException pe)
        {
            StringBuffer errorMessage = new StringBuffer(
                    "Failed to query for all active workflow templates.");

            c_logger.error(errorMessage.toString(), pe);

            String[] args = {};
            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_WORKFLOW_DEPENDENCIES_FOR_USER,
                    args, pe);
        }
        catch (Exception e)
        {
            String activityName = p_act.getName();

            StringBuffer errMessage = new StringBuffer(
                    "Failed to query for all workflow templates that contain activity ");
            errMessage.append(activityName);

            c_logger.error(errMessage.toString(), e);

            String args[] = { activityName };

            throw new DependencyCheckException(
                    DependencyCheckException.FAILED_TO_GET_WORKFLOW_TEMPLATES_FOR_ACTIVITY,
                    args, e);
        }
        finally
        {
            if (ctx != null)
            {
                ctx.close();
            }
        }

        return dependentWfTemplatesInfos;
    }

    /**
     * Wraps the code for getting the workflow server.
     */
    private WorkflowServer getWorkflowServer() throws DependencyCheckException
    {
        WorkflowServer ws = null;

        try
        {
            ws = ServerProxy.getWorkflowServer();
        }
        catch (GeneralException ge)
        {
            c_logger.error("Couldn't find the WorkflowServer", ge);

            throw new DependencyCheckException(
                    DependencyCheckException.MSG_FAILED_TO_FIND_WORKFLOW_SERVER,
                    null, ge);
        }

        return ws;
    }
}
