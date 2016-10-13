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
package com.globalsight.everest.webapp.applet.common;


import javax.swing.AbstractListModel;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * This is a sorted list model used for a JList.  It takes a list and
 * sorts it in ascending element order, according to the natural ordering 
 * of its elements.
 */



public class SortedJListModel extends AbstractListModel 
{

    // a sorted list model
    private SortedSet m_sortedSet = null;
    // the initial collection 
    private List m_initialCollection = null;



    /////////////////////////////////////////////////////////////////////////
    // Begin:  Constructors
    /////////////////////////////////////////////////////////////////////////
    /**
    * Construct a sorted list model.
    * @param p_collection - The collection to be sorted.
    */
    public SortedJListModel(List p_collection) 
    {        
        m_initialCollection = p_collection;

        m_sortedSet = new TreeSet(p_collection);        
    }
    /////////////////////////////////////////////////////////////////////////
    // End:  Constructors
    /////////////////////////////////////////////////////////////////////////




    /////////////////////////////////////////////////////////////////////////
    // Begin:  ListModel Implementation
    /////////////////////////////////////////////////////////////////////////
    /**
     * Get the size of the sorted collection.
     * @return The size of the collection. 
     */
    public int getSize() 
    {
        // Return the model size
        return m_sortedSet.size();
    }

    /**
     * Get the element at a specified index from the collection.
     * @param index - The index of the element.
     * @return The object at that particular index. 
     */
    public Object getElementAt(int p_index) 
    {
        // Return the appropriate element
        Object[] array = getListItems();

        return array[p_index];
    }
    /////////////////////////////////////////////////////////////////////////
    // End: ListModel Implementation
    /////////////////////////////////////////////////////////////////////////




    /////////////////////////////////////////////////////////////////////////
    // Begin: Local Methods (Set Implementation)
    /////////////////////////////////////////////////////////////////////////
    /**
     * Add an element to the collection.
     * @param element - The element to be added.
     */
    public void add(Object element) 
    {
        // if the element is added successfully, fire the contents changed event to
        // refresh the list model
        if (m_sortedSet.add(element))
        {
            fireContentsChanged(this, 0, getSize());
        }
    }

    /**
     * Add a collection of elements to this SortedSet.
     * @param elements - The elements to be added.
     * @return True if the elements were added to the collection, 
     * otherwise return false. 
     */
    public boolean addAll(Collection p_elements) 
    {
        boolean added = m_sortedSet.addAll(p_elements);
        if (added)
            fireContentsChanged(this, 0, getSize());

        return added;
    }

    /**
     * Removes all of the elements from this collection.
     */
    public void clear() 
    {
        m_sortedSet.clear();
        fireContentsChanged(this, 0, getSize());
    }

    /**
     * Determines whether this collection contains the specified element.
     * @param p_element - The object to be searched in the collection.
     * @return True if the collection contains this element.
     */
    public boolean contains(Object p_element) 
    {
        return m_sortedSet.contains(p_element);
    }

    /**
     * Get the first (lowest) element currently in this sorted set.
     * @return The first element of the collection.
     */
    public Object firstElement() 
    {
        // Return the appropriate element
        return m_sortedSet.first();
    }

    /**
     * Get the initial collection that was passed to this object for sorting.
     * @return The initial collection.
     */
    public List getInitialCollection()
    {
        return m_initialCollection;
    }

    /**
     * Get the items of the list as an array of objects.
     */
    public Object[] getListItems()
    {
        return m_sortedSet.toArray();
    }

    /**
     * Get an iterator over the elements in this collection.
     * @return An iterator over the elements in this collection.
     */
    public Iterator iterator() 
    {
        return m_sortedSet.iterator();
    }
    
    /**
     * Get the last (highest) element currently in this sorted set.
     * @return The last element of the collection.
     */
    public Object lastElement() 
    {
        return m_sortedSet.last();
    }    

    /**
     * Remove a collection of elements.
     * @param elements - the elements to be removed.
     * @return True if the elements were removed from the collection, 
     * otherwise return false. 
     */
    public boolean removeAll(Collection p_elements) 
    {
        boolean removed = m_sortedSet.removeAll(p_elements);
        if (removed)
        {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;   
    }

    /**
     * Remove an array of elements from the SortedSet.
     * @param elements - the elements to be removed.
     * @return True if the elements were removed from the collection, 
     * otherwise return false. 
     */
    public boolean removeAll(Object[] p_elements) 
    {
        return removeAll(Arrays.asList(p_elements));        
    }

    /**
     * Remove an element from the collection.
     * @param element - The element to be removed.
     * @return True if the element has been removed successfully, 
     * otherwise return false. 
     */
    public boolean remove(Object p_element) 
    {
        boolean removed = m_sortedSet.remove(p_element);
        if (removed)
        {
            fireContentsChanged(this, 0, getSize());
        }
        return removed;   
    }
    /////////////////////////////////////////////////////////////////////////
    // End: Local Methods (Set Implementation)
    /////////////////////////////////////////////////////////////////////////
}
