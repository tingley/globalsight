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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.globalsight.cxe.util.workflow.AddWorkflowUtil;

public class WorkflowAdditionSender
{
    private List<Long> m_wfIds = null;
    private long m_jobId = 0;

    public WorkflowAdditionSender(List<Long> p_wfIds, long p_jobId) throws Exception
    {
        m_wfIds = p_wfIds;
        m_jobId = p_jobId;
    }

    public void sendToAddWorkflows()
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("jobId", m_jobId);
        data.put("workflowIds", m_wfIds);
        // GBS-4400
        AddWorkflowUtil.addWorkflowsWithThread(data);
    }
}
