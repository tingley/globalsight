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
import java.util.HashMap;

import com.globalsight.reports.WorkflowTableModel;

public class AvgPerCompReportDataWrap extends BaseDataWrap 
{
	//Data Structure of AvgPerCompMap:
    //Jobid   <---Maps to---> ArrayList(4)
    //ArrayList(0): JobFormLabel (ArrayList);
    //ArrayList(1): JobFormValue (ArrayList);
    //ArrayList(2): WorkFlowTableSubColumn (Array);
    //ArrayList(3): WorkFlowTableModel (WorkflowTableModel);
	private HashMap avgPerCompMap = new HashMap();

	public HashMap getAvgPerCompMap() 
    {
		return avgPerCompMap;
	}
	public void setAvgPerCompMap(HashMap avgPerCompMap) 
    {
		this.avgPerCompMap = avgPerCompMap;
	}
   
    public ArrayList getJobFormLabel(Long jobId) 
    {
    	ArrayList tmp = getListByJobId(jobId);
    	return (ArrayList) tmp.get(0);
	}
   
	public ArrayList getJobFormValue(Long jobId) 
    {
		ArrayList tmp = getListByJobId(jobId);
		return (ArrayList) tmp.get(1);
	}
   
	public int[] getWorkflowTableSubColumns(Long jobId) 
    {
		ArrayList tmp = getListByJobId(jobId);
		return (int[]) tmp.get(2);
	}
   
	public WorkflowTableModel getWorkflowTableModel(Long jobId) 
    {
		ArrayList tmp = getListByJobId(jobId);
		return (WorkflowTableModel) tmp.get(3);
	}
   
	private ArrayList getListByJobId(Long jobId)
    {
		ArrayList tmp = null;
		if(avgPerCompMap.containsKey(jobId)) 
        {
			tmp = (ArrayList)avgPerCompMap.get(jobId);
		} 
        else 
        {
			tmp =  new ArrayList();
			avgPerCompMap.put(jobId, tmp);
		}
        
		return tmp;
	}

}
