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

import java.util.HashMap;
import java.util.Map;

import com.globalsight.cxe.util.costing.CostCalculatorUtil;

public class CostCalculator
{
    private long m_jobId = 0;
    private Currency m_currency = null;
    private boolean m_reCalculate = false;
    private int m_costType = 0;

    public CostCalculator(long p_jobId, Currency p_currency, boolean p_reCalculate, int p_costType)
    {
        m_jobId = p_jobId;
        m_currency = p_currency;
        m_costType = p_costType;
        m_reCalculate = p_reCalculate;
    }

    public void sendToCalculateCost()
    {
        Map<Integer, Object> data = new HashMap<Integer, Object>();
        data.put(CostingEngine.JOB, m_jobId);
        data.put(CostingEngine.CURRENCY, m_currency);
        data.put(CostingEngine.RECALCULATE, m_reCalculate);
        data.put(CostingEngine.COST_TYPE, m_costType);
        // GBS-4400
        CostCalculatorUtil.calculateCostWithThread(data);
    }
}
