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

// globalsight
import com.globalsight.everest.page.PrimaryFile;
import com.globalsight.everest.persistence.PersistentObject;

// java
import java.io.File;
import java.util.Date;

/**
 * This represents a file that has been imported into the system and has not
 * been parsed or extracted. So the user will work on the file in its native
 * format without the use of TM and having the localizable data parsed.
 */
public class UnextractedFile extends PersistentObject implements PrimaryFile
{
    // ///////////////////////////////////////////////
    // private variables
    // ///////////////////////////////////////////////

    /**
     * 
     */
    private static final long serialVersionUID = 2795654601051835897L;

    /**
     * This is the storage path of the file and is a relative path to the main
     * GS storage directory.
     */
    private String m_storagePath;

    /**
     * The length of the file.
     */
    private long m_fileLength = 0;

    /**
     * The last time the file was updated and stored.
     */
    private Date m_lastModifiedDate = null;

    /**
     * The user id of the user who updated the file.
     */
    private String m_lastModifiedBy = null;

    /**
     * Return the name of the file. This is the base file name without any path.
     */
    public String getName()
    {
        int lastIndex = m_storagePath.lastIndexOf(File.separatorChar);
        return m_storagePath.substring(lastIndex + 1);
    }

    /**
     * Return the path to the file, without the actual file name.
     */
    public String getParentPath()
    {
        // get the first part without the file name
        int lastIndex = m_storagePath.lastIndexOf(File.separatorChar);
        String path = new String();
        if (lastIndex > 0)
        {
            path = m_storagePath.substring(0, lastIndex);
        }
        return path;
    }

    /**
     * Return the path to where this file is stored in System4.
     */
    public String getStoragePath()
    {
        return m_storagePath;
    }

    /**
     * Set the storage path of the file.
     */
    public void setStoragePath(String p_storagePath)
    {
        m_storagePath = p_storagePath;
    }

    /**
     * Returns the user id of the person who last modified the file.
     */
    public String getLastModifiedBy()
    {
        return m_lastModifiedBy;
    }

    /**
     * Set the user id of the person who last modified the file.
     */
    public void setLastModifiedBy(String p_userId)
    {
        m_lastModifiedBy = p_userId;
    }

    /**
     * Returns the date this file was last modified.
     */
    public Date getLastModifiedDate()
    {
        return m_lastModifiedDate;
    }

    /**
     * Sets the last time the file was modified.
     */
    public void setLastModifiedDate(long p_time)
    {
        m_lastModifiedDate = new Date(p_time);
    }

    public void setLastModifiedDate(Date p_date)
    {
        m_lastModifiedDate = p_date;
    }

    /**
     * Returns the length of the file.
     */
    public long getLength()
    {
        return m_fileLength;
    }

    /**
     * Sets the length of the file.
     */
    public void setLength(long m_length)
    {
        m_fileLength = m_length;
    }

    /**
     * Create a new primray file from this one's data.
     */
    public PrimaryFile clonePrimaryFile()
    {
        UnextractedFile pf = new UnextractedFile();
        pf.setStoragePath(getStoragePath());
        pf.setLength(getLength());
        pf.setLastModifiedDate(getLastModifiedDate());
        pf.setLastModifiedBy(getLastModifiedBy());
        return pf;
    }

    /**
     * Return the type of file as UNEXTRACTED
     */
    public int getType()
    {
        return UNEXTRACTED_FILE;
    }

    public boolean isEmpty()
    {
        boolean isEmpty = true;

        if (m_fileLength > 0)
        {
            isEmpty = false;
        }
        else if (stringIsNotNull(m_lastModifiedBy))
        {
            isEmpty = false;
        }
        else if (stringIsNotNull(m_storagePath))
        {
            isEmpty = false;
        }
//        else if (m_lastModifiedDate != null)
//        {
//            isEmpty = false;
//        }

        return isEmpty;
    }

    private boolean stringIsNotNull(String s)
    {
        return s != null && s.trim().length() > 0;
    }

    public Long getFileLength()
    {
        return new Long(m_fileLength);
    }

    public void setFileLength(Long length)
    {
        m_fileLength = length == null? 0 : length.longValue();
    }
}
