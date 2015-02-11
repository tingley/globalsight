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

/**
 * SegmentAlignmentScorer defines interface of the source and target
 * segment alignment score functions. Score functions include
 * deletionScore (1-0), insertionScore (0-1), substitutionScore (1-1),
 * contractionScore (2-1), expansionScore (1-2) and meldingScore
 * (2-2).
 */
public interface SegmentAlignmentScorer
{
    /**
     * Set source and target locales.
     *
     * @param p_sourceLocale Source locale
     * @param p_targetLocale Target locale
     */
    public void setLocales(
        GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
        throws LingManagerException;
    
    /**
     * Calculate the cost of substitution of source segment by target
     * segment.
     *
     * @param p_sourceTuv Source TUV. Source is in X sequence in the DP map.
     * @param p_targetTuv Target TUV. Target is in Y sequence in the DP map.
     * @return cost of the substitution
     */
    public int substitutionScore(BaseTmTuv p_sourceTuv, BaseTmTuv p_targetTuv)
        throws LingManagerException;
    

    /**
     * Calculate the cost of deletion of source segment.
     *
     * @param p_sourceTuv Source TUV. Source is in X sequence in the DP map.
     * @return cost of the deletion
     */
    public int deletionScore(BaseTmTuv p_sourceTuv)
        throws LingManagerException;
    

    /**
     * Calculate the cost of insertion of target segment.
     *
     * @param p_targetTuv Target TUV. Target is in Y sequence in the DP map.
     * @return cost of the insertion
     */
    public int insertionScore(BaseTmTuv p_targetTuv)
        throws LingManagerException;
    
    /**
     * Calculate the cost of contracting two source segments to one
     * target segment.
     *
     * @param p_sourceTuv1 Source TUV1. Source is in X sequence in the DP map.
     * @param p_sourceTuv2 Source TUV2. Source is in X sequence in the DP map.
     * @param p_targetTuv Target TUV. Target is in Y sequence in the DP map.
     * @return cost of the contraction
     */
    public int contractionScore(BaseTmTuv p_sourceTuv1,
        BaseTmTuv p_sourceTuv2, BaseTmTuv p_targetTuv)
        throws LingManagerException;
    

    /**
     * Calculate the cost of expanding one source segment to two
     * target segments.
     *
     * @param p_sourceTuv Source TUV. Source is in X sequence in the DP map.
     * @param p_targetTuv1 Target TUV1. Target is in Y sequence in the DP map.
     * @param p_targetTuv2 Target TUV2. Target is in Y sequence in the DP map.
     * @return cost of the expansion
     */
    public int expansionScore(BaseTmTuv p_sourceTuv,
        BaseTmTuv p_targetTuv1, BaseTmTuv p_targetTuv2)
        throws LingManagerException;
    

    /**
     * Calculate the cost of melding of two source segments to two
     * target segments.
     *
     * @param p_sourceTuv1 Source TUV1. Source is in X sequence in the DP map.
     * @param p_sourceTuv2 Source TUV2. Source is in X sequence in the DP map.
     * @param p_targetTuv1 Target TUV1. Target is in Y sequence in the DP map.
     * @param p_targetTuv2 Target TUV2. Target is in Y sequence in the DP map.
     * @return cost of the melding
     */
    public int meldingScore(BaseTmTuv p_sourceTuv1, BaseTmTuv p_sourceTuv2,
        BaseTmTuv p_targetTuv1, BaseTmTuv p_targetTuv2)
        throws LingManagerException;
}
    
