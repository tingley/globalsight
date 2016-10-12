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

import com.globalsight.cxe.entity.xmldtd.XmlDtdImpl;
import com.globalsight.everest.company.CompanyWrapper;

/**
 * This class can be used to compare XmlRuleFile objects
 */
public class XmlDtdComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    // types of comparison
    public static final int NAME = 0;
    public static final int FILE_NUMBER = 1;
    public static final int DESC = 2;
    public static final int ASC_COMPANY = 3;

    /**
     * Creates a XmlRuleFileComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public XmlDtdComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public XmlDtdComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        XmlDtdImpl a = (XmlDtdImpl) p_A;
        XmlDtdImpl b = (XmlDtdImpl) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case FILE_NUMBER:
            rv = a.getFileNumber() - b.getFileNumber();
            break;
        case DESC:
            aValue = a.getDescription();
            bValue = b.getDescription();
            rv = this.compareStrings(aValue, bValue);
            break;
        case ASC_COMPANY:
            aValue = CompanyWrapper.getCompanyNameById(a.getCompanyId());
            bValue = CompanyWrapper.getCompanyNameById(b.getCompanyId());
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
