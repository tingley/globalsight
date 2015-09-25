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

import com.globalsight.cxe.entity.customAttribute.TMPAttribute;

public class TMPAttributeComparator  extends StringComparator
{
	private static final long serialVersionUID = 560081702967689854L;

	public static final int NAME = 0;
	public static final int ORDER = 1;

	public TMPAttributeComparator(Locale p_locale)
	{
		super(p_locale);
	}

	public TMPAttributeComparator(int p_type, Locale p_locale)
	{
		super(p_type, p_locale);
	}

	public int compare(java.lang.Object p_A, java.lang.Object p_B)
	{
		TMPAttribute a = (TMPAttribute) p_A;
		TMPAttribute b = (TMPAttribute) p_B;

		int rv;
		switch (m_type)
		{
		case ORDER:
			rv = a.getOrder() - b.getOrder();
			break;
		case NAME:
		default:
			rv = this.compareStrings(a.getAttributeName(), b.getAttributeName());
			break;
		}

		return rv;
	}
}
