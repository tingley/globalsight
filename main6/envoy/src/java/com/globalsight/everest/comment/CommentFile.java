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

package com.globalsight.everest.comment;
 
import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.util.GlobalSightLocale;
import java.util.Date;

/**
 * <P>This class describes a comment reference file (or other file stored on
 * the local disk. </P>
 */
public class CommentFile
    extends PersistentObject
{
    //
    // PUBLIC CONSTANTS FOR USE BY TOPLINK
    //
    public static final String M_TARGET_LOCALE = "m_targetLocale";
    public static final String M_FILENAME = "m_fileName";

    //
    // PRIVATE MEMBER VARIABLES
    //    
    private GlobalSightLocale m_targetLocale = null;  
    

    /**
     * Date of last modification.
     */
    private long m_fileSize = 0;

    /**
     * Date of last modification.
     */
    private Date m_lastModified = null;

    /**
     * The file name of this comment reference relative to a root directory.
     */
    private String m_filename = "";

    /**
     * The access level of the file. 
     */
    private String m_access = "";

    /**
     * Has the file been saved?
     */
    private boolean m_saved = true;
    
    /**
     * The absolute path of this comment reference file.
     */
    private String absolutePath = "";

    /**
     * Default constructor to be used by TopLink only.
     * This is here solely because the persistence mechanism that persists
     * instances of this class is using TopLink, and TopLink requires a
     * public default constructor for all the classes that it handles
     * persistence for.
     */
    public CommentFile()
    {
        super();
    }

    public void setTargetLocale(GlobalSightLocale p_locale)
    {
        m_targetLocale = p_locale;
    }

    public GlobalSightLocale getTargetLocale()
    {
        return m_targetLocale;
    }

    public void setFileSize(long p_size)
    {
        m_fileSize = p_size;
    }

    public long getFileSize()
    {
        return m_fileSize;
    }

    public void setFileAccess(String p_access)
    {
        m_access = p_access;
    }

    public String getFileAccess()
    {
        return m_access;
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

    public void setSaved(boolean p_saved)
    {
        m_saved = p_saved;
    }

    public boolean isSaved()
    {
        return m_saved;
    }

    public boolean equals(Object p_obj)
    {
        CommentFile cf = (CommentFile)p_obj;
        if (m_filename.equals(cf.getFilename()) &&
            m_saved == cf.isSaved() &&
            m_fileSize == cf.getFileSize())
            return true;
        return false;
    }
    
    /**
     * Return string representation of object
     *
     * @return string representation of object
     */
    public String toString()
    {
        return "[CommentFile " + m_filename + 
            " trg=" + m_targetLocale + " saved=" + m_saved + "]";
    }

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
}

