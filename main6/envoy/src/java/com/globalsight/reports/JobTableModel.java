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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.date.DateHelper;

public class JobTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = -7835559819604621491L;

    ResourceBundle m_bundle = null;
    Locale m_uilocale = null;
    LinkedList<List<Object>> m_datarows = null;
    LinkedList<Long> jobIds = null;
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
    public static final int REVENUE = 10;
    public static final int JOB_OBJECT = 11;

    public static final String[] COLUMN_NAMES =
    { "ID", "PRIORITY", "PROJECT", "PM", "JOBNAME", "SRCLOCALE", "DATASRC",
            "WORDCNT", "CREATE_DATE", "COST", "REVENUE" };

    private static final Object EMPTY = (Object) new String("");
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "jobTable";

    /**
     * Creates a new JobTableModel holding the relevant data about a job
     * excluding workflow information <br>
     * 
     * @param p_job
     *            -- the job to get data from
     * @param p_uilocale
     *            -- the UI locale
     * @return JobTableModel
     */
    public JobTableModel(List<Job> p_jobs, Locale p_uilocale,
            Currency p_currency)
    {
        m_uilocale = p_uilocale;
        m_bundle = ResourceBundle.getBundle(MY_MESSAGES, m_uilocale);
        m_datarows = new LinkedList<List<Object>>();
        m_currency = p_currency;

        // don't use job costing values even if it's turned on because the
        // particular report
        // doesn't care about the costing
        if (m_currency != null)
            m_jobCosting = true;

        fillAllData(p_jobs);
    }

    /**
     * Fills all job related data in the table for this job
     */
    private void fillAllData(List<Job> p_jobs)
    {
        for (Job job : p_jobs)
        {
            List<Object> datacolumns = new ArrayList<Object>();
            datacolumns.add(getJobValue(job, ID));
            datacolumns.add(getJobValue(job, PRIORITY));
            datacolumns.add(getJobValue(job, PROJECT));
            datacolumns.add(getJobValue(job, PM));
            datacolumns.add(getJobValue(job, JOBNAME));
            datacolumns.add(getJobValue(job, SRCLOCALE));
            datacolumns.add(getJobValue(job, DATASRC));
            datacolumns.add(getJobValue(job, WORDCNT));
            datacolumns.add(getJobValue(job, CREATE_DATE));
            datacolumns.add(getJobValue(job, COST));
            datacolumns.add(getJobValue(job, REVENUE));

            // add the job itself to the table so that sorting does not lose the
            // job
            // since it will be needed for costing and other operations
            datacolumns.add(job);
            m_datarows.add(datacolumns);
        }
        jobIds = new LinkedList<Long>();
        for (int i = 0; i < m_datarows.size(); i++)
        {
            jobIds.add((Long) m_datarows.get(i).get(0));
        }
    }

    /**
     * Gets the value at the specified cell <br>
     * 
     * @param r
     *            -- row
     * @param c
     *            -- column
     * @return value as Object
     */
    public Object getValueAt(int r, int c)
    {
        return m_datarows.get(r).get(c);
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
        m_datarows.get(r).set(c, o);
    }

    /**
     * Sets the value for the desired column <br>
     * 
     * @param j
     *            --a Job
     * @param dataRow
     *            --the desired DataRowWrap
     */
    @SuppressWarnings("unused")
    private void setJobValue(DataRowWrap dataRow, Job j)
    {
        Object o = EMPTY;
        try
        {
            L10nProfile l10nProfile;
            dataRow.setId(new Long(j.getId()));
            dataRow.setPriority(new Integer(j.getPriority()));
            l10nProfile = LocProfileHandlerHelper.getL10nProfile(j
                    .getL10nProfileId());
            dataRow.setProject(l10nProfile.getProject().getName());
            l10nProfile = LocProfileHandlerHelper.getL10nProfile(j
                    .getL10nProfileId());
            dataRow.setPm(l10nProfile.getProject().getProjectManagerId());
            dataRow.setJobName(j.getJobName());
            dataRow.setSrcLocal(j.getSourceLocale().getDisplayName(m_uilocale));
            dataRow.setDataSrc(j.getDataSourceName());
            dataRow.setWordCNT(new Integer(j.getWordCount()));
            dataRow.setCreateData(DateHelper.getFormattedDateAndTime(
                    j.getCreateDate(), m_uilocale));

            if (m_jobCosting)
            {
                Cost cost1 = getJobCost(j, m_currency, Cost.EXPENSE);
                o = ReportsPackage.getEstimatedCost(cost1);
            }
            else
            {
                o = ReportsPackage.ZERO_COST;
            }
            dataRow.setCost(o);

            if (m_jobCosting && BasicReportHandler.isJobRevenueOn())
            {
                Cost revenue1 = getJobCost(j, m_currency, Cost.REVENUE);
                o = ReportsPackage.getEstimatedCost(revenue1);
            }
            else
            {
                o = ReportsPackage.ZERO_COST;
            }
            dataRow.setRevenue(o);
            dataRow.setJob(j);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            ReportsPackage.logError("Error getting data for report", e);
        }
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
                case COST:
                    if (m_jobCosting)
                    {
                        l10nProfile = LocProfileHandlerHelper.getL10nProfile(j
                                .getL10nProfileId());
                        boolean isInContextMatch = PageHandler.isInContextMatch(j);
                        Cost cost1 = getJobCost(j, m_currency, Cost.EXPENSE);
                        o = (isInContextMatch) ? ReportsPackage.getEstimatedCost(cost1) : ReportsPackage.getNoUseEstimatCost(cost1);
                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;
                case REVENUE:
                    if (m_jobCosting && GlobalSightReplet.isJobRevenueOn())
                    {
                        l10nProfile = LocProfileHandlerHelper.getL10nProfile(j
                                .getL10nProfileId());
                        boolean isInContextMatch = PageHandler.isInContextMatch(j);
                        Cost revenue1 = getJobCost(j, m_currency, Cost.REVENUE);
                        o = (isInContextMatch) ? ReportsPackage.getEstimatedCost(revenue1) : ReportsPackage.getNoUseEstimatCost(revenue1);
                    }
                    else
                    {
                        o = ReportsPackage.ZERO_COST;
                    }
                    break;
                default:
                    o = EMPTY;
                    break;
            }
        }
        catch (Exception e)
        {
            ReportsPackage.logError("Error getting data for report", e);
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
            colCount = COLUMN_NAMES.length - 1;
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
        {
            cost = BasicReportHandler.calculateJobCost(p_job, p_currency,
                    p_costType);
        }
        else
        {
            cost = BasicReportHandler.calculateJobCost(p_job, p_currency,
                    p_costType);
        }

        return cost;
    }

    class DataRowWrap implements Comparable<Object>
    {
        private Long id = null;
        private Integer priority = null;
        private String project = null;
        private String pm = null;
        private String jobName = null;
        private String srcLocal = null;
        private String dataSrc = null;
        private Integer wordCNT = null;
        private String createData = null;
        private Object cost = null;
        private Object revenue = null;
        private Job job = null;

        public Job getJob()
        {
            return job;
        }

        public void setJob(Job job)
        {
            this.job = job;
        }

        public Object getCost()
        {
            return cost;
        }

        public void setCost(Object cost)
        {
            this.cost = cost;
        }

        public String getCreateData()
        {
            return createData;
        }

        public void setCreateData(String createData)
        {
            this.createData = createData;
        }

        public String getDataSrc()
        {
            return dataSrc;
        }

        public void setDataSrc(String dataSrc)
        {
            this.dataSrc = dataSrc;
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
        }

        public String getJobName()
        {
            return jobName;
        }

        public void setJobName(String jobName)
        {
            this.jobName = jobName;
        }

        public String getPm()
        {
            return pm;
        }

        public void setPm(String pm)
        {
            this.pm = pm;
        }

        public Integer getPriority()
        {
            return priority;
        }

        public void setPriority(Integer priority)
        {
            this.priority = priority;
        }

        public String getProject()
        {
            return project;
        }

        public void setProject(String project)
        {
            this.project = project;
        }

        public Object getRevenue()
        {
            return revenue;
        }

        public void setRevenue(Object revenue)
        {
            this.revenue = revenue;
        }

        public String getSrcLocal()
        {
            return srcLocal;
        }

        public void setSrcLocal(String srcLocal)
        {
            this.srcLocal = srcLocal;
        }

        public Integer getWordCNT()
        {
            return wordCNT;
        }

        public void setWordCNT(Integer wordCNT)
        {
            this.wordCNT = wordCNT;
        }

        public Object getValueAt(int i)
        {
            switch (i)
            {
                case ID:
                    return id;
                case PRIORITY:
                    return priority;
                case PROJECT:
                    return project;
                case PM:
                    return pm;
                case JOBNAME:
                    return jobName;
                case SRCLOCALE:
                    return srcLocal;
                case DATASRC:
                    return dataSrc;
                case WORDCNT:
                    return wordCNT;
                case CREATE_DATE:
                    return createData;
                case COST:
                    return cost;
                case REVENUE:
                    return revenue;
                case JOB_OBJECT:
                    return job;
                default:
                    return null;
            }
        }

        public void setValueAt(int i, Object o)
        {
            switch (i)
            {
                case ID:
                    id = (Long) o;
                    break;
                case PRIORITY:
                    priority = (Integer) o;
                    break;
                case PROJECT:
                    project = (String) o;
                    break;
                case PM:
                    pm = (String) o;
                    break;
                case JOBNAME:
                    jobName = (String) o;
                    break;
                case SRCLOCALE:
                    srcLocal = (String) o;
                    break;
                case DATASRC:
                    dataSrc = (String) o;
                    break;
                case WORDCNT:
                    wordCNT = (Integer) o;
                    break;
                case CREATE_DATE:
                    createData = (String) o;
                    break;
                case COST:
                    cost = o;
                    break;
                case REVENUE:
                    revenue = o;
                    break;
                case JOB_OBJECT:
                    job = (Job) o;
                    break;
                default:
                    return;
            }
        }

        public int compareTo(Object object)
        {
            DataRowWrap myClass = (DataRowWrap) object;
            return new CompareToBuilder().append(this.id, myClass.id)
                    .append(this.priority, myClass.priority)
                    .append(this.project, myClass.project)
                    .append(this.pm, myClass.pm)
                    .append(this.createData, myClass.createData)
                    .append(this.jobName, myClass.jobName).toComparison();
        }

        public String toString()
        {
            return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
                    .append("id", this.id).append("priority", this.priority)
                    .append("project", this.project)
                    .append("jobName", this.jobName)
                    .append("srcLocal", this.srcLocal)
                    .append("dataSrc", this.dataSrc).toString();
        }
    }

    public LinkedList<Long> getJobIds()
    {
        return jobIds;
    }
}
