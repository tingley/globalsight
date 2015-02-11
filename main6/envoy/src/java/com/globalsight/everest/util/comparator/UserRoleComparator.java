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
import com.globalsight.everest.usermgr.UserRoleInfo;

/**
* This class can be used to compare UserRoleInfo objects
*/
public class UserRoleComparator extends StringComparator
{
	//types of UserRoleInfo comparison
	public static final int SOURCENAME = 0;
	public static final int TARGETNAME = 1;
    public static final int ASC_COMPANY = 2;


	/**
	* Creates a UserRoleComparator with the given locale.
	*/
	public UserRoleComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two User objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		UserRoleInfo a = (UserRoleInfo) p_A;
		UserRoleInfo b = (UserRoleInfo) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		case SOURCENAME:
			aValue = a.getSourceDisplayName();
			bValue = b.getSourceDisplayName();
			rv = this.compareStrings(aValue,bValue);
			break;
		case TARGETNAME:
			aValue = a.getTargetDisplayName();
			bValue = b.getTargetDisplayName();
			rv = this.compareStrings(aValue,bValue);
			break;
        case ASC_COMPANY:
            aValue = CompanyWrapper.getCompanyNameById(a.getCompanyId());
            bValue = CompanyWrapper.getCompanyNameById(b.getCompanyId());
            rv = this.compareStrings(aValue,bValue);
            break;
		default:
			aValue = a.getSourceDisplayName();
			bValue = b.getSourceDisplayName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
