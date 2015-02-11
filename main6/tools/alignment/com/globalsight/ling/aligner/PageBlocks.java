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

public class PageBlocks
{
    private String m_Path = null;
    private String m_FileName = null;
    private String m_Locale = null;
    private List m_BlockList = null;
    private boolean m_Aligned = true;
    private boolean m_bWasExtracted = true;
    private Iterator m_blockIterator;
    
    /**
     * AlignedPageSegments constructor comment.
     */
    public PageBlocks()
    {
        super();
        m_BlockList = new LinkedList();        
    }
    
    /**
     *
     * @return java.lang.String
     */
    public java.lang.String getFileName()
    {
        return m_FileName;
    }
    
    /**
     *
     * @param newFileName java.lang.String
     */
    public void setFileName(java.lang.String newFileName)
    {
        m_FileName = newFileName;
    }
    
    /**
     *
     * @return java.lang.String
     */
    public java.lang.String getLocale()
    {
        return m_Locale;
    }
    
    /**
     *
     * @return int
     */
    public int getMismatchedSegments()
    {
        int mismatchedSegments = 0;
        
        Iterator blocks = getBlockListIterator();
        while(blocks.hasNext())
        {
            Block block = (Block)blocks.next();
            mismatchedSegments += block.getSegmentMismatches();
        }
        return mismatchedSegments;
    }
    
    /**
     *
     * @return int
     */
    public int getSegmentsWithMismatchedTags()
    {
        int segmentsWithMismatchedTags = 0;
        
        Iterator blocks = getBlockListIterator();
        while(blocks.hasNext())
        {
            Block block = (Block)blocks.next();
            segmentsWithMismatchedTags += block.getSegmentTagMismatches();
        }
        return segmentsWithMismatchedTags;
    }
    
    /**
     *
     * @return int
     */
    public int getSegmentCount()
    {
        int totalSegments = 0;
        
        Iterator blocks = getBlockListIterator();
        while(blocks.hasNext())
        {
            Block block = (Block)blocks.next();
            totalSegments += block.getSegmentCount();
        }
        return totalSegments;
    }
    
    /**
     *
     * @param newLocale java.lang.String
     */
    public void setLocale(String p_Locale)
    {
        m_Locale = p_Locale;
    }
    
    /**
     *
     * @return java.lang.String
     */
    public String getPath()
    {
        return m_Path;
    }
    
    /**
     *
     * @param newSourcePath java.lang.String
     */
    public void setPath(String p_Path)
    {
        m_Path = p_Path;
    }
    
    /**
     *
     * @param newBlockList java.util.List
     */
    public void addBlock(Block p_block)
    {
        m_BlockList.add(p_block);
    }
    
    /**
     *
     * @return java.util.List
     */
    public Iterator getBlockListIterator()
    {
        return m_BlockList.iterator();
    }
    
    public void startIteration()
    {
        m_blockIterator = m_BlockList.iterator();
    }
    
    public Block getNext()
    {
        if (m_blockIterator.hasNext())
        {
            return (Block)m_blockIterator.next();
        }
        
        return null;
    }
    
    public boolean isAligned()
    {
        return m_Aligned;
    }
    
    
    
    public void setAligned(boolean p_aligned)
    {
        m_Aligned = p_aligned;
    }
    
    public boolean wasExtracted()
    {
        return m_bWasExtracted;
    }
    
    public void setWasExtracted(boolean p_bWasExtracted)
    {
        m_bWasExtracted = p_bWasExtracted;
    }
    
    public int getBlockCount()
    {
        return m_BlockList.size();
    }
    
}