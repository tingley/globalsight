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

import com.globalsight.connector.blaise.vo.TranslationInboxEntryVo;

public class BlaiseInboxEntryComparator extends StringComparator
{

	private static final long serialVersionUID = -4816540731744095147L;

	public static final int RELATED_OBJECT_ID = 0;
	public static final int SOURCE_LOCALE = 1;
	public static final int TARGET_LOCALE = 2;
	public static final int DESCRIPTION = 3;
	public static final int COMPANY_NAME = 4;
	public static final int SOURCE_TYPE = 5;
	public static final int SOURCE_REVISION = 6;
	public static final int IS_GROUP = 7;
	public static final int IS_CHECKED_OUT = 8;
	public static final int DUE_DATE = 9;
	public static final int WORKFLOW_START_DATE = 10;
	public static final int WORKFLOW_OBJECT_ID = 11;
	public static final int WORKFLOW_ID = 12;
	public static final int ENTRY_ID = 13;

	public static final int JOB_ID = 14;

	public BlaiseInboxEntryComparator(Locale p_locale)
    {
        super(p_locale);
    }

	public int compare(java.lang.Object p_A, java.lang.Object p_B)
	{
		TranslationInboxEntryVo a = (TranslationInboxEntryVo) p_A;
		TranslationInboxEntryVo b = (TranslationInboxEntryVo) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case RELATED_OBJECT_ID:
            rv = (int)(a.getRelatedObjectId() - b.getRelatedObjectId());
            break;
        case SOURCE_LOCALE:
        	aValue = a.getDisplaySourceLocale();
            bValue = b.getDisplaySourceLocale();
            rv = this.compareStrings(aValue, bValue);
            break;
        case TARGET_LOCALE:
        	aValue = a.getDisplayTargetLocale();
            bValue = b.getDisplayTargetLocale();
            rv = this.compareStrings(aValue, bValue);
            break;
        case DESCRIPTION:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue, bValue);
            break;
        case COMPANY_NAME:
            aValue = a.getCompanyName();
            bValue = b.getCompanyName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case SOURCE_TYPE:
            aValue = a.getSourceType();
            bValue = b.getSourceType();
            rv = this.compareStrings(aValue, bValue);
            break;
        case SOURCE_REVISION:
            rv = a.getSourceRevision() - b.getSourceRevision();
            break;
        case IS_GROUP:
			aValue = a.getEntry().isGroup() ? "Yes" : "No";
			bValue = b.getEntry().isGroup() ? "Yes" : "No";
            rv = this.compareStrings(aValue, bValue);
            break;
        case IS_CHECKED_OUT:
			aValue = a.getEntry().isCheckedOut() ? "Yes" : "No";
			bValue = b.getEntry().isCheckedOut() ? "Yes" : "No";
            rv = this.compareStrings(aValue, bValue);
            break;
        case DUE_DATE:
            rv = (int) (a.getDueDate().getTime() - b.getDueDate().getTime());
            break;
        case WORKFLOW_START_DATE:
            rv = (int) (a.getWorkflowStartDate().getTime() - b.getWorkflowStartDate().getTime());
            break;
        case WORKFLOW_OBJECT_ID:
            rv = (int)(a.getWorkflowObjectId() - b.getWorkflowObjectId());
            break;
        case WORKFLOW_ID:
            aValue = a.getWorkflowId();
            bValue = b.getWorkflowId();
            rv = this.compareStrings(aValue, bValue);
            break;
        case ENTRY_ID:
            rv = (int)(a.getId() - b.getId());
            break;
        case JOB_ID:
        	aValue = a.getJobIdsForDisplay();
        	bValue = b.getJobIdsForDisplay();
        	rv = this.compareStrings(aValue, bValue);
        	break;
        }
        return rv;
	}
}
