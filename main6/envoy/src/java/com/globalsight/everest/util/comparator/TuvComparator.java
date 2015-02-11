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

import com.globalsight.ling.tm.TuvLing;
import java.util.Comparator;
import java.util.Locale;

/**
 * This class compares TUV objects derived from TuvLing by their exact
 * match key.  These classes, TuvImpl and TuvImplVo, do not
 * overwrite equals() like the BaseTmTuv - AbstractTmTuv -
 * SegmentTmTuv classes from the tm2 package do.
 */
public class TuvComparator
    extends StringComparator
{
    public TuvComparator()
    {
        // Locale doesn't matter since we're only interested in the
        // equals() semantics, not collation.
        super(Locale.US);
    }

    /**
     * Performs a comparison of two TUV objects based on their exact
     * match key.
     */
    public int compare(Object p_A, Object p_B)
    {
        TuvLing a = (TuvLing)p_A;
        TuvLing b = (TuvLing)p_B;

        String aValue = a.getExactMatchFormat();
        String bValue = b.getExactMatchFormat();

        return this.compareStrings(aValue, bValue);
    }
}
