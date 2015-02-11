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
package com.globalsight.cxe.entity.fileextension;

import com.globalsight.everest.persistence.PersistentObject;

/** Implements an FileExtension */
public class FileExtensionImpl extends PersistentObject implements
        FileExtension
{
    private static final long serialVersionUID = 499331744284465857L;

    public boolean useActive = true;

    public FileExtensionImpl()
    {
        m_name = null;
    }

    /** Constructs an FileExtensionImpl with id, name* */
    // public FileExtensionImpl(long p_id, String p_name)
    public FileExtensionImpl(String p_name, String p_companyId)
    {
        m_name = p_name;
        m_companyId = Long.parseLong(p_companyId);
    }

    public FileExtensionImpl(String p_name, long p_companyId)
    {
        m_name = p_name;
        m_companyId = p_companyId;
    }

    /** Constructs an FileExtensionImpl from an FileExtension * */
    public FileExtensionImpl(FileExtension o)
    {
        this(o.getName(), o.getCompanyId());
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public long getCompanyId()
    {
        return this.m_companyId;
    }

    /**
     * Get name of the company this activity belong to.
     * 
     * @return The company name.
     */
    public void setCompanyId(long p_companyId)
    {
        this.m_companyId = p_companyId;
    }

    /**
     * * Return the name of the File Extension *
     * 
     * @return File Extension name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * * Sets the name of the File Extension
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    /** Returns a string representation of the object */
    public String toString()
    {
        return m_name;
    }

    /**
     * Return a string representation of the object for debugging purposes.
     * 
     * @return a string representation of the object for debugging purposes.
     */
    public String toDebugString()
    {
        return super.toString() + " m_name="
                + (m_name == null ? "null" : m_name);
    }

    /**
     * Two file extensions are the same if their name is the same. Don't look at
     * the id in case a FileExtension object was created without an id just for
     * testing the equality.
     */
    public boolean equals(Object p_obj)
    {
        if (p_obj instanceof FileExtensionImpl)
        {
            return (getName().equals(((FileExtensionImpl) p_obj).getName()));
        }
        return false;
    }

    // PRIVATE MEMBERS
    private String m_name;
    private long m_companyId;
}
