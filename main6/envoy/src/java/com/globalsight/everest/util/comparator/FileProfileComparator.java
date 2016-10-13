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

import java.util.HashMap;
import java.util.Locale;

import com.globalsight.cxe.entity.fileprofile.FileprofileVo;

/**
* This class can be used to compare FileProfile objects
*/
public class FileProfileComparator extends StringComparator
{
	//types of comparison
	public static final int NAME = 0;
	public static final int DESC = 1;
	public static final int LP = 2;
	public static final int ASC_COMPANY = 3;
	public static final int FILTER_NAME = 4;
	public static final int FORMATTYPES_NAME = 5;
	public static final int EXTENSIONS_NAME = 6;
	public static final int CODE_NAME = 7;
	public static final String L10NPROFILES="p_l10nProfiles";
    public static final String FORMATTYPES = "idViewFormatTypes";
    public static final String EXTENSIONS = "idViewExtensions";
    HashMap<Long,String> m_idViewExtensions = new HashMap<Long,String>();
    
	/**
	* Creates a FileProfileComparator with the given type and locale.
	* If the type is not a valid type, then the default comparison
	* is done by displayName
	*/
	public FileProfileComparator(int p_type,Locale p_locale)
	{
	    super(p_type, p_locale);
	}

	public FileProfileComparator(Locale p_locale,HashMap<Long,String> idViewExtensions)
	{
	    super(p_locale);
	    m_idViewExtensions=idViewExtensions;
	}

	/**
	* Performs a comparison of two Tm objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
	    FileprofileVo a = (FileprofileVo) p_A;
	    FileprofileVo b = (FileprofileVo) p_B;

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
		case DESC:
			aValue = a.getDescription();
			bValue = b.getDescription();
			rv = this.compareStrings(aValue,bValue);
			break;
		case ASC_COMPANY:
			aValue = a.getCompanyName();
			bValue = b.getCompanyName();
			rv = this.compareStrings(aValue,bValue);
			break;
        case FILTER_NAME:
            aValue = a.getFilterName();
            bValue = b.getFilterName();
            rv = this.compareStrings(aValue,bValue);
            break;
        case FORMATTYPES_NAME:
            aValue = a.getFormatName();
            bValue = b.getFormatName();
            rv = this.compareStrings(aValue,bValue);
            break;
        case EXTENSIONS_NAME:
            aValue = m_idViewExtensions.get(a.getId());
            bValue =  m_idViewExtensions.get(b.getId());
//            aValue =aValue==null?"all":aValue;
//            bValue =bValue==null?"all":bValue;
            rv = this.compareStrings(aValue,bValue);
            break;
        case CODE_NAME:
            aValue = a.getCodeSet();
            bValue = b.getCodeSet();
            rv = this.compareStrings(aValue,bValue);
            break;
		default:
			aValue = a.getLocName();
			bValue = b.getLocName();
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
