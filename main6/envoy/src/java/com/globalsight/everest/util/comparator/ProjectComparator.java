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
import com.globalsight.everest.projecthandler.ProjectInfo;

/**
* This class can be used to compare ProjectInfo objects
*/
public class ProjectComparator extends StringComparator
{
	//types of Project comparison
	public static final int PROJECTNAME = 0;
	public static final int PROJECTMANAGER = 1;
    public static final int TERMBASE = 2;
    public static final int ASC_COMPANY = 3;
    public static final int DESCRIPTION=4;


	/**
	* Creates a ProjectComparator with the given locale.
	*/
	public ProjectComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two User objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		ProjectInfo a = (ProjectInfo) p_A;
		ProjectInfo b = (ProjectInfo) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		case PROJECTNAME:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case PROJECTMANAGER:
			aValue = a.getProjectManagerName();
			bValue = b.getProjectManagerName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case TERMBASE:
			aValue = a.getTermbaseName();
			bValue = b.getTermbaseName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case ASC_COMPANY:
			aValue = CompanyWrapper.getCompanyNameById(a.getCompanyId());
			bValue = CompanyWrapper.getCompanyNameById(b.getCompanyId());
			rv = this.compareStrings(aValue,bValue);
			break;
		case DESCRIPTION:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue,bValue);
            break;
		default:
			aValue = a.getName();
			bValue = b.getName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
