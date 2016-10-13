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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.util.comparator.FileComparator;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.javabean.NavigationBean;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.tasks.TaskSearchUtil;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.ling.common.URLDecoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.StringUtil;
import com.globalsight.util.zip.ZipIt;

/**
 * Supports the JSP download UI for customer download
 */
public class DownloadFileHandler extends PageHandler
{
	private static final Logger logger = Logger
			.getLogger(DownloadFileHandler.class);
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
    public static final String PARAM_COMPANY_FOLDER_PATH = "companyFolderPath";
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
    
    private boolean isDownloaded = false;

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
        
        // permission check
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        boolean canDownloadJob = userPerms.getPermissionFor(Permission.JOBS_DOWNLOAD);
        boolean showAllJobsPerm = userPerms.getPermissionFor(Permission.JOB_SCOPE_ALL);
        if (!canDownloadJob && !userPerms.getPermissionFor(Permission.ACTIVITIES_DOWNLOAD))
        {
            logger.error("User doesn't have the permission to download files via this page.");
            p_response.sendRedirect("/globalsight/ControlServlet?");
            return;
        }
        
        String action = p_request.getParameter("action");
        String jobIds = p_request.getParameter(PARAM_JOB_ID);
        String wfIds = p_request.getParameter(PARAM_WORKFLOW_ID);
        String taskId = p_request.getParameter(TASK_ID);
        String tasksTate = p_request.getParameter(TASK_STATE);
        p_request.setAttribute(PARAM_JOB_ID, jobIds);
        p_request.setAttribute(PARAM_WORKFLOW_ID, wfIds);
        p_request.setAttribute(PARAM_COMPANY_FOLDER_PATH, 
        		getCompanyFolderPath(sessionMgr, jobIds));
        p_request.setAttribute(TASK_ID, taskId);
        p_request.setAttribute(TASK_STATE, tasksTate);
        
        if("getDownloadFileList".equals(action))
        {
        	User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        	boolean isTaskAssignees = false;
        	if(StringUtil.isNotEmpty(taskId) && !taskId.equals("null"))
        	{       	
        		TaskImpl taskImpl = HibernateUtil.get(TaskImpl.class, new Long(taskId));
        		TaskSearchUtil.setAllAssignees(taskImpl);
        		Long jobId = taskImpl.getJobId();
        		if(taskImpl.getAllAssignees().contains(user.getUserId())
        				&& jobId.equals(new Long(jobIds)))
        			isTaskAssignees = true;
        	}
        	
        	Set<Long> jobIdSet = new HashSet<Long>();
        	for(String jobIdStr: StringUtil.split(jobIds, ","))
        	{
        		jobIdSet.add(Long.parseLong(jobIdStr));
        	}
        	Set<Long> wfIdSet = new HashSet<Long>();
        	if(!wfIds.equals("null"))
        	{
        		for(String wfIdStr: StringUtil.split(wfIds, ","))
            	{
        			wfIdSet.add(Long.parseLong(wfIdStr));
            	}
        	}
        	List<DownloadFile> downloadFiles = getDownloadFiles(jobIdSet, wfIdSet, 
        			sessionMgr, isTaskAssignees, canDownloadJob, showAllJobsPerm, user);
        	p_response.setContentType("text/html;charset=UTF-8");
        	p_response.getWriter().write(getJSON(downloadFiles));
        	return;
        }
        isDownloaded = false;
        // fix for GBS-1449
        if (CompanyThreadLocal.getInstance().fromSuperCompany())
        {
            sessionMgr.setAttribute(CompanyType, "superCompany");
        }
        else
        {
            sessionMgr.setAttribute(CompanyType, "commonCompany");
        }

        String fileAction = p_request.getParameter("fileAction");
        if (fileAction != null && fileAction.equals("download"))
        {     	
        	prepareFileList(p_request, p_response, sessionMgr);
        }

        if (!isDownloaded)
        {
            // forward to the jsp page.
            RequestDispatcher requestDispatcher = p_context
                    .getRequestDispatcher(p_pageDescriptor.getJspURL());
            requestDispatcher.forward(p_request, p_response);
        }
    }
    
    private void prepareFileList(HttpServletRequest p_request,
            HttpServletResponse p_response, SessionManager p_sessionMgr)
            throws EnvoyServletException, UnsupportedEncodingException
    {
        String uploadName = (String) p_sessionMgr
                .getAttribute(PARAM_UPLOAD_NAME);
        HashSet<Long> downloadJobIds = (HashSet<Long>) p_sessionMgr
                .getAttribute(PARAM_DOWNLOAD_NAME);
        p_request.getParameter("selectedFileList");

        if (uploadName == null)
        {
            CATEGORY.warn(PARAM_UPLOAD_NAME
                    + " is null, using 'Upload' instead.");
            uploadName = "Upload";
        }

        HashSet<String> fileList = new HashSet<String>();
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
            for (int i = 0; i < selectedFiles.length; i++)
            {
                fileList.add(selectedFiles[i].toString());
            }
        }
        
        try
        {
            doDownload(p_request, p_response, fileList, downloadJobIds);
        }
        catch (Exception e)
        {
            CATEGORY.error("Failed to do customer download with exception:", e);
            throw new EnvoyServletException(e);
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
    private void doDownload(HttpServletRequest p_request,
            HttpServletResponse p_response, HashSet<String> p_fileList,
            HashSet<Long> downloadJobIds) throws Exception
    {
        HttpSession session = p_request.getSession(true);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        Long jobCompanyId = (Long) sessionMgr
                .getAttribute(PARAM_JOB_COMPANY_ID);
        String fileStorageDirPathForJobCompany = AmbFileStoragePathUtils
                .getFileStorageDirPath(jobCompanyId);
        StringBuffer names = new StringBuffer();
        for(Long jobId:downloadJobIds)
        {
        	names.append(jobId).append(",");
        }
	        
        File tmpDir = new File(fileStorageDirPathForJobCompany,
                AmbFileStoragePathUtils.CUSTOMER_DOWNLOAD_SUB_DIR);
        tmpDir.mkdirs();
        File zipFile = File
                .createTempFile("GSCustomerDownload", ".zip", tmpDir);

        Set<File> entryFiles = new HashSet<File>();
        Set<String> locales = new HashSet<String>();
        Set<Long> jobIdSet = new HashSet<Long>();
        Iterator<String> iter = p_fileList.iterator();
        File cxeDocsDir = AmbFileStoragePathUtils.getCxeDocDir(jobCompanyId);
        while (iter.hasNext())
        {
            String fileName = iter.next();
            String fileName2 = fileName;
            if (fileName.contains("/"))
            {
                fileName2 = fileName.replace("/", "\\");
            }
            
            String temps = fileName2.length() > 30 ? fileName2.substring(0, 30) : fileName2;
            for(Long jobId : downloadJobIds)
            {
                if (temps.contains("\\" + jobId + "\\"))
                {
                	jobIdSet.add(jobId);
                }
            }

            String fileLocale = fileName2.substring(0, fileName2.indexOf("\\"));
            locales.add(fileLocale);

            File realFile = new File(cxeDocsDir, fileName);
            entryFiles.add(realFile);
        }
        
        Map<File, String> entryFileToFileNameMap = getEntryFileToFileNameMap(entryFiles, 
        		jobIdSet, locales, cxeDocsDir.getPath());
        ZipIt.addEntriesToZipFile(zipFile, entryFileToFileNameMap, "");
        
        String downloadFileName = zipFile.getName();
        if (jobIdSet!= null && jobIdSet.size() == 1)
        {
            Long ji = jobIdSet.iterator().next();
            long jobId = -1;
            String jobname = ji.toString();

            try
            {
                jobId = ji;
                Job j = ServerProxy.getJobHandler().getJobById(jobId);
                jobname = j.getJobName();
            }
            catch (Exception e)
            {
                jobname = ji.toString();
			}

			String tempS = locales.toString();
			String localestr = tempS.substring(1, tempS.length() - 1);
			localestr = locales.size() == 1 ? localestr : "Languages("
					+ localestr + ")";
			
			String isCheckedName = p_request.getParameter("isChecked");
			if ("true".equals(isCheckedName))
			{
				downloadFileName = (jobId > -1 ? jobId + "_" : "") + jobname
						+ "_" + localestr + ".zip";
			}
			else
			{
				downloadFileName = (jobId > -1 ? jobId + "_" : "") + jobname
						 + ".zip";
			}
		}
        else if (jobIdSet!= null && jobIdSet.size() > 1)
        {
            String tempS = jobIdSet.toString();
            String jobNamesstr =  tempS.substring(1, tempS.length() - 1);
            downloadFileName = "GlobalSight_Download_jobs(" + jobNamesstr + ").zip";
        }
        
        // write zip file to client
        p_response.setContentType("application/zip");
        p_response.setHeader("Content-Disposition",
                "attachment; filename=\"" + downloadFileName
                        + "\";");
        if (p_request.isSecure())
        {
            PageHandler.setHeaderForHTTPSDownload(p_response);
        }
        p_response.setContentLength((int) zipFile.length());

        // Send the data to the client
        byte[] inBuff = new byte[4096];
        FileInputStream fis = new FileInputStream(zipFile);
        int bytesRead = 0;
        while ((bytesRead = fis.read(inBuff)) != -1)
        {
            p_response.getOutputStream()
                    .write(inBuff, 0, bytesRead);
        }

        if (bytesRead > 0)
        {
            p_response.getOutputStream()
                    .write(inBuff, 0, bytesRead);
        }

        fis.close();

        isDownloaded = true;

        try {
            FileUtil.deleteFile(zipFile);            
        } catch (Exception ignore) {
            
        } finally {
            if (zipFile.exists())
                zipFile.deleteOnExit();
        }

        /*
        Long zipFileSize = Long.valueOf(zipFile.length());
        StringBuffer zipUrl = new StringBuffer();
        zipUrl.append("/globalsight/")
                .append(AmbFileStoragePathUtils.CUSTOMER_DOWNLOAD_SUB_DIR)
                .append("/").append(zipFile.getName());
        StringBuffer lastModifiedTimesStr = new StringBuffer();
        if(lastModifiedTimes.length > 0)
        {
        	lastModifiedTimesStr.append(lastModifiedTimes[0]);
	        for (int j = 1; j < lastModifiedTimes.length; j++)
	        {
	            lastModifiedTimesStr.append(",");
	            lastModifiedTimesStr.append(lastModifiedTimes[j]);
	        }
        }
        StringBuffer fileNames = new StringBuffer();
        if(entryFiles.length > 0)
        {        	
        	fileNames.append(URLEncoder.encode(
                entryFiles[0].getName(), "UTF-8"));
        	for (int j = 1; j < entryFiles.length; j++)
        	{
        		fileNames.append(",");
        		fileNames
        		.append(URLEncoder.encode(entryFiles[j].getName(), "UTF-8"));
        	}
        }
        p_request.setAttribute("jobNames", names);
        p_request.setAttribute("locale", locale);
        p_request.setAttribute("zipFileSize", zipFileSize);
        p_request.setAttribute("lastModifiedTimes", lastModifiedTimesStr);
        p_request.setAttribute("fileNames", fileNames);
        p_request.setAttribute("zipUrl", zipUrl);
        p_request.setAttribute("zipFileName", zipFile.getName());
        */
    }
    
    private String getJSON(List<DownloadFile> p_list)
    {
        if (p_list == null || p_list.size() == 0)
        {
            return "[]";
        }

        StringBuilder result = new StringBuilder();
        result.append("[");
        for (DownloadFile DownloadFile : p_list)
        {
            result.append(DownloadFile.toJSON()).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        result.append("]");
        return result.toString();
    }
    
    private List<DownloadFile> getDownloadFiles(Set<Long> jobIdSet, Set<Long> wfIdSet, 
    		SessionManager p_sessionMgr, boolean isTaskAssignees ,boolean canDownloadJob,
    		boolean showAllJobsPerm, User user)
    {
        List<DownloadFile> DownloadFiles = new ArrayList<DownloadFile>();
        Long companyId = (Long) p_sessionMgr.getAttribute(PARAM_JOB_COMPANY_ID);
        String companyFolderPath = AmbFileStoragePathUtils.getCxeDocDirPath(companyId);
        File companyFolder = new File(companyFolderPath);
        File[] localeFloders = companyFolder.listFiles();
        List<String> localesList = getDownloadLocaleList(jobIdSet, wfIdSet);
        Arrays.sort(localeFloders);
        for (File localeFolder : localeFloders)
        {
        	boolean isPassoloFolder = false;
        	if(localeFolder.getPath().endsWith(File.separator + "passolo"))
        	{
        		isPassoloFolder = true;
        	}
        	String filePath = localeFolder.getPath();
        	boolean isContainLocale = false;
        	String presentLocale = "";
        	for(String locale: localesList)
        	{
        		if(filePath.endsWith(locale))
        		{      			
        			isContainLocale = true;
        			presentLocale = locale;
        			break;
        		}
        	}
        	if(isPassoloFolder)
        		isContainLocale = true;
            if (localeFolder.isFile() || FileUtil.isEmpty(localeFolder) || !isContainLocale)
                continue;
            DownloadFile localeFile = new DownloadFile(localeFolder);
            String localePath = localeFolder.getPath();
            Long[] jobIds = jobIdSet.toArray(new Long[jobIdSet.size()]);
            Arrays.sort(jobIds);
        	for(Long jobId: jobIds)
        	{
				try
				{
					Job job = ServerProxy.getJobHandler().getJobById(jobId);
//					if(!isShowJob(companyId, showAllJobsPerm,job,user,
//							isTaskAssignees, canDownloadJob))
//					{
//						continue;
//					}
					isContainLocale = false;
					for (Workflow wf : job.getWorkflows())
					{
						if(presentLocale.equals(wf.getTargetLocale().toString()))
						{                        		
							isContainLocale = true;
						}
					}
					if(isPassoloFolder)
						isContainLocale = true;
					
					if(isContainLocale)
					{
						File tempFile = new File(localePath + File.separator + jobId);
						File tempWebserviceFile = new File(localePath + File.separator + "webservice" 
								+ File.separator + jobId);
						if(tempFile.exists() && !tempFile.isFile() && !FileUtil.isEmpty(tempFile))
						{
							DownloadFile downloadFile = new DownloadFile(tempFile);
							setChildren(downloadFile, tempFile);
			                if (downloadFile.getChildren() != null)
			                {
			                	localeFile.addChildren(downloadFile);
			                }
						}
						else if(tempWebserviceFile.exists() && !tempWebserviceFile.isFile() 
								&& !FileUtil.isEmpty(tempWebserviceFile))
						{
							DownloadFile downloadFile = new DownloadFile(tempWebserviceFile);
							setChildren(downloadFile, tempWebserviceFile);
			                if (downloadFile.getChildren() != null)
			                {
			                	localeFile.addChildren(downloadFile);
			                }
						}
					}
				} 
				catch (Exception e)
        		{
        			CATEGORY.error("Failed to get job " + jobId, e);
        		}
        	}       

            if (localeFile.getChildren() != null)
            {
                DownloadFiles.add(localeFile);
            }
        }

        return DownloadFiles;
    }
     
//    private boolean isShowJob(Long companyId, boolean showAllJobsPerm, Job job, User user,
//    		boolean isTaskAssignees, boolean canDownloadJob)
//    {
//    	boolean isSuperCompany = CompanyWrapper.isSuperCompany(companyId.toString());
//    	boolean showAlljobs = false;
//		if(showAllJobsPerm)
//		{
//			if(isSuperCompany || job.getCompanyId() == companyId)
//			{
//				showAlljobs = true;
//			}
//		}
//		
//		if(!job.getProject().getUserIds().contains(user.getUserId()) ||
//				!(isTaskAssignees || canDownloadJob))
//		{
//			if(!showAlljobs)
//				return false;
//		}
//		
//		return true;
//    }
    
    private List<String> getDownloadLocaleList(Set<Long> jobIdSet,
			Set<Long> wfIdSet) 
	{
    	List<String> localesList = new ArrayList<String>();
    	
    	if(wfIdSet.size() > 0)
        {
        	for(Long wfId: wfIdSet)
        	{      		
        		try 
        		{
					Workflow wf = ServerProxy.getWorkflowManager()
							.getWorkflowById(wfId);
					if(!localesList.contains(wf.getTargetLocale().toString()))
    				{                        		
    					localesList.add(wf.getTargetLocale().toString());
    				}
				}
        		catch (Exception e)
        		{
        			CATEGORY.error("Failed to get wf " + wfId, e);
        		}
        	}
        }
        else 
        {		
        	for(Long jobId: jobIdSet)
        	{
        		try 
        		{
        			Job job = ServerProxy.getJobHandler()
        					.getJobById(jobId);
        			for (Workflow wf : job.getWorkflows())
        			{
        				if(!localesList.contains(wf.getTargetLocale().toString()))
        				{                        		
        					localesList.add(wf.getTargetLocale().toString());
        				}
        			}
        		}
        		catch (Exception e)
        		{
        			CATEGORY.error("Failed to get job " + jobId, e);
        		}
        	}
		}
    	return localesList;
	}

	private void setChildren(DownloadFile p_downloadFolder, File p_folder)
    {
        if (p_folder.isFile() || FileUtil.isEmpty(p_folder))
            return;

        List<DownloadFile> children = new ArrayList<DownloadFile>();
        File[] files = p_folder.listFiles();
        Arrays.sort(files, new FileComparator(0, null, true));
        Set<String> xlzFolders = new HashSet<String>();
        for (File file : files)
        {
        	// is eloqua obj file
        	String name = file.getName();
        	if (name.startsWith("(") && name.endsWith(".obj"))
        	{
        		continue;
        	}
        	
            DownloadFile downloadFolder = new DownloadFile(file);
            if (file.isDirectory() && !FileUtil.isEmpty(file))
            {
                setChildren(downloadFolder, file);
            }
            children.add(downloadFolder);
            
            if(file.isFile() && file.getName().endsWith(".xlz"))
            {
            	xlzFolders.add(file.getName().substring(0, file.getName().length() - 4));
            }
        }
        
        List<DownloadFile> xlzFolderList = new ArrayList<DownloadFile>();
        for(DownloadFile downloadFile: children)
        {
        	if(xlzFolders.contains(downloadFile.getTitle()))
        	{
        		xlzFolderList.add(downloadFile);
        	}
        }
        children.removeAll(xlzFolderList);
        
        p_downloadFolder.setChildren(children);
    }
	
	private String getCompanyFolderPath(SessionManager p_sessionMgr, String p_jobIds)
	{
		if(p_jobIds == null)
		{
			return AmbFileStoragePathUtils.getCxeDocDirPath();
		}
		else 
		{		
			Set<String> jobIdSet = new HashSet<String>();
			if(p_jobIds.indexOf(",") > 0)
			{		
				jobIdSet = StringUtil.split(p_jobIds, ",");
			}
			else 
			{
				jobIdSet = StringUtil.split(p_jobIds, " ");
			}
			
			long companyId = new Long("0");
			String uploadName = "";
			Set<Long> downloadJobIds = new HashSet<Long>();
			for(String jobId: jobIdSet)
			{
				try 
				{
					Job job = ServerProxy.getJobHandler().getJobById(Long.parseLong(jobId));
					companyId = job.getCompanyId();
					uploadName = job.getJobName() + " ";
					downloadJobIds.add(job.getId());
				}
				catch (Exception e)
				{
					CATEGORY.error("Failed to get job " + jobId, e);
				}
			}
			p_sessionMgr.setAttribute(PARAM_UPLOAD_NAME, uploadName.substring(0,
					uploadName.length() - 1));
			p_sessionMgr.setAttribute(PARAM_DOWNLOAD_NAME,
					downloadJobIds);
			p_sessionMgr.setAttribute(PARAM_JOB_COMPANY_ID, companyId);
			return AmbFileStoragePathUtils.getCxeDocDirPath(companyId);
		}
	}
	
	private Map<File, String> getEntryFileToFileNameMap(Set<File> entryFiles, 
			Set<Long> jobIdSet, Set<String> locales, String cxeDocsDirPath)
	{
		Map<File, String> entryFileToFileNameMap = new HashMap<File, String>();
		File tempFile;
		
		for(Long jobId: jobIdSet)
		{
			ArrayList<String> entryNames = new ArrayList<String>();
			String prefixPassolo = cxeDocsDirPath + File.separator + "passolo" + File.separator 
					+ jobId;
			for(File entryFile: entryFiles)
			{
				String entryFilePath = entryFile.getPath();
				if(entryFilePath.startsWith(prefixPassolo))
				{
					entryNames.add(entryFilePath.replaceAll("\\\\", "/"));
				}					
			}
			if(entryNames.size() > 0)
			{				
				Map<String, String> tempMap = ZipIt.getEntryNamesMap(entryNames);
				for(String key: tempMap.keySet())
				{
					tempFile = new File(key);
					entryFileToFileNameMap.put(tempFile, jobId + File.separator + "passolo" 
							+ File.separator + tempMap.get(key));
				}
			}
			
			for(String locale: locales)
			{
				entryNames.clear();
				String prefixStr1 = cxeDocsDirPath + File.separator + locale + File.separator 
						+ jobId;
				String prefixStr2 = cxeDocsDirPath + File.separator + locale + File.separator 
						+ "webservice" + File.separator + jobId;
				for(File entryFile: entryFiles)
				{
					String entryFilePath = entryFile.getPath();
					if(entryFilePath.startsWith(prefixStr1) ||
							entryFilePath.startsWith(prefixStr2))
					{
						entryNames.add(entryFilePath.replaceAll("\\\\", "/"));
					}					
				}
				if(entryNames.size() > 0)
				{				
					Map<String, String> tempMap = ZipIt.getEntryNamesMap(entryNames);
					for(String key: tempMap.keySet())
					{
						tempFile = new File(key);
						entryFileToFileNameMap.put(tempFile, jobId + File.separator + locale 
								+ File.separator + tempMap.get(key));
					}
				}
			}
		}
		return entryFileToFileNameMap;
	}
	
}
