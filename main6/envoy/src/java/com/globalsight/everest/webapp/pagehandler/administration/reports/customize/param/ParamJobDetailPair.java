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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;

public class ParamJobDetailPair extends ParamObjectPair
{
    public ParamJobDetailPair(Param p_param)
    {
        super(p_param);
    }

    public List getResult(Job p_job, Workflow p_workflow, DateFormat p_format,
            ProjectWorkflowData workflowData)
    {
        List result = new ArrayList();

        this.countWorkCost(workflowData, p_workflow);

        L10nProfile l10nProfile = p_job.getL10nProfile();
        String fProfileNames = p_job.getFProfileNames();
        Project project = l10nProfile.getProject();

        Param[] children = this.getParam().getChildParams();
        if (children[0].getValue())
        {
            result.add(p_job.getJobName());
        }
        if (children[1].getValue())
        {
            String description = project.getDescription();
            result.add((description == null) ? "N/A" : description);
        }
        if (children[2].getValue())
        {
            result.add(p_format.format(p_job.getCreateDate()));
        }
        if (children[3].getValue())
        {
            result.add(p_job.getPageCount());
        }
        if (children[4].getValue())
        {
            result.add(p_job.getPriority());
        }
        if (children[5].getValue())
        {
            result.add(project.getName());
        }
        if (children[6].getValue())
        {
            result.add(UserUtil.getUserNameById(project.getProjectManagerId()));
        }
        if (children[7].getValue())
        {
            result.add(p_job.getSourceLocale().getDisplayName());
        }
        if (children[8].getValue())
        {
            result.add(p_workflow.getTargetLocale().getDisplayName());
        }
        if (children[9].getValue())
        {
            result.add(workflowData.getEstimatedCost());
        }
        if (children[10].getValue())
        {
            result.add(workflowData.getEstimatedBillingCharges());
        }

        if (children[11].getValue())
        {
            result.add(l10nProfile.getName());
        }
        if (children[12].getValue())
        {
             result.add(fProfileNames);
        }

        return result;
    }

    public List getTotal(ProjectWorkflowData workflowData)
    {
        List result = new ArrayList();

        String noneTotal = "N/A";

        Param[] children = this.getParam().getChildParams();
        if (children[0].getValue())
        {
            result.add(noneTotal);
        }
        if (children[1].getValue())
        {
            result.add(noneTotal);
        }
        if (children[2].getValue())
        {
            result.add(noneTotal);
        }
        if (children[3].getValue())
        {
            result.add(noneTotal);
        }
        if (children[4].getValue())
        {
            result.add(noneTotal);
        }
        if (children[5].getValue())
        {
            result.add(noneTotal);
        }
        if (children[6].getValue())
        {
            result.add(noneTotal);
        }
        if (children[7].getValue())
        {
            result.add(noneTotal);
        }
        if (children[8].getValue())
        {
            result.add(noneTotal);
        }
        if (children[9].getValue())
        {
            result.add(workflowData.getEstimatedCostAmount());
        }
        if (children[10].getValue())
        {
            result.add(workflowData.getEstimatedBillingChargesAmount());
        }

        if (children[11].getValue())
        {
            result.add(noneTotal);
        }
        if (children[12].getValue())
        {
            result.add(noneTotal);
        }

        return result;
    }

    private void countWorkCost(ProjectWorkflowData p_workflowData,
            Workflow p_workflow)
    {
        try
        {
            CostingEngine costEngine = ServerProxy.getCostingEngine();

            p_workflowData.setEstimatedCost(p_workflowData
                    .toBigDecimal(costEngine
                            .calculateCost(p_workflow,
                                    CurrencyThreadLocal.getCurrency(), true,
                                    Cost.EXPENSE).getEstimatedCost()
                            .getAmount()));

            p_workflowData.setEstimatedBillingCharges(p_workflowData
                    .toBigDecimal(costEngine
                            .calculateCost(p_workflow,
                                    CurrencyThreadLocal.getCurrency(), true,
                                    Cost.REVENUE).getEstimatedCost()
                            .getAmount()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
