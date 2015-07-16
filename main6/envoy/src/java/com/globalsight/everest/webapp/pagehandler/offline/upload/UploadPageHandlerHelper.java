package com.globalsight.everest.webapp.pagehandler.offline.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.projecthandler.ProjectImpl;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.Cancelable;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;

public class UploadPageHandlerHelper implements WebAppConstants
{
    private static final Logger CATEGORY = Logger
            .getLogger(UploadPageHandler.class.getName());

    /**
     * Prepare works (eg: Copy the ori file to server named temp file etc)
     * before uploading process
     * 
     * @param p_request
     * @param httpSession
     * @param status
     *            ProcessStatus object
     * @param contentType
     *            file upload contentType
     */
    public void uploadStartPrepare(HttpServletRequest p_request,
            HttpSession httpSession, OEMProcessStatus status, String contentType)
    {
        String state;
        if (contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            processRequest(p_request, status);
            // now update the task in the session
            try
            {
                long taskId = -1;
                User user = TaskHelper.getUser(httpSession);
                String taskIdParam = p_request.getParameter(TASK_ID);
                if (taskIdParam == null)
                {
                    Task task = (Task) TaskHelper.retrieveObject(httpSession,
                            WORK_OBJECT);
                    if (task != null)
                    {
                        taskId = task.getId();
                    }
                }
                else
                {
                    taskId = TaskHelper.getLong(taskIdParam);
                }
                state = (String) TaskHelper.retrieveObject(httpSession,
                        TASK_STATE);
                if (state == null)
                {
                    state = p_request.getParameter(WebAppConstants.TASK_STATE);
                }
                TaskImpl task = (TaskImpl) TaskHelper.getTask(user.getUserId(),
                        taskId, Integer.parseInt(state));
                TaskHelper.storeObject(httpSession,
                        WebAppConstants.WORK_OBJECT, task);
            }
            catch (Exception e)
            {
                throw new EnvoyServletException(e);
            }
        }
    }

    public void cancelProcess(HttpServletRequest p_request,
            HttpServletResponse p_response, SessionManager sessionMgr)
    {
        OfflineEditManager oem = (OfflineEditManager) sessionMgr
                .getAttribute(UPLOAD_MANAGER);

        if (oem == null)
            return;

        if (oem instanceof Cancelable)
        {
            Cancelable cancelable = (Cancelable) oem;
            cancelable.cancel();
        }
    }

    /**
     * Uploading process includes read file contents, validate the content,
     * compare file content and database content
     * 
     * @param p_request
     * @param p_response
     * @param sessionMgr
     */
    @SuppressWarnings(
    { "rawtypes", "unchecked" })
    public void uploadProcess(HttpServletRequest p_request,
            HttpServletResponse p_response, SessionManager sessionMgr)
    {
        OEMProcessStatus status;
        OfflineEditManager OEM;
        String isReport = null;
        User user = (User) sessionMgr.getAttribute(USER);

        boolean fromTaskUploadPage = UPLOAD_FROMTASKUPLOAD
                .equals((String) sessionMgr.getAttribute(UPLOAD_ORIGIN));
        // Setting Delay time for task complete after upload
        Date taskUploadFileStartTime = new Date();
        String userId = user.getUserId();
        String taskId = "";
        if (fromTaskUploadPage)
        {
            taskId = (String) sessionMgr.getAttribute(TASK_ID);
        }
        String fileName = (String) sessionMgr.getAttribute("tempFileName");
        File file = (File) sessionMgr.getAttribute("tempFile");

        String reportTypeInfo = null;
        if ((fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase()
                .endsWith(".xls")))
        {
            // Check "AA1" cell to see if it is a report file.
            String[] taskInfo = getReportInfoFromXlsx(fileName, file, p_request);
            isReport = taskInfo[0];
            reportTypeInfo = taskInfo[1];
            if (!StringUtil.isEmpty(taskInfo[2]))
            {
                taskId = taskInfo[2];
            }

            // Locate current in progress task.
            try
            {
                long tskId = Long.valueOf(taskId);
                Task curTaskForReport = ServerProxy.getTaskManager().getTask(
                        tskId);
                if (curTaskForReport.getState() != Task.STATE_ACCEPTED)
                {
                    curTaskForReport = locateCurrentTask(tskId);
                    taskId = String.valueOf(curTaskForReport.getId());
                }
            }
            catch (Exception ignore)
            {

            }
        }

        String delayTimeTableKey = userId + taskId;
        Hashtable delayTimeTable = (Hashtable) sessionMgr
                .getAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE);
        if (delayTimeTable == null)
        {
            delayTimeTable = new Hashtable();
        }
        delayTimeTable.put(delayTimeTableKey, taskUploadFileStartTime);
        sessionMgr.setAttribute(WebAppConstants.TASK_COMPLETE_DELAY_TIME_TABLE,
                delayTimeTable);

        status = (OEMProcessStatus) sessionMgr.getAttribute(UPLOAD_STATUS);
        OEM = (OfflineEditManager) sessionMgr.getAttribute(UPLOAD_MANAGER);
        if ("yes".equals(isReport))
        {
            processReportFileContents(file, user, fileName, p_request, status,
                    OEM, reportTypeInfo, taskId);

            p_response.setContentType("text/html");
        }
        else
        {
            processFileContents(fromTaskUploadPage, file, user, fileName,
                    p_request, status, OEM);
            p_response.setContentType("text/html");
        }
    }

    /**
     * The method is for refresh process status by ajax invoke, try to get the
     * process status, the ajax invoker will stop when the process is finished.
     * 
     * @param p_response
     * @param sessionMgr
     * @param status
     * @throws IOException
     */
    public void refreshProcessStatus(HttpServletResponse p_response,
            SessionManager sessionMgr, OEMProcessStatus status)
            throws IOException
    {
        // ProcessStatus m_status =
        // (ProcessStatus)sessionMgr.getAttribute(WebAppConstants.UPLOAD_STATUS);
        p_response.setContentType("text/html");
        if (status != null)
        {
            String errorMsg = (String) status.getResults();
            sessionMgr.setAttribute(OfflineConstants.ERROR_MESSAGE, errorMsg);
            int percentage = status.getPercentage();
            List<Long> taskIdList = new ArrayList<Long>();
            Set<Long> taskIds = new HashSet<Long>();
            taskIdList.addAll(status.getTaskIdList());
            taskIds.addAll(status.getTaskIds());

            Map map = new HashMap();
            map.put("percentage", percentage);
            map.put("counter", status.getCounter());
            map.put("msg", status.giveMessages());
            map.put("taskIdsSet", status.getTaskIds());
            List<String> list = new ArrayList<String>();
            Task task = null;
            ProjectImpl project = null;
            String str = null;
            if (taskIdList != null && taskIdList.size() > 0)
            {
                for (long taskId : taskIdList)
                {
                    task = (Task) HibernateUtil.get(TaskImpl.class, taskId);
                    if (task != null)
                    {
                        project = (ProjectImpl) task.getWorkflow().getJob()
                                .getProject();
                        boolean isCheckUnTranslatedSegments = project
                                .isCheckUnTranslatedSegments();
                        str = taskId + "," + isCheckUnTranslatedSegments;
                        list.add(str);
                    }
                }
            }
            else
            {
                if (taskIds != null && taskIds.size() > 0)
                {
                    for (long taskId : taskIds)
                    {
                        task = (Task) HibernateUtil.get(TaskImpl.class, taskId);
                        if (task != null)
                        {
                            project = (ProjectImpl) task.getWorkflow().getJob()
                                    .getProject();
                            boolean isCheckUnTranslatedSegments = project
                                    .isCheckUnTranslatedSegments();
                            str = taskId + "," + isCheckUnTranslatedSegments;
                            list.add(str);
                        }
                    }
                }
            }
            map.put("taskIsCheckUnTran", list);
            map.put("errMsg", errorMsg);
            if (status.getCheckResult() != null)
            {
                map.put("internalTagMiss",
                        status.getCheckResult().getMessage(true));
                status.setCheckResult(null);
            }

            map.put("process", status.isUseProcess() ? status.getProcess()
                    + "%" : null);

            ServletOutputStream out = p_response.getOutputStream();
            out.write(JsonUtil.toJson(map).getBytes("UTF-8"));
            out.close();
        }
    }

    public void translatedText(HttpServletRequest p_request,
            HttpServletResponse p_response) throws IOException
    {
        String parameter = p_request.getParameter("taskParam");
        long taskId = TaskHelper.getLong(parameter);
        Task task = null;
        try
        {
            task = TaskHelper.getTask(taskId);
        }
        catch (Exception e)
        {
            throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TRANSLATE_TEXT, null, e);
        }

        StringBuffer buffer = new StringBuffer();
        List<TargetPage> targetPages = task.getTargetPages();
        for (TargetPage tp : targetPages)
        {
            int percent = SegmentTuvUtil
                    .getTranslatedPercentageForTargetPage(tp.getId());
            String pageName = tp.getExternalPageId();
            buffer.append(pageName).append("<>").append(percent).append("||");
        }

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("jobId", task.getJobId());
        map.put("jobName", task.getJobName());
        map.put("taskId", buffer.toString());

        ServletOutputStream out = p_response.getOutputStream();
        out.write(JsonUtil.toJson(map).getBytes("UTF-8"));
        out.close();
    }

    // process uploaded file
    public void processRequest(HttpServletRequest p_request,
            OEMProcessStatus p_status) throws EnvoyServletException
    {
        OfflineEditManager OEM = null;
        File tempFile = null;
        HttpSession session = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // read the uploaded file
        MultipartFormDataReader reader = new MultipartFormDataReader();

        try
        {
            p_status = new OEMProcessStatus();
            OEM = (OfflineEditManager) ServerProxy.getOfflineEditManager();
            SystemResourceBundle srb = SystemResourceBundle.getInstance();
            ResourceBundle res = srb.getResourceBundle(
                    ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                    (Locale) p_request.getSession().getAttribute(
                            WebAppConstants.UILOCALE));
            p_status.addMessage(res.getString("msg_upld_copy_to_server"));

            tempFile = reader.uploadToTempFile(p_request);
            OEM.attachListener(p_status);
            sessionManager.setAttribute(UPLOAD_STATUS, p_status);
            sessionManager.setAttribute(UPLOAD_MANAGER, OEM);
            sessionManager.setAttribute("tempFile", tempFile);
            sessionManager.setAttribute("tempFileName", reader.getFilename());
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }

    }

    private void processReportFileContents(File p_tmpFile, User p_user,
            String p_fileName, HttpServletRequest p_request,
            OEMProcessStatus p_status, OfflineEditManager p_OEM,
            String p_reportName, String p_taskId) throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        try
        {
            Task task = null;
            Object workObj = TaskHelper.retrieveObject(session, WORK_OBJECT);
            if (workObj != null)
            {
                task = (Task) workObj;
            }

            if (task == null && !"".equals(p_taskId))
            {
                long id = Long.valueOf(p_taskId).longValue();
                task = ServerProxy.getTaskManager().getTask(id);
            }

            p_OEM.processUploadReportPage(p_tmpFile, p_user, task, p_fileName,
                    p_reportName);
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }
        finally
        {
            session = p_request.getSession(false);
            sessionManager = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);
            sessionManager.removeElement(OfflineConstants.ERROR_MESSAGE);
        }
    }

    /**
     * When generate TER, RCR, RCSR, task info will be added in "AA1" cell. Try
     * to get task relevant info from this cell now.
     */
    private String[] getReportInfoFromXlsx(String p_fileName, File p_file,
            HttpServletRequest p_request)
    {
        String[] result =
        { "", "", "" };
        try
        {
            String fileSuff = p_fileName.substring(p_fileName.lastIndexOf("."));
            File tmpFile = File.createTempFile("TEM_", fileSuff);
            FileUtils.copyFile(p_file, tmpFile);
            FileInputStream fis = new FileInputStream(tmpFile);
            Workbook workbook = ExcelUtil.getWorkbook(
                    tmpFile.getAbsolutePath(), fis);
            Sheet sheet = ExcelUtil.getDefaultSheet(workbook);
            String taskInfoReport = ExcelUtil.getCellValue(sheet, 0, 26);
            String titleInfo = ExcelUtil.getCellValue(sheet, 0, 0);

            String taskId = "";
            String isReport = "";
            String reportType = "";
            if (taskInfoReport != null && taskInfoReport.length() > 0)
            {
                String[] infos = taskInfoReport.split("_");
                String reportInfo = infos[0];
                if (reportInfo
                        .equals(ReportConstants.TRANSLATIONS_EDIT_REPORT_ABBREVIATION))
                {
                    reportType = WebAppConstants.TRANSLATION_EDIT;
                }
                else if (reportInfo
                        .equals(ReportConstants.REVIEWERS_COMMENTS_SIMPLE_REPORT_ABBREVIATION)
                        || reportInfo
                                .equals(ReportConstants.REVIEWERS_COMMENTS_REPORT_ABBREVIATION))
                {
                    reportType = WebAppConstants.LANGUAGE_SIGN_OFF;
                }
                else if (reportInfo
                        .equals(ReportConstants.POST_REVIEW_REPORT_ABBREVIATION))
                {
                    reportType = WebAppConstants.POST_REVIEW_QA;
                }
                else if (reportInfo
                        .equals(ReportConstants.TRANSLATIONS_VERIFICATION_REPORT_ABBREVIATION))
                {
                    reportType = WebAppConstants.TRANSLATION_VERIFICATION;
                }

                if (reportType != null && !"".equals(reportType))
                {
                    isReport = "yes";
                    taskId = infos[1];
                }
            }
            // Report file generated before 8.5.8 build has no task info in
            // "AA1" cell, check "A1" cell for further confirmation.
            if (!"yes".equals(isReport))
            {
                SystemResourceBundle srb = SystemResourceBundle.getInstance();
                ResourceBundle res = srb.getResourceBundle(
                        ResourceBundleConstants.LOCALE_RESOURCE_NAME,
                        (Locale) p_request.getSession().getAttribute(
                                WebAppConstants.UILOCALE));
                if (res.getString("review_reviewers_comments")
                        .equals(titleInfo)
                        || res.getString("review_reviewers_comments_simple")
                                .equals(titleInfo)
                        || res.getString("lb_translation_edit_report").equals(
                                titleInfo))
                {
                    isReport = "yes";
                }

                if (res.getString("review_reviewers_comments")
                        .equals(titleInfo)
                        || res.getString("review_reviewers_comments_simple")
                                .equals(titleInfo))
                {
                    reportType = WebAppConstants.LANGUAGE_SIGN_OFF;
                }
                else if (res.getString("lb_translation_edit_report").equals(
                        titleInfo))
                {
                    reportType = WebAppConstants.TRANSLATION_EDIT;
                }
                else if (res.getString("lb_post_review_qa_report").equals(
                        titleInfo))
                {
                    reportType = WebAppConstants.POST_REVIEW_QA;
                }
                else if (res.getString("lb_translation_verification_report")
                        .equals(titleInfo))
                {
                    reportType = WebAppConstants.TRANSLATION_VERIFICATION;
                }
            }
            result[0] = isReport;
            result[1] = reportType;
            result[2] = taskId;
        }
        catch (IOException e)
        {
            CATEGORY.warn("Error happens when try to detect xlsx file, maybe this xlsx file is not a report file."
                    + e.getMessage());
        }
        return result;
    }

    /**
     * The parameter "taskId" may be not the current task, need locate current
     * task.
     * 
     * @param p_taskId
     * @return
     */
    private Task locateCurrentTask(long p_taskId)
    {
        Task task = null;
        try
        {
            long id = Long.valueOf(p_taskId).longValue();
            task = ServerProxy.getTaskManager().getTask(id);
            Collection tasks = ServerProxy.getTaskManager().getCurrentTasks(
                    task.getWorkflow().getId());
            if (tasks != null)
            {
                for (Iterator it = tasks.iterator(); it.hasNext();)
                {
                    task = (Task) it.next();
                    if (task.getState() == Task.STATE_ACCEPTED)
                    {
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {

        }
        return task;
    }

    /**
     * Process file contests control method
     * 
     * @param p_tmpFile
     * @param p_user
     * @param p_fileName
     * @param p_request
     * @param p_status
     * @param p_OEM
     * @throws EnvoyServletException
     */
    private void processFileContents(boolean fromTaskUploadPage,
            File p_tmpFile, User p_user, String p_fileName,
            HttpServletRequest p_request, OEMProcessStatus p_status,
            OfflineEditManager p_OEM) throws EnvoyServletException
    {
        // TODO: Now we need to return a result "object" containing this flag
        // and
        // the error page. For now we force the Cache to refresh itself no
        // matter if the uploaded file we extracted or not.

        HttpSession session = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        try
        {
            Task task = null;
            if (fromTaskUploadPage)
            {
                task = getTask(p_request);
                CATEGORY.debug("THE TASK ID WE ARE ATTEMPING TO UPLOAD INTO IS "
                        + task.getId());
            }
            p_OEM.processUploadPage(p_tmpFile, p_user, task, p_fileName);
        }
        catch (Exception e)
        {
            CATEGORY.error(e.getMessage(), e);
            throw new EnvoyServletException(e);
        }
        finally
        {
            session = p_request.getSession(false);
            sessionManager = (SessionManager) session
                    .getAttribute(SESSION_MANAGER);
            sessionManager.removeElement(OfflineConstants.ERROR_MESSAGE);
        }
    }

    private Task getTask(HttpServletRequest p_request)
            throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        Task task = (Task) TaskHelper.retrieveObject(session, WORK_OBJECT);
        if (task == null)
        {
            EnvoyServletException e = new EnvoyServletException("TaskNotFound",
                    null, null);
            CATEGORY.error(e.getMessage(), e);
            throw e;
        }
        return task;
    }
}
