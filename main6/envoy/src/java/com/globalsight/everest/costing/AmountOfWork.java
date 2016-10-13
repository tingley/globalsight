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
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.taskmanager.Task;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class captures the amount of work estimated and actually done on a Task.
 * It is used by the CostingEngine to determine the estimated and actual costs
 * of a Task.
 */
public class AmountOfWork extends PersistentObject
{
    private static final long serialVersionUID = -7758060304906254122L;

    // -------- private attributes-------------------------
    private double m_estimatedAmount = 0;
    private double m_actualAmount = 0;

    /**
     * This should only be of type Rate.UnitOfWork And currently only the
     * follwing are valid: Rate.UnitOfWork.HOURLY Rate.UnitOfWork.PAGE_COUNT
     */
    // default to an hourly rate
    private Integer m_unitOfWork = Rate.UnitOfWork.HOURLY;
    private Task m_task = null;

    private static HashMap UOW_MAP = new HashMap();
    static
    {
        UOW_MAP.put(Rate.UnitOfWork.FIXED, "F");
        UOW_MAP.put(Rate.UnitOfWork.HOURLY, "H");
        UOW_MAP.put(Rate.UnitOfWork.PAGE_COUNT, "P");
        UOW_MAP.put(Rate.UnitOfWork.WORD_COUNT, "W");
    }

    /**
     * Constructor.
     */
    public AmountOfWork()
    {
    }

    /**
     * Create the amount of work object with a particular unit of work -but the
     * actuals and estimates are still set to the default '0'.
     * 
     * @param p_unitOfWork -
     *            Of type Rate.UnitOfWork.HOURLY or Rate.UnitOfWork.PAGE_COUNT
     *            Assumes that a valid uow is specified.
     */
    public AmountOfWork(Integer p_unitOfWork)
    {
        m_unitOfWork = p_unitOfWork;
    }

    /**
     * Create the amount of work object with the particular unit of work and
     * estimated amount of work.
     * 
     * @param p_unitOfWork -
     * @see AmountOfWork(int)
     * @param p_estimatedAmount -
     *            The estimated amount of work to do in the unit of work
     *            specified.
     */
    public AmountOfWork(Integer p_unitOfWork, double p_estimatedAmount)
    {
        m_unitOfWork = p_unitOfWork;
        m_estimatedAmount = p_estimatedAmount;
    }

    /**
     * Set the unit of work. Allows the same unit of work to be used if a rate
     * was changed from hourly to page-based. Just need to change the type.
     */
    public void setUnitOfWork(Integer p_unitOfWork)
    {
        m_unitOfWork = p_unitOfWork;
    }

    /**
     * Returns the unit of work the amounts are in.
     * 
     * @return int - The unit of work the amounts are in. They map to
     *         Rate.UnitOfWork.
     */
    public Integer getUnitOfWork()
    {
        return m_unitOfWork;
    }

    /**
     * @return The estimated amount of work.
     */
    public double getEstimatedAmount()
    {
        return m_estimatedAmount;
    }

    /**
     * Reset the estimated amount of time - in the unit of work already
     * specified in the object.
     * 
     * @param p_estimatedAmount -
     *            the amount specified as a double.
     */
    public void setEstimatedAmount(double p_estimatedAmount)
    {
        m_estimatedAmount = p_estimatedAmount;
    }

    /*
     * @return The actual amount of work done.
     */
    public double getActualAmount()
    {
        return m_actualAmount;
    }

    /**
     * @return The Task object this amount of work is related to.
     */
    public Task getTask()
    {
        return m_task;
    }

    /**
     * Set the task object for this amount of work
     */
    public void setTask(Task p_task)
    {
        m_task = p_task;
    }

    /**
     * Set/reset the actual amount of time - in the unit of work already
     * specified in the object.
     * 
     * @param p_actualAmount -
     *            the amount specified as a double.
     */
    public void setActualAmount(double p_actualAmount)
    {
        m_actualAmount = p_actualAmount;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("m_id = ");
        sb.append(getId());
        sb.append(", m_unitOfWork=");
        sb.append(m_unitOfWork);
        sb.append(", m_estimatedAmount=");
        sb.append(m_estimatedAmount);
        sb.append(", m_actualAmount=");
        sb.append(m_actualAmount);
        return sb.toString();
    }

    public String getStringUnitOfWork()
    {
        String value = null;
        if (m_unitOfWork != null)
        {
            value = (String) UOW_MAP.get(m_unitOfWork);
        }

        return value;
    }

    public void setStringUnitOfWork(String ofWork)
    {
        Integer value = null;
        if (ofWork != null)
        {
            Set keys = UOW_MAP.keySet();
            Iterator iterator = keys.iterator();
            Integer n = null;
            while (iterator.hasNext())
            {
                n = (Integer) iterator.next();
                String content = (String) UOW_MAP.get(n);
                if (ofWork.equalsIgnoreCase(content))
                {
                    value = n;
                }
            }
        }

        m_unitOfWork = value;
    }
}
