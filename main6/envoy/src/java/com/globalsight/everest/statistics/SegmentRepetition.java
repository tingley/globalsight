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

package com.globalsight.everest.statistics;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm2.SegmentTmTuv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of segment repetition.
 */
public class SegmentRepetition
{
    static private Logger c_logger =
        Logger.getLogger(
            StatisticsService.class);


    // Key : SegmentTmTuv
    // Value : a List of identical segments as its key.
    private Map m_uniqueSegments = new HashMap();


    /**
     * Constructor. Takes a Collection of SegmentTmTuv.
     */
    public SegmentRepetition(Collection p_segments)
    {
        Iterator it = p_segments.iterator();
        while (it.hasNext())
        {
            SegmentTmTuv tuv = (SegmentTmTuv)it.next();

            ArrayList identicalSegments = (ArrayList)m_uniqueSegments.get(tuv);
            if (identicalSegments == null)
            {
                identicalSegments = new ArrayList();
                m_uniqueSegments.put(tuv, identicalSegments);
            }
            identicalSegments.add(tuv);
        }
    }

    /**
     * Constructor. Takes a Collection of SegmentTmTuv and compresses
     * the internal hash map.
     */
    public SegmentRepetition(Collection p_segments, boolean p_compress)
    {
        this(p_segments);

        if (p_compress)
        {
            compressMap();
        }
    }

    /**
     * Iterates unique segments. next() method of this Iterator returns
     * an object whose type is SegmentTmTuv.
     */
    public Iterator iterator()
    {
        return m_uniqueSegments.keySet().iterator();
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
    public int getNumRepetition(SegmentTmTuv p_tuv)
    {
        int num = 0;

        ArrayList identicalSegments = (ArrayList)m_uniqueSegments.get(p_tuv);
        if (identicalSegments != null)
        {
            num = identicalSegments.size();
        }

        return num;
    }

    /**
     * Get a list of identical segments for the given Tuv.  This
     * is mainly used for workflow's word count calculation so all
     * Tuvs can be taken into consideration (from multiple pages).
     * See StatisticsService.calculateWorkflowWordCounts for more info.
     *
     * @param p_tuv The key to the unique segments map.
     *
     * @return A list of identical segments for the given Tuv.
     */
    public List getIdenticalSegments(SegmentTmTuv p_tuv)
    {
        return (List)m_uniqueSegments.get(p_tuv);
    }

    public void compressMap()
    {
        ArrayList keys = new ArrayList(m_uniqueSegments.keySet());

        for (int i = 0, max = keys.size(); i < max; i++)
        {
            SegmentTmTuv key = (SegmentTmTuv)keys.get(i);

            ArrayList reps = (ArrayList)m_uniqueSegments.get(key);

            if (reps == null || reps.size() <= 1)
            {
                m_uniqueSegments.remove(key);
            }
        }
    }
}
