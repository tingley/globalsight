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

import com.globalsight.everest.webapp.pagehandler.administration.systemActivities.offlineUploadState.Vo;


/**
 * This class can be used to compare XmlRuleFile objects
 */
public class OfflineUploadRequestComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    public static final int COMPANY = 1;
    public static final int FILE_NAME = 2;
    public static final int FILE_SIZE = 3;
    public static final int USER = 4;


    /**
     * Creates a XmlRuleFileComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public OfflineUploadRequestComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public OfflineUploadRequestComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        Vo a = (Vo) p_A;
        Vo b = (Vo) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case COMPANY:
            aValue = a.getCompany();
            bValue = b.getCompany();
            rv = this.compareStrings(aValue, bValue);
            break;
        case FILE_NAME:
            aValue = a.getFileName();
            bValue = b.getFileName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case FILE_SIZE:
            aValue = a.getFileSize();
            bValue = b.getFileSize();
            rv = (int) (Long.parseLong(aValue) - Long.parseLong(bValue));
            break;
        case USER:
            aValue = a.getUser();
            bValue = b.getUser();
            rv = this.compareStrings(aValue, bValue);
            break;        
        }
        return rv;
    }
}
