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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.CurrencyThreadLocal;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ProjectWorkflowData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.ReviewerVendorPoReportDataAssembler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.util.XlsReportData;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.util.IntHolder;

public class ReviewerVendorPoXlsReportHelper
{
    private static Logger s_logger = Logger
            .getLogger("Reports");
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private XlsReportData data = null;
    private WritableWorkbook m_workbook = null;
    String EMEA = CompanyWrapper.getCurrentCompanyName();

    public ReviewerVendorPoXlsReportHelper(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        this.request = p_request;
        this.response = p_response;

        String currency = request.getParameter("currency");
        CurrencyThreadLocal.setCurrency(currency);

        ReviewerVendorPoReportDataAssembler reportDataAssembler = new ReviewerVendorPoReportDataAssembler(
                p_request);

        reportDataAssembler.setProjectIdList();
        reportDataAssembler.setTargetLangList();
        reportDataAssembler.setActivityNameList();
        reportDataAssembler.setJobStateList();
        // set all the jobs that were originally imported with the wrong project
        // the users want to pretend that these jobs are in this project
        reportDataAssembler.setJobsInWrongProject();

        // set ProjectDate for report
        boolean recalculateFinishedWorkflow = false;
        String recalcParam = request.getParameter("recalc");
        if (recalcParam != null && recalcParam.length() > 0)
        {
            recalculateFinishedWorkflow = Boolean.valueOf(recalcParam)
                    .booleanValue();
        }
        reportDataAssembler.setProjectData(recalculateFinishedWorkflow);

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
        ResourceBundle bundle = PageHandler.getBundle(request.getSession());
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);

        m_workbook = Workbook.createWorkbook(response.getOutputStream(),
                settings);

        HashMap projectMap = data.projectMap;

        data.dellSheet = m_workbook.createSheet(bundle
                .getString("lb_globalsight_matches"), 0);
        data.tradosSheet = m_workbook.createSheet(bundle
                .getString("jobinfo.tradosmatches"), 1);

        addHeaderForDellMatches();
        addHeaderForTradosMatches();

        IntHolder row = new IntHolder(4);
        writeProjectDataForDellMatches(projectMap, row);

        row = new IntHolder(4);
        writeProjectDataForTradosMatches(projectMap, row);

        WritableSheet paramsSheet = m_workbook.createSheet(bundle
                .getString("lb_criteria"), 2);
        paramsSheet.addCell(new Label(0, 0, bundle
                .getString("lb_report_criteria")));
        paramsSheet.setColumnView(0, 50);

        if (data.wantsAllProjects)
        {
            paramsSheet.addCell(new Label(0, 1, bundle
                    .getString("lb_selected_projects")
                    + " " + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(0, 1, bundle
                    .getString("lb_selected_projects")));
            Iterator iter = data.projectIdList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                Long pid = (Long) iter.next();
                String projectName = "??";

                try
                {
                    Project p = ServerProxy.getProjectHandler().getProjectById(
                            pid.longValue());
                    projectName = p.getName();
                }
                catch (Exception e)
                {
                    s_logger.error("Failed to get project." + e);
                }

                String v = projectName + " ("
                        + bundle.getString("lb_report_id") + "="
                        + pid.toString() + ")";
                paramsSheet.addCell(new Label(0, r, v));
                r++;
            }
        }

        // add the date criteria
        String fromMsg = getMessage(JobSearchConstants.CREATION_START,
                JobSearchConstants.CREATION_START_OPTIONS);

        String untilMsg = getMessage(JobSearchConstants.CREATION_END,
                JobSearchConstants.CREATION_END_OPTIONS);

        paramsSheet.addCell(new Label(1, 1, bundle.getString("lb_from") + ":"));
        paramsSheet.addCell(new Label(1, 2, fromMsg));
        paramsSheet.setColumnView(1, 20);
        paramsSheet
                .addCell(new Label(2, 1, bundle.getString("lb_until") + ":"));
        paramsSheet.addCell(new Label(2, 2, untilMsg));
        paramsSheet.setColumnView(2, 20);

        // add the target lang criteria
        paramsSheet.setColumnView(3, 20);
        if (data.wantsAllTargetLangs)
        {
            paramsSheet.addCell(new Label(3, 1, bundle
                    .getString("lb_selected_langs")
                    + " " + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(3, 1, bundle
                    .getString("lb_selected_langs")));
            Iterator iter = data.targetLangList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String lang = (String) iter.next();
                paramsSheet.addCell(new Label(3, r++, lang));
            }
        }
        paramsSheet.setColumnView(4, 20);
        if (data.allJobStatus)
        {
            paramsSheet.addCell(new Label(4, 1, bundle
                    .getString("lb_job_status")
                    + ": " + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(4, 1, bundle
                    .getString("lb_job_status")
                    + ":"));
            Iterator iter = data.jobStatusList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String jobStatus = (String) iter.next();
                paramsSheet.addCell(new Label(4, r, jobStatus));
                r++;
            }
        }
        paramsSheet.setColumnView(5, 30);
        if (data.allActivities)
        {
            paramsSheet.addCell(new Label(5, 1, bundle
                    .getString("lb_activity_name")
                    + ": " + bundle.getString("all")));
        }
        else
        {
            paramsSheet.addCell(new Label(5, 1, bundle
                    .getString("lb_activity_name")
                    + ":"));
            Iterator iter = data.activityNameList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String activityName = (String) iter.next();
                Activity activity = (Activity) ServerProxy.getJobHandler()
                        .getActivity(activityName);
                paramsSheet.addCell(new Label(5, r, activity.getDisplayName()));
                r++;
            }
        }

        m_workbook.write();
        m_workbook.close();
    }

    private String getMessage(String creationLabel, String creationOptionLabel)
    {
        ResourceBundle bundle = PageHandler
                .getBundle(request.getSession(false));
        String condition = request.getParameter(creationOptionLabel);

        String result = "N/A";
        result = request.getParameter(creationLabel) + " ";

        if (SearchCriteriaParameters.NOW.equals(condition))
            result = result + bundle.getString("lb_now");
        else if (SearchCriteriaParameters.DAYS_AGO.equals(condition))
            result = result + bundle.getString("lb_days_ago");
        else if (SearchCriteriaParameters.HOURS_AGO.equals(condition))
            result = result + bundle.getString("lb_hours_ago");
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(condition))
            result = result + bundle.getString("lb_weeks_ago");
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(condition))
            result = result + bundle.getString("lb_months_ago");

        return result;
    }

    /**
     * Adds the table header for the Dell Matches sheet
     * 
     */
    private void addHeaderForDellMatches() throws Exception
    {
        ResourceBundle bundle = PageHandler
                .getBundle(request.getSession(false));
        WritableSheet theSheet = data.dellSheet;
        // title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        theSheet.addCell(new Label(0, 0, EMEA + " "
                + bundle.getString("lb_reviewer_po"), titleFormat));
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
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job_name"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_po_number_report"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_description"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 15);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_creation_date"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_activity_name"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 20);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("lb_accepted_reviewer_date"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_lang"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_word_count"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle
                .getString("jobinfo.tmmatches.invoice"), headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_tracking"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
    }

    /**
     * Adds the table header for the Trados Matches sheet
     * 
     */
    private void addHeaderForTradosMatches() throws Exception
    {
        ResourceBundle bundle = PageHandler
                .getBundle(request.getSession(false));
        WritableSheet theSheet = data.tradosSheet;
        // title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.ARIAL, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);

        theSheet.addCell(new Label(0, 0, EMEA + " "
                + bundle.getString("lb_reviewer_po"), titleFormat));
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

        WritableCellFormat langFormat = new WritableCellFormat(headerFont);
        langFormat.setWrap(true);
        langFormat.setBackground(jxl.format.Colour.GRAY_25);
        langFormat.setShrinkToFit(false);
        langFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        langFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountFormat = new WritableCellFormat(headerFont);
        wordCountFormat.setWrap(true);
        wordCountFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountFormat.setShrinkToFit(false);
        wordCountFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountValueFormat = new WritableCellFormat(
                headerFont);
        wordCountValueFormat.setWrap(true);
        wordCountValueFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueFormat.setShrinkToFit(false);
        wordCountValueFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THIN);

        WritableCellFormat wordCountValueRightFormat = new WritableCellFormat(
                headerFont);
        wordCountValueRightFormat.setWrap(true);
        wordCountValueRightFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueRightFormat.setShrinkToFit(false);
        wordCountValueRightFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueRightFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueRightFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat wordCountValueLeftRightFormat = new WritableCellFormat(
                headerFont);
        wordCountValueLeftRightFormat.setWrap(true);
        wordCountValueLeftRightFormat.setBackground(jxl.format.Colour.GRAY_25);
        wordCountValueLeftRightFormat.setShrinkToFit(false);
        wordCountValueLeftRightFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        wordCountValueLeftRightFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueLeftRightFormat.setBorder(jxl.format.Border.LEFT,
                jxl.format.BorderLineStyle.THICK);
        wordCountValueLeftRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        int c = 0;
        theSheet
                .addCell(new Label(0, 1, bundle.getString("lb_desp_file_list")));
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job_id"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_job"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_po_number_report"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_description"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        theSheet.setColumnView(c, 15);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_creation_date"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_lang"),
                langFormat));
        theSheet.mergeCells(c, 2, c, 3);
        c++;
        theSheet.addCell(new Label(c, 2, bundle.getString("lb_word_counts"),
                wordCountFormat));
        if (data.headers[0] != null && data.headers[1] != null)
        {
            theSheet.mergeCells(c, 2, c + 8, 2);
        }
        else
        {
            if (data.headers[0] != null || data.headers[1] != null)
            {
                theSheet.mergeCells(c, 2, c + 7, 2);
            }
            else
            {
                theSheet.mergeCells(c, 2, c + 6, 2);
            }
        }

        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tradosmatches.invoice.per100matches"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_95_99"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_85_94"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_75_84") + "*",
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_no_match"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("lb_repetition_word_cnt"), wordCountValueFormat));
        if (data.headers[0] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }
        if (data.headers[1] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_context_matches"), wordCountValueFormat));
        }
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_total"),
                wordCountValueLeftRightFormat));

        theSheet.addCell(new Label(c, 2, bundle
                .getString("jobinfo.tmmatches.invoice"), wordCountFormat));
        if (data.headers[0] != null && data.headers[1] != null)
        {
            theSheet.mergeCells(c, 2, c + 10, 2);
        }
        else
        {
            if (data.headers[0] != null || data.headers[1] != null)
            {
                theSheet.mergeCells(c, 2, c + 9, 2);
            }
            else
            {
                theSheet.mergeCells(c, 2, c + 8, 2);
            }
        }

        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tradosmatches.invoice.per100matches"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_95_99"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_85_94"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_75_84") + "*",
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_no_match"),
                wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("lb_repetition_word_cnt"), wordCountValueFormat));
        if (data.headers[0] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_in_context_tm"), wordCountValueFormat));
        }
        if (data.headers[1] != null)
        {
            theSheet.addCell(new Label(c++, 3, bundle
                    .getString("lb_context_matches"), wordCountValueFormat));
        }
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("lb_translation_total"), wordCountValueFormat));
        theSheet.addCell(new Label(c++, 3, bundle.getString("lb_review"),
                wordCountValueRightFormat));
        theSheet.addCell(new Label(c++, 3, bundle
                .getString("jobinfo.tmmatches.invoice.jobtotal"),
                wordCountValueRightFormat));

        theSheet.addCell(new Label(c, 2, bundle.getString("lb_tracking"),
                headerFormat));
        theSheet.mergeCells(c, 2, c, 3);
    }

    private void writeProjectDataForDellMatches(HashMap p_projectMap,
            IntHolder p_row) throws Exception
    {
        ResourceBundle bundle = PageHandler
                .getBundle(request.getSession(false));
        WritableSheet theSheet = data.dellSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        Collections.sort(projects);
        Iterator projectIter = projects.iterator();

        WritableCellFormat dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
        dateFormat.setWrap(false);
        dateFormat.setShrinkToFit(false);

        NumberFormat numberFormat = new NumberFormat(CurrencyThreadLocal
                .getMoneyFormatString());
        WritableCellFormat moneyFormat = new WritableCellFormat(numberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);

        WritableFont wrongJobFont = new WritableFont(WritableFont.TIMES, 11,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.GRAY_50);
        WritableCellFormat wrongJobFormat = new WritableCellFormat(wrongJobFont);

        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            boolean isWrongJob = data.wrongJobNames.contains(jobName);
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
                // Job Name
                if (isWrongJob)
                {
                    theSheet.addCell(new Number(col++, row, data.jobId,
                            wrongJobFormat));
                    theSheet.addCell(new Label(col++, row, data.jobName,
                            wrongJobFormat));
                }
                else
                {
                    theSheet.addCell(new Number(col++, row, data.jobId));
                    theSheet.addCell(new Label(col++, row, data.jobName));
                }
                theSheet.setColumnView(col - 2, 5);
                theSheet.setColumnView(col - 1, 50);

                // PO Number
                theSheet.addCell(new Label(col++, row, ""));

                // Description
                theSheet.addCell(new Label(col++, row, data.projectDesc));
                theSheet.setColumnView(col - 1, 22);

                // Create Date
                theSheet.addCell(new DateTime(col++, row, data.creationDate,
                        dateFormat));
                theSheet.setColumnView(col - 1, 15);
                
                theSheet
                        .addCell(new Label(col++, row, data.currentActivityName));

                // Accepted Reviewer Date
                if (data.dellReviewActivityState == Task.STATE_REJECTED)
                {
                    theSheet.addCell(new Label(col++, row, bundle
                            .getString("lb_rejected")));
                }
                else if (data.acceptedReviewerDate == null)
                {
                    theSheet.addCell(new Label(col++, row, bundle
                            .getString("lb_not_accepted")));
                }
                else
                {
                    theSheet.addCell(new DateTime(col++, row,
                            data.acceptedReviewerDate, dateFormat));
                }
                theSheet.setColumnView(col - 1, 22);

                // Lang
                theSheet.addCell(new Label(col++, row, data.targetLang));

                // Word Count
                theSheet
                        .addCell(new Number(col++, row, data.dellTotalWordCount));

                // Invoice
                if ((data.dellReviewActivityState == Task.STATE_REJECTED)
                        || (data.acceptedReviewerDate == null))
                {
                    theSheet.addCell(new Number(col++, row, 0, moneyFormat));
                }
                else
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.dellTotalWordCountCostForDellReview),
                            moneyFormat));
                }

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForDellMatches(p_row);
    }

    /** Adds totals */
    private void addTotalsForDellMatches(IntHolder p_row) throws Exception
    {
        ResourceBundle bundle = PageHandler
                .getBundle(request.getSession(false));
        WritableSheet theSheet = data.dellSheet;

        WritableFont subTotalFont = new WritableFont(WritableFont.ARIAL, 10,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);

        WritableCellFormat subTotalFormat = new WritableCellFormat(subTotalFont);
        subTotalFormat.setWrap(true);
        subTotalFormat.setShrinkToFit(false);
        subTotalFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        subTotalFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        subTotalFormat.setBackground(jxl.format.Colour.GRAY_25);

        NumberFormat numberFormat = new NumberFormat(CurrencyThreadLocal
                .getMoneyFormatString());
        WritableCellFormat moneyFormat = new WritableCellFormat(numberFormat);
        moneyFormat.setWrap(true);
        moneyFormat.setShrinkToFit(false);
        moneyFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBackground(jxl.format.Colour.GRAY_25);

        // Totals
        int totalsRow = p_row.getValue() + 1; // skip a row
        theSheet.addCell(new Label(0, totalsRow, bundle.getString("lb_totals"),
                subTotalFormat));
        theSheet.mergeCells(0, totalsRow, 7, totalsRow);

        int lastRow = p_row.getValue() - 2;
        int c = 8;
        // Word Count
        theSheet.addCell(new Formula(c++, totalsRow, "SUM(I5" + ":I" + lastRow
                + ")", subTotalFormat));
        // Invoice
        theSheet.addCell(new Formula(c++, totalsRow, "SUM(J5" + ":J" + lastRow
                + ")", moneyFormat));
        // add an extra column for Dell Tracking Use
        theSheet.addCell(new Label(c++, totalsRow, "", moneyFormat));
    }

    private void writeProjectDataForTradosMatches(HashMap p_projectMap,
            IntHolder p_row) throws Exception
    {
        WritableSheet theSheet = data.tradosSheet;
        ArrayList projects = new ArrayList(p_projectMap.keySet());
        Collections.sort(projects);
        Iterator projectIter = projects.iterator();

        WritableCellFormat dateFormat = new WritableCellFormat(
                DateFormats.DEFAULT);
        dateFormat.setWrap(false);
        dateFormat.setShrinkToFit(false);

        NumberFormat numberFormat = new NumberFormat(CurrencyThreadLocal
                .getMoneyFormatString());
        WritableCellFormat moneyFormat = new WritableCellFormat(numberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);

        WritableCellFormat wordCountValueRightFormat = new WritableCellFormat();
        wordCountValueRightFormat.setWrap(true);
        wordCountValueRightFormat.setShrinkToFit(false);
        wordCountValueRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat moneyRightFormat = new WritableCellFormat(
                moneyFormat);
        moneyRightFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableCellFormat langFormat = new WritableCellFormat();
        langFormat.setWrap(true);
        langFormat.setShrinkToFit(false);
        langFormat.setBorder(jxl.format.Border.RIGHT,
                jxl.format.BorderLineStyle.THICK);

        WritableFont wrongJobFont = new WritableFont(WritableFont.TIMES, 11,
                WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.GRAY_50);
        WritableCellFormat wrongJobFormat = new WritableCellFormat(wrongJobFont);
        
        while (projectIter.hasNext())
        {
            String jobName = (String) projectIter.next();
            boolean isWrongJob = data.wrongJobNames.contains(jobName);
            HashMap localeMap = (HashMap) p_projectMap.get(jobName);
            ArrayList locales = new ArrayList(localeMap.keySet());
            Collections.sort(locales);
            HashMap<String, String> jobLocale = new HashMap<String, String>();
            Iterator localeIter = locales.iterator();
            while (localeIter.hasNext())
            {
                int row = p_row.getValue();
                int col = 0;
                String localeName = (String) localeIter.next();
                ProjectWorkflowData data = (ProjectWorkflowData) localeMap
                        .get(localeName);
                
                if (jobLocale.size() != 0
                        && jobLocale.get(jobName + data.targetLang) != null)
                {
                    continue;
                }
                else
                {
                    jobLocale.put(jobName + data.targetLang, jobName
                            + data.targetLang);
                }
                if (isWrongJob)
                {
                    theSheet.addCell(new Number(col++, row, data.jobId,
                            wrongJobFormat));
                    theSheet.addCell(new Label(col++, row, data.jobName,
                            wrongJobFormat));
                }
                else
                {
                    theSheet.addCell(new Number(col++, row, data.jobId));
                    theSheet.addCell(new Label(col++, row, data.jobName));
                }
                theSheet.setColumnView(col - 2, 5);
                theSheet.setColumnView(col - 1, 50);
                theSheet.addCell(new Label(col++, row, "")); // PO number
                theSheet.addCell(new Label(col++, row, data.projectDesc));
                theSheet.setColumnView(col - 1, 22);
                /* data.creationDate.toString())); */
                theSheet.addCell(new DateTime(col++, row, data.creationDate,
                        dateFormat));
                theSheet.setColumnView(col - 1, 15);
                theSheet.addCell(new Label(col++, row, data.targetLang));

                theSheet
                        .addCell(new Number(col++, row, data.trados100WordCount));
                int numwidth = 10;
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.trados95to99WordCount));
                theSheet.setColumnView(col - 1, numwidth);

                theSheet.addCell(new Number(col++, row,
                        data.trados85to94WordCount));
                theSheet.setColumnView(col - 1, numwidth);

                theSheet.addCell(new Number(col++, row,
                        data.trados75to84WordCount));
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.tradosNoMatchWordCount));
                theSheet.setColumnView(col - 1, numwidth);
                theSheet.addCell(new Number(col++, row,
                        data.tradosRepsWordCount));
                theSheet.setColumnView(col - 1, numwidth);
                if (this.data.headers[0] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            data.tradosInContextWordCount));
                    theSheet.setColumnView(col - 1, numwidth);
                }

                if (this.data.headers[1] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            data.tradosContextWordCount));
                    theSheet.setColumnView(col - 1, numwidth);
                }
                theSheet.addCell(new Number(col++, row,
                        data.tradosTotalWordCount));
                theSheet.setColumnView(col - 1, numwidth);

                int moneywidth = 12;
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados100WordCountCost), moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados95to99WordCountCost), moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados85to94WordCountCost), moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.trados75to84WordCountCost), moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet
                        .addCell(new Number(col++, row,
                                asDouble(data.tradosNoMatchWordCountCost),
                                moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosRepsWordCountCost), moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                if (this.data.headers[0] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.tradosInContextWordCountCost),
                            moneyFormat));
                    theSheet.setColumnView(col - 1, moneywidth);
                }
                if (this.data.headers[1] != null)
                {
                    theSheet.addCell(new Number(col++, row,
                            asDouble(data.tradosContextWordCountCost),
                            moneyFormat));
                    theSheet.setColumnView(col - 1, moneywidth);
                }
                // theSheet.addCell(new
                // Number(col++,row,asDouble(data.tradosTotalWordCountCost),moneyFormat));
                // theSheet.setColumnView(col -1,moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosTotalWordCountCostForTranslation),
                        moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosTotalWordCountCostForDellReview),
                        moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);
                theSheet.addCell(new Number(col++, row,
                        asDouble(data.tradosTotalWordCountCost), moneyFormat));
                theSheet.setColumnView(col - 1, moneywidth);

                p_row.inc();
            }
        }

        p_row.inc();
        p_row.inc();
        addTotalsForTradosMatches(p_row);
    }

    /** Adds the totals and sub-total formulas */
    private void addTotalsForTradosMatches(IntHolder p_row) throws Exception
    {
        ResourceBundle bundle = PageHandler
                .getBundle(request.getSession(false));
        WritableSheet theSheet = data.tradosSheet;
        int row = p_row.getValue() + 1; // skip a row
        WritableFont subTotalFont = new WritableFont(WritableFont.ARIAL, 10,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);

        WritableCellFormat subTotalFormat = new WritableCellFormat(subTotalFont);
        subTotalFormat.setWrap(true);
        subTotalFormat.setShrinkToFit(false);
        subTotalFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        subTotalFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        String title = bundle.getString("lb_totals");
        subTotalFormat.setBackground(jxl.format.Colour.GRAY_25);

        String numberFormatString = CurrencyThreadLocal.getMoneyFormatString();
        NumberFormat numberFormat = new NumberFormat(numberFormatString);
        WritableCellFormat moneyFormat = new WritableCellFormat(numberFormat);
        moneyFormat.setWrap(false);
        moneyFormat.setShrinkToFit(false);
        moneyFormat.setBorder(jxl.format.Border.TOP,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBorder(jxl.format.Border.BOTTOM,
                jxl.format.BorderLineStyle.THIN);
        moneyFormat.setBackground(jxl.format.Colour.GRAY_25);

        theSheet.addCell(new Label(0, row, title, subTotalFormat));
        theSheet.mergeCells(0, row, 5, row);
        int lastRow = p_row.getValue() - 2;

        // add in word count totals
        int c = 6;
        // word counts
        if (data.headers[0] != null && data.headers[1] != null)
        {

            theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(J5:J" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(K5:K" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(L5:L" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(M5:M" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(N5:N" + lastRow + ")",
                    subTotalFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O" + lastRow
                    + ")", moneyFormat));

            // word count costs

            theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(U5" + ":U" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(V5" + ":V" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(W5" + ":W" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(X5" + ":X" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(Y5" + ":Y" + lastRow
                    + ")", moneyFormat));
            theSheet.addCell(new Formula(c++, row, "SUM(Z5" + ":Y" + lastRow
                    + ")", moneyFormat));

        }
        else
        {
            if (data.headers[0] != null || data.headers[1] != null)
            {
                theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(J5:J" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(K5:K" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(L5:L" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(M5:M" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(N5:N" + lastRow
                        + ")", subTotalFormat));

                // word count costs
                theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(U5" + ":U"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(V5" + ":V"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(W5" + ":W"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(X5" + ":X"
                        + lastRow + ")", moneyFormat));
            }
            else
            {
                theSheet.addCell(new Formula(c++, row, "SUM(G5:G" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(H5:H" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(I5:I" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(J5:J" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(K5:K" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(L5:L" + lastRow
                        + ")", subTotalFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(M5:M" + lastRow
                        + ")", subTotalFormat));

                // word count costs
                theSheet.addCell(new Formula(c++, row, "SUM(N5:N" + lastRow
                        + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(O5" + ":O"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(P5" + ":P"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(Q5" + ":Q"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(R5" + ":R"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(S5" + ":S"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(T5" + ":T"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(U5" + ":U"
                        + lastRow + ")", moneyFormat));
                theSheet.addCell(new Formula(c++, row, "SUM(V5" + ":V"
                        + lastRow + ")", moneyFormat));
            }

        }

        // add an extra column for Dell Tracking Use
        theSheet.addCell(new Label(c++, row, "", moneyFormat));
    }

    private double asDouble(BigDecimal d)
    {
        // BigDecimal v = d.setScale(SCALE_EXCEL,BigDecimal.ROUND_HALF_UP);
        return d.doubleValue();
    }

}
