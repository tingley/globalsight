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

package com.globalsight.everest.tuv;

import java.util.List;


/*
 * represents a source and a target segment pair. 
 * Merged segments can be accessed seamlessly
 */
public interface SegmentPair 
{
    /**
     * gets the TU id of the segment pair 
     */
    public long getTuId();

    /**
     * gets a source TUV object. If the segments are merged, the
     * returned Tuv is a clone of the first source TUV of the
     * merged segment that contains an entire merged text.
     */
    public Tuv getSourceTuv();

    /**    
     * gets a target TUV object. If the segments are merged, the
     * returned Tuv is the first target TUV of the merged segment. The
     * merged text has been already set in this Tuv.
     */
    public Tuv getTargetTuv();

    /**    
     *  returns true if the segment is a merged segmnet.
     */
    public boolean isMergedSegment();

    /**    
     *  returns a list of original TU ids if the segment is merged,
     *  otherwise null
     */
    public List getMergedTuIds();

    /**    
     *  returns a list of original source TUV ids if the segment is
     *  merged, otherwise null.
     */
    public List getMergedSourceTuvIds();

    /**    
     *  returns a list of original target TUV ids if the segment is
     *  merged, otherwise null.
     */
    public List getMergedTargetTuvIds();

    /**    
     *  Merges the specified segments into this segment.  This object
     *  must be the first segment of the merged segment.  The
     *  remaining objects must be ordered as they apear in the
     *  document.  If the segments cannot be merged because the
     *  segments are not adjacent or run across the paragraph
     *  boundary, an exception is throw that contains an appropriate
     *  error message. The merged segment pair is marked as modified.
     *
     * @param p_segmentPairsToMerge the pairs to be merged under this
     * segment. The SegmentPairs in this List must be in an ascending
     * order.
     */
    public void mergeSegments(List p_segmentPairsToMerge)
        throws PageSegmentsException;

    /**    
     *  Split a top segment from this segment. It returns SegmentPair
     *  object that is the split top segment.  If this segment is not
     *  merged, PageSegmentsException is thrown. Both split segments
     *  are marked as modified.
     *
     * @return Top half SegmentPair object.
     */
    public SegmentPair splitTopSegment()
        throws PageSegmentsException;

    /**    
     *  Split a bottom segment from this segment. It returns
     *  SegmentPair object that is the split bottom segment.  If this
     *  segment is not merged, PageSegmentsException is thrown. Both
     *  split segments are marked as modified.
     *
     * @return Bottom half SegmentPair object.
     */
    public SegmentPair splitBottomSegment()
        throws PageSegmentsException;

    /**
     * Returns modified state.
     */
    public boolean isModified();
    
    /**
     * Set modified flag to true.
     */
    public void setModified();

    
}
