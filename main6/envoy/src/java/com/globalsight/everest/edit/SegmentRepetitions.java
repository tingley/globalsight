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
package com.globalsight.everest.edit;

import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.TuvComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Keeps track of segment repetitions.
 *
 * This class works like the SegmentRepetition class in the statistics
 * package but uses different TUV objects. The statistics package uses
 * TUVs created during import (BaseTmTuv - AbstractTmTuv -
 * SegmentTMTuv), whereas the editors use the Tuv interface and
 * TuvLing->TuvImpl and TuvLing->TuvImplVo hierarchies.
 *
 * @see com.globalsight.everest.statistics.SegmentRepetition.
 */
public class SegmentRepetitions
{
    // Key : Tuv
    // Value : an ArrayList of identical TUVs as its key.
    // TUVs are compared according to their exact match key.
    private TreeMap m_map = new TreeMap(new TuvComparator());

    /**
     * Constructor. Takes an ArrayList of Tuv objects.
     */
    public SegmentRepetitions(ArrayList p_segments)
    {
        for (int i = 0, max = p_segments.size(); i < max; i++)
        {
            Tuv tuv = (Tuv)p_segments.get(i);

            ArrayList identicalTuvs = (ArrayList)m_map.get(tuv);

            if (identicalTuvs == null)
            {
                identicalTuvs = new ArrayList();
                m_map.put(tuv, identicalTuvs);
            }

            identicalTuvs.add(tuv);
        }

        // Always compress the map to conserve space.
        compressMap();
    }

    /**
     * Returns a number of repetition of a given Tuv. If a Tuv doesn't
     * have other identical segments in the set of segments, this
     * method returns 1. If a given Tuv cannot be found in the set of
     * segments, it returns 0. If the map has been compressed, 0 is
     * returned for both TUVs not in the map, and TUVs with no
     * repetitions - so only repeted segments come back with a
     * meaningful value &gt;= 2.
     */
    public int getNumRepetitions(Tuv p_tuv)
    {
        int num = 0;

        ArrayList identicalTuvs = (ArrayList)m_map.get(p_tuv);

        if (identicalTuvs != null)
        {
            num = identicalTuvs.size();
        }

        return num;
    }

    /**
     * Get a list of identical TUVs for the given TUV.
     *
     * @param p_tuv The key to the unique segments map.
     *
     * @return A list of identical TUVs for the given TUV, or null if
     * the TUV isn't repeated.
     */
    public ArrayList getIdenticalTuvs(Tuv p_tuv)
    {
        return (ArrayList)m_map.get(p_tuv);
    }

    /**
     * Compresses the map of identical TUVs by removing data for TUVs
     * that occur only once.
     */
    public void compressMap()
    {
        ArrayList keys = new ArrayList(m_map.keySet());

        for (int i = 0, max = keys.size(); i < max; i++)
        {
            Tuv key = (Tuv)keys.get(i);

            ArrayList reps = (ArrayList)m_map.get(key);

            if (reps == null || reps.size() <= 1)
            {
                m_map.remove(key);
            }
        }
    }
}
