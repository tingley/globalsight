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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.globalsight.everest.qachecks.DITAQAChecker;
import com.globalsight.everest.qachecks.DITAQACheckerHelper;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.util.comparator.FileComparator;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageActionHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportConstants;
import com.globalsight.everest.webapp.pagehandler.administration.reports.ReportHelper;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportFile;
import com.globalsight.everest.webapp.pagehandler.offline.upload.MultipartFormDataReader;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskHelper;
import com.globalsight.util.ExcelUtil;
import com.globalsight.util.FileUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Recent Reports Handler: View/Download/Delete Recent Reports.
 */
public class DitaQaReportsHandler extends PageActionHandler implements
        ReportConstants
{
    Logger logger = Logger.getLogger(DitaQaReportsHandler.class);

    String SELECT_REPORTS = "selReports";
    String ZIP_FILENAME = "RecentDITAQAReports";

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
        String taskIdStr = p_request.getParameter("taskId");
        long taskId = Long.parseLong(taskIdStr);
        String selReports = p_request.getParameter(SELECT_REPORTS);
        Set<File> reports = getSelectedReports(selReports, true);
        ReportHelper.deleteFiles(reports);
        // Delete Again for Empty Folder
        ReportHelper.deleteFiles(reports);
        p_response.sendRedirect("/globalsight/ControlServlet?activityName=ditaReports&taskId=" + taskId);

        pageReturn();
    }

    @ActionHandler(action = ACTION_DOWNLOAD, formClass = "")
    public void downloadReports(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        String taskIdStr = p_request.getParameter("taskId");
        long taskId = Long.parseLong(taskIdStr);
        String selReports = p_request.getParameter(SELECT_REPORTS);
        Set<File> reports = getSelectedReports(selReports, false);

        if (reports.size() == 1)
        {
            ReportHelper.sendFiles(reports, null, p_response, false);
        }
        else
        {
            File zipFile = new File(ZIP_FILENAME + ".zip");
            Task task = TaskHelper.getTask(taskId);
            File folder = DITAQACheckerHelper.getReportFileDir(task);
            if (folder.exists())
            {
                folder = folder.getParentFile();
            }
            String excludePath = folder.getAbsolutePath();
            ZipIt.addEntriesToZipFile(zipFile, reports, excludePath.toString(), "");
            File[] files = { zipFile };
            ReportHelper.sendFiles(files, null, p_response, true);
        }

        pageReturn();
    }

    @ActionHandler(action = ACTION_UPLOAD, formClass = "")
    public void uploadReport(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        // For DITA report, only store one copy for every task. The uploaded
        // file will replace old one if old one exsits.
        String taskIdStr = p_request.getParameter("taskId");
        long taskId = Long.parseLong(taskIdStr);

        String msg = "The selected file has been uploaded successfully";
        File reportFile = null;
        File tmpFile = null;
        String fileName = null;
        long taskIdFromReport = -1;
        String contentType = p_request.getContentType();
        if (contentType != null
                && contentType.toLowerCase().startsWith("multipart/form-data"))
        {
            MultipartFormDataReader reader = new MultipartFormDataReader();
            tmpFile = reader.uploadToTempFile(p_request);
            fileName = reader.getFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx"))
            {
                taskIdFromReport = getTaskIdFromReportFile(tmpFile);
                if (taskIdFromReport == -1)
                {
                    msg = "The uploading file is not a DITA QA checks report file";
                }
            }
            else
            {
                msg = "The uploading file is not an xlsx file";
            }
        }

        if (taskId == taskIdFromReport)
        {
            Task task = TaskHelper.getTask(taskId);
            File taskFolder = DITAQACheckerHelper.getReportFileDir(task);
            // As we only store one copy for one task, delete previous copy.
            FileUtil.deleteFile(taskFolder);
            taskFolder.mkdirs();
            reportFile = new File(taskFolder, fileName);
            FileUtils.copyFile(tmpFile, reportFile);
        }
        else if (taskIdFromReport != -1)
        {
            msg = "The uploading file is not for current task";
        }

        // Redirect to "Upload DITA QA Report" UI.
        if (msg != null)
        {
            HttpSession httpSession = p_request.getSession();
            SessionManager sessionMgr = (SessionManager) httpSession
                    .getAttribute(SESSION_MANAGER);
            sessionMgr.setAttribute("ditaUploadMsg", msg + ": " + fileName);
        }
        p_response.sendRedirect("/globalsight/ControlServlet?linkName=uploadDitaReport&pageName=QA_downloadDitaReport&taskId=" + taskId);

        pageReturn();
    }

    @ActionHandler(action = GENERATE_DITA_REPORT, formClass = "")
    public void generateDitaReport(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        long taskId = Long.parseLong(p_request.getParameter("taskId"));
        DITAQAChecker qaChecker = new DITAQAChecker();
        qaChecker.runQAChecksAndGenerateReport(taskId);

        pageReturn();
    }

    @ActionHandler(action = GET_DITA_REPORT, formClass = "")
    public void getDitaReport(HttpServletRequest p_request,
            HttpServletResponse p_response, Object form) throws Exception
    {
        long taskId = Long.parseLong(p_request.getParameter("taskId"));
        Task task = TaskHelper.getTask(taskId);
        File dir = DITAQACheckerHelper.getReportFileDir(task);
        if (dir.exists() && dir.isDirectory())
        {
            File[] files = dir.listFiles();
            if (files.length > 0)
            {
                ReportHelper.sendFiles(files, null, p_response);
            }
        }

        pageReturn();
    }

    /**
     * Get selected files for downloading or deleting.
     * 
     * @param p_reportsPath
     *            Input Reports Path String
     * @param p_isIncludeFolder
     *            Whether Include Folder
     * @return
     */
    private Set<File> getSelectedReports(String p_reportsPath,
            boolean p_isIncludeFolder)
    {
        Set<File> reports = new HashSet<File>();
        if (p_reportsPath == null || p_reportsPath.trim().length() == 0)
        {
            return reports;
        }

        for (String path : p_reportsPath.split(","))
        {
            File file = new File(path);
            if (!p_isIncludeFolder && file.isDirectory())
                continue;
            reports.add(file);
        }

        return reports;
    }

    /**
     * Get report files from the workflow current task is in.
     */
    private List<ReportFile> getWorkflowReportFiles(long taskId)
    {
        List<ReportFile> reportFiles = new ArrayList<ReportFile>();
        Task task = TaskHelper.getTask(taskId);
        File folder = DITAQACheckerHelper.getReportFileDir(task);
        if (folder.exists())
        {
            folder = folder.getParentFile();
        }
        File[] files = folder.listFiles();
        Arrays.sort(files);
        for (File file : files)
        {
            if (file.isFile() || FileUtil.isEmpty(file))
                continue;
            ReportFile dfReportFile = new ReportFile(file);
            setChildren(dfReportFile, file);
            if (dfReportFile.getChildren() != null)
            {
                reportFiles.add(dfReportFile);
            }
        }

        return reportFiles;
    }

    @SuppressWarnings("unchecked")
    private void setChildren(ReportFile p_reportFolder, File p_folder)
    {
        if (p_folder.isFile() || FileUtil.isEmpty(p_folder))
            return;

        List<ReportFile> children = new ArrayList<ReportFile>();
        File[] files = p_folder.listFiles();
        Arrays.sort(files, new FileComparator(0, null, true));
        for (File file : files)
        {
            ReportFile reportFolder = new ReportFile(file);
            if (file.isDirectory() && !FileUtil.isEmpty(file))
            {
                setChildren(reportFolder, file);
            }
            children.add(reportFolder);
        }
        p_reportFolder.setChildren(children);
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

    /**
     * In DITA QA report file, the task ID info is hidden in "AA1" cell. It is
     * like "DITA_1234". Need get the task ID "1234".
     * 
     * @param p_file
     * @return
     */
    private long getTaskIdFromReportFile(File p_file)
    {
        long taskId = -1;
        FileInputStream fis = null;
        File tmpFile = null;
        try
        {
            tmpFile = File.createTempFile("TEM_", ".xlsx");
            FileUtils.copyFile(p_file, tmpFile);
            fis = new FileInputStream(tmpFile);
            Workbook workbook = ExcelUtil.getWorkbook(tmpFile.getAbsolutePath(),
                    fis);
            Sheet sheet = ExcelUtil.getDefaultSheet(workbook);
            // task info is like "DITA_1234".
            String taskInfo = ExcelUtil.getCellValue(sheet, 0, 26);
            if (taskInfo != null && taskInfo.startsWith("DITA_"))
            {
                taskId = Long.parseLong(taskInfo.substring(5));                
            }
        }
        catch (Exception e)
        {
            logger.warn("Can not get taskID from 'AA1' cell of DITA report file, maybe this file is not a DITA report file."
                    + e.getMessage());
        }
        finally
        {
            if (fis != null)
                try
                {
                    fis.close();
                    tmpFile.delete();
                }
                catch (IOException e)
                {
                }
        }

        return taskId;
    }

    public void beforeAction(HttpServletRequest p_request,
            HttpServletResponse response)
    {
    }

    public void afterAction(HttpServletRequest request,
            HttpServletResponse response)
    {
    }
}
