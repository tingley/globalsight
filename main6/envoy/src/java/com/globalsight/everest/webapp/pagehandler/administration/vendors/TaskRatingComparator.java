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
package com.globalsight.everest.webapp.pagehandler.administration.vendors;

// globalsight
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.vendormanagement.Rating;

// java
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
* This class can be used to compare Tasks and their Rating objects
*/
public class TaskRatingComparator extends StringComparator
{
    //types of comparison
    public static final int ACTIVITY = 0;
    public static final int RATING = 1;
    public static final int DATE = 2;
    public static final int COMMENT = 3;
    public static final int RATER = 4;
    public static final int JOBNAME = 5;
    public static final int JOBID = 6;
    public static final int SRCLOCALE = 7;
    public static final int TARGLOCALE = 8;

    /**
     * Creates a RatingComparator with the given type and locale.
     * If the type is not a valid type, then the default comparison
     * is done by displayName
     */
    public TaskRatingComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Rating objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B) 
    {
        TaskImpl aTask = (TaskImpl) p_A;
        TaskImpl bTask = (TaskImpl) p_B;

        String aValue = null;
        String bValue = null;
        int rv;
        
        Rating aRating = getFirstTaskRating(aTask);
        Rating bRating = getFirstTaskRating(bTask);

        switch (m_type)
        {
            case ACTIVITY:
                if (aTask != null)
                    aValue = aTask.getTaskName();
                if (bTask != null)
                    bValue = bTask.getTaskName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case DATE:
                Date aDate = null;
                Date bDate = null;
                if (aRating != null)
                {
                    aDate = aRating.getModifiedDate();
                }
                else
                {
                    aDate = new Date();
                }
                if (bRating != null)
                {
                    bDate = bRating.getModifiedDate();
                }
                else
                {
                    bDate = new Date();
                }
                    if (aDate.after(bDate))
                        rv = 1;
                    else if (aDate.equals(bDate))
                        rv = 0;
                    else
                        rv = -1;
                    break;
            case COMMENT:
                if (aRating != null)
                    aValue = aRating.getComment();
                if (bRating != null)
                    bValue = bRating.getComment();
                rv = this.compareStrings(aValue,bValue);
                break;
            case RATER:
                if (aRating != null)
                    aValue = aRating.getRaterUserId();
                if (bRating != null)
                    bValue = bRating.getRaterUserId();
                rv = this.compareStrings(aValue,bValue);
                break;
            case JOBNAME:
                if (aTask != null)
                    aValue = aTask.getJobName();
                if (bTask != null)
                    bValue = bTask.getJobName();
                rv = this.compareStrings(aValue,bValue);
                break;
            case JOBID:
                if (aTask != null)
                    aValue = String.valueOf(aTask.getJobId());
                if (bTask != null)
                    bValue = String.valueOf(bTask.getJobId());
                rv = this.compareStrings(aValue,bValue);
                break;
            case SRCLOCALE:
                if (aTask != null)
                    aValue = aTask.getSourceLocale().getDisplayName(getLocale());
                if (bTask != null)
                    bValue = bTask.getSourceLocale().getDisplayName(getLocale());
                rv = this.compareStrings(aValue,bValue);
                break;
            case TARGLOCALE:
                if (aTask != null)
                    aValue = aTask.getTargetLocale().getDisplayName(getLocale());
                if (bTask != null)
                    bValue = bTask.getTargetLocale().getDisplayName(getLocale());
                rv = this.compareStrings(aValue,bValue);
                break;
            case RATING:
            // same as default
            default:
                int aInt = 0;
                int bInt = 0;
                if (aRating != null)
                    aInt = aRating.getValue();
                if (bRating != null)
                    bInt = bRating.getValue();
                if (aInt > bInt)
                    rv = 1;
                else if (aInt == bInt)
                    rv = 0;
                else
                    rv = -1;
                break;
        }
        return rv;
    }

        
    private Rating getFirstTaskRating(TaskImpl p_task)
    {
        List ratings = p_task.getRatings();
        // assume only one at this point
        if (ratings != null && ratings.size() > 0)
        {
            return (Rating)ratings.iterator().next();
        }
        else
        {
            return null;
        }
    }
}
