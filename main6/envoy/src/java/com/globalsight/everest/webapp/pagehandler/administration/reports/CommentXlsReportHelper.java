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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobSearchParameters;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.CommentComparator;
import com.globalsight.everest.util.comparator.IssueHistoryComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobComparator;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.JobSearchConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.IntHolder;

public class CommentXlsReportHelper
{
    private static Logger s_logger = Logger
            .getLogger(CommentXlsReportHelper.class);

    private WritableWorkbook m_workbook = null;
    private TargetPage targetPageVar = null;
    private boolean oneSheet = true;
    private boolean showPriority = false;
    private boolean showCategory = false;
    private boolean showStatus = true;
    private ArrayList statusList = new ArrayList();
    private ArrayList langList = new ArrayList();
    private boolean showAllLang = false;
    private GlobalSightLocale curLocale = null;
    private String externalPageId = null;
    private String pageUrl = null;
    private HashMap commentTargetPageMap = new HashMap();
    private Locale uiLocale = null;

    private final int JOB_FLAG = 1;
    private final int TASK_FLAG = 2;
    private final int SEGMENT_FLAG = 3;

    private static Map<String, ReportsData> m_reportsDataMap = 
            new ConcurrentHashMap<String, ReportsData>();
    
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
        String companyId = (String) p_request.getSession().getAttribute(
                "current_company_id");
        CompanyThreadLocal.getInstance().setIdValue(companyId);

        WorkbookSettings settings = new WorkbookSettings();
        settings.setSuppressWarnings(true);

        m_workbook = Workbook.createWorkbook(p_response.getOutputStream(),
                settings);

        addComments(p_request, p_response);
        
        if (m_workbook != null)
        {
            m_workbook.write();
            m_workbook.close();
        }
    }

    /**
     * Returns search params used to find the in progress jobs for all PMs
     * 
     * @return JobSearchParams
     */
    private JobSearchParameters getSearchParams(HttpServletRequest p_request)
            throws Exception
    {
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
            for (int i = 0; i < paramProjectIds.length; i++)
            {
                projectIdList.add(new Long(paramProjectIds[i]));
            }
            sp.setProjectId(projectIdList);
        }

        // Get creation start
        String paramCreateDateStartCount = p_request
                .getParameter(JobSearchConstants.CREATION_START);
        String paramCreateDateStartOpts = p_request
                .getParameter(JobSearchConstants.CREATION_START_OPTIONS);

        if ((paramCreateDateStartCount == null)
                || ("".equals(paramCreateDateStartCount)))
        {
            paramCreateDateStartOpts = "-1";
        }
        if ("-1".equals(paramCreateDateStartOpts) == false)
        {
            sp.setCreationStart(new Integer(paramCreateDateStartCount));
            sp.setCreationStartCondition(paramCreateDateStartOpts);
        }

        // Get creation end
        String paramCreateDateEndCount = p_request
                .getParameter(JobSearchConstants.CREATION_END);
        String paramCreateDateEndOpts = p_request
                .getParameter(JobSearchConstants.CREATION_END_OPTIONS);

        if ((paramCreateDateEndCount == null)
                || ("".equals(paramCreateDateStartCount)))
        {
            paramCreateDateEndOpts = "-1";
        }

        if (SearchCriteriaParameters.NOW.equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new java.util.Date());
        }
        else if (!"-1".equals(paramCreateDateEndOpts))
        {
            sp.setCreationEnd(new Integer(paramCreateDateEndCount));
            sp.setCreationEndCondition(paramCreateDateEndOpts);
        }

        return sp;
    }

    /**
     * Adds the table header for the job Comment sheet
     * 
     * @param p_sheet
     */
    private void addJobCommentHeader(WritableSheet p_sheet,
            ResourceBundle bundle) throws Exception
    {
        addHeader(p_sheet, bundle.getString("job_comments"), bundle);
    }

    /**
     * Adds the table header for the task Comment sheet
     * 
     * @param p_sheet
     */
    private void addTaskCommentHeader(WritableSheet p_sheet,
            ResourceBundle bundle) throws Exception
    {
        addHeader(p_sheet, bundle.getString("activity_comments"), bundle);
    }

    /**
     * Adds the table header for the segemnt Comment sheet
     * 
     * @param p_sheet
     */
    private void addSegmentCommentHeader(WritableSheet p_sheet,
            ResourceBundle bundle) throws Exception
    {
        addHeader(p_sheet, bundle.getString("segment_comments"), bundle);
    }

    /**
     * Adds the table header to the sheet
     * 
     * @param p_sheet
     */
    private void addHeader(WritableSheet p_sheet, String sheetTitle,
            ResourceBundle bundle) throws Exception
    {
        // title font is black bold on white
        WritableFont titleFont = new WritableFont(WritableFont.TIMES, 14,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat titleFormat = new WritableCellFormat(titleFont);
        titleFormat.setWrap(false);
        titleFormat.setShrinkToFit(false);
        p_sheet.addCell(new Label(0, 0, sheetTitle, titleFormat));
        p_sheet.setColumnView(0, 22);

        // headerFont is black bold on light grey
        WritableFont headerFont = new WritableFont(WritableFont.TIMES, 11,
                WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE,
                jxl.format.Colour.BLACK);
        WritableCellFormat headerFormat = new WritableCellFormat(headerFont);
        headerFormat.setWrap(true);
        headerFormat.setBackground(jxl.format.Colour.GRAY_25);
        headerFormat.setShrinkToFit(false);
        headerFormat.setBorder(jxl.format.Border.ALL,
                jxl.format.BorderLineStyle.THICK, jxl.format.Colour.BLACK);
        int col = 0;
        int row = 3;
        p_sheet.addCell(new Label(col++, row, bundle.getString("job_id"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 10);
        p_sheet.addCell(new Label(col++, row, bundle.getString("job_name"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 10);
        p_sheet.addCell(new Label(col++, row, bundle.getString("language"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 10);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("segment_number"), headerFormat));
        p_sheet.setColumnView(col - 1, 20);
        p_sheet.addCell(new Label(col++, row, bundle.getString("by_who"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 15);
        p_sheet.addCell(new Label(col++, row, bundle.getString("on_date"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 25);

        if (showStatus == true)
        {
            p_sheet.addCell(new Label(col++, row, bundle.getString("status"),
                    headerFormat));
        }
        p_sheet.setColumnView(col - 1, 10);
        if (true == showPriority)
        {
            p_sheet.addCell(new Label(col++, row, bundle.getString("priority"),
                    headerFormat));
        }
        if (showCategory)
        {
            p_sheet.addCell(new Label(col++, row, bundle.getString("category"),
                    headerFormat));
        }
        p_sheet.setColumnView(col - 1, 10);
        p_sheet.addCell(new Label(col++, row, bundle
                .getString("comment_header"), headerFormat));
        p_sheet.setColumnView(col - 1, 20);
        p_sheet.addCell(new Label(col++, row, bundle.getString("comment_body"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 25);
        p_sheet.addCell(new Label(col++, row, bundle.getString("link"),
                headerFormat));
        p_sheet.setColumnView(col - 1, 20);
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
        Collections.sort(p_jobs, comparator);
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
     * set Targe lang list.
     * 
     */
    private void setLang(HttpServletRequest p_request)
    {
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

        return langList.contains(curLocale.toString());
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
     * Add sheet header.
     * 
     */
    private HashMap addAllHeader(HttpServletRequest p_request,
            WritableSheet jobSheet, WritableSheet taskSheet,
            WritableSheet segmentSheet) throws Exception
    {
        HashMap sheetMap = new HashMap();
        String[] paramCommentTypeJob = p_request
                .getParameterValues("commentType_Job");
        String[] paramCommentTypeTask = p_request
                .getParameterValues("commentType_Activity");
        ResourceBundle bundle = PageHandler.getBundle(p_request.getSession());
        segmentSheet = m_workbook.createSheet(
                bundle.getString("segment_comments"), 0);
        addSegmentCommentHeader(segmentSheet, bundle);
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
                jobSheet = m_workbook.createSheet(
                        bundle.getString("job_comments"), 1);
                addJobCommentHeader(jobSheet, bundle);
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
                taskSheet = m_workbook.createSheet(
                        bundle.getString("activity_comments"), 2);
                addTaskCommentHeader(taskSheet, bundle);
            }
            sheetMap.put("taskSheet", taskSheet);
        }

        return sheetMap;
    }

    /**
     * Gets the jobs and outputs workflow information.
     * 
     * @exception Exception
     */
    private void addComments(HttpServletRequest p_request,
            HttpServletResponse p_response) throws Exception
    {
        String userId = (String) p_request.getSession().getAttribute(
                WebAppConstants.USER_NAME);
        // print out the request parameters
        // show Priority or not
        setShowPriority(p_request);
        s_logger.debug("showPriority:" + showPriority);
        // show Category or not
        setShowCategory(p_request);
        setStatus(p_request);
        setLang(p_request);

        s_logger.debug("status:" + statusList);

        WritableSheet jobSheet = null;
        WritableSheet taskSheet = null;
        WritableSheet segmentSheet = null;

        HashMap sheetMap = addAllHeader(p_request, jobSheet, taskSheet,
                segmentSheet);
        jobSheet = (WritableSheet) sheetMap.get("jobSheet");
        taskSheet = (WritableSheet) sheetMap.get("taskSheet");
        segmentSheet = (WritableSheet) sheetMap.get("segmentSheet");

        ArrayList jobs = new ArrayList();
        searchJob(jobs, p_request);
        List<Long> reportJobIDS = ReportHelper.getJobIDS(jobs);
        // Cancel Duplicate Request
        if (ReportHelper.checkReportsDataMap(m_reportsDataMap, userId,
                reportJobIDS, null))
        {
            String message = "Cancel the request, due the report is generating, userID/reportTypeList/reportJobIDS:"
                    + userId + ", " + "Comments Report" + ", " + reportJobIDS;
            s_logger.debug(message);
            m_workbook = null;
            p_response.sendError(p_response.SC_NO_CONTENT);
            return;
        }
        // Set m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                null, 0, ReportsData.STATUS_INPROGRESS);
        s_logger.debug("test");
        s_logger.debug("jobs " + jobs.size());
        s_logger.debug("jobSheet " + jobSheet);
        Iterator jobIter = jobs.iterator();
        if (oneSheet == true)
        {
            IntHolder row = new IntHolder(4);
            IntHolder jobSheetRow = row;
            IntHolder taskSheetRow = row;
            IntHolder segmentSheetRow = row;
            while (jobIter.hasNext())
            {
                Job j = (Job) jobIter.next();
                addSegmentComment(p_request, j, segmentSheet, segmentSheetRow);
            }
            jobIter = jobs.iterator();
            while (jobIter.hasNext())
            {
                Job j = (Job) jobIter.next();
                addTaskComment(p_request, j, taskSheet, taskSheetRow);
            }
            jobIter = jobs.iterator();
            while (jobIter.hasNext())
            {
                Job j = (Job) jobIter.next();
                addJobComment(p_request, j, jobSheet, jobSheetRow);
            }
        }
        else
        {
            IntHolder jobSheetRow = new IntHolder(4);
            IntHolder taskSheetRow = new IntHolder(4);
            IntHolder segmentSheetRow = new IntHolder(4);

            while (jobIter.hasNext())
            {
                Job j = (Job) jobIter.next();
                addJobComment(p_request, j, jobSheet, jobSheetRow);
                addTaskComment(p_request, j, taskSheet, taskSheetRow);
                addSegmentComment(p_request, j, segmentSheet, segmentSheetRow);
            }
        }
        
        // Set m_reportsDataMap.
        ReportHelper.setReportsDataMap(m_reportsDataMap, userId, reportJobIDS,
                null, 100, ReportsData.STATUS_FINISHED);
    }

    /**
     * Gets Job comment.
     * 
     */
    private void addJobComment(HttpServletRequest p_request, Job j,
            WritableSheet jobSheet, IntHolder row) throws Exception
    {
        if (null == jobSheet)
        {
            return;
        }

        List jobComments = j.getJobComments();
        s_logger.debug("jobComments" + jobComments.size());
        int flag = JOB_FLAG;
        sortComment(jobComments, p_request);
        addSegmentCommentFilter(p_request, j, jobSheet, row, jobComments, flag);

    }

    /**
     * Gets Task comment.
     * 
     */
    private void addTaskComment(HttpServletRequest p_request, Job j,
            WritableSheet taskSheet, IntHolder row) throws Exception
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
        addSegmentCommentFilter(p_request, j, taskSheet, row, list, flag);
        // only coment with target locale need to print target language
        curLocale = null;
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
        Collections.sort(list, commentComparator);
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
        Collections.sort(list, commentComparator);
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
        Collections.sort(list, issueHistoryComparator);
    }

    /**
     * Gets Segment comment helper.
     * 
     */
    private void addSegmentCommentFilter(HttpServletRequest p_request, Job j,
            WritableSheet segmentSheet, IntHolder row, List comments, int flag)
            throws Exception
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
                        s_logger.debug("ignore status:"
                                + ((Issue) comment).getStatus());
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
                    addCommentHistory(p_request, j, segmentSheet, row, comment,
                            issueHistory, flag);
                }

            }
            else
            {
                addComment(p_request, j, segmentSheet, row, comment, flag);
            }
        }
    }

    /**
     * Gets Segment comment.
     */
    private void addSegmentComment(HttpServletRequest p_request, Job j,
            WritableSheet segmentSheet, IntHolder row) throws Exception
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
        addSegmentCommentFilter(p_request, j, segmentSheet, row, comments, flag);

        // only comment with target locale need to print target language
        curLocale = null;
        targetPageVar = null;
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

    /**
     * add Comment for each row of each sheet
     * 
     * @exception Exception
     */
    private void addComment(HttpServletRequest p_request, Job j,
            WritableSheet sheet, IntHolder row, Comment comment, int flag)
            throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                p_request.getParameter("dateFormat"));

        HttpSession session = p_request.getSession();
        String jobPrefix = "Job Comment ";
        String taskPrefix = "Activity Comment ";
        int c = 0;
        int r = row.getValue();
        // 2.3 Job ID column. Insert GlobalSight job number here.
        sheet.addCell(new Label(c++, r, Long.toString(j.getJobId())));

        // 2.4 Job: Insert Job name here.
        sheet.addCell(new Label(c++, r, j.getJobName()));

        // 2.5 Lang: Insert each target language identifier for each workflow
        // in the retrieved Job on a different row.
        if ((this.targetPageVar != null)
                && (this.targetPageVar.getGlobalSightLocale() != null))
        {
            sheet.addCell(new Label(c++, r, this.targetPageVar
                    .getGlobalSightLocale().getDisplayName(uiLocale)));
        }
        else
        {
            sheet.addCell(new Label(c++, r, ""));
        }

        // 2.6 Word count: Insert Segement Number for the job.
        if (JOB_FLAG == flag)
        {
            sheet.addCell(new Label(c++, r, jobPrefix + comment.getId()));
        }
        else if (TASK_FLAG == flag)
        {
            sheet.addCell(new Label(c++, r, taskPrefix + comment.getId()));
        }
        else if (SEGMENT_FLAG == flag)
        {
            sheet.addCell(new Label(c++, r, CommentComparator
                    .getSegmentIdFromLogicalKey(((Issue) comment)
                            .getLogicalKey())));
        }

        // by who
        sheet.addCell(new Label(c++, r, UserUtil.getUserNameById(comment
                .getCreatorId())));

        // 2.7 Comment create date: Insert Comment creation date.
        sheet.addCell(new Label(c++, r, dateFormat.format(comment
                .getCreatedDateAsDate())));

        // 2.8 add Comment Status
        if (showStatus == true)
        {
            if (SEGMENT_FLAG == flag)
            {
                sheet.addCell(new Label(c++, r, ((Issue) comment).getStatus()));
            }
            else
            {
                sheet.addCell(new Label(c++, r, " "));
            }
        }
        // 2.9 add Comment priority
        if (true == showPriority)
        {
            if (SEGMENT_FLAG == flag)
            {
                sheet.addCell(new Label(c++, r, ((Issue) comment).getPriority()));
            }
            else
            {
                sheet.addCell(new Label(c++, r, " "));
            }
        }

        // 2.10 add Comment Category
        if (showCategory)
        {
            if (SEGMENT_FLAG == flag)
            {
                sheet.addCell(new Label(c++, r, ((Issue) comment).getCategory()));
            }
            else
            {
                sheet.addCell(new Label(c++, r, " "));
            }
        }

        // 2.11 add Comment comment
        sheet.addCell(new Label(c++, r, comment.getComment()));

        if (flag != SEGMENT_FLAG)
        {
            sheet.addCell(new Label(c++, r, ""));
            sheet.addCell(new Label(c++, r, ""));
            row.inc();
            return;
        }

        if (true)
        {
            sheet.addCell(new Label(c++, r, ""));
            sheet.addCell(new Formula(c++, r, "HYPERLINK(\"" + pageUrl
                    + "\",\"" + externalPageId + "\")"));
        }

        row.inc();

    }

    /**
     * add Comment for each row, for segment comment usage, each row contains a
     * comment history
     * 
     * @exception Exception
     */
    private void addCommentHistory(HttpServletRequest p_request, Job j,
            WritableSheet sheet, IntHolder row, Comment comment,
            IssueHistory issueHistory, int flag) throws Exception
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                p_request.getParameter("dateFormat"));
        HttpSession session = p_request.getSession();
        int c = 0;
        int r = row.getValue();
        // 2.3.2 Job ID column. Insert GlobalSight job number here.
        sheet.addCell(new Label(c++, r, Long.toString(j.getJobId())));

        // 2.4.2 Job: Insert Job name here.
        sheet.addCell(new Label(c++, r, j.getJobName()));

        // 2.5.2 Lang: Insert each target language identifier for each workflow
        // in the retrieved Job on a different row.
        if ((this.targetPageVar != null)
                && (this.targetPageVar.getGlobalSightLocale() != null))
        {
            sheet.addCell(new Label(c++, r, this.targetPageVar
                    .getGlobalSightLocale().getDisplayName(uiLocale)));
        }
        else
        {
            sheet.addCell(new Label(c++, r, ""));
        }

        // 2.6.2 Insert Segement Number for the job.
        sheet.addCell(new Label(c++, r, CommentComparator
                .getSegmentIdFromLogicalKey(((Issue) comment).getLogicalKey())));

        // by who
        sheet.addCell(new Label(c++, r, UserUtil.getUserNameById(issueHistory
                .reportedBy())));

        // 2.7.2 Comment create date: Insert Comment creation date.
        sheet.addCell(new Label(c++, r, dateFormat.format(issueHistory
                .dateReportedAsDate())));

        // 2.8.2 add Comment Status
        if (showStatus == true)
        {
            sheet.addCell(new Label(c++, r, ((Issue) comment).getStatus()));
        }
        // 2.9.2 add Comment priority
        if (true == showPriority)
        {
            sheet.addCell(new Label(c++, r, ((Issue) comment).getPriority()));
        }

        // 2.10.2 add Comment Category
        if (showCategory)
        {
            sheet.addCell(new Label(c++, r, ((Issue) comment).getCategory()));
        }

        // 2.11.2 add comment header
        sheet.addCell(new Label(c++, r, comment.getComment()));

        // 2.12.2 add comment body
        sheet.addCell(new Label(c++, r, issueHistory.getComment()));

        // 2.13.2 add link
        sheet.addCell(new Formula(c++, r, "HYPERLINK(\"" + pageUrl + "\",\""
                + externalPageId + "\")"));

        row.inc();

    }

}
