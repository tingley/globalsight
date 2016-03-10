package com.globalsight.everest.webapp.pagehandler.administration.reports.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.log4j.Logger;

import com.csvreader.CsvWriter;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.page.PageWordCounts;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.util.comparator.JobComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

/**
 * Used for generate Detailed Word Counts by Job.
 */
public class DetailedWordCountsByJobReportGenerator implements ReportGenerator
{
    static private final Logger logger = Logger
            .getLogger(DetailedWordCountsByJobReportGenerator.class);

    private CsvWriter csvWriter = null;
    private CellStyle contentStyle = null;
    private CellStyle headerStyle = null;
    private CellStyle subTotalStyle = null;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "MM/dd/yy hh:mm:ss a z");

    private int wordCountCol = 0;
    protected ResourceBundle m_bundle = null;
    protected HttpServletRequest m_request = null;
    protected String m_companyName = "";
    private RequestData data = new RequestData();
    String m_userId;

    private class RequestData
    {
        // Report on job Ids
        boolean reportOnJobIds = true;

        boolean wantsAllProjects = false;

        boolean wantsAllLocales = false;

        // Need to search job by projects, status, date
        boolean needSearchJob = false;

        boolean exportWithXlsx = true;

        ArrayList<GlobalSightLocale> trgLocaleList = new ArrayList<GlobalSightLocale>();

        List<Long> projectIdList = new ArrayList<Long>();

        List<String> jobStateList = new ArrayList<String>();

        List<String> workflowStateList = new ArrayList<String>();

        List<Long> jobIds = new ArrayList<Long>();

        boolean inludeMtColumn = false;
    };

    public DetailedWordCountsByJobReportGenerator(HttpServletRequest p_request,
            HttpServletResponse p_response) throws LocaleManagerException,
            RemoteException, GeneralException
    {
        HttpSession session = p_request.getSession();
        m_userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        m_bundle = PageHandler.getBundle(p_request.getSession());
        m_request = p_request;
        m_companyName = UserUtil.getCurrentCompanyName(p_request);
        CompanyThreadLocal.getInstance().setValue(m_companyName);

        setProjectIdList(p_request);
        setTargetLocales(p_request);
        setStates(p_request);
        setExportFormat(p_request);
        String dateFormatString = p_request.getParameter("dateFormat");
        String includeMtColumn = p_request.getParameter("includeMtColumn");
        if ("on".equalsIgnoreCase(includeMtColumn))
        {
            data.inludeMtColumn = true;
        }
        else
        {
            data.inludeMtColumn = false;
        }
        if (dateFormatString != null && !dateFormatString.equals(""))
        {
            dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    public void sendReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales,
            HttpServletResponse p_response) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = data.jobIds;
            p_targetLocales = data.trgLocaleList;
        }
        else if (data.jobIds == null || data.jobIds.size() == 0)
        {
            data.jobIds = p_jobIDS;
        }

        File[] reports = generateReports(p_jobIDS, p_targetLocales);
        ReportHelper.sendFiles(reports, getReportType(), p_response);
    }

    @Override
    public File[] generateReports(List<Long> p_jobIDS,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        if (p_jobIDS == null || p_jobIDS.size() == 0)
        {
            p_jobIDS = data.jobIds;
            p_targetLocales = data.trgLocaleList;
        }
        else if (data.jobIds == null || data.jobIds.size() == 0)
        {
            data.jobIds = p_jobIDS;
        }

        ArrayList<Job> jobs = ReportHelper.getJobListByIDS(p_jobIDS);
        File file;
        if (data.exportWithXlsx)
        {
        	setAllCellStyleNull();
            file = ReportHelper.getXLSReportFile(getReportType(), null);
            Workbook workBook = new SXSSFWorkbook();
            Sheet sheet = workBook.createSheet(m_bundle.getString("jobinfo.tradosmatches"));
            addTitleForXlsx(workBook, sheet);
            addHeaderForXlsx(workBook, sheet);
            addJobsForXlsx(workBook, sheet, jobs, p_targetLocales);
            if (workBook != null)
            {
            	FileOutputStream out = new FileOutputStream(file);
            	addCriteriaSheet(workBook, m_request);
                workBook.write(out);
                out.close();
                ((SXSSFWorkbook)workBook).dispose();
            }
        }
        else
        {
            file = ReportHelper.getReportFile(getReportType(), null, ".csv", null);
            OutputStream out = new FileOutputStream(file);
            csvWriter = new CsvWriter(out, ',', Charset.forName("UTF-8"));
            addJobsForCsv(jobs, p_targetLocales);
            if (csvWriter != null)
            {
                csvWriter.close();
            }
            else
            {
                return new File[0];
            }
        }
        
        List<File> workBooks = new ArrayList<File>();
        workBooks.add(file);
        return ReportHelper.moveReports(workBooks, m_userId);
    }
    
    private void addTitleForXlsx(Workbook p_workBook, 
    		Sheet p_sheet)throws Exception
    {
        String EMEA = CompanyWrapper.getCurrentCompanyName();
        
        Font titleFont = p_workBook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Arial");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle titleStyle = p_workBook.createCellStyle();
        titleStyle.setWrapText(false);
        titleStyle.setFont(titleFont);
        
        Row firRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(firRow, 0);
        titleCell.setCellValue(EMEA + " "
                + m_bundle.getString("file_list_report"));
        titleCell.setCellStyle(titleStyle);
        
        Font ldflFont = p_workBook.createFont();
        ldflFont.setUnderline(Font.U_NONE);
        ldflFont.setFontName("Arial");
        ldflFont.setFontHeightInPoints((short) 10);
        ldflFont.setBoldweight(Font.DEFAULT_CHARSET);
        ldflFont.setColor(IndexedColors.BLACK.getIndex());
        CellStyle ldflStyle = p_workBook.createCellStyle();
        ldflStyle.setWrapText(false);
        ldflStyle.setFont(ldflFont);
        
        Row secRow = getRow(p_sheet, 1);
        Cell ldflCell = getCell(secRow, 0);
        ldflCell.setCellValue(m_bundle.getString("lb_desp_file_list"));
        ldflCell.setCellStyle(ldflStyle);
        
        p_sheet.setColumnWidth(0, 15 * 256);
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeaderForXlsx(Workbook p_workBook, 
    		Sheet p_sheet)throws Exception
    {
        int col = 0;
        Row segHeaderRow = getRow(p_sheet, 2);
        Cell cell_A = getCell(segHeaderRow, col);
        cell_A.setCellValue(m_bundle.getString("lb_job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 15 * 256);
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_B = getCell(segHeaderRow, col);
        cell_B.setCellValue(m_bundle.getString("lb_job_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_C = getCell(segHeaderRow, col);
        cell_C.setCellValue(m_bundle.getString("lb_file_path"));
        cell_C.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 55 * 256);
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_D = getCell(segHeaderRow, col);
        cell_D.setCellValue(m_bundle.getString("lb_file_name"));
        cell_D.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 25 * 256);
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workBook));
        col++;
        // p_sheet.addCell(new Label(col, 2, bundle.getString("lb_loc_profile"),
        // headerFormat));
        // p_sheet.setColumnView(col, 20);
        // p_sheet.mergeCells(col, 2, col, 3);
        // col++;
        // p_sheet.addCell(new Label(col, 2,
        // bundle.getString("lb_file_profile"),
        // headerFormat));
        // p_sheet.setColumnView(col, 20);
        // p_sheet.mergeCells(col, 2, col, 3);
        // col++;
        Cell cell_E = getCell(segHeaderRow, col);
        cell_E.setCellValue(m_bundle.getString("reportDesc"));
        cell_E.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 30 * 256);
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col), 
        		getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_F = getCell(segHeaderRow, col);
        cell_F.setCellValue(m_bundle.getString("lb_creation_date"));
        cell_F.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 25 * 256);
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_G = getCell(segHeaderRow, col);
        cell_G.setCellValue(m_bundle.getString("lb_lang"));
        cell_G.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
        		getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_H_Header = getCell(segHeaderRow, col);
        cell_H_Header.setCellValue(m_bundle.getString("lb_word_counts"));
        cell_H_Header.setCellStyle(getHeaderStyle(p_workBook));
        wordCountCol = col;
        int span = 7;
        if (data.inludeMtColumn)
        {
            span += 1;
        }

        p_sheet.addMergedRegion(new CellRangeAddress(2, 2, col, col + span));
        setRegionStyle(p_sheet, new CellRangeAddress(2, 2, col, col + span), 
        		getHeaderStyle(p_workBook));

        segHeaderRow = getRow(p_sheet, 3);
        Cell cell_H = getCell(segHeaderRow, col);
        cell_H.setCellValue(m_bundle
                .getString("jobinfo.tradosmatches.wordcounts.per100matches"));
        cell_H.setCellStyle(getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_I = getCell(segHeaderRow, col);
        cell_I.setCellValue(m_bundle.getString("lb_95_99"));
        cell_I.setCellStyle(getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_J = getCell(segHeaderRow, col);
        cell_J.setCellValue(m_bundle.getString("lb_85_94"));
        cell_J.setCellStyle(getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_K = getCell(segHeaderRow, col);
        cell_K.setCellValue(m_bundle.getString("lb_75_84") + "*");
        cell_K.setCellStyle(getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_L = getCell(segHeaderRow, col);
        cell_L.setCellValue(m_bundle.getString("lb_no_match"));
        cell_L.setCellStyle(getHeaderStyle(p_workBook));
        col++;
        
        Cell cell_M = getCell(segHeaderRow, col);
        cell_M.setCellValue(m_bundle.getString("lb_repetition_word_cnt"));
        cell_M.setCellStyle(getHeaderStyle(p_workBook));
        p_sheet.setColumnWidth(col, 13 * 256);
        col++;

        Cell cell_InContext = getCell(segHeaderRow, col);
        cell_InContext.setCellValue(m_bundle.getString("lb_in_context_tm"));
        cell_InContext.setCellStyle(getHeaderStyle(p_workBook));

        if (data.inludeMtColumn)
        {
            col++;
            Cell cell_MT = getCell(segHeaderRow, col);
            cell_MT.setCellValue("MT");
            cell_MT.setCellStyle(getHeaderStyle(p_workBook));
        }

        col++;
        Cell cell_Total = getCell(segHeaderRow, col);
        cell_Total.setCellValue(m_bundle.getString("lb_total"));
        cell_Total.setCellStyle(getHeaderStyle(p_workBook));

        if (data.inludeMtColumn)
        {
        	segHeaderRow = getRow(p_sheet, 2);
            col++;
            Cell cell_Score = getCell(segHeaderRow, col);
            cell_Score.setCellValue(m_bundle.getString("lb_tm_mt_confidence_score"));
            cell_Score.setCellStyle(getHeaderStyle(p_workBook));
            p_sheet.addMergedRegion(new CellRangeAddress(2, 3, col, col));
            setRegionStyle(p_sheet, new CellRangeAddress(2, 3, col, col),
            		getHeaderStyle(p_workBook));
            p_sheet.setColumnWidth(col, 20 * 256);
        }
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addJobsForXlsx(Workbook p_workbook,Sheet p_sheet, ArrayList<Job> p_jobs,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        // sort jobs by job name
        SortUtil.sort(p_jobs, new JobComparator(Locale.US));

        IntHolder row = new IntHolder(4);
        int finishedJobNum = 0;
        for (Job j : p_jobs)
        {
            // Cancel generate reports.
            if (isCancelled())
            {
                p_workbook = null;
                printMsg("Cancelled Report Generator. " + j);
                return;
            }

            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            for (Workflow w : j.getWorkflows())
            {
                String state = w.getState();
                // skip certain workflow whose target locale is not selected
                if (!p_targetLocales.contains(w.getTargetLocale()))
                {
                    continue;
                }
                // if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    addWorkflowForXlsx(p_workbook, p_sheet, j, w, row);
                }
            }
            printMsg("Finished Job: " + j);
        }

        row.inc();
        addTotals(p_workbook, p_sheet, row);
    }
    
    /**
     * Gets the task for the workflow and outputs page information.
     * 
     * @exception Exception
     */
    private void addWorkflowForXlsx(Workbook p_workBook, Sheet p_sheet, Job p_job,
            Workflow p_workflow, IntHolder p_row) throws Exception
    {
        int threshold = p_job.getLeverageMatchThreshold();
        int mtConfidenceScore = p_workflow.getMtConfidenceScore();
        // write word count and file info
        for (TargetPage tg : p_workflow.getTargetPages())
        {
            // write job id, job name, description, creation date, creation
            // time, language
        	Row row = getRow(p_sheet, p_row.value);
        	CellStyle cs = getContentStyle(p_workBook);
            Cell cell_A = getCell(row, 0);
            cell_A.setCellValue(p_job.getId());
            cell_A.setCellStyle(cs);
            
            Cell cell_B = getCell(row, 1);
            cell_B.setCellValue(p_job.getJobName());
            cell_B.setCellStyle(cs);

            // file path 2 & file name 3
            try
            {
                String[] pathName = getFilePathName(tg);
                Cell cell_C = getCell(row, 2);
                cell_C.setCellValue(pathName[0]);
                cell_C.setCellStyle(cs);
                
                Cell cell_D = getCell(row, 3);
                cell_D.setCellValue(pathName[1]);
                cell_D.setCellStyle(cs);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            Cell cell_E = getCell(row, 4);
            cell_E.setCellValue(getProjectDesc(p_job));
            cell_E.setCellStyle(cs);
            
            Cell cell_F = getCell(row, 5);
            cell_F.setCellValue(dateFormat.format(p_job.getCreateDate()));
            cell_F.setCellStyle(cs);
            
            Cell cell_G = getCell(row,6);
            cell_G.setCellValue(p_workflow.getTargetLocale().toString());
            cell_G.setCellStyle(cs);

            try
            {
                addWordCountForXlsx(p_workBook, tg, p_row, p_sheet, threshold,
                        mtConfidenceScore);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            p_row.inc();
        }
    }
    
    /**
     * Add word count for Excel File
     */
    private int addWordCountForXlsx(Workbook p_workBook, TargetPage tg,
            IntHolder p_row, Sheet p_sheet, int threshold, int mtConfidenceScore)
            throws Exception
    {
        Job job = tg.getSourcePage().getRequest().getJob();
        boolean isInContextMatch = PageHandler.isInContextMatch(job);
        PageWordCounts pageWC = tg.getWordCount();

        // 100% match
        int _100MatchWordCount = 0;
        // in context word match
        int inContextWordCount = 0;

        if (isInContextMatch)
        {
            inContextWordCount = pageWC.getInContextWordCount();
            _100MatchWordCount = pageWC.getSegmentTmWordCount();
        }
        else
        {
        	_100MatchWordCount = pageWC.getTotalExactMatchWordCount();
        }

        int hiFuzzyWordCount = pageWC.getThresholdHiFuzzyWordCount();
        int medHiFuzzyWordCount = pageWC.getThresholdMedHiFuzzyWordCount();
        int medFuzzyWordCount = pageWC.getThresholdMedFuzzyWordCount();
        int lowFuzzyWordCount = pageWC.getThresholdLowFuzzyWordCount();
        int noMatchWordCount = pageWC.getThresholdNoMatchWordCount();
        int repetitionsWordCount = pageWC.getRepetitionWordCount();
        int totalWords = pageWC.getTotalWordCount();
        // MT
        int mtTotalWordCount = pageWC.getMtTotalWordCount();
        int mtExactMatchWordCount = pageWC.getMtExactMatchWordCount();
        int mtFuzzyNoMatchWordCount = pageWC.getMtFuzzyNoMatchWordCount();
        int mtRepetitionsWordCount = pageWC.getMtRepetitionsWordCount();

        int noMatchWorcCountForDisplay = lowFuzzyWordCount + noMatchWordCount;
        // If include MT column, need adjust word count according to threshold
        // and MT confidence score.
        if (data.inludeMtColumn)
        {
            if (mtConfidenceScore == 100)
            {
                _100MatchWordCount = _100MatchWordCount - mtExactMatchWordCount;
            }
            else if (mtConfidenceScore < 100 && mtConfidenceScore >= threshold)
            {
                if (mtConfidenceScore >= 95)
                {
                    hiFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 95 && mtConfidenceScore >= 85)
                {
                    medHiFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 85 && mtConfidenceScore >= 75)
                {
                    medFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 75)
                {
                    noMatchWorcCountForDisplay -= mtFuzzyNoMatchWordCount;
                }
                repetitionsWordCount -= mtRepetitionsWordCount;
            }
            else if (mtConfidenceScore < threshold)
            {
                noMatchWorcCountForDisplay -= mtFuzzyNoMatchWordCount;
                repetitionsWordCount -= mtRepetitionsWordCount;
            }
        }

        
        // write the information of word count
        int cursor = wordCountCol;
        Row row = getRow(p_sheet, p_row.value);
        CellStyle cs = getContentStyle(p_workBook);
        Cell cell_H = getCell(row, cursor);
        cell_H.setCellValue(_100MatchWordCount);
        cell_H.setCellStyle(cs);
        
        Cell cell_I = getCell(row, ++cursor);
        cell_I.setCellValue(hiFuzzyWordCount);
        cell_I.setCellStyle(cs);
        
        Cell cell_J = getCell(row, ++cursor);
        cell_J.setCellValue(medHiFuzzyWordCount);
        cell_J.setCellStyle(cs);
        
        Cell cell_K = getCell(row, ++cursor);
        cell_K.setCellValue(medFuzzyWordCount);
        cell_K.setCellStyle(cs);
        
        Cell cell_L = getCell(row, ++cursor);
        cell_L.setCellValue(noMatchWorcCountForDisplay);
        cell_L.setCellStyle(cs);
        
        Cell cell_M = getCell(row, ++cursor);
        cell_M.setCellValue(repetitionsWordCount);
        cell_M.setCellStyle(cs);

        cursor++;
        Cell cell_InContext = getCell(row, cursor);
        if (isInContextMatch)
        {
        	cell_InContext.setCellValue(inContextWordCount);
        	cell_InContext.setCellStyle(cs);
        }
        else
        {
        	cell_InContext.setCellValue(0);
        	cell_InContext.setCellStyle(cs);
        }

        if (data.inludeMtColumn)
        {
            cursor++;
            Cell cell_MT = getCell(row, cursor);
            cell_MT.setCellValue(mtTotalWordCount);
            cell_MT.setCellStyle(cs);
        }
         cursor++;

        Cell cell_Total = getCell(row, cursor);
        cell_Total.setCellValue(totalWords);
        cell_Total.setCellStyle(cs);

        if (data.inludeMtColumn)
        {
            cursor++;
            Cell cell_Score = getCell(row, cursor);
            cell_Score.setCellValue(mtConfidenceScore);
            cell_Score.setCellStyle(cs);
        }

        return totalWords;
    }

    /**
     * Add totals for Excel File
     * 
     * @param p_sheet
     * @param p_row
     * @param bundle
     * @throws Exception
     */
    private void addTotals(Workbook p_workbook, Sheet p_sheet, IntHolder p_row)
    	throws Exception
    {
        int row = p_row.getValue() + 1; // skip a row       
        // For "Add Job Id into online job report" issue
        String title = m_bundle.getString("lb_totals");
        char sumStartCellCol = 'H';
        int c = 7;
        
        Row segHeaderRow = getRow(p_sheet, row);
        Cell cell_A = getCell(segHeaderRow, 0);
        cell_A.setCellValue(title);
        p_sheet.addMergedRegion(new CellRangeAddress(row, row, 0, sumStartCellCol - 'B'));
        setRegionStyle(p_sheet, new CellRangeAddress(row, row, 0, sumStartCellCol - 'B'),
        		getSubTotalStyle(p_workbook));
        int lastRow = p_row.getValue() - 1;

        // add in word count totals
        // word counts
        Cell cell_H = getCell(segHeaderRow, c++);
        cell_H.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_H.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;
        // Matches
        Cell cell_I = getCell(segHeaderRow, c++);
        cell_I.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_I.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;
        
        Cell cell_J = getCell(segHeaderRow, c++);
        cell_J.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_J.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;
        
        Cell cell_K = getCell(segHeaderRow, c++);
        cell_K.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_K.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;
        
        Cell cell_L = getCell(segHeaderRow, c++);
        cell_L.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_L.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;
        
        Cell cell_M = getCell(segHeaderRow, c++);
        cell_M.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_M.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;

    	Cell cell_InContext = getCell(segHeaderRow, c++);
    	cell_InContext.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
    	cell_InContext.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;

        if (data.inludeMtColumn)
        {
        	Cell cell_MT = getCell(segHeaderRow, c++);
        	cell_MT.setCellFormula("SUM(" + sumStartCellCol + "5:"
                    + sumStartCellCol + lastRow + ")");
        	cell_MT.setCellStyle(getSubTotalStyle(p_workbook));
            sumStartCellCol++;
        }
        
        Cell cell_Total = getCell(segHeaderRow, c++);
        cell_Total.setCellFormula("SUM(" + sumStartCellCol + "5:"
                + sumStartCellCol + lastRow + ")");
        cell_Total.setCellStyle(getSubTotalStyle(p_workbook));
        sumStartCellCol++;

        // Add empty black sum line for MT Confidence Score.
        if (data.inludeMtColumn)
        {
        	Cell cell_Score = getCell(segHeaderRow, c++);
        	cell_Score.setCellStyle(getSubTotalStyle(p_workbook));
            sumStartCellCol++;
        }
    }
    
    /**
     * Add criteria sheet
     * 
     * @param p_workbook
     * @param p_request
     * @throws Exception
     */
    private void addCriteriaSheet(Workbook p_workbook, HttpServletRequest p_request)
            throws Exception
    {
    	Sheet paramsSheet = p_workbook.createSheet(m_bundle.getString("lb_criteria"));
    	Row firRow = getRow(paramsSheet, 0);
    	Cell cell_A_Title = getCell(firRow, 0);
    	cell_A_Title.setCellValue(m_bundle.getString("lb_report_criteria"));
    	cell_A_Title.setCellStyle(getContentStyle(p_workbook));

    	paramsSheet.setColumnWidth(0, 50 * 256);

    	Row secRow = getRow(paramsSheet, 1);
    	Cell cell_A_Header = getCell(secRow, 0);
        if (data.wantsAllProjects)
        {
        	cell_A_Header.setCellValue(m_bundle
                    .getString("lb_selected_projects")
                    + " "
                    + m_bundle.getString("all"));
        	cell_A_Header.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_A_Header.setCellValue(m_bundle
                    .getString("lb_selected_projects"));
        	cell_A_Header.setCellStyle(getContentStyle(p_workbook));
            Iterator<Long> iter = data.projectIdList.iterator();
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
                }
                Row row = getRow(paramsSheet, r);
                Cell cell_A = getCell(row, 0);
                cell_A.setCellValue(projectName);
                cell_A.setCellStyle(getContentStyle(p_workbook));
            	
            	Cell cell_B = getCell(row, 1);
            	cell_B.setCellValue(m_bundle.getString("lb_id")
                        + "=" + pid.toString());
            	cell_B.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }

        String from = "";
        String to = "";
        String startCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String startOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);
        if (startOpts != null && !"-1".equals(startOpts))
        {
            from = startCount + " " + translateOpts(startOpts);
        }

        String endCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String endOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);

        if (startOpts != null && !"-1".equals(endOpts))
        {
            to = endCount + " " + translateOpts(endOpts);
        }

        Row thirRow = getRow(paramsSheet, 2);
        Cell cell_C_Header = getCell(secRow, 2);
        cell_C_Header.setCellValue(m_bundle.getString("lb_from") + ":");
        cell_C_Header.setCellStyle(getContentStyle(p_workbook));
    	
    	Cell cell_C = getCell(thirRow, 2);
    	cell_C.setCellValue(from);
    	cell_C.setCellStyle(getContentStyle(p_workbook));
    	
    	Cell cell_D_Header = getCell(secRow, 3);
    	cell_D_Header.setCellValue(m_bundle.getString("lb_until") + ":");
    	cell_D_Header.setCellStyle(getContentStyle(p_workbook));
    	
    	Cell cell_D = getCell(thirRow, 3);
    	cell_D.setCellValue(to);
    	cell_D.setCellStyle(getContentStyle(p_workbook));

    	Cell cell_E_Header = getCell(secRow, 4);
        if (data.wantsAllLocales)
        {
        	cell_E_Header.setCellValue(m_bundle
                    .getString("lb_selected_langs")
                    + " "
                    + m_bundle.getString("all"));
        	cell_E_Header.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
        	cell_E_Header.setCellValue(m_bundle.getString("lb_selected_langs"));
        	cell_E_Header.setCellStyle(getContentStyle(p_workbook));
            Iterator<GlobalSightLocale> iter = data.trgLocaleList.iterator();
            int r = 2;
            while (iter.hasNext())
            {
                String locale = iter.next().toString();
                Row row = getRow(paramsSheet, r);
                Cell cell_E = getCell(row, 4);
            	cell_E.setCellValue(locale);
            	cell_E.setCellStyle(getContentStyle(p_workbook));
                r++;
            }
        }

    }
    
    @Override
    public String getReportType()
    {
        return ReportConstants.DETAILED_WORDCOUNTS_REPORT;
    }

    private void setProjectIdList(HttpServletRequest p_request)
    {
        data.projectIdList.clear();
        data.wantsAllProjects = false;
        // set the project Id
        String[] projectIds = p_request.getParameterValues("projectNameList");
        if (projectIds != null)
        {
            for (int i = 0; i < projectIds.length; i++)
            {
                String id = projectIds[i];
                if (id.equals("*"))
                {
                    data.wantsAllProjects = true;
                    break;
                }
                else
                {
                    data.projectIdList.add(new Long(id));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
	private void setTargetLocales(HttpServletRequest p_request)
            throws LocaleManagerException, RemoteException, GeneralException
    {
        data.trgLocaleList.clear();
        data.wantsAllLocales = false;
        String[] paramTrgLocales = p_request
                .getParameterValues(ReportConstants.TARGETLOCALE_LIST);
        if (paramTrgLocales != null)
        {
            for (int i = 0; i < paramTrgLocales.length; i++)
            {
                if ("*".equals(paramTrgLocales[i]))
                {
                    data.wantsAllLocales = true;
                    data.trgLocaleList = new ArrayList<GlobalSightLocale>(
                            ServerProxy.getLocaleManager()
                                    .getAllTargetLocales());
                    break;
                }
                else
                {
                    long localeId = Long.parseLong(paramTrgLocales[i]);
                    GlobalSightLocale locale = null;
                    try
                    {
                        locale = ServerProxy.getLocaleManager().getLocaleById(
                                localeId);
                    }
                    catch (Exception e)
                    {
                    }

                    if (locale != null && !data.trgLocaleList.contains(locale))
                    {
                        data.trgLocaleList.add(locale);
                    }
                }
            }
        }
    }

    /**
     * Set job and workflow status
     * 
     * @param p_request
     */
    private void setStates(HttpServletRequest p_request)
    {
        String reportOn = (String) p_request.getParameter("reportOn");
        data.reportOnJobIds = "jobIds".equals(reportOn);

        if (data.reportOnJobIds)
        {
            // Report on Job IDs
            data.jobIds.clear();
            String jobIdsStr = p_request.getParameter("jobIds");
            if (jobIdsStr != null && jobIdsStr.length() != 0)
            {
                String[] jobIds = jobIdsStr.split(",");
                long jobId = 0;
                for (int i = 0; i < jobIds.length; i++)
                {
                    try
                    {
                        jobId = Long.valueOf(jobIds[i].trim());
                    }
                    catch (Exception e)
                    {
                        continue;
                    }
                    if (!data.jobIds.contains(jobId))
                    {
                        data.jobIds.add(jobId);
                    }
                }
            }
            data.needSearchJob = false;
        }
        else
        {
            String[] jobIds = p_request.getParameterValues("jobNameList");
            data.needSearchJob = (jobIds == null);
            if (!data.needSearchJob)
            {
                data.jobIds.clear();
                long jobId = 0;
                for (int i = 0; i < jobIds.length; i++)
                {
                    jobId = Long.valueOf(jobIds[i]);
                    if (!data.jobIds.contains(jobId))
                    {
                        data.jobIds.add(jobId);
                    }
                }
            }
            else
            {
                data.jobStateList.clear();
                data.workflowStateList.clear();
                String[] states = p_request.getParameterValues("jobStatus");
                List<String> lst = new ArrayList<String>();
                if (states != null)
                {
                    lst = Arrays.asList(states);
                }
                if (lst.contains("*"))
                {
                    data.jobStateList.add(Job.READY_TO_BE_DISPATCHED);
                    data.workflowStateList.add(Workflow.READY_TO_BE_DISPATCHED);
                    data.jobStateList.add(Job.PENDING);
                    data.workflowStateList.add(Workflow.PENDING);
                    data.jobStateList.add(Job.DISPATCHED);
                    data.workflowStateList.add(Workflow.DISPATCHED);
                    data.jobStateList.add(Job.LOCALIZED);
                    data.workflowStateList.add(Workflow.LOCALIZED);
                    data.jobStateList.add(Job.EXPORTED);
                    data.jobStateList.add(Job.EXPORT_FAIL);
                    data.workflowStateList.add(Workflow.EXPORTED);
                    data.workflowStateList.add(Workflow.EXPORT_FAILED);
                    data.jobStateList.add(Job.ARCHIVED);
                    data.workflowStateList.add(Workflow.ARCHIVED);
                }
                else
                {
                    if (lst.contains(Job.READY_TO_BE_DISPATCHED))
                    {
                        data.jobStateList.add(Job.READY_TO_BE_DISPATCHED);
                        data.workflowStateList
                                .add(Workflow.READY_TO_BE_DISPATCHED);
                    }
                    if (lst.contains(Job.PENDING))
                    {
                        data.jobStateList.add(Job.PENDING);
                        data.workflowStateList.add(Workflow.PENDING);
                    }
                    if (lst.contains(Job.DISPATCHED))
                    {
                        data.jobStateList.add(Job.DISPATCHED);
                        data.workflowStateList.add(Workflow.DISPATCHED);
                    }
                    if (lst.contains(Job.LOCALIZED))
                    {
                        data.jobStateList.add(Job.LOCALIZED);
                        data.workflowStateList.add(Workflow.LOCALIZED);
                    }
                    if (lst.contains(Job.EXPORTED))
                    {
                        data.jobStateList.add(Job.EXPORTED);
                        data.workflowStateList.add(Workflow.EXPORTED);
                    }
                    if (lst.contains(Job.EXPORT_FAIL))
                    {
                        data.jobStateList.add(Job.EXPORT_FAIL);
                        data.workflowStateList.add(Workflow.EXPORT_FAILED);
                    }
                    if (lst.contains(Job.ARCHIVED))
                    {
                        data.jobStateList.add(Job.ARCHIVED);
                        data.workflowStateList.add(Workflow.ARCHIVED);
                    }
                }
            }
        }
    }

    /**
     * Set Export Format
     * 
     * @param p_request
     */
    public void setExportFormat(HttpServletRequest p_request)
    {
        String exportFormat = p_request.getParameter("exportFormat");
        if (exportFormat != null)
            exportFormat = exportFormat.toLowerCase();
        data.exportWithXlsx = "xlsx".equals(exportFormat);
    }

    private String translateOpts(String opt)
    {
        String optLabel = "";
        if (SearchCriteriaParameters.NOW.equals(opt))
        {
            optLabel = m_bundle.getString("lb_now");
        }
        else if (SearchCriteriaParameters.HOURS_AGO.equals(opt))
        {
            optLabel = m_bundle.getString("lb_hours_ago");
        }
        else if (SearchCriteriaParameters.DAYS_AGO.equals(opt))
        {
            optLabel = m_bundle.getString("lb_days_ago");
        }
        else if (SearchCriteriaParameters.WEEKS_AGO.equals(opt))
        {
            optLabel = m_bundle.getString("lb_weeks_ago");
        }
        else if (SearchCriteriaParameters.MONTHS_AGO.equals(opt))
        {
            optLabel = m_bundle.getString("lb_months_ago");
        }

        return optLabel;
    }

    /**
     * Adds the header for CSV File
     * 
     * @param p_sheet
     * @author Leon.Song
     */
    private void addHeaderForCsv(ResourceBundle bundle) throws Exception
    {
        csvWriter.write(bundle.getString("lb_job_id"));
        csvWriter.write(bundle.getString("lb_job_name"));
        csvWriter.write(bundle.getString("lb_file_path"));
        csvWriter.write(bundle.getString("lb_file_name"));
        // csvWriter.write(bundle.getString("lb_loc_profile"));
        // csvWriter.write(bundle.getString("lb_file_profile"));
        csvWriter.write(bundle.getString("reportDesc"));
        csvWriter.write(bundle.getString("lb_creation_date"));
        csvWriter.write(bundle.getString("lb_lang"));
        csvWriter.write(bundle
                .getString("jobinfo.tradosmatches.wordcounts.per100matches"));
        csvWriter.write(bundle.getString("lb_95_99"));
        csvWriter.write(bundle.getString("lb_85_94"));
        csvWriter.write(bundle.getString("lb_75_84") + "*");
        csvWriter.write(bundle.getString("lb_no_match"));
        csvWriter.write(bundle.getString("lb_repetition_word_cnt"));
        csvWriter.write(bundle.getString("lb_in_context_tm"));
        if (data.inludeMtColumn)
        {
            csvWriter.write("MT");
        }
        csvWriter.write(bundle.getString("lb_total"));
        if (data.inludeMtColumn)
        {
            csvWriter.write(bundle.getString("lb_tm_mt_confidence_score"));
        }
        csvWriter.endRecord();
    }

    /**
     * Gets the jobs and outputs workflow information for CSV File.
     * 
     * @exception Exception
     * @author Leon.Song
     */
    private void addJobsForCsv(ArrayList<Job> p_jobs,
            List<GlobalSightLocale> p_targetLocales) throws Exception
    {
        // sort jobs by job name
        SortUtil.sort(p_jobs, new JobComparator(Locale.US));

        addHeaderForCsv(m_bundle);

        int finishedJobNum = 0;
        for (Job j : p_jobs)
        {
            // Cancel generate reports.
            if (isCancelled())
            {
                csvWriter = null;
                printMsg("Cancelled Report Generator. " + j);
                return;
            }
            // Sets Reports Percent.
            setPercent(++finishedJobNum);

            for (Workflow w : j.getWorkflows())
            {
                String state = w.getState();
                // skip certain workflow whose target locale is not selected
                if (!p_targetLocales.contains(w.getTargetLocale()))
                {
                    continue;
                }
                // if (data.workflowStateList.contains(state))
                if (Workflow.DISPATCHED.equals(state)
                        || Workflow.READY_TO_BE_DISPATCHED.equals(state)
                        || Workflow.PENDING.equals(state)
                        || Workflow.EXPORTED.equals(state)
                        || Workflow.EXPORT_FAILED.equals(state)
                        || Workflow.ARCHIVED.equals(state)
                        || Workflow.LOCALIZED.equals(state))
                {
                    addWorkflowForCsv(j, w);
                }
            }

            printMsg("Finished Job: " + j);
        }
    }

    private String getProjectDesc(Job p_job)
    {
        Project p = p_job.getL10nProfile().getProject();
        String d = p.getDescription();
        String desc = null;
        if (d == null || d.length() == 0)
            desc = p.getName();
        else
            desc = p.getName() + ": " + d;
        return desc;
    }

    /**
     * Gets the task for the workflow and outputs page information for CSV File.
     * 
     * @exception Exception
     * @author Leon.Song
     */
    private void addWorkflowForCsv(Job p_job, Workflow p_workflow)
            throws Exception
    {
        int threshold = p_job.getLeverageMatchThreshold();
        int mtConfidenceScore = p_workflow.getMtConfidenceScore();
        // write word count and file info
        for (TargetPage tg : p_workflow.getTargetPages())
        {
            // write job id, job name, description, creation date, creation
            // time, language
            csvWriter.write(String.valueOf(p_job.getId()));
            csvWriter.write(p_job.getJobName());
            String[] filePathName = getFilePathName(tg);
            csvWriter.write(filePathName[0]);
            csvWriter.write(filePathName[1]);
            // csvWriter.write(p_job.getL10nProfile().getName());
            // csvWriter.write(getFileProfileNameForDisplay(tg));
            csvWriter.write(getProjectDesc(p_job));
            csvWriter.write(dateFormat.format(p_job.getCreateDate()));
            csvWriter.write(p_workflow.getTargetLocale().toString());

            try
            {
                addWordCountForCsv(tg, threshold, mtConfidenceScore);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            csvWriter.endRecord();
        }
    }

    private String[] getFilePathName(TargetPage tg) throws Exception
    {
        String[] filePathName = new String[2];

        String fileFullName = tg.getSourcePage().getExternalPageId();
        String filePath = fileFullName;
        String fileName = " ";
        if (fileFullName.indexOf("/") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("/") + 1, fileFullName.length());
            filePath = fileFullName.substring(0, fileFullName.lastIndexOf("/"));
        }
        else if (fileFullName.indexOf("\\") > -1)
        {
            fileName = fileFullName.substring(
                    fileFullName.lastIndexOf("\\") + 1, fileFullName.length());
            filePath = fileFullName
                    .substring(0, fileFullName.lastIndexOf("\\"));
        }

        filePathName[0] = filePath;
        filePathName[1] = fileName;

        return filePathName;
    }

    private void addWordCountForCsv(TargetPage tg, int threshold,
            int mtConfidenceScore) throws Exception
    {
        Job job = tg.getSourcePage().getRequest().getJob();
        boolean isInContextMatch = PageHandler.isInContextMatch(job);
        PageWordCounts pageWC = tg.getWordCount();

        // 100% match
        int _100MatchWordCount = 0;
        // in context word match
        int inContextWordCount = 0;

        if (isInContextMatch)
        {
            inContextWordCount = pageWC.getInContextWordCount();
            _100MatchWordCount = pageWC.getSegmentTmWordCount();
        }
        else
        {
        	_100MatchWordCount = pageWC.getTotalExactMatchWordCount();
        }

        // 95% match
        int hiFuzzyWordCount = pageWC.getThresholdHiFuzzyWordCount();
        // 85% match
        int medHiFuzzyWordCount = pageWC.getThresholdMedHiFuzzyWordCount();
        // 85% match
        int medFuzzyWordCount = pageWC.getThresholdMedFuzzyWordCount();
        // 50% match
        int lowFuzzyWordCount = pageWC.getThresholdLowFuzzyWordCount();
        // no match
        int noMatchWordCount = pageWC.getThresholdNoMatchWordCount();
        // repetition
        int repetitionsWordCount = pageWC.getRepetitionWordCount();
        int totalWords = pageWC.getTotalWordCount();
        // MT
        int mtTotalWordCount = pageWC.getMtTotalWordCount();
        int mtExactMatchWordCount = pageWC.getMtExactMatchWordCount();
        int mtFuzzyNoMatchWordCount = pageWC.getMtFuzzyNoMatchWordCount();
        int mtRepetitionsWordCount = pageWC.getMtRepetitionsWordCount();

        int noMatchWorcCountForDisplay = lowFuzzyWordCount + noMatchWordCount;
        // If include MT column, need adjust word count according to threshold
        // and MT confidence score.
        if (data.inludeMtColumn)
        {
            if (mtConfidenceScore == 100)
            {
                _100MatchWordCount = _100MatchWordCount - mtExactMatchWordCount;
            }
            else if (mtConfidenceScore < 100 && mtConfidenceScore >= threshold)
            {
                if (mtConfidenceScore >= 95)
                {
                    hiFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 95 && mtConfidenceScore >= 85)
                {
                    medHiFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 85 && mtConfidenceScore >= 75)
                {
                    medFuzzyWordCount -= mtFuzzyNoMatchWordCount;
                }
                else if (mtConfidenceScore < 75)
                {
                    noMatchWorcCountForDisplay -= mtFuzzyNoMatchWordCount;
                }
                repetitionsWordCount -= mtRepetitionsWordCount;
            }
            else if (mtConfidenceScore < threshold)
            {
                noMatchWorcCountForDisplay -= mtFuzzyNoMatchWordCount;
                repetitionsWordCount -= mtRepetitionsWordCount;
            }
        }

        // write the information of word count
        csvWriter.write(String.valueOf(_100MatchWordCount));
        csvWriter.write(String.valueOf(hiFuzzyWordCount));
        csvWriter.write(String.valueOf(medHiFuzzyWordCount));
        csvWriter.write(String.valueOf(medFuzzyWordCount));
        csvWriter.write(String.valueOf(noMatchWorcCountForDisplay));
        csvWriter.write(String.valueOf(repetitionsWordCount));

        if (isInContextMatch)
        {
            csvWriter.write(String.valueOf(inContextWordCount));
        }
        else
        {
            csvWriter.write(String.valueOf(0));
        }

        if (data.inludeMtColumn)
        {
            csvWriter.write(String.valueOf(mtTotalWordCount));
        }

        csvWriter.write(String.valueOf(totalWords));

        if (data.inludeMtColumn)
        {
            csvWriter.write(String.valueOf(mtConfidenceScore));
        }
    }
    
    private void setAllCellStyleNull()
    {
        this.contentStyle = null;
    }
    
    private CellStyle getHeaderStyle(Workbook p_workbook){
    	if(headerStyle == null){
    		Font headerFont = p_workbook.createFont();
    		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
    		headerFont.setColor(IndexedColors.BLACK.getIndex());
    		headerFont.setUnderline(Font.U_NONE);
    		headerFont.setFontName("Arial");
    		headerFont.setFontHeightInPoints((short) 9);
    		
    		CellStyle cs = p_workbook.createCellStyle();
    		cs.setFont(headerFont);
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
            CellStyle style = p_workbook.createCellStyle();
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
    }
    
    private CellStyle getSubTotalStyle(Workbook p_workbook) throws Exception
    {
        if (subTotalStyle == null)
        {
        	Font subTotalFont = p_workbook.createFont();
            subTotalFont.setUnderline(Font.U_NONE);
            subTotalFont.setFontName("Arial");
            subTotalFont.setFontHeightInPoints((short) 10);
            subTotalFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

            CellStyle cs = p_workbook.createCellStyle();
            cs.setFont(subTotalFont);
            cs.setWrapText(true);
            cs.setFillPattern(CellStyle.SOLID_FOREGROUND );
            cs.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cs.setBorderTop(CellStyle.BORDER_THIN);
            cs.setBorderBottom(CellStyle.BORDER_THIN);

            subTotalStyle = cs;
        }

        return subTotalStyle;
    }
    
    public void setRegionStyle(Sheet sheet, CellRangeAddress cellRangeAddress,
    		CellStyle cs) {
    		for (int i = cellRangeAddress.getFirstRow(); i <= cellRangeAddress.getLastRow();
    			i++) {
    			Row row = getRow(sheet, i);
    			for (int j = cellRangeAddress.getFirstColumn(); 
    				j <= cellRangeAddress.getLastColumn(); j++) {
    				Cell cell = getCell(row, j);
    				cell.setCellStyle(cs);
    			}
    	  }
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
    
    @Override
    public void setPercent(int p_finishedJobNum)
    {
        ReportGeneratorHandler.setReportsMapByGenerator(m_userId, data.jobIds,
                100 * p_finishedJobNum / data.jobIds.size(), getReportType());
    }

    @Override
    public boolean isCancelled()
    {
        return ReportGeneratorHandler.isCancelled(m_userId, null,
                getReportType());
    }

    private void printMsg(String p_msg)
    {
        // logger.info(p_msg);
    }
}