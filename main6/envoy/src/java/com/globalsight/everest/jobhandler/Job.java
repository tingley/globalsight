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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.WorkflowRequest;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

public interface Job extends WorkObject
{
    // Job states
    public static final String AUTOMATIC = "AUTOMATIC";
    public static final String MANUAL = "MANUAL";
    public static final String BATCHRESERVED = "BATCH_RESERVED";
    public static final String CANCELLED = "CANCELLED";
    public static final String DISPATCHED = "DISPATCHED";
    public static final String DTPINPROGRESS = "DTPINPROGRESS";
    public static final String EXPORTED = "EXPORTED";
    public static final String EXPORT_FAIL = "EXPORT_FAILED";
    public static final String LOCALIZED = "LOCALIZED";
    public static final String PENDING = "PENDING";
    public static final String IMPORTFAILED = "IMPORT_FAILED";
    public static final String READY_TO_BE_DISPATCHED = "READY_TO_BE_DISPATCHED";
    public static final String ARCHIVED = "ARCHIVED";
    public static final String ALLSTATUS = "ALL_STATUS";
    public static final String IN_CONTEXT = "in-context";
    public static final String EXACT_ONLY = "exact";
    public static final String ADD_FILE = "ADDING_FILES";
    public static final String DELETE_FILE = "DELETING_FILES";
    public static final String UPLOADING = "UPLOADING";
    public static final String IN_QUEUE = "IN_QUEUE";
    public static final String EXTRACTING = "EXTRACTING";
    public static final String LEVERAGING = "LEVERAGING";
    public static final String CALCULATING_WORD_COUNTS = "CALCULATING-WORD-COUNTS";
    public static final String PROCESSING = "PROCESSING";
    public static final String EXPORTING = "EXPORTING";
    public static final String SKIPPING = "SKIPPING";

    public static final String JOB_TYPE_BLAISE = "blaise";
    public static final String JOB_TYPE_GIT = "git";
    public static final String JOB_TYPE_CVS = "cvs";
    public static final String JOB_TYPE_MINDTOUCH = "mindtouch";
    public static final String JOB_TYPE_ELOQUA  = "eloqua";
    public static final String JOB_TYPE_COTI = "coti";
    public static final String JOB_TYPE_RSS = "rss";

    @SuppressWarnings("serial")
    public static final List<String> ALLSTATUSLIST = new ArrayList<String>()
    {
        {
            add(PENDING);
            add(BATCHRESERVED);
            add(IMPORTFAILED);
            add(READY_TO_BE_DISPATCHED);
            add(DISPATCHED);
            add(LOCALIZED);
            add(EXPORTING);
            add(SKIPPING);
            add(EXPORTED);
            add(EXPORT_FAIL);
            add(ARCHIVED);
            add(ADD_FILE);
            add(PROCESSING);
            add(CALCULATING_WORD_COUNTS);
        }
    };

    @SuppressWarnings("serial")
    public static final List<String> GRAY_STATUS_LIST = new ArrayList<String>()
    {
        {
            add(UPLOADING);
            add(IN_QUEUE);
            add(EXTRACTING);
            add(LEVERAGING);
        }
    };

    @SuppressWarnings("serial")
    public static final List<String> PENDING_STATUS_LIST = new ArrayList<String>()
    {
        {
            add(PENDING);
            add(BATCHRESERVED);
            add(IMPORTFAILED);
            add(ADD_FILE);
        }
    };

    public long getId();

    public long getJobId(); // could be deprecated - just calls getId() -

    public void setCompanyId(long companyId);

    public long getCompanyId();
    
    public Long getGroupId();
    
    public void setGroupId(Long groupId);
    
    public void setJobName(String p_jobName);

    public String getJobName();

    // get the priority of the job - this is first assigned from the
    // L10nProfile that is associated with the job.
    public int getPriority();

    public void setPriority(int p_priority);

    public void addRequest(Request p_request);

    public void removeRequest(Request p_request);

    public void addWorkflowRequest(WorkflowRequest p_workflowRequest);

    public void setState(String p_state);

    public String getState();

    public String getDisplayState();

    public String getDisplayStateByLocale(Locale locale);

    public void setWordCountReached(boolean p_isWordCountReached);

    public boolean isWordCountReached();

    public void setSourceLocale(GlobalSightLocale p_sourceLocale);

    public GlobalSightLocale getSourceLocale();

    public void setCreateDate(Date p_createDate);

    public Date getCreateDate();

    /*
     * Returns the due date of the job - ie. the latest due date of its
     * workflows.
     * 
     * @return Date The due date of the job or NULL if the date can't be
     * calculated yet (the job must be dispatched).
     */
    public Date getDueDate();

    /*
     * Returns the duration as number of days.
     */
    public long getDuration();

    public int getWordCount();

    /**
     * Overrides the calculated word count with the value specified.
     */
    public void overrideWordCount(int p_wc);

    /**
     * Remove the overriden word count. The word count will be calculated again
     * from all the source page word counts.
     */
    public void clearOverridenWordCount();

    /**
     * Returns 'true' if the word count has been overriden 'false' if it hasn't
     * and is still being calculated
     */
    public boolean isWordCountOverriden();

    public String getDispatchType();

    public void addWorkflowInstance(Workflow p_wfInstance);

    public Collection<Workflow> getWorkflows();

    // returns 'true' if the job contains at least one failed workflow
    // returns 'false' if none of them are failed
    public boolean containsFailedWorkflow();

    public Collection<Request> getRequestList();

    public Collection getWorkflowRequestList();

    /**
     * Return all the source pages in the job.
     */
    public Collection getSourcePages();

    /**
     * Return all the source pages in the job that contain the specified primary
     * file type.
     * 
     * @see PrimaryFile for the valid types
     */
    public Collection getSourcePages(int p_primaryFileType);

    public long getL10nProfileId();

    public L10nProfile getL10nProfile();

    public FileProfile getFileProfile();

    public ArrayList getAllFileProfiles();

    public String getDataSourceName();

    /*
     * Returns the number of pages in the job. This defaults to the number of
     * requests but can be changed by the user.
     */
    public int getPageCount();

    /**
     * Add this Job comment to the collection.
     * 
     * @param p_comment
     *            - The comment to be added.
     */
    public void addJobComment(Comment p_comment);

    /**
     * Remove this job comment from the collection.
     * 
     * @param p_comment
     *            - The comment remove.
     */
    public void removeJobComment(Comment p_comment);

    /**
     * Set the job comments to be this value.
     * 
     * @param p_comments
     *            - The task comments to be set.
     */
    public void setJobComments(List p_comments);

    /**
     * Get the list of job comments.
     * 
     * @return a List of Comments for this task.
     */
    public List getJobComments();

    /**
     * Get a job comment based on comment id
     * 
     * @return a Comment for this job.
     */
    public Comment getJobComment(long commentId);

    /**
     * Sets the number of pages in a job. This defaults to the number of
     * requests, but can be overriden.
     */
    public void setPageCount(int p_numOfPages);

    /**
     * Get the leverage match threshold for this job. The value is the original
     * TM Profile's leverage match threshold from when this job was created.
     * 
     * @return The leverage match threshold that was originally set during
     *         import.
     */
    int getLeverageMatchThreshold();

    /**
     * Set the leverage match threshold to be the specified value.
     * 
     * @param p_leverageMatchThreshold
     *            The value set from the TM Profile's during the import process.
     */
    void setLeverageMatchThreshold(int p_leverageMatchThreshold);

    /**
     * Set the quotation email date of this job.
     * <p>
     * 
     * @param p_quoteDate
     *            The quotation email date of this job.
     */
    public void setQuoteDate(String p_quoteDate);

    /**
     * Get the quotation email date.
     * 
     * @return The date of the quotation email.
     */
    public String getQuoteDate();

    // For "Quote process webEx"
    /**
     * Set the Approved Quote Date of this job.
     * 
     * @param p_quoteApprovedDate
     *            Approved Quote Date.
     */
    public void setQuoteApprovedDate(String p_quoteApprovedDate);

    /**
     * Get the Approved Quote Date of this job.
     * 
     * @return The date of the Confirm Approved Quote.
     */
    public String getQuoteApprovedDate();

    /**
     * Set the quote PO number of this job.
     * 
     * @param p_quotePoNumber
     *            The quote PO number by user set.
     */
    public void setQuotePoNumber(String p_quotePoNumber);

    /**
     * Get the quote PO number of this job.
     * 
     * @return quote PO number of this job.
     */
    public String getQuotePoNumber();

    /**
     * Set the person who approved the cost of job.
     */
    public void setUser(User user);

    /**
     * Get the person who approved the cost of job.
     * 
     * @return the approved person
     */
    public User getUser();

    /**
     * Get the person id who approved the cost of job.
     * 
     * @return the approved person's id
     */
    public String getUserId();

    public void setUserId(String userId);

    public void setLeverageOption(String leverageOption);

    public String getLeverageOption();

    public String getCreateUserId();

    public void setCreateUserId(String userId);

    public User getCreateUser();

    public Set<JobAttribute> getAttributes();

    public void setAttributes(Set<JobAttribute> attributes);

    public List<JobAttribute> getAllJobAttributes();

    public boolean hasSetCostCenter();

    public String getOrgState();

    public void setOrgState(String state);

    public long getProjectId();

    public Project getProject();

    public boolean hasPassoloFiles();

    public Date getStartDate();

    public void setStartDate(Date startDate);

    public Date getCompletedDate();

    public void setCompletedDate(Date completedDate);

    public String getFProfileNames();

    public void setIsMigrated(boolean p_isMigrated);

    public boolean isMigrated();

    public boolean getIsAllRequestGenerated();

    public void setIsAllRequestGenerated(boolean isAllRequestGenerated);

    public String getLmArchiveTable();

    public void setLmArchiveTable(String lmArchiveTable);

    public String getLmTable();

    public void setLmTable(String lmTable);

    public String getTuvArchiveTable();

    public void setTuvArchiveTable(String tuvArchiveTable);

    public String getTuvTable();

    public void setTuvTable(String tuvTable);

    public String getTuArchiveTable();

    public void setTuArchiveTable(String tuArchiveTable);

    public String getTuTable();

    public void setTuTable(String tuTable);

    public String getLmExtTable();

	public void setLmExtTable(String lmExtTable);

	public String getLmExtArchiveTable();

	public void setLmExtArchiveTable(String lmExtArchiveTable);

    public String getJobType();

    public void setJobType(String jobType);

    public boolean isBlaiseJob();
    public boolean isCotiJob();
    public boolean isEloquaJob();
    public boolean isGitJob();
    public boolean isMindTouchJob();
}
