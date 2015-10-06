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

public class ParamTradosMatchesPair extends ParamObjectPair 
{
    public ParamTradosMatchesPair(Param p_param) 
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
                result.add(new Long(workflowData.getTrados100WordCount()));
                workflowData.setTrados100WordCount(0l);
            }
            if (children[1].getValue())
            {
                result.add(new Long(workflowData.getTrados95to99WordCount()));
                workflowData.setTrados95to99WordCount(0l);
            }
            if (children[2].getValue())
            {
                result.add(new Long(workflowData.getTrados85to94WordCount()));
                workflowData.setTrados85to94WordCount(0l);
            }
            if (children[3].getValue())
            {
                result.add(new Long(workflowData.getTrados75to84WordCount()));
                workflowData.setTrados75to84WordCount(0l);
            }
            if (children[4].getValue())
            {
                result.add(new Long(workflowData.getTradosNoMatchWordCount()));
                workflowData.setTradosNoMatchWordCount(0l);
            }
            if (children[5].getValue())
            {
                result.add(new Long(workflowData.getTradosRepsWordCount()));
                workflowData.setTradosRepsWordCount(0l);
            }
            if (children[6].getValue())
            {
                result.add(new Long(workflowData.getTradosInContextWordCount()));
                workflowData.setTradosInContextWordCount(0l);
            }
            if (children[7].getValue())
            {
                result.add(new Long(workflowData.getTradosTotalWordCount()));
                workflowData.setTradosTotalWordCount(0l);
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
                result.add(workflowData.getTrados100WordCountCost());
                workflowData.setTrados100WordCountCost(new BigDecimal(0f));
            }
            if (children[1].getValue())
            {
                result.add(workflowData.getTrados95to99WordCountCost());
                workflowData.setTrados95to99WordCountCost(new BigDecimal(0f));
            }
            if (children[2].getValue())
            {
                result.add(workflowData.getTrados85to94WordCountCost());
                workflowData.setTrados85to94WordCountCost(new BigDecimal(0f));
            }
            if (children[3].getValue())
            {
                result.add(workflowData.getTrados75to84WordCountCost());
                workflowData.setTrados75to84WordCountCost(new BigDecimal(0f));
            }
            if (children[4].getValue())
            {
                result.add(workflowData.getTradosNoMatchWordCountCost());
                workflowData.setTradosNoMatchWordCountCost(new BigDecimal(0f));
            }
            if (children[5].getValue())
            {
                result.add(workflowData.getTradosRepsWordCountCost());
                workflowData.setTradosRepsWordCountCost(new BigDecimal(0f));
            }
            if (children[6].getValue())
            {
                result.add(workflowData.getTradosInContextWordCountCost());
                workflowData.setTradosInContextWordCountCost(new BigDecimal(0f));
            }
            if (children[7].getValue())
            {
                result.add(workflowData.getTradosTotalWordCountCost());
                workflowData.setTradosTotalWordCountCost(new BigDecimal(0f));
            }
        }
        
        return result;
    }
    
    public List getTotal(ProjectWorkflowData workflowData)
    {
        List result = new ArrayList();
        
        // For word count
        Param wordCountParam = this.getParam().getChildParams()[0];
        if (wordCountParam.getValue())
        {
            Param[] children = wordCountParam.getChildParams();
            
            if (children[0].getValue())
            {
                result.add(new Long(workflowData.getTrados100WordCountAmount()));
            }
            if (children[1].getValue())
            {
                result.add(new Long(workflowData.getTrados95to99WordCountAmount()));
            }
            if (children[2].getValue())
            {
                result.add(new Long(workflowData.getTrados85to94WordCountAmount()));
            }
            if (children[3].getValue())
            {
                result.add(new Long(workflowData.getTrados75to84WordCountAmount()));
            }
            if (children[4].getValue())
            {
                result.add(new Long(workflowData.getTradosNoMatchWordCountAmount()));
            }
            if (children[5].getValue())
            {
                result.add(new Long(workflowData.getTradosRepsWordCountAmount()));
            }
            if (children[6].getValue())
            {
                result.add(new Long(workflowData.getTradosInContextWordCountAmount()));
            }
            if (children[7].getValue())
            {
                result.add(new Long(workflowData.getTradosTotalWordCountAmount()));
            }
        }
        
        //For word count invoice
        Param invoiceParam = this.getParam().getChildParams()[1];
        if (invoiceParam.getValue())
        {
            Param[] children = invoiceParam.getChildParams();
            
            if (children[0].getValue())
            {
                result.add(workflowData.getTrados100WordCountCostAmount());
            }
            if (children[1].getValue())
            {
                result.add(workflowData.getTrados95to99WordCountCostAmount());
            }
            if (children[2].getValue())
            {
                result.add(workflowData.getTrados85to94WordCountCostAmount());
            }
            if (children[3].getValue())
            {
                result.add(workflowData.getTrados75to84WordCountCostAmount());
            }
            if (children[4].getValue())
            {
                result.add(workflowData.getTradosNoMatchWordCountCostAmount());
            }
            if (children[5].getValue())
            {
                result.add(workflowData.getTradosRepsWordCountCostAmount());
            }
            if (children[6].getValue())
            {
                result.add(workflowData.getTradosInContextWordCountCostAmount());
            }
            if (children[7].getValue())
            {
                result.add(workflowData.getTradosTotalWordCountCostAmount());
            }
        }
        
        return result;
    }
    
    private void countWorkCount(ProjectWorkflowData p_workflowData, Workflow p_workflow)
    {
        p_workflowData.setTradosRepsWordCount(p_workflow
                .getRepetitionWordCount());

        p_workflowData.setTrados95to99WordCount(p_workflow.getThresholdHiFuzzyWordCount());
        p_workflowData.setTrados85to94WordCount(p_workflow.getThresholdMedHiFuzzyWordCount());
        p_workflowData.setTrados75to84WordCount(p_workflow.getThresholdMedFuzzyWordCount());
        p_workflowData.setTrados50to74WordCount(p_workflow.getThresholdLowFuzzyWordCount());

        p_workflowData.setTradosNoMatchWordCount(
                p_workflow.getThresholdNoMatchWordCount());

		p_workflowData.setTrados100WordCount((PageHandler
				.isInContextMatch(p_workflow.getJob())) ? p_workflow
				.getSegmentTmWordCount() : p_workflow
				.getTotalExactMatchWordCount());

        p_workflowData.setTradosInContextWordCount((PageHandler
                .isInContextMatch(p_workflow.getJob())) ? p_workflow
                .getInContextMatchWordCount() : p_workflow
                .getNoUseInContextMatchWordCount());

        p_workflowData.setTradosTotalWordCount(
                p_workflowData.getTrados100WordCount() + 
                p_workflowData.getTradosInContextWordCount() + 
                p_workflowData.getTrados95to99WordCount() +
                p_workflowData.getTrados85to94WordCount() + 
                p_workflowData.getTrados75to84WordCount() +
                p_workflowData.getTrados50to74WordCount() +
                p_workflowData.getTradosRepsWordCount() + 
                p_workflowData.getTradosNoMatchWordCount());
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
            p_workflowData.setTradosRepetitionWordCountCost(
                    p_workflowData.toBigDecimal(costByWordCount.getRepetitionCost()));
            p_workflowData.setTradosRepsWordCountCost(p_workflowData.getTradosRepetitionWordCountCost());
           
            
            //exact match costs
            p_workflowData.setTrados100WordCountCost(
                    p_workflowData.toBigDecimal((PageHandler.isInContextMatch(p_workflow.getJob())) ? 
                            costByWordCount.getSegmentTmMatchCost() : costByWordCount.getNoUseExactMatchCost()));
            p_workflowData.setTradosInContextWordCountCost(
                    p_workflowData.toBigDecimal((PageHandler.isInContextMatch(p_workflow.getJob())) ? 
                            costByWordCount.getInContextMatchCost() : costByWordCount.getNoUseInContextMatchCost()));
            
            //fuzzy match costs
            p_workflowData.setTrados95to99WordCountCost(
                    p_workflowData.toBigDecimal(costByWordCount.getHiFuzzyMatchCost()));
            p_workflowData.setTrados85to94WordCountCost(
                    p_workflowData.toBigDecimal(costByWordCount.getMedHiFuzzyMatchCost()));
            p_workflowData.setTrados75to84WordCountCost(
                    p_workflowData.toBigDecimal(costByWordCount.getMedFuzzyMatchCost()));
            p_workflowData.setTrados50to74WordCountCost(
                    p_workflowData.toBigDecimal(costByWordCount.getLowFuzzyMatchCost()));
                                  
            //new words, no match costs  
            p_workflowData.setTradosNoMatchWordCountCost(
                    p_workflowData.toBigDecimal(costByWordCount.getNoMatchCost()));
           
            //totals
            p_workflowData.setTradosTotalWordCountCost( 
                    p_workflowData.getTrados100WordCountCost()
                    .add(p_workflowData.getTradosInContextWordCountCost())
                    .add(p_workflowData.getTrados95to99WordCountCost())
                    .add(p_workflowData.getTrados85to94WordCountCost())
                    .add(p_workflowData.getTrados75to84WordCountCost())
                    .add(p_workflowData.getTrados50to74WordCountCost())
                    .add(p_workflowData.getTradosRepsWordCountCost())
                    .add(p_workflowData.getTradosNoMatchWordCountCost()));
        }  
    }
}
