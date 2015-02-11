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
package com.globalsight.everest.edit.offline.upload;

import com.globalsight.util.GlobalSightLocale;
import com.globalsight.everest.edit.offline.AmbassadorDwUpException;
import com.globalsight.everest.edit.offline.AmbassadorDwUpConstants;
import com.globalsight.everest.edit.offline.AmbassadorDwUpExceptionConstants;
import com.globalsight.everest.webapp.pagehandler.offline.OfflineConstants;

import java.io.Serializable;
import java.util.Map;
import java.util.Vector;
import java.util.Collection;

/**
 * A data class that specifies various upload parameters.
 */
public class UploadParams
    implements Serializable
{
    //
    // Private Members
    //

    private int m_tagDisplayFormatID = -1;
    private GlobalSightLocale m_sourceLocale = null;
    private GlobalSightLocale m_targetLocale = null;
    private int m_fileFormat = -1;
    private String m_pageIdAsString = null;
    private String m_pageName = null;
    private boolean m_canUseUrl = false;
    private Map m_mergeOverrideDirectives = null;
    private boolean m_mergeEnabled = false;
    private Collection m_excludeTypeNames = null;
    private Long m_targetPageId = null;

    //
    // Constructors
    //

    /**
     * Constructor.
     */
    public UploadParams()
    {
        super();
    }

    //
    // Public Methods
    //

    public void setPageId(String p_id)
    {
        m_pageIdAsString = p_id;
    }

    public String getPageId()
    {
        return m_pageIdAsString;
    }

    public void setPageName(String p_name)
    {
        m_pageName = p_name;
    }

    public String getPageName()
    {
        return m_pageName;
    }

    public void setCanUseUrl(boolean p_state)
    {
        m_canUseUrl = p_state;
    }

    public boolean getCanUseUrl()
    {
        return m_canUseUrl;
    }

    public void setMergeOverrideDirectives(Map p_mergeOverrideDirectives)
    {
        m_mergeOverrideDirectives = p_mergeOverrideDirectives;
    }

    public Map getMergeOverrideDirectives()
    {
        return m_mergeOverrideDirectives;
    }

    public void setMergeEnabled(boolean p_state)
    {
        m_mergeEnabled = p_state;
    }

    public boolean getMergeEnabled()
    {
        return m_mergeEnabled;
    }

    public void setExcludedTypeNames(Collection p_excludeTypeNames)
    {
        m_excludeTypeNames = p_excludeTypeNames;
    }

    public Collection getExcludedTypeNames()
    {
        return m_excludeTypeNames;
    }

    /**
     * Returns the Tag display Format ID.
     */
    public int getTagDisplayFormatID()
    {
        return m_tagDisplayFormatID;
    }

    /**
     * Returns the source locale.
     */
    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }

    /**
     * Returns the target locale.
     */
    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    /**
     * Sets the ID of the detected file format.
     *
     * See AmbassadorDwUpConstants:
     *    DOWNLOAD_FILE_FORMAT_LIST_START
     *    DOWNLOAD_FILE_FORMAT_TXT
     *    DOWNLOAD_FILE_FORMAT_RTF
     *    DOWNLOAD_FILE_FORMAT_TRADOSRTF
     *    DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE
     *    DOWNLOAD_FILE_FORMAT_LIST_END
     */
    public void setFileFormatId(int p_fileFormat)
    {
        m_fileFormat = p_fileFormat;
    }

    /**
     * Returns the ID of the requested format for the offline file.
     *
     * See AmbassadorDwUpConstants:
     *    DOWNLOAD_FILE_FORMAT_LIST_START
     *    DOWNLOAD_FILE_FORMAT_TXT
     *    DOWNLOAD_FILE_FORMAT_RTF
     *    DOWNLOAD_FILE_FORMAT_TRADOSRTF
     *    DOWNLOAD_FILE_FORMAT_RTF_PARAVIEW_ONE
     *    DOWNLOAD_FILE_FORMAT_LIST_END
     *
     * @return - a file format id.
     */
    public int getFileFormatId()
    {
        return m_fileFormat;
    }

    /**
     * Sets the tag display format.
     */
    public void setTagDisplayFormatID(int p_newTagDisplayFormatID)
    {
        m_tagDisplayFormatID = p_newTagDisplayFormatID;
    }

    /**
     * Sets the source locale.
     */
    public void setSourceLocale(GlobalSightLocale p_locale)
    {
        m_sourceLocale = p_locale;
    }

    /**
     * Sets the target locale.
     */
    public void setTargetLocale(GlobalSightLocale p_locale)
    {
        m_targetLocale = p_locale;
    }

    public void setTargetPageId(Long p_id)
    {
        m_targetPageId = p_id;
    }

    public Long getTargetPageId()
    {
        return m_targetPageId;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("TagDisplayformat=" + m_tagDisplayFormatID);
        sb.append(", SrcLocale=" +
            ((m_sourceLocale == null) ? "null" : m_sourceLocale.toString()));
        sb.append(", TrgLocale=" +
            ((m_targetLocale == null) ? "null" : m_targetLocale.toString()));
        sb.append(", FileFormat=" + m_fileFormat);

        return sb.toString();
    }
}
