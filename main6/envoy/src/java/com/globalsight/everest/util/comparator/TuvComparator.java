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

import com.globalsight.ling.tm.TuvLing;

/**
 * This class compares TUV objects derived from TuvLing by their exact match
 * key. These classes, TuvImpl and TuvImplVo, do not overwrite equals() like the
 * BaseTmTuv - AbstractTmTuv - SegmentTmTuv classes from the tm2 package do.
 */
public class TuvComparator extends CachingStringComparator
{
    private static final long serialVersionUID = -3512515336271113942L;

    public static final int EXACT_MATCH_FORMAT = 0;
    public static final int LAST_MODIFIED = 1;

    public TuvComparator()
    {
        // Locale doesn't matter since we're only interested in the
        // equals() semantics, not collation.
        super(Locale.US);
    }

    public TuvComparator(int p_type, Locale p_locale)
    {
        super(p_type, p_locale);
    }

    /**
     * Performs a comparison of two TUV objects based on selected type.
     */
    public int compare(Object p_A, Object p_B)
    {
        TuvLing a = (TuvLing) p_A;
        TuvLing b = (TuvLing) p_B;

        int rv;
        switch (m_type)
        {
            case LAST_MODIFIED:
                Date aDate = a.getLastModified();
                Date bDate = b.getLastModified();
                if (aDate != null && bDate == null)
                {
                    rv = 1;
                }
                else if (aDate == null && bDate != null)
                {
                    rv = -1;
                }
                else
                {
                    if (aDate.after(bDate))
                        rv = 1;
                    else if (aDate.equals(bDate))
                        rv = 0;
                    else
                        rv = -1;
                }
                break;

            default:
                String aValue = a.getExactMatchFormat();
                String bValue = b.getExactMatchFormat();

                return rv = this.compareStrings(aValue, bValue);
        }

        return rv;
    }
}
