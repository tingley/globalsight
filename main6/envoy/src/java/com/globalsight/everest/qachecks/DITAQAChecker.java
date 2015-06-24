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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jbpm.taskmgmt.exe.TaskInstance;

import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.edit.online.OnlineTagHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlNames;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public class DITAQAChecker
{
    private static final Logger logger = Logger.getLogger(DITAQAChecker.class);

    private Locale m_uiLocale = Locale.US;
    private ResourceBundle m_bundle = null;

    private CellStyle headerStyle = null;
    private CellStyle contentStyle = null;
    private CellStyle noWrapContentStyle = null;
    private CellStyle rtlContentStyle = null;
    private CellStyle unlockedStyle = null;
    private Font normalFont = null;
    private Font redFont = null;

    public static final int LANGUAGE_HEADER_ROW = 3;
    public static final int LANGUAGE_INFO_ROW = 4;
    public static final int SEGMENT_HEADER_ROW = 6;
    public static final int SEGMENT_START_ROW = 7;
    public static final int FALSE_POSITIVE_COLUMN = 6;

    public static final String DITA_QA_CHECKS_REPORT = "DITAQAChecksReport";

    /**
     * No characters are allowed by DITA in between "uicontrol" elements if they
     * appear within a "menucascade".
     */
    public static final String DITA_CHECK1_EXTRA_CONTENT = "Extra content between uicontrol of menucascade elements";

    /**
     * Empty uicontrol, cite, term, b, i elements.
     */
    public static final String DITA_CHECK2_EMPTY_TAGS = "Empty Tags";

    /**
     * These tags cannot be empty.
     */
    public static final Set<String> emptyTags = new HashSet<String>();
    {
        emptyTags.add("uicontrol");
        emptyTags.add("cite");
        emptyTags.add("term");
        emptyTags.add("b");
        emptyTags.add("i");
    }

    /**
     * For JP and zh_CN, need insert "index-sort-as" element.
     */
    public static final String DITA_CHECK3_MISSING_INDEX_SORT_AS = "Missing <index-sort-as> within <indexterm>";

    public DITAQAChecker() throws Exception
    {
        m_bundle = SystemResourceBundle.getInstance().getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, m_uiLocale);
    }

    public File runQAChecksAndGenerateReport(TaskInstance p_taskInstance)
            throws Exception
    {
        return runQAChecksAndGenerateReport(p_taskInstance.getTask()
                .getTaskNode().getId());
    }

    public File runQAChecksAndGenerateReport(long p_taskId) throws Exception
    {
        Task task = TaskHelper.getTask(p_taskId);
        Company company = CompanyWrapper.getCompanyById(task.getCompanyId());
        if (!company.getEnableDitaChecks())
        {
            return null;
        }

        // Create report
        // Note: as SXSSFWorkbook does not support "RichTextString" to highlight
        // check failed segment, we have to use XSSFWorkbook, this need more
        // memory.
        Workbook workBook = new XSSFWorkbook();
        createReport(workBook, task);

        // Write workbook to file
        File reportDir = DITAQACheckerHelper.getReportFileDir(task);
        // As only store one copy for every task, before generate the new report
        // file, delete old ones if exists.
        FileUtil.deleteFile(reportDir);
        reportDir.mkdirs();

        String fileName = getReportFileName(task);
        File reportFile = new File(reportDir, fileName);
        FileOutputStream out = new FileOutputStream(reportFile);
        workBook.write(out);
        out.close();

        return reportFile;
    }

    /**
     * The DITA QA checks report name is like
     * "DITAQAChecksReport-Job Name-zh_CN-20141212 125403.xlsx".
     */
    private static String getReportFileName(Task p_task)
    {
		String dateSuffix = new SimpleDateFormat("yyyyMMdd HHmmss")
				.format(new Date());
        try
        {
            String trgLang = p_task.getTargetLocale().toString();
            StringBuffer fileName = new StringBuffer();
            fileName.append(DITA_QA_CHECKS_REPORT);
            fileName.append("-");
            fileName.append(p_task.getJobName());
            fileName.append("-");
            fileName.append(trgLang);
            fileName.append("-");
            fileName.append(dateSuffix + ReportConstants.EXTENSION_XLSX);
            return fileName.toString();
        }
        catch (Exception e)
        {
            String msg = "Error when get report file name for task "
                    + p_task.getId();
            logger.warn(msg, e);
        }

        String defaultName = DITA_QA_CHECKS_REPORT + "-" + dateSuffix
                + ReportConstants.EXTENSION_XLSX;
        return defaultName;
    }

    /**
     * Write report data info workbook in cache to output to file later.
     * @param p_workbook
     * @param task
     * @throws Exception
     */
    private void createReport(Workbook p_workbook, Task task) throws Exception
    {
        GlobalSightLocale trgLocale = task.getTargetLocale();
        GlobalSightLocale srgLocale = task.getSourceLocale();

        // Create Sheet
        Sheet sheet = p_workbook.createSheet(trgLocale.toString());
        sheet.protectSheet("");

        // Add Title
        addTitle(p_workbook, sheet);

        // Add hidden info "DITA_taskID" for uploading.
        addHidenInfoForUpload(p_workbook, sheet, task.getId());

        // Add Locale Pair Header
        addLanguageHeader(p_workbook, sheet);

        // Add language header information
        String srcLang = srgLocale.getDisplayName(m_uiLocale);
        String trgLang = trgLocale.getDisplayName(m_uiLocale);
        writeLanguageInfo(p_workbook, sheet, srcLang, trgLang);

        // Add Segment Header
        addSegmentHeader(p_workbook, sheet);

        // write data into report
        writeSegmentInfo(p_workbook, sheet, task);
    }

    private void addTitle(Workbook p_workBook, Sheet p_sheet) throws Exception
    {
        Font titleFont = p_workBook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle cs = p_workBook.createCellStyle();
        cs.setFont(titleFont);

        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue("DITA QA Report");
        titleCell.setCellStyle(cs);
    }

    private void addLanguageHeader(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = LANGUAGE_HEADER_ROW;

        Row langRow = getRow(p_sheet, row);
        Cell srcLangCell = getCell(langRow, col);
        srcLangCell.setCellValue(m_bundle.getString("lb_source_language"));
        srcLangCell.setCellStyle(getHeaderStyle(p_workBook));
        col++;

        Cell trgLangCell = getCell(langRow, col);
        trgLangCell.setCellValue(m_bundle.getString("lb_target_language"));
        trgLangCell.setCellStyle(getHeaderStyle(p_workBook));
    }

    private void writeLanguageInfo(Workbook p_workbook, Sheet p_sheet,
            String p_sourceLang, String p_targetLang) throws Exception
    {
        int col = 0;
        int row = LANGUAGE_INFO_ROW;
        CellStyle contentStyle = getContentStyle(p_workbook);
        Row langInfoRow = getRow(p_sheet, row);

        // Source Language
        Cell srcLangCell = getCell(langInfoRow, col++);
        srcLangCell.setCellValue(p_sourceLang);
        srcLangCell.setCellStyle(contentStyle);

        // Target Language
        Cell trgLangCell = getCell(langInfoRow, col++);
        trgLangCell.setCellValue(p_targetLang);
        trgLangCell.setCellStyle(contentStyle);
    }

    /**
     * Add hidden info "DITA_taskID" for offline uploading. When upload, system
     * can know the report type and current task ID report generated from.
     */
    private void addHidenInfoForUpload(Workbook p_workbook, Sheet p_sheet,
            long p_taskId) throws Exception
    {
        Row titleRow = getRow(p_sheet, 0);
        Cell cell_AA1 = getCell(titleRow, 26);
        cell_AA1.setCellValue("DITA_" + p_taskId);
        cell_AA1.setCellStyle(contentStyle);

        p_sheet.setColumnHidden(26, true);
    }

    private void addSegmentHeader(Workbook p_workBook, Sheet p_sheet)
            throws Exception
    {
        int col = 0;
        int row = SEGMENT_HEADER_ROW;
        Row segHeaderRow = getRow(p_sheet, row);

        // Description
        Cell cell_A = getCell(segHeaderRow, col);
        cell_A.setCellValue(m_bundle.getString("lb_description"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        // Page name
        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(m_bundle.getString("lb_page_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        // Job id
        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(m_bundle.getString("lb_job_id_report"));
        cell_C.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        // Segment Id
        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(m_bundle.getString("lb_segment_id"));
        cell_D.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        col++;

        // Source Segment
        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(m_bundle.getString("lb_source_segment"));
        cell_E.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        // Target Segment
        Cell cell_F = getCell(segHeaderRow, col);
        cell_F.setCellValue(m_bundle.getString("lb_target_segment"));
        cell_F.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;

        // False Positive
        Cell cell_G = getCell(segHeaderRow, col);
        cell_G.setCellValue(m_bundle.getString("lb_false_positive"));
        cell_G.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 20 * 256);
        col++;

        Cell cell_H = getCell(segHeaderRow, col);
        cell_H.setCellValue(m_bundle.getString("lb_comments"));
        cell_H.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 40 * 256);
        col++;
    }

    @SuppressWarnings("rawtypes")
    private int writeSegmentInfo(Workbook p_workBook, Sheet p_sheet, Task p_task)
            throws Exception
    {
        int row = SEGMENT_START_ROW;
        Job job = ServerProxy.getJobHandler().getJobById(p_task.getJobId());
        GlobalSightLocale trgLocale = p_task.getTargetLocale();
        
        Vector<TargetPage> targetPages = new Vector<TargetPage>();

        TranslationMemoryProfile tmp = null;
        Vector<String> excludItems = null;

        for (Workflow workflow : job.getWorkflows())
        {
            if (Workflow.PENDING.equals(workflow.getState())
                    || Workflow.CANCELLED.equals(workflow.getState())
//                    || Workflow.EXPORT_FAILED.equals(workflow.getState())
                    || Workflow.IMPORT_FAILED.equals(workflow.getState()))
            {
                continue;
            }
            if (trgLocale.getId() == workflow.getTargetLocale().getId())
            {
                targetPages = workflow.getTargetPages();
                tmp = workflow.getJob().getL10nProfile()
                        .getTranslationMemoryProfile();
                if (tmp != null)
                {
                    excludItems = tmp.getJobExcludeTuTypes();
                }
            }
        }

        List<SourcePage> nonDitaPages = new ArrayList<SourcePage>();
        if (targetPages.isEmpty())
        {
            // If no corresponding target page exists, set the cell blank
            writeBlank(p_sheet, row, 11);
        }
        else
        {
            Locale sourcePageLocale = p_task.getSourceLocale().getLocale();
            Locale targetPageLocale = trgLocale.getLocale();
            String tuType = null;
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_VERBOSE);
            for (int i = 0; i < targetPages.size(); i++)
            {
                TargetPage targetPage = (TargetPage) targetPages.get(i);
                SourcePage sourcePage = targetPage.getSourcePage();
                String externalPageId = sourcePage.getExternalPageId()
                        .replace("\\", "/");
                if (!externalPageId.toLowerCase().endsWith(".xml"))
                {
                    nonDitaPages.add(sourcePage);
                    continue;
                }

                SegmentTuUtil.getTusBySourcePageId(sourcePage.getId());
                List sourceTuvs = SegmentTuvUtil.getSourceTuvs(sourcePage);
                List targetTuvs = SegmentTuvUtil.getTargetTuvs(targetPage);

                boolean m_rtlSourceLocale = EditUtil
                        .isRTLLocale(sourcePageLocale.toString());
                boolean m_rtlTargetLocale = EditUtil
                        .isRTLLocale(targetPageLocale.toString());

                for (int j = 0; j < targetTuvs.size(); j++)
                {
                    int col = 0;
                    Tuv sourceTuv = (Tuv) sourceTuvs.get(j);
                    Tuv targetTuv = (Tuv) targetTuvs.get(j);

                    tuType = sourceTuv.getTu(job.getId()).getTuType();
                    if (excludItems != null && excludItems.contains(tuType))
                    {
                        continue;
                    }

                    String source = getSegment(pData, sourceTuv,
                            m_rtlSourceLocale, job.getId());
                    String target = getSegment(pData, targetTuv,
                            m_rtlTargetLocale, job.getId());
                    Object[] checkResults = doDitaCheck(source, target, trgLocale);

                    Row currentRow = getRow(p_sheet, row);

                    // Description
                    Cell cell_A = getCell(currentRow, col);
                    cell_A.setCellStyle(getContentStyle(p_workBook));
                    cell_A.setCellValue("");
                    // set the description message
                    if (checkResults != null && checkResults.length == 3)
                    {
                        cell_A.setCellValue((String) checkResults[0]);
                    }
                    col++;

                    //Page Name
                    Cell cell_B = getCell(currentRow, col);
                    cell_B.setCellValue(externalPageId);
                    cell_B.setCellStyle(getNoWrapContentStyle(p_workBook));
                    col++;

                    // Job Id
                    Cell cell_C = getCell(currentRow, col);
                    cell_C.setCellValue(job.getId());
                    cell_C.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Segment id
                    Cell cell_D = getCell(currentRow, col);
                    cell_D.setCellValue(sourceTuv.getTu(job.getId()).getId());
                    cell_D.setCellStyle(getContentStyle(p_workBook));
                    col++;

                    // Source segment
                    CellStyle srcStyle = m_rtlSourceLocale ? getRtlContentStyle(p_workBook)
                            : getContentStyle(p_workBook);
                    Cell cell_E = getCell(currentRow, col);
                    cell_E.setCellStyle(srcStyle);
                    List<String> srcHighlightIndexes = getHighlightIndexes(
                            checkResults, "source");
                    // DITA check failed
                    if (srcHighlightIndexes != null)
                    {
                        RichTextString ts = getRichTextString(p_workBook,
                                source, srcHighlightIndexes);
                        cell_E.setCellType(XSSFCell.CELL_TYPE_STRING);
                        cell_E.setCellValue(ts);
                    }
                    else
                    {
                        cell_E.setCellValue(source);
                    }
                    col++;

                    // Target segment
                    CellStyle trgStyle = m_rtlTargetLocale ? getRtlContentStyle(p_workBook)
                            : getContentStyle(p_workBook);
                    Cell cell_F = getCell(currentRow, col);
                    cell_F.setCellStyle(trgStyle);
                    List<String> trgHighlightIndexes = getHighlightIndexes(
                            checkResults, "target");
                    // DITA check failed
                    if (trgHighlightIndexes != null)
                    {
                        RichTextString ts = getRichTextString(p_workBook,
                                target, trgHighlightIndexes);
                        cell_F.setCellType(XSSFCell.CELL_TYPE_STRING);
                        cell_F.setCellValue(ts);
                    }
                    else
                    {
                        cell_F.setCellValue(target);
                    }
                    col++;

                    // False Positive
                    Cell cell_G = getCell(currentRow, col);
                    cell_G.setCellValue("No");
                    cell_G.setCellStyle(getUnlockedStyle(p_workBook));
                    col++;

                    // Category failure
                    Cell cell_H = getCell(currentRow, col);
                    cell_H.setCellValue("");
                    cell_H.setCellStyle(getUnlockedStyle(p_workBook));
                    col++;

                    row++;
                }
            }

            // Add category failure drop down list here.
            if (row > SEGMENT_START_ROW)
            {
                addFalsePositiveValidation(p_sheet, SEGMENT_START_ROW, row - 1,
                        FALSE_POSITIVE_COLUMN, FALSE_POSITIVE_COLUMN);
            }
        }

        row++;
        if (nonDitaPages.size() > 0)
        {
            for (SourcePage sp : nonDitaPages)
            {
                Row currentRow = getRow(p_sheet, row);
                Cell cell = getCell(currentRow, 0);
                cell.setCellStyle(getNoWrapContentStyle(p_workBook));
                String spName = sp.getExternalPageId().replace("\\", "/");
                cell.setCellValue("\"" + spName + "\" need not do DITA QA checks as it is not XML file.");
                row++;
            }
        }

        return row;
    }

    private void writeBlank(Sheet p_sheet, int p_row, int p_colLen)
            throws Exception
    {
        for (int col = 0; col < p_colLen; col++)
        {
            Row row = p_sheet.getRow(p_row);
            Cell cell = getCell(row, col);
            cell.setCellValue("");
            col++;
        }
    }

    private void addFalsePositiveValidation(Sheet p_sheet, int startRow,
            int lastRow, int startColumn, int lastColumn)
    {
        // Add category failure drop down list here.
        DataValidationHelper dvHelper = p_sheet.getDataValidationHelper();

        String[] options =
        { "Yes", "No" };
        DataValidationConstraint dvConstraint = dvHelper
                .createExplicitListConstraint(options);

        CellRangeAddressList addressList = new CellRangeAddressList(startRow,
                lastRow, startColumn, lastColumn);

        DataValidation validation = dvHelper.createValidation(dvConstraint,
                addressList);

        validation.setSuppressDropDownArrow(true);
        validation.setShowErrorBox(true);
        p_sheet.addValidationData(validation);
    }

    private String getSegment(PseudoData pData, Tuv tuv, boolean m_rtlLocale,
            long p_jobId)
    {
        StringBuffer content = new StringBuffer();
        String dataType = null;
        try
        {
            dataType = tuv.getDataType(p_jobId);
            pData.setAddables(dataType);
            TmxPseudo.tmx2Pseudo(tuv.getGxmlExcludeTopTags(), pData);
            content.append(pData.getPTagSourceString());

            // If there are subflows, output them too.
            List subFlows = tuv.getSubflowsAsGxmlElements();
            if (subFlows != null && subFlows.size() > 0)
            {
                long tuId = tuv.getTuId();
                for (int i = 0; i < subFlows.size(); i++)
                {
                    GxmlElement sub = (GxmlElement) subFlows.get(i);
                    String subId = sub.getAttribute(GxmlNames.SUB_ID);
                    content.append("\r\n#").append(tuId).append(":")
                            .append(subId).append("\n")
                            .append(getVerbosePtagString(sub, dataType));
                }
            }
        }
        catch (Exception e)
        {
            logger.error(tuv.getId(), e);
        }

        String result = content.toString();
        if (m_rtlLocale)
        {
            result = EditUtil.toRtlString(result);
        }

        return result;
    }

    private String getVerbosePtagString(GxmlElement p_gxmlElement,
            String p_dataType)
    {
        String verbosePtags = null;
        OnlineTagHelper applet = new OnlineTagHelper();
        try
        {
            String seg = GxmlUtil.getInnerXml(p_gxmlElement);
            applet.setDataType(p_dataType);
            applet.setInputSegment(seg, "", p_dataType);
            verbosePtags = applet.getVerbose();
        }
        catch (Exception e)
        {
            logger.info("getCompactPtagString Error.", e);
        }

        return verbosePtags;
    }

    private Object[] doDitaCheck(String source, String target,
            GlobalSightLocale trgLocale)
    {
        Object[] results1 = checkExtraContent(source, target);
        Object[] results2 = checkEmptyTags(source, target);
        Object[] results3 = checkIndexSortAs(source, target, trgLocale);

        List<Object[]> allCheckResults = new ArrayList<Object[]>();
        allCheckResults.add(results1);
        allCheckResults.add(results2);
        allCheckResults.add(results3);

        String description = null;
        List<String> srcAllStr = new ArrayList<String>();
        List<String> trgAllStr = new ArrayList<String>();
        for (Object[] checkResult: allCheckResults)
        {
            if (checkResult != null)
            {
                if (description == null)
                {
                    description = (String) checkResult[0];                    
                }
                else
                {
                    description += "\r\n" + checkResult[0];
                }

                if (checkResult[1] != null)
                {
                    srcAllStr.addAll((List<String>) checkResult[1]);
                }

                if (checkResult[2] != null)
                {
                    trgAllStr.addAll((List<String>) checkResult[2]);
                }
            }
        }

        Object[] result = new Object[3];
        result[0] = description;
        result[1] = mergeHighlightRegions(srcAllStr);
        result[2] = mergeHighlightRegions(trgAllStr);
        return result;
    }

    private static List<String> mergeHighlightRegions(List<String> highlightRegions)
    {
        if (highlightRegions == null || highlightRegions.size() == 0)
            return null;

        int start = -1;
        int end = -1;
        Set<Integer> allInSet = new HashSet<Integer>();
        for (String str : highlightRegions)
        {
            String[] arr = str.split("-");
            start = Integer.parseInt(arr[0]);
            end = Integer.parseInt(arr[1]);
            for (int i = start; i <= end; i++)
            {
                allInSet.add(i);
            }
        }

        List<Integer> allInList = new ArrayList<Integer>();
        allInList.addAll(allInSet);
        Collections.sort(allInList);

        int startNum = 0;
        int preNum = 0;
        int currentNum = 0;
        List<String> result = new ArrayList<String>();
        for (int j = 0; j < allInList.size(); j++)
        {
            currentNum = allInList.get(j);
            if (j == 0)
            {
                startNum =currentNum;
                preNum = currentNum;
                continue;
            }

            if (currentNum - preNum == 1)
            {
                preNum = currentNum;
            }
            else
            {
                result.add(startNum + "-" + preNum);

                startNum = currentNum;
                preNum = currentNum;
            }

            if (j == allInList.size() - 1)
            {
                result.add(startNum + "-" + currentNum);
            }
        }

        return result;
    }

    /**
     * There are 3 data in DITA check results, the first is check description,
     * the second is source highlight indexes, the third is target highlight
     * indexes.
     * 
     * @param arr
     * @param flag "source" or "target"
     * @return
     */
    @SuppressWarnings("unchecked")
    private List<String> getHighlightIndexes(Object[] arr, String flag)
    {
        if (arr == null || arr.length != 3
                || (!"source".equals(flag) && !"target".equals(flag)))
        {
            return null;
        }

        Object obj = null;
        if ("source".equals(flag))
        {
            obj = arr[1];
        }
        else if ("target".equals(flag))
        {
            obj = arr[2];
        }

        if (obj != null)
        {
            try
            {
                List<String> result = (List<String>) obj;
                if (result.size() > 0)
                {
                    return result;
                }                
            }
            catch (Exception e)
            {
                
            }
        }

        return null;
    }

    private RichTextString getRichTextString(Workbook p_workBook,
            String segment, List<String> highlightIndexes)
    {
        RichTextString ts = new XSSFRichTextString(segment);
        ts.applyFont(getNormalFont(p_workBook));
        for (String red : highlightIndexes)
        {
            String[] arr = red.split("-");
            int indexStart = Integer.parseInt(arr[0]);
            int indexEnd = Integer.parseInt(arr[1]);
            ts.applyFont(indexStart, indexEnd,
                    getRedFont(p_workBook));
        }
        return ts;
    }

    /**
     * No characters are allowed by DITA in between "uicontrol" elements if they
     * appear within a "menucascade". E.g The following sequence is invalid
     * because of the "xyz":
     * 
     * <menucascade><uicontrol>abc</uicontrol>xyz<uicontrol>abc</uicontrol></menucascade>
     * 
     * @param target
     *            -- like
     *            "Click [menucascade1][uicontrol2]Menu[/uicontrol2][uicontrol3]Automation[/uicontrol3][/menucascade1]."
     */
    private Object[] checkExtraContent(String source, String target)
    {
        if (target == null || target.indexOf("[menucascade") == -1
                || target.indexOf("[uicontrol") == -1)
            return null;

        List<String> trgRedIndexRegions = new ArrayList<String>();
        int indexStart = 0;
        boolean isInMenuCascade = false;
        boolean isInUIControl = false;

        StringBuffer tmp = new StringBuffer();
        int length = target.length();
        for (int i = 0; i < length; i++)
        {
            char c = target.charAt(i);
            if (c == '[')
            {
                if (isInMenuCascade && !isInUIControl
                        && tmp.toString().trim().length() > 0)
                {
                    trgRedIndexRegions.add(indexStart + "-" + i);
                }

                tmp = new StringBuffer();
                tmp.append(c);
                indexStart = i + 1;
            }
            else if (c == ']')
            {
                if (tmp.toString().startsWith("[menucascade"))
                {
                    isInMenuCascade = true;
                }
                if (tmp.toString().startsWith("[/menucascade"))
                {
                    isInMenuCascade = false;
                }

                if (tmp.toString().startsWith("[uicontrol"))
                {
                    isInUIControl = true;
                }
                if (tmp.toString().startsWith("[/uicontrol"))
                {
                    isInUIControl = false;
                }

                tmp = new StringBuffer();
                indexStart = i + 1;
            }
            else
            {
                tmp.append(c);
            }
        }

        if (trgRedIndexRegions != null && trgRedIndexRegions.size() > 0)
        {
            Object[] result = new Object[3];
            result[0] = DITA_CHECK1_EXTRA_CONTENT;
            // In this check, source segment does not need highlight.
            result[1] = null;
            Collections.sort(trgRedIndexRegions);
            result[2] = trgRedIndexRegions;
            return result;
        }

        return null;
    }

    /**
     * Empty uicontrol, cite, term, b, i elements. If empty, highlight these
     * tags in report.
     * 
     * @param target
     */
    private Object[] checkEmptyTags(String source, String target)
    {
        if (target == null || target.trim().length() == 0)
            return null;

        int indexStart = -1;
        int indexEnd = -1;
        String startTag, endTag;
        List<String> trgRedIndexRegions = new ArrayList<String>();
        List<String> srcRedIndexRegions = new ArrayList<String>();
        for (String tag : emptyTags)
        {
            for (int i = 1; i <= 20; i++)
            {
                startTag = "[" + tag + i + "]";
                endTag = "[/" + tag + i + "]";
                indexStart = target.indexOf(startTag);
                indexEnd = target.indexOf(endTag);
                if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd)
                {
                    if ((indexStart + startTag.length()) <= indexEnd)
                    {
                        String innerContent = target.substring(indexStart
                                + startTag.length(), indexEnd);
                        if (innerContent == null
                                || innerContent.trim().length() == 0)
                        {
                            trgRedIndexRegions.add(indexStart + "-"
                                    + (indexEnd + endTag.length()));
                            indexStart = source.indexOf(startTag);
                            indexEnd = source.indexOf(endTag);
                            if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd)
                            {
                                srcRedIndexRegions.add(indexStart + "-"
                                        + (indexEnd + endTag.length()));
                            }
                        }
                    }
                }
            }
        }

        if (trgRedIndexRegions != null && trgRedIndexRegions.size() > 0)
        {
            Object[] result = new Object[3];
            result[0] = DITA_CHECK2_EMPTY_TAGS;
            result[1] = srcRedIndexRegions;
            Collections.sort(trgRedIndexRegions);
            result[2] = trgRedIndexRegions;
            return result;
        }

        return null;
    }

    /**
     * Index sorting
     * The localized index entries do not need to be sorted for all languages (except Japanese and Simplifies Chinese) - the index is sorted automatically by the template.
     * For Zh-cn, and JA the indexes do not sort correctly. To get around this we require the translators for these languages to insert an extra element.
     * 
     * Japanese
     * Search for <indexterm>text</indexterm> where text is the kanji JA text
     * They need to insert an element <index-sort-as> and convert the text in <indexterm> to katakana/hiragana
     * Example:
     * <indexterm>[data]</indexterm>
     * becomes
     * <indexterm>[data]<index-sort-as>data</index-sort-as></indexterm>
     * where [data] is kanji and data is katakana
     * Simplified Chinese
     * Simplified Chinese must be sorted based on pinyin sounds
     * Search for <indexterm>text</indexterm> where text is the zh-cn text
     * Insert an element <index-sort-as> and add the text that is in <indexterm> as pinyin
     * Example:
     * <indexterm>[data]</indexterm>
     * becomes
     * <indexterm>[data]<index-sort-as>data</index-sort-as></indexterm>
     * where data is pinyin
     *  
     */
    private Object[] checkIndexSortAs(String source, String target,
            GlobalSightLocale trgLocale)
    {
        if (target == null || target.indexOf("[indexterm") == -1
                || trgLocale == null)
            return null;

        // for "index-sort-as" check, only for "zh_CN", "zh_SG" and "ja_JP".
        String trgLang = trgLocale.toString();
        if (!"zh_CN".equalsIgnoreCase(trgLang)
                && !"zh_SG".equalsIgnoreCase(trgLang)
                && !"ja_JP".equalsIgnoreCase(trgLang))
        {
            return null;
        }

        // find all "indexterm" tags
        Set<String> indextermTags = new HashSet<String>();
        for (int i = 1; i <= 10; i++)
        {
            if (target.indexOf("[indexterm" + i + "]") > -1)
            {
                indextermTags.add("indexterm" + i);
            }
        }

        // check if "index-sort-as" is added for all indexterm tags.
        String tmpStr = target;
        Set<String> failedIndextermTags = new HashSet<String>();
        int safeloop = 0;
        while (indextermTags.size() > 0 && safeloop < 10)
        {
            tmpStr = _ditaCheck3(tmpStr, indextermTags,
                    failedIndextermTags);
            safeloop++;
        }

        // need highlight tags that have no "index-sort-as" added.
        String startTag, endTag;
        int index = -1;
        List<String> srcRedIndexRegions = new ArrayList<String>();
        List<String> trgRedIndexRegions = new ArrayList<String>();
        if (failedIndextermTags.size() > 0)
        {
            for (String tag : failedIndextermTags)
            {
                startTag = "[" + tag + "]";
                index = source.indexOf(startTag);
                srcRedIndexRegions.add(index + "-" + (index + startTag.length()));
                index = target.indexOf(startTag);
                trgRedIndexRegions.add(index + "-" + (index + startTag.length()));

                endTag = "[/" + tag + "]";
                index = source.indexOf(endTag);
                srcRedIndexRegions.add(index + "-" + (index + endTag.length()));
                index = target.indexOf(endTag);
                trgRedIndexRegions.add(index + "-" + (index + endTag.length()));
            }
        }

        if (srcRedIndexRegions.size() > 0 || trgRedIndexRegions.size() > 0)
        {
            Object[] result = new Object[3];
            result[0] = DITA_CHECK3_MISSING_INDEX_SORT_AS;
            Collections.sort(srcRedIndexRegions);
            result[1] = srcRedIndexRegions;
            Collections.sort(trgRedIndexRegions);
            result[2] = trgRedIndexRegions;
            return result;
        }

        return null;
    }

    private static String _ditaCheck3(String verboseStr,
            Set<String> unhandledTags, Set<String> failedIndextermTags)
    {
        int indexStart = -1;
        int indexEnd = -1;
        String startTag, endTag, tag;
        String innerContent = null;
        for (Iterator<String> it = unhandledTags.iterator(); it.hasNext();)
        {
            tag = it.next();
            startTag = "[" + tag + "]";
            endTag = "[/" + tag + "]";
            indexStart = verboseStr.indexOf(startTag);
            indexEnd = verboseStr.indexOf(endTag);
            if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd)
            {
                innerContent = verboseStr.substring(indexStart + startTag.length(), indexEnd);
                // it has no nested "indexterm" tag. 
                if (innerContent == null
                        || innerContent.toLowerCase().indexOf("[indexterm") == -1)
                {
                    // Remove the handled content
                    verboseStr = verboseStr.replace(startTag + innerContent + endTag, "");
                    // remove handled tag
                    it.remove();
                    // check if "index-sort-as" is added
                    if (innerContent.indexOf("<index-sort-as>") == -1
                            || innerContent.indexOf("</index-sort-as>") == -1)
                    {
                        failedIndextermTags.add(tag);
                    }
                }
            }
        }

        return verboseStr;
    }

    private CellStyle getHeaderStyle(Workbook p_workbook) throws Exception
    {
        if (headerStyle == null)
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
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderRight(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);
            cs.setBorderLeft(CellStyle.BORDER_THIN);

            headerStyle = cs;
        }

        return headerStyle;
    }

    private CellStyle getContentStyle(Workbook p_workbook) throws Exception
    {
        if (contentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            contentStyle = style;
        }

        return contentStyle;
    }

    private CellStyle getNoWrapContentStyle(Workbook p_workbook)
            throws Exception
    {
        if (noWrapContentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(false);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            noWrapContentStyle = style;
        }

        return noWrapContentStyle;
    }

    private CellStyle getRtlContentStyle(Workbook p_workbook) throws Exception
    {
        if (rtlContentStyle == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            CellStyle style = p_workbook.createCellStyle();
            style.setFont(font);
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_RIGHT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

            rtlContentStyle = style;
        }

        return rtlContentStyle;
    }

    private CellStyle getUnlockedStyle(Workbook p_workbook) throws Exception
    {
        if (unlockedStyle == null)
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

            unlockedStyle = style;
        }

        return unlockedStyle;
    }

    private Font getNormalFont(Workbook p_workbook)
    {
        if (normalFont == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);

            normalFont = font;
        }

        return normalFont;
    }

    private Font getRedFont(Workbook p_workbook)
    {
        if (redFont == null)
        {
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            font.setColor(HSSFColor.RED.index);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

            redFont = font;
        }

        return redFont;
    }

    @SuppressWarnings("unused")
    private Sheet getSheet(Workbook p_workbook, int index)
    {
        Sheet sheet = p_workbook.getSheetAt(index);
        if (sheet == null)
            sheet = p_workbook.createSheet();
        return sheet;
    }

    private Row getRow(Sheet p_sheet, int p_col)
    {
        Row row = p_sheet.getRow(p_col);
        if (row == null)
            row = p_sheet.createRow(p_col);
        return row;
    }

    private Cell getCell(Row p_row, int index)
    {
        Cell cell = p_row.getCell(index);
        if (cell == null)
            cell = p_row.createCell(index);
        return cell;
    }
}
