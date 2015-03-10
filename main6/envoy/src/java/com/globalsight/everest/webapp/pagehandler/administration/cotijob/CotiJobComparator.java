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
package com.globalsight.everest.webapp.pagehandler.administration.cotijob;

import java.util.Locale;

import com.globalsight.everest.coti.COTIJob;
import com.globalsight.util.EnvoyDataComparator;

/**
 * Comparator implementation to enable sorting of COTI COTIJobs objects.
 */
public class CotiJobComparator extends EnvoyDataComparator
{
    public static final int JOB_ID = 1;
    /*
     * private String cotiProjectId; private String cotiProjectName; private
     * long globalsightJobId; private String status; private String sourceLang;
     * private String targetLang; private Date creationDate;
     */
    public static final int COTI_PORJECT_ID = 2;
    public static final int COTI_PORJECT_NAME = 3;
    public static final int GLOBALSIGHT_JOB_ID = 4;
    public static final int STATUS = 5;
    public static final int SOURCE_LOCALE = 6;
    public static final int TARGET_LOCALE = 7;
    public static final int CREATION_DATE = 8;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Construct a CotiJobComparator for sorting purposes based on the default
     * locale.
     * 
     * @param sortCol
     *            - The column that should be sorted.
     * @param sortAsc
     *            - A boolean that determines whether the sort is ascending or
     *            descending.
     */
    public CotiJobComparator(int p_sortCol, boolean p_sortAsc)
    {
        this(p_sortCol, null, p_sortAsc);
    }

    /**
     * Construct a JobComparator for sorting purposes based on a particular
     * locale.
     * 
     * @param sortCol
     *            - The column that should be sorted.
     * @param sortAsc
     *            - A boolean that determines whether the sort is ascending or
     *            descending.
     */
    public CotiJobComparator(int p_sortCol, Locale p_locale, boolean p_sortAsc)
    {
        super(p_sortCol, p_locale, p_sortAsc);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
    {
        Object objects[] = new Object[2];
        // our objects are COTIJob
        if (o1 instanceof COTIJob && o2 instanceof COTIJob)
        {
            objects = getValues(objects, (COTIJob) o1, (COTIJob) o2, sortColumn);
        }
        else
        {
            objects[0] = o1;
            objects[1] = o2;
        }

        return objects;
    }

    private Object[] getValues(Object[] p_objects, COTIJob job1, COTIJob job2,
            int p_sortColumn)
    {
        switch (p_sortColumn)
        {
            default:// should always be first column in job list (intentional
                    // fall through)
                compareByJobId(p_objects, job1, job2);
                break;

            /**
             * public static final int COTI_PORJECT_ID = 2; public static final
             * int COTI_PORJECT_NAME = 3; public static final int
             * GLOBALSIGHT_JOB_ID = 4; public static final int STATUS = 5;
             * public static final int SOURCE_LOCALE = 6; public static final
             * int TARGET_LOCALE = 7; public static final int CREATION_DATE = 8;
             */
            case COTI_PORJECT_ID:
                compareByCotiProjectId(p_objects, job1, job2);
                break;
            case COTI_PORJECT_NAME:
                compareByCotiProjectName(p_objects, job1, job2);
                break;

            case GLOBALSIGHT_JOB_ID:
                compareByGlobalSightJobId(p_objects, job1, job2);
                break;

            case STATUS:
            	compareByJobStatus(p_objects, job1, job2);
                break;

            case SOURCE_LOCALE:
                compareBySourceLocale(p_objects, job1, job2);
                break;
            case TARGET_LOCALE:
                compareByTargetLocale(p_objects, job1, job2);
                break;
            case CREATION_DATE:
                compareByCreationDate(p_objects, job1, job2);
                break;
        }

        return p_objects;
    }

    private void compareByCreationDate(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getCreationDate();
        p_objects[1] = p_job2.getCreationDate();
    }

    private void compareByJobId(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = new Long(p_job1.getJobId());
        p_objects[1] = new Long(p_job2.getJobId());
    }

    private void compareByCotiProjectId(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getCotiProjectId();
        p_objects[1] = p_job2.getCotiProjectId();
    }

    private void compareByCotiProjectName(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getCotiProjectName();
        p_objects[1] = p_job2.getCotiProjectName();
    }

    private void compareBySourceLocale(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getSourceLang();
        p_objects[1] = p_job2.getSourceLang();
    }

    private void compareByTargetLocale(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getTargetLang();
        p_objects[1] = p_job2.getTargetLang();
    }

    private void compareByGlobalSightJobId(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getGlobalsightJobId();
        p_objects[1] = p_job2.getGlobalsightJobId();
    }

    private void compareByJobStatus(Object[] p_objects, COTIJob p_job1,
            COTIJob p_job2)
    {
        p_objects[0] = p_job1.getStatus();
        p_objects[1] = p_job2.getStatus();
    }
}
