package com.globalsight.everest.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.SearchCriteriaParameters;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.localemgr.LocaleManagerWLRemote;
import com.globalsight.everest.util.comparator.CompanyComparator;
import com.globalsight.everest.util.comparator.GlobalSightLocaleComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.util.GlobalSightLocale;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportBatchEvent;
import com.globalsight.everest.page.pageexport.ExportEventObserverHelper;
import com.globalsight.everest.page.pageexport.ExportParameters;
import com.globalsight.everest.page.pageexport.ExportingPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.qachecks.DITAQACheckerHelper;
import com.globalsight.everest.qachecks.QACheckerHelper;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.taskmanager.TaskSearchParameters;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.customer.download.DownloadFileHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.webapp.pagehandler.projects.l10nprofiles.LocProfileHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.projects.workflows.WorkflowHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskListConstants;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskListParams;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskSearchUtil;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskVo;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;

public class TaskListServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private Logger logger = Logger.getLogger(TaskListServlet.class);
    private static ResourceBundle bundle = null;
    
    public void service(HttpServletRequest request, HttpServletResponse response) {
        PrintWriter out;
        try
        {
            HttpSession session = request.getSession();
            response.setCharacterEncoding("UTF-8");
            request.setCharacterEncoding("UTF-8");
            out = response.getWriter();
            if(isLoginSession(session, request))
            {
                bundle = PageHandler.getBundle(session);
                SessionManager sessionManager = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
                User user = (User) sessionManager.getAttribute(WebAppConstants.USER);
                PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
                
                CompanyThreadLocal.getInstance().setValue(user.getCompanyName());
                
                String result = "";
                TaskListParams params = getTaskListParams(request, sessionManager);
                params.setProjectManager(TaskSearchUtil.isProjectManager(user));
                
                String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
                boolean isSuperPm = ServerProxy.getUserManager().containsPermissionGroup(
                        userId, WebAppConstants.SUPER_PM_NAME);
                if (user.getCompanyName().equals(CompanyWrapper.getSuperCompanyName()) && !isSuperPm)
                    params.setSuperUser(true);
                
                if ("getHelpInfo".equals(params.getAction())) 
                {
                    result = getHelpInfo(params.getTaskState());
                } 
                else if ("checkUploadingStatus".equals(params.getAction())) 
                {
                    result = checkUploadingStatus(request.getParameter("taskId"),
                            request.getParameter("jobId"),request.getParameter("workflowId"),
                            request.getParameter("exportFrom"));
                } 
                else if ("export".equals(params.getAction())) 
                {
                    result =  export(request.getParameter("taskId"),
                            request.getParameter("jobId"),request.getParameter("workflowId"),
                            request.getParameter("exportFrom"), request);
                } 
                else if ("download".equals(params.getAction())) 
                {
                    List<String> selectFiles = getDownloadFiles(request.getParameter("taskId"),
                            request.getParameter("jobId"),request.getParameter("workflowId"),
                            request.getParameter("exportFrom"), request);
                    JSONObject object = new JSONObject();
                    if(selectFiles == null || selectFiles.size() == 0)
                    {
                        object.put("selectFiles", "");
                        object.put("percent", "0");
                    }
                    else
                    {
                        try
                        {
                            Float.parseFloat(selectFiles.get(0));
                            object.put("selectFiles", "");
                            object.put("percent", selectFiles.get(0));
                        }
                        catch (Exception e) 
                        {
                            object.put("selectFiles", JSON.toJSON(selectFiles));
                            object.put("percent","100");
                        }
                    }
                    result = object.toJSONString();
                } 
                else if ("getButtonStatus".equals(params.getAction())) 
                {
                    result = getButtonStatus(params.getTaskState(), perms, user);
                } 
                else if ("getFilterOptions".equals(params.getAction())) 
                {
                    result = getFilterOptions(user, params);
                } 
                else if ("getContextForTab".equals(params.getAction())) 
                {
                    String taskId = request.getParameter("taskId");
                    result = getContextForTab(taskId, perms, session);
                }
                else 
                {
                    
                    List<TaskVo> tasks = getTasks(user, params);
                    
                    JSONObject object = new JSONObject();
                    object.put("total", params.getTotalRecords());
                    object.put("begin", params.getStartRecord());
                    object.put("end", params.getEndRecord());
                    object.put("totalPages", params.getPageCount());
                    object.put("pageNumber", params.getPageNumber());
                    object.put("perPageCount", params.getPerPageCount());
                    object.put("tasks", JSON.toJSON(tasks));
                    
                    result = object.toJSONString();
                }            
                
                sessionManager.setMyactivitiesAttribute(TaskListConstants.TASK_LIST_PARAMS, params);
                
                out.print(result);
            }
            else 
            {
                out.print(JSON.toJSONString("sessionTimeout"));
            }
        }
        catch (IOException e)
        {
            logger.error("Error found", e);
        }
    }

    private String getFilterOptions(User user, TaskListParams params) {
        JSONArray array = null;
        JSONObject options = new JSONObject();
        JSONObject object = null;

        //Get source/target locales according by locale pairs.
        LocaleManagerWLRemote localeManager = null;
        Locale locale = params.getUILocale();
        try {
            localeManager = ServerProxy.getLocaleManager();
            Vector data = null;
            data = localeManager.getAllSourceLocales();
            array = getLocalesInJSON(data, locale);
            if (array != null)
                options.put("sourceLocales", array);

            data = localeManager.getAllTargetLocales();
            array = getLocalesInJSON(data, locale);
            if (array != null)
                options.put("targetLocales", array);

            if (params.isSuperUser()) {
                ArrayList<Company> companies =  new ArrayList<Company>(ServerProxy.getJobHandler().getAllCompanies());
                if (companies != null && companies.size() > 0) {
                    SortUtil.sort(companies, new CompanyComparator(locale));
                    array = new JSONArray();
                    for (Company company : companies) {
                        object = new JSONObject();
                        object.put("id", company.getId());
                        object.put("name", company.getCompanyName());
                        array.add(object);
                    }
                    options.put("companies", array);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot get source/target locale successfully.", e);
        }
        return options.toJSONString();
    }

    private JSONArray getLocalesInJSON(Vector data, Locale locale) {
        JSONArray array = null;
        JSONObject object = null;
        if (data != null && data.size() > 0) {
            SortUtil.sort(data, new GlobalSightLocaleComparator(
                    GlobalSightLocaleComparator.DISPLAYNAME, locale));
            array = new JSONArray();
            for (int i = 0; i < data.size(); i++) {
                GlobalSightLocale gsl = (GlobalSightLocale) data.get(i);
                object = new JSONObject();
                object.put("id", gsl.getId());
                object.put("name", gsl.getDisplayName());
                array.add(object);
            }
        }
        return array;
    }

    private String getContextForTab(String taskIdString, PermissionSet perms,
            HttpSession p_session)
    {
        JSONObject object = new JSONObject();
        
        if (StringUtil.isEmpty(taskIdString))
            return object.toJSONString();
        
        long taskId = -1L;
        try
        {
            taskId = Long.parseLong(taskIdString);
        }
        catch (Exception e)
        {
            return object.toJSONString();
        }
        
        boolean isPageDetailOne = true;
        boolean disableButtons = false;
        boolean reviewOnly = false;
        boolean workOffline = perms.getPermissionFor(Permission.ACTIVITIES_WORKOFFLINE);
        boolean isShowComments = perms.getPermissionFor(Permission.ACTIVITIES_JOB_COMMENTS_VIEW) || perms.getPermissionFor(Permission.ACTIVITIES_COMMENTS_VIEW);
        boolean isReviewerCommentsReport = perms.getPermissionFor(Permission.REPORTS_LANGUAGE_SIGN_OFF);
        try
        {
            Task task = ServerProxy.getTaskManager().getTask(taskId);
            int taskState = task.getState();
            reviewOnly = task.isType(Task.TYPE_REVIEW);
            switch (task.getState()) {
                case Task.STATE_ACCEPTED:
                    isPageDetailOne = false;
                    break;
                case Task.STATE_DISPATCHED_TO_TRANSLATION:
                    isPageDetailOne = false;
                    disableButtons = true;
                    break;
                case Task.STATE_IN_TRANSLATION:
                    isPageDetailOne = false;
                    disableButtons = true;
                    break;
                case Task.STATE_TRANSLATION_COMPLETED:
                    isPageDetailOne = false;
                    break;
                case Task.STATE_REDEAY_DISPATCH_GSEDTION:
                    isPageDetailOne = false;
                    disableButtons = true;
                    break;
                default:
                    break;
            }
            TaskImpl taskImpl = HibernateUtil.get(TaskImpl.class, taskId);
            TaskSearchUtil.setAllAssignees(taskImpl);
            boolean isAssignee = new Boolean(taskImpl.getAllAssignees().contains(
                            (String)p_session.getAttribute(WebAppConstants.USER_NAME)));
            if (!isAssignee)
            {
                disableButtons = true;
            }
            
            String workOfflineUrl = "";
            String secondaryTargetFilesUrl = "";
            String scorecardUrl = "";
            boolean isShowScorecard = task.getWorkflow().getScorecardShowType() == -1? false: true;
            boolean realShowScorecard = false;
            if (!isPageDetailOne) 
            {
                if (!disableButtons && workOffline) 
                {
                    if (reviewOnly)
                    {
                        if(isReviewerCommentsReport)
                            workOfflineUrl = "/globalsight/ControlServlet?linkName=downloadreport&pageName=TK2&state=" + taskState + "&taskId=" + taskId;
                    }
                    else
                    {
                        workOfflineUrl = "/globalsight/ControlServlet?linkName=download&pageName=TK2&state=" + taskState + "&taskId=" + taskId;
                    }
                }
                
                if(!disableButtons)
                {
                    if(reviewOnly && isShowScorecard)
                    {
                        realShowScorecard = true;
                        scorecardUrl = "/globalsight/ControlServlet?linkName=taskScorecard&pageName=TK2&taskAction=scorecard&state=" + taskState + "&taskId=" + taskId;
                    }
                }
            }
            if(!realShowScorecard)
            {
                isShowScorecard = false;
            }
            boolean isShowSecondTargetFile = !perms.getPermissionFor(Permission.ACTIVITIES_SECONDARYTARGETFILE);
            if (isShowSecondTargetFile) {
                Set<SecondaryTargetFile> stfs = task.getWorkflow().getSecondaryTargetFiles();
                int stfSize = stfs == null ? 0 : stfs.size();
                String stfCreationState = task.getStfCreationState();
                if ((stfCreationState != null && stfCreationState.length() > 0) || stfSize > 0)
                {
                    secondaryTargetFilesUrl = "/globalsight/ControlServlet?linkName=taskSecondaryTargetFiles&pageName=TK2&taskAction=getTask&state=" + taskState + "&taskId=" + taskId; 
                }
            }

            String commentUrl = "/globalsight/ControlServlet?linkName=comment&pageName=TK2&taskAction=getTask&state=" + taskState + "&taskId=" + taskId;
            
            object.put("workOfflineUrl", workOfflineUrl);
            object.put("secondaryTargetFilesUrl", secondaryTargetFilesUrl);
            object.put("commentUrl", commentUrl);
            object.put("scorecardUrl", scorecardUrl);
            object.put("isShowScorecard", isShowScorecard);
            object.put("isShowComment", isShowComments);
            object.put("targetFilesLabel", bundle.getString("lb_TargetFiles"));
            object.put("secondaryTargetFilesLabel", bundle.getString("lb_secondary_target_files"));
            object.put("commentLabel", bundle.getString("lb_comments"));
            object.put("workOfflineLabel", bundle.getString("lb_work_offline"));
            object.put("scorecardLabel", bundle.getString("lb_scorecard"));

            // QA Checks
            boolean isShowQaChecks = (disableButtons == false)
                    && task.getState() == Task.STATE_ACCEPTED
                    && (QACheckerHelper.isShowQAChecksTab(task) || DITAQACheckerHelper
                            .isShowDITAChecksTab(task));
            object.put("isShowQaChecks", isShowQaChecks);
            object.put("qaChecksUrl", getQAChecksUrl(task));
            object.put("qaChecksLabel", bundle.getString("lb_activity_qa_checks"));
        }
        catch (Exception e)
        {
            logger.error("Cannot get context info for tab.", e);
        }
        
        return object.toJSONString();
    }

    private String getQAChecksUrl(Task p_task)
    {
        StringBuffer url = new StringBuffer();
        int state = p_task.getState();
        long taskId = p_task.getId();
        if (QACheckerHelper.isShowQAChecksTab(p_task))
        {
            url.append("/globalsight/ControlServlet?linkName=downloadQAReport&pageName=QA_downloadQAReport&state=").append(state).append("&taskId=").append(taskId);
        }
        else if (DITAQACheckerHelper.isShowDITAChecksTab(p_task))
        {
            url.append("/globalsight/ControlServlet?linkName=downloadDitaReport&pageName=QA_downloadDitaReport&state=").append(state).append("&taskId=").append(taskId);
        }
        return url.toString();
    }

    private String checkUploadingStatus(String taskId, String jobId, String workflowIds, String exportFrom) {
        JSONObject obj = new JSONObject();
        String wfId = "-1";
        boolean isUploading = false;
        if (exportFrom.equals("task")) 
        {
            try {
                TaskImpl task = HibernateUtil.get(TaskImpl.class, Long.parseLong(taskId));
                if (task != null)
                {
                    wfId = String.valueOf(task.getWorkflow().getId());
                    if(task.getIsUploading() == 'Y')
                    {
                        isUploading = true;
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot find workflow by task id [" + taskId + "]", e);
            }
        }
        else if (exportFrom.equals("jobWorkflow"))
        {
            for(String workflowId : StringUtil.split(workflowIds, " "))
            {
                if(isUploading == true)
                    break;
                try 
                {
                    Workflow workflow = ServerProxy.getWorkflowManager().getWorkflowById(Long.parseLong(workflowId));
                    Hashtable tasks = workflow.getTasks();
                    for (Iterator<Long> ids = tasks.keySet().iterator(); ids.hasNext();)
                    {
                        long theTaskId = ids.next();
                        if(ServerProxy.getTaskManager().getTask(theTaskId).getIsUploading() == 'Y')
                        {
                            isUploading = true;
                            break;
                        }
                    }
                } 
                catch (Exception e)
                {
                }
            }
        }
        else //exportFrom.equals("jobList")
        {
            try 
            {
                Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(jobId));
                for(Workflow workflow: job.getWorkflows())
                {
                    if(isUploading == true)
                        break;
                    Hashtable tasks = workflow.getTasks();
                    for (Iterator<Long> ids = tasks.keySet().iterator(); ids.hasNext();)
                    {
                        long theTaskId = ids.next();
                        try
                        {
                            if(ServerProxy.getTaskManager().getTask(theTaskId).getIsUploading() == 'Y')
                            {
                                isUploading = true;
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            } 
            catch (Exception e) 
            {
                logger.error("Cannot find workflow by task id [" + taskId + "]", e);
            }
        }
        obj.put("workflowId", wfId);
        if(isUploading)
        {
            obj.put("isUploading", true);
        }
        else
        {
            obj.put("isUploading", false);
        }

        return obj.toJSONString();
    }

    private String export(String taskId, String jobId, String workflowIds,  String exportFrom, HttpServletRequest p_request)
    {
        Date startExportDate = new Date();
        try 
        {
            HttpSession session = p_request.getSession(true);
            Job job;
            List<Workflow> workflows = new ArrayList<Workflow>();
            if(exportFrom.equals("task"))
            {
                Task task = ServerProxy.getTaskManager().getTask(Long.valueOf(taskId));
                Workflow workflow = task.getWorkflow();
                workflows.add(workflow);
                job = workflow.getJob();
            }
            else if(exportFrom.equals("jobWorkflow"))
            {
                job = ServerProxy.getJobHandler().getJobById(Long.valueOf(jobId));
                for(String workflowId: StringUtil.split(workflowIds, " "))
                {
                    Workflow workflow = ServerProxy.getWorkflowManager().getWorkflowById(Long.parseLong(workflowId));
                    workflows.add(workflow);
                }
            }
            else //exportFrom.equals("jobList")
            {
                job = ServerProxy.getJobHandler().getJobById(Long.valueOf(jobId));
                for(Workflow workflow:job.getWorkflows())
                {
                    workflows.add(workflow);
                }
            }
            
            final User user = PageHandler.getUser(session);
            SystemConfiguration config = SystemConfiguration.getInstance();
            String exportLocation = config.getStringParameter(SystemConfigParamNames.CXE_DOCS_DIR);
            int bomType = job.getFileProfile().getBOMType();
            for(Workflow workflow: workflows)
            {
                if(WorkflowExportingHelper.isExporting(workflow.getId()))
                {
                    logger.info("Ignored re-exporting request from workflow(id=" + workflow.getId()
                            + ") since it is being exported.");
                    continue;
                }
                
                String localeSubDir = workflow.getTargetLocale().toString();
                
                L10nProfile l10nProfile = LocProfileHandlerHelper.getL10nProfile(job.getL10nProfileId());
                String codeSet = l10nProfile.getCodeSet(workflow.getTargetLocale());
                
                ExportParameters ep = new ExportParameters(workflow, codeSet,
                        exportLocation, localeSubDir, bomType);
                ep.setXlfSrcAsTrg(0);
                
                ArrayList<Long> workflowIdList = new ArrayList<Long>();
                workflowIdList.add(workflow.getId());
                WorkflowExportingHelper.setAsExporting(workflowIdList);
                String exportType = workflow.getState().equals(Workflow.LOCALIZED) ? ExportBatchEvent.FINAL_PRIMARY
                        : ExportBatchEvent.INTERIM_PRIMARY;
                List pageIds = new ArrayList();
                for(TargetPage tp :workflow.getTargetPages()) 
                {
                    pageIds.add(tp.getId());
                }
                long exportBatchId =ExportEventObserverHelper
                .notifyBeginExportTargetBatch(job, user, pageIds,
                        workflowIdList, null, exportType);
                
                WorkflowHandlerHelper.exportPage(ep, pageIds, true,
                        exportBatchId);
            }
            
        } 
        catch (Exception e) 
        {
            logger.error("Cannot get context info for tab.", e);
        }
        
        JSONObject obj = new JSONObject();
        obj.put("startExportDate", startExportDate.getTime());
        return obj.toJSONString();
    }
    
    private List<String> getDownloadFiles(String taskId, String jobId, String workflowIds,  String exportFrom, HttpServletRequest p_request)
    {
        int totalPagesNum = 0;
        int exportPageNum = 0;
        boolean exportEnd = true;
        try 
        {
            HttpSession session = p_request.getSession(true);
            SessionManager sessionMgr = (SessionManager) session
                    .getAttribute(WebAppConstants.SESSION_MANAGER);
            List<Workflow> workflows = new ArrayList<Workflow>();
            Job job;
            if(exportFrom.equals("task"))
            {
                Task task = ServerProxy.getTaskManager().getTask(Long.valueOf(taskId));
                Workflow workflow = task.getWorkflow();
                workflows.add(workflow);
                job = workflow.getJob();
                totalPagesNum = totalPagesNum + job.getSourcePages().size();
            }
            else if(exportFrom.equals("jobWorkflow"))
            {
                job = ServerProxy.getJobHandler().getJobById(Long.valueOf(jobId));
                for(String workflowId: StringUtil.split(workflowIds, " "))
                {
                    Workflow workflow = ServerProxy.getWorkflowManager().getWorkflowById(Long.parseLong(workflowId));
                    workflows.add(workflow);
                    totalPagesNum = totalPagesNum + job.getSourcePages().size();
                }
            }
            else //exportFrom.equals("jobList")
            {
                job = ServerProxy.getJobHandler().getJobById(Long.valueOf(jobId));
                for(Workflow workflow:job.getWorkflows())
                {
                    workflows.add(workflow);
                    totalPagesNum = totalPagesNum + job.getSourcePages().size();
                }
            }
            
            sessionMgr.setAttribute(DownloadFileHandler.PARAM_JOB_COMPANY_ID, job.getCompanyId());
            HashSet<Long> downloadJobIds = new HashSet<Long>();
            downloadJobIds.add(job.getId());
            sessionMgr.setAttribute(DownloadFileHandler.PARAM_DOWNLOAD_NAME, downloadJobIds);
            
            List<String> selectFiles = new ArrayList<String>();
            for(Workflow workflow: workflows)
            {
                Vector<TargetPage> targetPages = workflow.getTargetPages();
                for(TargetPage tp: targetPages)
                {
                    ExportingPage ep = getExportingPage(tp.getId());
                    if(ep == null)
                    {
                        exportPageNum ++;
                    }
                    else 
                    {
                        if(ep.getExportPath() != null)
                        {
                            exportPageNum ++;
                        }
                        else 
                        {
                            exportEnd = false;
                        }
                    }
                }
                String localeSubDir = workflow.getTargetLocale().toString();
                List<File> downloadFiles = getDownloadFiles(job, localeSubDir);
                String tempPrefix = localeSubDir + File.separator + job.getId();
                String tempWebservicePrefix = localeSubDir + File.separator + "webservice" 
                                                + File.separator + job.getId();
                String tempPassoloPrefix = "passolo" + File.separator + job.getId();
                String tempPassoloWebservicePrefix = "passolo" + File.separator + "webservice" 
                                                + File.separator + job.getId();
                for(File file: downloadFiles)
                {
                    String filePath = file.getPath();
                    if(filePath.indexOf(tempPrefix) > 0)
                    {
                        selectFiles.add(filePath.substring(filePath.indexOf(tempPrefix)));
                    }
                    else if(filePath.indexOf(tempWebservicePrefix) > 0)
                    {
                        selectFiles.add(filePath.substring(filePath.indexOf(tempWebservicePrefix)));
                    }
                    else if(filePath.indexOf(tempPassoloPrefix) > 0)
                    {
                        String tempPath = filePath.substring(filePath.indexOf(tempPassoloPrefix));
                        if(!selectFiles.contains(tempPath))
                        {
                            selectFiles.add(tempPath);
                        }
                    }
                    else if(filePath.indexOf(tempPassoloWebservicePrefix) > 0)
                    {
                        String tempPath = filePath.substring(filePath.indexOf(tempPassoloWebservicePrefix));
                        if(!selectFiles.contains(tempPath))
                        {
                            selectFiles.add(tempPath);
                        }
                    }
                }
            }
            if(exportEnd == true)
            {
                sessionMgr.setAttribute(DownloadFileHandler.PARAM_UPLOAD_NAME, job.getJobName());
                return selectFiles;
            }
            else 
            {
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setMaximumFractionDigits(0);
                String result = numberFormat.format((float)exportPageNum/(float)totalPagesNum*100);
                selectFiles.clear();
                selectFiles.add(result);
                return selectFiles;
            }
        } 
        catch (Exception e) 
        {
            logger.error("Cannot get context info for tab.", e);
        }
        return null;
    }
    
    private List<File> getDownloadFiles(Job job, String localeSubDir)
    {   
        List<File> targetFiles = new ArrayList<File>();
        List<File> xlzFolders = new ArrayList<File>();
        List<File> ignoreXlfFiles = new ArrayList<File>();
        String companyFolderPath = AmbFileStoragePathUtils.getCxeDocDirPath(job.getCompanyId());
        File companyFolder = new File(companyFolderPath);
        File[] localeFloders = companyFolder.listFiles();
        for (File localeFolder : localeFloders)
        {
            if(!localeFolder.getPath().endsWith(File.separator + localeSubDir) 
                    && !localeFolder.getPath().endsWith(File.separator + "passolo"))
            {
                continue;
            }
            String jobFolderPath = localeFolder.getPath() + File.separator + job.getId();
            String webserviceJobFolderPath = localeFolder.getPath() + File.separator + "webservice" 
                                                + File.separator + job.getId();
            File jobFolder = new File(jobFolderPath);
            if(!jobFolder.exists())
            {
                jobFolder = new File(webserviceJobFolderPath);
                if(!jobFolder.exists())
                {
                    continue;
                }
            }
            File[] fileList = jobFolder.listFiles();
            if(fileList == null || fileList.length == 0)
            {
                continue;
            }
            
            setTargetFiles(jobFolder,targetFiles,xlzFolders);
        }
        if(xlzFolders.size() > 0)
        {
            for(File xlzFloder: xlzFolders)
            {
                for(File file : targetFiles)
                {
                    if(file.getParentFile().equals(xlzFloder))
                    {
                        ignoreXlfFiles.add(file);
                    }
                }
            }
        }
        if(ignoreXlfFiles.size() > 0)
        {
            targetFiles.removeAll(ignoreXlfFiles);
        }
        return targetFiles;
    }
    
    private void setTargetFiles(File folder, List<File> targetFiles, List<File> xlzFolders)
    {
        File[] fileList = folder.listFiles();
        for(File file: fileList)
        {
            if(file.isFile())
            {
                // is eloqua obj file
                String name = file.getName();
                if (name.startsWith("(") && name.endsWith(".obj"))
                {
                    continue;
                }
                
                targetFiles.add(file);
                if(file.getName().endsWith(".xlz"))
                {
                    xlzFolders.add(new File(file.getPath().substring(0, file.getPath().length() - 4)));
                }
            }
            else if (!FileUtil.isEmpty(file))
            {
                setTargetFiles(file, targetFiles, xlzFolders);
            }
        }
    }
    
    private ExportingPage getExportingPage(Long targetPageId)
    {
        String hql = "from ExportingPage where page_id = " + targetPageId + "";
        return (ExportingPage) HibernateUtil.getFirst(hql);
    }

    private String getHelpInfo(int state) {
        JSONObject obj = new JSONObject();
        String stateString, helpText, helpFile, taskDateLabel;
        switch (state) {
            case 3: //Available
                taskDateLabel = bundle.getString("lb_accept_by");
                stateString = bundle.getString("lb_available");
                helpText = bundle.getString("helper_text_task_available");
                helpFile = bundle.getString("help_available_activities");
                break;
            case 8: //In Progress
                taskDateLabel = bundle.getString("lb_due_date");
                stateString = bundle.getString("lb_inprogress");
                helpText = bundle.getString("helper_text_task_inprogress");
                helpFile = bundle.getString("help_inprogress_activities");
                break;
            case -1: //Finish
                taskDateLabel = bundle.getString("lb_completed_on");
                stateString = bundle.getString("lb_finished");
                helpText = bundle.getString("helper_text_task_finished");
                helpFile = bundle.getString("help_finished_activities");
                break;
            case 6: //Reject
                taskDateLabel = bundle.getString("lb_due_date");
                stateString = bundle.getString("lb_rejected");
                helpText = bundle.getString("helper_text_task_rejected");
                helpFile = bundle.getString("help_rejected_activities");
                break;
            default: //All
                taskDateLabel = bundle.getString("lb_accept_by");
                stateString = bundle.getString("lb_all_status");
                helpText = "";
                helpFile = bundle.getString("help_all_status_activities");
                break;
        }
        obj.put("stateString", stateString);
        obj.put("helpText", helpText);
        obj.put("helpFile", helpFile);
        obj.put("taskDateLabel", taskDateLabel);
        
        return obj.toJSONString();
        
    }
    
    private List<TaskVo> getTasks(User user, TaskListParams params) {
        List<TaskVo> result = new ArrayList<TaskVo>();
        try
        {
            TaskSearchParameters sp = new TaskSearchParameters();
            int state = params.getTaskState();
            int pageNumber = params.getPageNumber();
            int rowsPerPage = params.getPerPageCount();
            int start = (pageNumber - 1) * rowsPerPage;
            
            sp.setActivityState(state);
            sp.setRowStart(start);
            sp.setRowPerPage(params.getPerPageCount());
            sp.setSortColumn(params.getSortColumn());
            sp.setSortType(params.isAscSort());
            HashMap<String,String> filters = params.getFilters();
            String tmp = "";
            
            tmp = filters.get("acceptanceStartFilter");
            if(StringUtil.isNotEmpty(tmp)){
                sp.setAcceptanceStart(new Integer(tmp));
                sp.setAcceptanceStartCondition(filters.get("acceptanceStartOptionsFilter"));
            }
            
            tmp = filters.get("acceptanceEndFilter");
            if(StringUtil.isNotEmpty(tmp)){
                sp.setAcceptanceEnd(new Integer(tmp));
                sp.setAcceptanceEndCondition(filters.get("acceptanceEndOptionsFilter"));
            }
            else if(SearchCriteriaParameters.NOW.equals(filters.get("acceptanceEndOptionsFilter")))
            {
                sp.setAcceptanceEndCondition(filters.get("acceptanceEndOptionsFilter"));
            }
            
            tmp = filters.get("completionStartFilter");
            if(StringUtil.isNotEmpty(tmp)){
                sp.setEstCompletionStart(new Integer(tmp));
                sp.setEstCompletionStartCondition(filters.get("completionStartOptionsFilter"));
            }
            
            tmp = filters.get("completionEndFilter");
            if(StringUtil.isNotEmpty(tmp)){
                sp.setEstCompletionEnd(new Integer(tmp));
                sp.setEstCompletionEndCondition(filters.get("completionEndOptionsFilter"));
            }
            else if(SearchCriteriaParameters.NOW.equals(filters.get("completionEndOptionsFilter")))
            {
                sp.setEstCompletionEndCondition(filters.get("completionEndOptionsFilter"));
            }
            
            
            tmp = filters.get("priorityFilter");
            if(StringUtil.isNotEmpty(tmp)){
                sp.setPriority(tmp);
            }
            tmp = filters.get("priorityFilter");
            if(StringUtil.isNotEmpty(tmp)){
                sp.setPriority(tmp);
            }
            tmp = filters.get("jobIdFilter");
            if (StringUtil.isNotEmpty(tmp)) {
                sp.setJobId(tmp);
                tmp = filters.get("jobIdOption");
                if (tmp.equals("GT"))
                    sp.setJobIdCondition(SearchCriteriaParameters.GREATER_THAN);
                else if(tmp.equals("LT"))
                    sp.setJobIdCondition(SearchCriteriaParameters.LESS_THAN);
                else 
                    sp.setJobIdCondition(SearchCriteriaParameters.EQUALS);
            }
            
            tmp = filters.get("jobNameFilter");
            if (StringUtil.isNotEmpty(tmp)) {
                sp.setJobName(tmp);
                sp.setJobNameCondition(SearchCriteriaParameters.CONTAINS);
            }
            tmp = filters.get("activityNameFilter");
            if (StringUtil.isNotEmpty(tmp)) {
                sp.setActivityName(tmp);
                sp.setActivityNameCondition(SearchCriteriaParameters.CONTAINS);
            }
            tmp = filters.get("assigneesNameFilter");
            if (StringUtil.isNotEmpty(tmp)) {
                sp.setAssigneesName(tmp);
            }
            tmp = filters.get("sourceLocaleFilter");
            if (StringUtil.isNotEmpty(tmp) && !"0".equals(tmp) && !"null".equalsIgnoreCase(tmp)) {
                sp.setSourceLocale(ServerProxy.getLocaleManager().getLocaleById(Long.parseLong(tmp)));
            }
            tmp = filters.get("targetLocaleFilter");
            if (StringUtil.isNotEmpty(tmp) && !"0".equals(tmp) && !"null".equalsIgnoreCase(tmp)) {
                sp.setTargetLocale(ServerProxy.getLocaleManager().getLocaleById(Long.parseLong(tmp)));
            }
            tmp = filters.get("companyFilter");
            if (StringUtil.isNotEmpty(tmp) && !"0".equals(tmp) && !"null".equalsIgnoreCase(tmp)) {
                sp.setCompanyName(ServerProxy.getJobHandler().getCompanyById(Long.parseLong(tmp)).getCompanyName());
            }
            
            List<TaskVo> tasks = TaskSearchUtil.search(user, sp);
            if (tasks == null || tasks.size() == 0) {
                params.setTotalRecords(0);
                params.setPageCount(1);
                params.setStartRecord(1);
                params.setEndRecord(1);
                
                return tasks;
            }
            TaskImpl taskImpl = null;

            int end = 0, taskCount = 0, totalPageCount = 1;
           
            end = start + rowsPerPage;
            taskCount = tasks.size();
            totalPageCount = Math.round(taskCount/rowsPerPage);
            if(taskCount%rowsPerPage > 0)
                totalPageCount++;
            if (end >= taskCount)
            {
                end = taskCount;
                start = end/rowsPerPage * rowsPerPage;
                params.setPageNumber(end/rowsPerPage + 1);
                if(end%rowsPerPage == 0)
                {
                    start = start - rowsPerPage;
                    params.setPageNumber(end/rowsPerPage);
                }
            }
            
            params.setTotalRecords(taskCount);
            params.setPageCount(totalPageCount);
            params.setStartRecord(start + 1);
            params.setEndRecord(end);
            
            int searchState = params.getTaskState();
            for (int i = start; i < end; i++) {
                TaskVo task = tasks.get(i);
                taskImpl = HibernateUtil.get(TaskImpl.class, task.getTaskId());
                if (WorkflowConstants.TASK_ALL_STATES == state) {
                    int oriState = taskImpl.getState();
                    TaskSearchUtil.setState(taskImpl, user.getUserId());
                    if (oriState == Task.STATE_FINISHING && taskImpl.getState() != Task.STATE_COMPLETED)
                        taskImpl.setState(oriState);
                } else 
                    taskImpl.setState(state);
                task.setState(taskImpl.getState());
                task.setStateString(bundle);
                task.setTaskDateString(params.getUILocale(), params.getTimeZone(), searchState);
                task.setEstimatedCompletionDateString(params.getUILocale(), params.getTimeZone());
                
                task.setActivityName(taskImpl.getTaskDisplayName());
                if (params.isProjectManager() || params.isCanManageProjects())
                    TaskSearchUtil.setAllAssignees(taskImpl);
                task.setAssignees(taskImpl.getAllAssigneesAsString());
                if(taskImpl.getIsUploading() == 'Y')
                {
                    task.setIsUploading("Yes");
                }
                else
                {
                    task.setIsUploading("No");
                }
                
                result.add(task);
            }
        }
        catch (Exception e)
        {
            logger.error("Error found when get tasks.", e);
        }
        
        return result;
    }

    private TaskListParams getTaskListParams(HttpServletRequest request, SessionManager sessionManager) {
        TaskListParams listParams = (TaskListParams) sessionManager.getMyactivitiesAttribute(TaskListConstants.TASK_LIST_PARAMS);
        if (listParams == null) {
            listParams = new TaskListParams();
            HttpSession session = request.getSession();
            PermissionSet perms = (PermissionSet) session.getAttribute(WebAppConstants.PERMISSIONS);
            listParams.setCanManageProjects(perms.getPermissionFor(Permission.PROJECTS_MANAGE));
            listParams.setCanManageWorkflows(perms.getPermissionFor(Permission.PROJECTS_MANAGE_WORKFLOWS));
            listParams.setCanExportAll(perms.getPermissionFor(Permission.ACTIVITIES_EXPORT));
            listParams.setCanExportInProgress(perms.getPermissionFor(Permission.ACTIVITIES_EXPORT_INPROGRESS));
            
            listParams.setPerPageCount(20);
            listParams.setPageNumber(1);
        }
        String getFilterFromRequest = request.getParameter("getFilterFromRequest");
        
        String action = request.getParameter("action");
        listParams.setAction(action);
        
        String sortColumn = request.getParameter("sortColumn");
        if (StringUtil.isNotEmpty(sortColumn) &&
                "true".equals(getFilterFromRequest))
            listParams.setSortColumn(sortColumn);
        
        String sortTypeString = request.getParameter("sortType");
        if (StringUtil.isNotEmpty(sortTypeString) &&
                "true".equals(getFilterFromRequest)) {
            boolean isAscSort = "desc".equalsIgnoreCase(sortTypeString) ? false : true;
            listParams.setAscSort(isAscSort);
        }
        
        if(StringUtil.isEmpty(listParams.getSortColumn()))
        {
            listParams.setSortColumn("jobId");
            listParams.setAscSort(false);
        }
        
        String pageNumberString = request.getParameter("pageNumber");
        if (StringUtil.isNotEmpty(pageNumberString)) {
            int pageNumber = Integer.parseInt(pageNumberString);
            if (pageNumber < 1)
                pageNumber = 1;
            listParams.setPageNumber(pageNumber);
        }
        
        String rowsPerPageString = request.getParameter("rowsPerPage");
        if (StringUtil.isNotEmpty(rowsPerPageString))
            listParams.setPerPageCount(Integer.parseInt(rowsPerPageString));

        //Default task state is 'All Status'
        String stateString = request.getParameter("state");
        int state = -10;
        if(StringUtil.isNotEmpty(stateString))
        {
            state = Integer.parseInt(stateString);
        }
        else
        {
            state = listParams.getTaskState();
        }
        listParams.setTaskState(state);

        String tempSearchType = "false";
        if(listParams.getFilters().get("advancedSearch") != null)
            tempSearchType = listParams.getFilters().get("advancedSearch");
        HashMap<String, String> filters = new HashMap<String, String>();
        String advancedSearch = request.getParameter("advancedSearch");
        if(advancedSearch == null)
            advancedSearch = tempSearchType;
        if(advancedSearch.equals("true"))
            setFilterValue(filters, "advancedSearch", "fasle");
        else
            setFilterValue(filters, "advancedSearch", "true");
        
        if("true".equals(getFilterFromRequest))
        {  
            try
            {
                setFilterValue(filters, "acceptanceStartFilter", request.getParameter("acceptanceStartFilter"));
                setFilterValue(filters, "acceptanceStartOptionsFilter", request.getParameter("acceptanceStartOptionsFilter"));
                setFilterValue(filters, "acceptanceEndFilter", request.getParameter("acceptanceEndFilter"));
                setFilterValue(filters, "acceptanceEndOptionsFilter", request.getParameter("acceptanceEndOptionsFilter"));
                setFilterValue(filters, "completionStartFilter", request.getParameter("completionStartFilter"));
                setFilterValue(filters, "completionStartOptionsFilter", request.getParameter("completionStartOptionsFilter"));
                setFilterValue(filters, "completionEndFilter", request.getParameter("completionEndFilter"));
                setFilterValue(filters, "completionEndOptionsFilter", request.getParameter("completionEndOptionsFilter"));
                setFilterValue(filters, "priorityFilter", request.getParameter("priorityFilter"));
                setFilterValue(filters, "jobIdOption", request.getParameter("jobIdOption"));
                setFilterValue(filters, "jobIdFilter", request.getParameter("jobIdFilter"));
                setFilterValue(filters, "jobNameFilter", new String(request.getParameter("jobNameFilter").getBytes("ISO8859-1"),"UTF-8"));
                setFilterValue(filters, "activityNameFilter", request.getParameter("activityNameFilter"));
                setFilterValue(filters, "assigneesNameFilter", request.getParameter("assigneesNameFilter"));
                setFilterValue(filters, "sourceLocaleFilter", request.getParameter("sourceLocaleFilter"));
                setFilterValue(filters, "targetLocaleFilter", request.getParameter("targetLocaleFilter"));
                setFilterValue(filters, "companyFilter", request.getParameter("companyFilter"));
                listParams.setFilters(filters);
            }
            catch (Exception e)
            {
            }
        }

        return listParams;
    }

    private void setFilterValue(HashMap<String, String> filters, String filterName, String filterValue) {
        if (StringUtil.isEmpty(filterValue))
            return;
        filters.put(filterName, filterValue);
    }
    
    private String getButtonStatus(int state, PermissionSet perms, User user) {
        JSONObject obj = new JSONObject();

        boolean isAccept = perms.getPermissionFor(Permission.ACTIVITIES_ACCEPT_ALL);
        boolean isTranslatedText = false;
        boolean isCompleteActivity = perms.getPermissionFor(Permission.ACTIVITIES_BATCH_COMPLETE_ACTIVITY);
        boolean isCompleteWorkflow = perms.getPermissionFor(Permission.ACTIVITIES_BATCH_COMPLETE_WORKFLOW);
        boolean isDetailWordCount = true;
        boolean isSearchReplace =  perms.getPermissionFor(Permission.ACTIVITIES_SEARCHREPLACE);;
        boolean isExportAll = perms.getPermissionFor(Permission.ACTIVITIES_EXPORT);
        boolean isExportInProgress = perms.getPermissionFor(Permission.ACTIVITIES_EXPORT_INPROGRESS);
        boolean isCanDownload = perms.getPermissionFor(Permission.ACTIVITIES_WORKOFFLINE);
        boolean isCanDownloadAll = perms.getPermissionFor(Permission.ACTIVITIES_DOWNLOAD_ALL);
        boolean isCanDownloadCombined = perms.getPermissionFor(Permission.ACTIVITIES_DOWNLOAD_COMBINED);
        boolean isOfflineUpload = perms.getPermissionFor(Permission.ACTIVITIES_OFFLINEUPLOAD_FROMANYACTIVITY);
        boolean isExportDownload = perms.getPermissionFor(Permission.ACTIVITIES_EXPORT_DOWNLOAD);
        boolean isExport = false;
        boolean isDownload = false;
        boolean isDownloadCombined = false;
        boolean showAssignees = TaskSearchUtil.isProjectManager(user) || perms.getPermissionFor(Permission.PROJECTS_MANAGE);

        switch (state) {
            case Task.STATE_ACTIVE:
                isTranslatedText = true;
                isSearchReplace = false;
                isExport = isExportAll;
                isDownload = isCanDownload;
                isDownloadCombined = isCanDownload && isCanDownloadCombined;
                break;
            case Task.STATE_ACCEPTED:
                isAccept = false;
                isTranslatedText = true;
                isExport = isExportInProgress;
                isDownload = isCanDownloadAll;
                isDownloadCombined = isCanDownloadAll && isCanDownloadCombined;
                break;
            case Task.STATE_COMPLETED:
            case Task.STATE_ALL:
                isExportDownload = false;
                isAccept = false;
                isTranslatedText = false;
                isCompleteActivity = false;
                isCompleteWorkflow = false;
                isSearchReplace = false;
                isExport = isExportAll;
                isDownload = false;
                isDownloadCombined = false;
                break;
            case Task.STATE_REJECTED:
                isExportDownload = false;
                isAccept = false;
                isTranslatedText = false;
                isDetailWordCount = false;
                isCompleteActivity = false;
                isCompleteWorkflow = false;
                isSearchReplace = false;
                isExport = false;
                isDownload = false;
                isDownloadCombined = false;
                isDetailWordCount = false;
                isOfflineUpload = false;
                break;
            default:
                break;
        }

        obj.put("accept", isAccept);
        obj.put("translatedText", isTranslatedText);
        obj.put("completeActivity", isCompleteActivity);
        obj.put("completeWorkflow", isCompleteWorkflow);
        obj.put("detailWordCount", isDetailWordCount);
        obj.put("searchReplace", isSearchReplace);
        obj.put("isExport", isExport);
        obj.put("download", isDownload);
        obj.put("downloadCombined", isDownloadCombined);
        obj.put("offlineUpload", isOfflineUpload);
        obj.put("showAssignees", showAssignees);
        obj.put("exportDownload", isExportDownload);

        return obj.toJSONString();
    }
    
    private boolean isLoginSession(HttpSession p_userSession,
            HttpServletRequest p_request)
    {
        boolean isLogin = true;
        if (p_userSession == null)
        {
            isLogin = false;
        }
        else
        {
            String sessionUserName = (String) p_userSession
                    .getAttribute(WebAppConstants.USER_NAME);
            sessionUserName = UserUtil.getUserNameById(sessionUserName);
            String loginFrom = p_request
                    .getParameter(WebAppConstants.LOGIN_FROM);
            if (sessionUserName == null || sessionUserName.length() == 0)
            {
                isLogin = false;
            }
            else if (loginFrom != null
                    && WebAppConstants.LOGIN_FROM_EMAIL.equals(loginFrom))
            {
                String loginName = p_request
                        .getParameter(WebAppConstants.LOGIN_NAME_FIELD);
                if (loginName != null && loginName.length() > 0
                        && !sessionUserName.equals(loginName))
                {
                    isLogin = false;
                }
            }
        }
        return isLogin;
    }
}
