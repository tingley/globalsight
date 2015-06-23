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

package com.globalsight.everest.foundation;

//java core
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.util.GlobalSightLocale;

/**
 * This type defines the interface to the Localization Profile data type used in
 * Envoy to specify workflow and project attributes.
 */
public interface L10nProfile
{
    // use for TM choice
    public static final int NO_TM = 0;
    public static final int REGULAR_TM = 1;
    public static final int REGULAR_TM_WITH_PAGE_TM = 2;

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
     * Get the identification of the main translation memory for this
     * localization profile. This is the TM associated with the project.
     * 
     * @return The identification of the main translation memory for this
     *         localization profile.
     */
    long getMainTmId();

    /**
     * @param p_description
     *            for this localization profile
     */
    void setDescription(String p_description);

    /**
     * @return String the description for this localization profile
     */
    String getDescription();

    /**
     * Set dispatch criteria for this localization profile.
     * 
     * @param p_dispatch_criteria
     *            The Criteria for dispatching this profile.
     */
    void setDispatchCriteria(DispatchCriteria p_dispatch_criteria);

    /**
     * Set the project id for this localization profile.
     * 
     * @param p_projectId
     *            The project id that this profile is associated with.
     */
    void setProjectId(long p_projectId);

    /**
     * Set the TM Choice for this localization profile
     * 
     * @param p_tmChoice
     *            0, 1 or 2 are the permitted values
     */
    void setTmChoice(int p_tmChoice);

    /**
     * The value for TM choice will be returned-no TM, regular TM or regular TM
     * + page TM
     * 
     * @return int 0,1,2 will be returned - see statics at top of interface
     */
    int getTmChoice();

    /**
     * @return the code set that the particular target locale is associated
     *         with.
     */
    String getCodeSet(GlobalSightLocale p_targetLocale);

    /**
     * @return the leveraging locales for all the locales
     */
    LeveragingLocales getLeveragingLocales();

    /**
     * Set a new name for this localization profile.
     * 
     * @param p_name
     *            The new name for this localization profile.
     */
    void setName(String p_name);

    /**
     * Set the source locale of this localization profile.
     * 
     * @param p_locale
     *            The source locale.
     */
    void setSourceLocale(GlobalSightLocale p_locale);

    /**
     * Set the priority of this L10nProfile.
     * 
     * @param p_priority
     *            A number specifying the priority of jobs that are associated
     *            with this L10nProfile.
     */
    void setPriority(int p_priority);

    /**
     * Get the priority of this L10nProfile.
     * 
     * @return The priority
     */
    int getPriority();

    /**
     * Add a target locale and its workflow information to this localization
     * profile.
     * 
     * @param p_locale
     *            The target locale.
     * @param p_workflowInfo
     *            The workflow information which contains the template ID and
     *            user assignment of each task.
     */
    void addWorkflowTemplateInfo(WorkflowTemplateInfo p_workflowTemplateInfo);

    /**
     * Remove a target locale and its workflow information from this
     * localization profile.
     * 
     * @param p_locale
     *            The target locale.
     * @return WorkflowInfo The workflow object that was removed.
     */
    WorkflowTemplateInfo removeWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale);

    /**
     * Put workflow information into this localization profile, removing any
     * workflow information having the same target locale.
     * 
     * @param p_workflowInfo
     *            The workflow information which contains the template ID and
     *            user assignment of each task.
     */
    void putWorkflowTemplateInfo(WorkflowTemplateInfo p_workflowTemplateInfo);

    /**
     * Set whether the workflows driven by this localization profile are to be
     * dispatched automatically.
     * 
     * @param p_automatic
     *            True if the workflows are to be dispatched automatically;
     *            false otherwise.
     */
    void setAutomaticDispatch(boolean p_automatic);

    /**
     * Get the source locale of this localization profile.
     * 
     * @return The source locale.
     */
    GlobalSightLocale getSourceLocale();

    /**
     * Get the list of target locales in this localization profile.
     * 
     * @return A list of target locales in this localization profile. Each
     *         element in the array is a GlobalSightLocale
     */
    GlobalSightLocale[] getTargetLocales();

    /**
     * Get the list of leveraging locales in this localization profile.
     * Leveraging locales are target locales and groups of extra locales users
     * selected from which segments are leveraged.
     * 
     * @return LeveragingLocales object
     */
    // LeveragingLocales getLeveragingLocales();

    /**
     * Get the workflow template id of the specified target locale.
     * 
     * @param p_targetLocale
     *            The target locale.
     * @return WorkflowInfo The workflow information of the specified target
     *         locale.
     */
    WorkflowTemplateInfo getWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale);

    /**
     * Get the Dtp workflow template id of the specified target locale.
     * 
     * @param p_targetLocale
     *            The target locale.
     * @return Dtp WorkflowInfo The workflow information of the specified target
     *         locale.
     */
    WorkflowTemplateInfo getDtpWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale);

    /**
     * Check whether the workflows created from templates in this localization
     * profile are to be dispatched automatically.
     * 
     * @return True if workflows are to be dispatched automatically; false
     *         otherwise.
     */
    boolean dispatchIsAutomatic();

    /**
     * Get the internal identification of this localization profile
     * 
     * @return The internal identification of this localization profile.
     */
    long getId();

    /**
     * Get dispatch criteria for this localization profile.
     * 
     * @return the Criteria for dispatching this profile.
     */
    DispatchCriteria getDispatchCriteria();

    /**
     * Get the project id of this localization profile.
     */
    long getProjectId();

    /**
     * Get the project that this localization profile is associated with.
     */
    Project getProject();

    /**
     * Get the name of this localization profile.
     */
    String getName();

    /**
     * Reset the L10nProfile object by setting the attributes to null.
     */
    void reset();

    /**
     * Returns true if a script should be run at job creation.
     */
    boolean runScriptAtJobCreation();

    /**
     * Set to turn off/on running a script at job creation.
     * 
     * @param p_runScript
     *            True - to run script False - to not run a script
     */
    void setRunScriptAtJobCreation(boolean p_runScript);

    /**
     * Return the name of the job creation script - including the path.
     * 
     * @return The script name or NULL if running the script isn't set.
     */
    String getNameOfJobCreationScript();

    /**
     * Set the script name to run at job creation.
     * 
     * @param p_scriptName
     *            The name of the script including the path.
     */
    void setJobCreationScriptName(String p_scriptName);

    /**
     * Returns if exact match editing can be used on jobs associated with this
     * profile.
     * 
     * @return 'true' if exact match editing can be used, 'false' if not.
     */
//    boolean isExactMatchEditing();

    /**
     * Set if exact match editing should or should not be used.
     * 
     * @param m_exactMatchEdit
     *            'true' if it should be used. 'false' if it shouldn't be used.
     */
//    void setExactMatchEditing(boolean p_exactMatchEdit);

    Collection getWorkflowTemplateInfos();

    void addTMProfile(TranslationMemoryProfile p_tmProfile);

    TranslationMemoryProfile getTranslationMemoryProfile();

    public List<Long> getUnActivelocaleIds();

    public List<GlobalSightLocale> getUnActiveLocales();

    void removeWfInfo(GlobalSightLocale p_targetLocale);

    void clearWorkflowTemplateInfo(GlobalSightLocale targetLocale);

    public Set getFileProfiles();

    public void setFileProfiles(Set fileprofiles);
    
    public int getTMEditType();
    public void setTMEditType(int TMEditType);
    
    public long getWfStatePostId();
    public void setWfStatePostId(long p_wfStatePostId);
}
