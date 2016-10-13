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

/**
 * This class can be used to compare RemoteIp instance.
 */
public class RemoteIpComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    // types of comparison
    public static final int IP = 0;
    public static final int DESC = 1;

    /**
     * Creates a RemoteIpComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public RemoteIpComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public RemoteIpComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two RemoteIp objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        RemoteIp a = (RemoteIp) p_A;
        RemoteIp b = (RemoteIp) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case IP:
            aValue = a.getIp();
            bValue = b.getIp();
            String[] as = aValue.split("\\.");
            String[] bs = bValue.split("\\.");
            
            for (int i = 0; i < as.length; i++)
            {
                if (bs.length <= i)
                    return 1;
                
                int result = compareUnit(as[i], bs[i]);
                if (result != 0)
                    return result;
            }
            
            rv = -1;
            break;
        case DESC:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }

    private int compareUnit(String s1, String s2)
    {
        if (s1.equals(s2))
            return 0;
        
        if (s1.length() != s2.length())
            return s1.length() - s2.length();
        
        return compareStrings(s1, s2);
    }
}
