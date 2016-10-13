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

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.EnvoyDataComparator;
import com.globalsight.util.GlobalSightLocale;

/**
 * To serve for sorting Tasks based on specified column.
 * The column number matches the column order on TaskList UI screen.
*/
public class WorkflowTaskDataComparator extends EnvoyDataComparator
{
    // Sort Column IDs 
    // These are pseudo column numbers which allow us to decouple the actual 
    // UI columns from the column numbers previously used in WorkflowDataComparator.
    // The JSP just uses these ids instead of the actual column number to define
    // the sort order. (the columns keep getting rearranged)
    public static final int JOB_ID              = 1;
    public static final int PRIORITY            = 2;
    public static final int JOB_NAME            = 3;
    public static final int JOB_NAME_TASK_NAME  = 4;
    public static final int SOURCE_LOCALE       = 5;
    public static final int TARGET_LOCALE       = 6;
    public static final int DUE_DATE            = 7;
    public static final int ACCEPT_DATE         = 8;
    public static final int COMPLETED_DATE      = 9;
    public static final int PRJ_MANGER_NAME     = 10;
    public static final int ASSIGNEE            = 11;
    public static final int EXACT_WRDCNT        = 12; // segment tm
    public static final int FUZZY_WRDCNT        = 13; // 95-99%
    public static final int NOMATCH_WRDCNT      = 14;
    public static final int REPETITION_WRDCNT   = 15;
    public static final int TOTAL_WRDCNT        = 16;
    public static final int EST_COMP_DATE       = 17;
    public static final int CONTEXT_WRDCNT      = 18;
    public static final int MED_HI_FUZZY_WRDCNT = 19; // 85-94%
    public static final int MED_FUZZY_WRDCNT    = 20; // 75-84%
    public static final int LOW_FUZZY_WRDCNT    = 21; // 50-74%
   
    public static final int COMPANY_NAME    	= 22;
    
    private int m_totalContextCount = 0;
    private int m_totalSegmentTmCount = 0;
    private int m_totalLowFuzzyCount = 0;
    private int m_totalMedFuzzyCount = 0;
    private int m_totalMedHiFuzzyCount = 0;
    private int m_totalHiFuzzyCount = 0;
    private int m_totalNoMatchCount = 0;
    private int m_totalRepetitionCount = 0;
    private int m_totalWordCount = 0;
    
    //////////////////////////////////////////////////////////////////////////////////
    //  Begin:  Constructor
    //////////////////////////////////////////////////////////////////////////////////
    /**
    * Construct a WorkflowTaskDataComparator for sorting purposes based on the default locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public WorkflowTaskDataComparator(int p_sortCol, boolean p_sortAsc)
    {
        this(p_sortCol, null, p_sortAsc);
    }


    /**
    * Construct a WorkflowTaskDataComparator for sorting purposes based on a particular locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public WorkflowTaskDataComparator(int p_sortCol, GlobalSightLocale p_locale, boolean p_sortAsc)
    {
        super(p_sortCol, p_locale, p_sortAsc);
    }
    //////////////////////////////////////////////////////////////////////////////////
    //  End:  Constructor
    //////////////////////////////////////////////////////////////////////////////////


    public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
    {
        Object objects[] = new Object[2];
        // our objects are Task
        if(o1 instanceof Task && o2 instanceof Task)
        {
            objects = getValues(objects, o1, o2, sortColumn);
        }
        else
        {
            objects[0] = o1;
            objects[1] = o2;
        }

        return objects;
    }

    private Object[] getValues(Object[] p_objects, Object o1, Object o2, int p_sortColumn)
    {

        switch(p_sortColumn)
        {
        default:  // should always be first column in task list
                  // intentional fall through
        case JOB_ID: // job id
            p_objects[0] = new Long(((Task)o1).getWorkflow().getJob().getJobId());
            p_objects[1] = new Long(((Task)o2).getWorkflow().getJob().getJobId());
            break;           
        case JOB_NAME: // job name
            p_objects[0] = ((Task)o1).getJobName();
            p_objects[1] = ((Task)o2).getJobName();
            break;           
        case JOB_NAME_TASK_NAME: // job name + task name
            p_objects[0] = ((Task)o1).getJobName() + ((Task)o1).getTaskName();
            p_objects[1] = ((Task)o2).getJobName() + ((Task)o2).getTaskName();
            break;
        case SOURCE_LOCALE: //source locale
            p_objects[0] = ((Task)o1).getSourceLocale().getDisplayName();
            p_objects[1] = ((Task)o2).getSourceLocale().getDisplayName();
            break;
        case TARGET_LOCALE: // target locale
            p_objects[0] = ((Task)o1).getTargetLocale().getDisplayName();
            p_objects[1] = ((Task)o2).getTargetLocale().getDisplayName();
            break;
        case DUE_DATE: //  due by
            p_objects[0] = ((Task)o1).getEstimatedCompletionDate();
            p_objects[1] = ((Task)o2).getEstimatedCompletionDate();
            break;            
        case PRJ_MANGER_NAME: //Project manager name
            p_objects[0] = ((Task)o1).getProjectManagerName();
            p_objects[1] = ((Task)o2).getProjectManagerName();
            break;
        case PRIORITY: //Priority
            p_objects[0] = new Integer (((Task)o1).getPriority());
            p_objects[1] = new Integer (((Task)o2).getPriority());
            break;
        case ACCEPT_DATE: //  accepted by
            p_objects[0] = ((Task)o1).getEstimatedAcceptanceDate();
            p_objects[1] = ((Task)o2).getEstimatedAcceptanceDate();
            break;
        case COMPLETED_DATE: //  completed date
            p_objects[0] = ((Task)o1).getCompletedDate();
            p_objects[1] = ((Task)o2).getCompletedDate();
            break;
        case ASSIGNEE: //Assignee
            p_objects[0] = ((Task)o1).getAllAssigneesAsString();
            p_objects[1] = ((Task)o2).getAllAssigneesAsString();
            break;
        case EXACT_WRDCNT: // exact word count (Segment TM)
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalSegmentTmCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalSegmentTmCount);
            break;
        case CONTEXT_WRDCNT: // exact context match word count
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalContextCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalContextCount);
            break;
        case FUZZY_WRDCNT: // HI fuzzy word count             
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalHiFuzzyCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalHiFuzzyCount);
            break;
        case MED_HI_FUZZY_WRDCNT: // Med-Hi fuzzy word count            
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalMedHiFuzzyCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalMedHiFuzzyCount);
            break;
        case MED_FUZZY_WRDCNT: // MED fuzzy word count            
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalMedFuzzyCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalMedFuzzyCount);
            break;
        case LOW_FUZZY_WRDCNT: // low fuzzy word count            
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalLowFuzzyCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalLowFuzzyCount);
            break;
        case NOMATCH_WRDCNT: // No Match word count            
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalNoMatchCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalNoMatchCount);
            break;
        case REPETITION_WRDCNT: // Repetition word count            
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalRepetitionCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalRepetitionCount);
            break;
        case TOTAL_WRDCNT: // Total word count            
            getWordCountTotals((Task)o1);
            p_objects[0] = new Integer(m_totalWordCount);
            getWordCountTotals((Task)o2);
            p_objects[1] = new Integer(m_totalWordCount);
            break;
        case EST_COMP_DATE: // Estimated Completion Date
            p_objects[0] = ((Task)o1).getEstimatedCompletionDate();
            p_objects[1] = ((Task)o2).getEstimatedCompletionDate();
            break;
        case COMPANY_NAME: // Company Name
            p_objects[0] = CompanyWrapper.getCompanyNameById(((Task)o1).getCompanyId());
            p_objects[1] = CompanyWrapper.getCompanyNameById(((Task)o2).getCompanyId());
            break;
	}
        return p_objects;
    }

    private void getWordCountTotals(Task p_task)
    {
        m_totalContextCount = m_totalSegmentTmCount = 
            m_totalLowFuzzyCount = m_totalMedFuzzyCount = 
            m_totalMedHiFuzzyCount = m_totalHiFuzzyCount = 
            m_totalNoMatchCount = m_totalWordCount = 0;
        
        // no longer need to build word counts, just get the totals from workflow
        Workflow wf = p_task.getWorkflow();
        
        m_totalContextCount = wf.getContextMatchWordCount();
        m_totalSegmentTmCount = wf.getSegmentTmWordCount();
        m_totalLowFuzzyCount = wf.getLowFuzzyMatchWordCount();
        m_totalMedFuzzyCount = wf.getMedFuzzyMatchWordCount();
        m_totalMedHiFuzzyCount = wf.getMedHiFuzzyMatchWordCount();
        m_totalHiFuzzyCount = wf.getHiFuzzyMatchWordCount();
        m_totalNoMatchCount = wf.getNoMatchWordCount();
        m_totalRepetitionCount = wf.getRepetitionWordCount();
        m_totalWordCount = wf.getTotalWordCount();                
    }

}
