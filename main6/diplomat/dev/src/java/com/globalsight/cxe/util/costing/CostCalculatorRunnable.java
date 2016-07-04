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
package com.globalsight.cxe.util.costing;

import java.util.Map;

public class CostCalculatorRunnable implements Runnable
{
    private Map<Integer, Object> m_data;

    public CostCalculatorRunnable(Map<Integer, Object> data)
    {
        m_data = data;
    }

    @Override
    public void run()
    {
        CostCalculatorUtil.calculateCost(getData());
    }

    private Map<Integer, Object> getData()
    {
        return m_data;
    }
}
