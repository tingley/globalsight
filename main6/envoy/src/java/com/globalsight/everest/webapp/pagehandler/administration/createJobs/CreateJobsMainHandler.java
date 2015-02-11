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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParameterImpl;
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
import com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
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
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

public class CreateJobsMainHandler extends PageHandler
{
    public static final String TMP_FOLDER_NAME = "createJob_tmp";

    private static final Logger logger = Logger
            .getLogger(CreateJobsMainHandler.class);
    private static final String DOT = ".";
    private static final String DISABLED = "disabled";
    private static final String SELECTED = "selected";
    private static final String SELECTED_FOLDER = "selected_folder_path_in_create_job";
    private static final int SUCCEED = 0;
    private static final int FAIL = 1;
    private Map<String, List<FileProfileImpl>> extensionToFileProfileMap;
    private Map<String, String> l10NToTargetLocalesMap;
    private Map<String, String> l10NToJobAttributeMap;

    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        // permission check
        HttpSession session = request.getSession(false);
        SessionManager sessionMgr = (SessionManager) session
                .getAttribute(SESSION_MANAGER);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.CREATE_JOB))
        {
            logger.error("User doesn't have the permission to create jobs via this page.");
            response.sendRedirect("/globalsight/ControlServlet?");
            return;
        }
        // get the operator
        User user = (User) sessionMgr.getAttribute(WebAppConstants.USER);
        if (user != null)
        {
            sessionMgr.setAttribute(WebAppConstants.USER, user);
        }
        else
        {
            logger.warn("User object is null while it should not.");
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
            else if (action.equals("createJob"))
            {
                int result = this.createJobs(request, currentCompanyId, user);
                if (result == SUCCEED)
                {
                    request.setAttribute("create_result",
                            bundle.getString("msg_job_create_successful"));
                }
                else if (result == FAIL)
                {
                    request.setAttribute("create_result",
                            bundle.getString("msg_job_create_failed"));
                }
            }
        }
        this.setPageParameter(request, bundle, user, session, currentCompanyId);
        super.invokePageHandler(pageDescriptor, request, response, context);
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
            if (l10NToJobAttributeMap.get(l10Nid) == null)
            {
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
                l10NToJobAttributeMap.put(l10Nid, hasAttribute);
            }
            else
            {
                hasAttribute = l10NToJobAttributeMap.get(l10Nid);
            }
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
        request.setAttribute(
                "lastSelectedFolder",
                convertFilePath(
                        getLastSelectedFolder(user.getUserId(), SELECTED_FOLDER))
                        .replace("\\", "\\\\"));
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
            l10NToTargetLocalesMap = new HashMap<String, String>();
            l10NToJobAttributeMap = new HashMap<String, String>();
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
                    Collections.sort(fileProfileListOfCompany,
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
     * Create a job
     * 
     * @param request
     */
    private int createJobs(HttpServletRequest request, String currentCompanyId,
            User user)
    {
        try
        {
            String jobName = EditUtil.utf8ToUnicode(
                    request.getParameter("jobName")).trim()
                    + "_" + getRandomNumber();
            String comment = request.getParameter("comment");
            // This is where the files are uploaded
            String tmpFolderName = request.getParameter("tmpFolderName");
            // full path of files
            String[] filePaths = request.getParameterValues("jobFilePath");
            // l10n info and file profile info
            String[] l10nAndfileProfiles = request
                    .getParameterValues("fileProfile");
            // mapped target locales
            String[] targetLocales = request.getParameterValues("targetLocale");
            // priority
            String priority = request.getParameter("priority");
            // the selected folder path
            String baseFolder = request.getParameter("baseFolder");
            if (StringUtils.isNotEmpty(baseFolder))
            {
                this.saveBaseFolder(user.getUserId(), SELECTED_FOLDER,
                        baseFolder);
            }
            // mark whether the file comes from server
            String[] isSwitched = request.getParameterValues("isSwitched");
            // name of attachment
            String attachmentName = request.getParameter("attachment");
            // baseStorageFolder = current random folder + "," + companyId
            String baseStorageFolder = request
                    .getParameter("baseStorageFolder");
            String attribute = request.getParameter("attributeString");

            BasicL10nProfile l10Profile = getBasicL10Profile(l10nAndfileProfiles);
            // init files and file profiles infomation
            List<File> sourceFilesList = new ArrayList<File>();
            List<String> descList = new ArrayList<String>();
            List<String> fpIdList = new ArrayList<String>();
            List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
            this.initDescAndFileProfile(descList, fpIdList, filePaths,
                    l10nAndfileProfiles, l10Profile, tmpFolderName, jobName,
                    isSwitched, sourceFilesList, fileProfileList);
            // init target locale infomation
            String locs = this.initTargetLocale(targetLocales);
            // create job
            // for GBS-2137, initialize the job with "IN_QUEUE" state
            SessionManager sessionMgr = (SessionManager) request.getSession()
                    .getAttribute(SESSION_MANAGER);
            String uuid = sessionMgr.getAttribute("uuid") == null ? null
                    : (String) sessionMgr.getAttribute("uuid");
            Job job = JobCreationMonitor.initializeJob(jobName, uuid,
                    user.getUserId(), l10Profile.getId(), priority,
                    Job.IN_QUEUE);
            Map<String, long[]> filesToFpId = excuteScriptOfFileProfile(
                    descList, fpIdList, fileProfileList);
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
            for (Iterator<String> i = fileNames.iterator(); i.hasNext();)
            {
                String fileName = i.next();
                long[] tmp = filesToFpId.get(fileName);
                String fileProfileId = String.valueOf(tmp[0]);
                int exitValue = (int) tmp[1];

                String key = jobName + fileName + ++count;
                CxeProxy.setTargetLocales(key, locs);
                CxeProxy.importFromFileSystem(fileName,
                        String.valueOf(job.getId()), jobName, fileProfileId,
                        pageCount, count, 1, 1, Boolean.TRUE, Boolean.FALSE,
                        CxeProxy.IMPORT_TYPE_L10N, exitValue, priority);
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
                        .getFileStorageDirPath())
                        + File.separator
                        + "GlobalSight"
                        + File.separator
                        + "CommentReference"
                        + File.separator
                        + "tmp"
                        + File.separator
                        + baseStorageFolder.split(",")[0];
                SaveCommentThread sct = new SaveCommentThread(jobName, comment,
                        attachmentName, user.getUserName(), dir);
                sct.start();
            }
            // Send email at the end.
            Project project = l10Profile.getProject();
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
     * If the fileprofile has a script, excute the script and create job
     * 
     * @param descList
     * @param fpIdList
     * @param fileProfileList
     * @return
     */
    private Map<String, long[]> excuteScriptOfFileProfile(
            List<String> descList, List<String> fpIdList,
            List<FileProfile> fileProfileList)
    {
        Map<String, long[]> filesToFpId = new HashMap<String, long[]>();

        for (int i = 0; i < descList.size(); i++)
        {
            String fileName = descList.get(i);
            FileProfile fp = fileProfileList.get(i);

            String scriptOnImport = fp.getScriptOnImport();
            long exitValue = 0;
            if (StringUtils.isNotEmpty(scriptOnImport))
            {
                String scriptedDir = fileName.substring(0,
                        fileName.lastIndexOf("."));
                String scriptedFolderPath = AmbFileStoragePathUtils
                        .getCxeDocDirPath(fp.getCompanyId())
                        + File.separator
                        + scriptedDir;
                File scriptedFolder = new File(scriptedFolderPath);
                if (!scriptedFolder.exists())
                {
                    File file = new File(fileName);
                    String filePath = AmbFileStoragePathUtils
                            .getCxeDocDirPath()
                            + File.separator
                            + file.getParent();
                    // Call the script on import to convert the file
                    try
                    {
                        String cmd = "cmd.exe /c " + scriptOnImport + " \""
                                + filePath + "\"";
                        // If the script is Lexmark tool, another parameter
                        // -encoding is passed.
                        if ("lexmarktool.bat".equalsIgnoreCase(new File(
                                scriptOnImport).getName()))
                        {
                            cmd += " \"-encoding " + fp.getCodeSet() + "\"";
                        }
                        Process process = Runtime.getRuntime().exec(cmd);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));
                        while (reader.readLine() != null)
                        {
                        }
                        BufferedReader error_reader = new BufferedReader(
                                new InputStreamReader(process.getErrorStream()));
                        while (error_reader.readLine() != null)
                        {
                        }
                        logger.info("Script on Import " + scriptOnImport
                                + " was called.");
                        exitValue = process.exitValue();
                    }
                    catch (Exception e)
                    {
                        exitValue = 1;
                        logger.error("The script on import was not executed successfully.");
                    }
                }

                // Iterator the files converted by the script and import
                // each one of them.
                if (scriptedFolder.exists())
                {
                    String scriptedFiles[] = scriptedFolder.list();
                    if (scriptedFiles != null && scriptedFiles.length > 0)
                    {
                        for (int j = 0; j < scriptedFiles.length; j++)
                        {
                            String scriptedFileName = scriptedFiles[j];
                            long fileProfileId = fp.getId();
                            String key_fileName = scriptedDir + File.separator
                                    + scriptedFileName;
                            filesToFpId.put(key_fileName, new long[]
                            { fileProfileId, exitValue });
                        }
                    }
                    else
                    // there are no scripted files in the folder
                    {
                        filesToFpId.put(fileName, new long[]
                        { fp.getId(), exitValue });
                    }
                }
                else
                // the corresponding folder was not created by the script.
                {
                    filesToFpId.put(fileName, new long[]
                    { fp.getId(), exitValue });
                }
            }
            else
            {
                filesToFpId.put(fileName, new long[]
                { fp.getId(), exitValue });
            }
        }

        return filesToFpId;
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
            UserParameter up = ServerProxy.getUserParameterManager()
                    .getUserParameter(userId, parameterName);
            if (up != null)
            {
                return up.getValue();
            }
            else
            {
                return "";
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to get last selected folder information.", e);
            return "";
        }
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
            String tmpFolderName, String jobName, String[] isSwitched,
            List<File> sourceFilesList, List<FileProfile> fileProfileList)
            throws FileNotFoundException, Exception
    {
        for (int i = 0; i < filePaths.length; i++)
        {
            String filePath = convertFilePath(filePaths[i]);
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
                    tmpFolderName, jobName, fp, isSwitched[i], sourceFilesList);
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
            String tmpFolderName, String jobName, FileProfile fp,
            String isSwitched, List<File> sourceFilesList)
            throws FileNotFoundException, Exception
    {
        try
        {
            List<String> ret = new ArrayList<String>();

            String sourceLocaleName = basicL10nProfile.getSourceLocale()
                    .getLocaleCode();

            File saveDir = AmbFileStoragePathUtils.getCxeDocDir();
            String currentLocation = "";
            String destinationLocation = "";
            if (isSwitched != null && isSwitched.equals("true"))
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
                            + jobName + File.separator + filePath;
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
                        + jobName + filePath;
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
            ServerProxy.getMailer().sendMailFromAdmin(user, messageArguments,
                    MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
                    MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE,
                    companyId);

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

            ServerProxy.getMailer().sendMailFromAdmin(pm, messageArguments,
                    MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
                    MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE,
                    companyId);
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
}
