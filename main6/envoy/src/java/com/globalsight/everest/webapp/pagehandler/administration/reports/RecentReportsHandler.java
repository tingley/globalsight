/**
 *  Copyright 2013 Welocalize, Inc. 
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.util.comparator.FileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.ActionHandler;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportFile;
import com.globalsight.everest.webapp.pagehandler.administration.reports.bo.ReportsData;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Recent Reports Handler: View/Download/Delete Recent Reports.
 */
public class RecentReportsHandler extends PageHandler implements ReportConstants
{
    Logger logger = Logger.getLogger(RecentReportsHandler.class);
    String SELECT_REPORTS = "selReports";
    String ZIP_FILENAME = "RecentReports";

    @ActionHandler(action = ACTION_VIEW, formClass = "")
    public void viewRecentReports(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        List<ReportFile> reportFiles = getUserReportFiles(userId);
        p_response.getWriter().write(getJSON(reportFiles));
    }

    @ActionHandler(action = ACTION_DELETE, formClass = "")
    public void deleteReports(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        String selReports = p_request.getParameter(SELECT_REPORTS);
        Set<File> reports = getReports(selReports, true);
        ReportHelper.deleteFiles(reports);
        // Delete Again for Empty Folder
        ReportHelper.deleteFiles(reports);
        p_response.sendRedirect("/globalsight/ControlServlet?activityName=recentReports");
    }

    @ActionHandler(action = ACTION_GET_REPORTSDATA, formClass = "")
    public void getReportsData(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        List<ReportsData> statusList = ReportDBUtil.getReportsData(userId);
        StringBuilder json = new StringBuilder();
        if (statusList != null && statusList.size() > 0)
        {
            for (ReportsData data : statusList)
            {
                json.append(data.toJSON()).append(",");
            }
            json.delete(json.lastIndexOf(","), json.length());
            json.insert(0, "[");
            json.append("]");
        }

        p_response.getWriter().write(json.toString());
    }
    
    @ActionHandler(action = ACTION_DOWNLOAD, formClass = "")
    public void downloadReports(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        HttpSession session = p_request.getSession();
        String userId = (String) session.getAttribute(WebAppConstants.USER_NAME);
        String selReports = p_request.getParameter(SELECT_REPORTS);
        Set<File> reports = getReports(selReports, false);   
        if (reports.size() == 1)
        {
            ReportHelper.sendFiles(reports, null, p_response, false);
        }
        else
        {
            File zipFile = new File(ZIP_FILENAME + ".zip");
            StringBuffer excludePathBuf = new StringBuffer();
            excludePathBuf.append(AmbFileStoragePathUtils.getFileStorageDirPath(1));
            excludePathBuf.append(File.separator);
            excludePathBuf.append(ReportConstants.REPORTS_SUB_DIR).append(File.separator);
            excludePathBuf.append(userId).append(File.separator);
            String excludePath = excludePathBuf.toString();
            if (File.separator.equals("\\"))
            {
                excludePath = excludePath.replace("/", File.separator);
            }
            ZipIt.addEntriesToZipFile(zipFile, reports, excludePath.toString(), "");
            File[] files = { zipFile };
            ReportHelper.sendFiles(files, null, p_response, true);
        }
    }


    /**
     * Invokes this PageHandler
     * 
     * @param p_pageDescriptor
     *            the page descriptor
     * @param p_request
     *            the original request sent from the browser
     * @param p_response
     *            the original response object
     * @param p_context
     *            context the Servlet context
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor, HttpServletRequest p_request,
            HttpServletResponse p_response, ServletContext p_context) throws ServletException, IOException,
            EnvoyServletException
    {
        String action = p_request.getParameter("action");

        beforeAction(p_request, p_response);

        callAction(p_request, p_response);

        afterAction(p_request, p_response);

        if (action == null)
        {
            super.invokePageHandler(p_pageDescriptor, p_request, p_response, p_context);
        }
    }

    private void callAction(HttpServletRequest p_request, HttpServletResponse p_response)
    {
        String action = p_request.getParameter("action");
        if (action == null)
        {
            return;
        }
        Method[] ms = this.getClass().getMethods();
        for (Method m : ms)
        {
            if (m.isAnnotationPresent(ActionHandler.class))
            {
                ActionHandler handler = m.getAnnotation(ActionHandler.class);
                if (action.matches(handler.action()))
                {
                    try
                    {
                        m.invoke(this, p_request, p_response);
                    }
                    catch (Exception e)
                    {
                        logger.error(e.getMessage(), e);
                    }

                    break;
                }
            }
        }
    }

    public void beforeAction(HttpServletRequest p_request, HttpServletResponse response)
    {
    }

    public void afterAction(HttpServletRequest request, HttpServletResponse response)
    {
    }
    
    /**
     * Get File Collection from Input Reports Path String
     * 
     * @param p_reportsPath
     *            Input Reports Path String
     * @param p_isIncludeFolder
     *            Whether Include Folder
     * @return
     */
    private Set<File> getReports(String p_reportsPath, boolean p_isIncludeFolder)
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
     * Get Reports Files by User ID.
     */
    private List<ReportFile> getUserReportFiles(String p_userId)
    {
        List<ReportFile> reportFiles = new ArrayList<ReportFile>();
        File folder = getFolder(p_userId, null);
        File[] files = folder.listFiles();
        Arrays.sort(files);
        for (File file : files)
        {
            if (file.isFile() || FileUtil.isEmpty(file))
                continue;
            ReportFile reportTypeFolder = new ReportFile(file);
            File[] dateFolder = file.listFiles();
            Arrays.sort(dateFolder, Collections.reverseOrder());
            for (File temp : dateFolder)
            {
                if (temp.isFile() || FileUtil.isEmpty(temp))
                    continue;
                ReportFile dfReportFile = new ReportFile(temp);
                setChildren(dfReportFile, temp);
                if (dfReportFile.getChildren() != null)
                {
                    reportTypeFolder.addChildren(dfReportFile);
                }
            }

            if (reportTypeFolder.getChildren() != null)
            {
                reportFiles.add(reportTypeFolder);
            }
        }

        return reportFiles;
    }

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

    private File getFolder(String p_userId, String p_subDir)
    {
        StringBuffer result = new StringBuffer();
        result.append(AmbFileStoragePathUtils.getFileStorageDirPath(1));
        result.append(File.separator);
        result.append(ReportConstants.REPORTS_SUB_DIR).append(File.separator);
        result.append(p_userId).append(File.separator);

        if (p_subDir != null && p_subDir.trim().length() > 0)
        {
            result.append(p_subDir).append(File.separator);
        }

        File file = new File(result.toString());
        file.mkdirs();
        return file;
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
}
