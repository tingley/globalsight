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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.reports.Constants;
import com.globalsight.reports.datawrap.TermAuditReportDataWrap;
import com.globalsight.reports.util.ReportHandlerFactory;
import com.globalsight.reports.util.ReportsPackage;

public class TermAuditReportHandler extends BasicReportHandler
{
    private static final String MY_MESSAGES = BUNDLE_LOCATION + "termAudit";
    private static final long MILLISECONDS_IN_A_DAY = (long) (24 * 60 * 60 * 1000);
    private static final String QUERY_TERM_AUDIT = "select event_date, username, termbase, target, languages, action, details "
            + "from tb_audit_log "
            + "where "
            + "event_date between ? and ? "
            + "and languages like ?";
    private static final String QUERY_TERM_LANG = "select distinct(name) from tb_language order by name";

    private static final String SYMBOL = "%";
    private static final String ALL = "ALL";
    private static final String LABEL_SUFFIX = ": ";
    private static final String DATEFORMAT = "MM/dd/yyyy";
    private static final String BLANK = " ";
    private static final int PAGESIZE = 22;
    protected ResourceBundle m_bundle = null;
    private TermAuditReportDataWrap reportDataWrap = null;

    /**
     * Initializes the report and sets all the required parameters
     */
    public void init()
    {
        try
        {
            super.init(); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            this.reportKey = Constants.TERMAUDIT_REPORT_KEY;
        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * The entry of the handler. This method will dispatch the
     * HttpServletRequest to different page based on the value of
     * <code>Constants.REPORT_ACT</code>. <code>Constants.REPORT_ACT_PREP</code>
     * means this is request for prepare the parameter data, then system will
     * show the parameter page. <code>Constants.REPORT_ACT_CREATE</code> means
     * create the real report based on the user input data, then system will
     * show the report page.
     */
    public void invokeHandler(HttpServletRequest req, HttpServletResponse res,
            ServletContext p_context) throws Exception
    {
        super.invokeHandler(req, res, p_context);
        String act = (String) req.getParameter(Constants.REPORT_ACT);

        if (Constants.REPORT_ACT_PREP.equalsIgnoreCase(act))
        {
            addMoreReportParameters(req); // prepare data for parameter page
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_PREP), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_CREATE.equalsIgnoreCase(act))
        {
            // just do this at the first time
            cleanSession(theSession);
            createReport(req);
            setTableNavigation(req, theSession, PAGESIZE,
                    Constants.DATARESOURCE, reportKey, reportDataWrap);
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
        else if (Constants.REPORT_ACT_TURNPAGE.equalsIgnoreCase(act))
        {
            // get current page data
            TermAuditReportDataWrap sessionDataWrap = (TermAuditReportDataWrap) getSessionAttribute(
                    theSession, reportKey + Constants.REPORT_DATA_WRAP);
            setTableNavigation(req, theSession, PAGESIZE,
                    Constants.DATARESOURCE, reportKey, sessionDataWrap);
            dispatcherForward(
                    ReportHandlerFactory.getTargetUrl(reportKey
                            + Constants.REPORT_ACT_CREATE), req, res, p_context);
        }
    }

    /**
     * Prepare the data for the parameter of report page <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    private void addMoreReportParameters(HttpServletRequest req)
            throws Exception
    {
        addDateRangeParameters(req);
        addLanguageParameter(req);
    }

    /**
     * Adds the termbase ids
     */
    private void addDateRangeParameters(HttpServletRequest req)
            throws Exception
    {
        Date now = new Date();
        long yesterday = now.getTime() - MILLISECONDS_IN_A_DAY;
        Date start = new Date(yesterday);
        SimpleDateFormat dateForm = new SimpleDateFormat(DATEFORMAT);
        String txtDate = dateForm.format(start);
        req.setAttribute(Constants.PARAM_STARTDATE_LABEL,
                ReportsPackage.getMessage(m_bundle, Constants.PARAM_STARTDATE));
        req.setAttribute(Constants.PARAM_STARTDATE, txtDate);
        txtDate = dateForm.format(now);
        req.setAttribute(Constants.PARAM_ENDDATE_LABEL,
                ReportsPackage.getMessage(m_bundle, Constants.PARAM_ENDDATE));
        req.setAttribute(Constants.PARAM_ENDDATE, txtDate);
    }

    /**
     * Adds the termbase languages
     */
    private void addLanguageParameter(HttpServletRequest req) throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            ArrayList termbaseLangs = new ArrayList();
            ArrayList termbaseLangLables = new ArrayList();
            termbaseLangs.add(ALL);
            termbaseLangLables.add(m_bundle.getString("all"));
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(QUERY_TERM_LANG);
            rs = ps.executeQuery();
            while (rs.next())
            {
                termbaseLangs.add(rs.getString(1));
                termbaseLangLables.add(rs.getString(1));
            }
            // We should use .addList() and use a multi-select list instead
            // to let the user select multiple languages...BUT
            // this widget just does not work in the current inetsoft version
            // with the DHTML viewer (ok for java viewer)
            // So, we're stuck with giving the user one value to choose.
            req.setAttribute(Constants.PARAM_LANGUAGE_LABEL, ReportsPackage
                    .getMessage(m_bundle, Constants.PARAM_LANGUAGE));
            req.setAttribute(Constants.PARAM_LANGUAGE, termbaseLangs);
            req.setAttribute(Constants.PARAM_LANGUAGE_LABELS,
                    termbaseLangLables);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Creates the actual report and fills it with data and messages. Also
     * determines the grouping styles. <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    private void createReport(HttpServletRequest req)
    {
        try
        {
            reportDataWrap = new TermAuditReportDataWrap();
            bindMessages();
            bindData(req);
            setSessionAttribute(theSession, reportKey
                    + Constants.REPORT_DATA_WRAP, reportDataWrap);

        }
        catch (Exception e)
        {
            ReportsPackage.logError(e);
        }
    }

    /**
     * Fills out the messages on the report
     */
    private void bindMessages() throws Exception
    {
        reportDataWrap.setReportTitle(ReportsPackage.getMessage(m_bundle,
                Constants.REPORT_TITLE));
        // set common messages for report, such as header ,footer
        setCommonMessages(reportDataWrap);
    }

    /**
     * Gets the data from the DB and binds it to charts and tables <br>
     * 
     * @param HttpServletRequest
     * @throws Exception
     */
    private void bindData(HttpServletRequest req) throws Exception
    {
        String selectedLang = req.getParameter(Constants.PARAM_SELECTEDLANG);
        String txtDate = req.getParameter(Constants.PARAM_STARTDATE);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATEFORMAT);
        Date startDate = null;
        try
        {
            startDate = simpleDateFormat.parse(txtDate);
        }
        catch (Exception ex)
        {
            startDate = new Date();
        }
        Timestamp startTS = new Timestamp();
        startTS.setDateAndTime(startDate, startDate);

        txtDate = req.getParameter(Constants.PARAM_ENDDATE);
        Date endDate = null;
        try
        {
        	Date date = simpleDateFormat.parse(txtDate);
        	long endLong = date.getTime()+ MILLISECONDS_IN_A_DAY-1;
            endDate = new Date(endLong);
        }
        catch (Exception ex)
        {
            endDate = new Date();
        }
        Timestamp endTS = new Timestamp();
        endTS.setDateAndTime(endDate, endDate);
        addCriteriaFormAtTop(startDate, endDate, selectedLang);
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(QUERY_TERM_AUDIT);
            ps.setTimestamp(1, new java.sql.Timestamp(startTS.getDate()
                    .getTime()));
            ps.setTimestamp(2,
                    new java.sql.Timestamp(endTS.getDate().getTime()));
            ps.setString(3, SYMBOL + selectedLang + SYMBOL);
            rs = ps.executeQuery();
            // set table header
            ResultSetMetaData rsMetaData = rs.getMetaData();
            ArrayList fieldnameList = new ArrayList();
            for (int i = 0; i < rsMetaData.getColumnCount(); i++)
            {
                String name = rsMetaData.getColumnName(i + 1);
                name = ReportsPackage.getMessage(m_bundle, name.toLowerCase());
                fieldnameList.add(name);
            }
            reportDataWrap.setTableHeadList(fieldnameList);
            // set table content
            ArrayList allRowsDataList = new ArrayList();
            while (rs.next())
            {
                ArrayList singleRowDataList = new ArrayList();
                for (int j = 0; j < rsMetaData.getColumnCount(); j++)
                {
                    singleRowDataList.add(rs.getString(j + 1));
                }
                allRowsDataList.add(singleRowDataList);
            }
            reportDataWrap.setDataList(allRowsDataList);
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }

    /**
     * Adds a form at the top of the report
     */
    private void addCriteriaFormAtTop(Date m_startDate, Date m_endDate,
            String m_selectedLang)
    {
        if (ALL.equals(m_selectedLang))
        {
            m_selectedLang = m_bundle.getString("all");
        }
        ArrayList temp = new ArrayList();
        DateFormat formatter = DateFormat.getDateTimeInstance(
                DateFormat.DEFAULT, DateFormat.DEFAULT, theUiLocale);
        temp.add(ReportsPackage.getMessage(m_bundle, Constants.PERIODSTART)
                + BLANK + formatter.format(m_startDate));
        temp.add(ReportsPackage.getMessage(m_bundle, Constants.PERIODEND)
                + BLANK + formatter.format(m_endDate));
        temp.add(ReportsPackage.getMessage(m_bundle, Constants.LANG) + BLANK
                + m_selectedLang);
        reportDataWrap.setCriteriaFormLabel(temp);
    }

}
