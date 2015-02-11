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
package com.globalsight.everest.page.pageexport;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.globalsight.everest.jobhandler.Job;
import com.globalsight.everest.page.Page;
import com.globalsight.everest.persistence.PersistentObject;

/**
 * ExportBatchEvent holds information about an export process. This info is
 * stored in the database for later processing (i.e. creation of secondary
 * target files, email notification for batch export status, and possibly
 * monitoring the whole process).
 */
public class ExportBatchEvent extends PersistentObject
{
    private static final long serialVersionUID = -7709778269789066668L;

    /* The page types for the export process */
    public final static String SOURCE = "SOURCE";
    public final static String PRIMARY_TARGET = "PRIMARY_TARGET";
    public final static String SECONDARY_TARGET = "SECONDARY_TARGET";

    /* The export types */
    public final static String INTERIM_PRIMARY = "INTERIM_PRIMARY";
    public final static String INTERIM_SECONDARY = "INTERIM_SECONDARY";
    public final static String FINAL_PRIMARY = "FINAL_PRIMARY";
    public final static String FINAL_SECONDARY = "FINAL_SECONDARY";
    public final static String EXPORT_SOURCE = "EXPORT_SOURCE";
    public final static String CREATE_STF = "CREATE_STF";

    private Job m_job = null;
    private long m_endTime = 0;
    private long m_startTime = 0;
    private Long m_taskId = null; // Long is used to avoid inserting 0 or -1
    // in db
    private List m_exportingPages = new Vector();
    private List m_workflowIds = new ArrayList();
    private String m_exportType = null;
    private String m_responsibleUserId = null;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructors
    // ////////////////////////////////////////////////////////////////////
    /**
     * Default constructor used by TOPLink.
     */
    public ExportBatchEvent()
    {
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructors
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the time where this export process was completed.
     * 
     * @return The end time of the export process in milliseconds.
     */
    public long getEndTime()
    {
        return m_endTime;
    }

    /*
     * Set the end date for this export event to be the specified value.
     * 
     * @param p_endTime The end time to be set.
     */
    public void setEndTime(long p_endTime)
    {
        m_endTime = p_endTime;
    }

    /**
     * Get a list of exporting pages (as ExportingPage objects) that are part of
     * this export batch.
     * 
     * @return A list of exporting pages.
     */
    public List getExportingPages()
    {
        return m_exportingPages;
    }
    
    public void setExportingPages(List exportingPages)
    {
        this.m_exportingPages = exportingPages;
    }

    /**
     * Get the export type for this event. The export type could be
     * 'INTERIM_EXPORT', 'FINAL_EXPORT', 'EXPORT_SOURCE', or 'CREATE_STF'
     * depending on how the export was invoked.
     * 
     * @return The export type for this event.
     */
    public String getExportType()
    {
        return m_exportType;
    }

    /*
     * Set the export type for this event to be the specified value. The valid
     * export types are 'INTERIM_EXPORT', 'FINAL_EXPORT', 'EXPORT_SOURCE', and
     * 'CREATE_STF'.
     * 
     * @param p_exportType The export type to be set.
     */
    public void setExportType(String p_exportType)
    {
        m_exportType = p_exportType;
    }

    /**
     * Get the job object that this export process belongs to.
     * 
     * @return The job object for the exported page(s)/workflow(s).
     */
    public Job getJob()
    {
        return m_job;
    }

    /*
     * Set the job for this export event to be the specified value.
     * 
     * @param p_job The job to be set.
     */
    public void setJob(Job p_job)
    {
        m_job = p_job;
    }

    /**
     * Get the user id of the individual who invoked the process. For automatic
     * export, the PM is the responsible person.
     * 
     * @return The responsible user id for this batch export process.
     */
    public String getResponsibleUserId()
    {
        return m_responsibleUserId;
    }

    /*
     * Set the responsible user id for this export event to be the specified
     * value.
     * 
     * @param p_responsibleUserId The user id of the individual responsible for
     * invoking the export process.
     */
    public void setResponsibleUserId(String p_responsibleUserId)
    {
        m_responsibleUserId = p_responsibleUserId;
    }

    /**
     * Get the time in milliseconds where this export process was started.
     * 
     * @return The start time of the export process in milliseconds.
     */
    public long getStartTime()
    {
        return m_startTime;
    }

    /*
     * Set the start time for this export event to be the specified value.
     * 
     * @param p_startTime The start time to be set in milliseconds.
     */
    public void setStartTime(long p_startTime)
    {
        m_startTime = p_startTime;
    }

    /**
     * Get the task id from which this export was started. Note that a valid id
     * will only be returned if the export was invoked based on a specific
     * activity (i.e. creation of secondary target files from a particular
     * activity).
     * 
     * @return The task id.
     */
    public Long getTaskId()
    {
        return m_taskId;
    }

    /*
     * Set the task id for this export event to be the specified value.
     * 
     * @param p_taskId The task id to be set.
     */
    public void setTaskId(Long p_taskId)
    {
        m_taskId = p_taskId;
    }

    /**
     * Get A list of workflow ids based on the exporting pages.
     * 
     * @return A list of workflow ids.
     */
    public List getWorkflowIds()
    {
        return m_workflowIds;
    }

    /*
     * Set the workflow ids for this export event to be the specified value.
     * 
     * @param p_workflowIds The list of workflow ids to be set.
     */
    public void setWorkflowIds(List p_workflowIds)
    {
        // since the list loaded thru TOPLink contians BigDecimals,
        // we need to convert them all to Long objects. This is the
        // only known solution.
        // int size = p_workflowIds == null ? 0 : p_workflowIds.size();
        //
        // for (int i = 0; i < size; i++)
        // {
        // Long id = new Long(((Number) p_workflowIds.get(i)).longValue());
        // m_workflowIds.add(id);
        // }
        m_workflowIds = p_workflowIds == null ? new ArrayList() : p_workflowIds;
    }

    /*
     * Get the ExportingPage from this batch export event. An ExportingPage is a
     * wrapper around an actual page being exported. If the requested PageId
     * does not exist in this batch, null is returned
     * 
     * NOTE: We use to have a hashtable holding the page info for easy lookup
     * (before the persistance updates). If we add that back, we would have to
     * make it persistant to.
     * 
     * @param p_pageId.
     */
    public ExportingPage getExportingPage(long p_pageId)
    {
        ExportingPage ep = null;
        boolean found = false;
        for (int i = 0; !found && i < m_exportingPages.size(); i++)
        {
            ep = (ExportingPage) m_exportingPages.get(i);
            // TODO: This cast will not work when we get an STF as an exporting
            // page
            found = (((Page) ep.getPage()).getId() == p_pageId);
        }
        return ep;
    }

    /*
     * Determine if the batch export has completed.
     * 
     * @return true if the batch has completed, otherwise false.
     */
    public boolean isCompleted()
    {
        ExportingPage ep = null;
        boolean isDone = true;

        for (int i = 0; isDone && i < m_exportingPages.size(); i++)
        {
            ep = (ExportingPage) m_exportingPages.get(i);
            isDone = !ep.getState().equals(ExportingPage.EXPORT_IN_PROGRESS);
        }

        return isDone;
    }

    /*
     * Determine if the batch export was successful.
     * 
     * @return true if the batch was successful, otherwise false.
     */
    public boolean isExportSuccess()
    {
        ExportingPage ep = null;
        boolean isDone = true;
        for (int i = 0; isDone && i < m_exportingPages.size(); i++)
        {
            ep = (ExportingPage) m_exportingPages.get(i);
            isDone = !ep.getState().equals(ExportingPage.EXPORT_FAIL);
        }
        return isDone;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("jobId="
                + ((m_job == null) ? "null" : Long.toString(m_job.getId())));
        sb.append(", startTime=" + m_startTime);
        sb.append(", endTime=" + m_endTime);
        sb
                .append(", workflowIds="
                        + ((m_workflowIds == null) ? "null" : m_workflowIds
                                .toString()));
        sb.append(", taskId=" + m_taskId);
        sb.append(", exportingPages="
                + ((m_exportingPages == null) ? "null" : m_exportingPages
                        .toString()));
        sb.append(", exportType="
                + ((m_exportType == null) ? "null" : m_exportType));
        sb
                .append(", responsibleUserId="
                        + ((m_responsibleUserId == null) ? "null"
                                : m_responsibleUserId));
        return sb.toString();
    }

    // ///////////////////////////////////////////////////////////////////
    // End: Public Methods
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Package Level Methods
    // ////////////////////////////////////////////////////////////////////
    /*
     * Set the list of the exporting pages to be the specified list.
     * 
     * @param p_exportingPages The exporting pages that are part of this event.
     */
    void addExportingPages(ExportingPage p_exportingPage)
    {
        p_exportingPage.setExportBatchEvent(this);
        m_exportingPages.add(p_exportingPage);
    }
    // ////////////////////////////////////////////////////////////////////
    // End: Package Level Methods
    // ////////////////////////////////////////////////////////////////////

}
