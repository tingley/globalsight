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
import java.util.Collection;
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

/**
 * Need to include "WorkObject" here also - as well as in Workflow.java TL can't
 * seem to find it from the interface Workflow.
 */
public class WorkflowImpl extends PersistentObject implements Workflow,
        WorkObject
{
    private static final long serialVersionUID = -9074840785216382158L;

    private static Logger c_logger = Logger
            .getLogger(WorkflowImpl.class.getName());

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

    private Hashtable m_tasks = new Hashtable();

    // private Collection m_targetPages = new ArrayList();
    private Vector m_targetPages = new Vector();

    private List m_secondaryTargetFiles = new ArrayList();

    private String m_dispatchType;

    private Long m_duration = new Long(0L); // the duration of the

    // workflow in days
    private Integer m_contextMatchWordCount = new Integer(0);

    private Integer m_segmentTmWordCount = new Integer(0);

    private Integer m_lowFuzzyMatchWordCount = new Integer(0);

    private Integer m_medFuzzyMatchWordCount = new Integer(0);
    
    private Integer m_medFuzzyRepetitionWordCount = new Integer(0);

    private Integer m_medHiFuzzyMatchWordCount = new Integer(0);
    
    private Integer m_medHiFuzzyRepetitionWordCount = new Integer(0);

    private Integer m_hiFuzzyMatchWordCount = new Integer(0);
    
    private Integer m_hiFuzzyRepetitionWordCount = new Integer(0);

    private Integer m_subLevMatchWordCount = new Integer(0);

    private Integer m_subLevRepetitionWordCount = new Integer(0);

    private Integer m_noMatchWordCount = new Integer(0);

    private Integer m_repetitionWordCount = new Integer(0);

    private Integer m_totalWordCount = new Integer(0);
    
    private Integer m_thresholdHiFuzzyWordCount = new Integer(0);
    
    private Integer m_thresholdMedHiFuzzyWordCount = new Integer(0);
    
    private Integer m_thresholdMedFuzzyWordCount = new Integer(0);
    
    private Integer m_thresholdLowFuzzyWordCount = new Integer(0);
    
    private Integer m_thresholdNoMatchWordCount = new Integer(0);
    
    private Date m_plannedCompletionDate = null;

    private List m_workflowOwners = new ArrayList();

    private Collection m_workflowComments = new ArrayList();

    private List m_sortedWorkflowComments;

    private boolean m_workflowCommentsNeedSorting = true;

    private boolean m_needsSorting = true;
    
    private Integer m_incontextmatch = new Integer(0);
    
    private Integer m_noUseInContextMatchWordCount = new Integer(0);

    private Integer m_noUseExactMatchWordCount = new Integer(0);

    // id of the company which this activity belong to
    private String m_companyId;

    // For sla report issue
    // User can override the estimatedCompletionDate
    private boolean m_isEstimatedCompletionDateOverrided = false;

    private boolean m_isEstimatedTranslateCompletionDateOverrided = false;

    private Date m_estimatedTranslateCompletionDate = null;

    private Date m_translationCompletedDate = null;

    // The translate activity in workflow
    private Task m_TranslateActivity = null;
    
    private int m_priority;

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public String getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(String p_companyId)
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
        for (int i = 0; i < m_targetPages.size(); i++)
        {
            TargetPage targetPage = (TargetPage) m_targetPages.get(i);

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
        for (int i = 0; i < m_targetPages.size(); i++)
        {
            TargetPage tp = (TargetPage) m_targetPages.get(i);
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
    public Vector getAllTargetPages()
    {
        sortTargetPages();
        return m_targetPages;
    }

    /**
     * @see Workflow.getSecondaryTargetFiles()
     */
    public List getSecondaryTargetFiles()
    {
        return m_secondaryTargetFiles;
    }

    public Set getSecondaryTargets()
    {
        Set targets = null;
        if (m_secondaryTargetFiles != null)
        {
            targets = new HashSet(m_secondaryTargetFiles);
        }
        return targets;
    }

    public void setSecondaryTargets(Set targets)
    {
        if (targets == null)
        {
            m_secondaryTargetFiles = new ArrayList();

        }
        else
        {
            m_secondaryTargetFiles = new ArrayList(targets);
        }
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
        return m_tasks;
    }

    public Set getTaskSet()
    {
        Set tasks = null;
        if (m_tasks != null)
        {
            tasks = new HashSet(m_tasks.values());
        }
        return tasks;
    }

    public void setTaskSet(Set tasks)
    {
        m_tasks = new Hashtable();
        if (tasks != null)
        {
            Iterator taskI = tasks.iterator();
            while (taskI.hasNext())
            {
                Task task = (Task) taskI.next();
                addTask(task);
            }
            tasks = new HashSet(m_tasks.values());
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
        m_tasks.put(new Long(p_task.getId()), p_task);
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
            m_tasks.remove(new Long(p_task.getId()));
            p_task.setWorkflow(null);
            p_task.removeAmountOfWork();
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
        m_contextMatchWordCount = new Integer(p_contextMatchWordCount);
    }

    /*
     * @see Workflow.getContextMatchWordCount()
     */
    public int getContextMatchWordCount()
    {
        return m_contextMatchWordCount == null ? 0 : m_contextMatchWordCount
                .intValue();
    }

    public void setContextMatchWordCountAsInteger(
            Integer p_contextMatchWordCount)
    {
        m_contextMatchWordCount = p_contextMatchWordCount;
    }

    public Integer getContextMatchWordCountAsInteger()
    {
        return m_contextMatchWordCount;
    }

    /*
     * @see Workflow.setSegmentTmWordCount()
     */
    public void setSegmentTmWordCount(int p_segmentTmWordCount)
    {
        m_segmentTmWordCount = new Integer(p_segmentTmWordCount);
    }

    /*
     * @see Workflow.getSegmentTmWordCount()
     */
    public int getSegmentTmWordCount()
    {
        return m_segmentTmWordCount == null ? 0 : m_segmentTmWordCount
                .intValue();
    }

    public void setSegmentTmWordCountAsInteger(Integer p_segmentTmWordCount)
    {
        m_segmentTmWordCount = p_segmentTmWordCount;
    }

    public Integer getSegmentTmWordCountAsInteger()
    {
        return m_segmentTmWordCount;
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
    
    public Integer getMedFuzzyRepetitionWordCountAsInteger()
    {
        return m_medFuzzyRepetitionWordCount;
    }

    public void setMedFuzzyRepetitionWordCountAsInteger(
            Integer medFuzzyRepetitionWordCountAsInteger)
    {
        this.m_medFuzzyRepetitionWordCount = medFuzzyRepetitionWordCountAsInteger;
    }

    public Integer getMedHiFuzzyRepetitionWordCountAsInteger()
    {
        return m_medHiFuzzyRepetitionWordCount;
    }

    public void setMedHiFuzzyRepetitionWordCountAsInteger(
            Integer medHiFuzzyRepetitionWordCountAsInteger)
    {
        this.m_medHiFuzzyRepetitionWordCount = medHiFuzzyRepetitionWordCountAsInteger;
    }

    public Integer getHiFuzzyRepetitionWordCountAsInteger()
    {
        return m_hiFuzzyRepetitionWordCount;
    }

    public void setHiFuzzyRepetitionWordCountAsInteger(
            Integer hiFuzzyRepetitionWordCountAsInteger)
    {
        this.m_hiFuzzyRepetitionWordCount = hiFuzzyRepetitionWordCountAsInteger;
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

    public void setInContextMatchWordCount(int inContextMatchWord){
        this.m_incontextmatch = new Integer(inContextMatchWord);
    }

    public int getInContextMatchWordCount(){
        return this.m_incontextmatch == null ? 0 : m_incontextmatch.intValue();
    }

    public void setNoUseInContextMatchWordCountAsInteger(Integer noUseInContextMatchWordCount) {
        this.m_noUseInContextMatchWordCount = noUseInContextMatchWordCount;
    }

    public void setNoUseInContextMatchWordCount(int noUseInContextMatchWordCount) {
        this.m_noUseInContextMatchWordCount = new Integer(noUseInContextMatchWordCount);
    }
    
    public Integer getNoUseInContextMatchWordCountAsInteger(){
        return this.m_noUseInContextMatchWordCount == null ? 0: m_noUseInContextMatchWordCount;
    }
    
    public int getNoUseInContextMatchWordCount(){
        return this.m_noUseInContextMatchWordCount == null ? 0: m_noUseInContextMatchWordCount.intValue();
    }
    
    public void setNoUseExactMatchWordCountAsInteger(Integer noUseExactMatchWordCount) {
        this.m_noUseExactMatchWordCount = noUseExactMatchWordCount;
    }
    
    public void setNoUseExactMatchWordCount(int noUseExactMatchWordCount) {
        this.m_noUseExactMatchWordCount = new Integer(noUseExactMatchWordCount);
    }

    public Integer getNoUseExactMatchWordCountAsInteger(){
        return this.m_noUseExactMatchWordCount == null ? 0: m_noUseExactMatchWordCount;
    }

    public int getNoUseExactMatchWordCount(){
        return this.m_noUseExactMatchWordCount == null ? 0: m_noUseExactMatchWordCount.intValue();
    }

    public void setInContextMatchWordCountAsInteger(Integer inContextMatchWord){
        m_incontextmatch = inContextMatchWord;
    }

    public Integer getInContextMatchWordCountAsInteger(){
        return m_incontextmatch;
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

    /**
     * @see Workflow.getSubLevMatchWordCount()
     */
    public int getSubLevMatchWordCount()
    {
        return m_subLevMatchWordCount == null ? 0 : m_subLevMatchWordCount
                .intValue();
    }

    /**
     * @see Workflow.setSubLevMatchWordCount(int)
     */
    public void setSubLevMatchWordCount(int p_subLevMatchWordCount)
    {
        m_subLevMatchWordCount = new Integer(p_subLevMatchWordCount);
    }

    public Integer getSubLevMatchWordCountAsInteger()
    {
        return m_subLevMatchWordCount;
    }

    public void setSubLevMatchWordCountAsInteger(Integer p_subLevMatchWordCount)
    {
        m_subLevMatchWordCount = p_subLevMatchWordCount;
    }

    /**
     * @see Workflow.getSubLevRepetitionWordCount()
     */
    public int getSubLevRepetitionWordCount()
    {
        return m_subLevRepetitionWordCount == null ? 0
                : m_subLevRepetitionWordCount.intValue();
    }

    /**
     * @see Workflow.setSubLevRepetitionWordCount(int)
     */
    public void setSubLevRepetitionWordCount(int p_subLevRepetitionWordCount)
    {
        m_subLevRepetitionWordCount = new Integer(p_subLevRepetitionWordCount);
    }

    public Integer getSubLevRepetitionWordCountAsInteger()
    {
        return m_subLevRepetitionWordCount;
    }

    public void setSubLevRepetitionWordCountAsInteger(
            Integer p_subLevRepetitionWordCount)
    {
        m_subLevRepetitionWordCount = p_subLevRepetitionWordCount;
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
        m_repetitionWordCount = new Integer(p_totalRepetitionWordCount);
    }

    /*
     * @see Workflow.getRepetitionWordCount()
     */
    public int getRepetitionWordCount()
    {
        int repetitionWordCount = 0;
        if (m_repetitionWordCount != null)
        {
            repetitionWordCount = m_repetitionWordCount.intValue();
        }
        return repetitionWordCount;
    }

    public void setRepetitionWordCountAsInteger(
            Integer p_totalRepetitionWordCount)
    {
        m_repetitionWordCount = p_totalRepetitionWordCount;
    }

    public Integer getRepetitionWordCountAsInteger()
    {
        return m_repetitionWordCount;
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
     * @param long
     *            p_wfInstanceId
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

    public void setWorkflowOwnerSet(Set owers)
    {
        m_workflowOwners = new ArrayList();
        if (owers != null)
        {
            Iterator owerI = owers.iterator();
            while (owerI.hasNext())
            {
                WorkflowOwner ownwe = (WorkflowOwner) owerI.next();
                addWorkflowOwner(ownwe);
            }
        }
    }

    /**
     * @see Workflow.getWorkflowOwners()
     */
    public List getWorkflowOwners()
    {
        return m_workflowOwners;
    }

    public Set getWorkflowOwnerSet()
    {
        Set owers = null;
        if (m_workflowOwners != null)
        {
            owers = new HashSet(m_workflowOwners);
        }
        return owers;
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
    public List getWorkflowOwnerIds()
    {
        int size = m_workflowOwners.size();

        if (size == 0)
        {
            return m_workflowOwners;
        }

        List owners = new ArrayList();
        for (int i = 0; i < size; i++)
        {
            WorkflowOwner wfo = (WorkflowOwner) m_workflowOwners.get(i);
            owners.add(wfo.getOwnerId());
        }

        return owners;
    }

    /**
     * @see Workflow.getWorkflowOwnersByType()
     */
    public List getWorkflowOwnerIdsByType(String p_ownerType)
    {
        int size = m_workflowOwners.size();

        if (size == 0)
        {
            return m_workflowOwners;
        }

        List owners = new ArrayList();
        for (int i = 0; i < size; i++)
        {
            WorkflowOwner wfo = (WorkflowOwner) m_workflowOwners.get(i);
            if (p_ownerType.equals(wfo.getOwnerType()))
            {
                owners.add(wfo.getOwnerId());
            }
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
        m_targetPages.size();
        m_tasks.size();
        // call the default writeObject
        out.defaultWriteObject();
    }

    /**
     * Add this workflow comment to the collection.
     * 
     * @param p_comment -
     *            The comment to be added.
     */
    public void addWorkflowComment(Comment p_comment)
    {
        m_workflowComments.add(p_comment);
        m_workflowCommentsNeedSorting = true;
    }

    /**
     * Remove this workflow comment from the collection.
     * 
     * @param p_comment -
     *            The comment remove.
     */
    public void removeWorkflowComment(Comment p_comment)
    {
        m_workflowComments.remove(p_comment);
        m_workflowCommentsNeedSorting = true;
    }

    /**
     * Set the workflow comments to be this value.
     * 
     * @param p_comments -
     *            The task comments to be set.
     */
    public void setWorkflowComments(List p_comments)
    {
        m_workflowComments = p_comments;
        m_workflowCommentsNeedSorting = true;
    }

    public void setWorkflowCommentSet(Set p_comments)
    {
        List comments;
        if (p_comments == null)
        {
            comments = new ArrayList();
        }
        else
        {
            comments = new ArrayList(p_comments);
        }
        setWorkflowComments(comments);
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
                Collections.sort(m_sortedWorkflowComments, Collections
                        .reverseOrder());
            }
            else if ("asc"
                    .equals((String) sc
                            .getStringParameter(SystemConfigParamNames.COMMENTS_SORTING)))
            {
                Collections.sort(m_sortedWorkflowComments);
            }
        }
        catch (GeneralException ge)
        {
            c_logger
                    .warn("Could not read system parameter for comment sorting: "
                            + SystemConfigParamNames.COMMENTS_SORTING);
            Collections.sort(m_sortedWorkflowComments, Collections
                    .reverseOrder());
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

    public Set getWorkflowCommentSet()
    {
        List comments = getWorkflowComments();
        if (comments == null)
        {
            comments = new ArrayList();
        }
        return new HashSet(comments);
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
        if(p_estimatedTranslateCompletionDate !=null) {
        m_estimatedTranslateCompletionDate = p_estimatedTranslateCompletionDate;
        }
        else {
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
                    this
                            .setEstimatedTranslateCompletionDate(m_TranslateActivity
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

    public Set getTargetPagesSet()
    {
        Set pages = null;
        if (m_targetPages != null)
        {
            pages = new HashSet(m_targetPages);
        }
        return pages;
    }

    public void setTargetPagesSet(Set p_targetPages)
    {
        if (p_targetPages == null)
        {
            m_targetPages = new Vector();
        }
        else
        {
            m_targetPages = new Vector(p_targetPages);
        }
    }

    /**
     * This does sort the pages according to page name by the US locale sorting
     * order. If we ever need to support user locale specific sorting for this
     * later, that can be added in the UI if the user's lang is not english
     */
    private void sortTargetPages()
    {
        if (m_targetPages == null || m_needsSorting == false)
        {
            return;
        }
        try
        {
            Comparator comparator = new PageComparator(
                    PageComparator.EXTERNAL_PAGE_ID, Locale.US);
            Collections.sort(m_targetPages, comparator);
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
                    Task task = (Task) m_tasks.get(new Long(taskInfo.getId()));
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
    
    public int getPriority() {
        return m_priority;
    }
    
    public void setPriority(int priority) {
        m_priority = priority;
    }

    @Override
    public int getHiFuzzyRepetitionWordCount()
    {
        return m_hiFuzzyRepetitionWordCount == null ? 0 : m_hiFuzzyRepetitionWordCount
                .intValue();
    }

    @Override
    public int getMedFuzzyRepetitionWordCount()
    {
        return m_medFuzzyRepetitionWordCount == null ? 0 : m_medFuzzyRepetitionWordCount
                .intValue();
    }

    @Override
    public int getMedHiFuzzyRepetitionWordCount()
    {
        return m_medHiFuzzyRepetitionWordCount == null ? 0 : m_medHiFuzzyRepetitionWordCount
                .intValue();
    }

    @Override
    public void setHiFuzzyRepetitionWordCount(int hiFuzzyRepetitionWordCount)
    {
        m_hiFuzzyRepetitionWordCount = new Integer(hiFuzzyRepetitionWordCount);
    }

    @Override
    public void setMedFuzzyRepetitionWordCount(int medFuzzyRepetitionWordCount)
    {
        m_medFuzzyRepetitionWordCount = new Integer(medFuzzyRepetitionWordCount);
    }

    @Override
    public void setMedHiFuzzyRepetitionWordCount(int medHiFuzzyRepetitionWordCount)
    {
        m_medHiFuzzyRepetitionWordCount = new Integer(medHiFuzzyRepetitionWordCount);
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
        return m_thresholdHiFuzzyWordCount == null ? 0 : m_thresholdHiFuzzyWordCount
                .intValue();
    }

    public void setThresholdHiFuzzyWordCount(int thresholdHiFuzzyWordCount)
    {
        this.m_thresholdHiFuzzyWordCount = new Integer(thresholdHiFuzzyWordCount);
    }

    public int getThresholdMedHiFuzzyWordCount()
    {
        return m_thresholdMedHiFuzzyWordCount == null ? 0 : m_thresholdMedHiFuzzyWordCount
                .intValue();
    }

    public void setThresholdMedHiFuzzyWordCount(int thresholdMedHiFuzzyWordCount)
    {
        this.m_thresholdMedHiFuzzyWordCount = new Integer(thresholdMedHiFuzzyWordCount);
    }

    public int getThresholdMedFuzzyWordCount()
    {
        return m_thresholdMedFuzzyWordCount == null ? 0 : m_thresholdMedFuzzyWordCount
                .intValue();
    }

    public void setThresholdMedFuzzyWordCount(int thresholdMedFuzzyWordCount)
    {
        this.m_thresholdMedFuzzyWordCount = new Integer(thresholdMedFuzzyWordCount);
    }

    public int getThresholdLowFuzzyWordCount()
    {
        return m_thresholdLowFuzzyWordCount == null ? 0 : m_thresholdLowFuzzyWordCount
                .intValue();
    }

    public void setThresholdLowFuzzyWordCount(int thresholdLowFuzzyWordCount)
    {
        this.m_thresholdLowFuzzyWordCount = new Integer(thresholdLowFuzzyWordCount);
    }

    public int getThresholdNoMatchWordCount()
    {
        return m_thresholdNoMatchWordCount == null ? 0 : m_thresholdNoMatchWordCount
                .intValue();
    }

    public void setThresholdNoMatchWordCount(int thresholdNoMatchWordCount)
    {
        this.m_thresholdNoMatchWordCount = new Integer(thresholdNoMatchWordCount);
    }
    
    
}
