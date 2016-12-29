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

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.reports.Constants;
import com.globalsight.reports.datawrap.TmReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

public class TmReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION + "tmReport";

    private static final String NO_CONTENT_STRING = "&nbsp";

    private static final String SUB_TOTAL_MSG = "subtotal";

    private static final String GRAND_TOTAL_MSG = "grandtotal";

    // this "NUM_ITEMS_DISPLAYED" should be computed according to web page
    // format
    private static final int NUM_ITEMS_DISPLAYED = 23;

    private ResourceBundle m_bundle = null;

    private TmReportDataWrap reportDataWrap = null;

    // query parts
    private StringBuffer m_query = null;

    private StringBuffer m_select = null;

    private StringBuffer m_where = null;

    private StringBuffer m_order = null;

    private StringBuffer m_from = null;

    private StringBuffer m_jobQuery = null;

    private StringBuffer m_jobSelect = null;

    private boolean useContextMatch = false;

    private Map<Long, String> noUseInContextValues = new HashMap<Long, String>();

    private Map<Long, Integer> noUseExactMatchValues = new HashMap<Long, Integer>();

    private Map<Long, Boolean> useInContexts = new HashMap<Long, Boolean>();

    private Map<Long, String> noUseContextValues = new HashMap<Long, String>();

    public TmReportHandler()
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
            this.reportKey = Constants.TM_REPORT_KEY;
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
        s_category.debug("Perfoem TmReportHandler.invokeHandler with action "
                + act);

        if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            cleanSession(theSession);
            cleanData();
            createReport(req);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            reportDataWrap = (TmReportDataWrap) getSessionAttribute(theSession,
                    this.reportKey + Constants.REPORT_DATA_WRAP);
        }

        setTableNavigation(req, theSession, NUM_ITEMS_DISPLAYED,
                Constants.TM_REPORT_CURRENT_PAGE_LIST, this.reportKey,
                reportDataWrap);

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
            reportDataWrap = new TmReportDataWrap();
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
        String userId = (String) req.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);
        genUseInContextInfos(userId);
        executeQuery(userId);
    }

    private void executeQuery(String curUserId) throws Exception
    {
        makeSelectClause();
        makeFromClause();
        makeWhereClause();
        makeOrderClause();
        makeQuery();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rsTable = null;

        try
        {
            conn = ConnectionPool.getConnection();

            ps = conn.prepareStatement(m_query.toString());

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                ps.setLong(1, Long.parseLong(currentId));
                ps.setString(2, curUserId);
            }
            else
            {
                ps.setString(1, curUserId);
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

    public void readResultSet(ResultSet rs) throws Exception
    {
        ArrayList<String> fieldnameList = new ArrayList<String>();
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "locale"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "job_id"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "job_name"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "page_name"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "50_74"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "75_84"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "85_94"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "95_99"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "100"));
        if (useContextMatch)
        {
            fieldnameList.add(ReportsPackage.getMessage(m_bundle,
                    "in_context_match"));
        }
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "no_match"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle, "repitition"));
        fieldnameList.add(ReportsPackage.getMessage(m_bundle,
                "total_word_count"));

        // get the each row's data of the result set.
        ArrayList<ArrayList<String>> allRowsDataList = new ArrayList<ArrayList<String>>();

        ResultSetMetaData rsMetaData = rs.getMetaData();

        while (rs.next())
        {
            ArrayList<String> singleRowDataList = new ArrayList<String>();
            for (int j = 0; j < rsMetaData.getColumnCount(); j++)
            {
                singleRowDataList.add(rs.getString(j + 1));
            }
            allRowsDataList.add(singleRowDataList);
        }

        SortUtil.sort(allRowsDataList, new Comparator<ArrayList<String>>()
        {
            public int compare(ArrayList<String> row1, ArrayList<String> row2)
            {
                if ((row1.get(0)).compareTo(row2.get(0)) != 0)
                { // sort by locale(0)
                    return (row1.get(0)).compareTo(row2.get(0));
                }
                else
                {// sort by job_id(1)
                    return (row1.get(1)).compareTo(row2.get(1));
                }
            }
        });

        // checks wether 'allRowsDataList' has data.
        int allRowsDataListSize = allRowsDataList.size();

        // get each group ending index in the "allRowsDataList"
        ArrayList<Integer> groupIndexList = new ArrayList<Integer>();

        String lastRowLocale = NO_CONTENT_STRING;
        for (int index = 0; index < allRowsDataList.size(); index++)
        {
            ArrayList<String> tmpList = allRowsDataList.get(index);
            String currentRowLocale = tmpList.get(0);
            if (!lastRowLocale.equalsIgnoreCase(currentRowLocale))
            {
                groupIndexList.add(new Integer(index));
                lastRowLocale = currentRowLocale;
            }
        }
        groupIndexList.add(new Integer(allRowsDataList.size()));

        // group by locale(0) and summarize columns 4,5,6,7
        double[] grandTotal = new double[]
        { 0.0, 0.0, 0.0, 0.0 };

        ArrayList<ArrayList<String>> allDataForJsp = new ArrayList<ArrayList<String>>();

        for (int i = 0; i < groupIndexList.size() - 1; i++)
        {
            ArrayList<String> rowLocale = new ArrayList<String>();
            int index = groupIndexList.get(i);
            // replace the locale abbreviation with the locale name
            Locale l = GlobalSightLocale.makeLocaleFromString((allRowsDataList
                    .get(index)).get(0));
            rowLocale.add(l.getDisplayName(theUiLocale));
            for (int k = 1; k < allRowsDataList.get(0).size(); k++)
            {
                rowLocale.add(NO_CONTENT_STRING);
            }
            allDataForJsp.add(rowLocale);

            double[] subTotal = new double[]
            { 0.0, 0.0, 0.0, 0.0 };

            int groupStartIndex = groupIndexList.get(i);
            int groupEndIndex = groupIndexList.get(i + 1);

            for (int j = groupStartIndex; j < groupEndIndex; j++)
            {
                ArrayList<String> curRowList = allRowsDataList.get(j);
                subTotal[0] = subTotal[0]
                        + Double.parseDouble(curRowList.get(4).toString());
                subTotal[1] = subTotal[1]
                        + Double.parseDouble(curRowList.get(5).toString());
                subTotal[2] = subTotal[2]
                        + Double.parseDouble(curRowList.get(6).toString());
                subTotal[3] = subTotal[3]
                        + Double.parseDouble(curRowList.get(7).toString());
                curRowList.set(0, NO_CONTENT_STRING);

                Long jobId = Long.parseLong(curRowList.get(1));
                if (!useInContexts.get(jobId))
                {
                    curRowList.set(8, curRowList.get(8) == null ? null
                            : noUseExactMatchValues.get(jobId).toString());
                }

                if (useContextMatch && !useInContexts.get(jobId))
                {
                    curRowList.set(9, noUseInContextValues.get(jobId)
                            .toString());
                }

                allDataForJsp.add(curRowList);
            }

            grandTotal[0] = grandTotal[0] + subTotal[0];
            grandTotal[1] = grandTotal[1] + subTotal[1];
            grandTotal[2] = grandTotal[2] + subTotal[2];
            grandTotal[3] = grandTotal[3] + subTotal[3];

            ArrayList<String> rowSubTotal = new ArrayList<String>();
            rowSubTotal.add(ReportsPackage.getMessage(m_bundle, SUB_TOTAL_MSG));

            for (int indexSubTotal = 1; indexSubTotal < fieldnameList.size(); indexSubTotal++)
            {
                if (indexSubTotal < 4 || indexSubTotal > 7)
                {
                    rowSubTotal.add(NO_CONTENT_STRING);
                }
                else
                {
                    Double doubleAdd = new Double(subTotal[indexSubTotal - 4]);
                    rowSubTotal.add(doubleAdd.toString());
                }
            }
            allDataForJsp.add(rowSubTotal);
        }

        ArrayList<String> rowGrandTotal = new ArrayList<String>();
        rowGrandTotal.add(ReportsPackage.getMessage(m_bundle, GRAND_TOTAL_MSG));

        for (int indexSubTotal = 1; indexSubTotal < fieldnameList.size(); indexSubTotal++)
        {
            if (indexSubTotal < 4 || indexSubTotal > 7)
            {
                rowGrandTotal.add(NO_CONTENT_STRING);
            }
            else
            {
                Double doubleAdd = new Double(grandTotal[indexSubTotal - 4]);
                rowGrandTotal.add(doubleAdd.toString());
            }
        }
        if (allRowsDataListSize > 0)
        {
            allDataForJsp.add(rowGrandTotal);
        }

        reportDataWrap.setTableHeadList(fieldnameList);
        reportDataWrap.setDataList(allDataForJsp);
    }

    /**
     * Fills out the select part of the query
     */
    private void makeSelectClause()
    {
        m_select = new StringBuffer();
        m_select.append("SELECT");

        // 0
        m_select.append(" concat(locale.ISO_COUNTRY_CODE,'_',locale.ISO_LANG_CODE) as \"Locale\",");
        // 1
        m_select.append(" job.id as \"JobId\",");
        // 2
        m_select.append(" job.name as \"JobName\",");
        // 3
        m_select.append(" source_page.external_page_id as \"PageName\",");
        // 4
        String low = this.commonBundle.getString("lb_50");
        m_select.append(" target_page.FUZZY_LOW_WORD_COUNT as \"").append(low)
                .append("\",");
        // 5
        String med = this.commonBundle.getString("lb_75");
        m_select.append(" target_page.FUZZY_MED_WORD_COUNT as \"").append(med)
                .append("\",");
        // 6
        String medhi = this.commonBundle.getString("lb_85");
        m_select.append(" target_page.FUZZY_MED_HI_WORD_COUNT as \"")
                .append(medhi).append("\",");
        // 7
        String hi = this.commonBundle.getString("lb_95");
        m_select.append(" target_page.FUZZY_HI_WORD_COUNT as \"").append(hi)
                .append("\",");
        // 8
        String exact = this.commonBundle.getString("lb_100");
        m_select.append(" target_page.EXACT_SEGMENT_TM_WORD_COUNT as \"")
                .append(exact).append("\",");
        // ? | 9
        if (useContextMatch)
        {
            String inContextTm = this.commonBundle
                    .getString("lb_in_context_tm");
            m_select.append(" target_page.IN_CONTEXT_MATCH_WORD_COUNT as \"")
                    .append(inContextTm).append("\",");
        }

        // 9 | 10
        String nomatch = this.commonBundle.getString("lb_no_match");
        m_select.append(" target_page.NO_MATCH_WORD_COUNT as \"")
                .append(nomatch).append("\",");
        // 10 | 11
        String repet = this.commonBundle.getString("lb_repetition");
        m_select.append(" target_page.REPETITION_WORD_COUNT as \"")
                .append(repet).append("\",");
        // 11 | 12
        m_select.append(" target_page.TOTAL_WORD_COUNT as \"Total Word Count\" ");

    }

    private void makeFromClause()
    {
        m_from = new StringBuffer();
        m_from.append(" FROM target_page, workflow, job, locale, source_page, l10n_profile, project_user ");
    }

    private void makeWhereClause()
    {
        m_where = new StringBuffer();
        m_where.append(" WHERE workflow.job_id = job.id");

        String currentId = CompanyThreadLocal.getInstance().getValue();

        if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
        {
            m_where.append(" AND workflow.COMPANY_ID = ?");
        }

		m_where.append(" AND workflow.target_locale_id = locale.id");
		m_where.append(" AND target_page.workflow_iflow_instance_id = workflow.iflow_instance_id");
		m_where.append(" AND source_page.id = target_page.SOURCE_PAGE_ID");
		m_where.append(" AND job.l10n_profile_id = l10n_profile.id");
		m_where.append(" AND l10n_profile.project_id = project_user.project_id");
		m_where.append(" AND workflow.state != 'CANCELLED'");
		m_where.append(" AND project_user.user_id = ?");
    }

    private void makeOrderClause()
    {
        m_order = new StringBuffer();
        m_order.append(" ORDER BY job.id");
    }

    private void makeQuery()
    {
        m_query = new StringBuffer();
        m_query.append(m_select).append(m_from).append(m_where).append(m_order);
    }

    private void genUseInContextInfos(String userId)
    {
        makeJobSelectClause();
        makeFromClause();
        makeWhereClause();
        makeOrderClause();
        makeJobQuery();

        Connection conn = null;
        PreparedStatement ps = null;

        try
        {
            conn = ConnectionPool.getConnection();
            ps = conn.prepareStatement(m_jobQuery.toString());

            String currentId = CompanyThreadLocal.getInstance().getValue();
            if (!CompanyWrapper.SUPER_COMPANY_ID.equals(currentId))
            {
                ps.setLong(1, Long.parseLong(currentId));
                ps.setString(2, userId);
            }
            else
            {
                ps.setString(1, userId);
            }

            ResultSet rs = ps.executeQuery();
            changeRSToArrayList(rs);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            closeStatement(ps);
            returnConnection(conn);
        }
    }

    private void changeRSToArrayList(ResultSet rs) throws JobException,
            RemoteException, GeneralException, SQLException, NamingException
    {
        while (rs.next())
        {
            Long jobId = rs.getLong(1);
            Integer noUseExactMatchWordCount = rs.getInt(2);

            Job job = ServerProxy.getJobHandler().getJobById(jobId);

            // Boolean isContextMatch = job.getL10nProfile()
            // .getTranslationMemoryProfile()
            // .getIsContextMatchLeveraging();
            boolean isInContextMatch = PageHandler.isInContextMatch(job);
            if (isInContextMatch)
            {
                useContextMatch = true;
            }
            else
            {
				noUseContextValues.put(jobId, "N/A");
                noUseExactMatchValues.put(jobId, noUseExactMatchWordCount);
                noUseInContextValues.put(jobId, "N/A");
            }

            useInContexts.put(jobId, isInContextMatch);
        }
    }

    private void makeJobSelectClause()
    {
        m_jobSelect = new StringBuffer();
        m_jobSelect.append("SELECT");
        m_jobSelect.append(" job.id as \"JobId\",");
        m_jobSelect
                .append(" target_page.TOTAL_EXACT_MATCH_WORD_COUNT as \"")
                .append("NOUSEEXACT")
                .append("\", ")
                .append(" target_page.EXACT_CONTEXT_WORD_COUNT as \"CONTEXT\" ");
    }

    private void makeJobQuery()
    {
        m_jobQuery = new StringBuffer();
        m_jobQuery.append(m_jobSelect).append(m_from).append(m_where)
                .append(m_order);
    }

    private void cleanData()
    {
        m_query = null;
        m_select = null;
        m_where = null;
        m_order = null;
        m_from = null;

        m_jobQuery = null;
        m_jobSelect = null;

        useContextMatch = false;

        noUseInContextValues = new HashMap<Long, String>();
        noUseExactMatchValues = new HashMap<Long, Integer>();
        useInContexts = new HashMap<Long, Boolean>();
    }
}
