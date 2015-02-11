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

package com.globalsight.terminology.termleverager.recognizer;

import java.util.BitSet;

/**
 * Data class that captures an alignment of a term in a segment.
 */
class TermAlignment
{
    private int m_startPosition;
    private int m_endPosition;
    private int m_alignmentScore;
    private int m_totalMatches;
    private int m_segmentLength;
    private int m_termLength;
    private BitSet m_termMatches;
    private BitSet m_segmentMatches;

    //
    // Constructor
    //
    public TermAlignment(int p_segmentLength, int p_termLength)
    {
        m_segmentLength = p_segmentLength;
        m_termLength = p_termLength;
        m_termMatches = new BitSet(p_termLength);
        m_segmentMatches = new BitSet(m_segmentLength);
    }

    //
    // Accessors
    //

    public int getStartPosition()
    {
        return m_startPosition;
    }

    public void setStartPosition(int p_startPosition)
    {
        m_startPosition = p_startPosition;
    }

    public int getEndPosition()
    {
        return m_endPosition;
    }

    public void setEndPosition(int p_endPosition)
    {
        m_endPosition = p_endPosition;
    }

    public int getAlignmentScore()
    {
        return m_alignmentScore;
    }

    public void setAlignmentScore(int p_alignmentScore)
    {
        m_alignmentScore = p_alignmentScore;
    }

    public int getTotalMatches()
    {
        return m_totalMatches;
    }

    public void setTotalMatches(int p_totalMatches)
    {
        m_totalMatches = p_totalMatches;
    }

    public int getSegmentLength()
    {
        return m_segmentLength;
    }

    public void setSegmentLength(int p_segmentLength)
    {
       m_segmentLength = p_segmentLength;
    }

    public int getTermLength()
    {
        return m_termLength;
    }

    public void setTermLength(int p_termLength)
    {
        m_termLength = p_termLength;
    }

    //
    // Interesting Methods
    //

    public BitSet termOverlaps(BitSet p_charMatches)
    {
        BitSet overlaps = (BitSet)p_charMatches.clone();
        overlaps.and(m_termMatches);
        return overlaps;
    }

    public BitSet segmentOverlaps(BitSet p_charMatches)
    {
        BitSet overlaps = (BitSet)p_charMatches.clone();
        overlaps.and(m_segmentMatches);
        return overlaps;
    }

    public BitSet getTermMatches()
    {
        return m_termMatches;
    }

    public BitSet getSegmentMatches()
    {
        return m_segmentMatches;
    }

    public void setMatch(int p_segmentIndex, int p_termIndex)
    {
        m_termMatches.set(p_termIndex);
        m_segmentMatches.set(p_segmentIndex);
    }

    public boolean isTermMatch(int p_index)
    {
        return m_termMatches.get(p_index);
    }

    public boolean isSegmentMatch(int p_index)
    {
        return m_segmentMatches.get(p_index);
    }
}
