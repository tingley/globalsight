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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostByWordCount;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;
import com.globalsight.everest.workflowmanager.Workflow;

public class ParamTmMatchesPair extends ParamObjectPair 
{
    public ParamTmMatchesPair(Param p_param)
    {
        super(p_param);
    }
    
    public List getResult(Job p_job, 
                          Workflow p_workflow, 
                          DateFormat p_format, 
                          ProjectWorkflowData workflowData) 
    {
        List result = new ArrayList();
        
        this.countWorkCount(workflowData, p_workflow);
        
        //For word count
        Param wordCountParam = this.getParam().getChildParams()[0];
        if (wordCountParam.getValue())
        {
            Param[] children = wordCountParam.getChildParams();
            
            if (children[0].getValue())
            {
                result.add(new Long(workflowData.getTmInternalRepsWordCount()));
                workflowData.setTmInternalRepsWordCount(0l);
            }
            if (children[1].getValue())
            {
                result.add(new Long(workflowData.getTmExactMatchWordCount()));
                workflowData.setTmExactMatchWordCount(0l);
            }
            if (children[2].getValue())
            {
                result.add(new Long(workflowData.getTmInContextMatchWordCount()));
                workflowData.setTmInContextMatchWordCount(0l);
            }
            if (children[3].getValue())
            {
                result.add(new Long(workflowData.getTmFuzzyMatchWordCount()));
                workflowData.setTmFuzzyMatchWordCount(0l);
            }
            if (children[4].getValue())
            {
                result.add(new Long(workflowData.getTmNewWordsWordCount()));
                workflowData.setTmNewWordsWordCount(0l);
            }
            if (children[5].getValue())
            {
                result.add(new Long(workflowData.getTmTotalWordCount()));
                workflowData.setTmTotalWordCount(0l);
            }
        }
        
        //For word count invoice
        Param invoiceParam = this.getParam().getChildParams()[1];
        if (invoiceParam.getValue())
        {
            this.countWordCost(workflowData, p_workflow);
            
            Param[] children = invoiceParam.getChildParams();
            
            if (children[0].getValue())
            {
                result.add(workflowData.getTmInternalRepsWordCountCost());
                workflowData.setTmInternalRepsWordCountCost(new BigDecimal(0f));
            }
            if (children[1].getValue())
            {
                result.add(workflowData.getTmExactMatchWordCountCost());
                workflowData.setTmExactMatchWordCountCost(new BigDecimal(0f));
            }
            if (children[2].getValue())
            {
                result.add(workflowData.getTmInContextMatchWordCountCost());
                workflowData.setTmInContextMatchWordCountCost(new BigDecimal(0f));
            }
            if (children[3].getValue())
            {
                result.add(workflowData.getTmFuzzyMatchWordCountCost());
                workflowData.setTmFuzzyMatchWordCountCost(new BigDecimal(0f));
            }
            if (children[4].getValue())
            {
                result.add(workflowData.getTmNewWordsWordCountCost());
                workflowData.setTmNewWordsWordCountCost(new BigDecimal(0f));
            }
            if (children[5].getValue())
            {
                result.add(workflowData.getTmTotalWordCountCost());
                workflowData.setTmTotalWordCountCost(new BigDecimal(0f));
            }
        }
        
        return result;
    }
    
    public List getTotal(ProjectWorkflowData workflowData)
    {
        List result = new ArrayList();
        
        //For word count amount
        Param wordCountParam = this.getParam().getChildParams()[0];
        if (wordCountParam.getValue())
        {
            Param[] children = wordCountParam.getChildParams();
            
            if (children[0].getValue())
            {
                result.add(new Long(workflowData.getTmInternalRepsWordCountAmount()));
            }
            if (children[1].getValue())
            {
                result.add(new Long(workflowData.getTmExactMatchWordCountAmount()));
            }
            if (children[2].getValue())
            {
                result.add(new Long(workflowData.getTmInContextMatchWordCountAmount()));
            }
            if (children[3].getValue())
            {
                result.add(new Long(workflowData.getTmFuzzyMatchWordCountAmount()));
            }
            if (children[4].getValue())
            {
                result.add(new Long(workflowData.getTmNewWordsWordCountAmount()));
            }
            if (children[5].getValue())
            {
                result.add(new Long(workflowData.getTmTotalWordCountAmount()));
            }
        }
        
        //For word count invoice amount
        Param invoiceParam = this.getParam().getChildParams()[1];
        if (invoiceParam.getValue())
        {
            Param[] children = invoiceParam.getChildParams();
            
            if (children[0].getValue())
            {
                result.add(workflowData.getTmInternalRepsWordCountCostAmount());
            }
            if (children[1].getValue())
            {
                result.add(workflowData.getTmExactMatchWordCountCostAmount());
            }
            if (children[2].getValue())
            {
                result.add(workflowData.getTmInContextMatchWordCountCostAmount());
            }
            if (children[3].getValue())
            {
                result.add(workflowData.getTmFuzzyMatchWordCountCostAmount());
            }
            if (children[4].getValue())
            {
                result.add(workflowData.getTmNewWordsWordCountCostAmount());
            }
            if (children[5].getValue())
            {
                result.add(workflowData.getTmTotalWordCountCostAmount());
            }
        }
        
        return result;
    }
    
    private void countWorkCount(ProjectWorkflowData p_workflowData, Workflow p_workflow)
    {
        p_workflowData.setTmInternalRepsWordCount(p_workflow
                .getRepetitionWordCount());

        p_workflowData.setLowFuzzyMatchWordCount(p_workflow.getThresholdLowFuzzyWordCount());
        p_workflowData.setMedFuzzyMatchWordCount(p_workflow.getThresholdMedFuzzyWordCount());
        p_workflowData.setMedHiFuzzyMatchWordCount(p_workflow.getThresholdMedHiFuzzyWordCount());
        p_workflowData.setHiFuzzyMatchWordCount(p_workflow.getThresholdHiFuzzyWordCount());
        
        //the Dell fuzzyMatchWordCount is the sum of the top 3 categories
        long tmFuzzyMatchWordCount = 
            p_workflowData.getMedFuzzyMatchWordCount() + 
            p_workflowData.getMedHiFuzzyMatchWordCount() + 
            p_workflowData.getHiFuzzyMatchWordCount();
            p_workflowData.setTmFuzzyMatchWordCount(tmFuzzyMatchWordCount); 
                   
        long tmNewWordsWordCount = 
            p_workflow.getNoMatchWordCount() + 
            p_workflowData.getLowFuzzyMatchWordCount();
        p_workflowData.setTmNewWordsWordCount(tmNewWordsWordCount);

        p_workflowData.setTmExactMatchWordCount((PageHandler.isInContextMatch(p_workflow.getJob())) ? p_workflow.getSegmentTmWordCount() : p_workflow.getTotalExactMatchWordCount());
        p_workflowData.setTmInContextMatchWordCount((PageHandler.isInContextMatch(p_workflow.getJob())) ? p_workflow.getInContextMatchWordCount() : p_workflow.getNoUseInContextMatchWordCount());

        long tmTotalWordCount = 
            p_workflowData.getTmFuzzyMatchWordCount() + 
            p_workflowData.getTmInternalRepsWordCount() +
            p_workflowData.getTmExactMatchWordCount() + 
            p_workflowData.getTmNewWordsWordCount() + 
            p_workflowData.getTmInContextMatchWordCount();
        p_workflowData.setTmTotalWordCount(tmTotalWordCount);
    }
    
    private void countWordCost(ProjectWorkflowData p_workflowData, Workflow p_workflow)
    {
        Cost wfCost = null;
        try {
            CostingEngine costEngine = ServerProxy.getCostingEngine();
            Currency pivotCurrency = CurrencyThreadLocal.getCurrency();
            wfCost = costEngine.calculateCost(p_workflow, pivotCurrency, false, Cost.REVENUE, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        CostByWordCount costByWordCount = wfCost.getCostByWordCount();
        if (costByWordCount != null)
        {
           //repetition costs
           p_workflowData.setTmRepetitionWordCountCost(
                   p_workflowData.toBigDecimal(costByWordCount.getRepetitionCost()));
           p_workflowData.setTmInternalRepsWordCountCost(
                   p_workflowData.getTmRepetitionWordCountCost());
           
           //exact match costs
           p_workflowData.setTmExactMatchWordCountCost(
                   p_workflowData.toBigDecimal(
                           ((PageHandler.isInContextMatch(p_workflow.getJob()))) ? 
                                   costByWordCount.getSegmentTmMatchCost() : costByWordCount.getNoUseExactMatchCost()));
           p_workflowData.setTmInContextMatchWordCountCost(
                   p_workflowData.toBigDecimal(
                           ((PageHandler.isInContextMatch(p_workflow.getJob()))) ? 
                                   costByWordCount.getInContextMatchCost() : costByWordCount.getNoUseInContextMatchCost()));
           
           //fuzzy match costs
           p_workflowData.setLowFuzzyMatchWordCountCost(
                   p_workflowData.toBigDecimal(costByWordCount.getLowFuzzyMatchCost()));
           p_workflowData.setMedFuzzyMatchWordCountCost(
                   p_workflowData.toBigDecimal(costByWordCount.getMedFuzzyMatchCost()));
           p_workflowData.setMedHiFuzzyMatchWordCountCost(
                   p_workflowData.toBigDecimal(costByWordCount.getMedHiFuzzyMatchCost()));
           p_workflowData.setHiFuzzyMatchWordCountCost(
                   p_workflowData.toBigDecimal(costByWordCount.getHiFuzzyMatchCost()));
           
           //fuzzy match cost is the sum of the top three fuzzy match categories
           p_workflowData.setTmFuzzyMatchWordCountCost(
                   p_workflowData.getMedFuzzyMatchWordCountCost().add(
                           p_workflowData.getMedHiFuzzyMatchWordCountCost()).add(
                                   p_workflowData.getHiFuzzyMatchWordCountCost()));
                                  
           //new words, no match costs  
           p_workflowData.setTmNewWordsWordCountCost(
                   p_workflowData.toBigDecimal(costByWordCount.getNoMatchCost()).add(
                           p_workflowData.getLowFuzzyMatchWordCountCost()));
           
           //totals
           p_workflowData.setTmTotalWordCountCost( 
                p_workflowData.getTmInternalRepsWordCountCost()
                .add(p_workflowData.getTmExactMatchWordCountCost())
                .add(p_workflowData.getTmInContextMatchWordCountCost())
                .add(p_workflowData.getTmFuzzyMatchWordCountCost())
                .add(p_workflowData.getTmNewWordsWordCountCost()));
        }  
    }
}
