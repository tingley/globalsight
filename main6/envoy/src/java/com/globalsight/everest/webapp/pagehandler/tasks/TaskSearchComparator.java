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

package com.globalsight.everest.webapp.pagehandler.tasks;

import java.util.Comparator;
import org.apache.log4j.Logger;

/**
 * TaskSearchComparator is used to sort List<TaskVo> 
 */
public class TaskSearchComparator implements Comparator<TaskVo>
{
    private int sortColumn = -1;
    private boolean sortAscending = true;

    public TaskSearchComparator(int p_sortCol, boolean p_sortAsc)
    {
        this.sortColumn = p_sortCol;
        this.sortAscending = p_sortAsc;
    }

    @Override
    public int compare(TaskVo o1, TaskVo o2)
    {
        int r = 0;
        switch (sortColumn)
        {
        case WorkflowTaskDataComparator.JOB_ID:
            r = (int) (o1.getJobId() - o2.getJobId());
            break;
                
        case WorkflowTaskDataComparator.JOB_NAME_TASK_NAME:
        case WorkflowTaskDataComparator.JOB_NAME:
            r = o1.getJobName().compareTo(o2.getJobName());
            break;  
        case WorkflowTaskDataComparator.TOTAL_WRDCNT: 
            r = o1.getWordCount() - o2.getWordCount();
            break;  
        case WorkflowTaskDataComparator.EST_COMP_DATE: // Estimated Completion Date

            if (o1.getEstimatedCompletionDate() == null
                    || o2.getEstimatedCompletionDate() == null)
            {
                r = 0;
            }
            else
            {
                r = o1.getEstimatedCompletionDate().compareTo(
                        o2.getEstimatedCompletionDate());
            }

            break;  
        }
        
        if (!sortAscending)
            r = 0 - r;
        return r;
    }
}
