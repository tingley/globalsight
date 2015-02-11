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
package com.globalsight.everest.util.comparator;


import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Locale;
import java.text.Collator;

/**
 * Comparator implementation to enable sorting of Java File objects
 * based on name, size, and last modified date.
 */
@SuppressWarnings("rawtypes")
public class FileComparator implements Comparator, Serializable
{
    private static final long serialVersionUID = -7910355636310552462L;

    // the column that should be sorted
    private int     m_sortColumn = -1;
    // is sort ascending?
    private boolean m_sortAscending = true;
    private Collator m_collator = null;

    public static final int NAME            = 0;
    public static final int SIZE            = 1;
    public static final int DATE            = 2;
    public static final int ABSOLUTE_PATH   = 3;
    
    //////////////////////////////////////////////////////////////////////
    //  Begin: Constructor
    //////////////////////////////////////////////////////////////////////
    /**
    * Construct a FileComparator for sorting purposes based on a particular locale.
    * @param sortCol - The column that should be sorted.
    * @param sortAsc - A boolean that determines whether the sort is ascending or descending.
    */
    public FileComparator(int p_sortCol, Locale p_locale, 
                         boolean p_sortAsc)
    {
        m_sortColumn = p_sortCol;
        m_sortAscending = p_sortAsc;
        m_collator = p_locale == null ? Collator.getInstance() :
            Collator.getInstance(p_locale);        
    }
    //////////////////////////////////////////////////////////////////////
    //  End: Constructor
    //////////////////////////////////////////////////////////////////////

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
        // Note: make sure that the arguments are not null.
        if ((!(o1 instanceof File) || !(o2 instanceof File)) ||
            (o1 == null && o2 == null))
        {
            return 0;
        }
        
        File f1 = (File)o1;
        File f2 = (File)o2;
        int result = 0;
        // if they are same type, do the regular check
        if (f1.isFile() == f2.isFile())
        {
            result = compareFiles(f1, f2);
        }
        else 
        {
            // folder vs. file 
            result = f1.isDirectory() ? -1 : 1;
        }
         
        // set the result based on ascending/descending
        if (!m_sortAscending)
        {
            result = -result;
        }

        return result;
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
        if (object instanceof FileComparator)
        {
            FileComparator compObj = (FileComparator)object;
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

    /**
     * Compare files based on the sort column.
     */
    private int compareFiles(File p_f1, File p_f2)
    {
        int result = 0;
        switch (m_sortColumn)
        {
            default:
                result = compareByName(p_f1, p_f2);
                break;
            case NAME:
                result = compareByName(p_f1, p_f2);
                break;
            case ABSOLUTE_PATH:
                result = compareByAbsolutePath(p_f1, p_f2);
                break;
            case SIZE:
                result = compareBySize(p_f1, p_f2);
                break;
            case DATE:
                result = compareByLastModified(p_f1, p_f2);
                break;
        }

        return result;
    }
                              
    /**
     * Compare the files based on their names.
     */
    private int compareByName(File p_f1, File p_f2)
    {
        String f1Name = p_f1.getName();
        String f2Name = p_f2.getName();

	f1Name = (f1Name != null && f1Name.length() > 0) ?
            f1Name : p_f1.getAbsolutePath();

        f2Name = (f2Name != null && f2Name.length() > 0) ?
            f2Name : p_f2.getAbsolutePath();

        //result = left.compareTo(right);
        return m_collator.getCollationKey(f1Name).compareTo(
            m_collator.getCollationKey(f2Name));
    }
     
    /**
     * Compare the files based on their absolute path.
     */
    private int compareByAbsolutePath(File p_f1, File p_f2)
    {
        return m_collator.getCollationKey(p_f1.getAbsolutePath()).
            compareTo(m_collator.getCollationKey(p_f2.getAbsolutePath()));
    }

    /**
     * Compare the files based on their size.
     */
    private int compareBySize(File p_f1, File p_f2)
    {
	long left = p_f1.length();
        long right = p_f2.length();
        return (left < right) ? -1 : ((left == right) ? 0: 1);
    }	

    /**
     * Compare the files based on their last modified date in milliseconds.
     */
    private int compareByLastModified(File p_f1, File p_f2)
    {
        long left = p_f1.lastModified();
        long right = p_f2.lastModified();
        return (left < right) ? -1 : ((left == right) ? 0: 1);
    }    
}
