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

import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.everest.company.CompanyWrapper;

/**
 * This class can be used to compare XmlRuleFile objects
 */
public class DefinedAttributeComparator extends StringComparator
{
    private static final long serialVersionUID = -7196747104191119958L;

    // types of comparison
    public static final int NAME = 0;
    public static final int ID = 1;
//    public static final int FILE_NUMBER = 1;
    public static final int DESC = 2;
    public static final int ASC_COMPANY = 3;
    public static final int TYPE = 4;
    public static final int REQUIRED = 5;
    public static final int INTERNAL_NAME = 6;

    /**
     * Creates a XmlRuleFileComparator with the given type and locale. If the
     * type is not a valid type, then the default comparison is done by
     * displayName
     */
    public DefinedAttributeComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public DefinedAttributeComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Tm objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        Attribute a = (Attribute) p_A;
        Attribute b = (Attribute) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
        default:
        case NAME:
            aValue = a.getDisplayName();
            bValue = b.getDisplayName();
            rv = this.compareStrings(aValue, bValue);
            break;
        case ID:
            rv = (int)(a.getId() - b.getId());
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
        case TYPE:
            aValue = a.getType();
            bValue = b.getType();
            rv = this.compareStrings(aValue, bValue);
            break;
        case REQUIRED:
            aValue = Boolean.toString(a.isRequired());
            bValue = Boolean.toString(b.isRequired());
            rv = this.compareStrings(aValue, bValue);
            break;
        case INTERNAL_NAME:
            aValue = a.getName();
            bValue = b.getName();
            rv = this.compareStrings(aValue, bValue);
            break;
        }
        return rv;
    }
}
