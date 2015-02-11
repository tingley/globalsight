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

import java.util.Comparator;
import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.cxe.entity.cms.teamsite.server.TeamSiteServer;

/**
* This class can be used to compare TeamSiteServer objects
*/
public class TeamSiteServerComparator extends StringComparator
{
	//types of TeamSiteServer comparison
	public static final int NAME = 0;
        public static final int OS = 1;
	public static final int MOUNT = 2;
	public static final int HOME = 3;
    public static final int COMPANY = 4;

	/**
	* Creates a TeamSiteServerComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public TeamSiteServerComparator(int p_type, Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public TeamSiteServerComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two TeamSiteServer objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		TeamSiteServer a = (TeamSiteServer) p_A;
		TeamSiteServer b = (TeamSiteServer) p_B;

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
                case OS:
                        aValue = a.getOS();
                        bValue = b.getOS();
                        rv = this.compareStrings(aValue,bValue);
                        break;
		case MOUNT:
			aValue = a.getMount();
			bValue = b.getMount();
			rv = this.compareStrings(aValue,bValue);
			break;
		case HOME:
			aValue = a.getHome();
			bValue = b.getHome();
			rv = this.compareStrings(aValue,bValue);
			break;
        case COMPANY:
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
}
