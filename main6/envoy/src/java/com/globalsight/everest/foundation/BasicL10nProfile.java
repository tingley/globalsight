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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.globalsight.cxe.entity.customAttribute.AttributeSet;
import com.globalsight.everest.persistence.PersistenceRuntimeException;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.Project;
import com.globalsight.everest.projecthandler.TranslationMemoryProfile;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.everest.servlet.util.ServerProxy;
import com.globalsight.ling.tm.LeveragingLocales;
import com.globalsight.util.GlobalSightLocale;

/**
 * This is a basic implementation of the Localization Profile data type that is
 * intended to be used by clients of Envoy as a whole, and clients of the
 * JobHandler component.
 */

public class BasicL10nProfile extends PersistentObject implements L10nProfile,
        Cloneable
{
    private static Logger c_logger = Logger.getLogger(BasicL10nProfile.class);
    private static final long serialVersionUID = 7634032326525793448L;

    // Constants
    private static final String DUMMY_NAME = "dummy profile";

    public static final String AUTO_DISPATCH = "m_autoDispatch";
    public static final String PROJECT = "m_project";
    public static final String SOURCE_LOCALE = "m_sourceLocale";
    public static final String WORKFLOW_TEMPLATES = "m_workflowTemplateInfoMap";
    public static final String M_NAME = "m_name";
    public static final String M_DESCRIPTION = "m_description";

    // Attributes
    protected long m_projectId = -1;
    protected Project m_project = null;
    protected String m_name = null;
    protected String m_description = null;
    public long m_wfStatePostId = -1;
    protected long m_companyId = -1;
    protected int m_priority = -1;
    protected GlobalSightLocale m_sourceLocale = null;
    protected boolean m_autoDispatch = true;

    // used to save translation workflow template information
    protected Hashtable<GlobalSightLocale, WorkflowTemplateInfo> workflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>(
            0);
    // used to save dtp workflow template information
    protected Hashtable<GlobalSightLocale, WorkflowTemplateInfo> dtpWorkflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>(
            0);

    // used for mapping between L10N Profile and WorkflowTemplateInfo
    private Set<WorkflowTemplateInfo> workflowTemplatesSet = new HashSet<WorkflowTemplateInfo>();
    protected DispatchCriteria m_dispatchCriteria = null;
    protected int tmChoice = -1;
    protected boolean m_runScript = false;
    protected String m_jobCreationScriptName = null;
    protected boolean m_isExactMatchEditing = false;
    private Set<TranslationMemoryProfile> tmProfilesSet = new HashSet<TranslationMemoryProfile>();

    private AttributeSet attributeSet = null;

    public boolean useActive = true;

    private Set m_fileProfiles = new HashSet();
    private String tuTypes = new String();
    
    //default is to allow edit of ICE and 100% lock segments
    private int TMEditType = 1; 

    /**
     * Default constructor.
     */
    public BasicL10nProfile()
    {
        this(DUMMY_NAME);
    }

    /**
     * Construct an instance of the BasicL10nProfile.
     * 
     * @param p_name
     *            The name of the localization profile.
     */
    public BasicL10nProfile(String p_name)
    {
        m_name = p_name;
        workflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>(
                5);
        dtpWorkflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>(
                5);
        workflowTemplatesSet = new HashSet<WorkflowTemplateInfo>();
        tmProfilesSet = new HashSet<TranslationMemoryProfile>();
        m_dispatchCriteria = new DispatchCriteria();
    }

    /**
     * Reset the L10nProfile object by setting the attributes to null.
     * 
     * CvdL: if this is a public method, shouldn't it reset all fields? I.e.,
     * why set m_projectId=-1 and leave m_project what it is?
     */
    public void reset()
    {
        m_projectId = -1;
        m_name = null;
        m_description = null;
        m_sourceLocale = null;
        m_autoDispatch = true;
        workflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>();
        dtpWorkflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>();
        workflowTemplatesSet = new HashSet<WorkflowTemplateInfo>();
        tmProfilesSet = new HashSet<TranslationMemoryProfile>();
        m_dispatchCriteria = new DispatchCriteria();
        tmChoice = -1;
    }

    /**
     * Set the priority of this L10nProfile.
     * 
     * @param p_priority
     *            A number specifying the priority of jobs that are associated
     *            with this L10nProfile.
     */
    public void setPriority(int p_priority)
    {
        m_priority = p_priority;
    }

    /**
     * Get the priority of this L10nProfile.
     * 
     * @return The priority
     */
    public int getPriority()
    {
        return m_priority;
    }

    /**
     * Add a target locale and its workflow information to this localization
     * profile.
     * 
     * @param p_locale
     *            The target locale.
     * @param p_workflowTemplateInfo
     *            The workflow information which contains the template ID and
     *            user assignment of each task.
     */
    public void addWorkflowTemplateInfo(
            WorkflowTemplateInfo p_workflowTemplateInfo)
    {
        if (!workflowTemplatesSet.contains(p_workflowTemplateInfo))
        {
            workflowTemplatesSet.add(p_workflowTemplateInfo);
        }

        updateWorkflowTemplateInfoMap();
        // Set the back pointer from WorkflowTemplateInfo to L10nProfile.
        p_workflowTemplateInfo.setL10nProfile(this);
    }

    /**
     * Remove workflow template info from workflowTemplatesSet by specified
     * target locale.
     */
    public void clearWorkflowTemplateInfo(GlobalSightLocale targetLocale)
    {
        for (Iterator it = workflowTemplatesSet.iterator(); it.hasNext();)
        {
            WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) it.next();
            if (targetLocale.getId() == wfti.getTargetLocale().getId())
            {
                it.remove();
            }
        }
        updateWorkflowTemplateInfoMap();
    }

    // setter
    public void setWorkflowTemplates(
            Set<WorkflowTemplateInfo> p_workflowTemplatesSet)
    {
        workflowTemplatesSet = p_workflowTemplatesSet;
        updateWorkflowTemplateInfoMap();
    }

    // getter
    public Set<WorkflowTemplateInfo> getWorkflowTemplates()
    {
        return this.workflowTemplatesSet;
    }

    private void updateWorkflowTemplateInfoMap()
    {
        workflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>();
        dtpWorkflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>();

        for (Iterator ite = workflowTemplatesSet.iterator(); ite.hasNext();)
        {
            WorkflowTemplateInfo wt = (WorkflowTemplateInfo) ite.next();

            if (WorkflowTemplateInfo.TYPE_TRANSLATION.equals(wt
                    .getWorkflowType()))
            {
                workflowTemplateInfoMap.put(wt.getTargetLocale(), wt);
            }
            else
            {
                dtpWorkflowTemplateInfoMap.put(wt.getTargetLocale(), wt);
            }
        }
    }

    public void addTMProfile(TranslationMemoryProfile p_tmProfile)
    {
        if (tmProfilesSet.size() > 0)
        {
            tmProfilesSet.clear();
        }

        if (p_tmProfile != null)
        {
            tmProfilesSet.add(p_tmProfile);
            tuTypes = p_tmProfile.getTuTypes();
            p_tmProfile.setL10nProfile(this);
        }
    }

    /**
     * Set the TM Choice for this localization profile
     * 
     * @param p_tmChoice
     *            0, 1 or 2 are the permitted values
     */
    public void setTmChoice(int p_tmChoice)
    {
        tmChoice = p_tmChoice;
    }

    /**
     * Get the TM Choice for this localization profile
     * 
     * @return int The TM Choice for this localization profile
     */
    public int getTmChoice()
    {
        return tmChoice;
    }

    /**
     * @return String returns the code set for this localization profile
     */
    public String getCodeSet(GlobalSightLocale p_targetLocale)
    {
        WorkflowTemplateInfo wfTemplateInfo = (WorkflowTemplateInfo) getWorkflowTemplateInfo(p_targetLocale);
        return wfTemplateInfo.getCodeSet();
    }

    /**
     * Deactivate this L10nProfile object. I.e. logically delete it.
     */
    public void deactivate()
    {
        this.isActive(false);
    }

    public WorkflowTemplateInfo removeWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale)
    {
        return (WorkflowTemplateInfo) workflowTemplateInfoMap
                .remove(p_targetLocale);
    }

    public void removeWfInfo(GlobalSightLocale p_targetLocale)
    {
        Iterator it = workflowTemplateInfoMap.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Entry) it.next();
            if (entry.getKey().equals(p_targetLocale))
            {
                it.remove();
            }
        }
    }

    public WorkflowTemplateInfo removeDtpWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale)
    {
        return (WorkflowTemplateInfo) dtpWorkflowTemplateInfoMap
                .remove(p_targetLocale);
    }

    /**
     * Put workflow information into this localization profile, removing any
     * workflow information having the same target locale.
     * 
     * @param p_workflowTemplateInfo
     *            The workflow information which contains the template ID and
     *            user assignment of each task.
     */
    public void putWorkflowTemplateInfo(
            WorkflowTemplateInfo p_workflowTemplateInfo)
    {
        // remove the old workflow information associated with the
        // target locale
        if (WorkflowTemplateInfo.TYPE_TRANSLATION.equals(p_workflowTemplateInfo
                .getWorkflowType()))
        {
            removeWorkflowTemplateInfo(p_workflowTemplateInfo.getTargetLocale());
        }
        else
        {
            removeDtpWorkflowTemplateInfo(p_workflowTemplateInfo
                    .getTargetLocale());
        }

        // add the new workflow information
        addWorkflowTemplateInfo(p_workflowTemplateInfo);
    }

    /**
     * Check whether the workflows created from templates in this localization
     * profile are to be dispatched automatically.
     * 
     * @return True if workflows are to be dispatched automatically; false
     *         otherwise.
     */
    public boolean dispatchIsAutomatic()
    {
        return m_autoDispatch;
    }

    /**
     * Compare against the specified object to see whether it is equal to this
     * instance. This method overrides the method in the java.lang.Object base
     * class.
     */
    public boolean equals(Object p_targetObject)
    {
        boolean isEqual = false;

        if (p_targetObject instanceof L10nProfile)
        {
            L10nProfile profile = (L10nProfile) p_targetObject;
            isEqual = (getId() == profile.getId());
        }

        return isEqual;
    }

    /**
     * Get the description of the Localization Profile
     * 
     * @return String The description of the Localization Profile
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Get the company id of the Localization Profile
     * 
     * @return String The company id of the Localization Profile
     */
    public long getCompanyId()
    {
        return m_companyId;
    }

    /**
     * Get the name of this localization profile.
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Get the project id of this localization profile.
     */
    public long getProjectId()
    {
        if (m_project != null)
        {
            return m_project.getId();
        }
        else
        {
            return m_projectId;
        }
    }

    /**
     * Get the project this localization profile is associated with.
     */
    public Project getProject()
    {
        return m_project;
    }

    /**
     * Get the dispatch criteria of this localization profile.
     */
    public DispatchCriteria getDispatchCriteria()
    {
        return m_dispatchCriteria;
    }

    /**
     * Get the source locale of this localization profile.
     * 
     * @return The source locale.
     */
    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    /**
     * Get the list of target locales in this localization profile.
     * 
     * @return A list of target locales in this profile. Each element in the
     *         array is a GlobalSightLocale.
     */
    public GlobalSightLocale[] getTargetLocales()
    {
        HashSet<GlobalSightLocale> targetLocalSet = new HashSet<GlobalSightLocale>();

        for (Iterator it = workflowTemplatesSet.iterator(); it.hasNext();)
        {
            WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) it.next();
            boolean isActive = true;
            try
            {
                isActive = ServerProxy
                        .getProjectHandler()
                        .getL10nProfileWfTemplateInfo(this.getId(),
                                wfti.getId()).getIsActive();
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }
            if (isActive)
            {
                targetLocalSet.add(wfti.getTargetLocale());
            }
        }

        GlobalSightLocale[] trgLocales = new GlobalSightLocale[targetLocalSet
                .size()];
        int count = 0;
        for (Iterator it2 = targetLocalSet.iterator(); it2.hasNext();)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) it2.next();
            trgLocales[count] = gsl;
            count++;
        }

        return trgLocales;
    }

    public List<GlobalSightLocale> getUnActiveLocales()
    {
        List<GlobalSightLocale> unActiveLocales = new ArrayList<GlobalSightLocale>();

        for (Iterator it = workflowTemplateInfoMap.entrySet().iterator(); it
                .hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            GlobalSightLocale targetLocale = (GlobalSightLocale) entry.getKey();
            WorkflowTemplateInfo wkInfo = (WorkflowTemplateInfo) entry
                    .getValue();
            boolean isActive = true;
            try
            {
                isActive = ServerProxy
                        .getProjectHandler()
                        .getL10nProfileWfTemplateInfo(this.getId(),
                                wkInfo.getId()).getIsActive();
            }
            catch (Exception e)
            {
                c_logger.error(e.getMessage(), e);
            }

            if (!isActive)
            {
                unActiveLocales.add(targetLocale);
            }
        }

        return unActiveLocales;
    }

    public List<Long> getUnActivelocaleIds()
    {
        List<Long> unActiveLocaleIds = new ArrayList<Long>();

        for (Iterator it = getUnActiveLocales().iterator(); it.hasNext();)
        {
            GlobalSightLocale gsl = (GlobalSightLocale) it.next();
            unActiveLocaleIds.add(gsl.getIdAsLong());
        }

        return unActiveLocaleIds;
    }

    /**
     * Get the list of leveraging locales in this localization profile.
     * Leveraging locales are target locales and groups of extra locales users
     * selected from which segments are leveraged.
     * 
     * @return LeveragingLocales object
     */
    public LeveragingLocales getLeveragingLocales()
    {
        LeveragingLocales leveragingLocales = new LeveragingLocales();

        for (Iterator it = workflowTemplateInfoMap.entrySet().iterator(); it
                .hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            GlobalSightLocale targetLocale = (GlobalSightLocale) entry.getKey();
            WorkflowTemplateInfo wkInfo = (WorkflowTemplateInfo) entry
                    .getValue();
            Set<GlobalSightLocale> levLocales = wkInfo.getLeveragingLocales();
            if (levLocales == null || levLocales.size() == 0)
            {
                leveragingLocales.setLeveragingLocale(targetLocale, null);
            }
            else
            {
                leveragingLocales.setLeveragingLocale(targetLocale, levLocales);
            }
        }

        return leveragingLocales;
    }

    /**
     * Get the workflow template information of the specified target locale.
     * 
     * @param p_targetLocale
     *            The target locale.
     * @return WorkflowTemplateInfo The workflow infomation of the specified
     *         target locale
     */
    public WorkflowTemplateInfo getWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale)
    {
        WorkflowTemplateInfo transWfti = null;

        for (Iterator it = workflowTemplatesSet.iterator(); it.hasNext();)
        {
            WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) it.next();
            String wfType = wfti.getWorkflowType();
            if (WorkflowTemplateInfo.TYPE_TRANSLATION.equals(wfType)
                    && wfti.getTargetLocale().equals(p_targetLocale))
            {
                transWfti = wfti;
                break;
            }
        }
        // Necessary??
        if (transWfti == null)
        {
            transWfti = (WorkflowTemplateInfo) workflowTemplatesSet.iterator()
                    .next();
        }

        return transWfti;
    }

    /**
     * Get the dtpWorkflow template information of the specified target locale.
     * 
     * @param p_targetLocale
     *            The target locale.
     * @return WorkflowTemplateInfo The dtp workflow infomation of the specified
     *         target locale
     */
    public WorkflowTemplateInfo getDtpWorkflowTemplateInfo(
            GlobalSightLocale p_targetLocale)
    {
        for (Iterator it = workflowTemplatesSet.iterator(); it.hasNext();)
        {
            WorkflowTemplateInfo wfti = (WorkflowTemplateInfo) it.next();
            if (WorkflowTemplateInfo.TYPE_DTP.equals(wfti.getWorkflowType())
                    && wfti.getTargetLocale().equals(p_targetLocale))
            {
                return wfti;
            }
        }

        return null;
    }

    /**
     * Set whether the workflows driven by this localization profile are to be
     * dispatched automatically.
     * 
     * @param p_automatic
     *            True if the workflows are to be dispatched automatically;
     *            false otherwise.
     */
    public void setAutomaticDispatch(boolean p_automatic)
    {
        m_autoDispatch = p_automatic;
    }

    /**
     * Set the description of the Localization Profile
     * 
     * @param String
     *            The description
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the company id of the Localization Profile
     * 
     * @param String
     *            The company id
     */
    public void setCompanyId(long p_companyId)
    {
        m_companyId = p_companyId;
    }

    /**
     * Set a new name for this localization profile.
     * 
     * @param p_name
     *            The new name for this localization profile.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /**
     * Set the project id for this localization profile.
     * 
     * @param p_projectId
     *            The project id that this profile is associated with.
     */
    public void setProjectId(long p_projectId)
    {
        m_projectId = p_projectId;

        // if the project is set and does not have the same id as the
        // id passed in, clear the project field
        if (m_project != null && m_project.getId() != p_projectId)
        {
            m_project = null;
        }
    }

    /**
     * Set the project for this localization profile.
     * 
     * @param p_project
     *            The project that this profile is associated with.
     */
    public void setProject(Project p_project)
    {
        m_project = p_project;
        m_projectId = p_project.getId();
    }

    /**
     * Set the dispatch criteria of this localization profile.
     */
    public void setDispatchCriteria(DispatchCriteria p_dispatchCriteria)
    {
        m_dispatchCriteria = p_dispatchCriteria;
    }

    /**
     * Set the source locale of this localization profile.
     * 
     * @param p_locale
     *            The source locale.
     */
    public void setSourceLocale(GlobalSightLocale p_locale)
    {
        m_sourceLocale = p_locale;
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return m_name;
    }

    /**
     * Returns a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        workflowTemplateInfoMap.size();

        return super.toString()
                + " m_projectId="
                + Long.toString(m_projectId)
                + " m_name="
                + (m_name == null ? "null" : m_name)
                + " m_description="
                + (m_description == null ? "null" : m_description)
                + " m_sourceLocale="
                + (m_sourceLocale == null ? "null" : m_sourceLocale
                        .toDebugString())
                + " m_autoDispatch="
                + new Boolean(m_autoDispatch).toString()
                + "\nm_workflowTemplateInfoMap="
                + (workflowTemplateInfoMap == null ? "null"
                        : workflowTemplateInfoMap.toString())
                + "\nm_dtpWorkflowTemplateInfoMap="
                + (dtpWorkflowTemplateInfoMap == null ? "null"
                        : dtpWorkflowTemplateInfoMap.toString())
                + "\nm_dispatchCriteria="
                + (m_dispatchCriteria == null ? "null" : m_dispatchCriteria
                        .toString())
                + " m_tmChoice="
                + Integer.toString(tmChoice)
                + " m_runScript="
                + new Boolean(m_runScript).toString()
                + " m_jobCreationScriptName="
                + (m_jobCreationScriptName == null ? "null"
                        : m_jobCreationScriptName) + " m_TMEditType="
                + Integer.toString(TMEditType) + "\n";
    }

    /**
     * This method is overwritten for TOPLink. TOPLink doesn't query all
     * collections of objects within an object. So if a Profile is serialized -
     * the TM information and WorkflowTemplateInfo information may not be
     * available (because they haven't been queried yet). Overwriting the method
     * forces the query to happen so when it is serialized all pieces of the
     * object are serialized and availble to the client.
     */
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        // touch workflow infos - since they are set up to only
        // populate when needed
        workflowTemplatesSet.size();

        // call the default writeObject
        out.defaultWriteObject();
    }

    /**
     * Make an exact clone of this object
     * 
     * @return Object Return a cloned object.
     * @exception CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException
    {
        BasicL10nProfile newObject = (BasicL10nProfile) super.clone();

        // Perform deep copy
        GlobalSightLocale[] targetLocales = getTargetLocales();
        for (int i = 0; i < targetLocales.length; i++)
        {
            WorkflowTemplateInfo wt1 = getWorkflowTemplateInfo(targetLocales[i]);
            WorkflowTemplateInfo wt2 = getDtpWorkflowTemplateInfo(targetLocales[i]);
            if (wt1 != null)
            {
                newObject.addWorkflowTemplateInfo(wt1);
            }
            if (wt2 != null)
            {
                newObject.addWorkflowTemplateInfo(wt2);
            }

        }

        return newObject;
    }

    /**
     * Makes a clone of this object, but makes it new so that it can we
     * persisted using TopLink (by setting m_id to -1). Assumes that this object
     * "isEditable"
     * 
     * @return BasicL10nProfile a cloned object, so that it can be inserted as a
     *         new profile using TopLink.
     * @exception CloneNotSupportedException
     */
    public BasicL10nProfile cloneForInsert() throws CloneNotSupportedException
    {
        BasicL10nProfile newObject = (BasicL10nProfile) super.clone();

        // Set the profile seq to -1 so TopLink can insert this cloned
        // object and set a new profile sequence number
        try
        {
            // clear out the ids
            newObject.makeNew();

            /*
             * GlobalSightLocale[] targetLocales = getTargetLocales(); for (int
             * i = 0; i < targetLocales.length; i++) { WorkflowTemplateInfo wf =
             * getWorkflowTemplateInfo( targetLocales[i]).cloneForInsert();
             * newObject.addWorkflowTemplateInfo(wf); }
             */
        }
        catch (PersistenceRuntimeException pe)
        {
            throw new CloneNotSupportedException();
        }

        return newObject;
    }

    /**
     * Get the identification of the main translation memory for this
     * localization profile.
     * 
     * @return long The identification of the main translation memory for this
     *         localization profile. If the project hasn't been set yet then
     *         return "-1".
     */
    public long getMainTmId()
    {
        /*
         * if (m_project != null) { return
         * m_project.getTranslationMemory().getId(); } else { return -1; }
         */
        return -1;
    }

    /**
     * Returns true if a script should be run at job creation.
     */
    public boolean runScriptAtJobCreation()
    {
        return m_runScript;
    }

    /**
     * Set to turn off/on running a script at job creation.
     * 
     * @param p_runScript
     *            True - to run script, False - to not run a script
     */
    public void setRunScriptAtJobCreation(boolean p_runScript)
    {
        m_runScript = p_runScript;
    }

    public void setRunScript(boolean p_runScript)
    {
        m_runScript = p_runScript;
    }

    public boolean getRunScript()
    {
        return m_runScript;
    }

    /**
     * Return the name of the job creation script - including the path.
     * 
     * @return The script name or NULL if one isn't set.
     */
    public String getNameOfJobCreationScript()
    {
        if (m_runScript)
        {
            return m_jobCreationScriptName;
        }
        else
        {
            return null;
        }
    }

    /**
     * Set the script name to run at job creation and turns on running the
     * script.
     * 
     * @param p_scriptName
     *            The name of the script including the path.
     */
    public void setJobCreationScriptName(String p_scriptName)
    {
        m_runScript = true;

        if (p_scriptName == null || p_scriptName.equals(""))
        {
            m_runScript = false;
        }

        m_jobCreationScriptName = p_scriptName;
    }

    public String getJobCreationScriptName()
    {
        return m_jobCreationScriptName;
    }

    /**
     * @see L10nProfile.isExactMatchEditing
     */
    public boolean isExactMatchEditing()
    {
        return m_isExactMatchEditing;
    }

    public boolean getIsExactMatchEditing()
    {
        return m_isExactMatchEditing;
    }

    /**
     * @see L10nProfile.setExactMatchEditing
     */
    public void setExactMatchEditing(boolean p_exactMatchEdit)
    {
        m_isExactMatchEditing = p_exactMatchEdit;
    }

    public void setIsExactMatchEditing(boolean p_exactMatchEdit)
    {
        m_isExactMatchEditing = p_exactMatchEdit;
    }

    /**
     * @returns Collection of translation WorkflowTemplateInfo objects.
     */
    public Collection getWorkflowTemplateInfos()
    {
        return workflowTemplateInfoMap.values();
    }

    /**
     * @returns Collection of dtp WorkflowTemplateInfo objects.
     */
    public Collection<WorkflowTemplateInfo> getDtpWorkflowTemplateInfos()
    {
        return dtpWorkflowTemplateInfoMap.values();
    }

    /**
     * @return Translation Memory Profile
     */
    public Set<TranslationMemoryProfile> getTmProfiles()
    {
        return tmProfilesSet;
    }

    /**
     * m_tmProfileList always has only one TranslationMemoryProfile in it.
     * 
     * @return Translation Memory Profile
     */
    public TranslationMemoryProfile getTranslationMemoryProfile()
    {
        Iterator ite = tmProfilesSet.iterator();
        if (ite.hasNext())
        {
            return (TranslationMemoryProfile) ite.next();
        }

        return null;
    }

    /**
     * Set the WorkflowTemplateInfos.
     * 
     * @param Vector
     *            of WorkflowTemplateInfo objects.
     */
    public void setWorkflowTemplateInfos(Vector p_workflowTemplateInfos)
    {
        // throw away any old WorkflowTemplateInfos
        workflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>(
                5);

        for (Iterator it = p_workflowTemplateInfos.iterator(); it.hasNext();)
        {
            addWorkflowTemplateInfo((WorkflowTemplateInfo) it.next());
        }
    }

    /**
     * Set the DtpWorkflowTemplateInfos.
     * 
     * @param Vector
     *            of DtpWorkflowTemplateInfo objects.
     */
    public void setDtpWorkflowTemplateInfos(Vector p_dtpWorkflowTemplateInfos)
    {
        // throw away any old DTP WorkflowTemplateInfos
        dtpWorkflowTemplateInfoMap = new Hashtable<GlobalSightLocale, WorkflowTemplateInfo>(
                5);

        Iterator it = p_dtpWorkflowTemplateInfos.iterator();
        while (it.hasNext())
        {
            addWorkflowTemplateInfo((WorkflowTemplateInfo) it.next());
        }
    }

    public void setTmProfiles(Set<TranslationMemoryProfile> p_tmProfiles)
    {
        tmProfilesSet = p_tmProfiles;

        TranslationMemoryProfile tmp = getTranslationMemoryProfile();
        if (tmp != null)
        {
            tuTypes = tmp.getTuTypes();
        }
    }

    public boolean isAutoDispatch()
    {
        return m_autoDispatch;
    }

    public void setAutoDispatch(boolean dispatch)
    {
        m_autoDispatch = dispatch;
    }

    public AttributeSet getAttributeSet()
    {
        return attributeSet;
    }

    public void setAttributeSet(AttributeSet attributeSet)
    {
        this.attributeSet = attributeSet;
    }

    public Set getFileProfiles()
    {
        return m_fileProfiles;
    }

    public void setFileProfiles(Set fileprofiles)
    {
        this.m_fileProfiles = fileprofiles;
    }

    public String getTuTypes()
    {
        return tuTypes;
    }

    public void setTuTypes(String tuTypes)
    {
        this.tuTypes = tuTypes;
    }

    public int getTMEditType()
    {
        return TMEditType;
    }

    public void setTMEditType(int allowEditType)
    {
        this.TMEditType = allowEditType;
    }
    

    public long getWfStatePostId()
    {
        return m_wfStatePostId;
    }

    public void setWfStatePostId(long p_wfStatePostId)
    {
        this.m_wfStatePostId = p_wfStatePostId;
    }
    
}
