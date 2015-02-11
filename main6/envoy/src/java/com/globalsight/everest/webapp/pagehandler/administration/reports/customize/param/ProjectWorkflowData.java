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
package com.globalsight.everest.webapp.pagehandler.administration.reports.customize.param;

import java.math.BigDecimal;

public class ProjectWorkflowData {

    private static final String BIG_DECIMAL_ZERO_STRING = "0.000";
    //the big decimal scale to use before sending to Excel
    //private static final int SCALE_EXCEL = 3;
    //the big decimal scale to use for internal math
    private static final int SCALE = 3;

    public ProjectWorkflowData() {
        // Nothing here.
    }

    /* Tm values */
    private long tmInternalRepsWordCount = 0;

    private long tmExactMatchWordCount = 0;
    
    private long tmInContextMatchWordCount = 0;

    private long tmFuzzyMatchWordCount = 0;

    private long tmNewWordsWordCount = 0;

    private long tmTotalWordCount = 0;
    
    /* Tm values amount*/
    private long tmInternalRepsWordCountAmount = 0;

    private long tmExactMatchWordCountAmount = 0;
    
    private long tmInContextMatchWordCountAmount = 0;

    private long tmFuzzyMatchWordCountAmount = 0;

    private long tmNewWordsWordCountAmount = 0;

    private long tmTotalWordCountAmount = 0;
    
    // "Trados" values
    private long trados100WordCount = 0;
    
    private long tradosInContextWordCount = 0;

    private long trados95to99WordCount = 0;

    private long trados85to94WordCount = 0;
    
    private long trados75to84WordCount = 0;

    private long trados50to74WordCount = 0;

    private long tradosNoMatchWordCount = 0;

    private long tradosRepsWordCount = 0;

    private long tradosTotalWordCount = 0;
    
    // "Trados" values amount
    private long trados100WordCountAmount = 0;
    
    private long tradosInContextWordCountAmount = 0;

    private long trados95to99WordCountAmount = 0;

    private long trados85to94WordCountAmount = 0;
    
    private long trados75to84WordCountAmount = 0;

    private long trados50to74WordCountAmount = 0;

    private long tradosNoMatchWordCountAmount = 0;

    private long tradosRepsWordCountAmount = 0;

    private long tradosTotalWordCountAmount = 0;

    /* word counts */
    private long lowFuzzyMatchWordCount = 0;

    private long medFuzzyMatchWordCount = 0;

    private long medHiFuzzyMatchWordCount = 0;

    private long hiFuzzyMatchWordCount = 0;

    private long totalWordCount = 0;
    
    // workflow cost
    private BigDecimal estimatedCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal estimatedCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal estimatedBillingCharges = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal estimatedBillingChargesAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* word count costs */
    private BigDecimal tmRepetitionWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tradosRepetitionWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal lowFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal medFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal medHiFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal hiFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Tm values */
    private BigDecimal tmInternalRepsWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmInternalRepsWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tmExactMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmInContextMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmExactMatchWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmInContextMatchWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tmFuzzyMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmFuzzyMatchWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tmNewWordsWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmNewWordsWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tmTotalWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tmTotalWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    /* Trados values */
    private BigDecimal trados100WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tradosInContextWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal trados100WordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tradosInContextWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal trados95to99WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal trados95to99WordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal trados85to94WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal trados85to94WordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal trados75to84WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal trados75to84WordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal trados50to74WordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal trados50to74WordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tradosRepsWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tradosRepsWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tradosTotalWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tradosTotalWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    private BigDecimal tradosNoMatchWordCountCost = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);
    
    private BigDecimal tradosNoMatchWordCountCostAmount = new BigDecimal(
            BIG_DECIMAL_ZERO_STRING);

    
    public BigDecimal getEstimatedCost() {
        return this.estimatedCost;
    }
    
    public BigDecimal getEstimatedCostAmount() {
        return this.estimatedCostAmount;
    }

    public void setEstimatedCost(BigDecimal estimatedCost) {
        this.estimatedCost = estimatedCost;
        this.estimatedCostAmount = this.estimatedCostAmount.add(estimatedCost);
    }
    
    public BigDecimal getEstimatedBillingCharges() {
        return this.estimatedBillingCharges;
    }
    
    public BigDecimal getEstimatedBillingChargesAmount() {
        return this.estimatedBillingChargesAmount;
    }

    public void setEstimatedBillingCharges(BigDecimal estimatedBillingCharges) {
        this.estimatedBillingCharges = estimatedBillingCharges;
        this.estimatedBillingChargesAmount = 
            this.estimatedBillingChargesAmount.add(estimatedBillingCharges);
    }
    
//    public long getContextMatchWordCount() {
//        return this.contextMatchWordCount;
//    }
//
//    public void setContextMatchWordCount(long contextMatchWordCount) {
//        this.contextMatchWordCount = contextMatchWordCount;
//    }
//
//    public BigDecimal getContextMatchWordCountCost() {
//        return this.contextMatchWordCountCost;
//    }
//
//    public void setContextMatchWordCountCost(BigDecimal contextMatchWordCountCost) {
//        this.contextMatchWordCountCost = contextMatchWordCountCost;
//    }

    public long getHiFuzzyMatchWordCount() {
        return this.hiFuzzyMatchWordCount;
    }

    public void setHiFuzzyMatchWordCount(long hiFuzzyMatchWordCount) {
        this.hiFuzzyMatchWordCount = hiFuzzyMatchWordCount;
    }

    public BigDecimal getHiFuzzyMatchWordCountCost() {
        return this.hiFuzzyMatchWordCountCost;
    }

    public void setHiFuzzyMatchWordCountCost(BigDecimal hiFuzzyMatchWordCountCost) {
        this.hiFuzzyMatchWordCountCost = hiFuzzyMatchWordCountCost;
    }

    public long getLowFuzzyMatchWordCount() {
        return this.lowFuzzyMatchWordCount;
    }

    public void setLowFuzzyMatchWordCount(long lowFuzzyMatchWordCount) {
        this.lowFuzzyMatchWordCount = lowFuzzyMatchWordCount;
    }

    public BigDecimal getLowFuzzyMatchWordCountCost() {
        return this.lowFuzzyMatchWordCountCost;
    }

    public void setLowFuzzyMatchWordCountCost(BigDecimal lowFuzzyMatchWordCountCost) {
        this.lowFuzzyMatchWordCountCost = lowFuzzyMatchWordCountCost;
    }

    public long getMedFuzzyMatchWordCount() {
        return this.medFuzzyMatchWordCount;
    }

    public void setMedFuzzyMatchWordCount(long medFuzzyMatchWordCount) {
        this.medFuzzyMatchWordCount = medFuzzyMatchWordCount;
    }

    public BigDecimal getMedFuzzyMatchWordCountCost() {
        return this.medFuzzyMatchWordCountCost;
    }

    public void setMedFuzzyMatchWordCountCost(BigDecimal medFuzzyMatchWordCountCost) {
        this.medFuzzyMatchWordCountCost = medFuzzyMatchWordCountCost;
    }

    public long getMedHiFuzzyMatchWordCount() {
        return this.medHiFuzzyMatchWordCount;
    }

    public void setMedHiFuzzyMatchWordCount(long medHiFuzzyMatchWordCount) {
        this.medHiFuzzyMatchWordCount = medHiFuzzyMatchWordCount;
    }

    public BigDecimal getMedHiFuzzyMatchWordCountCost() {
        return this.medHiFuzzyMatchWordCountCost;
    }

    public void setMedHiFuzzyMatchWordCountCost(
            BigDecimal medHiFuzzyMatchWordCountCost) {
        this.medHiFuzzyMatchWordCountCost = medHiFuzzyMatchWordCountCost;
    }

    public BigDecimal getTmRepetitionWordCountCost() {
        return this.tmRepetitionWordCountCost;
    }

    public void setTmRepetitionWordCountCost(BigDecimal tmRepetitionWordCountCost) {
        this.tmRepetitionWordCountCost = tmRepetitionWordCountCost;
    }
    
    public BigDecimal getTradosRepetitionWordCountCost() {
        return this.tradosRepetitionWordCountCost;
    }

    public void setTradosRepetitionWordCountCost(BigDecimal tradosRepetitionWordCountCost) {
        this.tradosRepetitionWordCountCost = tradosRepetitionWordCountCost;
    }

    public long getTmExactMatchWordCount() {
        return this.tmExactMatchWordCount;
    }
    
    public long getTmExactMatchWordCountAmount() {
        return this.tmExactMatchWordCountAmount;
    }

    public void setTmExactMatchWordCount(long tmExactMatchWordCount) {
        this.tmExactMatchWordCount = tmExactMatchWordCount;
        this.tmExactMatchWordCountAmount += tmExactMatchWordCount;
    }

    public BigDecimal getTmExactMatchWordCountCost() {
        return this.tmExactMatchWordCountCost;
    }
    
    public BigDecimal getTmExactMatchWordCountCostAmount() {
        return this.tmExactMatchWordCountCostAmount;
    }

    public void setTmExactMatchWordCountCost(BigDecimal tmExactMatchWordCountCost) {
        this.tmExactMatchWordCountCost = tmExactMatchWordCountCost;
        this.tmExactMatchWordCountCostAmount = 
            this.tmExactMatchWordCountCostAmount.add(tmExactMatchWordCountCost);
    }

    public long getTmFuzzyMatchWordCount() {
        return this.tmFuzzyMatchWordCount;
    }
    
    public long getTmFuzzyMatchWordCountAmount() {
        return this.tmFuzzyMatchWordCountAmount;
    }

    public void setTmFuzzyMatchWordCount(long tmFuzzyMatchWordCount) {
        this.tmFuzzyMatchWordCount = tmFuzzyMatchWordCount;
        this.tmFuzzyMatchWordCountAmount += tmFuzzyMatchWordCount;
    }

    public BigDecimal getTmFuzzyMatchWordCountCost() {
        return this.tmFuzzyMatchWordCountCost;
    }
    
    public BigDecimal getTmFuzzyMatchWordCountCostAmount() {
        return this.tmFuzzyMatchWordCountCostAmount;
    }

    public void setTmFuzzyMatchWordCountCost(BigDecimal tmFuzzyMatchWordCountCost) {
        this.tmFuzzyMatchWordCountCost = tmFuzzyMatchWordCountCost;
        this.tmFuzzyMatchWordCountCostAmount = 
            this.tmFuzzyMatchWordCountCostAmount.add(tmFuzzyMatchWordCountCost);
    }

    public long getTmInternalRepsWordCount() {
        return this.tmInternalRepsWordCount;
    }
    
    public long getTmInternalRepsWordCountAmount() {
        return this.tmInternalRepsWordCountAmount;
    }

    public void setTmInternalRepsWordCount(long tmInternalRepsWordCount) {
        this.tmInternalRepsWordCount = tmInternalRepsWordCount;
        this.tmInternalRepsWordCountAmount += tmInternalRepsWordCount;
    }

    public BigDecimal getTmInternalRepsWordCountCost() {
        return this.tmInternalRepsWordCountCost;
    }
    
    public BigDecimal getTmInternalRepsWordCountCostAmount() {
        return this.tmInternalRepsWordCountCostAmount;
    }

    public void setTmInternalRepsWordCountCost(
            BigDecimal tmInternalRepsWordCountCost) {
        this.tmInternalRepsWordCountCost = tmInternalRepsWordCountCost;
        this.tmInternalRepsWordCountCostAmount = 
            this.tmInternalRepsWordCountCostAmount.add(tmInternalRepsWordCountCost);
    }

    public long getTmNewWordsWordCount() {
        return this.tmNewWordsWordCount;
    }
    
    public long getTmNewWordsWordCountAmount() {
        return this.tmNewWordsWordCountAmount;
    }

    public void setTmNewWordsWordCount(long tmNewWordsWordCount) {
        this.tmNewWordsWordCount = tmNewWordsWordCount;
        this.tmNewWordsWordCountAmount += tmNewWordsWordCount;
    }

    public BigDecimal getTmNewWordsWordCountCost() {
        return this.tmNewWordsWordCountCost;
    }
    
    public BigDecimal getTmNewWordsWordCountCostAmount() {
        return this.tmNewWordsWordCountCostAmount;
    }

    public void setTmNewWordsWordCountCost(BigDecimal tmNewWordsWordCountCost) {
        this.tmNewWordsWordCountCost = tmNewWordsWordCountCost;
        this.tmNewWordsWordCountCostAmount =
            this.tmNewWordsWordCountCostAmount.add(tmNewWordsWordCountCost);
    }

    public long getTmTotalWordCount() {
        return this.tmTotalWordCount;
    }
    
    public long getTmTotalWordCountAmount() {
        return this.tmTotalWordCountAmount;
    }

    public void setTmTotalWordCount(long tmTotalWordCount) {
        this.tmTotalWordCount = tmTotalWordCount;
        this.tmTotalWordCountAmount += tmTotalWordCount;
    }

    public BigDecimal getTmTotalWordCountCost() {
        return this.tmTotalWordCountCost;
    }
    
    public BigDecimal getTmTotalWordCountCostAmount() {
        return this.tmTotalWordCountCostAmount;
    }

    public void setTmTotalWordCountCost(BigDecimal tmTotalWordCountCost) {
        this.tmTotalWordCountCost = tmTotalWordCountCost;
        this.tmTotalWordCountCostAmount = 
            this.tmTotalWordCountCostAmount.add(tmTotalWordCountCost);
    }

    public long getTotalWordCount() {
        return this.totalWordCount;
    }

    public long getTrados100WordCount() {
        return this.trados100WordCount;
    }
    
    public long getTrados100WordCountAmount() {
        return this.trados100WordCountAmount;
    }

    public void setTrados100WordCount(long trados100WordCount) {
        this.trados100WordCount = trados100WordCount;
        this.trados100WordCountAmount += trados100WordCount;
    }

    public BigDecimal getTrados100WordCountCost() {
        return this.trados100WordCountCost;
    }

    public BigDecimal getTrados100WordCountCostAmount() {
        return this.trados100WordCountCostAmount;
    }
    
    public void setTrados100WordCountCost(BigDecimal trados100WordCountCost) {
        this.trados100WordCountCost = trados100WordCountCost;
        this.trados100WordCountCostAmount = 
            this.trados100WordCountCostAmount.add(trados100WordCountCost);
    }

    public long getTrados50to74WordCount() {
        return this.trados50to74WordCount;
    }
    
    public long getTrados50to74WordCountAmount() {
        return this.trados50to74WordCountAmount;
    }

    public void setTrados50to74WordCount(long trados50to74WordCount) {
        this.trados50to74WordCount = trados50to74WordCount;
        this.trados50to74WordCountAmount += trados50to74WordCount;
    }

    public BigDecimal getTrados50to74WordCountCost() {
        return this.trados50to74WordCountCost;
    }
    
    public BigDecimal getTrados50to74WordCountCostAmount() {
        return this.trados50to74WordCountCostAmount;
    }

    public void setTrados50to74WordCountCost(BigDecimal trados50to74WordCountCost) {
        this.trados50to74WordCountCost = trados50to74WordCountCost;
        this.trados50to74WordCountCostAmount = 
            this.trados50to74WordCountCostAmount.add(trados50to74WordCountCost);
    }

    public long getTrados85to94WordCount() {
        return this.trados85to94WordCount;
    }
    
    public long getTrados85to94WordCountAmount() {
        return this.trados85to94WordCountAmount;
    }

    public void setTrados85to94WordCount(long trados85to94WordCount) {
        this.trados85to94WordCount = trados85to94WordCount;
        this.trados85to94WordCountAmount += trados85to94WordCount;
    }
    
    public long getTrados75to84WordCount() {
        return this.trados75to84WordCount;
    }
    
    public long getTrados75to84WordCountAmount() {
        return this.trados75to84WordCountAmount;
    }

    public void setTrados75to84WordCount(long trados75to84WordCount) {
        this.trados75to84WordCount = trados75to84WordCount;
        this.trados75to84WordCountAmount += trados75to84WordCount;
    }
    

    public BigDecimal getTrados85to94WordCountCost() {
        return this.trados85to94WordCountCost;
    }
    
    public BigDecimal getTrados85to94WordCountCostAmount() {
        return this.trados85to94WordCountCostAmount;
    }

    public void setTrados85to94WordCountCost(BigDecimal trados85to94WordCountCost) {
        this.trados85to94WordCountCost = trados85to94WordCountCost;
        this.trados85to94WordCountCostAmount = 
            this.trados85to94WordCountCostAmount.add(trados85to94WordCountCost);
    }
    
    public BigDecimal getTrados75to84WordCountCost() {
        return this.trados75to84WordCountCost;
    }
    
    public BigDecimal getTrados75to84WordCountCostAmount() {
        return this.trados75to84WordCountCostAmount;
    }

    public void setTrados75to84WordCountCost(BigDecimal trados75to84WordCountCost) {
        this.trados75to84WordCountCost = trados75to84WordCountCost;
        this.trados75to84WordCountCostAmount = 
            this.trados75to84WordCountCostAmount.add(trados75to84WordCountCost);
    }

    public long getTrados95to99WordCount() {
        return this.trados95to99WordCount;
    }
    
    public long getTrados95to99WordCountAmount() {
        return this.trados95to99WordCountAmount;
    }

    public void setTrados95to99WordCount(long trados95to99WordCount) {
        this.trados95to99WordCount = trados95to99WordCount;
        this.trados95to99WordCountAmount += trados95to99WordCount;
    }

    public BigDecimal getTrados95to99WordCountCost() {
        return this.trados95to99WordCountCost;
    }
    
    public BigDecimal getTrados95to99WordCountCostAmount() {
        return this.trados95to99WordCountCostAmount;
    }

    public void setTrados95to99WordCountCost(BigDecimal trados95to99WordCountCost) {
        this.trados95to99WordCountCost = trados95to99WordCountCost;
        this.trados95to99WordCountCostAmount = 
            this.trados95to99WordCountCostAmount.add(trados95to99WordCountCost);
    }

    public long getTradosNoMatchWordCount() {
        return this.tradosNoMatchWordCount;
    }
    
    public long getTradosNoMatchWordCountAmount() {
        return this.tradosNoMatchWordCountAmount;
    }

    public void setTradosNoMatchWordCount(long tradosNoMatchWordCount) {
        this.tradosNoMatchWordCount = tradosNoMatchWordCount;
        this.tradosNoMatchWordCountAmount += tradosNoMatchWordCount;
    }

    public BigDecimal getTradosNoMatchWordCountCost() {
        return this.tradosNoMatchWordCountCost;
    }
    
    public BigDecimal getTradosNoMatchWordCountCostAmount() {
        return this.tradosNoMatchWordCountCostAmount;
    }

    public void setTradosNoMatchWordCountCost(BigDecimal tradosNoMatchWordCountCost) {
        this.tradosNoMatchWordCountCost = tradosNoMatchWordCountCost;
        this.tradosNoMatchWordCountCostAmount = 
            this.tradosNoMatchWordCountCostAmount.add(tradosNoMatchWordCountCost);
    }

    public long getTradosRepsWordCount() {
        return this.tradosRepsWordCount;
    }
    
    public long getTradosRepsWordCountAmount() {
        return this.tradosRepsWordCountAmount;
    }

    public void setTradosRepsWordCount(long tradosRepsWordCount) {
        this.tradosRepsWordCount = tradosRepsWordCount;
        this.tradosRepsWordCountAmount += tradosRepsWordCount;
    }

    public BigDecimal getTradosRepsWordCountCost() {
        return this.tradosRepsWordCountCost;
    }
    
    public BigDecimal getTradosRepsWordCountCostAmount() {
        return this.tradosRepsWordCountCostAmount;
    }

    public void setTradosRepsWordCountCost(BigDecimal tradosRepsWordCountCost) {
        this.tradosRepsWordCountCost = tradosRepsWordCountCost;
        this.tradosRepsWordCountCostAmount = 
            this.tradosRepsWordCountCostAmount.add(tradosRepsWordCountCost);
    }

    public long getTradosTotalWordCount() {
        return this.tradosTotalWordCount;
    }
    
    public long getTradosTotalWordCountAmount() {
        return this.tradosTotalWordCountAmount;
    }

    public void setTradosTotalWordCount(long tradosTotalWordCount) {
        this.tradosTotalWordCount = tradosTotalWordCount;
        this.tradosTotalWordCountAmount += tradosTotalWordCount;
    }

    public BigDecimal getTradosTotalWordCountCost() {
        return this.tradosTotalWordCountCost;
    }
    
    public BigDecimal getTradosTotalWordCountCostAmount() {
        return this.tradosTotalWordCountCostAmount;
    }

    public void setTradosTotalWordCountCost(BigDecimal tradosTotalWordCountCost) {
        this.tradosTotalWordCountCost = tradosTotalWordCountCost;
        this.tradosTotalWordCountCostAmount = 
            this.tradosTotalWordCountCostAmount.add(tradosTotalWordCountCost);
    }

    /**
     * Adds the given float to the BigDecimal after scaling it to 3(SCALE) decimal
     * points of precision and rounding half up.
     * If you don't do this, then the float 0.255 will become 0.254999995231628
     * Returns a new BigDecimal which is the sum of a and p_f
     */
    public BigDecimal add(BigDecimal p_a, float p_f) 
    {
        String floatString = Float.toString(p_f);
        BigDecimal bd = new BigDecimal(floatString);
        BigDecimal sbd = bd.setScale(SCALE, BigDecimal.ROUND_HALF_UP);
        return p_a.add(sbd);
    }
    
    public BigDecimal toBigDecimal(float value)
    {
        String floatString = Float.toString(value);
        return new BigDecimal(floatString);
    }

    public long getTmInContextMatchWordCount() {
        return tmInContextMatchWordCount;
    }

    public void setTmInContextMatchWordCount(long tmInContextMatchWordCount) {
        this.tmInContextMatchWordCount = tmInContextMatchWordCount;
        this.tmInContextMatchWordCountAmount += tmInContextMatchWordCount;
    }

    public long getTmInContextMatchWordCountAmount() {
        return tmInContextMatchWordCountAmount;
    }

    
    public long getTradosInContextWordCount() {
        return tradosInContextWordCount;
    }

    public void setTradosInContextWordCount(long tradosInContextWordCount) {
        this.tradosInContextWordCount = tradosInContextWordCount;
        this.tradosInContextWordCountAmount += tradosInContextWordCount;
    }

    public long getTradosInContextWordCountAmount() {
        return tradosInContextWordCountAmount;
    }

    
    public BigDecimal getTmInContextMatchWordCountCost() {
        return tmInContextMatchWordCountCost;
    }

    public void setTmInContextMatchWordCountCost(
            BigDecimal tmInContextMatchWordCountCost) {
        this.tmInContextMatchWordCountCost = tmInContextMatchWordCountCost;
        this.tmInContextMatchWordCountCostAmount = 
            this.tmInContextMatchWordCountCostAmount.add(tmInContextMatchWordCountCost);
    }

    public BigDecimal getTmInContextMatchWordCountCostAmount() {
        return tmInContextMatchWordCountCostAmount;
    }

    
    public BigDecimal getTradosInContextWordCountCost() {
        return tradosInContextWordCountCost;
    }

    public void setTradosInContextWordCountCost(
            BigDecimal tradosInContextWordCountCost) {
        this.tradosInContextWordCountCost = tradosInContextWordCountCost;
        this.tradosInContextWordCountCostAmount = 
            this.tradosInContextWordCountCostAmount.add(tradosInContextWordCountCost);
    }

    public BigDecimal getTradosInContextWordCountCostAmount() {
        return tradosInContextWordCountCostAmount;
    }
}
