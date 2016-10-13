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
package com.globalsight.everest.projecthandler;

public class ProjectInfo implements java.io.Serializable
{
    private long m_projectId = -1;
    private String m_name = null;
    private String m_description = null;
    private long m_companyId = -1;
    private String m_userId = null;
    private String m_projectManagerName = null;
    private String m_termbaseName = null;
    private float PMCost = 0.1f;
    private boolean reviewOnlyAutoAccept = false;
    private boolean reviewOnlyAutoSend = false;
    private boolean autoAcceptPMTask = false;
    private boolean checkUnTranslatedSegments = false;
    private boolean saveTranslationsEditReport = true;
    private boolean saveReviewersCommentsReport = true;
    private boolean saveOfflineFiles = true;
    private String attributeSetName;

    /**
     * Default Constructor
     */
    public ProjectInfo(ProjectImpl pp)
    {
        m_projectId = pp.getId();
        m_name = pp.getName();
        m_description = pp.getDescription();
        m_userId = pp.getManagerUserId();
        m_termbaseName = pp.getTermbaseName();
        m_companyId = pp.getCompanyId();
        PMCost = pp.getPMCost();
        reviewOnlyAutoAccept = pp.getReviewOnlyAutoAccept();
        reviewOnlyAutoSend = pp.getReviewOnlyAutoSend();
        autoAcceptPMTask = pp.getAutoAcceptPMTask();
        checkUnTranslatedSegments = pp.isCheckUnTranslatedSegments();
        saveTranslationsEditReport = pp.getSaveTranslationsEditReport();
        saveReviewersCommentsReport = pp.getSaveReviewersCommentsReport();
        saveOfflineFiles = pp.getSaveOfflineFiles();
        attributeSetName = pp.getAttributeSet() == null ? "" : pp
                .getAttributeSet().getName();
    }

    /**
     * Default Constructor
     */
    public ProjectInfo(long p_projectId, String p_name, String p_description,
            String p_userId, String p_termbaseName)
    {
        m_projectId = p_projectId;
        m_name = p_name;
        m_description = p_description;
        m_userId = p_userId;
        m_termbaseName = p_termbaseName;
    }

    /**
     * Get the project name.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the project description.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Get the project id
     */
    public long getProjectId()
    {
        return m_projectId;
    }

    /**
     * Get the project manager userid
     */
    public String getProjectManagerId()
    {
        return m_userId;
    }

    /**
     * Get the project manager full name
     */
    public String getProjectManagerName()
    {
        return m_projectManagerName;
    }

    /**
     * Set the project manager full name. This attribute is not saved in
     * database, but retrieved later on from UserManager and set.
     * 
     * This value is set in ProjectHandlerLocal. We could have done this in
     * ResultHandler in persistence service also when we are creating an
     * instance of this object.
     */
    public void setProjectManagerName(String p_projectManagerName)
    {
        m_projectManagerName = p_projectManagerName;
    }

    public String getTermbaseName()
    {
        return m_termbaseName;
    }

    public long getCompanyId()
    {
        return m_companyId;
    }

    public boolean isReviewOnlyAutoAccept()
    {
        return reviewOnlyAutoAccept;
    }

    public void setReviewOnlyAutoAccept(boolean reviewOnlyAutoAccept)
    {
        this.reviewOnlyAutoAccept = reviewOnlyAutoAccept;
    }

    public boolean isReviewOnlyAutoSend()
    {
        return reviewOnlyAutoSend;
    }

    public void setReviewOnlyAutoSend(boolean reviewOnlyAutoSend)
    {
        this.reviewOnlyAutoSend = reviewOnlyAutoSend;
    }

    public boolean isAutoAcceptPMTask()
    {
        return autoAcceptPMTask;
    }

    public void setAutoAcceptPMTask(boolean autoAcceptPMTask)
    {
        this.autoAcceptPMTask = autoAcceptPMTask;
    }

    /**
     * Returns a string representation of the object (based on the object name).
     */
    public String toString()
    {
        return getName();
    }

    public String getAttributeSetName()
    {
        return attributeSetName;
    }

    public void setAttributeSetName(String attributeSetName)
    {
        this.attributeSetName = attributeSetName;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (m_projectId ^ (m_projectId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectInfo other = (ProjectInfo) obj;
        if (m_projectId != other.m_projectId)
            return false;
        return true;
    }

    public boolean isCheckUnTranslatedSegments()
    {
        return checkUnTranslatedSegments;
    }

    public void setCheckUnTranslatedSegments(boolean checkUnTranslatedSegments)
    {
        this.checkUnTranslatedSegments = checkUnTranslatedSegments;
    }

    public float getPMCost()
    {
        return PMCost;
    }

    public void setPMCost(float pMCost)
    {
        PMCost = pMCost;
    }

    public boolean saveTranslationsEditReport()
    {
        return saveTranslationsEditReport;
    }

    public void saveTranslationsEditReport(boolean saveTranslationsEditReport)
    {
        this.saveTranslationsEditReport = saveTranslationsEditReport;
    }

    public boolean saveReviewersCommentsReport()
    {
        return saveReviewersCommentsReport;
    }

    public void saveReviewersCommentsReport(boolean saveReviewersCommentsReport)
    {
        this.saveReviewersCommentsReport = saveReviewersCommentsReport;
    }

    public boolean saveOfflineFiles()
    {
        return saveOfflineFiles;
    }

    public void saveOfflineFiles(boolean saveOfflineFiles)
    {
        this.saveOfflineFiles = saveOfflineFiles;
    }
}
