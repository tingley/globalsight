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
package com.globalsight.everest.webapp.pagehandler.qachecks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.globalsight.cxe.entity.filterconfiguration.JsonUtil;
import com.globalsight.everest.edit.offline.OEMProcessStatus;
import com.globalsight.everest.qachecks.QAChecker;
import com.globalsight.everest.qachecks.QACheckerHelper;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.FileComparator;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportFile;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReader;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.SortUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.zip.ZipIt;

public class QAReportsHandler extends PageActionHandler implements
        ReportConstants
{
    @ActionHandler(action = ACTION_QA_REPORT_UPLOAD, formClass = "")
    public void uploadQAReport(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String msg = "The selected file has been uploaded successfully";
        String taskIdStr = p_request.getParameter("taskId");
        long taskId = Long.parseLong(taskIdStr);
        long taskIdFromReport = -1;
        String uploadedFileName = "";
        String contentType = p_request.getContentType();
        if (contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            MultipartFormDataReader reader = new MultipartFormDataReader();
            File tempFile = reader.uploadToTempFile(p_request);
            uploadedFileName = reader.getFilename();

            if (uploadedFileName != null
                    && uploadedFileName.toLowerCase().endsWith(EXTENSION_XLSX))
            {
                tempFile = FileUtil.changeExtension(tempFile, EXTENSION_XLSX);
                taskIdFromReport = getTaskIdFromReportFile(tempFile);
                if (taskIdFromReport == -1)
                {
                    msg = "The uploading file is not a QA checks report file";
                }
            }
            else
            {
                msg = "The uploading file is not an xlsx file";
            }

            if (taskId == taskIdFromReport)
            {
                Task task = ServerProxy.getTaskManager().getTask(taskId);
                File qaReport = QACheckerHelper.getQAReportFile(task);
                FileUtil.copyFile(tempFile, qaReport);
            }
            else if (taskIdFromReport != -1)
            {
                msg = "The uploading file is not for current task";
            }
            FileUtil.deleteTempFile(tempFile);

            if (msg != null)
            {
                HttpSession httpSession = p_request.getSession();
                SessionManager sessionMgr = (SessionManager) httpSession
                        .getAttribute(SESSION_MANAGER);

                sessionMgr.setAttribute("qaReportUploadMsg", msg + ": "
                        + uploadedFileName);
            }
        }
        p_response
                .sendRedirect("/globalsight/ControlServlet?linkName=uploadQAReport&pageName=QA_downloadQAReport&taskId="
                        + taskId);

        pageReturn();
    }

    private long getTaskIdFromReportFile(File tempFile)
    {
        long taskId = -1;
        Workbook workbook = ExcelUtil.getWorkbook(tempFile);
        Sheet sheet = ExcelUtil.getDefaultSheet(workbook);
        String hiddenInfo = ExcelUtil.getCellValue(sheet, 0,
                QAChecker.COLUMN_HIDDEN_INFO);

        if (!StringUtil.isEmpty(hiddenInfo)
                && hiddenInfo.startsWith(PREFIX_QA_CHECKS_REPORT + "_"))
        {
            String fromTaskId = StringUtil.replace(hiddenInfo,
                    PREFIX_QA_CHECKS_REPORT + "_", "");
            try
            {
                taskId = Long.parseLong(fromTaskId);
            }
            catch (NumberFormatException nfe)
            {
                taskId = -1;
            }
        }

        return taskId;
    }

    @ActionHandler(action = UPLOAD_ACTION_PROGRESS, formClass = "")
    public void uploadQAReportProgress(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        HttpSession session = p_request.getSession();
        ResourceBundle bundle = PageHandler.getBundle(session);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        OEMProcessStatus status = (OEMProcessStatus) sessionMgr
                .getAttribute(UPLOAD_STATUS);
        File tempFile = (File) sessionMgr.getAttribute("tempFile");
        String fileName = (String) sessionMgr.getAttribute("uploadedFileName");

        status.speak(50, bundle.getString("lb_upload_file") + fileName);
        String errorString = "";

        tempFile = FileUtil.changeExtension(tempFile, EXTENSION_XLSX);
        Workbook workbook = ExcelUtil.getWorkbook(tempFile);
        Sheet sheet = ExcelUtil.getDefaultSheet(workbook);
        String hiddenInfo = ExcelUtil.getCellValue(sheet, 0,
                QAChecker.COLUMN_HIDDEN_INFO);

        String fromTaskId = StringUtil.replace(hiddenInfo,
                PREFIX_QA_CHECKS_REPORT + "_", "");

        String toTaskId = (String) p_request.getParameter("taskId");
        if (fromTaskId != null && toTaskId != null
                && fromTaskId.equals(toTaskId))
        {
            Task task = ServerProxy.getTaskManager().getTask(
                    Long.parseLong(toTaskId));
            File qaReport = QACheckerHelper.getQAReportFile(task);
            FileUtil.copyFile(tempFile, qaReport);

        }
        tempFile.delete();

        status.setResults(errorString);
        status.speak(1, bundle.getString("lb_process_done"));

        pageReturn();
    }

    @ActionHandler(action = UPLOAD_ACTION_REFRESH, formClass = "")
    public void refreshProcessStatus(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        HttpSession session = p_request.getSession();
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        OEMProcessStatus status = (OEMProcessStatus) sessionMgr
                .getAttribute(UPLOAD_STATUS);

        String errorMsg = (String) status.getResults();
        sessionMgr.setAttribute(OfflineConstants.ERROR_MESSAGE, errorMsg);
        p_response.setContentType("text/html");
        if (status != null)
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("percentage", status.getPercentage());
            map.put("counter", status.getCounter());
            map.put("msg", status.giveMessages());
            map.put("errMsg", errorMsg);

            ServletOutputStream out = p_response.getOutputStream();
            out.write(JsonUtil.toJson(map).getBytes("UTF-8"));
            out.close();
        }

        pageReturn();
    }

    @ActionHandler(action = ACTION_QA_REPORT_GENERATE, formClass = "")
    public void generateQAReport(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        long taskId = Long.parseLong(p_request.getParameter("taskId"));
        QAChecker qaChecker = new QAChecker();
        qaChecker.runQAChecksAndGenerateReport(taskId);

        pageReturn();
    }

    @ActionHandler(action = ACTION_QA_REPORT_GET, formClass = "")
    public void getQAReport(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        long taskId = Long.parseLong(p_request.getParameter("taskId"));
        Task task = TaskHelper.getTask(taskId);
        File qaReport = QACheckerHelper.getQAReportFile(task);
        if (qaReport.exists())
        {
            File[] files = new File[1];
            files[0] = qaReport;

            ReportHelper.sendFiles(files, null, p_response);
        }

        pageReturn();
    }

    @ActionHandler(action = ACTION_VIEW, formClass = "")
    public void viewRecentReports(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String taskIdStr = p_request.getParameter("taskId");
        long taskId = Long.parseLong(taskIdStr);
        List<ReportFile> reportFiles = getWorkflowReportFiles(taskId);
        p_response.getWriter().write(getJSON(reportFiles));

        pageReturn();
    }

    @ActionHandler(action = ACTION_DELETE, formClass = "")
    public void deleteReports(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String selReports = p_request.getParameter("selReports");
        Set<File> reports = getSelectedReports(selReports, true);
        for (File file : reports)
        {
            FileUtil.deleteFile(file);
        }
    }

    @ActionHandler(action = ACTION_DOWNLOAD, formClass = "")
    public void downloadReports(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String taskIdStr = p_request.getParameter("taskId");
        long taskId = Long.parseLong(taskIdStr);
        String selReports = p_request.getParameter("selReports");
        Set<File> reports = getSelectedReports(selReports, false);

        if (reports.size() == 1)
        {
            ReportHelper.sendFiles(reports, null, p_response, false);
        }
        else
        {
            Task task = TaskHelper.getTask(taskId);
            String qaReportWorkflowPath = QACheckerHelper
                    .getQAReportWorkflowPath(task);

            StringBuilder fileName = new StringBuilder();
            fileName.append(REPORT_QA_CHECKS_REPORT);
            fileName.append("_");
            fileName.append(task.getJobName());
            fileName.append("_");
            fileName.append(task.getWorkflow().getJob().getSourceLocale()
                    .toString());
            fileName.append("-");
            fileName.append(task.getTargetLocale().toString());
            fileName.append(".zip");

            File zipFile = new File(fileName.toString());

            ZipIt.addEntriesToZipFile(zipFile, reports, qaReportWorkflowPath,
                    "");
            File[] files =
            { zipFile };
            ReportHelper.sendFiles(files, null, p_response, true);
        }

        pageReturn();
    }

    private Set<File> getSelectedReports(String p_reportsPath,
            boolean p_includeFolder)
    {
        Set<File> reports = new HashSet<File>();
        if (p_reportsPath == null || p_reportsPath.trim().length() == 0)
        {
            return reports;
        }

        for (String path : p_reportsPath.split(","))
        {
            File file = new File(path);
            if (!p_includeFolder && file.isDirectory())
            {
                continue;
            }
            reports.add(file);
        }

        return reports;
    }

    private List<ReportFile> getWorkflowReportFiles(long taskId)
    {
        List<ReportFile> reportFiles = new ArrayList<ReportFile>();
        Task task = TaskHelper.getTask(taskId);
        File qaReportWorkflowFolder = QACheckerHelper
                .getQAReportWorkflowFolder(task);
        if (qaReportWorkflowFolder.exists()
                && qaReportWorkflowFolder.isDirectory())
        {
            File[] taskFolders = qaReportWorkflowFolder.listFiles();
            if (taskFolders.length > 0)
            {
                for (File taskFolder : taskFolders)
                {
                    if (taskFolder.isFile() || FileUtil.isEmpty(taskFolder))
                    {
                        continue;
                    }
                    ReportFile taskFolderReportFile = new ReportFile(taskFolder);
                    setChildren(taskFolderReportFile, taskFolder);
                    if (taskFolderReportFile.getChildren() != null)
                    {
                        reportFiles.add(taskFolderReportFile);
                    }
                }
            }
        }

        return reportFiles;
    }

    private void setChildren(ReportFile taskFolderReportFile, File taskFolder)
    {
        List<ReportFile> children = new ArrayList<ReportFile>();
        File[] taskFiles = taskFolder.listFiles();
        SortUtil.sort(taskFiles, new FileComparator(0, null, true));
        for (File file : taskFiles)
        {
            ReportFile reportFile = new ReportFile(file);
            children.add(reportFile);
        }
        taskFolderReportFile.setChildren(children);
    }

    private String getJSON(List<ReportFile> p_list)
    {
        if (p_list == null || p_list.size() == 0)
        {
            return "[]";
        }

        StringBuilder result = new StringBuilder();
        result.append("[");
        for (ReportFile reportFile : p_list)
        {
            result.append(reportFile.toJSON()).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        result.append("]");
        return result.toString();
    }

    @Override
    public void beforeAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
    }

    @Override
    public void afterAction(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException,
            EnvoyServletException
    {
    }
}
