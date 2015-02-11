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

import com.globalsight.ling.aligner.gxml.Skeleton;
import com.globalsight.ling.common.Text;
import com.globalsight.ling.tm.LingManagerException;

/**
 * SkeletonAlignmentFunction implements DpFunction. The class is used
 * to align source and target skeleton. Aligned skeletons work as
 * paragraph delimiters. The type of the alignment element is
 * Skeleton.
 */

public class SkeletonAlignmentFunction
    implements DpFunction
{
    private static final int GAP_PENALTY = 0;
    private static final int MATCH_SCORE = 1;
    private static final int MISMATCH_SCORE = 0;
    
    /**
     * Set the score to DpMatrixCell at the specified location of the matrix.
     *
     * @param p_xPos X index of the matrix.
     * @param p_yPos Y index of the matrix.
     * @param p_matrix matrix
     */
    public void setCellScore(int p_xPos, int p_yPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        if(p_xPos == 0 && p_yPos == 0)
        {
            return;
        }
        
        DpMatrixCell currentCell = p_matrix.getCell(p_xPos, p_yPos);

        DpMatrixCell aboveCell = null;
        DpMatrixCell leftCell = null;
        DpMatrixCell diagonalCell = null;
        int aboveScore = -1;
        int leftScore = -1;
        int diagScore = -1;
        
        if(p_yPos > 0)
        {
            aboveCell = p_matrix.getCell(p_xPos, p_yPos - 1);
            aboveScore = aboveCell.getScore() + GAP_PENALTY;
        }
        
        if(p_xPos > 0)
        {
            leftCell = p_matrix.getCell(p_xPos - 1, p_yPos);
            leftScore = leftCell.getScore() + GAP_PENALTY;
        }
        
        if(p_xPos > 0 && p_yPos > 0)
        {
            diagonalCell = p_matrix.getCell(p_xPos - 1, p_yPos - 1);

            Skeleton sourceSkel
                = (Skeleton)p_matrix.getAlignmentElementX(p_xPos);
            Skeleton targetSkel
                = (Skeleton)p_matrix.getAlignmentElementY(p_yPos);

            String sourceSkelString
                = Text.normalizeWhiteSpaces(sourceSkel.getSkeletonString());
            String targetSkelString
                = Text.normalizeWhiteSpaces(targetSkel.getSkeletonString());
        
            diagScore = diagonalCell.getScore()
                + (sourceSkelString.equals(targetSkelString)
                    ? MATCH_SCORE : MISMATCH_SCORE);
        }
        
        DpMatrixCell backLink = null;
        int maxScore = -1;
            
        if(aboveScore > leftScore)
        {
            maxScore = aboveScore;
            backLink = aboveCell;
        }
        else
        {
            maxScore = leftScore;
            backLink = leftCell;
        }
            
        if(maxScore < diagScore)
        {
            maxScore = diagScore;
            backLink = diagonalCell;
        }
            
        currentCell.setScoreAndLink(maxScore, backLink);
    }
}
