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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Needed to display Segment Comment Summary table in the UI.
 */
public class LocaleCommentsComparator
    extends StringComparator
{
    //types of Comment comparison
    public static final int LOCALE = 0;

    public LocaleCommentsComparator(Locale p_locale)
    {
        super(p_locale);
    }

    /**
     * Performs a comparison of two TaskCommentInfo objects by their
         * create date.
     */
    public int compare(java.lang.Object p_A, java.lang.Object p_B)
        {
        LocaleCommentsSummary a = (LocaleCommentsSummary) p_A;
        LocaleCommentsSummary b = (LocaleCommentsSummary) p_B;

        int rv;
        switch (m_type)
        {
            default:
            case LOCALE:
                String aVal = a.getTargetLocale().getDisplayName();
                String bVal = b.getTargetLocale().getDisplayName();
                rv = aVal.compareTo(bVal);
                break;
        }
        return rv;
    }
}
