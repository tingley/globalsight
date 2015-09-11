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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.costing.AmountOfWork;
import com.globalsight.everest.costing.Rate;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.vendormanagement.Rating;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserUtil;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowConstants;
import com.globalsight.everest.workflow.WorkflowTaskInstance;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.util.DateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.date.DateHelper;

/**
 * TaskImpl is the implementation of Task interface and contains the information
 * of a workflow task which is a combination of iFlow's task and Envoy's domain
 * specific attributes.
 */
public class TaskImpl extends PersistentObject implements Task, WorkObject
{
    private static final long serialVersionUID = 1L;

    public static final String EMPTY_DATE = "--";

    private static List<String> ASSIGNABLE_STATE;
    static
    {
        ASSIGNABLE_STATE = new ArrayList<String>();
        ASSIGNABLE_STATE.add(STATE_ACTIVE_STR);
        ASSIGNABLE_STATE.add(STATE_ACCEPTED_STR);
        ASSIGNABLE_STATE.add(STATE_DEACTIVE_STR);
    }

    private int m_state = STATE_DEACTIVE; // default state

    private Date m_estimatedAcceptanceDate = null;

    private Date m_estimatedCompletionDate = null;

    private Date m_acceptedDate = null;

    private Date m_completedDate = null;

    private Rate m_expenseRate = null;

    private Rate m_revenueRate = null;

    private String m_stfCreationState = null;

    private String m_acceptor = null;

    private int m_rateSelectionCriteria = WorkflowConstants.USE_ONLY_SELECTED_RATE;

    private int m_type = TYPE_TRANSLATE;

    private String m_taskType = TYPE_TRANSLATION;

    private long m_companyId;

    private long m_taskInstanceId = -99;

    private List m_taskComments = new ArrayList();

    private List m_ratings = new ArrayList();

    // for costing
    private Hashtable m_work = new Hashtable();
    private boolean m_workNeedsCalculating = true;

    // for Hibernate
    private Set<AmountOfWork> m_workSet = new HashSet<AmountOfWork>();

    private Set taskTuvs = new HashSet();

    // used as a back pointer for TopLink query purposes (since a task belongs
    // to a workflow).
    private Workflow m_workflow = null;

    // Parameters not mapping to Hibernate.
    private String m_projectManagerName = null;
    private WorkflowTaskInstance m_wfTaskInstance = null;

    // for gbs-1939
    private char m_isUploading;
    
    //for gbs-3574
    private int m_isReportUploaded = 0;//0:not upload; 1: uploaded.
    private int m_isReportUploadCheck = 0;//0:not check; 1: check.
    
    private String m_qualityAssessment = null;
    private String m_marketSuitability = null;

    /**
     * Get state of the uploading this activity belong to.
     * 
     * @return The uploading state.
     */
    public char getIsUploading()
    {
        return this.m_isUploading;
    }

    /**
     * Set state of the uploading this activity belong to.
     * 
     * @return The uploading state.
     */
    public void setIsUploading(char p_isUploading)
    {
        this.m_isUploading = p_isUploading;
    }
    
    public void setIsReportUploadCheck(int p_isReportUploadCheck) {
    	this.m_isReportUploadCheck = p_isReportUploadCheck;
	}

	public int getIsReportUploadCheck() {
		return this.m_isReportUploadCheck;
	}

	public void setIsReportUploaded(int p_isReportUploaded) {
		this.m_isReportUploaded = p_isReportUploaded;
	}

	public int getIsReportUploaded() {
		return this.m_isReportUploaded;
	}

    /**
     * Default constructor to be used by TopLink only.
     */
    public TaskImpl()
    {
    }

    /**
     * Another constructor to be used by TopLink only
     */
    public TaskImpl(Workflow p_workflow)
    {
        m_workflow = p_workflow; // for TopLink use only
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     * OVERRIDE: return the id of the task
     */
    public long getId()
    {
        return m_taskInstanceId;
    }

    /**
     * OVERRIDE: must also override the getIdAsLong() method.
     */
    public Long getIdAsLong()
    {
        return new Long(getId());
    }

    /**
     * This method is visible on the TaskImpl only -- not on the interface.
     */
    public void setId(long p_id)
    {
        m_taskInstanceId = p_id;
    }

    /**
     * Get a list of SourcePage objects.
     * 
     * @return a List of Pages.
     */
    public List getSourcePages()
    {
        if (m_workflow == null)
        {
            return new ArrayList(0);
        }
        return new ArrayList(m_workflow.getJob().getSourcePages());
    }

    /**
     * @see Task.getSourcePages(int)
     */
    public List getSourcePages(int p_primaryFileType)
    {
        if (m_workflow == null)
        {
            return new ArrayList(0);
        }
        return new ArrayList(m_workflow.getJob().getSourcePages(
                p_primaryFileType));
    }

    /**
     * Get a list of TargetPage objects.
     * 
     * @return a List of Pages.
     */
    public List getTargetPages()
    {
        if (m_workflow == null)
        {
            return new ArrayList(0);
        }
        return new ArrayList(m_workflow.getTargetPages());
    }

    /**
     * Get a liat of TargetPage objects of a particular type. (un-extracted or
     * extracted0 see PrimaryFile for the valid types
     */
    public List getTargetPages(int p_primaryFileType)
    {
        if (m_workflow == null)
        {
            return new ArrayList(0);
        }
        return new ArrayList(m_workflow.getTargetPages(p_primaryFileType));
    }

    /**
     * Set the accepted date to be this particular date.
     * 
     * @param p_acceptedDate
     *            - The date for the task to be accepted by.
     */
    public void setAcceptedDate(Date p_acceptedDate)
    {
        m_acceptedDate = p_acceptedDate;
    }

    /**
     * Get the date that the task was accepted by the user.
     * 
     * @return a Date object.
     */
    public Date getAcceptedDate()
    {
        return m_acceptedDate;
    }

    /**
     * Get the date that the task was accepted by the user as a String.
     * 
     * @return String of The accepted date.
     */
    public String getAcceptedDateAsString()
    {
        if (m_acceptedDate == null)
        {
            return "";
        }
        return DateHelper.getFormattedDate(m_acceptedDate);
    }

    /**
     * Set the completed date to be this particular date.
     * 
     * @param p_completedDate
     *            - The date for the task to be completed by.
     */
    public void setCompletedDate(Date p_completedDate)
    {
        m_completedDate = p_completedDate;
    }

    /**
     * Get the date that the task was completed.
     * 
     * @return Date.
     */
    public Date getCompletedDate()
    {
        return m_completedDate;
    }

    /**
     * Get the date that the task was completed as String.
     * 
     * @return String of The task's completion date.
     */
    public String getCompletedDateAsString()
    {
        if (m_completedDate == null)
        {
            return "";
        }
        return DateHelper.getFormattedDate(m_completedDate);
    }

    /**
     * Get the duration required for completing this task.
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
            String p_hourAbbrev, String p_minuteAbbrev)
    {
        long completionTime = m_wfTaskInstance == null ? 0 : m_wfTaskInstance
                .getCompletedTime();

        return DateHelper.daysHoursMinutes(completionTime, p_dayAbbrev,
                p_hourAbbrev, p_minuteAbbrev);
    }

    /**
     * Gets the duration from accepted to completed.
     * 
     * @return the duration from accepted to completed.
     */
    public String getDurationString()
    {
        return DateUtil.formatDuration(getDuration());
    }

    /**
     * Gets the duration in long format. Note: will return a long number larger
     * than 0.
     * 
     * @return the duration
     */
    public long getDuration()
    {
        long completedTime = m_completedDate == null ? -1 : m_completedDate
                .getTime();
        long acceptTime = m_acceptedDate == null ? -1 : m_acceptedDate
                .getTime();

        long duration = completedTime - acceptTime;

        if (acceptTime > -1 && completedTime > -1 && duration > 0)
        {
            return duration;
        }

        return 0;
    }

    /**
     * Get the duration ( in milliseconds) for completing this task.
     */
    public long getTaskDuration()
    {
        return m_wfTaskInstance.getCompletedTime();
    }

    /**
     * Get the duration (in milliseconds) for accepting this task.
     */
    public long getTaskAcceptDuration()
    {
        return m_wfTaskInstance.getAcceptTime();
    }

    /**
     * Add this task comment to the collection.
     * 
     * @param p_taskComment
     *            - The comment to be added.
     */
    public void addTaskComment(Comment p_taskComment)
    {
        m_taskComments.add(p_taskComment);
    }

    /**
     * Remove this task comment from the collection.
     * 
     * @param p_taskComment
     *            - The comment remove.
     */
    public void removeTaskComment(Comment p_taskComment)
    {
        m_taskComments.remove(p_taskComment);
    }

    /**
     * Set the task comments to be this value.
     * 
     * @param p_taskComments
     *            - The task comments to be set.
     */
    public void setTaskComments(List p_taskComments)
    {
        if (p_taskComments == null)
        {
            m_taskComments = new ArrayList();
        }
        else
        {
            m_taskComments = p_taskComments;
        }
    }

    /**
     * Get a list of task comments
     * 
     * @return a List of Comments for this task.
     */
    public List getTaskComments()
    {
        return m_taskComments;
    }

    /**
     * Get a task comment
     * 
     * @return a Comment for this task.
     */
    public Comment getTaskComment(long commentId)
    {
        Iterator iter = m_taskComments.iterator();
        while (iter.hasNext())
        {
            Comment comment = (Comment) iter.next();
            if (comment.getId() == commentId)
                return comment;
        }
        return null;
    }

    /**
     * Get the source locale.
     * 
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getSourceLocale()
    {
        if (m_workflow == null || m_workflow.getJob() == null)
        {
            return null;
        }
        return m_workflow.getJob().getSourceLocale();
    }

    /**
     * Get the target locale.
     * 
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getTargetLocale()
    {
        if (m_workflow == null)
        {
            return null;
        }
        return m_workflow.getTargetLocale();
    }

    /**
     * Get the name of the project where the workflow belongs to.
     * 
     * @return The project name as String.
     */
    public String getProjectName()
    {
        if (m_workflow == null || m_workflow.getJob() == null
                || m_workflow.getJob().getL10nProfile() == null
                || m_workflow.getJob().getL10nProfile().getProject() == null)
        {
            return "";
        }
        return m_workflow.getJob().getL10nProfile().getProject().getName();
    }

    /**
     * Get the priority of this task (retrieved from the job).
     * 
     * @return The job priority
     */
    public int getPriority()
    {
        int priority = -1;

        if (m_workflow != null && m_workflow.getJob() != null)
        {
            priority = m_workflow.getJob().getPriority();
        }
        return priority;
    }

    /**
     * Get the ID of the job where the workflow belongs to.
     * 
     * @return The job name as long.
     */
    public long getJobId()
    {
        if (m_workflow == null || m_workflow.getJob() == null)
        {
            return -1;
        }

        return m_workflow.getJob().getJobId();
    }

    /**
     * Get the name of the job where the workflow belongs to.
     * 
     * @return The job name as String.
     */
    public String getJobName()
    {
        if (m_workflow == null || m_workflow.getJob() == null)
        {
            return "";
        }

        return m_workflow.getJob().getJobName();
    }

    /**
     * To get the user id of the project manager.
     * 
     * @return String
     */
    public String getProjectManagerId()
    {
        if (m_workflow == null || m_workflow.getJob() == null
                || m_workflow.getJob().getL10nProfile() == null
                || m_workflow.getJob().getL10nProfile().getProject() == null)
        {
            return "";
        }
        return m_workflow.getJob().getL10nProfile().getProject()
                .getProjectManagerId();
    }

    /**
     * To get the name of the project manager.
     * 
     * @return String
     */
    public String getProjectManagerName()
    {
        return m_projectManagerName;
    }

    /**
     * To set the name of the project manager.
     */
    public void setProjectManagerName(String p_pmName)
    {
        m_projectManagerName = p_pmName;
    }

    /**
     * To get the state of the task. The states are defined in constants.
     * 
     * @return int
     */
    public int getState()
    {
        // GBS-3160
        if (m_state == STATE_FINISHING)
        {
            return m_state;
        }
        if (m_wfTaskInstance != null)
        {
        	return m_wfTaskInstance.getTaskStateForAssignee();
        }

        return m_state;
    }

    /**
     * Get the state of the task as a string.
     */
    public String getStateAsString()
    {
        return getStateAsString(m_state);
    }

    public static String getStateAsString(int p_state)
    {
        String state = STATE_DEACTIVE_STR;
        switch (p_state)
        {
            case -1:
                state = STATE_COMPLETED_STR;
                break;
            case 8:
                state = STATE_ACCEPTED_STR;
                break;
            case 81:
                state = STATE_DISPATCHED_TO_TRANSLATION_STR;
                break;
            case 82:
                state = STATE_IN_TRANSLATION_STR;
                break;
            case 83:
                state = STATE_TRANSLATION_COMPLETED_STR;
                break;
            case 84:
                state = STATE_REDEAY_DISPATCH_GSEDTION_STR;
                break;
            case 3:
                state = STATE_ACTIVE_STR;
                break;
            case 4:
                state = STATE_DEACTIVE_STR;
                break;
            case 10:
                state = STATE_FINISHING_STR;
                break;
            default:
                // already set to DEACTIVE
                break;
        }
        return state;
    }

    public static int getStateAsInt(String p_state)
    {
        int state = STATE_DEACTIVE;

        if (p_state.equals(STATE_ACTIVE_STR))
        {
            state = STATE_ACTIVE;
        }
        else if (p_state.equals(STATE_ACCEPTED_STR))
        {
            state = STATE_ACCEPTED;
        }
        else if (p_state.equals(STATE_DISPATCHED_TO_TRANSLATION_STR))
        {
            state = STATE_DISPATCHED_TO_TRANSLATION;
        }
        else if (p_state.equals(STATE_IN_TRANSLATION_STR))
        {
            state = STATE_IN_TRANSLATION;
        }
        else if (p_state.equals(STATE_TRANSLATION_COMPLETED_STR))
        {
            state = STATE_TRANSLATION_COMPLETED;
        }
        else if (p_state.equals(STATE_REDEAY_DISPATCH_GSEDTION_STR))
        {
            state = STATE_REDEAY_DISPATCH_GSEDTION;
        }
        else if (p_state.equals(STATE_COMPLETED_STR))
        {
            state = STATE_COMPLETED;
        }
        else if (p_state.equals(STATE_DEACTIVE_STR))
        {
            state = STATE_DEACTIVE;
        }
        else if (p_state.equals(STATE_FINISHING_STR))
        {
            state = STATE_FINISHING;
        }
        return state;
    }

    /**
     * Set the state of the task to be the specified value.
     */
    public void setState(int p_state)
    {
        m_state = p_state;
        if(m_state == STATE_ACTIVE)
        {
        	setIsReportUploaded(0);
        }
    }

    /**
     * To get the name of the task.
     * 
     * @return String
     */
    public String getTaskName()
    {
        return getName();
    }

    public void setTaskName(String p_taskName)
    {
        m_name = p_taskName;
    }

    /**
     * To get the name of the Activity associated the task.
     * 
     * @return String
     */
    public String getTaskDisplayName()
    {
        String displayName = getName();
        try
        {
            Activity act = (Activity) ServerProxy.getJobHandler().getActivity(
                    getName());
            displayName = act.getDisplayName();
        }
        catch (Exception e)
        {

        }
        return displayName;
    }

    /**
     * To set a WorkflowTaskInstance to a Task.
     * 
     * @param p_wfTaskInstance
     *            the WorkflowTaskInstance to add.
     */
    public void setWorkflowTask(WorkflowTaskInstance p_wfTaskInstance)
    {
        m_wfTaskInstance = p_wfTaskInstance;
    }

    /**
     * @return String representation of the object.
     */
    public String toString()
    {
        // m_taskComments.size();
        return super.toString()
                + " TaskName="
                + (getTaskName() != null ? getTaskName() : "null")
                + ", ProjectName="
                + (getProjectName() != null ? getProjectName() : "null")
                + ", DueDate="
                + (getEstimatedCompletionDate())
                + ", ProjectManagerId="
                + (getProjectManagerId() != null ? getProjectManagerId()
                        : "null")
                + ", ProjectManagerName="
                + (getProjectManagerName() != null ? getProjectManagerName()
                        : "null")
                + ", CompletedDate="
                + (getCompletedDateAsString() != null ? getCompletedDateAsString()
                        : "null")
                + ", AcceptedDate="
                + (getAcceptedDateAsString() != null ? getAcceptedDateAsString()
                        : "null")
                + ", SourceLocale="
                + (getSourceLocale() != null ? getSourceLocale()
                        .toDebugString() : "null")
                + ", State="
                + getState()
                + ", TargetLocale="
                + (getTargetLocale() != null ? getTargetLocale()
                        .toDebugString() : "null")
                + ", TaskComments="
                + (getTaskComments() != null ? getTaskComments() : "null")
                + ", Workflow="
                + (getWorkflow() != null ? getWorkflow().getIdAsLong()
                        .toString() : "null")
                + "\nWorkflowTaskInstance="
                + (m_wfTaskInstance != null ? m_wfTaskInstance.toDebugString()
                        : "null")
                + ", m_rateSelectionCriteria="
                + (m_rateSelectionCriteria)
                + ", m_expenseRate="
                + (m_expenseRate == null ? "No rate" : m_expenseRate.toString())
                + ", m_revenueRate="
                + (m_revenueRate == null ? "No rate" : m_revenueRate.toString())
                + ", m_work="
                + (m_work == null ? "No amount of work " : m_work.toString())
                + ", m_taskType=" + m_taskType + "\n";
    }

    /**
     * Get the Workflow this Task links to.
     * 
     * @return a Workflow object.
     */
    public Workflow getWorkflow()
    {
        return m_workflow;
    }

    public void setWorkflow(Workflow p_workflow)
    {
        m_workflow = p_workflow;
    }

    /**
     * Get the time stamp the task was completed by the user as a String.
     * 
     * @return String of "Completed date" timestamp.
     */
    public String getCompletedOnAsString()
    {
        if (m_completedDate == null)
        {
            return "";
        }
        return DateHelper.getFormattedDateAndTime(m_completedDate, null);
    }

    /**
     * Get a list of assignees for an active node. If the node isn't active this
     * won't be set yet.
     * 
     * @return A list of assignees.
     */
    public List getAllAssignees()
    {
        if (m_wfTaskInstance == null)
        {
            return null;
        }
        return m_wfTaskInstance.getAllAssignees();
    }

    /**
     * Get a list of assignees separated by comma. This is only for an active
     * node.
     * 
     * @return A list of assignees as one string.
     */
    public String getAllAssigneesAsString()
    {
        if (m_wfTaskInstance == null)
        {
            return "";
        }
        Vector allAssignees = m_wfTaskInstance.getAllAssignees();
        int sz = allAssignees.size();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sz; i++)
        {
            sb.append(UserUtil.getUserNameById((String) allAssignees
                    .elementAt(i)));
            if (i < sz - 1)
            {
                sb.append("<BR>");
            }
        }
        return sb.toString();
    }

    /**
     * This method is to be used for task's that aren't active yet. It returns
     * the specifically chosen assignee's full name or "All Qualified Users" if
     * any of the "qualified" users could be chosen when the task becomes
     * active.
     */
    public String getPossibleAssignee()
    {
        String assignee = "";
        if (m_wfTaskInstance != null)
        {
            assignee = m_wfTaskInstance.getDisplayRoleName();
        }
        return assignee;
    }

    /**
     * Get the list of target node info for a condition node ONLY.
     * 
     * @return A list of ConditionNodeTargetInfo objects for a condition node.
     *         Otherwise, returns null.
     */
    public List getConditionNodeTargetInfos()
    {
        if (m_wfTaskInstance == null)
        {
            return null;
        }
        return m_wfTaskInstance.getConditionNodeTargetInfos();
    }

    /**
     * Get the role of the task.
     * 
     * @return The string representation of the activity role.
     */
    public String[] getActivityRoles()
    {
        return m_wfTaskInstance == null ? null : m_wfTaskInstance.getRoles();
    }

    /**
     * @see Task.getActivityRolesAsString()
     */
    public String getActivityRolesAsString()
    {
        return m_wfTaskInstance == null ? null : m_wfTaskInstance
                .getRolesAsString();
    }

    /**
     * @see Task.getExpenseRate()
     */
    public Rate getExpenseRate()
    {
        return m_expenseRate;
    }

    /**
     * @see Task.setExpenseRate(Rate)
     */
    public void setExpenseRate(Rate p_rate)
    {
        m_expenseRate = p_rate;
    }

    /**
     * @see Task.getRevenueRate()
     */
    public Rate getRevenueRate()
    {
        return m_revenueRate;
    }

    /**
     * @see Task.setRevenueRate(Rate)
     */
    public void setRevenueRate(Rate p_rate)
    {
        m_revenueRate = p_rate;
    }

    /**
     * @see Task.setAmountOfWork(AmountOfWork)
     */
    public void setAmountOfWork(AmountOfWork p_work)
    {
        ensureWorkIsCalculated();
        if (p_work != null)
        {
            p_work.setTask(this);
            m_work.put(p_work.getUnitOfWork(), p_work);
            m_workSet.add(p_work);
        }
    }

    /**
     * @see Task.removeAmountOfWork(Integer)
     */
    public void removeAmountOfWork(Integer p_unitOfWork)
    {
        ensureWorkIsCalculated();
        m_workSet.remove(m_work.get(p_unitOfWork));
        m_work.remove(p_unitOfWork);

    }

    /**
     * @see Task.removeAmountOfWork()
     */
    public void removeAmountOfWork()
    {
        m_work.clear();
        m_workSet.clear();
        m_workNeedsCalculating = false;
    }

    /**
     * @see Task.getAmountOfWork(int)
     */
    public AmountOfWork getAmountOfWork(Integer p_unitOfWork)
    {
        ensureWorkIsCalculated();
        return (AmountOfWork) m_work.get(p_unitOfWork);
    }

    /**
     * @see Task.getStfCreationState().
     */
    public String getStfCreationState()
    {
        return m_stfCreationState;
    }

    /**
     * @see Task.setStfCreationState(String).
     */
    public void setStfCreationState(String p_stfCreationState)
    {
        m_stfCreationState = p_stfCreationState;
    }

    /**
     * @see Task.getAcceptor()
     */
    public String getAcceptor()
    {
        return m_acceptor;
    }

    /**
     * @see Task.setAcceptor
     */
    public void setAcceptor(String p_user)
    {
        m_acceptor = p_user;
    }

    /**
     * @see Task.getRateSelectionCriteria()
     */
    public int getRateSelectionCriteria()
    {
        return m_rateSelectionCriteria;
    }

    /**
     * @see Task.setRateSelectionCriteria
     */
    public void setRateSelectionCriteria(int p_criteria)
    {
        m_rateSelectionCriteria = p_criteria;
    }

    /**
     * @see Task.getEstimatedAcceptanceDate()
     */
	public Date getEstimatedAcceptanceDate()
	{
		if (m_workflow != null
				&& m_workflow.isEstimatedCompletionDateOverrided())
		{
			m_estimatedAcceptanceDate = m_workflow.getEstimatedCompletionDate();
		}
		return m_estimatedAcceptanceDate;
	}

    /**
     * @see Task.getEstimatedCompletionDate()
     */
    public Date getEstimatedCompletionDate()
	{
		if (m_workflow != null
				&& m_workflow.isEstimatedCompletionDateOverrided())
		{
			m_estimatedCompletionDate = m_workflow.getEstimatedCompletionDate();
		}
		return m_estimatedCompletionDate;
	}

    /**
     * @see Task.setEstimatedAcceptanceDate(Date)
     */
    public void setEstimatedAcceptanceDate(Date p_estimatedAcceptanceDate)
    {
        m_estimatedAcceptanceDate = p_estimatedAcceptanceDate;
    }

    /**
     * @see Task.setEstimatedCompletionDate(Date)
     */
    public void setEstimatedCompletionDate(Date p_estimatedCompletionDate)
    {
        m_estimatedCompletionDate = p_estimatedCompletionDate;
    }

    /**
     * @see Task.getRatings()
     */
    public List getRatings()
    {
        return m_ratings;
    }

    /**
     * @see Task.addRating(Rating)
     */
    public void addRating(Rating p_rating)
    {
        m_ratings.add(p_rating);
    }

    /**
     * @see Task.removeRating(Rating)
     */
    public void removeRating(Rating p_rating)
    {
        m_ratings.remove(p_rating);
    }

    public boolean isAccepted()
    {
        return m_acceptor != null;
    }

    /**
     * @see Task.isType(int)
     */
    public boolean isType(int p_type)
    {
        if (TYPE_REVIEW_EDITABLE == p_type)
        {
            try
            {
                Activity a = ServerProxy.getJobHandler().getActivity(getName());
                return TYPE_REVIEW == a.getType() && a.getIsEditable();
            }
            catch (Exception e)
            {
                return false;
            }
        }

        return m_type == p_type;
    }
    
    public boolean isReviewOnly()
    {
        try
        {
            Activity a = ServerProxy.getJobHandler().getActivity(getName());
            return TYPE_REVIEW == a.getType() && !a.getIsEditable();
        }
        catch (Exception e)
        {
            return false;
        }

    }

    /**
     * @see Type.getType()
     */
    public int getType()
    {
        return m_type;
    }

    /**
     * @see setType(int)
     */
    public void setType(int p_type)
    {
        if (Activity.isValidType(p_type))
        {
            m_type = p_type;
        }
        // for sla report issue
        else if (Task.TYPE_REVIEW_EDITABLE == p_type)
        {
            m_type = TYPE_TRANSLATE;
        }
    }

    public WorkflowTaskInstance getWorkflowTask()
    {
        return m_wfTaskInstance;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Task Implementation
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package-scope Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * This method is overwritten for TOPLink. TOPLink doesn't query all
     * collections of objects within an object. So if a Profile is serialized -
     * the TM information and WorkflowInfo information may not be available
     * (because they haven't been queried yet). Overwriting the method forces
     * the query to happen so when it is serialized all pieces of the object are
     * serialized and availble to the client.
     */
    protected void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException
    {
        // touch task comments - since they are set up to only
        // populate when needed

        m_taskComments.size();

        // call the default writeObject
        out.defaultWriteObject();
    }

    public String getTaskType()
    {
        return m_taskType;
    }

    public void setTaskType(String p_taskType)
    {
        m_taskType = p_taskType;
    }

    // Use by Hibernate
    public void setStateStr(String p_state)
    {
        m_state = getStateAsInt(p_state);
        if(m_state == STATE_ACTIVE)
        {
        	setIsReportUploaded(0);
        }
    }

    public String getStateStr()
    {
        return getStateAsString(m_state);
    }

    public void setTypeStr(String typeStr)
    {
        m_type = Activity.typeAsInt(typeStr);
    }

    public String getTypeStr()
    {
        return Activity.typeAsString(m_type);
    }

    public void setRatings(List m_ratings)
    {
        this.m_ratings = m_ratings;
    }

    public Set<AmountOfWork> getWorkSet()
    {

        return m_workSet;
    }

    public void setWorkSet(Set<AmountOfWork> p_workSet)
    {
        this.m_workSet = (p_workSet == null) ? new HashSet<AmountOfWork>()
                : p_workSet;
        m_workNeedsCalculating = true;
    }

    private void ensureWorkIsCalculated()
    {
        if (m_workNeedsCalculating)
        {
            m_work = new Hashtable();
            for (AmountOfWork work : m_workSet)
            {
                if (work != null)
                {
                    work.setTask(this);
                    m_work.put(work.getUnitOfWork(), work);
                }
            }
            m_workNeedsCalculating = false;
        }
    }

    /**
     * Return this task can be reassign or not.
     * 
     * @return
     */
    public boolean reassignable()
    {
        return ASSIGNABLE_STATE.contains(getStateAsString());
    }

    /**
     * Gets the duration of the task.<br>
     * 
     * if the task has not been accepted, the duration is -1. If the task has
     * been accepted, the duration equals to the complete date minus the accept
     * date. When the task has not been completed, the duration equasl to the
     * current date minus the accept date.
     * 
     * @return long
     */
    public String getActualDuration()
    {
        if (m_acceptedDate == null)
        {
            return EMPTY_DATE;
        }

        long accetpDate = m_acceptedDate.getTime();
        long duration = m_completedDate == null ? new Date().getTime()
                - accetpDate : m_completedDate.getTime() - accetpDate;

        return DateHelper.daysHoursMinutes(duration, "d", "h", "m");
    }

    public Set getTaskTuvs()
    {
        return taskTuvs;
    }

    public void setTaskTuvs(Set taskTuvs)
    {
        this.taskTuvs = taskTuvs;
    }

    @Override
    public void setQualityAssessment(String qualityAssessment)
    {
       this.m_qualityAssessment = qualityAssessment; 
    }

    @Override
    public String getQualityAssessment()
    {
        return m_qualityAssessment;
    }

    @Override
    public void setMarketSuitability(String marketSuitabilty)
    {
        this.m_marketSuitability = marketSuitabilty;
    }

    @Override
    public String getMarketSuitability()
    {
        return m_marketSuitability;
    }
}
