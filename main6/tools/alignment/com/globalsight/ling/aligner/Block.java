/*
Copyright (c) 2000 GlobalSight Corporation. All rights reserved.

THIS DOCUMENT CONTAINS TRADE SECRET DATA WHICH IS THE PROPERTY OF
GLOBALSIGHT CORPORATION. THIS DOCUMENT IS SUBMITTED TO RECIPIENT
IN CONFIDENCE. INFORMATION CONTAINED HEREIN MAY NOT BE USED, COPIED
OR DISCLOSED IN WHOLE OR IN PART EXCEPT AS PERMITTED BY WRITTEN
AGREEMENT SIGNED BY AN OFFICER OF GLOBALSIGHT CORPORATION.

THIS MATERIAL IS ALSO COPYRIGHTED AS AN UNPUBLISHED WORK UNDER
SECTIONS 104 AND 408 OF TITLE 17 OF THE UNITED STATES CODE.
UNAUTHORIZED USE, COPYING OR OTHER REPRODUCTION IS PROHIBITED
BY LAW.
*/

package com.globalsight.ling.aligner;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

class Block
{
    private boolean m_SegmentsAligned = true;
    private List m_SegmentList = null;
    private int m_segmentCount = 0;   
    private int m_segmentTagMismatches = 0;
    private String m_locale;
    private Iterator m_iterator;

    /**
     * Block constructor comment.
     */
    public Block()
    {
        super();
        m_SegmentList = new LinkedList();
    }

    /**
     *
     * @param newSegmentsAligned boolean
     */
    public void setSegmentsAligned(boolean p_SegmentsAligned)
    {
        m_SegmentsAligned = p_SegmentsAligned;       
    }

    /**
     *
     * @return boolean
     */
    public boolean areSegmentsAligned()
    {
        return m_SegmentsAligned;
    }

    /**
     *
     * @param p_segment com.globalsight.ling.aligner.Segment
     */
    public void addSegment(Segment p_segment)
    {
        p_segment.setLocale(m_locale);
        m_SegmentList.add(p_segment);
        m_segmentCount++;
        if (!p_segment.areTagsAligned())
        {
            m_segmentTagMismatches++;
        }
    }

    /**
     *
     * @return java.util.Iterator
     */
    Iterator getSegmentListIterator()
    {
        return m_SegmentList.iterator();
    }
    
    public int getSegmentCount()
    {
        return m_segmentCount;
    }
    
    public int getSegmentMismatches()
    {
        if (m_SegmentsAligned)
        {
            return 0;
        }
        
        return m_SegmentList.size();
    }
    
    public int getSegmentTagMismatches()
    {
        return m_segmentTagMismatches;
    }    
    
    /** Getter for property m_locale.
     * @return Value of property m_locale.
     */
    public String getLocale()
    {
        return m_locale;
    }
    
    /** Setter for property m_locale.
     * @param m_locale New value of property m_locale.
     */
    public void setLocale(String p_locale)
    {
        this.m_locale = p_locale;
    }
    
    public void startIteration()
    {
        m_iterator = m_SegmentList.iterator();
    }
    
    public Segment getNext()
    {
        if (m_iterator.hasNext())
        {
            return (Segment)m_iterator.next();
        }
        
        return null;
    }    
}