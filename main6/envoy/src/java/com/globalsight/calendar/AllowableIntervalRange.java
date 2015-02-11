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
package com.globalsight.calendar;


import java.io.Serializable;
import java.util.Date;

/**
 * AllowableIntervalRange is class used to define a time range from begin date to end date.
 * It only contains the begin and end date of a range.
 */


public class AllowableIntervalRange
    implements Serializable, Comparable<AllowableIntervalRange>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -973931404255963955L;
	private Date m_beginDate = null;
    private Date m_endDate = null;

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Create an initialized AllowableIntervalRange.
     */
    public AllowableIntervalRange(Date p_beginDate, Date p_endDate)
    {
        m_beginDate = p_beginDate;
        m_endDate = p_endDate;
    }
    
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    ////////////////////////////////////////////////////////////////////// 

    /**
     * Get the beginning date for this range.
     * @return The beginning date for this allowable business
     * interval range.
     */
    public Date getBegin()
    {
        return m_beginDate;
    }

    /**
     * Get the end date for this range.
     * @return The end date for this allowable business
     * interval range.
     */
    public Date getEnd()
    {
        return m_endDate;
    }
    
    public int compareTo(AllowableIntervalRange allowableIntervalRange) {
		return this.getBegin().compareTo(allowableIntervalRange.getBegin());
	}
}
