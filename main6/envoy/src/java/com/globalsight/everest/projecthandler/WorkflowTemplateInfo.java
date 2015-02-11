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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.globalsight.everest.company.CompanyThreadLocal;
import com.globalsight.everest.foundation.L10nProfile;
import com.globalsight.everest.foundation.LeverageLocales;
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.workflow.WorkflowTemplate;
import com.globalsight.util.GlobalSightLocale;

/**
 * This is a wrapper class for workflow template, which contains the
 * additional info that are stored in our database (not stored in
 * i-Flow db).
 */
public class WorkflowTemplateInfo
    extends PersistentObject
{
    private static final long serialVersionUID = 7148736237934022388L;
    
    // query keys for TOPLink queries
    public static final String TEMPLATE_ID = "m_templateId";
    public static final String SOURCE_LOCALE = "m_sourceLocale";
    public static final String TARGET_LOCALE = "m_targetLocale";
    public static final String WF_MANAGER_IDS = "m_wfManagerIds";
    
    // Workflow type constants
    public static final String TYPE_TRANSLATION = WorkflowTypeConstants.TYPE_TRANSLATION;
    public static final String TYPE_DTP = WorkflowTypeConstants.TYPE_DTP;

    // persistence variables

    private String             m_description = null;
    private Project            m_project = null;
    private GlobalSightLocale  m_sourceLocale = null;
    private GlobalSightLocale  m_targetLocale = null;
    private long               m_templateId = -1;
    private String             m_encoding = null;
    private boolean            m_notifyPm = true;
    private L10nProfile        m_l10nProfile= null;
    private Vector             m_leveragingLocales;
    private List<String>       m_wfManagerIds = new ArrayList<String>();

    private String             m_workflowType = TYPE_TRANSLATION;
    private String 			   m_companyId = null;


    // non-persistence variables
    private WorkflowTemplate m_workflowTemplate = null;


    //
    //  Begin: Constructor
    //

    /**
     * Default constructor to be used by TopLink only.  This is here
     * solely because the persistence mechanism that persists
     * instances of this class is using TopLink, and TopLink requires
     * a public default constructor for all the classes that it
     * handles persistence for.
     */
    public WorkflowTemplateInfo()
    {
    }

    public WorkflowTemplateInfo(String p_name, String p_description,
                                Project p_project, boolean p_notifyPm,
                                List p_wfManagerIds, GlobalSightLocale p_sourceLocale,
        GlobalSightLocale p_targetLocale, String p_encoding,
        Vector p_leveragingLocales)
    {
        setName(p_name);
        m_description = p_description;
        m_project = p_project;
        m_notifyPm = p_notifyPm;
        setWorkflowManagerIds(p_wfManagerIds); // for validation purposes
        m_sourceLocale = p_sourceLocale;
        m_targetLocale = p_targetLocale;
        m_encoding = p_encoding;
        m_leveragingLocales = p_leveragingLocales;
        m_companyId = CompanyThreadLocal.getInstance().getValue();

        for (Iterator it = m_leveragingLocales.iterator(); it.hasNext(); )
        {
            LeverageLocales leverageLocales = (LeverageLocales)it.next();
            leverageLocales.setBackPointer(this);
        }
    }

    //
    // Helper Methods
    //

    /**
     * Get the target locale's code set
     * @return The encoding of the target locale.
     */
    public String getCodeSet()
    {
        return m_encoding;
    }

    public boolean isDtpWorkflow(String p_workflowType) {
    	return TYPE_DTP.equals(p_workflowType);
    }
    
    public void setWorkflowType(String p_workflowType) {
    	m_workflowType = p_workflowType;
    }
    
    public String getWorkflowType() {
    	return m_workflowType;
    }
    
    /**
     * Get the company id of this workflow template.
     * @return The workflow template's company id.
     */
    public String getCompanyId()
    {
        return m_companyId;
    }
    
    /**
     * Get the description of this workflow template.
     * @return The workflow template's description.
     */
    public String getDescription()
    {
        return m_description;
    }

    /**
     * Get the project manager id for this workflow.
     * @return The project manager id for this workflow.
     */
    public String getProjectManagerId()
    {
        return m_project.getProjectManagerId();
    }

    /**
     * Get the project for this workflow.
     * @return The project for this workflow.
     */
    public Project getProject()
    {
        return m_project;
    }

    /**
     * Get the source locale of this workflow.
     * @return The source locale of this workflow.
     */
    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    /**
     * Get the target locale
     *
     * @return Locale The target locale
     */
    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    /**
     * Get a list of leveraging locales
     *
     * @return Vector of GlobalSightLocale. If leveraging locales are
     * not set, null is returned.
     */
    public Vector getLeveragingLocales()
    {
        Vector globalSightLocales = null;

        if (m_leveragingLocales.size() > 0)
        {
            globalSightLocales = new Vector(m_leveragingLocales.size());

            for (Iterator it = m_leveragingLocales.iterator(); it.hasNext(); )
            {
                globalSightLocales.add(((LeverageLocales)it.next()).getLocale());
            }
        }

        return globalSightLocales;
    }

    public Vector getLeverageLocales()
    {
        return m_leveragingLocales;
    }

    /**
     * Get the workflow manager id for this workflow.
     * @return The workflow manager id for this workflow.
     */
    public List<String> getWorkflowManagerIds()
    {
        return m_wfManagerIds;
    }

    /**
     * Get workflow template ID
     *
     * @return long The workflow template ID
     */
    public long getWorkflowTemplateId()
    {
        return m_templateId;
    }

    /**
     * Determine whether the email notification is enabled for the PM.
     * @return True if it's selected (enabled).  Otherwise, return false.
     */
    public boolean notifyProjectManager()
    {
        return m_notifyPm;
    }

    /**
     * Set the PM email notification flag to be the specified value.
     * @param p_notifyPm - The flag to be set.
     */
    public void notifyProjectManager(boolean p_notifyPm)
    {
        m_notifyPm = p_notifyPm;
    }

    /**
     * Set the leverage locales
     * @return void
     */
    public void setLeverageLocales(Vector p_leveragingLocales)
    {
        m_leveragingLocales = new Vector();
        m_leveragingLocales.addAll(p_leveragingLocales);

        for (Iterator it = m_leveragingLocales.iterator(); it.hasNext(); )
        {
            LeverageLocales leverageLocale = (LeverageLocales)it.next();
            leverageLocale.setBackPointer(this);
        }
    }
    
    // For hibernate to use
    public void setLeveragingLocalesSet(Set p_leveragingLocales)
    {
    	setLeverageLocales(new Vector(p_leveragingLocales));
    }
    // For hibernate to use
    public Set getLeveragingLocalesSet()
    {
        return new HashSet(m_leveragingLocales);
    }

    /**
     * Set the encoding for the target locale to be the specified value.
     * @param p_encoding - The target encoding to be set.
     */
    public void setCodeSet(String p_encoding)
    {
        m_encoding = p_encoding;
    }

    /**
     * Set the company id of this template to be the specified value.
     * @param p_companyId - The company id to be set.
     */
    public void setCompanyId(String p_companyId)
    {
        m_companyId = p_companyId;
    }
    
    /**
     * Set the description of this template to be the specified value.
     * @param p_description - The description to be set.
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set the project for this workflow.
     * @param p_project - The project the template is associated with.
     */
    public void setProject(Project p_project)
    {
        m_project = p_project;
    }

    /**
     * Set the project manager id for this workflow.
     * @param p_projectManagerId - The PM id to be set.
     */
    public void setWorkflowManagerIds(List<String> p_wfManagerIds)
    {
        // since the default for a non-selected option is blank,
        // we should check here.
        if (p_wfManagerIds == null ||
            p_wfManagerIds.size() == 0)
        {
            // make an empty list - do not leave as NULL
            m_wfManagerIds = new ArrayList<String>();
        }
        else
        {
            m_wfManagerIds = p_wfManagerIds;
        }
    }

    /**
     * Set the workflow template object to be the specified value.
     * @param p_workflowTemplate - The template to be set.
     */
    public void setWorkflowTemplate(WorkflowTemplate p_workflowTemplate)
    {
        m_workflowTemplate = p_workflowTemplate;
        // reset the iflow template id (in case of modification of wf
        // template)
        m_templateId = p_workflowTemplate.getId();
    }

    public void setL10nProfile(L10nProfile p_l10nProfile)
    {
        m_l10nProfile = p_l10nProfile;
    }
    public L10nProfile getL10nProfile()
    {
    	return m_l10nProfile;
    }
    /**
     * Return a string representation of the object.
     */
    public String toString()
    {
        return super.toString()
            + " m_templateId=" + m_templateId
            + " m_workflowType=" + m_workflowType
            + " m_name=" + getName()
            + " m_sourceLocale="
            + (m_sourceLocale!=null?m_sourceLocale.toString(): "null")
            + " m_targetLocale="
            + (m_targetLocale!=null?m_targetLocale.toString(): "null")
            + " m_encoding="
            + (m_encoding!=null?m_encoding:"null")
            + (m_wfManagerIds != null && m_wfManagerIds.size() > 0 ? 
               m_wfManagerIds.toString() : "empty")
            ;
    }

    //
    // Package Level Methods
    //

    /**
     * Deactivate this WorkflowTemplateInfo object.
     * i.e. Logically delete it.
     */
    public void deactivate()
    {
        isActive(false);
    }

    /**
     * Get the workflow template object (only set during create/update).
     * @return The workflow template object.
     */
    WorkflowTemplate getWorkflowTemplate()
    {
        return m_workflowTemplate;
    }

    /**
     * Set the source locale to be the specified locale.
     * @param p_sourceLocale - The locale to be set.
     */
    void setSourceLocale(GlobalSightLocale p_sourceLocale)
    {
        m_sourceLocale = p_sourceLocale;
    }

    /**
     * Set the target locale to be the specified locale.
     * @param p_targetLocale - The locale to be set.
     */
    void setTargetLocale(GlobalSightLocale p_targetLocale)
    {
        m_targetLocale = p_targetLocale;
    }

    /**
     * Set the workflow template id to be the specified value.
     * @param p_id - The id to be set.
     */
    void setWorkflowTemplateId(long p_id)
    {
        m_templateId = p_id;
    }

    /**
     * Makes a clone of this object, but makes it new so that it can
     * be persisted using TopLink (basically no id).
     *
     * @return WorkflowInfo and all task assignments in it, so that it
     * can be inserted using TopLink.
     * @exception CloneNotSupportedException
     */
    public WorkflowTemplateInfo cloneForInsert()
    {
        WorkflowTemplateInfo wf = new WorkflowTemplateInfo(
            this.m_name,
            this.m_description,
            this.m_project,
            this.m_notifyPm,
            this.m_wfManagerIds,
            this.m_sourceLocale,
            this.m_targetLocale,
            this.m_encoding,
            this.m_leveragingLocales);

        return wf;
    }

	public String getEncoding()
	{
		return m_encoding;
	}

	public void setEncoding(String m_encoding)
	{
		this.m_encoding = m_encoding;
	}

	public boolean isNotifyPm()
	{
		return m_notifyPm;
	}

	public void setNotifyPm(boolean pm)
	{
		m_notifyPm = pm;
	}
} 
