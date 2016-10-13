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

package com.globalsight.everest.secondarytargetfile;

import com.globalsight.everest.page.GenericPage;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.workflowmanager.Workflow;

public class SecondaryTargetFile extends PersistentObject implements
        GenericPage
{
    private static final long serialVersionUID = -4987462061582510613L;

    private long m_fileSize = 0;

    private long m_lastUpdatedTime = 0;

    private String m_eventFlowXml = null;

    private String m_modifierUserId = null;

    private String m_state = null;

    private String m_storagePath = null;

    private Workflow m_workflow = null;
    
    private int m_bomType = 0;

    // ////////////////////////////////////////////////////////////////////
    // Begin: Constructor
    // ////////////////////////////////////////////////////////////////////
    /**
     * Default constructor used by TOPLink.
     */
    public SecondaryTargetFile()
    {
        super();
    }

    /**
     * Constructor for initial settings of a secondary target file.
     */
    public SecondaryTargetFile(String p_eventFlowXml, String p_modifierUserId,
            String p_state, String p_storagePath)
    {
        m_eventFlowXml = p_eventFlowXml;
        m_modifierUserId = p_modifierUserId;
        m_state = p_state;
        m_storagePath = p_storagePath;
//        m_storagePath = m_storagePath.replace('\\', '/');
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Constructor
    // ////////////////////////////////////////////////////////////////////

    // ////////////////////////////////////////////////////////////////////
    // Begin: Public Methods
    // ////////////////////////////////////////////////////////////////////
    /**
     * Get the event flow xml as string.
     * 
     * @return The string representation of the EventFlowXml.
     */
    public String getEventFlowXml()
    {
        return m_eventFlowXml;
    }

    /**
     * Get the size of this secondary target file in bytes.
     * 
     * @return The file size in bytes.
     */
    public long getFileSize()
    {
        return m_fileSize;
    }

    /**
     * Get the latest time in millisecond that this file was updated.
     * 
     * @return The update time in millisecond.
     */
    public long getLastUpdatedTime()
    {
        return m_lastUpdatedTime;
    }

    /**
     * Get the username of the person who has modified this file.
     * 
     * @return The last modifier of this file's user name.
     */
    public String getModifierUserId()
    {
        return m_modifierUserId;
    }

    /**
     * Get the state for this secondary target file. The state could be
     * 'ACTIVE_JOB', 'LOCALIZED', 'OUT_OF_DATE', 'EXPORTED',
     * 'EXPORT_IN_PROGRESS', or 'EXPORT_FAIL'
     * 
     * @return The state of this exporting page.
     */
    public String getState()
    {
        return m_state;
    }

    /**
     * Get the file's path relative to the system-wide defined root. This root
     * is defined during the installation of System4 plus the fixed suffix used
     * as the STF directory.
     * 
     * @return The file's storage path relative to the defined root.
     */
    public String getStoragePath()
    {
        return m_storagePath;
    }

    /**
     * Get the workflow where this secondary target file belongs to.
     * 
     * @return The workflow object for this file.
     */
    public Workflow getWorkflow()
    {
        return m_workflow;
    }

    /**
     * Set the event flow xml for this file to be the specified value.
     * 
     * @param p_eventFlowXml -
     *            The event flow xml to be set.
     */
    public void setEventFlowXml(String p_eventFlowXml)
    {
        m_eventFlowXml = p_eventFlowXml;
    }

    /**
     * Set the size of this file to be the specified value.
     * 
     * @param p_fileSize -
     *            The file size to be set.
     */
    public void setFileSize(long p_fileSize)
    {
        m_fileSize = p_fileSize;
    }

    /**
     * Set the time that the file was updated to the specified value.
     * 
     * @param p_lastUpdatedTime -
     *            The time which this file was updated in milliseconds.
     */
    public void setLastUpdatedTime(long p_lastUpdatedTime)
    {
        m_lastUpdatedTime = p_lastUpdatedTime;
    }

    /**
     * Set the user id of the person who modified this file to the specified
     * value.
     * 
     * @param p_modifierUserId -
     *            The user id to be set.
     */
    public void setModifierUserId(String p_modifierUserId)
    {
        m_modifierUserId = p_modifierUserId;
    }

    /*
     * Set the state of this exporting page.
     * 
     * @param p_state The state of this exporting page. Note that only
     * 'ACTIVE_JOB', 'LOCALIZED', 'OUT_OF_DATE', 'EXPORTED',
     * 'EXPORT_IN_PROGRESS', or 'EXPORT_FAIL' are valid states.
     */
    public void setState(String p_state)
    {
        m_state = p_state;
    }

    /**
     * Set the storage path for this file to be the specifed value.
     * 
     * @param p_storagePath -
     *            The storage path to be set.
     */
    public void setStoragePath(String p_storagePath)
    {
        m_storagePath = p_storagePath;
    }

    /**
     * Set the workflow which this file belongs to.
     * 
     * @param p_workflow -
     *            The workflow to be set.
     */
    public void setWorkflow(Workflow p_workflow)
    {
        m_workflow = p_workflow;
    }

    // ////////////////////////////////////////////////////////////////////
    // End: Public Methods
    // ////////////////////////////////////////////////////////////////////

    /**
     * Logically delete this SecondaryTargetFile object by deactivating it.
     */
    void deactivate()
    {
        this.isActive(false);
    }

    public String toString()
    {
        return m_fileSize + ", " + m_lastUpdatedTime + ", " + m_eventFlowXml
                + ", " + m_modifierUserId + ", " + m_storagePath;
    }

    /**
     * @param m_bomType the m_bomType to set
     */
    public void setBOMType(int m_bomType)
    {
        this.m_bomType = m_bomType;
    }

    /**
     * @return the m_bomType
     */
    public int getBOMType()
    {
        return m_bomType;
    }
}
