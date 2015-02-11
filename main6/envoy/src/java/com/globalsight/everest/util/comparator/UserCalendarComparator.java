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

import com.globalsight.calendar.UserFluxCalendar;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
* This class can be used to compare User Calendar objects
*/
public class UserCalendarComparator extends StringComparator
{
	//types of Calendar comparison
	public static final int NAME = 0;
	public static final int TIMEZONE = 1;
    public static final int ASC_COMPANY = 2;
        
	/**
	* Creates a CalendarComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public UserCalendarComparator(Locale p_locale)
	{
	    super(p_locale);
	}


	/**
	* Performs a comparison of two Calendar objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
        {
		UserFluxCalendar a = (UserFluxCalendar)p_A;
		UserFluxCalendar b = (UserFluxCalendar)p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		case NAME:
			aValue = a.getOwnerUserId();
			bValue = b.getOwnerUserId();
			rv = this.compareStrings(aValue,bValue);
			break;
		case TIMEZONE:
			aValue = a.getTimeZone().getDisplayName();
			bValue = b.getTimeZone().getDisplayName();
			rv = this.compareStrings(aValue,bValue);
			break;
        case ASC_COMPANY:
            aValue = null;
            bValue = null;
            try {
                aValue = CompanyWrapper.getCompanyNameById(
                        ServerProxy.getCalendarManager().findCalendarById(a.getParentCalendarId()).getCompanyId());
                bValue = CompanyWrapper.getCompanyNameById(
                        ServerProxy.getCalendarManager().findCalendarById(b.getParentCalendarId()).getCompanyId());
            }
            catch (Exception e) {
                    e.printStackTrace();
                }
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

