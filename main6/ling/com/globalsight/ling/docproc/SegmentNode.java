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
package com.globalsight.ling.docproc;

import com.globalsight.everest.segmentationhelper.Segmentation;

public class SegmentNode 
    implements Cloneable
{
    private String  m_strSegment = ""; // The segment
    private int     m_iWordCount = 0;
    private boolean isLeadingWS = false;
    private boolean isTrailingWS = false;
    private String m_srcComment = null;

    /**
     * SegmentNode constructor comment.
     */
    public SegmentNode()
    {
        super();
    }

    /**
     * Insert the method's description here.
     * Creation date: (4/6/00 12:10:16 PM)
     * @author: Jim Hargrave
     * 
     * @param p_OriginalCopy com.globalsight.ling.docproc.SegmentNode
     */
    public SegmentNode(SegmentNode p_OriginalCopy)
    {
        m_strSegment = new String(p_OriginalCopy.m_strSegment);
        m_iWordCount = p_OriginalCopy.m_iWordCount;
    }

    /**
     * Insert the method's description here.
     * 
     * @return java.lang.String
     */
    public String getSegment()
    {
        return m_strSegment;
    }

    /**
     * Insert the method's description here.
     * 
     * @return int
     */
    public int getWordCount()
    {
        return m_iWordCount;
    }

    /**
     * Insert the method's description here.
     * 
     * @param p_segment java.lang.String
     */
    public void setSegment(String p_segment)
    {
        m_strSegment = p_segment;
    }

    /**
     * Insert the method's description here.
     * 
     * @param p_wordCount int
     */
    public void setWordCount(int p_wordCount)
    {
        m_iWordCount = p_wordCount;
    }

    /**
     * Insert the method's description here.
     * 
     * @param p_segment java.lang.String
     */
    public SegmentNode(String p_segment)
    {
        m_strSegment = p_segment;
    }

    public boolean isLeadingWS()
    {
        return isLeadingWS;
    }

    public void setIsLeadingWS(boolean isLeadingWS)
    {
        this.isLeadingWS = isLeadingWS;
    }

    public boolean isTrailingWS()
    {
        return isTrailingWS;
    }

    public void setIsTrailingWS(boolean isTrailingWS)
    {
        this.isTrailingWS = isTrailingWS;
    }
    
    public boolean outputToSkeleton()
    {
        if (isLeadingWS)
        {
            return true;
        }
        
        if (isTrailingWS)
        {
            return true;
        }
        
        if (Segmentation.isWhitespaceString(m_strSegment))
        {
            return true;
        }
        
        return false;
    }

    public String getSrcComment()
    {
        return m_srcComment;
    }

    public void setSrcComment(String m_srcComment)
    {
        this.m_srcComment = m_srcComment;
    }
}
