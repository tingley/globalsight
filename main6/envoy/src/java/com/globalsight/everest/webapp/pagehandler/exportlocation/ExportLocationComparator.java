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
package com.globalsight.everest.webapp.pagehandler.exportlocation;

// globalsight
import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManager;
import com.globalsight.cxe.persistence.exportlocation.ExportLocationPersistenceManagerWLRemote;
import com.globalsight.cxe.entity.exportlocation.ExportLocationImpl;
import com.globalsight.cxe.entity.exportlocation.ExportLocation;
import com.globalsight.util.GlobalSightLocale;

// java
import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator implementation to enable sorting of export location objects.
 */
public class ExportLocationComparator
    implements Comparator, Serializable
{
    public static final int NAME             = 1;
    public static final int LOCATION         = 2;
    public static final int DESCRIPTION      = 3;
    public static final int DEFAULT          = 4;

    private static final int lessThan        = -1;
    private static final int greaterThan     = 1;
    private static final int equalTo         = 0;

    private int m_criteria                   = -1;
    private boolean m_reverse                = true;

    public int compare(Object p_object1, Object p_object2)
    {
        if (!(p_object1 instanceof ExportLocation))
        {
            throw new ClassCastException();
        }

        if (!(p_object2 instanceof ExportLocation))
        {
            throw new ClassCastException();
        }

        ExportLocation el1 = (ExportLocation)p_object1;
        ExportLocation el2 = (ExportLocation)p_object2;

        if (el1 != null && el2 != null)
        {
            switch (m_criteria)
            {
            case LOCATION:
                return compareByLocation(el1, el2);
            case DESCRIPTION:
                return compareByDescription(el1, el2);
            default:
                m_criteria = NAME;
                return compareByName(el1, el2);
            }
        }

        return equalTo;
    }

    public boolean equals(Object p_object)
    {
        if (p_object == this)
        {
            return true;
        }

        return false;
    }

    public int getComparisonCriteria()
    {
        return m_criteria;
    }

    public void setComparisonCriteria(int p_criteria)
    {
        m_criteria = p_criteria;
    }

    public boolean getReverse()
    {
        return m_reverse;
    }

    public void setReverse(boolean p_reverse)
    {
        m_reverse = p_reverse;
    }

    private int compareByName(ExportLocation p_el1, ExportLocation p_el2)
    {
        return p_el1.getName().compareTo(p_el2.getName());
    }

    private int compareByLocation(ExportLocation p_el1, ExportLocation p_el2)
    {
        return p_el1.getLocation().compareTo(p_el2.getLocation());
    }

    private int compareByDescription(ExportLocation p_el1, ExportLocation p_el2)
    {
        return p_el1.getDescription().compareTo(p_el2.getDescription());
    }
}
