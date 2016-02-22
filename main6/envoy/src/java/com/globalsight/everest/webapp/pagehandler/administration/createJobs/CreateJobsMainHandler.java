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
package com.globalsight.everest.webapp.pagehandler.administration.createJobs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParameterImpl;
import com.globalsight.config.UserParameterPersistenceManagerLocal;
import com.globalsight.cxe.engine.util.FileUtils;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.customAttribute.Condition;
import com.globalsight.cxe.entity.customAttribute.DateCondition;
import com.globalsight.cxe.entity.customAttribute.FloatCondition;
import com.globalsight.cxe.entity.customAttribute.IntCondition;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.customAttribute.ListCondition;
import com.globalsight.cxe.entity.customAttribute.TextCondition;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.entity.fileprofile.FileProfileUtil;
import com.globalsight.cxe.message.CxeMessage;
import com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.cxe.util.fileImport.FileImportRunnable;
import com.globalsight.cxe.util.fileImport.FileImportUtil;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.glossaries.GlossaryException;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManagerException;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

import de.innosystec.unrar.rarfile.FileHeader;

public class CreateJobsMainHandler extends PageHandler
{
    public static final String TMP_FOLDER_NAME = "createJob_tmp";

    private static final Logger logger = Logger
            .getLogger(CreateJobsMainHandler.class);
    private static final String DOT = ".";
    private static final String DISABLED = "disabled";
    private static final String SELECTED = "selected";
    private static final String SELECTED_FOLDER = "selected_folder_path_in_create_job";
    public static final String CREATING_JOBS_NUM_SQL = "select count(ID) from JobImpl "
    	+ " where STATE in ('" + Job.UPLOADING + "', '" + Job.IN_QUEUE
    	+ "', '" + Job.EXTRACTING + "', '" + Job.LEVERAGING + "', '"
    	+ Job.CALCULATING_WORD_COUNTS + "', '" + Job.PROCESSING + "')";
    private static final int SUCCEED = 0;
    private static final int FAIL = 1;
    private final static int MAX_LINE_LENGTH = 4096;
    private Map<String, List<FileProfileImpl>> extensionToFileProfileMap;
    private Map<String, String> l10NToTargetLocalesMap = new HashMap<String, String>();
//    private Map<String, String> l10NToJobAttributeMap = new HashMap<String, String>();

    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        // permission check
        HttpSession session = request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.CREATE_JOB)
                && !userPerms.getPermissionFor(Permission.CREATE_JOB_NO_APPLET))
        {
            logger.error("User doesn't have the permission to create jobs via this page.");
            response.sendRedirect("/globalsight/ControlServlet?");
            return;
        }
        // get the operator
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user == null)
        {
            String userName = request.getParameter("userName");
            if (userName != null && !"".equals(userName))
            {
                user = ServerProxy.getUserManager().getUserByName(userName);
                sessionMgr.setAttribute(WebAppConstants.USER, user);
            }
        }

        ResourceBundle bundle = PageHandler.getBundle(session);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();
        String action = request.getParameter(UPLOAD_ACTION);
        if (action != null)
        {
            if (action.equals("queryTargetLocales"))
            {
                this.queryTargetLocales(request, response, user);
                return;
            }
            else if (action.equals("queryFileProfile"))
            {
            	this.setPageParameter(request, bundle, user, session,
                        currentCompanyId);
                this.queryFileProfile(request, response, currentCompanyId, user);
                return;
            }
            else if (action.equals("deleteFile"))
            {
                this.deleteFile(request);
                return;
            }
            else if (action.equals("getAttributes"))
            {
                this.queryJobAttributes(request, response);
                return;
            }
            else if (action.equals("getUploadedFiles"))
            {
                this.getUploadedFiles(request, response);
                return;
            }
            else if (action.equals("getCreatingJobsNum"))
            {
                Integer creatingJobsNum = getCreatingJobsNum(new Long(
                        currentCompanyId));
                PrintWriter writer = response.getWriter();
                writer.write(creatingJobsNum.toString());
                return;
            }
            else if (action.equals("uploadSelectedFile"))
            {
                String tempFolder = request.getParameter("tempFolder");
                String type = request.getParameter("type");
                List<File> uploadedFiles = new ArrayList<File>();
                try
                {
					uploadedFiles = uploadSelectedFile(request, tempFolder,	type);
					for (File uploadedFile : uploadedFiles)
					{
                    	StringBuffer ret = new StringBuffer("[");
                    	response.setContentType("text/html;charset=UTF-8");
                    	if ("0".equals(type))// source files
                    	{
                    		if (isSupportedZipFileFormat(uploadedFile)
                    				&& isUnCompress(uploadedFile))
                    		{
                    			if (CreateJobUtil.isZipFile(uploadedFile))
                    			{
                    				ret.append(addZipFile(uploadedFile));
                    			}
                    			else if (CreateJobUtil.isRarFile(uploadedFile))
                    			{
                    				ret.append(addRarFile(uploadedFile));
                    			}
                    			else if (CreateJobUtil.is7zFile(uploadedFile))
                    			{
                    				ret.append(addZip7zFile(uploadedFile));
                    			}
                    			else
                    			{
                    				ret.append(addCommonFile(uploadedFile));
                    			}
                    			
                    			ret.append("]");
                    			PrintWriter writer = response.getWriter();
                    			writer.write("<script type='text/javascript'>window.parent.addDivForNewFile("
                    					+ ret.toString() + ")</script>;");
                    			if (CreateJobUtil.isZipFile(uploadedFile)
                    					|| CreateJobUtil.isRarFile(uploadedFile)
                    					|| (CreateJobUtil.is7zFile(uploadedFile)))
                    			{
                    				uploadedFile.delete();
                    			}
                    		}
                    		else
                    		{
                    			ret.append(addCommonFile(uploadedFile));
                    			ret.append("]");
                    			PrintWriter writer = response.getWriter();
                    			writer.write("<script type='text/javascript'>window.parent.addDivForNewFile("
                    					+ ret.toString() + ")</script>;");
                    		}
                    	}
                    	else if ("1".equals(type))// job comment file
                    	{
                    		PrintWriter writer = response.getWriter();
                    		writer.write("<script type='text/javascript'>window.parent.addAttachment('"
                    				+ uploadedFile.getName().replace("'", "\\'")
                    				+ "')</script>;");
                    	}
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return;
            }
            else if (action.equals("createJob"))
            {
                final Map<Object, Object> dataMap = prepareDataForJob(request,
                        currentCompanyId, user);
                this.setPageParameter(request, bundle, user, session,
                        currentCompanyId);
                request.setAttribute("create_result",
                        bundle.getString("msg_job_create_successful"));
                Runnable runnable = new Runnable()
                {
                    public void run()
                    {
                        createJobs(dataMap);
                    }
                };
                Thread t = new MultiCompanySupportedThread(runnable);
                t.start();
            }
        }
        if (!"createJob".equals(action))
        {
            this.setPageParameter(request, bundle, user, session,
                    currentCompanyId);
        }

        // how many jobs are being created
        Integer creatingJobsNum = getCreatingJobsNum(new Long(currentCompanyId));
        request.setAttribute("creatingJobsNum", creatingJobsNum);

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    /**
     * Try to decompress "zip", "rar" or "7z" file to see if it can be
     * decompressed successfully.
     */
    private boolean isUnCompress(File uploadedFile) throws Exception
    {
        boolean result = false;
        if (CreateJobUtil.isZipFile(uploadedFile))
        {
            result = CreateJobUtil.unzipFile(uploadedFile);
        }
        else if (CreateJobUtil.isRarFile(uploadedFile))
        {
            result = CreateJobUtil.unrarFile(uploadedFile);
        }
        else if (CreateJobUtil.is7zFile(uploadedFile))
        {
            result = CreateJobUtil.un7zFile(uploadedFile);
        }

        return result;
    }

    private boolean isSupportedZipFileFormat(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if ("rar".equalsIgnoreCase(extension)
                || "zip".equalsIgnoreCase(extension)
                || "7z".equalsIgnoreCase(extension))
        {
            return true;
        }
        return false;
    }

    public static Integer getCreatingJobsNum(Long companyId)
    {
    	Integer creatingJobsNum = null;
    	try
        {
    		boolean isSuperCompany = CompanyWrapper.isSuperCompany(companyId.toString());
    		if(isSuperCompany)
    		{ 			
    			creatingJobsNum = HibernateUtil.count(CREATING_JOBS_NUM_SQL);
    		}
    		else
    		{
    			creatingJobsNum = HibernateUtil.count(CREATING_JOBS_NUM_SQL 
    					+ " and COMPANY_ID = " + companyId);
    		}
        }
        catch (Exception e)
        {
        	logger.error("Failed to get createingJobsNum.", e);
            // not blocking the following processes.
        }
        return creatingJobsNum;
    }

    private Map<Object, Object> prepareDataForJob(HttpServletRequest request,
            String currentCompanyId, User user)
    {
        Map<Object, Object> dataMap = new HashMap<Object, Object>();
        dataMap.put("currentCompanyId", currentCompanyId);
        dataMap.put("user", user);
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(SESSION_MANAGER);
        dataMap.put(SESSION_MANAGER, sessionMgr);

        String jobName = EditUtil.removeCRLF(request.getParameter("jobName"))
                + "_" + getRandomNumber();
        dataMap.put("jobName", jobName);
        String comment = request.getParameter("comment");
        dataMap.put("comment", comment);
        // This is where the files are uploaded
        String tmpFolderName = request.getParameter("tmpFolderName");
        dataMap.put("tmpFolderName", tmpFolderName);
        // full path of files
        String[] filePaths = request.getParameterValues("jobFilePath");
        dataMap.put("jobFilePath", filePaths);
        // l10n info and file profile info
        String[] l10nAndfileProfiles = request
                .getParameterValues("fileProfile");
        dataMap.put("fileProfile", l10nAndfileProfiles);
        // mapped target locales
        String[] targetLocales = request.getParameterValues("targetLocale");
        dataMap.put("targetLocale", targetLocales);
        // priority
        String priority = request.getParameter("priority");
        dataMap.put("priority", priority);
        // the selected folder path
        String baseFolder = request.getParameter("baseFolder");
        dataMap.put("baseFolder", baseFolder);
        // mark whether the file comes from server
        String[] isSwitched = request.getParameterValues("isSwitched");
        dataMap.put("isSwitched", isSwitched);
        // name of attachment
        String attachmentName = request.getParameter("attachment");
        dataMap.put("attachment", attachmentName);
        String baseStorageFolder = request.getParameter("baseStorageFolder");
        dataMap.put("baseStorageFolder", baseStorageFolder);
        String attribute = request.getParameter("attributeString");
        dataMap.put("attributeString", attribute);

        return dataMap;
    }

    /**
     * Get job attributes
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    private void queryJobAttributes(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        PrintWriter writer = response.getWriter();
        try
        {
            String l10Nid = request.getParameter("l10Nid");
            String hasAttribute = "false";
//            if (l10NToJobAttributeMap.get(l10Nid) == null)
//            {
                L10nProfile lp = ServerProxy.getProjectHandler()
                        .getL10nProfile(Long.valueOf(l10Nid));
                Project p = lp.getProject();
                AttributeSet attributeSet = p.getAttributeSet();

                if (attributeSet != null)
                {
                    List<Attribute> attributeList = attributeSet
                            .getAttributeAsList();
                    for (Attribute attribute : attributeList)
                    {
                        if (attribute.isRequired())
                        {
                            hasAttribute = "required";
                            break;
                        }
                        else
                        {
                            hasAttribute = "true";
                        }
                    }
                }
//                l10NToJobAttributeMap.put(l10Nid, hasAttribute);
//            }
//            else
//            {
//                hasAttribute = l10NToJobAttributeMap.get(l10Nid);
//            }
            response.setContentType("text/html;charset=UTF-8");
            writer.write(hasAttribute);
        }
        catch (Exception e)
        {
            logger.error("Failed to query job attributes of project.", e);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Set useful parameters onto the jsp
     * 
     * @param request
     * @param bundle
     * @param user
     * @param session
     * @param currentCompanyId
     */
    private void setPageParameter(HttpServletRequest request,
            ResourceBundle bundle, User user, HttpSession session,
            String currentCompanyId)
    {
        this.setLable(request, bundle);
        request.setAttribute("rand",
                session.getAttribute("UID_" + session.getId()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        String tmpFolderName = sdf.format(new Date()) + "-" + getRandomNumber();
        if (user != null)
        {
            request.setAttribute(
                    "lastSelectedFolder",
                    convertFilePath(
                            getLastSelectedFolder(user.getUserId(),
                                    SELECTED_FOLDER)).replace("\\", "\\\\"));
        }
        else
        {
            request.setAttribute("lastSelectedFolder", "");
        }

        request.setAttribute(
                "baseTmpFolder",
                convertFilePath(
                        AmbFileStoragePathUtils.getCxeDocDir() + File.separator
                                + TMP_FOLDER_NAME).replace("\\", "\\\\"));
        request.setAttribute("baseStorageFolder", tmpFolderName + ","
                + currentCompanyId);

        if (request.getParameter("currentFolderName") != null)
        {
            request.setAttribute("tmpFolderName",
                    convertFilePath(request.getParameter("currentFolderName")));
        }
        else
        {
            request.setAttribute("tmpFolderName", tmpFolderName);
            extensionToFileProfileMap = new HashMap<String, List<FileProfileImpl>>();
        }
        SystemConfiguration sysConfig = SystemConfiguration.getInstance();
        boolean useSSL = sysConfig
                .getBooleanParameter(SystemConfigParamNames.USE_SSL);
        if (useSSL)
        {
            request.setAttribute("httpProtocolToUse",
                    WebAppConstants.PROTOCOL_HTTPS);
        }
        else
        {
            request.setAttribute("httpProtocolToUse",
                    WebAppConstants.PROTOCOL_HTTP);
        }
    }

    /**
     * Called by ajax, search target locales for files, and init the target
     * locales checkbox on the jsp
     * 
     * @param request
     * @param response
     * @param user
     * @throws IOException
     */
    private void queryTargetLocales(HttpServletRequest request,
            HttpServletResponse response, User user) throws IOException
    {
        String l10Nid = request.getParameter("l10Nid");
        if (StringUtils.isNotEmpty(l10Nid))
        {
            String targetLocalesString = l10NToTargetLocalesMap.get(l10Nid);
            if (targetLocalesString == null)
            {
                String hsql = "select wti.targetLocale from "
                        + "L10nProfileWFTemplateInfo as ltp, WorkflowTemplateInfo wti "
                        + "where wti.id = ltp.key.wfTemplateId and ltp.key.l10nProfileId = "
                        + l10Nid
                        + " and ltp.isActive = 'Y' and wti.isActive = 'Y' "
                        + "order by wti.targetLocale.language";
                List<?> localeList = HibernateUtil.search(hsql);

                if (localeList != null)
                {
                    targetLocalesString = this.initTargetLocaleSelect(
                            localeList, user);
                    l10NToTargetLocalesMap.put(l10Nid, targetLocalesString);
                }
                else
                {
                    targetLocalesString = "";
                }
            }

            response.setContentType("text/html;charset=UTF-8");
            PrintWriter writer = response.getWriter();
            writer.write(targetLocalesString);
            writer.close();
        }
    }

    /**
     * Called by ajax, to search the file profiles for files, and init all file
     * profile selects on the jsp.
     * 
     * @param request
     * @param response
     * @param currentCompanyId
     * @param user
     * @throws IOException
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes" })
    private void queryFileProfile(HttpServletRequest request,
            HttpServletResponse response, String currentCompanyId, User user)
            throws IOException
    {
        try
        {
            String fileName = request.getParameter("fileName");
            String l10nIdStr = request.getParameter("l10Nid");
            long l10nId = 0;
            if (!StringUtils.isEmpty(l10nIdStr))
            {
                l10nId = Long.parseLong(l10nIdStr);
            }

            if (fileName != null && fileName.contains(DOT))
            {
                String fileExtension = fileName.substring(fileName
                        .lastIndexOf(DOT) + 1);
                List<FileProfileImpl> fileProfileListOfUser;

                if (extensionToFileProfileMap.get(fileExtension) != null)
                {
                    fileProfileListOfUser = extensionToFileProfileMap
                            .get(fileExtension);
                }
                else
                {
                    fileProfileListOfUser = new ArrayList<FileProfileImpl>();
                    List<String> extensionList = new ArrayList<String>();
                    extensionList.add(fileExtension);
                    List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                            .getFileProfilePersistenceManager()
                            .getFileProfilesByExtension(extensionList,
                                    Long.valueOf(currentCompanyId));
                    SortUtil.sort(fileProfileListOfCompany,
                            new Comparator<Object>()
                            {
                                public int compare(Object arg0, Object arg1)
                                {
                                    FileProfileImpl a0 = (FileProfileImpl) arg0;
                                    FileProfileImpl a1 = (FileProfileImpl) arg1;
                                    return a0.getName().compareToIgnoreCase(
                                            a1.getName());
                                }
                            });

                    List projectsOfCurrentUser = ServerProxy
                            .getProjectHandler().getProjectsByUser(
                                    user.getUserId());

                    for (FileProfileImpl fp : fileProfileListOfCompany)
                    {
                        Project fpProj = getProject(fp);
                        // get the project and check if it is in the group of
                        // user's projects
                        if (projectsOfCurrentUser.contains(fpProj))
                        {
                            fileProfileListOfUser.add(fp);
                        }
                    }
                    extensionToFileProfileMap.put(fileExtension,
                            fileProfileListOfUser);
                }

                if (fileProfileListOfUser.size() > 0)
                {
                    // the return value should be in the pattern of html
                    response.setContentType("text/html;charset=UTF-8");
                    PrintWriter writer = response.getWriter();
                    this.initFileProfileSelect(fileProfileListOfUser, writer,
                            l10nId);
                    writer.close();
                }
            }
            else
            {
                logger.warn("The file " + fileName
                        + " doesn't have a extension.");
            }
        }
        catch (Exception e)
        {
            logger.error("Query fileprofile error.", e);
        }
    }

    /**
     * When deleting a file on page, delete the file from server too. If all
     * files are removed on page, delete the whole folder.
     * 
     * @param request
     */
    private void deleteFile(HttpServletRequest request)
    {
        try
        {
            String filePath = request.getParameter("filePath");
            String folder = request.getParameter("folder");
            String uploadPath = AmbFileStoragePathUtils.getCxeDocDir()
                    + File.separator + TMP_FOLDER_NAME + File.separator
                    + folder;
            if (filePath == null)
            {
                FileUtil.deleteFile(new File(uploadPath));
            }
            else if (filePath.contains(folder))
            {
            	File file = new File(filePath);
            	file.delete();
            }
            else
            {
                filePath = convertFilePath(filePath);
                if (filePath.contains(":"))
                {
                    filePath = filePath.substring(filePath.indexOf(":") + 1);
                }
                String storagePath = uploadPath + File.separator + filePath;
                File fileToDelete = new File(storagePath);
                fileToDelete.delete();
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to delete file.", e);
        }
    }

    /**
     * Get uploaded files on the server. Current folder is not included.
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    private void getUploadedFiles(HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        PrintWriter writer = response.getWriter();
        try
        {
            String baseTmpPath = AmbFileStoragePathUtils.getCxeDocDir()
                    + File.separator + TMP_FOLDER_NAME;
            String path = request.getParameter("path") == null ? baseTmpPath
                    : request.getParameter("path");
            path = convertFilePath(path);
            String folderName = request.getParameter("folder");
            File tmpDir = new File(path);
            if (!tmpDir.exists())
            {
                return;
            }

            response.setContentType("text/html;charset=UTF-8");
            String[] fileNames = tmpDir.list();
            String data = initUploadedFolderSelect(fileNames, folderName, path);
            writer.write(data);
        }
        catch (Exception e)
        {
            logger.error("Get uploaded files error", e);
        }
        finally
        {
            writer.close();
        }
    }

    /**
     * Init the new opened window of uploaded files
     * 
     * @param fileNames
     * @param folderName
     * @param path
     * @return
     */
    private String initUploadedFolderSelect(String[] fileNames,
            String folderName, String path)
    {
        StringBuffer data = new StringBuffer();
        for (String fileName : fileNames)
        {
            // don't need to show current folder
            if (fileName.equals(folderName))
            {
                continue;
            }
            String link = convertFilePath(path + File.separator + fileName);
            File tmp = new File(link);
            // don't need to show empty folder
            if (FileUtils.isEmpty(tmp))
            {
                FileUtil.deleteFile(tmp);
                continue;
            }
            link = link.replace("\\", "\\\\");
            data.append(
                    "<div id='" + FileUtil.getFileNo(link)
                            + "' STYLE='word-wrap:break-word;'>")
                    .append("<input name='uploaded' type='checkbox' value='")
                    .append(fileName).append("'>&nbsp;");
            if (tmp.isDirectory())
            {
                data.append(
                        "<IMG SRC='/globalsight/images/folderclosed.gif' width='15' height='13'>")
                        .append("&nbsp;&nbsp;")
                        .append("<a href='javascript:getUploadedFiles(\"")
                        .append(link).append("\")'>");
            }
            else
            {
                data.append(
                        "<IMG SRC='/globalsight/images/file.gif' height='15' width='13'>")
                        .append("&nbsp;&nbsp;");
            }
            data.append(fileName);
            if (tmp.isDirectory())
            {
                data.append("</a>");
            }
            data.append("</div>");
        }
        return data.toString();
    }

    /**
     * Creates a job
     */
    private int createJobs(Map<Object, Object> dataMap)
    {
        try
        {
            User user = (User) dataMap.get("user");
            String currentCompanyId = (String) dataMap.get("currentCompanyId");
            String jobName = (String) dataMap.get("jobName");
            String comment = (String) dataMap.get("comment");
            String tmpFolderName = (String) dataMap.get("tmpFolderName");
            String[] filePaths = (String[]) dataMap.get("jobFilePath");
            String[] l10nAndfileProfiles = (String[]) dataMap
                    .get("fileProfile");
            String[] targetLocales = (String[]) dataMap.get("targetLocale");
            String priority = (String) dataMap.get("priority");
            String baseFolder = (String) dataMap.get("baseFolder");
            if (StringUtils.isNotEmpty(baseFolder))
            {
                this.saveBaseFolder(user.getUserId(), SELECTED_FOLDER,
                        baseFolder);
            }
            String[] isSwitched = (String[]) dataMap.get("isSwitched");
            String attachmentName = (String) dataMap.get("attachment");
            String baseStorageFolder = (String) dataMap
                    .get("baseStorageFolder");
            String attribute = (String) dataMap.get("attributeString");

            BasicL10nProfile l10Profile = getBasicL10Profile(l10nAndfileProfiles);
            // init files and file profiles infomation
            List<File> sourceFilesList = new ArrayList<File>();
            List<String> descList = new ArrayList<String>();
            List<String> fpIdList = new ArrayList<String>();
            List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
            // init target locale infomation
            String locs = this.initTargetLocale(targetLocales);
            // for GBS-2137, initialize the job with "IN_QUEUE" state
            SessionManager sessionMgr = (SessionManager) dataMap
                    .get(SESSION_MANAGER);
            String uuid = sessionMgr.getAttribute("uuid") == null ? null
                    : (String) sessionMgr.getAttribute("uuid");
            sessionMgr.removeElement("uuid");

            Job job = JobCreationMonitor.initializeJob(jobName, uuid,
                    user.getUserId(), l10Profile.getId(), priority,
                    Job.IN_QUEUE);
            this.initDescAndFileProfile(descList, fpIdList, filePaths,
                    l10nAndfileProfiles, l10Profile, tmpFolderName, job,
                    isSwitched, sourceFilesList, fileProfileList);
            Map<String, long[]> filesToFpId = FileProfileUtil.excuteScriptOfFileProfile(
                    descList, fileProfileList, job);
            Set<String> fileNames = filesToFpId.keySet();
            Integer pageCount = new Integer(fileNames.size());
            String jobUuid = uuid == null ? ((JobImpl) job).getUuid() : uuid;
            List<JobAttribute> jobAttribtues = getJobAttributes(attribute,
                    l10Profile);
            // cache job attributes if there are any
            if (jobAttribtues != null && jobAttribtues.size() != 0)
            {
                RuntimeCache.addJobAtttibutesCache(jobUuid, jobAttribtues);
            }

            int count = 0;
            List<CxeMessage> cxeMsgs = new ArrayList<CxeMessage>();
            for (Iterator<String> i = fileNames.iterator(); i.hasNext();)
            {
                String fileName = i.next();
                long[] tmp = filesToFpId.get(fileName);
                String fileProfileId = String.valueOf(tmp[0]);
                int exitValue = (int) tmp[1];

                String key = jobName + fileName + ++count;
                CxeProxy.setTargetLocales(key, locs);
                // If use JMS
                if (FileImportUtil.USE_JMS)
                {
					CxeProxy.importFromFileSystem(fileName,
							String.valueOf(job.getId()), jobName,
							fileProfileId, pageCount, count, 1, 1,
							Boolean.TRUE, Boolean.FALSE,
							CxeProxy.IMPORT_TYPE_L10N, exitValue, priority);
                }
                // If not use JMS, we control the concurrent threads number
                else
                {
					CxeMessage cxeMessage = CxeProxy.formCxeMessageType(
							fileName, String.valueOf(job.getId()), jobName,
							fileProfileId, pageCount, count, 1, 1,
							Boolean.TRUE, Boolean.FALSE,
							CxeProxy.IMPORT_TYPE_L10N, exitValue, priority,
							String.valueOf(job.getCompanyId()));
					cxeMsgs.add(cxeMessage);
                }
            }

            // If not use JMS
            if (cxeMsgs.size() > 0)
            {
                ExecutorService pool = Executors.newFixedThreadPool(100);
                for (CxeMessage msg : cxeMsgs)
                {
                    FileImportRunnable runnable = new FileImportRunnable(msg);
                    Thread t = new MultiCompanySupportedThread(runnable);
                    pool.execute(t);
                }
                pool.shutdown();
            }

            // save job attributes if there are any
            if (jobAttribtues != null)
            {
                saveAttributes(jobAttribtues, currentCompanyId, job);
            }

            // save job comment
            if (!StringUtils.isEmpty(comment)
                    || !StringUtils.isEmpty(attachmentName))
            {
                String dir = convertFilePath(AmbFileStoragePathUtils
                        .getFileStorageDirPath(job.getCompanyId()))
                        + File.separator
                        + "GlobalSight"
                        + File.separator
                        + "CommentReference"
                        + File.separator
                        + "tmp"
                        + File.separator
                        + baseStorageFolder.split(",")[0];
                SaveCommentThread sct = new SaveCommentThread(jobName, comment,
                        attachmentName, user.getUserId(), dir);
                sct.start();
            }
            // Send email at the end.
            Project project = l10Profile.getProject();
            if(comment == null || comment.equals("null")){
            	comment = "";
            }
            sendUploadCompletedEmail(filePaths, fpIdList, jobName, comment,
                    new Date(), user, currentCompanyId, project);

            // after all steps, delete files that are used to create job
            for (File source : sourceFilesList)
            {
                FileUtil.deleteFile(source);
            }
            // delete the upload directory
            File uploads = new File(AmbFileStoragePathUtils.getCxeDocDir()
                    + File.separator + TMP_FOLDER_NAME + File.separator
                    + tmpFolderName);
            if (uploads != null && uploads.exists())
            {
                FileUtil.deleteFile(uploads);
            }
            return SUCCEED;
        }
        catch (FileNotFoundException ex)
        {
            logger.error("Cannot find the tmp uploaded files.", ex);
            return FAIL;
        }
        catch (Exception e)
        {
            logger.error("Create job failed.", e);
            return FAIL;
        }
    }

    /**
     * Save job attributes of the job
     * 
     * @param attributeString
     * @param job
     * @param currentCompanyId
     * @param l10Profile
     * @throws ParseException
     */
    private void saveAttributes(List<JobAttribute> jobAttributeList,
            String currentCompanyId, Job job)
    {
        AddJobAttributeThread thread = new AddJobAttributeThread(
                ((JobImpl) job).getUuid(), currentCompanyId);
        thread.setJobAttributes(jobAttributeList);
        thread.createJobAttributes();
    }

    private List<JobAttribute> getJobAttributes(String attributeString,
            BasicL10nProfile l10Profile)
    {
        List<JobAttribute> jobAttributeList = new ArrayList<JobAttribute>();

        if (l10Profile.getProject().getAttributeSet() == null)
        {
            return null;
        }

        if (StringUtils.isNotEmpty(attributeString))
        {
            String[] attributes = attributeString.split(";.;");
            for (String ele : attributes)
            {
                try
                {
                    String attributeId = ele.substring(ele.indexOf(",.,") + 3,
                            ele.lastIndexOf(",.,"));
                    String attributeValue = ele.substring(ele
                            .lastIndexOf(",.,") + 3);

                    Attribute attribute = HibernateUtil.get(Attribute.class,
                            Long.parseLong(attributeId));
                    JobAttribute jobAttribute = new JobAttribute();
                    jobAttribute.setAttribute(attribute.getCloneAttribute());
                    if (attribute != null
                            && StringUtils.isNotEmpty(attributeValue))
                    {
                        Condition condition = attribute.getCondition();
                        if (condition instanceof TextCondition)
                        {
                            jobAttribute.setStringValue(attributeValue);
                        }
                        else if (condition instanceof IntCondition)
                        {
                            jobAttribute.setIntegerValue(Integer
                                    .parseInt(attributeValue));
                        }
                        else if (condition instanceof FloatCondition)
                        {
                            jobAttribute.setFloatValue(Float
                                    .parseFloat(attributeValue));
                        }
                        else if (condition instanceof DateCondition)
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    DateCondition.FORMAT);
                            jobAttribute
                                    .setDateValue(sdf.parse(attributeValue));
                        }
                        else if (condition instanceof ListCondition)
                        {
                            String[] options = attributeValue.split("#@#");
                            List<String> optionValues = Arrays.asList(options);
                            jobAttribute.setValue(optionValues, false);
                        }
                    }
                    jobAttributeList.add(jobAttribute);
                }
                catch (Exception e)
                {
                    logger.error("Failed to get job attributes", e);
                }
            }
        }
        else
        {
            List<Attribute> attsList = l10Profile.getProject()
                    .getAttributeSet().getAttributeAsList();
            for (Attribute att : attsList)
            {
                JobAttribute jobAttribute = new JobAttribute();
                jobAttribute.setAttribute(att.getCloneAttribute());
                jobAttributeList.add(jobAttribute);
            }
        }

        return jobAttributeList;
    }

    /**
     * Init the checkbox of target locales. The pattern is fr_FR(French_France).
     * 
     * @param localeList
     * @param user
     * @param checked
     */
    private String initTargetLocaleSelect(List<?> localeList, User user)
    {
        StringBuffer sb = new StringBuffer();
        Locale locale = this.getUserLocale(user);
        for (int i = 0; i < localeList.size(); i++)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) localeList.get(i);
            sb.append("<div class='locale'>");
            sb.append("<input type='checkbox' name='targetLocale' value='"
                    + gsl.getId() + "' checked='true'>&nbsp;");
            sb.append(gsl.getLanguage() + "_" + gsl.getCountry() + " ("
                    + gsl.getDisplayLanguage(locale) + "_"
                    + gsl.getDisplayCountry(locale) + ")");
            sb.append("</div>");
        }
        return sb.toString();
    }

    /**
     * Save the selected folder path to database, so that next time the user
     * upload files, it will open the last selected folder path
     * 
     * @param operatorId
     * @param parameterName
     * @param folderPath
     */
    private void saveBaseFolder(String userId, String parameterName,
            String folderPath)
    {
        try
        {
            UserParameter up = ServerProxy.getUserParameterManager()
                    .getUserParameter(userId, parameterName);
            if (up == null)
            {
                up = new UserParameterImpl(userId, parameterName, folderPath);
                HibernateUtil.save(up);
            }
            else
            {
                up.setValue(folderPath);
                HibernateUtil.update(up);
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to save selected folder information.", e);
        }
    }

    /**
     * Get last selected folder, so that user won't have to choose again.
     * 
     * @param userId
     * @param name
     * @return folderPath
     */
    private String getLastSelectedFolder(String userId, String parameterName)
    {
        try
        {
            if (userId != null)
            {
                UserParameter up = ServerProxy.getUserParameterManager()
                        .getUserParameter(userId, parameterName);
                if (up != null)
                {
                    return up.getValue();
                }
            }
        }
        catch (Exception e)
        {
            logger.warn("Failed to get last selected folder information.", e);
            return "";
        }

        return "";
    }

    /**
     * Move tmp files to proper directory and save the paths and file profiles.
     * 
     * @param descList
     * @param fpIdList
     * @param filePaths
     * @param l10nAndfileProfiles
     * @param blp
     * @param tmpFolderName
     * @param jobName
     * @throws FileNotFoundException
     * @throws Exception
     */
    private void initDescAndFileProfile(List<String> descList,
            List<String> fpIdList, String[] filePaths,
            String[] l10nAndfileProfiles, BasicL10nProfile blp,
            String tmpFolderName, Job job, String[] isSwitched,
            List<File> sourceFilesList, List<FileProfile> fileProfileList)
            throws FileNotFoundException, Exception
    {
        for (int i = 0; i < filePaths.length; i++)
        {
            String filePath = convertFilePath(filePaths[i]);
            if(filePath.contains(tmpFolderName))
            {
            	filePath = filePath.substring(filePath.indexOf(tmpFolderName) + tmpFolderName.length());
            }
            String fileProfileId = l10nAndfileProfiles[i].split(",")[1];

            FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
                    .getFileProfileById(Long.parseLong(fileProfileId), true);
            if (filePath.toLowerCase().endsWith(".xlz"))
            {
                fileProfileId = String.valueOf(fp.getReferenceFP());
                fp = ServerProxy
                        .getFileProfilePersistenceManager()
                        .getFileProfileById(Long.parseLong(fileProfileId), true);
            }

            List<String> desc = copyTmpFilesToProperDirectory(blp, filePath,
                    tmpFolderName, job, fp, isSwitched[i], sourceFilesList);
            descList.addAll(desc);
            for (int j = 0; j < desc.size(); j++)
            {
                fpIdList.add(fileProfileId);
                fileProfileList.add(fp);
            }
        }
    }

    /**
     * According to the array input, return a string of locales. Locales are
     * seperated with commas.
     * 
     * @param targetLocales
     * @return
     * @throws LocaleManagerException
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws GeneralException
     */
    private String initTargetLocale(String[] targetLocales)
            throws LocaleManagerException, NumberFormatException,
            RemoteException, GeneralException
    {
        StringBuffer targetLocaleString = new StringBuffer();
        for (int i = 0; i < targetLocales.length; i++)
        {
            GlobalSightLocale gsl = ServerProxy.getLocaleManager()
                    .getLocaleById(Long.parseLong(targetLocales[i]));
            String loaleName = gsl.toString();
            if (targetLocaleString.length() != 0)
            {
                targetLocaleString.append(",");
            }
            targetLocaleString.append(loaleName);
        }
        return targetLocaleString.toString();
    }

    /**
     * Query the l10n profile information. The whole job share the same l10n
     * profile
     * 
     * @param l10nAndfileProfiles
     * @return
     */
    private BasicL10nProfile getBasicL10Profile(String[] l10nAndfileProfiles)
    {
        String profileValue = l10nAndfileProfiles[0];
        String[] l10nAndFp = profileValue.split(",");
        long l10Id = Long.parseLong(l10nAndFp[0]);
        BasicL10nProfile blp = HibernateUtil.get(BasicL10nProfile.class, l10Id);
        return blp;
    }

    /**
     * If the file is uploaded on page, copy it to current job folder and delete
     * it. If the file is uploaded in advance by new web DI, copy it to current
     * job folder and delete it. If the file is uploaded by upload page, don't
     * copy, just use it. Considering xlz files, the return value should be a
     * list.
     * 
     * @param basicL10nProfile
     * @param filePath
     * @param tmpFolderName
     * @param jobName
     * @param isSwitched
     * @return
     * @throws FileNotFoundException
     * @throws Exception
     */
    private List<String> copyTmpFilesToProperDirectory(
            BasicL10nProfile basicL10nProfile, String filePath,
            String tmpFolderName, Job job, FileProfile fp, String isSwitched,
            List<File> sourceFilesList) throws FileNotFoundException, Exception
    {
        try
        {
            List<String> ret = new ArrayList<String>();

            String sourceLocaleName = basicL10nProfile.getSourceLocale()
                    .getLocaleCode();

            File saveDir = AmbFileStoragePathUtils.getCxeDocDir();
            String currentLocation = "";
            String destinationLocation = "";
            if ("true".equals(isSwitched))
            {
                // the files are uploaded in advance.
                currentLocation = filePath;
                if (currentLocation.contains(TMP_FOLDER_NAME))
                {
                    // the file is under createjob_tmp folder
                    String baseTmpDir = saveDir + File.separator
                            + TMP_FOLDER_NAME;
                    filePath = filePath.substring(filePath.indexOf(baseTmpDir)
                            + baseTmpDir.length());
                    if (filePath.startsWith(File.separator))
                    {
                        filePath = filePath.substring(1);
                    }
                    tmpFolderName = filePath.substring(0,
                            filePath.indexOf(File.separator));
                    filePath = filePath.substring(filePath
                            .indexOf(File.separator) + 1);
                    destinationLocation = sourceLocaleName + File.separator
                            + job.getId() + File.separator + filePath;
                    // find old attachment path, and delete unused attachment
                    // files
                    String oldAttachmentPath = AmbFileStoragePathUtils
                            .getFileStorageDirPath()
                            + File.separator
                            + "GlobalSight"
                            + File.separator
                            + "CommentReference"
                            + File.separator
                            + "tmp"
                            + File.separator + tmpFolderName;
                    FileUtil.deleteFile(new File(oldAttachmentPath));
                }
                else
                {
                    // the file is under locale folder
                    filePath = filePath.substring(filePath.indexOf(saveDir
                            .getPath()) + saveDir.getPath().length());
                    if (filePath.startsWith(File.separator))
                    {
                        filePath = filePath.substring(1);
                    }
                    destinationLocation = filePath;
                }
            }
            else
            {
                // files are uploaded on page
                if (filePath.contains(":"))
                {
                    filePath = filePath.substring(filePath.indexOf(":") + 1);
                }
                currentLocation = saveDir + File.separator + TMP_FOLDER_NAME
                        + File.separator + tmpFolderName + File.separator
                        + filePath;
                destinationLocation = sourceLocaleName + File.separator
                        + job.getId() + filePath;
            }

            // if the source file is in tmp folder,
            // copy it to proper folder and delete it in the current folder.
            if (currentLocation.contains(TMP_FOLDER_NAME))
            {
                File sourceFile = new File(currentLocation);
                File descFile = new File(saveDir + File.separator
                        + destinationLocation);
                FileUtil.copyFile(sourceFile, descFile);

                sourceFilesList.add(sourceFile);
            }

            // FileProfile fp = ServerProxy.getFileProfilePersistenceManager()
            // .getFileProfileById(Long.parseLong(fileProfileId), true);

            if (fp.getKnownFormatTypeId() == XliffFileUtil.KNOWN_FILE_FORMAT_XLIFF
                    && ServerProxy.getFileProfilePersistenceManager()
                            .isXlzReferenceXlfFileProfile(fp.getName()))
            {
                String zipDir = destinationLocation.substring(0,
                        destinationLocation.lastIndexOf("."));
                ArrayList<String> zipFiles = ZipIt.unpackZipPackage(saveDir
                        + File.separator + destinationLocation, saveDir
                        + File.separator + zipDir);
                for (String file : zipFiles)
                {
                    String tmpFilename = zipDir + File.separator + file;
                    if (XliffFileUtil.isXliffFile(tmpFilename))
                    {
                        changeFileListByXliff(ret, tmpFilename, fp);
                    }
                }
                return ret;
            }
            else if (fp.getKnownFormatTypeId() == XliffFileUtil.KNOWN_FILE_FORMAT_XLIFF)
            {
                changeFileListByXliff(ret, destinationLocation, fp);
                return ret;
            }
            else
            {
                ret.add(destinationLocation);
                return ret;
            }
        }
        catch (FileNotFoundException ex)
        {
            throw ex;
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    private void changeFileListByXliff(List<String> p_fileList, String p_file,
            FileProfile p_fileProfile)
    {
        Hashtable<String, FileProfile> subFiles = new Hashtable<String, FileProfile>();
        String tmp = "";
        XliffFileUtil.processMultipleFileTags(subFiles, p_file, p_fileProfile);
        for (Iterator<String> iterator = subFiles.keySet().iterator(); iterator
                .hasNext();)
        {
            tmp = iterator.next();
            p_fileList.add(tmp);
        }
    }

    /**
     * Init the select box of each files. Files in a same job must map the
     * target locales in the same localization profiles. All the other file
     * profiles under other localizations must be disabled.
     * 
     * @param fileProfileList
     * @param writer
     * @throws IOException
     */
    private void initFileProfileSelect(List<?> fileProfileList,
            PrintWriter writer, long l10nId) throws IOException
    {
        if (fileProfileList.size() == 1)
        {
            FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(0);
            if (l10nId == 0)
            {
                l10nId = fp.getL10nProfileId();
            }
            if (fp.getL10nProfileId() == l10nId)
            {
                // if file profile has the same localization profile, select it.
                writer.write(initFileProfileOption(fp, SELECTED));
            }
            else
            {
                // if file profile has different l10n, it should be disabled.
                writer.write(initFileProfileOption(fp, DISABLED));
            }
        }
        else
        {
            int enableCount = countAvailableOptionCounts(fileProfileList,
                    l10nId);
            for (int i = 0; i < fileProfileList.size(); i++)
            {
                FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(i);
                if (l10nId == 0)
                {
                    // just add the options, don't select.
                    writer.write(initFileProfileOption(fp, null));
                }
                else if (fp.getL10nProfileId() == l10nId)
                {
                    if (enableCount == 1)
                    {
                        // select the only 1 proper file profile
                        writer.write(initFileProfileOption(fp, SELECTED));
                    }
                    else
                    {
                        // just add the options, don't select.
                        writer.write(initFileProfileOption(fp, null));
                    }
                }
                else
                {
                    // disable other file profiles under other localization
                    // profiles
                    writer.write(initFileProfileOption(fp, DISABLED));
                }
            }
        }
    }

    /**
     * Calculate how many file profiles are fit for the localization profile
     * 
     * @param fileProfileList
     * @param l10nId
     * @return
     */
    private int countAvailableOptionCounts(List<?> fileProfileList, long l10nId)
    {
        int enableCount = 0;
        for (int i = 0; i < fileProfileList.size(); i++)
        {
            FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(i);
            if (l10nId != 0 && fp.getL10nProfileId() == l10nId)
            {
                enableCount++;
            }
        }
        return enableCount;
    }

    /**
     * Init a single file profile option for the file profile select.
     * 
     * @param fp
     * @param flag
     * @return
     */
    private String initFileProfileOption(FileProfileImpl fp, String flag)
    {
        StringBuffer option = new StringBuffer();
        option.append("<option title=\"");
        if (StringUtils.isEmpty(fp.getDescription()))
        {
            option.append(fp.getName());
        }
        else
        {
            option.append(replaceSpecialCharacters(fp.getDescription()));
        }
        option.append("\" value=\"").append(fp.getL10nProfileId()).append(",")
                .append(fp.getId()).append("\"");
        if (flag != null)
        {
            option.append(" ").append(flag).append("=\"true\"");
        }
        option.append(">").append(fp.getName()).append("</option>");
        return option.toString();
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private void setLable(HttpServletRequest request, ResourceBundle bundle)
    {
        setLableToJsp(request, bundle, "lb_name");// name
        setLableToJsp(request, bundle, "lb_status");// status
        setLableToJsp(request, bundle, "lb_size");// size
        setLableToJsp(request, bundle, "lb_file_profile");// file profile
        setLableToJsp(request, bundle, "lb_target_locales");// target locales
        setLableToJsp(request, bundle, "lb_create_job");// create job
        setLableToJsp(request, bundle, "lb_create_job_without_java");// create job(zip only)
        setLableToJsp(request, bundle, "lb_add_files");// add files
        setLableToJsp(request, bundle, "lb_browse");// Browse
        setLableToJsp(request, bundle, "lb_cancel");// Cancel
        setLableToJsp(request, bundle, "jsmsg_customer_job_name");
        setLableToJsp(request, bundle, "jsmsg_invalid_job_name_1");
        setLableToJsp(request, bundle,
                "jsmsg_choose_file_profiles_for_all_files");
        setLableToJsp(request, bundle, "lb_import_select_target_locale");
        setLableToJsp(request, bundle, "jsmsg_customer_job_name");
        setLableToJsp(request, bundle, "jsmsg_customer_comment");
        setLableToJsp(request, bundle, "jsmsg_comment_must_be_less");
        setLableToJsp(request, bundle, "lb_total");// Total
        setLableToJsp(request, bundle, "lb_uploaded");
        setLableToJsp(request, bundle, "msg_failed");
        setLableToJsp(request, bundle, "msg_job_add_files");
        setLableToJsp(request, bundle, "helper_text_create_job");
        setLableToJsp(request, bundle, "helper_text_create_job_without_java");
        setLableToJsp(request, bundle, "msg_job_folder_confirm");
        setLableToJsp(request, bundle, "help_create_job");
        setLableToJsp(request, bundle, "msg_job_create_empty_file");
        setLableToJsp(request, bundle, "msg_job_create_exist");
        setLableToJsp(request, bundle, "msg_job_create_large_file");
        setLableToJsp(request, bundle, "highest");
        setLableToJsp(request, bundle, "major");
        setLableToJsp(request, bundle, "normal");
        setLableToJsp(request, bundle, "lower");
        setLableToJsp(request, bundle, "lowest");
        setLableToJsp(request, bundle, "lb_attachment");
        setLableToJsp(request, bundle, "lb_reference_file");
        setLableToJsp(request, bundle, "lb_uploaded_files");
        setLableToJsp(request, bundle, "lb_clear_profile");
        setLableToJsp(request, bundle, "msg_job_attachment_uploading");
        setLableToJsp(request, bundle, "lb_create_job_uploaded_files_tip");
        setLableToJsp(request, bundle, "lb_create_job_clean_map_tip");
        setLableToJsp(request, bundle, "lb_create_job_add_file_tip");
        setLableToJsp(request, bundle, "lb_create_job_browse_tip");
        setLableToJsp(request, bundle, "lb_job_creating");
        setLableToJsp(request, bundle, "lb_jobs_creating");
        setLableToJsp(request, bundle, "lb_job_attributes");
    }

    /**
     * Set languages on the page according to locales
     * 
     * @param request
     * @param bundle
     */
    private void setLableToJsp(HttpServletRequest request,
            ResourceBundle bundle, String msg)
    {
        String label = bundle.getString(msg);
        request.setAttribute(msg, label);
    }

    /**
     * Generate a random number
     * 
     * @return
     */
    private long getRandomNumber()
    {
        return (long) (Math.random() * 1000000000);
    }

    /**
     * Sends email to user after completing the process of uploading
     * 
     * @param fileNames
     * @param fpIds
     * @param jobName
     * @param jobComment
     * @param uploadDate
     * @param user
     * @param companyId
     */
    private void sendUploadCompletedEmail(String[] fileNames,
            List<String> fpIds, String jobName, String jobComment,
            Date uploadDate, User user, String companyId, Project project)
    {
        try
        {
            boolean systemNotificationEnabled = EventNotificationHelper
                    .systemNotificationEnabled();
            if (!systemNotificationEnabled)
            {
                return;
            }
            // an array contains all arguments
            String[] messageArguments = new String[7];

            Timestamp time = new Timestamp();
            time.setLocale(getUserLocale(user));
            time.setDate(uploadDate);
            messageArguments[0] = time.toString();
            messageArguments[1] = jobName;
            messageArguments[2] = jobComment;
            messageArguments[3] = "Project: " + project.getName();
            messageArguments[4] = user.getUserName() + "(" + user.getEmail()
                    + ")";
            messageArguments[5] = this.getFileProfileForEmail(fileNames, fpIds);
            messageArguments[6] = user.getSpecialNameForEmail();

            // send mail to uploader
			UserParameterPersistenceManagerLocal uppml = new UserParameterPersistenceManagerLocal();
			UserParameter up = uppml.getUserParameter(user.getUserId(),
					UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
			if (up != null && up.getIntValue() == 1) {
				ServerProxy.getMailer().sendMailFromAdmin(user,
						messageArguments,
						MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
						MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE,
						companyId);
			}
            // get the PM address
            User pm = UserHandlerHelper.getUser(project.getProjectManagerId());
            if (pm == null)
            {
                logger.error("Can not get project manager for DesktopIcon upload notification by project "
                        + project.getName());
                return;
            }
            else if (pm.getUserId().equals(user.getUserId()))
            {
                // the pm and operator are the same person,
                // don't need to email the second time.
                return;
            }
            messageArguments[6] = pm.getSpecialNameForEmail();

		    up = uppml.getUserParameter(pm.getUserId(),
					UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
			if (up != null && up.getIntValue() == 1) {
				ServerProxy.getMailer().sendMailFromAdmin(pm, messageArguments,
						MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
						MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE,
						companyId);
			}
        }
        catch (Exception e)
        {
            logger.error("Failed to send the file upload completion emails.", e);
        }
    }

    private String getFileProfileForEmail(String[] fileNames, List<String> fpIds)
            throws FileProfileEntityException, NumberFormatException,
            RemoteException, GeneralException, NamingException
    {
        StringBuffer sb = new StringBuffer();
        int filesLength = fileNames.length;
        if (filesLength > 1)
        {
            sb.append("\r\n");
        }
        for (int i = 0; i < filesLength; i++)
        {
            sb.append(fileNames[i])
                    .append("  (")
                    .append(ServerProxy.getFileProfilePersistenceManager()
                            .readFileProfile(Long.parseLong(fpIds.get(i)))
                            .getName()).append(")");
            if (i != filesLength - 1)
            {
                sb.append("\r\n");
            }
        }
        return sb.toString();
    }

    /**
     * Get default UI locale information for specified user
     * 
     * @param user
     *            User information
     * @return Locale Default UI locale for the specified user
     */
    private Locale getUserLocale(User user)
    {
        String dl = null;
        if (user != null)
        {
            dl = user.getDefaultUILocale();
        }
        if (dl == null)
            return new Locale("en", "US");
        else
        {
            try
            {
                String language = dl.substring(0, dl.indexOf("_"));
                String country = dl.substring(dl.indexOf("_") + 1);
                country = (country == null) ? "" : country;

                return new Locale(language, country);
            }
            catch (Exception e)
            {
                return new Locale("en", "US");
            }
        }
    }

    /**
     * Replace "\" and "/" to file separator
     * 
     * @param path
     * @return
     */
    private String convertFilePath(String path)
    {
        if (path != null)
        {
            return path.replace("\\", File.separator).replace("/",
                    File.separator);
        }
        else
        {
            return "";
        }
    }

    /**
     * Get the project that the file profile is associated with.
     * 
     * @param p_fp
     *            File profile information
     * @return Project Project information which is associated with specified
     *         file profile
     */
    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error(
                    "Failed to get the project that file profile "
                            + p_fp.toString() + " is associated with.", e);
        }
        return p;
    }

    private String replaceSpecialCharacters(String input)
    {
        return input.replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
    
    private List<File> uploadSelectedFile(HttpServletRequest request,
            String tempFolder, String type) throws Exception
    {
        File parentFile = null;
        List<String> fileNames = new ArrayList<String>();
        List<File> uploadedFiles = new ArrayList<File>();
        if (type.equals("0"))// source files
        {
            File saveDir = AmbFileStoragePathUtils.getCxeDocDir();
            String baseTmpDir = saveDir + File.separator + TMP_FOLDER_NAME;
            parentFile = new File(baseTmpDir + File.separator + tempFolder);
            parentFile.mkdirs();
        }
        else if (type.equals("1"))// comment file
        {
            File saveDir = AmbFileStoragePathUtils.getCommentReferenceDir();
            parentFile = new File(saveDir + File.separator + "tmp"
                    + File.separator + tempFolder);
            parentFile.mkdirs();
        }

        fileNames = uploadFile(request, parentFile);
		for (String fileName : fileNames)
        {
        	File uploadedFile = new File(fileName); 
        	uploadedFiles.add(uploadedFile);
        }
        return uploadedFiles;
    }

	private List<String> uploadFile(HttpServletRequest p_request,
			File parentFile) throws GlossaryException, IOException
	{
		byte[] inBuf = new byte[MAX_LINE_LENGTH];
		int bytesRead;
		ServletInputStream in;
		String contentType;
		String boundary;
		String filePath = "";
		String path = parentFile.getPath() + File.separator;
		List<String> filePaths = new ArrayList<String>();
		File file = new File(path);   
	    Set<String> uploadedFileNames = new HashSet<String>();
	    for (File f : file.listFiles()) {
	    	uploadedFileNames.add(f.getName());
	    }
		
		// Let's make sure that we have the right type of content
		//
		contentType = p_request.getContentType();
		if (contentType == null
				|| !contentType.toLowerCase().startsWith("multipart/form-data"))
		{
			String[] arg = { "form did not use ENCTYPE=multipart/form-data but `"
					+ contentType + "'" };
		
			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}
		
		// Extract the boundary string in this request. The
		// boundary string is part of the content type string
		//
		int bi = contentType.indexOf("boundary=");
		if (bi == -1)
		{
			String[] arg = { "no boundary string found in request" };

			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}
		else
		{
			// 9 := len("boundary=")
			boundary = contentType.substring(bi + 9);

			// The real boundary has additional two dashes in
			// front
			//
			boundary = "--" + boundary;
		}
		
		in = p_request.getInputStream();
		bytesRead = in.readLine(inBuf, 0, inBuf.length);
		
		if (bytesRead < 3)
		{
			String[] arg = { "incomplete request (not enough data)" };
		
			// Not enough content was send as part of the post
			throw new GlossaryException(
					GlossaryException.MSG_FAILED_TO_UPLOAD_FILE, arg, null);
		}
		
		while (bytesRead != -1)
		{
			String lineRead = new String(inBuf, 0, bytesRead, "utf-8");
			if (lineRead.startsWith("Content-Disposition: form-data; name=\""))
			{
				if (lineRead.indexOf("filename=\"") != -1)
				{		
					// Get file name
					String fileName = getFilename(lineRead.substring(0, lineRead.length() - 2));
					
					// Get content type line
					bytesRead = in.readLine(inBuf, 0, inBuf.length);
					lineRead = new String(inBuf, 0, bytesRead - 2, "utf-8");
		
					// Read and ignore the blank line
					bytesRead = in.readLine(inBuf, 0, inBuf.length);
		
					// Create a temporary file to store the
					// contents in it for now. We might not have
					// additional information, such as TUV id for
					// building the complete file path. We will
					// save the contents in this file for now and
					// finally rename it to correct file name.
					//
					
					// if a file with same name has been uploaded, ignore this
					if (uploadedFileNames.contains(fileName)) {
						continue;
					}

		        	filePath = path + fileName;
			        filePaths.add(filePath);
					File m_tempFile = new File(filePath);
					FileOutputStream fos = new FileOutputStream(m_tempFile);
					BufferedOutputStream bos = new BufferedOutputStream(fos,
							MAX_LINE_LENGTH * 4);

					// Read through the file contents and write
					// it out to a local temp file.
					boolean writeRN = false;
					while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
					{
						// Let's first check if we are already on
						// boundary line
						if (bytesRead > 2 && inBuf[0] == '-' && inBuf[1] == '-')
						{
							lineRead = new String(inBuf, 0, bytesRead, "utf-8");
							if (lineRead.startsWith(boundary))
							{
								break;
							}
						}
		
						// Write out carriage-return, new-line
						// pair which might have been left over
						// from last write.
						//
						if (writeRN)
						{
							bos.write(new byte[] { (byte) '\r', (byte) '\n' });
							writeRN = false;
						}
		
						// The ServletInputStream.readline() adds
						// "\r\n" bytes for the last line of the
						// file contents. If we find these pair
						// as the last bytes we need to delay
						// writing it until the next go, since it
						// could very well be the last line of
						// file content.
						//
						if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
								&& inBuf[bytesRead - 1] == '\n')
						{
							bos.write(inBuf, 0, bytesRead - 2);
							writeRN = true;
						}
						else
						{
							bos.write(inBuf, 0, bytesRead);
						}
					}
		
					bos.flush();
					bos.close();
					fos.close();
				}
				else
				{
					// This is the field part
		
					// First get the field name
		
//					int start = lineRead.indexOf("name=\"");
//					int end = lineRead.indexOf("\"", start + 7);
//					String fieldName = lineRead.substring(start + 6, end);
		
					// Read and ignore the blank line
					bytesRead = in.readLine(inBuf, 0, inBuf.length);
		
					// String Buffer to keep the field value
					//
					StringBuffer fieldValue = new StringBuffer();
		
					boolean writeRN = false;
					while ((bytesRead = in.readLine(inBuf, 0, inBuf.length)) != -1)
					{
						lineRead = new String(inBuf, 0, bytesRead, "utf-8");
		
						// Let's first check if we are already on
						// boundary line
						//
						if (bytesRead > 2 && inBuf[0] == '-' && inBuf[1] == '-')
						{
							if (lineRead.startsWith(boundary))
							{
								break;
							}
						}
		
						// Write out carriage-return, new-line
						// pair which might have been left over
						// from last write.
						//
						if (writeRN)
						{
							fieldValue.append("\r\n");
							writeRN = false;
						}
		
						// The ServletInputStream.readline() adds
						// "\r\n" bytes for the last line of the
						// field value. If we find these pair as
						// the last bytes we need to delay
						// writing it until the next go, since it
						// could very well be the last line of
						// field value.
						//
						if (bytesRead > 2 && inBuf[bytesRead - 2] == '\r'
								&& inBuf[bytesRead - 1] == '\n')
						{
							fieldValue.append(lineRead.substring(0, lineRead
									.length() - 2));
							writeRN = true;
						}
						else
						{
							fieldValue.append(lineRead);
						}
					}
				}
			}
		
			bytesRead = in.readLine(inBuf, 0, inBuf.length);
		}
		return filePaths;
	}

	private String getFilename(String p_filenameLine)
	{
		int start = 0;

		if (p_filenameLine != null
				&& (start = p_filenameLine.indexOf("filename=\"")) != -1)
		{
			String filepath = p_filenameLine.substring(start + 10,
					p_filenameLine.length() - 1);

			// Handle Windows v/s Unix file path
			if ((start = filepath.lastIndexOf('\\')) > -1)
			{
				return filepath.substring(start + 1);
			}
			else if ((start = filepath.lastIndexOf('/')) > -1)
			{
				return  filepath.substring(start + 1);
			}
			else
			{
				return  filepath;
			}
		}
		return null;
	}
	
	
    /**
     * Add a progress bar for each files within a zip file.
     * @param file
     * @throws Exception 
     */
    private String addZipFile(File file) throws Exception
    {
        String zipFileFullPath = file.getPath();
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(file.getName()));
        
        List<net.lingala.zip4j.model.FileHeader> entriesInZip = CreateJobUtil.getFilesInZipFile(file);
        
        StringBuffer ret = new StringBuffer("");
        for (net.lingala.zip4j.model.FileHeader entry : entriesInZip)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zipEntryName = entry.getFileName();
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zipFilePath
                    + file.getName().substring(0, file.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(file) + File.separator + zipEntryName;
            // if zip file contains subfolders, entry name will contains "/" or "\"
            if (zipEntryName.indexOf("/") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (zipEntryName.indexOf("\\") != -1)
            {
                zipEntryName = zipEntryName.substring(zipEntryName
                        .lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'")
                    .append(id)
                    .append("',zipName:'")
                    .append(file.getName().replace("'", "\\'"))
                    .append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(zipEntryName.replace("'", "\\'")).append("',size:'")
                    .append(entry.getUncompressedSize()).append("'}");
        }
        return ret.toString();
    }
    
    /**
     * Add a progress bar for each files within a rar file.
     * @param file
     */
    private String addRarFile(File file) throws Exception
    {
        String rarEntryName = null;
        String rarFileFullPath = file.getPath();
        String rarFilePath = rarFileFullPath.substring(0,
                rarFileFullPath.indexOf(file.getName()));
        
        List<FileHeader> entriesInRar = CreateJobUtil.getFilesInRarFile(file);
        
        StringBuffer ret = new StringBuffer("");
        for (FileHeader header : entriesInRar)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            if (header.isUnicode())
            {
                rarEntryName = header.getFileNameW();
            }
            else
            {
                rarEntryName = header.getFileNameString();
            }
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = rarFilePath
                    + file.getName().substring(0, file.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(file) + File.separator + rarEntryName;
            // if zip file contains subfolders, entry name will contains "/" or "\"
            if (rarEntryName.indexOf("/") != -1)
            {
                rarEntryName = rarEntryName.substring(rarEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (rarEntryName.indexOf("\\") != -1)
            {
                rarEntryName = rarEntryName.substring(rarEntryName
                        .lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'")
                    .append(id)
                    .append("',zipName:'")
                    .append(file.getName().replace("'", "\\'"))
                    .append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\").replace("'", "\\'"))
                    .append("',name:'").append(rarEntryName.replace("'", "\\'")).append("',size:'")
                    .append(header.getDataSize()).append("'}");
        }
        return ret.toString();
    }
    
    /**
     * Add a progress bar for each files within a 7z file.
     * @param file
     * @throws Exception 
     */
    private String addZip7zFile(File file) throws Exception
    {
        String zip7zFileFullPath = file.getPath();
        String zip7zFilePath = zip7zFileFullPath.substring(0,
                zip7zFileFullPath.indexOf(file.getName()));

        List<SevenZArchiveEntry> entriesInZip7z = CreateJobUtil
                .getFilesIn7zFile(file);

        StringBuffer ret = new StringBuffer("");
        for (SevenZArchiveEntry item : entriesInZip7z)
        {
            if (ret.length() > 0)
            {
                ret.append(",");
            }
            String zip7zEntryName = item.getName();
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zip7zFilePath
                    + file.getName().substring(0, file.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(file) + File.separator + zip7zEntryName;
            // if zip file contains subf,olders, entry name will contains "/" or
            // "\"
            if (zip7zEntryName.indexOf("/") != -1)
            {
                zip7zEntryName = zip7zEntryName.substring(zip7zEntryName
                        .lastIndexOf("/") + 1);
            }
            else if (zip7zEntryName.indexOf("\\") != -1)
            {
                zip7zEntryName = zip7zEntryName.substring(zip7zEntryName
                        .lastIndexOf("\\") + 1);
            }
            String id = CreateJobUtil.getFileId(unzippedFileFullPath);
            ret.append("{id:'")
                    .append(id)
                    .append("',zipName:'")
                    .append(file.getName().replace("'", "\\'"))
                    .append("',path:'")
                    .append(unzippedFileFullPath.replace("\\", File.separator)
                            .replace("/", File.separator).replace("\\", "\\\\")
                            .replace("'", "\\'")).append("',name:'")
                    .append(zip7zEntryName.replace("'", "\\'"))
                    .append("',size:'").append(item.getSize()).append("'}");
        }
        return ret.toString();
    }

    /**
     * Add a progress bar for a common file.
     * @param file
     */
    private String addCommonFile(File file)
    {
        String id = CreateJobUtil.getFileId(file.getPath());
        StringBuffer ret = new StringBuffer("");
        ret.append("{id:'").append(id).append("',zipName:'").append(file.getName().replace("'", "\\'"))
        		.append("',path:'").append(file.getPath().replace("\\", "\\\\").replace("'", "\\'"))
                .append("',name:'").append(file.getName().replace("'", "\\'")).append("',size:'")
                .append(file.length()).append("'}");
        return ret.toString();
    }
}
