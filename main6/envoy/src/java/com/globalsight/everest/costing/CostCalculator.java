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

// java
import java.io.Serializable;
import java.util.Hashtable;

import javax.jms.JMSException;
import javax.naming.NamingException;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.util.jms.JmsHelper;

public class CostCalculator
    implements Serializable
{
    //////////////////////////////////////
    // Constants                        //
    //////////////////////////////////////
    private long m_jobId = 0;
    private Currency m_currency = null;
    private boolean m_reCalculate = false;
    private int m_costType = 0;


    //////////////////////////////////////
    // Constructors                     //
    //////////////////////////////////////

    public CostCalculator(long p_jobId,
                          Currency p_currency, 
                          boolean p_reCalculate, 
                          int p_costType)
    {
        m_jobId = p_jobId;
        m_currency = p_currency;
        m_costType = p_costType;
        m_reCalculate = p_reCalculate;
    }

    /**
     * Sends a JMS message to the calculator to calculate the cost
     * 
     * @exception JMSException
     * @exception NamingException
     */
    public void sendToCalculateCost()
    throws JMSException, NamingException
    {
        Hashtable map = new Hashtable(3);   
        CompanyWrapper.saveCurrentCompanyIdInMap(map, null);
        map.put(new Integer(CostingEngine.JOB), new Long(m_jobId));
        map.put(new Integer(CostingEngine.CURRENCY), m_currency);
        map.put(new Integer(CostingEngine.RECALCULATE), new Boolean(m_reCalculate));
        map.put(new Integer(CostingEngine.COST_TYPE), new Integer(m_costType));
        JmsHelper.sendMessageToQueue(map,JmsHelper.JMS_CALCULATE_COST_QUEUE);
    }
}

