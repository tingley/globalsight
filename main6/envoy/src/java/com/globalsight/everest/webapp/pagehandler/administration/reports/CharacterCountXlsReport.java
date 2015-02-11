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
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.edit.EditUtil;

public class CharacterCountXlsReport
{
    private HttpServletRequest m_request = null;

    private HttpServletResponse m_response = null;
    private ResourceBundle m_bundle = null;
    private Locale uiLocale = null;

    private SessionManager m_sessionMgr = null;

    private WritableWorkbook m_workbook = null;

    private WritableCellFormat m_contentFormat = null;

    private WritableCellFormat m_headerFormat = null;

    public static final int SEGMENT_START_ROW = 7;

    public static final int SEGMENT_HEADER_ROW = 6;

    public static final int LANGUAGE_HEADER_ROW = 3;

    public static final int LANGUAGE_INFO_ROW = 4;

    /**
     * Constructor. Create a helper to generate the report
     * 
     * @param p_request
     *            the request
     * @param p_response
     *            the response
     * @throws Exception
     */
    public CharacterCountXlsReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        m_request = p_request;
        m_bundle = PageHandler.getBundle(m_request.getSession());
        uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        m_response = p_response;
        HttpSession session = p_request.getSession();
        m_sessionMgr = (SessionManager) session
                .getAttribute(WebAppConstants.SESSION_MANAGER);

        String companyName = UserUtil.getCurrentCompanyName(m_request);
        CompanyThreadLocal.getInstance().setValue(companyName);
    }

    /**
     * Generates Excel report
     * 
     * @throws Exception
     */
    public void generateReport() throws Exception
    {
        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);
        m_workbook = Workbook.createWorkbook(m_response.getOutputStream(),
                settings);
        createReport();
        m_workbook.write();
        m_workbook.close();
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

        p_sheet.addCell(new Label(col++, row, m_bundle.getString("lb_segment_id"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 20);

        p_sheet.addCell(new Label(col++, row, m_bundle.getString("lb_source_segment"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_source_character_count"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, row, m_bundle.getString("lb_target_segment"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 40);

        p_sheet.addCell(new Label(col++, row, m_bundle
                .getString("lb_target_character_count"), getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
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
        p_sheet.addCell(new Label(col++, row, m_bundle.getString("lb_source_language"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);
        p_sheet.addCell(new Label(col++, row, m_bundle.getString("lb_target_language"),
                getHeaderFormat()));
        p_sheet.setColumnView(col - 1, 30);

        p_sheet.addCell(new Label(col++, row, m_bundle.getString("jobinfo.jobid"), getHeaderFormat()));
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

    private void createReport() throws Exception
    {
        WritableSheet sheet = m_workbook.createSheet(m_bundle
                .getString("lb_character_count_report"), 0);
        addHeader(sheet);
        writeLanguageInfo(sheet);
        writeSegmentInfo(sheet);
    }

    /**
     * Write the Job information into the header
     * 
     * @param p_sheet
     *            the sheet
     * @throws Exception
     */
    private void writeLanguageInfo(WritableSheet p_sheet) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        if (m_request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(m_request.getParameter(WebAppConstants.JOB_ID)))
        {
            writeLanguageInfo(p_sheet, m_request
                    .getParameter(WebAppConstants.JOB_ID), EditUtil
                    .utf8ToUnicode(m_request
                            .getParameter(WebAppConstants.TARGET_LANGUAGE)));
        }
        else if (m_sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
        {
            writeLanguageInfo(p_sheet, (String) m_sessionMgr
                    .getAttribute(WebAppConstants.JOB_ID),
                    (String) m_sessionMgr
                            .getAttribute(WebAppConstants.TARGETVIEW_LOCALE));
        }
        else
        {
            // If no job exists, just set the cell blank.
            p_sheet.addCell(new Label(col++, row, ""));
            p_sheet.addCell(new Label(col++, row, ""));
        }
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
    private void writeLanguageInfo(WritableSheet p_sheet, String p_jobId,
            String p_targetLang) throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(
                Long.parseLong(p_jobId));

        int col = 0;
        int row = LANGUAGE_INFO_ROW;

        // Write Source Language into the sheet
        p_sheet.addCell(new Label(col++, row, job.getSourceLocale()
                .getDisplayName(uiLocale), getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Write Target Language into the sheet
        p_sheet
                .addCell(new Label(col++, row, p_targetLang, getContentFormat()));
        p_sheet.setColumnView(col - 1, 30);

        // Write Job ID into the sheet
        p_sheet.addCell(new Label(col++, row, p_jobId, getContentFormat()));
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
     * @throws Exception
     */
    private void writeSegmentInfo(WritableSheet p_sheet) throws Exception
    {
        if (m_request.getParameter(WebAppConstants.JOB_ID) != null
                && !"*".equals(m_request.getParameter(WebAppConstants.JOB_ID)))
        {
            writeCharacterCountSegmentInfo(p_sheet, m_request
                    .getParameter(WebAppConstants.JOB_ID), EditUtil
                    .utf8ToUnicode(m_request
                            .getParameter(WebAppConstants.TARGET_LANGUAGE)), "");
        }
        else if (m_sessionMgr.getAttribute(WebAppConstants.JOB_ID) != null)
        {
            writeCharacterCountSegmentInfo(p_sheet, (String) m_sessionMgr
                    .getAttribute(WebAppConstants.JOB_ID),
                    (String) m_sessionMgr
                            .getAttribute(WebAppConstants.TARGETVIEW_LOCALE),
                    "");
        }
        else
        {
            // If no job exists, just set the cell blank
            writeCommentsAnalysisBlank(p_sheet);
        }
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
            String p_jobId, String p_targetLang, String p_srcPageId)
            throws Exception
    {
        Job job = ServerProxy.getJobHandler().getJobById(
                Long.parseLong(p_jobId));
        Collection wfs = job.getWorkflows();
        Iterator it = wfs.iterator();
        Vector targetPages = new Vector();

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
            writeCommentsAnalysisBlank(p_sheet);
        }
        else
        {
            int row = SEGMENT_START_ROW;
            ArrayList allSourceTuvs = new ArrayList();
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();
                List sourceTuvs = getPageTuvs(sourcePage);
                allSourceTuvs.addAll(sourceTuvs);
            }
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
                List targetTuvs = getPageTuvs(targetPage);
                List sourceTuvs = getPageTuvs(sourcePage);

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
                    p_sheet.addCell(new Number(col++, row, sourceTuv.getTu()
                            .getId(), getContentFormat()));
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
}