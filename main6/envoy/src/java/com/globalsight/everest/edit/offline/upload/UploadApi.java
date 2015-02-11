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
import java.io.InputStream;
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

import jxl.Sheet;
import jxl.Workbook;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
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
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.edit.CommentHelper;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditHelper;
import com.globalsight.everest.edit.offline.OfflineFileUploadStatus;
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
import com.globalsight.everest.tuv.TuvException;
import com.globalsight.everest.tuv.TuvImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReviewerLisaQAXlsReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.Cancelable;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.ReviewersCommentsReportGenerator;
import com.globalsight.everest.webapp.pagehandler.edit.EditCommonHelper;
import com.globalsight.ling.rtf.RtfAPI;
import com.globalsight.ling.rtf.RtfDocument;
import com.globalsight.ling.tm2.persistence.DbUtil;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

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

    // For uploading report
    private Map segId2RequiredTranslation = null;

    private Map<Long, String> segId2Comment = null;

    private Map segId2PageId = null;

    private Map segId2FailureType = null;

    private Map segId2CommentStatus = null;

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

    public OEMProcessStatus getStatus()
    {
        return status;
    }

    public void setStatus(OEMProcessStatus status)
    {
        this.status = status;
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

            String errPage;
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
            
            OfflineFileUploadStatus status = OfflineFileUploadStatus
                    .getInstance();

            // for GBS-1939, for GBS-2829
            String taskId = m_uploadPageData.getTaskId();
            String[] taskIds = null;
            if (taskId.contains(","))
            {
                taskIds = taskId.split(",");
                for (String tid : taskIds)
                {
                    Long isUploadingTaskId = Long.valueOf(tid);
                    Task isUploadingTask = getTask(isUploadingTaskId);
                    if (isUploadingTask != null)
                    {
                        isUploadingTask.setIsUploading('Y');
                        HibernateUtil.update(isUploadingTask);
                        isUploadingTasks.add(isUploadingTask);

                        status.addFileState(isUploadingTaskId,
                                p_fileName, "Running");
                    }
                    
                }
            }
            else
            {
                Long isUploadingTaskId = Long.valueOf(taskId);
                Task isUploadingTask = getTask(isUploadingTaskId);
                isUploadingTask.setIsUploading('Y');
                HibernateUtil.update(isUploadingTask);
                
                isUploadingTasks.add(isUploadingTask);
                
                status.addFileState(isUploadingTaskId,
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
                        return errPage;
                    }
                }
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_reader)) != null)
            {
                return errPage;
            }

            // Verify if upload file is a consolated file
            if (m_uploadPageData.getPageId().indexOf(",") > 0)
                m_uploadPageData.setConsolated(true);

            if ((errPage = postLoadInit(p_ownerTaskId,
                    m_uploadPageData.getTaskId(), p_excludedItemTypes,
                    DOWNLOAD_FILE_FORMAT_TXT)) != null)
            {
                return errPage;
            }

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkPage(p_user, p_fileName)) != null)
            {
                return errPage;
            }
            
            // do not save page if iscontinue = n for internal tag error
            if (this.status.getIsContinue() != null
                    && this.status.getIsContinue().equals(Boolean.FALSE))
            {
                return "IsContinue=fasle";
            }

            if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                    p_jmsQueueDestination, p_user, p_fileName)) != null)
            {
                return errPage;
            }

        }
        finally
        {
            if (isUploadingTasks != null)
            {
                for (Task isUploadingTask : isUploadingTasks)
                {
                    isUploadingTask.setIsUploading('N');
                    HibernateUtil.update(isUploadingTask);
                }
            }
        }

        return null;
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
    public String process_GS_PARAVIEW_1(RtfDocument p_rtfDoc, User p_user,
            long p_ownerTaskId, String p_fileName,
            Collection p_excludedItemTypes, String p_jmsQueueDestination)
    {
        // for GBS-1939
        Task isUploadingTask = null;

        try
        {
            // m_referencePageData = new PageData();
            // so getPage() will be cleared if errors occur
            m_uploadPageData = new OfflinePageData();

            String errPage = null;

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

            // for GBS-1939
            Long isUploadingTaskId = Long.valueOf(m_uploadPageData.getTaskId());
            isUploadingTask = getTask(isUploadingTaskId);
            isUploadingTask.setIsUploading('Y');
            HibernateUtil.update(isUploadingTask);
            
            OfflineFileUploadStatus status = OfflineFileUploadStatus
                    .getInstance();
            status.addFileState(isUploadingTaskId, p_fileName, "Running");

            // Fix for GBS-2191
            if (p_ownerTaskId == -1)
            {
                p_ownerTaskId = Long.valueOf(m_uploadPageData.getTaskId());
                Task task = getTask(p_ownerTaskId);
                if ((errPage = checkTask(p_user, task, p_fileName)) != null)
                {
                    return errPage;
                }
                p_excludedItemTypes = getExcludedItemTypes(task);
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_rtfDoc)) != null)
            {
                return errPage;
            }

            if ((errPage = postLoadInit(p_ownerTaskId,
                    m_uploadPageData.getTaskId(), p_excludedItemTypes,
                    DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE)) != null)
            {
                return errPage;
            }

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkPage(p_user, p_fileName)) != null)
            {
                return errPage;
            }
            
            // do not save page if iscontinue = n for internal tag error
            if (this.status.getIsContinue() != null
                    && this.status.getIsContinue().equals(Boolean.FALSE))
            {
                return "IsContinue=fasle";
            }

            // Now we're ready to save.
            if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                    p_jmsQueueDestination, p_user, p_fileName)) != null)
            {
                return errPage;
            }
        }
        finally
        {
            if (isUploadingTask != null)
            {
                isUploadingTask.setIsUploading('N');
                HibernateUtil.update(isUploadingTask);
            }
        }

        return null;
    }

    public String processReport(File p_tempFile, User p_user, Task p_task,
            String p_fileName, String p_jmsQueueDestination, String p_reportName)
            throws Exception
    {
        String errPage = null;

        long taskId = p_task.getId();
        long companyId = p_task.getCompanyId();
        if ((errPage = preLoadInit(taskId, p_user)) != null)
        {
            return errPage;
        }
        // load the upload file
        if ((errPage = loadReportData(p_tempFile, p_fileName, taskId,
                p_reportName)) != null)
        {
            CATEGORY.error("UploadApi.loadReportData(): "
                    + "Unable to load the upload-file.");

            return errPage;
        }

        if (cancel)
            return null;

        if (p_reportName.equals(WebAppConstants.TRANSLATION_EDIT))
        {
            if ((errPage = createErrorChecker()) != null)
            {
                return errPage;
            }

            if (cancel)
                return null;

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkReportPage(p_user, companyId)) != null)
            {
                return errPage;
            }
        }

        if (cancel)
            return null;

        boolean onlyComment = p_reportName
                .equals(WebAppConstants.LANGUAGE_SIGN_OFF);

        uploadComments(p_user, onlyComment, companyId);

        return null;
    }

    private void updateProcess(int n)
    {
        if (status == null)
            return;

        status.updateProcess(n);
    }

    @SuppressWarnings("unchecked")
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
            tmpFile = p_tempFile.createTempFile("RI_", fileSuff);
            FileUtils.copyFile(p_tempFile, tmpFile);
            fis = new FileInputStream(tmpFile);
            
            Task task = ServerProxy.getTaskManager().getTask(p_taskId);
            long companyId = task != null ? task.getCompanyId() : Long
                    .parseLong(CompanyWrapper.getCurrentCompanyId());
            long jobId = task.getJobId();
            
            org.apache.poi.ss.usermodel.Workbook workbook = ExcelUtil
                    .getWorkbook(tmpFile.getAbsolutePath(), fis);
            org.apache.poi.ss.usermodel.Sheet sheet = null;
            
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

            int languageInfoRow = ReviewerLisaQAXlsReportHelper.LANGUAGE_INFO_ROW;
            int segmentHeaderRow = ReviewerLisaQAXlsReportHelper.SEGMENT_HEADER_ROW;
            int segmentStartRow = ReviewerLisaQAXlsReportHelper.SEGMENT_START_ROW;

            if (p_reportName.equals(WebAppConstants.LANGUAGE_SIGN_OFF))
            {
                languageInfoRow = ReviewersCommentsReportGenerator.LANGUAGE_INFO_ROW;
                segmentHeaderRow = ReviewersCommentsReportGenerator.SEGMENT_HEADER_ROW;
                segmentStartRow = ReviewersCommentsReportGenerator.SEGMENT_START_ROW;
            }

            String targetLanguage = sheet.getRow(languageInfoRow).getCell(1).toString(); 
            if (StringUtil.isEmpty(targetLanguage))
            {
                m_errWriter
                        .addFileErrorMsg("No language information detected.");
                return m_errWriter.buildReportErroPage().toString();
            }
            else if (targetLanguage.indexOf('[') < 0
                    || targetLanguage.indexOf(']') < 0)
            {
                // Operates Reviewers Comments Report from 8.2.1 Release Server.
                // if (p_reportName.equals(WebAppConstants.LANGUAGE_SIGN_OFF)
                // && sheet.getCell(0,
                // 3).getContents().equalsIgnoreCase("Job id"))
                // {
                // return loadReportData821(sheet, task);
                // }

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
            Set<String> jobIds = new HashSet<String>();

            String value = "";
            if (p_reportName.equals(WebAppConstants.TRANSLATION_EDIT))
            {
                value = ExcelUtil.getCellValue(sheet, segmentHeaderRow, 0);
                if (!ExcelUtil.getCellValue(sheet, segmentHeaderRow, 0)
                        .equalsIgnoreCase("Job id")
                        || !ExcelUtil.getCellValue(sheet, segmentHeaderRow, 1)
                                .equalsIgnoreCase("Segment id")
                        || !ExcelUtil.getCellValue(sheet, segmentHeaderRow, 2)
                                .equalsIgnoreCase("TargetPage id")
                        || !ExcelUtil.getCellValue(sheet, segmentHeaderRow, 8)
                                .startsWith("Required translation"))
                {
                    // Added for parsing report from 8.2.2 release.
                    if (ExcelUtil.getCellValue(sheet, segmentHeaderRow, 7)
                            .startsWith("Required translation"))
                    {
                        return loadTranslateReportDate822(sheet, task, tLocale);
                    }

                    m_errWriter
                            .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
                                    + "\r\nMaybe you have changed some column header signatures or orders."
                                    + "\r\nThe following column header signatures and orders should keep the source report's format."
                                    + "\r\nJob id, Segment id, TargetPage id, Required translation."
                                    + "\r\nPlease make sure they are correct and upload again.");
                    return m_errWriter.buildReportErroPage().toString();
                }
                else
                {
                    segId2RequiredTranslation = new HashMap();
                    segId2PageId = new HashMap();
                    segId2FailureType = new HashMap();
                    segId2Comment = new HashMap<Long, String>();
                    segId2CommentStatus = new HashMap();

                    String segmentId = null;
                    long segIdLong;
                    String updatedText = null;
                    String jobIdText = null;
                    String comment = null;
                    String requiredComment = null;
                    String failureType = null;
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

                        segmentId = ExcelUtil.getCellValue(sheet, j, 1);
                        if(StringUtil.isEmpty(segmentId))
                        {
                            break;
                        }
                        segIdLong = new Long(Long.parseLong(segmentId));

                        updatedText = ExcelUtil.getCellValue(sheet, j, 8);
                        comment = ExcelUtil.getCellValue(sheet, j, 9);
                        requiredComment = ExcelUtil.getCellValue(sheet, j, 10);
                        failureType = ExcelUtil.getCellValue(sheet, j, 11);
                        commentStatus = ExcelUtil.getCellValue(sheet, j, 12);

                        if (EditUtil.isRTLLocale(tLocale))
                            updatedText = EditUtil.removeU200F(updatedText);

                        jobIdText = ExcelUtil.getCellValue(sheet, j, 0);
                        jobIds.add(jobIdText);
                        if (segmentId != null && !segmentId.equals(""))
                        {
                            if (updatedText != null && !updatedText.equals(""))
                            {
                                segId2RequiredTranslation.put(segIdLong,
                                        updatedText);
                            }
                            if (requiredComment != null
                                    && !requiredComment.equals(""))
                            {

                                segId2Comment.put(segIdLong, requiredComment);
                                if ("".equals(comment))
                                {
                                    // New added comment, the status must be
                                    // query
                                    segId2CommentStatus.put(segIdLong, "query");
                                }
                                else
                                {
                                    segId2CommentStatus.put(segIdLong,
                                            commentStatus);
                                }

                            }
                            else
                            {
                                if (comment != null && !comment.equals(""))
                                {
                                    segId2Comment.put(segIdLong, comment);
                                    segId2CommentStatus.put(segIdLong,
                                            commentStatus);
                                }
                            }
                        }
                        else
                        {
                            m_errWriter
                                    .addFileErrorMsg("Segment id is lost in row "
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
                            && !jobIds.contains(String.valueOf(jobId)))
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
                }

            }
            else if (p_reportName.equals(WebAppConstants.LANGUAGE_SIGN_OFF))
            {
                if (!ExcelUtil.getCellValue(sheet, segmentHeaderRow, 0)
                        .equalsIgnoreCase("Job id")
                        || !ExcelUtil.getCellValue(sheet, segmentHeaderRow, 1)
                                .equalsIgnoreCase("Segment id")
                        || !ExcelUtil.getCellValue(sheet, segmentHeaderRow, 2)
                                .equalsIgnoreCase("Page name")
                        || !ExcelUtil.getCellValue(sheet, segmentHeaderRow, 8)
                                .equalsIgnoreCase(
                                        bundle.getString("lb_comment_free")))
                {
                    // Added for parsing report from 8.2.2 release.
                    if (ExcelUtil.getCellValue(sheet, segmentHeaderRow, 6)
                            .equalsIgnoreCase(
                                    bundle.getString("lb_comment_free")))
                    {
                        return loadReviewReportDate822(sheet, task, tLocale);
                    }

                    m_errWriter
                            .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
                                    + "\r\nMaybe you have changed some column header signature or orders."
                                    + "\r\nThe following column header signatrues and orders should keep the source report's format."
                                    + "\r\nJob id, Segment id, Page name, Comment(free hand your comments)."
                                    + "\r\nPlease make sure they are correct and upload again.");
                    return m_errWriter.buildReportErroPage().toString();
                }
                else
                {
                    segId2Comment = new HashMap();
                    segId2PageId = new HashMap();
                    segId2FailureType = new HashMap();
                    segId2CommentStatus = new HashMap();
                    String segmentId = null;
                    String pageId = null;
                    String comment = null;
                    Long segIdLong = null;
                    String jobIdText = null;
                    String failureType = null;
                    String commentStatus = null;
                    boolean hasIdErro = false;
                    for (int k = segmentStartRow, row = sheet.getLastRowNum(); k <= row; k++)
                    {
                        if (cancel)
                            return null;

                        segmentId = ExcelUtil.getCellValue(sheet, k, 1);
                        if (segmentId == null || segmentId.trim().length() == 0)
                        {
                            break;
                        }
                        segIdLong = new Long(Long.parseLong(segmentId));
                        Tu tu = ServerProxy.getTuvManager()
                                .getTuForSegmentEditor(segIdLong, companyId);
                        TuImpl tuImpl = (TuImpl) tu;
                        Tuv tuv = tuImpl.getTuv(reportTargetLocaleId, companyId);
                        TuvImpl tuvImpl = (TuvImpl) tuv;
                        TargetPage targetPage = tuvImpl
                                .getTargetPage(companyId);
                        pageId = new String(String.valueOf(targetPage.getId()));

                        comment = ExcelUtil.getCellValue(sheet, k, 8);
                        if (EditUtil.isRTLLocale(tLocale))
                            comment = EditUtil.removeU200F(comment);

                        failureType = ExcelUtil.getCellValue(sheet, k, 9);
                        commentStatus = ExcelUtil.getCellValue(sheet, k, 10);
                        jobIdText = ExcelUtil.getCellValue(sheet, k, 0);

                        jobIds.add(jobIdText);

                        if (comment != null && !comment.equals(""))
                        {
                            if (segmentId != null && !segmentId.equals("")
                                    && pageId != null && !pageId.equals(""))
                            {
                                segId2PageId.put(segIdLong,
                                        new Long(Long.parseLong(pageId)));
                                segId2Comment.put(segIdLong, comment);
                                segId2FailureType.put(segIdLong, failureType);
                                segId2CommentStatus.put(segIdLong,
                                        commentStatus);
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
                            && !jobIds.contains(String.valueOf(jobId)))
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
                }

            }
            else
            {
                m_errWriter
                        .addFileErrorMsg("The report type is not correct."
                                + "\r\nPlease make sure the report type is correct and upload again.");
                return m_errWriter.buildReportErroPage().toString();
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

    // Due modify Translations Edit Report column in 8.2.3, there need a
    // function for old report from 8.2.2.
    private String loadTranslateReportDate822(org.apache.poi.ss.usermodel.Sheet p_sheet, Task p_task,
            GlobalSightLocale p_tLocale) throws TuvException, RemoteException,
            GeneralException
    {
        long companyId = p_task != null ? p_task.getCompanyId() : Long
                .parseLong(CompanyWrapper.getCurrentCompanyId());

        int segmentHeaderRow = ReviewerLisaQAXlsReportHelper.SEGMENT_HEADER_ROW;
        int segmentStartRow = ReviewerLisaQAXlsReportHelper.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();
        if (!ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 0)
                .equalsIgnoreCase("Job id")
                || !ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 1)
                        .equalsIgnoreCase("Segment id")
                || !ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 2)
                        .equalsIgnoreCase("TargetPage id")
                || !ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 7)
                        .startsWith("Required translation"))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
                            + "\r\nMaybe you have changed some column header signatures or orders."
                            + "\r\nThe following column header signatures and orders should keep the source report's format."
                            + "\r\nJob id, Segment id, TargetPage id, Required translation."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else
        {
            segId2RequiredTranslation = new HashMap();
            segId2PageId = new HashMap();
            segId2FailureType = new HashMap();
            segId2Comment = new HashMap<Long, String>();
            segId2CommentStatus = new HashMap();

            String segmentId = null;
            long segIdLong;
            String pageId = null;
            String updatedText = null;
            String jobIdText = null;
            String comment = null;
            String requiredComment = null;
            String failureType = null;
            String commentStatus = null;
            boolean hasSegmentIdErro = false;

            for (int j = segmentStartRow, row = p_sheet.getLastRowNum(); j <= row; j++)
            {
                segmentId = ExcelUtil.getCellValue(p_sheet, j, 1);
                segIdLong = new Long(Long.parseLong(segmentId));

                Tu tu = ServerProxy.getTuvManager().getTuForSegmentEditor(
                        segIdLong, companyId);
                TuImpl tuImpl = (TuImpl) tu;
                Tuv tuv = tuImpl.getTuv(reportTargetLocaleId, companyId);
                TuvImpl tuvImpl = (TuvImpl) tuv;
                TargetPage targetPage = tuvImpl.getTargetPage(companyId);
                pageId = new String(String.valueOf(targetPage.getId()));

                updatedText = ExcelUtil.getCellValue(p_sheet, j, 7);
                comment = ExcelUtil.getCellValue(p_sheet, j, 8);
                requiredComment = ExcelUtil.getCellValue(p_sheet, j, 9);
                failureType = ExcelUtil.getCellValue(p_sheet, j, 10);
                commentStatus = ExcelUtil.getCellValue(p_sheet, j, 11);

                if (EditUtil.isRTLLocale(p_tLocale))
                    updatedText = EditUtil.removeU200F(updatedText);

                jobIdText = ExcelUtil.getCellValue(p_sheet, j, 0);
                jobIds.add(jobIdText);
                if (segmentId != null && !segmentId.equals(""))
                {
                    if (updatedText != null && !updatedText.equals(""))
                    {
                        segId2RequiredTranslation.put(segIdLong, updatedText);
                    }
                    if (requiredComment != null && !requiredComment.equals(""))
                    {

                        segId2PageId.put(segIdLong,
                                new Long(Long.parseLong(pageId)));
                        segId2Comment.put(segIdLong, requiredComment);
                        if ("".equals(comment))
                        {
                            // New added comment, the status must be
                            // query
                            segId2CommentStatus.put(segIdLong, "query");
                        }
                        else
                        {
                            segId2CommentStatus.put(segIdLong, commentStatus);
                        }

                    }
                    else
                    {
                        if (comment != null && !comment.equals(""))
                        {
                            segId2PageId.put(segIdLong,
                                    new Long(Long.parseLong(pageId)));
                            segId2Comment.put(segIdLong, comment);
                            segId2CommentStatus.put(segIdLong, commentStatus);
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
                    && !jobIds.contains(String.valueOf(p_task.getJobId())))
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
        }
        return null;
    }

    // Due modify Reviewer Comments Report column in 8.2.3, there need a
    // function for old report from 8.2.2.
    private String loadReviewReportDate822(org.apache.poi.ss.usermodel.Sheet p_sheet, Task p_task,
            GlobalSightLocale p_tLocale) throws TuvException, RemoteException,
            GeneralException
    {
        long companyId = p_task != null ? p_task.getCompanyId() : Long
                .parseLong(CompanyWrapper.getCurrentCompanyId());

        int segmentHeaderRow = ReviewersCommentsReportGenerator.SEGMENT_HEADER_ROW;
        int segmentStartRow = ReviewersCommentsReportGenerator.SEGMENT_START_ROW;
        Set<String> jobIds = new HashSet<String>();
        if (!ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 0)
                .equalsIgnoreCase("Job id")
                || !ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 1)
                        .equalsIgnoreCase("Segment id")
                || !ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 2)
                        .equalsIgnoreCase("Page name")
                || !ExcelUtil.getCellValue(p_sheet, segmentHeaderRow, 6)
                        .startsWith("Comment"))
        {
            m_errWriter
                    .addFileErrorMsg("The file you are uploading does not keep the report's correct format."
                            + "\r\nMaybe you have changed some column header signature or orders."
                            + "\r\nThe following column header signatrues and orders should keep the source report's format."
                            + "\r\nJob id, Segment id, Page name, Comment(free hand your comments)."
                            + "\r\nPlease make sure they are correct and upload again.");
            return m_errWriter.buildReportErroPage().toString();
        }
        else
        {
            segId2Comment = new HashMap<Long, String>();
            segId2PageId = new HashMap();
            segId2FailureType = new HashMap();
            segId2CommentStatus = new HashMap();
            String segmentId = null;
            String pageId = null;
            String comment = null;
            Long segIdLong = null;
            String jobIdText = null;
            String failureType = null;
            String commentStatus = null;
            boolean hasIdErro = false;
            for (int k = segmentStartRow, row = p_sheet.getLastRowNum(); k <= row; k++)
            {
                segmentId = ExcelUtil.getCellValue(p_sheet, k, 1);
                segIdLong = new Long(Long.parseLong(segmentId));
                Tu tu = ServerProxy.getTuvManager().getTuForSegmentEditor(
                        segIdLong, companyId);
                TuImpl tuImpl = (TuImpl) tu;
                Tuv tuv = tuImpl.getTuv(reportTargetLocaleId, companyId);
                TuvImpl tuvImpl = (TuvImpl) tuv;
                TargetPage targetPage = tuvImpl.getTargetPage(companyId);
                pageId = new String(String.valueOf(targetPage.getId()));

                comment = ExcelUtil.getCellValue(p_sheet, k, 6);
                if (EditUtil.isRTLLocale(p_tLocale))
                    comment = EditUtil.removeU200F(comment);

                failureType = ExcelUtil.getCellValue(p_sheet, k, 7);
                commentStatus = ExcelUtil.getCellValue(p_sheet, k, 8);
                jobIdText = ExcelUtil.getCellValue(p_sheet, k, 0);

                jobIds.add(jobIdText);

                if (comment != null && !comment.equals(""))
                {
                    if (segmentId != null && !segmentId.equals("")
                            && pageId != null && !pageId.equals(""))
                    {
                        segId2PageId.put(segIdLong,
                                new Long(Long.parseLong(pageId)));
                        segId2Comment.put(segIdLong, comment);
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
                    && !jobIds.contains(String.valueOf(p_task.getJobId())))
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
        }

        return null;
    }

    /**
     * Adds Function to operate Reviewers Comments Report from 8.2.1 Release
     * Server. See details in GBS-2252.
     */
    /*
     * private String loadReportData821(Sheet p_sheet, Task p_task) { int
     * segmentHeaderRowRCR = 3;
     * 
     * try { long jobId = p_task.getJobId(); String targetLanguage =
     * p_task.getTargetLocale().getDisplayName();
     * 
     * if (targetLanguage == null || targetLanguage.equals("")) { m_errWriter
     * .addFileErrorMsg("No language information detected."); return
     * m_errWriter.buildReportErroPage().toString(); } else if
     * (targetLanguage.indexOf('[') < 0 || targetLanguage.indexOf(']') < 0) {
     * m_errWriter
     * .addFileErrorMsg("Target language format is not correct.\r\nIt should " +
     * "contain a portion which is a locale code encolsed by [ ] such as [zh_CN]"
     * ); return m_errWriter.buildReportErroPage().toString(); }
     * reportTargetLocaleId = getLocaleId(targetLanguage); GlobalSightLocale
     * tLocale = HibernateUtil.get(GlobalSightLocale.class,
     * reportTargetLocaleId);
     * 
     * Set<String> jobIds = new HashSet<String>();
     * 
     * if (!p_sheet.getCell(0, segmentHeaderRowRCR).getContents()
     * .equalsIgnoreCase("Job id") || !p_sheet.getCell(1,
     * segmentHeaderRowRCR).getContents() .equalsIgnoreCase("Segment id") ||
     * !p_sheet.getCell(2, segmentHeaderRowRCR).getContents()
     * .equalsIgnoreCase("Page name") || !p_sheet.getCell(8,
     * segmentHeaderRowRCR).getContents() .startsWith("Comment")) { m_errWriter
     * .addFileErrorMsg(
     * "The file you are uploading does not keep the report's correct format." +
     * "\r\nMaybe you have changed some column header signature or orders." +
     * "\r\nThe following column header signatrues and orders should keep the source report's format."
     * + "\r\nJob id, Segment id, Page name, Comment(free hand your comments)."
     * + "\r\nPlease make sure they are correct and upload again."); return
     * m_errWriter.buildReportErroPage().toString(); } else { segId2Comment =
     * new HashMap(); segId2PageId = new HashMap(); segId2FailureType = new
     * HashMap(); segId2CommentStatus = new HashMap(); String segmentId = null;
     * String pageId = null; String comment = null; Long segIdLong = null;
     * String jobIdText = null; String targetLang = null; String failureType =
     * null; boolean hasIdErro = false; for (int k = segmentHeaderRowRCR + 1,
     * row = p_sheet.getRows(); k < row; k++) { jobIdText = p_sheet.getCell(0,
     * k).getContents(); targetLang = p_sheet.getCell(5, k).getContents(); if
     * (!(String.valueOf(jobId).equals(jobIdText) &&
     * targetLanguage.equals(targetLang))) { continue; }
     * 
     * segmentId = p_sheet.getCell(1, k).getContents(); Tu tu =
     * ServerProxy.getTuvManager().getTuForSegmentEditor(
     * Long.parseLong(segmentId)); TuImpl tuImpl = (TuImpl) tu; Tuv tuv =
     * tuImpl.getTuv(reportTargetLocaleId); TuvImpl tuvImpl = (TuvImpl) tuv;
     * TargetPage targetPage = tuvImpl.getTargetPage(); pageId = new
     * String(String.valueOf(targetPage.getId()));
     * 
     * comment = p_sheet.getCell(8, k).getContents(); if
     * (EditUtil.isRTLLocale(tLocale)) comment = EditUtil.removeU200F(comment);
     * 
     * failureType = p_sheet.getCell(9, k).getContents();
     * 
     * jobIds.add(jobIdText); segIdLong = new Long(Long.parseLong(segmentId));
     * segId2FailureType.put(segIdLong, failureType);
     * 
     * if (comment != null && !comment.equals("")) { if (segmentId != null &&
     * !segmentId.equals("") && pageId != null && !pageId.equals("")) {
     * segId2PageId.put(segIdLong, new Long(Long.parseLong(pageId)));
     * segId2Comment.put(segIdLong, comment); } else { m_errWriter
     * .addFileErrorMsg("Segment or Page id is lost in row " + (k + 1) +
     * "\r\n"); hasIdErro = true; }
     * 
     * }
     * 
     * } if (hasIdErro) { return m_errWriter.buildReportErroPage().toString(); }
     * 
     * if (jobIds.size() == 0) { m_errWriter
     * .addFileErrorMsg("No matched job detected." +
     * "\r\nPlease make sure they are correct and upload again."); return
     * m_errWriter.buildReportErroPage().toString(); } }
     * 
     * } catch (Throwable ex) { String args[] = {
     * EditUtil.encodeHtmlEntities(ex.getMessage()) }; String errMsg =
     * MessageFormat.format(m_messages .getString("FormatTwoLoadError"),
     * (Object[]) args);
     * 
     * CATEGORY.error(errMsg);
     * 
     * m_errWriter.addFileErrorMsg(errMsg); return
     * m_errWriter.buildReportErroPage().toString(); }
     * 
     * return null;
     * 
     * }
     */

    private void uploadComments(User p_user, boolean onlyComment,
            long companyId) throws Exception
    {
        CommentManager commentManager = ServerProxy.getCommentManager();

        Set<Long> tuIds = segId2Comment.keySet();
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

        int n = LOAD_DATA + CHECK_SAVE;
        int m = COMMENT;

        if (onlyComment)
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
                    reportTargetLocaleId, companyId);
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
            if (comment != null && comment != "")
            {
                Long targetPageId = (Long) segId2PageId.get(tuIdLong);
                if (targetPageId == null)
                {
                    TargetPage targetPage = ((TuvImpl) tuv)
                            .getTargetPage(companyId);
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
                        String tpId_tuId_tuvId = issue.getLogicalKey()
                                .substring(0, index + 1);
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

                existIssues = cachedIssues.get(targetPageId + "_" + tuId + "_" + tuvId + "_");
                if (existIssues == null || existIssues.size() == 0)
                {
                    failureType = failureTypeError ? "" : failureType;
                    commentStatus = commentStatusError ? Issue.STATUS_OPEN
                            : commentStatus;
                    IssueImpl issue = new IssueImpl(Issue.TYPE_SEGMENT, tuvId,
                            "Comment by LSO", Issue.PRI_MEDIUM, commentStatus,
                            failureType.trim(), p_user.getUserId(), comment,
                            CommentHelper.makeLogicalKey(targetPageId, tuId,
                                    tuvId, 0));
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
                            IssueHistory history = (IssueHistory) histories
                                    .get(0);

                            if (history.reportedBy().equals(p_user.getUserId()))
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
                                // commentManager.replyToIssue(issue.getId(),
                                // "Comment by LSO",
                                // Issue.PRI_MEDIUM, Issue.STATUS_OPEN,
                                // Issue.CATEGORY_TYPE01, p_userId,
                                // comment);
                                commentManager.replyToIssue(issue.getId(),
                                        issue.getTitle(), issue.getPriority(),
                                        commentStatus, failureType.trim(),
                                        p_user.getUserId(), comment);
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
            
            OfflineFileUploadStatus status = OfflineFileUploadStatus
                    .getInstance();

            // for GBS-1939, for GBS-2829
            String taskId = m_uploadPageData.getTaskId();
            String[] taskIds = null;
            if (taskId.contains(","))
            {
                taskIds = taskId.split(",");
                for (String tid : taskIds)
                {
                    Long isUploadingTaskId = Long.valueOf(tid);
                    Task isUploadingTask = getTask(isUploadingTaskId);
                    if (isUploadingTask != null)
                    {
                        isUploadingTask.setIsUploading('Y');
                        HibernateUtil.update(isUploadingTask);
                        isUploadingTasks.add(isUploadingTask);
                        
                        status.addFileState(isUploadingTaskId, p_fileName,
                                "Running");
                    }
                }
            }
            else
            {
                Long isUploadingTaskId = Long.valueOf(taskId);
                Task isUploadingTask = getTask(isUploadingTaskId);
                isUploadingTask.setIsUploading('Y');
                HibernateUtil.update(isUploadingTask);
                
                isUploadingTasks.add(isUploadingTask);
                status.addFileState(isUploadingTaskId, p_fileName, "Running");
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
                        return errPage;
                    }
                }
            }

            if ((errPage = preLoadInit(p_ownerTaskId, p_user, p_rtfDoc)) != null)
            {
                return errPage;
            }

            // Verify if upload file is a consolated file
            if (m_uploadPageData.getPageId().indexOf(",") > 0)
                m_uploadPageData.setConsolated(true);

            if ((errPage = postLoadInit(p_ownerTaskId,
                    m_uploadPageData.getTaskId(), p_excludedItemTypes,
                    DOWNLOAD_FILE_FORMAT_TXT)) != null)
            {
                return errPage;
            }

            // Check uploaded page for errors. If there are no errors - save it
            if ((errPage = checkPage(p_user, p_fileName)) != null)
            {
                return errPage;
            }
            
            // do not save page if iscontinue = n for internal tag error
            if (this.status.getIsContinue() != null
                    && this.status.getIsContinue().equals(Boolean.FALSE))
            {
                return "IsContinue=fasle";
            }

            if ((errPage = save(m_uploadPageData, m_referencePageDatas,
                    p_jmsQueueDestination, p_user, p_fileName)) != null)
            {
                return errPage;
            }
        }
        finally
        {
            if (isUploadingTasks != null)
            {
                for (Task isUploadingTask : isUploadingTasks)
                {
                    isUploadingTask.setIsUploading('N');
                    HibernateUtil.update(isUploadingTask);
                }
            }
        }
        return null;
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
            
            OfflineFileUploadStatus status = OfflineFileUploadStatus
                    .getInstance();
            status.addFileState(p_ownerTaskId, p_fileName, "Running");

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
    private String loadListViewTextFile(Reader p_reader, String p_fileName,
            boolean p_keepIssues)
    {

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

            // set err writer's page, task and job ids
            m_errWriter.processOfflinePageData(m_uploadPageData);
        }
        catch (Throwable ex)
        {
            String args[] =
            { EditUtil.encodeHtmlEntities(ex.getMessage()) };

            try
            {
                p_reader.reset();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            bindErrMsg(args, p_reader);

            String errMsg = MessageFormat
                    .format(m_messages.getString("FormatTwoLoadError"),
                            (Object[]) args);

            CATEGORY.error(errMsg);

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
        // set report locale
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
            CATEGORY.error("Upload error, unable to set report locale to "
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
                                p_excludedItemTypes, p_uploadFileFormat);
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
            User p_user, String p_fileName)
    {
        try
        {
            m_uploadPageSaver.savePageToDb(m_uploadPageData, m_refPageDatas,
                    p_jmsQueueDestination, p_user, p_fileName);
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

        if ((errPage == null || errPage
                .contains("The following mandatory tags are missing"))
                && fileName.toLowerCase().endsWith(".ttx"))
        {
            Vector segments = m_uploadPageData.getSegmentList();
            boolean isError = false;
            if (segments != null)
            {
                for (Object object : segments)
                {
                    OfflineSegmentData segData = (OfflineSegmentData) object;
                    if (segData.getDisplaySegmentID() != null
                            && segData.getDisplaySegmentID().length() > 0
                            && "".equals(segData.getDisplaySourceText())
                            && "".equals(segData.getDisplayTargetText()))
                    {
                        m_errWriter.addSegmentErrorMsg(
                                segData.getDisplaySegmentID(),
                                "Cannot find target segment");
                        isError = true;
                    }
                }
            }

            if (isError)
            {
                errPage = m_errWriter.buildPage().toString();
            }
        }
        
        if (errPage != null)
        {
            if (CATEGORY.isDebugEnabled())
            {
                CATEGORY.debug("Page failed error checking. Returning error results");
            }

            return errPage;
        }

        return null;
    }

    private String checkReportPage(User p_user, long companyId)
            throws Exception
    {
        String errPage = null;

        boolean adjustWS = getAdjustWhitespaceParam(p_user.getUserId());
        if ((errPage = m_errChecker.checkAndSave(segId2RequiredTranslation,
                adjustWS, reportTargetLocaleId, companyId, p_user)) != null)
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
                // TODO Auto-generated catch block
                e.printStackTrace();
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
     * Get excludeed item types by task, fix for GBS-2191
     * 
     * @param task
     * @return
     */
    private List getExcludedItemTypes(Task task)
    {
        L10nProfile l10nProfile = task.getWorkflow().getJob().getL10nProfile();
        List p_excludedItemTypes = l10nProfile.getTranslationMemoryProfile()
                .getJobExcludeTuTypes();
        return p_excludedItemTypes;
    }

    private Sheet getSheet(Workbook p_workbook, Task p_task, String p_reportName)
    {
        if (WebAppConstants.LANGUAGE_SIGN_OFF.equals(p_reportName))
        {
            String sheetName = p_task.getTargetLocale().toString();
            Sheet sheet = p_workbook.getSheet(sheetName);
            return sheet == null ? p_workbook.getSheet(0) : sheet;
        }
        return p_workbook.getSheet(0);
    }

    @Override
    public void cancel()
    {
        cancel = true;

        if (m_errChecker != null)
            m_errChecker.cancel();
    }
}
