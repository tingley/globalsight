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

package com.globalsight.everest.webapp.pagehandler.administration.glossaries;

import com.globalsight.everest.webapp.pagehandler.administration.glossaries.GlossaryConstants;
import com.globalsight.util.GlobalSightLocale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.io.Serializable;

/**
 * <p>A helper class that combines all state variables necessary for
 * the glossary page handler to do his work into one object.</p>
 *
 * <p>This class is also used by the Online Editor (see
 * EditorResourcePageHandler).</p>
 *
 * @see GlossaryConstants
 */
public class GlossaryState
    implements GlossaryConstants, Serializable
{
    //
    // Member Variables
    //
    GlobalSightLocale m_sourceLocale = null;
    GlobalSightLocale m_targetLocale = null;
    String m_category = null;
    int m_sortColumn = 1;

    // message when upload succeeds or fails
    String m_message = null;

    // locale pairs to offer in upload screen
    Collection m_allSourceLocales;
    Collection m_allTargetLocales;

    /**
     * Sorted list of GlossaryFile objects.
     */
    private ArrayList m_glossaries;

    //
    // Constructors
    //

    public GlossaryState()
    {
    }

    //
    // Public methods
    //

    public void setGlossaries(ArrayList p_list)
    {
        m_glossaries = p_list;
    }

    public ArrayList getGlossaries()
    {
        return m_glossaries;
    }

    public void setSourceLocale(GlobalSightLocale p_locale)
    {
        m_sourceLocale = p_locale;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    public void setTargetLocale(GlobalSightLocale p_locale)
    {
        m_targetLocale = p_locale;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    public void setCategory(String p_cat)
    {
        m_category = p_cat;
    }

    public String getCategory()
    {
        return m_category;
    }

    public void setSortColumn(int p_col)
    {
        m_sortColumn = p_col;
    }

    public int getSortColumn()
    {
        return m_sortColumn;
    }

    public void setMessage(String p_msg)
    {
        m_message = p_msg;
    }

    public String getMessage()
    {
        return m_message;
    }

    public void setAllSourceLocales(Collection p)
    {
        m_allSourceLocales = p;
    }

    public Collection getAllSourceLocales()
    {
        return m_allSourceLocales;
    }

    public void setAllTargetLocales(Collection p)
    {
        m_allTargetLocales = p;
    }

    public Collection getAllTargetLocales()
    {
        return m_allTargetLocales;
    }
}
