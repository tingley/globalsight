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
package com.globalsight.everest.webapp.pagehandler.administration.reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ProjectWorkflowData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.SlaReportDataAssembler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.XlsReportData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.util.IntHolder;

public class SlaXlsReportHelper
{
    private static Logger s_logger = Logger.getLogger(SlaXlsReportHelper.class);
    private HttpServletRequest request = null;
    private ResourceBundle bundle = null;
    private HttpServletResponse response = null;
    private XlsReportData data = null;
    private WritableWorkbook m_workbook = null;
    private static Map<String, ReportsData> m_reportsDataMap = 
            new ConcurrentHashMap<String, ReportsData>();

    public SlaXlsReportHelper(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        this.request = p_request;
        this.response = p_response;
        bundle = PageHandler.getBundle(p_request.getSession());

        String companyName = UserUtil.getCurrentCompanyName(p_request);
        if (!UserUtil.isBlank(companyName))
        {
            CompanyThreadLocal.getInstance().setValue(companyName);
        }

        SlaReportDataAssembler reportDataAssembler = new SlaReportDataAssembler(
                p_request);

        reportDataAssembler.setJobIdList();
        reportDataAssembler.setProjectIdList();
        reportDataAssembler.setStatusList();
        reportDataAssembler.setTargetLangList();
        reportDataAssembler.setDateFormat();
        reportDataAssembler.setProjectData();

        data = reportDataAssembler.getXlsReportData();
    }

    /**
     * Generates the Excel report as a temp file and returns the temp file.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport() throws Exception
    {
        String userId = (String) request.getSession().getAttribute(
                WebAppConstants.USER_NAME);
        List<Long> reportJobIDS = new ArrayList<Long>(data.jobIdList);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataMap(m_reportsDataMap, userId,
                reportJobIDS, null))
        {
            response.sendError(response.SC_NO_CONTENT);
            return;
        }
        // Set m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                null, 0, ReportsData.STATUS_INPROGRESS);

        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);

        m_workbook = Workbook.createWorkbook(response.getOutputStream(),
                settings);

        HashMap projectMap = data.projectMap;

        data.generalSheet = m_workbook.createSheet(bundle.getString("lb_sheet")
                + "1", 0);

        addHeader();

        IntHolder row = new IntHolder(4);
        writeProjectData(projectMap, row);

        m_workbook.write();
        m_workbook.close();
        
        // Set m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                        null, 100, ReportsData.STATUS_FINISHED);
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeader() throws Exception
    {
        WritableSheet theSheet = data.generalSheet;
        // title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        theSheet.addCell(new Label(0, 0, bundle
                .getString("translation_sla_performance"), titleFormat));
        theSheet.setColumnView(0, 20);

        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 9,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        headerFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        int c = 0;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job_id"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 7);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job_name"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 40);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_workflow"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 25);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_language"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 12);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_word_count"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 7);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_current_activity"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 14);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_translation_start_date"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 28);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_translation_due_date"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 28);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_translation_finish_date"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 28);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_on_time"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 28);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_leadtime"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 12);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_actual_performance"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 14);
    }

    private void writeProjectData(HashMap p_projectMap, IntHolder p_row)
            throws Exception
    {
        WritableSheet theSheet = data.generalSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        Collections.sort(projects);
        Iterator projectIter = projects.iterator();

        SimpleDateFormat dateFormat = data.dateFormat;

        String euroJavaNumberFormat = "\u20AC###,###,##0.000;(\u20AC###,###,##0.000)";
        NumberFormat euroNumberFormat = new NumberFormat(euroJavaNumberFormat);
        WritableCellFormat moneyFormat = new WritableCellFormat(
                euroNumberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            Collections.sort(locales);
            Iterator localeIter = locales.iterator();

            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);

                // Job Id
                theSheet.addCell(new Number(col++, row, data.jobId));

                // Job Name
                theSheet.addCell(new Label(col++, row, data.jobName));

                // Workflow Name
                theSheet.addCell(new Label(col++, row, data.workflowName));

                // Lang
                theSheet.addCell(new Label(col++, row, data.targetLang));

                // Word Count
                theSheet.addCell(new Number(col++, row, data.totalWordCount));

                // Current Activity
                theSheet.addCell(new Label(col++, row, data.currentActivityName));

                // Translation Start Date
                theSheet.addCell(new Label(col++, row, dateFormat
                        .format(data.creationDate)));

                // Translation Due Date
                if (data.estimatedTranslateCompletionDate == null)
                {
                    theSheet.addCell(new Label(col++, row, bundle
                            .getString("lb_no_translation")));
                }
                else
                {
                    theSheet.addCell(new Label(col++, row, dateFormat
                            .format(data.estimatedTranslateCompletionDate)));
                }

                // Translation Finish Date
                if (data.actualTranslateCompletionDate == null)
                {
                    theSheet.addCell(new Label(col++, row, ""));
                }
                else
                {
                    theSheet.addCell(new Label(col++, row, dateFormat
                            .format(data.actualTranslateCompletionDate)));
                }

                // On Time
                if (data.actualTranslateCompletionDate == null)
                {
                    theSheet.addCell(new Label(col++, row, ""));
                }
                else if (data.actualTranslateCompletionDate
                        .before(data.estimatedTranslateCompletionDate))
                {
                    theSheet.addCell(new Label(col++, row, bundle
                            .getString("lb_yes")));
                }
                else
                {
                    theSheet.addCell(new Label(col++, row, bundle
                            .getString("lb_no")));
                }

                // Leadtime
                if ((data.leadtime == null) || ("".equals(data.leadtime)))
                {
                    // keep blank if "No translation"
                    col++;
                }
                else
                {
                    theSheet.addCell(new Label(col++, row, data.leadtime));
                }

                // Actual Performance
                if ((data.actualPerformance == null)
                        || ("".equals(data.actualPerformance)))
                {
                    theSheet.addCell(new Label(col++, row, bundle
                            .getString("lb_not_completed")));
                }
                else
                {
                    theSheet.addCell(new Label(col++, row,
                            data.actualPerformance));
                }

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
    }

}
