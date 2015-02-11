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
package com.globalsight.everest.webapp.pagehandler.projects.jobvo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import com.globalsight.util.EnvoyDataComparator;

/**
 * Comparator implementation to enable sorting of Jobs objects.
 */
public class JobVoComparator extends EnvoyDataComparator
{
	private static final long serialVersionUID = 2392591861602794020L;
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
    public static final int EST_TRANSLATE_COMPLETION_DATE = 12;
    public static final int JOB_GROUP_ID	  = 13;
    private static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
    * Construct a JobComparator for sorting purposes based on the default locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public JobVoComparator(int p_sortCol, boolean p_sortAsc)
    {
        this(p_sortCol, null, p_sortAsc);
    }


    /**
    * Construct a JobComparator for sorting purposes based on a particular locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public JobVoComparator(int p_sortCol, Locale p_locale, 
                         boolean p_sortAsc)
    {
        super(p_sortCol, p_locale, p_sortAsc);
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

    public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
    {
        Object objects[] = new Object[2];
        // our objects are Job
        if(o1 instanceof JobVo && o2 instanceof JobVo)
        {
            objects = getValues(objects, (JobVo)o1, (JobVo)o2, sortColumn);
        }
        else
        {
            objects[0] = o1;
            objects[1] = o2;
        }

        return objects;        
    }

    private Object[] getValues(Object[] p_objects, JobVo job1, JobVo job2, int p_sortColumn)
    {
        switch (p_sortColumn)
        {
            default:// should always be first column in JobVo list (intentional fall through)
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
                compareByCompletionDate(p_objects, job1, job2);
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
            	compareByGroupId(p_objects, job1, job2);
                break;
        }

        return p_objects;
    }
                              
    private void compareByJobId(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
	    p_objects[0] = new Long(p_job1.getId());
	    p_objects[1] = new Long(p_job2.getId());	
    }
    
    private void compareByJobName(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
        p_objects[0] = p_job1.getName();
        p_objects[1] = p_job2.getName();
    }	

    private void compareByProject(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
    	p_objects[0] = p_job1.getProject();
        p_objects[1] = p_job2.getProject();
    }

    private void compareBySourceLocale(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
	    p_objects[0] = p_job1.getSourceLocale();
	    p_objects[1] = p_job2.getSourceLocale();	
    }

    private void compareByWordCount(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
	    p_objects[0] = new Integer(p_job1.getWordcount());
	    p_objects[1] = new Integer(p_job2.getWordcount());	
    }

    private void compareByCreationDate(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
        try
        {
			p_objects[0] = sdf.parse(p_job1.getCreateDate());
			p_objects[1] = sdf.parse(p_job2.getCreateDate());
		} 
		catch (ParseException e)
		{
	        p_objects[0] = p_job1.getCreateDate();
	        p_objects[1] = p_job2.getCreateDate();
		}
    }

    private void compareByPriority(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
        p_objects[0] = new Integer(p_job1.getPriority());
        p_objects[1] = new Integer(p_job2.getPriority());
    	
    }
    
    private void compareByCompletionDate(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
    	try
        {
			p_objects[0] = sdf.parse(p_job1.getPlannedCompletionDate());
			p_objects[1] = sdf.parse(p_job2.getPlannedCompletionDate());
		} 
		catch (ParseException e)
		{
			p_objects[0] = p_job1.getPlannedCompletionDate();
	        p_objects[1] = p_job2.getPlannedCompletionDate();
		}
    }

    private void compareByPlannedDate(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
    	try
        {
			p_objects[0] = sdf.parse(p_job1.getPlannedCompletionDate());
			p_objects[1] = sdf.parse(p_job2.getPlannedCompletionDate());
		} 
		catch (ParseException e)
		{
			p_objects[0] = p_job1.getPlannedCompletionDate();
	        p_objects[1] = p_job2.getPlannedCompletionDate();
		}
    }

    
    private void compareByJobDisplayStatus(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
        p_objects[0] = p_job1.getStatues();
        p_objects[1] = p_job2.getStatues();
    }
    
    private void compareByTranslateCompletionDate(Object[] p_objects, JobVo p_job1, JobVo p_job2)
    {
    	try
        {
			p_objects[0] = sdf.parse(p_job1.getEstimatedTranslateCompletionDate());
			p_objects[1] = sdf.parse(p_job2.getEstimatedTranslateCompletionDate());
		} 
		catch (ParseException e)
		{
			p_objects[0] = p_job1.getEstimatedTranslateCompletionDate();
	        p_objects[1] = p_job2.getEstimatedTranslateCompletionDate();
		}
    }
    
    private void compareByGroupId(Object[] p_objects, JobVo p_job1, JobVo p_job2)
	{
		if (p_job1.getGroupId() == null || p_job1.getGroupId().equals(""))
		{
			p_objects[0] = 0;
		}
		else
		{
			p_objects[0] = new Long(p_job1.getGroupId());
		}
		if (p_job2.getGroupId() == null || p_job2.getGroupId().equals(""))
		{
			p_objects[1] = 0;
		}
		else
		{
			p_objects[1] = new Long(p_job2.getGroupId());
		}
	}
}
