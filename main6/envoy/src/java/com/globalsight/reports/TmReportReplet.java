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

import inetsoft.report.ReportSheet;
import inetsoft.report.StyleConstants;
import inetsoft.report.StyleSheet;
import inetsoft.report.TextElement;
import inetsoft.report.filter.DefaultSortedTable;
import inetsoft.report.filter.GroupFilter;
import inetsoft.report.filter.SortFilter;
import inetsoft.report.filter.SumFormula;
import inetsoft.report.lens.JDBCTableLens;
import inetsoft.report.lens.SubTableLens;
import inetsoft.report.lens.TableChartLens;
import inetsoft.report.lens.swing.TableModelLens;
import inetsoft.report.style.Professional;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletRequest;
import inetsoft.sree.SreeLog;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.naming.NamingException;
import javax.swing.table.DefaultTableModel;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobException;
import com.globalsight.everest.persistence.PersistenceException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.usermgr.UserLdapHelper;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportsPackage;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;

public class TmReportReplet extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "tmReport";

    private ResourceBundle m_bundle = null;
    // query parts
    private StringBuffer m_query = null;
    private StringBuffer m_select = null;
    private StringBuffer m_where = null;
    private StringBuffer m_order = null;
    private StringBuffer m_from = null;

    private StringBuffer m_jobQuery = null;
    private StringBuffer m_jobSelect = null;
    private String[] headers;

    public TmReportReplet()
    {
        readTemplate();
    }

    /**
     * Initializes the report and sets all the required parameters <br>
     * 
     * @param RepletRequest
     * @throws RepletException
     */
    public void init(RepletRequest req) throws RepletException
    {
        super.init(req); // get the common parameters
        addMoreRepletParameters();
    }

    /**
     * Returns the Style Report Template name
     */
    public String getTemplateName()
    {
        return MY_TEMPLATE;
    }

    /**
     * Adds additional report creation parameters to the replet parameters
     */
    private void addMoreRepletParameters()
    {
        addRepletParameters(theParameters);
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping and chart styles. <br>
     * 
     * @param RepletRequest
     * @return ReportSheet
     * @throws RepletException
     */
    public ReportSheet createReport(RepletRequest req) throws RepletException
    {
        try
        {
            bindMessages(req);
            bindData(req);
        }
        catch (Exception e)
        {
            SreeLog.print(e);
            throw new RepletException(e.getMessage());
        }
        return theReport;
    }

    /**
     * Fills out the messages on the report <br>
     * 
     * @param RepletRequest
     */
    private void bindMessages(RepletRequest req)
    {
        setCommonMessages(req);
        m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
        TextElement txtReportTitle = (TextElement) theReport
                .getElement("txtReportTitle");
        txtReportTitle.setText(ReportsPackage.getMessage(m_bundle,
                "txtReportTitle"));
    }

    /**
     * Gets the data from the DB and binds it to charts and tables <br>
     * 
     * @param RepletRequest
     * @throws Exception
     */
    private void bindData(RepletRequest req) throws Exception
    {
        String companyId = CompanyThreadLocal.getInstance().getValue();
        if (companyId == null)
        {
            String companyName = (String) theSession
                    .getAttribute(UserLdapHelper.LDAP_ATTR_COMPANY);
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        ArrayList jobs = getIsUseInContext();
        headers = getHeaders(jobs);

        JDBCTableLens table = executeQuery();

        // sort by locale(2) and job_id(0)
        SortFilter sf = new SortFilter(table, new int[]
        { 2, 0 });
        // group by locale(2) and summarize columns 4,5,6
        DefaultSortedTable dst = new DefaultSortedTable(sf, new int[]
        { 2 });

        GroupFilter group = new GroupFilter(dst, new int[]
        { 4, 5, 6, 7 }, new SumFormula(), new SumFormula());
        group.setShowGroupColumns(false);
        group.setGrandLabel(ReportsPackage.getMessage(m_bundle,
                "all_locales_short"));
        group.setAddGroupHeader(true);
        fullyLoadTable(group);

        int chartCols[] = new int[]
        { 4, 5, 6, 7 };
        ArrayList labels = new ArrayList();
        ArrayList rows = new ArrayList();

        int numGroupRows = group.getRowCount();
        for (int r = 0; r < numGroupRows; r++)
        {
            boolean isGroupHeaderRow = group.isGroupHeaderRow(r);
            boolean isSummaryRow = group.isSummaryRow(r);
            if (isSummaryRow)
            {
                rows.add(new Integer(r));
                String l = (String) group.getObject(r, 0);
                labels.add(l);
            }
        }

        int chartRows[] = new int[rows.size()];
        String[] chartLabels = new String[rows.size()];
        for (int i = 0; i < chartRows.length; i++)
        {
            Integer ival = (Integer) rows.get(i);
            chartRows[i] = ival.intValue();
            chartLabels[i] = (String) labels.get(i);
        }

        // do the chart
        TableChartLens tcl = new TableChartLens(new SubTableLens(group,
                chartRows, chartCols), false /* use columns as data sets */);
        tcl.setLabels(chartLabels);
        tcl.setYTitle(ReportsPackage.getMessage(m_bundle, "tm_count"));
        tcl.setTitleFont(GlobalSightReplet.FONT_CHART_TITLE);
        tcl.setStyle(StyleConstants.CHART_BAR);

        StyleSheet ss = (StyleSheet) theReport;
        ss.addChart(tcl);

        if (group.getRowCount() < 2)
        {
            ReportsPackage.logError("No data in grouped table.");
            return;
        }

        DefaultTableModel dtm = new DefaultTableModel(group.getRowCount() - 1,
                group.getColCount());
        Vector colNames = new Vector();
        for (int h = 0; h < group.getColCount(); h++)
            colNames.add(group.getObject(0, h));
        dtm.setColumnIdentifiers(colNames);
        for (int r = 1; r < group.getRowCount(); r++)
        {
            for (int c = 0; c < group.getColCount(); c++)
            {
                if (c == 0 && group.isSummaryRow(r) == false
                        && group.isGroupHeaderRow(r) == true)
                {
                    // replace the locale abbreviation with the locale name
                    Locale l = GlobalSightLocale
                            .makeLocaleFromString((String) group
                                    .getObject(r, 0));
                    dtm.setValueAt(l.getDisplayName(theUiLocale), r - 1, 0);
                }
                else if (c == 0 && group.isSummaryRow(r) == true
                        && group.isGroupHeaderRow(r) == false)
                {
                    if (r < group.getRowCount() - 1)
                    {
                        // replace the locale with "SubTotal"
                        dtm.setValueAt(
                                ReportsPackage.getMessage(m_bundle, "subtotal"),
                                r - 1, 0);
                    }
                    else
                    {
                        // replace the locale with "SubTotal"
                        dtm.setValueAt(ReportsPackage.getMessage(m_bundle,
                                "grandtotal"), r - 1, 0);
                    }
                }
                else
                {
                    // copy the value from the group table over to the new table
                    dtm.setValueAt(group.getObject(r, c), r - 1, c);
                }
            }
        }
        if (headers[0] != null)
        {
            int index = 0;
            for (int i = 0; i < jobs.size(); i++)
            {
                ArrayList job = (ArrayList) jobs.get(i);

                boolean isUseInContext = ((Boolean) job.get(0)).booleanValue();
                if (dtm.getValueAt(i, 0) != null)
                {
                    index++;
                }
                if (!isUseInContext)
                {

                    dtm.setValueAt((dtm.getValueAt(index, 9) == null) ? null
                            : job.get(2), index, 9);

                    dtm.setValueAt((dtm.getValueAt(index, 10) == null) ? null
                            : job.get(1), index, 10);

                }
                index++;
            }
        }

        Professional prof = new Professional(new TableModelLens(dtm));
        prof.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);
        prof.setFont(GlobalSightReplet.FONT_NORMAL);
        prof.setRowFont(0, GlobalSightReplet.FONT_LABEL);
        for (int r = 1; r < prof.getRowCount(); r++)
        {
            if (group.isSummaryRow(r) || group.isGroupHeaderRow(r))
            {
                prof.setRowFont(r, GlobalSightReplet.FONT_LABEL);
            }
        }

        ss.addTable(prof);
    }

    private String[] getHeaders(ArrayList jobs)
    {
        String[] headers = new String[2];
        for (int i = 0; i < jobs.size(); i++)
        {
            ArrayList job = (ArrayList) jobs.get(i);
            boolean isUseInContext = ((Boolean) job.get(0)).booleanValue();

            if (isUseInContext)
            {
                headers[0] = "IN CONTEXT MATCH";
            }
        }
        return headers;
    }

    private ArrayList getIsUseInContext()
    {
        makeJobSelectClause();
        makeFromClause();
        makeWhereClause();
        makeOrderClause();
        makeJobQuery();
        Connection conn = null;
        PreparedStatement ps = null;
        ArrayList table = null;

        try
        {
            conn = ConnectionPool.getConnection();
            ps = conn.prepareStatement(m_jobQuery.toString());
            ResultSet rs = ps.executeQuery();
            table = changeRSToArrayList(rs);
            ps.close();
        }
        catch (ConnectionPoolException e)
        {
            e.printStackTrace();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        catch (JobException e)
        {
            e.printStackTrace();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (GeneralException e)
        {
            e.printStackTrace();
        }
        catch (NamingException e)
        {
            e.printStackTrace();
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(conn);
        }

        return table;
    }

    private ArrayList changeRSToArrayList(ResultSet rs) throws SQLException,
            JobException, RemoteException, GeneralException, NamingException
    {
        ResultSetMetaData rsMetaData = rs.getMetaData();
        ArrayList allRowsDataList = new ArrayList();
        while (rs.next())
        {
            ArrayList singleRowDataList = new ArrayList();

            Job job = ServerProxy.getJobHandler().getJobById(rs.getLong(1));
            boolean isInContextMatch = PageHandler.isInContextMatch(job);
            singleRowDataList.add(isInContextMatch);
            // NO_USE_IC_MATCH_WORD_COUNT
            singleRowDataList.add(new Integer(rs.getInt(2)));
            // TOTAL_EXACT_MATCH_WORD_COUNT
            singleRowDataList.add(new Integer(rs.getInt(3)));

            allRowsDataList.add(singleRowDataList);
        }
        return allRowsDataList;
    }

    private void makeJobSelectClause()
    {
        m_jobSelect = new StringBuffer();
        m_jobSelect.append("SELECT");
        m_jobSelect.append(" job.id as \"JobId\",");
        m_jobSelect.append(" target_page.NO_USE_IC_MATCH_WORD_COUNT as \"")
                .append("NOUSEINCONTEXT").append("\",");
        m_jobSelect.append(" target_page.TOTAL_EXACT_MATCH_WORD_COUNT as \"")
                .append("NOUSEEXACT").append("\" ");
    }

    private void makeJobQuery()
    {
        m_jobQuery = new StringBuffer();
        m_jobQuery.append(m_jobSelect).append(m_from).append(m_where)
                .append(m_order);

        if (c_category.isDebugEnabled())
        {
            c_category.debug(m_jobQuery.toString());            
        }
    }

    /**
     * Fills out the select part of the query
     */
    private void makeSelectClause()
    {
        m_select = new StringBuffer();
        m_select.append("SELECT");
        m_select.append(" job.id as \"JobId\",");
        m_select.append(" job.name as \"JobName\",");
        m_select.append(" locale.ISO_COUNTRY_CODE || '_' || locale.ISO_LANG_CODE as \"Locale\",");
        m_select.append(" source_page.external_page_id as \"PageName\",");
        String low = this.commonBundle.getString("lb_50");
        String med = this.commonBundle.getString("lb_75");
        String medhi = this.commonBundle.getString("lb_85");
        String hi = this.commonBundle.getString("lb_95");
        m_select.append(" target_page.FUZZY_LOW_WORD_COUNT as \"").append(low)
                .append("\",");
        m_select.append(" target_page.FUZZY_MED_WORD_COUNT as \"").append(med)
                .append("\",");
        m_select.append(" target_page.FUZZY_MED_HI_WORD_COUNT as \"")
                .append(medhi).append("\",");
        m_select.append(" target_page.FUZZY_HI_WORD_COUNT as \"").append(hi)
                .append("\",");
        String contexttm = this.commonBundle.getString("lb_context_tm");
        String exact = this.commonBundle.getString("lb_100");
        m_select.append(" target_page.EXACT_CONTEXT_WORD_COUNT as \"")
                .append(contexttm).append("\",");
        m_select.append(" target_page.EXACT_SEGMENT_TM_WORD_COUNT as \"")
                .append(exact).append("\",");
        String inContexttm = this.commonBundle.getString("lb_in_context_tm");
        m_select.append(" target_page.IN_CONTEXT_MATCH_WORD_COUNT as \"")
                .append(inContexttm).append("\",");
        String nomatchrep = this.commonBundle
                .getString("lb_no_match_repetition");
        m_select.append(" target_page.REPETITION_WORD_COUNT as \"")
                .append(nomatchrep).append("\",");
        m_select.append(" target_page.TOTAL_WORD_COUNT as \"Total Word Count\"");
    }

    private void makeNoUseInContextMatch()
    {
        m_select = new StringBuffer();
        m_select.append("SELECT");
        m_select.append(" job.id as \"JobId\",");
        m_select.append(" job.name as \"JobName\",");
        m_select.append(" locale.ISO_COUNTRY_CODE || '_' || locale.ISO_LANG_CODE as \"Locale\",");
        m_select.append(" source_page.external_page_id as \"PageName\",");
        String low = this.commonBundle.getString("lb_50");
        String med = this.commonBundle.getString("lb_75");
        String medhi = this.commonBundle.getString("lb_85");
        String hi = this.commonBundle.getString("lb_95");
        m_select.append(" target_page.FUZZY_LOW_WORD_COUNT as \"").append(low)
                .append("\",");
        m_select.append(" target_page.FUZZY_MED_WORD_COUNT as \"").append(med)
                .append("\",");
        m_select.append(" target_page.FUZZY_MED_HI_WORD_COUNT as \"")
                .append(medhi).append("\",");
        m_select.append(" target_page.FUZZY_HI_WORD_COUNT as \"").append(hi)
                .append("\",");
        String contexttm = this.commonBundle.getString("lb_context_tm");
        String exact = this.commonBundle.getString("lb_100");
        m_select.append(" target_page.EXACT_CONTEXT_WORD_COUNT as \"")
                .append(contexttm).append("\",");
        m_select.append(" target_page.TOTAL_EXACT_MATCH_WORD_COUNT as \"")
                .append(exact).append("\",");
        // String inContexttm = this.commonBundle.getString("lb_in_context_tm");
        // m_select.append(" target_page.IN_CONTEXT_MATCH_WORD_COUNT as \"").append(inContexttm).append("\",");
        String nomatchrep = this.commonBundle
                .getString("lb_no_match_repetition");
        m_select.append(" target_page.REPETITION_WORD_COUNT as \"")
                .append(nomatchrep).append("\",");
        m_select.append(" target_page.TOTAL_WORD_COUNT as \"Total Word Count\"");
    }

    private void makeFromClause()
    {
        m_from = new StringBuffer();
        m_from.append(" FROM target_page, workflow, job, locale, source_page");
    }

    private void makeWhereClause()
    {
        m_where = new StringBuffer();
        m_where.append(" WHERE workflow.job_id = job.id");
        m_where.append(" AND workflow.target_locale_id = locale.id");
        m_where.append(" AND target_page.workflow_iflow_instance_id = workflow.iflow_instance_id");
        m_where.append(" AND source_page.id = target_page.SOURCE_PAGE_ID");
        m_where.append(" AND workflow.state != 'CANCELLED'");
        try
        {
            Vector arg = CompanyWrapper.addCompanyIdBoundArgs(new Vector());
            arg.get(1);
            m_where.append(" AND job.company_id >= ");
            m_where.append(arg.get(0));
            m_where.append(" AND job.company_id <= ");
            m_where.append(arg.get(1));
        }
        catch (PersistenceException e)
        {
            e.printStackTrace();
        }

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

        if (c_category.isDebugEnabled())
        {
            c_category.debug(m_query.toString());            
        }
    }

    private JDBCTableLens executeQuery() throws Exception
    {
        if (headers[0] != null)
        {
            makeSelectClause();
        }
        else
        {
            makeNoUseInContextMatch();
        }

        makeFromClause();
        makeWhereClause();
        makeOrderClause();
        makeQuery();
        Connection conn = null;
        PreparedStatement ps = null;
        JDBCTableLens table = null;

        try
        {
            conn = ConnectionPool.getConnection();
            ps = conn.prepareStatement(m_query.toString());
            ResultSet rs = ps.executeQuery();
            table = new JDBCTableLens(rs);
            ps.close();
        }
        finally
        {
            this.closeStatement(ps);
            this.returnConnection(conn);
        }

        return table;
    }
}
