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
package com.globalsight.reports.datawrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.globalsight.reports.WorkflowTableModel;


public class WorkflowStatusReportDataWrap extends BaseDataWrap
{
    private static final long serialVersionUID = 6246958804567243345L;

    //Data Structure of job2WorkFlowMap:
    //Jobid   <---Maps to---> ArrayList(4)
    //ArrayList(0): JobFormLabel (ArrayList);
    //ArrayList(1): JobFormValue (ArrayList);
    //ArrayList(2): WorkFlowTableSubColumn (Array);
    //ArrayList(3): WorkFlowTableModel (WorkflowTableModel);
    private LinkedHashMap<Object, ArrayList<Object>> job2WorkFlowMap = new LinkedHashMap<Object, ArrayList<Object>>();
  
    public LinkedHashMap<Object, ArrayList<Object>> getJob2WorkFlowMap()
    {
        return job2WorkFlowMap;
    }
       
    public void setJob2WorkFlowMap(LinkedHashMap<Object, ArrayList<Object>> linkedHashMap) 
    {
        job2WorkFlowMap = linkedHashMap;
    }
       
    public ArrayList<?> getJobFormLabel(Long jobId) 
    {
       return (ArrayList<?>) getListByJobId(jobId).get(0);
    }
       
    public ArrayList<?> getJobFormValue(Long jobId) 
    {
        return (ArrayList<?>) getListByJobId(jobId).get(1);
    }
       
    public int[] getWorkflowTableSubColumns(Long jobId)
    {
        return (int[]) getListByJobId(jobId).get(2);
    }

    public WorkflowTableModel getWorkflowTableModel(Long jobId) 
    {
        return (WorkflowTableModel) getListByJobId(jobId).get(3);
    }

    private ArrayList<Object> getListByJobId(Long jobId)
    {
        ArrayList<Object> tmp = null;
        if(job2WorkFlowMap.containsKey(jobId))
        {
            tmp = job2WorkFlowMap.get(jobId);
        }
        else
        {
            tmp =  new ArrayList<Object>();
            job2WorkFlowMap.put(jobId, tmp);
        }

        return tmp;
    }



}
