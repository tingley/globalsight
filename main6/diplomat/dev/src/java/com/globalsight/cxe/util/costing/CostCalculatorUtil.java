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

import java.rmi.RemoteException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.costing.CostingEngine;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * Class {@code CostCalculatorUtil} is used for calculating costs without using
 * JMS.
 * 
 * @since GBS-4400
 */
public class CostCalculatorUtil
{
    static private final Logger logger = Logger.getLogger(CostCalculatorUtil.class);

    /**
     * Processes the cost calculation asynchronously with thread instead of JMS.
     */
    static public void calculateCostWithThread(Map<Integer, Object> data)
    {
        CostCalculatorRunnable runnable = new CostCalculatorRunnable(data);
        Thread t = new MultiCompanySupportedThread(runnable);
        t.start();
    }

    /**
     * Processes the cost calculation synchronously.
     */
    static public void calculateCost(Map<Integer, Object> p_data)
    {
        try
        {
            long jobId = (Long) p_data.get(CostingEngine.JOB);
            Currency oCurrency = (Currency) p_data.get(CostingEngine.CURRENCY);
            boolean reCalculate = (Boolean) p_data.get(CostingEngine.RECALCULATE);
            int costType = (Integer) p_data.get(CostingEngine.COST_TYPE);

            calculateCost(jobId, oCurrency, reCalculate, costType);
        }
        catch (Exception e)
        {
            logger.error("Failed to calculate cost.", e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private static void calculateCost(long p_jobId, Currency oCurrency, boolean reCalculate,
            int costType) throws EnvoyServletException
    {
        try
        {
            ServerProxy.getCostingEngine().calculateCost(p_jobId, oCurrency, reCalculate, costType);
        }
        catch (GeneralException ge)
        {
            throw new EnvoyServletException(ge);
        }
        catch (RemoteException re)
        {
            throw new EnvoyServletException(GeneralException.EX_REMOTE, re);
        }
    }
}
