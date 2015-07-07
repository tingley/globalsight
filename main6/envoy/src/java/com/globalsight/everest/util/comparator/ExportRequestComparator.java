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

import java.util.Locale;

import com.globalsight.everest.webapp.pagehandler.administration.systemActivities.jobExportState.RequestFile;


/**
 * This class can be used to compare XmlRuleFile objects
 */
public class ExportRequestComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    public static final int Company = 1;
    public static final int JOB_ID = 2;
    public static final int JOB_NAME = 3;
    public static final int FILE_NAME = 4;
    public static final int FILE_PROFILE = 5;
    public static final int PROJECT = 9;
//    public static final int FILE_SIZE = 8;
    public static final int LOCALE = 6;
    public static final int REQUEST_TIME = 7;

    /**
     * Creates a XmlRuleFileComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public ExportRequestComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public ExportRequestComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        RequestFile a = (RequestFile) p_A;
        RequestFile b = (RequestFile) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case Company:
            aValue = a.getCompany();
            bValue = b.getCompany();
            rv = this.compareStrings(aValue, bValue);
            break;
        case JOB_ID:
            rv = (int)(a.getJobId() - b.getJobId());
            break;
        case JOB_NAME:
            aValue = a.getJobName();
            bValue = b.getJobName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case FILE_NAME:
            aValue = a.getFile();
            bValue = b.getFile();
            rv = this.compareStrings(aValue, bValue);
            break;
        case LOCALE:
            aValue = a.getWorkflowLocale();
            bValue = b.getWorkflowLocale();
            rv = this.compareStrings(aValue, bValue);
            break;
        case FILE_PROFILE:
            aValue = a.getFileProfile();
            bValue = b.getFileProfile();
            rv = this.compareStrings(aValue, bValue);
            break;
        case PROJECT:
            aValue = a.getProject();
            bValue = b.getProject();
            rv = this.compareStrings(aValue, bValue);
            break;
        case REQUEST_TIME:
            rv = a.getRequestTimeAsDate().before(b.getRequestTimeAsDate()) ? -1 : 1;
            break;
        }
        return rv;
    }
}
