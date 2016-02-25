/**
 * Copyright 2009 Welocalize, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 */
package com.globalsight.everest.edit.offline.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.everest.comment.CommentManager;
import com.globalsight.everest.comment.Issue;
import com.globalsight.everest.comment.IssueHistory;
import com.globalsight.everest.comment.IssueHistoryImpl;
import com.globalsight.everest.comment.IssueImpl;
import com.globalsight.everest.comment.IssueOptions;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.OfflineFileUploadStatus;
import com.globalsight.everest.edit.offline.XliffConstants;
import com.globalsight.everest.edit.offline.page.OfflinePageData;
import com.globalsight.everest.edit.offline.page.OfflineSegmentData;
import com.globalsight.everest.edit.offline.page.PageData;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.PageManager;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.UnextractedFile;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.persistence.tuv.SegmentTuUtil;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFileMgr;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskManager;
import com.globalsight.everest.tuv.Tu;
import com.globalsight.everest.tuv.TuImpl;
import com.globalsight.everest.tuv.Tuv;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.Cancelable;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ImplementedCommentsCheckReportGenerator;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsReportGenerator;
import com.globalsight.everest.webapp.pagehandler.edit.EditCommonHelper;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.ling.rtf.RtfAPI;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.ling.tw.offline.parser.ParseException;
import com.globalsight.ling.tw.offline.parser.Token;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.regexp.internal.RECompiler;
import com.sun.org.apache.regexp.internal.REProgram;
import com.sun.org.apache.regexp.internal.RESyntaxException;

/**
 * Contains methods responsible for error-checking and saving uploaded content.
 */
public class UploadApi implements AmbassadorDwUpConstants, Cancelable
{
    static private final Logger CATEGORY = Logger.getLogger(UploadApi.class);

    private PageData m_referencePageData = null;

    private ArrayList<PageData> m_referencePageDatas = null;

    private OfflinePageData m_uploadPageData = null;

    private PtagErrorPageWriter m_errWriter = null;

    private OfflinePtagErrorChecker m_errChecker = null;

    private ResourceBundle m_messages = null;

    private GlobalSightLocale m_uiLocale = null;

    private UploadPageSaver m_uploadPageSaver = null;

    private String m_unextractedFilenameHead = null;

    private String m_unextractedFileId = null;

    private String m_unextractedFileType = null;

    private String m_unextractedFileTaskId = null;

    private String m_unextractedFilenameTail = null;

    private String m_unextractedFilenameExt = null;

    private String m_normalizedLB = null;

    private String SECONDARY_TYPE = "S";

    private String m_tempFileName = null;

    private Map<Long, String> segId2RequiredTranslation = null;

    private Map<Long, String> segId2Comment = null;

    private Map<Long, Long> segId2PageId = null;

    private Map<Long, String> segId2FailureType = null;

    private Map<Long, String> segId2CommentStatus = null;

    String qualityAssessment = null;

    String marketSuitabilty = null;

    String taskComment = null;

    private long reportTargetLocaleId = -1;

    public static final int REPORT_COMMENT_UPLOAD = 1;

    public static final int REPORT_TRANSLATION_UPLOAD = 2;

    static private int RE_UNEXTRACTED_FILE_HEAD = 1; // Regex position

    static private int RE_UNEXTRACTED_FILE_ID = 2; // Regex position

    static private int RE_UNEXTRACTED_FILE_TYPE = 3; // Regex position

    static private int RE_UNEXTRACTED_FILE_TASK_ID = 4; // Regex position

    static private int RE_UNEXTRACTED_FILE_TAIL = 5; // Regex position

    static private int RE_UNEXTRACTED_FILE_EXT = 2; // Regex position

    static private final REProgram RE_UNEXTRACTED_FILE_FNAME = createProgram("^(.*?)"
            + // (head)
            AmbassadorDwUpConstants.FILE_NAME_BREAK + "([0-9]+)([S||s||P||p])" + // (pageid)(type)
            AmbassadorDwUpConstants.FILE_NAME_BREAK + "([0-9]+)" + // (taskid)
            "(.*?)$"); // optional (tail)

    static private final REProgram RE_UNEXTRACTED_FILE_FNAME_EXT = createProgram("(.*)\\.(.*)$");

    private OEMProcessStatus status = null;

    public static int LOAD_DATA = 40;
    public static int CHECK_SAVE = 40;
    public static int COMMENT = 20;

    private boolean cancel = false;

    private static String GS_TOOLKIT_FORMAT = XliffConstants.WARN_SIGN
            + XliffConstants.GS_TOOLKIT_FORMAT;

    public OEMProcessStatus getStatus()
    {
        return status;
    }

    public void setStatus(OEMProcessStatus status)
    {
        this.status = status;
    }

    public void setTempFileName(String p_tempFileName)
    {
        this.m_tempFileName = p_tempFileName;
    }

    static private REProgram createProgram(String p_pattern)
    {
        REProgram pattern = null;

        try
        {
            RECompiler compiler = new RECompiler();
            pattern = compiler.compile(p_pattern);
        }
        catch (RESyntaxException ex)
        {
            // Pattern syntax error. Stop the application.
            throw new RuntimeException(ex.getMessage());
        }

        return pattern;
    }

    //
    // Constructors
    //

    /**
     * Default constructor.
     */
    public UploadApi() throws AmbassadorDwUpException
    {
        super();

        m_errWriter = new PtagErrorPageWriter();

        try
        {
            m_uploadPageSaver = new UploadPageSaver();
        }
        catch (Exception ex)
        {
            throw new AmbassadorDwUpException(ex);
        }
    }

    //
    // Public Methods
    //

    /**
     * Processes a single upload page (list view text file).
     * 
     * If the page contains errors, an HTML error page is returned as a string.
     * If no errors occur, segmemts are converted back to GXML and written to
     * database.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_inputStream
     *            a stream opened on the input file..
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId
     *            - the task Id that the upload page must match.
     * @param p_fileName
     *            - The name of the file to be uploaded. Used for email
     *            notification.
     * @param p_jmsQueueDestination
     *            - The JMS destination queue for performing the save and
     *            indexing process in the background.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
    @SuppressWarnings("rawtypes")
    public String processPage(Reader p_reader, User p_user, long p_ownerTaskId,
            String p_fileName, Collection p_excludedItemTypes,
            String p_jmsQueueDestination)
    {
        // for GBS-1939
        List<Task> isUploadingTasks = new ArrayList<Task>();

        try
        {
            // m_referencePageData = new PageData();
            // so getPage() will be cleared if errors occur
            m_uploadPageData = new OfflinePageData();

            m_referencePageDatas = new ArrayList<PageData>();

            String errPage = null;
            String checkErrMsg = null;
            // Set locale
            if ((errPage = setLocale(p_user)) != null)
            {
                return errPage;
            }

            // load the upload file
            if ((errPage = loadListViewTextFile(p_reader, p_fileName, false)) != null)
            {
                CATEGORY.error("UploadApi.processPage(): "
                        + "Unable to load the upload-file.");

                return errPage;
            }

            // for GBS-1939, for GBS-2829
            String taskId = m_uploadPageData.getTaskId();
            String[] taskIds = null;
            if (taskId.contains(","))
            {
                taskIds = taskId.split(",");
                List<Long> taskIdsList = new ArrayList<Long>();
                for (String tid : taskIds)
                {
                    Long isUploadingTaskId = Long.valueOf(tid);
                    taskIdsList.add(isUploadingTaskId);
                    // Update task status (Uploading)
                    Task isUploadingTask = TaskHelper.updateTaskStatus(
                            isUploadingTaskId, UPLOAD_IN_PROGRESS);
                    if (isUploadingTask != null)
                    {
                        isUploadingTasks.add(isUploadingTask);

                        OfflineFileUploadStatus.addFileState(isUploadingTaskId,
                                p_fileName, "Running");
                    }
                }
                m_uploadPageData.setTaskIds(taskIdsList);
            }
            else
            {
                Long isUploadingTaskId = Long.valueOf(taskId);
                // Update task status (Uploading)
                Task isUploadingTask = TaskHelper.updateTaskStatus(
                        isUploadingTaskId, UPLOAD_IN_PROGRESS);
                isUploadingTasks.add(isUploadingTask);
                OfflineFileUploadStatus.addFileState(isUploadingTaskId,
                        p_fileName, "Running");
            }

            // Fix for GBS-2191
            if (p_ownerTaskId == -1)
            {
                String ownerTaskId = taskIds != null ? taskIds[0] : taskId;
                p_ownerTaskId = Long.parseLong(ownerTaskId);
                m_uploadPageData.setTaskId(ownerTaskId);

                Task task = getTask(p_ownerTaskId);
                if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                {
                    completeUploadingTask(isUploadingTasks);
                    return errPage;
                }
                p_excludedItemTypes = getExcludedItemTypes(task);
            }

            // check all tasks for combined download files
            if (isUploadingTasks != null)
            {
                for (Task task : isUploadingTasks)
                {
                    if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                    {
                        completeUploadingTask(isUploadingTasks);
                        return errPage;
                    }
                }
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_reader)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }

            // Verify if upload file is a consolidated file
            if (m_uploadPageData.getPageId().indexOf(",") > 0)
                m_uploadPageData.setConsolated(true);

            if ((errPage = postLoadInit(p_ownerTaskId,
                    m_uploadPageData.getTaskId(), p_excludedItemTypes,
                    DOWNLOAD_FILE_FORMAT_TXT)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkPage(p_user, p_fileName)) != null)
            {
                // do not return here.
                checkErrMsg = errPage;
            }

            // do not save page if iscontinue = n for internal tag error
            if (this.status.getIsContinue() != null
                    && this.status.getIsContinue().equals(Boolean.FALSE))
            {
                // do not return here
                if (checkErrMsg == null)
                    checkErrMsg = "IsContinue=fasle";
            }

            if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                    p_jmsQueueDestination, p_user, p_fileName, isUploadingTasks)) != null)
            {
                completeUploadingTask(isUploadingTasks);
            }

            return getErrorMsg(checkErrMsg, errPage);
        }
        finally
        {

        }
    }

    /**
     * Processes a single upload page (RTF paragraph view).
     * 
     * If the page contains errors, an HTML error page is returned as a string.
     * If no errors occur, segmemts are converted back to GXML and written to
     * database.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_rtfDoc
     *            an RTF DOM representing the uploaded content.
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId
     *            - the task Id that the upload page must match.
     * @param p_fileName
     *            - The name of the file to be uploaded. Used for email
     *            notification.
     * @param p_jmsQueueDestination
     *            - The JMS queue used for performing the save and indexing
     *            process in the background.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
    @SuppressWarnings("rawtypes")
    public String process_GS_PARAVIEW_1(RtfDocument p_rtfDoc, User p_user,
            long p_ownerTaskId, String p_fileName,
            Collection p_excludedItemTypes, String p_jmsQueueDestination)
    {
        try
        {
            List<Task> isUploadingTasks = new ArrayList<Task>();
            isUploadingTasks.add(getTask(Long.valueOf(m_uploadPageData
                    .getTaskId())));
            // m_referencePageData = new PageData();
            // so getPage() will be cleared if errors occur
            m_uploadPageData = new OfflinePageData();

            String errPage = null;
            String checkErrMsg = null;

            // Set locale
            if ((errPage = setLocale(p_user)) != null)
            {
                return errPage;
            }

            // load the upload file
            if ((errPage = load_GS_PARAVIEW_1_File(p_rtfDoc, p_fileName)) != null)
            {
                CATEGORY.error("UploadApi.process_GS_PARAVIEW_1(): "
                        + "Unable to load the upload-file.");

                return errPage;
            }

            // for GBS-1939:Update task status (Uploading)
            TaskHelper.updateTaskStatus(
                    Long.valueOf(m_uploadPageData.getTaskId()),
                    UPLOAD_IN_PROGRESS);
            OfflineFileUploadStatus.addFileState(
                    Long.valueOf(m_uploadPageData.getTaskId()), p_fileName,
                    "Running");

            // Fix for GBS-2191
            if (p_ownerTaskId == -1)
            {
                p_ownerTaskId = Long.valueOf(m_uploadPageData.getTaskId());
                Task task = getTask(p_ownerTaskId);
                if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                {
                    completeUploadingTask(isUploadingTasks);
                    return errPage;
                }
                p_excludedItemTypes = getExcludedItemTypes(task);
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_rtfDoc)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }

            if ((errPage = postLoadInit(p_ownerTaskId,
                    m_uploadPageData.getTaskId(), p_excludedItemTypes,
                    DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkPage(p_user, p_fileName)) != null)
            {
                // do not return here.
                checkErrMsg = errPage;
            }

            // do not save page if iscontinue = n for internal tag error
            if (this.status.getIsContinue() != null
                    && this.status.getIsContinue().equals(Boolean.FALSE))
            {
                // do not return here
                if (checkErrMsg == null)
                    checkErrMsg = "IsContinue=fasle";
            }

            if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                    p_jmsQueueDestination, p_user, p_fileName, isUploadingTasks)) != null)
            {
                completeUploadingTask(isUploadingTasks);
            }

            return getErrorMsg(checkErrMsg, errPage);
        }
        finally
        {

        }
    }

    public String processReport(File p_tempFile, User p_user, Task p_task,
            String p_fileName, String p_jmsQueueDestination, String p_reportName)
            throws Exception
    {
        String errPage = null;
        boolean uploaded = false;

        long taskId = p_task.getId();
        Task task = ServerProxy.getTaskManager().getTask(taskId);
        long jobId = task.getJobId();
        if ((errPage = preLoadInit(taskId, p_user)) != null)
        {
            return errPage;
        }

        try
        {
            // Update task status (Uploading)
            TaskHelper.updateTaskStatus(taskId, UPLOAD_IN_PROGRESS);

            // load the upload file
            if ((errPage = loadReportData(p_tempFile, p_fileName, taskId,
                    p_reportName)) != null)
            {
                CATEGORY.error("UploadApi.loadReportData(): "
                        + "Unable to load the upload-file.");

                return errPage;
            }

            // check the task acceptor and task state(Task.STATE_ACCEPTED)
            if ((errPage = checkReportTask(p_user, task)) != null)
            {
                return errPage;
            }

            // check task type
            int taskType = task.getType();
            if (!task.isType(taskType))
            {
                String errMsg = "TaskTypeError: Type matching error.";
                m_errWriter.addFileErrorMsg(errMsg);

                return m_errWriter.buildReportErroPage().toString();
            }

            if (cancel)
                return null;

            if (p_reportName.equals(WebAppConstants.TRANSLATION_EDIT)
                    || WebAppConstants.POST_REVIEW_QA.equals(p_reportName)
                    || WebAppConstants.TRANSLATION_VERIFICATION.equals(p_reportName))
            {
                if ((errPage = createErrorChecker()) != null)
                {
                    return errPage;
                }

                if (cancel)
                    return null;

                // Check uploaded page. Save segments without errors, ignore
                // those with errors.
                if ((errPage = checkReportPage(p_user, jobId)) != null)
                {
                    // do not return here, need to continue to upload comments.
                }
            }

            if (WebAppConstants.POST_REVIEW_QA.equals(p_reportName))
            {
                // update task comment
                if (taskComment != null && !"".equals(taskComment))
                {
                    TaskHelper.saveComment(task, task.getId(),
                            p_user.getUserId(), taskComment);
                }
                TaskHelper.updateReviewInfo(task.getId(), qualityAssessment,
                        marketSuitabilty);
            }

            if (cancel)
                return null;

            // update segment comments
            uploadComments(p_user, p_reportName, jobId);

            uploaded = true;
        }
        finally
        {
            // Update task status (Upload Done)
            TaskHelper.updateTaskStatus(taskId, UPLOAD_DONE, uploaded);
            CATEGORY.info("Report uploading for report name(" + p_reportName
                    + ") and file name(" + p_fileName + ") is finished.");
        }

        return errPage;
    }

    private void updateProcess(int n)
    {
        if (status == null)
            return;

        status.updateProcess(n);
    }

    private String loadReportData(File p_tempFile, String p_fileName,
            long p_taskId, String p_reportName)
    {
        if (cancel)
            return null;

        File tmpFile = null;
        FileInputStream fis = null;

        try
        {
            m_errWriter.setFileName(p_fileName);
            if (StringUtil.isEmpty(p_fileName))
            {
                m_errWriter
                        .addFileErrorMsg("The file name is empty. Please make sure it is correct and upload again.");
                return m_errWriter.buildReportErroPage().toString();
            }
            if (!ExcelUtil.isExcel(p_fileName))
            {
                m_errWriter
                        .addFileErrorMsg("The file you are trying to upload is not an excel (xls or xlsx format)."
                                + "\r\nPlease make sure it is correct and upload again.");
                return m_errWriter.buildReportErroPage().toString();
            }

            String fileSuff = p_fileName.substring(p_fileName.lastIndexOf("."));
            /**
             * Create a temporary file to get data from excel
             */
            tmpFile = File.createTempFile("RI_", fileSuff);
            FileUtils.copyFile(p_tempFile, tmpFile);
            fis = new FileInputStream(tmpFile);

            Task task = ServerProxy.getTaskManager().getTask(p_taskId);

            Workbook workbook = ExcelUtil.getWorkbook(
                    tmpFile.getAbsolutePath(), fis);
            Sheet sheet = null;

            if (WebAppConstants.LANGUAGE_SIGN_OFF.equals(p_reportName))
            {
                String sheetName = task.getTargetLocale().toString();
                sheet = workbook.getSheet(sheetName);
            }
            if (sheet == null)
                sheet = ExcelUtil.getDefaultSheet(workbook);

            if (sheet == null)
            {
                m_errWriter.addFileErrorMsg("No Sheet detected.");
                return m_errWriter.buildReportErroPage().toString();
            }
            ResourceBundle bundle = SystemResourceBundle.getInstance()
                    .getResourceBundle(
                            ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                            Locale.US);

            int languageInfoRow = ImplementedCommentsCheckReportGenerator.LANGUAGE_INFO_ROW;
            int segmentHeaderRow = ImplementedCommentsCheckReportGenerator.SEGMENT_HEADER_ROW;
            int segmentStartRow = ImplementedCommentsCheckReportGenerator.SEGMENT_START_ROW;

            if (p_reportName.equals(WebAppConstants.LANGUAGE_SIGN_OFF))
            {
                languageInfoRow = ReviewersCommentsReportGenerator.LANGUAGE_INFO_ROW;
                segmentHeaderRow = ReviewersCommentsReportGenerator.SEGMENT_HEADER_ROW;
                segmentStartRow = ReviewersCommentsReportGenerator.SEGMENT_START_ROW;
            }

            String targetLanguage = sheet.getRow(languageInfoRow).getCell(1)
                    .toString();
            if (StringUtil.isEmpty(targetLanguage))
            {
                m_errWriter
                        .addFileErrorMsg("No language information detected.");
                return m_errWriter.buildReportErroPage().toString();
            }
            else if (targetLanguage.indexOf('[') < 0
                    || targetLanguage.indexOf(']') < 0)
            {
                m_errWriter
                        .addFileErrorMsg("Target language format is not correct.\r\nIt should "
                                + "contain a portion which is a locale code encolsed by [ ] such as [zh_CN]");
                return m_errWriter.buildReportErroPage().toString();
            }
            reportTargetLocaleId = getLocaleId(targetLanguage);
            GlobalSightLocale tLocale = HibernateUtil.get(
                    GlobalSightLocale.class, reportTargetLocaleId);

            updateProcess(1);
            // Load the TUs and TUVs prior to improve performance.
            if (task != null)
            {
                for (Iterator spIt = task.getSourcePages(
                        PrimaryFile.EXTRACTED_FILE).iterator(); spIt.hasNext();)
                {
                    if (cancel)
                        return null;

                    SourcePage sp = (SourcePage) spIt.next();
                    SegmentTuUtil.getTusBySourcePageId(sp.getId());
                    SegmentTuvUtil.getSourceTuvs(sp);
                }
                updateProcess(3);
                for (Iterator tpIt = task.getTargetPages(
                        PrimaryFile.EXTRACTED_FILE).iterator(); tpIt.hasNext();)
                {
                    if (cancel)
                        return null;

                    TargetPage tp = (TargetPage) tpIt.next();
                    SegmentTuvUtil.getTargetTuvs(tp);
                }
            }

            updateProcess(5);

            if (WebAppConstants.TRANSLATION_EDIT.equals(p_reportName))
            {
                if (isTERReport(sheet, segmentHeaderRow))
                {
                    return loadTERReportData(sheet, task, tLocale);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
                                    + "\r\nMaybe you have changed some column header signatures or orders."
                                    + "\r\nThe following column header signatures and orders should keep the source report's format."
                                    + "\r\nJob id, Segment id, TargetPage id, Required translation."
                                    + "\r\nPlease make sure they are correct and upload again.");
                    return m_errWriter.buildReportErroPage().toString();
                }
            }
            else if (WebAppConstants.LANGUAGE_SIGN_OFF.equals(p_reportName))
            {
                if (isRCRSimpleReportAfter855(sheet, segmentHeaderRow))
                {
                    return loadRCRSimpleReportDataAfter855(sheet, task,
                            tLocale, bundle);
                }
                else if (isRCRSimpleReportFor855(sheet, segmentHeaderRow))
                {
                    return loadRCRSimpleReportDataFor855(sheet, task, tLocale,
                            bundle);
                }
                else if (isRCRReport(sheet, segmentHeaderRow))
                {
                    return loadRCRReportData(sheet, task, tLocale, bundle);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("The report type is not correct."
                                    + "\r\nPlease make sure the report type is correct and upload again.");
                    return m_errWriter.buildReportErroPage().toString();
                }
            }
            else if (WebAppConstants.POST_REVIEW_QA.equals(p_reportName))
            {
                if (isPRRReport(sheet, segmentHeaderRow + 4))
                {
                    return loadPRRReportData(sheet, task, tLocale);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("The report type is not correct."
                                    + "\r\nPlease make sure the report type is correct and upload again.");
                    return m_errWriter.buildReportErroPage().toString();
                }
            }
            else if (WebAppConstants.TRANSLATION_VERIFICATION
                    .equals(p_reportName))
            {
                if (isTVRReport(sheet, segmentHeaderRow))
                {
                    return loadTVRReportData(sheet, task, tLocale);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("The report type is not correct."
                                    + "\r\nPlease make sure the report type is correct and upload again.");
                    return m_errWriter.buildReportErroPage().toString();
                }
            }
        }
        catch (Throwable ex)
        {
            String args[] =
            { EditUtil.encodeHtmlEntities(ex.getMessage()) };
            String errMsg = MessageFormat
                    .format(m_messages.getString("FormatTwoLoadError"),
                            (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildReportErroPage().toString();
        }
        finally
        {
            try
            {
                if (fis != null)
                    fis.close();

                if (tmpFile != null)
                    tmpFile.delete();
            }
            catch (Exception e)
            {
                CATEGORY.error("Cannot close Excel file.", e);
            }
        }

        return null;
    }

    private String loadTERReportData(Sheet sheet, Task task,
            GlobalSightLocale tLocale) throws RemoteException
    {
        int segmentStartRow = ImplementedCommentsCheckReportGenerator.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();

        segId2RequiredTranslation = new HashMap<Long, String>();
        segId2PageId = new HashMap<Long, Long>();
        segId2FailureType = new HashMap<Long, String>();
        segId2Comment = new HashMap<Long, String>();
        segId2CommentStatus = new HashMap<Long, String>();

        String segmentId = null;
        long segIdLong;
        String updatedText = null;
        String jobIdText = null;
        String comment = null;
        String requiredComment = null;
        String commentStatus = null;
        boolean hasSegmentIdErro = false;

        int n = 5;
        int m = LOAD_DATA - n;

        for (int j = segmentStartRow, row = sheet.getLastRowNum(); j <= row; j++)
        {
            if (cancel)
                return null;

            int x = j * m / row;
            updateProcess(n + x);

            segmentId = ExcelUtil.getCellValue(sheet, j, 11);
            if (StringUtil.isEmpty(segmentId))
            {
                break;
            }
            segIdLong = new Long(Long.parseLong(segmentId));

            updatedText = ExcelUtil.getCellValue(sheet, j, 2);
            comment = ExcelUtil.getCellValue(sheet, j, 3);
            requiredComment = ExcelUtil.getCellValue(sheet, j, 4);
            commentStatus = ExcelUtil.getCellValue(sheet, j, 6);

            if (EditUtil.isRTLLocale(tLocale))
                updatedText = EditUtil.removeU200F(updatedText);

            jobIdText = ExcelUtil.getCellValue(sheet, j, 10);
            jobIds.add(jobIdText);
            if (segmentId != null && !segmentId.equals(""))
            {
                if (updatedText != null && !updatedText.equals(""))
                {
                    segId2RequiredTranslation.put(segIdLong, updatedText);
                }
                if (comment != null && !comment.equals(""))
                {
                    if (requiredComment != null
                            && !requiredComment.equals("")
                            && !StringUtil.equalsIgnoreSpace(requiredComment,
                                    ""))
                    {
                        segId2Comment.put(segIdLong, requiredComment);
                    }
                    segId2CommentStatus.put(segIdLong, commentStatus);
                }
                else
                {
                    if (requiredComment != null
                            && !requiredComment.equals("")
                            && !StringUtil.equalsIgnoreSpace(requiredComment,
                                    ""))
                    {
                        segId2Comment.put(segIdLong, requiredComment);
                        segId2CommentStatus.put(segIdLong, "query");
                    }
                }
            }
            else
            {
                m_errWriter.addFileErrorMsg("Segment id is lost in row "
                        + (j + 1) + "\r\n");
                hasSegmentIdErro = true;
            }
        }

        if (hasSegmentIdErro)
        {
            return m_errWriter.buildReportErroPage().toString();
        }

        if (jobIds.size() > 1)
        {
            m_errWriter
                    .addFileErrorMsg("The job id is not consistent, you may hava changed some of them."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if ((jobIds.size() == 1)
                && !jobIds.contains(String.valueOf(task.getJobId())))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not belong to this job."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if (jobIds.size() == 0)
        {
            m_errWriter
                    .addFileErrorMsg("No job id detected."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }

        return null;
    }

    private String loadRCRSimpleReportDataAfter855(Sheet sheet, Task task,
            GlobalSightLocale tLocale, ResourceBundle bundle)
            throws RemoteException
    {
        int segmentStartRow = ReviewersCommentsReportGenerator.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();

        segId2Comment = new HashMap<Long, String>();
        segId2PageId = new HashMap<Long, Long>();
        segId2FailureType = new HashMap<Long, String>();
        segId2CommentStatus = new HashMap<Long, String>();
        String segmentId = null;
        String pageId = null;
        String reviewerComment = null;
        Long segIdLong = null;
        String jobIdText = null;
        String failureType = null;
        String commentStatus = null;
        boolean hasIdErro = false;
        for (int k = segmentStartRow, row = sheet.getLastRowNum(); k <= row; k++)
        {
            if (cancel)
                return null;

            segmentId = ExcelUtil.getCellValue(sheet, k, 8);
            if (segmentId == null || segmentId.trim().length() == 0)
            {
                break;
            }
            jobIdText = ExcelUtil.getCellValue(sheet, k, 7);
            jobIds.add(jobIdText);
            long currentJobId = Long.parseLong(jobIdText);
            segIdLong = new Long(Long.parseLong(segmentId));
            Tu tu = ServerProxy.getTuvManager().getTuForSegmentEditor(
                    segIdLong, currentJobId);
            TuImpl tuImpl = (TuImpl) tu;
            Tuv tuv = tuImpl.getTuv(reportTargetLocaleId, currentJobId);
            TuvImpl tuvImpl = (TuvImpl) tuv;
            TargetPage targetPage = tuvImpl.getTargetPage(currentJobId);
            pageId = new String(String.valueOf(targetPage.getId()));

            reviewerComment = ExcelUtil.getCellValue(sheet, k, 2);
            if (EditUtil.isRTLLocale(tLocale))
                reviewerComment = EditUtil.removeU200F(reviewerComment);

            failureType = ExcelUtil.getCellValue(sheet, k, 3);
            commentStatus = "";

            if (StringUtil.isNotEmpty(reviewerComment)
                    || checkCommentStatus(sheet, k))
            {
                if (segmentId != null && !segmentId.equals("")
                        && pageId != null && !pageId.equals(""))
                {
                    segId2PageId.put(segIdLong,
                            new Long(Long.parseLong(pageId)));
                    segId2Comment.put(segIdLong, reviewerComment);
                    segId2FailureType.put(segIdLong, failureType);
                    segId2CommentStatus.put(segIdLong, commentStatus);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("Segment or Page id is lost in row "
                                    + (k + 1) + "\r\n");
                    hasIdErro = true;
                }

            }
        }
        if (hasIdErro)
        {
            return m_errWriter.buildReportErroPage().toString();
        }

        if (jobIds.size() > 1)
        {
            m_errWriter
                    .addFileErrorMsg("The job id is not consistent, you may change some of them."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if ((jobIds.size() == 1)
                && !jobIds.contains(String.valueOf(task.getJobId())))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not belong to this job."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if (jobIds.size() == 0)
        {
            m_errWriter
                    .addFileErrorMsg("No job id detected."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }

        return null;
    }

    private String loadRCRSimpleReportDataFor855(Sheet sheet, Task task,
            GlobalSightLocale tLocale, ResourceBundle bundle)
            throws RemoteException
    {
        int segmentStartRow = ReviewersCommentsReportGenerator.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();

        segId2Comment = new HashMap<Long, String>();
        segId2PageId = new HashMap<Long, Long>();
        segId2FailureType = new HashMap<Long, String>();
        segId2CommentStatus = new HashMap<Long, String>();
        String segmentId = null;
        String pageId = null;
        String reviewerComment = null;
        Long segIdLong = null;
        String jobIdText = null;
        String failureType = null;
        String commentStatus = null;
        boolean hasIdErro = false;
        for (int k = segmentStartRow, row = sheet.getLastRowNum(); k <= row; k++)
        {
            if (cancel)
                return null;

            segmentId = ExcelUtil.getCellValue(sheet, k, 7);
            if (segmentId == null || segmentId.trim().length() == 0)
            {
                break;
            }

            jobIdText = ExcelUtil.getCellValue(sheet, k, 6);
            jobIds.add(jobIdText);
            long currentJobId = Long.parseLong(jobIdText);
            segIdLong = new Long(Long.parseLong(segmentId));
            Tu tu = ServerProxy.getTuvManager().getTuForSegmentEditor(
                    segIdLong, currentJobId);
            TuImpl tuImpl = (TuImpl) tu;
            Tuv tuv = tuImpl.getTuv(reportTargetLocaleId, currentJobId);
            TuvImpl tuvImpl = (TuvImpl) tuv;
            TargetPage targetPage = tuvImpl.getTargetPage(currentJobId);
            pageId = new String(String.valueOf(targetPage.getId()));

            reviewerComment = ExcelUtil.getCellValue(sheet, k, 2);
            if (EditUtil.isRTLLocale(tLocale))
                reviewerComment = EditUtil.removeU200F(reviewerComment);

            failureType = "";
            commentStatus = "";

            if (StringUtil.isNotEmpty(reviewerComment)
                    || checkCommentStatus(sheet, k))
            {
                if (segmentId != null && !segmentId.equals("")
                        && pageId != null && !pageId.equals(""))
                {
                    segId2PageId.put(segIdLong,
                            new Long(Long.parseLong(pageId)));
                    segId2Comment.put(segIdLong, reviewerComment);
                    segId2FailureType.put(segIdLong, failureType);
                    segId2CommentStatus.put(segIdLong, commentStatus);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("Segment or Page id is lost in row "
                                    + (k + 1) + "\r\n");
                    hasIdErro = true;
                }

            }
        }
        if (hasIdErro)
        {
            return m_errWriter.buildReportErroPage().toString();
        }

        if (jobIds.size() > 1)
        {
            m_errWriter
                    .addFileErrorMsg("The job id is not consistent, you may change some of them."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if ((jobIds.size() == 1)
                && !jobIds.contains(String.valueOf(task.getJobId())))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not belong to this job."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if (jobIds.size() == 0)
        {
            m_errWriter
                    .addFileErrorMsg("No job id detected."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }

        return null;
    }

    private String loadRCRReportData(Sheet sheet, Task task,
            GlobalSightLocale tLocale, ResourceBundle bundle)
            throws RemoteException
    {
        int segmentStartRow = ReviewersCommentsReportGenerator.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();

        segId2Comment = new HashMap<Long, String>();
        segId2PageId = new HashMap<Long, Long>();
        segId2FailureType = new HashMap<Long, String>();
        segId2CommentStatus = new HashMap<Long, String>();
        String segmentId = null;
        String pageId = null;
        String reviewerComment = null;
        Long segIdLong = null;
        String jobIdText = null;
        String failureType = null;
        String commentStatus = null;
        boolean hasIdErro = false;
        for (int k = segmentStartRow, row = sheet.getLastRowNum(); k <= row; k++)
        {
            if (cancel)
                return null;

            segmentId = ExcelUtil.getCellValue(sheet, k, 10);
            if (segmentId == null || segmentId.trim().length() == 0)
            {
                break;
            }

            jobIdText = ExcelUtil.getCellValue(sheet, k, 9);
            jobIds.add(jobIdText);
            long curJobId = Long.parseLong(jobIdText);
            segIdLong = new Long(Long.parseLong(segmentId));
            Tu tu = ServerProxy.getTuvManager().getTuForSegmentEditor(
                    segIdLong, curJobId);
            TuImpl tuImpl = (TuImpl) tu;
            Tuv tuv = tuImpl.getTuv(reportTargetLocaleId, curJobId);
            TuvImpl tuvImpl = (TuvImpl) tuv;
            TargetPage targetPage = tuvImpl.getTargetPage(curJobId);
            pageId = new String(String.valueOf(targetPage.getId()));

            reviewerComment = ExcelUtil.getCellValue(sheet, k, 3);
            if (EditUtil.isRTLLocale(tLocale))
                reviewerComment = EditUtil.removeU200F(reviewerComment);

            failureType = ExcelUtil.getCellValue(sheet, k, 4);
            commentStatus = ExcelUtil.getCellValue(sheet, k, 5);

            if (StringUtil.isNotEmpty(reviewerComment)
                    || checkCommentStatus(sheet, k))
            {
                if (segmentId != null && !segmentId.equals("")
                        && pageId != null && !pageId.equals(""))
                {
                    segId2PageId.put(segIdLong,
                            new Long(Long.parseLong(pageId)));
                    segId2Comment.put(segIdLong, reviewerComment);
                    segId2FailureType.put(segIdLong, failureType);
                    segId2CommentStatus.put(segIdLong, commentStatus);
                }
                else
                {
                    m_errWriter
                            .addFileErrorMsg("Segment or Page id is lost in row "
                                    + (k + 1) + "\r\n");
                    hasIdErro = true;
                }
            }
        }

        if (hasIdErro)
        {
            return m_errWriter.buildReportErroPage().toString();
        }

        if (jobIds.size() > 1)
        {
            m_errWriter
                    .addFileErrorMsg("The job id is not consistent, you may change some of them."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if ((jobIds.size() == 1)
                && !jobIds.contains(String.valueOf(task.getJobId())))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not belong to this job."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if (jobIds.size() == 0)
        {
            m_errWriter
                    .addFileErrorMsg("No job id detected."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }

        return null;
    }

    // This is since 8.5.0.1 version (Aug. 2013)
    private boolean isTERReport(Sheet sheet, int segmentHeaderRow)
    {
        // Cell "K7"
        String jobId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 10);
        // Cell "L7"
        String segmentId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 11);
        // Cell "M7"
        String pageName = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 12);
        // Cell "C7"
        String modifyTranslation = ExcelUtil.getCellValue(sheet,
                segmentHeaderRow, 2);

        if ("Job id".equalsIgnoreCase(jobId)
                && "Segment id".equalsIgnoreCase(segmentId)
                && "Page name".equalsIgnoreCase(pageName)
                && modifyTranslation.startsWith("Modify the translation here"))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if this report is generated from production instance which version
     * is AFTER 8501.
     */
    private boolean isRCRReport(Sheet sheet, int segmentHeaderRow)
    {
        // Cell "J7"
        String jobId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 9);
        // Cell "K7"
        String segmentId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 10);
        // Cell "L7"
        String pageName = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 11);
        // Cell "D7"
        String revComments = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 3);

        if ("Job id".equalsIgnoreCase(jobId)
                && "Segment Id".equalsIgnoreCase(segmentId)
                && "Page name".equalsIgnoreCase(pageName)
                && "Reviewers Comments (enter your comments here)"
                        .equalsIgnoreCase(revComments))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if this report is RCR simplified report.
     */
    private boolean isRCRSimpleReportFor855(Sheet sheet, int segmentHeaderRow)
    {
        // Cell "G7"
        String jobId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 6);
        // Cell "H7"
        String segmentId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 7);
        // Cell "I7"
        String pageName = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 8);
        // Cell "C7"
        String revComments = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 2);

        if ("Job id".equalsIgnoreCase(jobId)
                && "Segment Id".equalsIgnoreCase(segmentId)
                && "Page name".equalsIgnoreCase(pageName)
                && "Reviewers Comments (enter your comments here)"
                        .equalsIgnoreCase(revComments))
        {
            return true;
        }

        return false;
    }

    /**
     * Check if this report is RCR simplified report.
     */
    private boolean isRCRSimpleReportAfter855(Sheet sheet, int segmentHeaderRow)
    {
        // Cell "H7"
        String jobId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 7);
        // Cell "I7"
        String segmentId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 8);
        // Cell "J7"
        String pageName = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 9);
        // Cell "C7"
        String revComments = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 2);

        if ("Job id".equalsIgnoreCase(jobId)
                && "Segment Id".equalsIgnoreCase(segmentId)
                && "Page name".equalsIgnoreCase(pageName)
                && "Reviewers Comments (enter your comments here)"
                        .equalsIgnoreCase(revComments))
        {
            return true;
        }

        return false;
    }

    // Check comment status for upload(ReviewersCommentsReport).
    private boolean checkCommentStatus(
            org.apache.poi.ss.usermodel.Sheet p_sheet, int p_row)
    {
        String translatorComment = ExcelUtil.getCellValue(p_sheet, p_row, 2);
        String commentStatus = ExcelUtil.getCellValue(p_sheet, p_row, 5);
        if (StringUtil.isNotEmpty(commentStatus)
                && StringUtil.isNotEmpty(translatorComment))
            return true;

        return false;
    }

    // Updated comment status.
    private void updateCommentStatus(Issue p_issue, String commentStatus)
    {
        IssueImpl issue = HibernateUtil.get(IssueImpl.class, p_issue.getId());
        issue.setStatus(commentStatus);
        HibernateUtil.update(issue);
    }

    private void uploadComments(User p_user, String p_reportName, long p_jobId)
            throws Exception
    {
        CommentManager commentManager = ServerProxy.getCommentManager();

        Set<Long> tuIds = segId2CommentStatus.keySet();
        Iterator<Long> tuIdIterator = tuIds.iterator();
        long tuId = -1;
        long tuvId = -1;
        Long tuIdLong = null;
        String comment = null;
        String failureType = null;
        String commentStatus = null;

        // Used to tell which target page's issues have been cached
        Set<Long> cachedTargetPageIds = new HashSet<Long>();
        // String: targetPageId_tuId_tuvId_
        Map<String, ArrayList<IssueImpl>> cachedIssues = new HashMap<String, ArrayList<IssueImpl>>();
        List<IssueImpl> existIssues = null;

        int n, m;
        if (WebAppConstants.TRANSLATION_EDIT.equals(p_reportName)
                || WebAppConstants.TRANSLATION_VERIFICATION
                        .equals(p_reportName))
        {
            n = LOAD_DATA + CHECK_SAVE;
            m = COMMENT;
        }
        else
        {
            n = 5;
            m = 95;
        }

        int j = -1;
        int size = tuIds.size();

        List<IssuesVo> issuesVos = new ArrayList<IssuesVo>();
        List<IssueImpl> newIssues = new ArrayList<IssueImpl>();

        while (tuIdIterator.hasNext())
        {
            j++;
            int x = j * m / size;
            updateProcess(n + x);

            tuIdLong = (Long) tuIdIterator.next();
            tuId = tuIdLong.longValue();
            Tuv tuv = SegmentTuvUtil.getTuvByTuIdLocaleId(tuId,
                    reportTargetLocaleId, p_jobId);
            tuvId = tuv.getId();
            comment = (String) segId2Comment.get(tuIdLong);
            failureType = (String) segId2FailureType.get(tuIdLong);
            commentStatus = (String) segId2CommentStatus.get(tuIdLong);
            boolean commentStatusError = false;
            boolean failureTypeError = false;
            // Fix for GBS-2383
            if (failureType == null)
            {
                // If the failureType is null, then failureType error
                failureTypeError = true;
            }
            if (!IssueOptions.getAllStatus().contains(commentStatus))
            {
                // If the commentStatus is not open, query or closed, then
                // comment status error
                commentStatusError = true;
            }

            Long targetPageId = (Long) segId2PageId.get(tuIdLong);
            if (targetPageId == null)
            {
                TargetPage targetPage = ((TuvImpl) tuv).getTargetPage(p_jobId);
                targetPageId = targetPage.getId();
            }
            // has NOT been cached
            if (!cachedTargetPageIds.contains(targetPageId))
            {
                List<IssueImpl> tpIssues = commentManager.getIssues(
                        Issue.TYPE_SEGMENT, targetPageId);
                cachedTargetPageIds.add(targetPageId);
                for (IssueImpl issue : tpIssues)
                {
                    int index = issue.getLogicalKey().lastIndexOf("_");
                    String tpId_tuId_tuvId = issue.getLogicalKey().substring(0,
                            index + 1);
                    ArrayList<IssueImpl> issues = cachedIssues
                            .get(tpId_tuId_tuvId);
                    if (issues == null)
                    {
                        issues = new ArrayList<IssueImpl>();
                    }
                    issues.add(issue);
                    cachedIssues.put(tpId_tuId_tuvId, issues);
                }
            }

            existIssues = cachedIssues.get(targetPageId + "_" + tuId + "_"
                    + tuvId + "_");
            if (existIssues == null || existIssues.size() == 0)
            {
                failureType = failureTypeError ? "" : failureType;
                commentStatus = commentStatusError ? Issue.STATUS_OPEN
                        : commentStatus;
                IssueImpl issue = new IssueImpl(Issue.TYPE_SEGMENT, tuvId,
                        "Comment by LSO", Issue.PRI_MEDIUM, commentStatus,
                        failureType.trim(), p_user.getUserId(), comment,
                        CommentHelper.makeLogicalKey(targetPageId, tuId, tuvId,
                                0));
                issue.setShare(false);
                issue.setOverwrite(false);
                newIssues.add(issue);
            }
            else
            {
                List histories = null;

                for (int i = 0; i < existIssues.size(); i++)
                {
                    Issue issue = (Issue) existIssues.get(i);
                    histories = issue.getHistory();

                    commentStatus = commentStatusError ? issue.getStatus()
                            : commentStatus;
                    failureType = failureTypeError ? issue.getCategory()
                            : failureType;
                    if (histories != null && histories.size() > 0)
                    {
                        IssueHistory history = (IssueHistory) histories.get(0);

                        if (history.reportedBy().equals(p_user.getUserId()))
                        {
                            if (StringUtil.isNotEmpty(comment))
                            {
                                IssuesVo vo = new IssuesVo();
                                issuesVos.add(vo);
                                vo.id = issue.getId();
                                vo.title = issue.getTitle();
                                vo.priority = issue.getPriority();
                                vo.commentStatus = commentStatus;
                                vo.failureType = failureType.trim();
                                vo.userId = p_user.getUserId();
                                vo.comment = comment;
                            }
                            else
                            {
                                updateCommentStatus(issue, commentStatus);
                            }
                        }
                        else
                        {
                            if (comment != null && !comment.equals(""))
                            {
                                commentManager.replyToIssue(issue.getId(),
                                        issue.getTitle(), issue.getPriority(),
                                        commentStatus, failureType.trim(),
                                        p_user.getUserId(), comment);
                            }
                            else
                            {
                                issue.setStatus(commentStatus);
                                HibernateUtil.saveOrUpdate(issue);
                            }
                        }
                    }
                }

            }

        }

        if (newIssues.size() > 0)
        {
            HibernateUtil.saveOrUpdate(newIssues);
        }

        if (issuesVos.size() > 0)
        {
            editIssues(issuesVos);
        }
    }

    private class IssuesVo
    {
        long id;
        String title;
        String priority;
        String commentStatus;
        String failureType;
        String userId;
        String comment;
    }

    // ih.reportedBy().equals(vo.userId)
    private void editIssues(List<IssuesVo> issuesVos)
    {
        IssueImpl issue = null;

        Session session = HibernateUtil.getSession();
        Transaction tx = HibernateUtil.getTransaction();

        Connection conn = null;
        PreparedStatement stmt = null;
        try
        {

            conn = DbUtil.getConnection();
            conn.setAutoCommit(false);

            String sqlUpdate = "update ISSUE_HISTORY set DESCRIPTION= ? ,"
                    + "REPORTED_DATE = ? "
                    + " Where REPORTED_BY = ? and REPORTED_DATE = ?";
            stmt = conn.prepareStatement(sqlUpdate);

            int batchUpdate = 0;
            for (IssuesVo vo : issuesVos)
            {
                issue = (IssueImpl) session.get(IssueImpl.class,
                        new Long(vo.id));

                issue.setTitle(vo.title);
                issue.setPriority(vo.priority);
                issue.setStatus(vo.commentStatus);
                issue.setCategory(vo.failureType);
                issue.setOverwrite(false);
                issue.setShare(false);

                IssueHistoryImpl ih = (IssueHistoryImpl) issue.getHistory()
                        .get(0);

                Date date = ih.dateReportedAsDate();
                Date currentDate = Calendar.getInstance().getTime();

                ih.dateReported(Calendar.getInstance().getTime());
                ih.setComment(vo.comment);
                session.saveOrUpdate(ih);

                stmt.setString(1, vo.comment);
                stmt.setDate(2, new java.sql.Date(currentDate.getTime()));
                stmt.setString(3, vo.userId);
                stmt.setDate(4, new java.sql.Date(date.getTime()));

                batchUpdate++;
                if (batchUpdate > DbUtil.BATCH_INSERT_UNIT)
                {
                    stmt.executeBatch();
                    batchUpdate = 0;
                }
            }

            if (batchUpdate > 0)
            {
                stmt.executeBatch();
            }

            HibernateUtil.commit(tx);
        }
        catch (Exception ex)
        {
            HibernateUtil.rollback(tx);
            CATEGORY.error("Failed to edit issue.", ex);
        }
        finally
        {
            // session.close();
            DbUtil.silentClose(stmt);
            if (conn != null)
            {
                try
                {
                    conn.commit();
                }
                catch (SQLException e)
                {
                    CATEGORY.error(e);
                }

                DbUtil.silentReturnConnection(conn);
            }
        }
    }

    private long getLocaleId(String language) throws Exception
    {
        String locale = null;
        long localeId = -1;
        int startIndex = language.indexOf("[");
        int endIndex = language.indexOf("]");
        if (startIndex != -1 && endIndex != -1)
        {
            locale = language.substring(startIndex + 1, endIndex);
            localeId = ServerProxy.getLocaleManager().getLocaleByString(locale)
                    .getId();
        }
        return localeId;
    }

    public String processXliff20(Reader p_reader, String p_fileName,
            User p_user, long p_ownerTaskId, Collection p_excludedItemTypes,
            String p_jmsQueueDestination)
    {
        loadListViewTextFile(p_reader, p_fileName, true);

        m_uploadPageData.setIsXliff20(true);

        List<Task> isUploadingTasks = new ArrayList<Task>();
        String errPage = null;
        String checkErrMsg = null;

        // for GBS-1939, for GBS-2829
        List<Long> taskIdsList = null;
        String taskId = m_uploadPageData.getTaskId();
        String[] taskIds = null;
        if (taskId.contains(","))
        {
            taskIds = taskId.split(",");
            taskIdsList = new ArrayList<Long>();
            for (String tid : taskIds)
            {
                Long isUploadingTaskId = Long.valueOf(tid);
                taskIdsList.add(isUploadingTaskId);
                // Update task status (Uploading)
                Task isUploadingTask = TaskHelper.updateTaskStatus(
                        isUploadingTaskId, UPLOAD_IN_PROGRESS);
                if (isUploadingTask != null)
                {
                    isUploadingTasks.add(isUploadingTask);
                    OfflineFileUploadStatus.addFileState(isUploadingTaskId,
                            p_fileName, "Running");
                }
            }
            m_uploadPageData.setTaskIds(taskIdsList);
        }
        else
        {
            Long isUploadingTaskId = Long.valueOf(taskId);
            // Update task status (Uploading)
            Task isUploadingTask = TaskHelper.updateTaskStatus(
                    isUploadingTaskId, UPLOAD_IN_PROGRESS);
            if (isUploadingTask != null)
            {
                isUploadingTasks.add(isUploadingTask);
                OfflineFileUploadStatus.addFileState(isUploadingTaskId,
                        p_fileName, "Running");
            }
        }

        // Fix for GBS-2191
        if (p_ownerTaskId == -1)
        {
            String ownerTaskId = taskIds != null ? taskIds[0] : taskId;
            p_ownerTaskId = Long.parseLong(ownerTaskId);
            m_uploadPageData.setTaskId(ownerTaskId);

            Task task = getTask(p_ownerTaskId);
            if ((errPage = checkTask(p_user, task, p_fileName)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }
            p_excludedItemTypes = getExcludedItemTypes(task);
        }

        // check all tasks for combined download files
        if (isUploadingTasks != null)
        {
            for (Task task : isUploadingTasks)
            {
                if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                {
                    completeUploadingTask(isUploadingTasks);
                    return errPage;
                }
            }
        }

        if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_reader)) != null)
        {
            completeUploadingTask(isUploadingTasks);
            return errPage;
        }

        // Verify if upload file is a consolated file
        if (m_uploadPageData.getPageId().indexOf(",") > 0)
            m_uploadPageData.setConsolated(true);

        if ((errPage = postLoadInit(p_ownerTaskId,
                m_uploadPageData.getTaskId(), p_excludedItemTypes,
                DOWNLOAD_FILE_FORMAT_TXT)) != null)
        {
            completeUploadingTask(isUploadingTasks);
            return errPage;
        }

        // Check uploaded page for errors. If there are no errors - save it
        if ((errPage = checkPage(p_user, p_fileName)) != null)
        {
            // do not return here
            checkErrMsg = errPage;
        }

        // do not save page if iscontinue = n for internal tag error
        if (this.status.getIsContinue() != null
                && this.status.getIsContinue().equals(Boolean.FALSE))
        {
            // do not return here
            if (checkErrMsg == null)
                checkErrMsg = "IsContinue=fasle";
        }

        if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                p_jmsQueueDestination, p_user, p_fileName, isUploadingTasks)) != null)
        {
            completeUploadingTask(isUploadingTasks);
        }

        return getErrorMsg(checkErrMsg, errPage);
    }

    /**
     * Processes a single upload page (Unicode text file, RTF list view).
     * 
     * If the page contains errors, an HTML error page is returned as a string.
     * If no errors occur, segments are converted back to GXML and written to
     * database.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_rtfDoc
     *            an RTF DOM representing the uploaded content.
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId
     *            - the task Id that the upload page must match.
     * @param p_fileName
     *            - The name of the file to be uploaded. Used for email
     *            notification.
     * @param p_jmsQueueDestination
     *            - The JMS queue used for performing the save and indexing
     *            process in the background.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
    @SuppressWarnings("rawtypes")
    public String process_GS_WRAPPED_UNICODE_TEXT(RtfDocument p_rtfDoc,
            User p_user, long p_ownerTaskId, String p_fileName,
            Collection p_excludedItemTypes, String p_jmsQueueDestination)
    {
        // for GBS-1939
        List<Task> isUploadingTasks = new ArrayList<Task>();

        try
        {
            // m_referencePageData = new OfflinePageData();
            // so getPage() will be cleared if errors occur
            m_uploadPageData = new OfflinePageData();

            String errPage = null;
            String checkErrMsg = null;
            if ((errPage = setLocale(p_user)) != null)
            {
                return errPage;
            }
            // load the upload file
            if ((errPage = load_GS_WRAPPED_UNICODE_TEXT_File(p_rtfDoc,
                    p_fileName)) != null)
            {
                CATEGORY.error("process_GS_WRAPPED_UNICODE_TEXT: "
                        + "Unable to load the upload-file.");

                return errPage;
            }

            // for GBS-1939, for GBS-2829
            List<Long> taskIdsList = null;
            String taskId = m_uploadPageData.getTaskId();
            String[] taskIds = null;
            if (taskId.contains(","))
            {
                taskIds = taskId.split(",");
                taskIdsList = new ArrayList<Long>();
                for (String tid : taskIds)
                {
                    Long isUploadingTaskId = Long.valueOf(tid);
                    taskIdsList.add(isUploadingTaskId);
                    // Update task status (Uploading)
                    Task isUploadingTask = TaskHelper.updateTaskStatus(
                            isUploadingTaskId, UPLOAD_IN_PROGRESS);
                    if (isUploadingTask != null)
                    {
                        isUploadingTasks.add(isUploadingTask);
                        OfflineFileUploadStatus.addFileState(isUploadingTaskId,
                                p_fileName, "Running");
                    }
                }
                m_uploadPageData.setTaskIds(taskIdsList);
            }
            else
            {
                Long isUploadingTaskId = Long.valueOf(taskId);
                // Update task status (Uploading)
                Task isUploadingTask = TaskHelper.updateTaskStatus(
                        isUploadingTaskId, UPLOAD_IN_PROGRESS);
                if (isUploadingTask != null)
                {
                    isUploadingTasks.add(isUploadingTask);
                    OfflineFileUploadStatus.addFileState(isUploadingTaskId,
                            p_fileName, "Running");
                }
            }

            // Fix for GBS-2191
            if (p_ownerTaskId == -1)
            {
                String ownerTaskId = taskIds != null ? taskIds[0] : taskId;
                p_ownerTaskId = Long.parseLong(ownerTaskId);
                m_uploadPageData.setTaskId(ownerTaskId);

                Task task = getTask(p_ownerTaskId);
                if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                {
                    completeUploadingTask(isUploadingTasks);
                    return errPage;
                }
                p_excludedItemTypes = getExcludedItemTypes(task);
            }

            // check all tasks for combined download files
            if (isUploadingTasks != null)
            {
                for (Task task : isUploadingTasks)
                {
                    if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                    {
                        completeUploadingTask(isUploadingTasks);
                        return errPage;
                    }
                }
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_rtfDoc)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }

            // Verify if upload file is a consolated file
            if (m_uploadPageData.getPageId().indexOf(",") > 0)
                m_uploadPageData.setConsolated(true);

            if ((errPage = postLoadInit(p_ownerTaskId,
                    m_uploadPageData.getTaskId(), p_excludedItemTypes,
                    DOWNLOAD_FILE_FORMAT_TXT)) != null)
            {
                completeUploadingTask(isUploadingTasks);
                return errPage;
            }

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkPage(p_user, p_fileName)) != null)
            {
                // do not return here
                checkErrMsg = errPage;
            }

            // do not save page if iscontinue = n for internal tag error
            if (this.status.getIsContinue() != null
                    && this.status.getIsContinue().equals(Boolean.FALSE))
            {
                // do not return here
                if (checkErrMsg == null)
                    checkErrMsg = "IsContinue=fasle";
            }

            if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                    p_jmsQueueDestination, p_user, p_fileName, isUploadingTasks)) != null)
            {
                completeUploadingTask(isUploadingTasks);
            }

            return getErrorMsg(checkErrMsg, errPage);
        }
        finally
        {

        }
    }

    // Connect two error message into one to return to UI.
    private String getErrorMsg(String errMsg1, String errMsg2)
    {
        StringBuffer err = new StringBuffer();
        if (errMsg1 != null)
            err.append(errMsg1);
        if (err.length() > 0 && errMsg2 != null)
            err.append("<br>");
        if (errMsg2 != null)
            err.append(errMsg2);
        return err.toString();
    }

    /**
     * Update tasks status to finished.
     */
    private void completeUploadingTask(List<Task> isUploadingTasks)
    {
        boolean isReportUploaded = false;
        TaskHelper.updateTaskStatus(isUploadingTasks, UPLOAD_DONE,
                isReportUploaded);
    }

    /**
     * Processes an unextracted file.
     * 
     * The name of an Unextracted file must adhere to the following syntax:
     * 
     * [FILENAME]_[FILEID[FILETYPE]]_[TASKID][EXTENSION]
     * 
     * Where: FILENAME = optional (user defined) filename '_' = a mandatory dash
     * FILEID = the System4 generated id for this unextracted file FILETYPE =
     * the target type: 'P' == Primary, 'S' == Secondary '_' = a mandatory dash
     * TASKID = the task Id that this file was downloaded for EXTENSION =
     * optional (user defined) extension
     * 
     * Example: MyFileName_1001P_298.html
     * 
     * If the filename contains errors, an HTML error page is returned. If no
     * errors occur, the file is sent to the NativeFileManager to be saved.
     * 
     * NOTE: All exceptions from this method are routed to the offline error
     * report.
     * 
     * @param p_tmpFile
     *            the upload temp file.
     * @param p_user
     *            the user object used for getting email and locale info.
     * @param p_ownerTaskId
     *            - the task Id that the upload page must match.
     * @param p_fileName
     *            - The name of the file to be uploaded. Used for email
     *            notification.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error page is returned.
     */
    public String doUnextractedFileUpload(File p_tmpFile, User p_user,
            long p_ownerTaskId, String p_fileName)
    {
        GlobalSightLocale sourceLocale;
        GlobalSightLocale targetLocale;
        long companyId;

        String errPage;
        // Set locale
        if ((errPage = setLocale(p_user)) != null)
        {
            return errPage;
        }
        // Load:
        // NOTE: There is no formal load into an OfflinePageData since this is
        // am unextracted file. The load consists of verify the filename
        // syntax and extracting the routing ids from the filename.
        if (parseUnextractedFilename(p_fileName))
        {
            if ((errPage = setLocale(p_user)) != null)
            {
                return errPage;
            }

            if (p_ownerTaskId == -1)
            {
                p_ownerTaskId = Long.parseLong(m_unextractedFileTaskId);
            }

            OfflineFileUploadStatus.addFileState(p_ownerTaskId, p_fileName,
                    "Running");

            Task task = getTask(p_ownerTaskId);
            if ((errPage = checkTask(p_user, task, p_fileName)) != null)
            {
                return errPage;
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_tmpFile)) != null)
            {
                return errPage;
            }
        }
        else
        {
            String[] args =
            { p_fileName };
            String errMsg = MessageFormat.format(
                    m_messages.getString("InvalidUnextractedFilename"),
                    (Object[]) args);

            CATEGORY.info(errMsg);

            m_errWriter.addFileErrorMsg(EditUtil.encodeHtmlEntities(errMsg));
            return m_errWriter.buildPage().toString();

        }

        // Perform post load checks specific to unextracted files:

        // verify the filename taskID is the same as the ownerTaskId
        if ((errPage = confirmValidFileTaskId(p_ownerTaskId,
                m_unextractedFileTaskId)) != null)
        {
            return errPage;
        }

        // verify that an stf or primary target file by this id exists
        // in this workflow
        if (!isValidUnextractedFileId(m_unextractedFileTaskId,
                m_unextractedFileId, m_unextractedFileType))
        {
            String args[] =
            { m_unextractedFileId };
            String errMsg = MessageFormat.format(
                    m_messages.getString("InvalidUnextractedFileId"),
                    (Object[]) args);

            CATEGORY.info(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        // Save file:
        // Handles an un-extracted primary target or a secondary target file
        try
        {
            if (m_unextractedFileType.equalsIgnoreCase(SECONDARY_TYPE))
            {
                SecondaryTargetFileMgr stfMgr = ServerProxy
                        .getSecondaryTargetFileManager();
                SecondaryTargetFile stf = stfMgr.getSecondaryTargetFile(Long
                        .parseLong(m_unextractedFileId));
                companyId = stf.getWorkflow().getCompanyId();
                sourceLocale = stf.getWorkflow().getJob().getSourceLocale();
                targetLocale = stf.getWorkflow().getTargetLocale();

                // re-assemble upload filename (without IDs)
                StringBuffer sb = new StringBuffer();
                sb.append(m_unextractedFilenameHead);
                sb.append(m_unextractedFilenameTail);

                ServerProxy.getNativeFileManager().save(stf, p_tmpFile, p_user,
                        sb.toString());
            }
            else
            // if m_unextractedFileType.equalsIgnoreCase(PRIMARY_TYPE)
            {
                PageManager pm = ServerProxy.getPageManager();
                TargetPage tp = pm.getTargetPage(Long
                        .parseLong(m_unextractedFileId));
                companyId = tp.getWorkflowInstance().getCompanyId();
                // assumes that this file contains an un-extraced file -
                // otherwise
                // this place in the code wouldn't have been reached
                UnextractedFile uf = (UnextractedFile) tp.getPrimaryFile();

                sourceLocale = tp.getWorkflowInstance().getJob()
                        .getSourceLocale();
                targetLocale = tp.getWorkflowInstance().getTargetLocale();

                // verify the upload extension
                if (!compareExtension(uf.getStoragePath(),
                        m_unextractedFilenameExt))
                {
                    String[] args =
                    { uf.getStoragePath().toLowerCase(),
                            m_unextractedFilenameExt.toLowerCase() };
                    String errMsg = MessageFormat.format(
                            m_messages.getString("ContentMismatch"),
                            (Object[]) args);
                    m_errWriter.addSystemErrorMsg(errMsg);
                    return m_errWriter.buildPage().toString();
                }
                else
                {
                    ServerProxy.getNativeFileManager().save(uf, p_tmpFile,
                            p_user);
                    // Persist the updated unextracted file info.
                    // The user, modify date and file length was updated in the
                    // "save" method.
                    pm.updateUnextractedFileInfo(tp);
                }
            }

            // Send success e-mail:
            // We only need to do this to be consistant with extracted uploads.
            // For an extracted upload, we need the user to wait till the Tuvs
            // are saved and indexed (via JMS) before finishing the task.
            // Since we do not use JMS here, there is need to send a failure
            // e-mail, they will get the Error page in the UI right away.
            String localePair = OfflineEditHelper.localePair(sourceLocale,
                    targetLocale, m_uiLocale);
            OfflineEditHelper.notifyUser(p_user, p_fileName, localePair,
                    OfflineEditHelper.UPLOAD_SUCCESSFUL_SUBJECT,
                    OfflineEditHelper.UPLOAD_SUCCESSFUL_MESSAGE,
                    String.valueOf(companyId));
        }
        catch (Exception ex)
        {
            CATEGORY.error("Unable to save un-extracted file:", ex);
            m_errWriter.addSystemErrorMsg(ex.toString());
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    //
    // Private methods
    //

    /**
     * Loads the upload file into an OfflinePageData object.
     * 
     * @param p_reader
     *            a stream opened on the upload file.
     * @param p_keepIssues
     *            when an OfflinePageData object is called *twice* to load data,
     *            this parameter allows to keep issues read in the first run
     *            (the second run normally clears the entire object). This is
     *            necessary for RTF list view which first parses the RTF, then
     *            loads the textual content as list view text file.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error report page is returned.
     */
    public String loadListViewTextFile(Reader p_reader, String p_fileName,
            boolean p_keepIssues)
    {
        if (m_uploadPageData == null)
        {
            m_uploadPageData = new OfflinePageData();
            m_referencePageDatas = new ArrayList<PageData>();
        }

        try
        {
            p_reader.mark(0);
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
        }

        String errPage = null;

        // Set the linefeed normalization sequence.
        if ((errPage = getLFNormalizationSequence()) != null)
        {
            return errPage;
        }

        // filter some text
        Reader new_reader = null;
        try
        {
            StringBuffer content = new StringBuffer();
            BufferedReader br = new BufferedReader(p_reader);
            String line = br.readLine();
            String previousLine = null;
            while (line != null)
            {
                boolean ignoreThisLine = line.startsWith(SEGMENT_PAGE_NAME_KEY)
                        || line.startsWith(SEGMENT_FILE_PATH_KEY)
                        || line.startsWith(HEADER_JOB_NAME)
                        || line.startsWith(HEADER_JOB_ID)
                        || line.startsWith(GS_TOOLKIT_FORMAT)
                        || line.startsWith(SEGMENT_SID_KEY)
                        || line.startsWith(SEGMENT_XLF_TARGET_STATE_KEY)
                        || line.startsWith(SEGMENT_INCONTEXT_MATCH_KEY)
                        || line.startsWith(SEGMENT_TM_PROFILE_KEY)
                        || line.startsWith(SEGMENT_TERMBASE_KEY)
                        || line.startsWith(HEADER_POPULATE_100_SEGMENTS);
                if (!ignoreThisLine)
                {
                    content.append(line).append("\r\n");
                }

                // check if it is omegat
                if (ignoreThisLine && line.startsWith(GS_TOOLKIT_FORMAT))
                {
                    int index = line.indexOf(":");
                    String f = index > 0 ? line.substring(index + 1).trim()
                            : "xliff";
                    m_uploadPageData.setIsOmegaT("omegat".equalsIgnoreCase(f));
                    m_uploadPageData.setIsXliff("xliff".equalsIgnoreCase(f));
                }

                // (GBS-3711) Store "state" attribute value of XLF target
                // section.
                if (ignoreThisLine
                        && line.startsWith(SEGMENT_XLF_TARGET_STATE_KEY))
                {
                    int index = line.indexOf(":");
                    if (index > 0)
                    {
                        String state = line.substring(index + 1).trim();
                        String tuId = previousLine.substring(2);
                        m_uploadPageData.addXlfTargetState(tuId, state);
                    }
                }
                // GBS-3825
                if (ignoreThisLine
                        && line.startsWith(HEADER_POPULATE_100_SEGMENTS))
                {
                    int index = line.indexOf(":");
                    if (index > 0)
                    {
                        String isPopulate100 = line.substring(index + 1).trim();
                        m_uploadPageData.setPopulate100("yes"
                                .equalsIgnoreCase(isPopulate100));
                    }
                }
                previousLine = line;
                line = br.readLine();
            }

            new_reader = new StringReader(content.toString());

            br.close();
        }
        catch (Exception e)
        {
            new_reader = p_reader;
        }

        // Read the upload file into an OfflinePageData object.
        try
        {
            m_errWriter.setFileName(p_fileName);
            m_uploadPageData.setLoadConversionLineBreak(m_normalizedLB);
            m_uploadPageData.loadOfflineTextFile(new_reader, false);
            Vector<OfflineSegmentData> list = m_uploadPageData.getSegmentList();
            for (OfflineSegmentData object : list)
            {
                String targetText = object.getDisplayTargetText();
                targetText = StringUtil.replace(targetText, OfflineConstants.PONUD_SIGN, "#");
                object.setDisplayTargetText(targetText);
            }
            
            // set err writer's page, task and job ids
            m_errWriter.processOfflinePageData(m_uploadPageData);
        }
        catch (Throwable ex)
        {
            try
            {
                p_reader.reset();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            String errMsg = null;
            boolean noSegments = false;
            if (ex instanceof ParseException)
            {
                ParseException pe = (ParseException) ex;
                int[][] expected = pe.expectedTokenSequences;
                if (expected != null && expected.length == 2
                        && expected[0].length == 1 && expected[1].length == 1
                        && expected[0][0] == 8 && expected[1][0] == 9)
                {
                    Token current = pe.currentToken;
                    if (current != null && current.next != null
                            && current.next.kind == 17)
                    {
                        noSegments = true;
                    }
                }
            }

            // check if this is empty
            if (noSegments)
            {
                errMsg = m_messages.getString("NoSegmentsInFile");
            }
            else
            {
                String exMsg = ex.getMessage();
                String args[] =
                { EditUtil.encodeHtmlEntities(exMsg) };
                bindErrMsg(args, p_reader);

                errMsg = MessageFormat.format(
                        m_messages.getString("FormatTwoLoadError"),
                        (Object[]) args);

                CATEGORY.error(errMsg);
            }

            m_errWriter.addFileErrorMsg(errMsg);
            m_errWriter.processOfflinePageData(m_uploadPageData);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    /**
     * Loads the upload file into an OfflinePageData object.
     * 
     * @param p_rtfDoc
     *            the rtf DOM.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error report page is returned.
     */
    private String load_GS_PARAVIEW_1_File(RtfDocument p_rtfDoc,
            String p_fileName)
    {
        String errPage = null;

        // Set the linefeed normalization sequence.
        if ((errPage = getLFNormalizationSequence()) != null)
        {
            return errPage;
        }

        // Load file into OfflinePageData.
        try
        {
            m_errWriter.setFileName(p_fileName);
            m_uploadPageData.setLoadConversionLineBreak(m_normalizedLB);
            m_uploadPageData.loadParaViewOneWorkFile(p_rtfDoc);

            // set err writer's page, task and job ids
            m_errWriter.processOfflinePageData(m_uploadPageData);
        }
        catch (Throwable ex)
        {
            String msg = "";

            if (ex instanceof GeneralException)
            {
                msg = ((GeneralException) ex)
                        .getMessage(m_uiLocale.getLocale());
            }
            else
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                msg = sw.toString();
            }

            String args[] =
            { EditUtil.encodeHtmlEntities(msg) };
            String errMsg = MessageFormat.format(
                    m_messages.getString("FormatParaViewOneLoadError"),
                    (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    /**
     * Loads the wrapped text file into an OfflinePageData object. Loads RTF
     * list view files.
     * 
     * @param p_rtfDoc
     *            the RTF DOM.
     * @return if there are no errors, null is returned. If there are errors, a
     *         fully formed HTML error report page is returned.
     */
    private String load_GS_WRAPPED_UNICODE_TEXT_File(RtfDocument p_rtfDoc,
            String p_fileName)
    {
        // ---------------------------------------------------------------
        // NOTE:
        // ---------------------------------------------------------------
        // We are half way to direct RTF reading for list-view. For
        // now we still get segments the old way (by getting a plain
        // text dump from the Rtf reader and passing that to the
        // original plain text parser). However, we do now read/load
        // segment annotations with the new RTF parser.
        // ---------------------------------------------------------------

        // -----------------------------------
        // Load comments (eventually loadListViewOneWorkFile should
        // load the entire file)
        // -----------------------------------
        try
        {
            m_errWriter.setFileName(p_fileName);
            m_uploadPageData.setLoadConversionLineBreak(m_normalizedLB);
            m_uploadPageData.loadListViewOneWorkFile(p_rtfDoc);

            // set err writer's page, task and job ids
            m_errWriter.processOfflinePageData(m_uploadPageData);
        }
        catch (Throwable ex)
        {
            String msg = "";

            if (ex instanceof GeneralException)
            {
                msg = ((GeneralException) ex)
                        .getMessage(m_uiLocale.getLocale());
            }
            else
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                msg = sw.toString();
            }

            String args[] =
            { EditUtil.encodeHtmlEntities(msg) };
            String errMsg = MessageFormat
                    .format(m_messages.getString("FormatTwoLoadError"),
                            (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        // -----------------------------------
        // Now load segments the old way.
        // Eventually loadListViewOneWorkFile (above)
        // should load the entire file.
        // -----------------------------------

        StringReader p_reader = null;
        String c = RtfAPI.getText(p_rtfDoc);
        try
        {
            StringBuffer content = new StringBuffer();
            StringReader sreader = new StringReader(c);
            BufferedReader br = new BufferedReader(sreader);
            String line = br.readLine();

            while (line != null)
            {
                boolean ignoreThisLine = line.startsWith(SEGMENT_PAGE_NAME_KEY)
                        || line.startsWith(SEGMENT_FILE_PATH_KEY)
                        || line.startsWith(HEADER_JOB_NAME)
                        || line.startsWith(HEADER_JOB_ID);
                if (!ignoreThisLine)
                {
                    content.append(line).append("\r\n");
                }

                line = br.readLine();
            }

            sreader.close();
            br.close();

            p_reader = new StringReader(content.toString());
        }
        catch (Exception e)
        {
            p_reader = new StringReader(c);
        }

        return loadListViewTextFile(p_reader, p_fileName, true);
    }

    private boolean isValidUnextractedFileId(String p_unextractedTaskId,
            String p_unextractedFileId, String p_unextractedFileType)
    {
        boolean found = false;

        try
        {
            TaskManager taskMgr = ServerProxy.getTaskManager();
            Task task = taskMgr.getTask(Long.parseLong(p_unextractedTaskId));

            if (p_unextractedFileType.equalsIgnoreCase(SECONDARY_TYPE))
            {
                for (SecondaryTargetFile stf : task.getWorkflow()
                        .getSecondaryTargetFiles())
                {
                    found = p_unextractedFileId.equals(stf.getIdAsLong()
                            .toString());
                    if (found)
                        break;
                }
            }
            else
            // assume that it is a primary target file type - PRIMARY_TYPE
            {
                List unextractedTargets = task.getWorkflow().getTargetPages(
                        UnextractedFile.UNEXTRACTED_FILE);

                for (int i = 0; !found && i < unextractedTargets.size(); i++)
                {
                    TargetPage tp = (TargetPage) unextractedTargets.get(i);
                    found = p_unextractedFileId.equals(tp.getIdAsLong()
                            .toString());
                    if (found)
                        break;
                }
            }
        }
        catch (Exception ex)
        {
            CATEGORY.error("Problem validating un-extracted file id", ex);
        }

        return found;
    }

    private boolean parseUnextractedFilename(String p_name)
    {
        boolean found = false;

        RE fname = new RE(RE_UNEXTRACTED_FILE_FNAME);
        RE fnameExt = new RE(RE_UNEXTRACTED_FILE_FNAME_EXT);

        if (fname.match(p_name))
        {
            found = true;

            m_unextractedFilenameHead = fname
                    .getParen(RE_UNEXTRACTED_FILE_HEAD);
            m_unextractedFileId = fname.getParen(RE_UNEXTRACTED_FILE_ID);
            m_unextractedFileType = fname.getParen(RE_UNEXTRACTED_FILE_TYPE);
            m_unextractedFileTaskId = fname
                    .getParen(RE_UNEXTRACTED_FILE_TASK_ID);
            m_unextractedFilenameTail = fname
                    .getParen(RE_UNEXTRACTED_FILE_TAIL);

            if (fnameExt.match(m_unextractedFilenameTail))
            {
                m_unextractedFilenameExt = fnameExt
                        .getParen(RE_UNEXTRACTED_FILE_EXT);
            }
        }

        return found;
    }

    private boolean compareExtension(String p_path, String p_newExt)
    {
        // get current extension from current path
        int idx = p_path.lastIndexOf(".");
        String extension = idx >= 0 ? p_path.substring(idx + 1) : "";

        String newExt = p_newExt != null ? p_newExt : "";

        // validate extension
        // Note: this does not prevent anyone from renaming files to get
        // around this. For instance, you could rename a_100S_200.doc
        // to a_100S_200.html and then upload over the html file.
        // We do not try to detect the actual file format (by content)
        return extension.toLowerCase().equals(newExt.toLowerCase());
    }

    private boolean getAdjustWhitespaceParam(String p_user)
    {
        UserParameter param = getUserParameter(p_user,
                UserParamNames.EDITOR_AUTO_ADJUST_WHITESPACE);

        if (param != null && !param.getValue().equals("0"))
        {
            return true;
        }

        return false;
    }

    private UserParameter getUserParameter(String p_user, String p_param)
    {
        try
        {
            return ServerProxy.getUserParameterManager().getUserParameter(
                    p_user, p_param);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private String setLocale(User p_user)
    {
        try
        {
            m_uiLocale = ServerProxy.getLocaleManager().getLocaleByString(
                    p_user.getDefaultUILocale());
            m_errWriter.setLocale(m_uiLocale.getLocale());
            m_messages = ResourceBundle.getBundle(getClass().getName(),
                    m_uiLocale.getLocale());
        }
        catch (Exception ex)
        {
            CATEGORY.error(
                    "Upload error, unable to set locale to "
                            + p_user.getDefaultUILocale(), ex);

            m_errWriter.addFileErrorMsg(ex.toString());
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    private String confirmReader(Reader p_reader)
    {
        // confirm we have a stream
        if (p_reader == null)
        {
            String args[] =
            { "Null reference to the upload-input stream." };

            String errMsg = MessageFormat.format(
                    m_messages.getString("IOReadError"), (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    /**
     * Confirm we have some content.
     * 
     * Note: submitting an invalid path for the browser to read from (in the UI)
     * results in zero bytes being set as content in the request.
     */
    private String confirmInputBytes(File p_tmpFile)
    {
        if (p_tmpFile == null || !p_tmpFile.exists() || p_tmpFile.length() == 0)
        {
            String errMsg = MessageFormat.format(
                    m_messages.getString("NoFileContent"), (Object[]) null);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    private String confirmRtfDoc(RtfDocument p_rtfDoc)
    {
        // confirm we have a stream
        if (p_rtfDoc == null)
        {
            String args[] =
            { "Null reference to the upload-input stream." };

            String errMsg = MessageFormat.format(
                    m_messages.getString("IOReadError"), (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    private String confirmValidUserTaskId(User p_user, long p_ownerTaskId)
    {
        try
        {
            EditCommonHelper.verifyTask(p_user.getUserId(),
                    Long.toString(p_ownerTaskId));
        }
        catch (EnvoyServletException ex)
        {
            String errMsg = m_messages.getString("TaskDeactivated");

            CATEGORY.error(errMsg, ex);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    /**
     * The upload file TaskId is extracted from our file header or (as in the
     * case of unextracted files) from the file name.
     */
    private String confirmValidFileTaskId(long p_ownerTaskId,
            String p_fileTaskId)
    {
        String p_ownerTaskIdAsString = Long.toString(p_ownerTaskId);

        if (!p_ownerTaskIdAsString.equals(p_fileTaskId))
        {
            String args[] =
            { p_fileTaskId, p_ownerTaskIdAsString };
            String errMsg = MessageFormat.format(
                    m_messages.getString("TaskIdMatchError"), (Object[]) args);

            CATEGORY.info(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    /**
     * Loads the reference page by unmerging it and then remerging according to
     * the merge directives contained in m_uploadPageData.
     */
    private String loadReferencePageData(long p_ownerTaskId,
            Collection p_excludedItemTypes, int p_uploadFileFormat)
    {
        // load reference data from the DB
        try
        {
            if (!m_uploadPageData.isConsolated())
            {
                m_referencePageData = m_uploadPageSaver
                        .initializeAndGetReferencePage(m_uploadPageData,
                                p_excludedItemTypes, p_uploadFileFormat,
                                m_tempFileName);
                m_referencePageDatas = new ArrayList<PageData>();
                m_referencePageDatas.add(m_referencePageData);
            }
            else
            {
                m_referencePageDatas = m_uploadPageSaver
                        .initializeAndGetReferencePages(m_uploadPageData,
                                p_excludedItemTypes, p_uploadFileFormat);
            }
        }
        catch (UploadPageSaverException ex)
        {
            CATEGORY.error("Unable to load extracted file reference data:", ex);

            String args[] =
            { AmbassadorDwUpConstants.LABEL_PAGEID,
                    AmbassadorDwUpConstants.LABEL_SRCLOCALE,
                    AmbassadorDwUpConstants.LABEL_TRGLOCALE,
                    ex.getStackTraceString() };

            String errMsg = MessageFormat.format(
                    m_messages.getString("ReferencePageLoadError"),
                    (Object[]) args);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    private String createErrorChecker()
    {
        // Create offline error checker
        try
        {
            m_errChecker = new OfflinePtagErrorChecker(m_errWriter);
            m_errChecker.setStatus(status);
        }
        catch (AmbassadorDwUpException ex)
        {
            CATEGORY.error("Unable to instantiate OfflinePtagErrorChecker", ex);

            m_errWriter
                    .addSystemErrorMsg("Unable to instantiate OfflinePtagErrorChecker\n"
                            + ex.getStackTraceString());

            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    /** Initialize using a Reader. */
    private String preLoadInit(long p_ownerTaskId, User p_user, Reader p_reader)
    {
        String errPage = null;

        if ((errPage = preLoadInit(p_ownerTaskId, p_user)) != null)
        {
            return errPage;
        }
        else if ((errPage = confirmReader(p_reader)) != null)
        {
            return errPage;
        }

        return null;
    }

    /** Initialize using a File. */
    private String preLoadInit(long p_ownerTaskId, User p_user, File p_tmpFile)
    {
        String errPage = null;

        if ((errPage = preLoadInit(p_ownerTaskId, p_user)) != null)
        {
            return errPage;
        }
        else if ((errPage = confirmInputBytes(p_tmpFile)) != null)
        {
            return errPage;
        }

        return null;
    }

    /** Initialize using a RtfDocument. */
    private String preLoadInit(long p_ownerTaskId, User p_user,
            RtfDocument p_rtfDoc)
    {
        String errPage = null;

        if ((errPage = preLoadInit(p_ownerTaskId, p_user)) != null)
        {
            return errPage;
        }
        else if ((errPage = confirmRtfDoc(p_rtfDoc)) != null)
        {
            return errPage;
        }

        return null;
    }

    /** Common initialization checks. */
    private String preLoadInit(long p_ownerTaskId, User p_user)
    {
        String errPage = null;
        if ((errPage = confirmValidUserTaskId(p_user, p_ownerTaskId)) != null)
        {
            return errPage;
        }

        return null;
    }

    private String postLoadInit(long p_ownerTaskId, String p_fileTaskId,
            Collection p_excludedItemTypes, int p_uploadFileFormat)
    {
        String errPage = null;

        if ((errPage = confirmValidFileTaskId(p_ownerTaskId,
                m_uploadPageData.getTaskId())) != null)
        {
            return errPage;
        }
        else if ((errPage = loadReferencePageData(p_ownerTaskId,
                p_excludedItemTypes, p_uploadFileFormat)) != null)
        {
            return errPage;
        }
        else if ((errPage = createErrorChecker()) != null)
        {
            return errPage;
        }

        return null;
    }

    private String getLFNormalizationSequence()
    {
        String errMsg = null;
        String rslt = null;

        // Get the linefeed normalization sequence
        // NOTE1: We must normalize during parsing so the parser can remove
        // newlines we added for formating.
        // NOTE: processPage() takes care of container-format linefeeds.
        try
        {
            ResourceBundle res = ResourceBundle
                    .getBundle(AmbassadorDwUpConstants.OFFLINE_CONFIG_PROPERTY);
            m_normalizedLB = res
                    .getString(AmbassadorDwUpConstants.OFFLINE_CONFIG_KEY_LB_NORMALIZATION);
        }
        catch (Throwable ex)
        {
            String args[] =
            { AmbassadorDwUpConstants.OFFLINE_CONFIG_PROPERTY };
            errMsg = MessageFormat.format(
                    m_messages.getString("ResourceFileLoadError"),
                    (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            rslt = m_errWriter.buildPage().toString();
        }

        // Validate the linebreak normalization sequence
        if (!m_normalizedLB.equals("\n") && !m_normalizedLB.equals("\r")
                && !m_normalizedLB.equals("\r\n"))
        {
            errMsg = m_messages.getString("LineBreakNormalizationError");

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            rslt = m_errWriter.buildPage().toString();
        }

        return rslt;
    }

    private String save(OfflinePageData m_uploadPageData,
            ArrayList<PageData> m_refPageDatas, String p_jmsQueueDestination,
            User p_user, String p_fileName, List<Task> p_isUploadingTasks)
    {
        try
        {
            m_uploadPageSaver.savePageToDb(m_uploadPageData, m_refPageDatas,
                    p_jmsQueueDestination, p_user, p_fileName,
                    p_isUploadingTasks);
        }
        catch (UploadPageSaverException ex)
        {
            CATEGORY.error("Unable to save page", ex);

            m_errWriter.addSystemErrorMsg("Unable to save page:\n"
                    + ex.getStackTraceString());

            return m_errWriter.buildPage().toString();
        }
        catch (Throwable ex)
        {
            CATEGORY.error("Unable to save page", ex);

            m_errWriter.addSystemErrorMsg("Unable to save page:\n"
                    + ex.toString());

            return m_errWriter.buildPage().toString();
        }

        return null;
    }

    private String checkPage(User p_user, String fileName)
    {
        String errPage = null;

        m_errChecker.setFileName(fileName);
        boolean adjustWS = getAdjustWhitespaceParam(p_user.getUserId());
        if (!m_uploadPageData.isConsolated())
        {
            ArrayList<PageData> pageDatas = new ArrayList<PageData>();
            pageDatas.add(m_referencePageData);
            errPage = m_errChecker.check(pageDatas, m_uploadPageData, adjustWS);
        }
        else
            errPage = m_errChecker.check(m_referencePageDatas,
                    m_uploadPageData, adjustWS);

        return errPage;
    }

    private String checkReportPage(User p_user, long p_jobId) throws Exception
    {
        String errPage = null;

        boolean adjustWS = getAdjustWhitespaceParam(p_user.getUserId());
        if ((errPage = m_errChecker.checkAndSave(segId2RequiredTranslation,
                adjustWS, reportTargetLocaleId, p_user, p_jobId)) != null)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Page failed error checking. Returning error results");
            }

            return errPage;
        }
        return null;
    }

    private String addLinesErrMsg(Reader p_reader, int current_line)
    {
        StringBuffer string_buffer = new StringBuffer();
        // Print the message between start_line and end_line.
        int start_line = (current_line - 5) >= 0 ? current_line - 5 : 0;
        int end_line = current_line + 5;
        String line;

        try
        {
            BufferedReader reader = new BufferedReader(p_reader);
            for (int line_count = 1; (line_count <= end_line)
                    && (line = reader.readLine()) != null; line_count++)
            {
                if ((line_count >= start_line))
                {
                    string_buffer.append(line_count < 10 ? "0" + line_count
                            : line_count);
                    string_buffer.append("\t");
                    string_buffer.append(line);
                    string_buffer.append("\n");
                }
            }
        }
        catch (IOException e)
        {
            CATEGORY.error("addLinesErrMsg error", e);
        }

        return string_buffer.toString();
    }

    // Gets Error Line Number from head_err_msg.
    private int getErrLine(String head_err_msg)
    {
        int line = 0;
        String lineMsg = " at line ";
        int start = head_err_msg.indexOf(lineMsg) + lineMsg.length();
        String err_line = "";

        for (int i = start; i < head_err_msg.length(); i++)
        {
            String temp = "" + head_err_msg.charAt(i);
            if (temp.matches("\\d"))
            {
                err_line += temp;
            }
            else if (err_line.length() > 0)
            {
                break;
            }
        }

        try
        {
            line = Integer.parseInt(err_line);
        }
        catch (Exception e)
        {
            line = 0;
        }
        return line;
    }

    private void bindErrMsg(String[] args, Reader p_reader)
    {
        String head_err_msg = args[0];
        int current_line = getErrLine(head_err_msg);
        String lines_err_msg = addLinesErrMsg(p_reader, current_line);
        if (!("".equals(lines_err_msg.trim())))
        {
            String oldArg = args[0];
            String newArg = "Encountered incorrect text at line "
                    + current_line + ":\n";
            newArg += lines_err_msg;
            newArg += "\n\n";

            String expecting = null;
            int index = oldArg.indexOf("Was expecting one of:");

            if (index == -1)
            {
                index = oldArg.indexOf("Was expecting:");
            }

            if (index != -1)
            {
                expecting = oldArg.substring(index);
                expecting = expecting.replace("Was expecting one of:",
                        "Was expecting one of following:");
                expecting = expecting.replace("Was expecting:",
                        "Was expecting following:");
                expecting = expecting.replace("&lt;ID&gt;", "# &lt;ID&gt;");
                expecting = expecting.replace("&lt;SUBFLOW_ID&gt;",
                        "# &lt;SUBFLOW_ID&gt;");
                expecting = expecting.replace(
                        "&quot;# END GlobalSight Download File&quot;",
                        "# END GlobalSight Download File");
            }

            if (expecting != null)
            {
                newArg += expecting;
            }

            args[0] = newArg;
        }
    }

    public OfflinePageData getUploadPageData()
    {
        return m_uploadPageData;
    }

    public String getUnextractedFileTaskId()
    {
        return m_unextractedFileTaskId;
    }

    /**
     * Check the task state(Task.STATE_ACCEPTED) and task acceptor, fix for
     * GBS-3584
     * 
     * @param task
     * @return
     */
    private String checkReportTask(User user, Task task)
    {
        String acceptor = task.getAcceptor();
        String userId = user.getUserId();
        if (!userId.equals(acceptor))
        {
            String errMsg = "TaskAcceptorError: You are not the acceptor of the activity that the uploaded file belongs to.";
            CATEGORY.error(errMsg);
            m_errWriter.addFileErrorMsg(errMsg);

            return m_errWriter.buildReportErroPage().toString();
        }

        int state = task.getState();
        if (state != Task.STATE_ACCEPTED)
        {
            String errMsg = "TaskStateError:The activity that the uploaded file belongs to is not in progress.";
            CATEGORY.error(errMsg);
            m_errWriter.addFileErrorMsg(errMsg);

            return m_errWriter.buildReportErroPage().toString();
        }
        return null;
    }

    /**
     * Check the task state(Task.STATE_ACCEPTED) and task acceptor, fix for
     * GBS-2191
     * 
     * @param task
     * @param fileName
     * @return
     */
    private String checkTask(User user, Task task, String fileName)
    {
        if (task == null)
        {
            String args[] =
            { fileName, m_uploadPageData.getTaskId() };
            String errMsg = MessageFormat.format(
                    m_messages.getString("TaskNullError"), (Object[]) args);

            CATEGORY.error(errMsg);

            m_errWriter.addFileErrorMsg(errMsg);
            return m_errWriter.buildPageForTaskError().toString();
        }

        String userId = user.getUserId();

        int state = task.getState();
        if (state != Task.STATE_ACCEPTED)
        {
            // Fix for GBS-2393
            boolean statusError = true;
            PermissionSet ps = null;
            try
            {
                ps = Permission.getPermissionManager().getPermissionSetForUser(
                        user.getUserId());
            }
            catch (Exception e)
            {
                CATEGORY.error(e);
            }
            if (ps.getPermissionFor(Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY))
            {
                // fetch the accepted task of this workflow if the task's
                // acceptor is current user
                Hashtable<String, Task> tasks = task.getWorkflow().getTasks();
                Iterator<Task> it = tasks.values().iterator();
                while (it.hasNext())
                {
                    Task tk = it.next();
                    if (tk.getState() == Task.STATE_ACCEPTED)
                    {
                        if (userId.equals(tk.getAcceptor()))
                        {
                            statusError = false;
                        }
                        break;
                    }
                }
            }
            if (statusError)
            {
                String args[] =
                { fileName, task.getJobName(),
                        task.getTargetLocale().toString(),
                        String.valueOf(task.getId()) };
                String errMsg = MessageFormat.format(
                        m_messages.getString("TaskStatusError"),
                        (Object[]) args);

                CATEGORY.error(errMsg);

                m_errWriter.addFileErrorMsg(errMsg);
                return m_errWriter.buildPageForTaskError().toString();
            }
        }
        else
        {
            String acceptor = task.getAcceptor();
            if (!userId.equals(acceptor))
            {
                String args[] =
                { fileName, task.getJobName(),
                        task.getTargetLocale().toString(),
                        String.valueOf(task.getId()),
                        String.valueOf(task.getAcceptor()), acceptor };
                String errMsg = MessageFormat.format(
                        m_messages.getString("TaskAcceptorError"),
                        (Object[]) args);

                CATEGORY.error(errMsg);

                m_errWriter.addFileErrorMsg(errMsg);
                return m_errWriter.buildPageForTaskError().toString();
            }
        }
        return null;
    }

    /**
     * Get task by task id, fix for GBS-2191
     * 
     * @param ownerTaskId
     * @return
     */
    private Task getTask(long ownerTaskId)
    {
        Task task = null;
        try
        {
            task = ServerProxy.getTaskManager().getTask(ownerTaskId);
        }
        catch (Exception e)
        {
            CATEGORY.error("getTask Error. And taskId is " + ownerTaskId, e);
        }
        return task;
    }

    /**
     * Get excluded item types by task, fix for GBS-2191.
     * 
     * @param task
     * @return
     */
    private List<String> getExcludedItemTypes(Task task)
    {
        if (task != null)
        {
            L10nProfile l10nProfile = task.getWorkflow().getJob()
                    .getL10nProfile();
            List<String> p_excludedItemTypes = l10nProfile
                    .getTranslationMemoryProfile().getJobExcludeTuTypes();
            return p_excludedItemTypes;
        }
        return null;
    }

    @Override
    public void cancel()
    {
        cancel = true;

        if (m_errChecker != null)
            m_errChecker.cancel();
    }

    public String reportErrorInfos()
    {
        String errMsg = "TaskIDError: Now you are attempting to upload old report, "
                + "please make sure that proper report type and task id infomation are available in the report."
                + "\n\nCurrent supported report types are:\nTranslation Edit Report\nReviewers Comments Report"
                + "\nReviewers Comments Report(Simplified) \n\nFor new reports downloaded from former "
                + "GlobalSight server,please downloaded new offline report again .";
        CATEGORY.error(errMsg);

        m_errWriter.addFileErrorMsg(errMsg);
        return m_errWriter.buildPageForTaskError().toString();
    }

    private boolean isPRRReport(Sheet sheet, int segmentHeaderRow)
    {
        // Cell "K11"
        String jobId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 10);
        // Cell "L11"
        String segmentId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 11);
        // Cell "M11"
        String pageName = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 12);
        // Cell "C11"
        String modifyTranslation = ExcelUtil.getCellValue(sheet,
                segmentHeaderRow, 2);

        if ("Job id".equalsIgnoreCase(jobId)
                && "Segment id".equalsIgnoreCase(segmentId)
                && "Page name".equalsIgnoreCase(pageName)
                && modifyTranslation.startsWith("Updated Target Segment"))
        {
            return true;
        }

        return false;
    }

    // Post Review QA Report
    private String loadPRRReportData(Sheet sheet, Task task,
            GlobalSightLocale tLocale)
    {
        int segmentStartRow = ImplementedCommentsCheckReportGenerator.SEGMENT_START_ROW + 4;
        Set<String> jobIds = new HashSet<String>();

        segId2RequiredTranslation = new HashMap<Long, String>();
        segId2PageId = new HashMap<Long, Long>();
        segId2FailureType = new HashMap<Long, String>();
        segId2Comment = new HashMap<Long, String>();
        segId2CommentStatus = new HashMap<Long, String>();

        String segmentId = null;
        long segIdLong;
        String updatedText = null;
        String jobIdText = null;
        String comment = null;
        String commentStatus = null;
        boolean hasSegmentIdErro = false;

        int n = 5;
        int m = LOAD_DATA - n;

        qualityAssessment = ExcelUtil.getCellValue(sheet, 6, 1);
        marketSuitabilty = ExcelUtil.getCellValue(sheet, 7, 1);
        taskComment = ExcelUtil.getCellValue(sheet, 8, 1);

        for (int j = segmentStartRow, row = sheet.getLastRowNum(); j <= row; j++)
        {
            if (cancel)
                return null;

            int x = j * m / row;
            updateProcess(n + x);

            segmentId = ExcelUtil.getCellValue(sheet, j, 11);
            if (StringUtil.isEmpty(segmentId))
            {
                break;
            }
            segIdLong = new Long(Long.parseLong(segmentId));

            updatedText = ExcelUtil.getCellValue(sheet, j, 2);
            comment = ExcelUtil.getCellValue(sheet, j, 3);
            commentStatus = ExcelUtil.getCellValue(sheet, j, 5);

            if (EditUtil.isRTLLocale(tLocale))
                updatedText = EditUtil.removeU200F(updatedText);

            jobIdText = ExcelUtil.getCellValue(sheet, j, 10);
            jobIds.add(jobIdText);
            if (segmentId != null && !segmentId.equals(""))
            {
                if (updatedText != null && !updatedText.equals(""))
                {
                    segId2RequiredTranslation.put(segIdLong, updatedText);
                }
            }
            else
            {
                m_errWriter.addFileErrorMsg("Segment id is lost in row "
                        + (j + 1) + "\r\n");
                hasSegmentIdErro = true;
            }
        }
        if (task.isType(Task.TYPE_REVIEW))
        {
            segId2RequiredTranslation.clear();
        }

        if (hasSegmentIdErro)
        {
            return m_errWriter.buildReportErroPage().toString();
        }

        if (jobIds.size() > 1)
        {
            m_errWriter
                    .addFileErrorMsg("The job id is not consistent, you may hava changed some of them."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if ((jobIds.size() == 1)
                && !jobIds.contains(String.valueOf(task.getJobId())))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not belong to this job."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if (jobIds.size() == 0)
        {
            m_errWriter
                    .addFileErrorMsg("No job id detected."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }

        return null;
    }
    
    private boolean isTVRReport(Sheet sheet, int segmentHeaderRow)
    {
        // Cell "L7"
        String jobId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 11);
        // Cell "M7"
        String segmentId = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 12);
        // Cell "N7"
        String pageName = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 13);
        // Cell "D7"
        String modifyTranslation = ExcelUtil.getCellValue(sheet,
                segmentHeaderRow, 3);

        if ("Job id".equalsIgnoreCase(jobId)
                && "Segment id".equalsIgnoreCase(segmentId)
                && "Page name".equalsIgnoreCase(pageName)
                && modifyTranslation.startsWith("Modify the translation here"))
        {
            return true;
        }

        return false;
    }
    
    private String loadTVRReportData(Sheet sheet, Task task,
            GlobalSightLocale tLocale)
    {
        int segmentStartRow = ImplementedCommentsCheckReportGenerator.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();

        segId2RequiredTranslation = new HashMap<Long, String>();
        segId2PageId = new HashMap<Long, Long>();
        segId2FailureType = new HashMap<Long, String>();
        segId2Comment = new HashMap<Long, String>();
        segId2CommentStatus = new HashMap<Long, String>();

        String segmentId = null;
        long segIdLong;
        String updatedText = null;
        String jobIdText = null;
        String comment = null;
        String requiredComment = null;
        String commentStatus = null;
        boolean hasSegmentIdErro = false;

        int n = 5;
        int m = LOAD_DATA - n;

        for (int j = segmentStartRow, row = sheet.getLastRowNum(); j <= row; j++)
        {
            if (cancel)
                return null;

            int x = j * m / row;
            updateProcess(n + x);

            segmentId = ExcelUtil.getCellValue(sheet, j, 12);
            if (StringUtil.isEmpty(segmentId))
            {
                break;
            }
            segIdLong = new Long(Long.parseLong(segmentId));

            updatedText = ExcelUtil.getCellValue(sheet, j, 3);
            comment = ExcelUtil.getCellValue(sheet, j, 4);
            requiredComment = ExcelUtil.getCellValue(sheet, j, 5);
            commentStatus = ExcelUtil.getCellValue(sheet, j, 7);

            if (EditUtil.isRTLLocale(tLocale))
                updatedText = EditUtil.removeU200F(updatedText);

            jobIdText = ExcelUtil.getCellValue(sheet, j, 11);
            jobIds.add(jobIdText);
            if (segmentId != null && !segmentId.equals(""))
            {
                if (updatedText != null && !updatedText.equals(""))
                {
                    segId2RequiredTranslation.put(segIdLong, updatedText);
                }
                if (comment != null && !comment.equals(""))
                {
                    if (requiredComment != null
                            && !requiredComment.equals("")
                            && !StringUtil.equalsIgnoreSpace(requiredComment,
                                    ""))
                    {
                        segId2Comment.put(segIdLong, requiredComment);
                    }
                    segId2CommentStatus.put(segIdLong, commentStatus);
                }
                else
                {
                    if (requiredComment != null
                            && !requiredComment.equals("")
                            && !StringUtil.equalsIgnoreSpace(requiredComment,
                                    ""))
                    {
                        segId2Comment.put(segIdLong, requiredComment);
                        segId2CommentStatus.put(segIdLong, "query");
                    }
                }
            }
            else
            {
                m_errWriter.addFileErrorMsg("Segment id is lost in row "
                        + (j + 1) + "\r\n");
                hasSegmentIdErro = true;
            }
        }

        if (hasSegmentIdErro)
        {
            return m_errWriter.buildReportErroPage().toString();
        }

        if (jobIds.size() > 1)
        {
            m_errWriter
                    .addFileErrorMsg("The job id is not consistent, you may hava changed some of them."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if ((jobIds.size() == 1)
                && !jobIds.contains(String.valueOf(task.getJobId())))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not belong to this job."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else if (jobIds.size() == 0)
        {
            m_errWriter
                    .addFileErrorMsg("No job id detected."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }

        return null;
    }
}
