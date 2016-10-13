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

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.projecthandler.WorkflowTemplateInfo;
import com.globalsight.util.GlobalSightLocale;

/**
 * This data class contains cross locale leveraging info for a workflow.
 */

public class LeverageLocales extends PersistentObject
{
    private static final long serialVersionUID = -6886247982353941916L;

    // constants for TOPLink querying
    static public final String LEVERAGING_LOCALE = "m_leveragingLocale";

    private GlobalSightLocale leveragingLocale;
    private WorkflowTemplateInfo workflowTemplateInfoBackPointer;

    /**
     * Default constructor to be used by TopLink only. This is here solely
     * because the persistence mechanism that persists instances of this class
     * is using TopLink, and TopLink requires a public default constructor for
     * all the classes that it handles persistence for.
     */
    public LeverageLocales()
    {
    }

    /**
     * Constructor
     * 
     * @param p_leveragingLocale
     *            locale for cross locale leveraging
     */
    public LeverageLocales(GlobalSightLocale p_leveragingLocale)
    {
        leveragingLocale = p_leveragingLocale;
    }

    public long getId()
    {
        return getTemporarilyUnavailableId();
    }

    /**
     * Get locale
     * 
     * @return GlobalSightLocale
     */
    public GlobalSightLocale getLocale()
    {
        return leveragingLocale;
    }

    public void setLocale(GlobalSightLocale locale)
    {
        leveragingLocale = locale;
    }

    /**
     * Makes a clone of this object, but makes it new so that it can we
     * persisted using TopLink (basically no id).
     * 
     * @return a new LeverageLocales so that it can be inserted using TopLink.
     */
    public LeverageLocales cloneForInsert()
    {
        LeverageLocales leverageLocale = new LeverageLocales(this.getLocale());
        return leverageLocale;
    }

    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        return super.toString()
                + " m_leveragingLocale="
                + leveragingLocale.toString()
                + " m_workflowTemplateInfoBackPointer="
                + (workflowTemplateInfoBackPointer != null ? workflowTemplateInfoBackPointer
                        .getIdAsLong().toString() : "null");
    }

    /**
     * Set the back pointer to workflow info for TopLink only. This method is
     * here solely because having a back pointer is a more convenient way to
     * implement one-to-many relationship with TopLink.
     * 
     * @param WorkflowTemplateInfo
     *            The WorkflowTemplateInfo being pointed to.
     */
    public void setBackPointer(WorkflowTemplateInfo p_wfTemplateInfo)
    {
        workflowTemplateInfoBackPointer = p_wfTemplateInfo;
    }

    public WorkflowTemplateInfo getBackPointer()
    {
        return workflowTemplateInfoBackPointer;
    }

    /**
     * Deactivate this TaskAssignment object. i.e. Logically delete it.
     */
    void deactivate()
    {
        this.isActive(false);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object paramObject)
    {
        return super.equals(paramObject);
    }
}