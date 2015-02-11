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
package com.globalsight.everest.workflowmanager;

// globalsight
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.util.GlobalSightLocale;

/**
 * Provides the interface for a Workflow object. This interface specifies the
 * pieces of a workflow that are specific to localization. The pieces specific
 * to a workflow in general (activities) are found in the IflowInstance set and
 * retrieved by this interface.
 */
public interface Workflow extends WorkObject
{
    public String PENDING = Job.PENDING;

    public String IMPORT_FAILED = Job.IMPORTFAILED;

    public String DISPATCHED = Job.DISPATCHED;

    public String LOCALIZED = Job.LOCALIZED;

    public String EXPORTING = Job.EXPORTING;

    public String SKIPPING = Job.SKIPPING;

    public String EXPORTED = Job.EXPORTED;

    public String EXPORT_FAILED = Job.EXPORT_FAIL;

    public String CANCELLED = Job.CANCELLED;

    public String ARCHIVED = Job.ARCHIVED;

    public String BATCHRESERVED = Job.BATCHRESERVED;

    public String READY_TO_BE_DISPATCHED = Job.READY_TO_BE_DISPATCHED;

    // query key used to build a TOPLink query
    public String WORKFLOW_ID = "m_wfInstanceId";

    public String WORKFLOW_JOB = "m_job";

    public String WORKFLOW_LOCALE = "m_targetLocale";

    public String COMPANY_ID = "m_companyId";

    public String getWorkflowType();

    public void setWorkflowType(String p_workflowType);

    // Getters.
    public Date getCompletedDate();

    public Date getDispatchedDate();

    public long getId();

    public Long getIdAsLong();

    public WorkflowInstance getIflowInstance();

    /**
     * Get a collection of task objects for accessing additional info not stored
     * in iFlow. The key is the task id and the value is the task object itself.
     * 
     * @return A map of tasks.
     */
    Hashtable getTasks();

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
     * Add a task to this workflow.
     * 
     * @param p_task
     *            The task to be added to this workflow.
     */
    void addTask(Task p_task);

    /**
     * Remove a task from this workflow.
     * 
     * @param p_task
     *            The task to be removed from this workflow.
     */
    public void removeTask(Task p_task);

    public Job getJob();

    public String getState();

    /**
     * Return the target pages of the workflow that have a primary file of the
     * type specified and are not in the IMPORT_FAIL state.
     * 
     * @return An array list of target pages or an empty collection if no target
     *         pages with that primary file type exist.
     */
    public ArrayList getTargetPages(int p_primaryFileType);

    /**
     * Return the target pages of the workflow that can be worked on (not
     * IMPORT_FAIL pages).
     */
    public Vector<TargetPage> getTargetPages();

    /**
     * Return ALL the target pages no matter what their state.
     */
    public Vector<TargetPage> getAllTargetPages();

    /**
     * Get a list of secondary target files associated with this workflow.
     * 
     * @return A list of secondary target files (if any is associated with this
     *         workflow). Otherwise, return an empty list.
     */
    public Set<SecondaryTargetFile> getSecondaryTargetFiles();

    public GlobalSightLocale getTargetLocale();

    public int getPercentageCompletion();

    /*
     * Retuns the duration of the workflow as number of days.
     */
    public long getDuration();

    public int getInContextMatchWordCount();

    public void setInContextMatchWordCount(int inContextMatchWord);

    public void setMtExactMatchWordCount(int mtExactMatchWord);

    public int getMtExactMatchWordCount();

    public void setMtTotalWordCount(int p_mtTotalWordCount);

    public int getMtTotalWordCount();

    public void setMtFuzzyNoMatchWordCount(int p_mtFuzzyNoMatchWordCount);

    public int getMtFuzzyNoMatchWordCount();

    public void setMtRepetitionsWordCount(int p_mtRepetitionsWordCount);

    public int getMtRepetitionsWordCount();

    /**
     * Retuns the workflow total context match word count. This is a
     * workflow-level context match word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public int getContextMatchWordCount();

    /**
     * Retuns the workflow total exact segment TM word count. This is a
     * workflow-level-exact segment TM word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public int getSegmentTmWordCount();

    /**
     * Retuns the workflow total Low Fuzzy word count (50-74%). This is a
     * workflow-level low fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public int getLowFuzzyMatchWordCount();

    /**
     * Retuns the workflow total MED Fuzzy word count (75-84%). This is a
     * workflow-level MED fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public int getMedFuzzyMatchWordCount();

    /**
     * Retuns the workflow total MED-HI Fuzzy word count (85-94%). This is a
     * workflow-level MED-HI fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public int getMedHiFuzzyMatchWordCount();

    /**
     * Retuns the workflow total HI Fuzzy word count (95-99%). This is a
     * workflow-level HI fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public int getHiFuzzyMatchWordCount();

    /**
     * Retuns the workflow total HI Fuzzy Repetition word count (95-99%). This
     * is a workflow-level HI fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
//    public int getHiFuzzyRepetitionWordCount();

    /**
     * Retuns the workflow total Med HI Fuzzy Repetition word count (85-94%).
     * This is a workflow-level HI fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
//    public int getMedHiFuzzyRepetitionWordCount();

    /**
     * Retuns the workflow total Med Fuzzy Repetition word count (75-84%). This
     * is a workflow-level HI fuzzy word count calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
//    public int getMedFuzzyRepetitionWordCount();

    /**
     * Get the word count for the sub-leverage-match category. See
     * setSubLevMatchWordCount method for more details.
     * 
     * @return The sub-leverage-match category word counts.
     */
//    int getSubLevMatchWordCount();

    /**
     * Get the repetition word count for the sub-leverage-match category. See
     * setSubLevRepetitionWordCount method for more details.
     * 
     * @return The sub-leverage-match-repetition word counts.
     */
//    int getSubLevRepetitionWordCount();

    /*
     * Retuns the workflow total noMatch word count. This is a
     * workflow-level-noMatch word count calculated by
     * StatisticsService.calculateWorkflowStatistics() that incorporates
     * repetitions of segments across all files in the workflow.
     */
    public int getNoMatchWordCount();

    /*
     * Returns the workflow total repetition word count. This is a
     * workflow-level-repetition word count calculated by
     * StatisticsService.calculateWorkflowStatistics() that reflects repetitions
     * of segments across all files in the workflow.
     */
    public int getRepetitionWordCount();

    /*
     * Retuns the workflow total word count. This is a workflow-level total word
     * count calculated by StatisticsService.calculateWorkflowStatistics().
     */
    public int getTotalWordCount();

    public int getNoUseInContextMatchWordCount();

    public void setNoUseInContextMatchWordCount(int noUseInContextMatchWordCount);

    public int getTotalExactMatchWordCount();

    public void setTotalExactMatchWordCount(int totalExactMatchWordCount);

    // Setters.

    /**
     * Set the duration of the workflow as number of days.
     */
    public void setDuration(long p_duration);

    /**
     * Sets the workflow's total context match word count as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setContextMatchWordCount(int p_totalContextMatchWordCount);

    /**
     * Sets the workflow's total segment TM exact match word count as calculated
     * by StatisticsService.calculateWorkflowStatistics().
     */
    public void setSegmentTmWordCount(int p_totalSegmentTmWordCount);

    /**
     * Sets the workflow's total low fuzzy word count (50-74%) as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setLowFuzzyMatchWordCount(int p_totalLowFuzzyWordCount);

    /**
     * Sets the workflow's total MED fuzzy word count (75-84%) as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setMedFuzzyMatchWordCount(int p_totalMedFuzzyWordCount);

    /**
     * Sets the workflow's repetition word count (75-84%) calculated by
     * StatisticsService.calculateWorkflowStatistics().
     * 
     * @param p_hiFuzzyRepetitionWordCount
     */
//    public void setMedFuzzyRepetitionWordCount(int p_medFuzzyRepetitionWordCount);

    /**
     * Sets the workflow's total MED_HI fuzzy word count (85-94%) as calculated
     * by StatisticsService.calculateWorkflowStatistics().
     */
    public void setMedHiFuzzyMatchWordCount(int p_totalMedHiFuzzyWordCount);

    /**
     * Sets the workflow's repetition word count (85-94%) calculated by
     * StatisticsService.calculateWorkflowStatistics().
     * 
     * @param p_hiFuzzyRepetitionWordCount
     */
//    public void setMedHiFuzzyRepetitionWordCount(
//            int p_medHiFuzzyRepetitionWordCount);

    /**
     * Sets the workflow's total HI fuzzy word count (95-99%) as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setHiFuzzyMatchWordCount(int p_totalHiFuzzyWordCount);

    /**
     * Sets the workflow's repetition word count (95-99%) calculated by
     * StatisticsService.calculateWorkflowStatistics().
     * 
     * @param p_hiFuzzyRepetitionWordCount
     */
//    public void setHiFuzzyRepetitionWordCount(int p_hiFuzzyRepetitionWordCount);

    /**
     * Set the value of the Sub-Leverage-Match word count to be the specified
     * value. This value is determined based on the leverage match threshold
     * percentage of the job during import with respect to the leverage match
     * categories. For example if the leverage match threshold is at 70%, the
     * value for this word count would be the number of words in the 50-69%
     * category.
     * 
     * @param p_subLevMatchWordCount
     *            The number of words in the sub leverage match category.
     */
//    void setSubLevMatchWordCount(int p_subLevMatchWordCount);

    /**
     * Set the value of the Sub-Leverage-Match-Repetition word count to be the
     * specified value. This value is determined based on the leverage match
     * threshold percentage of the job during import with respect to the
     * leverage match categories. For example if the leverage match threshold is
     * at 70%, the value for this word count would be the number of repetitions
     * in the 50-69% category (since anything below 70% is considered no-match).
     * 
     * @param p_subLevRepetitionWordCount
     *            The number of repetitions in the sub leverage match category.
     */
//    void setSubLevRepetitionWordCount(int p_subLevRepetitionWordCount);

    /*
     * Sets the workflow's total noMatch word count as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setNoMatchWordCount(int p_totalNoMatchWordCount);

    /*
     * Sets the workflow's total repetition word count as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setRepetitionWordCount(int p_totalRepetitionWordCount);

    /*
     * Sets the workflow's total word count as calculated by
     * StatisticsService.calculateWorkflowStatistics().
     */
    public void setTotalWordCount(int p_totalWordCount);

    public void setId(long p_id);

    public void setIflowInstance(WorkflowInstance p_wfInstance);

    public void setJob(Job p_job);

    public void setState(String p_state);

    public void setTargetLocale(GlobalSightLocale p_locale);

    public void addTargetPage(TargetPage p_targetPage);

    public void addSecondaryTargetFile(SecondaryTargetFile p_stf);

    public void setDispatchedDate(Date p_dispatchedDate);

    public void setCompletedDate(Date p_completedDate);

    public int getCompletionNumerator();

    public int getCompletionDenominator();

    public void setCompletionFraction(int p_numerator, int p_denominator);

    /**
     * @deprecated For sla report issue. Set the planned completion date of this
     *             workflow to be the specified date.
     * @param p_plannedCompletionDate
     *            - The date to be set.
     */
    void setPlannedCompletionDate(Date p_plannedCompletionDate);

    /**
     * @deprecated For sla report issue. Get the planned completion date of this
     *             worklfow.
     * @return The planned completion date.
     */
    Date getPlannedCompletionDate();

    /**
     * Add this owner to the list of the workflow owners.
     * 
     * @param p_workflowOwner
     *            - The workflow owner to be added.
     */
    void addWorkflowOwner(WorkflowOwner p_workflowOwner);

    /**
     * Get a list of workflow owner objects.
     * 
     * @return A list of workflow owners who can perform modification (i.e.
     *         structural edit and reassignment) of this workflow.
     */
    List getWorkflowOwners();

    /**
     * Get a list of workflow owner ids
     * 
     * @return A list of workflow owner ids
     */
    List<String> getWorkflowOwnerIds();

    /**
     * Get a list of workflow owner ids by type.
     * 
     * @return A list of workflow owners by ownership type (i.e. ProjectManager,
     *         WorkflowManager).
     */
    List getWorkflowOwnerIdsByType(String p_ownerType);

    /**
     * Remove the specified owner from the list of workflow owners.
     * 
     * @param p_workflowOwner
     *            - The owner to be removed from this workflow.
     */
    void removeWorkflowOwner(WorkflowOwner p_workflowOwner);

    /**
     * Add this workflow comment to the collection.
     * 
     * @param p_comment
     *            - The comment to be added.
     */
    public void addWorkflowComment(Comment p_comment);

    /**
     * Remove this workflow comment from the collection.
     * 
     * @param p_comment
     *            - The comment remove.
     */
    public void removeWorkflowComment(Comment p_comment);

    /**
     * Set the workflow comments to be this value.
     * 
     * @param p_comments
     *            - The task comments to be set.
     */
    public void setWorkflowComments(Set<Comment> p_comments);

    /**
     * Get a list of workflow comments sorted descending by date
     * 
     * @return a List of Comments for this task.
     */
    public List getWorkflowComments();

    /**
     * Get the estimated completion time for this workflow.
     * 
     * @return The estimated completion date.
     */
    Date getEstimatedCompletionDate();

    /**
     * Set the estimated completion time for this workflow to be the specified
     * date.
     * 
     * @param p_estimatedCompletionDate
     *            - The date to be set.
     */
    void setEstimatedCompletionDate(Date p_estimatedCompletionDate);

    /**
     * For sla report issue. User can override the estimatedCompletionDate.
     * 
     * @return isEstimatedCompletionDateOverrided.
     */
    boolean isEstimatedCompletionDateOverrided();

    /**
     * For sla report issue. User can override the estimatedCompletionDate.
     * 
     * @param p_isEstimatedCompletionDateOverrided
     *            - is overrided.
     */
    void setEstimatedCompletionDateOverrided(
            boolean p_isEstimatedCompletionDateOverrided);

    /**
     * For sla report issue.
     * 
     * Get the estimated translate completion time for this workflow to be the
     * specified date.
     * 
     * @return The estimated translate completion date.
     */
    public Date getEstimatedTranslateCompletionDate();

    /**
     * For sla report issue.
     * 
     * Set the estimated translate completion time for this workflow to be the
     * specified date.
     * 
     * @param p_estimatedTranslateCompletionDate
     *            - The date to be set.
     */
    public void setEstimatedTranslateCompletionDate(
            Date p_estimatedTranslateCompletionDate);

    /**
     * For sla report issue. User can override the
     * estimatedTranslateCompletionDate.
     * 
     * @return isEstimatedTranslateCompletionDateOverrided.
     */
    boolean isEstimatedTranslateCompletionDateOverrided();

    /**
     * For sla report issue. User can override the
     * estimatedTranslateCompletionDate.
     * 
     * @param p_isEstimatedCompletionDateOverrided
     *            - is overrided.
     */
    void setEstimatedTranslateCompletionDateOverrided(
            boolean p_isEstimatedTranslateCompletionDateOverrided);

    /**
     * For sla report issue. Get the completion date of the first translate
     * activity in workflow.
     * 
     * @return p_actualTranslateCompletionDate
     */
    public Date getTranslationCompletedDate();

    public void setTranslationCompletedDate(Date p_translationCompletedDate);

    /**
     * Use for calculate "Tranlation Completed Date" and "Estimated Translate
     * Completion Date". It need to be called every time while the workflow
     * updated.
     */
    public void updateTranslationCompletedDates();

    /**
     * Judges whether the workflow is translation workflow.
     * 
     * @return
     */
    public boolean isTranslationWorkflow();

    public int getPriority();

    public void setPriority(int priority);

    public int getThresholdHiFuzzyWordCount();

    public void setThresholdHiFuzzyWordCount(int thresholdHiFuzzyWordCount);

    public int getThresholdMedHiFuzzyWordCount();

    public void setThresholdMedHiFuzzyWordCount(int thresholdMedHiFuzzyWordCount);

    public int getThresholdMedFuzzyWordCount();

    public void setThresholdMedFuzzyWordCount(int thresholdMedFuzzyWordCount);

    public int getThresholdLowFuzzyWordCount();

    public void setThresholdLowFuzzyWordCount(int thresholdLowFuzzyWordCount);

    public int getThresholdNoMatchWordCount();

    public void setThresholdNoMatchWordCount(int thresholdNoMatchWordCount);

    public void setUseMT(boolean p_value);

    public boolean getUseMT();

    public void setMtConfidenceScore(int p_mtConfidenceScore);

    public int getMtConfidenceScore();
    
    public int getScorecardShowType();
    
    public void setScorecardShowType(int p_scorecardShowType);
    
    public String getScorecardComment();
    
    public void setScorecardComment(String p_scorecardComment);

    public void setMtEngineWordCount(int p_mtEngineWordCount);

    public int getMtEngineWordCount();
    
    public String getMtProfileName();

    public void setMtProfileName(String mtProfileName);
}
