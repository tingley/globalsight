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

package com.globalsight.everest.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.globalsight.everest.tuv.LeverageGroup;

/**
 * This represents a file that has been imported into they system and also
 * parsed and extracted. So the user can work with only the localizable pieces
 * of the file.
 */
abstract public class ExtractedFile implements PrimaryFile
{
    private static final long serialVersionUID = 791795100574082735L;

    // ====================================================
    // protected attributes
    // ====================================================
    protected String m_internalBaseHref = " ";

    protected String m_externalBaseHref = " ";

    // holding the templates of the file
    protected Map<Long, PageTemplate> m_templateMap = new HashMap<Long, PageTemplate>();

    // holding the leverage group(s) of the file
    protected List<LeverageGroup> m_leverageGroupList = new ArrayList<LeverageGroup>();

    protected List<Long> m_leverageGroupIds = new ArrayList<Long>();

    protected String m_gxmlVersion = "";

    /**
     * Default constructor.
     */

    /**
     * Returns the LeverageGroup identifiers associated with the file. The
     * return type is a non-null value (the arrayList could be empty though).
     * 
     * @return LeverageGroup identifiers as Longs.
     */
    public List getLeverageGroupIds()
    {
        // This field is only pre-populated during a query. If a new
        // file is created and persisted, the ids are not set in this
        // case. Therefore, we need to get them from the leverage
        // group objects.
        if (m_leverageGroupIds.size() == 0)
        {
            updateLeverageGroupIds(m_leverageGroupIds);
        }

        return m_leverageGroupIds;
    }

    /**
     * Get the external base href of this file.
     * 
     * @return The base href if there is one or NULL if one not specified.
     */
    public String getExternalBaseHref()
    {
        return m_externalBaseHref;
    }

    /**
     * Set the external base href for this file.
     * 
     * @param p_baseHref -
     *            The external base href for this file.
     */
    public void setExternalBaseHref(String p_baseHref)
    {
        m_externalBaseHref = p_baseHref;
    }

    /**
     * Get the internal base href of this file.
     * 
     * @return The base href if there is one or NULL if one is not specified.
     */
    public String getInternalBaseHref()
    {
        return m_internalBaseHref;
    }

    /**
     * Set the internal base href for this file.
     * 
     * @param p_baseHref -
     *            The internal base href for this file.
     */
    public void setInternalBaseHref(String p_baseHref)
    {
        m_internalBaseHref = p_baseHref;
    }

    /**
     * Return the Template of the given type associated with the file.
     * 
     * @return a file Template of the given type.
     */
    public PageTemplate getPageTemplate(int p_templateType)
    {
        return (PageTemplate) getTemplateMap().get(new Long(p_templateType));
    }

    /**
     * Adds the LeverageGroup to the file. Does not persist the change.
     * 
     * @param p_leverageGroup
     *            the leverage group to associate with the file.
     */
    public void addLeverageGroup(LeverageGroup p_leverageGroup)
    {
        m_leverageGroupList.add(p_leverageGroup);
    }

    /**
     * Removes the LeverageGroup from the file. Does not persist the change.
     * 
     * @param p_leverageGroup
     *            the leverage group to remove from the file.
     */
    public void removeLeverageGroup(LeverageGroup p_leverageGroup)
    {
        m_leverageGroupList.remove(p_leverageGroup);
    }

    /**
     * Get the leverage groups of this file. Can be an empty ArrayList.
     * 
     * @return The leverage groups of this file.
     */
    public List<LeverageGroup> getLeverageGroups()
    {
        return m_leverageGroupList;
    }

    public List<LeverageGroup> getLeverageGroupSet()
    {
        return m_leverageGroupList;
    }

    public void setLeverageGroupSet(List<LeverageGroup> groups)
    {
        m_leverageGroupList = groups;
    }

    /**
     * Add the Template of the page based on the type.. Does not persist the
     * change.
     * 
     * @param p_template
     *            the template to associate with the page.
     * @param p_templateType
     *            the type of template.
     */
    public void addPageTemplate(PageTemplate p_template, int p_templateType)
    {
        // set this page as a back pointer for TopLink purposes
        // tbd - change the method name??
        // p_template.setSourcePage(this);
        m_templateMap.put(new Long(p_templateType), p_template);
    }

    /*
     * Remove the template with the specified type.
     */
    public void removePageTemplate(int p_templateType)
    {
        m_templateMap.remove(new Integer(p_templateType));
    }

    /**
     * Get the generated templates for this file. The templates are returned as
     * a java.util.Map with the type as the key.
     * 
     * @return The template map.
     */
    public Map getTemplateMap()
    {
        return m_templateMap;
    }

    public Map getTemplates()
    {
        if (m_templateMap.size() == 0)
        {
            return null;
        }
        return m_templateMap;
    }

    public void setTemplates(Map templates)
    {
        m_templateMap.clear();
        if (templates != null)
        {
            Iterator iterator = templates.values().iterator();
            while (iterator.hasNext())
            {
                PageTemplate template = (PageTemplate) iterator.next();
                m_templateMap.put(new Long(template.getType()), template);
            }
        }
    }

    // manual population of LeverageGroupIds collection (only for a
    // newly created file that has not been queried).
    private void updateLeverageGroupIds(List<Long> p_leverageGroupIds)
    {
        int size = m_leverageGroupList.size();

        for (int i = 0; i < size; i++)
        {
            LeverageGroup lg = (LeverageGroup) m_leverageGroupList.get(i);
            p_leverageGroupIds.add(lg.getIdAsLong());
        }
    }

    /**
     * Clear out the template map.
     */
    public void clearTemplateMap()
    {
        m_templateMap.clear();
    }

    /**
     * Clear out all leverage groups.
     */
    public void clearLeverageGroupList()
    {
        m_leverageGroupList.clear();
    }

    /**
     * Return the type of file as EXTRACTED
     */
    public int getType()
    {
        return EXTRACTED_FILE;
    }

    /**
     * Sets the GXML version string.
     */
    public void setGxmlVersion(String p_version)
    {
        m_gxmlVersion = p_version;
    }

    /**
     * Gets the GXML version string.
     */
    public String getGxmlVersion()
    {
        return m_gxmlVersion;
    }

    /**
     * This method is overwritten for TOPLink. TOPLink doesn't query all
     * collections of objects within an object. So if a ExtractedFile is
     * serialized - the leverage groups may not be available (because they
     * haven't been queried yet). Overwriting the method forces the query to
     * happen so when it is serialized all pieces of the object are serialized
     * and availble to the client.
     */

    /*
     * This method will cause the exception when system shutdown, but we still
     * don't know what is the usage of this method.
     * Left the method commmented and can remove it unless there is no related issue occured.
     * commented at 11/17/2008
     */

    // private void writeObject(java.io.ObjectOutputStream out)
    // throws java.io.IOException
    // {
    // // touch leverage groups, and templates - since they are set
    // // up to only populate when needed
    // m_leverageGroupList.size();
    // m_templateMap.size();
    //
    // // call the default writeObject
    // out.defaultWriteObject();
    // }
    /**
     * Return a string representation of the object.
     * 
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append(" m_gxmlVersion=");
        sb.append(m_gxmlVersion);
        sb.append(" m_internalBaseHref=");
        sb.append(m_internalBaseHref);
        sb.append(" m_externalBaseHref=");
        sb.append(m_externalBaseHref);
        sb.append("\nm_templateMap=");
        m_templateMap.size();
        sb.append(m_templateMap.toString());
        sb.append("\nm_leverageGroupList=");
        m_leverageGroupList.size();
        sb.append(m_leverageGroupList.toString());
        sb.append("\n");

        return sb.toString();
    }
}
