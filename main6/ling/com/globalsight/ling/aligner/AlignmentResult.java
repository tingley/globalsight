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
package com.globalsight.ling.aligner;

import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm2.BaseTmTu;

import java.util.List;
import java.util.ArrayList;

/**
 * AlignmentResult records a result of the alignment of a pair of
 * source and target TMXs.
 */

public class AlignmentResult
{
    // List of AlignedSegments objects
    private List m_alignedSegments = new ArrayList();

    // List of BaseTmTuv objects
    private List m_isolatedSource = new ArrayList();
    private List m_isolatedTarget = new ArrayList();

    // source and target TMX file names
    private String m_sourceTmx;
    private String m_targetTmx;
    

    public void setSourceTmxFileName(String p_tmx)
    {
        m_sourceTmx = p_tmx;
    }


    public void setTargetTmxFileName(String p_tmx)
    {
        m_targetTmx = p_tmx;
    }
    

    /**
     * Add aligned source and target segments. Source and target
     * segments can be more than one because we allow many to many
     * alignment.
     *
     * @param p_sourceTuvs List of source BaseTmTuv objects
     * @param p_targetTuvs List of target BaseTmTuv objects
     */
    public void addAlignedSegments(List p_sourceTuvs, List p_targetTuvs)
    {
        AlignedSegments alignedSegments
            = new AlignedSegments(p_sourceTuvs, p_targetTuvs);

        m_alignedSegments.add(alignedSegments);
    }
    

    /**
     * Add isolated source segment. Isolated source segment is a
     * segment that doesn't have a corresponging target segment.
     *
     * @param p_segment Isolated source segment
     */
    public void addSourceIsolatedSegment(BaseTmTuv p_segment)
    {
        m_isolatedSource.add(p_segment);
    }
    

    /**
     * Add isolated target segment. Isolated target segment is a
     * segment that doesn't have a corresponging source segment.
     *
     * @param p_segment Isolated target segment
     */
    public void addTargetIsolatedSegment(BaseTmTuv p_segment)
    {
        m_isolatedTarget.add(p_segment);
    }
    

    /**
     * Returns source TMX file name.
     *
     * @return Source TMX file name
     */
    public String getSourceTmx()
    {
        return m_sourceTmx;
    }
    

    /**
     * Returns target TMX file name.
     *
     * @return Target TMX file name
     */
    public String getTargetTmx()
    {
        return m_targetTmx;
    }
    

    /**
     * Returns a List of AlignedSegments
     *
     * @return List of AlignedSegments objects
     */
    public List getAlignedSegments()
    {
        return m_alignedSegments;
    }
    

    /**
     * Returns a List of isolated source segmnets (BaseTmTuv)
     *
     * @return List of BaseTmTuv
     */
    public List getSourceIsolatedSegments()
    {
        return m_isolatedSource;
    }
    

    /**
     * Returns a List of isolated target segmnets (BaseTmTuv)
     *
     * @return List of BaseTmTuv
     */
    public List getTargetIsolatedSegments()
    {
        return m_isolatedTarget;
    }


    
}
