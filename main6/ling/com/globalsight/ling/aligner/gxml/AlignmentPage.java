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
package com.globalsight.ling.aligner.gxml;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * AlignmentPage represents a file to be aligned with the other corresponding
 * file. The class stores Skeleton and BaseTmTuv object of the file.
 */

public class AlignmentPage
{
    GlobalSightLocale m_locale;

    // Key: Skeleton ID (Integer)
    // Value: Skeleton object
    private Map m_skeleton;

    // Key: Skeleton ID (Integer)
    // Value: List of BaseTmTuv objects. Those segments appear after
    // the skeleton with the id in the key.
    private Map m_segments;

    private static SkeletonComparator c_skeletonComparator = new SkeletonComparator();

    private static SegmentComparator c_segmentComparator = new SegmentComparator();

    public AlignmentPage(GlobalSightLocale p_locale)
    {
        m_locale = p_locale;

        m_skeleton = new HashMap();
        m_segments = new HashMap();
    }

    public void addSkeleton(Skeleton p_skeleton)
    {
        m_skeleton.put(p_skeleton.getId(), p_skeleton);
    }

    public void addSegment(Integer p_skeletonId, BaseTmTuv p_segment)
    {
        List segments = (List) m_segments.get(p_skeletonId);
        if (segments == null)
        {
            segments = new ArrayList();
            m_segments.put(p_skeletonId, segments);
        }

        segments.add(p_segment);
    }

    public List getAllSkeletons()
    {
        List skeletons = new ArrayList(m_skeleton.values());
        SortUtil.sort(skeletons, c_skeletonComparator);
        return skeletons;
    }

    public List getSegments(Integer p_skeletonId)
    {
        List segments = (List) m_segments.get(p_skeletonId);
        if (segments == null)
        {
            segments = new ArrayList();
        }

        return segments;
    }

    // return List of BaseTmTuv objects
    public List getAllSegments()
    {
        List segments = new ArrayList();
        Iterator it = m_segments.values().iterator();
        while (it.hasNext())
        {
            List subSegments = (List) it.next();
            segments.addAll(subSegments);
        }

        SortUtil.sort(segments, c_segmentComparator);
        return segments;
    }

    public GlobalSightLocale getLocale()
    {
        return m_locale;
    }

    private static class SkeletonComparator implements Comparator
    {
        public int compare(Object p_o1, Object p_o2)
        {
            Skeleton skeleton1 = (Skeleton) p_o1;
            Skeleton skeleton2 = (Skeleton) p_o2;

            return skeleton1.getId().compareTo(skeleton2.getId());
        }
    }

    private static class SegmentComparator implements Comparator
    {
        public int compare(Object p_o1, Object p_o2)
        {
            BaseTmTuv segment1 = (BaseTmTuv) p_o1;
            BaseTmTuv segment2 = (BaseTmTuv) p_o2;

            return (int) (segment1.getId() - segment2.getId());
        }
    }
}
