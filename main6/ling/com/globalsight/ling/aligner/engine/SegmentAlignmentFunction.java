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

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.ling.tm2.BaseTmTuv;
import com.globalsight.ling.tm.LingManagerException;

import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * SegmentAlignmentFunction implements DpFunction. The class is used
 * to align source and target segments. The type of the alignment
 * element is PageTmTuv.
 */

public class SegmentAlignmentFunction
    implements DpFunction
{
    /* -100 * log([prob of 2-1 match] / [prob of 1-1 match]) */
    private final static int PENALTY_21 = 400;  // orig 230

    /* -100 * log([prob of 2-2 match] / [prob of 1-1 match]) */
    private final static int PENALTY_22 = 440;  // orig 440

    /* -100 * log([prob of 0-1 match] / [prob of 1-1 match]) */
    private final static int PENALTY_01 = 100;  // orig 450


    private GlobalSightLocale m_sourceLocale;
    private GlobalSightLocale m_targetLocale;

    private List m_scorerList;
    
    public SegmentAlignmentFunction(
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
        throws LingManagerException
    {
        m_sourceLocale = p_sourceLocale;
        m_targetLocale = p_targetLocale;

        m_scorerList = new ArrayList();
        try
        {
            ResourceBundle res
                = ResourceBundle.getBundle("properties/aligner/Scorer");
            Enumeration keys = res.getKeys();
            while(keys.hasMoreElements())
            {
                String scorerClass = (String)keys.nextElement();
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)Class.forName(scorerClass)
                    .newInstance();
                scorer.setLocales(m_sourceLocale, m_targetLocale);

                m_scorerList.add(scorer);
            }
        }
        catch(Exception e)
        {
            throw new LingManagerException(e);
        }
        
    }
    
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
        
        DpMatrixCell currentCell = getCell(p_xPos, p_yPos, p_matrix);

        DpMatrixCell insertionCell = getCell(p_xPos, p_yPos - 1, p_matrix);
        DpMatrixCell deletionCell = getCell(p_xPos - 1, p_yPos, p_matrix);
        DpMatrixCell substitutionCell
            = getCell(p_xPos - 1, p_yPos - 1, p_matrix);
        DpMatrixCell contractionCell
            = getCell(p_xPos - 2, p_yPos - 1, p_matrix);
        DpMatrixCell expansionCell = getCell(p_xPos - 1, p_yPos - 2, p_matrix);
        DpMatrixCell meldingCell = getCell(p_xPos - 2, p_yPos - 2, p_matrix);

            
        int deletionScore = getDeletionScore(deletionCell, p_xPos, p_matrix);
        int insertionScore
            = getInsertionScore(insertionCell, p_yPos, p_matrix);
        int substitutionScore
            = getSubstitutionScore(substitutionCell, p_xPos, p_yPos, p_matrix);
        int contractionScore
            = getContractionScore(contractionCell, p_xPos, p_yPos, p_matrix);
        int expansionScore
            = getExpansionScore(expansionCell, p_xPos, p_yPos, p_matrix);
        int meldingScore
            = getMeldingScore(meldingCell, p_xPos, p_yPos, p_matrix);

        setScoreAndLink(currentCell,
            deletionCell, insertionCell, substitutionCell,
            contractionCell, expansionCell, meldingCell,
            deletionScore, insertionScore, substitutionScore,
            contractionScore, expansionScore, meldingScore);
        
    }


    private DpMatrixCell getCell(int p_xPos, int p_yPos, DpMatrix p_matrix)
    {
        DpMatrixCell cell = null;
        
        if(p_xPos >= 0 && p_yPos >= 0)
        {
            cell = p_matrix.getCell(p_xPos, p_yPos);            
        }
        
        return cell;
    }
    
    
    private int getDeletionScore(
        DpMatrixCell p_deletionCell, int p_xPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        int score = Integer.MAX_VALUE;

        if(p_deletionCell != null)
        {
            score = 0;
            BaseTmTuv tuv = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos);
        
            Iterator it = m_scorerList.iterator();
            while(it.hasNext())
            {
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)it.next();
                score += scorer.deletionScore(tuv);
            }
            
            score += p_deletionCell.getScore() + PENALTY_01;
        }
        
        return score;
    }
    

    private int getInsertionScore(
        DpMatrixCell p_insertionCell, int p_yPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        int score = Integer.MAX_VALUE;

        if(p_insertionCell != null)
        {
            score = 0;
            BaseTmTuv tuv = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos);
        
            Iterator it = m_scorerList.iterator();
            while(it.hasNext())
            {
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)it.next();
                score += scorer.insertionScore(tuv);
            }

            score += p_insertionCell.getScore() + PENALTY_01;
        }
        
        return score;
    }
    

    private int getSubstitutionScore(DpMatrixCell p_substitutionCell,
        int p_xPos, int p_yPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        int score = Integer.MAX_VALUE;

        if(p_substitutionCell != null)
        {
            score = 0;
            BaseTmTuv sourceTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos);
            BaseTmTuv targetTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos);
        
            Iterator it = m_scorerList.iterator();
            while(it.hasNext())
            {
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)it.next();
                score += scorer.substitutionScore(sourceTuv, targetTuv);
            }

            score += p_substitutionCell.getScore();
        }
        
        return score;
    }
    

    private int getContractionScore(DpMatrixCell p_contractionCell,
        int p_xPos, int p_yPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        int score = Integer.MAX_VALUE;
        
        if(p_contractionCell != null)
        {
            score = 0;
            
            BaseTmTuv currentSourceTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos);
            BaseTmTuv prevSourceTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos - 1);
            BaseTmTuv targetTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos);
        
            Iterator it = m_scorerList.iterator();
            while(it.hasNext())
            {
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)it.next();
                score += scorer.contractionScore(
                    currentSourceTuv, prevSourceTuv, targetTuv);
            }

            score += p_contractionCell.getScore() + PENALTY_21;
        }
        
        return score;
    }
    

    private int getExpansionScore(DpMatrixCell p_expansionCell,
        int p_xPos, int p_yPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        int score = Integer.MAX_VALUE;
        
        if(p_expansionCell != null)
        {
            score = 0;
            
            BaseTmTuv sourceTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos);
            BaseTmTuv currentTargetTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos);
            BaseTmTuv prevTargetTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos - 1);
        
            Iterator it = m_scorerList.iterator();
            while(it.hasNext())
            {
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)it.next();
                score += scorer.expansionScore(
                    sourceTuv, currentTargetTuv, prevTargetTuv);
            }

            score += p_expansionCell.getScore() + PENALTY_21;
        }
        
        return score;
    }
    

    private int getMeldingScore(DpMatrixCell p_meldingCell,
        int p_xPos, int p_yPos, DpMatrix p_matrix)
        throws LingManagerException
    {
        int score = Integer.MAX_VALUE;
        
        if(p_meldingCell != null)
        {
            score = 0;
            
            BaseTmTuv currentSourceTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos);
            BaseTmTuv prevSourceTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementX(p_xPos - 1);
            BaseTmTuv currentTargetTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos);
            BaseTmTuv prevTargetTuv
                = (BaseTmTuv)p_matrix.getAlignmentElementY(p_yPos - 1);
        
            Iterator it = m_scorerList.iterator();
            while(it.hasNext())
            {
                SegmentAlignmentScorer scorer
                    = (SegmentAlignmentScorer)it.next();
                score += scorer.meldingScore(currentSourceTuv,
                    prevTargetTuv, currentTargetTuv, prevTargetTuv);
            }

            score += p_meldingCell.getScore() + PENALTY_22;
        }
        
        return score;
    }
    


    private void setScoreAndLink(DpMatrixCell p_currentCell,
        DpMatrixCell p_deletionCell, DpMatrixCell p_insertionCell,
        DpMatrixCell p_substitutionCell, DpMatrixCell p_contractionCell,
        DpMatrixCell p_expansionCell, DpMatrixCell p_meldingCell,
        int p_deletionScore, int p_insertionScore,
        int p_substitutionScore, int p_contractionScore,
        int p_expansionScore, int p_meldingScore)
        throws LingManagerException
    {
        DpMatrixCell backLink = null;
        int minScore = Integer.MAX_VALUE;
            
        if(minScore > p_deletionScore)
        {
            minScore = p_deletionScore;
            backLink = p_deletionCell;
        }

        if(minScore > p_insertionScore)
        {
            minScore = p_insertionScore;
            backLink = p_insertionCell;
        }

        if(minScore > p_substitutionScore)
        {
            minScore = p_substitutionScore;
            backLink = p_substitutionCell;
        }

        if(minScore > p_contractionScore)
        {
            minScore = p_contractionScore;
            backLink = p_contractionCell;
        }

        if(minScore > p_expansionScore)
        {
            minScore = p_expansionScore;
            backLink = p_expansionCell;
        }

        if(minScore > p_meldingScore)
        {
            minScore = p_meldingScore;
            backLink = p_meldingCell;
        }
            
        p_currentCell.setScoreAndLink(minScore, backLink);
    }

}
