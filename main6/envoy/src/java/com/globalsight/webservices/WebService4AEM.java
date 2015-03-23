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
public interface WebService4AEM {

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
	public String uploadFileForInitial(WrapHashMap map) throws WebServiceException;

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
	 *         <status>downloadable | create_error | unknown</status> </job>
	 *         </jobs>
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
	public String getJobStatus(String p_accessToken, String p_jobName)
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
            String p_userId, String p_comment, byte[] p_file,
            String p_fileName, String p_access) throws WebServiceException;
}
