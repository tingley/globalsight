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

// java
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
import com.globalsight.everest.util.system.RemoteServer;
import com.globalsight.everest.workflow.Activity;

public class JobHandlerWLRMIImpl extends RemoteServer implements
        JobHandlerWLRemote
{
    JobHandler m_localReference;

    public JobHandlerWLRMIImpl() throws RemoteException
    {
        super(JobHandler.SERVICE_NAME);
        m_localReference = new JobHandlerLocal();
    }

    public Object getLocalReference()
    {
        return m_localReference;
    }

    public void cancelJob(Job p_job) throws RemoteException, JobException
    {
        m_localReference.cancelJob(p_job);
    }

    public void cancelJob(Job p_job, boolean p_reimport)
            throws RemoteException, JobException
    {
        m_localReference.cancelJob(p_job, p_reimport);
    }

    public void cancelJob(long p_jobId) throws RemoteException, JobException
    {
        m_localReference.cancelJob(p_jobId);
    }

    public void cancelJob(String p_idOfUserRequestingCancel, Job p_job,
            String p_state) throws RemoteException, JobException
    {
        m_localReference.cancelJob(p_idOfUserRequestingCancel, p_job, p_state);
    }

    public void dispatchJob(Job p_job) throws RemoteException, JobException
    {
        m_localReference.dispatchJob(p_job);
    }

    public void cancelImportErrorPages(String p_idOfUserRequestingCancel,
            Job p_job) throws RemoteException, JobException
    {
        m_localReference.cancelImportErrorPages(p_idOfUserRequestingCancel,
                p_job);
    }

    public void archiveJob(Job p_job) throws RemoteException, JobException
    {
        m_localReference.archiveJob(p_job);
    }

    public Activity createActivity(Activity param1) throws RemoteException,
            JobException
    {
        return m_localReference.createActivity(param1);
    }

    public Activity getActivity(String p_activityName) throws RemoteException,
            JobException
    {
        return m_localReference.getActivity(p_activityName);
    }

    public Activity getActivityByDisplayName(String p_activityName)
            throws RemoteException, JobException
    {
        return m_localReference.getActivityByDisplayName(p_activityName);
    }

    public Activity getActivityByCompanyId(String p_activityName,
            String p_companyId) throws RemoteException, JobException
    {
        return m_localReference.getActivityByCompanyId(p_activityName,
                p_companyId);
    }

    public Collection getAllActivities() throws RemoteException, JobException
    {
        return m_localReference.getAllActivities();
    }

    public Collection getAllActivitiesByCompanyId(String p_companyId)
            throws RemoteException, JobException
    {
        return m_localReference.getAllActivitiesByCompanyId(p_companyId);
    }

    public Collection getAllCompanies() throws RemoteException, JobException
    {
        return m_localReference.getAllCompanies();
    }

    public Company getCompany(String p_companyName) throws RemoteException,
            JobException
    {
        return m_localReference.getCompany(p_companyName);
    }

    public Company getCompanyById(long p_companyId) throws RemoteException,
            JobException
    {
        return m_localReference.getCompanyById(p_companyId);
    }

    public Company createCompany(Company p_company, String p_userId)
            throws RemoteException, JobException
    {
        return m_localReference.createCompany(p_company, p_userId);
    }

    public void createCategory(Category category) throws JobException
    {
        m_localReference.createCategory(category);
    }
    
    public void createScorecardCategory(ScorecardCategory scorecardCategory) throws JobException
    {
        m_localReference.createScorecardCategory(scorecardCategory);
    }
    
    public void createPostReviewCategory(PostReviewCategory postReviewCategory) throws JobException
    {
        m_localReference.createPostReviewCategory(postReviewCategory);
    }
    
    public void modifyCompany(Company p_company) throws RemoteException,
            JobException
    {
        m_localReference.modifyCompany(p_company);
    }

    public void removeCompany(Company p_company) throws RemoteException,
            JobException
    {
        m_localReference.removeCompany(p_company);
    }

    public Collection getAllTransActivities() throws RemoteException,
            JobException
    {
        return m_localReference.getAllTransActivities();
    }

    public Collection getAllDtpActivities() throws RemoteException,
            JobException
    {
        return m_localReference.getAllDtpActivities();
    }

    public Job getJobById(long p_jobId) throws RemoteException, JobException
    {
        return m_localReference.getJobById(p_jobId);
    }

    public Job refreshJob(Job job) throws JobException
    {
        return m_localReference.refreshJob(job);
    }

    public Collection getJobs(JobSearchParameters p_searchParams)
            throws RemoteException, JobException
    {
        return m_localReference.getJobs(p_searchParams);
    }

    public Collection getJobsByState(String param1) throws RemoteException,
            JobException
    {
        return m_localReference.getJobsByState(param1);
    }

    public Collection getJobsByRate(String param1) throws RemoteException,
            JobException
    {
        return m_localReference.getJobsByRate(param1);
    }

    public Collection getJobsByStateList(Vector p_listOfStates)
            throws RemoteException, JobException
    {
        return m_localReference.getJobsByStateList(p_listOfStates);
    }

    /**
     * @see JobHandler.getJobsByStateList(Vector, boolean)
     */
    public Collection getJobsByStateList(Vector p_listOfStates,
            boolean p_queryLimitByDate) throws RemoteException, JobException
    {
        return m_localReference.getJobsByStateList(p_listOfStates,
                p_queryLimitByDate);
    }

    public Collection getJobsByStateList(String p_httpSessionId,
            Vector p_listOfStates) throws RemoteException, JobException
    {
        return m_localReference.getJobsByStateList(p_httpSessionId,
                p_listOfStates);
    }

    public Collection getJobsByManagerId(String p_managerId)
            throws RemoteException, JobException
    {
        return m_localReference.getJobsByManagerId(p_managerId);
    }

    public Collection getJobsByManagerIdAndState(String p_managerId,
            String p_state) throws RemoteException, JobException
    {
        return m_localReference
                .getJobsByManagerIdAndState(p_managerId, p_state);
    }

    public Collection getJobsByManagerIdAndStateList(String p_managerId,
            Vector p_listOfStates) throws RemoteException, JobException
    {
        return m_localReference.getJobsByManagerIdAndStateList(p_managerId,
                p_listOfStates);
    }

    public Collection getJobsByWfManagerIdAndStateList(String p_wfManagerId,
            Vector p_listOfStates) throws RemoteException, JobException
    {
        return m_localReference.getJobsByWfManagerIdAndStateList(p_wfManagerId,
                p_listOfStates);
    }

    public Collection getJobsByProjectManager(String param1, String param2)
            throws RemoteException, JobException
    {
        return m_localReference.getJobsByProjectManager(param1, param2);
    }

    public L10nProfile getL10nProfileByJobId(long p_jobId)
            throws RemoteException, JobException
    {
        return m_localReference.getL10nProfileByJobId(p_jobId);
    }

    public Collection getRequestListByJobId(long p_jobId)
            throws RemoteException, JobException
    {
        return m_localReference.getRequestListByJobId(p_jobId);
    }

    public Collection getSourcePageByJobId(long p_jobId)
            throws RemoteException, JobException
    {
        return m_localReference.getSourcePageByJobId(p_jobId);
    }

    /**
     * @see JobHandler.getSourcePagesByTypeAndJobId(int, long)
     */
    public Collection getSourcePagesByTypeAndJobId(int p_primaryFileType,
            long p_jobId) throws RemoteException, JobException
    {
        return m_localReference.getSourcePagesByTypeAndJobId(p_primaryFileType,
                p_jobId);
    }

    public Job findJobOfPage(long p_sourcePageId) throws RemoteException,
            JobException
    {
        return m_localReference.findJobOfPage(p_sourcePageId);
    }

    public Job updatePageCount(Job p_job, int p_pageCount)
            throws RemoteException, JobException
    {
        return m_localReference.updatePageCount(p_job, p_pageCount);
    }

    /**
     * @see JobHandler.updateQuoteDate(Job, String)
     */
    public void updateQuoteDate(Job p_job, String p_quoteDate)
            throws RemoteException, JobException
    {
        m_localReference.updateQuoteDate(p_job, p_quoteDate);
    }

    // For "Quote process webEx" issue

    /**
     * Update Approve quote date for job.
     * 
     * @param p_job
     *            The job to update.
     * @param p_quoteApprovedDate
     *            The new date of Approve Quote.
     */
    public void updateQuoteApprovedDate(Job p_job, String p_quoteApprovedDate)
            throws RemoteException, JobException
    {
        m_localReference.updateQuoteApprovedDate(p_job, p_quoteApprovedDate);
    }

    /**
     * Update the quote PO Number
     * 
     * @param p_job
     *            The job to update.
     * @param p_quotePoNumber
     *            The new PO number.
     */
    public void updateQuotePoNumber(Job p_job, String p_quotePoNumber)
            throws RemoteException, JobException
    {
        m_localReference.updateQuotePoNumber(p_job, p_quotePoNumber);
    }

    /**
     * @see JobHandler.overrideWordCount(Job, int)
     */
    public Job overrideWordCount(Job p_job, int p_wordCount)
            throws RemoteException, JobException
    {
        return m_localReference.overrideWordCount(p_job, p_wordCount);
    }

    /**
     * @see JobHandler.clearOverridenWordCount(Job)
     */
    public Job clearOverridenWordCount(Job p_job) throws RemoteException,
            JobException
    {
        return m_localReference.clearOverridenWordCount(p_job);
    }

    public void modifyActivity(Activity param1) throws RemoteException,
            JobException
    {
        m_localReference.modifyActivity(param1);
    }

    public void removeActivity(Activity param1) throws RemoteException,
            JobException
    {
        m_localReference.removeActivity(param1);
    }

    public void updateAuthoriserUser(Job p_job, User user)
            throws RemoteException, JobException
    {
        m_localReference.updateAuthoriserUser(p_job, user);
    }

    public Job getJobByJobName(String p_jobName) throws RemoteException,
            JobException
    {
        return m_localReference.getJobByJobName(p_jobName);
    }

	@Override
	public String[] getJobIdsByCompany(String p_companyId, int p_offset,
			int p_count, boolean p_isDescOrder, String currentUserId)
			throws RemoteException, JobException
	{
		return m_localReference.getJobIdsByCompany(p_companyId, p_offset,
				p_count, p_isDescOrder, currentUserId);
	}

    @Override
	public String[] getJobIdsByState(String p_companyId, String p_state,
			int p_offset, int p_count, boolean p_isDescOrder,
			String currentUserId) throws RemoteException, JobException
	{
		return m_localReference.getJobIdsByState(p_companyId, p_state,
				p_offset, p_count, p_isDescOrder, currentUserId);
	}
    
    @Override
    public String[] getJobIdsByCreator(long p_companyId, String p_creatorUserId,
            int p_offset, int p_count, boolean p_isDescOrder,String currentUserId)
            throws RemoteException, JobException
    {
        return m_localReference.getJobIdsByCreator(p_companyId, p_creatorUserId,
                p_offset, p_count, p_isDescOrder,currentUserId);
    }

    public HashMap<String, Integer> getCountsByJobState(String p_companyId)
            throws RemoteException, JobException
    {
        return m_localReference.getCountsByJobState(p_companyId);
    }

    /**
     * Get Jobs by userId and stateList.
     */
    public Collection<JobImpl> getJobsByUserIdAndState(String p_userId,
            Vector<String> p_listOfStates) throws RemoteException, JobException
    {
        return m_localReference.getJobsByUserIdAndState(p_userId,
                p_listOfStates);
    }
    
    public Collection<JobImpl> getJobsByState(String p_state, String userId)
            throws RemoteException, JobException
    {
        return m_localReference.getJobsByState(p_state, userId);
    }
}
