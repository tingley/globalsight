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
package com.globalsight.reports.xmlqueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.swing.table.AbstractTableModel;

import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.common.XmlWriter;
import com.globalsight.reports.JobTableModel2;
import com.globalsight.reports.WorkflowTableModel2;
import com.globalsight.reports.util.ReportsPackage;

/**
 * JobInfoQuery gets job(workflow) information and dumps it out in XML.
 */
public class JobInfoQuery
{
    private Locale m_uiLocale = Locale.US;
    private Currency m_currency = null;
    private XmlWriter m_writer = null;

    public JobInfoQuery(HttpServletRequest p_request) throws Exception
    {
        m_currency = ServerProxy.getCostingEngine().getPivotCurrency();
        m_writer = new XmlWriter();
    }

    /**
     * Gets the data from the DB and writes out XML <br>
     * 
     * @throws Exception
     * @return String of XML
     */
    public String runQuery() throws Exception
    {
        ArrayList jobs = queryJobs();
        ArrayList jobWorkflows = getAllWorkflowsForAllJobs(jobs);
        JobTableModel2 jtm = new JobTableModel2(jobs, m_uiLocale, m_currency);
        m_writer.xmlDeclaration("UTF-8");
        m_writer.startElement("jobInfo", null);
        HashMap workflowTableModels = makeWorkflowTableModels(jtm, jobWorkflows);
        // run through the jobs and dump out all the job info we have
        for (int r = 0; r < jtm.getRowCount(); r++)
        {
            Long jobId = (Long) jtm.getValueAt(r, JobTableModel2.ID);
            WorkflowTableModel2 wtm = (WorkflowTableModel2) workflowTableModels
                    .get(jobId);
            m_writer.startElement("job", null);
            dumpJobInformation(jtm, r);
            dumpWorkflowInformation(wtm);
            m_writer.endElement();
        }
        m_writer.endElement();
        return m_writer.getXml();
    }

    /**
     * Returns a list of jobs for the states: EXPORTED, DISPATCHED, LOCALIZED
     * 
     * @return ArrayList
     */
    private ArrayList queryJobs() throws Exception
    {
        Vector v = new Vector();
        v.add("EXPORTED");
        v.add("DISPATCHED");
        v.add("LOCALIZED");
        return new ArrayList(ServerProxy.getJobHandler().getJobsByStateList(v));
    }

    /**
     * Dumps out job information in XML
     * 
     * @param p_jtm
     *            job table model
     * @param p_row
     *            current row in the job table
     */
    private void dumpJobInformation(JobTableModel2 p_jtm, int p_row)
    {
        Long jobId = (Long) p_jtm.getValueAt(p_row, JobTableModel2.ID);
        String jobName = p_jtm.getValueAt(p_row, JobTableModel2.JOBNAME)
                .toString();
        String priority = p_jtm.getValueAt(p_row, JobTableModel2.PRIORITY)
                .toString();
        String project = p_jtm.getValueAt(p_row, JobTableModel2.PROJECT)
                .toString();
        String projectManager = p_jtm.getValueAt(p_row, JobTableModel2.PM)
                .toString();
        String sourceLocale = p_jtm.getValueAt(p_row, JobTableModel2.SRCLOCALE)
                .toString();

        m_writer.element("jobId", null, jobId.toString(), false);
        m_writer.element("jobName", null, jobName, true);
        m_writer.element("priority", null, priority, false);
        m_writer.element("project", null, project, true);
        m_writer.element("projectManager", null, projectManager, true);
        m_writer.element("sourceLocale", null, sourceLocale, true);
    }

    /**
     * Dumps out all the information from the workflow table model in XML
     * 
     * @param p_wtm
     *            workflowtablemodel2
     */
    private void dumpWorkflowInformation(WorkflowTableModel2 p_wtm)
    {
        for (int r = 0; r < p_wtm.getRowCount(); r++)
        {
            String targetLocale = p_wtm.getValueAt(r,
                    WorkflowTableModel2.TRGLOCALE).toString();
            String state = p_wtm.getValueAt(r, WorkflowTableModel2.WFSTATE)
                    .toString();
            String currentActivity = p_wtm.getValueAt(r,
                    WorkflowTableModel2.CURACTIVITY).toString();
            String accepter = p_wtm.getValueAt(r, WorkflowTableModel2.ACCEPTER)
                    .toString();
            String wcExact = p_wtm.getValueAt(r,
                    WorkflowTableModel2.SEGMENT_TM_WC).toString();
            String wcFuzzy = p_wtm.getValueAt(r,
                    WorkflowTableModel2.FUZZY_HI_WC).toString();
            String wcNoMatch = p_wtm
                    .getValueAt(r, WorkflowTableModel2.NO_MATCH).toString();
            String wcRep = p_wtm.getValueAt(r, WorkflowTableModel2.REP_WC)
                    .toString();
            String perComplete = p_wtm.getValueAt(r,
                    WorkflowTableModel2.PER_COMPLETE).toString();
            String duration = p_wtm.getValueAt(r, WorkflowTableModel2.DURATION)
                    .toString();
            String cost = p_wtm.getValueAt(r, WorkflowTableModel2.COST)
                    .toString();
            String actualCost = p_wtm.getValueAt(r,
                    WorkflowTableModel2.ACTUALCOST).toString();
            String actualRevenue = p_wtm.getValueAt(r,
                    WorkflowTableModel2.ACTUAL_REVENUE).toString();

            m_writer.startElement("workflow", null);
            m_writer.element("targetLocale", null, targetLocale, true);
            m_writer.element("state", null, state, false);
            m_writer.element("currentActivity", null, currentActivity, true);
            m_writer.element("accepter", null, accepter, true);
            m_writer.element("wcExact", null, wcExact, false);
            m_writer.element("wcFuzzy", null, wcFuzzy, false);
            m_writer.element("wcNoMatch", null, wcNoMatch, false);
            m_writer.element("wcRep", null, wcRep, false);
            m_writer.element("perComplete", null, perComplete, false);
            m_writer.element("duration", null, duration, false);
            m_writer.element("cost", null, cost, false);
            m_writer.element("actualCost", null, actualCost, false);
            m_writer.element("actualRevenue", null, actualRevenue, false);
            m_writer.endElement();
        }
    }

    /**
     * Returns an arraylist containing workflowtablemodels for each job <br>
     * 
     * @param p_jobWorkflows --
     *            an arraylist of arraylists for each job containing the
     *            workflows
     * @return ArrayList of WorkflowTableModels
     */
    private HashMap makeWorkflowTableModels(JobTableModel2 p_jtm,
            ArrayList p_jobWorkflows)
    {
        HashMap wtms = new HashMap();
        for (int i = 0; i < p_jobWorkflows.size(); i++)
        {
            ArrayList workflows = (ArrayList) p_jobWorkflows.get(i);
            WorkflowTableModel2 wtm = new WorkflowTableModel2(workflows, null,
                    null, m_currency);
            Long jobid = (Long) p_jtm.getValueAt(i, 0);
            wtms.put(jobid, wtm);
        }
        return wtms;
    }

    /**
     * Returns an array list containing an array list for each job containing
     * its workflows <br>
     * 
     * @param p_jobs --
     *            ArrayList of jobs
     */
    private ArrayList getAllWorkflowsForAllJobs(ArrayList p_jobs)
    {
        ArrayList jobWorkflows = new ArrayList(); // contains an array list
                                                    // for each job
        for (int i = 0; i < p_jobs.size(); i++)
        {
            Job job = (Job) p_jobs.get(i);
            ArrayList workflows = null; // the workflows for this job
            try
            {
                workflows = new ArrayList(ServerProxy.getJobReportingManager()
                        .getWorkflowsByJobId(job.getId()));
            }
            catch (Exception e)
            {
                workflows = new ArrayList();
                ReportsPackage.logError(e);
            }
            jobWorkflows.add(workflows);
        }
        return jobWorkflows;
    }

    private static final Integer ONE = new Integer(1);

    /**
     * Inner class to handle counting the number of jobs per activity <br>
     * 
     * @param
     * @return
     * @throws
     */
    class ActivityCountTableModel extends AbstractTableModel
    {
        private String[] m_columnNames;
        private Integer[] m_data;

        public ActivityCountTableModel(JobTableModel2 p_jtm, HashMap p_workflows)
        {
            HashMap map = new HashMap();
            String activity;
            for (int j = 0; j < p_jtm.getRowCount(); j++)
            {
                Long jobid = (Long) p_jtm.getValueAt(j, 0);
                WorkflowTableModel2 wtm = (WorkflowTableModel2) p_workflows
                        .get(jobid);
                for (int w = 0; w < wtm.getRowCount(); w++)
                {
                    activity = (String) wtm.getValueAt(w,
                            WorkflowTableModel2.CURACTIVITY);
                    if (map.containsKey(activity) == false)
                    {
                        map.put(activity, ONE); // first count
                    }
                    else
                    {
                        Integer oldValue = (Integer) map.get(activity);
                        Integer newValue = new Integer(oldValue.intValue() + 1);
                        map.put(activity, newValue);
                    }
                }

                m_columnNames = new String[map.size()];
                m_data = new Integer[map.size()];

                TreeSet activities = new TreeSet(map.keySet());
                Iterator iter = activities.iterator();
                int i = 0;
                Integer totalcount;
                while (iter.hasNext())
                {
                    activity = (String) iter.next();
                    totalcount = (Integer) map.get(activity);
                    m_columnNames[i] = activity;
                    m_data[i] = totalcount;
                    i++;
                }
            }
        }

        public Object getValueAt(int r, int c)
        {
            return m_data[c];
        }

        public int getRowCount()
        {
            return 1;
        }

        public int getColumnCount()
        {
            return m_columnNames.length;
        }

        public String getColumnName(int c)
        {
            return m_columnNames[c];
        }
    }
}
