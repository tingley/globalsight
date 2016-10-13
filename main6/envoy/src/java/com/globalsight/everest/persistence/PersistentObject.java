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
package com.globalsight.everest.persistence;

// This class must remain in the same package as TopLinkPersistence,
// PersistenceService, and TopLinkProject, because of references to
// the package-scoped method makeEditable().  It should move to
// com.globalsight.persistence after release.

import java.io.Serializable;
import java.sql.Timestamp;
 
import com.globalsight.util.edit.EditUtil;

/**
 * PersistentObject is the common base class for entities in the
 * Globalsight system that are considered persistent in some way
 * (usually though database persistence).  Provides some data and
 * behavior common to all persistent objects.
 */
public abstract class PersistentObject implements Serializable
{
    private static final long serialVersionUID = 1528686961151128501L;
    //
    // PUBLIC CONSTANTS -- FOR USE BY TOPLINK
    //
    public static final String M_ID = "m_id";
    public static final String M_TIMESTAMP = "m_timestamp";
    public static final String M_NAME = "m_name";
    public static final String M_IS_ACTIVE = "m_isActive";
    public static final long INITIAL_ID = -1L;

    // constants used by subclasses
    public static final int CLOB_THRESHOLD = 65500;// 65535 ("text" length in mysql)

    //
    // PRIVATE CONSTANTS
    //
    private static final Long INITIAL_ID_AS_LONG = new Long(INITIAL_ID);

    //
    // PRIVATE MEMBER VARIABLES
    //
    private long m_id;
    protected String m_name;
    private Timestamp m_timestamp;
    private /*transient*/ boolean m_editable;
    private transient Long m_idAsLong;
    private boolean m_isActive = true;

    //
    // PUBLIC CONSTRUCTORS
    //

    /**
     * Create an initialized persistent object.
     */
    public PersistentObject()
    {
        super();
        initializeMembers();
    }

    //
    // PUBLIC METHODS
    //
    public void setId(long p_id)
    {
        if (m_id == INITIAL_ID)
        {
            m_id = p_id;
        }
    }

    /**
     * Return the persistent object's id.
     *
     * @return the unique identifier.
     */
    public long getId()
    {
        return m_id;
    }

    /**
     * Return the name of the object.
     *
     * @return the name of the object
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Set the name of the object.
     *
     * @param p_name Set the object name to the one specified.
     */
    public void setName(String p_name)
    {
        m_name = p_name;
    }

    public void setTimestamp(Timestamp p_timestamp)
    {
        m_timestamp = p_timestamp;
    }

    /**
     * Return whether this persistent object is new or not --
     * i.e. whether it has EVER been saved to the database.
     *
     * @return true if the object is new (i.e its id value is negative)
     */
    public boolean isNew()
    {
        return (getId() == INITIAL_ID);
    }

    /**
     * Return the persistent object's id as a Long object.<p>
     *
     * This is a convenience method that simply wraps the id as an
     * object, so that, for example, the idAsLong can be used as a
     * Hashtable key.
     *
     * @return the unique identifier as a Long object.
     */
    public Long getIdAsLong()
    {
        return(isNew() ? INITIAL_ID_AS_LONG : idAsLong());
    }

    /**
     * Prepare the PersistentObject so that it can be inserted into
     * the database as a new object.  In order for this to succeed,
     * the persistent object must have a non-negative ID, and it must
     * be editable (signifying that it was already cloned by the
     * persistence service).
     *
     * @throws RuntimeException if the object is not editable.
     */
    public void makeNew()
    {
        initializeMembers();
    }

    /**
     * Tells if the object is active or inactive (logically deleted).
     *
     * @return true if the object is active.
     */
    public boolean isActive()
    {
        return m_isActive;
    }

    /**
     * Makes the object active or in-active.  If a subclass wants to
     * expose this method they can make it public with their interface
     * (for example the LDAP objects need to make it public).
     */
    protected void isActive(boolean p_isActive)
    {
        m_isActive = p_isActive;
    }

    /**
     * Truncates a Unicode string so that its UTF-8 representation (in
     * bytes) is less than MAX bytes.
     *
     * This method uses a simple, non-optimized loop implementation.
     * Therefore, don't call this function too often.
     *
     * If MAX is <= 0, the original string is returned.
     */
    public static final String truncateString(String p_string, int p_max)
    {
        if (p_string == null || p_string.length() == 0 || p_max <= 0)
        {
            return p_string;
        }

        while (EditUtil.getUTF8Len(p_string) > p_max)
        {
            p_string = p_string.substring(0, p_string.length() - 1);
        }

        return p_string;
    }

    /**
     * Return a description of the persistent object.
     *
     * @return a string representation of the object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append(super.toString());
        sb.append("{m_id=");
        sb.append(m_id);
        sb.append(", m_name=");
        sb.append(m_name);
        sb.append(", m_timestamp=");
        sb.append(m_timestamp);
        sb.append(", m_editable=");
        sb.append(m_editable);
        sb.append(", m_isActive=");
        sb.append(m_isActive);
        sb.append("}");

        return sb.toString();
    }

    //
    // PACKAGE-SCOPED METHODS
    // These methods must be visible to PersistenceService and to subclasses
    // but not to anyone else.
    //

    /**
     * Indicate that the persistent object can be changed so that
     * persistence services can correctly merge the clone back into
     * the cached object.
     */
    void makeEditable()
    {
        m_editable = true;
    }

    /**
     * Return true if the persistent object can be changed; false
     * otherwise.
     *
     * @return the current editability state of the persistent object.
     */
    boolean isEditable()
    {
        return m_editable;
    }

    //
    // PROTECTED METHODS
    //

    /**
     * This method can be used by any subclass of PersistentObject in
     * which the m_id attribute is not mapped.  For example, if a
     * database table has a character-based key rather than a numeric
     * key, then there would be no mapping for m_id, and the object
     * would obtain its key value from some other means.  In that
     * case, the subclass would override its getId() method to call
     * getTemporarilyUnavailableId() and force a run time
     * exception.<p>
     *
     * Eventually, all entities in our system will map to tables that
     * use a numeric id, and when that happens the subclasses can
     * complete the mapping and remove the overridden getId() method.
     */
    protected long getTemporarilyUnavailableId()
    {
        throw new RuntimeException(
            "m_id is not mapped for this PersistentObject; " +
            "getId() should not be called.");
    }

    //
    // PRIVATE SUPPORT METHODS
    //

    /**
     * Gets the current value of the id as a long; creates a new one
     * if necessary.
     */
    private Long idAsLong()
    {
        if (m_idAsLong == null)
        {
            m_idAsLong = new Long(getId());
        }

        return m_idAsLong;
    }

    /** Sets all member variables to initial values.*/
    private void initializeMembers()
    {
        m_id = INITIAL_ID;
        m_name = "";
        m_timestamp = new Timestamp(System.currentTimeMillis());
        m_editable = false;
        m_idAsLong = null;
        m_isActive = true;
    }

	public boolean getIsActive()
	{
		return m_isActive;
	}

	public void setIsActive(boolean active)
	{
		m_isActive = active;
	}

    public Timestamp getTimestamp()
    {
        return m_timestamp;
    }
}
