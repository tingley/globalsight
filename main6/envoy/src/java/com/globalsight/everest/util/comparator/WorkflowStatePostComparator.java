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

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.workflowmanager.WorkflowStatePosts;

public class WorkflowStatePostComparator extends StringComparator
{
	private static final long serialVersionUID = 1483159845942252846L;

	// types of WorkflowStatePost comparison
    public static final int NAME = 0;
    public static final int DESCRIPTION = 1;
    public static final int LISTENER_URL = 2;
    public static final int SECRET_KEY = 3;
    public static final int ASC_COMPANY = 4;

    public WorkflowStatePostComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two WorkflowTemplateInfo objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        WorkflowStatePosts a = (WorkflowStatePosts) p_A;
        WorkflowStatePosts b = (WorkflowStatePosts) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            case NAME:
                aValue = a.getName();
                bValue = b.getName();
                rv = this.compareStrings(aValue, bValue);
                break;
            case DESCRIPTION:
                aValue = a.getDescription();
                bValue = b.getDescription();
                rv = this.compareStrings(aValue, bValue);
                break;
            case LISTENER_URL:
                aValue = a.getListenerURL();
                bValue = b.getListenerURL();
                rv = this.compareStrings(aValue, bValue);
                break;
            case SECRET_KEY:
                aValue = a.getSecretKey();
                bValue = b.getSecretKey();
                rv = this.compareStrings(aValue, bValue);
                break;
            case ASC_COMPANY:
                aValue = CompanyWrapper.getCompanyNameById(a.getCompanyId());
                bValue = CompanyWrapper.getCompanyNameById(b.getCompanyId());
                rv = this.compareStrings(aValue, bValue);
                break;
            default:
                aValue = a.getName();
                bValue = b.getName();
                rv = aValue.compareTo(bValue);
                break;
        }
        return rv;
    }
}
