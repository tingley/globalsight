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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.util.Locale;

import com.globalsight.everest.util.comparator.StringComparator;

/**
 * Report Job Info Comparator
 * 
 * @author Leon
 * 
 */
public class ReportJobInfoComparator extends StringComparator
{
    private static final long serialVersionUID = 8344407185371703682L;

    // types of comparison
    public static final int JOBNAME = 0;
    public static final int JOBID = 1;

    /**
     * Creates a ReportJobInfoComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public ReportJobInfoComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public ReportJobInfoComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        ReportJobInfo a = (ReportJobInfo) p_A;
        ReportJobInfo b = (ReportJobInfo) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            case JOBNAME:
                aValue = a.getJobName();
                bValue = b.getJobName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case JOBID:
                aValue = a.getJobId();
                bValue = b.getJobId();
                rv = this.compareStrings(aValue, bValue);
                break;
            default:
                aValue = a.getJobName();
                bValue = b.getJobName();
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
