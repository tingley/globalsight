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

//Java
import java.util.Locale;

//GlobalSight
import com.globalsight.everest.foundation.DispatchCriteria;
import com.globalsight.util.GlobalSightLocale;


public class BasicL10nProfileAbbreviated
    extends BasicL10nProfileInfo
    implements java.io.Serializable
{
    // Attributes
    protected int               m_priority       = -1;
    protected String            m_sourceLocale   = null;
    protected String[]          m_targetLocales   = null;
    protected boolean           m_autoDispatch   = true;
    protected DispatchCriteria  m_dispatchCriteria = null;

    /**
     * Default Constructor
     */
    public BasicL10nProfileAbbreviated(long p_profileId, String p_name,
        String p_description, int p_priority, String p_companyId)
    {
        
        super(p_profileId, 
              p_name, 
              p_description,
              p_companyId);
        m_priority = p_priority;
    }

    /**
     * Get the priority of this profile.  All jobs associated with it
     * will be assigned this priority.
     */
    int getPriority()
    {
        return m_priority;
    }

    /**
     * Get the dispatch criteria of this localization profile.
     */
    public DispatchCriteria getDispatchCriteria()
    {
        return m_dispatchCriteria;
    }

    /**
     * Check whether the workflows created from templates in this
     * localization profile are to be dispatched automatically.
     *
     * @return True if workflows are to be dispatched automatically;
     * false otherwise.
     */
    public boolean dispatchIsAutomatic()

    {
        return m_autoDispatch;
    }

    /**
     * Get the displayable value of the source locale of this
     * localization profile based on the user's UI locale.
     *
     * @return The source locale's display string in user's UI locale.
     */
    public String getSourceLocale()
    {
        return m_sourceLocale;
    }

    /**
     * Get the list of target locales display string in this
     * localization profile based on user's UI locale.
     *
     * @return A list of target locales in this profile.  Each element in
     * the array is a displayable locale.
     */
    public String[] getTargetLocales()
    {
        return m_targetLocales;
    }

    /**
     * Set whether the workflows driven by this localization profile
     * are to be dispatched automatically.
     *
     * @param p_automatic True if the workflows are to be dispatched
     * automatically; false otherwise.
     */
    public void setAutomaticDispatch(boolean p_automatic)
    {
        m_autoDispatch = p_automatic;
    }

    /**
     * Set the description of the Localization Profile
     *
     * @param String The description
     */
    public void setDescription(String p_description)
    {
        m_description = p_description;
    }

    /**
     * Set a new name for this localization profile.
     *
     * @param p_name The new name for this localization profile.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
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
     * @param p_locale The source locale.
     * @param p_uiLocale The UI locale of the user.
     */
    public void setSourceLocale(GlobalSightLocale p_locale, Locale p_uiLocale)
    {
        m_sourceLocale = p_locale.getDisplayName(p_uiLocale);
    }

    /**
     * Set the target locales of this localization profile.
     * @param p_locales The source locale.
     * @param p_uiLocale The UI locale of the user.
     */
    public void setTargetLocales(GlobalSightLocale[] p_locales, Locale p_uiLocale)
    {
        if (p_locales != null)
        {
            int size = p_locales.length;
            m_targetLocales = new String[size];
            for (int i = 0; i < size; i++)
            {
                m_targetLocales[i] = p_locales[i].getDisplayName(p_uiLocale);
            }
        }
    }
}
