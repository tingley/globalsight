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

import java.io.Serializable;
import java.util.Locale;

import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.webapp.pagehandler.administration.systemActivities.jobCreationState.RequestFile;
import com.globalsight.util.EnvoyDataComparator;

/**
 * This class can be used to compare create request objects. It is used for
 * sorting this information in a table.
 */
public class CreateRequestComparator extends EnvoyDataComparator implements
        Serializable
{
    private static final long serialVersionUID = 6656992627832899380L;
    public static final int Company = 1;
    public static final int JOB_ID = 2;
    public static final int JOB_NAME = 3;
    public static final int FILE_Name = 4;
    public static final int FILE_PROFILE = 5;
    public static final int PRIORITY = 6;
    public static final int REQUEST_TIME = 7;

    /**
     * Construct a CreateRequestComparator for sorting purposes based on the
     * default locale.
     * 
     * @param sortCol
     *            - The column that should be sorted.
     * @param sortAsc
     *            - A boolean that determines whether the sort is ascending or
     *            descending.
     */
    public CreateRequestComparator(int sortCol, boolean sortAsc,
            Currency currency)
    {
        this(sortCol, null, sortAsc, currency);
    }

    /**
     * Construct a RequestFileComparator for sorting purposes based on a particular
     * locale.
     * 
     * @param sortCol
     *            - The column that should be sorted.
     * @param sortAsc
     *            - A boolean that determines whether the sort is ascending or
     *            descending.
     */
    public CreateRequestComparator(int sortCol, Locale locale, boolean sortAsc,
            Currency currency)
    {
        super(sortCol, locale, sortAsc);
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    public Object[] getComparableObjects(Object o1, Object o2, int sortColumn)
    {
        Object objects[] = new Object[2];
        // our objects are RequestFile
        if (o1 instanceof RequestFile && o2 instanceof RequestFile)
        {
            objects = getValues(objects, (RequestFile) o1, (RequestFile) o2, sortColumn);
        }
        else
        {
            objects[0] = o1;
            objects[1] = o2;
        }

        return objects;
    }

    private Object[] getValues(Object[] objects, RequestFile request1, RequestFile requestFile2,
            int sortColumn)
    {
        switch (sortColumn)
        {
        default:// should always be first column in requestFile list (intentional fall
                // through)
            compareByRequestTime(objects, request1, requestFile2);
            break;
        case Company:
            compareByCompany(objects, request1, requestFile2);
            break;
        case JOB_ID:
            compareByJobId(objects, request1, requestFile2);
            break;
        case JOB_NAME:
            compareByJobName(objects, request1, requestFile2);
            break;
        case FILE_Name:
            compareByFile(objects, request1, requestFile2);
            break;
        case FILE_PROFILE:
            compareByFileProfile(objects, request1, requestFile2);
            break;
        case REQUEST_TIME:
            compareByRequestTime(objects, request1, requestFile2);
            break;
        case PRIORITY:
            compareByPriority(objects, request1, requestFile2);
            break;
        }

        return objects;
    }

    private void compareByCompany(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getCompany();
        objects[1] = request2.getCompany();
    }
    
    private void compareByRequestTime(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getRequestTime();
        objects[1] = request2.getRequestTime();
    }
    
    private void compareByJobId(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getJobId();
        objects[1] = request2.getJobId();
    }
    
    private void compareByJobName(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getJobName();
        objects[1] = request2.getJobName();
    }

    private void compareByPriority(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getPriority();
        objects[1] = request2.getPriority();
    }
    
    private void compareByFile(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getFile();
        objects[1] = request2.getFile();
    }
    
    private void compareByFileProfile(Object[] objects, RequestFile request1, RequestFile request2)
    {
        objects[0] = request1.getFileProfile();
        objects[1] = request2.getFileProfile();
    }
}
