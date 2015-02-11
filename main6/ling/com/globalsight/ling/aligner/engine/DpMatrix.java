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

import com.globalsight.ling.tm.LingManagerException;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * DpMatrix is a matrix used in dynamic programming. Two sequences to
 * be aligned are passed to this class in a form of List of
 * Object. Each cell (DpMatrixCell) of the matrix records the
 * alignment score of the two elements. The score is calculated by
 * DpFunction class that is passed to the constructor. The alignment
 * result is returned as a List of DpMatrixCell.
 */

public class DpMatrix
{
    // matrix
    private DpMatrixCell[][] m_matrix;
    
    // sequences to be aligned
    private List m_sequenceX;
    private List m_sequenceY;
    
    // DpFunction class
    private DpFunction m_dpFunction;
    

    // constructor
    public DpMatrix(List p_sequenceX, List p_sequenceY,
        DpFunction p_dpFunction)
        throws LingManagerException
    {
        m_sequenceX = p_sequenceX;
        m_sequenceY = p_sequenceY;

        m_dpFunction = p_dpFunction;
        
        m_matrix = new DpMatrixCell
            [m_sequenceX.size() + 1][m_sequenceY.size() + 1];
        initMatrix();
    }


    /**
     * Align the two sequences. The result is returned as a List of
     * DpMatrixCell in order from the start to the end. Each
     * DpMatrixCell may indicate the match, insertion or deletion of
     * the elements. Some may even indicate the 1:n, n:1 or n:n
     * match. The match state should be queried to DpMatrixCell.
     *
     * @return List of DpMatrixCell 
     */
    public List align()
        throws LingManagerException
    {
        fillMatrix();
        return alignedResult();
    }
    

    /**
     * Returns an element of X sequence.
     *
     * @param p_xIdx index of X sequence. The index starts with 1. The
     * rule of sum is to specify the same index as the cell's X index,
     * then you'll get what you want. If 0 is specified,
     * IndexOutOfBoundsException will be thrown.
     * @return Object
     */
    public Object getAlignmentElementX(int p_xIdx)
    {
        return m_sequenceX.get(p_xIdx - 1);
    }
    

    /**
     * Returns an element of Y sequence.
     *
     * @param p_yIdx index of Y sequence. The index starts with 1. The
     * rule of sum is to specify the same index as the cell's Y index,
     * then you'll get what you want. If 0 is specified,
     * IndexOutOfBoundsException will be thrown.
     * @return Object
     */
    public Object getAlignmentElementY(int p_yIdx)
    {
        return m_sequenceY.get(p_yIdx - 1);
    }
    

    /**
     * Returns a List of X sequence elements starting at p_xFromIdx
     * (inclusive) and ending at p_xToIdx (exclusive).
     *
     * @param p_xFromIdx start index of X sequence (inclusive). The
     * index starts with 1. Usually, a return value of
     * DpMatrixCell#getMultiMatchXIndexBegin() is specified.
     * @param p_xToIdx end index of X sequence (exclusive). The index
     * starts with 1. Usually, a return value of
     * DpMatrixCell#getMultiMatchXIndexEnd() is specified.
     * @return List of X sequence elements
     */
    public List getAlignmentElementsX(int p_xFromIdx, int p_xToIdx)
    {
        return new ArrayList(
            m_sequenceX.subList(p_xFromIdx - 1, p_xToIdx - 1));
    }
    

    /**
     * Returns a List of Y sequence elements starting at p_yFromIdx
     * (inclusive) and ending at p_yToIdx (exclusive).
     *
     * @param p_yFromIdx start index of Y sequence (inclusive). The
     * index starts with 1. Usually, a return value of
     * DpMatrixCell#getMultiMatchYIndexBegin() is specified.
     * @param p_yToIdx end index of Y sequence (exclusive). The index
     * starts with 1. Usually, a return value of
     * DpMatrixCell#getMultiMatchYIndexEnd() is specified.
     * @return List of Y sequence elements
     */
    public List getAlignmentElementsY(int p_yFromIdx, int p_yToIdx)
    {
        return new ArrayList(
            m_sequenceY.subList(p_yFromIdx - 1, p_yToIdx - 1));
    }



    /**
     * Return a cell of the matrix.
     *
     * @param p_xIdx Index of X axis. The index starts with 0.
     * @param p_yIdx Index of Y axis. The index starts with 0.
     * @return DpMatrixCell object
     */
    public DpMatrixCell getCell(int p_xIdx, int p_yIdx)
    {
        return m_matrix[p_xIdx][p_yIdx];
    }
    

    // initialize the matrix. Set DpMatrixCell in each matrix cell and
    // initialize cells (0, y) and (x, 0).
    private void initMatrix()
        throws LingManagerException
    {
        for(int x = 0; x < m_matrix.length; x++)
        {
            for(int y = 0; y < m_matrix[x].length; y++)
            {
                m_matrix[x][y] = new DpMatrixCell(x, y);
            }
        }

//         // initialize cells (0,y) and (x,0)
//         for(int x = 1; x < m_matrix.length; x++)
//         {
//             m_matrix[x][0].setScoreAndLink(0, m_matrix[x - 1][0]);
//         }
        
//         for(int y = 1; y < m_matrix[0].length; y++)
//         {
//             m_matrix[0][y].setScoreAndLink(0, m_matrix[0][y - 1]);
//         }
    }

    
    // Fill the matrix with score. The initial stage of dynamic
    // programming. Fill the matrix cell from (1, 1) to (n, m). Cells
    // (0, x) and (y, 0) are already initialized in initMatrix()
    // method.
    private void fillMatrix()
        throws LingManagerException
    {
        for(int x = 0; x < m_matrix.length; x++)
        {
            for(int y = 0; y < m_matrix[x].length; y++)
            {
                m_dpFunction.setCellScore(x, y, this);
            }
        }
    }


    // get the alignment result
    private List alignedResult()
    {
        LinkedList result = new LinkedList();
        
        // get the right bottom cell
        DpMatrixCell cell = m_matrix[m_sequenceX.size()][m_sequenceY.size()];
        
        while(cell.hasNext())
        {
            result.addFirst(cell);
            cell = cell.nextCell();
        }
        
        return result;
    }


    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        for(int y = 0; y < m_matrix[0].length; y++)
        {
            for(int x = 0; x < m_matrix.length; x++)
            {
                if(x == 0)
                {
                    sb.append("\r\n");
                }

                DpMatrixCell cell = m_matrix[x][y];
                sb.append(cell.getScore()).append("\t");
                
//                 sb.append(">>").append(x).append(":")
//                     .append(y).append(" ").append(cell.toString());
            }
        }
        
        return sb.toString();
    }
    
}
