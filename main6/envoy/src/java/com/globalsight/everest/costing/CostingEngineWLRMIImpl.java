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

// globalsight imports
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.config.SystemParameterEntityException;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.util.system.SystemStartupException;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This class represents the remote implementation of a CostingEngine that
 * manages the objects needed for costing and calculates the cost of various
 * objects in the system (i.e. Job, Workflow). Note that all of the methods of
 * this class throw the following exceptions: 1. CostingException - For costing
 * related errors. 2. RemoteException - For network related exception.
 */
public class CostingEngineWLRMIImpl extends RemoteServer implements
        CostingEngineWLRemote
{

    // for logging purposes
    private static final Logger c_logger = Logger
            .getLogger(CostingEngineWLRMIImpl.class.getName());

    // passes all calls off to the local instance (serves as a proxy)
    private CostingEngineLocal m_localInstance = null;

    /**
     * Construct a remote Costing Engine.
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     */
    public CostingEngineWLRMIImpl() throws RemoteException, CostingException
    {
        super(CostingEngine.SERVICE_NAME);
        m_localInstance = new CostingEngineLocal();
    }

    /**
     * Initialize the server
     * 
     * @throws SystemStartupException
     *             when a NamingException or other Exception occurs.
     */
    public void init() throws SystemStartupException
    {
        // bind the server
        super.init();

        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            boolean costingEnabled = sc
                    .getBooleanParameter(SystemConfigParamNames.COSTING_ENABLED);

            if (costingEnabled)
            {
                // load the currencies and set the pivot currency
                m_localInstance.loadCurrencies();
                Map pivotCurrencies = Currency.getPivotCurrencies();
                if (pivotCurrencies != null && pivotCurrencies.size() > 0)
                {
                    c_logger.info("Costing is enabled with the pivot currencies");
                }
                else
                {
                    String errorMessage = new String(
                            "The pivot currency failed to be set, the costing engine failed to start.");
                    c_logger.error(errorMessage);
                    throw new SystemStartupException(
                            SystemStartupException.COMP_COSTING, errorMessage);
                }
            }

            // register as a listener to any changes to a system parameter
            ServerProxy.getSystemParameterPersistenceManager()
                    .registerListener(this,
                            SystemConfigParamNames.PIVOT_CURRENCY);
        }
        catch (CostingException ce)
        {
            c_logger.error("CostingException when loading currencies.", ce);
            throw new SystemStartupException(
                    SystemStartupException.COMP_COSTING, ce);
        }
        catch (Exception e)
        {
            c_logger.error("An exception was thrown when trying "
                    + "to read the system configuration file "
                    + "for costing parameters.  Costing is disabled.", e);
            throw new SystemStartupException(
                    SystemStartupException.COMP_COSTING, e);
        }
    }

    /**
     * @see CostingEngine.getIsoCurrencies
     */
    public Vector getIsoCurrencies() throws RemoteException, CostingException
    {
        return m_localInstance.getIsoCurrencies();
    }

    /**
     * @see CostingEngine.getIsoCurrenciesWithNoConversion
     */
    public Vector getIsoCurrenciesWithoutConversion() throws RemoteException,
            CostingException
    {
        return m_localInstance.getIsoCurrenciesWithoutConversion();
    }

    /**
     * @see CostingEngine.getIsoCurrency(String)
     */
    public IsoCurrency getIsoCurrency(String p_isoCode) throws RemoteException,
            CostingException
    {
        return m_localInstance.getIsoCurrency(p_isoCode);
    }

    /**
     * @see CostingEngine.addOrModifyCurrency(Currency)
     */
    public Currency addOrModifyCurrency(Currency p_currency)
            throws RemoteException, CostingException
    {
        return m_localInstance.addOrModifyCurrency(p_currency);
    }

    /**
     * @see CostingEngine.getCurrencies
     */
    public Collection getCurrencies() throws RemoteException, CostingException
    {
        return m_localInstance.getCurrencies();
    }

    /**
     * @see CostingEngine.getCurrency(String)
     */
    public Currency getCurrency(String p_isoCode, long p_companyId)
            throws RemoteException, CostingException
    {
        return m_localInstance.getCurrency(p_isoCode, p_companyId);
    }

    /**
     * @see CostingEngine.getCurrency(String)
     */
    public Currency getCurrency(String p_isoCodes) throws RemoteException,
            CostingException
    {
        return m_localInstance.getCurrency(p_isoCodes);
    }

    /**
     * @see CostingEngine.getPivotCurrency
     */
    public Currency getPivotCurrency() throws RemoteException, CostingException
    {
        return m_localInstance.getPivotCurrency();
    }

    /**
     * @see CostingEngine.changePivotCurrency(Currency)
     */
    public void changePivotCurrency(Currency p_newPivotCurrency)
            throws RemoteException, CostingException
    {
        m_localInstance.changePivotCurrency(p_newPivotCurrency);
    }

    /**
     * Implementation of the SystemParameterChangeListener.listen(String,
     * String) method. Passes on to the local instance to handle any processing
     * depending on the system parameter that was changed.
     */
    public void listen(String p_systemParameterName,
            String p_newSystemParameterValue) throws RemoteException,
            SystemParameterEntityException
    {
        m_localInstance
                .listen(p_systemParameterName, p_newSystemParameterValue);
    }

    /**
     * @see CostingEngine.addRate(Rate)
     */
    public Rate addRate(Rate p_rate) throws RemoteException, CostingException
    {
        return m_localInstance.addRate(p_rate);
    }

    /**
     * @see CostingEngine.modifyRate(Rate)
     */
    public void modifyRate(Rate p_rate) throws RemoteException,
            CostingException
    {
        m_localInstance.modifyRate(p_rate);
    }

    /**
     * @see CostingEngine.deleteRatesOnRole(Role)
     */
    public void deleteRatesOnRole(Role p_role) throws CostingException,
            RemoteException
    {
        m_localInstance.deleteRatesOnRole(p_role);
    }

    /**
     * @see CostingEngine.getRates
     */
    public Collection getRates() throws RemoteException, CostingException
    {
        return m_localInstance.getRates();
    }

    /**
     * @see CostingEngine.getRate(long)
     */
    public Rate getRate(long p_id) throws RemoteException, CostingException
    {
        return m_localInstance.getRate(p_id);
    }

    /**
     * @see CostingEngine.getRates(Activity, GlobalSightLocale,
     *      GlobalSightLocale)
     */
    public Collection getRates(Activity p_activity,
            GlobalSightLocale p_sourceLocale, GlobalSightLocale p_targetLocale)
            throws RemoteException, CostingException
    {
        return m_localInstance.getRates(p_activity, p_sourceLocale,
                p_targetLocale);
    }

    /**
     * @see CostingEngine.getRates(GlobalSightLocale, GlobalSightLocale)
     */
    public Hashtable getRates(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale) throws RemoteException,
            CostingException
    {
        return m_localInstance.getRates(p_sourceLocale, p_targetLocale);
    }

    /**
     * @see CostingEngine.getRates(GlobalSightLocale)
     */
    public Hashtable getRates(GlobalSightLocale p_sourceLocale)
            throws RemoteException, CostingException
    {
        return m_localInstance.getRates(p_sourceLocale);
    }

    /**
     * @see CostingEngine.overrideCost(WorkObject, float, int)
     */
    public Cost overrideCost(WorkObject p_obj, float p_overrideCost,
            int p_costType) throws RemoteException, CostingException
    {
        return m_localInstance.overrideCost(p_obj, p_overrideCost, p_costType);
    }

    /**
     * @see CostingEngine.clearOverrideCost(WorkObject, int )
     */
    public Cost clearOverrideCost(WorkObject p_obj, int p_costType)
            throws RemoteException, CostingException
    {
        return m_localInstance.clearOverrideCost(p_obj, p_costType);
    }

    /**
     * @see CostingEngine.addSurcharge(long, Surcharge, int )
     */
    public Cost addSurcharge(long p_costId, Surcharge p_newSurcharge,
            int p_costType) throws RemoteException, CostingException
    {
        return m_localInstance.addSurcharge(p_costId, p_newSurcharge,
                p_costType);
    }

    /**
     * @see CostingEngine.modifySurcharge(long, String, Surcharge, int )
     */
    public Cost modifySurcharge(long p_costId, String p_surchargeOldName,
            Surcharge p_modifiedSurcharge, int p_costType)
            throws RemoteException, CostingException
    {
        return m_localInstance.modifySurcharge(p_costId, p_surchargeOldName,
                p_modifiedSurcharge, p_costType);
    }

    /**
     * @see CostingEngine.removeSurcharge(long, String, int )
     */
    public Cost removeSurcharge(long p_costId, String p_surchargeName,
            int p_costType) throws RemoteException, CostingException
    {
        return m_localInstance.removeSurcharge(p_costId, p_surchargeName,
                p_costType);
    }

    /**
     * @see CostingEngine.setEstimatedAmountOfWorkInJob(long, int, float)
     */
    public void setEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException
    {
        m_localInstance.setEstimatedAmountOfWorkInJob(p_jobId, p_unitOfWork,
                p_amount);
    }

    /**
     * @see CostingEngine.getEstimatedAmountOfWorkInJob(long, int, int)
     */
    public float getEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            int p_costType) throws RemoteException, CostingException
    {
        return m_localInstance.getEstimatedAmountOfWorkInJob(p_jobId,
                p_unitOfWork, p_costType);
    }

    /**
     * @see CostingEngine.setEstimatedAmountOfWork(long, int, float)
     */
    public void setEstimatedAmountOfWork(long p_taskId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException
    {
        m_localInstance.setEstimatedAmountOfWork(p_taskId, p_unitOfWork,
                p_amount);
    }

    /**
     * @see CostingEngine.addActualAmountOfWork(long, int, float)
     */
    public void setActualAmountOfWork(long p_taskId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException
    {
        m_localInstance.setActualAmountOfWork(p_taskId, p_unitOfWork, p_amount);
    }

    /**
     * @see CostingEngine.calculateCost(long, Currency, boolean, int)
     */
    public void calculateCost(long p_jobId, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        m_localInstance.calculateCost(p_jobId, p_currency, p_recalculate,
                p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Job, Currency, boolean, int)
     */
    public Cost calculateCost(Job p_job, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        return m_localInstance.calculateCost(p_job, p_currency, p_recalculate,
                p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Job, Currency, int)
     */
    public Cost reCostJob(Job p_job, Currency p_currency, int p_costType)
            throws RemoteException, CostingException
    {
        return m_localInstance.reCostJob(p_job, p_currency, p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Job, boolean, int)
     */
    public Cost calculateCost(Job p_job, boolean p_recalculate, int p_costType)
            throws RemoteException, CostingException
    {
        return m_localInstance.calculateCost(p_job, p_recalculate, p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Workflow, Currency, boolean, int)
     */
    public Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        return m_localInstance.calculateCost(p_workflow, p_currency,
                p_recalculate, p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Workflow, boolean, int)
     */
    public Cost calculateCost(Workflow p_workflow, boolean p_recalculate,
            int p_costType) throws RemoteException, CostingException
    {
        return m_localInstance.calculateCost(p_workflow, p_recalculate,
                p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Task, Currency, boolean, int)
     */
    public Cost calculateCost(Task p_task, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException
    {
        return m_localInstance.calculateCost(p_task, p_currency, p_recalculate,
                p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Task, boolean, int)
     */
    public Cost calculateCost(Task p_task, boolean p_recalculate, int p_costType)
            throws RemoteException, CostingException
    {
        return m_localInstance.calculateCost(p_task, p_recalculate, p_costType);
    }

    /**
     * @see CostingEngine.calculateCost(Workflow, Currency, boolean, int,
     *      boolean)
     */
    public Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, int p_costType,
            boolean p_recalcFinishedWorkflow) throws RemoteException,
            CostingException
    {
        return m_localInstance.calculateCost(p_workflow, p_currency,
                p_recalculate, p_costType, p_recalcFinishedWorkflow);
    }

    /**
     * @see CostingEngine.loadCurrencies
     */
    public void loadCurrencies() throws RemoteException, CostingException
    {
        m_localInstance.loadCurrencies();
    }

    public Currency addOrModifyPivotCurrency(String p_companyId)
            throws RemoteException, CostingException, GeneralException
    {
        return m_localInstance.addOrModifyPivotCurrency(p_companyId);
    }

    public float getFactor(Currency currency, String targetName,
            String companyId) throws RemoteException, CostingException,
            GeneralException
    {
        return m_localInstance.getFactor(currency, targetName, companyId);
    }

    public Currency getCurrencyByName(String targetName, String companyId)
            throws RemoteException, CostingException, GeneralException
    {
        return m_localInstance.getCurrencyByName(targetName, companyId);
    }

    public void removeRate(long p_id) throws GeneralException
    {
        m_localInstance.removeRate(p_id);
    }
}
