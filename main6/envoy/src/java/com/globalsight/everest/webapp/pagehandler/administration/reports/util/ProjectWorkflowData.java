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
package com.globalsight.everest.webapp.pagehandler.administration.reports.util;

import java.math.BigDecimal;
import java.util.Date;

public class ProjectWorkflowData
{
    // defines a 0 format for a 3 decimal precision point BigDecimal
    public static final String BIG_DECIMAL_ZERO_STRING = "0.000";
    
    // the big decimal scale to use for internal math
    public static int SCALE = 3;
    
    public String jobName;

    public long jobId = -1;
    public String workflowName;
    public String projectDesc;
    public String targetLang;

    public Date creationDate;
    public Date estimatedTranslateCompletionDate;
    public Date actualTranslateCompletionDate;
    public String leadtime;
    public String actualPerformance;
    
    public String currentActivityName;
    
    public int dellReviewActivityState = 0;
    public Date acceptedReviewerDate;
    
    /* Dell values */
    public long dellInternalRepsWordCount = 0;

    public long dellExactMatchWordCount = 0;
    
    public long dellInContextMatchWordCount = 0;
    
    public long dellContextMatchWordCount = 0;

    public long dellFuzzyMatchWordCount = 0;

    public long dellNewWordsWordCount = 0;

    public long dellTotalWordCount = 0;

    // "Trados" values
    public long trados100WordCount = 0;
    
    public long tradosInContextWordCount = 0;
    
    public long tradosContextWordCount = 0;

    public long trados95to99WordCount = 0;

    public long trados85to94WordCount = 0;

    public long trados75to84WordCount = 0;
    
    public long trados50to74WordCount = 0;

    public long tradosNoMatchWordCount = 0;

    public long tradosRepsWordCount = 0;

    public long tradosTotalWordCount = 0;

    /* word counts */
    public long repetitionWordCount = 0;

    public long lowFuzzyMatchWordCount = 0;

    public long medFuzzyMatchWordCount = 0;

    public long medHiFuzzyMatchWordCount = 0;

    public long hiFuzzyMatchWordCount = 0;

    public long contextMatchWordCount = 0;
    
    public long inContextMatchWordCount = 0;

    public long segmentTmWordCount = 0;

    public long noMatchWordCount = 0;

    public long totalWordCount = 0;

    /* word count costs */
    public BigDecimal repetitionWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal lowFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal medFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal medHiFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal hiFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal contextMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal inContextMatchWordCountCost = new BigDecimal(
    		BIG_DECIMAL_ZERO_STRING);
    

    public BigDecimal segmentTmWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal noMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal totalWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Dell values */
    public BigDecimal dellInternalRepsWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellExactMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal dellInContextMatchWordCountCost = new BigDecimal(
    		BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellContextMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal dellFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellNewWordsWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellTotalWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Trados values */
    public BigDecimal trados100WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal tradosInContextWordCountCost = new BigDecimal(
    		BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosContextWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal trados95to99WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal trados85to94WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal trados75to84WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal trados50to74WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosRepsWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosTotalWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosNoMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Dell values for non "REPORT_ACTIVITY" */
    public BigDecimal dellTotalWordCountCostForTranslation = new BigDecimal(BIG_DECIMAL_ZERO_STRING);

    /* Trados values for non "REPORT_ACTIVITY" */
    public BigDecimal tradosTotalWordCountCostForTranslation = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Word count costs for activity named "REPORT_ACTIVITY" */
    public BigDecimal repetitionWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal lowFuzzyMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal medFuzzyMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal medHiFuzzyMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal hiFuzzyMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal contextMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal inContextMatchWordCountCostForDellReview = new BigDecimal(
    		BIG_DECIMAL_ZERO_STRING);

    public BigDecimal segmentTmWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal noMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal totalWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Dell values for activity named "REPORT_ACTIVITY" */
    public BigDecimal dellInternalRepsWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellExactMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal dellInContextMatchWordCountCostForDellReview = new BigDecimal(
    		BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellContextMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal dellFuzzyMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellNewWordsWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal dellTotalWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Trados values for activity named "REPORT_ACTIVITY" */
    public BigDecimal trados100WordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal tradosInContextWordCountCostForDellReview = new BigDecimal(
    		BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal tradosContextWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    public BigDecimal trados95to99WordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal trados75to94WordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal trados1to74WordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosRepsWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosTotalWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public BigDecimal tradosNoMatchWordCountCostForDellReview = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    public ProjectWorkflowData()
    {
    }
    
    
    
    
    
}
