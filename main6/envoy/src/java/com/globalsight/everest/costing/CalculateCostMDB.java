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

import java.rmi.RemoteException;
import java.util.Hashtable;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adaptermdb.EventTopicMap;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.jms.GenericQueueMDB;
import com.globalsight.everest.util.jms.JmsHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GeneralException;

/**
 * This MessageDrivenBean is responsible for calculating costs asynchronously
 */
@MessageDriven(messageListenerInterface = MessageListener.class, activationConfig =
{
        @ActivationConfigProperty(propertyName = "destination", propertyValue = EventTopicMap.QUEUE_PREFIX_JBOSS
                + JmsHelper.JMS_CALCULATE_COST_QUEUE),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = JmsHelper.JMS_TYPE_QUEUE) })
@TransactionManagement(value = TransactionManagementType.BEAN)
public class CalculateCostMDB extends GenericQueueMDB
{
    private static final long serialVersionUID = 1L;

    private static final Logger s_logger = Logger
            .getLogger(CalculateCostMDB.class);

    /**
     * Default constructor
     */
    public CalculateCostMDB()
    {
        super(s_logger);
    }

    /**
     * Start the cost calculation process as a separate thread. This method is
     * not a public API and is ONLY invoked by it's consumer for calculating the
     * cost
     * 
     * @param p_message
     *            - The message to be passed.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void onMessage(Message p_message)
    {
        try
        {
            if (s_logger.isDebugEnabled())
            {
                s_logger.debug("received message for calculate cost: "
                        + p_message.toString());
            }

            // get the hashtable that contains the info about cost calculation
            Hashtable<?, ?> map = (Hashtable<?, ?>) ((ObjectMessage) p_message)
                    .getObject();

            CompanyThreadLocal.getInstance().setIdValue(
                    (String) map.get(CompanyWrapper.CURRENT_COMPANY_ID));

            calculateCost(map);
        }
        catch (Exception e)
        {
            s_logger.error("Failed to calculateCost ", e);
        }
        finally
        {
            HibernateUtil.closeSession();
        }
    }

    private void calculateCost(Hashtable<?, ?> p_map) throws Exception
    {
        long jobId = ((Long) p_map.get(new Integer(CostingEngine.JOB)))
                .longValue();
        Currency oCurrency = (Currency) p_map.get(new Integer(
                CostingEngine.CURRENCY));
        boolean reCalculate = ((Boolean) p_map.get(new Integer(
                CostingEngine.RECALCULATE))).booleanValue();
        int costType = ((Integer) p_map
                .get(new Integer(CostingEngine.COST_TYPE))).intValue();
        calculateCost(jobId, oCurrency, reCalculate, costType);
    }

    private void calculateCost(long p_jobId, Currency oCurrency,
            boolean reCalculate, int costType) throws GeneralException,
            EnvoyServletException, RemoteException
    {
        try
        {
            ServerProxy.getCostingEngine().calculateCost(p_jobId, oCurrency,
                    reCalculate, costType);
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
