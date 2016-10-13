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
package com.globalsight.util;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import com.globalsight.everest.foundation.Timestamp;

/**
 * This class is an implementation of Comparator which is used for
 * sorting data in a a table with multiple columns of different types.
 *
 */
public abstract class EnvoyDataComparator
    implements Comparator, Serializable
{
    private static final long serialVersionUID = 1L;

    // the column that should be sorted
    private int     m_sortColumn = -1;
    // is sort ascending?
    private boolean m_sortAscending = true;
    private Locale m_locale;
    private transient Collator m_collator = null;

    protected void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        m_collator = m_locale == null ? Collator.getInstance() :
            Collator.getInstance(m_locale);
    }
    //
    //  Begin:  Constructor
    //

    /**
     * Construct a EnvoyDataComparator for sorting purposes based on
     * the default locale.
     * @param sortCol - The column that should be sorted.
     * @param sortAsc - A boolean that determines whether the sort is
     * ascending or descending.
     */
    public EnvoyDataComparator(int p_sortCol)
    {
        this(p_sortCol, (Locale)null, true);
    }


    /**
     * Construct a EnvoyDataComparator for sorting purposes based on a
     * particular locale.
     * @param sortCol - The column that should be sorted.
     * @param sortAsc - A boolean that determines whether the sort is
     * ascending or descending.
     */
    public EnvoyDataComparator(int p_sortCol, GlobalSightLocale p_locale,
        boolean p_sortAsc)
    {
        this(p_sortCol, p_locale.getLocale(), p_sortAsc);
    }

    /**
     * Construct a EnvoyDataComparator for sorting purposes based on a
     * particular locale.
     * @param sortCol - The column that should be sorted.
     */
    public EnvoyDataComparator(int p_sortCol, Locale p_locale)
    {
        this(p_sortCol, p_locale, true);
    }

    /**
     * Construct a EnvoyDataComparator for sorting purposes based on a
     * particular locale.
     * @param sortCol - The column that should be sorted.
     * @param sortAsc - A boolean that determines whether the sort is
     * ascending or descending.
     */
    public EnvoyDataComparator(int p_sortCol, Locale p_locale,
        boolean p_sortAsc)
    {
        m_sortColumn = p_sortCol;
        m_sortAscending = p_sortAsc;
        m_locale = p_locale;
        m_collator = p_locale == null ? Collator.getInstance() :
            Collator.getInstance(p_locale);
    }

    //
    //  End:  Constructor
    //

    //////////////////////////////////////////////////////////////////////
    //  Begin: Abstract Methods
    //////////////////////////////////////////////////////////////////////

    public abstract Object[] getComparableObjects(Object o1, Object o2,
        int sortColumn);
    //////////////////////////////////////////////////////////////////////
    //  End: Abstract Methods
    //////////////////////////////////////////////////////////////////////

    //
    //  Begin:  Comparator Implementation
    //

    /**
     * Compares the two arguments for ordering purposes.
     * @param o1 - The first argument for comparison against another
     * argument.
     * @param o2 - The second argument for comparison against the
     * first one.
     * @return The result of the comparison which is a negative
     * integer, zero, or a positive integer as the first argument is
     * less than, equal to, or greater than the second one.
     */
    public int compare(Object o1, Object o2)
    {
        Object[] obj = getComparableObjects(o1, o2, m_sortColumn);
        Object first = obj[0];
        Object second = obj[1];

        // Note: make sure that the arguments are not null.
        if (first == null && second == null)
        {
            return 0;
        }
        else if (first == null)
        {
            return -1;
        }
        else if (second == null)
        {
            return 1;
        }

        // the result value to be returned after the comparison.
        int result = 0;

        // String -- need to use collator
        if (first instanceof String)
        {
            String left = (String)first;
            String right = (String)second;
            //result = left.compareTo(right);
            if (m_collator == null) 
            {
                m_collator = m_locale == null ? Collator.getInstance() :
                    Collator.getInstance(m_locale);            	
            }
            
            if (left.indexOf("(sheet") > 0 && right.indexOf("(sheet") > 0)
            {
            	String aMainName = this.getMainFileName(left);
                String aSubName = this.getSubFileName(left);
                String bMainName = this.getMainFileName(right);
                String bSubName = this.getSubFileName(right);
                
                result = aMainName.compareTo(bMainName);
                if (result == 0)
                {
                	if (aSubName.matches("\\(sheet\\d+\\)") && bSubName.matches("\\(sheet\\d+\\)"))
                	{
                		String n1 = aSubName.substring(6, aSubName.length() - 1);
                		String n2 = bSubName.substring(6, bSubName.length() - 1);
                		result = Integer.parseInt(n1) - Integer.parseInt(n2);
                	}
                }
            }
            else
            {
            	result = m_collator.getCollationKey(left).compareTo(
                        m_collator.getCollationKey(right));
            }
        }
        else if (first instanceof Timestamp) // Envoy's Timestamp
        {
            long left = ((Timestamp)first).getTimeInMillisec();
            long right = ((Timestamp)second).getTimeInMillisec();
            result = (left < right) ? -1 : ((left == right) ? 0: 1);
        }
        // Date
        else if (first instanceof Date)
        {
            long left = ((Date)first).getTime();
            long right = ((Date)second).getTime();
            result = (left < right) ? -1 : ((left == right) ? 0: 1);
        }
        // Char
        else if (first instanceof Character)
        {
            char left = ((Character)first).charValue();
            char right = ((Character)second).charValue();
            result = (left < right) ? -1 : ((left == right) ? 0: 1);
        }
        // BigDecimal
        else if (first instanceof BigDecimal)
        {
            BigDecimal left = (BigDecimal)first;
            BigDecimal right = (BigDecimal)second;
            result = left.compareTo(right);
        }
        // BigInteger
        else if (first instanceof BigInteger)
        {
            BigInteger left = (BigInteger)first;
            BigInteger right = (BigInteger)second;
            result = left.compareTo(right);
        }
        // Number
        else if (first instanceof Number)
        {
            Number leftValue = (Number)first;
            Number rightValue = (Number)second;
            if (first instanceof Float || first instanceof Double)
            {
                double left = leftValue.doubleValue();
                double right = rightValue.doubleValue();
                result = (left < right) ? -1 : ((left == right) ? 0: 1);
            }
            else if (first instanceof Long)
            {
                long left = leftValue.longValue();
                long right = rightValue.longValue();
                result = (left < right) ? -1 : ((left == right) ? 0: 1);
            }
            else
            {
                int left = leftValue.intValue();
                int right = rightValue.intValue();
                result = (left < right) ? -1 : ((left == right) ? 0: 1);
            }
        }
        // Other...
        else
        {
            String s1 = first.toString();
            String s2 = second.toString();
            //result = s1.compareTo(s2);
            result = m_collator.getCollationKey(s1).compareTo(
                m_collator.getCollationKey(s2));
        }

        // set the result based on ascending/descending
        if (!m_sortAscending)
        {
            result = -result;
        }

        return result;
    }
    
    /**
     * Extracts the filename part of an MsOffice multipart file:
     * "(header) en_US/ppt.ppt" --&gt; "en_US/ppt.ppt".
     */
    public String getMainFileName(String p_filename)
    {
    	p_filename = p_filename.replace("\\", "/");
    	int index = p_filename.lastIndexOf("/");
    	if (index > 0)
    	{
    		p_filename = p_filename.substring(index + 1);
    	}
    	
    	index = p_filename.indexOf(" (");
    	if (index > 0)
    	{
    		p_filename = p_filename.substring(0, index);
    	}
    	
        return p_filename;
    }

    /**
     * Extracts the sub-file part of an MsOffice multipart file:
     * "(header) en_US/ppt.ppt" --&gt; "(header)".
     */
    public String getSubFileName(String p_filename)
    {
        int index = p_filename.lastIndexOf("(");
        if (index > 0 && p_filename.endsWith(")"))
        {
            return p_filename.substring(index + 1, p_filename.length() - 1);
        }

        return "";
    }


    /**
     * Indicates whether some other object is "equal to" this
     * Comparator. (Note: This method overrides equal in class Object.)
     * @param object - the reference object with which to compare.
     * @return True only if the specified object is also a comparator
     * and it imposes the same ordering as this comparator.
     */
    public boolean equals(Object object)
    {
        if (object instanceof EnvoyDataComparator)
        {
            EnvoyDataComparator compObj = (EnvoyDataComparator)object;
            return  (compObj.m_sortColumn == m_sortColumn) &&
                    (compObj.m_sortAscending == m_sortAscending);
        }

        return false;
    }

    //////////////////////////////////////////////////////////////////////
    //  Begin: Helper Methods
    //////////////////////////////////////////////////////////////////////
    /**
     * Get the column number used for sorting the list.
     * @return The column number used for sorting the list.
     */
    public int getSortColumn()
    {
        return m_sortColumn;
    }

    /**
     * Get the sort direction
     * @return sortAscending boolean
     */
    public boolean getSortAscending()
    {
        return m_sortAscending;
    }

    /**
     * Reverse the sorting order flag.
     */
    public void reverseSortingOrder()
    {
        m_sortAscending = !m_sortAscending;
    }

    /**
     * Set the sort column to be the specified value.
     * @param p_sortColumn - The sort column to be set.
     */
    public void setSortColumn(int p_sortColumn)
    {
        m_sortColumn = p_sortColumn;
    }     
    //////////////////////////////////////////////////////////////////////
    //  End: Helper Methods
    //////////////////////////////////////////////////////////////////////
}
