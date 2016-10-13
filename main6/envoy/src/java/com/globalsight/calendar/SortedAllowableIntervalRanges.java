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


// JDK
import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;


/**
 * SortedAllowableIntervalRanges is the sub-class of WorkingDay and is associated with
 * a Calendar.  Since an object can only be mapped to one table, this sub-class
 * is required.
 */


public class SortedAllowableIntervalRanges
    implements Serializable
{

    /**
	 * 
	 */
	private static final long serialVersionUID = -1158452407897191906L;
	private Iterator<AllowableIntervalRange> m_iterator = null;

    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
     * Construct a SortedAllowableIntervalRanges with given sorted set.
     * @param p_allowableBizIntervalRanges - a sorted set of AllowableIntervalRange
     */
    public SortedAllowableIntervalRanges(SortedSet<AllowableIntervalRange> p_allowableBizIntervalRanges)
    {
        super();
        if (p_allowableBizIntervalRanges != null)
        {
            m_iterator = p_allowableBizIntervalRanges.iterator();
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    ////////////////////////////////////////////////////////////////////// 

    /**
     * Determines whether the sorted allowable interval range has
     * more elements.
     * @return True if the iteration has more elements; otherwise return false.
     */
    public boolean hasNext()
    {
        return m_iterator == null ? false : m_iterator.hasNext();
    }

    /**
     * Get the next allowable interval range (if any).
     * @return The next allowable interval range in the iteration.
     *
     * @throws NoSuchElementException - Thrown when iteration has 
     * no more elements.  
     */
    public AllowableIntervalRange nextRange()
        throws NoSuchElementException
    {
        if (m_iterator == null)
        {
            return null;
        }

        return m_iterator.next();
    }
}
