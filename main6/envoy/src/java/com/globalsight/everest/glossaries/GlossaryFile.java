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

package com.globalsight.everest.glossaries;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.GlobalSightLocale;
import java.util.Date;

/**
 * <P>This class describes a glossary file (or other file stored on
 * the local disk. </P>
 */
public class GlossaryFile
    extends PersistentObject
{
    //
    // PUBLIC CONSTANTS FOR USE BY TOPLINK
    //
    public static final String M_SOURCE_LOCALE = "m_sourceLocale";
    public static final String M_TARGET_LOCALE = "m_targetLocale";
    public static final String M_CATEGORY = "m_category";
    public static final String M_FILENAME = "m_fileName";

    //
    // PRIVATE MEMBER VARIABLES
    //    
    private GlobalSightLocale m_sourceLocale = null;
    private GlobalSightLocale m_targetLocale = null;  
    private boolean m_anySourceLocale = false;
    private boolean m_anyTargetLocale = false;    
    
    // Should be a list of categories.
    private String m_category = "";

    /**
     * Date of last modification.
     */
    private long m_fileSize = 0;

    /**
     * Date of last modification.
     */
    private Date m_lastModified = null;

    /**
     * The file name of this glossary relative to a root directory.
     */
    private String m_filename = "";

    /**
     * Default constructor to be used by TopLink only.
     * This is here solely because the persistence mechanism that persists
     * instances of this class is using TopLink, and TopLink requires a
     * public default constructor for all the classes that it handles
     * persistence for.
     */
    public GlossaryFile()
    {
        super();
    }

    public void setAnySourceLocale(boolean p_enable)
    {
        m_anySourceLocale = p_enable;
    }

    public void setSourceLocale(GlobalSightLocale p_locale)
    {
        m_sourceLocale = p_locale;
    }

    public boolean isForAnySourceLocale()
    {
        return m_anySourceLocale;
    }

    public GlobalSightLocale getSourceLocale()
    {
        return m_sourceLocale;
    }
    
    public String getGlobalSourceLocaleName()
    {
        return GlossaryUpload.KEY_ANY_SOURCE_LOCALE;
    }

    public void setAnyTargetLocale(boolean p_enable)
    {
        m_anyTargetLocale = p_enable;
    }

    public void setTargetLocale(GlobalSightLocale p_locale)
    {
        m_targetLocale = p_locale;
    }

    public boolean isForAnyTargetLocale()
    {
        return m_anyTargetLocale;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    public String getGlobalTargetLocaleName()
    {
        return GlossaryUpload.KEY_ANY_TARGET_LOCALE;
    }


    public void setCategory(String p_cat)
    {
        m_category = p_cat;
    }

    public String getCategory()
    {
        return m_category;
    }

    public void setFileSize(long p_size)
    {
        m_fileSize = p_size;
    }

    public long getFileSize()
    {
        return m_fileSize;
    }

    public void setLastModified(Date p_date)
    {
        m_lastModified = p_date;
    }

    public void setLastModified(long p_date)
    {
        m_lastModified = new Date(p_date);
    }

    public Date getLastModified()
    {
        return m_lastModified;
    }

    public void setFilename(String p_name)
    {
        m_filename = p_name;
    }

    public String getFilename()
    {
        return m_filename;
    }
    
    /**
     * Return string representation of object
     *
     * @return string representation of object
     */
    public String toString()
    {
        return "[GlossaryFile " + m_filename + " category=" + m_category +
            " src=" + m_sourceLocale + " anySrc=" + m_anySourceLocale + " trg=" + m_targetLocale + " anyTrg=" + m_anyTargetLocale +"]";
    }
}

