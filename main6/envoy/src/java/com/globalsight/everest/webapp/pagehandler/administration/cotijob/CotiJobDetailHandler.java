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
package com.globalsight.everest.webapp.pagehandler.administration.cotijob;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParameterPersistenceManagerLocal;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.customAttribute.Attribute;
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
import com.globalsight.cxe.persistence.fileprofile.FileProfileEntityException;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.MultiCompanySupportedThread;
import com.globalsight.everest.coti.COTIDocument;
import com.globalsight.everest.coti.COTIPackage;
import com.globalsight.everest.coti.COTIProject;
import com.globalsight.everest.coti.util.COTIConstants;
import com.globalsight.everest.coti.util.COTIDbUtil;
import com.globalsight.everest.coti.util.COTIUtilEnvoy;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.localemgr.LocaleManager;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.permission.PermissionException;
import com.globalsight.everest.permission.PermissionSet;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandlerException;
import com.globalsight.everest.servlet.EnvoyServletException;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.servlet.util.SessionManager;
import com.globalsight.everest.webapp.WebAppConstants;
import com.globalsight.everest.webapp.pagehandler.PageHandler;
import com.globalsight.everest.webapp.pagehandler.administration.createJobs.SaveCommentThread;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.webnavigation.WebPageDescriptor;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.FileUtil;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.ProcessRunner;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.SortUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

/**
 * Handler class for COTI job details page
 * @author Wayzou
 *
 */
public class CotiJobDetailHandler extends PageHandler
{
    private static final Logger logger = Logger
            .getLogger(CotiJobDetailHandler.class);

    private static final String DOT = ".";
    private static final String DISABLED = "disabled";
    private static final String SELECTED = "selected";
    private Map<String, List<FileProfileImpl>> extensionToFileProfileMap;

    private static final int SUCCEED = 0;
    private static final int FAIL = 1;
    private static final String transDirName = COTIConstants.Dir_TranslationFiles_Name;

    public void invokePageHandler(WebPageDescriptor pageDescriptor,
            HttpServletRequest request, HttpServletResponse response,
            ServletContext context) throws ServletException, IOException,
            EnvoyServletException, PermissionException
    {
        // permission check
        HttpSession session = request.getSession(false);
        PermissionSet userPerms = (PermissionSet) session
                .getAttribute(WebAppConstants.PERMISSIONS);
        if (!userPerms.getPermissionFor(Permission.COTI_JOB))
        {
            logger.error("User doesn't have the permission to create COTI jobs via this page.");
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

        extensionToFileProfileMap = new HashMap<String, List<FileProfileImpl>>();
        ResourceBundle bundle = PageHandler.getBundle(session);
        String currentCompanyId = CompanyThreadLocal.getInstance().getValue();

        String projectId = request.getParameter(JOB_ID);
        if (projectId == null || "".equals(projectId))
        {
            projectId = request.getParameter("projectId");
        }

        long jid = -1;
        COTIProject cproject = null;
        COTIPackage cpackage = null;
        List<COTIDocument> documents = null;
        LocaleManager lm = null;
        GlobalSightLocale sourceLocale = null;
        GlobalSightLocale targetLocale = null;
        JobImpl job = null;

        try
        {
            jid = Long.parseLong(projectId);
            cproject = COTIDbUtil.getCOTIProject(jid);
            cpackage = COTIDbUtil.getCOTIPackage(cproject.getPackageId());
            documents = COTIDbUtil.getCOTIDocumentsByProjectId(jid);
            lm = COTIUtilEnvoy.getLocaleManager();
            sourceLocale = lm.getLocaleById(Long.parseLong(cproject
                    .getSourceLang()));
            targetLocale = lm.getLocaleById(Long.parseLong(cproject
                    .getTargetLang()));
            long jobId = cproject.getGlobalsightJobId();
            if (jobId > 0)
            {
                job = HibernateUtil.get(JobImpl.class, jobId);
            }
        }
        catch (Exception ex)
        {
            throw new ServletException("Cannot find COTI project for id: "
                    + projectId, ex);
        }

        request.setAttribute(JOB_ID, projectId);
        request.setAttribute("cotiProject", cproject);
        request.setAttribute("cotiPackage", cpackage);
        request.setAttribute("cotiDocuments", documents);
        request.setAttribute("sourceLocale", sourceLocale);
        request.setAttribute("targetLocale", targetLocale);
        request.setAttribute("gsjob", job);

        String action = request.getParameter("theAction");
        if (action != null)
        {
            if ("queryFileProfile".equals(action))
            {
                queryFileProfile(request, response, currentCompanyId, user,
                        sourceLocale, targetLocale);
                return;
            }
            else if ("createJob".equals(action))
            {
                final Map<String, Object> dataMap = prepareDataForJob(request,
                        currentCompanyId, user, cproject, cpackage, documents);
                request.setAttribute("create_result",
                        bundle.getString("msg_cotijob_create_successful"));
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

        super.invokePageHandler(pageDescriptor, request, response, context);
    }

    private long getRandomNumber()
    {
        return (long) (Math.random() * 1000000000);
    }

    private Map<String, Object> prepareDataForJob(HttpServletRequest request,
            String currentCompanyId, User user, COTIProject cproject,
            COTIPackage cpackage, List<COTIDocument> documents)
    {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("currentCompanyId", currentCompanyId);
        dataMap.put("user", user);
        SessionManager sessionMgr = (SessionManager) request.getSession()
                .getAttribute(SESSION_MANAGER);
        dataMap.put(SESSION_MANAGER, sessionMgr);

        dataMap.put("cotiPackage", cpackage);
        dataMap.put("cotiProject", cproject);
        dataMap.put("cotiDocuments", documents);
        
        String[] referenceFilePaths = request.getParameterValues("referenceFilePath");
        dataMap.put("referenceFilePath", referenceFilePaths);
        
        String jobName = EditUtil.removeCRLF(request.getParameter("jobName"))
                + "_" + getRandomNumber();
        dataMap.put("jobName", jobName);
        String comment = request.getParameter("comment");
        dataMap.put("comment", comment);
        // full path of files
        String[] filePaths = request.getParameterValues("jobFilePath");
        dataMap.put("jobFilePath", filePaths);
        // l10n info and file profile info
        String[] l10nAndfileProfiles = request
                .getParameterValues("fileProfile");
        dataMap.put("fileProfile", l10nAndfileProfiles);
        // mapped target locales
        GlobalSightLocale targetLocale = (GlobalSightLocale) request
                .getAttribute("targetLocale");
        dataMap.put("targetLocale", targetLocale);
        // priority
        String priority = request.getParameter("priority");
        dataMap.put("priority", priority);
        // attribute
        String attribute = request.getParameter("attributeString");
        dataMap.put("attributeString", attribute);

        return dataMap;
    }

    /**
     * Creates a job
     */
    private int createJobs(Map<String, Object> dataMap)
    {
        try
        {
            User user = (User) dataMap.get("user");
            String currentCompanyId = (String) dataMap.get("currentCompanyId");
            String jobName = (String) dataMap.get("jobName");
            String comment = (String) dataMap.get("comment");
            String[] filePaths = (String[]) dataMap.get("jobFilePath");
            String[] referenceFilePaths = (String[]) dataMap.get("referenceFilePath");
            String[] l10nAndfileProfiles = (String[]) dataMap
                    .get("fileProfile");
            GlobalSightLocale targetLocale = (GlobalSightLocale) dataMap
                    .get("targetLocale");
            String priority = (String) dataMap.get("priority");

            COTIProject cproject = (COTIProject) dataMap.get("cotiProject");
            COTIPackage cpackage = (COTIPackage) dataMap.get("cotiPackage");
            List<COTIDocument> documents = (List<COTIDocument>) dataMap
                    .get("cotiDocuments");
            String attribute = (String) dataMap.get("attributeString");

            BasicL10nProfile l10Profile = getBasicL10Profile(l10nAndfileProfiles);
            // init files and file profiles infomation
            List<File> sourceFilesList = new ArrayList<File>();
            List<String> descList = new ArrayList<String>();
            List<String> fpIdList = new ArrayList<String>();
            List<FileProfile> fileProfileList = new ArrayList<FileProfile>();
            // init target locale infomation
            String locs = targetLocale.toString();
            // for GBS-2137, initialize the job with "IN_QUEUE" state
            SessionManager sessionMgr = (SessionManager) dataMap
                    .get(SESSION_MANAGER);
            String uuid = sessionMgr.getAttribute("uuid") == null ? null
                    : (String) sessionMgr.getAttribute("uuid");
            Job job = JobCreationMonitor.initializeJob(jobName, uuid,
                    user.getUserId(), l10Profile.getId(), priority,
                    Job.IN_QUEUE, Job.JOB_TYPE_COTI);
            initDescAndFileProfile(descList, fpIdList, filePaths,
                    l10nAndfileProfiles, l10Profile, job, sourceFilesList,
                    fileProfileList);
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
            
            // save GlobalSight Job id for COTIProject
            cproject.setGlobalsightJobId(job.getId());
            COTIDbUtil.update(cproject);

            // save job attributes if there are any
            if (jobAttribtues != null)
            {
                saveAttributes(jobAttribtues, currentCompanyId, job);
            }

            // save job comment
            if (!StringUtils.isEmpty(comment))
            {
                String dir = null;
                SaveCommentThread sct = new SaveCommentThread(jobName, comment,
                        null, user.getUserId(), dir);
                sct.start();
            }
            
            if (referenceFilePaths != null && referenceFilePaths.length > 0)
            {
                String dir = convertFilePath(AmbFileStoragePathUtils
                        .getFileStorageDirPath())
                        + File.separator
                        + "GlobalSight"
                        + File.separator
                        + "CommentReference"
                        + File.separator
                        + "tmp"
                        + File.separator;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
                
                
                for (int i = 0; i < referenceFilePaths.length; i++)
                {
                    String fpath = referenceFilePaths[i];
                    String random = sdf.format(new Date()) + "-"
                            + Math.floor(Math.random() * 1000000000);
                    String temp = dir + random;
                    File src = new File(fpath);
                    String fname = src.getName();
                    File des = new File(temp, fname);
                    
                    FileUtil.copyFile(src, des);
                    
                    SaveCommentThread sct = new SaveCommentThread(jobName, "",
                            fname, user.getUserId(), temp);
                    sct.start();
                }
            }
            
            // Send email at the end.
            Project project = l10Profile.getProject();
            if (comment == null || comment.equals("null"))
            {
                comment = "";
            }
            sendUploadCompletedEmail(filePaths, fpIdList, jobName, comment,
                    new Date(), user, currentCompanyId, project);
            
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

    private BasicL10nProfile getBasicL10Profile(String[] l10nAndfileProfiles)
    {
        String profileValue = l10nAndfileProfiles[0];
        String[] l10nAndFp = profileValue.split(",");
        long l10Id = Long.parseLong(l10nAndFp[0]);
        BasicL10nProfile blp = HibernateUtil.get(BasicL10nProfile.class, l10Id);
        return blp;
    }

    private void initDescAndFileProfile(List<String> descList,
            List<String> fpIdList, String[] filePaths,
            String[] l10nAndfileProfiles, BasicL10nProfile blp, Job job,
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
                    transDirName, job, fp, sourceFilesList);
            descList.addAll(desc);
            for (int j = 0; j < desc.size(); j++)
            {
                fpIdList.add(fileProfileId);
                fileProfileList.add(fp);
            }
        }
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
            messageArguments[5] = getFileProfileForEmail(fileNames, fpIds);
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
            GeneralException, NamingException, RemoteException
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
     * If the file is uploaded on page, copy it to current job folder. If the
     * file is uploaded in advance by new web DI, copy it to current job folder.
     * If the file is uploaded by upload page, don't copy, just use it.
     * Considering xlz files, the return value should be a list.
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
            String tmpFolderName, Job job, FileProfile fp,
            List<File> sourceFilesList) throws FileNotFoundException, Exception
    {
        try
        {
            List<String> ret = new ArrayList<String>();

            String sourceLocaleName = basicL10nProfile.getSourceLocale()
                    .getLocaleCode();

            String fileName = filePath.substring(filePath
                    .lastIndexOf(transDirName) + transDirName.length());

            File saveDir = AmbFileStoragePathUtils.getCxeDocDir();
            String currentLocation = filePath;
            String destinationLocation = sourceLocaleName + File.separator
                    + job.getId() + File.separator + fileName;

            File sourceFile = new File(currentLocation);
            File descFile = new File(saveDir + File.separator
                    + destinationLocation);
            FileUtil.copyFile(sourceFile, descFile);

            sourceFilesList.add(sourceFile);

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
     * Called by ajax, to search the file profiles for files, and init all file
     * profile selects on the jsp.
     * 
     * @param request
     * @param response
     * @param currentCompanyId
     * @param user
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void queryFileProfile(HttpServletRequest request,
            HttpServletResponse response, String currentCompanyId, User user,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale)
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
                    initFileProfileSelect(fileProfileListOfUser, writer,
                            l10nId, sourceLocale, targetLocale);
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
     * Init the select box of each files. Files in a same job must map the
     * target locales in the same localization profiles. All the other file
     * profiles under other localizations must be disabled.
     * 
     * @param fileProfileList
     * @param writer
     * @throws IOException
     * @throws NamingException
     * @throws GeneralException
     * @throws ProjectHandlerException
     */
    private void initFileProfileSelect(List<?> fileProfileList,
            PrintWriter writer, long l10nId, GlobalSightLocale sourceLocale,
            GlobalSightLocale targetLocale) throws IOException,
            ProjectHandlerException, GeneralException, NamingException
    {
        if (fileProfileList.size() == 1)
        {
            FileProfileImpl fp = (FileProfileImpl) fileProfileList.get(0);
            if (l10nId == 0)
            {
                l10nId = fp.getL10nProfileId();
            }

            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(
                    Long.valueOf(l10nId));

            if (fp.getL10nProfileId() == l10nId
                    && isRightL10nProfile(lp, sourceLocale, targetLocale))
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

                L10nProfile lp = ServerProxy.getProjectHandler()
                        .getL10nProfile(fp.getL10nProfileId());
                if (!isRightL10nProfile(lp, sourceLocale, targetLocale))
                {
                    continue;
                }

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

    private boolean isRightL10nProfile(L10nProfile lp,
            GlobalSightLocale sourceLocale, GlobalSightLocale targetLocale)
    {
        if (lp.getSourceLocale().equals(sourceLocale))
        {
            GlobalSightLocale[] targetLocales = lp.getTargetLocales();
            for (GlobalSightLocale tl : targetLocales)
            {
                if (tl.equals(targetLocale))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private String replaceSpecialCharacters(String input)
    {
        return input.replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;");
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
}
