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
package com.globalsight.everest.persistence.workflow;

import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.workflowmanager.Workflow;

/**
 * WorkflowDescriptorModifier extends DescriptorModifier by providing amendment
 * methods unique to the Workflow descriptor.
 */
public class WorkflowDescriptorModifier
{

    private static String MANAGER_ID_ARG = "pmUserId";
    private static String PROJECT_ID_ARG = "projectId";
    public static String WORKFLOW_BY_OWNER_AND_TYPE_SQL = null;
    static
    {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT DISTINCT wf.*");
        sb.append(" FROM JOB j, WORKFLOW wf, WORKFLOW_OWNER wfo, "
                + "REQUEST r, L10N_PROFILE lp, PROJECT p ");
        sb.append(" WHERE j.id = wf.job_id");
        sb.append(" AND wf.IFLOW_INSTANCE_ID = wfo.WORKFLOW_ID");
        sb.append(" AND wfo.OWNER_TYPE = '");
        sb.append(Permission.GROUP_PROJECT_MANAGER);
        sb.append("' AND r.L10N_PROFILE_ID = lp.ID");
        sb.append(" AND r.JOB_ID = j.ID");
        sb.append(" AND lp.PROJECT_ID = p.PROJECT_SEQ");
        sb.append(" AND p.project_seq = :");
        sb.append(PROJECT_ID_ARG);
        sb.append(" AND wfo.OWNER_ID = :");
        sb.append(MANAGER_ID_ARG);
        sb.append(" AND wf.STATE <> '");
        sb.append(Workflow.CANCELLED);
        sb.append("'");

        WORKFLOW_BY_OWNER_AND_TYPE_SQL = sb.toString();
    }

}
