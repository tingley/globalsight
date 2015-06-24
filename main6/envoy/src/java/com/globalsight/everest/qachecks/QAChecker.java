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
package com.globalsight.everest.qachecks;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.filterconfiguration.QAFilter;
import com.globalsight.cxe.entity.filterconfiguration.QAFilterManager;
import com.globalsight.cxe.entity.filterconfiguration.QARule;
import com.globalsight.cxe.entity.filterconfiguration.QARuleDefault;
import com.globalsight.cxe.entity.filterconfiguration.QARuleException;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.util.comparator.QARuleComparator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

/**
 * Core class used for running QA Checks and generating QA Report.
 * 
 * @author leo
 * @since GBS-3697
 */
public class QAChecker
{
    private static final Logger logger = Logger.getLogger(QAChecker.class);

    private static Locale m_uiLocale = Locale.US;

    private static ResourceBundle m_bundle = SystemResourceBundle.getInstance()
            .getResourceBundle(ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                    m_uiLocale);

    private CellStyle m_titleStyle = null;
    private CellStyle m_headerStyle = null;
    private CellStyle m_contentStyle = null;
    private CellStyle m_rtlContentStyle = null;
    private CellStyle m_unlockedStyle = null;

    private static final String FALSE_POSITIVE_YES = "Yes";
    private static final String FALSE_POSITIVE_NO = "No";

    public static final int ROW_LANGUAGE_HEADER = 3;
    public static final int ROW_LANGUAGE_INFO = 4;
    public static final int ROW_SEGMENT_HEADER = 6;
    public static final int ROW_SEGMENT_START = 7;
    public static final int COLUMN_FALSE_POSITIVE = 6;
    public static final int COLUMN_HIDDEN_INFO = 26;

    public void runQAChecksAndGenerateReport(TaskInstance taskInstance)
    {
        try
        {
            if (!QACheckerHelper.isQAActivity(taskInstance))
            {
                return;
            }
            long taskId = taskInstance.getTask().getTaskNode().getId();
            runQAChecksAndGenerateReport(taskId);
        }
        catch (Exception e)
        {
            logger.error(
                    "An error occurred while trying to run QA checks and generate report.",
                    e);
        }
    }

    public String runQAChecksAndGenerateReport(long taskId) throws Exception
    {
        Task task = ServerProxy.getTaskManager().getTask(taskId);
        Company company = CompanyWrapper.getCompanyById(task.getCompanyId());
        if (!company.getEnableQAChecks())
        {
            return null;
        }
        Workflow workflow = task.getWorkflow();
        GlobalSightLocale targetLocale = workflow.getTargetLocale();
        GlobalSightLocale sourceLocale = workflow.getJob().getSourceLocale();
        String srcLang = sourceLocale.getDisplayName(m_uiLocale);
        String trgLang = targetLocale.getDisplayName(m_uiLocale);

        logger.info("Running QA Checks and generating report on "
                + task.getTaskDisplayName() + "[" + sourceLocale.toString()
                + "_" + targetLocale.toString() + "].");

        Workbook workbook = new SXSSFWorkbook();

        Sheet sheet = workbook.createSheet(targetLocale.toString());
        sheet.protectSheet("");

        addTitle(workbook, sheet);

        addHiddenInfoForUpload(workbook, sheet, taskId);

        addLanguageHeader(workbook, sheet);

        writeLanguageInfo(workbook, sheet, srcLang, trgLang);

        addSegmentHeader(workbook, sheet);

        boolean hasErrors = writeSegmentInfo(workbook, sheet, workflow);
        String reportPath = saveReportFile(workbook, task);

        if (hasErrors)
        {
            logger.info("Done QA Checks, report " + reportPath + " is saved.");
        }
        else
        {
            logger.info("Done QA Checks, no issues reported. Report "
                    + reportPath + " is saved.");
        }
        return reportPath;
    }

    private boolean writeSegmentInfo(Workbook p_workbook, Sheet p_sheet,
            Workflow p_workflow) throws Exception
    {
        boolean processed = false;
        int row = ROW_SEGMENT_START;

        GlobalSightLocale sourceLocale = p_workflow.getJob().getSourceLocale();
        GlobalSightLocale targetLocale = p_workflow.getTargetLocale();
        boolean rtlSourceLocale = EditUtil.isRTLLocale(sourceLocale);
        boolean rtlTargetLocale = EditUtil.isRTLLocale(targetLocale);

        long jobId = p_workflow.getJob().getId();
        Vector<TargetPage> targetPages = p_workflow.getTargetPages();
        for (TargetPage targetPage : targetPages)
        {
            SourcePage sourcePage = targetPage.getSourcePage();
            long fileProfileId = sourcePage.getRequest().getDataSourceId();
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .readFileProfile(fileProfileId);
            QAFilter qaFilter = fp.getQaFilter();
            if (qaFilter == null)
            {
                // no QA checks needed for this page
                continue;
            }
            List<QARule> rules = QAFilterManager.getQARules(qaFilter);
            List<QARuleDefault> rulesDefault = QAFilterManager
                    .getQARulesDefault(qaFilter);
            if (rules.size() == 0 && rulesDefault.size() == 0)
            {
                continue;
            }
            SortUtil.sort(rules, new QARuleComparator(
                    QARuleComparator.PRIORITY, m_uiLocale));

            SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
            List<Tuv> sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
            List<TuvImpl> targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

            for (int i = 0; i < targetTuvs.size(); i++)
            {
                Tuv targetTuv = (Tuv) targetTuvs.get(i);
                Tuv sourceTuv = (Tuv) sourceTuvs.get(i);
                String sourceSegment = getCompactSegment(sourceTuv, jobId);
                String displaySourceSegment = rtlSourceLocale ? EditUtil
                        .toRtlString(sourceSegment) : sourceSegment;

                String targetSegment = getCompactSegment(targetTuv, jobId);
                String displayTargetSegment = rtlTargetLocale ? EditUtil
                        .toRtlString(targetSegment) : targetSegment;

                // check regular rules
                for (QARule rule : rules)
                {
                    String check = rule.getCheck();
                    if (check == null)
                    {
                        continue;
                    }
                    boolean isRE = rule.isRE();
                    // Business logic:
                    // The source segment should be used as the
                    // reference base. If a match for a rule is found in the
                    // source segment, it should expect to find a match in the
                    // target also. If not, this should be reported as an error.
                    // If NO match for a rule is found in the source, it doesn¡¯t
                    // then matter whether or not a match is found in the
                    // target. Also, any exceptions for the language being
                    // checked should be considered.
                    int matchesInSource = 0;
                    int matchesInTarget = 0;

                    matchesInSource = QACheckerHelper.findMatches(
                            sourceSegment, check, isRE);
                    if (matchesInSource > 0)
                    {
                        matchesInTarget = QACheckerHelper.findMatches(
                                targetSegment, check, isRE);
                    }

                    if (matchesInTarget < matchesInSource)
                    {
                        // consider exceptions if no enough matches found in
                        // target
                        List<QARuleException> exceptions = rule.getExceptions();
                        for (QARuleException exception : exceptions)
                        {
                            long language = exception.getLanguage();
                            if (language == targetLocale.getId())
                            {
                                String exceptionContent = exception
                                        .getExceptionContent();
                                boolean exceptionIsRE = exception
                                        .exceptionIsRE();
                                int exceptionMatches = QACheckerHelper
                                        .findMatches(targetSegment,
                                                exceptionContent, exceptionIsRE);

                                matchesInTarget += exceptionMatches;
                            }
                        }
                    }

                    if (matchesInTarget >= matchesInSource)
                    {
                        // normal matches, no error reported. Do next rule
                        // check.
                        continue;
                    }
                    Row currentRow = getRow(p_sheet, row);

                    fillCells(p_workbook, currentRow, p_workflow, jobId,
                            rtlSourceLocale, rtlTargetLocale, sourcePage,
                            sourceTuv, displaySourceSegment,
                            displayTargetSegment, rule.getDescription());
                    row++;
                    processed = true;
                }
                // check default rules
                for (QARuleDefault ruleDefault : rulesDefault)
                {
                    String check = ruleDefault.getCheck();
                    if (check == null)
                    {
                        continue;
                    }
                    // Business logic:
                    // 1. Source equal to Target: Every target segment is
                    // checked to see if its content matches its source. If so,
                    // should be flagged as an error.
                    // 2. Target string expansion of X% or more: Every target
                    // segment is checked to see if its content is X% (where X
                    // is specified number) longer in terms of character count
                    // than its source. If so, should be flagged as an error.

                    if (QARuleDefault.SOURCE_EQUAL_TO_TARGET.equals(check))
                    {
                        if (!targetSegment.equals(sourceSegment))
                        {
                            continue;
                        }
                    }
                    else if (check
                            .startsWith(QARuleDefault.TARGET_STRING_EXPANSION_OF))
                    {
                        int targetExpansion = ruleDefault.getTargetExpansion();
                        if (targetExpansion > 0)
                        {
                            int dif = targetSegment.length()
                                    - sourceSegment.length();
                            if (dif > 0)
                            {
                                int expansion = (int) ((float) dif
                                        / sourceSegment.length() * 100);
                                if (expansion < targetExpansion)
                                {
                                    continue;
                                }
                            }
                            else
                            {
                                continue;
                            }
                        }
                        else
                        {
                            continue;
                        }
                    }
                    Row currentRow = getRow(p_sheet, row);

                    fillCells(p_workbook, currentRow, p_workflow, jobId,
                            rtlSourceLocale, rtlTargetLocale, sourcePage,
                            sourceTuv, displaySourceSegment,
                            displayTargetSegment, ruleDefault.getDescription());
                    row++;
                    processed = true;
                }
            }
        }
        if (processed)
        {
            addFalsePositiveValidation(p_sheet, row - 1);
        }
        return processed;
    }

    private void fillCells(Workbook p_workbook, Row p_currentRow,
            Workflow p_workflow, long p_jobId, boolean p_rtlSourceLocale,
            boolean p_rtlTargetLocale, SourcePage p_sourcePage,
            Tuv p_sourceTuv, String p_sourceSegment, String p_targetSegment,
            String p_desc)
    {
        int col = 0;

        // Description
        Cell descCell = getCell(p_currentRow, col);
        descCell.setCellValue(p_desc);
        descCell.setCellStyle(getContentStyle(p_workbook));
        col++;

        // Page Name
        StringBuilder sb = new StringBuilder();
        sb.append(AmbFileStoragePathUtils.getCxeDocDir(p_workflow
                .getCompanyId()));
        sb.append(File.separator);
        sb.append(SourcePage.filtSpecialFile(p_sourcePage.getExternalPageId()));

        Cell pageNameCell = getCell(p_currentRow, col);
        pageNameCell.setCellValue(sb.toString());
        pageNameCell.setCellStyle(getContentStyle(p_workbook));
        col++;

        // Job ID
        Cell jobIdCell = getCell(p_currentRow, col);
        jobIdCell.setCellValue(p_jobId);
        jobIdCell.setCellStyle(getContentStyle(p_workbook));
        col++;

        // Segment ID
        Cell segmentIdCell = getCell(p_currentRow, col);
        segmentIdCell.setCellValue(p_sourceTuv.getTu(p_jobId).getId());
        segmentIdCell.setCellStyle(getContentStyle(p_workbook));
        col++;

        // Source segment with compact tags
        CellStyle srcStyle = p_rtlSourceLocale ? getRtlContentStyle(p_workbook)
                : getContentStyle(p_workbook);
        Cell sourceCell = getCell(p_currentRow, col);
        sourceCell.setCellValue(p_sourceSegment);
        sourceCell.setCellStyle(srcStyle);
        col++;

        // Target segment with compact tags
        CellStyle trgStyle = p_rtlTargetLocale ? getRtlContentStyle(p_workbook)
                : getContentStyle(p_workbook);
        Cell targetCell = getCell(p_currentRow, col);
        targetCell.setCellValue(p_targetSegment);
        targetCell.setCellStyle(trgStyle);
        col++;

        // False Positive
        Cell falsePositiveCell = getCell(p_currentRow, col);
        falsePositiveCell.setCellValue(FALSE_POSITIVE_NO);
        falsePositiveCell.setCellStyle(getUnlockedStyle(p_workbook));
        col++;

        // User Comments
        Cell commentsCell = getCell(p_currentRow, col);
        commentsCell.setCellValue("");
        commentsCell.setCellStyle(getUnlockedStyle(p_workbook));
    }

    private void addFalsePositiveValidation(Sheet p_sheet, int p_lastRow)
    {
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();

        DataValidationConstraint dvConstraint = dvHelper
                .createExplicitListConstraint(new String[]
                { FALSE_POSITIVE_YES, FALSE_POSITIVE_NO });
        CellRangeAddressList addressList = new CellRangeAddressList(
                ROW_SEGMENT_START, p_lastRow, COLUMN_FALSE_POSITIVE,
                COLUMN_FALSE_POSITIVE);

        DataValidation validation = dvHelper.createValidation(dvConstraint,
                addressList);
        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);

        p_sheet.addValidationData(validation);
    }

    private String getCompactSegment(Tuv p_tuv, long p_jobId) throws Exception
    {
        PseudoData pData = new PseudoData();
        pData.setMode(PseudoConstants.PSEUDO_COMPACT);

        StringBuilder segment = new StringBuilder();

        String dataType = p_tuv.getDataType(p_jobId);
        pData.setAddables(dataType);
        TmxPseudo.tmx2Pseudo(p_tuv.getGxmlExcludeTopTags(), pData);
        segment.append(pData.getPTagSourceString());

        // If there are subflows, output them too.
        List subFlows = p_tuv.getSubflowsAsGxmlElements();
        if (subFlows != null && subFlows.size() > 0)
        {
            long tuId = p_tuv.getTuId();
            for (int i = 0; i < subFlows.size(); i++)
            {
                GxmlElement sub = (GxmlElement) subFlows.get(i);
                String subId = sub.getAttribute(GxmlNames.SUB_ID);
                segment.append("\r\n#").append(tuId).append(":").append(subId)
                        .append("\n")
                        .append(getCompactPtagString(sub, dataType));
            }
        }

        return segment.toString();
    }

    private String getCompactPtagString(GxmlElement p_gxmlElement,
            String p_dataType) throws Exception
    {
        String compactPtags = null;

        OnlineTagHelper tagHelper = new OnlineTagHelper();
        String seg = GxmlUtil.getInnerXml(p_gxmlElement);
        tagHelper.setDataType(p_dataType);
        tagHelper.setInputSegment(seg, "", p_dataType);
        compactPtags = tagHelper.getCompact();

        return compactPtags;
    }

    public String saveReportFile(Workbook p_workbook, Task p_task)
            throws Exception
    {
        StringBuilder sb = new StringBuilder();

        sb.append(QACheckerHelper.getQAReportPath(p_task));
        new File(sb.toString()).mkdirs();
        sb.append(QACheckerHelper.getQAReportName(p_task));

        File file = new File(sb.toString());
        FileOutputStream out = new FileOutputStream(file);
        p_workbook.write(out);
        out.close();
        ((SXSSFWorkbook) p_workbook).dispose();

        return sb.toString();
    }

    private void addSegmentHeader(Workbook p_workbook, Sheet p_sheet)
    {
        int row = ROW_SEGMENT_HEADER;
        int col = 0;
        Row segHeaderRow = getRow(p_sheet, row);

        Cell descCell = getCell(segHeaderRow, col);
        descCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_description"));
        descCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell pageNamecell = getCell(segHeaderRow, col);
        pageNamecell.setCellValue(m_bundle
                .getString("lb_report_qa_report_page_name"));
        pageNamecell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 30 * 256);
        col++;

        Cell jobIdCell = getCell(segHeaderRow, col);
        jobIdCell
                .setCellValue(m_bundle.getString("lb_report_qa_report_job_id"));
        jobIdCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 12 * 256);
        col++;

        Cell segmentIdCell = getCell(segHeaderRow, col);
        segmentIdCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_segment_id"));
        segmentIdCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 12 * 256);
        col++;

        Cell sourceCell = getCell(segHeaderRow, col);
        sourceCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_source"));
        sourceCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell targetCell = getCell(segHeaderRow, col);
        targetCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_target"));
        targetCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        Cell falsePositiveCell = getCell(segHeaderRow, col);
        falsePositiveCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_false_positive"));
        falsePositiveCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        Cell commentsCell = getCell(segHeaderRow, col);
        commentsCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_comments"));
        commentsCell.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col, 40 * 256);
    }

    private void addLanguageHeader(Workbook p_workbook, Sheet p_sheet)
    {
        int row = ROW_LANGUAGE_HEADER;
        int col = 0;

        Row langRow = getRow(p_sheet, row);
        Cell srcLangCell = getCell(langRow, col);
        srcLangCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_source_language"));
        srcLangCell.setCellStyle(getHeaderStyle(p_workbook));
        col++;

        Cell trgLangCell = getCell(langRow, col);
        trgLangCell.setCellValue(m_bundle
                .getString("lb_report_qa_report_target_language"));
        trgLangCell.setCellStyle(getHeaderStyle(p_workbook));
    }

    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet,
            String p_sourceLang, String p_targetLang) throws Exception
    {
        int row = ROW_LANGUAGE_INFO;
        int col = 0;

        Row langInfoRow = getRow(p_sheet, row);

        Cell srcLangCell = getCell(langInfoRow, col++);
        srcLangCell.setCellValue(p_sourceLang);
        srcLangCell.setCellStyle(getContentStyle(p_workbook));

        Cell trgLangCell = getCell(langInfoRow, col++);
        trgLangCell.setCellValue(p_targetLang);
        trgLangCell.setCellStyle(getContentStyle(p_workbook));
    }

    private void addHiddenInfoForUpload(Workbook p_workbook, Sheet p_sheet,
            long p_taskId)
    {
        int hiddenColumn = COLUMN_HIDDEN_INFO;
        String hiddenValue = ReportConstants.PREFIX_QA_CHECKS_REPORT + "_"
                + p_taskId;
        Row titleRow = getRow(p_sheet, 0);
        Cell hiddenCell = getCell(titleRow, hiddenColumn);
        hiddenCell.setCellValue(hiddenValue);
        hiddenCell.setCellStyle(getContentStyle(p_workbook));

        p_sheet.setColumnHidden(hiddenColumn, true);
    }

    private void addTitle(Workbook p_workbook, Sheet p_sheet)
    {
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(m_bundle.getString("lb_report_qa_report_title"));
        titleCell.setCellStyle(getTitleStyle(p_workbook));
    }

    private Row getRow(Sheet p_sheet, int p_row)
    {
        Row row = p_sheet.getRow(p_row);
        if (row == null)
        {
            row = p_sheet.createRow(p_row);
        }
        return row;
    }

    private Cell getCell(Row p_row, int columnIndex)
    {
        Cell cell = p_row.getCell(columnIndex);
        if (cell == null)
        {
            cell = p_row.createCell(columnIndex);
        }
        return cell;
    }

    private CellStyle getTitleStyle(Workbook p_workbook)
    {
        if (m_titleStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            Font font = p_workbook.createFont();
            font.setUnderline(Font.U_NONE);
            font.setFontName("Times");
            font.setFontHeightInPoints((short) 14);
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            style.setFont(font);

            m_titleStyle = style;
        }

        return m_titleStyle;
    }

    private CellStyle getContentStyle(Workbook p_workbook)
    {
        if (m_contentStyle == null)
        {
            CellStyle style = p_workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            m_contentStyle = style;
        }

        return m_contentStyle;
    }

    private CellStyle getHeaderStyle(Workbook p_workbook)
    {
        if (m_headerStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setColor(IndexedColors.BLACK.getIndex());
            font.setUnderline(Font.U_NONE);
            font.setFontName("Times");
            font.setFontHeightInPoints((short) 11);

            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(font);
            cs.setWrapText(true);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);

            m_headerStyle = cs;
        }

        return m_headerStyle;
    }

    private CellStyle getRtlContentStyle(Workbook p_workbook)
    {
        if (m_rtlContentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            m_rtlContentStyle = style;
        }

        return m_rtlContentStyle;
    }

    private CellStyle getUnlockedStyle(Workbook p_workbook)
    {
        if (m_unlockedStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setLocked(false);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            m_unlockedStyle = style;
        }

        return m_unlockedStyle;
    }
}
