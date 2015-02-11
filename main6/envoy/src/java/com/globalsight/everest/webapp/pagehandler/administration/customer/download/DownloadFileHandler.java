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
package com.globalsight.everest.webapp.pagehandler.administration.customer.download;

// TO-DO: replace with com.globalsight.util after integration
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.cxe.adapter.passolo.PassoloUtil;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.page.pageexport.ExportConstants;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.StringUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Supports the JSP download UI for customer download
 */
public class DownloadFileHandler extends PageHandler
{
    // http request params / session attributes
    public static final String PARAM_UPLOAD_NAME = "customerDownload.uploadName";
    public static final String PARAM_LOCALE = "customerDownload.locale";
    public static final String PARAM_CURRENT_FOLDER = "currentFolder";
    public static final String PARAM_FIRST_ENTRY = "firstEntry";
    public static final String PARAM_JOB_ID = "customerDownload.jobId";
    public static final String PARAM_JOB_COMPANY_ID = "customerDownload.jobCompanyId";
    public static final String PARAM_WORKFLOW_ID = "customerDownload.wfId";
    public static final String PARAM_DOWNLOAD_NAME = "customerDownload.originalFloderName";
    public static final String PARAM_DOWNLOAD_FILE_NAME = "customerDownload.FileName";
    public static final String DOWNLOAD_FROM_JOB = "downloadFlag";
    public static final String DOWNLOAD_JOB_LOCALES = "locales";
    public static final String FILE_LIST = "dl_fileList";
    public static final String SELECT_FILE = "selectFile";
    public static final String DOWNLOAD_APPLET = "downloadApplet";
    public static final String DONE = "done";
    public static final String DONE_FROM_JOB = "doneFromJob";
    public static final String DONE_FROM_EXPORT_JOBS = "doneFromExportJobs";
    public static final String DONE_FROM_LOCALIZED_JOBS = "doneFromLocalizedJobs";
    public static final String DONE_FROM_TASK = "doneFromTask";
    public static final String DESKTOP_FOLDER = "webservice";
    public static final String CompanyType = "companyType";

    private static final Logger CATEGORY = Logger
            .getLogger(DownloadFileHandler.class.getName());
    private static final String FOLDER_LISTING = "folderListing";

    /**
     * Invokes this EntryPageHandler object
     * <p>
     * 
     * @param p_pageDescriptor
     *            the description of the page to be produced.
     * @param p_request
     *            original request sent from the browser.
     * @param p_response
     *            original response object.
     * @param p_context
     *            the Servlet context.
     */
    public void invokePageHandler(WebPageDescriptor p_pageDescriptor,
            HttpServletRequest p_request, HttpServletResponse p_response,
            ServletContext p_context) throws ServletException, IOException,
            RemoteException, EnvoyServletException
    {
        // create the links and the navigation beans.
        NavigationBean folderListingBean = new NavigationBean(FOLDER_LISTING,
                p_pageDescriptor.getPageName());
        NavigationBean fileListBean = new NavigationBean(FILE_LIST,
                p_pageDescriptor.getPageName());
        NavigationBean selectFileBean = new NavigationBean(SELECT_FILE,
                p_pageDescriptor.getPageName());
        NavigationBean downloadAppletBean = new NavigationBean(DOWNLOAD_APPLET,
                p_pageDescriptor.getPageName());
        NavigationBean doneBean = new NavigationBean(DONE,
                p_pageDescriptor.getPageName());
        NavigationBean doneFromJobBean = new NavigationBean(DONE_FROM_JOB,
                p_pageDescriptor.getPageName());
        NavigationBean doneFromTaskBean = new NavigationBean(DONE_FROM_TASK,
                p_pageDescriptor.getPageName());
        NavigationBean doneFromExportJobsBean = new NavigationBean(
                DONE_FROM_EXPORT_JOBS, p_pageDescriptor.getPageName());
        NavigationBean doneFromLocalizedJobsBean = new NavigationBean(
                DONE_FROM_LOCALIZED_JOBS, p_pageDescriptor.getPageName());

        p_request.setAttribute(FOLDER_LISTING, folderListingBean);
        p_request.setAttribute(FILE_LIST, fileListBean);
        p_request.setAttribute(SELECT_FILE, selectFileBean);
        p_request.setAttribute(DOWNLOAD_APPLET, downloadAppletBean);
        p_request.setAttribute(DONE, doneBean);
        p_request.setAttribute(DONE_FROM_JOB, doneFromJobBean);
        p_request.setAttribute(DONE_FROM_TASK, doneFromTaskBean);
        p_request.setAttribute(DONE_FROM_EXPORT_JOBS, doneFromExportJobsBean);
        p_request.setAttribute(DONE_FROM_LOCALIZED_JOBS,
                doneFromLocalizedJobsBean);

        HttpSession session = p_request.getSession(true);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);

        // clear out the file list if this is the first entry into the screen
        if (p_request.getParameter(PARAM_FIRST_ENTRY) != null)
        {
            sessionMgr.setAttribute(FILE_LIST, new HashSet());
        }
        String downloadFlag = p_request.getParameter(DOWNLOAD_FROM_JOB);
        // the upload name
        String uploadName = p_request.getParameter(PARAM_UPLOAD_NAME);
        String locale = p_request.getParameter(PARAM_LOCALE);
        StringBuffer jobNameOri = new StringBuffer();
        String jobCompanyName = null;
        String jobId = null;
        boolean hasPassolo = false;

        if (uploadName != null)
            sessionMgr.setAttribute(PARAM_UPLOAD_NAME, uploadName);
        else
        {
            // upload name is null, but maybe job id was passed in, in which
            // case the upload name is just the job name
            jobId = p_request.getParameter(PARAM_JOB_ID);
            if (jobId != null)
            {
                try
                {
                    long job_id = Long.parseLong(jobId);
                    Job j = ServerProxy.getJobHandler().getJobById(job_id);
                    // retrieve company name for current job
                    long jobCompanyId = j.getCompanyId();
                    sessionMgr.setAttribute(PARAM_JOB_COMPANY_ID,
                            j.getCompanyId());
                    jobCompanyName = ServerProxy.getJobHandler()
                            .getCompanyById(jobCompanyId).getCompanyName();

                    sessionMgr.setAttribute("jobName", j.getJobName());
                    List sps = new ArrayList(j.getSourcePages());
                    SourcePage sp = (SourcePage) sps.get(0);
                    String pageId = sp.getExternalPageId();
                    String[] pageIdToken = (pageId.replace('\\', '/'))
                            .split("/");
                    Iterator pageIterator = sps.iterator();
                    List pageList = new ArrayList();
                    List fullPageList = new ArrayList();

                    // Clear the delayTimeTable date for this job export
                    Hashtable delayTimeTable = (Hashtable) sessionMgr
                            .getAttribute(WebAppConstants.DOWLOAD_DELAY_TIME_TABLE);
                    String wfId = p_request.getParameter(PARAM_WORKFLOW_ID);
                    if (delayTimeTable != null)
                    {
                        User user = (User) sessionMgr.getAttribute(USER);
                        String userId = user.getUserId();
                        String delayTimeKey = userId + jobId + wfId;

                        Object startTimeObj = delayTimeTable.get(delayTimeKey);
                        if (startTimeObj != null)
                        {
                            delayTimeTable.remove(delayTimeKey);
                        }
                    }

                    if (downloadFlag != null && downloadFlag.equals("true"))
                    {
                        // Get target locales from workflows of selected job
                        List localesList = new ArrayList();
                        for (Workflow wf : j.getWorkflows())
                        {
                            localesList.add(wf.getTargetLocale().toString());
                        }
                        sessionMgr.setAttribute(DOWNLOAD_JOB_LOCALES,
                                localesList);
                    }

                    String wfIdParam = p_request
                            .getParameter(PARAM_WORKFLOW_ID);
                    long wId = -1;
                    if (wfIdParam != null)
                    {
                        wId = Long.parseLong(wfIdParam);
                    }

                    while (pageIterator.hasNext())
                    {
                        SourcePage sourcePage = (SourcePage) pageIterator
                                .next();
                        boolean isPassoloFile = PassoloUtil
                                .isPassoloFile(sourcePage);

                        if (wId > 0 && isPassoloFile)
                        {
                            boolean found = false;
                            for (TargetPage tp : sourcePage.getTargetPages())
                            {
                                if (tp.getWorkflowInstance().getId() == wId)
                                {
                                    found = true;
                                    break;
                                }
                            }

                            if (!found)
                            {
                                continue;
                            }
                        }

                        // For Import&Export script issue.
                        // Download the reverted file instead of the scripted
                        // file when importing.
                        Request req = sourcePage.getRequest();
                        FileProfile fp = HibernateUtil.get(
                                FileProfileImpl.class, req.getDataSourceId(),
                                false);

                        String externalPageId = sourcePage.getExternalPageId();

                        String scriptOnImport = fp.getScriptOnImport();
                        if (scriptOnImport != null
                                && scriptOnImport.length() > 0)
                        {
                            String tempPath = externalPageId.substring(0,
                                    externalPageId.lastIndexOf(File.separator));
                            String targetFolder = tempPath.substring(0,
                                    tempPath.lastIndexOf(File.separator));
                            String scriptedFolderName = tempPath
                                    .substring(tempPath
                                            .lastIndexOf(File.separator) + 1);
                            File srcFolder = new File(
                                    AmbFileStoragePathUtils
                                            .getCxeDocDirPath(String
                                                    .valueOf(sourcePage
                                                            .getCompanyId()))
                                            + File.separator + targetFolder);
                            File sourceFiles[] = srcFolder.listFiles();
                            for (int i = 0; sourceFiles != null
                                    && i < sourceFiles.length; i++)
                            {
                                File file = sourceFiles[i];
                                if (file.isFile())
                                {
                                    String fileName = file.getName();
                                    String scriptedFolderNamePrefix = FileSystemUtil
                                			.getScriptedFolderNamePrefixByJob(job_id);
                                    String folderName = scriptedFolderNamePrefix + "_" 
                                    		+ fileName.substring(0, fileName.lastIndexOf(".")) + "_"
                                    		+ fileName.substring(fileName.lastIndexOf(".") + 1);
                                    if (scriptedFolderName.equals(folderName) ||
                                    		scriptedFolderName.equals(fileName
                                    			.substring(0,
                                                    fileName.lastIndexOf("."))))
                                    {
                                        // re-set the externalPageId to the
                                        // reverted file name.
                                        externalPageId = targetFolder
                                                + File.separator + fileName;
                                        
                                        externalPageId = externalPageId.replace('\\', '/');
                                        if (fullPageList.contains(externalPageId))
                                            continue;
                                        
                                        break;
                                    }
                                }
                            }
                        }

                        // Process XLZ file
                        String tmp = externalPageId.toLowerCase();
                        if (XliffFileUtil.isXliffFile(tmp))
                        {
                            tmp = StringUtil.replace(tmp, "/", File.separator);
                            tmp = StringUtil.replace(tmp, "\\", File.separator);
                            int tmpIndex = tmp.lastIndexOf(".sub"
                                    + File.separator);
                            if (tmpIndex != -1)
                            {
                                tmp = tmp.substring(0, tmpIndex);
                                if (XliffFileUtil.isXliffFile(tmp))
                                    externalPageId = externalPageId.substring(
                                            0, tmpIndex);
                            }

                            String tmpFullname = "";
                            tmp = externalPageId.substring(0,
                                    externalPageId.lastIndexOf(File.separator))
                                    + ".xlz";

                            String companyName = CompanyWrapper
                                    .getCompanyNameById(j.getCompanyId());
                            if ("1".equals(CompanyWrapper.getCurrentCompanyId())
                                    && !"1".equals(j.getCompanyId()))
                            {
                                String baseDir = AmbFileStoragePathUtils
                                        .getCxeDocDir().getPath();
                                tmpFullname = baseDir + File.separator
                                        + companyName + File.separator + tmp;
                            }
                            else
                            {
                                tmpFullname = getAbsolutePath(tmp);
                            }
                            File tmpFullFile = new File(tmpFullname);
                            if (tmpFullFile.exists() && tmpFullFile.isFile())
                            {
                                externalPageId = tmp;
                            }
                        }

                        externalPageId = externalPageId.replace('\\', '/');

                        if (fullPageList.contains(externalPageId))
                            continue;

                        fullPageList.add(externalPageId);

                        if (isPassoloFile)
                        {
                            File f = new File(sourcePage.getPassoloFileName());
                            String name = f.getName();
                            if (!pageList.contains(name))
                            {
                                pageList.add(name);
                            }

                            List localesList = (List) sessionMgr
                                    .getAttribute(DOWNLOAD_JOB_LOCALES);

                            if (localesList == null)
                            {
                                localesList = new ArrayList();
                            }

                            if (localesList != null
                                    && !localesList.contains("passolo"))
                            {
                                localesList.add("passolo");
                                sessionMgr.setAttribute(DOWNLOAD_JOB_LOCALES,
                                        localesList);
                            }

                            hasPassolo = true;
                        }
                        else
                        {
                            pageList.add(externalPageId.substring(
                                    externalPageId.lastIndexOf("/") + 1,
                                    externalPageId.length()));
                        }
                    }
                    sessionMgr.setAttribute(PARAM_DOWNLOAD_FILE_NAME, pageList);

                    if (pageIdToken.length > 2)
                    {
                        if (pageIdToken[1].equals(DESKTOP_FOLDER))
                        {
                            jobNameOri.append(pageIdToken[1]);
                            sessionMgr.setAttribute(PARAM_DOWNLOAD_NAME,
                                    pageIdToken[2]);
                            sessionMgr.setAttribute(DESKTOP_FOLDER,
                                    pageIdToken[1]);
                        }
                        else
                        {
                            jobNameOri.append(pageIdToken[1]);
                            sessionMgr.setAttribute(PARAM_DOWNLOAD_NAME,
                                    pageIdToken[1]);
                        }
                    }
                    else
                    {
                        sessionMgr.setAttribute(PARAM_DOWNLOAD_NAME, null);
                    }

                    uploadName = j.getJobName();

                    sessionMgr.setAttribute(PARAM_UPLOAD_NAME, uploadName);
                }
                catch (Exception e)
                {
                    CATEGORY.error("Failed to get job " + jobId, e);
                }
            }
        }

        if (locale != null)
            sessionMgr.setAttribute(PARAM_LOCALE, locale);
        else
        {
            String wfIdParam = p_request.getParameter(PARAM_WORKFLOW_ID);
            if (wfIdParam != null)
            {
                try
                {
                    long wfId = Long.parseLong(wfIdParam);
                    Workflow w = ServerProxy.getWorkflowManager()
                            .getWorkflowByIdRefresh(wfId);
                    String wfLocale = w.getTargetLocale().toString();
                    sessionMgr.setAttribute(PARAM_LOCALE, wfLocale);
                    List localesList = (List) sessionMgr
                            .getAttribute(DOWNLOAD_JOB_LOCALES);
                    if (localesList != null && !localesList.contains(wfLocale))
                    {
                        localesList.add(wfLocale);
                    }
                }
                catch (Exception e)
                {
                    CATEGORY.error("Failed to get workflow " + wfIdParam, e);
                }
            }

            // the locale may have been previously stored in the session or just
            // read in now from the workflow
            locale = (String) sessionMgr.getAttribute(PARAM_LOCALE);
        }

        // Clicking on the folder sends the parameter via the request
        // object. Get it and put on the session.
        String currentFolder = p_request.getParameter(PARAM_CURRENT_FOLDER);

        SystemConfiguration sc = SystemConfiguration.getInstance();
        String ruleType = sc
                .getStringParameter(SystemConfiguration.DIRECTORY_RULE_TYPE);

        if (currentFolder == null)
        {
            if (locale == null || hasPassolo)
            {
                currentFolder = "/";
            }
            else
            {
                if (ruleType.toUpperCase().equals(
                        ExportConstants.LANGUAGE_DIRECTORY))
                {
                    int index = locale.indexOf("_");
                    currentFolder = "/" + locale.substring(0, index);
                }
                else if (ruleType.toUpperCase().equals(
                        ExportConstants.EXPORT_DIRECTORY))
                {
                    currentFolder = "/export";
                }
                else
                {
                    currentFolder = "/" + locale;
                    if (jobNameOri != null && jobNameOri.length() != 0)
                    {
                        currentFolder = currentFolder + "/"
                                + jobNameOri.toString();
                    }
                    // for super user works for sub company
                    if (jobCompanyName != null
                            && !CompanyWrapper
                                    .isSuperCompanyName(jobCompanyName)
                            && CompanyThreadLocal.getInstance()
                                    .fromSuperCompany()
                            && !currentFolder.startsWith("/" + jobCompanyName))
                    {
                        currentFolder = "/" + jobCompanyName + currentFolder;
                    }
                }
            }
        }
        // for download from list page
        if (downloadFlag != null && downloadFlag.equals("true"))
        {
            currentFolder = "/";
        }

        session.setAttribute(PARAM_CURRENT_FOLDER, currentFolder);

        // the selected file listing.
        String tokenized = currentFolder.replace('\\', '/');
        StringTokenizer tok = new StringTokenizer(tokenized, "/");
        List pageList = (List) sessionMgr
                .getAttribute(DownloadFileHandler.PARAM_DOWNLOAD_FILE_NAME);
        int depth = tok.countTokens(); // depth relative to cxe docs dir
        // fix for GBS-1449
        if (CompanyThreadLocal.getInstance().fromSuperCompany())
        {
            sessionMgr.setAttribute(CompanyType, "superCompany");
        }
        else
        {
            sessionMgr.setAttribute(CompanyType, "commonCompany");
        }

        prepareFileList(p_request, sessionMgr, depth, pageList);

        // turn off cache. do both. "pragma" for the older browsers.
        // p_response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        // p_response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
        // p_response.addHeader("Cache-Control", "no-store"); // tell proxy not
        // to
        // cache
        // p_response.addHeader("Cache-Control", "max-age=0"); // stale right
        // away

        // forward to the jsp page.
        RequestDispatcher requestDispatcher = p_context
                .getRequestDispatcher(p_pageDescriptor.getJspURL());
        requestDispatcher.forward(p_request, p_response);
    }

    private void recurseFileStructures(File p_baseFile, HashSet p_fileList,
            int p_depth, String p_uploadName, List p_pageList)
            throws EnvoyServletException
    {
        File[] directoryContent = p_baseFile.listFiles();
        File cxeBaseDir = AmbFileStoragePathUtils.getCxeDocDir();
        File doc = null;
        File xlzFile = null;

        if (directoryContent != null)
        {
            for (int i = 0; i < directoryContent.length; i++)
            {
                doc = directoryContent[i];

                if (doc.isDirectory())
                {
                    xlzFile = new File(doc.getAbsoluteFile() + ".xlz");
                    if (xlzFile.exists() && xlzFile.isFile())
                        continue;

                    if (p_depth == 1)
                    {
                        // only recurse if the dir has upload in the name
                        if (doc.getName().equals(p_uploadName) == true)
                        {
                            recurseFileStructures(doc, p_fileList, p_depth + 1,
                                    p_uploadName, p_pageList);
                        }
                    }
                    else
                    {
                        recurseFileStructures(doc, p_fileList, p_depth + 1,
                                p_uploadName, p_pageList);
                    }
                }
                else
                {
                    if (p_depth >= 1 && p_pageList != null
                            && p_pageList.size() > 0)
                    {
                        Iterator pages = p_pageList.iterator();
                        while (pages.hasNext())
                        {
                            String fileName = (String) pages.next();
                            if (doc.getName().equals(fileName))
                            {
                                p_fileList
                                        .add(getRelativePath(cxeBaseDir, doc));
                            }
                        }
                    }
                }
            }
        }
    }

    // obtain the file list and add or remove the
    // files selected from the selected list.
    private void prepareFileList(HttpServletRequest p_request,
            SessionManager p_sessionMgr, int p_depth, List p_pageList)
            throws EnvoyServletException, UnsupportedEncodingException
    {
        String uploadName = (String) p_sessionMgr
                .getAttribute(PARAM_UPLOAD_NAME);
        String downloadName = (String) p_sessionMgr
                .getAttribute(PARAM_DOWNLOAD_NAME);
        String localName = (String) p_sessionMgr.getAttribute(PARAM_LOCALE);
        File cxeBaseDir = AmbFileStoragePathUtils.getCxeDocDir();

        if (uploadName == null)
        {
            CATEGORY.warn(PARAM_UPLOAD_NAME
                    + " is null, using 'Upload' instead.");
            uploadName = "Upload";
        }

        HashSet fileList = getFileList(p_sessionMgr);
        String fileAction = p_request.getParameter("fileAction");
        if (fileAction != null)
        {
            // All the checkboxes are named "file" so the "file" parameter value
            // comes back as an array.
            String files[] = p_request.getParameterValues("file");
            String file = null;
            if (fileAction.equals("add"))
            {
                // Add the selected files to the import list.
                String webservice = (String) p_sessionMgr
                        .getAttribute(DESKTOP_FOLDER);
                for (int i = 0; i < files.length; i++)
                {
                    try
                    {
                        file = URLDecoder.decode(files[i].toString(), "UTF-8");
                    }
                    catch (Exception e)
                    {
                        file = files[i].toString();
                    }

                    File addFile = new File(getAbsolutePath(file));
                    if (addFile.isDirectory())
                    {
                        if (p_depth == 1)
                        {
                            // only recurse if the dir has upload in the name
                            if (addFile.getName().equals(downloadName) == true)
                            {
                                recurseFileStructures(addFile, fileList,
                                        p_depth + 1, downloadName, p_pageList);
                            }
                            if (webservice != null
                                    && webservice.equals(DESKTOP_FOLDER))
                            {
                                recurseFileStructures(addFile, fileList,
                                        p_depth, downloadName, p_pageList);
                            }
                        }
                        else
                        {
                            if (p_depth == 0 && webservice != null
                                    && webservice.equals(DESKTOP_FOLDER))
                            {
                                addFile = new File(addFile, webservice);
                            }
                            recurseFileStructures(addFile, fileList,
                                    p_depth + 1, downloadName, p_pageList);
                        }
                    }
                    else
                    {
                        if (p_pageList != null && p_pageList.size() > 0)
                        {
                            Iterator pages = p_pageList.iterator();
                            while (pages.hasNext())
                            {
                                String fileName = (String) pages.next();
                                if (addFile.getName().equals(fileName))
                                {
                                    fileList.add(getRelativePath(cxeBaseDir,
                                            addFile));
                                }
                            }
                        }
                    }
                }
            }
            else if (fileAction.equals("remove"))
            {
                // Remove files from the import list.
                for (int i = 0; i < files.length; i++)
                {
//                    file = URLDecoder.decode(files[i].toString(), "UTF-8");
                    file = files[i].toString();
                    fileList.remove(file);
                }
            }
            else if (fileAction.equals("download"))
            {
                // Fix for GBS-1570, get the selected files list string
                String selectedFilesListStr = p_request
                        .getParameter("selectedFileList");
                if (selectedFilesListStr != null
                        && !selectedFilesListStr.equals(""))
                {
                    String[] selectedFiles = selectedFilesListStr.split(",");
                    for (int i = 0; i < selectedFiles.length; i++)
                    {
                        try
                        {
                            selectedFiles[i] = URLDecoder.decode(
                                    selectedFiles[i], "UTF-8");
                        }
                        catch (Exception e)
                        {
                            CATEGORY.error("Failed to decode fileNames: "
                                    + selectedFilesListStr, e);
                        }
                    }
                    HashSet selectedFilesList = new HashSet();
                    for (int i = 0; i < selectedFiles.length; i++)
                    {

                        file = selectedFiles[i].toString();
                        selectedFilesList.add(file);
                    }
                    fileList = selectedFilesList;
                }
                try
                {
                    doDownload(p_request, fileList, downloadName, localName);
                }
                catch (Exception e)
                {
                    CATEGORY.error("Failed to do customer download:", e);
                    throw new EnvoyServletException(e);
                }
            }
        }
    }

    /**
     * Zips the selected files into a zip file and sets values related to it on
     * the request: zipUrl, zipFileName, and zipFileSize
     * 
     * which the applet can use to retrieve the file.
     * 
     * @exception Exception
     */
    private void doDownload(HttpServletRequest p_request, HashSet p_fileList,
            String jobName, String locale) throws Exception
    {
        // make a dir under filestorage for the .zip to live temporarily
        // File tmpDir = AmbFileStoragePathUtils.getCustomerDownloadDir();
        HttpSession session = p_request.getSession(true);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        Long jobCompanyId = (Long) sessionMgr
                .getAttribute(PARAM_JOB_COMPANY_ID);
        String fileStorageDirPathForJobCompany = AmbFileStoragePathUtils
                .getFileStorageDirPath(jobCompanyId);
        Job job = ServerProxy.getJobHandler().getJobByJobName(jobName);
        if (job == null) {
            job = ServerProxy.getJobHandler().getJobById(Long.parseLong(jobName));
            if (job == null)
                return;
        }
        
        File tmpDir = new File(fileStorageDirPathForJobCompany,
                AmbFileStoragePathUtils.CUSTOMER_DOWNLOAD_SUB_DIR
                        + File.separator + job.getJobId());
        tmpDir.mkdirs();
        File zipFile = File
                .createTempFile("GSCustomerDownload", ".jar", tmpDir);

        File[] entryFiles = new File[p_fileList.size()];
        String[] lastModifiedTimes = new String[p_fileList.size()];
        Iterator iter = p_fileList.iterator();
        int i = 0;
        File cxeDocsDir = AmbFileStoragePathUtils.getCxeDocDir();
        while (iter.hasNext())
        {
            String fileName = (String) iter.next();
            File realFile = new File(cxeDocsDir, fileName);
            lastModifiedTimes[i] = String.valueOf(realFile.lastModified());
            entryFiles[i++] = realFile;
        }

        ZipIt.addEntriesToZipFile(zipFile, entryFiles);
        Long zipFileSize = Long.valueOf(zipFile.length());
        StringBuffer zipUrl = new StringBuffer();
        zipUrl.append("/globalsight/")
                .append(AmbFileStoragePathUtils.CUSTOMER_DOWNLOAD_SUB_DIR)
                .append("/").append(job.getJobId()).append("/")
                .append(zipFile.getName());
        StringBuffer lastModifiedTimesStr = new StringBuffer(
                lastModifiedTimes[0]);
        for (int j = 1; j < lastModifiedTimes.length; j++)
        {
            lastModifiedTimesStr.append(",");
            lastModifiedTimesStr.append(lastModifiedTimes[j]);
        }
        StringBuffer fileNames = new StringBuffer(URLEncoder.encode(
                entryFiles[0].getName(), "UTF-8"));
        for (int j = 1; j < entryFiles.length; j++)
        {
            fileNames.append(",");
            fileNames
                    .append(URLEncoder.encode(entryFiles[j].getName(), "UTF-8"));
        }
        p_request.setAttribute("jobName", jobName);
        p_request.setAttribute("locale", locale);
        p_request.setAttribute("zipFileSize", zipFileSize);
        p_request.setAttribute("lastModifiedTimes", lastModifiedTimesStr);
        p_request.setAttribute("fileNames", fileNames);
        p_request.setAttribute("zipUrl", zipUrl);
        p_request.setAttribute("zipFileName", zipFile.getName());
    }

    private HashSet getFileList(SessionManager p_sessionMgr)
    {
        HashSet fileList = (HashSet) p_sessionMgr.getAttribute(FILE_LIST);
        if (fileList == null)
        {
            fileList = new HashSet();
            p_sessionMgr.setAttribute(FILE_LIST, fileList);
        }
        return fileList;
    }

    public static String getRelativePath(File p_parent, File p_absolute)
    {
        String parent;

        if (p_parent.getPath().endsWith(File.separator))
            parent = p_parent.getPath();
        else
            parent = p_parent.getPath() + File.separator;

        String absolute = p_absolute.getPath();

        return absolute.substring(parent.length());
    }

    public static String getAbsolutePath(String p_absolute)
    {
        return AmbFileStoragePathUtils.getCxeDocDir().getPath()
                + File.separator + p_absolute;
    }

    public static File getCXEBaseDir()
    {
        return AmbFileStoragePathUtils.getCxeDocDir();
    }
}
