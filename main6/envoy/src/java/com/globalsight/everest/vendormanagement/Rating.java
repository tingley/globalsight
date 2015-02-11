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

package com.globalsight.everest.vendormanagement;


// Globalsight
import java.util.Date;

import com.globalsight.everest.persistence.PersistentObject;
import com.globalsight.everest.taskmanager.Task;

/**
 * This class represents a rating on a vendor.
 */
public class Rating
        extends PersistentObject
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static public final int GOOD_RATING = 1;
    static public final int AVERAGE_RATING = 2;
    static public final int POOR_RATING = 3;

    private int m_rating;
    private Date m_modifiedDate = null;
    private String m_comment = null;
    private String m_raterUserId = null;
    private Task m_task = null;
    private Vendor m_vendor = null;


    //
    // Constructor
    //

    /**
     * Default Constructor - For TOPLink use only.
     */
    public Rating()
    {
    }

    /**
     * Constructor - Used for general rating.
     */
    public Rating(int p_rating,
                  String p_raterUserId,
                  String p_comment)
    {
        this(p_rating, p_raterUserId, p_comment, null);
    }

    /**
     * Constructor - Used for rating for a particular task.
     */
    public Rating(int p_rating,
                  String p_raterUserId,
                  String p_comment,
                  Task p_task)
    {
        //tbd - check if rating is valid
        m_rating = p_rating;
        m_raterUserId =  p_raterUserId;
        m_modifiedDate = new Date();
        m_comment = p_comment;
        m_task = p_task;
        if (p_task != null)
        {
            // set the pointer from task to Rating too
            p_task.addRating(this);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the comment associated with the rating.
     */
    public String getComment()
    {
        return m_comment;
    }

    /**
     * Get the create date/time.
     */
    public Date getModifiedDate()
    {
        return m_modifiedDate;
    }

    /**
     * Get the rater's user Id (the person who created the rating.
     */
    public String getRaterUserId()
    {
        return m_raterUserId;
    }

    
    /**
     * Get the task the rating is associated with.
     * If it returns NULL then the rating is just associated
     * generally with the Vendor and not a particular task they
     * worked on.
     */
    public Task getTask()
    {
        return m_task;
    }

    /** 
     * Return the rating's value.
     */
    public int getValue() 
    {
        return m_rating;
    }

    /**
     * Return the vendor this rating is associated with.
     */
    public Vendor getVendor()
    {
        return m_vendor;
    }

    /**
     * Update the modifiable attributes of rating.
     */
    public void updateRating(int p_rating, String p_comment, 
                             String p_raterUserId)
    {
        m_rating = p_rating;
        m_comment = p_comment;
        m_raterUserId = p_raterUserId;
        m_modifiedDate = new Date();
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Public Helper Methods
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Public Override Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Indicates whether some other Rating is "equal to" this one.
     *
     * @param p_rating  The reference object with which to compare.
     * @return true if this rating object is the same as the
     * p_rating argument; false otherwise.
     */
    public boolean equals(Object p_rating)
    {
	if (p_rating instanceof Rating)
        {
	    return (getId() == ((Rating)p_rating).getId());
	}
	return false;
    }
    
    /**
     * The hashCode method is overridden to support the 'equals' method.  
     * If two Rating objects are equal according to the equals(Object) 
     * method, then calling the hashCode method on each of the two objects 
     * will produce the same integer result. 
     *
     *  @return the rating id.
     */
    public int hashCode()
    {
        return getIdAsLong().hashCode();
    }

    /**
     * Get the string representation of this rating object.
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Rating[ id = ");
        sb.append(getId());
        sb.append(", rating = ");
        sb.append(m_rating);
        sb.append(", comment = ");
        sb.append(m_comment);
        sb.append(" ]");
        return sb.toString();
    }

    //////////////////////////////////////////////////////////////////////
    //  End: Public Override Methods
    //////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////
    //  Begin: Package-scope Methods
    //////////////////////////////////////////////////////////////////////

    /**
     * Set the vendor which this rating is associated with
     * to be the specified vendor.
     */
    void setVendor(Vendor p_vendor)
    {
        m_vendor = p_vendor;
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Package-scope Methods
    //////////////////////////////////////////////////////////////////////

	public void setTask(Task m_task)
	{
		this.m_task = m_task;
	}

	public int getRating()
	{
		return m_rating;
	}

	public void setRating(int m_rating)
	{
		this.m_rating = m_rating;
	}

	public void setModifiedDate(Date date)
	{
		m_modifiedDate = date;
	}

	public void setRaterUserId(String userId)
	{
		m_raterUserId = userId;
	}

	public void setComment(String m_comment)
	{
		this.m_comment = m_comment;
	}

}
