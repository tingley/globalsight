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

package com.globalsight.everest.costing;


import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.page.TargetPage;

/**
 * This class is used to update the workflow word counts based
 * on the leverage match threshold and the sub leverage match word count.
 * Note that anything below the lev-match-threshold is considered as no-match
 * for costing.
 */

public class WordcountForCosting
{
    private int m_lowFuzzyMatchCount = 0;
    private int m_medFuzzyMatchCount = 0;
    private int m_medHiFuzzyMatchCount = 0;
    private int m_hiFuzzyMatchCount = 0;
    private int m_totalSubLevMatchCount = 0;
    private int m_levMatchThreshold = 0;

    //
    // Constructor
    //
    public WordcountForCosting(Workflow p_workflow)
    {
        this(p_workflow.getJob().getLeverageMatchThreshold(),
             p_workflow.getLowFuzzyMatchWordCount(),
             p_workflow.getMedFuzzyMatchWordCount(),
             p_workflow.getMedHiFuzzyMatchWordCount(),
             p_workflow.getHiFuzzyMatchWordCount(),
             p_workflow.getSubLevMatchWordCount() +
             p_workflow.getSubLevRepetitionWordCount());
    }

    /**
     * Constructor - Target Page word count
     */
    public WordcountForCosting(TargetPage p_tp)
    {
        this(p_tp.getWorkflowInstance().
             getJob().getLeverageMatchThreshold(),
             p_tp.getWordCount().getLowFuzzyWordCount(),
             p_tp.getWordCount().getMedFuzzyWordCount(),
             p_tp.getWordCount().getMedHiFuzzyWordCount(),
             p_tp.getWordCount().getHiFuzzyWordCount(),
             p_tp.getWordCount().getSubLevMatchWordCount() +
             p_tp.getWordCount().getSubLevRepetitionWordCount());
    }


    /**
     * Creates an object to hold updated word counts for costing based on
     *  the Leverage Match Threshold.  If the LMT is below 50%, then the 
     * original values are not changed.
     * 
     * @param p_levMatchThreshold
     * @param p_lowFuzzyMatchCount
     * @param p_medFuzzyMatchCount
     * @param p_medHiFuzzyMatchCount
     * @param p_hiFuzzyMatchCount
     * @param p_totalSubLevMatchCount
     */
    public WordcountForCosting(
        int p_levMatchThreshold, int p_lowFuzzyMatchCount, 
        int p_medFuzzyMatchCount, int p_medHiFuzzyMatchCount, 
        int p_hiFuzzyMatchCount,int p_totalSubLevMatchCount)
    {
        m_levMatchThreshold = p_levMatchThreshold;
        m_totalSubLevMatchCount = p_totalSubLevMatchCount;
        m_lowFuzzyMatchCount = p_lowFuzzyMatchCount;
        m_medFuzzyMatchCount = p_medFuzzyMatchCount;
        m_medHiFuzzyMatchCount = p_medHiFuzzyMatchCount;
        m_hiFuzzyMatchCount = p_hiFuzzyMatchCount;

        if (m_levMatchThreshold > 50 && m_levMatchThreshold <= 74)
        {
            m_lowFuzzyMatchCount -= m_totalSubLevMatchCount;
        }
        else if (m_levMatchThreshold >= 75 && 
                 m_levMatchThreshold <=84)
        {
            m_medFuzzyMatchCount -= (m_totalSubLevMatchCount - 
                                     m_lowFuzzyMatchCount);

            m_lowFuzzyMatchCount = 0;
        }
        else if (m_levMatchThreshold >= 85 &&
                 m_levMatchThreshold <=94)
        {
            m_medHiFuzzyMatchCount -= (m_totalSubLevMatchCount - 
                                       (m_lowFuzzyMatchCount + m_medFuzzyMatchCount));

            m_lowFuzzyMatchCount = 0;
            m_medFuzzyMatchCount = 0;
        }
        else if (m_levMatchThreshold >= 95 &&
                 m_levMatchThreshold <=99)
        {
            m_hiFuzzyMatchCount -= (m_totalSubLevMatchCount - 
                                    (m_lowFuzzyMatchCount + m_medFuzzyMatchCount + 
                                     m_medHiFuzzyMatchCount));

            m_lowFuzzyMatchCount = 0;
            m_medFuzzyMatchCount = 0;
            m_medHiFuzzyMatchCount = 0;
        }
        else if (m_levMatchThreshold > 99)
        {
            m_lowFuzzyMatchCount = 0;
            m_medFuzzyMatchCount = 0;
            m_medHiFuzzyMatchCount = 0;
            m_hiFuzzyMatchCount = 0;
        }

    }


    //
    // Public Helper Methods
    //

    /**
     * Get the updated low-fuzzy-match word count based on the
     * leverage match threshold.
     */
    public int updatedLowFuzzyMatchCount()
    {
        return m_lowFuzzyMatchCount;
    }

    /**
     * Get the updated med-fuzzy-match word count based on the
     * leverage match threshold.
     */
    public int updatedMedFuzzyMatchCount()
    {
        return m_medFuzzyMatchCount;
    }

    /**
     * Get the updated medHi-fuzzy-match word count based on the
     * leverage match threshold.
     */
    public int updatedMedHiFuzzyMatchCount()
    {
        return m_medHiFuzzyMatchCount;
    }

    /**
     * Get the updated hi-fuzzy-match word count based on the
     * leverage match threshold.
     */
    public int updatedHiFuzzyMatchCount()
    {
        return m_hiFuzzyMatchCount;
    }

    /**
     * Gets the total sublev match count used
     */
    public int getTotalSubLevMatchCount()
    {
        return m_totalSubLevMatchCount;
    }

    /**
     * Returns the leverage match threshold used.
     */
    public int getLevMatchThreshold()
    {
        return m_levMatchThreshold;
    }
}

