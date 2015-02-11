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
package com.globalsight.everest.webapp.javabean;

import com.globalsight.everest.costing.Rate;

/**
 * This is a light-weight version of TaskImpl object used for the graphical
 * workflow UI.
 */
public class TaskInfoBean implements java.io.Serializable
{
    private long m_taskId;
    private String m_activityName;
    private String m_estimatedHours = null;
    private String m_actualHours = null;
    private Rate m_expenseRate = null;
    private Rate m_revenueRate = null;
    private int m_rateSelectionCriteria = 1;
    private int m_isReportUploadCheck = 0;

    /**
     * Constructor that leaves the actual amount of hours set to null.
     * 
     * @param p_taskId
     *            The id of the task this information is about.
     * @param p_estimatedHours
     *            A string specifying the amount of estimated hours or NULL if
     *            not specified yet.
     * @param p_expenseRate
     *            The expense rate the task is associated with or NULL if no
     *            rate is associated with it.
     * @param p_revenueRate
     *            The revenue rate the task is associated with or NULL if no
     *            rate is associated with it.
     * @param p_rateSelectionCriteria
     *            The revenue rate the task is associated with or NULL if no
     *            rate is associated with it.
     */
    public TaskInfoBean(long p_taskId, String p_estimatedHours,
            Rate p_expenseRate, Rate p_revenueRate, int p_rateSelectionCriteria)
    {
        m_taskId = p_taskId;
        m_estimatedHours = p_estimatedHours;
        m_expenseRate = p_expenseRate;
        m_revenueRate = p_revenueRate;
        m_rateSelectionCriteria = p_rateSelectionCriteria;
    }

    /**
     * Constructor
     * 
     * @param p_taskId
     *            The id of the task this information is about.
     * @param p_estimatedHours
     *            A string specifying the amount of estimated hours or NULL if
     *            not specified yet.
     * @param p_actualHours
     *            A string specifying the amount of actual hours or NULL if not
     *            specified yet.
     * @param p_expenseRate
     *            The expense rate the task is associated with or NULL if no
     *            rate is associated with it.
     * @param p_revenueRate
     *            The revenue rate the task is associated with or NULL if no
     *            rate is associated with it.
     */
    public TaskInfoBean(long p_taskId, String p_estimatedHours,
            String p_actualHours, Rate p_expenseRate, Rate p_revenueRate,
            int p_rateSelectionCriteria, int p_isReportUploadCheck)
    {
        m_taskId = p_taskId;
        m_estimatedHours = p_estimatedHours;
        m_actualHours = p_actualHours;
        m_expenseRate = p_expenseRate;
        m_revenueRate = p_revenueRate;
        m_rateSelectionCriteria = p_rateSelectionCriteria;
        m_isReportUploadCheck = p_isReportUploadCheck;
    }

    public TaskInfoBean(long p_taskId, String p_estimatedHours,
            String p_actualHours, Rate p_expenseRate, Rate p_revenueRate,
            int p_rateSelectionCriteria, String p_activityName,
            int p_isReportUploadCheck)
    {
        m_taskId = p_taskId;
        m_estimatedHours = p_estimatedHours;
        m_actualHours = p_actualHours;
        m_expenseRate = p_expenseRate;
        m_revenueRate = p_revenueRate;
        m_rateSelectionCriteria = p_rateSelectionCriteria;
        m_activityName = p_activityName;
        m_isReportUploadCheck = p_isReportUploadCheck;
    }

    // ////////////////////////////////////////////////////////////////////
    // Begin: Helper methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the estimated number of hours spent on this task. This will return
     * the estimated amount of hours for a rate with type of
     * Rate.UnitOfWork.HOURLY.
     * 
     * @return The estimated amount of hours spend on this task or NULL if not
     *         set.
     */
    public String getEstimatedHours()
    {
        return m_estimatedHours;
    }

    /**
     * Get the actual number of hours spent on this task. This will return the
     * actual amount of hours for a rate with type of Rate.UnitOfWork.HOURLY.
     * 
     * @return The actual amount of hours spend on this task or NULL if not set.
     */
    public String getActualHours()
    {
        return m_actualHours;
    }

    /**
     * Sets the amount of actual hours.
     * 
     * @param p_actualHours
     *            The number of actual hours spent on the task or NULL if
     *            clearing out.
     * 
     */
    public void setActualHours(String p_actualHours)
    {
        m_actualHours = p_actualHours;
    }

    /**
     * Get the expense rate object for this task.
     * 
     * @return The expense rate of the task.
     */
    public Rate getExpenseRate()
    {
        return m_expenseRate;
    }

    /**
     * Get the revenue rate object for this task.
     * 
     * @return The revenue rate of the task.
     */
    public Rate getRevenueRate()
    {
        return m_revenueRate;
    }

    /**
     * Get the rate selection criteria
     */
    public int getRateSelectionCriteria()
    {
        return m_rateSelectionCriteria;
    }

    /**
     * Set the rate selection criteria
     */
    public void setRateSelectionCriteria(int p_rateSelection)
    {
        m_rateSelectionCriteria = p_rateSelection;
    }

    /**
     * Get the task instance id.
     * 
     * @return The id of the task.
     */
    public long getTaskId()
    {
        return m_taskId;
    }

    public String getActivityName()
    {
        return m_activityName;
    }

    public void setActivityName(String p_activityName)
    {
        m_activityName = p_activityName;
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Helper methods
    // ////////////////////////////////////////////////////////////////////

	public void setIsReportUploadCheck(int m_isReportUploadCheck) {
		this.m_isReportUploadCheck = m_isReportUploadCheck;
	}

	public int getIsReportUploadCheck() {
		return m_isReportUploadCheck;
	}
}
