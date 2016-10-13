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

import java.util.Locale;
import com.globalsight.everest.jobhandler.JobGroup;
import com.globalsight.util.EnvoyDataComparator;

/**
 * Comparator implementation to enable sorting of Jobs objects.
 */
public class JobGroupComparator extends EnvoyDataComparator
{
	/**
	 * TODO
	 */
	private static final long serialVersionUID = 1L;
	public static final int JOB_GROUP_ID = 1;
	public static final int JOB_GROUP_NAME = 2;
	public static final int JOB_GROUP_PROJECT = 3;
	public static final int JOB_GROUP_SOURCE_LOCALE = 4;
	public static final int JOB_GROUP_DATE_CREATED = 5;
	public static final int JOB_GROUP_CREATED_USER = 6;
	public static final int JOB_GROUP_CONPANY_NAME = 7;

	// ////////////////////////////////////////////////////////////////////
	// Begin: Constructor
	// ////////////////////////////////////////////////////////////////////
	 /**
	    * Construct a JobGroupComparator for sorting purposes based on the default locale.
	    * @param sortCol - The column that should be sorted.
	    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
	    */
	    public JobGroupComparator(int p_sortCol, boolean p_sortAsc)
	    {
	        this(p_sortCol, null, p_sortAsc);
	    }


	    /**
	    * Construct a JobGroupComparator for sorting purposes based on a particular locale.
	    * @param sortCol - The column that should be sorted.
	    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
	    */
	    public JobGroupComparator(int p_sortCol, Locale p_locale, 
	                         boolean p_sortAsc)
	    {
	        super(p_sortCol, p_locale, p_sortAsc);
	    }
	// ////////////////////////////////////////////////////////////////////
	// End: Constructor
	// ////////////////////////////////////////////////////////////////////

	public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
	{
		Object objects[] = new Object[2];
		// our objects are Job
		if (o1 instanceof JobGroup && o2 instanceof JobGroup)
		{
			objects = getValues(objects, (JobGroup) o1, (JobGroup) o2,
					sortColumn);
		}
		else
		{
			objects[0] = o1;
			objects[1] = o2;
		}

		return objects;
	}

	private Object[] getValues(Object[] p_objects, JobGroup jobGroup1,
			JobGroup jobGroup2, int p_sortColumn)
	{
		switch (p_sortColumn)
		{
			default:// should always be first column in job list (intentional
					// fall through)
				compareByJobGroupId(p_objects, jobGroup1, jobGroup2);
				break;
			case JOB_GROUP_NAME:
				compareByJobGroupName(p_objects, jobGroup1, jobGroup2);
				break;
			case JOB_GROUP_PROJECT:
				compareByProject(p_objects, jobGroup1, jobGroup2);
				break;
			case JOB_GROUP_SOURCE_LOCALE:
				compareBySourceLocale(p_objects, jobGroup1, jobGroup2);
				break;
			case JOB_GROUP_DATE_CREATED:
				compareByCreationDate(p_objects, jobGroup1, jobGroup2);
				break;
			case JOB_GROUP_CREATED_USER:
				compareByCreateUser(p_objects, jobGroup1, jobGroup2);
				break;
			case JOB_GROUP_CONPANY_NAME:
				compareByCompanyName(p_objects, jobGroup1, jobGroup2);
				break;
		}

		return p_objects;
	}

	private void compareByJobGroupId(Object[] p_objects, JobGroup p_jobGroup1,
			JobGroup p_jobGroup2)
	{
		p_objects[0] = new Long(p_jobGroup1.getId());
		p_objects[1] = new Long(p_jobGroup2.getId());
	}

	private void compareByJobGroupName(Object[] p_objects, JobGroup p_jobGroup1,
			JobGroup p_jobGroup2)
	{
		p_objects[0] = p_jobGroup1.getName();
		p_objects[1] = p_jobGroup2.getName();
	}

	private void compareByProject(Object[] p_objects, JobGroup p_jobGroup1,
			JobGroup p_jobGroup2)
	{
		p_objects[0] = p_jobGroup1.getProject().getName();
		p_objects[1] = p_jobGroup2.getProject().getName();
	}

	private void compareBySourceLocale(Object[] p_objects, JobGroup p_jobGroup1,
			JobGroup p_jobGroup2)
	{
		p_objects[0] = p_jobGroup1.getSourceLocale().getDisplayName();
		p_objects[1] = p_jobGroup2.getSourceLocale().getDisplayName();
	}

	private void compareByCreationDate(Object[] p_objects,
			JobGroup p_jobGroup1, JobGroup p_jobGroup2)
	{
		p_objects[0] = p_jobGroup1.getCreateDate();
		p_objects[1] = p_jobGroup2.getCreateDate();
	}

	private void compareByCreateUser(Object[] p_objects, JobGroup p_jobGroup1,
			JobGroup p_jobGroup2)
	{
		p_objects[0] = p_jobGroup1.getCreateUser().getUserName();
		p_objects[1] = p_jobGroup2.getCreateUser().getUserName();
	}
	
	private void compareByCompanyName(Object[] p_objects, JobGroup p_jobGroup1,
			JobGroup p_jobGroup2)
	{
		p_objects[0] = p_jobGroup1.getCreateUser().getCompanyName();
		p_objects[1] = p_jobGroup2.getCreateUser().getCompanyName();
	}
}
