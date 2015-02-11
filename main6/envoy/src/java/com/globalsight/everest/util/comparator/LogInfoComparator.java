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

import java.util.Date;
import java.util.Locale;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.util.system.LogInfo;

/**
 * This class can be used to compare LogInfo objects
 */
public class LogInfoComparator extends StringComparator
{
    private static final long serialVersionUID = 3973061614121349046L;

    public static final int ID = 0;
    public static final int OBJECTTYPE = 7;
    public static final int EVENTTYPE = 1;
    public static final int OBJECTID = 2;
    public static final int OPERATOR = 3;
    public static final int OPERATETIME = 4;
    public static final int MESSAGE = 5;
    public static final int COMPANY_NAME = 6;

    /**
     * Creates a CurrencyComparator with the given type and locale. If the type
     * is not a valid type, then the default comparison is done by displayName
     */
    public LogInfoComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public LogInfoComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Currency objects.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {
        LogInfo a = (LogInfo) p_A;
        LogInfo b = (LogInfo) p_B;

        String aValue;
        String bValue;
        int rv;

        switch (m_type)
        {
            default:
            case ID:
                rv = (int)(b.getId() - a.getId());
                break;
            case OBJECTTYPE:
                aValue = a.getObjectType();
                bValue = b.getObjectType();
                rv = this.compareStrings(aValue, bValue);
                break;
            case EVENTTYPE:
                aValue = a.getEventType();
                bValue = b.getEventType();
                rv = this.compareStrings(aValue, bValue);
                break;
            case OBJECTID:
                aValue = a.getObjectId();
                bValue = b.getObjectId();
                rv = this.compareStrings(aValue, bValue);
                break;
            case OPERATOR:
                aValue = a.getOperator();
                bValue = b.getOperator();
                rv = this.compareStrings(aValue, bValue);
                break;
            case OPERATETIME:
                Date aDate = a.getOperateTime();
                Date bDate = b.getOperateTime();
                if (aDate.after(bDate))
                    rv = 1;
                else if (aDate.equals(bDate))
                    rv = 0;
                else
                    rv = -1;
                break;
            case MESSAGE:
                aValue = a.getMessage();
                bValue = b.getMessage();
                rv = this.compareStrings(aValue, bValue);
                break;
            case COMPANY_NAME:
                aValue = CompanyWrapper.getCompanyNameById(a.getCompanyId());
                bValue = CompanyWrapper.getCompanyNameById(b.getCompanyId());
                rv = this.compareStrings(aValue, bValue);
                break;
        }
        return rv;
    }
}
