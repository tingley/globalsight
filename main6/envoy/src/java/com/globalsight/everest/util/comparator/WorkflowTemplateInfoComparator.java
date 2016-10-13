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
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;

/**
* This class can be used to compare WorkflowTemplateInfo objects
*/
public class WorkflowTemplateInfoComparator extends StringComparator
{
	//types of WorkflowTemplateInfo comparison
	public static final int NAME = 0;
	public static final int DESCRIPTION = 1;
	public static final int LOCALEPAIR = 2;
    public static final int SOURCE_LOCALE = 3;
    public static final int TARGET_LOCALE = 4;
    public static final int PROJECT = 5;
	public static final int PROJECTMGR = 6;
	public static final int TARGET_ENCODING = 7;
	
	// used for workflowType comparison
	public static final int WORKFLOW_TYPE = 8;
	public static final int ASC_COMPANY = 9;
        

	/**
	* Creates a WorkflowTemplateInfoComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public WorkflowTemplateInfoComparator(int p_type, Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public WorkflowTemplateInfoComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two WorkflowTemplateInfo objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		WorkflowTemplateInfo a = (WorkflowTemplateInfo) p_A;
		WorkflowTemplateInfo b = (WorkflowTemplateInfo) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		case NAME:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case DESCRIPTION:
			aValue = a.getDescription();
			bValue = b.getDescription();
			rv = this.compareStrings(aValue,bValue);
			break;
        case LOCALEPAIR:
            aValue = getLocalePairAsString(a);
            bValue = getLocalePairAsString(b);
            rv = this.compareStrings(aValue,bValue);
            break;
        case SOURCE_LOCALE:
            aValue = a.getSourceLocale().getDisplayName();
            bValue = b.getSourceLocale().getDisplayName();
            rv = this.compareStrings(aValue,bValue);
            break;
        case TARGET_LOCALE:
            aValue = a.getTargetLocale().getDisplayName();
            bValue = b.getTargetLocale().getDisplayName();
            rv = this.compareStrings(aValue,bValue);
            break;
        case PROJECT:
            aValue = a.getProject().getName();
            bValue = b.getProject().getName();
            rv = this.compareStrings(aValue,bValue);
            break;
		case PROJECTMGR:
			aValue = a.getProjectManagerId();
			bValue = b.getProjectManagerId();
			rv = this.compareStrings(aValue,bValue);
			break;
		case TARGET_ENCODING:
		    aValue = a.getEncoding();
		    bValue = b.getEncoding();
		    rv = this.compareStrings(aValue,bValue);
		    break;
		case WORKFLOW_TYPE:
			aValue = a.getWorkflowType();
			bValue = b.getWorkflowType();
			rv = this.compareStrings(aValue, bValue);
			break;
		case ASC_COMPANY:
			aValue = CompanyWrapper.getCompanyNameById(a.getCompanyId());
			bValue = CompanyWrapper.getCompanyNameById(b.getCompanyId());
			rv = this.compareStrings(aValue,bValue);
			break;
		default:
			aValue = a.getName();
			bValue = b.getName();        
			rv = aValue.compareTo(bValue);
			break;
		}
		return rv;
	}

        /*
         * Get the display string of the locale pair for a workflow template info.
         */
        private String getLocalePairAsString(WorkflowTemplateInfo p_wfTemplateInfo)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(p_wfTemplateInfo.getSourceLocale()
                      .getDisplayName(getLocale()));
            sb.append(" -> ");
            sb.append(p_wfTemplateInfo.getTargetLocale()
                      .getDisplayName(getLocale()));

            return sb.toString();
        }
}
