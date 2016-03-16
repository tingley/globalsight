/**
 * Copyright 2009 Welocalize, Inc.
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
package com.globalsight.webservices;

import javax.jws.WebService;

@WebService
public interface WebService4AEM
{

    public String login(String p_username, String p_password)
            throws WebServiceException;

    /**
     * Gets a unique job name.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * 
     * @return job name used in database.
     */
    public String getUniqueJobName(String p_accessToken, String p_jobName)
            throws WebServiceException;

    /**
     * Get all of the FileProfile information from GlobalSight side as Xml
     * string.
     * 
     * @param p_accessToken
     * @return String An XML description which contains all file profiles
     * @throws WebServiceException
     */
    public String getFileProfileInfoEx(String p_accessToken)
            throws WebServiceException;

    /**
     * Uploads a file to service
     * 
     * @param accessToken
     * @param jobName
     *            String Job name
     * @param filePath
     *            String Absolute Path of file
     * @param fileProfileId
     *            String ID of selected file profile
     * @param content
     *            byte[] Job content
     * @throws WebServiceException
     */
    public void uploadFile(String accessToken, String jobName, String filePath,
            String fileProfileId, byte[] content) throws WebServiceException;

    /**
     * 
     * @param args
     * @return jobId
     * @throws WebServiceException
     */
    public String uploadFileForInitial(WrapHashMap map)
            throws WebServiceException;

    /**
     * To create a job
     * 
     * @param accessToken
     * @param jobName
     *            String Job name
     * @param comment
     *            String Job comment
     * @param filePaths
     *            String Path of files which are contained in job, split by "|"
     * @param fileProfileIds
     *            String ID of file profiles, split by "|"
     * @param targetLocales
     *            String Target locales which like to be translated, split by
     *            "|"
     * @throws WebServiceException
     */
    public void createJob(String accessToken, String jobName, String comment,
            String filePaths, String fileProfileIds, String targetLocales)
                    throws WebServiceException;

    /**
     * check if some jobs is downloadable or delete them from backup file in
     * client
     * 
     * @param p_accessToken
     * @param p_message
     * @return xml String result <jobs> <job> <name>job name</name>
     *         <status>downloadable | create_error |
     *         unknown</status> </job> </jobs>
     * @throws WebServiceException
     */
    public String getDownloadableJobs(String p_accessToken, String p_msg)
            throws WebServiceException;

    /**
     * Return exported files information for job's "EXPORTED" state workflows.
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * @param workflowLocale
     *            Locale of workflow, it can accept fr_FR, fr-FR, fr_fr formats
     *            If it is null, all "EXPORTED" workflows' exported files info
     *            will be returned.
     * @return String String in XML format contains all exported files list of
     *         job according with special workflow locale
     * @throws WebServiceException
     */
    public String getJobExportWorkflowFiles(String p_accessToken,
            String p_jobName, String workflowLocale) throws WebServiceException;

    /**
     * Get job status by job name.
     * 
     * @param p_accessToken
     * @param p_jobName
     * @return
     * @throws WebServiceException
     */
    public String getStatus(String p_accessToken, String p_jobName)
            throws WebServiceException;

    /**
     * Get exported job files (do not care if the workflow is "EXPORTED")
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     *            Job name
     * @return String String in XML format contains information of exported job
     *         files
     * @throws WebServiceException
     */
    public String getJobExportFiles(String p_accessToken, String p_jobName)
            throws WebServiceException;

    /**
     * Returns number of jobs importing and number of workflows exporting.
     * 
     * @param p_accessToken
     * @return String An XML description which contains number of jobs importing
     *         and number of workflows exporting
     * @throws WebServiceException
     */
    public String getImportExportStatus(String p_accessToken)
            throws WebServiceException;

    /**
     * Adds a comment
     * <p>
     * 
     * @param p_accessToken
     *            - the accessToken received from login()
     * @param p_objectId
     *            - The id of the object (Job or Task) to add the comment too.
     *            The object must be DISPATCHED or part of a DISPATCHED job.
     * @param p_objectType
     *            - The type of the object that p_objectId refers to. 1 = JOB 3
     *            = TASK
     * @param p_userId
     *            - A valid user's user id that is adding the comment.
     * @param p_comment
     *            - A comment to add to the task.
     * @param p_file
     *            - A file which was attached.
     * @param p_fileName
     *            - file name of attached file.
     * @param p_access
     *            - access specification of attachment file Restricted = Only
     *            the Project Manager can view this file. General = All
     *            Participants of the Task can view this file.
     */
    public String addJobComment(String p_accessToken, String p_jobName,
            String p_userId, String p_comment, byte[] p_file, String p_fileName,
            String p_access) throws WebServiceException;

    /**
     * Returns general information about a job and its workflows.
     * 
     * @param p_accessToken
     * @param p_jobId
     *            String ID of Job
     * @return String An XML description which contains job information and its
     *         workflow information
     * @throws WebServiceException
     */
    public String getJobAndWorkflowInfo(String p_accessToken, long p_jobId)
            throws WebServiceException;

    /**
     * Cancels the job
     * 
     * @param p_accessToken
     * @param p_jobName
     *            String Job name
     * @return An XML description which contains canceling information
     * @throws WebServiceException
     */
    public String cancelJob(String p_accessToken, String p_jobName)
            throws WebServiceException;

    /**
     * Cancels the job and all of its workflow - job specified by its id.
     * 
     * @param p_accessToken
     * @param p_jobId
     *            String ID of job
     * @return An XML description which contains canceling information
     * @throws WebServiceException
     */
    public String cancelJobById(String p_accessToken, long p_jobId)
            throws WebServiceException;

    /**
     * Edit job detail info.
     * 
     * @param p_accessToken
     *            - The access token received from the login.
     * @param p_jobId
     *            - Job id is not empty and exist in GS server.
     * @param p_jobName
     *            - Job name can be empty.
     * @param p_estimatedDateXml
     *            - EstimatedDateXml can be empty. If not empty,example :
     *            <estimatedDates> <workflow> <targetLocale>zh_CN</targetLocale>
     *            <estimatedTranslateCompletionDate>yyyyMMdd
     *            HHmmss</estimatedTranslateCompletionDate>
     *            <estimatedWorkflowCompletionDate>yyyyMMdd
     *            HHmmss</estimatedWorkflowCompletionDate> </workflow>
     *            <workflow> ... ... </workflow> </estimatedDates>
     * @param p_priority
     *            Priority can be empty.If not empty,priority must be 1,2,3,4 or
     *            5;
     */
    public String editJobDetailInfo(String p_accessToken, String p_jobId,
            String p_jobName, String p_estimatedDateXml, String p_priority)
                    throws WebServiceException;

    /**
     * Get job translation percentage
     * 
     * @param p_jobId
     *            Job id can not empty.
     * @param p_targetLocales
     *            Target locale can be empty, can be one or more.
     * @return Return xml, for example: <?xml version="1.0" encoding="UTF-8" ?>
     *         <job> <id>280</id> <name>job_4012_861430940</name>
     *         <workflows> <workflow> <targetLocal>French (France)
     *         [fr_FR]</targetLocal>
     *         <workflowTranslationPercentage>3%</workflowTranslationPercentage>
     *         <targetPages> <targetPage> <pageName>en_US\280\
     *         Welocalize_Company_IncludingRepeat_Codesensitive .html</pageName>
     *         <pageTranslationPercentage>100%</pageTranslationPercentage>
     *         </targetPage> <targetPage> <pageName>en_US\280\
     *         Welocalize_Company_IncludingRepeat_Leverage Match
     *         Threshold.html</pageName>
     *         <pageTranslationPercentage>1%</pageTranslationPercentage>
     *         </targetPage> </targetPages> </workflow> </workflows> </job>
     */
    public String getTranslationPercentage(String p_accessToken, String p_jobId,
            String p_targetLocales) throws WebServiceException;

    /**
     * Get a link for in context review for specified task ID. User need not
     * logging in GlobalSight.
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param p_taskId
     *            -- task ID
     * @return A link like
     *         "http://10.10.215.20:8080/globalsight/ControlServlet?linkName=self&pageName=inctxrvED1&secret=E127B35E1A1C1B52C742353BBA176327D7F54956B373428134DE7252182EAA0D"
     *         .
     * 
     * @throws WebServiceException
     */
    public String getInContextReviewLink(String p_accessToken, String p_taskId)
            throws WebServiceException;

    /**
     * Create job group
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param groupName
     *            --can not be empty
     * @param projectName
     *            --can not be empty
     * @param sourceLocale
     *            --can not be empty,like "de_DE"
     * @return Create if succeed,return group name and id
     * 
     * @throws WebServiceException
     */

    public String createJobGroup(String p_accessToken, String groupName,
            String projectName, String sourceLocale) throws WebServiceException;

    /**
     * Add job to group
     * 
     * @param p_accessToken
     *            -- login user's token
     * @param groupId
     *            --can not be empty
     * @param jobId
     *            --can not be empty,like "126" or "126,127,128"
     * 
     * @return Returns true if successful
     * 
     * @throws WebServiceException
     */
    public String addJobToGroup(String p_accessToken, String groupId,
            String jobId) throws WebServiceException;

    /**
     * Judge if there is workflow defined for specified source-target locales in
     * specified file profile.
     * 
     * @param p_accessToken
     *            String
     * @param p_fileProfileId
     * @param p_srcLangCountry
     * @param p_trgLangCountry
     * @return "yes" or "no"
     * @throws WebServiceException
     */
    public String isSupportCurrentLocalePair(String p_accessToken,
            String p_fileProfileId, String p_srcLangCountry,
            String p_trgLangCountry) throws WebServiceException;

    /**
     * To dispatch workflows. Each workflow can be dispatched only when its
     * state is READY_TO_BE_DISPATCHED
     * 
     * @param p_accessToken
     *            Access token
     * @param p_wfIds
     *            String of one or more workflow IDs Using "," to split more
     *            workflow IDs
     * @return If success, then return null. If fail, return error message
     * @throws WebServiceException
     */
    public String dispatchWorkflow(String p_accessToken, String p_wfIds)
            throws WebServiceException;

    /**
     * To dispatch job
     * 
     * @param p_accessToken
     *            Access token
     * @param p_jobName
     * 
     * @return If success, then return null. If fail, return error message
     * @throws WebServiceException
     */
    public String dispatchJob(String p_accessToken, String jobName)
            throws WebServiceException;

    /**
     * Get server version such as 7.1.7.2. For GS edition feature,it need to be
     * run on 7.1.7.2 or upper servers.
     * 
     * @param p_accessToken
     * @return
     * @throws WebServiceException
     */
    public String getServerVersion(String p_accessToken)
            throws WebServiceException;
    
    public String getJobNameById(String p_accessToken, long jobId)
            throws WebServiceException;
}
