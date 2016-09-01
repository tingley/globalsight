/**
 * Copyright 2016 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.restful.version1_0.job;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import com.globalsight.config.UserParamNames;
import com.globalsight.config.UserParameter;
import com.globalsight.config.UserParameterPersistenceManagerLocal;
import com.globalsight.cxe.adaptermdb.filesystem.FileSystemUtil;
import com.globalsight.cxe.entity.customAttribute.Attribute;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.cxe.util.CxeProxy;
import com.globalsight.diplomat.util.database.ConnectionPool;
import com.globalsight.diplomat.util.database.ConnectionPoolException;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.company.CompanyWrapper;
import com.globalsight.everest.foundation.BasicL10nProfile;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.Timestamp;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.jobhandler.jobcreation.JobCreationMonitor;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.permission.Permission;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.ProjectHandler;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.tm.importer.ImportUtil;
import com.globalsight.everest.webapp.applet.createjob.CreateJobUtil;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.EventNotificationHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.everest.workflowmanager.WorkflowExportingHelper;
import com.globalsight.ling.common.URLEncoder;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.restful.RestResource;
import com.globalsight.restful.RestWebServiceException;
import com.globalsight.restful.RestWebServiceLog;
import com.globalsight.restful.RestWebServiceUtil;
import com.globalsight.util.AmbFileStoragePathUtils;
import com.globalsight.util.Assert;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.RuntimeCache;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.file.XliffFileUtil;
import com.globalsight.util.mail.MailerConstants;
import com.globalsight.util.zip.ZipIt;
import com.globalsight.webservices.AmbassadorUtil;
import com.globalsight.webservices.attribute.AddJobAttributeThread;
import com.globalsight.webservices.attribute.AttributeUtil;
import com.globalsight.webservices.attribute.Attributes;
import com.globalsight.webservices.attribute.JobAttributeVo;
import com.globalsight.webservices.vo.JobFiles;

@Path("/1.0/companies/{companyID}/jobs")
public class JobResource extends RestResource
{
    private static final Logger logger = Logger.getLogger(JobResource.class);

    public static final String GET_UNIQUE_JOB_NAME = "getUniqueJobName";
    public static final String UPLOAD_SOURCE_FILE = "uploadSourceFile";
    public static final String UPLOAD_ZIP_SOURCE_FILE = "uploadZipSourceFile";
    public static final String CREATE_JOB = "createJob";
    public static final String GET_JOB_STATUS = "getJobStatus";
    public static final String GET_JOB_EXPORT_FILES = "getJobExportFiles";

    public static String ERROR_JOB_NAME = "You cannot have \\, /, :, ;, *, ?, |, \", &lt;, &gt;, % or &amp; in the Job Name.";

    private static Set<Long> cachedJobIds = Collections.synchronizedSet(new HashSet<Long>());

    /**
     * Get an unique job name.
     * 
     * @param p_companyID
     *                  Company ID. Required.
     * @param p_jobName 
     *                  Job name. Required.
     * @return Return an unique job name in current system.
     * 
     */
    @GET
    @Path("/getUniqueJobName")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getUniqueJobName(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @QueryParam("jobName") String p_jobName) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));
            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobName", p_jobName);
            restStart = RestWebServiceLog.start(JobResource.class, GET_UNIQUE_JOB_NAME, restArgs);
            String jobNameValidation = validateJobName(p_jobName);
            if (jobNameValidation != null)
            {
                throw new RestWebServiceException(jobNameValidation);
            }

            p_jobName = getJobName(p_jobName);
            return Response.status(200).entity(p_jobName).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_UNIQUE_JOB_NAME, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    /**
     * Upload source file to server side.
     * 
     * @param p_companyID
     *            Company ID. Required.
     * @param p_jobName
     *            Job name. Required.
     * @param p_fileProfileId
     *            File profile ID for current file. Required.
     * @param p_input
     *            Uploading source file content. Required.
     *
     * @return A successful message
     * 
     * @throws RestWebServiceException
     * 
     */
    @POST
    @Path("/sourceFiles")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadSourceFile(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @QueryParam("jobName") String p_jobName,
            @QueryParam("fileProfileId") String p_fileProfileId,
            MultipartInput p_input) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        boolean updateJobStateIfException = true;
        Job job = null;
        String jobId = null;
        User user;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobName", p_jobName);
            restArgs.put("fileProfileId", p_fileProfileId);
            restStart = RestWebServiceLog.start(JobResource.class, UPLOAD_SOURCE_FILE, restArgs);
            checkPermission(userName, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);
            p_jobName = EditUtil.removeCRLF(p_jobName);
            String jobNameValidation = validateJobName(p_jobName);
            if (jobNameValidation != null)
            {
                throw new RestWebServiceException(jobNameValidation);
            }
            if (StringUtil.isEmpty(p_fileProfileId))
            {
                throw new RestWebServiceException("Empty file profile id.");
            }
            FileProfile fp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(
                    Long.parseLong(p_fileProfileId));
            long l10nProfileId = fp.getL10nProfileId();
            BasicL10nProfile blp = HibernateUtil.get(BasicL10nProfile.class, l10nProfileId);
            String priority = String.valueOf(blp.getPriority());
            user = ServerProxy.getUserManager().getUserByName(userName);
            job = ServerProxy.getJobHandler().getJobByJobName(p_jobName);
            if (job != null)
                jobId = String.valueOf(job.getId());
            else
            {
                job = JobCreationMonitor.initializeJob(p_jobName, user.getUserId(),
                        fp.getL10nProfileId(), priority, Job.UPLOADING);
                jobId = String.valueOf(job.getId());
            }

            String msg = checkIfCreateJobCalled(UPLOAD_SOURCE_FILE, Long.parseLong(jobId),
                    p_jobName);
            if (msg != null)
            {
                updateJobStateIfException = false;
                throw new RestWebServiceException(msg);
            }

            if (!isInSameCompany(userName, String.valueOf(fp.getCompanyId()))
                    && !UserUtil.isSuperPM(user.getUserId()))
            {
                String message = "Current user cannot upload file with the file profile which is in other company.";
                throw new RestWebServiceException(message);
            }

            String srcLocale = findSrcLocale(p_fileProfileId);
            StringBuffer newPath = new StringBuffer();
            newPath.append(AmbFileStoragePathUtils.getCxeDocDir(job.getCompanyId()));
            newPath.append(File.separator).append(srcLocale);
            newPath.append(File.separator).append("webservice");
            newPath.append(File.separator).append(jobId);

            List<InputPart> inputParts = p_input.getParts();
            for (InputPart inputPart : inputParts)
            {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String contentDisposition = header.getFirst("Content-Disposition");
                String fileName = getFileNameFromHeaderInfo(contentDisposition);
                if (fileName != null)
                {
                    // convert the uploaded file content to InputStream
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    byte[] bytes = IOUtils.toByteArray(inputStream);

                    File sourceFile = new File(newPath.toString(), fileName);
                    RestWebServiceUtil.writeFile(sourceFile, bytes);
                }
            }
        }
        catch (Exception e)
        {
            if (jobId != null && updateJobStateIfException)
            {
                JobCreationMonitor.updateJobState(Long.parseLong(jobId), Job.IMPORTFAILED);
            }
            logger.error(e);
            throw new RestWebServiceException(makeErrorJson(UPLOAD_SOURCE_FILE, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
        return Response.status(200).entity("File is uploaded successfully for job: " + jobId)
                .build();
    }
    
    /**
     * Create a job
     * 
     * @param p_companyID 
     *                  Company ID. Required.
     * @param p_jobId
     *                  Job id. Required.
     * @param p_filePaths
     *                  String Path of files which are contained in job, split by "|". Required.
     * @param p_fileProfileIds
     *                  String ID of file profiles, split by "|". Required.
     * @param p_targetLocales
     *                  String Target locales which like to be translated. Optional.
     * @param p_comment
     *                  String Job comment. Optional.
     * @param p_attributes
     *                  String Attributes used to create job. Optional. Example: 
     *            <attributes>
     *                      <attributes xsi:type="fileJobAttributeVo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *                          <displayName>file_01</displayName>
     *                          <fromSuperCompany>false</fromSuperCompany>
     *                          <internalName>file_01</internalName>
     *                          <required>false</required> 
     *                          <type>file</type> 
     *                      </attributes>
     *                      <attributes xsi:type="textJobAttributeVo" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *                          <displayName>file_02</displayName>
     *                          <fromSuperCompany>false</fromSuperCompany>
     *                          <internalName>file_02</internalName>
     *                          <required>false</required> 
     *                          <type>text</type> 
     *                      </attributes>
     *            </attributes>
     * @return A "Create job success." message.
     * 
     */
    @POST
    @Path("/createJob")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createJob(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @QueryParam("jobId") String p_jobId,
            @QueryParam("filePaths") String p_filePaths,
            @QueryParam("fileProfileIds") String p_fileProfileIds,
            @QueryParam("targetLocales") String p_targetLocales,
            @QueryParam("comment") String p_comment,
            @QueryParam("attributes") String p_attributes) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        boolean reCreate = false;
        Job job = null;
        try
        {
            Vector<String> fileProfileIds = new Vector<String>();
            Vector<String> filePaths = new Vector<String>();
            Vector<String> targetLocales = new Vector<String>();
            FileProfilePersistenceManager fppm = null;
            String fpId = "";
            long iFpId = 0l;
            String filename = "", realFilename = "", tmpFilename = "";
            String zipDir = "";
            String vTargetLocale = "";
            FileProfile fp = null, referenceFP = null;
            File file = null;
            Vector fileProfiles = new Vector();
            Vector files = new Vector();
            Vector afterTargetLocales = new Vector();
            ArrayList<String> zipFiles = null;
            long referenceFPId = 0l;
            boolean isWSFlag = true;
            String userName = getUserNameFromSession(accessToken.get(0));
            User user = ServerProxy.getUserManager().getUserByName(userName);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobId", p_jobId);
            restArgs.put("comment", p_comment);
            restArgs.put("filePaths", p_filePaths);
            restArgs.put("fileProfileIds", p_fileProfileIds);
            restArgs.put("targetLocales", p_targetLocales);
            restArgs.put("attributes", p_attributes);
            restStart = RestWebServiceLog.start(JobResource.class, CREATE_JOB, restArgs);

            checkPermission(userName, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);
            Assert.assertIsInteger(p_jobId);
            if (StringUtil.isNotEmpty(p_jobId))
                job = JobCreationMonitor.loadJobFromDB(Long.parseLong(p_jobId));
            if (job == null)
            {
                String msg = "current jobId : " + p_jobId + " does not exist.";
                throw new RestWebServiceException(msg);
            }
            
            String msg = checkIfCreateJobCalled(CREATE_JOB, job.getId(), job.getJobName());
            if (msg != null)
            {
                reCreate = true;
                throw new RestWebServiceException(msg);
            }
            cachedJobIds.add(job.getId());

            validateParameters(job, p_filePaths, p_fileProfileIds, p_targetLocales, fileProfileIds,
                    filePaths, targetLocales, false);

            fppm = ServerProxy.getFileProfilePersistenceManager();
            for (int i = 0; i < filePaths.size(); i++)
            {
                vTargetLocale = (String) targetLocales.get(i);

                fpId = (String) fileProfileIds.get(i);
                iFpId = Long.parseLong(fpId);
                fp = fppm.readFileProfile(iFpId);
                referenceFPId = fp.getReferenceFP();
                referenceFP = fppm.readFileProfile(referenceFPId);

                filename = (String) filePaths.get(i);
                filename = filename.replace('\\', File.separatorChar);
                String srcLocale = findSrcLocale(fpId);
                filename = getRealPath(p_jobId, filename, srcLocale, isWSFlag);
                realFilename = AmbFileStoragePathUtils.getCxeDocDir(job.getCompanyId())
                        + File.separator + filename;
                file = new File(realFilename);
                if (file.getAbsolutePath().endsWith(".xml"))
                {
                    saveFileAsUTF8(file);
                }
                // indicates this is an "XLZ" format file profile
                if (48 == fp.getKnownFormatTypeId())
                {
                    zipDir = realFilename.substring(0, realFilename.lastIndexOf("."));
                    zipFiles = ZipIt.unpackZipPackage(realFilename, zipDir);
                    String relativePath = filename.substring(0, filename.lastIndexOf("."));
                    String tmp = "";
                    for (String f : zipFiles)
                    {
                        if (XliffFileUtil.isXliffFile(f))
                        {
                            tmp = relativePath + File.separator + f;
                            changeFileListByXliff(tmp, vTargetLocale, referenceFP, fileProfiles,
                                    files, afterTargetLocales);
                        }
                    }
                }
                else if (39 == fp.getKnownFormatTypeId())
                {
                    changeFileListByXliff(filename, vTargetLocale, fp, fileProfiles, files,
                            afterTargetLocales);
                }
                else
                {
                    fileProfiles.add(fp);
                    files.add(file);
                    afterTargetLocales.add(vTargetLocale);
                }
            }

            Vector result = FileSystemUtil.execScript(files, fileProfiles, afterTargetLocales,
                    Long.parseLong(p_jobId), job.getJobName());
            Vector sFiles = (Vector) result.get(0);
            Vector sProFiles = (Vector) result.get(1);
            Vector stargetLocales = (Vector) result.get(2);
            Vector exitValues = (Vector) result.get(3);
            // cache job attributes
            List<JobAttributeVo> atts = null;
            String companyId = CompanyThreadLocal.getInstance().getValue();
            String uuId = ((JobImpl) job).getUuid();
            if (StringUtil.isNotEmptyAndNull(p_attributes))
            {
                Attributes attributes = com.globalsight.cxe.util.XmlUtil.string2Object(
                        Attributes.class, p_attributes);
                atts = (List<JobAttributeVo>) attributes.getAttributes();

                List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
                for (JobAttributeVo jobAttributeVo : atts)
                {
                    jobatts.add(AttributeUtil.createJobAttribute(jobAttributeVo));
                }
                RuntimeCache.addJobAtttibutesCache(uuId, jobatts);
            }
            else
            {
                AttributeSet as = ((JobImpl) job).getAttributeSet();
                if (as != null)
                {
                    List<Attribute> jas = as.getAttributeAsList();
                    List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
                    atts = new ArrayList<JobAttributeVo>();

                    for (Attribute ja : jas)
                    {
                        JobAttributeVo vo = AttributeUtil.getAttributeVo(ja.getCloneAttribute());
                        atts.add(vo);
                        jobatts.add(AttributeUtil.createJobAttribute(vo));
                    }
                    RuntimeCache.addJobAtttibutesCache(uuId, jobatts);
                }
            }

            // Sends events to cxe.
            int pageCount = sFiles.size();
            for (int i = 0; i < pageCount; i++)
            {
                File realFile = (File) sFiles.get(i);
                FileProfile realProfile = (FileProfile) sProFiles.get(i);
                String targetLocale = (String) stargetLocales.get(i);
                String path = realFile.getPath();
                String relativeName = path.substring(AmbFileStoragePathUtils.getCxeDocDir()
                        .getPath().length() + 1);

                publishEventToCxe(String.valueOf(job.getJobId()), job.getJobName(), i + 1,
                        pageCount, 1, 1, relativeName, Long.toString(realProfile.getId()),
                        targetLocale, (Integer) exitValues.get(i),
                        String.valueOf(job.getPriority()));
            }

            // set job attribute
            if (atts != null && atts.size() > 0)
            {
                AddJobAttributeThread thread = new AddJobAttributeThread(uuId, companyId);
                thread.setJobAttributeVos(atts);
                thread.createJobAttributes();
            }

            // Send email at the end.
            sendUploadCompletedEmail(filePaths, fileProfileIds, user, job.getJobName(), p_comment,
                    new Date());

            // It is allowed to create job with inactive file profile Ids, but
            // throw exception to warn user.
            ArrayList<String> inactive_list = new ArrayList<String>();
            for (String s : fileProfileIds)
            {
                FileProfile fp_inactive = HibernateUtil.get(FileProfileImpl.class,
                        Long.parseLong(s), true);
                if (fp_inactive == null)
                {
                    inactive_list.add(s);
                }
            }
            if (inactive_list.size() > 0)
            {
                String invalidFpIds = AmbassadorUtil.listToString(inactive_list);
                String errMsg = "You are using inactive profile ids " + invalidFpIds
                        + ", in a future release this create job may fail.";
                logger.warn(errMsg);
                throw new RestWebServiceException(errMsg);
            }
        }
        catch (Exception e)
        {
            if (job != null && !reCreate)
            {
                JobCreationMonitor.updateJobState(job, Job.IMPORTFAILED);
            }
            throw new RestWebServiceException(makeErrorJson(CREATE_JOB, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("Create job success.").build();
    }
    
    /**
     * Upload ZIP source file to server side.
     * 
     * @param p_companyID
     *            Company ID. Required.
     * @param p_jobName
     *            Job name. Required.
     * @param p_fileProfileId
     *            File profile ID for current file. Required.
     * @param p_input
     *            Uploading source file content. Required.
     *
     * @return A successful message
     * 
     * @throws RestWebServiceException
     * 
     */
    @POST
    @Path("/sourceFiles/zip")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadZipSourceFile(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @QueryParam("jobName") String p_jobName,
            @QueryParam("fileProfileIds") String p_fileProfileIds,
            MultipartInput p_input) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        boolean updateJobStateIfException = true;
        Job job = null;
        String jobId = null;
        User user;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobName", p_jobName);
            restArgs.put("fileProfileIds", p_fileProfileIds);
            restStart = RestWebServiceLog.start(JobResource.class, UPLOAD_ZIP_SOURCE_FILE, restArgs);
            checkPermission(userName, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);
            p_jobName = EditUtil.removeCRLF(p_jobName);
            String jobNameValidation = validateJobName(p_jobName);
            if (jobNameValidation != null)
            {
                throw new RestWebServiceException(jobNameValidation);
            }
            if (StringUtil.isEmpty(p_fileProfileIds))
            {
                throw new RestWebServiceException("Empty file profile id.");
            }
            String[] fileProfileIdArr = p_fileProfileIds.split(",");
            List<Long> l10nProfileIds = new ArrayList<Long>();
            List<Long> companyIds = new ArrayList<Long>();
            for (int i = 0; i < fileProfileIdArr.length; i++)
            {
                FileProfile fp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(
                        Long.parseLong(fileProfileIdArr[i]));
                if (!l10nProfileIds.contains(fp.getL10nProfileId()))
                {
                    l10nProfileIds.add(fp.getL10nProfileId());
                }
                
                if (!companyIds.contains(fp.getCompanyId()))
                {
                    companyIds.add(fp.getCompanyId());
                }
            }
            
            if (l10nProfileIds.size() > 1)
            {
                String message = "The p_fileProfileIds parameter of the ID values from different localize profile.";
                throw new RestWebServiceException(message);
            }
            
            if (companyIds.size() > 1)
            {
                String message = "The p_fileProfileIds parameter of the ID values from different company.";
                throw new RestWebServiceException(message);
            }
            
            long l10nProfileId = -1;
            if(l10nProfileIds.size() == 1)
                l10nProfileId = l10nProfileIds.get(0);
            
            long companyIdFromFileProfile = -1;
            if(companyIds.size() == 1)
                companyIdFromFileProfile = companyIds.get(0);

            BasicL10nProfile blp = HibernateUtil.get(BasicL10nProfile.class, l10nProfileId);
            String priority = String.valueOf(blp.getPriority());
            user = ServerProxy.getUserManager().getUserByName(userName);
            job = ServerProxy.getJobHandler().getJobByJobName(p_jobName);
            if (job != null)
                jobId = String.valueOf(job.getId());
            else
            {
                job = JobCreationMonitor.initializeJob(p_jobName, user.getUserId(),
                        l10nProfileId, priority, Job.UPLOADING);
                jobId = String.valueOf(job.getId());
            }

            String msg = checkIfCreateJobCalled(UPLOAD_ZIP_SOURCE_FILE, Long.parseLong(jobId),
                    p_jobName);
            if (msg != null)
            {
                updateJobStateIfException = false;
                throw new RestWebServiceException(msg);
            }

            if (!isInSameCompany(userName, String.valueOf(companyIdFromFileProfile))
                    && !UserUtil.isSuperPM(user.getUserId()))
            {
                String message = "Current user cannot upload file with the file profile which is in other company.";
                throw new RestWebServiceException(message);
            }

            String srcLocale = findSrcLocale(fileProfileIdArr[0]);
            StringBuffer newPath = new StringBuffer();
            newPath.append(AmbFileStoragePathUtils.getCxeDocDir(job.getCompanyId()));
            newPath.append(File.separator).append(srcLocale);
            newPath.append(File.separator).append("webservice");
            newPath.append(File.separator).append(jobId);

            List<InputPart> inputParts = p_input.getParts();
            for (InputPart inputPart : inputParts)
            {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                String contentDisposition = header.getFirst("Content-Disposition");
                String fileName = getFileNameFromHeaderInfo(contentDisposition);
                
                String extension = fileName.substring(fileName.lastIndexOf("."), fileName.length());
                if (!"rar".equalsIgnoreCase(extension) && "zip".equalsIgnoreCase(extension)
                        && !"7z".equalsIgnoreCase(extension))
                {
                    String message = "Current interface only supports compressed file upload.";
                    throw new RestWebServiceException(message);
                }
                if (fileName != null)
                {
                    // convert the uploaded file content to InputStream
                    InputStream inputStream = inputPart.getBody(InputStream.class, null);
                    byte[] bytes = IOUtils.toByteArray(inputStream);

                    File sourceFile = new File(newPath.toString(), fileName);
                    RestWebServiceUtil.writeFile(sourceFile, bytes);
                }
            }
        }
        catch (Exception e)
        {
            if (jobId != null && updateJobStateIfException)
            {
                JobCreationMonitor.updateJobState(Long.parseLong(jobId), Job.IMPORTFAILED);
            }
            logger.error(e);
            throw new RestWebServiceException(makeErrorJson(UPLOAD_ZIP_SOURCE_FILE, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
        return Response.status(200).entity("File is uploaded successfully for job: " + jobId)
                .build();
    }

    /**
     * Create a job for zip file
     * 
     * @param p_companyID
     *            Company ID. Required.
     * @param p_jobId
     *            Job id. Required.
     * @param p_filePaths
     *            String Path of files which are contained in job, split by
     *            ",".Example:"test_zip_01.zip,test_zip_02.rar" Required.
     * @param p_fileProfileIds
     *            String ID of file profiles, split by ",".
     *            Example:"1001,1002".This file profile id must correspond to
     *            the files in a compressed file format. Required.
     * @param p_targetLocales
     *            String target locale which like to be translated,split by
     *            ",".Example: "fr_FR,de_DE" or empty. The target language of
     *            all files is the same. Optional.
     * @param p_comment
     *            String Job comment. Optional.
     * @param p_attributes
     *            String Attributes used to create job. Optional. Example:
     *            <attributes> <attributes xsi:type="fileJobAttributeVo"
     *            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *            <displayName>file_01</displayName>
     *            <fromSuperCompany>false</fromSuperCompany>
     *            <internalName>file_01</internalName>
     *            <required>false</required> <type>file</type> </attributes>
     *            <attributes xsi:type="textJobAttributeVo"
     *            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     *            <displayName>file_02</displayName>
     *            <fromSuperCompany>false</fromSuperCompany>
     *            <internalName>file_02</internalName>
     *            <required>false</required> <type>text</type> </attributes>
     *            </attributes>
     * @return A "Create job success." message.
     * 
     */
    @POST
    @Path("/createJob/zip")
    @Produces(MediaType.TEXT_PLAIN)
    public Response createJobForZipFile(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @QueryParam("jobId") String p_jobId,
            @QueryParam("filePaths") String p_filePaths,
            @QueryParam("fileProfileIds") String p_fileProfileIds,
            @QueryParam("targetLocales") String p_targetLocales,
            @QueryParam("comment") String p_comment,
            @QueryParam("attributes") String p_attributes) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        boolean reCreate = false;
        Job job = null;
        try
        {
            Vector<String> fileProfileIds = new Vector<String>();
            Vector<String> filePaths = new Vector<String>();
            Vector<String> targetLocales = new Vector<String>();
            Vector<String> realUploadFileList = new Vector<String>();
            Vector<String> extensionList = new Vector<String>();
            Vector<FileProfile> realFileProfiles = new Vector<FileProfile>();
            String fpId = "",filename = "", zipFilename = "",zipDir = "",vTargetLocale = "";
            FileProfile checkFp = null, referenceFP = null;
            Vector fileProfiles = new Vector();
            Vector files = new Vector();
            Vector afterTargetLocales = new Vector();
            ArrayList<String> zipFiles = null;
            long referenceFPId = 0l;
            boolean isWSFlag = true;
            String userName = getUserNameFromSession(accessToken.get(0));
            User user = ServerProxy.getUserManager().getUserByName(userName);

            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobId", p_jobId);
            restArgs.put("comment", p_comment);
            restArgs.put("filePaths", p_filePaths);
            restArgs.put("fileProfileIds", p_fileProfileIds);
            restArgs.put("targetLocales", p_targetLocales);
            restArgs.put("attributes", p_attributes);
            restStart = RestWebServiceLog.start(JobResource.class, CREATE_JOB, restArgs);

            checkPermission(userName, Permission.CUSTOMER_UPLOAD_VIA_WEBSERVICE);
            Assert.assertIsInteger(p_jobId);
            if (StringUtil.isNotEmpty(p_jobId))
                job = JobCreationMonitor.loadJobFromDB(Long.parseLong(p_jobId));
            if (job == null)
            {
                String msg = "current jobId : " + p_jobId + " does not exist.";
                throw new RestWebServiceException(msg);
            }
            
            String msg = checkIfCreateJobCalled(CREATE_JOB, job.getId(), job.getJobName());
            if (msg != null)
            {
                reCreate = true;
                throw new RestWebServiceException(msg);
            }
            cachedJobIds.add(job.getId());
            if (StringUtil.isNotEmptyAndNull(p_targetLocales))
            {
                if (p_targetLocales.contains("|"))
                {
                    String message = "Target locales : " + p_targetLocales + " format not correct.";
                    throw new RestWebServiceException(message);
                }
            }
            validateParameters(job, p_filePaths, p_fileProfileIds, p_targetLocales, fileProfileIds,
                    filePaths, targetLocales, true);
            
            fpId = (String) fileProfileIds.get(0);
            for (int i = 0; i < filePaths.size(); i++)
            {
                filename = (String) filePaths.get(i);
                filename = filename.replace('\\', File.separatorChar);
                String srcLocale = findSrcLocale(fpId);
                filename = getRealPath(p_jobId, filename, srcLocale, isWSFlag);
                zipFilename = AmbFileStoragePathUtils.getCxeDocDir(job.getCompanyId())
                        + File.separator + filename;
                File zipFile = new File(zipFilename);
                if (isSupportedZipFileFormat(zipFile) && isUnCompress(zipFile))
                {
                    addUploadFile(realUploadFileList,extensionList,zipFile);
                }
            }
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    Long.parseLong(p_companyID));
            @SuppressWarnings({ "unchecked", "rawtypes" })
            List<FileProfileImpl> fileProfileListOfCompany = (List) ServerProxy
                    .getFileProfilePersistenceManager().getFileProfilesByExtension(extensionList,
                            Long.valueOf(company.getId()));
            for (String id : fileProfileIds)
            {
                checkFp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(
                        Long.valueOf(id));
                if (!fileProfileListOfCompany.contains(checkFp))
                {
                    String message = "Current file profile id: " + id
                            + " is not corresponds the upload files.";
                    throw new RestWebServiceException(message);
                }
                realFileProfiles.add(checkFp);
            }
            
            FileProfilePersistenceManager fppm = ServerProxy.getFileProfilePersistenceManager();
            for (int i = 0; i < realUploadFileList.size(); i++)
            {
                vTargetLocale = targetLocales.get(0);
                File file = new File(realUploadFileList.get(i).replace('\\', File.separatorChar));
                filename = file.getName();
                String fileExtension = filename.substring(filename.lastIndexOf(".") + 1);
                Vector<String> tempExtensionList = new Vector<String>();
                tempExtensionList.add(fileExtension);
                List<FileProfileImpl> fileProfileList = (List) ServerProxy
                        .getFileProfilePersistenceManager().getFileProfilesByExtension(tempExtensionList,
                                Long.valueOf(company.getId()));
                for (FileProfile filePro : realFileProfiles)
                {
                    if (fileProfileList.contains(filePro))
                    {
                        if (file.getAbsolutePath().endsWith(".xml"))
                        {
                            saveFileAsUTF8(file);
                        }
                        // indicates this is an "XLZ" format file profile
                        if (48 == filePro.getKnownFormatTypeId())
                        {
                            referenceFPId = filePro.getReferenceFP();
                            referenceFP = fppm.readFileProfile(referenceFPId);
                            zipDir = realUploadFileList.get(i).substring(0,
                                    realUploadFileList.get(i).lastIndexOf("."));
                            zipFiles = ZipIt.unpackZipPackage(realUploadFileList.get(i), zipDir);
                            String relativePath = filename.substring(0, filename.lastIndexOf("."));
                            String tmp = "";
                            for (String f : zipFiles)
                            {
                                if (XliffFileUtil.isXliffFile(f))
                                {
                                    tmp = relativePath + File.separator + f;
                                    changeFileListByXliff(tmp, vTargetLocale, referenceFP,
                                            fileProfiles, files, afterTargetLocales);
                                }
                            }
                        }
                        else if (39 == filePro.getKnownFormatTypeId())
                        {
                            changeFileListByXliff(filename, vTargetLocale, filePro, fileProfiles,
                                    files, afterTargetLocales);
                        }
                        else
                        {
                            fileProfiles.add(filePro);
                            files.add(file);
                            afterTargetLocales.add(vTargetLocale);
                        }
                    }
                }
            }

            Vector result = FileSystemUtil.execScript(files, fileProfiles, afterTargetLocales,
                    Long.parseLong(p_jobId), job.getJobName());
            Vector sFiles = (Vector) result.get(0);
            Vector sProFiles = (Vector) result.get(1);
            Vector stargetLocales = (Vector) result.get(2);
            Vector exitValues = (Vector) result.get(3);
            // cache job attributes
            List<JobAttributeVo> atts = null;
            String companyId = CompanyThreadLocal.getInstance().getValue();
            String uuId = ((JobImpl) job).getUuid();
            if (StringUtil.isNotEmptyAndNull(p_attributes))
            {
                Attributes attributes = com.globalsight.cxe.util.XmlUtil.string2Object(
                        Attributes.class, p_attributes);
                atts = (List<JobAttributeVo>) attributes.getAttributes();

                List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
                for (JobAttributeVo jobAttributeVo : atts)
                {
                    jobatts.add(AttributeUtil.createJobAttribute(jobAttributeVo));
                }
                RuntimeCache.addJobAtttibutesCache(uuId, jobatts);
            }
            else
            {
                AttributeSet as = ((JobImpl) job).getAttributeSet();
                if (as != null)
                {
                    List<Attribute> jas = as.getAttributeAsList();
                    List<JobAttribute> jobatts = new ArrayList<JobAttribute>();
                    atts = new ArrayList<JobAttributeVo>();

                    for (Attribute ja : jas)
                    {
                        JobAttributeVo vo = AttributeUtil.getAttributeVo(ja.getCloneAttribute());
                        atts.add(vo);
                        jobatts.add(AttributeUtil.createJobAttribute(vo));
                    }
                    RuntimeCache.addJobAtttibutesCache(uuId, jobatts);
                }
            }

            // Sends events to cxe.
            int pageCount = sFiles.size();
            for (int i = 0; i < pageCount; i++)
            {
                File realFile = (File) sFiles.get(i);
                FileProfile realProfile = (FileProfile) sProFiles.get(i);
                String targetLocale = (String) stargetLocales.get(i);
                String path = realFile.getPath();
                String relativeName = path.substring(AmbFileStoragePathUtils.getCxeDocDir()
                        .getPath().length() + 1);

                publishEventToCxe(String.valueOf(job.getJobId()), job.getJobName(), i + 1,
                        pageCount, 1, 1, relativeName, Long.toString(realProfile.getId()),
                        targetLocale, (Integer) exitValues.get(i),
                        String.valueOf(job.getPriority()));
            }

            // set job attribute
            if (atts != null && atts.size() > 0)
            {
                AddJobAttributeThread thread = new AddJobAttributeThread(uuId, companyId);
                thread.setJobAttributeVos(atts);
                thread.createJobAttributes();
            }

            // Send email at the end.
            sendUploadCompletedEmail(filePaths, fileProfileIds, user, job.getJobName(), p_comment,
                    new Date());

            // It is allowed to create job with inactive file profile Ids, but
            // throw exception to warn user.
            ArrayList<String> inactive_list = new ArrayList<String>();
            for (String s : fileProfileIds)
            {
                FileProfile fp_inactive = HibernateUtil.get(FileProfileImpl.class,
                        Long.parseLong(s), true);
                if (fp_inactive == null)
                {
                    inactive_list.add(s);
                }
            }
            if (inactive_list.size() > 0)
            {
                String invalidFpIds = AmbassadorUtil.listToString(inactive_list);
                String errMsg = "You are using inactive profile ids " + invalidFpIds
                        + ", in a future release this create job may fail.";
                logger.warn(errMsg);
                throw new RestWebServiceException(errMsg);
            }
        }
        catch (Exception e)
        {
            if (job != null && !reCreate)
            {
                JobCreationMonitor.updateJobState(job, Job.IMPORTFAILED);
            }
            throw new RestWebServiceException(makeErrorJson(CREATE_JOB, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }

        return Response.status(200).entity("Create job success.").build();
    }
    
    /**
     * Get job status by job id.
     * 
     * @param p_companyID
     *                  Company ID. Required.
     * @param p_jobId
     *                  Job id.  Required.
     * 
     * @return Return job status for JSON
     * 
     */
    @GET
    @Path("/{jobId}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobStatus(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @PathParam("jobId") String p_jobId) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            String name = "";
            String status;
            String userName = getUserNameFromSession(accessToken.get(0));
            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobId", p_jobId);
            restStart = RestWebServiceLog.start(JobResource.class, GET_JOB_STATUS, restArgs);

            String sql = "SELECT NAME,STATE FROM JOB WHERE COMPANY_ID=? AND ID=?";
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setLong(1, CompanyWrapper.getCompanyById(p_companyID).getId());
            query.setString(2, p_jobId);

            results = query.executeQuery();
            if (results.next())
            {
                name = results.getString(1);
                status = results.getString(2);
            }
            else
            {
                // This job is not in the table of DB, it was not created
                status = "UNKNOWN";
            }

            GetJobStatusResponse jobStatus = new GetJobStatusResponse();
            jobStatus.setId(Long.valueOf(p_jobId));
            jobStatus.setName(name);
            jobStatus.setstatus(status);

            return Response.status(200).entity(jobStatus).build();
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_JOB_STATUS, e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
    }

    /**
     * Gets exported files in one zip by job ids
     * 
     * @param p_companyID
     *                  Company ID. Required.
     * @param p_jobIds
     *                  Job ids. Required.
     * 
     * @return Return all export files compressed.
     * 
     */
    @GET
    @Path("/{jobIds}/targetFiles")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJobExportedFiles(
            @HeaderParam("accessToken") List<String> accessToken,
            @PathParam("companyID") String p_companyID,
            @PathParam("jobIds") String p_jobIds) throws RestWebServiceException
    {
        RestWebServiceLog.Start restStart = null;
        try
        {
            String userName = getUserNameFromSession(accessToken.get(0));
            User user = ServerProxy.getUserManager().getUserByName(userName);
            Map<Object, Object> restArgs = new HashMap<Object, Object>();
            restArgs.put("loggedUserName", userName);
            restArgs.put("companyID", p_companyID);
            restArgs.put("jobIds", p_jobIds);
            restStart = RestWebServiceLog.start(JobResource.class, GET_JOB_EXPORT_FILES,
                    restArgs);
            checkPermission(userName, Permission.JOBS_VIEW);
            checkPermission(userName, Permission.JOBS_EXPORT);
            checkPermission(userName, Permission.JOBS_DOWNLOAD);
            Company company = ServerProxy.getJobHandler().getCompanyById(
                    Long.parseLong(p_companyID));

            String errorMsg = "";
            p_jobIds = p_jobIds.replace(" ", "");
            if (p_jobIds == null || p_jobIds.trim() == "")
            {
                errorMsg = "Job ids can not be empty.";
                throw new RestWebServiceException(errorMsg);
            }

            String[] jobIdlist = p_jobIds.split(",");
            Set<Long> jobIds = new HashSet<Long>();
            Set<String> companyIds = new HashSet<String>();
            Set<String> locales = new HashSet<String>();
            for (String id : jobIdlist)
            {
                long jobId = Long.parseLong(id);
                Job job = JobCreationMonitor.loadJobFromDB(jobId);
                if (job == null)
                {
                    errorMsg = "Job " + jobId + " does not exist.";
                    throw new RestWebServiceException(errorMsg);
                }

                String jobCompanyId = String.valueOf(job.getCompanyId());
                if (!String.valueOf(company.getId()).equals(jobCompanyId)
                        && !UserUtil.isSuperAdmin(user.getUserId())
                        && !UserUtil.isSuperPM(user.getUserId()))
                {
                    errorMsg = "Job " + jobId + " is not from the user's company.";
                    throw new RestWebServiceException(errorMsg);
                }

                jobIds.add(jobId);
                companyIds.add(jobCompanyId);
            }

            if (UserUtil.isSuperPM(user.getUserId()) && companyIds.size() > 1)
            {
                errorMsg = user.getUserId()
                        + " is super PM, but job ids are not from the same company.";
                throw new RestWebServiceException(errorMsg);
            }

            // Check if there are exporting workflows
            List<Workflow> exportingwfs = new ArrayList<Workflow>();
            for (Long id : jobIds)
            {
                for (Workflow wf : JobCreationMonitor.loadJobFromDB(id).getWorkflows())
                {
                    if (WorkflowExportingHelper.isExporting(wf.getId()))
                    {
                        exportingwfs.add(wf);
                    }
                }
            }
            // Find all entry files
            Set<String> jobFileList = new HashSet<String>();
            Set<File> entryFiles = new HashSet<File>();
            String identifyKey = AmbassadorUtil.getRandomFeed();
            findAllEntryFiles(exportingwfs, jobIds, entryFiles, jobFileList, locales);

            String tempS = jobIds.toString();
            String jobNamesstr = tempS.substring(1, tempS.length() - 1);
            String zipName = "GlobalSight_Download_jobs(" + jobNamesstr + ").zip";

            // Zip all files into zip file
            if (exportingwfs.size() == 0 && jobFileList.size() > 0)
            {
                try
                {
                    String cxedocpath = AmbFileStoragePathUtils.getCxeDocDirPath(company.getId());
                    if (UserUtil.isSuperAdmin(user.getUserId())
                            || UserUtil.isSuperPM(user.getUserId()))
                    {
                        cxedocpath = cxedocpath + "/Welocalize";
                    }
                    File zipFileDir = new File(cxedocpath + "/webservice_zip" + "/" + identifyKey);
                    File zipfile = new File(zipFileDir, zipName);
                    zipfile.getParentFile().mkdirs();

                    Map<File, String> entryFileToFileNameMap = getEntryFileToFileNameMap(
                            entryFiles, jobIds, locales, cxedocpath,
                            String.valueOf(user.getUserId()));
                    ZipIt.addEntriesToZipFile(zipfile, entryFileToFileNameMap, "");

                    ResponseBuilder response = Response.ok((Object) zipfile);
                    response.header("Content-Disposition", "attachment; filename=\"" + zipName
                            + "\"");
                    response.encoding("gzip");
                    return response.build();
                }
                catch (Exception e)
                {
                    logger.error("Error found in getJobExportedFiles.", e);
                    throw new RestWebServiceException(e.getMessage());
                }
            }
            else
            {
                GetJobExportFileResponse exportJobFiles = new GetJobExportFileResponse();
                exportJobFiles.setRequestedJobIds(p_jobIds);
                if (exportingwfs.size() > 0)
                {
                    for (Workflow wfone : exportingwfs)
                    {
                        exportJobFiles.addExportingJobs(wfone.getJob().getId(), wfone
                                .getTargetLocale().toString());
                    }
                    exportJobFiles.setMessage("");
                    exportJobFiles.setPath("");
                    
                    return Response.ok().entity(exportJobFiles).build();
                }
                else if (jobFileList.size() == 0)
                {
                    exportJobFiles.setMessage("No exported files found");
                    exportJobFiles.setPath("");
                    return Response.ok().entity(exportJobFiles).build();
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_JOB_EXPORT_FILES,
                    e.getMessage()));
        }
        finally
        {
            if (restStart != null)
            {
                restStart.end();
            }
        }
        return null;
    }

    private String getJobName(String p_jobName) throws RestWebServiceException
    {
        Connection connection = null;
        PreparedStatement query = null;
        ResultSet results = null;
        try
        {
            String randomStr = String.valueOf((new Random()).nextInt(999999999));
            while (randomStr.length() < 9)
            {
                randomStr = "0" + randomStr;
            }
            String uniqueJobName = p_jobName + "_" + randomStr;
            String sql = "SELECT ID FROM JOB WHERE NAME=?";
            connection = ConnectionPool.getConnection();
            query = connection.prepareStatement(sql);
            query.setString(1, uniqueJobName);
            results = query.executeQuery();
            if (results.next())
            {
                releaseDBResource(results, query, connection);
                return getJobName(p_jobName);
            }
            else
            {
                return uniqueJobName;
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(e.getMessage());
        }
        finally
        {
            releaseDBResource(results, query, connection);
        }
    }
    
    private Map<File, String> getEntryFileToFileNameMap(Set<File> entryFiles, Set<Long> jobIdSet,
            Set<String> locales, String cxeDocsDirPath, String userId)
    {
        Map<File, String> entryFileToFileNameMap = new HashMap<File, String>();
        File tempFile;

        for (Long jobId : jobIdSet)
        {
            ArrayList<String> entryNames = new ArrayList<String>();
            String prefixPassolo = cxeDocsDirPath + File.separator + "passolo" + File.separator
                    + jobId;
            for (File entryFile : entryFiles)
            {
                String entryFilePath = entryFile.getPath();
                if (entryFilePath.startsWith(prefixPassolo))
                {
                    entryNames.add(entryFilePath.replaceAll("\\\\", "/"));
                }
            }
            if (entryNames.size() > 0)
            {
                Map<String, String> tempMap = ZipIt.getEntryNamesMap(entryNames);
                for (String key : tempMap.keySet())
                {
                    tempFile = new File(key);
                    entryFileToFileNameMap.put(tempFile, jobId + File.separator + "passolo"
                            + File.separator + tempMap.get(key));
                }
            }

            for (String locale : locales)
            {
                entryNames.clear();
                String prefixStr1 = cxeDocsDirPath.replace("/", "\\") + File.separator + locale
                        + File.separator + jobId;
                String prefixStr2 = cxeDocsDirPath.replace("/", "\\") + File.separator + locale
                        + File.separator + "webservice" + File.separator + jobId;
                String prefixStr3 = File.separator + locale + File.separator + "webservice"
                        + File.separator + jobId;
                String prefixStr4 = File.separator + locale + File.separator + jobId;
                for (File entryFile : entryFiles)
                {
                    String entryFilePath = entryFile.getPath();
                    if (!UserUtil.isSuperAdmin(userId) && !UserUtil.isSuperPM(userId))
                    {
                        if (entryFilePath.startsWith(prefixStr1)
                                || entryFilePath.startsWith(prefixStr2))
                        {
                            entryNames.add(entryFilePath.replaceAll("\\\\", "/"));
                        }
                    }
                    else
                    {
                        if (entryFilePath.contains(prefixStr3)
                                || entryFilePath.contains(prefixStr4))
                        {
                            entryNames.add(entryFilePath.replaceAll("\\\\", "/"));
                        }
                    }
                }
                if (entryNames.size() > 0)
                {
                    Map<String, String> tempMap = ZipIt.getEntryNamesMap(entryNames);
                    for (String key : tempMap.keySet())
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

    private void findAllEntryFiles(List<Workflow> exportingwfs, Set<Long> jobIds,
            Set<File> entryFiles, Set<String> jobFileList, Set<String> locales)
            throws RestWebServiceException
    {
        try
        {
            FileProfilePersistenceManager fpManager = ServerProxy
                    .getFileProfilePersistenceManager();
            if (exportingwfs.size() == 0)
            {
                for (Long jobId : jobIds)
                {
                    Job job = JobCreationMonitor.loadJobFromDB(jobId);
                    JobFiles jobFileInOneJob = new JobFiles();
                    long fileProfileId = -1l;
                    FileProfile fp = null;
                    boolean isXLZFile = false;
                    Set<String> passoloFiles = new HashSet<String>();
                    for (Workflow w : job.getWorkflows())
                    {
                        ArrayList<String> fileList = new ArrayList<String>();
                        for (TargetPage page : w.getTargetPages())
                        {
                            SourcePage sPage = page.getSourcePage();
                            if (sPage != null && sPage.isPassoloPage())
                            {
                                String p = sPage.getPassoloFilePath();
                                p = p.replace("\\", "/");
                                p = p.substring(p.indexOf("/") + 1);
                                passoloFiles.add(p);
                                if (fileList.contains(p))
                                    continue;
                                else
                                {
                                    fileList.add(p);
                                }

                                continue;
                            }

                            fileProfileId = sPage.getRequest().getFileProfileId();
                            fp = fpManager.getFileProfileById(fileProfileId, false);
                            if (fpManager.isXlzReferenceXlfFileProfile(fp.getName()))
                                isXLZFile = true;

                            String path = page.getExternalPageId();
                            path = path.replace("\\", "/");
                            if (StringUtil.isNotEmpty(fp.getScriptOnExport()))
                            {
                                path = handlePathForScripts(path, job);
                            }
                            int index = path.indexOf("/");
                            path = path.substring(index);
                            path = getRealFilePathForXliff(path, isXLZFile);

                            if (fileList.contains(path))
                                continue;
                            else
                            {
                                fileList.add(path);
                            }

                            StringBuffer allPath = new StringBuffer();
                            allPath.append(page.getGlobalSightLocale());
                            for (String s : path.split("/"))
                            {
                                if (s.length() > 0)
                                {
                                    allPath.append("/").append(s);
                                }
                            }
                            jobFileInOneJob.addPath(allPath.toString());

                            isXLZFile = false;
                        }
                    }

                    for (String path : passoloFiles)
                    {
                        StringBuffer allPath = new StringBuffer();
                        allPath.append("passolo");
                        for (String s : path.split("/"))
                        {
                            if (s.length() > 0)
                            {
                                allPath.append("/").append(URLEncoder.encode(s, "utf-8"));
                            }
                        }
                        jobFileInOneJob.addPath(allPath.toString());
                    }

                    for (String path : jobFileInOneJob.getPaths())
                    {
                        String[] pathlist = path.split("/");
                        String entryfilepath = (AmbFileStoragePathUtils.getCxeDocDirPath(job
                                .getCompanyId()) + "\\" + path).replace("/", "\\");
                        File entryfile = new File(entryfilepath);
                        if (entryfile.exists() && entryfile.isFile())
                        {
                            entryFiles.add(entryfile);
                            jobFileList.add(pathlist[0] + "/" + pathlist[pathlist.length - 2] + "/"
                                    + pathlist[pathlist.length - 1]);
                            locales.add(pathlist[0]);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(GET_JOB_EXPORT_FILES,
                    e.getMessage()));
        }
    }

    private String handlePathForScripts(String path, Job job)
    {
        path = path.replace("\\", "/");
        String finalPath = path;
        // for new scripts on import/export
        if (path.contains("/PreProcessed_" + job.getId() + "_"))
        {
            finalPath = path.replace(
                    path.substring(path.lastIndexOf("/PreProcessed_" + job.getId() + "_"),
                            path.lastIndexOf("/")), "");
        }
        // compatible codes for old import/export
        else
        {
            int index = path.lastIndexOf("/");
            if (index > -1)
            {
                String fileName = path.substring(index + 1);
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                String rest = path.substring(0, index);
                if (rest.endsWith("/" + fileName))
                {
                    finalPath = rest + "." + extension;
                }
            }
        }

        return finalPath;
    }

    private String getRealFilePathForXliff(String path, boolean isXLZFile)
    {
        if (StringUtil.isEmpty(path))
            return path;

        int index = -1;
        index = path.lastIndexOf(".sub/");
        if (index > 0)
        {
            // one big xliff file is split to some sub-files
            path = path.substring(0, index);
        }
        if (isXLZFile)
        {
            path = path.substring(0, path.lastIndexOf("/"));
            path += ".xlz";
        }

        return path;
    }

    private void validateParameters(Job job, String p_filePaths, String p_fileProfileIds,
            String p_targetLocales, Vector<String> fileProfileIds, Vector<String> filePaths,
            Vector<String> targetLocales, boolean isZipFileCreateJob)
            throws RestWebServiceException
    {
       
        try
        {
            if (isZipFileCreateJob)
            {
                // filePaths
                for (String path : (p_filePaths).split(","))
                {
                    filePaths.add(path);
                }
                for (String fId : (p_fileProfileIds).split(","))
                {
                    fileProfileIds.add(fId);
                }
            }
            else
            {
                // filePaths
                for (String path : (p_filePaths).split("\\|"))
                {
                    filePaths.add(path);
                }
                // fileProfileIds
                for (String fId : (p_fileProfileIds).split("\\|"))
                {
                    fileProfileIds.add(fId);
                }
            }

            ArrayList<String> list = new ArrayList<String>();
            for (String id : fileProfileIds)
            {
                FileProfile fp = HibernateUtil.get(FileProfileImpl.class, Long.parseLong(id), false);
                if (fp == null)
                {
                    list.add(id);
                }
            }
            if (list.size() > 0)
            {
                String invalidFpIds = AmbassadorUtil.listToString(list);
                String errMsg = "Below file profiles do not exist : " + invalidFpIds;
                throw new RestWebServiceException(errMsg);
            }

            FileProfile fp;
            for (String fpids : fileProfileIds)
            {
                long iFpId = Long.parseLong(fpids);
                fp = ServerProxy.getFileProfilePersistenceManager().readFileProfile(iFpId);
                if (fp.getCompanyId() != job.getCompanyId())
                {
                    String message = "Current user cannot create job with the file profile which is in other company.";
                    throw new RestWebServiceException(message);
                }
            }

            // targetLocales
            if (StringUtil.isNotEmpty(p_targetLocales))
            {
                handleTargetLocales(targetLocales, p_targetLocales, fileProfileIds.size(),
                        isZipFileCreateJob);
            }
            else
            {
                handleTargetLocales(targetLocales, "", fileProfileIds.size(), isZipFileCreateJob);
            }

            GlobalSightLocale[] targetLocales_l10n = job.getL10nProfile().getTargetLocales();
            Set<String> gls = new HashSet<String>();
            for (GlobalSightLocale trgLoc_l10n : targetLocales_l10n)
            {
                gls.add(trgLoc_l10n.toString().toLowerCase());
            }
            StringBuilder sb = new StringBuilder();
            for (String trgLocs : targetLocales)
            {
                for (String trgLoc : trgLocs.split(","))
                {
                    if (StringUtil.isNotEmpty(trgLocs) && !gls.contains(trgLoc.toLowerCase()))
                    {
                        sb.append(",");
                        sb.append(trgLoc);
                    }
                }
            }
            if (sb.length() > 0)
            {
                String message = "Invalid or non-exsit tagetLocale: " + sb.toString().substring(1)
                        + "  in current L10nProfile";
                throw new RestWebServiceException(message);
            }

            if (filePaths != null && filePaths.size() > 0)
            {
                for (int i = 0; i < filePaths.size(); i++)
                {
                    String filePath = (String) filePaths.get(i);
                    String extensionMsg = checkExtensionExisted(filePath);
                    if (extensionMsg != null)
                    {
                        throw new RestWebServiceException(extensionMsg);
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(e.getMessage());
        }
    }

    private void publishEventToCxe(String p_jobId, String p_batchId, int p_pageNum,
            int p_pageCount, int p_docPageNum, int p_docPageCount, String p_fileName,
            String p_fileProfileId, String p_targetLocales, Integer p_exitValueByScript,
            String p_priority) throws Exception
    {
        String key = p_batchId + p_fileName + p_pageNum;
        CxeProxy.setTargetLocales(key, p_targetLocales);
        logger.info("Publishing import request to CXE for file " + p_fileName);
        CxeProxy.importFromFileSystem(p_fileName, p_jobId, p_batchId, p_fileProfileId,
                Integer.valueOf(p_pageCount), Integer.valueOf(p_pageNum),
                Integer.valueOf(p_docPageCount), Integer.valueOf(p_docPageNum), Boolean.TRUE,
                Boolean.FALSE, CxeProxy.IMPORT_TYPE_L10N, p_exitValueByScript, p_priority);
    }

    private void sendUploadCompletedEmail(List p_fileNames, List p_fpIds, User user,
            String p_jobName, String p_jobComment, Date p_uploadDate)
    {
        try
        {
            Object[] projects = getProjectsFromFPIds(p_fpIds);
            int projectsLength = projects.length;
            String companyIdStr = String.valueOf(((Project) projects[0]).getCompanyId());

            String[] messageArguments = new String[7];
            messageArguments[1] = p_jobName;
            messageArguments[2] = p_jobComment;

            // Prepare the project label and name since project can be
            // displayed as either "Division" or "Project"
            StringBuffer sb = new StringBuffer();
            sb.append("Project: ");
            for (int i = 0; i < projectsLength; i++)
            {
                sb.append(((Project) projects[i]).getName());
                if (i != projectsLength - 1)
                    sb.append(", ");
            }
            messageArguments[3] = sb.toString();

            sb = new StringBuffer();
            sb.append(user.getUserName());
            sb.append(" (");
            sb.append(user.getEmail());
            sb.append(")");
            messageArguments[4] = sb.toString();

            sb = new StringBuffer();
            int filesLength = p_fileNames.size();
            if (filesLength > 1)
                sb.append("\r\n");
            for (int i = 0; i < filesLength; i++)
            {
                // en_us\webservice\test\test.txt (fileprofile_1)
                sb.append(p_fileNames.get(i))
                        .append("  (")
                        .append(ServerProxy.getFileProfilePersistenceManager()
                                .readFileProfile(Long.parseLong(p_fpIds.get(i).toString()))
                                .getName()).append(")");
                if (i != filesLength - 1)
                    sb.append("\r\n");
            }
            messageArguments[5] = sb.toString();
            messageArguments[6] = user.getSpecialNameForEmail();

            Timestamp time = new Timestamp();
            time.setLocale(getUserLocal(user));
            time.setDate(p_uploadDate);
            messageArguments[0] = time.toString();

            writeResultToLogFile(messageArguments);

            boolean m_systemNotificationEnabled = EventNotificationHelper
                    .systemNotificationEnabled();
            if (!m_systemNotificationEnabled)
            {
                return;
            }

            UserParameterPersistenceManagerLocal uppml = new UserParameterPersistenceManagerLocal();
            UserParameter up = uppml.getUserParameter(user.getUserId(),
                    UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
            if (up != null && up.getIntValue() == 1)
            {
                ServerProxy.getMailer().sendMailFromAdmin(user, messageArguments,
                        MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
                        MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE, companyIdStr);
            }
            // get the PMs address (could be a group alias)
            List pms = new ArrayList();
            boolean add = true;
            for (int i = 0; i < projectsLength; i++)
            {
                User u = UserHandlerHelper.getUser(((Project) projects[i]).getProjectManagerId());
                if (u == null)
                {
                    logger.error("Can not get project manager for DesktopIcon upload notification by project "
                            + (Project) projects[i]);
                    return;
                }
                if (u.getUserId().equals(user.getUserId()))
                {
                    add = false;
                }
                else
                {
                    for (Iterator iter = pms.iterator(); iter.hasNext();)
                    {
                        User element = (User) iter.next();
                        if (u.getUserId().equals(element.getUserId()))
                            add = false;
                    }
                }
                if (add)
                    pms.add(u);
            }
            if (pms == null || pms.isEmpty())
            {
                if (add)
                {
                    logger.error("There was no GlobalSight project manager email address for DesktopIcon upload notification.");
                }

                return;
            }

            // send an email to the PMs
            for (Iterator iter = pms.iterator(); iter.hasNext();)
            {
                User u = (User) iter.next();
                time = new Timestamp();
                time.setLocale(getUserLocal(u));
                time.setDate(p_uploadDate);
                messageArguments[0] = time.toString();
                messageArguments[6] = u.getSpecialNameForEmail();

                up = uppml.getUserParameter(u.getUserId(), UserParamNames.NOTIFY_SUCCESSFUL_UPLOAD);
                if (up != null && up.getIntValue() == 1)
                {
                    ServerProxy.getMailer().sendMailFromAdmin(u, messageArguments,
                            MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_SUBJECT,
                            MailerConstants.DESKTOPICON_UPLOAD_COMPLETED_MESSAGE, companyIdStr);
                }
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to send the file upload completion emails.", e);
        }
    }

    private Locale getUserLocal(User p_user)
    {
        String dl = p_user.getDefaultUILocale();
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

    private Object[] getProjectsFromFPIds(List p_fps) throws Exception
    {
        List projects = new ArrayList();
        int len = p_fps.size();
        for (int i = 0; i < len; i++)
        {
            String fileProfileId = (String) p_fps.get(i);
            long fpid = Long.parseLong(fileProfileId);
            FileProfilePersistenceManager fppm = ServerProxy.getFileProfilePersistenceManager();
            FileProfile fp = fppm.readFileProfile(fpid);
            Project p = getProject(fp);
            boolean add = true;
            for (Iterator iter = projects.iterator(); iter.hasNext();)
            {
                Project e = (Project) iter.next();
                if (e.getId() == p.getId())
                    add = false;
            }
            if (add)
                projects.add(p);
        }

        return projects.toArray();
    }

    private Project getProject(FileProfile p_fp)
    {
        Project p = null;
        try
        {
            long l10nProfileId = p_fp.getL10nProfileId();
            L10nProfile lp = ServerProxy.getProjectHandler().getL10nProfile(l10nProfileId);
            p = lp.getProject();
        }
        catch (Exception e)
        {
            logger.error("Failed to get the project that file profile " + p_fp.toString()
                    + " is associated with.", e);
            // just leave and return NULL
        }
        return p;
    }

    private void changeFileListByXliff(String p_filename, String p_targetLocale,
            FileProfile p_fileProfile, Vector p_fileProfileList, Vector p_fileList,
            Vector p_afterTargetLocales)
    {
        Hashtable<String, FileProfile> splitFiles = new Hashtable<String, FileProfile>();
        XliffFileUtil.processMultipleFileTags(splitFiles, p_filename, p_fileProfile);
        if (splitFiles != null && splitFiles.size() > 0)
        {
            for (Iterator<String> iterator = splitFiles.keySet().iterator(); iterator.hasNext();)
            {
                String tmp = iterator.next();
                p_fileList.add(new File(AmbFileStoragePathUtils.getCxeDocDir(), tmp));
                p_fileProfileList.add(p_fileProfile);
                p_afterTargetLocales.add(p_targetLocale);
            }
        }
    }

    private void saveFileAsUTF8(File file)
    {
        String originalEncode = "";
        try
        {
            originalEncode = ImportUtil.guessEncodingByBom(file);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage(), e);
        }
        if (originalEncode == null && file.isFile())
        {
            logger.warn("Can not get the encoding of file: " + file
                    + ", please check whether the encoding of file: " + file + " is unicode.");
        }
        else
        {
            ImportUtil.saveFileAsUTF8(file.getAbsolutePath(), originalEncode);
        }
    }

    private void handleTargetLocales(Vector<String> targetLocales, String p_targetLocales,
            int fileSize, boolean isZipFileCreateJob)
    {
        if (StringUtil.isEmpty(p_targetLocales)
                || StringUtil.isEmpty(p_targetLocales.replace("|", ""))
                || StringUtil.isEmpty(p_targetLocales.replace(",", ""))
                || p_targetLocales.trim().equals("*"))
        {
            for (int i = 0; i < fileSize; i++)
            {
                targetLocales.add(" ");
            }
        }
        else
        {
            if (isZipFileCreateJob)
            {
                for (String tLocale : p_targetLocales.split(","))
                {
                    if (tLocale.trim().equals("*"))
                    {
                        targetLocales.add(" ");
                        break;
                    }
                    else
                    {
                        String locales = tLocale.trim() + ",";
                        if (locales != "" && locales.endsWith(","))
                        {
                            targetLocales.add(locales.substring(0, locales.lastIndexOf(",")));
                        }
                    }
                }
            }
            else
            {
                for (String tLocale : p_targetLocales.split("\\|"))
                {
                    if (tLocale.contains(","))
                    {
                        String locales = "";
                        for (String locale : tLocale.split(","))
                        {
                            locales += locale.trim() + ",";
                        }
                        if (locales != "" && locales.endsWith(","))
                        {
                            targetLocales.add(locales.substring(0, locales.lastIndexOf(",")));
                        }
                    }
                    else
                    {
                        if (tLocale.trim().equals("*"))
                        {
                            targetLocales.add(" ");
                        }
                        else
                        {
                            targetLocales.add(tLocale.trim());
                        }
                    }
                }
            }
        }
    }

    private String getRealPath(String jobId, String filePath, String srcLocale,
            boolean hasWebserviceInPath)
    {
        StringBuffer newPath = new StringBuffer();
        newPath.append(srcLocale);
        if (hasWebserviceInPath)
        {
            newPath.append(File.separator).append("webservice");
        }
        newPath.append(File.separator).append(jobId);
        newPath.append(File.separator).append(filePath);

        return newPath.toString();
    }

    private String findSrcLocale(String p_fileProfileId) throws RestWebServiceException
    {
        String errorMsg = null;
        String sourceLocale = null;

        try
        {
            long fpid = 0;
            try
            {
                fpid = Long.parseLong(p_fileProfileId);
            }
            catch (Exception e)
            {
                errorMsg = "The parameter fileProfileId is not numeric: " + p_fileProfileId;
                throw new Exception(errorMsg, e);
            }

            FileProfile fp = null;
            try
            {
                if (fpid != 0)
                {
                    FileProfilePersistenceManager fppm = ServerProxy
                            .getFileProfilePersistenceManager();
                    fp = fppm.readFileProfile(fpid);
                }
                if (fp == null)
                {
                    throw new Exception();
                }
            }
            catch (Exception e)
            {
                errorMsg = "Fail to get FileProfile by fileProfileId: " + fpid;
                throw new Exception(errorMsg, e);
            }

            long lpid = 0;
            try
            {
                if (fp != null)
                {
                    lpid = fp.getL10nProfileId();
                    ProjectHandler ph = ServerProxy.getProjectHandler();
                    L10nProfile lp = ph.getL10nProfile(lpid);
                    sourceLocale = lp.getSourceLocale().toString();
                }
            }
            catch (Exception e)
            {
                errorMsg = "Fail to get source locale by l10nProfile Id: " + lpid;
                throw new Exception(errorMsg, e);
            }

            return sourceLocale;
        }
        catch (Exception e)
        {
            throw new RestWebServiceException(makeErrorJson(UPLOAD_SOURCE_FILE, e.getMessage()));
        }
    }

    private boolean isInSameCompany(String p_userName, String p_companyId)
    {
        if (p_userName == null || p_userName.trim().equals(""))
            return false;
        if (p_companyId == null || p_companyId.trim().equals(""))
            return false;
        try
        {
            User user = ServerProxy.getUserManager().getUserByName(p_userName);
            String userCompanyId = ServerProxy.getJobHandler().getCompany(user.getCompanyName())
                    .getIdAsLong().toString();
            return userCompanyId.equals(p_companyId) ? true : false;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    private String checkIfCreateJobCalled(String methodName, long jobId, String jobName)
    {
        if (cachedJobIds.contains(jobId))
        {
            String message = "Current job (jobId:" + jobId + ";jobName:" + jobName
                    + ") is being created or has been created already.";
            return message;
        }
        return null;
    }

    private String checkExtensionExisted(String p_filePath)
    {
        if (p_filePath == null || p_filePath.trim().length() == 0)
            return "filePath is null or empty";

        try
        {
            String fileName = null;
            String path = p_filePath.replace("\\", "/");
            int index = path.lastIndexOf("/");
            if (index > -1)
            {
                fileName = path.substring(index + 1);
            }
            else
            {
                fileName = path;
            }

            index = fileName.lastIndexOf(".");
            if (index > -1)
            {
                String extension = fileName.substring(index + 1);
                if (extension != null && extension.trim().length() > 0)
                    return null;
            }
        }
        catch (Exception ignore)
        {
            return null;
        }

        return "The file " + p_filePath + " has no extension.";
    }

    /**
     * validate job name with "[\\w+-]{1,}" pattern (character, '+', '-')
     */
    private String validateJobName(String p_jobname)
    {
        if (StringUtil.isEmpty(p_jobname))
        {
            return "Empty job name.";
        }
        String name = p_jobname.trim();
        int index = name.lastIndexOf("_");
        if (index > -1)
        {
            name = name.substring(0, index);
        }
        if (name.length() > 100)
        {
            return "The length of job name exceeds 100 characters.";
        }

        if (!p_jobname.matches("[^\\\\/:;*?|\"<>&%]*"))
        {
            return ERROR_JOB_NAME;
        }

        return null;
    }

    /**
     * Releases the resource created to DB operations
     * 
     * @param results
     *            Resource for ResultSet object
     * @param query
     *            PreparedStatement object for querying
     * @param connection
     *            Database connection
     */
    private void releaseDBResource(ResultSet results, PreparedStatement query, Connection connection)
    {
        // close ResultSet
        if (results != null)
        {
            try
            {
                results.close();
            }
            catch (Exception e)
            {
                logger.error("Closing ResultSet", e);
            }
        }
        // close PreparedStatement
        if (query != null)
        {
            try
            {
                query.close();
            }
            catch (Exception e)
            {
                logger.error("Closing query", e);
            }
        }
        // close Connection
        try
        {
            ConnectionPool.returnConnection(connection);
        }
        catch (ConnectionPoolException e)
        {
            logger.error("Closing Connection", e);
        }
    }

    private void writeResultToLogFile(String[] p_messageArguments)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("\r\n");
        sb.append("Upload time: ");
        sb.append(p_messageArguments[0]);
        sb.append("\r\n");
        sb.append("Job name: ");
        sb.append(p_messageArguments[1]);
        sb.append("\r\n");
        sb.append("Job Description: ");
        sb.append(p_messageArguments[2]);
        sb.append("\r\n");
        sb.append("Uploaded by: ");
        sb.append(p_messageArguments[4]);
        sb.append("\r\n");
        sb.append("Uploaded files: \r\n");
        sb.append(p_messageArguments[5]);
        sb.append("\r\n");

        logger.info(sb.toString());
    }
    
    private boolean isSupportedZipFileFormat(File file)
    {
        String extension = CreateJobUtil.getFileExtension(file);
        if ("rar".equalsIgnoreCase(extension) || "zip".equalsIgnoreCase(extension)
                || "7z".equalsIgnoreCase(extension))
        {
            return true;
        }
        return false;
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
    
    private void addUploadFile(List<String> realUploadFileList, List<String> extensionList,
            File zipFile) throws Exception
    {
        String zipFileFullPath = zipFile.getPath();
        String zipFilePath = zipFileFullPath.substring(0,
                zipFileFullPath.indexOf(zipFile.getName()));

        List<net.lingala.zip4j.model.FileHeader> entriesInZip = CreateJobUtil
                .getFilesInZipFile(zipFile);

        for (net.lingala.zip4j.model.FileHeader entry : entriesInZip)
        {
            String zipEntryName = entry.getFileName();
            String fileExtension = zipEntryName.substring(zipEntryName.lastIndexOf(".") + 1);
            extensionList.add(fileExtension);
            /*
             * The unzipped files are in folders named by the zip file name
             */
            String unzippedFileFullPath = zipFilePath
                    + zipFile.getName().substring(0, zipFile.getName().lastIndexOf(".")) + "_"
                    + CreateJobUtil.getFileExtension(zipFile) + File.separator + zipEntryName;
            realUploadFileList.add(unzippedFileFullPath);
        }
    }
}