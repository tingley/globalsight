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
package com.globalsight.everest.workflow;

import java.util.Comparator;

import com.globalsight.everest.workflow.WorkflowTask;

/**
 * Comparator implementation to enable sorting of WorkflowTasks.
 */
public class WorkflowTaskComparator implements Comparator
{
    private static final int lessThan = -1;
    private static final int greaterThan = 1;
    private static final int equalTo = 0;

    public int compare(Object p_object1, Object p_object2)
    {
	    WorkflowTask task1 = (WorkflowTask)p_object1;
	    WorkflowTask task2 = (WorkflowTask)p_object2;
	
	    if (task1 != null && task2 != null)
	    {
	        if (task1.getSequence() < task2.getSequence())
            {
		        return lessThan;
            }
	        if (task1.getSequence() > task2.getSequence())
            {
		        return greaterThan;
            }
		    return equalTo;
        }
        if (task1 != null)
        {
            return greaterThan;
        }
        if (task2 != null)
        {
            return lessThan;
        }
        return equalTo;       
	}
}	
