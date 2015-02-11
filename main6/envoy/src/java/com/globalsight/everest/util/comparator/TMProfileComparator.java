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
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
* This class can be used to compare TMProfile objects
*/
public class TMProfileComparator extends StringComparator
{
	//types of TMProfile comparison
	public static final int NAME = 0;
	public static final int DESCRIPTION = 1;
	public static final int ASC_COMPANY = 2;
        
	/**
	* Creates a TMProfileComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public TMProfileComparator(int p_type, Locale p_locale)
	{
            super(p_type, p_locale);
	}

	public TMProfileComparator(Locale p_locale)
	{
            super(p_locale);
	}

	/**
	* Performs a comparison of two TMProfileInfo objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) 
        {
		TranslationMemoryProfile a = (TranslationMemoryProfile)p_A;
		TranslationMemoryProfile b = (TranslationMemoryProfile)p_B;

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
		case ASC_COMPANY:
			aValue = null;
			bValue = null;
			try {
				aValue = CompanyWrapper.getCompanyNameById(
						ServerProxy.getProjectHandler().getProjectTMById(a.getProjectTmIdForSave(), false).getCompanyId());
				bValue = CompanyWrapper.getCompanyNameById(
						ServerProxy.getProjectHandler().getProjectTMById(b.getProjectTmIdForSave(), false).getCompanyId());
			} catch (Exception e) {
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

