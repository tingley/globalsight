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
package com.globalsight.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;
import javax.swing.table.AbstractTableModel;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.date.DateHelper;

public class JobTableModel2 extends AbstractTableModel
{
    ResourceBundle m_bundle = null;
    Locale m_uilocale = null;
    ArrayList m_datarows = null;
    HttpSession m_session = null;
    boolean m_jobCosting = false;
    Currency m_currency = null;

    public static final int ID = 0;
    public static final int PRIORITY = 1;
    public static final int PROJECT = 2;
    public static final int PM = 3;
    public static final int JOBNAME = 4;
    public static final int SRCLOCALE = 5;
    public static final int DATASRC = 6;
    public static final int WORDCNT = 7;
    public static final int CREATE_DATE = 8;
    public static final int COST = 9;
    public static final int ACTUALCOST = 10;
    public static final int TOTALCOST = 11;
    public static final int JOB_OBJECT = 12;

    public static final int JOBSTATUS = 13;
    public static final int CURRENCY = 14;
    public static final int LOCPROFILE = 15;

    // public static final int REVENUE = 16;
    public static final int ACTUAL_REVENUE = 16;
    public static final int TOTAL_REVENUE = 17;

    public static final String[] COLUMN_NAMES =
    { "ID", "PRIORITY", "PROJECT", "PM", "JOBNAME", "SRCLOCALE", "DATASRC",
            "WORDCNT", "CREATE_DATE", "COST", "ACTUALCOST", "TOTALCOST",
            "JOB_OBJECT", "JOBSTATUS", "CURRENCY", "LOCPROFILE",
            "ACTUAL_REVENUE", "TOTAL_REVENUE" };

    private static final Object EMPTY = (Object) new String("");
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "jobTable";

    /**
     * Creates a new JobTableModel2 holding the relevant data about a job
     * excluding workflow information <br>
     * 
     * @param p_job
     *            -- the job to get data from
     * @param p_uilocale
     *            -- the UI locale
     * @return JobTableModel2
     */
    public JobTableModel2(ArrayList p_jobs, Locale p_uilocale,
            Currency p_currency)
    {
        m_uilocale = p_uilocale;
        m_bundle = ResourceBundle.getBundle(MY_MESSAGES, m_uilocale);
        m_datarows = new ArrayList(p_jobs.size());
        m_currency = p_currency;
        if (m_currency == null)
            m_jobCosting = false;
        else
            m_jobCosting = true;

        fillAllData(p_jobs);
    }

    /**
     * Fills all job related data in the table for this job
     */
    private void fillAllData(ArrayList p_jobs)
    {
        for (int i = 0; i < p_jobs.size(); i++)
        {
            Job job = (Job) p_jobs.get(i);
            Object datacolumns[] = new Object[COLUMN_NAMES.length];
            datacolumns[ID] = getJobValue(job, ID);
            datacolumns[PRIORITY] = getJobValue(job, PRIORITY);
            datacolumns[PROJECT] = getJobValue(job, PROJECT);
            datacolumns[PM] = getJobValue(job, PM);
            datacolumns[JOBNAME] = getJobValue(job, JOBNAME);
            datacolumns[SRCLOCALE] = getJobValue(job, SRCLOCALE);
            datacolumns[DATASRC] = getJobValue(job, DATASRC);
            datacolumns[WORDCNT] = getJobValue(job, WORDCNT);
            datacolumns[CREATE_DATE] = getJobValue(job, CREATE_DATE);
            datacolumns[COST] = getJobValue(job, COST);
            datacolumns[ACTUALCOST] = getJobValue(job, ACTUALCOST);
            datacolumns[TOTALCOST] = getJobValue(job, TOTALCOST);

            // add the job itself to the table so that sorting does not loose
            // the job
            // since it will be needed for costing and other operations

            // datacolumns.add(job);//this is "JOB_OBJECT", add the object if
            // really needed beacause it is too big

            datacolumns[JOBSTATUS] = getJobValue(job, JOBSTATUS);
            datacolumns[CURRENCY] = getJobValue(job, CURRENCY);
            datacolumns[LOCPROFILE] = getJobValue(job, LOCPROFILE);

            if (GlobalSightReplet.isJobRevenueOn())
            {
                // datacolumns[REVENUE] = getJobValue(job, REVENUE);
                datacolumns[ACTUAL_REVENUE] = getJobValue(job, ACTUAL_REVENUE);
                datacolumns[TOTAL_REVENUE] = getJobValue(job, TOTAL_REVENUE);
            }
            m_datarows.add(Arrays.asList(datacolumns));
        }
    }

    /**
     * Gets the value at the specified cell <br>
     * 
     * @param
     * @return value as Object
     */
    public Object getValueAt(int r, int c)
    {
        // ArrayList datacolumns = (ArrayList) m_datarows.get(r);
        List datacolumns = (List) m_datarows.get(0);
        return datacolumns.get(c);
    }

    /**
     * Gets the value at the specified cell, the default is get the first job
     * (jobs[0])
     * 
     * @param c
     *            specified the cell number
     * @return the cell content by specified cell id "c"
     */
    public Object getValueAt(int c)
    {
        // ArrayList datacolumns = (ArrayList) m_datarows.get(0);
        List datacolumns = (List) m_datarows.get(0);
        return datacolumns.get(c);
    }

    /**
     * Set the value at the desired cell <br>
     * 
     * @param r
     *            -- row
     * @param c
     *            -- column
     * @return the value as an Object
     */
    public void setValueAt(Object o, int r, int c)
    {
        ArrayList datacolumns = (ArrayList) m_datarows.get(r);
        datacolumns.set(c, o);
    }

    /**
     * Gets the value for the desired column <br>
     * 
     * @param j
     *            --a Job
     * @param c
     *            --the desired column
     * @return the desired value for the column
     */
    private Object getJobValue(Job j, int c)
    {
        Object o = EMPTY;
        try
        {
            L10nProfile l10nProfile;
            switch (c)
            {
                case ID:
                    o = new Long(j.getId());
                    break;
                case PRIORITY:
                    o = new Integer(j.getPriority());
                    break;
                case PROJECT:
                    l10nProfile = LocProfileHandlerHelper.getL10nProfile(j
                            .getL10nProfileId());
                    o = l10nProfile.getProject().getName();
                    break;
                case PM:
                    l10nProfile = LocProfileHandlerHelper.getL10nProfile(j
                            .getL10nProfileId());
                    o = UserUtil.getUserNameById(l10nProfile.getProject()
                            .getProjectManagerId());
                    break;
                case JOBNAME:
                    o = j.getJobName();
                    break;
                case SRCLOCALE:
                    o = j.getSourceLocale().getDisplayName(m_uilocale);
                    break;
                case DATASRC:
                    o = j.getDataSourceName();
                    break;
                case WORDCNT:
                    o = new Integer(j.getWordCount());
                    break;
                case CREATE_DATE:
                    o = DateHelper.getFormattedDateAndTime(j.getCreateDate(),
                            m_uilocale);
                    break;

                // Finds Estimated Cost for the Job
                case COST:
                    if (m_jobCosting)
                    {
                        Cost cost1 = getJobCost(j, m_currency, Cost.EXPENSE);
                        o = ReportsPackage.getEstimatedCost(cost1);
                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;

                // Finds Actual Cost for the Job

                case ACTUALCOST:
                    if (m_jobCosting)
                    {
                        Cost cost2 = getJobCost(j, m_currency, Cost.EXPENSE);
                        o = ReportsPackage.getActualCost(cost2);
                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;

                // Finds the Total Actual Cost for the Job

                case TOTALCOST:
                    if (m_jobCosting)
                    {
                        Cost cost3 = getJobCost(j, m_currency, Cost.EXPENSE);
                        o = new Float(cost3.getFinalCost().getAmount());

                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;
                // Finds Estimated Revenue for the Job

                // case REVENUE:
                // if (m_jobCosting)
                // {
                // Cost revenue1 = getJobCost(j, m_currency, Cost.REVENUE);
                // o = ReportsPackage.getEstimatedCost(revenue1);
                // }
                // else
                // {
                // o = ReportsPackage.ZERO_COST;
                // }
                // break;

                // Finds Actual Revenue for the Job

                case ACTUAL_REVENUE:
                    if (m_jobCosting)
                    {
                        Cost revenue2 = getJobCost(j, m_currency, Cost.REVENUE);
                        o = ReportsPackage.getActualCost(revenue2);
                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;

                // Finds the Total Revenue for the Job
                case TOTAL_REVENUE:
                    if (m_jobCosting)
                    {
                        Cost revenue3 = getJobCost(j, m_currency, Cost.REVENUE);
                        o = ReportsPackage.getFinalCost(revenue3);
                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;

                case JOBSTATUS:
                    o = j.getState();
                    break;
                case CURRENCY:
                    o = m_currency.getDisplayName();
                    break;
                case LOCPROFILE:
                    L10nProfile localProfile = j.getL10nProfile();
                    o = localProfile.getName();
                    break;

                default:
                    o = EMPTY;
                    break;

            }
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
        return o;
    }

    public int getRowCount()
    {
        return m_datarows.size();

    }

    public int getColumnCount()
    {
        int colCount = 0;
        if (BasicReportHandler.isJobRevenueOn())
        {
            colCount = COLUMN_NAMES.length;
        }
        else
        {
            // skip the revenue columns
            colCount = COLUMN_NAMES.length - 2;
        }
        return colCount;
    }

    public String getColumnName(int c)
    {
        return ReportsPackage.getMessage(m_bundle, COLUMN_NAMES[c]);

    }

    private Cost getJobCost(Job p_job, Currency p_currency, int p_costType)
    {
        Cost cost = null;
        if (p_costType == Cost.EXPENSE)
            cost = BasicReportHandler.calculateJobCost(p_job, p_currency,
                    p_costType);
        else
            cost = BasicReportHandler.calculateJobCost(p_job, p_currency,
                    p_costType);
        return cost;
    }
}
