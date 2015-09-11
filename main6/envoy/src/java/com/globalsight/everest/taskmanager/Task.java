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
package com.globalsight.everest.taskmanager;

import java.util.Date;
import java.util.List;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.costing.AmountOfWork;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.GlobalSightLocale;

public interface Task extends WorkObject
{
    // query key used to build a TOPLink query
    public static final String TASK_ID = "m_taskInstanceId";
    public static final String CREATE_DATE = "m_createDate";
    public static final String TASK_COMMENTS = "m_taskComments";
    public static final String COMPANY_ID = "m_companyId";
    public static final String IS_UPLOADING = "m_isUploading";// for gbs-1939
    // public static final String RATE = "m_rate";
    public static final String EXPENSE_RATE = "m_expenseRate";
    public static final String REVENUE_RATE = "m_revenueRate";
    public static final String RATE_SELECTION_CRITERIA = "m_rateSelectionCriteria";

    // Workflow type constants
    public static final String TYPE_TRANSLATION = WorkflowTypeConstants.TYPE_TRANSLATION;
    public static final String TYPE_DTP = WorkflowTypeConstants.TYPE_DTP;

    // ///////////////
    // Task States
    // ///////////////
    // A task can only have one of the following states. Note that the
    // 'rejected' state is only retrieved from iFlow and is not persisted
    // in our table.
    public static final int STATE_ACTIVE = WorkflowConstants.TASK_ACTIVE; // ACTIVE
    public static final int STATE_ACCEPTED = WorkflowConstants.TASK_ACCEPTED; // ACCEPTED
    public static final int STATE_DISPATCHED_TO_TRANSLATION = WorkflowConstants.TASK_DISPATCHED_TO_TRANSLATION; // DISPATCHED_TO_TRANSLATION
    public static final int STATE_IN_TRANSLATION = WorkflowConstants.TASK_IN_TRANSLATION; // IN_TRANSLATION
    public static final int STATE_TRANSLATION_COMPLETED = WorkflowConstants.TASK_TRANSLATION_COMPLETED; // TRANSLATION_COMPLETED
    public static final int STATE_REDEAY_DISPATCH_GSEDTION = WorkflowConstants.TASK_READEAY_DISPATCH_GSEDTION;
    public static final int STATE_COMPLETED = WorkflowConstants.TASK_COMPLETED; // COMPLETED
    public static final int STATE_DEACTIVE = WorkflowConstants.TASK_DEACTIVE; // DEACTIVE
    public static final int STATE_REJECTED = WorkflowConstants.TASK_DECLINED;
    public static final int STATE_ALL = WorkflowConstants.TASK_ALL_STATES;
    public static final int STATE_SKIP = WorkflowConstants.TASK_ALL_STATES;
    public static final int STATE_FINISHING = WorkflowConstants.TASK_FINISHING;

    public static final String STATE_FINISHING_STR = "FINISHING";
    public static final String STATE_ACTIVE_STR = "ACTIVE"; // ACTIVE
    public static final String STATE_ACCEPTED_STR = "ACCEPTED"; // ACCEPTED
    public static final String STATE_DISPATCHED_TO_TRANSLATION_STR = "DISPATCHED_TO_TRANSLATION"; // DISPATCHED_TO_TRANSLATION
    public static final String STATE_IN_TRANSLATION_STR = "IN_TRANSLATION"; // IN_TRANSLATION
    public static final String STATE_TRANSLATION_COMPLETED_STR = "TRANSLATION_COMPLETED"; // TRANSLATION_COMPLETED
    public static final String STATE_REDEAY_DISPATCH_GSEDTION_STR = "READY_FOR_DISPATCHING_TO_TRANSLATION";
    public static final String STATE_COMPLETED_STR = "COMPLETED"; // COMPLETED
    public static final String STATE_DEACTIVE_STR = "DEACTIVE"; // DEACTIVE
    // for gbs-1302, 'TRIGGERED' mark
    public static final String STATE_INTERIM_TRIGGERED = "TRIGGERED"; // TRIGGERED

    // used if it can't determine what the task's state is
    public static final int STATE_NOT_KNOWN = 0;

    // Secondary Target File Creation States
    public static final String COMPLETED = "COMPLETED";
    public static final String FAILED = "FAILED";
    public static final String IN_PROGRESS = "IN_PROGRESS";

    // task types
    public static final int TYPE_TRANSLATE = Activity.TYPE_TRANSLATE;
    public static final int TYPE_REVIEW = Activity.TYPE_REVIEW;

    // For sla report
    // This type is only in Task, not in Activity.
    public static final int TYPE_REVIEW_EDITABLE = Activity.TYPE_REVIEW_EDITABLE;

    public String getTaskType();

    public void setTaskType(String p_taskType);

    /**
     * Set compnay id in request. It is used to create job, workflow, taskinfo.
     */
    public void setCompanyId(long p_companyId);

    /**
     * Returns the company id stored in request.
     * 
     * @return
     */
    public long getCompanyId();

    /**
     * Get the id of this task.
     * 
     * @return long of The task's id.
     */
    public long getId();

    /**
     * Get the Source Pages
     * 
     * @return a list of SourcePage.
     */
    public List getSourcePages();

    /**
     * Get the source pages that are associated with the specified primary file
     * type.
     * 
     * @see PrimaryFile for the valid types
     */
    public List getSourcePages(int p_primaryFileType);

    /**
     * Get a list of TargetPage objects.
     * 
     * @return a List of TargetPages.
     */
    public List getTargetPages();

    /**
     * Get a list of TargetPage objects of a particular type. (un-extracted or
     * extracted0 see PrimaryFile for the valid types
     */
    public List getTargetPages(int p_primaryFileType);

    /**
     * Set the accepted date to be this particular date.
     * 
     * @param p_acceptedDate
     *            - The date for the task to be accepted by.
     */
    public void setAcceptedDate(Date p_acceptedDate);

    /**
     * Get the date that the task was accepted by the user.
     * 
     * @return a Date object.
     */
    public Date getAcceptedDate();

    /**
     * Get the date that the task was accepted by the user as a String.
     * 
     * @return String of The accepted date.
     */
    public String getAcceptedDateAsString();

    /**
     * Set the completed date to be this particular date.
     * 
     * @param p_completedDate
     *            - The date for the task to be completed by.
     */
    public void setCompletedDate(Date p_completedDate);

    /**
     * Get the date that the task was completed.
     * 
     * @return Date.
     */
    public Date getCompletedDate();

    /**
     * Get the date that the task was completed as String.
     * 
     * @return String of The task's completion date.
     */
    public String getCompletedDateAsString();

    /**
     * Get the duration (as a string) required for completing this task.
     * 
     * @param p_dayAbbrev
     *            the day abbreviation to be displayed on UI.
     * @param p_hourAbbrev
     *            the hour abbreviation to be displayed on UI.
     * @param p_minuteAbbrev
     *            the minute abbreviation to be displayed on UI.
     * 
     * @return a string representation of the duration in terms of days, hours,
     *         and minutes.
     */
    public String getTaskDurationAsString(String p_dayAbbrev,
            String p_hourAbbrev, String p_minuteAbbrev);

    /**
     * Get the duration ( in milliseconds) for completing this task.
     */
    public long getTaskDuration();

    /**
     * Gets the duration from accepted to completed.
     * 
     * @return the duration from accepted to completed.
     */
    public String getDurationString();

    /**
     * Get the duration (in milliseconds) for accepting this task.
     */
    public long getTaskAcceptDuration();

    /**
     * Add this task comment to the collection.
     * 
     * @param p_taskComment
     *            - The comment to be added.
     */
    public void addTaskComment(Comment p_taskComment);

    /**
     * Remove this task comment from the collection.
     * 
     * @param p_taskComment
     *            - The comment remove.
     */
    public void removeTaskComment(Comment p_taskComment);

    /**
     * Set the task comments to be this value.
     * 
     * @param p_taskComments
     *            - The task comments to be set.
     */
    public void setTaskComments(List p_taskComments);

    /**
     * Get a list of task comments.
     * 
     * @return a List of Comments for this task.
     */
    public List getTaskComments();

    /**
     * Get a task comment based on comment id
     * 
     * @return a Comment for this task.
     */
    public Comment getTaskComment(long commentId);

    /**
     * Get the source locale.
     * 
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getSourceLocale();

    /**
     * Get the target locale.
     * 
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getTargetLocale();

    /**
     * Get the name of the project where the workflow belongs to.
     * 
     * @return The project name as String.
     */
    public String getProjectName();

    /**
     * Get the priority of this task (retrieved from the job).
     * 
     * @return The job priority
     */
    public int getPriority();

    /**
     * Get the ID of the job where the workflow belongs to.
     * 
     * @return The job name as long.
     */
    public long getJobId();

    /**
     * Get the name of the job where the workflow belongs to.
     * 
     * @return The job name as String.
     */
    public String getJobName();

    /**
     * To get the user id of the project manager.
     * 
     * @return String
     */
    public String getProjectManagerId();

    /**
     * To get the name of the project manager.
     * 
     * @return String
     */
    public String getProjectManagerName();

    /**
     * To set the name of the project manager.
     */
    public void setProjectManagerName(String p_pmName);

    /**
     * To get the state of the task. The states are defined in constants.
     * 
     * @return int
     */
    public int getState();

    /**
     * Get the state of the task as a string.
     */
    public String getStateAsString();

    /**
     * Set the task state to be the specified state. Note that only the states
     * defined in Task interface can be used.
     */
    void setState(int p_state);

    /**
     * To get the name of the task.
     * 
     * @return String
     */
    public String getTaskName();

    // we may need to update the task name
    public void setTaskName(String p_taskName);

    /**
     * To get the name of the Activity associated the task.
     * 
     * @return String
     */
    public String getTaskDisplayName();

    /**
     * To set a WorkflowTaskInstance to a Task.
     * 
     * @param p_wfTaskInstance
     *            the WorkflowTaskInstance to add.
     */
    public void setWorkflowTask(WorkflowTaskInstance p_wfTaskInstance);

    /**
     * Get the Workflow this Task links to.
     * 
     * @return a Workflow object.
     */
    public Workflow getWorkflow();

    /**
     * Set the Workflow this Task links to.
     * 
     * @param a
     *            Workflow object.
     */
    public void setWorkflow(Workflow p_workflow);

    /**
     * Get the time stamp the task was completed by the user as a String.
     * 
     * @return String of "Completed date" timestamp.
     */
    public String getCompletedOnAsString();

    /**
     * Get a list of assignees.
     * 
     * @return A list of assignees.
     */
    List getAllAssignees();

    /**
     * Get a list of assignees separated by comma.
     * 
     * @return A list of assignees as one string.
     */
    String getAllAssigneesAsString();

    /**
     * This method is to be used for task's that aren't active yet. It returns
     * the specifically chosen assignee's full name or "All Qualified Users" if
     * any of the "qualified" users could be chosen when the task becomes
     * active.
     */
    public String getPossibleAssignee();

    /**
     * Get the list of target node info for a condition node ONLY.
     * 
     * @return A list of ConditionNodeTargetInfo objects for a condition node.
     *         Otherwise, returns null.
     */
    List getConditionNodeTargetInfos();

    /**
     * Get the roles of the task.
     * 
     * @return A string array with elements representing the activity role.
     */
    String[] getActivityRoles();

    /**
     * Get the activity roles as a comma delimited string.
     * 
     * @return A comma delimited string represening activity roles.
     */
    String getActivityRolesAsString();

    /**
     * Return the expense rate that is associated with this task. This could be
     * null if there isn't a rate associated with this task, so no costing
     * should be done on thist task.
     */
    Rate getExpenseRate();

    /**
     * Sets the expense rate that is associated with this task.
     * 
     * @p_rate Rate Either the rate to assign to this task or NULL to remove a
     *         setting.
     */
    void setExpenseRate(Rate p_rate);

    /**
     * Return the revenue rate that is associated with this task. This could be
     * null if there isn't a rate associated with this task, so no costing
     * should be done on thist task.
     */
    Rate getRevenueRate();

    /**
     * Sets the revenue rate that is associated with this task.
     * 
     * @p_rate Rate Either the rate to assign to this task or NULL to remove a
     *         setting.
     */
    void setRevenueRate(Rate p_rate);

    /**
     * Set the amount of work object for the UnitOfWork specified. Only sets it
     * if the UnitOfWork = Rate.UnitOfWork.HOURLY or Rate.UnitOfWork.PAGE_COUNT
     * 
     * @param p_work
     *            The amount of work object with estimated and actual amount of
     *            work. Makes the assumption that this task has a rate on it
     *            with the same UnitOfWork and needs this object for calculating
     *            the cost. No verification is done.
     */
    void setAmountOfWork(AmountOfWork p_work);

    /**
     * Get the AmountOfWork for the specified UnitOfWork.
     * 
     * @return The amount of work for the UnitOfWork specified. Returns NULL if
     *         the UnitOfWork doesn't need a work amount specified or if one
     *         hasn't been set yet.
     */
    AmountOfWork getAmountOfWork(Integer p_unitOfWork);

    /*
     * Remove amount of work
     */
    public void removeAmountOfWork(Integer p_unitOfWork);

    /*
     * Remove amount of work
     */
    public void removeAmountOfWork();

    /**
     * Get the acceptor of this task.
     * 
     * @return The Uses who accepted this task.
     */
    public String getAcceptor();

    /**
     * Set the acceptor for this task.
     * 
     * @param p_user
     *            . The user who accepted this task.
     */
    public void setAcceptor(String p_user);

    /**
     * Get the selection criteria for this task.
     */
    public int getRateSelectionCriteria();

    /**
     * Set the selection criteria for this task.
     */
    public void setRateSelectionCriteria(int p_criteria);

    /**
     * Get the state of the secondary target file creation process.
     * 
     * @return The state of the process of secondary target file creation. A
     *         valid state is 'IN_PROGRESS', 'COMPLETED', or 'FAILED'.
     */
    String getStfCreationState();

    /**
     * Set the creation of secondary target file state for this task. This state
     * is only set if there are any associated secondary target files with this
     * task.
     * 
     * @param p_stfCreationState
     *            - The state to be set.
     */
    void setStfCreationState(String p_stfCreationState);

    /**
     * Get the estimated acceptance time for this task.
     * 
     * @return The estimated acceptance date.
     */
    Date getEstimatedAcceptanceDate();

    /**
     * Get the estimated completion time for this task.
     * 
     * @return The estimated completion date.
     */
    Date getEstimatedCompletionDate();

    /**
     * Set the estimated acceptance date for this task to be the specified date.
     * 
     * @param p_estimatedAcceptanceDate
     *            - The date to be set.
     */
    void setEstimatedAcceptanceDate(Date p_estimatedAcceptanceDate);

    /**
     * Set the estimated completion time for this task to be the specified date.
     * 
     * @param p_estimatedCompletionDate
     *            - The date to be set.
     */
    void setEstimatedCompletionDate(Date p_estimatedCompletionDate);

    /**
     * Returns the ratings that are associated with this task. Will be NULL or
     * empty if this Task doesn't have any ratings. A task can only have ratings
     * if it is complete and the user that worked on it is a vendor.
     */
    List getRatings();

    /**
     * Add the rating to the task.
     */
    void addRating(Rating p_rating);

    /**
     * Remove the rating from the collection.
     */
    void removeRating(Rating p_rating);

    /**
     * Returns 'true' if the activity is of the type specified. 'false' if the
     * activity is not of the type.
     */
    public boolean isType(int p_type);

    /**
     * Return the activity's type
     */
    public int getType();

    /**
     * Set the activity's type
     */
    public void setType(int p_type);

    /**
     * Return this task can be reassign or not.
     * 
     * @return
     */
    public boolean reassignable();

    /**
     * Gets the duration of the task.
     * <p>
     * 
     * If the task has not been accepted, the duration is "--". If the task has
     * been accepted, the duration equals to the complete date minus the accept
     * date. When the task has not been completed, the duration equasl to the
     * current date minus the accept date.
     * <p>
     * 
     * 
     * @return A string like "*d *h *m" if has duration or "--".
     */
    public String getActualDuration();

    // for GBS-1939
    /**
     * Get state of the uploading this activity belong to.
     * 
     * @return The uploading state.
     */
    public char getIsUploading();

    /**
     * Set state of the uploading this activity belong to.
     * 
     * @return The uploading state.
     */
    public void setIsUploading(char p_isUploading);
    
    public void setIsReportUploadCheck(int p_isReportUploadCheck);

	public int getIsReportUploadCheck();

	public void setIsReportUploaded(int p_isReportUploaded);

	public int getIsReportUploaded();

    public void setQualityAssessment(String qualityAssessment);
    
    public String getQualityAssessment();

    public void setMarketSuitability(String marketSuitabilty);
    
    public String getMarketSuitability();
    

}
