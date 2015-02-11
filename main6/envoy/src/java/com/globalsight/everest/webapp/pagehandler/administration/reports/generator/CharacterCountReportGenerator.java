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
package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.CellFormat;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

/**
 * Character Count Report Generator
 */
public class CharacterCountReportGenerator implements ReportGenerator
{
    protected ResourceBundle m_bundle = null;
    protected Locale m_uiLocale = null;
    protected String m_companyName = "";
    protected List<Long> m_jobIDS = new ArrayList<Long>();
    protected List<GlobalSightLocale> m_targetLocales = new ArrayList<GlobalSightLocale>();
    String m_userId;

    protected WritableCellFormat m_contentFormat = null;
    protected WritableCellFormat m_headerFormat = null;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;

    /**
     * Constructor. Create a helper to generate the report
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public CharacterCountReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_bundle = PageHandler.getBundle(p_request.getSession());
        m_uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        if (m_uiLocale == null)
        {
            m_uiLocale = Locale.US;
        }

        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);

        if (p_request.getParameter(ReportConstants.JOB_IDS) != null)
        {
            m_jobIDS = ReportHelper.getListOfLong(p_request
                    .getParameter(ReportConstants.JOB_IDS));
            GlobalSightLocaleComparator comparator = new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.ISO_CODE, m_uiLocale);
            m_targetLocales = ReportHelper.getTargetLocaleList(p_request
                    .getParameterValues(ReportConstants.TARGETLOCALE_LIST),
                    comparator);
        }
    }

    /**
     * Send Report Files to Client
     * 
     * @param p_jobIDS
     *            Job ID List
     * @param p_targetLocales
     *            Target Locales List
     * @param p_companyName
     *            Company Name, used for checking permission.
     * @param p_response
     *            HttpServletResponse
     * @throws Exception
     */
    public void sendReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = m_jobIDS;
            p_targetLocales = m_targetLocales;
        }
        else if (m_jobIDS == null || m_jobIDS.size() == 0)
        {
            m_jobIDS = p_jobIDS;
        }

        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    /**
     * Generates Excel reports
     * 
     * @param p_jobIDS
     *            Job ID List
     * @param p_targetLocales
     *            Target Locales List
     * @param p_companyName
     *            Company Name
     * @throws Exception
     */
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = m_jobIDS;
            p_targetLocales = m_targetLocales;
        }
        else if (m_jobIDS == null || m_jobIDS.size() == 0)
        {
            m_jobIDS = p_jobIDS;
        }
        List<File> workBooks = new ArrayList<File>();
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        int finishedJobNum = 0;
        for (long jobID : p_jobIDS)
        {
            // Cancel generate reports.
            if (isCancelled())
                return null;

            Job job = ServerProxy.getJobHandler().getJobById(jobID);
            if (job == null)
                continue;

            File file = ReportHelper.getXLSReportFile(getReportType(), job);
            WritableWorkbook workbook = Workbook.createWorkbook(file, settings);
            m_contentFormat = m_headerFormat = null;
            createReport(workbook, job, p_targetLocales);
            workbook.write();
            workbook.close();
            workBooks.add(file);

            // Sets Reports Percent.
            setPercent(++finishedJobNum);
        }

        return workBooks.toArray(new File[workBooks.size()]);
    }

    /**
     * Create one excel file report per job. Within each excel spreadsheet, use
     * one tab per target language.
     * 
     * @param p_workbook
     *            excel file
     * @param p_job
     *            job
     * @param p_targetLangIDS
     *            target language id list
     * @throws Exception
     */
    private void createReport(WritableWorkbook p_workbook, Job p_job,
            List<GlobalSightLocale> p_targetLangIDS) throws Exception
    {
        List<GlobalSightLocale> jobTL = ReportHelper.getTargetLocals(p_job);
        for (GlobalSightLocale tgtL : p_targetLangIDS)
        {
            if (!jobTL.contains(tgtL))
                continue;
            WritableSheet sheet = p_workbook.createSheet(tgtL.toString(),
                    p_workbook.getNumberOfSheets());
            // Write Header
            addHeader(sheet);
            // Write Language Information
            writeLanguageInfo(sheet, p_job, tgtL.getDisplayName(m_uiLocale));
            // Write Segments Information
            writeCharacterCountSegmentInfo(sheet, p_job, tgtL.getDisplayName(),
                    "");
        }
    }

    /**
     * Add header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addHeader(WritableSheet p_sheet) throws Exception
    {
        // add title
        addTitle(p_sheet, m_bundle.getString("character_count_report"));
        // add job header
        addLanguageHeader(p_sheet);
        // add segment info header
        addSegmentHeader(p_sheet);
    }

    private void addTitle(WritableSheet p_sheet, String p_title)
            throws Exception
    {
        // Title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.TIMES, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);
        p_sheet.addCell(new Label(0, 0, p_title, titleFormat));
        p_sheet.setColumnView(0, 40);
    }

    /**
     * Add job header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addLanguageHeader(WritableSheet p_sheet) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_language"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_target_language"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("jobinfo.jobid"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
    }

    /**
     * Add segment header to the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void addSegmentHeader(WritableSheet p_sheet) throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_segment_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_character_count"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_target_segment"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_target_character_count"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
    }

    /**
     * 
     * @return format of the sheet header
     * @throws Exception
     */
    private CellFormat getHeaderFormat() throws Exception
    {
        if (m_headerFormat == null)
        {
            WritableFont headerFont = new WritableFont(WritableFont.TIMES, 11,
                    WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                    jxl.format.Colour.BLACK);
            WritableCellFormat format = new WritableCellFormat(headerFont);
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setBackground(jxl.format.Colour.GRAY_25);
            format.setBorder(jxl.format.Border.ALL,
                    jxl.format.BorderLineStyle.THIN, jxl.format.Colour.BLACK);

            m_headerFormat = format;
        }
        return m_headerFormat;
    }

    /**
     * Write the Job information into the header
     * 
     * @param p_sheet
     *            the sheet
     * @param p_jobId
     *            the job id
     * @param p_targetLang
     *            the displayed target language
     * @param p_dateFormat
     *            the date format
     * @throws Exception
     */
    private void writeLanguageInfo(WritableSheet p_sheet, Job p_job,
            String p_targetLang) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        // Write Source Language into the sheet
        p_sheet.addCell(new Label(col++, row, p_job.getSourceLocale()
                .getDisplayName(m_uiLocale), getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Write Target Language into the sheet
        p_sheet.addCell(new Label(col++, row, p_targetLang, getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Write Job ID into the sheet
        p_sheet.addCell(new Label(col++, row, String.valueOf(p_job.getId()),
                getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);
    }

    /**
     * 
     * @return format of the report content
     * @throws Exception
     */
    private WritableCellFormat getContentFormat() throws Exception
    {
        if (m_contentFormat == null)
        {
            WritableCellFormat format = new WritableCellFormat();
            format.setWrap(true);
            format.setShrinkToFit(false);
            format.setAlignment(Alignment.LEFT);
            format.setVerticalAlignment(VerticalAlignment.CENTRE);

            m_contentFormat = format;
        }
        return m_contentFormat;
    }

    /**
     * Write segment information into each row of the sheet
     * 
     * @param p_sheet
     *            the sheet
     * @param p_jobId
     *            the job id
     * @param p_targetLang
     *            the displayed target language
     * @param p_srcPageId
     *            the source page id
     * @throws Exception
     */
    private void writeCharacterCountSegmentInfo(WritableSheet p_sheet,
            Job p_job, String p_targetLang, String p_srcPageId)
            throws Exception
    {
        Collection wfs = p_job.getWorkflows();
        Iterator it = wfs.iterator();
        Vector targetPages = new Vector();

        String companyId = p_job.getCompanyId();

        while (it.hasNext())
        {
            Workflow workflow = (Workflow) it.next();

            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
                    || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (p_targetLang
                    .equals(workflow.getTargetLocale().getDisplayName()))
            {
                targetPages = workflow.getTargetPages();
            }
        }
        if (targetPages.isEmpty())
        {
            // If no corresponding target page exists, set the cell blank
            // writeCommentsAnalysisBlank(p_sheet);
        }
        else
        {
            int row = SEGMENT_START_ROW;
            String sourceSegmentString = null;
            String targetSegmentString = null;

            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();

                if (!"".equals(p_srcPageId)
                        && !p_srcPageId.equals(String.valueOf(sourcePage
                                .getId())))
                {
                    // ignore the source pages not equal to the one
                    // if the request comes from pop up editor
                    continue;
                }

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = getPageTuvs(sourcePage);
                List targetTuvs = getPageTuvs(targetPage);

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    sourceSegmentString = sourceTuv.getGxmlElement()
                            .getTextValue();
                    targetSegmentString = targetTuv.getGxmlElement()
                            .getTextValue();

                    // Segment id
                    p_sheet.addCell(new Number(col++, row, sourceTuv.getTu(
                            companyId).getId(), getContentFormat()));
                    p_sheet.setColumnView(col - 1, 20);

                    // Source segment
                    p_sheet.addCell(new Label(col++, row, sourceSegmentString,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 40);

                    // Source Character count
                    p_sheet.addCell(new Label(col++, row, String
                            .valueOf(sourceSegmentString.length()),
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);

                    // Target segment
                    p_sheet.addCell(new Label(col++, row, targetSegmentString,
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 40);

                    // Target Character count
                    p_sheet.addCell(new Label(col++, row, String
                            .valueOf(targetSegmentString.length()),
                            getContentFormat()));
                    p_sheet.setColumnView(col - 1, 30);
                    row++;
                }
            }
        }
    }

    /**
     * Set the cell blank
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void writeCommentsAnalysisBlank(WritableSheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = 7;
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
        p_sheet.addCell(new Label(col++, row, ""));
    }

    /**
     * Get tuvs of source page from database. Return a list
     * 
     * @param p_sourcePage
     *            source page
     * @throws Exception
     */
    private List getPageTuvs(SourcePage p_sourcePage) throws Exception
    {
        return new ArrayList(ServerProxy.getTuvManager()
                .getSourceTuvsForStatistics(p_sourcePage));
    }

    /**
     * Get tuvs of target page from database. Return a list
     * 
     * @param p_targetPage
     *            target page
     * @throws Exception
     */
    private List getPageTuvs(TargetPage p_targetPage) throws Exception
    {
        return new ArrayList(ServerProxy.getTuvManager()
                .getTargetTuvsForStatistics(p_targetPage));
    }

    @Override
    public String getReportType()
    {
        return ReportConstants.CHARACTER_COUNT_REPORT;
    }

    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, m_jobIDS, 100
                * p_finishedJobNum / m_jobIDS.size());
    }

    @Override
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(m_userId,
                m_jobIDS);
        if (data != null)
            return data.isCancle();

        return false;
    }
}