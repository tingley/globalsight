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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.workflowmanager.Workflow;

public class ParamStatusPair extends ParamObjectPair { 

    public ParamStatusPair(Param p_param) {
        super(p_param);
    }
    
    public List getResult(Job p_job, 
                          Workflow p_workflow, 
                          DateFormat p_format, 
                          ProjectWorkflowData workflowData)
    {
        List result = new ArrayList();
        
        Param[] children = this.getParam().getChildParams();
        if (children[0].getValue())
        {
            result.add(p_job.getDisplayState());
        }
        if (children[1].getValue())
        {
            result.add(p_workflow.getState()); 
        }
        if (children[2].getValue())
        {
            // Estimated job completion: 
            // Insert the estimated completion date (and time) for each workflow.
            if (p_workflow.getEstimatedCompletionDate() != null)
            {
                result.add(p_format.format(p_workflow.getEstimatedCompletionDate()));
            }
            else
            {
                result.add("N/A");
            }        
        }
        if (children[3].getValue())
        {
            // Actual job completion: 
            // Insert the date (and time) while the workflow was exported.
            if (p_workflow.getCompletedDate() != null)
            {
                result.add(p_format.format(p_workflow.getCompletedDate()));
            }
            else
            {
                result.add("Not Yet Completed");
            }
        }
        
        return result;
    }

    public List getTotal(ProjectWorkflowData workflowData)
    {
        List result = new ArrayList();
        
        String noneTotal = "N/A";
        
        Param[] children = this.getParam().getChildParams();
        if (children[0].getValue())
        {
            result.add(noneTotal);
        }
        if (children[1].getValue())
        {
            result.add(noneTotal); 
        }
        if (children[2].getValue())
        {
            result.add(noneTotal);
        }
        if (children[3].getValue())
        {
            result.add(noneTotal);
        }
        
        return result;
    }
}
