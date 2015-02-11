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
import inetsoft.report.lens.JDBCTableLens;
import inetsoft.report.style.Professional;
import inetsoft.sree.RepletException;
import inetsoft.sree.RepletRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.reports.handler.BasicReportHandler;
import com.globalsight.reports.util.ReportsPackage;

/**
 * Presents a report on the Term Audit Log
 */
public class TermAuditReplet extends GlobalSightReplet
{
    private static final String MY_TEMPLATE = "/templates/basicFlowReport.srt";
    private static final String MY_MESSAGES = BasicReportHandler.BUNDLE_LOCATION
            + "termAudit";
    private static final long MILLISECONDS_IN_A_DAY = (24 * 60 * 60 * 1000);

    private static final String QUERY_TERM_AUDIT = "select event_date, "
            + "username, termbase, target, languages, action, details "
            + "from tb_audit_log " + "where " + "event_date between ? and ? "
            + "and languages like ?";

    private static final String QUERY_TERM_LANG = "select distinct name "
            + "from tb_language order by name";

    protected ResourceBundle m_bundle = null;
    private StyleSheet ss = null;
    private String m_selectedLang = null;
    private Date m_startDate = null;
    private Date m_endDate = null;

    // private static final String ALL = "ALL";

    public TermAuditReplet()
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
        try
        {
            super.init(req); // get the common parameters
            m_bundle = ResourceBundle.getBundle(MY_MESSAGES, theUiLocale);
            addMoreRepletParameters();
        }
        catch (RepletException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new RepletException(e.getMessage());
        }
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
    private void addMoreRepletParameters() throws Exception
    {
        addDateRangeParameters();
        addLanguageParameter();
        addRepletParameters(theParameters);
    }

    /**
     * Adds the termbase ids
     */
    protected void addDateRangeParameters() throws Exception
    {
        Date now = new Date();
        long yesterday = now.getTime() - MILLISECONDS_IN_A_DAY;
        Date start = new Date(yesterday);

        theParameters.addDate("startDate", start);
        theParameters.setAlias("startDate", "StartDate");
        theParameters.addTime("startTime", start);
        theParameters.setAlias("startTime", "StartTime");

        theParameters.addDate("endDate", now);
        theParameters.setAlias("endDate", "EndDate");
        theParameters.addTime("endTime", now);
        theParameters.setAlias("endTime", "EndTime");
    }

    /**
     * Adds the termbase languages
     */
    protected void addLanguageParameter() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try
        {
            ArrayList termbaseLangs = new ArrayList();
            termbaseLangs.add(m_bundle.getString("all"));
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(QUERY_TERM_LANG);
            rs = ps.executeQuery();
            while (rs.next())
            {
                termbaseLangs.add(rs.getString(1));
            }

            // We should use .addList() and use a multi-select list instead
            // to let the user select multiple languages...BUT
            // this widget just does not work in the current inetsoft version
            // with the DHTML viewer (ok for java viewer)
            // So, we're stuck with giving the user one value to choose.
            theParameters.addChoice("selectedLang", m_bundle.getString("all"),
                    termbaseLangs.toArray());
            theParameters.setAlias("selectedLang", "Language");
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
     * determines the grouping and chart styles. <br>
     * 
     * @param RepletRequest
     * @return ReportSheet
     * @throws RepletException
     */
    public ReportSheet createReport(RepletRequest req) throws RepletException
    {
        ss = (StyleSheet) theReport;
        try
        {
            bindMessages(req);
            bindData(req);
        }
        catch (Exception e)
        {
            ReportsPackage.logError("Problem creating report", e);
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
        m_selectedLang = req.getString("selectedLang");
        Date startDate = req.getDate("startDate");
        Date startTime = req.getTime("startTime");
        Date endDate = req.getDate("endDate");
        Date endTime = req.getTime("endTime");
        Timestamp startTS = new Timestamp();
        startTS.setDateAndTime(startDate, startTime);
        m_startDate = startTS.getDate();
        Timestamp endTS = new Timestamp();
        endTS.setDateAndTime(endDate, endTime);
        m_endDate = endTS.getDate();
        addCriteriaFormAtTop();
        JDBCTableLens table = dumpAuditEvents();
        Professional prof = new Professional(table);
        prof.setRowBackground(0, GlobalSightReplet.COLOR_TABLE_HDR_BG);
        prof.setFont(GlobalSightReplet.FONT_VERY_SMALL);
        prof.setRowFont(0, GlobalSightReplet.FONT_LABEL);
        prof.setInsets(new java.awt.Insets(1, 1, 1, 1));
        ss.addTable(prof);
    }

    /**
     * Adds a form at the top with the selected termbase <br>
     * 
     * @param termbaseName
     */
    private void addCriteriaFormAtTop()
    {
        ss.setCurrentFont(GlobalSightReplet.FONT_LABEL);
        ss.addText(ReportsPackage.getMessage(m_bundle, "periodStart") + " "
                + m_startDate);
        ss.addNewline(1);
        ss.addText(ReportsPackage.getMessage(m_bundle, "periodEnd") + " "
                + m_endDate);
        ss.addNewline(1);
        ss.addText(ReportsPackage.getMessage(m_bundle, "lang") + " "
                + m_selectedLang);
        ss.setCurrentFont(GlobalSightReplet.FONT_NORMAL);
        ss.addSeparator(StyleConstants.THIN_LINE);
    }

    /**
     * Queries out the audit events and writes them out to the report.
     */
    private JDBCTableLens dumpAuditEvents() throws Exception
    {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try
        {
            c = ConnectionPool.getConnection();
            ps = c.prepareStatement(QUERY_TERM_AUDIT);
            ps.setTimestamp(1, new java.sql.Timestamp(m_startDate.getTime()));
            ps.setTimestamp(2, new java.sql.Timestamp(m_endDate.getTime()));
            ps.setString(3, "%" + m_selectedLang + "%");
            rs = ps.executeQuery();
            JDBCTableLens table = new JDBCTableLens(rs);
            return table;
        }
        finally
        {
            ConnectionPool.silentClose(rs);
            ConnectionPool.silentClose(ps);
            ConnectionPool.silentReturnConnection(c);
        }
    }
}
