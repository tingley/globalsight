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
import java.util.Set;

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.foundation.User;

/**
 * A Project represents the work and resources associated with localizing a
 * related set of source data.
 */
public interface Project
{
    public static final int PO_REQUIRED = 1;
    public static final int NO_PO_REQUIRED = 0;

    /**
     * Get the description of this project.
     * <p>
     * 
     * @return Returns the description of this project.
     */
    String getDescription();

    /**
     * Get the project id of this project.
     * <p>
     * 
     * @return Return the project id of this project.
     */
    long getId();

    /**
     * <p>
     * Return the persistent object's id as a Long object.
     * </p>
     * 
     * This is a convenience method that simply wraps the id as an object, so
     * that, for example, the idAsLong can be used as a Hashtable key.
     * 
     * @return the unique identifier as a Long object.
     */
    Long getIdAsLong();

    /**
     * Get the name of this project.
     * <p>
     * 
     * @return Returns the name of this project.
     */
    String getName();

    /**
     * Get the project manager who is assigned to this project.
     * <p>
     * 
     * @return The project manager (user object) who is assigned to this
     *         project.
     */
    User getProjectManager();

    /**
     * Get the quote person who is designated to this project.
     * <p>
     * 
     * @return The quote person (user object) who is designated to this project.
     */
    Object getQuotePerson();

    /**
     * Get the project manager id.
     * <p>
     * 
     * @return The user id of the project manager.
     */
    String getProjectManagerId();

    /**
     * Get the quote person id.
     * <p>
     * 
     * @return The user id of the quote person.
     */
    String getQuotePersonId();

    /**
     * Get the default termbase of this project.
     * <p>
     * 
     * @return The name of the default termbase of this project.
     */
    String getTermbaseName();

    /**
     * Get the company id of this project.
     * <p>
     * 
     * @return The name of the company id of this project.
     */
    long getCompanyId();

    /**
     * Set the description of this project.
     * <p>
     * 
     * @param p_description
     *            The description of this project.
     */
    void setDescription(String p_description);

    /**
     * Set the name of this project.
     * <p>
     * 
     * @param p_name
     *            The name of this project.
     */
    void setName(String p_name);

    /**
     * Set the project manager to this project.
     * <p>
     * 
     * @param p_projectManager
     *            The project manager assigned to this project.
     */
    void setProjectManager(User p_projectManager);

    /**
     * Set the quote person to this project.
     * <p>
     * 
     * @param p_quotePerson
     *            The quote person designated to this project.
     */
    void setQuotePerson(Object p_quotePerson);

    /**
     * Adds this set of user ids to the existing list of users that are
     * associated with the project. It will disregard any duplicates.
     * 
     * @param p_userIds
     *            A set of the user ids to add to the user list.
     * 
     */
    void addUserIds(Set p_userIds);

    /**
     * Adds this one user id to the existing list of users that are associated
     * with the project.
     * 
     * @param p_userId
     *            The user id to add to the project.
     */
    void addUserId(String p_userId);

    /**
     * Removes the user ids in the specified set from the users that are
     * associated with this project.
     * 
     * @param p_userIds
     *            A set of user ids of users to remove the list.
     * 
     */
    void removeUserIds(Set p_userIds);

    /**
     * Removes this one user id from the existing list of users that are
     * associated with the project.
     * 
     * @param p_userId
     *            The user id to remove from the project.
     */
    void removeUserId(String p_userId);

    /**
     * Returns the set of user ids that are associated with the project.
     * 
     * @return The set of user ids with no duplicates.
     */
    Set getUserIds();

    /**
     * Sets the list of user ids. This is used to get/store in database since
     * the Users themselves are stored in LDAP - but the relationship is stored
     * in database.
     * 
     * @param p_userIds
     *            The set of userids to set for this project to be associated
     *            with (no duplicates).
     */
    void setUserIds(Set p_userIds);

    /**
     * Set the default termbase of this project.
     * <p>
     * 
     * @param p_tb
     *            The default termbase name of this project.
     */
    void setTermbaseName(String p_tb);

    /**
     * Set the company id of this project.
     * <p>
     * 
     * @param p_tb
     *            The default company id of this project.
     */
    void setCompanyId(long p_companyId);

    /**
     * Deactivate this L10nProfile object. I.e. logically delete it.
     */
    void deactivate();

    public AttributeSet getAttributeSet();

    public void setAttributeSet(AttributeSet attributeSet);

    public float getPMCost();

    public void setPMCost(float m_pmcost);

    public int getPoRequired();

    public void setPoRequired(int poRequired);

    // Auto accept task and auto send report option in Project Level.
    public boolean getAutoAcceptTrans();

    public void setAutoAcceptTrans(boolean autoAcceptTrans);

    public boolean getAutoSendTrans();

    public void setAutoSendTrans(boolean autoSendTrans);
    
    public boolean getReviewOnlyAutoAccept();

    public void setReviewOnlyAutoAccept(boolean reviewAutoAccept);

    public boolean getReviewOnlyAutoSend();

    public void setReviewOnlyAutoSend(boolean reviewAutoSend);

    public boolean getAutoAcceptPMTask();

    public void setAutoAcceptPMTask(boolean autoAcceptPMTask);

    public boolean getSaveTranslationsEditReport();

    public void setSaveTranslationsEditReport(boolean saveTranslationsEditReport);

    public boolean getSaveReviewersCommentsReport();

    public void setSaveReviewersCommentsReport(
            boolean saveReviewersCommentsReport);

    public boolean getSaveOfflineFiles();

    public void setSaveOfflineFiles(boolean saveOfflineFiles);

    public boolean getAllowManualQAChecks();

    public void setAllowManualQAChecks(boolean allowManualQAChecks);

    public boolean getAutoAcceptQATask();

    public void setAutoAcceptQATask(boolean autoAcceptQATask);

    public boolean getAutoSendQAReport();

    public void setAutoSendQAReport(boolean autoSendQAReport);

    public boolean getManualRunDitaChecks();

    public void setManualRunDitaChecks(boolean manualRunDitaChecks);

    public boolean getAutoAcceptDitaQaTask();

    public void setAutoAcceptDitaQaTask(boolean autoAcceptQaTask);

    public boolean getAutoSendDitaQaReport();

    public void setAutoSendDitaQaReport(boolean autoSendQaReport);
}
