package com.globalsight.everest.webapp.pagehandler.offline.upload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.edit.offline.OfflineEditManager;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.tuv.SegmentTuvUtil;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskBO;
import com.globalsight.everest.taskmanager.TaskException;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.generator.Cancelable;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
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
        String action;
        if (contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            processRequest(p_request, status);
            // now update the task in the session
            try
            {
                User user = TaskHelper.getUser(httpSession);
                long taskId = ((Task) TaskHelper.retrieveObject(httpSession,
                        WORK_OBJECT)).getId();
                action = (String) TaskHelper.retrieveObject(httpSession,
                        TASK_STATE);
                TaskImpl task = (TaskImpl) TaskHelper.getTask(user.getUserId(), taskId, Integer.parseInt(action));
                TaskHelper.storeObject(httpSession, WebAppConstants.WORK_OBJECT, task);
                
                // Set task uploadStatus
                TaskBO taskBO = new TaskBO(task.getId());
                taskBO.setUploadStatus(OfflineConstants.TASK_UPLOADSTATUS_UPLOADING);
                Map<Long, TaskBO> taskBOMap = new HashMap<Long, TaskBO>();
                taskBOMap.put(taskBO.getId(),taskBO);
                TaskHelper.storeObject(httpSession, SESSION_MAP_TASKBO, taskBOMap);
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
    	OfflineEditManager oem = (OfflineEditManager) sessionMgr.getAttribute(UPLOAD_MANAGER);
    	
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
    public void uploadProcess(HttpServletRequest p_request,
            HttpServletResponse p_response, SessionManager sessionMgr)
    {
        OEMProcessStatus status;
        OfflineEditManager OEM;
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
        String fileName = (String) sessionMgr.getAttribute("tempFileName");
        File file = (File) sessionMgr.getAttribute("tempFile");
        String isReport = (String) sessionMgr.getAttribute("isReport");
        if (isReport != null && isReport.equals("yes"))
        {
            String reportType = (String) sessionMgr.getAttribute(REPORT_TYPE);
            processReportFileContents(file, user, fileName, p_request, status,
                    OEM, reportType);
            sessionMgr.removeElement("isReport");
            sessionMgr.removeElement(REPORT_TYPE);
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
        StringBuffer sb = new StringBuffer();
        String errorMsg = (String) status.getResults();
        sessionMgr.setAttribute(OfflineConstants.ERROR_MESSAGE, errorMsg);
        p_response.setContentType("text/html");
        if (status != null)
        {
            int percentage = status.getPercentage();
            
            Map map = new HashMap();
            map.put("percentage", percentage);
            map.put("counter", status.getCounter());
            map.put("msg", status.giveMessages());
            map.put("taskIdsSet", status.getTaskIds());
            map.put("errMsg", errorMsg);
            if (status.getCheckResult() != null)
            {
            	map.put("internalTagMiss", status.getCheckResult().getMessage(true));
            	status.setCheckResult(null);
            }
            
            map.put("process", status.isUseProcess() ?  status.getProcess() + "%" : null);            
            
            PrintWriter out = p_response.getWriter();
            out.write(JsonUtil.toJson(map));
            out.close();
        }
    }

    public void translatedText(HttpServletRequest p_request, 
    		HttpServletResponse p_response)throws IOException
    {
    	String parameter = p_request.getParameter("taskParam");
    	long taskId = TaskHelper.getLong(parameter);
        Task task = null;
        try
        {
            task = TaskHelper.getTask(taskId);
        }
        catch(Exception e){
        	throw new TaskException(
                    TaskException.MSG_FAILED_TO_GET_TRANSLATE_TEXT,null, e);
        }
        List<TargetPage> targetPages = task.getTargetPages();
        List<Long> targetPageIds = new ArrayList<Long>();
        List<String> targetPageNames = new ArrayList<String>();
        Map<String, Object> map = new HashMap<String, Object>();
        Long jobId = task.getJobId();
        String jobName = task.getJobName();
        for (TargetPage tp : targetPages)
        {
            targetPageIds.add(tp.getIdAsLong());
            targetPageNames.add(tp.getExternalPageId());
        }
        StringBuffer buffer = new StringBuffer();
        for(int i = 0 ; i < targetPageIds.size();i++)
        {
            String percent = SegmentTuvUtil
                    .getTranslatedPercentage(targetPageIds.get(i));
        	String pageNames = targetPageNames.get(i);
        	buffer.append(pageNames).append("<>").append(percent).append("||");
        }
        map.put("jobId", jobId);
        map.put("jobName", jobName);
        map.put("taskId", buffer.toString());
        PrintWriter out = p_response.getWriter();
        out.write(JsonUtil.toJson(map));
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

        String reportName = (String) p_request.getParameter(REPORT_TYPE);
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
            if (reportName != null)
            {
                sessionManager.setAttribute("isReport", "yes");
                sessionManager.setAttribute(REPORT_TYPE, reportName);
            }
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
            String p_reportName) throws EnvoyServletException
    {
        HttpSession session = p_request.getSession(false);
        SessionManager sessionManager = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        try
        {
            Task task = getTask(p_request);
            CATEGORY.debug("THE TASK ID WE ARE ATTEMPING TO UPLOAD INTO IS "
                    + task.getId());

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
