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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.jobhandler.JobImpl;
import com.globalsight.everest.page.PageState;
import com.globalsight.everest.page.TargetPage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.WorkflowTypeConstants;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.secondarytargetfile.SecondaryTargetFile;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.Task;
import com.globalsight.everest.taskmanager.TaskInfo;
import com.globalsight.everest.util.comparator.PageComparator;
import com.globalsight.everest.util.system.SystemConfigParamNames;
import com.globalsight.everest.util.system.SystemConfiguration;
import com.globalsight.everest.workflow.Activity;
import com.globalsight.everest.workflow.WorkflowInstance;
import com.globalsight.util.GeneralException;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.SortUtil;

/**
 * Need to include "WorkObject" here also - as well as in Workflow.java TL can't
 * seem to find it from the interface Workflow.
 */
public class WorkflowImpl extends PersistentObject implements Workflow,
        WorkObject
{
    private static final long serialVersionUID = -9074840785216382158L;

    private static Logger c_logger = Logger.getLogger(WorkflowImpl.class
            .getName());

    public static final String STATE = "m_state";

    public static final String JOB = "m_job";

    public static final String M_TARGET_LOCALE = "m_targetLocale";

    public static final String M_ESTIMATED_COMPLETION_DATE = "m_estimatedCompletionDate";

    public static final String M_WORKFLOW_OWNERS = "m_workflowOwners";

    public static final String SLASH = "/";

    public static final String NUMERATOR_INITIAL = "0";

    public static final String DENOMINATOR_INITIAL = "0";

    // Workflow type constants
    public static final String TYPE_TRANSLATION = WorkflowTypeConstants.TYPE_TRANSLATION;

    public static final String TYPE_DTP = WorkflowTypeConstants.TYPE_DTP;

    private long m_wfInstanceId;

    private Job m_job;

    private String m_state;

    private String m_fraction = NUMERATOR_INITIAL + SLASH + DENOMINATOR_INITIAL;

    private GlobalSightLocale m_targetLocale;

    private WorkflowInstance m_wfInstance;

    private Date m_dispatchedDate = null;

    private Date exportDate = null;

    private Date m_estimatedCompletionDate = null;

    private Date m_completedDate = null;

    private Set<Task> m_tasks = new HashSet<Task>();
    private Hashtable<Long, Task> m_taskMap = new Hashtable<Long, Task>();
    private boolean m_taskMapNeedsBuilding = true;

    private Set<TargetPage> m_targetPages = new HashSet<TargetPage>();
    private Vector<TargetPage> m_sortedTargetPages = new Vector<TargetPage>();
    // Do the target pages need sorting?
    private boolean m_needsSorting = true;

    private Set<SecondaryTargetFile> m_secondaryTargetFiles = new HashSet<SecondaryTargetFile>();

    private String m_dispatchType;

    // the duration of the workflow in days
    private Long m_duration = new Long(0L);

    private Integer contextMatchWordCount = new Integer(0);
    private Integer segmentTmWordCount = new Integer(0);
    private Integer incontextMatchWordCount = new Integer(0);
    private Integer mtExactMatchWordCount = new Integer(0);

    /**
     * This includes ALL exact match word counts(segment-TM,context,MT,XLF and
     * PO exact matches etc).
     */
    private Integer totalExactMatchWordCount = new Integer(0);
    private Integer noUseInContextMatchWordCount = new Integer(0);

    private Integer m_lowFuzzyMatchWordCount = new Integer(0);
    private Integer m_medFuzzyMatchWordCount = new Integer(0);
    private Integer m_medHiFuzzyMatchWordCount = new Integer(0);
    private Integer m_hiFuzzyMatchWordCount = new Integer(0);
    private Integer m_noMatchWordCount = new Integer(0);
    private Integer m_totalRepetitionWordCount = new Integer(0);
    private Integer m_totalWordCount = new Integer(0);

    // When use MT,
    // If MT confidence score is 100, mtTotalWordCount =
    // m_MTExactMatchWordCount, mtFuzzyNoMatchWordCount and
    // mtRepetitionsWordCount are 0 now.
    // If MT confidence score is < 100, mtTotalWordCount =
    // mtFuzzyNoMatchWordCount + mtRepetitionsWordCount, m_MTExactMatchWordCount
    // is 0 now.
    private Integer mtTotalWordCount = new Integer(0);
    private Integer mtFuzzyNoMatchWordCount = new Integer(0);
    private Integer mtRepetitionsWordCount = new Integer(0);
    // Word count MT engine "thinks".
    private Integer mtEngineWordCount = new Integer(0);

    private Integer m_thresholdHiFuzzyWordCount = new Integer(0);

    private Integer m_thresholdMedHiFuzzyWordCount = new Integer(0);

    private Integer m_thresholdMedFuzzyWordCount = new Integer(0);

    private Integer m_thresholdLowFuzzyWordCount = new Integer(0);

    private Integer m_thresholdNoMatchWordCount = new Integer(0);

    private Date m_plannedCompletionDate = null;

    private Set<WorkflowOwner> m_workflowOwners = new HashSet<WorkflowOwner>();

    private Set<Comment> m_workflowComments = new HashSet<Comment>();

    private List m_sortedWorkflowComments;

    private boolean m_workflowCommentsNeedSorting = true;

    // id of the company which this activity belong to
    private long m_companyId;

    // For sla report issue
    // User can override the estimatedCompletionDate
    private boolean m_isEstimatedCompletionDateOverrided = false;

    private boolean m_isEstimatedTranslateCompletionDateOverrided = false;

    private Date m_estimatedTranslateCompletionDate = null;

    private Date m_translationCompletedDate = null;

    // The translate activity in workflow
    private Task m_TranslateActivity = null;

    private int m_priority;

    private boolean useMT = false;
    private int mtConfidenceScore = 0;
    private String mtProfileName = null;
    private String scorecardComment;
    private int scorecardShowType = -1;//-1:Not Showing,0:Optional,1:Required  

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

    private String m_workflowType = TYPE_TRANSLATION;

    public WorkflowImpl()
    {
    }

    /**
     * This method sets the iflow instance
     * 
     * @param WorkflowInstance
     *            p_wfInstance
     */
    public void setIflowInstance(WorkflowInstance p_wfInstance)
    {
        m_wfInstance = p_wfInstance;
    }

    /**
     * Return the iflow instance associated with this workflow object
     * 
     * @return WorkflowInstance
     */
    public WorkflowInstance getIflowInstance()
    {
        return m_wfInstance;
    }

    /**
     * Add a target page to this workflow
     * 
     * @param TargetPage
     *            p_targetPage
     */
    public void addTargetPage(TargetPage p_targetPage)
    {
        p_targetPage.setWorkflowInstance(this);
        m_targetPages.add(p_targetPage);
        m_needsSorting = true;
    }

    /**
     * @see Workflow.getTargetPgaes(int)
     */
    public ArrayList getTargetPages(int p_primaryFileType)
    {
        sortTargetPages();
        ArrayList targetPages = new ArrayList();
        // loop through the current target page collection
        // and gather ones of the particular type
        for (TargetPage targetPage : m_sortedTargetPages)
        {
            if ((targetPage.getPrimaryFileType() == p_primaryFileType)
                    && (!targetPage.getPageState()
                            .equals(PageState.IMPORT_FAIL)))
            {
                targetPages.add(targetPage);
            }
        }
        return targetPages;
    }

    /**
     * @see Workflow.getTargetPages()
     */
    public Vector<TargetPage> getTargetPages()
    {
        sortTargetPages();
        Vector<TargetPage> tps = new Vector<TargetPage>();
        for (TargetPage tp : m_sortedTargetPages)
        {
            // if the target page is not of an IMPORT_FAIL add to list
            if (!tp.getPageState().equals(PageState.IMPORT_FAIL))
            {
                tps.add(tp);
            }
        }
        return tps;
    }

    /**
     * @see Workflow.getAllTargetPages
     */
    public Vector<TargetPage> getAllTargetPages()
    {
        sortTargetPages();
        return m_sortedTargetPages;
    }

    /**
     * @see Workflow.getSecondaryTargetFiles()
     */
    public Set<SecondaryTargetFile> getSecondaryTargetFiles()
    {
        return getSecondaryTargets();
    }

    public Set<SecondaryTargetFile> getSecondaryTargets()
    {
        return m_secondaryTargetFiles;
    }

    public void setSecondaryTargets(Set<SecondaryTargetFile> targets)
    {
        m_secondaryTargetFiles = targets;
    }

    /**
     * @see Workflow.addSecondaryTargetFile(SecondaryTargetFile)
     */
    public void addSecondaryTargetFile(SecondaryTargetFile p_stf)
    {
        p_stf.setWorkflow(this);
        m_secondaryTargetFiles.add(p_stf);
    }

    /**
     * Get a collection of task objects for accessing additional info not stored
     * in iFlow. The key is the task id and the value is the task object itself.
     * 
     * @return A map of tasks.
     */
    public Hashtable getTasks()
    {
        buildTaskMap();
        return m_taskMap;
    }

    public Set getTaskSet()
    {
        return m_tasks;
    }

    public void setTaskSet(Set tasks)
    {
        m_tasks = tasks;
        m_taskMapNeedsBuilding = true;
    }

    private void buildTaskMap()
    {
        if (m_taskMapNeedsBuilding)
        {
            m_taskMap = new Hashtable<Long, Task>();
            for (Task task : m_tasks)
            {
                m_taskMap.put(task.getId(), task);
            }
            m_taskMapNeedsBuilding = false;
        }
    }

    /**
     * Add a task to this workflow.
     * 
     * @param p_task
     *            The task to be added to this workflow.
     */
    public void addTask(Task p_task)
    {
        m_tasks.add(p_task);
        m_taskMapNeedsBuilding = true;
    }

    /**
     * Remove a task from this workflow.
     * 
     * @param p_task
     *            The task to be removed from this workflow.
     */
    public void removeTask(Task p_task)
    {
        if (p_task != null)
        {
            m_tasks.remove(p_task);
            p_task.setWorkflow(null);
            p_task.removeAmountOfWork();
            m_taskMapNeedsBuilding = true;
        }
    }

    /**
     * The date the workflow was completed. Returns NULL if not completed yet.
     */
    public Date getCompletedDate()
    {
        return m_completedDate;
    }

    /*
     * @see Workflow.getDuration()
     */
    public long getDuration()
    {
        return m_duration.longValue();
    }

    /*
     * @see Workflow.setDuration(long)
     */
    public void setDuration(long p_duration)
    {
        m_duration = new Long(p_duration);
    }

    /*
     * @see Workflow.getDuration()
     */
    public Long getDurationAsLong()
    {
        return m_duration;
    }

    /*
     * @see Workflow.setDuration(long)
     */
    public void setDurationAsLong(Long p_duration)
    {
        m_duration = p_duration;
    }

    /*
     * @see Workflow.setContextMatchWordCount()
     */
    public void setContextMatchWordCount(int p_contextMatchWordCount)
    {
        contextMatchWordCount = new Integer(p_contextMatchWordCount);
    }

    /*
     * @see Workflow.getContextMatchWordCount()
     */
    public int getContextMatchWordCount()
    {
        return contextMatchWordCount == null ? 0 : contextMatchWordCount
                .intValue();
    }

    public void setContextMatchWordCountAsInteger(
            Integer p_contextMatchWordCount)
    {
        contextMatchWordCount = p_contextMatchWordCount;
    }

    public Integer getContextMatchWordCountAsInteger()
    {
        return contextMatchWordCount;
    }

    /*
     * @see Workflow.setSegmentTmWordCount()
     */
    public void setSegmentTmWordCount(int p_segmentTmWordCount)
    {
        segmentTmWordCount = new Integer(p_segmentTmWordCount);
    }

    /*
     * @see Workflow.getSegmentTmWordCount()
     */
    public int getSegmentTmWordCount()
    {
        return segmentTmWordCount == null ? 0 : segmentTmWordCount.intValue();
    }

    public void setSegmentTmWordCountAsInteger(Integer p_segmentTmWordCount)
    {
        segmentTmWordCount = p_segmentTmWordCount;
    }

    public Integer getSegmentTmWordCountAsInteger()
    {
        return segmentTmWordCount;
    }

    /*
     * @see Workflow.setLowFuzzyMatchWordCount()
     */
    public void setLowFuzzyMatchWordCount(int p_lowFuzzyMatchWordCount)
    {
        m_lowFuzzyMatchWordCount = new Integer(p_lowFuzzyMatchWordCount);
    }

    /*
     * @see Workflow.getLowFuzzyMatchWordCount()
     */
    public int getLowFuzzyMatchWordCount()
    {
        return m_lowFuzzyMatchWordCount == null ? 0 : m_lowFuzzyMatchWordCount
                .intValue();
    }

    public void setLowFuzzyMatchWordCountAsInteger(
            Integer p_lowFuzzyMatchWordCount)
    {
        m_lowFuzzyMatchWordCount = p_lowFuzzyMatchWordCount;
    }

    public Integer getLowFuzzyMatchWordCountAsInteger()
    {
        return m_lowFuzzyMatchWordCount;
    }

    /*
     * @see Workflow.setMedFuzzyMatchWordCount()
     */
    public void setMedFuzzyMatchWordCount(int p_medFuzzyMatchWordCount)
    {
        m_medFuzzyMatchWordCount = new Integer(p_medFuzzyMatchWordCount);
    }

    /*
     * @see Workflow.getMedFuzzyMatchWordCount()
     */
    public int getMedFuzzyMatchWordCount()
    {
        return m_medFuzzyMatchWordCount == null ? 0 : m_medFuzzyMatchWordCount
                .intValue();
    }

    public void setMedFuzzyMatchWordCountAsInteger(
            Integer p_medFuzzyMatchWordCount)
    {
        m_medFuzzyMatchWordCount = p_medFuzzyMatchWordCount;
    }

    public Integer getMedFuzzyMatchWordCountAsInteger()
    {
        return m_medFuzzyMatchWordCount;
    }

    /*
     * @see Workflow.setMedHiFuzzyMatchWordCount()
     */
    public void setMedHiFuzzyMatchWordCount(int p_medHiFuzzyMatchWordCount)
    {
        m_medHiFuzzyMatchWordCount = new Integer(p_medHiFuzzyMatchWordCount);
    }

    /*
     * @see Workflow.getMedHiFuzzyMatchWordCount()
     */
    public int getMedHiFuzzyMatchWordCount()
    {
        return m_medHiFuzzyMatchWordCount == null ? 0
                : m_medHiFuzzyMatchWordCount.intValue();
    }

    public void setMedHiFuzzyMatchWordCountAsInteger(
            Integer p_medHiFuzzyMatchWordCount)
    {
        m_medHiFuzzyMatchWordCount = p_medHiFuzzyMatchWordCount;
    }

    public Integer getMedHiFuzzyMatchWordCountAsInteger()
    {
        return m_medHiFuzzyMatchWordCount;
    }

    /*
     * @see Workflow.setHiFuzzyMatchWordCount()
     */
    public void setHiFuzzyMatchWordCount(int p_hiFuzzyMatchWordCount)
    {
        m_hiFuzzyMatchWordCount = new Integer(p_hiFuzzyMatchWordCount);
    }

    /*
     * @see Workflow.getHiFuzzyMatchWordCount()
     */
    public int getHiFuzzyMatchWordCount()
    {
        return m_hiFuzzyMatchWordCount == null ? 0 : m_hiFuzzyMatchWordCount
                .intValue();
    }

    public void setInContextMatchWordCount(int inContextMatchWord)
    {
        this.incontextMatchWordCount = new Integer(inContextMatchWord);
    }

    public int getInContextMatchWordCount()
    {
        return this.incontextMatchWordCount == null ? 0
                : incontextMatchWordCount.intValue();
    }

    public void setMtExactMatchWordCount(int mtExactMatchWord)
    {
        this.mtExactMatchWordCount = mtExactMatchWord;
    }

    public int getMtExactMatchWordCount()
    {
        return this.mtExactMatchWordCount == null ? 0 : mtExactMatchWordCount
                .intValue();
    }

    public void setNoUseInContextMatchWordCountAsInteger(
            Integer noUseInContextMatchWordCount)
    {
        this.noUseInContextMatchWordCount = noUseInContextMatchWordCount;
    }

    public void setNoUseInContextMatchWordCount(int noUseInContextMatchWordCount)
    {
        this.noUseInContextMatchWordCount = new Integer(
                noUseInContextMatchWordCount);
    }

    public Integer getNoUseInContextMatchWordCountAsInteger()
    {
        return this.noUseInContextMatchWordCount == null ? 0
                : noUseInContextMatchWordCount;
    }

    public int getNoUseInContextMatchWordCount()
    {
        return this.noUseInContextMatchWordCount == null ? 0
                : noUseInContextMatchWordCount.intValue();
    }

    public void setTotalExactMatchWordCountAsInteger(
            Integer totalExactMatchWordCount)
    {
        this.totalExactMatchWordCount = totalExactMatchWordCount;
    }

    public void setTotalExactMatchWordCount(int totalExactMatchWordCount)
    {
        this.totalExactMatchWordCount = new Integer(totalExactMatchWordCount);
    }

    public Integer getTotalExactMatchWordCountAsInteger()
    {
        return this.totalExactMatchWordCount == null ? 0
                : totalExactMatchWordCount;
    }

    public int getTotalExactMatchWordCount()
    {
        return this.totalExactMatchWordCount == null ? 0
                : totalExactMatchWordCount.intValue();
    }

    public void setInContextMatchWordCountAsInteger(Integer inContextMatchWord)
    {
        incontextMatchWordCount = inContextMatchWord;
    }

    public Integer getInContextMatchWordCountAsInteger()
    {
        return incontextMatchWordCount;
    }

    public void setMtExactMatchWordCountAsInteger(Integer mtExactMatchWord)
    {
        this.mtExactMatchWordCount = mtExactMatchWord;
    }

    public Integer getMtExactMatchWordCountAsInteger()
    {
        return this.mtExactMatchWordCount;
    }

    public void setHiFuzzyMatchWordCountAsInteger(
            Integer p_hiFuzzyMatchWordCount)
    {
        m_hiFuzzyMatchWordCount = p_hiFuzzyMatchWordCount;
    }

    public Integer getHiFuzzyMatchWordCountAsInteger()
    {
        return m_hiFuzzyMatchWordCount;
    }

    /*
     * @see Workflow.setNoMatchWordCount()
     */
    public void setNoMatchWordCount(int p_totalNoMatchWordCount)
    {
        m_noMatchWordCount = new Integer(p_totalNoMatchWordCount);
    }

    /*
     * @see Workflow.getNoMatchWordCount()
     */
    public int getNoMatchWordCount()
    {
        int noMatchWordCount = 0;
        if (m_noMatchWordCount != null)
        {
            noMatchWordCount = m_noMatchWordCount.intValue();
        }
        return noMatchWordCount;
    }

    public void setNoMatchWordCountAsInteger(Integer p_totalNoMatchWordCount)
    {
        m_noMatchWordCount = p_totalNoMatchWordCount;
    }

    public Integer getNoMatchWordCountAsInteger()
    {
        return m_noMatchWordCount;
    }

    /*
     * @see Workflow.setRepetitionWordCount()
     */
    public void setRepetitionWordCount(int p_totalRepetitionWordCount)
    {
        m_totalRepetitionWordCount = new Integer(p_totalRepetitionWordCount);
    }

    /*
     * @see Workflow.getRepetitionWordCount()
     */
    public int getRepetitionWordCount()
    {
        if (m_totalRepetitionWordCount != null)
        {
            return m_totalRepetitionWordCount.intValue();
        }

        return 0;
    }

    public void setRepetitionWordCountAsInteger(
            Integer p_totalRepetitionWordCount)
    {
        m_totalRepetitionWordCount = p_totalRepetitionWordCount;
    }

    public Integer getRepetitionWordCountAsInteger()
    {
        return m_totalRepetitionWordCount;
    }

    /*
     * @see Workflow.setTotalWordCount()
     */
    public void setTotalWordCount(int p_totalWordCount)
    {
        m_totalWordCount = new Integer(p_totalWordCount);
    }

    /*
     * @see Workflow.getTotalWordCount()
     */
    public int getTotalWordCount()
    {
        int totalWordCount = 0;
        if (m_totalWordCount != null)
        {
            totalWordCount = m_totalWordCount.intValue();
        }
        return totalWordCount;
    }

    public void setTotalWordCountAsInteger(Integer p_totalWordCount)
    {
        m_totalWordCount = p_totalWordCount;
    }

    public Integer getTotalWordCountAsInteger()
    {
        return m_totalWordCount;
    }

    /**
     * The date the workflow has been estimated to be done. Returns NULL if the
     * workflow hasn't started yet and the due date calculated.
     */
    public Date getDispatchedDate()
    {
        return m_dispatchedDate;
    }

    /**
     * OVERRIDE:
     * 
     * @return long Id
     */
    public long getId()
    {
        return m_wfInstanceId;
    }

    /**
     * OVERRIDE: return the id as a Long
     */
    public Long getIdAsLong()
    {
        return new Long(getId());
    }

    /**
     * @return Job
     */
    public Job getJob()
    {
        return m_job;
    }

    /**
     * @return String
     */
    public String getState()
    {
        return m_state;
    }

    /**
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    /**
     * @return String
     */
    public String getDispatchType()
    {
        Vector rL = new Vector(m_job.getRequestList());
        // Collection rL = m_job.getRequestList();
        Request[] r = new Request[0];
        r = (Request[]) rL.toArray();
        boolean dispatchType = r[0].getL10nProfile().dispatchIsAutomatic();
        if (dispatchType == true)
        {
            m_dispatchType = "AUTOMATIC";
        }
        else
        {
            m_dispatchType = "MANUAL";
        }
        return m_dispatchType;
    }

    /**
     * @param long p_wfInstanceId
     */
    public void setId(long p_wfInstanceId)
    {
        m_wfInstanceId = p_wfInstanceId;
    }

    /**
     * @param Job
     *            p_job
     */
    public void setJob(Job p_job)
    {
        m_job = p_job;
    }

    /**
     * @param Date
     *            p_dispatched Date
     */
    public void setDispatchedDate(Date p_dispatchedDate)
    {
        m_dispatchedDate = p_dispatchedDate;
    }

    /**
     * @return int percentage completion
     */
    public int getPercentageCompletion()
    {
        double num = (double) (getCompletionNumerator());
        double den = (double) ((getCompletionDenominator() != 0 ? getCompletionDenominator()
                : 1));
        return (int) ((num / den) * 100.0);
    }

    public int getCompletionNumerator()
    {
        return Integer.parseInt(m_fraction.substring(0, getSlashIndex()));
    }

    public int getCompletionDenominator()
    {
        return Integer.parseInt(m_fraction.substring(getSlashIndex() + 1));
    }

    private int getSlashIndex()
    {
        return m_fraction.indexOf(SLASH);
    }

    public void setCompletionFraction(int p_numerator, int p_denominator)
    {
        m_fraction = "" + p_numerator + SLASH + p_denominator;
    }

    public String getCompletionFraction()
    {
        return m_fraction;
    }

    /**
     * @param Date
     *            p_completedDate
     */
    public void setCompletedDate(Date p_completedDate)
    {
        m_completedDate = p_completedDate;
    }

    /**
     * @param String
     *            p_state
     */
    public void setState(String p_state)
    {
        m_state = p_state;
    }

    /**
     * @param GlobalSightLocale
     *            p_targetLocale
     */
    public void setTargetLocale(GlobalSightLocale p_targetLocale)
    {
        m_targetLocale = p_targetLocale;
    }

    /**
     * @deprecated For sla report issue.
     * @see Workflow.setPlannedCompletionDate(Date);
     */
    public void setPlannedCompletionDate(Date p_plannedCompletionDate)
    {
        m_plannedCompletionDate = p_plannedCompletionDate;
    }

    /**
     * @deprecated For sla report issue.
     * @see Workflow.getPlannedCompletionDate();
     */
    public Date getPlannedCompletionDate()
    {
        return m_plannedCompletionDate;
    }

    /**
     * @see Workflow.addWorkflowOwner(WorkflowOwner)
     */
    public void addWorkflowOwner(WorkflowOwner p_workflowOwner)
    {
        p_workflowOwner.setWorkflow(this);
        m_workflowOwners.add(p_workflowOwner);
    }

    public void setWorkflowOwnerSet(Set owners)
    {
        m_workflowOwners = owners;
    }

    /**
     * @see Workflow.getWorkflowOwners()
     */
    public List getWorkflowOwners()
    {
        return new ArrayList<WorkflowOwner>(m_workflowOwners);
    }

    public Set<WorkflowOwner> getWorkflowOwnerSet()
    {
        return m_workflowOwners;
    }

    /**
     * @see Workflow.removeWorkflowOwner(WorkflowOwner)
     */
    public void removeWorkflowOwner(WorkflowOwner p_workflowOwner)
    {
        if (p_workflowOwner != null)
        {
            m_workflowOwners.remove(p_workflowOwner);
        }
    }

    /**
     * @see Workflow.getWorkflowOwnerIds()
     */
    public List<String> getWorkflowOwnerIds()
    {
        List<String> owners = new ArrayList<String>();
        for (WorkflowOwner wfo : m_workflowOwners)
        {
            owners.add(wfo.getOwnerId());
        }

        return owners;
    }

    /**
     * @see Workflow.getWorkflowOwnersByType()
     */
    public List getWorkflowOwnerIdsByType(String p_ownerType)
    {
        List owners = new ArrayList();
        try
        {
            for (WorkflowOwner wfo : m_workflowOwners)
            {
                if (p_ownerType != null
                        && p_ownerType.equals(wfo.getOwnerType()))
                {
                    owners.add(wfo.getOwnerId());
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("WorkflowImpl.getWorkflowOwnerIdsByType() :: ", e);
        }

        return owners;
    }

    /*
     * This method is overwritten So if Workflow is serialized - the target page
     * information may not be available (because they haven't been queried yet).
     * Overwriting the method forces the query to happen so when it is
     * serialized all pieces of the object are serialized and availble to the
     * client.
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        // We think the 2 lines are not necessary any more (since 8.2.1)
        // m_targetPages.size();
        // m_tasks.size();
        // call the default writeObject
        out.defaultWriteObject();
    }

    /**
     * Add this workflow comment to the collection.
     * 
     * @param p_comment
     *            - The comment to be added.
     */
    public void addWorkflowComment(Comment p_comment)
    {
        m_workflowComments.add(p_comment);
        m_workflowCommentsNeedSorting = true;
    }

    /**
     * Remove this workflow comment from the collection.
     * 
     * @param p_comment
     *            - The comment remove.
     */
    public void removeWorkflowComment(Comment p_comment)
    {
        m_workflowComments.remove(p_comment);
        m_workflowCommentsNeedSorting = true;
    }

    /**
     * Set the workflow comments to be this value.
     * 
     * @param p_comments
     *            - The task comments to be set.
     */
    public void setWorkflowComments(Set<Comment> p_comments)
    {
        m_workflowComments = p_comments;
        m_workflowCommentsNeedSorting = true;
    }

    public void setWorkflowCommentSet(Set<Comment> p_comments)
    {
        setWorkflowComments(p_comments);
    }

    private void sortWorkflowComments()
    {
        try
        {
            SystemConfiguration sc = SystemConfiguration.getInstance();
            if ("desc"
                    .equals((String) sc
                            .getStringParameter(SystemConfigParamNames.COMMENTS_SORTING))
                    || "default"
                            .equals((String) sc
                                    .getStringParameter(SystemConfigParamNames.COMMENTS_SORTING)))
            {
                SortUtil.sort(m_sortedWorkflowComments,
                        Collections.reverseOrder());
            }
            else if ("asc"
                    .equals((String) sc
                            .getStringParameter(SystemConfigParamNames.COMMENTS_SORTING)))
            {
                SortUtil.sort(m_sortedWorkflowComments);
            }
        }
        catch (GeneralException ge)
        {
            c_logger.warn("Could not read system parameter for comment sorting: "
                    + SystemConfigParamNames.COMMENTS_SORTING);
            SortUtil.sort(m_sortedWorkflowComments, Collections.reverseOrder());
        }
    }

    /**
     * Get a list of workflow comments sorted descending by date
     * 
     * @return a List of Comments for this task.
     */
    public List getWorkflowComments()
    {
        if (m_workflowCommentsNeedSorting == true)
        {
            m_sortedWorkflowComments = new ArrayList(m_workflowComments);
            sortWorkflowComments();
            m_workflowCommentsNeedSorting = false;
        }
        return m_sortedWorkflowComments;
    }

    public Set<Comment> getWorkflowCommentSet()
    {
        return m_workflowComments;
    }

    /**
     * @see Workflow.getEstimatedCompletionDate()
     */
    public Date getEstimatedCompletionDate()
    {
        return m_estimatedCompletionDate;
    }

    /**
     * @see Workflow.setEstimatedCompletionDate(Date)
     */
    public void setEstimatedCompletionDate(Date p_estimatedCompletionDate)
    {
        m_estimatedCompletionDate = p_estimatedCompletionDate;
    }

    /**
     * @see Workflow.isEstimatedCompletionDateOverrided()
     */
    public boolean isEstimatedCompletionDateOverrided()
    {
        return m_isEstimatedCompletionDateOverrided;
    }

    public Boolean getIsEstimatedCompletionDateOverrided()
    {
        return Boolean.valueOf(m_isEstimatedCompletionDateOverrided);
    }

    /**
     * @see Workflow.setEstimatedCompletionDateOverrided(boolean)
     */
    public void setEstimatedCompletionDateOverrided(
            boolean p_isEstimatedCompletionDateOverrided)
    {
        m_isEstimatedCompletionDateOverrided = p_isEstimatedCompletionDateOverrided;
    }

    public void setIsEstimatedCompletionDateOverrided(
            Boolean p_isEstimatedCompletionDateOverrided)
    {
        if (p_isEstimatedCompletionDateOverrided != null)
        {
            m_isEstimatedCompletionDateOverrided = p_isEstimatedCompletionDateOverrided
                    .booleanValue();
        }
        else
        {
            m_isEstimatedCompletionDateOverrided = false;
        }
    }

    /**
     * @see Workflow.setEstimatedTranslateCompletionDate(Date)
     */
    public void setEstimatedTranslateCompletionDate(
            Date p_estimatedTranslateCompletionDate)
    {
        if (p_estimatedTranslateCompletionDate != null)
        {
            m_estimatedTranslateCompletionDate = p_estimatedTranslateCompletionDate;
        }
        else
        {
            m_estimatedTranslateCompletionDate = p_estimatedTranslateCompletionDate;
        }
    }

    /**
     * @see Workflow.isEstimatedTranslateCompletionDateOverrided()
     */
    public boolean isEstimatedTranslateCompletionDateOverrided()
    {
        return m_isEstimatedTranslateCompletionDateOverrided;
    }

    public Boolean getIsEstimatedTranslateCompletionDateOverrided()
    {
        return Boolean.valueOf(m_isEstimatedTranslateCompletionDateOverrided);
    }

    /**
     * @see Workflow.setEstimatedTranslateCompletionDateOverrided(boolean)
     */
    public void setEstimatedTranslateCompletionDateOverrided(
            boolean p_isEstimatedTranslateCompletionDateOverrided)
    {
        m_isEstimatedTranslateCompletionDateOverrided = p_isEstimatedTranslateCompletionDateOverrided;
    }

    public void setIsEstimatedTranslateCompletionDateOverrided(
            Boolean p_isEstimatedTranslateCompletionDateOverrided)
    {
        if (p_isEstimatedTranslateCompletionDateOverrided != null)
        {
            m_isEstimatedTranslateCompletionDateOverrided = p_isEstimatedTranslateCompletionDateOverrided
                    .booleanValue();
        }
        else
        {
            m_isEstimatedTranslateCompletionDateOverrided = false;
        }

    }

    /**
     * @see Workflow.getEstimatedTranslateCompletionDate()
     */
    public Date getEstimatedTranslateCompletionDate()
    {
        return m_estimatedTranslateCompletionDate;
    }

    /**
     * @see Workflow.getTranslationCompletedDate()
     */
    public Date getTranslationCompletedDate()
    {
        return m_translationCompletedDate;
    }

    /**
     * @see Workflow.setTranslationCompletedDate()
     */
    public void setTranslationCompletedDate(Date p_translationCompletedDate)
    {
        m_translationCompletedDate = p_translationCompletedDate;
    }

    /**
     * Use for calculate "Tranlation Completed Date" and "Estimated Translate
     * Completion Date". It need to be called every time while the workflow
     * updated.
     * 
     * @see Workflow.updateTranslationCompletedDates()
     */
    public void updateTranslationCompletedDates()
    {
        if (m_translationCompletedDate == null)
        {
            // Get translate activity
            if (m_TranslateActivity == null)
            {
                setTranslateActivity();
            }

            // Update Tranlation Completed Date
            if (m_TranslateActivity == null)
            {
                this.setTranslationCompletedDate(null);
            }
            else if (m_TranslateActivity.getState() == Task.STATE_COMPLETED)
            {
                this.setTranslationCompletedDate(m_TranslateActivity
                        .getCompletedDate());
            }

            // Update Estimated Translate Completion Date
            if (!isEstimatedTranslateCompletionDateOverrided())
            {
                if (m_TranslateActivity == null)
                {
                    this.setEstimatedTranslateCompletionDate(null);
                }
                else
                {
                    this.setEstimatedTranslateCompletionDate(m_TranslateActivity
                            .getEstimatedCompletionDate());
                }
            }
        }
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toStringNoTargetPages()
    {
        m_tasks.size();
        return super.toString()
                + " m_wfInstanceId="
                + Long.toString(m_wfInstanceId)
                + " m_job="
                + (m_job != null ? m_job.getJobName() + " jobID="
                        + ((JobImpl) m_job).getIdAsLong() : "null")
                + " m_state="
                + (m_state != null ? m_state : "null")
                + " m_targetLocale="
                + (m_targetLocale != null ? m_targetLocale.toString() : "null")
                + "\n"
                + " m_wfInstance="
                + (m_wfInstance != null ? m_wfInstance.toString() : "null")
                + " m_dispatchedDate="
                + (m_dispatchedDate != null ? m_dispatchedDate.toString()
                        : "null")
                + " m_completedDate="
                + (m_completedDate != null ? m_completedDate.toString()
                        : "null") + "\n" + " m_dispatchType="
                + (m_dispatchType != null ? m_dispatchType : "null")
                + " m_duration="
                + (m_duration != null ? m_duration.toString() : "null")
                + "\nm_tasks=" + m_tasks.toString() + "\n";
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        m_targetPages.size();
        return toStringNoTargetPages() + "\nm_targetPages="
                + m_targetPages.toString() + "\nWorkflow "
                + getIdAsLong().toString() + " toString end\n";
    }

    /**
     * Returns 'true' if the ids of the Workflow objects are equal, 'false' if
     * they aren't.
     * 
     * @param p_workflow
     *            The Workflow object to compare with
     * @return 'true' or 'false'
     */
    public boolean equals(Object p_workflow)
    {
        if (p_workflow instanceof Workflow)
        {
            return (getId() == ((Workflow) p_workflow).getId());
        }
        return false;
    }

    /**
     * Returns id
     * 
     * @return the value of id
     */
    public int hashCode()
    {
        Long idAsLong = new Long(getId());
        return idAsLong.hashCode();
    }

    public Set<TargetPage> getTargetPagesSet()
    {
        return m_targetPages;
    }

    public void setTargetPagesSet(Set<TargetPage> p_targetPages)
    {
        m_targetPages = p_targetPages;
        m_needsSorting = true;
    }

    /**
     * This does sort the pages according to page name by the US locale sorting
     * order. If we ever need to support user locale specific sorting for this
     * later, that can be added in the UI if the user's lang is not english
     */
    private void sortTargetPages()
    {
        if (!m_needsSorting)
        {
            return;
        }
        try
        {
            Comparator comparator = new PageComparator(
                    PageComparator.EXTERNAL_PAGE_ID, Locale.US);
            m_sortedTargetPages = new Vector<TargetPage>(m_targetPages);
            SortUtil.sort(m_sortedTargetPages, comparator);
            m_needsSorting = false;
        }
        catch (Exception e)
        {
            c_logger.error("Failed to sort targetPages ", e);
        }
    }

    public void setWorkflowType(String p_workflowType)
    {
        m_workflowType = p_workflowType;
    }

    public String getWorkflowType()
    {
        return m_workflowType;
    }

    // For sla report issue
    private void setTranslateActivity()
    {
        buildTaskMap();
        try
        {
            List taskInfos = ServerProxy.getWorkflowManager()
                    .getTaskInfosInDefaultPath(this);
            Iterator taskInfosIterator = taskInfos.iterator();
            if (taskInfos != null)
            {
                for (int i = taskInfos.size() - 1; i >= 0; i--)
                {
                    TaskInfo taskInfo = (TaskInfo) taskInfos.get(i);
                    Task task = (Task) m_taskMap
                            .get(new Long(taskInfo.getId()));
                    Activity act = ServerProxy.getJobHandler().getActivity(
                            task.getTaskName());
                    if (Activity.isTranslateActivity(act.getType()))
                    {
                        m_TranslateActivity = task;
                        break;
                    }
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error(e);
        }
    }

    public String getFraction()
    {
        return m_fraction;
    }

    public void setFraction(String m_fraction)
    {
        this.m_fraction = m_fraction;
    }

    /**
     * Judges whether the workflow is translation workflow.
     * 
     * @return
     */
    public boolean isTranslationWorkflow()
    {
        return WorkflowTypeConstants.TYPE_TRANSLATION.equals(getWorkflowType());
    }

    public Date getExportDate()
    {
        return exportDate;
    }

    public void setExportDate(Date exportDate)
    {
        this.exportDate = exportDate;
    }

    public int getPriority()
    {
        return m_priority;
    }

    public void setPriority(int priority)
    {
        m_priority = priority;
    }

    public Integer getThresholdHiFuzzyWordCountAsInteger()
    {
        return m_thresholdHiFuzzyWordCount;
    }

    public void setThresholdHiFuzzyWordCountAsInteger(
            Integer thresholdHiFuzzyWordCountAsInteger)
    {
        this.m_thresholdHiFuzzyWordCount = thresholdHiFuzzyWordCountAsInteger;
    }

    public Integer getThresholdMedHiFuzzyWordCountAsInteger()
    {
        return m_thresholdMedHiFuzzyWordCount;
    }

    public void setThresholdMedHiFuzzyWordCountAsInteger(
            Integer thresholdMedHiFuzzyWordCountAsInteger)
    {
        this.m_thresholdMedHiFuzzyWordCount = thresholdMedHiFuzzyWordCountAsInteger;
    }

    public Integer getThresholdMedFuzzyWordCountAsInteger()
    {
        return m_thresholdMedFuzzyWordCount;
    }

    public void setThresholdMedFuzzyWordCountAsInteger(
            Integer thresholdMedFuzzyWordCountAsInteger)
    {
        this.m_thresholdMedFuzzyWordCount = thresholdMedFuzzyWordCountAsInteger;
    }

    public Integer getThresholdLowFuzzyWordCountAsInteger()
    {
        return m_thresholdLowFuzzyWordCount;
    }

    public void setThresholdLowFuzzyWordCountAsInteger(
            Integer thresholdLowFuzzyWordCountAsInteger)
    {
        this.m_thresholdLowFuzzyWordCount = thresholdLowFuzzyWordCountAsInteger;
    }

    public Integer getThresholdNoMatchWordCountAsInteger()
    {
        return m_thresholdNoMatchWordCount;
    }

    public void setThresholdNoMatchWordCountAsInteger(
            Integer thresholdNoMatchWordCountAsInteger)
    {
        this.m_thresholdNoMatchWordCount = thresholdNoMatchWordCountAsInteger;
    }

    public int getThresholdHiFuzzyWordCount()
    {
        return m_thresholdHiFuzzyWordCount == null ? 0
                : m_thresholdHiFuzzyWordCount.intValue();
    }

    public void setThresholdHiFuzzyWordCount(int thresholdHiFuzzyWordCount)
    {
        this.m_thresholdHiFuzzyWordCount = new Integer(
                thresholdHiFuzzyWordCount);
    }

    public int getThresholdMedHiFuzzyWordCount()
    {
        return m_thresholdMedHiFuzzyWordCount == null ? 0
                : m_thresholdMedHiFuzzyWordCount.intValue();
    }

    public void setThresholdMedHiFuzzyWordCount(int thresholdMedHiFuzzyWordCount)
    {
        this.m_thresholdMedHiFuzzyWordCount = new Integer(
                thresholdMedHiFuzzyWordCount);
    }

    public int getThresholdMedFuzzyWordCount()
    {
        return m_thresholdMedFuzzyWordCount == null ? 0
                : m_thresholdMedFuzzyWordCount.intValue();
    }

    public void setThresholdMedFuzzyWordCount(int thresholdMedFuzzyWordCount)
    {
        this.m_thresholdMedFuzzyWordCount = new Integer(
                thresholdMedFuzzyWordCount);
    }

    public int getThresholdLowFuzzyWordCount()
    {
        return m_thresholdLowFuzzyWordCount == null ? 0
                : m_thresholdLowFuzzyWordCount.intValue();
    }

    public void setThresholdLowFuzzyWordCount(int thresholdLowFuzzyWordCount)
    {
        this.m_thresholdLowFuzzyWordCount = new Integer(
                thresholdLowFuzzyWordCount);
    }

    public int getThresholdNoMatchWordCount()
    {
        return m_thresholdNoMatchWordCount == null ? 0
                : m_thresholdNoMatchWordCount.intValue();
    }

    public void setThresholdNoMatchWordCount(int thresholdNoMatchWordCount)
    {
        this.m_thresholdNoMatchWordCount = new Integer(
                thresholdNoMatchWordCount);
    }

    public void setMtTotalWordCount(int p_mtTotalWordCount)
    {
        this.mtTotalWordCount = p_mtTotalWordCount;
    }

    public int getMtTotalWordCount()
    {
        return this.mtTotalWordCount == null ? 0 : mtTotalWordCount.intValue();
    }

    public void setMtFuzzyNoMatchWordCount(int p_mtFuzzyNoMatchWordCount)
    {
        this.mtFuzzyNoMatchWordCount = p_mtFuzzyNoMatchWordCount;
    }

    public int getMtFuzzyNoMatchWordCount()
    {
        return this.mtFuzzyNoMatchWordCount == null ? 0
                : mtFuzzyNoMatchWordCount.intValue();
    }

    public void setMtRepetitionsWordCount(int p_mtRepetitionsWordCount)
    {
        this.mtRepetitionsWordCount = p_mtRepetitionsWordCount;
    }

    public int getMtRepetitionsWordCount()
    {
        return this.mtRepetitionsWordCount == null ? 0 : mtRepetitionsWordCount
                .intValue();
    }

    public void setMtEngineWordCount(int p_mtEngineWordCount)
    {
        this.mtEngineWordCount = p_mtEngineWordCount;
    }

    public int getMtEngineWordCount()
    {
        return this.mtEngineWordCount;
    }

    public void setUseMT(boolean p_value)
    {
        this.useMT = p_value;
    }

    public boolean getUseMT()
    {
        return this.useMT;
    }

    public void setMtConfidenceScore(int p_mtConfidenceScore)
    {
        this.mtConfidenceScore = p_mtConfidenceScore;
    }

    public int getMtConfidenceScore()
    {
        return this.mtConfidenceScore;
    }

	public void setScorecardComment(String p_scorecardComment) {
		this.scorecardComment = p_scorecardComment;
	}

	public String getScorecardComment() {
		return this.scorecardComment;
	}

	public void setScorecardShowType(int scorecardShowType) {
		this.scorecardShowType = scorecardShowType;
	}

	public int getScorecardShowType() {
		return scorecardShowType;
	}

    public String getMtProfileName()
    {
        return mtProfileName;
    }

    public void setMtProfileName(String mtProfileName)
    {
        this.mtProfileName = mtProfileName;
    }
}
