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
package com.globalsight.reports.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.costing.Cost;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.reports.Constants;
import com.globalsight.reports.datawrap.CostsByLocaleReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.SortUtil;

public class CostsByLocaleReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION
            + "costsByLocaleReport";
    private static final String NO_CONTENT_STRING = "&nbsp";
    private static final String SUB_TOTAL_MSG = "subtotal";
    private static final String GRAND_TOTAL_MSG = "grandtotal";
    // this "NUM_ITEMS_DISPLAYED" should be computed according to web page
    // format
    private static final int NUM_ITEMS_DISPLAYED = 23;

    private ResourceBundle m_bundle = null;
    private CostsByLocaleReportDataWrap reportDataWrap = null;
    // query parts
    private StringBuffer m_query = null;
    private StringBuffer m_select = null;
    private StringBuffer m_where = null;
    private StringBuffer m_order = null;
    private StringBuffer m_from = null;

    public CostsByLocaleReportHandler()
    {
        // just a empty constructor.
    }

    /**
     * Initializes the report and sets all the required parameters <br>
     * 
     */
    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.COSTS_BY_LOCALE_REPORT_KEY;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        // just do this at the first time
        super.invokeHandler(req, res, p_context);

        String act = (String) req.getParameter(Constants.REPORT_ACT);
        s_category
                .debug("Perfoem CostsByLocaleReportHandler.invokeHandler with action "
                        + act);

        if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            cleanSession(theSession);
            createReport(req);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            reportDataWrap = (CostsByLocaleReportDataWrap) getSessionAttribute(
                    theSession, this.reportKey + Constants.REPORT_DATA_WRAP);
        }

        setTableNavigation(req, theSession, NUM_ITEMS_DISPLAYED,
                Constants.COSTS_BY_LOCALE_REPORT_CURRENT_PAGE_LIST,
                this.reportKey, reportDataWrap);
        dispatcherForward(
                ReportHandlerFactory.getTargetUrl(reportKey
                        + Constants.REPORT_ACT_CREATE), req, res, p_context);
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping styles. <br>
     * 
     * @param HttpServletRequest
     */
    public void createReport(HttpServletRequest req)
    {
        try
        {
            // define a kind of data class like WorkflowStatusReportDataWrap to
            // store data.
            reportDataWrap = new CostsByLocaleReportDataWrap();
            bindMessages();
            bindData(req);
            setSessionAttribute(theSession, this.reportKey
                    + Constants.REPORT_DATA_WRAP, reportDataWrap);
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    public void bindMessages()
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                Constants.REPORT_TITLE));
        reportDataWrap.setDescription(ReportsPackage.getMessage(m_bundle,
                Constants.DESCRIPTION));
        setCommonMessages(reportDataWrap);
    }

    /**
     * Gets the data from the DB and binds it to tables <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    public void bindData(HttpServletRequest req) throws Exception
    {
        executeQuery(req);
    }

    public void readResultSet(ResultSet rs) throws Exception
    {
        SystemConfiguration sc = SystemConfiguration.getInstance();
        boolean s_jobRevenueIsOn = sc
                .getBooleanParameter(SystemConfigParamNames.REVENUE_ENABLED);

        // get the each feild's name of the result set.
        ResultSetMetaData rsMetaData = rs.getMetaData();
        ArrayList fieldnameList = new ArrayList();
        for (int i = 0; i < rsMetaData.getColumnCount(); i++)
        {
            if (i == 1)
            {
                continue;
            }

            String name = rsMetaData.getColumnName(i + 1);
            fieldnameList.add(ReportsPackage.getMessage(m_bundle, name));
        }
        String[] columnKeys = new String[4];
        for (int i = 0; i < 4; i++)
        {
            columnKeys[i] = (String) fieldnameList.get(i + 7);
        }
        reportDataWrap.setColumnKeys(columnKeys);
        // get the each row's data of the result set.
        ArrayList allRowsDataList = new ArrayList();
        int rowCount = 0;
        long jobId = 0;
        while (rs.next())
        {
            rowCount++;
            ArrayList singleRowDataList = new ArrayList();
            for (int j = 0; j < rsMetaData.getColumnCount(); j++)
            {
                if (j == 1)
                {
                    jobId = rs.getLong(2);
                    continue;
                }
                if (j == 2)
                {
                    // project manager
                    singleRowDataList.add(UserUtil.getUserNameById(rs
                            .getString(j + 1)));
                    continue;
                }

                if ("final_cost".equalsIgnoreCase((String) fieldnameList
                        .get(j == 0 ? 0 : j - 1)))
                {
                    Job p_job = ServerProxy.getJobHandler().getJobById(jobId);

                    Cost finalCost = BasicReportHandler.calculateJobCost(p_job,
                            ServerProxy.getCostingEngine().getPivotCurrency(),
                            Cost.EXPENSE);

                    singleRowDataList.add(String.valueOf(finalCost
                            .getFinalCost().getAmount()));

                }
                else
                {
                    singleRowDataList.add(rs.getString(j + 1));
                }
            }
            allRowsDataList.add(singleRowDataList);
        }
        String[] rowKeys = new String[rowCount];
        for (int i = 0; i < rowCount; i++)
        {
            rowKeys[i] = (String) ((ArrayList) allRowsDataList.get(i)).get(2);
        }
        reportDataWrap.setRowKeys(rowKeys);
        double[][] datas = new double[rowCount][columnKeys.length];
        for (int i = 0; i < datas.length; i++)
        {
            for (int j = 0; j < datas[i].length; j++)
            {
                if (((ArrayList) allRowsDataList.get(i)).get(j + 7) == null)
                {
                    datas[i][j] = 0;
                }
                else
                {
                    datas[i][j] = Double
                            .parseDouble((String) ((ArrayList) allRowsDataList
                                    .get(i)).get(j + 7));
                }

            }
        }

        reportDataWrap.setData(datas);
        // sort by locale(0) and job_id(1)
        SortUtil.sort(allRowsDataList, new Comparator()
        {
            public int compare(Object arr1, Object arr2)
            {
                ArrayList row1 = (ArrayList) arr1;
                ArrayList row2 = (ArrayList) arr2;

                if (((String) row1.get(0)).compareTo((String) row2.get(0)) != 0)
                {
                    return ((String) row1.get(0)).compareTo((String) row2
                            .get(0));
                }
                if (((String) row1.get(1)).compareTo((String) row2.get(1)) != 0)
                {
                    return ((String) row1.get(1)).compareTo((String) row2
                            .get(1));
                }
                return 0;
            }
        });

        // checks wether 'allRowsDataList' has data.
        int allRowsDataListSize = allRowsDataList.size();
        // get each group ending index in the "allRowsDataList"
        ArrayList groupIndexList = new ArrayList();
        String lastRowLocale = NO_CONTENT_STRING;
        for (int index = 0; index < allRowsDataList.size(); index++)
        {
            ArrayList tmpList = (ArrayList) allRowsDataList.get(index);
            String currentRowLocale = (String) tmpList.get(0);
            if (!lastRowLocale.equalsIgnoreCase(currentRowLocale))
            {
                groupIndexList.add(new Integer(index));
                lastRowLocale = currentRowLocale;
            }
        }
        groupIndexList.add(new Integer(allRowsDataList.size()));

        reportDataWrap.setTableHeadList(fieldnameList);
        reportDataWrap.setDataList(allRowsDataList);
    }

    private void executeQuery(HttpServletRequest req) throws Exception
    {
        makeSelectClause();
        makeFromClause();
        makeWhereClause();
        makeQuery();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rsTable = null;

        try
        {
            conn = ConnectionPool.getConnection();
            ps = conn.prepareStatement(m_query.toString());

            String currentId = CompanyThreadLocal.getInstance().getValue();
            String currentUserId = (String) req.getSession(false).getAttribute(WebAppConstants.USER_NAME);
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                ps.setLong(1, Long.parseLong(currentId));
                ps.setString(2, currentUserId);
            }
            else
            {
                ps.setString(1, currentUserId);
            }

            rsTable = ps.executeQuery();

            readResultSet(rsTable);

            ps.close();
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(conn);
        }
    }

    /**
     * Fills out the select part of the query
     */
    private void makeSelectClause()
    {
        m_select = new StringBuffer();
        m_select.append("SELECT");
        m_select.append(" VIEW_WORKFLOW_LEVEL.PROJECT_NAME as \"Project\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.JOB_ID as JOB_ID, ");
        m_select.append(" VIEW_WORKFLOW_LEVEL.PROJECT_MANAGER as \"Project Manager\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.JOB_NAME as \"Job Name\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.SOURCE_LOCALE \"Source Locale\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.TARGET_LOCALE as \"")
                .append("Target Locale").append("\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.START_DATE as \"")
                .append("Start Date").append("\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.ACTUAL_END as \"")
                .append("End Date").append("\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.ESTIMATED_COST as \"")
                .append("Estimated Cost").append("\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.ACTUAL_COST as \"")
                .append("Actual Cost").append("\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.FINAL_COST as \"")
                .append("Final Cost").append("\",");
        m_select.append(" VIEW_WORKFLOW_LEVEL.OVERRIDE_COST as \"")
                .append("Override Cost").append("\"");

    }

    private void makeFromClause() 
    {
        m_from = new StringBuffer();
        m_from.append(" FROM VIEW_WORKFLOW_LEVEL, JOB, L10N_PROFILE, PROJECT_USER ");
    }

    private void makeWhereClause()
    {
        m_where = new StringBuffer();
        m_where.append(" WHERE VIEW_WORKFLOW_LEVEL.JOB_ID = JOB.ID");
        m_where.append(" AND JOB.L10N_PROFILE_ID = L10N_PROFILE.ID");
        m_where.append(" AND L10N_PROFILE.PROJECT_ID = PROJECT_USER.PROJECT_ID");

        String currentId = CompanyThreadLocal.getInstance().getValue();

        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            m_where.append(" AND VIEW_WORKFLOW_LEVEL.company_id = ?");
        }
            m_where.append(" AND PROJECT_USER.USER_ID = ?");
    }

    private void makeQuery()
    {
        m_query = new StringBuffer();
        m_query.append(m_select).append(m_from).append(m_where);
    }
}
