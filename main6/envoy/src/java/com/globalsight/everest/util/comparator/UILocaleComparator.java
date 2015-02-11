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

import com.globalsight.everest.webapp.pagehandler.administration.config.remoteip.RemoteIp;
import com.globalsight.everest.webapp.pagehandler.administration.config.uilocale.UILocale;
import com.sun.org.apache.xml.internal.utils.StringComparable;

/**
 * This class can be used to compare RemoteIp instance.
 */
public class UILocaleComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104321119958L;

    // types of comparison
    public static final int NAME = 0;
    public static final int ISDEFAULT = 1;

    /**
     * Creates a UILocaleComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public UILocaleComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public UILocaleComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two RemoteIp objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
    	UILocale a = (UILocale) p_A;
    	UILocale b = (UILocale) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case NAME:
            aValue = a.getLongName();
            bValue = b.getLongName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case ISDEFAULT:
            aValue = "" + a.isDefaultLocale();
            bValue = "" + b.isDefaultLocale();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
