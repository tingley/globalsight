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
package com.globalsight.everest.jobhandler;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import com.globalsight.everest.company.Category;
import com.globalsight.everest.company.Company;
import com.globalsight.everest.company.PostReviewCategory;
import com.globalsight.everest.company.ScorecardCategory;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.workflow.Activity;

/**
 * JobHandler is an interface used for handling and delegating job related
 * processes.
 */
public interface JobHandler
{
    public static final String SERVICE_NAME = "JobHandlerServer";

    /**
     * Cancel the entire job and all its workflow. This is used by the server
     * and not the GUI so no user id or session id is passed in.
     * 
     * @param p_job
     *            - The job to cancel
     */
    public void cancelJob(Job p_job) throws RemoteException, JobException;

    /**
     * Cancel the entire job and all its workflow. This is used by the server
     * and not the GUI so no user id or session id is passed in.
     * 
     * @param p_job
     *            - The job to cancel
     * @param p_boolean
     *            - from reimport?
     */
    public void cancelJob(Job p_job, boolean p_reimport)
            throws RemoteException, JobException;

    /**
     * Cancel the entire job and all its workflows. This is used by the
     * server/API and not the GUI so no session id is passed in.
     * 
     * @param p_jobId
     *            - The id of the job to cancel.
     */
    public void cancelJob(long p_jobId) throws RemoteException, JobException;

    /**
     * Cancel all the job's workflows that have the given state. Then, if all
     * workflows have been cancelled, cancel the job too.
     * <p>
     * 
     * @param p_idOfUserRequestingCancel
     * @param p_jobId
     *            The specified job id.
     * @param p_state
     *            the workflow state to cancel. If NULL then cancel all.
     */
    public void cancelJob(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws RemoteException, JobException;

    public void dispatchJob(Job p_job) throws RemoteException, JobException;

    public void archiveJob(Job p_job) throws RemoteException, JobException;

    /**
     * Create a new activity.
     * <p>
     * 
     * @param p_activity
     *            - The activity to be created.
     * 
     * @return The created activity with id updated.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Activity createActivity(Activity p_activity) throws RemoteException,
            JobException;

    /**
     * Get the activity with the specified name.
     * <p>
     * 
     * @param p_activityName
     *            - The name of the activity to find.
     */
    public Activity getActivity(String p_activityName) throws RemoteException,
            JobException;

    /**
     * Get the activity with the specified name.
     * <p>
     * 
     * @param p_activityName
     *            - The name of the activity to find.
     */
    public Activity getActivityByDisplayName(String p_activityName)
            throws RemoteException, JobException;

    /**
     * Get the activity with the specified name and company id.
     * <p>
     * 
     * @param p_activityName
     *            - The name of the activity to find.
     * @param p_companyId
     *            - The id of the company this activity belongs to.
     */
    public Activity getActivityByCompanyId(String p_activityName,
            String p_companyId) throws RemoteException, JobException;

    /**
     * Get a list of all existing activities in the system.
     * <p>
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Collection<Activity> getAllActivities() throws RemoteException,
            JobException;

    /**
     * Get a list of all existing activities in the system.
     * <p>
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Collection getAllActivitiesByCompanyId(String p_companyId)
            throws RemoteException, JobException;

    /**
     * Create a new company.
     * <p>
     * 
     * @param p_company
     *            - The company to be created.
     * 
     * @return The created company with id updated.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Company createCompany(Company p_company, String p_userId)
            throws RemoteException, JobException;

    /**
     * Create a company category.
     * 
     * @param category
     *            the category to be created.
     * @throws JobException
     *             Component related exception.
     */
    public void createCategory(Category category) throws JobException;
    
    /**
     * Create a scorecard category.
     * 
     * @param category
     *            the category to be created.
     * @throws JobException
     *             Component related exception.
     */
    public void createScorecardCategory(ScorecardCategory scorecardCategory) throws JobException;

    /**
     * Create a post review category.
     * 
     * @param postReviewCategory
     *            the category to be created.
     * @throws JobException
     *             Component related exception.
     */
    public void createPostReviewCategory(
            PostReviewCategory postReviewCategory) throws JobException;

    /**
     * Get the company with the specified name.
     * <p>
     * 
     * @param p_companyName
     *            - The name of the company to find.
     */
    public Company getCompany(String p_companyName) throws RemoteException,
            JobException;

    /**
     * Get the company with the specified id.
     * <p>
     * 
     * @param p_companyName
     *            - The id of the company to find.
     */
    public Company getCompanyById(long p_companyId) throws RemoteException,
            JobException;

    public Collection getAllDtpActivities() throws RemoteException,
            JobException;

    public Collection getAllTransActivities() throws RemoteException,
            JobException;

    /**
     * Get a list of all existing companies in the system.
     * <p>
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Collection getAllCompanies() throws RemoteException, JobException;

    /**
     * Remove an company.
     * <p>
     * 
     * @param p_company
     *            - The company to be removed.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public void removeCompany(Company p_company) throws RemoteException,
            JobException;

    /**
     * Modify an company's description.
     * <p>
     * 
     * @param p_company
     *            - The company to be updated.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public void modifyCompany(Company p_company) throws RemoteException,
            JobException;

    /**
     * Gets a collection of Jobs based on the search params
     * 
     * @param p_searchParams
     * @return Collection of jobs
     * @exception RemoteException
     * @exception JobException
     */
    public Collection<JobImpl> getJobs(JobSearchParameters p_searchParams)
            throws RemoteException, JobException;

    /**
     * Get the job object specified by its job id.
     * <p>
     * 
     * @param p_jobId
     *            The specified job id.
     * @return The job object.
     */
    public Job getJobById(long p_jobId) throws RemoteException, JobException;

    public Job refreshJob(Job job) throws JobException;

    /**
     * Get a collection of jobs specified by the state.
     * <p>
     * 
     * @param p_state
     *            The state interested in.
     * @return Collection of job objects.
     */
    public Collection<JobImpl> getJobsByState(String p_state)
            throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByState(String p_state, String userId)
            throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByRate(String p_rateId)
            throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByStateList(Vector<String> p_listOfStates)
            throws RemoteException, JobException;

    /**
     * Get a list of jobs based on the given list of states. The query could
     * optionally be limited in terms of job creation date (i.e. from the past
     * 30 days or any value specified in envoy.properties).
     * 
     * @param p_listOfStates
     *            - A list of valid job states.
     * @param p_queryLimitByDate
     *            - True if the query should be limited.
     * 
     * @return A collection of jobs in the given states.
     */
    public Collection<JobImpl> getJobsByStateList(
            Vector<String> p_listOfStates, boolean p_queryLimitByDate)
            throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByStateList(String p_httpSessionId,
            Vector<String> p_listOfStates) throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByManagerId(String p_managerId)
            throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByManagerIdAndState(String p_managerId,
            String p_state) throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByManagerIdAndStateList(
            String p_managerId, Vector<String> p_listOfStates)
            throws RemoteException, JobException;

    /**
     * Get a collection of Jobs for a Workflow Manager (who assists PM for
     * workflow modifications.
     * 
     * @param p_wfManagerId
     *            - The user name of the workflow manager.
     * @param p_listOfStates
     *            - A list of job states for query purposes.
     * @return A collection of jobs where the workflow manager can perform
     *         actions on behalf of the PM (only on Workflows that they are
     *         assigned to).
     * 
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public Collection<JobImpl> getJobsByWfManagerIdAndStateList(
            String p_wfManagerId, Vector<String> p_listOfStates)
            throws RemoteException, JobException;

    public Collection<JobImpl> getJobsByProjectManager(String p_userId,
            String p_state) throws RemoteException, JobException;

    public L10nProfile getL10nProfileByJobId(long p_jobId)
            throws RemoteException, JobException;

    /**
     * Return all the the source pages associated with the specified job id.
     */
    public Collection getSourcePageByJobId(long p_jobId)
            throws RemoteException, JobException;

    /**
     * Returns all the source pages associated with the specified job id and
     * contain a primary file of the specified type.
     * 
     * @see PrimaryFile for the valid types.
     */
    public Collection getSourcePagesByTypeAndJobId(int p_primaryFileType,
            long p_jobId) throws RemoteException, JobException;

    public Collection getRequestListByJobId(long p_jobId)
            throws RemoteException, JobException;

    /**
     * Find the job that contains the specific source page.
     * 
     * @param p_sourcePageId
     *            - the unique identifier of the source page.
     * 
     * @return Either the job that contains that source page or NULL if a job
     *         doesn't contain that page.
     */
    public Job findJobOfPage(long p_sourcePageId) throws RemoteException,
            JobException;

    /**
     * Cancel all the error pages in an IMPORT_FAIL job, so releases the job to
     * be dispatched or accept more requests.
     * <p>
     * 
     * @param p_idOfUserRequestingCancel
     * @param p_job
     *            The job to cancel the error pages from.
     */
    public void cancelImportErrorPages(String p_idOfUserRequestingCancel,
            Job p_job) throws RemoteException, JobException;

    /**
     * Update the number of pages there are in a job.
     * 
     * @param p_job
     *            The job to update the page count of.
     * @param p_pageCount
     *            The new number of pages.
     * @return The job updated with the new number of pages.
     */
    public Job updatePageCount(Job p_job, int p_pageCount)
            throws RemoteException, JobException;

    /**
     * Update the date of quotation email in a job.
     * 
     * @param p_job
     *            The job to update the quotation email date of.
     * @param p_quoteDate
     *            The new date of quotation email.
     */
    public void updateQuoteDate(Job p_job, String p_quoteDate)
            throws RemoteException, JobException;

    /**
     * Update the date of Approved quote date in a job.
     * 
     * @param p_job
     *            The job to update the Approved Quotedate of.
     * @param p_quoteApprovedDate
     *            The new date of Approved Quote.
     */
    public void updateQuoteApprovedDate(Job p_job, String p_quoteApprovedDate)
            throws RemoteException, JobException;

    /**
     * Update the PO Number of quote in a job.
     * 
     * @param p_job
     *            The job to update the PO Number of Quote.
     * @param p_quotePoNumber
     *            The PO Number of Quote.
     */
    public void updateQuotePoNumber(Job p_job, String p_quotePoNumber)
            throws RemoteException, JobException;

    /**
     * Update the user who approved the cost of a job
     * 
     * @param p_job
     * @param user
     * @throws RemoteException
     * @throws JobException
     */
    public void updateAuthoriserUser(Job p_job, User user)
            throws RemoteException, JobException;

    /**
     * Overrides the total word count (source page word count) for the job. This
     * is an overriden word count and is not necessarily the total of all the
     * source page word counts.
     * 
     * @param p_job
     *            The job to override/ update the word count of.
     * @param p_wordCount
     *            The word count amount that overrides the calculated word
     *            count.
     */
    public Job overrideWordCount(Job p_job, int p_wordCount)
            throws RemoteException, JobException;

    /**
     * Removes the overriden word count on a job. Not the total word count will
     * be calculated from all the source pages in the job.
     * 
     * @param p_job
     *            The job to remove the overriden word count from.
     */
    public Job clearOverridenWordCount(Job p_job) throws RemoteException,
            JobException;

    /**
     * Modify an activity's description.
     * <p>
     * 
     * @param p_activity
     *            - The activity to be updated.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public void modifyActivity(Activity p_activity) throws RemoteException,
            JobException;

    /**
     * Remove an existing activity.
     * <p>
     * 
     * @param p_activity
     *            - The activity to be removed.
     * @exception java.rmi.RemoteException
     *                Network related exception.
     * @exception JobException
     *                Component related exception.
     */
    public void removeActivity(Activity p_activity) throws RemoteException,
            JobException;

    /**
     * Get the job object specified by its job name.
     * 
     * @param p_jobName
     *            : The specified job name.
     * @return The job object
     * @throws RemoteException
     * @throws JobException
     */
    public Job getJobByJobName(String p_jobName) throws RemoteException,
            JobException;

    /**
     * Get job IDs with special range of fetching records
     * 
     * @param p_companyId
     *            Company ID
     * @param p_offset
     *            Begin index of records
     * @param p_count
     *            Count number of fetching records
     * @return
     * @throws RemoteException
     * @throws JobException
     * @author Vincent Yan, 2011/01/12
     */
	public String[] getJobIdsByCompany(String p_companyId, int p_offset,
			int p_count, boolean p_isDescOrder, String currentUserId)
			throws RemoteException, JobException;

    /**
     * Get job IDs with special job state and range of fetching records
     * 
     * @param p_companyId
     *            Company ID
     * @param p_state
     *            State of job
     * @param p_offset
     *            Begin index of records
     * @param p_count
     *            Count number of fetching records
     * @return
     * @throws RemoteException
     * @throws JobException
     * @author Vincent Yan, 2011/01/12
     */
	public String[] getJobIdsByState(String p_companyId, String p_state,
			int p_offset, int p_count, boolean p_isDescOrder,
			String currentUserId) throws RemoteException, JobException;
    
    /**
     * Get job IDs with special creator userName and range of fetching records
     * 
     * @param p_companyId
     *            Company ID
     * @param p_creatorUserName
     *            creator's userName of job
     * @param p_offset
     *            Begin index of records
     * @param p_count
     *            Count number of fetching records
     * @return
     * @throws RemoteException
     * @throws JobException
     */
	public String[] getJobIdsByCreator(long p_companyId,
			String p_creatorUserId, int p_offset, int p_count,
			boolean p_isDescOrder, String currentUserId)
			throws RemoteException, JobException;

    /**
     * Get counts of every job state with speical company id
     * 
     * @param p_companyId
     *            Company Id
     * @return HashMap<String, Integer> Counts of every job state
     * @throws RemoteException
     * @throws JobException
     * @author Vincent Yan, 2011/01/17
     */
    public HashMap<String, Integer> getCountsByJobState(String p_companyId)
            throws RemoteException, JobException;

    /**
     * Get Jobs by userId and stateList.
     */
    public Collection<JobImpl> getJobsByUserIdAndState(String p_userId,
            Vector<String> p_listOfStates) throws RemoteException, JobException;
}
