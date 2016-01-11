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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.persistence.hibernate.HibernateUtil;

/**
 * Persists and provides manipulation of all the costs for a particular
 * WorkObject.
 */
public class Cost extends PersistentObject
{
    private static final long serialVersionUID = -208218008296111217L;

    public final static float ZERO_COST = (float) 0.0;

    // the cost types as integers
    public final static int EXPENSE = 1;
    public final static int REVENUE = 2;

    public final static String EXPENSE_STRING = "E"; // maps to EXPENSE above
    public final static String REVENUE_STRING = "R"; // maps to REVENUE above

    // this is the estimated cost for the m_objectToCost
    // Leverage In-Context Match
    private Float m_estimatedCost = new Float(0);
    // Leverage 100% Match
    private Float m_noUseEstimatedCost = new Float(0);
    // Leverage Default Match
    private Float m_defaultContextEstimatedCost = new Float(0);
    // this is the actual cost of m_objectToCost BEFORE any surcharges
    private Float m_actualCost = new Float(0);
    // this cost overrides the TOTAL actual cost.
    private Float m_overrideCost = null;
    // stores the final cost - which is the actual cost + surcharges
    private Float m_finalCost = null;
    // the currency all the costs are in.
    private Currency m_currency;
    // the COST type
    private int m_rateType = EXPENSE;

    // points to a WorkObject (Job, Workflow, Task)
    private WorkObject m_objectToCost;

    // Contains all surcharges to be applied to the m_actualCost
    // The key is the name of the surcharge and the value is the Surcharge
    // object.
    private Set<Surcharge> m_surcharges = new HashSet<Surcharge>();

    // A costed object doesn't have to have a cost by word count object,
    // but if it does, this contains the granual by word count costing
    // on which the estimated cost was based
    private CostByWordCount m_costByWordCount = null;

    public boolean isUseInContext = false;

    private HashMap<Long, Cost> workflowCost = new HashMap<Long, Cost>();

    private HashMap<Long, Cost> taskCost = new HashMap<Long, Cost>();

    /**
     * Constructor.
     */
    public Cost()
    {
    }

    /**
     * Constructor to specify just the WorkObject it applies to, its estiamted
     * cost, the currency being used and the rate type.
     */
    public Cost(WorkObject p_object, float p_estimatedCost,
            float p_noUseEstimatedCost, float p_defaultContextEstimatedCost,
            Currency p_currency, int p_type)
    {
        setObject(p_object);
        setEstimatedCost(p_estimatedCost);
        setNoUseEstimatedCost(p_noUseEstimatedCost);

        setDefaultContextEstimatedCost(p_defaultContextEstimatedCost);
        m_currency = p_currency;
        m_rateType = p_type;
    }

    /**
     * Return the object that this cost applies to.
     */
    public WorkObject getWorkObject()
    {
        return m_objectToCost;
    }

    public WorkObject getObject()
    {
        return m_objectToCost;
    }

    public void setObject(WorkObject m_object)
    {
        this.m_objectToCost = m_object;
    }

    /**
     * Return the currency all the costs are in.
     */
    public Currency getCurrency()
    {
        return m_currency;
    }

    /**
     * Set the currency.
     */
    public void setCurrency(Currency p_currency)
    {
        m_currency = p_currency;
    }

    /**
     * Set the estimated cost to the value. Assumes it is in the Currency set.
     */
    public void setEstimatedCost(float p_estimate)
    {
        m_estimatedCost = new Float(p_estimate);
    }

    public void setEstimatedCostAsFloat(float p_estimate)
    {
        m_estimatedCost = new Float(p_estimate);
    }

    public Float getEstimatedCostAsFloat()
    {
        Float cost = null;
        Money money = getEstimatedCost();
        if (money != null)
        {
            cost = new Float(money.getAmount());
        }

        return cost;
    }

    /**
     * Get the estimated cost.
     * 
     * @return The estimated cost of this costable object (includes any costs of
     *         children objects). This could be NULL if the estimated cost has
     *         not been calculated yet.
     */
    public Money getEstimatedCost()
    {
        return new Money(m_estimatedCost.floatValue(), m_currency);
    }

    /**
     * Set the actual cost. Assumes it is in the Currency set.
     */
    public void setActualCost(float p_actual)
    {
        m_actualCost = new Float(p_actual);
    }

    public void setActualCostAsFloat(float p_actual)
    {
        m_actualCost = new Float(p_actual);
    }

    public Float getActualCostAsFloat()
    {
        Float cost = null;
        Money money = getActualCost();
        if (money != null)
        {
            cost = new Float(money.getAmount());
        }

        return cost;
    }

    /**
     * Get the actual cost.
     * 
     * @return The actual cost of this costable object (includes any costs of
     *         children objects). This could be NULL if the actual cost has not
     *         been calculated yet.
     */
    public Money getActualCost()
    {
        Money m = null;

        if (this.getType() == EXPENSE)
        {
            if (m_actualCost != null)
            {
                m = new Money(m_actualCost.floatValue(), m_currency);
            }
        }
        // For job costing issue
        else if (this.getType() == REVENUE)
        {
            if (m_estimatedCost != null && m_noUseEstimatedCost != null)
            {
                m = new Money(
                        (isUseInContext) ? m_estimatedCost.floatValue()
                                : m_noUseEstimatedCost.floatValue(),
                        m_currency);
            }
        }

        return m;
    }

    /**
     * Set the cost rate type. No Type/Revenue/Expense
     */
    public void setType(int p_type)
    {
        m_rateType = p_type;
    }

    /**
     * Get the Cost rate type
     * 
     * @return 1/2 => Expense/Revenue
     */
    public int getType()
    {
        return m_rateType;
    }

    public void setRateTypeAsString(String p_type)
    {
        if (p_type == null || p_type.equalsIgnoreCase(EXPENSE_STRING))
        {
            m_rateType = EXPENSE;
        }
        else
        {
            m_rateType = REVENUE;
        }
    }

    public String getRateTypeAsString()
    {
        return getTypeAsString(m_rateType);
    }

    /**
     * Return the type as a string. These map to the consts above.
     * 
     * @return The string that maps to the type, or null if the type isn't
     *         defined.
     */
    static public String getTypeAsString(int p_type)
    {
        if (p_type == EXPENSE)
        {
            return EXPENSE_STRING;
        }
        else if (p_type == REVENUE)
        {
            return REVENUE_STRING;
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns 'true' if this cost has been overriden. 'false' if not (cost is
     * calculated).
     */
    public boolean isOverriden()
    {
        boolean overriden = false;
        if (m_overrideCost != null)
        {
            overriden = true;
        }
        return overriden;
    }

    /**
     * Set the override cost.
     * 
     * @param p_cost
     *            The new override cost. Assumes it is in the Currency set in
     *            the Cost object.
     */
    public void setOverrideCost(float p_cost)
    {
        m_overrideCost = new Float(p_cost);
    }

    public void setOverrideCostAsFloat(Float p_cost)
    {
        m_overrideCost = p_cost;
    }

    public Float getOverrideCostAsFloat()
    {
        Float cost = null;
        Money money = getOverrideCost();
        if (money != null)
        {
            cost = new Float(money.getAmount());
        }

        return cost;
    }

    /**
     * Clears the override cost - use the actual cost now.
     */
    public void clearOverrideCost()
    {
        m_overrideCost = null;
    }

    /**
     * Get the override cost.
     * 
     * @return The override cost of this costable object. This is NULL if the
     *         cost hasn't been overriden.
     */
    public Money getOverrideCost()
    {
        Money m = null;
        if (m_overrideCost != null)
        {
            m = new Money(m_overrideCost.floatValue(), m_currency);
        }
        return m;
    }

    /**
     * Get the final cost (actual cost with surcharges) or the override cost.
     */
    public Money getFinalCost()
    {
        // if the cost is overriden just return that
        if (isOverriden())
        {
            return getOverrideCost();
        }
        else
        {
            calculateFinalCost();
        }

        return new Money(m_finalCost.floatValue(), m_currency);
    }

    public void setFinalCostAsFloat(Float cost)
    {
        m_finalCost = cost;
    }

    public Float getFinalCostAsFloat()
    {
        Float cost = null;
        Money money = getFinalCost();
        if (money != null)
        {
            cost = new Float(money.getAmount());
        }

        return cost;
    }

    public void setNoUseEstimatedCostAsFloat(Float noUseAmount)
    {
        m_noUseEstimatedCost = noUseAmount;
    }

    public Float getNoUseEstimatedCostAsFloat()
    {
        return m_noUseEstimatedCost;
    }

    public void setNoUseEstimatedCost(float noUseAmount)
    {
        m_noUseEstimatedCost = new Float(noUseAmount);
    }

    public Money getNoUseEstimatedCost()
    {
        return new Money(m_noUseEstimatedCost.floatValue(), m_currency);
    }

    public void setDefaultContextEstimatedCostAsFloat(Float noUseAmount)
    {
        m_defaultContextEstimatedCost = noUseAmount;
    }

    public Float getDefaultContextEstimatedCostAsFloat()
    {
        return m_defaultContextEstimatedCost;
    }

    public void setDefaultContextEstimatedCost(float noUseAmount)
    {
        m_defaultContextEstimatedCost = new Float(noUseAmount);
    }

    public Money getDefaultContextEstimatedCost()
    {
        return new Money(m_defaultContextEstimatedCost.floatValue(), m_currency);
    }

    /**
     * Gets the granular cost by word count information
     * 
     * @return CostByWordCount
     */
    public CostByWordCount getCostByWordCount()
    {
        if (m_costByWordCount == null)
        {
            m_costByWordCount = (CostByWordCount) HibernateUtil.getFirst(
                    "from CostByWordCount cwc where cwc.cost.id = ?",
                    this.getId());
        }
        return m_costByWordCount;
    }

    /**
     * Sets the granular cost by word count information
     * 
     */
    public void setCostByWordCount(CostByWordCount p_costByWordCount)
    {
        m_costByWordCount = p_costByWordCount;
    }

    /**
     * Return the list of surcharges.
     * 
     * @return Returns the list of surcharges. Could be empty, but should not be
     *         NULL.
     */
    public Collection<Surcharge> getSurcharges()
    {
        if (m_surcharges == null)
        {
            return new ArrayList();
        }
        else
        {
            return new ArrayList(m_surcharges);
        }
    }

    public Set<Surcharge> getSurchargeSet()
    {
        return m_surcharges;
    }

    public void setSurchargeSet(Set<Surcharge> surcharges)
    {
        m_surcharges = surcharges;
    }

    /**
     * Add a surcharge to the cost.
     */
    public void addSurcharge(Surcharge p_surcharge)
    {
        p_surcharge.setCost(this);

        // if a flat fee surcharge - convert to currency if needed
        if (p_surcharge.getType() == Surcharge.FLAT_FEE)
        {
            FlatSurcharge fs = (FlatSurcharge) p_surcharge;
            // if the currencies aren't equal - convert the surcharge
            Money amount = fs.getAmount();
            if (!amount.getCurrency().equals(this.getCurrency()))
            {
                fs.setAmount(Cost.convert(amount.getAmount(),
                        amount.getCurrency(), this.getCurrency()), this
                        .getCurrency());
            }

            m_surcharges.add(fs);
        }
        else
        {
            m_surcharges.add(p_surcharge);
        }
        // re-calculate the final cost since
        // the surcharge amounts may have changed
        calculateFinalCost();
    }

    /**
     * Modify the surcharge being passed in. It already exists as part of the
     * cost.
     * 
     * p_surchargeOldName This is the name of the surcharge before modification.
     * The name may not have been changed, but in case it has this is needed to
     * locate the old surcharge for modification. p_surcharge The modified
     * surcharge.
     */
    public void modifySurcharge(String p_surchargeOldName, Surcharge p_surcharge)
    {
        Iterator<Surcharge> ite = m_surcharges.iterator();

        while (ite.hasNext())
        {
            Surcharge surcharge = ite.next();
            if (surcharge.getName().equals(p_surchargeOldName))
            {
                m_surcharges.remove(surcharge);
                surcharge.setCost(null);

                addSurcharge(p_surcharge);

                return;
            }
        }
    }

    /**
     * Remove a surcharge from the cost.
     */
    public Surcharge removeSurcharge(String p_surchargeName)
    {
        Iterator<Surcharge> ite = m_surcharges.iterator();

        while (ite.hasNext())
        {
            Surcharge surcharge = ite.next();
            if (surcharge.getName().equals(p_surchargeName))
            {
                m_surcharges.remove(surcharge);
                surcharge.setCost(null);
                // re-calculate the final cost since
                // the surcharge amounts may have changed
                calculateFinalCost();

                return surcharge;
            }
        }

        return null;
    }

    /**
     * Return a new cost object by adding the specified one to it.
     */
    public Cost add(Cost p_cost)
    {
        Cost newCost = null;
        // if the currencies aren't equal
        // convert to the same currency
        if (!getCurrency().getIsoCode().equals(
                p_cost.getCurrency().getIsoCode()))
        {
            newCost = p_cost.convert(getCurrency());
        }
        else
        {
            newCost = p_cost;
        }

        Money newEstimated = newCost.getEstimatedCost().add(
                this.getEstimatedCost());
        this.setEstimatedCost(newEstimated.getAmount());

        Money newNoUseEstimated = newCost.getNoUseEstimatedCost().add(
                this.getNoUseEstimatedCost());
        this.setNoUseEstimatedCost(newNoUseEstimated.getAmount());

        Money newDefaultContextEstimated = newCost
                .getDefaultContextEstimatedCost().add(
                        this.getDefaultContextEstimatedCost());
        this.setDefaultContextEstimatedCost(newDefaultContextEstimated
                .getAmount());

        Money newActual = newCost.getActualCost().add(this.getActualCost());
        this.setActualCost(newActual.getAmount());
        Money newOverriden = null;
        if (isOverriden() && newCost.isOverriden())
        {
            newOverriden = newCost.getOverrideCost()
                    .add(this.getOverrideCost());
        }
        else if (!isOverriden() && newCost.isOverriden())
        {
            newOverriden = newCost.getOverrideCost();
        }
        else if (isOverriden() && !newCost.isOverriden())
        {
            newOverriden = this.getOverrideCost();
        }
        if (newOverriden != null)
        {
            this.setOverrideCost(newOverriden.getAmount());
        }

        calculateFinalCost();
        return this;
    }

    /**
     * Converts the Cost from the currency it is specified in to the currency
     * requested.
     */
    public Cost convert(Currency p_requestedCurrency)
    {
        if (!getCurrency().getIsoCode()
                .equals(p_requestedCurrency.getIsoCode()))
        {
            float estimatedAmount = convert(getEstimatedCost().getAmount(),
                    getCurrency(), p_requestedCurrency);
            float noUseEstimatedAmount = convert(getNoUseEstimatedCost()
                    .getAmount(), getCurrency(), p_requestedCurrency);
            float defaultContextEstimatedAmount = convert(
                    getDefaultContextEstimatedCost().getAmount(),
                    getCurrency(), p_requestedCurrency);

            float actualAmount = convert(getActualCost().getAmount(),
                    getCurrency(), p_requestedCurrency);
            this.setEstimatedCost(estimatedAmount);
            this.setNoUseEstimatedCost(noUseEstimatedAmount);

            this.setDefaultContextEstimatedCost(defaultContextEstimatedAmount);

            this.setActualCost(actualAmount);

            // if there is an override cost convert it too.
            if (isOverriden())
            {
                float overrideCost = convert(getOverrideCost().getAmount(),
                        getCurrency(), p_requestedCurrency);
                this.setOverrideCost(overrideCost);
            }

            convertCostByWordCount(p_requestedCurrency);
            convertSurcharges(p_requestedCurrency);
            setCurrency(p_requestedCurrency);
            calculateFinalCost();
        }
        // else the currencies are the same so can just leave it
        return this;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer("Cost: ");
        sb.append("m_id=");
        sb.append(getId());
        sb.append(", m_rateType=");
        sb.append(m_rateType);
        sb.append(", m_currency=");
        sb.append(m_currency.getIsoCode());
        sb.append(", m_estimatedCost=");
        sb.append(m_estimatedCost);
        sb.append(", m_actualCost=");
        sb.append(m_actualCost);
        if (isOverriden())
        {
            sb.append(", m_overrideCost=");
            sb.append(m_overrideCost);
        }
        sb.append(", m_objectToCost=");
        sb.append(((PersistentObject) m_objectToCost).getId());
        return sb.toString();
    }

    // ------------------------------package--------------------------------------

    /**
     * Calculate the final cost. - package method used by the CostingEngine
     */
    void calculateFinalCost()
    {
        Money actualCost = getActualCost();
        Money totalCost = actualCost;
        // iterator through surcharges and add to it
        // 1st, calculate the flat surcharge.
        for (Iterator<Surcharge> i = getSurcharges().iterator(); i.hasNext();)
        {
            Surcharge s = (Surcharge) i.next();
            if (s instanceof FlatSurcharge)
            {
                totalCost = totalCost.add(s.surchargeAmount(actualCost));
            }
        }

        // tempCost includes actualCost & all flat surcharges.
        // tempCost will used to calculate the percentage surcharges.
        Money tempCost = new Money(totalCost.getAmount(),
                totalCost.getCurrency());

        // 2nd, calculate the percentage surcharge base on total cost.
        for (Iterator<Surcharge> i = getSurcharges().iterator(); i.hasNext();)
        {
            Surcharge s = (Surcharge) i.next();
            if (s instanceof PercentageSurcharge)
            {
                totalCost = totalCost.add(s.surchargeAmount(tempCost));
            }
        }

        m_finalCost = new Float(totalCost.getAmount());
    }

    // -----------------------------private-----------------------------
    /**
     * Convert surcharges.
     */
    private void convertSurcharges(Currency p_requestedCurrency)
    {
        // convert the flat-fee surcharges
        for (Iterator<Surcharge> i = getSurcharges().iterator(); i.hasNext();)
        {
            Surcharge s = (Surcharge) i.next();

            if (s.getType().equals(Surcharge.FLAT_FEE))
            {
                FlatSurcharge fs = (FlatSurcharge) s;
                float surchargeAmount = convert(fs.getAmount().getAmount(), fs
                        .getAmount().getCurrency(), p_requestedCurrency);
                fs.setAmount(surchargeAmount, p_requestedCurrency);
            }
        }
    }

    private void convertCostByWordCount(Currency p_requestedCurrency)
    {
        CostByWordCount cost = getCostByWordCount();
        if (cost != null)
        {
            cost.setRepetitionCost(convert(cost.getRepetitionCost(),
                    m_currency, p_requestedCurrency));
            cost.setContextMatchCost(convert(cost.getContextMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setInContextMatchCost(convert(cost.getInContextMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setNoUseInContextMatchCost(convert(
                    cost.getNoUseInContextMatchCost(), m_currency,
                    p_requestedCurrency));
            cost.setNoUseExactMatchCost(convert(cost.getNoUseExactMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setSegmentTmMatchCost(convert(cost.getSegmentTmMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setLowFuzzyMatchCost(convert(cost.getLowFuzzyMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setMedFuzzyMatchCost(convert(cost.getMedFuzzyMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setMedHiFuzzyMatchCost(convert(cost.getMedHiFuzzyMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setHiFuzzyMatchCost(convert(cost.getHiFuzzyMatchCost(),
                    m_currency, p_requestedCurrency));
            cost.setNoMatchCost(convert(cost.getNoMatchCost(), m_currency,
                    p_requestedCurrency));

            HibernateUtil.update(cost);
        }
    }

    /**
     * Converts the cost from the currency it is specified in to the currency
     * requested.
     */
    public static float convert(float p_cost, Currency p_currentCurrency,
            Currency p_requestedCurrency)
    {
        float convertedCost = p_cost;

        // if the currencies passed in are different - perform the conversion
        //
        if (!p_currentCurrency.equals(p_requestedCurrency))
        {
            float costInPivot = p_cost
                    / p_currentCurrency.getConversionFactor();
            // The areas where calculations are done (multiply, add, subtract)
            // should be changed to use BigDecimal for the actual calculation.
            convertedCost = BigDecimalHelper.multiply(costInPivot,
                    p_requestedCurrency.getConversionFactor());
        }

        return convertedCost;
    }

    public void setFinalCost(Float cost)
    {
        m_finalCost = cost;
    }

    public HashMap<Long, Cost> getWorkflowCost()
    {
        return this.workflowCost;
    }

    public void addworkflowCost(long wfId, Cost c)
    {
        workflowCost.put(wfId, c);
    }

    public HashMap<Long, Cost> getTaskCost()
    {
        return this.taskCost;
    }

    public void addTaskCost(long taskId, Cost c)
    {
        taskCost.put(taskId, c);
    }
}
