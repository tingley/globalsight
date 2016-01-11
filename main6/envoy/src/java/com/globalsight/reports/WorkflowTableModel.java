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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;
import javax.swing.table.AbstractTableModel;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.LabeledValueHolder;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.date.DateHelper;

public class WorkflowTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = -570609718287991861L;

    private ResourceBundle m_bundle = null;
    private Locale m_uilocale = null;
    private ArrayList<Map<Integer, Object>> m_datarows = null;
    boolean m_containsActivityInfo = true;
    boolean m_jobCosting = false;
    Currency m_currency = null;

    private WorkflowTaskInstance m_task = null;
    public static final int TRGLOCALE = 0;
    public static final int WFSTATE = 1;
    public static final int CURACTIVITY = 2;
    public static final int ACCEPTER = 3;

    public static final int SEGMENT_TM_WC = 4;
    public static final int IN_CONTEXT_WC = 5;
    public static final int CONTEXT_WC = 6;
    public static final int FUZZY_LOW_WC = 7;
    public static final int FUZZY_MED_WC = 8;
    public static final int FUZZY_MED_HI_WC = 9;
    public static final int FUZZY_HI_WC = 10;

    public static final int NO_MATCH = 11;
    public static final int REP_WC = 12;
    public static final int PER_COMPLETE = 13;
    public static final int DURATION = 14;
    public static final int COST = 15;
    public static final int REVENUE = 16;

    public String[] COLUMN_NAMES =
    { "TRGLOCALE", "WFSTATE", "CURACTIVITY", "ACCEPTER", "SEGMENT_TM_WC",
            "IN_CONTEXT_WC", "CONTEXT_WC", "FUZZY_LOW_WC", "FUZZY_MED_WC",
            "FUZZY_MED_HI_WC", "FUZZY_HI_WC", "NO_MATCH", "REP_WC",
            "PER_COMPLETE", "DURATION", "COST", "REVENUE" };

    private static final Object EMPTY = new String("");
    private static final String NOT_ACCEPTED_YET = "notAcceptedYet";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "workflowTable";

    private boolean useInContext = false;

    public WorkflowTableModel()
    {

    }

    /**
     * Creates a Workflow table representing the given list of workflows such
     * that every workflow is in the desired state. <br>
     * 
     * @param p_workflows
     *            -- the list of workflows
     * @param p_wfstate
     *            -- the desired state of the workflow
     * @param p_session
     *            -- the HttpSession which is needed for IFLOW
     * @param p_currency
     *            -- the currency to use if job costing is on (otherwise null)
     */
    public WorkflowTableModel(ArrayList<Workflow> p_workflows,
            String p_wfstate, HttpSession p_session, Currency p_currency)
    {
        this(p_workflows, p_wfstate, p_session, p_currency, true);
    }

    public WorkflowTableModel(ArrayList<Workflow> p_workflows,
            String p_wfstate, HttpSession p_session, Currency p_currency,
            boolean fillAllData)
    {
        m_uilocale = (Locale) p_session.getAttribute(WebAppConstants.UILOCALE);
        m_bundle = ResourceBundle.getBundle(MY_MESSAGES, m_uilocale);
        m_datarows = new ArrayList<Map<Integer, Object>>(p_workflows.size());
        m_currency = p_currency;

        if (m_currency != null)
            m_jobCosting = true;

        if (p_wfstate == null || p_wfstate.equals(GlobalSightReplet.DISPATCHED))
            m_containsActivityInfo = true;
        else
            m_containsActivityInfo = false;

        if (fillAllData)
        {
            fillAllData(p_wfstate, p_workflows);
        }
    }

    /**
     * Creates a Workflow table representing the given list of workflows with no
     * restriction on workflow state <br>
     * 
     * @param p_workflows
     *            -- the list of workflows
     * @param p_session
     *            -- the HttpSession which is needed for IFLOW
     * @param p_currency
     *            -- the currency to use if job costing is on (otherwise null)
     */
    public WorkflowTableModel(ArrayList<Workflow> p_workflows,
            HttpSession p_session, Currency p_currency)
    {
        this(p_workflows, null, p_session, p_currency);
    }

    public WorkflowTableModel(ArrayList<Workflow> p_workflows,
            HttpSession p_session, Currency p_currency, boolean fillAllData)
    {
        this(p_workflows, null, p_session, p_currency, fillAllData);
    }

    /**
     * Fills the table data with the workflow values <br>
     * 
     * @param wfstate
     *            -- the desired workflow state
     */
    public void fillAllData(String p_wfstate, ArrayList<Workflow> p_workflows)
    {
        for (Workflow w : p_workflows)
        {
            if (p_wfstate == null || w.getState().equals(p_wfstate)
                    && !Workflow.CANCELLED.equals(p_wfstate))
            {
                Map<Integer, Object> datacolumns = new HashMap<Integer, Object>();

                datacolumns.put(TRGLOCALE, getWorkflowValue(w, TRGLOCALE));
                datacolumns.put(WFSTATE, getWorkflowValue(w, WFSTATE));
                if (m_containsActivityInfo)
                {
                    datacolumns.put(CURACTIVITY,
                            getWorkflowValue(w, CURACTIVITY));
                    datacolumns.put(ACCEPTER, getWorkflowValue(w, ACCEPTER));
                }
                else
                {
                    datacolumns.put(CURACTIVITY, EMPTY);
                    datacolumns.put(ACCEPTER, EMPTY);
                }
                datacolumns.put(SEGMENT_TM_WC,
                        getWorkflowValue(w, SEGMENT_TM_WC));
                if (useInContext)
                {
                    datacolumns.put(IN_CONTEXT_WC,
                            getWorkflowValue(w, IN_CONTEXT_WC));
                }

                datacolumns.put(FUZZY_HI_WC, getWorkflowValue(w, FUZZY_HI_WC));
                datacolumns.put(FUZZY_MED_HI_WC,
                        getWorkflowValue(w, FUZZY_MED_HI_WC));
                datacolumns
                        .put(FUZZY_MED_WC, getWorkflowValue(w, FUZZY_MED_WC));
                datacolumns
                        .put(FUZZY_LOW_WC, getWorkflowValue(w, FUZZY_LOW_WC));
                datacolumns.put(NO_MATCH, getWorkflowValue(w, NO_MATCH));
                datacolumns.put(REP_WC, getWorkflowValue(w, REP_WC));
                datacolumns
                        .put(PER_COMPLETE, getWorkflowValue(w, PER_COMPLETE));

                if (m_containsActivityInfo)
                {
                    datacolumns.put(DURATION, getWorkflowValue(w, DURATION));
                }
                else
                {
                    datacolumns.put(DURATION, EMPTY);
                }

                if (m_jobCosting)
                {
                    boolean isInContextMatch = false;
                    try
                    {
						isInContextMatch = PageHandler.isInContextMatch(w
								.getJob());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    // create a wf cost object
                    Cost cost = BasicReportHandler.calculateWorkflowCost(w,
                            m_currency, Cost.EXPENSE);

                    // get estimated and actual costs
					datacolumns.put(
							COST,
							(isInContextMatch) ? ReportsPackage
									.getEstimatedCost(cost) : ReportsPackage
									.getNoUseEstimatCost(cost));

                    if (BasicReportHandler.isJobRevenueOn())
                    {
                        // create a wf revenue object
                        Cost revenue = BasicReportHandler
                                .calculateWorkflowCost(w, m_currency,
                                        Cost.REVENUE);
                        datacolumns.put(REVENUE,
                                ReportsPackage.getActualCost(revenue));
                    }
                }
                else
                {
                    datacolumns.put(COST, ReportsPackage.ZERO_COST);
                }
                m_datarows.add(datacolumns);
            }
        }
    }

    /**
     * Gets the value at the desired cell <br>
     * 
     * @param r
     *            -- row
     * @param c
     *            -- column
     * @return the value as an Object
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
        m_datarows.get(r).put(c, o);
    }

    /**
     * Gets the desired value from the workflow <br>
     * 
     * @param w
     *            -- a Workflow
     * @param c
     *            -- the desired column value
     * @return the value as an Object
     */
    private Object getWorkflowValue(Workflow w, int c)
    {
        Object o = EMPTY;
        try
        {
            switch (c)
            {
                case TRGLOCALE:
                    o = w.getTargetLocale().getDisplayName(m_uilocale);
                    break;
                case WFSTATE:
                    // need to retain the original value of the state
                    o = new LabeledValueHolder(w.getState(),
                            ReportsPackage.getMessage(m_bundle, w.getState()));
                    break;
                case CURACTIVITY:
                    Map<?, ?> activeTasks = ServerProxy.getWorkflowServer()
                            .getActiveTasksForWorkflow(w.getId());
                    // for now we'll only have one active task
                    Object[] tasks = (activeTasks == null) ? null : activeTasks
                            .values().toArray();
                    if (tasks != null && tasks.length > 0)
                    {
                        m_task = (WorkflowTaskInstance) tasks[0];
                        o = m_task.getActivityDisplayName();
                    }
                    else
                    {
                        m_task = null;
                        o = ReportsPackage.getMessage(m_bundle,
                                "noCurrentActivity");
                    }
                    break;

                case ACCEPTER:
                    if (m_task != null)
                    {
                        o = UserUtil.getUserNameById(m_task.getAcceptUser());
                        if (o == null)
                            o = ReportsPackage.getMessage(m_bundle,
                                    NOT_ACCEPTED_YET);
                    }
                    else
                    {
                        o = EMPTY;
                    }
                    break;
                case SEGMENT_TM_WC:
                    o = getTotalWordCount(w, SEGMENT_TM_MATCH_TYPE);
                    break;
                case CONTEXT_WC:
                    o = getTotalWordCount(w, CONTEXT_MATCH_TYPE) == -1 ? "N/A"
                            : getTotalWordCount(w, CONTEXT_MATCH_TYPE);
                    break;
                case IN_CONTEXT_WC:
                    o = getTotalWordCount(w, IN_CONTEXT_MATCH_TYPE) == -1 ? "N/A"
                            : getTotalWordCount(w, IN_CONTEXT_MATCH_TYPE);
                    break;
                case FUZZY_HI_WC:
                    o = getTotalWordCount(w, HI_FUZZY_MATCH_TYPE);
                    break;
                case FUZZY_MED_HI_WC:
                    o = getTotalWordCount(w, MED_HI_FUZZY_MATCH_TYPE);
                    break;
                case FUZZY_MED_WC:
                    o = getTotalWordCount(w, MED_FUZZY_MATCH_TYPE);
                    break;
                case FUZZY_LOW_WC:
                    o = getTotalWordCount(w, LOW_FUZZY_MATCH_TYPE);
                    break;
                case NO_MATCH:
                    o = getTotalWordCount(w, UNMATCHED_TYPE);
                    break;
                case REP_WC:
                    o = getTotalWordCount(w, REPETITIONS);
                    break;

                case PER_COMPLETE:
                    o = new Integer(w.getPercentageCompletion());
                    break;
                case DURATION:
                    if (m_task == null)
                        o = EMPTY;
                    else
                    {
                        o = (Object) DateHelper.daysHoursMinutes(m_task
                                .getCompletedTime(), ReportsPackage.getMessage(
                                m_bundle, "lb_abbreviation_day"),
                                ReportsPackage.getMessage(m_bundle,
                                        "lb_abbreviation_hour"), ReportsPackage
                                        .getMessage(m_bundle,
                                                "lb_abbreviation_minute"));
                    }
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

    // word count types.
    private static final int SEGMENT_TM_MATCH_TYPE = 1; // segment tm
    private static final int HI_FUZZY_MATCH_TYPE = 2; // hi-fuzzy (94-99%)
    private static final int UNMATCHED_TYPE = 3;
    private static final int REPETITIONS = 4;
    private static final int IN_CONTEXT_MATCH_TYPE = 5;
    private static final int LOW_FUZZY_MATCH_TYPE = 6; // low-fuzzy (50-74%)
    private static final int MED_FUZZY_MATCH_TYPE = 7; // med-fuzzy (75-84%)
    private static final int MED_HI_FUZZY_MATCH_TYPE = 8; // med-hi-fuzzy
    private static final int CONTEXT_MATCH_TYPE = 9;

    // (85-99%)

    /**
     * Computes the total wordcount for all target pages in a workflow.
     */
    private static Integer getTotalWordCount(Workflow p_workflow, int p_type)
    {
        int count = 0;
        Iterator<?> pages = p_workflow.getTargetPages().iterator();
        while (pages.hasNext())
        {
            TargetPage curPage = (TargetPage) pages.next();
            PageWordCounts wordCounts = curPage.getWordCount();

            switch (p_type)
            {
                case SEGMENT_TM_MATCH_TYPE:
                    count += (PageHandler.isInContextMatch(p_workflow.getJob())) ? wordCounts
                            .getSegmentTmWordCount()
                            : wordCounts.getTotalExactMatchWordCount();
                    break;
                case IN_CONTEXT_MATCH_TYPE:
                {
                    count += (PageHandler.isInContextMatch(p_workflow.getJob())) ? wordCounts
                            .getInContextWordCount() : -1;
                    break;
                }
                case CONTEXT_MATCH_TYPE:
                {
                    count += -1;
                    break;
                }

                case LOW_FUZZY_MATCH_TYPE:
                    count += wordCounts.getLowFuzzyWordCount();
                    break;
                case MED_FUZZY_MATCH_TYPE:
                    count += wordCounts.getMedFuzzyWordCount();
                    break;
                case MED_HI_FUZZY_MATCH_TYPE:
                    count += wordCounts.getMedHiFuzzyWordCount();
                    break;
                case HI_FUZZY_MATCH_TYPE:
                    count += wordCounts.getHiFuzzyWordCount();
                    break;
                case UNMATCHED_TYPE:
                    count += wordCounts.getNoMatchWordCount();
                    break;
                case REPETITIONS:
                    count += wordCounts.getRepetitionWordCount();
                    break;
            }
        }
        return new Integer(count);
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

    public String[] getColumnNames()
    {
        return COLUMN_NAMES;
    }

    public void setColumnNames(String[] column_names)
    {
        COLUMN_NAMES = column_names;
    }

    public boolean isUseInContext()
    {
        return useInContext;
    }

    public void setUseInContext(boolean useInContext)
    {
        this.useInContext = useInContext;
    }

}
