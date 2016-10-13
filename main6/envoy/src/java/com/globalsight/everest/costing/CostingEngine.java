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

// globalsight
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import com.globalsight.config.SystemParameterChangeListener;
import com.globalsight.everest.foundation.Role;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

/**
 * This interface is used for creating and modifying objects needed for job
 * costing. It also defines the methods needed for calculating the cost of
 * various objects within System4. These objects include TargetPages, Tasks,
 * Workflows and Jobs. The cost can be calculated in the currency specified or
 * in the pivot currency if not specified.
 * 
 * It extends the SystemParameterChangeListener since there are system
 * parameters that the costing engine needs to know about if they change in
 * values.
 */
public interface CostingEngine extends SystemParameterChangeListener
{

    public static final String SERVICE_NAME = "CostingEngine";

    /**
     * Integer constant representing JOB
     */
    public static final int JOB = 1;

    /**
     * Integer constant representing CURRENCY
     */
    public static final int CURRENCY = 2;

    /**
     * Integer constant representing RECALCULATE
     */
    public static final int RECALCULATE = 3;

    /**
     * Integer constant representing COST_TYPE
     */
    public static final int COST_TYPE = 4;

    /**
     * Return a Vector of all the iso currency codes.
     * <p>
     * 
     * @return A vector of the currency codes supported by System4.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Vector getIsoCurrencies() throws RemoteException, CostingException;

    /**
     * Return a Vector of all the iso currency codes that currently don't have a
     * conversion factor set up.
     * <p>
     * 
     * @return A vector of the currency codes supported by System4.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Vector getIsoCurrenciesWithoutConversion() throws RemoteException,
            CostingException;

    /**
     * Return the specified iso currency by specifying the code.
     */
    IsoCurrency getIsoCurrency(String p_isoCode) throws RemoteException,
            CostingException;

    /**
     * Add or modify a currency. Adds the new currency or replaces the existing
     * one.
     * <p>
     * 
     * @param p_currency
     *            The currency to add or modify.
     * @return The currency that was just added - with the id filled in.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Currency addOrModifyCurrency(Currency p_currency) throws RemoteException,
            CostingException;

    /**
     * Retrieve all the currencies in the system.
     * <p>
     * 
     * @return All the currencies in the system. Could be an empty collection
     *         (but not null).
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Collection getCurrencies() throws RemoteException, CostingException;

    /**
     * Retrieve the currency with the specified iso code.
     * <p>
     * 
     * @return The currency that is associated with the iso code or NULL if
     *         there is none.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Currency getCurrency(String p_isoCode, long p_companyId)
            throws RemoteException, CostingException;

    Currency getCurrency(String p_isoCodes) throws RemoteException,
            CostingException;

    /**
     * Retrieve the pivot (i.e. default) currency. All other currencies
     * conversion factors are based on this currency.
     * <p>
     * 
     * @return The default currency.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Currency getPivotCurrency() throws RemoteException, CostingException;

    /**
     * Changes the pivot (i.e. default) currency to a new currency. It also
     * recalculates all the conversion factors for all the other currencies so
     * they are reflected off the new pivot.
     * 
     * @param p_newPivotCurrency
     *            The currency that will be the new pivot.
     */
    void changePivotCurrency(Currency p_newPivotCurrency)
            throws RemoteException, CostingException;

    /**
     * Adds a new rate to a certain role.
     * <p>
     * 
     * @param p_rate
     *            The rate to add - the rate contains the activity and locale
     *            pair it should be associated with.
     * @return Returns the rate that has been added (with id set).
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Rate addRate(Rate p_rate) throws RemoteException, CostingException;

    /**
     * Modify the rate (name, description or rates per word count)
     * <p>
     * 
     * @param p_rate
     *            The rate to modify.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    void modifyRate(Rate p_rate) throws RemoteException, CostingException;

    /**
     * Delete the rate(s) associated with the specific role. This really just
     * deactivates them so they can still be referenced for historical purposes.
     * They are NOT removed from the role itself, but just deactivated within
     * the rate table. Existing jobs can still pick them up.
     * <p>
     * 
     * @param p_role
     *            The role that the rate(s) to be removed are referenced by.
     * @exception CostingException
     *                - An error occurred when removing the rate(s).
     *                RemoteException - a network exception occurred.
     */
    void deleteRatesOnRole(Role p_role) throws CostingException,
            RemoteException;

    /**
     * Returns all the rates in the system.
     * <p>
     * 
     * @return A collection of all the rates in the system. This could be an
     *         empty collection, but not null.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Collection getRates() throws RemoteException, CostingException;

    /**
     * Retrieves the specified rate.
     * <p>
     * 
     * @param p_id
     *            The id of the rate to find.
     * @return The rate or NULL if it couldn't be found.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Rate getRate(long p_id) throws RemoteException, CostingException;

    /**
     * Retrieves all the rates associated with the specific activity and
     * locales.
     * <p>
     * 
     * @param p_activity
     *            The activity that the role is associated with.
     * @param p_sourceLocale
     *            The source locale the role is associated with.
     * @param p_targetLocale
     *            The target locale the role is associated with.
     * @return A collection of rates.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Collection getRates(Activity p_activity, GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale) throws RemoteException,
            CostingException;

    /**
     * Returns the rates that are associated with the specific locales.
     * <p>
     * 
     * @param p_sourceLocale
     *            The source locale the role is associated with.
     * @param p_targetLocale
     *            The target locale the role is associated with.
     * @return A hashtable where the key is the activity and the value is a
     *         vector of the Rates associated with that activity.
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Hashtable getRates(GlobalSightLocale p_sourceLocale,
            GlobalSightLocale p_targetLocale) throws RemoteException,
            CostingException;

    /**
     * Returns all the rates that are associated with roles of a specific source
     * locale.
     * <p>
     * 
     * @param p_sourceLocale
     *            The source locale the role the rate is associated with.
     * @return A hashtable of hashtables. The external hashtable's key is the
     *         target locale id and then a hashtable which contains as a key the
     *         Activity and the values are the rates associated with it. Ex. 32
     *         - Translate --> rate1, rate2 - Review --> rate3, rate4 35 -
     *         Translate --> rate 6 - Review --> rate 7
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Hashtable getRates(GlobalSightLocale p_sourceLocale)
            throws RemoteException, CostingException;

    /**
     * Override and persist the cost for the object specified.
     * 
     * @param p_obj
     *            The costable object that the cost is being overriden on. (Job,
     *            Workflow, Task)
     * @param p_overrideCost
     *            The cost to override with. Assumes it is in the same currency
     *            that the actual and estimates are currently in.
     * 
     * @return Returns the newly updated cost (with the override).
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost overrideCost(WorkObject p_obj, float p_overrideCost, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Clear the override cost.
     * 
     * @param p_obj
     *            The costable object that the overriden cost should be cleared
     *            from.
     * 
     * @return Returns the newly updated cost (without the override).
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost clearOverrideCost(WorkObject p_obj, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Add the specified surcharge to the cost and persist.
     * 
     * @param p_costId
     *            The id of the cost to add the surcharge to.
     * @param p_newSurcharge
     *            The surcharge to add.
     * 
     * @return Returns the newly updated cost (with the surcharge added).
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost addSurcharge(long p_costId, Surcharge p_newSurcharge, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Modify the specified surcharge of the cost and persist.
     * 
     * @param p_costId
     *            The id of the cost object that the surcharge is associated
     *            with.
     * @param p_surchargeOldName
     *            The name of the surcharge before modification. The name may be
     *            the same, however the old one is needed to locate the
     *            surcharge just in case the name was modified.
     * @param p_modifiedSurcharge
     *            The surcharge to modify.
     * 
     * @return Returns the newly updated cost (with the modified surcharge).
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost modifySurcharge(long p_costId, String p_oldSurchareName,
            Surcharge p_modifiedSurcharge, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Remove the specified surcharge and persists the change.
     * 
     * @param p_costId
     *            The id of the cost object, to remove the surcharge from.
     * @param p_surchargeName
     *            The name of the surcharge to remove.
     * 
     * @return Returns the newly updated cost (with the surcharge removed).
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost removeSurcharge(long p_costId, String p_surchargeName, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Add/reset the estimated amount of work in EVERY task in the job that has
     * a rate with the unit of work specified.
     * 
     * @param p_jobId
     *            The id of the job to add the AmountOfWork to.
     * @param p_unitOfWork
     *            The unit of work the amount is in.
     * @param p_amount
     *            The amount of estimated work.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    void setEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException;

    /**
     * Retrieve the average estimated amount of work in the job. Goes through
     * all tasks with page-based rates - totals them up and averages them.
     * 
     * @param p_jobId
     *            The id of the job to get the AmountOfWork from.
     * @param p_unitOfWork
     *            The unit of work the amount is in.
     * @param p_costType
     *            Is a Revenue or Expense type.
     * 
     * @return Returns the estimated amount. If there were no tasks in the job
     *         with that unit of work will return '-1'.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    float getEstimatedAmountOfWorkInJob(long p_jobId, int p_unitOfWork,
            int p_costType) throws RemoteException, CostingException;

    /**
     * Add/reset the estimated amount of work to the task.
     * 
     * @param p_taskId
     *            The id of the task to add the AmountOfWork to.
     * @param p_unitOfWork
     *            The unit of work the amount is in.
     * @param p_amount
     *            The amount of estimated work.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    void setEstimatedAmountOfWork(long p_taskId, int p_unitOfWork,
            float p_amount) throws RemoteException, CostingException;

    /**
     * Add/reset the actual amount of work to the task. The AmountOfWork must
     * exist with an estimate set.
     * 
     * @param p_taskId
     *            The id of the task to add the actual amount to.
     * @param p_unitOfWork
     *            The unit of work the amount is in.
     * @param p_amount
     *            The amount of actual work.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    void setActualAmountOfWork(long p_taskId, int p_unitOfWork, float p_amount)
            throws RemoteException, CostingException;

    /**
     * Calculates the cost of a job in the currency specified.
     * 
     * @param p_jobId
     *            The id of job to calculate the cost for.
     * @param p_currency
     *            The currency to calculate the cost in.
     * @param p_recalculate
     *            'false' - if the cost was calculating earlier then just use
     *            it. 'true' - recalculate the cost no matter what and persist
     *            any changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return void
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    void calculateCost(long p_jobId, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException;

    /**
     * Calculates the cost of a job in the currency specified.
     * 
     * @param p_job
     *            The job to calculate the cost for.
     * @param p_currency
     *            The currency to calculate the cost in.
     * @param p_recalculate
     *            'false' - if the cost was calculating earlier then just use
     *            it. 'true' - recalculate the cost no matter what and persist
     *            any changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return Returns the cost object for the job.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost calculateCost(Job p_job, Currency p_currency, boolean p_recalculate,
            int p_costType) throws RemoteException, CostingException;

    /**
     * Calculates the cost of a job in the pivot currency.
     * 
     * @param p_job
     *            The job to calculate the cost for.
     * @param p_recalculate
     *            'false' - if the cost was already calculated just return it.
     *            'true' - recalculate the cost no matter what and persist any
     *            changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return Returns the cost object for the job.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost calculateCost(Job p_job, boolean p_recalculate, int p_costType)
            throws RemoteException, CostingException;

    /** Re-sets the cost of a job without recalculating workflows */
    Cost reCostJob(Job p_job, Currency p_currency, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Calculates the cost of a workflow in the currency specified.
     * 
     * @param p_workflow
     *            The workflow to calculate the cost for.
     * @param p_currency
     *            The currency to calculate the cost in.
     * @param p_recalculate
     *            'false' - if the cost was calculating earlier then just use
     *            it. 'true' - recalculate the cost no matter what and persist
     *            any changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return Returns the cost object for the workflow.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, int p_costType) throws RemoteException,
            CostingException;

    /**
     * Calculates the cost of a workflow in the pivot currency.
     * 
     * @param p_workflow
     *            The workflow to calculate the cost for.
     * @param p_recalculate
     *            'false' - if the cost was calculating earlier then just use
     *            it. 'true' - recalculate the cost no matter what and persist
     *            the changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return Returns the cost object for the workflow.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost calculateCost(Workflow p_workflow, boolean p_recalculate,
            int p_costType) throws RemoteException, CostingException;

    /**
     * Calculates the cost of a task in the given currency.
     * 
     * @param p_task
     *            The task to calculate the cost for.
     * @param p_currency
     *            The currency to calculate the cost in.
     * @param p_recalculate
     *            'false' - if the cost was calculating earlier then just use
     *            it. 'true' - recalculate the cost no matter what and persist
     *            the changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return Returns the cost object for the task.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost calculateCost(Task p_task, Currency p_currency, boolean p_recalculate,
            int p_costType) throws RemoteException, CostingException;

    /**
     * Calculates the cost of a task in the pivot currency.
     * 
     * @param p_task
     *            The task to calculate the cost for.
     * @param p_recalculate
     *            'false' - if the cost was calculating earlier then just use
     *            it. 'true' - recalculate the cost no matter what and persist
     *            the changes.
     * @param p_costType
     *            The type of cost to calculate (Expense or Revenue) See
     *            Cost.java to find out valid cost types.
     * 
     * @return Returns the cost object for the task.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    Cost calculateCost(Task p_task, boolean p_recalculate, int p_costType)
            throws RemoteException, CostingException;

    /**
     * Calculate cost for the Workflows having the different status.
     * 
     * @param p_workflow
     *            - Specify the workflow.
     * @param p_currency
     *            - Specify the currency for cost.
     * @param p_recalculate
     *            - Determine whether to calculate costing for unfinished jobs.
     * @param p_costType
     *            - Cost type.
     * @param p_recalcFinishedWorkflow
     *            - Determine whether to recalculate costing for finished jobs.
     * 
     * @return Cost.
     * @throws RemoteException
     * @throws CostingException
     */
    public Cost calculateCost(Workflow p_workflow, Currency p_currency,
            boolean p_recalculate, int p_costType,
            boolean p_recalcFinishedWorkflow) throws RemoteException,
            CostingException;

    /**
     * This method can be used to load any new currencies and updates the
     * conversions. This is currently just used to load the pivot currency on
     * start-up.
     * 
     * @exception CostingException
     *                - An error occurred in the component RemoteException - a
     *                network exception occurred
     */
    void loadCurrencies() throws RemoteException, CostingException;

    /**
     * FIXME This method need to be modified after the system parameters made
     * company sensitive.
     */
    public Currency addOrModifyPivotCurrency(String p_companyId)
            throws RemoteException, CostingException, GeneralException;

    /**
     * Get the factor based on the source currency and target currency.
     * 
     * @param currency
     * @param targetName
     * @param companyId
     * @return
     * @throws RemoteException
     * @throws CostingException
     * @throws GeneralException
     */
    public float getFactor(Currency currency, String targetName,
            String companyId) throws RemoteException, CostingException,
            GeneralException;

    /**
     * Gets the currency based on the currency name.
     * 
     * @param targetName
     * @param companyId
     * @return
     * @throws RemoteException
     * @throws CostingException
     * @throws GeneralException
     */
    public Currency getCurrencyByName(String targetName, String companyId)
            throws RemoteException, CostingException, GeneralException;

    public void removeRate(long p_id) throws GeneralException;
}