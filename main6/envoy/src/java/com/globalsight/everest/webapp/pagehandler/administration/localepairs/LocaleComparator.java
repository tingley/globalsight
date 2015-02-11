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
package com.globalsight.everest.webapp.pagehandler.administration.localepairs;    

import java.util.Comparator;
import java.util.Locale;
import com.globalsight.everest.foundation.LocalePair;
import com.globalsight.everest.util.comparator.StringComparator;

/**
* This class can be used to compare LocalePair objects
*/
public class LocaleComparator extends StringComparator
{
    //types of comparison
    public static final int SRC = 0;
    public static final int TARG = 1;

	/**
	* Creates a LocaleComparator with the given locale.
	*/
	public LocaleComparator(Locale p_locale)
	{
	    super(p_locale);
	}

	/**
	* Performs a comparison of two LocalePair objects.
	*/
	public int compare(java.lang.Object p_A, java.lang.Object p_B) {
		LocalePair a = (LocalePair) p_A;
		LocalePair b = (LocalePair) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        case SRC:
            aValue = a.getSource().getDisplayName(getLocale());
            bValue = b.getSource().getDisplayName(getLocale());
            rv = this.compareStrings(aValue,bValue);
            break;
        case TARG:
            aValue = a.getTarget().getDisplayName(getLocale());
            bValue = b.getTarget().getDisplayName(getLocale());
            rv = this.compareStrings(aValue,bValue);
            break;
        default:
            aValue = a.getSource().getDisplayName(getLocale());
            bValue = b.getSource().getDisplayName(getLocale());
            rv = this.compareStrings(aValue,bValue);
            break;
        }
		return rv;
	}
}
