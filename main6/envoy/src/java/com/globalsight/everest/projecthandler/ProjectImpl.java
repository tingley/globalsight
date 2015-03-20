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

// java
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.foundation.User;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.servlet.util.ServerProxy;

/**
 * This interface provides access to the project in Envoy.
 * <p>
 */
public class ProjectImpl extends PersistentObject implements Project,
        Serializable
{
    private static final long serialVersionUID = 1620074336358442246L;
    private static final Logger CATEGORY = Logger.getLogger(ProjectImpl.class
            .getName());
    // Used for TOPLink Querying
    public static final String PM_USER_ID = "m_userId";
    public static final String QP_USER_ID = "m_qpId";
    public static final String USER_IDS = "m_userIds";

    private String projectManagerName;

    private String m_description = null;
    private String m_name = null;
    private User m_projectManager = null;
    private User m_quotePerson = null;
    private String m_userId = null;
    private String m_qpId = null;
    private String m_termbase = null;
    private long m_companyId = -1;
    // holds the list of users that are associated with this project.
    // it doesn't hold duplicates
    private Set m_userIds = new TreeSet();
    private AttributeSet attributeSet;

    private float m_pmcost = 0.1f;

    private int poRequired = 1;

    // Auto-accept task and Auto-send report options.
    private boolean autoAcceptTrans = false; // Auto-accept Translation Task
    private boolean autoSendTrans = false; // Auto-send Translations Comments Report
    private boolean reviewOnlyAutoAccept = false; // Auto-accept Review Task
    private boolean reviewOnlyAutoSend = false; // Auto-send Reviewers Comments
                                                // Report
    private boolean reviewReportIncludeCompactTags = false; // Reviewers
                                                            // Comments Report
                                                            // include Compact
                                                            // Tags
    private boolean autoAcceptPMTask = false; // Auto-accept PM Task

    private boolean checkUnTranslatedSegments = false;
    // GBS-3115
    private boolean saveTranslationsEditReport = true;
    private boolean saveReviewersCommentsReport = true;
    private boolean saveOfflineFiles = true;
    private boolean allowManualQAChecks = false;
    private boolean autoAcceptQATask = false;
    private boolean autoSendQAReport = false;

    // GBS-3704
    private boolean manualRunDitaChecks = false;
    private boolean autoAcceptDitaQaTask = false;
    private boolean autoSendDitaQaReport = false;

    /**
     * Constructor.
     */
    public ProjectImpl()
    {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param p_name
     *            The name of the project.
     */
    public ProjectImpl(String p_name)
    {
        this(p_name, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param p_name
     *            The name of the project.
     * @param p_description
     *            The description of the project.
     */
    public ProjectImpl(String p_name, String p_description)
    {
        this(p_name, p_description, null, null);
    }

    /**
     * Constructor.
     * 
     * @param p_name
     *            The name of the project.
     * @param p_description
     *            The description of the project.
     * @param p_projectManager
     *            The user assigned to the project.
     */
    public ProjectImpl(String p_name, String p_description,
            User p_projectManager)
    {
        this(p_name, p_description, p_projectManager, null);
    }

    /**
     * Constructor.
     * 
     * @param p_name
     *            The name of the project.
     * @param p_description
     *            The description of the project.
     * @param p_projectManager
     *            The user assigned to the project.
     * @param p_quotePerson
     *            The user assigned to receive the quotation email.
     */
    public ProjectImpl(String p_name, String p_description,
            User p_projectManager, Object p_quotePerson)
    {
        m_name = p_name;
        m_description = p_description;
        m_projectManager = p_projectManager;

        if (p_projectManager != null)
        {
            m_userId = p_projectManager.getUserId();
        }
        setQuotePerson(p_quotePerson);
    }

    /**
     * Get the description of this project.
     * <p>
     * 
     * @return Returns the description of this project as a String object.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Get the name of this project.
     * <p>
     * 
     * @return Returns the name of this project.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the project manager who is assigned to this project.
     * <p>
     * 
     * @return The project manager (user object) who is assigned to this
     *         project.
     */
    public User getProjectManager()
    {
        if (m_projectManager == null && m_userId != null)
        {
            try
            {
                m_projectManager = ServerProxy.getUserManager().getUser(
                        m_userId);
            }
            catch (Exception e)
            {
                CATEGORY.warn("Failed to get project manager " + m_userId
                        + " from LDAP.");
            }
        }
        return m_projectManager;
    }

    /**
     * Get the project manager id.
     * <p>
     * 
     * @return The user id of the project manager.
     */
    public String getProjectManagerId()
    {
        return m_userId;
    }

    /**
     * Get the quote person id.
     * <p>
     * 
     * @return The user id of the quote person.
     */
    public String getQuotePersonId()
    {
        return m_qpId;
    }

    /**
     * Get the default termbase of this project.
     * <p>
     * 
     * @return The name of the default termbase of this project.
     */
    public String getTermbaseName()
    {
        return m_termbase;
    }

    /**
     * Get the company id of this project.
     * <p>
     * 
     * @return The name of the company id of this project.
     */
    public long getCompanyId()
    {
        return m_companyId;
    }

    /**
     * Get the quote person who is designated to receive the quotation email.
     * <p>
     * 
     * @return The quote person (user object) who is designated to receive the
     *         quotation email.
     */
    public Object getQuotePerson()
    {
        if (m_quotePerson == null && CATEGORY.isDebugEnabled())
        {
            CATEGORY.debug("getQuotePerson null for m_qpId " + m_qpId + " "
                    + toDebugString());
        }
        return m_quotePerson;
    }

    /**
     * Set the description of this project.
     * <p>
     * 
     * @param p_description
     *            The description of this project.
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the name of this project.
     * <p>
     * 
     * @param p_name
     *            The name of this project.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the project manager to this project.
     * <p>
     * 
     * @param p_projectManager
     *            The project manager assigned to this project.
     */
    public void setProjectManager(User p_projectManager)
    {
        m_projectManager = p_projectManager;

        m_userId = p_projectManager.getUserId();
    }

    /**
     * Set the quote person to this project.
     * <p>
     * 
     * @param p_quotePerson
     *            The quote person designated to this project.
     */
    public void setQuotePerson(Object p_quotePerson)
    {
        if (p_quotePerson instanceof String)
        {
            m_qpId = (String) p_quotePerson;
        }
        else if (p_quotePerson instanceof User)
        {
            m_quotePerson = (User) p_quotePerson;
            m_qpId = m_quotePerson.getUserId();
        }
        else
        {
            m_quotePerson = null;
            m_qpId = "";
        }
    }

    /**
     * @see Project.setUsers(Set)
     */
    public void setUserIds(Set p_userIds)
    {
        m_userIds = p_userIds;
    }

    /**
     * @see Project.addUsers(Set)
     */
    public void addUserIds(Set p_userIds)
    {
        m_userIds.addAll(p_userIds);
    }

    /**
     * @see Project.addUserId(String)
     */
    public void addUserId(String p_userId)
    {
        m_userIds.add(p_userId);
    }

    /**
     * @see Project.removeUsers(Set)
     */
    public void removeUserIds(Set p_userIds)
    {
        m_userIds.removeAll(p_userIds);
    }

    /**
     * @see Project.removeUserId(String)
     */
    public void removeUserId(String p_userId)
    {
        m_userIds.remove(p_userId);
    }

    /**
     * @see getUserIds()
     */
    public Set getUserIds()
    {
        return m_userIds;
    }

    /**
     * Set the default termbase of this project.
     * <p>
     * 
     * @param p_tb
     *            The default termbase name of this project.
     */
    public void setTermbaseName(String p_tb)
    {
        m_termbase = p_tb;
    }

    /**
     * Set the company id of this project.
     * <p>
     * 
     * @param p_tb
     *            The company id of this project.
     */
    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    //
    // other public methods
    //

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return getName();
    }

    /**
     * Return a string representation of the object for debugging purposes.
     * 
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString()
                + " m_name="
                + (m_name == null ? "null" : m_name)
                + " m_description="
                + (m_description == null ? "null" : m_description)
                + " m_userId="
                + (m_userId == null ? "null" : m_userId)
                + " m_projectManager="
                + (m_projectManager == null ? "null" : m_projectManager
                        .toString() + "m_userIds=" + m_userIds.toString())
                + " m_quotePerson="
                + (m_quotePerson == null ? "null" : m_quotePerson.toString()
                        + "m_userIds=" + m_userIds.toString());
    }

    public boolean equals(Object p_object)
    {
        if (this == p_object)
        {
            return true;
        }
        if (p_object instanceof ProjectImpl)
        {
            return this.equals((ProjectImpl) p_object);
        }
        return false;
    }

    public boolean equals(ProjectImpl p_projectImpl)
    {
        if (this == p_projectImpl)
            return true;
        if (getId() == p_projectImpl.getId())
            return true;
        return false;
    }

    //
    // protected methods
    //

    public String getManagerUserId()
    {
        return m_userId;
    }

    public void setManagerUserId(String p_userId)
    {
        m_userId = p_userId;
    }

    public String getQuoteUserId()
    {
        String id = m_qpId;
        if (m_qpId == null && m_quotePerson != null)
        {
            id = m_quotePerson.getUserId();
        }
        return id;
    };

    public void setQuoteUserId(String p_userId)
    {
        m_qpId = p_userId;
    }

    public String getTermbase()
    {
        return m_termbase;
    }

    public void setTermbase(String m_termbase)
    {
        this.m_termbase = m_termbase;
    }

    /**
     * Deactivate this L10nProfile object. I.e. logically delete it.
     */
    public void deactivate()
    {
        this.isActive(false);
    }

    public AttributeSet getAttributeSet()
    {
        return attributeSet;
    }

    public void setAttributeSet(AttributeSet attributeSet)
    {
        this.attributeSet = attributeSet;
    }

    public float getPMCost()
    {
        return m_pmcost;
    }

    public void setPMCost(float mPmcost)
    {
        m_pmcost = mPmcost;
    }

    public int getPoRequired()
    {
        return poRequired;
    }

    public void setPoRequired(int poRequired)
    {
        this.poRequired = poRequired;
    }

    public boolean getAutoAcceptTrans() {
		return autoAcceptTrans;
	}

	public void setAutoAcceptTrans(boolean autoAcceptTrans) {
		this.autoAcceptTrans = autoAcceptTrans;
	}

	public boolean getAutoSendTrans() {
		return autoSendTrans;
	}

	public void setAutoSendTrans(boolean autoSendTrans) {
		this.autoSendTrans = autoSendTrans;
	}

	public boolean getReviewOnlyAutoAccept()
    {
        return reviewOnlyAutoAccept;
    }

    public void setReviewOnlyAutoAccept(boolean reviewAutoAccept)
    {
        this.reviewOnlyAutoAccept = reviewAutoAccept;
    }

    public boolean getReviewOnlyAutoSend()
    {
        return reviewOnlyAutoSend;
    }

    public void setReviewOnlyAutoSend(boolean reviewAutoSend)
    {
        this.reviewOnlyAutoSend = reviewAutoSend;
    }

    public boolean getAutoAcceptPMTask()
    {
        return autoAcceptPMTask;
    }

    public void setAutoAcceptPMTask(boolean autoAcceptPMTask)
    {
        this.autoAcceptPMTask = autoAcceptPMTask;
    }

    public String getProjectManagerName()
    {
        return projectManagerName;
    }

    public void setProjectManagerName(String projectManagerName)
    {
        this.projectManagerName = projectManagerName;
    }

    public boolean isCheckUnTranslatedSegments()
    {
        return checkUnTranslatedSegments;
    }

    public void setCheckUnTranslatedSegments(boolean p_checkUnTranslatedSegments)
    {
        this.checkUnTranslatedSegments = p_checkUnTranslatedSegments;
    }

    public boolean getSaveTranslationsEditReport()
    {
        return saveTranslationsEditReport;
    }

    public void setSaveTranslationsEditReport(boolean saveTranslationsEditReport)
    {
        this.saveTranslationsEditReport = saveTranslationsEditReport;
    }

    public boolean getSaveReviewersCommentsReport()
    {
        return saveReviewersCommentsReport;
    }

    public void setSaveReviewersCommentsReport(
            boolean saveReviewersCommentsReport)
    {
        this.saveReviewersCommentsReport = saveReviewersCommentsReport;
    }

    public boolean getSaveOfflineFiles()
    {
        return saveOfflineFiles;
    }

    public void setSaveOfflineFiles(boolean saveOfflineFiles)
    {
        this.saveOfflineFiles = saveOfflineFiles;
    }

    public boolean getAllowManualQAChecks()
    {
        return allowManualQAChecks;
    }

    public void setAllowManualQAChecks(boolean allowManualQAChecks)
    {
        this.allowManualQAChecks = allowManualQAChecks;
    }

    public boolean getAutoAcceptQATask()
    {
        return autoAcceptQATask;
    }

    public void setAutoAcceptQATask(boolean autoAcceptQATask)
    {
        this.autoAcceptQATask = autoAcceptQATask;
    }

    public boolean getAutoSendQAReport()
    {
        return autoSendQAReport;
    }

    public void setAutoSendQAReport(boolean autoSendQAReport)
    {
        this.autoSendQAReport = autoSendQAReport;
    }

    public boolean isReviewReportIncludeCompactTags()
    {
        return reviewReportIncludeCompactTags;
    }

    public void setReviewReportIncludeCompactTags(
            boolean reviewReportIncludeCompactTags)
    {
        this.reviewReportIncludeCompactTags = reviewReportIncludeCompactTags;
    }

    public boolean getManualRunDitaChecks()
    {
        return manualRunDitaChecks;
    }

    public void setManualRunDitaChecks(boolean p_manualRunDitaChecks)
    {
        this.manualRunDitaChecks = p_manualRunDitaChecks;
    }

    public boolean getAutoAcceptDitaQaTask()
    {
        return autoAcceptDitaQaTask;
    }

    public void setAutoAcceptDitaQaTask(boolean p_autoAcceptDitaQaTask)
    {
        this.autoAcceptDitaQaTask = p_autoAcceptDitaQaTask;
    }

    public boolean getAutoSendDitaQaReport()
    {
        return autoSendDitaQaReport;
    }

    public void setAutoSendDitaQaReport(boolean p_autoSendDitaQaReport)
    {
        this.autoSendDitaQaReport = p_autoSendDitaQaReport;
    }
}
