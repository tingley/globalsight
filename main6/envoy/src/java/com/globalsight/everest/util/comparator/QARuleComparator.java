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

import com.globalsight.cxe.entity.filterconfiguration.QARule;

public class QARuleComparator extends StringComparator
{
    private static final long serialVersionUID = -8149263709337552072L;

    public static final int PRIORITY = 0;
    public static final int CHECK = 1;
    public static final int DESCRIPTION = 2;

    public QARuleComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    public QARuleComparator(Locale p_locale)
    {
        super(p_locale);
    }

    public int compare(Object o1, Object o2)
    {
        QARule a = (QARule) o1;
        QARule b = (QARule) o2;

        int rv;

        switch (m_type)
        {
            default:
            case PRIORITY:
                rv = a.getPriority() - b.getPriority();
                break;
            case CHECK:
                rv = compareStrings(a.getCheck(), b.getCheck());
                break;
        }
        return rv;
    }
}
