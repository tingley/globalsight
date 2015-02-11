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
package com.globalsight.ling.aligner.engine;

import org.apache.log4j.Logger;

import com.globalsight.ling.tm.LingManagerException;

/**
 * DpMatrixCell is a cell of DpMatrix. It records the score of the
 * cell, the state of the cell (match, insertion or subtraction), the
 * possible path(s) to the upper left corner of the matrix. The cell
 * supports 1:n and n:m matches, too.
 */

public class DpMatrixCell
{
    private static final Logger c_logger =
        Logger.getLogger(
            DpMatrixCell.class);

    private DpMatrixCell m_backLink;
    private int m_xPos;
    private int m_yPos;
    private int m_score;
    private int m_state;
    
    public static final int DELETED = 1;
    public static final int INSERTED = 2;
    public static final int MATCH = 3;
    public static final int MULTI_MATCH = 4;
    public static final int NO_STATE = -1;
    

    public DpMatrixCell(int p_xPos, int p_yPos)
    {
        m_xPos = p_xPos;
        m_yPos = p_yPos;

        m_score = 0;
        m_state = NO_STATE;
        m_backLink = null;
    }
    

    public boolean hasNext()
    {
        return m_backLink != null;
    }
    

    public DpMatrixCell nextCell()
    {
        return m_backLink;
    }


    public int getXindex()
    {
        return m_xPos;
    }
    

    public int getYindex()
    {
        return m_yPos;
    }
    

    public int getScore()
    {
        return m_score;
    }
    

    public int getState()
    {
        return m_state;
    }


    /**
     * Return start index (inclusive) of X axis of the matrix for the
     * multiple match. The return value can be used in
     * DpMatrix#getAlignmentElementsX() method. This should be called
     * only when the state is MULTI_MATCH.
     */
    public int getMultiMatchXIndexBegin()
    {
        return m_backLink.m_xPos + 1;
    }
    

    /**
     * Return start index (inclusive) of Y axis of the matrix for the
     * multiple match. The return value can be used in
     * DpMatrix#getAlignmentElementsY() method. This should be called
     * only when the state is MULTI_MATCH.
     */
    public int getMultiMatchYIndexBegin()
    {
        return m_backLink.m_yPos + 1;
    }


    /**
     * Return end index (exclusive) of X axis of the matrix for the
     * multiple match. The return value can be used in
     * DpMatrix#getAlignmentElementsX() method. This should be called
     * only when the state is MULTI_MATCH.
     */
    public int getMultiMatchXIndexEnd()
    {
        return m_xPos + 1;
    }
    

    /**
     * Return end index (exclusive) of Y axis of the matrix for the
     * multiple match. The return value can be used in
     * DpMatrix#getAlignmentElementsX() method. This should be called
     * only when the state is MULTI_MATCH.
     */
    public int getMultiMatchYIndexEnd()
    {
        return m_yPos + 1;
    }

    
    public void setScoreAndLink(int p_score, DpMatrixCell p_backLink)
        throws LingManagerException
    {
        m_backLink = p_backLink;
        m_score = p_score;
        m_state = getState(p_backLink);
    }
    

    private int getState(DpMatrixCell p_backLink)
        throws LingManagerException
    {
        int state = NO_STATE;
        
        int linkX = p_backLink.m_xPos;
        int linkY = p_backLink.m_yPos;
        
        if((linkX == m_xPos && linkY == m_yPos)
            || linkX > m_xPos || linkY > m_yPos)
        {
            String[] params = new String[4];
            params[0] = Integer.toString(m_xPos);
            params[1] = Integer.toString(m_yPos);
            params[2] = Integer.toString(linkX);
            params[3] = Integer.toString(linkY);
            
            throw new LingManagerException("InvalidBackLink", params, null);
        }
        
        if(linkX == m_xPos)
        {
            state = INSERTED;
        }
        else if(linkY == m_yPos)
        {
            state = DELETED;
        }
        else if(linkX == m_xPos - 1 && linkY == m_yPos - 1)
        {
            state = MATCH;
        }
        else
        {
            state = MULTI_MATCH;
        }
        
        return state;
    }
    

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[ xPos = ").append(m_xPos).append(" yPos = ")
            .append(m_yPos).append(" score = ").append(m_score)
            .append(" state = ");

        String state = null;
        switch(m_state)
        {
        case DELETED:
            state = "DELETED";
            break;
        case INSERTED:
            state = "INSERTED";
            break;
        case MATCH:
            state = "MATCH";
            break;
        case MULTI_MATCH:
            state = "MULTI_MATCH";
            break;
        case NO_STATE:
            state = "NO_STATE";
            break;
        }
        
        sb.append(state).append("]\r\n");
        
        return sb.toString();
    }
    
        
}
