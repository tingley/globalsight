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

package com.globalsight.everest.webapp.pagehandler.administration.comment;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.util.comparator.StringComparator;
import com.globalsight.everest.comment.Issue;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Needed to display Segment Comment Summary table in the UI.
 */
public class IssueComparator
    extends StringComparator
{
    static public final List s_priorities = Arrays.asList(
        new String[] {
        Issue.PRI_LOW,
        Issue.PRI_MEDIUM,
        Issue.PRI_HIGH,
        Issue.PRI_URGENT,
    });

    //types of Issue comparison
    public static final int STATE = 0;
    public static final int PRIORITY = 1;
    public static final int TITLE = 2;

    public IssueComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two Issue objects by their
         * create date.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
    {

        Issue a = (Issue) p_A;
        Issue b = (Issue) p_B;

        int rv;
        switch (m_type)
        {
            case STATE:
                String aVal = a.getStatus();
                String bVal = b.getStatus();
                rv = bVal.compareTo(aVal);
                break;
            default:
            case PRIORITY:
                aVal = a.getPriority();
                bVal = b.getPriority();
                rv = s_priorities.indexOf(bVal) - s_priorities.indexOf(aVal);
                break;
            case TITLE:
                aVal = a.getTitle();
                bVal = b.getTitle();
                rv = aVal.compareTo(bVal);
                break;
        }
        return rv;
    }
}
