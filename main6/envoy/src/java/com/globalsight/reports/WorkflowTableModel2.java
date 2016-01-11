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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.http.HttpSession;
import javax.swing.table.AbstractTableModel;

import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.costing.Currency;
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

public class WorkflowTableModel2 extends AbstractTableModel
{
    private static final long serialVersionUID = 852996928283753234L;

    private ResourceBundle m_bundle = null;
    private Locale m_uilocale = null;

    private List<Map<Integer, Object>> m_dataMapRows = new ArrayList<Map<Integer, Object>>();

    boolean m_containsActivityInfo = true;
    boolean m_jobCosting = false;
    private Currency m_currency = null;
    private WorkflowTaskInstance m_task = null;

    public static final int TRGLOCALE = 0;
    public static final int WFSTATE = 1;
    public static final int CURACTIVITY = 2;
    public static final int ACCEPTER = 3;

    public static final int SEGMENT_TM_WC = 4;
    public static final int FUZZY_HI_WC = 5;
    public static final int FUZZY_MED_HI_WC = 6;
    public static final int FUZZY_MED_WC = 7;
    public static final int FUZZY_LOW_WC = 8;
    public static final int NO_MATCH = 9;
    public static final int REP_WC = 10;
    public static final int IN_CONTEXT_WC = 11;
    public static final int SUBLEVREPS = 12;
    public static final int SUBLEVMATCHES = 13;

    public static final int PER_COMPLETE = 14;
    public static final int DURATION = 15;
    public static final int COST = 16;
    public static final int ACTUALCOST = 17;
    public static final int ACTUAL_REVENUE = 18;

    public static final int CONTEXT_WC = 19;
    // does used by costing report. use 'useInContext' instead.
    public String[] headers = null;

    // public String[] COLUMN_NAMES = {
    // "TRGLOCALE", "WFSTATE", "CURACTIVITY", "ACCEPTER", "SEGMENT_TM_WC",
    // "FUZZY_HI_WC", "FUZZY_MED_HI_WC", "FUZZY_MED_WC", "FUZZY_LOW_WC",
    // "NO_MATCH", "REP_WC", "CONTEXT_WC", "IN_CONTEXT_WC", "SUBLEVREPS",
    // "SUBLEVMATCHES", "PER_COMPLETE", "DURATION", "COST", "ACTUALCOST",
    // "ACTUAL_REVENUE"
    // };

    private static Map<Integer, Object> COLUMNS = new HashMap<Integer, Object>();

    static
    {
        COLUMNS.put(TRGLOCALE, "TRGLOCALE");
        COLUMNS.put(WFSTATE, "WFSTATE");
        COLUMNS.put(CURACTIVITY, "CURACTIVITY");
        COLUMNS.put(ACCEPTER, "ACCEPTER");
        COLUMNS.put(SEGMENT_TM_WC, "SEGMENT_TM_WC");
        COLUMNS.put(FUZZY_HI_WC, "FUZZY_HI_WC");
        COLUMNS.put(FUZZY_MED_HI_WC, "FUZZY_MED_HI_WC");
        COLUMNS.put(FUZZY_MED_WC, "FUZZY_MED_WC");
        COLUMNS.put(FUZZY_LOW_WC, "FUZZY_LOW_WC");
        COLUMNS.put(NO_MATCH, "NO_MATCH");
        COLUMNS.put(REP_WC, "REP_WC");
        COLUMNS.put(IN_CONTEXT_WC, "IN_CONTEXT_WC");
        COLUMNS.put(CONTEXT_WC, "CONTEXT_WC");
        // COLUMNS.put(SUBLEVREPS, "SUBLEVREPS");
        // COLUMNS.put(SUBLEVMATCHES, "SUBLEVMATCHES");
        COLUMNS.put(PER_COMPLETE, "PER_COMPLETE");
        COLUMNS.put(DURATION, "DURATION");
        COLUMNS.put(COST, "COST");
        COLUMNS.put(ACTUALCOST, "ACTUALCOST");
        COLUMNS.put(ACTUAL_REVENUE, "ACTUAL_REVENUE");
    }

    private static final Object EMPTY = new String("");
    private static final String NOT_ACCEPTED_YET = "notAcceptedYet";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "workflowTable";

    private boolean useInContext = false;

    public WorkflowTableModel2()
    {
        // empty constructor
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
    public WorkflowTableModel2(List p_workflows, String p_wfstate,
            HttpSession p_session, Currency p_currency)
    {
        this(p_workflows, p_wfstate, p_session, p_currency, true);
    }

    public WorkflowTableModel2(List p_workflows, String p_wfstate,
            HttpSession p_session, Currency p_currency, boolean fillData)
    {
        if (p_session == null)
            m_uilocale = Locale.US;
        else
            m_uilocale = (Locale) p_session
                    .getAttribute(WebAppConstants.UILOCALE);
        m_bundle = ResourceBundle.getBundle(MY_MESSAGES, m_uilocale);
        m_currency = p_currency;

        if (m_currency != null)
            m_jobCosting = true;
        if (p_wfstate == null || p_wfstate.equals(GlobalSightReplet.DISPATCHED))
            m_containsActivityInfo = true;
        else
            m_containsActivityInfo = false;

        if (fillData)
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
    public WorkflowTableModel2(List p_workflows, HttpSession p_session,
            Currency p_currency)
    {
        this(p_workflows, null, p_session, p_currency);
    }

    public WorkflowTableModel2(List p_workflows, HttpSession p_session,
            Currency p_currency, boolean fillData)
    {
        this(p_workflows, null, p_session, p_currency, fillData);
    }

    /**
     * Fills the table data with the workflow values <br>
     * 
     * @param wfstate
     *            -- the desired workflow state
     */
    public void fillAllData(String p_wfstate, List p_workflows)
    {
        for (int j = 0; j < p_workflows.size(); j++)
        {
            Workflow w = (Workflow) p_workflows.get(j);

            if ((p_wfstate == null || w.getState().equals(p_wfstate))
                    && !Workflow.CANCELLED.equals(w.getState()))
            {
                Map<Integer, Object> dataMap = new HashMap<Integer, Object>();

                dataMap.put(TRGLOCALE, getWorkflowValue(w, TRGLOCALE));
                dataMap.put(WFSTATE, getWorkflowValue(w, WFSTATE));
                if (m_containsActivityInfo)
                {
                    dataMap.put(CURACTIVITY, getWorkflowValue(w, CURACTIVITY));
                    dataMap.put(ACCEPTER, getWorkflowValue(w, ACCEPTER));
                }
                dataMap.put(SEGMENT_TM_WC, getWorkflowValue(w, SEGMENT_TM_WC));
                dataMap.put(FUZZY_HI_WC, getWorkflowValue(w, FUZZY_HI_WC));
                dataMap.put(FUZZY_MED_HI_WC,
                        getWorkflowValue(w, FUZZY_MED_HI_WC));
                dataMap.put(FUZZY_MED_WC, getWorkflowValue(w, FUZZY_MED_WC));
                dataMap.put(FUZZY_LOW_WC, getWorkflowValue(w, FUZZY_LOW_WC));
                dataMap.put(NO_MATCH, getWorkflowValue(w, NO_MATCH));
                dataMap.put(REP_WC, getWorkflowValue(w, REP_WC));
                if (useInContext)
                {
                    dataMap.put(IN_CONTEXT_WC,
                            getWorkflowValue(w, IN_CONTEXT_WC));
                }

                // dataMap.put(SUBLEVREPS, getWorkflowValue(w, SUBLEVREPS));
                // dataMap.put(SUBLEVMATCHES, getWorkflowValue(w,
                // SUBLEVMATCHES));
                dataMap.put(PER_COMPLETE, getWorkflowValue(w, PER_COMPLETE));

                if (m_containsActivityInfo)
                {
                    dataMap.put(DURATION, getWorkflowValue(w, DURATION));
                }
                if (m_jobCosting)
                {
                    dataMap.put(COST, getWorkflowValue(w, COST));
                    dataMap.put(ACTUALCOST, getWorkflowValue(w, ACTUALCOST));
                    if (GlobalSightReplet.isJobRevenueOn())
                    {
                        dataMap.put(ACTUAL_REVENUE,
                                getWorkflowValue(w, ACTUAL_REVENUE));
                    }
                }
                m_dataMapRows.add(dataMap);
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
        return getValue(r, c);
    }

    public Object getValue(int r, int c)
    {
        return m_dataMapRows.get(r).get(c);
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
        setValue(o, r, c);
    }

    public void setValue(Object o, int r, int c)
    {
        m_dataMapRows.get(r).put(c, o);
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
            boolean isInContextMatch = PageHandler.isInContextMatch(w.getJob());
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
                    Map activeTasks = ServerProxy.getWorkflowServer()
                            .getActiveTasksForWorkflow(w.getId());
                    // for now we'll only have one active task
                    if (activeTasks == null || activeTasks.size() == 0)
                    {
                        m_task = null;
                    }
                    else
                    {
                        Object[] tasks = activeTasks.values().toArray();
                        m_task = (WorkflowTaskInstance) tasks[0];
                    }

                    if (m_task == null)
                        o = ReportsPackage.getMessage(m_bundle,
                                "noCurrentActivity");
                    else
                        o = m_task.getActivityDisplayName();
                    break;
                case ACCEPTER:
                    if (m_task != null)
                    {
                        o = UserUtil.getUserNameById(m_task.getAccepter());
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
                    o = new Integer(
                            (isInContextMatch) ? w.getSegmentTmWordCount()
                                    : w.getTotalExactMatchWordCount());
                    break;
                case CONTEXT_WC:
                    o = new Integer(0);
                    break;
                case IN_CONTEXT_WC:
                    o = new Integer(
                            (isInContextMatch) ? w.getInContextMatchWordCount()
                                    : w.getNoUseInContextMatchWordCount());
                    break;
                case FUZZY_HI_WC:
                    o = new Integer(w.getThresholdHiFuzzyWordCount());
                    break;
                case FUZZY_MED_HI_WC:
                    o = new Integer(w.getThresholdMedHiFuzzyWordCount());
                    break;
                case FUZZY_MED_WC:
                    o = new Integer(w.getThresholdMedFuzzyWordCount());
                    break;
                case FUZZY_LOW_WC:
                    o = new Integer(w.getThresholdLowFuzzyWordCount());
                    break;

                case NO_MATCH:
                    o = new Integer(w.getThresholdNoMatchWordCount());
                    break;
                case REP_WC:
                    o = new Integer(w.getRepetitionWordCount());
                    break;

                // case SUBLEVREPS:
                // o = new Integer(w.getSubLevRepetitionWordCount());
                // break;
                // case SUBLEVMATCHES:
                // o = new Integer(w.getSubLevMatchWordCount());
                // break;

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

                // get estimated and actual costs
                case COST:
                    // create a wf cost object
                    Cost cost = BasicReportHandler.calculateWorkflowCost(w,
                            m_currency, Cost.EXPENSE);
                    o = (isInContextMatch) ? ReportsPackage.getEstimatedCost(cost) : ReportsPackage.getNoUseEstimatCost(cost);
                    break;
                case ACTUALCOST:
                    Cost cost2 = BasicReportHandler.calculateWorkflowCost(w,
                            m_currency, Cost.EXPENSE);
                    o = ReportsPackage.getActualCost(cost2);
                    break;
                case ACTUAL_REVENUE:
                    Cost revenue2 = BasicReportHandler.calculateWorkflowCost(w,
                            m_currency, Cost.REVENUE);
                    o = ReportsPackage.getActualCost(revenue2);
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

    // // word count types.
    // private static final int SEGMENT_TM_MATCH_TYPE = 1; // segment tm
    // private static final int HI_FUZZY_MATCH_TYPE = 2; // hi-fuzzy (94-99%)
    // private static final int UNMATCHED_TYPE = 3;
    // private static final int REPETITIONS = 4;
    // private static final int IN_CONTEXT_MATCH_TYPE = 5;
    // private static final int LOW_FUZZY_MATCH_TYPE = 6; // low-fuzzy (50-74%)
    // private static final int MED_FUZZY_MATCH_TYPE = 7; // med-fuzzy (75-84%)
    // private static final int MED_HI_FUZZY_MATCH_TYPE = 8; // med-hi-fuzzy
    // (85-99%)
    // private static final int SUBLEVMATCHES_TYPE = 9;
    // private static final int SUBLEVREPS_TYPE = 10;

    // /**
    // * Computes the total wordcount for all target pages in a workflow.
    // */
    // private static Integer getTotalWordCount(Workflow p_workflow, int p_type)
    // {
    // int count = 0;
    // Iterator pages = p_workflow.getTargetPages().iterator();
    // while (pages.hasNext())
    // {
    // TargetPage curPage = (TargetPage)pages.next();
    // PageWordCounts wordCounts = curPage.getWordCount();
    // Job job = curPage.getSourcePage().getRequest().getJob();
    // boolean isUseInContext =
    // job.getL10nProfile().getTranslationMemoryProfile().getIsContextMatchLeveraging();
    // boolean isInContextMatch = false;
    // try
    // {
    // isInContextMatch = PageHandler.isInContextMatch(job, isUseInContext);
    // }
    // catch (Exception e)
    // {
    // e.printStackTrace();
    // }
    //
    // switch (p_type)
    // {
    // case SEGMENT_TM_MATCH_TYPE:
    // count += (isInContextMatch) ? wordCounts.getSegmentTmWordCount()
    // : wordCounts.getNoUseExactMatchWordCount();
    // break;
    // case IN_CONTEXT_MATCH_TYPE:
    // count += (isInContextMatch) ? wordCounts.getInContextWordCount()
    // : wordCounts.getNoUseInContextMatchWordCount();
    // break;
    // case LOW_FUZZY_MATCH_TYPE:
    // count += wordCounts.getLowFuzzyWordCount();
    // break;
    // case MED_FUZZY_MATCH_TYPE:
    // count += wordCounts.getMedFuzzyWordCount();
    // break;
    // case MED_HI_FUZZY_MATCH_TYPE:
    // count += wordCounts.getMedHiFuzzyWordCount();
    // break;
    // case HI_FUZZY_MATCH_TYPE:
    // count += wordCounts.getHiFuzzyWordCount();
    // break;
    // case UNMATCHED_TYPE:
    // count += wordCounts.getUnmatchedWordCount();
    // break;
    // case REPETITIONS:
    // count += wordCounts.getRepetitionWordCount();
    // break;
    // case SUBLEVREPS_TYPE:
    // count += wordCounts.getSubLevRepetitionWordCount();
    // break;
    // case SUBLEVMATCHES_TYPE:
    // count += wordCounts.getSubLevMatchWordCount();
    // break;
    // }
    // }
    // return new Integer(count);
    // }

    public int getRowCount()
    {
        return m_dataMapRows.size();
    }

    public int getColumnCount()
    {
        return COLUMNS.size();
    }

    public String getColumnName(int c)
    {
        return ReportsPackage.getMessage(m_bundle, (String) COLUMNS.get(c));
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
