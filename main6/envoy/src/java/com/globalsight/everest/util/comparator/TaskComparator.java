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
package com.globalsight.everest.util.comparator;    

import java.util.Date;
import java.util.Locale;

import com.globalsight.everest.taskmanager.Task;

/**
* This class can be used to compare Task objects
*/
public class TaskComparator extends StringComparator
{
    private static final long serialVersionUID = 1L;

    //types of Task comparison
    public static final int JOB_NAME            = 0;
    public static final int JOB_ID              = 1;
    public static final int ACTIVITY            = 2;
    public static final int BAND1               = 3;
    public static final int BAND2               = 4;
    public static final int BAND3               = 5;
    public static final int BAND4               = 6;
    public static final int NO_MATCH            = 7;
    public static final int REPETITIONS         = 8;
    public static final int CONTEXT             = 9;
    public static final int WC_TOTAL            = 10;
    public static final int EXACT               = 11;
//    public static final int SUBLEVMATCH         = 12;
//    public static final int SUBLEVREP           = 13;
    public static final int LMT                 = 14;
    public static final int TOTAL_FUZZY         = 15;
    public static final int IN_CONTEXT          = 16;
    public static final int NO_USE_IN_CONTEXT   = 17;
    public static final int TOTAL_EXACT        = 18;
    public static final int DEFAULT_CONTEXT_EXACT = 19;
    public static final int HIFUZZYREPETITION   = 20;
    public static final int MEDHIFUZZYREPETITION   = 21;
    public static final int MEDFUZZYREPETITION   = 22;
    public static final int COMPLETE_DATE = 23;

	/**
	* Creates a TaskComparator with the given locale.
	*/
	public TaskComparator(Locale p_locale)
	{
	    super(p_locale);
	}

    public TaskComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }
    
	/**
	* Performs a comparison of two Task objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
		Task a = (Task)p_A;
		Task b = (Task)p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
        default:
		case JOB_NAME:
			aValue = a.getJobName();
			bValue = b.getJobName();
			rv = this.compareStrings(aValue,bValue);
			break;
 
		case JOB_ID:
			long along = a.getWorkflow().getJob().getJobId();
			long blong = b.getWorkflow().getJob().getJobId();
            if (along > blong)
               rv = 1;
            else if (along == blong)
               rv = 0;
            else
               rv = -1;
			break;

        case ACTIVITY:
			aValue = a.getTaskName();
			bValue = b.getTaskName();        
			rv = aValue.compareTo(bValue);
			break;

        case EXACT:
			int aint = a.getWorkflow().getSegmentTmWordCount();
			int bint = b.getWorkflow().getSegmentTmWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
            break;

        case BAND1:  
			aint = a.getWorkflow().getHiFuzzyMatchWordCount();
			bint = b.getWorkflow().getHiFuzzyMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case BAND2:  
			aint = a.getWorkflow().getMedHiFuzzyMatchWordCount();
			bint = b.getWorkflow().getMedHiFuzzyMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case BAND3:  
			aint = a.getWorkflow().getMedFuzzyMatchWordCount();
			bint = b.getWorkflow().getMedFuzzyMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case BAND4:  
			aint = a.getWorkflow().getLowFuzzyMatchWordCount();
			bint = b.getWorkflow().getLowFuzzyMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case NO_MATCH:
			aint = a.getWorkflow().getNoMatchWordCount();
			bint = b.getWorkflow().getNoMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case REPETITIONS:
			aint = a.getWorkflow().getRepetitionWordCount();
			bint = b.getWorkflow().getRepetitionWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case CONTEXT:
			aint = a.getWorkflow().getContextMatchWordCount();
			bint = b.getWorkflow().getContextMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;
        case DEFAULT_CONTEXT_EXACT:
            aint = a.getWorkflow().getSegmentTmWordCount() - a.getWorkflow().getContextMatchWordCount();
            bint = b.getWorkflow().getSegmentTmWordCount() - b.getWorkflow().getContextMatchWordCount();
            if (aint > bint)
                rv = 1;
            else if (aint == bint)
                rv = 0;
            else
                rv = -1;
            break;

        case IN_CONTEXT:
            aint = a.getWorkflow().getInContextMatchWordCount();
            bint = b.getWorkflow().getInContextMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
            break;

        case NO_USE_IN_CONTEXT:
            aint = a.getWorkflow().getNoUseInContextMatchWordCount();
            bint = b.getWorkflow().getNoUseInContextMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
            break;

        case TOTAL_EXACT:
            aint = a.getWorkflow().getTotalExactMatchWordCount();
            bint = b.getWorkflow().getTotalExactMatchWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
            break;

        case LMT:
    	    aint = a.getWorkflow().getJob().getLeverageMatchThreshold();
    	    bint = b.getWorkflow().getJob().getLeverageMatchThreshold();
    	    if (aint > bint)
    	       rv = 1;
    	    else if (aint == bint)
    	       rv = 0;
    	    else
    	       rv = -1;
    	    break;

        case TOTAL_FUZZY:  // total fuzzy adjusted based on LMT
            aint = (a.getWorkflow().getLowFuzzyMatchWordCount() + 
                        a.getWorkflow().getMedFuzzyMatchWordCount() + 
                        a.getWorkflow().getMedHiFuzzyMatchWordCount() +
                        a.getWorkflow().getHiFuzzyMatchWordCount());

            bint = (b.getWorkflow().getLowFuzzyMatchWordCount() + 
                    b.getWorkflow().getMedFuzzyMatchWordCount() + 
                    b.getWorkflow().getMedHiFuzzyMatchWordCount() +
                    b.getWorkflow().getHiFuzzyMatchWordCount());
    	    if (aint > bint)
    	       rv = 1;
    	    else if (aint == bint)
    	       rv = 0;
    	    else
    	       rv = -1;
    	    break;

        case WC_TOTAL: 
			aint = a.getWorkflow().getTotalWordCount();
			bint = b.getWorkflow().getTotalWordCount();
            if (aint > bint)
               rv = 1;
            else if (aint == bint)
               rv = 0;
            else
               rv = -1;
			break;

        case COMPLETE_DATE:
            Date dt1 = a.getCompletedDate();
            Date dt2 = b.getCompletedDate();

            if (dt1 == null && dt2 == null) {
                // both have no completed date, go to top of the list
                rv = 0;
            } else if (dt1 == null && dt2 != null) {
                rv = 1;
            } else if (dt1 != null && dt2 == null) {
                rv = -1;
            } else {
                rv = dt1.compareTo(dt2);
            }
            break;
		}

		return rv;
	}
}

