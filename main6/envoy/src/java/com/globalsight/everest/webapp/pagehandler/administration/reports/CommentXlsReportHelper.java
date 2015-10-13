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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.util.comparator.CommentComparator;
import com.globalsight.everest.util.comparator.IssueHistoryComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReportGeneratorHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.DiplomatBasicParserException;
import com.globalsight.ling.tw.PseudoConstants;
import com.globalsight.ling.tw.PseudoData;
import com.globalsight.ling.tw.TmxPseudo;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;
import com.globalsight.util.SortUtil;

public class CommentXlsReportHelper
{
    private static Logger s_logger = Logger
            .getLogger(CommentXlsReportHelper.class);

    private TargetPage targetPageVar = null;
    private boolean oneSheet = true;
    private boolean showPriority = false;
    private boolean showCategory = false;
    private boolean showStatus = false;
    private ArrayList statusList = new ArrayList();
    private ArrayList langList = new ArrayList();
    private boolean showAllLang = false;
    private GlobalSightLocale curLocale = null;
    private String externalPageId = null;
    private String pageUrl = null;
    private HashMap commentTargetPageMap = new HashMap();
    private Locale uiLocale = null;
    private ResourceBundle bundle = null;
    private String userId = null;
    private CellStyle contentStyle = null;
    private CellStyle headerStyle = null;
    private boolean showSourAndTar = false;

    private final int JOB_FLAG = 1;
    private final int TASK_FLAG = 2;
    private final int SEGMENT_FLAG = 3;
    private List<Long> m_jobIDS = null;
    
    /**
     * Generates the Excel report and spits it to the outputstream The report
     * consists of all in progress workflows that are currently at a reviewOnly
     * stage.
     * 
     * @return File
     * @exception Exception
     */
    public void generateReport(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        uiLocale = (Locale) p_request.getSession().getAttribute(
                WebAppConstants.UILOCALE);
        bundle = PageHandler.getBundle(p_request.getSession());
        userId = (String) p_request.getSession(false).getAttribute(
                WebAppConstants.USER_NAME);
        String companyId = (String) p_request.getSession().getAttribute(
                "current_company_id");
        CompanyThreadLocal.getInstance().setIdValue(companyId);
        // show Priority or not
        setShowPriority(p_request);
        s_logger.debug("showPriority:" + showPriority);
        // show Category or not
        setShowCategory(p_request);
        setStatus(p_request);
        setSourceAndTarget(p_request);
        
        Workbook p_workbook = new SXSSFWorkbook();     
        createSheets(p_request, p_workbook, p_response);
        
        // Cancelled the report, return nothing.
        if (isCancelled())
        {
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        
        ServletOutputStream out = p_response.getOutputStream();
        p_workbook.write(out);
        out.close();
        ((SXSSFWorkbook)p_workbook).dispose();
    }

    private void createSheets(HttpServletRequest p_request, Workbook p_workbook,
    		HttpServletResponse p_response) throws Exception
    {
    	Sheet segmentSheet = null;
        Sheet jobSheet = null;
        Sheet taskSheet = null;
    	
        HashMap<String, Sheet> sheetMap = new HashMap<String, Sheet>();
        String[] paramCommentTypeJob = p_request
                .getParameterValues("commentType_Job");
        String[] paramCommentTypeTask = p_request
                .getParameterValues("commentType_Activity");
        segmentSheet = p_workbook.createSheet(bundle.getString("segment_comments"));
        addTitle(p_workbook, segmentSheet, bundle.getString("segment_comments"));
        addHeader(p_workbook, segmentSheet);
        sheetMap.put("segmentSheet", segmentSheet);
        if ((paramCommentTypeJob != null)
                && ("Job".equals(paramCommentTypeJob[0]) == true))
        {
            if (oneSheet == true)
            {
                jobSheet = segmentSheet;
            }
            else
            {
            	jobSheet = p_workbook.createSheet(bundle.getString("job_comments"));
            	addTitle(p_workbook, jobSheet, bundle.getString("job_comments"));
                addHeader(p_workbook, jobSheet);
            }
            sheetMap.put("jobSheet", jobSheet);
        }
        if ((paramCommentTypeTask != null)
                && ("Activity".equals(paramCommentTypeTask[0]) == true))
        {
            if (oneSheet == true)
            {
                taskSheet = segmentSheet;
            }
            else
            {
            	taskSheet = p_workbook.createSheet(bundle.getString("activity_comments"));
            	addTitle(p_workbook, taskSheet, bundle.getString("activity_comments"));
                addHeader(p_workbook, taskSheet);
            }
            sheetMap.put("taskSheet", taskSheet);
        }

        addComments(p_workbook, p_request, sheetMap, p_response);
    }
    
    private void addTitle(Workbook p_workbook, Sheet p_sheet,
    		String sheetTitle)throws Exception
	{
    	// title font is black bold on white
    	Font titleFont = p_workbook.createFont();
        titleFont.setUnderline(Font.U_NONE);
        titleFont.setFontName("Times");
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        CellStyle titileCs = p_workbook.createCellStyle();
        titileCs.setWrapText(false);
        titileCs.setFont(titleFont);
        
        Row titleRow = getRow(p_sheet, 0);
        Cell titleCell = getCell(titleRow, 0);
        titleCell.setCellValue(sheetTitle);
        titleCell.setCellStyle(titileCs);
        p_sheet.setColumnWidth(0, 22 * 256);
	}
    
    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(Workbook p_workbook, Sheet p_sheet) throws Exception
    {     
        int col = 0;
        
        Row headerRow = getRow(p_sheet, 3);
        Cell cell_A = getCell(headerRow, col++);
        cell_A.setCellValue(bundle.getString("job_id"));
        cell_A.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 10 * 256);
        
        Cell cell_B = getCell(headerRow, col++);
        cell_B.setCellValue(bundle.getString("job_name"));
        cell_B.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_C = getCell(headerRow, col++);
        cell_C.setCellValue(bundle.getString("comment_type"));
        cell_C.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 15 * 256);
        
        Cell cell_D = getCell(headerRow, col++);
        cell_D.setCellValue(bundle.getString("language"));
        cell_D.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_E = getCell(headerRow, col++);
        cell_E.setCellValue(bundle.getString("segment_number"));
        cell_E.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 15 * 256);
        
        Cell cell_F = getCell(headerRow, col++);
        cell_F.setCellValue(bundle.getString("by_who"));
        cell_F.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 15 * 256);
        
        Cell cell_G = getCell(headerRow, col++);
        cell_G.setCellValue(bundle.getString("on_date"));
        cell_G.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 25 * 256);

        if (showStatus)
        {
        	Cell cell_Status = getCell(headerRow, col++);
        	cell_Status.setCellValue(bundle.getString("status"));
        	cell_Status.setCellStyle(getHeaderStyle(p_workbook));
        	p_sheet.setColumnWidth(col - 1, 10 * 256);
        }
        if (showPriority)
        {
        	Cell cell_Priority = getCell(headerRow, col++);
        	cell_Priority.setCellValue(bundle.getString("priority"));
        	cell_Priority.setCellStyle(getHeaderStyle(p_workbook));
        	p_sheet.setColumnWidth(col - 1, 10 * 256);
        }
        if (showCategory)
        {
        	Cell cell_Category = getCell(headerRow, col++);
        	cell_Category.setCellValue(bundle.getString("category"));
        	cell_Category.setCellStyle(getHeaderStyle(p_workbook));
        	p_sheet.setColumnWidth(col - 1, 40 * 256);
        }
        
        if (showSourAndTar)
        {
            Cell cell_Source = getCell(headerRow, col++);
            cell_Source.setCellValue(bundle.getString("source_segment"));
            cell_Source.setCellStyle(getHeaderStyle(p_workbook));
            p_sheet.setColumnWidth(col - 1, 40 * 256);

            Cell cell_Target = getCell(headerRow, col++);
            cell_Target.setCellValue(bundle.getString("target_segment"));
            cell_Target.setCellStyle(getHeaderStyle(p_workbook));
            p_sheet.setColumnWidth(col - 1, 40 * 256);
        }
        
        Cell cell_CommentHeader = getCell(headerRow, col++);
        cell_CommentHeader.setCellValue(bundle.getString("comment_header"));
        cell_CommentHeader.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 20 * 256);
        
        Cell cell_CommentBody = getCell(headerRow, col++);
        cell_CommentBody.setCellValue(bundle.getString("comment_body"));
        cell_CommentBody.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 25 * 256);
        
        Cell cell_Link = getCell(headerRow, col++);
        cell_Link.setCellValue(bundle.getString("link"));
        cell_Link.setCellStyle(getHeaderStyle(p_workbook));
        p_sheet.setColumnWidth(col - 1, 40 * 256);
    }
    
    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addComments(Workbook p_workbook, HttpServletRequest p_request,
            HashMap sheetMap, HttpServletResponse p_response) throws Exception
    {
        // print out the request parameters
        setLang(p_request);
        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("status:" + statusList);            
        }

        Sheet jobSheet = (Sheet) sheetMap.get("jobSheet");
        Sheet taskSheet = (Sheet) sheetMap.get("taskSheet");
        Sheet segmentSheet = (Sheet) sheetMap.get("segmentSheet");

        ArrayList<Job> jobs = new ArrayList<Job>();
        searchJob(jobs, p_request);
        m_jobIDS = ReportHelper.getJobIDS(jobs);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataInProgressStatus(userId, m_jobIDS, getReportType()))
        {
            String message = "Cancel the request, due the report is generating, userID/reportTypeList/reportJobIDS:"
                    + userId + ", " + "Comments Report" + ", " + m_jobIDS;
            s_logger.info(message);
            p_workbook = null;
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Set ReportsData.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                0, ReportsData.STATUS_INPROGRESS);

        if (s_logger.isDebugEnabled())
        {
            s_logger.debug("test");
            s_logger.debug("jobs " + jobs.size());
            s_logger.debug("jobSheet " + jobSheet);            
        }

        if (oneSheet == true)
        {
            IntHolder row = new IntHolder(4);
            IntHolder jobSheetRow = row;
            IntHolder taskSheetRow = row;
            IntHolder segmentSheetRow = row;
            for (Job j: jobs)
            {
                if (isCancelled())
                {
                    p_workbook = null;
                    return;
                }
                addSegmentComment(p_request, j, p_workbook, segmentSheet, segmentSheetRow);
            }
            for (Job j: jobs)
            {
                if (isCancelled())
                {
                    p_workbook = null;
                    return;
                }
                addTaskComment(p_request, j, p_workbook, taskSheet, taskSheetRow);
            }
            for (Job j: jobs)
            {
                if (isCancelled())
                {
                    p_workbook = null;
                    return;
                }
                addJobComment(p_request, j, p_workbook, jobSheet, jobSheetRow);
            }
        }
        else
        {
            IntHolder jobSheetRow = new IntHolder(4);
            IntHolder taskSheetRow = new IntHolder(4);
            IntHolder segmentSheetRow = new IntHolder(4);

            for (Job j: jobs)
            {
                if (isCancelled())
                {
                    p_workbook = null;
                    return;
                }
                addJobComment(p_request, j, p_workbook, jobSheet, jobSheetRow);
                addTaskComment(p_request, j, p_workbook, taskSheet, taskSheetRow);
                addSegmentComment(p_request, j, p_workbook, segmentSheet, segmentSheetRow);
            }
        }

        // Set ReportsData.
        ReportHelper.setReportsData(userId, m_jobIDS, getReportType(),
                100, ReportsData.STATUS_FINISHED);
    }

    /**
     * Gets Job comment.
     * 
     */
    private void addJobComment(HttpServletRequest p_request, Job j,
            Workbook p_workbook, Sheet jobSheet, IntHolder row) throws Exception
    {
        if (null == jobSheet)
        {
            return;
        }
        int count = 0;
        for (Workflow wf : j.getWorkflows())
        {
        	if (checkLang(wf) != true)
        	{
        		continue;
        	}
        	count++;
        }
        if(count > 0){
        	
        	List jobComments = j.getJobComments();
        	s_logger.debug("jobComments" + jobComments.size());
        	int flag = JOB_FLAG;
        	sortComment(jobComments, p_request);
        	addSegmentCommentFilter(p_request, j, p_workbook, jobSheet, row, jobComments, flag);
        }

    }

    /**
     * Gets Task comment.
     * 
     */
    private void addTaskComment(HttpServletRequest p_request, Job j,
            Workbook p_workbook, Sheet taskSheet, IntHolder row) throws Exception
    {
        if (null == taskSheet)
            return;

        Collection c = j.getWorkflows();
        ArrayList list = new ArrayList();
        for (Iterator it = c.iterator(); it.hasNext();)
        {
            Workflow wf = (Workflow) it.next();

            if (checkLang(wf) != true)
            {
                continue;
            }
            
            for (TargetPage t : wf.getTargetPages())
            {
                // used to SetPageURL;
                targetPageVar = t;
            }

            Hashtable tasks = wf.getTasks();
            for (Iterator i = tasks.values().iterator(); i.hasNext();)
            {
                Task t = (Task) i.next();
                List commentList = t.getTaskComments();
                list.addAll(commentList);
            }
        }
        int flag = TASK_FLAG;
        sortComment(list, p_request);
        addSegmentCommentFilter(p_request, j, p_workbook, taskSheet, row, list, flag);
        // only coment with target locale need to print target language
        curLocale = null;
        targetPageVar = null;
    }
    
    /**
     * Gets Segment comment.
     */
    private void addSegmentComment(HttpServletRequest p_request, Job j,
            Workbook p_workbook, Sheet segmentSheet, IntHolder row) throws Exception
    {
        if (null == segmentSheet)
        {
            return;
        }

        ArrayList<IssueImpl> comments = new ArrayList<IssueImpl>();
        for (Workflow wf : j.getWorkflows())
        {
            // target locale filter;
            if (checkLang(wf) == false)
            {
                continue;
            }

            for (TargetPage t : wf.getTargetPages())
            {
                // used to SetPageURL;
                targetPageVar = t;
                List<IssueImpl> commentList = ServerProxy.getCommentManager()
                        .getIssues(Issue.TYPE_SEGMENT, t.getId());

                setCommentTargetPage(commentList, t);
                comments.addAll(commentList);
            }

        }

        int flag = SEGMENT_FLAG;
        sortCommentBySegmentNumber(comments, p_request);
        addSegmentCommentFilter(p_request, j, p_workbook, segmentSheet, row, comments, flag);

        // only comment with target locale need to print target language
        curLocale = null;
        targetPageVar = null;
    }
    
    /**
     * Gets Segment comment helper.
     * 
     */
    private void addSegmentCommentFilter(HttpServletRequest p_request, Job j,
            Workbook p_workbook, Sheet segmentSheet, IntHolder row,
            List comments, int flag) throws Exception
    {
        // sortComment(comments, p_request);
        for (Iterator it = comments.iterator(); it.hasNext();)
        {
            Comment comment = (Comment) it.next();
            if (SEGMENT_FLAG == flag)
            {
                if (showStatus == true)
                {
                    if (statusList.contains(((Issue) comment).getStatus()) == false)
                    {
                        if (s_logger.isDebugEnabled())
                        {
                            s_logger.debug("ignore status:"
                                    + ((Issue) comment).getStatus());                            
                        }
                        continue;
                    }
                }
                setPageURL(p_request, comment, j.getId());

                // get the comment body of each segment comment
                List list = ((Issue) comment).getHistory();
                // Sort them by date
                sortCommentByIssueHistoryDate(list, p_request);
                for (Iterator iterator = list.iterator(); iterator.hasNext();)
                {
                    IssueHistory issueHistory = (IssueHistory) iterator.next();
                    addCommentHistory(p_request, j, p_workbook, segmentSheet, row, comment,
                            issueHistory, flag);
                }

            }
            else
            {
                addComment(p_request, j, p_workbook, segmentSheet, row, comment, flag);
            }
        }
    }
    
    /**
     * add Comment for each row of each sheet
     * 
     * @exception Exception
     */
    private void addComment(HttpServletRequest p_request, Job j,
            Workbook p_workbook, Sheet p_sheet, IntHolder row, Comment comment,
            int flag) throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                p_request.getParameter("dateFormat"));

        String jobPrefix = "Job Comment ";
        String taskPrefix = "Activity Comment ";
        int c = 0;
        int r = row.getValue();
        
        Row p_row = getRow(p_sheet, r);
        // 2.3 Job ID column. Insert GlobalSight job number here.
        Cell cell_A = getCell(p_row, c++);
        cell_A.setCellValue(j.getJobId());
        cell_A.setCellStyle(getContentStyle(p_workbook));

        // 2.4 Job: Insert Job name here.
        Cell cell_B = getCell(p_row, c++);
        cell_B.setCellValue(j.getJobName());
        cell_B.setCellStyle(getContentStyle(p_workbook));
        
        //Insert comment type
        Cell cell_C = getCell(p_row, c++);
        if (JOB_FLAG == flag)
        {
            cell_C.setCellValue(jobPrefix);
            cell_C.setCellStyle(getContentStyle(p_workbook));
        }
        else if (TASK_FLAG == flag)
        {
            cell_C.setCellValue(taskPrefix);
            cell_C.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.5 Lang: Insert each target language identifier for each workflow
        // in the retrieved Job on a different row.
        Cell cell_D = getCell(p_row, c++);
        if ((this.targetPageVar != null)
                && (this.targetPageVar.getGlobalSightLocale() != null))
        {
            cell_D.setCellValue(this.targetPageVar
                    .getGlobalSightLocale().getDisplayName(uiLocale));
            cell_D.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
            cell_D.setCellValue("");
            cell_D.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.6 Word count: Insert Segement Number for the job.
        Cell cell_E = getCell(p_row, c++);
        cell_E.setCellValue("");
        cell_E.setCellStyle(getContentStyle(p_workbook));

        // by who
        Cell cell_F = getCell(p_row, c++);
    	cell_F.setCellValue(UserUtil.getUserNameById(comment
                .getCreatorId()));
    	cell_F.setCellStyle(getContentStyle(p_workbook));

        // 2.7 Comment create date: Insert Comment creation date.
    	Cell cell_G = getCell(p_row, c++);
    	cell_G.setCellValue(dateFormat.format(comment
                .getCreatedDateAsDate()));
    	cell_G.setCellStyle(getContentStyle(p_workbook));

        // 2.8 add Comment Status
        if (showStatus)
        {
        	Cell cell_Status = getCell(p_row, c++);
        	cell_Status.setCellValue("");
        	cell_Status.setCellStyle(getContentStyle(p_workbook));
        }
        // 2.9 add Comment priority
        if (showPriority)
        {
        	Cell cell_Priority = getCell(p_row, c++);
        	cell_Priority.setCellValue("");
        	cell_Priority.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.10 add Comment Category
        if (showCategory)
        {
        	Cell cell_Category = getCell(p_row, c++);
        	cell_Category.setCellValue("");
        	cell_Category.setCellStyle(getContentStyle(p_workbook));
        }
        //add source and target segment
        if (showSourAndTar)
        {
            Cell cell_Source = getCell(p_row, c++);
            cell_Source.setCellValue("");
            cell_Source.setCellStyle(getContentStyle(p_workbook));
            
            Cell cell_Target = getCell(p_row, c++);
            cell_Target.setCellValue("");
            cell_Target.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.11 add Comment comment
        Cell cell_CommentHeader = getCell(p_row, c++);
        cell_CommentHeader.setCellValue("");
        cell_CommentHeader.setCellStyle(getContentStyle(p_workbook));

        Cell cell_CommentBody = getCell(p_row, c++);
        cell_CommentBody.setCellValue(comment.getComment());
        cell_CommentBody.setCellStyle(getContentStyle(p_workbook));
        
    	Cell cell_Link = getCell(p_row, c++);
    	if (flag != SEGMENT_FLAG)
        {
            cell_Link.setCellValue("");
        }else{
            cell_Link.setCellFormula("HYPERLINK(\"" + pageUrl
                    + "\",\"" + externalPageId + "\")");
        }
    	cell_Link.setCellStyle(getContentStyle(p_workbook));

        row.inc();

    }

    /**
     * add Comment for each row, for segment comment usage, each row contains a
     * comment history
     * 
     * @exception Exception
     */
    private void addCommentHistory(HttpServletRequest p_request, Job j,	
            Workbook p_workbook, Sheet p_sheet, IntHolder row, Comment comment,
            IssueHistory issueHistory, int flag) throws Exception
    {
        String segmentPrefix = "Segment Comment";
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                p_request.getParameter("dateFormat"));
        int c = 0;
        int r = row.getValue();
        // 2.3.2 Job ID column. Insert GlobalSight job number here.
        Row p_row = getRow(p_sheet, r);
        Cell cell_A = getCell(p_row, c++);
        cell_A.setCellValue(j.getJobId());
        cell_A.setCellStyle(getContentStyle(p_workbook));

        // 2.4.2 Job: Insert Job name here.
        Cell cell_B = getCell(p_row, c++);
        cell_B.setCellValue( j.getJobName());
        cell_B.setCellStyle(getContentStyle(p_workbook));
        
        Cell cell_C = getCell(p_row, c++);
        cell_C.setCellValue(segmentPrefix);
        cell_C.setCellStyle(getContentStyle(p_workbook));

        // 2.5.2 Lang: Insert each target language identifier for each workflow
        // in the retrieved Job on a different row.
        Cell cell_D = getCell(p_row, c++);
        if ((this.targetPageVar != null)
                && (this.targetPageVar.getGlobalSightLocale() != null))
        {
            cell_D.setCellValue(this.targetPageVar
                    .getGlobalSightLocale().getDisplayName(uiLocale));
            cell_D.setCellStyle(getContentStyle(p_workbook));
        }
        else
        {
            cell_D.setCellValue("");
            cell_D.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.6.2 Insert Segement Number for the job.
        Cell cell_E = getCell(p_row, c++);
        Integer segmentNum = Integer.valueOf(CommentComparator
                .getSegmentIdFromLogicalKey(((Issue) comment).getLogicalKey()));
        cell_E.setCellValue(segmentNum);
        cell_E.setCellStyle(getContentStyle(p_workbook));

        // by who
        Cell cell_F = getCell(p_row, c++);
        cell_F.setCellValue(UserUtil.getUserNameById(issueHistory.reportedBy()));
        cell_F.setCellStyle(getContentStyle(p_workbook));

        // 2.7.2 Comment create date: Insert Comment creation date.\
        Cell cell_G = getCell(p_row, c++);
        cell_G.setCellValue(dateFormat.format(issueHistory.dateReportedAsDate()));
        cell_G.setCellStyle(getContentStyle(p_workbook));

        // 2.8.2 add Comment Status
        if (showStatus)
        {
        	Cell cell_Status = getCell(p_row, c++);
        	cell_Status.setCellValue(((Issue) comment).getStatus());
        	cell_Status.setCellStyle(getContentStyle(p_workbook));
        }
        // 2.9.2 add Comment priority
        if (showPriority)
        {
        	Cell cell_Priority = getCell(p_row, c++);
        	cell_Priority.setCellValue(((Issue) comment).getPriority());
        	cell_Priority.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.10.2 add Comment Category
        if (showCategory)
        {
        	Cell cell_Category = getCell(p_row, c++);
        	cell_Category.setCellValue(((Issue) comment).getCategory());
        	cell_Category.setCellStyle(getContentStyle(p_workbook));
        }
        
        if (showSourAndTar)
        {
            long targetLocalId = this.targetPageVar.getGlobalSightLocale()
                    .getId();
            long sourceLocalId = j.getSourceLocale().getId();
            Tuv sourceTuv = SegmentTuvUtil.getTuvByTuIdLocaleId(segmentNum, sourceLocalId,
                    j.getId());
            
            Tuv targetTuv = SegmentTuvUtil.getTuvByTuIdLocaleId(segmentNum,
                    targetLocalId, j.getId());
            PseudoData pData = new PseudoData();
            pData.setMode(PseudoConstants.PSEUDO_COMPACT);
            
            Cell cell_SourceSegment = getCell(p_row, c++);
            cell_SourceSegment.setCellValue(getSegment(pData, sourceTuv, j.getId()));
            cell_SourceSegment.setCellStyle(getContentStyle(p_workbook));
            
            Cell cell_TargetSegment = getCell(p_row, c++);
            cell_TargetSegment.setCellValue(getSegment(pData, targetTuv, j.getId()));
            cell_TargetSegment.setCellStyle(getContentStyle(p_workbook));
        }

        // 2.11.2 add comment header
        Cell cell_CommentHeader = getCell(p_row, c++);
        cell_CommentHeader.setCellValue(comment.getComment());
        cell_CommentHeader.setCellStyle(getContentStyle(p_workbook));

        // 2.12.2 add comment body
        Cell cell_CommentBody = getCell(p_row, c++);
        cell_CommentBody.setCellValue(issueHistory.getComment());
        cell_CommentBody.setCellStyle(getContentStyle(p_workbook));

        // 2.13.2 add link
        Cell cell_Link = getCell(p_row, c++);
        cell_Link.setCellFormula("HYPERLINK(\"" + pageUrl + "\",\""
                + externalPageId + "\")");
        cell_Link.setCellStyle(getContentStyle(p_workbook));

        row.inc();
    }

    private String getSegment(PseudoData pData, Tuv tuv, long p_jobId)
    {
        StringBuffer content = new StringBuffer();
        String dataType = null;
        try
        {
            dataType = tuv.getDataType(p_jobId);
            pData.setAddables(dataType);
            TmxPseudo.tmx2Pseudo(tuv.getGxmlExcludeTopTags(), pData);
            content.append(pData.getPTagSourceString());
        }
        catch (DiplomatBasicParserException e)
        {
            s_logger.error(tuv.getId(), e);
        }
        String result = content.toString();
        return result;
    }

    /**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
            throws Exception
    {
    	SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yyyy");
        String[] paramProjectIds = p_request.getParameterValues("projectId");
        String[] paramStatus = p_request.getParameterValues("status");

        paramProjectIds = (paramProjectIds == null) ? new String[]
        { "*" } : paramProjectIds;

        paramStatus = (paramStatus == null) ? new String[]
        { "*" } : paramStatus;

        JobSearchParameters sp = new JobSearchParameters();

        ArrayList stateList = new ArrayList();
        if ((paramStatus == null) || ("*".equals(paramStatus[0])))
        {
            // just do a query for all in progress jobs, localized, and exported
            stateList.add(Job.DISPATCHED);
            stateList.add(Job.LOCALIZED);
            stateList.add(Job.EXPORTED);
        }
        else
        {
            for (int i = 0; i < paramStatus.length; i++)
            {
                stateList.add(paramStatus[i]);
            }
        }
        sp.setJobState(stateList);

        // Get project ids
        ArrayList projectIdList = new ArrayList();
		if (paramProjectIds != null && !paramProjectIds[0].equals("*")) 
		{
			for (int i = 0; i < paramProjectIds.length; i++) {
				projectIdList.add(new Long(paramProjectIds[i]));
			}
		} else 
		{
			try 
			{
				List<Project> projectList = (ArrayList<Project>) ServerProxy
						.getProjectHandler().getProjectsByUser(userId);
				for (Project project : projectList) {
					projectIdList.add(project.getIdAsLong());
				}
			} 
			catch (Exception e) 
			{
			}
		}
        sp.setProjectId(projectIdList);

        // Get creation start
        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        if (paramCreateDateStartCount != null && paramCreateDateStartCount != "")
        {
            sp.setCreationStart(simpleDate.parse(paramCreateDateStartCount));
        }

        // Get creation end
        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        if (paramCreateDateEndCount != null && paramCreateDateEndCount != "")
        {
        	Date date = simpleDate.parse(paramCreateDateEndCount);
        	long endLong = date.getTime()+(24*60*60*1000-1);
            sp.setCreationEnd(new Date(endLong));
        }

        return sp;
    }

    /**
     * Sort the jobs .
     * 
     */
    private void sortJob(ArrayList p_jobs, HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute("uiLocale");
        if (uiLocale == null)
            uiLocale = Locale.ENGLISH;
        int JOB_ID = 1;
        JobComparator comparator = new JobComparator(JOB_ID, uiLocale, true,
                null);
        comparator.setSortColumn(JobComparator.JOB_ID);
        SortUtil.sort(p_jobs, comparator);
    }

    /**
     * Gets the jobs .
     * 
     */
    private void searchJob(ArrayList jobs, HttpServletRequest p_request)
            throws Exception
    {
        String[] paramJobId = p_request.getParameterValues("jobId");
        paramJobId = (paramJobId == null) ? new String[]
        { "*" } : paramJobId;

        if ((paramJobId != null) && "*".equals(paramJobId[0]))
        {
            // do a search based on the params
            JobSearchParameters searchParams = null;
            try
            {
                searchParams = getSearchParams(p_request);
            }
            catch (NumberFormatException e)
            {
                s_logger.warn("NumberFormatException:"
                        + "--please input legal number in data area");
            }
            jobs.addAll(ServerProxy.getJobHandler().getJobs(searchParams));
        }
        else
        {
            // just get the specific jobs they chose
            for (int i = 0; i < paramJobId.length; i++)
            {
                if ("*".equals(paramJobId[i]) == false)
                {
                    long jobId = Long.parseLong(paramJobId[i]);
                    Job j = ServerProxy.getJobHandler().getJobById(jobId);
                    jobs.add(j);
                }
            }
        }

        sortJob(jobs, p_request);
    }

    /**
     * set showPriority value.
     * 
     */
    private void setShowPriority(HttpServletRequest p_request)
    {
        String[] paramCommentPriority = p_request
                .getParameterValues("commentPriority_On");

        if ((null != paramCommentPriority)
                && ("on".equals(paramCommentPriority[0])))
        {
            showPriority = true;
        }
        else
        {
            showPriority = false;
        }
    }

    /**
     * Sets showCategory value.
     * 
     */
    private void setShowCategory(HttpServletRequest p_request)
    {
        String[] paramCommentCategory = p_request
                .getParameterValues("commentCategory_On");
        if ((null != paramCommentCategory)
                && ("on".equals(paramCommentCategory[0])))
        {
            showCategory = true;
        }
        else
        {
            showCategory = false;
        }
    }

    /**
     * set Status.
     * 
     */
    private void setStatus(HttpServletRequest p_request)
    {
        statusList = new ArrayList();
        List allList = IssueOptions.getAllStatus();
        String[] paramCommentStatusOpen = p_request
                .getParameterValues("commenStatus_open");
        String[] paramCommentStatusQuery = p_request
                .getParameterValues("commenStatus_query");
        String[] paramCommentStatusClosed = p_request
                .getParameterValues("commenStatus_closed");
        String[] paramCommentStatusRejected = p_request
                .getParameterValues("commenStatus_rejected");

        if ((paramCommentStatusOpen != null)
                && allList.contains(paramCommentStatusOpen[0]))
        {
            statusList.add(paramCommentStatusOpen[0]);
        }

        if ((paramCommentStatusQuery != null)
                && allList.contains(paramCommentStatusQuery[0]))
        {
            statusList.add(paramCommentStatusQuery[0]);
        }

        if ((paramCommentStatusClosed != null)
                && allList.contains(paramCommentStatusClosed[0]))
        {
            statusList.add(paramCommentStatusClosed[0]);
        }

        if ((paramCommentStatusRejected != null)
                && allList.contains(paramCommentStatusRejected[0]))
        {
            statusList.add(paramCommentStatusRejected[0]);
        }

        if (statusList.size() == 0)
        {
            showStatus = false;
        }
        else
        {
            showStatus = true;
        }

    }
    /**
     * set source and target
     */
    private void setSourceAndTarget(HttpServletRequest p_request)
    {
        String[] paramSourAndTar = p_request
                .getParameterValues("show_SourceAndTarget");
        if ((null != paramSourAndTar)
                && ("on".equals(paramSourAndTar[0])))
        {
            showSourAndTar = true;
        }
        else
        {
            showSourAndTar = false;
        }  
    }

    /**
     * set Targe lang list.
     * 
     */
    private void setLang(HttpServletRequest p_request) {
        langList = new ArrayList();
        String[] paramLang = p_request.getParameterValues("targetLang");

        if (paramLang != null && !paramLang[0].equals("*"))
        {
            for (int i = 0; i < paramLang.length; i++)
            {
                langList.add(paramLang[i]);
                s_logger.warn("Lang:" + paramLang[i]);
            }
            showAllLang = false;
        }
        else
        {
            showAllLang = true;
        }

        s_logger.warn("all Lange:" + showAllLang);
    }

    /**
     * Check taget locale.
     * 
     */
    private boolean checkLang(Workflow w)
    {
        curLocale = w.getTargetLocale();

        if (showAllLang)
        {
            return true;
        }

        return langList.contains(Long.toString(curLocale.getId()));
    }

    /**
     * Set page name and URL .
     * 
     */
    private void setPageURL(HttpServletRequest p_request, Comment comment,
            long jobId)
    {
        String url = p_request.getHeader("Referer");
        s_logger.debug("url--:" + url);
        url = url.substring(0, (url.indexOf("?") + 1));
        targetPageVar = (TargetPage) commentTargetPageMap.remove(new Long(
                comment.getId()));

        String key = ((Issue) comment).getLogicalKey();
        String[] ids = key.split("_");

        String editorReviewUrl = url
                + "linkName=editor&pageName=SEGCOMMENTS&reviewMode=true";
        url = "&" + WebAppConstants.SOURCE_PAGE_ID + "="
                + targetPageVar.getSourcePage().getId() + "&"
                + WebAppConstants.TARGET_PAGE_ID + "=" + targetPageVar.getId()
                + "&" + WebAppConstants.JOB_ID + "=" + jobId + "&curTuId="
                + ids[1] + "&curTuvId=" + ids[2] + "&curSubId=" + ids[3];
        pageUrl = editorReviewUrl + url;
        externalPageId = targetPageVar.getExternalPageId();
        s_logger.debug("externalPageId: " + externalPageId);
    }

    /**
     * Sort Comment here.
     * 
     */
    private void sortComment(List list, HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute("uiLocale");
        if (uiLocale == null)
            uiLocale = Locale.ENGLISH;
        CommentComparator commentComparator = new CommentComparator(uiLocale);
        commentComparator.setType(CommentComparator.COMMENT_ID);
        SortUtil.sort(list, commentComparator);
    }

    /**
     * Sort Segment Comment by Segment Number
     * 
     */
    private void sortCommentBySegmentNumber(List list,
            HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute("uiLocale");
        if (uiLocale == null)
            uiLocale = Locale.ENGLISH;
        CommentComparator commentComparator = new CommentComparator(uiLocale);
        commentComparator.setType(CommentComparator.SEGMENT_NUMBER);
        SortUtil.sort(list, commentComparator);
    }

    /**
     * Sort Segment Issue History by Date
     * 
     */
    private void sortCommentByIssueHistoryDate(List list,
            HttpServletRequest p_request)
    {
        HttpSession session = p_request.getSession(false);
        Locale uiLocale = (Locale) session.getAttribute("uiLocale");
        if (uiLocale == null)
            uiLocale = Locale.ENGLISH;
        IssueHistoryComparator issueHistoryComparator = new IssueHistoryComparator(
                uiLocale);
        issueHistoryComparator.setType(IssueHistoryComparator.DATE);
        SortUtil.sort(list, issueHistoryComparator);
    }

    /**
     * Set TargetLocale for each comment, used in get URL
     * 
     */
    private void setCommentTargetPage(List ls, TargetPage t)
    {
        for (int i = 0; i < ls.size(); i++)
        {
            Comment c = (Comment) ls.get(i);
            commentTargetPageMap.put(new Long(c.getId()), t);
        }
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
            CellStyle style = p_workbook.createCellStyle();
            style.setWrapText(true);
            style.setAlignment(CellStyle.ALIGN_LEFT);
            style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            Font font = p_workbook.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            style.setFont(font);

            contentStyle = style;
        }

        return contentStyle;
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
    
    public String getReportType()
    {
        return ReportConstants.COMMENTS_REPORT;
    }
    
    public boolean isCancelled()
    {
        ReportsData data = ReportGeneratorHandler.getReportsMap(userId,
                m_jobIDS, getReportType());
        if (data != null)
            return data.isCancle();

        return false;
    }
}