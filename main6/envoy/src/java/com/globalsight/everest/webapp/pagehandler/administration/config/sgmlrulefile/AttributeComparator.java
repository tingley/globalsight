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
package com.globalsight.everest.webapp.pagehandler.administration.config.sgmlrulefile;

import java.util.Comparator;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.ling.sgml.sgmlrules.SgmlRule;
import java.util.Locale;

/**
* This class can be used to compare SgmlRule.Attribute objects
*/
public class AttributeComparator extends StringComparator
{
    private static final long serialVersionUID = -8099512491320564951L;

    //types of comparison
	public static final int NAME = 0;

	public AttributeComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two VendorRole objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		SgmlRule.Attribute a = (SgmlRule.Attribute) p_A;
		SgmlRule.Attribute b = (SgmlRule.Attribute) p_B;

		String aValue;
		String bValue;
		int rv;

		switch (m_type)
		{
		default:
		case NAME:
			aValue = a.m_name;
			bValue = b.m_name;
			rv = this.compareStrings(aValue,bValue);
			break;
		}
		return rv;
	}
}
