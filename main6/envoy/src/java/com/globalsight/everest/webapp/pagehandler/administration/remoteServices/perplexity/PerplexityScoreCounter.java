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
package com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.tuv.TuvPerplexity;
import com.globalsight.everest.webapp.pagehandler.administration.remoteServices.perplexity.vo.PerplexityScores;

/**
 * <code>PerplexityScoreCounter</code> can help us with counting perplexity.
 * <p>
 * For GBS-4495 perplexity score on MT.
 */
public class PerplexityScoreCounter
{
    private PerplexityService ps;
    private String key;
    private double sourceThreshold;
    private double targetThreshold;

    private String accessToken = null;
    private PerplexityScoreHelper helper = null;

    /**
     * Init PerplexityScoreCounter with WorkflowTemplateInfo. You should call
     * the method first before using.
     * 
     * @param workflowTemplateInfo
     */
    public void init(WorkflowTemplateInfo workflowTemplateInfo)
    {
        ps = workflowTemplateInfo.getPerplexityService();
        key = workflowTemplateInfo.getPerplexityKey();

        sourceThreshold = workflowTemplateInfo.getPerplexitySourceThreshold();
        targetThreshold = workflowTemplateInfo.getPerplexityTargetThreshold();

        helper = new PerplexityScoreHelper();
        accessToken = helper.getToken(ps.getUrl(), ps.getUserName(), ps.getPassword());
    }

    /**
     * Score the source tuv and target tuv. And then save the result to target
     * tuv. 
     * <p>
     * For GBS-4495 perplexity score on MT.
     * 
     * @param src
     * @param trg
     */
    public void score(Tuv src, TuvImpl trg)
    {
        if (accessToken == null)
            return;

        List<String> srcs = new ArrayList<>();
        List<String> trgs = new ArrayList<>();

        srcs.add(src.getGxmlElement().getTextValue());
        trgs.add(trg.getGxmlElement().getTextValue());

        PerplexityScores result = helper.getPerplexityScore(accessToken, ps.getUrl(),
                Long.parseLong(key), srcs, trgs);
        if (result != null)
        {
            List<Double> ss = result.getSources();
            List<Double> ts = result.getTargets();
            
            TuvPerplexity perplexity = new TuvPerplexity();
            perplexity.setPerplexitySource(ss.get(0));
            perplexity.setPerplexityTarget(ts.get(0));
            perplexity.setPerplexityResult(ss.get(0) <= sourceThreshold && ts.get(0) <= targetThreshold);
            perplexity.setCompanyId(CompanyWrapper.getCurrentCompanyIdAsLong());
            
            trg.setPerplexity(perplexity);
        }
    }
}
