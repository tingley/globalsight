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
package com.globalsight.everest.webapp.pagehandler.projects.workflows;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.EnvoyDataComparator;

/**
 * Comparator implementation to enable sorting of Jobs objects.
 */
public class JobComparator extends EnvoyDataComparator
{
	private static final long serialVersionUID = 1L;
	public static final int JOB_ID	  = 1;
    public static final int JOB_NAME	  = 2;
    public static final int PROJECT    	  = 3;
    public static final int SOURCE_LOCALE = 4;
    public static final int DATA_SOURCE	  = 5;
    public static final int WORD_COUNT	  = 6;
    public static final int DATE_CREATED  = 7;
    public static final int PRIORITY      = 8;
    public static final int EST_COMPLETION_DATE      = 9;
    public static final int PLANNED_DATE  = 10 ;
    public static final int JOB_STATUS  = 11 ;
    // For sla report issue
    public static final int EST_TRANSLATE_COMPLETION_DATE = 12;
    public static final int JOB_GROUP_ID  = 13 ;
    private Currency m_currency = null; // not used anymore since costing removed
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
    * Construct a JobComparator for sorting purposes based on the default locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public JobComparator(int p_sortCol, boolean p_sortAsc, 
                         Currency p_currency)
    {
        this(p_sortCol, null, p_sortAsc, p_currency);
    }


    /**
    * Construct a JobComparator for sorting purposes based on a particular locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public JobComparator(int p_sortCol, Locale p_locale, 
                         boolean p_sortAsc, Currency p_currency)
    {
        super(p_sortCol, p_locale, p_sortAsc);
        m_currency = p_currency;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
    {
        Object objects[] = new Object[2];
        // our objects are Job
        if(o1 instanceof Job && o2 instanceof Job)
        {
            objects = getValues(objects, (Job)o1, (Job)o2, sortColumn);
        }
        else
        {
            objects[0] = o1;
            objects[1] = o2;
        }

        return objects;        
    }

    private Object[] getValues(Object[] p_objects, Job job1, Job job2, int p_sortColumn)
	{
		switch (p_sortColumn)
		{
			default:// should always be first column in job list (intentional fall through)
				compareByJobId(p_objects, job1, job2);
				break;
			case JOB_NAME:
				compareByJobName(p_objects, job1, job2);
				break;
			case PROJECT:
				compareByProject(p_objects, job1, job2);
				break;
			case SOURCE_LOCALE:
				compareBySourceLocale(p_objects, job1, job2);
				break;
			case DATA_SOURCE:
				compareByDataSource(p_objects, job1, job2);
				break;
			case WORD_COUNT:
				compareByWordCount(p_objects, job1, job2);
				break;
			case DATE_CREATED:
				compareByCreationDate(p_objects, job1, job2);
				break;
			case PRIORITY:
				compareByPriority(p_objects, job1, job2);
				break;
			case EST_COMPLETION_DATE:
				compareByEstiCompletionDate(p_objects, job1, job2);
				break;
			case PLANNED_DATE:
				compareByPlannedDate(p_objects, job1, job2);
				break;
			case JOB_STATUS:
				compareByJobDisplayStatus(p_objects, job1, job2);
				break;
			case EST_TRANSLATE_COMPLETION_DATE:
				compareByTranslateCompletionDate(p_objects, job1, job2);
				break;
			case JOB_GROUP_ID:
				compareByJobGroupId(p_objects, job1, job2);
				break;
		}

		return p_objects;
	}
                              
    private void compareByJobId(Object[] p_objects, Job p_job1, Job p_job2)
    {
	p_objects[0] = new Long(p_job1.getJobId());
	p_objects[1] = new Long(p_job2.getJobId());	
    }
    
    private void compareByJobGroupId(Object[] p_objects, Job p_job1, Job p_job2)
    {
	p_objects[0] =  p_job1.getGroupId();
	p_objects[1] =  p_job2.getGroupId();	
    }
    
    private void compareByJobName(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = p_job1.getJobName();
        p_objects[1] = p_job2.getJobName();
    }	

    private void compareByProject(Object[] p_objects, Job p_job1, Job p_job2)
    {
	L10nProfile l10nProfile1 = null;
	L10nProfile l10nProfile2 = null;
	try
	{
	    l10nProfile1 = LocProfileHandlerHelper.getL10nProfile(p_job1.getL10nProfileId());
	    l10nProfile2 = LocProfileHandlerHelper.getL10nProfile(p_job2.getL10nProfileId());
	}
	catch (Exception e)
	{
	}
        p_objects[0] = l10nProfile1 == null ? p_job1.getJobName() : 
            l10nProfile1.getProject().getName();
        p_objects[1] = l10nProfile2 == null ? p_job2.getJobName() : 
            l10nProfile2.getProject().getName();	
    }

    private void compareBySourceLocale(Object[] p_objects, Job p_job1, Job p_job2)
    {
	p_objects[0] = p_job1.getSourceLocale().getDisplayName();
	p_objects[1] = p_job2.getSourceLocale().getDisplayName();	
    }	

    private void compareByDataSource(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = p_job1.getDataSourceName();
        p_objects[1] = p_job2.getDataSourceName();
    }

    private void compareByWordCount(Object[] p_objects, Job p_job1, Job p_job2)
    {
	p_objects[0] = new Integer(p_job1.getWordCount());
	p_objects[1] = new Integer(p_job2.getWordCount());	
    }

    private void compareByCreationDate(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = p_job1.getCreateDate();
        p_objects[1] = p_job2.getCreateDate();
    }

    private void compareByPriority(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = new Integer(p_job1.getPriority());
        p_objects[1] = new Integer(p_job2.getPriority());
    	
    }

    // For sla report issue
    private void compareByEstiCompletionDate(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = p_job1.getDueDate();
        p_objects[1] = p_job2.getDueDate();
    }
    
    private void compareByTranslateCompletionDate(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = getEstimatedTranslateCompletionDate(p_job1);
        p_objects[1] = getEstimatedTranslateCompletionDate(p_job2);
    }

    private void compareByPlannedDate(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = getPlannedDate(p_job1);
        p_objects[1] = getPlannedDate(p_job2);
    }

    private Date getPlannedDate(Job p_job)
    {
        Collection wfs = p_job.getWorkflows();
        Date planned = null;
        Iterator iter = wfs.iterator();
        while (iter.hasNext())
        {
            Workflow wf = (Workflow)iter.next();
            if (Workflow.CANCELLED.equals(wf.getState())) continue;
            
            Date d = wf.getPlannedCompletionDate();
            if (planned == null || planned.compareTo(d) < 0)
            {
                planned = d;
            }
        }
        return planned;
    }
    
    private void compareByJobDisplayStatus(Object[] p_objects, Job p_job1, Job p_job2)
    {
        p_objects[0] = p_job1.getDisplayState();
        p_objects[1] = p_job2.getDisplayState();
    }
    
    private Date getEstimatedTranslateCompletionDate(Job p_job)
    {
        Collection wfs = p_job.getWorkflows();
        Date estimated = null;
        Iterator iter = wfs.iterator();
        while (iter.hasNext())
        {
            Workflow wf = (Workflow)iter.next();
            if (Workflow.CANCELLED.equals(wf.getState())) continue;
            
            Date d = wf.getEstimatedTranslateCompletionDate();
            if ((d != null) && (estimated == null || estimated.compareTo(d) < 0))
            {
                estimated = d;
            }
        }
        return estimated;
    }
}
