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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeClone;
import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.cxe.entity.customAttribute.JobAttribute;
import com.globalsight.cxe.entity.fileprofile.FileProfile;
import com.globalsight.cxe.entity.fileprofile.FileProfileImpl;
import com.globalsight.cxe.persistence.databaseprofile.DatabaseProfilePersistenceManager;
import com.globalsight.cxe.persistence.fileprofile.FileProfilePersistenceManager;
import com.globalsight.everest.comment.Comment;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.foundation.WorkObject;
import com.globalsight.everest.page.AddingSourcePage;
import com.globalsight.everest.page.AddingSourcePageManager;
import com.globalsight.everest.page.SourcePage;
import com.globalsight.everest.page.UpdateSourcePageManager;
import com.globalsight.everest.page.UpdatedSourcePage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.request.Request;
import com.globalsight.everest.request.WorkflowRequest;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.everest.taskmanager.TaskImpl;
import com.globalsight.everest.webapp.pagehandler.administration.config.attribute.action.CostCenterAction;
import com.globalsight.everest.webapp.pagehandler.administration.users.UserHandlerHelper;
import com.globalsight.everest.workflowmanager.Workflow;
import com.globalsight.persistence.hibernate.HibernateUtil;
import com.globalsight.util.GlobalSightLocale;
import com.globalsight.util.resourcebundle.ResourceBundleConstants;
import com.globalsight.util.resourcebundle.SystemResourceBundle;
import com.globalsight.webservices.attribute.AddJobAttributeThread;

/**
 * Need to include "WorkObject" here also - as well as in Task.java for
 * TOPLink's use. TL can't seem to find it from the interface Task.
 */
public class JobImpl extends PersistentObject implements Job, WorkObject
{
    private static final long serialVersionUID = 5395578307790122441L;

    static private final Logger c_logger = Logger.getLogger(JobImpl.class);

    // query key used to build a TOPLink query
    static public final String STATE = "m_state";
    static public final String JOB_ID = M_ID;
    static public final String JOB_NAME = "m_jobName";
    static public final String PROFILE = "m_l10nProfile";
    static public final String REQUESTS = "m_requestList";
    static public final String WORKFLOWS = "m_wfInstances";
    static public final String PRIORITY = "m_priority";
    static public final String CREATE_DATE = "m_createDate";
    static public final String COMPANY_ID = "m_companyId";

    private String m_quoteDate = null;

    // For "Quote process webEx" issue
    private String m_quoteApprovedDate = null;
    private String m_quotePoNumber = null;

    private String m_state;
    private String m_orgState;
    private String m_jobName;
    private int m_priority = 3;
    private int m_leverageMatchThreshold = 75;
    private GlobalSightLocale m_sourceLocale;
    private boolean m_isWordCountReached = false;
    private String m_dispatchType;
    private Date m_createDate = null;
    private String m_dataSourceName;
    private L10nProfile m_l10nProfile = null;
    private String m_fProfileNames = "";
    private Collection<Request> m_requestList = new ArrayList<Request>();
    private Collection m_workflowRequestList = new ArrayList();
    private Set<Workflow> m_wfInstances = new HashSet<Workflow>();
    private Collection<SourcePage> m_sourcePages = new ArrayList<SourcePage>();
    private List m_jobComments = new ArrayList();
    // holds the overriden word count.
    // is NULL if the word count is not overriden
    private Integer m_overridenWordCount = null;
    private User m_user = null;
    private User m_createUser = null;
    private String m_jauId = null;
    private String uuid;
    private static Integer UUID_PREFIX = 0;

    private int m_pageCount = 1;
    // from GBS-2137
    private long m_l10nProfileId = -1;

    private String m_createUserId = null;

    // id of the company which this activity belong to
    private long m_companyId;

	private Long m_groupId = null;
    
	private String leverageOption = Job.IN_CONTEXT;

    private Set<JobAttribute> attributes;
    private AttributeSet attributeSet;

    private Map<AttributeClone, JobAttribute> attriguteMap;

    private Date startDate = null;
    private Date completedDate = null;

    private boolean isMigrated = false;
    private boolean isAllRequestGenerated = false;

    private String tuTable = null;
    private String tuArchiveTable = null;
    private String tuvTable = null;
    private String tuvArchiveTable = null;
    private String lmTable = null;
    private String lmArchiveTable = null;
    private String lmExtTable = null;
    private String lmExtArchiveTable = null;

    private String jobType = null;

    public JobImpl()
    {
        m_leverageMatchThreshold = 50;
    }

    // could be deprecated - just calls getId() -
    public long getJobId()
    {
        return getId();
    }
    
    public Long getGroupId()
   	{
   		return m_groupId;
   	}

   	public void setGroupId(Long p_groupId)
   	{
   		this.m_groupId = p_groupId;
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

    public int getPriority()
    {
        return m_priority;
    }

    public void setPriority(int p_priority)
    {
        m_priority = p_priority;
    }

    public void addRequest(Request p_request)
    {
        p_request.setJob(this);
        m_requestList.add(p_request);
        m_pageCount = m_requestList.size();
    }

    public void addWorkflowRequest(WorkflowRequest p_workflowRequest)
    {
        p_workflowRequest.setJob(this);
        m_workflowRequestList.add(p_workflowRequest);
    }

    public Collection getWorkflowRequestList()
    {
        return m_workflowRequestList;
    }

    public Set getWorkflowRequestSet()
    {
        Set requests = null;
        if (m_workflowRequestList != null)
        {
            requests = new HashSet(m_workflowRequestList);
        }
        return requests;
    }

    public void removeRequest(Request p_request)
    {
        m_requestList.remove(p_request);
        p_request.setJob(null);
        m_pageCount = m_requestList.size();
    }

    public Collection<Request> getRequestList()
    {
        return m_requestList;
    }

    public void addWorkflowInstance(Workflow p_wfInstance)
    {
        p_wfInstance.setJob(this);
        m_wfInstances.add(p_wfInstance);
    }

    public void setWorkflowInstances(List p_wfInstances)
    {
        m_wfInstances = new HashSet<Workflow>(p_wfInstances);
    }

    public Set getWorkflowInstanceSet()
    {
        return m_wfInstances;
    }

    public void setWorkflowInstanceSet(Set p_wfInstances)
    {
        m_wfInstances = p_wfInstances;
    }

    public String getJobName()
    {
        return m_jobName;
    }

    /*
     * Override the super class one to use the "m_jobName" field.
     */
    public String getName()
    {
        return getJobName();
    }

    public void setCreateDate(Date p_createDate)
    {
        m_createDate = p_createDate;
    }

    public Date getCreateDate()
    {
        return m_createDate;
    }

    /*
     * @see Job.getDueDate
     */
    public Date getDueDate()
    {
        Date dueDate = null;

        // if in the correct state to calculate due date
        if (!getState().equals(PENDING) && !getState().equals(BATCHRESERVED) &&
        // // For sla report issue
        // !getState().equals(READY_TO_BE_DISPATCHED) &&
                !getState().equals(IMPORTFAILED))
        {
            for (Iterator it = m_wfInstances.iterator(); it.hasNext();)
            {
                // finds the workflow with the last date and sets due
                // date to that
                Workflow wf = (Workflow) it.next();

                if (!wf.getState().equals(Workflow.IMPORT_FAILED)
                        && !wf.getState().equals(Workflow.CANCELLED))
                {
                    Date estimatedDate = wf.getEstimatedCompletionDate();
                    if (estimatedDate != null
                            && (dueDate == null || dueDate
                                    .before(estimatedDate)))
                    {
                        dueDate = estimatedDate;
                    }
                }
            }
        }

        return dueDate;
    }

    /*
     * @see Job.getDuration
     */
    public long getDuration()
    {
        long duration = 0;

        // go through all worklows and get their duration
        for (Iterator it = m_wfInstances.iterator(); it.hasNext();)
        {
            // finds the workflow with the largest duration
            Workflow wf = (Workflow) it.next();

            if (!wf.getState().equals(Workflow.IMPORT_FAILED)
                    && !wf.getState().equals(Workflow.CANCELLED))
            {
                if (duration < wf.getDuration())
                {
                    duration = wf.getDuration();
                }
            }
        }

        return duration;
    }

    public void setWordCountReached(boolean p_isWordCountReached)
    {
        m_isWordCountReached = p_isWordCountReached;
    }

    public boolean isWordCountReached()
    {
        return m_isWordCountReached;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    public void setSourceLocale(GlobalSightLocale p_sourceLocale)
    {
        m_sourceLocale = p_sourceLocale;
    }

    /**
     * @see Job.getSourcePages()
     */
    public Collection<SourcePage> getSourcePages()
    {
        try
        {
            m_sourcePages = ServerProxy.getJobHandler().getSourcePageByJobId(
                    getId());
        }
        catch (Exception e)
        {
            c_logger.error("The source pages of job " + getId()
                    + " could not be obtained." + e);
        }

        return m_sourcePages;
    }

    /**
     * @see Job.getSourcePages(int)
     */
    public Collection getSourcePages(int p_primaryFileType)
    {
        try
        {
            m_sourcePages = ServerProxy.getJobHandler()
                    .getSourcePagesByTypeAndJobId(p_primaryFileType, getId());
        }
        catch (Exception e)
        {
            c_logger.error("The source pages of type " + p_primaryFileType
                    + " for job " + getId() + " could not be obtained." + e);
        }

        return m_sourcePages;
    }

    public String getDispatchType()
    {
        boolean dispatchType = getL10nProfile().dispatchIsAutomatic();

        if (dispatchType == true)
        {
            m_dispatchType = Job.AUTOMATIC;
        }
        else
        {
            m_dispatchType = Job.MANUAL;
        }

        return m_dispatchType;
    }

    public void setL10nProfileId(Long p_l10nProfileId)
    {
        m_l10nProfileId = p_l10nProfileId;
    }

    public long getL10nProfileId()
    {
        if (m_l10nProfileId != -1)
        {
            return m_l10nProfileId;
        }
        L10nProfile lp = getL10nProfile();
        if (lp != null)
        {
            return lp.getId();
        }

        return -1;
    }

    public String getFProfileNames()
    {
        if (!"".equals(m_fProfileNames))
        {
            return m_fProfileNames;
        }
        Set<FileProfile> set = new HashSet<FileProfile>();
        set.addAll(getAllFileProfiles());
        for (FileProfile fp : set)
        {
            try
            {
                FileProfilePersistenceManager fpManager = ServerProxy
                        .getFileProfilePersistenceManager();
                boolean isRef = fpManager.isXlzReferenceXlfFileProfile(fp
                        .getName());
                if (isRef)
                {
                    String tmp = fp.getName().substring(0,
                            fp.getName().length() - 4);
                    fp = fpManager.getFileProfileByName(tmp);
                    if (fp == null)
                        fp = fpManager.getFileProfileByName(tmp, false);
                }
            }
            catch (Exception e)
            {

            }

            if ("".equals(m_fProfileNames))
            {
                m_fProfileNames += fp.getName();
            }
            else
            {
                m_fProfileNames += "," + fp.getName();
            }
        }

        return m_fProfileNames;
    };

    public L10nProfile getL10nProfile()
    {
        if (m_l10nProfile == null)
        {
            try
            {
                if (m_l10nProfileId != -1)
                {
                    m_l10nProfile = ServerProxy.getProjectHandler()
                            .getL10nProfile(m_l10nProfileId);
                }
                else
                {
                    m_l10nProfile = ServerProxy.getJobHandler()
                            .getL10nProfileByJobId(getId());
                }
            }
            catch (Exception e)
            {
                c_logger.error("The l10nProfile could not be obtained for job with id, state:"
                        + getId() + ",  " + getState() + " ::  " + e);
            }
        }

        if (m_l10nProfile == null)
        {
            List<UpdatedSourcePage> pages = UpdateSourcePageManager
                    .getAllUpdatedSourcePage(this);
            for (UpdatedSourcePage page : pages)
            {
                m_l10nProfile = page.getL10nProfile();
                break;
            }
        }

        return m_l10nProfile;
    }

    /*
     * Calculate the word count in the job.
     */
    public int getWordCount()
    {
        int totalWordCount = 0;
        if (m_overridenWordCount != null)
        {
            totalWordCount = m_overridenWordCount.intValue();
        }
        else
        {
            try
            {
                m_sourcePages = getSourcePages();
                for (SourcePage sp : m_sourcePages)
                {
                    totalWordCount += sp.getWordCount();
                    // WRONG!! Can't use 'totalWordCount' overwrite
                    // 'm_overridenWordCount'.
                    // Source page word count and job word count can be edited
                    // on job details UI.
                    // m_overridenWordCount = totalWordCount;
                }
            }
            catch (Exception e)
            {
                c_logger.error("The source pages could not be obtained");
            }
        }

        return totalWordCount;
    }

    /**
     * @see Job.overrideWordCount(int)
     */
    public void overrideWordCount(int p_wc)
    {
        m_overridenWordCount = new Integer(p_wc);
    }

    /**
     * @see Job.clearOverridenWordCount()
     */
    public void clearOverridenWordCount()
    {
        // removes what was set before
        // now the word count will be calculated
        m_overridenWordCount = null;
    }

    /**
     * @see Job.isWordCountOverriden()
     */
    public boolean isWordCountOverriden()
    {
        return (m_overridenWordCount != null);
    }

    /**
     * Returns all the workflows associated with this job.
     */
    public Collection<Workflow> getWorkflows()
    {
        return m_wfInstances;
    }

    /**
     * Returns 'true' if the job contains at least one failed workflow returns
     * 'false' if none of them are failed
     */
    public boolean containsFailedWorkflow()
    {
        boolean containsFailedWorkflow = false;

        for (Iterator it = getWorkflows().iterator(); !containsFailedWorkflow
                && it.hasNext();)
        {
            Workflow w = (Workflow) it.next();

            if (w.getState().equals(Workflow.IMPORT_FAILED))
            {
                containsFailedWorkflow = true;
            }
        }

        return containsFailedWorkflow;
    }

    public void setJobName(String p_jobName)
    {
        m_jobName = p_jobName;
    }

    public void setState(String p_state)
    {
        if (p_state != null)
        {
            p_state = p_state.toUpperCase();
        }

        m_state = p_state;

        setTimestamp(new Timestamp(System.currentTimeMillis()));
    }

    public String getState()
    {
        return m_state;
    }

    public String getDisplayState()
    {
        return getDisplayStateByLocale(Locale.US);
    }

    public String getDisplayStateByLocale(Locale locale)
    {
        String propertyKey = "";
        String defaultName = "";
        if ((m_state.equals(Job.PENDING))
                || (m_state.equals(Job.BATCHRESERVED))
                || (m_state.equals(Job.IMPORTFAILED)))
        {
            defaultName = "Pending";
            propertyKey = "lb_pending";
        }
        else if (m_state.equals(Job.READY_TO_BE_DISPATCHED))
        {
            defaultName = "Ready";
            propertyKey = "lb_ready";
        }
        else if (m_state.equals(Job.ADD_FILE))
        {
            defaultName = "Adding Files";
            propertyKey = "lb_addfiles";
        }
        else if (m_state.equals(Job.DISPATCHED))
        {
            defaultName = "In Progress";
            propertyKey = "lb_inprogress";
        }
        else if (m_state.equals(Job.LOCALIZED))
        {
            defaultName = "Localized";
            propertyKey = "lb_localized";
        }
        else if (m_state.equals(Job.DTPINPROGRESS))
        {
            defaultName = "DTP In Progress";
            propertyKey = "lb_dtpinprogress";
        }
        else if (m_state.equals(Job.EXPORTING))
        {
            defaultName = "Exporting";
            propertyKey = "lb_state_exporting";
        }
        else if (m_state.equals(Job.EXPORTED))
        {
            defaultName = "Exported";
            propertyKey = "lb_exported";
        }
        else if(m_state.equals(Job.EXPORT_FAIL))
        {
        	defaultName = "Export Failed";
            propertyKey = "lb_exported_failed";
        }
        else if (m_state.equals(Job.UPLOADING))
        {
            defaultName = "Uploading";
            propertyKey = "lb_state_uploading";
        }
        else if (m_state.equals(Job.IN_QUEUE))
        {
            defaultName = "In Queue";
            propertyKey = "lb_state_inqueue";
        }
        else if (m_state.equals(Job.EXTRACTING))
        {
            defaultName = "Extracting";
            propertyKey = "lb_state_extracting";
        }
        else if (m_state.equals(Job.LEVERAGING))
        {
            defaultName = "Leveraging";
            propertyKey = "lb_state_leveraging";
        }
        else if (m_state.equals(Job.CALCULATING_WORD_COUNTS))
        {
            defaultName = "Calculating Word Counts";
            propertyKey = "lb_state_CalculatingWordCounts";
        }
        else if (m_state.equals(Job.PROCESSING))
        {
            long fileCount = 0;
            Collection<Request> requests = getRequestList();
            Set<Long> pageNumbers = new HashSet<Long>(requests.size());
            for (Request r : requests)
            {
                if (fileCount == 0)
                {
                    fileCount = r.getBatchInfo().getPageCount();
                }
                long pageNumber = r.getBatchInfo().getPageNumber();
                if (!pageNumbers.contains(pageNumber))
                {
                    pageNumbers.add(pageNumber);
                }
            }
            int fileNumber = pageNumbers.size();
            defaultName = "Processing (" + fileNumber + " of " + fileCount
                    + ")";
        }
        else if (m_state.equals(Job.SKIPPING))
        {
            defaultName = "Skipping";
            propertyKey = "lb_state_skipping";
        }
        else
        {
            defaultName = "Archived";
            propertyKey = "lb_archived";
        }

        // get value from resource bundle
        SystemResourceBundle srb = SystemResourceBundle.getInstance();
        ResourceBundle rb = srb.getResourceBundle(
                ResourceBundleConstants.LOCALE_RESOURCE_NAME, locale);
        String result = null;
        try
        {
            result = rb.getString(propertyKey);
        }
        catch (MissingResourceException e)
        {
            result = defaultName;
        }

        return result;
    }

    public String getDataSourceName()
    {
        String result = "";
        String lastDataSourceName = null;

        try
        {
            m_sourcePages = ServerProxy.getJobHandler().getSourcePageByJobId(
                    getId());
            if (m_sourcePages.size() > 0)
            {
                Iterator it = m_sourcePages.iterator();
                boolean isSame = true;
                while (it.hasNext() && isSame == true)
                {
                    SourcePage sp = (SourcePage) it.next();
                    String dataSourceType = sp.getDataSourceType();
                    long dataSourceId = sp.getRequest().getDataSourceId();
                    String currentResult = getCurrentDataSourceName(
                            dataSourceType, dataSourceId);

                    if (currentResult.endsWith("_RFP"))
                        currentResult = currentResult.substring(0,
                                currentResult.lastIndexOf("_RFP"));

                    if (lastDataSourceName == null)
                    {
                        lastDataSourceName = currentResult;
                    }

                    if (currentResult.equals(lastDataSourceName))
                    {
                        isSame = true;
                        result = currentResult;
                    }
                    else
                    {
                        result = "MULTIPLE";
                        isSame = false;
                    }

                    lastDataSourceName = currentResult;
                }
            }
        }
        catch (Exception e)
        {
            c_logger.error("The request list could not be obtained", e);
        }

        return result;
    }

    /**
     * Returns the number of pages in the job. This defaults to the number of
     * requests but can be changed by the user.
     */
    public int getPageCount()
    {
        return m_pageCount;
    }

    /**
     * Sets the number of pages in a job. This defaults to the number of
     * requests, but can be overriden.
     */
    public void setPageCount(int p_numOfPages)
    {
        m_pageCount = p_numOfPages;
    }

    /**
     * @see Job.getLeverageMatchThreshold();
     */
    public int getLeverageMatchThreshold()
    {
        return m_leverageMatchThreshold;
    }

    /**
     * @see Job.setLeverageMatchThreshold(int);
     */
    public void setLeverageMatchThreshold(int p_leverageMatchThreshold)
    {
        m_leverageMatchThreshold = p_leverageMatchThreshold;
    }

    //
    // Private Methods
    //
    private String getCurrentDataSourceName(String p_dataSourceType,
            long p_dataSourceId) throws Exception
    {
        String result;

        if (p_dataSourceType.equals("db"))
        {
            result = getDBProfilePersistenceManager().getDatabaseProfile(
                    p_dataSourceId).getName();
        }
        else
        {
            result = HibernateUtil.get(FileProfileImpl.class, p_dataSourceId,
                    false).getName();
        }

        return result;
    }

    private DatabaseProfilePersistenceManager getDBProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getDatabaseProfilePersistenceManager();
    }

    private FileProfilePersistenceManager getFileProfilePersistenceManager()
            throws Exception
    {
        return ServerProxy.getFileProfilePersistenceManager();
    }

    /**
     * Add this Job comment to the collection.
     * 
     * @param p_comment
     *            - The comment to be added.
     */
    public void addJobComment(Comment p_comment)
    {
        m_jobComments.add(p_comment);
    }

    /**
     * Remove this job comment from the collection.
     * 
     * @param p_comment
     *            - The comment remove.
     */
    public void removeJobComment(Comment p_comment)
    {
        m_jobComments.remove(p_comment);
    }

    /**
     * Set the job comments to be this value.
     * 
     * @param p_comments
     *            - The task comments to be set.
     */
    public void setJobComments(List p_comments)
    {
        if (p_comments == null)
        {
            m_jobComments = new ArrayList();
        }
        else
        {
            m_jobComments = p_comments;
        }
    }

    public void setJobCommentSet(Set p_comments)
    {
        if (p_comments == null)
        {
            m_jobComments = new ArrayList();
        }
        else
        {
            m_jobComments = new ArrayList(p_comments);
        }
    }

    /**
     * Get the list of job comments.
     * 
     * @return a List of Comments for this task.
     */
    public List getJobComments()
    {
        return m_jobComments;
    }

    public Set getJobCommentSet()
    {
        Set comments = null;
        if (m_jobComments != null)
        {
            comments = new HashSet(m_jobComments);
        }
        return comments;
    }

    /**
     * Get a job comment
     * 
     * @return a Comment for this task.
     */
    public Comment getJobComment(long commentId)
    {
        for (Iterator it = m_jobComments.iterator(); it.hasNext();)
        {
            Comment comment = (Comment) it.next();

            if (comment.getId() == commentId)
            {
                return comment;
            }
        }

        return null;
    }

    public String toDebugString()
    {
        return "\nJob "
                + getIdAsLong().toString()
                + " toString start:\n"
                + super.toString()
                + " m_state="
                + (m_state != null ? m_state : "null")
                + " m_jobName="
                + (m_jobName != null ? m_jobName : "null")
                + " getSourceLocale="
                + (getSourceLocale() != null ? getSourceLocale()
                        .toDebugString() : "null")
                + " getWordCount="
                + Integer.toString(getWordCount())
                + " m_isWordCountReached="
                + new Boolean(m_isWordCountReached).toString()
                + " m_dispatchType="
                + getDispatchType()
                + " m_createDate="
                + (m_createDate != null ? m_createDate.toString() : "null")
                + " getDueDate="
                + (getDueDate() != null ? getDueDate().toString() : "null")
                + " getQuoteDate="
                + (getQuoteDate() != null ? getQuoteDate().toString() : "null")
                + " getDuration="
                + Long.toString(getDuration())
                + " m_dataSourceName="
                + (m_dataSourceName != null ? m_dataSourceName : "null")
                + "\ngetL10nProfile="
                + (getL10nProfile() != null ? getL10nProfile().toString()
                        : "null")
                + "\n"
                + " m_requestList="
                + (m_requestList != null ? m_requestList.toString() : "null")
                + "\n"
                + " m_wfInstances="
                + (m_wfInstances != null ? m_wfInstances.toString() : "null")
                + "\ngetSourcePages()="
                + (getSourcePages() != null ? getSourcePages().toString()
                        : "null") + "\nJob " + getIdAsLong().toString()
                + " toString end\n";
    }

    public String toString()
    {
        return getName() + getId();
    }

    /**
     * Get the quotation email date.
     * 
     * @return The date of the quotation email.
     */
    public String getQuoteDate()
    {
        return m_quoteDate;
    }

    /**
     * Set the quotation email date of this job.
     * <p>
     * 
     * @param p_quoteDate
     *            The quotation email date of this job.
     */
    public void setQuoteDate(String p_quoteDate)
    {
        m_quoteDate = p_quoteDate;
    }

    // For "Quote process webEx"
    /**
     * Set Quote Approved Date for job
     * 
     * @param p_quoteApprovedDate
     *            The Approved Quote Date of this job
     */
    public void setQuoteApprovedDate(String p_quoteApprovedDate)
    {
        m_quoteApprovedDate = p_quoteApprovedDate;
    }

    /**
     * Get the Approved Quote Date
     * 
     * @return The date of the approved quote
     */
    public String getQuoteApprovedDate()
    {
        return m_quoteApprovedDate;
    }

    /**
     * Set the quote PO Number for job
     * 
     * @param p_quotePoNumber
     *            The new Quote PO Number
     */
    public void setQuotePoNumber(String p_quotePoNumber)
    {
        m_quotePoNumber = p_quotePoNumber;
    }

    /**
     * Get The Quote PO Number of this job
     * 
     * @return The Quote PO Number
     */
    public String getQuotePoNumber()
    {
        // TODO Auto-generated method stub
        return m_quotePoNumber;
    }

    public void setUser(User p_user)
    {
        m_user = p_user;
    }

    public User getUser()
    {
        User user = null;
        if (m_user == null)
        {
            try
            {
                if (m_jauId != null)
                    user = UserHandlerHelper.getUser(m_jauId);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            setUser(user);
            return user;
        }
        return m_user;
    }

    public User getCreateUser()
    {
        if (m_createUser == null && m_createUserId != null)
        {
            try
            {
                m_createUser = UserHandlerHelper.getUser(m_createUserId);
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }
        }
        return m_createUser;
    }

    public String getUserId()
    {
        return m_jauId;
    }

    public void setUserId(String p_jauId)
    {
        m_jauId = p_jauId;
    }

    public Integer getOverridenWordCount()
    {
        return m_overridenWordCount;
    }

    public void setOverridenWordCount(Integer wordCount)
    {
        m_overridenWordCount = wordCount;
    }

    public String getJauId()
    {
        return m_jauId;
    }

    public void setJauId(String id)
    {
        m_jauId = id;
    }

    public boolean getIsWordCountReached()
    {
        return m_isWordCountReached;
    }

    public void setIsWordCountReached(boolean wordCountReached)
    {
        m_isWordCountReached = wordCountReached;
    }

    public void setDispatchType(String type)
    {
        m_dispatchType = type;
    }

    public void setDataSourceName(String sourceName)
    {
        m_dataSourceName = sourceName;
    }

    /**
     * @deprecated use {@link JobImpl#setL10nProfileId(Long)} for update database!
     * @param profile
     */
    public void setL10nProfile(L10nProfile profile)
    {
        m_l10nProfile = profile;
    }

    public void setRequestList(Collection<Request> list)
    {
        m_requestList = list;
    }

    public void setRequestSet(Collection set)
    {
        m_requestList = set;
    }

    public Set getRequestSet()
    {
        Set request = null;
        if (m_requestList != null)
        {
            request = new HashSet(m_requestList);
        }
        return request;
    }

    public void setWorkflowRequestList(Collection requestList)
    {
        m_workflowRequestList = requestList;
    }

    public void setWorkflowRequestSet(Set requestList)
    {
        if (requestList == null)
        {
            m_workflowRequestList = new ArrayList();
        }
        else
        {
            m_workflowRequestList = new ArrayList(requestList);
        }
    }

    public Collection getWfInstances()
    {
        return m_wfInstances;
    }

    public void setWfInstances(Collection instances)
    {
        m_wfInstances = new HashSet<Workflow>(instances);
    }

    public void setSourcePages(Collection<SourcePage> pages)
    {
        m_sourcePages = pages;
    }

    @Override
    public FileProfile getFileProfile()
    {

        try
        {
            m_sourcePages = ServerProxy.getJobHandler().getSourcePageByJobId(
                    getId());
            if (m_sourcePages.size() > 0)
            {
                long dataSourceId = ((SourcePage) m_sourcePages.iterator()
                        .next()).getRequest().getDataSourceId();
                return getFileProfilePersistenceManager().readFileProfile(
                        dataSourceId);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<FileProfile> getAllFileProfiles()
    {
        ArrayList<FileProfile> prifiles = new ArrayList<FileProfile>();
        try
        {
            m_sourcePages = ServerProxy.getJobHandler().getSourcePageByJobId(
                    getId());

            if (m_sourcePages.size() > 0)
            {
                for (Iterator it = m_sourcePages.iterator(); it.hasNext();)
                {
                    long dataSourceId = ((SourcePage) it.next()).getRequest()
                            .getDataSourceId();
                    FileProfile fp = getFileProfilePersistenceManager()
                            .readFileProfile(dataSourceId);
                    prifiles.add(fp);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return prifiles;
    }

    public String getLeverageOption()
    {
        return this.leverageOption;
    }

    public void setLeverageOption(String leverageOption)
    {
        this.leverageOption = leverageOption;
    }

    public String getCreateUserId()
    {
        return m_createUserId;
    }

    public void setCreateUserId(String userId)
    {
        m_createUserId = userId;
    }

    public List<JobAttribute> getAttributesAsList()
    {
        List<JobAttribute> atts = new ArrayList<JobAttribute>();
        if (attributes != null)
        {
            atts.addAll(attributes);
        }

        return atts;
    }

    public List<AttributeClone> getAllAttributeAsList()
    {
        List<AttributeClone> atts = new ArrayList<AttributeClone>();
        if (attributes != null)
        {
            for (JobAttribute jobAtt : attributes)
            {
                atts.add(jobAtt.getAttribute());
            }
        }

        return atts;
    }

    public List<JobAttribute> getAllJobAttributes()
    {
        List<JobAttribute> atts = new ArrayList<JobAttribute>();
        Set<JobAttribute> jobAtts = getAttributes();
        if (jobAtts != null)
        {
            atts.addAll(jobAtts);
        }

        return atts;
    }

    public Set<JobAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(Set<JobAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public AttributeSet getAttributeSet()
    {
        if (attributeSet == null)
        {
            L10nProfile l10nProfile = getL10nProfile();
            if (l10nProfile == null)
                return attributeSet;

            Project project = l10nProfile.getProject();
            if (project == null)
                return attributeSet;

            attributeSet = project.getAttributeSet();

        }

        return attributeSet;
    }

    public List<AttributeClone> getVisitbleAttributeAsList()
    {
        List<AttributeClone> atts = new ArrayList<AttributeClone>();
        List<AttributeClone> attsets = getAllAttributeAsList();
        for (AttributeClone att : attsets)
        {
            if (att.isVisible())
            {
                atts.add(att);
            }
        }

        return atts;
    }

    public Map<AttributeClone, JobAttribute> getAttriguteMap()
    {
        if (attriguteMap == null)
        {
            attriguteMap = new HashMap<AttributeClone, JobAttribute>();

            Set<JobAttribute> jobAttributes = getAttributes();
            if (jobAttributes != null)
            {
                for (JobAttribute jobAttribute : jobAttributes)
                {
                    attriguteMap.put(jobAttribute.getAttribute(), jobAttribute);
                }
            }
        }

        return attriguteMap;
    }

    public synchronized static String createUuid()
    {
        UUID_PREFIX++;
        Date date = new Date();
        return "uuid" + UUID_PREFIX + date.getTime();
    }

    public void initUuid()
    {
        setUuid(createUuid());
    }

    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    /**
     * Not only cost center but also the required attribute will be checked.
     */
    @SuppressWarnings(
    { "unchecked", "rawtypes", "unused" })
    public boolean hasSetCostCenter()
    {
        synchronized (AddJobAttributeThread.getLock(uuid))
        {
            // do nothing, wait until attribute are set.
            int i = 0;
        }

        String hql = "from JobAttribute j where j.job.id = :jobId";
        Map map = new HashMap();
        map.put("jobId", getIdAsLong());

        List<JobAttribute> atts = (List<JobAttribute>) HibernateUtil.search(
                hql, map);
        if (atts == null || atts.size() == 0)
            return true;

        CostCenterAction action = new CostCenterAction();
        for (JobAttribute att : atts)
        {
            action.run(att);
            
            if (att != null && att.getAttribute().isRequired() && !att.isSet())
            {
                c_logger.info("The job can not be dispatched until required attribute is set.");
                return false;
            }
        }

        if (!action.isSeted())
        {
            c_logger.info("The job can not be dispatched until cost center attribute is set.");
        }

        return action.isSeted();
    }

    public String getOrgState()
    {
        return m_orgState;
    }

    public void setOrgState(String state)
    {
        m_orgState = state;
    }

    public String canAddSourceFiles()
    {
        List<UpdatedSourcePage> uPages = UpdateSourcePageManager
                .getAllUpdatedSourcePage(this);
        if (uPages.size() > 0)
        {
            return "msg_cannot_add_delete_file2";
        }

        List<AddingSourcePage> aPages = AddingSourcePageManager
                .getAllAddingSourcePage(this);
        if (aPages.size() > 0)
        {
            return "msg_cannot_add_delete_file2";
        }

        if (Job.READY_TO_BE_DISPATCHED.equals(getState())
                || Job.DISPATCHED.equals(getState()))
        {
            Collection<Workflow> ws = this.getWorkflows();
            for (Workflow w : ws)
            {
                if (Workflow.CANCELLED.equals(w.getState()))
                {
                    continue;
                }

                Hashtable<Long, TaskImpl> tasks = w.getTasks();
                for (TaskImpl task : tasks.values())
                {
                    if (task.getAcceptedDate() != null)
                    {
                        return "msg_cannot_add_delete_file";
                    }
                }
            }

            return null;
        }

        return "msg_cannot_add_delete_file";
    }

    public long getProjectId()
    {
        return getL10nProfile().getProjectId();
    }

    public Project getProject()
    {
        return getL10nProfile().getProject();
    }

    /**
     * The job is create from web service or not.
     * 
     * @return true if the job is create from web service
     */
    public boolean isFromWebService()
    {
        Collection<SourcePage> pages = getSourcePages();
        if (pages != null)
        {
            for (SourcePage page : pages)
            {
                String path = page.getExternalPageId();
                if (path != null)
                {
                    path = path.replace("\\", "/");
                    String[] parts = path.split("/");
                    if (parts.length > 1 && "webservice".equals(parts[1]))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean hasPassoloFiles()
    {
        Collection<SourcePage> sps = this.getSourcePages();
        for (SourcePage sp : sps)
        {
            if (sp.isPassoloPage())
                return true;
        }
        return false;
    }

    public Date getCompletedDate()
    {
        return completedDate;
    }

    public void setCompletedDate(Date completedDate)
    {
        this.completedDate = completedDate;
    }

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public void setIsMigrated(boolean p_isMigrated)
    {
        this.isMigrated = p_isMigrated;
    }

    public boolean getIsMigrated()
    {
        return isMigrated;
    }

    public boolean isMigrated()
    {
        return isMigrated;
    }

    public boolean getIsAllRequestGenerated()
    {
        return isAllRequestGenerated;
    }

    public void setIsAllRequestGenerated(boolean isAllRequestGenerated)
    {
        this.isAllRequestGenerated = isAllRequestGenerated;
    }

    public String getLmArchiveTable()
    {
        return lmArchiveTable;
    }

    public void setLmArchiveTable(String lmArchiveTable)
    {
        this.lmArchiveTable = lmArchiveTable;
    }

    public String getLmTable()
    {
        return lmTable;
    }

    public void setLmTable(String lmTable)
    {
        this.lmTable = lmTable;
    }

    public String getTuvArchiveTable()
    {
        return tuvArchiveTable;
    }

    public void setTuvArchiveTable(String tuvArchiveTable)
    {
        this.tuvArchiveTable = tuvArchiveTable;
    }

    public String getLmExtTable() {
		return lmExtTable;
	}

	public void setLmExtTable(String lmExtTable) {
		this.lmExtTable = lmExtTable;
	}

	public String getLmExtArchiveTable() {
		return lmExtArchiveTable;
	}

	public void setLmExtArchiveTable(String lmExtArchiveTable) {
		this.lmExtArchiveTable = lmExtArchiveTable;
	}

	public String getTuvTable()
    {
        return tuvTable;
    }

    public void setTuvTable(String tuvTable)
    {
        this.tuvTable = tuvTable;
    }

    public String getTuArchiveTable()
    {
        return tuArchiveTable;
    }

    public void setTuArchiveTable(String tuArchiveTable)
    {
        this.tuArchiveTable = tuArchiveTable;
    }

    public String getTuTable()
    {
        return tuTable;
    }

    public void setTuTable(String tuTable)
    {
        this.tuTable = tuTable;
    }

    public String getJobType()
    {
    	return jobType;
    }

    public void setJobType(String jobType)
    {
    	this.jobType = jobType;
    }

    // Utility
    public boolean isBlaiseJob()
    {
    	return Job.JOB_TYPE_BLAISE.equalsIgnoreCase(jobType);
    }

    public boolean isCotiJob()
    {
    	return Job.JOB_TYPE_COTI.equalsIgnoreCase(jobType);
    }

    public boolean isEloquaJob()
    {
    	return Job.JOB_TYPE_ELOQUA.equalsIgnoreCase(jobType);
    }

    public boolean isGitJob()
    {
    	return Job.JOB_TYPE_GIT.equalsIgnoreCase(jobType);
    }

    public boolean isMindTouchJob()
    {
    	return Job.JOB_TYPE_MINDTOUCH.equalsIgnoreCase(jobType);
    }
}
